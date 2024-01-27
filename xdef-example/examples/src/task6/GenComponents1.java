package task6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents1 {
	public static void main(String... args) throws Exception {
		// 1. delete the directory with X-components
		File components = new File("src/task6/components1");
		if (components.exists()) {
			for (File x: components.listFiles()) {
				x.delete();
			}
			components.delete();
		}

		// 2. Compile X-definitions
		XDPool xPool = XDFactory.compileXD(null,  //use System properties
			"src/task6/townA.xdef",  // X-definition files
			"src/task6/townB.xdef"); 

		// 3. generate X-components
		xPool.genXComponent(new File("src/"), "UTF-8", false, false);
		System.out.println("X-components are generated to\n"
			+ components.getCanonicalPath());

		// 4. save XDPool to the file "src/task6/components/Town1.xp"
		ObjectOutputStream os = new ObjectOutputStream(
			new FileOutputStream(new File(components, "Town1.xp")));
		os.writeObject(xPool);
		os.close();
		System.out.println("XDPool is saved to\n"
			+ components.getCanonicalPath() + File.separator + "Town1.xp");
	}
}