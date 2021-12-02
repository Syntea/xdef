package test.xdef;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtil;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test YAML.
 * @author Vaclav Trojan
 */
public class TestYaml extends XDTester {

	@Override
	/** Run test and display error information. */
	public void test() {
		try {
			org.xdef.xon.XonYaml.prepareYAML();
		} catch (SRuntimeException ex) {
			setResultInfo("YAML tests skipped: package "
				+ "org.yaml.snakeyaml is not available");
			return;			
		}
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
		String yaml;
		XDDocument xd;
		XDPool xp;
		ArrayReporter reporter = new ArrayReporter();
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='test' name='A'>\n"+
"  <xd:json name=\"test\">\n" +
"    { \"date\" : \"date(); finally outln('Measured on: ' + getText() + '\\n');\",\n" +
"      \"cities\"  : [\n" +
"        {$script = \"occurs 1..*; finally outln();\",\n" +
"          \"from\": [\n" +
"            \"string(); finally outln('From ' + getText());\",\n" +
"            {$script = \"occurs 1..*; finally outln();\",\n" +
"              \"to\": \"jstring(); finally out(' to ' + getText() + ' is distance: ');\",\n" +
"              \"distance\": \"int(); finally out(getText() + ' (km)');\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }\n" +
"  </xd:json>\n" +
"</xd:def>";
			xp = compile(xdef);
			xd = xp.createXDDocument("A");
			yaml =
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
			assertTrue(XonUtil.xonEqual(xon, 
				xd.yparse(XonUtil.toYamlString(xon), reporter)));
			assertNoErrors(reporter);
			reporter.clear();
		} catch (Exception ex) {fail(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}