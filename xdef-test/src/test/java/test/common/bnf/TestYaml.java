package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import java.io.File;
import buildtools.STester;

/** Test of BNF.
 * @author Vaclav Trojan
 */
public class TestYaml extends STester {

	public TestYaml() {super();}

	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			grammar.setUserObject(this);
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
/*
	yaml.org/spec/1.2/spec.html#YAML 1.1 processing//
	https://learnxinyminutes.com/docs/yaml/
*/
	@Override
	public void test() {
		String s;
		BNFGrammar g;
		try {
			g = BNFGrammar.compile(new File(getDataDir() + "TestYaml.bnf"));
//System.out.println(g);
			s = "%TAG !e! !e!foo \"bar\"\n";
//			g.trace(System.err);
			assertEq(s, parse(g, "l_directive", s));
//if(true) return;
			s =
"%TAG !e! tag:example.com,2000:app/\n"+
"# This stream contains no\n" +
"# documents, only comments.\n";
//			g.trace(System.err);
			assertEq(s, parse(g, "l_directive", s));
//if(true) return;
			s =
"%TAG !e! tag:example.com,2000:app/\n" +
"---\n" +
"!e!foo \"bar\"\n";
//			g.trace(System.err);
//			assertEq(s, parse(g, "l_yaml_stream", s));
//if(true) return;
			s =
"%YAML 1.2\n" +
"---\n" +
"!!map {\n" +
"  ? !!str \"sequence\"\n" +
"  : !!seq [ !!str \"one\", !!str \"two\" ],\n" +
"  ? !!str \"mapping\"\n" +
"  : !!map {\n" +
"    ? !!str \"sky\" : !!str \"blue\",\n" +
"    ? !!str \"sea\" : !!str \"green\",\n" +
"  },\n" +
"}\n";
//			g.trace(System.err);
//			assertEq(s, parse(g, "l_yaml_stream", s));
//if(true) return;
			s =
"%YAML 1.2\n" +
"---\n" +
"!<tag:example.com,2000:app/int> \"1 - 3\"\n";
//			g.trace(System.err);
//			assertEq(s, parse(g, "l_yaml_stream", s));
//if(true) return;
			s =
"%YAML 1.2\n" +
"---\n" +
"!!str \"Document\"";
//			g.trace(System.err);
//			assertEq(s, parse(g, "l_yaml_stream", s));
//if(true) return;
			s =
"Block style: !!seq\n" +
"- Clark Evans\n" +
"- Ingy döt Net\n" +
"- Oren Ben-Kiki";
//			g.trace(System.err);
//			assertEq(s, parse(g, "l_yaml_stream", s));
//if(true) return;
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}