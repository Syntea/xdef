package org.xdef.xml;

import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SException;
import org.xdef.sys.SIOException;
import org.xdef.sys.SRuntimeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.ReportWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;

/** Collection of static methods designed for easy work with XML data.
 * @author Vaclav Trojan
 */
public final class KXmlUtils extends KDOMUtils {
	/** Recommended default length of source line.*/
	private final static int LINELENGTH = 80;
	/** Root map of prefixes.*/
	private final static Map<String, String> ROOT_NSPREFIXMAP = new LinkedHashMap<>();
	static {
		ROOT_NSPREFIXMAP.put("xmlns", "");
	}

	/** Creates an XML Document object with created document builder (see SetDOMImplementation).
	 * @return new Document object.
	 */
	public static final Document newDocument() {return new KDOMBuilder().newDocument();}

	/** Creates an XML Document object with empty root element created by document builder
	 * (see SetDOMImplementation).
	 * @param ns namespace of created root element (or null).
	 * @param qname qualified name of root element.
	 * @param docType DocumentType object or null.
	 * @return new Document object with empty root element.
	 */
	public static final Document newDocument(final String ns, final String qname, final DocumentType docType){
		return new KDOMBuilder().newDocument(ns, qname, docType);
	}

	/** The DOMImplementation object that handles this document.
	 * A DOM application may use objects from multiple implementations.
	 * @return  The DOMImplementation object.
	 */
	public static final DOMImplementation getDOMImplementation() {
		return new KDOMBuilder().getDOMImplementation();
	}

	/** Removes all namespaces and namespace prefixes from document and its attributes and all child nodes.
	 * If an attribute with the local name already exists the it is not replaced.
	 * @param doc document where all prefixes and namespaces of child child nodes are removed.
	 */
	public static final void setAllNSToNull(final Document doc) {
		Element el = doc.getDocumentElement();
		if (el != null) {
			Element newelem = setAllNSToNull(el);
			if (newelem != el) {
				doc.replaceChild(newelem, el);
			}
		}
	}

	/** Removes all namespaces and namespace prefixes from element and its attributes and all child nodes.
	 * If an attribute with the local name already exists then it is left unchanged. All xmlns* attributes
	 * are removed.
	 * @param elem element if scope of which all prefixes and namespaces are removed.
	 * @return element without namespaces and namespace prefixes. If
	 * nothing was changed then the original element is returned.
	 */
	public static final Element setAllNSToNull(final Element elem) {
		NamedNodeMap nm = elem.getAttributes();
		for (int i = nm.getLength() - 1; i >= 0 ; i--) {
			Attr attr = (Attr) nm.item(i);
			String qname = attr.getNodeName();
			if (qname.startsWith("xmlns:") || "xmlns".equals(qname)) {
				elem.removeAttribute(qname);
			} else {
				int ndx;
				if ((ndx = qname.indexOf(':')) > 0) {
					String lname = qname.substring(ndx + 1);
					elem.removeAttribute(qname);
					if (!elem.hasAttribute(lname)) {
						elem.setAttribute(lname, attr.getValue());
					}
				}
			}
		}
		Element result;
		int ndx;
		String qname = elem.getTagName();
		if ((ndx = qname.indexOf(':')) < 0 && elem.getNamespaceURI() == null) {
			result = elem;
		} else {
			result = elem.getOwnerDocument().createElement(qname.substring(ndx + 1));
			nm = elem.getAttributes();
			for (int i = 0; i < nm.getLength(); i++) {
				Node n = nm.item(i);
				result.setAttribute(n.getNodeName(), n.getNodeValue());
			}
		}
		Node actnode = elem.getFirstChild();
		while (actnode != null) {
			Node nxtnode = actnode.getNextSibling();
			Node newnode =
				actnode.getNodeType() == Node.ELEMENT_NODE ? setAllNSToNull((Element) actnode) : actnode;
			if (result == elem) {
				if (newnode != actnode) {
					result.replaceChild(newnode, actnode);
				}
			} else {
				result.appendChild(newnode);
			}
			actnode = nxtnode;
		}
		return result;
	}

	/** Return a string in the XML format. All occurrences of special characters (&lt;,&gt;,&amp;,",')
	 * are replaced with entity references. If the argument "ignoreWhiteSpaces" is true all ignorable
	 * white spaces are removed. If the argument delimiter is "&lt;" the result string is created for
	 * a text node, otherwise it will be created as a value of attribute and* delimiter occurrence will
	 * be transformed to the appropriate predefined entity. Note that the argument shouldn't contain
	 * an entity reference or character reference.
	 * @param delimiter attributes: '"' or "'", text nodes: '&lt;'.
	 * @param ignoreSpaces If true all ignorable white spaces are removed.
	 * @param value The original string.
	 * @return The string in canonical form.
	 */
	public static final String toXmlText(final String value, final char delimiter,final boolean ignoreSpaces){
		int len = value.length();
		int ndx = 0;
		int specialChar;
		String specials;
		if (delimiter == '<') {
			specials = "<&>";
		} else {
			specials = "<&" + delimiter;
		}
		for (char c = 0; c < ' '; c++) {
			if (delimiter != '<' || c != '\n') {
				specials += c;
			}
		}
		if (ignoreSpaces) {
			specials += ' ';
			if (delimiter == '<') {
				specials += '\n';
			}
		}
		//check special characters
		loop: {
			while (ndx < len) {
				if ((specialChar = specials.indexOf(value.charAt(ndx++))) >= 0){
					break loop; //first occurrence of special character
				}
			}
			return value; //no special characters found
		}
		int lastndx;
		if (ndx == 1 && specials.charAt(specialChar) <= ' ') {
			while (ndx < len && (specialChar = " \n".indexOf(value.charAt(ndx))) >= 0) {
				ndx++;
			}
			lastndx = ndx;
		} else {
			lastndx = 0;
		}
		StringBuilder sb = new StringBuilder(value.length() + 16);
		for (;;) {
			if (specialChar >= 0) {
				if (lastndx < ndx -1) {
					sb.append(value.substring(lastndx,ndx - 1));
				}
				char c;
				switch (c = specials.charAt(specialChar)) {
					case '<': sb.append("&lt;"); break;
					case '&': sb.append("&amp;"); break;
					case '\'': sb.append("&apos;"); break;
					case '"': sb.append("&quot;"); break;
					case '>':  //in the text value can't be "]]>"
						if (delimiter == '<' &&
							ndx > 2 && value.charAt(ndx - 2) == ']' &&
							value.charAt(ndx - 3) == ']') {
							sb.append("&gt;");
						} else {
							sb.append('>');
						}
						break;
					default:
						if (c==' ' || delimiter=='<' && (c=='\n' || c=='\t')) {
							while (ndx < len && ((c = value.charAt(ndx)) == ' '
								|| (c=='\n' || c=='\t'))) {
								ndx++;
							}
							if (ndx < len) {
								sb.append(' ');
							}
						} else {
							if ((c == '\n' || c == '\t') && delimiter == '<') {
								sb.append('\n');
							} else {
								sb.append("&#");
								sb.append(Integer.toString(c));
								sb.append(';');
							}
						}
				}
				lastndx = ndx;
			}
			if (ndx < len) {
				specialChar = specials.indexOf(value.charAt(ndx++));
			} else {
				if (lastndx < len) {
					sb.append(value.substring(lastndx));
				}
				return sb.toString();
			}
		}
	}

