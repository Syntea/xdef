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
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xdef.impl.parsers.XDParseDateYMDhms;
import org.xdef.impl.parsers.XDParseEmailDate;
import org.xdef.impl.parsers.XDParseMD5;
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
import org.xdef.impl.parsers.XSParseInt;
import org.xdef.impl.parsers.XSParseLong;
import org.xdef.impl.parsers.XSParseTime;

/** Generate X-definition from XML.
 * @author Vaclav Trojan
 */
public class GenXDef implements XDConstants {

	/** Prevent create an instance of this class.*/
	private GenXDef() {}

	private static class XAtt {
		String _type;
		boolean _required;
		XAtt(String type) {_type = type; _required = true;}
		@Override
		public String toString() {
			return (_required ? "required " : "optional ") + _type;
		}
	}

	private static class XModel {final private String _name;
		final private String _nsuri;
		private final Map<String,XAtt> _atts = new LinkedHashMap<String,XAtt>();
		private final List<XModel> _models = new ArrayList<XModel>();
		private String _value;
		private int _min = 1;
		private int _max = 1;
		private boolean _emptyAtts;
		private boolean _mixed;

		XModel(final String name, final String nsuri) {_name=name;_nsuri=nsuri;}

		private XModel cloneModel() {
			final XModel result = new XModel(_name, _nsuri);
			result._value = _value;
			result._min =  _min;
			result._max =  _max;
			result._emptyAtts =  _emptyAtts;
			result._mixed =  _mixed;
			for (String name : _atts.keySet()) {
				final XAtt attx = _atts.get(name);
				final XAtt atty = new XAtt(attx._type);
				atty._required = attx._required;
				result._atts.put(name, atty);
			}
			for (XModel x : _models) {
				result._models.add(x.cloneModel());
			}
			return result;
		}

		XModel(final String text) {
			_name = "$text";
			_nsuri = null;
			_value = genType(text);
		}

