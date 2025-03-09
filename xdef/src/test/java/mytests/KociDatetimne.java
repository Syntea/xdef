package mytests;

import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import static org.xdef.sys.STester.runTest;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Ukazka ydatetime. */
public class KociDatetimne extends XDTester {
	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' root='a'>\n"+
"  <a a=\"ydatetime('yyyy-MM-ddTHH:mm:ss[ZZ]', 'yyyy-MM-ddTHH:mm:ssZ');\"/>\n" +
"</xd:def>";
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_DEFAULTZONE, "CET");
		XDPool xp = XDFactory.compileXD(props, xdef);
		String xml = "<a a='2024-10-22T11:55:30'/>"; // zone NOT specified
		XDDocument xd = xp.createXDDocument();
		Element el = xd.xparse(xml, null);
		System.out.println(KXmlUtils.nodeToString(el));
		xml = "<a a='2024-10-22T11:55:30-03:30'/>"; // zone specified
		xd = xp.createXDDocument();
		el = xd.xparse(xml, null);
		System.out.println(KXmlUtils.nodeToString(el));
		xml = "<a a='2024-10-22T11:55:30Etc/GMT-14'/>"; // zone specified
		xd = xp.createXDDocument();
		el = xd.xparse(xml, null);
		System.out.println(KXmlUtils.nodeToString(el));
	}

	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
