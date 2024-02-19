package org.xdef.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xdef.XDPool;
import org.xdef.sys.SUtils;
import org.xdef.util.xd2xsd.Xd2Xsd;

/** Convertor of X-definition to XML schema.
 * @author Vaclav Trojan
 */
public class XdefToXsd {

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
	 * @param genAnnotation if true documentation information is generated.
	 */
	public static void genSchema(final File[] xdefs,
		final File outDir,
		final String xdName,
		final String modelName,
		final String outName,
		final boolean genAnnotation) {
		Xd2Xsd.genSchema(xdefs,
			outDir, xdName, modelName, outName, genAnnotation);
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

	private static final String INFO =
"XdefToXsd - convertor of X-definition to XML Schema.\n" +
"Parameters:\n"+
" -i or --xdef:     list of input source pathnames with X-definitions\n" +
" -o or --outDir:   pathname of output directory \n" +
" -s or --outName:  name of main XML schema file\n" +
" -m or --root:     name of root model (optional)\n" +
" -x or --xdName:   name of X-definition (optional)\n" +
" -v or --genInfo:  genarate documentation etc.\n" +
" -h or /?:         help";

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
		String xdName = null;
		String modelName = null;
		File outDir = null;
		String outName = null;
		boolean genAnnotation = false;
		List<String> source = new ArrayList<>();
		if (args == null || args.length < 2) {
			throw new RuntimeException("Error: parameters missing.\n" + INFO);
		}
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "-o":
				case "--outDir":
					if (outDir != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + INFO);
					}
					outDir =  new File(args[++i]);
					if (!outDir.exists() || !outDir.isDirectory()) {
						throw new RuntimeException(
							"\"-outDir\" is not directory.\n" + INFO);
					}
					continue;
				case "-s":
				case "--outName":
					if (outName != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + INFO);
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
							"Redefinition of "+arg+".\n" + INFO);
					}
					xdName = args[++i];
					continue;
				case "-root":
				case "--root":
					if (modelName != null) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + INFO);
					}
					modelName = args[++i];
					continue;
				case "-v":
				case "--genInfo":
					if (genAnnotation) {
						throw new RuntimeException(
							"Redefinition of "+arg+".\n" + INFO);
					}
					genAnnotation = true;
			}
		}
		if (source.isEmpty()) {
			throw new RuntimeException("Missing idefinition sources.\n"+INFO);
		}
		if (outDir == null) {
			throw new RuntimeException("Missing output directory.\n" + INFO);
		}
		File[] xdefs =
			SUtils.getFileGroup(source.toArray(new String[source.size()]));
		genSchema(xdefs, outDir, xdName, modelName, outName, genAnnotation);
	}

}
