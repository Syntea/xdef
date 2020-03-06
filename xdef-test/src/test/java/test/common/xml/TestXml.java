package test.common.xml;

import java.io.ByteArrayInputStream;
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
import buildtools.STester;

/** Test XML writer.
 * @author Vaclav Trojan
 */
public class TestXml extends STester  {

	/** Creates a new instance of TestXml */
	public TestXml() {}

	@Override
	/** Run test and print error information. */
	public void test() {
		String s = "Kůň " + (char)9 + (char)13 + " úpěl";
		DOMImplementation di;
		Document doc;
		Element el;
		try {
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

			s = s.trim();
			if (!("<root atr=\"Kůň &#9;&#13; úpěl\">" +
				"Kůň " + (char)9 + "&#13; úpěl</root>").equals(s) &&
				//ignore case if there is hexacecimal reprezentation
				!("<root atr=\"kůň &#x9;&#xd; úpěl\">" +
				"kůň "+(char)9+"&#xd; úpěl</root>").equals(s.toLowerCase())) {
				fail(s);
			}
		} catch (Exception ex) {
			fail(ex);
		}
		try {
			DocumentBuilderFactory bf =
				DocumentBuilderFactory.newInstance();
			DocumentBuilder db = bf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));
			doc = db.parse(is);
			el = doc.getDocumentElement();
			assertEq("Kůň "+(char)9+(char)13+" úpěl", el.getAttribute("atr"));
			assertEq("Kůň "+(char)9+(char)13+" úpěl",
				el.getChildNodes().item(0).getNodeValue());
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 * @throws Exception if an error occurs.
	 */
	public static void main(String... args) throws Exception {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
