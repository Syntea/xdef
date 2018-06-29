/*
 * File: TestXml.java
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
package test.common.xml;

import cz.syntea.xdef.sys.STester;
import cz.syntea.xdef.xml.KDOMBuilder;
import cz.syntea.xdef.xml.KXmlUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import cz.syntea.xdef.sys.ReportWriter;

/** This test is based on James Clark package of XML data (see
 * http://www.jclark.com/xml/).
 *
 * @author Vaclav Trojan
 *
 * @version 1.0.0
 */
public class TestXml extends STester  {

	/** Creates a new instance of TestXml */
	public TestXml() {}

	private String getFileNameFromIndex(int i) {
		return (i < 10 ? "00" + i : i < 100 ? "0" + i : String.valueOf(i)) +
			".xml";
	}

	private void testValid(String provider, String dir) {
		KDOMBuilder builder;
		Document doc, doc1;
		String dataDir = getDataDir() + "xmltest/valid/" + dir + "/";
		int i = 1;
		File f;
		String fname;
		while ((f = new File(dataDir +
				(fname = getFileNameFromIndex(i++)))).exists()) {
			try {
				builder = new KDOMBuilder();
				builder.setNamespaceAware(false);
				builder.setExpandEntityReferences(true);
				builder.setValidating(true);
				builder.setIgnoringComments(true);
				builder.setCoalescing(true);
				doc = builder.parse(f);
				try {
					builder = new KDOMBuilder();
					builder.setNamespaceAware(false);
					builder.setExpandEntityReferences(true);
					builder.setValidating(false);
					builder.setIgnoringComments(false);
					builder.setIgnoringComments(true);
					builder.setCoalescing(true);
					doc1 = builder.parse(new File(dataDir + "out/" + fname));
					ReportWriter rwi = KXmlUtils.compareElements(
						doc.getDocumentElement(), doc1.getDocumentElement());
					if (rwi != null && rwi.errorWarnings()) {
						if (provider.equals("javax") &&
							(("sa".equals(dir) && i != 97) ||
							("not-sa".equals(dir) && i != 22) ||
							("ext-sa".equals(dir) && i != 999))) {
							continue;
						}
						System.err.flush();
						fail(provider + ": " + dir + "/" + fname +
							", incorrect result:\n" +
							KXmlUtils.nodeToString(doc) + "\ncorrect:\n" +
							KXmlUtils.nodeToString(doc1));
						rwi.getReportReader().printReports(System.err);
					}
				} catch (Exception ex) {
					System.err.flush();
					fail(provider + ": " + dir + "/out/" + fname +
						" can't parse\n" + ex);
				}
			} catch (Exception ex) {
				if (provider.equals("javax") &&
					(("sa".equals(dir) && i != 97) ||
					("not-sa".equals(dir) && i != 22) ||
					("ext-sa".equals(dir) && i != 999))) {
					continue;
				}
				System.err.flush();
				fail(provider + ": " + dir + "/" + fname + ", exception\n" +ex);
			}
			System.err.flush();
		}
	}

	private void testNotWF(String provider, String dir) {
		if ("javax".equals(provider)) return;
		KDOMBuilder builder;
		Document doc;
		String dataDir = getDataDir() + "xmltest/not-wf/" + dir + "/";
		int i = 1;
		File f;
		String fname;
		while ((f = new File(dataDir +
			(fname = getFileNameFromIndex(i++)))).exists()) {
			try {
				builder = new KDOMBuilder();
				builder.setNamespaceAware(false);
				builder.setExpandEntityReferences(true);
				builder.setValidating(true);
				builder.setIgnoringComments(true);
				builder.setCoalescing(true);
				doc = builder.parse(f);
				fail(provider + ": " + dir + "/" + fname +
					", notWF error not thrown\n" +
					KXmlUtils.nodeToString(doc));
			} catch (Exception ex) {}
			System.err.flush();
		}
	}

	private void testInvalid(String provider) {
		if ("javax".equals(provider)) {
			return;
		}
		KDOMBuilder builder;
		Document doc, doc1;
		String dataDir = getDataDir() + "xmltest/invalid/";
		int i = 1;
		File f;
		String fname;
		while ((f = new File(dataDir +
			(fname = getFileNameFromIndex(i++)))).exists()) {
			try {
				builder = new KDOMBuilder();
				builder.setNamespaceAware(false);
				builder.setExpandEntityReferences(true);
				builder.setValidating(true);
				builder.setIgnoringComments(true);
				builder.setCoalescing(true);
				doc = builder.parse(f);
				fail(provider + ": invalid/" + fname +
					", invalid error not thrown\n" +
					KXmlUtils.nodeToString(doc));
			} catch (Exception ex) {

			}
			System.err.flush();
		}
	}

