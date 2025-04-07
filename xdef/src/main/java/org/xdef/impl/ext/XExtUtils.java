package org.xdef.impl.ext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.net.InetAddress;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDConstants;
import org.xdef.XDContainer;
import org.xdef.XDCurrency;
import org.xdef.XDEmailAddr;
import org.xdef.XDFactory;
import org.xdef.XDGPSPosition;
import org.xdef.XDParseResult;
import org.xdef.XDPrice;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ELEMENT;
import static org.xdef.XDValueID.XX_ELEMENT;
import org.xdef.impl.ChkNode;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefChar;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefIPAddr;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefXPathExpr;
import org.xdef.msg.XDEF;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXNode;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.Price;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SError;
import org.xdef.sys.SException;
import org.xdef.sys.SPosition;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xon.XonNames;
import org.xdef.xon.XonTools;

/** External utilities called from X-definition processor.
 * @author Vaclav Trojan
 */
public final class XExtUtils {

	/** Get information about actual version of X-definition.
	 * @return build version and datetime.
	 */
	public static final String getVersionInfo() {
		return XDConstants.BUILD_VERSION + " " + XDConstants.BUILD_DATETIME;
	}

	/** Get the prefix part of given QName.
	 * @param s string with QName,
	 * @return prefix part of given QName or the empty string.
	 */
	public static final String getQnamePrefix(final String s) {
		int ndx = s.indexOf(':');
		return ndx > 0 ? s.substring(0, ndx) : "";
	}

	/** Get the local part of given QName.
	 * @param s string with QName,
	 * @return local part of given QName .
	 */
	public static final String getQnameLocalpart(final String s) {
		int ndx = s.indexOf(':');
		return ndx > 0 ? s.substring(ndx + 1) : s;
	}

	/** Check if the given year is a leap year.
	 * @param n the year
	 * @return true if the the year is a leap year.
	 */
	public static final boolean isLeapYear(final long n) {return SDatetime.isLeapYear((int) n);}

	/** Check if the year from given date is a leap year.
	 * @param d date to be checked.
	 * @return true if the the year from given date is a leap year.
	 */
	public static final boolean isLeapYear(final SDatetime d) {return d.isLeapYear();}

	/** Get the date with the Easter Monday of the year from given date.
	 * @param date date with the year to be used for computing Easter Monday date.
	 * @return date with the Easter Monday of the year from given date.
	 */
	public static final SDatetime easterMonday(final SDatetime date) {return date.getEasterMonday();}

	/** Get the date with the Easter Monday of given year.
	 * @param year year to be used for computing Easter Monday date.
	 * @return date with the Easter Monday of given year
	 */
	public static final SDatetime easterMonday(final long year) {return SDatetime.getEasterMonday((int)year);}

	/** Check if XQuery implementation is available.
	 * @return true if XQuery implementation is available.
	 */
	public static final boolean isXQuerySupported() {return XDFactory.isXQuerySupported();}

	/** Check if XPath2 implementation is available.
	 * @return true if XPath2 implementation is available.
	 */
	public static final boolean isXPath2Supported() {return XDFactory.isXPath2Supported();}

	/**	Get namespace URI of qualified name.
	 * @param qname qualified name
	 * @param elem element where namespace URI is searched.
	 * @return namespace URI or an empty string.
	 */
	public static final String getQnameNSUri(final String qname, final Element elem) {
		byte xmlVersion = "1.1".equals(elem.getOwnerDocument().getXmlVersion())
			? StringParser.XMLVER1_1 : StringParser.XMLVER1_0;
		if (!StringParser.chkXMLName(qname, xmlVersion)) {
			return "";
		}
		int ndx = qname.indexOf(':');
		String prefix = ndx > 0 ? qname.substring(0, ndx) : "";
		return getNSUri(prefix, elem);
	}