		XModel(final Element elem) {
			_name = elem.getNodeName();
			_nsuri = elem.getNamespaceURI();
			_value = null;
			NamedNodeMap nnm = elem.getAttributes();
			for (int i = 0; nnm != null && i < nnm.getLength(); i++) {
				final Node att = nnm.item(i);
				String s = att.getNodeName();
				if (s.startsWith("xmlns")) {
					_atts.put(s, new XAtt(att.getNodeValue()));
				} else {
					String t = att.getNamespaceURI();
					s = (t != null ? "{"+ t + "}" : "") + s;
					_atts.put(s, new XAtt(genType(att.getNodeValue().trim())));
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
						if (z == null) {
							y._min = 0;
						} else {
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
					}
					final Set<String> names = new HashSet<String>();
					for (int i = 0; i < _models.size(); i++) {
						final String name;
						if (!names.add(name = _models.get(i)._name)) {
							_mixed = true;
							for (int j = 0; j < i; j++) {
								if(_models.get(j)._name.equals(name)) {
									XModel z = _models.get(j)
										.compareModel(_models.get(i));
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

		private void mergeAttrs(final XModel x) {
			for (String name: _atts.keySet()){
				final XAtt yattr = _atts.get(name);
				final XAtt xattr = x._atts.get(name);
				if (xattr != null) { //has attribute
					if (!yattr._type.equals(xattr._type)) {
						yattr._type = "string()";
					}
					if (!xattr._required) {
						yattr._required = false;
					}
				} else {
					yattr._required = false;
				}
			}
			for (String name: x._atts.keySet()){
				XAtt yattr = _atts.get(name);
				if (yattr == null) {
					final XAtt xattr = x._atts.get(name);
					yattr = new XAtt(xattr._type);
					yattr._required = false;
					_atts.put(name, yattr);
				}
			}
		}

		private XModel compareModel(final XModel x) {
			if (!_name.equals(x._name) || _nsuri!=null
				&& !_nsuri.equals(x._nsuri)	|| _nsuri==null && _nsuri!=null) {
				return null; // names not equal
			}
			// names are equal
			final XModel y = this.cloneModel();
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
			return (_nsuri != null ? '{' + _nsuri + '}' : "")
				+ _name + " " + _min + ".." + _max
				+ "; size=" + _models.size();
		}
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param source XML element.
	 * @return String with X-definition.
	 * @throws Exception if an error occurs.
	 */
	public static final Element genXdef(final String source) throws Exception {
		return genXdef(KXmlUtils.parseXml(source).getDocumentElement());
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param elem XML element.
	 * @return org.w3c.dom.Document object with X-definition.
	 */
	public static final Element genXdef(final Element elem) {
		Element newElem = (Element) elem.cloneNode(true);
		newElem.normalize();
		KXmlUtils.trimTextNodes(newElem, true);
		return genXDefinition(newElem);
	}

	/** Creates a new instance of GenX-definition.
	 * @param data Object from which we'll create a X-definition.
	 * @return element with X-definition.
	 */
	public static final Element genXDefinition(final Element data) {
		Element elem = (Element) data.cloneNode(true);
		canonizeXML(elem);
		Element rootDef = elem.getOwnerDocument().getImplementation()
			.createDocument(XDEF32_NS_URI, XDEF_NS_PREFIX + ":def", null)
			.getDocumentElement();
		rootDef.setAttribute("xmlns:" + XDEF_NS_PREFIX, XDEF32_NS_URI);
		final String s = elem.getNodeName();
		rootDef.setAttribute("root", s);
		if (elem.getNamespaceURI() != null) {
			int i = s.indexOf(':');
			String t = i > 0 ? "xmlns:" + s.substring(0, i) : "xmlns";
			rootDef.setAttribute(t, elem.getNamespaceURI());
		}
		XModel x = new XModel(elem);
		x.optimize();
		genModel(rootDef, x);
		return rootDef;
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

	private static String genType(final String data) {
		if (data.length() == 0) {
			return ""; //TODO option Accept empty attributes?
		}
		if (new XDParseDateYMDhms().check(null, data).matches()) {
			return "dateYMDhms()";
		}
		if (new XSParseInt().check(null, data).matches()) {
			return "int()";
		}
		if (new XSParseLong().check(null, data).matches()) {
			return "long()";
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
		if ((data.trim().endsWith("=") || data.trim().length() >= 16)
			&& new XSParseBase64Binary().check(null, data).matches()) {
			return "base64Binary()";
		}
		if (data.trim().length() == 16
			&& new XDParseMD5().check(null, data).matches()) {
			return "MD5()";
		}
		if (new XDParseEmailDate().check(null, data).matches()) {
			return "emailDate()";
		}
		final StringParser p = new StringParser(data);
		p.setBufIndex(0);
		String mask;
		if (p.isDatetime("d.M.yyyy") || p.isDatetime("d/M/yyyy")) {
			mask = "d.M.yyyy";
		} else if (p.isDatetime("d.M.yy") || p.isDatetime("d/M/yy")) {
			mask = "d.M.yy";
		} else {
			mask = "";
			p.setBufIndex(0);
		}
		if (mask.length() > 0) {
			final String s = p.getParsedBufferPart();
			final int i = s.indexOf('.');
			if (i < 0) {
				mask = mask.replace('.', '/');
			}
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
		if ("$text".equals(x._name)) {
			String val = (x._min == 0 ? "optional " : "required ")
				+ x._value + ";";
			appendText(parent, val);
			return;
		}
		Element model = createElement(parent, x._nsuri, x._name);
		if (x._max > 1) {
			model.setAttributeNS(XDEF32_NS_URI, XDEF_NS_PREFIX + ":script",
				"occurs " + (x._min == 0 ? "*;" : "+;"));
		} else {
			if (x._min == 0) {
				model.setAttributeNS(XDEF32_NS_URI, XDEF_NS_PREFIX + ":script",
					"occurs ?;");
			}
		}
		for (String name: x._atts.keySet()){
			final XAtt att = x._atts.get(name);
			if (name.startsWith("xmlns")) {
				model.setAttribute(name, att._type);
			} else {
				final String value =
					(att._required ? "required" : "optional") +
					(att._type.length() != 0 ? " " + att._type : "") + ";";
				final int i = name.indexOf('}');
				if (i < 0) {
					model.setAttribute(name, value);
				} else {
					String uri = name.substring(1, i);
					model.setAttributeNS(uri, name.substring(i + 1), value);
				}
			}
		}
		parent.appendChild(model);
		if (x._mixed) {
			final Element el =
				createElement(model, XDEF32_NS_URI, XDEF_NS_PREFIX + ":mixed");
			model.appendChild(el);
			model = el;
		}
		for (XModel y: x._models) {
			genModel(model, y);
		}
	}

}