package test.xdutils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.xdef.XDConstants;
import test.XDTester;

/** Run all available tests for package org.xdef.util  with all features of the tester.
 * @author Vaclav Trojan
 */
public class TestAllFull {

    /** Run all available tests in this package
     * @param args The array of arguments
     * @return number of errors.
     */
    public static final int runTests(final String... args) {
        XDTester.setFulltestMode(true);
        PrintStream log;
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream("testUtils.log");
            log = new PrintStream(fis);
        } catch (IOException ex) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException x) {}
            }
            log = null;
        }
        String xdNS = XDTester._xdNS;
        XDTester[] tests = TestAll.getTests();
        XDTester._xdNS = XDConstants.XDEF31_NS_URI;
        System.out.println("[INFO] Testing Xdefinition utilities version 3.1");
        int result = XDTester.runTests(System.out, System.err, log,
            tests, "package xdutils", XDTester.getFulltestMode(), args);
        XDTester._xdNS = XDConstants.XDEF32_NS_URI;
        System.out.println("[INFO] Testing Xdefinition utilities version 3.2");
        result += XDTester.runTests(System.out, System.err, log,
            tests, "package xdutils", XDTester.getFulltestMode(), args);
        XDTester._xdNS = XDConstants.XDEF40_NS_URI;
        System.out.println("[INFO] Testing Xdefinition utilities version 4.0");
        result += XDTester.runTests(System.out, System.err, log,
            tests, "package xdutils", XDTester.getFulltestMode(), args);
        XDTester._xdNS = XDConstants.XDEF41_NS_URI;
        System.out.println("[INFO] Testing Xdefinition utilities version 4.1");
        result += XDTester.runTests(System.out, System.err, log,
            tests, "package xdutils", XDTester.getFulltestMode(), args);
        XDTester._xdNS = XDConstants.XDEF42_NS_URI;
        System.out.println("[INFO] Testing Xdefinition utilities version 4.2");
        result += XDTester.runTests(System.out, System.err, log,
            tests, "package xdutils", XDTester.getFulltestMode(), args);
        if (log!= null) {
            log.close();
        }
        XDTester._xdNS = xdNS;
        return result;
    }

    /** Run all available tests in this package
     * @param args list of of arguments
     */
    public static final void main(final String... args) {
        XDTester.setFulltestMode(true);
        System.exit(runTests(args));
    }
}