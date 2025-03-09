package mytests;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.xon.XonUtils;
import test.XDTester;

/** Test .properties and .ini files. */
public class IniTest extends XDTester {

	public IniTest() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		System.out.println("Xdefinition version: " + XDFactory.getXDVersion());
		String ini;
		File file;
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		Map<String, Object> x;
		XDPool xp;
		XDDocument xd;
		try	{
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"#this is INI file comment\n" +
"address=string(); options noTrimAttr\n" +
"dns = ipAddr()\n"  +
"name = string()\n"+
"  parser.factor.1=string()\n" +
"servertool.up=string()\n"+
"  </xd:ini>\n"  +
"</xd:def>";
			file = new File(clearTempDir(), "xp.xp");
			XDFactory.writeXDPool(file, compile(xdef));
			xp = XDFactory.readXDPool(file);
			xd = xp.createXDDocument("A");
			ini =
"#this is INI file comment\n" +
"address=dhcp\1\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.up=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
			x = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString((Map<String,Object>)x))));
			assertNoErrors(reporter);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type=int(0,9)\n" +
"hostaddr= ipAddr(); options acceptEmptyAttributes\n" + //
"port= int(0, 9999);\n" + //options acceptEmptyAttributes
"[system] %script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
			file = new File(clearTempDir(), "xp1.xp");
			XDFactory.writeXDPool(file, compile(xdef));
			xp = XDFactory.readXDPool(file);
//			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"hostaddr= 123.45.6.7\n" +
"port= 0\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			x = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString((Map<String,Object>)x))));
			assertNoErrors(reporter);
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"hostaddr= 123.45.6.7\n" +
"port= 0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			x = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString((Map<String,Object>)x))));
			assertNoErrors(reporter);
		} catch (IOException | RuntimeException ex) {fail(ex);}
	}

	public static void main (String[] args) throws Exception {
		if (runTest(args) > 0) {System.exit(1);}
  }
}