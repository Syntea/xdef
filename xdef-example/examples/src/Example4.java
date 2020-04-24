import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import javax.xml.namespace.QName;

/** Construct XML element from source XML data; element with source data is
 parsed by X-definition command (see xd:declaration section).
 */
public class Example4 {

	public static void main(String[] args) {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example4.xdef";
		String xmlData = "./src/Example4.xml";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument();

		// 3. parse source XML (and set values of variables).
		xdoc.xparse(xmlData, null);

		// 4. Set context with parsed element in previous step to XDDocument.
		// XDDocument contains parsed element and also there are
		// assigned values to variables in XDDocument.
		// We set the parsed result element as context to the XDDocument.
		xdoc.setXDContext(xdoc.getElement());

		// 5. Because the model used for cconstruction of result has
		// the namespance we must create the QNname with
		// the namespace URI and local name.
		String modelNamespace = "http://www.w3.org/1999/xhtml";
		String modelLocalname = "html";
		QName modelName = new QName(modelNamespace, modelLocalname);

		// 6. create result from model (context data were set
		// in the previons step).
		QName model = new QName(modelNamespace, modelLocalname);
		xdoc.xcreate(model, null);

		// 7. print it!
		System.out.println(KXmlUtils.nodeToString(xdoc.getElement(), true));
	}
}