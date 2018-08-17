/*
 * File: TestJava.java
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
package test.common.bnf;

import cz.syntea.xdef.sys.BNFGrammar;
import cz.syntea.xdef.sys.StringParser;
import test.util.STester;

import java.io.File;

import org.testng.annotations.Test;

/** Test of BNF.
 * @author Vaclav Trojan
 */
public class TestJava extends STester {

	public TestJava() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}


////////////////////////////////////////////////////////////////////////////////
	@Test(groups = "common", enabled = false)
	@Override
	public void runUnitTest() {
		super.runUnitTest();
	}
	
	
	
	@Override
	public void test() {
		String s;
		BNFGrammar g;
		try {
			g = BNFGrammar.compile(
				null, new File(getDataDir() + "JavaSyntax.bnf"), null);
			s = "i+1";
			assertEq(s, parse(g, "additive_expression", s));
//			s =
//"/*soubor LICENSE.TXT.*/\n"+
//"package test.common.bnf;\n" +
//"import cz.syntea.xdef.sys.STester;\n" +
//"import x.Y;\n" +
//"public final class TestJava extends STester implements a.b, c.d {n" +
//"	public TestJava() {super();}\n" +
//"}" +
//"\n" +
//"";
//			assertEq(s, parse(g, "java_source", s));
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
