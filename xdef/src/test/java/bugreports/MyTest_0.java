package bugreports;

import java.io.File;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.impl.XDefinition;
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.parsers.XSAbstractParser;
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.model.XMVariable;
import org.xdef.model.XMVariableTable;
import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;

/*
[options] bootstrap class path not set in conjunction with -source 1.6
file.encoding cannot be set as system property, use <argLine>-Dfile.encoding=...</argLine> instead

(konverze dos -> shell)
http://www.tldp.org/LDP/abs/html/dosbatch.html
https://aka.ms/wslstore
Windows Subsystem for Linux (WSL)
https://www.thewindowsclub.com/how-to-run-sh-or-shell-script-file-in-windows-10
https://www.quora.com/How-do-I-run-shell-scripts-on-Windows
https://www.onlinetoolsportal.com/online-batch-file-shell-script-converter/
cd D:/cvs/DIST/java/_31_xdef/dist/jdk1.6/tests
bash testXdef.sh
cd D:/cvs/DIST/java/xdef/examples
bash runExample.sh Example1

ucet "trojan" (s "tvym" heslem) pouzitelny na vetsinu linuxovych serveru
v Syntee, na hrani a pokusy je vhodny napr. server "uzovka.syntea.cz"
(nebo jen "uzovka").
SSH-pristup (na windowsech napriklad pomoci ssh-terminalu "putty.exe"):
ssh trojan@uzovka
pro prenos souboru download WINscp
*/

/** Various tests.
 * @author Vaclav Trojan
 */
public class MyTest_0 extends XDTester {

	private final Stack<String> _stack = new Stack<String>();
	private String _item;

	public MyTest_0() {super();}

////////////////////////////////////////////////////////////////////////////////
// User methods used in X-definitions tests
////////////////////////////////////////////////////////////////////////////////
	public static boolean next(XXElement x) {
		MyTest_0 y = (MyTest_0) x.getUserObject();
		return !y._stack.empty() && (y._item = y._stack.pop()) != null;
	}
	public static boolean methodA(XXElement x) {
		return "A".equals(((MyTest_0) x.getUserObject())._item);
	}
	public static boolean methodB(XXElement x) {
		return "B".equals(((MyTest_0) x.getUserObject())._item);
	}
	public static boolean myErr(XXData x) {return false;}
	public static void exc() {throw new RuntimeException("bla...");}
	public static boolean cPar(XDContainer c) {
		return "A".equals(c.getXDNamedItemAsString("a"))
			&& "B".equals(c.getXDNamedItemAsString("b"));
	}
	public static XDParseResult kp(XXNode chkel, XDValue[] params) {
		XDContainer c = XDFactory.createXDContainer((XDContainer) params[2]);
		c.setXDNamedItem("minInclusive", params[0]);
		c.setXDNamedItem("maxInclusive", params[1]);
		try {
			XSAbstractParser d = new XSParseDecimal();
			d.setNamedParams(null, c);
			return d.check(null, chkel.getXMLNode().getNodeValue());
		} catch (Exception ex) {
			XDParseResult x = XDFactory.createParseResult("");
			x.error("", ex.toString());
			return x;
		}
	}

	private static void displayData(XMData x) {
		System.out.println(x.getName());
		System.out.println(x.getValueTypeName());
		String s = x.getRefTypeName();
		if (s != null) {
			XMVariableTable xv = x.getXDPool().getVariableTable();
			XMVariable v = xv.getVariable(s);
			if (v == null) {
				String xdName = x.getXMDefinition().getName();
				v = xv.getVariable(xdName + "#" + s);
				s = (v != null ? xdName + "#" : "UNDEFINED: ") + s;
			}
		}
		System.out.println(s);
		XDValue v = x.getParseMethod();
		if (v instanceof XDParser) {
			XDParser p = ((XDParser) v);
			System.out.println("\t" + p.parserName());
			XDContainer c = p.getNamedParams();
			for (XDNamedValue n : c.getXDNamedItems()) {
				String name = n.getName();
				System.out.print("\t\t" + name + "=");
				XDValue val = n.getValue();
				if (val.getItemId() == XDValue.XD_CONTAINER) {
					boolean first = true;
					for (XDValue y : ((XDContainer) val).getXDItems()) {
						if (first) {
							first = false;
						} else {
							System.out.print(",");
						}
						System.out.print(y.toString());
					}
					System.out.println();
				} else {
					System.out.println("\t" + n.toString());
				}
			}
		} else {
			// null -> string()!
			System.out.println("\t" + (v==null?"string":v.toString()));
		}
	}

	private static void printXMData(final XMData x) {
		System.out.println(x.getXDPosition()
			+ ", Parse: " + x.getParseMethod()
			+ ", Type: " + x.getValueTypeName()
			+ ", Ref: " + x.getRefTypeName()
			+ ", Default: " + x.getDefaultValue()
			+ ", Fixed: " + x.getFixedValue()
			+ ", occ: " + x.getOccurence());
	}

