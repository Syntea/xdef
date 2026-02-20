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
import org.xdef.sys.SUtils;
import org.xdef.util.xd2xsd.Xd2Xsd;
import org.xdef.xml.KXmlUtils;

/** Convertor of X-definition to XML schema (XSD) (see {@link org.xdef.util.XdefToXsd#main(String[])}).
 * @author Vaclav Trojan
 */
public class XdefToXsd {

    /** Generate XML Schema (XSD) from given X-definition files and save XSD files to given output directory.
     * @param xdefs X-definition file.
     * @param outDir output XSD schema files directory.
     * @param xdName name of X-definition. May be null, then the nameless X-definition or the first one
     * X-definition is used.
     * @param modelName name of the root model. May be null, then all values from "xs:root" parameter are
     * used to create models.
     * @param outName name of base XML schema (XSD) file.
     * @param outType name of XML schema (XSD) file with type declarations (may be null, then declared simple
     * types are generated to the file with model).
     * @param genInfo if true documentation information is generated.
     * @param genXdateOutFormat if true, use as mask to validate XML data the parameter describing output
     * format from the "xdatetime" method.
     */
    public static void genSchema(final File[] xdefs,
        final File outDir,
        final String xdName,
        final String modelName,
        final String outName,
        final String outType,
        final boolean genInfo,
        final boolean genXdateOutFormat) {
        if (xdefs == null || xdefs.length == 0) {
            throw new RuntimeException("Missing X-definition source files");
        }
        Properties props = new Properties();
        props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT,
            XDConstants.XDPROPERTYVALUE_IGNORE_UNDEF_EXT_TRUE);
        XDPool xp = XDFactory.compileXD(props, xdefs);
        String xname = xdName == null
            ? xp.getXMDefinition("") != null ? "" : xp.getXMDefinitions()[0].getName() : xdName;
        String oname = outName == null ? xname : outName;
        Map<String, Element> schemaMap =
            Xd2Xsd.genSchema(xp, xname, modelName, oname, outType, genInfo, genXdateOutFormat);
        writeSchema(outDir, schemaMap);
    }

    /** Write created XML schema (XSD) files to the directory.
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
                String fileName = key + ".xsd";
                // source line length is 130
                KXmlUtils.writeXml(new File(outDir, fileName), "UTF-8", schemaMap.get(key), true, true, 130);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Run XML schema (XSD) generator.
     * @param xp compiled XDPool.
     * @param xdName name of root X-definition.
     * @param modelName name of the root model. May be null, then all values
     * from "xs:root" parameter are used to create models.
     * @param outName name of root XML schema (XSD) file.
     * @param outType name of XML schema (XSD) file with type declarations (may be
     * null, then declared simple types are generated to the file with model).
     * @param genAnnotation switch if generate annotation with documentation.
     * @param genXdateOutFormat if true, from the xdatetime method the outFormat
     * parameter (the second sequential) is used as mas to validate datetime.
     * @return map with names of XML schema (XSD) files and corresponding Elements.
     */
    public static Map<String, Element> genSchema(final XDPool xp,
        final String xdName,
        final String modelName,
        final String outName,
        final String outType,
        final boolean genAnnotation,
        final boolean genXdateOutFormat) {
        return Xd2Xsd.genSchema(xp, xdName, modelName, outName, outType, genAnnotation, genXdateOutFormat);
    }

    /** String with command line information. */
    private static final String INFO =
"XdefToXsd - convertor of X-definition to XML Schema (XSD).\n" +
"Parameters:\n"+
" -h or --help:     display help information\n" +
" -i or --xdef:     list of input source pathnames with X-definitions\n" +
" -o or --outDir:   pathname of output directory \n" +
" -s or --outName:  name of main XML schema file (optional)\n" +
" -t or --outType:  name of file with XML schema type declarations (optional)\n"+
" -r or --root:     name of root model (optional). If this argument is missing\n"+
"                   then from all values of \"xs:root\" parameter are created\n"+
"                   models.\n" +
" -x or --xdName:   name of X-definition (optional)\n" +
" -v or --genInfo:  generate documentation information.\n" +
" -xx:              use output format of xdatetime method to create XML schema\n"+
"                   type";

    /** Run XML schema (XSD) generator from command line.
     * @param args array of string with command line arguments:
     * <ul>
     * <li>-h or --help: display help information.</li>
     * <li>-i or --xdef: list of input source path names with X-definitions.</li>
     * <li>-o or --outDir:  pathname of output directory.</li>
     * <li>-s or --outName: name of main XML schema file (optional).</li>
     * <li>-t or --outType: name of file with XML schema type declarations
     * (optional).</li>
     * <li>-r or --root: name of root model (optional). If this argument is
     * missing then from all values from "xs:root" parameter are created
     * models. </li>
     * <li>-x or --xdName: name of X-definition (optional).</li>
     * <li>-v or --genInfo: generate documentation information.</li>
     * <li>-xx:  use output format of xdatetime method to create data type.</li>
     * </ul>
     */
    public static void main(String... args) {
        String xdName = null; // name of X-definition
        String modelName = null; // name of model
        File outDir = null; // output directory
        String outName = null; //name of output file
        String outType = null; //name of file with declared types
        boolean genDecInfo = false; //switch to generate documentation info
        boolean genXdateOutFormat = false; //switch generate xdatatime outFormat
        List<String> xdSources = new ArrayList<>(); //X-definition source
        if (args == null || args.length == 0) {
            throw new RuntimeException("Error: parameters missing.\n" + INFO);
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "/?":
                case "--help":
                    System.out.println(INFO);
                    return;
                case "-o":
                case "--outDir":
                    if (outDir != null) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    outDir =  new File(args[++i]);
                    if (!outDir.exists() || !outDir.isDirectory()) {
                        throw new RuntimeException("\"-outDir\" is not directory.\n" + INFO);
                    }
                    continue;
                case "-s":
                case "--outName":
                    if (outName != null) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    outName = args[++i];
                    continue;
                case "-t":
                case "--outType":
                    if (outType != null) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    outType = args[++i];
                    continue;
                case "-i":
                case "--xdef":
                    for (;;) {
                        String s = args[++i];
                        if (!xdSources.contains(s)) {
                            xdSources.add(s);
                        }
                        if (i+1 >= args.length || args[i+1].startsWith("-")){
                            break;
                        }
                    }
                    continue;
                case "-x":
                case "--xdName":
                    if (xdName != null) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    xdName = args[++i];
                    continue;
                case "-r":
                case "--root":
                    if (modelName != null) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    modelName = args[++i];
                    continue;
                case "-v":
                case "--genInfo":
                    if (genDecInfo) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    genDecInfo = true;
                    continue;
                case "--xx":
                    if (genXdateOutFormat) {
                        throw new RuntimeException("Redefinition of "+arg+".\n" + INFO);
                    }
                    genXdateOutFormat = true;
                    continue;
                default:
                    throw new RuntimeException("Switch error "+arg+".\n"+INFO);
            }
        }
        if (xdSources.isEmpty()) {
            throw new RuntimeException("Missing X-definition sources.\n"+INFO);
        }
        if (outDir == null) {
            throw new RuntimeException("Missing output directory.\n" + INFO);
        }
        File[] xdefs = SUtils.getFileGroup(xdSources.toArray(new String[0]));
        genSchema(xdefs, outDir, xdName, modelName,
            outName, outType, genDecInfo, genXdateOutFormat);
    }
}