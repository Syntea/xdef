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

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

import test.xdef.Tester;

/** Execute all tests fast.
 * @author Vaclav Trojan
 */
public class TestAll {
    
    /** prepare tests */
    @BeforeTest
    public static void beforeTests() {
        Tester.setFulltestMode(false);
    }

    /** run TestAll in test.common */
    @Test
    public static void testCommon() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        a.assertEquals(test.common.TestAll.runTests(args0), 0);
    }
        
    /** run TestAll in test.xdef */
    @Test(dependsOnMethods = {"testCommon"})
    public static void testXdef() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};        
        a.assertEquals(test.xdef.TestAll.runTests(args0), 0);
    }
    
    /** run TestAll in test.xdutil */
    @Test(dependsOnMethods = {"testXdef"})
    public static void testXDUtils() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        a.assertEquals(test.xdutils.TestAll.runTests(args0), 0);
    }

    
    
    /**Run tests with TestNG */
    public static void mainTestNG() {
        TestNG              testNG = new TestNG();
        TestListenerAdapter tla    = new TestListenerAdapter();
        testNG.setTestClasses(new Class<?>[] {TestAll.class});
        testNG.setOutputDirectory("run/output/test-ng");
        testNG.addListener(tla);
        testNG.run();

        for (ITestResult result : tla.getFailedTests()) {
            String id = result.getTestClass().getName() 
                + "." + result.getName();
            Throwable ex = result.getThrowable();
            if (ex != null) {
                System.err.println(id + ":");
                ex.printStackTrace();
            } else {
                System.err.println(id + ": failure without exception!");
            }
        }
    }

    /** Run all test directly */
    public static void mainTest() {
        beforeTests();
        
        try {
            testCommon();
            testXdef();
            testXDUtils();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

 	
    
    /** @param args the command line arguments. */
	public static void main(String... args) {
	    //mainTest();
	    mainTestNG();
	}
}
