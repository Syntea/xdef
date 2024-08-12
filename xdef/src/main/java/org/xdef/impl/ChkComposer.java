package org.xdef.impl;

import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDBytes;
import static org.xdef.XDConstants.XON_NS_URI_W;
import static org.xdef.XDConstants.XON_NS_URI_XD;
import org.xdef.XDContainer;
import org.xdef.XDDebug;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTES;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NAMEDVALUE;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_RESULTSET;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XX_DOCUMENT;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefSQLConstructor;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.msg.XDEF;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SError;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import static org.xdef.xon.XonNames.X_VALUE;

/** Constructs XML object according to X-definition.
 * @author Vaclav Trojan
 */
final class ChkComposer extends SReporter {
	private final static QName XONITEMW = new QName(XON_NS_URI_W, X_VALUE);
	private final static QName XONITEM = new QName(XON_NS_URI_XD, X_VALUE);
	/** Root check element. */
	private ChkElement _rootChkElement;
	/** Lexicon of tag names in different languages.*/
	private XDLexicon _lexicon = null;
	/** XDLexicon source language ID.*/
	private int _sourceLanguageID = -1;
	/** XDLexicon destination language ID.*/
	private int _destLanguageID = -1;

	/** Creates a new instance of ChkComposer. */
	ChkComposer(final ReportWriter reporter) {super(reporter);}

	/** Create an element from string. */
	private Element stringToElement(final ChkElement chkEl,
		final String value) {
		if (value == null) {
			return null;
		}
		String qname = "$any".equals(chkEl._xElement.getName())
			? "_ANY_" : chkEl._xElement.getName();
		Document doc = chkEl._element.getOwnerDocument();
		Element e = doc.createElementNS(chkEl._xElement.getNSUri(), qname);
		if (!value.isEmpty()) {
			e.appendChild(doc.createTextNode(value));
		}
		return e;
	}

	/** Set attributes from Container to element (only when not exists).
	 * @param e Element where to set attributes.
	 * @param c Container.
	 * @return updated element.
	 */
	private Element attrsToElement(final Element e, final XDContainer c) {
		if (c.getXDNamedItemsNumber() > 0) {
			XDNamedValue[] ni = c.getXDNamedItems();
			for (XDNamedValue nv: ni) {
				try {
					if (!e.hasAttribute(nv.getName())) {
						XDValue v = nv.getValue();
						if (v != null && !v.isNull()) {
							XDContainer x;
							if (v.getItemId() == XD_CONTAINER
								&&(x=(XDContainer)v).getXDNamedItemsNumber()>0){
								for (XDNamedValue y: x.getXDNamedItems()){
									if ((v=y.getValue())!=null && !v.isNull()) {
										e.setAttribute(y.getName(),
											v.stringValue());
									}
								}
							} else {
								e.setAttribute(nv.getName(), v.toString());
							}
						}
					}
				} catch (DOMException ex) {}
			}
		}
		return e;
	}

	/** Create result from container.
	 * @param chkel CHKElement.
	 * @param xdc Container.
	 * @param ndx index (counter of elements)
	 * @param isRoot true if called from root.
	 * @return XDValue to be used for construction.
	 */
	private XDValue setCreateResult(final ChkElement chkel,
		final XDContainer xdc,
		final int ndx) {
		XDValue item = xdc.getXDItem(ndx);
		if (item!=null && !item.isNull() && item.getItemId()==XD_PARSERESULT) {
			XDParseResult xdp = (XDParseResult) item;
			item = xdp.matches() ? xdp.getParsedValue() : null;
		}
		if (item == null || item.isNull()) {
			return item;
		}
		switch (item.getItemId()) {
			case XD_BYTES: {
				Element el = stringToElement(chkel,((XDBytes)item).getBase64());
				return new DefElement(attrsToElement(el, xdc));
			}
			case XD_ELEMENT:
				return item;
			case XD_CONTAINER: {
				XDContainer xdc1 = (XDContainer) item;
				if (ndx != 0 || xdc == xdc1 || xdc.getXDItemsNumber() != 1
					|| xdc1.getXDNamedItemsNumber() == 0) {
					xdc1 = xdc; // this is nasty! see toElement in Container???.
				}
				Element el = xdc1.toElement(
					chkel.getXXNSURI(), chkel.getXXName());
				return new DefElement(attrsToElement(el, (XDContainer) item));
			}
			case XD_NAMEDVALUE: {
				XDNamedValue x = (XDNamedValue) item;
				String s = x.getName();
				XDValue v = x.getValue();
				if (v.getItemId() == XD_CONTAINER) {
					XDContainer xv = (XDContainer) v;
					if (xv.getXDItemsNumber() == 0
						&& xv.getXDNamedItemsNumber() > 0) {
						// Only named items, create element with attributes
						DefElement el = new DefElement(null,s);
						for (XDNamedValue att : xv.getXDNamedItems()) {
							XDValue val = x.getValue();
							if (val != null && !val.isNull()) {
								switch (val.getItemId()) {
									case XD_ELEMENT:
										el.addXDItem(val.getElement());
										break;
									case XD_CONTAINER:
										XDContainer c = (XDContainer) val;
										el.addXDItem(c.toElement(null, s));
										break;
									default:
										el.getElement().setAttribute(
											att.getName(),
											att.getValue().toString());
								}
							}
						}
						return el;
					} else {
						return new DefElement(xv.toElement(null, s));
					}
				}
				break;
			}
			case XD_RESULTSET: {
				Element el = item.getElement();
				return new DefElement(attrsToElement(el, xdc));
			}
		}
		Element el = stringToElement(chkel, item.toString());
		return new DefElement(attrsToElement(el, xdc));
	}

	/** Run create mode with given ChkDocument.
	 * @param chkDoc The ChkDocument object.
	 * @param nsURI NameSpace of result element.
	 * @param qname qualified name of result element.
	 */
	final void xcreate(final ChkDocument chkDoc,
		final String nsURI,
		final String qname) {
		XElement oldXElement = chkDoc._xElement; //Save XElement
		if (oldXElement == null || oldXElement._xon == 0) {
			chkDoc._xElement = null;
		}
		setReportWriter(chkDoc.getReportWriter());
		boolean oldMode = chkDoc.isCreateMode();
		chkDoc.setCreateMode(true);
		try {
			XPool xp = ((XPool) chkDoc.getXDPool());
			_lexicon = xp._lexicon;
			_sourceLanguageID = chkDoc._sourceLanguageID;
			_destLanguageID = chkDoc._destLanguageID;
			_rootChkElement = (ChkElement) chkDoc.prepareRootXXElementNS(
				nsURI, qname, false);
			Object obj = chkDoc.getCreateContext();
			Element elem = (obj != null && (obj instanceof Element))
				? (Element) obj : null;
			if (_rootChkElement._scp.isDebugMode()
				&& _rootChkElement._scp.getDebugger() != null) {
				// open debugger
				_rootChkElement._scp.getDebugger().openDebugger(
					_rootChkElement._scp.getProperties(),
					_rootChkElement._rootChkDocument.getXDPool());
			}
			chkDoc._scp.initscript(); //Initialize variables and methods
			composeRoot(elem);
		} finally {
			chkDoc._xElement = oldXElement; //restore original value of XElement
			KXmlUtils.setNecessaryXmlnsAttrs(chkDoc.getElement());
			chkDoc.setCreateMode(oldMode);
			chkDoc._sourceLanguageID = _sourceLanguageID;
			chkDoc._destLanguageID = _destLanguageID;
		}
	}

