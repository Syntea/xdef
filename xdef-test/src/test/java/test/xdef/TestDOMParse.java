package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.xml.KDOMBuilder;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDOutput;
import java.io.StringWriter;
import org.w3c.dom.Element;

/** Test of checking of DOM object.
 * @author  Vaclav Trojan
 */
public final class TestDOMParse extends XDTester {

	public TestDOMParse() {super();}

	@Override
	public void test() {
		try {
			test1();
		} catch (Error ex) {
			fail(ex);
		}
		resetTester();
	}

	private void test1() {
		String xml;
		String xdef;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Report rep;
		Element el;
		XDDocument xd;
		XDOutput out;
		StringWriter strw;
		String s;
		KDOMBuilder builder;
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = \"root\" name = \"root\">\n"+
"<root attr='required string()'\n"+
"      xd:script=\"finally out('xyz')\">\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"</xd:def>\n";
			xml = "<root attr='attr'><a/><a/></root>";
			xp = compile(xdef);
			xd = xp.createXDDocument("root");
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			parse(xd, xml, reporter);
			strw.close();
			String result = strw.toString();
			if (!result.equals("xyz")) {
				fail("Unexpected result: " + result);
			}
			if (reporter.errors()) {
				ReportPrinter.printListing(System.out, xml, reporter, true);
				fail("Unexpected error");
			}
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = \"root\" name = \"root\">\n"+
"<root attr='required string()'\n"+
"      xd:script=\"finally out('xyz')\">\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<root bttr='attr'><a/><a/></root>";
			xd = xp.createXDDocument("root");
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			parse(xd, xml, reporter);
			strw.close();
			s = strw.toString();
			if (!s.equals("xyz")) {
				fail("Unexpected result: " + s);
			}
			if (reporter.getErrorCount() != 2) {
				ReportPrinter.printListing(System.out, xml, reporter, true);
				fail("Unexpected number of errors: "
					+ reporter.getErrorCount());
			} else {
				boolean rep525 = false;
				boolean rep526 = false;
				while ((rep = reporter.getReport()) != null) {
					if ("XDEF525".equals(rep.getMsgID())) {
						rep525 = true;
					} else if ("XDEF526".equals(rep.getMsgID())) {
						rep526 = true;
					} else {
						fail("Unexpected error report: " + rep.toString());
					}
				}
				if (!(rep525 & rep526)) {
					fail("Unexpected error reports.");
				}
			}
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = \"root\" name = \"root\">\n"+
"<root attr='required string()'\n"+
"      xd:script=\"finally out('xyz')\">\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"</xd:def>\n";
			xml = "<root bttr='attr'><a/><b/></root>";
			xp = compile(xdef);
			xd = xp.createXDDocument("root");
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			parse(xd, xml, reporter);
			strw.close();
			s = strw.toString();
			if (!s.equals("xyz")) {
				fail("Unexpected result: " + s);
			}
			if (reporter.errorWarnings()) {
				if ((rep = reporter.getReport()) == null ||
					!"XDEF525".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/@bttr") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF526".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/@attr") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF555".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/a") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF501".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root") < 0) {
					fail("error: " + rep);
				}
				while ((rep = reporter.getReport()) != null) {
					fail("Unexpected error message: " + rep);
				}
			} else {
				fail("Errors not reported");
			}
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = \"root\" name = \"root\">\n"+
"<root attr='required string()'>\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"\n"+
"</xd:def>\n";
			xml = "<root bttr='attr'><a/><b/></root>";
			xp = compile(xdef);
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			parse(xp, "root", el, reporter);
			if (reporter.errorWarnings()) {
				if ((rep = reporter.getReport()) == null ||
					!"XDEF525".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/@bttr") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF526".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/@attr") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF555".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root/a") < 0) {
					fail("error: " + rep);
				}
				if ((rep = reporter.getReport()) == null ||
					!"XDEF501".equals(rep.getMsgID()) ||
					rep.getModification().indexOf("/root") < 0) {
					fail("error: " + rep);
				}
				while ((rep = reporter.getReport()) != null) {
					fail("Unexpected error message: " + rep);
				}
			} else {
				fail("Errors not reported");
			}
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root = \"root\" name = \"root\">\n"+
"<root attr='required string()'>\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"</xd:def>\n";
			xml = "<root bttr='attr'><a/><b/></root>";
			xp = compile(xdef);
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			parse(xp, "root", el);
			fail("Exception not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || s.indexOf("E XDEF525") < 0 ||
				s.indexOf("/root/@bttr") < 0 ) {
				fail(ex);
			}
		}
		try {
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "' >\n"+
"<xd:def\n"+
"xmlns:s = \"http://www.w3c.org/2003/05/soap-envelope\"\n"+
"impl-version = \"2.0.0.0\"\n"+
"impl-date = \"18.9.2007\"\n"+
"xd:name = \"XDefSOAPEnvelope\"\n"+
"xd:root = \"s:Envelope\" >\n"+
"\n"+
"<s:Envelope xd:script=\"occurs 1\"\n"+
"s:encodingStyle=\"fixed 'http://www.syntea.cz/skp/pis/encoding'\" >\n"+
"  <s:Body a = \"required string()\" s:b = \"required string()\"/>\n"+
"</s:Envelope>\n"+
"\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml =
"<e:Envelope e:encodingStyle=\"http://www.syntea.cz/skp/pis/encoding\"\n"+
"xmlns:e=\"http://www.w3c.org/2003/05/soap-envelope\">\n"+
"<e:Body a=\"1\" e:b=\"2\"/>\n"+
"</e:Envelope>";

			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "XDefSOAPEnvelope", xml, reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq("", strw.toString());
			//2) ChkDomParser
			builder = new KDOMBuilder();
			builder.setNamespaceAware(true);
			xd = xp.createXDDocument("XDefSOAPEnvelope");
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}
