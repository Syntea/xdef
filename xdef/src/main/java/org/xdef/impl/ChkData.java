package org.xdef.impl;

import org.xdef.impl.code.DefParseResult;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.impl.xml.KNamespace;
import org.xdef.component.XComponent;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDResultSet;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXData;
import org.xdef.proc.XXNode;
import org.xdef.model.XMData;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.ReportWriter;
import org.xdef.XDContainer;
import org.xdef.impl.code.CodeUniqueset;

/** Contains data from X-definition or it can be used as a base
 * for construction of XML objects according to a X-definition.
 * @author Vaclav Trojan
 */
abstract class ChkData extends XDValueAbstract implements XXData {

	final private XData _xDataModel;
	private DefParseResult _parseResult;
	final private ChkElement _parent;
	private Object _userObject;
	private final Node _node;

	ChkData(Node node, ChkElement parent, XData xDataModel) {
		_xDataModel = xDataModel;
		_parent = parent;
		_node = node;
	}

	/** Get default table with references IDS (used internally in the
	 * processor of XScript).
	 * @return default table with references IDS or <tt>null</tt>.
	 */
	public final CodeUniqueset getIdRefTable() {return _parent.getIdRefTable();}

	@Override
	/** Get text value of this node.
	 * @return The name of node.
	 */
	public final String getTextValue() {return _parent.getTextValue();}

	@Override
	/** Set text value to this node.
	 * @param text the text value to be set.
	 */
	public final void setTextValue(String text) {_parent.setTextValue( text);}

	@Override
	/** Get parsed result of an attribute or text node.
	 * @return parsed result of an attribute or text node.
	 */
	public final XDParseResult getParseResult() {
		return _parent.getParseResult();
	}

	@Override
	/** Get XDDocument.
	 * @return XDDocument.
	 */
	public final XDDocument getXDDocument() {return _parent.getXDDocument();}

	@Override
	/** Get XDPool.
	 * @return XDPool.
	 */
	public final XDPool getXDPool() {return _parent.getXDPool();}

	@Override
	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getRootXXElement() {
		return _parent.getRootXXElement();
	}

	@Override
	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	public final XXElement getXXElement()  {return _parent;}

////////////////////////////////////////////////////////////////////////////////

	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	final ChkElement getChkElement() {return _parent;}

	/** Get Element value assigned to this node.
	 * @return Element value assigned to this node.
	 */
	final Element getElemValue() {return _parent.getElemValue();}

	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	final void setElemValue(final Element elem) {_parent.setElemValue(elem);}

	@Override
	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	public final XMData getXMData() {return _xDataModel;}

	@Override
	/** Get namespace context of the parent element.
	 * @return namespace context of the parent element.
	 */
	public final KNamespace getXXNamespaceContext() {
		return _parent.getXXNamespaceContext();
	}

	@Override
	public final String getXXName() {return _xDataModel.getName();}

	@Override
	public final String getXXNSURI() {return _xDataModel.getNSUri();}

	@Override
	public final Object getUserObject() {
		return _userObject != null ? _userObject : _parent.getUserObject();
	}

	@Override
	public final void setUserObject(Object obj) {_userObject = obj;}

	@Override
	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <tt>null</tt>.
	 */
	public final Object setUserObject(final String id, final Object obj) {
		return _parent.setUserObject(id, obj);
	}

	@Override
	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public final Object removeUserObject(final String id) {
		return _parent.removeUserObject(id);
	}

	@Override
	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public final Object getUserObject(final String id) {
		return _parent.getUserObject(id);
	}

	@Override
	public final String getXPos() {return _parent.getXPos();}

	@Override
	/** Get source position.
	 * @return source position or <tt>null</tt> if position is not available.
	 */
	public final SPosition getSPosition() {return _parent.getSPosition();}

	@Override
	public final XXNode getParent() {return _parent;}

	@Override
	public final XXNode[] getChildXXNodes() {return null;}

	@Override
	public final XMElement getXMElement() {return _parent.getXMElement();}

	@Override
	public final String getXDPosition() {return _parent.getXDPosition();}

	@Override
	public final XMDefinition getXMDefinition() {
		return _parent.getXMDefinition();
	}

