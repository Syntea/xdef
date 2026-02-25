package task5;

import java.io.File;
import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;

public class Network {
    private static String _error = "";
    private static int _itemCounter = 0; // number of items.

    /** Finally process parsed item of data.
     * @param xdata actually processed data item.
     */
    @SuppressWarnings("unchecked")
    public static void processItem(XXNode xdata) {
        Map<String, Object> o = (Map) xdata.getXon(); //get parsed XON object (java.util.Map)
        setItemCounter(getItemCounter() + 1); // increase itemsCounter
        if (!getError().isEmpty()) {
            System.err.println( "Error on item:\n" + o);
            setError("");
        }
        // Here you can process parsed item
        // TODO ...Here you can parsed item from network model
    }

    public static String getError() {return _error;}
    public static void setError(String x) {_error = x;}

    public static int getItemCounter() {return _itemCounter;}
    public static void setItemCounter(int x) {_itemCounter = x;}

    /** Read data from the file "src/resources/network-element-v2-20200903220717001.json"
     * and process each line as an item with JSON data.
     * @param args not used here.
     */
    public static void main(String... args) {
        // Compile X-definitions to XDPool.
        XDPool xp = XDFactory.compileXD(null, "src/task5/network.xdef" );
        // prepare XDDocument used for processing of data item with X-definitrion
        XDDocument xd = xp.createXDDocument("Network");

        // Create BufferedReader with data. Each line contains a JSON object with one item,
        File data = new File("task5/input/network.json");
        setItemCounter(0); // number of items (lines).
        ArrayReporter reporter = new ArrayReporter();
        long time = System.currentTimeMillis();
        xd.jparse(data, reporter); // Data is processed as the JSON array.
        if (reporter.errors()) {
            System.err.println("Error in Task5.Network\n" + reporter);
        } else {
            System.out.format("OK, %s, %d items, %4.2f seconds\n",
                "Task5.Network", getItemCounter(), (System.currentTimeMillis() - time)/1000.0D);
        }
    }

}