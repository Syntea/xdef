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

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import test.utils.XDTester;

/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll {
    
    /** prepare tests */
    @BeforeTest
    public static void beforeTests() {
        XDTester.setFulltestMode(false);
    }

    /** run TestAll in test.common */
    @Test
    public static void testCommon() {
        Assert.assertEquals(test.common.TestAll.runTests(new String[0]), 0);
    }
        
    /** run TestAll in test.xdef */
    @Test(dependsOnMethods = {"testCommon"})
    public static void testXdef() {
        Assert.assertEquals(test.xdef.TestAll.runTests(new String[0]), 0);
    }
    
    /** run TestAll in test.xdutil */
    @Test(dependsOnMethods = {"testXdef"})
    public static void testXDUtils() {
        Assert.assertEquals(test.xdutils.TestAll.runTests(new String[0]), 0);
    }

    
    
    /**Run tests with TestNG */
    private static void mainTestNG() {
		List<String> suiteList = new ArrayList<String>();
		suiteList.add("src/test/resources/testng.xml");
		
		TestNG testNG = new TestNG();
		testNG.setTestSuites(suiteList);
		//testNG.setTestClasses(new Class<?>[] {TestAll.class});
		testNG.setOutputDirectory("target/test-output/report");
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
