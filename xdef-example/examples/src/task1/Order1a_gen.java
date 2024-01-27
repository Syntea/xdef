package task1;

import java.io.FileOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.ObjectOutputStream;
import java.util.Properties;

public class Order1a_gen {

	public static void main(String... args) throws Exception {
		// Compile the XDPool from the X-definition source file
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task1/Order1.xdef");
		// Write the XDPool object to the file
		ObjectOutputStream outstr = new ObjectOutputStream(
			new FileOutputStream("task1/output/Order1a.xp"));
		outstr.writeObject(xpool);
		outstr.close();
	}
}