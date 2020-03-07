package test.xdef;

import buildtools.XDTester;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.util.GregorianCalendar;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/** Test template
 * @author Vaclav Trojan
 */
public final class TestTemplate extends XDTester {

	public TestTemplate() {super();}

	@Override
	public void test() {
		String xdef;
		ArrayReporter reporter  = new ArrayReporter();
		XDPool xp;
		XDDocument xd;
		String s;
		String xml;
		Element el;
		try {
			//default trimtext
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script=\"template\"\n"+
"  a1=\"a1\">" +
"<b>" +
"<c a2=\"a2\" />" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>t</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</a>\n"+
"</xd:def>";
			xml =
"<a a1=\"a1\">" +
"<b>" +
"<c a2=\"a2\" />" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>t</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //noTrimText in header
"<xd:def xmlns:xd='" + _xdNS + "' script='options noTrimText' root='a'>\n"+
"<a xd:script=\"template\"\n"+
"   a1=\"a1\">\n"+
"  <b>\n"+
"  <c a2=\"a2\" />\n"+
"<![CDATA[xx]]>\n"+
" y </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>t</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"</xd:def>";
			xml =
"<a a1=\"a1\">\n"+
"  <b>\n"+
"  <c a2=\"a2\" />\n"+
"<![CDATA[xx]]>\n"+
" y </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>t</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //trimText
"<xd:def xmlns:xd='" + _xdNS + "' root='a'>\n"+
"<a xd:script=\"template; options trimText\"\n"+
"   a1=\"a1\">\n"+
"  <b>\n"+
"  <c a2=\"a2\" />\n"+
"  </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>t</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"</xd:def>";
			xml =
"<a a1=\"a1\">" +
"<b>" +
"<c a2=\"a2\" />" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>t</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</a>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "a", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //modification of template
"<xd:def xmlns:xd='" + _xdNS + "' root='x'>\n"+
"<a xd:script=\"template; options trimText\"\n"+
"   a1=\"xxx\">\n"+
"  <b>\n"+
"  <c a2=\"a2\" />\n"+
"  </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>t</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"\n"+
"<x xd:script=\"ref a\"\n"+
"  a1=\"required string(); create 'a1'\"\n"+
"  aa=\"required string(); create 'aa'\" >\n"+
"</x>\n"+
"</xd:def>";
			xml =
"<x a1=\"a1\" aa = \"aa\">" +
"<b>" +
"<c a2=\"a2\"/>" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>t</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</x>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "x", reporter));
			assertNoErrorwarnings(reporter);
			xdef = // $$$script:
"<xd:def xmlns:xd='" + _xdNS + "' root='x'>\n"+
"<xd:declaration>\n"+
"  String a1() {return 'a1';}\n"+
"  String aa() {return 'aa';}\n"+
"</xd:declaration>\n"+
"<a xd:script=\"template; options trimText\"\n"+
"   a1=\"aaa\" a2=\"$$$script: optional string(); create 'a2 a2'\">\n"+
"   $$$script: optional string(); create 'a a'\n"+
"  <b>\n"+
"  <c a2=\"xxx\" />\n"+
"  </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>t</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g></g>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"\n"+
"<x xd:script=\"ref a\"\n"+
"  a1=\"required string(); create a1()\"\n"+
"  aa=\"required string(); create aa()\" >\n"+
"</x>\n"+
"</xd:def>";
			xml =
"<x a1=\"a1\" aa = \"aa\">" +
"<b>" +
"<c a2=\"xxx\"/>" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>t</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</x>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq("<x a1=\"a1\" aa = \"aa\" a2 = \"a2 a2\">a a" +
"<b><c a2=\"xxx\"/></b><c><d/><e/><e>t</e><e/><f/><g/></c><c/></x>",
				create(xp, "", "x", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //method call
"<xd:def xmlns:xd='" + _xdNS + "' root='x'>\n"+
"<xd:declaration>\n"+
"  String a1() {return 'a1';}\n"+
"  String aa() {return 'aa';}\n"+
"</xd:declaration>\n"+
"\n"+
"<a xd:script=\"template; options trimText\"\n"+
"   a1=\"syntea.cz\">\n"+
"  <b>\n"+
"  <c a2=\"xxx\" />\n"+
"  </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>text</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"\n"+
"<x xd:script=\"ref a\"\n"+
"  a1=\"required string(); create a1()\"\n"+
"  aa=\"required string(); create aa()\" >\n"+
"</x>\n"+
"</xd:def>";
			xml =
"<x a1=\"a1\" aa = \"aa\">" +
"<b>" +
"<c a2=\"xxx\"/>" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>text</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</x>";
			xp = compile(xdef);
			parse(xp, "", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "", "x", reporter));
			assertNoErrorwarnings(reporter);
			xdef = //collection
"<xd:collection xmlns:xd='" + _xdNS + "'>\n"+
"<xd:def name='test1'>\n"+
"<xd:declaration>\n"+
"  String a1() {return 'a1';}\n"+
"  String aa() {return 'aa';}\n"+
"</xd:declaration>\n"+
"<a xd:script=\"template; options trimText\"\n"+
"   a1=\"syntea.cz\">\n"+
"  <b>\n"+
"  <c a2=\"xxx\" />\n"+
"  </b>\n"+
"    <c>\n"+
"    <d/>\n"+
"    <e/>\n"+
"    <e>text</e>\n"+
"    <e/>\n"+
"    <f/>\n"+
"    <g/>\n"+
"  </c>\n"+
"  <c/>\n"+
"</a>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name = \"test2\" xd:root = \"x\" >\n"+
"<x xd:script=\"ref test1#a\"\n"+
"  a1=\"required string(); create a1()\"\n"+
"  aa=\"required string(); create aa()\" >\n"+
"</x>\n"+
"</xd:def>\n"+
"\n"+
"</xd:collection>\n";
			xml =
"<x a1=\"a1\" aa = \"aa\">" +
"<b>" +
"<c a2=\"xxx\"/>" +
"</b>" +
"<c>" +
"<d/>" +
"<e/>" +
"<e>text</e>" +
"<e/>" +
"<f/>" +
"<g/>" +
"</c>" +
"<c/>" +
"</x>";
			xp = compile(xdef);
			parse(xp, "test2", xml, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, create(xp, "test2", "x", reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'elem'>\n"+
"<elem xd:script = \"template; options preserveTextWhiteSpaces, noTrimText\"\n"+
"    a1 = \"ab cd\"\n"+
"    a2 = \"$$$script: optional xdatetime('yyyy/M/d');\n"+
"                      create now().toString('yyyy/M/d')\">"+
"<child1/><child2>\n text  1 \n</child2><child2>text2</child2>"+
"</elem>\n"+
"</xd:def>";
			xml = "<elem a2=\"{d}\" a1=\"ab cd\">"
				+ "<child1/><child2>\n text  1 \n</child2><child2>text2</child2>"
				+ "</elem>";
			s = SUtils.modifyString(xml, "{d}", "2010/01/31");
			xp = compile(xdef);
			assertEq(s, parse(xp, "", s, reporter));
			assertNoErrorwarnings(reporter);
			el = create(xp, "", "elem", reporter);
			s = new SDatetime(new GregorianCalendar()).formatDate("yyyy/M/d");
			assertNoErrorwarnings(reporter);
			assertEq(SUtils.modifyString(xml, "{d}", s), el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'elem'\n"+
"        xd:script= 'options preserveTextWhiteSpaces, noTrimText' >\n"+
"<elem xd:script = \"template\"\n"+
"    a1 = \"ab cd\"\n"+
"    a2 = \"$$$script: optional xdatetime('yyyy/M/d');\n"+
"                      create now().toString('yyyy/M/d')\">"+
"<child1/><child2>\n text  1 \n</child2><child2>text2</child2>"+
"</elem>\n"+
"</xd:def>";
			s = SUtils.modifyString(xml, "{d}", "2010/01/31");
			xp = compile(xdef);
			assertEq(s, parse(xp, "", s, reporter));
			assertNoErrorwarnings(reporter);
			el = create(xp, "", "elem", reporter);
			s = new SDatetime(new GregorianCalendar()).formatDate("yyyy/M/d");
			assertNoErrorwarnings(reporter);
			assertEq(SUtils.modifyString(xml, "{d}", s), el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='elem'\n"+
"        xd:script= 'options ignoreTextWhiteSpaces, trimText,\n"+
"                    ignoreAttrWhiteSpaces, trimAttr' >\n"+
"<elem xd:script = \"template\"\n"+
"    a1 = \" ab  cd \"\n"+
"    a2 = \"$$$script: optional xdatetime('yyyy/M/d');\n"+
"                      create now().toString('yyyy/M/d')\">"+
"<child1/><child2>\ntext  1\n</child2><child2>text2</child2>"+
"</elem>\n"+
"</xd:def>";
			xml = "<elem a2=\"{d}\" a1=\"ab cd\">"
				+ "<child1/><child2>text 1</child2><child2>text2</child2>"
				+ "</elem>";
			s = SUtils.modifyString(xml, "{d}", "2010/01/31");
			xp = compile(xdef);
			assertEq(s, parse(xp, "", s, reporter));
			assertNoErrorwarnings(reporter);
			el = create(xp, "", "elem", reporter);
			s = new SDatetime(new GregorianCalendar()).formatDate("yyyy/M/d");
			assertNoErrorwarnings(reporter);
			assertEq(SUtils.modifyString(xml, "{d}", s), el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'elem'\n"+
"        xd:script= 'options ignoreTextWhiteSpaces, trimText,\n"+
"                    ignoreAttrWhiteSpaces, trimAttr' >\n"+
"<elem xd:script = \"template\"\n"+
"    attr1 = \" ab  cd \"\n"+
"    attr2 = \"$$$script: optional xdatetime('yyyy/M/d');\n"+
"                         create now().toString('yyyy/M/d')\">\n"+
"  <child1/>\n"+
"  <child2>\n"+
"    text  1 \n"+
"  </child2>\n"+
"  <child2>\n"+
"    text2\n"+
"  </child2>\n"+
"</elem>\n"+
"</xd:def>";
			xml = "<elem attr2=\"{d}\" attr1=\"ab cd\">"
				+ "<child1/><child2>text 1</child2><child2>text2</child2>"
				+ "</elem>";
			s = SUtils.modifyString(xml, "{d}", "2010/01/31");
			xp = compile(xdef);
			assertEq(s, parse(xp, "", s, reporter));
			assertNoErrorwarnings(reporter);
			el = create(xp, "", "elem", reporter);
			s = new SDatetime(new GregorianCalendar()).formatDate("yyyy/M/d");
			assertNoErrorwarnings(reporter);
			assertEq(SUtils.modifyString(xml, "{d}", s), el);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
" name = 'Example' root = 'html' xmlns='http://www.w3.org/1999/xhtml'>\n"+
"<html>\n"+
"<head xd:script='template'><title>Panovníci českých zemí</title></head>\n"+
"<body>\n"+
"  <table border=\"create 'border'\">\n"+
"<tr xd:script='template'>"+
"<td>Panovník</td>"+
"<td>vládl od</td>"+
"<td>vládl do</td>"+
"<td>po dobu</td>"+
"</tr>\n"+
"    <tr xd:script=\"*;create from('//panovnik').sort('panoval/od/text()')\">\n"+
"        <td>create from('jmeno/text()')</td>\n"+
"        <td>create from('panoval/od/text()')</td>\n"+
"        <td>optional string(); create from('panoval/do/text()')</td>\n"+
"        <td>optional string();\n"+
"           create from('panoval/do/text()').getLength() == 0 ? null :\n"+
"              from('number(panoval/do/text()) - number(panoval/od/text())')\n"+
"        </td>\n"+
"    </tr>\n"+
"  </table>\n"+
"</body>"+
"</html>\n"+
"</xd:def>";
			xml =
"<panovnici>\n"+
"  <panovnik><jmeno>Tomáš Garrigue Masaryk</jmeno>\n"+
"   <panoval titul=\"prezident\"><od>1918</od><do>1935</do></panoval>\n"+
"  </panovnik>\n"+
"  <panovnik rod=\"přemyslovec\"><jmeno>Václav I.</jmeno>\n"+
"    <panoval titul=\"král český\"><od>1230</od><do>1253</do></panoval>\n"+
"  </panovnik>\n"+
"  <panovnik><jmeno>Václav Klaus</jmeno>\n"+
"   <panoval titul=\"prezident\"><od>2003</od></panoval>\n"+
"  </panovnik>\n"+
"</panovnici>";
			assertEq(create(xdef, "Example",
				new QName("http://www.w3.org/1999/xhtml", "html"),
				reporter, xml, null, null),
"<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
"<head><title>Panovníci českých zemí</title></head>"+
"<body><table border=\"border\">"+
"<tr>"+
"<td>Panovník</td>"+
"<td>vládl od</td>"+
"<td>vládl do</td>"+
"<td>po dobu</td>"+
"</tr>"+
"<tr><td>Václav I.</td><td>1230</td><td>1253</td><td>23</td></tr>"+
"<tr><td>Tomáš Garrigue Masaryk</td><td>1918</td><td>1935</td><td>17</td></tr>"+
"<tr><td>Václav Klaus</td><td>2003</td><td/><td/></tr>"+
"</table></body></html>");
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"        xmlns='http://www.w3.org/1999/xhtml'>\n"+
"<html>\n"+
"   <head xd:script='template'><title>Panovníci českých zemí</title></head>\n"+
"<body xd:script='template'>"+
"<table border=\"border\">"+
"<tr><td>Panovník</td></tr>"+
"<tr xd:script='$$$script:*;\n"+
"       create from(\"//panovnik\").sort(\"panoval/od/text()\")'>\n"+
"         <td>create from('jmeno/text()')</td>\n"+
"</tr>"+
"</table>"+
"</body>\n"+
"</html>\n"+
"</xd:def>\n";
			xml =
"<panovnici>\n"+
"  <panovnik><jmeno>Neznamy</jmeno>\n"+
"   <panoval><od>3000</od></panoval>\n"+
"  </panovnik>\n"+
"  <panovnik><jmeno>Václav Havel</jmeno>\n"+
"   <panoval><od>1989</od><do>2003</do></panoval>\n"+
"  </panovnik>\n"+
"  <panovnik><jmeno>Přemysl Otakar I.</jmeno>\n"+
"    <panoval><od>1192</od><do>1193</do></panoval>\n"+
"  </panovnik>\n"+
"</panovnici>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			s = "<html xmlns=\"http://www.w3.org/1999/xhtml\">"+
				"<head><title>Panovníci českých zemí</title></head>"+
				"<body><table border=\"border\">"+
				"<tr><td>Panovník</td></tr>"+
				"<tr><td>Přemysl Otakar I.</td></tr>"+
				"<tr><td>Václav Havel</td></tr>"+
				"<tr><td>Neznamy</td></tr>"+
				"</table>"+
				"</body>"+
				"</html>";
			assertEq(s, create(xd,
				new QName("http://www.w3.org/1999/xhtml", "html"), reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"        xmlns='http://www.w3.org/1999/xhtml'>\n"+
"<html xd:script='template'>"+
"<head><title>Panovníci českých zemí</title></head>"+
"<body>"+
"<table border=\"border\">"+
"<tr><td>Panovník</td></tr>"+
"<tr xd:script='$$$script:*;\n"+
"           create from(\"//panovnik\").sort(\"panoval/od/text()\")'>\n"+
"         <td>create from('jmeno/text()')</td>\n"+
"</tr>"+
"</table>"+
"</body>"+
"</html>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setXDContext(xml);
			assertEq(s, create(xd,
				new QName("http://www.w3.org/1999/xhtml", "html"), reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"        xmlns='http://www.w3.org/1999/xhtml'>\n"+
"<xd:declaration>external Element source</xd:declaration>\n"+
"<html xd:script='template'>\n"+
"  <head><title>Panovníci českých zemí</title></head>\n"+
"  <body>\n"+
"    <table border=\"border\">\n"+
"      <tr><td>Panovník</td></tr>\n"+
"      <tr xd:script='$$$script:*;\n"+
"           create from(source,\"//panovnik\").sort(\"panoval/od/text()\")'>\n"+
"         <td>create from('jmeno/text()')</td>\n"+
"      </tr>\n"+
"    </table>\n"+
"  </body>\n"+
"</html>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("source", xml);
			assertEq(s,
				create(xd,
				new QName("http://www.w3.org/1999/xhtml", "html"), reporter));
			assertNoErrorwarnings(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'\n"+
"        xmlns='http://www.w3.org/1999/xhtml'\n"+
"        script='options noTrimAttr,noTrimText'>\n"+
"<xd:declaration>external Element source</xd:declaration>\n"+
"<html xd:script='template'>\n"+
"  <head><title>Panovníci českých zemí</title></head>\n"+
"  <body>\n"+
"    <table border=\"border\">\n"+
"      <tr><td>Panovník</td></tr>\n"+
"      <tr xd:script=\"$$$script:*;\n"+
"           create from(source,'//panovnik').sort('panoval/od/text()')\">\n"+
"         <td>create from('jmeno/text()')</td>\n"+
"      </tr>\n"+
"    </table>\n"+
"  </body>\n"+
"</html>\n"+
"</xd:def>\n";
			xp = compile(xdef);
			xd = xp.createXDDocument();
			xd.setVariable("source", xml);
			xml = "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"+
"  <head><title>Panovníci českých zemí</title></head>\n"+
"  <body>\n"+
"    <table border=\"border\">\n"+
"      <tr><td>Panovník</td></tr>\n"+
"      <tr>\n"+
"        <td>Přemysl Otakar I.</td>\n"+
"      </tr><tr>\n"+
"        <td>Václav Havel</td>\n"+
"      </tr><tr>\n"+
"        <td>Neznamy</td>\n"+
"       </tr>\n"+
"    </table>\n"+
"  </body>\n"+
"</html>";
			el = create(xd,
				new QName("http://www.w3.org/1999/xhtml", "html"), reporter);
			assertEq(el, xml);
			assertNoErrorwarnings(reporter);
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}

}