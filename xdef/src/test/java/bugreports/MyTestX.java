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
import org.xdef.impl.XConstants;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.FUtils;
import org.xdef.sys.SDatetime;
import static org.xdef.sys.STester.printThrowable;
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
	final String _package;
	public MyTestX() {
		super();
		_package = this.getClass().getPackage().getName();
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
	}

	/** Testing the entered data using XDPool.
	 * @param xdef X-definition source.
	 * @param xdName name of X-definition.
	 * @param xon XON to be tested.
	 * @return empty string or error.
	 */
	private String testX(final XDPool xp,final String xdName,final String xon) {
		return testX(xp, xdName, null, xon, null);
	}

	/** Testing the entered data using XDPool.
	 * @param xdef X-definition source.
	 * @param xdName name of X-definition.
	 * @param cls XComponent class name or null.
	 * @param xon XON to be tested.
	 * @return empty string or error.
	 */
	private String testX(final XDPool xp,
		final String xdName, final String cls, final String xon) {
		return testX(xp, xdName, cls, xon, null);
	}
	/** Testing the entered data using XDPool.
	 * @param xdef X-definition source.
	 * @param xdName name of X-definition.
	 * @param cls XComponent class name or null.
	 * @param xon XON to be tested.
	 * @param outResult expected result of out stream or null.
	 * @return empty string or error.
	 */
	private String testX(final XDPool xp,
		final String xdName,
		final String cls,
		final String xon,
		final String outResult){
putInfo(xon);
		String result = "";
		try {
			ArrayReporter reporter = new ArrayReporter();
			Object o = XonUtils.parseXON(xon);
			XDDocument xd = xp.createXDDocument(xdName);
			StringWriter swr;
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(
					swr = new StringWriter(), false));
			} else {
				swr = null;
			}
			Object x = xd.jparse(xon, reporter);
			if (reporter.errorWarnings()) {
				result += "** 1\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 2\n" + XonUtils.toXonString(x, true) + "\n";
			}
			if (outResult != null) {
				swr.close();
				if (!outResult.equals(swr.toString())) {
					result += "** 3 '"+outResult+"', '"+swr.toString()+"'\n";
				}
			}
			xd = xp.createXDDocument(xdName);
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(
					swr = new StringWriter(), false));
			}
			XComponent xc = xd.jparseXComponent(xon, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 4\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			if (xc == null) {
				return result += "** 5\n X-component is null\n";
			}
			if (outResult != null) {
				swr.close();
				if (!outResult.equals(swr.toString())) {
					result +="** 6 '"+outResult+"', '"+swr.toString()+"'\n";
				}
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 7\n" + XonUtils.toXonString(x, true) + "\n";
			}
			xd = xp.createXDDocument(xdName);
			if (outResult != null) {
				xd.setStdOut(XDFactory.createXDOutput(
					swr = new StringWriter(), false));
			}
			x = x instanceof String ? XonUtils.toJsonString(x) : x;
			xc = xd.jparseXComponent(x, null, reporter);
			if (reporter.errorWarnings()) {
				result += "** 8\n" + reporter.printToString() + "\n";
				reporter.clear();
			}
			x = xc.toXon();
			if (!XonUtils.xonEqual(o, x)) {
				result += "** 9\n" + XonUtils.toXonString(x, true) + "\n";
			}
			if (outResult != null) {
				swr.close();
				if (!outResult.equals(swr.toString())) {
					result += "** 10 '"+outResult+"', '"
						+ swr.toString()+"'\n";
				}
			}
			if (cls != null) {
				Class<?> clazz = Class.forName(cls);
				xd = xp.createXDDocument(xdName);
				if (outResult != null) {
					xd.setStdOut(XDFactory.createXDOutput(
						swr = new StringWriter(), false));
				}
				xc = xd.jparseXComponent(xon, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 11\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 12\n"+XonUtils.toXonString(x, true)+"\n";
				}
				if (outResult != null) {
					swr.close();
					if (!outResult.equals(swr.toString())) {
						result +="** 13 '"+outResult+"', '"
							+ swr.toString()+"'\n";
					}
				}
				xd = xp.createXDDocument(xdName);
				if (outResult != null) {
					xd.setStdOut(XDFactory.createXDOutput(
						swr = new StringWriter(), false));
				}
				x = x instanceof String ? XonUtils.toJsonString(x) : x;
				xc = xd.jparseXComponent(x, clazz, reporter);
				if (reporter.errorWarnings()) {
					result += "** 14\n" + reporter.printToString() + "\n";
					reporter.clear();
				}
				x = xc.toXon();
				if (!XonUtils.xonEqual(o, x)) {
					result += "** 15\n" + XonUtils.toXonString(x,true)+"\n";
				}
				if (outResult != null) {
					swr.close();
					if (!outResult.equals(swr.toString())) {
						result +="** 16 '"+outResult+"', '"
							+ swr.toString()+"'\n";
					}
				}
			}
		} catch (Exception ex) {
			result += printThrowable(ex) + "\n";
		}
		return result.isEmpty() ? null : xon + "\n" + result;
	}
	private void genAndCopyXComponents(final XDPool xp) throws Exception {
		File file = clearTempDir();
		genXComponent(xp, file).checkAndThrowErrors();
		String source = getSourceDir();
		File[] files = new File(file, _package).listFiles();
		for (int i=0; i < files.length; i++) {
			File f = files[i];
			String name = f.getName();
			if (name.endsWith(".java")) {
				File f1 = new File(source, name);
				FUtils.copyToFile(f, f1);
				files[i] = f1;
			} else {
				files[i] = null;
			}
		}
		_geneatedSources = files;
	}
	
	private void clearSources() {
		if (_geneatedSources != null) {
			for (int i = 0; i < _geneatedSources.length; i++) {
				_geneatedSources[i].delete();
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
		for(XDValue z: y) System.out.print(" " + z);;
		System.out.println();
	}

	@SuppressWarnings({"unchecked", "unchecked"})
	@Override
	/** Run test and display error information. */
	public void test() {
/**/
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES,
			"");
////////////////////////////////////////////////////////////////////////////////
		boolean T = false; // if false, all tests are invoked
////////////////////////////////////////////////////////////////////////////////
		Element el;
		File file;
		String ini,json, s, xml, xon ;
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
if(true)return;
/**/
		try {
			xdef = // test XON models in different X-definitions
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name=\"a\" root=\"testX\">\n" +
"<xd:xon name=\"testX\"> [%anyObj=\"*\" ] </xd:xon>\n" + // array
"</xd:def>\n" +
"<xd:def name=\"m\" root=\"testX\">\n" + // map
"  <xd:xon name=\"testX\"> { %anyName: %anyObj=\"*;\" } </xd:xon>\n"  +
"</xd:def>\n" +
"<xd:def name=\"x\" root=\"testX\">\n" + // any object
"<xd:xon name=\"testX\"> %anyObj </xd:xon>\n" +
"<xd:component>\n" +
"  %class "+_package+".MyTestX_AnyXXa %link a#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXm %link m#testX;\n" +
"  %class "+_package+".MyTestX_AnyXXx %link x#testX;\n" +
"</xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = XDFactory.compileXD(null,xdef);
			genAndCopyXComponents(xp);
			s = "bugreports.MyTestX_AnyXXx";
			assertNull(testX(xp,"x", s, "\"\\\"\""));
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(true)return;
	clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_Str %link #A;</xd:component>\n" +
"<xd:declaration>boolean x(){outln(getText());return true;}</xd:declaration>\n"+
"<xd:xon name='A'>\n" +
//"{\"\": \"jstring()\", \"a\": \"jstring()\", \"b\":\"jstring()\"}\n"+
"[ \"eq('2021')\", \"num()\", \"gYear()\", \"jstring()\", \"jstring()\" ]\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			genAndCopyXComponents(xp);
			xon = "{\"\":\"nul\", \"a\": \"a1\", \"b\": \"a-1\"}";
			xon = "{\"\":\"null\", \"a\": \"1\", \"b\": \"-1\"}";
			xon = "[\"2021\", \"2021\", \"2021\", \"null\", \"-1\"]";
//			xon = "[\"-1\"]";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toXonString(x, true);
System.out.println(s);
			XonUtils.parseXON(s);
			y = XonUtils.parseXON(XonUtils.toXonString(x, true));
			assertTrue(XonUtils.xonEqual(x,y));
			json = XonUtils.toXonString(x, true);
System.out.println(json);
			y = jparse(xp, "", json, reporter);
System.out.println(XonUtils.toXonString(y, true));
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
if (!XonUtils.xonEqual(xc.toXon(),y)) {
System.out.println("errorB\n" + ((List)xc.toXon()).get(0).getClass());
System.out.println("errorB\n" + XonUtils.toXonString(xc.toXon(), true));
}
			xd = xp.createXDDocument();
			xc = xd.jparseXComponent(xc.toXon(), null, reporter);
			assertNoErrorwarningsAndClear(reporter);
//			assertTrue(XonUtils.xonEqual(xc.toXon(),y));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
if (!XonUtils.xonEqual(xc.toXon(),y)) {
System.out.println("errorC\n" + XonUtils.toXonString(xc.toXon(), true));
}
//			assertTrue(XonUtils.xonEqual(xc.toXon(), y));
//			el = XonUtils.xonToXmlW(xc.toXon());
			el = xc.toXml();
			s = XonUtils.toJsonString(XonUtils.xmlToXon(el), true);
System.out.println(s);			
			o = XonUtils.xonToJson(jparse(xp, "", el, reporter));
			assertNoErrorwarningsAndClear(reporter);
if (!XonUtils.xonEqual(o,y)) {
System.out.println("errorD\n" + KXmlUtils.nodeToString(el, true));
System.out.println("errorD\n" + XonUtils.toXonString(y, true));
System.out.println("errorD\n" + XonUtils.toXonString(o, true));
}
//			assertTrue(XonUtils.xonEqual(o, y));
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(true)return;
if(T) return;
	clearSources();
/**/
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"  <xd:component>%class "+_package+".MytestX_Hex0 %link #A;</xd:component>\n" +
"<xd:xon name='A'>\n" +
"[\n" +
"  \"base64Binary()\",\n" +
"  \"hexBinary()\",\n" +
"  \"? hexBinary()\"\n" +
"]\n" +
"</xd:xon>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null,xdef);
			genXComponent(xp, clearTempDir());
			xon = "[b(true), x(0FAE99), x()]";
			x = XonUtils.parseXON(xon);
			s = XonUtils.toJsonString(x, true);
			XonUtils.parseXON(s);
			y = XonUtils.parseXON(XonUtils.toXonString(x, true));
			assertTrue(XonUtils.xonEqual(x,y));
			json = XonUtils.toXonString(x, true);
			y = jparse(xp, "", json, reporter);
			assertNoErrorwarningsAndClear(reporter);
			genXComponent(xp, clearTempDir());
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(xc.toXon(),y));
			xd = xp.createXDDocument();
			xd.setXONContext(xon);
			xc = xd.jcreateXComponent("A", null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(x, xc.toXon()));
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(T) return;
/**/
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = \"A\">\n" +
"  <xd:component>%class "+_package+".MytestX_Hex %link #A;</xd:component>\n" +
"<xd:xon name=\"A\">\n" +
"  [ \"base64Binary()\", \"hexBinary()\" ]\n" +
"</xd:xon>\n" +
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
		} catch (Exception ex) {fail(ex); reporter.clear();}
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
"<xd:def xmlns:xd='" + _xdNS + "' name = 'O' root = \"A\">\n" +
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
			xdef = // sequence witn separatoritem (compatible item types)
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:component>%class "+_package+".MytestX_SQ %link #a;</xd:component>\n" +
"<xd:declaration>\n"+
"  type s sequence(%separator=',', %item=[int,long]);\n"+
"</xd:declaration>\n"+
" <a a='?s'>\n"+
"  ? s;\n"+
"  <b xd:script='?'>s;</b>\n"+
" </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<a a=' 1,2 '>   3,4    </a>";
			assertEq("<a a='1,2'>3,4</a>", el = parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			if ((o = SUtils.getValueFromGetter(xc, "geta")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(1, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(2, x);
			} else {
				fail("incorrect type");
			}
			if ((o=SUtils.getValueFromGetter(xc,"get$value")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(3, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(4, x);
			} else {
				fail("incorrect type");
			}
			assertNull(SUtils.getValueFromGetter(xc, "get$b"));
			assertEq(xml, xc.toXml());
			xml = "<a><b>5,6</b></a>";
			assertNoErrorwarningsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertNull(SUtils.getValueFromGetter(xc, "geta"));
			assertNull(SUtils.getValueFromGetter(xc, "get$value"));
			if ((o = SUtils.getValueFromGetter(xc, "get$b")) instanceof List) {
				x = ((List) o).get(0);
				assertTrue(x instanceof Long);
				assertEq(5, x);
				x = ((List) o).get(1);
				assertTrue(x instanceof Long);
				assertEq(6, x);
			} else {
				fail("incorrect type");
			}
			assertEq(xml, xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(T)return;
clearSources();
/**/
		try {
			TestXComponents_Y21enum.class.getClass(); // force to compile
			xdef = // test enum and ref
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A'>\n" +
"  <xd:declaration scope=\"local\" >\n" +
"    type Test_int int(1, 10);\n" +
"  </xd:declaration>\n" +
"  <xd:component>%class "+_package+".MytestX_A %link #A;</xd:component>\n" +
"  <A d='? list(%item=Test_int);'>\n" +
"      ? list(%item=int(), %length=3)\n" +
"  </A>\n" +
"</xd:def>";
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			xml = "<A>1\n2\n3</A>";
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			el = xc.toXml();
			assertEq("<A>1 2 3</A>", el);
			xc = parseXC(xp,"", el , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
			xml = "<A d='1 2'/>";
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			el = xc.toXml();
			assertEq(xml, el);
			xc = parseXC(xp,"", el , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(el, xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
if(T)return;
clearSources();
/**/
		try {
			TestXComponents_Y21enum.class.getClass(); // force to compile
			xdef = // test enum and ref
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='A' name='Y21'>\n" +
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
//			xp = XDFactory.compileXD(null,xdef);
			xp = compile(xdef);
			genXComponent(xp, clearTempDir());
			xml = "<A b='x'>z<B c='a'>x</B><B c='c' d='1 2'>y</B>x</A>";
			xc = parseXC(xp,"Y21", xml , null, reporter);
			assertNoErrorwarningsAndClear(reporter);
			assertEq(xml, xc.toXml());
			SUtils.setValueToSetter(xc, "set$value", null);
			o = SUtils.getObjectField("mytests.Y21_enum", "y");
			SUtils.setValueToSetter(xc, "setb", o);
			list = (List) SUtils.getValueFromGetter(xc, "listOfB");
			o = SUtils.getObjectField("test.xdef.TestXComponents_Y21enum", "b");
			SUtils.setValueToSetter(list.get(1), "setc", o);
			assertEq("<A b='y'><B c='a'>x</B><B c='b' d='1 2'>y</B>x</A>",
				xc.toXml());
		} catch (Exception ex) {fail(ex); reporter.clear();}
//if(true)return;
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
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
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
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
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
"<xd:declaration>\n"+
"  external method boolean mytests.MyTestX.chk1(String s);\n"+
"  external method boolean mytests.MyTestX.chk2(int i, XDContainer c);\n"+
"  external method void mytests.MyTestX.chk3();\n"+
"  external method boolean mytests.MyTestX.chk4(XXNode x, int i);\n"+
"  external method boolean mytests.MyTestX.chk5(XXNode x,int i);\n"+
"  external method void mytests.MyTestX.chk6(XXNode x, XDValue[] y);\n"+
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
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"D\">\n" +
"<xd:xon name=\"D\">\n" +
"  [ \"* int();\"]\n" +
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXonD %link #D; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			json = "[ 1, 2 ]";
			s = "mytests.MyTestXonD";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root=\"C\">\n" +
"<xd:xon name=\"C\">\n" +
"  [ %anyObj, \"int();\"]\n" +
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXonC %link #C; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			json = "[ true, 2 ]";
			s = "mytests.MyTestXonC";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertNoErrors(reporter);
			System.out.println(xc.toXon());
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\">{ %anyName: %anyObj=\"occurs *;\" }</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX00M %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX00M";
			json = "{}";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json),
				SUtils.getValueFromGetter(xc, "getMap$")));
			json = "{ a:1, b:true }";
			assertNull(testX(xp, "", s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseXON(json),
				SUtils.getValueFromGetter(xc, "getMap$")));
//			setValueToSetter(xc, "setval", 2);
			json = "null";
			assertNotNull(testX(xp, "", s, json)); // error: not map
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> \"int()\" </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX00 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX00";
			json = "1";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(1, SUtils.getValueFromGetter(xc, "getval"));
			SUtils.setValueToSetter(xc, "setval", 2);
			assertEq(2, SUtils.getValueFromGetter(xc, "getval"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> [\"int()\"] </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTest_xxy %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTest_xxy";
			json = "[1]";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getArray$"));
			SUtils.setValueToSetter(xc, "set$item", 2);
			assertEq(1, ((List) xc.toXon()).size());
			assertEq(2, ((List) xc.toXon()).get(0));
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getArray$"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> { a:\"? int()\" }</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX02 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX02";
			json = "{}";
			assertNull(testX(xp,"",s, json)); // OK
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getMap$"));
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
			json = "{a:123}";
			assertNull(testX(xp,"",s, json)); // OK
			SUtils.setValueToSetter(xc, "set$a", 9);
			assertEq(9, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", null);
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
			xc = xp.createXDDocument().jparseXComponent(json, null, reporter);
			assertEq(xc.toXon(), SUtils.getValueFromGetter(xc, "getMap$"));
			assertEq(123, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", 9);
			assertEq(9, SUtils.getValueFromGetter(xc, "get$a"));
			SUtils.setValueToSetter(xc, "set$a", null);
			assertNull(SUtils.getValueFromGetter(xc, "get$a"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> [ \"occurs *; int()\" ] </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX03 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX03";
			assertNull(testX(xp,"",s, "[]")); // OK
			assertNull(testX(xp,"",s, "[1]")); // OK
			assertNull(testX(xp,"",s, "[1,2]")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> { a:\"? int()\" } </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX04 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX04";
			assertNull(testX(xp,"",s, "{}")); // OK
			assertNull(testX(xp,"",s, "{a:1}")); // OK
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> [ %anyObj] </xd:xon>\n" +
//"<xd:xon name=\"A\"> [ %anyObj=\"occurs 1;\", %anyObj=\"occurs ?;\" ] </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX05 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX05";
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
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\"> [ %anyObj=\"occurs +;\" ] </xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX06 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX06";
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
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test occurrence for %anyName and %anyObj directives
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\">\n" +
"{ %anyName: %anyObj=\"occurs 2;\" }\n" +
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX07 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX07";
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
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\">\n" +
"[ %anyObj = \"0..1;\" ]\n" +
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX08 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX08";
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
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\">\n" +
"<xd:xon name=\"A\">%anyObj</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX09 %link #A; </xd:component>\n" +
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX09";
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
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='Any'>\n" +
"<xd:xon name=\"Any\">\n" +
" [ %oneOf, \"jvalue(); finally out('V')\",\n" +
"   [ %script=\"*; ref anyA; finally out('A')\" ],\n" +
"   { %script=\"*; ref anyM; finally out('M')\" }\n" +
" ]\n" +
"</xd:xon>\n" +
"<xd:xon name=\"anyA\">\n" +
" [ %anyObj=\"*;\" ]\n" +
//" [ [%script=\"*; finally outln('AA');\", %anyObj ] ]\n" +
"</xd:xon>\n" +
"<xd:xon name=\"anyM\">\n" +
//" { %anyName: %anyObj }\n"+
" {%anyName:\n" +
"   [%oneOf, \"jvalue()\",\n" +
"     [%script=\"ref Any; finally outln('MM')\"],\n" +
"   ]\n" +
" }\n" +
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX10 %link X#Any; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX10";
			assertNull(testX(xp,"X", s, "true", "V"));
			assertNull(testX(xp,"X", s, "[]", "A"));
			assertNull(testX(xp,"X", s, "[[1,2]]", "A"));
			assertNull(testX(xp,"X", s, "{}", "M"));
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
/**/
		try {
			xdef = // test XON reference to %any in %oneOf
"<xd:def xmlns:xd='" + _xdNS + "' name='X' root='Any'>\n" +
"<xd:xon name=\"Any\">\n" +
" [ %oneOf, \"jvalue();\",\n" +
"   [ %script=\"*; ref anyA;\" ],\n" +
"   { %script=\"*; ref anyM;\" }\n" +
" ]\n" +
"</xd:xon>\n" +
"<xd:xon name=\"anyA\">\n" +
" [ %anyObj ]\n" +
"</xd:xon>\n" +
"<xd:xon name=\"anyM\">\n" +
" { %anyName: %anyObj }\n"+
"</xd:xon>\n" +
"<xd:component> %class "+_package+".MyTestXX11 %link X#Any; </xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX11";
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
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name=\"X\" root=\"testI\">\n" +
"<xd:xon name=\"testI\">\n"  +
"  [ %oneOf, \"jvalue()\", [%anyObj], {%anyName: %anyObj} ]\n"  +
"</xd:xon>\n"  +
"<xd:component> %class "+_package+".MyTestXX12 %link X#testI; </xd:component>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			s = "mytests.MyTestXX12";
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
		try {// test %anyName in map
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
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
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(s),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
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
		} catch (Exception ex) {fail(ex);}
		reporter.clear();
if(T)return;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Skladby'>\n"+
"<xd:xon name=\"Skladby\">\n"+
"  {\n" +
"     \"Style\": [ %oneOf,\n" +
"       [ \"occurs 2..* string()\" ],\n" +
"       \"string()\"\n" +
"     ]\n" +
"  }\n" +
"</xd:xon>\n"+
"<xd:component>%class "+_package+".MyTestXX13 %link Skladby</xd:component>\n"+
"</xd:def>";
			s ="{\"Style\": \"Classic\"}";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			xd = xp.createXDDocument();
			x = XonUtils.parseXON(s);
			x = jparse(xd, s, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "Skladby", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"{ a:\"?int();\" }\n" +
"</xd:xon>\n" +
"<xd:component>%class "+_package+".MyTestXX14 %link A</xd:component>\n"+
"</xd:def>";
			s = "{ }";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			xd = xp.createXDDocument();
			x = XonUtils.parseXON(s);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "A", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='Skladby'>\n"+
"<xd:xon name=\"Skladby\">\n"+
"  {\n" +
"     \"Style\": [ %oneOf,\n" +
"       [ \"occurs 2..* string()\" ],\n" +
"       \"string()\"\n" +
"     ]\n" +
"  }\n" +
"</xd:xon>\n"+
"<xd:component>%class "+_package+".MyTestXX15 %link Skladby</xd:component>\n"+
"</xd:def>";
			s = "  { \"Style\": \"Classic\" }";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
			assertNull(testX(xp,"", s));
			xd = xp.createXDDocument();
			x = XonUtils.parseXON(s);
			x = jparse(xd, s, reporter);
			assertNoErrorwarnings(reporter);
			xd = xp.createXDDocument("");
			xd.setXONContext(x);
			o = jcreate(xd, "Skladby", reporter);
			assertNoErrorwarnings(reporter);
			if (!XonUtils.xonEqual(x, o)) {
				fail(x + "\n" + o + "\n");
			}
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"[\n" +
"  { %script= \"occurs 1..*\",\n" +
"    \"Genre\": [ %oneOf,\n" +
"      \"string()\",\n" +
"      [\"occurs 1..* string()\"]\n" +
"    ]\n" +
"  }\n" +
"]\n" +
"</xd:xon>\n" +
"<xd:component>%class "+_package+".MyTestXX16 %link a</xd:component>\n"+
"</xd:def>";
			s =
"[\n" +
"  {\n" +
"    \"Genre\": [\"A1\"]\n" +
"  },\n" +
"  {\n" +
"    \"Genre\": [\"B1\", \"B2\"]\n" +
"  },\n" +
"  {\n" +
"    \"Genre\": \"C1\"\n" +
"  }\n" +
"]";
			xp = compile(xdef);
			genAndCopyXComponents(xp);
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='A'>\n"+
"<xd:xon name='A'>\n" +
"{ a:\" int();\" }\n" +
"</xd:xon>\n" +
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
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:xon>\n" +
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
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' xd:root='a'>\n" +
"<xd:xon name='a'>\n" +
"{ \"\": \"jstring()\" }\n" +
"</xd:xon>\n" +
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
"<xd:def xmlns:xd='" + _xdNS + "' name=\"Example\" root=\"test\">\n" +
"<xd:component>%class "+_package+".MyTestXX20 %link test</xd:component>\n"+
"  <xd:xon name=\"test\">\n" +
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
"  </xd:xon>\n" +
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
		} catch (Exception ex) {fail(ex);}
if(T)return;
clearSources();
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"TRSconfig\">\n" +
"  <xd:ini xd:name=\"TRSconfig\">\n" +
"    TRSUser = string()\n" +
"    [User]\n" +
"      Home = file()\n" +
"      Authority = enum(\"SECURITY\", \"SOFTWARE\", \"CLIENT\", \"UNREGISTRED\")\n" +
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
			assertTrue(XonUtils.xonEqual(xini,
				xd.iparse(s = XonUtils.toIniString(xini), reporter)));
			assertNoErrorwarnings(reporter);
			assertEq("/123.45.67.8",
				"" + ((Map<String, Object>)xini.get("Server")).get("SeverIP"));
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
if(T)return;
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
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
		} catch (Exception ex) {fail(ex);}

//		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}