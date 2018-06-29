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

/** Test XDefinition script.
 * @author Vaclav Trojan
 */
public class TestXdScript extends STester {
	public TestXdScript() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
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

	private void printCode(BNFGrammar g) {
		for (Object o : g.getAndClearParsedObjects()) {
			System.out.println("\"" + o + "\"");
		}		
	}
	
////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			File f = new File(getDataDir()).getParentFile().getParentFile()
				.getParentFile().getParentFile();
			f = new File(f, "test/xdef/data/test/TestXdefOfXdef.xdef");
			Element e = KXmlUtils.parseXml(f).getDocumentElement();
			e = KXmlUtils.firstElementChildNS(e,
				e.getNamespaceURI(), "BNFGrammar");
			String s = KXmlUtils.getTextValue(e);
			BNFGrammar g = BNFGrammar.compile(null, s, null);
//			BNFGrammar grammar = BNFGrammar.compile(null,
//				new File(getDataDir() + "TestXdScript.bnf"), null);
			s = g.toString();
			g = BNFGrammar.compile(null, s, null);
			
/*labels not implemented yet*
			s = "{loop : while ( true ) continue loop ;}";
			assertEq(s, parse(grammar, "Block", s));
			s = "{loop : while ( true ) break loop ;}";
			assertEq(s, parse(grammar, "Block", s));
/*labels not implemented yet*/

/*errors*/
			s = "-a + -0d3 - 0xFE * 1e-2 % 1.2 / 0.5E3 + 'x*y'";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "(aa | !ab AND NOT bc OR !!! (cd AND de))";
			assertEq(s, parse(g, "Expression", s));
//			printCode(g);
			s = "((String)i).substring(1)";
			assertEq(s, parse(g, "Expression", s));
			s = "( x() == 'B' )";
			assertEq(s, parse(g, "Expression", s));
			s = "void x(int a) {out();}";
			assertEq(s, parse(g, "MethodDeclaration", s));
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
			s = "{ i ++ ; ++ k ; j += 2;}";
			assertEq(s, parse(g, "Block", s));
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
			s = "{{for ( int i= 0; i < 2; i++) return 1;}}";
			assertEq(s, parse(g, "Block", s));
			s = "{  for (int i = 0; i LT b.size(); i++) {\n"+
				"    b.setAt(i,i);\n"+
				"  } }";
			assertEq(s, parse(g, "Block", s));
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
			s = "create {return (getElementName() == 'B') ? null : null;}";
			assertEq(s, parse(g, "AttributeScript", s));
			s = "*; onAbsence{i = 1; j = - i--;} finally outln()";
			assertEq(s, parse(g, "ElementScript", s));
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
			s = "match @x==''; options acceptEmptyAttributes";
			assertEq(s, parse(g, "ElementScript", s));
			s = "  String t = ((String) 1.5 ) . subtring(1);";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = "X#Y/@Z";
			assertEq(s, parse(g, "XPosition", s));
			s = "X#Y/$mixed[1]/A[22]/@Z";
			assertEq(s, parse(g, "XPosition", s));
			s = "X#Y/$mixed[1]/A[22]/$text[1]";
			assertEq(s, parse(g, "XPosition", s));
			s = 
"type XY enum('XX','YY'); type flt float();\n" +
"type dat xdatetime('yyyy-MM-dd');uniqueSet uflt flt; uniqueSet udat dat;";
			assertEq(s, parse(g, "DeclarationScript", s));
			s = 
"  %class test.xdef.component.C2 %link C#Town/Street/House;\n" +
"  %interface test.xdef.component.CI %link C#Person;\n" +
"  %ref test.xdef.TestXComponents_W.W %link A#A/W;\n" +
"  %ref test.xdef.TestXComponents_B %link B#A;\n";
			assertEq(s, parse(g, "XCComponent", s));
//			printCode(g);
			s = "void test.xdef.TestXComponents_C.test(XXData)";
			assertEq(s, parse(g, "MethodListItem", s));
//			printCode(g);
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
