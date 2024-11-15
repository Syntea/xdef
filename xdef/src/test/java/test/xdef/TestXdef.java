package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FileReportReader;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.msg.XDEF;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.TimeZone;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.xml.XMLConstants;
import org.w3c.dom.DOMException;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.proc.XXElement;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefParseResult;
import org.xdef.XDValueID;
import org.xdef.component.XComponent;
import org.xdef.proc.XXData;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import static test.XDTester._xdNS;

/** All sorts of tests of X-definition.
 * @author Vaclav Trojan
 */
public final class TestXdef extends XDTester {

	private static int _myX;

	@Override
	public final void test() {
		final String dataDir = getDataDir() + "test/";
		XDPool xp;
		String xdef;
		String xml;
		String s;
		ArrayReporter reporter = new ArrayReporter();
		XComponent xc;
		XDDocument xd;
		Element el;
		Report rep;
		XDOutput out;
		StringWriter swr;
		String tempDir;
		Properties props;
		try {
			tempDir = clearTempDir().getCanonicalPath().replace('\\', '/');
			if (!tempDir.endsWith("/")) {
				tempDir += "/";
			}
		} catch (IOException ex) {
			fail(ex);
			return;
		}
		_myX = 1;
		boolean chkSyntax = getChkSyntax();
		try {
			XDFactory.getXDBuilder(null).compileXD(); // no sources
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (ex.getMessage()==null || !ex.getMessage().contains("XDEF903")) {
				fail(ex);
			}
		}
		try {//no source
			XDFactory.compileXD(null, (Object[]) new File[0]);
			fail("Error not reported");
		} catch (RuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("XDEF903") > 0, ex);
		}
		try {//no source
			XDFactory.compileXD(null, "myxxx.xdef");
			fail("Error not reported");
		} catch (SRuntimeException ex) {
			assertTrue(ex.getMessage().indexOf("XDEF903") > 0, ex);
		}
		try {
			xp = compile(
"<xd:def xmlns:xd=\""+_xdNS+"\" xmlns:tns=\"http://www.w3schools.com\">\n" +
"  <tns:note>\n" +
"    <xd:sequence xd:script=\"occurs 1\">\n" +
"      <tns:to xd:script=\"occurs 1\">required string()</tns:to>\n" +
"    </xd:sequence>\n" +
"  </tns:note>\n" +
"</xd:def>");
			parse(xp,"", "<tns:note><tns:to/></tns:note>", reporter);
			fail("Error not recognized");
		} catch (Exception ex) {
			if (!reporter.printToString().contains("XML080")) {fail(ex);}
		}
		try { // compile InputStream, String and more
			xdef = "<x:def xmlns:x ='" + _xdNS + "' name='a' root='a'><a/></x:def>";
			Object[][] params = new Object[1][2];
			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			xp = XDFactory.compileXD(null, (Object[]) params);
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			params = new Object[2][2];
			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			params[1][0] = new ByteArrayInputStream(
				("<x:def xmlns:x ='"+_xdNS+"' name='b' root='b'><b/></x:def>").getBytes("UTF-8"));
			params[1][1] = "Osoba2 xdef";
			xp = XDFactory.compileXD(null, (Object[]) params);
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<b/>", parse(xp, "b", "<b/>", reporter));
			assertNoErrorwarnings(reporter);
			params = new Object[2][2];
			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			params[1][0] = new ByteArrayInputStream(
				("<x:def xmlns:x ='"+_xdNS+"' name='b' root='b'><b/></x:def>").getBytes("UTF-8"));
			params[1][1] = "Osoba2 xdef";
			xp = XDFactory.compileXD(null, (Object[]) params,
				"<x:def xmlns:x ='"+_xdNS+"' name='c' root='c'><c/></x:def>");
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<b/>", parse(xp, "b", "<b/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<c/>", parse(xp, "c", "<c/>", reporter));
			assertNoErrorwarnings(reporter);
			compile(// check semicolon tolerance
"<x:def xmlns:x ='" + _xdNS + "' root='a'>\n"+
"  <a a=\"required eq('a'); onTrue {outln('a');}; finally {outln('b');};\"/>\n"+
"</x:def>");
			compile(//check empty declaration
"<xd:def xmlns:xd = '" + _xdNS + "'>\n"+
"  <xd:declaration/>\n"+
"</xd:def>");
			compile(//check if comments are ignored
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"/*comment*/\n"+
"  <a a='/*comment*/required/*comment*/string/*comment*/'>\n"+
"/*comment*/\n"+
"  </a>\n"+
"/*comment*/\n"+
"</xd:def>");
			xd = compile(// check the sequence of processing of attributes
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A a='onTrue out(@a)' b='onTrue out(@b)' c='onTrue out(@c)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xd = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A a='finally out(@a)' b='finally out(@b)' c='finally out(@c)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xd = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A a='init out(@a)' b='init out(@b)' c='init out(@c)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xd = compile(// check the sequence of processed attribute
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A a='onStartElement out(@a)' b='onStartElement out(@b)' c='onStartElement out(@c)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
			xd = compile(// check the sequence of processing of attributes
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A b='onTrue out(@b)' c='onTrue out(@c)' a='onTrue out(@a)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xd = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A b='finally out(@b)' c='finally out(@c)' a='finally out(@a)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xd = compile(// check the sequence of processed attribute
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n" +
"  <A b='onStartElement out(@b)' c='onStartElement out(@c)' a='onStartElement out(@a)' />\n" +
"</xd:def>").createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A b='b' c='c' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xml = "<A c='c' b='b' a='a' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("bca", swr.toString());
			xd = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='A' xmlns:a='a.a' >\n" +
"  <A a:b='finally out(@a:a)' a:a='finally out(@a:b)' a:c='finally out(@a:c)'/>\n"+
"</xd:def>").createXDDocument();
			xml = "<A xmlns:x='a.a' x:c='c' x:a='a' x:b='b' />";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("abc", swr.toString());
		} catch (UnsupportedEncodingException | RuntimeException ex){fail(ex);}
		try {
			xdef = // check onIllegalRoot
"<x:def xmlns:x ='" + _xdNS + "' root ='a' x:script=\"onIllegalRoot {clearReports(); error('OK');}\">\n"+
"  <a a = \"required eq('a')\"/>\n"+
"</x:def>";
			parse(xdef, "", "<b a = 'b' />", reporter);
			if (reporter.errors()) {
				rep = reporter.getReport();
				if (!rep.getText().startsWith("OK")) {
					fail(rep.toString());
					while ((rep = reporter.getReport()) != null) {
						fail(rep.toString());
					}
				}
			} else {
				fail();
			}
		} catch (Exception ex) {
			if (!ex.getMessage().startsWith("E XDEF315")) {
				fail(ex);
			}
		}
		try {
			xp = compile(// test onXmlError, onIllegalRoot
"<xd:def xmlns:xd='" + _xdNS + "' root='A|B' xd:script=\n" +
"        'onXmlError {clearReports();out(1);} onIllegalRoot out(2);'>\n" +
"  <A b='int'><B c='int'/>string()</A>\n" +
"  <B b='int'/>\n" +
"</xd:def>");
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(swr);
			parse(xd, "<A b=''><B c='<'/>xxx<A/>", reporter); // XML error
			assertNoErrorsAndClear(reporter); // XML errors cleared
			assertEq("1", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(swr);
			parse(xd, "<C b='1'/>", reporter); // illegal root
			assertErrorsAndClear(reporter); // error illegal root reported
			assertEq("2", swr.toString());
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
		try {
			xp = compile(// recursive reference
"<xd:def  xmlns:xd='" + _xdNS + "' root='A' name='Y21'>\n"+
"  <A><B b='? string()'><B xd:script='*; ref A/B'/></B></A>\n"+
"</xd:def>");
			xml = "<A><B b='1'><B b='2'><B b='3'/></B><B b='4'/></B></A>";
			assertEq(xml, parse(xp, "", xml , reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def  xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A><B xd:script='0..1; ref Y'/></A>\n"+
"  <Y b='? string()' ><Y xd:script='*; ref Y'/></Y>\n"+
"</xd:def>");
			xml = "<A><B b='1'><Y b='2'><Y b='3'/></Y><Y b='4'/></B></A>";
			assertEq(xml, parse(xp, "", xml , reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method\n"+
"      boolean test.xdef.TestXdef.testExt(XXElement,String,String,String);\n"+
"  </xd:declaration>\n"+
"  <a xd:script = \"\n"+
"    finally {if (!testExt('a','b','c')) throw new Exception('Error!');}\"/>\n"+
"</xd:def>");
			parse(xp, "a", "<a/>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(_myX, 0);
			xp = compile(//no namespace prefix fro XDefinition
"<def xmlns='" + _xdNS + "' xmlns:n='a.b' root='n:a'>\n"+
"  <n:a xmlns:n='a.b'>\n"+
"    <mixed>\n"+
"      <n:x/>\n"+
"      <n:y/>\n"+
"    </mixed>\n"+
"  </n:a>\n"+
"</def>");
			xml = "<n:a xmlns:n='a.b'><n:x/><n:y/></n:a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a xmlns='a.b'><x/><y/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =  // test Contex constructor and conversion to element
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"+; finally {\n"+
"    Container c =\n"+
"      [%a:x=[%xmlns:a='a',[%a:y=[]],[%y=null],'t',[%z=[%a='a']]]];\n"+
"    returnElement(c.toElement().toContainer().toElement());}\"/>\n"+
"</xd:def>";
			assertEq(parse(xdef, null, "<a/>", reporter),
				"<a:x xmlns:a='a'><a:y xmlns:a='a'/><y/>t<z a='a'/></a:x>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <xd:declaration> type x string(2) AAND enum('ab','bc'); </xd:declaration>\n"+
"  <a a='x()'/>\n"+
"</xd:def>";
			xml = "<a a='ab'/>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='a'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xml = "<a a='12'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = //test of exception in external method.
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>" +
"  <a xd:script='finally test.xdef.TestXdef.myError()' />" +
"</xd:def>";
			parse(xdef, "", "<a/>", reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if(!reporter.errorWarnings()) {
				fail(ex);
			} else {
				assertEq("XDEF569", reporter.getReport().getMsgID());
			}
		}
		try {
			xdef = //test of exception in external method.
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>" +
"  <xd:declaration>\n"+
	 "external method long test.xdef.TestXdef.myError();\n"+
	 "int i = myError();\n"+
"  </xd:declaration>\n"+
"  <a/>" +
"</xd:def>";
			parse(xdef, "", "<a/>", reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if(!reporter.errorWarnings()) {
				fail(ex);
			} else {
				assertEq("XDEF569", reporter.getReport().getMsgID());
			}
		}
		try {//check empty attribute in model
			xdef = "<xd:def xmlns:xd='"+_xdNS+"' root='A'><A a=''/></xd:def>";
			xml = "<A a='a'></A>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xdef, "", "A", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef = //Test references to noname XDefinifion
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <A xd:script=\"create [%a='a','b'].toElement()\" a='string'> string </A>\n"+
"</xd:def>\n"+
"<xd:def name='X' root = \"#A\"><B xd:script=\"ref #A\"/></xd:def>\n"+
"</xd:collection>";
			xml = "<A a='a'>b</A>";
			assertEq(xml, parse(xdef, "X", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<B a='a'>b</B>", create(xdef, "X", "B", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = //check type expression
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"  <xd:declaration>\n"+
"    boolean x(){return (eq('abc') || eq('xyz')) AAND string(2, 50);}\n"+
"  </xd:declaration>\n"+
"  <A a=\"x()\"/>\n"+
"</xd:def>";
			xml = "<A a='abc'/>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A a='xyz'/>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A a='aaa'/>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertTrue(reporter.errorWarnings());
			// check onAbsence not invoked after attribute was deleted in
			// onFalse method
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"int; onFalse setText((String)null); onAbsence error('X1','x')\">\n"+
"    int(); onFalse setText((String)null); onAbsence error('X2','x')\n"+
"  </a>\n"+
"</xd:def>";
			assertEq("<a/>", parse(xdef, null, "<a a='c'>d</a>", reporter));
			s = reporter.printToString();
			assertTrue(s.contains("path=/a/text()")&& s.contains("XDEF527")
				&& !s.contains("X1") && !s.contains("X2"), s);
			assertEq("<a/>", parse(xdef, null, "<a/>", reporter));
			s = reporter.printToString();
			assertTrue(s.contains("XDEF526") && s.contains("path=/a/text()")
				&& !s.contains("XDEF527") // missing text should not be!!!
				&& s.contains("path=/a/@a") && s.contains("X1")
				&& s.contains("X2"), s);
			xml = "<a a='1'>1</a>";
			assertEq(xml, parse(xdef, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = //list, listi, tokens, tokensi
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"enum('abc', 'defg')\">\n"+
"    <x xd:script='occurs *'> enum('abc', 'defg') </x>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='abc'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String x='abc',y='defg'; </xd:declaration>\n"+
"  <a a=\"enum(x, y)\"> <x xd:script='occurs *'> enum(x, y) </x> </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='abc'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"enumi('aBc', 'DeF')\">\n"+
"    <x xd:script='occurs *'>enumi('aBc', 'DeFg')</x>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='ABC'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String x='aBc',y='DeFg'; </xd:declaration>\n"+
"  <a a=\"enumi(x, y)\"> <x xd:script='occurs *'> enumi(x, y) </x> </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='ABC'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"enum('abc','defg')\">\n"+
"    <x xd:script='occurs *'>enum('abc','defg')</x>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='abc'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"enumi('aBc','DeF')\">\n"+
"    <x xd:script='occurs *'>enumi('aBc','DeFg')</x>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='ABC'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef = // xd:text attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String s = ''; </xd:declaration>\n"+
"  <a xd:text='occurs 2 string(1); finally s += getText() + 3;'> <b/> </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a>a<b/>b</a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("a3b3", xd.getVariable("s").toString());
			xd.setVariable("s", "xxx");
			assertEq("xxx", xd.getVariable("s").toString());
			xdef = // xd:text attribute
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <a xd:text=\"occurs 2..3 int();\"><b/><c/><d/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b/><c/><d/></a>"; // error XDEF527
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF527"), reporter.toString());
			xml = "<a>1<b/><c/><d/></a>"; // error XDEF527
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF527"), reporter.toString());
			xml = "<a><b/>1<c/>2<d/></a>";  // OK
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/>1<c/>2<d/>3</a>";  // OK
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a>1<b/>2<c/>3<d/>4</a>"; // error XDEF533
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF533"), reporter.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'><a xd:text='* string()'/></xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'> <b xd:script='*'/> </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1<b/><b/>2</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1<b/>2/>3</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a>1<b/>2<b/>3<b/>4</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>int i = 0;</xd:declaration>\n" +
"  <a xd:text='* string(); create ++i'> <b xd:script='*'/> </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/></a>";
			assertEq("<a>1<b/>2</a>", create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b/><b/></a>";
			assertEq("<a>1<b/>2<b/>3</a>", create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <B xd:text=\"string()\"><c/><d/></B>\n" +
"  <a> <b xd:script=\"ref B\" xd:text=\"3 string()\"/> </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b>t1<c/>t2<d/>t3</b></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrorwarnings(reporter);
			xml = "<a><b><c/><d/>t</b></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xdef = // xd:textcontent attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String s = ''; </xd:declaration>\n"+
"  <a xd:textcontent='string(2); finally s += getText() + 3;'> <b/> </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a>a<b/>b</a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("ab3", xd.getVariable("s").toString());
			// test both text and textcontent
			for (int i = 0; i < 2; i++) {
				xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A " + (i == 0 ? "xd:text='*" : "xd:textcontent='?") + " string()'>\n" +
"    <B xd:script='*'/>\n" +
"  </A>\n" +
"</xd:def>";
				xp = compile(xdef);
				xml = "<A/>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A>test<B/></A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A><B/><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A>test<B/><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A><B/>test<B/></A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
				xml = "<A>test<B/>test<B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrorwarnings(reporter);
			}
			xdef = // test format, printf
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> Locale loc = new Locale('cs', 'CZ'); </xd:declaration>\n"+
"  <a xd:script=\"finally {String s = '%d,%2.1f';\n"+
"    printf(s, 1, 1.1); s = '; ' + s;\n"+
"    out(format(s,2,2.2)); printf(loc,s,1,1.1); out(format(loc,s,2,2.2));\n"+
"    printf($stdOut, s, 1, 1.1); $stdOut.out(format(s, 2, 2.2));\n"+
"    printf($stdOut,loc,s,1,1.1); $stdOut.out(format(loc,s,2,2.2));}\"/>\n"+
"</xd:def>";
			swr = new StringWriter();
			parse(xdef, "", "<a/>", reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "1,1.1; 2,2.2; 1,1,1; 2,2,2; 1,1.1; 2,2.2; 1,1,1; 2,2,2");
			xdef = // "$" identifiers, miscellaneous
"<xd:def xmlns:xd='" + _xdNS + "' impl-version = '2.0'\n"+
"        name='abc' script='options trimText' root='Davka'>\n"+
"  <xd:declaration>\n"+
"    external method void test.xdef.TestXdef.myProc(XDValue[] p);\n" +
"    final String myversion = getImplProperty('version');\n"+
"    final String reg = '[A-Z][A-Z]B';\n"+
"    final String min = '987654320';\n"+
"    final String max = '987654321';\n"+
"  </xd:declaration>\n"+
"  <xd:declaration>\n"+
"     int myParseInt(String s){ return parseInt(s); }\n"+
"     boolean myCheck0(String s) {\n"+
"       switch(s) {\n"+
"         case 'MD5': return true;\n"+
"         case 'CRC': return true;\n"+
"       }\n"+
"       return error('Chybný typ: ' + s);\n"+
"     }\n"+
"     boolean myCheckInt() {\n"+
"       try {\n"+
"         if (int()) return true; else throw new Exception('vyjimka');\n"+
"       } catch (Exception ex) {\n"+
"          if (getText() EQ 'a123') return true;\n"+
"         throw new Exception('vyjimka1');\n"+
"       }\n"+
"     }\n"+
"     boolean $myCheck(){return myCheck0(getText());}\n"+
"  </xd:declaration>\n"+
"  <Davka Verze      =\"fixed myversion\"\n"+
"         Kanal      =\"required string(%pattern=reg)\"\n"+
"         Seq        =\"required int(myParseInt(min), myParseInt(max))\"\n"+
"         SeqRef     =\"optional myCheckInt()\"\n"+
"         Date       =\"required xdatetime('d.M.yyyy')\"\n"+
"         dp0        =\"required decimal\"\n"+
"         dp1        =\"required decimal(%totalDigits=3)\"\n"+
"         dp2        =\"required decimal(%totalDigits=5,%fractionDigits=1)\"\n"+
"         xd:attr    =\"optional\"\n"+
"         xd:script  =\"finally myProc(1,0.5,'xxx')\">\n"+
"    <File   Name       = \"required string(1,256)\"\n"+
"            Format     = \"required enum('TXT','XML','CTL')\"\n"+
"            Kind       = \"required string(3,3)&amp;(eq('abc')|eq('xyz'))\"\n"+
"            RecNum     = \"required num(8)\"\n"+
"            xd:script= \"occurs 1..\">\n"+
"      <xd:mixed>\n"+
"        <CheckSum Type       = \"required $myCheck()\"\n"+
"                 Value       = \"required string()\"\n"+
"                 xd:script = \"occurs 1\">\n"+
"          optional string()\n"+
"        </CheckSum>\n"+
"        <x xd:script = \"occurs 1..5; ref empty.node\" />\n"+
"      </xd:mixed>\n"+
"    </File>\n"+
"    <xd:choice>\n"+
"      <Osoba xd:script = \"occurs 1..1; ref Osoba\" />\n"+
"      <OSVC xd:script = \"occurs 1..1; ref OSVC\" />\n"+
"      <Organizace xd:script = \"occurs 1..2; ref Organizace\" />\n"+
"    </xd:choice>\n"+
"    fixed 'ahoj'\n"+
"    <y xd:script=\"ref y\" />\n"+
"    <z xd:script=\"ref log\" />\n"+
"  </Davka>\n"+
"  <Osoba jmeno=\"required string()\"/>\n"+
"  <OSVC nazev=\"required string()\"/>\n"+
"  <Organizace adresa=\"required string()\"/>\n"+
"  <empty.node/>\n"+
"  <qwert xd:script=\"ref y\" />\n"+
"  <y xd:script=\"ref z\" />\n"+
"  <z><fff attr=\"optional\"/></z>\n"+
"  <zz attr=\"required\"/>\n"+
"  <log bttr=\"required\"> </log>\n"+
"</xd:def>";
			xml =
"<Davka Kanal=\"XYB\"\n"+
"       Date=\"1.1.2003\"\n"+
"       Seq=\"987654321\"\n"+
"       SeqRef= \"a123\"\n"+
"       any=\"145\"\n"+
"       dp0=\"123.456\"\n"+
"       dp1=\"145\"\n"+
"       dp2=\"-1234.1\">\n"+
"   <File Name=\"abc.dat\"\n"+
"         Format=\"TXT\"\n"+
"         Kind=\"xyz\"\n"+
"         RecNum=\"12345678\">\n"+
"       <CheckSum Type=\"MD5\" Value=\"0xfadb8701a\"> text </CheckSum>\n"+
"       <x/>\n"+
"   </File>\n"+
"   <Organizace adresa=\"ulice1\" />\n"+
"   <Organizace adresa=\"ulice2\" />\n"+
"ahoj\n"+
"   <y><fff attr=\"attr\"/></y> \n"+
"   <z bttr=\"bttr\"/>\n"+
"</Davka>";
			el = parse(compile(xdef), "abc", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("2.0", el.getAttribute("Verze"));
			assertEq(_myX, 3);
			xdef = //test direct expression as type check
"<xd:def xmlns:xd='" + _xdNS + "' root='a|b|c|d'>\n"+
"  <xd:declaration>\n"+
"    boolean x() { return !eq('hi'); }\n"+
"    boolean y() { return true; }\n"+
"  </xd:declaration>\n"+
"  <a p=\"'hi';\"/> <b p=\"x()\"/> <c p=\"y()\"/> <d p=\"x()\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a p='hi'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<b p='xx'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<b p='hi'/>", reporter);
			assertTrue(reporter.errorWarnings());
			parse(xp, "", "<c p='xxx'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<d p='xx'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<d p='hi'/>", reporter);
			assertTrue(reporter.errorWarnings());
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"  <x:declaration>\n"+
"    external method void test.xdef.TestXdef.myCheck(XXElement, String, byte[]);\n"+
"  </x:declaration>\n"+
"  <a Ver     =\"fixed '1.2'\"\n"+
"     Class   =''\n"+
"     Recurr  ='?'\n"+
"     x:script=\"finally {myCheck('abc', parseBase64('abc'.getBytes().toBase64()));}\" >\n"+
"    <x:any x:attr='?' x:script='*; options moreElements, moreText'/>\n"+
"  </a>\n"+
"</x:def>";
			xp = compile(xdef);
			xml =
"<a Ver='1.2' Class='Cls' Recurr='recurr'>"+
"<any xx='nn'>"+
"<any1 xx1='nn'>"+
"<inner_any/>"+
"any1 text..."+
"</any1>"+
"<any2/>"+
"any text..."+
"</any>"+
"</a>";
			assertEq(xml, parse(xp,"", xml, reporter, this));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDParseResult test.xdef.TestXdef.dateToLocal(XXData, XDValue[]);\n"+
"  </xd:declaration>\n"+
"  <a date=\"required dateToLocal('yyyyMMddTHHmmssZ','yyyyMMddTHHmmssZ')\"/>"+
"</xd:def>";
			xml = "<a date='20080225T000000Z'/>";
			xp = compile(xdef);
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			s = el.getAttribute("date");
			if (!"20080225T000000Z".equals(s)) {
				fail(s);
			}
			xml = "<a date='20082502T000000Z'/>";
			parse(xp, "", xml, reporter);
			if ((rep = reporter.getReport()) == null) {
				fail("error not reported");
			} else {
				if (!"E02".equals(rep.getMsgID())) {
					fail(rep.toString());
				}
				while ((rep = reporter.getReport()) != null) {
					fail(rep.toString());
				}
			}
			xdef = //check typ of fixed value
"<xd:def xmlns:xd='" + _xdNS + "' root='a'> <a v=\"float; fixed '2.0'\"/> </xd:def>";
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a v='2.0'/>", reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a v='20'/>", reporter));
			assertErrors(reporter);
		} catch (RuntimeException ex) {fail(ex);}
		try {//ignoreEmptyAttributes
			xdef = //errors
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"required enum('A','B','C'); onFalse {out('error');} onAbsence {out('missing');}\n"+
"       options ignoreEmptyAttributes,trimAttr\"/>\n"+
"</xd:def>";
			swr = new StringWriter();
			xml = "<a a = ' '/>";
			parse(xdef, "", xml, reporter, swr, null, null);
			assertTrue(reporter.errorWarnings() &&
				"XDEF526".equals(reporter.getReport().getMsgID()));
			assertEq("missing", swr.toString());
			xdef = //errors cleared
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"required enum('A','B','C'); onFalse {out('error');clearReports();}\n"+
"       onAbsence {out('missing'); clearReports();} options ignoreEmptyAttributes,trimAttr\"/>\n"+
"</xd:def>";
			swr = new StringWriter();
			parse(xdef, "", "<a a=' '/>", reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("missing", swr.toString());
			xdef = //setAttr
"<xd:def xmlns:xd='" + _xdNS + "' root='test'> <test a = \"int; onTrue setAttr('a', '2');\"/> </xd:def>";
			xml = "<test a = '1'/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			s = el.getAttribute("a");
			assertTrue("2".equals(s), "a = " + s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test'>\n"+
"  <test a = \"int;\" xd:script=\"finally setAttr('a', '2');\"/>\n"+
"</xd:def>";
			el = parse(xdef, "", xml, null);
			s = el.getAttribute("a");
			assertTrue("2".equals(s), "a = " + s);
			xdef = // check in the onIllegalRoot
"<x:def xmlns:x ='" + _xdNS + "' root='a' x:script=\"onIllegalRoot throw new Exception('OK')\">\n"+
"  <a a = \"required eq('a')\"/>\n"+
"</x:def>";
			xml = "<b a = 'b' />";
			parse(xdef, "", xml, reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (!ex.getMessage().contains("OK") ||
				!ex.getMessage().contains("XDEF905")) {
				fail(ex);
			}
		}
		try {
			xdef = // add child nodes to refered element
"<xd:def root='a' xmlns:xd='" + _xdNS + "' script='options ignoreEmptyAttributes'>\n"+
"  <a xd:script = 'ref b'>\n"+
"    <p/>\n"+
"    <q/>\n"+
"    required int()\n"+
"  </a>\n"+
"  <b attr=\"required an()\"> <c/> </b>\n"+
"</xd:def>";
			xml = "<a attr='a1'><c/><p/><q/>123</a>";
			parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef = //int type equals
"<x:def xmlns:x='" + _xdNS + "' root='a'> <a><int>int(10_000)</int></a> </x:def>";
			xml = "<a><int>10000</int></a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			//test xd:text as attribute
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"  <root xd:text=\"required string()\">\n"+
"    <xd:choice>\n"+
"      <a xd:script=\"occurs 0..2\" />\n"+
"      <b xd:script=\"occurs 0..2\" />\n"+
"    </xd:choice>\n"+
"  </root>\n"+
"</xd:def>");
			xml = "<root><a/></root>";
			el = parse(xp, "", xml, reporter);
			if (reporter.getErrorCount() != 1
				|| !"XDEF527".equals(reporter.getReport().getMsgID())) {
				fail(reporter.printToString());
			}
			assertEq(xml,el);
			xp = compile(
"<xd:def root ='root' xmlns:xd='" + _xdNS + "'>\n"+
"<root xd:text=\"* string()\">\n"+
"  <xd:choice>\n"+
"    <a xd:script=\"occurs 0..2\" />\n"+
"    <b xd:script=\"occurs 0..2\" />\n"+
"  </xd:choice>\n"+
"</root>\n"+
"</xd:def>");
			el = parse(xp,"", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml,el);
			xml = "<root>text1<a/>text2</root>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        xd:script= 'options ignoreEmptyAttributes'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <p xd:script = \"occurs 1..\" />\n"+
"      <xd:mixed>\n"+
"        <q xd:script = \"occurs 1\" />\n"+
"        <r xd:script = \"occurs 1\" />\n"+
"      </xd:mixed>\n"+
"    </xd:mixed>\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a><r/><q/><p/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a><p/><q/><r/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a><p/><r/><q/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a><p/><q/><r/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef =  // Test fixed from a field
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <xd:declaration scope='global'>\n"+
"    String verze = '1.23';\n"+
"    String x = '???';\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name='a' root='root'>\n"+
"  <root Verze=\"float; fixed verze\"\n"+
"        arg =\"required string(); onTrue x=getText().substring(3)\">\n"+
"    <a aa=\"required eq(x);\" />\n"+
"  </root>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml = "<root arg=\"ou=xyz\"><a aa=\"xyz\"/></root>";
			el = parse(xdef, "a", xml, reporter);
			if (!el.hasAttribute("Verze")) {
				fail("Attribute 'Verze' is missing");
			} else {
				assertEq("1.23", el.getAttribute("Verze"));
				el.removeAttribute("Verze");
			}
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xp = compile(//Authomatic conversion of ParseResult to boolean
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"  <xd:declaration> boolean myCheck() {boolean b = enum('A','B','C'); return b;} </xd:declaration>\n"+
"  <A a=\"?myCheck\" b='myCheck'/>\n"+
"</xd:def>");
			xml = "<A b='C'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<A b=' '/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<A a= 'B' b='C'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<A a = ' ' b='C'/>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xp = compile( // test match section
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"  </a>\n" +
"</xd:def>");
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:sequence>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:sequence>\n"+
"  </a>\n" +
"</xd:def>");
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:choice>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:choice>\n"+
"  </a>\n" +
"</xd:def>");
			xml = "<a><b x='1'/><b x='1'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // test match
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:mixed>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:mixed>\n"+
"  </a>\n" +
"</xd:def>");
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:mixed>\n"+
"      match getOccurrence() == 0; ? string(); \n" +
"      <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"      match getOccurrence() == 0; string(); \n" +
"    </xd:mixed>\n"+
"  </a>\n" +
"</xd:def>");
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external int typ;\n"+ // 1, 2
"  </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice>\n"+
"      <b xd:script=\"match(typ == 1); create typ == 1 ? from('b') : null;\"\n"+
"         b=\"string\"\n"+
"        Text=\"create 'text'\" />\n"+
"      <b xd:script=\"match(typ == 2); create typ==2 ? from('b') : null;\"\n"+
"         c=\"create 'z'\"\n"+
"         Kod=\"create 'kod'\" />\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			xd.setVariable("typ", 1);
			xml = "<a><b Text='x' b='b'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			el = create(xd, "a", reporter, xml);
			assertNoErrorwarnings(reporter);
			assertEq("<a><b Text='text' b='b'/></a>", el);
			xml = "<a><b Kod='x' c='c'/></a>";
			el = parse(xd, xml, reporter);
			assertErrors(reporter);
			assertEq("<a><b/></a>", el);
			xd = xp.createXDDocument();
			xd.setVariable("typ", 2);
			xml = "<a><b Kod='x' c='c'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			el = create(xd, "a", reporter, xml);
			assertNoErrorwarnings(reporter);
			assertEq("<a><b Kod='kod' c='z'/></a>", el);
			xml = "<a><b Text='x' b='b'/></a>";
			el = parse(xd, xml, reporter);
			assertErrors(reporter);
			assertEq("<a><b/></a>", el);
			String tempFile = tempDir + "vystup.txt";
			xp = compile(
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='xxx'>\n"+
"  <xd:declaration scope='global'>\n"+
"    String child = 'CHILD';\n"+
"    String verze = '2.0';\n"+
"    Output vystup = new Output('" + tempFile + "');\n"+
"    Input vstup = new Input('" + tempFile + "');\n"+
"    Output err = new Output('#System.err');\n"+
"    int count = 0;\n"+
"  </xd:declaration>\n"+
"  <xd:declaration scope='global'>\n"+
"    void testparams(String i, int j, Datetime k) {\n"+
"      outln('testparams ' + i + ',' + j + ',' + k);\n"+
"    }\n"+
"    String myStringa() { return 'myString'; }\n"+
"    String myString(String p) {return myStringa() + ' ' + p;}\n"+
"    String myString() { return myString('xxx'); }\n"+
"    boolean myCheckInt() {\n"+
"      try {\n"+
"        if (int()) return true; else throw new Exception('vyjimka');\n"+
"      } catch (Exception ex) { throw new Exception('vyjimka1'); }\n"+
"    }\n"+
"    boolean myCheckInt1(){\n"+
"      try {return myCheckInt();}\n"+
"      catch(Exception ex) {return error('vyjimka2');}\n"+
"    }\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name='abc' root='root | *'>\n"+
"   <xd:declaration scope='global'>\n"+
"     void myOut() {vystup.outln(child);}\n"+
"     boolean myCheck(){return enum('A','B','C');}\n"+
"   </xd:declaration>\n"+
"  <root Verze=\"fixed verze\"\n"+
"        PlatnostOd=\"optional xdatetime('d.M.yyyy H:mm');\n"+
"                    onAbsence setText('11'); finally {outln(myString()); outln((String)@Kanal);}\"\n"+
"        Kanal=\"required myCheckInt1();\"\n"+
"        Seq=\"required myCheck()\"\n"+
"        SeqRef=\"optional 'xyz'\"\n"+
"        Date=\"required xdatetime('d.M.y')\"\n"+
"        xd:attr=\"occurs 1.. int(); onTrue outln('&lt;' + getElementName() + ' '\n"+
"                    + getAttrName() + '=\\'' + getText()+ '\\'/&gt;' );\n"+
"                    finally testparams('a',1, parseDate('1999-05-01T20:43:09+01:00'));\"\n"+
"        xd:script= \"match (@Kanal == '123') AND @SeqRef\">\n"+
"    <child xd:script=\"occurs 1..2; ref abc#child\"/>\n"+
"    <xd:list ref = \"sq\" />\n"+
"    <xd:choice occurs=\"?\">\n"+
"      <a xd:script=\"occurs 0..2\"/>\n"+
"      <b xd:script=\"occurs 0..2\"/>\n"+
"    </xd:choice>\n"+
"    <end xd:script=\"occurs 0..\"/>\n"+
"  </root>\n"+
"  <child xd:script=\"finally {myOut();}\">\n"+
"    optional string(1,100);\n"+
"      onTrue setText('text');\n"+
"      onFalse setText('empty');\n"+
"      onAbsence setText('absence')\n"+
"  </child>\n"+
"  <xd:list name='sq'>\n"+
"    <x xd:script=\"occurs 0..2; \"/>\n"+
"    <y xd:script=\"occurs 1\"/>\n"+
"    required\n"+
"  </xd:list>\n"+
"</xd:def>\n"+
"</xd:collection>");
			xml =
" <root Verze=\"2.0\"\n"+
"       PlatnostOd=\"1.1.2000 20:00\"\n"+
"       Kanal=\"123\"\n"+
"       Seq=\"C\"\n"+
"       SeqRef=\"xyz\"\n"+
"       any=\"12000\"\n"+
"       Date=\"1.1.2000\">\n"+
"   <child>\n"+
"     toto je text...\n"+
"   </child>\n"+
"   <child/>\n"+
"   <x/>\n"+
"   <x/>\n"+
"   <y/>\n"+
"   text v include\n"+
"   <a/>\n"+
"   <end/>\n"+
" </root>";
			swr = new StringWriter();
			el = parse(xp, "abc", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("<root any='12000'/>\n"+
				"myString xxx\n123\ntestparams a,1,1999-05-01T20:43:09+01:00\n",
				swr.toString());
			assertEq("2.0", el.getAttribute("Verze"));
			StringBuilder sb;
			try (FileReader fr = new FileReader(tempFile)) {
				FileReportReader frep = new FileReportReader(fr, true);
				Report r;
				sb = new StringBuilder();
				while ((r = frep.getReport()) != null) {
					s = r.toString();
					if (" ".equals(s)) {
						sb.append('\n');
					} else {
						sb.append(s);
					}
				}	frep.close();
			}
			new File(tempFile).delete();
			assertEq("CHILD\nCHILD\n", sb.toString());
			xp = compile(// test DOCTYPE and entity
"<!DOCTYPE xd:def [\n"+
"  <!ENTITY jmeno \"required string(5,30)\">\n"+
"  <!ENTITY plat \"optional int(1000,99999)\">\n"+
"  <!ENTITY v \"fixed '20'\">\n"+
"]>\n"+
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='osoba'>\n"+
"  <xd:declaration>\n"+
"    String data = 'Pan reditel';\n"+
"    String x = 'nobody';\n"+
"  </xd:declaration>\n"+
"  <osoba funkce = \"required\" jmeno = \"&jmeno;\" plat = \"&plat;\" v = \"&v;\">\n"+
"    required eq(data); onFalse {clearReports(); setText(x);} onAbsence setText(x);\n"+
"    <podrizeny jmeno = \"required\" xd:script=\"occurs 0..2\"/>\n"+
"  </osoba>\n"+
"  <AdresaCE KodOkresu  = \"optional num(4)\"\n"+
"       PSC             = \"required string(5); onAbsence setText('0')\"\n"+
"       Obec            = \"optional string(1,30)\"\n"+
"       Ulice           = \"optional string(1,30)\"\n"+
"       CisloOrientacni = \"required int(0,32767); onAbsence setText('0')\"\n"+
"       ZnakDomu        = \"optional string(1)\"\n"+
"       CisloPopisne    = \"required int(0,32767); onAbsence setText('0')\"\n"+
"       DruhCislaDomu   = \"required string(1); onAbsence setText('P')\" />\n"+
"</xd:def>");
			xml =
"<osoba funkce = \"boss\" jmeno = \"Voprsalek\" plat = \"10000\">\n"+
" toto je text\n"+
"  <podrizeny jmeno = \"Novak\" />\n"+
"  <podrizeny jmeno = \"Houzvicka\" />\n"+
"  <podrizeny jmeno = \"Horak\" />\n"+
"</osoba>";
			xd = xp.createXDDocument("a");
			xd.xparse(xml, reporter);
			if (!reporter.errorWarnings()) {
				fail("unreported error");
			} else if (reporter.getErrorCount() != 1) {
				rep = reporter.getReport();
				fail("incorrect error: " + rep);
			} else {
				rep = reporter.getReport();
				assertEq("XDEF558", rep.getMsgID(), rep.toString()); //Maximum occurrence limit exceeded
			}
			if (!("boss".equals(xd.getElement().getAttribute("funkce")))) {
				fail("incorrect boss=\"" + xd.getElement().getAttribute("boss") + '"');
			} else if (!("20".equals(xd.getElement().getAttribute("v")))) {
				fail("incorrect v=\"" + xd.getElement().getAttribute("v") + '"');
			} else if (!("nobody".equals(
				xd.getElement().getChildNodes().item(0).getNodeValue()))) {
				fail("incorrect data: '" + xd.getElement().getChildNodes().item(0) + "'");
			}
			xp = compile( // test messages
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='messages'>\n"+
"  <messages>\n"+
"    <xd:any xd:script=\"occurs 0..\"\n"+
"      ces='optional; onTrue $stdErr.outln(getElementName() + getText())'/>\n"+
"  </messages>\n"+
"</xd:def>");
			xml =
"<messages>\n"+
"  <XDEF500 ces=\"&amp;{line}{; řádka=}&amp;{column}{;"
		+ " sloupec=}&amp;{sysId}{; zdroj='}{'}\"/>\n"+
"  <XDEF501 ces=\"Výskyt nepovoleného elementu '&amp;{child}'&amp;{xpath}{"
		+ " v }&amp;{#XDEF500}\"/>\n"+
"  <XDEF502 ces=\"Element '&amp;{child}' není definován jako 'root'{ v }"
		+ "&amp;{#XDEF500}\"/>\n"+
"</messages>";
			parse(xp, "a", xml, reporter);
			s = reporter.printToString().trim();
			assertTrue(s.indexOf("XDEF500&{line}{; řádka=}&{column}{; sloupec=}&{sysId}{; zdroj='}{'}") == 0
				&& s.indexOf("XDEF501Výskyt nepovoleného elementu '&{child}'&{xpath}{ v }&{#XDEF500}") > 0
				&& s.endsWith("XDEF502Element '&{child}' není definován jako 'root'{ v }&{#XDEF500}"), s);
			xp = compile(//test macro
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='messages' root='messages' script='options ${mac#opt}'>\n"+
"  <messages>\n"+
"    <xd:any xd:script = \"occurs 0..\"\n"+
"        ces = \"optional; onTrue outln(getElementName()+' '+getText())\"/>\n"+
"  </messages>\n"+
"</xd:def>\n"+
"<xd:def name=\"mac\">\n"+
"  <xd:macro name = \"opt\">setAttrUpperCase${mac#opt1}</xd:macro>\n"+
"  <xd:macro name = \"opt1\">,setTextUpperCase</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>");
			xml =
"<messages>\n"+
"  <X00 ces=\"&amp;{line}{; x=}&amp;{column}{; y=}&amp;{sysId}{; z='}{'}\"/>\n"+
"  <X01 ces=\"Element error '&amp;{child}'&amp;{xpath}{ in }&amp;{#X00}\"/>\n"+
"  <X02 ces=\"Element '&amp;{child}' is not 'root'{ in }&amp;{#X00}\"/>\n"+
"</messages>";
			swr = new StringWriter();
			parse(xp, "messages", xml, null, swr, null, null);
			assertEq(
"X00 &{LINE}{; X=}&{COLUMN}{; Y=}&{SYSID}{; Z='}{'}\n"+
"X01 ELEMENT ERROR '&{CHILD}'&{XPATH}{ IN }&{#X00}\n"+
"X02 ELEMENT '&{CHILD}' IS NOT 'ROOT'{ IN }&{#X00}\n",
				swr.toString());
			xp = compile(//test macro
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='macTest' root='macTest'>\n"+
"  <macTest xd:script = \"occurs 0..${mac#m3(op = '${mac#m4}',"+
"           p1 = '\\'Ahoj\\'', p2 = 'Pane', p3 = 'Tepic', end= '')}\"/>\n"+
"</xd:def>\n"+
"<xd:def name = \"mac\">\n"+
"  <xd:macro name = \"m1\">${mac#m2()}</xd:macro>\n"+
"  <xd:macro name = \"m2\">out</xd:macro>\n"+
"  <xd:macro name = \"m3\" op=\"${mac#m1}\" p1=\"'Hi'\" p2=\"Sir\"\n"+
"                 p3=\"Bye\" end=\"outln('End');\">\n"+
"; finally {#{op}(#{p1});#{op}('#{p2}');#{op}('#{p3}');#{end}}\n"+
"  </xd:macro>\n"+
"<xd:macro name = \"m4\">out</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>");
			swr = new StringWriter();
			parse(xp, "macTest", "<macTest></macTest>\n",null,swr,null,null);
			assertEq("AhojPaneTepic", swr.toString());
			xp = compile(
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='a' root='macTest'>\n"+
"  <macTest xd:script = \"occurs 0..${mac#m3}\"/>\n"+
"</xd:def>\n"+
"<xd:def name = \"mac\">\n"+
"  <xd:macro name = \"m1\">${mac#m2()}</xd:macro>\n"+
"  <xd:macro name = \"m2\">out</xd:macro>\n"+
"  <xd:macro name = \"m3\" op=\"${mac#m1}\" p1=\"'Hi'\" p2=\"Sir\" p3=\"Bye\" end=\"outln('End');\">\n"+
"; finally {#{op}(#{p1});#{op}('#{p2}');#{op}('#{p3}');#{end}}\n"+
"  </xd:macro>\n"+
"  <xd:macro name = \"m4\">out</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("HiSirByeEnd\n", swr.toString());
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='a'>\n"+
"  <xd:macro name='m' p1='abc' p2='xyz'> finally {out('#{p1}#{p2}');} </xd:macro>\n"+
"  <a xd:script='${m()}' />\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "test", "<a/>", null, swr, null, null);
			assertEq("abcxyz", swr.toString());
			xp = compile(
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name = 'mac'>\n"+
"  <xd:macro name='m' p1='abc' p2='xyz'> finally {out('#{p1}#{p2}');} </xd:macro>\n"+
"</xd:def>\n"+
"<xd:def name='test' root='a'>\n"+
"  <a xd:script=\"${mac#m(p2='DEF', p1 = 'ABC')}\" />\n"+
"</xd:def>\n"+
"</xd:collection>");
			swr = new StringWriter();
			parse(xp, "test", "<a/>", null, swr, null, null);
			assertEq("ABCDEF", swr.toString());
			xp = compile(
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script='finally ${text}; options trimText;'/>\n"+
"  <xd:macro name='text'\n"+
"  >out('Volání makra text má tvar: \\u0024{text}')</xd:macro>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("Volání makra text má tvar: ${text}", swr.toString());
			xp = compile(
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"  <macTest xd:script='finally ${m1}${m2}; options trimText;'/>\n"+
"  <xd:macro name='m1'> </xd:macro>\n"+//one space
"  <xd:macro name='m2'>out('m2')</xd:macro>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("m2", swr.toString());
			xp = compile(
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"  <macTest xd:script='finally $$${m1}}; \noptions trimText;'/>\n"+
"  <xd:macro name='m1'>{m2}</xd:macro>\n"+
"  <xd:macro name='m2'>{m3</xd:macro>\n"+
"  <xd:macro name='m3'>out\n(\n'm2')</xd:macro>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("m2", swr.toString());
			// macro $${m1} -> ${m2} -> out('m2')
			xp = compile(
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"  <macTest xd:script='finally $${m1}; options trimText;'/>\n"+
"  <xd:macro name='m1'>{m2}</xd:macro>\n"+
"  <xd:macro name='m2'>out('m2')</xd:macro>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("m2", swr.toString());
			xp = compile( // macro empty replacement
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"  <macTest xd:script = \"finally out('${m1}'); options trimText;\"/>\n"+
"  <xd:macro name = \"m1\">${m2}m1</xd:macro>\n"+ //m2 is empty
"  <xd:macro name = \"m2\"></xd:macro>\n"+ //empty macro
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<macTest/>", null, swr, null, null);
			assertEq("m1", swr.toString());
			xp = compile(// macro in the declaration part
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n" +
"    <xd:macro name='a'>'aaa'</xd:macro>\n"+
"    String s = ${a};\n"+
"  </xd:declaration>\n" +
"  <a xd:script=\"finally out(${a}+s)\"/>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "", "<a/>", null, swr, null, null);
			assertEq("aaaaaa", swr.toString());
			xp = compile(new String[] {
"<xd:declaration xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:macro name='a'>'aaa'</xd:macro>\n"+
"  String s = ${a};\n"+
"</xd:declaration>",
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"finally out(s + ${a})\"/>\n"+
"</xd:def>"});
			swr = new StringWriter();
			parse(xp, "", "<a/>", null, swr, null, null);
			assertEq("aaaaaa", swr.toString());
			xp = compile(
"<xd:def name='a' root='a' xmlns:xd='" + _xdNS + "'>\n"+
"  <a xd:script='options trimText;'>\n"+
"    required string(); onTrue out(getText())\n"+
"  </a>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<a>data</a>\n", null, swr, null, null);
			assertEq("data", swr.toString());
			xp = compile(
"<x:def xmlns:x = '" + _xdNS + "' name='a' root='a'>\n"+
"  <a x:script = \"options trimText;\">\n"+
"    <a x:script = \"occurs 0..2\" />\n"+
"    <b x:script = \"occurs 0..2\" />\n"+
"    required string(); onTrue out(getText())\n"+
"  </a>\n"+
"</x:def>");
			swr = new StringWriter();
			parse(xp, "a", "<a>data</a>", null, swr, null, null);
			assertEq("data", swr.toString());
			swr = new StringWriter();
			parse(xp, "a", "<a><a/><b/>data</a>", null, swr, null, null);
			assertEq("data", swr.toString());
			swr = new StringWriter();
			try {
				parse(xp, "a", "<a><a/><b/>data<c/></a>", null,
					swr, null, null);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				if (!ex.getMessage().contains("XDEF501")) {
					fail("Incorrect exception");
					fail(ex);
				}
				swr.close();
			}
			assertEq("data", swr.toString());
			xp = compile(
"<x:def xmlns:x = '" + _xdNS + "' name='a' root='a'>\n"+
"  <a x:script = \"options trimText;\"\n"+
"     date=\"required xdatetime('d.M.yyyy');onTrue setText(toString(getParsedDatetime(),'yyyyMMdd'));\"/>\n"+
"</x:def>");
			el = parse(xp, "a", "<a date=\"17.7.2003\"/>");
			s = el.getAttribute("date");
			assertEq("20030717", s);
			xp = compile(
"<X:def xmlns:X = '" + _xdNS + "' X:name='a' X:root='a'>\n"+
"  <a X:script = \"options trimText;\" date = \"required xdatetime('d.M.yyyy','yyyyMMdd');\"/>\n"+
"</X:def>");
			el = parse(xp, "a", "<a date=\"17.7.2003\"/>");
			s = el.getAttribute("date");
			assertEq("20030717", s);
			xp = compile(// volani '@atr'
"<xd:def xmlns:xd = '" + _xdNS + "' name='a' root='a'>\n"+
"	<a s = \"optional int(1,99999999)\"\n"+
"	   m = \"optional string(); onTrue {if(!@s) setText('Error');} onAbsence {if(@s) setText('CZK');}\"\n"+
"    xd:script= \"finally out(getAttr('m'))\"/>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<a s='33'/>", null, swr, null, null);
			assertEq("CZK", swr.toString());
			swr = new StringWriter();
			parse(xp, "a", "<a m='xxx'/>", null, swr, null, null);
			assertEq("Error", swr.toString());
			swr = new StringWriter();
			parse(xp, "a", "<a/>", null, swr, null, null);
			assertEq("", swr.toString());
			xp = compile(// root *
"<xd:def name='a' root='a | b | *' xmlns:xd='" + _xdNS + "'>\n"+
"	<a s = \"optional int(1,99999999)\"\n"+
"	   m = \"optional string(); onTrue {if(!@s) setText('Error');} onAbsence {if(@s) setText('CZK');}\"\n"+
"    xd:script= \"finally out(getAttr('m'))\"/>\n"+
"	<b/>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "a", "<a s='33'/>", null, swr, null, null);
			assertEq("CZK", swr.toString());
			swr = new StringWriter();
			el = parse(xp, "a", "<c x='a'/>", null, swr, null, null);
			assertEq("", swr.toString());
			if (!"c".equals(el.getNodeName())) {
				fail("Incorrect root name:\n'" + el.getNodeName() + "'");
			}
			if (!"a".equals(el.getAttribute("x"))) {
				fail("Incorrect attribute:\n'" + el.getAttribute("x") + "'");
			}
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a xd:script = \"ref b\" />\n"+
"  <b> <c xd:script = \"ref d; options clearAdoptedForgets;\" /> </b>\n"+
"  <d> <e xd:script = \"forget\" /> </d>\n"+
"</xd:def>");
			xml = "<a><c><e/></c></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a xd:script = \"ref b\" />\n"+
"  <b><c xd:script = \"forget; ref d; options clearAdoptedForgets;\" /></b>\n"+
"  <d><e xd:script = \"forget\" /></d>\n"+
"</xd:def>");
			xml = "<a><c><e/></c></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a/>", el);
			xp = compile(
"<x:def xmlns:x='" + _xdNS + "' x:name =\"a\" x:root =\"a\"> <a>fixed 'abcd'</a> </x:def>");
			xml = "<a>ab<![CDATA[cd]]></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a><![CDATA[ab]]>cd</a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xp = compile(//datetime
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a date = \"required xdatetime('d.M.y', 'yyyyMMdd')\" />\n"+
"</xd:def>");
			xml = "<a date = \"20.5.2004\"/>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520'/>", el);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a date = \"required xdatetime('{H1m1s1}d.M.y|{H23m59s59}d/M/y', 'yyyyMMddHHmmss')\" />\n"+
"</xd:def>");
			xml = "<a date = \"20.5.2004\"/>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520010101'/>", el);
			xml = "<a date = \"20/5/2004\"/>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520235959'/>", el);
			xp = compile(// hex
"<x:def xmlns:x='" + _xdNS + "' x:name='a' x:root='a'>\n"+
"  <a a='required hexBinary(2)' b='required hexBinary(2)'>\n"+
"     required hexBinary(3)\n"+
"  </a>\n"+
"</x:def>");
			xml = "<a a='2345' b='AbcF'>112233</a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a a='p234' b='AbcF0'>2233</a>";
			parse(xp, "a", xml, reporter);
			assertErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='hex; onTrue {Bytes b=hex.parse().getValue(); out(b.toHex);}'/>\n"+
"</xd:def>");
			parse(xp, "", "<a a=' '/>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a a='0x1a'/>", reporter);
			assertErrors(reporter);
			swr = new StringWriter();
			assertEq("<a a='a'/>", parse(xp, null, "<a a='  a  '/>", reporter, swr, null, null));
			assertEq(swr.toString(), "0A");
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			assertEq("<a a='a b'/>", parse(xp, null, "<a a=' a b '/>", reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "AB");
			xml = "<a a='    a b c d   e    '/>";
			swr = new StringWriter();
			assertEq("<a a='a b c d   e'/>", parse(xp, null, xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "0ABCDE");
			swr = new StringWriter();
			assertEq("<a a='bcde'/>" , parse(xp, null, "<a a=' bcde '/>", reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "BCDE");
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='hex; onTrue {Bytes b=getParsedValue(); out(b.toHex);}'/>\n"+
"</xd:def>");
			xml = "<a a='    a b c d   e    '/>";
			swr = new StringWriter();
			assertEq("<a a='a b c d   e'/>", parse(xp, null, xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "0ABCDE");
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a Davka = \"required\" ZeDne = \"required\" > \n"+
"    <a xd:script = \"finally {Container c = xpath('../@Davka');\n"+
"           out('len=' + c.getLength() + ', typ=' + c.getItemType(0) + ', value=' + c.getText(0));}\" />\n"+
"    required\n"+
"  </a>\n"+
"</xd:def>");
			xml ="<a Davka='davka' ZeDne='1.1.99'><a/>text</a>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			s = swr.toString();
			assertTrue(("len=1, typ=" + XDValueID.XD_ATTR + ", value=davka").equals(s), s);
			xp = compile( // test remove from context
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"string(); finally {Container c = new Container();\n"+
"        c.addItem('a'); c.addItem('b'); c.addItem('c');\n"+
"        AnyValue v=c.removeItem(1);\n"+
"        setText(v.toString() + c.item(0) + c.item(1)); }\" />\n"+
"</xd:def>");
			xml = "<a a='x'/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a a='bac'/>", el);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a attr='required string()'\n"+
"        xd:script=\"finally out('xyz')\">\n"+
"    <a xd:script=\"occurs 2\"/>\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a bttr='attr'><a/><b/></a>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null,null);
			if (reporter.errorWarnings()) {
				assertTrue(reporter.getErrorCount() == 4, reporter.printToString());
			} else {
				fail("error not reported");
			}
			xp = compile(
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"  <a x:script='options trimText'>\n"+
"    <a x:script='occurs 0..2'/>\n"+
"    <b x:script='occurs 0..2'/>\n"+
"    required string(); onTrue out(getText())\n"+
"  </a>\n"+
"</x:def>");
			swr = new StringWriter();
			parse(xp, "", "<a>data</a>", reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("data", swr.toString());
			xdef =
"<x:def xmlns:x='" + _xdNS + "' name='a' root='a'\n"+
"       script='options ignoreEmptyAttributes'>\n"+
"  <a x:script='ref b'>\n"+
"    <p/>\n"+
"    <q/>\n"+
"    optional int(); default 456\n"+
"  </a>\n"+
"  <b attr=\"optional an(); default 'a123x'\"> <c/> </b>\n"+
"</x:def>\n";
			assertEq("<a attr='a123x'><c/><p/><q/>456</a>", parse(xdef, "a", "<a><c/><p/><q/></a>",reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        xd:script='options ignoreEmptyAttributes,ignoreAttrWhiteSpaces,"
				+ "noTrimText,preserveTextWhiteSpaces'>\n"+
"  <a xd:script='ref x' />\n"+
"  <x>\n"+
"    <xd:mixed>\n"+
"      <b xd:script='occurs 0..'/>\n"+
"      <c xd:script='occurs 0..'/>\n"+
"      <d xd:script='occurs 0..'/>\n"+
"    </xd:mixed>\n"+
"  </x>\n"+
"</xd:def>";
			parse(xdef, "", "<a><b/> <c/> </a>", reporter);
			assertNoErrorwarnings(reporter);
			xp = compile( // check initialization of declaration
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"<![CDATA[\n"+
"     void p1(){ out('this is an error!'); }\n"+
"     void p2(){ out('this is an error!'); }\n"+
"     void p3(){ out('this is an error!'); }\n"+
"     void p4(){ out('this is an error!'); }\n"+
"     int ii = 3*5; \n"+
"     int jj = 2*ii; \n"+
"     int kk = 2*jj; \n"+
"     int xx = 2*kk; \n"+
"]]>\n"+
"  </xd:declaration>\n"+
"  <a xd:script = \"finally out('xx = ' + xx)\"/>\n"+
"</xd:def>");
			swr = new StringWriter();
			parse(xp, "", "<a/>", reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("xx = 120", swr.toString());
			xp = compile( //test sequence methods (init, finally)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script=\"init outln('start'); finally outln('end')\">\n"+
"      <b xd:script='occurs 1..*'/>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>");
			_myX = 0;
			swr = new StringWriter();
			parse(xp, "", "<a><b/><b/><b/><c/></a>", reporter, swr,null,null);
			assertNoErrorwarnings(reporter);
			assertEq("start\nend\n", swr.toString());
		} catch(IOException | DOMException | SRuntimeException ex) {fail(ex);}
		try { //test collection, metaNamespace, any and match
			setChkSyntax(false);
			xp = compile( //test collection, metaNamespace, any and match
"<xd:collection xmlns:xd='my.meta.ns' xmlns:x='" + _xdNS + "' x:metaNamespace='my.meta.ns'>\n"+
"<x:def root='a'>\n"+
"  <a>\n"+
"    <xd:any xd:script=\"match 'x' == getQnameLocalpart(getElementName());\n"+
"               onAbsence out('Absence'); finally out('OK')\" />\n"+
"  </a>\n"+
"</x:def>\n"+
"</xd:collection>");
			swr = new StringWriter();
			assertEq("<a/>", parse(xp, "", "<a><b/></a>", reporter, swr, null, null));
			if (!reporter.errorWarnings()) {
				fail();
			} else {
				if ((rep = reporter.getReport()) == null ||
					!"XDEF501".equals(rep.getMsgID())) {
					fail("" + rep);
				}
			}
			assertEq("Absence", swr.toString());
			xml = "<a><x/></a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq("OK", s = swr.toString(), s);
		} catch(Exception ex){fail(ex);}
		setChkSyntax(chkSyntax);
		try {
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='foo'>\n"+
"  <foo><bar><foo xd:script='occurs ?; ref foo'/></bar></foo>\n"+
"</xd:def>");
			parse(xp, "", "<foo><bar/></foo>", reporter);
			assertNoErrorwarnings(reporter);
			xml = "<foo><bar><foo><bar><foo><bar/></foo></bar></foo></bar></foo>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			//test if XML error is recognized (missing '>' on line 2)
			XDBuilder xb = XDFactory.getXDBuilder(null);
			xb.setSource(new InputStream[]{new ByteArrayInputStream(
("<xd:def xmlns:xd='" + _xdNS + "' name='U' root='U' >\n"+
"   <U C='required num(1,9);'\n"+
"      <O J='optional string(1,36);' />\n"+
"   </U>\n"+
"</xd:def>").getBytes())}, new String[]{dataDir + "U.xdef"});
			xb.compileXD();
			fail("Error not reported");
		} catch(Exception ex) {
			s = ex.getMessage();
			if (s == null) {
				fail(ex);
			} else {
				assertTrue(s.contains("XML075"), ex);
				assertTrue(s.contains(dataDir + "U.xdef"), ex);
			}
		}
		try {
			xp = compile(//test recursive references
"<xd:def xmlns:xd='" + _xdNS + "' root='foo'> <foo><bar xd:script='occurs ?; ref foo'/></foo> </xd:def>");
			xml = "<foo/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<foo><bar><bar><bar><bar/></bar></bar></bar></foo>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			//1. references to Xdefinitions with the same prefixes of namespaces
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:def xd:name='t011' xmlns:a='http://www.w3ctest.com'>\n"+
"    <a:to/>\n"+
"  </xd:def>\n"+
"  <xd:def xd:name='a'\n"+
"    xmlns:tns='http://www.w3schools.com'\n"+
"    xd:root='tns:note'\n"+
"    xmlns:a='http://www.w3ctest.com'>\n"+
"    <tns:note>\n"+
"      <a:to xd:script='ref t011#a:to; occurs 1..1'/>\n"+
"    </tns:note>\n"+
"  </xd:def>\n"+
"</xd:collection>";
			xml =
"<a:note xmlns:a='http://www.w3schools.com'><b:to xmlns:b='http://www.w3ctest.com'/></a:note>";
			parse(xdef, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			//2. references to Xdefinitions with different namespace prefixes
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:def xd:name='t011' xmlns:tns='http://www.w3ctest.com'>\n"+
"    <tns:to/>\n"+
"  </xd:def>\n"+
"  <xd:def xd:name='a'\n"+
"    xmlns:tns='http://www.w3schools.com'\n"+
"    xd:root='tns:note'\n"+
"    xmlns:a='http://www.w3ctest.com'>\n"+
"    <tns:note>\n"+
"      <a:to xd:script='ref t011#a:to; occurs 1..1'/>\n"+
"    </tns:note>\n"+
"  </xd:def>\n"+
"</xd:collection>";
			xml =
"<a:note xmlns:a='http://www.w3schools.com'><b:to xmlns:b='http://www.w3ctest.com'/></a:note>";
			parse(xdef, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			xp = compile( // test nillable
"<xd:def root='tns:DM' xmlns:tns='abc' xmlns:xd='" + _xdNS + "'>\n"+
"  <tns:DM xd:script=\"occurs 0..1; options nillable\">\n"+
"    required string(0, 40)\n"+
"  </tns:DM>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			xml =
"<tns:DM xmlns:tns='abc' xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"' xsi:nil='true'/>";
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def root='DM' xmlns:xd='" + _xdNS + "'>\n"+
"  <DM xd:script=\"occurs 0..1; options nillable\">\n"+
"    <a/>\n"+
"    required string(0, 40)\n"+
"    <b/>\n"+
"  </DM>\n"+
"</xd:def>");
			xml = "<DM xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"' xsi:nil='true'/>\n";
			xd = xp.createXDDocument();
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			xml =
"<DM xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"' xsi:nil='true'><a/>x<b/></DM>\n";
			xd = xp.createXDDocument();
			parse(xd, xml, reporter);
			assertErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script='options nillable'>\n"+
"      <c/>\n"+
"      required string(0, 40)\n"+
"      <d/>\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"'><b xsi:nil='true'/></a>\n";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml =
"<a xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"'><b xsi:nil='true' x='x'/></a>\n";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml =
"<a xmlns:xsi='"+XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI+"'><b xsi:nil='true'><c/>x<d/></b></a>\n";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xdef = //getXPos, getSourcePosdition
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method void test.xdef.TestXdef.testPos(XXNode);\n"+
"  </xd:declaration>\n"+
"  <a a='string; onTrue testPos()'> string; onTrue testPos() </a>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xd.xparse("<a\n a = '123'\n>\nx</a>", null);
			xp = compile( //test getXDPosition, getXPos
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='string; onTrue outln(getXDPosition() + \"; \" + getXPos())'>\n"+
"    string; onTrue outln(getXDPosition() + '; ' + getXPos())\n"+
"  </a>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			swr = new StringWriter();
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
			xml = "<a\n a='123'\n>\nx</a>";
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			out.close();
			assertEq("#a/@a; /a/@a\n#a/$text; /a/text()\n", swr.toString());
			xdef = //Container
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    int i = 1;\n"+
"    String s = 'Z';\n"+
"    Container c1 = [%a='A', %b='B'], c2 = new Container();\n"+
"   Container c3 = [1, 'x', s], c4 = [%a='x',1];\n"+
"    NamedValue n = %y='Y';\n"+
"  </xd:declaration>\n"+
"  <a>\n"+
"    string;\n"+
"      onTrue {\n"+
"        c2.setNamedItem(new NamedValue('x', i));\n"+
"        c2.setNamedItem(n);\n"+
"        c2.setNamedItem(%z=s);\n"+
"        c2.setNamedItem(n=%q='Q');\n"+
"        setText(c1.getNamedItem('a').toString() + c1.getNamedItem('b')\n"+
"           + c2.getNamedItem('x')+c2.getNamedItem('y')+c2.getNamedItem('z')\n"+
"           + c2.getNamedItem('q')+c3.getItemType(0)\n"+
"           + (c3.getItemType(1)==$STRING)+(c3.getItemType(0)==$INT)\n"+
"           + n + (c4.getItemType(0) == $INT)\n"+
"           + c4.getNamedItem('a') + c4.getNamedItem('x')); }\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a>x</a>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("AB1YZQ1truetrue%q=Qtruex", el.getChildNodes().item(0).getNodeValue());
			xdef = //test xpath namespace context
"<xd:def root='x:a' xmlns:x='abc' xmlns:z='xyz' xmlns:xd='" + _xdNS + "'>"+
"  <x:a>\n"+
"    <y:b xmlns='xyz' xmlns:y='def' x='required'>\n"+
"      <c><x/>optional string</c>\n"+
"      required string\n"+
"      <d xd:script='finally out(xpath(\"//z:c/text()\"))'/>\n"+
"    </y:b>\n"+
"  </x:a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a:a xmlns:a='abc'>"+
				"<b:b xmlns:b='def' x='x'>"+
				"<c:c xmlns:c='xyz'><c:x/>x</c:c>y<c:d xmlns:c='xyz'/>"+
				"</b:b>"+
				"</a:a>";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			out.close();
			assertEq("x", swr.toString());
		} catch (DOMException | SRuntimeException ex) {fail(ex);}
		String oldProperty = getProperty(XDConstants.XDPROPERTY_WARNINGS);
		try {
			setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
			//test complex types
			xdef = dataDir + "TestXdef_type.xdef";
			xp = compile(xdef);
			xml = dataDir + "TestXdef_type_valid_1.xml";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
		setProperty(XDConstants.XDPROPERTY_WARNINGS, oldProperty);
		try {
			xdef = // optional
"<xd:def xmlns:xd='" + _xdNS + "' root='a'> <a a='? string(0,10);'/> </xd:def>";
			xml = "<a a=''/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"  <a xd:script='options acceptEmptyAttributes' t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a' script='options acceptEmptyAttributes'>"+
"  <a t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"  <a xd:script='options acceptEmptyAttributes; ref b'/>" +
"  <b t='string'/>" +
"</xd:def>");
			xml = "<a t=''/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"  <a xd:script='ref b'/>" +
"  <b xd:script='options acceptEmptyAttributes' t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			xp = compile(
"<x:def xmlns:x='"+_xdNS+"' root='a' script='options acceptEmptyAttributes'>\n"+
"  <x:declaration>uniqueSet x {key: string(3,4)};</x:declaration>\n"+
"  <a><b x:script='+' a='required x.key.ID;'/></a>\n"+
"</x:def>");
			xml = "<a><b a=''/></a>";
			el = parse(xp, "",  xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a><b a='123456'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertTrue(reporter.errors(), "Error not reported");
			assertEq(xml, el);
			xml = "<a><b a=''/><b a=''/></a>"; //empty attribute is not checked!
			el = parse(xp, "",  xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a><b a='12'/></a>";
			assertEq(xml, parse(xp, "",  xml, reporter));
			assertTrue(reporter.errors(), "Error not reported");
			xml = "<a><b a='123'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a><b a='123'/><b a='123'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertTrue(reporter.errors(), "Error not reported");
			assertEq(xml, el);
			xp = compile( // test reference to xd:any
"<xd:def xmlns:xd='" + _xdNS + "' root='x|def'>\n"+
"  <xd:any xd:script=\"match getNamespaceURI()=='u'; options moreAttributes;\n"+
"            finally out(getNamespaceURI()+','+getElementLocalName());\"\n"+
"          xd:name='def'\n"+
"          name='required string'\n"+
"          script='required string'\n"+
"          a='required string'>\n"+
"    <b/>\n"+
"  </xd:any>\n"+
"   <x>\n"+
"     <xd:any xd:script='ref def'/>\n"+
"   </x>\n"+
"</xd:def>");
			xml ="<f a='a' name='b' script='c'><b/></f>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null, null);
			assertErrors(reporter);
			xml =
"<xd:f xmlns:xd='u' a='a' name='b' script='c'><b/></xd:f>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("u,f", swr.toString());
			xml = "<x><f a='a' name='b' script='c'><b/></f></x>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null, null);
			assertErrors(reporter);
			xml =
"<x><xd:f xmlns:xd='u' a='a' name='b' script='c'><b/></xd:f></x>";
			swr = new StringWriter();
			parse(xp, "", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("u,f", swr.toString());
			xdef = // forced conversion of ParseResult to boolean
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    boolean myCheck() {boolean b = eq('ho'); boolean c = b; return c;}\n"+
"  </xd:declaration>\n"+
"  <a a=\"optional myCheck()\"/>\n"+
"</xd:def>\n";
			parse(xdef, "", "<a a='ho'/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>boolean x(boolean b) {return b;}</xd:declaration>\n"+
"  <a a=\"optional x((boolean) eq('ho'));\"/>\n"+
"</xd:def>\n";
			parse(xdef, "", "<a a='ho'/>", reporter);
			assertNoErrorwarnings(reporter);
			xp = compile( //test moreElement option
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='options moreElements'>\n"+
"    <b xd:script='?; finally i++;'/>\n"+
"    <c xd:script='?; finally i++;'/>\n"+
"  </a>\n"+
"  <xd:declaration> int i = 10;</xd:declaration>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			parse(xd, "<a><b/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xd.getVariable("i").intValue(), 12);
			xd = xp.createXDDocument();
			parse(xd, "<a><x/><b/><y/><c/><z/></a>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xd.getVariable("i").intValue(), 12);
			xd = xp.createXDDocument();
			parse(xd, "<a><b/><y/><c/><z/></a>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xd.getVariable("i").intValue(), 12);
			xd = xp.createXDDocument();
			parse(xd, "<a><b/><c/><z/></a>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xd.getVariable("i").intValue(), 12);
			xd = xp.createXDDocument();
			parse(xd, "<a><x/><b/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xd.getVariable("i").intValue(), 12);
			xp = compile( // Test fully qualified method call
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:script='finally out(test.xdef.TestXdef.getInt5());'/>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			swr = new StringWriter();
			out = XDFactory.createXDOutput(swr, false);
			xd.setStdOut(out);
			parse(xd, "<a/>", reporter);
			assertNoErrorwarnings(reporter);
			out.close();
			assertEq("5", swr.toString());
			xp = compile( //X-definition ver 3.1 //////////////////////////////////////
"<xd:def xmlns:xd='" + XDConstants.XDEF31_NS_URI + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method XDParser test.xdef.TestXdef.licheCislo();\n"+
"  </xd:declaration>\n"+
"  <a a='licheCislo'/>\n"+
"</xd:def>\n");
			parse(xp, "", "<a a=\" 1 \"/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a=\"10\"/>", reporter);
			assertErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>external Parser licheCislo;</xd:declaration>\n"+
"  <a a='licheCislo()'/>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			xd.setVariable("licheCislo", new LicheCislo());
			parse(xd, "<a a=' 1 '/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='10'/>", reporter);
			assertErrors(reporter);
			xp = compile( // result of xpath (ie. Container) in boolean expression
"<xd:def xmlns:xd='" + _xdNS + "' root='a|b|c|d'>\n"+
"  <a typ='int()'>\n"+
"    <xd:choice>\n"+
"      <b xd:script=\"match xpath('parent::a[@typ=1]')\"/>\n"+
"      <c xd:script=\"match xpath('parent::a[@typ=2]')\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"  <b typ='int()'>\n"+
"    <xd:choice>\n"+
"      <b xd:script=\"match xpath('../@typ=&quot;1&quot;')\"/>\n"+
"      <c xd:script=\"match xpath('../@typ=2')\"/>\n"+
"    </xd:choice>\n"+
"  </b>\n"+
"  <c typ='int()'>\n"+
"    <xd:choice>\n"+
"       <b xd:script=\"match xpath('//c[@typ=&quot;1&quot;]')\"/>\n"+
"       <c xd:script=\"match xpath('//c[@typ=2]')\"/>\n"+
"    </xd:choice>\n"+
"  </c>\n"+
"  <d a=\"optional int\"\n"+
"     b=\"optional int\"\n"+
"     xd:script=\"finally if (!(xpath('@a') XOR xpath('@b')))\n"+
"        error('EE', '@a, @b mus be excluzive');\"/>\n"+
"</xd:def>");
			xml = "<a typ='1'><b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a typ='2'><b/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='2'><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a typ='1'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='4'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<b typ='1'><b/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b typ='2'><b/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<b typ='2'><c/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b typ='1'><c/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<c typ='1'><b/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<c typ='2'><b/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<c typ='2'><c/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<c typ='1'><c/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<d a='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<d b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<d a='1' b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xml = "<d/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xp = compile( // variables declared in script of Element
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:script=\"var int b=0,c=0; occurs *; finally out('B='+b+',C='+c);\">\n"+
"    <B xd:script=\"occurs *; finally b++;\"/>\n"+
"    <C xd:script=\"occurs *; finally c++;\"/>\n"+
"  </A>\n"+
"</xd:def>");
			swr = new StringWriter();
			xml = "<A><B/><B/><C/></A>";
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq("B=2,C=1", swr.toString());
			xp = compile( // test ignore
"<xd:def name='a' root='root' xmlns:xd='" + _xdNS + "'>\n"+
"  <root xd:text=\"ignore\">\n"+
"    <xd:choice>\n"+
"      <a xd:script=\"occurs 0..2\" />\n"+
"      <b xd:script=\"occurs 0..2\" />\n"+
"    </xd:choice>\n"+
"  </root>\n"+
"</xd:def>");
			xml = "<root>text1<a/>text2</root>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<root><a/></root>", el);
			xdef =
"<xd:def xmlns:xd='"+ _xdNS +"' root='a'><a> <x attr='ignore;'> <xd:text>ignore</xd:text></x> </a></xd:def>";
			xml = "<a><x attr='attr'>text</x></a>";
			assertEq("<a><x/></a>", parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <x xd:script='occurs ignore;' attr='ignore;'>\n"+
"     <xd:text>ignore</xd:text>\n"+
"    </x>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><x attr='attr'>text</x></a>";
			assertEq("<a/>", parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='occurs ignore;'>\n"+
"    <x xd:script='occurs ignore;' attr='ignore;'>\n"+
"     <xd:text>ignore</xd:text>\n"+
"    </x>\n"+
"  </a>\n"+
"</xd:def>";
			assertNull(parse(xdef, "", "<a><x attr='attr'>text</x></a>", reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // xd:attr
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <a xd:attr='* getAttrName().startsWith(\"impl-\")'/>\n"+
"</xd:def>");
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a impl-a='1' impl-bb='2' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a impl-a='1' ympl-bb='2' />";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n"+
"  <a xd:attr='ignore int'>\n"+
"    <B xd:script='ignore'/>\n"+
"    ignore string\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x='1' y='2' z = '3'><B><C/>z</B><B/>abc</a>";
			assertEq("<a/>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(// test option ignoreOther
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n"+
"  <a xd:script='options ignoreOther' x='? int'/>\n"+
"</xd:def>");
			xml = "<a x='1' y='2' z = '3'><B a='a'><C/>z</B><B/>abc</a>";
			assertEq("<a x='1'/>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a y='2' z = '3'><B a='a'><C/>z</B><B/>abc</a>";
			assertEq("<a/>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(// test option acceptOther
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'> <a xd:script='options acceptOther' x='? int'/> </xd:def>");
			xml = "<a x='1' y='2' z = '3'><B a='a'><C/>z</B><B/>abc</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a y='2' z = '3'><B a='a'><C/>z</B><B/>abc</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(// illegal
"<xd:def root='a' xmlns:xd='" + _xdNS + "'>\n"+
"<a xd:text=\"illegal\">\n"+
"  <xd:choice>\n"+
"    <a xd:script=\"occurs 0..2\" />\n"+
"    <b xd:script=\"occurs 0..2\" />\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>");
			xml = "<a>text1<a/>text2</a>";
			el = parse(xp, "", xml, reporter);
			if (reporter.getErrorCount() != 2 || !"XDEF528".equals(reporter.getReport().getMsgID())
				|| !"XDEF528".equals(reporter.getReport().getMsgID())) {
				fail(reporter.printToString());
			}
			assertEq("<a><a/></a>", el);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n"+
"  <a xd:attr='illegal int'>\n"+
"    <B xd:script='illegal'/>\n"+
"    illegal string\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x='1' y='2' z = '3'><B><C/>z</B><B/>abc</a>";
			assertEq("<a/>", parse(xp, "", xml, reporter));
			assertEq(6, reporter.getErrorCount(), reporter.printToString());
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> ParseResult x() {return int();}</xd:declaration>" +
"  <a x='x()'/>" +
"</xd:def>");
			xml = "<a x='1' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <xd:declaration scope = 'local'>\n" +
"    int i = - 0x_ff_ff_ff_ff_ff_ff_ff__ff_;\n" +
"    float x = -1_1_.2_30_e2;\n" +
"    NamedValue nv = %x:y..n-v=%y=%z=-0d123__456_890_999_000_333.0;\n" +
"    ParseResult p() {\n"	+
"      String s = getText();\n" +
"      ParseResult p;\n" +
"      try {\n" +
"        p = int(s);\n" +
"        int i = p.getValue();\n" +
"        if (i != 123) {\n" +
"          p.error('e123');\n" +
"        }\n" +
"        return p;\n" +
"      } catch (Exception ex) {\n" +
"        p = new ParseResult(s);\n" +
"        p.error(ex.getMessage());\n" +
"        return p;\n" +
"      }\n" +
"    }\n"	+
"  </xd:declaration>" +
"  <a xd:script=\"var int j; finally out('i='+i+',j='+j+',x='+x+','+nv);\"\n" +
"     a='p(); onTrue {j = getParsedValue();}'/>\n" +
"</xd:def>");
			xml = "<a a='123'/>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "i=1,j=123,x=-1123.0,%x:y..n-v=%y=%z=-123456890999000333.0");
			swr = new StringWriter();
			assertEq(xml, create(xp, "", "a", reporter, xml, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "i=1,j=123,x=-1123.0,%x:y..n-v=%y=%z=-123456890999000333.0");
			xp = compile( // types in different declarations
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"  <xd:declaration>\n" +
"    int i = 1;\n" +
"    type paramCode string(i);\n" +
"  </xd:declaration>\n" +
"  <xd:declaration>\n" +
"    int j = i;\n" +
"    type xx zz;\n" +
"  </xd:declaration>\n" +
"  <xd:declaration> type zz paramCode; </xd:declaration>\n" +
"  <a paramCode='xx' />\n" +
"</xd:def>");
			xml = " <a paramCode='xx'/>";
			assertEq("<a paramCode='xx'/>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // types in different declarations
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"  <xd:declaration>\n" +
"     int j = i;\n" +
"     type xx zz;\n" +
"  </xd:declaration>\n" +
"  <xd:declaration> type zz paramCode(); </xd:declaration>\n" +
"  <xd:declaration>\n" +
"    int i = 1;\n" +
"    type paramCode string(i);\n" +
"  </xd:declaration>\n" +
"   <a paramCode='xx()' />\n" +
"</xd:def>");
			xml = "<a paramCode='xx'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // test correct error reporting
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int x = 0; </xd:declaration>\n" +
"  <a x=\"?; onAbsence x = -1;\">\n" +
"    <A xd:script=\"occurs 1; onAbsence if (x==-1) error('Missing x');\"/>\n"+
"    <B xd:script=\"occurs ?\"/>\n" +
"    <C xd:script=\"occurs ?\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml = "<a x=\"1\"><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x=\"1\"><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x=\"1\"><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><A/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int x = 0; </xd:declaration>\n" +
"  <a x=\"?; onAbsence x = -1;\">\n" +
"    <A xd:script=\"occurs ?; onAbsence if (x == -1) error('Missing x');\"/>\n"+
"    <B xd:script=\"occurs ?\"/>\n" +
"    <C xd:script=\"occurs ?\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml = "<a x=\"1\"><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x=\"1\"><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a x=\"1\"><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><A/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xml = "<a><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().startsWith("E: Missing x"),reporter);
			xp = compile( // test var section of element script
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"  <a xd:script=\"var String s = 'x';\">\n" +
"    <B xd:script=\"var int count = 0; occurs 1..*; finally s += count;\">\n"+
"      string; onAbsence addText(s);\n" +
"    </B>\n" +
"  </a>\n" +
"</xd:def>");
			xml = "<a><B/></a>";
			assertEq("<a><B>x</B></a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><B/><B/></a>";
			assertEq("<a><B>x</B><B>x0</B></a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(// test variable parameter in validation method.
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"  <xd:declaration scope=\"local\">\n" +
"    int   max;\n" +
"    type  cislo1 int(1,max); \n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <Item xd:script=\"*\" Size=\"int(); onTrue max=parseInt(getText())\" Number=\"cislo1()\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml =
"<a>\n" +
"  <Item Size=\"1\" Number=\"1\"/>\n" +
"  <Item Size=\"2\" Number=\"2\"/>\n" +
"  <Item Size=\"3\" Number=\"3\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<a>\n" +
"  <Item Size=\"1\" Number=\"1\"/>\n" +
"  <Item Size=\"2\" Number=\"11\"/>\n" +
"  <Item Size=\"3\" Number=\"3\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(s.contains("XDEF813") && s.contains("maxInclusive"), s);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"  <xd:declaration scope=\"local\">Parser  xtype;</xd:declaration>\n" +
"  <a>\n" +
"    <Item xd:script=\"*\" Type=\"xdType(); onTrue xtype=getParsedValue()\" Value=\"xtype()\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml =
"<a>\n" +
"  <Item Value=\"Alfa\"     Type=\"string(1,5)\"/>\n" +
"  <Item Value=\"99\"       Type=\"int(1,99)\"/>\n" +
"  <Item Value=\"3.6.2002\" Type=\"xdatetime('d.M.yyyy')\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml =
"<a>\n" +
"  <Item Value=\"Alfa\"     Type=\"string(1,5)\"/>\n" +
"  <Item Value=\"Beta\"     Type=\"int(1,99)\"/>\n" +
"  <Item Value=\"3.6.2002\" Type=\"xdatetime('d.M.yyyy')\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(s.contains("XDEF813"), s);
			xp = compile( // variants in xdatetime
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <B xd:script='occurs 2' a=\"xdatetime('dd.MM.yy|dd.MM.yyyy')\"/>\n"+
"    <C xd:script='occurs 2' a=\"xdatetime('dd.MM.yyyy|dd.MM.yy')\"/>\n"+
"  </a>\n"+
"</xd:def>");
			xml =
"<a>\n"+
"  <B a='11.06.87'/><B a='11.06.1987'/>\n"+
"  <C a='11.06.87'/><C a='11.06.1987'/>\n"+
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile(//test collection
"<xd:collection xmlns:xd='" + XDConstants.XDEF31_NS_URI + "'>"+
"<xd:def xd:name='X' xd:root='a' xmlns:xd='" + XDConstants.XDEF40_NS_URI + "'>"+
"  <a a='string()'>"+
"    <B xd:script='+; ref X#R'/>"+
"  </a>"+
"  <R r='optional string()'/>"+
"</xd:def>"+
"<xd:def xd:name='Y' xd:root='B' xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>"+
"  <B b='string()'/>"+
"</xd:def>"+
"</xd:collection>");
			xml = "<a a='x'><B r='y'/></a>";
			assertEq(xml, parse(xp, "X", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<B b='y'/>";
			assertEq(xml, parse(xp, "Y", xml, reporter));
			assertNoErrorwarnings(reporter);
			// test DOCTYPE
			setProperty(XDConstants.XDPROPERTY_DOCTYPE, "true");
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a' xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <a><body>string</body></a>\n" +
"</xd:def>";
			xml =
"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'\n" +
"  'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n" +
"<a xmlns='http://www.w3.org/1999/xhtml'><body> xxx&nbsp;xxx </body></a>";
			el = parse(xdef, "", xml, reporter);
			assertTrue(el.getOwnerDocument().getDoctype() != null, "NULL");
			assertNoErrorwarnings(reporter);
			xp = compile(//test matches with parsers and declared types
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:declaration scope='local'>\n"+
"    int i=3, j=4;\n"+
"    type t int(i,j);\n"+
"    void m() {\n"+
"      out('x' + int(i,j).parse(\"3\").matches());\n"+
"      out(t.parse(\"1\").matches());\n"+
"      Parser p=int(i,j);\n"+
"      out(p.parse(\"3\").matches());\n"+
"    }\n"+
"  </xd:declaration>\n"+
"  <a xd:script='var {\n"+
"      int i=1, j=2; type t int(i,j); boolean b=t.parse(\"3\").matches();}'\n"+
"     b = 't; finally {out(b); m();}'/>\n"+
"</xd:def>");
			xd = xp.createXDDocument();
			xml = "<a b='1'/>";
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("falsextruefalsetrue", swr.toString());
			xp = XDFactory.compileXD(null, // test metanamespace MUST compile XDFactory.compileXD!!!
"<xd:def xmlns:xd='meta.b.cz' xmlns:w='"+_xdNS+"'\n" +
"        w:metaNamespace='meta.b.cz' name='X' xd:root='A'>\n" +
"  <A a='string'> <w:B xd:script='*'/> </A>\n" +
"</xd:def>");
			xml = "<A a='a'><x:B xmlns:x='"+_xdNS+"'/></A>";
			assertEq(xml, parse(xp, "X", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // test nested type declatation
"<xd:def xmlns:xd='"+XDConstants.XDEF31_NS_URI+"' xd:root='A'>\n" +
"  <xd:declaration> type t1 string(1, 40); </xd:declaration>\n" +
"  <xd:declaration> type t2 t1(); </xd:declaration>\n" +
"  <A>t2();</A>\n" +
"</xd:def>");
			xml = "<A>xyz</A>";
			assertEq(xml, parse(xp,"", xml, null, reporter));
			assertNoErrorwarnings(reporter);
			xdef = // test addComment,insertComment,addPI,insertPI
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"finally {\n"+
"insertComment('a0');addComment('a1');insertPI('a','0');addPI('a','1')}\">\n"+
"    string(); finally {insertComment('10');addComment('11');insertPI('_1','0');addPI('_1','1')}\n"+
"    <b xd:script=\"finally {insertComment('b0');addComment('b1');insertPI('b','0');addPI('b','1')}\">\n"+
"       string(); finally {insertComment('20');addComment('21');insertPI('_2','0'); addPI('_2','1')}\n"+
"    </b>\n"+
"    string(); finally {\n"+
"insertComment('30');addComment('31');insertPI('_3','0'); addPI('_3','1')}\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a>1<b>2</b>3</a>";
			assertEq(KXmlUtils.nodeToString(parse(xdef, "", xml).getOwnerDocument()),
"<!--a0--><?a 0?><a>"+
"<!--10-->1<?_1 0?><!--11--><?_1 1?><!--b0--><?b 0?>"+
"<b><!--20-->2<?_2 0?><!--21--><?_2 1?></b><!--b1--><?b 1?>"+
"<!--30-->3<?_3 0?><!--31--><?_3 1?>"+
"</a><!--a1--><?a 1?>");
			xp = compile( // test addText,insertText
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"finally addText('def');\">\n"+
"    <b xd:script=\"finally insertText('abc');\"/>\n"+
"  </a>\n"+
"</xd:def>");
			xml = "<a><b/></a>";
			assertEq("<a>abc<b/>def</a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xp = compile( // test importLocal attribute
"<xd:collection xmlns:xd='"+_xdNS+"'>\n"+
"<xd:def name='A' root='A'>\n"+ // no importLocal
"  <xd:declaration scope='local'>\n"+
"    void a() {out(xx() + xxx + yy() + yyy);}\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"  <A a = 'x' b = 'y' c = 'u.c.ID' xd:script='finally a();' />\n"+
"</xd:def>\n"+
"<xd:def name='B' root='A' importLocal='X'>\n"+ // importLocal from X
"  <xd:declaration scope='local'>\n"+
"    void a() {out(xx() + xxx + yy() + yyy);}\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"  <A a = 'x' b = 'y' c = 'u.c.ID' xd:script='finally a();' />\n"+
"</xd:def>\n"+
"<xd:def name='C' root='A' importLocal='Y'>\n"+ // importLocal from Y
"  <xd:declaration scope='local'>\n"+
"    void a() {out(xx() + xxx + yy() + yyy);}\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"  <A a = 'x' b = 'y' c = 'u.c.ID' xd:script='finally a();' />\n"+
"</xd:def>\n"+
"<xd:def name='D' root='A' importLocal='X,Y'>\n"+ // importLocal from X,Y
"  <xd:declaration scope='local'>\n"+
"    void a() {out(xx() + xxx + yy() + yyy);}\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"  <A a = 'x' b = 'y' c = 'u.c.ID' xd:script='finally a();' />\n"+
"</xd:def>\n"+
"<xd:def name='E' root='A' importLocal='X,Y'>\n"+ // local and importLocal
"  <xd:declaration scope='local'>\n"+
"    String xx() {return 'Exx';}\n"+
"    type x eq('Ex');\n"+
"    type y eq('Ey');\n"+
"    int xxx = 1;\n"+
"    void a() {out(xx() + xxx + yy() + yyy);}\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"  <A a = 'x' b = 'y' c = 'u.c.ID' xd:script='finally a();' />\n"+
"</xd:def>\n"+
"<xd:def name = 'X'>\n"+ // define locals  X
"  <xd:declaration scope='local'>\n"+
"    String xx() {return 'Xxx';}\n"+
"    type x eq('Xx');\n"+
"    int xxx = 2;\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name = 'Y'>\n"+ // define locals  Y
"  <xd:declaration scope='local'>\n"+
"    String yy() {return 'Yyy';}\n"+
"    type y eq('Yy');\n"+
"    int yyy = 3;\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name = 'Z'>\n"+ // define globals
"  <xd:declaration scope='global'>\n"+
"    String xx() {return 'Zxx';}\n"+
"    String yy() {return 'Zyy';}\n"+
"    type x eq('Zx');\n"+
"    type y eq('Zy');\n"+
"    int xxx = 4;\n"+
"    int yyy = 2;\n"+
"    uniqueSet u {c: x}\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>");
			xd = xp.createXDDocument("A");
			xd.setStdOut(swr = new StringWriter());
			xml = "<A a='Zx' b='Zy' c='Zx'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("Zxx4Zyy2", swr.toString());
			xd = xp.createXDDocument("B");
			xd.setStdOut(swr = new StringWriter());
			xml = "<A a='Xx' b='Zy' c='Xx'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("Xxx2Zyy2", swr.toString());
			xd = xp.createXDDocument("C");
			xd.setStdOut(swr = new StringWriter());
			xml = "<A a='Zx' b='Yy' c='Zx'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("Zxx4Yyy3", swr.toString());
			xd = xp.createXDDocument("D");
			xd.setStdOut(swr = new StringWriter());
			xml = "<A a='Xx' b='Yy' c='Xx'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("Xxx2Yyy3", swr.toString());
			xd = xp.createXDDocument("E");
			xd.setStdOut(swr = new StringWriter());
			xml = "<A a='Ex' b='Ey' c='Ex'/>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("Exx1Yyy3", swr.toString());
		} catch (RuntimeException ex) {fail(ex);}
		try { // test FileReportWriter as parameter of compilation.
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "'><A a='xxxx()'/></xd:def>";
			reporter.clear();
			XDFactory.compileXD(reporter, null, xdef);
			assertTrue(reporter.printToString().contains("XDEF443"));
		} catch (RuntimeException ex) {fail(ex);}
		reporter.clear();
		try { // test DOCTYPE not allowed
			setProperty(XDConstants.XDPROPERTY_DOCTYPE, "false");
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='a' xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <a><body>string</body></a>\n" +
"</xd:def>";
			xml =
"<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Transitional//EN'\n" +
"  'http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd'>\n" +
"<a xmlns='http://www.w3.org/1999/xhtml'><body> xxx&nbsp;xxx </body></a>";
			el = parse(xdef, "", xml, reporter);
			assertTrue(el.getOwnerDocument().getDoctype() != null, "NULL");
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {
			if ((s = ex.getMessage()) == null || !s.contains("XML099")) {fail(ex);}
		}
		try {// test "classpath" and "file" protocol in URL
			XDFactory.compileXD(null, //without wildcards
				"classpath://org.xdef.impl.compile.XdefOfXdefBase.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef31.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef32.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef40.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef41.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef42.xdef");
			XDFactory.compileXD(null, "classpath://org.xdef.impl.compile.XdefOfXdef*.xdef"); //with wildcards
			XDFactory.compileXD(null, //collection without wildcards
"<xd:collection xmlns:xd='" + _xdNS + "'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef31.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef32.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef40.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef41.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef42.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdefBase.xdef;'/>");
			XDFactory.compileXD(null, //collection with wildcards
"<xd:collection xmlns:xd='" + _xdNS + "'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef*.xdef'/>");
			XDFactory.compileXD(null, //X-definition with imports with wildcards
"<xd:def xmlns:xd='" + _xdNS + "' name='xxx'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef*.xdef'/>");
		} catch (RuntimeException ex) {fail(ex);}
		try { //Test default property "xdef_warning"s and values "true" and "false".
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='a'>\n"+
"  <a a=\"list('x','y')\" b=\"x()\"> </a>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(new Properties(), xdef);// empty property
			xd = xp.createXDDocument();
			xd.xparse("<a a='y' b='z'/>", null);
			fail("Error not thrown");
		} catch (RuntimeException ex) {
			if ((s = ex.getMessage()) == null || !s.contains("XDEF998")) {fail(ex);}
		}
		try { // test with property xdef_warnings=false
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method boolean test.xdef.TestXdef.x(XXData x);\n"+
"  </xd:declaration>\n"+
"  <a a=\"list('x','y')\" b=\"x()\"> </a>\n"+
"</xd:def>";
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
			xp = XDFactory.compileXD(props, xdef);
			xd = xp.createXDDocument();
			xd.xparse("<a a='y' b='z'/>", null);
		} catch (RuntimeException ex) {fail(ex);}
		if (_xdNS.contains("/xdef/3.")) {
			return;// skip if version is less then 4.0
		}
		// only versions higher then version 3.2
		try {
			xp = compile( // test choice in root specification
"<xd:def xmlns:xd='"+_xdNS+"' root='A|B|Z'>\n" +
"  <xd:choice name='A'>\n" +
"    <A/>\n" +
"    <B xd:script='match !@a'/>\n" +
"    <B a='int()'><X/></B>\n" +
"  </xd:choice>\n" +
"  <xd:choice name='B'>\n" +
"    <C/>\n" +
"    <D xd:script='match @b' b='int()'><Y/></D>\n" +
"    <D/>\n" +
"  </xd:choice>\n" +
"  <Z/>\n" +
"</xd:def>");
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<B/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<B a='1'><X/></B>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<C/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<D/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<D b='2'><Y/></D>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<Z/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (RuntimeException ex) {fail(ex);}
		try { // test X-script method now() and default zone
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a='dateTime(); onTrue {Datetime d = (Datetime) getParsedValue(); outln(d.getZoneName());\n" +
"          outln(d.toString()); d.setZoneName(\"GMT\"); outln(d.toString()); outln(d.getZoneName());\n"+
"          d.setZoneOffset(-3600000); outln(d.toString());}'/>\n" +
"  <xd:component> %class test.xdef.TestTZ%link a; </xd:component>\n"+
"</xd:def>");
			xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertEq(xml, parse(xd, xml, reporter));
			assertEq(swr.toString(),
				"CET\n2024-10-22T11:55:30+02:00\n2024-10-22T09:55:30Z\nGMT\n2024-10-22T08:55:30-01:00\n");
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T08:55:30-01:00'/>", xc.toXml());
			xml = "<a a='2024-10-22T11:55:30+03:30'/>"; // zone SPECIFIED
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr = new StringWriter(), false));
			assertEq(TimeZone.getTimeZone("CET"), xd.getDefaultZone());
			assertNoErrorsAndClear(reporter);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq(swr.toString(),
				"GMT\n2024-10-22T11:55:30+03:30\n2024-10-22T08:25:30Z\nGMT\n2024-10-22T07:25:30-01:00\n");
			genXComponent(xp, clearTempDir());
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T07:25:30-01:00'/>", xc.toXml());
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a=\"xdatetime('yyyy-MM-ddTHH:mm[:ss][Z]', 'yyyy-MM-ddTHH:mmZ'); /* date and time, no seconds */\"/>\n" +
"  <xd:component> %class test.xdef.TestTZ1%link a; </xd:component>\n"+
"</xd:def>");
			genXComponent(xp, clearTempDir());
			xml = "<a a='2024-10-22T11:55:15'/>";
			assertEq("<a a='2024-10-22T11:55+02:00'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T11:55:15+02:00'/>", xc.toXml());
			xml = "<a a='2024-10-22T11:55Z'/>";
			assertEq("<a a='2024-10-22T11:55Z'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='2024-10-22T11:55Z'/>", xc.toXml());
			assertEq(null, compile("<def xmlns='"+_xdNS+"'/>").getDefaultZone());
			xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <a a='dateYMDhms();'/>\n" +
"  <xd:component> %class test.xdef.TestTZ2%link a; </xd:component>\n"+
"</xd:def>");
			genXComponent(xp, clearTempDir());
			xml = "<a a='20241022115530'/>";
			assertEq("<a a='20241022115530'/>", parse(xp, "", xml));
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertEq("<a a='20241022115530'/>", xc.toXml());
			assertEq("2024-10-22T11:55:30+02:00",
				((SDatetime) SUtils.getValueFromGetter(xc,"geta")).toString());
		} catch (RuntimeException ex) {fail(ex);}
		try {
			props = new Properties();
			props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = XDFactory.compileXD(props,
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='a'>\n"+
"  <a a=\"ydatetime('yyyy-MM-ddTHH:mm:ss[ZZ]', 'yyyy-MM-ddTHH:mm:ssZ');\"/>\n" +
"</xd:def>");
			xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T11:55:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30Europe/Prague'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T11:55:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30-03:30'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T17:25:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30GB'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T12:55:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30Etc/GMT-14'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-21T23:55:30+02:00\"/>", xd.xparse(xml, null));
			xml = "<a a='2024-10-22T11:55:30NZ'/>"; // zone specified
			xd = xp.createXDDocument();
			assertEq("<a a=\"2024-10-22T00:55:30+02:00\"/>", xd.xparse(xml, null));
		} catch (RuntimeException ex) {fail(ex);}
		try { // test default zone and ydatetime
			setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
			xp = compile(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='a'>\n"+
"  <a a=\"ydatetime('yyyy-MM-ddTHH:mm:ss[ZZ]', 'yyyy-MM-ddTHH:mm:ssZ');\"/>\n" +
"</xd:def>");
			assertEq("<a a='2024-10-22T11:55:30+02:00'/>",
				parse(xp, "", "<a a='2024-10-22T11:55:30'/>", null)); // zone NOT specified
			assertEq("<a a='2024-10-22T17:25:30+02:00'/>",
				parse(xp,"","<a a='2024-10-22T11:55:30-03:30'/>",null)); // zone specified
			assertEq("<a a='2024-10-21T23:55:30+02:00'/>",
				parse(xp, "", "<a a='2024-10-22T11:55:30Etc/GMT-14'/>", null)); // zone specified
		} catch (RuntimeException ex) {fail(ex);}
		setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, null);
		try { // test minYear, maxYear, specDates
			setProperty(XDConstants.XDPROPERTY_MINYEAR, "1900");
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, "2100");
			setProperty(XDConstants.XDPROPERTY_SPECDATES, "3000-12-31T23:59:59");
			xp = compile( //ydatetime
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\">\n" +
"  <root datum=\"ydatetime('yyyy-MM-ddTHH:mm:ss[Z]', 'yyyy-MM-ddTHH:mm:ss');\" />\n" +
"</xd:def>");
			xml = "<root datum=\"2024-11-04T10:00:00\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xml = "<root datum=\"3024-11-04T10:00:00\"/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF818"));
			reporter.clear();
			xml = "<root datum=\"a3024-11-04T10:00:00\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF809"));
			reporter.clear();
			xml = "<root datum=\"2024-11-04T10:00:00a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			reporter.clear();
			xml = "<root datum=\"3024-11-04T10:00:00a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			xp = compile( //datetime
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\"><root datum=\"dateTime();\" /></xd:def>");
			xml = "<root datum=\"2024-11-04T10:00:00\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xml = "<root datum=\"3024-11-04T10:00:00\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF818"));
			reporter.clear();
			xml = "<root datum=\"a3024-11-04T10:00:00\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF809"));
			reporter.clear();
			xml = "<root datum=\"2024-11-04T10:00:00a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			reporter.clear();
			xml = "<root datum=\"3024-11-04T10:00:00a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			xp = compile( //date
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\"><root datum=\"date();\"/></xd:def>");
			xml = "<root datum=\"2024-11-04\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xml = "<root datum=\"3024-11-04\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF818"));
			reporter.clear();
			xml = "<root datum=\"a3024-11-04\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF809"));
			reporter.clear();
			xml = "<root datum=\"2024-11-04a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			reporter.clear();
			xml = "<root datum=\"3024-11-04a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			xp = compile( //dateYMDhms
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"root\"><root datum=\"dateYMDhms();\" /></xd:def>");
			xml = "<root datum=\"20241104100000\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xml = "<root datum=\"30241104100000\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF818"));
			reporter.clear();
			xml = "<root datum=\"a30241104100000\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF809"));
			reporter.clear();
			xml = "<root datum=\"20241104100000a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
			reporter.clear();
			xml = "<root datum=\"30241104100000a\" />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.printToString().contains("XDEF804"));
		} catch (Exception ex) {fail(ex);}
		setProperty(XDConstants.XDPROPERTY_MINYEAR, null);
		setProperty(XDConstants.XDPROPERTY_MAXYEAR, null);
		setProperty(XDConstants.XDPROPERTY_SPECDATES, null);
		try { // test "implements"
			xp = compile(new String[] {
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" xd:name=\"Types\">\n" +
"    <xd:declaration scope=\"global\">\n" +
"        type  cisloSmlouvy  string(1,35);\n" +
"        type  id            long(-1,999_999_999_999); /* Gam_Type */\n" +
"        type  poradiVozidla string(1,10);\n" +
"    </xd:declaration>\n" +
"</xd:def>\n",
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" xd:root=\"IdentSmlouvy\" xd:name=\"Common\">\n" +
"  <IdentSmlouvy CisloSmlouvy=\"cisloSmlouvy()\" IdPojistitel=\"id()\" PoradiVozidla=\"poradiVozidla()\"/>\n"+
"</xd:def>\n",
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" xd:root=\"IdentSmlouvy\" xd:name=\"Example\">\n" +
"    <IdentSmlouvy IdPojistitel=\"id()\" CisloSmlouvy=\"cisloSmlouvy()\" PoradiVozidla=\"poradiVozidla()\"\n"+
"                  xd:script = \"implements Common#IdentSmlouvy\"/>\n" +
"</xd:def>\n"});
			xml = "<IdentSmlouvy CisloSmlouvy=\"c\" IdPojistitel=\"1\" PoradiVozidla=\"p\"/>";
			xd = xp.createXDDocument("Common");
			assertEq(xml, parse(xd,xml, reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument("Example");
			assertEq(xml, parse(xd,xml, reporter));
			assertNoErrorsAndClear(reporter);
		} catch (RuntimeException ex) {fail(ex);}

		clearTempDir(); // delete created temporary files
		resetTester();
	}

////////////////////////////////////////////////////////////////////////////////
// methods and objects used in X-definitions as external.
////////////////////////////////////////////////////////////////////////////////
	public static final long getInt5() {return 5;}
	public static final void testOldx(final XXNode xnode, final XDValue[] params) {
		Element el = xnode.getElement();
		if (el == null) {
			xnode.error("", "Object is null");
		} else if (!"a".equals(el.getNodeName())) {
			xnode.error("","Object is not element 'a'");
		}
	}
	public static final void testOldy(final XXData xdata, final XDValue[] params) {
		String s = xdata.getTextValue();
		if (s == null) {
			xdata.error("", "Object is null");
		} else if (!"1".equals(s)) {
			xdata.error("", "Object is not String '1'");
		}
	}
	public static final void myCheck(final XXElement xel, final String s, final byte[] b) {
		if (!s.equals(new String(b))) {
			((TestXdef) xel.getXDDocument().getUserObject()).fail("Check");
		}
	}
	public final void myProc(final String s) {_myX = 1;}
	public static final void myProc(final XXNode xnode, final String s) {_myX = 2;}
	public static final void myProc(final XDValue[] p) {_myX = 3;}
	public static final void setDateProc(final XXData xdata, final XDValue[] params) {
		String s = params[0].datetimeValue().formatDate("yyyy-MM-dd");
		xdata.setTextValue(s);
	}
	public final static boolean testExt(final XXElement xel, final String a, final String b, final String c) {
		if (_myX == 1 && "a".equals(a) && "b".equals(b) && "c".equals(c)) {
			_myX = 0;
			return true;
		}
		return false;
	}
	public final static void myErr(final XXNode xel, final XDValue[] params) {
		xel.getTemporaryReporter().clear();
		if (params.length==1 && params[0].getItemId()==XDValueID.XD_LONG && params[0].longValue()==4204) {
			xel.getTemporaryReporter().clear();
			_myX = 4204;
		} else {
			_myX = 1;
		}
	}
	public final static long myError() {throw new RuntimeException("MyError");}
	public final static void testPos(final XXNode xnode) {}
	/** Check datetime according to mask1. If parsed value has time zone UTC,
	 * then convert date to the local time. Format of result is given by mask2.
	 * @param xdata actual XXData object.
	 * @param args array of parameters.
	 * @return true if format is OK.
	 */
	public final static XDParseResult dateToLocal(final XXData xdata, final XDValue[] args) {
		String mask1 = args.length >= 1 ? args[0].toString() : "yyyyMMddTHHmmss[Z]";
		String s = xdata.getTextValue();
		StringParser p = new StringParser(s);
		if (!p.isDatetime(mask1)) {
			DefParseResult result = new DefParseResult(s);
			result.error("E01", "Chybny format");
			return result;
		}
		if (!p.testParsedDatetime()) {
			DefParseResult result = new DefParseResult(s);
			result.error("E02", "Chybna hodnota");
			return result;
		}
		SDatetime sd = p.getParsedSDatetime();
		sd.toTimeZone(TimeZone.getTimeZone("GMT"));
		String mask2 = args.length >= 2 ? args[1].toString() : "yyyyMMddTHHmmssZ";
		xdata.setTextValue(sd.formatDate(mask2));
		return new DefParseResult(s,new DefDate(sd));
	}
	private static final class LicheCislo extends XDParserAbstract {
		LicheCislo() {}
		@Override
		public final void parseObject(final XXNode xnode,
			final XDParseResult p) {
			StringParser parser = new StringParser(p.getSourceBuffer());
			parser.isSpaces();
			if (parser.isInteger()) {
				long x = parser.getParsedLong();
				if ((x & 1) == 0) {
					p.error("CHYBA001", "Cislo neni liche");
				} else {
					p.setParsedValue(new DefLong(x));
				}
				parser.isSpaces();
				p.setIndex(parser.getIndex());
			} else {
				p.error(XDEF.XDEF515); // Value error
			}
		}
		@Override
		public final String parserName() {return "licheCislo";}
	}
	public final static XDParser licheCislo() {return new LicheCislo();}
	public final static boolean x(final XXData x) {return true;}

	/** Run test
	 * @param args the command line arguments
	 */
	public final static void main(final String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}