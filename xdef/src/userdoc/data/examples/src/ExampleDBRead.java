import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDService;
import java.util.Properties;
import org.w3c.dom.Element;

/** Read data from database. */
public class ExampleDBRead {

    public static void main(String[] args) {
        // Generate XDPool
        String xdef = "./src/ExampleDBRead.xdef";
        Properties props = System.getProperties();
        XDPool xp = XDFactory.compileXD(props, xdef);

        // Create database connection
        String url = GenDerby.DB_URL;
        String user = "myself";
        String password = "blabla";
        XDService service = XDFactory.createSQLService(url, user, password);

        // Create XDDocument
        XDDocument xd = xp.createXDDocument();

        // Set external variable with database connection to XDDocument
        xd.setVariable("service", service);

        // Construct XML data with books
        Element el = xd.xcreate("Books", null);

        // Close database connection
        service.close();

        // Print created element
        System.out.println(KXmlUtils.nodeToString(el, true));
        System.out.println("ExampleDBRead OK");
    }

}