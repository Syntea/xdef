package test.xdef;

import test.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDPool;
import org.xdef.impl.util.gencollection.XDGenCollection;
import org.w3c.dom.Element;

/** Test of X-definitions by X-definition.
 * @author Vaclav Trojan
 */
public final class TestXdefOfXdef extends XDTester {

	private final XDPool _xp;

	public TestXdefOfXdef() {
		super();
		setChkSyntax(false); // here it MUST be false!
		_xp = compile("classpath://org.xdef.impl.compile.XdefOfXdef*.xdef");
	}

	final public ArrayReporter parse(final String xml) {
		ArrayReporter reporter = new ArrayReporter();
		_xp.createXDDocument().xparse(xml, reporter);
		return reporter;
	}

	private static String genCollection(final String... sources) {
		try {
			Element el = XDGenCollection.genCollection(sources,
				true, //resolvemacros
				false, // do not removeActions
				false);
			return KXmlUtils.nodeToString(el, true);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "";
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		String xml;
		final String dataDir = getDataDir() + "test/";
		try { //check xdefinition of xdefinitions
			xml = genCollection(new String[] {
"<xd:declaration xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:macro name='a'>'aaa'</xd:macro>\n"+
"  String s = ${a};\n"+
"</xd:declaration>",
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script=\"finally outln(${a})\"/>\n"+
"</xd:def>"});
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' name = 'a' root = 'foo'"+
"   xd:include = \"" + dataDir +"TestInclude_1.xdef\">\n"+
"  <foo xd:script = \"finally out('f')\">\n"+
"    <bar xd:script = '*; ref b#bar'/>\n"+ // b is xdefinition from include
"  </foo>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root=\"A\">\n" +
"  <A b='onStartElement out(@b)'/>\n" +
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
" <a xd:script = \"\n" +
"    var { int i = 1;\n" +
"          uniqueSet id1 {t: string()};\n" +
"          type cislo int();\n" +
"          uniqueSet id2 {t: cislo;}\n" +
"          type datum xdatetime('d. M. yyyy[ HH:mm[:ss]]');\n" +
"          uniqueSet id3 {t: xdatetime('yyyyMMddHHmmss')}\n" +
"        }\n" +
"    finally { id1.CLEAR();\n" +
"              for (int i = 0; i LT b.size(); i++) b.setAt(i,i);\n" +
"            }\" >\n" +
"  <b a = \"optional id1.t.IDREF()\"/>\n" +
"  <c xd:script = \"occurs 1..; finally id2.CLEAR()\"\n" +
"     stamp = \"required id3.t.ID()\" >\n" +
"     <d xd:script = \"occurs 1..\"\n" +
"        a1 = \"required id1.t.ID()\" a2 = \"optional id2.t.ID()\"/>\n" +
"     <e a3 = \"required id2.t.IDREF()\"/>\n" +
"  </c>\n" +
"  <f a4 = \"required id1.t.IDREF()\" a5 = \"optional id3.t.IDREF()\"/>\n" +
" </a>\n" +
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root = \"#A\">\n"+
"  <A><B xd:script='occurs 2 /*intentionaly no parse method*/'/></A>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>uniqueSet id1 {t: string(); s: int;};\n"+
"  uniqueSet id2 string\n"+
"</xd:declaration>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n" +
"<xd:declaration>uniqueSet id1 {t: string(); s: int;};\n"+
"  boolean x() {\n"+
"     int i=1;\n" +
"     switch(i) {\n" +
"       case 1: i=2;\n" +
"       default: return true;\n" +
"     }\n" +
"     return true;\n" +
"  }\n" +
"</xd:declaration>\n"+
"<a b=\"optional x(); default 'abc'; finally outln();\"/>\n" +
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml =
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:declaration> String t = ((String)1.5).substring(1);</xd:declaration>\n"+
"  <a xd:script=\"*; create getElementName()=='B' ? null : null;\"/>\n"+
"</xd:def>";
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'>\n"+
"  <a xd:script=\"match @x=='';options acceptEmptyAttributes\" x=''/>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def root='a'><a a='myType'/></xd:def>\n"+
"<xd:def xd:name='a'>\n"+
"  <xd:declaration>type myType $rrr.check('intList');</xd:declaration>\n"+
"  <xd:BNFGrammar name='$base'>\n"+
"    integer  ::= [0-9]+\n"+
"    S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"  </xd:BNFGrammar>\n"+
"  <xd:BNFGrammar name='$rrr' extends='$base'>\n"+
"    intList  ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"  </xd:BNFGrammar>\n"+
"</xd:def>\n"+
"</xd:collection>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));

			xml = genCollection(new String[] {
"<xd:def xmlns:xd='" + _xdNS + "' root='a'><a a='myType'/></xd:def>",
"<xd:BNFGrammar xmlns:xd='" + _xdNS + "' name='$base'>\n"+
"    integer  ::= [0-9]+\n"+
"    S ::= [#9#10#13 ]+ /*skipped white spaces*/\n"+
"    name ::= [A-Z] [a-z]+\n"+
"</xd:BNFGrammar>",
"<xd:BNFGrammar xmlns:xd='" + _xdNS + "' name='$rrr' extends='$base'>\n"+
"    intList  ::= integer (S? \",\" S? integer)*\n"+
"    fullName ::= name S ([A-Z] \".\")? S name\n"+
"</xd:BNFGrammar>",
"<xd:def xmlns:xd='" + _xdNS + "' xd:name='a'>\n"+
"  <xd:declaration>type myType $rrr.check('intList');</xd:declaration>\n"+
"</xd:def>"});
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));

			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' name='XDDecl'>  \n"+
"  <xd:BNFGrammar name=\"xscript\"><![CDATA[L::='a'/*E*/]]></xd:BNFGrammar>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "'>\n"+
"<xd:declaration>\n"+
" external method boolean a.b.a(int);\n"+
" type an a(2)\n"+ // here is intentionally missing semicolon
"</xd:declaration>\n"+
"<A a='required an();'/>\n"+
"</xd:def>");
			assertNoErrorwarnings(parse(xml), xml);
			assertNoErrorwarnings(parse(xml), genCollection(xml));
			if (getFulltestMode()) {xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root ='a'>\n"+
"  <a a=\"fixed {return 'abc';}\" />\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), xml);
				assertNoErrorwarnings(parse(xml), genCollection(xml));
				xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <xd:declaration>\n"+
"    external method boolean test.xdef.TestTypes.kp(XXNode, XDValue[]);"+
"  </xd:declaration>\n"+
"  <a a='kp(1,5,%totalDigits=1,%enumeration=1,%pattern=\"\\\\d\")'/>\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), xml);
				assertNoErrorwarnings(parse(xml), genCollection(xml));
				xml = genCollection(
"<xd:def xmlns:xd='" + _xdNS + "' root='a | b | m/n | m/o | x'>\n"+
"   <xd:any xd:name='x' b='int()' />\n"+
"   <xd:mixed xd:name='m'> <n/> <o/> </xd:mixed>\n"+
"   <a> <xd:mixed xd:script='ref m' /> </a>\n"+
"   <b><xd:any xd:script='ref x' b='int()' /></b>\n"+
"</xd:def>");
				assertNoErrorwarnings(parse(xml), genCollection(xml));

//				// In this X-definition is <xd:def xmlns:xd = "METAXDef" ...
//				xml = "classpath:"
//					+ "//org.xdef.impl.compile.XdefOfXdefBase.xdef";
//				assertNoErrorwarnings(parse(xml), xml);
			}
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}