package bugreports;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xdef.util.xd2xsd.Xd2Xsd;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import static org.xdef.sys.STester.runTest;
import org.xdef.util.XdefToXsd;
import org.xdef.xml.KXmlUtils;
import org.xml.sax.SAXException;
import test.XDTester;

/** Test generation of XML schema from X-definition.
 * @author Vaclav Trojan
 */
public class TestXdXsd extends XDTester {
	public TestXdXsd() {super();}
////////////////////////////////////////////////////////////////////////////////
	private final static SchemaFactory XSDFACTORY =
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	/** Check XML data with SCHEMA.
	 * @param xsd XML schema file or string.
	 * @param xml XML data file or string.
	 * @return an empty string if validation os OK, otherwise return error
	 * message.
	 */
	private String checkXsd(final String xsd, final String xml) {
		File f;
		Validator validator;
		try {// create validator
			f = new File(xsd);
			Source schemaSource = f.exists() ? new StreamSource(f)
				: new StreamSource(new StringReader(xsd));
			Schema schema = XSDFACTORY.newSchema(schemaSource);
			validator = schema.newValidator();
		} catch (SAXException ex) {
			return xsd + "\n" + STester.printThrowable(ex);
		}
		if (xml != null) {
			String s = xml;
			try {//check by XML schema
				f = new File(s);
				Source xmlSource = f.exists() ? new StreamSource(f)
				: new StreamSource(new StringReader(xml));
				validator.validate(xmlSource);
				if (f.exists() && xml.endsWith(".xml")) {
					s = xml.substring(0, xml.length()-4);
					for (int i = 0; i < 10; i++) {
						f = new File(s + "_" + i + ".xml") ;
						if (f.exists()) {
							xmlSource = new StreamSource(f);
							validator.validate(xmlSource);
						}
					}
					s = xml.substring(0, xml.length()-4);
					for (int i = 0; i < 10; i++) {
						f = new File(s + "_e" + i + ".xml") ;
						if (f.exists()) {
							xmlSource = new StreamSource(f);
							try {
								validator.validate(xmlSource);
								return "Error not recognized: " + f.getName();
							} catch (IOException | SAXException ex) {
								// ok
							}
						}
					}
				}
			} catch (IOException | SAXException ex) {
				return "source: " + s + "\n" + STester.printThrowable(ex);
			}
		}
		return "";
	}

	/** Check XML data with X-definition.
	 * @param xdef X-definition file or string.
	 * @param xdName name of root X-definition.
	 * @param xml XML data file or string.
	 * @return an empty string if validation os OK, otherwise return error
	 * message.
	 */
	private String checkXdef(final String xdef,
		final String xdName,
		final String xml) {
		try {//check by X-definition
//			if (xml != null) {
//				XDPool xp = XDFactory.compileXD(null, xdef);
//				xp.createXDDocument(xdName).xparse(xml, null);
//			}
			return "";
		} catch (RuntimeException ex) {
			return "ERROR: " + STester.printThrowable(ex);
		}
	}

	/** Generate XML schema from X-definition and test it with xml data.
	 * @param xdef X-definition source or path name.
	 * @param xdName name of X-definition.
	 * @param modelName name model to generate.
	 * @param xml XML source or path name.
	 * @param outDir directory where to generate files.
	 * @param outName name of base XML schema file.
	 * @return empty string or error message.
	 */
	private String genAndTestSchema(final String xdef,
		final String xdName,
		final String modelName,
		final String xml,
		final String outDir,
		final String outName,
		final boolean outFormat) {
		try {
			String t = checkXdef(xdef, xdName, xml);
			if (!t.isEmpty() && !t.endsWith("\n")) {
				t += "\n";
			}
//			t = "";
			if (new File(xdef).exists()) {
				if (outFormat) {
						XdefToXsd.main(
						"--xdef", xdef,
						"--xdName", xdName,
						"--outDir", outDir,
						"--outName", outName,
						"--root", modelName,
						"--xx",
						"-v");
				} else {XdefToXsd.main(
					"--xdef", xdef,
					"--xdName", xdName,
					"--outDir", outDir,
					"--outName", outName,
					"--root", modelName,
					"-v");
				}
			} else {
				Properties props = new Properties();
				props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
					XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
				XDPool xp = XDFactory.compileXD(props, xdef);
				Map<String, Element> schemas = Xd2Xsd.genSchema(xp,
					xdName, modelName, outName, true, outFormat);
				for (String key: schemas.keySet()) {
					File f = new File(outDir, key + ".xsd");
					KXmlUtils.writeXml(f,
						"UTF-8", schemas.get(key), true, true, 110);
//System.out.println(f.getName() + ":");
//System.out.println(KXmlUtils.nodeToString(KXmlUtils.parseXml(f), true));
				}
			}
			File in = new File(outDir, outName + ".xsd");
			return t + checkXsd(in.getAbsolutePath(), xml);
		} catch (IOException | SRuntimeException ex) {
			return STester.printThrowable(ex);
		}
	}
////////////////////////////////////////////////////////////////////////////////
	/** Test all files in a directory.
	 * @param dir where are XML and XSD files.
	 * @return an empty string or error message.
	 */
	private String checkAll_X(final String dir) {
		String result = "";
		for (int i = 0; i < 100; i++) {
			String s = dir + "X" + (i<10?"0":"") + i;
			if (new File(s + ".xml").exists() &&
				new File(s + ".xsd").exists()) {
				String t = checkXsd(s + ".xsd", s + ".xml");
				if (!result.isEmpty()) {
					result += '\n';
				}
				if (!t.isEmpty()) {
					result += s + '\n' + t;
				}
			}
		}
		return result;
	}