	/** Write XML header.
	 * @param out where to write.
	 * @param xmlVersion xml version.
	 * @param xmlEncoding file encoding.
	 * @param indentStep indentation step.
	 * @returnj true if header was written, otherwise return false.
	 * @throws IOException if an error occurs.
	 */
	private static boolean writeXmlHdr(final Writer out,
		final String xmlVersion,
		final String xmlEncoding,
		final String indentStep) throws IOException {
		if ("1.0".equals(xmlVersion) && (xmlEncoding == null || xmlEncoding.equalsIgnoreCase("UTF-8"))) {
			return false;
		}
		out.write("<?xml version=\"");
		out.write(xmlVersion != null ? xmlVersion : "1.0");
		out.write("\" encoding=\"");
		out.write(xmlEncoding != null ? xmlEncoding : "UTF-8");
		out.write("\"?>");
		if (indentStep != null) {
			out.write('\n');
		}
		return true;
	}

	/** Write text value.
	 * @param out output writer for result.
	 * @param text text value.
	 * @param isCdata true if CDATA section.
	 * @param indent indentation prefix or null.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space
	 * and value is trimmed.
	 * @param lineLen length of source line.
	 * @throws IOException if an error occurs.
	 */
	private static void writeText(final Writer out,
		final String text,
		final boolean isCdata,
		final String indent,
		final boolean removeIgnorableWhiteSpaces,
		final int lineLen) throws IOException {
		int len = text.length();
		int pos = (indent == null) ? 0 : indent.length();
		for (int j = 0; j < len; j++) {
			char c;
			switch (c = text.charAt(j)) {
				case '<':
					if (isCdata) {
						out.write('<');
						pos++;
					} else {
						out.write("&lt;");
						pos += 4;
					}
					break;
				case '&':
					if (isCdata) {
						out.write('&');
						pos++;
					} else {
						out.write("&amp;");
						pos += 5;
					}
					break;
				case '>':
					if (j >= 2 &&
						(text.charAt(j-1) == ']' || text.charAt(j-2) == ']' )) {
						if (isCdata) { // we must create other CDataSection
							out.write("><!CDATA[");
							pos += 9;
						} else {out.write("&gt;");
							pos += 4;
						}
					} else { //in the text value can't be "]]>"
						out.write('>');
						pos++;
					}
					break;
				case '\t':
				case '\n':
				case ' ':
					if (removeIgnorableWhiteSpaces) {
						if (indent != null && pos >= lineLen-1 && j + 1 < len) {
							//wrap line
							out.write(indent);
							pos = indent.length();
						} else {
							out.write(' ');
							pos ++;
						}
						while (j + 1 < len && ((c=text.charAt(j + 1)) == ' ' || c == '\n' || c == '\t')) {
							j++;
						}
					} else {
						out.write(c); // default print char
						pos++;
					}
					break;
				default:
					if (c < ' ') {
						out.write("&#");
						out.write(Integer.toString(c));
						out.write(';');
						pos += c < 10 ? 4 : 5;
					} else {
						out.write(c);
						pos++;
					}
			}
		}
	}

