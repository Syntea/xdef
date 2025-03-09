package mytests;

import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDPool;
import static org.xdef.XDValueID.XD_BYTE;
import org.xdef.component.XComponent;
import org.xdef.impl.code.DefLong;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 *
 * @author Vaclav Trojan
 */
public class TestExtParsers extends XDTester {

	public static XDParseResult pars(XDParseResult r) {
		if ("ab".equals(r.getSourceBuffer())) {
			r.isSpaces();
			if (r.eos()) {
				r.setSourceBuffer("1");
				r.setParsedValue(new DefLong(1));
				return r;
			}
		}
		r.error("TEST001", "Chyba: \"&{0}\"", "&{0}" + r.getParsedString());
		return r;
	}

	public void copyXComponentToMytests(final File file, final String name) {
		try {
			File f1 = new File(new File(file, "mytests"), name + ".java");
			File f2 = new File(getSourceDir(), name + ".java");
			org.xdef.sys.FUtils.copyToFile(f1, f2);
		} catch (org.xdef.sys.SException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static class MyParser extends XDParserAbstract {
		public MyParser() {super();}
		@Override
		public void parseObject(XXNode xnode, XDParseResult p) {
			p.isSpaces();
			DefLong result = null;
			String source = null;
			p.isSpaces();
			if (p.isToken("ab")) {
				result = new DefLong(1);
				source = "1";
			}
			if (result != null && (p.isSpaces() || p.eos())) {
				p.setParsedValue(result);
				p.setSourceBuffer(source);
				p.eos();
			} else {
				p.error("TEST001", "Chyba: \"&{0}\"", "&{0}"
					+ p.getParsedString());
			}
		}
		@Override
		public String parserName() { return "MyParser";}
		@Override
		public short parsedType() { return XD_BYTE;}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef, xml;
		XComponent xc;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		File file;
		StringWriter swr;
		try {
			xdef = // test MyParser
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  external Parser p\n"+
"</xd:declaration>\n"+
"<A x='p;' >\n"+
"</A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("p", new MyParser());
			xml = "<A x=' ab '/>";
			el = parse(xd, xml, reporter);
			assertNoErrors(reporter);
			assertEq("<A x='1'/>", el);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = // test getXPos
"<x:def xmlns:x='" + _xdNS + "' root='a'>\n"+
"<a x='string; finally {out(getXPos());}' >\n"+
"  <b y='string; finally out(getXPos())'>\n"+
"    string; finally {out(getXPos()); setText('ab');}\n"+
"  </b>\n"+
"</a>\n"+
"</x:def>";
			xp = compile(xdef);
			xp.displayCode();
			xml = "<a x='a'><b y='b'>c</b></a>";
			swr = new StringWriter();
			assertEq("<a x='a'><b y='b'>ab</b></a>",
				parse(xp, null, xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "/a/b[1]/text()/a/b[1]/@y/a/@x");
			swr = new StringWriter();
			assertEq("<a x='a'><b y='b'>ab</b></a>",
				create(xp, null, "a", reporter, xml, swr, null));
			assertNoErrorwarnings(reporter);
			assertEq(swr.toString(), "/a/b[1]/text()/a/b[1]/@y/a/@x");
		} catch (Exception ex) {fail(ex);}
//if(true)return;
//		try {
//			xdef =
//"<xd:def xmlns:xd='" + _xdNS + "' root=\"a|b\">\n" +
//"  <xd:component>%class mytests.MytestX_EXT1 %link #a;</xd:component>\n" +
//"  <xd:component>%class mytests.MytestX_EXT2 %link #b;</xd:component>\n" +
//"<xd:declaration>\n"  +
//"  external method XDParseResult mytests.TestExtParsers.pars(XDParseResult);\n"+
//"  external Parser p;\n"  +
//"  ParseResult x() {\n" +
//"    ParseResult r = string();\n"+
//"    return pars(r);\n"+
//"  }\n"  +
//"</xd:declaration>\n"  +
////"<a a=\"x();\"/>\n"  +
//"<a a=\"p();\"/>\n"  +
//"<b a=\"p();\"/>\n"  +
////"<b a=\"eq('ab');\"/>\n"  +
////"<b a=\"'ab'\"/>\n"  +
//"</xd:def>";
//			xp = compile(xdef);
////			xp.displayCode();
//			file = clearTempDir();
//			genXComponent(xp, file);
////			copyXComponentToMytests(file, "MytestX_EXT1");
//			xml = "<a a='ab'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq("<a a='1'/>", el = parse(xd, xml, reporter));
//			assertNoErrors(reporter);
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			xc = parseXC(xd, xml , null, reporter);
//			assertNoErrorwarningsAndClear(reporter);
//			assertEq(el, xc.toXml());
//			xml = "<a a='abc'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq(xml, parse(xd, xml, reporter));
//			assertErrors(reporter);
//
//			xml = "<b a='ab'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq("<b a='1'/>", el = parse(xd, xml, reporter));
//			assertNoErrors(reporter);
//			xc = parseXC(xd, xml , null, reporter);
//			assertNoErrorwarningsAndClear(reporter);
//			assertEq(el, xc.toXml());
//			xml = "<b a='bc'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq("<b a='2'/>", el = parse(xd, xml, reporter));
//			assertNoErrors(reporter);
//			xc = parseXC(xd, xml , null, reporter);
//			assertNoErrorwarningsAndClear(reporter);
//			assertEq(el, xc.toXml());
//			xml = "<b a='bcd'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq(xml, parse(xd, xml, reporter));
//			assertErrors(reporter);
//			xml = "<b a='cd'/>";
//			xd = xp.createXDDocument();
//			xd.setVariable("p", new MyParser());
//			assertEq(xml, parse(xd, xml, reporter));
//			assertErrors(reporter);
//		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"a\">\n" +
"<xd:component>%class mytests.MytestX_EXT3 %link #a;</xd:component>\n" +
"<xd:declaration>\n" +
"  ParseResult p() {\n" +
/*XX*/
"    String s = getText();\n" + // parse eq
"    if (s.equals('ab')) {\n" +
"      setText('1');\n"+
"      return int(1,1); \n" + // parse integer,
"    }\n"+
"    ParseResult result = new ParseResult(s);\n"+
/*XX*
"    ParseResult result = eq('ab');\n" + // parse eq
"    if (result.matches()) {\n" +
"      setText('1');\n"+
"      return int(1,1); \n" + // parse integer,
"    }\n"+
"    clearReports(result);\n" +
/*XX*/
"    result.error('X00','Incorect value: \"'+getSource(result).trim()+'\"');\n"+
"    return result;\n" +
"  }\n" +
"  type p union(%item=[int(1,10), int(-3,-1)]);\n"+
"  String t = '1', f = '-1', a = '-2';\n"+
"</xd:declaration>\n" +
"<a a=\"p; onTrue setText(t);onFalse setText(f);onAbsence setText(a)\">\n"+
" ? p; onTrue setText(t);onFalse setText(f);onAbsence setText(a)\n"+
"</a>"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			xp.display();
			file = clearTempDir();
			genXComponent(xp, file);
//			copyXComponentToMytests(file, "MytestX_EXT3");

			xd = xp.createXDDocument();
			xml = "<a a='2'>2</a>"; // onTrue
			assertEq("<a a='1'>1</a>", el = parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = parseXC(xd, xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xd = xp.createXDDocument();
			xml = "<a a='cd'>cd</a>"; // onFalse
			assertEq("<a a='-1'>-1</a>", el = parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = parseXC(xd, xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xml = "<a/>"; // onAbsence
			assertEq("<a a='-2'>-2</a>", el = parse(xd, xml, reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = parseXC(xd, xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());

			xml = "<a a='2'/>"; // onTrue
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq("<a a='1'>-2</a>", el = create(xd, "a", reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent("a", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xml = "<a a='cd'/>"; // onFalse
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq("<a a='-1'>-2</a>", el = create(xd, "a", reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent("a", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xml = "<a/>"; // onAbsense
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq("<a a='-2'>-2</a>", el = create(xd, "a", reporter));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			xc = xd.xcreateXComponent("a", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}