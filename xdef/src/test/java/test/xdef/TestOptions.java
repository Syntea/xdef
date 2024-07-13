package test.xdef;

import java.io.StringWriter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test of text options and text values.
 * @author Vaclav Trojan
 */
public final class TestOptions extends XDTester {

	public TestOptions() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String s, xdef, xml;
		ArrayReporter reporter = new ArrayReporter();
		Element el;
		StringWriter swr;
		try {
			// trimAttr, trimText
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" xd:script='options trimAttr, trimText'>\n"+
"  <a a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\" a \"> a </a>";
			assertEq("<a a1=\"a\">a</a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a1=\" a\t  \n b \"> a\t  \n b </a>";
			assertEq("<a a1=\"a     b\">a\t  \n b</a>",
				parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			// ignoreTextWhiteSpaces
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" xd:script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces'>\n"+
"  <a a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\" a \"> a </a>";
			assertEq("<a a1=\"a\">a</a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<a a1=\" a\t  \n b \"> a\t  \n b </a>";
			assertEq("<a a1=\"a b\">a b</a>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			// setAttrLowerCase, setTextLowerCase
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" xd:script='options setAttrLowerCase, setTextLowerCase'>\n"+
"  <a a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\"a\" a2=\"A\">A</a>";
			assertEq("<a a1=\"a\" a2=\"a\">a</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
" xd:script='options setAttrUpperCase, setTextUpperCase'>\n"+
"  <a a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\"a\" a2=\"A\">a</a>";
			assertEq("<a a1=\"A\" a2=\"A\">A</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='options setAttrLowerCase, setTextLowerCase'\n"+
"     a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\"a\" a2=\"A\">A</a>";
			assertEq("<a a1=\"a\" a2=\"a\">a</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='options setAttrUpperCase, setTextUpperCase'\n"+
"     a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\"a\" a2=\"A\">a</a>";
			assertEq("<a a1=\"A\" a2=\"A\">A</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='options setTextLowerCase'\n"+
"     a1 = 'optional string(0,100); options setAttrLowerCase'\n"+
"     a2 = 'optional string(0,100); options setAttrLowerCase'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1=\"a\" a2=\"A\">A</a>";
			assertEq("<a a1=\"a\" a2=\"a\">a</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='options setTextUpperCase'\n"+
"     a1 = 'optional string(0,100); options setAttrUpperCase'\n"+
"     a2 = 'optional string(0,100); options setAttrUpperCase'>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1='a' a2='A'>a</a>";
			assertEq("<a a1='A' a2='A'>A</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a1 = 'optional string(0,100); options setAttrLowerCase'\n"+
"     a2 = 'optional string(0,100); options setAttrLowerCase'>\n"+
"    optional string(0,100); options setTextLowerCase\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1='a' a2='A'>A</a>";
			assertEq("<a a1='a' a2='a'>a</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a a1 = 'optional string(0,100); options setAttrUpperCase'\n"+
"     a2 = 'optional string(0,100); options setAttrUpperCase'>\n"+
"    optional string(0,100); options setTextUpperCase\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1='a' a2='A'>a</a>";
			assertEq("<a a1='A' a2='A'>A</a>", parse(xp, "", xml,reporter));
			assertNoErrorwarnings(reporter);
			xdef = //text, texcontent attributes
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:text=\"1..3 string;" +
"    onTrue out('T:'+getText());finally out('f:'+getText());\">\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>t1<b/>t2<c/>t3</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq("T:t1f:t1T:t2f:t2T:t3f:t3", swr.toString());
			swr = new StringWriter();
			assertEq(xml, create(xp,"", "a", reporter, xml, swr, null));
			assertEq("T:t1f:t1T:t2f:t2T:t3f:t3", swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:textcontent=\"string();\n"+
"    onTrue out('T:'+getText()); finally out('f:'+getText());\">\n"+
"    <b/>\n"+
"    <c/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>t1<b/>t2<c/>t3</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertNoErrorwarnings(reporter);
			assertEq("T:t1t2t3f:t1t2t3", swr.toString());
			swr = new StringWriter();
			assertEq("<a><b/><c/>t1t2t3</a>",
				create(xp,"", "a", reporter, xml, swr, null));
			assertEq("T:t1t2t3f:t1t2t3", swr.toString());
			xdef = //test xd:textcontent attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:textcontent='required float; finally out(getText())'>\n"+
"  <b/>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>1.2<b/>345E-23</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("1.2345E-23", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a>1.23<b/>34.56</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("1.2334.56", swr.toString());
			assertTrue(reporter.errorWarnings());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:textcontent=\"required string; finally out(getText());create 'xyz'\">\n"+
"  <b/>\n"+
"</a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>aaa<b/></a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("aaa", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a><b/>bbb</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("bbb", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a>aaa<b/>bbb</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("aaabbb", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a><b/></a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("", swr.toString());
			assertTrue(reporter.errorWarnings());
			swr = new StringWriter();
			assertEq("<a><b/>xyz</a>",
				create(xp, "", "a", reporter, null, swr, null)); //???
			assertEq("xyz", swr.toString()); //???
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root=\"a\">\n"+
"<a xd:text=\"+string; finally out(getText()); create 'xyz';\">\n"+
"  <b/>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a>aaa<b/></a>";
			xp = compile(xdef);
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("aaa", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a><b/>bbb</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("bbb", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a>aaa<b/>bbb</a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("aaabbb", swr.toString());
			assertNoErrorwarnings(reporter);
			xml = "<a><b/></a>";
			swr = new StringWriter();
			assertEq(xml, parse(xp, "", xml, reporter, swr, null, null));
			assertEq("", swr.toString());
			assertTrue(reporter.errorWarnings());
			swr = new StringWriter();
			assertEq("<a>xyz<b/>xyz</a>",
				create(xp, "", "a", reporter, null, swr, null));
			assertEq("xyzxyz", swr.toString());
			assertNoErrorwarnings(reporter);
			xdef = //test xd:text attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:text=\"* string; create 'xyz'\">\n"+
"    optional string(0,100)\n"+
"	<b/>\n"+
"    optional string(0,100)\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>ab<b/>cd</a>";
			parse(xp, "", xml, reporter);
			assertEq("<a>xyz<b/>xyz</a>",
				create(xp, "", "a", reporter, "<a b='bbb'/>"));
			assertNoErrorwarnings(reporter);
			xdef = // option igenoreOther; forget
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.0' name='rdf' root='rdf:RDF'\n"+
" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
" xmlns:dcterms=\"http://purl.org/dc/terms/\">\n"+
"<xd:declaration> String a; boolean b; </xd:declaration>\n" +
"<rdf:RDF>\n" +
"  <rdf:Description xd:script=\"occurs +;options ignoreOther;\n" +
"    init {a='';b=false;} finally {if (b EQ true) outln(a);} forget;\">\n" +
"    <dcterms:title xd:script=\"occurs +\">\n" +
"      onTrue a = getText() + ': ';\n" +
"    </dcterms:title>\n" +
"    <dcterms:abstract>\n" +
"      onTrue {a= a + getText();b=true;}\n" +
"    </dcterms:abstract>\n" +
"  </rdf:Description>\n" +
"</rdf:RDF>\n" +
"</xd:def>";
			xp = compile(xdef);
			xml =
"<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"+
" xmlns:dcterms=\"http://purl.org/dc/terms/\">\n" +
"  <rdf:Description>\n" +
"    <dcterms:title>b c d</dcterms:title>\n" +
"      all this is ignored: <A><B/></A>somethig<C b='b'/>else\n" +
"    <dcterms:abstract>Abstract</dcterms:abstract>\n" +
"  </rdf:Description>\n" +
"</rdf:RDF>";
			swr = new StringWriter();
			el = parse(xp, "rdf", xml, reporter, swr, null, null);
			assertNoErrorwarnings(reporter);
			assertEq("rdf:RDF", el.getTagName());
			assertEq(2, el.getAttributes().getLength());//just xmlns attributes
			assertEq(0, el.getChildNodes().getLength());//all children forgotten
			assertEq("b c d: Abstract\n", swr.toString());
			xdef = //option acceptQualifiedAttr
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:a='a' root='a:a'>\n"+
"  <a:a xmlns:a='a'\n"+
"    a='required int(); options acceptQualifiedAttr'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<b:a xmlns:b='a' b:a='123' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<b:a xmlns:b='a' a='123' b:a='456' />";
			parse(xp, "", xml, reporter);
			if (!reporter.errorWarnings()) {
				fail("Error not reported");
			} else if (!"XDEF559".equals(reporter.getReport().getMsgID())) {
				fail(reporter.printToString());
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:a='a' root='a:a'>\n"+
"  <a:a xmlns:a='a'\n"+
"    xd:attr='+ int(); options acceptQualifiedAttr'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<b:a xmlns:b='a' b:a='123' />";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<b:a xmlns:b='a' c='123'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<b:a xmlns:b='a' c='123' d='456'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xdef = // xd:text attribute
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:text=\"* string; create 'xyz'\">\n"+
"	<b/>\n"+
"  </a>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a>ab<b/>cd</a>";
			parse(xp, "", xml, reporter);
			assertEq("<a>xyz<b/>xyz</a>",
				create(xp, "", "a", reporter, "<a b='bbb'/>"));
			assertNoErrorwarnings(reporter);
			xdef = // test xd:attr attribute
"<xd:def xmlns:xd='" + _xdNS + "' root = 'a'>\n"+
"  <a xd:attr=\"occurs 1..2 int();\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF531"),
				reporter.toString());
			assertErrors(reporter); // should be an error!
			xml = "<a x='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter); // OK
			xml = "<a x='1' y='1' z='1'/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertTrue(reporter.toString().contains("XDEF532"),
				reporter.toString());
			xdef = //option acceptQualifiedAttr; xd:attr and match.
"<xd:def xmlns:xd='" + _xdNS + "' xmlns:a='a' root='a:a'>\n"+
"  <a:a xmlns:a='a'\n"+
"    xd:attr='match @a:a; + int(); options acceptQualifiedAttr' />\n"+
"</xd:def>";
			xp = compile(xdef);
			reporter = new ArrayReporter();
			xml = "<b:a xmlns:b='a' b:a='123'/>";
			assertEq("<b:a xmlns:b='a' b:a='123'/>",
				parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xml = "<b:a xmlns:b='a' a='123' />";
			parse(xp, "", xml, reporter);
			if (reporter.errors()) {
				if (!"XDEF525".equals(reporter.getReport().getMsgID())) {
					fail(reporter.printToString());
				}
			}
			xdef = //test option ignoreEmptyAttributes
"<xd:def xmlns:xd='" + _xdNS + "' root='a'\n"+
"   xd:script='options ignoreEmptyAttributes'>\n"+
"\n"+
"  <a a='optional string(1,8)'>\n"+
"    <b b='optional string(1,8)'/>\n"+
"  </a>\n"+
"</xd:def>";
			xml = "<a><b b='1'/></a>";
			if (test(xdef, xml, "",'P', xml, "")) {
				fail();
			}
			xml = "<a><b b=''/></a>";
			if (test(xdef, xml, "",'P', "<a><b/></a>", "")) {
				fail();
			}
			xdef = //trimAttr, trimText
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a a='string; options noTrimAttr' b='string'>\n"+
"  <b xd:script='2; options noTrimText'>string;</b>\n"+
"  <c>string; options noTrimText</c>\n"+
"</a>\n"+
"</xd:def>";
			xml = "<a a=' Y ' b=' Z '><b>1</b><b> 2 </b><c> 3 </c></a>";
			el = parse(xdef, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a a=' Y ' b='Z'><b>1</b><b> 2 </b><c> 3 </c></a>");
			assertEq(" Y ", el.getAttribute("a"));
			assertEq("Z", el.getAttribute("b"));
			assertEq(" 2 ",
				((Element) el.getChildNodes().item(1)).getTextContent());
			assertEq(" 3 ",
				((Element) el.getChildNodes().item(2)).getTextContent());
			xml =
			"<x a=' Y ' b=' Z '><b c='C'>1</b><b> 2 </b><b>3</b><c> 3 </c></x>";
			el = create(xdef, "",  "a", reporter, xml);
			assertNoErrorwarnings(reporter);
			assertEq(el, "<a a=' Y ' b='Z'><b>1</b><b> 2 </b><c> 3 </c></a>");
			assertEq(" Y ", el.getAttribute("a"));
			assertEq("Z", el.getAttribute("b"));
			assertEq(" 2 ",
				((Element) el.getChildNodes().item(1)).getTextContent());
			assertEq(" 3 ",
				((Element) el.getChildNodes().item(2)).getTextContent());
			xdef = //test options, match for attribute
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:def name='a' root='x#a' xd:script='options setAttrLowerCase'/>\n"+
"  <xd:def name='x' xd:script='options setAttrUpperCase'>\n"+
"    <a a1='optional string(0,100)' a2='optional string(0,100)'/>\n"+
"  </xd:def>\n"+
"</xd:collection>";
			xml = "<a a1='a' a2='A'/>";
			xp = compile(xdef);
			assertEq("<a a1='A' a2='A'/>", parse(xp, "a", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"  <xd:def name='a' root='x#a' xd:script='options setAttrUpperCase'/>\n"+
"  <xd:def name='x' xd:script='options ignoreEmptyAttributes'>\n"+
"    <a a1 = 'optional string(0,100)' a2 = 'optional string(0,100)'/>\n"+
"  </xd:def>\n"+
"</xd:collection>";
			xml = "<a a1='A' a2=''/>";
			xp = compile(xdef);
			assertEq("<a a1='A'/>", parse(xp, "a", xml, reporter));
			assertNoErrorwarnings(reporter);
//ignore/preserve/accept empty attributes
			xdef = //optional ignore
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'\n"+
"  script='options ignoreEmptyAttributes'>\n"+
"  <a a='optional string;'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertFalse(el.hasAttribute("a"));
			xml = "<a a='x'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=''/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertFalse(el.hasAttribute("a"));
			xdef = //optional preserve
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'\n"+
"  script='options preserveEmptyAttributes'>\n"+
"  <a a='optional string;'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertFalse(el.hasAttribute("a"));
			xml = "<a a='x'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=''/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("a"));
			xdef = //optional accept
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'\n"+
"  script='options acceptEmptyAttributes'>\n"+
"  <a a='optional string;'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertFalse(el.hasAttribute("a"));
			xml = "<a a='x'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=''/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("a"));
			xdef = //required accept
"<xd:def xmlns:xd='" + _xdNS + "' xd:root='a'"+
"  script='options acceptEmptyAttributes'>\n"+
"  <a a='string;'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a/>";
			el = parse(xp, "", xml, reporter);
			assertTrue(reporter.errors(), "not reported");
			assertFalse(el.hasAttribute("a"));
			xml = "<a a='x'/>";
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			xml = "<a a=''/>";
			el = parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.hasAttribute("a"));
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
" <xd:def name='a' root='x#a' xd:script='options setAttrUpperCase'/>\n"+
" <xd:def name='x' xd:script='options ignoreEmptyAttributes'>\n"+
"  <a a1='optional string(0,100); match false' a2='optional string(0,100)'/>\n"+
" </xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<a a1='a' a2=''/>";
			assertEq("<a/>", parse(xp, "a", xml, reporter));
			assertTrue(reporter.errorWarnings(), "Not reported: not allowed");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
" root='a' xd:script='options acceptEmptyAttributes'>\n"+
"  <a a1='optional string(0,100); match false' a2='optional string(0,100)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1='a' a2=''/>";
			assertEq("<a a2=''/>", parse(xp, "", xml, reporter));
			assertTrue(reporter.errorWarnings(), "Not reported: not allowed");
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
" root='a' xd:script='options acceptEmptyAttributes'>\n"+
"  <a a1='optional string(0,100); match true' a2='optional string(0,100)'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<a a1='a' a2=''/>";
			assertEq(xml, parse(xp, "", xml, reporter));
			assertFalse(reporter.errorWarnings(), "reported warning");
			assertNoErrorwarnings(reporter);
			xdef = //uppercase, lowercase
"<xd:def xmlns:xd='" + _xdNS + "' root='A'\n"+
"xd:script='options setTextUpperCase,setAttrUpperCase,trimText,trimAttr'>\n"+
"  <A a='string' xd:script=''>\n"+
"    string();\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A a='ab'>cd</A>";
			assertEq("<A a='AB'>CD</A>", parse(xp, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'\n"+
"xd:script='options setTextLowerCase,setAttrLowerCase,trimText,trimAttr'>\n"+
"  <A a='string' xd:script=''>\n"+
"    string()\n"+
"  </A>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml = "<A a='AB'>CD</A>";
			assertEq("<A a='ab'>cd</A>", parse(xp, "", xml, reporter));
			xdef = //check empty attributes
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"match @x==''; options ignoreEmptyAttributes\"/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a/>", parse(xp, "", "<a x=''/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='match @x; options ignoreEmptyAttributes'/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a/>", parse(xp, "", "<a x=''/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd = '" + _xdNS + "' root='a'>\n"+
"  <a xd:script=\"match @x==''; options acceptEmptyAttributes\" x=''/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a x=''/>", parse(xp, "", "<a x=''/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='match @x; options acceptEmptyAttributes' x=''/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a x=''/>", parse(xp, "", "<a x=''/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='match @x==null; options acceptEmptyAttributes' x=''/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a/>", parse(xp, "", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='match !@x; options acceptEmptyAttributes' x=''/>\n"+
"</xd:def>";
			xp = compile(xdef);
			assertEq("<a/>", parse(xp, "", "<a/>", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //test options in xdef header and in references
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='A' root='A'\n"+
"  xd:script='options setAttrUpperCase, setTextUpperCase'>\n"+
"  <A v=\"fixed '  1a  B '\"\n"+
"     a='required' b='required' c='required' d='required'>\n"+
"    required\n"+
"  </A>\n"+
"</xd:def>\n"+
"<xd:def xd:name='B' root='A'\n"+
"  xd:script='options setAttrLowerCase, setTextLowerCase'>\n"+
"  <A xd:script='ref A#A'/>\n"+
"</xd:def>\n"+
"<xd:def xd:script='options setAttrLowerCase, setTextUpperCase' root='B#A'\n"+
" name='C'/>\n"+
"<xd:def name='D' root='A'>\n"+
"  <A xd:script='options setAttrLowerCase, setTextLowerCase; ref A#A' />\n"+
"</xd:def>\n"+
"<xd:def name='E' root='A'>\n"+
"  <A xd:script='options noTrimAttr, noTrimText; ref A#A' />\n"+
"</xd:def>\n"+
"<xd:def name='F' root='A' xd:script='options noTrimAttr, noTrimText'>\n"+
"  <A xd:script='ref A#A' />\n"+
"</xd:def>\n"+
"<xd:def name='G' root='A'>\n"+
"  <A xd:script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces;\n"+
"     ref A#A'/>\n"+
"</xd:def>\n"+
"<xd:def name='H' root='A'\n"+
"    xd:script='options ignoreAttrWhiteSpaces, ignoreTextWhiteSpaces'>\n"+
"  <A xd:script='ref A#A'/>\n"+
"</xd:def>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='Ab' b='1Ab' c='  ab ' d=' A  b'> A  b </A>";
			assertEq("<A a='ab' b='1ab' c='ab' d='a  b' v='  1a  B '>a  b</A>",
				parse(xp, "B", xml, reporter)); //LowerCase
			assertNoErrorwarnings(reporter);
			assertEq("<A a='ab' b='1ab' c='ab' d='a  b' v='  1a  B '>a  b</A>",
				parse(xp, "C", xml, reporter)); //LowerCase
			assertNoErrorwarnings(reporter);
			assertEq("<A a='ab' b='1ab' c='ab' d='a  b' v='  1a  B '>a  b</A>",
				parse(xp, "D", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<A a='AB' b='1AB' c='  AB ' d=' A  B' v='  1a  B '>"+
				" A  B </A>", parse(xp, "E", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq("<A a='AB' b='1AB' c='  AB ' d=' A  B' v='  1a  B '>"+
				" A  B </A>", parse(xp, "F", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "G", xml, reporter),
				"<A a='AB' b='1AB' c='AB' d='A B' v='  1a  B '>A B</A>");
			assertNoErrorwarnings(reporter);
			assertEq( parse(xp, "H", xml, reporter),
				"<A a='AB' b='1AB' c='AB' d='A B' v='  1a  B '>A B</A>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='Log'>\n"+
"   <Log xd:script='init;'\n"+
"      Verze='required' Misto='required' Code='required'/>\n"+
"</xd:def>\n"+
"<xd:def name='DN'>\n"+
"  <Log xd:script='ref Log#Log'/>\n"+
"</xd:def>\n"+
"<xd:def name='P_LOG' root='DN#Log'/>\n"+
"<xd:def name='Q_LOG' xd:script='options setAttrLowerCase' root='DN#Log'/>\n"+
"<xd:def name='R_LOG' xd:script='options setAttrUpperCase' root='DN#Log'/>\n"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<Log Verze='2.0' Misto='Praha' Code='1a'/>";
			assertEq(xml, parse(xp, "P_LOG", xml, reporter));
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "Q_LOG", xml, reporter),
				"<Log Verze='2.0' Misto='praha' Code='1a'/>");
			assertNoErrorwarnings(reporter);
			assertEq(parse(xp, "R_LOG", xml, reporter),
				"<Log Verze='2.0' Misto='PRAHA' Code='1A'/>");
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			////////////////////////////////////////////////////////////////////
			////////// the options inheritance of elements focused on //////////
			////////// attributes is tested                           //////////
			////////////////////////////////////////////////////////////////////

			// attribut "c" inherits options from the refered element "A"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'\n"+
"  script='options ignoreEmptyAttributes, noTrimAttr'>\n"+
"  <A xd:script='ref NB#B; options preserveEmptyAttributes'\n"+
"    a='string(2); options acceptEmptyAttributes'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options trimAttr,ignoreEmptyAttributes'>\n"+
"  <B xd:script='ref C; options ignoreEmptyAttributes'\n"+
"    c='optional string(2)'/>\n"+
"	 <C xd:script='options ignoreEmptyAttributes'\n"+
"      d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a='' c='' d=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the parent element "B"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script=''>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'\n"+
"  script='options trimAttr, ignoreEmptyAttributes'>\n"+
"  <B xd:script='ref C; options preserveEmptyAttributes'\n"+
"    c='optional string(2)'/>\n"+
"  <C xd:script='options ignoreEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a='' c='' d=''></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" has default options
			// attribute "d" has default options
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script=''>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options trimAttr'>\n"+
"  <B xd:script='ref C' c='optional string(2)'/>\n"+
"  <C xd:script='' d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a='' c='' d=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "c" has default options
			// attribute "d" has default options
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options trimAttr'>\n"+
"  <B xd:script='ref C' c='optional string(2)'/>\n"+
"  <C d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a='' c='' d=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script=''>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options trimAttr'>\n"+
"  <B xd:script='ref C' c='optional string(2)'/>\n"+
"  <C xd:script='options ignoreEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the refered element "A"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options acceptEmptyAttributes'>\n"+
"  <A xd:script='ref NB#B; options ignoreEmptyAttributes'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options acceptEmptyAttributes'>\n"+
"	<B xd:script='ref C; options acceptEmptyAttributes'\n"+
"     c='optional string(2)'/>\n"+
"	<C xd:script='options acceptEmptyAttributes' d='string(2);'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the NA X-Def header
			// attribute "d" inherits options from the NA X-Def header
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options ignoreEmptyAttributes'>\n"+
"  <A xd:script='ref NB#B;'\n"+
"    a='string(2); options acceptEmptyAttributes, noTrimAttr'\n"+
"    b='string(2); options ignoreEmptyAttributes, trimAttr'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' xd:script='options acceptEmptyAttributes'>\n"+
"  <B xd:script='ref C; options acceptEmptyAttributes'\n"+
"    c='optional string(2)'/>\n"+
"  <C xd:script='options acceptEmptyAttributes' d='string(2);'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A a=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script=''>\n"+
"  <A xd:script=\"ref NB#B\"\n"+
"    a='string(2); options noTrimAttr, ignoreEmptyAttributes'\n"+
"    b='string(2)'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script=''>\n"+
"  <B xd:script='ref C' c='optional string(2); options trimAttr'/>\n"+
"  <C xd:script='options acceptEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A b='' c='' d=''></A>", parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options noTrimAttr, ignoreEmptyAttributes'\n"+
"    b='string(2)'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B xd:script='ref C' c='optional string(2); options trimAttr'/>\n"+
"  <C xd:script='options acceptEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A b='' c='' d=''></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the NB X-Def header
			// attribute "c" inherits options from the NB X-Def header
			// attribute "d" inherits options from the NB X-Def header
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' xd:root='A'>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options noTrimAttr, ignoreEmptyAttributes'\n"+
"    b='string(2)'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options ignoreEmptyAttributes'>\n"+
"  <B xd:script='ref C' c='optional string(2); options trimAttr'/>\n"+
"  <C xd:script='options acceptEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A></A>", parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the parent element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='ref NB#B'\n"+
"    a='string(2); options noTrimAttr, ignoreEmptyAttributes'\n"+
"    b='string(2)'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B xd:script='ref C' c='optional string(2); options trimAttr'/>\n"+
"  <C xd:script='options ignoreEmptyAttributes'\n"+
"    d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a=' ab\t\n' b=' ab ' d='ab'></A>";
			assertEq("<A a=' ab  ' b='ab' d='ab'></A>",
				parse(xp, "NA", xml, reporter));
			xml = "<A a='' b='' c='' d=''></A>";
			assertEq("<A></A>", parse(xp, "NA", xml, reporter));
			// attributes "b", "c" and "d" inherits their options from the
			// NB X-Def header
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='ref NB#B' a='string(2); options setAttrUpperCase'\n"+
"    b='string(2)'/>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setAttrLowerCase'>\n"+
"  <B xd:script='ref C' c='optional string(2);'/>\n"+
"  <C d='string(2); options noTrimAttr'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A a='aB' b='aB' c='aB' d=' aB'></A>";
			assertEq("<A a='AB' b='ab' c='ab' d=' ab'></A>",
				parse(xp, "NA", xml, reporter));
			////////////////////////////////////////////////////////////////////
			///// the same, but text nodes instead of attributes are used //////
			////////////////////////////////////////////////////////////////////

			// attribut "c" inherits options from the refered element "A"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setTextLowerCase'>\n"+
"  <A xd:script='options preserveTextCase'>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B; options preserveTextCase'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextLowerCase'>\n"+
"  <B xd:script='options setTextLowerCase'>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E; options setTextLowerCase'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>\n"+
"    string(2);\n"+
"  </E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cc</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the parent element "B"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextLowerCase'>\n"+
"  <B xd:script='options preserveTextCase'>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E'/>"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>\n"+
"    string(2);\n"+
"  </E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cc</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" has default options
			// attribute "d" has default options
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E>\n"+
"    string(2);\n"+
"  </E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cC</C><D>dD</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" has default options
			// attribute "d" has default options
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def xd:name='NA' xd:root='A'\n>\n"+
"  <A>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B'/>"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E'/>"+
"  </B>\n"+
"  <E>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cC</C><D>dD</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B'/>"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E'/>"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>string(2)</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cC</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the refered element "A"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setTextUpperCase'>\n"+
"  <A xd:script='options setTextLowerCase'>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"  <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextUpperCase'>\n"+
"  <B xd:script='options setTextLowerCase'>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script='ref E;'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>CC</C><D>DD</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the refered element "A"
			// attribute "d" inherits options from the refered element "A"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setTextUpperCase'>\n"+
"  <A xd:script='options setTextUpperCase'>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextUpperCase'>\n"+
"  <B xd:script='options setTextUpperCase'>\n"+
"    <C>optional string(2)</C>\n"+
"    <D xd:script=\"ref E; options setTextLowerCase\"/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextUpperCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>CC</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "c" inherits options from the NA X-Def header
			// attribute "d" inherits options from the NA X-Def header
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setTextLowerCase'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextUpperCase</A>\n"+
"    <B>string(2); options setTextLowerCase</B>\n"+
"    <C xd:script='ref NB#B; options setTextLowerCase'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextUpperCase'>\n"+
"  <B xd:script='options setTextLowerCase'>\n"+
"    <C>optional string(2); options preserveTextCase</C>\n"+
"    <D xd:script='ref E; options preserveTextCase'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>\n"+
"    string(2); options setTextUpperCase\n"+
"  </E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>AA</A><B>bb</B><C><C>cC</C><D>DD</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextLowerCase</A>\n"+
"    <B>string(2)</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C>optional string(2);</C>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextUpperCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>aa</A><B>bB</B><C><C>cC</C><D>DD</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the referenced element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'\n>\n"+
"  <A>\n"+
"    <A>string(2); options setTextLowerCase</A>\n"+
"    <B>string(2)</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B xd:script='options setTextUpperCase'>\n"+
"    <C>optional string(2);</C>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>aa</A><B>bB</B><C><C>cC</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the NB X-Def header
			// attribute "c" inherits options from the NB X-Def header
			// attribute "d" inherits options from the NB X-Def header
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'\n>\n"+
"  <A>\n"+
"    <A>string(2); options setTextLowerCase</A>\n"+
"    <B>string(2)</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options setTextLowerCase'>\n"+
"  <B>\n"+
"    <C>optional string(2);</C>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextUpperCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>aa</A><B>bB</B><C><C>cc</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			// attribute "b" inherits options from the referenced element "C"
			// attribute "c" inherits options from the parent element "C"
			// attribute "d" inherits options from the parent element "C"
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setTextUpperCase'>\n"+
"  <A>\n"+
"    <A>string(2); options setTextLowerCase</A>\n"+
"    <B>string(2)</B>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script=''>\n"+
"  <B>\n"+
"    <C>optional string(2);</C>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options setTextLowerCase'>string(2);</E>\n"+
"</xd:def>"+
"</xd:collection>";
			xp = compile(xdef);
			xml = "<A><A> aA\t\n</A><B> bB </B><C><C>cC</C><D>dD</D></C></A>";
			assertEq("<A><A>aa</A><B>BB</B><C><C>CC</C><D>dd</D></C></A>",
				parse(xp, "NA", xml, reporter));
			////////////////////////////////////////////////////////////////////
			//// the same, but the options inheritance of elements is tested ///
			////////////////////////////////////////////////////////////////////
			xdef = // moreAttributes
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"  <C/>\n"+
"  <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options moreAttributes'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A/><B/><C><C/><D d=''/></C></A>";
			assertEq("<A><A/><B/><C><C/><D d=''/></C></A>",
				parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B xd:script='options moreAttributes'>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A a=''/><B b=''/><C><C c=''/><D d=''/></C></A>";
			parse(xdef, "NA", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B; options moreAttributes'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A a=''/><B b=''/><C><C c=''/><D d=''/></C></A>";
			parse(xdef, "NA", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='options moreAttributes'>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A a=''/><B b=''/><C><C c=''/><D d=''/></C></A>";
			parse(xdef, "NA", xml, reporter);
			assertEq(4, reporter.getErrorCount());
			xdef = // moreText
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E xd:script='options moreText'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A></A><B></B><C><C></C><D>d</D></C></A>";
			assertEq("<A><A></A><B></B><C><C></C><D>d</D></C></A>",
				parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B xd:script='options moreText'>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A></A><B></B><C>c<C></C><D></D></C></A>";
			assertEq("<A><A></A><B></B><C>c<C></C><D></D></C></A>",
				parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B; options moreText'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A></A><B></B><C>c<C></C><D></D></C></A>";
			assertEq("<A><A></A><B></B><C>c<C></C><D></D></C></A>",
				parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A xd:script='options moreText'>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A>a</A><B>b</B><C>c<C>c</C><D>d</D></C></A>";
			parse(xdef, "NA", xml, reporter);
			assertEq(5, reporter.getErrorCount());
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options moreText'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A>a</A><B>b</B><C>c<C></C><D></D></C></A>";
			assertEq("<A><A>a</A><B>b</B><C>c<C></C><D></D></C></A>",
				parse(xdef, "NA", xml, reporter));
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A'>\n"+
"  <A>\n"+
"    <A/>\n"+
"    <B/>\n"+
"    <C xd:script='ref NB#B'/>\n"+
"  </A>\n"+
"</xd:def>"+
"<xd:def name='NB' root='B' script='options moreText'>\n"+
"  <B>\n"+
"    <C/>\n"+
"    <D xd:script='ref E'/>\n"+
"  </B>\n"+
"  <E/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A><A></A><B></B><C>c<C>c</C><D>d</D></C></A>";
			assertEq("<A><A></A><B></B><C>c<C>c</C><D>d</D></C></A>",
				parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef = //test property "xdef_clearReports" and onIllegal events
"<xd:def xmlns:xd='" + _xdNS + "' root = \"A\">\n" +
"<A xd:script='onIllegalElement out(\"e\");'\n" +
"   a='illegal int; onIllegalAttr out(\"a\"); onAbsence out(\"b\");'>\n"+
"  <B xd:script='illegal; onIllegalElement out(\"e\");'/>\n" +
"  illegal int; onIllegalText out(\"t\");\n" +
"</A>\n" +
"</xd:def>";
			assertTrue(compile(xdef).isClearReports());
			System.setProperty("xdef_clearReports", "true");
			xp = compile(xdef);
			assertTrue(xp.isClearReports());
			swr = new StringWriter();
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			el = parse(xd, "<A a='1'> <B/> 2 </A>", reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("abet", swr.toString());
			if (el.getChildNodes().getLength() > 0
				|| el.getAttributes().getLength() > 0) {
				fail(KXmlUtils.nodeToString(el));
			}
			System.setProperty("xdef_clearReports", "false");
			xp = compile(xdef);
			assertFalse(xp.isClearReports());
			swr = new StringWriter();
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			el = parse(xd, "<A a='1'> <B/> 2 </A>", reporter);
			assertEq(3, reporter.getErrorCount());
			reporter.clear();
			assertEq("abet", swr.toString());
			if (el.getChildNodes().getLength() > 0
				|| el.getAttributes().getLength() > 0) {
				fail(KXmlUtils.nodeToString(el));
			}
			xdef = //test property "xdef_saveReports" and onIllegal events
"<xd:def xmlns:xd='" + _xdNS + "' root = \"A\">\n" +
"<A xd:script='option clearReports; onIllegalElement out(\"e\");'\n" +
"   a='illegal int; onIllegalAttr out(\"a\"); onAbsence out(\"b\");'>\n"+
"  <B xd:script='illegal; option clearReports; onIllegalElement out(\"e\");'/>\n" +
"  illegal int; onIllegalText out(\"t\");\n" +
"</A>\n" +
"</xd:def>";
			xp = compile(xdef);
			swr = new StringWriter();
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			el = parse(xd, "<A a='1'> <B/> 2 </A>", reporter);
			assertNoErrorsAndClear(reporter);
			assertEq("abet", swr.toString());
			if (el.getChildNodes().getLength() > 0
				|| el.getAttributes().getLength() > 0) {
				fail(KXmlUtils.nodeToString(el));
			}
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = \"A\">\n" +
"<A xd:script='option preserveReports; onIllegalElement out(\"e\");'\n" +
"   a='illegal int; onIllegalAttr out(\"a\"); onAbsence out(\"b\");'>\n"+
"  <B xd:script='illegal; option preserveReports; onIllegalElement out(\"e\");'/>\n" +
"  illegal int; onIllegalText out(\"t\");\n" +
"</A>\n" +
"</xd:def>";
			xp = compile(xdef);
			swr = new StringWriter();
			xd = xp.createXDDocument();
			xd.setStdOut(XDFactory.createXDOutput(swr, false));
			el = parse(xd, "<A a='1'> <B/> 2 </A>", reporter);
			assertEq(3, reporter.getErrorCount());
			reporter.clear();
			assertEq("abet", swr.toString());
			if (el.getChildNodes().getLength() > 0
				|| el.getAttributes().getLength() > 0) {
				fail(KXmlUtils.nodeToString(el));
			}
			xdef = //(moreAttributes moreText moreElements) and (illegal ignore)
"<xd:def xmlns:xd='" + _xdNS + "' root=\"A|B|C\">\n" +
"  <A xd:script='option moreAttributes, moreText, moreElements'\n" +
"     x='illegal int'\n" +
"     y='ignore int' >\n" +
"    illegal int\n" +
"    <I xd:script='illegal'/>\n" +
"    <J xd:script='ignore'/>\n" +
"    ignore int\n" +
"  </A>\n" +
"  <B xd:script='ref X; option moreAttributes, moreText, moreElements'\n" +
"      x='illegal'\n" +
"      y='ignore int'>\n" +
"     illegal int\n" +
"    <I xd:script='illegal'/>\n" +
"    <J xd:script='ignore'/>\n" +
"    ignore int\n" +
"  </B>" +
"  <X x='int()' y='int()'></X>" +
"  <C xd:script='ref Y;' x='illegal'>\n" +
"    illegal int\n" +
"    <I xd:script='illegal'/>\n" +
"    <J xd:script='ignore'/>\n" +
"    ignore int\n" +
"  </C>\n" +
"  <Y xd:script=' option moreAttributes, moreText, moreElements'\n" +
"     x='int()'\n" +
"     y='ignore int' />\n" +
"</xd:def>";
			for (char c='A'; c <= 'C'; c = (char) (c+1)) {
				xml = "<"+c+" x='1' y='2' z='3' > 4 <I/> <J/> <K/> 5 </"+c+">";
				el = parse(xdef, null, xml, reporter); //not reported!
				if (reporter.errors()) {
					s = reporter.printToString();
					if (s.contains("F525") && s.contains("F528")
						&& s.contains("XDEF557")) {
						if (el.hasAttribute("x") || el.hasAttribute("y")) {
							fail("Contains @y or @y: "
								+ KXmlUtils.nodeToString(el));
						}
						if (!el.hasAttribute("z")) {
							fail("<"+c+"> not contains @z");
						}
						if ((s = el.getTextContent()) != null && !s.isEmpty()) {
							fail("<"+c+"> contains text");
						}
						if (el.getChildNodes().getLength() != 1 ||
							!"K".equals(el.getChildNodes().item(0).getNodeName())){
							fail("<"+c+"> not contains <K>");
						}
					} else {
						fail(reporter);
					}
				} else {
					fail("error not reported");
				}
			}
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='NA' root='A' script='options setAttrUpperCase'>\n"+
"  <A xd:script='ref NB#B; options setAttrLowerCase'/>\n"+
"</xd:def>"+
"<xd:def name='NB' xd:root='B'>\n"+
"  <B d='string'/>\n"+
"</xd:def>"+
"</xd:collection>";
			xml = "<A d='dD'/>";
			assertEq("<A d='dd'/>", parse(xdef, "NA", xml, reporter));
			assertNoErrorwarnings(reporter);

			////////////////////////////////////////////////////////////////////
			//////////////  test references in one X-Definition  ///////////////
			////////////////////////////////////////////////////////////////////
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='A'\n"+
" script='options setTextUpperCase'>\n"+
"  <A xd:script='ref C; options setTextLowerCase'/>"+
"    <C>\n"+
"    <B>string</B>string</C>\n"+
"</xd:def>";
			xml = "<A><B>bB</B>aA</A>";
			assertEq("<A><B>BB</B>aa</A>", parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='"+_xdNS+"' root='A' script='options setAttrUpperCase'>\n"+
"	<A xd:script='options setAttrLowerCase'>\n"+
"		<B b='string'/>\n"+
"	</A>\n"+
"</xd:def>";
			xml = "<A><B b='bB'/></A>";
			assertEq("<A><B b='BB'/></A>", parse(xdef, "", xml, reporter));
			assertNoErrorwarnings(reporter);
			//creating empty attributes
			xdef = // default => don't create empty attributes
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A>\n"+
"   <B a=\"optional; create ''\"/>\n"+
"  </A>\n"+
"</xd:def>";
			assertEq("<A><B/></A>", create(xdef, "", "A", reporter, null));
			xdef =  //IGNORE empty attributes
"<xd:def xmlns:xd='" + _xdNS + "' root='A'\n"+
"  xd:script='options acceptEmptyAttributes'>\n"+
"  <A>\n"+
"   <B xd:script='options ignoreEmptyAttributes' a=\"optional; create ''\"/>\n"+
"  </A>\n"+
"</xd:def>";
			assertEq("<A><B/></A>", create(xdef, "", "A", reporter, null));
			xdef = //ACCEPT empty attributes
"<xd:def xmlns:xd='" + _xdNS + "' root='A'\n"+
"   xd:script='options acceptEmptyAttributes'>\n"+
"  <A>\n"+
"   <B a=\"optional; create ''\"/>\n"+
"  </A>\n"+
"</xd:def>";
			assertEq("<A><B a=''/></A>", create(xdef,"","A",reporter,null));
			xdef = // option cdata
"<xd:def xmlns:xd='" + _xdNS + "' root='A'>\n"+
"  <A>string(); option cdata; create 'text'</A>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<A><![CDATA[text]]></A>";
			el = parse(xd, xml , reporter);
			assertNoErrorwarnings(reporter);
			assertTrue(el.getChildNodes().item(0).getNodeType()
				== Node.CDATA_SECTION_NODE);
			assertEq("text", el.getChildNodes().item(0).getNodeValue());
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq(xml, swr.toString());
			el = create(xd, "A", reporter);
			assertTrue(el.getChildNodes().item(0).getNodeType()
				== Node.CDATA_SECTION_NODE);
			assertEq("text", el.getChildNodes().item(0).getNodeValue());
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq(xml, swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:textcontent='string(); option cdata;'>\n"+
"    string(); create 'x1';\n"+
"    <b/>\n"+
"    string(); create 'x2';\n"+
"  </a>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b/>t2</a>";
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq(xml, swr.toString());
			assertNoErrorwarnings(reporter);
			xd = compile(xdef).createXDDocument();
			el = create(xd, "a", reporter);
			xml = "<a>x1<b/>x2</a>";
			assertEq(xml, el);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq(xml, swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var int i;'\n"+
"    xd:text='+ string(); create ++i; option cdata;'>\n"+
"    string; option cdata;\n"+
"    <b/>\n"+
"  </a>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b/>t2</a>";
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[t1]]><b/><![CDATA[t2]]></a>",swr.toString());
			el = create(xd, "a", reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[1]]><b/><![CDATA[2]]></a>",swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var int i;'\n"+
"    xd:text='* string(); create null; option cdata;'>\n"+
"    <b/>\n"+
"  </a>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			el = create(xd, "a", reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><b/></a>",swr.toString());
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"  <a xd:script='var int i;'\n"+
"    xd:text='+ string(); create ++i; option cdata;'>\n"+
"    string; option cdata; create 't1';\n"+
"    <b/>\n"+
"  </a>\n"+
"</xd:def>";
			xd = compile(xdef).createXDDocument();
			xml = "<a>t1<b/></a>";
			el = parse(xd, xml, reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[t1]]><b/></a>",swr.toString());
			el = create(xd, "a", reporter);
			assertNoErrorwarnings(reporter);
			swr = new StringWriter();
			KXmlUtils.writeXml(swr,
				null, //encoding
				el,
				null, //indentStep
				false, //canonical
				false, //removeIgnorableWhiteSpaces
				true); //comments
			assertEq("<a><![CDATA[t1]]><b/><![CDATA[1]]></a>",swr.toString());
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}