	/** Create value of an attribute.
	 * @param value attribute value.
	 * @param removeIgnorableWhiteSpaces if true the value is trimmed and then all white space sequences
	 * are replaced by one space.
	 * @return quoted string with the created attribute value.
	 */
	private static String createAttrValue(String value, final boolean removeIgnorableWhiteSpaces) {
		String s = value;
		char delimiter = s.indexOf('"') < 0 ? '"' : s.indexOf('\'') < 0 ? '\'' : '"';
		if (removeIgnorableWhiteSpaces) {
			s = s.trim();
		}
		StringBuilder sb = new StringBuilder(s.length() + 2);
		sb.append(delimiter);
		int len = s.length();
		for (int j = 0; j < len; j++) {
			char c;
			switch (c = s.charAt(j)) {
				case '<': sb.append("&lt;"); continue;
				case '&': sb.append("&amp;"); continue;
				case ' ':
					if (removeIgnorableWhiteSpaces) {
						sb.append(' ');
						while (j + 1 < len && s.charAt(j + 1) == ' ') {
							j++;
						}
					} else {
						sb.append(c); //default print char
					}
					continue;
			}
			if (c < ' ') {
				sb.append("&#").append(Integer.toString(c)).append(';');
			} else {
				if (c == delimiter) {
					sb.append((delimiter == '"' ? "&quot;" : "&apos;"));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.append(delimiter).toString();
	}

	/** Create string with the attribute.
	 * @param attr The attribute.
	 * @param removeIgnorableWhiteSpaces if true the value is trimmed and then all white space sequences
	 * are replaced by one space.
	 * @return string with source value of the attribute.
	 */
	private static String createAttr(final Node attr,
		final boolean removeIgnorableWhiteSpaces,
		final Map<String, String> namespaceMap,
		final Map<String, String> unresolved) {
		String name = attr.getNodeName();
		String uri = attr.getNamespaceURI();
		int ndx;
		String prefix;
		if (uri!=null && (ndx=name.indexOf(':')) >= 0 && !(prefix=name.substring(0, ndx)).startsWith("xml")) {
			String xmlnsName = "xmlns:" + prefix;
			if (!uri.equals(namespaceMap.get(xmlnsName))) {
				unresolved.put(xmlnsName, uri);
			}
		}
		return name + "=" + createAttrValue(attr.getNodeValue(), removeIgnorableWhiteSpaces);
	}

	/** Write node as XML to output stream. Format of result will be indented if the argument indentStep
	 * is string with indenting spaces. If argument indentStep is null the output is not indented. If argument
	 * true the output is in canonical form (i.e. without document type, entity references, CDATA sections are
	 * converted to text values). If argument comments is false all Comment nodes are ignored.
	 * @param out output writer for result.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param lineStart null or the line prefix.
	 * @param indentStep null or the indenting string.
	 * @param canonical if true the output is canonical form.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space.
	 * @param comments if true comments are generated.
	 * @param namespaceMap map of prefixes.
	 * @param lineLen length of source line.
	 * @throws IOException if an error occurs.
	 */
	private static void writeXml(final Writer out,
		final String encoding,
		final Node node,
		final String lineStart,
		final String indentStep,
		final boolean canonical,
		final boolean removeIgnorableWhiteSpaces,
		final boolean comments,
		final Map<String, String> namespaceMap,
		final int lineLen) throws IOException {
		// map valid for child nodes
		Map<String, String> newPrefixMap = new LinkedHashMap<>(namespaceMap);
		short type;
		String startLine = lineStart;
		String indent = indentStep;
		if ((type = node.getNodeType()) == Node.COMMENT_NODE && !comments) {
			return;
		}
		if (indent != null) {
			if (startLine == null) {
				startLine = "\n";
			} else {
				if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
					if (removeIgnorableWhiteSpaces) {
						out.write(startLine);
					} else {
						out.write("\n");
					}
				} else if (type != Node.ENTITY_REFERENCE_NODE) {
					out.write(startLine);
				}
			}
			if (startLine.length() <= 60) {
				indent = startLine.length() > 40 ? " " : "  ";
			}
		}
		switch (type) {
			case Node.ATTRIBUTE_NODE:
				out.write(node.getNodeName() + "=");
				out.write(createAttrValue(node.getNodeValue(), removeIgnorableWhiteSpaces));
				break;
			case Node.COMMENT_NODE:
				if (comments) {
					out.write("<!--");
					out.write(node.getNodeValue());
					out.write("-->");
				}
				break;
			case Node.DOCUMENT_NODE: {
				Document document = (Document)node;
				if (!writeXmlHdr(out, document.getXmlVersion(), encoding, indentStep)) {
					startLine = null; // header was not written.
				}
				Node docType = document.getDoctype();
				if (docType != null) {
					writeXml(out,
						null,
						docType,
						startLine,
						indent,
						false,
						removeIgnorableWhiteSpaces,
						comments,
						newPrefixMap,
						lineLen);
				}
				NodeList nl = node.getChildNodes();
				for (int i = 0, n = nl.getLength(); i < n; i++) {
					if (nl.item(i).getNodeType() != Node.DOCUMENT_TYPE_NODE) {
						writeXml(out,
							null,
							nl.item(i),
							startLine,
							indent,
							canonical,
							removeIgnorableWhiteSpaces,
							comments,
							newPrefixMap,
							lineLen);
					}
				}
				break;
			}
			case Node.DOCUMENT_TYPE_NODE: {
				DocumentType doctype = (DocumentType)node;
				out.write("<!DOCTYPE ");
				out.write(doctype.getName());
				String publicId = doctype.getPublicId();
				String systemId = doctype.getSystemId();
				if (publicId != null) {
					out.write(" PUBLIC '");
					out.write(publicId);
					out.write("' '");
					if (systemId != null) {
						out.write(systemId);
					}
					out.write('\'');
				} else if (systemId != null) {
					out.write(" SYSTEM '");
					out.write(systemId);
					out.write('\'');
				}
				String internalSubset = doctype.getInternalSubset();
				if (internalSubset != null) {
					if (publicId != null || systemId != null) {
						out.write(' ');
					}
					out.write("[");
					out.write(indent != null ? startLine + indent : "\n");
					out.write(internalSubset);
					out.write(']');
				}
				out.write('>');
				if (indent == null) {
					out.write('\n');
				}
				break;
			}
			case Node.ELEMENT_NODE: {
				Map<String, String> unresolved = new LinkedHashMap<>();
				if (encoding != null) {
					writeXmlHdr(out, node.getOwnerDocument().getXmlVersion(), encoding, indentStep);
				}
				out.write('<');
				String tagName = node.getNodeName();
				out.write(tagName);
				NodeList nl = node.getChildNodes();
				int numItems = nl.getLength();
				// if indented mode the alen is indenting of attributes otherwise, it is 0
				int alen = startLine != null ? tagName.length()+startLine.length()+(numItems==0?3:2) : 0;
				NamedNodeMap nm = node.getAttributes();
				int numAttrs;
				if (nm == null) {
					numAttrs = 0;
				} else if ((numAttrs = nm.getLength()) > 0) {
					for (int i = 0; i < numAttrs; i++) {
						Node n = nm.item(i);
						String name = n.getNodeName();
						if ("xmlns".equals(name) || name.startsWith("xmlns:")) {
							newPrefixMap.put(name, n.getNodeValue());
						}
					}
					out.write(' ');
					String s = createAttr(nm.item(0), removeIgnorableWhiteSpaces, newPrefixMap, unresolved);
					if (numAttrs > 1) {
						String aindent;
						if (startLine!=null) {
							aindent = startLine;
							for (int j = 0; j < tagName.length() + 2; j++) {
								aindent += ' '; // indent to first attibute
							}
						} else {
							aindent = " "; // attribute separator
						}
						int i = 1;
						String t = s;
						for (; i < numAttrs; i++) {
							t += ' ' + createAttr(nm.item(i),
								removeIgnorableWhiteSpaces, newPrefixMap, unresolved);
							if (alen>0 && alen + t.length() >= lineLen) {
								i = 1;
								t = s;
								break;
							}
						}
						out.write(t);
						for (; i < numAttrs; i++) {
							out.write(aindent);
							out.write(createAttr(nm.item(i),
								removeIgnorableWhiteSpaces, newPrefixMap, unresolved));
						}
					} else {
						out.write(s);
					}
				}
				int ndx = tagName.indexOf(':');
				String xmlnsAttrName =
					"xmlns" + (ndx >= 0 ? ':' + tagName.substring(0, ndx) : "");
				String uri = node.getNamespaceURI();
				if (uri == null) {
					uri = "";
				}
				if (!uri.equals(newPrefixMap.get(xmlnsAttrName))) {
					unresolved.put(xmlnsAttrName, uri);
					newPrefixMap.put(xmlnsAttrName, uri);
				}
				boolean first = true;
				for (Entry<String, String> e: unresolved.entrySet()) {
					String key = e.getKey();
					String value = e.getValue();
					newPrefixMap.put(key, value);
					String s = key + "=" + createAttrValue(value, false);
					if (first && numAttrs <= 1 && alen + s.length() < lineLen) {
						out.write(' ');
						first = false;
					} else if (startLine != null) {
						out.write(startLine + "  ");
					} else {
						out.write(" ");
					}
					out.write(s);
				}
				if (numItems == 0) {
					out.write("/>");
				} else {
					if (numItems == 1 && indent != null && nl.item(0).getNodeType() == Node.TEXT_NODE) {
						String s = nl.item(0).getNodeValue();
						s = removeIgnorableWhiteSpaces ? s.trim(): s;
						if (s.isEmpty()) {
							out.write("/>");
							return;
						} else if (s.length() + alen+tagName.length() + 2 < lineLen && s.indexOf('<') < 0
							&& s.indexOf('&') < 0 && s.indexOf('>') < 0
							&& s.indexOf('\n') < 0 && s.indexOf('\t') < 0) {
							//write text on the same line as element start
							out.write('>');
							writeText(out, s, false, null, removeIgnorableWhiteSpaces, lineLen);
							out.write("</");
							out.write(tagName);
							out.write('>');
							return;
						}
					}
					out.write('>');
					String newIndent = indent==null ? null : startLine + indent;
					for (int i = 0; i < numItems; i++) {
						Node item = nl.item(i);
						if (item.getNodeType() == Node.TEXT_NODE) {
							int len;
							String s = item.getNodeValue();
							if (s == null || (len=(s=removeIgnorableWhiteSpaces || indent!=null
												? s.trim() : s).length()) == 0) {
								continue;
							}
							if (numItems == 1 && indent != null && startLine != null
								&& (len+tagName.length() * 2 + startLine.length()) + 4 < lineLen
								&& s.indexOf('<') < 0 && s.indexOf('&') < 0) {
								if (removeIgnorableWhiteSpaces) {
									out.write(newIndent);
								}
								writeText(out, s, false, newIndent, removeIgnorableWhiteSpaces, lineLen);
								if (removeIgnorableWhiteSpaces) {
									out.write(startLine);
								}
								out.write("</");
								out.write(tagName);
								out.write('>');
								return;
							}
						}
						writeXml(out,
							null,
							item,
							newIndent,
							indent,
							canonical,
							removeIgnorableWhiteSpaces,
							comments,
							newPrefixMap,
							lineLen);
					}
					if (indent != null) {
						out.write(startLine);
					}
					out.write("</");
					out.write(tagName);
					out.write('>');
				}
				break;
			}
			case Node.ENTITY_REFERENCE_NODE: {
				if (!canonical) {
					out.write('&');
					out.write(node.getNodeName());
					out.write(';');
				} else {
					NodeList nl = node.getChildNodes();
					for (int i = 0, j = nl.getLength(); i < j; i++) {
						writeXml(out,
							null,
							nl.item(i),
							null,
							null,
							true,
							false,
							false,
							newPrefixMap,
							lineLen);
					}
				}
				break;
			}
			case Node.CDATA_SECTION_NODE:
			case Node.TEXT_NODE: {
				String s = node.getNodeValue();
				if (!canonical && type == Node.CDATA_SECTION_NODE) {
					//not canonical mode generates CDATA (otherwise text value!)
					if (removeIgnorableWhiteSpaces) {
						s = s.trim(); // we'll generate even an empty CDATA
					}
					int ndx;
					int oldx = 0;
					// this code is necessary because it is possible to set
					// the value of a CDATA node a string containing with "]]>"
					while ((ndx = s.indexOf("]]>", oldx)) >= 0) {
						out.write("<![CDATA[");
						writeText(out,
							s.substring(oldx, ndx + 2), true, startLine, removeIgnorableWhiteSpaces, lineLen);
						out.write("]]>");
						oldx = ndx + 2;
					}
					out.write("<![CDATA[");
					writeText(out, s.substring(oldx), true, startLine, removeIgnorableWhiteSpaces, lineLen);
					out.write("]]>");
					break;
				}
				if (s == null || s.isEmpty()) {
					break;
				}
				if (removeIgnorableWhiteSpaces && startLine != null) {
					if ((s = s.trim()).isEmpty()) {
						break;
					}
				}
				writeText(out, s, false, startLine, removeIgnorableWhiteSpaces, lineLen);
				break;
			}
			case Node.PROCESSING_INSTRUCTION_NODE:
				out.write("<?");
				out.write(node.getNodeName());
				String data = node.getNodeValue();
				if (data != null && data.length() > 0) {
					out.write(' ');
					out.write(data);
				}
				out.write("?>");
				break;
			default:
		}
	}

	/** Write node to output stream. Result will be indented if the argument indentStep is a string with
	 * indenting  spaces. If argument indentStep is null then output is not indented. If argument canonical
	 * is true the output is in canonical form (i.e. without entity references, CDATA sections are converted
	 * to text values). If argument comments is false all Comment nodes are ignored.
	 * @param out output writer used for result.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param encoding name of output code table.
	 * @param indentStep null or the indenting string.
	 * @param canonical if true the output is canonical form.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space.
	 * @param comments if true comments are generated.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeXml(final Writer out,
		final String encoding,
		final Node node,
		final String indentStep,
		final boolean canonical,
		final boolean removeIgnorableWhiteSpaces,
		final boolean comments) throws IOException {
		writeXml(out,
			encoding,
			node,
			null, //line indent
			indentStep,
			canonical,
			removeIgnorableWhiteSpaces,
			comments,
			ROOT_NSPREFIXMAP,
			LINELENGTH);
		out.flush();
	}

	/** Write node as XML to output stream. The character set encoding is set from the outputStreamWriter.
	 * @param out output writer used for result.
	 * @param node org.w3c.dom.Node to be converted.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeXml(final OutputStreamWriter out, final Node node) throws IOException {
		String encoding = KCharsetNames.getXmlEncodingName(out.getEncoding());
		if (encoding == null) {
			//Incorrect name of data encoding&{0}{: '}{'}
			throw new SIOException(XML.XML090, out.getEncoding());
		}
		writeXml(out, encoding, node, null, true, true, true);
		out.flush();
	}

	/** Write node in XML format.
	 * @param out output writer used for result.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indenting if true the output will be indented.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space.
	 * @param comments if true comment nodes are written to output.
	 * @throws IOException if on I/O error occurs.
	 */
	public static final void writeXml(final OutputStreamWriter out,
		final Node node,
		final boolean indenting,
		final boolean removeIgnorableWhiteSpaces,
		final boolean comments) throws IOException {
		String encoding = KCharsetNames.getXmlEncodingName(out.getEncoding());
		if (encoding == null) {
			//Incorrect name of data encoding&{0}{: '}{'}
			throw new SIOException(XML.XML090, out.getEncoding());
		}
		writeXml(out,
			encoding, //encoding
			node,
			(indenting ? "  " : null), //indentStep
			true,  //canonical
			removeIgnorableWhiteSpaces,
			comments);
		out.flush();
	}

	/** Write node in XML format.
	 * @param fname output file name.
	 * @param encoding character set name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indenting if true the output will be indented.
	 * @param comments if true comment nodes are written to output.
	 * @throws IOException if an I/O error occurs.
	 */
	public static final void writeXml(final String fname,
		final String encoding,
		final Node node,
		final boolean indenting,
		final boolean comments) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(fname);
			OutputStreamWriter out = new OutputStreamWriter(fos,encoding)) {
			writeXml(out,
				encoding,
				node,
				(indenting ? "  " : null), //indentStep
				true, //canonical
				indenting, //removeIgnorableWhiteSpaces
				comments);
		}
	}

