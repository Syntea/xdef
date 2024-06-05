package bugreports;

import java.io.File;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.XsdToXdef;
import test.XDTester;

/**
 * @author Trojan
 */
public class A extends XDTester {
	@Override
	/** Run test and display error information. */
	public void test() {
		try {
			File dir = null;
			for (char c = 'C'; c <= 'Z'; c++) {
				dir = new File(c + ":/C1/tempx/Y");
				if (dir.exists() && dir.isDirectory()) {
					break;
				}
				dir = null;
			}
			if (dir == null) {
				throw new RuntimeException("Can't find tempx on flash");
			}
			File schemaFile = new File(dir, "_x_.xsd");
			File xdFile = new File(dir, "_x_.xdef");
			xdFile.delete();
			XsdToXdef.genCollection(schemaFile.getAbsolutePath(),
				xdFile.getAbsolutePath(), null, null);
			if (!xdFile.exists() || !xdFile.isFile()) {
				throw new RuntimeException("File " + xdFile + " not exists");
			}
			XDPool xp = XDFactory.compileXD(null, xdFile);
//			xp.displayCode();
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}