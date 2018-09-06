/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: TestAll.java, created 2010-03-27.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import test.utils.XDTester;



/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll {
	
	/** prepare tests */
	@BeforeSuite
	@Parameters("fullTestMode")
	public static void beforeSuite(boolean fullTestMode) {
		XDTester.setFulltestMode(fullTestMode);
		
		logger.info("tests initialized");
	}

	/** finish tests */
	@AfterSuite
	public static void afterSuite() {
		logger.info("tests finished");
	}
	
	
	
	/** run TestAll in test.common */
	@Test(groups = "common", enabled = false)
	public static void testCommon() {
		Assert.assertEquals(test.common.TestAll.runTests(new String[0]), 0);
	}
	
	/** run TestAll in test.xdef */
	@Test(groups = "xdef", enabled = false)
	public static void testXdef() {
		Assert.assertEquals(test.xdef.TestAll.runTests(new String[0]), 0);
	}
	
	/** run TestAll in test.xdutil */
	@Test(groups = "xdutils", enabled = false)
	public static void testXDUtils() {
		Assert.assertEquals(test.xdutils.TestAll.runTests(new String[0]), 0);
	}
	
	
	
	/** run tests with TestNG */
	public static void mainTestNG() {
		List<String> suiteList = new ArrayList<String>();
		suiteList.add("src/test/resources/testng.xml");
		
		TestNG testNG = new TestNG();
		testNG.setTestSuites(suiteList);
		//testNG.setTestClasses(new Class<?>[] {TestAll.class});
		testNG.setOutputDirectory(testOutDir);
		testNG.run();
	}
	
	/** run all test directly */
	public static void mainTest() {
		beforeSuite(false);
		
		testCommon();
		testXdef();
		testXDUtils();
	}
	
 	
	
	/** @param args the command line arguments. */
	public static void main(String... args) {
		//mainTest();
		mainTestNG();
	}



	/** default output directory for TestNG */
	private static final String testOutDir = "target/test-output/report";
	/** logger */
	private static final Logger logger     = LoggerFactory.getLogger(
		TestAll.class
	);

}
