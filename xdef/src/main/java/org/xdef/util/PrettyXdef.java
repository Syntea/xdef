package org.xdef.util;

import java.io.ByteArrayInputStream;
import org.xdef.sys.SUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.impl.xml.XInputStream;
import org.xdef.impl.xml.XReader;

/** Generate formatted source of the X-definitions.
 * Also provides main method for calling the program from command line.
 * (see {@link org.xdef.util.PrettyXdef#main(String[])})
 * @author Vaclav Trojan
 */
public class PrettyXdef extends XReader {
	/** Concatenated text values. */
	private final StringBuilder _text = new StringBuilder();
	/** String with result source XML:. */
	private final StringBuilder _result = new StringBuilder();
	/** Level of child element. */
	private int _level = 0;
	/** Number of indent spaces. */
	private final int _indentStep;
	/** flag to trim text values. */
	private final boolean _trimText = true;
	/** flag to trim text attribute values. */
	private final boolean _trimAttr = false;

	/** Create new instance of PrettyXdef.
	 * @param mi XInputStream reader.
	 * @param indentStep number of indent spaces (0 .. no indenting).
	 * @throws IOException if an error occurs,
	 */
	private PrettyXdef(XInputStream mi, int indentStep) throws IOException {
		super(mi);
		_indentStep = indentStep;
	}

