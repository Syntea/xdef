package task6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents1 {

	public static void main(String... args) throws IOException {
		// 1. clear and create the directory with X-components
		File components = new File("src/task6/components1");
		if (components.exists()) {
			for (File x: components.listFiles()) {
				x.delete();
			}
			components.delete();
		}
		components.mkdirs();

		// 2. Compile X-definitions
		XDPool xPool = XDFactory.compileXD(null,  //use System properties
			"src/task6/townA.xdef",
			"src/task6/townB.xdef"); 

		// 3. generate X-components City and House
		xPool.genXComponent(new File("src/"), "UTF-8", false, false);
		System.out.println("XComponents City and House are created to src/task6/components1");
		
		// 4. save XDPool to the file "src/task6/components1/Town1.xp"
		try (
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(components, "Town1.xp")))) {
			os.writeObject(xPool);
			System.out.println("XDPool saved to src/task6/components1/Town1.xp");
		}
		
		// 4. Generate xcomponent XCBindWith 
		xPool = XDFactory.compileXD(null, "src/task6/BindWith.xdef");
		xPool.genXComponent(new File("src/"), "UTF-8", false, false);
		System.out.println("XComponent XCBindWith is created to src/task6/components1");

		System.out.println("OK, task6.GenComponents1");
	}
}