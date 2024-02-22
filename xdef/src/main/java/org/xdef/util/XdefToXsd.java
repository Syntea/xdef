package org.xdef.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.model.XMElement;
import org.xdef.sys.SUtils;
import org.xdef.util.xd2xsd.Xd2Xsd;
import org.xdef.xml.KXmlUtils;

/** Convertor of X-definition to XML schema.
 * (see {@link org.xdef.util.XdefToXsd#main(String[])})
 * @author Vaclav Trojan
 */
public class XdefToXsd {
	/** Default extension used for XML schema file name. */
	private static final String SCHEMA_EXTENSION = "xsd";

	/** Generates XML Schema from given X-definition files and saves schema
	 * files to given output directory.
	 * @param xdefs X-definition file.
	 * @param outDir output schema files directory.
	 * @param xdName name of X-definition. May be null, then the nameless
	 * X-definition or the first one X-definition is used.
	 * @param modelName name of model of X-definition to used. May be null,
	 * then the value from "xs:root" parameter is used to create models.
	 * @param outName name of base XML schema file. May be null, then
	 * local name of X-definition model is used.
	 * @param genInfo if true documentation information is generated.
	 */
	public static void genSchema(final File[] xdefs,
		final File outDir,
		final String xdName,
		final String modelName,
		final String outName,
		final boolean genInfo) {
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
			XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
		XDPool xp = XDFactory.compileXD(props, xdefs);
		String xname = xdName == null
			? xp.getXMDefinition("") != null ? ""
			: xp.getXMDefinitions()[0].getName()
			: xdName;
		String mname = null;
		if (modelName == null) {
			XMElement[] roots = xp.getXMDefinition(xname).getRootModels();
			if (roots != null && roots.length > 0) {
				mname = roots[0].getLocalName();
			}
		} else {
			XMElement[] xels = xp.getXMDefinition(xname).getModels();
			for (XMElement xel : xels) {
				if (modelName.equals(xel.getName())) {
					mname = xel.getLocalName();
					break;
				}
			}
		}
		if (mname == null) {
			throw new RuntimeException("Can't find model " + modelName);
		}
		String oname = outName == null ? mname : outName;
		Map<String, Element> schemaMap =
			Xd2Xsd.genSchema(xp, xname, mname, oname, genInfo);
		writeSchema(outDir, schemaMap);
	}

	/** Write created schema files to the directory.
	 * @param outDir directory where to write.
	 * @param schemaMap map with file names and create XML schema elements.
	 */
	public static void writeSchema(final File outDir,
		final Map<String, Element> schemaMap ) {
		if (outDir == null || !outDir.exists() || !outDir.isDirectory()) {
			throw new RuntimeException("Not directory: " + outDir);
		}
		try { // write created XSD files to the output directory.
			for (String key: schemaMap .keySet()) {
				File f = new File(outDir, key + "." + SCHEMA_EXTENSION);
				KXmlUtils.writeXml(f, "UTF-8", schemaMap .get(key), true, true);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Run XML schema generator.
	 * @param xp compiled XDPool.
	 * @param xdName name of root X-definition.
	 * @param modelName name of root model.
	 * @param outName name of root XML schema file.
	 * @param genAnnotation switch if generate annotation with documentation.
	 * @return map with names of XML schema files and corresponding Elements.
	 */
	public static Map<String, Element> genSchema(final XDPool xp,
		final String xdName,
		final String modelName,
		final String outName,
		final boolean genAnnotation) {
		return Xd2Xsd.genSchema(xp, xdName, modelName, outName, genAnnotation);
	}

	/** Run XML schema generator from command line.
	 * @param args array of string with command line arguments:
	 * <ul>
	 * <li>-i or --xdef: list of input source path names with X-definitions
	 * <li>-o or --outDir:  pathname of output directory
	 * <li>-s or --outName: name of main XML schema file
	 * <li>-m or --root: name of root model (optional)
	 * <li>-x or --xdName: name of X-definition (optional)
	 * <li>-v or --genInfo: generate documentation etc.
	 * <li> -h or /?: help
	 * </ul>
	 */
	public static void main(String... args) {
		String info =
"XdefToXsd - convertor of X-definition to XML Schema.\n" +
"Parameters:\n"+
" -i or --xdef:     list of input source pathnames with X-definitions\n" +
" -o or --outDir:   pathname of output directory \n" +
" -s or --outName:  name of main XML schema file\n" +
" -m or --root:     name of root model (optional)\n" +
" -x or --xdName:   name of X-definition (optional)\n" +
" -v or --genInfo:  genarate documentation etc.\n" +
" -h or /?:         help";
		String xdName = null;
		String modelName = null;
		File outDir = null;
		String outName = null;
		boolean genInfo = false;
		List<String> source = new ArrayList<>();
		if (args == null || args.length < 2) {
			throw new RuntimeException("Error: parameters missing.\n" + info);
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "-o":
				case "--outDir":
					if (outDir != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + info);
					}
					outDir =  new File(args[++i]);
					if (!outDir.exists() || !outDir.isDirectory()) {
						throw new RuntimeException(
							"\"-outDir\" is not directory.\n" + info);
					}
					continue;
				case "-s":
				case "--outName":
					if (outName != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + info);
					}
					outName = args[++i];
					continue;
				case "-i":
				case "--xdef":
					for (;;) {
						String s = args[++i];
						if (!source.contains(s)) {
							source.add(s);
						}
						if (i+1 >= args.length || args[i+1].startsWith("-")){
							break;
						}
					}
					continue;
				case "-x":
				case "--xdName":
					if (xdName != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + info);
					}
					xdName = args[++i];
					continue;
				case "-root":
				case "--root":
					if (modelName != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + info);
					}
					modelName = args[++i];
					continue;
				case "-v":
				case "--genInfo":
					if (genInfo) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + info);
					}
					genInfo = true;
			}
		}
		if (source.isEmpty()) {
			throw new RuntimeException("Missing idefinition sources.\n"+info);
		}
		if (outDir == null) {
			throw new RuntimeException("Missing output directory.\n" + info);
		}
		genSchema(SUtils.getFileGroup(source.toArray(new String[source.size()])),
			outDir, xdName, modelName, outName, genInfo);
	}
}