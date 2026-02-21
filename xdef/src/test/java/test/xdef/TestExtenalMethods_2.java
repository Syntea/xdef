package test.xdef;

import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import java.math.BigDecimal;

/** Class containing external methods.
 * @author Vaclav Trojan
 */
public class TestExtenalMethods_2 extends TestExtenalMethods_1 {
	final public static long m30(XXNode d) {return 1;}
	final public static String m30(XXNode d, long x) {return "" + x;}
	final public static long m31(XXData d) {return 1;}
	final public static String m31(XXData d, long x) {return "" + x;}
	final public static long m32(XXElement d) {return 1;}
	final public static String m32(XXElement d, long x) {return "" + x;}
	final public static Integer m33(XXElement d) {return Integer.decode("1");}
	final public static String m33(XXElement d, Integer x) {return "" + x;}
	final public static BigDecimal m34() {return new BigDecimal("1");}
	final public static String m34(BigDecimal x) {return "" + x;}
	final public static int m35(XXElement d) {return Integer.parseInt("1");}
	final public static String m35(XXElement d, int x) {return "" + x;}
}