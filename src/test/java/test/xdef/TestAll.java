/*
 * File: TestAll.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package test.xdef;

import cz.syntea.xdef.sys.STester;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/** Run all available tests for package cz.syntea.xd.
 * @author Vaclav Trojan
 */
public class TestAll {

	private TestAll() {}

	/** Run all available tests in this package
	 * @param args The array of arguments
	 * @return number of errors.
	 */
	public static int runTests(String[] args) {
		PrintStream log;
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream("testXdef.log");
			log = new PrintStream(fis);
		} catch (Exception ex) {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException x) {}
			}
			log = null;
		}
		STester[] tests = new STester[] {
			new Test000(),
			new Test001(),
			new Test002(),
			new TestBNF(),
			new TestCompose(),
			new TestConstruct(),
			new TestDatabase(),
			new TestDebug(),
			new TestDOMParse(),
			new TestDTDTypes(),
			new TestErrors(),
			new TestExtenalMethods(),
			new TestExternalVariables(),
			new TestGroups(),
			new TestImplementsAndUses(),
			new TestInclude(),
			new TestJSON(),
			new TestKeyAndRef(),
			new TestNamespaces(),
			new TestOptions(),
			new TestParse(),
			new TestSaxon(),
			new TestScript(),
			new TestTemplate(),
			new TestTryCatch(),
			new TestTypes(),
			new TestUserMethods(),
			new TestUserQuery(),
			new TestXComponents(),
			new TestXDChecker(),
			new TestXDGen(),
			new TestXDService(),
			new TestXSTypes(),
			new TestXdefOfXdef(),
			new TestXmlWriter(),
		};
		int result = STester.runTests(System.out, System.err, log,
			tests, "package xdef",
/*#if DEBUG*#/
			true, args);
/*#else*/
			false, args);
/*#end*/
		if (log != null) {
			log.close();
		}
		return result;
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
//		Tester.chkXDSyntax = true;
		if (runTests(args) > 0) {System.exit(1);}
	}
}
