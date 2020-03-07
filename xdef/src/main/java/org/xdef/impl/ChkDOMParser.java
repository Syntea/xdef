package org.xdef.impl;

import org.xdef.impl.code.DefOutStream;
import org.xdef.msg.XDEF;
import org.xdef.msg.XML;
import org.xdef.sys.FileReportWriter;
import org.xdef.sys.Report;
import org.xdef.sys.SException;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDConstants;
import org.xdef.XDInput;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.ReportWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Provides processing of an XML document in the form of org.w3c.dom object.
 * @author  Vaclav Trojan
 */
class ChkDOMParser extends SReporter {

	/** The root ChkDocument. */
	private ChkDocument _chkDoc;
	/** Element to be processed. */
	private Element _elem;
	private boolean _locationDetails;

	/** This prevents the user to create instance of ChkDOMParser. */
	private ChkDOMParser() {}

	/** Create instance of ChkDOMParser with source element.
	 * @param elem source element.
	 * @param reporter report writer.
	 */
	ChkDOMParser(ReportWriter reporter, Element elem) {
		super(reporter);
		_elem = elem;
	}

	/* The class DOMValidate separates user from the public methods of
	 * ReporterInterface.
	 */
	private final class DOMValidate extends ChkDOMParser {
		/** The cumulated text. */
		private StringBuilder _text;
		/** Root document. */
		private  Document _doc;

		/** This method adds cumulated text nodes to the result. */
		private void addText(final ChkElement actNode) {
			if (_text.length() > 0) {
				actNode.addText(_text.toString());
				_text.setLength(0);
			}
		}