	/** Get string with given number of spaces.
	 * @param num Number of spaces.
	 * @return The string with given number of spaces.
	 */
	private String getIndentSpaces(int num) {
		if (_indentStep <= 0) {
			return "";
		}
		StringBuilder sb = _result.length() == 0
			? new StringBuilder() : new StringBuilder("\n");
		for (int i = 0; i < num; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	private void addToResult(String s) {
		_result.append(getIndentSpaces(_level * _indentStep)).append(s);
	}

	private int parseCommentOrPI() {
		int i;
		if ((i = scanPI()) != -1 || (i = scanComment()) != -1) {
			addToResult(getBufPart(i, getPos()).trim());
		}
		return i;
	}

	private void parseMisc() {
		for(;;) {
			scanSpaces();
			if (parseCommentOrPI() == -1) {
				break;
			}
		}
	}

	private void parseProlog() {
		int i;
		scanXMLDecl();
		parseMisc();
		if ((i=scanDoctype()) != -1) {
			addToResult(this.getBufPart(i, getPos()).trim());
		}
		parseMisc();
	}

	private void addText() {
		if (_text.length() > 0) {
			String s = _text.toString();
			if (_trimText) {
				s = s.trim();
			}
			if (!s.isEmpty()) {
				addToResult(s);
			}
			_text.setLength(0);
		}
	}

	private static class Attr {
		final String _name;
		final String _value;
		Attr(String name, String value) {_name = name; _value = value;}
		@Override
		public String toString() {return _name + "=" + _value;}
	}

	private Attr parseAttr() {
		scanSpaces();
		int i;
		if ((i = scanName()) != -1) {
			String s = getBufPart(i, getPos());
			scanSpaces();
			if (isChar('=')) {
				scanSpaces();
				if ((i = scanLiteral()) != -1) {
					String value = getBufPart(i, getPos());
					if (_trimAttr) {
						char quote = value.charAt(0);
						value = quote +
							value.substring(1, value.length()-1).trim() + quote;
					}
					return new Attr(s, value);
				}
			} else {
				throw new RuntimeException("Attribute expected");
			}
		}
		return null;
	}

	private boolean parseElement() {
		releaseScanned();
		int i;
		for(;;) {
			addText();
			if ((i = scanPI()) != -1 || (i = scanComment()) != -1
				|| (i = scanCDATA()) != -1) {
				addToResult(getBufPart(i, getPos()));
			} else if ((i = scanText()) != -1 || (i = scanEntity()) != -1) {
				do {
					_text.append(getBufPart(i, getPos()));
				} while ((i = scanText()) != -1 || (i = scanEntity()) != -1);
			} else if ((i = scanEndElement()) >= 0) {
				_level--;
				addToResult(getBufPart(i, getPos()));
				return false;
			} else {
				break;
			}
		}
		if (isChar('<') && (i = scanName()) != -1) {
			String name = getBufPart(i, getPos());
			addToResult("<" + name);
			List<Attr> attrs = new ArrayList<>();
			Attr att;
			while ((att = parseAttr()) != null) {
				attrs.add(att);
			}
			boolean wasAttr = false;
			for (Attr a : attrs) {
				if (a._name.startsWith("xmlns")) {//write first xmlns attributes
					String indent = wasAttr && _indentStep > 0 ?
						getIndentSpaces(name.length()+2)+_level*_indentStep:" ";
					_result.append(indent).append(a.toString());
					wasAttr = true;
				}
			}
			for (Attr a : attrs) {//otehr attributes
				if (!a._name.startsWith("xml")) {
					String indent = wasAttr && _indentStep > 0 ?
						getIndentSpaces(_level*_indentStep+name.length()+2):" ";
					_result.append(indent).append(a.toString());
					wasAttr = true;
				}
			}
			if (isToken("/>")) {
				_result.append("/>");
			} else if (isChar('>')) {
				_result.append('>');
				_level++;
				while (parseElement()) {}
			} else {
				throw new RuntimeException("Element error");
			}
			return true;
		}
		return false;
	}

	private String parse() throws IOException {
		while (read() >= 0) {} // read XML to buffer
		close();
		parseProlog();
		_text.setLength(0);
		parseElement();
		_text.setLength(0);
		parseMisc();
		return _result.toString();
	}

	/** Write pretty formatted file with source X-definition to given directory.
	 * @param source The input file or the string with XML data.
	 * @param indentStep number of indent spaces.
	 * @param out OutputStream where to write formatted XML.
	 * @param encoding The character encoding name or null.
	 * @return the encoding of written XML.
	 * @throws IOException if an error occurs.
	 */
	public static String prettyWrite(String source,
		int indentStep,
		OutputStream out,
		String encoding) throws IOException {
		InputStream in;
		if (source.startsWith("<")) {
			in = new ByteArrayInputStream(
				source.getBytes(Charset.forName("UTF-8")));
		} else {
			URL u = SUtils.getExtendedURL(source);
			in = u.openStream();
		}
		return prettyWrite(in, indentStep, out, encoding);
	}

	/** Write pretty formatted file with source X-definition to given directory.
	 * @param in The input stream with XML data.
	 * @param indentStep number of indent spaces.
	 * @param out OutputStream where to write formatted XML.
	 * @param encoding The character encoding name or null.
	 * @return the encoding of written XML.
	 * @throws IOException if an error occurs.
	 */
	public static String prettyWrite(InputStream in,
		int indentStep,
		OutputStream out,
		String encoding) throws IOException {
		XInputStream mi = new XInputStream(in);
		String enc = encoding != null ? encoding : mi.getXMLEncoding();
		String ver = mi.getXMLVersion();
		PrettyXdef p = new PrettyXdef(mi, indentStep);
		p.parse();
		OutputStreamWriter wr = new OutputStreamWriter(out, enc);
		if (!"1.0".equals(ver) || !"UTF-8".equals(enc)
			|| mi.getXMLStandalone()) {
			wr.write("<?xml version=\"" + ver + "\" encoding=\"" + enc + "\"" +
				(mi.getXMLStandalone() ? " standalone=\"yes\"" : "") + "?>\n" );
		}
		wr.write(p._result.toString());
		wr.close();
		return enc;
	}

	/** Write pretty formatted file with source X-definition to given directory.
	 * @param file The input file.
	 * @param outDir directory where to put result files.
	 * @param indentStep number of indent spaces.
	 * @param encoding The character encoding name or null.
	 * @return the encoding of written XML.
	 * @throws IOException if an error occurs.
	 */
	public static String prettyWriteToDir(File file,
		File outDir,
		int indentStep,
		String encoding) throws IOException {
		String outDirName = outDir.getAbsolutePath().replace('\\','/');
		if (!outDirName.endsWith("/")) {
			outDirName += '/';
		}
		String s = file.getAbsolutePath();
		if (!file.exists() || !file.canRead()) {
			System.err.println("Can't read file '" + s + "'");
		}
		String fname = s.replace('\\','/');
		int index = fname.lastIndexOf('/');
		if (index < 0) {
			fname = outDirName + fname;
		} else {
			fname = outDirName + fname.substring(index + 1);
		}
		InputStream in = new FileInputStream(file);
		OutputStream out = new FileOutputStream(fname);
		return prettyWrite(in, indentStep, out, encoding);
	}

	/** Write pretty formatted files with source X-definitions.
	 * @param files Array of files.
	 * @param outDir directory where to put result files.
	 * @param indentStep number of indent spaces.
	 * @param encoding The character encoding name or null.
	 * @throws IOException if an error occurs.
	 */
	public static void prettyWrite(File[] files,
		File outDir,
		int indentStep,
		String encoding) throws IOException {
		for (File file : files) {
			prettyWriteToDir(file, outDir, indentStep, encoding);
		}
	}

	/** Calling the program from command line.
	 * @param args The array of strings with arguments.
	 * <i>[-d outDir | -o outFile] [-i n] [-e encoding] [-p prefix] file</i>
	 * <ul>
	 *  <li><i>-o outFile</i> - output file or output directory.
	 * If this parameter is not specified then input file is replaced by the
	 * formatted version.
	 *  <li><i>-d outDir</i> - output directory. If this parameter is not
	 * specified the formated files will be replaced by the the formated
	 * version.
	 *  <li><i>-i n</i> - number of spaces used for indentation. If this
	 * parameter is not specified the parameter is set to 2.
	 * If n is equal to 0 no indentation is provided.
	 *  <li><i>-e encoding</i> - name of character set. If this parameter
	 * is not specified it will be used the original character set.
	 *  <li><i>file</i> - the file with source X-definition.
	 * </ul>
	 * @throws IOException if an error occurs.
	 */
	public static void main(String... args) throws IOException {
		final String info =
"Formating of source files with X-definitions.\n"+
"Command line arguments:\n"+
"    [-d outDir | -o outFile] [-i n] [-e encoding] [-p prefix] file\n"+
"Where:\n"+
"-o outFile  Output file or out directory. If this parameter is not\n"+
"            specified then input file is replaced by the formated version.\n"+
"-d outDir   Output directory. If this parameter is not specified the\n"+
"            formated files will be replaced by the the formated version.\n"+
"-i n        Number of spaces used for indentation. If this parameter is\n"+
"            not specified the parameter is set to 2.\n"+
"            If n is equal to 0 no indentation is provided.\n"+
"-e encoding Name of character set. If this parameter is not specified\n"+
"            it will be used the original character set.\n"+
"file        The file with source X-definition.";
		Map<String, File> xdefs = new LinkedHashMap<>();
		File outDir = null;
		String fileName = null;
		String encoding = null;
		int indent = Integer.MIN_VALUE;
		int i = 0;
		String msg = "";
		while (i < args.length && args[i].startsWith("-")) {
			int swNum = i + 1;
			if (args[i].length() == 1) {
				msg += "Incorrect parameter [" + swNum + "]: '-'\n";
				i++;
				continue;
			}
			char c = args[i].charAt(1);
			if (c == 'h' || c == '?') {
				System.out.println(info); //help
				return;
			}
			String s;
			if (args[i].length() == 2) {
				i++;
				if (i >= args.length || args[i].startsWith("-")) {
					s = null;
				} else {
					s = args[i];
					i++;
				}
			} else {
				s = args[i].substring(2);
			}
			switch (c) {
				case 'd': {//output file
					if (fileName != null) {
						msg +=
							"'-o' and '-d' swithes can't be specified both.\n";
						continue;
					}
					if (s == null) {
						msg += "Parameter [" + swNum +
							"], '" + args[swNum-1]
							+ "': missing following argument\n";
						continue;
					}
					String dirName = s; //output directory
					outDir = new File(dirName);
					if (!outDir.exists()) {
						outDir.mkdirs();
					}
					if (!outDir.exists() || !outDir.isDirectory()) {
						msg +=  "Invalid output directory\n";
					}
					continue;
				}
				case 'i': {
					if (s == null) {
						msg += "Parameter [" + swNum +
							"], '" + args[swNum-1] +
							"': missing following argument\n";
						continue;
					}
					if (indent == Integer.MIN_VALUE) {
						try {
							indent = Integer.parseInt(s);
							if (indent >= 0) {
								if (indent == 0) {
									indent = -1;
								}
								continue;
							}
						} catch (Exception ex) {}
						msg += "Incorrect indentation parameter;" +
							" indentation must be >= 0\n";
					}
					continue;
				}
				case 'o': {//output directory
					if (outDir != null) {
						msg += "Swithes '-o' and '-d' can't be" +
							" used simultaneously.\n";
						continue;
					}
					if (s == null) {
						msg += "Parameter [" + swNum +
							"], '" + args[swNum-1] +
							"': missing following argument\n";
						continue;
					}
					fileName = s;
					File f = new File(fileName);
					if (f.exists()) {
						if (f.isDirectory()) {
							msg += "Output file " + fileName +
								" can't be directory\n";
							continue;
						} else if (f.exists() && !f.canWrite()){
							msg += "Can't write to output file "
								+ fileName + "\n";
							continue;
						}
					}
					continue;
				}
				case 'e': {//encoding
					encoding = s;
					continue;
				}
				default:
					msg += "Parameter [" + swNum +
						"], '" + args[swNum-1] +
						" is incorrect switch\n";
			}
		}
		if (i < args.length) {
			File[] files = SUtils.getFileGroup(args[i]);
			for (File file : files) {
				if (file.exists() && file.isFile() && file.canRead()) {
					try {
						xdefs.put(file.getCanonicalPath(), file);
					} catch (Exception ex) {
						msg += "Error on file " + file.getAbsoluteFile() + "\n";
					}
				} else {
					msg += "Error on file " + file.getAbsoluteFile() + "\n";
				}
			}
		}
		if (outDir == null && fileName == null) {
			msg += "Output not specified.\n";
		}
		if (xdefs.isEmpty()) {
			msg += "Input files missing.\n";
		}
		if (msg.length() > 0) {
			throw new RuntimeException(msg + info);
		}
		if (indent == Integer.MIN_VALUE) {
			indent = 2;
		}
		if (xdefs.size() == 1 && fileName != null) {
			InputStream in =
				new FileInputStream((File)xdefs.values().toArray()[0]);
			OutputStream out = new FileOutputStream(fileName);
			prettyWrite(in, indent, out, encoding);
		} else if (fileName != null) {
			throw new RuntimeException(
				"'-o' switch can't be used for group of files\n" + info);
		} else {
			File[] files = new File[xdefs.values().size()];
			xdefs.values().toArray(files);
			prettyWrite(files, outDir, indent, encoding);
		}
	}
}