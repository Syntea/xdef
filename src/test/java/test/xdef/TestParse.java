package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
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
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.TimeZone;
import org.w3c.dom.Element;
import java.io.File;
import java.util.Properties;
import org.xdef.XDParserAbstract;
import org.xdef.proc.XXNode;
import org.xdef.proc.XXElement;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefParseResult;
import org.xdef.XDValueID;
import org.xdef.proc.XXData;
import org.xdef.sys.SUtils;

/** Test of parsing of source XML according to XDefinition.
 * @author Vaclav Trojan
 */
public final class TestParse extends XDTester {

	public TestParse() {super();}

	private static int _myX;

	@Override
	public void test() {
		final String dataDir = getDataDir() + "test/";
		XDPool xp;
		String xdef;
		String xml;
		String s;
		ArrayReporter reporter = new ArrayReporter();
		XDDocument xd;
		Element el;
		Report rep;
		XDOutput out;
		StringWriter strw;
		String tempDir = getTempDir();
		_myX = 1;
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
		} catch (Exception ex) {
			assertTrue(ex.getMessage().indexOf("XDEF903") > 0, ex);
		}
		try {//no source
			XDFactory.compileXD(null, "myxxx.xdef");
			fail("Error not reported");
		} catch (Exception ex) {
			assertTrue(ex.getMessage().indexOf("XDEF903") > 0, ex);
		}
		try { // compile InputStream, String and more
			xdef =
"<x:def xmlns:x ='" + _xdNS + "' name='a' root='a'>\n"+
"<a/>\n"+
"</x:def>";
			Object[][] params = new Object[1][2];
			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			xp = XDFactory.compileXD(new Properties(), (Object[]) params);
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrors(reporter);

			params = new Object[2][2];
			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			params[1][0] = new ByteArrayInputStream((
"<x:def xmlns:x ='" + _xdNS + "' name='b' root='b'>\n"+
"<b/>\n"+
"</x:def>").getBytes("UTF-8"));
			params[1][1] = "Osoba2 xdef";
			xp = XDFactory.compileXD(new Properties(), (Object[]) params);
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<b/>", parse(xp, "b", "<b/>", reporter));
			assertNoErrors(reporter);
			params = new Object[2][2];

			params[0][0] = new ByteArrayInputStream(xdef.getBytes("UTF-8"));
			params[0][1] = "Osoba xdef";
			params[1][0] = new ByteArrayInputStream((
"<x:def xmlns:x ='" + _xdNS + "' name='b' root='b'>\n"+
"<b/>\n"+
"</x:def>").getBytes("UTF-8"));
			params[1][1] = "Osoba2 xdef";
			xp = XDFactory.compileXD(new Properties(), (Object[]) params,
"<x:def xmlns:x ='" + _xdNS + "' name='c' root='c'>\n"+
"<c/>\n"+
"</x:def>");
			assertEq("<a/>", parse(xp, "a", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<b/>", parse(xp, "b", "<b/>", reporter));
			assertNoErrors(reporter);
			assertEq("<c/>", parse(xp, "c", "<c/>", reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			compile(// check semicolon tolerance
"<x:def xmlns:x ='" + _xdNS + "' root='a'>\n"+
"<a a=\"required eq('a'); onTrue {outln('a');}; finally {outln('b');}; \">\n"+
"</a>\n"+
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
			xdef = // check the sequence of processing of attributes
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A a='onTrue out(@a)' b='onTrue out(@b)' c='onTrue out(@c)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xdef =
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A a='finally out(@a)' b='finally out(@b)' c='finally out(@c)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xdef =
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A a='init out(@a)' b='init out(@b)' c='init out(@c)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xdef = // check the sequence of processed attribute
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A a='onStartElement out(@a)'\n" +
"     b='onStartElement out(@b)'\n" +
"     c='onStartElement out(@c)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
			xdef = // check the sequence of processing of attributes
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A b='onTrue out(@b)' c='onTrue out(@c)' a='onTrue out(@a)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xdef =
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A b='finally out(@b)' c='finally out(@c)' a='finally out(@a)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xdef = // check the sequence of processed attribute
"<xd:def xmlns:xd=\"" + _xdNS + "\" xd:root=\"A\">\n" +
"  <A b='onStartElement out(@b)'\n" +
"     c='onStartElement out(@c)'\n" +
"     a='onStartElement out(@a)' />\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A a='a' b='b' c='c' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A b='b' c='c' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xml = "<A c='c' b='b' a='a' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("bca", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A' xmlns:a='a.a' >\n" +
"  <A a:b='finally out(@a:a)'\n" +
"     a:a='finally out(@a:b)'\n" +
"     a:c='finally out(@a:c)'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A xmlns:x='a.a' x:c='c' x:a='a' x:b='b' />";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("abc", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // check in the onIllegalRoot
"<x:def xmlns:x ='" + _xdNS + "' root ='a'"+
"  x:script=\"onIllegalRoot {clearReports(); error('OK');}\">\n"+
"<a a = \"required eq('a')\"/>\n"+
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
		try { // recursive reference
			xdef =
"<xd:def  xmlns:xd='http://www.syntea.cz/xdef/2.0' root='A' name='Y21'>\n"+
"  <A>\n"+
"    <B b='? string()'>\n"+
"		<B xd:script='*; ref A/B'/>\n"+
"	</B>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B b='1'><B b='2'><B b='3'/></B><B b='4'/></B></A>";
			assertEq(xml, parse(xp, "", xml , reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def  xmlns:xd='http://www.syntea.cz/xdef/2.0' root='A'>\n"+
"  <A>\n"+
"    <B xd:script='0..1; ref Y'/>\n"+
"  </A>\n"+
"  <Y b='? string()' >\n"+
"    <Y xd:script='*; ref Y'/>\n"+
"  </Y>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B b='1'><Y b='2'><Y b='3'/></Y><Y b='4'/></B></A>";
			assertEq(xml, parse(xp, "", xml , reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"<a xd:script = \"\n"+
"       finally {if (!testExt('a','b', 'c'))\n"+
"                throw new Exception('Error!');}\">\n"+
"</a>\n"+
"</xd:def>";
			parse(compile(xdef, getClass()), "a", "<a/>", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(_myX, 0);
			//no namespace prefix fro XDefinition
			xdef =
"<def xmlns='" + _xdNS + "'\n"+
"        xmlns:n='a.b' root='n:a'>\n"+
"  <n:a xmlns:n='a.b'>\n"+
"    <mixed>\n"+
"      <n:x/>\n"+
"      <n:y/>\n"+
"    </mixed>\n"+
"  </n:a>\n"+
"</def>";
			xp = compile(xdef);
			xml = "<n:a xmlns:n='a.b'><n:x/><n:y/></n:a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a xmlns='a.b'><x/><y/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =  // test Contex constructor and conversion to element
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script=\"+; finally {\n"+
" Container c = [%a:x=[%xmlns:a='a',[%a:y=[]],[%y=null],'t',[%z=[%a='a']]]];\n"+
"  returnElement(c.toElement().toContainer().toElement());}\"/>\n"+
"</xd:def>";
			assertEq(parse(xdef, null, "<a/>", reporter),
				"<a:x xmlns:a='a'><a:y xmlns:a='a'/><y/>t<z a='a'/></a:x>");
			xdef = //test of exception in external method.
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>" +
"    <a xd:script='finally test.xdef.TestParse.myError()' />" +
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
		try { //test of exception in external method.
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>" +
"<xd:declaration>\n"+
	"external method long test.xdef.TestParse.myError();\n"+
	"int i = myError();\n"+
"</xd:declaration>\n"+
"    <a/>" +
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
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A a=''/>\n"+
"</xd:def>";
			xml = "<A a='a'></A>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xdef, "", "A", reporter, xml));
			assertNoErrorwarnings(reporter);
			//Test references to noname XDefinifion
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"  <A xd:script=\"create [%a='a','b'].toElement()\" a='string'> string </A>\n"+
"</xd:def>\n"+
"<xd:def name='X' root = \"#A\">\n"+
"  <B xd:script=\"ref #A\"/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml = "<A a='a'>b</A>";
			assertEq(xml, parse(xdef, "X", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<B a='a'>b</B>", create(xdef, "X", "B", reporter, null));
			assertNoErrorwarnings(reporter);
			xdef = //check type expression
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<A a=\"(eq('abc') | eq('xyz')) AAND string(2, 50);\"/>\n"+
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
"<a a=\"int; onFalse setText((String)null); onAbsence error('X1','x')\">\n"+
"  int(); onFalse setText((String)null); onAbsence error('X2','x')\n"+
"</a>\n"+
"</xd:def>";
			assertEq("<a/>", parse(xdef, null, "<a a='c'>d</a>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF809") >= 0  //incorrect value of int ???
				&& s.indexOf("xpath=/a/text()") >= 0 // ???
				&& s.indexOf("XDEF527") >= 0 // missing text ???
				&& s.indexOf("X1") < 0
				&& s.indexOf("X2") < 0, s);
			assertEq("<a/>", parse(xdef, null, "<a/>", reporter));
			s = reporter.printToString();
			assertTrue(s.indexOf("XDEF526") >= 0  //missing attribute
				&& s.indexOf("xpath=/a/text()") >= 0
				&& s.indexOf("XDEF527") < 0 // missing text should not be!!!
				&& s.indexOf("xpath=/a/@a") >= 0
				&& s.indexOf("X1") >= 0
				&& s.indexOf("X2") >= 0, s);
			xml = "<a a='1'>1</a>";
			assertEq(xml, parse(xdef, null, xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = //list, listi, tokens, tokensi
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"enum('abc', 'defg')\">\n"+
"    <x xd:script='occurs *'>enum('abc', 'defg')</x>\n"+
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
"  <a a=\"enum(x, y)\">\n"+
"    <x xd:script='occurs *'>enum(x, y)</x>\n"+
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
"  <a a=\"enumi(x, y)\">\n"+
"    <x xd:script='occurs *'>enumi(x, y)</x>\n"+
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
"  <a a=\"tokens('abc|defg')\">\n"+
"    <x xd:script='occurs *'>tokens('abc|defg')</x>\n"+
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
"  <xd:declaration> String x='abc|defg'; </xd:declaration>\n"+
"  <a a=\"tokens(x)\">\n"+
"    <x xd:script='occurs *'>tokens(x)</x>\n"+
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
"  <a a=\"tokensi('aBc|DeF')\">\n"+
"    <x xd:script='occurs *'>tokensi('aBc|DeFg')</x>\n"+
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
"  <a xd:text='occurs 2 string(1); finally s += getText() + 3;'>\n"+
"    <b/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<a>a<b/>b</a>";
			assertEq(xml, parse(xd, xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("a3b3", xd.getVariable("s").toString());
			xdef = // xd:text attribute
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<a xd:text=\"occurs 2..3 int();\"><b/><c/><d/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b/><c/><d/></a>"; // error XDEF527
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF527"),
				reporter.toString());
			xml = "<a>1<b/><c/><d/></a>"; // error XDEF527
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF527"),
				reporter.toString());
			xml = "<a><b/>1<c/>2<d/></a>";  // OK
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><b/>1<c/>2<d/>3</a>";  // OK
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a>1<b/>2<c/>3<d/>4</a>"; // error XDEF533
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF533"),
				reporter.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/><b/>1</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/><b/>2</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/>1<b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/>2/>3</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a>1<b/>2<b/>3<b/>4</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef = 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef = 
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq("<a>1</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/></a>";
			assertEq("<a>1<b/>2</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b/><b/></a>";
			assertEq("<a>1<b/>2<b/>3</a>", create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <B xd:text=\"string()\"><c/><d/></B>\n" +
"  <a>\n" +
"    <b xd:script=\"ref B\" xd:text=\"3 string()\"/>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b>t1<c/>t2<d/>t3</b></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			assertEq(xml, create(xp, "", "a", reporter, xml));
			assertNoErrors(reporter);
			xml = "<a><b><c/><d/>t</b></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xdef = // xd:textcontent attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String s = ''; </xd:declaration>\n"+
"  <a xd:textcontent='string(2); finally s += getText() + 3;'>\n"+
"    <b/>\n"+
"  </a>\n"+
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
"   <A " + (i == 0 ? "xd:text='*" : "xd:textcontent='?") + " string()'>\n" +
"     <B xd:script='*'/>\n" +
"   </A>\n" +
"</xd:def>";
				xp = XDFactory.compileXD(null, xdef);
				xml = "<A/>";
				assertEq(xml, parse(xp, "", xml, reporter));
				xml = "<A>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				xml = "<A><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				xml = "<A>test<B/></A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				xml = "<A><B/><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
				xml = "<A>test<B/><B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
				xml = "<A><B/>test<B/></A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
				xml = "<A>test<B/>test<B/>test</A>";
				assertEq(xml, parse(xp, "", xml, reporter));
				assertNoErrors(reporter);
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> String x='aBc|DeFg'; </xd:declaration>\n"+
"  <a a=\"tokensi(x)\">\n"+
"    <x xd:script='occurs *'>tokensi(x)</x>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='ABC'><x>abc</x><x>defg</x></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='xx'><x>xxx</x><x>xxxx</x></a>", reporter);
			assertErrors(reporter);
			xdef = // test format, printf
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> Locale loc = new Locale('cs', 'CZ'); </xd:declaration>\n"+
"<a xd:script=\"finally {String s = '%d,%2.1f';\n"+
"  printf(s, 1, 1.1); s = '; ' + s;\n"+
"  out(format(s,2,2.2)); printf(loc,s,1,1.1); out(format(loc,s,2,2.2));\n"+
"  printf($stdOut, s, 1, 1.1); $stdOut.out(format(s, 2, 2.2));\n"+
"    printf($stdOut,loc,s,1,1.1); $stdOut.out(format(loc,s,2,2.2));}\"/>\n"+
"</xd:def>";
			strw = new StringWriter();
			parse(xdef, "", "<a/>", reporter, strw, null, null);
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"1,1.1; 2,2.2; 1,1,1; 2,2,2; 1,1.1; 2,2.2; 1,1,1; 2,2,2");
			xdef = // "$" identifiers, miscellaneous
"<xd:collection xmlns:xd='" + XDConstants.XDEF20_NS_URI + "'>\n"+
"<xd:def xd:classes = 'test.xdef.TestParse' impl-version = '2.0'\n"+
"   name='abc' script='options trimText' root='Davka'>\n"+
"<xd:declaration>\n"+
"  final String myversion = getImplProperty('version');\n"+
"  final String reg = '[A-Z][A-Z]B';\n"+
"  final String min = '987654320';\n"+
"  final String max = '987654321';\n"+
"</xd:declaration>\n"+
"<xd:declaration>\n"+
"     int myParseInt(String s){\n"+
"       final int result = parseInt(s);\n"+
"       return result;\n"+
"     }\n"+
"     boolean myCheck0(String s) {\n"+
"       switch(s) {\n"+
"         case 'MD5': return true;\n"+
"         case 'CRC': return true;\n"+
"       }\n"+
"       return error('Chybný typ: ' + s);\n"+
"     }\n"+
"     boolean myCheckInt() {\n"+
"       try {\n"+
"         if (int()) return true;\n"+
"		  else \n"+
"           throw new Exception('vyjimka');\n"+
"       } catch (Exception ex) {\n"+
"          if (getText() EQ 'a123') return true;\n"+
"         throw new Exception('vyjimka1');\n"+
"       }\n"+
"     }\n"+
"     boolean $myCheck(){return myCheck0(getText());}\n"+
"</xd:declaration>\n"+
"<Davka Verze      = \"fixed myversion\"\n"+
"       Kanal      = \"required regex(reg)\"\n"+
"       Seq        = \"required int(myParseInt(min), myParseInt(max))\"\n"+
"       SeqRef     = \"optional myCheckInt()\"\n"+
"       Date       = \"required xdatetime('d.M.yyyy')\"\n"+
"       dp0        = \"required dec\"\n"+
"       dp1        = \"required dec(3)\"\n"+
"       dp2        = \"required dec(5,1)\"\n"+
"       xd:attr    = \"optional\"\n"+
"       xd:script  = \"finally myProc(1,0.5,'xxx')\">\n"+
"   <File\n"+
"           Name       = \"required string(1,256)\"\n"+
"           Format     = \"required tokens('TXT|XML|CTL')\"\n"+
"           Kind       = \"required string(3,3)&amp;(eq('abc')|eq('xyz'))\"\n"+
"           RecNum     = \"required num(8)\"\n"+
"           xd:script= \"occurs 1..\">\n"+
"     <xd:mixed>\n"+
"       <CheckSum Type       = \"required $myCheck()\"\n"+
"                Value       = \"required string()\"\n"+
"                xd:script = \"occurs 1\">\n"+
"         optional string()\n"+
"       </CheckSum>\n"+
"       <x xd:script = \"occurs 1..5; ref empty.node\" />\n"+
"     </xd:mixed>\n"+
"   </File>\n"+
"   <xd:choice>\n"+
"	    <Osoba xd:script = \"occurs 1..1; ref Osoba\" />\n"+
"		 <OSVC xd:script = \"occurs 1..1; ref OSVC\" />\n"+
"		<Organizace xd:script = \"occurs 1..2; ref Organizace\" />\n"+
"   </xd:choice>\n"+
"   fixed 'ahoj'\n"+
"   <y xd:script=\"ref y\" />\n"+
"   <z xd:script=\"ref log\" />\n"+
"</Davka>\n"+
"<Osoba jmeno=\"required string()\"/>\n"+
"<OSVC nazev=\"required string()\"/>\n"+
"<Organizace adresa=\"required string()\"/>\n"+
"<empty.node/>\n"+
"<qwert xd:script=\"ref y\" />\n"+
"<y xd:script=\"ref z\" />\n"+
"<z><fff attr=\"optional\"/></z>\n"+
"<zz attr=\"required\"/>\n"+
"<log bttr=\"required\"> </log>\n"+
"</xd:def>\n"+
"</xd:collection>";
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
"       <CheckSum Type=\"MD5\" Value=\"0xfadb8701a\">\n"+
"           text\n"+
"       </CheckSum>\n"+
"       <x/>\n"+
"   </File>\n"+
"   <Organizace adresa=\"ulice1\" />\n"+
"   <Organizace adresa=\"ulice2\" />\n"+
"ahoj\n"+
"   <y><fff attr=\"attr\"/></y> \n"+
"   <z bttr=\"bttr\"/>\n"+
"</Davka>";
			el = parse(compile(xdef, getClass()), "abc", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("2.0", el.getAttribute("Verze"));
			assertEq(_myX, 3);
			//test direct expression as type check
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a|b|c|d'>\n"+
"  <xd:declaration> boolean x() { return !eq('hi'); } </xd:declaration>\n"+
"  <a p=\"'hi';\"/>\n"+
"  <b p=\"!eq('hi');\"/>\n"+
"  <c p=\"true;\"/>\n"+
"  <d p=\"x\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a p='hi'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<b p='xx'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<b p='hi'/>", reporter);
			assertTrue(reporter.errorWarnings());
			parse(xp, "", "<c p='xxx'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<d p='xx'/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<d p='hi'/>", reporter);
			assertTrue(reporter.errorWarnings());
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a Ver     =\"fixed '1.2'\"\n"+
"   Class   =''\n"+
"   Recurr  ='?'\n"+
"   x:script=\"finally {myCheck('abc',\n"+
"      parseBase64('abc'.getBytes().toBase64()));}\" >\n"+
"   <x:any x:attr='?' x:script='*; options moreElements, moreText'/>\n"+
"</a>\n"+
"</x:def>";
			xp = compile(xdef, getClass());
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
"<a date=\"required dateToLocal('yyyyMMddTHHmmssZ','yyyyMMddTHHmmssZ')\"/>"+
"</xd:def>";
			xml = "<a date='20080225T000000Z'/>";
			xp = compile(xdef, getClass());
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
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
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = //check typ of fixed value
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a v=\"float; fixed '2.0'\"/>\n"+
"</xd:def>";
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a v='2.0'/>", reporter));
			assertNoErrors(reporter);
			assertEq("<a v='2.0'/>", parse(xdef, "", "<a v='20'/>", reporter));
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {//ignoreEmptyAttributes
			xdef = //errors
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"required enum('A','B','C'); onFalse {out('error');}\n"+
"       onAbsence {out('missing');}\n"+
"       options ignoreEmptyAttributes,trimAttr\"/>\n"+
"</xd:def>";
			strw = new StringWriter();
			xml = "<a a = ' '/>";
			parse(xdef, "", xml, reporter, strw, null, null);
			assertTrue(reporter.errorWarnings() &&
				"XDEF526".equals(reporter.getReport().getMsgID()));
			assertEq("missing", strw.toString());
			xdef = //errors cleared
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"required enum('A','B','C'); onFalse {out('error');clearReports();}\n"+
"       onAbsence {out('missing'); clearReports();}\n"+
"       options ignoreEmptyAttributes,trimAttr\"/>\n"+
"</xd:def>";
			strw = new StringWriter();
			parse(xdef, "", "<a a=' '/>", reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("missing", strw.toString());
			xdef = //setAttr
"<xd:def xmlns:xd='" + _xdNS + "' root='test'>\n"+
"<test a = \"int; onTrue setAttr('a', '2');\"/>\n"+
"</xd:def>";
			xml = "<test a = '1'/>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			s = el.getAttribute("a");
			assertTrue("2".equals(s), "a = " + s);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test'>\n"+
"<test a = \"int;\" xd:script=\"finally setAttr('a', '2');\"/>\n"+
"</xd:def>";
			el = parse(xdef, "", xml, null);
			s = el.getAttribute("a");
			assertTrue("2".equals(s), "a = " + s);
			xdef = // check in the onIllegalRoot
"<x:def xmlns:x ='" + _xdNS + "' root='a'"+
"       x:script=\"onIllegalRoot throw new Exception('OK')\">\n"+
"<a a = \"required eq('a')\"/>\n"+
"</x:def>";
			xml = "<b a = 'b' />";
			parse(xdef, "", xml, reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			if (ex.getMessage().indexOf("OK") < 0 ||
				ex.getMessage().indexOf("XDEF905") < 0) {
				fail(ex);
			}
		}
		try {
			xdef = // add child nodes to refered element
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a' script='options ignoreEmptyAttributes'>\n"+
"<a xd:script = 'ref b'>\n"+
"  <p/>\n"+
"  <q/>\n"+
"  required int()\n"+
"</a>\n"+
"<b attr=\"required an()\">\n"+
"  <c/>\n"+
"</b>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xml = "<a attr='a1'><c/><p/><q/>123</a>";
			parse(xdef, "", xml, reporter);
			assertNoErrors(reporter);
			//int type equals
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a><int>int(10_000)</int></a>\n"+
"</x:def>";
			xml = "<a><int>10000</int></a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			//test xd:text as attribute
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root xd:text=\"required string()\">\n"+
"  <xd:choice>\n"+
"    <a xd:script=\"occurs 0..2\" />\n"+
"    <b xd:script=\"occurs 0..2\" />\n"+
"  </xd:choice>\n"+
"</root>\n"+
"</xd:def>");
			xml = "<root><a/></root>";
			el = parse(xp, "", xml, reporter);
			if (reporter.getErrorCount() != 1 ||
				!"XDEF527".equals(reporter.getReport().getMsgID())) {
				fail(reporter.printToString());
			}
			assertEq(xml,el);
			xp = compile("<xd:def root ='root' xmlns:xd='" + _xdNS + "'>\n"+
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
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        xd:script= 'options ignoreEmptyAttributes'>\n"+
"<a>\n"+
"  <xd:mixed>\n"+
"    <p xd:script = \"occurs 1..\" />\n"+
"    <xd:mixed>\n"+
"      <q xd:script = \"occurs 1\" />\n"+
"      <r xd:script = \"occurs 1\" />\n"+
"    </xd:mixed>\n"+
"  </xd:mixed>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><r/><q/><p/></a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><p/><q/><r/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><p/><r/><q/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a><p/><q/><r/></a>";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xdef =  // Test fixed from a field
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def>\n"+
"<xd:declaration>\n"+
"   String verze = '1.23';\n"+
"   String x = '???';\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name='a' root='root'>\n"+
"<root Verze=\"float; fixed verze\"\n"+
"      arg =\"required string(); onTrue x=getText().substring(3)\">\n"+
"    <a aa=\"required eq(x);\" />\n"+
"</root>\n"+
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
			//Authomatic conversion of ParseResult to boolean
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"  boolean myCheck() {boolean b = tokens('A|B|C'); return b;}\n"+
"</xd:declaration>\n"+
"<A a=\"?{boolean b = tokens('A|B|C'); return b;}\" b = 'myCheck'/>\n"+
"</xd:def>";
			xp = compile(xdef);
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
			// test match section
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:sequence>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:sequence>\n"+
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:choice>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:choice>\n"+
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b x='1'/><b x='1'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			// test match
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"    <xd:mixed>\n"+
"       <b xd:script='occurs 1..2;match getOccurrence() LT 2;' x='fixed 1'/>\n"+
"       <b xd:script='occurs 0..2;' y='fixed 2'/>\n" +
"    </xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b x='1'/><b x='1'/><b y='2'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n" +
"<xd:mixed>\n"+
"    match getOccurrence() == 0; ? string(); \n" +
"    <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"    match getOccurrence() == 0; string(); \n" +
"</xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external int typ;\n"+ // 1, 2
"</xd:declaration>\n"+
" \n"+
"<a>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match(typ == 1); create typ == 1 ? from('b') : null;\"\n"+
"       b=\"string\"\n"+
"      Text=\"create 'text'\" />\n"+
"    <b xd:script=\"match(typ == 2); create typ==2 ? from('b') : null;\"\n"+
"       c=\"create 'z'\"\n"+
"      Kod=\"create 'kod'\" />\n"+
"  </xd:choice>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("typ", 1);
			xml = "<a><b Text='x' b='b'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			el = create(xd, "a", reporter, xml);
			assertNoErrors(reporter);
			assertEq("<a><b Text='text' b='b'/></a>", el);
			xml = "<a><b Kod='x' c='c'/></a>";
			el = parse(xd, xml, reporter);
			assertErrors(reporter);
			assertEq("<a><b/></a>", el);
			xd = xp.createXDDocument();
			xd.setVariable("typ", 2);
			xml = "<a><b Kod='x' c='c'/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			el = create(xd, "a", reporter, xml);
			assertNoErrors(reporter);
			assertEq("<a><b Kod='kod' c='z'/></a>", el);
			xml = "<a><b Text='x' b='b'/></a>";
			el = parse(xd, xml, reporter);
			assertErrors(reporter);
			assertEq("<a><b/></a>", el);
			String tempFile = tempDir + "vystup.txt";
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='xxx'>\n"+
"<xd:declaration>\n"+
"   String child = 'CHILD';\n"+
"   String verze = '2.0';\n"+
"   Output vystup = new Output('" + tempFile + "');\n"+
"   Input vstup = new Input('" + tempFile + "');\n"+
"   Output err = new Output('#System.err');\n"+
"   int count = 0;\n"+
"</xd:declaration>\n"+
"<xd:declaration>\n"+
"  void testparams(String i, int j, Datetime k) {\n"+
"    outln('testparams ' + i + ', ' + j + ', ' + k);\n"+
"  }\n"+
"\n"+
"  String myStringa() { return 'myString'; }\n"+
"  String myString(String p) {return myStringa() + ' ' + p;}\n"+
"  String myString() { return myString('xxx'); }\n"+
"  boolean myCheckInt() {\n"+
"      try {\n"+
"        if (int()) {\n"+
"          return true;\n"+
"		   } else {\n"+
"          throw new Exception('vyjimka');\n"+
"        }\n"+
"      } catch (Exception ex) {\n"+
"        throw new Exception('vyjimka1');\n"+
"      }\n"+
"  }\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='abc' root='root | *'>\n"+
"   <xd:declaration>\n"+
"     void myOut() {vystup.outln(child);}\n"+
"     boolean myCheck(){return tokens('A|B|C');}\n"+
"   </xd:declaration>\n"+
"<root Verze=\"fixed verze\"\n"+
"       PlatnostOd=\"optional xdatetime('d.M.yyyy H:mm');\n"+
"                   onAbsence setText('11');\n"+
"                   finally {outln(myString()); outln((String)@Kanal);}\"\n"+
"       Kanal=\"required {try {return myCheckInt();} catch(Exception ex)\n"+
"               {return error('vyjimka2');}}\"\n"+
"       Seq=\"required myCheck()\"\n"+
"       SeqRef=\"optional 'xyz'\"\n"+
"       Date=\"required xdatetime('d.M.y')\"\n"+
"       xd:attr=\"occurs 1.. int();\n"+
"                   onTrue outln('&lt;' + getElementName() + ' '\n"+
"                       + getAttrName() + '=\\'' + getText()+ '\\'/&gt;' );\n"+
"                   finally testparams('a',1, \n"+
"                           parseDate('1999-5-1T20:43:09+01:00'));\"\n"+
"       xd:script= \"match (@Kanal == '123') AND @SeqRef\">\n"+
"  <child xd:script=\"occurs 1..2; ref abc#child\"/>\n"+
"  <xd:list ref = \"sq\" />\n"+
"  <xd:choice occurs=\"?\">\n"+
"    <a xd:script=\"occurs 0..2\"/>\n"+
"    <b xd:script=\"occurs 0..2\"/>\n"+
"  </xd:choice>\n"+
"  <end xd:script=\"occurs 0..\"/>\n"+
"</root>\n"+
"<child xd:script=\"finally {myOut();}\">\n"+
"  optional string(1,100);\n"+
"             onTrue setText('text');\n"+
"             onFalse setText('empty');\n"+
"             onAbsence setText('absence')\n"+
"</child>\n"+
"\n"+
"<xd:list name='sq'>\n"+
"    <x xd:script=\"occurs 0..2; \"/>\n"+
"    <y xd:script=\"occurs 1\"/>\n"+
"    required\n"+
"</xd:list>\n"+
"\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml =
" <root\n"+
"       Verze=\"2.0\"\n"+
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
			strw = new StringWriter();
			el = parse(xp, "abc", xml, reporter, strw, null, null);
			assertEq("<root any='12000'/>\n"+
				"myString xxx\n123\ntestparams a, 1, 1999-05-01T20:43:09+01:00\n",
				strw.toString());
			assertNoErrors(reporter);
			assertEq("2.0", el.getAttribute("Verze"));
			FileReader fr = new FileReader(tempFile);
			FileReportReader frep = new FileReportReader(fr, true);
			Report r;
			StringBuffer sb = new StringBuffer();
			while ((r = frep.getReport()) != null) {
				s = r.toString();
				if (" ".equals(s)) {
					sb.append('\n');
				} else {
					sb.append(s);
				}
			}
			frep.close();
			fr.close();
			assertEq("CHILD\nCHILD\n", sb.toString());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name=\"a\">\n"+
"<xd:declaration>\n"+
"   String child = 'CHILD';\n"+
"   String verze = '2.0';\n"+
"   Output vystup = new Output('" + tempFile + "','',false);\n"+
"   Input vstup = new Input('" + tempFile + "');\n"+
"   Output err = new Output('#System.err');\n"+
"   int count = 0;\n"+
"</xd:declaration>\n"+
"<xd:declaration>\n"+
"  void testparams(String i, int j, String s) {\n"+
"    outln('testparams ' + i + ', ' + j + ', ' + s);\n"+
"  }\n"+
"\n"+
"  String myStringa() { return 'myString'; }\n"+
"  String myString(String p) {return myStringa() + ' ' + p;}\n"+
"  String myString() { return myString('xxx'); }\n"+
"  boolean myCheckInt() {\n"+
"      try {\n"+
"        if (int()) {\n"+
"          return true;\n"+
"		 } else {\n"+
"          throw new Exception('vyjimka');\n"+
"        }\n"+
"      } catch (Exception ex) {\n"+
"        throw new Exception('vyjimka1');\n"+
"      }\n"+
"  }\n"+
"</xd:declaration>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name = \"abc\"\n"+
"          root = \"root | *\">\n"+
"   <xd:declaration>\n"+
"     void myOut() {vystup.outln(child);}\n"+
"     boolean myCheck(){return tokens('A|B|C');}\n"+
"   </xd:declaration>\n"+
"<root Verze=\"fixed verze\"\n"+
"       PlatnostOd=\"optional xdatetime('d.M.yyyy H:mm');\n"+
"                   onAbsence setText('11');\n"+
"                   finally {outln(myString()); outln((String)@Kanal);}\"\n"+
"       Kanal=\"required {try {return myCheckInt();} catch(Exception ex)\n"+
"               {return error('vyjimka2');}}\"\n"+
"       Seq=\"required myCheck()\"\n"+
"       SeqRef=\"optional 'xyz'\"\n"+
"       Date=\"required xdatetime('d.M.y')\"\n"+
"       xd:attr=\"occurs 1.. int();\n"+
"                   onTrue outln('&lt;' + getElementName() + ' '\n"+
"                       + getAttrName() + '=\\'' + getText()+ '\\'/&gt;' );\n"+
"                   finally testparams('a',1, getAttrName());\"\n"+
"       xd:script= \"match (@Kanal == '123') AND @SeqRef\">\n"+
"  <child xd:script=\"occurs 1..2; ref abc#child\"/>\n"+
"  <xd:choice occurs= \"?\">\n"+
"    <a xd:script=\"occurs 0..2\"/>\n"+
"    <b xd:script=\"occurs 0..2\"/>\n"+
"  </xd:choice>\n"+
"  <end xd:script=\"occurs 0..\"/>\n"+
"</root>\n"+
"<child xd:script=\"finally {myOut();}\">\n"+
"  optional string(1,100);\n"+
"             onTrue setText('text');\n"+
"             onFalse setText('empty');\n"+
"             onAbsence setText('absence')\n"+
"</child>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml =
" <root\n"+
"       Verze=\"2.0\"\n"+
"       PlatnostOd=\"1.1.2000 20:00\"\n"+
"       Kanal=\"123\"\n"+
"       Seq=\"C\"\n"+
"       EwqRef=\"1\"\n"+
"       SeqRef=\"xyz\"\n"+
"       any=\"12000\"\n"+
"       Date=\"1.1.2000\">\n"+
"   <child>\n"+
"      toto je text...\n"+
"   </child>\n"+
"   <child/>\n"+
"   <a/>\n"+
"   <end/>\n"+
" </root>";
			strw = new StringWriter();
			parse(xp, "abc", xml, null, strw, null, null);
			assertEq("<root EwqRef='1'/>\n<root any='12000'/>\n"+
				"myString xxx\n123\ntestparams a, 1, EwqRef\n" +
				"testparams a, 1, any\n",
				strw.toString());
			BufferedReader br = new BufferedReader(new FileReader(tempFile));
			sb = new StringBuffer();
			while ((s = br.readLine()) != null) {
				sb.append(s);
				sb.append('\n');
			}
			br.close();
			assertEq("CHILD\nCHILD\n", sb.toString());
			if (getFailCount() == 0) {
				try {
					FUtils.deleteAll(tempDir, true);
				} catch (Exception ex) {fail(ex);}
			}
			xdef =
"<!DOCTYPE xd:def [\n"+
"  <!ENTITY jmeno \"required string(5,30)\">\n"+
"  <!ENTITY plat \"optional int(1000,99999)\">\n"+
"  <!ENTITY v \"fixed '20'\">\n"+
"]>\n"+
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='osoba'>\n"+
"<xd:declaration>\n"+
"    String data = 'Pan reditel';\n"+
"    String x = 'nobody';\n"+
"</xd:declaration>\n"+
"<osoba funkce = \"required\" jmeno = \"&jmeno;\" plat = \"&plat;\"\n"+
"       v = \"&v;\">\n"+
"  required eq(data); onFalse {clearReports(); \n"+
"           setText(x);} onAbsence setText(x);\n"+
"  <podrizeny jmeno = \"required\" xd:script=\"occurs 0..2\"/>\n"+
"</osoba>\n"+
"<AdresaCE KodOkresu       = \"optional num(4)\"\n"+
"       PSC             = \"required string(5); onAbsence setText('0')\"\n"+
"       Obec            = \"optional string(1,30)\"\n"+
"       Ulice           = \"optional string(1,30)\"\n"+
"       CisloOrientacni = \"required int(0,32767); onAbsence setText('0')\"\n"+
"       ZnakDomu        = \"optional string(1)\"\n"+
"       CisloPopisne    = \"required int(0,32767); onAbsence setText('0')\"\n"+
"       DruhCislaDomu   = \"required string(1); onAbsence setText('P')\" />\n"+
"</xd:def>";
			xp = compile(xdef);
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
				//Maximum occurrence limit exceeded
				assertEq("XDEF558", rep.getMsgID(), rep.toString());
			}
			if (xd == null) {
				fail("DOC IS NULL");
			} else if (
				!("boss".equals(xd.getElement().getAttribute("funkce")))) {
				fail("incorrect boss=\""
					+ xd.getElement().getAttribute("boss") + '"');
			} else if (!("20".equals(xd.getElement().getAttribute("v")))) {
				fail("incorrect v=\""
					+ xd.getElement().getAttribute("v") + '"');
			} else if (!("nobody".equals(
				xd.getElement().getChildNodes().item(0).getNodeValue()))) {
				fail("incorrect data: '"
					+ xd.getElement().getChildNodes().item(0) + "'");
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='messages'>\n"+
"<messages>\n"+
"  <xd:any xd:script=\"occurs 0..\"\n"+
"    ces='optional; onTrue $stdErr.outln(getElementName() + getText())'/>\n"+
"</messages>\n"+
"</xd:def>";
			xml =
"<messages>\n"+
"  <XDEF500 ces=\"&amp;{line}{; řádka=}&amp;{column}{;"
		+ " sloupec=}&amp;{sysId}{; zdroj='}{'}\"/>\n"+
"  <XDEF501 ces=\"Výskyt nepovoleného elementu '&amp;{child}'&amp;{xpath}{"
		+ " v }&amp;{#XDEF500}\"/>\n"+
"  <XDEF502 ces=\"Element '&amp;{child}' není definován jako 'root'{ v }"
		+ "&amp;{#XDEF500}\"/>\n"+
"</messages>";
			xp = compile(xdef);
			parse(xp, "a", xml, reporter);
			s = reporter.printToString().trim();
			assertTrue(s.indexOf("XDEF500&{line}{; řádka=}&{column}{;"
				+ " sloupec=}&{sysId}{; zdroj='}{'}") == 0 &&
				s.indexOf("XDEF501Výskyt nepovoleného elementu '&{child}"
				+ "'&{xpath}{ v }&{#XDEF500}") > 0 &&
				s.endsWith("XDEF502Element '&{child}' není definován jako "
				+ "'root'{ v }&{#XDEF500}"), s);
			//test macro
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='messages' root='messages' script='options ${mac#opt}'>\n"+
"<messages>\n"+
"  <xd:any xd:script = \"occurs 0..\"\n"+
"    ces = \"optional; onTrue outln(getElementName() + ' ' + getText())\"/>\n"+
"</messages>\n"+
"</xd:def>\n"+
"<xd:def name=\"mac\">\n"+
"<xd:macro name = \"opt\">setAttrUpperCase${mac#opt1}</xd:macro>\n"+
"<xd:macro name = \"opt1\">,setTextUpperCase</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xml =
"<messages>\n"+
"  <X00 ces=\"&amp;{line}{; řádka=}&amp;{column}{; sloupec=}&amp;{sysId}"+
"{; zdroj='}{'}\"/>\n"+
"  <X01 ces=\"Výskyt nepovoleného elementu '&amp;{child}'&amp;{xpath}"+
 "{ v }&amp;{#X00}\"/>\n"+
"  <X02 ces=\"Element '&amp;{child}' není definován jako 'root'"+
"{ v }&amp;{#X00}\"/>\n"+
"</messages>\n";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "messages", xml, null, strw, null, null);
			assertEq(
"X00 &{LINE}{; ŘÁDKA=}&{COLUMN}{; SLOUPEC=}&{SYSID}{; ZDROJ='}{'}\n"+
"X01 VÝSKYT NEPOVOLENÉHO ELEMENTU '&{CHILD}'&{XPATH}{ V }&{#X00}\n"+
"X02 ELEMENT '&{CHILD}' NENÍ DEFINOVÁN JAKO 'ROOT'{ V }&{#X00}\n",
				strw.toString());
			//test macro
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='macTest' root='macTest'>\n"+
"<macTest xd:script = \"occurs 0..${mac#m3(op = '${mac#m4}',"+
"         p1 = '\\'Ahoj\\'', p2 = 'Pane', p3 = 'Tepic', end= '')}\"/>\n"+
"</xd:def>\n"+
"<xd:def name = \"mac\">\n"+
"<xd:macro name = \"m1\">${mac#m2()}</xd:macro>\n"+
"<xd:macro name = \"m2\">out</xd:macro>\n"+
"<xd:macro name = \"m3\" op=\"${mac#m1}\" p1=\"'Hi'\" p2=\"Sir\"\n"+
"                 p3=\"Bye\" end=\"outln('End');\">\n"+
"; finally {#{op}(#{p1});#{op}('#{p2}');#{op}('#{p3}');#{end}}\n"+
"</xd:macro>\n"+
"<xd:macro name = \"m4\">out</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "macTest", "<macTest></macTest>\n",null,strw,null,null);
			assertEq("AhojPaneTepic", strw.toString());
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name='a' root='macTest'>\n"+
"<macTest xd:script = \"occurs 0..${mac#m3}\"/>\n"+
"</xd:def>\n"+
"<xd:def name = \"mac\">\n"+
"<xd:macro name = \"m1\">${mac#m2()}</xd:macro>\n"+
"<xd:macro name = \"m2\">out</xd:macro>\n"+
"<xd:macro name = \"m3\" op=\"${mac#m1}\" p1=\"'Hi'\" p2=\"Sir\""+
"                          p3=\"Bye\" end=\"outln('End');\">\n"+
"; finally {#{op}(#{p1});#{op}('#{p2}');#{op}('#{p3}');#{end}}\n"+
"</xd:macro>\n"+
"<xd:macro name = \"m4\">out</xd:macro>\n"+
"</xd:def>\n"+
"</xd:collection>\n";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("HiSirByeEnd\n", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='a'>\n"+
"<xd:macro name='m' p1='abc' p2='xyz'>\n"+
"    finally {out('#{p1}#{p2}');}\n"+
"</xd:macro>\n"+
"<a xd:script='${m()}' />\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "test", "<a/>", null, strw, null, null);
			assertEq("abcxyz", strw.toString());
			xdef =
"<xd:collection xmlns:xd = '" + _xdNS + "'>\n"+
"<xd:def name = 'mac'>\n"+
"  <xd:macro name='m' p1='abc' p2='xyz'>\n"+
"    finally {out('#{p1}#{p2}');}\n"+
"  </xd:macro>\n"+
"</xd:def>\n"+
"<xd:def name='test' root='a'>\n"+
"  <a xd:script=\"${mac#m(p2='DEF', p1 = 'ABC')}\" />\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "test", "<a/>", null, strw, null, null);
			assertEq("ABCDEF", strw.toString());
			xdef =
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script='finally ${text}; options trimText;'/>\n"+
"<xd:macro name='text'\n"+
"  >out('Volání makra text má tvar: \\u0024{text}')</xd:macro>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("Volání makra text má tvar: ${text}", strw.toString());
			xdef =
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script='finally ${m1}${m2}; options trimText;'/>\n"+
"<xd:macro name='m1'> </xd:macro>\n"+//one space
"<xd:macro name='m2'>out('m2')</xd:macro>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("m2", strw.toString());
			xdef =
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script='finally $$${m1}}; \noptions trimText;'/>\n"+
"<xd:macro name='m1'>{m2}</xd:macro>\n"+
"<xd:macro name='m2'>{m3</xd:macro>\n"+
"<xd:macro name='m3'>out\n(\n'm2')</xd:macro>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("m2", strw.toString());
			// macro $${m1} -> ${m2} -> out('m2')
			xdef =
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script='finally $${m1}; options trimText;'/>\n"+
"<xd:macro name='m1'>{m2}</xd:macro>\n"+
"<xd:macro name='m2'>out('m2')</xd:macro>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("m2", strw.toString());
			// macro empty replacement
			xdef =
"<xd:def name='a' root='macTest' xmlns:xd='" + _xdNS + "'>\n"+
"<macTest xd:script = \"finally out('${m1}'); options trimText;\"/>\n"+
"<xd:macro name = \"m1\">${m2}m1</xd:macro>\n"+ //m2 is empty
"<xd:macro name = \"m2\"></xd:macro>\n"+ //empty macro
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<macTest/>", null, strw, null, null);
			assertEq("m1", strw.toString());
			// macro in the declaration part
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n" +
"  <xd:macro name='a'>'aaa'</xd:macro>\n"+
"    String s = ${a};\n"+
"  </xd:declaration>\n" +
"  <a xd:script=\"finally out(${a}+s)\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", "<a/>", null, strw, null, null);
			assertEq("aaaaaa", strw.toString());
			xp = compile(new String[] {
"<xd:declaration xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:macro name='a'>'aaa'</xd:macro>\n"+
"  String s = ${a};\n"+
"</xd:declaration>",
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script=\"finally out(s + ${a})\"/>\n"+
"</xd:def>"});
			strw = new StringWriter();
			parse(xp, "", "<a/>", null, strw, null, null);
			assertEq("aaaaaa", strw.toString());

			xdef =
"<xd:def name='a' root='txt' xmlns:xd='" + _xdNS + "'>\n"+
"<txt xd:script = \"options trimText;\">\n"+
"required string(); onTrue out(getText())\n"+
"</txt>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<txt>data</txt>\n", null, strw, null, null);
			assertEq("data", strw.toString());
			xdef =
"<x:def xmlns:x = '" + _xdNS + "' name='a' root='txt'>\n"+
"  <txt x:script = \"options trimText;\">\n"+
"    <a x:script = \"occurs 0..2\" />\n"+
"    <b x:script = \"occurs 0..2\" />\n"+
"    required string(); onTrue out(getText())\n"+
"  </txt>\n"+
"</x:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<txt>data</txt>", null, strw, null, null);
			assertEq("data", strw.toString());
			strw = new StringWriter();
			parse(xp, "a", "<txt><a/><b/>data</txt>", null, strw, null, null);
			assertEq("data", strw.toString());
			strw = new StringWriter();
			try {
				parse(xp, "a", "<txt><a/><b/>data<c/></txt>", null,
					strw, null, null);
				fail("Exception not thrown");
			} catch (SRuntimeException ex) {
				if (ex.getMessage().indexOf("XDEF501") < 0) {
					fail("Incorrect exception");
					fail(ex);
				}
				strw.close();
			}
			assertEq("data", strw.toString());
			xdef =
"<x:def name='a' root='t' xmlns:x='" + _xdNS + "'>\n"+
"<t x:script = \"options trimText;\"\n"+
"   date = \"required xdatetime('d.M.yyyy');\n"+
"            onTrue setText(toString(getParsedDatetime(),'yyyyMMdd'));\"/>\n"+
"</x:def>";
			xp = compile(xdef);
			el = parse(xp, "a", "<t date=\"17.7.2003\"/>");
			s = el.getAttribute("date");
			assertEq("20030717", s);
			xdef =
"<x:def name='a' root='t' xmlns:x='" + _xdNS + "'>\n"+
"<t x:script = \"options trimText;\"\n"+
"   date = \"required xdatetime('d.M.yyyy','yyyyMMdd');\"/>\n"+
"</x:def>";
			xp = compile(xdef);
			el = parse(xp, "a", "<t date=\"17.7.2003\"/>");
			s = el.getAttribute("date");
			assertEq("20030717", s);
			// volani '@atr'
			xdef =
"<xd:def name='a' root='a' xmlns:xd = '" + _xdNS + "'>\n"+
"	<a s = \"optional int(1,99999999)\"\n"+
"	   m = \"optional string(); onTrue {if(!@s) setText('Error');}"+
"           onAbsence {if(@s) setText('CZK');}\"\n"+
"    xd:script= \"finally out(getAttr('m'))\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<a s='33'/>", null, strw, null, null);
			assertEq("CZK", strw.toString());
			strw = new StringWriter();
			parse(xp, "a", "<a m='xxx'/>", null, strw, null, null);
			assertEq("Error", strw.toString());
			strw = new StringWriter();
			parse(xp, "a", "<a/>", null, strw, null, null);
			assertEq("", strw.toString());
			// root *
			xdef =
"<xd:def name='a' root='a | b | *' xmlns:xd='" + _xdNS + "'>\n"+
"	<a s = \"optional int(1,99999999)\"\n"+
"	   m = \"optional string(); onTrue {if(!@s) setText('Error');}\n"+
"                             onAbsence {if(@s) setText('CZK');}\"\n"+
"    xd:script= \"finally out(getAttr('m'))\"/>\n"+
"	<b/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "a", "<a s='33'/>", null, strw, null, null);
			assertEq("CZK", strw.toString());
			xp = compile(xdef);
			strw = new StringWriter();
			el = parse(xp, "a", "<c x='a'/>", null, strw, null, null);
			assertEq("", strw.toString());
			if (!"c".equals(el.getNodeName())) {
				fail("Incorrect root name:\n'" + el.getNodeName() + "'");
			}
			if (!"a".equals(el.getAttribute("x"))) {
				fail("Incorrect attribute:\n'" + el.getAttribute("x") + "'");
			}
			xdef =
 "<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a xd:script = \"ref b\" />\n"+
"  <b>\n"+
"    <c xd:script = \"ref d; options clearAdoptedForgets;\" />\n"+
"  </b>\n"+
"  <d>\n"+
"    <e xd:script = \"forget\" />\n"+
"  </d>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><c><e/></c></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xdef =
 "<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a xd:script = \"ref b\" />\n"+
"  <b>\n"+
"    <c xd:script = \"forget; ref d; options clearAdoptedForgets;\" />\n"+
"  </b>\n"+
"  <d>\n"+
"    <e xd:script = \"forget\" />\n"+
"  </d>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><c><e/></c></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a/>", el);
			xdef =
 "<x:def xmlns:x='" + _xdNS + "'\n"+
"       x:name =\"a\" x:root =\"a\">\n"+
"  <a>fixed 'abcd'</a>\n"+
"</x:def>";
			xp = compile(xdef);
			xml = "<a>ab<![CDATA[cd]]></a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a><![CDATA[ab]]>cd</a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xdef = //datetime
 "<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a date = \"required xdatetime('d.M.y', 'yyyyMMdd')\" />\n"+
"</xd:def>";
			xml = "<a date = \"20.5.2004\"/>";
			xp = compile(xdef);
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520'/>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"  <a date = \"required xdatetime('{H1m1s1}d.M.y|{H23m59s59}d/M/y',\n"+
"             'yyyyMMddHHmmss')\" />\n"+
"</xd:def>";
			xml = "<a date = \"20.5.2004\"/>";
			xp = compile(xdef);
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520010101'/>", el);
			xml = "<a date = \"20/5/2004\"/>";
			xp = compile(xdef);
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<a date='20040520235959'/>", el);
			xdef = // hex
"<x:def xmlns:x='" + _xdNS + "' x:name='a' x:root='a'>\n"+
"  <a a='required hexBinary(2)' b= 'required hexBinary(2)'>\n"+
"     required hexBinary(3)\n"+
"  </a>\n"+
"</x:def>";
			xp = compile(xdef);
			xml = "<a a='2345' b='AbcF'>112233</a>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xml = "<a a='p234' b='AbcF0'>2233</a>";
			parse(xp, "a", xml, reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='hex; onTrue {Bytes b=hex.parse().getValue(); out(b.toHex);}'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xdef, "", "<a a=' '/>", reporter);
			assertErrors(reporter);
			parse(xdef, "", "<a a='0x1a'/>", reporter);
			assertErrors(reporter);
			strw = new StringWriter();
			assertEq("<a a='a'/>",
				parse(xp, null, "<a a='  a  '/>", reporter, strw, null, null));
			assertEq(strw.toString(), "0A");
			assertNoErrorwarnings(reporter);
			strw = new StringWriter();
			assertEq("<a a='a b'/>",
				parse(xp, null, "<a a=' a b '/>", reporter, strw, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(strw.toString(), "AB");
			xml = "<a a='    a b c d   e    '/>";
			strw = new StringWriter();
			assertEq("<a a='a b c d   e'/>",
				parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(strw.toString(), "0ABCDE");
			strw = new StringWriter();
			assertEq("<a a='bcde'/>" ,
				parse(xp, null, "<a a=' bcde '/>", reporter, strw, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(strw.toString(), "BCDE");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='hex; onTrue {Bytes b=getParsedValue(); out(b.toHex);}'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='    a b c d   e    '/>";
			strw = new StringWriter();
			assertEq("<a a='a b c d   e'/>",
				parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(strw.toString(), "0ABCDE");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a\n"+
"    Davka        = \"required\"\n"+
"    ZeDne        = \"required\" > \n"+
"    <a  xd:script = \"finally {Container c = xpath('../@Davka');\n"+
"		out('len=' + c.getLength()\n"+
"		+', typ=' + c.getItemType(0)\n"+
"		+', value=' + getText(c,0));\n"+
"      }\" /> \n"+
" required\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml ="<a Davka='davka' ZeDne='1.1.99'><a/>text</a>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null, null);
			s = strw.toString();
			assertTrue(("len=1, typ=" + XDValueID.XD_ATTR +
				", value=davka").equals(s), s);
			// test remove from context
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a=\"string(); finally {Container c = new Container();\n"+
"      c.addItem('a'); c.addItem('b'); c.addItem('c');\n"+
"      AnyValue v=c.removeItem(1);\n"+
"      setText(v.toString() + c.item(0) + c.item(1)); }\" />\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xml = "<a a='x'/>";
			el = parse(xp, "", xml, reporter);
			assertEq("<a a='bac'/>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='root'>\n"+
"<root attr='required string()'\n"+
"      xd:script=\"finally out('xyz')\">\n"+
"  <a xd:script=\"occurs 2\"/>\n"+
"</root>\n"+
"\n"+
"</xd:def>\n";
			xml = "<root bttr='attr'><a/><b/></root>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null,null);
			if (reporter.errorWarnings()) {
				assertTrue(reporter.getErrorCount() == 4,
					reporter.printToString());
			} else {
				fail("error not reported");
			}
			xdef =
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"  <a x:script='options trimText'>\n"+
"    <a x:script='occurs 0..2'/>\n"+
"    <b x:script='occurs 0..2'/>\n"+
"    required string(); onTrue out(getText())\n"+
"  </a>\n"+
"</x:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", "<a>data</a>", reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("data", strw.toString());
			xdef =
"<x:def xmlns:x='" + _xdNS + "' name='a' root='a'\n"+
"       script='options ignoreEmptyAttributes'>\n"+
"<a x:script='ref b'>\n"+
"  <p/>\n"+
"  <q/>\n"+
"  optional int(); default 456\n"+
"</a>\n"+
"<b attr=\"optional an(); default 'a123x'\">\n"+
"  <c/>\n"+
"</b>\n"+
"</x:def>\n";
			assertEq("<a attr='a123x'><c/><p/><q/>456</a>",
				parse(xdef, "a", "<a><c/><p/><q/></a>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"  xd:script='options ignoreEmptyAttributes,ignoreAttrWhiteSpaces,"
				+ "noTrimText,preserveTextWhiteSpaces'>\n"+
"<a xd:script='ref x' />\n"+
"<x>\n"+
"  <xd:mixed>\n"+
"    <b xd:script='occurs 0..'/>\n"+
"    <c xd:script='occurs 0..'/>\n"+
"    <d xd:script='occurs 0..'/>\n"+
"  </xd:mixed>\n"+
"</x>\n"+
"</xd:def>";
			parse(xdef, "", "<a><b/> <c/> </a>", reporter);
			assertNoErrorwarnings(reporter);
			xdef = // check initialization of declaration
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"<![CDATA[\n"+
"   void p1(){ out('this is an error!'); }\n"+
"   void p2(){ out('this is an error!'); }\n"+
"   void p3(){ out('this is an error!'); }\n"+
"   void p4(){ out('this is an error!'); }\n"+
"   int ii = 3*5; \n"+
"   int jj = 2*ii; \n"+
"   int kk = 2*jj; \n"+
"   int xx = 2*kk; \n"+
"]]>\n"+
"</xd:declaration>\n"+
"\n"+
"  <a xd:script = \"finally out('xx = ' + xx)\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			parse(xp, "", "<a/>", reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("xx = 120", strw.toString());
			xdef = //test sequence methods (init, finally)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence script=\"init outln('start'); finally outln('end')\">\n"+
"      <b xd:script='occurs 1..*'/>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			_myX = 0;
			strw = new StringWriter();
			parse(xp, "", "<a><b/><b/><b/><c/></a>", reporter, strw,null,null);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			assertEq("start\nend\n", strw.toString());
			xdef = //test collection, metaNamespace, any and match
"<xd:collection xmlns:xd='my.meta.ns'\n"+
"  xmlns:x='" + _xdNS + "'\n"+
"  x:metaNamespace='my.meta.ns'>\n"+
"<x:def root='a'>\n"+
"  <a>\n"+
"    <xd:any xd:script=\"match 'x' == getQnameLocalpart(getElementName());\n"+
"               onAbsence out('Absence'); finally out('OK')\" />\n"+
"  </a>\n"+
"</x:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq("<a/>",
				parse(xp, "", "<a><b/></a>", reporter, strw, null, null));
			if (!reporter.errorWarnings()) {
				fail();
			} else {
				if ((rep = reporter.getReport()) == null ||
					!"XDEF501".equals(rep.getMsgID())) {
					fail("" + rep);
				}
			}
			assertEq("Absence", strw.toString());
			xml = "<a><x/></a>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			assertEq("OK", s = strw.toString(), s);
			xdef =  //test recursive references
"<xd:def xmlns:xd='" + _xdNS + "' root='foo'>\n"+
"<foo><bar xd:script='occurs ?; ref foo'/></foo>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<foo/>";
			parse(xp, "", xml, reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			xml = "<foo><bar><bar><bar><bar/></bar></bar></bar></foo>";
			parse(xp, "", xml, reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
		} catch(Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='foo'>\n"+
"<foo>\n"+
"  <bar>\n"+
"    <foo xd:script='occurs ?; ref foo'/>\n"+
"  </bar>\n"+
"</foo>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<foo><bar/></foo>", reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
			xml =
				"<foo><bar><foo><bar><foo><bar/></foo></bar></foo></bar></foo>";
			parse(xp, "", xml, reporter);
			assertEq(reporter.errorWarnings(), false, reporter.printToString());
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
				fail();
			} else {
				assertTrue(s.indexOf("XML075") >= 0, ex);
				assertTrue(s.indexOf(dataDir + "U.xdef") >= 0, ex);
			}
		}
		try {
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
			xml = "<a:note xmlns:a='http://www.w3schools.com'>" +
				"<b:to xmlns:b='http://www.w3ctest.com'/>"+
				"</a:note>";
			parse(xdef, "a", xml, reporter);
			assertNoErrors(reporter);
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
			xml = "<a:note xmlns:a='http://www.w3schools.com'>" +
				"<b:to xmlns:b='http://www.w3ctest.com'/>"+
				"</a:note>";
			parse(xdef, "a", xml, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // test nillable
"<xd:def root='tns:DM' xmlns:tns='abc'\n"+
"        xmlns:xd='" + _xdNS + "'>\n"+
"  <tns:DM xd:script=\"occurs 0..1; options nillable\">\n"+
"        required string(0, 40)\n"+
"  </tns:DM>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml = "<tns:DM xmlns:tns='abc'"+
				" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"+
				" xsi:nil='true'/>";
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			xdef =
"<xd:def root='DM' xmlns:xd='" + _xdNS + "'>\n"+
"  <DM xd:script=\"occurs 0..1; options nillable\">\n"+
"    <a/>\n"+
"    required string(0, 40)\n"+
"    <b/>\n"+
"  </DM>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<DM xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"+
				" xsi:nil='true'/>\n";
			xd = xp.createXDDocument();
			parse(xd, xml, reporter);
			assertNoErrors(reporter);
			xml = "<DM xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'"+
				" xsi:nil='true'><a/>x<b/></DM>\n";
			xd = xp.createXDDocument();
			parse(xd, xml, reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <b xd:script='options nillable'>\n"+
"      <c/>\n"+
"      required string(0, 40)\n"+
"      <d/>\n"+
"    </b>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"+
				"<b xsi:nil='true'/></a>\n";
			parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			xml = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"+
				"<b xsi:nil='true' x='x'/></a>\n";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"+
				"<b xsi:nil='true'><c/>x<d/></b></a>\n";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xdef = //getXPos, getSourcePosdition
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='string; onTrue testPos()'>\n"+
"   string; onTrue testPos()\n"+
" </a>\n"+
"</xd:def>";
			xd = compile(xdef, getClass()).createXDDocument();
			xd.xparse("<a\n a = '123'\n>\nx</a>", null);
			xdef = //test getXDPosition, getXPos
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='string; onTrue outln(getXDPosition() + \"; \" + getXPos())'>\n"+
"   string; onTrue outln(getXDPosition() + '; ' + getXPos())\n"+
" </a>\n"+
"</xd:def>";
			xd = compile(xdef, getClass()).createXDDocument();
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			xml = "<a\n a='123'\n>\nx</a>";
			parse(xd, xml, reporter);
			out.close();
			assertEq("#a/@a; /a/@a\n#a/$text; /a/text()\n", strw.toString());
			xdef = //Container
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  int i = 1;\n"+
"  String s = 'Z';\n"+
"  Container c1 = [%a='A', %b='B'], c2 = new Container();\n"+
"  Container c3 = [1, 'x', s], c4 = [%a='x',1];\n"+
"  NamedValue n = %y='Y';\n"+
"</xd:declaration>\n"+
" <a>\n"+
"   string;\n"+
"   onTrue {\n"+
"     c2.setNamedItem(new NamedValue('x', i));\n"+
"     c2.setNamedItem(n); c2.setNamedItem(%z=s); c2.setNamedItem(n=%q='Q');\n"+
"     setText(c1.getNamedItem('a').toString() + c1.getNamedItem('b')\n"+
"       + c2.getNamedItem('x') + c2.getNamedItem('y') + c2.getNamedItem('z')\n"+
"       + c2.getNamedItem('q')+c3.getItemType(0)\n"+
"       + (c3.getItemType(1)==$STRING)+(c3.getItemType(0)==$INT)\n"+
"       + n + (c4.getItemType(0) == $INT)\n"+
"       + c4.getNamedItem('a') + c4.getNamedItem('x')); }\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a>x</a>";
			el = parse(xdef, "", xml, reporter);
			assertEq("AB1YZQ1truetrue%q=Qtruex",
				el.getChildNodes().item(0).getNodeValue());

			xdef = //test xpath namespace context
"<xd:def root='x:a'\n"+
"    xmlns:x='abc' xmlns:z='xyz' xmlns:xd='" + _xdNS + "'>"+
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
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			parse(xd, xml, reporter);
			out.close();
			assertNoErrors(reporter);
			assertEq("x", strw.toString());
			//test complex types
			xdef = dataDir + "TestParse_type.xdef";
			xp = compile(xdef);
			xml = dataDir + "TestParse_type_valid_1.xml";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef = // optional
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='? string(0,10);'/>\n"+
"</xd:def>";
			xml = "<a a=''/>";
			el = parse(xdef, "", xml, reporter);
			assertEq(xml, el);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"  <a xd:script='options acceptEmptyAttributes' t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertEq(xml, el);
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"  script='options acceptEmptyAttributes'>" +
"<a t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"<a xd:script='options acceptEmptyAttributes; ref b'/>" +
"<b t='string'/>" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a t=''/>";
			el = parse(xp, "", xml, reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>" +
"<a xd:script='ref b'/>" +
"<b xd:script='options acceptEmptyAttributes' t='string'/>" +
"</xd:def>";
			xml = "<a t=''/>";
			el = parse(xdef, "", xml, reporter);
			assertTrue(el.hasAttribute("t") && "".equals(el.getAttribute("t")));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"        script='options acceptEmptyAttributes'>\n"+
" <xd:declaration>\n"+
"   uniqueSet x {key: string(3,4)};\n"+
" </xd:declaration>\n"+
"  <a><b xd:script='+' a='required x.key.ID;'/></a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a><b a=''/></a>";
			el = parse(xp, "",  xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xml = "<a><b a='123456'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertTrue(reporter.errors(), "Error not reported");
			assertEq(xml, el);
			xml = "<a><b a=''/><b a=''/></a>"; //empty attribute is not checked!
			el = parse(xp, "",  xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xml = "<a><b a='12'/></a>";
			assertEq(xml, parse(xp, "",  xml, reporter));
			assertTrue(reporter.errors(), "Error not reported");
			xml = "<a><b a='123'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			xml = "<a><b a='123'/><b a='123'/></a>";
			el = parse(xp, "",  xml, reporter);
			assertTrue(reporter.errors(), "Error not reported");
			assertEq(xml, el);

			xdef = // test reference to xd:any
"<xd:def xmlns:xd='" + _xdNS + "' root='x|def'>\n"+
"<xd:any xd:script=\"match getNamespaceURI() EQ 'u';options moreAttributes;\n"+
"          finally out(getNamespaceURI()+','+getElementLocalName());\"\n"+
"  xd:name='def'\n"+
"  name='required string'\n"+
"  script='required string'\n"+
"  a='required string'>\n"+
"  <b/>\n"+
"</xd:any>\n"+
" <x>\n"+
"   <xd:any xd:script='ref def'/>\n"+
" </x>\n"+
"</xd:def>";
			xp = compile(xdef); //vytvořeni ze zdroju
			xml ="<f a='a' name='b' script='c'><b/></f>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null, null);
			assertErrors(reporter);
			xml =
"<xd:f xmlns:xd='u' a='a' name='b' script='c'><b/></xd:f>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("u,f", strw.toString());
			xml = "<x><f a='a' name='b' script='c'><b/></f></x>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null, null);
			assertErrors(reporter);
			xml =
"<x><xd:f xmlns:xd='u' a='a' name='b' script='c'><b/></xd:f></x>";
			strw = new StringWriter();
			parse(xp, "", xml, reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("u,f", strw.toString());

			xdef = // forced conversion of ParseResult to boolean
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a=\"optional {boolean b = eq('ho'); boolean c = b; return c;}\"/>\n"+
"</xd:def>\n";
			parse(xdef, "", "<a a='ho'/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>boolean x(boolean b) {return b;}</xd:declaration>\n"+
"<a a=\"optional {boolean b = x((boolean) eq('ho')); return b;}\"/>\n"+
"</xd:def>\n";
			parse(xdef, "", "<a a='ho'/>", reporter);
			assertNoErrorwarnings(reporter);

			xdef = //test moreElement option
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script='options moreElements'>\n"+
"    <b xd:script='?; finally i++;'/>\n"+
"    <c xd:script='?; finally i++;'/>\n"+
"</a>\n"+
"<xd:declaration> int i = 10;</xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
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

			xdef = // Test fully qualified method call
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:script='finally out(test.xdef.TestParse.getInt5());'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			strw = new StringWriter();
			out = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(out);
			parse(xd, "<a/>", reporter);
			out.close();
			assertNoErrors(reporter);
			assertEq("5", strw.toString());
			xdef = //X-definition ver 2.0 //////////////////////////////////////
"<xd:def xmlns:xd='" + XDConstants.XDEF20_NS_URI + "' root='a'\n"+
"  methods= 'XDParser test.xdef.TestParse.licheCislo()' >\n"+
"  <a a='licheCislo'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a=\" 1 \"/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a=\"10\"/>", reporter);
			assertErrors(reporter);
			xdef = //X-definition ver 3.1 //////////////////////////////////////
"<xd:def xmlns:xd='" + XDConstants.XDEF31_NS_URI + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method XDParser test.xdef.TestParse.licheCislo();\n"+
"</xd:declaration>\n"+
"  <a a='licheCislo'/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			parse(xp, "", "<a a=\" 1 \"/>", reporter);
			assertNoErrors(reporter);
			parse(xp, "", "<a a=\"10\"/>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>external Parser licheCislo;</xd:declaration>\n"+
"  <a a='licheCislo()'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("licheCislo", new LicheCislo());
			parse(xd, "<a a=' 1 '/>", reporter);
			assertNoErrors(reporter);
			parse(xd, "<a a='10'/>", reporter);
			assertErrors(reporter);

			xdef = // result of xpath (ie. Container) in boolean expression
"<xd:def xmlns:xd='" + _xdNS + "' root='a|b|c|d'>\n"+
"<a typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xpath('parent::a[@typ=1]')\"/>\n"+
"    <c xd:script=\"match xpath('parent::a[@typ=2]')\"/>\n"+
"  </xd:choice>\n"+
"</a>\n"+
"<b typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xpath('../@typ=&quot;1&quot;')\"/>\n"+
"    <c xd:script=\"match xpath('../@typ=2')\"/>\n"+
"  </xd:choice>\n"+
"</b>\n"+
"<c typ='int()'>\n"+
"  <xd:choice>\n"+
"    <b xd:script=\"match xpath('//c[@typ=&quot;1&quot;]')\"/>\n"+
"    <c xd:script=\"match xpath('//c[@typ=2]')\"/>\n"+
"  </xd:choice>\n"+
"</c>\n"+
"<d\n"+
"  a=\"optional int\"\n"+
"  b=\"optional int\"\n"+
"  xd:script=\"finally if (!(xpath('@a') XOR xpath('@b')))\n"+
"     error('EE', '@a, @b mus be excluzive');\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a typ='1'><b/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a typ='2'><b/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='2'><c/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a typ='1'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<a typ='4'><c/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<b typ='1'><b/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<b typ='2'><b/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<b typ='2'><c/></b>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<b typ='1'><c/></b>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<c typ='1'><b/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<c typ='2'><b/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<c typ='2'><c/></c>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<c typ='1'><c/></c>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);

			xml = "<d a='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<d b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<d a='1' b='2'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xml = "<d/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertErrors(reporter);
			xdef = // variables declared in script of Element
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:script=\"var int b=0,c=0; occurs *; finally out('B='+b+',C='+c);\">\n"+
"    <B xd:script=\"occurs *; finally b++;\"/>\n"+
"    <C xd:script=\"occurs *; finally c++;\"/>\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			strw = new StringWriter();
			xml = "<A><B/><B/><C/></A>";
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("B=2,C=1", strw.toString());
			xdef = // test ignore
"<xd:def name='a' root='root' xmlns:xd='" + _xdNS + "'>\n"+
"<root xd:text=\"ignore\">\n"+
"  <xd:choice>\n"+
"    <a xd:script=\"occurs 0..2\" />\n"+
"    <b xd:script=\"occurs 0..2\" />\n"+
"  </xd:choice>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<root>text1<a/>text2</root>";
			el = parse(xp, "a", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<root><a/></root>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x attr='ignore;'> <xd:text>ignore</xd:text></x>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><x attr='attr'>text</x></a>";
			assertEq("<a><x/></a>", parse(xdef, "", xml, reporter));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <x xd:script='occurs ignore;' attr='ignore;'>\n"+
"   <xd:text>ignore</xd:text>\n"+
"  </x>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a><x attr='attr'>text</x></a>";
			assertEq("<a/>", parse(xdef, "", xml, reporter));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script='occurs ignore;'>\n"+
"  <x xd:script='occurs ignore;' attr='ignore;'>\n"+
"   <xd:text>ignore</xd:text>\n"+
"  </x>\n"+
"</a>\n"+
"</xd:def>";
			assertNull(parse(xdef, "", "<a><x attr='attr'>text</x></a>",
				reporter));
			xdef = // xd:attr
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<A xd:attr='* getAttrName().startsWith(\"impl-\")'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A impl-a='1' impl-bb='2' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<A impl-a='1' ympl-bb='2' />";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:attr='ignore int'>\n"+
"    <B xd:script='ignore'/>\n"+
"    ignore string\n"+
"  </A>\n"+
"</xd:def>");
			xml = "<A/>";
			assertEq("<A/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A x='1' y='2' z = '3'><B><C/>z</B><B/>abc</A>";
			assertEq("<A/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			// test ignoreOther
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:script='option ignoreOther' x='? int'/>\n"+
"</xd:def>");
			xml = "<A x='1' y='2' z = '3'><B a='a'><C/>z</B><B/>abc</A>";
			assertEq("<A x='1'/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A y='2' z = '3'><B a='a'><C/>z</B><B/>abc</A>";
			assertEq("<A/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			// test acceptOther
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:script='option acceptOther' x='? int'/>\n"+
"</xd:def>");
			xml = "<A x='1' y='2' z = '3'><B a='a'><C/>z</B><B/>abc</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A y='2' z = '3'><B a='a'><C/>z</B><B/>abc</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			// illegal
			xp = compile("<xd:def root='root' xmlns:xd='" + _xdNS + "'>\n"+
"<root xd:text=\"illegal\">\n"+
"  <xd:choice>\n"+
"    <a xd:script=\"occurs 0..2\" />\n"+
"    <b xd:script=\"occurs 0..2\" />\n"+
"  </xd:choice>\n"+
"</root>\n"+
"</xd:def>");
			xml = "<root>text1<a/>text2</root>";
			el = parse(xp, "", xml, reporter);
			if (reporter.getErrorCount() != 2 ||
				!"XDEF528".equals(reporter.getReport().getMsgID()) ||
				!"XDEF528".equals(reporter.getReport().getMsgID())) {
				fail(reporter.printToString());
			}
			assertEq("<root><a/></root>", el);
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"  <A xd:attr='illegal int'>\n"+
"    <B xd:script='illegal'/>\n"+
"    illegal string\n"+
"  </A>\n"+
"</xd:def>");
			xml = "<A/>";
			assertEq("<A/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<A x='1' y='2' z = '3'><B><C/>z</B><B/>abc</A>";
			assertEq("<A/>", parse(xp, "", xml, reporter));
			assertEq(6, reporter.getErrorCount(), reporter.printToString());
			xp = compile("<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> ParseResult x() {return int();}</xd:declaration>" +
" <a x='x()'/>" +
"</xd:def>");
			xml = "<a x='1' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<xd:declaration scope = 'local'>\n" +
"  int i = - 0x_ff_ff_ff_ff_ff_ff_ff__ff_;\n" +
"  float x = -1_1_.2_30_e2;\n" +
"  NamedValue nv = %x:y..n-v=%y=%z=-0d123__456_890_999_000_333.0;\n" +
"  ParseResult p() {\n"	+
"    String s = getText();\n" +
"    ParseResult p;\n" +
"    try {\n" +
"      p = int(s);\n" +
"      int i = p.getValue();\n" +
"      if (i != 123) {\n" +
"        p.error('e123');\n" +
"      }\n" +
"      return p;\n" +
"    } catch (Exception ex) {\n" +
"      p = new ParseResult(s);\n" +
"      p.error(ex.getMessage());\n" +
"      return p;\n" +
"    }\n" +
"  }\n"	+
"</xd:declaration>" +
"  <A xd:script=\"var int j; finally out('i='+i+',j='+j+',x='+x+','+nv);\"\n" +
"     a='p(); onTrue {j = getParsedValue();}'/>\n" +
"</xd:def>");
			xml = "<A a='123'/>";
			strw = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"i=1,j=123,x=-1123.0,%x:y..n-v=%y=%z=-123456890999000333.0");
			strw = new StringWriter();
			assertEq(xml, create(xp, "", "A", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq(strw.toString(),
				"i=1,j=123,x=-1123.0,%x:y..n-v=%y=%z=-123456890999000333.0");
			xdef = // types in different declarations
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"<xd:declaration>\n" +
"   int i = 1;\n" +
"   type paramCode string(i);\n" +
"</xd:declaration>\n" +
"<xd:declaration>\n" +
"   int j = i;\n" +
"   type xx zz;\n" +
"</xd:declaration>\n" +
"<xd:declaration>\n" +
"   type zz paramCode;\n" +
"</xd:declaration>\n" +
" <a paramCode='xx' />\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = " <a paramCode='xx'/>";
			assertEq("<a paramCode='xx'/>", parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef = // types in different declarations
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"<xd:declaration>\n" +
"   int j = i;\n" +
"   type xx zz;\n" +
"</xd:declaration>\n" +
"<xd:declaration>\n" +
"   type zz paramCode();\n" +
"</xd:declaration>\n" +
"<xd:declaration>\n" +
"   int i = 1;\n" +
"   type paramCode string(i);\n" +
"</xd:declaration>\n" +
" <a paramCode='xx()' />\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a paramCode='xx'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			// test correct error reporting
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> int x = 0; </xd:declaration>\n" +
"<a x=\"?; onAbsence x = -1;\">\n" +
"  <A xd:script=\"occurs 1; onAbsence if (x == -1) error('Missing x');\"/>\n"+
"  <B xd:script=\"occurs ?\"/>\n" +
"  <C xd:script=\"occurs ?\"/>\n" +
"</a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x=\"1\"><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a x=\"1\"><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a x=\"1\"><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><A/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
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
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration> int x = 0; </xd:declaration>\n" +
"<a x=\"?; onAbsence x = -1;\">\n" +
"  <A xd:script=\"occurs ?; onAbsence if (x == -1) error('Missing x');\"/>\n"+
"  <B xd:script=\"occurs ?\"/>\n" +
"  <C xd:script=\"occurs ?\"/>\n" +
"</a>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x=\"1\"><B/><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a x=\"1\"><B/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a x=\"1\"><C/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml = "<a><A/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
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
			xdef = // test var section of element script
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n" +
"<A xd:script=\"var String s = 'x';\">\n" +
"  <B xd:script=\"var int count = 0; occurs 1..*; finally s += count;\">\n"+
"    string; onAbsence addText(s);\n" +
"  </B>\n" +
"</A>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B/></A>";
			assertEq("<A><B>x</B></A>", parse(xp, "", xml, reporter));
			xml = "<A><B/><B/></A>";
			assertEq("<A><B>x</B><B>x0</B></A>", parse(xp, "", xml, reporter));
// test declared type in version 2.0, 3.1, 3.2 ...
			xdef = // version 3.1 and higher
"<xd:def xmlns:xd='"+_xdNS+"' root='a'>\n"+
"  <xd:declaration>\n"+
"    type x {if (true) return true; else return error('xxx');}\n"+
  "</xd:declaration>\n"+
"  <a x='x'/>\n"+
"</xd:def>";
			xml="<a x='x'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
// test variable parameter in validation method.
			xp = compile(
"<xd:def xmlns:xd='" + _xdNS + "' root='a' >\n" +
"  <xd:declaration scope=\"local\">\n" +
"    int   max;\n" +
"    type  cislo1 int(1,max); \n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <Item xd:script=\"*\"\n" +
"          Size     =\"int(); onTrue max=parseInt(getText())\"\n" +
"          Number   =\"cislo1()\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml =
"<a>\n" +
"  <Item Size=\"1\" Number=\"1\"/>\n" +
"  <Item Size=\"2\" Number=\"2\"/>\n" +
"  <Item Size=\"3\" Number=\"3\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
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
"    <Item xd:script=\"*\"\n" +
"          Type      =\"xdType(); onTrue xtype=getParsedValue()\" \n" +
"          Value     =\"xtype()\"/>\n" +
"  </a>\n" +
"</xd:def>");
			xml =
"<a>\n" +
"  <Item Value=\"Alfa\"     Type=\"string(1,5)\"/>\n" +
"  <Item Value=\"99\"       Type=\"int(1,99)\"/>\n" +
"  <Item Value=\"3.6.2002\" Type=\"xdatetime('d.M.yyyy')\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xml =
"<a>\n" +
"  <Item Value=\"Alfa\"     Type=\"string(1,5)\"/>\n" +
"  <Item Value=\"Beta\"     Type=\"int(1,99)\"/>\n" +
"  <Item Value=\"3.6.2002\" Type=\"xdatetime('d.M.yyyy')\"/>\n" +
"</a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(s.contains("XDEF813"), s);
// version 2.0
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF20_NS_URI + "' root='a'>\n"+
"  <xd:declaration>type x {parse : {return true;}}</xd:declaration>\n"+
"  <a x='x'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a x='x'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =  // version 2.0
"<xd:def xmlns:xd='" + XDConstants.XDEF20_NS_URI + "' root='a'>\n" +
"    <xd:declaration>\n" +
"        type gamYear        {parse : {return long(1,2);}}\n" +
"        type gamDate        {parse : xdatetime('yyyyMMdd');}\n" +
"        type gamDateTime    {parse:  xdatetime('yyyyMMddHHmmss');}\n" +
"        type gamS           {parse:  {return true;}}\n" +
"    </xd:declaration>\n" +
"  <a a='gamYear' b='gamDate' c='gamDateTime' d='gamS' />\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a='2' b='20180501' c='20180501122105' d='?'/>";
			assertEq(xml, parse(xp,"",xml, reporter));
			assertNoErrors(reporter);
			try {
				xdef = SUtils.modifyFirst(xdef, "xdef/2.0'", "xdef/3.1'");
				compile(xdef);
			} catch (Exception ex) {
				if(!ex.toString().contains("W XDEF997")) fail(ex);
			}
//test ver 20 and 31 in collection
			xp = compile(
"<xd:collection xmlns:xd='" + XDConstants.XDEF20_NS_URI + "'>"+
"<xd:def xd:name='X' xd:root='A' xmlns:xd='" + _xdNS + "'>"+
"<A a='string()'>"+
"  <B xd:script='+; ref X#R'/>"+
"</A>"+
"<R r='optional string()'/>"+
"</xd:def>"+
"<xd:def xd:name='Y' xd:root='B' xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>"+
"<B b='string()'/>"+
"</xd:def>"+
"</xd:collection>");
			xml = "<A a='x'><B r='y'/></A>";
			assertEq(xml, parse(xp, "X", xml, reporter));
			assertNoErrors(reporter);
			xml = "<B b='y'/>";
			assertEq(xml, parse(xp, "Y", xml, reporter));
			assertNoErrors(reporter);
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
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
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
			assertNoErrors(reporter);
		} catch (Exception ex) {
			if (!ex.getMessage().contains("XML099"))fail(ex);
		}
		try {// test "classpath" and "file" protocol in URL
			xp = XDFactory.compileXD((Properties) null, //without wildcards
				"classpath://org.xdef.impl.compile.XdefOfXdefBase.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef20.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef31.xdef",
				"classpath://org.xdef.impl.compile.XdefOfXdef32.xdef");
			xp = XDFactory.compileXD((Properties) null, //with wildcards
				"classpath://org.xdef.impl.compile.XdefOfXdef*.xdef");
			xp = XDFactory.compileXD((Properties) null, //without wildcards
"<xd:collection xmlns:xd='" + _xdNS + "'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef20.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef31.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef32.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdefBase.xdef;'/>");
			xp = XDFactory.compileXD((Properties) null, //with wildcards
"<xd:collection xmlns:xd='" + _xdNS + "'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef*.xdef'/>");
			xp = XDFactory.compileXD((Properties) null, //without wildcards
"<xd:def xmlns:xd='" + _xdNS + "' name='xxx'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef20.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef31.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdef32.xdef;\n"+
"    classpath://org.xdef.impl.compile.XdefOfXdefBase.xdef;'/>");
			xp = XDFactory.compileXD((Properties) null, //with wildcards
"<xd:def xmlns:xd='" + _xdNS + "' name='xxx'\n"+
"  xd:include='classpath://org.xdef.impl.compile.XdefOfXdef*.xdef'/>");
		} catch (Exception ex) {fail(ex);}

		resetTester();
		new File(tempDir + "vystup.txt").delete();
	}

////////////////////////////////////////////////////////////////////////////////
// methods and objects used in X-definitions as external.
////////////////////////////////////////////////////////////////////////////////
	public static long getInt5() {return 5;}
	public static void testOldx(XXNode xnode, XDValue[] params) {
		Element el = xnode.getElement();
		if (el == null) {
			xnode.error("", "Object is null");
		} else if (!"a".equals(el.getNodeName())) {
			xnode.error("","Object is not element 'a'");
		}
	}
	public static void testOldy(XXData xdata, XDValue[] params) {
		String s = xdata.getTextValue();
		if (s == null) {
			xdata.error("", "Object is null");
		} else if (!"1".equals(s)) {
			xdata.error("", "Object is not String '1'");
		}
	}
	public static void myCheck(final XXElement xel,
		final String s, final byte[] b) {
		if (!s.equals(new String(b))) {
			((TestParse) xel.getXDDocument().getUserObject()).fail("Check");
		}
	}
	public void myProc(final String s) {_myX = 1;}
	public static void myProc(final XXNode xnode, final String s) {_myX = 2;}
	public static void myProc(final XDValue[] p) {_myX = 3;}
	public static void setDateProc(XXData xdata, XDValue[] params) {
		String s = params[0].datetimeValue().formatDate("yyyy-MM-dd");
		xdata.setTextValue(s);
	}
	public static boolean testExt(XXElement xel, String a, String b, String c){
		if (_myX == 1 && "a".equals(a) && "b".equals(b) && "c".equals(c)) {
			_myX = 0;
			return true;
		}
		return false;
	}
	public static void myErr(XXNode xel, XDValue[] params) {
		xel.getTemporaryReporter().clear();
		if (params.length == 1
			&& params[0].getItemId() == XDValueID.XD_INT
			&& params[0].longValue() == 4204) {
			xel.getTemporaryReporter().clear();
			_myX = 4204;
		} else {
			_myX = 1;
		}
	}
	public static long myError() {throw new RuntimeException("MyError");}
	final public static void testPos(final XXNode xnode) {}
	/** Check datetime according to mask1. If parsed value has time zone UTC,
	 * then convert date to the local time. Format of result is given by mask2.
	 * @param xdata actual XXData object.
	 * @param args array of parameters.
	 * @return true if format is OK.
	 */
	final public static XDParseResult dateToLocal(XXData xdata, XDValue[] args){
		String mask1 =
			args.length >= 1 ? args[0].toString() : "yyyyMMddTHHmmss[Z]";
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
		String mask2 =
			args.length >= 2 ? args[1].toString() : "yyyyMMddTHHmmssZ";
		xdata.setTextValue(sd.formatDate(mask2));
		return new DefParseResult(s,new DefDate(sd));
	}
	private static class LicheCislo extends XDParserAbstract {
		LicheCislo() {}
		@Override
		final public void parseObject(final XXNode xnode,
			final XDParseResult p) {
			StringParser parser = new StringParser(p.getSourceBuffer());
			parser.skipSpaces();
			if (parser.isInteger()) {
				long x = parser.getParsedLong();
				if ((x & 1) == 0) {
					p.error("CHYBA001", "Cislo neni liche");
				} else {
					p.setParsedValue(new DefLong(x));
				}
				parser.skipSpaces();
				p.setBufIndex(parser.getIndex());
			} else {
				p.error(XDEF.XDEF515); // Value error
			}
		}
		@Override
		public String parserName() {return "licheCislo";}
	}
	final public static XDParser licheCislo() {return new LicheCislo();}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}