package bugreports;

import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import java.util.Properties;

/** Example of using method parseType. */
public class ParseTypeExample {

	public static void main(String... args) {
		String xdef1 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='A'>\n"+
"  /* This declaration contains types to be checked. */\n"+
"  <xd:declaration scope='local'>\n"+
"    type t1 int();\n"+
"    type t2 starts(%argument='wsdl:');\n"+
"    uniqueSet u{t:t1; s:t2};\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef2 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='B'>\n"+
"  <xd:declaration scope='local'>\n"+
"    BNFGrammar g = new BNFGrammar('\n"+
"      x ::= S? [0-9]+\n"+
"      y ::= S? [a-zA-Z]+\n"+
"      S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"      z ::= x | y\n"+
"    ');\n"+
"    type t3 g.rule('z');\n"+
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef3 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='C'>\n"+
"  <xd:declaration scope='local'>\n"+
"    type t4 tt();\n"+
"    boolean tt() {\n"+
"      return getText().startsWith('a');\n"+
"    }\n"+ // 10
"  </xd:declaration>\n"+
"</xd:def>";
		String xdef4 =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/3.2' name='X'\n"+
"        importLocal='A, B, C'>\n"+
"</xd:def>";
		Properties props = new Properties();
		XDPool xp = XDFactory.compileXD(props, xdef1, xdef2, xdef3, xdef4);
		String xdefName = "X";
		XDDocument xd = xp.createXDDocument(xdefName);

		XDParseResult pr;

		String typeName = "t1";
		pr = xd.parseXDType(typeName, "123");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "123s");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}

		typeName = "t2";
		pr = xd.parseXDType(typeName, "wsdl:1");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "xsdl:1");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}

		typeName = "t3";
		pr = xd.parseXDType(typeName, "abc");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "xy12");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}

		typeName = "t4";
		pr = xd.parseXDType(typeName, "a1 2x");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "1a 2x");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		////////////////////////////////////////////////////////////////////////
		typeName = "u.t";
		pr = xd.parseXDType(typeName, "123");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "123x");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}

		typeName = "u.s";
		pr = xd.parseXDType(typeName, "wsdl:x");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
		pr = xd.parseXDType(typeName, "wdl:x");
		if (pr.errors()) {
			System.out.println(typeName + ": " + pr.getReporter());
		} else {
			System.out.println(typeName + " OK");
		}
	}
}