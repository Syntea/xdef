package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import java.util.Properties;

/** Test default property "xdef_warning"s and values "true" and "false".
 * @author Vaclav Trojan
 */
public class TestX {
	public static boolean myErr(XXData x) {return false;}
	/** Run test.
	 * @param args not used.
	 */
	public static void main(String[] args) {
		Properties props = new Properties();
		XDPool xp;
		XDDocument xd;
		String xml;
		String s;
		String xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" name=\"X\" root=\"a\">\n"+
" <a a=\"list('x','y')\">\n"+
" </a>\n"+
"</xd:def>";
			xml = "<a a='y'></a>";
		try {
			xp = XDFactory.compileXD(props, xdef, TestX.class); // no property
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.contains("XDEF998")) {
				System.out.println("OK1");
			} else {
				ex.printStackTrace();
			}
		}
		try {
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true
			xp = XDFactory.compileXD(null, xdef, TestX.class);
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
		} catch (Exception ex) {
			s = ex.getMessage();
			if (s.contains("XDEF998")) {
				System.out.println("OK2");
			} else {
				ex.printStackTrace();
			}
		}
		try {
			props.setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
				XDConstants.XDPROPERTYVALUE_WARNINGS_FALSE); // false
			xp = XDFactory.compileXD(props, xdef, TestX.class);
			xd = xp.createXDDocument();
			xd.xparse(xml, null);
			System.out.println("OK3");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
