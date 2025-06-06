package test.xdef;

import test.XDTester;
import org.xdef.XDConstants;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.proc.XXNode;
import org.xdef.XDPool;
import org.w3c.dom.Element;
import org.xdef.XDValue;
import org.xdef.component.XComponent;
import org.xdef.proc.XXData;
import static org.xdef.sys.STester.runTest;
import static test.XDTester._xdNS;

/** TestScript.
 * @author Vaclav Trojan
 */
public final class TestScript extends XDTester {

	public TestScript() {super();}

	private boolean _result = false;

	private boolean _printCode = false;

	final public static void setResult(XXNode xnode, boolean result) {
		((TestScript) xnode.getUserObject())._result = result;
	}
	final public static void setResult(XXData xnode, XDParser parser) {
		setResult(xnode, parser.check(null, xnode.getTextValue()));
	}
	final public static void setResult(XXNode xnode, XDParseResult result) {
		((TestScript) xnode.getUserObject())._result = !result.errors();
	}
	final public static boolean myCheck(XXNode x,XDValue[] params){return true;}
	final public static void myCheck1(XXNode x, XDValue[] params) {}
	final public static String myCheck2(XXNode x,	XDValue[] params) {
		return String.valueOf(20 + params.length);
	}
	final public static void myCheck3() {}
	final public static long myCheck4() {return 2;}
	final public static long myCheck4(long i) {return i + 1;}
	final public static long myCheck5(double p1, long p2, long p3, String p4) {
		return 3;
	}

	private static void display(final XDPool defPool,
		final String xdef,
		final String data) {
		System.out.flush();
		System.err.flush();
		System.out.println("\nXdefinition:\n"+ xdef);
		System.out.println("Data:\n"+ data);
		if (defPool != null) {
			defPool.display(System.out);
		}
		System.out.flush();
	}

