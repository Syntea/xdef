package org.xdef.xml;

import org.xdef.impl.xml.KNodeList;
import org.xdef.msg.XML;
import org.xdef.sys.SRuntimeException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.sys.StringParser;
import java.lang.reflect.Constructor;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** XPath expression.
 * @author Vaclav Trojan
 */
public class KXpathExpr {

	private static final XPathFactory XPF;
	private static final boolean XP2;
	/** Source string with XPath expression. */
	private String _source;
	private XPathExpression _value;
	private XPath _xp;
	static {
		XPathFactory x;
		try {
			Class<?> cls = Class.forName("net.sf.saxon.xpath.XPathFactoryImpl");
			Constructor<?> c = cls.getConstructor();
			Object obj = c.newInstance();
			x = (XPathFactory) obj;
		} catch (Exception ex) {
			x = null;
		}
		XPF = (x == null) ? XPathFactory.newInstance() : x;
		XP2 = x != null;
	}

	/** Creates a new instance of KXpathExpr from other expression with
	 * compiled new expression. The NameSpace context, functions and variables
	 * are retrieved  from the argument.
	 * @param expr String with XPath expression.
	 * @param xp NameSpace context or <tt>null</tt>.
	 */
	public KXpathExpr(String expr, final XPath xp) {
		_source = expr.trim();
		if (chkSimpleExpr() >= 0) {
			return;
		}
		try {
			_value = _xp.compile(expr);
		} catch (XPathExpressionException ex) {
			throw new SRuntimeException(XML.XML505, ex); //XPath error&{0}{: }
		}
	}

	/** Creates a new instance of KXpathExpr without NameSpace context,
	 * variables and functions.
	 * @param expr String with XPath expression.
	 */
	public KXpathExpr(final String expr) {
		this(expr, (NamespaceContext) null);
	}

	/** check if this expression can be converted to w3c.dom operations.
	 * @return -1 .. not simple, 0 .. element name, 1 .. attr, 6 .. self::.
	 */
	private int chkSimpleExpr() {
		String s;
		if (_source == null || (s =_source.trim()).isEmpty()) {
			return -1;
		}
		int ndx = s.charAt(0) == '@' ? 1 : s.startsWith("self::") ? 6 : 0;
		s = s.substring(ndx);
		return s.indexOf("::") < 0
			&& StringParser.chkXMLName(s, (byte) 10) ? ndx : -1;
	}

	/** Creates a new instance of KXpathExpr
	 * @param expr String with XPath expression.
	 * @param nc NameSpace context or <tt>null</tt>.
	 */
	public KXpathExpr(final String expr, final NamespaceContext nc) {
		_source = expr;
		if (chkSimpleExpr() >= 0 && nc == null) return;
		try {//return compiled XPath as XPathExpressionImpl object
			_xp = XPF.newXPath();
			if (nc != null) _xp.setNamespaceContext(nc);
			_value = _xp.compile(expr);
		} catch (XPathExpressionException ex) {
			throw new SRuntimeException(XML.XML505, ex); //XPath error&{0}{: }
		}
	}

	/** Creates a new instance of KXpathExpr
	 * @param expr String with XPath expression.
	 * @param nc NameSpace context or <tt>null</tt>.
	 * @param fr Function resolver or <tt>null</tt>.
	 * @param vr Variable resolver or <tt>null</tt>.
	 */
	public KXpathExpr(final String expr,
		final NamespaceContext nc,
		final XPathFunctionResolver fr,
		final XPathVariableResolver vr) {
		_source = expr.trim();
		try {//return compiled XPath as XPathExpressionImpl object
			if (nc != null) {
				_xp = XPF.newXPath();
				_xp.setNamespaceContext(nc);
			}
			if (fr != null) {
				if (_xp == null) _xp = XPF.newXPath();
				_xp.setXPathFunctionResolver(fr);
			}
			if (vr != null) {
				if (_xp == null) _xp = XPF.newXPath();
				_xp.setXPathVariableResolver(vr);
			}
			if (chkSimpleExpr() >= 0)
				return;
			if (_xp == null) _xp = XPF.newXPath();
			_value = _xp.compile(expr);
		} catch (XPathExpressionException ex) {
			throw new SRuntimeException(XML.XML505, ex); //XPath error&{0}{: }
		}
	}

