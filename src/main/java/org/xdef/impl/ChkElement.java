package org.xdef.impl;

import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefParseResult;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.impl.xml.KNamespace;
import org.xdef.xml.KXmlUtils;
import org.xdef.component.XComponent;
import org.xdef.XDDebug;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.XDValueType;
import org.xdef.proc.XXData;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.impl.compile.CompileBase;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import javax.xml.XMLConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides validation of input data or it can be used as base for construction
 * of XML objects according to a X-definition.
 *  This code is nasty code in some parts, should be written better!
 * @author Vaclav Trojan
 */
public final class ChkElement extends ChkNode implements XXElement, XXData {

	/** Text value (of actual text node or attribute). It is used
	 * for communication with the XScript interpreter.
	 * Important note: it should be cleared after invocation of
	 * external methods - to allow gc to do the job!
	 */
	private String _data;
	/** Element value used for X-Script code.
	 * Important note: it should be cleared after invocation of
	 * external methods - to allow garbage collector to do the job!
	 */
	private Element _elemValue;
	/** The name of actually processed attribute for communication with
	 *  the X-Script interpreter. */
	private String _attName;
	/** The namespace URI of actually processed attribute for communication with
	 *  the X-Script interpreter. */
	private String _attURI;
	/** List of names of attributes. */
	private HashSet<String> _attNames;
	/** The Map to store the element XPath occurrence. */
	private final Map<String, XPosInfo> _xPosOccur;
	/** Array of X-definitions. */
	private XNode[] _defList;
	/** Array of occurrence counters. */
	private int[] _counters;
	/** Index to actual X-definition. */
	int _actDefIndex;
	/** Index to next X-definition. */
	int _nextDefIndex;
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
	/** The list of child check elements. */
	ArrayList<ChkElement> _chkChildNodes;
	/** If true the element attributes had been checked. */
	private boolean _attsChecked;
	 /** mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined. */
	private byte _mode;
	/** XComponent if exists or null. */
	private XComponent _xComponent;
	/** Model of the processed data object.*/
	XMData _xdata;

	/** The set containing marked unique sets. */
	final Set<CodeUniqueset> _markedUniqueSets = new HashSet<CodeUniqueset>();

	/** Creates a new empty instance of ChkElement - just for internal use.
	 * @param xelement X-definition of element.
	 * @param parent ChkNode parent.
	 * @param element element with attributes.
	 * @param ignoreAll if <tt>true</tt> ignore this and all child nodes.
	 */
	ChkElement(final ChkNode parent,
		Element element,
		final XElement xelement,
		boolean ignoreAll) {
		super(element==null ? xelement.getName(): element.getNodeName(),parent);
//		_sourceElem = _elemValue = null;  //Java makes it!
//		_selector = null; //Java makes it!
//		Arrays.fill(_counters, 0); //Java makes it!
//		_nextDefIndex = _numText = 0; //Java makes it!
//		_nil = false; //Java makes it!
		_element = element;
		_ignoreAll = ignoreAll || xelement.isIgnore() || xelement.isIllegal();
		if (xelement.isIgnore() || xelement.isIllegal()) {
			_forget = true;
		}
		_xElement = xelement;
		_xPosOccur = new LinkedHashMap<String, XPosInfo>();
		StringBuilder sb =
			new StringBuilder(_parent.getXPos()).append('/').append(_name);
		if (_parent.getParent() != null) {
			int xPosCnt =
				getElemXPos(((ChkElement)_parent)._xPosOccur, sb.toString());
			sb.append('[').append(String.valueOf(xPosCnt)).append(']');
		}
		_errCount=getReporter().getErrorCount()+_scp._reporter.getErrorCount();
		_xPos = sb.toString();
		_defList = _xElement._childNodes;
		_actDefIndex = -1; //index of actual X-definition
		_counters = new int[_defList.length + 1]; //one more for '*'
		_chkChildNodes = new ArrayList<ChkElement>();
		_attNames = new HashSet<String>();
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
				&& "true".equals(_element.getAttributeNS(
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil"))) {
				_nil = true;
			}
		}
		if (!_ignoreAll && getElement() != null
			&& (_xComponent = _parent.getXComponent()) != null) {
			_xComponent = _xComponent.xCreateXChild(this);
		}
	}

	/** Execute X-script from given address (with given type).
	 * @param addr address of script.
	 * @param type type of model ('E' - element, 'A' - attribute, 'T' text,
	 * otherwise 'U').
	 */
	final XDValue exec(final int addr, final byte type) {
		if (addr < 0) {
			return null;
		}
		setXXType(type);
		return _scp.exec(addr, this);
	}

	/** Get list of child nodes.
	 * @return list of child nodes.
	 */
	final XNode[] getDefList() {return _defList;}


	@Override
	/** Get name of actual node.
	 * @return The name of node.
	 */
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

	@Override
	/** Get namespace URI of actual node.
	 * @return namespace URI or <tt>null</tt>.
	 */
	public final String getNodeURI() {
		return (getItemId() != XX_ELEMENT)
			? _attURI : _element != null ? _element.getNamespaceURI() : null;
	}

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

	@Override
	/** Store model variable.
	 * @param name name of variable.
	 * @param val value to be stored.
	 */
	final void storeModelVariable(final String name, final XDValue val) {
		int addr = findModelVariableOffset(name);
		if (addr < 0) {
			_parent.storeModelVariable(name, val);
		} else {
			_variables[addr] = val;
		}
	}

	@Override
	/** Load model variable.
	 * @param name name of variable.
	 * @return loaded value.
	 */
	public final XDValue loadModelVariable(final String name) {
		int addr = findModelVariableOffset(name);
		return addr < 0 ? _parent.loadModelVariable(name) : _variables[addr];
	}

	@Override
	/** Set this element will be forgotten after being processed.*/
	public final void forgetElement() {_forget = true;}

	@Override
	/** Increase reference counter by one.
	 * @return The increased reference number.
	 */
	final int incRefNum() {
		return _actDefIndex < 0 ? 0 : ++_counters[_actDefIndex];
	}

	@Override
	/** Decrease reference counter by one.
	 * @return The increased reference number.
	 */
	final int decRefNum() {
		return _actDefIndex < 0 ? 0 : --_counters[_actDefIndex];
	}

	@Override
	/** Get reference counter of actual node
	 * @return The reference number.
	 */
	public final int getRefNum() {
		return _actDefIndex < 0 ? 0 : _counters[_actDefIndex];
	}

	/** Get reference counter of given node (with an index).
	 * @param index the index of inspected node.
	 * @return The reference number.
	 */
	public final int getRefNum(final int index) {return _counters[index];}

	/** Get counters of child nodes.
	 * @return array of counters of child nodes.
	 */
	final int[] getCounters() {return _counters;}

	@Override
	/** Get occurrence of actual element
	 * @return The reference number.
	 */
	final int getOccurrence() {return _parent.getRefNum();}

	@Override
	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	final ChkElement getChkElement() {return this;}

	@Override
	/** Prepare construction of the new element according to X-definition.
	 * @param qname qualified name of the element (prefixed).
	 * @param ns NameSpace URI of the element.
	 * @return created check element object.
	 */
	public final XXElement prepareXXElementNS(final String ns,
		final String qname) {
		return ns == null
			? createChkElement(_rootChkDocument._doc.createElement(qname))
			: createChkElement(_rootChkDocument._doc.createElementNS(ns,qname));
	}

	@Override
	/** Prepare construction of the new element according to X-definition.
	 * @param name Tag name of the element.
	 * @return created check element object.
	 */
	public final XXElement prepareXXElement(final String name) {
		return createChkElement(_rootChkDocument._doc.createElement(name));
	}

	@Override
	/** Prepare construction of the new child according to X-definition.
	 * @param model child model.
	 * @return created XXElemnt element object.
	 */
	public final XXElement createChildXXElement(final XMElement model) {
		String ns = model.getNSUri();
		String qname = model.getName();
		Element el = ns == null ? _rootChkDocument._doc.createElement(qname)
			: _rootChkDocument._doc.createElementNS(ns, qname);
		return chkElem((XElement) model, el);
	}

