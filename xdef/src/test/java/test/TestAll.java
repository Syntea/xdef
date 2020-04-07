package test;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xdef.impl.code.DefXQueryExpr;


/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll {

	/** prepare tests */
	@BeforeTest
	public static void beforeTests() {
		XDTester.setFulltestMode(false);
		System.out.println("Testing java version: "
			+System.getProperty("java.version") + " (with"
			+ (DefXQueryExpr.isXQueryImplementation() ? "" : "out")
			+ " Saxon library) ...");
	}

	/** run TestAll in test.common */
	@Test
	public static void testCommon() {
		Assert.assertEquals(test.common.TestAll.runTests(new String[0]), 0);
	}

	/** run TestAll in test.xdef */
	@Test
//	@Test(dependsOnMethods = {"testCommon"})
	public static void testXdef() {
		Assert.assertEquals(test.xdef.TestAll.runTests(new String[0]), 0);
	}

	/** run TestAll in test.xdutil */
	@Test
//	@Test(dependsOnMethods = {"testXdef"})
	public static void testXDUtils() {
		Assert.assertEquals(test.xdutils.TestAll.runTests(new String[0]), 0);
	}

	/**Run tests with TestNG */
	private static void mainTestNG() {
		String[] suiteList = {
			"src/test/resources/testng.xml"
		};

		TestNG testNG = new TestNG();
		testNG.setTestSuites(Arrays.asList(suiteList));
		testNG.setOutputDirectory("target/test-reports");
		testNG.run();
	}

	/** Run all test directly */
	@SuppressWarnings("unused")
	private static void mainTest() {
		beforeTests();
		
		testCommon();
		testXdef();
		testXDUtils();
	}

	/** @param args the command line arguments. */
	public static void main(String... args) {
		//mainTest();
		mainTestNG();
	}
}