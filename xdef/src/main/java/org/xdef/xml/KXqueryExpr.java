package org.xdef.xml;

import org.xdef.sys.SRuntimeException;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;

/** XQuery expression.
 * @author Vaclav Trojan
 */
public class KXqueryExpr implements KXquery {
	/** Implementation of XQuery expression. */
	private static final KXquery XDS;

	/** Executable XQuery engine. */
	private final KXquery _impl;
	/** Source string with XQuery expression. */
	private final String _source;

	static {
		KXquery x;
		try {
			Class<?> cls = Class.forName("org.xdef.impl.saxon.XQuerySaxonExpr");
			x = (KXquery) cls.getConstructor().newInstance();
		} catch (Exception ex) {
			x = null;
		} catch (Error ex) {
			x = null;
		}
		XDS = x;
	}

	/** Creates a new instance of KXqueryExpr from other expression with
	 * compiled new expression. The name space context, functions and variables
	 * are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 */
	public KXqueryExpr(String source) {
		_source = source != null ? source.intern(): null;
		if (XDS != null) {
			_impl = XDS.newExpression(source);
		} else {
			throw new SRuntimeException();
		}
	}

	@Override
	/** Creates a new instance of KXQueryExpr from other expression with
	 * compiled new expression. The name space context, functions and variables
	 * are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 * @return the KXqueryExpr object.
	 */
	public KXqueryExpr newExpression(final String source) {
		return new KXqueryExpr(source);
	}

	@Override
	/** Set implicit time zone.
	 * @param tz time zone to be set as implicit.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setImplicitTimeZone(final TimeZone tz) throws SRuntimeException{
		if (_impl != null) {
			_impl.setImplicitTimeZone(tz);
		}
	}

	@Override
	/** Get implicit time zone.
	 * @return implicit time zone.
	 * @throws SRuntimeException if an error occurs.
	 */
	public TimeZone getImplicitTimeZone() throws SRuntimeException {
		return (_impl != null) ? _impl.getImplicitTimeZone() : null;
	}

	@Override
	/** Get array with QNames of external variables
	 * @return array with QNames of external variables or <tt>null</tt>.
	 */
	public QName[] getAllExternalVariables() {
		return (_impl != null) ? _impl.getAllExternalVariables() : null;
	}

	@Override
	/** Get array with QNames of unbound external variables
	 * @return array with QNames of unbound external variables or <tt>null</tt>.
	 */
	public QName[] getAllUnboundExternalVariables() {
		return (_impl != null) ? _impl.getAllUnboundExternalVariables() : null;
	}

	@Override
	/** Bind variable to XQuery expression.
	 * @param qname QName of variable.
	 * @param value object to be bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void bindValue(final QName qname, final Object value)
		throws SRuntimeException {
		if (_impl != null) {
			_impl.bindValue(qname, value);
		}
	}

	@Override
	/** Execute precompiled XQuery expression and return result.
	 * @param node node or <tt>null</tt>.
	 * @return object with result of XQuery expression.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Object evaluate(final Node node) throws SRuntimeException {
		return _impl != null ? _impl.evaluate(node) : null;
	}

	@Override
	/** Execute XQuery expression and return result.
	/* If result type is <tt>null</tt> then result types are checked in
	 * following sequence:
	 * @return object with result of XQuery expression.
	 */
	public Object evaluate() {
		return _impl != null ? _impl.evaluate() : null;
	}

	/** Get string with source expression.
	 * @return string with source expression.
	 */
	public String getSourceExpr() {return _source;}

	/** Execute XQuery expression with context node and return result.
	 * @param node node or <tt>null</tt>.
	 * @param expr String with XQuery expression.
	 * @return the result of evaluation as XQResultSequence object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static Object evaluate(final String expr, final Node node)
		throws SRuntimeException {
		return new KXqueryExpr(expr).evaluate(node);
	}

	/** Execute XQuery expression without context from the argument.
	 * @param expr String with XQuery expression.
	 * @return the result of evaluation as XQResultSequence object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static Object evaluate(final String expr) throws SRuntimeException {
		return new KXqueryExpr(expr).evaluate();
	}

	/** Check if XQuery implementation is available.
	 * @return true if XQuery implementation is available.
	 */
	public static final boolean isXQueryImplementation() {return XDS != null;}

}