	/** Create check element object.
	 * @param element The element with attributes.
	 * @return created check element object.
	 */
	final ChkElement createChkElement(final Element element) {
		_data = null;
		_parseResult = null;
		if (_nil) {
			//Not allowed element '&{0}'
			error(XDEF.XDEF501, element.getNodeName());
			ChkElement result = new ChkElement(this,
				element, _xElement.createAnyDefElement(), true);
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
		} else if (result != null && (result.getXMElement().isIgnore()
			|| result.getXMElement().isIllegal())) {
			if (result.getXMElement().isIllegal()) {
				//Illegal element '&{0}'
				error(XDEF.XDEF557, element.getNodeName());
			}
			result =
				new ChkElement(this, el, _xElement.createAnyDefElement(), true);
			if (el != null) {
				el.removeChild(element);
			}
		}
		boolean ignoreAll = false;
		if (result == null) {
			if (_xElement._moreElements!='T' && _xElement._moreElements!='I') {
				ignoreAll = true;
				debugXPos(XDDebug.ONILLEGALELEMENT);
				if (_xElement._onIllegalElement >= 0) {
					_elemValue = _element;
					//Not allowed element '&{0}'
					putTemporaryReport(Report.error(XDEF.XDEF501,
						element.getNodeName()));
					exec(_xElement._onIllegalElement, (byte) 'E');
					copyTemporaryReports();
				} else if (textcontent == null) {
					//Not allowed element '&{0}'
					error(XDEF.XDEF501, element.getNodeName());
				}
				result = new ChkElement(this,
					element, _xElement.createAnyDefElement(), ignoreAll);
			} else {//moreElements || ignoreOther
				_nextDefIndex = nextDefIndex;
				_actDefIndex = actDefIndex;
				result = new ChkElement(this,
					element,
					_xElement.createAnyDefElement(),
					_ignoreAll || ignoreAll || _xElement._moreElements == 'I');
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
			putTemporaryReport(_counters[index] == 0 ?
				//Required element '&{0}' is missing
				Report.error(XDEF.XDEF539,
					xelem.getName() + "&{xdpos}" + xelem.getXDPosition()) :
				//Minimum occurrence not reached for &amp;{0}
				Report.error(XDEF.XDEF555, xelem.getName() +
					"&{xpath}" + _xPos + "/" + xelem.getName()
					+ "&{xdpos}" + xelem.getXDPosition()));
		}
		copyTemporaryReports();
	}

	/** Get SqId of XElement.
	 * @return SqId of XElement.
	 */
	final int getSqId() {return _xElement.getSqId();}

	private String getTextPathIndex(final int index) {
		int counter = 0;
		for (int i = 0; i < index; i++) {
			if (_defList[i].getKind() == XNode.XMTEXT) {
				counter++;
			}
		}
		return (counter > 0) ? "[" + (counter+1) + "]" : "";
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
		if (!_attNames.contains(txtname)
			&& xtxt._onAbsence >= 0) {//exec onAbsence
			_attNames.add(txtname);
			_elemValue = _element;
			exec(xtxt._onAbsence, (byte) 'T');
			if (_data != null) {
				checkDatatype(xtxt, true);
			}
			copyTemporaryReports();
		} else if (!ignoreAbsence && _data == null
			&& xtxt.minOccurs() >= XData.REQUIRED && !_nil) {
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
				NodeList nl = _element.getChildNodes();
				Node txt = xtxt._cdata == 'T'
					? _rootChkDocument._doc.createCDATASection(_data)
					: _rootChkDocument._doc.createTextNode(_data);
				if (orig == null) {
					if (c == null) {
						_element.appendChild(txt);
					} else {
						_element.insertBefore(txt, nl.item(c._itemIdex));
						c._itemIdex++;
					}
					incRefNum();
				} else {
					_element.replaceChild(txt, nl.item(c._itemIdex));
				}
			}
		} else if (orig != null) {
			NodeList nl = _element.getChildNodes();
			_element.removeChild(nl.item(c._itemIdex));
			c._itemIdex--;
			decRefNum();
			_data = null;
			_parseResult = null;
		}
		_xPos = xPos;
		_xdata = null;
		_parseResult = null;
	}

