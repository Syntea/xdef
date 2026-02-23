import org.xdef.sys.ArrayReporter;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.util.Properties;

/** Insert books to database (explicit statements). */
public class ExampleDBInsert {

    public static void main(String[] args) {
        // Generate XDPool
        String xdef = "./src/ExampleDBInsert.xdef";
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
        String xml = "./src/ExampleDBInsert.xml";
        ArrayReporter reporter = new ArrayReporter(); //prepare reporter
        xd.xparse(xml, reporter); //process data
        //close database connection
        service.close();

        // Get number of inserted books from XDDocument
        int i = xd.getVariable("inserted").intValue();
        // Print number of inseted books
        System.out.println("Inserted " + i + " books");
        if (reporter.errors()) {
            System.err.println("Error in ExampleDBInsert\n" + reporter.printToString());
        } else {
            System.out.println("ExampleDBInsert OK");
        }
    }

}