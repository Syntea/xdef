package org.xdef.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xdef.XDDebug;
import org.xdef.XDParseResult;
import org.xdef.XDUniqueSetKey;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ATTR;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XX_ATTR;
import static org.xdef.XDValueID.XX_ELEMENT;
import static org.xdef.XDValueID.XX_TEXT;
import static org.xdef.XDValueID.X_UNIQUESET;
import static org.xdef.XDValueID.X_UNIQUESET_M;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.XXATTR;
import static org.xdef.XDValueType.XXELEMENT;
import static org.xdef.XDValueType.XXTEXT;
import org.xdef.component.XComponent;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.xml.KNamespace;
import org.xdef.xon.XonTools;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.xml.KXmlUtils;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;

/** Provides validation of input data or it can be used as base for construction of XML objects according to
 * X-definition.  This code is nasty code in some parts, should be written better!
 * @author Vaclav Trojan
 */
public final class ChkElement extends ChkNode implements XXElement, XXData {
	/** The set containing marked unique sets. */
	final Set<CodeUniqueset> _markedUniqueSets = new HashSet<>();
	/** Switch if the actual reporter is cleared on invoked action. */
	final private boolean _clearReports;
	/** Array of bound keys.*/
	XDUniqueSetKey[] _boundKeys;
	/** Model of the processed data object.*/
	XMData _xdata;
	/** XON Map. */
	Map<String, Object> _xonMap;
	/** XON Array. */
	List<Object> _xonArray;
	/** XON item name. */
	String _xonKey;
	/** XON item value. */
	Object _xonValue;
	/** Index to actual X-definition. */
	int _actDefIndex;
	/** Index to next X-definition. */
	int _nextDefIndex;
	/** Text value of actual text node or attribute. It is used for communication with X-script interpreter.
	 * Important note: it should be cleared after invocation of external methods - to allow gc to do the job!
	 */
	private String _data;
	/** Element value used for X-script code. Important note: it should be cleared after invocation of
	 * external methods - to allow garbage collector to do the job!
	 */
	private Element _elemValue;
	/** Name of actually processed attribute for communication with the X-script interpreter. */
	private String _attName;
	/** Namespace URI of actually processed attribute for communication with  the X-script interpreter. */
	private String _attURI;
	/** List of names of attributes. */
	private Set<String> _attNames;
	/** Map with child XPath occurrences. */
	private final Map<String, XPosInfo> _xPosOccur;
	/** Array of child nodes. */
	private XNode[] _childList;
	/** Array of occurrence counters. */
	private int[] _counters;
	/** Number of text nodes found. */
	private int _numText;
	/** Flag this element should be forgotten. */
	private boolean _forget;
	/** If true ignore this element and all nested nodes. */
	private boolean _ignoreAll;
	/** Actual selector. */
	SelectorState _selector;
	/** If true the element was set to nil. */
	private boolean _nil;
	/** List of child check elements. */
	List<ChkElement> _chkChildNodes;
	/** If true the element attributes had been checked. */
	private boolean _attsChecked;
	 /** mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined. */
	private byte _mode;
	/** XComponent if exists or null. */
	private XComponent _xComponent;

