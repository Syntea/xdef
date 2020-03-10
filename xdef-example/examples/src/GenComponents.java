import java.io.File;
import java.io.FileOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.GenXComponent;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents {
	public static void main(String[] args) throws Exception {
		// 1. Compile all X-definitios from directory sec/data
		XDPool xdPool = XDFactory.compileXD(null,  //use System properties
			"src/Example_XC*.xdef");  // X-definition files

		// 2. save XDPool to the file
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
			"src/data/TownPool.xp"));
		os.writeObject(xdPool);
		os.close();

		// 3. generate X-components
		GenXComponent.genXComponent(xdPool,  // XDPool
			new File("temp").getAbsolutePath(),  // directory where to generate
			null);  // character set (default UTF-8)
	}
}