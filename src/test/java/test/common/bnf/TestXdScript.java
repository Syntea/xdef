/*
 * File: TestBNF.java
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

import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.sys.BNFGrammar;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.File;
import org.w3c.dom.Element;
import test.xdef.Tester;

/** Test XDefinition script.
 * @author Vaclav Trojan
 */
public class TestXdScript extends STester {

	public TestXdScript() {super();}

	private static String parse(final BNFGrammar grammar,
		final String name,
		final String source) {
		try {
			if (grammar.parse(new StringParser(source), name)) {
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (grammar.getParser().eos()?
					"eos" : grammar.getParser().getPosition().toString()) +"; ";
			}
		} catch (Exception ex) {
			return "Exception " + ex;
		}
	}

	private static void printCode(final BNFGrammar g) {
		Object[] o = g.getAndClearParsedObjects();
		if (o == null) {
			System.out.println("ParsedObjects = null");
			return;
		} else if (o.length == 0) {
			System.out.println("ParsedObjects length = 0");
			return;
		}
		for (Object x : o) {
			System.out.println("\"" + x + "\"");
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		String s;
		BNFGrammar g;
		try {
			File f = new File(getDataDir()).getParentFile().getParentFile()
				.getParentFile().getParentFile();
			f = new File(f, "test/xdef/data/test/TestXdefOfXdef.xdef");
			Element e = KXmlUtils.parseXml(f).getDocumentElement();
			e = KXmlUtils.firstElementChildNS(
				e, e.getNamespaceURI(), "BNFGrammar");
			String bnfOfBNF = KXmlUtils.getTextValue(e);
			g = BNFGrammar.compile(null, bnfOfBNF, null);
			if (Tester.getFulltestMode()) {
				g = BNFGrammar.compile(null, g.toString(), null);
			}
/*labels not implemented yet*
			s = "{loop : while ( true ) continue loop ;}";
			assertEq(s, parse(grammar, "Block", s));
			s = "{loop : while ( true ) break loop ;}";
			assertEq(s, parse(grammar, "Block", s));
/*labels not implemented yet*/
			s = "( x() == 'B' )";
			assertEq(s, parse(g, "Expression", s));
			s = "void x(int a) {out();}";
			assertEq(s, parse(g, "MethodDeclaration", s));
//			printCode(g);
			s = "int x(int a, float b) {out(a+b);\nreturn 1;}";
			assertEq(s, parse(g, "MethodDeclaration", s));
//			printCode(g);
			s = "-a + -0d3 - 0xFE * 1e-2 % 1.2 / 0.5E3 + 'x*y'";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "(aa | !ab AND NOT bc OR !!! (cd AND de))";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "((String)i).substring(1)";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "((String)(i=a>b?c:d)).substring(1)";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "string(3,3)&eq('abc')|eq('xyz')";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "int a";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "final int a=1";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "int a=x()";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "int a = (1+2)/3";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "float a = 3.14592e1";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "String a = 'This is ''string'''+\" and other \"\"string\"\"\"";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "String a = /*empty string*/ ' ' + \" \"";
			assertEq(s, parse(g, "VariableDeclaration", s));
			s = "\n";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int a; void x(int a) {out();} type t int()\n";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) return 1;;}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) {return 1;}}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) {return 1;;;}}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) {return 1;} else return 2;}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) {return 1;} else {return 2;};}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) {{}{}} else {};}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "int x(){if (true) return 1; else return 2;}";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "  String t = ((String) 1.5 ) . subtring(1);";
			assertEq(s, parse(g, "DeclarationScript", s));
//			printCode(g);
			s = "  uniqueSet u flt;\n";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "  uniqueSet u {x: flt; y : optional flt;}\n";
			assertEq(s, parse(g, "DeclarationScript", s));
			s =
"type XY enum('XX','YY'); type flt float();\n" +
"type dat xdatetime('yyyy-MM-dd');uniqueSet uflt flt; uniqueSet udat dat;";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = " Locale loc = new Locale('cs', 'CZ'); ";
			assertEq(s, parse(g, "DeclarationScript", s));
			s =
"  int i = - 0x_ff_ff_ff_ff_ff_ff_ff__ff_;\n" +
"  float x = -1_1_.2_30_e2;\n" +
"  NamedValue nv = %x:y..n-v=%y=%z=-0d123__456_890_999_000_333.0;\n";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "external Element source";
			assertEq(s, parse(g, "DeclarationScript", s));

//			printCode(g);

			s = "{ i ++ ; ++ k ; j += 2;}";
			assertEq(s, parse(g, "Block", s));
//			printCode(g);
			s = "{while ( true ) return 1;}";
			assertEq(s, parse(g, "Block", s));
			s = "{while ( true ) continue;}";
			assertEq(s, parse(g, "Block", s));
			s = "{while (true) break;}";
			assertEq(s, parse(g, "Block", s));
			s = "{do{return 1;}while(true);}";
			assertEq(s, parse(g, "Block", s));
			s = "{for ( int i= 0; i < 2; i++) return 1;}";
			assertEq(s, parse(g, "Block", s));
			s = "{for ( int i= 0; i < 2; i++) { return 1; } }";
			assertEq(s, parse(g, "Block", s));
			s = "{for(i=0;i<2;i++){return 1;}}";
			assertEq(s, parse(g, "Block", s));
			assertEq(s, parse(g, "Block", s));
			s = "{for(int i=0;i<b.size();i++)b.setAt(i,i);}";
			assertEq(s, parse(g, "Block", s));
			s = "{{for ( int i= 0; i < 2; i++) return 1;}}";
			assertEq(s, parse(g, "Block", s));
			s = "{\n"+
				"  for (int i = 0; i LT b.size(); i++) {\n"+
				"     b.setAt(i,i);\n"+
				"   }\n"+
				"}";
			assertEq(s, parse(g, "Block", s));
//			printCode(g);
			s = "{switch (x) {case 1: case 2: x(); break; default: i=0; };}";
			assertEq(s, parse(g, "Block", s));
			s = "{throw new Exception('abc');}";
			assertEq(s, parse(g, "Block", s));
			s = "{try{i=0;}catch(Exception x){x();}}";
			assertEq(s, parse(g, "Block", s));
			s = "{ try { i = 0 ; } catch ( Exception x ) { x ( ) ; } }";
			assertEq(s, parse(g, "Block", s));
			s = "\n";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "?";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "+";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "*";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "required{/*comment*/string(/**/);/*comment*/}";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "required string ( true, 'x', 2, 3.14E-10, (2 + 3) / 4 )";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "required false;onTrue x();onError{i=1;j=2;}finally outln();";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "match @a:a; + int(); options acceptQualifiedAttr";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "create {return (getElementName() == 'B') ? null : null;}";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "required string(3,3)AND(eq('abc')|eq('xyz'))";
			assertEq(s, parse(g, "AttributeScript", s));
			s =
"required {if (eq('nazdar'))\n"+
" {return parseDate('2000-5-1T20:43+01:00').isLeapYear();} return false;}\n"+
"onFalse setResult(false); onTrue setResult(true);";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "optional setResult(eq(s));finally {String s='???';int i=0;"
				+ "switch(i){case 1:break;case 2:break;case 3:s='12';}}";
			assertEq(s, parse(g, "AttributeScript", s));

			s = "x(1, 10, %min=3, %max='4')";
			assertEq(s, parse(g, "Method", s));
			s = "x(1, 10, %enumeration=[3,[5,7]])";
			assertEq(s, parse(g, "Method", s));
			s = "x(1, 10, %pattern=[ 'a*', '\\\\d*' ] )";
			assertEq(s, parse(g, "Method", s));
			s = "x(%enumeration=[3,5,7], %pattern=['a*', '\\\\d*'])";
			assertEq(s, parse(g, "Method", s));
			s = "\n";
			assertEq(s, parse(g, "ElementScript", s));
			s = "finally outln('x'); implements X#A";
			assertEq(s, parse(g, "ElementScript", s));
			s = "*; onAbsence{i = 1; j = - i--;} finally outln()";
			assertEq(s, parse(g, "ElementScript", s));
			s = "match @x==''; options acceptEmptyAttributes";
			assertEq(s, parse(g, "ElementScript", s));
			s = "finally outln('Number of people = ' + n + \n"+
				" ', average salary = ' + (sum/n));";
			assertEq(s, parse(g, "ElementScript", s));
			s = "ref SODContainer#SODContainer; init $phase = 'Template'";
			assertEq(s, parse(g, "ElementScript", s));

			s = "X#Y/@Z";
			assertEq(s, parse(g, "XPosition", s));
			s = "X#Y/$mixed[1]/A[22]/@Z";
			assertEq(s, parse(g, "XPosition", s));
			s = "X#Y/$mixed[1]/A[22]/$text[1]";
			assertEq(s, parse(g, "XPosition", s));
			s =
"external method String test.xdef.TestExtenalMethods_2.m35(XXElement, int);\n"+
"external method {\n"+
"void test.xdef.TestExtenalMethods_1.m00() as m;\n"+
"byte[] test.xdef.TestExtenalMethods_2.m20();\n"+
"String test.xdef.TestExtenalMethods_2.m20(byte[]);\n"+
"String test.xdef.TestExtenalMethods_2.m35(XXElement, int)\n"+
"}\n";
			assertEq(s, parse(g,"DeclarationScript", s));
//			printCode(g);
			s =
"  %class test.xdef.component.C2 %link C#Town/Street/House;\n" +
"  %interface test.xdef.component.CI %link C#Person;\n" +
"  %ref test.xdef.TestXComponents_W.W %link A#A/W;\n" +
"  %ref test.xdef.TestXComponents_B %link B#A;\n";
			assertEq(s, parse(g, "XCComponent", s));
			s = "void test.xdef.TestXComponents_C.test(XXData)";
			assertEq(s, parse(g, "MethodListItem", s));
//			printCode(g);
			if (Tester.getFulltestMode()) {
				s = g.toString();
				assertEq(s, parse(g, "BNFGrammar", s));
				assertEq(bnfOfBNF, parse(g, "BNFGrammar", bnfOfBNF));
			}
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