	private boolean isEmptyGroup(final int begIndex, final int endIndex) {
		for (int i = begIndex; i <= endIndex; i++) {
			switch (_defList[i].getKind()) {
				case XNode.XMSEQUENCE:
				case XNode.XMCHOICE:
				case XNode.XMMIXED:
				case XNode.XMSELECTOR_END:
					continue;
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
	 * @param skipSelectors if <tt>true</tt> the internal selectors are skipped.
	 * @return <tt>true</tt> if nonempty content is required.
	 */
	private boolean checkSequenceAbsence(final SelectorState selector,
		final Counter c,
		final boolean skipSelectors) {
		boolean required = selector.minOccurs() > 0;
		if (selector._selective || !required && !selector._occur) {
			return false;
		}
		int endIndex = selector._endIndex;
		for (int i = selector._begIndex + 1; i < endIndex; i++) {
			XNode xnode;
			switch ((xnode = _defList[i]).getKind()) {
				case XNode.XMTEXT: {
					chkTextAbsence(i, (XData) xnode, false, c);
					required &= ((XData) xnode).minOccurs() > 0;
					if (c != null) {
						c._itemIdex += _counters[i];
					}
					continue;
				}
				case XNode.XMELEMENT: {
					chkElementAbsence(i, (XElement) xnode, c);
					required &= ((XElement) xnode).minOccurs() > 0;
					if (c != null) {
						c._itemIdex += _counters[i];
					}
					continue;
				}
				case XNode.XMSEQUENCE:
				case XNode.XMCHOICE:
					required &= ((XSelector) xnode).minOccurs() > 0;
					if (skipSelectors) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_defList[i]).getKind()) {
								case XNode.XMCHOICE:
								case XNode.XMMIXED:
								case XNode.XMSEQUENCE:
								case XNode.XMSELECTOR_END:
									continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s =
							new SelectorState(selector, (XSelector) xnode);
						required =
							s._kind == XNode.XMCHOICE && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skipSelectors);
						i = s._endIndex;
					}
					continue;
				case XNode.XMMIXED:
					if (skipSelectors) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_defList[i]).getKind()) {
								case XNode.XMCHOICE:
								case XNode.XMMIXED:
								case XNode.XMSEQUENCE:
								case XNode.XMSELECTOR_END:
									continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s =
							new SelectorState(selector, (XSelector) xnode);
						required = (s._kind==XNode.XMCHOICE) && s.minOccurs()>0;
						required &= checkAbsence(s, c, skipSelectors);
						i = s._endIndex;
					}
					continue;
				case XNode.XMSELECTOR_END:
					if (skipSelectors) {
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
	 * @param skipSelectors if <tt>true</tt> the internal selectors are skipped.
	 * @return <tt>true</tt> if nonempty content is required.
	 */
	private boolean checkMixedAbsence(final SelectorState selector,
		final Counter c,
		final boolean skipSelectors) {
		int endIndex = selector._endIndex;
		int begIndex = selector._begIndex + 1;
		boolean required = selector.minOccurs() > 0;
		boolean empty = isEmptyGroup(begIndex, endIndex);
		if (empty) {
			XSelector xs = (XSelector) _defList[selector._begIndex];
			if (xs._onAbsence >= 0) {
				if (skipSelectors || !required) {
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
				switch ((xnode = _defList[i]).getKind()) {
					case XNode.XMTEXT: {
						chkTextAbsence(i, (XData) xnode, false, c);
						required &= ((XData) xnode).minOccurs() > 0;
						if (c != null) {
							c._itemIdex += _counters[i];
						}
						continue;
					}
					case XNode.XMELEMENT: {
						required &= ((XElement) xnode).minOccurs() > 0;
						chkElementAbsence(i, (XElement) xnode, c);
						if (c != null) {
							c._itemIdex += _counters[i];
						}
						continue;
					}
					case XNode.XMCHOICE: {
						XChoice xch = (XChoice) xnode;
						required &= xch.minOccurs() > 0;
						int j = (xch)._endIndex;
						if (_counters[j] < xch.minOccurs()) {
							//Minimum occurrence not reached for &{0}
							error(XDEF.XDEF555, "choice");
							if (xch._onAbsence >= 0) {
								exec(xch._onAbsence, (byte)'U');
							}
						}
						i = j;
						continue;
					}
					case XNode.XMSEQUENCE:
						required &= ((XSelector) xnode).minOccurs() > 0;
						if (skipSelectors) {
							int j = ((XSelector) xnode)._endIndex;
							while (++i < j) {
								switch ((_defList[i]).getKind()) {
									case XNode.XMCHOICE:
									case XNode.XMMIXED:
									case XNode.XMSEQUENCE:
									case XNode.XMSELECTOR_END:
										continue;
									default:
										if (c != null) {
											c._itemIdex += _counters[i];
										}
								}
							}
						} else {
							SelectorState s =
								new SelectorState(selector, (XSelector) xnode);
							required =
								s._kind == XNode.XMCHOICE && s.minOccurs() > 0;
							required &= checkAbsence(s, c, skipSelectors);
							i = s._endIndex;
						}
						continue;
					case XNode.XMMIXED:
						if (skipSelectors) {
							int j = ((XSelector) xnode)._endIndex;
							while (++i < j) {
								switch ((_defList[i]).getKind()) {
									case XNode.XMCHOICE:
									case XNode.XMMIXED:
									case XNode.XMSEQUENCE:
									case XNode.XMSELECTOR_END:
										continue;
									default:
										if (c != null) {
											c._itemIdex += _counters[i];
										}
								}
							}
						} else {
							SelectorState s =
								new SelectorState(selector, (XSelector) xnode);
							required =
								(s._kind==XNode.XMCHOICE) && s.minOccurs() > 0;
							required &= checkAbsence(s, c, skipSelectors);
							i = s._endIndex;
						}
						continue;
					case XNode.XMSELECTOR_END:
						if (skipSelectors) {
							continue;
						}
						return required;
					default:
				}
			}
		}
		if (required &&	!selector._occur &&	selector._count == 0) {
			if (!empty && selector._prev != null
				&& selector._prev._count >= selector._prev.minOccurs()) {
				return required;
			}
			//Sequence "xd:mixed" has no required item
			error(XDEF.XDEF520, "&{xdpos}"
				+ _defList[selector._begIndex].getXDPosition()
				+ "&{xpath}" + _xPos);
		}
		return required;
	}

	/** Check occurrences in a selector of choice.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skipSelectors if <tt>true</tt> the internal selectors are skipped.
	 * @return <tt>true</tt> if nonempty content is required.
	 */
	private boolean checkChoiceAbsence(final SelectorState selector,
		final Counter c,
		final boolean skipSelectors) {
		if (selector.minOccurs() <= 0) {
			return false; // not required
		}
		boolean required = selector.minOccurs() > 0;
		int endIndex = selector._endIndex;
		for (int i = selector._begIndex + 1; i < endIndex; i++) {
			XNode xnode;
			switch ((xnode = _defList[i]).getKind()) {
				case XNode.XMTEXT: {
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
				case XNode.XMELEMENT: {
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
				case XNode.XMSEQUENCE:
				case XNode.XMCHOICE:
					required &= ((XSelector) xnode).minOccurs() > 0;
					if (skipSelectors) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_defList[i]).getKind()) {
								case XNode.XMCHOICE:
								case XNode.XMMIXED:
								case XNode.XMSEQUENCE:
								case XNode.XMSELECTOR_END:
									continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s =
							new SelectorState(selector, (XSelector) xnode);
						required =
							s._kind == XNode.XMCHOICE && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skipSelectors);
						i = s._endIndex;
					}
					continue;
				case XNode.XMMIXED:
					if (skipSelectors) {
						int j = ((XSelector) xnode)._endIndex;
						while (++i < j) {
							switch ((_defList[i]).getKind()) {
								case XNode.XMCHOICE:
								case XNode.XMMIXED:
								case XNode.XMSEQUENCE:
								case XNode.XMSELECTOR_END:
									continue;
								default:
									if (c != null) {
										c._itemIdex += _counters[i];
									}
							}
						}
					} else {
						SelectorState s =
							new SelectorState(selector, (XSelector) xnode);
						required =
							(s._kind == XNode.XMCHOICE) && s.minOccurs() > 0;
						required &= checkAbsence(s, c, skipSelectors);
						i = s._endIndex;
					}
					continue;
				case XNode.XMSELECTOR_END:
					if (skipSelectors) {
						continue;
					}
					return required;
				default:
			}
		}
		if (!skipSelectors && selector._occur == false && selector._count == 0
			&& required) {
			// do not report error if onAbsence
			if (((XSelector) _defList[selector._begIndex])._onAbsence < 0) {
				//Missing required items in a section
				error(XDEF.XDEF541, "&{xdpos}(<xd:choice>)&{xpath}" +_xPos);
			}
		}
		return required;
	}

	/** Check occurrences in a selector.
	 * @param selector Selector of group to be investigated.
	 * @param c Counter (index) of first item of the group.
	 * @param skipSelectors if <tt>true</tt> the internal selectors are skipped.
	 * @return <tt>true</tt> if nonempty content is required.
	 */
	final boolean checkAbsence(final SelectorState selector,
		final Counter c,
		final boolean skipSelectors) {
		switch (selector._kind) {
			case XNode.XMCHOICE:
				return checkChoiceAbsence(selector, c, skipSelectors);
			case XNode.XMMIXED:
				return checkMixedAbsence(selector, c, skipSelectors);
		}
		return checkSequenceAbsence(selector, c, skipSelectors);
	}

	/** Finish processing of a group.
	 * @return true if all models are processed.
	 */
	final boolean finishGroup() {
		_actDefIndex = -1;
		int finaly = _selector._finallyCode;
		debugXPos(XDDebug.SELECTORCREATE);
		if (_selector._kind == XNode.XMMIXED) {
			if (!_selector._occur) {//no variant reached
				if (_selector._count == 0) {
					if (_selector.minOccurs() > 0) {
						if (_selector._prev == null
							|| _selector._prev._kind == XNode.XMSEQUENCE) {
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
				return false;
			}
			if (_selector._occur) {
				_selector._occur = false;
				_counters[_nextDefIndex] = _selector._count = 1;
				_nextDefIndex = _selector._begIndex + 1;
			} else {
				if (_selector._prev != null) {
					_selector._prev._occur |= _selector._count > 0;
				}
				_selector = _selector._prev;
				_nextDefIndex++;
			}
			return false;
		}
		if (_selector._kind == XNode.XMCHOICE) {
			if (_selector._occur) {
				_selector._count = ++_counters[_nextDefIndex];
			}
			if (_selector.maxOccurs() <= 1 || !_selector._occur) {
				if (_selector._count < _selector.minOccurs()
					&& (_selector._prev == null
					|| _selector._prev.minOccurs() > _selector._prev._count
					&& _selector._prev._begIndex == _selector._begIndex + 1
					&& _selector._prev._endIndex == _selector._endIndex - 1)) {
					//Minimum occurrence not reached for &{0}
					error(XDEF.XDEF555, "choice");
					XSelector xsel =
						(XSelector) getDefElement(_selector._begIndex);
					if (xsel._onAbsence >= 0) {
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
				return false;
			} else {
				checkAbsence(_selector,
					new Counter(_selector._firstChild), true);
				if (_selector.maxOccurs() <= 1) {
					if (finaly >= 0) {
						exec(finaly, (byte) 'U');
					}
					_nextDefIndex++;
					return false;
				}
				// Choice was not finished, will continue
				if (_selector.saveAndClearCounters()) {
					_selector._count++;
					_selector._occur = false;
				}
				_nextDefIndex = _selector._begIndex + 1;
				return false;
			}
		} else {// _selector._kind == XNode.X_SEQUENCE
			if (_selector._occur) {
				checkAbsence(_selector,
					new Counter(_selector._firstChild), true);
				if (_selector._count >= _selector.maxOccurs() -1) {
					_selector._count++;
					_selector.updateCounters(); //maximum reached
					_nextDefIndex++;
					if (_selector._prev != null) {
						_selector._prev._occur = true;
					}
					_selector = _selector._prev;
					if (finaly >= 0) {
						exec(finaly, (byte)'U');
					}
					return false;
				}
			}
			if (_selector.maxOccurs() <= 1 || !_selector._occur) {
				if (_selector._prev == null) {
					if (!_selector._ignorable
						&& _selector.minOccurs() > 0) {
						if (!_selector._occur) {//was not checked
							checkAbsence(_selector,
								new Counter(_selector._firstChild), true);
						}
					}
					if (finaly >= 0) {
						exec(finaly, (byte) 'U');
					}
					_selector = null;
					_nextDefIndex++;
				} else {
					_selector._prev._occur |=
						_selector._occur || _selector._count > 0;
					if (_selector._prev._kind != XNode.XMSEQUENCE
						&& _selector._count > 0) {
						_selector._prev._occur = true;
						_nextDefIndex =
							_selector._prev._begIndex + 1;
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
				return false;
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
				return false;
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
					if (value != null && !value.isNull()
						&& !value.booleanValue()) {
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
			if (xs.getKind() == XNode.XMCHOICE) {
				if (_counters[_selector._endIndex] >= _selector.maxOccurs()) {
					_nextDefIndex = _selector._endIndex + 1;
				}
			}
			return true;
	}

	/** Search XNode in the list of nodes.<p/>
	 * This is very tricky method. It is necessary to know that it is invoked
	 * when a child node occurs. There are important variables:<p/>
	 * _defList .. array of XNodes<br/>
	 * _counters .. array of occurrence counters<br/>
	 * _actDefIndex .. -1 or index to actually processed XNode item<br/>
	 * _nextDefIndex .. index to the XNode item to be inspected.<br/>
	 * _selector .. container of information about actual selection block.<br/>
	 * @param el the element or <tt>null</tt> it text node is processed.
	 * @return XNode or ChkElement object or <tt>null</tt>.
	 */
	private Object findXNode(final Element el) {
		XNode xn;
		ChkElement result;
		int defLength = _defList.length;
		int lastNextDefIndex = _nextDefIndex;
		int lastActDefIndex = _actDefIndex;
		if (el == null) { // element is null => text node
			if (_nextDefIndex < defLength
				&& _defList[_nextDefIndex].getKind() != XNode.XMTEXT) {
				if ((xn = _xElement.getDefAttr("$text", -1)) != null) {
					return xn;
				} else if ((xn=_xElement.getDefAttr("$textcontent",-1))!=null) {
					return new XData( //dummy, just process string()
						"$text", null, xn.getDefPool(), XNode.XMTEXT);
				}
			}
		} else if (_actDefIndex >= 0
			&& (xn = _defList[_actDefIndex]).getKind() == XNode.XMELEMENT
			&& xn.maxOccurs() > 1
			&& (_selector == null || _selector._kind != XNode.XMMIXED)) {
			if ((result = chkElem((XElement) xn, el)) != null) {
				// repeated nodes
				if (xn.maxOccurs() != 1 || xn.minOccurs() != 1) {//???template
					if (_counters[_actDefIndex] < xn.maxOccurs()
						|| _actDefIndex+1 >= defLength) {
						// max occurrence not reached or in the X-definition
						// not follows a XElement node
						return result;
					}
					// maxOccurrence exceeded, so check if the next node in
					// is an element with the same name.
					XNode x = _defList[_actDefIndex+1];
					if (x.getKind() != XNode.XMELEMENT
						|| !xn.getName().equals(x.getName())) {
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
			if (_selector._kind == XNode.XMMIXED) {
				// TODO xx if mixed is finished set endidex ??
				_nextDefIndex = _selector._begIndex + 1;
			}
		}
		_actDefIndex = -1;
		while (_nextDefIndex < defLength) {
			short kind;
			switch (kind = (xn = _defList[_nextDefIndex]).getKind()) {
				case XNode.XMTEXT: {
					if (el == null) {// is text node
						int oldAefIndex = _actDefIndex;
						_actDefIndex = _nextDefIndex++;
						XData xd = (XData) xn;
						if (xd._match >= 0 && !getXDDocument().isCreateMode()) {
							_elemValue = _element;
							XDValue item = exec(xd._match, (byte) 'T');
							copyTemporaryReports();
							if (item == null || !item.booleanValue()) {
								_actDefIndex = oldAefIndex;
								continue;
							}
						}
						if (_selector != null) {
							_selector._occur = true; //found
							if (_selector._kind == XNode.XMCHOICE) {
								_nextDefIndex = _selector._endIndex;
							} else if (_selector._prev != null
								&& _defList[_selector._prev._begIndex]
									.maxOccurs() > 0
								&& _selector._kind == XNode.XMMIXED) {
								// test if full xx
								boolean all = true;
								for (int i = _selector._begIndex + 1;
									i < _selector._endIndex; i++) {
									if (_counters[i]<_defList[i].maxOccurs()) {
										all = false;
										break;
									}
								}
								if (all) {
									finishGroup();
									_nextDefIndex = _selector._endIndex + 1;
								}
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
				case XNode.XMELEMENT: {
					XElement xel = (XElement) xn;
					result = null;
					if (el != null) { // not text node (element)
						int oldAefIndex = _actDefIndex;
						_actDefIndex = _nextDefIndex; // save actual index
						if ((result = chkElem(xel, el)) != null) {
							_nextDefIndex++;
							if (_selector != null) {
								_selector._occur = true;
							}
							if (_selector != null) {
								if (_selector._kind == XNode.XMCHOICE) {
									_nextDefIndex = _selector._endIndex;
								} else if (_selector._prev != null
									&& _defList[_selector._prev._begIndex]
										.maxOccurs() > 0
									&& _selector._kind == XNode.XMMIXED) {
									// test if full xx
									boolean all = true;
									for (int i = _selector._begIndex + 1;
										i < _selector._endIndex; i++) {
										if (_counters[i] <
											_defList[i].maxOccurs()) {
											all = false;
											break;
										}
									}
									if (all) {
										finishGroup();
										_nextDefIndex = _selector._endIndex + 1;
									}
								}
							}
							return result;
						}
						_actDefIndex = oldAefIndex; // reset actual index
					} else if (xel.minOccurs() <= 0) {
						// el == null => text node and this element not required
						_nextDefIndex++; // next item
						continue;
					}
					if (_selector == null
						|| _selector._kind == XNode.XMSEQUENCE
						&& !_selector._selective && _selector._prev == null) {
						int index = _nextDefIndex;
						int counter = _counters[index];
						if (counter == 0 && xel._onAbsence >= 0
							&& xel.minOccurs() == 0) {
							// not required element, however execute onAbsence
							chkElementAbsence(index, xel, null);
							_nextDefIndex++;
							continue;
						}
						if (counter < xel.minOccurs()) {
							//required element is missing
							while (_nextDefIndex + 1 < _defList.length) {
								XNode x = _defList[_nextDefIndex + 1];
								if (x.getKind()==XNode.XMELEMENT && el!=null) {
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
					} else if (_selector._kind == XNode.XMSEQUENCE
						&& _selector._selective
						&& _nextDefIndex == _selector._begIndex + 1) {
						_nextDefIndex = _selector._endIndex;
						continue;
					}
					_nextDefIndex++;
					continue;
				}
				case XNode.XMSELECTOR_END: {
					if (_selector == null) {//???
						_nextDefIndex++;
						return null;
					}
					if (el == null && _selector._prev == null
						&& _selector._kind == XNode.XMMIXED
						&& (_nextDefIndex == defLength -1
						|| _defList[_nextDefIndex+1].getKind()!=XNode.XMTEXT)) {
						//just to improve error reporting ???
						_nextDefIndex = lastNextDefIndex;
						_actDefIndex = lastActDefIndex;
						return null;
					}
					finishGroup();
					continue;
				}
				case XNode.XMSEQUENCE:
				case XNode.XMMIXED:
				case XNode.XMCHOICE: {
					createGroup((XSelector) xn);
					continue;
				}
				default: //error - unknown kind
					//Internal error&{0}{: }
					throw new SRuntimeException(SYS.SYS066,
						"Xdefinifion - ChkElement, unknown item: "
							+ kind + " " + xn);
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
		NodeList nl = _element.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
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

	private void appendTextNode(final String data, final XData xtxt) {
		Node txt = xtxt._cdata == 'T'
			? _rootChkDocument._doc.createCDATASection(data)
			: _rootChkDocument._doc.createTextNode(data);
		_element.appendChild(txt);
		if (_scp.getXmlStreamWriter() != null) {
			try {
				_scp.getXmlStreamWriter().writeNode(txt);
			} catch (SRuntimeException ex) {
				putReport(ex.getReport());
			}
		}
	}

	/** Check if element complies with model.
	 * @param xel the XElement object.
	 * @param el The source element from which the result is composed.
	 * @return ChkElement object or <tt>null</tt> if element do not comply.
	 */
	private ChkElement chkElem(final XElement xel, final Element el) {
		if (!"$any".equals(xel.getName())) {
			String localName = xel.getName();
			int ndx = localName.indexOf(':');
			if (ndx >= 0) {
				localName = localName.substring(ndx + 1);
			}
			String s;
			s = _rootChkDocument.findInLexicon(xel.getXDPosition());
			if (s != null) {
				localName = s;
			}
			s = el.getNodeName();
			if ((ndx = s.indexOf(':')) >= 0) {
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
	 * @param index The index of X-definition.
	 * @return The actual definition or null.
	 */
	public final XNode getDefElement(final int index) {
		return index < _defList.length ? _defList[index] : null;
	}

	/** Get maximal index of X-definition in the list.
	 * @return Max index of definition list.
	 */
	final int getDefinitionMaxIndex() {return _defList.length;}

	/** Add the new attribute to the current element.
	 * @param att The object with attribute.
	 * @return <tt>true</tt> if attribute was created according to X-definition.
	 */
	final boolean newAttribute(final Attr att) {
		_node = att;
		boolean result = addAttributeNS(
			att.getNamespaceURI(), att.getName(), att.getValue());
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
		if ((xatt != null && xatt._attrWhiteSpaces!= 0)
			? xatt._attrWhiteSpaces == 'T'
			: (_xElement._attrWhiteSpaces!= 0)
				? _xElement._attrWhiteSpaces == 'T'
				: _rootChkDocument._attrWhiteSpaces == 'T') {
			result = SUtils.trimAndRemoveMultipleWhiteSpaces(data);
		} else if ((xatt != null && xatt._trimAttr != 0)
			? xatt._trimAttr != 'F'
			: (_xElement._trimAttr != 0)
			? _xElement._trimAttr != 'F' : _rootChkDocument._trimAttr != 'F') {
			result = data.trim();
		} else {
			result = data;
		}
		if (result.length() == 0) {
			if ((xatt != null && xatt._ignoreEmptyAttributes != 0)
				? xatt._ignoreEmptyAttributes == 'T'
				: (_xElement._ignoreEmptyAttributes != 0)
					? _xElement._ignoreEmptyAttributes == 'T'
					: _rootChkDocument._ignoreEmptyAttributes == 'T') {
				return null;
			}
			return result;
		}
		if (xatt != null && xatt.isFixed()) {
			return result;
		}
		byte c = (xatt != null && xatt._attrValuesCase != 0)
			? xatt._attrValuesCase
			: _xElement._attrValuesCase != 0
			? _xElement._attrValuesCase : _rootChkDocument._setAttrValuesCase;
		return c == 'T' ? result.toUpperCase() :
			c == 'F' ? result.toLowerCase() : result;
	}

	private XData getXAttr(final String nsURI, final String qname) {
		if (nsURI == null) {
			return getXAttr(qname);
		}
		int ndx = qname.indexOf(':');
		String localName = qname.substring(ndx + 1);
		XData xatt = _xElement.getDefAttrNS(nsURI,
			localName, _rootChkDocument._sourceLanguageID);
		if (xatt == null && nsURI.equals(_element.getNamespaceURI())) {
			XData xa = _xElement.getDefAttr(
				localName, _rootChkDocument._sourceLanguageID);
			if (xa != null && xa._acceptQualifiedAttr == 'T') {
				return xa;
			}
		}
		return xatt;
	}

	private XData getXAttr(final String name) {
		return _xElement.getDefAttr(name, _rootChkDocument._sourceLanguageID);
	}

	/** Execute validation method and if putTempErrors is true then put errors
	 * to reporter.
	 * @param xdata model of data.
	 * @param putTempErrors if true then put errors to reporter.
	 */
	private void checkDatatype(final XData xdata, final boolean putTempErrors) {
		if (xdata._check >= 0) {
			XDValue item = exec(xdata._check, (byte) 'A');
			if (item.getItemId() == XD_PARSERESULT) {
				_parseResult = (XDParseResult) item;
				if (_xComponent != null
					&& _parseResult.matches() && getXMNode() != null
					&& getXMNode().getXDPosition() != null) {
					_xComponent.xSetAttr(this, _parseResult);
				}
			} else {
				_parseResult = new DefParseResult(_data);
				if (item.booleanValue()) {
					if (_xComponent != null && getXMNode() != null
						&& getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				} else {
					//Value error
					_parseResult.error(XDEF.XDEF515);
				}
			}
		} else {//default: do not check; i.e. always true
			setXXType((byte) 'A');
			_parseResult = new DefParseResult(_data);
			if (_xComponent != null && getXMNode() != null
				&& getXMNode().getXDPosition() != null) {
				_xComponent.xSetAttr(this, _parseResult);
			}
		}
		if (!_parseResult.matches()) { //error
			for (Report rep: _parseResult.getReporter()) {
				String s = rep.getModification();
				if (s == null) s = "";
				rep.setModification(
					s+"&{xpath}"+_xPos+"&{xdpos}"+xdata.getXDPosition());
				if (putTempErrors) {
					_scp.getTemporaryReporter().putReport(rep);
				}
			}
			if (!chkTemporaryErrors()) {
				if (putTempErrors) {
					//Value error
					putTemporaryReport(Report.error(XDEF.XDEF515));
				}
			}
		}
	}

	@Override
	/** Add the new attribute to the current XXElement.
	 * @param qname The qualified name of attribute (including prefix).
	 * @param data The value of attribute.
	 * @param nsURI The value of namespace URI.
	 * @return <tt>true</tt> if attribute was created according to X-definition.
	 */
	public final boolean addAttributeNS(final String nsURI,
		final String qname,
		final String data) {
		if (_element == null) {
			return _ignoreAll;
		}
		if ("xmlns".equals(qname) || qname.startsWith("xmlns:")
			|| qname.startsWith("xml:")) {
			String uri;
			if ((uri = nsURI) == null) {
				uri = qname.startsWith("xml:") ? XMLConstants.XML_NS_URI
					: XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			}
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
			error(XDEF.XDEF525, //Attribute not allowed
				qname, "&{xpath}" + _xPos + "&{xdpos}" + getXDPosition());
			return false;
		}
		XData xatt = (nsURI != null) ? getXAttr(nsURI, qname) : getXAttr(qname);
		if (xatt == null) {
			xatt = _xElement.getDefAttr("$attr", -1); // any attr
		}
		String adata;
		int ndx = qname.indexOf(':');
		if ((adata = attrWhitespaces(xatt, data)) == null) {
			if (nsURI != null) {
				_element.removeAttributeNS(nsURI, qname.substring(ndx + 1));
			} else {
				_element.removeAttribute(qname);
			}
			return true;
		}
		String xPos = _xPos;
		_xPos += "/@" + qname;
		_attName = qname;
		_attURI = nsURI;
		_xdata = xatt;
		if (xatt!=null && xatt._match>=0 &&	!getXDDocument().isCreateMode()) {
			_elemValue = _element;
			_data = adata;
			XDValue item = exec(xatt._match, (byte) 'A');
			_elemValue = null;
			_data = null;
			_parseResult = null;
			if (item != null && !item.booleanValue()) {//delete it
				if (nsURI != null) {
					_element.removeAttributeNS(nsURI, qname.substring(ndx + 1));
				} else {
					_element.removeAttribute(qname);
				}
				if (xatt.minOccurs() != XOccurrence.IGNORE) {
					if (xatt.minOccurs() != XOccurrence.ILLEGAL) {
						//Attribute not allowed
						putTemporaryReport(Report.error(XDEF.XDEF525, qname,
							"&{xpath}" + _xPos + "&{xdpos}" + getXDPosition()));
					}
					if (xatt._onIllegalAttr >= 0) {
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
		if (xatt != null) {
			String xname = xatt.getName();
			//let's register that we processed this attribute
			if (_attNames.contains(xatt.getName()) && !"$attr".equals(xname)
				&& xatt._acceptQualifiedAttr == 'T') {
				//Both, the qualified and unqualified attributes are not allowed
				//with the option acceptQualifiedAttr: &{0}
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
				case XOccurrence.IGNORE: // ignore
					_attName = null;
					_attURI = null;
					if (nsURI != null) {
						_element.removeAttributeNS(nsURI,
							qname.substring(ndx + 1));
					} else {
						_element.removeAttribute(qname);
					}
					_attNames.add(xname);
					_xdata = null;
					_parseResult = null;
					_xPos = xPos;
					return true;
				case XOccurrence.ILLEGAL: // illegal
					debugXPos(XDDebug.ONILLEGALATTR);
					if (xatt._onIllegalAttr >= 0) {
						_elemValue = _element;
						_data = adata;
						exec(xatt._onIllegalAttr, (byte) 'A');
						copyTemporaryReports();
						_attName = null;
						_attURI = null;
						_data = null;
						_parseResult = null;
						_xdata = null;
						_parseResult = null;
						_xPos = xPos;
						return false;
					}
					break; // report as it is undefined
				default : {// required(1) or optional(0)
					_data = adata;
					debugXPos(XDDebug.INIT);
					if (xatt._init >= 0) {// execute "onInit" action
						_elemValue = _element;
						exec(xatt._init, (byte) 'A');
						copyTemporaryReports();
					}
					if (_data == null) {
						if (xatt._onFalse >= 0) {// value not exist
							_elemValue = _element;
							exec(xatt._onFalse, (byte) 'A');
							copyTemporaryReports();
						}
						_attNames.add(xname);
						_parseResult = new DefParseResult(_data);
					} else {
						_elemValue = _element;
						//if the value is an ampty string and the option is
						//set to "acceptEmptyAttributes" at any level then
						//set the result of the check method to "true" (do NOT
						//report and/or process an error)!
						if (_data.length() == 0
							&& ((xatt._ignoreEmptyAttributes == 'A'
								|| xatt._ignoreEmptyAttributes == 'P'
								&& xatt.isOptional())
								|| xatt._ignoreEmptyAttributes == 0
								&& (_xElement._ignoreEmptyAttributes == 'A'
								||_xElement._ignoreEmptyAttributes == 'P'
								&& xatt.isOptional())
								|| _xElement._ignoreEmptyAttributes == 0
								&& (_rootChkDocument._ignoreEmptyAttributes=='A'
								|| _rootChkDocument._ignoreEmptyAttributes=='P'
								&& xatt.isOptional()))) {
							//accept empty attributes
							_attNames.add(xname);
							_parseResult = new DefParseResult(""); // empty attr
						} else {
							debugXPos(XDDebug.PARSE);
							//we are now sure the length is > 0 because if
							//the option was not set to "acceptEmptyAttributes"
							//then an empty attribute value was set to null and
							//the attribute had been ignored in attrWhitespaces!
							_attNames.add(xname);
							checkDatatype(xatt, false);
						}
						if (_parseResult.matches()) { // true
							clearTemporaryReporter();
							if (_data != null) {
								if (!_data.equals(adata)) {
									if (nsURI != null) {
										_element.setAttributeNS(nsURI,
											qname, _data);
									} else {
										_element.setAttribute(qname, _data);
									}
								}
							} else {
								if (nsURI != null) {
									_element.removeAttributeNS(nsURI,
										qname.substring(ndx + 1));
								} else {
									_element.removeAttribute(qname);
								}
							}
							debugXPos(XDDebug.ONTRUE);
							if (xatt._onTrue >= 0) {
								exec(xatt._onTrue, (byte) 'A');
								copyTemporaryReports();
								if (_data == null) {
									if (nsURI != null) {
										_element.removeAttributeNS(nsURI,
											qname.substring(ndx + 1));
									} else {
										_element.removeAttribute(qname);
									}
								}
							}
						} else { // false
							//call put error reports to chkElement
							debugXPos(XDDebug.ONFALSE);
							if (xatt._onFalse >= 0) {
								clearTemporaryReporter();
								exec(xatt._onFalse, (byte) 'A');
							} else {
								result = false;
								//copy errors from parsed result to
								//the temporary reporter.
								for (Report rep: _parseResult.getReporter()) {
									_scp.getTemporaryReporter().putReport(rep);
								}
								if (!chkTemporaryErrors()) {
									putTemporaryReport( //Value error
										Report.error(XDEF.XDEF515));
								}
							}
							copyTemporaryReports();
						}
					}
					if (_data != null && !_data.equals(adata)) {
						if ((adata = attrWhitespaces(xatt, adata)) == null) {
							if (nsURI != null) {
								_element.removeAttributeNS(nsURI,
									qname.substring(ndx + 1));
							} else {
								_element.removeAttribute(qname);
							}
							_attName = null;
							_attURI = null;
							_data = null;
							_parseResult = null;
							_attNames.add(xname);
							_xdata = null;
							_parseResult = null;
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
						if (nsURI != null) {
							_element.removeAttributeNS(nsURI,
								qname.substring(ndx + 1));
						} else {
							_element.removeAttribute(qname);
						}
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
		// X-definition not found
		if (_xElement._moreAttributes=='T' || _xElement._moreAttributes=='I') {
			//more attributes allowed, add attribute as it is
			//no X-definition for this attribute
			_parseResult = new DefParseResult(data);
			if (nsURI != null) {
				if (_xElement._moreAttributes=='I') {
					_element.removeAttributeNS(nsURI, qname);
				} else {
					_element.setAttributeNS(nsURI, qname, adata);
					if (_xComponent != null && getXMNode() != null
						&& getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				}
			} else {
				if (_xElement._moreAttributes=='I') {
					_element.removeAttribute(qname);
				} else {
					_element.setAttribute(qname, adata);
					if (_xComponent != null && getXMNode() != null
						&& getXMNode().getXDPosition() != null) {
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
		if (_xElement._onIllegalAttr >= 0) {
			//Attribute not allowed
			putTemporaryReport(Report.error(XDEF.XDEF525, qname,
				"&{xpath}" + _xPos + "&{xdpos}" + getXDPosition()));
			_elemValue = _element;
			_data = adata;
			exec(_xElement._onIllegalAttr, (byte) 'E');
			if (_data != null) {
				if (nsURI != null) {
					_element.setAttributeNS(nsURI, qname, adata);
					if (_xComponent != null && getXMNode() != null
						&& getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				} else {
					_element.setAttribute(qname, adata);
					if (_xComponent != null && getXMNode() != null
						&& getXMNode().getXDPosition() != null) {
						_xComponent.xSetAttr(this, _parseResult);
					}
				}
			}
			copyTemporaryReports();
		} else {
			//Attribute not allowed
			error(XDEF.XDEF525, qname,
				"&{xpath}" + _xPos + "&{xdpos}" + getXDPosition());
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

	@Override
	/** Add the new attribute to the current XXElement.
	 * @param name name of attribute.
	 * @param data value of attribute.
	 * @return <tt>true</tt> if attribute was created according to X-definition.
	 */
	public final boolean addAttribute(final String name, final String data) {
		return addAttributeNS(null, name, data);
	}

	/** Set attribute to the current element. First remove the original
	 * attribute if exists to prevent report about attribute redefinition.
	 * @param name The name of attribute.
	 * @param value The value of attribute.
	 * @return <tt>true</tt> if attribute was created according to X-definition.
	 */
	public final boolean setAttribute(final String name,
		final String value) {
		_element.setAttribute(name, value);
		return newAttribute(_element.getAttributeNode(name));
	}

	/** Set attribute to the current element. First remove the original
	 * attribute if exists to prevent report about attribute redefinition.
	 * @param name The name of attribute.
	 * @param data The value of attribute.
	 * @param nsURI The value of name space URI.
	 * @return <tt>true</tt> if attribute was created according to X-definition.
	 */
	public final boolean setAttribute(final String name,
		final String data,
		final String nsURI) {
		if (nsURI == null) {
			return setAttribute(name, data);
		}
		_element.setAttributeNS(nsURI, name, data);
		return newAttribute(_element.getAttributeNode(name));
	}

	/** Update actual element in the tree. If argument is null the actual
	 * element will be removed from the tree.
	 * @param el The element which will replace the actual one.
	 */
	public final void updateElement(final Element el) {
		Element el1, el2;
		if ((el2 = el) == null) {
			//remove child
			if (_element != null) {
				if (_parent._parent == null) { // root element
					try {
						((ChkDocument) _parent).
							getDocument().removeChild(_element);
					} catch (Exception ex) {}
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

	@Override
	/** This method is called when the end of the current element attribute list
	 * was parsed. The implementation may check the list of attributes and
	 * may invoke appropriate actions.
	 * @return <tt>true</tt> if element is compliant with X-definition.
	 */
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
		XData[] xattrs = _xElement.getXDAttrs();
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
			}
			if (_attNames.contains(xname)) {
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
							_element.setAttributeNS(
								xatt.getNSUri(), xname, _data);
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
					clearTemporaryReporter();
					Report rep = null;
					if (xatt.minOccurs() == XData.REQUIRED) {
						//Missing required attribute &{0}
						rep = Report.error(XDEF.XDEF526, xname);
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
						if (uri == null && _element.hasAttribute(xname)
							|| uri != null && _element.hasAttributeNS(uri,
							xname.substring(xname.indexOf(':') + 1))) {
							//remove the message "missing"
							removeTemporaryReport(rep);
							continue; // attribute exists, don't invoke default
						}
					}
					if (xatt._deflt < 0) {
						copyTemporaryReports();
						continue; // skip default method
					}
					removeTemporaryReport(rep); // not report "missing" twice
					copyTemporaryReports();
				}
				if (xatt.minOccurs() == XData.REQUIRED) {
					//no method called; put error
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

	final void finishSelector() {
		if (_selector == null) {
			return;
		}
		//2. ckeck if there is list of pending selector
		if (!_selector._ignorable
			&& _selector.minOccurs() > 0
			&& _selector.minOccurs() > (_selector._occur ?
				_selector._kind == XNode.XMMIXED ?
					1 : ++_selector._count : _selector._count)) {
			XSelector xsel = (XSelector) getDefElement(_selector._begIndex);
			//Minimum occurrence not reached for &{0}
			error(XDEF.XDEF555, xsel.getName().substring(1));
			debugXPos(XDDebug.ONABSENCE);
			if (xsel._onAbsence>= 0) {
				exec(xsel._onAbsence, (byte)'U');
			}
		}
		boolean nested = false;
		while (_selector != null) {
			if (_selector._kind == XNode.XMCHOICE) {
				nested = true;
				if (_selector._occur && _nextDefIndex == _selector._endIndex) {
					_selector._count = ++_counters[_nextDefIndex];
				}
			} else if (_selector._occur) {
				_selector._count++;
			}
			//check absence within a group. If actual node is the end of a group
			// then set "skipselector" to true, othewise to false.
			checkAbsence(_selector, null,
				_nextDefIndex < _defList.length
					&& _defList[_nextDefIndex].getKind() == XNode.XMSELECTOR_END
					&& nested);
			nested = true;
			_actDefIndex = -1;
			_nextDefIndex = _selector._endIndex + 1;
			debugXPos(XDDebug.SELECTORFINALLY);
			if (_selector._finallyCode >= 0) {
				exec(_selector._finallyCode, (byte) 'U');
			}
			if (_selector._prev != null && _selector._occur) {
				_selector._prev._count++;
			}
			_selector = _selector._prev;
		}
	}

	/** Finish checking of model.
	 * @param element the element or <tt>null</tt> it text node is processed.
	 * @return the XNode object from the list or <tt>null</tt>.
	 */
	private void finishModel() {
		_parseResult = null;
		//1. check if last element occurrence
		if (_actDefIndex >= 0) { // check last processed item
			XNode xn = _defList[_actDefIndex];
			if (xn.getKind() == XNode.XMELEMENT) {
				if (xn.minOccurs() > _counters[_actDefIndex]) {
					chkElementAbsence(_actDefIndex, (XElement) xn, null);
				}
			} else if (xn.getKind() == XNode.XMTEXT) {
				if (xn.minOccurs() > _counters[_actDefIndex]) {
					chkTextAbsence(_actDefIndex, (XData) xn, false, null);
				}
			}
			_actDefIndex = -1;
		}
		finishSelector();
		//3. check remaining part of model.
		int nextDefIndex = _nextDefIndex;
		while (_nextDefIndex < _defList.length) {
			short kind;
			XNode xnode;
			switch (kind = (xnode = _defList[_nextDefIndex]).getKind()) {
				case XNode.XMTEXT:
				case XNode.XMELEMENT: {
					if (_selector != null && _selector._selective) {
						if ((_selector.maxOccurs() <= 1 || !_selector._occur)
							&& _selector._count <= _selector.minOccurs()) {
							if (_selector._prev == null
								|| _selector._prev._count+1 <=
								_selector._prev.minOccurs()) {
								XSelector xsel = (XSelector)
									getDefElement(_selector._begIndex);
								if (xsel._onAbsence>=0 && (_selector._count==0
									|| _selector._count<_selector.minOccurs())){
									debugXPos(XDDebug.ONABSENCE);
									exec(xsel._onAbsence, (byte)'U');
								} else if (_selector._count
									<_selector.minOccurs()){
									String s = _selector._kind == XNode.XMCHOICE
										? "choice"
										: _selector._kind==XNode.XMMIXED
										? "mixed" : "sequence";
									//Minimum occurrence not reached for &{0}
									error(XDEF.XDEF555, s);
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
				case XNode.XMSELECTOR_END: {
					_actDefIndex = -1;
					if (_selector == null) {
						_nextDefIndex++;
						continue;
					}
					switch (_selector._kind) {
						case XNode.XMMIXED: {
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
									_selector._prev._occur|=_selector._count>0;
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
									_selector._prev._occur|=_selector._count>0;
									_selector = _selector._prev;
								}
								_nextDefIndex++;
							}
							continue;
						}
						case XNode.XMCHOICE: {
							if (_selector._occur) {
								_selector._count =
									++_counters[_selector._endIndex];
							}
							if (_selector.maxOccurs()<=1 || !_selector._occur) {
								if (!_selector._ignorable
									&& _selector._count < _selector.minOccurs()) {
									//Minimum occurrence not reached for &{0}
									error(XDEF.XDEF555, "choice");
									XSelector xsel = (XSelector)
										getDefElement(_selector._begIndex);
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
									_selector._prev._occur|=_selector._count>0;
									_selector = _selector._prev;
								}
							} else {
								checkAbsence(_selector,
									new Counter(_selector._firstChild), true);
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
									_selector._prev._occur |=
										_selector._occur || _selector._count>0;
									if (_selector._prev._kind!=XNode.XMSEQUENCE
										&& _selector._count > 0) {
										_selector._prev._occur = true;
										_nextDefIndex = _selector._begIndex + 1;
									} else {
										_nextDefIndex++;
									}
									_selector.updateCounters();
									_selector = _selector._prev;
								}
							} else {
								checkAbsence(_selector,
									new Counter(_selector._firstChild), true);
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
				case XNode.XMSEQUENCE:
				case XNode.XMMIXED:
				case XNode.XMCHOICE: {
					_actDefIndex = -1;
					if (_selector != null && _selector._selective) {
						if ((!_selector._ignorable &&
							_selector._count < _selector.minOccurs())
							&& (_selector._prev == null
							|| _selector._prev.minOccurs()
								>_selector._prev._count
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
						_selector =
							new SelectorState(_selector, (XSelector) xnode);
					}
					_nextDefIndex++;
					continue;
				}
				default: //error - unknkown kind
					//Internal error&{0}{: }
					throw new SRuntimeException(SYS.SYS066, "kind: " + kind);
			}
			break;
		}
		//check absence of items in root selection
		if ((_nextDefIndex = nextDefIndex) < _defList.length) {
			XSequence xs = new XSequence();
			xs.setOccurrence(1, 1);
			xs._begIndex = _nextDefIndex - 1;
			xs._endIndex = _defList.length;
			_selector = new SelectorState(null, xs);
			checkAbsence(_selector,
				new Counter(_element.getChildNodes().getLength()), false);
			_selector = null;
		}
	}

	/** Process text white spaces before processing.
	 * @param xd XData model or <tt>null</tt>.
	 * @param data String to be processed.
	 * @return text with processed white spaces.
	 */
	final String textWhitespaces(final XCodeDescriptor xd, final String data){
		String result;
		if ((xd != null && xd._textWhiteSpaces != 0) ?
			xd._textWhiteSpaces  == 'T' :
			_xElement._textWhiteSpaces == 'T') {
			result = SUtils.trimAndRemoveMultipleWhiteSpaces(data);
		} else if ((xd != null && xd._trimText != 0) ?
			xd._trimText != 'F' :
			(_xElement._trimText != 0) ? _xElement._trimText != 'F' :
			_rootChkDocument._trimText != 'F') {
			result = data.trim();
		} else {
			result = data;
		}
		if (result.length() == 0 || xd != null && xd.isFixed()) {
			return result;
		}
		byte b = (xd != null && xd._textValuesCase != 0) ?
			xd._textValuesCase
			: _xElement._textValuesCase != 0 ? _xElement._textValuesCase
			: _rootChkDocument._xElement._textValuesCase;
		return b == 'T' ? result.toUpperCase() :
			b == 'F' ? result.toLowerCase() : result;
	}

	@Override
	/** Add new element as a child of the current element.
	 * Checks all attributes and child elements for occurrence.
	 * @return <tt>true</tt> if element was added and complies to X-definition.
	 */
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
			_defList = new XNode[0];
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
					error(XDEF.XDEF558, "element");
					error = true;
				}
			} else {
				_parent.incRefNum();
			}
		}
		if (_element != null) {
			XData xatt;
			String name;
			String xPos	= _xPos;
			XData[] xattrs = _xElement.getXDAttrs();
			int anyAttrs = 0;
			for (int i = 0; i < xattrs.length; i++) {
				xatt = xattrs[i];
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
								String orig = _data =
									nm.item(j).getNodeValue();
								exec(xatt._finaly, (byte) 'A');
								copyTemporaryReports();
								anyAttrs++;
								if (_data == null) {
									_element.removeAttributeNode(
										(Attr) item);
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
						//Minimum number of attributes declared as "xd:attr"
						//was not reached
						putTemporaryReport(Report.error(XDEF.XDEF531));
					} else if (anyAttrs > xatt.getOccurence().maxOccurs()) {
						//Maximum number of attributes declared as "xd:attr"
						//was exceeded
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
							_node = _element.getAttributeNodeNS(uri,
								name.substring(name.indexOf(':') + 1));
							orig = _data = _element.getAttributeNS(
								uri, name.substring(name.indexOf(':') + 1));
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
									_element.removeAttributeNS(uri,
										name.substring(name.indexOf(':') + 1));
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
			if ((xatt = _xElement.getDefAttr("$text", -1)) != null) {
				_xPos = xPos + "/text()";
				if (_numText < xatt.minOccurs()) {
					debugXPos(XDDebug.ONABSENCE);
					if (xatt._onAbsence >= 0) {
						_elemValue = _element;
						_data = null;
						_parseResult = null;
						exec(xatt._onAbsence, (byte) 'T');
						copyTemporaryReports();
						if (_data != null) {
							NodeList nl = _element.getChildNodes();
							for (int j = nl.getLength() - 1; j >= 0; j--) {
								_element.removeChild(nl.item(j));
							}
							appendTextNode(_data, xatt);
							_numText++;
							if (xatt._finaly >= 0) {
								exec(xatt._finaly, (byte) 'T');
							}
						}
					}
					if (_numText < xatt.minOccurs()) {
						if (!_nil || _numText > 0) {
							//Missing required text
							error(XDEF.XDEF527,"&{xdpos}"+xatt.getXDPosition());
						}
					}
				}
				if (_numText > xatt.maxOccurs() && !xatt.isIllegal()) {
					//Maximum number of text nodes declared as "xd:text"
					// was exceeded
					error(XDEF.XDEF533, "&{xdpos}" + xatt.getXDPosition());
				}
			}
			if ((xatt = _xElement.getDefAttr("$textcontent", -1)) != null) {
				_xPos = xPos + "/text()";
				String orig = _data = KXmlUtils.getTextContent(_element);
				if (orig.length() > 0) {
					_numText = 1;
					debugXPos(XDDebug.PARSE);
					XDValue item = xatt._check >= 0 ?
						exec(xatt._check, (byte) 'T') : new DefBoolean(true);
					if (item != null && (item.getItemId() == XD_PARSERESULT
						? ((XDParseResult) item).matches()
						: item.booleanValue())) {
						clearTemporaryReporter();
						debugXPos(XDDebug.ONTRUE);
						if (xatt._onTrue >= 0) {
							exec(xatt._onTrue, (byte) 'T');
							copyTemporaryReports();
						}
					} else {
						debugXPos(XDDebug.ONFALSE);
						if (xatt._onFalse >= 0) {
							clearTemporaryReporter();
							exec(xatt._onFalse, (byte) 'T');
						} else {
							if (!chkTemporaryErrors()) {
								//Value error
								putTemporaryReport(Report.error(XDEF.XDEF515));
							}
						}
						copyTemporaryReports();
						if (!orig.equals(_data)) {
							NodeList nl = _element.getChildNodes();
							for (int j = nl.getLength() - 1; j >= 0; j--) {
								Node n = nl.item(j);
								if (n.getNodeType() == Node.TEXT_NODE
									||n.getNodeType()==Node.CDATA_SECTION_NODE){
									_element.removeChild(n);
								}
							}
							appendTextNode(_data, xatt);
						}
					}
					debugXPos(XDDebug.FINALLY);
					if (xatt._finaly >= 0) {
						exec(xatt._finaly, (byte) 'T');
						copyTemporaryReports();
					}
				} else if (_numText < xatt.minOccurs()) {
					debugXPos(XDDebug.ONABSENCE);
					if (xatt._onAbsence >= 0) {
						_elemValue = _element;
						_data = null;
						_parseResult = null;
						exec(xatt._onAbsence, (byte) 'T');
						copyTemporaryReports();
						if (_data != null) {
							appendTextNode(_data, xatt);
							_numText++;
							debugXPos(XDDebug.FINALLY);
							if (xatt._finaly >= 0) {
								_data = KXmlUtils.getTextValue(_element);
								exec(xatt._finaly, (byte) 'T');
								copyTemporaryReports();
							}
						}
					} else if (_numText < xatt.minOccurs() && !_nil) {
						//Missing required text
						error(XDEF.XDEF527, "&{xdpos}" + xatt.getXDPosition());
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

	/** Destruct ChkElement. */
	private void closeChkElement() {//just let's gc do the job
		_scp.closeFinalList(getFinalList()); // close objects from final list
		for (CodeUniqueset x: _markedUniqueSets) {
			String s = x.checkNotMarked(this);
			if (!s.isEmpty()) {
				//Not referred keys found in the uniqueSet &{0}&{1}{: }
				error(XDEF.XDEF524, x.getName(), s);
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
			if (_xComponent.xGetModelPosition().indexOf("/$any")>0
				|| _xComponent.xGetModelPosition().endsWith("#*")) {
				_xComponent.xSetAny(_element);
			}
			if (_xComponent.xGetParent() != null
				&& _xComponent != getParent().getXComponent()) {
				_xComponent.xGetParent().xAddXChild(_xComponent);
			}
			_xComponent = null;
		}
		if (!getXDDocument().isCreateMode()
			&& (_forget || _xElement._forget == 'T' || _xComponent != null)) {
			updateElement(null);
			_parent.getChkChildNodes().remove(this);
			_chkChildNodes = null;
			_xElement = null;
			_element = null;
			_xPosOccur.clear();
		}
		if (_variables != null) {
			for(int i = 0; i < _variables.length; i++) {
				XDValue x = _variables[i];
				if (x != null && !x.isNull()
					&& (x.getItemId() == CompileBase.UNIQUESET_VALUE
					|| x.getItemId() == CompileBase.UNIQUESET_M_VALUE)) {
					CodeUniqueset y = (CodeUniqueset)x;
					y.checkAndClear(_scp.getTemporaryReporter());
				}
				_variables[i] = null;
			}
			_variables = null;
		}
		_defList = new XNode[0];
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
	}

	@Override
	/** Add new Text node to current element.
	 * @param data The value of text node.
	 * @throws SRuntimeException if an error occurs.
	 * @return <tt>true</tt> if text node is compliant with X-definition.
	 */
	public final boolean addText(final String data) {
		if (_ignoreAll || _element  == null) {
			return true; //all checks are ignored (undef element)
		}
		if (_nil) {
			if (data.trim().length() == 0) {
				return true;
			} else {
				//Text value not declared
				error(XDEF.XDEF534);
				return false;
			}
		}
		if ((_data = textWhitespaces(null, data)).length() == 0) {
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
			if (value.length() == 0) {
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
				if (value.length() == 0) {
					_data = null;
					_parseResult = null;
					_nextDefIndex = nextDefIndex;
					_actDefIndex = actDefIndex;
					_xPos = xPos;
					_xdata = null;
					return true;
				}
			} else {
				if (value.length() == 0) {
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
			if (value.trim().length() == 0) {
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
			} else if (_xElement._moreElements != 'T'
				&& _xElement._moreElements != 'I'
				&& !_xElement.hasDefAttr("$textcontent")
				&& _xElement._moreText != 'T'
				&& _xElement._moreText != 'I') {
				debugXPos(XDDebug.ONILLEGALTEXT);
				if (_xElement._onIllegalText >= 0) {
					_elemValue = _element;
					//Text value not declared
					putTemporaryReport(Report.error(XDEF.XDEF534));
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
				xtxt = xtxt1 = new XData(// dummy text
					"$text", null, _xElement.getDefPool(), XNode.XMTEXT);
				if (_xElement.hasDefAttr("$textcontent")) { //copy option cdata!
					xtxt1._cdata =
						_xElement.getDefAttr("$textcontent", -1)._cdata;
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
				if (value.length() == 0) {
					_xdata = null;
					_xPos = xPos;
					return true;
				}
				debugXPos(XDDebug.ONILLEGALTEXT);
				if (xtxt1._onIllegalAttr >= 0) {
					//Illegal text
					putTemporaryReport(Report.error(XDEF.XDEF528));
					_elemValue = _element;
					_data = value;
					exec(xtxt1._onIllegalAttr, (byte) 'T');
					_data = null;
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
			default : // required(1) or implied(0)
				if (value.length() == 0) {// the text node without text ???
					_data = null;
					_parseResult = null;
					debugXPos(XDDebug.ONFALSE);
					if (xtxt1._onFalse >= 0) {// value not exist
						_elemValue = _element;
						exec(xtxt1._onFalse, (byte) 'T');
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
								_parseResult.error(XDEF.XDEF515); //Value error
							}
						}
						if (_parseResult.matches()) {
							clearTemporaryReporter(); // clear all error reports
							if (_xComponent != null) {
								_xComponent.xSetText(this, _parseResult);
							}
							debugXPos(XDDebug.ONTRUE);
							if (xtxt1._onTrue >= 0) {
								exec(xtxt1._onTrue, (byte) 'T');
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
								exec(xtxt1._onFalse, (byte) 'T');
							}
							copyTemporaryReports();
						}
					} else {
						_parseResult = new DefParseResult();
						debugXPos(XDDebug.ONTRUE);
						if (xtxt1._onTrue >= 0) {
							// if check exception not defined we call onTrue
							// action for value which is not null
							_elemValue = _element;
							exec(xtxt1._onTrue, (byte) 'T');
							copyTemporaryReports();
						}
					}
				}
				value = _data != null && !_data.equals(value)
					? textWhitespaces(xtxt1, _data) : _data;
				if (value != null) {
					debugXPos(XDDebug.FINALLY);
					if (xtxt1._finaly >= 0) {
						_data = value;
						_elemValue = _element;
						exec(xtxt1._finaly, (byte) 'T');
						value = _data;
					}
					if (value != null && value.length() > 0) {
						if (_element != null) {
							appendTextNode(value, xtxt1);
						}
						if (_actDefIndex >= 0
							&& _defList[_actDefIndex].getKind()==XNode.XMTEXT) {
							int n = xtxt == xtxt1 ? incRefNum() : getRefNum();
							if (_actDefIndex > 0 && n > xtxt1.maxOccurs()) {
								//Maximum occurrence limit of &amp;{0} exceeded
								error(XDEF.XDEF558, "text");
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

	@Override
	/** Add new Comment node to current element.
	 * @param data The value of Comment node.
	 * @return <tt>true</tt> if Comment node is compliant with X-definition.
	 */
	//TODO
	public final boolean addComment(final String data) {return true;}

	@Override
	/** Add new Processing instruction node to current element.
	 * @param name The name of the PI node.
	 * @param X The value of instruction part of the PI node.
	 * @throws SRuntimeException if an error occurs.
	 * @return <tt>true</tt> if PI node is compliant with X-definition.
	 */
	//TODO
	public final boolean addPI(final String name, final String x) {return true;}

	@Override
	/** Get text value of this node.
	 * @return The string with value of node.
	 */
	public final String getTextValue() {
		return (getItemId() != XX_ELEMENT) ? _data : null;
	}

	@Override
	/** Set text value to this node.
	 * @param data the text value to be set.
	 */
	public final void setTextValue(final String data) {
		if (getItemId() != XX_ELEMENT) {
			_data = data;
		} else {
			//Illegal use of method: &{0}
			throw new SRuntimeException(SYS.SYS083, "setText");
		}
	}

	 /** Set mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined. */
	final void setXXType(final byte mode) {_mode = mode;}

	 /** Get mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined.
	 * @return mode.
	 */
	final byte getXXType() {return _mode;}

	@Override
	public final short getItemId() {
		return _mode == 'T' ? XX_TEXT : _mode == 'A'? XX_ATTR : XX_ELEMENT;
	}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public final XDValueType getItemType() {
		return _mode == 'T' ? XDValueType.XXTEXT :
			_mode == 'A'? XDValueType.XXATTR : XDValueType.XXELEMENT;
	}

	@Override
	/** Get attribute with namespace from XXElement.
	 * @param uri The namespace of attribute.
	 * @param name The local name of attribute.
	 * @return value of attribute or the empty string if the attribute is legal
	 * otherwise throws the SRuntimeException.
	 * @throws SRuntimeException if the attribute is not legal in actual model.
	 */
	public final String getAttributeNS(final String uri, final String name) {
		Attr att = (uri == null) ? _element.getAttributeNode(name) :
			_element.getAttributeNodeNS(uri,name);
		if (att != null) {
			return att.getValue();
		}
		//attribute not exist in element.
		XMElement xel = getXMElement();
		XMData xatt = (uri == null) ?
			xel.getAttr(name) : xel.getAttrNS(uri, name);
		//prepare path for error message
		if (xatt != null) {
			if (xatt.isIllegal()) {
				//Attempt to get illegal item
				throw new SRuntimeException(XDEF.XDEF582,
					getXPos() + "/@" + name);
			}
			return null; //attribute is defined but not exists
		} else if (xel.hasOtherAttrs()) {
			//If X-definition has a VARIABLE_PART it makes no sense
			//to check it more.
			return null;
		}
		//Attempt to get undeclared item
		throw new SRuntimeException(XDEF.XDEF581, getXPos() + "/@" + name);
	}

	@Override
	/** Get attribute from the XXElement object.
	 * @param name The name of attribute.
	 * @return The value of attribute or the empty string if the value
	 * doesn't exist or return null if required attribute is defined in the
	 * XXElement, however it does not exist in the actual element.
	 * @throws SRuntimeException if required attribute is not defined
	 * in the X-definition.
	 */
	public final String getAttribute(final String name) {
		return getAttributeNS(null, name);
	}

////////////////////////////////////////////////////////////////////////////////
// Auxiliary methods
////////////////////////////////////////////////////////////////////////////////

	/** Set actual element.
	 * @param e The element.
	 */
	final void setActElem(final Element e) {_element = e;}

	@Override
	/** Get work element value.
	 * @return work element value.
	 */
	final Element getElemValue() {return _elemValue;}

	@Override
	/** Set work element value.
	 * @param e The element.
	 */
	final void setElemValue(final Element e) {_elemValue = e;}

////////////////////////////////////////////////////////////////////////////////
// Methods to retrieve values from checked tree.
////////////////////////////////////////////////////////////////////////////////

	/** Update counters from values in the argument array.
	 * @param cc array with values.
	 * @param ndx index where to start.
	 */
	final void updateCounters(final int[] cc, final int ndx) {
		if (cc != null) { // update counters.
			int len = cc.length + ndx - 1;
			for (int x = 0, y = ndx + 1; y < len; y++, x++) {
				_counters[y] += cc[x];
			}
		}
	}

	/**
	 * Look up for the X-Position (XPos) of the element set by <tt>xPath</tt>.
	 * For look up is used the hash table with the XPaths and their
	 * occurrences.
	 * @param xPath the XPath to the current ChkElement (Element
	 *	from the source XML document that is actually processed).
	 * @return the position of this element in the source XML document
	 *	to complete XPath identifier.
	 */
	private int getElemXPos(Map<String, XPosInfo> xPosOccur, String xPath) {
		if(xPosOccur != null) { // never should happen!!
			XPosInfo xPathInfo;
			if ((xPathInfo = xPosOccur.get(xPath)) == null) {
				// first occurrence of the xPath
				xPosOccur.put(xPath, new XPosInfo());
				return 1;
			} else {
				// another (second and more) occurrence of the xPath
				return xPathInfo.addCount();
			}
		}
		// Never should happen - internal error
		throw new SRuntimeException(XDEF.XDEF569, //Fatal error&{0}{: }
			"ChkElement:getElemXPos: _xPathOccur == null");
	}

	/** Saved counter object.*/
	private static final class Counter {
		int _itemIdex;
		Counter(final int counter) {_itemIdex = counter;}
	}

	/**
	 * Class to represent short information about XPaths for all elements
	 * present in the input XML source.
	 * This class is not deleted after element processing when "forget"
	 * option is specified !!!
	 * This class is deleted (nulled) when the end of parent element is reached
	 * in the XML source.
	 * Maximum recommended size of object created from this class is 8 kB
	 * to avoid OutOfMemory exception by processing very large XML sources.
	 */
	private static final class XPosInfo {
		/** Field to count the amount of the same XPaths. */
		private int _counter;

		/** Creates info object in case of first occurrence of this XPath
		 * in the input XML source.
		 */
		XPosInfo() {_counter = 1;}

		/** Another occurrence of this XPath in the input XML source.
		 * @return total number of this XPath occurrences in the input
		 *	XML source.
		 */
		int addCount() {return ++_counter;}
	}

	/** Mark unique set with this instance of ChkElement.
	 * @param us unique set.
	 */
	public final void addMarkedUniqueset(CodeUniqueset us) {
		_markedUniqueSets.add(us);
		us.setMarker(this);
	}

////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getRootXXElement(){return _rootChkDocument._chkRoot;}

	@Override
	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getXXElement() {return this;}

	@Override
	/** Get associated XML node.
	 * @return the associated XML node.
	 */
	public final Node getXMLNode() {return _node;}

	@Override
	/** Get namespace context of corresponding XElement.
	 * @return namespace context of the parent element.
	 */
	public final KNamespace getXXNamespaceContext() {
		return _xElement.getXDNamespaceContext();
	}

	@Override
	/** Check if attribute is legal in the XXElement.
	 * @param name The name of attribute.
	 * @return <tt>true</tt> if and only if the attribute is legal in the
	 * XXElement, otherwise return <tt>false</tt>.
	 */
	public final boolean checkAttributeLegal(final String name) {
		XData xatt = getXAttr(name);
		return xatt != null && !xatt.isIllegal();
	}

	@Override
	/** Check if attribute with given namespace is legal in the XXElement.
	 * @param uri namespace URI.
	 * @param name name of attribute (optionally with prefix).
	 * @return <tt>true</tt> if and only if the attribute is legal in the
	 * XXElement, otherwise return <tt>false</tt>.
	 */
	public final boolean checkAttributeNSLegal(final String uri,
		final String name) {
		XData xatt = uri == null || uri.length() == 0 ?
			getXAttr(name) : getXAttr(uri, name);
		return xatt != null && !xatt.isIllegal();
	}

	@Override
	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	public final XXNode[] getChildXXNodes() {
		XXNode[] result = new XXNode[_chkChildNodes.size()];
		_chkChildNodes.toArray(result);
		return result;
	}

	@Override
	final ArrayList<ChkElement> getChkChildNodes() {return _chkChildNodes;}

	@Override
	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	public final XMData getXMData() {return (XMData) getXMNode();}

	@Override
	/** Get actual model.
	 * @return actual model.
	 */
	public final XMNode getXMNode() {
	 /** mode: 'C' - comment, 'E' - element, 'A' - attribute, 'T' - text,
	 * 'D' - document, 'P' - processing instruction,'U' undefined. */
		return (_mode == (byte) 'A' || _mode == (byte) 'T') ?
			(XMNode) _xdata : (XMNode) _xElement;
	}

	@Override
	/** Get XComponent.
	 * @return The XComponent object (may be <tt>null</tt>).
	 */
	public final XComponent getXComponent() {return _xComponent;}

	@Override
	/** Set XComponent.
	 * @param x XComponent object.
	 */
	public final void setXComponent(final XComponent x) {_xComponent = x;}

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
		/** Index of first child of selector in NodeList */
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
				System.arraycopy(_counters,
					_begIndex + 1, _savedCounters, 0, len);
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