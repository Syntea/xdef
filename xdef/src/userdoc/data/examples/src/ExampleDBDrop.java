import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.util.Properties;

/** Drop database schema (use X-definition). */
public class ExampleDBDrop {

    public static void main(String[] args) {
        //Generate XDPool
        String xdef = "./src/ExampleDBDrop.xdef";
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
        xd.setVariable("service", service); //set connection

        // Execute processing of XML data with XDefinition
        String xml = "./src/ExampleDBDrop.xml";
        xd.xparse(xml, null); //process data (insert to database)
        System.out.println("ExampleDBDrop OK, database schema dropped");
        //close database connection
        service.close();
    }

}