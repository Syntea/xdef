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

import cz.syntea.xdef.sys.STester;

/** Test JSON tools.
 * @author trojan
 */
public class TestJSON1 extends STester {

	public TestJSON1() {super();}

	public static boolean check(String json, String el) {
		return TestJSON.check(json, el);
	}

	@Override
	/** Run test and print error information. */
	public void test() {
/*xx*
		assertTrue(check("{\"a\":[{\"b\":null}, {\"c\":null}]}",
"<a>"
	+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
	+ "<b/><c/>"
	+ "</js:array>"+
"</a>"));
if(true) return;
/*xx*/
		assertTrue(check("[]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'/>"));
		assertTrue(check("{}",
"<js:map xmlns:js='http://www.syntea.cz/json/1.0'/>"));
		assertTrue(check("[0]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>0</js:item>"+
"</js:array>"));
		assertTrue(check("[[[]]]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:array><js:array/></js:array>"+
"</js:array>"));
		assertTrue(check("[[],[]]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:array/><js:array/>"+
"</js:array>"));
		assertTrue(check("{\"a\": null}", "<a/>"));
		assertTrue(check("[{\"a\": null}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'><a/></js:array>"));
		assertTrue(check("[0, 1]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>0</js:item>"
+ "<js:item>1</js:item>"+
"</js:array>"));
		assertTrue(check("[{\"a\":0}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a>0</a>"+
"</js:array>"));
		assertTrue(check("{\"a\":{\"x\":1}}", "<a x='1'/>"));
		assertTrue(check("{\"a\":1}", "<a>1</a>"));
		assertTrue(check("{\"a\":[\"\\\\a\\\"\"]}",
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+ "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("{\"a\":[{\"b\":null}, {\"c\":null}]}",
"<a>"
	+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
	+ "<b/><c/>"
	+ "</js:array>"+
"</a>"));
		assertTrue(check("{\"a\":[{\"b\":\"\tx\n\"},\"\tx\n\t\"]}",
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+   "<b>\"\tx\n\"</b>"
+   "<js:item>\"\tx\n\t\"</js:item>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("{\"\":1}", "<_u0_>1</_u0_>"));
		assertTrue(check("{\"\": null}", "<_u0_/>"));
		assertTrue(check("{\"a\":{\"a:b\":\"x\",\"xmlns:a\":\"x\"}}",
			"<a a:b=\"x\" xmlns:a=\"x\"/>"));
		assertTrue(check("[\"A\",{}, []]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>A</js:item>"
+ "<js:map/>"
+ "<js:array/>"+
"</js:array>"));
		assertTrue(check("[{\"a\":{\"x\":1}},\"x\"]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x='1'/>"
+ "<js:item>x</js:item>"+
"</js:array>"));
		assertTrue(check("{\"a&+\": null}", "<a_u26__u2b_/>"));
		assertTrue(check("{\"a\":[{},[]]}",
"<a>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:map/><js:array/></js:array>"+
"</a>"));
		assertTrue(check("{\"x\":[1]}",
"<x>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<js:item>1</js:item>"
+ "</js:array>"+
"</x>"));
		assertTrue(check("[{\"a\":null}, []]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a/>"
+ "<js:array/>"+
"</js:array>"));
		assertTrue(check("{\"a\":{\"b\":null}}", "<a b='null'></a>"));
		assertTrue(check("{\"a\":[\"\\\\a\"\"\"]}",
"<a>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("[{\"a\":{}}, []]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
	+ "<a><js:map/></a>"
	+ "<js:array/>"+
"</js:array>"));
		assertTrue(check("[{\"a\":{\"x\":1}},{\"b\":null}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/><b/>"+
"</js:array>"));
		assertTrue(check("[{\"a\":{\"x\":1}}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'><a x='1'/></js:array>"));
		assertTrue(check("{\"a\":1}", "<a>1</a>"));
		assertTrue(check("[{\"a\":1}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'><a>1</a></js:array>"));
		assertTrue(check("{\"a\":{\"x\":\"1\",\"y\":\"2\"}}",
			"<a x='\"1\"' y='\"2\"'/>"));
		assertTrue(check("[{\"a\":{\"b\":1,\"c\":2}},\"abc\"]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a b=\"1\" c=\"2\"/>"
+ "<js:item>abc</js:item>"+
"</js:array>"));
		assertTrue(check("{\"x\":[{\"a\":{\"b\":1,\"c\":2}},\"abc\"]}",
"<x>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<a b=\"1\" c=\"2\"/>"
+   "<js:item>abc</js:item>"
+ "</js:array>"+
"</x>"));
		assertTrue(check("{\"a\": [\"GM\", \"XM\"]}",
"<a><js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>GM</js:item>"
+ "<js:item>XM</js:item>"+
"</js:array></a>"));
		assertTrue(check("{\"a\":null}", "<a/>"));
		assertTrue(check("{\"_\":true}", "<_>true</_>"));
		assertTrue(check("{\"_u\":false}", "<_u>false</_u>"));
		assertTrue(check("{\"_u_\":null}",  "<_u_/>"));
		assertTrue(check("{\"_ux_\":-1.5e1}",  "<_ux_>-15</_ux_>"));
		assertTrue(check("{\"_uA_\":1}",  "<_u5f_uA_>1</_u5f_uA_>"));
		assertTrue(check("{\"_ua_\":1}",  "<_u5f_ua_>1</_u5f_ua_>"));
		assertTrue(check("{\" \": {\" \": 1}}",  "<_u20_ _u20_=\"1\"/>"));
		assertTrue(check("{\"_u1234_\":1}",  "<_u5f_u1234_>1</_u5f_u1234_>"));
		assertTrue(check("{\"_u12345_\":1}",  "<_u12345_>1</_u12345_>"));
		assertTrue(check("{\"A:B\":1}",  "<A_u3a_B>1</A_u3a_B>"));
		assertTrue(check("{\"Ahoj \nNazdar\":1}",
			"<Ahoj_u20__ua_Nazdar>1</Ahoj_u20__ua_Nazdar>"));
		assertTrue(check("{\"<A?*,'\":1}",
			"<_u3c_A_u3f__u2a__u2c__u27_>1</_u3c_A_u3f__u2a__u2c__u27_>"));
		assertTrue(check("{\" \": {\" \": 1}}",  "<_u20_ _u20_=\"1\"/>"));
		assertTrue(check("[{\"a\": null}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'><a/></js:array>"));
		assertTrue(check("{\"a\":[{},[]]}",
"<a>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<js:map/><js:array/>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("{ \"a\":{\"x\":1}}", "<a x=\"1\"/>"));
		assertTrue(check("{\"a\":{\"'x\":1,\"y\":2}}","<a _u27_x='1' y='2'/>"));
		assertTrue(check("[{\"a\":null},{\"x\":1},null,-1.23E+4,\"\\\\a\\\"\"]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a/>"
+ "<x>1</x>"
+ "<js:item>null</js:item>"
+ "<js:item>-1.23E+4</js:item>"
+ "<js:item>\\a\"</js:item>"+
"</js:array>"));
		assertTrue(check("{\"a\":[{\"x\":\"1\"},null, -1.23E+4, \"\\\\a\\\"\"]}",
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+   "<x>\"1\"</x>"
+   "<js:item>null</js:item>"
+   "<js:item>-1.23E+4</js:item>"
+   "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("{\"a\":[{\"x\":1}, null, -1230.0, \"\\\\a\\\"\"]}",
"<a>"
+ "<js:array xmlns:js=\"http://www.syntea.cz/json/1.0\">"
+   "<x>1</x>"
+   "<js:item>null</js:item>"
+   "<js:item>-1230.0</js:item>"
+   "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>"));
		assertTrue(check("[\"A\", { \"B\": 1}, \"x\" ]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:item>A</js:item>"
+ "<B>1</B>"
+ "<js:item>x</js:item>"+
"</js:array>"));
		assertTrue(check(
"{\n" +
"    \"glossary\": {\n" +
"       \"title\": \"example glossary\",\n" +
"		\"GlossDiv\": {\n" +
"           \"title\": \"S\",\n" +
"			\"GlossList\": {\n" +
"                \"GlossEntry\": {\n" +
"                   \"ID\": \"SGML\",\n" +
"					\"GlossDef\": {\n" +
"                       \"para\": \"A meta-markup language as DocBook.\",\n" +
"						\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
"                    },\n" +
"					\"GlossSee\": \"markup\"\n" +
"                }\n" +
"            }\n" +
"        }\n" +
"    }\n" +
"}",
"<glossary title=\"example glossary\">"
+ "<js:mapItems xmlns:js=\"http://www.syntea.cz/json/1.0\">"
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
+ "</js:mapItems>"+
"</glossary>"));
		assertTrue(check(
"{\"Image\": {\n"+
"     \"Width\":  800,\n"+
"     \"Title\":  \"View\",\n"+
"     \"Thumbnail\": {\n"+
"         \"Url\":   \"www\"\n"+
"     },\n"+
"     \"IDs\": [116, 38793]\n"+
"  }\n"+
"}",
"<Image Title=\"View\" Width=\"800\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<IDs>"
+   "<js:array>"
+     "<js:item>116</js:item>"
+     "<js:item>38793</js:item>"
+   "</js:array>"
+ "</IDs>"
+ "<Thumbnail Url=\"www\"/>"
+ "</js:mapItems>"+
"</Image>"));
		assertTrue(check(
"[\n"+
"  {\n"+
"    \"State\":   [\"CA\",\"CZ\",\"US\"],\n"+
"    \"Country\": \"US\"\n"+
"  },\n"+
"  {\n"+
"    \"State\":   \"CA\",\n"+
"    \"Country\": \"US\"\n"+
"   }\n"+
"]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<js:map>"
+   "<State>"
+     "<js:array>"
+       "<js:item>CA</js:item>"
+       "<js:item>CZ</js:item>"
+       "<js:item>US</js:item>"
+     "</js:array>"
+   "</State>"
+   "<Country>US</Country>"
+ "</js:map>"
+ "<js:map><State>CA</State><Country>US</Country></js:map>"+
"</js:array>"));
		assertTrue(check(
"{\"breakfast_menu\": {\"food\": [\n" +
"  {\n" +
"    \"price\": \"$5.95\",\n" +
"    \"description\": \"Two of our famous Belgian Waffles\",\n" +
"    \"name\": \"Belgian Waffles\",\n" +
"    \"calories\": 650\n" +
"  },\n" +
"  {\n" +
"    \"price\": \"$7.95\",\n" +
"    \"description\": \"Light Belgian waffles\",\n" +
"    \"name\": \"Strawberry Belgian Waffles\",\n" +
"    \"calories\": 900\n" +
"  }\n" +
"]}}",
"<breakfast_menu>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<food>"
+     "<js:array>"
+       "<js:map>"
+         "<price>$5.95</price>"
+         "<description>\"Two of our famous Belgian Waffles\"</description>"
+         "<name>\"Belgian Waffles\"</name>"
+         "<calories>650</calories>"
+       "</js:map>"
+       "<js:map>"
+         "<price>$7.95</price>"
+         "<description>\"Light Belgian waffles\"</description>"
+         "<name>\"Strawberry Belgian Waffles\"</name>"
+         "<calories>900</calories>"
+       "</js:map>"
+     "</js:array>"
+   "</food>"
+ "</js:mapItems>"+
"</breakfast_menu>"));
		assertTrue(check(
"{\"menu\": {\n" +
"  \"id\": \"file\",\n" +
"  \"value\": \"File\",\n" +
"  \"popup\": {\n" +
"    \"menuitem\": [\n" +
"      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n" +
"      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n" +
"      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" +
"    ]\n" +
"  }\n" +
"}}",
"<menu id=\"file\" value=\"File\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<popup>"
+   "<js:mapItems>"
+     "<menuitem>"
+       "<js:array>"
+         "<js:map>"
+           "<value>New</value>"
+           "<onclick>CreateNewDoc()</onclick>"
+         "</js:map>"
+         "<js:map>"
+           "<value>Open</value>"
+           "<onclick>OpenDoc()</onclick>"
+         "</js:map>"
+         "<js:map><value>Close</value>"
+         "<onclick>CloseDoc()</onclick>"
+         "</js:map>"
+       "</js:array>"
+     "</menuitem>"
+   "</js:mapItems>"
+ "</popup>"
+ "</js:mapItems>"+
"</menu>"));
		assertTrue(check(
"{\"menu\":\n"+
"  {\n" +
"    \"header\": \"SVG Viewer\",\n" +
"    \"items\": [\n" +
"        {\"id\": \"Open\"},\n" +
"        {\"id\": \"OpenNew\", \"label\": \"Open New\"},\n" +
"        null,\n" +
"        {\"id\": \"ZoomIn\", \"label\": \"Zoom In\"},\n" +
"        {\"id\": \"ZoomOut\", \"label\": \"Zoom Out\"},\n" +
"        {\"id\": \"OriginalView\", \"label\": \"Original View\"},\n" +
"        null,\n" +
"        {\"id\": \"Quality\"},\n" +
"        {\"id\": \"Pause\"},\n" +
"        {\"id\": \"Mute\"},\n" +
"        null,\n" +
"        {\"id\": \"Find\", \"label\": \"Find...\"},\n" +
"        {\"id\": \"Copy\"},\n" +
"        {\"id\": \"ViewSource\", \"label\": \"View Source\"},\n" +
"        {\"id\": \"SaveAs\", \"label\": \"Save As\"},\n" +
"        null,\n" +
"        {\"id\": \"Help\"},\n" +
"        {\"id\": \"About\", \"label\": \"About CVG Viewer...\"}\n" +
"    ]\n" +
"  }\n"+
"}",
"<menu header=\"SVG Viewer\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<items>"
+     "<js:array>"
+       "<id>Open</id>"
+       "<js:map><id>OpenNew</id><label>\"Open New\"</label></js:map>"
+       "<js:item>null</js:item>"
+       "<js:map><id>ZoomIn</id><label>\"Zoom In\"</label></js:map>"
+       "<js:map><id>ZoomOut</id><label>\"Zoom Out\"</label></js:map>"
+       "<js:map><id>OriginalView</id><label>\"Original View\"</label>"
+       "</js:map>"
+       "<js:item>null</js:item>"
+       "<id>Quality</id>"
+       "<id>Pause</id>"
+       "<id>Mute</id>"
+       "<js:item>null</js:item>"
+       "<js:map><id>Find</id><label>Find...</label></js:map>"
+       "<id>Copy</id>"
+       "<js:map><id>ViewSource</id><label>\"View Source\"</label></js:map>"
+       "<js:map><id>SaveAs</id><label>\"Save As\"</label></js:map>"
+       "<js:item>null</js:item>"
+       "<id>Help</id>"
+       "<js:map><id>About</id><label>\"About CVG Viewer...\"</label></js:map>"
+     "</js:array>"
+   "</items>"
+ "</js:mapItems>"+
"</menu>"));
		assertTrue(check("[{\"a\":null},{\"x\":\"1\"},null,-1.23E+4,\"\\\\a\\\"\"]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a/>"
+ "<x>\"1\"</x>"
+ "<js:item>null</js:item>"
+ "<js:item>-1.23E+4</js:item>"
+ "<js:item>\\a\"</js:item>"+
"</js:array>"));
		assertTrue(check("{\"a\":[1,null,-1.23E+4,[true, 5]\"\\\\a\\\"\"]}",
"<a>"
+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<js:item>1</js:item>"
+   "<js:item>null</js:item>"
+   "<js:item>-1.23E+4</js:item>"
+   "<js:array>"
+     "<js:item>true</js:item>"
+     "<js:item>5</js:item>"
+   "</js:array>"
+   "<js:item>\\a\"</js:item>"
+ "</js:array>"+
"</a>"));
				assertTrue(check(
"{\"a\": {\n" +
"  \"b\": [\n" +
"    {\n" +
"      \"c\": \"c\",\n" +
"      \"d\": \"d\",\n" +
"      \"C\": {\n" +
"        \"D\": \"d\"\n" +
"      }\n" +
"    }\n" +
"  ]\n" +
" }\n" +
"}",
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
"</a>"));
		assertTrue(check(
"{\"A\": {\n" +
"  \"x\": null,\n" +
"  \"y\": 1,\n" +
"  \"z\": false,\n" +
"  \"a\": [\n" +
"    {\"b\": \"B\",\"c\": {\"C\": 5}},\n" +
"    {\"b\": \"B1\",\"c\": {\"C\": \"C2\"}},\n" +
"    {\"d\": \"d\"},\n" +
"    {\"b\": {\"c\": true}}\n" +
"  ],\n" +
"  \"g\": {\n" +
"    \"h\": \"/\",\n" +
"    \"i\": \"/i/*\"\n" +
"  },\n" +
"  \"j\": {\n" +
"    \"k\": \"/K\"\n" +
"   }\n" +
"  }\n" +
"}",
"<A x=\"null\" y=\"1\" z=\"false\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<g h=\"/\" i=\"/i/*\"/>"
+   "<a>"
+     "<js:array>"
+       "<js:map><b>B</b><c C=\"5\"/></js:map>"
+       "<js:map><b>B1</b><c C=\"C2\"/></js:map>"
+       "<d>d</d>"
+       "<b c=\"true\"/>"
+     "</js:array>"
+   "</a>"
+   "<j k=\"/K\"/>"
+ "</js:mapItems>"+
"</A>"));
		assertTrue(check("[{\"a\":{\"x\":1}},{\"b\":{\"y\":\"\"}}]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\"/><b y=\"\"/>"+
"</js:array>"));
		assertTrue(check(
"["
+ "{\"a\":{\"y\":true,\"x\":1}},"
+ "["
+   "{\"b\":{\"y\":\"x\"}},"
+   "1"
+   "],"
+   "["
+     "{\"c\":{\"y\":\"y\"}},"
+     "2"
+   "]"
+ "]",
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>"
+ "<a x=\"1\" y=\"true\"/>"
+ "<js:array><b y=\"x\"/>"
+   "<js:item>1</js:item>"
+ "</js:array>"
+ "<js:array>"
+   "<c y=\"y\"/>"
+   "<js:item>2</js:item>"
+ "</js:array>"+
"</js:array>"));
		assertTrue(check(
"{\"a\":{\"b\":{\"d\":{\"e\":5},\"c\":\"C\"}}}",
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<b c=\"C\">"
+       "<js:mapItems>"
+         "<d e='5'/>"
+       "</js:mapItems>"
+   "</b>"
+ "</js:mapItems>" +
"</a>"));
		assertTrue(check(
"{\"a\":{\"b\":[{\"d\":{\"e\":5},\"c\":\"C\"}]}}",
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
"</a>"));
		assertTrue(check(
"{"
+ "\"A\":{"
+   "\"g\":{"
+     "\"h\":\"/\","
+     "\"i\":\"/i/*\""
+   "},"
+   "\"a\":["
+     "{"
+       "\"b\":\"B\","
+       "\"c\":{\"C\":5}"
+     "},"
+     "{"
+       "\"b\":\"B1\","
+       "\"c\":{\"C\":\"C2\"}"
+     "},"
+     "{\"d\":[\"d\"]},"
+     "{"
+       "\"b\":{\"c\":true}"
+     "}"
+   "],"
+   "\"j\":{\"k\":\"/K\"},"
+   "\"z\":false,"
+   "\"y\":1,"
+   "\"x\":null"
+ "}" +
"}",
"<A x=\"null\" y=\"1\" z=\"false\">"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<g h=\"/\" i=\"/i/*\"/>"
+   "<a>"
+     "<js:array>"
+       "<js:map>"
+         "<b>B</b><c C=\"5\"/>"
+       "</js:map>"
+       "<js:map>"
+         "<b>B1</b>"
+         "<c C=\"C2\"/>"
+       "</js:map>"
+       "<d>"
+         "<js:array>"
+           "<js:item>d</js:item>"
+         "</js:array>"
+       "</d>"
+       "<b c=\"true\"/>"
+     "</js:array>"
+   "</a>"
+   "<j k=\"/K\"/>"
+ "</js:mapItems>"+
"</A>"));
		assertTrue(check(
"{\"a\": {\n" +
"  \"b\": [\n" +
"    {\n" +
"      \"b-n\": \"b\",\n" +
"	   \"b-c\": \"o\",\n" +
"      \"i-p\": {\n" +
"        \"xmlns:c\": \"some.name\",\n" +
"        \"c:a\": null,\n" +
"        \"c:s\": \"/c\",\n" +
"        \"t\": \"o\"\n" +
"      }\n"+
"    },\n" +
"    {\n" +
"      \"s-n\": \"c\",\n" +
"      \"s-c\": \"e\",\n" +
"      \"i-p\": {\n" +
"        \"m\": \"m\",\n" +
"        \"mH\": \"m2\"}\n"+
"    },\n" +
"    {\n" +
"      \"s-n\": \"d\",\n" +
"      \"s-c\": \"x\"\n"+
"    },\n" +
"    {\n" +
"      \"s-n\": \"o\",\n" +
"      \"s-c\": \"t\",\n" +
"      \"i-p\": {\n" +
"        \"l\": 1,\n" +
"        \"d\": 1\n" +
"      }"+
"    }\n"+
"  ],\n" +
"  \"s-m\": {\n" +
"    \"c\": \"/\",\n" +
"    \"f\": \"/t/*\"\n"+
"  },\n" +
"  \"t\": {\n" +
"    \"t-u\": \"c\",\n" +
"    \"t-l\": \"/W\"}\n"+
"  }"+
"}",
"<a>"
+ "<js:mapItems xmlns:js='http://www.syntea.cz/json/1.0'>"
+   "<t t-l=\"/W\" t-u=\"c\"/>"
+   "<b>"
+     "<js:array>"
+       "<js:map>"
+         "<b-n>b</b-n>"
+         "<i-p c:a=\"null\" c:s=\"/c\" t=\"o\" xmlns:c=\"some.name\"/>"
+         "<b-c>o</b-c>"
+       "</js:map>"
+       "<js:map>"
+         "<s-c>e</s-c>"
+          "<i-p m=\"m\" mH=\"m2\"/>"
+          "<s-n>c</s-n>"
+       "</js:map>"
+       "<js:map>"
+         "<s-c>x</s-c>"
+         "<s-n>d</s-n>"
+       "</js:map>"
+       "<js:map>"
+         "<s-c>t</s-c>"
+           "<i-p d=\"1\" l=\"1\"/>"
+           "<s-n>o</s-n>"
+       "</js:map>"
+     "</js:array>"
+   "</b>"
+   "<s-m c=\"/\" f=\"/t/*\"/>"
+ "</js:mapItems>"+
"</a>"));
		assertTrue(check(
"{\"A\": {\n" +
"  \"a\": [\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxCDS\",\n" +
"      \"init-param\": {\"maxUrlLength\": 500}},\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxEmail\",\n" +
"      \"init-param\": {\"mailHostOverride\": \"mail2\"}},\n" +
"    {\"servlet-class\": \"FileServlet\"},\n" +
"    {\n" +
"      \"init-param\": {\n" +
"        \"betaServer\": true}}],\n" +
"  \"servlet-mapping\": {\n" +
"    \"cofaxCDS\": \"/\",\n" +
"    \"cofaxTools\": \"/tools/*\"},\n" +
"  \"taglib\": {\n" +
"    \"taglib-location\": \"/WEB-INF/tlds/cofax.tld\"}}}",
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
"</A>"));
		assertTrue(check(
"{\"web-app\": {\n" +
"  \"servlet\": [\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxCDS\",\n" +
"      \"servlet-class\": \"org.cofax.cds.CDSServlet\",\n" +
"      \"init-param\": {\n" +
"        \"xmlns:configGlossary\": \"some.namespace\",\n" +
"        \"configGlossary:adminEmail\": \"ksm@pobox.com\",\n" +
"        \"configGlossary:staticPath\": \"/content/static\",\n" +
"        \"templateProcessorClass\": \"org.cofax.WysiwygTemplate\",\n" +
"        \"templateLoaderClass\": \"org.cofax.FilesTemplateLoader\",\n" +
"        \"templatePath\": \"templates\",\n" +
"        \"templateOverridePath\": \"\",\n" +
"        \"useJSP\": false,\n" +
"        \"cachePagesRefresh\": 10,\n" +
"        \"searchEngineRobotsDb\": \"WEB-INF/robots.db\",\n" +
"        \"useDataStore\": true,\n" +
"        \"dataStoreUser\": \"sa\",\n" +
"        \"dataStoreInitConns\": 10,\n" +
"        \"dataStoreMaxConns\": 100,\n" +
"        \"maxUrlLength\": 500}},\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxEmail\",\n" +
"      \"servlet-class\": \"org.cofax.cds.EmailServlet\",\n" +
"      \"init-param\": {\n" +
"      \"mailHost\": \"mail1\",\n" +
"      \"mailHostOverride\": \"mail2\"}},\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxAdmin\",\n" +
"      \"servlet-class\": \"org.cofax.cds.AdminServlet\"},\n" +
"    {\n" +
"      \"servlet-name\": \"fileServlet\",\n" +
"      \"servlet-class\": \"org.cofax.cds.FileServlet\"},\n" +
"    {\n" +
"      \"servlet-name\": \"cofaxTools\",\n" +
"      \"servlet-class\": \"org.cofax.cms.CofaxToolsServlet\",\n" +
"      \"init-param\": {\n" +
"        \"log\": 1,\n" +
"        \"logMaxSize\": \"\",\n" +
"        \"adminGroupID\": 4,\n" +
"        \"betaServer\": true}}],\n" +
"  \"servlet-mapping\": {\n" +
"    \"cofaxCDS\": \"/\",\n" +
"    \"cofaxEmail\": \"/cofaxutil/aemail/*\",\n" +
"    \"cofaxAdmin\": \"/admin/*\",\n" +
"    \"fileServlet\": \"/static/*\",\n" +
"    \"cofaxTools\": \"/tools/*\"},\n" +
"  \"taglib\": {\n" +
"    \"taglib-uri\": \"cofax.tld\",\n" +
"    \"taglib-location\": \"/WEB-INF/tlds/cofax.tld\"}}}",
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
+       "<js:map>"
+         "<init-param"
+         " cachePagesRefresh=\"10\""
+         " configGlossary:adminEmail=\"ksm@pobox.com\""
+         " configGlossary:staticPath=\"/content/static\""
+         " dataStoreInitConns=\"10\""
+         " dataStoreMaxConns=\"100\""
+         " dataStoreUser=\"sa\""
+         " maxUrlLength=\"500\""
+         " searchEngineRobotsDb=\"WEB-INF/robots.db\""
+         " templateLoaderClass=\"org.cofax.FilesTemplateLoader\""
+         " templateOverridePath=\"\""
+         " templatePath=\"templates\""
+         " templateProcessorClass=\"org.cofax.WysiwygTemplate\""
+         " useDataStore=\"true\""
+         " useJSP=\"false\""
+         " xmlns:configGlossary=\"some.namespace\"/>"
+         "<servlet-class>org.cofax.cds.CDSServlet</servlet-class>"
+         "<servlet-name>cofaxCDS</servlet-name>"
+       "</js:map>"
+       "<js:map>"
+         "<init-param mailHost=\"mail1\" mailHostOverride=\"mail2\"/>"
+         "<servlet-class>org.cofax.cds.EmailServlet</servlet-class>"
+         "<servlet-name>cofaxEmail</servlet-name>"
+       "</js:map>"
+       "<js:map>"
+         "<servlet-class>org.cofax.cds.AdminServlet</servlet-class>"
+         "<servlet-name>cofaxAdmin</servlet-name>"
+       "</js:map>"
+       "<js:map>"
+         "<servlet-class>org.cofax.cds.FileServlet</servlet-class>"
+         "<servlet-name>fileServlet</servlet-name>"
+       "</js:map>"
+       "<js:map>"
+         "<init-param adminGroupID=\"4\""
+         " betaServer=\"true\""
+         " log=\"1\""
+         " logMaxSize=\"\"/>"
+         "<servlet-class>org.cofax.cms.CofaxToolsServlet</servlet-class>"
+         "<servlet-name>cofaxTools</servlet-name>"
+       "</js:map>"
+     "</js:array>"
+   "</servlet>"
+ "</js:mapItems>" +
"</web-app>"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