	@Override
	public final String[] getVariableNames() {
		return _parent.getVariableNames();
	}

	@Override
	public final XDValue getVariable(String name) {
		return _parent.getVariable(name);
	}

	@Override
	public final void setVariable(final String name, final long value) {
		_parent.setVariable(name, value);
	}

	@Override
	public final void setVariable(final String name, final double value) {
		_parent.setVariable(name, value);
	}

	@Override
	public final void setVariable(final String name, final boolean value) {
		_parent.setVariable(name, value);
	}

	@Override
	public final void setVariable(final String name, final Object value) {
		_parent.setVariable(name, value);
	}

	@Override
	public final SReporter getReporter() {return _parent.getReporter();}

	@Override
	public final ReportWriter getReportWriter() {
		return _parent.getReportWriter();
	}

	@Override
	public final boolean errorWarnings() {return _parent.errorWarnings();}

	@Override
	public final boolean errors() {return _parent.errors();}

	@Override
	public final ArrayReporter getTemporaryReporter() {
		return _parent.getTemporaryReporter();
	}

	@Override
	public final ArrayReporter setTemporaryReporter(
		final ArrayReporter reporter) {
		return _parent.setTemporaryReporter(reporter);
	}

	@Override
	public final void clearTemporaryReporter() {
		_parent.clearTemporaryReporter();
	}

	@Override
	public final boolean removeTemporaryReport(final Report rep) {
		return _parent.removeTemporaryReport(rep);
	}

	@Override
	public final boolean copyTemporaryReports() {
		return _parent.copyTemporaryReports();
	}

	@Override
	public final void putTemporaryReport(final Report message) {
		_parent.putTemporaryReport(message);
	}

	@Override
	public final boolean chkTemporaryErrors() {
		return _parent.chkTemporaryErrors();
	}

	@Override
	public final void fatal(final String id,
		final String msg,
		final Object... modif) {
		_parent.fatal(id, msg, modif);
	}

	@Override
	public final void error(final String id,
		final String msg,
		final Object... modif) {
		_parent.error(id, msg, modif);
	}

	@Override
	public final void warning(final String id,
		final String msg,
		final Object... modif) {
		_parent.warning(id, msg, modif);
	}

	@Override
	public final void fatal(final long id, final Object... modif) {
		_parent.fatal(id, modif);
	}

	@Override
	public final void error(final long id, final Object... modif) {
		_parent.error(id, modif);
	}

	@Override
	public final void warning(final long id, final Object... modif) {
		_parent.warning(id, modif);
	}

	@Override
	public final void putReport(final Report report){_parent.putReport(report);}

	@Override
	public final XPathFunctionResolver getXXFunctionResolver() {
		return _parent.getXXFunctionResolver();
	}

	@Override
	public final XPathVariableResolver getXXVariableResolver() {
		return _parent.getXXVariableResolver();
	}

	@Override
	public final XDValue getXDContext() {return _parent.getXDContext();}

	@Override
	public final void setXDContext(XDContainer xdc) {_parent.setXDContext(xdc);}

	@Override
	public final void setXDContext(final XDResultSet xdc) {
		_parent.setXDContext(xdc);
	}

	@Override
	public final void setXDContext(final Node node){_parent.setXDContext(node);}

	@Override
	/** Set source element as context for create mode.
	 * @param source string with pathname, URL or source of XML node.
	 */
	public final void setXDContext(final String source) {
		_parent.setXDContext(source);
	}

	@Override
	public abstract short getItemId();

	@Override
	public final XDValue cloneItem() {return this;}

	@Override
	public final String stringValue() {
		return _node == null ? null : _node.getNodeValue();
	}

	@Override
	public final Node getXMLNode() {return _node;}

	@Override
	public final Element getElement() {return _parent.getElement();}

	@Override
	/** Get actual model.
	 * @return actual model.
	 */
	public final XMNode getXMNode() {return _parent.getXMNode();}

	@Override
	/** Get XComponent.
	 * @return The XComponent object (may be <tt>null</tt>).
	 */
	public final XComponent getXComponent() {return _parent.getXComponent();}

	@Override
	/** Set XComponent.
	 * @param x XComponent object.
	 */
	public final void setXComponent(final XComponent x) {
		_parent.setXComponent(x);
	}

}