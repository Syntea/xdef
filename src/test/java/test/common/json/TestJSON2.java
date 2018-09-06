/*
 * Copyright 2014 Syntea software group a.s. All rights reserved.
 *
 * File: TestJSON1, created 2014-08-06.
 * Package: mytest.json
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package test.common.json;

import test.utils.STester;

/** Test JSON tools.
 *
 * @author trojan
 */
public class TestJSON2 extends STester {

	public TestJSON2() {super();}

	private static boolean check(String el, String json) {
		return TestJSON.check(el, json);
	}
	private static boolean check(String x) {
		return TestJSON.check(x);
	}

	@Override
	/** Run test and print error information. */
	public void test() {

//		assertTrue(check("<a><b/><c/></a>", "{\"a\":[{\"b\":null},{\"c\":null}]}"));
/*
o1:[{a=null}, {b=null}, {c=null}]
e2:<js:array xmlns:js="http://www.syntea.cz/json/1.0"><a/><b/><c/></js:array>
should be:
o1:{"a":[{"b":=null}, {"c":null}]}
*/
if(true) return;
/*xx*
		assertTrue(check("<a>\\a\"</a>", "{\"a\":\"\\\\a\"\"\"}"));
	if(true) return;
/*xx*/
		assertTrue(check("<js:array xmlns:js='http://www.syntea.cz/json/1.0'/>",
			"[]"));
		assertTrue(check("<js:map xmlns:js='http://www.syntea.cz/json/1.0'/>",
			"{}"));
		assertTrue(check("<a/>", "{\"a\":null}"));
		assertTrue(check("<a x='1'/>", "{\"a\":{\"x\":1}}"));
		assertTrue(check("<a>1</a>", "{\"a\":1}"));
		assertTrue(check("<a>\\a\"</a>", "{\"a\":\"\\\\a\"\"\"}"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>0</js:item>"+
"</js:array>", "[0]"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>0</js:item><js:item>1</js:item>"+
"</js:array>", "[0, 1]"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/>"
+ "<js:item>x</js:item>"+
"</js:array>",
			"[{\"a\":{\"x\":1}},\"x\"]"));
		assertTrue(check(
"<a>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<js:item>GM</js:item><js:item>XM</js:item>"
+ "</js:array>"+
"</a>",
"{\"a\":[\"GM\",\"XM\"]}"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/><js:item>x</js:item><js:item>y</js:item>"+
"</js:array>",
"[{\"a\":{\"x\":1}},\"x\",\"y\"]"));
		assertTrue(check(
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+   "<b>\"\tx\n\"</b>"
+   "<js:item>\"\tx\n\t\"</js:item>"
+ "</js:array>"+
"</a>"),
"{\"a\":[{\"b\":\"\tx\n\"},\"\tx\n\t\"]}");
		assertTrue(check("<a b='null'></a>", "{\"a\":{\"b\":null}}"));
		assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<x>"
+     "<js:array>"
	+ "<js:item>\"1\"</js:item>"
	+ "<js:item>null</js:item>"
	+ "<js:item>-1.23E+4</js:item>"
	+ "<js:item>\\a\"</js:item>"
+     "</js:array>"
+   "</x>"
+ "</js:mapItems>"+
"</a>",
"{\"a\":{\"x\":[\"1\",null,-1.23E+4,\"\\\\a\"\"\"]}}"));
		assertTrue(check(
"<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+ "<a/>"
+ "<js:item>\"1\"</js:item>"
+ "<b/>"
+ "<js:item>\"\"</js:item>"+
"</js:array>",
"[{\"a\":null},\"1\",{\"b\":null},\"\"]"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a/>"
+ "<js:item>1</js:item>"
+ "<b/>"
+ "<js:item>\"\"</js:item>"+
"</js:array>",
"[{\"a\":null},1,{\"b\":null},\"\"]"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/>"
+ "<b/>"+
"</js:array>",
"[{\"a\":{\"x\":1}},{\"b\":null}]"));
		assertTrue(check(
"<a>"
	+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
	+ "<b/><c/>"
	+ "</js:array>"+
"</a>",
"{\"a\":[{\"b\":null}, {\"c\":null}]}"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/>"
+ "<b y=\"\"/>"+
"</js:array>",
"[{\"a\":{\"x\":1}},{\"b\":{\"y\":\"\"}}]"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\" y=\"true\"/>"
+ "<js:array>"
+   "<b y=\"x\"/>"
+   "<js:item>1</js:item>"
+ "</js:array>"
+ "<js:array>"
+   "<c y=\"y\"/>"
+   "<js:item>2</js:item>"
+ "</js:array>"+
"</js:array>",
"["
+ "{\"a\":{\"y\":true,\"x\":1}},"
+ "["
+   "{\"b\":{\"y\":\"x\"}},"
+   "1"
+ "],"
+ "["
+   "{\"c\":{\"y\":\"y\"}},"
+   "2"
+ "]"+
"]"));
		assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<b>"
