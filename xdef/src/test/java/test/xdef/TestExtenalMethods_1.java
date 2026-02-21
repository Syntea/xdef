package test.xdef;

import java.math.BigDecimal;

/** Class containing external methods.
 * @author Vaclav Trojan
 */
public class TestExtenalMethods_1 {
	final public static void m00() {}
	final public static void m00(long x) {}
	final public static byte m01() {return (byte) 1;}
	final public static String m01(byte x) {return "" + x;}
	final public static Byte m02() {return Byte.decode("1");}
	final public static String m02(Byte x) {return "" + x;}
	final public static short m03() {return (short) 1;}
	final public static String m03(short x) {return "" + x;}
	final public static Short m04() {return Short.decode("1");}
	final public static String m04(Short x) {return "" + x;}
	final public static int m05() {return 1;}
	final public static String m05(int x) {return "" + x;}
	final public static Integer m06() {return Integer.decode("1");}
	final public static String m06(Integer x) {return "" + x;}
	final public static long m07() {return 1;}
	final public static String m07(long x) {return "" + x;}
	final public static Long m08() {return Long.decode("1");}
	final public static String m08(Long x) {return "" + x;}
	final public static BigDecimal m09() {return BigDecimal.valueOf(1);}
	final public static String m09(BigDecimal x) {return "" + x;}
	final public static float m11() {return 1;}
	final public static String m11(float x) {return "" + x;}
	final public static Float m12() {return Float.valueOf("1.0");}
	final public static String m12(Float x) {return "" + x;}
	final public static double m13() {return 1;}
	final public static String m13(double x) {return "" + x;}
	final public static Double m14() {return Double.valueOf("1.0");}
	final public static String m14(Double x) {return "" + x;}
	final public static boolean m15() {return true;}
	final public static String m15(boolean x) {return "" + x;}
	final public static Boolean m16() {return Boolean.TRUE;}
	final public static String m16(Boolean x) {return "" + x;}
	final public static byte[] m20() {return new byte[]{1};}
	final public static String m20(byte[] x) {return "" + x.length;}
	final public static int m21() {return 1;}
	final public static int m21(int i) {return i;}
	final public static float m22() {return (float) 1.0;}
	final public static float m22(float f) {return f;}
}