	/** Compose the XML element from the source XML element. The name
	 * of root element of result is given by parameter. For construction is used
	 * the X-definition specified by ChkComposer constructor.
	 * @param sourceElem The element with source data.
	 */
	private void composeRoot(final Element sourceElem) {
		try {
			_rootChkElement.getXDDocument().setXDContext(sourceElem);
			XElement xElement = _rootChkElement._xElement;
			XDValue result;
			if (xElement._compose >= 0) {
				result = execComposeElement(_rootChkElement, sourceElem, null);
				if (result != null && !result.isNull()) {
					switch (result.getItemId()) {
						case XD_RESULTSET: {
							XDResultSet it = (XDResultSet) result;// is iterator
							try {
								XDValue item = it.nextXDItem(_rootChkElement);
								if (item == null) {
									result = null;
									break;
								}
								result = item;
								if (it.nextXDItem(_rootChkElement) != null) {
									//Too many iterator items for &{0}
									_rootChkElement.error(XDEF.XDEF565,
										_rootChkElement._name);
									_rootChkElement._scp.closeResultSet(it);
								}
							} catch (SRuntimeException ex) {
								_rootChkElement.putReport(ex.getReport());
								return;
							}
							break;
						}
						case XD_NAMEDVALUE: {
							XDNamedValue nv = (XDNamedValue) result;
							XDValue v = nv.getValue();
							if (v == null || v.isNull()) {
								result = null;
								break;
							}
							DefContainer xdc = new DefContainer();
							xdc.setXDNamedItem(nv);
							xdc.addXDItem(nv.getValue());
							result = setCreateResult(_rootChkElement, xdc, 0);
							break;
						}
						case XD_DATETIME:
						case XD_DURATION:
						case XD_DOUBLE:
						case XD_STRING: {
							result = new DefElement(stringToElement(
								_rootChkElement, result.stringValue()));
							_rootChkElement._sourceElem = sourceElem;
							break;
						}
						case XD_CONTAINER:
						case XD_ELEMENT:
							break;
						case XD_BOOLEAN: {
							if (!result.booleanValue()) {
								result = null;
								break;
							}
						}
						default: {
							result = createContainer(_rootChkElement, result);
							_rootChkElement._sourceElem = sourceElem;
						}
					}
				}
			} else { //create section not declared => create default result
				result = (xElement.minOccurs() <= 0) ? null // ignore
					: (sourceElem == null)//no context -> create default element
						? new DefElement(KXmlUtils.newDocument() // any?
							.createElementNS(xElement.getNSUri(),
							"$any".equals(xElement.getName()) ? "_ANY_" //any
							: xElement.getName())) // no any, default element
						: new DefElement(sourceElem); //sourceElem as context
			}
			composeElement(null, _rootChkElement, result, null);
			_rootChkElement._rootChkDocument.endDocument();
			if (_rootChkElement.getElement() == null) {
				error(XDEF.XDEF556); //Root element was not created
			}
		} catch (SError e) {
			Report rep = e.getReport();
			//X-definition canceled
			if (rep != null && "XDEF906".equals(rep.getMsgID())) {
				error(rep.getMsgID(), rep.getText(), rep.getModification());
				_rootChkElement._rootChkDocument.endDocument();
			} else {
				throw new SRuntimeException(e.getMessage(), e.getCause());
			}
		}
	}

	/** Create Container with elements from integer or boolean value. */
	private XDValue createContainer(final ChkElement chkEl,
		final XDValue expr) {
		long i = 0;
		XElement xElem = chkEl._xElement;
		if (expr.getItemId() == XD_LONG) {
			if ((i = expr.longValue()) > 0 && xElem.maxOccurs() > 0) {
				if (xElem.maxOccurs() < i) {
					i = xElem.maxOccurs() + 1; //enable to put "too many" error
				}
			}
		} else if (expr.getItemId() == XD_BOOLEAN) {
			return expr;
		}
		if (i <= 0) {
			return null;
		}
		DefElement[] values = new DefElement[(int) i];
		if ("$any".equals(xElem.getName()) &&
			expr.getItemId() == XD_CONTAINER) {
			XDContainer xc = (XDContainer) expr;
			xc = xc.getXDElements();
			for (int j = 0, n = xc.getXDItemsNumber(); j < i; j++) {
				values[j] = j < n
					? new DefElement(xc.getXDItem(j).getElement())
					: new DefElement(KXmlUtils.newDocument(
						xElem.getNSUri(), "_ANY_", null).getDocumentElement());
			}
		} else {
			for (int j = 0; j < i; j++) {
				values[j] = new DefElement(
					KXmlUtils.newDocument(xElem.getNSUri(),xElem.getName(),null)
						.getDocumentElement());
			}
		}
		return new DefContainer(values);
	}

	/** Compose Element to ChkElement and create following text nodes if
	 * xtxt object from the attribute xd:text is not null.
	 * @param chkElement actual ChkElement.
	 * @param elem Element from which to create Element to chkElement (or null).
	 * @param xtxt xtxt object from the attribute xd:text or null.
	 */
	private void composeElAndTxt(final ChkElement parentChkElem,
		final ChkElement chkElement,
		final Element elem,
		final XData xtxt) {
		composeElement(chkElement, elem);
		if (xtxt != null) {
			Element e1 = elem;
			if (e1 != null) {
				Node n = e1.getNextSibling();
				if (n  == null || n.getNodeType() != Node.CDATA_SECTION_NODE
					&& n.getNodeType() != Node.TEXT_NODE) {
					e1 = null;
				}
			}
			createTextNode(parentChkElem == null ? chkElement : parentChkElem,
				e1, e1, xtxt, elem); //lastNode
		}
	}

