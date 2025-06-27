package org.xdef.xml;

import java.lang.reflect.InvocationTargetException;
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
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
			| InstantiationException | NoSuchMethodException | SecurityException
			| InvocationTargetException | Error ex) {
			x = null;
		}
		XDS = x;
	}

	/** Creates a new instance of KXqueryExpr from other expression with compiled new expression.
	 * The namespace context, functions and variables are retrieved  from the argument.
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

	/** Creates a new instance of KXQueryExpr from other expression with compiled new expression.
	 * The namespace context, functions and variables are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 * @return the KXqueryExpr object.
	 */
	@Override
	public KXqueryExpr newExpression(final String source) {return new KXqueryExpr(source);}

	/** Set implicit time zone.
	 * @param tz time zone to be set as implicit.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public void setImplicitTimeZone(final TimeZone tz) throws SRuntimeException{
		if (_impl != null) {
			_impl.setImplicitTimeZone(tz);
		}
	}

	/** Get implicit time zone.
	 * @return implicit time zone.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public TimeZone getImplicitTimeZone() throws SRuntimeException {
		return (_impl != null) ? _impl.getImplicitTimeZone() : null;
	}

	/** Get array with QNames of external variables
	 * @return array with QNames of external variables or null.
	 */
	@Override
	public QName[] getAllExternalVariables() {return (_impl!=null) ? _impl.getAllExternalVariables() : null;}

	/** Get array with QNames of unbound external variables
	 * @return array with QNames of unbound external variables or null.
	 */
	@Override
	public QName[] getAllUnboundExternalVariables() {
		return (_impl != null) ? _impl.getAllUnboundExternalVariables() : null;
	}

	/** Bind variable to XQuery expression.
	 * @param qname QName of variable.
	 * @param value object to be bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public void bindValue(final QName qname, final Object value) throws SRuntimeException {
		if (_impl != null) {
			_impl.bindValue(qname, value);
		}
	}

	/** Execute compiled XQuery expression and return result.
	 * @param node node or null.
	 * @return object with result of XQuery expression.
	 * @throws SRuntimeException if an error occurs.
	 */
	@Override
	public Object evaluate(final Node node) throws SRuntimeException {
		return _impl != null ? _impl.evaluate(node) : null;
	}

	/** Execute XQuery expression and return result. If result type is null then result types are checked in
	 * following sequence:
	 * @return object with result of XQuery expression.
	 */
	@Override
	public Object evaluate() {return _impl != null ? _impl.evaluate() : null;}

	/** Get string with source expression.
	 * @return string with source expression.
	 */
	public String getSourceExpr() {return _source;}

	/** Execute XQuery expression with context node and return result.
	 * @param node node or null.
	 * @param expr String with XQuery expression.
	 * @return the result of evaluation as XQResultSequence object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static Object evaluate(final String expr, final Node node) throws SRuntimeException {
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