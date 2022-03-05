package test.xdef;

import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test YAML.
 * @author Vaclav Trojan
 */
public class TestYamlAndIni extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		String xdef;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		try { // test YAML
			org.xdef.xon.XonYaml.prepareYAML();
			try {
				xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:xon name=\"test\">\n" +
"    { \"date\" : \"date(); finally\n" +
"                    outln('Measured on: ' + getText() + '\\n');\",\n" +
"      \"cities\"  : [\n" +
"        {$script = \"occurs 1..*; finally outln();\",\n" +
"          \"from\": [\n" +
"            \"string(); finally outln('From ' + getText());\",\n" +
"            {$script = \"occurs 1..*; finally outln();\",\n" +
"              \"to\": \"jstring(); finally \n" +
"                         out(' to ' + getText() + ' is distance: ');\",\n" +
"              \"distance\": \"int(); finally out(getText() + ' (km)');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  </xd:xon>\n" +
"</xd:def>";
				xp = compile(xdef);
				xd = xp.createXDDocument("A");
				String yaml =
"date: '2020-02-22'\n" +
"cities:\n" +
"- from:\n" +
"  - Brussels\n" +
"  - {to: London, distance: 322}\n" +
"  - {to: Paris, distance: 265}\n" +
"- from:\n" +
"  - London\n" +
"  - {to: Brussels, distance: 322}\n" +
"  - {to: Paris, distance: 344}\n";
				Object xon = xd.yparse(yaml, reporter);
				assertNoErrors(reporter);
				reporter.clear();
				assertTrue(XonUtils.xonEqual(xon,
					xd.yparse(XonUtils.toYamlString(xon), reporter)));
				assertNoErrors(reporter);
				reporter.clear();
			} catch (Exception ex) {fail(ex);}
		} catch (SRuntimeException exx) {
			setResultInfo("YAML tests skipped: the package "
				+ "org.yaml.snakeyaml is not available");
		}
		try { // test Windows INI
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:ini xd:name = \"test\">\n" +
"    name = string();\n" +
"    date = date();\n" +
"    email = ? emailAddr();\n" +
"    [Server]\n" +
"    IPAddr = ? ipAddr();\n" +
"  </xd:ini>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			String ini =
"date = 2021-02-03\n"+
"name = Jan Novak\n"+
"[Server]";
			Map<String, Object> xini = xd.iparse(ini, reporter);
			assertNoErrors(reporter);
			reporter.clear();
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
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
			xd = compile(xdef).createXDDocument("A");
			ini =
"#this is INI file comment\n" +
"address=dhcp\1\n" +
"dns = 192.168.1.1\n"  +
"name = John E\\\n"+
" . \\\n"  +
" Smith\n"  +
"  parser.factor.1=')' \\u00E9 esperado.\n" +
"servertool.up=\\u670D\\u52A1\\u5668\\u5DF2\\u5728\\u8FD0\\u884C\\u3002";
			xini = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			assertNoErrors(reporter);
			reporter.clear();
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name=\"A\" root=\"test\">\n" +
"  <xd:ini name=\"test\">\n" +
"proxy type=int(0,9)\n" +
"hostaddr= ? ipAddr(); options acceptEmptyAttributes\n" + //
"port= ? int(0, 9999);\n" +
"[system] $script = optional\n" +
"autolaunch=int()\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=ipAddr()\n" +
"  </xd:ini>\n"  +
"</xd:def>";
			xd = compile(xdef).createXDDocument("A");
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
			xini = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			assertNoErrors(reporter);
			reporter.clear();
			ini =
"proxy type=0\n" +
"hostaddr=\n" +
"[system]\n" +
"autolaunch=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			assertNoErrors(reporter);
			reporter.clear();
			ini =
"proxy type=0\n" +
"[ x.y ]\n" +
"[selfupdate]\n" +
"version=11.0.0.55";
			xini = xd.iparse(ini, reporter);
			assertTrue(XonUtils.xonEqual(XonUtils.parseINI(ini),
				XonUtils.parseINI(XonUtils.toIniString(xini))));
			assertNoErrors(reporter);
		} catch (Exception ex) {fail(ex);}

		clearTempDir(); // clear temporary directory
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}