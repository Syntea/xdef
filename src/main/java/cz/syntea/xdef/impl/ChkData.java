/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: ChkAttr.java, created 2010-06-07.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.impl.code.DefParseResult;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SPosition;
import cz.syntea.xdef.sys.SReporter;
import cz.syntea.xdef.xml.KNamespace;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.XDResultSet;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import cz.syntea.xdef.proc.XXElement;
import cz.syntea.xdef.proc.XXData;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.model.XMData;
import cz.syntea.xdef.model.XMDefinition;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.model.XMNode;
import java.util.Map;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.XDContainer;

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

	@Override
	/** Get text value of this node.
	 * @return The name of node.
	 */
	public String getTextValue() {return _parent.getTextValue();}

	@Override
	/** Set text value to this node.
	 * @param text the text value to be set.
	 */
	public void setTextValue(String text) {_parent.setTextValue( text);}

	@Override
	/** Get parsed result of an attribute or text node.
	 * @return parsed result of an attribute or text node.
	 */
	public XDParseResult getParseResult() {return _parent.getParseResult();}

	@Override
	/** Get XDDocument.
	 * @return XDDocument.
	 */
	public XDDocument getXDDocument() {return _parent.getXDDocument();}

	@Override
	/** Get XDPool.
	 * @return XDPool.
	 */
	public final XDPool getXDPool() {return _parent.getXDPool();}

	@Override
	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	public XXElement getRootXXElement() {return _parent.getRootXXElement();}

	@Override
	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	public XXElement getXXElement()  {return _parent;}

