package task1;

import org.xdef.XDFactory;
import org.xdef.XDPool;

public class Order1b_gen {

	public static void main(String[] args) throws Exception {
		// Compile X-definitions to XDPool
		XDPool xpool = XDFactory.compileXD(null, "src/task1/Order1.xdef");
		// Generate Java source class with the compiled XDPool
		XDFactory.genXDPoolClass(xpool, "src", "task1.Order1bXP", null);
	}
}