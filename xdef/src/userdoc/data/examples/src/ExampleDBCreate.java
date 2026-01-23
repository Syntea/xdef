import java.util.Properties;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;

/** Creating a database schema described by XML data using X-definition. 
 * (see ExampleDBCreate.xml and ExampleDBCreate.xdef). */
public class ExampleDBCreate {

	public static void main(String[] args) {
		// Delete the database (if exists) and create the clean new one.
	   GenDerby.prepare();

	   // Generate XDPool
	   String xdef = "./src/ExampleDBCreate.xdef";
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
	   xd.setVariable("service", service);

	   // Execute processing of XML data with XDefinition
	   String xml = "./src/ExampleDBCreate.xml";
	   xd.xparse(xml, null);

	   //close database connection
	   service.close();
	   System.out.println("ExampleDBCreate OK, Derby database prepared");
    }

}