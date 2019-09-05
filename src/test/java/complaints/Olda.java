package complaints;

import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.proc.XXData;
import org.xdef.sys.ArrayReporter;
import java.io.StringWriter;
import org.w3c.dom.Element;
import test.utils.XDTester;

public class Olda extends XDTester {

	public Olda() {super();}

	public static Long getIdDefPartnerLong(XXData chkEl) {
		return null;
	}
	public static String getIdDefPartnerString(XXData chkEl) {
		return null;
	}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xml;
		String xdef;
		XDDocument xd;
		XDPool xp;
		Element el;
		String s;
		StringWriter strw;
		setProperty("xdef.warnings", "true");
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "'>\n" +
"  <xd:declaration>\n"+				
"  type x long(1, 1000);\n"+				
"  </xd:declaration>\n"+				
"<A a='optional long(); create getIdDefPartnerLong()'\n" +
"   b='optional string(); create getIdDefPartnerString()' />\n"+
"</xd:def>";
			xp = compile(xdef, this.getClass());
			xp.display();
//			System.out.println(xp.getXMDefinition("").getModel(null, "A").getAttr("Long").getParseMethod());
			el = create(xp, "", "A", reporter, null);
			assertNoErrors(reporter);
			assertEq(el,"<A/>");
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}

}
