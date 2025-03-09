package mytests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.FUtils;
import org.xdef.util.XdefToXsd;
import org.xdef.xml.KXmlUtils;

public class Tomas {

	public static void main(String... args) throws Exception {
		String info =
"Tomas - tester of convertor of Xdefinition to XML Schema.\n" +
"Parameters:\n"+
" name of Xdefinition\n" +
" pathname of output directory (NOTE all files are deleted before execution)\n"+
" list of input source pathnames with Xdefinitions\n" +
" \"-XML\" pathname of xml data file (optional)";
		if (args == null || args.length < 3) {
			throw new RuntimeException("Arguments missing\n" + info);
		}
		List<String> argsList = new ArrayList<>();
		File xml = null;
		File outDir = null;
		String xdName = null;
		String outName = null;
		String root;
		boolean genXdateOutFormat = false;// Switch generate xdatatime outFormat
		List<String> xdSources = new ArrayList<>(); // Xdefinition source
		int i = 0;
		if (!args[0].startsWith("-")) {
			xml = new File(args[++i]);
			if (!xml.exists() || !xml.isFile()) {
				System.err.println("Error: expected -XML and XML file: "
					+ xml + "\n" + info);
				System.exit(1);
				return;
			}
		}
		for (;i < args.length; i++) {
			String arg = args[i];
			switch(arg) {
				case "-XML":
					if (i + 1 < args.length && !args[i+1].startsWith("-")) {
						xml = new File(args[++i]);
						if (!xml.exists() || !xml.isFile()) {
							System.err.println(
								"Error: incorrect XML file: "+xml+"\n"+info);
							System.exit(1);
							return;
						}
					} else {
						System.err.println(
							"Error: after \"-XML\" ios expected XML file\n"
							+ info);
						System.exit(1);
						return;
					}
					continue;
				case "-o":
				case "--outDir":
					argsList.add(arg);
					argsList.add(args[++i]);
					outDir = new File(args[i]);
					if (!outDir.exists() || !outDir.isDirectory()) {
						System.err.println("Error: expected -outDir directory"
							+ "\n" + info);
						System.exit(1);
						return;
					}
					continue;
				case "-i":
				case "--xdef":
					argsList.add(arg);
					for (;;) {
						String s = args[++i];
						if (!xdSources.contains(s)) {
							File f = new File(s);
							if (!f.exists() || !f.isFile()) {
								System.err.println(
									"Error: \""+s+"\" is not file\n" + info);
								System.exit(1);
								return;
							}
							xdSources.add(s);
							argsList.add(s);
						}
						if (i+1 >= args.length || args[i+1].startsWith("-")){
							break;
						}
					}
					continue;
				case "-x":
				case "--xdName":
					argsList.add(arg);
					argsList.add(xdName = args[++i]);
					continue;
				case "-s":
				case "--outName":
					argsList.add(arg);
					argsList.add(outName = args[++i]);
					continue;
				case "-r":
				case "--root":
					argsList.add(arg);
					argsList.add(root = args[++i]);
					continue;
				case "--xx":
					genXdateOutFormat = true;
				default:
					argsList.add(arg);
			}
		}
		if (xdSources.isEmpty()) {
			System.err.println("Error: missing Xdefinition file"+"\n"+info);
			System.exit(1);
			return;
		}
		if (outDir == null) {
			System.err.println("Error: missing outDir directory"+"\n"+info);
			System.exit(1);
			return;
		}
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
		XDPool xp = XDFactory.compileXD(props,xdSources.toArray(new String[0]));
		if (xdName == null) {
			xdName = "";
		}
		if (xp.getXMDefinition(xdName) == null) {
			System.err.println(
				"Error: missing Xdefinition \""+xdName+"\"\n"+info);
			System.exit(1);
			return;
		}
		if (outName == null) {
			outName = xdName.isEmpty() ? "Test" : xdName;
		}
		FUtils.deleteAll(outDir.listFiles(), false);
		XdefToXsd.main(argsList.toArray(new String[0]));
		if (xml != null) { // xml data
			try {
				xp = XDFactory.compileXD(null,xdSources.toArray(new String[0]));
			} catch (RuntimeException ex) {
				System.err.println("Error: Can't compile  XDPool:\n"
					+ ex.getMessage());
				System.exit(1);
				return;
			}
			XDDocument xd = xp.createXDDocument(xdName);
			// validate with Xdefinition and create processoed element
			Element el = xd.xparse(xml, null);
			if (!genXdateOutFormat) {
				el = KXmlUtils.parseXml(xml).getDocumentElement();
			}
			el.setAttribute("xmlns:xsi",
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			el.setAttribute("xsi:noNamespaceSchemaLocation", outName + ".xsd");
			KXmlUtils.writeXml(new File(outDir, outName + ".xml"),
				"UTF-8", el, true, true, 120);
			// validate with XML Schema
			Validator validator;
			File f = new File(outDir, outName + ".xsd");
			Source schemaSource = new StreamSource(f);
			Schema schema = SchemaFactory.newInstance(
				XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource);
			validator = schema.newValidator();
			f = new File(outDir, outName + ".xml");
			Source xmlSource = new StreamSource(f);
			validator.validate(xmlSource);
			System.out.println("XML schema generated and XML data tested OK");
		} else {
			System.out.println("XML schema generated OK");
		}
	}
}
