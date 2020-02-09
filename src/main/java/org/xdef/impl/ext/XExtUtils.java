package org.xdef.impl.ext;

import org.xdef.XDConstants;
import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SError;
import org.xdef.sys.StringParser;
import org.xdef.XDValue;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.impl.XConstants;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefXPathExpr;
import java.math.BigDecimal;
import java.math.MathContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDContainer;
import org.xdef.XDValueID;
import org.xdef.impl.code.DefDate;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SUtils;

/** External utilities for key definition and key reference.
 * @author Vaclav Trojan
 */
public final class XExtUtils {

	/** Prevent user to instantiate this class. */
	private XExtUtils() {}

	public final static String getVersionInfo() {
		return XDConstants.BUILD_VERSION + " " +
			XDConstants.BUILD_DATE + " (" + XDConstants.JAVA_VERSION + ")";
	}


	/**	Get name space URI of qualified name.
	 * @param qname qualified name
	 * @param elem element where name space URI is searched.
	 * @return name space URI or an empty string.
	 */
	public final static String getQnameNSUri(final String qname,
		final Element elem) {
		byte xmlVersion = "1.1".equals(elem.getOwnerDocument().getXmlVersion())
			? XConstants.XML11 : XConstants.XML10;
		if (!StringParser.chkXMLName(qname, xmlVersion)) {
			return "";
		}
		String prefix;
		int ndx;
		prefix = (ndx = qname.indexOf(':')) > 0 ? qname.substring(0, ndx) : "";
		return getNSUri(prefix, elem);
	}

