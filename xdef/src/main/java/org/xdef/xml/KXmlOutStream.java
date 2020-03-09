package org.xdef.xml;

import org.xdef.impl.xml.KNamespace;
import org.xdef.msg.SYS;
import org.xdef.msg.XML;
import org.xdef.sys.SIOException;
import org.xdef.sys.SRuntimeException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides incremental writer of XML to data stream.
 * @author Vaclav Trojan
 */
public class KXmlOutStream {

	private final File _file;
	private final String _encoding;
	private final boolean _writeDocumentHeader;
	private Writer _writer;
	private boolean _hdrWritten = false;
	private boolean _rootWritten = false;
	private String _indent = null;
	private final KNamespace _ns = new KNamespace();
	private final Stack<String> _names = new Stack<String>();

	/** Creates new instance of DefXmlOutStream with java.io.Writer
	 * @param writer where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 */
	public KXmlOutStream(final Writer writer,
		final String encoding,
		final boolean writeDocumentHeader) {
		_file = null;
		_writer = writer;
		_encoding = encoding == null ? "UTF-8" : encoding;
		_writeDocumentHeader = writeDocumentHeader;
	}

	/** Creates new instance of DefXmlOutStream with java.io.OutputStream.
	 * @param out where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @throws IOException if an error occurs.
	 */
	public KXmlOutStream(final OutputStream out,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		_file = null;
		_encoding = encoding == null ? "UTF-8" : encoding;
		_writer = new OutputStreamWriter(
			out, KCharsetNames.getJavaCharsetName(_encoding));
		_writeDocumentHeader = writeDocumentHeader;
	}

	/** Creates new instance of DefXmlOutStream with the name of output file.
	 * If the file already exists it is deleted. The file will be created
	 * only if something was written.
	 * @param filename the name of file where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @throws IOException if an error occurs.
	 */
	public KXmlOutStream(final String filename,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		_file = new File(filename).getCanonicalFile();
		if (_file.exists()) {
			if (!_file.canWrite()) {
				//Can't write to output stream&{0}{; }
				throw new SIOException(SYS.SYS027, filename);
			}
			if (!_file.delete()) {
				//Can't write to output stream&{0}{; }
				throw new SIOException(SYS.SYS027, filename);
			}
		}
		if (!_file.createNewFile()) {
			//Can't write to output stream&{0}{; }
			throw new SIOException(SYS.SYS027, filename);
		}
		if (!_file.delete()) {
			//Can't write to output stream&{0}{; }
			throw new SIOException(SYS.SYS027, filename);
		}
		_encoding = encoding == null ? "UTF-8" : encoding;
		_writeDocumentHeader = writeDocumentHeader;
	}

	/** Set output will be indented.
	 * @param indent if <tt>true</tt> then the output will be indented.
	 */
	public void setIndenting(boolean indent) {
		_indent = indent ? "\n" : null;
	}

	/** Create stack of XML namespace information and add necessary
	 * xmlns attributes.
	 * @param elem element where xmlns attributes will be written.
	 * @param node the node from which namespace context is updated (may be an
	 * element or an attribute).
	 */
	private boolean procNS(final Node node, final String indent)
		throws IOException{
		String name = node.getNodeName();
		if (!name.toLowerCase().startsWith("xml")) {
			String uri = node.getNamespaceURI();
			if (uri != null) {
				String prefix = node.getPrefix();
				if (prefix == null) {
					prefix = "";
				}
				if (!uri.equals(_ns.getNamespaceURI(prefix))) {
					String s = prefix.length() > 0 ? "xmlns:"+prefix : "xmlns";
					String uri1 = _ns.getNamespaceURI(prefix);
					if (!uri.equals(uri1)) {
						_ns.setPrefix(prefix, uri);
						wrAttr(indent, s, uri);
						return true;
					}
				}
			}
		}
		return false;
	}

