package test.common.json;

import org.xdef.sys.JSONUtil;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import test.utils.STester;

/** Test JSON tools.
 * @author trojan
 */
public class TestJSON extends STester {

	public TestJSON() {super();}

	private static NodeList trimText(Element el) {
		NodeList nl = el.getChildNodes();
		for (int i = nl.getLength() - 1; i >=0 ; i--) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.TEXT_NODE ||
				n.getNodeType() == Node.CDATA_SECTION_NODE) {
				String s = n.getNodeValue();
				if (s != null) {
					s = s.trim();
					if (s.length() > 0) {
						StringParser p = new StringParser(s);
						StringBuilder sb = new StringBuilder();
						String sep = "";
						while ((p.isSpaces() || true) && !p.eos()) {
							if (p.isToken("null")) {
								sb.append(sep).append("null");
							} else if (p.isToken("false")) {
								sb.append(sep).append("false");
							} else if (p.isToken("true")) {
								sb.append(sep).append("true");
							} else if (p.isSignedFloat()||p.isSignedInteger()) {
								sb.append(sep).append(p.getParsedString());
							} else {
								if (p.isChar('"')) { // quoted string
									sb.append(sep);
									sb.append('"');
									for (;;) {
										if (p.eos()) {
											throw new RuntimeException(
												"Unclosed string");
										}
										if (p.isToken("\"\"")) {
											sb.append("\"\"");
										} else if (p.isChar('"')) {
											sb.append('"');
											break;
										} else {
											sb.append(p.peekChar());
										}
									}
								} else { //not quoed string
									int pos = p.getIndex();
									while (!p.isSpace() && !p.eos()) {
										p.nextChar();
									}
									sb.append(
										p.getParsedBufferPartFrom(pos).trim());
								}
							}
							sep = " ";
						}
						n.setNodeValue(sb.toString());
					} else {
						el.removeChild(n);
					}
				}
			}
		}
		return el.getChildNodes();
	}

	public static boolean equalJElems(Element e1, Object o) {
		Element e2 = (o instanceof String) ?
			KXmlUtils.parseXml((String) o).getDocumentElement() : (Element) o;
		String uri = e1.getNamespaceURI();
		if (uri == null) {
			if (e2.getNamespaceURI() != null) {
				return false;
			}
		} else if (!uri.equals(e2.getNamespaceURI())) {
			return false;
		}
		if (e1.getNodeName().equals(e2.getNodeName())) {
			NamedNodeMap nnm1 = e1.getAttributes();
			NamedNodeMap nnm2 = e2.getAttributes();
			int len = nnm1 == null ? 0 : nnm1.getLength();
			if (len != (nnm2 == null ? 0 : nnm2.getLength())) {
				return false;
			}
			if (len > 0) {
				for (int i = 0; i < len; i++) {
					Node n1 = nnm1.item(i);
					String u = n1.getNamespaceURI();
					Node n2;
					if (u != null) {
						n2 = nnm2.getNamedItemNS(u, n1.getLocalName());
					} else {
						n2 = nnm2.getNamedItem(n1.getNodeName());
					}
					if (n2 == null ||
						!n1.getNodeValue().equals(n2.getNodeValue())) {
						return false;
					}
				}
			}
		} else {
			return false;
		}
		NodeList nl1 = trimText(e1);
		NodeList nl2 = trimText(e2);
		int len = nl1.getLength();
		if (nl2.getLength() != len) {
			return false;
		}
		if (JSONUtil.JSON_NS_URI.equals(uri) &&
			(JSONUtil.J_MAP.equals(e1.getLocalName())
			|| JSONUtil.J_EXTMAP.equals(e1.getLocalName()))) {
			for (int i = 0; i < len; i++) {
				Element m1 = (Element) nl1.item(i);
				for (int j=0; j < len; j++) {
					Element m2 =
						KXmlUtils.firstElementChild(e2, m1.getNodeName());
					if (m2 == null || !equalJElems(m1, m2)) {
						return false;
					}
				}
			}
		} else {
			for (int i = 0; i < len; i++) {
				Node n1 = nl1.item(i);
				Node n2 = nl2.item(i);
				if (n1.getNodeType() != n2.getNodeType()) {
					return false;
				}
				if (n1.getNodeType() == Node.ELEMENT_NODE) {
					if (!equalJElems((Element) n1, (Element) n2)) {
						return false;
					}
				} else {
					String s1 = n1.getNodeValue();
					if (s1 == null && n2.getNodeValue() != null) {
						return false;
					}
					if (n2.getNodeValue() == null) {
						return false;
					}
					if (!s1.trim().equals(n2.getNodeValue().trim())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	static boolean check(String s1, String s2) {
		if (!check(s1)) {
			return false;
		}
		if (!check(s2)) {
			return false;
		}
		if (s1.charAt(0) == '<') {
			return chkx(s1, s2);
		} else {
			return chkj(s1, s2);
		}
	}

	static boolean chkj(String json, String el) {
		Object o1 = JSONUtil.parseJSON(json);
		Element e = JSONUtil.jsonToXml(o1);
//		System.out.println("===\n"+JSONUtil.toJSONString(o1, true));
		Object o2 = JSONUtil.xmlToJson(e);
		if (!JSONUtil.jsonEqual(o1, o2)) {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nJ2: " + JSONUtil.toJSONString(o2)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\njs: " + json + "\nel: " + el);
			return false;
		}
		Element f = JSONUtil.jsonToXml(o2);
		if (equalJElems(e, f) && equalJElems(f, el)) {
			return true;
		} else {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nJ2: " + JSONUtil.toJSONString(o2)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\nE2: " + KXmlUtils.nodeToString(f)
				+ "\njs: " + json + "\nel: " + el);
			return false;
		}
	}

	static boolean chkx(String el, String json) {
		Element e = KXmlUtils.parseXml(el).getDocumentElement();
		Object o1 = JSONUtil.xmlToJson(e);
		Element f = JSONUtil.jsonToXml(o1);
		if (!equalJElems(e, f)) {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\nE2: " + KXmlUtils.nodeToString(f)
				+ "\njs: " + json + "\nel: " + el);
			return true;
		}
		Object o2 = JSONUtil.xmlToJson(f);
		if (!JSONUtil.jsonEqual(o2, JSONUtil.parseJSON(json))) {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nJ2: " + JSONUtil.toJSONString(o2)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\nE2: " + KXmlUtils.nodeToString(f)
				+ "\njs: " + json + "\nel: " + el);
			return true;
		}
		if (!JSONUtil.jsonEqual(o1, o2)) {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nJ2: " + JSONUtil.toJSONString(o2)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\nE2: " + KXmlUtils.nodeToString(f)
				+ "\njs: " + json + "\nel: " + el);
			return true;
		}
		if (equalJElems(f, el)) {
			return true;
		} else {
			System.err.println("J1: " + JSONUtil.toJSONString(o1)
				+ "\nJ2: " + JSONUtil.toJSONString(o2)
				+ "\nEl: " + KXmlUtils.nodeToString(e)
				+ "\nE2: " + KXmlUtils.nodeToString(f)
				+ "\njs: " + json + "\nel: " + el);
			return false;
		}
	}

	static boolean check(String s) {
		if (s.charAt(0) == '<') {
			return chkx(s);
		} else {
			return chkj(s);
		}
	}

	static boolean chkj(String json) {
		try {
			Object o1 = JSONUtil.parseJSON(json);
			Element e1 = JSONUtil.jsonToXml(o1);
			Object o2 = JSONUtil.xmlToJson(e1);
			if (!JSONUtil.jsonEqual(o1, o2)) {
				System.err.println("o1:" + JSONUtil.toJSONString(o1));
				System.err.println("e1:" + KXmlUtils.nodeToString(e1));
				System.err.println("o2:" + JSONUtil.toJSONString(o2));
				return false;
			}
			Element e2 = JSONUtil.jsonToXml(o2);
			if (!equalJElems(e1, e2)) {
				return false;
			}
			return JSONUtil.jsonEqual(
				JSONUtil.xmlToJson(e1),JSONUtil.xmlToJson(e2));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return false;
		}
	}

	static boolean chkx(String x) {
		try {
			Element e1 = KXmlUtils.parseXml(x).getDocumentElement();
			Object o1 = JSONUtil.xmlToJson(e1);
//			System.out.println(JSONUtil.toJSONString(o1));
			Element e2 = JSONUtil.jsonToXml(o1);
			Object o2 = JSONUtil.xmlToJson(e2);
			if (!JSONUtil.jsonEqual(o1, o2) || !equalJElems(e1, e2)) {
				System.err.println("e1:" + x);
				System.err.println("o1:" + o1);
				System.err.println("e2:" + KXmlUtils.nodeToString(e2));
				System.err.println("o2:" + o2);
				return false;
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return false;
		}
	}

    static boolean chkxj(String x, String json) {
		try {
			Element e1 = KXmlUtils.parseXml(x).getDocumentElement();
			Object o1 = JSONUtil.xmlToJson(e1);
			Object o2 = JSONUtil.parseJSON(json);
			if (!JSONUtil.jsonEqual(o1, o2)) {
				System.err.println("e1:" + x);
				System.err.println("o1:" + JSONUtil.toJSONString(o1));
				System.err.println("o2:" + JSONUtil.toJSONString(o2));
				return false;
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return false;
		}
	}

	static boolean chkIO(String source, String charset) {
		try {
			java.io.ByteArrayOutputStream baos =
				new java.io.ByteArrayOutputStream();
			java.io.Writer wr = new java.io.OutputStreamWriter(baos, charset);
			wr.write(source);
			wr.close();
			java.io.ByteArrayInputStream in =
				new java.io.ByteArrayInputStream(baos.toByteArray());
			return (source.equals(
				JSONUtil.toJSONString(JSONUtil.parseJSON(in))));
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return false;
		}
	}

	@Override
	/** Run test and print error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		// check input stream in UTF-8, UTF-16(BE,LE) and UTF-32(BE,LE)
		assertTrue(chkIO("[]", "UTF-8"));
		assertTrue(chkIO("[]", "UTF-16LE"));
		assertTrue(chkIO("[]", "UTF-16BE"));
		assertTrue(chkIO("[]", "UTF-32LE"));
		assertTrue(chkIO("[]", "UTF-32BE"));
		assertTrue(chkIO("{\"a\":\"\u010d\"}", "UTF-8"));
		assertTrue(chkIO("{\"a\":\"\u010d\"}", "UTF-16LE"));
		assertTrue(chkIO("{\"a\":\"\u010d\"}", "UTF-16BE"));
		assertTrue(chkIO("{\"a\":\"\u010d\"}", "UTF-32LE"));
		assertTrue(chkIO("{\"a\":\"\u010d\"}", "UTF-32BE"));
////////////////////////////////////////////////////////////////////////////////
		// Check error reporting
		try {
			JSONUtil.parseJSON("[00]"); // more leading zeroes
			fail("Error not reported");
		} catch (Exception ex) {
			if (ex.toString().indexOf("JSON014") < 0) {
				fail(ex);
			}
		}
////////////////////////////////////////////////////////////////////////////////
		// check conversion of xml to json
		assertTrue(chkxj("<a/>", "{\"a\":null}"));
		assertTrue(chkxj("<a x='1'/>", "{\"a\":{\"x\":1}}"));
		assertTrue(chkxj("<a>1</a>", "{\"a\":1}"));
		assertTrue(chkxj("<a x='1'>2</a>", "[{\"a\":{\"x\":1}},2]"));
		assertTrue(chkxj("<a>1 2</a>", "{\"a\":[1,2]}"));
		assertTrue(chkxj("<a x='1'>2 3</a>", "[{\"a\":{\"x\":1}},2,3]"));
		assertTrue(chkxj("<a><b/></a>", "{\"a\":{\"b\":null}}"));
		assertTrue(chkxj("<a x='1'><b/></a>",
			"[{\"a\":{\"x\":1}},{\"b\":null}]"));
		assertTrue(chkxj("<a><b/><c/></a>",
			"{\"a\":[{\"b\":null},{\"c\":null}]}"));
		assertTrue(chkxj("<a x='1'><b/><c/></a>",
			"[{\"a\":{\"x\":1}},{\"b\":null},{\"c\":null}]"));
		assertTrue(chkxj(
"<a><js:array xmlns:js='http://www.syntea.cz/json/1.0'>1 2</js:array></a>",
"{\"a\":[1, 2]}"));
		assertTrue(chkxj(
"<a x='1'><js:array xmlns:js='http://www.syntea.cz/json/1.0'>1</js:array></a>",
"[{\"a\":{\"x\":1}},[1]]"));
		assertTrue(chkxj(
"<a><js:array xmlns:js='http://www.syntea.cz/json/1.0'><b/></js:array></a>",
"{\"a\":[{\"b\":null}]}"));
		assertTrue(chkxj(
"<a x='1'>"
	+ "<js:array xmlns:js='http://www.syntea.cz/json/1.0'><b/></js:array>"+
"</a>",
"[{\"a\":{\"x\":1}},[{\"b\":null}]]"));
////////////////////////////////////////////////////////////////////////////////
		// check simple json <-> conversions
		assertTrue(check("[]"));
		assertTrue(check("{}"));
		assertTrue(check("[0]"));
		assertTrue(check("<a/>"));
		assertTrue(check("{\"a\":null}"));
		assertTrue(check("<a x='1' y='true' z='null'/>"));
		assertTrue(check("{\"a\":{\"z\":null,\"y\":true,\"x\":1}}"));
		assertTrue(check("<a x='1'/>"));
////////////////////////////////////////////////////////////////////////////////
//		System.out.println(KXmlUtils.nodeToString(KXmlUtils.parseXml(
//			"test/test/common/json/personnel1_o.xml"), true));
// http://convertjson.com/json-to-xml.htm
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
