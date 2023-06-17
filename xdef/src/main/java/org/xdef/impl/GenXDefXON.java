package org.xdef.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XDEF42_NS_URI;
import static org.xdef.XDConstants.XDEF_NS_PREFIX;
import org.xdef.XDEmailAddr;
import org.xdef.XDTelephone;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonUtils;

/** Generate X-definition from JSON/XON.
 * @author Vaclav Trojan
 */
public final class GenXDefXON {

	/** Generation of model of map. */
	private static void genMap(final StringBuilder sb,
		final Map data) {
		sb.append("{");
		boolean first = true;
		for (Object key: data.keySet()) {
			String name = (String) key;
			Object value = data.get(name);
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append('"').append(XonTools.jstringToSource(name)).append("\":");
			genModel(sb, value);
		}
		sb.append("}");
	}

	private static void genList(final StringBuilder sb, final List data) {
		sb.append("[");
		boolean first = true;
		for (Object o: data) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			genModel(sb,o);
		}
		sb.append("]");
	}

	private static void genItem(final StringBuilder sb, final Object x) {
		sb.append("\"");
		if (x == null) {
			sb.append("jnull()");
		} else if (x instanceof Boolean) {
			sb.append("jboolean()");
		} else if (x instanceof String) {
			sb.append("jstring()");
		} else if (x instanceof Number) {
			if (x instanceof Long) {
				sb.append("long()");
			} else if (x instanceof Double) {
				sb.append("double()");
			} else if (x instanceof Float) {
				sb.append("float()");
			} else if (x instanceof Integer) {
				sb.append("int()");
			} else if (x instanceof Short) {
				sb.append("short()");
			} else if (x instanceof Byte) {
				sb.append("byte()");
			} else if (x instanceof BigInteger) {
				sb.append("integer()");
			} else if (x instanceof BigDecimal) {
				sb.append("decimal()");
			} else {
				sb.append("jnumber()");
			}
		} else if (x instanceof Character) {
			sb.append("char()");
		} else if (x instanceof URI) {
			sb.append("uri()");
		} else if (x instanceof XDEmailAddr) {
			sb.append("emailAddr()");
		} else if (x instanceof SDatetime) {
			sb.append("dateTime()");
		} else if (x instanceof GPSPosition) {
			sb.append("gps()");
		} else if (x instanceof Price) {
			sb.append("price()");
		} else if (x instanceof Currency) {
			sb.append("currency()");
		} else if (x instanceof XDTelephone) {
			sb.append("telephone()");
		} else if (x instanceof SDuration) {
			sb.append("duration()");
		} else if (x instanceof InetAddress) {
			sb.append("ipAddr()");
		} else if (x instanceof byte[]) {// byte array
			sb.append("base64Binary");
		} else {
			sb.append("jvalue()");
		}
		sb.append(";\"");
	}

	/** Recursive generation of X-definition model from given element.
	 * @param parent node to which model is added.
	 * @param x model from which a model is generated.
	 */
	private static void genModel(final StringBuilder sb, final Object data) {
		if (data instanceof Map) {
			genMap(sb, (Map) data);
		} else if (data instanceof List) {
			genList(sb, (List) data);
		} else {
			genItem(sb, data);
		}
	}
	/** Generate X-definition from input data to given output stream writer.
	 * @param xon JSON/XON data.
	 * @param xdName name XDefinition or null.
	 * @return org.w3c.dom.Document object with X-definition.
	 */
	public static final Element genXdef(final Object xon,
		final String xdName) {
		Document doc = KXmlUtils.newDocument(XDEF42_NS_URI, "xd:def", null);
		Element xdef = doc.getDocumentElement();
		xdef.setAttribute("xmlns:" + XDEF_NS_PREFIX, XDEF42_NS_URI);
		if (xdName != null && !xdName.isEmpty()) {
			xdef.setAttributeNS(XDEF42_NS_URI, "xd:name", xdName);
		}
		Element xmodel = doc.createElementNS(XDEF42_NS_URI, "xd:xon");
		String modelName = "model";
		xdef.setAttributeNS(XDEF42_NS_URI, "xd:root", modelName);
		xmodel.setAttributeNS(XDEF42_NS_URI, "xd:name", modelName);
		StringBuilder sb = new StringBuilder();
		genModel(sb, xon);
		String s = sb.toString();
		s = XonUtils.toXonString(XonUtils.parseXON(s), true);
		xmodel.appendChild(xmodel.getOwnerDocument().createTextNode(s));
		xdef.appendChild(xmodel);
		return xdef;
	}
}