	@Override
	/** Run test and display error information. */
	public void test() {
////////////////////////////////////////////////////////////////////////////////
//		boolean T = false; // if false, all tests are invoked
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, // xdef_warnings
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); // true | false
		String xdef, xml;
		String sourceDir = getSourceDir();
		String outDir = sourceDir + "xsd/";
		new File(outDir).mkdir();
////////////////////////////////////////////////////////////////////////////////
		try {
/**/
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Ab\" root=\"A\" >\n" +
"<xd:declaration scope = 'global'>\n" +
"  external method void test.xdef.TAB(String);\n"+
"</xd:declaration>\n" +
"  <A><B xd:script='*;' c='string(2,4) CHECK TAB(\"x\")'/></A>\n"+
//"  <A><B xd:script='*;' c='regex(\"\\\\d{4}\") AAND regex(\"\\\\d{4}\")'/></A>\n"+
"</xd:def>";
			xml ="<A><B c='123'/></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XX",true));

//			xdef =
//"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' xd:name='Ab' xd:root='A'\n" +
//"   xmlns=\"a.b\">\n" +
//" <A c=\"date('2014-01-01', '2017-01-01')\"\n" +
//"    d=\"? dec(2, 1)\"\n" +
//"    e=\"float; default '1.2'\"\n" +
//"    f=\"float; fixed '1.3'\">\n" +
//"  <e>float; default '1.4'</e>\n" +
//"  <f>float; fixed '1.5'</f>\n" +
//"</A>\n" +
//"</xd:def>";
//			xml ="<A xmlns=\"a.b\" c=\"2014-01-01\" f=\"1.3\"><e/><f/></A>";
//			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XX",true));
if(true)return;
/**/

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
"<A>\n" +
"  x()\n" +
"</A>\n" +
"</xd:def>";
			xml = "<A> A-a.b </A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XA",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<xd:declaration scope = 'global'>\n" +
"  type a NCName();\n" +
"</xd:declaration>\n" +
"<xd:declaration scope = 'local'>\n" +
"  type v a;\n" +
"  type x v;\n" +
"  type w string(4);\n" +
"  type z w;\n" +
"</xd:declaration>\n" +
"<A a='x'><B b='z'/></A>\n" +
"</xd:def>";
			xml = "<A a='ab'><B b='abcd'/></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XB",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<xd:declaration scope = 'global'>\n" +
"  type a enum('abc','123');\n" +
"</xd:declaration>\n" +
"<xd:declaration scope = 'global'>\n" +
"  type v a;\n" +
"  type x v;\n" +
"  type w string(4,*);\n" +
"  type z w;\n" +
"</xd:declaration>\n" +
"<A a='x()' b='? z()' c='int(1,2)' d='? date()'>\n" +
"  <B/>\n" +
"  <C>?xdatetime('d.M.yyyy')</C>\n" +
"</A>\n" +
"</xd:def>";
			xml = "<A a='123' b='abcde' c='1'><B/><C>19.1.2024</C></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XC",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<A>\n" +
"  <xd:sequence>\n" +
"    <B xd:script='?'/>\n" +
"    <C xd:script='*'/>\n" +
"  </xd:sequence>\n" +
"</A>\n" +
"</xd:def>";
			xml = "<A><B/><C/></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XD",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<A>\n" +