	private static void printXMData(final XMElement xe) {
		for (XMData x: xe.getAttrs()) {
			printXMData(x);
		}
		for (XMNode x: xe.getChildNodeModels()) {
			if (x.getKind() == XMNode.XMTEXT) {
				printXMData((XMData) x);
			} else if (x.getKind() == XMNode.XMELEMENT) {
				printXMData((XMElement) x);
			}
		}
	}
////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
//		T = true; // if true, only the first one test is invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		File tempDir = clearTempDir();
		XDPool xp;
		XComponent xc;
		String xdef;
		String xml;
		String s, json;
		Object o, x;
		XDDocument xd;
		Element el;
		XDOutput xout;
		StringWriter strw;
		Report rep;
		ArrayReporter reporter = new ArrayReporter();
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='a'>\n"+
"<xd:xon name='a'>\n" +
" [\"price()\"]\n"+
"</xd:xon>\n"+
"<xd:component>\n"+
"  %class bugreports.X_on %link #a;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef); // no property
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json = "[ \"p(123.45 CZK)\" ]";
			o = xd.jparse(json, reporter);
			System.out.println(((List) o).get(0));
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			System.out.println(((List) xc.toXon()).get(0));
			if (!XonUtils.xonEqual(o, x = xc.toXon())) {
				fail(XonUtils.toXonString(o, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
"<xd:component>%class bugreports.Csvxx %link a</xd:component>\n"+
" <xd:xon name='a'>\n"+
"    [ [%script=\"+\", \"int\", \"int\", \"string()\", \"boolean()\"] ]\n"+
" </xd:xon>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef); // no property
			genXComponent(xp, clearTempDir());
			xd = xp.createXDDocument();
			json =
"[\n" +
"  [1, 2, \"a\", true],\n" +
"  [null, 1, \"a\t\n\\\"b\", false],\n" +
"  [6, null, null, true],\n" +
"  [null, null, null, null]\n" +
"]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			if (!XonUtils.xonEqual(o, x = xc.toXon())) {
				fail(XonUtils.toXonString(o, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
//			xdef =
//"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' name='Example' root='root'\n>"+
//"  <xd:declaration scope='local'>\n" +
//"    type t1 int();\n" +
//"    boolean b = false;\n" +
//"    ParseResult x() {\n" +
//"       ParseResult p = t1();\n"+
//"       if (p.matches()) {\n"+
//"			b = true;\n"+
//"       }\n"+
//"       return p;\n" +
//"    }\n" +
//"  </xd:declaration>\n" +
//"\n" +
//"  <root a='x(); finally if (!b) error(b);' />\n" +
//"</xd:def>";
//			xml = "<root a='123'/>";
//			xp = compile(xdef);
//			assertEq(xml, parseJSON(xp, "Example", xml, reporter));
//			assertNoErrorwarnings(reporter);
//double d = Double.NaN;
//System.out.println(d);
//System.out.println(Double.parseDouble("NaN"));
//d = Double.POSITIVE_INFINITY;
//System.out.println(d);
//System.out.println(Double.parseDouble("Infinity"));
//d = Double.NEGATIVE_INFINITY;
//System.out.println(d);
//System.out.println(Double.parseDouble("-Infinity"));
//System.out.println("==================");


			xdef =
"<X:def xmlns:X='http://www.xdef.org/xdef/4.1' name='Example' root='root'\n>"+
"  <X:declaration scope='local'>\n" +
"    type t1 int();\n" +
"    type t2 starts(%argument='wsdl:');\n" +
"    uniqueSet u{t:t1};\n" +
"    ParseResult testAndCheck() {\n" +
"       ParseResult p = t2();\n" +
"       if (p) {\n" +
"         String s = p.getParsedString();\n" +
"         setText(s.substring(5));\n" +
"         p = u.t.CHKID();\n" +
"         setText(s);\n" +
"       }\n" +
"       return p;\n" +
"    }\n" +
"  </X:declaration>\n" +
"  <root>\n" +
"    <B X:script='*' b='u.t.ID;'/>\n" +
"    <C X:script='*' c='testAndCheck();'/>\n" +
"  </root>\n" +
"</X:def>";
			xp = compile(xdef);
			xml =
"<root>\n" +
"  <B b='123'/>\n" +
"  <B b='125'/>\n" +
"  <C c='wsdl:123'/>\n" +
"  <C c='wsdl:124'/> <!-- error: 124 is not in the set u -->\n" +
"  <C c='wsdx:125'/> <!-- error: fails t2 (wsdx) -->\n" +
"  <C c='wsdl:125'/>\n" +
"</root>";
			assertEq(xml, parse(xp, "Example", xml, reporter));
			assertEq(2, reporter.getErrorCount());
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.1'>\n"+
"<xd:def name='A'>\n"+
" <xd:declaration scope='global'>Parser p;uniqueSet u{x:p}</xd:declaration>\n"+
"</xd:def>\n"+
"<xd:def name='B' root='A'>\n"+
"  <A id=\"? xdType(); onTrue p = getParsedValue();\" />\n"+
"</xd:def>\n"+
"</xd:collection>";
			compile(xdef);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<X:def xmlns:X='" + _xdNS + "' root='a|root'>\n"+
"  <X:declaration scope='local'>\n"+
"    BNFGrammar g = new BNFGrammar('\n"+
"      x ::= S? [0-9]+\n"+
"      y ::= S? [a-zA-Z]+\n"+
"      S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      z ::= x | y\n"+
"    ');\n"+
"    type t1 int();\n"+
"    type t2 starts(%argument='wsdl:');\n"+ // 10
"    uniqueSet u{t:t1};\n"+
"    ParseResult testAndCheck() {\n"+
"       if (t2().matches()) {\n"+
"         String s = getParsedValue();\n"+
"         setText(s.substring(5));\n"+
"         return u.t.CHKID();\n"+
"       }\n"+
"       return getParsedResult();\n"+
"    }\n"+
"  </X:declaration>\n"+
"  <root>\n"+
"    <B X:script='*' b='u.t.ID;'/>\n"+
"    <C X:script='*' c='testAndCheck();'/>\n"+
"  </root>\n"+
"  <a>\n"+
"    <X:choice>\n"+
"      <b X:script=\"1..; match g.rule('x').validate(@n);\"\n" +
"         n                =\"string()\"/>\n" +
"      <b X:script=\"1..; match g.rule('y').validate(@n);\"\n" +
"         n                =\"string()\"/>\n" +
"    </X:choice>\n"+
"  </a>\n"+
"</X:def>";
			xml = "<a><b n='1'/><b n='456'/></a>";
			xp = XDFactory.compileXD(null, xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b n='a'/><b n='Def'/></a>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a><b n='123'/><b n='Def'/></a>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<root><B b='123'/><C c='wsdl:123'/></root>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml =
"<root>\n<B b='123'/>\n<C c='wsdl:124'/>\n<C c='wsdl:123'/>\n</root>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml = "<root>\n<B b='123'/>\n<C c='wsdx:123'/>\n</root>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"Example\" root=\"root\" >\n" +
" <xd:declaration>\n"+
"   ParseResult xx() {\n"+
"     ParseResult result = xdatetime('dd.MM.yyyy');\n"+
"     int year = result.matches() ? result.datetimeValue().getYear() : 0;\n"+
"     if (year GT 2020) {\n"+
"       result.error('xx001', 'Chybny rok: &amp;{0}', '' + year);\n"+
"     }\n"+
"     return result;\n"+
"   }\n"+
" </xd:declaration>\n"+
"  <root a=\"int();\" >\n" +
"    <b xd:script=\"*\" >\n" +
"      ? xx()\n" +
"    </b>\n" +
"  </root>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml = "<root a=\"123\" >\n" +
"  <b>22.09.2020</b>\n" +
"  <b/>\n" +
"</root>";
			xd = xp.createXDDocument();
			parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
" <xd:declaration scope='global'>\n"+
" type p int;\n"+
" uniqueSet u{x:int};\n"+
" void x(Parser p, Parser q) {\n"+
"   outln('xx ' + p + ', ' + q);\n"+
" }\n"+
" </xd:declaration>\n"+
//"  <A id=\"? union(%item=[u.x.ID, p]); finally x(int, ID)\" />\n"+
//"  <A id=\"? union(%item=[eq('ab'), int]); finally x(int, u.x)\" />\n"+
"  <A id=\"? union(%item=[u.x]);\" />\n"+ // must be check if it is parser
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(null, xdef);
//			xp.displayCode();
			xml =
"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
"\n" +
"\n" +
"<A id='123'/>";
			xd = xp.createXDDocument();
			xd.xparse(xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (SRuntimeException ex) {
			if (!"XDEF474".equals(ex.getMsgID())) fail(ex);
		} catch (Exception ex) {fail(ex);}
if(T)return;
//		try {
//			xdef =
//"<xd:def xmlns:xd    =\"http://www.xdef.org/xdef/4.1\"\n" +
//"        impl-version=\"3.0.x_dr\" impl-date=\"2020-05-25\"\n" +
//"        xd:name     =\"CodeBook\"\n" +
//"        xd:root     =\"CodeBook\">\n" +
//"\n" +
//"<xd:declaration scope=\"local\">\n" +
//"     int  i;\n" +
//"     int  AttrCount;\n" +
//"     type attrValue           string(1,511);\n" +
//"     type description         string(1,511);\n" +
//"     type name                string(1,30);\n" +
//"     type version             string(1,20);\n" +
//"     type xdate               xdatetime('yyyy-MM-dd');\n" +
//"     uniqueSet nodes        {Node:   name(); var int AttrCount};\n" +
//"     uniqueSet attrs        {Node:   name(); Attr:  name()};\n" +
//"     boolean xxx() {return nodes.Node.ID() AAND attrs.Node();}\n" +
//"     boolean yyy() {return nodes.Node.IDREF() AAND attrs.Node();}\n" +
//"     boolean zz1() {return nodes.Node.ID() AAND attrs.Node();}\n" +
//"     boolean zz2() {return nodes.Node.IDREF() AAND attrs.Node();}\n" +
//"\n" +
//"</xd:declaration>\n" +
//"\n" +
//"<CodeBook>\n" +
//"    <Def xd:script=\"0..1; ref Def\"/>       <!-- Definice číselníku -->\n" +
//"    <Values xd:script=\"0..1; ref Values\"/> <!-- Hodnoty číselníku  -->  \n" +
//"</CodeBook>\n" +
//"<!-- Definice -->\n" +
//"<Def Name        =\"name()\"\n" +
//"     Version     =\"version()\"\n" +
//"     Description =\"description()\">\n" +
//"  <Node  xd:script=\"1..; ref NodeDef\"/>\n" +
//"</Def>\n" +
//"\n" +
//"<NodeDef Name=\"zz1(); onTrue{nodes.AttrCount=0;}\">\n" +
//"   <Attr xd:script=\"1..; ref AttrDef\"/>\n" +
//"   <Node xd:script=\"0..; ref NodeDef\"/>\n" +
//"</NodeDef>\n" +
//"\n" +
//"<AttrDef Name=\"attrs.Attr.ID();\n"+
//"                onTrue {nodes.AttrCount=nodes.AttrCount+1;}\"/>\n" +
//"<!-- Hodnoty -->\n" +
//"<Values>\n" +
//"  <Node xd:script=\"1..; ref NodeValue\"/>\n" +
//" </Values>\n" +
//"\n" +
//"<NodeValue xd:script=\"var {String x; int NodeAttrCount;}\n" +
//"              /*finally {AttrCount=NodeAttrCount; attrs.Node=x;}*/\"\n"+//=====
//"           Name=\"zz2();\n" +
//"              onTrue {NodeAttrCount=nodes.AttrCount; AttrCount=NodeAttrCount;\n" +
//"           x=nodes.Node;}\">\n" +
//"   <xd:sequence xd:script=\"1..\">\n" +
//"     <Row  xd:script=\"1..; ref RowValues\"/>\n" +
//"     <Node xd:script=\"0..; ref NodeValue\"/>\n" +
//"   </xd:sequence>\n" +
//"</NodeValue>\n" +
//"\n" +
//"<RowValues xd:script=\"onStartElement {i=0;}\n" +
//"             finally {if (AttrCount!=i) error('chybí atribut: '+AttrCount);\">\n" +
//"    <Attr xd:script=\"1..; ref AttrValue\"/>\n" +
//"</RowValues>\n" +
//"\n" +
//"<AttrValue Name=\"attrs.Attr.IDREF(); onTrue {i++}\">\n" +
//"  attrValue()\n" +
//"</AttrValue>\n" +
//"</xd:def>";
//			xp = compile(xdef);
//		} catch (Exception ex) {fail(ex);}
//if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n"+
"    uniqueSet r {a: int();};\n"+
"     type s string(1,*);\n" +
"     type T4_str enum(%argument=['a','b']);\n"+
"	  type T4_int int(1, 10);\n"+
"  </xd:declaration>\n"+
"  <A a = ''>\n"+
"  <X xd:script='var uniqueSet r {a: string();};'>\n"+
"    ? union(%item=[T4_str, T4_int]);\n"+
"    <B xd:script='*' a='? r.a.ID' b='? r.a.ID()' c='? r.a' d='? r.a()'/>\n"+
"    ? list(%item=T4_int);\n"+
"    <C xd:script='*;' a='? r.a.CHKIDS' b='? r.a.CHKIDS()'/>\n"+
"    <D xd:script='*;' a='? T4_str' b='? T4_str()'  c='? s' d='? s()'/>\n"+
"    ? r.a.CHKIDS\n"+
"  </X>\n"+
"  </A>\n"+
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(xdef);
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A a='x'><X><B a='x'/><C a='x'/></X></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a='? t()'/>\n"+
"  <xd:declaration>\n"+
"     type t enum(%argument=['a','b']);\n"+
//"     boolean t() {return enum(%argument=['a','b']).parseJSON().matches();};\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			XMDefinition xmdef = xp.getXMDefinition("");
			XMElement xmel = xmdef.getModel(null, "a");
			XMData xmd = xmel.getAttr("a");
System.out.println("ParserType: "+CompileBase.getTypeName(xmd.getParserType()));
System.out.println("ValueTypeName: " + xmd.getValueTypeName());
System.out.println("RefTypeName: " + xmd.getRefTypeName());
System.out.println("ParserName: " + xmd.getParserName());
			assertEq(xmd.getParserType(),  XDValue.XD_STRING);
			assertEq(xmd.getRefTypeName(), "t");
			xml = "<a a='b' />";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'/>\n" +
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
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"   <a xd:text='* string()'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
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
"   <xd:declaration>int i = 0;</xd:declaration>\n" +
"   <a xd:text='* string(); create ++i'>\n" +
"     <b xd:script='*'/>\n" +
"   </a>\n" +
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
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root = 'a'>\n"+
"  <a xd:textcontent=\"string();\n" +
"       onTrue out('T:'+getText()); finally out('f:'+getText());\">\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>t1<b/>t2<c/>t3</a>";
			StringWriter bos = new StringWriter();
			xout = XDFactory.createXDOutput(bos, false);
			xd = xp.createXDDocument();
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			xout.close();
			assertNoErrorwarnings(reporter);
			assertEq("T:t1t2t3f:t1t2t3", bos.toString());
			assertEq("<a>t1<b/>t2<c/>t3</a>",
				KXmlUtils.nodeToString(xd.getElement()));

			bos = new StringWriter();
			xout = XDFactory.createXDOutput(bos, false);
			xd = xp.createXDDocument();
			xd.setStdOut(xout);
			xd.setXDContext(KXmlUtils.parseXml(xml).getDocumentElement());
			el = xd.xcreate("a", null);
			xout.close();
			assertEq("<a><b/><c/>t1t2t3</a>", KXmlUtils.nodeToString(el));
			assertEq("T:t1t2t3f:t1t2t3", bos.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n"+
"    uniqueSet r {a: int();};\n"+
"     type s string(1,*);\n" +
"     type T4_str enum(%argument=['a','b']);\n"+
"	  type T4_int int(1, 10);\n"+
"  </xd:declaration>\n"+
"  <A a = ''>\n"+
//"  <X>\n"+
"  <X xd:script='var uniqueSet r {a: string();};'>\n"+
"    ? union(%item=[T4_str, T4_int]);\n"+
"    <B xd:script='*' a='? r.a.ID' b='? r.a.ID()' c='? r.a' d='? r.a()'/>\n"+
"    ? list(%item=T4_int);\n"+
"    <C xd:script='*;' a='? r.a.CHKIDS' b='? r.a.CHKIDS()'/>\n"+
"    <D xd:script='*;' a='? T4_str' b='? T4_str()'  c='? s' d='? s()'/>\n"+
"    ? r.a.CHKIDS\n"+
"  </X>\n"+
"  </A>\n"+
"</xd:def>";
//			xp = XDFactory.compileXD(null, xdef);
			xp = compile(xdef);
			printXMData(xp.getXMDefinition().getModel(null, "A"));
			xml = "<A a='x'><X><B a='x'/><C a='x'/></X></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='T'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet r {a: string(); b: int()};\n" +
"  </xd:declaration>\n" +
"  <T>\n" +
"    <R xd:script='*' A='r.a' B='r.b.ID()'/>\n" +
"  </T>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<T>\n" +
"  <R A='xx' B='a'/>\n" +
"  <R A='yy' B='a'/>\n" +
"</T>";
			parse(xd, xml, reporter);
			System.out.println(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"  <xd:declaration>\n" +
"    uniqueSet u {var Parser x; var int y; a: string(); var String z}\n" +
"  </xd:declaration>\n" +
"  <a>\n" +
"    <DefParams>\n" +
"       <Param xd:script='*;'\n" +
"          Name='u.a.ID();'\n" +
"          Type='xdType(); onTrue u.x=getParsedValue();\n" +
"                          onFalse u.y=99;\n" +
"                          finally out(u.y)'/>\n" +
"    </DefParams>\n" +
"    <Params xd:script=\"*; init u.checkUnref()\">\n" +
"       <Param xd:script='*;'\n" +
"              Name='u.a.CHKID();'\n" +
"              Value='u.x; onTrue out(u.x); '/>\n" +
"    </Params>\n" +
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml =
"<a>\n" +
"  <DefParams>\n" +
"    <Param Name=\"Jmeno\" Type=\"string()\" />\n" +
"    <Param Type=\"dec()\" Name=\"Vyska\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Type=\"xdatetime('dd.MM.yyyy')\" />\n" +
"  </DefParams>\n" +
"  <Params>\n" +
"    <Param Name=\"Jmeno\" Value=\"Jan\"/>\n" +
"    <Param Name=\"Vyska\" Value=\"14.8\"/>\n" +
"    <Param Name=\"DatumNarozeni\" Value=\"01.02.1987\"/>\n" +
"  </Params>\n" +
"  <Params>\n" +
"    <Param Value=\"14.8a\" Name=\"Vyska\"/>\n" +
"  </Params>\n" +
"</a>";
			strw = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(strw, false));
			parse(xd, xml, reporter);
			assertEq("stringdecxdatetime", strw.toString());
			assertTrue((rep = reporter.getReport()) != null
				&& "XDEF804".equals(rep.getMsgID()), rep);
			assertTrue((rep = reporter.getReport()) != null
				&& "XDEF524".equals(rep.getMsgID()), rep);
			assertNull(rep = reporter.getReport(), rep);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='A'>\n"+
"  <xd:declaration scope='global'>\n" +
"    type name string(1, 128);\n" +
"  </xd:declaration>\n" +
"  <A>name()</A>" +
"</xd:def>\n";
			File f = new File(tempDir, "x.xdef");
			SUtils.writeString(f, xdef);
			xp = compile(f);
			xml = "<A>1?xyz</A>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			s = "D:/cvs/DEV/java/xdef31/resources/"
				+ "cz/syntea/xdef/impl/compile/XdefOfXdef*.xdef";
			// filepath
			xp = XDFactory.compileXD(null, s);//with wildcards
			xp = XDFactory.compileXD(null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='" + s + "'/>");
			xp = XDFactory.compileXD(null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='" + s + "'/>");
			// URL (file:/filepath)
			xp = XDFactory.compileXD(null, "file:/" + s);
			xp = XDFactory.compileXD((Properties) null,
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='X' xd:include='file:/" + s + "'/>");
			xp = XDFactory.compileXD((Properties) null,
"<xd:collection xmlns:xd='" + _xdNS + "' xd:include='file:/" + s + "'/>");
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			reporter.clear();
			xp = XDFactory.compileXD(reporter, (Properties) null,
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration scope='global'>\n"+
"    int i;\n"+
"    int x(){return 0;}\n"+
"    type t int();\n"+
"  </xd:declaration>\n"+
"  <a/>\n"+
"</xd:def>",
"<xd:declaration xmlns:xd='"+_xdNS+"'>\n"+
"  int i; int x(){return 0;} type t int();\n"+
"</xd:declaration>");
			if (reporter.errorWarnings()) {
				s = reporter.printToString();
				assertTrue(s.contains("XDEF450"), s);
				assertTrue(s.contains("XDEF462"), s);
				assertTrue(s.contains("XDEF470"), s);
			} else {
				fail("Error not reported");
			}
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"A\">\n" +
"<xd:declaration scope = 'local'>\n" +
"type t string(1,10,%pattern=[\"[a-z]+\", \"d+\"], %whiteSpace=\"replace\");\n"+
"</xd:declaration>\n" +
"  <A a='?'\n" +
"     b='? t'\n" +
"     c='? decimal(1,2,%fractionDigits=5, %totalDigits=11)'\n" +
"     d='? boolean()'\n" +
"     e='? date(%enumeration=[\"2010-01-01\",\"2010-01-31\"])' >\n" +
"   ? t;\n"+
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			XDefinition xmd = (XDefinition) xp.getXMDefinitions()[0];
			XMElement xme = xmd.getModel(null, "A");
			for (XMNode xn : xme.getChildNodeModels()) {
				if (xn instanceof XMData) {
					displayData((XMData) xn);
				}
			}
			xml = "<A a='a' b='bb' c='1' d='true'></A>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xd:name=\"Test\" xd:root=\"json\">\n" +
"  <xd:xon name='json'>{\"A\":\"int();\"}</xd:xon>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef,
"<xd:def xmlns:xd='" + _xdNS + "'\n" +
"   xd:name=\"Test1\" xd:root=\"Test#json\"/>"
			);
			xd = xp.createXDDocument("Test1");
			s = "{\"A\":1234}";
			assertTrue(XonUtils.xonEqual(xd.jparse(s, reporter),
				XonUtils.parseJSON(s)));
			assertNoErrorwarnings(reporter);
			reporter.clear();
			s = "{\"A\":\"abc\"}";
			xd.jparse(s, reporter);
			assertErrors(reporter);
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" xd:root=\"A\">\n" +
"    <A xd:script=\"create from('/*/*[last()]');\"\n" +
"       a=\"required string()\" >\n" +
"        <B xd:script=\"occurs 1..\"\n" +
"           b=\"required string()\">\n" +
"            <xd:choice script='occurs +'>\n" +
"               <X xd:script=\"ref X\" />\n" +
"               <Y xd:script=\"ref Y\" />\n" +
"               <Z xd:script=\"ref Z\" />\n" +
"            </xd:choice>\n" +
"        </B>\n" +
"    </A>\n" +
"    <X x=\"required string()\"/>\n" +
"    <Y x=\"required string()\"/>\n" +
"    <Z x=\"required string()\"/>\n" +
"\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xml =
"<A_ IdFlow=\"181131058\">\n" +
"    <XXX IdDefPartner=\"163\"/>\n" +
"    <YYY DruhSouboru=\"W1A\"/>\n" +
"    <A a=\"Pojistitel_SLP\">\n" +
"        <B b=\"b\">\n" +
"            <Z x=\"3\" />\n" +
"            <Z x=\"4\" />\n" +
"        </B>\n" +
"   </A>\n" +
"</A_>";
			xd.setXDContext(xml);
			el = create(xd, "A", reporter);
			System.out.println(KXmlUtils.nodeToString(el, true));
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var {type x int(2,3); uniqueSet u {p: x; q: t};}'\n"+
"    a='? t()'\n"+
"    b='? x()'\n"+
"    c='? dateTime()'\n"+
"    d='? u.p.ID()'\n"+
"    e='? u.q.ID()'\n"+
"    f='? integer()'\n"+
"    g='? string(%enumeration=[\"a\",\"b\"])'\n"+
"    h='? long(%enumeration=[2,3])'\n"+
"    i='? s' />\n"+
"  <xd:declaration>\n"+
"     type s string(1,*);\n" +
"     type t enum(%argument=['a','b']);\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			xp = compile(xdef);
			XMDefinition xmdef = xp.getXMDefinition("");
			XMElement xmel = xmdef.getModel(null, "a");
			XMData xmd = xmel.getAttr("a");
			assertEq(xmd.getParserType(),  XDValue.XD_STRING);
			System.out.println("RefTypeName: " + xmd.getRefTypeName());
			assertEq(xmd.getRefTypeName(), "t");
			assertEq(xmd.getParserName(), "enum");
			o = xmd.getParseParams();
			System.out.println(o);
			System.out.println(xmel.getAttr("a"));
			System.out.println(xmel.getAttr("b"));
			System.out.println(xmel.getAttr("c"));
			System.out.println(xmel.getAttr("d"));
			System.out.println(xmel.getAttr("e"));
			System.out.println(xmel.getAttr("f"));
			System.out.println(xmel.getAttr("g"));
			System.out.println(xmel.getAttr("h"));
			System.out.println(xmel.getAttr("i"));
			xml = "<a/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
" <xd:declaration scope='global'>\n"+
"   <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
" </xd:declaration>\n"+
" <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
" <xd:declaration scope='local'>\n"+
"  <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
" </xd:declaration>\n"+
" <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
"  <xd:macro name=\"moreAll\">options moreAttributes,moreElements;\n"+
"    </xd:macro>\n"+
"</xd:declaration>\n" +
"<xd:def root='A'>\n"+
"  <A xd:script=\"${moreAll}\"/>\n"+
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			parse(xp, "", "<A a = 'a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrorwarnings(reporter);
			xp = compile(new String[] {
"<xd:declaration xmlns:xd='" + XDConstants.XDEF32_NS_URI + "'>\n"+
" <xd:macro name='moreAll'>options moreAttributes,moreElements;</xd:macro>\n"+
"</xd:declaration>",
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A xd:script='${moreAll}'/>\n"+
"</xd:def>"
			});
			parse(xp, "", "<A a='a'><XXX xx='xx'>xx</XXX></A>", reporter);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='B' root='A'>\n"+
"  <xd:declaration> Parser p; uniqueSet u {x: p} </xd:declaration>\n"+
"  <A id=\"? xdType(); onTrue p = getParsedValue();\" />\n"+
"</xd:def>\n"+
"</xd:collection>";
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(xdef);
			oos.writeObject(compile(xdef));
			oos.writeObject("<A id=\"string()\"/>");
			oos.close();
			ByteArrayInputStream bais =
				new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			ois.readObject();
			xp = (XDPool) ois.readObject();
			xml = (String) ois.readObject();
			ois.close();
			xd = xp.createXDDocument("B");
			el = KXmlUtils.parseXml(xml).getDocumentElement();
			assertEq(xml, xd.xparse(el, null));
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='a'>\n"+
"<a>required { switch(getText()) {\n"+
"                case '1': return true;\n"+
"                default: return false;\n"+
"              };\n"+
"            }\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			fail("deprecation not reported");
		} catch (Exception ex) {}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='a'>\n"+
"<xd:declaration>\n"+
" type t { switch(getText()) {\n"+
"            case '1': return true;\n"+
"            default: return false;\n"+
"          };\n"+
"        }\n"+
"</xd:declaration>\n"+
"<a>required t; </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			fail("deprecation not reported");
		} catch (Exception ex) {}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF40_NS_URI + "' root='a'>\n"+
"<xd:declaration>\n"+
" boolean t() { switch(getText()) {\n"+
"                 case '1': return true;\n"+
"                 default: return false;\n"+
"               };\n"+
"        }\n"+
"</xd:declaration>\n"+
"<a>required t(); </a>\n"+
"</xd:def>";
			xml = "<a>1</a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= [0-9]+\n"+
"      S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      intList ::= integer (S? \",\" S? integer)* S?\n"+
"    ');\n"+
"    type intList rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"<a>required intList()</a>\n"+
"</xd:def>";
			xml = "<a>123, 456, 789</a>";
			assertEq(xml, parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally out(parseInt(\"12x3\"))'>\n" +
"<xd:mixed>\n"+
"    match getOccurrence() == 0; ? string(); \n" +
"    <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"    match getOccurrence() == 0; string(); \n" +
"</xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("", strw.toString());
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='finally out(parseInt(\"12x3\"))'>\n" +
"    <xd:mixed>\n"+
"       match getOccurrence() == 0; ? string(); \n" +
"       <b xd:script = \"occurs 0..2;\" x = \"fixed 'S'\"/>\n" +
"       match getOccurrence() == 0; string(); \n" +
"    </xd:mixed>\n"+
"  </a>\n" +
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b x='S'/>t2<b x='S'/></a>";
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			assertEq(xml, parse(xd, xml, reporter));
			assertEq("", strw.toString());
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:textcontent='int(); create 123; option cdata;'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>123</a>";
			el = parse(xdef, "", xml, reporter);
			assertEq(xml, el);
			strw = new StringWriter();
			KXmlUtils.writeXml(strw,
				null, //encoding
				el,   //element to write
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[123]]></a>",strw.toString());
			assertNoErrorwarnings(reporter);
			el = create(xd, "a", reporter);
			assertNoErrorwarnings(reporter);
			strw = new StringWriter();
			KXmlUtils.writeXml(strw,
				null, //encoding
				el,   //element to write
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[123]]></a>",strw.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration>\n"+
"    external method boolean bugreports.MyTest_0.next(XXElement xyz_);\n"+
"    external method boolean bugreports.MyTest_0.methodA(XXElement $_x);\n"+
"    external method boolean bugreports.MyTest_0.methodB(XXElement x);\n"+
"  </xd:declaration>\n"+
"  <A>\n"+
"    <xd:sequence xd:script='occurs *; create next()'>\n"+
"      <a xd:script=\"occurs 0..1; create methodA();\n"+
"                     finally outln(getElement());\n"+
"                     forget\"/>\n"+
"      <b xd:script=\"occurs 0..1; create methodB();\n"+
"                     finally outln(getElement());\n"+
"                     forget\"/>\n"+
"    </xd:sequence>\n"+
"  </A>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			_stack.push("A");
			_stack.push("A");
			_stack.push("B");
			_stack.push("A");
			_stack.push("B");
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setUserObject(this);
			assertEq("<A><b/><a/><b/><a/><a/></A>", create(xd, "A", null));
			assertEq("<b/>\n<a/>\n<b/>\n<a/>\n<a/>\n", strw.toString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"   ParseResult licheCislo() {\n"+
"      ParseResult p = int();\n"+
"      if (p.intValue() % 2 == 0) {\n"+
"         p.error(\"Cislo musí byt liche!\");\n"+
"      }\n"+
"      return p;\n"+
"   }\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo(); onTrue outln(getParsedInt()==1);\"/>"+
"</xd:def>\n";
			strw = new StringWriter();
			parse(xdef, "", "<a a='3'></a>", reporter, strw, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("false\n", strw.toString());
			strw = new StringWriter();
			parse(xdef, "", "<a a='2'></a>", reporter, strw, null, null);
			assertErrors(reporter);
			assertEq("", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  ParseResult lc() {\n"+
"    ParseResult r = int();\n"+
"    int i = r.intValue();\n"+
"    if (i % 2 == 0) {\n"+
"      r.error(\"Cislo musí byt liche!\");\n"+
"    }\n"+
"    return r;\n"+
"  }\n"+
"   type licheCislo lc;\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo()\"/>"+
"</xd:def>\n";
			parse(xdef, "", "<a a='1'></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a a='2'></a>", reporter);
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"   boolean licheCislo() {\n"+
"     return parseInt(getText()) % 2 != 0\n"+
"       ? true : error(\"Cislo musí byt liche!\");\n"+
"   }\n"+
"</xd:declaration>\n"+
"<a a=\"required licheCislo()\"/>"+
"</xd:def>\n";
			parse(xdef, "", "<a a='1'></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a a='2'></a>", reporter);
			assertErrors(reporter);
			xdef = // var in model with reference
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var {int j, i=123;}'>\n"+
"    <xd:choice xd:script=\"?\">\n"+
"      <O xd:script=\"occurs 0..2;\n"+
"           finally if (i != 123) throw new Exception('error');\"/>\n"+
"      <A xd:script=\"occurs 1;\n"+
"            finally if (i != 123) throw new Exception('error');\">\n"+
"        <X xd:script=\"optional; ref Y;\"/>\n"+
"      </A>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"  <Y xd:script=\"var {String t='T';}\"/>\n"+
"</xd:def>";
			parse(xdef, "", "<a><O/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a><O/><O/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a><A><X/></A></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xdef, "", "<a><O/><O/><O/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xdef, "", "<a><O/><A/></a>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		boolean chkSynteax = getChkSyntax();
		try {
			setChkSyntax(false);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
" external method {\n"+
"   void bugreports.MyTest_0.p3(Long,) as p;\n"+
"   boolean bugreports.MyTest_0.p3(Long) as;\n"+
"   long bugreports.MyTest_0.p3(Long) ppp;\n"+
"   double bugreports.MyTest_0.p3 Long);\n"+
"   String bugreports.MyTest_0.p2(String, Long as p2;\n"+
"   bugreports.MyTest_0.p2(String, Long)\n"+
"   bugreports.MyTest_0.p3(XDValue[) as pp\n"+
" }\n"+
"</xd:declaration>\n"+
"<a id = \"int; onTrue pp('a', 5)\" xd:script = 'finally p(3)'/>\n"+
"</xd:def>\n";
			compile(xdef);
			fail("Error not recognized");
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s == null || s.indexOf("SYS012") < 0
				|| s.indexOf("XDEF410") < 0 || s.indexOf("XDEF362") < 0
				|| s.indexOf("XDEF220") < 0 || s.indexOf("XDEF443") < 0
				|| s.indexOf("XDEF412") < 0 || s.indexOf("XDEF225") < 0
				|| s.indexOf("XDEF228") < 0) {
				fail(ex);
			}
		}
		setChkSyntax(chkSynteax);
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a>\n"+
"  <b>required string()</b>\n"+
"  <xd:sequence occurs = '*'>\n"+
"    required string();\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"  <b>required string()</b>\n"+
"  <xd:sequence occurs = '*'>\n"+
"    required string();\n"+
"    <c/>\n"+
"  </xd:sequence>\n"+
"</a>\n"+
"</xd:def>\n";
			xml = "<a><b>0</b>1<c/>2<c/><b>3</b>4<c/>5<c/></a>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, xml);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:macro name='m' p='?'>init out('i#{p}');finally out('f#{p}')</xd:macro>\n"+
"  <a xd:script=\"${m(p='a')}\">\n"+
"    <xd:choice>\n"+
"      <xd:sequence>\n"+
"        <c xd:script=\"1; ${m(p='c1')}; create from('/a/c')\"/>\n"+
"        <d xd:script=\"?; ${m(p='d1')}; create from('//a/d')\"/>\n"+
"        <e xd:script=\"?; ${m(p='e1')}\"/>\n"+
"      </xd:sequence>\n"+
"      <xd:sequence>\n"+
"        <d/>\n"+
"        <c xd:script=\"occurs ?; ${m(p='c2')}\"/>\n"+
"        <e xd:script=\"occurs *; ${m(p='e2')}\"/>\n"+
"      </xd:sequence>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a/>";
			xp = compile(xdef);
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			if (reporter.getErrorCount() == 0) {
				fail("error not reported");
			} else {
				assertTrue(reporter.getErrorCount() == 1 &&
					reporter.getReport().getModification().indexOf("/a") > 0,
					reporter.printToString());
			}
			assertEq("iafa", strw.toString());
			assertEq(el, xml);
			xml = "<a><c/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrorwarnings(reporter);
			assertEq("iaic1fc1fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrorwarnings(reporter);
			assertEq("iafa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/><c/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrorwarnings(reporter);
			assertEq("iaic2fc2fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><d/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrorwarnings(reporter);
			assertEq("iaie2fe2fa", strw.toString());
			assertEq(el, xml);
			xml = "<a><c/><d/><e/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount()==1&&s.indexOf("XDEF501")>=0,s);
			assertEq("iaic1fc1id1fd1ie1fe1fa", strw.toString());
			assertEq(el, "<a><c/><d/><e/></a>");
			xml = "<a><c/><d/><d/><e/><e/></a>";
			strw = new StringWriter();
			el = parse(xp, "", xml, reporter, strw, null,null);
			assertNoErrorwarnings(reporter);
			assertEq("iaic1fc1id1fd1ie2fe2ie2fe2fa", strw.toString());
			assertEq(el, xml);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='A'>\n"+
"<xd:macro name='m' p='?'>init out('i#{p}');finally out('f#{p}')</xd:macro>\n"+
" <A xd:script=\"1;${m(p='A')}\">\n"+
"   <xd:sequence xd:script=\"1;${m(p='SQ')}\">\n"+
"      <B xd:script=\"1;${m(p='B')}; create from('/A/B')\"/>\n"+
"      <xd:any xd:script=\"1; ${m(p='X')}; create from('/A/C')\"/>\n"+
"   </xd:sequence>\n"+
"   <D xd:script=\"1;${m(p='D')}; create from('/A/D')\"/>\n"+
" </A>\n"+
"</xd:def>";
			xml = "<A><B/><C/><D/></A>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setXDContext(xml);
			el = create(xd, "A", reporter);
			xout.close();
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			assertEq("iAiSQiBfBiXfXfSQiDfDfA", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			el = parse(xd, xml, reporter);
			strw.close();
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			assertEq("iAiSQiBfBiXfXfSQiDfDfA", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script='init out(\"ia\");finally out(\"fa\")'>\n"+
"   <xd:sequence xd:script='1;init out(\"im\");finally out(\"fm\")'>\n"+
"      <b xd:script='init out(\"ib\");finally out(\"fb\")'/>\n"+
"      <xd:any xd:script='1; init out(\"ix\");finally out(\"fx\")'/>\n"+
"   </xd:sequence>\n"+
"   <c xd:script='init out(\"ic\");finally out(\"fc\")'/>\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a><b/><x/><c/></a>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			xd.setXDContext(xml);
			el = xd.xcreate("a", reporter);
			xout.close();
			assertNoErrorwarnings(reporter);
			assertEq("<a><b/><b/><c/></a>", el);
			assertEq("iaimibfbixfxfmicfcfa", strw.toString());
			xd = xp.createXDDocument();
			strw = new StringWriter();
			xout = XDFactory.createXDOutput(strw, false);
			xd.setStdOut(xout);
			el = parse(xd, xml, reporter);
			strw.close();
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			assertEq("iaimibfbixfxfmicfcfa", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='a' root='a'>\n"+
"    <a>\n"+
"        <xd:mixed xd:script='?;'>\n"+
"            <d xd:script='1; create from(\"/a/d\")'/>\n"+
"        </xd:mixed>\n"+
"    </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a></a>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			el = xd.xcreate("a", reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:mixed>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"      <b/>\n"+
"      <c/>\n"+
"    </xd:mixed>\n"+
"    <z/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/><p/><q/><z/></a>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <xd:mixed><p/><q/></xd:mixed>\n"+
"      <b/><c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><p/><q/><b/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><q/><p/><b/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><p/><p/><b/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"      <c/>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><p/><q/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><b/><q/><p/><c/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			assertErrors(reporter);
			parse(xp, "", "<a><b/><p/><p/><c/></a>", reporter);
			assertErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a>\n"+
"    <xd:sequence>\n"+
"      <b/>\n"+
"      <c/>\n"+
"      <xd:mixed> <p/> <q/> </xd:mixed>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a><b/><c/><p/><q/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><b/><c/><q/><p/></a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a><b/><c/></a>", reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration> int $typ = 1; </xd:declaration>\n"+
"  <a>\n"+
"    <xd:choice xd:script='occurs 2;create 2'>\n"+
"      <b xd:script=\"*;create $typ EQ 1; finally $typ = 2;\"/>\n"+
"      <c xd:script=\"create ++$typ EQ 3\"/>\n"+
"      <d xd:script=\"create $typ EQ 3\"/>\n"+
"    </xd:choice>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef); //vytvořeni ze zdroju
			create(xp, "", "a", reporter);
			assertFalse(xd.errorWarnings(), reporter.printToString());
		} catch (Exception ex) {fail(ex);}
if(T){return;}
		try {
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' xd:root = \"err\" >\n"+
"<xd:declaration>\n"+
"  external method void bugreports.MyTest_0.exc()\n"+
"</xd:declaration>\n"+
"<err xd:script = \"finally exc();\">\n"+
"</err>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<err/>", reporter);
			fail("Exception not thrown");
		} catch (Exception ex) {
			s = ex.getMessage();
			assertTrue(s.indexOf("bla...") >= 0, s);
		}
if(T){return;}
		try {//union with base
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method boolean bugreports.MyTest_0.cPar(XDContainer c);\n"+
"  NamedValue n = %y = 'Y';\n"+
"  Container m1 = [n, %a='A',%b='B', 1];\n"+
"  Container m2 = new Container();\n"+
"  Container c = [1, 'x'];\n"+
"</xd:declaration>\n"+
" <a>\n"+
"   string;\n"+
"   onTrue {\n"+
"     m2.setNamedItem(new NamedValue('x', 1));\n"+
"     m2.setNamedItem(n); m2.setNamedItem('z', 'Z');\n"+
"     m2.setNamedItem(n = %q = 'Q');\n"+
"     setText(m1.getNamedItem('a') + ',' + m1.getNamedItem('b') +\n"+
"       ',' + m2.getNamedItem('x') +','+ m2.getNamedItem('y') + ',' + \n"+
"       m2.getNamedItem('z') + ',' +  c.getItemType(0) + ',' +\n"+
"       c.getItemType(1) +','+ n +','+ m2.getNamedItem('q') + ',' +\n"+
"       m1.getNamedItem('y') + ',' + m1.item(0) + ',' + cPar(m1));\n"+
"   }\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			el = parse(xp, "", "<a>x</a>", reporter);
			assertEq("<a>A,B,1,Y,Z,1,15,%q=Q,Q,Y,1,true</a>", el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name = 'test' root = 'a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"    %enumeration=['true 1', 'false 2']); \n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			parse(xp, "", "<a a='true 1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='false 2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xp, "", "<a a='false 1' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='Example' root='a'> <a>required myType()</a> </xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration scope='global'>\n"+
"    BNFGrammar $rrr = new BNFGrammar('\n"+
"      S        ::= $whitespace+ /*skipped white spaces*/\n"+
"      intList  ::= $integer (S? \",\" S? $integer)*\n"+
"      name     ::= $uppercaseLetter $lowercaseLetter+\n"+
"      fullName ::= name S ($uppercaseLetter \".\" S)? name\n"+
"      nameList ::= fullName (S? \",\" S? fullName)*\n"+
"      list     ::= intList | nameList\n"+
"    ');\n"+
"    type myType $rrr.parse('list');\n"+
"  </xd:declaration>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xd = xp.createXDDocument("Example");
			parse(xd, "<a>123</a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a>123, 456, 789</a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a>Arthur C. Clark, Jack London</a>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a>Arthur C Clark, Jack London</a>", reporter);
			assertTrue(reporter.errorWarnings(),"error not recognized");
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='Example' root='a'> <a> required myType() </a> </xd:def>\n"+
"<xd:def xd:name = 'modif'>\n"+
"  <xd:declaration scope='global'>\n"+
"     type myType $rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"  <xd:BNFGrammar name = \"$base\" scope='global'>\n"+
"    integer ::= [0-9]+\n"+
"    S       ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar name = \"$rrr\" extends = \"$base\" >\n"+
"    intList ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\" S)? name\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xd = xp.createXDDocument("Example");
			parse(xd, "<a>123, 456, 789</a>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    BNFGrammar rrr = new BNFGrammar('\n"+
"      integer ::= S? [0-9]+\n"+
"      S       ::= [#9#10#13 ]+  /*skip redundant white spaces*/\n"+
"      intList ::= integer (S? \",\" integer)* S?  /*list of integers*/\n"+
"    ');\n"+
"    type intList rrr.parse('intList');\n"+
"  </xd:declaration>\n"+
"\n"+
"<a>required intList()</a>\n"+
"\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a> 123, 456, 789 </a>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal(%whiteSpace = \"collapse\");\n"+
"       options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal(%whiteSpace = \"collapse\");\n"+
"       options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='   1   ' />", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='required decimal; options preserveAttrWhiteSpaces,noTrimAttr'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = list(%item=decimal, %enumeration=['1', '2', '3 4']); \n"+
"  Parser t = union(%item=[boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a=' true' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 2 ' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='3 4' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 7 ' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' true 1 ' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"                      %enumeration=['true 1', 'false 2']); \n"+
"  Parser t = union(%item=[decimal,boolean, s]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a=' true 1 ' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' true ' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = sequence(%item=[boolean,decimal],\n"+
"                      %enumeration=['true 1', 'false 2']);\n"+
"</xd:declaration>\n"+
" <a a='required s'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true 1' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='false 2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='false 1' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  Parser s = string(%enumeration=['abc', 'xyz']); \n"+
"  Parser t = union(%item=[decimal, boolean, s ]); \n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  type t  int;\n"+
"</xd:declaration>\n"+
" <a a='required t'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(0,1)'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a=\"optional; finally \n"+
"    {if(!gYear(%minInclusive='1999').parse()) error('false');}\"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='0999'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='1999'/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='list(%item=decimal(%minInclusive=1,%maxInclusive=$i," +
"      %totalDigits=1,%fractionDigits=0,%enumeration=[1,3]," +
"      %pattern=[\"\\\\d\"]))'/>"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1 4'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a='1 3'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2000'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='list(%item=decimal(%minInclusive= -100))'/>"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='1 3'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2000'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='1 4'/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"external method XDParseResult bugreports.MyTest_0.kp(XXNode x,XDValue[] y);\n"+
"</xd:declaration>\n"+
"<a a='kp(1,5,%totalDigits=1,%enumeration=[1,3],%pattern=[\"\\\\d\"])'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1.23'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='+1.23'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='-99999999999999999.0000999999'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='--99999999999999999.0000999999'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(%base=decimal(%minInclusive=0),%minInclusive=1," +
"      %maxInclusive=5,%totalDigits=1,%fractionDigits=0," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			xd.xparse("<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal(%minInclusive=1, %maxInclusive=$i, %totalDigits=1," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			xd.xparse("<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='decimal(0,5, %totalDigits=1,\n"+
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal( 0, $i, %totalDigits=1," +
"      %enumeration=[1,3],%pattern=[\"\\\\d\"])'\n"+
"/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>int $i = 5; </xd:declaration>\n"+
"<a a='decimal(%base=decimal(%minInclusive=0),%minInclusive=1," +
"      %maxInclusive=5,%totalDigits=1,%fractionDigits=0,%enumeration=[1,3]," +
"      %pattern=[\"\\\\d\"])' />\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='1'/>", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2'/>", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a a='union(%item=[decimal(%maxInclusive=5), boolean()])'/>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			parse(xd, "<a a='true' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='2' />", reporter);
			assertNoErrorwarnings(reporter);
			parse(xd, "<a a='xyz' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
			parse(xd, "<a a=' 1 2' />", reporter);
			assertTrue(reporter.errorWarnings(), "Error not recognized");
		} catch (Exception ex) {fail(ex);}
if(T){return;}

		clearTempDir();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}