	/** Get name space URI of given prefix from the context of an element.
	 * @param pfx string with the prefix.
	 * @param elem the element.
	 * @return name space URI.
	 */
	public final static String getNSUri(final String pfx, final Element elem) {
		Element el;
		if ((el = elem) == null) {
			return "";
		}
		String nsAttr = pfx.length() == 0 ? "xmlns" : "xmlns:" + pfx;
		for (;;) {
			if (el.hasAttribute(nsAttr)) {
				return el.getAttribute(nsAttr);
			}
			Node n = el.getParentNode();
			if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
				return "";
			}
			el = (Element) n;
		}
	}
	public final static void addComment(final XXNode x, final String s) {
		addComment(x.getElement(), s);
	}
	public final static void addComment(final Element el, final String s) {
		el.appendChild(el.getOwnerDocument().createComment(s != null? s : ""));
	}
	public final static void addPI(final XXNode x,
		final String target,
		final String data) {
		addPI(x.getElement(), target, data);
	}
	public final static void addPI(final Element el,
		final String target,
		final String data) {
		el.appendChild(el.getOwnerDocument().createProcessingInstruction(
			target, data));
	}
	public final static void addText(final XXNode x, final String s) {
		addText(x.getElement(), s);
	}
	public final static void addText(final Element el, final String s) {
		if (s != null && !s.isEmpty()) {
			el.appendChild(el.getOwnerDocument().createTextNode(s));
		}
	}
	public final static String getTextContent(final XXNode x) {
		return getTextContent(x.getElement());
	}
	public final static String getTextContent(final Element el) {
		return el != null ? el.getTextContent() : null;
	}
	public final static String getXPos(final XXNode x) {return x.getXPos();}
	public final static String getXDPosition(final XXNode xnode) {
		return xnode.getXMNode().getXDPosition();
	}
	public static long getSourceLine(final XXNode xnode) {
		return xnode.getSPosition().getLineNumber();
	}
	public static long getSourceColumn(final XXNode xnode) {
		return xnode.getSPosition().getColumnNumber();
	}
	public static String getSysId(final XXNode xnode) {
		return xnode.getSPosition().getSysId();
	}
	public static String getSourcePosition(final XXNode xnode) {
		return "" + xnode.getSPosition();
	}
	public static String getEnv(final String name) {return System.getenv(name);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of script methods.
////////////////////////////////////////////////////////////////////////////////

	public final static void cancel() {
		throw new SError(Report.error(XDEF.XDEF906)); //X-definition canceled
	}

	public final static void cancel(final String msg) {
		 //X-definition canceled&{0}{; }
		throw new SError(Report.error(XDEF.XDEF906, msg));
	}

	public final static byte[] parseBase64(final String s) {
		try {
			return SUtils.decodeBase64(s);
		} catch (Exception ex) {
			return null;
		}
	}

	public final static byte[] parseHex(final String s) {
		try {
			return SUtils.decodeHex(s);
		} catch (Exception ex) {
			return null;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	public final static Element getParentContextElement(final XXElement xElem) {
		XDValue val = ((XXElement) xElem.getParent()).getXDContext();
		if (val == null || val.getItemId() != XDValueID.XD_ELEMENT) {
			return null;
		}
		return val.getElement();
	}

	public final static Element getParentContextElement(final XXElement xElem,
		final long level) {
		if (level == 0) {
			return getParentContextElement(xElem);
		}
		XXNode xel = xElem;
		for (int i = 0; i < level; i++) {
			xel = xel.getParent();
			if (xel == null ||
				xel.getItemId() != XDValueID.XX_ELEMENT) {
				xel = null;
				break;
			}
		}
		if (xel != null) {
			XDValue val = ((XXElement) xel).getXDContext();
			if (val != null && val.getItemId() == XDValueID.XD_ELEMENT){
				return val.getElement();
			}
		}
		return null;
	}

	public final static XDContainer fromParent(final XXElement elem,
		final String expr){
		XDValue val = elem.getParent().getXDContext();
		if (val == null || val.getItemId() != XDValueID.XD_ELEMENT) {
			return new DefContainer();
		}
		Element el = val.getElement();
		DefXPathExpr xe = new DefXPathExpr(expr,
			elem.getXXNamespaceContext(),
			elem.getXXFunctionResolver(),
			elem.getXXVariableResolver());
		return new DefContainer(xe.exec(el));
	}

	public final static XDContainer fromParentContext(final XXElement e,
		final String x) {
		return fromParent(e, x);
	}

	public final static XDContainer fromRoot(final XXNode xElem,
		final String expr) {
		XDValue val = xElem.getXDDocument().getXDContext();
		if (val == null || val.getItemId() != XDValueID.XD_ELEMENT) {
			return new DefContainer();
		}
		Element elem = val.getElement();
		DefXPathExpr xe = new DefXPathExpr(expr,
			xElem.getXXNamespaceContext(),
			xElem.getXXFunctionResolver(),
			xElem.getXXVariableResolver());
		return new DefContainer(xe.exec(elem));
	}

	public final static XDContainer fromRootContext(final XXNode xElem,
		final String expr) {
		return fromRoot(xElem, expr);
	}

	public final static XDContainer fromRoot(final XXElement xElem,
		final String expr,
		final Element elem) {
		DefXPathExpr xe = new DefXPathExpr(expr,
			xElem.getXXNamespaceContext(),
			xElem.getXXFunctionResolver(),
			xElem.getXXVariableResolver());
		return new DefContainer(xe.exec(elem));
	}

	public static XDContainer fromRootContext(final XXElement xElem,
		final String expr,
		final Element elem) {
		return fromRoot(xElem, expr, elem);
	}

////////////////////////////////////////////////////////////////////////////////
// datetime
////////////////////////////////////////////////////////////////////////////////
	public final static int getMaxYear(XXNode xnode) {
		return xnode.getXDPool().getMaxYear();
	}
	public final static void setMaxYear(XXNode xnode, int i) {
		xnode.getXDDocument().setMaxYear(i);
	}
	public final static int getMinYear(XXNode xnode) {
		return xnode.getXDPool().getMaxYear();
	}
	public final static void setMinYear(XXNode xnode, int i) {
		xnode.getXDDocument().setMinYear(i);
	}
	public final static XDContainer getSpecialDates(XXNode xnode) {
		SDatetime[] dates = xnode.getXDDocument().getSpecialDates();
		DefContainer c = new DefContainer();
		if (dates != null) {
			for (SDatetime d : dates) {
				c.addXDItem(new DefDate(d));
			}
		}
		return c;
	}
	public final static void setSpecialDates(XXNode xnode, XDContainer c) {
		SDatetime[] dates = new SDatetime[c.getXDItemsNumber()];
		for (int i = 0; i < dates.length; i++) {
			dates[i] = c.getXDItem(i).datetimeValue();
		}
		xnode.getXDDocument().setSpecialDates(dates);
	}

	public final static SDatetime parseEmailDate(final String x) {
		StringParser p = new StringParser((x == null) ? "" : x.trim());
		return p.isRFC822Datetime() && p.eos() &&
			p.testParsedDatetime()
			? p.getParsedSDatetime() : null;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of predefined X-Script Math methods (ensure conversion
// of arguments long -> double). Other math methods are available in Math.
////////////////////////////////////////////////////////////////////////////////
//	public static long abs(long a) {return Math.abs(a);}
	public final static double acos(long a) {return Math.acos(a);}
	public final static double asin(long a) {return Math.asin(a);}
	public final static double atan(long a) {return Math.atan(a);}
	public final static double atan2(long a, long b) {return Math.atan2(a,b);}
	public final static double atan2(long a, double b) {return Math.atan2(a,b);}
	public final static double atan2(double a, long b) {return Math.atan2(a,b);}
	public final static double cbrt(long a) {return Math.cbrt(a);}
	public final static double ceil(long a) {return Math.ceil(a);}
	public final static double cos(long a) {return Math.cos(a);}
	public final static double cosh(long a) {return Math.cosh(a);}
	public final static double exp(long a) {return Math.exp(a);}
	public final static double expm1(long a) {return Math.expm1(a);}
	public final static double floor(long a) {return Math.floor(a);}
	public final static double hypot(long a, long b) {return Math.hypot(a, b);}
	public final static double hypot(long a, double b) {return Math.hypot(a,b);}
	public final static double hypot(double a, long b) {return Math.hypot(a,b);}
	public final static double IEEEremainder(long a, long b) {
		return Math.IEEEremainder(a,b);
	}
	public final static double IEEEremainder(long a, double b) {
		return Math.IEEEremainder(a,b);
	}
	public final static double IEEEremainder(double a, long b) {
		return Math.IEEEremainder(a,b);
	}
	public final static double log(long a) {return Math.log(a);}
	public final static double log10(long a) {return Math.log10(a);}
	public final static double log1p(long a) {return Math.log1p(a);}
	public final static long max(long a, long b) {return Math.max(a, b);}
	public final static double max(double a, long b) {return Math.max(a, b);}
	public final static double max(long a, double b) {return Math.max(a, b);}
	public final static long min(long a, long b) {return Math.min(a, b);}
	public final static double min(double a, long b) {return Math.min(a, b);}
	public final static double min(long a, double b) {return Math.min(a, b);}
	public final static double pow(long a, long b) {return Math.pow(a, b);}
	public final static double pow(long a, double b) {return Math.pow(a, b);}
	public final static double pow(double a, long b) {return Math.pow(a, b);}
	public final static double rint(long a) {return Math.rint(a);}
	public final static long round(long a) {return Math.round(a);}
	public final static double signum(long a) {return Math.signum(a);}
	public final static double sin(long a) {return Math.sin(a);}
	public final static double sinh(long a) {return Math.sinh(a);}
	public final static double sqrt(long a) {return Math.sqrt(a);}
	public final static double tan(long a) {return Math.tan(a);}
	public final static double tanh(long a) {return Math.tanh(a);}
	public final static double toDegrees(long a) {return Math.toDegrees(a);}
	public final static double toRadians(long a) {return Math.toRadians(a);}
	public final static double ulp(long a) {return Math.ulp(a);}
	// Decimal constructors
	public final static BigDecimal decimalValue(final BigDecimal a) {
		return new BigDecimal(a.toString());
	}
	public final static BigDecimal decimalValue(final String a) {
		try {
			return new BigDecimal(a);
		} catch (NumberFormatException ex) {
			return new BigDecimal(Double.NaN);
		}
	}
	public final static BigDecimal decimalValue(final long a) {
		return new BigDecimal(a);
	}
	public final static BigDecimal decimalValue(final double a) {
		return new BigDecimal(a);
	}
	// Decimal methods
	public final static BigDecimal abs(final BigDecimal a) {return a.abs();}
	public final static BigDecimal add(final BigDecimal a, final BigDecimal b) {
		return a.add(b);
	}
	public final static BigDecimal add(final BigDecimal a, final long b) {
		return a.add(new BigDecimal(b));
	}
	public final static BigDecimal add(final BigDecimal a, final double b) {
		return a.add(new BigDecimal(b));
	}
	public final static long compare(final BigDecimal a, final BigDecimal b) {
		return a.compareTo(a);
	}
	public final static long compare(final BigDecimal a, final long b) {
		return a.compareTo(a);
	}
	public final static long compare(final BigDecimal a, final double b) {
		return a.compareTo(a);
	}
	public final static BigDecimal divide(final BigDecimal a,
		final BigDecimal b) {
		return a.divide(b);
	}
	public final static BigDecimal divide(final BigDecimal a, final long b) {
		return a.divide(new BigDecimal(b));
	}
	public final static BigDecimal divide(final BigDecimal a, final double b) {
		return a.divide(new BigDecimal(b));
	}
	public final static boolean equals(final BigDecimal a, final BigDecimal b) {
		return a.equals(b);
	}
	public final static boolean equals(final BigDecimal a, final long b) {
		return a.equals(b);
	}
	public final static boolean equals(final BigDecimal a, final double b) {
		return a.equals(b);
	}
	public final static long intValue(final BigDecimal a){return a.longValue();}
	public final static double floatValue(final BigDecimal a){
		return a.doubleValue();
	}
	public final static BigDecimal max(final BigDecimal a, final BigDecimal b) {
		return a.max(b);
	}
	public final static BigDecimal max(final BigDecimal a, final long b) {
		return a.max(new BigDecimal(b));
	}
	public final static BigDecimal max(final BigDecimal a, final double b) {
		return a.max(new BigDecimal(b));
	}
	public final static BigDecimal max(final long a, final BigDecimal b) {
		return new BigDecimal(a).max(b);
	}
	public final static BigDecimal max(final double a, final BigDecimal b) {
		return new BigDecimal(a).max(b);
	}
	public final static BigDecimal min(final BigDecimal a, final BigDecimal b) {
		return a.min(b);
	}
	public final static BigDecimal min(final BigDecimal a, final long b) {
		return a.min(new BigDecimal(b));
	}
	public final static BigDecimal min(final BigDecimal a, final double b) {
		return a.min(new BigDecimal(b));
	}
	public final static BigDecimal min(final long a, final BigDecimal b) {
		return new BigDecimal(a).min(b);
	}
	public final static BigDecimal min(final double a, final BigDecimal b) {
		return new BigDecimal(a).min(b);
	}
	public final static BigDecimal movePointLeft(final BigDecimal a,
		final long b) {
		return a.movePointLeft((int) b);
	}
	public final static BigDecimal movePointRight(final BigDecimal a,
		final long b) {
		return a.movePointRight((int) b);
	}
	public final static BigDecimal multiply(final BigDecimal a,
		final BigDecimal b) {
		return a.multiply(b);
	}
	public final static BigDecimal multiply(final BigDecimal a, final long b) {
		return a.multiply(new BigDecimal(b));
	}
	public final static BigDecimal multiply(final BigDecimal a, final double b){
		return a.multiply(new BigDecimal(b));
	}
	public final static BigDecimal negate(final BigDecimal a) {
		return a.negate();
	}
	public final static BigDecimal plus(final BigDecimal a) {return a.plus();}
	public final static BigDecimal pow(final BigDecimal a, final long b) {
		return a.pow((int) b);
	}
	public final static BigDecimal remainder(final BigDecimal a,
		final BigDecimal b) {
		return a.remainder(b);
	}
	public final static BigDecimal remainder(final BigDecimal a, final long b) {
		return a.remainder(new BigDecimal(b));
	}
	public final static BigDecimal remainder(final BigDecimal a,final double b){
		return a.remainder(new BigDecimal(b));
	}
	public final static BigDecimal round(final BigDecimal a) {
		return a.round(MathContext.UNLIMITED);
	}
	public final static BigDecimal scaleByPowerOfTen(final BigDecimal a,
		final long b) {
		return a.scaleByPowerOfTen((int) b);
	}
	public final static BigDecimal setScale(final BigDecimal a, final long b) {
		return a.setScale((int) b);
	}
	public final static BigDecimal stripTrailingZeros(final BigDecimal a) {
		return a.stripTrailingZeros();
	}
	public final static BigDecimal subtract(final BigDecimal a,
		final BigDecimal b) {
		return a.subtract(b);
	}
	public final static BigDecimal subtract(final BigDecimal a, final long b) {
		return a.subtract(new BigDecimal(b));
	}
	public final static BigDecimal subtract(final BigDecimal a, final double b){
		return a.subtract(new BigDecimal(b));
	}
	public final static BigDecimal ulp(final BigDecimal a) {return a.ulp();}
}