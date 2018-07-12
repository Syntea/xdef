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
import java.io.File;
import org.w3c.dom.Document;
import cz.syntea.xdef.sys.ReportWriter;

/**
 *
 * @author Vaclav Trojan
 *
 * @version 1.0.0
 */
public class TestXml1 extends STester  {

	/** Creates a new instance of TestXml */
	public TestXml1() {}

	private String getFileNameFromIndex(int i) {
		return (i < 10 ? "00" + i : i < 100 ? "0" + i : String.valueOf(i)) +
			".xml";
	}

	private void testValid(String provider, String dir, int i) {
		KDOMBuilder builder;
		Document doc, doc1;
		String dataDir = getDataDir() + "xmltest/valid/" + dir + "/";
		File f;
		String fname;
		if ((f = new File(dataDir +
				(fname = getFileNameFromIndex(i)))).exists()) {
//            System.err.println("===" + dir + "/" + fname + "===");
//            System.err.flush();
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
							return;
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
				System.err.flush();
				fail(provider + ": " + dir + "/" + fname + ", exception\n" +
					ex);
			}
			System.err.flush();
		}
	}

	private void testNotWF(String provider, String dir, int i) {
		KDOMBuilder builder;
		Document doc, doc1;
		String dataDir = getDataDir() + "xmltest/not-wf/" + dir + "/";
		File f;
		String fname;
		if ((f = new File(dataDir +
			(fname = getFileNameFromIndex(i)))).exists()) {
			System.out.println(provider + ": " + dir + "/" + fname);
			try {
				builder = new KDOMBuilder();
				builder.setNamespaceAware(false);
				builder.setExpandEntityReferences(true);
				builder.setValidating(true);
				builder.setIgnoringComments(true);
				builder.setCoalescing(true);
				doc = builder.parse(f);
				fail(provider + ": " + dir + "/" + fname +
					", error not thrown\n" +
					KXmlUtils.nodeToString(doc));
			} catch (Exception ex) {
				fail(ex);
			}
			System.err.flush();
		}
	}

	private void testInvalid(String provider, int i) {
		KDOMBuilder builder;
		Document doc, doc1;
		String dataDir = getDataDir() + "xmltest/invalid/";
		File f;
		String fname;
		if ((f = new File(dataDir +
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
				fail(ex);
			}
			System.err.flush();
		}
	}

	private void test(String provider) {
		testValid(provider, "sa", 114);
//		testValid(provider, "sa", 128);
//		testValid(provider, "sa", 97);
//      testValid(provider, "not-sa", 22);
//      testValid(provider, "ext-sa", 0);
//		testNotWF(provider, "sa", 181);
//		testNotWF(provider, "sa", 187);
//		testNotWF(provider, "not-sa", 8);
//		testNotWF(provider, "ext-sa", 2);
//		testInvalid(provider, 1);
	}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
/*#if DEBUG*/
			test("javax");
/*#end*/
			test("syntea");
		} catch (Error ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 * @throws Exception if an error occurs.
	 */
	public static void main(String[] args) throws Exception {
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
