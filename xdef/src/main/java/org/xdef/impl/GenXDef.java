package org.xdef.impl;

import org.xdef.XDConstants;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.impl.parsers.XDParseEmail;
import org.xdef.impl.parsers.XDParseEmailDate;
import org.xdef.impl.parsers.XDParseMD5;
import org.xdef.impl.parsers.XDParsePrintableDate;
import org.xdef.impl.parsers.XDParseSHA1;
import org.xdef.impl.parsers.XSParseBase64Binary;
import org.xdef.impl.parsers.XSParseBoolean;
import org.xdef.impl.parsers.XSParseDate;
import org.xdef.impl.parsers.XSParseDatetime;
import org.xdef.impl.parsers.XSParseDecimal;
import org.xdef.impl.parsers.XSParseDuration;
import org.xdef.impl.parsers.XSParseGDay;
import org.xdef.impl.parsers.XSParseGMonth;
import org.xdef.impl.parsers.XSParseGMonthDay;
import org.xdef.impl.parsers.XSParseGYear;
import org.xdef.impl.parsers.XSParseGYearMonth;
import org.xdef.impl.parsers.XSParseHexBinary;
import org.xdef.impl.parsers.XSParseInt;
import org.xdef.impl.parsers.XSParseInteger;
import org.xdef.impl.parsers.XSParseLong;
import org.xdef.impl.parsers.XSParseTime;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;

/** Generate X-definition from XML.
 * @author Vaclav Trojan
 */
public class GenXDef implements XDConstants {
	/** Prevent user to create an instance of this class.*/
	private GenXDef() {}

	/** Model of attribute. */
	private final static class XAttr {
		String _type;
		boolean _required;
		XAttr(String type) {_type = type; _required = true;}
		@Override
		public String toString() {
			return (_required ? "" : "optional ") + _type;
		}
	}

	/** Model of element or text node. */
	private final static class XModel {
		final private QName _qname;
		private final Map<QName,XAttr> _atts = new LinkedHashMap<QName,XAttr>();
		private final List<XModel> _models = new ArrayList<XModel>();
		private final Set<String> _options = new HashSet<String>();
		private String _value;
		private int _min = 1;
		private int _max = 1;
		private boolean _mixed;

		XModel(final QName qname) {_qname = qname;}

		XModel(final String name, final String uri) {this(getQName(name,uri));}

		private XModel cloneModel() {
			final XModel result = new XModel(_qname);
			result._value = _value;
			result._min =  _min;
			result._max =  _max;
			result._options.addAll(_options);
			result._mixed =  _mixed;
			for (QName name : _atts.keySet()) {
				final XAttr attx = _atts.get(name);
				final XAttr atty = new XAttr(attx._type);
				atty._required = attx._required;
				result._atts.put(name, atty);
			}
			for (XModel x : _models) {
				result._models.add(x.cloneModel());
			}
			return result;
		}

		XModel(final String text) {
			this(new QName("#text"));
			_value = genType(this, text);
		}

		XModel(final Element elem) {
			this(getQName(elem));
			_value = null;
			NamedNodeMap nnm = elem.getAttributes();
			for (int i = 0; nnm != null && i < nnm.getLength(); i++) {
				final Node att = nnm.item(i);
				QName qname = getQName(att);
				if (att.getNodeName().startsWith("xmlns")) {
					_atts.put(qname, new XAttr(att.getNodeValue().trim()));
				} else {
					_atts.put(qname,
						new XAttr(genType(this,att.getNodeValue().trim())));
				}
			}
			final NodeList nl = elem.getChildNodes();
			for (int i = 0; nl != null && i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					_models.add(new XModel((Element) n));
				} else if (n.getNodeType() == Node.TEXT_NODE ||
					n.getNodeType() == Node.CDATA_SECTION_NODE) {
					String s = n.getNodeValue();
					while (i + 1 <  nl.getLength() &&
						(n = nl.item(i+1)).getNodeType() != Node.ELEMENT_NODE) {
						if (n.getNodeType() == Node.TEXT_NODE
							|| n.getNodeType() == Node.CDATA_SECTION_NODE) {
							s += n.getNodeValue();
						}
						i++;
					}
					if ((s = s.trim()).length() > 0) {
						_models.add(new XModel(s));
					}
				}
			}
		}

