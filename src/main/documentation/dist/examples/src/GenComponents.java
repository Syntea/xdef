import org.xdef.component.GenXComponent;

/** Compile X-definitions and create the class with compiled XDPool. */
public class GenComponents {
	public static void main(String[] args) throws Exception {
//		// 1. Compile all X-definitios from directory sec/data
//		XDPool xdPool = XDFactory.compileXD(null,  //use System properties
//			"src/data/*.xdef");  // X-definition files
//		// 2. save XDPool to the file
//		xdPool.writeXDPool(new File("src/data/XDPool.dat"));
//		// 3. generate X-components
//		GenXComponent.genXComponent(xdPool,  // XDPool
//		new File("src").getAbsolutePath(),  // directory where to generate
//			null);  // character set (default UTF-8)
//		
//		
		// In the directory "components" will be the Java class "Pool" 
		// containing compiled the XDPool the X-definitions and the generated
		// source files with X-components
		GenXComponent.main(new String[] {
			"-i", "src/Example_XC*.xdef", // X-definition source files
			"-x", "components.TownPool", // Generate Java class with compiled XDPool
			"-o", "src"}); // The directory where to generate X-components
	}
}
