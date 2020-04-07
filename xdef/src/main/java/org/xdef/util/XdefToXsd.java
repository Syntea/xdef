package org.xdef.util;

import org.xdef.XDConstants;
import org.xdef.msg.XDEF;
import org.xdef.impl.util.conv.Util;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.conv.xd2xsd.Convertor;
import org.xdef.impl.util.conv.xd.doc.XdDoc;
import org.xdef.impl.util.conv.xsd.doc.XsdVersion;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.PrintStream;
import org.xdef.impl.XPool;

/** Convertor of X-definition to XML Schema.
 * (see {@link org.xdef.util.XdefToXsd#main(String[])})
 * @author Ilia Alexandrov
 */
public final class XdefToXsd {
	private static final XsdVersion SCHEMA_VERSION = XsdVersion.SCHEMA_1_0;

	/** Creates instance of X-definition to XML Schema convertor with default
	 * values.
	 */
	private XdefToXsd() {}

	/** Generates XML Schema from given X-definition file or as string and
	 * saves files to given output directory.
	 * @param xdef X-definition file or X-definition as string.
	 * @param outputDir output schema files directory.
	 * @param schemaPrefix prefix of XML schema namespace.
	 * @param schemaFileExt file extension of schema files.
	 * @param out where print information messages (if null System.out).
	 */
	public final static void genSchema(final String xdef,
		final String outputDir,
		final String schemaPrefix,
		final String schemaFileExt,
		final PrintStream out) {
		XdDoc xdDoc;
		SReporter reporter = new SReporter(new FileReportWriter(
			out == null ? System.out : out));
		String sPrefix = schemaPrefix == null ? "xs" : schemaPrefix;
		String sFileExt = schemaFileExt == null ? "xsd" : schemaFileExt;
		try {
			xdDoc = XdDoc.getXdDoc(xdef, reporter, false);
		} catch (Exception ex) {
			//Can not create X-definition&{0}{ }
			throw new SRuntimeException(XDEF.XDEF713, ex);
		}
		Convertor convertor;
		try {
			convertor = Convertor.getConvertor(
				xdDoc, SCHEMA_VERSION, sPrefix, reporter, sFileExt);
		} catch (Exception ex) {
			//Can not create convertor&{0}{ }
			throw new SRuntimeException(XDEF.XDEF714, ex);
		}
		Util.printSchemas(convertor.getSchemaDocuments(), outputDir);
	}

	/** Generates XML Schema from given X-definition and returns map of schema
	 * file names and DOM Documents.
	 * @param xdef X-definition file or X-definition as string.
	 * @param schemaPrefix prefix of XML schema namespace.
	 * @param schemaFileExt file extension of schema files.
	 * @param out where print information messages (if null System.out).
	 * @return map of schema file names and DOM Documents.
	 */
	public final static Map<String, Document> genSchema(final String xdef,
		final String schemaPrefix,
		final String schemaFileExt,
		final PrintStream out) {
		SReporter reporter = new SReporter(new FileReportWriter(
			out == null ? System.out : out));
		String sPrefix = schemaPrefix == null ? "xs" : schemaPrefix;
		String sFileExt = schemaFileExt == null ? "xsd" : schemaFileExt;
		XdDoc xdDoc = XdDoc.getXdDoc(xdef, reporter, false);
		Convertor convertor = Convertor.getConvertor(
			xdDoc, SCHEMA_VERSION, sPrefix, reporter, sFileExt);
		return convertor.getSchemaDocuments();
	}

	/** Generates XML Schema from given X-definition file and saves schema
	 * files to given output directory.
	 * @param xdef X-definition file.
	 * @param outputDir output schema files directory.
	 * @param schemaPrefix prefix of XML schema namespace.
	 * @param schemaFileExt file extension of schema files.
	 * @param out where print information messages (if null System.out).
	 */
	public final static void genSchema(final File xdef,
		final String outputDir,
		final String schemaPrefix,
		final String schemaFileExt,
		final PrintStream out) {
		genSchema(xdef.getAbsolutePath(),
			outputDir, schemaPrefix, schemaFileExt, out);
	}

	/** Generates XML Schema from given X-definition files and saves schema
	 * files to given output directory.
	 * @param xdefs X-definition file.
	 * @param outputDir output schema files directory.
	 * @param xdName name of X-definition. May be null, then the nameless
	 * X-definition or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param schemaPrefix prefix of XML schema namespace.
	 * @param schemaFileExt file extension of schema files.
	 * @param out where print information messages (if null System.out).
	 * @throws Exception if an error occurs.
	 */
	public final static void genSchema(final File[] xdefs,
		final String outputDir,
		final String xdName,
		final String model,
		final String schemaPrefix,
		final String schemaFileExt,
		final PrintStream out) throws Exception {
		String[] srcs = new String[xdefs.length];
		for (int i = 0; i < xdefs.length; i++) {
			File f = xdefs[i];
			srcs[i] = f.getCanonicalPath();
		}
		genSchema(srcs,
			outputDir, xdName, model,schemaPrefix, schemaFileExt, out);
	}

