package org.xdef.impl.saxon;

import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xdef.msg.XML;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.xml.KXquery;

/** XQuery expression container.
 * @author Vaclav Trojan
 */
public class XQuerySaxonExpr implements KXquery {
	private final static XQDataSource XDS = new com.saxonica.xqj.SaxonXQDataSource();
	/** XQuery engine. */
	private XQConnection _conn;
	XQPreparedExpression _value;

	public XQuerySaxonExpr() {}

	/** Creates a new instance of KXqueryExpr from other expression with compiled new expression.
	 * The namespace context, functions and variables are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 */
	private XQuerySaxonExpr(String source) {
		try {
			_conn = XDS.getConnection();
			_value = _conn.prepareExpression(source);
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Creates a new instance of KXQueryExpr from other expression with compiled new expression.
	 * The namespace context, functions and variables* are retrieved  from the argument.
	 * @param source String with XQuery expression.
	 * @return the KXqueryExpr object.
	 */
	public KXquery newExpression(final String source) {return new XQuerySaxonExpr(source);}

	@Override
	/** Set implicit time zone.
	 * @param tz time zone to be set as implicit.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void setImplicitTimeZone(final TimeZone tz) throws SRuntimeException{
		try {
			_value.setImplicitTimeZone(tz);
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Get implicit time zone.
	 * @return implicit time zone.
	 * @throws SRuntimeException if an error occurs.
	 */
	public TimeZone getImplicitTimeZone() throws SRuntimeException {
		try {
			return _value.getImplicitTimeZone();
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Get array with QNames of external variables
	 * @return array with QNames of external variables or null.
	 */
	public QName[] getAllExternalVariables() {
		try {
			return _value.getAllExternalVariables();
		} catch (XQException ex) {
			return null;
		}
	}

	@Override
	/** Get array with QNames of unbound external variables
	 * @return array with QNames of unbound external variables or null.
	 */
	public QName[] getAllUnboundExternalVariables() {
		try {
			return _value.getAllUnboundExternalVariables();
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Bind variable to XQuery expression.
	 * @param qname QName of variable.
	 * @param value object to be bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void bindValue(final QName qname, final Object value) throws SRuntimeException {
		QName[] qnames = getAllExternalVariables();
		if (qnames == null || qnames.length == 0 || value == null) {
			return;
		}
		boolean found = false;
		for (QName qn : qnames) {
			if (qname.equals(qn)) {
				found = true;
			}
		}
		if (!found) {
			return;
		}
		try {
			if (value instanceof Byte) {
				_value.bindByte(qname, ((Byte)value), null);
			} else if (value instanceof Short) {
				_value.bindShort(qname, ((Short)value), null);
			} else if (value instanceof Integer) {
				_value.bindInt(qname, ((Integer)value), null);
			} else if (value instanceof Long) {
				_value.bindLong(qname, ((Long)value), null);
			} else if (value instanceof Float) {
				_value.bindFloat(qname, ((Float) value), null);
			} else if (value instanceof Double) {
				_value.bindDouble(qname, ((Double) value), null);
			} else if (value instanceof Node) {
				_value.bindNode(qname, (Node) value, null);
			} else {
				if (value instanceof SDatetime) {
					int i = XQItemType.XQBASETYPE_DATETIME;
					_value.bindAtomicValue(qname, ((SDatetime) value).toISO8601(), _conn.createAtomicType(i));
				} else if (value instanceof SDuration) {
					int i = XQItemType.XQBASETYPE_DURATION;
					_value.bindAtomicValue(qname, value.toString(), _conn.createAtomicType(i));
				} else {
					_value.bindString(qname, value.toString(), null);
				}
			}
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&amp;{0}{: }
		}
	}

	/** Bind variable to XQuery expression.
	 * @param qname QName of variable.
	 * @param value value to be bound.
	 * @throws SRuntimeException if an error occurs.
	 */
	public void bindValue(final QName qname, final long value) throws SRuntimeException {
		try {
			_value.bindLong(qname, value, null);
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //"XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Execute precompiled XQuery expression and return result.
	 * @param node node or null.
	 * @return object with result of XQuery expression.
	 * @throws SRuntimeException if an error occurs.
	 */
	public Object evaluate(final Node node) throws SRuntimeException {
		try {
			if (node == null) {
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.DOCUMENT_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM,
					_conn.createItemFromNode(node, _conn.createDocumentType()));
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM,
					_conn.createItemFromNode(node,
						_conn.createElementType(KXmlUtils.getQName(node), XQItemType.XQBASETYPE_ANYTYPE)));
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.TEXT_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM,
					_conn.createItemFromNode(node, _conn.createTextType()));
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM, _conn.createItemFromNode(
					node,_conn.createProcessingInstructionType(node.getNodeValue())));
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.COMMENT_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM,
					_conn.createItemFromNode(node, _conn.createCommentType()));
				return _value.executeQuery();
			} else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				_value.bindItem(XQConstants.CONTEXT_ITEM, _conn.createItemFromNode(node,
					_conn.createAttributeType(KXmlUtils.getQName(node),XQItemType.XQBASETYPE_ANYATOMICTYPE)));
				return _value.executeQuery();
			} else {
				//XQuery expression error&{0}{: }
				throw new SRuntimeException(XML.XML506,"Unknown argument type");
			}
		} catch (XQException | DOMException | SRuntimeException ex) {
			if (ex instanceof SRuntimeException) {
				throw (SRuntimeException) ex;
			}
			if (node == null) {
				return null;
			}
			throw new SRuntimeException(XML.XML506, ex); //"XQuery expression error&{0}{: }
		}
	}

	@Override
	/** Execute XQuery expression and return result. If result type is null then result types are checked.
	 * @return object with result of XQuery expression.
	 */
	public Object evaluate() {
		try {
			return _value.executeQuery();
		} catch (XQException ex) {
			throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
		}
	}
}