	/** Write element in XML format in UTF-8.
	 * @param fname output file name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indenting if true the output will be indented.
	 * @param comments if true comment nodes are written to output.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final String fname,
		final Node node,
		final boolean indenting,
		final boolean comments) throws IOException {
		writeXml(fname, "UTF-8", node, indenting, comments);
	}

	/** Write element in XML format in given character set.
	 * @param fname output file name.
	 * @param encoding character set name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final String fname, final String encoding, final Node node)
		throws IOException {
		writeXml(fname, encoding, node, false, true);
	}

	/** Write element in XML format in UTF-8 character set.
	 * @param fname output file name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final String fname, final Node node) throws IOException {
		writeXml(fname, "UTF-8", node, false, true);
	}

	/** Write node in XML format.
	 * @param file output file.
	 * @param encoding character set name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indenting if true the output will be indented.
	 * @param comments if true comment nodes are written to output.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final File file,
		final String encoding,
		final Node node,
		final boolean indenting,
		final boolean comments) throws IOException {
		writeXml(file, encoding, node, indenting, comments, LINELENGTH);
	}

	/** Write node in XML format.
	 * @param file output file.
	 * @param encoding character set name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indenting if true the output will be indented.
	 * @param comments if true comment nodes are written to output.
	 * @param lineLen length of source line.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final File file,
		final String encoding,
		final Node node,
		final boolean indenting,
		final boolean comments,
		final int lineLen) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter out = new OutputStreamWriter(fos, encoding)) {
			writeXml(out,
				encoding,
				node,
				null, //line indent
				(indenting ? "  " : null),//indentStep
				true, //canonical
				indenting, //removeIgnorableWhiteSpaces
				comments,
				ROOT_NSPREFIXMAP,
				lineLen);
			out.flush();
		}
	}

	/** Write element in XML format in UTF-8 character set.
	 * @param file output file.
	 * @param encoding character set name.
	 * @param node org.w3c.dom.Node to be converted.
	 * @throws IOException if an I/O error occurs.
	 */
	public static final void writeXml(final File file, final String encoding, final Node node)
		throws IOException {
		writeXml(file, encoding, node, false, true);
	}

