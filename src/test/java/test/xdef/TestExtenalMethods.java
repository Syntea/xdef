package test.xdef;

import builtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDConstants;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.impl.parsers.XSParseInt;
import org.xdef.impl.parsers.XSParseName;
import java.io.StringWriter;
import org.xdef.XDContainer;
import org.xdef.XDValueID;

/** Test of user methods, process mode, create mode, groups.
 * @author Vaclav Trojan
 */
public final class TestExtenalMethods extends XDTester {

	public TestExtenalMethods() {super();}

	private int _m1 = 0;
	private long _m2 = 0;
	private int _m7 = 0;
	private int _m8 = 0;
	private int _m9 = 0;
	private int _m10 = 0;

	final public static void m1(final XXNode x) {
		((TestExtenalMethods) x.getUserObject())._m1 = 1;
	}
	final public static void m2(final XXNode x, final long i) {
		((TestExtenalMethods) x.getUserObject())._m2 = i;
	}
	final public static long m3(final XXNode x) {
		return ((TestExtenalMethods) x.getUserObject())._m1;
	}
	final public static long m4(final XXNode x) {
		return ((TestExtenalMethods) x.getUserObject())._m2;
	}
	final public static void m5(final XXNode x, final long i) {
		((TestExtenalMethods) x.getUserObject())._m1 = (int) i;
	}
	final public static void m6(final XXNode x, final long i) {
		((TestExtenalMethods) x.getUserObject())._m2 = i;
	}
	final public static void m7(final XXNode x, final XDValue[] p) {
		((TestExtenalMethods) x.getUserObject())._m7 =
			p[0].intValue() + p[1].intValue();
	}
	final public static void m8(final XXNode x, final long i,final XDContainer p){
		((TestExtenalMethods) x.getUserObject())._m8 =
			(int) i + p.getXDNamedItem("a").getValue().intValue();
	}
	final public static void m9(final XXNode x, final long i,final XDContainer p){
		((TestExtenalMethods) x.getUserObject())._m9 = (int) i;
	}
	final public static void m10(final XXNode x, final XDValue[] p) {
		((TestExtenalMethods) x.getUserObject())._m10 = p[0].intValue() +
			((XDContainer) p[1]).getXDNamedItem("a").getValue().intValue();
	}

	final public static void x(final XXElement x) {x.error("x0", "");}
	final public static void x(final XXElement x, final XDContainer a) {
		x.error("x1", "");
	}
	final public static void x(XXElement x, XDContainer a, XDContainer b) {
		x.error("x2", "");
	}

	final public static XDValue y() {return null;}
	final public static XDValue z() {return XDFactory.createXDValue("z");}

	final public static XDParseResult p2(XXNode xn, String s, long x) {
		return new XSParseInt().check(xn, s + x);
	}
	final public static int p3(final XDValue[] p) {return p.length;}
	final public static int p4(final long x) {return (int) x;}
	final public static String p5(final XXNode xn, final long x) {
		String result = String.valueOf(x);
		if (!"a".equals(xn.getElement().getNodeName())) {
			 result += "E1:" + xn.getElement().getNodeName() + ";";
		} else if (xn.getItemId() != XDValueID.XX_ELEMENT) {
			result += "E2:" + xn.getItemId() + ";";
		}
		return result;
	}
	final public static void p6(final XXNode xn, final XDValue x) {
		XDOutput out = xn.getXDDocument().getStdOut();
		out.writeString(x.toString());
		if (!"a".equals(xn.getElement().getNodeName())) {
			out.writeString("E1:" + xn.getElement().getNodeName() + ";");
		}
		if (xn.getItemId() != XDValueID.XX_ELEMENT) {
			out.writeString( "E2:" + xn.getItemId() + ";");
		}
	}
	final public static void p7(final XXElement xe, final XDValue[] p) {
		xe.getXDDocument().getStdOut().writeString(String.valueOf(p.length));
	}
	final public static int p8(final byte[] p) {return p.length;}
	final public static byte[] p9() {return new byte[] {1,2,3};}
	final public static XDParseResult p1(final XXData xd, final String x) {
		return new XSParseName().check(xd, x);
	}
	final public static void g1(final String s) {};
	final public static void t1(final XDContainer s) {};
	final public static void t2(final String s) {};
	final public static void t3(final XDContainer s) {};

	/** Simple user defined class loader. */
	private static class TestClassLoader extends ClassLoader {
		@Override
		final public Class<?> loadClass(final String name)
			throws ClassNotFoundException {
			// use super (Java default) implementation
			return super.loadClass(name);
		}
	}