+     "<js:array>"
+       "<js:map>"
+         "<d e='5'/>"
+         "<c>C</c>"
+       "</js:map>"
+     "</js:array>"
+   "</b>"
+ "</js:mapItems>" +
"</a>",
"{"
+ "\"a\":{"
+   "\"b\":["
+     "{"
+       "\"d\":{\"e\":5},"
+       "\"c\":\"C\""
+     "}"
+   "]"
+ "}" +
"}"));
		assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<b c=\"C\">"
+       "<js:mapItems>"
+         "<d e='5'/>"
+       "</js:mapItems>"
+   "</b>"
+ "</js:mapItems>" +
"</a>",
"{"
+ "\"a\":{"
+   "\"b\":{"
+     "\"d\":{\"e\":5},"
+     "\"c\":\"C\""
+   "}"
+ "}" +
"}"));
		assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<b>"
+     "<js:array>"
+       "<js:map>"
+         "<d e='5'/>"
+         "<c>C</c>"
+       "</js:map>"
+     "</js:array>"
+   "</b>"
+ "</js:mapItems>" +
"</a>",
"{"
+ "\"a\":{"
+   "\"b\":["
+     "{"
+       "\"d\":{\"e\":5},"
+       "\"c\":\"C\""
+     "}"
+   "]"
+ "}" +
"}"));
		assertTrue(check(
"<A x=\"null\" y=\"1\" z=\"false\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<g h=\"/\" i=\"/i/*\"/>"
+   "<a>"
+     "<js:array>"
+       "<js:map>"
+         "<b>B</b>"
+         "<c C=\"5\"/>"
+       "</js:map>"
+       "<js:map>"
+         "<b>B1</b>"
+         "<c C=\"C2\"/>"
+       "</js:map>"
+       "<d>d</d>"
+       "<b c=\"true\"/>"
+     "</js:array>"
+   "</a>"
+   "<j k=\"/K\"/>"
+ "</js:mapItems>" +
"</A>",
"{"
+ "\"A\":{"
+   "\"g\":{"
+     "\"h\":\"/\","
+     "\"i\":\"/i/*\""
+   "},"
+ "\"a\":["
+   "{"
+     "\"b\":\"B\","
+     "\"c\":{\"C\":5}},"
+     "{\"b\":\"B1\",\"c\":{\"C\":\"C2\"}},"
+     "{\"d\":\"d\"},"
+     "{\"b\":{\"c\":true}"
+   "}"
+ "],"
+ "\"j\":{\"k\":\"/K\"},"
+ "\"z\":false,"
+ "\"y\":1,"
+ "\"x\":null"
+ "}"+
"}"));
		assertTrue(check(
"<A>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<servlet-mapping cofaxCDS=\"/\" cofaxTools=\"/tools/*\"/>"
+   "<taglib taglib-location=\"/WEB-INF/tlds/cofax.tld\"/>"
+   "<a>"
+   "<js:array>"
+     "<js:map>"
+       "<init-param maxUrlLength=\"500\"/>"
+       "<servlet-name>cofaxCDS</servlet-name>"
+     "</js:map>"
+     "<js:map>"
+       "<init-param mailHostOverride=\"mail2\"/>"
+       "<servlet-name>cofaxEmail</servlet-name>"
+     "</js:map>"
+     "<servlet-class>FileServlet</servlet-class>"
+     "<init-param betaServer=\"true\"/>"
+ "</js:array>"
+ "</a>"
+ "</js:mapItems>"+
"</A>",
"{"
+ "\"A\":{"
+   "\"servlet-mapping\":{"
+     "\"cofaxCDS\":\"/\","
+     "\"cofaxTools\":\"/tools/*\""
+   "},"
+   "\"taglib\":{"
+     "\"taglib-location\":\"/WEB-INF/tlds/cofax.tld\""
+   "},"
+   "\"a\":["
+     "{"
+       "\"init-param\":{\"maxUrlLength\":500},"
+       "\"servlet-name\":\"cofaxCDS\""
+     "},"
+     "{\"init-param\":{\"mailHostOverride\":\"mail2\"},"
+       "\"servlet-name\":\"cofaxEmail\""
+     "},"
+     "{\"servlet-class\":\"FileServlet\"},"
+      "{"
+         "\"init-param\":{\"betaServer\":true}"
+      "}"
+   "]"
+ "}"+
"}"));
		assertTrue(check(
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a/>"
+ "<x>\"1\"</x>"
+ "<js:item>null</js:item>"
+ "<js:item>-1.23E+4</js:item>"
+ "<js:item>\\a\"</js:item>"+
"</js:array>",
"[{\"a\":null},{\"x\":\"1\"},null,-1.23E+4,\"\\\\a\"\"\"]"));
		assertTrue(check(
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+   "<js:item>1</js:item>"
+   "<js:item>null</js:item>"
+   "<js:item>-1.23E+4</js:item>"
+   "<js:array>"
+     "<js:item>true</js:item>"
+     "<js:item>5</js:item>"
+   "</js:array>"
+   "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>",
"{\"a\":[1,null,-1.23E+4,[true,5],\"\\\\a\"\"\"]}"));
				assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<b>"