	/** Generates XML Schema from given X-definition file names and saves schema
	 * files to given output directory.
	 * @param xdefs X-definition file.
	 * @param outputDir output schema files directory.
	 * @param xdName name of X-definition. May be null, then the nameless
	 * X-definition or the first one X-definition is used.
	 * @param model name of model of X-definition to used. May be null, then
	 * the value from "xs:root" parameter is used to create models.
	 * @param schemaPrefix prefix of XML schema namespace.
	 * @param schemaFileExt file extension of schema files.
	 * @param out where print information messages (if null System.out).
	 * @throws Exception if an error occurs.
	 */
	public final static void genSchema(final String[] xdefs,
		final String outputDir,
		final String xdName,
		final String model,
		final String schemaPrefix,
		final String schemaFileExt,
		final PrintStream out) throws Exception {
		Element collection;
		String xdMode = model;
		if (xdefs.length == 1) {
			collection = KXmlUtils.parseXml(xdefs[0]).getDocumentElement();
		} else {
			collection = GenCollection.genCollection(xdefs, true, true, true);
		}
		if (xdName != null) {
			NodeList nl = KXmlUtils.getChildElementsNS(collection,
				XDConstants.XDEF_INSTANCE_NS_URI, "xdef");
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				Attr a = el.getAttributeNode("name");
				String name = "";
				if (a != null) {
					name = a.getValue();
				} else {
					a = el.getAttributeNodeNS(
						XDConstants.XDEF_INSTANCE_NS_URI, "name");
					if (a != null) {
						name = a.getValue();
					}
				}
				if (!name.equals(xdName)) {
					continue;
				}
				if (xdMode == null) {
					xdMode = xdName;
				}
				a = el.getAttributeNode("root");
				if (a == null) {
					a = el.getAttributeNodeNS(
						XDConstants.XDEF_INSTANCE_NS_URI, "root");
				}
				if (a == null) {
					el.setAttributeNS(
						XDConstants.XDEF_INSTANCE_NS_URI, "root", xdMode);
				} else {
					String value = a.getValue();
					StringTokenizer st = new StringTokenizer(value, " |");
					boolean found = false;
					while (st.hasMoreTokens()) {
						name = st.nextToken();
						if (xdMode.equals(name)) {
							found = true;
						}
					}
					if (!found) {
						a.setValue(value + " | " + xdMode);
					}
				}
			}
		}
		genSchema(KXmlUtils.nodeToString(collection, false),
			outputDir, schemaPrefix, schemaFileExt, out);
	}

	/** Run class from command line.
	 * @param args array of string with command line arguments:
	 * <ul>
	 * <li>-i list of input sources with X-definitions.</li>
	 * <li>-o output directory.</li>
	 * <li>-m name of root model (optional).</li>
	 * <li>-x name of X-definition (optional).</li>
	 * <li>-sp prefix of XML schema namespace (optional, default is "xs").</li>
	 * <li>-se extension of schema file (optional, default is "xsd").</li>
	 * <li>-?, -h, help</li>
	 * </ul>
	 */
	public final static void main(final String... args) {
		ArrayList<String> source = new ArrayList<String>();
		String outputDir = null;
		String xdName = null;
		String model = null;
		String schemaPrefix = null;
		String schemaFileExt = null;
		PrintStream out = System.out;
		final String info =
"Using XdefToXsd: \n"
+ "-i  <PATH> list of input sources with X-definitions\n"
+ "-o  <PATH> output directory\n"
+ "-m  name of root model (optional)\n"
+ "-x  name of X-definition (optional)\n"
+ "-sp prefix of XML schema namespace (optional, default is \"xs\")\n"
+ "-se extension of schema file (optional, default is \"xsd\")";
		final String info1 = info + "\n-h  help";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Parameters missing!\n" + info1);
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if ("-h".equals(arg)) {
				System.out.println(info);
				return;
			} else if ("-i".equals(arg)) {
				if (!source.isEmpty()) {
					throw new RuntimeException(
						"Input files already set\n" + info1);
				}
				while (i + 1 < args.length) {
					if (args[i+1] == null || args[i+1].startsWith("-")) {
						break;
					}
					source.add(args[++i]);
				}
				if (source.isEmpty()) {
					throw new RuntimeException(
						"No input files specified\n" + info1);
				}
			} else if ("-o".equals(arg)) {
				if (outputDir != null) {
					throw new RuntimeException(
						"Output directory is already set\n" + info1);
				}
				if (i + 1 < args.length) {
					outputDir = args[++i];
				}
			} else if ("-m".equals(arg)) {
				if (model != null) {
					throw new RuntimeException(
						"Model name is already set\n" + info1);
				}
				if (i + 1 < args.length) {
					model = args[++i];
				}
			} else if ("-x".equals(arg)) {
				if (xdName != null) {
					throw new RuntimeException(
						"X-definition name is already set\n" + info1);
				}
				if (i + 1 < args.length) {
					xdName = args[++i];
				}
			} else if ("-sp".equals(arg)) {
				if (schemaPrefix != null) {
					throw new RuntimeException("XML schema"
						+ " namespace prefix is already set\n" + info1);
				}
				if (i + 1 < args.length) {
					schemaPrefix = args[++i];
				}
			} else if ("-se".equals(arg)) {
				if (schemaFileExt != null) {
					throw new RuntimeException("XML schema file name"
						+ " extension is already set\n" + info1);
				}
				if (i + 1 < args.length) {
					schemaFileExt = args[++i];
				}
			} else {
				throw new RuntimeException("Incorrect argument: arg\n" + info1);
			}
		}
		// validating input file
		if (source.isEmpty()) {
			throw new RuntimeException("No input file specified\n" + info1);
		}
		//validating output file
		if (outputDir == null) {
			throw new RuntimeException("Output directory is missing\n" + info1);
		}
		if (schemaPrefix == null) {
			schemaPrefix = "xs";
		}
		if (schemaFileExt == null) {
			schemaFileExt = "xsd";
		}
		try {
			XdefToXsd.genSchema(
				SUtils.getFileGroup(source.toArray(new String[source.size()])),
				outputDir,
				xdName,
				model,
				schemaPrefix,
				schemaFileExt,
				out);
		} catch (Exception x) {
			throw new RuntimeException("Exception during setting parameters",x);
		}
	}
}