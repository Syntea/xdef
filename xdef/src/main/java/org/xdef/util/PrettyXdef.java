package org.xdef.util;

import org.xdef.XDConstants;
import org.xdef.sys.SUtils;
import org.xdef.xml.KDOMBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Generate formatted source of the X-definitions.
 * Also provides main method for calling the program from command line.
 * (see {@link cz.syntea.xdef.util.PrettyXdef#main(String[])})
 * @author Vaclav Trojan
 */
public class PrettyXdef {

	/** Prevent user to create a new instance of PrettyXdef */
	private PrettyXdef() {}

	/** Write pretty formatted files with source X-definitions.
	 * @param files Array of files.
	 * @param outDir directory where to put result files.
	 * @param indentStep number of indent spaces..
	 * @param encoding The character encoding name or null.
	 * @param newPrefix The new name space prefix (if null the prefix remains
	 * unchanged.
	 */
	public static void prettyWrite(File[] files,
		File outDir,
		int indentStep,
		String encoding,
		String newPrefix) {
		for (int i = 0; i < files.length; i++) {
			prettyWriteToDir(files[i], outDir, indentStep, encoding, newPrefix);
		}
	}

	/** Write pretty formatted file with source X-definition to given directory.
	 * @param file The input file.
	 * @param outDir directory where to put result files.
	 * @param indentStep number of indent spaces..
	 * @param encoding The character encoding name or null.
	 * @param newPrefix The new name space prefix (if null the prefix remains
	 * unchanged.
	 */
	public static void prettyWriteToDir(File file,
		File outDir,
		int indentStep,
		String encoding,
		String newPrefix) {
		String outDirName = outDir.getAbsolutePath().replace('\\','/');
		if (!outDirName.endsWith("/")) {
			outDirName += '/';
		}
		String fname = file.getAbsolutePath();
		if (!file.exists() || !file.canRead()) {
			System.err.println("Can't read file '" + fname + "'");
		}
		fname = fname.replace('\\','/');
		int index = fname.lastIndexOf('/');
		if (index < 0) {
			fname = outDirName + fname;
		} else {
			fname = outDirName + fname.substring(index + 1);
		}
		prettyWrite(file, fname, indentStep, encoding, newPrefix);

	}

	/** Write pretty formatted file with source X-definition.
	 * @param input the source file.
	 * @param fname the name of result file.
	 * @param indentStep number of indent spaces..
	 * @param encoding The character encoding name or null.
	 * @param newPrefix The new name space prefix (if null the prefix remains
	 * unchanged.
	 */
	public static void prettyWrite(File input,
		String fname,
		int indentStep,
		String encoding,
		String newPrefix) {
		try {
			KDOMBuilder kb = new KDOMBuilder();
			kb.setNamespaceAware(true);
			kb.setCoalescing(true);
			kb.setExpandEntityReferences(true);
			kb.setIgnoringComments(false);
			Element elem = kb.parse(input).getDocumentElement();
			prettyWrite(elem, fname, indentStep, encoding, newPrefix);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	/** Write pretty formatted X-definition.
	 * @param input the element with pool of definitions or X-definition.
	 * @param fname the name of output file.
	 * @param indentStep number of indent spaces..
	 * @param encoding The character encoding name or null.
	 * @param newPrefix The new name space prefix (if null the prefix remains
	 * unchanged).
	 */
	private static void prettyWrite(Element input,
		String fname,
		int indentStep,
		String encoding,
		String newPrefix) {
		try {
			if (encoding == null || encoding.isEmpty()) {
				encoding = "UTF-8";
			}
			String oldPrefix;
			int index = input.getNodeName().indexOf(':');
			if (index <= 0) {
				System.err.println("Input is not X-definition");
				return;
			}
			oldPrefix = input.getNodeName().substring(0,index + 1);
			String oldPrefixNS =
				"xmlns:" + oldPrefix.substring(0, oldPrefix.length() - 1);
			if (!XDConstants.XDEF20_NS_URI.equals(
				input.getAttribute(oldPrefixNS))
				&& !XDConstants.XDEF31_NS_URI.equals(
					input.getAttribute(oldPrefixNS))
				&& !XDConstants.XDEF32_NS_URI.equals(
					input.getAttribute(oldPrefixNS))
				&& !XDConstants.XDEF40_NS_URI.equals(
					input.getAttribute(oldPrefixNS))) {
				System.err.println("Input is not X-definition");
				return;
			}
			String xdNS = input.getAttribute(oldPrefixNS);
			if (newPrefix == null || newPrefix.length() == 0) {
				newPrefix = oldPrefix;
			} else if (!newPrefix.endsWith(":")) {
				newPrefix += ":";
			}
			OutputStreamWriter out =
				new OutputStreamWriter(new FileOutputStream(fname),encoding);
			out.write("<?xml version=\"1.0\" encoding=\""+encoding+"\" ?>\n\n");
			String defPoolName = newPrefix + "collection";
			NodeList nl = input.getChildNodes();
			NamedNodeMap nm = input.getAttributes();
			int maxlen = getMaxAttrLen(nm, oldPrefix, newPrefix);
			if (input.getNodeName().equals(oldPrefix + "collection")) {
				out.write("<" + defPoolName);
				writeAttr(out,
					"xmlns:" + newPrefix.substring(0, newPrefix.length() - 1),
					xdNS,
					false,
					1,
					newPrefix.length() + indentStep,
					maxlen,
					oldPrefix,
					newPrefix);
				String s = input.getAttribute(oldPrefix + "include");
				if (s != null && s.length() > 0) {
					StringTokenizer st = new StringTokenizer(s," \t\n\r\f,");
					String value = "";
					while (st.hasMoreTokens()) {
						if (value.length() > 0) {
							value += ",\n" + getSpaces(maxlen + 12);
						}
						value += st.nextToken();
					}
					writeAttr(out,
						newPrefix + "include",
						value,
						true,
						1,
						newPrefix.length() + indentStep,
						maxlen,
						oldPrefix,
						newPrefix);
				}
				for (int i = 0; i < nm.getLength(); i++) {
					Node item = nm.item(i);
					String name = item.getNodeName();
					if (name.startsWith(oldPrefix)
						|| name.equals(oldPrefixNS)) {
						continue;
					}
					writeAttr(out,
						updateName(name, oldPrefix, newPrefix),
						item.getNodeValue(),
						true,
						1,
						newPrefix.length() + indentStep,
						maxlen,
						oldPrefix,
						newPrefix);
				}
				out.write(" >\n");
				for (int i = 0; i < nl.getLength(); i++) {
					Node item = nl.item(i);
					switch (item.getNodeType()) {
						case Node.ELEMENT_NODE:
							writeDefinition(out,
								(Element)item,
								indentStep,
								oldPrefix,
								newPrefix);
							continue;
						case Node.COMMENT_NODE:
							s = item.getNodeValue();
							if (s == null) {
								continue;
							}
							s = s.trim();
							if (s.isEmpty()) {
								continue;
							}
							out.write("\n<!-- "	+ s + " -->\n");
					}
				}
			} else if (input.getNodeName().equals(oldPrefix + "def")) {
				out.write("<" + defPoolName + " xmlns:" +
					newPrefix.substring(0, newPrefix.length() - 1) +
					" = \"" + xdNS + "\" >\n");
				writeDefinition(out, input, indentStep, oldPrefix, newPrefix);
			} else {
				System.err.println("Definition error");
			}
			out.write("\n</" + defPoolName + ">\n");
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
			System.err.println("IO error thrown when writing to file " + fname);
		}
	}

	/** Replace old prefix with new prefix. */
	private static String updateName(String name,
		String oldPrefix,
		String newPrefix) {
		if (name.equals("xmlns:"+oldPrefix.substring(0, oldPrefix.length()-1))){
			return "xmlns:" + newPrefix.substring(0, newPrefix.length() - 1);
		}
		if (!name.startsWith(oldPrefix)) {
			return name;
		}
		return newPrefix + name.substring(oldPrefix.length());
	}

	/** Find the longest attribute name (with new prefix). */
	private static int getMaxAttrLen(NamedNodeMap nm,
		String oldPrefix,
		String newPrefix) {
		int len = nm.getLength();
		int maxlen = 0;
		for (int i = 0; i < len; i++) {
			int j = updateName(nm.item(i).getNodeName(), oldPrefix, newPrefix)
				.length();
			if (j > maxlen) {
				maxlen = j;
			}
		}
		return 2;
	}

	/** Write attribute to output file. Replace old namespace prefix with
	 * the new one.
	 * @param out The output stream.
	 * @param name The name of attribute.
	 * @param value The value of attribute.
	 * @param wasFirst <tt>true</tt> if first attribute was printed.
	 * @param firstAttrIndent The indent space of the first attribute.
	 * @param indent The indent of following attributes.
	 * @param maxlen The length of longest attribute name.
	 * @param oldPrefix the old namespace prefix.
	 * @param newPrefix the new namespace prefix.
	 */
	private static void writeAttr(OutputStreamWriter out,
		String name,
		String value,
		boolean wasFirst,
		int firstAttrIndent,
		int indent,
		int maxlen,
		String oldPrefix,
		String newPrefix) throws IOException {
		if (!wasFirst) {
			out.write(getSpaces(firstAttrIndent));
		} else {
			out.write('\n' + getSpaces(indent));
		}
		name = updateName(name, oldPrefix, newPrefix);
		out.write(name);
		out.write(getSpaces(maxlen - name.length()));
		value = value.trim();
		out.write(" = \"");
		StringBuilder sb = new StringBuilder();
		int len = value.length();
		for (int j = 0; j < len; j++) {
			char c;
			switch (c = value.charAt(j)) {
				case '<':
					sb.append("&lt;");
					continue;
				case '&':
					sb.append("&amp;");
					continue;
				case '"':
					sb.append("&quot;");
					continue;
				default:
					sb.append(c);
			}
		}
		out.write(sb.toString());
		out.write('"');
	}

	/** Get string with given number of spaces.
	 * @param num Number of spaces.
	 * @return The string with given number of spaces.
	 */
	private static String getSpaces(int num) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	/** Write element to output stream.
	 * @param out The output stream.
	 * @param xel The element.
	 * @param indentStep number of indent spaces..
	 * @param indent The indent of following attributes.
	 * @param maxElemLen The length of longest element tag name.
	 * @param oldPrefix the old namespace prefix.
	 * @param newPrefix the new namespace prefix.
	 */
	private static void writeElem(OutputStreamWriter out,
		Element xel,
		int indentStep,
		int indent,
		String oldPrefix,
		String newPrefix) throws IOException {
		String elName =  updateName(xel.getNodeName(), oldPrefix, newPrefix);
		out.write('\n' + getSpaces(indent) + "<" + elName);
		NamedNodeMap nm = xel.getAttributes();
		int maxlen = getMaxAttrLen(nm, oldPrefix, newPrefix);
		boolean first = false;
		int attrIndent = indent + elName.length() + 2;
		int firstAttrIndent = 1;
		Node item = nm.getNamedItem(oldPrefix + "script");
		String s;
		if (item != null) {
			writeAttr(out,
				item.getNodeName(),
				item.getNodeValue(),
				false,
				firstAttrIndent,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		int len = nm.getLength();
		for (int i = 0; i < len; i++) {
			item = nm.item(i);
			String name = item.getNodeName();
			if (name.startsWith(oldPrefix)) {
				continue;
			}
			writeAttr(out,
				updateName(name, oldPrefix, newPrefix),
				item.getNodeValue(),
				first,
				firstAttrIndent,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		NodeList nl = xel.getChildNodes();
		maxlen = getMaxElemNameLen(nl, oldPrefix, newPrefix);
		if (nl.getLength() == 0) {
			out.write(" />");
		} else {
			out.write(" >");
			for (int i = 0; i < nl.getLength(); i++) {
				item = nl.item(i);
				switch (item.getNodeType()) {
					case Node.ELEMENT_NODE:
						writeElem(out,
							(Element) item,
							indentStep,
							indent + indentStep,
							oldPrefix,
							newPrefix);
						continue;
					case Node.CDATA_SECTION_NODE:
						s = item.getNodeValue();
						if (s == null) {
							continue;
						}
						s = s.trim();
						if (s.isEmpty()) {
							continue;
						}
						out.write("\n" + getSpaces(indent + indentStep)
							+ "<![CDATA[\n");
						out.write(s);
						out.write("\n" + getSpaces(indent + indentStep)+"]]>");
						continue;
					case Node.TEXT_NODE:
						s = item.getNodeValue();
						if (s == null) {
							continue;
						}
						s = s.trim();
						if (s.isEmpty()) {
							continue;
						}
						out.write("\n" + getSpaces(indent + indentStep) + s);
						continue;
					case Node.COMMENT_NODE:
						s = item.getNodeValue();
						if (s == null) {
							continue;
						}
						s = s.trim();
						if (s.isEmpty()) {
							continue;
						}
						out.write("\n" + getSpaces(indent + indentStep)
							+ "<!-- " + s + " -->");
				}

			}
			out.write('\n' + getSpaces(indent) + "</" + elName + ">");
		}
	}

	/** Get size of longest element tag name from given node list. Old namespace
	 * prefix replace with the new one.
	 * @param nl node list.
	 * @param oldPrefix old namespace prefix.
	 * @param newPrefix new namespace prefix.
	 */
	private static int getMaxElemNameLen(NodeList nl,
		String oldPrefix,
		String newPrefix) {
		int len = nl.getLength();
		int maxlen = 0;
		for (int i = 0; i < len; i++) {
			if (nl.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			int j = updateName(nl.item(i).getNodeName(), oldPrefix, newPrefix)
				.length();
			if (j > maxlen) {
				maxlen = j;
			}
		}
		return maxlen;
	}

	/** Write X-definition to output stream.
	 * @param out output stream.
	 * @param xdef X-definition.
	 * @param indentStep number of indent spaces..
	 * @param oldPrefix old name space prefix.
	 * @param newPrefix new name space prefix.
	 */
	private static void writeDefinition(OutputStreamWriter out,
		Element xdef,
		int indentStep,
		String oldPrefix,
		String newPrefix) throws IOException {
		String defName = newPrefix + "def";
		out.write("\n<" + defName);
		int attrIndent = defName.length() + 2;
		NamedNodeMap nm = xdef.getAttributes();
		int len = nm.getLength();
		int maxlen = getMaxAttrLen(nm, oldPrefix, newPrefix);
		boolean first = false;
		Node item = nm.getNamedItem(oldPrefix + "name");
		if (item != null) {
			writeAttr(out,
				item.getNodeName(),
				item.getNodeValue(),
				false,
				1,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		item = nm.getNamedItem(oldPrefix + "root");
		if (item != null) {
			StringTokenizer st =
				new StringTokenizer(item.getNodeValue()," \r\t\n\f|");
			String value = "";
			while (st.hasMoreTokens()) {
				if (value.length() > 0) {
					value += '\n' + getSpaces(maxlen + 12) + "| ";
				}
				value += st.nextToken();
			}
			writeAttr(out,
				item.getNodeName(),
				value,
				first,
				1,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		item = nm.getNamedItem(oldPrefix + "classes");
		if (item != null) {
			writeAttr(out,
				item.getNodeName(),
				item.getNodeValue(),
				first,
				1,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		item = nm.getNamedItem(oldPrefix + "script");
		if (item != null) {
			writeAttr(out,
				item.getNodeName(),
				item.getNodeValue(),
				first,
				1,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		item = nm.getNamedItem(oldPrefix + "include");
		if (item != null) {
			String s = item.getNodeValue();
			if (s != null && s.length() > 0) {
				StringTokenizer st = new StringTokenizer(s," \t\n\r\f,");
				String value = "";
				while (st.hasMoreTokens()) {
					if (value.length() > 0) {
						value += ",\n" + getSpaces(maxlen + 12);
					}
					value += st.nextToken();
				}
				writeAttr(out,
					item.getNodeName(),
					s,
					first,
					1,
					attrIndent,
					maxlen,
					oldPrefix,
					newPrefix);
			}
		}
		String oldPrefixNS = "xmlns:"
			+ oldPrefix.substring(0, oldPrefix.length() - 1);
		for (int i = 0; i < len; i++) {
			item = nm.item(i);
			String name = item.getNodeName();
			if (name.startsWith(oldPrefix) || name.equals(oldPrefixNS)) {
				continue;
			}
			writeAttr(out,
				name,
				item.getNodeValue(),
				first,
				1,
				attrIndent,
				maxlen,
				oldPrefix,
				newPrefix);
			first = true;
		}
		NodeList nl = xdef.getChildNodes();
		maxlen = getMaxElemNameLen(nl, oldPrefix, newPrefix);
		len = nl.getLength();
		if (len == 0) {
			out.write(" />\n");
		} else {
			out.write(" >");
			String s;
			for (int i = 0; i < len; i++) {
				out.write('\n');
				item = nl.item(i);
				switch (item.getNodeType()) {
					case Node.ELEMENT_NODE:
						writeElem(out,
							(Element)item,
							indentStep,
							indentStep,
							oldPrefix,
							newPrefix);
						continue;
					case Node.COMMENT_NODE:
						s = item.getNodeValue();
						if (s == null) {
							continue;
						}
						s = s.trim();
						if (s.isEmpty()) {
							continue;
						}
						out.write("\n" + getSpaces(indentStep) + "<!-- "
							+ s + " -->");
				}
			}
		}
		out.write("\n\n</" + defName + ">\n");
	}

	/** Calling the program from command line.
	 * @param args The array of strings with arguments.
		 * <tt>[-d outDir | -o outFile] [-i n] [-e encoding] [-p prefix] file</tt>
		 * <ul>
		 *  <li><tt>-o outFile</tt> - output file or output directory.
		 * If this parameter is not specified then input file is replaced by the
		 * formatted version.</li>
		 *  <li><tt>-d outDir</tt> - output directory. If this parameter is not
		 * specified the formated files will be replaced by the the formated
		 * version.</li>
		 *  <li><tt>-i n</tt> - number of spaces used for indentation. If this
		 * parameter is not specified the parameter is set to 2.</li>
		 *  <li><tt>-e encoding</tt> - name of character set. If this parameter
		 * is not specified it will be used the original character set.</li>
		 *  <li><tt>-p prefix</tt> - the prefix of the X-definition name space.
		 * If this parameter is not specified it will be used the original prefix.</li>
		 *  <li><tt>file</tt> - the file with source X-definition.</li>
		 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"PrettyXdef - Formating of source files with X-definitions.\n"+
"Usage: [-d outDir | -o outFile] [-i n] [-e encoding] [-p prefix] file\n"+
"where:\n"+
"-o outFile  Output file or out directory. If this parameter is not\n"+
"            specified then input file is replaced by the formated version.\n"+
"-d outDir   Output directory. If this parameter is not specified the\n"+
"            formated files will be replaced by the the formated version.\n"+
"-i n        Number of spaces used for indentation. If this parameter is\n"+
"            not specified the parameter is set to 2.\n"+
"-e encoding Name of character set. If this parameter is not specified\n"+
"            it will be used the original character set.\n"+
"-p prefix   The prefix of the X-definition namespace. If this parameter\n"+
"            is not specified it will be used the original prefix.\n"+
"file        The file with source X-definition.\n"+
"(c)2007 Syntea Software Group";
		HashMap<String, File> xdefs = new HashMap<String, File>();
		File outDir = null;
		String fileName = null;
		String newPrefix = null;
		String encoding = "UTF-8";
		int indent = -1;
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
					if (indent == -1) {
						try {
							indent = Integer.parseInt(s);
							if (indent >= 0) {
								continue;
							}
						} catch (Exception ex) {
						}
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
				case 'p': {//new prefix
					if (s == null) {
						msg += "Parameter [" + swNum +
							"], '" + args[swNum-1] +
							"': missing following argument\n";
						continue;
					}
					newPrefix = s;
					if (s.startsWith("xml")) {
						msg += "prefix can't start with 'xml'\n";
						s = null;
					} else {
						for (int j = 0; j < s.length(); j++) {
							if (Character.isLetterOrDigit(newPrefix.charAt(j))){
								continue;
							}
							msg += "Incorrect prefix specification\n";
							s = null;
							break;
						}
					}
					newPrefix = s;
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
			for (int j = 0; j < files.length; j++) {
				if (files[j].exists()
					&& files[j].isFile()
					&& files[j].canRead())
				{
					try {
						xdefs.put(files[j].getCanonicalPath(),files[j]);
					} catch (Exception ex) {
						msg += "Error on file " +
							files[j].getAbsoluteFile() + "\n";
					}
				} else {
					msg += "Error on file " + files[j].getAbsoluteFile() + "\n";
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
		if (indent <= 0) {
			indent = 2;
		}
		if (xdefs.size() == 1 && fileName != null) {
			prettyWrite((File)xdefs.values().toArray()[0],
				fileName, indent, encoding, newPrefix);
		} else if (fileName != null) {
			throw new RuntimeException(
				"'-o' switch can't be used for group of files\n" + info);
		} else {
			File[] files = new File[xdefs.values().size()];
			xdefs.values().toArray(files);
			prettyWrite(files, outDir, indent, encoding, newPrefix);
		}
	}
}