	private void initWriter() throws IOException {
		if (_writer == null) {
			String e = _encoding == null ? "UTF-8" : _encoding;
			_writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(_file),
				KCharsetNames.getJavaCharsetName(e)));
		}
	}

	/** Write XML document header.
	 * @param node The node of which the document header will be written.
	 */
	private void writeXmlHdr(final Node node) throws IOException {
		initWriter();
		if (_hdrWritten || !_writeDocumentHeader) {
			return;
		}
		_hdrWritten = true;
		String savedIndent = _indent;
		_indent = "";
		boolean somethingWritten;
		Document doc = node.getNodeType() == Node.DOCUMENT_NODE
			? (Document) node : node.getOwnerDocument();
//		if ((!"UTF-8".equalsIgnoreCase(_encoding)) ||
//			!"1.0".equals(doc.getXmlVersion())) {
			_writer.write("<?xml version=\"");
			_writer.write(doc.getXmlVersion());
			_writer.write("\" encoding=\"");
			_writer.write(_encoding==null ? "UTF-8" : _encoding);
			_writer.write("\" ?>");
			somethingWritten = true;
			_indent = "\n";
//		} else {
//			somethingWritten = false;
//		}
		//write all child nodes before root element
		Node item = doc.getFirstChild();
		boolean nodeWritten = false;
		while (item != null && item.getNodeType() != Node.ELEMENT_NODE) {
			writeNode(item);
			_indent = "\n";
			somethingWritten = true;
			nodeWritten = true;
			item = item.getNextSibling();
		}
		if ((_indent = savedIndent) == null) {
			if (somethingWritten) {
				_writer.write('\n');
			}
		} else if (somethingWritten && !nodeWritten) {
			_writer.write('\n');
		}
		_writer.flush();
	}

	private void wrAttr(final String indent,
		final String name,
		final String val) throws IOException {
		_writer.write(indent);
		_writer.write(name);
		_writer.write("=\"");
		int len = val != null ? val.length() : 0;
		for (int j = 0; j < len; j++) {
			char c;
			switch (c = val.charAt(j)) {
				case '<':
					_writer.write("&lt;");
					break;
				case '&':
					_writer.write("&amp;");
					break;
				case '"':
					_writer.write("&quot;");
					break;
				case ' ':
					_writer.write(c); //default print char
					break;
				default:
					if (c < ' ') {
						_writer.write("&#");
						_writer.write(Integer.toString(c));
						_writer.write(';');
					} else {
						_writer.write(c);
					}
			}
		}
		_writer.write('"');

	}

	private void wrElemStart(final Element elem) throws IOException {
		try {
			String indent = _rootWritten && _indent != null ? _indent : null;
			String name = elem.getNodeName();
			_names.push(name);
			writeXmlHdr(elem);
			_ns.pushContext();
			if (indent != null) {
				_writer.write(indent);
			}
			_writer.write('<');
			_writer.write(elem.getNodeName());
			if (_indent != null) {
				_indent += "  ";
			}
			indent = " ";
			if (procNS(elem, indent) && _indent != null) {
				indent = _indent;
			}
			NamedNodeMap nm = elem.getAttributes();
			if (nm != null) {
				for (int i = 0; i < nm.getLength(); i++) {
					if (procNS(nm.item(i), indent) && _indent != null) {
						indent = _indent;
					}
				}
			}
			nm = elem.getAttributes();
			if (nm != null) {
				for (int i = 0; i < nm.getLength(); i++) {
					Node attr = nm.item(i);
					String atrname = attr.getNodeName();
					if (!atrname.toLowerCase().startsWith("xmlns")) {
						wrAttr(indent, atrname, attr.getNodeValue());
						if (_indent != null) {
							indent = _indent;
						}
					}
				}
			}
			_rootWritten = true;
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
	}

	/** Write start of XML (element name and attributes).
	 * @param elem element of which the start will be written.
	 */
	public void writeElementStart(final Element elem) {
		try {
			wrElemStart(elem);
			_writer.write('>');
			_writer.flush();
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
	}

	/** Write XML end tag.
	 * @param elem element of which the tag will be written.
	 */
	private void writeElementEnd(final String name) {
		try {
			_ns.popContext();
			if (_indent != null) {
				_indent = _indent.substring(0, _indent.length() - 2);
				_writer.write(_indent);
			}
			_writer.write("</");
			_writer.write(name);
			_writer.write('>');
			_writer.flush();
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
	}

	public void writeElementEnd() {
		writeElementEnd(_names.pop());
	}

	/** Write XML end tag.
	 * @param elem element of which the tag will be written.
	 */
	public final void writeElementEnd(final Element elem) {
		String name = _names.pop();
		if (!elem.getNodeName().equals(name)) {
			//Start and end of element differs
			throw new SRuntimeException(XML.XML602);
		}
		writeElementEnd(name);
	}

	/** Write XML node.
	 * @param nlist The node list to be written.
	 */
	public final void writeNodeList(final NodeList nlist) {
		for (int i = 0; i < nlist.getLength(); i++) {
			writeNode(nlist.item(i));
		}
	}

	/** Write string as text node.
	 * @param text The text to be written.
	 */
	public final void writeText(final String text) {
		try {
			String s;
			if (_indent != null) {
				 s = text == null ?	text : (text.trim());
			} else {
				s = text;
			}
			int len;
			if (s == null || (len = s.length()) == 0) {
				return;
			}
			if (_indent != null) {
				_writer.write(_indent);
			}
			if (!_rootWritten && _writeDocumentHeader) {
				//Can not write text node before root element
				throw new SRuntimeException(XML.XML601);
			}
			for (int j = 0; j < len; j++) {
				char c;
				switch (c = s.charAt(j)) {
					case '<':
						_writer.write("&lt;");
						continue;
					case '>':
						if (j >= 2 && s.charAt(j - 1) == ']' &&
							s.charAt(j - 2) == ']') {
							_writer.write("&gt;");
						} else {
							_writer.write('>');
						}
						continue;
					case '&':
						_writer.write("&amp;");
						continue;
					case '\r': {
						if (j < len && s.charAt(j + 1) == '\n') {
							j++;
							_writer.write('\n');
							continue;
						}
					}
					case '\t':
					case '\n':
					case ' ':
						_writer.write(c); // default print char
						continue;
					default:
						if (c < ' ') {
							_writer.write("&#");
							_writer.write(Integer.toString(c));
							_writer.write(';');
						} else {
							_writer.write(c);
						}
//						continue;
				}
			}
			_writer.flush();
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
	}

	/** Write XML node.
	 * @param node The node to be written.
	 */
	public final void writeNode(final Node node) {
		try {
			switch (node.getNodeType()) {
				case Node.DOCUMENT_NODE: {
					Document doc = (Document) node;
					writeNode(doc.getDocumentElement());
					writeXmlTail(doc);
					return;
				}
				case Node.ELEMENT_NODE: {
					Element elem = (Element) node;
					try {
						wrElemStart(elem);
						NodeList nl = elem.getChildNodes();
						if (nl.getLength() == 0 ) {
							_writer.write("/>");
							_writer.flush();
							_ns.popContext();
							_names.pop();
							if (_indent != null) {
								_indent =
									_indent.substring(0, _indent.length() - 2);
							}
						} else {
							_writer.write('>');
							writeNodeList(nl);
							writeElementEnd(elem);
						}
					} catch (IOException ex) {
						//Program exception&{0}{: }
						throw new SRuntimeException(SYS.SYS036, ex);
					}
					return;
				}
				case Node.COMMENT_NODE:
					initWriter();
					if (_indent != null) {
						_writer.write(_indent);
					}
					_writer.write("<!--");
					_writer.write(node.getNodeValue());
					_writer.write("-->");
					_writer.flush();
					return;
				case Node.CDATA_SECTION_NODE:
					initWriter();
					_writer.write("<![CDATA[");
					_writer.write(node.getNodeValue());
					_writer.write("]]>");
					_writer.flush();
					return;
				case Node.TEXT_NODE: {
					writeText(node.getNodeValue());
					return;
				}
				case Node.PROCESSING_INSTRUCTION_NODE:
					initWriter();
					if (_indent != null) {
						_writer.write(_indent);
					}
					_writer.write("<?");
					_writer.write(node.getNodeName());
					_writer.write(' ');
					_writer.write(node.getNodeValue());
					_writer.write("?>");
					_writer.flush();
					return;
				default:
					//Can't write this node
					throw new SRuntimeException(SYS.SYS093);
			}
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
	}

	private void endPendingElements() {

		while (!_names.empty()) {
			String name = _names.pop();
			writeElementEnd(name);
		}
	}

	/** Flush stream writer.*/
	public final void flushStream() {
		if (_writer != null) {
			if (_file != null) {
				closeStream();
			} else {
				endPendingElements();
				try {
					_writer.flush();
				} catch (IOException ex) {
					//Program exception&{0}{: }
					throw new SRuntimeException(SYS.SYS036, ex);
				}
			}
		}
	}

	/** Close stream writer.*/
	public final void closeStream() {
		if (_writer != null) {
			endPendingElements();
			try {
				_writer.close();
				_writer = null;
			} catch (IOException ex) {
				//Program exception&{0}{: }
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		}
		_names.clear();
		_ns.clearContext();
		_indent = null;
	}

	/** Write nodes after document element (comments and/or PI's).
	 * @param doc The document node.
	 */
	public final void writeXmlTail(final Document doc) {
		endPendingElements();
		if (_hdrWritten && _writeDocumentHeader) {
			Node item = doc.getDocumentElement();
			if (item != null) {
				String indent = _indent;
				_indent = "\n";
				while ((item = item.getNextSibling()) != null) {
					writeNode(item);
				}
				_indent = indent;
			}
		}
		flushStream();
	}

}