	/** Simple incorrect user defined class loader. */
	private class TestFailClassLoader extends ClassLoader {
		@Override
		final public Class<?> loadClass(final String name)
			throws ClassNotFoundException {
			// this class loader doesn't find (and return) any Java class
			return null;
		}
	}

	@Override
	/** Run tests and print error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String xdef, xml;
		ArrayReporter reporter = new ArrayReporter();
		StringWriter strw;
		String s;
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"external method void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"external method {\n"+
"void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"void test.xdef.TestExtenalMethods_1.m00(long) as m;\n"+
"byte test.xdef.TestExtenalMethods_1.m01();\n"+
"String test.xdef.TestExtenalMethods_1.m01(byte);\n"+
"Byte test.xdef.TestExtenalMethods_1.m02();\n"+
"String test.xdef.TestExtenalMethods_1.m02(Byte);\n"+
"short test.xdef.TestExtenalMethods_1.m03();\n"+
"String test.xdef.TestExtenalMethods_1.m03(short);\n"+
"Short test.xdef.TestExtenalMethods_1.m04();\n"+
"String test.xdef.TestExtenalMethods_1.m04(Short);\n"+
"int test.xdef.TestExtenalMethods_1.m05();\n"+
"String test.xdef.TestExtenalMethods_1.m05(int);\n"+
"Integer test.xdef.TestExtenalMethods_1.m06();\n"+
"String test.xdef.TestExtenalMethods_1.m06(Integer);\n"+
"long test.xdef.TestExtenalMethods_1.m07();\n"+
"String test.xdef.TestExtenalMethods_1.m07(long);\n"+
"Long test.xdef.TestExtenalMethods_1.m08();\n"+
"String test.xdef.TestExtenalMethods_1.m08(Long);\n"+
"BigDecimal test.xdef.TestExtenalMethods_1.m09();\n"+
"String test.xdef.TestExtenalMethods_1.m09(BigDecimal);\n"+
"float test.xdef.TestExtenalMethods_1.m11();\n"+
"String test.xdef.TestExtenalMethods_1.m11(float);\n"+
"String test.xdef.TestExtenalMethods_1.m12(Float);\n"+
"Float test.xdef.TestExtenalMethods_1.m12();\n"+
"double test.xdef.TestExtenalMethods_1.m13();\n"+
"String test.xdef.TestExtenalMethods_1.m13(double);\n"+
"Double test.xdef.TestExtenalMethods_1.m14();\n"+
"String test.xdef.TestExtenalMethods_1.m14(Double);\n"+
"boolean test.xdef.TestExtenalMethods_1.m15();\n"+
"String test.xdef.TestExtenalMethods_1.m15(boolean);\n"+
"Boolean test.xdef.TestExtenalMethods_1.m16();\n"+
"String test.xdef.TestExtenalMethods_2.m16(Boolean);\n"+
"byte[] test.xdef.TestExtenalMethods_2.m20();\n"+
"String test.xdef.TestExtenalMethods_2.m20(byte[]);\n"+
"int test.xdef.TestExtenalMethods_2.m21();\n"+
"int test.xdef.TestExtenalMethods_2.m21(int);\n"+
"float test.xdef.TestExtenalMethods_2.m22();\n"+
"float test.xdef.TestExtenalMethods_2.m22(float);\n"+
"long test.xdef.TestExtenalMethods_2.m30(XXNode);\n"+
"String test.xdef.TestExtenalMethods_2.m30(XXNode, long);\n"+
"long test.xdef.TestExtenalMethods_2.m31(XXData);\n"+
"String test.xdef.TestExtenalMethods_2.m31(XXData, long);\n"+
"long test.xdef.TestExtenalMethods_2.m32(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m32(XXElement, long);\n"+
"Integer test.xdef.TestExtenalMethods_2.m33(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m33(XXElement, Integer);\n"+
"BigDecimal test.xdef.TestExtenalMethods_2.m34();\n"+
"String test.xdef.TestExtenalMethods_2.m34(BigDecimal);\n"+
"int test.xdef.TestExtenalMethods_2.m35(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m35(XXElement, int)\n"+
"}\n"+
"external method String test.xdef.TestExtenalMethods_2.m35(XXElement, int);\n"+
"</xd:declaration>\n"+
" <a\n"+
"  m00=\"?string;finally{m();m(1);}\"\n"+
"  m01=\"?string;finally{String s=m01(m01());if('1'!=s)setText(s);}\"\n"+
"  m02=\"?string;finally{String s=m02(m02());if('1'!=s)setText(s);}\"\n"+
"  m03=\"?string;finally{String s=m03(m03());if('1'!=s)setText(s);}\"\n"+
"  m04=\"?string;finally{String s=m04(m04());if('1'!=s)setText(s);}\"\n"+
"  m05=\"?string;finally{String s=m05(m05());if('1'!=s)setText(s);}\"\n"+
"  m06=\"?string;finally{String s=m06(m06());if('1'!=s)setText(s);}\"\n"+
"  m07=\"?string;finally{String s=m07(m07());if('1'!=s)setText(s);}\"\n"+
"  m08=\"?string;finally{String s=m08(m08());if('1'!=s)setText(s);}\"\n"+
"  m09=\"?string;finally{String s=m09(m09());if('1'!=s)setText(s);}\"\n"+
"  m11=\"?string;finally{String s=m11(m11());if('1.0'!=s)setText(s);}\"\n"+
"  m12=\"?string;finally{String s=m12(m12());if('1.0'!=s)setText(s);}\"\n"+
"  m13=\"?string;finally{String s=m13(m13());if('1.0'!=s)setText(s);}\"\n"+
"  m14=\"?string;finally{String s=m14(m14());if('1.0'!=s)setText(s);}\"\n"+
"  m15=\"?string;finally{String s=m15(m15());if('true'!=s)setText(s);}\"\n"+
"  m16=\"?string;finally{String s=m16(m16());if('true'!=s)setText(s);}\"\n"+
"  m20=\"?string;finally{String s=m20(m20());if('1'!=s)setText(s);}\"\n"+
"  m21=\"?string;finally{int i=m21(m21()+1);if(2!=i)setText(''+i);}\"\n"+
"  m22=\"?string;finally{float f=m22(m22()+1.0);if(2.0!=f)setText(''+f);}\"\n"+
"  m30=\"?string;finally{String s=m30(m30());if('1'!=s)setText(s);}\"\n"+
"  m31=\"?string;finally{String s=m31(m31());if('1'!=s)setText(s);}\"\n"+
"  m32=\"?string;finally{String s=m32(m32());if('1'!=s)setText(s);}\"\n"+
"  m33=\"?string;finally{String s=m33(m33());if('1'!=s)setText(s);}\"\n"+
"  m34=\"?string;finally{String s=m34(m34());if('1'!=s)setText(s);}\"\n"+
"  m35=\"?string;finally{String s=m35(m35());if('1'!=s)setText(s);}\"\n"+
" />\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a"
				+ " m00='x' m01='x' m02='x' m03='x' m04='x' m05='x'"
				+ " m06='x' m07='x' m08='x' m09='x'"
				+ " m11='x' m12='x' m13='x' m14='x' m15='x' m16='x'"
				+ " m20='x'" + " m21='x'" + " m22='x'"
				+ " m30='x' m31='x' m32='x' m33='x' m34='x' m35='x'"
				+ "/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration scope='local'>\n"+
"external method {\n"+
"void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"void test.xdef.TestExtenalMethods_1.m00(long) as m;\n"+
"byte test.xdef.TestExtenalMethods_1.m01();\n"+
"String test.xdef.TestExtenalMethods_1.m01(byte);\n"+
"Byte test.xdef.TestExtenalMethods_1.m02();\n"+
"String test.xdef.TestExtenalMethods_1.m02(Byte);\n"+
"short test.xdef.TestExtenalMethods_1.m03();\n"+
"String test.xdef.TestExtenalMethods_1.m03(short);\n"+
"Short test.xdef.TestExtenalMethods_1.m04();\n"+
"String test.xdef.TestExtenalMethods_1.m04(Short);\n"+
"int test.xdef.TestExtenalMethods_1.m05();\n"+
"String test.xdef.TestExtenalMethods_1.m05(int);\n"+
"Integer test.xdef.TestExtenalMethods_1.m06();\n"+
"String test.xdef.TestExtenalMethods_1.m06(Integer);\n"+
"long test.xdef.TestExtenalMethods_1.m07();\n"+
"String test.xdef.TestExtenalMethods_1.m07(long);\n"+
"Long test.xdef.TestExtenalMethods_1.m08();\n"+
"String test.xdef.TestExtenalMethods_1.m08(Long);\n"+
"BigDecimal test.xdef.TestExtenalMethods_1.m09();\n"+
"String test.xdef.TestExtenalMethods_1.m09(BigDecimal);\n"+
"float test.xdef.TestExtenalMethods_1.m11();\n"+
"String test.xdef.TestExtenalMethods_1.m11(float);\n"+
"String test.xdef.TestExtenalMethods_1.m12(Float);\n"+
"Float test.xdef.TestExtenalMethods_1.m12();\n"+
"double test.xdef.TestExtenalMethods_1.m13();\n"+
"String test.xdef.TestExtenalMethods_1.m13(double);\n"+
"Double test.xdef.TestExtenalMethods_1.m14();\n"+
"String test.xdef.TestExtenalMethods_1.m14(Double);\n"+
"boolean test.xdef.TestExtenalMethods_1.m15();\n"+
"String test.xdef.TestExtenalMethods_1.m15(boolean);\n"+
"Boolean test.xdef.TestExtenalMethods_1.m16();\n"+
"String test.xdef.TestExtenalMethods_2.m16(Boolean);\n"+
"byte[] test.xdef.TestExtenalMethods_2.m20();\n"+
"String test.xdef.TestExtenalMethods_2.m20(byte[]);\n"+
"int test.xdef.TestExtenalMethods_2.m21();\n"+
"int test.xdef.TestExtenalMethods_2.m21(int);\n"+
"float test.xdef.TestExtenalMethods_2.m22();\n"+
"float test.xdef.TestExtenalMethods_2.m22(float);\n"+
"long test.xdef.TestExtenalMethods_2.m30(XXNode);\n"+
"String test.xdef.TestExtenalMethods_2.m30(XXNode, long);\n"+
"long test.xdef.TestExtenalMethods_2.m31(XXData);\n"+
"String test.xdef.TestExtenalMethods_2.m31(XXData, long);\n"+
"long test.xdef.TestExtenalMethods_2.m32(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m32(XXElement, long);\n"+
"Integer test.xdef.TestExtenalMethods_2.m33(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m33(XXElement, Integer);\n"+
"BigDecimal test.xdef.TestExtenalMethods_2.m34();\n"+
"String test.xdef.TestExtenalMethods_2.m34(BigDecimal);\n"+
"int test.xdef.TestExtenalMethods_2.m35(XXElement);\n"+
"String test.xdef.TestExtenalMethods_2.m35(XXElement, int);\n"+
"}\n"+
"</xd:declaration>\n"+
" <a\n"+
"  m00=\"?string;finally{m();m(1);}\"\n"+
"  m01=\"?string;finally{String s=m01(m01());if('1'!=s)setText(s);}\"\n"+
"  m02=\"?string;finally{String s=m02(m02());if('1'!=s)setText(s);}\"\n"+
"  m03=\"?string;finally{String s=m03(m03());if('1'!=s)setText(s);}\"\n"+
"  m04=\"?string;finally{String s=m04(m04());if('1'!=s)setText(s);}\"\n"+
"  m05=\"?string;finally{String s=m05(m05());if('1'!=s)setText(s);}\"\n"+
"  m06=\"?string;finally{String s=m06(m06());if('1'!=s)setText(s);}\"\n"+
"  m07=\"?string;finally{String s=m07(m07());if('1'!=s)setText(s);}\"\n"+
"  m08=\"?string;finally{String s=m08(m08());if('1'!=s)setText(s);}\"\n"+
"  m09=\"?string;finally{String s=m09(m09());if('1'!=s)setText(s);}\"\n"+
"  m11=\"?string;finally{String s=m11(m11());if('1.0'!=s)setText(s);}\"\n"+
"  m12=\"?string;finally{String s=m12(m12());if('1.0'!=s)setText(s);}\"\n"+
"  m13=\"?string;finally{String s=m13(m13());if('1.0'!=s)setText(s);}\"\n"+
"  m14=\"?string;finally{String s=m14(m14());if('1.0'!=s)setText(s);}\"\n"+
"  m15=\"?string;finally{String s=m15(m15());if('true'!=s)setText(s);}\"\n"+
"  m16=\"?string;finally{String s=m16(m16());if('true'!=s)setText(s);}\"\n"+
"  m20=\"?string;finally{String s=m20(m20());if('1'!=s)setText(s);}\"\n"+
"  m21=\"?string;finally{int i=m21(m21()+1);if(2!=i)setText(''+i);}\"\n"+
"  m22=\"?string;finally{float f=m22(m22()+1.0);if(2.0!=f)setText(''+f);}\"\n"+
"  m30=\"?string;finally{String s=m30(m30());if('1'!=s)setText(s);}\"\n"+
"  m31=\"?string;finally{String s=m31(m31());if('1'!=s)setText(s);}\"\n"+
"  m32=\"?string;finally{String s=m32(m32());if('1'!=s)setText(s);}\"\n"+
"  m33=\"?string;finally{String s=m33(m33());if('1'!=s)setText(s);}\"\n"+
"  m34=\"?string;finally{String s=m34(m34());if('1'!=s)setText(s);}\"\n"+
"  m35=\"?string;finally{String s=m35(m35());if('1'!=s)setText(s);}\"\n"+
" />\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);

			xdef =
"<xd:def xmlns:xd='" + XDConstants.XDEF20_NS_URI + "' root='a'\n"+
"  classes='test.xdef.TestExtenalMethods_2'>\n"+
"<a\n"+
"m00=\"?string;finally{m00();m00(1);}\"\n"+
"m01=\"?string;\"\n"+ // byte not supported for declared class
"m02=\"?string;\"\n"+ // Byte not supported for declared class
"m03=\"?string;\"\n"+ // short not supported for declared class
"m04=\"?string;\"\n"+ // Short not supported for declared class
"m05=\"?string;finally{String s=m05(m05());if('1'!=s)setText(s);}\"\n"+
"m06=\"?string;finally{String s=m06(m06());if('1'!=s)setText(s);}\"\n"+
"m07=\"?string;finally{String s=m07(m07());if('1'!=s)setText(s);}\"\n"+
"m08=\"?string;finally{String s=m08(m08());if('1'!=s)setText(s);}\"\n"+
"m09=\"?string;finally{String s=m09(m09());if('1'!=s)setText(s);}\"\n"+
"m11=\"?string;finally{String s=m11(m11());if('1.0'!=s)setText(s);}\"\n"+
"m12=\"?string;finally{String s=m12(m12());if('1.0'!=s)setText(s);}\"\n"+
"m13=\"?string;finally{String s=m13(m13());if('1.0'!=s)setText(s);}\"\n"+
"m14=\"?string;finally{String s=m14(m14());if('1.0'!=s)setText(s);}\"\n"+
"m15=\"?string;finally{String s=m15(m15());if('true'!=s)setText(s);}\"\n"+
"m16=\"?string;finally{String s=m16(m16());if('true'!=s)setText(s);}\"\n"+
"m20=\"?string;finally{String s=m20(m20());if('1'!=s)setText(s);}\"\n"+
"m21=\"?string;finally{int i=m21(m21()+1);if(2!=i)setText(''+i);}\"\n"+
"m22=\"?string;finally{float f=m22(m22()+1.0);if(2.0!=f)setText(''+f);}\"\n"+
"m30=\"?string;finally{String s=m30(m30());if('1'!=s)setText(s);}\"\n"+
"m31=\"?string;finally{String s=m31(m31());if('1'!=s)setText(s);}\"\n"+
"m32=\"?string;finally{String s=m32(m32());if('1'!=s)setText(s);}\"\n"+
"m33=\"?string;finally{String s=m33(m33());if('1'!=s)setText(s);}\"\n"+
"m34=\"?string;finally{String s=m34(m34());if('1'!=s)setText(s);}\"\n"+
"m35=\"?string;finally{String s=m35(m35());if('1'!=s)setText(s);}\"\n"+
"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a\n"+
"m00=\"?string;finally{m00();m00(1);}\"\n"+
"m01=\"?string;\"\n"+ // byte not supported for declared class
"m02=\"?string;\"\n"+ // Byte not supported for declared class
"m03=\"?string;\"\n"+ // short not supported for declared class
"m04=\"?string;\"\n"+ // Short not supported for declared class
"m05=\"?string;finally{String s=m05(m05());if('1'!=s)setText(s);}\"\n"+
"m06=\"?string;finally{String s=m06(m06());if('1'!=s)setText(s);}\"\n"+
"m07=\"?string;finally{String s=m07(m07());if('1'!=s)setText(s);}\"\n"+
"m08=\"?string;finally{String s=m08(m08());if('1'!=s)setText(s);}\"\n"+
"m09=\"?string;finally{String s=m09(m09());if('1'!=s)setText(s);}\"\n"+
"m11=\"?string;finally{String s=m11(m11());if('1.0'!=s)setText(s);}\"\n"+
"m12=\"?string;finally{String s=m12(m12());if('1.0'!=s)setText(s);}\"\n"+
"m13=\"?string;finally{String s=m13(m13());if('1.0'!=s)setText(s);}\"\n"+
"m14=\"?string;finally{String s=m14(m14());if('1.0'!=s)setText(s);}\"\n"+
"m15=\"?string;finally{String s=m15(m15());if('true'!=s)setText(s);}\"\n"+
"m16=\"?string;finally{String s=m16(m16());if('true'!=s)setText(s);}\"\n"+
"m20=\"?string;finally{String s=m20(m20());if('1'!=s)setText(s);}\"\n"+
"m21=\"?string;finally{int i=m21(m21()+1);if(2!=i)setText(''+i);}\"\n"+
"m22=\"?string;finally{float f=m22(m22()+1.0);if(2.0!=f)setText(''+f);}\"\n"+
"m30=\"?string;finally{String s=m30(m30());if('1'!=s)setText(s);}\"\n"+
"m31=\"?string;finally{String s=m31(m31());if('1'!=s)setText(s);}\"\n"+
"m32=\"?string;finally{String s=m32(m32());if('1'!=s)setText(s);}\"\n"+
"m33=\"?string;finally{String s=m33(m33());if('1'!=s)setText(s);}\"\n"+
"m34=\"?string;finally{String s=m34(m34());if('1'!=s)setText(s);}\"\n"+
"m35=\"?string;finally{String s=m35(m35());if('1'!=s)setText(s);}\"\n"+
"/>\n"+
"</xd:def>";
			xp = compile(xdef,
				TestExtenalMethods_1.class,	TestExtenalMethods_2.class);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
			xp = compile(xdef, TestExtenalMethods_2.class);
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try { // using of user defined ClassLoader (fail state)
			XDBuilder xdb = XDFactory.getXDBuilder(null);
			xdb.setSource(
"<?xml version='1.0' encoding='UTF-8'?>\n"+
"<xd:def xmlns:xd='" + _xdNS + "' name='Test' root='Test'>\n"+
"<xd:declaration>\n"+
"  external method void test.xdef.TestExtenalMethods.g1(String);\n"+
"</xd:declaration>\n"+
"  <Test xd:script=\"finally g1('Hello...')\" />\n"+
"</xd:def>"
			);
			xdb.setClassLoader(new TestFailClassLoader());
			xdb.compileXD();
			fail("TestFailClassLoader had to cause an error that external "
				+ "method 'g1' wasn't found.");
		} catch (Exception ex) {
			// fail state is correct, because the TestFailClassLoader hasn't
			// found the class test.xdef.TestExtenalMethods
			if ((s = ex.getMessage()) == null ||
				s.indexOf("XDEF228") < 0 || // unknown class
				s.indexOf("XDEF443") < 0) {// unknown method
				fail(ex);
			}
		}
		try { // using of user defined ClassLoader (correct state)
			XDBuilder xdb = XDFactory.getXDBuilder(null);
			xdb.setSource(
"<?xml version='1.0' encoding='UTF-8'?>\n"+
"<xd:def xmlns:xd='" + _xdNS + "' name='Test' root='Test'>\n"+
"<xd:declaration>\n"+
"  external method void test.xdef.TestExtenalMethods.g1(String);\n"+
"</xd:declaration>\n"+
"  <Test xd:script=\"finally g1('Hello...')\" />\n"+
"</xd:def>"
			);
			xdb.setClassLoader(new TestClassLoader());
			xp = xdb.compileXD();
			parse(xp, "Test", "<Test/>", reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"<xd:declaration>\n"+
"  external method void test.xdef.TestExtenalMethods.g1(String);\n"+
"  external method int test.xdef.TestExtenalMethods.p4(long);\n"+
"</xd:declaration>\n"+
"  <A a=\"string; finally {g1('g1'); toString(p4(4)); \n"+
"      test.xdef.TestExtenalMethods.g1('.g1');\n"+
"      toString(test.xdef.TestExtenalMethods.p4(4))}; \n"+
"      onTrue fce()\" \n"+
"    xd:script=\"finally {g1('g1'); toString(p4(4)); \n"+
"      test.xdef.TestExtenalMethods.g1('.g1');\n"+
"      toString(test.xdef.TestExtenalMethods.p4(4)); fce()}\"/>\n"+
"  \n"+
"  <xd:declaration>\n"+
"    void fce() {\n"+
"      g1('g1'); \n"+
"      toString(p4(4)); \n"+
"      test.xdef.TestExtenalMethods.g1('.g1');\n"+
"      toString(test.xdef.TestExtenalMethods.p4(4));\n"+
"    }\n"+
"  </xd:declaration>\n"+
"</xd:def>";
			parse(xdef, null, "<A a='a'/>", reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
"external method {\n"+
"XDParseResult test.xdef.TestExtenalMethods.p1(XXData, String);\n"+
"XDParseResult test.xdef.TestExtenalMethods.p2(XXNode, String, long);\n"+
"int test.xdef.TestExtenalMethods.p3(XDValue[]);\n"+
"int test.xdef.TestExtenalMethods.p4(long);\n"+
"String test.xdef.TestExtenalMethods.p5(XXNode, long);\n"+
"void test.xdef.TestExtenalMethods.p6(XXNode, XDValue);\n"+
"void test.xdef.TestExtenalMethods.p7(XXElement, XDValue[]);\n"+
"int test.xdef.TestExtenalMethods.p3(XDValue[]) as q;\n"+
"void test.xdef.TestExtenalMethods.p7(XXElement, XDValue[]) as Q;\n"+
"int test.xdef.TestExtenalMethods.p8(byte[]);\n"+
"byte[] test.xdef.TestExtenalMethods.p9();\n"+
"}\n"+
"</xd:declaration>\n"+
"  <a xd:script = \"finally {Bytes b = p9();\n"+
"     AnyValue a = '6'; out(''+p1('p1')+p2('2',1)+p3()+p3(3,3,3)+p4(4)+\n"+
"     p5(5)+q()+q(1,'x',2,true)+ p8(b) + b.size());\n"+
"     p6(a); Q(); Q(1,'x',2,true);}\"/>\n"+
"</xd:def>\n";
			xml ="<a/>";
			xp = compile(xdef);
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("p12103450433604", strw.toString());
			strw = new StringWriter();
			assertEq(xml, create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq("p12103450433604", strw.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <a xd:script = \"finally {Bytes b = p9();\n"+
"     AnyValue a = '6'; out(''+p1('p1')+p2('2',1)+p3()+p3()+p3(3,3,3)+\n"+
"     p4(4)+p5(5) + p8(b) + b.size()); p6(a); p7(); p7(1,'x',2,true);}\"/>\n"+
"</xd:def>\n";
			xml ="<a/>";
			xp = compile(xdef, getClass());
			strw = new StringWriter();
			assertEq(xml, parse(xp, null, xml, reporter, strw, null, null));
			assertNoErrors(reporter);
			assertEq("p1210034533604", strw.toString());
			strw = new StringWriter();
			assertEq(xml, create(xp, null, "a", reporter, xml, strw, null));
			assertNoErrors(reporter);
			assertEq("p1210034533604", strw.toString());
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method XDValue test.xdef.TestExtenalMethods.y();\n"+
"  external method org.xdef.XDValue test.xdef.TestExtenalMethods.z();\n"+
"</xd:declaration>\n"+
"  <a a=\"string;\n"+
"        onTrue{if (y()==null) error('y'); error(z().toString());}\"/>\n"+
"</xd:def>";
			parse(xdef, null, "<a a = 'a'></a>", reporter);
			s = reporter.printToString();
			assertTrue(s.indexOf("y") > 0 && s.indexOf("z") > 0, s);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"external method {\n"+
//"  XDValue test.xdef.TestExtenalMethods.x();\n"+
"void test.xdef.TestExtenalMethods.x(org.xdef.proc.XXElement);\n"+
"void test.xdef.TestExtenalMethods.x(XXElement, org.xdef.XDContainer);\n"+
"void test.xdef.TestExtenalMethods.x(XXElement, XDContainer, XDContainer);\n"+
"}\n"+
"  Container p = null, q = null, r = null, s = null, t = null;\n"+
"</xd:declaration>\n"+
"  <a a = \"string; finally {x();x(p);x(p,q);}\">\n"+
"  </a>\n"+
"</xd:def>";
			parse(xdef, null, "<a a = 'a'></a>", reporter);
			s = reporter.printToString();
			assertTrue(s.indexOf("x0") > 0 &&
				s.indexOf("x1") > 0 && s.indexOf("x2") > 0, s);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"<xd:declaration>\n"+
"external method {\n"+
"void test.xdef.TestExtenalMethods.m1(XXNode);\n"+
"void test.xdef.TestExtenalMethods.m2(XXNode, long);\n"+
"long test.xdef.TestExtenalMethods.m3(XXNode);\n"+
"long test.xdef.TestExtenalMethods.m4(XXNode);\n"+
"void test.xdef.TestExtenalMethods.m5(XXNode, long);\n"+
"void test.xdef.TestExtenalMethods.m6(XXNode, long);\n"+
"void test.xdef.TestExtenalMethods.m7(XXNode, XDValue[]);\n"+
"void test.xdef.TestExtenalMethods.m8(XXNode, long, XDContainer);\n"+
"void test.xdef.TestExtenalMethods.m9(XXNode, long, XDContainer);\n"+
"void test.xdef.TestExtenalMethods.m10(XXNode, XDValue[]);\n"+
"}\n"+
"</xd:declaration>\n"+
"  <a a='string; finally {m1();m2(2); m5(m3()); m6(m4()); m7(7,8);\n"+
"        m8(1, %a = 2); m9(9, []); m10(1, %a = 2);}' />\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a = 'a'/>";
			xd = xp.createXDDocument();
			xd.setUserObject(this);
			xd.xparse(xml, null);
			assertEq(_m1, 1);
			assertEq(_m2, 2);
			assertEq(_m7, 15);
			assertEq(_m8, 3);
			assertEq(_m9, 9);
			assertEq(_m10, 3);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <a a='string; finally {m1();m2(2); m5(m3()); m6(m4()); m7(7,8);\n"+
"        m8(1, %a = 2); m9(9, []); m10(1, %a = 2);}' />\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xml = "<a a = 'a'/>";
			xd = xp.createXDDocument();
			xd.setUserObject(this);
			xd.xparse(xml, null);
			assertEq(_m1, 1);
			assertEq(_m2, 2);
			assertEq(_m7, 15);
			assertEq(_m8, 3);
			assertEq(_m9, 9);
			assertEq(_m10, 3);
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<xd:declaration>\n"+
"  external method {\n"+ //*
"    void test.xdef.TestExtenalMethods.t1(XDContainer);\n"+
"    void test.xdef.TestExtenalMethods.t2(String);\n"+
"    void test.xdef.TestExtenalMethods.t3(XDContainer);\n"+
"  }\n"+
"</xd:declaration>\n"+
"  <a a=\"string; finally {t1(from('/a/@a')); t2((String)from('/a/@a'));\n"+
"        t3(from('/a'));}\">\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a a='a'/>";
			parse(xdef, "", xml, reporter);
		} catch (Exception ex) {fail(ex);}
		try {// test declarations scope local and global
			xp = compile(new String[] {
"<xd:def xmlns:xd='" + _xdNS + "' name='A' root='a'>\n"+
"<xd:declaration scope='local'>\n"+
"  external method {\n"+
"    void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"    void test.xdef.TestExtenalMethods_1.m00(long) as m;\n"+
"    byte test.xdef.TestExtenalMethods_1.m01();\n"+
"    String test.xdef.TestExtenalMethods_1.m01(byte);\n"+
"  }"+
"  int e1=99;\n"+
"</xd:declaration>\n"+
" <a\n"+
"  m00=\"?string;finally{m();m(1);e();e(e1);e(e2)}\"\n"+
"  m01=\"?string;finally{String s=m01(m01());if('1'!=s)setText(s);}\"\n"+
" />\n"+
"</xd:def>",
"<xd:def xmlns:xd='" + _xdNS + "' name='B' root='b'>\n"+
"<xd:declaration scope='global'>\n"+
"  external method {\n"+
"    void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"    void test.xdef.TestExtenalMethods_1.m00(long) as m;\n"+
"    byte test.xdef.TestExtenalMethods_1.m01() as e;\n"+
"  }\n"+
"  external int e1;\n"+
"  int e2 = 2;\n"+
"  void e(int i) {outln('e(' + i +'); e1=' + e1 + ', e2=' + e2);}\n"+
"</xd:declaration>\n"+
" <b\n"+
"  b=\"?string; onTrue{m();m(1); e(); e(e1); e(e2);}\"\n"+
" />\n"+
"</xd:def>"});
			xd = xp.createXDDocument("A");
			xd.setVariable("e1", 3);
			xd.setVariable("e2", 4);
			strw = new StringWriter();
			xd.setStdOut(strw);
			parse(xd, "<a m00='a' m01='b'/>", reporter);
			assertEq("e(99); e1=3, e2=2\ne(2); e1=3, e2=2\n", strw.toString());
			xd = xp.createXDDocument("B");
			strw = new StringWriter();
			xd.setStdOut(strw);
			parse(xd, "<b b='a'/>", reporter);
//			assertEq("e(0); e1=0, e2=2\ne(2); e1=0, e2=2\n", strw.toString());
			assertEq("e(); e1=, e2=2\ne(2); e1=, e2=2\n", strw.toString());
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}

}
