/*
 * File: TestJSON.java
 * Copyright 2016 Syntea.
 *
 * This file may be copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt kopirovan, modifikovan a siren pouze v souladu4
 * s textem prilozeneho souboru LICENCE.TXT, ktery obsahuje specifikaci
 * prislusnych prav.
 */
package test.xdef;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.JSONUtil;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.component.XJUtil;
import cz.syntea.xdef.XDPool;
import org.w3c.dom.Element;

/** Test of parsing of JSON objects with XDefinition.
 * @author Vaclav Trojan
 */
public class TestJSON extends Tester {

	public TestJSON() {super();}

	private boolean check(final String source) {
		String xdef = null;
		String xml = null;
		try {
			boolean result = true;
			if (source.charAt(0) == '<') {
				Element el1 = KXmlUtils.parseXml(source).getDocumentElement();
				Object j1 = XJUtil.xmlToJson(el1);
				xdef = KXmlUtils.nodeToString(XJUtil.jsonToXDef(j1), true);
				XDPool xp = compile(xdef);
				Element el2 = XJUtil.jsonToXml(j1);
				Object j2 = XJUtil.xmlToJson(el2);
				if (!XJUtil.jsonEqual(j1, j2)) {
					System.err.println("JSON objects are not equal:");
					System.err.println(XJUtil.toJSONString(j1, true));
					System.err.println(XJUtil.toJSONString(j2, true));
					result = false;
				}
				ArrayReporter reporter = new ArrayReporter();
				parse(xp, "", el2, reporter);
				if (reporter.errorWarnings()) {
					System.err.println("Element fails in XDefinition:");
					if (result) {
						System.err.println(xml);
					}
					System.err.println(xdef);
					System.err.println(reporter.printToString());
					result = false;
				}
			} else {
				Object json =  JSONUtil.parseJSON(source);
				Element e1 = JSONUtil.jsonToXml(json);
				xml = KXmlUtils.nodeToString(e1);
				xdef = KXmlUtils.nodeToString(XJUtil.jsonToXDef(json), true);
				XDPool xp = compile(xdef);
				ArrayReporter reporter = new ArrayReporter();
				Element e2 = parse(xp, "", xml, reporter);
				result = true;
				if (KXmlUtils.compareElements(e1, e2).errorWarnings()) {
					System.err.println("Elements are not equal:");
					System.err.println(xml);
					System.err.println(KXmlUtils.nodeToString(e2));
					result = false;
				}
				if (reporter.errorWarnings()) {
					System.err.println("Element fails in XDefinition:");
					if (result) {
						System.err.println(xml);
					}
					System.err.println(xdef);
					System.err.println(reporter.printToString());
					result = false;
				}
				if (!JSONUtil.jsonEqual(json, JSONUtil.xmlToJson(e2))) {
					System.err.println("Json created from xml differs");
					System.err.println(source);
					System.err.println(
						JSONUtil.toJSONString(JSONUtil.xmlToJson(e2), true));
					result = false;
				}
			}
			return result;
		} catch (Exception ex) {
			System.err.println(source);
			System.err.println(xdef);
			System.err.println(xml);
			ex.printStackTrace(System.err);
			return false;
		}
	}
	@Override
	/** Run test and print error information. */
	public void test() {
		setProperty("xdef.warnings", "true");
		String xml;
		String xdef;
		XDPool xp;
		Object js;
		Element el;
		ArrayReporter reporter = new ArrayReporter();
/*xx*
		assertTrue(check("{}"));
if (true) return;
/*xx*/
		try {
			xdef =
"<xd:def root=\"js:array\" name = 'json'\n"+
"  xmlns:js=\"http://www.syntea.cz/json/1.0\"\n"+
"  xmlns:xd='" + XDEFNS + "'>\n"+
"  <js:array>\n"+
"    sequence(%item=[jvalue(),jvalue()])\n"+
"  </js:array>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H M</js:array>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			js = XJUtil.xmlToJson(el);
			assertTrue(check(XJUtil.toJSONString(js)));
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H</js:array>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H M X</js:array>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xdef =
"<xd:def root=\"js:array\" name = 'json'\n"+
"  xmlns:js=\"http://www.syntea.cz/json/1.0\"\n"+
"  xmlns:xd='" + XDEFNS + "'>\n"+
"  <js:array>\n"+
"    xs:list(%length=2,%item=jvalue())\n"+
"  </js:array>\n"+
"</xd:def>";
			xp = compile(xdef);
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H M</js:array>";
			el = parse(xp, "", xml, reporter);
			assertNoErrors(reporter);
			assertEq(xml, el);
			js = XJUtil.xmlToJson(el);
			assertTrue(check(XJUtil.toJSONString(js)));
			xdef = KXmlUtils.nodeToString(XJUtil.jsonToXDef(js), true);
			xp = compile(xdef);
			el = parse(xp, "", XJUtil.jsonToXml(js), reporter);
			assertNoErrors(reporter);
			assertEq(XJUtil.jsonToXml(js), el);
			assertTrue(check(xml));
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H</js:array>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
			xml =
"<js:array xmlns:js='http://www.syntea.cz/json/1.0'>H M X</js:array>";
			parse(xp, "", xml, reporter);
			assertErrors(reporter);
		} catch (Exception ex) {fail(ex);}
		assertTrue(check("[]"));
		assertTrue(check("{}"));
		assertTrue(check("[[[]]]"));
		assertTrue(check("[0]"));
		assertTrue(check("[\"H\" \"M\"]"));
		assertTrue(check("{\"a\":null}"));
		assertTrue(check("{\"a\":1}"));
		assertTrue(check("{\"a\":{\"z\":null,\"y\":true,\"x\":1}}"));
		assertTrue(check("[123, false, null, \"\", \"ab cd\", \"x\"]"));
		assertTrue(check("[\"H\",\"M\"]"));
		assertTrue(check("{\"a\":[\"\\\\a\\\"\"]}"));
		assertTrue(check(
"[{\"a\":null},{\"x\":\"1\"},null,-1.23E+4,\"\\\\a\\\"\"]"));
		assertTrue(check("{\"a\":[{\"b\":\"\tx\n\"},\"\tx\n\t\"]}"));
		assertTrue(check(
"{\n"+
"  \"Image\" : {\n"+
"    \"Width\" : 800,\n"+
"    \"Title\" : \"View\",\n"+
"    \"Thumbnail\" : {\"Url\": \"www\"},\n"+
"    \"IDs\" : [116, 38793, \"x\"]\n"+
"  }\n"+
"}"));
		assertTrue(check("{\"a\":{\"a:b\":\"x\",\"xmlns:a\":\"x\"}}"));
		assertTrue(check(
"{\n"+
"   \"glossary\": {\n"+
"      \"title\": \"example glossary\",\n"+
"      \"GlossDiv\": {\n"+
"         \"title\": \"S\",\n"+
"         \"GlossList\": {\n"+
"            \"GlossEntry\": {\n"+
"               \"ID\": \"SGML\",\n"+
"               \"GlossDef\": {\n"+
"                  \"para\": \"A meta-markup language as DocBook.\",\n"+
"                  \"GlossSeeAlso\": [\"GML\", \"XML\"]\n"+
"               },\n"+
"               \"GlossSee\": \"markup\"\n"+
"            }\n"+
"         }\n"+
"      }\n"+
"   }\n"+
"}"));
		assertTrue(check(
"{\n"+
"  \"menu\": {\n"+
"    \"id\": \"file\",\n"+
"    \"value\": \"File\",\n"+
"    \"popup\": {\n"+
"      \"menuitem\": [\n"+
"        {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n"+
"        {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n"+
"        {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n"+
"      ]\n"+
"    }\n"+
"  }\n"+
"}"));
		assertTrue(check(
"{\n"+
"  \"menu\": {\n"+
"    \"header\": \"SVG Viewer\",\n"+
"    \"items\": [\n"+
"      {\"id\": \"Open\"},\n"+
"      {\"id\": \"OpenNew\", \"label\": \"Open New\"},\n"+
"      null,\n"+
"      {\"id\": \"ZoomIn\", \"label\": \"Zoom In\"},\n"+
"      {\"id\": \"ZoomOut\", \"label\": \"Zoom Out\"},\n"+
"      {\"id\": \"OriginalView\", \"label\": \"Original View\"},\n"+
"      null,\n"+
"      {\"id\": \"Quality\"},\n"+
"      {\"id\": \"Pause\"},\n"+
"      {\"id\": \"Mute\"},\n"+
"      null,\n"+
"      {\"id\": \"Find\", \"label\": \"Find...\"},\n"+
"      {\"id\": \"Copy\"},\n"+
"      {\"id\": \"ViewSource\", \"label\": \"View Source\"},\n"+
"      {\"id\": \"SaveAs\", \"label\": \"Save As\"},\n"+
"      null,\n"+
"      {\"id\": \"Help\"},\n"+
"      {\"id\": \"About\", \"label\": \"About CVG Viewer...\"}\n"+
"    ]\n"+
"  }\n"+
"}"));
		assertTrue(check(
"{\n"+
"  \"web-app\": {\n"+
"    \"servlet\": [\n"+
"      {\n"+
"        \"servlet-name\": \"cofaxCDS\",\n"+
"        \"servlet-class\": \"org.cofax.cds.CDSServlet\",\n"+
"        \"init-param\": {\n"+
"          \"xmlns:configGlossary\": \"some.namespace\",\n"+
"          \"configGlossary:adminEmail\": \"ksm@pobox.com\",\n"+
"          \"configGlossary:staticPath\": \"/content/static\",\n"+
"          \"templateProcessorClass\": \"org.cofax.WysiwygTemplate\",\n"+
"          \"templateLoaderClass\": \"org.cofax.FilesTemplateLoader\",\n"+
"          \"templatePath\": \"templates\",\n"+
"          \"templateOverridePath\": \"\",\n"+
"          \"useJSP\": false,\n"+
"          \"cachePagesRefresh\": 10,\n"+
"          \"searchEngineRobotsDb\": \"WEB-INF/robots.db\",\n"+
"          \"useDataStore\": true,\n"+
"          \"dataStoreUser\": \"sa\",\n"+
"          \"dataStoreInitConns\": 10,\n"+
"          \"dataStoreMaxConns\": 100,\n"+
"          \"maxUrlLength\": 500\n"+
"        }\n"+
"      },\n"+
"      {\n"+
"        \"servlet-name\": \"cofaxEmail\",\n"+
"        \"servlet-class\": \"org.cofax.cds.EmailServlet\",\n"+
"        \"init-param\": {\n"+
"          \"mailHost\": \"mail1\",\n"+
"          \"mailHostOverride\": \"mail2\"\n"+
"        }\n"+
"      },\n"+
"      {\n"+
"        \"servlet-name\": \"cofaxAdmin\",\n"+
"        \"servlet-class\": \"org.cofax.cds.AdminServlet\"\n"+
"      },\n"+
"      {\n"+
"        \"servlet-name\": \"fileServlet\",\n"+
"        \"servlet-class\": \"org.cofax.cds.FileServlet\"\n"+
"      },\n"+
"      {\n"+
"        \"servlet-name\": \"cofaxTools\",\n"+
"        \"servlet-class\": \"org.cofax.cms.CofaxToolsServlet\",\n"+
"        \"init-param\": {\n"+
"          \"log\": 1,\n"+
"          \"logMaxSize\": \"\",\n"+
"          \"adminGroupID\": 4,\n"+
"          \"betaServer\": true\n"+
"        }\n"+
"      }\n"+
"    ],\n"+
"    \"servlet-mapping\": {\n"+
"      \"cofaxCDS\": \"/\",\n"+
"      \"cofaxEmail\": \"/cofaxutil/aemail/*\",\n"+
"      \"cofaxAdmin\": \"/admin/*\",\n"+
"      \"fileServlet\": \"/static/*\",\n"+
"      \"cofaxTools\": \"/tools/*\"},\n"+
"    \"taglib\": {\n"+
"      \"taglib-uri\": \"cofax.tld\",\n"+
"      \"taglib-location\": \"/WEB-INF/tlds/cofax.tld\"\n"+
"    }\n"+
"  }\n"+
"}"));
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
/*#if DEBUG*#/
		Tester.setGenObjFile(true);
/*#end*/
		if (runTest(args) > 0) {System.exit(1);}
	}

}
