package mytests;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.impl.XConstants;
import org.xdef.impl.XData;
import org.xdef.impl.XVariable;
import org.xdef.impl.compile.CompileBase;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMVariable;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.XdefToXsd;
import org.xdef.util.XsdToXdef;
import org.xdef.xml.KXmlUtils;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class MyTest0 extends XDTester {
	public MyTest0() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
////////////////////////////////////////////////////////////////////////////////

		XDPool xp;
		String xdef;

////////////////////////////////////////////////////////////////////////////////

		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
		try { // test XSD
			String xsd =
"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
"  <xs:complexType name=\"Unknown_type\">\n" +
"    <xs:attribute name=\"IdChanged\"\n" +
"      type=\"xs:long\"\n" +
"      use=\"required\"/>\n" +
"  </xs:complexType>\n" +
"  <xs:element name=\"M1\">\n" +
"    <xs:complexType>\n" +
"      <xs:sequence>\n" +
"        <xs:element name=\"Unknown\"\n" +
"          type=\"Unknown_type\"\n" +
"          minOccurs=\"0\"\n" +
"          maxOccurs=\"unbounded\"/>\n" +
"      </xs:sequence>\n" +
"      <xs:attribute name=\"Tax\"\n" +
"        type=\"xs:string\"\n" +
"        use=\"required\"/>\n" +
"      <xs:attribute name=\"Date\"\n" +
"        type=\"xs:string\"\n" +
"        use=\"required\"/>\n" +
"    </xs:complexType>\n" +
"  </xs:element>\n" +
"</xs:schema>";
			File f = new File("C:/tempx/test.xsd");
			System.out.println(xsd);
			FUtils.writeString(f, xsd);
			XsdToXdef.genCollection("C:/tempx/test.xsd", "xd", System.out);
		} catch (IOException | SException | RuntimeException ex) {fail(ex);}

/**/
		try { // test forget
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<xd:declaration scope = 'global'>\n" +
"  type a NCName();\n" +
"</xd:declaration>\n" +
"<xd:declaration scope = 'local'>\n" +
"  type v a;\n" +
"  type x v;\n" +
"  type w string(4,*);\n" +
"  type z w;\n" +
"</xd:declaration>\n" +
"<A a='? v'>\n" +
"  x()\n" +
"</A>\n" +
"</xd:def>";
			xp = XDFactory.compileXD(null, xdef);
			XMVariable[] vars = xp.getVariableTable().toArray();
			for (XMVariable v : vars) {
				if (v.getName().charAt(0) != '$') {
					XData wrk = new XData("$text", null, xp, XMTEXT);
					wrk._check = ((XVariable)v).getParseMethodAddr();
					XDParser parser = (XDParser) wrk.getParseMethod();
					System.out.println(v.getName()
						+ "; ref: " + ((XVariable) v).getKeyRefName()
						+ "; ref1: " + parser.getDeclaredName()
						+ ", " + ((char) v.getKind())
						+ "/" + CompileBase.getTypeName(v.getType()));
				}
			}
			Map<String, Element> xsds = XdefToXsd.genSchema(xp,
				"Ab", "A", "xx", null, true, true);
//				"D7_", "?type", "xx", true, true);
			for (String key: xsds.keySet()) {
				System.out.println("*** fileName: " + key + ".xsd ***\n"
					+ KXmlUtils.nodeToString(xsds.get(key), true));
			}
/**
			xdef = // nested declaration of type
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='D7_' root='A | B'>\n" +
"    <xd:declaration scope=\"global\">\n" +
"        type  cisloDN num(5);\n" +
"        type  cj      string(1,50);\n" +
"        type  plan    gamDate();\n" +
"        type  rokDN   gamYear();\n" +
"    </xd:declaration>\n" +
"    <A RokDN=\"rokDN()\" CisloDN=\"cisloDN()\"/>\n" +
"    <B xd:script=\"ref A\" C=\"cj()\" P=\"? plan()\"/>\n" +
"</xd:def>\n" +
"<xd:def name='D7_decl'>\n" +
"    <xd:declaration scope=\"global\">\n" +
"        type  rok  long(1800, 2200);\n" +
"        type  gamYear rok;\n" +
"        type  gamDate  xdatetime('yyyyMMdd');\n" +
"    </xd:declaration>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = XDFactory.compileXD(null, xdef);
			XMVariable[] vars = xp.getVariableTable().toArray();
			for (XMVariable v : vars) {
				if (v.getName().charAt(0) != '$') {
					XData wrk = new XData("$text", null, xp, XMTEXT);
					wrk._check = ((XVariable)v).getParseMethodAddr();
					XDParser parser = (XDParser) wrk.getParseMethod();
					System.out.println(v.getName()
						+ "; ref: " + ((XVariable) v).getKeyRefName()
						+ "; ref1: " + parser.getDeclaredName()
						+ ", " + ((char) v.getKind())
						+ "/" + CompileBase.getTypeName(v.getType()));
				}
			}
			Map<String, Element> xsds = XdefToXsd.genSchema(xp,
				"D7_", "A", "xx", true, true);
//				"D7_", "?type", "xx", true, true);
			for (String key: xsds.keySet()) {
				System.out.println("*** fileName: " + key + ".xsd ***\n"
					+ KXmlUtils.nodeToString(xsds.get(key), true));
			}
/**/
		} catch (RuntimeException ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