	/** Write element in XML format and UTF-8 character set.
	 * @param file The output file.
	 * @param node The org.w3c.dom.Node to be converted.
	 * @throws IOException if an I/O error occurs
	 */
	public static final void writeXml(final File file, final Node node) throws IOException {
		writeXml(file, "UTF-8", node, false, true);
	}

	/** Create string in XML format from given argument.
	 * Output format may be either not indented (and without inserted new lines) or in the intended form.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indent If this parameter is set to true the output string is in indented format, otherwise
	 * no indentation is generated.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space.
	 * @param comments if true comment nodes are written to output.
	 * @return string with the XML representation of the parameter element.
	 */
	public static final String nodeToString(final Node node,
		final boolean comments,
		final boolean removeIgnorableWhiteSpaces,
		final boolean indent) {
		return nodeToString(node, comments, removeIgnorableWhiteSpaces, indent, LINELENGTH);
	}

	/** Create string in XML format from given argument. Output format may be either not indented (and without
	 * inserted new lines) or in the intended form.
	 * @param node org.w3c.dom.Node to be converted.
	 * @param indent If this parameter is set to true the output string is
	 * in indented format, otherwise in no indentation is generated.
	 * @param removeIgnorableWhiteSpaces if true all white space sequences are replaced by one space.
	 * @param comments if true comment nodes are written to output.
	 * @param lineLen length of source line.
	 * @return string with the XML representation of the parameter element.
	 */
	public static final String nodeToString(final Node node,
		final boolean comments,
		final boolean removeIgnorableWhiteSpaces,
		final boolean indent,
		final int lineLen) {
		try {
			StringWriter wr = new StringWriter();
			writeXml(wr,
				null, // encoding
				node,
				null, //indentPrefix
				(indent ? "  " : null),
				true, //canonical
				removeIgnorableWhiteSpaces,
				comments,
				ROOT_NSPREFIXMAP,
				lineLen);
			return wr.toString();
		} catch (IOException ex) {return null;} //never happens
	}

