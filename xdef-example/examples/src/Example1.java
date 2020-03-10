import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;

/** Validate XML with X-definition (no errors, input data are correct) */
public class Example1 {

	public static void main(String[] args) {
		// Prepare path to X-definition and XML data.
		String xdef = "./src/Example1.xdef";
		String xmlData = "./src/Example1.xml";

		// 1. Create XDPool.
		Properties props = System.getProperties();
		XDPool xp = XDFactory.compileXD(props, xdef);

		// 2. Create XDDocument.
		XDDocument xdoc = xp.createXDDocument();

		// 3. Validate and process XML data.
		try {
			xdoc.xparse(xmlData, null); //validate and process data
			System.out.println("Input data processed and no errors detected");
		} catch(Exception ex) {
			System.err.println(ex.toString());
		}
	}

}