////////////////////////////////////////////////////////////////////////////////

	/** Get ChkElement assigned to this node.
	 * @return ChkElement assigned to this node.
	 */
	ChkElement getChkElement() {return _parent;}

	/** Get Element value assigned to this node.
	 * @return Element value assigned to this node.
	 */
	Element getElemValue() {return _parent.getElemValue();}

	/** Assign Element value to this node.
	 * @param elem Element value to be assigned to this node.
	 */
	void setElemValue(Element elem) {_parent.setElemValue(elem);}

	@Override
	/** Get model of the processed data object.
	 * @return model of the processed data object.
	 */
	public XMData getXMData() {return _xDataModel;}

	@Override
	/** Get namespace context of the parent element.
	 * @return namespace context of the parent element.
	 */
	public KNamespace getXXNamespaceContext() {
		return _parent.getXXNamespaceContext();
	}

	@Override
	public String getXXName() {return _xDataModel.getName();}

	@Override
	public String getXXNSURI() {return _xDataModel.getNSUri();}

	@Override
	public Object getUserObject() {
		return _userObject != null ? _userObject : _parent.getUserObject();
	}

	@Override
	public void setUserObject(Object obj) {_userObject = obj;}

	@Override
	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <tt>null</tt>.
	 */
	public Object setUserObject(String id, Object obj) {
		return _parent.setUserObject(id, obj);
	}

	@Override
	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public Object removeUserObject(String id) {
		return _parent.removeUserObject(id);
	}

	@Override
	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public Object getUserObject(String id) {
		return _parent.getUserObject(id);
	}

	@Override
	public String getXPos() {return _parent.getXPos();}

	@Override
	/** Get source position.
	 * @return source position or <tt>null</tt> if position is not available.
	 */
	public SPosition getSPosition() {return _parent.getSPosition();}

	@Override
	public XXNode getParent() {return _parent;}

	@Override
	public XXNode[] getChildXXNodes() {return null;}

	@Override
	public XMElement getXMElement() {return _parent.getXMElement();}

	@Override
	public String getXDPosition() {return _parent.getXDPosition();}

	@Override
	public XMDefinition getXMDefinition() {return _parent.getXMDefinition();}

	@Override
	public String[] getVariableNames() {return _parent.getVariableNames();}

	@Override
	public XDValue getVariable(String name) {return _parent.getVariable(name);}

	@Override
	public void setVariable(String name, long value) {
		_parent.setVariable(name, value);
	}

	@Override
	public void setVariable(String name, double value) {
		_parent.setVariable(name, value);
	}

	@Override
	public void setVariable(String name, boolean value) {
		_parent.setVariable(name, value);
	}

	@Override
	public void setVariable(String name, Object value) {
		_parent.setVariable(name, value);
	}

	@Override
	public SReporter getReporter() {return _parent.getReporter();}

	@Override
	public ReportWriter getReportWriter() {
		return _parent.getReportWriter();
	}

	@Override
	public boolean errorWarnings() {return _parent.errorWarnings();}

	@Override
	public boolean errors() {return _parent.errors();}

	@Override
	public ArrayReporter getTemporaryReporter() {
		return _parent.getTemporaryReporter();
	}

	@Override
	public ArrayReporter setTemporaryReporter(ArrayReporter reporter) {
		return _parent.setTemporaryReporter(reporter);
	}

	@Override
	public void clearTemporaryReporter() {_parent.clearTemporaryReporter();}

	@Override
	public boolean removeTemporaryReport(Report rep) {
		return _parent.removeTemporaryReport(rep);
	}

	@Override
	public boolean copyTemporaryReports() {
		return _parent.copyTemporaryReports();
	}

	@Override
	public void putTemporaryReport(Report message) {
		_parent.putTemporaryReport(message);
	}

	@Override
	public boolean chkTemporaryErrors() {return _parent.chkTemporaryErrors();}

	@Override
	public void fatal(String id, String msg, Object... modif) {
		_parent.fatal(id, msg, modif);
	}

	@Override
	public void error(String id, String msg, Object... modif) {
		_parent.error(id, msg, modif);
	}

	@Override
	public void warning(String id, String msg, Object... modif) {
		_parent.warning(id, msg, modif);
	}

	@Override
	public void fatal(long id, Object... modif) {_parent.fatal(id, modif);}

	@Override
	public void error(long id, Object... modif) {_parent.error(id, modif);}

	@Override
	public void warning(long id, Object... modif) {_parent.warning(id, modif);}

	@Override
	public void putReport(Report report) {_parent.putReport(report);}

	@Override
	public XPathFunctionResolver getXXFunctionResolver() {
		return _parent.getXXFunctionResolver();
	}

	@Override
	public XPathVariableResolver getXXVariableResolver() {
		return _parent.getXXVariableResolver();
	}

	@Override
	public XDValue getXDContext() {return _parent.getXDContext();}

	@Override
	public void setXDContext(XDContainer xdc) {_parent.setXDContext(xdc);}

	@Override
	public void setXDContext(XDResultSet xdc) {_parent.setXDContext(xdc);}

	@Override
	public void setXDContext(Node node) {_parent.setXDContext(node);}

	@Override
	/** Set source element as context for create mode.
	 * @param source string with pathname, URL or source of XML node.
	 */
	public void setXDContext(String source) {_parent.setXDContext(source);}

	@Override
	public abstract short getItemId();

	@Override
	public XDValue cloneItem() {return this;}

	@Override
	public String stringValue() {
		return _node == null ? null : _node.getNodeValue();
	}

	@Override
	public Node getXMLNode() {return _node;}

	@Override
	public Element getElement() {return _parent.getElement();}

	@Override
	/** Get table with references to an object (used internally in the
	 * processor of XScript).
	 * @return table with references to an object or <tt>null</tt>.
	 */
	public Map<Object, ArrayReporter> getIdRefTable() {
		return _parent.getIdRefTable();
	}

	@Override
	/** Get actual model.
	 * @return actual model.
	 */
	public XMNode getXMNode() {return _parent.getXMNode();}

	@Override
	/** Get XComponent.
	 * @return The XComponent object (may be <tt>null</tt>).
	 */
	public XComponent getXComponent() {return _parent.getXComponent();}

	@Override
	/** Set XComponent.
	 * @param x XComponent object.
	 */
	public void setXComponent(XComponent x) {_parent.setXComponent(x);}

}