	/** Create string in XML format from given argument. The output format may be either not indented (without
	 * inserted new lines) or in the intended form.
	 * @param node org.w3c.dom.Node to be converted to the string.
	 * @param indent If this parameter is set to true the output string is in indented format, otherwise
	 * no indentation is generated.
	 * @return string with the XML representation of the parameter element.
	 */
	public static final String nodeToString(final Node node, final boolean indent) {
		try {
			StringWriter caw = new StringWriter();
			writeXml(caw,
				null, //encoding
				node,
				null, //linePrefix
				(indent ? "  " : null), //indentStep
				true, //canonical
				indent, //removeIgnorableWhiteSpaces
				true, //comments
				ROOT_NSPREFIXMAP,
				LINELENGTH); // map of prefixes
			return caw.toString();
		} catch (IOException ex) {return null;} //never happens
	}

	/** Create string in XML format from given node.
	 * The output format is not indented.
	 * @param node node to be converted to the string.
	 * @return string with the XML representation of the parameter element.
	 */
	public static final String nodeToString(final Node node) {
		try {
			StringWriter caw = new StringWriter();
			writeXml(caw,
				null, //encoding
				node,
				null, //linePrefix
				null, //indentStep
				true, //canonical
				false, //removeIgnorableWhiteSpaces
				true, //comments
				ROOT_NSPREFIXMAP, // root prefix map
				LINELENGTH); // source line length
			return caw.toString();
		} catch (IOException ex) {return null;} //never happens
	}

	/** Parse source file or a string with XMLke format and create org.w3c.dom.Document
	 * (i.e. it starts with '&lt;').
	 * @param source can be the path to a file or a string in XML format.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final String source) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		return b.parse(source);
	}

	/** Parse source file or a string with XMLke format and create org.w3c.dom.Document
	 * (i.e. it starts with '&lt;').
	 * @param source can be the path to a file or a string in XML format.
	 * @param comments if true comment nodes are written to result document.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final String source,final boolean comments) {
		KDOMBuilder b = new KDOMBuilder();
		b.setIgnoringComments(comments);
		b.setNamespaceAware(true);
		return b.parse(source);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in file with the source XML.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final java.io.File in) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in The file with the source XML.
	 * @param comments if true comment nodes are written to result document.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final java.io.File in, final boolean comments) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		b.setIgnoringComments(comments);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in URL pointing to the source XML data.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final URL in) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in URL pointing to the source XML.
	 * @param comments if true comment nodes are written to result.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final URL in, final boolean comments) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		b.setIgnoringComments(comments);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in input stream with the source XML.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final java.io.InputStream in) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in input stream with the source XML.
	 * @param comments if true comment nodes are written to result.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final java.io.InputStream in, final boolean comments) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		b.setIgnoringComments(comments);
		return b.parse(in);
	}

	/** Parse source file with XML and return org.w3c.dom.Document.
	 * @param in input stream with the source XML.
	 * @param comments if true comment nodes are written to result.
	 * @param closeStream if true the input stream is closed after parsing.
	 * @return parsed document.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static final Document parseXml(final java.io.InputStream in,
		final boolean comments,
		final boolean closeStream) {
		KDOMBuilder b = new KDOMBuilder();
		b.setNamespaceAware(true);
		b.setIgnoringComments(comments);
		return b.parse(in, closeStream);
	}

	/** Append an element from the argument what at the child list of the element from argument where.
	 * If the owner of both elements are different create new copy of the element what in the document owner
	 * of the element where.
	 * @param what element to be appended.
	 * @param where element where the element will be appended.
	 */
	public static final void appendChild(final Element what, final Element where) {
		Document doc = where.getOwnerDocument();
		where.appendChild(what.getOwnerDocument() == doc ? what : doc.importNode(what, true));
	}

