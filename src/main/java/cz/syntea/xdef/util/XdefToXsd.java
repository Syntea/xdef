/*
 * File: XdefToXsd.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.util;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.util.conv.Util;
import cz.syntea.xdef.sys.FileReportWriter;
import cz.syntea.xdef.sys.SReporter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.SUtils;
import cz.syntea.xdef.xml.KXmlConstants;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.util.conv.xd2xsd.Convertor;
import cz.syntea.xdef.util.conv.xd.doc.XdDoc;
import cz.syntea.xdef.util.conv.xsd.doc.XsdVersion;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.PrintStream;

/** Convertor of X-definition to XML Schema.
 * (see {@link cz.syntea.xdef.util.XdefToXsd#main(String[])})
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
	public static void genSchema(String xdef,
		String outputDir,
		String schemaPrefix,
		String schemaFileExt,
		PrintStream out) {
		XdDoc xdDoc;
		SReporter reporter = new SReporter(new FileReportWriter(
			out == null ? System.out : out));
		String sPrefix = schemaPrefix == null ? "xs" : schemaPrefix;
		String sFileExt = schemaFileExt == null ? "xsd" : schemaPrefix;
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
	public static Map<?,?> genSchema(String xdef,
		String schemaPrefix,
		String schemaFileExt,
		PrintStream out) {
		SReporter reporter = new SReporter(new FileReportWriter(
			out == null ? System.out : out));
		String sPrefix = schemaPrefix == null ? "xs" : schemaPrefix;
		String sFileExt = schemaFileExt == null ? "xsd" : schemaPrefix;
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
	public static void genSchema(File xdef,
		String outputDir,
		String schemaPrefix,
		String schemaFileExt,
		PrintStream out) {
		genSchema(xdef.getAbsolutePath(),
			outputDir, schemaPrefix, schemaFileExt,out);
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
	public static void genSchema(File[] xdefs,
		String outputDir,
		String xdName,
		String model,
		String schemaPrefix,
		String schemaFileExt,
		PrintStream out) throws Exception {
		String[] srcs = new String[xdefs.length];
		for (int i = 0; i < xdefs.length; i++) {
			File f = xdefs[i];
			srcs[i] = f.getAbsolutePath();
		}
		genSchema(srcs,
			outputDir, xdName, model,schemaPrefix, schemaFileExt, out);
	}

	@SuppressWarnings("deprecation") //NS_XDEF_2_0_INSTANCE
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
	public static void genSchema(String[] xdefs,
		String outputDir,
		String xdName,
		String model,
		String schemaPrefix,
		String schemaFileExt,
		PrintStream out) throws Exception {
		Element collection;
		if (xdefs.length == 1) {
			collection = KXmlUtils.parseXml(xdefs[0]).getDocumentElement();
		} else {
			collection = GenCollection.genCollection(xdefs, true, true, true);
		}
		if (xdName != null) {
			NodeList nl = KXmlUtils.getChildElementsNS(collection,
				KXmlConstants.NS_XDEF_2_0_INSTANCE, "xdef");
			if (nl.getLength() == 0) {
				nl = KXmlUtils.getChildElementsNS(collection,
					KXmlConstants.XDEF_INSTANCE_NS_URI, "xdef");
			}
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				Attr a = el.getAttributeNode("name");
				String name = "";
				if (a != null) {
					name = a.getValue();
				} else {
					a = el.getAttributeNodeNS(
						KXmlConstants.NS_XDEF_2_0_INSTANCE, "name");
					if (a == null) {
						a = el.getAttributeNodeNS(
							KXmlConstants.XDEF_INSTANCE_NS_URI, "name");

					}
					if (a != null) {
						name = a.getValue();
					}
				}
				if (!name.equals(xdName)) {
					continue;
				}
				if (model == null) {
					model = xdName;
				}
				a = el.getAttributeNode("root");
				if (a == null) {
					a = el.getAttributeNodeNS(
						KXmlConstants.NS_XDEF_2_0_INSTANCE, "root");
				}
				if (a == null) {
					el.setAttributeNS(
						KXmlConstants.XDEF_INSTANCE_NS_URI, "root", model);
				} else {
					String value = a.getValue();
					StringTokenizer st = new StringTokenizer(value, " |");
					boolean found = false;
					while (st.hasMoreTokens()) {
						name = st.nextToken();
						if (model.equals(name)) {
							found = true;
						}
					}
					if (!found) {
						a.setValue(value + " | " + model);
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
	 * <li>-?, -h, help</li>
	 * </ul>
	 */
	public static void main(String... args) {
		ArrayList<String> source = new ArrayList<String>();
		String outputDir = null;
		String xdName = null;
		String model = null;
		String schemaPrefix = "xs";
		String schemaFileExt = "xsd";
		PrintStream out = System.out;
		String info =
"Using XdefToXsd: \n"
+ "-i <PATH> list of input sources with X-definitions\n"
+ "-o <PATH> output directory \n"
+ "-m name of root model (optional)\n"
+ "-x name of X-definition (optional)\n"
+ "-?, -h,  help";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Parameters missing!\n" + info);
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if ("-h".equals(arg) || "-?".equals(arg)) {
				System.out.println(info);
				return;
			} else if ("-i".equals(arg)) {
				if (!source.isEmpty()) {
					throw new RuntimeException(
						"Input files already set\n" + info);
				}
				while (i + 1 < args.length) {
					if (args[i+1] == null || args[i+1].startsWith("-")) {
						break;
					}
					source.add(args[++i]);
				}
				if (source.isEmpty()) {
					throw new RuntimeException(
						"No input files specified\n" + info);
				}
			} else if ("-o".equals(arg)) {
				if (outputDir != null) {
					throw new RuntimeException(
						"Output directory is already set\n" + info);
				}
				if (i + 1 < args.length) {
					outputDir = args[++i];
				}
			} else if ("-m".equals(arg)) {
				if (model != null) {
					throw new RuntimeException(
						"Model name is already set\n" + info);
				}
				if (i + 1 < args.length) {
					model = args[++i];
				}
			} else if ("-x".equals(arg)) {
				if (xdName != null) {
					throw new RuntimeException(
						"X-definition name is already set\n" + info);
				}
				if (i + 1 < args.length) {
					xdName = args[++i];
				}
			} else {
				throw new RuntimeException("Incorrect argument: arg\n" + info);
			}
		}
		// validating input file
		if (source.isEmpty()) {
			throw new RuntimeException("No input file specified\n" + info);
		}
		//validating output file
		if (outputDir == null) {
			throw new RuntimeException("Output directory is missing\n" + info);
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