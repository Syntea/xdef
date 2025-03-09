package mytests;

import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import test.XDTester;

/**
 * @author Vaclav Trojan
 */
public class ToDo1 extends XDTester {
	public ToDo1() {super();}

	private void test(XDPool xp, String json) {
		ArrayReporter reporter = new ArrayReporter();
		Object o;
		Element el;
		Object p = null;
		try {
			XDDocument xd = xp.createXDDocument();
			o = XonUtils.parseXON(json);
			el = XonUtils.xonToXml(o);
//			xml = KXmlUtils.nodeToString(el, true);
			p = xd.jparse(json, reporter);
			if (!XonUtils.xonEqual(o, p)) {
				System.err.println("*** JSON data:\n" + json +
					"\nresult XON:\n" + p +
					"\nXML:\n" + KXmlUtils.nodeToString(xd.getElement(), true)+
					"\nGenerated XML:\n" +
					KXmlUtils.nodeToString(XonUtils.xonToXml(o), true));
					if (reporter.errorWarnings()) {
						System.err.println((reporter.errors()?"\nerrors:\n" +
							reporter.printToString():""));
					}
				return;
			}
			if (reporter.errorWarnings()) {
				System.err.println("*** Errors;" + reporter.printToString() +
					"\nJSON data:\n" + json +
					"\nXML:\n" + KXmlUtils.nodeToString(xd.getElement(), true)+
					"\nGeneratedXML:\n" +
					KXmlUtils.nodeToString(XonUtils.iniToXml(o), true));
				return;
			}
			System.out.println("OK: " + json);
		} catch (RuntimeException ex) {
			System.err.println("*** " + ex + "\nJSON data:\n" + json);
			if (reporter.errors()) {
				System.err.println(reporter.printToString());
			}
			System.err.println("result JSON: " + p);
//			fail(printThrowable(ex) + "\nJSON data:\n" + json);
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		String xdef;
		XDPool xp;
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////
/*xx*/
		xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1'\n"+
"    xmlns:jx=\"http://www.xdef.org/xon/4.0/w\" root='jx:map'>\n" +
"  <jx:map xmlns:jx=\"http://www.xdef.org/xon/4.0/w\">\n" +
"    <xd:mixed xmlns:xd=\"http://www.xdef.org/xdef/4.1\">\n" +
"      <a val=\"string();\" xd:script=\"optional\"/>\n" +
"      <b xd:script=\"optional\">\n" +
"        <jx:map>\n" +
"          <a val=\"string();\" xd:script=\"optional\"/>\n" +
"        </jx:map>\n" +
"      </b>\n" +
"      <c xd:script=\"optional;\">\n" +
"        <jx:array xd:script=\"optional\">\n" +
"          <jx:item val=\"int();\" xd:script=\"optional\"/>\n" +
"          <jx:map>\n" +
"            <a val=\"int();\" xd:script=\"optional\"/>\n" +
"          </jx:map>\n" +
"        </jx:array>\n" +
"      </c>\n" +
"      <d xd:script=\"optional;\">\n" +
"        <jx:array>\n" +
"          <jx:map/>\n" +
"        </jx:array>\n" +
"      </d>\n" +
"    </xd:mixed>\n" +
"  </jx:map>\n" +
"</xd:def>";
		xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.1' root='network'>\n" +
"<xd:json name='network'>\n" +
"{\n" +
"  a: \"optional string();\"\n" +
"  b: {%script=\"optional\", a: \"optional string();\"}\n" +
"  c: [%script=\"optional\", \"optional int();\",\n" +
"                {%script=\"optional\", a: \"optional int();\"}]\n" +
"  d: [%script=\"optional\", {}]\n" +
"}\n" +
"</xd:json>\n" +
"</xd:def>";
		xp = compile(xdef);
//		xp.display();
		test(xp, "{ b: {} }");
		test(xp, "{ b: {a: \"x\"} }");
		test(xp, "{ c:[1, {a:2}] }");
		test(xp, "{ a:\"fda88\", b: {a: \"x\"} }");
		test(xp, "{ a:\"fda88\", c:[1, {a:2}] }");
		test(xp, "{ c:[1, {a:2}], b: {a: \"x\"} }");
		test(xp, "{ }");
		test(xp, "{ a:\"fda88\" }");
		test(xp, "{ c:[{a:2}] }");
		test(xp, "{ d: [ { } ] }");
		test(xp, "{ c:[1] }");//Required element 'map'is missing; column=6;
		test(xp, "{ b: {}, a:\"fda88\" }");//Not allowed element 'a'; column=7;
		test(xp, "{ b: {}, c:[1, {a:2}]}"); //Not allowed element 'c'; line=1; column=7;
		test(xp, "{ b: {a: \"x\"}, c:[1, {a:2}] }");//Not allowed element 'c'; line=1; column=7
		test(xp, "{ c:[1, {a:2}], b: {a: \"x\"}, a:\"fda88\" }"); //Not allowed element 'a' line=1; column=21;
		test(xp, "{ a:\"fda88\", b: {a: \"xyz\"}, c:[1, {a:2}] }");
	}

////////////////////////////////////////////////////////////////////////////////

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}