	/** Write message about exception in parsing of source XML.
	 * @param reporter report writer.
	 * @param ex exception.
	 * @param p letter 'A' or 'B'.
	 */
	private static void reportException(final ReportWriter reporter, final Exception ex, final char p) {
		if (ex instanceof SRuntimeException) {
			reporter.putReport(((SRuntimeException)ex).getReport());
		} else if (ex instanceof SException) {
			reporter.putReport(((SException)ex).getReport());
		} else {
			reporter.error(SYS.SYS066, ex); //Internal error: &{0}
		}
		reporter.error(XML.XML075, p); //Can't parse &{0}
	}

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * @param xml_A file with the first document.
	 * @param xml_B file with the second document.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final File xml_A, final File xml_B) {
		return compareElements(xml_A, xml_B, null);
	}

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * @param xml_A file with the first document.
	 * @param xml_B file with the second document.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static ReportWriter compareElements(final File xml_A,final File xml_B,final ReportWriter reporter){
		return compareElements(xml_A, xml_B, false, reporter);
	}

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * @param xml_A file with the first document.
	 * @param xml_B file with the second document.
	 * @param trimText if true then text values are trimmed before comparing
	 * and empty text nodes are removed.
	 * @return report writer with results of comparing.
	 */
	public static ReportWriter compareElements(final File xml_A, final File xml_B, final boolean trimText) {
		return compareElements(xml_A, xml_B, trimText, null);
	}

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * @param xml_A file with the first document.
	 * @param xml_B file with the second document.
	 * @param trimText if true then text values are trimmed before comparing
	 * and empty text nodes are removed.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static ReportWriter compareElements(final File xml_A,
		final File xml_B,
		final boolean trimText,
		final ReportWriter reporter) {
		ReportWriter r = reporter == null ? new ArrayReporter() : reporter;
		Element elem_A = null;
		Element elem_B = null;
		try {
			elem_A = parseXml(xml_A, false).getDocumentElement();
		} catch (Exception ex) {
			reportException(r, ex, 'A');
		}
		try {
			elem_B = parseXml(xml_B, false).getDocumentElement();
		} catch (Exception ex) {
			reportException(r, ex, 'B');
		}
		if (r.errors()) {
			return r;
		}
		return compareElements(elem_A, elem_B, trimText, r);
	}

	/** Compare XML documents. The result is reporter which contains error
	 * messages with differences.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String xml_A, final String xml_B) {
		return compareElements(xml_A, xml_B, false, null);
	}

	/** Compare XML documents. The result is reporter which contains error
	 * messages with differences.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @param trimText if true then all text values are rimmed before comparing.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String xml_A,
		final String xml_B,
		final boolean trimText) {
		return compareElements(xml_A, xml_B, trimText, null);
	}

	/** Compare XML documents. The result is reporter which contains error
	 * messages with differences.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @param trimText if true then text values are trimmed before comparing
	 * and empty text nodes are removed.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String xml_A,
		final String xml_B,
		final boolean trimText,
		final ReportWriter reporter) {
		ReportWriter r = reporter == null ? new ArrayReporter() : reporter;
		Element elem_A = null;
		Element elem_B = null;
		try {
			elem_A = parseXml(xml_A).getDocumentElement();
		} catch (Exception ex) {
			reportException(r, ex, 'A');
		}
		try {
			elem_B = parseXml(xml_B).getDocumentElement();
		} catch (Exception ex) {
			reportException(r, ex, 'B');
		}
		if (r.errors()) {
			return r;
		}
		return compareElements(elem_A, elem_B, trimText, r);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A, final Element elem_B) {
		return compareElements(elem_A, elem_B, null);
	}

	/** Compare XML elements. The result is reporter which contains error
	 * messages with differences.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param trimText if true then text values are trimmed before comparing
	 * and empty text nodes are removed.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final Element elem_B,
		final boolean trimText) {
		return compareElements(elem_A, elem_B, trimText, null);
	}

	/** Compare XML elements. The result is reporter which contains error
	 * messages with differences.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param trimText if true then text values are trimmed before comparing
	 * and empty text nodes are removed.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final Element elem_B,
		final boolean trimText,
		ReportWriter reporter) {
		Element a,b;
		if (elem_A == null) {
			ReportWriter r = reporter;
			if (elem_B != null) {
				if (r == null) {
					r = new ArrayReporter();
				}
				r.error(null, "A is null");
			}
			return r;
		}
		if (elem_B == null) {
			ReportWriter r = reporter == null ? new ArrayReporter() : reporter;
			r.error(null, "B is null");
			return r;
		}
		if (trimText) {
			a = (Element) elem_A.cloneNode(true);
			b = (Element) elem_B.cloneNode(true);
			KXmlUtils.trimTextNodes(a, true);
			KXmlUtils.trimTextNodes(b, true);
		} else {
			a = elem_A;
			b = elem_B;
		}
		return compareElements(a, b, reporter);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static ReportWriter compareElements(
		final Element elem_A,
		final Element elem_B,
		final ReportWriter reporter) {
		ReportWriter r = reporter == null ? new ArrayReporter() : reporter;
		if (elem_A == null) {
			r.error(null, "A is null");
		}
		if (elem_B == null) {
			r.error(null, "B is null");
		}
		if (r.errors()) {
			return r;
		}
		if (cmpElements(elem_A, elem_B, r)) {
			r.text(null, "OK");
		}
		return r;
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param A source string with the first element.
	 * @param elem_B the second element.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String A, final Element elem_B) {
		return compareElements(parseXml(A).getDocumentElement(), elem_B, false, null);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param A source string with the first element.
	 * @param elem_B the second element.
	 * @param trimText if true then text values are trimmed before comparing.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String A,
		final Element elem_B,
		final boolean trimText) {
		return compareElements(parseXml(A).getDocumentElement(), elem_B, trimText, null);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param A source string with the first element.
	 * @param elem_B the second element.
	 * @param trimText if true then text values are trimmed before comparing.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String A,
		final Element elem_B,
		final boolean trimText,
		ReportWriter reporter) {
		return compareElements(parseXml(A).getDocumentElement(), elem_B, trimText, reporter);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param A source string with the first element.
	 * @param elem_B the second element.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final String A,
		final Element elem_B,
		ReportWriter reporter) {
		return compareElements(parseXml(A).getDocumentElement(), elem_B, reporter);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param elem_A the first element.
	 * @param B source string with the second element.
	 * @param trimText if true then text values are trimmed before comparing.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final String B,
		final boolean trimText,
		ReportWriter reporter) {
		return compareElements(elem_A, parseXml(B).getDocumentElement(), trimText, reporter);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param elem_A the first element.
	 * @param B source string with the second element.
	 * @param trimText if true then text values are trimmed before comparing.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final String B,
		final boolean trimText) {
		return compareElements(elem_A, parseXml(B).getDocumentElement(), trimText, null);
	}

	/** Compare XML elements. The result is reporter which contains error messages with differences.
	 * @param elem_A the first element.
	 * @param B source string with the second element.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final String B,
		ReportWriter reporter) {
		return compareElements(elem_A, parseXml(B).getDocumentElement(),reporter);
	}

	/** Compare XML elements. The result with reporter contains error messages with differences.
	 * @param elem_A the first element.
	 * @param B source string with the second element.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareElements(final Element elem_A,
		final String B) {
		return compareElements(elem_A, parseXml(B).getDocumentElement());
	}

	/** Compare two elements. Write error messages with differences to the
	 * reporter. Return true if and only if no differences were found.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param reporter report writer.
	 * @return true if elements are equal, otherwise false.
	 */
	public static boolean cmpElements(final Element elem_A, final Element elem_B,final ReportWriter reporter){
		try {
			if (!elem_A.getNodeName().equals(elem_B.getNodeName())) {
				reporter.error(null, "A: " + elem_A.getNodeName() + ", B: " + elem_B.getNodeName());
				return false;
			} else {
				String ua = elem_A.getNamespaceURI();
				String ub = elem_B.getNamespaceURI();
				if (ua != null && !ua.equals(ub) || ua == null && ub != null) {
					reporter.error("", "<" + elem_A.getNodeName() + "> A: ns=" + ua + ", B: ns=" + ub);
					return false;
				}
			}
			return cmpAttrLists(elem_A, elem_B, reporter) & cmpChildNodes(elem_A, elem_B, reporter);
		} catch (Exception ex) {
			throw new SRuntimeException(ex);
		}
	}

	/** Compare list of attributes of two elements. Write differences to the
	 * reporter. Return true if and only if no differences were found.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param reporter The report writer.
	 * @return true if lists are equal, otherwise false.
	 */
	private static boolean cmpAttrLists(final Element elem_A,
		final Element elem_B,
		final ReportWriter reporter) {
		NamedNodeMap nlA = elem_A.getAttributes();
		NamedNodeMap nlB = elem_B.getAttributes();
		int lenA = nlA.getLength();
		int lenB = nlB.getLength();
		for (int i = lenB - 1; i >= 0; i--) {//ignore xmlns attributes
			if (nlB.item(i).getNodeName().startsWith("xmlns")) {
				lenB--;
			}
		}
		for (int i = lenA - 1; i >= 0; i--) {
			Attr a = (Attr) nlA.item(i);
			String name = a.getNodeName();
			if (name.startsWith("xmlns")) {//ignore xmlns attributes
				lenA--;
				continue;
			}
			Attr b = elem_B.getAttributeNode(name);
			if (b == null) {
				reporter.error(null, "<" + elem_A.getNodeName() + "> B: attr missing: " + name);
				return false;
			}
			if (!(elem_A.getAttribute(name)).equals(
				elem_B.getAttribute(name))) {
				reporter.error(null, "<" + elem_A.getNodeName()
					+ "> attr " + name + " A:'" + elem_A.getAttribute(name)
					+ "', B:'" + elem_B.getAttribute(name)+ "'");
				return false;
			} else {
				String ua = a.getNamespaceURI();
				String ub = b.getNamespaceURI();
				if (ua != null && !ua.equals(ub) || ua == null && ub != null) {
					reporter.error(null,
						"<" + elem_A.getNodeName() +"> attr "+ name + ", ns A:='" + ua + "' B:'" + ub + "'");
					return false;
				}
			}
		}
		if (lenA != lenB) {
			for (int i = lenB - 1; i >= 0; i--) {
				Attr b = (Attr) nlB.item(i);
				String name = b.getNodeName();
				if (name.startsWith("xmlns")) {//ignore xmlns attributes
					continue;
				}
				Attr a = elem_A.getAttributeNode(name);
				if (a == null) {
					reporter.error(null,
						"<"+ elem_A.getNodeName() +"> A: attr missing: "+ name +"=\""+ b.getNodeValue() +'"');
					return false;
				}
			}
			reporter.error(null,
				"<" + elem_A.getNodeName() + "> A: attr len = " + lenA + ", B: attr len = " + lenB);
			return false;
		}
		return true;
	}

	/** Compare list of child nodes of two elements. Write differences to the
	 * reporter. Return true if and only if no differences were found.
	 * @param elem_A the first element.
	 * @param elem_B the second element.
	 * @param reporter report writer.
	 * @return true if lists are equal, otherwise false.
	 */
	private static boolean cmpChildNodes(final Element elem_A,
		final Element elem_B,
		final ReportWriter reporter) {
		NodeList nlA = elem_A.getChildNodes();
		NodeList nlB = elem_B.getChildNodes();
		if (nlA.getLength() != nlB.getLength()) {
			reporter.error(null,
				"<" + elem_A.getNodeName() + "> A: nodes len = " + nlA.getLength()
					+ ", B: nodes len = " + nlB.getLength());
			return false;
		}
		for (int i = 0; i < nlA.getLength(); i++) {
			Node nodeA = nlA.item(i);
			Node nodeB = nlB.item(i);
			if (nodeA.getNodeType() != nodeB.getNodeType()) {
				reporter.error(null,
					elem_A.getNodeName() + " A node type: " + nodeA.getNodeName()
						+ ", /B: " + nodeB.getNodeName());
				return false;
			}
			switch (nodeA.getNodeType()) {
			case Node.ELEMENT_NODE:
				if (!cmpElements((Element)nodeA, (Element)nodeB, reporter)) {
					return false;
				}
				continue;
			case Node.TEXT_NODE:
				String sa = nodeA.getNodeValue();
				String sb = nodeB.getNodeValue();
				if (sa == null || sb == null) {
					if (sa == null && sb != null && sb.length() > 0 || sb == null && sa != null
						&& sa.length() > 0) {
						return false;
					}
				} else if (!(sa.trim()).equals(sb.trim())) {
					reporter.error(null, "<" + elem_A.getNodeName() +"> A text: '"+ sa +"', B: '"+ sb + "'");
					return false;
				}
			}
		}
		return true;
	}