	private void testWriter(String provider) {
		String s = "Kùò " + (char)9 + (char)13 + " úpìl";
		DOMImplementation di;
		Document doc;
		Element el;
		try {
			if ("syntea".equals(provider)) {
				KDOMBuilder builder = new KDOMBuilder();
				di = builder.getDOMImplementation();
				doc = di.createDocument(null, "root", null);
				el = doc.getDocumentElement();
				el.setAttribute("atr", s);
				el.appendChild(doc.createTextNode(s));
				s = KXmlUtils.nodeToString(el);
			} else {
				DocumentBuilderFactory bf =
					DocumentBuilderFactory.newInstance();
				DocumentBuilder db = bf.newDocumentBuilder();
				di = db.getDOMImplementation();
				doc = di.createDocument(null, "root", null);
				el = doc.getDocumentElement();
				el.setAttribute("atr", s);
				el.appendChild(doc.createTextNode(s));
				TransformerFactory transFactory =
					TransformerFactory.newInstance();
				Transformer transformer = transFactory.newTransformer();
				StringWriter buffer = new StringWriter();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
					"yes");
				transformer.transform(
					new DOMSource(el), new StreamResult(buffer));
				s = buffer.toString();
			}
			s = s.trim();
			if (!("<root atr=\"Kùò &#9;&#13; úpìl\">" +
				"Kùò " + (char)9 + "&#13; úpìl</root>").equals(s) &&
				//ignore case if there is hexacecimal reprezentation
				!("<root atr=\"kùò &#x9;&#xd; úpìl\">" +
				"kùò "+(char)9+"&#xd; úpìl</root>").equals(s.toLowerCase())) {
				fail("'" + s + "', provider = " + provider);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			if ("syntea".equals(provider)) {
				KDOMBuilder builder = new KDOMBuilder();
				InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
				doc = builder.parse(is);
			} else {
				DocumentBuilderFactory bf =
					DocumentBuilderFactory.newInstance();
				DocumentBuilder db = bf.newDocumentBuilder();
				InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
				doc = db.parse(is);
			}
			el = doc.getDocumentElement();
			assertEq("Kùò "+(char)9+(char)13+" úpìl", el.getAttribute("atr"),
				provider);
			assertEq("Kùò "+(char)9+(char)13+" úpìl",
				el.getChildNodes().item(0).getNodeValue(), provider);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	private void test(String provider) {
		testValid(provider, "sa");
		testValid(provider, "not-sa");
		testValid(provider, "ext-sa");
		testNotWF(provider, "sa");
		testNotWF(provider, "not-sa");
		testNotWF(provider, "ext-sa");
		testInvalid(provider);
		testWriter(provider);
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
/*#if DEBUG*#/
			test("javax");
/*#end*/
		} catch (Error ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception {
//		cz.syntea.common.xml.KXmlUtils.setDOMImplementation("javax",true,true);
		// TODO code application logic here
/*
		ByteArrayInputStream in = new ByteArrayInputStream(
			"<a xmlns:b=\"asd\"><b:b/></a>".getBytes("UTF-8"));
		KDOMBuilder builder = new KDOMBuilder("javax");
		builder.setValidating(false);
		builder.setNamespaceAware(true);
		Document doc = builder.parse(in);
		System.out.println(KXmlUtils.nodeToString(doc));

		doc = KXmlUtils.parseXml("<a xmlns:b=\"asd\"><b:b/></a>");
		System.out.println(KXmlUtils.nodeToString(doc));
		Element el = doc.getDocumentElement();
		System.out.println(KXmlUtils.nodeToString(el));
		System.out.println(KXmlUtils.nodeToString(el.getChildNodes().item(0)));
		builder.setValidating(true);
		builder.setNamespaceAware(true);
		doc = builder.parse("d:/temp/a.xml");
		System.out.println(KXmlUtils.nodeToString(doc));
 **/
		if (runTest(args) > 0) {System.exit(1);}
	}

}
