package org.xdef.component;

import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import java.util.HashSet;
import org.xdef.XDConstants;
import org.xdef.impl.XElement;
import org.xdef.impl.XPool;
import org.xdef.model.XMNode;
import org.xdef.sys.Report;

/** Generation of Java source code of XDComponents.
 * @author Vaclav Trojan
 */
public final class GenXComponent {

	/** Generate sources of enumerations.
	 * @param xdpool XDPool object.
	 * @param fdir directory where to generate.
	 * @param charset character of generated Java sources.
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param reporter where to write report,.
	 */
	private static void genEnumerations(final XDPool xdpool,
		final File fdir,
		final String charset,
		final boolean genJavadoc,
		final ArrayReporter reporter) {
		Map<String, String> enums =	xdpool.getXComponentEnums();
		if (enums == null || enums.isEmpty()) {
			return;
		}
		for (Entry<String, String> e: enums.entrySet()) {
			String cname = e.getValue();
			int ndx = cname.indexOf(' ');
			String enumName = cname.substring(0, ndx);
			if (enumName.charAt(0) == '%') {
				continue; // just reference
			}
			String values = cname.substring(ndx + 1);
			String packageName = "";
			if ((ndx = enumName.lastIndexOf('.')) > 0) {
				packageName = enumName.substring(0, ndx);
				enumName = enumName.substring(ndx + 1);
			}
			File fparent = new File(fdir, packageName.replace('.', '/'));
			fparent.mkdirs();
			File f = new File(fparent, enumName + ".java");
			FileOutputStream fos = null;
			try {
				final String LN = XDConstants.LINE_SEPARATOR;
				fos = new FileOutputStream(f);
				Writer out = charset == null || charset.isEmpty() ?
					new OutputStreamWriter(fos) :
					new OutputStreamWriter(fos, charset);
				out.write(
"//This enumeration was generated by org.xdef.component.GenXComponent"+LN+
"//from declared parser type name: " + e.getKey() + "."+LN+
"//Any modifications to this file will be lost upon recompilation."+LN);
				if (!packageName.isEmpty()) {
					out.write("package " + packageName +";"+LN);
				}
				if (genJavadoc) {
					out.write(LN+"/** This enumeration represents the type "
						+ e.getKey() + " from X-definition.*/"+LN);
				}
				out.write("public enum "
					+ enumName
					+ " implements org.xdef.component.XCEnumeration{"+LN);
				boolean notFirst = false;
				StringTokenizer st = new StringTokenizer(values);
				while (st.hasMoreTokens()) {
					if (notFirst) {
						out.write(","+LN);
					}
					notFirst = true;
					out.write("\t"+ st.nextToken());
				}
				final String template =";"+LN+
(genJavadoc ?
(LN+"\t@Override"+LN+
"\t/** Get object associated with this item of enumeration."+LN+
"\t * @return object associated with this item of enumeration."+LN+
"\t */"+LN) : "\t@Override"+LN) +
"\tpublic final Object itemValue() {return name();}"+LN+
(genJavadoc ?
(LN+"\t@Override"+LN+
"\t/** Get string which is used to create enumeration."+LN+
"\t * @return string which is used to create enumeration."+LN+
"\t */"+LN) : "\t@Override"+LN) +
"\tpublic final String toString() {return name();}"+LN+
(genJavadoc ?
(LN+"\t/** Create enumeration item from an object."+LN+
"\t * @param x Object to be converted."+LN+
"\t * @return the item of this  enumeration (or null)."+LN+
"\t */"+LN) : "") +
"\tpublic static final " + enumName + " toEnum(final Object x) {"+LN+
"\t\tif (x!=null)"+LN+
"\t\t\tfor(" + enumName + " y: values())"+LN+
"\t\t\t\tif (y.itemValue().toString().equals(x.toString())) return y;"+LN+
"\t\treturn null;"+LN+
"\t}"+LN+
"}";
				out.write(template);
				out.close();
			} catch (Exception ex) {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception exx) {}
				}
				reporter.error(SYS.SYS036, ex); //Program exception &{0}
			}
		}
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param suppressPrintWarnings suppress print of warnings.
	 * @return ArrayReporter with errors and warnings
	 * @throws IOException if an error occurs.
	 */
	public static ArrayReporter genXComponent(XDPool xdpool,
		String dir,
		String charset,
		boolean genJavadoc,
		boolean suppressPrintWarnings) throws IOException {
		return genXComponent(xdpool,
			new File(dir), charset, genJavadoc, suppressPrintWarnings);
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param fdir directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param suppressPrintWarnings suppress print of warnings.
	 * @return ArrayReporter with errors and warnings
	 * @throws IOException if an error occurs.
	 */
	public static final ArrayReporter genXComponent(XDPool xdpool,
		final File fdir,
		final String charset,
		final boolean genJavadoc,
		final boolean suppressPrintWarnings) throws IOException {
		final ArrayReporter reporter = new ArrayReporter();
		if (!fdir.exists() || !fdir.isDirectory()) {
			//Argument &{0} must be a directory
			throw new SRuntimeException(XDEF.XDEF368, fdir.getAbsolutePath());
		}
		final Map<String, String> components =
			new LinkedHashMap<String, String>(xdpool.getXComponents());
		for (int runCount = 0; runCount < 2; runCount++) {
			// create HashSet with class names of X.components
			HashSet<String> classNames = new HashSet<String>();
			for (Entry<String, String> e: xdpool.getXComponents().entrySet()) {
				String s = e.getValue();
				int ndx = s.indexOf(" ");
				classNames.add(ndx > 0 ? s.substring(0, ndx): s);
			}
			// create array of all X-components so that first are the items
			// which are extensions of an other X-component and then follows
			// those not extendsd. This ensures that X-components which extends
			// other X-component are compiled first.
			ArrayList<Entry<String, String>> xcarray =
				new ArrayList<Entry<String, String>>();
			for (Entry<String, String> e: xdpool.getXComponents().entrySet()) {
				int ndx;
				String s = e.getValue();
				if ((ndx = s.indexOf(" extends ")) > 0) {
					s = s.substring(ndx + 9);
					ndx = s.indexOf(' ');
					if (ndx > 0) {
						s = s.substring(0, ndx);
					}
					if (classNames.contains(s)) {
						xcarray.add(e);
						continue;
					}
				}
				xcarray.add(0, e);
			}
			 // in first run we generate only component classes
			 // in second run we generate only interfaces
			for (Entry<String, String> e: xcarray) {
				final String model = e.getKey();
				String className = e.getValue();
				String extName = "", interfaceName = "";
				int ndx;
				String packageName = "";
				if ((ndx = className.indexOf(" interface ")) > 0) {
					if (runCount == 0) {
						String s = className.substring(ndx + 11);
						className = className.substring(0, ndx);
						int ndx1 = className.indexOf(" implements ");
						if (ndx1 < 0) {
							className += " implements " + s;
						} else {
							int ndx2 = className.indexOf(' ' + s);
							if (ndx2 < 0) {
								ndx2 = className.indexOf(',' + s);
							}
							if (ndx2 < 0) {
								className += ',' + s;
							}
						}
					} else {
						interfaceName = className.substring(ndx + 11);
						className = "";
						ndx = interfaceName.lastIndexOf('.');
						if (ndx > 0) {
							packageName = interfaceName.substring(0, ndx);
						}
					}
				} else if (className.startsWith("interface ")) {
					if (runCount == 0) {
						continue;
					}
					// never should happen to be here
					interfaceName = className.substring(10);
					className = "";
				} else if (className.startsWith("%ref ")) {
					if (runCount == 0) {
						className = className.substring(5).trim();
						components.put(model, className);
					}
					continue;
				} else if (runCount == 1) {
					continue;
				}
				if ((ndx = className.indexOf(' ')) > 0) {
					extName += className.substring(ndx);
					className = className.substring(0, ndx).trim();
				}
				File fparent;
				if ((ndx = className.lastIndexOf('.')) > 0) {
					packageName = className.substring(0, ndx);
					className = className.substring(ndx + 1);
				}
				if (packageName.isEmpty()) {
					fparent = fdir;
				} else {
					fparent = new File(fdir, packageName.replace('.', '/'));
					fparent.mkdirs();
				}
				String fName = className.replace('\n', ' ')
					.replace('\r', ' ').replace('\t', ' ');
				if ((ndx = fName.indexOf(' ')) > 0) {
					fName = className.substring(0 , ndx);
				}
				className = className + extName;
				String extClass = "";
				if ((ndx = className.indexOf(" extends")) > 0) {
					extClass = " extends " + className.substring(ndx+8).trim();
					className = className.substring(0,ndx).trim();
				} else if ((ndx = className.indexOf(" implements")) > 0) {
					extClass=" implements "+className.substring(ndx+11).trim();
					className = className.substring(0,ndx).trim();
				}
				XMNode xn = xdpool.findModel(model);
				if (xn == null || xn.getKind() != XMNode.XMELEMENT) {
					//Model "&{0}" not exsists.
					reporter.add(Report.fatal(XDEF.XDEF373, model));
					continue;
				}
				XCGenerator genxc;
				if (((XPool)xdpool)._oldXomponents && ((XElement) xn)._json==0){
					genxc = new XCGeneratorOld(xdpool, reporter, genJavadoc);
				} else {
					genxc = new XCGeneratorNew(xdpool, reporter, genJavadoc);
				}
				final String result = genxc.genXComponent(model, //model name
					className, //name of generated class
					extClass, //class extension
					interfaceName, //name of interface
					packageName, //package of generated class
					components); // Map with components
				if (result != null) {
					File f = new File(fparent, fName + ".java");
					FileOutputStream fos = new FileOutputStream(f);
					Writer out = charset == null || charset.isEmpty() ?
						new OutputStreamWriter(fos) :
						new OutputStreamWriter(fos, charset);
					out.append(SUtils.modifyString(result, "\t", "  ")).close();
				}
				if (genxc.getIinterfaces() != null) {
					packageName = "";
					if ((ndx = interfaceName.lastIndexOf('.')) > 0) {
						packageName = interfaceName.substring(0, ndx);
						interfaceName = interfaceName.substring(ndx + 1);
					}
					fparent = new File(fdir, packageName.replace('.', '/'));
					fparent.mkdirs();
					File f = new File(fparent, interfaceName + ".java");
					FileOutputStream fos = new FileOutputStream(f);
					Writer out = charset == null || charset.isEmpty() ?
						new OutputStreamWriter(fos) :
						new OutputStreamWriter(fos, charset);
					out.append(SUtils.modifyString(
						genxc.getIinterfaces().toString(), "\t", "  ")).close();
				}
			}
		}
		genEnumerations(xdpool, fdir, charset, genJavadoc, reporter);
		reporter.checkAndThrowErrors();
		if (!suppressPrintWarnings && reporter.errorWarnings()) {
			reporter.printReports(System.err);
		}
		return reporter;
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @throws IOException if an error occurs.
	 */
	public static void genXComponent(final XDPool xdpool,
		final String dir,
		final String charset) throws IOException {
		genXComponent(xdpool, dir, charset, false, false);
	}

	/** Call generation of Java source code of XComponents from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li>-i X-definitions list of files, required. Wildcards are
	 * supported, required.</li>
	 * <li>-x Qualified name of class with XDPool which source will
	 *  be generated, optional (if not specified, source is not generated)</li>
	 * <li>-p package name, optional (if not specified no package is used)</li>
	 * <li>-o Output directory where the sources are generated, required</li>
	 * <li>-e Encoding name, optional (default is the Java system encoding)</li>
	 * <li>-d Generate JavaDoc, optional (default is not generate JavaDoc)</li>
	 * <li>-j Generate JAXB annotations. Optional, default is not generate.</li>
	 * <li>-h Help message, optional</li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"GenXComponent - generate XComponent Java source code from X-definition.\n"+
"Parameters:\n"+
" -i X-definitions list of files, required. Wildcards may be used. Required.\n"+
" -o Output directory where XComponents are generated, required\n"+
" -p Output directory where the compiled XDPool will be stored,\n"+
"    optional (if not specified the XDPool object is not stored)\n"+
" -e Encoding name, optional (default is the Java system encoding)\n"+
" -d Generate JavaDoc, optional (default is not generate JavaDoc)\n"+
" -h Help message, optional";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Missing parameters\n" + info);
		}
		if (args.length < 3) {
			if (args.length == 1 &&
				("-h".equals(args[0]) || "-?".equals(args[0]))) {
				System.out.println(info);
				return;
			} else {
				throw new RuntimeException("Incorrect parameters\b" + info);
			}
		}
		ArrayList<String> sources = new ArrayList<String>();
		File xcDir = null; // base directory where XComponents will be generated
		FileOutputStream xpFile = null; // the file where save compiled XDPool
		String encoding = null;
		boolean javadoc = false;
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg == null || arg.isEmpty()
				|| arg.charAt(0) != '-' || arg.length() != 2) {
				throw new RuntimeException(
					"Incorrect parameter " + (i+1) + ": " + arg + '\n' + info);
			}
			switch (arg.charAt(1)) {
				case 'd': // Generate JavaDoc
					if (javadoc) {
						throw new RuntimeException(
							"Redefinition of key \"-d\"\n" + info);
					}
					javadoc = true;
					continue;
				case 'e': // Encoding
					if (encoding != null) {
						throw new RuntimeException(
							"Redefinition of key \"-e\".\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						encoding = arg;
						i++;
						continue;
					} else {
						throw new RuntimeException(
							"Parameter '-e' is not encoding name.\n" + info);
					}
				case 'i': // X-definitions list of files
					while (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						sources.add(arg);
					}
					continue;
				case 'j': // JAXB annotations
					System.err.println("Warning JAXB annotations swith"
						+ " is ignored in this version!");
					continue;
				case 'h': // help
					System.out.println(info);
					i++;
					continue;
				case 'o': // Output directory
					if (xcDir != null) {
						throw new RuntimeException(
							"Redefinition of key \"-o\"\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						try {
							xcDir = new File(arg);
							if (xcDir.exists() && xcDir.isDirectory()) {
								i++;
								continue;
							}
						} catch (Exception ex) {}
						throw new RuntimeException(
							"Parameter '-o' is not output directory.\n" + info);
					} else {
						throw new RuntimeException(
							"Parameter '-o' is not output directory.\n" + info);
					}
				case 'p': // where to write the XDPool
					if (xpFile != null) {
						throw new RuntimeException(
							"Redefinition of key \"-p\"\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						File f = new File(arg);
						if (f.exists() && f.isDirectory()) {
							throw new RuntimeException(
								"The key \"-p\" must be file\n" + info);
						}
						try {
							i++;
							xpFile = new FileOutputStream(f);
							continue;
						} catch (Exception ex) {
							throw new RuntimeException(
								"Can't write to the file from tne key \"-p\"\n"
									+ info, ex);
						}
					} else {
						throw new RuntimeException(
							"Parameter '-p' is not file\n" + info);
					}
				default:
					throw new RuntimeException("Incorrect parameter \""
						+arg+"\" on position " + (i+1)+".\n" + info);
			}
		}
		if (sources.isEmpty()) {
			throw new RuntimeException(
				"No XDPool source is specified.\n" + info);
		}
		if (xcDir == null) {
			throw new RuntimeException(
				"No output directory specified.\n" + info);
		}
		try {
			Object[] xdefs = new String[sources.size()];
			sources.toArray(xdefs);
			XDPool x = XDFactory.compileXD(null, xdefs);
			genXComponent(x, xcDir.getCanonicalPath(), encoding,javadoc, false);
			if (xpFile != null) {
				XDFactory.writeXDPool(xpFile, x);
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}