package org.xdef.util;

import org.xdef.sys.SUtils;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.xdef.XDContainer;
import org.xdef.XDNamedValue;
import org.xdef.XDParser;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import org.xdef.impl.parsers.XDParseEnum;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;

/** Generation of DTD from X-definitions.
 * Also provides main method for calling the program from command line.
 * (see {@link org.xdef.util.GenDTD#main(String[])})
 * @author Vaclav Trojan
 */
public class GenDTD {

	/** The output stream for DTD */
	private final OutputStreamWriter _out;
	/** HashMap which prevents repeated declaration of elements */
	private Set<String> _defElems;

	/** Creates a new instance of DTD */
	private GenDTD(final OutputStreamWriter out) {_out = out;}

	/** Generate DTD from list of input definitions
	 * @param inputNames The array of source file names.
	 * @param elemName The name of root element.
	 * @param out The output stream.
	 * @throws IOException if IOError occurs on output.
	 */
	public static void genDTD(final String[] inputNames, final String elemName, final OutputStreamWriter out)
		throws IOException {
		genDTD(getInputStreamsFromFiles(SUtils.getFileGroup(inputNames)), elemName, out);
	}

	/** Create array of input streams created from array of files.
	 * @param files The array of files.
	 * @return The array of of input streams.
	 */
	private static InputStream[] getInputStreamsFromFiles(final File[] files) throws IOException {
		InputStream[] result = new InputStream[files.length];
		for (int i = 0; i < files.length; i++) {
			result[i] = new FileInputStream(files[i]);
		}
		return result;
	}

