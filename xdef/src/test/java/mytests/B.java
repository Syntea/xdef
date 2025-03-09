package mytests;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.NullReportWriter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Test of bibliographic date from external disk.
 * @author Trojan
 */
public class B extends XDTester {
	public B() {super();}

	private static void test(final File xdFile, final File data)
		throws Exception {
		System.out.println("***** Test with data file \"" + data + "\"");
		if (!data.exists() || !data.isFile()) {
			System.err.println("Can't find archive file. on flash");
			return;
		}
		XDPool xp = XDFactory.compileXD(null, xdFile);
		System.out.println("Data file length: " + data.length() + ".");
		InputStream in = new FileInputStream(data);
		if (data.getName().endsWith(".gz")) {
			in=new GZIPInputStream(in);
		}
		XDDocument xd = xp.createXDDocument();
		long time = System.currentTimeMillis();
		try {
			xd.xparse(in, new NullReportWriter(true));
		} catch (Exception | Error ex) {
			System.err.println(ex);
		}
		System.out.println("Test with \"" + data + "\", "
			+ (((System.currentTimeMillis() - time) / 1000.0D))) ;
		in.close();
	}

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
				System.err.println(
					"Can't find directory \"/C1/tempx/Y\"");
				return;
			}
			System.out.println("Directory \"" + dir + "\" found.");
			File xdFile = new File(dir, "_x_.xdef");
			if (!xdFile.exists() || !xdFile.isFile()) {
				System.err.println("Can't find \"" + xdFile + "\".");
				return;
			}
			System.out.println("File \"" + xdFile + "\" found.");
			test(xdFile, new File(dir, "MARC21.xml"));
			test(xdFile, new File(dir,"dnb_all_dnbmarc_20240213-3.mrc.xml.gz"));
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
