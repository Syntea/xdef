package org.xdef.impl;

import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefDouble;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.impl.xml.KNamespace;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDInput;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.proc.XXNode;
import org.xdef.XDResultSet;
import org.xdef.XDValueAbstract;
import org.xdef.XDValue;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMVariableTable;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.xdef.sys.ReportWriter;
import org.xdef.XDContainer;
import org.xdef.impl.code.CodeUniqueset;
import org.xdef.impl.code.DefLocale;
import java.util.Locale;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;

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
	private ArrayList<XDValue> _finalList;
	/** Actual number of errors.*/
	int _errCount;

	/** Create the new instance of ChkNode.
	 * @param name The name of node.
	 * @param parent The parent node.
	 */
	ChkNode(final String name, final ChkNode parent) {
//		_xElement=null; _node=null; _element=null;_xPos=null; //Java makes it!
//		_variables=null; _userObject=null; _sourceElem=null; //Java makes it!
//		_iterator= null; _scp=null; //Java makes it!
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
			_finalList = new ArrayList<XDValue>();
		}
		_finalList.add(x);
	}

	/** Get list of object to be managed before dispose.*/
	final ArrayList<XDValue> getFinalList() {return _finalList;}

	@Override
	/** Get name of the X-model.
	 * @return The name of node.
	 */
	public final String getXXName() {return _name;}

	@Override
	/** Get namespace URI of the X-model.
	 * @return namespace URI of node or <tt>null</tt>.
	 */
	public String getXXNSURI() {
		return (_xElement == null) ? null : _xElement.getNSUri();
	}

	@Override
	/** get User object.
	 * @return The user object.
	 */
	public final Object getUserObject() {return _userObject;}

	@Override
	/** set User object.
	 * @param obj The user object.
	 */
	public final void setUserObject(final Object obj) {_userObject = obj;}

	@Override
	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <tt>null</tt>.
	 */
	public final Object setUserObject(final String id, final Object obj) {
		return _scp.setUserObject(id, obj);
	}

	@Override
	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public Object removeUserObject(final String id) {
		return _scp.removeUserObject(id);
	}

	@Override
	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public final Object getUserObject(final String id) {
		return _scp.getUserObject(id);
	}

	@Override
	/** Return parent node.
	 * @return The parent node.
	 */
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
	public final void setStdOut(final PrintStream out) {
		setStdOut(new DefOutStream(out));
	}

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
	public final void setStdIn(final InputStream in) {
		setStdIn(new DefInStream(in, false));
	}

	/** Set standard input stream.
	 * @param in XDInputStream object.
	 */
	public final void setStdIn(final XDInput in) {_scp.setStdIn(in);}

	/** Get default table with references IDS (used internally in the
	 * processor of XScript).
	 * @return default table with references IDS or <tt>null</tt>.
	 */
	public final CodeUniqueset getIdRefTable() {return _scp.getIdRefTable();}

	/** Set source element as context for create mode.
	 * @param sourceElem source element to be set (create mode).
	 */
	public final void setCreateContext(final Element sourceElem) {
		_sourceElem = sourceElem;
	}

	/** Get actual source context for create mode.
	 * @return source context or <tt>null</tt> if not available.
	 */
	public final Object getCreateContext() {return _sourceElem;}

	final void debugXPos(final char action) {
		if (_scp.isDebugMode()) {
			if (_scp.getDebugger().hasXPos(action + _xPos)) {
				_scp.getDebugger().debug(this, null, -1, -1, null, null,
					getXDPool().getDebugInfo(), null, (byte) 0);
			}
		}
	}

	@Override
	/** Get actual source context for create mode.
	 * @return source context or <tt>null</tt> if not available.
	 */
	public final XDValue getXDContext() {
		if (_sourceElem != null) {
			return new DefElement(_sourceElem);
		}
		return null;
	}

	@Override
	/** Set value from argument as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	public final void setXDContext(final XDContainer xdc) {
		if (xdc != null && xdc.getXDItemsNumber() == 1 &&
			xdc.getXDItem(0).getItemId() == XD_ELEMENT) {
			_sourceElem = xdc.getXDItem(0).getElement();
			return;
		}
		_sourceElem = xdc == null ? null : xdc.toElement(null, "_");
	}

	@Override
	/** Set source element as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	public final void setXDContext(final XDResultSet xdc) {_iterator = xdc;}

	@Override
	/** Set source element as context for create mode.
	 * @param node XML node (Element or a Node). If this argument is not an
	 * Element then it represents Document element of owner document.
	 */
	public final void setXDContext(final Node node) {
		if (node == null) {
			_sourceElem = null;
		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
			_sourceElem = (Element) node;
		} else if (node.getNodeType() == Node.DOCUMENT_NODE) {
			_sourceElem = ((Document) node).getDocumentElement();
		} else {
			_sourceElem = node.getOwnerDocument().getDocumentElement();
		}
	}

	@Override
	/** Set source element as context for create mode.
	 * @param source string with pathname, URL or source of XML node.
	 */
	public final void setXDContext(final String source) {
		setXDContext(KXmlUtils.parseXml(source));
	}

	@Override
	/** Get names of variables.
	 * @return array of names of variables.
	 */
	public final String[] getVariableNames() {
		XMVariableTable t=_rootChkDocument._xdef.getXDPool().getVariableTable();
		return  t.getVariableNames();
	}

	@Override
	/** Get XDDocument.
	 * @return XDDocument.
	 */
	public final XDDocument getXDDocument() {return _rootChkDocument;}

	@Override
	/** Get XDPool.
	 * @return XDPool.
	 */
	public final XDPool getXDPool() {
		return _rootChkDocument.getXMDefinition().getXDPool();
	}

	@Override
	/** Get value of variable from XMDefinition.
	 * @param name name of variable.
	 * @return XDValue object or <tt>null</tt>.
	 */
	public final XDValue getVariable(final String name) {
		return _scp.getVariable(name);
	}

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
		//Variable '&{0}' doesn't exist
		throw new SRuntimeException(XDEF.XDEF563, name);
	}

	@Override
	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
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
			case XD_CONTAINER:
				_scp.setVariable(xv, new DefContainer(value));
				return;
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
		if (value instanceof String) {setVariable(name, (String) value);
		} else if (value instanceof Long) {setVariable(name, (Long) value);
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
		} else {
			//Value is not compatible with the type of variable '&{0}'
			throw new SRuntimeException(XDEF.XDEF564, name);
		}
	}

	@Override
	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public final void setVariable(final String name, final long value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_FLOAT:
				_scp.setVariable(xv, new DefDouble(value));
				return;
			case XD_INT:
				_scp.setVariable(xv, new DefLong(value));
				return;
			case XD_DECIMAL:
				_scp.setVariable(xv, new DefDecimal(value));
				return;
			case XD_STRING:
				_scp.setVariable(xv, new DefString(String.valueOf(value)));
				return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	@Override
	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public final void setVariable(final String name, final double value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_FLOAT:
				_scp.setVariable(xv, new DefDouble(value));
				return;
			case XD_STRING:
				_scp.setVariable(xv, new DefString(String.valueOf(value)));
				return;
			case XD_DECIMAL:
				_scp.setVariable(xv, new DefDecimal(String.valueOf(value)));
				return;
		}
		//Value is not compatible with the type of variable '&{0}'
		throw new SRuntimeException(XDEF.XDEF564, name);
	}

	@Override
	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public final void setVariable(final String name, final boolean value) {
		XVariable xv = findVariable(name);
		switch (xv.getType()) {
			case XD_BOOLEAN:
				_scp.setVariable(xv, new DefBoolean(value));
				return;
			case XD_STRING:
				_scp.setVariable(xv, new DefString(String.valueOf(value)));
				return;
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
				case XD_CONTAINER:
					_scp.setVariable(xv, new DefContainer(value));
					return;
				case XD_STRING:
					setVariable(name, value.toString());
					return;
				case XD_FLOAT:
					if (value.getItemId() == XD_INT) {
						setVariable(name, value.doubleValue());
						return;
					}
					break;
				case XD_INT:
					if (value.getItemId() == XD_FLOAT) {
						setVariable(name, value.longValue());
						return;
					}
					break;
				case XD_BOOLEAN: {
					if (value.getItemId() == XD_STRING) {
						_scp.setVariable(xv, new DefBoolean(value.toString()));
						return;
					}
					break;
				}
				case XD_PARSER: {
					_scp.setVariable(xv, (XDParser) value);
					return;
				}
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
			case XD_CONTAINER:
				_scp.setVariable(xv, new DefString(value));
				return;
			case XD_BOOLEAN: {
				_scp.setVariable(xv, new DefBoolean(value));
				return;
			}
			case XD_FLOAT: {
				_scp.setVariable(xv, new DefDouble(value));
				return;
			}
			case XD_INT: {
				_scp.setVariable(xv, new DefLong(value));
				return;
			}
			case XD_DECIMAL: {
				_scp.setVariable(xv, new DefDecimal(value));
				return;
			}
			case XD_ELEMENT: {
				_scp.setVariable(xv, new DefElement(
					KXmlUtils.parseXml(value).getDocumentElement()));
				return;
			}
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
			case XD_INT:
				_scp.setVariable(xv, new DefLong(value.longValue()));
				return;
			case XD_FLOAT:
				_scp.setVariable(xv, new DefDouble(value.doubleValue()));
				return;
			case XD_DECIMAL:
				_scp.setVariable(xv, new DefDecimal(value));
				return;
			case XD_STRING:
				_scp.setVariable(xv, new DefString(value.toString()));
				return;
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
	public final XPathFunctionResolver getXXFunctionResolver() {
		return _scp._functionResolver;
	}

	@Override
	public final XPathVariableResolver getXXVariableResolver() {
		return _scp._variableResolver;
	}

////////////////////////////////////////////////////////////////////////////////
// Methods to retrieve values from checked tree.
////////////////////////////////////////////////////////////////////////////////

	/** Get document with root element.
	 * @return The Document object.
	 */
	public final Document getDocument() {return _rootChkDocument._doc;}

	/** Get document element.
	 * @return root element of the document.
	 */
	public final Element getDocumentElement() {
		return _rootChkDocument._element;
	}

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

	@Override
	/** Get actual element.
	 * @return The element.
	 */
	public final Element getElement() {return _element;}

	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	abstract void setElemValue(Element elem);

	@Override
	/** Get position of the node as a XPath expression string.
	 * @return position of the node as a XPath expression string.
	 */
	public final String getXPos() {return _xPos;}

	@Override
	/** Get source position.
	 * @return source position or <tt>null</tt> if position is not available.
	 */
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

	@Override
	/** Get report writer assigned to the report generator.
	 * @return The report writer.
	 */
	public final ReportWriter getReportWriter() {
		return _rootChkDocument._reporter.getReportWriter();
	}

	@Override
	/** Copy temporary reports to global reporter.
	 * @return true if and only if temporary reporter contained errors before
	 * it was cleared.
	 */
	public final boolean copyTemporaryReports() {
		Report rep;
		while ((rep = _scp.getTemporaryReporter().getReport()) != null) {
			putReport(rep);
		}
		boolean result = _scp.getTemporaryReporter().errors();
		_scp.getTemporaryReporter().clear();
		return result;
	}

	@Override
	/** Clear temporary reporter. */
	public final void clearTemporaryReporter() {
		_scp.getTemporaryReporter().clear();
	}

	@Override
	/** Put message to temporary reporter.
	 * @param report report to be added to the temporary reporter.
	 */
	public final void putTemporaryReport(Report report) {
		ensurePosInfo(report);
		_scp.getTemporaryReporter().putReport(report);
	}

	@Override
	/** Remove report from temporary reporter.
	 * @param rep report to be removed.
	 * @return true if report was found and removed.
	 */
	public final boolean removeTemporaryReport(final Report rep) {
		return _scp.getTemporaryReporter().removeReport(rep);
	}

	@Override
	/** Get temporary reporter.
	 * @return ArrayReporter used as temporary reporter.
	 */
	public final ArrayReporter getTemporaryReporter() {
		return _scp.getTemporaryReporter();
	}

	@Override
	/** Check if temporary reporter has errors.
	 * @return true if temporary reporter has errors.
	 */
	public final boolean chkTemporaryErrors() {
		return _scp.getTemporaryReporter().errors();
	}

	@Override
	/** Set new temporary reporter.
	 * @param reporter new temporary reporter.
	 * @return ArrayReporter old temporary reporter.
	 */
	public final ArrayReporter setTemporaryReporter(ArrayReporter reporter) {
		ArrayReporter result = _scp.getTemporaryReporter();
		_scp.setTemporaryReporter(reporter);
		return result;
	}

	@Override
	/** Get SReporter of XDDocument.
	 * @return SReporter of XDDocument..
	 */
	public final SReporter getReporter() {return _rootChkDocument._reporter;}

	@Override
	/** Check if errors, fatal errors, light errors or warnings were reported.
	 * @return <tt>true</tt> if errors, fatal errors, light errors
	 * or warnings were reported.
	 */
	public boolean errorWarnings() {
		return _rootChkDocument._reporter.errorWarnings();
	}

	@Override
	/** Check if errors, fatal errors or light errors were reported.
	 * @return <tt>true</tt> if errors, fatal errors or light errors were
	 * reported.
	 */
	public boolean errors() {return _rootChkDocument._reporter.errors();}

	@Override
	/** Put fatal error message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.fatal(id, msg, mod));
	}

	@Override
	/** Put error message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public final void error(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}

	@Override
	/** Put warning message with modification parameters.
	 * @param id The message id.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public final void warning(final String id,
		final String msg,
		final Object... mod) {
		putReport(Report.warning(id, msg, mod));
	}

	@Override
	/** Put fatal error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public final void fatal(final long id, final Object... mod) {
		putReport(Report.fatal(id, mod));
	}

	@Override
	/** Put error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(final long id, final Object... mod) {
		putReport(Report.error(id, mod));
	}

	@Override
	/** Put warning message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(final long id, final Object... mod) {
		putReport(Report.warning(id, mod));
	}

	@Override
	/** Put report.
	 * @param report The report.
	 */
	public final void putReport(final Report report) {
		ensurePosInfo(report);
		_rootChkDocument._reporter.putReport(report);
	}

	@Override
	/** Get XMDefinition.
	 * @return X-definition of this document.
	 */
	public final XMDefinition getXMDefinition() {return _rootChkDocument._xdef;}

	@Override
	/** Get model of the processed object.
	 * @return model of the processed object (XMElement).
	 */
	public final XMElement getXMElement() {return _xElement;}

	@Override
	/** Get XDPosition of the processed element.
	 * @return XDPosition of the processed element.
	 */
	public final String getXDPosition() {
		String result = _xElement == null ? null : _xElement.getXDPosition();
		return result == null ? "" : result;
	}

////////////////////////////////////////////////////////////////////////////////
// Interface of XXNode.
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	abstract public XXNode[] getChildXXNodes();

	// can't be final this method is overriden!
	abstract ArrayList<ChkElement> getChkChildNodes();

	@Override
	/** Get parsed result of an attribute or text node.
	 * @return parsed result of an attribute or text node.
	 */
	public XDParseResult getParseResult() {return _parseResult;}

////////////////////////////////////////////////////////////////////////////////
// Message reporting
////////////////////////////////////////////////////////////////////////////////

	public final void ensurePosInfo(final Report report) {
		String mod = report.getModification();
		if (mod == null) {
			mod = getPosMod(getXDPosition(), getXPos());
		} else {
			String[] pos = getPosInfo(getXDPosition(), getXPos());
			if (pos[0] != null && mod.indexOf("&{xdpos}") < 0) {
				mod += "&{xdpos}" + pos[0];
			}
			if (pos[1] != null && mod.indexOf("&{xpath}") < 0) {
				mod += "&{xpath}" + pos[1];
			}
			SPosition sp;
			if (mod.indexOf("&{line}") < 0 && (sp = getSPosition()) != null
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
		XMData key = ((XMElement)xnode).getAttr("key");
		if (key != null) {
			return ".['"+key.getFixedValue().toString() + "']";
		}
		return "";
	}

	/** Get X-position and xpath information. If it is JSON, then create
	 * modified JSON path.
	 * @param xpos X-position of model.
	 * @param xpath Xpath of data (may be null).
	 * @return array with two items - the first one is X-position and the
	 * second one is Xpath.
	 */
	private String[] getPosInfo(final String xpos, final String xpath) {
		String[] result = new String[]{xpos, xpath};
		if (xpos == null) {
			return result;
		}
		int ndx, ndx1, ndy, ndy1;
		if ((ndx = xpos.indexOf('#')) < 0) {
			return result;
		}
		if ((ndx1 = xpos.indexOf('/')) < 0) {
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
		XMNode[] xx = base.getChildNodeModels();
		if (xx == null || base.getJsonMode() == 0) {
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
			if (s.startsWith("array")) {
				XMNode[] yy = xx;
				xx = null;
				for (int i=0, j=0; i < yy.length; i++) {
					XMNode x = yy[i];
					if ("array".equals(x.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(x) + arrayInfo1;
							jpath += !t.isEmpty() ?getItemName(x)+arrayInfo2:"";
							arrayInfo1 = arrayInfo2 = "";
							xx = ((XMElement)x).getChildNodeModels();
							wasArray = true;
							break;
						}
						j++;
					}
				}
				if (xx==null) {
					return result;
				}
			} else if (s.startsWith("map")) {
				XMNode[] yy = xx;
				xx = null;
				for (int i=0, j=0; i < yy.length; i++) {
					XMNode x = yy[i];
					if ("map".equals(x.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(x) + arrayInfo1;
							jpath += !t.isEmpty() ?getItemName(x)+arrayInfo2 :"";
							wasArray = false;
							arrayInfo1 = arrayInfo2 = "";
							xx = ((XMElement)x).getChildNodeModels();
							break;
						}
						j++;
					}
				}
				if (xx==null) {
					return result;
				}
			} else if (s.startsWith("item")) {
				XMNode[] yy = xx;
				xx = null;
				for (int i=0, j=0; i < yy.length; i++) {
					XMNode x = yy[i];
					if ("item".equals(x.getLocalName())) {
						if (j == m) { // model found
							xdpath += getItemName(x) + arrayInfo1;
							jpath += !t.isEmpty() ?getItemName(x)+arrayInfo2:"";
							arrayInfo1 = arrayInfo2 = "";
							wasArray = false;
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
	 * @return modification information (convert to JSON format if JSON).
	 */
	public final String getPosMod(final String xpos, final String xpath) {
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