	/** Generate DTD from list of input definitions
	 * @param inputStreams The array of source input streams.
	 * @param elemName The name of root element.
	 * @param out The output stream.
	 * @throws IOException if IOError occurs on output.
	 */
	public static void genDTD(final InputStream[] inputStreams,
		final String elemName,
		final OutputStreamWriter out) throws IOException {
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT, "true");
		XDBuilder xb = XDFactory.getXDBuilder(props);
		xb.setSource(inputStreams, null);
		genDTD(xb.compileXD(), elemName, out);
	}

	/** Returns the element model represented by given name or null if definition item isn't available.
	 * @param xdef XMdefinition.
	 * @param key The name of definition item used for search.
	 * @return The required XElement or null.
	 */
	private static XMElement getXElement(final XMDefinition xdef,
		final String key) {
		int ndx = key.lastIndexOf('#');
		String lockey;
		XMDefinition def;
		if (ndx < 0) { //reference to this set, element with the name from key.
			lockey = key;
			def = xdef;
		} else {
			def = xdef.getXDPool().getXMDefinition(key.substring(0,ndx));
			if (def == null) {
				return null;
			}
			lockey = key.substring(ndx + 1);
		}
		XMElement[] elems = def.getModels();
		for (XMElement xel : elems) {
			if (lockey.equals(xel.getName())) {
				return xel;
			}
		}
		return null;
	}

	/** Generate DTD from list of input definitions
	 * @param xp Refers to the XDefPool object.
	 * @param name The name of root element.
	 * @param out The output stream.
	 * @throws IOException if IOError occurs on output.
	 */
	public static void genDTD(final XDPool xp, final String name, final OutputStreamWriter out)
		throws IOException {
		String defName, elemName;
		defName = elemName = name;
		int ndx = defName.indexOf('#');
		if (ndx >= 0) {
			if (ndx == 0) {
				elemName = defName = elemName.substring(1);
			} else {
				defName = elemName.substring(0, ndx);
				elemName = elemName.substring(ndx+1);
			}
		}
		XMDefinition def = xp.getXMDefinition(defName);
		if (def == null) {
			System.out.println("Unknown definition: " + defName);
			return;
		}
		XMElement defElem = getXElement(def, elemName);
		if (defElem == null) {
			System.out.println("Unknown root element: " + elemName);
			return;
		}
		GenDTD gd = new GenDTD(out);
		gd._defElems = new HashSet<>();
		gd.genDTD(defElem);
	}

	/** Get values of enumeration type (list, tokens, eq, string).
	 * @return string array with values specified as enumeration or return null if specified type is not
	 * enumeration of string values.
	 */
	private static String[] getEnumerationValues(XDValue code) {
		if (code == null) {
			return null;
		}
		if (code.getCode() == LD_CONST && code.getItemId() == XD_PARSER) {
			XDParser p = (XDParser) code;
			XDContainer pars = p.getNamedParams();
			if (pars == null) {
				return null;
			}
			XDNamedValue n;
			String name = p.parserName();
			n = "list".equals(name) || "tokens".equals(name) ? pars.getXDNamedItem("argument")
				: "string".equals(name) ? pars.getXDNamedItem("enumeration") : null;
			if (n == null) {
				return null;
			}
			XDValue v;
			if ((v = n.getValue()) != null && !v.isNull()) {
				if (v.getItemId() == XDValue.XD_CONTAINER) {
					XDContainer c = (XDContainer) v;
					XDValue[] vv = c.getXDItems();
					int num = vv == null ? 0 : vv.length;
					if (num == 0) {
						return null;
					}
					String[] result = new String[num];
					for (int i = 0; i < num; i++) {
						result[i] = "" + vv[i];
					}
					return result;
				} else {
					String s;
					if ((s = v.toString()) == null || s.isEmpty()) {
						return null;
					}
					if ("tokens".equals(name)) {
						// Convert string to array of strings
						XDContainer context = XDParseEnum.tokensToContainer(s);
						int num = context.getXDItemsNumber();
						if (num == 0) {
							return null;
						}
						String[] result = new String[num];
						for(int i=0; i < num; i++) {
							result[i] = context.getXDItem(i).toString();
						}
						return result;
					} else {
						return new String[] {s};
					}
				}
			}
		}
		return null;
	}


	/** Generate attribute list.
	 * @param xel The XElement object.
	 * @throws IOException if IO error occurs.
	 */
	private void genAttlist(final XMElement xel) throws IOException {
		XMData[] attrs = xel.getAttrs();
		if (attrs.length > 0) {
			_out.write("<!ATTLIST " + xel.getName() + "\n");
			boolean attlistClosed = false;
			for (int i = 0; i < attrs.length; i++) {
				XMData xAttr = attrs[i];
				_out.write("          ");
				_out.write(xAttr.getName());
				_out.write(' ');
				if (!xAttr.isIllegal()) {
					if (xAttr.isIgnore()) {
						_out.write("CDATA #IMPLIED");
					} else {
						XDValue xv;
						if (xAttr.isFixed() && (xv = xAttr.getFixedValue()) != null) {
							_out.write("CDATA #FIXED \"");
							_out.write(xv.toString());
							_out.write("\"");
						} else {
							String[] en = getEnumerationValues(xAttr.getParseMethod());
							if (en != null && en.length > 0) {
								_out.write("(");
								_out.write(en[0]);
								for (int j = 1; j < en.length; j++) {
									_out.write("|");
									_out.write(en[j]);
								}
								_out.write(") ");
							} else {
								_out.write("CDATA ");
							}
							xv = xAttr.getDefaultValue();
							if (xv != null) {
								_out.write('"');
								_out.write(xv.toString());
								_out.write('"');
							} else {
								_out.write(xAttr.isOptional() ? "#IMPLIED" : "#REQUIRED");
							}
						}
					}
					if (i == attrs.length-1) {
						_out.write('>');
						attlistClosed = true;
					}
					_out.write('\n');
				}
			}
			if (!attlistClosed) {
				_out.write(">\n");
			}
		}
	}

	private static String getOccurrenceChar(XMNode dn) {
		if (dn.minOccurs() == 0) {
			if (dn.maxOccurs() > 1) {
				return "*";
			} else if (dn.maxOccurs() == 1) {
				return "?";
			}
		} else if (dn.minOccurs() >= 1) {
			if (dn.maxOccurs() > 1) {
				return "+";
			}
		}
		return "";
	}

	/** Generate DTD.
	 * @param def The XElement object.
	 * @throws IOException if IO error occurs.
	 */
	private void genDTD(final XMElement def) throws IOException {
		if (!_defElems.add(def.getName())) {
			return; //don't generate duplicities
		}
		_out.write("<!ELEMENT " + def.getName());
		boolean wasFirst = false;
		final String seqSeparator = ", ";
		final String selSeparator = " | ";
		String separator = seqSeparator;
		XMNode[] childNodes = def.getChildNodeModels();
		boolean isMixed = false;
		List<String> elements = new ArrayList<>();
		String name;
		XMNode dn;
		StringBuilder sb = new StringBuilder();
		for (XMNode childNode : childNodes) {
			dn = childNode;
			name = dn.getName();
			short kind;
			if ((kind = dn.getKind()) == XMTEXT) {
				isMixed = true;
				separator = selSeparator;
				sb.append("#PCDATA");
				wasFirst = true;
			} else if (kind == XMELEMENT) {
				if (elements.contains(name)) {
					System.out.println("WARNING: duplicate name $" + def.getName() + "/" + name);
				}
			}
		}
		for (int i = 0; i < childNodes.length; i++) {
			dn = childNodes[i];
			switch (dn.getKind()) {
				case XMELEMENT: {
					XMElement de = (XMElement)dn;
					name = de.getName();
					if (name.indexOf(":any") > 0) {
						System.out.println("WARNING: '" + name + "' referred in '" + def.getName()
							+ "', will be ignored.");
						continue;
					}
					if (isMixed) {
						if (elements.contains(name)) {
							continue;
						}
						elements.add(name);
					}
					if (wasFirst) {
						sb.append(separator).append("\n          ");
					}
					sb.append(name);
					if (de.minOccurs() == 0 && de.maxOccurs() == 1) {
						if (!isMixed) {
							sb.append('?');
						}
					} else if (de.minOccurs() >= 1 && de.maxOccurs() > 1) {
						if (!isMixed) {
							sb.append('+');
						}
					} else if (de.minOccurs() == 0 && de.maxOccurs() > 1) {
						if (!isMixed) {
							sb.append('*');
						}
					}
					wasFirst = true;
					break;
				}
				case XMMIXED: {
					if (wasFirst) {
						sb.append(separator).append("\n          ");
					}
					for (int j = 0; true; j++) {
						short kind = childNodes[++i].getKind();
						if (kind == XMSELECTOR_END) {
							break;
						}
						if (kind != XMNode.XMELEMENT) {
							continue; //can be a text value
						}
						XMElement de = (XMElement)childNodes[i];
						if (j > 0) {
							sb.append(selSeparator).append("\n          ");
						}
						sb.append(de.getName());
						if (de.minOccurs() == 0 && de.maxOccurs() == 1) {
							sb.append('?');
						} else if (de.minOccurs() >= 1 &&
							(de.maxOccurs() > 1 || de.maxOccurs() == 0)) {
							sb.append('+');
						}
					}
					if (seqSeparator.equals(separator) && (wasFirst || i + 1 < childNodes.length)) {
						sb.insert(0,'(');
						sb.append(")*\n");
					} else {
						isMixed = true;
					}
					wasFirst = true;
					break;
				}
				case XMSEQUENCE: {
					if (wasFirst) {
						sb.append(separator).append("\n          ");
					}
					String occ = getOccurrenceChar(dn) + "\n";
					sb.append('(');
					for (int j = 0; true; j++) {
						dn = childNodes[++i];
						if (dn.getKind() == XMSELECTOR_END) {
							i--;
							break;
						}
						if (j > 0) {
							sb.append(seqSeparator).append("\n          ");
						}
						sb.append(dn.getName());

					}
					sb.append(')').append(occ);
					wasFirst = true;
					break;
				}
				case XMCHOICE: {
					if (wasFirst) {
						sb.append(separator).append("\n          ");
					}
					String occ = getOccurrenceChar(dn);
					sb.append('(');
					for (int j = 0; true; j++) {
						dn = childNodes[++i];
						if (dn.getKind() == XMSELECTOR_END) {
							i--;
							break;
						}
						if (j > 0) {
							sb.append(selSeparator).append("\n          ");
						}
						sb.append(dn.getName());

					}
					sb.append(')').append(occ).append('\n');
					wasFirst = true;
				}
			}
		}
		if (wasFirst) {
			_out.write(" (");
			_out.write(sb.toString());
			_out.write(")");
			if (isMixed) {
				_out.write('*');
			}
		} else {
			_out.write(" EMPTY");
		}
		_out.write(">\n");
		genAttlist(def);
		for (XMNode childNode : childNodes) {
			if ((dn = childNode).getKind() == XMELEMENT) {
				if (dn.getName().startsWith("xd:any")) {
					continue;
				}
				genDTD((XMElement)dn);
			}
		}
	}

	/** String with command line information. */
	private static final String INFO =
