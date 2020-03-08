import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.util.Properties;
import org.w3c.dom.Element;

/** Read data from database, another example. */
public class Example6_DBRead1 {

	public static void main(String[] args) {
		//Generate XDPool
		String xdef = "./src/Example6_DBRead1.xdef";
		Properties props = System.getProperties();
		XDPool xp = XDFactory.compileXD(props, xdef);

		// Create XDDocument
		XDDocument xd = xp.createXDDocument();
		// Create database connection
		String url = GenDerby.DB_URL;
		String user = "myself";
		String password = "blabla";
		XDService service = XDFactory.createSQLService(url, user, password);

		// Set external variable with database connection to XDDocument
		xd.setVariable("inventory#service", service);

		// Construct element with books
		Element el = xd.xcreate("Inventory", null); //execute construction
		// Print created element
		System.out.println(KXmlUtils.nodeToString(el, true));
	}

}