	/** Get variable resolver.
	 * @return variable resolver.
	 */
	public XPathVariableResolver getVariableResolver() {
		return _xp == null ? null : _xp.getXPathVariableResolver();
	}

	/** Get function resolver.
	 * @return function resolver.
	 */
	public XPathFunctionResolver getFunctionResolver() {
		return _xp == null ? null : _xp.getXPathFunctionResolver();
	}

	/** Get namespace context.
	 * @return namespace context.
	 */
	public NamespaceContext getNamespaceContext() {
		return _xp == null ? null : _xp.getNamespaceContext();
	}

	/** Execute XPath expression and return result.
	 * If result type is <tt>null</tt> then result types are checked in
	 * following sequence:
	 * 1) NODESET, 2) STRING, 3) NODE.
	 * @param node node or <tt>null</tt>.
	 * @param type QName with result type.
	 * @return object with result of XPath expression.
	 */
	public Object evaluate(Node node, final QName type) {
		if (_value == null) {
			Element el = node.getNodeType() == Node.DOCUMENT_NODE ?
				((Document) node).getDocumentElement()
				: node.getNodeType()==Node.ELEMENT_NODE ? (Element) node : null;
			if (el == null) {
				return null;
			}
			int i = chkSimpleExpr();
			if (i == 1) {
				//attributes
				int ndx =_source.indexOf(':');
				Node x;
				if (ndx<0 || _xp == null || _xp.getNamespaceContext() == null) {
					x = el.getAttributeNode(_source.substring(1));
				} else {
					String prefix = _source.substring(1, ndx);
					String localName = _source.substring(ndx+1);
					String uri =
						_xp.getNamespaceContext().getNamespaceURI(prefix);
					x = el.getAttributeNodeNS(uri, localName);
				}
				if (type == null) {
					return x == null ? null : new KNodeList(x);
				}
				if (type.equals(XPathConstants.STRING)) {
					return x == null ? null : x.getNodeValue();
				}
				if (type.equals(XPathConstants.NODE))
					return x;
				if (type.equals(XPathConstants.NODESET))
					return new KNodeList(x);
				if (type.equals(XPathConstants.NUMBER))
					return x==null ? 0 : 1;
				if (type.equals(XPathConstants.BOOLEAN))
					return x!=null;
				return x!=null;
			}
			//elements
			NodeList nl;
			if (_xp == null) {
				if (i == 6) { // self
					return el.getNodeName().equals(_source.substring(6)) ?
						new KNodeList(el) : new KNodeList();
				}
				nl = KXmlUtils.getChildElements(node, _source);
			} else { //element or attribute
				String name = i < 0 ? _source : _source.substring(i);
				int ndx = name.indexOf(':');
				String prefix = ndx > 0 ? name.substring(0, ndx): "";
				String localName = ndx > 0 ? name.substring(ndx+1) : name;
				String uri = _xp == null ? null :
					_xp.getNamespaceContext() == null ? null
					:_xp.getNamespaceContext().getNamespaceURI(prefix);
				if (i == 6) { // self
					String lName = el.getLocalName();
					if (lName == null) {
						lName = el.getNodeName();
					}
					if (lName.equals(localName)) {
						if (uri == null) {
							return el.getNamespaceURI() == null ?
								new KNodeList(el) : new KNodeList();
						}
						return uri.equals(el.getNamespaceURI()) ?
							new KNodeList(el) : new KNodeList();
					}
					return new KNodeList();
				}
				if (uri == null) {
					nl = KXmlUtils.getChildElements(node, _source);
				} else {
					nl = KXmlUtils.getChildElementsNS(node, uri, localName);
				}
			}
			if (type == null || type.equals(XPathConstants.NODESET)) {
				return nl;
			}
			if (type.equals(XPathConstants.NUMBER)) return nl.getLength();
			if (type.equals(XPathConstants.BOOLEAN)) return nl.getLength() > 0;
			return nl.getLength() > 0 ? nl.item(0) : null;
		}
		if (type == null || type.equals(XPathConstants.NODESET)) {
			try {
				return (NodeList) _value.evaluate(node, XPathConstants.NODESET);
			} catch (Exception ex) {
				// !!!!!!!!!!!!!!!!!! This is very nasty code !!!!!!!!!!!!!!!!!!
				if (type == null || type.equals(XPathConstants.NODESET)
					&& ex instanceof XPathExpressionException) {
					String s = ex.getMessage();
					Throwable x = ex;
					while (s == null && (x = x.getCause()) != null) {
						s = x.getMessage();
					}
					if (s != null) {
						try {
							if (s.toUpperCase().contains("BOOLEAN"))
								return _value.evaluate(node,
									XPathConstants.BOOLEAN);
							if (s.toUpperCase().contains("NUMBER"))
								return _value.evaluate(node,
									XPathConstants.NUMBER);
						} catch (Exception exx) {}
					}
				}
				try {
					return _value.evaluate(node, XPathConstants.STRING);
				} catch (Exception ex1) {
					try {
						return _value.evaluate(node, XPathConstants.NODE);
					} catch (Exception ex2) {
						if (node == null) {
							return null;
						}
						//XPath error&{0}{: }
						throw new SRuntimeException(XML.XML505,
							ex.toString() + ",\n" + ex1.toString()
							+ ",\n" + ex2.toString());
					}
				}
			}
		} else {
			try {
				return _value.evaluate(node, type);
			} catch (Exception ex) {
				if (node == null) {
					return null;
				}
				//XPath error&{0}{: }
				throw new SRuntimeException(XML.XML505,	ex.toString());
			}
		}
	}