	private void test(final String value, final String source) {
		System.out.flush();
		System.err.flush();
		String xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"   script='options preserveEmptyAttributes," +
"           preserveAttrWhiteSpaces, noTrimAttr'>\n"+
"<xd:declaration>\n"+
"  external method {\n"+
"     void test.xdef.TestScript.setResult(XXNode, boolean);\n"+
"     void test.xdef.TestScript.setResult(XXData, XDParser);\n"+
"     void test.xdef.TestScript.setResult(XXNode, XDParseResult);\n"+
"     boolean test.xdef.TestScript.myCheck(XXNode, XDValue[]);\n"+
"     void test.xdef.TestScript.myCheck1(XXNode, XDValue[]);\n"+
"     String test.xdef.TestScript.myCheck2(XXNode, XDValue[]);\n"+
"     void test.xdef.TestScript.myCheck3();\n"+
"     long test.xdef.TestScript.myCheck4();\n"+
"     long test.xdef.TestScript.myCheck4(long);\n"+
"     long test.xdef.TestScript.myCheck5(double, long, long, String);\n"+
"   }\n"+
"</xd:declaration>\n"+
"  <a a=\"optional; finally " + source + "\"/>\n"+
"</xd:def>\n";
		String xml = "<a a=\"" + value + "\"/>";
		XDPool xp = null;
		try {
			setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE);
			xp = compile(xdef);
			ArrayReporter rep = new ArrayReporter();
			_result = false;
			XDDocument xd = xp.createXDDocument();
			xd.setUserObject(this);
			xd.xparse(xml, rep);
			if (rep.errorWarnings()) {
				rep.printReports(System.out);
				System.out.flush();
				throw new Exception("Errors returned");
			}
			if (!_result) {
				System.out.flush();
				throw new Exception("Incorrect result");
			}
			if (_printCode) {
				display(xp, xdef, xml);
			}
		} catch (Exception ex) {
			System.out.flush();
			System.err.flush();
			System.out.println(xdef);
			System.out.flush();
			fail(ex);
			if (xp != null) {
				display(xp, xdef, xml);
			}
		}
	}

	private void testAttr1(final String source, final String expected) {
		System.err.flush();
		System.out.flush();
		String xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  String result='?';\n"+
"</xd:declaration>\n"+
"  <a a='string' xd:script='finally result=" + source + "'/>\n"+
"</xd:def>\n";
		String xml = "<a a='abc'/>";
		XDPool xp = null;
		try {
			xp = compile(xdef);
			ArrayReporter rep = new ArrayReporter();
			XDDocument xd = xp.createXDDocument();
			xd.xparse(xml, rep);
			if (rep.errorWarnings()) {
				rep.printReports(System.out);
				System.out.flush();
				throw new Exception("Errors returned");
			}
			System.out.flush();
			_result = expected.equals(xd.getVariable("result").toString());
			if (!_result) {
				throw new Exception("Incorrect result:" + xd.getVariable("result"));
			}
			if (_printCode) {
				display(xp, xdef, xml);
			}
		} catch (Exception ex) {
			System.out.flush();
			fail(ex);
			System.err.flush();
			display(xp, xdef, xml);
		}
		System.err.flush();
	}

	private void testAttr2(final String value, final String source) {
		System.err.flush();
		System.out.flush();
		String xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method {\n"+
"      void test.xdef.TestScript.setResult(XXNode, boolean);\n"+
"      void test.xdef.TestScript.setResult(XXData, XDParser);\n"+
"      void test.xdef.TestScript.setResult(XXNode, XDParseResult);\n"+
"      boolean test.xdef.TestScript.myCheck(XXNode, XDValue[]);\n"+
"      void test.xdef.TestScript.myCheck1(XXNode, XDValue[]);\n"+
"      String test.xdef.TestScript.myCheck2(XXNode, XDValue[]);\n"+
"      void test.xdef.TestScript.myCheck3();\n"+
"      long test.xdef.TestScript.myCheck4();\n"+
"      long test.xdef.TestScript.myCheck4(long);\n"+
"      long test.xdef.TestScript.myCheck5(double, long, long, String);\n"+
"    }\n"+
"  </xd:declaration>\n"+
"  <a a=\"" + source + "\"/>\n"+
"</xd:def>\n";
		String xml = "<a a=\"" + value + "\"/>";
		XDPool xp = null;
		try {
			xp = compile(xdef);
			ArrayReporter rep = new ArrayReporter();
			_result = false;
			XDDocument xd = xp.createXDDocument();
			xd.setUserObject(this);
			xd.xparse(xml, rep);
			if (rep.errorWarnings()) {
				rep.printReports(System.out);
				System.out.flush();
				throw new Exception("Errors returned");
			}
			if (!_result) {
				System.out.flush();
				throw new Exception("Incorrect result");
			}
			if (_printCode) {
				display(xp, xdef, xml);
			}
		} catch (Exception ex) {
			System.out.flush();
			fail(ex);
			System.err.flush();
			display(xp, xdef, xml);
		}
	}

	private void testCheckMethod(final String value,
		final String source,
		final String call) {
		System.err.flush();
		System.out.flush();
		String xdef =
			"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method {\n"+
"      void test.xdef.TestScript.setResult(XXNode, boolean);\n"+
"      void test.xdef.TestScript.setResult(XXData, XDParser);\n"+
"      void test.xdef.TestScript.setResult(XXNode, XDParseResult);\n"+
"      boolean test.xdef.TestScript.myCheck(XXNode, XDValue[]);\n"+
"      void test.xdef.TestScript.myCheck1(XXNode, XDValue[]);\n"+
"      String test.xdef.TestScript.myCheck2(XXNode, XDValue[]);\n"+
"      void test.xdef.TestScript.myCheck3();\n"+
"      long test.xdef.TestScript.myCheck4();\n"+
"      long test.xdef.TestScript.myCheck4(long);\n"+
"      long test.xdef.TestScript.myCheck5(double, long, long, String);\n"+
"   }\n"+
 source +
"  </xd:declaration>\n"+
			"\n"+
			"  <a a=\"optional " + call + "\"/>\n"+
			"</xd:def>\n";
		String data = "<a a=\"" + value + "\"/>";
		XDPool xp = null;
		try {
			xp = compile(xdef);
			ArrayReporter rep = new ArrayReporter();
			_result = false;
			XDDocument xd = xp.createXDDocument();
			xd.setUserObject(this);
			xd.xparse(data, rep);
			if (rep.errorWarnings()) {
				rep.printReports(System.out);
				System.out.flush();
				throw new Exception("Errors returned");
			}
			if (!_result) {
				System.out.flush();
				throw new Exception("Incorrect result");
			}
			if (_printCode) {
				display(xp, xdef, data);
			}
		} catch (Exception ex) {
			System.out.flush();
			fail(ex);
			System.err.flush();
			display(xp, xdef, data);
		}
	}

	@Override
	public void test() {
		String xdef, xml, s;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		XComponent xc;
		XDPool xp;

		setDebug(true);
		_printCode = true;
		_printCode = false;
////////////////////////////////////////////////////////////////////////////////
//		test("de,ef","setResult(NCNameList(','));");
//if(true)return;
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
		xdef = // test rounding seconds acordint to milliseconds
"<xd:def xmlns:xd='" + _xdNS + "' root='T'>\n" +
"  <xd:declaration>\n" +
"    type x xdatetime(\n"+
"            '{SSS000}yyyy-MM-dd HH:mm:ss[.S]','yyyy-MM-ddTHH:mm:ss');\n"+
"  </xd:declaration>\n" +
"  <T D1='required x()' D2='required x()' D3='required x()'/>\n" +
"</xd:def>";
		assertEq("<T D1='2022-01-01T12:34:56'"+
" D2='2022-01-01T12:34:57' D3='2022-01-01T12:34:57'/>",
			parse(xdef, "",
"<T D1='2022-01-01 12:34:56.1'" +
" D2='2022-01-01 12:34:56.5' D3='2022-01-01 12:34:56.6'/>", reporter));
		assertNoErrorwarnings(reporter);
////////////////////////////////////////////////////////////////////////////////
		testAttr1("(String)@a", "abc");
		testAttr1(" (String) @a", "abc");
		testAttr1("toString(@a)", "abc");
		testAttr1(" toString (@b ) ", "");
		testAttr1(" @a.toString() ", "abc");
		testAttr1("@a. toString()", "abc");
		testAttr1("@a . toString()", "abc");
		testAttr1("@a . toString ()", "abc");
		testAttr1("@a . toString ( )", "abc");
		testAttr1("toString(@a == \"abc\")", "true");
		testAttr1("toString(@a EQ \"abc\")", "true");
		testAttr1("toString(@a LE \"abc\")", "true");
		testAttr1("toString(@a GE \"abc\")", "true");
		testAttr1("toString(@a == \"\")", "false");
		testAttr1("toString(@a GT \"\")", "true");
		testAttr1("toString(@a > \"\")", "true");
		testAttr1("toString(@a &gt; \"\")", "true");
		testAttr1("(@a == \"abc\").toString()", "true");
		testAttr1("@a?\"true\":\"false\"", "true");
		testAttr1("@b?\"true\":\"false\"", "false");
		testAttr1("(String) (boolean) @a", "true");
		testAttr1("(String) ((boolean) @a)", "true");
		testAttr1("\"\" + (boolean) @a", "true");
		testAttr1("toString((boolean) @a)", "true");
		testAttr1("toString((boolean) @b)", "false");
		testAttr1("toString(!@a)", "false");
		testAttr1("toString(!@a.exists())", "false");
		testAttr1("toString(!(@a).exists())", "false");
		testAttr1("toString(!@a AND !@b)", "false");
		testAttr1("(String) (!@a OOR !@b)", "true");
		testAttr1("(\"\" + (@a) + (@b)) + @a", "abcabc");
		testAttr1("toString(!(@a OR @b) AND @a)", "false");
		testAttr1("(String) ((@a | @b) &amp; @a)", "true");
////////////////////////////////////////////////////////////////////////////////
		test("","/*koment*/setResult(true);/*koment*/;");
		test("","/*koment*/setResult/*koment*/(/*koment*/true/*koment*/OR"
			+ "/*koment*/false/*koment*/)/*koment*/;/*koment*/");
		test("","setResult(toString(9223372036854775807) == '9223372036854775807');");
		test("","setResult(1 == 1 AND 1 != 2);");
		test("","setResult(2 GT 1 AND 1 LT 2);");
		test("","setResult(2 GE 1 AND 1 LE 2);");
		test("","setResult('a' GE 'a' AND 'a' LE 'b');");
		test("","setResult('a' == 'a' AND 'a' != 'b');");
		test("","setResult('b' GT 'a' AND 'a' LT 'b');");
		test("","setResult('ab' GT 'a' AND 'ab' LT 'b');");
		test("","setResult('ab' GE 'a' AND 'ab' LE 'b');");
		test("","setResult('a' GE 'a' AND 'a' LE 'b');");
		test("","setResult('a'.startsWith('a'));");
		test("","setResult('ab'.startsWith('a'));");
		test("","setResult('Ab'.startsWithi('a'));");
		test("","setResult('ab'.startsWithi('A'));");
		test("","setResult('b'.endsWith('b'));");
		test("","setResult('ab'.endsWith('b'));");
		test("","setResult('aB'.endsWithi('b'));");
		test("","setResult('ab'.endsWithi('B'));");
		test("","setResult('a'.equals('a'));");
		test("","setResult(!'a'.equals('aa'));");
		test("","setResult('aB'.equalsIgnoreCase('Ab'));");
		test("","setResult(toString(-9223372036854775808) == '-9223372036854775808');");
		test("","setResult(toString(9_223_372_036_854_775_807)"
			+ "== '9223372036854775807');");
		test("","setResult(toString(-9_223_372_036_854_775_808) == '-9223372036854775808');");
		test("","setResult(toString(0xf0) == '240');");
		test("","setResult(toString(0XF0) == '240');");
		test("","setResult(toString(0xffffffffffffffff) == '-1');");
		test("","setResult(toString(-0xffffffffffffffff) == '1');");
		test("","setResult(toString(-0xff_ff_ff_ff_ff_ff_ff_ff) == '1');");
		test("","setResult(toString(0x8000000000000000) == '-9223372036854775808');");
		test("","setResult(toString(0x8_000_000_000_000_000) == '-9223372036854775808');");
		test("", "setResult(toString(0x8000000000000000 - 1) == '9223372036854775807');");
		test("","setResult(toString(0x7fffffffffffffff) == '9223372036854775807');");
		test("","setResult(toString(-0x7fffffffffffffff) == '-9223372036854775807');");
		test("","setResult(toString(-0d999.99) == '-999.99');");
		test("","setResult(toString(-0i999999) == '-999999');");
		test("","setResult(toString(0) == '0');");
		test("","setResult(toString(0.1) == '0.1');");
		test("","setResult(toString(001.9) == '1.9');");
		test("","setResult(toString(0377) == '377');");
		test("","setResult(toString(-0377) == '-377');");
		test("","{boolean b = true; b AND= false; setResult(!b);}");
		test("","{boolean b = true; b OR= false; setResult(b);}");
		test("a","setResult(eq('a'));");
		test("ab","setResult(eq('ab'));");
		test("\\","setResult(eq('\\\\'));");
		test(" \\ ","setResult(eq(' \\\\ '));"); //??
		test("'","setResult(eq('\\''));");
		test(" '","setResult(eq(' \\''));"); //??
		test("' ","setResult(eq('\\' '));"); //??
		test("\\","setResult(eq(' \\\\ '.trim()));");
		test(" \\ ","{setText(getText().trim()); setResult(eq('\\\\'));}");
		test("A","setResult(eq('\\u0041'));");
		test("Aa","setResult(eq('\\u0041a'));");
		test("aA","setResult(eq('a\\u0041'));");
		test("aAa","setResult(eq('a\\u0041a'));");
		test("ab'cd","setResult(eq('ab\\'cd'));");
		test("ab''cd","setResult(eq('ab\\'\\'cd'));");
		test("ab''cd", "{setText(getText().toUpper()); setResult(eq('AB\\'\\'CD'));}");
		test("AB''CD","{setResult(eq('ab\\'\\'cd'.toUpper()));}");
		test("AB''CD", "{setText(getText().toLower());setResult(eq('ab\\'\\'cd'));}");
		test("M","setResult(enum('M'));");
		test("","setResult(enum('M',''));");
		test("XY","setResult(enum('XY','ABC','DE'));");
		test("XY","setResult(enumi('XY','ABC','DE'));");
		test("aBc","setResult(enumi('XY','ABC','DE'));");
		test("de","setResult(NCName());");
		test("de:ef","setResult(!NCName());");
		test("de","setResult(NCNameList());");
		test("de ef","setResult(NCNameList());");
		test("de,ef","setResult(NCNameList(','));");
		test(",","setResult(!NCNameList(','));");
		test("de,ef","setResult(!NCNameList());");
		test("de,ef","setResult(NCNameList(','));");
		test("de,,ef","setResult(NCNameList(','));");
		test("de,ef,","setResult(!NCNameList(','));");
		test("","setResult(!NCNameList());");
		test("de:ef","setResult(QName());");
		test("de:ef gh","setResult(QNameList());");
		test("de:ef,gh","setResult(QNameList(','));");
		test("de:ef, gh","setResult(QNameList(', '));");
		test("de:ef, gh","setResult(QNameList(', '));");
		test("de,ef:gh","setResult(QNameList(','));");
		test("de,ef:gh,","setResult(!QNameList(','));");
		test(",de,,,,ef:gh,","setResult(!QNameList(','));");
		test("", "setResult(!QNameList());");

		test("12","{int i=1; int j=(i=3)*3; setResult(eq(toString(i+j)));}");
		test("12","{int i=1; int j=3*(i=3); setResult(eq(toString(i+j)));}");
		test("12", "{String s = ''; int i = 1; int j = 2; s = '' + (i) + (j); setResult(eq(s));}");
		test("12", "{int i = 1; int j = 1; int k = j = 2; String s = '' + i + j; setResult(eq(s));}");
		test("6", "{int k = 0; int j = 0; int i = j = k = 2; String s=''; s = i + j + k; setResult(eq(s));}");
		test("6", "{int i = 2, j = i, k = i; String s = ''; s = i + j + k; setResult(eq(s));}");
		test("7", "{int l, i, j, k = j = i = 2; String s = ''; s = i + j + k + l= 1; setResult(eq(s));}");
		test("2", "{int i, j, k, l; i = 0; j = k = i++; String s='';s=i + j + k + l = 1; setResult(eq(s));}");
		test("4", "{int i, j, k, l; i = 0; j = k = ++i; String s='';s=i + j + k + l = 1; setResult(eq(s));}");
		test("false", "{int i=2+3*2; setResult(eq(toString(i > 3 == true == false)));}");
		test("12", "{int i=0; String s=''; for (i=1; i LT 3; i=i+1) s = s + i; setResult(eq(s));}");
		test("12", "{int i=0; String s=''; for (i=1; i LT 3; i++) s += i; setResult(eq(s));}");
		test("12", "{int i=0; String s=''; for (i=1; i LT 3; ++i) s += i; setResult(eq(s));}");
		test("67", "setResult(eq(toString(2 + 3 * 2 * 10 + 3 + 2)));");
		test("67", "setResult(eq('' + (2 + 3 * 2 * 10 + 3 + 2)));");
		test("26032", "setResult(eq('' + 2 + 3 * 2 * 10 + 3 + 2));");
		test("67", "{String s = ''; s += 2+3*2*10 + 3 + 2; setResult(eq(s));}");
		test("true", "{int i = 0; setResult(eq(toString(i != 1 AND i != 2 AND i != 3)));}");
		test("true", "{int i = 0; setResult(eq(toString(i != 1 AAND i != 2 AAND i != 3)));}");
		test("true", "{int i = 0; setResult(eq(toString(i != 1 OR i != 2 OR i != 3)));}");
		test("true", "{int i = 0; setResult(eq(toString(i != 1 OOR i != 2 OOR i != 3)));}");
		test("true", "{int i = 0; setResult(eq(toString(i != 1 XOR i != 2 XOR i != 3)));}");
		test("true", "{int i = 0; setResult(eq(toString(i == 0)));}");
		test("false", "{int i = 1; setResult(eq(toString(i == 0)));}");
		test("false", "{int i = 0; setResult(eq(toString(i != 0)));}");
		test("true", "{int i = 1; setResult(eq(toString(i != 0)));}");
		test("true", "{int i = -1; setResult(eq(toString(i LE 0)));}");
		test("true", "{int i = 0; setResult(eq(toString(i LE 0)));}");
		test("false", "{int i = 1; setResult(eq(toString(i LE 0)));}");
		test("false", "{int i = -1; setResult(eq(toString(i GE 0)));}");
		test("true", "{int i = 0; setResult(eq(toString(i GE 0)));}");
		test("true", "{int i = 1; setResult(eq(toString(i GE 0)));}");
		test("true", "{int i = 1; setResult(eq(toString(!(i LT 0))));}");
		test("true", "{int i = -1; setResult(eq(toString(i LT 0)));}");
		test("false", "{int i = 0; setResult(eq(toString(i LT 0)));}");
		test("false", "{int i = 1; setResult(eq(toString(i LT 0)));}");
		test("false", "{int i = -1; setResult(eq(toString(i GT 0)));}");
		test("false", "{int i = 0; setResult(eq(toString(i GT 0)));}");
		test("true", "{int i = 1; setResult(eq(toString(i GT 0)));}");
		test("true", "{int i=1;i=i LSH 3; setResult(eq(toString(i==8)));}");
		test("true", "{int i=1;i&lt;&lt;=3; setResult(eq(toString(i==8)));}");
		test("true", "{int i=1;i LSH=3; setResult(eq(toString(i==8)));}");
		test("true", "{int i=8;i=i RSH 3; setResult(eq(toString(i==1)));}");
		test("true", "{int i=8;i RSH=3; setResult(eq(toString(i==1)));}");
		test("true", "{int i=8;i &gt;&gt;=3; setResult(eq(toString(i==1)));}");
		test("true", "{int i=$MININT;i=i RRSH 60;setResult(eq(toString(i==8)));}");
		test("true", "{int i=$MININT; i RRSH=60;setResult(eq(toString(i==8)));}");
		test("true", "{int i=$MININT;i=i&gt;&gt;&gt;60;setResult(eq(toString(i==8)));}");
		test("true", "{int i=$MININT;i&gt;&gt;&gt;=60;setResult(eq(toString(i==8)));}");
		test("false", "{int i=2+3*2; setResult(eq(toString(i GT 3 == true == false)));}");
		test("true", "{int i = 1; if (i LT 1) i = 2; else i = -1; setResult(eq(toString(i == -1)));}");
		test("true0", "{boolean i = 1 LT 0; int j = 0; String s = '';"
			+ "if (i) i=false; else i=true; s=s+i; s=s+j; setResult(eq(s));}");
		test("false2", "{boolean i = 1 LT 0; int j = i ? 1 : 2; String s = '';"
			+ "if (i) i=false; else s=s+i; s=s+j; setResult(eq(s));}");
		test("false2", "{boolean i = !(1 GE 0); int j = i ? 1 : 2; String s='';"
			+ "if (i) i=false; else s=s+i; s=s+j; setResult(eq(s));}");
		test("109", "{int i = 10; String s=''; while (i GT 8) { s = s + i; i = i - 1; } setResult(eq(s));}");
		test("12", "{int i=0; i=1; String s=''; while(i LT 3){s=s+i; i=i+1;} setResult(eq(s));}");
		test("12", "{int i=0; i=1; String s=''; while(!(i GE 3)){s=s+i; i=i+1;} setResult(eq(s));}");
		test("12", "{int i; String s=''; for (i=1; i LT 3; i=i+1) s=s+i; setResult(eq(s));}");
		test("12", "{int i; String s=''; for (i=1; i != 3; i=i+1) s=s+i; setResult(eq(s));}");
		test("12", "{int i; String s=''; for (i=1; !(i GE 3); i=i+1) s=s+i; setResult(eq(s));}");
		test("12", "{String s=''; for (int i=1; i LT 3; i=i+1) s=s+i; setResult(eq(s));}");
		test("01", "{int i=0; String s=''; do {s=s+i; i=i+1;} while(i==1); setResult(eq(s));}");
		test("01", "{int i=0; String s=''; do {s=s+i; i=i+1;} while(!(i!=1)); setResult(eq(s));}");
		test("8","{int i=0;int j=1;int k=2;int l=3; while((i==0)AND(j==1)OR(k==2)AND(l==3)){i++;k++;}" +
			"setResult(eq(toString(i+j+k+l)));}");
		test("8","{int i=0;int j=1;int k=2;int l=3; while((i==0)AAND(j==1)OOR(k==2)AAND(l==3)){i++;k++;}" +
			"setResult(eq(toString(i+j+k+l)));}");
		test("","{int i=0;int j=1; setResult((i==0 | j==0));}");
		test("","{int i=0;int j=1; setResult((i==0 || j==0));}");
		test("","{int i=0;int j=1; setResult((i==0 ^ j==0));}");
		test("","{int i=0;int j=1; setResult(!(i==0 ^ j==1));}");
		test("","{int i=0;int j=1; setResult((i==0 AND j==1));}");
		test("","{int i=0;int j=1; setResult((i==0 AAND j==1));}");
		test("","{int i=0;int j=1; setResult((i==0==true AND j==1==true));}");
		test("","{int i=0;int j=1; setResult((i==0==true AAND j==1==true));}");
		test("false","setResult(eq(toString(2+3*2 GT 3 == true == false)));");
		test("","setResult((2+3*2 > 3 == empty().parse() == false) == false);");
		test("false","{String s = toString(2+3*2 GT 3 == true == false);"
			+ "s = s != '' ? s = toString(2+3*2 GT 3 == true == false) : 'no'; setResult(eq(s));}");
		test("BAc","setResult(eq(translate('--abc--','ab-','BA')));");
		test("1","setResult(eq(toString(abs(-1))));");
		test("1","setResult(eq(toString(abs(1))));");
		test("1.5","setResult(eq(toString(abs(-1.5))));");
		test("1.5","setResult(eq(toString(abs(1.5))));");
		test("1","{ int i = -1; setResult(eq(toString(abs(i))));}");
		test("1","{ int i = -1; setResult(!(!eq(toString(abs(i)))));}");
		test("1","{ int i = -1; setResult(!eq(toString(i)));}");
		test("1.5","{float r = -1.5; setResult(eq(toString(abs(r))));}");
		test("1.0","{float r = -1.5; float s = -2.5; setResult(eq(toString(IEEEremainder(r,s))));}");
		test("1.0", "setResult(eq(toString(sin($PI/2))));");
		test("1.0", "setResult(eq(toString(cos(0.0))));");
		test("0.0", "setResult(eq(toString(tan(0.0))));");
		test("0.0", "setResult(eq(toString(asin(0.0))));");
		test("4.0", "setResult(eq(toString(ceil($PI))));");
		test("3.0", "setResult(eq(toString(floor($PI))));");
		test("1.0", "setResult(eq(toString(log($E))));");
		test("1.0", "setResult(eq(toString(exp(0.0))));");
		test("180.0", "setResult(eq(toString(toDegrees($PI))));");
		test("-1.0", "setResult(eq(toString(cos(toRadians(180.0)))));");
		test("1.0", "{int i = 1; setResult(eq((String)(float)(int)round((float)(int)i)));}");//casting
		test("1.4142135623730951","setResult(eq(toString(sqrt(2))));");
		test("1.4142135623730951","setResult(eq(toString(pow(2,1.0/2))));");
		test("1.4142135623730951", "setResult(eq(toString(pow(myCheck4(),1.0/2))));");
		test("1","{int i=1; int j=2; setResult(eq(toString(min(i,j))));}");
		test("2","{int i=1; int j=2; setResult(eq(toString(max(i,j))));}");
		test("1.5", "{float i=1.5;float j=2.5;setResult(eq(toString(min(i,j))));}");
		test("2.5", "{float i=1.5;float j=2.5;setResult(eq(toString(max(i,j))));}");
		test("1.5","{float i=1.5;int j=2;setResult(eq(toString(min(i,j))));}");
		test("2.5","{int i=1;float j=2.5;setResult(eq(toString(max(i,j))));}");
		test("67","setResult(eq(toString(myCheck4()+3*2*10 + 3 + 2)));");
		test("63","setResult(eq(toString(-myCheck4(1)+3*2*10 + 3 + 2)));");
		test("Monday, 23 January 2006", "setResult(xdatetime('EEEE, d MMMM y'))");
		test("Mon, 23 Jan 2006", "setResult(xdatetime('EEE, d MMM y'));");
		test("1999-05-01T20:43:09.876+01:00",
			"setResult(eq(parseDate('1999-05-01T20:43:09.876+01:00').toString()));");
		test("1999-05-01T20:43:09+01:00", "setResult(eq(toString(parseDate('1999-05-01T20:43:09+01:00'))));");
		test("1999-05-01", "setResult(eq(parseDate(getText(), 'yyyy-MM-dd').toString()));");
		test("1999-04-05", "setResult(eq(easterMonday(1999).toString()));");
		test("1999-04-05", "setResult(eq(easterMonday(parseDate(getText())).toString()));");
		test("1999-04-05", "setResult(parseDate(getText()).lastDayOfMonth() == 30);");
		test("Mon, 23 Jan 2006 20:26:46", "setResult(emailDate());");
		test("Mon, 23 Jan 2006 20:26:46 +0100", "setResult(emailDate());");
		test("Mon, 23 Jan 2006 20:26:46 +0100 (CET)","setResult(emailDate());");
		test("Mon, 23 Jan 2006 20:26:46 CET", "setResult(emailDate());");
		test("Fri, 6 Nov 2009 01:44:36 +0100", "setResult(emailDate());");
		test("Fri, 6 Nov 2009 21:59:58 +0000 (GMT)", "setResult(emailDate());");
		test("ahoj 2","setResult(eq('ahoj ' + 002));");
		test("2 nazdar","setResult(eq(002 + ' nazdar'));");
		test("3.5.2004", "setResult((xdatetime('d.M.y')? 123 : 456) == 123);");
		test("ho","{boolean b = eq('ho'); myCheck1(pow(5,6) + 1 + 5,2,3,'haha'); setResult(b);}");
		test("ho","{boolean b = eq('ho') AAND (myCheck2(pow(5,6) + 1 + 5,2,3,'haha')=='24');setResult(b);}");
		test("ho","{boolean b = eq('ho'); myCheck3(); setResult(b);}");
		test("ho", "{boolean b=eq('ho'); b=b AND (myCheck4() == 2); setResult(b);};");
		test("ho", "{boolean b=eq('ho'); b=b AND (myCheck4(4) == 5); setResult(b);}");
		test("nazdar","{boolean b = eq('nazdar'); b = (2+3*2 GT 3) | (2 GT 3) == true; setResult(b);}");
		test("nazdar","{boolean b = eq('nazdar'); b = !(!((2+3*2 GT 3) ^ (2 GT 3)));setResult(b);}");
		test("nazdar","setResult(eq('nazdar')? true : false);");
		test("1.4","setResult(eq(toString(1.4)) ? true : false);");
		test("1.4","setResult(eq(toString(1.4,'')) ? true : false);");
		test("01.40","setResult(eq(toString(1.4,'00.00'))? true: false);");
		test("2", "{setResult(int);}");
		test("2", "{setResult(int(%enumeration=['2','3']));}");
		test("1", "{setResult(!int(%enumeration=['2','3']));}");
		test("2", "{setResult(int(1,3,%enumeration=['2','3']));}");
		test("---01", "{setResult(gDay());}");
		test("---01", "{setResult(gDay() ? true : false);}");
		test("---31", "{setResult(gDay());}");
		test("---32", "{setResult(!gDay());}");
		test("---00", "{setResult(!gDay());}");
		test("--01", "{setResult(gMonth());}");
		test("--12", "{setResult(gMonth());}");
		test("--13", "{setResult(!gMonth());}");
		test("--00", "{setResult(!gMonth());}");
		test("--01-01", "{setResult(gMonthDay());}");
		test("--12-31", "{setResult(gMonthDay());}");
		test("--02-29", "{setResult(gMonthDay());}");
		test("--00-01", "{setResult(!gMonthDay());}");
		test("--13-01", "{setResult(!gMonthDay());}");
		test("--12-32", "{setResult(!gMonthDay());}");
		test("--02-30", "{setResult(!gMonthDay());}");
		test("--0230", "{setResult(!gMonthDay());}");
		setProperty(XDConstants.XDPROPERTY_MINYEAR, "1916");
		setProperty(XDConstants.XDPROPERTY_MAXYEAR, "2216");
		setProperty(XDConstants.XDPROPERTY_SPECDATES, "3000-12-31,3000-12-31T00:00:01,3000-12-31T23:59:59");
		test("1999", "{setResult(gYear());}");
		test("1915", "{setResult(!gYear());}");
		test("2217", "{setResult(!gYear());}");
		test("1999-01", "{setResult(gYearMonth());}");
		test("1999-12", "{setResult(gYearMonth());}");
		test("1700-12", "{setResult(!gYearMonth());}");
		test("2500-12", "{setResult(!gYearMonth());}");
		test("1999-00", "{setResult(!gYearMonth());}");
		test("1999-13", "{setResult(!gYearMonth());}");
		test("199913", "{setResult(!gYearMonth());}");
		test("1999-12-31", "{setResult(date());}");
		test("3000-12-31T00:00:00", "{setResult(!dateTime());}");
		test("1999-12-31T12:31:59+01:00", "{setResult(dateTime());}");
		test("1999-12-31T12:31:59Z", "{setResult(dateTime());}");
		test("1999-12-31T12:31:59", "{setResult(dateTime());}");
		test("1999-12-31T12:31:59+01:00", "{setResult(dateTime());}");
		test("1999-12-31T12:31:59Z", "{setResult(dateTime());}");
		test("1999-12-31T12:31:59", "{setResult(dateTime());}");
		test("2999-12-31T12:31:59+01:00", "{setResult(!dateTime());}");
		test("3000-12-31T00:00:00", "{setResult(dateTime());}");
		test("3000-12-31T00:00:01", "{setResult(dateTime());}");
		test("01.03.1999", "{setResult(xdatetime('dd.MM.yyyy'));}");
		test("01.03.1699", "{setResult(!xdatetime('dd.MM.yyyy'));}");
		test("01.03.2999", "{setResult(!xdatetime('dd.MM.yyyy'));}");
		test("01.03.1999", "{setResult(xdatetime('d.M.y'));}"); //???
		test("31.12.3000", "{setResult(xdatetime('d.M.y'));}"); //???
		test("30.12.3000", "{setResult(!xdatetime('d.M.y'));}"); //???
		resetProperties();
		test("","setResult(myCheck(pow(5,6) + 1 + 5,2,3,'haha'));");
		test("","setResult(myCheck5(pow(5,6) + 1 + 5,2,3,'haha') != 3 ? false: true);");
		test("false", "setResult(eq(parseDate('1999-5-1T20:43+01:00') == now()));");
		test("3.5.2004", "setResult(xdatetime('d.M.y')? true : false);");
		test("a.5.2004", "{if (xdatetime('d.M.y')) setResult(false);else{clearReports(); setResult(true);}}");
		test("19480010", "setResult(xdatetime('yyyyMMdd')? false : true);");
		test("01051999","setResult(eq(toString(parseDate('1999-5-1','y-M-d'),'ddMMy')));");
		test("01051999","setResult(eq(toString(parseDate('1999-5-1','yyyy-M-d'),'ddMMyyyy')));");
		test("1.5.1999","setResult(eq(toString(parseDate('19990501','yyyyMMdd'),'d.M.yyyy')));");
		test("010599", "setResult(eq(toString(parseDate('99-05-01','YY-MM-dd'),'ddMMyy')));");
		test("", "{Datetime d = parseDate('2005-03-01T14:48:40.352');"+
			"String s = 'datum: 01/03/2005, čas: 14:48:40.352';" +
			"String m='\\'datum: \\'dd/MM/yyyy\\', čas: \\'HH:mm:ss.SSS';" +
			"setResult(toString(d,m) == s);}");
		test("2008", "setResult(eq(toString(parseDate('2008','yyyy'),'yyyy')));");
		test("2008-01", "setResult(eq(toString(parseDate('1/2008','M/yyyy'),'yyyy-MM')));");
		test("","{Datetime d = now(); int i = d.getDaytimeMillis(); d=d.setDaytimeMillis(31601);"
			+ " setResult(d.getDaytimeMillis() LT i);}");
		test("","{Datetime d = parseDate('2005-03-01T20:10:01+01:00');"
			+ "d = d.setDaytimeMillis(0);"
			+ "if (d.getDaytimeMillis() == 0) setResult(true);"
			+ "else {outln('m='+d.getDaytimeMillis()); setResult(false);}}");
		test("","{int i=now().getDaytimeMillis();setResult((i GE 0) AND (i LE 36*60*60*1000));}");//zone chg!
		test("","{Datetime d = parseDate('2005-03-01T23:10:01+01:00');"
			+ "int i = d.getDaytimeMillis(); d = d.addMillisecond(3600000); "
			+ " if (d.getDaytimeMillis() == 601000) setResult(true); "
			+ "else {outln('m='+(d.getDaytimeMillis()-i));setResult(false);}}");
		test("","{Datetime d = parseDate('2005-03-01T20:10:01+01:00');"
			+ "int i = d.getDaytimeMillis(); d = d.addMillisecond(3600000);"
			+ "if (d.getDaytimeMillis() - i == 3600000) setResult(true); "
			+ "else {outln('m='+(d.getDaytimeMillis()-i));setResult(false);}}");
		test("01051999","setResult(eq('01051999') ? true : false);");
		test("001234","setResult(eq(toString(1234,'000000')));");
		test("A","setResult(regex('[A-Z]'));");
		test("A-A","setResult(regex('([A-Z0-9](?!--)[-A-Z0-9]{0,8})'));");
		test("A--A","setResult(!regex('([A-Z0-9](?!--)[-A-Z0-9]{0,8})'));");
		test("A--A","setResult(regex('([A-Z0-9](?=--)[-A-Z0-9]{0,8})'));");
		test("A-A","setResult(!regex('([A-Z0-9](?=--)[-A-Z0-9]{0,8})'));");
		test("A","{String s='[A-Z]'; setResult(regex(s));}");
		test("01","{int i=0; setResult(eq(toString(i++) + i));}");
		test("11","{int i=0; setResult(eq(toString(++i) + i));}");
		test("1","{int i=0; ++i; setResult(eq(i));}");
		test("1","{int i=0; i++; setResult(eq(i));}");
		test("01", "{float i=0.0; setResult(eq(toString(round(i++)) + round(i)));}");
		test("11", "{float i=0.0; setResult(eq(toString(round(++i))+round(i)));}");
		test("1", "{String s='1'; if(false) {s += 1;} setResult(eq(s));}");
		test("12", "{String s='1'; if(true) {s += 2;} setResult(eq(s));}");
		test("12", "{String s='1';if(false)s+=1; else s+=2;setResult(eq(s));}");
		test("11", "{String s='1';if(true)s+=1; else s+=2; setResult(eq(s));}");
		test("12", "{String s='1'; if(true) {s += 2;} setResult(eq(s));}");
		test("12", "{String s; if((s='1') == '1'){s += 2;} setResult(eq(s));}");
		test("12", "{String s; if((s='12') == '1'){s += 2;}setResult(eq(s));}");
		test("12", "{String s; if('1' == (s='1')){s += 2;} setResult(eq(s));}");
		test("12", "{String s; if('1' == (s='12')){s += 2;}setResult(eq(s));}");
		test("12", "{int i=1; String s=''; do {s +=i; i++;if (i GE 3) break;}while(true);setResult(eq(s));}");
		test("1", "{int i=1; String s=''; do {s +=i; i++;if (i GE 3) break;}while(false);setResult(eq(s));}");
		test("12", "{int i=1; String s=''; while(i LT 3) {s +=i; i++;} setResult(eq(s));}");
		test("12", "{int i=1; String s=''; for(;;) {s += i; i++; if (i GE 3) break;} setResult(eq(s));}");
		test("12", "{int i=1; String s=''; for(;true;) {s += i; i++; if(i GE 3)break;}setResult(eq(s));}");
		test("1", "{int i=1; String s='1'; for(;false;) {s += i; i++; if(i GE 3)break;}setResult(eq(s));}");
		test("12", "{int i=1; String s=''; while(true) {s += i; i++; if(i GE 3)break;} setResult(eq(s));}");
		test("1", "{int i=1; String s='1'; while(false) {s += i; i++; if(i GE 3)break;}setResult(eq(s));}");
		test("12", "{String s=''; for(int i=1;; i++){s +=i; if (i GE 2) break;} setResult(eq(s));}");
		test("12", "{String s=''; for(int i=1; i LE 2; i++) s +=i; setResult(eq(s));}");
		test("12", "{String s='???'; int i=0; switch(i){"
			+ "case 1: break; case 2: break; default: s = '12';} setResult(eq(s));}");
		test("a b c d", "{String s=' \na  b\nc \t d \n'; setResult(eq(removeWhiteSpaces(s)));}");
		test("12", "{int i=parseInt(getText()); setResult(i == 12);}");
		test("23", "{String s='123'; setResult(eq(tail(s,2)));}");
		test("23", "{String s='23'; setResult(eq(tail(s,2)));}");
		test("3", "{String s='3'; setResult(eq(tail(s,2)));}");
		test("abc", "{setResult(starts('ab'));}");
		test("aBc", "{setResult(!starts('ab'));}");
		test("aBc", "{setResult(startsi('AB'));}");
		test("aBc", "{setResult(!startsi('BC'));}");
		test("abcd", "{setResult(ends('cd'));}");
		test("aBcD", "{setResult(!ends('ab'));}");
		test("aBcD", "{setResult(endsi('CD'));}");
		test("aBcD", "{setResult(!endsi('BC'));}");
		test("abcd", "{setResult(contains('ab'));}");
		test("aBcD", "{setResult(!contains('ab'));}");
		test("aBcD", "{setResult(containsi('AB'));}");
		test("aBcD", "{setResult(!containsi('xy'));}");
		// test "continue" and "break" in "for"
		test("","{for(int i=0;;){if(i GT 4)break;if(i++LE 1)continue;"
			+ "if(i!=3)setResult(false);break;}setResult(true);}");
		test("","{for(int i=0;i LT 5;){if(i GT 4)break;"
			+ "if(i++LE 1)continue;if(i!=3)setResult(false);break;} setResult(true);}");
		test("", "{for(int i=0;;i++){if(i GT 4)break;if(i LE 1){continue;}"
			+ "if(i!=3)setResult(false);break;}setResult(true);}");
		test("","{for(int i=0;i LT 6;i++){if(i GT 4)break;if(i==0)continue;"
			+ "if(i!=1)setResult(false); break;}setResult(true);}");
		// test "continue" and "break" in "while"
		test("","{int j= 0;while(j LT 6){if(j GE 4)break;if(j++LE 1)continue;"
			+ "if(j!=3)setResult(false); break;}setResult(true);}");
		// test "continue" and "break" in "do"
		test("","{int j=0;do{if(j GE 4)break;if(j++LE 2)continue;"
			+"if(j!=4)setResult(false);break;}while(j LT 6);setResult(true);}");
		// Test regular expression (XML schema expression is conterted to Java form)
		test("", "{Regex r = new Regex('(\\\\d{1,2})(:(\\\\d{1,2}))?');"
			+ "String s = '5:34'; RegexResult x = r.getMatcher(s);"
			+ "boolean b = x.matches(); int i = x.groupCount(); b AND= (i==4);"
			+ "if (b){b = (x.group(0)=='5:34');"
			+ "b AND= (x.group(1)=='5');"
			+ "b AND= (x.group(2)==':34');"
			+ "b AND= (x.group(3)=='34');}"
			+ "setResult(b);}");
		test("", "{Regex r = new Regex('(\\\\d{1,2})[\\\\-./](\\\\d{4}|\\\\d{2})');"
			+ "String s = '6-2003'; RegexResult x = r.getMatcher(s);"
			+ "boolean b=x.matches(); int i=x.groupCount(); b AND= (i==3);"
			+ "if (b){b= (x.group(0)=='6-2003'); b AND= (x.group(1)== '6');"
			+ "b AND= (x.group(2)=='2003');}"
			+ "setResult(b);}");
		test("", "{Regex r = new Regex('(\\\\d{1,2})[\\\\-./](\\\\d{4}|\\\\d{2})');"
			+ "String s = '6-05'; RegexResult x = r.getMatcher(s);"
			+ "boolean b=x.matches(); int i=x.groupCount(); b=b AND (i==3);"
			+ "if (b){b= (x.group(0)=='6-05'); b=b AND (x.group(1)== '6');"
			+ "b=b AND (x.group(2)=='05');}"
			+ "setResult(b);}");
		test("", "{Regex r = new Regex('[A-Z]{2}\\\\d{6}');"
			+ " String s = 'AA999999';"
			+ "RegexResult x = r.getMatcher(s); boolean b = x.matches();"
			+ "int i = x.groupCount();  b = b AND (i==1);"
			+ "if (b) b = x.group(0) == 'AA999999'; setResult(b);}");
		test("", "{Regex r = new Regex('[\\\\- A-Z0-9]{5,12}');"
			+ "String s = 'AX 1234';"
			+ "RegexResult x = r.getMatcher(s); boolean b = x.matches();"
			+ "int i = x.groupCount();  b = b AND (i==1);"
			+ "if (b) b = x.group(0) == 'AX 1234'; setResult(b);}");
		test("", "{Regex r = new Regex('\\\\d{1,10}'); String s='1234';"
			+ "RegexResult x = r.getMatcher(s); boolean b = x.matches();"
			+ "int i = x.groupCount();  b = b AND (i==1);"
			+ "if (b) b = x.group(0) == '1234'; setResult(b);}");
		test("", "{Regex r = new Regex('([1-9]\\\\d*)?([mHDWM])');"
			+ "String s = '23H';"
			+ "RegexResult x = r.getMatcher(s); boolean b = x.matches();"
			+ "int i=x.groupCount(); b=b AAND (i==3);"
			+ "if (b){b = x.group(0)=='23H'; b = b AAND (x.group(1)== '23');"
			+ "b = b AAND (x.group(2)=='H');}"
			+ "setResult(b);}");
		test("", "{Regex r = new Regex('([1-9]\\\\d*)?([mHDWM])');"
			+ " String s = 'H';"
			+ "RegexResult x = r.getMatcher(s); boolean b = x.matches();"
			+ "int i = x.groupCount(); b = b AND (i==3);"
			+ "if (b){b = x.group(0)=='H'; b = b AND (x.group(1)==null);}"
			+ "b = b AND (x.group(2)=='H');"
			+ "setResult(b);}");
		// Test XML schema gYear
		test("", "{Parser p = gYear(%minInclusive=1999);\nsetResult(p.parse('1999'));}");
		test("", "{Parser p = gYear(%minInclusive=1999);\nsetResult(!p.parse('1998'));}");
		test("", "{Parser p = gYear(%minInclusive=1999);\nParseResult r = p.parse('1999');\n"
			+ "setResult(r.matches());}");
		test("", "{Parser p = gYear(%minInclusive=1999);\nParseResult r = p.parse('1998');\n"
			+ "setResult(!r.matches());}");
		test("1999", "{setResult(gYear(%minInclusive=1999));}");
		test("1999", "{setResult(gYear(%minInclusive=1999).parse());}");
		test("1990", "{setResult(!gYear(%minInclusive=1999));}");
		test("1990", "{setResult(!gYear(%minInclusive=1999).parse());}");
		// test email
		testAttr2("tr.ab@vol.cz","onTrue setResult(true);required emailAddr();onFalse setResult(false);");//OK
		testAttr2("&lt;p-G@d-t.o&gt;","onTrue setResult(true);"
			+ "required emailAddr();onFalse setResult(false);"); // OK
		testAttr2("Pa Gr &lt;p-G@d-t.o&gt;","onTrue setResult(true);"
			+ "required emailAddr();onFalse setResult(false);"); // OK
		testAttr2("(Pa Gr)p-G@d-t.o","onTrue setResult(true);"
			+ "required emailAddr();onFalse setResult(false);"); // OK
		testAttr2("p-G@d-t.o(Pa Gr)","onTrue setResult(true);"
			+ "required emailAddr();onFalse setResult(false);"); // OK
		testAttr2("(a a) =?UTF-8?Q?Xx. Yy?=(b)&lt;1@2g>(c)","onTrue setResult("
			+ "true); required emailAddr(); onFalse setResult(false);"); // OK
		testAttr2("tro.cz","onTrue setResult(false); required emailAddr();"
			+ " onFalse setResult(true);"); // missing "@"
		testAttr2("@trovolny.cz","onTrue setResult(false);required emailAddr();"
			+ " onFalse setResult(true);"); // missing local name
		testAttr2("trovolny.cz@","onTrue setResult(false);required emailAddr();"
			+ " onFalse setResult(true);"); // missing domain
		testAttr2("tr@@vol.cz","onTrue setResult(false); required emailAddr();"
			+ " onFalse setResult(true);"); // more than one "@"
		testAttr2("tr@vol@ny.cz","onTrue setResult(false);required emailAddr();"
			+ " onFalse setResult(true);"); // more than one "@"
		testAttr2("a b t@v","onTrue setResult(true); required emailAddr();"
			+ " onFalse setResult(true);"); // OK
		// emailAddrList
		testAttr2("t@v.c(ab)","onTrue setResult(true);required emailAddrList();"
			+ " onFalse setResult(true);"); // OK
		testAttr2("t@v.cc,a@bb.cc","onTrue setResult(true);"
			+ "required emailAddrList();onFalse setResult(false);"); // OK
		testAttr2("@v","onTrue setResult(true); required emailAddrList();"
			+ " onFalse setResult(true);"); // local part missing
		testAttr2("t@v.","onTrue setResult(true); required emailAddrList();"
			+ " onFalse setResult(true);"); // top domain part missing
		testAttr2(" x &lt;t@v.cc>\t (a b) ;\n (c d) a@b.cc ", "onTrue setResult"
			+ "(true); required emailAddrList();" // OK, white spaces allowed
			+ " onFalse setResult(false);");
		testAttr2("t@v.cc a@bb.cc","onTrue setResult(true);required"
			+ " emailAddrList();onFalse setResult(true);"); //missing ";" or ","
		// switch (String) tests
		testCheckMethod("12", "boolean m(){switch(getText()){case '12': " +
				"setResult(true); return true; default: return false;}}","m()");
		testCheckMethod("ahoj", "boolean m(){switch(getText()){case 'ahoj'"
			+ ": setResult(true);return true; default: setResult(false);"
			+ " return false;}}", "m()");
		testCheckMethod("ahoj", "boolean m(){switch(getText()){case'ahoj':"
			+ "case'nazdar':setResult(true);return true;}setResult(false);"
			+ "return false;}", "m()");
		testCheckMethod("ahoj", "boolean xxx(String s, int i){"
			+ "if (i != 999) {setResult(false); return false;}"
			+ "switch(s){}setResult(true);return true;}",
			"xxx(getText(),999);");
		testCheckMethod("ahoj", "boolean xxx(String s, int i){if (i != 999) {"
			+ "setResult(false); return false;} switch(s){"
			+ "case'ahoj':case'nazdar':"
			+ "setResult(true);return true;} setResult(false); return false;}",
			"xxx('ahoj',999);");
		testCheckMethod("ahoj", "boolean xxx(String s, int i){if (i != 999) {"
			+ "setResult(false); return false;} switch(s){"
			+ "case'ahoj': setResult(true);return true;"
			+ "default: setResult(false); return false;}}",
			"xxx(getText(),999);");
////////////////////////////////////////////////////////////////////////////////
		testAttr2("+1.21","onTrue setResult(true); required decimal; onFalse setResult(false); ");
		testAttr2("ahoj","required enum('nazdar','ahoj');onTrue"
			+ " setResult(true);onFalse {clearReports(); setResult(false);}");
		testAttr2("hoj","required enum('nazdar','ahoj');onTrue"
			+ " setResult(false);onFalse {clearReports(); setResult(true);}");
		testAttr2("Ahoj","required enum('nazdar','ahoj');"+
			"onTrue setResult(false); onFalse {clearReports(); setResult(true);}");
		testAttr2("hoj","required  enum('nazdar','ahoj');" +
			"onTrue setResult(false); onFalse {clearReports(); setResult(true);}");
		testAttr2("ahoj","required; onTrue setResult(true); onFalse {clearReports(); setResult(false);}");
		testAttr2("ahoj","required string(); onTrue setResult(true);"
			+ "onFalse {clearReports(); setResult(false);}");
		testAttr2("ahoj", "required; finally setResult(true);");
		testAttr2("ahoj", "required; onTrue setResult(true);");
		testAttr2("ahoj", "required eq('ahoj'); onTrue setResult(true); onFalse setResult(false);");
		testAttr2("ahoj", "required eq('nazdar'); onTrue setResult(false);"
			+ "onFalse {clearReports(); setResult(true);}");
		testAttr2("ahoj", "required eq('ahoj'); onTrue setResult(false);"
			+ "onFalse setResult(false); finally setResult(true);");
		testAttr2("nazdar","required eq('nazdar');\nonFalse setResult(false); onTrue setResult(true);");
		testAttr2("nazdar","onTrue setResult(true); required string(1,100); onFalse setResult(false);");
		testAttr2("+1.21","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+1,21","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+0.21","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+0,21","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+0000000000.21", "onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+00000000001.21", "onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+0000000.00","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+0000000,00","onTrue setResult(true); required dec(3,2); onFalse setResult(false); ");
		testAttr2("+10.21","onTrue setResult(false); required dec(3,2); onFalse setResult(true); ");
		testAttr2("10.21","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2("0","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2("1234","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2(".1234","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2(",1234","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2("0.","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2("0,","onTrue setResult(true); required dec(4); onFalse setResult(false); ");
		testAttr2("12.1234","onTrue {setResult(false);} required dec(4); onFalse setResult(true); ");
		testAttr2("123456.12345","onTrue setResult(true);required dec(11,5); onFalse setResult(false); ");
		testAttr2("1234567.1234", "onTrue setResult(false); required dec(11,5); onFalse setResult(true); ");
		testAttr2("1234567,1234", "onTrue setResult(false); required dec(11,5); onFalse setResult(true); ");
		testAttr2("aaacaaacaaa", "onTrue {setResult(replace(getText(),'aaa','ab') EQ 'abcabcab');}"
			+ "required string(); onFalse setResult(false); ");
		testAttr2("aaacaaa", "onTrue {setResult(replaceFirst(getText(),'aaa','a') EQ 'acaaa');}"
			+ "required string(); onFalse setResult(false); ");
		testAttr2("bcr", "onTrue {setResult(translate(getText(),'abc','ABa') EQ 'Bar');}"
			+ "required string(); onFalse setResult(false); ");
		testAttr2("-abc-", "onTrue {setResult(translate(getText(),'ab-','BA') EQ 'BAc');}"
			+ "required string(); onFalse setResult(false); ");
		testAttr2("//X/y","onTrue setResult(true); required file(); onFalse setResult(false); ");
		testAttr2("Sun, 18 Nov 2001 23:42:39 +0100",
			"onTrue setResult(true); required emailDate(); onFalse setResult(false); ");
		testAttr2("Tue, 27 Nov 2001 12:17:54 +0100 (CET)", "onTrue setResult(true); required emailDate();"
			+ " onFalse setResult(false); ");
		testAttr2("http://pes.eunet.cz","onTrue setResult(true);required url(); onFalse setResult(false); ");
////////////////////////////////////////////////////////////////////////////////
// Test check methods
////////////////////////////////////////////////////////////////////////////////
//		_printCode = true;
		_printCode = false;
		testCheckMethod("ahoj", "boolean m(){setResult(true); return true;}", "m()");
		testCheckMethod("ahoj", "boolean m(){String val = getText();\n"
			+ "boolean result = val=='ahoj';\n"
			+ "setResult(result); return result;}\n", "m()");
		testCheckMethod("ahoj", "boolean m(){String val = getText(); boolean result = false;\n"
			+ "result = (val=='nazdar') | (val=='ahoj');\n"
			+ "setResult(result); return result;}\n", "m()");
		testCheckMethod("ahoj", "boolean m(){String val=getText(); setResult(val!='nazdar');"
			+ "return val!='nazdar';}", "m()");
		testCheckMethod("ahoj", "boolean m(){setResult(eq('ahoj')); return eq('ahoj');}", "m()");
		testCheckMethod("ahoj", "boolean m(){String val=getText(); if(val == 'ahoj')"
			+ "{setResult(true);return true;} setResult(false); return false;}",
			"m()");
		testCheckMethod("ahoj", "boolean xxx(String s, int i){if (s != 'ahoj') {"
			+ "setResult(false); return false;}"
			+ " switch(i){}setResult(true);return true;}",
			"xxx(getText(),999);");
////////////////////////////////////////////////////////////////////////////////
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a date=\"required xdatetime('d.M.yyyy','HH:mm:ss')\"/>\n"+
"</xd:def>";
			xml = "<a date= '1.1.2000'/>";
			xp = compile(xdef);
			XDDocument xd = xp.createXDDocument();
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("00:00:00", el.getAttribute("date"));
			el = create(xp, "", reporter, xml);
			assertNoErrorwarnings(reporter);
			assertEq("00:00:00", el.getAttribute("date"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    <![CDATA[\n"+
"    int count;\n"+
"    boolean T(int i) {\n"+
"      count += i;\n"+
"      return true;\n"+
"    }\n"+
"    boolean F(int i) {\n"+
"      count += i;\n"+
"      return false;\n"+
"    }\n"+
"    void test() {\n"+
"      int i = 1; int j=1; int k=2; int l=3;\n"+
"      if (i > 2 ? true : false || true ? true : false) \n"+
"        out('OK 1 ');\n"+
"      else out('ERROR 1 ');\n"+
"      if (!(i > 2 ? true : false || true ? true : false)) \n"+
"        out('ERROR 2 ');\n"+
"      else out('OK 2 ');\n"+
"      i = (i > 2 ? true : false) ? 1 : 2;\n"+
"      if (i == 2) {\n"+
"        i = l;\n"+
"        out('OK 3 ');\n"+
"      } else out('ERROR 3 ');\n"+
"      if (!(i > 0 || i LT -1 && false)) out('ERROR 4 '); else out('OK 4 ');\n"+
"      boolean result = i > 0 || i LT -1 && false;\n"+
"      if (!result) out('ERROR 5 '); else out('OK 5 ');\n"+
"      if (i < 2 ? true : false || false ? true : i > 2)\n"+
"        out('OK 6 ');\n"+
"      else out('ERROR 6 ');\n"+
"      if (!(i < 2 ? true : false || false ? true : i > 2)) \n"+
"        out('ERROR 7 ');\n"+
"      else out('OK 7 ');\n"+
"      i=0; k = 2;\n"+
"      while((i==0) AND (j==1) OOR (k==2) AND (l==3)){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 21, i= ' +  i); else out('OK 21 ');\n"+
"      i = 0; k = 2;\n"+
"      while((i==0) AAND (j==1) OOR (k==2) AND (l==3)){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 22, i= ' +  i); else out('OK 22 ');\n"+
"      i = 0; k = 2;\n"+
"      while((i==0) AAND (j==1) OR (k==2) AAND (l==3)){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 23, i= ' +  i); else out('OK 23 ');\n"+
"      i = 0; k = 2;\n"+
"      while((i==0) AAND (j==1) OOR (k==2) AAND (l==3)){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 24, i= ' +  i); else out('OK 24 ');\n"+
"      i = 0; k = 2;\n"+
"      while((i==0) && (j==1) && (k==2) && (l==3) || i+j==1 || k+l==5){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 25, i= ' +  i); else out('OK 25 ');\n"+
"      i = 0; k = 2;\n"+
"      while(i+j == 1 || k+l == 5 || ((i==0) && (j==1) && (k==2) & (l==3))){\n"+
"        i++; k++;\n"+
"      }\n"+
"      if (i != 1) out('ERROR 26, i= ' +  i); else out('OK 26 ');\n"+
"      count = 0;\n"+
"      if (i < 2 ? T(1) : F(2) || T(4) ? T(8) : F(16)) {\n"+
"        if (count != 1) out('ERROR 40: ' + count);\n"+
"      } else out('ERROR 41: ' + count);\n"+
"      count = 0;\n"+
"      if (i > 2 ? T(1) : F(2) || T(4) ? T(8) : F(16)) {\n"+
"        if (count != 14) out('ERROR 42: ' + count);\n"+
"      } else out('ERROR 43: ' + count);\n"+
"      count = 0;\n"+
"      if (T(1) || F(2) || T(4) && F(8) || T(16)) {\n"+
"        if (count != 1) out('ERROR 44: ' + count);\n"+
"      } else out('ERROR 45: ' + count);\n"+
"      count = 0;\n"+
"      if (F(1) || T(2) || T(4) && F(8) || T(16)) {\n"+
"        if (count != 3) out('ERROR 46: ' + count);\n"+
"      } else out('ERROR 47: ' + count);\n"+
"      count = 0;\n"+
"      if (F(1) && F(2) || T(4) && F(8) || T(16)) {\n"+
"        if (count != 29) out('ERROR 48: ' + count);\n"+
"      } else out('ERROR 49: ' + count);\n"+
"      outln;\n"+
"    }\n"+
"    ]]>\n"+
"  </xd:declaration>\n"+
"  <a xd:script=\"occurs 1..; finally test();\" />\n"+
"</xd:def>\n";
			xml ="<a/>";
			assertFalse(test(xdef, xml, "", 'P', xml,
				"OK 1 OK 2 OK 3 OK 4 OK 5 OK 6 OK 7 OK 21 OK 22 OK 23 OK 24 OK 25 OK 26 \n"));
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    void x() {\n"+
"      Decimal d = decimalValue('123');\n"+
"      Decimal e = decimalValue(2);\n"+
"      Decimal f = decimalValue($PI);\n"+
"      out(f.toString() + '; ' + intValue(f));\n"+
"      out(add(d,e).toString());\n"+
"      out(divide(d,e).toString());\n"+
"      out(multiply(d,e).toString());\n"+
"      out(remainder(d,e).toString());\n"+
"    }\n"+
"  </xd:declaration>\n"+
"  <a xd:script=\"onStartElement x()\"/>\n"+
"</xd:def>\n";
			xml = "<a/>";
			assertFalse(test(xdef, xml, "", 'P', xml,
"3.141592653589793115997963468544185161590576171875; 312561.52461"));
			xdef = // Test hasAttribute and hasNamedItem
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration><![CDATA[\n"+
"    Container c = [%a='a'];"+
"    Element e = xparse(\"<a a='a'/>\");\n]]>\n"+
"  </xd:declaration>\n"+
"  <a xd:script=\"finally {\n"+
"    if (e.hasAttribute('x')) error('err1');\n"+
"    if (!e.hasAttribute('a')) error('err2');\n"+
"    if (c.hasNamedItem('x')) error('err3');\n"+
"    if (!c.hasNamedItem('a')) error('err4');\n"+
"  }\"/>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xp.createXDDocument().xparse("<a/>", reporter);
			assertNoErrorwarnings(reporter);
			xdef = // Test reference to model with different namespace
"<xd:def xmlns:xd='" + _xdNS + "' name='A' xmlns:a='a.b' xmlns:b='c.d'\n"+
"        root='a:a | b:a'>\n"+
"  <a:a a='int()' a:b='int()' >\n"+
"    <a:b a='int()' a:b='int()' />\n"+
"  </a:a>\n"+
"  <b:a xd:script='ref A#a:a' b:c='int()'> int(); </b:a>\n"+
"  <xd:component>\n"+
"   %class "+_package+".MyTest10 %link A#a:a;\n" +
"   %class "+_package+".MyTest11 %link A#b:a;\n" +
"  </xd:component>\n" +
"</xd:def>";
			genXComponent(xp = compile(xdef));
			xml = "<a:a xmlns:a='a.b' a='1' a:b='2'><a:b a='3' a:b='4'/></a:a>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
			xml = "<b:a xmlns:b='c.d' a='1' b:b='2' b:c='3'><b:b a='4' b:b='5'/>6</b:a>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			xc = xp.createXDDocument().xparseXComponent(xml, null, reporter);
			assertNoErrors(reporter);
			assertEq(xml, xc.toXml());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='A' root='a' xmlns='a.b' xmlns:a='a.b'>\n"+
"  <a a='int()' a:b='int()' >\n"+
"    <b a='int()' a:b='int()' />\n"+
"  </a>\n"+
"</xd:def>\n" +
"<xd:def name='B' root='a' xmlns='c.d' xmlns:a='a.b'>\n"+
"  <a xd:script='ref A#a:a'/>\n"+
"</xd:def>\n" +
"<xd:component>\n"+
" %class "+_package+".MyTest20 %link A#a;\n" +
" %class "+_package+".MyTest22 %link B#a;\n" +
"</xd:component>\n" +
"</xd:collection>";
			genXComponent(xp = compile(xdef));
			xml = "<b:a xmlns:b='a.b' a='1' b:b='2'><b:b  a='3' b:b='4'/></b:a>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b:a xmlns:b='c.d' a='1' b:b='2'><b:b  a='3' b:b='4'/></b:a>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = // Test reference to model with different namespace
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='A' root='a' xmlns='a.b' xmlns:a='a.b'>\n"+
"  <a a='int()' a:b='int()' >\n"+
"    <b a='int()' a:b='int()' />\n"+
"  </a>\n"+
"</xd:def>\n" +
"<xd:def name='B' root='a' xmlns:a='a.b'>\n"+
"  <a xd:script='ref A#a:a' />\n"+
"</xd:def>\n" +
"<xd:component>\n"+
" %class "+_package+".MyTest30 %link A#a;\n" +
" %class "+_package+".MyTest32 %link B#a;\n" +
"</xd:component>\n" +
"</xd:collection>";
			genXComponent(xp = compile(xdef));
			xml ="<b:a xmlns:b='a.b' a='1' b:b='2'><b:b  a='3' b:b='4'/></b:a>";
			assertEq(xml, parse(xp, "A", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a='1' xmlns:a='a.b' a:b='2'><a:b  a='3' a:b='4'/></a>";
			assertEq(xml, parse(xp, "B", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = // test CHECK operator
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" root=\"A\" >\n" +
"  <xd:component>%class "+_package+".MytestX_CHECK %link #A;</xd:component>\n" +
"  <xd:declaration>\n" +
"    type p string(1,3) CHECK regex('[0-9]{3}');\n" +
"    boolean chk() {\n"+
"      return regex('[0-9]{3}').parse().matches();\n" +
"   }\n" +
"  </xd:declaration>\n" +
"  <A a=\"? p();\" >\n" +
"    <b xd:script=\"occurs *\" >\n" +
"      optional string(1,3) CHECK chk();\n" +
"    </b>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xml = "<A a=\"abc\"/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			if (reporter.errorWarnings()) {
				assertTrue(reporter.printToString().contains("XDEF822"));
			}
			reporter.clear();
			xml = "<A a=\"123\"/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml = "<A a=\"1234\"/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			if (reporter.errorWarnings()) {
				assertTrue(reporter.printToString().contains("XDEF815"));
			}
			reporter.clear();
			xml = "<A><b>abc</b><b>1234</b></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			if (reporter.errorWarnings()) {
				s = reporter.printToString();
				assertTrue(s.contains("XDEF822") && s.contains("XDEF815"));
			}
			reporter.clear();
			xml = "<A><b>123</b></A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
			xc = parseXC(xp,"", xml , null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <xd:declaration>\n" +
"    ParseResult a(){return new ParseResult(getText());}\n" +
"    type b long();\n" +
"    boolean c() { return (getText() GE '1') AAND (getText() LE '4'); }\n" +
"    type x string(1);\n" +
"    type y x CHECK c();\n" +
"  </xd:declaration>\n" +
"  <xd:component>%class "+_package+".MytestX_CHK1 %link #A;</xd:component>\n" +
"  <A a='x() CHECK c();' b='y();' c='a() CHECK c' d='b() CHECK c'/>\n" +
"</xd:def>";
			xp = compile(xdef);
			genXComponent(xp);
			xml = "<A a='1' b='2' c='3' d='4'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarningsAndClear(reporter);
			xd = xp.createXDDocument();
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			setProperty(XDConstants.XDPROPERTY_MINYEAR, "1900");//min hodnota
			setProperty(XDConstants.XDPROPERTY_MAXYEAR, "2100");//max hodnota
			setProperty(XDConstants.XDPROPERTY_SPECDATES, "3000-12-31, 3000-12-31T23:59:59");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A\" >\n" +
"  <A>\n" +
"    <xd:mixed>\n" +
"		<B xd:script='*' d=\"date()\" />\n" +
"		<C xd:script='*' d=\"dateTime()\" />\n" +
"    </xd:mixed>\n" +
"  </A>\n" +
"</xd:def>";
			xp = compile(xdef);

			xml =
"<A>\n"+
"  <B d='3000-12-31' />\n"+
"  <C d='3000-12-31T00:00:00' />\n"+
"  <C d='3000-12-31T23:59:59' />\n"+
"  <C d='3000-12-31T23:59:58' />\n"+
"</A>";
			assertEq(xml, parse(xp, "", xml, reporter));
			s = reporter.printToString();
			assertTrue(reporter.getErrorCount() == 2 && s.contains("/A/C[1]/@d") && s.contains("/A/C[3]/@d"));
			reporter.clear();
		} catch (RuntimeException ex) {fail(ex); reporter.clear();}
		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
