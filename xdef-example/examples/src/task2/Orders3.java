package task2;

import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;

public class Orders3 {
	public static void main(String[] args) throws Exception {
		// Compile X-definition to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task2/Orders3.xdef");

		// Create the instance of XDDocument object (from XDPool)
		XDDocument xdoc = xpool.createXDDocument("Orders");

		// create instasnce of Orders3ext
		Orders3ext writer = new Orders3ext("task2/output/Orders.xml",
			"task2/errors/Orders_err.xml");
		xdoc.setUserObject(writer);

		// Run validation mode (you can also try task2/input/Order_err.xml)
		xdoc.xparse("task2/input/Orders.xml", null);

		// close all streams
		writer.closeAll();

		if (writer.errNum() != 0) {
			System.err.println("Incorrect input data");
		} else {
			System.out.println("OK");
		}
	}
}