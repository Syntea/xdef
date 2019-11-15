package test.xdef;

import org.xdef.sys.ArrayReporter;
import org.xdef.component.GenXComponent;
import org.xdef.XDPool;
import org.xdef.sys.FUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import buildtools.XDTester;

/** Generate X-components Java source files.
 * @author Vaclav Trojan
 */
public class TestXComponentsGen extends XDTester {

	@Override
	/** Run test and print error information. */
	public void test() {
		File f = new File("temp");
		f.mkdir();
		String dir = f.getAbsolutePath().replace('\\', '/');
		if (!dir.endsWith("/")) {
			dir += '/';
		}
		if (!f.isDirectory()) {
			System.err.println('\"' + dir + "\" is not directory");
			return;
		}
		File g = new File("test");
		if (!g.exists() || !g.isDirectory()) {
			g = new File("src/test/java");
			if (!g.isDirectory()) {
				throw new RuntimeException("Test directory is missing");
			}
		}
		// generate XCDPool from sources
		try {
			// ensure following classes are compiled!
			TestXComponents_C.class.getClass();
			TestXComponents_G.class.getClass();
			TestXComponents_Y04.class.getClass();
			TestXComponents_Y06Container.class.getClass();
			TestXComponents_Y06Domain.class.getClass();
			TestXComponents_Y06DomainContainer.class.getClass();
			TestXComponents_Y06XCDomain.class.getClass();
			TestXComponents_Y07Operation.class.getClass();
			String xcomponents = getDataDir() + "test/TestXComponentsGen.xdef";
			String xcomponents1 = getDataDir()+	"test/TestXComponent_Z.xdef";
			XDPool xp = compile(new String[] {xcomponents, xcomponents1});
			// generate XComponents from xp
			ArrayReporter reporter = GenXComponent.genXComponent(xp,
				dir, "UTF-8", false, true);
			reporter.checkAndThrowErrors();
			// should generate warnings on xdef Y19 and xdef Y20
			if (reporter.errors()
				|| !reporter.printToString().contains("XDEF360")
				|| !reporter.printToString().contains("Y19#A/B/B_1/C/B")
				|| !reporter.printToString().contains("Y20#")) {
				System.err.println(reporter.printToString());
			}
			// save XDPool object to the file
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
				dir + "test/xdef/component/Pool.xp"));
			os.writeObject(xp);
			os.close();
			// update components with generated files
			String msg = FUtils.updateDirectories(
				new File(f, "test/xdef/component"),
				new File(g, "test/xdef/component"),
				null, // all extensions
				true, // delete others
				true); // process subdirectories
			if (msg.isEmpty()) {
				System.out.println("X-component data was not changed");
			} else {
				System.out.println(msg);
				System.out.println("X-component data created");
			}
			FUtils.deleteAll(f, true); // delete temp directory
		} catch (Exception ex) {fail(ex);}
	}

	/** Generate XComponents from XDPool.
	 * @param args not used.
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(false);
		if (runTest(args) > 0) {System.exit(1);}
	}
}