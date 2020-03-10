import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.util.Properties;


/** Create database schema from XML data (use X-definition). */
public class Example6_DBCreate {

	public static void main(String[] args) {
		 // Deletes teh old one Derby database and creates the new one.
		GenDerby.prepare();

		// Generate XDPool
		String xdef = "./src/Example6_DBCreate.xdef";
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
		xd.setVariable("#service", service);

		// Execute processing of XML data with XDefinition
		String xml = "./src/Example6_DBCreate.xml";
		xd.xparse(xml, null);

		//close database connection
		service.close();
	}

}