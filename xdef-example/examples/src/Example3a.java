import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

/** Construct XML element from source XML data; set context to the external
 variable
 */
public class Example3a {

	public static void main(String[] args) {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example3a.xdef";
		String xmlData = "./src/Example3.xml";

		// 1. Create XDPool.
		XDPool xpool = XDFactory.compileXD(System.getProperties(), xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xpool.createXDDocument();

		// 3. Set to XDDocument element with source data.
		xdoc.setVariable("source", xmlData);

		// 4. generate result XML document.
		Element result = xdoc.xcreate("Contract", null);

		// 5. print it!
		System.out.println(KXmlUtils.nodeToString(result, true));

	}
}