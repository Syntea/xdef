package org.xdef.proc;

import org.xdef.model.XMElement;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SPosition;
import org.xdef.sys.SReporter;
import org.xdef.impl.xml.KNamespace;
import org.xdef.component.XComponent;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMNode;
import org.w3c.dom.Node;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.xdef.sys.ReportWriter;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDResultSet;
import org.xdef.XDValue;

/** Interface of models in X-definition.
* @author Vaclav Trojan
 */
public interface XXNode extends XDValue {

	/** Get name of the model.
	 * @return The name of node.
	 */
	public String getXXName();

	/** Get namespace URI of the model.
	 * @return namespace URI or <tt>null</tt>.
	 */
	public String getXXNSURI();

	/** Get name of actual node.
	 * @return The name of node.
	 */
	public String getNodeName();

	/** Get namespace URI of actual node.
	 * @return namespace URI or <tt>null</tt>.
	 */
	public String getNodeURI();

	/** Get User object.
	 * @return The user object.
	 */
	public Object getUserObject();

	/** Set User object.
	 * @param obj The user object.
	 */
	public void setUserObject(Object obj);

	/** Set named user object.
	 * @param id identifier of the object.
	 * @param obj user object.
	 * @return previous value of the object or <tt>null</tt>.
	 */
	public Object setUserObject(String id, Object obj);

	/** Remove named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public Object removeUserObject(String id);

	/** Get named user object.
	 * @param id identifier of the object.
	 * @return value of the object or <tt>null</tt>.
	 */
	public Object getUserObject(String id);

	/** Get position of the node as a XPath expression string.
	 * @return position of the node as a XPath expression string.
	 */
	public String getXPos();

	/** Get source position of this model in X-definition.
	 * @return SPosition or <tt>null</tt> if position is not available.
	 */
	public SPosition getSPosition();

	/** Return parent node.
	 * @return The parent node.
	 */
	public XXNode getParent();

	/** Get array of XXNodes or null.
	 * @return array of XXNodes or null.
	 */
	public XXNode[] getChildXXNodes();

	/** Get XDDocument.
	 * @return XDDocument.
	 */
	public XDDocument getXDDocument();

	/** Get XDPool.
	 * @return XDPool.
	 */
	public XDPool getXDPool();

	/** Get root XXElement.
	 * @return root XXElement node.
	 */
	public XXElement getRootXXElement();

	/** Get actual associated XXElement.
	 * @return root XXElement node.
	 */
	public XXElement getXXElement();

	/** Get model of the processed element.
	 * @return model of the processed element.
	 */
	public XMElement getXMElement();

	/** Get XDPosition of the processed element.
	 * @return XDPosition of the processed element.
	 */
	public String getXDPosition();

	/** Get XMDefinition.
	 * @return X-definition of this document.
	 */
	public XMDefinition getXMDefinition();

	/** Get names of global variables.
	 * @return array of names of variables.
	 */
	public String[] getVariableNames();

	/** Get value of variable from X-definition.
	 * @param name name of variable.
	 * @return XDValue object or <tt>null</tt> if variable with the name not
	 * exists.
	 */
	public XDValue getVariable(String name);

	/** Set variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public void setVariable(String name, long value);

	/** Set integer variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public void setVariable(String name, double value);

	/** Set boolean variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public void setVariable(String name, boolean value);

	/** Set boolean variable.
	 * @param name name name of variable.
	 * @param value value to be set to the variable.
	 */
	public void setVariable(String name, Object value);

	/** Get SReporter of XDDocument.
	 * @return SReporter of XDDocument..
	 */
	public SReporter getReporter();

	/** Get report reader created from the reporter assigned to XDDocument.
	 * @return report reader.
	 */
	public ReportWriter getReportWriter();

	/** Check if errors, fatal errors, light errors or warnings were reported.
	 * @return <tt>true</tt> if and only if errors, fatal errors, light errors
	 * or warnings were reported.
	 */
	public boolean errorWarnings();

	/** Check if errors, fatal errors or light errors were reported.
	 * @return <tt>true</tt> if and only if errors, fatal errors or light errors
	 * were reported.
	 */
	public boolean errors();

	/** Get temporary reporter used by XD processor.
	 * @return ArrayReporter used as temporary reporter.
	 */
	public ArrayReporter getTemporaryReporter();

	/** Set new temporary reporter.
	 * @param reporter new temporary reporter.
	 * @return ArrayReporter old temporary reporter.
	 */
	public ArrayReporter setTemporaryReporter(ArrayReporter reporter);

	/** Clear temporary reporter. */
	public void clearTemporaryReporter();

	/** Remove report from temporary reporter.
	 * @param rep report to be removed.
	 * @return true if report was found and removed.
	 */
	public boolean removeTemporaryReport(final Report rep);

	/** Copy temporary reports to global reporter.
	 * @return true if and only if temporary reporter contained errors before it
	 * was cleared.
	 */
	public boolean copyTemporaryReports();

	/** Put message to temporary reporter.
	 * @param message report to be added to the temporary reporter.
	 */
	public void putTemporaryReport(Report message);

	/** Check if temporary reporter has errors.
	 * @return true if temporary reporter has errors.
	 */
	public boolean chkTemporaryErrors();

	/** Put fatal error message with modification parameters.
	 * @param id The message id or <tt>null</tt>.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final String id, final String msg, final Object... mod);

	/** Put error message with modification parameters.
	 * @param id The message id or <tt>null</tt>.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public void error(final String id, final String msg, final Object... mod);

	/** Put warning message with modification parameters.
	 * @param id The message id or <tt>null</tt>.
	 * @param msg The message text.
	 * @param mod Message modification parameters.
	 */
	public void warning(final String id, final String msg, final Object... mod);

	/** Put fatal error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final long id, final Object... mod);

	/** Put error message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(final long id, final Object... mod);

	/** Put warning message with modification parameters.
	 * @param id registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(final long id, final Object... mod);

	/** Put report.
	 * @param report The report.
	 */
	public void putReport(final Report report);

	/** Get nameSpace context of this XXnode.
	 * @return nameSpace context.
	 */
	public KNamespace getXXNamespaceContext();

	/** Get assigned XPathFunctionResolver.
	 * @return XPathFunctionResolver.
	 */
	public XPathFunctionResolver getXXFunctionResolver();

	/** Get assigned XPathVariableResolver.
	 * @return XPathVariableResolver.
	 */
	public XPathVariableResolver getXXVariableResolver();

	/** Get parsed result of an attribute or text node.
	 * @return parsed result of an attribute or text node.
	 */
	public XDParseResult getParseResult();

	/** Get actual creation context (XDResultSet).
	 * @return the XDResultSet or <tt>null</tt> if it is not available.
	 */
	public XDValue getXDContext();

	/** Set value from argument as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	public void setXDContext(XDContainer xdc);

	/** Set source element as context for create mode.
	 * @param xdc context to be set (create mode).
	 */
	public void setXDContext(XDResultSet xdc);

	/** Set source element as context for create mode.
	 * @param node XML node.
	 */
	public void setXDContext(Node node);

	/** Set source element as context for create mode.
	 * @param source string with pathname, URL or source of XML node.
	 */
	public void setXDContext(String source);

	/** Get actual model.
	 * @return actual model.
	 */
	public XMNode getXMNode();

	/** Get XComponent.
	 * @return The XComponent object (may be <tt>null</tt>).
	 */
	public XComponent getXComponent();

	/** Set XComponent.
	 * @param x XComponent object.
	 */
	public void setXComponent(XComponent x);

}