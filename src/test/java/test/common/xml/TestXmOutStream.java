/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: TestXmlWriter.java, created 2011-11-12.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package test.common.xml;

import cz.syntea.xdef.xml.KDOMBuilder;
import cz.syntea.xdef.xml.KXmlOutStream;
import test.util.STester;

import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Test XmOutStream.
 * @author Vaclav Trojan <vaclav.trojan@syntea.cz>
 */
public class TestXmOutStream extends STester {

	public TestXmOutStream() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		KXmlOutStream w;
		StringWriter sw;
		Document doc;
		Element el;
		NodeList nl;
		KDOMBuilder db;
		String s;
		try {// no indenting;
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			doc = db.parse(
"<!-- c1 -->"+
"<a a=\"1\">"+
"<b>"+
"<c c=\"C\" d=\"D\"/>"+
"</b>"+
"<e e=\"1\">"+
"<e1 e1=\"e1\" />"+
"</e>"+
"<f f=\"&amp;p\n\">"+
"<g>"+
"<h h=\"H\"/>"+
"</g>"+
" text&amp;1 "+
"</f>"+
"</a>"+
"<!-- c2 -->");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "windows-1250", true);
			w.setIndenting(false);
			w.writeElementStart(el);
			w.writeText("\n");
			nl = el.getElementsByTagName("f");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					w.writeNode(nl.item(i));
				}
			}
			w.writeText("\n");
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"windows-1250\" ?>\n"+
"<!-- c1 -->\n"+
"<a a=\"1\">\n"+
"<f f=\"&amp;p \"><g><h h=\"H\"/></g> text&amp;1 </f>\n"+
"</a>\n"+
"<!-- c2 -->");
		} catch (Error ex) {
			fail(ex);
		}
		try {// indenting
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			doc = db.parse(
"<!-- c1 -->"+
"<a a=\"1\">"+
"<b>"+
"<c c=\"C\" d=\"D\"/>"+
"</b>"+
"<e e=\"1\">"+
"<e1 e1=\"e1\" />"+
"</e>"+
"<f f=\"&amp;p\n\">"+
"<g>"+
"<h h=\"H\"/>"+
"</g>"+
" text&amp;1 "+
"</f>"+
"</a>"+
"<!-- c2 -->");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "windows-1250", true);
			w.setIndenting(true);
			w.writeElementStart(el);
			w.writeText("\n");
			nl = el.getElementsByTagName("f");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					w.writeNode(nl.item(i));
				}
			}
			w.writeText("\n");
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"windows-1250\" ?>\n"+
"<!-- c1 --><a a=\"1\">\n"+
"  <f f=\"&amp;p \">\n"+
"    <g>\n"+
"      <h h=\"H\"/>\n"+
"    </g>\n"+
"    text&amp;1\n"+
"  </f>\n"+
"</a>\n"+
"<!-- c2 -->");
		} catch (Error ex) {
			fail(ex);
		}
		try {// namespace, no indenting
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			db.setNamespaceAware(true);
			doc = db.parse(
"<a xmlns=\"a\" a=\"1\">"+
"<b>"+
"<c c=\"C\" d=\"D\"/>"+
"</b>"+
"<e e=\"1\">"+
"<e1 e1=\"e1\" />"+
"</e>"+
"<f f=\"&amp;p\n\">"+
"<g>"+
"<h h=\"H\"/>"+
"</g>"+
" text&amp;1 "+
"</f>"+
"</a>");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "UTF-8", true);
			w.setIndenting(false);
			w.writeElementStart(el);
			w.writeText("\n");
			nl = el.getElementsByTagName("f");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					w.writeNode(nl.item(i));
				}
			}
			w.writeText("\n");
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
"<a xmlns=\"a\" a=\"1\">\n"+
"<f f=\"&amp;p \"><g><h h=\"H\"/></g> text&amp;1 </f>\n"+
"</a>");
		} catch (Error ex) {
			fail(ex);
		}
		try {// namespace, indenting
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			db.setNamespaceAware(true);
			doc = db.parse(
"<a xmlns=\"a\" xmlns:x=\"x\" a=\"1\">"+
"<b>"+
"<c c=\"C\" d=\"D\"/>"+
"</b>"+
" <e e=\"1\">"+
"<e1 e1=\"e1\" />"+
"</e>"+
"<f f=\"&amp;p\n\">"+
"<g>"+
"<h h=\"H\"/>"+
"</g>\n"+
"\n    text&amp;1  \n"+
"</f>\n"+
"</a>\n");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "UTF-8", true);
			w.setIndenting(true);
			w.writeElementStart(el);
			w.writeText("\n");
			nl = el.getElementsByTagName("f");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					w.writeNode(nl.item(i));
				}
			}
			w.writeText("\n");
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
"<a xmlns=\"a\"\n"+
"  a=\"1\">\n"+
"  <f f=\"&amp;p \">\n"+
"    <g>\n"+
"      <h h=\"H\"/>\n"+
"    </g>\n"+
"    text&amp;1\n"+
"  </f>\n"+
"</a>");
		} catch (Error ex) {
			fail(ex);
		}
		try {// namespace, no indenting
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			db.setNamespaceAware(true);
			doc = db.parse(
"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"+
"<soap:Header>"+
"</soap:Header>"+
"<soap:Body>"+
"<m:GetStockPrice xmlns:m=\"http://www.example.org/stock\">"+
"<m:StockName>IBM</m:StockName>"+
"</m:GetStockPrice>"+
"</soap:Body>"+
"</soap:Envelope>");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "UTF-8", true);
			w.setIndenting(false);
			w.writeElementStart(el);
			nl = el.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				w.writeNode(nl.item(i));
			}
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"+
"<soap:Header/>"+
"<soap:Body>"+
"<m:GetStockPrice xmlns:m=\"http://www.example.org/stock\">"+
"<m:StockName>IBM</m:StockName>"+
"</m:GetStockPrice>"+
"</soap:Body>"+
"</soap:Envelope>");
		} catch (Error ex) {
			fail(ex);
		}
		try {// namespace, no indenting
			db = new cz.syntea.xdef.xml.KDOMBuilder();
			db.setIgnoringComments(false);
			db.setNamespaceAware(true);
			doc = db.parse(
"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"+
"<soap:Header>"+
"</soap:Header>"+
"<soap:Body>"+
"<m:GetStockPrice xmlns:m=\"http://www.example.org/stock\">"+
"<m:StockName>IBM</m:StockName>"+
"</m:GetStockPrice>"+
"</soap:Body>"+
"</soap:Envelope>");
			el = doc.getDocumentElement();

			sw = new StringWriter();
			w = new KXmlOutStream(sw, "UTF-8", true);
			w.setIndenting(true);
			w.writeElementStart(el);
			nl = el.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				w.writeNode(nl.item(i));
			}
			w.writeElementEnd(el);
			w.writeXmlTail(doc);
			w.closeStream();
			s = sw.toString();
//			System.out.println("'" + s + "'");
			db.parse(s);
			assertEq(s,
"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+
"<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n"+
"  <soap:Header/>\n"+
"  <soap:Body>\n"+
"    <m:GetStockPrice xmlns:m=\"http://www.example.org/stock\">\n"+
"      <m:StockName>\n"+
"        IBM\n"+
"      </m:StockName>\n"+
"    </m:GetStockPrice>\n"+
"  </soap:Body>\n"+
"</soap:Envelope>");
		} catch (Error ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
//		cz.syntea.common.xml.KXmlUtils.setDOMImplementation("javax",true,true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
