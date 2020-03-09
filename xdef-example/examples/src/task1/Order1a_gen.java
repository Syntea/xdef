package task1;

import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

public class Order1a_gen {

	public static void main(String[] args) throws Exception {
		// Compile XDPool from the X-definition
		Properties props = new Properties();
		XDPool xpool = XDFactory.compileXD(props, "src/task1/Order1.xdef");
		// Write XDPool to the file
		ObjectOutputStream outstr= new ObjectOutputStream(
			new FileOutputStream("src/task1/Order1a.xp"));
		outstr.writeObject(xpool);
		outstr.close();
	}
}