		/** This method process element and all child nodes.
		 * @param parentNode The parent node.
		 * @param sourceElem The element with source data.
		 */
		private void processElement(final ChkElement parentNode,
			final Element sourceElem) {
			String ns = sourceElem.getNamespaceURI();
			NamedNodeMap atrs = sourceElem.getAttributes();
			int maxAttr = atrs.getLength();
			String elementName = sourceElem.getNodeName();
			String xdefInstancePrefix = "";
			String xdefInstanceNSAttr = "";
			ChkElement chkEl;
			if (parentNode == null) {
				for (int i=0; i < maxAttr; i++) {
					String name = atrs.item(i).getNodeName();
					String val = atrs.item(i).getNodeValue();
					if (name.startsWith("xmlns:")) {
						if (XDConstants.XDEF_INSTANCE_NS_URI.equals(val)
							|| XPool.XDEF31_INSTANCE_NS_URI.equals(val)
							|| XPool.XDEF20_INSTANCE_NS_URI.equals(val)) {
							xdefInstancePrefix = name.substring(6);
							xdefInstanceNSAttr = name;
							break;
						}
					}
				}
				if (xdefInstancePrefix.length() > 0 && _chkDoc == null) {
					//xdi:location
					String key = xdefInstancePrefix + ":location";
					String val = sourceElem.getAttribute(key);
					if (val != null && val.length() > 0) {
						StringParser p = new StringParser(val);
						p.skipSpaces();
						String systemLiteral;
						int i = p.isOneOfTokens("SYSTEM", "PUBLIC");
						if (i == 1) { //PUBLIC
							if (!p.isSpaces()) {
								//Whitespace expected after '&{0}'
								error(XML.XML014, "PUBLIC");
							}
							String pubidLiteral;
							if ((pubidLiteral = ChkParser.readString(p)) !=
								null) {
								if (!p.isSpaces()) {
									//Whitespace expected after '&{0}'
									error(XML.XML014, "PUBLIC");
									return;
								}
								if ((systemLiteral = ChkParser.readString(p)) ==
									null) {
									//Quoted string declaration expected"
									error(XDEF.XDEF504);
									return;
								}
							} else {
								//Quoted string declaration expected"
								error(XDEF.XDEF504);
								return;
							}
						} else if (i == 0) { //SYSTEM
							if (!p.isSpaces()) {
								//Whitespace expected after '&{0}'
								error(XML.XML014, "PUBLIC");
							}
							if ((systemLiteral = ChkParser.readString(p)) ==
								null) {
								//Quoted string declaration expected"
								error(XDEF.XDEF504);
								return;
							}
						} else {
							systemLiteral = p.getSourceBuffer().trim();
						}
						URL u;
						try {
							u = SUtils.resolveSystemID(systemLiteral,
								SUtils.getActualPath());
						} catch (SException ex) {
							Report rep = ex.getReport();
							fatal(rep.getMsgID(),
								rep.getText(),
								rep.getModification());
							return;
						}
						XDPool xdp;
						try {
							xdp = new XBuilder(null).setSource(u).compileXD();
						} catch (Exception ex) {
							//In X-definition are errors&{0}{: }
							fatal(XDEF.XDEF543, ex);
							return;
						}
						key = xdefInstancePrefix+":xdefName"; // xdi:definition
						val = sourceElem.getAttribute(key).trim();
						String value = null;
						if (val.length() > 0) {
							if (xdp != null) {
								value = val;
							}
						} else {
							value = sourceElem.getTagName();
						}
						if (xdp != null) {
							XMDefinition def = xdp.getXMDefinition(value);
							if (def != null) {
								XDOutput stdOut = null;
								key = xdefInstancePrefix+":stdOut"; // xd:stdOut
								val = sourceElem.getAttribute(key).trim();
								if (val.length() > 0 && _chkDoc != null) {
									value = val.trim();
									int index = value.indexOf(',');
									if (index >= 0) {
										String encoding =
											value.substring(index + 1).trim();
										value = value.substring(0,index).trim();
										stdOut = new DefOutStream(value,
											encoding);
									} else {
										stdOut = new DefOutStream(value);
									}
								}
								key = xdefInstancePrefix+":stdErr"; // xd:stdErr
								val = sourceElem.getAttribute(key).trim();
								if (val.length() > 0 && _chkDoc != null) {
									value = val;
									int index = value.indexOf(',');
									if (index >= 0) {
										String encoding =
											value.substring(index + 1).trim();
										value = value.substring(0,index).trim();
										setReportWriter(
											new FileReportWriter(
												value,encoding,true));
									} else {
										setReportWriter(
											new FileReportWriter(value));
									}
								}
								_chkDoc = (ChkDocument) def.createXDDocument();
								_chkDoc._reporter.setReportWriter(
									getReportWriter());
								if (stdOut != null) {
									_chkDoc.setStdOut(stdOut);
								}
							} else {
								//Missing X-definition &{0}{: }
								fatal(XDEF.XDEF530, value);
								return;
							}
						}
					}
				}
				Element el = _doc.createElementNS(ns, elementName);
				for (int i = 0; i < maxAttr; i++) {
					Node n = atrs.item(i);
					el.setAttributeNS(n.getNamespaceURI(),
						n.getNodeName(), n.getNodeValue());
				}
				chkEl = _chkDoc.createRootChkElement(el, true);
			} else {
				Element el = _doc.createElementNS(ns, elementName);
				for (int i = 0; i < maxAttr; i++) {
					Node n = atrs.item(i);
					el.setAttributeNS(n.getNamespaceURI(),
						n.getNodeName(), n.getNodeValue());
				}
				chkEl = parentNode.createChkElement(el);
			}
			List<Attr> atrs1 = new ArrayList<Attr>(); // list of processed atrs
			// Process atrributes which have model
			for (XMData x: chkEl.getXMElement().getAttrs()) {
				String uri = x.getNSUri();
				Attr att = uri == null
					? (Attr) atrs.getNamedItem(x.getName())
					: (Attr) atrs.getNamedItemNS(uri,
						x.getQName().getLocalPart());
				if (att != null) {
					chkEl.newAttribute(att);
					atrs1.add(att);
				}
			}
			// Process remaining atrributes
			for (int i = 0, max = atrs.getLength(); i < max; i++) {
				Attr attr = (Attr) atrs.item(i);
				if (atrs1.contains(attr)) {
					continue; // already processed
				}
				chkEl.newAttribute(attr);
			}
			chkEl.checkElement();
			NodeList nl = sourceElem.getChildNodes();
			for (int i = 0, nodeMax = nl.getLength(); i < nodeMax; i++) {
				Node item = nl.item(i);
				switch (item.getNodeType()) {
					case Node.COMMENT_NODE:
						addText(chkEl);
						chkEl.addComment(item.getNodeValue());
						continue;
					case Node.CDATA_SECTION_NODE:
					case Node.TEXT_NODE:
						_text.append(item.getNodeValue());
						continue;
					case Node.ENTITY_REFERENCE_NODE:
						_text.append(KXmlUtils.nodeToString(
							item, false, false, false));
						continue;
					case Node.ELEMENT_NODE:
						addText(chkEl);
						processElement(chkEl, (Element) item);
						continue;
					case Node.PROCESSING_INSTRUCTION_NODE:
						addText(chkEl);
				}
			}
			addText(chkEl);
			chkEl.addElement();
			if (parentNode == null) { //root element finished!
				_chkDoc.endDocument();
			}
		}

