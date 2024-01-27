import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;

/** Construct XML element from XML source element. */
public class ExampleXQuery {

	public static void main(String[] args) {
		// Prepare source path to XDefinition and XML data.
		String xdef = "./src/ExampleXQuery.xdef";
		String xmlData = "./src/ExampleXQuery.xml";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);
		
		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument();
		
		// 3. set the XML to context 
		Element el = KXmlUtils.parseXml(xmlData).getDocumentElement();
		KXmlUtils.nodeToString(el, true);
		xdoc.setXDContext(el);

		// 4. create result
		Element result = xdoc.xcreate("anthill", null);

		// 5. print it!
		System.out.println(KXmlUtils.nodeToString(result, true));
    }

}
