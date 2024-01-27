package task6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents2 {
	public static void main(String... args) throws Exception {
		// 1. delete the directory with X-components
		File components1 = new File("src/task6/components2");
		if (components1.exists()) {
			for (File x: components1.listFiles()) {
				x.delete();
			}
			components1.delete();
		}

		// 2. Compile the X-definitions
		XDPool xPool = XDFactory.compileXD(null,  //use System properties
			"src/task6/townA.xdef",  // X-definition files
			"src/task6/townC.xdef",
			"src/task6/townD.xdef");

		// 3. generate X-components
		xPool.genXComponent(new File("src/"), "UTF-8", false, false);
		System.out.println("X-components are generated to\n"
			+ components1.getCanonicalPath());

		// 4. save XDPool to the file "src/task6/components1/Town2.xp"
		ObjectOutputStream os = new ObjectOutputStream(
			new FileOutputStream(new File(components1, "Town2.xp")));
		os.writeObject(xPool);
		os.close();

		System.out.println("XDPool and X-components are generated to\n"
			+ components1.getCanonicalPath());
		System.out.println("XDPool is saved to\n"
			+ components1.getCanonicalPath() + File.separator + "Town2.xp");
	}
}