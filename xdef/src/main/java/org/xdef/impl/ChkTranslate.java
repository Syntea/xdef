package org.xdef.impl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.XDBytes;
import org.xdef.XDContainer;
import org.xdef.XDDebug;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefSQLConstructor;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.msg.XDEF;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SError;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;

/** Translates element from one language to another language
 * according to Lexicon.
 * @author Vaclav Trojan
 */
final class ChkTranslate extends SReporter implements XDValueID {

	/** Root check element. */
	private ChkElement _rootChkElement;
	/** Lexicon of tag names in different languages.*/
	private XDLexicon _lexicon = null;
	/** XDLexicon source language ID.*/
	private int _sourceLanguageID = -1;
	/** XDLexicon destination language ID.*/
	private int _destLanguageID = -1;

	/** Creates a new instance of ChkTranslate. */
	ChkTranslate(final ReportWriter reporter) {super(reporter);}

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
							if (v.getItemId() == XD_CONTAINER) {
								XDContainer xv = (XDContainer) v;
								if (xv.getXDNamedItemsNumber() > 0) {
									for (XDNamedValue x: xv.getXDNamedItems()){
										if (x.getValue() != null
											&& !x.getValue().isNull()) {
											e.setAttribute(x.getName(),
												x.getValue().stringValue());
										}
									}
								}
							} else {
								e.setAttribute(nv.getName(), v.toString());
							}
						}
					}
				} catch (Exception ex) {}
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
		if (item != null && !item.isNull()
			&& item.getItemId() == XD_PARSERESULT) {
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
				Element el;
				if (xdc.getXDNamedItemsNumber() == 0
					&& xdc.getXDItemsNumber() > 1) {
					el = xdc.toElement(chkel.getXXNSURI(), chkel.getXXName());
				} else {
					el = ((XDContainer) item).toElement(
						chkel.getXXNSURI(), chkel.getXXName());
				}
				XDContainer x = (XDContainer) item;
				return new DefElement(attrsToElement(el, x));
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
								if (val.getItemId() == XD_ELEMENT) {
									el.addXDItem(val.getElement());
								} else if (val.getItemId() == XD_CONTAINER) {
									XDContainer c = (XDContainer) val;
									el.addXDItem(c.toElement(null, s));
								} else {
									el.getElement().setAttribute(att.getName(),
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
	 * @param inputElement input element.
	 * @param qname qualified name of result element.
	 */
	final void xtranslate(final ChkDocument chkDoc,
		final Element inElem,
		final String sourceLanguage,
		final String destLanguage) {
		XElement oldXElement = chkDoc._xElement; //Save XElement
		boolean oldMode = chkDoc.isCreateMode();
		Element oldContext = chkDoc._sourceElem;
		int oldSourceLanguageID = chkDoc._sourceLanguageID;
		int oldDestLanguageID = chkDoc._destLanguageID;
		_lexicon = ((XPool) chkDoc.getXDPool())._lexicon;
		if (_lexicon == null) {
			//Lexicon is not available in the XDPool object
			throw new SRuntimeException(XDEF.XDEF143);
		}
		chkDoc.setCreateMode(true);
		chkDoc._xElement = null;
		setReportWriter(chkDoc.getReportWriter());
		try {
			chkDoc.setLexiconLanguage(sourceLanguage);
			_sourceLanguageID = chkDoc._sourceLanguageID;
			chkDoc.setDestLexiconLanguage(destLanguage);
			_destLanguageID = chkDoc._destLanguageID;
			String nsuri = inElem.getNamespaceURI();
			XElement xel = chkDoc.getXElement(inElem.getTagName(),
				nsuri, _sourceLanguageID);
			String name = ((XPool) chkDoc.getXDPool())._lexicon.findText(
				xel.getXDPosition(), _destLanguageID);
			chkDoc.setXDContext(inElem);
			_rootChkElement = (ChkElement) chkDoc.prepareRootXXElementNS(
				nsuri, name, false);
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
			chkDoc._sourceElem = oldContext;
			chkDoc._sourceLanguageID = oldSourceLanguageID;
			chkDoc._destLanguageID = oldDestLanguageID;
		}
	}

	/** Compose the XML element from the source XML element. The name
	 * of root element of result is given by parameter. For construction is used
	 * the X-definition specified by ChkTranslate constructor.
	 * @param sourceElem The element with source data.
	 */
	private void composeRoot(final Element sourceElem) {
		try {
			_rootChkElement.getXDDocument().setXDContext(sourceElem);
			XElement xElement = _rootChkElement._xElement;
			XDValue resultExpr = createDefaultContext(xElement, sourceElem);
			composeElement(null, _rootChkElement, resultExpr);
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
				throw new SRuntimeException(e.getReport(), e.getCause());
			}
		}
	}

	private void composeElement(final ChkElement parentChkElem,
		final ChkElement chkElement,
		final XDValue result) {
		if (result == null || result.isNull()) {
			chkElement.updateElement(null); // no source - delete this element
			return;
		}
		XElement xElem = chkElement._xElement;
		DefContainer xdc;
		switch (result.getItemId()) {
			case XD_ELEMENT: {
				Element el = result.getElement();
				chkElement.setElemValue(el);
				if (el != null) {
					createElement(chkElement, el);
				} else {
					chkElement.updateElement(null); // delete this element
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
						if (item.getItemId() == XD_ELEMENT) {
							if (n >= xElem.maxOccurs()) {
								//Too many iterator items for &{0}
								chkElem.error(XDEF.XDEF565, xElem.getName());
								chkElem._scp.closeResultSet(it);
								return;
							}
							Element el = item.getElement();
							if (el != null) {
								chkElem.setElemValue(el);
								createElement(chkElem, el);
								n++;
								continue;
							}
						} else if (item.getItemId() == XD_RESULTSET) {
							if (n >= xElem.maxOccurs()) {
								//Too many iterator items for &{0}
								chkElem.error(XDEF.XDEF565, xElem.getName());
								chkElem._scp.closeResultSet(it);
								return;
							}
							Element el = item.getElement();
							if (el != null) {
								chkElem.setElemValue(el);
								createElement(chkElem, el);
								n++;
								continue;
							}
						} else if (item.getItemId() == XD_STRING) {
							if (!item.stringValue().isEmpty()) {
								Element el = it.getElement();
								if (el != null) {
									chkElem.setElemValue(el);
									createElement(chkElem, el);
									n++;
									continue;
								}
							}
						}
						chkElement.updateElement(null);
					}
					chkElem._scp.closeResultSet(chkElem._iterator);
				} catch (SRuntimeException ex) {
					Report r = ex.getReport();
					chkElem.error(r.getMsgID(),r.getText(),r.getModification());
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
					createElement(chkElement, null);
				} else {
					chkElement.updateElement(null); //delete this element
				}
				return;
			case XD_INT:
				if (result.longValue() > 0) {
					createElement(chkElement, null);
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
				createElement(chkElement, xdc.toElement(null, null));
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
			createElement(chkElem, val.getElement());
		}
		if (n == 0) {
			chkElem.updateElement(null);//no source; delete this element
		}
	}

	/** Find elements with given name in child nodes. If the argument "elem" has
	 * the name and namespace URI as required then add this element to result
	 * and return true. Otherwise try to find direct child nodes with such name
	 * and namespace URI, add them to result and return true. If nothing was
	 * found then return false. Note this is very NASTY trick!
	 * @param result Context where found elements are added.
	 * @param chkEl actual object ChkElement.
	 * @param elem where ti search.
	 * @return true if an element was found and all found elements are added
	 * to the context.
	 */
	private boolean getChildElementsByName(final DefContainer result,
		final ChkElement chkEl,
		final Element elem) {
		String uri = chkEl.getXMElement().getNSUri();
		String qname = _lexicon.findText(chkEl.getXMElement().getXDPosition(),
			_sourceLanguageID);
		if (qname == null) {
			qname = chkEl.getXMElement().getName();
		}
		int n = qname.indexOf(':');
		String localName = n < 0 ? qname : qname.substring(n + 1);
		String lName = elem.getLocalName();
		if (lName == null) {
			lName = elem.getNodeName();
		}
		if (localName.equals(lName)) {
			if (uri == null || uri.equals(elem.getNamespaceURI())) {
				result.addXDItem(new DefElement(elem));
				return true; //element is the element itself
			}
		}
		NodeList nl = elem.getChildNodes();
		if ((n = nl.getLength()) == 0) {
			return false; //nothing found
		}
		int max = chkEl.getXMElement().maxOccurs();
		int m = 0;
		for (int i = 0; i < n; i++) {
			Node node = nl.item(i);
			lName = node.getLocalName();
			if (lName == null) {
				lName = node.getNodeName();
			}
			if ("_".equals(lName) && node.getNamespaceURI() == null
				|| (localName.equals(lName)
				&& ((uri == null || uri.equals(node.getNamespaceURI()))))) {
				result.addXDItem(new DefElement((Element) node));
				if (++m >= max) {//the element added
					break; // we do not nead others
				}
			}
		}
		return m > 0; // found some elements (added to result context).
	}

	/** Execute "compose" action.
	 * @param chkEl The actual check element.
	 * @param sourceElem The source element from which the result is composed.
	 * @return The XDValue object or <tt>null</tt>.
	 */
	private XDValue execComposeElement(final ChkElement chkEl,
		final Element sourceElem) {
		chkEl.debugXPos(XDDebug.CREATE);
		if (sourceElem == null) {
			if (chkEl._xElement.minOccurs() <= 0) {
				return null;
			}
			String qname = "$any".equals(chkEl._xElement.getName()) ? "_ANY_" :
				chkEl._xElement.getName();
			DefElement result = new DefElement(
				chkEl._sourceElem = KXmlUtils.newDocument(
				chkEl._xElement.getNSUri(), qname, null).getDocumentElement());
			return result;
		}
		//default contex (no script specified)
		DefContainer result = new DefContainer();
		if ("$any".equals(chkEl._xElement.getName())) { //any element
			NodeList nl = sourceElem.getChildNodes();
			for (int i = 0, max = nl.getLength(); i < max; i++) {
				Node node = nl.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					result.addXDItem(new DefElement((Element) node));
				}
			}
		} else {//create result with child nodes found by name; nasty trick!!!
			getChildElementsByName(result, chkEl, sourceElem);
		}
		Element el = result.getXDElement(0);
		//if somethig found, let's set first element as source context,
		//otherwise set source from the argument.
		chkEl._sourceElem = el != null ? el : sourceElem;
		if (el == null ||
			result.getXDItemsNumber() < chkEl._xElement.minOccurs()) {
			if (chkEl._parent != null
				&& chkEl._parent.getItemId() != XX_DOCUMENT
				&& ((ChkElement) chkEl._parent)._selector != null
				&& ((ChkElement) chkEl._parent)._selector._kind
					!= XMNode.XMSEQUENCE) {
				return result; //if choice or mixed we do not create dummy items
			}
			//we create necessary number of items
			for (int i=result.getXDItemsNumber();
				i<chkEl._xElement.minOccurs(); i++) {
				result.addXDItem(new DefElement(sourceElem));
			}
		}
		return result;
	}


	private void createElement(final ChkElement chkElem,
		final Element sourceElem) {
		XElement xel = chkElem._xElement;
		Element savedSource =  chkElem._sourceElem; //save source element
		chkElem._sourceElem = sourceElem;
		// create attributes
		String xpos = chkElem._xPos;
		if (chkElem._parent._parent != null) { // not root
			chkElem.initElem();
		}
		XData[] xattrs = xel.getXDAttrs();
		chkElem.setXXType((byte) 'A');
		for (XData xatr: xattrs) {
			chkElem._xdata = xatr;
			String attrName = xatr.getName();
			if (attrName.charAt(0) == '$') { //special XDEF attribute
				continue; // skip xd:text etc
			}
			String attrName1 = _lexicon.findText(xatr.getXDPosition(),
				_sourceLanguageID);
			if (attrName1 != null) {
				attrName = attrName1;
			}
			chkElem._xPos = xpos + "/@" + attrName;
			chkElem.debugXPos(XDDebug.CREATE);
			//no source action, try to find attribute in source element.
			if (sourceElem == null || !sourceElem.hasAttribute(attrName)) {
				chkElem.setTextValue(null);
			} else {
				chkElem.setTextValue(sourceElem.getAttribute(attrName));
			}
			String s = chkElem.getTextValue();
			if (s != null) {
				if (!s.isEmpty() || xatr._ignoreEmptyAttributes != 0 // set attr
					|| xatr._ignoreEmptyAttributes == 0
					&& xel._ignoreEmptyAttributes != 0
					||  xel._ignoreEmptyAttributes == 0
					&& chkElem._rootChkDocument._ignoreEmptyAttributes != 0) {
					// set attribute
					String newName = _lexicon.findText(xatr.getXDPosition(),
						_destLanguageID);
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
		XData xtxt = xel.getDefAttr("$text", -1);
		chkElem._xdata = xtxt;
		createChildNodes(chkElem, sourceElem, savedSource, 0, xtxt, null);
		chkElem._sourceElem = sourceElem; //set source
		if ((xtxt = xel.getDefAttr("$textcontent", -1)) != null) {
			chkElem._xdata = xtxt;
			chkElem.setXXType((byte) 'T');
			chkElem._xPos = xpos + "/text()";
			chkElem.debugXPos(XDDebug.CREATE);
			if (sourceElem != null) {//default;
				String s = KXmlUtils.getTextContent(sourceElem).trim();
				chkElem.setTextValue(!s.isEmpty() ? s : null);
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
			if (xn.getKind() == XNode.XMELEMENT) {
				chkElem.chkElementAbsence(i, (XElement) xn, null);
			} else if (xn.getKind() == XNode.XMTEXT) {
				chkElem.chkTextAbsence(i, (XData) xn, false, null);
			} else if (xn.getKind() == XNode.XMSEQUENCE
				|| xn.getKind() == XNode.XMCHOICE
				|| xn.getKind() == XNode.XMMIXED) {
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
	 * @param lastNode last processed text from source item or <tt>null</tt>.
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

	/** Create child nodes. This is very nasty code, should be written better!
	 * However, it works somehow.
	 * @param chkElem actual ChkElement object.
	 * @param sourceEl actual source element.
	 * @param savedSource saved source element.
	 * @param index index to defList.
	 * @param xtxt xd:text item.
	 * @param lastNode last processed text from source item or <tt>null</tt>.
	 */
	private int createChildNodes(final ChkElement chkEl,
		final Element sourceEl,
		final Element savedSource,
		final int index,
		final XData xtxt,
		Node lastNode) {
		int i = index;
		chkEl.setElemValue(sourceEl);
		XNode xNode;
		int count;
		while((xNode = chkEl.getDefElement(i)) != null) {
			switch (xNode.getKind()) {
				case XNode.XMELEMENT: {
					if (xtxt != null && chkEl._element != null) {
						Node n = chkEl._element.getLastChild();
						if (n == null || n.getNodeType() != Node.TEXT_NODE
							&& n.getNodeType() != Node.CDATA_SECTION_NODE) {
							lastNode = createTextNode(
								chkEl,sourceEl,savedSource,xtxt,lastNode);
						}
					}
					XElement childDef = (XElement) xNode;
					chkEl._sourceElem = savedSource;
					count = chkEl.getRefNum(i);
					ChkElement childChkEl =
						prepareChkElement(chkEl, null, childDef, i);
					XDValue result;
					result = execComposeElement(childChkEl, sourceEl);
					if (childChkEl._xElement._compose < 0 &&
						chkEl._sourceElem != null) {
						//propagate explicit source context
						if (!equalNames(childChkEl._xElement,
							childChkEl._sourceElem)) {
							childChkEl._sourceElem = chkEl._sourceElem;
						}
					}
					if (result == null || result.isNull()
						|| (result.getItemId() == XD_INT
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
						if (result.getItemId() == XD_ELEMENT) {
							chkEl._element.replaceChild(
								result.getElement(), el);
							childChkEl._sourceElem = chkEl._sourceElem;
							childChkEl.initElem();
							childChkEl.addElement();
						} else if (result.getItemId() == XD_RESULTSET) {
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
								Report r = ex.getReport();
								chkEl.error(r.getMsgID(), r.getText(),
									r.getModification());
							}
							chkEl._scp.closeResultSet(it);
						} else {
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
							}
							try {
								chkEl._element.removeChild(el);
							} catch (Exception ex) {} //??? ignore exception
						}
					} else { //result here is not null!
						if (result.isNull()) {
							childChkEl.updateElement(null);
							break; // do not continue
						}
						if (childChkEl._sourceElem == null) {
							childChkEl._sourceElem = childChkEl.getElemValue();
						}
						if (result.getItemId() == XD_STRING) {
							//we create a dummy element with the text child
							String text = result.stringValue();
							if (text != null && !text.isEmpty()) {
								Element el = stringToElement(childChkEl, text);
								childChkEl.setElemValue(el);
								createElement(childChkEl, el);
							} else {//empty string is no value!
								childChkEl.updateElement(null);
							}
						} else if (result.getItemId() == XD_BOOLEAN) {
							for (int j = 0;;) {
								if (j < childChkEl._xElement.maxOccurs()
									&& result != null && result.booleanValue()){
									createElement(childChkEl ,null);
									if (childChkEl._xElement == null ||
										++j>=childChkEl._xElement.maxOccurs()){
										break; // do not continue
									}
									childChkEl = prepareChkElement(chkEl,
										sourceEl, childDef, i);
									result = execComposeElement(childChkEl,
										sourceEl);
								} else { //delete this element
									childChkEl.updateElement(null);
									break; // do not continue
								}
							}
						} else {
							composeElement(chkEl, childChkEl, result); //2222
						}
					}
					if (xtxt != null) {
						lastNode = createTextNode(chkEl,
							sourceEl, savedSource, xtxt, lastNode);
					}
					if (chkEl._selector != null
						&& chkEl._selector._kind == XNode.XMCHOICE
						&& count < chkEl.getRefNum(i)
						&& i < chkEl._selector._endIndex) {
						//something created in xd:choice, so we skip the
						//other variants
						i = chkEl._selector._endIndex;
						continue;
					}
					break;
				}
				case XNode.XMTEXT: {
					count = chkEl.getRefNum(index);
					createTextNode(chkEl,
						sourceEl, savedSource, (XData) xNode, lastNode);
					if (chkEl._selector != null
						&& chkEl._selector._kind == XNode.XMCHOICE
						&& count < chkEl.getRefNum(i)
						&& i < chkEl._selector._endIndex) {
						//something created in xd:choice, so we skip the
						//other variants
						i = chkEl._selector._endIndex;
						continue;
					}
					i++;
					continue;
				}
				case XNode.XMCHOICE:
				case XNode.XMMIXED:
				case XNode.XMSEQUENCE: {
					XSelector xsel = (XSelector) xNode;
					XDValue result;
					int addr;
					Object savedUserObj = chkEl.getUserObject();
					chkEl.debugXPos(XDDebug.SELECTORCREATE);
					if ((addr = xsel._compose) >= 0) {
						chkEl.debugXPos(XDDebug.CREATE);
						result = chkEl.exec(addr, (byte) 'E');
					} else {//default
						 // we force to create an optional model
						result = new DefLong(xsel.minOccurs() == 0
							? 1 : xsel.minOccurs());
					}
					if (!chkEl.createGroup(xsel)) {
						i = chkEl._nextDefIndex;
						continue;
					}
					if (result == null || result.isNull()
						|| (result.getItemId() == XD_INT
						&& result.intValue() <= 0)
						|| (result.getItemId() == XD_BOOLEAN
						&& !result.booleanValue())) { //skip the group
						i = groupNotGenerated(chkEl, xsel);
						continue;
					} else if (result.getItemId() == XD_INT) {
						int xnum = (int) ((DefLong) result).intValue();
						for(int j = 0; j < xsel.maxOccurs() && j < xnum; j++) {
							createGroup(chkEl,xtxt, sourceEl,
								savedSource, i, lastNode, savedUserObj);
							if (chkEl._selector != null
								&& xsel.getKind() != XNode.XMMIXED
								&& chkEl._selector._count > xsel.maxOccurs()) {
								//Maximum occurrence of &{0} exceeded
								chkEl.error(XDEF.XDEF558,
									xsel.getName().substring(1));
								break;
							}
						}
					} else if (result.getItemId() == XD_BOOLEAN) {
						for(int j = 0; j < xsel.maxOccurs(); j++) {
							createGroup(chkEl,xtxt, sourceEl,
								savedSource, i, lastNode, savedUserObj);
							result = chkEl.exec(addr, (byte) 'E');
							chkEl.copyTemporaryReports();
							if (!result.booleanValue()) {
								break;
							}
						}
					} else if (result.getItemId() == XD_RESULTSET) {
						XDResultSet it = (XDResultSet) result;
						try {
							XDValue item = it.nextXDItem(chkEl);
							if (item == null) {// no raw
								i = groupNotGenerated(chkEl, xsel);
								continue;
							}
							for(int j = 0; j <= xsel.maxOccurs() && item!=null;
								item = it.nextXDItem(chkEl), j++) {
								Element el = item.getElement();
								if (el == null) {
									el = prepareElement(chkEl,
										null, chkEl._xElement);
								}
								createGroup(chkEl, xtxt, el,
									null, i, lastNode, savedUserObj);
								if (xsel.getKind() != XNode.XMMIXED &&
									j + 1 >= xsel.maxOccurs()) {
									//Maximum occurrence of &{0} exceeded
									chkEl.error(XDEF.XDEF558,
										xsel.getName().substring(1));
								}
							}
						} catch (SRuntimeException ex) {
							Report r = ex.getReport();
							chkEl.error(r.getMsgID(),
								r.getText(), r.getModification());
						}
						chkEl._scp.closeResultSet(it);
					} else {
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
						int max = xsel.getKind() == XNode.XMMIXED ?
							len : xsel.maxOccurs();
						for(int j = 0; j <= max && j < len; j++) {
							createGroup(chkEl,xtxt,
								el, null, i, lastNode, savedUserObj);
							if (j + 1 >= dc.getXDItemsNumber()) {
								break;
							}
							el = dc.getXDElement(j + 1);
							if (el != null) {
								if (xsel.getKind() != XNode.XMMIXED
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
				case XNode.XMSELECTOR_END:
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
				if (chkEl._selector._kind == XNode.XMCHOICE) {
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

	/** Create a group of objects.
	 * @param chkEl where to generate.
	 * @param xtxt x-data.
	 * @param sourceEl source element.
	 * @param savedSource saved default source element - MUST be null if
	 * we set the new element as context.
	 * @param defIndex index to group start, will continue after that.
	 * @param lastNode last processed text from source item or <tt>null</tt>.
	 * @param nextDefIndex next index.
	 * @param savedUserObj user object.
	 * @return true if something was generated.
	 */
	private boolean createGroup(final ChkElement chkEl,
		final XData xtxt,
		final Element sourceEl,
		final Element savedSource,
		final int defIndex,
		final Node lastNode,
		final Object savedUserObj) {
		chkEl._actDefIndex = -1;
		int oldnextDefIndex = chkEl._nextDefIndex;
		createChildNodes(chkEl,
			sourceEl, savedSource, defIndex + 1, xtxt, lastNode);
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

	private XDValue createDefaultContext(final XElement xElem,
		final Element sourceEl) {
		if (xElem.minOccurs() <= 0) {
			return null;
		}
		if (sourceEl == null) {
			return new DefElement(
				KXmlUtils.newDocument().createElementNS(xElem.getNSUri(),
				"$any".equals(xElem.getName()) ? "_ANY_" : xElem.getName()));
		}
		return new DefElement(sourceEl);
	}

	/** Creates a new instance of ChkTranslate. This constructor is used only
	 * internally when the composer is called from script.
	 * @param reporter temporary reporter from script processor.
	 * @param xdef the XDefinition.
	 * @param nsURI Name space URI of the element.
	 * @param qname Qualified name of the element.
	 * @param chkElem the ChkElement object from which the constructor was
	 * called.
	 */
	private ChkTranslate(final ArrayReporter reporter,
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
		String s = _lexicon.findText(xel.getXDPosition(), _destLanguageID);
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
			if (parentChkElem._selector._kind == XNode.XMCHOICE) {
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
		ChkTranslate cc = new ChkTranslate(reporter, xdef, nsURI, qname, chkElem);
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
					Report rep;
					while ((rep = ar.getReport()) != null) {
						reporter.putReport(rep);
					}
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