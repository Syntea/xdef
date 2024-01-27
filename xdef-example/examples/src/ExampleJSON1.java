import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.xdef.xon.XonUtils;

/** Example of parsing JSON data with X-definition. */
public class ExampleJSON1 {

	public static void main(String[] args) {
		// Prepare source path to X-definition and JSON data.
		String xdef = "./src/ExampleJSON1.xdef";
		String data = "./src/ExampleJSON1.json";

		// 1. Create XDPool
		Properties props = System.getProperties();
		XDPool xpool = XDFactory.compileXD(props, xdef);
		
		// 2. Create XDDocument
		XDDocument xdoc = xpool.createXDDocument();

		// 4. parse JSON data; if an error occurs the RuntimeException is thrown
		Object json = xdoc.jparse(data, null); // 
		
		// 5. Print parsed result 
		System.out.println("Parsed JSON result: ");
		System.out.println(XonUtils.toJsonString(json, true));
    }
}