		private void optimize() {
			if (_models.size() > 0) {
				for (int i = _models.size() - 1; i >= 0; i--) {
					_models.get(i).optimize();
				}
				if (_models.size() > 1) {
					for (int i = _models.size() - 1; i > 0; i--) {
						XModel x = _models.get(i - 1);
						XModel y = _models.get(i);
						XModel z = y.compareModel(x);
						int min = 0;
						if (z != null) {
							int max = ++z._max;
							while (z != null && i > 0) {
								_models.remove(i);
								_models.set(--i, z);
								if (i > 0) {
									x = _models.get(i - 1);
									y = z;
									y._max = ++max;
									y._min = (y._min > min) ? min : y._min;
									z = y.compareModel(x);
								}
							}
							i = 0;
						}
						y._options.addAll(x._options);
					}
					final Set<QName> names = new HashSet<QName>();
					for (int i = 0; i < _models.size(); i++) {
						final QName name;
						if (!names.add(name = _models.get(i)._qname)) {
							_mixed = true;
							for (int j = 0; j < i; j++) {
								if(_models.get(j)._qname.equals(name)) {
									XModel z = _models.get(j)
										.compareModel(_models.get(i));
									z._options.addAll(_models.get(i)._options);
									if (z != null) {
										z._max++;
										_models.set(j, z);
										_models.remove(i);
										i = 0;
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		private static String mergeTypes(final String x, final String y) {
			if (x.equals(y)) {
				return x;
			}
			if (x.startsWith("string(")
				&& y.startsWith("string(")) {
				return !"string()".equals(x) ? x : y;
			} else if (x.equals("dateYMDhms()") && y.equals("long()")) {
				return "long()";
			} else if (x.equals("int()")
				&& (y.equals("long()") || y.equals("dateYMDhms()"))) {
				return "long()";
			} else if (x.equals("int()") && y.equals("integer()")) {
				return "integer()";
			} else if (x.equals("long()") && (y.equals("int()")
				|| y.equals("dateYMDhms()"))) {
				return "long()";
			} else if (x.equals("long()") && (y.equals("int()")
				|| y.equals("dateYMDhms()"))) {
				return "long()";
			} else if (x.equals("long()") && y.equals("integer()")) {
				return "integer()";
			} else if (x.equals("integer()") && (y.equals("int()")
				|| y.equals("long()") || y.equals("dateYMDhms()"))) {
				return "integer()";
			} else if (x.equals("MD5()")
				&& (y.equals("SHA1()") || y.equals("hexBinary()"))) {
				return "hexBinary()";
			} else if (x.equals("SHA1()")
				&& (y.equals("MD5()") || y.equals("hexBinary()"))) {
				return "hexBinary()";
			} else if (x.equals("hexBinary()")
				&& (y.equals("MD5()") || y.equals("SHA1()"))) {
				return "hexBinary()";
			}
			return "string()";
		}

		private void mergeAttrs(final XModel x) {
			for (QName name: _atts.keySet()){
				final XAttr yattr = _atts.get(name);
				final XAttr xattr = x._atts.get(name);
				if (xattr != null) { //has attribute
					yattr._type = mergeTypes(xattr._type, yattr._type);
					if (!xattr._required) {
						yattr._required = false;
					}
				} else {
					yattr._required = false;
				}
			}
			for (QName name: x._atts.keySet()){
				XAttr yattr = _atts.get(name);
				if (yattr == null) {
					final XAttr xattr = x._atts.get(name);
					yattr = new XAttr(xattr._type);
					yattr._required = false;
					_atts.put(name, yattr);
				}
			}
		}

		private XModel compareModel(final XModel x) {
			if (!_qname.equals(x._qname)) {
				return null; // names not equal
			}
			// names are equal
			final XModel y = this.cloneModel();
			y._options.addAll(x._options);
			x._options.addAll(y._options);
			_options.addAll(x._options);
			y.mergeAttrs(x);
			if (y._max < x._max) {
				y._max = x._max;
			}
			if (y._min > x._min) {
				y._min = x._min;
			}
			if (y._models.isEmpty() && x._models.isEmpty()) {
				return y;
			}
			if (y._models.size() > 0 && x._models.isEmpty()) {
				for (XModel m : y._models) {
					m._min = 0;
				}
				return y;
			} else if (y._models.isEmpty()) {
				for (XModel xm: x._models) {
					xm = xm.cloneModel();
					xm._min = 0;
					y._models.add(xm);
				}
				return y;
			} else {
				int j = 0, m, n = 0;
				do {
					m = j;
					for (int i = 0; i < y._models.size(); i++) {
						final XModel ym = y._models.get(i);
						for (int k = j; k < x._models.size(); k++) {
							XModel xm = x._models.get(k);
							XModel model = ym.compareModel(xm);
							if (model != null) {
								j++;
								ym.mergeAttrs(model);
								ym._max = model._max;
								ym._min = model._min;
								ym._models.clear();
								ym._models.addAll(model._models);
								ym.optimize();
								if (xm._value != null && ym._value != null
									&& !xm._value.equals(ym._value)) {
									model._value = "string";
								}
								ym._mixed |= xm._mixed;
								ym._value = model._value;
								break;
							} else {
								xm = xm.cloneModel();
								xm._min = 0;
								y._models.add(j, xm);
								i = y._models.size();
								break;
							}
						}
					}
					if (n == 0) {
						n = 1;
						for (; j < x._models.size(); j++) {
							final XModel xm = x._models.get(j).cloneModel();
							xm._min = 0;
							y._mixed = true;
							y._models.add(xm);
						}
					}
				} while (j != m);
			}
			y.optimize();
			return y;
		}

		@Override
		public String toString() {
			return _qname + " " + _min + ".." + _max
				+ "; size=" + _models.size();
		}
	}

	private static QName checkQName(final QName qname) {
		String uri = qname.getNamespaceURI();
		if (XDEF40_NS_URI.equals(uri) || XDEF32_NS_URI.equals(uri)
			|| XDEF31_NS_URI.equals(uri) || XDEF20_NS_URI.equals(uri)) {
			//Namespace of X-definition is not allowed in XML input data
			throw new SRuntimeException(XDEF.XDEF882);
		}
		if (XDEF_NS_PREFIX.equals(qname.getPrefix())) {
			//Prefix "xd" is not allowed in XML input data
			throw new SRuntimeException(XDEF.XDEF881);
		}
		return qname;
	}

	private static QName getQName(final String name, final String uri) {
		if (uri == null || uri.isEmpty()) {
			return checkQName(new QName(name));
		} else {
			int ndx = name.indexOf(':');
			String prefix = ndx > 0 ? name.substring(0, ndx) : "";
			String localName = ndx > 0 ? name.substring(ndx + 1) : name;
			return checkQName(new QName(uri, localName, prefix));
		}
	}

	private static QName getQName(Node node) {
		QName qn = new QName(node.getNamespaceURI(), node.getLocalName(),
			node.getPrefix() == null ? "" : node.getPrefix());
		return checkQName(qn);
	}

	private static String getNameFromQName(final QName name) {
		String uri = name.getNamespaceURI();
		String prefix = name.getPrefix();
		if (uri==null || uri.isEmpty() || prefix==null || prefix.isEmpty()) {
			return name.getLocalPart();
		}
		return prefix + ':' + name.getLocalPart();
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param elem XML element.
	 * @return org.w3c.dom.Document object with X-definition.
	 */
	public static final Element genXdef(final Element elem) {
		Element el = (Element) elem.cloneNode(true);
		el.normalize();
		KXmlUtils.trimTextNodes(el, true);
		canonizeXML(el);
		Document doc = el.getOwnerDocument();
		doc = doc.getImplementation().createDocument(
			XDEF40_NS_URI, XDEF_NS_PREFIX + ":def", doc.getDoctype());
		Element xdef = doc.getDocumentElement();
		xdef.setAttribute("xmlns:" + XDEF_NS_PREFIX, XDEF40_NS_URI);
		final String s = el.getNodeName();
		if (el.getNamespaceURI() != null) {
			int i = s.indexOf(':');
			String t = i > 0 ? "xmlns:" + s.substring(0, i) : "xmlns";
			xdef.setAttribute(t, el.getNamespaceURI());
		}
		xdef.setAttribute("root", s);
		XModel x = new XModel(el);
		x.optimize();
		genModel(xdef, x);
		return xdef;
	}

	/** Remove all comments, processing instructions, entity references and
	 * concatenate adjacent text nodes.
	 * @param elem element to be canonized.
	 */
	private static void canonizeXML(final Node elem) {
		final NodeList nl = elem.getChildNodes();
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			Node n = nl.item(i);
			switch (n.getNodeType()) {
				case Node.ELEMENT_NODE:
					canonizeXML(n);
					continue;
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE: {
					String s = n.getNodeValue();
					while(i > 0) {
						Node x = nl.item(i-1);
						switch (x.getNodeType()) {
							case Node.ELEMENT_NODE:
								break;
							case Node.TEXT_NODE:
							case Node.CDATA_SECTION_NODE:
								s = x.getNodeValue() + s;
							default:
								elem.removeChild(x);
								continue;
						}
						break;
					}
					if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
						Node x = n.getOwnerDocument().createTextNode(s);
						elem.replaceChild(x, n);
					} else {
						n.setNodeValue(s);
					}
				}
			}
		}
	}

	private static String genType(final XModel model, final String data) {
		if (data.isEmpty()) {
			model._options.add("acceptEmptyAttributes");
			return "string(0,*)";
		}
		if (new XSParseInt().check(null, data).matches()) {
			return data.trim().length() > 1 && data.trim().charAt(0) == '0'
				? "num()" : "int()";
		}
		if (new XSParseLong().check(null, data).matches()) {
			return data.trim().length() > 1 && data.trim().charAt(0) == '0'
				? "num()" : "long()";
		}
		if (new XSParseInteger().check(null, data).matches()) {
			return data.trim().length() > 1 && data.trim().charAt(0) == '0'
				? "num()" : "integer()";
		}
		if (new XSParseDecimal().check(null, data).matches()) {
			return "decimal()";
		}
		if (new XSParseBoolean().check(null, data).matches()) {
			return "boolean()";
		}
		if (new XSParseDatetime().check(null, data).matches()) {
			return "dateTime()";
		}
		if (new XSParseDate().check(null, data).matches()) {
			return "date()";
		}
		if (new XSParseTime().check(null, data).matches()) {
			return "time()";
		}
		if (new XSParseDuration().check(null, data).matches()) {
			return "duration()";
		}
		if (new XSParseGDay().check(null, data).matches()) {
			return "gDay()";
		}
		if (new XSParseGMonth().check(null, data).matches()) {
			return "gMonth()";
		}
		if (new XSParseGMonthDay().check(null, data).matches()) {
			return "gMonthDay()";
		}
		if (new XSParseGYearMonth().check(null, data).matches()) {
			return "gYearMonth()";
		}
		if (new XSParseGYear().check(null, data).matches()) {
			return "gYear()";
		}
		if (data.length() == 32
			&& new XDParseMD5().check(null, data).matches()) {
			return "MD5()";
		}
		if (data.length() == 40
			&& new XDParseSHA1().check(null, data).matches()) {
			return "SHA1()";
		}
		if (data.length() > 16
			&& new XSParseHexBinary().check(null, data).matches()) {
			return "hexBinary()";
		}
		if (data.length() > 16
			&& new XSParseBase64Binary().check(null, data).matches()) {
			return "base64Binary()";
		}
		if (new XDParseEmailDate().check(null, data).matches()) {
			return "emailDate()";
		}
		if (new XDParseEmail().check(null, data).matches()) {
			return "email()";
		}
		if (new XDParsePrintableDate().check(null, data).matches()) {
			return "printableDate()";
		}
		final StringParser p = new StringParser(data);
		p.setBufIndex(0);
		String mask;
		if (p.isDatetime("d-M-yyyy")) {
			mask = "d-M-yyyy";
		} else if (p.isDatetime("d.M.yyyy")) {
			mask = "d.M.yyyy";
		} else if (p.isDatetime("d/M/yyyy")) {
			mask = "d/M/yyyy";
		} else {
			mask = "";
			p.setBufIndex(0);
		}
		if (!mask.isEmpty()) {
			if (p.eos()) {
				return "xdatetime('" + mask + "')";
			}
			if (p.isChar('T')) {
				mask += 'T';
			} else if (p.isChar(' ')) {
				mask += ' ';
			} else {
				return "string()";
			}
		}
		if (p.isDatetime("H:m")) {
			mask += "H:m";
			if (p.eos()) {
				return "xdatetime('" + mask + "')";
			}
			if (p.isDatetime(":s")) {
				mask += ":s";
				if (p.eos()) {
					return "xdatetime('" + mask + "')";
				}
				if (p.isDatetime(".S")) {
					mask += ".S";
					if (p.eos()) {
						return "xdatetime('" + mask + "')";
					}
				}
				if (p.isDatetime(" Z") && p.eos()) {
					return "xdatetime('" + mask + " Z')";
				}
			}
		}
		return "string()";
	}

	private static void appendText(final Element el, final String text) {
		el.appendChild(el.getOwnerDocument().createTextNode(text));
	}

	private static Element createElement(final Element el,
		final String ns,
		final String name) {
		return ns == null ? el.getOwnerDocument().createElement(name)
			: el.getOwnerDocument().createElementNS(ns, name);
	}

	/** Recursive generation of X-definition model from given element.
	 * Result is added to parent (i.e. a node from X-definition).
	 * @param parent node to which model is added.
	 * @param x model from which a model is generated.
	 */
	private static void genModel(final Element parent, final XModel x) {
		if (new QName("#text").equals(x._qname)) {
			String val = (x._min == 0 ? "optional " : "required ")
				+ x._value + ";";
			appendText(parent, val);
			return;
		}
		String qname = getNameFromQName(x._qname);
		Element model = createElement(parent, x._qname.getNamespaceURI(),qname);
		String s = "";
		if (x._max > 1) {
			s = "occurs " + (x._min == 0 ? "*;" : "+;");
		} else {
			if (x._min == 0) {
				s = "occurs ?;";
			}
		}
		if (!x._options.isEmpty()) {
			String t = null;
			for (String y : x._options) {
				if (t == null) {
					t = "options " + y;
				} else {
					t += "," + y;
				}
			}
			s += t + ';';
		}
		if (s.isEmpty() && !"xd:def".equals(parent.getNodeName())) {
			s = "occurs 1;";
		}
		if (!s.isEmpty()) {
			model.setAttributeNS(XDEF40_NS_URI, XDEF_NS_PREFIX + ":script", s);
		}
		XAttr att = x._atts.get(new QName(XDEF40_NS_URI, "script"));
		if (att != null) { // to be the first attribute
			model.setAttributeNS(XDEF40_NS_URI,
				XDEF_NS_PREFIX + ":script", att._type);
			x._atts.remove(att);
		}
		for (QName name: x._atts.keySet()) {
			att = x._atts.get(name);
			String qn = getNameFromQName(name);
			String uri = name.getNamespaceURI();
			if (qn.startsWith("xmlns")) {
				model.setAttributeNS(uri, qn, att._type);
			} else {
				final String value =
					(att._required ? "required" : "optional") +
					(att._type.length() != 0 ? " " + att._type : "") + ";";
				if (uri == null || uri.isEmpty()) {
					model.setAttribute(qn, value);
				} else {
					model.setAttributeNS(uri, qn, value);
				}
			}
		}
		parent.appendChild(model);
		if (x._mixed) {
			final Element el = createElement(model,
				XDEF40_NS_URI, XDEF_NS_PREFIX + ":mixed");
			model.appendChild(el);
			model = el;
		}
		for (XModel y: x._models) {
			genModel(model, y);
		}
	}
}