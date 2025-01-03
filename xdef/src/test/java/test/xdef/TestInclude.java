package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDConstants;
import org.xdef.XDPool;
import java.io.File;
import java.io.StringWriter;
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

/** Test of attribute "include" in header of XDefinition and of "xi:include"
 * in XML data.
 * @author Vaclav Trojan
 */
public final class TestInclude extends XDTester {

	public TestInclude() {super();}

	@Override
	public void test() {
		String xdef;
		String xml;
		ArrayReporter reporter = new ArrayReporter();
		XDPool xp;
		StringWriter swr, swr1;
		String s;
		Report report;
		String dataDir = new File(getDataDir(), "test").getAbsolutePath()
			.replace('\\', '/');
		if (!dataDir.endsWith("/")) {
			dataDir += '/';
		}
		boolean chkSyntax = getChkSyntax();
		try {
			//xd:include in XDefinition header
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='foo'"+
"        xd:include = \"" + dataDir +"TestInclude_1.xdef\">\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>";
			xp = compile(xdef);
			swr = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("f", swr.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			swr = new StringWriter();
			parse(xp,"a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("bf", swr.toString());
			// the same from file
			xdef = dataDir + "TestInclude.xdef";
			xp = compile(xdef);
			swr = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("f", swr.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			swr = new StringWriter();
			parse(xp,"a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("bf", swr.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			//xi:include in collection
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name = 'a' root = 'foo'>\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>\n"+
"<xi:include xmlns:xi = '"+XDConstants.XINCLUDE_NS_URI+"'\n" +
"            href = '" + dataDir + "TestInclude_1.xdef" + "' />\n" +
"</xd:collection>";
			xp = compile(xdef);
			swr = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("f", swr.toString());
			xml = "<foo><bar><foo/></bar></foo>";
			swr = new StringWriter();
			parse(xp,"a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("bf", swr.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			//include attribute in collection
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'\n"+
	"include='" + dataDir + "TestInclude.xdef'/>";
			xp = compile(xdef);
			swr = new StringWriter();
			xml = "<foo><bar><foo/></bar></foo>";
			parse(xp,"a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("bf", swr.toString());
		} catch (Exception ex) {fail(ex);}
		try {//xinclude
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a><b>required string()</b></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_1_1.xml";
			assertEq("<a><b>&lt;c>text&lt;/c></b></a>",
				parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = dataDir + "TestInclude_2_1.xml";
			parse(xp, "", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				report = reporter.getReport();
				if (report == null) {
					fail("Error not reported");
				} else {
					while (report != null && ("XML308".equals(report.getMsgID())
						|| "XDEF539".equals(report.getMsgID()))) {
						report = reporter.getReport();
					}
				}
				while (report != null) {
					fail("Unexpected error: " + report.toString());
				}
			}
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !s.contains("Not_Exists.File")) {
				fail(ex);
			}
		}
		try {// xinclude
			xdef =
"<xdef:def xmlns:xdef='" + _xdNS + "' xdef:name=\"test\" xdef:root=\"a\">\n"+
"  <a><b>required string()</b></a>\n"+
"</xdef:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_3_1.xml";
			parse(xp, "test", xml, reporter);
			if (!reporter.errors()) {
				fail("Error not reported");
			} else {
				while (!"XML308".equals(
					(report=reporter.getReport()).getMsgID()) &&
					!"XML306".equals(report.getMsgID()) &&
					!"XDEF527".equals(report.getMsgID()) &&
					!"SYS036".equals(report.getMsgID()) &&
					!"SYS033".equals(report.getMsgID())) {
					fail(report.toString());
				}
			}
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || !(s.contains("SYS033") || s.contains("XML306"))) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='a'>\n"+
"  <a><b>required string()</b></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_4_1.xml";
			assertEq("<a><b>&lt;c>text&lt;/c></b></a>",
				parse(xp, "test", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			setChkSyntax(false);
			xdef = dataDir + "TestInclude_5.xdef";
			xp = compile(xdef);
			xml = dataDir + "TestInclude_5.xml";
			parse(xp, "wsdefinitions", xml);
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {
			xdef = dataDir + "TestInclude_6.xdef";
			try {
				setChkSyntax(false);
				compile(xdef);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				s = ex.getMessage();
				if (s==null || !s.contains("XML308") && !s.contains("XML075")) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {// once more with ignoreEntities
			xdef = dataDir + "TestInclude_7.xdef";
			try {
				setChkSyntax(false);
				compile(xdef);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				s = ex.getReport().getModification();
				if (s==null || !s.contains("XML308") && !s.contains("XML075")) {
					fail(ex);
				}
			}
		} catch (Exception ex) {fail(ex);}
		setChkSyntax(chkSyntax);
		try {
			xp = compile(dataDir + "TestInclude_8.xdef");
			xml = dataDir + "TestInclude_8.xml";
			parse(xp, "A", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = dataDir + "TestInclude_8_6.xml";
			parse(xp, "A", xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			//test Include default (not allowed)
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' xd:root ='A'>\n"+
"  <A><b/></A>\n"+
"</xd:def>";
			xp = compile(xdef); //here is default (not allowed)
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
			xml = dataDir + "TestInclude_9.xml";
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			// include allowed from properties
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			xp = compile(xdef);
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(xdef);
			 // from program
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			xdef = dataDir + "TestInclude_9_1.xdef";
			// not allowed from properties
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
			// allowed from properties
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_TRUE);
			//allowed
			xp = compile(xdef);
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(xdef);
			 // from program
			assertEq("<A><b/></A>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			setProperty(XDConstants.XDPROPERTY_XINCLUDE,
				XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);
			parse(xp, "", xml, reporter);
			assertTrue(reporter.errors() &&
				reporter.printToString().indexOf("XML309") > 0);
		} catch (Exception ex) {fail(ex);}

		try { // xd:include='classpath://...
			xp = compile(
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/3.2'\n" +
"xd:include='classpath://test.xdef.data.test.TestInclude.xdef;\n"+
"            classpath://test.xdef.data.test.TestInclude_10.xdef'/>");
			xml = "<B b='123'/>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			xml = "<foo/>";
			parse(xp, "a", xml, reporter, swr, null, null);
			if (reporter.errors()) {
				swr1 = new StringWriter();
				ReportPrinter.printListing(swr1, xml, reporter, true);
				fail(swr1.toString());
			}
			assertEq("f", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='foo'\n"+
"        xd:include='classpath://test.xdef.data.test.TestInclude_10.xdef'>\n"+
"  <foo>\n"+
"    <bar xd:script = '*; ref B#B'/>\n"+ // B is from include
"  </foo>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<foo/>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp,
				"A",
				"classpath://test.xdef.data.test.TestInclude_10.xml",
				reporter),
				"classpath://test.xdef.data.test.TestInclude_10.xml");
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}