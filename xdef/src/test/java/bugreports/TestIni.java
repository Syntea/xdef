package bugreports;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.util.Properties;
import org.xdef.sys.ArrayReporter;
import test.XDTester;

/** Tests used for development..
 * @author Vaclav Trojan
 */
public class TestIni extends XDTester {

	public TestIni() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
		XDPool xp;
		XDDocument xd;
		String ini;
		Map<String, Object> map;
		String xdef;
		InputStream in;
		Properties props = new Properties();
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" name=\"X\" root=\"a\">\n"+
" <xd:ini name='a'>\n"+
"   A=?string()\n" +
"   B=int()\n" +
"   C=date()\n" +
"   D=decimal()\n" +
"   [E; $script=?]\n" +
"     x = ?int()\n" +
"   [F]\n" +
" </xd:ini>\n"+
"</xd:def>";
			xp = XDFactory.compileXD(props, xdef); // no property
			xd = xp.createXDDocument();
			ini = "A=a\n B = 1\n C=2121-10-19\n D=2.121\n[E]\nx=123\n[F]";
			map = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			in = new ByteArrayInputStream(ini.getBytes());
			map = xd.iparse(in, reporter);
			assertNoErrors(reporter);
			ini = "\n B = 1 \n C=2121-10-19\n D=2.121\n [E] \n[F]";
			map = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			in = new ByteArrayInputStream(ini.getBytes());
			map = xd.iparse(in, reporter);
			assertNoErrors(reporter);
			map = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			in = new ByteArrayInputStream(ini.getBytes());
			map = xd.iparse(in, reporter);
			assertNoErrors(reporter);
			ini = "\n B = 1 \n C=2121-10-19\n D=2.121\n[F]";
			map = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			in = new ByteArrayInputStream(ini.getBytes());
			map = xd.iparse(in, reporter);
			assertNoErrors(reporter);
			map = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			in = new ByteArrayInputStream(ini.getBytes());
			map = xd.iparse(in, reporter);
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}