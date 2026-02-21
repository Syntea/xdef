package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xdef.XDFactory;
import org.xdef.impl.code.DefXQueryExpr;

/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll {

	/** prepare tests */
	@BeforeAll
	public static void beforeTests() {
		XDTester.setFulltestMode(false);
		System.out.println("[INFO] Java version: " + System.getProperty("java.version") + " ("
			+ (DefXQueryExpr.isXQueryImplementation() ? "with" : "without") + " Saxon library) ...");
		System.out.println("[INFO] X-definition version: " + XDFactory.getXDVersion());
	}

	/** run TestAll in test.common */
	@Test
	@Order(1)
	public void testCommon() {assertEquals(test.common.TestAll.runTests(), 0);}

	/** run TestAll in test.xdef */
	@Test
	@Order(2)
	public void testXdef() {assertEquals(test.xdef.TestAll.runTests(new String[0]), 0);}

	/** run TestAll in test.xdutil */
	@Test
	@Order(3)
	public void testXDUtils() {assertEquals(test.xdutils.TestAll.runTests(new String[0]), 0);}

	/** Run all tests directly */
	private static void mainTest() {
		beforeTests();
		new TestAll().testCommon();
		new TestAll().testXdef();
		new TestAll().testXDUtils();
	}

	/** @param args the command line arguments. */
	public static void main(String... args) {mainTest();}
}