package test.xdef;

import org.xdef.sys.ArrayReporter;
import org.xdef.component.GenXComponent;
import org.xdef.component.XComponent;
import org.xdef.XDPool;
import org.xdef.proc.XXNode;
import org.xdef.sys.FUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import test.utils.XDTester;

/** Generate X-components Java source files.
 * @author Vaclav Trojan
 */
public class TestXComponentsGen extends XDTester {

	XComponent _X, _Y, _G;

	private String _XX;
	private int _flags;

	public TestXComponentsGen() {}

	public static void setXX(XXNode xx, String s) {
		if (xx.getXComponent() != null) {
			((TestXComponentsGen) xx.getXComponent())._XX = s;
		}
	}

	public static void genXC(XXNode xnode) {
		String name = xnode.getXXName();
		TestXComponentsGen xm = (TestXComponentsGen) xnode.getUserObject();
		if ("G".equals(name)) {
			xm._G = xnode.getXComponent();
		} else if ("XXX".equals(name)) {
			xm._X = xnode.getXComponent();
		} else if ("YYY".equals(name)) {
			xm._Y = xnode.getXComponent();
		} else {
			throw new RuntimeException("Unknown element:" + name);
		}
	}

	public String getXX() {return _XX;}
	public final void xSetFlags(final int flags) {_flags |= flags;}
	public final void xClearFlags(final int flags) {_flags &= ~flags;}
	public final boolean xCheckFlags(final int flags) {
		return (flags & _flags) == flags;
	}
	public final int xGetFlags() {return _flags;}

////////////////////////////////////////////////////////////////////////////////
	private final List<XComponent> _YYY = new ArrayList<XComponent>();
	private String _g;
	private String XD_NAME_g;
	private XComponent _XXX;

	public String getg() {return _g;}
	public void setg(String x) {_g = x + '_';}
	public XComponent getXXX() {return _XXX;}
	public void setXXX(XComponent x) {_XXX = x;}
	public List<XComponent> listOfYYY() {return _YYY;}
	public void setYYY(List<XComponent> x) {
		_YYY.clear();
		if (x != null) {_YYY.addAll(x);}
	}

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
			// force following classes to be compiled!
			TestXComponents_C.class.getClass();
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
				dir, "UTF-8", false, false, true);
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