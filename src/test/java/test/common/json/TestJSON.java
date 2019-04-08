package test.common.json;

import org.xdef.sys.JSONUtil;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import test.utils.STester;

/** Test JSON tools.
 * @author trojan
 */
public class TestJSON extends STester {

	public TestJSON() {super();}

	static String chkXML(String xml) {
		return chkXML(KXmlUtils.parseXml(xml).getDocumentElement());
	}

	static String chkXML(Element e) {
		Object j = JSONUtil.xmlToJson(e);
		Element e1 = JSONUtil.jsonToXml(j, null);
		Object j1 = JSONUtil.xmlToJson(e1);
		if (!JSONUtil.jsonEqual(j1, j)
			&& !KXmlUtils.compareElements(e, e1, true, null).errorWarnings()){
			StringBuilder sb = new StringBuilder();
			sb.append(KXmlUtils.nodeToString(e, true)).append('\n');
			sb.append(JSONUtil.toJSONString(j, true)).append("\n*********\n");
			sb.append(KXmlUtils.nodeToString(e1, true)).append('\n');
			sb.append(JSONUtil.toJSONString(j1, true)).append("\n*********\n");
			return "XML:\n" + sb;
		} else {
			chkJSON(JSONUtil.toJSONString(j, true));
			return "";
		}
	}

	static String chkJSON(String json) {
		return chkJSON(JSONUtil.parseJSON(json));
	}

	static String chkJSON(Object j) {
		Element e = JSONUtil.jsonToXml(j, null);
		Object j1 = JSONUtil.xmlToJson(e);
		Element e1 = JSONUtil.jsonToXml(j1, null);
		if (!JSONUtil.jsonEqual(j1, j)
			&& !KXmlUtils.compareElements(e, e1, true, null).errorWarnings()){
			StringBuilder sb = new StringBuilder();
			sb.append(JSONUtil.toJSONString(j, true)).append('\n');
			sb.append(KXmlUtils.nodeToString(e, true)).append("\n*********\n");
			sb.append(JSONUtil.toJSONString(j1, true)).append('\n');
			sb.append(KXmlUtils.nodeToString(e1, true)).append("\n*********\n");
			return "JSON:\n" + sb;
		}
		return "";
	}
	
	static String check(String s) {
		if (s.charAt(0) == '<') {
			return chkXML(s);
		} else {
			return chkJSON(s);
		}
	}
	
