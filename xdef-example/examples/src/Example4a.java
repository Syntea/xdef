import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/** Construct XML element with X-definition from source XML data. Element with
 * source data is parsed by command in the X-definition.
 */
public class Example4a {

	public static void main(String[] args) {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example4a.xdef";
		String xmlData = "./src/Example4.xml";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument();

		// 3. set name of the input file to variable "source"
		xdoc.setVariable("source", xmlData);

		// 4. Because the model used for cconstruction of result has
		// the namespance we must create the QNname with
		// the namespace URI and local name.
		String modelNamespace = "http://www.w3.org/1999/xhtml";
		String modelLocalname = "html";
		QName modelName = new QName(modelNamespace, modelLocalname);

		// 5. create result from model.
		Element element = xdoc.xcreate(modelName, null);

		// 6. print it!
		System.out.println(KXmlUtils.nodeToString(element, true));
	}
}