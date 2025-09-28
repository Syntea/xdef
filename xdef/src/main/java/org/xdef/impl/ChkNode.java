package org.xdef.impl;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDContainer;
import org.xdef.XDCurrency;
import org.xdef.XDDocument;
import org.xdef.XDGPSPosition;
import org.xdef.XDInput;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDPrice;
import org.xdef.XDResultSet;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefLocale;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefString;
import org.xdef.impl.xml.KNamespace;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXNode;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonNames;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_MAP;
import org.xdef.xon.XonUtils;
import static org.xdef.xon.XonNames.X_VALUE;

/** The abstract class for checking objects.
 * @author Vaclav Trojan
 */
public abstract class ChkNode extends XDValueAbstract implements XXNode {
	/** The name of element model. */
	final String _name;
	/** Root check Document. */
	final ChkDocument _rootChkDocument;
	/** The parent check node. */
	final ChkNode _parent;
	/** The level of element model. */
	final int _level;
	/** Actual element model. */
	XElement _xElement;
	/** Actual w3c.dom.Node. */
	Node _node;
	/** Actual w3c.dom.Element. */
	Element _element;
	/** Actual Node position of the node as XPath expression string. */
	String _xPos;
	/** Variable block */
	XDValue[] _variables;
	/** user object. */
	Object _userObject;
	/** Element used as source reference in compose mode. */
	Element _sourceElem;
	/** Iterator used in compose mode. */
	XDResultSet _iterator;
	/** Script code processor */
	XCodeProcessor _scp;
	/** Result of type parser. */
	XDParseResult _parseResult;
	/** List of objects to be managed before dispose. */
	private List<XDValue> _finalList;
	/** Actual number of errors.*/
	int _errCount;

	/** Create the new instance of ChkNode.
	 * @param name The name of node.
	 * @param parent The parent node.
	 */
	ChkNode(final String name, final ChkNode parent) {
		_name = name;
		_parent = parent;
		if (parent == null) {//root ChkDocument
			_rootChkDocument = (ChkDocument) this;
			_level = -1;
		} else {
			_rootChkDocument = parent._rootChkDocument;
			_scp = _rootChkDocument._scp; //accelerate
			_userObject = parent._userObject; //propagate user object
			_level = parent._level + 1;
		}
	}

	/** Add the object to final list.
	 * @param x the object to be added.
	 */
	final void addToFinalList(final XDValue x) {
		if (_finalList == null) {
			_finalList = new ArrayList<>();
		}
		_finalList.add(x);
	}

	/** Get list of object to be managed before dispose.*/
	final List<XDValue> getFinalList() {return _finalList;}

	/** Get name of the X-model.
	 * @return The name of node.
	 */
	@Override
	public final String getXXName() {return _name;}

	/** Get namespace URI of the X-model.
	 * @return namespace URI of node or <i>null</i>.
	 */
	@Override
	public String getXXNSURI() {return (_xElement == null) ? null : _xElement.getNSUri();}

	/** get User object.
	 * @return The user object.
	 */
	@Override
	public final Object getUserObject() {return _userObject;}

	/** set User object.
	 * @param obj The user object.
	 */
	@Override
	public final void setUserObject(final Object obj) {_userObject = obj;}

	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <i>null</i>.
	 */
	@Override
	public final Object setUserObject(final String id, final Object obj) {return _scp.setUserObject(id, obj);}

	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <i>null</i>.
	 */
	@Override
	public Object removeUserObject(final String id) {return _scp.removeUserObject(id);}

	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <i>null</i>.
	 */
	@Override
	public final Object getUserObject(final String id) {return _scp.getUserObject(id);}

	/** Return parent node.
	 * @return The parent node.
	 */
	@Override
	public final XXNode getParent() {return _parent;}

	/** Get nesting level the check node (model). The level of ChkDocument
	 * is -1.
	 * @return The node level.
	 */
	public final int getLevel() {return _level;}

	/** get StdOut.
	 * @return XDOutput object used as standard output stream.
	 */
	public final XDOutput getStdOut() {return _scp.getStdOut();}

	/** get StdErr.
	 * @return XDOutput object used as standard error stream.
	 */
	public final XDOutput getStdErr() {return _scp.getStdErr();}

