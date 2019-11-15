package complaints;

import org.xdef.sys.ArrayReporter;
import org.xdef.XDPool;
import buildtools.XDTester;

public class Lubor extends XDTester {

	public Lubor() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		String xdef;
		String xml;
		XDPool xp;
		try {
			xdef =
"<xd:def xmlns:xd='http://www.syntea.cz/xdef/3.1' name='A' root='A | B'>\n"+
"   <A xd:script='ref C' >\n"+
"      <X/>\n"+
"   </A>\n"+
"   <B xd:script='ref C' />\n"+
"   <C c='string'>\n"+
"     <D xd:script='occurs 0..1' d='string'/>\n"+
"   </C>\n"+
"   <D D='string'>\n"+
"     <D/>\n"+
"   </D>\n"+
"<xd:component>\n"+
"  %class complaints.data.C %link A#C;\n"+
"  %class complaints.data.B extends complaints.data.C %link A#B;\n"+
"  %class complaints.data.A extends complaints.data.C %link A#A;\n"+
//"  %class complaints.data.B %link A#B;\n"+
//"  %class complaints.data.A %link A#A;\n"+
"  %class complaints.data.D %link A#D;\n"+
"</xd:component>\n"+
"</xd:def>";
			xp = compile(xdef);
			// Generate X-components
			reporter = org.xdef.component.GenXComponent.genXComponent(xp,
				"src/test/java", null, false, true);
/*xx*/
			if (reporter.errorWarnings()) {
				System.out.println(reporter.printToString());
			}
			xml = "<A c='c'><D d='d'/><X/></A>";
			complaints.data.A p = (complaints.data.A)
				parseXC(xp,"A", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, p.toXml());
			xml = "<B c='c'><D d='d'/></B>";
			complaints.data.B q = (complaints.data.B)
				parseXC(xp,"A", xml , null, reporter);
			assertNoErrorwarnings(reporter);
			assertEq(xml, q.toXml());
/*xx*/
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		runTest();
	}
}