	/** Create a new empty instance of ChkElement - just for internal use.
	 * @param xelem XElement from X-definition.
	 * @param parent ChkNode parent.
	 * @param element element with attributes.
	 * @param ignoreAll if true ignore this and all child nodes.
	 */
	ChkElement(final ChkNode parent,
		Element element,
		final XElement xelem,
		boolean ignoreAll) {
		super(element==null ? xelem.getName(): element.getNodeName(),parent);
		_clearReports = xelem._clearReports != 0
			? xelem._clearReports == (byte) 'T' : xelem.getXDPool().isClearReports();
		_element = element;
		_ignoreAll = ignoreAll || xelem.isIgnore() || xelem.isIllegal();
		if (xelem.isIgnore() || xelem.isIllegal()) {
			_forget = true;
		}
		_xElement = xelem;
		_xPosOccur = new LinkedHashMap<>();
		_xPos = _parent.getXPos() + '/' + _name;
		if (_parent.getParent() != null) {
			int xPosCnt = getElemXPos(((ChkElement)_parent)._xPosOccur, _xPos);
			_xPos += '[' + String.valueOf(xPosCnt)+ ']';
		}
		_errCount=getReporter().getErrorCount()+_scp._reporter.getErrorCount();
		_childList = _xElement._childNodes;
		_actDefIndex = -1; //index of actual X-definition
		_counters = new int[_childList.length + 1]; //one more for '*'
		_chkChildNodes = new ArrayList<>();
		_attNames = new HashSet<>();
		if (ignoreAll) {
			_element = null;
		} else if (_element != null) {
			Element el;
			if (_parent._parent == null) { //root element
				el = _rootChkDocument._doc.getDocumentElement();
				if (el == null) {
					_rootChkDocument._doc.appendChild(_element);
				} else if (el != _element) {
					el.appendChild(_element);
				}
			} else if ((el = _parent.getElement()) != null) {
				el.appendChild(_element);
			}
			if (_xElement._nillable == 'T'
				&& "true".equals(_element.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,"nil"))){
				_nil = true;
			}
		}
		if (!_ignoreAll && _element != null) {
			if (_xElement._xon > 0) { //XON
				if (_element.hasAttribute(X_KEYATTR)) {
					_xonKey = XonTools.xmlToJName(_element.getAttribute(X_KEYATTR));
				}
				if (X_MAP.equals(_element.getLocalName())) {
					_xonMap = new LinkedHashMap<>();
				} else if (X_ARRAY.equals(_element.getLocalName())) {
					_xonArray = new ArrayList<>();
				}
			}
			if ((_xComponent = _parent.getXComponent()) != null) {// X-component
				_xComponent = _xComponent.xCreateXChild(this);
			}
		}
	}

	/** Execute X-script from given address (with given type).
	 * @param addr address of script.
	 * @param type type of model ('E' - element, 'A' - attribute, 'T' text, otherwise 'U').
	 */
	final XDValue exec(final int addr, final byte type) {
		_mode = type;
		return addr < 0 ? null : _scp.exec(addr, this);
	}

	 /** Set mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined. */
	final void setXXType(final byte mode) {_mode = mode;}

	 /** Get mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined.
	 * @return mode.
	 */
	public final byte getXXType() {return _mode;}

	/** Set name of the actually processed attribute or Text node.
	 * @param name The name of the actually processed attribute or text node.
	 */
	final void setProcessedAttrName(final String name) {_attName = name;}

	/** Set name of the actually processed attribute or Text node.
	 * @param name The name of the actually processed attribute or text node.
	 */
	final void setProcessedAttrURI(final String uri) {_attURI = uri;}

	/** Get offset of model variable.
	 * @param name name of model variable.
	 * @return offset of model variable or -1 if variable is not declared.
	 */
	private int findModelVariableOffset(final String name) {
		if (_xElement._vartable == null) {
			return -1;
		}
		XVariable v = _xElement._vartable.getXVariable(name);
		return v == null || !v.isInitialized() ? -1 : v.getOffset();
	}

	/** Create check element object.
	 * @param element The element with attributes.
	 * @return created check element object.
	 */
	final ChkElement createChkElement(final Element element) {
		_data = null;
		_parseResult = null;
		if (_nil) {
			error(XDEF.XDEF501, element.getNodeName()); //Not allowed element '&{0}'
			ChkElement result = new ChkElement(this, element, _xElement.createAnyDefElement(), true);
			_chkChildNodes.add(result);
			return result;
		}
		boolean appended = false;
		Element el;
		if ((el = getElement()) != null) {
			//let's append it for a while to be able to execute XPath
			el.appendChild(element);
			appended = true;
		}
		int nextDefIndex = _nextDefIndex; //save index for moreElement case
		int actDefIndex = _actDefIndex;
		ChkElement result = (ChkElement) findXNode(element);
		XMData textcontent =_xElement.getDefAttr("$textcontent",-1);
		if (appended && result == null && textcontent == null) {
			el.removeChild(element);
		} else if (result != null && (result.getXMElement().isIgnore() || result.getXMElement().isIllegal())){
			if (result.getXMElement().isIllegal()) {
				putTemporaryReport(Report.error(XDEF.XDEF557, element.getNodeName()));//Illegal element '&{0}'
				if (_xElement._onIllegalElement >= 0) {
					if (_clearReports) {
						clearTemporaryReporter();
					}
					exec(_xElement._onIllegalElement, (byte) 'E');
					copyTemporaryReports();
				}
			}
			result = new ChkElement(this, el, _xElement.createAnyDefElement(), true);
			if (el != null) {
				el.removeChild(element);
			}
		}
		if (result == null) {
			if (_xElement._moreElements!='T' && _xElement._moreElements!='I') {
				// Illegal element
				debugXPos(XDDebug.ONILLEGALELEMENT);
				if (_xElement._onIllegalElement >= 0) {
					_elemValue = _element;
					//Not allowed element '&{0}'
					putTemporaryReport(Report.error(XDEF.XDEF501, element.getNodeName()));
					if (_clearReports) {
						clearTemporaryReporter();
					}
					exec(_xElement._onIllegalElement, (byte) 'E');
					copyTemporaryReports();
				} else if (textcontent == null) {
					if (_xElement._xon > 0) {
						Node n = element.getAttributeNode(X_KEYATTR);
						//Not allowed item&{1}{ "}{"} in &{0}
						error(XDEF.XDEF507, _xElement.getLocalName(), n==null ? null : n.getNodeValue());
					} else {
						error(XDEF.XDEF501, element.getNodeName()); //Not allowed element '&{0}'
					}
				}
				result = new ChkElement(this, element, _xElement.createAnyDefElement(), true);
			} else {//moreElements || ignoreOther
				_nextDefIndex = nextDefIndex;
				_actDefIndex = actDefIndex;
				result = new ChkElement(this,
					element, _xElement.createAnyDefElement(), _ignoreAll || _xElement._moreElements == 'I');
			}
		}
		_chkChildNodes.add(result);
		return result;
	}

	/** Check absence of an element node in model. */
	final void chkElementAbsence(final int index,
		final XElement xelem,
		final Counter c) {
		if ( _nil) {
			return;
		}
		if (_counters[index] >= xelem.minOccurs()) {
			if (_counters[index] == 0 && xelem._onAbsence >= 0) {
				ChkElement chkElem = new ChkElement(this, null, xelem, true);
				if (_clearReports) {
					clearTemporaryReporter();
				}
				_scp.exec(xelem._onAbsence, chkElem);
			}
			return;
		}
		if (xelem._onAbsence >= 0) {
			//we call the onAbsence method for all required items.
			_actDefIndex = index;
			for (int j = _counters[index]; j < xelem.minOccurs(); j++) {
				_elemValue = _element;
				//create "Pseudo" ChkElement
				ChkElement chkElem = new ChkElement(this, null, xelem, true);
				chkElem.setXXType((byte) 'E');
				if (_clearReports) {
					clearTemporaryReporter();
				}
				_scp.exec(xelem._onAbsence, chkElem);
				if (chkElem._element != null) {
					//save original reports and set clear new reporter
					ArrayReporter arep = getTemporaryReporter();
					setTemporaryReporter(new ArrayReporter());
					chkElem._ignoreAll = false; //set real mode
					if (c == null) {
						_element.appendChild(chkElem._element);
					} else {
						Node n = _element.getChildNodes().item(c._itemIdex);
						if (n == null) {
							_element.appendChild(chkElem._element);
						} else {
							_element.insertBefore(chkElem._element, n);
						}
					}
					_chkChildNodes.add(chkElem);
					chkElem.checkElement();
					chkElem.addElement();
					setTemporaryReporter(arep);//restore reporter
				}
			}
			_actDefIndex = -1;
		} else {
			String name = xelem.getName();
			if (xelem._xon > 0 && X_VALUE.equals(name = xelem.getLocalName())) {
				String[] x = getPosInfo(xelem.getXDPosition(), null);
				int ndx = (x[0].lastIndexOf("['"));
				if (ndx >= 0) {
					int ndx1 = x[0].indexOf("']", ndx);
					if (ndx1 > 0) {
						name = x[0].substring(ndx + 2, ndx1);
					}
				}
			}
			putTemporaryReport(_counters[index] == 0
				? Report.error(XDEF.XDEF539, //Required element '&{0}' is missing
					name + getPosMod(xelem.getXDPosition(), null))
				: Report.error(XDEF.XDEF555, //Minimum occurrence not reached for &amp;{0}
					name + getPosMod(xelem.getXDPosition(), _xPos + "/" + name)));
		}
		copyTemporaryReports();
	}

	private String getTextPathIndex(final int index) {
		int counter = 0;
		for (int i = 0; i < index; i++) {
			if (_childList[i].getKind() == XMTEXT) {
				counter++;
			}
		}
		return (counter > 0) ? "[" + (counter+1) + "]" : "";
	}

	private Node getNodeByIndex(final int index) {
		Node n = _element.getFirstChild();
		for (int i = 0; n != null; n = n.getNextSibling()) {
			if (i++ == index) {
				return n;
			}
		}
		return null;
	}

	/** Check absence of a text node in model. */
	final void chkTextAbsence(final int index,
		final XData xtxt,
		final boolean ignoreAbsence,
		final Counter c) {
		if (_counters[index] != 0 || xtxt.minOccurs() <= XOccurrence.IGNORE) {
			return; //exists or IGNORED
		}
		_xdata = xtxt;
		String orig = _data = null;
		_parseResult = null;
		String xPos = _xPos;
		String txtname = getTextPathIndex(index); // index or ""
		_xPos += "/text()" + txtname;
		txtname = "$text" + txtname;
		if (!_attNames.contains(txtname) && xtxt._onAbsence >= 0) {
			_attNames.add(txtname);
			_elemValue = _element;
			if (_clearReports) {
				clearTemporaryReporter();
			}
			exec(xtxt._onAbsence, (byte) 'T'); //exec onAbsence
			if (_data != null) {
				checkDatatype(xtxt, true);
			}
			copyTemporaryReports();
		} else if (!ignoreAbsence && _data == null && xtxt.minOccurs() >= XData.REQUIRED && !_nil) {
			error(XDEF.XDEF527); //Missing required text
		}
		if (_data == null && xtxt._deflt >= 0) {//exec default
			_data = null;
			_parseResult = null;
			_elemValue = _element;
			XDValue value = exec(xtxt._deflt, (byte) 'T');
			if (value != null) {
				_data = value.toString();
				checkDatatype(xtxt, true);
			}
			copyTemporaryReports();
		}
		debugXPos(XDDebug.FINALLY);
		if (xtxt._finaly >= 0) {
			_elemValue = _element;
			exec(xtxt._finaly, (byte) 'T');
			copyTemporaryReports();
		}
		if (_data != null) {
			if (!_data.equals(orig)) {
				Node txt = xtxt._cdata == 'T' ? _rootChkDocument._doc.createCDATASection(_data)
					: _rootChkDocument._doc.createTextNode(_data);
				if (orig == null) {
					if (c == null) {
						_element.appendChild(txt);
					} else {
						_element.insertBefore(txt, getNodeByIndex(c._itemIdex));
						c._itemIdex++;
					}
					incRefNum();
				} else {
					_element.replaceChild(txt, getNodeByIndex(c._itemIdex));
				}
			}
		} else if (orig != null) {
			_element.removeChild(getNodeByIndex(c._itemIdex));
			c._itemIdex--;
			decRefNum();
			_data = null;
		}
		_xPos = xPos;
		_xdata = null;
		_parseResult = null;
	}

	private boolean isEmptyGroup(final int begIndex, final int endIndex) {
		for (int i = begIndex; i <= endIndex; i++) {
			switch (_childList[i].getKind()) {
				case XMSEQUENCE:
				case XMCHOICE:
				case XMMIXED:
				case XMSELECTOR_END: continue;
				default:
					if (_counters[i] != 0) {
						return false;
					}
			}
		}
		return true;
	}

	/** Check occurrences in a selector of sequence.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skip if true the internal selectors are skipped.
	 * @return true if nonempty content is required.
	 */
	private boolean checkSequenceAbsence(final SelectorState selector, final Counter c, final boolean skip) {
		boolean required = selector.minOccurs() > 0;
		if (selector._selective || !required && !selector._occur) {
			return false;
		}
		int endIndex = selector._endIndex;
		for (int i = selector._begIndex + 1; i < endIndex; i++) {
			XNode xnode;
			switch ((xnode = _childList[i]).getKind()) {
				case XMTEXT: {
					chkTextAbsence(i, (XData) xnode, false, c);
					required &= ((XData) xnode).minOccurs() > 0;
					if (c != null) {
						c._itemIdex += _counters[i];
					}
					continue;
				}
				case XMELEMENT: {
					chkElementAbsence(i, (XElement) xnode, c);
					required &= ((XElement) xnode).minOccurs() > 0;
					if (c != null) {
						c._itemIdex += _counters[i];
					}
					continue;
				}
				case XMSEQUENCE:
				case XMCHOICE:
					required &= ((XSelector) xnode).minOccurs() > 0;
					if (skip) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_childList[i]).getKind()) {
								case XMCHOICE:
								case XMMIXED:
								case XMSEQUENCE:
								case XMSELECTOR_END: continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s = new SelectorState(selector, (XSelector) xnode);
						required = s._kind == XMCHOICE && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skip);
						i = s._endIndex;
					}
					continue;
				case XMMIXED:
					if (skip) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_childList[i]).getKind()) {
								case XMCHOICE:
								case XMMIXED:
								case XMSEQUENCE:
								case XMSELECTOR_END: continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s = new SelectorState(selector, (XSelector) xnode);
						required = (s._kind==XMCHOICE) && s.minOccurs()>0;
						required &= checkAbsence(s, c, skip);
						i = s._endIndex;
					}
					continue;
				case XMSELECTOR_END:
					if (skip) {
						continue;
					}
					return required;
				default:
			}
		}
		return required;
	}

	/** Check occurrences in a selector of mixed.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skip if skip the internal selectors are skipped.
	 * @return true if nonempty content is required.
	 */
	private boolean checkMixedAbsence(final SelectorState selector, final Counter c, final boolean skip) {
		int endIndex = selector._endIndex;
		int begIndex = selector._begIndex + 1;
		boolean required = selector.minOccurs() > 0;
		boolean empty = isEmptyGroup(begIndex, endIndex);
		if (empty) {
			XSelector xs = (XSelector) _childList[selector._begIndex];
			if (xs._onAbsence >= 0) {
				if (skip || !required) {
					if (_clearReports) {
						clearTemporaryReporter();
					}
					exec(xs._onAbsence, (byte) 'U');
				}
				return false;
			}
			if (!required) {
				return false;
			}
		}
		if (!required && selector._selective) {
			return false;
		}
		if (!empty) {
			for (int i = begIndex; i < endIndex; i++) {
				XNode xnode;
				switch ((xnode = _childList[i]).getKind()) {
					case XMTEXT: {
						chkTextAbsence(i, (XData) xnode, false, c);
						required &= ((XData) xnode).minOccurs() > 0;
						if (c != null) {
							c._itemIdex += _counters[i];
						}
						continue;
					}
					case XMELEMENT: {
						chkElementAbsence(i, (XElement) xnode, c);
						required &= ((XElement) xnode).minOccurs() > 0;
						if (c != null) {
							c._itemIdex += _counters[i];
						}
						continue;
					}
					case XMCHOICE: {
						XChoice xch = (XChoice) xnode;
						required &= xch.minOccurs() > 0;
						int j = xch._endIndex;
						int k = 0; // number of occurrences
						for (int n = xch._begIndex; n <= j; n++) {
							k += _counters[n];
						}
						if (k < xch.minOccurs()) {
							//Minimum occurrence not reached for &{0}
							putTemporaryReport(Report.error(XDEF.XDEF555, "choice"));
							if (xch._onAbsence >= 0) {
								if (_clearReports) {
									clearTemporaryReporter();
								}
								exec(xch._onAbsence, (byte)'U');
							}
						}
						i = j;
						continue;
					}
					case XMSEQUENCE:
						required &= ((XSelector) xnode).minOccurs() > 0;
						if (skip) {
							int j = ((XSelector) xnode)._endIndex;
							while (++i < j) {
								switch ((_childList[i]).getKind()) {
									case XMCHOICE:
									case XMMIXED:
									case XMSEQUENCE:
									case XMSELECTOR_END: continue;
									default:
										if (c != null) {
											c._itemIdex += _counters[i];
										}
								}
							}
						} else {
							SelectorState s = new SelectorState(selector, (XSelector) xnode);
							required = s._kind == XMCHOICE && s.minOccurs() > 0;
							required &= checkAbsence(s, c, skip);
							i = s._endIndex;
						}
						continue;
					case XMMIXED:
						if (skip) {
							int j = ((XSelector) xnode)._endIndex;
							while (++i < j) {
								switch ((_childList[i]).getKind()) {
									case XMCHOICE:
									case XMMIXED:
									case XMSEQUENCE:
									case XMSELECTOR_END: continue;
									default:
										if (c != null) {
											c._itemIdex += _counters[i];
										}
								}
							}
						} else {
							SelectorState s = new SelectorState(selector, (XSelector) xnode);
							required = (s._kind==XMCHOICE) && s.minOccurs() > 0;
							required &= checkAbsence(s, c, skip);
							i = s._endIndex;
						}
						continue;
					case XMSELECTOR_END:
						if (skip) {
							continue;
						}
						return required;
					default:
				}
			}
		}
		if (required &&	!selector._occur &&	selector._count == 0) {
			if (!empty && selector._prev != null && selector._prev._count >= selector._prev.minOccurs()) {
				return required;
			}
			if (_xElement._xon > 0) {
				error(XDEF.XDEF541, _xElement.getLocalName()); //Missing required item(s) in &{0}
			} else {
				//Sequence "xd:mixed" has no required item
				error(XDEF.XDEF520, getPosMod(_childList[selector._begIndex].getXDPosition(), _xPos));
			}
		}
		return required;
	}

	/** Check occurrences in a selector of choice.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skip if true the internal selectors are skipped.
	 * @return true if nonempty content is required.
	 */
	private boolean checkChoiceAbsence(final SelectorState selector, final Counter c, final boolean skip) {
		if (selector.minOccurs() <= 0) {
			return false; // not required
		}
		boolean required = selector.minOccurs() > 0;
		int endIndex = selector._endIndex;
		for (int i = selector._begIndex + 1; i < endIndex; i++) {
			XNode xnode;
			switch ((xnode = _childList[i]).getKind()) {
				case XMTEXT: {
					XData xtxt = (XData) xnode;
					if (_counters[i] == 0) {
						if (xtxt.minOccurs() == 0) {
							return false; //optional variant
						}
						continue;
					}
					chkTextAbsence(i, xtxt, false, c);
					return false;
				}
				case XMELEMENT: {
					XElement xelem = (XElement) xnode;
					if (_counters[i] == 0) {
						if (xelem.minOccurs() == 0) {
							return false; //optional variant
						}
						continue;
					}
					chkElementAbsence(i, xelem, c);
					return false;
				}
				case XMSEQUENCE:
				case XMCHOICE:
					required &= ((XSelector) xnode).minOccurs() > 0;
					if (skip) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_childList[i]).getKind()) {
								case XMCHOICE:
								case XMMIXED:
								case XMSEQUENCE:
								case XMSELECTOR_END: continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s = new SelectorState(selector, (XSelector) xnode);
						required = s._kind == XMCHOICE && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skip);
						i = s._endIndex;
					}
					continue;
				case XMMIXED:
					if (skip) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_childList[i]).getKind()) {
								case XMCHOICE:
								case XMMIXED:
								case XMSEQUENCE:
								case XMSELECTOR_END: continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s = new SelectorState(selector, (XSelector) xnode);
						required = (s._kind == XMCHOICE) && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skip);
						i = s._endIndex;
					}
					continue;
				case XMSELECTOR_END:
					if (skip) {
						continue;
					}
					return required;
				default:
			}
		}
		if (!skip && selector._occur == false
			&& selector._count == 0 && required) {
			// do not report error if onAbsence
			if (((XSelector) _childList[selector._begIndex])._onAbsence < 0) {
				//Missing required item(s0 in &{0}
				error(XDEF.XDEF541, getPosMod(getXDPosition()+"/#choice",_xPos));
			}
		}
		return required;
	}

	/** Check occurrences in a selector.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skip if true the internal selectors are skipped.
	 * @return true if nonempty content is required.
	 */
	final boolean checkAbsence(final SelectorState selector, final Counter c, final boolean skip) {
		switch (selector._kind) {
			case XMCHOICE: return checkChoiceAbsence(selector, c, skip);
			case XMMIXED: return checkMixedAbsence(selector, c, skip);
		}
		return checkSequenceAbsence(selector, c, skip);
	}

	/** Finish processing of a group. */
	final void finishGroup() {
		_actDefIndex = -1;
		int finaly = _selector._finallyCode;
		debugXPos(XDDebug.SELECTORCREATE);
		if (_selector._kind == XMMIXED) {
			if (!_selector._occur) {//no variant reached
				if (_selector._count == 0) {
					if (_selector.minOccurs() > 0) {
						if (_selector._prev == null || _selector._prev._kind == XMSEQUENCE) {
							checkAbsence(_selector, null, true);
						}
					}
				} else {
					checkAbsence(_selector, null, true);
				}
				if (_selector._prev != null) {
					_selector._prev._occur |= _selector._count > 0;
				}
				_selector = _selector._prev;
				_nextDefIndex++;
				if (finaly >= 0) {
					exec(finaly, (byte)'U');
				}
				return;
			}
			if (_selector._occur) {
				_selector._occur = false;
				_counters[_nextDefIndex] = ++_selector._count;
				_nextDefIndex = _selector._begIndex + 1;
			} else {
				if (_selector._prev != null) {
					_selector._prev._occur |= _selector._count > 0;
				}
				_selector = _selector._prev;
				_nextDefIndex++;
			}
			return;
		}
		if (_selector._kind == XMCHOICE) {
			if (_selector._occur) {
				_selector._count = ++_counters[_nextDefIndex];
			}
			if (_selector.maxOccurs() <= 1 || !_selector._occur) {
				if (_selector._count < _selector.minOccurs() && (_selector._prev == null
					|| _selector._prev.minOccurs() > _selector._prev._count
					&& _selector._prev._begIndex == _selector._begIndex + 1
					&& _selector._prev._endIndex == _selector._endIndex - 1)) {
					putReport(Report.error(XDEF.XDEF555, "choice")); //Minimum occurrence not reached for &{0}
					XSelector xsel = (XSelector) getDefElement(_selector._begIndex);
					if (xsel._onAbsence >= 0) {
						if (_clearReports) {
							clearTemporaryReporter();
						}
						exec(xsel._onAbsence, (byte)'U');
					}
				}
				_selector.updateCounters();
				if (_selector._prev != null) {
					_selector._prev._occur |= _selector._count > 0;
					_selector = _selector._prev;
				} else {
					_selector = null;
				}
				_nextDefIndex++;
				if (finaly >= 0) {
					exec(finaly, (byte)'U');
				}
			} else {
				checkAbsence(_selector, new Counter(_selector._firstChild), true);
				if (_selector.maxOccurs() <= 1) {
					if (finaly >= 0) {
						exec(finaly, (byte) 'U');
					}
					_nextDefIndex++;
					return;
				}
				// Choice was not finished, will continue
				if (_selector.saveAndClearCounters()) {
					_selector._count++;
					_selector._occur = false;
				}
				_nextDefIndex = _selector._begIndex + 1;
			}
		} else {// _selector._kind == XNode.X_SEQUENCE
			if (_selector._occur) {
				checkAbsence(_selector,new Counter(_selector._firstChild),true);
				if (_selector._count >= _selector.maxOccurs() -1) {
					_selector._count++;
					if (_selector.minOccurs() >  _selector._count) {
						error(XDEF.XDEF558, "sequence"); //Maximum occurrence limit of &{0} exceeded
					}
					_selector.updateCounters(); //maximum reached
					_nextDefIndex++;
					if (_selector._prev != null) {
						_selector._prev._occur = true;
					}
					_selector = _selector._prev;
					if (finaly >= 0) {
						exec(finaly, (byte)'U');
					}
					return;
				}
			}
			if (_selector.maxOccurs() <= 1 || !_selector._occur) {
				if (_selector._prev == null) {
					if (!_selector._ignorable && _selector.minOccurs() > 0) {
						if (!_selector._occur) {//was not checked
							checkAbsence(_selector, new Counter(_selector._firstChild), true);
						}
					}
					if (finaly >= 0) {
						exec(finaly, (byte) 'U');
					}
					_selector = null;
					_nextDefIndex++;
				} else {
					_selector._prev._occur |= _selector._occur || _selector._count > 0;
					if (_selector._prev._kind != XMSEQUENCE && _selector._count > 0) {
						_selector._prev._occur = true;
						_nextDefIndex = _selector._prev._begIndex + 1;
					} else {
						if (finaly >= 0) {
							exec(finaly, (byte) 'U');
						}
						_nextDefIndex++;
					}
					_selector.updateCounters();
					_selector = _selector._prev;
				}
				if (finaly >= 0) {
					exec(finaly, (byte)'U');
				}
			} else {
				checkAbsence(_selector,new Counter(_selector._firstChild),true);
				if (_selector.saveAndClearCounters()) {
					_selector._count++;
					_selector._occur = false;
				}
				_nextDefIndex = _selector._begIndex + 1;
				if (finaly >= 0) {
					exec(finaly, (byte)'U');
				}
			}
		}
	}

	final boolean createGroup(final XSelector xs) {
		_actDefIndex = -1;
		if (xs.minOccurs() < 0) { //ignore, illegal
			_nextDefIndex = xs._endIndex + 1;
			return false;
		}
		if (xs._match >= 0 && !getXDDocument().isCreateMode()) {
			_elemValue = _element;
			XDValue value = exec(xs._match, (byte) 'U');
			if (xs._match >= 0) {
				if (value != null && !value.isNull() && !value.booleanValue()) {
					_nextDefIndex = xs._endIndex + 1;
					return false; // not match
				}
			}
		}
		if (_selector == null || _selector._begIndex != _nextDefIndex) {
			_selector =	new SelectorState(_selector, xs);
			debugXPos(XDDebug.SELECTORINIT);
			if (xs._init >= 0) {
				exec(xs._init, (byte) 'U');
			}
		}
		_nextDefIndex++;
		if (xs.getKind() == XMCHOICE) {
			if (_counters[_selector._endIndex] >= _selector.maxOccurs()) {
				_nextDefIndex = _selector._endIndex + 1;
			}
		}
		return true;
	}

	/** Checks if all items of a mixed group are filled. If yes, call the method finishGroup() and
	 * _nextDefIndex is set after the mixed group. If the mixed group is the last one in a sequence group,
	 * then then the counter of occurrences of the sequence is increased.
	 */
	private void checkMixedAll() {
		if (_selector._prev != null && _childList[_selector._prev._begIndex].maxOccurs() > 0
			&& _selector._kind == XMMIXED) {
			for (int i = _selector._begIndex + 1; i < _selector._endIndex; i++) {
				if (_counters[i] < _childList[i].maxOccurs()) {
					return;
				}
			}
			// all items of "mixed" sequence were processed; finish the group.
			_selector._occur = true;
			_selector._count++;
			_nextDefIndex = _selector._endIndex + 1;
			if (_selector._prev._kind!=XMSEQUENCE || _childList[_nextDefIndex].getKind()!=XMSELECTOR_END) {
				finishGroup();
				return;
			}
			if (_selector._prev.maxOccurs() == 1) {
				return;
			}
			if (_selector._prev._count <= _selector._prev.maxOccurs()) {
				_nextDefIndex = _selector._prev._begIndex + 1;
				_selector._prev._count++;
				_selector._prev._occur = true;
				finishGroup();
			} else if(_selector._prev._count > _selector._prev.maxOccurs()) {
				_nextDefIndex = _selector._prev._endIndex;
				error(XDEF.XDEF558, "sequence"); //Maximum occurrence limit of &{0} exceeded
			}
		}
	}

	/** Search XNode in the list of nodes.<p/>
	 * This is very tricky method. It is necessary to know that it is invoked
	 * when a child node occurs. There are important variables:<p/>
	 * _defList .. array of XNodes<br/>
	 * _counters .. array of occurrence counters<br/>
	 * _actDefIndex .. -1 or index to actually processed XNode item<br/>
	 * _nextDefIndex .. index to the XNode item to be inspected.<br/>
	 * _selector .. container of information about actual selection block.<br/>
	 * @param el the element or null it text node is processed.
	 * @return XNode or ChkElement object or null.
	 */
	private Object findXNode(final Element el) {
		XNode xn;
		ChkElement result;
		int defLength = _childList.length;
		int lastNextDefIndex = _nextDefIndex;
		int lastActDefIndex = _actDefIndex;
		if (el == null) { // element is null => text node
			if (_nextDefIndex < defLength && _childList[_nextDefIndex].getKind() != XMTEXT) {
				if ((xn = _xElement.getDefAttr("$text", -1)) != null) {
					return xn;
				} else if ((xn=_xElement.getDefAttr("$textcontent",-1))!=null) {
					return new XData("$text", null, xn.getXDPool(), XMTEXT); //dummy, just process string()
				}
			}
		} else if (_actDefIndex >= 0 && (xn = _childList[_actDefIndex]).getKind() == XMELEMENT
			&& xn.maxOccurs() > 1 && (_selector == null || _selector._kind != XMMIXED)) {
			if ((result = chkElem((XElement) xn, el)) != null) {
				// repeated nodes
				if (xn.maxOccurs() != 1 || xn.minOccurs() != 1) {//???template
					if (_counters[_actDefIndex] < xn.maxOccurs() || _actDefIndex+1 >= defLength) {
						// max occurrence not reached or if the X-definition not follows a XElement node
						return result;
					}
					// maxOccurrence exceeded, so check if the next node is an element with the same name.
					XNode x = _childList[_actDefIndex+1];
					if (x.getKind() != XMELEMENT || !xn.getName().equals(x.getName())) {
						return result; // not XElement node or not same name
					}
					// so we force to skip to the next model of element
					_nextDefIndex = _actDefIndex + 1;
				}
			}
			if (xn.minOccurs() > _counters[_actDefIndex]) {
				chkElementAbsence(_actDefIndex, (XElement) xn, null);
			}
		}
		if (_selector!= null) {
			if (_selector._kind == XMMIXED) {
				_nextDefIndex = _selector._begIndex + 1;
			}
		}
		_actDefIndex = -1;
		while (_nextDefIndex < defLength) {
			short kind;
			switch (kind = (xn = _childList[_nextDefIndex]).getKind()) {
				case XMTEXT: {
					if (el == null) {// is text node
						int oldDefIndex = _actDefIndex;
						_actDefIndex = _nextDefIndex++;
						XData xd = (XData) xn;
						if (xd._match >= 0 && !getXDDocument().isCreateMode()) {
							_elemValue = _element;
							XDValue item = exec(xd._match, (byte) 'T');
							copyTemporaryReports();
							if (item == null || !item.booleanValue()) {
								_actDefIndex = oldDefIndex;
								continue;
							}
						}
						if (_selector != null) {
							_selector._occur = true; //found
							if (_selector._kind == XMCHOICE) {
								_nextDefIndex = _selector._endIndex;
							} else {
								checkMixedAll();
							}
						}
						return xd;
					}
					if (_selector == null) {
						genAbsentText((XData) xn);
					} else {
						_nextDefIndex++;
					}
					_actDefIndex = -1;
					continue;
				}
				case XMELEMENT: {
					XElement xel = (XElement) xn;
					if (el != null) { // not text node (element)
						int oldDefIndex = _actDefIndex;
						_actDefIndex = _nextDefIndex; // save actual index
						if ((result = chkElem(xel, el)) != null) {
							_nextDefIndex++;
							if (_selector != null) {
								_selector._occur = true;
								if (_selector._kind == XMCHOICE) {
									_nextDefIndex = _selector._endIndex;
									if (_selector._count > _selector.maxOccurs()) {
										//Maximum occurrence limit of &amp;{0} exceeded
										error(XDEF.XDEF558, "choice");
									}
								} else {
									checkMixedAll();
								}
							}
							return result;
						}
						_actDefIndex = oldDefIndex; // reset actual index
					} else if (xel.minOccurs() <= 0) {
						// el == null => text node and this element not required
						_nextDefIndex++; // next item
						continue;
					}
					if (_selector == null || _selector._kind == XMSEQUENCE
						&& !_selector._selective && _selector._prev == null) {
						int index = _nextDefIndex;
						int counter = _counters[index];
						if (counter == 0 && xel._onAbsence >= 0 && xel.minOccurs() == 0) {
							// not required element, however execute onAbsence
							chkElementAbsence(index, xel, null);
							_nextDefIndex++;
							continue;
						}
						if (counter < xel.minOccurs()) {//required element is missing
							while (_nextDefIndex + 1 < _childList.length) {
								XNode x = _childList[_nextDefIndex + 1];
								if (x.getKind()==XMELEMENT && el!=null) {
									if ((result=chkElem((XElement)x,el))!=null){
										chkElementAbsence(index, xel, null);
										//following element is OK
										_nextDefIndex++;
										if (_selector != null) {
											_selector._occur = true; //found
										}
										_actDefIndex = _nextDefIndex++;
										return result;
									} else if (x.minOccurs() <= 0) {
										// skip next XMELEMENT if not required
										_actDefIndex = _nextDefIndex++;
										continue;
									}
								}
								break;
							}
							if (_selector == null) {
								return null; //not in sequence, not found
							}
						}
					} else if (_selector._kind == XMSEQUENCE && _selector._selective
						&& _nextDefIndex == _selector._begIndex + 1) {
						_nextDefIndex = _selector._endIndex;
						continue;
					}
					_nextDefIndex++;
					continue;
				}
				case XMSELECTOR_END: {
					if (_selector == null) {//???
						_nextDefIndex++;
						return null;
					}
					if (el == null && _selector._prev == null && _selector._kind == XMMIXED
						&& (_nextDefIndex==defLength -1 || _childList[_nextDefIndex+1].getKind()!=XMTEXT)) {
						//just to improve error reporting ???
						_nextDefIndex = lastNextDefIndex;
						_actDefIndex = lastActDefIndex;
						return null;
					}
					finishGroup();
					continue;
				}
				case XMSEQUENCE:
				case XMMIXED:
				case XMCHOICE: {
					createGroup((XSelector) xn);
					continue;
				}
				default: //error - unknown kind
					throw new SRuntimeException(SYS.SYS066, //Internal error&{0}{: }
						"Xdefinifion - ChkElement, unknown item: "+kind+" "+xn);
			}
		}
		return null;
	}

	/** Generate missing text if it is required. */
	private void genAbsentText(final XData xtxt) {
		_actDefIndex = _nextDefIndex++;
		_xdata = xtxt;
		String xPos = _xPos;
		_xPos += "/text()";
		int ndx = -1;
		for (Node n=_element.getFirstChild(); n!=null; n=n.getNextSibling()) {
			if (n.getNodeType() == Node.TEXT_NODE) {
				ndx++;
			}
		}
		if (ndx >= 0) {
			_xPos += "[" + (ndx + 2) + "]";
		}
		if (xtxt._deflt >= 0) {//exec default
			_elemValue = _element;
			XDValue value = exec(xtxt._deflt, (byte) 'T');
			if (value != null) {
				_data = value.toString();
				checkDatatype(xtxt, true);
			}
			copyTemporaryReports();
		}
		if (_data == null && xtxt.minOccurs() == XData.REQUIRED) {
			//required && missing obligatory text
			if (xtxt._onAbsence >= 0) {
				if (_clearReports) {
					clearTemporaryReporter();
				}
				//exec onAbsence
				_elemValue = _element;
				exec(xtxt._onAbsence, (byte) 'T');
				if (_data != null) {
					checkDatatype(xtxt, true);
				}
				copyTemporaryReports();
			}
			if (_data == null) {
				if (_selector == null || !_selector._error) {
					if (xtxt._onAbsence < 0  && !_nil) {
						error(XDEF.XDEF527); //Missing required text in
					}
					if (_selector != null) {
						_selector._error = true; //don't repeat error reporting
					}
				}
			}
		}
		if (_data != null) {
			appendTextNode(_data, xtxt);
			incRefNum();
		}
		_xPos = xPos;
		_xdata = null;
		_parseResult = null;
	}

	private Node appendTextNode(final String data, final XData xtxt) {
		Node txt = xtxt._cdata == 'T' ? _rootChkDocument._doc.createCDATASection(data)
			: _rootChkDocument._doc.createTextNode(data);
		_element.appendChild(txt);
		if (_scp.getXmlStreamWriter() != null) {
			try {
				_scp.getXmlStreamWriter().writeNode(txt);
			} catch (SRuntimeException ex) {
				putReport(ex.getReport());
			}
		}
		return txt;
	}

	/** Check if element complies with model.
	 * @param xel the XElement object.
	 * @param el The source element from which the result is composed.
	 * @return ChkElement object or null if element do not comply.
	 */
	private ChkElement chkElem(final XElement xel, final Element el) {
		if (!"$any".equals(xel.getName())) {
			String localName = xel.getLocalName();
			String s = _rootChkDocument.findInLexicon(xel.getXDPosition());
			if (s != null) {
				localName = s;
			}
			s = el.getNodeName();
			int ndx = s.indexOf(':');
			if (ndx >= 0) {
				s = s.substring(ndx + 1);
			}
			if (!localName.equals(s)) {
				return null;
			}
			String uri = el.getNamespaceURI();
			if (uri == null) {
				if (xel.getNSUri() != null) {
					return null;
				}
			} else if (!uri.equals(xel.getNSUri())) {
				return null;
			}
		}
		ChkElement chkEl = new ChkElement(this, el, xel, false);
		if (chkEl._nil) {
			return chkEl;
		}
		if (xel._match >= 0 && !getXDDocument().isCreateMode()) {
			//Execute "match" action on element
			chkEl._elemValue = chkEl._element;
			chkEl.setXXType((byte) 'E');
			XDValue result = _scp.exec(xel._match, chkEl);
			copyTemporaryReports();
			if(result == null || !result.booleanValue()) {
				String s = _xPos + '/' + chkEl._name;
				XPosInfo x = _xPosOccur.get(s);
				if (x != null) { // never should be null!
					if (x.subCount() <= 0) { // decrease xpath counter
						_xPosOccur.remove(s); // if it is the first remove it
					}
				}
				return null;
			}
		}
		chkEl.initElem();
		return chkEl;
	}

	/** Prepare variables and execute the init section of X-script. */
	final void initElem() {
		// prepare variables declared in the script (do not make it twice)
		if (_xElement._varinit >= 0 && _variables == null) {
			_variables = new XDValue[_xElement._varsize];
			exec(_xElement._varinit, (byte) 'E');
			copyTemporaryReports();
		}
		debugXPos(XDDebug.INIT);
		if (_xElement._init >= 0) {
			_elemValue = _element;
			exec(_xElement._init, (byte) 'E');
			copyTemporaryReports();
			_elemValue = null;
		}
	}

	/** Get actual X-definition assigned to node.
	 * @param i The index of X-definition.
	 * @return The actual definition or null.
	 */
	public final XNode getDefElement(final int i) {return i < _childList.length ? _childList[i] : null;}

	/** Get maximal index of X-definition in the list.
	 * @return Max index of definition list.
	 */
	final int getDefinitionMaxIndex() {return _childList.length;}

	/** Add the new attribute to the current element.
	 * @param att The object with attribute.
	 * @return true if attribute was created according to X-definition.
	 */
	final boolean newAttribute(final Attr att) {
		_node = att;
		boolean result = addAttributeNS(att.getNamespaceURI(), att.getName(), att.getValue());
		_node = null;
		return result;
	}

	/** Process attribute white spaces.
	 * @param xatt Model of attribute.
	 * @param data value of attribute.
	 * @return modified value.
	 */
	private String attrWhitespaces(final XData xatt, final String data) {
		String result;
		if ((xatt != null && xatt._attrWhiteSpaces!= 0) ? xatt._attrWhiteSpaces == 'T'
			: (_xElement._attrWhiteSpaces!= 0) ? _xElement._attrWhiteSpaces == 'T'
			: _rootChkDocument._attrWhiteSpaces == 'T') {
			result = SUtils.trimAndRemoveMultipleWhiteSpaces(data);
		} else if ((xatt != null && xatt._trimAttr != 0) ? xatt._trimAttr != 'F'
			: (_xElement._trimAttr != 0) ? _xElement._trimAttr != 'F'
			: _rootChkDocument._trimAttr != 'F') {
			result = data.trim();
		} else {
			result = data;
		}
		if (result.isEmpty()) {
			if ((xatt != null && xatt._ignoreEmptyAttributes != 0)
				? xatt._ignoreEmptyAttributes == 'T'
				: (_xElement._ignoreEmptyAttributes != 0) ? _xElement._ignoreEmptyAttributes == 'T'
				: _rootChkDocument._ignoreEmptyAttributes == 'T') {
				return null;
			}
			return result;
		}
		if (xatt != null && xatt.isFixed()) {
			return result;
		}
		byte c = (xatt != null && xatt._attrValuesCase != 0) ? xatt._attrValuesCase
			: _xElement._attrValuesCase != 0 ? _xElement._attrValuesCase
			: _rootChkDocument._setAttrValuesCase;
		return c == 'T' ? result.toUpperCase() : c == 'F' ? result.toLowerCase() : result;
	}

	private XData getXAttr(final String nsURI, final String qname) {
		if (nsURI == null) {
			return getXAttr(qname);
		}
		int ndx = qname.indexOf(':');
		String localName = qname.substring(ndx + 1);
		XData xatt = _xElement.getDefAttrNS(nsURI, localName, _rootChkDocument._sourceLanguageID);
		if (xatt == null && nsURI.equals(_element.getNamespaceURI())) {
			XData xa = _xElement.getDefAttr(localName, _rootChkDocument._sourceLanguageID);
			if (xa != null && xa._acceptQualifiedAttr == 'T') {
				return xa;
			}
		}
		return xatt;
	}

	private XData getXAttr(final String name) {
		return _xElement.getDefAttr(name, _rootChkDocument._sourceLanguageID);
	}

	/** Execute validation method and if putTempErrors is true then put errors to reporter.
	 * @param xdata model of data.
	 * @param putTempErrors if true then put errors to reporter.
	 */
	private void checkDatatype(final XData xdata, final boolean putTempErrors) {
		if (xdata._check >= 0) {
			XDValue item = exec(xdata._check, (byte) 'A');
			if (item.getItemId() == XD_PARSERESULT) {
				_parseResult = (XDParseResult) item;
				_data = _parseResult.getSourceBuffer();
				if (_xComponent != null && _parseResult.matches()
					&& getXMNode()!=null && getXMNode().getXDPosition()!=null) {
					if ("$text".equals(xdata.getName())) {
						_xComponent.xSetText(this, _parseResult);
					} else {
						_xComponent.xSetAttr(this, _parseResult);
					}
				}
			} else {
				_parseResult = new DefParseResult(_data);
				if (item.booleanValue()) {
					if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
						_parseResult.setParsedValue(_data);
						_xComponent.xSetAttr(this, _parseResult);
					}
				} else {
					_parseResult.putDefaultParseError(); //XDEF515=Value error&{0}{ :}
				}
			}
		} else {//default: do not check; i.e. always true
			setXXType((byte) 'A');
			_parseResult = new DefParseResult(_data);
			_parseResult.setEos();
			_parseResult.setParsedValue(_data);
//			org.xdef.XDParserAbstract.checkCharset(this, _parseResult); // check charset
			if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
				_parseResult.setParsedValue(_data);
				_xComponent.xSetAttr(this, _parseResult);
			}
		}
		if (!_parseResult.matches()) { //error
			for (Report rep: _parseResult.getReporter()) {
				String s = rep.getModification();
				if (s != null && !s.isEmpty()) {
					rep.setModification(s + getPosMod(xdata.getXDPosition(),_xPos));
				}
				if (putTempErrors) {
					_scp.getTemporaryReporter().putReport(rep);
				}
			}
			if (!chkTemporaryErrors()) {
				if (putTempErrors) {
					putTemporaryReport(Report.error(XDEF.XDEF515)); //Value error
				}
			}
		} else if (_xElement._xon > 0) {
			Object value = _parseResult.getParsedValue();
			if (value==null) {
				value = _parseResult.getSourceBuffer();
			}
			if (X_KEYATTR.equals(xdata.getName())) {
				_xonKey = XonTools.xmlToJName(value.toString());
			} else if (X_VALATTR.equals(xdata.getName())) {
				_xonKey = _xElement.getNSUri() == null ? XonTools.xmlToJName(_element.getTagName())
					: XonTools.xmlToJName(_element.getAttribute(X_KEYATTR));
				if (value instanceof XDValue) {
					XDValue x = (XDValue) value;
					if (x.isNull() || x.getItemId() == XD_NULL) {
						_xonValue = null;
					} else {
						Object obj = x.getObject();
						if (obj instanceof Number) {
							switch (xdata.getParserName()) {
								case "byte": _xonValue = x.byteValue(); break;
								case "unsignedByte":
								case "short": _xonValue = x.shortValue(); break;
								case "unsignedShort":
								case "int": _xonValue = x.intValue(); break;
								case "float": _xonValue = x.floatValue(); break;
								default: _xonValue = obj; // "decimal", "dec"
							}
						} else if (obj instanceof String) {
							_xonValue = XonTools.xmlToJValue((String) obj);
						} else {
							_xonValue = obj;
						}
					}
				} else {
					_xonValue = value;
				}
			}
		}
	}

	/** Remove attribute from actual element.
	 * @param nsURI namespace URI of attribute.
	 * @param qname qualified name of attribute.
	 */
	private void removeAttr(final String nsURI, final String qname) {
		if (nsURI != null) {
			int ndx = qname.indexOf(':');
			_element.removeAttributeNS(nsURI, ndx >= 0 ? qname.substring(ndx + 1) : qname);
		} else {
			_element.removeAttribute(qname);
		}
	}

	/** If data value was changed in a section then run validation method
	 * @param xatt model of attribute.
	 * @param orig original value of data.
	 * @param nsURI namespace URI of attribute.
	 * @param qname qualified name of attribute.
	 */
	private void updateAttrValue(final XData xatt, final String orig, final String nsURI, final String qname){
		copyTemporaryReports();
		if (_data != orig) { // _data was changed, even may be equal
			if (_data == null) {
				removeAttr(nsURI, qname);
			} else {
				checkDatatype(xatt, false);
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////
	/** Get reference counter of given node (with an index).
	 * @param index the index of inspected node.
	 * @return The reference number.
	 */
	public final int getRefNum(final int index) {return _counters[index];}

	/** Set attribute to the current element. First remove the original attribute if exists to prevent report
	 * about attribute redefinition.
	 * @param name The name of attribute.
	 * @param value The value of attribute.
	 * @return true if attribute was created according to X-definition.
	 */
	public final boolean setAttribute(final String name, final String value) {
		_element.setAttribute(name, value);
		return newAttribute(_element.getAttributeNode(name));
	}

	/** Set attribute to the current element. First remove the original attribute if exists to prevent report
	 * about attribute redefinition.
	 * @param name The name of attribute.
	 * @param data The value of attribute.
	 * @param nsURI The value of namespace URI.
	 * @return true if attribute was created according to X-definition.
	 */
	public final boolean setAttribute(final String name, final String data, final String nsURI) {
		if (nsURI == null) {
			return setAttribute(name, data);
		}
		_element.setAttributeNS(nsURI, name, data);
		return newAttribute(_element.getAttributeNode(name));
	}

	/** Update actual element in the tree. If argument is null actual element will be removed from the tree.
	 * @param el The element which will replace the actual one.
	 */
	public final void updateElement(final Element el) {
		Element el1, el2;
		if ((el2 = el) == null) { //remove child
			if (_element != null) {
				if (_parent._parent == null) { // root element
					try {
						((ChkDocument) _parent).getDocument().removeChild(_element);
					} catch (DOMException ex) {}
					((ChkDocument) _parent)._element = null;
				} else if ((el1 = _parent.getElement()) != null ) {
					el1.removeChild(_element);
				}
				_element = null;
			}
		} else if (el2 != _element) {
			//update child
			if (_element == null) {
				if (_parent._parent == null) { // root element
					((ChkDocument) _parent).getDocument().appendChild(el2);
				} else {
					if ((el1 = _parent.getElement()) != null) {
						el1.appendChild(el2);
					}
				}
			} else {
				if (_parent._parent == null) { // root  element
					ChkDocument root = ((ChkDocument) _parent);
					root.getDocument().replaceChild(el2, _element);
					root._element = el2;
				} else if ((el1 = _parent.getElement()) != null) {
					Document doc = _element.getOwnerDocument();
					Document doc1 = el1.getOwnerDocument();
					if (doc != doc1) {
						el1 = (Element) doc.importNode(el1, true);
					}
					if (el2 != doc1.getDocumentElement()) {
						Document doc2 = el2.getOwnerDocument();
						if (doc != doc2) {
							el2 = (Element) doc.importNode(el2, true);
						}
						if (el2 != doc1.getDocumentElement()) {
							el1.replaceChild(el2, _element);
						}
					}
				}
			}
			_element = el2;
		}
	}

	final void finishSelector() {
		if (_selector == null) {
			return;
		}
		// ckeck if there is list of pending selector
		if (!_selector._ignorable && _selector.minOccurs() > 0 && _selector.minOccurs() > (
			_selector._occur ? _selector._kind == XMMIXED ? 1 : ++_selector._count : _selector._count)) {
			XSelector xsel = (XSelector) getDefElement(_selector._begIndex);
			error(XDEF.XDEF555, xsel.getName().substring(1)); //Minimum occurrence not reached for &{0}
			debugXPos(XDDebug.ONABSENCE);
			if (xsel._onAbsence>= 0) {
				exec(xsel._onAbsence, (byte)'U');
			}
		}
		boolean nested = false;
		while (_selector != null) {
			if (_selector._kind == XMCHOICE) {
				nested = true;
				if (_selector._occur && _nextDefIndex == _selector._endIndex) {
					_selector._count = ++_counters[_nextDefIndex];
				}
			} else if (_selector._occur) {
				_selector._count++;
			}
			//check absence within a group. If actual node is the end of a group
			// then set "skipselector" to true, othewise to false.
			checkAbsence(_selector,
				null,
				_nextDefIndex<_childList.length&&_childList[_nextDefIndex].getKind()==XMSELECTOR_END&&nested);
			if (_selector._kind == XMSEQUENCE && _selector._count <_selector.minOccurs()) {
				error(XDEF.XDEF555, "sequence"); //Minimum occurrence not reached for &{0}
			}
			nested = true;
			_actDefIndex = -1;
			_nextDefIndex = _selector._endIndex + 1;
			debugXPos(XDDebug.SELECTORFINALLY);
			if (_selector._finallyCode >= 0) {
				exec(_selector._finallyCode, (byte) 'U');
			}
			if (_selector._prev != null) {
				_selector._prev._count++;
			}
			_selector = _selector._prev;
		}
	}

	/** Finish checking of model.
	 * @param element the element or null it text node is processed.
	 * @return the XNode object from the list or null.
	 */
	private void finishModel() {
		_parseResult = null;
		// check if last element occurrence
		if (_actDefIndex >= 0) { // check last processed item
			XNode xn = _childList[_actDefIndex];
			if (xn.getKind() == XMELEMENT) {
				if (xn.minOccurs() > _counters[_actDefIndex]) {
					chkElementAbsence(_actDefIndex, (XElement) xn, null);
				}
			} else if (xn.getKind() == XMTEXT) {
				if (xn.minOccurs() > _counters[_actDefIndex]) {
					chkTextAbsence(_actDefIndex, (XData) xn, false, null);
				}
			}
			_actDefIndex = -1;
		}
		finishSelector();
		// check remaining part of model.
		int nextDefIndex = _nextDefIndex;
		while (_nextDefIndex < _childList.length) {
			short kind;
			XNode xnode;
			switch (kind = (xnode = _childList[_nextDefIndex]).getKind()) {
				case XMTEXT:
				case XMELEMENT: {
					if (_selector != null && _selector._selective) {
						if ((_selector.maxOccurs() <= 1 || !_selector._occur)
							&& _selector._count <= _selector.minOccurs()) {
							if (_selector._prev == null
								|| _selector._prev._count+1 <= _selector._prev.minOccurs()) {
								XSelector xsel = (XSelector) getDefElement(_selector._begIndex);
								if (xsel._onAbsence>=0
									&& (_selector._count==0 || _selector._count<_selector.minOccurs())) {
									debugXPos(XDDebug.ONABSENCE);
									exec(xsel._onAbsence, (byte)'U');
								} else if (_selector._count <_selector.minOccurs()) {
									String s = _selector._kind == XMCHOICE
										? "choice" : _selector._kind==XMMIXED ? "mixed" : "sequence";
									error(XDEF.XDEF555, s); //Minimum occurrence not reached for &{0}
								}
							}
						}
						_nextDefIndex = _selector._endIndex + 1;
						_selector = _selector._prev;
						break;
					} else {
						_nextDefIndex++;
					}
					continue;
				}
				case XMSELECTOR_END: {
					_actDefIndex = -1;
					if (_selector == null) {
						_nextDefIndex++;
						continue;
					}
					switch (_selector._kind) {
						case XMMIXED: {
							if (!_selector._occur) {// no variant reached
								if (_selector._count == 0) {
									if (_selector.minOccurs() > 0) {// not empty
										checkAbsence(_selector, null, true);
									}
								} else {
									checkAbsence(_selector, null, true);
								}
								if (_selector._prev == null) {
									_selector = null;
								} else {
									_selector._prev._occur|=_selector._count > 0;
									_selector = _selector._prev;
								}
								_nextDefIndex++;
								continue;
							}
							if (_selector._occur) {
								_selector._occur = false;
								_selector._count = 1;
								_nextDefIndex = _selector._begIndex + 1;
							} else {
								if (_selector._prev == null) {
									_selector = null;
								} else {
									_selector._prev._occur|=_selector._count > 0;
									_selector = _selector._prev;
								}
								_nextDefIndex++;
							}
							continue;
						}
						case XMCHOICE: {
							if (_selector._occur) {
								_selector._count = ++_counters[_selector._endIndex];
							}
							if (_selector.maxOccurs()<=1 || !_selector._occur) {
								if (!_selector._ignorable && _selector._count < _selector.minOccurs()) {
									error(XDEF.XDEF555, "choice"); //Minimum occurrence not reached for &{0}
									XSelector xsel = (XSelector) getDefElement(_selector._begIndex);
									debugXPos(XDDebug.ONABSENCE);
									if (xsel._onAbsence >= 0) {
										exec(xsel._onAbsence, (byte)'U');
									}
								}
								_selector.updateCounters();
								_nextDefIndex++;
								if (_selector._prev == null) {
									_selector = null;
								} else {
									_selector._prev._occur|=_selector._count > 0;
									_selector = _selector._prev;
								}
							} else {
								checkAbsence(_selector, new Counter(_selector._firstChild), true);
								if (_selector.maxOccurs() <= 1) {
									_nextDefIndex++;
									continue;
								}
								if (_selector.saveAndClearCounters()) {
									_selector._count++;
									_selector._occur = false;
								}
								_nextDefIndex = _selector._begIndex + 1;
							}
							continue;
						}
						default: {
							if (_selector.maxOccurs()<=1 || !_selector._occur) {
								if (_selector._prev == null) {
									_selector = null;
									_nextDefIndex++;
								} else {
									_selector._prev._occur |= _selector._occur || _selector._count>0;
									if (_selector._prev._kind!=XMSEQUENCE && _selector._count > 0) {
										_selector._prev._occur = true;
										_nextDefIndex = _selector._begIndex + 1;
									} else {
										_nextDefIndex++;
									}
									_selector.updateCounters();
									_selector = _selector._prev;
								}
							} else {
								checkAbsence(_selector, new Counter(_selector._firstChild), true);
								if (_selector.saveAndClearCounters()) {
									_selector._count++;
									_selector._occur = false;
								}
								_nextDefIndex = _selector._begIndex + 1;
							}
							continue;
						}
					}
				}
				case XMSEQUENCE:
				case XMMIXED:
				case XMCHOICE: {
					_actDefIndex = -1;
					if (_selector != null && _selector._selective) {
						if ((!_selector._ignorable && _selector._count < _selector.minOccurs())
							&& (_selector._prev == null || _selector._prev.minOccurs() >_selector._prev._count
							&& _selector._prev._begIndex==_selector._begIndex+1
							&& _selector._prev._endIndex==_selector._endIndex-1)
							&& (_selector.maxOccurs()<=1 || !_selector._occur)){
							//Minimum occurrence not reached for &{0}
							error(XDEF.XDEF555, xnode.getName().substring(1));
							XSelector xsel = (XSelector) xnode;
							if (xsel._onAbsence >= 0) {
								exec(xsel._onAbsence, (byte)'U');
							}
						}
						_nextDefIndex = _selector._endIndex + 1;
						_selector = _selector._prev;
						break;
					}
					if (_selector==null || _selector._begIndex!=_nextDefIndex) {
						_selector = new SelectorState(_selector, (XSelector) xnode);
					}
					_nextDefIndex++;
					continue;
				}
				default: throw new SRuntimeException(SYS.SYS066, "kind: " + kind); //Internal error&{0}{: }
			}
			break;
		}
		//check absence of items in root selection
		if ((_nextDefIndex = nextDefIndex) < _childList.length) {
			XSequence xs = new XSequence();
			xs.setOccurrence(1, 1);
			xs._begIndex = _nextDefIndex - 1;
			xs._endIndex = _childList.length;
			_selector = new SelectorState(null, xs);
			checkAbsence(_selector, new Counter(_element.getChildNodes().getLength()), false);
			_selector = null;
		}
	}

	/** Process text white spaces before processing.
	 * @param xd XData model or null.
	 * @param data String to be processed.
	 * @return text with processed white spaces.
	 */
	final String textWhitespaces(final XCodeDescriptor xd, final String data) {
		String result;
		if ((xd != null && xd._textWhiteSpaces != 0) ?
			xd._textWhiteSpaces  == 'T' : _xElement._textWhiteSpaces == 'T') {
			result = SUtils.trimAndRemoveMultipleWhiteSpaces(data);
		} else if ((xd != null && xd._trimText != 0) ? xd._trimText != 'F'
			: (_xElement._trimText != 0) ? _xElement._trimText != 'F'
			: _rootChkDocument._trimText != 'F') {
			result = data.trim();
		} else {
			result = data;
		}
		if (result.isEmpty() || xd != null && xd.isFixed()) {
			return result;
		}
		byte b = (xd != null && xd._textValuesCase != 0) ? xd._textValuesCase
			: _xElement._textValuesCase != 0 ? _xElement._textValuesCase
			: _rootChkDocument._xElement._textValuesCase;
		return b == 'T' ? result.toUpperCase() : b == 'F' ? result.toLowerCase() : result;
	}

	/** Destruct ChkElement. */
	private void closeChkElement() {//just let's gc do the job
		_scp.closeFinalList(getFinalList()); // close objects from final list
		for (CodeUniqueset x: _markedUniqueSets) {
			String s = x.checkNotMarked(this);
			if (!s.isEmpty()) {
				error(XDEF.XDEF524, x.getName(), s);//Not referred keys found in the uniqueSet &{0}&{1}{: }
			}
		}
		if (_scp.getXmlStreamWriter() != null) {
			//write the end of element if XML stream writer exists.
			try {
				_scp.getXmlStreamWriter().writeElementEnd();
			} catch (SRuntimeException ex) {
				putReport(ex.getReport());
			}
		}
		if (_xComponent != null) {
			if (_xComponent.xGetModelPosition().indexOf("/$any") > 0
				|| _xComponent.xGetModelPosition().endsWith("#*")) {
				if (!(_forget || _xElement._forget == 'T')) { // not forget
					_xComponent.xSetAny(_element);
				}
			}
			if (_xComponent.xGetParent() != null
				&& _xComponent != getParent().getXComponent()) {
				if (!(_forget || _xElement._forget == 'T')) { // not forget
					_xComponent.xGetParent().xAddXChild(_xComponent);
				}
			}
		}
		if (!getXDDocument().isCreateMode()
			&& (_forget || _xElement._forget == 'T' || _xComponent != null)) {
			// create mode is not set and forget or _xComponent != null
			updateElement(null);
			_parent.getChkChildNodes().remove(this);
			_chkChildNodes = null;
			_xElement = null;
			_element = null;
			_xonArray = null;
			_xonKey = null;
			_xonMap = null;
			_xonValue = null;
		}
		_xComponent = null;
		if (_variables != null) {
			for(int i = 0; i < _variables.length; i++) {
				XDValue x = _variables[i];
				if (x!=null && !x.isNull() && (x.getItemId()==X_UNIQUESET || x.getItemId() == X_UNIQUESET_M)){
					CodeUniqueset y = (CodeUniqueset)x;
					y.checkAndClear(_scp.getTemporaryReporter());
				}
				_variables[i] = null;
			}
			_variables = null;
		}
		_xPosOccur.clear();
		_childList = new XNode[0];
		_counters = new int[0];
		_actDefIndex = -1;
		_xPos = null;
		_elemValue = null;
		_sourceElem = null;
		_data = null;
		_parseResult = null;
		_attNames = null;
		_selector = null;
		_userObject = null;
		_attName = null;
		_attURI = null;
		_xdata = null;
		if (_boundKeys != null) {
			for (XDUniqueSetKey x: _boundKeys) {
				if (x != null) {
					x.resetKey();
				}
			}
		}
	}

	/** Mark unique set with this instance of ChkElement.
	 * @param us unique set.
	 */
	public final void addMarkedUniqueset(CodeUniqueset us) {
		_markedUniqueSets.add(us);
		us.setMarker(this);
	}

////////////////////////////////////////////////////////////////////////////////
// Methods to retrieve values from checked tree.
////////////////////////////////////////////////////////////////////////////////

	/** Look up for the X-position (XPos) of the element set by xPath. For look up is used the hash table with
	 * the XPaths and their occurrences.
	 * @param xPath the XPath to the current ChkElement (Element from the source XML document that is
	 * actually processed).
	 * @return the position of this element in the source XML document to complete XPath identifier.
	 */
	private int getElemXPos(Map<String, XPosInfo> xPosOccur, String xPath) {
		if(xPosOccur != null) { // never should happen!!
			XPosInfo xPathInfo;
			if ((xPathInfo = xPosOccur.get(xPath)) == null) { // first occurrence of the xPath
				xPosOccur.put(xPath, new XPosInfo());
				return 1;
			} else { // another (second and more) occurrence of the xPath
				return xPathInfo.addCount();
			}
		}
		// Never should happen - internal error
		//Fatal error&{0}{: }
		throw new SRuntimeException(XDEF.XDEF569, "ChkElement:getElemXPos: _xPathOccur == null");
	}

	/** Saved counter object.*/
	private static final class Counter {
		int _itemIdex;
		Counter(final int counter) {_itemIdex = counter;}
	}

	/** Class to represent short information about XPaths for all elements present in the input XML source.
	 * This class is not deleted after element processing when "forget" option is specified !!!
	 * This class is deleted (nulled) when the end of parent element is reached in the XML source.
	 * Maximum recommended size of object created from this class is 8 kB to avoid OutOfMemory exception
	 * by processing very large XML sources.
	 */
	private static final class XPosInfo {
		/** Field to count the amount of the same XPaths. */
		private int _counter;

		/** Creates info object in case of first occurrence of this XPath
		 * in the input XML source.
		 */
		XPosInfo() {_counter = 1;}

		/** Increase counter of occurrence of XPath .
		 * @return increased occurrence of this XPath .
		 */
		int addCount() {return ++_counter;}

		/** Decrease counter of occurrence of XPath.
		 * @return decreased occurrence of this XPath .
		 */
		int subCount() {return --_counter;}
	}

////////////////////////////////////////////////////////////////////////////////

	/** Add the new attribute to the current XXElement.
	 * @param qname The qualified name of attribute (including prefix).
	 * @param data The value of attribute.
	 * @param nsURI The value of namespace URI.
	 * @return true if attribute was created according to X-definition.
	 */
	@Override
	public final boolean addAttributeNS(final String nsURI, final String qname, final String data) {
		if (_element == null) {
			return _ignoreAll;
		}
		if ("xmlns".equals(qname) || qname.startsWith("xmlns:") || qname.startsWith("xml:")) {
			String uri = qname.startsWith("xml:")
				? XMLConstants.XML_NS_URI : XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			_element.setAttributeNS(uri, qname, data);
			return true;
		}
		if (_ignoreAll) {
			return true; //all checks areignored (undef element)
		}
		if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(nsURI)) {
			return false;
		}
		if (_nil) {// XML schema; xsi:nil & options nillable
			error(XDEF.XDEF525, qname, getPosMod(getXDPosition(), _xPos));//Attribute not allowed
			return false;
		}
		XData xatt = (nsURI != null) ? getXAttr(nsURI, qname) : getXAttr(qname);
		if (xatt == null) {
			xatt = _xElement.getDefAttr("$attr", -1); // any attr
			if (xatt == null && _xElement._moreAttributes == 'T') { // more attributes
				xatt = new XData(qname, nsURI, getXDPool(), XD_ATTR); // check not declared attributes
			}
		}
		String adata;
		if ((adata = attrWhitespaces(xatt, data)) == null) {
			removeAttr(nsURI, qname);
			return true;
		}
		String xPos = _xPos;
		_xPos += "/@" + qname;
		_attName = qname;
		_attURI = nsURI;
		_xdata = xatt;
		if (xatt!=null && xatt._match >=0 && !getXDDocument().isCreateMode()) {
			_elemValue = _element;
			_data = adata;
			XDValue item = exec(xatt._match, (byte) 'A');
			_elemValue = null;
			_data = null;
			_parseResult = null;
			if (item != null && !item.booleanValue()) {//delete it
				removeAttr(nsURI, qname);
				if (xatt.minOccurs() != XOccurrence.IGNORE) {
					if (xatt.minOccurs() != XOccurrence.ILLEGAL) {
						putTemporaryReport(Report.error(XDEF.XDEF525, //Attribute not allowed
							qname, getPosMod(getXDPosition(), _xPos)));
					}
					if (xatt._onIllegalAttr >= 0) {
						if (_clearReports) {
							clearTemporaryReporter();
						}
						exec(xatt._onIllegalAttr, (byte) 'A');
					}
				}
				copyTemporaryReports();
				_xdata = null;
				_parseResult = null;
				_xPos = xPos;
				return false;
			}
			copyTemporaryReports();
		}
		_parseResult = null;
		if (xatt != null) {
			String xname = xatt.getName();
			//let's register that we processed this attribute
			if (_attNames.contains(xatt.getName()) && !"$attr".equals(xname)
				&& xatt._acceptQualifiedAttr == 'T') {
				//Both, the qualified and unqualified attributes are not allowed with the option
				// acceptQualifiedAttr: &{0}
				error(XDEF.XDEF559, qname);
			}
			if ("$attr".equals(xname)) {
				if (!_attNames.contains("$attr")) {
					_attNames.add(xname);
				}
				xname += qname;
			}
			boolean result = true;
			switch (xatt.minOccurs()) {
				case XOccurrence.ILLEGAL:  break; // report as it is undefined
				case XOccurrence.IGNORE: // ignore
					_attName = null;
					_attURI = null;
					removeAttr(nsURI, qname);
					_attNames.add(xname);
					_xdata = null;
					_xPos = xPos;
					return true;
				default : {// required(1) or optional(0)
					_data = adata;
					debugXPos(XDDebug.INIT);
					if (xatt._init >= 0) {// execute "onInit" action
						_elemValue = _element;
						exec(xatt._init, (byte) 'A');
						copyTemporaryReports();
					}
					if (_data == null) { // value not exist
						if (xatt._onFalse >= 0) {
							String x = _data;
							_elemValue = _element;
							if (_clearReports) {
								clearTemporaryReporter();
							}
							exec(xatt._onFalse, (byte) 'A');
							updateAttrValue(xatt, x, nsURI, qname);
						}
						_attNames.add(xname);
						_parseResult = new DefParseResult(_data);
					} else {
						_elemValue = _element;
						// if the value is an ampty string and the option is set to "acceptEmptyAttributes"
						// at any level then set the result of the check method to "true" (do NOT
						// report and/or process an error)!
						if (_data.isEmpty()
							&& ((xatt._ignoreEmptyAttributes == 'A' || xatt._ignoreEmptyAttributes == 'P'
							&& xatt.isOptional()) || xatt._ignoreEmptyAttributes == 0
							&& (_xElement._ignoreEmptyAttributes=='A' ||_xElement._ignoreEmptyAttributes=='P'
							&& xatt.isOptional()) || _xElement._ignoreEmptyAttributes == 0
							&& (_rootChkDocument._ignoreEmptyAttributes=='A'
								|| _rootChkDocument._ignoreEmptyAttributes=='P' && xatt.isOptional()))) {
							//accept empty attributes
							_attNames.add(xname);
							_parseResult = new DefParseResult(""); // empty attr
						} else {
							debugXPos(XDDebug.PARSE);
							// now we are sure the length is > 0 because if the option was not set to
							// "acceptEmptyAttributes" then an empty attribute value was set to null and
							// the attribute had been ignored in attrWhitespaces!
							_attNames.add(xname);
							checkDatatype(xatt, false);
						}
						if (_parseResult.matches()) { // true
							clearTemporaryReporter();
							if (_data != null) {
								if (!_data.equals(adata)) {
									if (nsURI != null) {
										_element.setAttributeNS(nsURI, qname, _data);
									} else {
										_element.setAttribute(qname, _data);
									}
								}
							} else {
								removeAttr(nsURI, qname);
							}
							debugXPos(XDDebug.ONTRUE);
							if (xatt._onTrue >= 0) {
								String x = _data;
								exec(xatt._onTrue, (byte) 'A');
								updateAttrValue(xatt, x, nsURI, qname);
							}
						} else { // _parseResult.matches() == false
							// put error reports to chkElement
							debugXPos(XDDebug.ONFALSE);
							if (xatt._onFalse >= 0) {
								String x = _data;
								if (_clearReports) {
									clearTemporaryReporter();
								}
								exec(xatt._onFalse, (byte) 'A');
								if (x != data) { // data changed, even may be equal
									updateAttrValue(xatt, x, nsURI, qname);
								}
							} else {
								result = false; // an error found
								//copy reports from parsed result to the temporary reporter.
								_scp.getTemporaryReporter().addReports(_parseResult.getReporter());
								if (!chkTemporaryErrors()) {
									putTemporaryReport(Report.error(XDEF.XDEF515));//Incorrect value&{0}
								}
							}
							copyTemporaryReports();
						}
					}
					if (_data != null && !_data.equals(adata)) {
						if ((adata = attrWhitespaces(xatt, adata)) == null) {
							removeAttr(nsURI, qname);
							_attName = null;
							_attURI = null;
							_data = null;
							_parseResult = null;
							_attNames.add(xname);
							_xdata = null;
							_xPos = xPos;
							return result; // ignore empty attributes
						}
					} else {
						adata = _data;
					}
					if (_data != null && !_data.equals(adata)) {
						adata = attrWhitespaces(xatt, _data);
					}
					_attName = null;
					_attURI = null;
					_data = null;
					if (adata == null) {
						removeAttr(nsURI, qname);
						_xdata = null;
						_parseResult = null;
						_xPos = xPos;
						return !xatt.isRequired();
					}
					if (nsURI != null) {
						_element.setAttributeNS(nsURI, qname, adata);
					} else {
						_element.setAttribute(qname, adata);
					}
					_attNames.add(xname);
					_xdata = null;
					_xPos = xPos;
					return result;
				}
			}
		}
		if ((_xElement._moreAttributes == 'T' || _xElement._moreAttributes == 'I')
			&& (xatt == null || !xatt.isIllegal())) {
			//more attributes allowed, add attribute as it is no X-definition for this attribute
			if (nsURI != null) {
				if (_xElement._moreAttributes == 'I') {
					_element.removeAttributeNS(nsURI, qname);
				} else {
					_element.setAttributeNS(nsURI, qname, adata);
					if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				}
			} else {
				if (_xElement._moreAttributes == 'I') {
					_element.removeAttribute(qname);
				} else {
					_element.setAttribute(qname, adata);
					if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				}
			}
			_attName = null;
			_attURI = null;
			_xdata = null;
			_xPos = xPos;
			return true;
		}
		debugXPos(XDDebug.ONILLEGALATTR);
		_data = adata = null;
		//Attribute "&amp;{0}" not allowed
		putTemporaryReport(Report.error(XDEF.XDEF525, qname, getPosMod(getXDPosition(), _xPos)));
		if (xatt != null && xatt._onIllegalAttr >= 0) {
			if (_clearReports) {
				clearTemporaryReporter();
			}
			exec(xatt._onIllegalAttr, (byte) 'T');
			_parseResult = null;
		} else if (_xElement._onIllegalAttr >= 0) {
			if (_clearReports) {
				clearTemporaryReporter();
			}
			_elemValue = _element;
			_data = adata;
			exec(_xElement._onIllegalAttr, (byte) 'E');
		}
		copyTemporaryReports();
		if (_data != null) {
			if (nsURI != null) {
				_element.setAttributeNS(nsURI, qname, adata);
				if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
					_xComponent.xSetAttr(this, _parseResult);
				}
			} else {
				_element.setAttribute(qname, adata);
				if (_xComponent != null && getXMNode() != null && getXMNode().getXDPosition() != null) {
					_xComponent.xSetAttr(this, _parseResult);
				}
			}
		} else {
			if (nsURI != null) {
				_element.removeAttributeNS(nsURI, qname);
			} else {
				_element.removeAttribute(qname);
			}
		}
		_attName = null;
		_attURI = null;
		_data = null;
		_parseResult = null;
		_xdata = null;
		_xPos = xPos;
		return false;
	}

	/** Get name of actual node.
	 * @return The name of node.
	 */
	@Override
	public final String getNodeName() {
		if (getItemId() != XX_ELEMENT) {
			return _attName;
		} else {
			if (_element != null) {
				String s = _element.getLocalName();
				return s == null ? _element.getNodeName() : s;
			}
			return null;
		}
	}

	/** Get namespace URI of actual node.
	 * @return namespace URI or null.
	 */
	@Override
	public final String getNodeURI() {
		return (getItemId() != XX_ELEMENT) ? _attURI : _element != null ? _element.getNamespaceURI() : null;
	}

	/** Store model variable.
	 * @param name name of variable.
	 * @param val value to be stored.
	 */
	@Override
	final void storeModelVariable(final String name, final XDValue val) {
		int addr = findModelVariableOffset(name);
		if (addr < 0) {
			_parent.storeModelVariable(name, val);
		} else {
			_variables[addr] = val;
		}
	}

	/** Load model variable.
	 * @param name name of variable.
	 * @return loaded value.
	 */
	@Override
	public final XDValue loadModelVariable(final String name) {
		int addr = findModelVariableOffset(name);
		return addr < 0 ? _parent.loadModelVariable(name) : _variables[addr];
	}

	/** Set this element will be forgotten after being processed.*/
	@Override
	public final void forgetElement() {_forget = true;}

	/** Increase reference counter by one.
	 * @return The increased reference number.
	 */
	@Override
	final int incRefNum() {return _actDefIndex < 0 ? 0 : ++_counters[_actDefIndex];}

	/** Decrease reference counter by one.
	 * @return The increased reference number.
	 */
	@Override
	final int decRefNum() {return _actDefIndex < 0 ? 0 : --_counters[_actDefIndex];}

	/** Get reference counter of actual node
	 * @return The reference number.
	 */
	@Override
	public final int getRefNum() {return _actDefIndex < 0 ? 0 : _counters[_actDefIndex];}

	/** Get occurrence of actual element
	 * @return The reference number.
	 */
	@Override
	final int getOccurrence() {return _parent.getRefNum();}

	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	@Override
	final ChkElement getChkElement() {return this;}

	/** Prepare construction of the new element according to X-definition.
	 * @param qname qualified name of the element (prefixed).
	 * @param ns NameSpace URI of the element.
	 * @return created check element object.
	 */
	@Override
	public final XXElement prepareXXElementNS(final String ns, final String qname) {
		return createChkElement(ns == null ? _rootChkDocument._doc.createElement(qname)
			: _rootChkDocument._doc.createElementNS(ns,qname));
	}

	/** Prepare construction of the new element according to X-definition.
	 * @param name Tag name of the element.
	 * @return created check element object.
	 */
	@Override
	public final XXElement prepareXXElement(final String name) {return prepareXXElementNS(null, name);}

	/** Prepare construction of the new child according to X-definition.
	 * @param model child model.
	 * @return created XXElemnt element object.
	 */
	@Override
	public final XXElement createChildXXElement(final XMElement model) {
		String ns = model.getNSUri();
		String qname = model.getName();
		Element el = ns == null ? _rootChkDocument._doc.createElement(qname)
			: _rootChkDocument._doc.createElementNS(ns, qname);
		return chkElem((XElement) model, el);
	}

	/** Add the new attribute to the current XXElement.
	 * @param name name of attribute.
	 * @param data value of attribute.
	 * @return true if attribute was created according to X-definition.
	 */
	@Override
	public final boolean addAttribute(final String name, final String data) {
		return addAttributeNS(null, name, data);
	}

	/** This method is called when the end of the current element attribute list was parsed. The
	 * implementation may check the list of attributes and may invoke appropriate actions.
	 * @return true if element is compliant with X-definition.
	 */
	@Override
	public final boolean checkElement() {
		_parseResult = null;
		if (_attsChecked) {
			return true;
		}
		_attsChecked = true;
		if (_ignoreAll) { // all checks are ignored (undef element)
			return true;
		}
		boolean result = true;
		//check if there are missing required attributes
		XData[] xattrs = (XData[]) _xElement.getAttrs();
		String xPos = _xPos;
		for (int i = 0; i < xattrs.length && ! _nil; i++) {
			XData xatt = xattrs[i];
			_xdata = xatt;
			String xname = xatt.getName();
			if (xname.charAt(0) == '$') {//service attrs text, attr, textcontent
				continue; // TODO
			}
			boolean processed = false; // true if attribute was processed
			if (xatt.getNSUri() != null) {
				if (_element.hasAttributeNS(xatt.getNSUri(),
					xname.substring(xname.indexOf(':') + 1))) {
					processed = true;
				}
			} else if (_element.hasAttribute(xname)) {
				processed = true;
			} else if (_attNames.contains(xname)) {
				processed = true;
			}
			if (!processed) {
				_xPos = xPos + "/@" + xname;
				if (xatt._deflt >= 0) {// exec default method
					_data = null;
					_parseResult = null;
					_attName = xname;
					_elemValue = _element;
					XDValue value = exec(xatt._deflt, (byte) 'A');
					if (value != null) {
						_data = value.toString();
						checkDatatype(xatt, true);
						if (xatt.getNSUri() == null) {
							_element.setAttribute(xname, _data);
						} else {
							_element.setAttributeNS(xatt.getNSUri(), xname, _data);
						}
					}
					copyTemporaryReports();
					if (xatt.getNSUri() == null) {
						if (_element.hasAttribute(xname)) {
							continue;
						}
					} else {
						if (_element.hasAttributeNS(xatt.getNSUri(),
							xname.substring(xname.indexOf(':') + 1))) {
							continue;
						}
					}
				}
				//missing attribute
				debugXPos(XDDebug.ONABSENCE);
				if (xatt._onAbsence >= 0) {
					// onAbsence method
					_data = null;
					_parseResult = null;
					_attName = xname;
					_elemValue = _element;
					Report rep = null;
					if (xatt.minOccurs() == XData.REQUIRED) {
						rep = Report.error(XDEF.XDEF526, xname); //Missing required attribute &{0}
						putTemporaryReport(rep);
					}
					if (!_attNames.contains(xatt.getName())) {
						String uri = xatt.getNSUri(); // was not processed
						_xPos = xPos +"/@" + xname;
						exec(xatt._onAbsence, (byte) 'A');
						if (_data != null) {
							checkDatatype(xatt, true);
							if (uri == null) {
								_element.setAttribute(xname, _data);
							} else {
								_element.setAttributeNS(uri, xname, _data);
							}
							_attNames.add(xname);
						}
						if (uri == null && _element.hasAttribute(xname) || uri != null
							&& _element.hasAttributeNS(uri, xname.substring(xname.indexOf(':') + 1))) {
							//remove the message "missing"
							removeTemporaryReport(rep);
							continue; // attribute exists, don't invoke default
						}
					}
					if (xatt._deflt < 0) {
						copyTemporaryReports();
						continue; // skip default method
					}
					removeTemporaryReport(rep); // don't report "missing" twice
					copyTemporaryReports();
				}
				if (xatt.minOccurs() == XData.REQUIRED) { //no method called; put error
					error(XDEF.XDEF526, xname);//Missing required attribute &{0}
					result = false;
				}
			}
			_parseResult = null;
			if (xatt._onStartElement >= 0) { // execute onStartElement action
				_data = null;
				_attName = xname;
				_elemValue = _element;
				exec(xatt._onStartElement, (byte) 'A');
			}
		}
		_xPos = xPos;
		_parseResult = null;
		debugXPos(XDDebug.ONSTARTELEMENT);
		if (_xElement._onStartElement >= 0) {// exec on end of attr list
			_elemValue = _element;
			exec(_xElement._onStartElement, (byte) 'E');
			copyTemporaryReports();
			updateElement(_elemValue);
			if (_elemValue == null) {
				result = false;
			}
		}
		if (_scp.getXmlStreamWriter() != null) {
			try {
				_scp.getXmlStreamWriter().writeElementStart(_element);
			} catch (SRuntimeException ex) {
				putReport(ex.getReport());
			}
		}
		_xdata = null;
		_parseResult = null;
		return result;
	}

	/** Add new element as a child of the current element.
	 * Checks all attributes and child elements for occurrence.
	 * @return true if element was added and complies to X-definition.
	 */
	@Override
	public final boolean addElement() {
		if (_nil) {
			debugXPos(XDDebug.FINALLY);
			if (_xElement._finaly >= 0) {
				_elemValue = _element;
				exec(_xElement._finaly, (byte) 'E');
				copyTemporaryReports();
				updateElement(_elemValue);
			}
			_parent.incRefNum();
			closeChkElement();
			return true;
		}
		if (_ignoreAll) { //all checks are ignored (undef element)
			if (_element!=null && (_elemValue = _parent.getElement())!=null) {
				_elemValue.removeChild(_element);
			}
			//let's garbage collector do the job
			_chkChildNodes = null;
			_attNames = null;
			_xElement = null;
			_element = null;
			_childList = new XNode[0];
			_counters = new int[0];
			_xPos = null;
			_elemValue = null;
			_sourceElem = null;
			_data = null;
			_parseResult = null;
			_userObject = null;
			return true;
		}
		finishModel();
		boolean error = false;
		if (_element != null) {
			int n = _parent.getRefNum();
			if (n >= _xElement.maxOccurs()) {
				debugXPos(XDDebug.ONEXCESS);
				if (_xElement._onExcess >= 0) {
					_elemValue = _element;
					exec(_xElement._onExcess, (byte) 'E');
					copyTemporaryReports();
					updateElement(_elemValue);
				}
				if (_element != null) {
					_parent.incRefNum();
					//Maximum occurrence limit of &amp;{0} exceeded
					error(XDEF.XDEF558, "element " + _element.getTagName());
					error = true;
				}
			} else {
				_parent.incRefNum();
			}
		}
		if (_parent._parent != null && _xElement._xon > 0) {//not root; gen XON
			if (!_forget && _xElement._forget != 'T' && _element != null) {
				ChkElement chkEl = (ChkElement) _parent;
				Object value = X_MAP.equals(_element.getLocalName())
					?_xonMap : X_ARRAY.equals(_element.getLocalName()) ?_xonArray : _xonValue;
				if (chkEl._xonMap != null) {
					chkEl._xonMap.put(_xonKey, value);
				} else if (chkEl._xonArray != null) {
					chkEl._xonArray.add(value);
				} else {
					chkEl._xonValue = value;
				}
			}
		}
		if (_element != null) {
			String name;
			String xPos	= _xPos;
			XData[] xattrs = (XData[]) _xElement.getAttrs();
			int anyAttrs = 0;
			for (XData xatt : xattrs) {
				_xdata = xatt;
				name = xatt.getName();
				if ("$attr".equals(name)) {// any attribute
					_xPos = xPos;
					NamedNodeMap nm = _element.getAttributes();
					for (int j = 0, k = nm.getLength(); j < k; j++) {
						Node item = nm.item(j);
						name = item.getNodeName();
						int n = name.indexOf(':');
						if (n >= 0 && item.getNamespaceURI() != null) {
							name = name.substring(n+1);
						}
						_attName = name;
						if (!_attNames.contains(name)) {
							debugXPos(XDDebug.FINALLY);
							if (xatt._finaly >= 0) {
								String orig = _data = nm.item(j).getNodeValue();
								exec(xatt._finaly, (byte) 'A');
								copyTemporaryReports();
								anyAttrs++;
								if (_data == null) {
									_element.removeAttributeNode((Attr) item);
									anyAttrs--;
								} else if (!_data.equals(orig)) {
									((Attr) item).setValue(_data);
									_data = null;
									_parseResult = null;
								}
							} else {
								anyAttrs++;
							}
						}
					}
					if (anyAttrs < xatt.getOccurence().minOccurs()) {
						//Minimum number of attributes declared as "xd:attr" was not reached
						putTemporaryReport(Report.error(XDEF.XDEF531));
					} else if (anyAttrs > xatt.getOccurence().maxOccurs()) {
						//Maximum number of attributes declared as "xd:attr" was exceeded
						putTemporaryReport(Report.error(XDEF.XDEF532));
					}
				} else if (name.charAt(0) != '$') {// normal attribute
					_xPos = xPos + "/@" + name;
					debugXPos(XDDebug.FINALLY);
					if (xatt._finaly >= 0) {
						if (!_attNames.contains(name)) {
							//TODO
						}
						_attName = name;
						String orig;
						String uri;
						if ((uri = xatt.getNSUri()) == null) {
							_node = _element.getAttributeNode(name);
							orig = _data = _element.getAttribute(name);
						} else {
							_node = _element.getAttributeNodeNS(uri, name.substring(name.indexOf(':') + 1));
							orig = _data = _element.getAttributeNS(uri, name.substring(name.indexOf(':')+1));
						}
						exec(xatt._finaly, (byte) 'A');
						_node = null;
						copyTemporaryReports();
						if (!_data.equals(orig)) {
							if  (uri == null) {
								if (_data == null) {
									_element.removeAttribute(name);
								} else {
									_element.setAttribute(name, _data);
								}
							} else {
								if (_data == null) {
									_element.removeAttributeNS(uri, name.substring(name.indexOf(':') + 1));
								} else {
									_element.setAttributeNS(uri, name, _data);
								}
							}
						}
						_data = null;
						_parseResult = null;
					}
				}
			}
			XData xtxt;
			if ((xtxt = _xElement.getDefAttr("$text", -1)) != null) {
				_xPos = xPos + "/text()";
				if (_numText < xtxt.minOccurs()) {
					debugXPos(XDDebug.ONABSENCE);
					if (xtxt._onAbsence >= 0) {
						_elemValue = _element;
						_data = null;
						_parseResult = null;
						exec(xtxt._onAbsence, (byte) 'T');
						copyTemporaryReports();
						if (_data != null) {
							for (Node n=_element.getLastChild(); n != null;) {
								Node m = n.getPreviousSibling();
								_element.removeChild(n);
								n = m;
							}
							appendTextNode(_data, xtxt);
							_numText++;
							if (xtxt._finaly >= 0) {
								exec(xtxt._finaly, (byte) 'T');
							}
						}
					}
					if (_numText < xtxt.minOccurs()) {
						if (!_nil || _numText > 0) {
							error(XDEF.XDEF527, getPosMod(xtxt.getXDPosition(), null));//Missing required text
						}
					}
				}
				if (_numText > xtxt.maxOccurs() && !xtxt.isIllegal()) {
					//Maximum number of text nodes declared as "xd:text" was exceeded
					error(XDEF.XDEF533, getPosMod(xtxt.getXDPosition(), null));
				}
			}
			if ((xtxt = _xElement.getDefAttr("$textcontent", -1)) != null) {
				_xPos = xPos + "/text()";
				String orig = _data = KXmlUtils.getTextContent(_element);
				if (!orig.isEmpty()) {
					_numText = 1;
					debugXPos(XDDebug.PARSE);
					XDValue item = xtxt._check >= 0 ? exec(xtxt._check, (byte) 'T') : new DefBoolean(true);
					if (item != null && (item.getItemId() == XD_PARSERESULT
						? ((XDParseResult) item).matches() : item.booleanValue())) {
						clearTemporaryReporter();
						debugXPos(XDDebug.ONTRUE);
						if (xtxt._onTrue >= 0) {
							String x = _data;
							exec(xtxt._onTrue, (byte) 'T');
							copyTemporaryReports();
							if (x != _data && xtxt._check >= 0) { // _data was changed, even may be equal
								exec(xtxt._check, (byte) 'T');
								copyTemporaryReports();
							}
						}
					} else {
						debugXPos(XDDebug.ONFALSE);
						if (xtxt._onFalse >= 0) {
							String x = _data;
							if (_clearReports) {
								clearTemporaryReporter();
							}
							exec(xtxt._onFalse, (byte) 'T');
							if (x != _data && xtxt._check >= 0) { // _data was changed, even may be equal
								exec(xtxt._check, (byte) 'T');
							}
						} else {
							if (!chkTemporaryErrors()) {
								putTemporaryReport(Report.error(XDEF.XDEF515)); //Value error
							}
						}
						copyTemporaryReports();
						if (!orig.equals(_data)) {
							for (Node n=_element.getLastChild(); n != null;) {
								Node m = n = n.getPreviousSibling();
								if (n.getNodeType() == Node.TEXT_NODE
									|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
									_element.removeChild(n);
								}
								n = m;
							}
							appendTextNode(_data, xtxt);
						}
					}
					debugXPos(XDDebug.FINALLY);
					if (xtxt._finaly >= 0) {
						exec(xtxt._finaly, (byte) 'T');
						copyTemporaryReports();
					}
				} else if (_numText < xtxt.minOccurs()) {
					debugXPos(XDDebug.ONABSENCE);
					if (xtxt._onAbsence >= 0) {
						_elemValue = _element;
						_data = null;
						_parseResult = null;
						exec(xtxt._onAbsence, (byte) 'T');
						copyTemporaryReports();
						if (_data != null) {
							appendTextNode(_data, xtxt);
							_numText++;
							debugXPos(XDDebug.FINALLY);
							if (xtxt._finaly >= 0) {
								_data = KXmlUtils.getTextValue(_element);
								exec(xtxt._finaly, (byte) 'T');
								copyTemporaryReports();
							}
						}
					} else if (_numText < xtxt.minOccurs() && !_nil) {
						error(XDEF.XDEF527, getPosMod(xtxt.getXDPosition(), null));//Missing required text
					}
				}
			}
			_xdata = null;
			_parseResult = null;
			_xPos = xPos;
		}
		debugXPos(XDDebug.FINALLY);
		if (_xElement._finaly >= 0) {
			_elemValue = _element;
			exec(_xElement._finaly, (byte) 'E');
			copyTemporaryReports();
			updateElement(_elemValue);
		}
		closeChkElement();
		return !error;
	}

	/** Add new Text node to current element.
	 * @param data The value of text node.
	 * @throws SRuntimeException if an error occurs.
	 * @return true if text node is compliant with X-definition.
	 */
	@Override
	public final boolean addText(final String data) {
		if (_ignoreAll || _element  == null) {
			return true; //all checks are ignored (undef element)
		}
		if (_nil) {
			if (data.trim().isEmpty()) {
				return true;
			} else {
				error(XDEF.XDEF534); //Text value not declared
				return false;
			}
		}
		if ((_data = textWhitespaces(null, data)).isEmpty()) {
			_data = null;
			_parseResult = null;
			return true;
		}
		int nextDefIndex = _nextDefIndex, actDefIndex = _actDefIndex;
		_data = data;
		XData xtxt = (XData) findXNode(null);
		if (xtxt != null) {
			_data = textWhitespaces(xtxt, data);
		}
		XData xtxt1 = xtxt;
		_xdata = xtxt;
		String value = _data;
		String xPos = _xPos;
		String txtname = getTextPathIndex(actDefIndex);
		_xPos += "/text()" + txtname;
		if (xtxt1 != null) {// found
			txtname = "$text" + txtname;
			_attNames.add(txtname);
			if (value.isEmpty()) {
				debugXPos(XDDebug.ONABSENCE);
				if (xtxt1._onAbsence >= 0) {
					_elemValue = _element;
					exec(xtxt1._onAbsence, (byte) 'T');
				}
				debugXPos(XDDebug.FINALLY);
				if (_data != null) {
					if (xtxt1._finaly >= 0) {
						_elemValue = _element;
						exec(xtxt1._finaly, (byte) 'T');
					}
				}
				value = _data == null ? "" : _data;
				if (value.isEmpty()) {
					_data = null;
					_parseResult = null;
					_nextDefIndex = nextDefIndex;
					_actDefIndex = actDefIndex;
					_xPos = xPos;
					_xdata = null;
					return true;
				}
			} else {
				if (value.isEmpty()) {
					_data = null;
					_parseResult = null;
					_nextDefIndex = nextDefIndex;
					_actDefIndex = actDefIndex;
					_xPos = xPos;
					_xdata = null;
					return true;
				}
			}
			_numText++;
		} else {// not found
			if (value.trim().isEmpty()) {
				_data = null;
				_parseResult = null;
				// we ignore empty text
				_nextDefIndex = nextDefIndex;
				_actDefIndex = actDefIndex;
				_xPos = xPos;
				_xdata = null;
				return true;
			}
			_numText++;
			if (_xElement.hasDefAttr("$text")) {
				_nextDefIndex = nextDefIndex;
				_actDefIndex = actDefIndex;
				xtxt1 = _xElement.getDefAttr("$text", -1);
			} else if (_xElement._moreElements != 'T' && _xElement._moreElements != 'I'
				&& !_xElement.hasDefAttr("$textcontent") && _xElement._moreText != 'T'
				&& _xElement._moreText != 'I') {
				debugXPos(XDDebug.ONILLEGALTEXT);
				if (_xElement._onIllegalText >= 0) {
					_elemValue = _element;
					putTemporaryReport(Report.error(XDEF.XDEF534)); //Text value not declared
					exec(_xElement._onIllegalText, (byte) 'E');
					copyTemporaryReports();
				} else {
					if (_xElement._moreText == 'I') {
						_data = null;
					} else {
						error(XDEF.XDEF534); //Text value not declared
					}
				}
				_xdata = null;
				_parseResult = null;
				_xPos = xPos;
				return false;
			} else {// moreElements, textcontent
				if (_xElement._moreText == 'I') {
					_data = null;
					_xdata = null;
					_parseResult = null;
					_xPos = xPos;
					return true;
				}
				xtxt = xtxt1 = new XData("$text", null, _xElement.getXDPool(), XMTEXT); // dummy text
				if (_xElement.hasDefAttr("$textcontent")) { //copy option cdata!
					xtxt1._cdata = _xElement.getDefAttr("$textcontent", -1)._cdata;
				}
			}
		}
		_data = value;
		debugXPos(XDDebug.INIT);
		if (xtxt1._init >= 0) { // execute "onInit" action
			_elemValue = _element;
			exec(xtxt1._init, (byte) 'T');
			copyTemporaryReports();
		}
		int obligation;
		switch (obligation = xtxt1.minOccurs()) {
			case XOccurrence.IGNORE: // ignore
				_data = null;
				_parseResult = null;
				_xdata = null;
				_xPos = xPos;
				return true;
			case XOccurrence.ILLEGAL: // illegal
				if (value.isEmpty()) {
					_xdata = null;
					_xPos = xPos;
					return true;
				}
				debugXPos(XDDebug.ONILLEGALTEXT);
				if (xtxt1._onIllegalText >= 0) {
					if (!_clearReports) {
						putTemporaryReport(Report.error(XDEF.XDEF528)); //Illegal text
					}
					_elemValue = _element;
					_data = null;
					exec(xtxt1._onIllegalText, (byte) 'T');
					_parseResult = null;
					copyTemporaryReports();
					_xdata = null;
					_xPos = xPos;
					return true;
				}
				error(XDEF.XDEF528); //Illegal text
				_data = null;
				_parseResult = null;
				_xdata = null;
				_xPos = xPos;
				return false;
			default : // required(1) or optional(0)
				if (value.isEmpty()) {// the text node without text ???
					_data = null;
					_parseResult = null;
					debugXPos(XDDebug.ONFALSE);
					if (xtxt1._onFalse >= 0) {// value not exist
						_elemValue = _element;
						if (_clearReports) {
							clearTemporaryReporter();
						}
						exec(xtxt1._onFalse, (byte) 'T');
						if (_data!=null) {
							exec(xtxt1._check, (byte) 'T');
						}
						copyTemporaryReports();
					}
					_parseResult = new DefParseResult(_data);
				} else {
					debugXPos(XDDebug.PARSE);
					if (xtxt1._check >= 0) {
						XDValue item;
						_elemValue = _element;
						item = exec(xtxt1._check, (byte) 'T');
						if (item.getItemId() == XD_PARSERESULT) {
							_parseResult = (XDParseResult) item;
						} else {
							_parseResult = new DefParseResult(_data);
							if (!item.booleanValue()) {
								_parseResult.putDefaultParseError(); //XDEF515 .. Value error&{0}{ :}
							}
						}
						if (_parseResult.matches()) {
							clearTemporaryReporter(); // clear all error reports
							debugXPos(XDDebug.ONTRUE);
							if (xtxt1._onTrue >= 0) {
								String x = _data;
								exec(xtxt1._onTrue, (byte) 'T');
								if (x != _data) {//_data was changed, even may be equal
									item = exec(xtxt1._check, (byte) 'T');
									if (item.getItemId() == XD_PARSERESULT) {
										_parseResult = (XDParseResult) item;
									} else {
										_parseResult = new DefParseResult(_data);
										if (!item.booleanValue()) {
											_parseResult.putDefaultParseError(); //XDEF515 Value error&{0}{ :}
										}
									}
								}
								copyTemporaryReports();
							}
						} else {
							//call put error reports to chkElement
							if (!chkTemporaryErrors()) {
								for (Report rep: _parseResult.getReporter()) {
									putTemporaryReport(rep);
								}
							}
							debugXPos(XDDebug.ONFALSE);
							if (xtxt1._onFalse >= 0) {
								if (_clearReports) {
									clearTemporaryReporter();
								}
								String x = _data;
								exec(xtxt1._onFalse, (byte) 'T');
								if (x != _data && xtxt1._check > 0) {//_data was changed, even may be equal
									exec(xtxt1._check, (byte) 'T');
								}
							}
							copyTemporaryReports();
						}
					} else {
						_parseResult = new DefParseResult(_data);
						_parseResult.setEos();
						_parseResult.setParsedValue(_data);
//						org.xdef.XDParserAbstract.checkCharset(this, _parseResult); // check charset
						if (!_parseResult.matches()) { //Charset error detected, put error
							putTemporaryReport(_parseResult.getReporter().getReport());
							if (xtxt1._onFalse >= 0) {
								_elemValue = _element;
								String x = _data;
								if (_clearReports) {
									clearTemporaryReporter();
								}
								exec(xtxt1._onFalse, (byte) 'T');
								if (x != _data && xtxt1._check >= 0) {//_data was changed, even may be equal
									exec(xtxt1._check, (byte) 'T');

								}
							}
						} else if (xtxt1._onTrue >= 0) { //if check method is not specified call onTrue action
							debugXPos(XDDebug.ONTRUE);
							_elemValue = _element;
							String x = _data;
							exec(xtxt1._onTrue, (byte) 'T');
							copyTemporaryReports();
							if (x != _data && xtxt1._check >= 0) {//_data was changed, even may be equal
								exec(xtxt1._check, (byte) 'T');
							}
						}
					}
				}
				value = _data != null && !_data.equals(value) ? textWhitespaces(xtxt1, _data) : _data;
				if (value != null) {
					debugXPos(XDDebug.FINALLY);
					if (xtxt1._finaly >= 0) {
						_elemValue = _element;
						Node txt = appendTextNode(_data = value, xtxt1);
						exec(xtxt1._finaly, (byte) 'T');
						if ((value = _data) == null || value.isEmpty()) {
							_element.removeChild(txt);
						} else {
							txt.setNodeValue(value);
						}
					} else if (!value.isEmpty()) {
						appendTextNode(value, xtxt1);
						if (_xComponent != null && _parseResult.getParsedValue() != null) {
							_xComponent.xSetText(this, _parseResult);
						}
					}
					if (value != null && !value.isEmpty()) {
						if (_actDefIndex >= 0 && _childList[_actDefIndex].getKind() == XMTEXT) {
							int n = xtxt == xtxt1 ? incRefNum() : getRefNum();
							if (_actDefIndex > 0 && n > xtxt1.maxOccurs()) {
								error(XDEF.XDEF558, "text"); //Maximum occurrence limit of &amp;{0} exceeded
							}
						}
					}
					_data = null;
					_xdata = null;
					_xPos = xPos;
					return true;
				} else {
					_data = null;
					_parseResult = null;
					debugXPos(XDDebug.FINALLY);
					if (xtxt1._finaly >= 0) {
						_elemValue = _element;
						exec(xtxt1._finaly, (byte) 'T');
					}
					_xdata = null;
					_xPos = xPos;
					return obligation != XData.REQUIRED;
			}
		}
	}

	/** Add new Comment node to current element.
	 * @param data The value of Comment node.
	 * @return true if Comment node is compliant with X-definition.
	 */
	@Override
	public final boolean addComment(final String data) {return true;} //TODO

	/** Add new Processing instruction node to current element.
	 * @param name The name of the PI node.
	 * @param x The value of instruction part of the PI node.
	 * @throws SRuntimeException if an error occurs.
	 * @return true if PI node is compliant with X-definition.
	 */
	@Override
	public final boolean addPI(final String name, final String x) {return true;} //TODO

	/** Get text value of this node.
	 * @return The string with value of node.
	 */
	@Override
	public final String getTextValue() {return (getItemId() != XX_ELEMENT) ? _data : null;}

	/** Set text value to this node.
	 * @param data the text value to be set.
	 */
	@Override
	public final void setTextValue(final String data) {
		if (getItemId() != XX_ELEMENT) {
			_data = data;
		} else {
			//Illegal use of method: &{0}
			throw new SRuntimeException(SYS.SYS083, "setText");
		}
	}

	@Override
	public final short getItemId() {return _mode == 'T' ? XX_TEXT : _mode == 'A'? XX_ATTR : XX_ELEMENT;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public final XDValueType getItemType() {return _mode == 'T' ? XXTEXT : _mode == 'A'? XXATTR : XXELEMENT;}

	/** Get attribute with namespace from XXElement.
	 * @param uri The namespace of attribute.
	 * @param name The local name of attribute.
	 * @return value of attribute or the empty string if the attribute is legal otherwise throws the
	 * SRuntimeException.
	 * @throws SRuntimeException if the attribute is not legal in actual model.
	 */
	@Override
	public final String getAttributeNS(final String uri, final String name) {
		Attr att = (uri == null) ? _element.getAttributeNode(name) : _element.getAttributeNodeNS(uri,name);
		if (att != null) {
			return att.getValue();
		}
		//attribute not exist in element.
		XMElement xel = getXMElement();
		XMData xatt = (uri == null) ? xel.getAttr(name) : xel.getAttrNS(uri, name);
		//prepare path for error message
		if (xatt != null) {
			if (xatt.isIllegal()) {
				//Attempt to get illegal item
				throw new SRuntimeException(XDEF.XDEF582, getXPos() + "/@" + name);
			}
			return null; //attribute is defined but not exists
		} else if (xel.hasOtherAttrs()) {
			return null; //If X-definition has a VARIABLE_PART it makes no sense to check it more.
		}
		//Attempt to get undeclared item
		throw new SRuntimeException(XDEF.XDEF581, getXPos() + "/@" + name);
	}

	/** Get attribute from the XXElement object.
	 * @param name The name of attribute.
	 * @return The value of attribute or the empty string if the value
	 * doesn't exist or return null if required attribute is defined in the XXElement, however it does not
	 * exist in the actual element.
	 * @throws SRuntimeException if required attribute is not defined in the X-definition.
	 */
	@Override
	public final String getAttribute(final String name) {return getAttributeNS(null, name);}

	/** Get work element value.
	 * @return work element value.
	 */
	@Override
	public final Element getElemValue() {return _elemValue;}

	/** Set work element value.
	 * @param e The element.
	 */
	@Override
	final void setElemValue(final Element e) {_elemValue = e;}

	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	@Override
	public final XXElement getRootXXElement(){return _rootChkDocument._chkRoot;}

	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	@Override
	public final XXElement getXXElement() {return this;}

	/** Get associated XML node.
	 * @return the associated XML node.
	 */
	@Override
	public final Node getXMLNode() {return _node;}

	/** Get namespace context of corresponding XElement.
	 * @return namespace context of the parent element.
	 */
	@Override
	public final KNamespace getXXNamespaceContext() {return _xElement.getXDNamespaceContext();}

	/** Check if attribute is legal in the XXElement.
	 * @param name The name of attribute.
	 * @return true if and only if the attribute is legal in the XXElement, otherwise return false.
	 */
	@Override
	public final boolean checkAttributeLegal(final String name) {
		XData xatt = getXAttr(name);
		return xatt != null && !xatt.isIllegal();
	}

	/** Check if attribute with given namespace is legal in the XXElement.
	 * @param uri namespace URI.
	 * @param name name of attribute (optionally with prefix).
	 * @return true if and only if the attribute is legal in the XXElement, otherwise return false.
	 */
	@Override
	public final boolean checkAttributeNSLegal(final String uri, final String name) {
		XData xatt = uri == null || uri.isEmpty() ? getXAttr(name) : getXAttr(uri, name);
		return xatt != null && !xatt.isIllegal();
	}

	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	@Override
	public final XXNode[] getChildXXNodes() {
		XXNode[] result = new XXNode[_chkChildNodes.size()];
		_chkChildNodes.toArray(result);
		return result;
	}

	@Override
	final List<ChkElement> getChkChildNodes() {return _chkChildNodes;}

	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	@Override
	public final XMData getXMData() {return _xdata;}

	/** Get actual model.
	 * @return actual model.
	 */
	@Override
	public final XMNode getXMNode() {
		/** mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
		* 'D' - document, 'P' - processing instruction,'U' undefined. */
		return (_mode == (byte) 'A' || _mode == (byte) 'T') ? (XMNode) _xdata : (XMNode) _xElement;
	}

	/** Get XComponent.
	 * @return The XComponent object (may be null).
	 */
	@Override
	public final XComponent getXComponent() {return _xComponent;}

	/** Set XComponent.
	 * @param x XComponent object.
	 */
	@Override
	public final void setXComponent(final XComponent x) {_xComponent = x;}

	/** Get XON result of processed element.
	 * @return result of JSON/XON parsing or return null.
	 */
	@Override
	public Object getXon() {return _xonArray != null ? _xonArray : _xonMap != null ?_xonMap : _xonValue;}

////////////////////////////////////////////////////////////////////////////////

	/** This is object containing actual selector state. */
	final class SelectorState extends XOccurrence {
		/** Previous selector */
		SelectorState _prev;
		/** Saved counters */
		int[] _savedCounters;
		/** Where selector begins */
		int _begIndex;
		/** Where selector ends */
		int _endIndex;
		/** Index of first child in selector list */
		int _firstChild;
		/** Number of selector repetitions */
		int _count;
		/** Address of "finally" method or -1 */
		int _finallyCode;
		/** Kind of selector */
		short _kind;
		/** True if first item of sequence is selective */
		boolean _selective;
		/** True if selector is ignorable */
		boolean _ignorable;
		/** True if an error was reported */
		boolean _error;
		/** True if an item was recognized */
		boolean _occur;

		/** Create new SelectorState.
		 * @param prev previous SelectorState in chain or null.
		 * @param xs Selector from which SelectorState will be created.
		 */
		SelectorState(final SelectorState prev, final XSelector xs) {
			super(xs.minOccurs(), xs.maxOccurs());
			_prev = prev;
			_kind = xs.getKind();
			_begIndex = xs._begIndex;
			_endIndex = xs._endIndex;
			_ignorable = xs._ignorable;
			_selective = xs._selective;
			_count = 0;
			_finallyCode = xs._finaly;
			_firstChild = _element.getChildNodes().getLength();
		}

		/** Save actual counters and clear them. */
		final boolean saveAndClearCounters() {
			boolean result = false;
			int len = _endIndex - (_begIndex + 1);
			if (_savedCounters == null) {
				_savedCounters = new int[len];
				System.arraycopy(_counters, _begIndex + 1, _savedCounters, 0, len);
				for (int i = 0; i < len; i++) {
					if (_savedCounters[i] > 0) {
						result = true;
						break;
					}
				}
			} else {
				for (int i = 0, j = _begIndex + 1; i < len; i++,j++) {
					int z;
					if ((z = _counters[j]) > 0) {
						_savedCounters[i] += z;
						result = true;
					}
				}
			}
			Arrays.fill(_counters, _begIndex + 1, _endIndex, 0);
			_occur = false;
			return result;
		}

		/** Update counters form saved counters. */
		final boolean updateCounters() {
			if (_savedCounters == null) {
				return false;
			}
			boolean result = false;
			int len = _endIndex - (_begIndex + 1);
			for (int x = _begIndex + 1, y = 0; y < len; x++, y++) {
				int z;
				if ((z = _savedCounters[y]) > 0) {
					_counters[x] += z;
					result = true;
				}
			}
			_savedCounters = null;
			return result;
		}
	}
}
