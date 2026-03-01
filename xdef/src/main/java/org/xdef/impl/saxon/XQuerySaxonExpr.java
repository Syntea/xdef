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

    /** Creates a new instance of KXQueryExpr from other expression with compiled new expression.
     * The namespace context, functions and variables* are retrieved  from the argument.
     * @param source String with XQuery expression.
     * @return the KXqueryExpr object.
     */
    @Override
    public KXquery newExpression(final String source) {return new XQuerySaxonExpr(source);}

    /** Set implicit time zone.
     * @param tz time zone to be set as implicit.
     * @throws SRuntimeException if an error occurs.
     */
    @Override
    public void setImplicitTimeZone(final TimeZone tz) throws SRuntimeException{
        try {
            _value.setImplicitTimeZone(tz);
        } catch (XQException ex) {
            throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
        }
    }

    /** Get implicit time zone.
     * @return implicit time zone.
     * @throws SRuntimeException if an error occurs.
     */
    @Override
    public TimeZone getImplicitTimeZone() throws SRuntimeException {
        try {
            return _value.getImplicitTimeZone();
        } catch (XQException ex) {
            throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
        }
    }

    /** Get array with QNames of external variables
     * @return array with QNames of external variables or null.
     */
    @Override
    public QName[] getAllExternalVariables() {
        try {
            return _value.getAllExternalVariables();
        } catch (XQException ex) {
            return null;
        }
    }

    /** Get array with QNames of unbound external variables
     * @return array with QNames of unbound external variables or null.
     */
    @Override
    public QName[] getAllUnboundExternalVariables() {
        try {
            return _value.getAllUnboundExternalVariables();
        } catch (XQException ex) {
            throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
        }
    }

    /** Bind variable to XQuery expression.
     * @param qname QName of variable.
     * @param val object to be bound.
     * @throws SRuntimeException if an error occurs.
     */
    @Override
    public void bindValue(final QName qname, final Object val) throws SRuntimeException {
        QName[] qnames = getAllExternalVariables();
        if (qnames == null || qnames.length == 0 || val == null) {
            return;
        }
        boolean found = false;
        for (QName qn : qnames) {
            if (qname.equals(qn)) {
                found = true;
                break;
            }
        }
        if (!found) {
            return;
        }
        try {
            if (val instanceof Boolean) {
                _value.bindBoolean(qname, ((Boolean)val), null);
            } else if (val instanceof Byte) {
                _value.bindByte(qname, ((Byte)val), null);
            } else if (val instanceof Short) {
                _value.bindShort(qname, ((Short)val), null);
            } else if (val instanceof Integer) {
                _value.bindInt(qname, ((Integer)val), null);
            } else if (val instanceof Long) {
                _value.bindLong(qname, ((Long)val), null);
            } else if (val instanceof Float) {
                _value.bindFloat(qname, ((Float) val), null);
            } else if (val instanceof Double) {
                _value.bindDouble(qname, ((Double) val), null);
            } else if (val instanceof Node) {
                _value.bindNode(qname, (Node) val, null);
            } else {
                if (val instanceof SDatetime) {
                    _value.bindAtomicValue(qname,
                        ((SDatetime) val).toISO8601(), _conn.createAtomicType(XQItemType.XQBASETYPE_DATETIME));
                } else if (val instanceof SDuration) {
                    _value.bindAtomicValue(qname,
                        val.toString(), _conn.createAtomicType(XQItemType.XQBASETYPE_DURATION));
                } else if (val instanceof byte[]) {
                    _value.bindAtomicValue(qname,
                        new String(org.xdef.sys.SUtils.encodeBase64((byte[]) val)),
                        _conn.createAtomicType(XQItemType.XQBASETYPE_BASE64BINARY));
                } else {
                    _value.bindString(qname, val.toString(), null);
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

    /** Execute precompiled XQuery expression and return result.
     * @param node node or null.
     * @return object with result of XQuery expression.
     * @throws SRuntimeException if an error occurs.
     */
    @Override
    public Object evaluate(final Node node) throws SRuntimeException {
        try {
            if (node == null) {
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM, _conn.createItemFromNode(node, _conn.createDocumentType()));
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM,
                    _conn.createItemFromNode(node,
                        _conn.createElementType(KXmlUtils.getQName(node), XQItemType.XQBASETYPE_ANYTYPE)));
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM, _conn.createItemFromNode(node, _conn.createTextType()));
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM,
                    _conn.createItemFromNode(node,_conn.createProcessingInstructionType(node.getNodeValue())));
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.COMMENT_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM, _conn.createItemFromNode(node, _conn.createCommentType()));
                return _value.executeQuery();
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                _value.bindItem(XQConstants.CONTEXT_ITEM,
                    _conn.createItemFromNode(node,
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

    /** Execute XQuery expression and return result. If result type is null then result types are checked.
     * @return object with result of XQuery expression.
     */
    @Override
    public Object evaluate() {
        try {
            return _value.executeQuery();
        } catch (XQException ex) {
            throw new SRuntimeException(XML.XML506, ex); //XQuery expression error&{0}{: }
        }
    }
}