////////////////////////////////////////////////////////////////////////////////
// DEPRECATED METHODS
////////////////////////////////////////////////////////////////////////////////

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * This method is deprecated. Use the method compareElements(...) instead.
	 * @param xml_A file with the first document.
	 * @param xml_B file with the second document.
	 * @return report writer with results of comparing.
	 */
	@Deprecated
	public static final ReportWriter compareXML(final File xml_A, final File xml_B) {
		return compareElements(xml_A, xml_B);
	}

	/** Compare XML documents. The result is reporter which contains error
	 * messages with differences.
	 * This method is deprecated. Use the method compareElements(...) instead.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @return report writer with results of comparing.
	 */
	@Deprecated
	public static final ReportWriter compareXML(final String xml_A, final String xml_B) {
		return compareElements(xml_A, xml_B);
	}

	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * This method is deprecated. Use the method compareElements(...) instead.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @param trimText if true then all text values are rimmed before comparing.
	 * @return report writer with results of comparing.
	 */
	@Deprecated
	public static final ReportWriter compareXML(final String xml_A,
		final String xml_B,
		final boolean trimText) {
		return compareElements(xml_A, xml_B, trimText);
	}

	@Deprecated
	/** Compare XML documents. The result is reporter which contains error messages with differences.
	 * This method is deprecated. Use the method compareElements(...) instead.
	 * @param xml_A the first document (or element).
	 * @param xml_B the second document (or element).
	 * @param trimText if true then text values are trimmed before comparing and empty text nodes are removed.
	 * @param reporter report writer or null and ArrayReporter is created.
	 * @return report writer with results of comparing.
	 */
	public static final ReportWriter compareXML(final String xml_A,
		final String xml_B,
		final boolean trimText,
		final ReportWriter reporter) {
		return compareElements(xml_A, xml_B, trimText, reporter);
	}
}