"  <xd:mixed xd:script='0..1' >\n" +
"    <B xd:script='?'/>\n" +
"    <C xd:script='2'/>\n" +
"  </xd:mixed>\n" +
"</A>\n" +
"</xd:def>";
			xml = "<A><C/><C/><B/></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XE",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A'>\n" +
"<A>\n" +
"  <xd:choice>\n" +
"    <B xd:script='?'/>\n" +
"    <C xd:script='*'/>\n" +
"  </xd:choice>\n" +
"</A>\n" +
"</xd:def>";
			xml = "<A><B/></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XF",true));
			xdef =
"<xd:def xmlns:xd = \"http://www.xdef.org/xdef/4.2\"\n" +
"        xmlns:c  = \"http://example.com/carInfo\"\n" +
"        xmlns:e  = \"http://example.com/eshopInfo\"\n" +
"        xd:name  =  \"Ab\"\n" +
"        xd:root  =  \"A\" >\n" +
"<A>\n" +
"  <c:Car xd:script='occurs +' e:from=\"required xdatetime('dd.MM.yyyy')\">\n" +
"    <c:VIN>required an()</c:VIN>\n" +
"    <c:Built>xdatetime(\"yyyy/MM\")</c:Built>\n" +
"    <c:Mark>required enum('Škoda','Audi','Mercedes-Benz','BMW')</c:Mark>\n" +
"    <e:Info xd:script=\"optional\">required string(1, 500)</e:Info>\n" +
"  </c:Car>\n" +
"</A>\n" +
"</xd:def>";
			xml =
"<A xmlns:car = \"http://example.com/carInfo\"\n" +
"      xmlns:shop = \"http://example.com/eshopInfo\" >\n" +
"    <car:Car shop:from = \"12.05.2008\">\n" +
"        <car:VIN>156AM587L5H1DF598Q</car:VIN>\n" +
"        <car:Built>2000/05</car:Built>\n" +
"        <car:Mark>BMW</car:Mark>\n" +
"        <shop:Info>Very good, but a little old car.</shop:Info>\n" +
"    </car:Car>\n" +
"    <car:Car shop:from = \"02.12.2010\">\n" +
"        <car:VIN>8GR5BD268P5E120BVZ</car:VIN>\n" +
"        <car:Built>2009/01</car:Built>\n" +
"        <car:Mark>Škoda</car:Mark>\n" +
"    </car:Car>\n" +
"</A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XG",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A' >\n" +
"<A>\n" +
"<xd:mixed>\n" +
" <Test1 xd:script=\"occurs *\">\n" +
"  string(%minLength='1',%maxLength='10',%pattern=['[A-Z][a-z]*','[A-Z]*'])\n" +
" </Test1>\n" +
" <Test2 xd:script=\"occurs *\">\n" +
"  list(%item=int(%minInclusive='1', %maxInclusive='10'))\n" +
" </Test2>\n" +
" <Test3 xd:script=\"occurs *\">\n" +
"   list(%item=int(%minInclusive='1', %maxInclusive='10'), %length='3')\n" +
" </Test3>\n" +
" <Test4 xd:script=\"occurs *\">\n" +
   "union(%item=[ int(%minInclusive='1', %maxInclusive='10'),\n" +
"                 string(%enumeration=['A', 'B', 'C'])\n" +
"               ])\n" +
" </Test4>\n" +
"</xd:mixed>\n" +
"</A>\n" +
"</xd:def>";
			xml =
"<A>\n" +
"    <Test1>Abcdef</Test1>\n" +
"    <Test1>A</Test1>\n" +
"    <Test2>1 2 3 4 5 6 7 8 9 10</Test2>\n" +
"    <Test3>4 5 10</Test3>\n" +
"    <Test4>A</Test4>\n" +
"    <Test4>5</Test4>\n" +
"    <Test4>C</Test4>\n" +
"    <Test4>10</Test4>\n" +
"</A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XH",true));
			xdef =
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' name='Ab' root='A' >\n" +
"<xd:declaration scope='global'>\n" +
"  type name string(1,100);\n" +
"  type addr string(1,100);\n" +
"  type city string(1,80);\n" +
"  type country string(1,80);\n" +
"  type text string(1,1000);\n" +
"  type quantity int(1,*);\n" +
"  type price decimal(0,*);\n" +
"</xd:declaration>\n" +
"<A orderid=\"num(2,10)\">\n" +
" <orderperson>name</orderperson>\n" +
"  <shipto>\n" +
"    <name>name</name>\n" +
"    <address>addr</address>\n" +
"    <city>city</city>\n" +
"    <country>country</country>\n" +
"  </shipto>\n" +
"  <item xd:script='+'>\n" +
"    <title>text</title>\n" +
"    <note xd:script='?'>? text</note>\n" +
"    <quantity>int(1,*)</quantity>\n" +
"    <price>price</price>\n" +
"  </item>\n" +
"</A>\n" +
"</xd:def>";
			xml =