	/** Create Element (or sequence of Elements).
	 * @param parentChkElem parent ChkElement.
	 * @param chkElement actual chkElement.
	 * @param result object from which the element(s) will be created.
	 * @param xtxt XData object from the attribute xd:text or null.
	 */
	private void composeElement(final ChkElement parentChkElem,
		final ChkElement chkElement,
		final XDValue result,
		final XData xtxt) {
		if (result == null || result.isNull()) {
			chkElement.updateElement(null); // no source - delete this element
			return;
		}
		XElement xElem = chkElement._xElement;
		DefContainer xdc;
		Element el;
		switch (result.getItemId()) {
			case XD_ELEMENT: {
				el = result.getElement();
				chkElement.setElemValue(el);
				if (el != null) {
					composeElAndTxt(parentChkElem, chkElement, el, xtxt);
				}
				return;
			}
			case XD_RESULTSET: {
				XDResultSet it = (XDResultSet) result;
				XDValue item = it.lastXDItem();
				if (item == null) { // no source - delete this element
					chkElement.updateElement(null);
					return;
				}
				int n = 0;
				ChkElement chkElem = chkElement;
				try {
					for (; item != null; item = it.nextXDItem(chkElement)) {
						if (n > 0) {
							if (parentChkElem == null) { //root
								//Required element &{0} is missing
								chkElem.error(XDEF.XDEF539, xElem.getName());
								return; //root
							}
							chkElem = genChkElement(parentChkElem, null, xElem);
						}
						chkElem._iterator = (XDResultSet) result;
						switch (item.getItemId()) {
							case XD_ELEMENT:
								if (n >= xElem.maxOccurs()) {
									//Too many iterator items for &{0}
									chkElem.error(XDEF.XDEF565, xElem.getName());
									chkElem._scp.closeResultSet(it);
									return;
								}
								el = item.getElement();
								if (el != null) {
									chkElem.setElemValue(el);
									composeElAndTxt(parentChkElem,chkElem,el,xtxt);
									n++;
									continue;
								}
								break;
							case XD_RESULTSET:
								if (n >= xElem.maxOccurs()) {
									//Too many iterator items for &{0}
									chkElem.error(XDEF.XDEF565, xElem.getName());
									chkElem._scp.closeResultSet(it);
									return;
								}
								el = item.getElement();
								if (el != null) {
									chkElem.setElemValue(el);
									composeElAndTxt(parentChkElem,chkElem,el,xtxt);
									n++;
									continue;
								}
								break;
							case XD_STRING:
								if (!item.stringValue().isEmpty()) {
									el = it.getElement();
									if (el != null) {
										chkElem.setElemValue(el);
										composeElAndTxt(parentChkElem,
											chkElem, el, xtxt);
										n++;
										continue;
									}
								}
						}
						chkElement.updateElement(null);
					}
					chkElem._scp.closeResultSet(chkElem._iterator);
				} catch (SRuntimeException ex) {
					chkElem.putReport(ex.getReport());
					return;
				}
				if (n == 0) {
					chkElem.updateElement(null);//no source; delete this element
				}
				return;
			}
			case XD_NAMEDVALUE:
				XDNamedValue nv = (XDNamedValue) result;
				XDValue v = nv.getValue();
				if (v == null || v.isNull()) {
					chkElement.updateElement(null); // delete this element
					return;
				}
				xdc = new DefContainer();
				xdc.setXDNamedItem(nv);
				xdc.addXDItem(nv.getValue());
				break;
			case XD_CONTAINER: {
				xdc = (DefContainer) result;
				break;
			}
			case XD_BOOLEAN:
				if (result.booleanValue()) {
					composeElAndTxt(parentChkElem, chkElement, null, xtxt);
				} else {
					chkElement.updateElement(null); //delete this element
				}
				return;
			case XD_LONG:
				if (result.longValue() > 0) {
					composeElAndTxt(parentChkElem, chkElement, null, xtxt);
				} else {
					chkElement.updateElement(null); //delete this element
				}
				return;
			default: //???
				return; // should never happen!!!
		}
		// here is prepared Container (i.e. from XD_NAMEDVALUE or XD_CONTAINER)
		if (xdc.getXDItemsNumber() == 0) {
			if (xdc.getXDNamedItemsNumber() == 0) {
				chkElement.updateElement(null);
			} else {
				el = xdc.toElement(null, null);
				composeElAndTxt(parentChkElem, chkElement, el, xtxt);
			}
			return;
		}
		int  n = 0;
		int max = xElem.maxOccurs();
		ChkElement chkElem = chkElement;
		for (int i = 0; i < max; i++) {
			XDValue val = setCreateResult(chkElem, xdc, i);
			if (++n > max || val == null) {
				break;
			}
			if (val.isNull()) {
				continue; // skip this item
			}
			if (n > 1) { // not first, so create new ChkElemement
				chkElem = genChkElement(parentChkElem, null, xElem);
			}
			chkElem.setElemValue(val.getElement());
			composeElAndTxt(parentChkElem, chkElem, val.getElement(), xtxt);
		}
		if (n == 0) {
			chkElem.updateElement(null);//no source; delete this element
		}
	}

	/** Execute "compose" action for value of an attribute or a text nodes.
	 * @param addr The address of action or negative number.
	 * @param mode mode of process.
	 * @param chkElem The actual check element containing attribute/text.
	 * @param sourceElem The source element.
	 */
	private void execComposeValue(final int addr,
		final byte mode,
		final ChkElement chkElem,
		final Element sourceElem) {
		String xpos = chkElem._xPos; //save xPos
		chkElem.debugXPos(XDDebug.CREATE);
		if (addr >= 0) {
			chkElem.setElemValue(sourceElem);
			chkElem.setXXType(mode);
			chkElem.setTextValue(null);
			// prepare variables declared in the script (do not make it twice)
			if (chkElem._xElement._varinit >= 0 && chkElem._variables == null) {
				chkElem._variables = new XDValue[chkElem._xElement._varsize];
				chkElem.exec(chkElem._xElement._varinit, (byte) 'E');
				chkElem.copyTemporaryReports();
			}
			XDValue result = chkElem.exec(addr, mode);
			chkElem.copyTemporaryReports();
			if (result != null && !result.isNull()) {
				String s = result.toString();
				if (!s.isEmpty() || mode == 'A') {
					//attribute may have empty string value
					chkElem.setTextValue(s);
				}
			}
		}
		chkElem._xPos = xpos;  //reset xPos
	}