	/** Get namespace URI of given prefix from the context of an element.
	 * @param pfx string with the prefix.
	 * @param elem the element.
	 * @return namespace URI.
	 */
	public static final String getNSUri(final String pfx, final Element elem) {
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

	/** Get w3c.dom.Node from the XXNode. */
	private static Node getActualNode(final XXNode x) {
		Element el = x.getElement();
		return x.getItemId() == XXNode.XX_ELEMENT ? el : el.getLastChild();
	}

	/** Add comment to the XXNode.
	 * @param x where to add.
	 * @param s text of comment.
	 */
	public static final void addComment(final XXNode x, final String s) {
		Node n = getActualNode(x);
		addComment((n != null ? n : x.getElement()), s);
	}

	/** Add comment to the org.w3c.dom.Node.
	 * @param n where to add.
	 * @param s text of comment.
	 */
	public static final void addComment(final Node n, final String s) {
		n.getParentNode().appendChild(n.getOwnerDocument().createComment(s != null? s : ""));
	}

	/** Insert comment to XXNode.
	 * @param x where to insert.
	 * @param s text of comment.
	 */
	public static final void insertComment(final XXNode x, final String s) {
		Node n = getActualNode(x);
		if (n != null) {
			insertComment(n, s);
		} else {
			addComment(x.getElement(), s);
		}
	}

	/** Insert comment to org.w3c.dom.Node.
	 * @param n where to insert.
	 * @param s text of comment.
	 */
	public static final void insertComment(final Node n, final String s) {
		n.getParentNode().insertBefore(n.getOwnerDocument().createComment(s), n);
	}

	/** Add programming instruction to the XXNode.
	 * @param x where to add.
	 * @param target target of PI.
	 * @param data data of PI.
	 */
	public static final void addPI(final XXNode x, final String target, final String data) {
		Node n = getActualNode(x);
		addPI(n != null ? n : x.getElement(), target, data);
	}

	/** Add programming instruction to the org.w3c.dom.Node.
	 * @param n where to add.
	 * @param target target of PI.
	 * @param data data of PI.
	 */
	public static final void addPI(final Node n, final String target, final String data) {
		n.getParentNode().appendChild(n.getOwnerDocument().createProcessingInstruction(target, data));
	}

	/** Insert programming instruction to the XXNode.
	 * @param x where to insert.
	 * @param target target of PI.
	 * @param data data of PI.
	 */
	public static final void insertPI(final XXNode x, final String target, final String data) {
		Node n = getActualNode(x);
		if (n != null) {
			insertPI(n, target, data);
		} else {
			addPI(x.getElement(), target, data);
		}
	}

	/** Insert programming instruction to the org.w3c.dom.Node.
	 * @param n where to insert.
	 * @param target target of PI.
	 * @param data data of PI.
	 */
	public static final void insertPI(final Node n, final String target, final String data) {
		n.getParentNode().insertBefore(n.getOwnerDocument().createProcessingInstruction(target,data), n);
	}

	/** Add text to the XXNode.
	 * @param x where to add.
	 * @param s text value.
	 */
	public static final void addText(final XXNode x, final String s) {addText(x.getElement(), s);}

	/** Add text to the org.w3c.Element.
	 * @param el where to add.
	 * @param s text value.
	 */
	public static final void addText(final Element el, final String s) {
		if (s != null && !s.isEmpty()) {
			el.appendChild(el.getOwnerDocument().createTextNode(s));
		}
	}

	/** Insert text to the XXNode.
	 * @param x where to insert.
	 * @param s text value.
	 */
	public static final void insertText(final XXNode x, final String s) {
		Node n = getActualNode(x);
		if (n != null) {
			insertText(n, s);
		} else {
			addText(x.getElement(), s);
		}
	}

	/** Insert text to org.w3c.dom.Node.
	 * @param n where to insert.
	 * @param s text value.
	 */
	public static final void insertText(final Node n, final String s) {
		if (s != null && !s.isEmpty()) {
			n.getParentNode().insertBefore(n.getOwnerDocument().createTextNode(s), n);
		}
	}

	/** Get text content of XXNode.
	 * @param x node with text content.
	 * @return text content of XXNode.
	 */
	public static final String getTextContent(final XXNode x) {return getTextContent(x.getElement());}

	/** Get text content of org.w3c.do,.Element.
	 * @param el element with text content.
	 * @return text content of element.
	 */
	public static final String getTextContent(final Element el) {
		return el != null ? el.getTextContent() : null;
	}

	/** Get X-position of XXNode.
	 * @param x node with position.
	 * @return X-position of node.
	 */
	public static final String getXPos(final XXNode x) {return x.getXPos();}

	/** Get X-position of model in X-definition.
	 * @param x node with position.
	 * @return X-position of model of XXNode..
	 */
	public static final String getXDPosition(final XXNode x) {return x.getXMNode().getXDPosition();}

	/** Get source line number of XXNode.
	 * @param x node with line number.
	 * @return source line numer of node.
	 */
	public static final long getSourceLine(final XXNode x) {
		SPosition spos = x.getSPosition();
		return spos != null ? spos.getLineNumber() : 0;
	}

	/** Get source column number of XXNode.
	 * @param x node.
	 * @return source column numer of node.
	 */
	public static final long getSourceColumn(final XXNode x) {
		SPosition spos = x.getSPosition();
		return spos != null ? spos.getColumnNumber() : 0;
	}

	/** Get system id of XXNode.
	 * @param x node.
	 * @return system id of XXNode.
	 */
	public static final String getSysId(final XXNode x) {
		SPosition spos = x.getSPosition();
		return spos != null ? spos.getSysId() : "";
	}

	/** Get source position of given node.
	 * @param x node with source position.
	 * @return source position of node or null if it is not available.
	 */
	public static final String getSourcePosition(final XXNode x) {
		SPosition spos = x.getSPosition();
		return spos != null ? spos.toString() : "";
	}

	/**	Returns the current time in milliseconds (depends on underlying
	 * operating system).
	 * @return the current time in milliseconds.
	 */
	public static final long currentTimeMillis() {return System.currentTimeMillis();}

	/** Get value of environmental variable.
	 * @param name of environmental variable.
	 * @return value of environmental variable.
	 */
	public static final String getEnv(final String name) {return System.getenv(name);}

	/** Get user name from email address.
	 * @param x email address.
	 * @return user name from the email address.
	 */
	public static final String getEmailUserName(final XDEmailAddr x) {return x.getUserName();}

	/** Get local part from email address.
	 * @param x email address.
	 * @return local part from email address.
	 */
	public static final String getEmailLocalPart(final XDEmailAddr x) {return x.getLocalPart();}

	/** Get domain from email address.
	 * @param x email address.
	 * @return domain from email address.
	 */
	public static final String getEmailDomain(final XDEmailAddr x) {return x.getDomain();}

	/** Get string from email address.
	 * @param x email address.
	 * @return  string created from email address.
	 */
	public static final String getEmailAddr(final XDEmailAddr x){return x.getEmailAddr();}

	/** Get string from internet address.
	 * @param x internet address.
	 * @return  string created from internet address.
	 */
	public static final String getHostAddress(final InetAddress x){return x.getHostAddress();}

	/** Get bytes created from internet address.
	 * @param x internet address.
	 * @return bytes created from internet address.
	 */
	public static final byte[] getBytes(final InetAddress x) {return x.getAddress();}

	/** Check if InetAddres from argument is version 6.
	 * @param x InetAddres to be checked
	 * @return if InetAddres from argument is version 6.
	 */
	public static final boolean isIPv6(final InetAddress x) {
		return x == null ? false : x.getAddress().length > 4;
	}

	/** Get value of XON key from XXNoede as string.
	 * @param x node.
	 * @return value of XON key from XXNoede as string.
	 */
	public static final String getXonKey(final XXNode x) {
		String s = x.getElement().getAttribute(XonNames.X_KEYATTR);
		return s != null ? XonTools.xmlToJName(s) : null;
	}

	/** Get XDValue from object.
	 * @param o object.
	 * @return XDValue created from object.
	 */
	public static final XDValue getXDValueOfObject(final Object o) {
		if (o instanceof Map) {
			DefContainer c = new DefContainer();
			Map x = (Map) o;
			for (Object y : x.entrySet()) {
				Map.Entry en = (Map.Entry) y;
				c.setXDNamedItem((String)en.getKey(), getXDValueOfObject(en.getValue()));
			}
			return c;
		} else if (o instanceof List) {
			DefContainer c = new DefContainer();
			List x = (List) o;
			for (Object y : x) {
				c.addXDItem(getXDValueOfObject(y));
			}
			return c;
		} else if (o instanceof XDValue) {
			return (XDValue) o;
		} else if (o instanceof String) {
			return new DefString((String) o);
		} else if (o instanceof Boolean) {
			return new DefBoolean((Boolean) o);
		} else if (o instanceof Character) {
			return new DefChar((Character) o);
		} else if (o instanceof Float || o instanceof Long) {
			return new DefDouble(((Number) o).doubleValue());
		} else if (o instanceof Byte || o instanceof Short
			|| o instanceof Integer	|| o instanceof Long) {
			return new DefLong(((Number) o).longValue());
		} else if (o instanceof BigInteger) {
			return new DefBigInteger(((BigInteger) o));
		} else if (o instanceof BigDecimal) {
			return new DefDecimal(((BigDecimal) o));
		} else if (o instanceof SDatetime) {
			return new DefDate((SDatetime) o);
		} else if (o instanceof SDuration) {
			return new DefDuration((SDuration) o);
		} else if (o instanceof InetAddress) {
			return new DefIPAddr((InetAddress) o);
		} else if (o instanceof GPSPosition) {
			return new XDGPSPosition((GPSPosition) o);
		} else if (o instanceof Price) {
			return new XDPrice((Price) o);
		} else if (o instanceof Currency) {
			return new XDCurrency((Currency) o);
		}
		return DefNull.NULL_VALUE;
	}

	/** Get XDValue from XXElement containg XON value.
	 * @param xel XXElement with XON value..
	 * @return XDValue created from XXElement containg XON value.
	 */
	public static final XDValue getXDValueOfXon(final XXElement xel){return getXDValueOfObject(xel.getXon());}

/* **********************************
* Implementation of script methods. *
************************************/

	/** Cancel running X-definition process. */
	public static final void cancel() {throw new SError(Report.error(XDEF.XDEF906));} //X-definition canceled

	/** Cancel running X-definition process and throw message.
	 * @param msg reason of cancelling.
	 */
	public static final void cancel(final String msg) {
		throw new SError(Report.error(XDEF.XDEF906, msg)); //X-definition canceled&{0}{; }
	}

	/** Parse base64 data.
	 * @param s base64 data
	 * @return parsed bytes from base64 data.
	 */
	public static final byte[] parseBase64(final String s) {
		try {
			return SUtils.decodeBase64(s);
		} catch (SException ex) {
			return null;
		}
	}

	/** Parse hexadecimal data.
	 * @param s hexadecimal data
	 * @return parsed bytes from hexadecimal data.
	 */
	public static final byte[] parseHex(final String s) {
		try {
			return SUtils.decodeHex(s);
		} catch (SException ex) {
			return null;
		}
	}

/* ********************
* Methods with XXNode *
**********************/

	public static final Element getCreateContextElement(final XXNode xElem) {
		return ((ChkNode) xElem).getElemValue();
	}
	public static final Element getParentContextElement(final XXNode xElem) {
		XDValue val = ((XXElement) xElem.getParent()).getXDContext();
		if (val == null || val.getItemId() != XD_ELEMENT) {
			return null;
		}
		return val.getElement();
	}
	public static final Element getParentContextElement(final XXNode xElem, final long level) {
		if (level == 0) {
			return getParentContextElement(xElem);
		}
		XXNode xel = xElem;
		for (int i = 0; i < level; i++) {
			xel = xel.getParent();
			if (xel == null || xel.getItemId() != XX_ELEMENT) {
				xel = null;
				break;
			}
		}
		if (xel != null) {
			XDValue val = ((XXElement) xel).getXDContext();
			if (val != null && val.getItemId() == XD_ELEMENT){
				return val.getElement();
			}
		}
		return null;
	}
	public static final XDContainer fromParent(final XXNode xel,final String exp) {
		XDValue val = xel.getParent().getXDContext();
		if (val == null || val.getItemId() != XD_ELEMENT) {
			return new DefContainer();
		}
		Element el = val.getElement();
		DefXPathExpr xe = new DefXPathExpr(exp,
			xel.getXXNamespaceContext(), xel.getXXFunctionResolver(), xel.getXXVariableResolver());
		return new DefContainer(xe.exec(el));
	}
	public static final XDContainer fromRoot(final XXNode xElem, final String exp, final Element elem) {
		DefXPathExpr xe = new DefXPathExpr(exp,
			xElem.getXXNamespaceContext(), xElem.getXXFunctionResolver(), xElem.getXXVariableResolver());
		return new DefContainer(xe.exec(elem));
	}
	public static final XDContainer fromRoot(final XXNode xElem, final String expr) {
		XDValue val = xElem.getXDDocument().getXDContext();
		return (val == null || val.getItemId() != XD_ELEMENT)
			? new DefContainer() : fromRoot(xElem, expr, val.getElement());
	}

/* ************
* PARSERESULT *
**************/
	public static final void clearReports(final XDParseResult x) {x.clearReports();}
	public static final String getSource(final XDParseResult x) {return x.getSourceBuffer();}
/* *********
* dateTime *
***********/
	public static final int getMaxYear(final XXNode xnode) {return xnode.getXDPool().getMaxYear();}
	public static final void setMaxYear(XXNode xnode, int i) {xnode.getXDDocument().setMaxYear(i);}
	public static final int getMinYear(final XXNode xnode) {return xnode.getXDPool().getMaxYear();}
	public static final void setMinYear(final XXNode xnode, int i) {xnode.getXDDocument().setMinYear(i);}
	public static final XDContainer getSpecialDates(final XXNode xnode) {
		SDatetime[] dates = xnode.getXDDocument().getSpecialDates();
		DefContainer c = new DefContainer();
		if (dates != null) {
			for (SDatetime d : dates) {
				c.addXDItem(new DefDate(d));
			}
		}
		return c;
	}
	public static final void setSpecialDates(final XXNode xnode, final XDContainer c) {
		SDatetime[] dates = new SDatetime[c.getXDItemsNumber()];
		for (int i = 0; i < dates.length; i++) {
			dates[i] = c.getXDItem(i).datetimeValue();
		}
		xnode.getXDDocument().setSpecialDates(dates);
	}
	public static final SDatetime parseEmailDate(final String x) {
		StringParser p = new StringParser((x == null) ? "" : x.trim());
		return p.isRFC822Datetime() && p.eos() && p.testParsedDatetime() ? p.getParsedSDatetime() : null;
	}

/* ************************************************************************
* Implementation of predefined X-script Math methods (ensure conversion   *
* of arguments long -> double). Other math methods are available in Math. *
**************************************************************************/
	public static final double acos(final long a) {return Math.acos(a);}
	public static final double asin(final long a) {return Math.asin(a);}
	public static final double atan(final long a) {return Math.atan(a);}
	public static final double atan2(final long a, long b) {return Math.atan2(a,b);}
	public static final double atan2(final long a, double b) {return Math.atan2(a,b);}
	public static final double atan2(double a, long b) {return Math.atan2(a,b);}
	public static final double cbrt(final long a) {return Math.cbrt(a);}
	public static final double ceil(final long a) {return Math.ceil(a);}
	public static final double cos(final long a) {return Math.cos(a);}
	public static final double cosh(final long a) {return Math.cosh(a);}
	public static final double exp(final long a) {return Math.exp(a);}
	public static final double expm1(final long a) {return Math.expm1(a);}
	public static final double floor(final long a) {return Math.floor(a);}
	public static final double hypot(final long a, long b) {return Math.hypot(a, b);}
	public static final double hypot(final long a, double b) {return Math.hypot(a,b);}
	public static final double hypot(double a, long b) {return Math.hypot(a,b);}
	public static final double IEEEremainder(final long a, long b) {return Math.IEEEremainder(a,b);}
	public static final double IEEEremainder(final long a, double b) {return Math.IEEEremainder(a,b);}
	public static final double IEEEremainder(double a, long b) {return Math.IEEEremainder(a,b);}
	public static final double log(final long a) {return Math.log(a);}
	public static final double log10(final long a) {return Math.log10(a);}
	public static final double log1p(final long a) {return Math.log1p(a);}
	public static final long max(final long a, long b) {return Math.max(a, b);}
	public static final double max(final double a, long b) {return Math.max(a, b);}
	public static final double max(final long a, double b) {return Math.max(a, b);}
	public static final long min(final long a, long b) {return Math.min(a, b);}
	public static final double min(final double a, long b) {return Math.min(a, b);}
	public static final double min(final long a, double b) {return Math.min(a, b);}
	public static final double pow(final long a, long b) {return Math.pow(a, b);}
	public static final double pow(final long a, double b) {return Math.pow(a, b);}
	public static final double pow(final double a, long b) {return Math.pow(a, b);}
	public static final double rint(final long a) {return Math.rint(a);}
	public static final long round(final long a) {return Math.round(a);}
	public static final double signum(final long a) {return Math.signum(a);}
	public static final double sin(final long a) {return Math.sin(a);}
	public static final double sinh(final long a) {return Math.sinh(a);}
	public static final double sqrt(final long a) {return Math.sqrt(a);}
	public static final double tan(final long a) {return Math.tan(a);}
	public static final double tanh(final long a) {return Math.tanh(a);}
	public static final double toDegrees(final long a) {return Math.toDegrees(a);}
	public static final double toRadians(final long a) {return Math.toRadians(a);}
	public static final double ulp(final long a) {return Math.ulp(a);}
	// Decimal constructors
	public static final BigDecimal decimalValue(final BigDecimal a) {return new BigDecimal(a.toString());}
	public static final BigDecimal decimalValue(final String a) {return new BigDecimal(a);}
	public static final BigDecimal decimalValue(final long a) {return new BigDecimal(a);}
	public static final BigDecimal decimalValue(final double a) {return new BigDecimal(a);}
	// Decimal methods
	public static final BigDecimal abs(final BigDecimal a) {return a.abs();}
	public static final BigDecimal add(final BigDecimal a, final BigDecimal b) {return a.add(b);}
	public static final BigDecimal add(final BigDecimal a, final long b) {return a.add(new BigDecimal(b));}
	public static final BigDecimal add(final BigDecimal a, final double b) {return a.add(new BigDecimal(b));}
	public static final long compare(final BigDecimal a, final BigDecimal b){return a.compareTo(a);}
	public static final long compare(final BigDecimal a, final long b) {return a.compareTo(a);}
	public static final long compare(final BigDecimal a, final double b) {return a.compareTo(a);}
	public static final BigDecimal divide(final BigDecimal a, final BigDecimal b) {return a.divide(b);}
	public static final BigDecimal divide(final BigDecimal a, final long b) {
		return a.divide(new BigDecimal(b));
	}
	public static final BigDecimal divide(final BigDecimal a, final double b) {
		return a.divide(new BigDecimal(b));
	}
	public static final boolean equals(final BigDecimal a, final BigDecimal b) {return a.equals(b);}
	public static final boolean equals(final BigDecimal a, final long b) {return a.equals(b);}
	public static final boolean equals(final BigDecimal a, final double b) {return a.equals(b);}
	public static final long intValue(final BigDecimal a){return a.longValue();}
	public static final double floatValue(final BigDecimal a) {return a.doubleValue();}
	public static final BigDecimal max(final BigDecimal a, final BigDecimal b) {return a.max(b);}
	public static final BigDecimal max(final BigDecimal a, final long b) {return a.max(new BigDecimal(b));}
	public static final BigDecimal max(final BigDecimal a, final double b) {return a.max(new BigDecimal(b));}
	public static final BigDecimal max(final long a, final BigDecimal b) {return new BigDecimal(a).max(b);}
	public static final BigDecimal max(final double a, final BigDecimal b) {return new BigDecimal(a).max(b);}
	public static final BigDecimal min(final BigDecimal a, final BigDecimal b) {return a.min(b);}
	public static final BigDecimal min(final BigDecimal a, final long b) {return a.min(new BigDecimal(b));}
	public static final BigDecimal min(final BigDecimal a, final double b) {return a.min(new BigDecimal(b));}
	public static final BigDecimal min(final long a, final BigDecimal b) {return new BigDecimal(a).min(b);}
	public static final BigDecimal min(final double a, final BigDecimal b) {return new BigDecimal(a).min(b);}
	public static final BigDecimal movePointLeft(final BigDecimal a, final long b) {
		return a.movePointLeft((int) b);
	}
	public static final BigDecimal movePointRight(final BigDecimal a, final long b) {
		return a.movePointRight((int) b);
	}
	public static final BigDecimal multiply(final BigDecimal a, final BigDecimal b) {return a.multiply(b);}
	public static final BigDecimal multiply(final BigDecimal a, final long b) {
		return a.multiply(new BigDecimal(b));
	}
	public static final BigDecimal multiply(final BigDecimal a, final double b) {
		return a.multiply(new BigDecimal(b));
	}
	public static final BigDecimal negate(final BigDecimal a) {return a.negate();}
	public static final BigDecimal plus(final BigDecimal a) {return a.plus();}
	public static final BigDecimal pow(final BigDecimal a, final long b) {return a.pow((int) b);}
	public static final BigDecimal remainder(final BigDecimal a, final BigDecimal b) {return a.remainder(b);}
	public static final BigDecimal remainder(final BigDecimal a, final long b) {
		return a.remainder(new BigDecimal(b));
	}
	public static final BigDecimal remainder(final BigDecimal a,final double b) {
		return a.remainder(new BigDecimal(b));
	}
	public static final BigDecimal round(final BigDecimal a){return a.round(MathContext.UNLIMITED);}
	public static final BigDecimal scaleByPowerOfTen(final BigDecimal a,final long b) {
		return a.scaleByPowerOfTen((int) b);
	}
	public static final BigDecimal setScale(final BigDecimal a, final long b) {return a.setScale((int) b);}
	public static final BigDecimal stripTrailingZeros(final BigDecimal a) {return a.stripTrailingZeros();}
	public static final BigDecimal subtract(final BigDecimal a, final BigDecimal b) {return a.subtract(b);}
	public static final BigDecimal subtract(final BigDecimal a, final long b) {
		return a.subtract(new BigDecimal(b));
	}
	public static final BigDecimal subtract(final BigDecimal a, final double b) {
		return a.subtract(new BigDecimal(b));
	}
	public static final BigDecimal ulp(final BigDecimal a) {return a.ulp();}
}
