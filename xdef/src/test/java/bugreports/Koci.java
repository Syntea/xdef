package bugreports;

import org.xdef.XDDocument;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** test create mode with recursive reference.
 * @author Trojan
 */
public class Koci extends XDTester {

	public Koci() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xml;
		XDDocument xd;
/**/
		try {// OK
			xd =  compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X a=\"string()\">\n" +
"      <X xd:script=\"0..; create from('X'); ref X\"/>\n" + /////
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null)); // error
		} catch (RuntimeException ex) {fail(ex);}
		try {// ERROR
			xd = compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <X a=\"string()\">\n" +
"      <X xd:script=\"0..; ref X\"/>\n" + /////
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<X a=\"a1\"><X a=\"a2\"/><X a=\"a3\"/></X>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("X", null)); // error
		} catch (RuntimeException ex) {fail(ex);}
/**/
		try {// OK
			xd = compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle>\n" +
//"     <Part xd:script=\"0..; create from('Part'); ref X\"/>\n" +
"     <Part xd:script=\"0..; ref X\"/>\n" +
"   </Vehicle>\n" +
"   <X name=\"string()\">\n" +
"      <Part xd:script=\"0..; create from('Part'); ref X\"/>\n" + ///////
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null));
		} catch (RuntimeException ex) {fail(ex);}
		try {// ERROR
			xd = compile(
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"0..; ref X\"/>\n" +
"   </Vehicle>\n" +
"   <X name=\"string()\">\n" +
"      <Part xd:script=\"0..; ref X\"/>\n" + ///////
"   </X>\n" +
"</xd:def>").createXDDocument();
			xml = "<Vehicle><Part name=\"a1\"><Part name=\"a2\"/><Part name=\"a3\"/></Part></Vehicle>";
			xd.setXDContext(xml);
			assertEq(xml, xd.xcreate("Vehicle", null)); // error?
		} catch (RuntimeException ex) {fail(ex);}
/**/
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}