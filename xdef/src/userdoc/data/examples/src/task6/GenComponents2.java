package task6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents2 {
	public static void main(String... args) throws IOException {
		// 1. clear and create the directory with X-components
		File components1 = new File("src/task6/components2");
		if (components1.exists()) {
			for (File x: components1.listFiles()) {
				x.delete();
			}
			components1.delete();
		}
		components1.mkdirs();

		// 2. Compile the X-definitions
		XDPool xPool = XDFactory.compileXD(null,  //use System properties
			"src/task6/townA.xdef",  // X-definition files
			"src/task6/townC.xdef",
			"src/task6/townD.xdef");

		// 3. generate X-components
		xPool.genXComponent(new File("src/"), "UTF-8", false, false);
		System.out.println("XComponents Citizen, City, House, Preson and Tebabts are created to src/task6/components2");
	
		// 4. save XDPool to the file "src/task6/components2/Town2.xp"
		try (
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(components1, "Town2.xp")))) {
			os.writeObject(xPool);
			System.out.println("XDPool saved to src/task6/components2/Town2.xp ");
		}
		System.out.println("OK, task6.GenComponents2");
	}
}