		/** Parse XML source and process check and processing instructions.
		 * @param chkDoc the ChkDocument object.
		 * @param elem the element to be validated.
		 * @return The ChkDocument object.
		 */
		private void xvalidate(final ChkDocument chkDoc, final Element elem) {
			_chkDoc = chkDoc;
			_chkDoc._node = null;
			_chkDoc._element = null;
			XCodeProcessor scp = _chkDoc._scp;
			Properties props = scp.getProperties();
			_chkDoc._scp = null;
			_chkDoc.init(chkDoc._xdef,
				(Document) _chkDoc.getDocument().cloneNode(false),
				chkDoc._reporter,
				props,
				chkDoc._userObject);
			_chkDoc._scp = scp;
			_doc = _chkDoc._doc;
			setReportWriter(chkDoc._reporter.getReportWriter());
			setIndex(-1);
			setLineNumber(-1);
			if (_chkDoc.isDebug() && _chkDoc.getDebugger() != null) {
				// open debugger
				_chkDoc.getDebugger().openDebugger(props, _chkDoc.getXDPool());
			}
			_chkDoc._scp.initscript(); //Initialize variables and methods
			_text = new StringBuilder();
			processElement(null, elem);
		}

		/** Creates a new empty instance of ChkDOMParser. */
		private DOMValidate() {}

		/** Creates a new instance of ChkDOMParser.
		 * @param root The root element of source data to be validated.
		 * @param xdef XDefinition.
		 * @param reportWriter The report writer.
		 * @param stdOut The standard output.
		 * @param stdIn The standard input.
		 * @param userObj The user object connected to parser.
		 * @param outDebug debug output stream.
		 * @param inDebug debug input stream.
		 */
		private DOMValidate(final Element root,
			final XDefinition xdef,
			final ReportWriter reportWriter,
			final XDOutput stdOut,
			final XDInput stdIn,
			final Object userObj,
			final PrintStream outDebug,
			final InputStream inDebug) {
			super(reportWriter, null);
			setIndex(-1);
			setLineNumber(-1);
			_text = new StringBuilder();
			if (xdef != null) {
				_chkDoc = new ChkDocument(xdef);
				_chkDoc._reporter.setReportWriter(getReportWriter());
				if (stdOut != null) {
					_chkDoc.setStdOut(stdOut);
				}
				if (stdIn != null) {
					_chkDoc.setStdIn(stdIn);
				}
				if (userObj != null) {
					_chkDoc.setUserObject(userObj);
				}
				_doc = _chkDoc._doc;
				_chkDoc.getDebugger().setOutDebug(outDebug);
				_chkDoc.getDebugger().setInDebug(inDebug);
			} else {
				_chkDoc = null;
				_doc = null;
			}
			processElement(null, root);
		}
		@Override
		public final void warning(final String id,
			final String msg,
			final Object... mod) {
			putReport(Report.warning(id, msg, mod));
		}
		@Override
		public final void lightError(final String id,
			final String msg,
			final Object... mod) {
			putReport(Report.lightError(id, msg, mod));
		}
		@Override
		public final void error(final String id,
			final String msg,
			final Object... mod) {
			putReport(Report.error(id, msg, mod));
		}
		@Override
		public final void fatal(final String id,
			final String msg,
			final Object... mod) {
			putReport(Report.fatal(id, msg, mod));
		}
		@Override
		public final void putReport(final Report report) {
			super.putReport(report);
		}
		public final void putReport(final byte type,
			final String id,
			final String msg,
			final Object... modif) {
			if (getReportWriter() == null) {
				if (type != Report.WARNING) {
					throw new SRuntimeException(id, msg, modif);
				}
			} else {
				putReport(new Report(type, id, msg, modif));
			}
		}
	}

	/** Parse XML source element and process check and processing instructions.
	 * @param chkDoc The ChkDocument object.
	 */
	final void xparse(final ChkDocument chkDoc) {
		setReportWriter(chkDoc.getReportWriter());
		new DOMValidate().xvalidate(chkDoc, _elem);
	}

}