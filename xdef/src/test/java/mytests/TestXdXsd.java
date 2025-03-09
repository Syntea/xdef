package mytests;

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
import org.w3c.dom.DOMException;
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

/** Test generation of XML schema from Xdefinition.
 * @author Vaclav Trojan
 */
public class TestXdXsd extends XDTester {

	public TestXdXsd() {super();}

	private final static SchemaFactory XSDFACTORY =
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	/** Check XML data with SCHEMA.
	 * @param xsd XML schema file or string.
	 * @param xml XML data file or string.
	 * @return an empty string if validation os OK, otherwise return error message.
	 */
	private String checkXsd(final String xsd, final String xml) {
		File f;
		Validator validator;
		try {// create validator
			f = new File(xsd);
			Source schemaSource = f.exists() ? new StreamSource(f) : new StreamSource(new StringReader(xsd));
			Schema schema = XSDFACTORY.newSchema(schemaSource);
			validator = schema.newValidator();
		} catch (SAXException ex) {return xsd + "\n" + STester.printThrowable(ex);}
		if (xml != null) {
			String s = xml;
			try {//check by XML schema
				f = new File(s);
				Source xmlSource = f.exists() ? new StreamSource(f) : new StreamSource(new StringReader(xml));
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
	/** Check XML data with Xdefinition.
	 * @param xdef Xdefinition file or string.
	 * @param xdName name of root Xdefinition.
	 * @param xml XML data file or string.
	 * @return an empty string if validation os OK, otherwise return error message.
	 */
	private String checkXdef(final String xdef, final String xdName, final String xml) {
		try {//check by Xdefinition
			if (xml != null) {
				XDPool xp = XDFactory.compileXD(null, xdef);
				xp.createXDDocument(xdName).xparse(xml, null);
			}
			return "";
		} catch (RuntimeException ex) {
			return "ERROR: " + STester.printThrowable(ex);
		}
	}
	/** Generate XML schema from Xdefinition and test it with xml data.
	 * @param xdef Xdefinition source or path name.
	 * @param xdName name of Xdefinition.
	 * @param modelName name model to generate.
	 * @param xml XML source or path name.
	 * @param outDir directory where to generate files.
	 * @param outName name of base XML schema file.
	 * @param outType name of XML schema file with type declarations.
	 * @return empty string or error message.
	 */
	private String genAndTestSchema(final String xdef,
		final String xdName,
		final String modelName,
		final String xml,
		final String outDir,
		final String outName,
		final String outType,
		final boolean outFormat) {
		try {
			String t = checkXdef(xdef, xdName, xml);
			if (!t.isEmpty() && !t.endsWith("\n")) {
				t += "\n";
			}
			if (new File(xdef).exists()) {
				if (outFormat) {
					XdefToXsd.main("--xdef", xdef,
						"--xdName", xdName,
						"--outDir", outDir,
						"--outName", outName,
						"--outType", outType,
						"--root", modelName,
						"--xx",//use output format of xdatetime method for type
						"-v");
				} else {
					XdefToXsd.main("--xdef", xdef,
						"--xdName", xdName,
						"--outDir", outDir,
						"--outName", outName,
						"--outType", outType,
						"--root", modelName,
						"-v");
				}
			} else {
				Properties props = new Properties();
				props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
					XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
				XDPool xp = XDFactory.compileXD(props, xdef);
				Map<String, Element> schemas = Xd2Xsd.genSchema(xp, xdName,
					modelName, outName, outType, true, outFormat);
				for (String key: schemas.keySet()) {
					File f = new File(outDir, key + ".xsd");
					KXmlUtils.writeXml(f,
						"UTF-8", schemas.get(key), true, true, 110);
				}
			}
			File in = new File(outDir, outName + ".xsd");
			return t + checkXsd(in.getAbsolutePath(), xml);
		} catch (IOException | SRuntimeException ex) {return STester.printThrowable(ex);}
	}
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

	/** Generate XML schema from Xdefinition and test it with xml data.
	 * @param outName name of base XML schema file.
	 * @return empty string or error message.
	 */
	private String genAndTestSchema(final String outName) {
		String outdir = getSourceDir() + "xsd/";
		String xdef = outdir + outName + ".xdef";
		String xml = outdir + outName + ".xml";
		return genAndTestSchema(xdef, "Ab", "A", xml, outdir, outName, "",true);
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
		String sourceDir = getSourceDir();
		String outDir = sourceDir + "xsd/";
////////////////////////////////////////////////////////////////////////////////
		try {
			assertEq("", genAndTestSchema("_XA"));
			assertEq("", genAndTestSchema("_XB"));
			assertEq("", genAndTestSchema("_XC"));
			assertEq("", genAndTestSchema("_XD"));
			assertEq("", genAndTestSchema("_XE"));
			assertEq("", genAndTestSchema("_XF"));
			assertEq("", genAndTestSchema("_XG"));
			assertEq("", genAndTestSchema("_XH"));
			assertEq("", genAndTestSchema("_XI"));
			assertEq("", genAndTestSchema("_XJ"));
			assertEq("", genAndTestSchema("_XK"));
			assertEq("", genAndTestSchema("_XL"));

			Element el = parse(sourceDir+"P1A/SouborP1A.xdef", "SouborP1A", sourceDir+"P1A/P1A.xml");
			el.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			el.setAttribute("xsi:noNamespaceSchemaLocation", "_XM.xsd");
			KXmlUtils.writeXml(new File(outDir+"_XM.xml"), "UTF-8", el, true, true, 120);
			assertEq("",
				genAndTestSchema(sourceDir+"P1A/SouborP1A.xdef",
				"SouborP1A",
				null,
				outDir+"_XM.xml",
				outDir,
				"_XM",
				"_XMTypeDeclaration",
				true));

			assertEq("",
				genAndTestSchema(sourceDir+"D1A/SouborD1A.xdef",
				"SouborD1A",
				null,
				sourceDir+"D1A/D1A.xml",
				outDir,
				"_XN",
				"_XNTypes",
				false));

			// generate only XML schema with types to the file "_XN_T.xsd".
			XdefToXsd.main("--xdef", sourceDir+"D1A/SouborD1A.xdef",
				"--xdName", "SouborD1A",
				"--outDir", outDir,
				"--outType", "_XN_T",
				"--xx",
				"-v");
			assertEq("", checkAll_X(sourceDir + "X/"));
		} catch (IOException | DOMException ex) {fail(ex);}

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