	/** Execute XPath expression and return result.
	/* If result type is <tt>null</tt> then result types are checked in
	 * following sequence:
	 * 1) NODESET, 2) STRING, 3) NODE.
	 * @param node node or <tt>null</tt>.
	 * @param type QName with result type or <tt>null</tt>.
	 * @param expr String with XPath expression.
	 * @param nc NameSpace context.
	 * @param fr Function resolver or <tt>null</tt>.
	 * @param vr Variable resolver or <tt>null</tt>.
	 * @return object with result of XPath expression.
	 */
	public static Object evaluate(final Node node,
		final QName type,
		final String expr,
		final NamespaceContext nc,
		final XPathFunctionResolver fr,
		final XPathVariableResolver vr) {
		return new KXpathExpr(expr, nc, fr, vr).evaluate(node, type);
	}

	/** Execute XPath expression (no NameSpace context, no variables,
	 * no functions) and return result.
	 * Result types are checked in following sequence:
	 * 1) NODESET, 2) STRING, 3) NODE.
	 * @param node node or <tt>null</tt>.
	 * @param expr String with XPath expression.
	 * @return object with result of XPath expression.
	 */
	public static Object evaluate(final Node node, final String expr) {
		return new KXpathExpr(expr).evaluate(node, (QName) null);
	}

	/** Execute XPath expression (no NameSpace context, no variables,
	 * no functions) and return result.
	 * Result types are checked in following sequence:
	 * 1) NODESET, 2) STRING, 3) NODE.
	 * @param node node or <tt>null</tt>.
	 * @param expr String with XPath expression.
	 * @param nc NameSpace context.
	 * @return object with result of XPath expression.
	 */
	public static Object evaluate(final Node node,
		final String expr,
		final NamespaceContext nc) {
		return new KXpathExpr(expr, nc).evaluate(node, (QName) null);
	}

	@Override
	/** Get string with XPath source,
	 * @return string with XPath source.
	 */
	public String toString() {return _source;}

	/** Check if XPath2 implementation is available.
	 * @return true if XPath2 implementation is available.
	 */
	public static final boolean isXPath2() {return XP2;}

}