package org.xdef.xml;

import org.xdef.sys.SRuntimeException;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;

/** Interface for XQuery expression.
 * @author Vaclav Trojan
 */
public interface KXquery {

	/** Get implicit time zone.
	 * @return implicit time zone.
	 * @throws SRuntimeException if an error occurs.
	 */
	public TimeZone getImplicitTimeZone() throws SRuntimeException;

	/** Set implicit time zone.
	 * @param tz time zone to be set as implicit.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setImplicitTimeZone(final TimeZone tz) throws SRuntimeException;

	/** Get array with QNames of external variables
	 * @return array with QNames of external variables or <tt>null</tt>.
	 */
	public QName[] getAllExternalVariables();

	/** Get array with QNames of unbound external variables
	 * @return array with QNames of unbound external variables or <tt>null</tt>.
	 */
	public QName[] getAllUnboundExternalVariables();

	/** Bind variable to XQuery expression.
	 * @param qname QName of variable.
	 * @param value object to be bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void bindValue(final QName qname, final Object value);

	/** Execute precompiled XQuery expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @return object with result of XQuery expression.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Object evaluate(final Node node) throws SRuntimeException;

	/** Execute XQuery expression and return result.
	/* If result type is <tt>null</tt> then result types are checked in
	 * following sequence:
	 * @return object with result of XQuery expression.
	 */
	public Object evaluate() throws SRuntimeException;

	/** Creates a new instance of KXQueryExpr from other expression with
	 * compiled new expression. The name space context, functions and variables
	 * are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 * @return the KXqueryExpr object.
	 */
	public KXquery newExpression(final String source);

}