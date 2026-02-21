package bugreports;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.impl.XConstants;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;
import test.xdef.TestXComponents_Y21enum;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTestX extends XDTester {
	File[] _geneatedSources = null;
	public MyTestX() {
		super();
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
	}

	private void genAndCopyXComponents(final XDPool xp) {
		File file = clearTempDir();
		file.mkdirs();
		FUtils.deleteAll(file.listFiles(), true);
		genXComponent(xp, file).checkAndThrowErrors();
		String source = getSourceDir();
		File[] files = new File(file, _package).listFiles();
		for (int i=0; i < files.length; i++) {
			File f = files[i];
			String name = f.getName();
			if (name.endsWith(".java")) {
				File f1 = new File(source, name);
				try {
					FUtils.copyToFile(f, f1);
				} catch (SException ex) {
					throw new SRuntimeException(ex.getReport());
				}
				files[i] = f1;
			} else {
				files[i] = null;
			}
		}
		_geneatedSources = files;
	}

	private void clearSources() {
		if (_geneatedSources != null) {
			for (File _geneatedSource : _geneatedSources) {
				if (_geneatedSource != null) {
					_geneatedSource.delete();
				}
			}
			_geneatedSources = null;
		}
	}
	public static boolean chk1(String s)
	{System.out.println("i1=" + s); return true;}
	public static boolean chk2(int i, XDContainer c)
	{System.out.println("i2="+i + ", " + c);return true;}
	public static void chk3() {System.out.println("x3");}
	public static boolean chk4(XXNode x,int i)
	{System.out.println("i4="+i + ", " + x);return true;}
	public static boolean chk5(XXNode x,int i)
	{System.out.println("i5="+i + ", " + x);return true;}
	public static void chk6(XXNode x, XDValue[] y) {
		System.out.print("x6 " + x.getXPos() + ",");
		for(XDValue z: y) System.out.print(" " + z);
		System.out.println();
	}

	/** Run test and display error information. */
	@SuppressWarnings({"unchecked"})
	@Override
	public void test() {
/**
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			"");
/**/
		Element el;
		File file;
		String ini, json, s, xml, xon;
		Object o,x,y;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter swr;
		XComponent xc;
		Map<String, Object> xini;
		XDDocument xd;
		List list;
		String xdef;
		XDPool xp;
/**
		s = "_x61_hoj"; //ahoj
		System.out.print(s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x69_tem";  //item
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x6b_ey";  //key
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x76_alue";  //value
		System.out.print("; " + s + " ");
		System.out.print(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x78_ml"; //xml
		System.out.print("; " + s + " ");
		System.out.println(org.xdef.xon.XonTools.xmlToJName(s));
		s = "_x6d_ap"; //map
		System.out.print("; " + s + " ");
		System.out.println(org.xdef.xon.XonTools.xmlToJName(s));
/**/
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class " + _package + ".D_idref1 %link A</xd:component>\n" +
"  <xd:declaration scope='local'> uniqueSet u{t: string()}; </xd:declaration>\n"+
"  <A> <B xd:script='*' b='? u.t.CHKIDS();' a='? u.t.ID'/> </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A><B a=\"a'b\"/><B a='a b'/><B b=\"'a b' 'a''b'\"/></A>";
			genAndCopyXComponents(xp);
			clearSources();
			xc = parseXC(xp, "", xml, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_num %link #A;</xd:component>\n" +
"<xd:json name='A'>\n" +
"\"eq('2021')\"\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xon = "\"2021\"";
			s = _package+".MytestX_num";
			assertNull(testX(xp,"", s, xon));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T) return;
clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_Str %link #A;</xd:component>\n" +
"<xd:json name='A'>\n" +
"[ \"num()\" ]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xon = "[\"2021\"]";
			s = _package+".MytestX_Str";
			assertNull(testX(xp,"", s, xon));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T) return;
clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" name=\"X\" root=\"a\">\n" +
"<xd:component>%class " + _package + ".Csvxx1 %link a</xd:component>\n" +
" <xd:json name='a'>\n" +
"    [ [ %script =\"+\", \"string()\"] ]\n" +
//"    [ [ %script =\"+\", \"int\", \"int\", \"string()\", \"boolean()\"] ]\n" +
" </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef); // no property
			genAndCopyXComponents(xp);
			xd = xp.createXDDocument();
			json =
"[\n" +
"  [\"a\t\n\\\"b\"],\n" +
//"  [1, 2, \"a\", true],\n" +
//"  [null, 1, \"a\t\n\\\"b\", false],\n" +
//"  [6, null, null, true],\n" +
//"  [null, null, null, null]\n" +
"]";
			o = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				fail(XonUtils.toXonString(o, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
//if(true)return;
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"X\" root=\"a\">\n" +
"<xd:component>%class "+_package+".Csvxx2 %link a</xd:component>\n" +
" <xd:json name='a'>\n" +
"    [ [ %script =\"+\", \"int\", \"int\", \"string()\", \"boolean()\"] ]\n" +
" </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
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
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"<xd:json name='A'>\n" +
"[\n" +
"  \"base64Binary()\",\n" +
"  \"base64Binary()\",\n" +
"  \"hexBinary()\",\n" +
"  \"hexBinary()\"\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>\n" +
"  %class "+_package+".Xon %link #A;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			xon = "[ b(D66Z), b(), x(0FAE99), x() ]";
			x = jparse(xp, "", xon, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genAndCopyXComponents(xp);
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
			el = xc.toXml();
			xd = xp.createXDDocument();
			xd.xparse(el,reporter);
			y = xd.getXon();
			assertNoErrorwarningsAndClear(reporter);
			if (!XonUtils.xonEqual(x, y)) {
				fail(XonUtils.xonDiff(x, XonUtils.xonToJson(y))
					+ '\n' + KXmlUtils.nodeToString(el, true)
					+ "\n***\n" + XonUtils.toXonString(x, true)
					+ "\n***\n" + XonUtils.toXonString(y, true));
			}
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			TestXComponents_Y21enum.class.getClass(); // force to compile
			xdef = // test enum and ref
"<xd:def xmlns:xd='"+_xdNS+"' root='A' name='Y21'>\n" +
"  <xd:declaration scope=\"local\" >\n" +
"    type eType enum('x', 'y', 'A1_b', 'z', '_1', 'A1_b2', '$');\n" +
"    type myType eType;\n" +
"    type eType1 enum('a', 'b', 'c');\n" +
"    type extType eType1;\n" +
"    type Test_int int(1, 10);\n" +
"  </xd:declaration>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Y21 %link Y21#A;\n" +
"    %enum mytests.Y21_enum eType;\n" +
"    %ref %enum test.xdef.TestXComponents_Y21enum eType1;\n" +
"  </xd:component>\n" +
"  <A b='myType;' >\n" +
"    ? myType;\n" +
"    <B xd:script='*' c='eType1;' d='? list(%item=Test_int);'>\n" +
"      myType;\n" +
"    </B>\n" +
"    ? myType;\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml = "<A b='x'>z<B c='a'>x</B><B c='c' d='1 2'>y</B>x</A>";
			xc = parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			XComponentUtil.set(xc, "$value", null);
			o = SUtils.getObjectField("mytests.Y21_enum", "y");
			XComponentUtil.set(xc, "b", o);
			list = (List) XComponentUtil.getx(xc, "listOfB");
			o = SUtils.getObjectField("test.xdef.TestXComponents_Y21enum", "b");
			XComponentUtil.set((XComponent) list.get(1), "c", o);
			assertEq("<A b='y'><B c='a'>x</B><B c='b' d='1 2'>y</B>x</A>", xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(T)return;
clearSources();
/**/
		try {
			String type;
			type = "date";
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
//"  <xd:json name='A'> [\"* " + type + "()\"] </xd:json>\n"+
//"  <xd:json name='A'> [ \"" + type + "()\" ]</xd:json>\n"+
"  <xd:json name='A'> \"" + type + "()\"</xd:json>\n"+
"  <xd:component> %class "+_package+".Xon0 %link #A; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
//			xon = "[null, d2021-01-12, d1999-01-05+01:01, d1998-12-21Z ]";
//			xon = "[d2021-01-12]";
			xon = "d2021-01-12";
//			xon = "e\"a@bc\"";
			o = XonUtils.parseXON(xon);
			xd = xp.createXDDocument();
			x = xd.jparse(xon, reporter);
			assertNoErrors(reporter);
			if (!XonUtils.xonEqual(o, x)) {
				fail("** 2\n" + XonUtils.toXonString(o, true) + "\n" + XonUtils.toXonString(x, true));
			}

			assertNull(testA("jnull", "[ null, null ]"));
			assertNull(testA("jboolean", "[ null, true ]"));
			assertNull(testA("jnumber", "[ null, 1, 3.14E+2, -0 ]"));
			assertNull(testA("jstring", "[ null, \"abc\" ]"));
			assertNull(testA("jvalue", "[ null,true,1,3.14E+2,\"a b\",\"\" ]"));

			assertNull(testA("byte", "[null, 1b, -3b ]"));
			assertNull(testA("short", "[null, 1s ]"));
			assertNull(testA("int", "[null, 1i ]"));
			assertNull(testA("long", "[null, 1 ]"));
			assertNull(testA("integer", "[null, 0N, -3N ]"));
			assertNull(testA("float", "[null, 1.0f, 1f ]"));
			assertNull(testA("double", "[ null, 1.0, 1d ]"));
			assertNull(testA("decimal", "[ null,0D,1D,-1D,1.5D,3.33e-5D ]"));
			assertNull(testA("date", "[null, d2021-01-12, d1999-01-05+01:01, d1998-12-21Z ]"));
			assertNull(testA("gYear", "[null,  d2021+01:00, d1999, d-0012Z ]"));
			assertNull(testA("gps", "[null,g(20.2,19.9),g(20.21,19.99,0.1),g(51.52,-0.09,0,xxx)]"));
			assertNull(testA("price", "[null, p(20.21 CZK), p(19.99 USD) ]"));
			assertNull(testA("char", "[null, c\"a\", c\"'\", c\"\\\"\", c\"\\u0007\", c\"\\\\\" ]"));
			assertNull(testA("anyURI", "[null, u\"http://a.b\" ]"));
			assertNull(testA("emailAddr", "[ null, e\"tro@volny.cz\", e\"a b<x@y.zz>\" ]"));
			assertNull(testA("file", "[null, \"temp/a.txt\" ]"));
			assertNull(testA("ipAddr", "[null, /::FFFF:129.144.52.38,/0.0.0]"));
			assertNull(testA("currency", "[null, C(USD), C(CZK)]"));
			assertNull(testA("telephone","[null, t\"+420 234 567 890\"]"));

			assertNull(testM("int", "{a:1}"));
			assertNull(testM("int", "{ }"));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" \"jvalue();\"\n" +
"</xd:json>\n" +
"<xd:component>\n" +
"  %class "+_package+".Xon0 %link X#Any;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".Xon0";
			// value
			assertNull(testX(xp,"X", s, "true"));
			assertNull(testX(xp,"X", s, "1"));
			assertNull(testX(xp,"X", s, "null"));
			assertNull(testX(xp,"X", s, "\"abcd\""));
			assertNull(testX(xp,"X", s, "\"\""));
			assertNull(testX(xp,"X", s, "\"ab cd\""));
			assertNull(testX(xp,"X", s, "\"true\""));
			assertNull(testX(xp,"X", s, "\"ab\tcd\""));
			assertNull(testX(xp,"X", s, "\"\""));
			assertNull(testX(xp,"X", s, "\"true\""));
			assertNull(testX(xp,"X", s, "\"1\""));
			assertNull(testX(xp,"X", s, "\" ab cd \""));
		} catch (Exception ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"<xd:json name='A'>\n" +
"[\"date()\", \"gYear()\"]\n" +
"</xd:json>\n" +
"<xd:component> %class " + _package + ".Xon0x %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef); // no property
			genAndCopyXComponents(xp);
			xd = xp.createXDDocument();
			json = "[d2021-01-11,d2023]";
			y = xd.jparse(json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			if (!XonUtils.xonEqual(y, x = xc.toXon())) {
				fail(XonUtils.toXonString(y, true)
					+ "\n*****\n" + XonUtils.toXonString(x, true));
			}
			el = xc.toXml();
			s = XonUtils.toXonString(XonUtils.xmlToXon(el), true);
			o = jparse(xp, "", s, reporter);
			if (reporter.errorWarnings() || !XonUtils.xonEqual(o, y)) {
				fail((reporter.errorWarnings()?reporter.printToString()+'\n':"")
					+(!XonUtils.xonEqual(o,y)?"diff: "+XonUtils.xonDiff(o,y):"")
					+ "\n" + KXmlUtils.nodeToString(el, true)
					+"\ns = " + s
					+ "\noriginal =" + XonUtils.toXonString(y, true)
					+ "\nnew =" + XonUtils.toXonString(o, true));
				reporter.clear();
			}
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			xdef = // test XON reference to %any in %oneOf
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" [ \"%oneOf\", \"jvalue();\",\n" +
"   [ \"%script:*; ref anyA;\" ],\n" +
"   { \"%script\":\"*; ref anyM;\" }\n" +
" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" +
" [ %anyObj=\"*\" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" +
" { %anyName: %anyObj=\"*;\" }\n" +
"</xd:json>\n" +
"<xd:component>\n" +
"  %class "+_package+".MyTest_xxx %link X#Any;\n" +
"</xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTest_xxx";
			// value
			assertNull(testX(xp,"X", s, "true"));
			assertNull(testX(xp,"X", s, "1"));
			assertNull(testX(xp,"X", s, "null"));
			assertNull(testX(xp,"X", s, "\"abcd\""));
			assertNull(testX(xp,"X", s, "\"ab cd\""));
			assertNull(testX(xp,"X", s, "\"ab\tcd\""));
			assertNull(testX(xp,"X", s, "\"\""));
			assertNull(testX(xp,"X", s, "\"true\""));
			assertNull(testX(xp,"X", s, "\"1\""));
			assertNull(testX(xp,"X", s, "\" ab cd \""));
//			assertNull(testX(xp,"X", s, "/128.0.0"));
//			assertNull(testX(xp,"X", s, "e\"a@b.c\""));
			// array
			assertNull(testX(xp,"X", s, "[]"));
			assertNull(testX(xp,"X", s, "[null]"));
			assertNull(testX(xp,"X", s, "[1]"));
			assertNull(testX(xp,"X", s, "[true]"));
			assertNull(testX(xp,"X", s, "[1, true]"));
			assertNull(testX(xp,"X", s, "[\"1\"]"));
			assertNull(testX(xp,"X", s, "[\"true\"]"));
			assertNull(testX(xp,"X", s, "[1, \"true\"]"));
			assertNull(testX(xp,"X", s, "[\" ab cd \"]"));
			assertNull(testX(xp,"X", s, "[[]]"));
			assertNull(testX(xp,"X", s, "[{}]"));
//			assertNull(testX(xp,"X",s,"[null, /::FFFF:129.144.52.38, /0.0.0]"));
//			assertNull(testX(xp,"X",s,"[null, e\"a@b.c\"]"));
			// map
			assertNull(testX(xp,"X", s, "{}"));
			assertNull(testX(xp,"X", s, "{a:1}"));
			assertNull(testX(xp,"X", s, "{a:\"1\"}"));
//			assertNull(testX(xp,"X", s, "{a:/1.2.3}"));
//			assertNull(testX(xp,"X", s, "{a:e\"a@b.c\"}"));
			assertNull(testX(xp,"X", s, "{a:1, b:[],c:null,d:[], e:{}}"));
			assertNull(testX(xp,"X", s, "{a:1, b:[],c:null,d:[], e:{}}"));
		} catch (Exception ex) {fail(ex); reporter.clear();}
//if(true)return;
if(T)return;
clearSources();
/**/
		try {
			xdef = // test XON models in different X-definitions
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name=\"a\" root=\"testX\">\n" +
"  <xd:json name=\"testX\"> [%anyObj=\"*\" ] </xd:json>\n" + // array
"</xd:def>\n" +
"<xd:def name=\"m\" root=\"testX\">\n" + // map
"  <xd:json name=\"testX\"> { %anyName: %anyObj=\"*;\" } </xd:json>\n"  +
"</xd:def>\n" +
"<xd:def name=\"x\" root=\"testX\">\n" + // any object
"  <xd:json name=\"testX\"> %anyObj </xd:json>\n" +
"</xd:def>\n" +
"<xd:def name=\"y\" root=\"testX\">\n" + // jstring
"  <xd:json name=\"testX\"> \"jstring();\" </xd:json>\n" +
"</xd:def>\n" +
"<xd:def name=\"z\" root=\"testX\">\n" + // string
"  <xd:json name=\"testX\"> \"string();\" </xd:json>\n" +
"</xd:def>\n" +
"<xd:component>\n" +
"  %class "+_package+".MyTestX_AnyXXa %link a#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXm %link m#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXx %link x#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXy %link y#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXz %link z#testX;\n" +
"</xd:component>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestX_AnyXXx";
			assertNull(testX(xp,"x", s, "null"));
			assertNull(testX(xp,"x", s, "true"));
			assertNull(testX(xp,"x", s, "1"));
			assertNull(testX(xp,"x", s, "-0.5e+2"));
			assertNull(testX(xp,"x", s, "null"));
			assertNull(testX(xp,"x", s, "\"abc\""));
			assertNull(testX(xp,"x", s, "\"\""));
			assertNull(testX(xp,"x", s, "\" ab\tcd \""));
			assertNull(testX(xp,"x", s, "\" ab\\tcd \""));
			assertNull(testX(xp,"x", s, "\" ab\\u0020tcd \""));
			assertNull(testX(xp,"x", s, "\" \t\n \""));
			assertNull(testX(xp,"x", s, "\" \\t\\n \""));
//			assertNull(testX(xp,"x", s, "\"\\\"\""));
//			assertNull(testX(xp,"x", s, "\"\\\"\\\"\""));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
clearSources();
if(T)return;
/**/
		try {
			xdef =
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name=\"X\" root=\"testI\">\n" +
"<xd:json name=\"testI\">\n"  +
"  [ \"%oneOf\", \"jvalue()\", [%anyObj], {%anyName: %anyObj} ]\n"  +
"</xd:json>\n"  +
"<xd:component> %class "+_package+".MyTestXX12 %link X#testI; </xd:component>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"X", "[1]"));
			assertNull(testX(xp,"X", "[ [] ]"));
			assertNull(testX(xp,"X", "[ [1, true] ]"));
			assertNull(testX(xp,"X", "[ [\"a\"] ]"));
			assertNull(testX(xp,"X", "[ [\"a\"] ]"));
			assertNull(testX(xp,"X", "[ [1] ]"));
			assertNull(testX(xp,"X", "[ [1, 2] ]"));
			assertNull(testX(xp,"X", "[ [ { a:1 } ] ]"));
			assertNull(testX(xp,"X", "[ [ 1,  { a:1, b:[] } ] ]"));
			assertNull(testX(xp,"X", "[ [{}] ]"));
			assertNull(testX(xp,"X", "[ [ { a:[1, 2]} ] ]"));//
			assertNull(testX(xp,"X", "[{a:1,b:[3,4],c:{d:5,e:[6,7]}}]"));
			assertNull(testX(xp,"X", "{a:1}"));
			assertNull(testX(xp,"X", "true"));
			assertNull(testX(xp,"X", "1"));
			assertNull(testX(xp,"X", "null"));
			assertNull(testX(xp,"X", "\"\""));
			assertNull(testX(xp,"X", "\"a\tb\nc\""));
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
if(T)return;
clearSources();
/**/
		try {
			xdef = // test XON models in different X-definitions
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name=\"a\" root=\"testX\">\n" +
"<xd:json name=\"testX\"> [%anyObj=\"*\" ] </xd:json>\n" + // array
"</xd:def>\n" +
"<xd:def name=\"m\" root=\"testX\">\n" + // map
"  <xd:json name=\"testX\"> { %anyName: %anyObj=\"*;\" } </xd:json>\n"  +
"</xd:def>\n" +
"<xd:def name=\"x\" root=\"testX\">\n" + // any object
"<xd:json name=\"testX\"> %anyObj </xd:json>\n" +
"<xd:component>\n" +
"  %class "+_package+".MyTestX_AnyXXa %link a#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXm %link m#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXx %link x#testX;\n" +
"</xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestX_AnyXXx";
			assertNull(testX(xp,"x", s, "\" ab\tcd \""));
			assertNull(testX(xp,"x", s, "\" ab\\tcd \""));
			assertNull(testX(xp,"x", s, "\"\\\"\""));
			assertNull(testX(xp,"x", s, "\"\\\"\\\"\""));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
if(T)return;
	clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_Hex0 %link #A;</xd:component>\n" +
"<xd:json name='A'>\n" +
"[\n" +
"  \"base64Binary()\",\n" +
"  \"hexBinary()\",\n" +
"  \"? hexBinary()\"\n" +
"]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xon = "[b(true), x(0FAE99), x()]";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toJsonString(x, true);
			XonUtils.parseXON(s);
			y = XonUtils.parseXON(XonUtils.toXonString(x, true));
			assertTrue(XonUtils.xonEqual(x,y));
			y = jparse(xp, "", xon, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(xon, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xc.toXon(),y));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
clearSources();
if(T)return;
/**/
		try {
			xdef = // test XON reference to %any in %oneOf
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" [ \"%oneOf\", \"jvalue();\",\n" +
"   [ \"%script: *; ref anyA;\" ],\n" +
"   { \"%script\":\"*; ref anyM;\" }\n" +
" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" +
" [ \"%anyObj\" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" +
" { \"%anyName\": \"%anyObj\" }\n"+
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX11 %link X#Any; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX11";
			// value
			assertNull(testX(xp,"X", s, "true"));
			assertNull(testX(xp,"X", s, "1"));
			assertNull(testX(xp,"X", s, "\"\""));
			// array
			assertNull(testX(xp,"X", s, "[1]"));
			assertNull(testX(xp,"X", s, "[[]]"));
			assertNull(testX(xp,"X", s, "[{}]"));

			// map
			assertNull(testX(xp,"X", s, "{a:1}"));
			assertNull(testX(xp,"X", s, "{a:[]}"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
	try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root=\"test\">\n" +
"<xd:component>%class "+_package+".MyTestX_OneOfb %link test</xd:component>\n"+
"<xd:json name=\"test\">\n" +
"{ a:[ \"%oneOf: ?\",\n" +
"       \"jnull(); finally outln('null')\", \n" + // must be first
"       \"date(); finally outln('date')\", \n" +
"       \"ipAddr(); finally outln('ipAddr')\", \n" +
"       [\"%script:finally outln('[...]')\",\"*int()\"], \n" +
"       {\"%script\":\"finally outln('{ . }')\",x:\"? int()\",y:\"? string()\"},\n"+
"       \"string(); finally outln('string')\" \n" +
"  ]\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			s = "{a:\"2022-04-10\"}";
			o = XonUtils.parseXON(s);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			x = xd.jparse(s, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.xonToJson(x), o));
			assertEq("date\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertEq("date\n", swr.toString());
			XComponentUtil.set((XComponent) XComponentUtil.get(xc,"jx$item_1"), "val",
				new SDatetime("2022-04-15"));
			assertEq(new SDatetime("2022-04-15"), ((Map)xc.toXon()).get("a"));
			s = "{a:\"202.2.4.10\"}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("ipAddr\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()),
				XonUtils.toXonString(o)+'\n'+XonUtils.toXonString(xc.toXon()));
			s = "{a:{x:1, y:\" ab\tcd \"}}";
			o = XonUtils.parseXON(s);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			x = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			if (!XonUtils.xonEqual(o, x)) {
				fail(XonUtils.toXonString(o)+'\n'+XonUtils.toXonString(x));
				assertEq(" ab\tcd ", ((Map)((Map) o).get("a")).get("y"));
				assertEq(" ab\tcd ", ((Map)((Map) x).get("a")).get("y"));
			}
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			x = xc.toXon();
			assertEq("{ . }\n", swr.toString());
			assertEq(" ab\tcd ", ((Map)((Map) x).get("a")).get("y"));
			assertTrue(XonUtils.xonEqual(o, x), XonUtils.toXonString(o)+'\n'+XonUtils.toXonString(x));
			s = "{a:[1,2]}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("[...]\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()),
				XonUtils.toXonString(o)+'\n'+XonUtils.toXonString(xc.toXon()));
			s = "{a:{}}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("{ . }\n", swr.toString());
			assertTrue(XonUtils.xonEqual(o, xc.toXon()),
				XonUtils.toXonString(o)+'\n'+XonUtils.toXonString(xc.toXon()));
			s = "{a:null}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("null\n", swr.toString()); //????, however it is OK
			assertNull(((Map) o).get("a"));
			assertTrue(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("null\n", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertTrue(((Map) xc.toXon()).containsKey("a"));
			s = "{}";
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			o = xd.jparse(s, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", swr.toString()); //????
			assertNull(((Map) o).get("a"));
			assertFalse(((Map) o).containsKey("a"));
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq("", swr.toString());
			assertNull(XComponentUtil.get(xc, "$a"));
			assertNull(((Map) xc.toXon()).get("a"));
			assertFalse(((Map) xc.toXon()).containsKey("a"));
	} catch (RuntimeException ex) {fail(ex); reporter.clear();}
//if(true)return;
clearSources();
if(T)return;
/**/
	try {
		xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name='Y16' xmlns:x='x.int' root='a|c|e'>\n" +
"  <xd:component>\n" +
"    %class "+_package+".component.Y16 %link a;\n" +
"    %class "+_package+".component.Y16a %link x:x;\n" +
"    %class "+_package+".component.Y16c %link c;\n" +
"    %class "+_package+".component.Y16d %link y;\n" +
"    %class "+_package+".component.Y16e %link e;\n" +
"    %class "+_package+".component.Y16f %link g;\n" +
"  </xd:component>\n" +
"  <a><x:b xd:script='ref x:x'/></a>\n" +
"  <x:x y='int'/>\n" +
"  <c>\n" +
"    <d xmlns='y.int' xd:script='+; ref y'/>\n" +
"  </c>\n" +
"  <y xmlns='y.int' y='int'/>\n" +
"  <e>\n" +
"    <f xd:script='ref g'/>\n" +
"  </e>\n" +
"  <g y='int'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<a><x:b xmlns:x='x.int' y='1'/></a>";
			x = SUtils.getNewInstance(_package+".component.Y16");
			y = SUtils.getNewInstance(_package+".component.Y16a");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.set((XComponent) x, "$b", y);
			o = XonUtils.xmlToXon(KXmlUtils.parseXml(xml).getDocumentElement());
			el = ((XComponent)x).toXml();
			assertEq(xml, el);
			assertTrue(XonUtils.xonEqual(o, XonUtils.xmlToXon(el)),
				XonUtils.toXonString(o) + '\n' + KXmlUtils.nodeToString(el)
				+ '\n' + XonUtils.toXonString(XonUtils.xmlToXon(el)));
			x = SUtils.getNewInstance(_package+".component.Y16c");
			y = SUtils.getNewInstance(_package+".component.Y16d");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.add((XComponent) x, "d", y);
			xml = "<c><d xmlns='y.int' y='1'/></c>";
			assertEq(xml, ((XComponent) x).toXml());
			x = SUtils.getNewInstance(_package+".component.Y16e");
			y = SUtils.getNewInstance(_package+".component.Y16f");
			XComponentUtil.set((XComponent) y, "y", 1);
			XComponentUtil.set((XComponent) x, "f", y);
			xml = "<e><f y='1'/></e>";
			assertEq(xml, ((XComponent) x).toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
clearSources();
if(T)return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root = \"A\">\n" +
"  <xd:component>%class "+_package+".MytestX_Hex %link #A;</xd:component>\n" +
"<xd:json name=\"A\">\n" +
"  [ \"base64Binary()\", \"hexBinary()\" ]\n" +
"</xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xon = "[ b(FF00), x(FF00) ]";
			x = XonUtils.parseXON(xon);
list = (List) x;
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(x, true));
			xd = xp.createXDDocument();
			xd.jparse(xon, reporter);
			y = xd.getXon();
list = (List) y;
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(y, true));
			assertTrue(XonUtils.xonEqual(x,y));
			assertNoErrorsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent(xon, null, reporter);
			assertNoErrorsAndClear(reporter);
			y = xc.toXon();
list = (List) y;
System.out.println(list.get(0).getClass() + "," + list.get(1).getClass());
System.out.println(XonUtils.toXonString(y, true));
			assertTrue(XonUtils.xonEqual(x,y));
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
if(T)return;
clearSources();
/**/
		try {
//		String defDir =
//			"D:/cvs/DEV/java/xdefxx/src/test/resources/test/xdef/data/";
//		xp = compile(new String[] {defDir+"test/TestXComponents.xdef",
//			defDir + "test/TestXComponent_Z.xdef"});
//		assertNoErrors(genXComponent(xp, clearTempDir()));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name = 'O' root = \"A\">\n" +
"  <xd:component>%class "+_package+".MytestX_O %link O#A;</xd:component>\n" +
"  <A>\n" +
"    <xd:choice xd:script='+'>\n" +
"      <B/>\n" +
"      <C/>\n" +
"      int\n" +
"    </xd:choice>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq(xml, xc.toXml());
			xml = "<A><B/>1<C/><C/><B/>2<C/>3<B/></A>";
			xc = parseXC(xp, "O", xml, null, null);
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"D\">\n" +
"<D a='union(%item=[byte(), unsignedByte()]);'/>\n" +
"<xd:component> %class "+_package+".MyTestXUnion %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<D a='111'/>";
			assertEq(xml, parse(xp,"", xml, reporter));
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<A xd:script=\"finally printf('Pi = %f', 3.141592)\"/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			swr = new StringWriter();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			parse(xd, "<A/>", reporter);
			System.out.println(swr);
			System.out.println(String.join(", ", "a", "b")+".");
			System.out.println(String.format("a, %s.", "b"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root='D'>\n" +
"<xd:declaration>\n"+
"  external method boolean "+_package+".MyTestX.chk1(String s);\n"+
"  external method boolean "+_package+".MyTestX.chk2(int i, XDContainer c);\n"+
"  external method void "+_package+".MyTestX.chk3();\n"+
"  external method boolean "+_package+".MyTestX.chk4(XXNode x, int i);\n"+
"  external method boolean "+_package+".MyTestX.chk5(XXNode x,int i);\n"+
"  external method void "+_package+".MyTestX.chk6(XXNode x, XDValue[] y);\n"+
"</xd:declaration>\n"+
"<D a='chk1(getText());' xd:script='finally chk3();'>\n" +
" <E b='chk4(4);' xd:script='finally chk6(1,2,%t=9);'>\n" +
"   chk5(5);\n" +
" </E>\n" +
" chk2(2, %a = 2);\n" +
"</D>\n" +
"<xd:component> %class "+_package+".MyTestXExt %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<D a='111'><E b='444'>555</E>222</D>";
			parse(xp,"", xml, reporter);
			assertNoErrors(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"D\">\n" +
"<xd:json name=\"D\">\n" +
"  [ \"* int();\"]\n" +
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXonD %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			json = "[ 1, 2 ]";
			s = _package+".MyTestXonD";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"C\">\n" +
"<xd:json name=\"C\">\n" +
"  [ %anyObj, \"int();\"]\n" +
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXonC %link #C; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			json = "[ true, 2 ]";
			s = _package+".MyTestXonC";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\">{ %anyName: %anyObj=\"occurs *;\" }</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX00M %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX00M";
			json = "{}";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), XComponentUtil.get(xc, "Map$")));
			json = "{ a:1, b:true }";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json), XComponentUtil.get(xc, "Map$")));
//			setValueToSetter(xc, "setval", 2);
			json = "null";
			assertNotNull(testX(xp, "", s, json)); // error: not map
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> \"int()\" </xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX00 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX00";
			json = "1";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(1, XComponentUtil.get(xc, "val"));
			XComponentUtil.set(xc, "val", 2);
			assertEq(2, XComponentUtil.get(xc, "val"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> [\"int()\"] </xd:json>\n" +
"<xd:component> %class "+_package+".MyTest_xxy %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTest_xxy";
			json = "[1]";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), XComponentUtil.get(xc, "Array$"));
			XComponentUtil.set(xc, "$item", 2);
			assertEq(1, ((List) xc.toXon()).size());
			assertEq(2, ((List) xc.toXon()).get(0));
			assertEq(xc.toXon(), XComponentUtil.get(xc, "Array$"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> { a:\"? int()\" }</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX02 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX02";
			json = "{}";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), XComponentUtil.get(xc, "Map$"));
			assertNull(XComponentUtil.get(xc, "$a"));
			json = "{a:123}";
			assertNull(testX(xp,"",s, json)); // OK
			XComponentUtil.set(xc, "$a", 9);
			assertEq(9, XComponentUtil.get(xc, "$a"));
			XComponentUtil.set(xc, "$a", null);
			assertNull(XComponentUtil.get(xc, "$a"));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), XComponentUtil.get(xc, "Map$"));
			assertEq(123, XComponentUtil.get(xc, "$a"));
			XComponentUtil.set(xc, "$a", 9);
			assertEq(9, XComponentUtil.get(xc, "$a"));
			XComponentUtil.set(xc, "$a", null);
			assertNull(XComponentUtil.get(xc, "$a"));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> [ \"occurs *; int()\" ] </xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX03 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX03";
			assertNull(testX(xp,"",s, "[]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[1,2]")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> { a:\"? int()\" } </xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX04 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX04";
			assertNull(testX(xp,"",s, "{}")); // OK
			assertNull(testX(xp,"",s, "{a:1}")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> [ %anyObj] </xd:json>\n" +
//"<xd:json name=\"A\"> [ %anyObj=\"occurs 1;\", %anyObj=\"occurs ?;\" ] </xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX05 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX05";
			assertNull(testX(xp,"",s, "[true]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[[1]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}]")); // OK
			assertNull(testX(xp,"",s, "[[]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2}]")); // OK
			assertNotNull(testX(xp,"",s, "[1,2]")); // error more then two
			assertNotNull(testX(xp,"",s, "[[],{}]")); // error more then two
			assertNotNull(testX(xp,"",s, "[1,2,3]")); // error more then two
			assertNotNull(testX(xp,"",s, "[]")); // error empty
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\"> [ %anyObj=\"occurs +;\" ] </xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX06 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX06";
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent("[[123, true]]", null, reporter);
			System.out.println(((List<Object>) xc.toXon()).get(0));
			assertNull(testX(xp,"",s, "[true]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[[1]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1}]")); // OK
			assertNull(testX(xp,"",s, "[[]]")); // OK
			assertNull(testX(xp,"",s, "[{a:1,b:2}]")); // OK
			assertNull(testX(xp,"",s, "[1,2]")); // OK
			assertNull(testX(xp,"",s, "[[],{}]")); // OK
			assertNotNull(testX(xp,"",s, "[]")); // error empty
			assertNotNull(testX(xp,"",s, "{a:1,b:2}")); // error not array
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\">\n" +
"{ %anyName: %anyObj=\"occurs 2;\" }\n" +
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX07 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX07";
			assertNull(testX(xp,"",s, "{x:1, y:2}"));
			assertNotNull(testX(xp,"",s, "{}")); // empty
			assertNotNull(testX(xp,"",s, "{x:1}")); // only one item
//?			assertNotNull(testX(xp,"",s, "{x:1,y:2,z:3}"));//more items
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test %anyObj in array
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\">\n" +
"[ %anyObj = \"0..1;\" ]\n" +
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX08 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX08";
			assertNull(testX(xp,"",s, "[]"));
			assertNull(testX(xp,"",s, "[true]"));
			assertNull(testX(xp,"",s, "[ [true] ]"));
			assertNull(testX(xp,"",s,"[{a:1,b:2}]"));
			assertNotNull(testX(xp,"",s,"[1,2]")); // more then one item
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test %anyObj in array
"<xd:def xmlns:xd='"+_xdNS+"' root=\"A\">\n" +
"<xd:json name=\"A\">%anyObj</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX09 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX09";
			assertNull(testX(xp,"",s, "123"));
			assertNull(testX(xp,"",s, "[]"));
			assertNull(testX(xp,"",s, "[true]"));
			assertNull(testX(xp,"",s, "[ [true] ]"));
			assertNull(testX(xp,"",s,"{a:1,b:2}"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test XON models
"<xd:def xmlns:xd='"+_xdNS+"' name='X' root='Any'>\n" +
"<xd:json name=\"Any\">\n" +
" [ \"%oneOf\", \"jvalue(); finally out('V')\",\n" +
"   [ \"%script: *; ref anyA; finally out('A')\" ],\n" +
"   { \"%script\": \"*; ref anyM; finally out('M')\" }\n" +
" ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyA\">\n" +
" [ \"%anyObj: *;\" ]\n" +
//" [ [%script=\"*; finally outln('AA');\", %anyObj ] ]\n" +
"</xd:json>\n" +
"<xd:json name=\"anyM\">\n" +
//" { \"%anyName\": \"%anyOb\"j }\n"+
" {%anyName:\n" +
"   [\"%oneOf\", \"jvalue()\",\n" +
"     [\"%script: ref Any; finally outln('MM')\"],\n" +
"   ]\n" +
" }\n" +
"</xd:json>\n" +
"<xd:component> %class "+_package+".MyTestXX10 %link X#Any; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = _package+".MyTestXX10";
			assertNull(testX(xp,"X", s, "true", "V"));
			assertNull(testX(xp,"X", s, "[]", "A"));
			assertNull(testX(xp,"X", s, "[[1,2]]", "A"));
			assertNull(testX(xp,"X", s, "{}", "M"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type = int()\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"port= ? int(0, 9999)\n" +
"[system] %script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ] %script = optional\n" +
"[selfupdate] %script = optional\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			s = "hostaddr=\nproxy type = 1";
			xini = xd.iparse(s, reporter);
			assertTrue(xini.containsKey("hostaddr"));
			assertNull(xini.get("hostaddr"));
			assertNoErrorwarnings(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(s),XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type = required int(0,9)\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" +
"port= ? int(0, 9999);\n" +
"[system] %script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"hostaddr= 123.45.6.7\n" +
"port= 0\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			ini =
"proxy type=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
		} catch (RuntimeException ex) {fail(ex);}
		reporter.clear();
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Skladby'>\n"+
"<xd:json name=\"Skladby\">\n"+
"  {\n" +
"     \"Style\": [ \"%oneOf\",\n" +
"       [ \"occurs 2..* string()\" ],\n" +
"       \"string()\"\n" +
"     ]\n" +
"  }\n" +
"</xd:json>\n"+
"<xd:component>%class "+_package+".MyTestXX13 %link Skladby</xd:component>\n"+
"</xd:def>";
			s ="{\"Style\": \"Classic\"}";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			xd = xp.createXDDocument();
			x = jparse(xd, s, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "Skladby", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"<xd:json name='A'>\n" +
"{ a:\"?int();\" }\n" +
"</xd:json>\n" +
"<xd:component>%class "+_package+".MyTestXX14 %link A</xd:component>\n"+
"</xd:def>";
			s = "{ }";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			x = XonUtils.parseXON(s);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "A", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='Skladby'>\n"+
"<xd:json name=\"Skladby\">\n"+
"  {\n" +
"     \"Style\": [ \"%oneOf\",\n" +
"       [ \"occurs 2..* string()\" ],\n" +
"       \"string()\"\n" +
"     ]\n" +
"  }\n" +
"</xd:json>\n"+
"<xd:component>%class "+_package+".MyTestXX15 %link Skladby</xd:component>\n"+
"</xd:def>";
			s = "  { \"Style\": \"Classic\" }";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			xd = xp.createXDDocument();
			x = jparse(xd, s, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "Skladby", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"<xd:json name='a'>\n" +
"[\n" +
"  { %script= \"occurs 1..*\",\n" +
"    \"Genre\": [ \"%oneOf\",\n" +
"      \"string()\",\n" +
"      [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
"</xd:json>\n" +
"<xd:component>%class "+_package+".MyTestXX16 %link a</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
//			s =
//"[\n" +
//"  {\n" +
//"    \"Genre\": [\"A1\"]\n" +
//"  },\n" +
//"  {\n" +
//"    \"Genre\": [\"B1\", \"B2\"]\n" +
//"  },\n" +
//"  {\n" +
//"    \"Genre\": \"C1\"\n" +
//"  }\n" +
//"]";
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n"+
"<xd:json name='A'>\n" +
"{ a:\" int();\" }\n" +
"</xd:json>\n" +
"<xd:component>%class "+_package+".MyTestXX17 %link A</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			file = clearTempDir();
			genXComponent(xp, file);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", "{ a:1 }"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"<xd:json name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:json>\n" +
"<xd:component>%class "+_package+".MyTestXX18 %link a</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", "{ \"\":\"\" }"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' xd:root='a'>\n" +
"<xd:json name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:json>\n" +
"<xd:component>%class "+_package+".MyTestXX19 %link a</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", "{ \"\":\"\" }"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"Example\" root=\"test\">\n" +
"<xd:component>%class "+_package+".MyTestXX20 %link test</xd:component>\n"+
"  <xd:json name=\"test\">\n" +
"    { date: \"date()\",\n" +
"      cities: [\n" +
"        { %script = \"occurs 1..*;\",\n" +
"          \"from\": [\n" +
"            \"string();\",\n" +
"            { %script = \"occurs 1..*;\",\n" +
"              \"to\": \"jstring();\",\n"+
"              \"distance\": \"int();\"\n" +
"            }\n" +
"    	  ]\n" +
"        }"+
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xd = xp.createXDDocument("Example");
			s =
"{ \"date\" : \"2020-02-22\",\n" +
"  \"cities\" : [ \n" +
"    { \"from\": [\"Brussels\",\n" +
"        {\"to\": \"London\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 265}\n" +
"      ]\n" +
"    },\n" +
"    { \"from\": [\"London\",\n" +
"        {\"to\": \"Brussels\", \"distance\": 322},\n" +
"        {\"to\": \"Paris\", \"distance\": 344}\n" +
"      ]\n" +
"    }\n" +
"  ]\n" +
"}";
			x = xd.jparse(s, reporter);
			assertNoErrorwarnings(reporter);
			reporter.clear();
			assertEq(((Map)x).get("date"), new SDatetime("2020-02-22"));
			xd = xp.createXDDocument("Example");
			xc = xd.jparseXComponent(s, null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(((Map)xc.toXon()).get("date"),new SDatetime("2020-02-22"));
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd=\""+_xdNS+"\" root=\"TRSconfig\">\n" +
"  <xd:ini xd:name=\"TRSconfig\">\n" +
"    TRSUser = string()\n" +
"    [User]\n" +
"      Home = file()\n" +
"      Authority=enum(\"SECURITY\",\"SOFTWARE\",\"CLIENT\",\"UNREGISTRED\")\n" +
"      ItemSize = int(10000, 15000000)\n" +
"      ReceiverSleep = int(1, 3600)\n" +
"    [Server] %script = optional\n" +
"      RemoteServerURL = url()\n" +
"      SeverIP = ipAddr()\n" +
"      SendMailHost = domainAddr()\n" +
"      MailAddr = emailAddr()\n" +
"      Signature = SHA1()\n" +
"  </xd:ini>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			s =
"############# TRS configuration #############\n" +
"# TRS user name\n" +
"TRSUser = John Smith\n" +
"[User]\n" +
"# user directory\n" +
"Home = D:/TRS_Client/usr/Smith\n" +
"# authority(SECURITY | SOFTWARE | CLIENT | UNREGISTRED)\n" +
"Authority=CLIENT\n" +
"# Maximal item size (10000 .. 15000000)\n" +
"ItemSize=4000000\n" +
"# Receiver sleep time in seconds (1 .. 3600).\n" +
"ReceiverSleep=1\n" +
"[Server]\n" +
"# Remote server\n" +
"RemoteServerURL=http://localhost:8080/TRS/TRSServer\n" +
"SeverIP = 123.45.67.8\n" +
"SendMailHost = smtp.synth.cz\n" +
"MailAddr = jira@synth.cz\n" +
"Signature = 12afe0c1d246895a990ab2dd13ce684f012b339c\n" +
"";
			xini = xd.iparse(s, reporter);
			assertNoErrorwarnings(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(xini, xd.iparse(XonUtils.toIniString(xini), reporter)));
			assertNoErrorwarnings(reporter);
			assertEq("/123.45.67.8", "" + ((Map<String, Object>)xini.get("Server")).get("SeverIP"));
			reporter.clear();
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"  </xd:ini>\n"  +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			s = "hostaddr=";
			xini = xd.iparse(s, reporter);
			assertTrue(xini.containsKey("hostaddr"));
			assertNull(xini.get("hostaddr"));
			assertNoErrorwarnings(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(s),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
		} catch (RuntimeException ex) {fail(ex);}
		try {
			xdef =
"<xd:def  xmlns:xd='"+_xdNS+"' root='DefCiselnik' >\n" +
"    <DefCiselnik              xd:script=\"create from('/*/*[last()]');\"\n" +
"                              Name              =\"required string()\" >\n" +
"        <Table                xd:script  = \"occurs 1..\"\n" +
"                              Table             =\"required string()\">\n" +
"              <xd:any  xd:script=\"occurs 1..; create from('*')\" />\n" +
"        </Table>\n" +
"    </DefCiselnik>\n" +
"    <TransA   Column           =\"required string()\"/>\n" +
"    <TransF   Column           =\"required string()\"/>\n" +
"    <TransM   Column           =\"required string()\"/>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<DefCiselnik_ IdFlow=\"181131058\">\n" +
"    <ControlId IdDefPartner=\"163\"/>\n" +
"    <AnswerId DruhSouboru=\"W1A\"/>\n" +
"    <DefCiselnik Name=\"Pojistitel_SLP\">\n" +
"        <Table Table=\"Pojistitel\">\n" +
"            <TransA Column=\"KodPojistitele\"/>\n" +
"            <TransA Column=\"Pojistitel\"/>\n" +
"            <TransA Column=\"IdKancelar\"/>\n" +
"            <TransM Column=\"IdSubjekt\" />\n" +
"            <TransM Column=\"IdAdresa\" />\n" +
"            <TransA Column=\"PlatnostDo\"/>\n" +
"            <TransA Column=\"PlatnostOd\"/>\n" +
"            <TransA Column=\"IdPojistitelFuze\"/>\n" +
"        </Table>\n" +
"   </DefCiselnik>\n" +
"</DefCiselnik_>";
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			el = create(xd, "DefCiselnik", reporter);
			assertNoErrorsAndClear(reporter);
			System.out.println(KXmlUtils.nodeToString(el,true));
		} catch (RuntimeException ex) {fail(ex);}
if(T)return;
clearSources();

		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}