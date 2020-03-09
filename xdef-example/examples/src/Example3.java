import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;

/** Construct the XML element from XML data (xcreate). */
public class Example3 {

	public static void main(String[] args) {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example3.xdef";
		String xmlData = "./src/Example3.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xpool.createXDDocument();

		// 3. Set element from the file as context.
		xdoc.setXDContext(KXmlUtils.parseXml(xmlData));

		// 4. generate result XML.
		Element result = xdoc.xcreate("Contract", null);

		// 5. print it!
		System.out.println(KXmlUtils.nodeToString(result, true));
	}
}