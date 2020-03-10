import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.w3c.dom.Element;

/** Construct XML element from XML source element. */
public class Example5 {

	public static void main(String[] args) {
		// Prepare source path to XDefinition and XML data.
		String xdef = "./src/Example5.xdef";
		String xmlData = "./src/Example5.xml";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument();

		// 3. set source element file name to the variable "source".
		xdoc.setVariable("source", xmlData);

		// 4. create result
		Element result = xdoc.xcreate("company", null);

		// 5. print it!
		System.out.println(KXmlUtils.nodeToString(result, true));
	}

}