	/** Find elements with given name in child nodes. If the argument "elem" has
	 * the name and namespace URI as required then add this element to result
	 * and return true. Otherwise try to find direct child nodes with such name
	 * and namespace URI, add them to result and return true. If nothing was
	 * found then return false. Note this is very NASTY trick!
	 * @param result Container where found elements are added.
	 * @param chkEl actual object ChkElement.
	 * @param elem where to search.
	 * @param lastElement last processed element from source or null.
	 */
	private void getChildElementsByName(final DefContainer result,
		final ChkElement chkEl,
		final Element elem,
		final Element lastElement) {
		if ("$any".equals(chkEl._xElement.getName())) { //any element
			for (Node node = elem.getFirstChild();
				node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					result.addXDItem(new DefElement((Element) node));
				}
			}
			return;
		}
		String uri = chkEl.getXMElement().getNSUri();
		String qname = getSrcLexiconName(chkEl.getXMElement());
		if (qname == null) {
			qname = chkEl.getXMElement().getName();
		}
		int ndx = qname.indexOf(':');
		String localName = ndx < 0 ? qname : qname.substring(ndx + 1);
		String locnm = elem.getNamespaceURI() == null
			? elem.getNodeName() : elem.getLocalName();
		final int max = chkEl.getXMElement().maxOccurs();
		Node first = elem.getFirstChild();
		if (localName.equals(locnm)
			&& (uri == null || uri.equals(elem.getNamespaceURI()))) {
			// Find the node that matches the model
			int m = 0;
			for (Node node = first; node!=null;  node = node.getNextSibling()) {
				String u = node.getNamespaceURI();
				locnm = u == null ? node.getNodeName() : node.getLocalName();
				if ((u == null && "_".equals(locnm)||((localName.equals(locnm))
					&& ((uri == null || uri.equals(u)))))) {
					if (chkEl._xElement._match >= 0) { // try matches section
						Element el = chkEl.getElemValue(); // savet _elemValue
						chkEl.setElemValue((Element) node);
						XDValue value =
							chkEl.exec(chkEl._xElement._match, (byte) 'E');
						chkEl.setElemValue(el); // reset _elemValue
						if (value == null || value.isNull()
							|| !value.booleanValue()) {
							continue; // not matches
						}
					}
					// node matches model
					result.addXDItem(new DefElement((Element) node));
					if (++m >= max) {//more elements found then maxOccurs
						return; // do not nead others
					}
				}
			}
			if (result.isEmpty()) {
				result.addXDItem(new DefElement(elem));
				return; //element is the element itself
			}
		}
		if (first == null) {
			return; //nothing found
		}
		int m = 0;
		for (int i = 0; i < result.getXDItemsNumber(); i++) {
			Element e = result.getXDElement(i);
			String u = e.getNamespaceURI();
			locnm = u==null ? e.getNodeName() : e.getLocalName();
			if ("_".equals(locnm) && u == null
				|| (localName.equals(locnm) && ((uri==null || uri.equals(u))))){
				if (chkEl._xElement._match >= 0) {
					Element el = chkEl.getElemValue();
					chkEl.setElemValue(e);
					XDValue value= chkEl.exec(chkEl._xElement._match,(byte)'E');
					chkEl.setElemValue(el);
					if (value==null || value.isNull() || !value.booleanValue()){
						continue;
					}
				}
				if (++m >= max) {//more elements found then maxOccurs
					return; // do not nead others
				}
			}
		}
		// Look for elements following lastElement
		Node next = (lastElement!=null && lastElement.getParentNode()==elem)
			? lastElement.getNextSibling() : first;
		for (Node node = next; node != null; node = node.getNextSibling()) {
			String u = node.getNamespaceURI();
			locnm = u==null ? node.getNodeName() : node.getLocalName();
			if ("_".equals(locnm) && u==null
				|| (localName.equals(locnm) && ((uri==null || uri.equals(u))))){
				if (chkEl._xElement._match >= 0) {
					Element el = chkEl.getElemValue();
					chkEl.setElemValue((Element) node);
					XDValue value= chkEl.exec(chkEl._xElement._match,(byte)'E');
					chkEl.setElemValue(el);
					if (value==null || value.isNull() || !value.booleanValue()){
						continue;
					}
				}
				result.addXDItem(new DefElement((Element) node));
				if (++m >= max) {//more elements found then maxOccurs
					return; // do not nead others
				}
			}
		}
		if (m > 0 || first == next) {//some elements found or looked from first
			return; // do not find from first
		}
		// nothing found, look again now from first
		for (Node node = first; node != next; node = node.getNextSibling()) {
			String u = node.getNamespaceURI();
			locnm = u==null ? node.getNodeName() : node.getLocalName();
			if ("_".equals(locnm) && u==null
				|| (localName.equals(locnm) && ((uri==null || uri.equals(u))))){
				if (chkEl._xElement._match >= 0) {
					Element el = chkEl.getElemValue();
					chkEl.setElemValue((Element) node);
					XDValue value= chkEl.exec(chkEl._xElement._match,(byte)'E');
					chkEl.setElemValue(el);
					if (value==null || value.isNull() || !value.booleanValue()){
						continue;
					}
				}
				if (++m >= max) {//more elements found then maxOccurs
					return; // do not nead others
				}
				result.addXDItem(new DefElement((Element) node));
			}
		}
	}

	/** Execute "compose" action.
	 * @param chkEl The actual check element.
	 * @param sourceElem The source element from which the result is composed.
	 * @param lastElement last processed element from source or null.
	 * @return The XDValue object or <i>null</i>.
	 */
	private XDValue execComposeElement(final ChkElement chkEl,
		final Element sourceElem,
		final Element lastElement) {
		int addr;
		if ((addr = chkEl._xElement._compose) < 0) {
			if (sourceElem == null) {
				if (chkEl._xElement.minOccurs() <= 0) {
					return null;
				}
				String qname = "$any".equals(chkEl._xElement.getName())
					? "_ANY_" : chkEl._xElement.getName();
				chkEl._sourceElem = KXmlUtils.newDocument(
					chkEl._xElement.getNSUri(),qname,null).getDocumentElement();
				return new DefElement(chkEl._sourceElem);
			}
			//default contex (no script specified)
			DefContainer xdc = new DefContainer();
			getChildElementsByName(xdc, chkEl, sourceElem, lastElement);
			Element el = xdc.getXDElement(0);
			//if somethig found, let's set first element as source context,
			//otherwise set source from the argument.
			chkEl._sourceElem = el != null ? el : sourceElem;
			if (el == null ||
				xdc.getXDItemsNumber() < chkEl._xElement.minOccurs()) {
				if (chkEl._parent != null
					&& chkEl._parent.getItemId() != XX_DOCUMENT
					&& ((ChkElement) chkEl._parent)._selector != null
					&& ((ChkElement) chkEl._parent)._selector._kind
						!= XMSEQUENCE) {
					return xdc; //if choice or mixed do not create dummy items
				}
				//create required minimum number of items
				for (int i = xdc.getXDItemsNumber();
					i < chkEl._xElement.minOccurs(); i++) {
					xdc.addXDItem(new DefElement(sourceElem));
				}
			}
			return xdc;
		}
		chkEl.setElemValue(sourceElem);
		chkEl.debugXPos(XDDebug.CREATE);
		XDValue result = chkEl.exec(addr, (byte) 'E');
		chkEl.copyTemporaryReports();
		if (result != null && !result.isNull()) {
			Element el = null;
			switch (result.getItemId()) {
				case XD_RESULTSET: {
					XDResultSet it = (XDResultSet) result;
					try {
						chkEl._iterator = it;
						XDValue item = it.nextXDItem(chkEl);
						if (item != null) {
							el = it.getElement();
							result = it;
						} else {
							return new DefElement();
						}
					} catch (SRuntimeException ex) {
						chkEl.putReport(ex.getReport());
					}
					break;
				}
				case XD_NAMEDVALUE:
					result = new DefContainer(result);
				case XD_CONTAINER: {
					DefContainer xdc = (DefContainer) result;
					if (xdc.getXDItemsNumber() > 0) {
						if ((el = xdc.getXDElement(0)) == null) {
							result = new DefElement(el = xdc.toElement(
								chkEl.getXXNSURI(), chkEl.getXXName()));
						}
					}
					break;
				}
				case XD_ELEMENT:
					el = result.getElement();
					break;
				case XD_STRING: {
					el = stringToElement(chkEl, result.stringValue());
					break;
				}
				default:
					return createContainer(chkEl, result);
			}
			if (el != null && el.getParentNode() != null) {
				chkEl._sourceElem = el;
			}
		}
		return result;
	}

	private String getDestLexiconName(final String xdPosition) {
		int languageID = _destLanguageID;
		int savedLanguageID = _sourceLanguageID;
		if (languageID < 0) {
			languageID = savedLanguageID;
		}
		if (_lexicon != null && languageID >= 0) {
			_rootChkElement._rootChkDocument._sourceLanguageID = languageID;
			String newName = _lexicon.findText(xdPosition, languageID);
			_rootChkElement._rootChkDocument._sourceLanguageID =savedLanguageID;
			return newName;
		}
		return null;
	}

	/** Get modified name of given model from lexicon or null.
	 * @param xmNode model to be checked.
	 * @return modified name of XPosition from lexicon or original model name.
	 */
	private String getSrcLexiconName(final XMNode xmNode) {
		String xdPosition = xmNode.getXDPosition();
		return _lexicon != null && _sourceLanguageID >= 0
			? _lexicon.findText(xdPosition, _sourceLanguageID)
			: xmNode.getName();
	}

	/** Compose element from source.
	 * @param chkElem model of constructed element.
	 * @param sourceElem source element (or null).
	 */
	private void composeElement(final ChkElement chkElem,
		final Element sourceElem) {
		XElement xel = chkElem._xElement;
		Element savedSource =  chkElem._sourceElem; //save source element
		chkElem._sourceElem = sourceElem;
		// create attributes
		String xpos = chkElem._xPos;
		if (chkElem._parent._parent != null) { // not root
			chkElem.initElem();
		}
		XData[] xattrs = (XData[]) xel.getAttrs();
		chkElem.setXXType((byte) 'A');
		for (XData xatr: xattrs) {
			chkElem._xdata = xatr;
			String attrName = getSrcLexiconName(xatr);
			if (attrName.charAt(0) == '$') { //special XDEF attribute
				continue; // skip xd:text etc
			}
			chkElem._xPos = xpos + "/@" + attrName;
			chkElem.debugXPos(XDDebug.CREATE);
			if (xatr._compose < 0) {
				//no source action, try to find attribute in source element.
				if (sourceElem == null || !sourceElem.hasAttribute(attrName)) {
					chkElem.setTextValue(null);
				} else {
					chkElem.setTextValue(sourceElem.getAttribute(attrName));
				}
			} else {//execute user source action
				chkElem.setProcessedAttrName(attrName);
				chkElem.setProcessedAttrURI(xatr.getNSUri());
				execComposeValue(xatr._compose, (byte)'A', chkElem, sourceElem);
				chkElem.setProcessedAttrName(null);
				chkElem.setProcessedAttrURI(null);
			}
			String s = chkElem.getTextValue();
			if (s != null) {
				if (!s.isEmpty() || xatr._ignoreEmptyAttributes != 0 //set attr
					|| xatr._ignoreEmptyAttributes == 0
					&& xel._ignoreEmptyAttributes != 0
					||  xel._ignoreEmptyAttributes == 0
					&& chkElem._rootChkDocument._ignoreEmptyAttributes != 0) {
					// set attribute
					String newName = getDestLexiconName(xatr.getXDPosition());
					if (newName != null) {
						attrName = newName;
					}
					if (xatr.getNSUri() == null) {
						chkElem._element.setAttribute(attrName, s);
					} else {
						chkElem._element.setAttributeNS(xatr.getNSUri(),
							attrName, s);
					}
				}
			}
		}
		chkElem._xdata = null;
		chkElem._xPos = xpos;
		NamedNodeMap nm = chkElem._element.getAttributes();
		int languageID = _rootChkElement._rootChkDocument._destLanguageID;
		int savedLanguageID =_rootChkElement._rootChkDocument._sourceLanguageID;
		_rootChkElement._rootChkDocument._sourceLanguageID = languageID;
		//validate attributes
		for (int i = nm.getLength() - 1; i >= 0; i--) {
			chkElem.newAttribute((Attr) nm.item(i));
		}
		_rootChkElement._rootChkDocument._sourceLanguageID =savedLanguageID;
		if (xel._moreAttributes == 'T' && sourceElem != null) {
			NamedNodeMap nm1 = sourceElem.getAttributes();
			if (nm1!= null) {// copy other attributes
				for (int i = nm1.getLength() - 1; i >= 0; i--) {
					Attr attr = (Attr) nm1.item(i);
					String uri = attr.getNamespaceURI();
					if (uri == null) {
						String name = attr.getNodeName();
						if (!xel.hasDefAttr(name)) {
							chkElem.newAttribute(attr);
						}
					} else {
						String name = attr.getLocalName();
						if (name ==null) {
							name = attr.getNodeName();
						}
						if (!xel.hasDefAttrNS(uri, name)) {
							chkElem.newAttribute(attr);
						}
					}
				}
			}
		}
		chkElem.setXXType((byte) 'E');
		chkElem.checkElement();
		chkElem._sourceElem = savedSource; //reset source
		composeChildNodes(chkElem, sourceElem, savedSource, 0, null, null);
		chkElem._sourceElem = sourceElem; //set source
		XData xtxt;
		if ((xtxt = xel.getDefAttr("$textcontent", -1)) != null) {
			chkElem._xdata = xtxt;
			chkElem.setXXType((byte) 'T');
			chkElem._xPos = xpos + "/text()";
			chkElem.debugXPos(XDDebug.CREATE);
			if (xtxt._compose >= 0) {
				if (KXmlUtils.getTextContent(chkElem._element).isEmpty()) {
					// call compose only if no text was created
					execComposeValue(xtxt._compose,
						(byte)'T', chkElem, sourceElem);
				}
			} else {
				if (sourceElem != null) {//default;
					String s = KXmlUtils.getTextContent(sourceElem).trim();
					chkElem.setTextValue(!s.isEmpty() ? s : null);
				}
			}
			chkElem._xPos = xpos;
			if (chkElem.getTextValue() != null) {
				chkElem.addText(chkElem.getTextValue());
			}
			chkElem.setXXType((byte) 'E');
			chkElem._xdata = null;
		}
		//check all child nodes which are not in a group
		for (int i = 0; i < chkElem.getDefinitionMaxIndex(); i++) {
			XNode xn = chkElem.getDefElement(i);
			switch (xn.getKind()) {
				case XMELEMENT:
					chkElem.chkElementAbsence(i, (XElement) xn, null);
					break;
				case XMTEXT:
					chkElem.chkTextAbsence(i, (XData) xn, false, null);
					break;
				case XMSEQUENCE:
				case XMCHOICE:
				case XMMIXED:
					i = ((XSelector) xn)._endIndex;
			}
		}
		chkElem._actDefIndex = -1;
		chkElem._nextDefIndex = chkElem.getDefinitionMaxIndex();
		chkElem.addElement();
	}

	/** Create default text node (i.e. from text model).
	 * @param chkElem actual ChkElement object.
	 * @param sourceElem actual source element.
	 * @param savedSource saved source element.
	 * @param xText text node model.
	 * @param lastNode last processed text from source item or <i>null</i>.
	 */
	private Node createTextNode(final ChkElement chkElem,
		final Element sourceElem,
		final Element savedSource,
		final XData xtxt,
		final Node lastNode) {
		chkElem._sourceElem = savedSource;
		chkElem._xdata = xtxt;
		chkElem.setXXType((byte) 'T');
		chkElem.setTextValue(null);
		Node result = null;
		String xpos = chkElem._xPos;
		chkElem._xPos += "/text()";
		chkElem.debugXPos(XDDebug.CREATE);
		if (xtxt._compose >= 0) {
			execComposeValue(xtxt._compose, (byte) 'T', chkElem, sourceElem);
			result = lastNode;
		} else {
			if (sourceElem == null) {
				chkElem.setTextValue(null);
				chkElem._xdata = null;
				return null;
			}
			chkElem.setTextValue(null);
			Node n = lastNode == null ?
				sourceElem.getFirstChild() : lastNode.getNextSibling();
			String s;
			if (n == null && sourceElem instanceof DefSQLConstructor.MyElement
				&& "$text".equals(chkElem.getXMNode().getName())
				&& chkElem.getParent().getChildXXNodes().length == 1) {
				NamedNodeMap nnm = sourceElem.getAttributes();
				//find column in the row of database Resultset
				n = nnm.getNamedItem(chkElem.getXMElement().getName());
				s = n != null ? n.getNodeValue() : null;
			} else {
				StringBuilder sb = new StringBuilder();
				while (n != null) {
					short type = n.getNodeType();
					if (type == Node.TEXT_NODE ||
						type == Node.CDATA_SECTION_NODE ||
						type == Node.ENTITY_REFERENCE_NODE) {
						result = n;
						sb.append(n.getNodeValue());
						for (;;) {
							//concatenate adjacent text nodesů skip comments, PI
							n = n.getNextSibling();
							if (n != null) {
								switch (n.getNodeType()) {
									case Node.TEXT_NODE:
									case Node.CDATA_SECTION_NODE:
									case Node.ENTITY_REFERENCE_NODE:
										sb.append(n.getNodeValue());
									case Node.COMMENT_NODE:
									case Node.PROCESSING_INSTRUCTION_NODE:
										continue;
								}
							}
							break;
						}
						break;
					} else {
						n = n.getNextSibling();
					}
				}
				s = sb.toString();
			}
			if (s != null && !(s = chkElem.textWhitespaces(xtxt, s)).isEmpty()){
				chkElem.setTextValue(s);
			}
		}
		chkElem._xPos = xpos;
		if (chkElem.getTextValue() != null) {
			chkElem.addText(chkElem.getTextValue());
			chkElem.setTextValue(null);
		}
		chkElem._xdata = null;
		chkElem.setXXType((byte) 'E');
		return result;
	}

	private int groupNotGenerated(final ChkElement chkEl, XSelector xsel) {
		int i = chkEl._nextDefIndex = xsel._endIndex + 1;
		chkEl._actDefIndex = -1;
		if (xsel.minOccurs() > 0) { //ignore, illegal
			//Minimum occurrence not reached for &{0}
			chkEl.error(XDEF.XDEF555, xsel.getName().substring(1));
		}
		return i;
	}

	private boolean equalNames(final XNode x, final Node y) {
		String name = x.getName();
		if ("$any".equals(name)) {
			return true;
		}
		String uri = x.getNSUri();
		if (uri == null) {
			if (y.getNamespaceURI() != null) {
				return false;
			}
			return y.getNodeName().equals(name);
		} else if (!uri.equals(y.getNamespaceURI())) {
			return false;
		}
		int i = name.indexOf(':');
		if (i >= 0) {
			name = name.substring(i + 1);
		}
		String name1 = y.getLocalName();
		if (name1 == null) {
			name1 = y.getNodeName();
		}
		return name1.equals(name);
	}

	/** Compose child nodes.
	 * This is very nasty code, should be written better! However, it works.
	 * @param chkElem actual ChkElement object.
	 * @param sourceEl actual source element.
	 * @param savedSource saved source element.
	 * @param index index to defList.
	 * @param lastTextNode last processed text from source item or null.
	 * @param lastElement last processed element from source or null.
	 * @return index to defList.
	 */
	private int composeChildNodes(final ChkElement chkEl,
		final Element sourceEl,
		final Element savedSource,
		final int index,
		final Node lastTextNode,
		final Element lastElement) {
		int i = index;
		chkEl.setElemValue(sourceEl);
		XNode xNode;
		int count;
		Node lastText = lastTextNode;
		Element lastElem = lastElement;
		XData xtxt = chkEl._xElement.getDefAttr("$text", -1);
		if (xtxt != null) {
			if (xtxt._compose < 0) {
				if (sourceEl != null) {
					Node n = sourceEl.getFirstChild();
					if (n != null
						&& (n.getNodeType() == Node.TEXT_NODE
						|| n.getNodeType() == Node.CDATA_SECTION_NODE)) {
						lastText = createTextNode(
							chkEl, sourceEl, savedSource, xtxt, null);
					}
				}
			} else {
				xNode = chkEl.getDefElement(i);
				if (i != 0 || xNode == null
					|| xNode.getKind() == XMELEMENT
					|| xNode.getKind() == XMTEXT
					&& ((XData) xNode)._compose < 0) {
					lastText = createTextNode(
						chkEl, sourceEl, savedSource, xtxt, lastText);
				}
			}
		}
		while((xNode = chkEl.getDefElement(i)) != null) {
			switch (xNode.getKind()) {
				case XMELEMENT: {
					XElement childDef = (XElement) xNode;
					chkEl._sourceElem = savedSource;
					count = chkEl.getRefNum(i);
					ChkElement childChkEl =
						prepareChkElement(chkEl, null, childDef, i);
					//if selector is mixed set null to lastElem
					Element lastEl = chkEl._selector != null
							&& chkEl._selector._kind == XMMIXED
						? null : lastElem;
					XDValue result = execComposeElement(
						childChkEl, sourceEl, lastEl);
					if (childChkEl._xElement.getXonMode() != 0
						&& childChkEl._xElement._compose < 0
						&& result.getItemId() == XD_CONTAINER
						&& ((DefContainer) result).isEmpty()) {
						// xon model of item and empty reslut (Container)
						if (childChkEl._xElement.getXonMode() != 0
							&& (XONITEMW.equals(childChkEl._xElement.getQName())
							|| XONITEM.equals(childChkEl._xElement.getQName()))
							&& childChkEl._xElement.getAttr(
								XonNames.X_VALATTR).getComposeCode() < 0){
							// if create section do not create this node
							result = new DefBoolean(false);
						} else if (result.getItemId() == XD_CONTAINER){
							XDContainer c = (XDContainer) result;
							if (chkEl._selector == null ||
								chkEl._selector._kind == XMCHOICE) {
								if (!c.isEmpty()) {
									c.addXDItem(new DefBoolean(true));
								}
							} else {
								c.addXDItem(new DefBoolean(true));
							}
						}
					} else if (childChkEl._xElement._compose < 0 &&
						chkEl._sourceElem != null) {
						if (result.getItemId() == XD_CONTAINER) {
							lastElem = ((DefContainer) result).getXDElement(0);
						}
						//propagate explicit source context
						if (!equalNames(childChkEl._xElement,
							childChkEl._sourceElem)) {
							childChkEl._sourceElem = chkEl._sourceElem;
						}
					}
					if (result == null || result.isNull()
						|| (result.getItemId() == XD_LONG
						&& result.intValue() <= 0)
						|| (result.getItemId() == XD_BOOLEAN
						&& !result.booleanValue())) { //nothing generate
						childChkEl.updateElement(null);
						chkEl._nextDefIndex = ++i; //next child definition
						chkEl._actDefIndex = -1;
						continue;
					}
					// now result is never null
					chkEl._chkChildNodes.add(childChkEl);
					if ("$any".equals(childDef.getName())) {
						Element el = childChkEl._element;
						switch (result.getItemId()) {
							case XD_ELEMENT:
								chkEl._element.replaceChild(
									result.getElement(), el);
								childChkEl._sourceElem = chkEl._sourceElem;
								childChkEl.initElem();
								childChkEl.addElement();
								break;
							case XD_RESULTSET:
								XDResultSet it = (XDResultSet) result;
								try {
									XDValue item = it.nextXDItem(chkEl);
									Document doc = chkEl.getDocument();
									for(int j = 0,
										xmax = childChkEl._xElement.maxOccurs();
										j <= xmax && item != null; j++,
										item = it.nextXDItem(childChkEl)) {
										Node node =
											doc.importNode(item.getElement(), true);
										if (item.getItemId() == XD_RESULTSET) {
											chkEl._iterator=(XDResultSet)result;
										}
										childChkEl.initElem();
										chkEl._element.insertBefore(node, el);
										chkEl.incRefNum();
									}
									chkEl._element.removeChild(el);
								} catch (SRuntimeException ex) {
									chkEl.putReport(ex.getReport());
								}	chkEl._scp.closeResultSet(it);
								break;
							default:
								DefContainer dc = (DefContainer) result;
								Document doc = chkEl.getDocument();
								for (int j = 0, ymax = 0, len=dc.getXDItemsNumber(),
									xmax = childChkEl._xElement.maxOccurs();
									j<len; j++){
									if (ymax++ < xmax) {
										int addr;
										Node node = doc.importNode(
											dc.getXDItem(j).getElement(),
											true);
										childChkEl.initElem();
										chkEl._element.insertBefore(node, el);
										addr = childChkEl._xElement._onStartElement;
										childChkEl.debugXPos(
											XDDebug.ONSTARTELEMENT);
										if (addr >= 0) {
											childChkEl.setElemValue(
												childChkEl._element);
											childChkEl.exec(addr, (byte) 'E');
											childChkEl.copyTemporaryReports();
										}
										childChkEl.debugXPos(XDDebug.FINALLY);
										addr = childChkEl._xElement._finaly;
										if (addr >= 0) {
											childChkEl.setElemValue(
												childChkEl._element);
											childChkEl.exec(addr, (byte) 'E');
											childChkEl.copyTemporaryReports();
										}
										chkEl.incRefNum();
									}
								}	try {
									chkEl._element.removeChild(el);
								} catch (DOMException ex) {} //ignore exception
								break;

						}
					} else { //result here is not null!
						if (result.isNull()) {
							childChkEl.updateElement(null);
							break; // do not continue
						}
						if (childChkEl._sourceElem == null) {
							childChkEl._sourceElem = childChkEl.getElemValue();
							lastElem = childChkEl._sourceElem;

						}
						switch (result.getItemId()) {
							case XD_STRING:
								//create a dummy element with the text child
								String text = result.stringValue();
								if (text != null && !text.isEmpty()) {
									Element el =
										stringToElement(childChkEl, text);
									childChkEl.setElemValue(el);
									composeElement(childChkEl, el);
								} else {//empty string is no value!
									childChkEl.updateElement(null);
								}
								break;
							case XD_BOOLEAN:
								for (int j = 0;;) {
									if (j < childChkEl._xElement.maxOccurs()
										&& result != null
										&& result.booleanValue()) {
										composeElement(childChkEl ,null);
										if (childChkEl._xElement == null
											|| ++j >=
											childChkEl._xElement.maxOccurs()){
											break; // do not continue
										}
										childChkEl = prepareChkElement(chkEl,
											sourceEl, childDef, i);
										result = execComposeElement(
											childChkEl, sourceEl, lastElem);
									} else { //delete this element
										childChkEl.updateElement(null);
										break; // do not continue
									}
								}
								break;
							default:
								composeElement(chkEl, childChkEl, result, xtxt);
								if (childDef._compose < 0
									&& result.getItemId() == XD_CONTAINER
									&& childChkEl.getOccurrence() > 0) {
									// set last processeed item to lastElem
									lastElem = ((DefContainer)result)
										.getXDElement(
											childChkEl.getOccurrence() - 1);
								}
						}
					}
					if (chkEl._selector != null
						&& chkEl._selector._kind == XMCHOICE
						&& count < chkEl.getRefNum(i)
						&& i < chkEl._selector._endIndex) {
						//something created in xd:choice, so skip other variants
						i = chkEl._selector._endIndex;
						continue;
					}
					break;
				}
				case XMTEXT: {
					count = chkEl.getRefNum(index);
					lastText = createTextNode(chkEl,
						sourceEl, savedSource, (XData) xNode, lastText);
					if (chkEl._selector != null
						&& chkEl._selector._kind == XMCHOICE
						&& count < chkEl.getRefNum(i)
						&& i < chkEl._selector._endIndex) {
						//something created in xd:choice, so skip other variants
						i = chkEl._selector._endIndex;
						continue;
					}
					i++;
					continue;
				}
				case XMCHOICE:
				case XMMIXED:
				case XMSEQUENCE: {
					XSelector xsel = (XSelector) xNode;
					XDValue result;
					int addr;
					Object savedUserObj = chkEl.getUserObject();
					chkEl.debugXPos(XDDebug.SELECTORCREATE);
					if ((addr = xsel._compose) >= 0) {
						chkEl.debugXPos(XDDebug.CREATE);
						result = chkEl.exec(addr, (byte) 'E');
					} else {//default
						 // force to create an optional model
						result = new DefLong(xsel.minOccurs() == 0
							? 1 : xsel.minOccurs());
					}
					if (!chkEl.createGroup(xsel)) {
						i = chkEl._nextDefIndex;
						continue;
					}
					if (result == null || result.isNull()
						|| (result.getItemId() == XD_LONG
						&& result.intValue() <= 0)
						|| (result.getItemId() == XD_BOOLEAN
						&& !result.booleanValue())) { //skip the group
						i = groupNotGenerated(chkEl, xsel);
						continue;
					}
					switch (result.getItemId()) {
						case XD_LONG:
							int xnum = (int) ((DefLong) result).intValue();
							for(int j=0; j<xsel.maxOccurs() && j<xnum; j++) {
								composeGroup(chkEl, sourceEl, savedSource,
									i, lastText, lastElem, savedUserObj);
								if (chkEl._selector != null
									&& xsel.getKind() != XMMIXED
									&& chkEl._selector._count>xsel.maxOccurs()){
									//Maximum occurrence of &{0} exceeded
									chkEl.error(XDEF.XDEF558,
										xsel.getName().substring(1));
									break;
								}
							}
							break;
						case XD_BOOLEAN:
							for(int j = 0; j < xsel.maxOccurs(); j++) {
								composeGroup(chkEl, sourceEl, savedSource,
									i, lastText, lastElem, savedUserObj);
								result = chkEl.exec(addr, (byte) 'E');
								chkEl.copyTemporaryReports();
								if (!result.booleanValue()) {
									break;
								}
							}
							break;
						case XD_RESULTSET:
							XDResultSet it = (XDResultSet) result;
							try {
								XDValue item = it.nextXDItem(chkEl);
								if (item == null) {// no raw
									i = groupNotGenerated(chkEl, xsel);
									continue;
								}
								for(int j=0; j<=xsel.maxOccurs() && item!=null;
									item = it.nextXDItem(chkEl), j++) {
									Element el = item.getElement();
									if (el == null) {
										el = prepareElement(
											chkEl, null, chkEl._xElement);
									}
									composeGroup(chkEl, el,
										null,i,lastText,lastElem,savedUserObj);
									if (xsel.getKind() != XMMIXED &&
										j + 1 >= xsel.maxOccurs()) {
										//Maximum occurrence of &{0} exceeded
										chkEl.error(XDEF.XDEF558,
											xsel.getName().substring(1));
									}
								}
							} catch (SRuntimeException ex) {
								chkEl.putReport(ex.getReport());
							}
							chkEl._scp.closeResultSet(it);
							break;
						default:
							DefContainer dc;
							int len;
							dc = result.getItemId()== XD_CONTAINER
								? (DefContainer) result
								: result.getItemId() == XD_ELEMENT
								? new DefContainer(result) : null;
							if (dc == null || (len = dc.getXDItemsNumber()) <= 0
								|| dc.getXDItem(0).getItemId() != XD_ELEMENT) {
								i = groupNotGenerated(chkEl, xsel);
								continue;
							}
							Element el = dc.getXDElement(0);
							int max = xsel.getKind() == XMMIXED
								? len : xsel.maxOccurs();
							for(int j = 0; j <= max && j < len; j++) {
								composeGroup(chkEl,
									el,null,i,lastText, lastElem,savedUserObj);
								if (j + 1 >= dc.getXDItemsNumber()) {
									break;
								}
								el = dc.getXDElement(j + 1);
								if (el != null) {
									if (xsel.getKind() != XMMIXED
										&& j + 1 >= xsel.maxOccurs()) {
										//Maximum occurrence of &{0} exceeded
										chkEl.error(XDEF.XDEF558,
											xsel.getName().substring(1));
									}
								} else {
									break;
								}
							}
					}
					if (chkEl._selector != null) {
						chkEl._selector.updateCounters();
						chkEl.finishSelector();
					}
					chkEl._nextDefIndex = i = xsel._endIndex + 1;
					chkEl._actDefIndex = -1;
					continue;
				}
				case XMSELECTOR_END:
					chkEl._actDefIndex = -1;
					chkEl._nextDefIndex = ++i;
					if (chkEl._selector != null) {
						return i;
					}
					continue; //never should happen
				default:
					i++; //never should happen
					continue;
			}
			chkEl._actDefIndex = -1;
			i++;
			if (chkEl._selector != null) {
				if (chkEl._selector._kind == XMCHOICE) {
					if (count < chkEl.getRefNum(i)) { //node was created?
						if (chkEl._selector._count >
							chkEl._selector.maxOccurs()) {
							i = chkEl._selector._endIndex;
						}
					}
				}
				//othewise force checking of next xdef item
				chkEl._nextDefIndex = i;
			}
		}
		return i;
	}

	/** Compose a group of objects.
	 * @param chkEl where to generate.
	 * @param sourceEl source element.
	 * @param savedSource saved default source element - MUST be null if
	 * set the new element as context.
	 * @param defIndex index to group start, will continue after that.
	 * @param lastTextNode last processed text from source item or null.
	 * @param lastElement last processed element from source item or null.
	 * @param nextDefIndex next index.
	 * @param savedUserObj user object.
	 * @return true if something was generated.
	 */
	private boolean composeGroup(final ChkElement chkEl,
		final Element sourceEl,
		final Element savedSource,
		final int defIndex,
		final Node lastTextNode,
		final Element lastElement,
		final Object savedUserObj) {
		chkEl._actDefIndex = -1;
		int oldnextDefIndex = chkEl._nextDefIndex;
		composeChildNodes(chkEl,
			sourceEl, savedSource, defIndex+1, lastTextNode, lastElement);
		chkEl._nextDefIndex = oldnextDefIndex;
		chkEl.setUserObject(savedUserObj); //reset
		chkEl._actDefIndex = -1;
		if (chkEl._selector != null) {
			chkEl.checkAbsence(chkEl._selector, null, true);
			if (chkEl._selector.saveAndClearCounters()) {
				chkEl._selector._count++;
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/** Creates a new instance of ChkComposer. This constructor is used only
	 * internally when the composer is called from script.
	 * @param reporter temporary reporter from script processor.
	 * @param xdef the XDefinition.
	 * @param nsURI Name space URI of the element.
	 * @param qname Qualified name of the element.
	 * @param chkElem the ChkElement object from which the constructor was
	 * called.
	 */
	private ChkComposer(final ArrayReporter reporter,
		final XDefinition xdef,
		final String nsURI,
		final String qname,
		final ChkElement chkElem) {
		super(reporter);
		ChkDocument chkDoc = new ChkDocument(xdef, chkElem);
		chkDoc.setDebugger(chkElem._rootChkDocument.getDebugger());
		chkDoc.setCreateMode(true);
		_rootChkElement =
			(ChkElement) chkDoc.prepareRootXXElementNS(nsURI, qname, false);
	}

	private Element prepareElement(final ChkElement chkElem,
		final Element sourceElem,
		final XElement xel) {
		String n = xel.getName();
		String u = xel.getNSUri();
		if ("$any".equals(n)) {
			if (sourceElem != null) {
				n = sourceElem.getNodeName();
				u = sourceElem.getNamespaceURI();
			} else {
				n = "_ANY_";
				u = null;
			}
		}
		String s = getDestLexiconName(xel.getXDPosition());
		if (s != null) {
			n = s;
		}
		return chkElem._element.getOwnerDocument().createElementNS(u, n);
	}

	private ChkElement genChkElement(final ChkElement parentChkElem,
		final Element sourceElem,
		final XElement xElem) {
		Element el = prepareElement(parentChkElem, sourceElem, xElem);
		ChkElement chkElem =  new ChkElement(parentChkElem, el, xElem, false);
		chkElem._userObject = parentChkElem._userObject;
		return chkElem;
	}

	private ChkElement prepareChkElement(final ChkElement parentChkElem,
		final Element sourceElem,
		final XElement xElem,
		final int defIndex) {
		ChkElement childChkEl = genChkElement(parentChkElem, sourceElem, xElem);
		childChkEl._sourceElem = sourceElem == null ?
			parentChkElem._sourceElem : sourceElem;
		parentChkElem._actDefIndex = defIndex;
		if (parentChkElem._selector != null) {
			if (parentChkElem._selector._kind == XMCHOICE) {
				parentChkElem._nextDefIndex = parentChkElem._selector._endIndex;
			} else {
				parentChkElem._nextDefIndex = defIndex + 1;
			}
			parentChkElem._selector._occur = true;
		} else {
			parentChkElem._nextDefIndex = defIndex + 1;
		}
		return childChkEl;
	}

////////////////////////////////////////////////////////////////////////////////
// methods called ONLY internally from X-script processor!
///////////////////////////////////////////////////////////////////////////////

	/** Returns the available element model represented by given name or
	 * <i>null</i> if definition item is not available.
	 * @param xdef XDefinition.
	 * @param key The name of definition item used for search.
	 * @return The required X-element or null.
	 */
	private static XElement getXElement(final XDefinition xdef,
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
		for (XMElement xel: def.getModels()) {
			if (lockey.equals(xel.getName())) {
				return (XElement) xel;
			}
		}
		return null;
	}

	/** Compose the XML element from the source XML element. The name
	 * of root element of result is given by parameter.
	 * NOTE This method is called ONLY internally from script processor!
	 * @param reporter array report writer.
	 * @param xdef XDefinition.
	 * @param rootName name of root element.
	 * @param chkElem the ChkElement object from which this was called.
	 * @return composed XML element.
	 * @throws SRuntimeException if an error occurs.
	 */
	static Element compose(final ArrayReporter reporter,
		final XDefinition xdef,
		final String rootName,
		final ChkElement chkElem) {
		String qname = null;
		String nsURI = null;
		Element sourceElem = chkElem._element;
		if (rootName == null) {
			if (sourceElem == null) {
				XElement xe;
				if ((xe = getXElement(xdef, xdef.getName())) != null) {
					qname = xdef.getName();
					nsURI = xe.getNSUri();
				}
			} else {
				qname = sourceElem.getTagName();
				nsURI = sourceElem.getNamespaceURI();
			}
		} else {
			XElement xe;
			if ((xe = getXElement(xdef, rootName)) != null) {
				qname = rootName;
				nsURI = xe.getNSUri();
			} else {
				//Model of element &{0} is missing in X-definition &{1}
				chkElem.error(XDEF.XDEF601, rootName, xdef.getName());
				return null;
			}
		}
		ChkComposer cc = new ChkComposer(reporter, xdef, nsURI, qname, chkElem);
		cc._rootChkElement._scp.setDebug(chkElem._scp.isDebugMode());
		cc._rootChkElement._scp.setDebugger(
			chkElem._rootChkDocument.getDebugger());
		cc.composeRoot(sourceElem);
		if (cc.errorWarnings()) {
			if (reporter == null) {
				cc.checkAndThrowErrors();
			} else {
				ArrayReporter ar = (ArrayReporter) cc.getReportWriter();
				if (ar != reporter) { // do not write to the same reporter!
					reporter.addReports(ar); // copy reportes
				}
			}
		}
		Element result;
		if ((result = cc._rootChkElement.getElement()) != null) {
			KXmlUtils.setNecessaryXmlnsAttrs(result);
		}
		return result;
	}
}