"Generate DTD from X-definition.\n"+
"Command line arguments:\n"+
"  definition_name#root_element_name\n" +
"  output_file_name\n" +
"  [-e encoding]   (default is \"UTF-8\")\n" +
"  file_xdef1 [file_xdef2] [...]\n" +
"NOTE: wildcard chars '*' or '?' are possible for xdef files.";

	/** Calling the program from command line.
	 * @param args The array of arguments.
		 * <code>definition_name#root_element_name output_file_name [-e encoding]
		 * file_xdef1 [file_xdef2] [...]</code>
		 *  <ul>
		 *  <li>definition_name#root_element_name - name of X-definition and name of root element
		 *  <li>output_file_name - name of output file
		 *  <li>[-e encoding] - output file encoding (default is "UTF-8")
		 *  <li><code>file_xdef1 [file_xdef2] [...]
		 * </code> - X-definition file(s)
		 * </ul>
	 */
	public static void main(final String... args) {
		if (args.length < 3) {
			throw new RuntimeException("Missing parameters\n" + INFO);
		}
		Map<File, FileInputStream> fileTab = new LinkedHashMap<>();
		String encoding = "UTF-8";
		for (int i = 2; i < args.length; i++) {
			if (args[i].startsWith("-e")) {
				if (args[i].length() == 2) {
					if (++i == args.length) {
						throw new RuntimeException("Missing encoding\n" + INFO);
					}
					encoding = args[i];
				} else {
					encoding = args[i].substring(2);
				}
				continue;
			}
			File f = new File(args[i]);
			if (!f.exists() || !f.canRead()) {
				throw new RuntimeException("Can't read file: " + args[i] + "\n" + INFO);
			} else {
				try {
					File[] files = SUtils.getFileGroup(args[i]);
					for (File file : files) {
						fileTab.put(file.getCanonicalFile(), new FileInputStream(file));
					}
				} catch (IOException ex) {
					throw new RuntimeException("Can't open file: " + args[i] + "\n" + INFO);
				}
			}
		}
		if (fileTab.isEmpty()) {
			throw new RuntimeException("No valid input file\n" + INFO);
		}
		OutputStreamWriter out = null;
		try {
			out = new java.io.OutputStreamWriter(new java.io.FileOutputStream(args[1]), encoding);
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			throw new RuntimeException("Invalid output file: " + args[1] + "\n" + INFO);
		}
		try {
			out.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
			InputStream[] streams = new InputStream[fileTab.values().size()];
			fileTab.values().toArray(streams);
			genDTD(streams, args[0], out);
			out.close();
		} catch (IOException ex) {
			throw new RuntimeException("DTD not generated.", ex);
		}
	}
}
