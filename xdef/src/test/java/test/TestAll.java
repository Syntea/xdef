package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
		System.out.println("[INFO] Java version: "
			+ System.getProperty("java.version") + " ("
			+ (DefXQueryExpr.isXQueryImplementation() ? "with" : "without")
			+ " Saxon library) ...");
		System.out.println(
			"[INFO] X-definition version: " + XDFactory.getXDVersion());
	}

	/** run TestAll in test.common */
	@Test
	public void testCommon() {
		Assertions.assertEquals(test.common.TestAll.runTests(), 0);
	}

	/** run TestAll in test.xdef */
	@Test
//	@Test(dependsOnMethods = {"testCommon"})
	public void testXdef() {
		Assertions.assertEquals(test.xdef.TestAll.runTests(new String[0]), 0);
	}

	/** run TestAll in test.xdutil */
	@Test
//	@Test(dependsOnMethods = {"testXdef"})
	public void testXDUtils() {
		Assertions.assertEquals(test.xdutils.TestAll.runTests(new String[0]), 0);
	}

	/** Run all tests directly */
	private void mainTest() {
		beforeTests();
		testCommon();
		testXdef();
		testXDUtils();
	}

	/** @param args the command line arguments. */
	public static void main(String... args) {
		new TestAll().mainTest();
	}
}