	@Override
	/** Run test and print error information. */
	public void test() {
		boolean T; //This flag is used to return from a test
		T = true; //This flag is used to return from a test
//		T = false; //This flag is used to return from a test
		try {
			assertEq("", check(
"<js:array xmlns:js='"+XDConstants.JSON_NS_URI+"'>\n"+
"<a/>\n"+
"</js:array>"));
		assertEq("", check(
"<js:map xmlns:js='"+XDConstants.JSON_NS_URI+"'>\n"+
"    <plat>1000</plat>\n" +
"    <jmeno>Alena</jmeno>\n" +
"    <zena>true</zena>\n" +
"    <kocka>\n" +
"        <jmeno>Mnau</jmeno>\n" +
"        <vek>1</vek>\n" +
"    </kocka>\n" +
"</js:map>"));
		assertEq("", check(
"<js:map xmlns:js='"+XDConstants.JSON_NS_URI+"'\n"+
"        plat='1000'\n"+
"        jmeno='Alena'\n"+
"        zena='true'>\n" +
"  <kocka jmeno='Mnau'\n"+
"         vek='1'/>\n" +
"</js:map>"));
		assertEq("", check(
"<neco plat='1000'\n"+
"      jmeno='Alena'\n"+
"      zena='true' >\n" +
"  <kocka jmeno='Mnau'\n"+
"         vek='1' />\n" +
"</neco>"));
		assertEq("", check(
"<neco plat='1000'\n"+
"      jmeno='Alena'\n"+
"      zena='true' >\n" +
"  \"\"\n"+
"  <kocka jmeno='Mnau'\n"+
"         vek='1' />\n" +
" true\n" +
"</neco>"));
		assertEq("", check(
"<neco plat='1000'\n"+
"      jmeno='Alena'\n"+
"      zena='true' >\n" +
"  <js:string xmlns:js='"+XDConstants.JSON_NS_URI+"'/>\n"+
"  <kocka jmeno='Mnau'\n"+
"         vek='1' />\n" +
"  <js:item xmlns:js='"+XDConstants.JSON_NS_URI+"'>true</js:item>\n"+
"</neco>"));
		assertEq("", check(
"<personnel>\n" +
"  <person>\n" +
"    <id>Big.Boss</id>\n" +
"    <name>\n" +
"      <family>Boss</family>\n" +
"      <given>Big</given>\n" +
"    </name>\n" +
"    <email>chief@oxygenxml.com</email>\n" +
"    <link>\n" +
"      <subordinates>one.worker</subordinates>\n" +
"      <subordinates>two.worker</subordinates>\n" +
"    </link>\n" +
"  </person>\n" +
"  <person>\n" +
"    <id>one.worker</id>\n" +
"    <name>\n" +
"      <family>Worker</family>\n" +
"      <given>One</given>\n" +
"    </name>\n" +
"    <email>one@oxygenxml.com</email>\n" +
"    <link>\n" +
"      <manager>Big.Boss</manager>\n" +
"    </link>\n" +
"  </person>\n" +
"  <person>\n" +
"    <id>two.worker</id>\n" +
"    <name>\n" +
"      <family>Worker</family>\n" +
"      <given>Two</given>\n" +
"    </name>\n" +
"    <email>two@oxygenxml.com</email>\n" +
"    <link>\n" +
"      <manager>Big.Boss</manager>\n" +
"    </link>\n" +
"  </person>\n" +
"</personnel>"));
		assertEq("", check("{}"));
		assertEq("", check("{\"x\":{}}"));
		assertEq("", check("{\"x\":[]}"));
		assertEq("", check("[]"));
		assertEq("", check("[[]]"));
		assertEq("", check("[{}]"));
		assertEq("", check(
"{\n" +
"  \"plat\":1000.0,\n" +
"  \"jmeno\":\"Alena\",\n" +
"  \"zena\":true,\n" +
"  \"kocka\":{\n" +
"    \"jmeno\":\"Mnau\",\n" +
"    \"vek\":1.0\n" +
"  }\n" +
"}"));
		assertEq("", check(
"{\n" +
"  \"jmeno\":\"Alena\",\n" +
"  \"plat\":1000.0,\n" +
"  \"zena\":true,\n" +
"  \"kocka\":{\n" +
"    \"jmeno\":\"Mnau\",\n" +
"    \"vek\":1.0\n" +
"  }\n" +
"}"));
		assertEq("", check(
"{\n" +
"  \"neco\":{\n" +
"    \"jmeno\":\"Alena\",\n" +
"    \"plat\":1000.0,\n" +
"    \"zena\":true,\n" +
"    \"kocka\":{\n" +
"      \"jmeno\":\"Mnau\",\n" +
"      \"vek\":1.0\n" +
"    }\n" +
"  }\n" +
"}"));
		assertEq("", check(
"[\n" +
"  \"\",\n" +
"  {\n" +
"    \"kocka\":{\n" +
"      \"jmeno\":\"Mnau\",\n" +
"      \"vek\":1.0\n" +
"    }\n" +
"  },\n" +
"  true\n" +
"]"));
		assertEq("", check(
"[\n" +
"  \"\",\n" +
"  {\n" +
"    \"kocka\":{\n" +
"      \"jmeno\":\"Mnau\",\n" +
"      \"vek\":1.0\n" +
"    }\n" +
"  },\n" +
"  true\n" +
"]"));
		assertEq("", check(
"[\n" +
"  {\n" +
"    \"person\":{\n" +
"      \"id\":\"Big.Boss\",\n" +
"      \"name\":{\n" +
"        \"family\":\"Boss\",\n" +
"        \"given\":\"Big\"\n" +
"      }\n" +
"    }\n" +
"  },\n" +
"  {\n" +
"    \"person\":{\n" +
"      \"id\":\"Big.Boss\"\n" +
"    }\n" +
"  },\n" +
"  {\n" +
"    \"person\":{\n" +
"      \"id\":\"Big.Boss\"\n" +
"    }\n" +
"  }\n" +
"]"));
		} catch (Exception ex) {fail(ex);}
//if(T)return;		
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