"<A orderid=\"889923\">\n" +
"  <orderperson>John Smith</orderperson>\n" +
"  <shipto>\n" +
"    <name>Ola Nordmann</name>\n" +
"    <address>Langgt 23</address>\n" +
"    <city>4000 Stavanger</city>\n" +
"    <country>Norway</country>\n" +
"  </shipto>\n" +
"  <item>\n" +
"    <title>Empire Burlesque</title>\n" +
"    <note>Special Edition</note>\n" +
"    <quantity>1</quantity>\n" +
"    <price>10.90</price>\n" +
"  </item>\n" +
"  <item>\n" +
"    <title>Hide your heart</title>\n" +
"    <quantity>1</quantity>\n" +
"    <price>9.90</price>\n" +
"  </item>\n" +
"</A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XI",true));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Ab\" root=\"A\" >\n" +
"  <xd:declaration>\n"+
"  final BNFGrammar rr = new BNFGrammar('\n"+
"    WS     ::= [#9#10#13 ]*   /*skip white spaces*/\n"+
"    SEP    ::= WS \",\" WS    /*separator of values*/\n"+
"    LnPrd  ::= [1-9] | [1-4] [0-9]\n"+
"    Month  ::= [1-9] | [1] [0-2]\n"+
"    Months ::= Month ( SEP Month )*\n"+
"    YPrd   ::= LnPrd? \"Y\" \"(\" Months \")\"\n"+
"    MDay   ::= [1-9] | [1-2][0-9] | [3][0-1] | \"-1\"\n"+
"    MDays  ::= MDay (SEP MDay)*\n"+
"    MPrd   ::= LnPrd? \"M\" \"(\" MDays \")\"\n"+
"    WDay   ::= [0-7] | \"-1\"\n"+
"    WDays  ::= WDay (SEP WDay)*\n"+
"    WPrd   ::= LnPrd? WS \"W\" \"(\" WDays \")\"\n"+
"    TimeH  ::= [0-1][0-9] | [2][0-3]\n"+
"    TimeM  ::= [0-5] [0-9]\n"+
"    Time   ::= TimeH \":\" TimeM\n"+
"    Times  ::= Time (SEP Time)*\n"+
"    DPrd   ::= LnPrd? WS \"D\" \"(\" Times \")\"\n"+
"    HPrd   ::= LnPrd \"H\"\n"+
"    MinPrd ::= LnPrd \"Min\"\n"+
"    reccur ::= MinPrd? HPrd? DPrd? WPrd? MPrd? YPrd?'\n"+
");\n"+
"  </xd:declaration>\n"+
"  <A>BNF(rr, 'reccur');</A>\n"+
"</xd:def>";
			xml ="<A>49 D(10:00, 11:55) W(1, 2, 3, 4, 5, 6)</A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XJ",true));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Ab\" root=\"A\" >\n" +
"  <A><B xd:script='*; ref C'/></A>\n"+
"  <C c='? string()'>? string</C>\n"+
"</xd:def>";
			xml ="<A><B c='c'>d</B></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XK",true));
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.2\" name=\"Ab\" root=\"A\" >\n" +
"  <A><B xd:script='*; ref C'/></A>\n"+
"  <C xd:script='*; ref D'/>\n"+
"  <D xd:script='*; ref E'/>\n"+
"  <E c='? string()'>? string</E>\n"+
"</xd:def>";
			xml ="<A><B c='c'>d</B></A>";
			assertEq("",genAndTestSchema(xdef,"Ab","A",xml,outDir,"_XL",true));
		} catch (Exception ex) {fail(ex);}
		try {
			assertEq("", checkAll_X(sourceDir + "X/"));
		} catch (Exception ex) {fail(ex);}
		clearTempDir(); // delete temporary files.
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