	/** get StdIn.
	 * @return XDOutput object used as standard input stream.
	 */
	public final XDInput getStdIn() {return _scp.getStdIn();}

	/** Set standard output stream.
	 * @param out PrintStream object.
	 */
	public final void setStdOut(final PrintStream out) {setStdOut(new DefOutStream(out));}

	/** Set standard output stream.
	 * @param out PrintStream object.
	 */
	public void setStdOut(Writer out) {setStdOut(new DefOutStream(out));}

	/** Set standard output stream.
	 * @param out XDOutput object.
	 */
	public final void setStdOut(final XDOutput out) {_scp.setStdOut(out);}

	/** Set standard input stream.
	 * @param in InputStream object.
	 */
	public final void setStdIn(final InputStream in) {setStdIn(new DefInStream(in, false));}

	/** Set standard input stream.
	 * @param in XDInputStream object.
	 */
	public final void setStdIn(final XDInput in) {_scp.setStdIn(in);}

	/** Get default table with references IDS (used internally in the
	 * processor of XScript).
	 * @return default table with references IDS or null.
	 */
	public final CodeUniqueset getIdRefTable() {return _scp.getIdRefTable();}

	/** Set source element as context for create mode.
	 * @param sourceElem source element to be set (create mode).
	 */
	public final void setCreateContext(final Element sourceElem) {_sourceElem = sourceElem;}

	/** Get actual source context for create mode.
	 * @return source context or null if not available.
	 */
	public final Object getCreateContext() {return _sourceElem;}

	final void debugXPos(final char action) {
		if (_scp.isDebugMode()) {
			if (_scp.getDebugger().hasXPos(action + _xPos)) {
				_scp.getDebugger().debug(
					this, null, -1, -1, null, null,getXDPool().getDebugInfo(), null, (byte) 0);
			}
		}
	}

	/** Get actual source context for create mode.
	 * @return source context or <i>null</i> if not available.
	 */
	@Override
	public final XDValue getXDContext() {return _sourceElem != null ? new DefElement(_sourceElem) : null;}

	/** Set value from argument as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	@Override
	public final void setXDContext(final XDContainer xdc) {
		if (xdc != null && xdc.getXDItemsNumber() == 1 && xdc.getXDItem(0).getItemId() == XD_ELEMENT) {
			_sourceElem = xdc.getXDItem(0).getElement();
			return;
		}
		_sourceElem = xdc == null ? null : xdc.toElement(null, "_");
	}

	/** Set source element as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	@Override
	public final void setXDContext(final XDResultSet xdc) {_iterator = xdc;}

	/** Set source element as context for create mode.
	 * @param node XML node (Element or a Node). If this argument is not an
	 * Element then it represents Document element of owner document.
	 */
	@Override
	public final void setXDContext(final Node node) {
		_sourceElem = node == null ? null
			: node.getNodeType() == Node.ELEMENT_NODE ? (Element) node
			: node.getNodeType() == Node.DOCUMENT_NODE ? ((Document) node).getDocumentElement()
			: node.getOwnerDocument().getDocumentElement();
	}

	/** Set XON/JSON data as context for create mode.
	 * @param data the XON/JSON data. It can be either pathname or URL.
	 * @throws SRuntimeException if data is incorrect or if model is not found.
	 */
	@Override
	public final void setXONContext(final String data) throws SRuntimeException {
		setXDContext(XonUtils.xonToXmlW(data));
	}

	/** Set XON/JSON data as context for create mode.
	 * @param data the XON/JSON data. It can be either XON/JSON object or
	 * File, URL or InputStream with XON/JSON data* or XDResultSet
	 * or XML data to be converted to XON/JSON.
	 * @throws SRuntimeException if data is incorrect or if model is not found.
	 */
	@Override
	public final void setXONContext(final Object data) throws SRuntimeException{
		Element e = null;
		if (data == null || data instanceof Map || data instanceof List
			|| data instanceof String || data instanceof Number || data instanceof Boolean) {
			e = XonUtils.xonToXmlW(data);
		} else if (data instanceof File || data instanceof URL
			|| data instanceof InputStream || data instanceof String) {
			e = XonUtils.xonToXmlW( data);
		} else if (data instanceof Document) {
			e = ((Document) data).getDocumentElement();
		} else if (data instanceof Element){
			e = (Element) data;
		} else if (data instanceof XDResultSet) {
			_iterator = (XDResultSet) data;
			return;
		}
		if (e == null) {
			throw new SRuntimeException(XDEF.XDEF318); //Incorrect XON/JSON data
		}
		setXDContext(e);
	}