+     "<js:array>"
+       "<js:map>"
+         "<d>d</d>"
+         "<c>c</c>"
+         "<C D=\"d\"/>"
+       "</js:map>"
+     "</js:array>"
+   "</b>"
+ "</js:mapItems>"+
"</a>",
"{"
+ "\"a\":{"
+   "\"b\":["
+     "{"
+       "\"d\":\"d\","
+       "\"c\":\"c\","
+       "\"C\":{\"D\":\"d\"}"
+     "}"
+   "]"
+ "}"+
"}"));
		assertTrue(check(
"<Image Title=\"View\" Width=\"800\">"
	+ "<js:mapItems xmlns:js=\"http://www.syntea.cz/json/1.0\">"
	+   "<IDs>"
	+     "<js:array>"
	+       "<js:item>116</js:item>"
	+       "<js:item>38793</js:item>"
	+     "</js:array>"
	+   "</IDs>"
	+   "<Thumbnail Url=\"www\"/>"
	+ "</js:mapItems>"+
"</Image>",
"{\"Image\":"
+ "{"
+   "\"IDs\":[116,38793],"
+   "\"Width\":800,"
+   "\"Thumbnail\":{\"Url\":\"www\"},"
+ "\"Title\":\"View\""
+ "}"+
"}"));
		assertTrue(check(
"<glossary title=\"example glossary\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<GlossDiv title=\"S\">"
+     "<js:mapItems>"
+       "<GlossList>"
+         "<js:mapItems>"
+           "<GlossEntry GlossSee=\"markup\" ID=\"SGML\">"
+             "<js:mapItems>"
+               "<GlossDef para=\"A meta-markup language as DocBook.\">"
+                 "<js:mapItems>"
+                   "<GlossSeeAlso>"
+                     "<js:array>"
+                       "<js:item>GML</js:item>"
+                       "<js:item>XML</js:item>"
+                     "</js:array>"
+                   "</GlossSeeAlso>"
+                 "</js:mapItems>"
+               "</GlossDef>"
+             "</js:mapItems>"
+           "</GlossEntry>"
+         "</js:mapItems>"
+       "</GlossList>"
+     "</js:mapItems>"
+   "</GlossDiv>"
+ "</js:mapItems>" +
"</glossary>",
"{"
+ "\"glossary\":{"
+   "\"title\":\"example glossary\","
+   "\"GlossDiv\":{"
+     "\"GlossList\":{"
+       "\"GlossEntry\":{"
+         "\"GlossDef\":{"
+           "\"GlossSeeAlso\":[\"GML\",\"XML\"],"
+           "\"para\":\"A meta-markup language as DocBook.\""
+         "},"
+         "\"GlossSee\":\"markup\","
+         "\"ID\":\"SGML\""
+       "}"
+     "},"
+     "\"title\":\"S\""
+   "}"
+ "}"+
"}"));
		assertTrue(check(
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<t t-l=\"/W\" t-u=\"c\"/>"
+ "<b>"
+ "<js:array>"
+ "<js:map>"
+ "<b-n>b</b-n>"
+ "<i-p c:a=\"null\" c:s=\"/c\" t=\"o\" xmlns:c=\"some.name\"/>"
+ "<b-c>o</b-c>"
+ "</js:map>"
+ "<js:map>"
+ "<s-c>e</s-c>"
+ "<i-p m=\"m\" mH=\"m2\"/>"
+ "<s-n>c</s-n>"
+ "</js:map>"
+ "<js:map>"
+ "<s-c>x</s-c>"
+ "<s-n>d</s-n>"
+ "</js:map>"
+ "<js:map>"
+ "<s-c>t</s-c>"
+ "<i-p d=\"1\" l=\"1\"/>"
+ "<s-n>o</s-n>"
+ "</js:map>"
+ "</js:array>"
+ "</b>"
+ "<s-m c=\"/\" f=\"/t/*\"/>"
+ "</js:mapItems>"+
"</a>",
"{"
+ "\"a\":{"
+   "\"t\":{"
+     "\"t-u\":\"c\","
+     "\"t-l\":\"/W\""
+   "},"
+   "\"b\":["
+     "{"
+       "\"b-n\":\"b\","
+       "\"i-p\":{"
+         "\"xmlns:c\":\"some.name\","
+         "\"t\":\"o\","
+         "\"c:a\":null,"
+         "\"c:s\":\"/c\""
+       "},"
+       "\"b-c\":\"o\""
+     "},"
+     "{"
+       "\"s-c\":\"e\","
+       "\"i-p\":{"
+         "\"mH\":\"m2\","
+         "\"m\":\"m\""
+       "},"
+       "\"s-n\":\"c\""
+     "},"
+     "{"
+       "\"s-c\":\"x\","
+       "\"s-n\":\"d\""
+     "},"
+     "{"
+       "\"s-c\":\"t\","
+       "\"i-p\":{"
+         "\"d\":1,"
+         "\"l\":1"
+       "},"
+       "\"s-n\":\"o\""
+     "}"
+   "],"
+   "\"s-m\":{"
+     "\"f\":\"/t/*\","
+     "\"c\":\"/\""
+   "}"
+ "}"+
"}"));
		assertTrue(check(
"<web-app>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<servlet-mapping"
+   " cofaxAdmin=\"/admin/*\""
+   " cofaxCDS=\"/\""
+   " cofaxEmail=\"/cofaxutil/aemail/*\""
+   " cofaxTools=\"/tools/*\""
+   " fileServlet=\"/static/*\"/>"
+   "<taglib"
+ " taglib-location=\"/WEB-INF/tlds/cofax.tld\""
+ " taglib-uri=\"cofax.tld\"/>"
+   "<servlet>"
+     "<js:array>"
+     "<js:map>"
+       "<init-param"
+       " cachePagesRefresh=\"10\""
+       " configGlossary:adminEmail=\"ksm@pobox.com\""
+       " configGlossary:staticPath=\"/content/static\""
+       " dataStoreUser=\"sa\""
+       " templateOverridePath=\"\""
+       " useDataStore=\"true\""
+       " useJSP=\"false\""
+       " xmlns:configGlossary=\"some.namespace\"/>"
+       "<servlet-class>org.cofax.cds.CDSServlet</servlet-class>"
+       "<servlet-name>cofaxCDS</servlet-name>"
+     "</js:map>"
+     "<js:map>"
+       "<init-param mailHost=\"mail1\" mailHostOverride=\"mail2\"/>"
+       "<servlet-class>org.cofax.cds.EmailServlet</servlet-class>"
+       "<servlet-name>cofaxEmail</servlet-name>"
+     "</js:map>"
+     "<js:map>"
+       "<servlet-class>org.cofax.cds.AdminServlet</servlet-class>"
+       "<servlet-name>cofaxAdmin</servlet-name>"
+     "</js:map>"
+     "<js:map>"
+       "<servlet-class>org.cofax.cds.FileServlet</servlet-class>"
+       "<servlet-name>fileServlet</servlet-name>"
+     "</js:map>"
+     "<js:map>"
+       "<init-param adminGroupID=\"4\""
+       " betaServer=\"true\""
+       " dataLog=\"1\""
+       " dataLogLocation=\"/usr/local/tomcat/logs/dataLog.log\""
+       " log=\"1\""
+       " logMaxSize=\"\""
+       " lookInContext=\"1\"/>"
+       "<servlet-class>org.cofax.cms.CofaxToolsServlet</servlet-class>"
+       "<servlet-name>cofaxTools</servlet-name>"
+     "</js:map>"
+   "</js:array>"
+ "</servlet>"
+ "</js:mapItems>" +
"</web-app>",
"{"
+ "\"web-app\":{"
+   "\"servlet-mapping\":{"
+     "\"fileServlet\":\"/static/*\","
+     "\"cofaxCDS\":\"/\","
+     "\"cofaxAdmin\":\"/admin/*\","
+     "\"cofaxEmail\":\"/cofaxutil/aemail/*\","
+     "\"cofaxTools\":\"/tools/*\""
+   "},"
+   "\"taglib\":{"
+     "\"taglib-uri\":\"cofax.tld\","
+     "\"taglib-location\":\"/WEB-INF/tlds/cofax.tld\""
+   "},"
+   "\"servlet\":["
+     "{"
+       "\"init-param\":{"
+         "\"xmlns:configGlossary\":\"some.namespace\","
+         "\"useJSP\":false,"
+         "\"useDataStore\":true,"
+         "\"configGlossary:adminEmail\":\"ksm@pobox.com\","
+         "\"cachePagesRefresh\":10,"
+         "\"templateOverridePath\":\"\","
+         "\"configGlossary:staticPath\":\"/content/static\","
+         "\"dataStoreUser\":\"sa\""
+       "},"
+       "\"servlet-class\":\"org.cofax.cds.CDSServlet\","
+       "\"servlet-name\":\"cofaxCDS\""
+     "},"
+     "{"
+       "\"init-param\":{"
+         "\"mailHostOverride\":\"mail2\","
+         "\"mailHost\":\"mail1\""
+       "},"
+       "\"servlet-class\":\"org.cofax.cds.EmailServlet\","
+       "\"servlet-name\":\"cofaxEmail\""
+     "},"
+     "{"
+       "\"servlet-class\":\"org.cofax.cds.AdminServlet\","
+       "\"servlet-name\":\"cofaxAdmin\""
+     "},"
+     "{"
+       "\"servlet-class\":\"org.cofax.cds.FileServlet\","
+       "\"servlet-name\":\"fileServlet\""
+     "},"
+     "{"
+        "\"init-param\":"
+       "{"
+          "\"logMaxSize\":\"\","
+          "\"dataLog\":1,"
+          "\"lookInContext\":1,"
+          "\"betaServer\":true,"
+          "\"log\":1,"
+          "\"adminGroupID\":4,"
+          "\"dataLogLocation\":\"/usr/local/tomcat/logs/dataLog.log\""
+       "},"
+       "\"servlet-class\":\"org.cofax.cms.CofaxToolsServlet\","
+       "\"servlet-name\":\"cofaxTools\""
+     "}"
+   "]"
+ "}"+
"}"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