	/** Set source element as context for create mode.
	 * @param source string with pathname, URL or source of XML node.
	 */
	@Override
	public final void setXDContext(final String source) {setXDContext(KXmlUtils.parseXml(source));}

	/** Get names of variables.
	 * @return array of names of variables.
	 */
	@Override
	public final String[] getVariableNames() {
		return _rootChkDocument._xdef.getXDPool().getVariableTable().getVariableNames();
	}

	/** Get XDDocument.
	 * @return XDDocument.
	 */
	@Override
	public final XDDocument getXDDocument() {return _rootChkDocument;}

	/** Get XDPool.
	 * @return XDPool.
	 */
	@Override
	public final XDPool getXDPool() {return _rootChkDocument.getXMDefinition().getXDPool();}

	/** Get value of variable from XMDefinition.
	 * @param name name of variable.
	 * @return XDValue object or <i>null</i>.
	 */
	@Override
	public final XDValue getVariable(final String name) {return _scp.getVariable(name);}

	/** Find variable for setVariable (it Can't be final).
	 * @param name name of variable.
	 * @return suitable variable.
	 */
	private XVariable findVariable(final String name) {
		XPool xp = (XPool) _rootChkDocument._xdef.getXDPool();
		XVariable xv = xp.getVariable(name);
		if (xv == null) {
			xv = _rootChkDocument._xdef.findVariable(name);
		}
		if (xv != null) {
			if (xv.isFinal() && _scp.getVariable(name) != null) {
				//Variable '&{0}' is 'final'; the value can't be assigned
				throw new SRuntimeException(XDEF.XDEF562, name);
			}
			return xv;
		}
		throw new SRuntimeException(XDEF.XDEF563, name); //Variable '&{0}' doesn't exist
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	@Override
	public final void setVariable(final String name, final Object value) {
		if (value instanceof XDValue) {
			setVariable(name, (XDValue) value);
			return;
		}
		XVariable xv = findVariable(name);
		if (value == null) {
			_scp.setVariable(xv, DefNull.genNullValue(xv.getType()));
			return;
		}
		switch (xv.getType()) {
			case XD_CONTAINER: _scp.setVariable(xv, new DefContainer(value)); return;
			case XD_DATETIME: {
				if (value instanceof SDatetime) {
					_scp.setVariable(xv, new DefDate((SDatetime) value));
					return;
				}
				if (value instanceof Calendar) {
					_scp.setVariable(xv, new DefDate((Calendar) value));
					return;
				}
				if (value instanceof String) {
					_scp.setVariable(xv, new DefDate((String) value));
					return;
				}
				break;
			}
			case XD_DURATION: {
				if (value instanceof String) {
					_scp.setVariable(xv, new DefDuration((String) value));
					return;
				}
				if (value instanceof SDuration) {
					_scp.setVariable(xv, new DefDuration((SDuration) value));
					return;
				}
				break;
			}
			case XD_ELEMENT: {
				Element e;
				if (value instanceof Node) {
					Node node = (Node) value;
					switch (node.getNodeType()) {
						case Node.DOCUMENT_NODE:
							e = ((Document)node).getDocumentElement();
							break;
						case Node.ELEMENT_NODE:
							e = (Element)node;
							break;
						default:
						//Value is not compatible with type of variable '&{0}'
						throw new SRuntimeException(XDEF.XDEF564, name);
					}
				} else if (value instanceof XDValue) {
					setVariable(name, (XDValue) value);
					return;
				} else if (value instanceof String) {
					e = KXmlUtils.parseXml((String) value).getDocumentElement();
				} else if (value instanceof File) {
					e = KXmlUtils.parseXml((File) value).getDocumentElement();
				} else if (value instanceof URL) {
					e = KXmlUtils.parseXml((URL) value).getDocumentElement();
				} else if (value instanceof InputStream) {
					e = KXmlUtils.parseXml(
						(InputStream) value).getDocumentElement();
				} else {
					//Value is not compatible with the type of variable '&{0}'
					throw new SRuntimeException(XDEF.XDEF564, name);
				}
				_scp.setVariable(xv, new DefElement(e));
				return;
			}
		}
		if (value instanceof String) {
			setVariable(name, (String) value);
		} else if (value instanceof Long) {
			setVariable(name, (Long) value);
		} else if (value instanceof Integer) {
			setVariable(name, ((Integer) value).longValue());
		} else if (value instanceof Double) {setVariable(name, (Double) value);
		} else if (value instanceof Float) {
			setVariable(name, ((Float) value).doubleValue());
		} else if (value instanceof Boolean) {
			setVariable(name, ((Boolean) value).booleanValue());
		} else if (value instanceof BigDecimal) {
			setVariable(name, ((BigDecimal) value));
		} else if (value instanceof Locale) {
			setVariable(name, (new DefLocale((Locale) value)));
		} else if (value instanceof GPSPosition) {
			setVariable(name, (new XDGPSPosition((GPSPosition) value)));
		} else if (value instanceof Price) {
			setVariable(name, (new XDPrice((Price) value)));
		} else if (value instanceof InetAddress) {
			setVariable(name, (new DefIPAddr((InetAddress) value)));
		} else if (value instanceof Currency) {
			setVariable(name, new XDCurrency(((Currency) value).getCurrencyCode()));
		} else {
			//Value is not compatible with the type of variable '&{0}'
			throw new SRuntimeException(XDEF.XDEF564, name);
		}
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	@Override
	public final void setVariable(final String name, final long value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_DOUBLE: _scp.setVariable(xv, new DefDouble(value)); return;
			case XD_LONG: _scp.setVariable(xv, new DefLong(value)); return;
			case XD_DECIMAL: _scp.setVariable(xv, new DefDecimal(value)); return;
			case XD_BIGINTEGER: _scp.setVariable(xv, new DefBigInteger(value)); return;
			case XD_STRING: _scp.setVariable(xv, new DefString(String.valueOf(value))); return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	@Override
	public final void setVariable(final String name, final double value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_DOUBLE: _scp.setVariable(xv, new DefDouble(value)); return;
			case XD_STRING: _scp.setVariable(xv, new DefString(String.valueOf(value))); return;
			case XD_DECIMAL: _scp.setVariable(xv, new DefDecimal(String.valueOf(value))); return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	@Override
	public final void setVariable(final String name, final boolean value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_BOOLEAN: _scp.setVariable(xv, new DefBoolean(value)); return;
			case XD_STRING: _scp.setVariable(xv, new DefString(String.valueOf(value))); return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value XDValue to be set.
	 */
	private void setVariable(final String name, final XDValue value) {
		XVariable xv = findVariable(name);
		if (xv.getType() == value.getItemId()) {
			_scp.setVariable(xv, value);
		} else {
			switch (xv.getType()) {
				case XD_CONTAINER: _scp.setVariable(xv, new DefContainer(value)); return;
				case XD_STRING: setVariable(name, value.toString()); return;
				case XD_DOUBLE:
					if (value.getItemId() == XD_LONG) {
						setVariable(name, value.doubleValue()); return;
					}
					break;
				case XD_LONG:
					if (value.getItemId() == XD_DOUBLE) {
						setVariable(name, value.longValue()); return;
					}
					break;
				case XD_BOOLEAN:
					if (value.getItemId() == XD_STRING) {
						_scp.setVariable(xv, new DefBoolean(value.toString())); return;
					}
					break;
				case XD_PARSER: _scp.setVariable(xv, (XDParser) value); return;
			}
			//Value is not compatible with the type of variable '&{0}'
			throw new SRuntimeException(XDEF.XDEF564, name);
		}
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value DefValue to be set.
	 */
	private void setVariable(final String name, final String value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_STRING:
			case XD_CONTAINER: _scp.setVariable(xv, new DefString(value)); return;
			case XD_BOOLEAN: _scp.setVariable(xv, new DefBoolean(value)); return;
			case XD_DOUBLE: _scp.setVariable(xv, new DefDouble(value)); return;
			case XD_LONG: _scp.setVariable(xv, new DefLong(value)); return;
			case XD_DECIMAL: _scp.setVariable(xv, new DefDecimal(value)); return;
			case XD_BIGINTEGER: _scp.setVariable(xv, new DefBigInteger(value)); return;
			case XD_ELEMENT:
				_scp.setVariable(xv, new DefElement(KXmlUtils.parseXml(value).getDocumentElement())); return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	private void setVariable(final String name, final BigDecimal value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_LONG: _scp.setVariable(xv, new DefLong(value.longValue())); return;
			case XD_DOUBLE: _scp.setVariable(xv, new DefDouble(value.doubleValue())); return;
			case XD_DECIMAL: _scp.setVariable(xv, new DefDecimal(value)); return;
			case XD_STRING: _scp.setVariable(xv, new DefString(value.toString())); return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	/** Store model variable.
	 * @param name name of variable.
	 * @param val value to be stored.
	 */
	abstract void storeModelVariable(final String name, final XDValue val);

	/** Store  model variable.
	 * @param name name of variable.
	 * @return loaded value.
	 */
	abstract XDValue loadModelVariable(final String name);

	@Override
	public abstract KNamespace getXXNamespaceContext();

	@Override
	public final XPathFunctionResolver getXXFunctionResolver() {return _scp._functionResolver;}

	@Override
	public final XPathVariableResolver getXXVariableResolver() {return _scp._variableResolver;}

////////////////////////////////////////////////////////////////////////////////
// Methods to retrieve values from checked tree.
////////////////////////////////////////////////////////////////////////////////

	/** Get document with root element.
	 * @return The Document object.
	 */
	public Document getDocument() {return _rootChkDocument.getDocument();}

	/** Get document element.
	 * @return root element of the document.
	 */
	public final Element getDocumentElement() {return _rootChkDocument._element;}

	/** Increase reference counter by one.
	 * @return The increased reference number.
	 */
	abstract int incRefNum();

	/** Get occurrence of actual node
	 * @return The reference number.
	 */
	abstract int getOccurrence();

	/** Decrease reference counter by one.
	 * @return The increased reference number.
	 */
	abstract int decRefNum();

	/** Get reference counter of actual definition
	 * @return The reference number.
	 */
	abstract int getRefNum();

	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	abstract ChkElement getChkElement();

	/** Get Element value assigned to this node.
	 * @return Element value assigned to this node.
	 */
	public abstract Element getElemValue();

	/** Get actual element.
	 * @return The element.
	 */
	@Override
	public final Element getElement() {return _element;}

	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	abstract void setElemValue(Element elem);

	/** Get position of the node as a XPath expression string.
	 * @return position of the node as a XPath expression string.
	 */
	@Override
	public final String getXPos() {return _xPos;}

	/** Get source position.
	 * @return source position or <i>null</i> if position is not available.
	 */
	@Override
	public SPosition getSPosition() {return _rootChkDocument._reporter;}

	/** Set position of the node as XPath expression.
	 * @param xPath string with position of the node as XPath expression.
	 */
	final void setXPos(final String xPath) {_xPos = xPath;}

	@Override
	public final String stringValue() {return "XXNode " + getXXName();}

////////////////////////////////////////////////////////////////////////////////
// Methods for reporting
////////////////////////////////////////////////////////////////////////////////

	/** Get report writer assigned to the report generator.
	 * @return The report writer.
	 */
	@Override
	public final ReportWriter getReportWriter() {return _rootChkDocument._reporter.getReportWriter();}

	/** Copy temporary reports to global reporter.
	 * @return true if and only if temporary reporter contained errors before
	 * it was cleared.
	 */
	@Override
	public final boolean copyTemporaryReports() {
		Report rep;
		while ((rep = _scp.getTemporaryReporter().getReport()) != null) {
			putReport(rep);
		}
		boolean result = _scp.getTemporaryReporter().errors();
		_scp.getTemporaryReporter().clear();
		return result;
	}

	/** Clear temporary reporter. */
	@Override
	public final void clearTemporaryReporter() {_scp.getTemporaryReporter().clear();}

	/** Put message to temporary reporter.
	 * @param report report to be added to the temporary reporter.
	 */
	@Override
	public final void putTemporaryReport(Report report) {
		ensurePosInfo(report);
		_scp.getTemporaryReporter().putReport(report);
	}

	/** Remove report from temporary reporter.
	 * @param rep report to be removed.
	 * @return true if report was found and removed.
	 */
	@Override
	public final boolean removeTemporaryReport(final Report rep) {
		return _scp.getTemporaryReporter().removeReport(rep);
	}

	/** Get temporary reporter.
	 * @return ArrayReporter used as temporary reporter.
	 */
	@Override
	public final ArrayReporter getTemporaryReporter() {return _scp.getTemporaryReporter();}

	/** Check if temporary reporter has errors.
	 * @return true if temporary reporter has errors.
	 */
	@Override
	public final boolean chkTemporaryErrors() {return _scp.getTemporaryReporter().errors();}

	/** Set new temporary reporter.
	 * @param reporter new temporary reporter.
	 * @return ArrayReporter old temporary reporter.
	 */
	@Override
	public final ArrayReporter setTemporaryReporter(ArrayReporter reporter) {
		ArrayReporter result = _scp.getTemporaryReporter();
		_scp.setTemporaryReporter(reporter);
		return result;
	}

	/** Get SReporter of XDDocument.
	 * @return SReporter of XDDocument..
	 */
	@Override
	public final SReporter getReporter() {return _rootChkDocument._reporter;}

	/** Check if errors, fatal errors, light errors or warnings were reported.
	 * @return <i>true</i> if errors, fatal errors, light errors
	 * or warnings were reported.
	 */
	@Override
	public boolean errorWarnings() {return _rootChkDocument._reporter.errorWarnings();}

	/** Check if errors, fatal errors or light errors were reported.
	 * @return <i>true</i> if errors, fatal errors or light errors were
	 * reported.
	 */
	@Override
	public boolean errors() {return _rootChkDocument._reporter.errors();}

	/** Put fatal error message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void fatal(final String id, final String msg, final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	/** Put error message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	/** Put warning message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void warning(final String id, final String msg, final Object... mod) {
		putReport(Report.warning(id, msg, mod));
	}

	/** Put fatal error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public final void fatal(final long id, final Object... mod) {putReport(Report.fatal(id, mod));}

	/** Put error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public void error(final long id, final Object... mod) {putReport(Report.error(id, mod));}

	/** Put warning message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	@Override
	public void warning(final long id, final Object... mod) {putReport(Report.warning(id, mod));}

	/** Put report.
	 * @param report The report.
	 */
	@Override
	public final void putReport(final Report report) {
		ensurePosInfo(report);
		_rootChkDocument._reporter.putReport(report);
	}

	/** Get XMDefinition.
	 * @return X-definition of this document.
	 */
	@Override
	public final XMDefinition getXMDefinition() {return _rootChkDocument._xdef;}

	/** Get model of the processed object.
	 * @return model of the processed object (XMElement).
	 */
	@Override
	public final XMElement getXMElement() {return _xElement;}

	/** Get XDPosition of the processed element.
	 * @return XDPosition of the processed element.
	 */
	@Override
	public final String getXDPosition() {
		String result = _xElement == null ? null : _xElement.getXDPosition();
		return result == null ? "" : result;
	}

////////////////////////////////////////////////////////////////////////////////
// Interface of XXNode.
////////////////////////////////////////////////////////////////////////////////

	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	@Override
	abstract public XXNode[] getChildXXNodes();

	// can't be final this method is overriden!
	abstract List<ChkElement> getChkChildNodes();

	/** Get parsed result of an attribute or text node.
	 * @return parsed result of an attribute or text node.
	 */
	@Override
	public final XDParseResult getParseResult() {return _parseResult;}

	/** Get XON mode.
	 * @return XON mode or zero.
	 */
	@Override
	public final byte getXonMode() {return _xElement == null ? 0 : _xElement._xon;}

	/** Get actual value of default time zone.
	 * @return actual value of default time zone.
	 */
	@Override
	public final TimeZone getDefaultZone() {return _scp.getDefaultZone();}

////////////////////////////////////////////////////////////////////////////////
// Message reporting
////////////////////////////////////////////////////////////////////////////////

	/** Ensure creating X-position in the report.
	 * @param report where ensure creating X-position.
	 */
	public final void ensurePosInfo(final Report report) {
		String mod = report.getModification();
		if (mod == null) {
			mod = getPosMod(getXDPosition(), getXPos());
		} else {
			String[] pos = getPosInfo(getXDPosition(), getXPos());
			if (pos[0] != null && !mod.contains("&{xdpos}")) {
				mod += "&{xdpos}" + pos[0];
			}
			if (pos[1] != null && !mod.contains("&{xpath}")) {
				mod += "&{xpath}" + pos[1];
			}
			SPosition sp;
			if (!mod.contains("&{line}") && (sp = getSPosition()) != null
				&& sp.getLineNumber() > 0) {
				mod += "&{line}" + sp.getLineNumber();
				int ndx = mod.indexOf("&{column}");
				if (ndx >= 0) {
					int i = mod.indexOf('&', ndx + 1);
					mod = i > 0 ? mod.substring(ndx, i) : mod.substring(ndx);
				}
				mod += "&{column}" + sp.getColumnNumber();
				if ((ndx = mod.indexOf("&{sysId}")) >= 0) {
					int i = mod.indexOf('&', ndx + 1);
					mod = i > 0 ? mod.substring(ndx, i) : mod.substring(ndx);
				}
				if (sp.getSysId() != null && !sp.getSysId().isEmpty()) {
					mod += "&{sysId}" + sp.getSysId();
				}
			}
		}
		if (!mod.equals(report.getModification())) {
			report.setModification(mod);
		}
	}

	/** Get name of item object element.
	 * @param xnode XMNode with value.
	 * @return name of element or empty string.
	 */
	private static String getItemName(final XMNode xnode) {
		XMData key = ((XMElement)xnode).getAttr(XonNames.X_KEYATTR);
		if (key != null) {
			XDValue keyVal = key.getFixedValue();
			if (keyVal != null) {
				return ".['"+keyVal.toString() + "']";
			}
		}
		return "";
	}

	/** Get X-position and XPath information. If it is XON/JSON, then create
	 * modified XON/JSON path.
	 * @param xpos X-position of model.
	 * @param xpath XPath of data (may be null).
	 * @return array with two items - the first one is X-position and the
	 * second one is XPath.
	 */
	final String[] getPosInfo(final String xpos, final String xpath) {
		String[] result = new String[]{xpos, xpath};
		if (xpos == null) {
			return result;
		}
		int ndx, ndx1, ndy, ndy1;
		if ((ndx = xpos.indexOf('#')) < 0 || xpos.indexOf('/') < 0) {
			return result;
		}
		XMDefinition xd = getXDPool().getXMDefinition(xpos.substring(0, ndx));
		if ((ndx1 = xpos.indexOf('/')) < 0) {
			return result;
		}
		XMElement base = xd.getModel(null, xpos.substring(ndx + 1, ndx1));
		if (base == null) {
			return result;
		}
		XMNode[] xnodes = base.getChildNodeModels();
		if (xnodes == null || base.getXonMode() == 0) {
			return result;
		}
		ndx = ndx1 + 1;
		if ((ndx1 = xpos.indexOf('/', ndx)) < 0) {
			ndx1 = xpos.length();
		}
		String xdpath =  xpos.substring(0, ndx) + '$';
		String jpath;
		if (xpath == null) {
			jpath = "";
			ndy1 = ndy = 0;
		} else {
			jpath = "$";
			ndy = xpath.indexOf('/') + 1;
			ndy1 = xpath.indexOf('/', ndy);
			if (ndy1 < 0) {
				ndy1 = xpath.length();
			}
		}
		boolean wasArray = false;
		String arrayInfo1 = "";
		String arrayInfo2 = "";
		while (ndx1 >= 0 && (ndy1 >= 0 || xpath == null)) {
			String s = xpos.substring(ndx, ndx1);
			int m = s.indexOf(':');
			if (m >= 0) {
				s = s.substring(m + 1);
			}
			m = 0;
			if (s.endsWith("]")) {
				int i = s.indexOf('[');
				if (i > 0) {
					m = Integer.parseInt(s.substring(i + 1, s.length() -1));
					if (m > 0) {
						m--;
					}
				}
			}
			String t = xpath!=null ? xpath.substring(ndy, ndy1) : "";
			int n = t.indexOf(':');
			if (n >= 0) {
				t = t.substring(n + 1);
			}
			n = 0;
			if (t.endsWith("]")) {
				int i = t.indexOf('[');
				if (i > 0) {
					n = Integer.parseInt(t.substring(i + 1, t.length() -1));
					if (n > 0) {
						n--;
					}
				}
			}
			if (wasArray) {
				arrayInfo1 = "[" + m + "]";
				arrayInfo2 = !t.isEmpty() ? "[" + n + "]" : "";
				wasArray = false;
			}
			if (s.startsWith(X_ARRAY)) {
				XMNode[] ynodes = xnodes;
				xnodes = null;
				for (int i=0, j=0; i < ynodes.length; i++) {
					XMNode x = ynodes[i];
					if (X_ARRAY.equals(x.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(x) + arrayInfo1;
							jpath += !t.isEmpty() ?getItemName(x)+arrayInfo2:"";
							arrayInfo1 = arrayInfo2 = "";
							xnodes = ((XMElement)x).getChildNodeModels();
							wasArray = true;
							break;
						}
						j++;
					}
				}
				if (xnodes==null) {
					return result;
				}
			} else if (s.startsWith(X_MAP)) {
				XMNode[] ynodes = xnodes;
				xnodes = null;
				for (int i=0, j=0; i < ynodes.length; i++) {
					XMNode x = ynodes[i];
					if (X_MAP.equals(x.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(x) + arrayInfo1;
							jpath += !t.isEmpty() ?getItemName(x)+arrayInfo2 :"";
							wasArray = false;
							arrayInfo1 = arrayInfo2 = "";
							xnodes = ((XMElement)x).getChildNodeModels();
							break;
						}
						j++;
					}
				}
				if (xnodes==null) {
					return result;
				}
			} else if (s.startsWith(X_VALUE)) {
				XMNode[] ynodes = xnodes;
				for (int i=0, j=0; i < ynodes.length; i++) {
					XMNode xn = ynodes[i];
					if (X_VALUE.equals(xn.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(xn) + arrayInfo1;
							jpath += !t.isEmpty()
								? getItemName(xn) + arrayInfo2 : "";
							return new String[]{xdpath, xpath!=null?jpath:null};
						}
						j++;
					}
				}
				return result;
			}
			ndx = ndx1 + 1;
			if (ndx >= xpos.length()) {
				break;
			}
			if ((ndx1 = xpos.indexOf('/', ndx)) < 0) {
				ndx1 = xpos.length();
			}
			if (!s.startsWith("$") && xpath != null) {
				ndy = ndy1 + 1;
				if (ndy >= xpath.length()) {
					break;
				}
				if ((ndy1 = xpath.indexOf('/', ndy)) < 0) {
					ndy1 = xpath.length();
				}
			}
		}
		return new String[]{xdpath, xpath!=null?jpath:null};
	}

	/** Get XPosition, XPath and source position for modification information
	 * in message reporting.
	 * @param xpos string with X-position.
	 * @param xpath XPath of data (may be null).
	 * @return modification information (convert to XON format if XON).
	 */
	final String getPosMod(final String xpos, final String xpath) {
		String[] x = getPosInfo(xpos, xpath);
		String result = "";
		if (x[0] != null) {
			result += "&{xdpos}" + x[0];
		}
		if (x[1] != null) {
			result += "&{xpath}" + x[1];
		}
		SPosition t = getSPosition();
		if (t != null) {
			if (t.getLineNumber() > 0) {
				result += "&{line}" + t.getLineNumber();
				result += "&{column}" + t.getColumnNumber();
			}
			if (t.getSysId() != null && !t.getSysId().isEmpty()) {
				result += "&{sysId}" + t.getSysId();
			}
		}
		return result;
	}
}
