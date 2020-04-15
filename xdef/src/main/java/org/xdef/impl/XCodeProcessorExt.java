package org.xdef.impl;

import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefNamedValue;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefXmlWriter;
import org.xdef.impl.code.DefString;
import org.xdef.impl.code.DefSQLService;
import org.xdef.impl.code.DefElement;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefException;
import org.xdef.impl.code.DefOutStream;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefObject;
import org.xdef.impl.code.DefInStream;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefReport;
import org.xdef.msg.XDEF;
import org.xdef.sys.Report;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SError;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDException;
import org.xdef.XDOutput;
import org.xdef.XDParseResult;
import org.xdef.XDParser;
import org.xdef.XDReport;
import org.xdef.XDService;
import org.xdef.XDValue;
import org.xdef.XDXmlOutStream;
import org.xdef.proc.XXElement;
import org.xdef.proc.XXException;
import org.xdef.proc.XXNode;
import org.xdef.impl.ext.XExtUtils;
import org.xdef.impl.code.CodeExtMethod;
import org.xdef.impl.code.CodeDisplay;
import org.xdef.impl.compile.CompileBase;
import org.xdef.impl.code.CodeTable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.xml.XMLConstants;
import org.w3c.dom.Element;
import org.xdef.XDValueID;
import org.xdef.impl.code.DefLocale;

/** Provides invoking of external method from script code.
 * @author Vaclav Trojan
 */
final class XCodeProcessorExt implements CodeTable, XDValueID {

	private XCodeProcessorExt() {}

	static final XDValue perform1v(final XDValue item, final XDValue p) {
		switch (item.getCode()) {
			case GET_TYPEID: //get type of a value (as integer type id)
				return new DefLong(p.getItemId());
			case GET_TYPENAME: // get name of type of a value
				return new DefString(
					CompileBase.getTypeName(p.getItemId()));
			case CHECK_TYPE: // check type conversion
				if (p != null && !p.isNull() &&
					p.getItemId() != item.getParam()) {
					switch (p.getItemId()) {
						case XD_BOOLEAN:
							return new DefBoolean(p.booleanValue());
						case XD_INT:
							return new DefLong(p.intValue());
						case XD_FLOAT:
							return new DefDouble(p.floatValue());
						case XD_DECIMAL:
							return new DefDecimal(p.decimalValue());
						case XD_DATETIME:
							return new DefDate(p.datetimeValue());
						case XD_DURATION:
							return new DefDuration(p.durationValue());
						case XD_STRING:
							return new DefString(p.stringValue());
						case XD_ELEMENT:
							if (item.getParam() == XD_CONTAINER) {
								return new DefContainer(p);
							}
					}
					//Icorrect type conversion from AnyValue
					throw new SRuntimeException(XDEF.XDEF536);
				}
				return p;
			case BYTES_CLEAR: //Clear byte array
				((DefBytes) p).clear();
				return p;
			case BYTES_SIZE: //size of byte array
				return new DefLong(((DefBytes) p).size());
			case BYTES_TO_BASE64:
				return new DefString(((DefBytes) p).getBase64());
			case BYTES_TO_HEX:
				return new DefString(((DefBytes) p).getHex());
			case PARSE_DURATION: //Duration
				try {
					return new DefDuration(p.toString());
				} catch (Exception ex) {
					return DefNull.genNullValue(XD_DURATION);
				}
			case DURATION_GETYEARS:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getYears());
			case DURATION_GETMONTHS:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getMonths());
			case DURATION_GETDAYS:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getDays());
			case DURATION_GETHOURS:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getHours());
			case DURATION_GETMINUTES:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getMinutes());
			case DURATION_GETSECONDS:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getSeconds());
			case DURATION_GETRECURRENCE:
				return p == null || p.isNull()
					? new DefLong(-1)
					: new DefLong(p.durationValue().getRecurrence());
			case DURATION_GETFRACTION:
				return p == null || p.isNull()
					? new DefDouble(-1)
					: new DefDouble(p.durationValue().getFraction());
			case DURATION_GETSTART:
				return p == null || p.isNull()
					? new DefDate()
					: new DefDate(p.durationValue().getStart());
			case DURATION_GETEND:
				return p == null || p.isNull()
					? new DefDate()
					: new DefDate(p.durationValue().getEnd());
			case DURATION_GETNEXTTIME:
				return p == null || p.isNull()
					? new DefDate()
					: new DefDate(p.durationValue().getNextTime());
		//Element
			case ELEMENT_CHILDNODES: {
				Element el;
				return p == null || p.isNull() || (el = p.getElement()) == null
					? new DefContainer() : new DefContainer(el.getChildNodes());
			}
			case ELEMENT_NAME: {
				Element el;
				return p == null || p.isNull() || (el = p.getElement()) == null
					? new DefString(null) : new DefString(el.getTagName());
			}
			case ELEMENT_NSURI: {
				Element el;
				return p == null || p.isNull() || (el = p.getElement()) == null
					? new DefString(null) : new DefString(el.getNamespaceURI());
			}
			//ParseResult
			case GET_PARSED_STRING:
				return new DefString(((DefParseResult) p).getSourceBuffer());
			//datetime
			case GET_DAY: //Get day from date
				return new DefLong(p.datetimeValue().getDay());
			case GET_WEEKDAY: //Get week day
				return new DefLong(p.datetimeValue().getDayOfWeek());
			case GET_MONTH: //Get month from date
				return new DefLong(p.datetimeValue().getMonth());
			case GET_YEAR: //Get year from date
				return new DefLong(p.datetimeValue().getYear());
			case GET_HOUR: //Get hour from date
				return new DefLong(p.datetimeValue().getHour());
			case GET_MINUTE: //Get minute from date
				return new DefLong(p.datetimeValue().getMinute());
			case GET_SECOND: //Get second
				return new DefLong(p.datetimeValue().getSecond());
			case GET_MILLIS: //Get millisecond
				return new DefLong(p.datetimeValue().getMillisecond());
			case GET_NANOS:
				return new DefLong(p.datetimeValue().getNanos());
			case GET_FRACTIONSECOND: //get fraction of second
				return new DefDouble(p.datetimeValue().getFraction());
			case GET_EASTERMONDAY:
				return new DefDate(p.getItemId() == XD_DATETIME
					? p.datetimeValue().getEasterMonday()
					: SDatetime.getEasterMonday(p.intValue()));
			case GET_LASTDAYOFMONTH:
				return new DefLong(
					SDatetime.getLastDayOfMonth(p.datetimeValue()));
			case GET_DAYTIMEMILLIS: //get daytime
				return new DefLong(p.datetimeValue().getDaytimeInMillis());
			case GET_ZONEOFFSET: //zone shift to GMT
				return new DefLong(p.datetimeValue().getTimeZoneOffset());
			case GET_ZONEID: //get time zone name
				return new DefString(p.datetimeValue().getTZ().getID());
			case IS_LEAPYEAR: //check leap year.
				// Return true if date is leap year.
				return new DefBoolean(SDatetime.isLeapYear(
					p.getItemId() == XD_INT
						? p.intValue() : p.datetimeValue().getYear()));
			//String
			case LOWERCASE: { //set to lower case
				String s = p.stringValue();
				return s != null ? new DefString(s.toLowerCase()) : p;
			}
			case UPPERCASE: {//set to upper case
				String s = p.stringValue();
				return s != null ?	new DefString(s.toUpperCase()) : p;
			}
			case TRIM_S: {
				String s = p.stringValue();
				return s != null ? new DefString(s.trim()) : p;
			}
			case GET_STRING_LENGTH: { //s.length()
				String s = p.stringValue();
				return new DefLong(s != null ? s.length() : 0);
			}
			case WHITESPACES_S: {
				StringBuilder s=new StringBuilder(p.toString().trim());
				for (int i = s.length() -1; i >= 0; i--) {
					char c;
					int j = i;
					while (j >= 0 && ((c = s.charAt(j)) == ' ' ||
						c == '\n' || c == '\t'  || c == '\r')) {
						j--;
					}
					if (j < i) {
						s.replace(j+1,i+1, " ");
						i = j;
					}
				}
				return new DefString(s.toString());
			}
			//Report
			case GET_REPORT: {
				Report rep;
				if (p.isNull()) {
					rep = null;
				} else if (p.getItemId() == XD_EXCEPTION) {
					 rep = ((XDException) p).reportValue();
				} else {
					XDOutput out = (XDOutput) p;
					rep = out.getLastErrorReport();
				}
				return new DefReport(rep);
			}
		}
		return null;
	}

	static final void perform2(final XDValue cmd,
		final XDValue p1,
		final XDValue p2) {
		switch (cmd.getCode()) {
			//Element
			case ELEMENT_ADDELEMENT: { // Add element to element as child
				Element el1 = p1.getElement();
				el1.appendChild(
					el1.getOwnerDocument().importNode(p2.getElement(), true));
				return;
			}
			case ELEMENT_ADDTEXT: { // Add text to element as child
				Element el = p1.getElement();
				String s = p2.stringValue();
				if (s != null && !s.isEmpty()) {
					el.appendChild(el.getOwnerDocument().createTextNode(s));
				}
				return;
			}
			//Bytes
			case BYTES_ADDBYTE: //Add byte
				((DefBytes) p1).add(p2.intValue());
				return;
			//Report
			case PUT_REPORT:
				if (!p2.isNull()) {
					((XDOutput) p1).putReport(((XDReport)p2).reportValue());
				}
				return;
			//XmlWriter
			case SET_XMLWRITER_INDENTING:  // Set writer indenting.
				((XDXmlOutStream) p1).setIndenting(p2.booleanValue());
				return;
			case WRITE_TEXTNODE: // Write text node.
				((XDXmlOutStream) p1).writeText(p2.stringValue());
		}
	}

	static final XDValue perform2v(final XDValue cmd,
		final XDValue p1,
		final XDValue p2) {
		switch (cmd.getCode()) {
			//formating number to string
			case INTEGER_FORMAT:
			case FLOAT_FORMAT: {
				String s = p2.toString();
				int ndx;
				DecimalFormat ds;
				if (s.length() > 2 && s.startsWith("{L(") &&
					(ndx = s.indexOf(")}")) > 0) {
					StringTokenizer st =
						new StringTokenizer(s.substring(3,ndx), " \n\t\r,");
					s = s.substring(ndx + 2);
					ds = new DecimalFormat(s);
					String s1 = st.nextToken().toLowerCase();
					String s2 =
						(st.hasMoreTokens() ? st.nextToken():"").toUpperCase();
					DecimalFormatSymbols dfs = new DecimalFormatSymbols(
						st.hasMoreTokens() ?
							new Locale(s1,s2,st.nextToken()) :
							new Locale(s1,s2));
					ds.setDecimalFormatSymbols(dfs);
				} else {
					ds = new DecimalFormat(s);
					if (cmd.getCode() == FLOAT_FORMAT) {
						DecimalFormatSymbols df = ds.getDecimalFormatSymbols();
						df.setDecimalSeparator('.');
						ds.setDecimalFormatSymbols(df);
					}
				}
				return new DefString(cmd.getCode() == INTEGER_FORMAT ?
					ds.format(p1.longValue()):ds.format(p1.doubleValue()));
			}
		//Bytes
			case BYTES_GETAT: //Get byte at position
				return new DefLong(((DefBytes) p1).getAt(p2.intValue()));
		//datetime
			case DATE_FORMAT: //format date
				return new DefString(
					p1.datetimeValue().formatDate(p2.toString()));
			case ADD_DAY: //Add days to date.
				return new DefDate(p1.datetimeValue().add(
					0, 0, p2.intValue(), 0, 0, 0, 0.0));
			case ADD_MONTH: //Add months to date.
				return new DefDate(p1.datetimeValue().add(
					0, p2.intValue(), 0, 0, 0, 0, 0.0));
			case ADD_YEAR: //Add years to date.
				return new DefDate(p1.datetimeValue().add(
					p2.intValue(), 0, 0, 0, 0, 0, 0.0));
			case ADD_HOUR: //Add hours to date.
				return new DefDate(p1.datetimeValue().add(
					0, 0, 0, p2.intValue(), 0, 0, 0.0));
			case ADD_MINUTE: //Add minutes to date.
				return new DefDate(p1.datetimeValue().add(
					0, 0, 0, 0, p2.intValue(), 0, 0.0));
			case ADD_SECOND: //Add seconds to date.
				return new DefDate(p1.datetimeValue().add(
					0, 0, 0, 0, 0, p2.intValue(), 0.0));
			case ADD_MILLIS: {//Add millisecs to date.
				long amount = p2.longValue();
				return new DefDate(p1.datetimeValue().add(0, 0, 0, 0, 0,
					(int) amount/1000, (amount%1000)/1000.0));
			}
			case ADD_NANOS: {//Add nanosecs to date.
				long amount = p2.longValue();
				return new DefDate(p1.datetimeValue().add(0, 0, 0, 0, 0,
					(int) (amount / 1000000000L),
					(amount % 1000000000L)/1000000000.0));
			}
			case SET_DAY: {//Set day from date.
				SDatetime t = p1.datetimeValue();
				t.setDay(p2.intValue());
				return new DefDate(t);
			}
			case SET_MONTH: {//Set month from date.
				SDatetime t = p1.datetimeValue();
				t.setMonth(p2.intValue());
				return new DefDate(t);
			}
			case SET_YEAR: {//Set year from date.
				SDatetime t = p1.datetimeValue();
				t.setYear(p2.intValue());
				return new DefDate(t);
			}
			case SET_HOUR: {//Set hour from date.
				SDatetime t = p1.datetimeValue();
				t.setHour(p2.intValue());
				return new DefDate(t);
			}
			case SET_MINUTE: {//Set minute from date.
				SDatetime t = p1.datetimeValue();
				t.setMinute(p2.intValue());
				return new DefDate(t);
			}
			case SET_SECOND: {//Set second from date.
				SDatetime t = p1.datetimeValue();
				t.setSecond(p2.intValue());
				return new DefDate(t);
			}
			case SET_MILLIS: {//Set millisecond.
				SDatetime t = p1.datetimeValue();
				t.setMillisecond(p2.intValue());
				return new DefDate(t);
			}
			case SET_NANOS: {//Set nanosecond.
				SDatetime t = p1.datetimeValue();
				t.setNanos(p2.intValue());
				return new DefDate(t);
			}
			case SET_FRACTIONSECOND: {//Set fraction of second.
				SDatetime t = p1.datetimeValue();
				t.setFraction(p2.doubleValue());
				return new DefDate(t);
			}
			case SET_DAYTIMEMILLIS: {
				SDatetime t = p1.datetimeValue();
				t.setDaytimeInMillis(p2.intValue());
				return new DefDate(t);
			}
			case SET_ZONEOFFSET: {//shift to GMT
				SDatetime t = p1.datetimeValue();
				t.setTimeZoneOffset(p2.intValue());
				return new DefDate(t);
			}
			case SET_ZONEID: { //Set time zone name
				SDatetime t = p1.datetimeValue();
				t.setTimeZoneID(p2.stringValue());
				return new DefDate(t);
			}
			//String
			case GET_STRING_TAIL: {//tail(s,i);
				int i = p2.intValue();
				String s = p1.stringValue();
				if (s != null) {
					int j = s.length() -  i;
					if (j > 0) {
						s = s.substring(j);
					}
				}
				return new DefString(s);
			}
			case GET_SUBSTRING: {//s.substring(i);
				int i = p2.intValue();
				String s = p1.stringValue();
				return (s != null && s.length() >  i) ?
					new DefString(s.substring(i)) : new DefString("");
			}
			case CUT_STRING: {//cut(s,i);
				int i = p2.intValue();
				String s = p1.stringValue();
				return (s != null && s.length() >  i) ?
					new DefString(s.substring(0, i)) : new DefString(s);
			}
			//Report
			case REPORT_TOSTRING:
				return new DefString(
					((XDReport) p2).toString(p1.stringValue()));
			case REPORT_GETPARAM:
				return new DefString(
					((XDReport) p1).getParameter(p2.stringValue()));
			//constructors
			case NEW_NAMEDVALUE:
				return new DefNamedValue(p1.stringValue(),p2);
		}
		return null;
	}

	/** Execute external method (parameter).
	 * @param item command to be executed.
	 * @param sp stack pointer.
	 * @param stack stack.
	 * @return new value of stack pointer.
	 * @throws Exception
	 */
	static final int perform(final XCodeProcessor cp,
		final XDValue item,
		final int sp1,
		final XDValue[] stack) throws Exception {
		int sp = sp1;
		switch (item.getCode()) {
			//Bytes
			case BYTES_INSERT: {//Insert byte before
				int b = stack[sp--].intValue();
				int pos = stack[sp--].intValue();
				((DefBytes) stack[sp--]).insertBefore(pos, b);
				return sp;
			}
			case BYTES_REMOVE: {//remove byte(s)
				int size = item.getParam() == 2 ? 1 : stack[sp--].intValue();
				int pos = stack[sp--].intValue();
				((DefBytes) stack[sp--]).remove(pos, size);
				return sp;
			}
			case BYTES_SETAT: {//set byte at position
				int b = stack[sp--].intValue();
				int pos = stack[sp--].intValue();
				((DefBytes) stack[sp--]).setAt(pos, b);
				return sp;
			}
			//parse Datetime
			case PARSE_DATE: {
				String mask = null;
				if (item.getParam() == 2) {
					mask = stack[sp].stringValue();
					sp--;
				}
				String s = stack[sp].stringValue();
				s = s == null ? "" : s.trim();
				StringParser p = cp.getStringParser();
				p.setSourceBuffer(s);
				boolean parsed =
					mask == null ? p.isISO8601Datetime() : p.isDatetime(mask);
				stack[sp] = parsed && p.eos() && p.testParsedDatetime()
					? new DefDate(p.getParsedSDatetime())
					: DefNull.genNullValue(XD_DATETIME);
				return sp;
			}
			// Element
			case ELEMENT_TOSTRING: { //Get text value of the element
				boolean indent =
					item.getParam() == 2 ? stack[sp--].booleanValue() : false;
				Element el = stack[sp].getElement();
				stack[sp] = el != null
					? new DefString(KXmlUtils.nodeToString(el, indent))
					: DefNull.genNullValue(XD_ELEMENT);
				return sp;
			}
			case ELEMENT_TOCONTEXT: {
				DefElement e = new DefElement(stack[sp].getElement());
				stack[sp] = e.toContext();
				return sp;
			}
			case ELEMENT_GETATTR: { // Get attribute of the element
				String name = stack[sp--].toString();
				String uri = item.getParam() == 3
					? stack[sp--].stringValue() : null;
				Element el = stack[sp].getElement();
				if (uri == null) {
					stack[sp] = new DefString(el.getAttribute(name));
				} else {
					int i = name.indexOf(':');
					if (i > 0) {
						name = name.substring(i + 1);
					}
					stack[sp] = new DefString(el.getAttributeNS(uri, name));
				}
				return sp;
			}
			case ELEMENT_HASATTR: { // has attribute of the element
				String name = stack[sp--].toString();
				String uri =
					item.getParam() == 3 ? stack[sp--].stringValue() : null;
				Element el = stack[sp].getElement();
				if (uri == null) {
					stack[sp] = new DefBoolean(el.hasAttribute(name));
				} else {
					int i = name.indexOf(':');
					if (i > 0) {
						name = name.substring(i + 1);
					}
					stack[sp] = new DefBoolean(el.hasAttributeNS(uri, name));
				}
				return sp;
			}
			case ELEMENT_SETATTR: { // Set attribute to element
				String value = stack[sp--].stringValue();
				String name = stack[sp--].toString();
				String uri =
					item.getParam() == 4 ? stack[sp--].stringValue() : null;
				Element el = stack[sp--].getElement();
				if (uri != null) {
					if (value == null) {
						int i = name.indexOf(':');
						if (i > 0) {
							name = name.substring(i + 1);
						}
						el.removeAttributeNS(uri, name);
					} else {
						el.setAttributeNS(uri, name, value);
					}
				} else {
					if (value == null) {
						el.removeAttribute(name);
					} else {
						el.setAttribute(name, value);
					}
				}
				return sp;
			}
			//String
			case TRANSLATE_S:  // translate(s,t)
			case REPLACEFIRST_S: // replaceFirst(s,t)
			case REPLACE_S: { // replace(s,t)
				String q = stack[sp--].stringValue();
				String p = stack[sp--].stringValue();
				String s = stack[sp].stringValue();
				stack[sp] = new DefString(item.getCode() == TRANSLATE_S ?
					SUtils.translate(s,p,q) :
					item.getCode() == REPLACEFIRST_S ?
						SUtils.modifyFirst(s, p, q) :
						SUtils.modifyString(s, p, q));
				return sp;
			}
			case GET_SUBSTRING: {//s.substring(i[,j]);
				int j = stack[sp--].intValue();
				if (item.getParam() == 2) {//s.substring(i)
					String s = stack[sp].stringValue();
					stack[sp] = new DefString(s != null && s.length() >  j
						? s.substring(j) : "");
				} else {
					int i = stack[sp--].intValue();
					String s = stack[sp].stringValue();
					stack[sp] = new DefString(s != null && s.length() >  i
						? s.substring(i, j) : "");
				}
				return sp;
			}
			case GET_INDEXOFSTRING:
			case GET_LASTINDEXOFSTRING: {
				int ndx;
				if (item.getParam() == 2) {//s.indexOf(s)
					String s = stack[sp--].stringValue();
					String t = stack[sp].stringValue();
					ndx = item.getCode() == GET_INDEXOFSTRING
						? t.indexOf(s) : t.lastIndexOf(s);
				} else {//s.indexOf(s, pos)
					int i = stack[sp--].intValue();
					String s = stack[sp--].stringValue();
					String t = stack[sp].stringValue();
					ndx = item.getCode() == GET_INDEXOFSTRING
						? t.indexOf(s, i) : t.lastIndexOf(s, i);
				}
				stack[sp] = new DefLong(ndx);
				return sp;
			}
			//Report
			case REPORT_SETPARAM: {
				String value = stack[sp--].stringValue();
				String name = stack[sp--].stringValue();
				XDReport x = (XDReport) stack[sp];
				stack[sp] = x.setParameter(name, value);
				return sp;
			}
			case REPORT_SETTYPE: {
				String s = stack[sp--].toString();
				stack[sp] = ((XDReport) stack[sp]).setType(
					(byte) (s == null || s.isEmpty() ? 'T' : s.charAt(0)));
				return sp;
			}
////////////////////////////////////////////////////////////////////////////////
//Constructors
////////////////////////////////////////////////////////////////////////////////
			case NEW_CONTEXT: {
				int i;
				if ((i = item.getParam()) == 0) {
					stack[++sp] = DefNull.genNullValue(XD_CONTAINER);
				} else {
					sp -= i - 1;
					stack[sp] = new DefContainer(stack, sp, sp + i - 1);
				}
				return sp;
			}
			case NEW_ELEMENT: {
				String name = stack[sp].toString();
				String uri = item.getParam()==1 ? null : stack[--sp].toString();
				Element el =
					KXmlUtils.newDocument(uri, name, null).getDocumentElement();
				if (uri != null) {
					String nsAttr = "xmlns";
					int i = name.indexOf(':');
					if (i > 0) {
						nsAttr += ':' + name.substring(0, i);
					}
					el.setAttributeNS(
						XMLConstants.XMLNS_ATTRIBUTE_NS_URI, nsAttr, uri);
				}
				stack[sp] = new DefElement(el);
				return sp;
			}
			case NEW_BYTES:
				if (item.getParam() == 0) {
					stack[++sp] = new DefBytes(new byte[0]);
				} else {
					stack[sp] =
						new DefBytes(new byte[stack[sp].intValue()]);
				}
				return sp;
			case NEW_INSTREAM:
				switch (item.getParam()) {
					case 3: {
						boolean xmlFormat = stack[sp--].booleanValue();
						String s = stack[sp--].toString();
						stack[sp] = new DefInStream(
							stack[sp].toString(), s, xmlFormat);
						break;
					}
					case 2: {
						XDValue v = stack[sp--];
						if (v.getItemId() == XD_BOOLEAN) {
							stack[sp] = new DefInStream(stack[sp].toString(),
								v.booleanValue());

						} else {
							stack[sp] =	new DefInStream(stack[sp].toString(),
								v.toString(), false);
						}
						break;
					}
					default:
						stack[sp] =
							new DefInStream(stack[sp].toString(), false);
						break;
				}
				return sp;
			case NEW_OUTSTREAM: {
				switch (item.getParam()) {
					case 3: {
						boolean xmlFormat = stack[sp--].booleanValue();
						String s = stack[sp--].toString();
						stack[sp] = new DefOutStream(
							stack[sp].toString(),s, xmlFormat);
						break;
					}
					case 2: {
						String s = stack[sp--].toString();
						stack[sp] = new DefOutStream(stack[sp].toString(), s);
						break;
					}
					default:
						stack[sp] = new DefOutStream(stack[sp].toString());
						break;
				}
				return sp;
			}
			case NEW_BNFGRAMAR: {
				int extndx;
				DefBNFGrammar y;
				String s;
				if (item.getParam() == 1) {
					extndx = -1;
					y = null;
					s = stack[sp].toString();
				} else {
					extndx = 0;
					y = (DefBNFGrammar) stack[sp--];
					s = stack[sp].toString();
				}
				try {
					//we MUST recompile this with actual data!!!
					DefBNFGrammar x = new DefBNFGrammar(y,
						extndx, new SBuffer(s), null);
					x.setCode(LD_CONST_I); //However, we do it just first time!
					stack[sp] = x;
				} catch (SRuntimeException ex) {
					cp.getTemporaryReporter().putReport(ex.getReport());
				}
				return sp;
			}
			case NEW_SERVICE: {
				String passw = stack[sp--].stringValue();
				String user = stack[sp--].stringValue();
				String url = stack[sp--].stringValue();
				String service = stack[sp].stringValue();
				XDService c;
				if ("JDBC".equalsIgnoreCase(service)) {
					c = new DefSQLService(url, user, passw);
					c.setProperty("autocomit", "yes");
					stack[sp] = c;
				} else {
					throw new SRuntimeException("Unknown service: " + service);
				}
				return sp;
			}
			case NEW_XMLWRITER: {
				boolean writehdr =
					item.getParam() == 3 ? stack[sp--].booleanValue() : true;
				String encoding =
					item.getParam() >= 2 ? stack[sp--].stringValue() : null;
				String name = stack[sp].stringValue();
				stack[sp] = new DefXmlWriter(name, encoding, writehdr);
				return sp;
			}
			case NEW_REPORT: {
				int numPar = item.getParam();
				if (numPar == 1) {
					stack[sp] = new DefReport(stack[sp].toString());
				} else {
					String modification =
						numPar == 3 ? stack[sp--].stringValue() : null;
					String text = stack[sp--].stringValue();
					String id = stack[sp].stringValue();
					stack[sp] =
						new DefReport(Report.text(id, text, modification));
				}
				return sp;
			}
			case NEW_LOCALE: {
				int numPar = item.getParam();
				if (numPar == 1) {
					stack[sp] =
						new DefLocale(stack[sp].toString().toLowerCase());
				} else if (numPar == 2) {
					stack[sp - 1] =
						new DefLocale(stack[sp-1].toString().toLowerCase(),
						stack[sp].toString().toUpperCase());
					sp--;
				} else {
					stack[sp - 2] =
						new DefLocale(stack[sp-2].toString().toLowerCase(),
						stack[sp-1].toString().toUpperCase(),
						stack[sp].toString());
					sp -= 2;
				}
				return sp;
			}
		}
		return sp;
	}

	/** Execute command.
	 * @param cmd command to be executed.
	 * @param xNode actually processed node.
	 * @param sp stack pointer.
	 * @param stack stack.
	 * @param pc program counter.
	 * @param reporter reporter where to log errors.
	 * @return new value of stack pointer.
	 * @throws Exception
	 */
	static final int performX(final XCodeProcessor cp,
		final XDValue cmd,
		final ChkNode chkNode,
		final int sp1,
		final XDValue[] stack,
		final int pc) throws Exception {
		int sp = sp1;
		switch (cmd.getCode()) {
			// Parsing
			case PARSE_INT: {
				String s = stack[sp].stringValue();
				s = s == null ? "" : s.trim();
				StringParser p = cp.getStringParser();
				p.setSourceBuffer(s);
				stack[sp] = p.isSignedInteger() && p.eos()
					? new DefLong(p.getParsedLong())
					: DefNull.genNullValue(XD_INT);
				return sp;
			}
			case PARSE_FLOAT: {
				String s = stack[sp].stringValue();
				s = s == null ? "" : s.trim();
				StringParser p = cp.getStringParser();
				p.setSourceBuffer(s);
				stack[sp] = p.isSignedFloat() && p.eos()
					? new DefDouble(p.getParsedDouble())
					: DefNull.genNullValue(XD_FLOAT);
				return sp;
			}
			case GET_PARSED_BOOLEAN:
			case GET_PARSED_BYTES:
			case GET_PARSED_DECIMAL:
			case GET_PARSED_LONG:
			case GET_PARSED_DOUBLE:
			case GET_PARSED_DATETIME:
			case GET_PARSED_DURATION: {
				XDParseResult pr = (cmd.getParam() == 1)
					? (XDParseResult) stack[sp--] : chkNode._parseResult;
				XDValue val = pr.getParsedValue();
				if (val == null) {
					val = new DefNull();
				}
				stack[++sp] = val;
				return sp;
			}
////////////////////////////////////////////////////////////////////////////////
			case GET_NS: //getNamespaceURI()
				if (cmd.getParam() == 0) {
					String s = chkNode.getElement().getNamespaceURI();
					if (s == null) {
						s = "";
					}
					 stack[++sp] = new DefString(s);
				} else {
					Element el;
					String prefix;
					if (cmd.getParam() == 1) {
						if (stack[sp].getItemId() == XD_ELEMENT) {
							el = ((DefElement) stack[sp]).getElement();
							String s = el == null ? "" : el.getNamespaceURI();
							if (s == null) {
								s = "";
							}
							 stack[sp] = new DefString(s);
							return sp;
						} else {
							prefix = stack[sp].toString();
							el = chkNode.getElement();
						}
					} else {
						prefix = stack[sp].toString();
						el = ((DefElement) stack[--sp]).getElement();
					}
					stack[sp] = new DefString(XExtUtils.getNSUri(prefix, el));
				}
				return sp;
			case GET_QNAMEURI: {//getQnameURI(s[,e])
				String prefix = stack[sp].isNull()? "" : stack[sp].toString();
				Element el = cmd.getParam() == 1 ? chkNode.getElement()
					: ((DefElement) stack[--sp]).getElement();
				int ndx;
				prefix = (ndx = prefix.indexOf(':')) <= 0 ?
					"" : prefix.substring(0, ndx);
				stack[sp] = new DefString(XExtUtils.getNSUri(prefix, el));
				return sp;
			}
			case WRITE_ELEMENT_START: { // Write element start.
				Element el = cmd.getParam() == 2 ?
					stack[sp--].getElement() : chkNode.getElement();
				((XDXmlOutStream) stack[sp--]).writeElementStart(el);
				return sp;
			}
			case WRITE_ELEMENT_END: {// Write element end tag.
				if (cmd.getParam() == 2) {
					sp--;
				}
				((XDXmlOutStream) stack[sp--]).writeElementEnd();
				return sp;
			}
			case WRITE_ELEMENT: {// Write element.
				Element el = cmd.getParam() == 2 ?
					stack[sp--].getElement() : chkNode.getElement();
				((XDXmlOutStream) stack[sp--]).writeNode(el);
				return sp;
			}
			case GET_LASTERROR: {
				Report rep = cp.getTemporaryReporter().getLastErrorReport();
				if (rep == null) {
					rep = chkNode.getReportWriter().getLastErrorReport();
				}
				stack[++sp] = new DefReport(rep);
				return sp;
			}
			case IS_CREATEMODE: {
				stack[++sp] =
					new DefBoolean(chkNode.getXDDocument().isCreateMode());
				return sp;
			}
////////////////////////////////////////////////////////////////////////////////
//Constructors
////////////////////////////////////////////////////////////////////////////////
			case NEW_EXCEPTION: {
				Report rep;
				switch (cmd.getParam()) {
					case 1:
						rep = Report.error(null, stack[sp].toString());
						break;
					case 2:
						sp--;
						rep = Report.error(
							stack[sp].toString(), stack[sp].toString());
						break;
					default:
						//if (item.getParam() == 3)
						sp -= 2;
						rep = Report.error(stack[sp].toString(),
							stack[sp + 1].toString(),
							stack[sp + 2].toString());
						break;
				}
				stack[sp] = new DefException(rep,
					chkNode != null ? chkNode.getXPos() : null, pc);
				return sp;
			}
////////////////////////////////////////////////////////////////////////////////
// External methods
////////////////////////////////////////////////////////////////////////////////
			case EXTMETHOD:
			case EXTMETHOD_CHKEL:
			case EXTMETHOD_ARRAY:
			case EXTMETHOD_CHKEL_ARRAY:
			case EXTMETHOD_XXNODE:
			case EXTMETHOD_XDARRAY:
			case EXTMETHOD_CHKEL_XDARRAY:
			case EXTMETHOD_XXNODE_XDARRAY:
			case EXTMETHOD_XXELEM: {
				int paramCount = cmd.getParam();
				short code = cmd.getCode();
				Object[] pars;
				XDValue[] parlist;
				CodeExtMethod dm = (CodeExtMethod) cmd;
				Method m = dm.getExtMethod();
				if (code == EXTMETHOD ||
					code == EXTMETHOD_CHKEL ||
					code == EXTMETHOD_XXNODE ||
					code == EXTMETHOD_ARRAY ||
					code == EXTMETHOD_XDARRAY ||
					code == EXTMETHOD_CHKEL_XDARRAY ||
					code == EXTMETHOD_XXNODE_XDARRAY ||
					code == EXTMETHOD_CHKEL_ARRAY ||
					code == EXTMETHOD_XXELEM) {
					if (paramCount == 0) {
						switch (code) {
							case EXTMETHOD_CHKEL_XDARRAY:
								pars = new Object[] {chkNode, new XDValue[0]};
								break;
							case EXTMETHOD_XXNODE_XDARRAY:
								pars = new Object[] {
									(XXNode) chkNode, new XDValue[0]};
								break;
							case EXTMETHOD_XDARRAY:
								pars = new Object[] {new XDValue[0]};
								break;
							case EXTMETHOD_CHKEL:
								pars = new Object[] {chkNode};
								break;
							case EXTMETHOD_XXNODE:
								pars = new Object[] {(XXNode) chkNode};
								break;
							case EXTMETHOD_XXELEM:
								pars = new Object[] {(XXElement) chkNode};
								break;
							default:
								pars = new Object[0];
						}
					} else {
						int k;
						if (code == EXTMETHOD_CHKEL ||
							code == EXTMETHOD_XXNODE ||
							code == EXTMETHOD_XXNODE_XDARRAY ||
							code == EXTMETHOD_CHKEL_XDARRAY ||
							code == EXTMETHOD_CHKEL_ARRAY) {
							pars = new Object[paramCount + 1];
							pars[0] = chkNode;
							if (code == EXTMETHOD_XXNODE ||
								code == EXTMETHOD_XXNODE_XDARRAY) {
								pars[0] = (XXNode) chkNode;
							}
							k = 1;
						} else {
							pars = new Object[paramCount];
							k = 0;
						}
						if (code == EXTMETHOD ||
							code == EXTMETHOD_CHKEL ||
							code == EXTMETHOD_XXELEM ||
							code == EXTMETHOD_XXNODE) {
							Class<?>[] p = m.getParameterTypes();
							for (int i = sp - paramCount + 1, j = 0;
								i <= sp; i++, j++) {
								if (p[j + k].equals(XDValue.class)) {
									pars[j + k] = stack[i];
									continue;
								}
								switch (stack[i].getItemId()) {
									case XD_DECIMAL:
										pars[j + k] = stack[i].decimalValue();
										break;
									case XD_INT: {
										Class<?> x;
										if ((x = p[j+k]).equals(Long.TYPE) ||
											x.equals(Long.class)) {
											pars[j+k] = stack[i].longValue();
										} else if (x.equals(Integer.TYPE)
											|| x.equals(Integer.class)){
											pars[j+k] = stack[i].intValue();
										} else if (x.equals(Short.TYPE)
											|| x.equals(Short.class)){
											pars[j+k] = stack[i].shortValue();
										} else if (x.equals(Byte.TYPE) ||
											x.equals(Byte.class)){
											pars[j+k] = stack[i].byteValue();
										}
										break;
									}
									case XD_FLOAT:
										Class<?> x;
										if ((x=p[j+k]).equals(Double.TYPE)
											|| x.equals(Double.class)) {
											pars[j+k] = stack[i].doubleValue();
										} else if (x.equals(Float.TYPE)
											|| x.equals(Float.class)) {
											pars[j+k] = stack[i].floatValue();
										}
										break;
									case XD_BOOLEAN:
										pars[j + k] = stack[i].booleanValue() ?
											Boolean.TRUE : Boolean.FALSE;
										break;
									case XD_STRING:
										pars[j + k]= stack[i].stringValue();
										break;
									case XD_DATETIME:
										pars[j + k] = stack[i].datetimeValue();
										break;
									case XD_DURATION:
										pars[j + k] = stack[i].durationValue();
										break;
									case XD_ELEMENT:
										pars[j + k] = stack[i].getElement();
										break;
									case XD_CONTAINER:
										pars[j + k] = stack[i];
										break;
									case XD_BYTES:
										pars[j + k] = stack[i].getBytes();
										break;
									case XD_XPATH:
										pars[j + k] = stack[i].stringValue();
										break;
									case XD_REGEX:
									case XD_REGEXRESULT:
									case XD_INPUT:
									case XD_OUTPUT:
									case XD_RESULTSET:
									case XD_STATEMENT:
									case XD_SERVICE:
									case XX_ELEMENT:
									case XD_PARSERESULT:
									case XD_ANY:
									case XD_OBJECT:
									case XX_ATTR:
									case XX_TEXT:
									case XD_PARSER:
										pars[j + k] = stack[i];
										break;
									default:
										//Internal error: &{0}
										throw new SError(XDEF.XDEF309,
											"Undefined type on PC=" +
											(pc - 1) + "; " +
											cmd.getClass().getName() +
											"; code= " + code);
								}
							}
						} else {
							parlist = new XDValue[paramCount];
							System.arraycopy(stack,
								sp - paramCount + 1, parlist, 0, paramCount);
							switch (code) {
								case EXTMETHOD_CHKEL_XDARRAY:
								case EXTMETHOD_CHKEL_ARRAY:
									pars = new Object[] {chkNode, parlist};
									break;
								case EXTMETHOD_XXNODE_XDARRAY:
									pars=new Object[]{(XXNode)chkNode, parlist};
									break;
								default:
									//EXTERNAL_METHOD_ARRAY_CODE
									pars = new Object[]	{parlist};
									break;
							}
						}
						sp -= paramCount;
					}
					Object o = m.invoke(null, pars);
					short type = dm.getItemId();
					if (o == null) {
						stack[++sp] = DefNull.genNullValue(type);
					} else if (o instanceof XDValue) {
						if (type != XD_VOID) {
							XDValue x = (XDValue) o;
							if (type == x.getItemId()) {
								stack[++sp] = x;
							} else {
								switch (dm.getItemId()) {
									case XD_VOID:
										break;
									case XD_DECIMAL:
										stack[++sp] = new DefDecimal(
											(BigDecimal) o);
										break;
									case XD_INT:
										stack[++sp] =
											new DefLong(x.longValue());
										break;
									case XD_FLOAT:
										stack[++sp] =
											new DefDouble(x.doubleValue());
										break;
									case XD_BOOLEAN:
										stack[++sp] =
											new DefBoolean(x.booleanValue());
										break;
									case XD_STRING:
										stack[++sp] =
											new DefString(x.stringValue());
										break;
									default:
										stack[++sp] = x;
										break;
								}
							}
						}
					} else {
						switch (dm.getItemId()) {
							case XD_VOID:
								break;
							case XD_DECIMAL:
								stack[++sp] = new DefDecimal((BigDecimal) o);
								break;
							case XD_INT:
								stack[++sp] =
									new DefLong(((Number) o).longValue());
								break;
							case XD_FLOAT:
								stack[++sp] =
									new DefDouble(((Number) o).doubleValue());
								break;
							case XD_BOOLEAN:
								stack[++sp] = new DefBoolean(((Boolean) o));
								break;
							case XD_STRING:
								stack[++sp] = new DefString((String) o);
								break;
							case XD_DATETIME: {
								stack[++sp] = new DefDate((SDatetime) o);
								break;
							}
							case XD_DURATION: {
								stack[++sp] = new DefDuration((SDuration) o);
								break;
							}
							case XD_ELEMENT:
								stack[++sp] = new DefElement((Element) o);
								break;
							case XD_CONTAINER:
								stack[++sp] = (DefContainer) o;
								break;
							case XD_BYTES:
								stack[++sp] = new DefBytes((byte[]) o);
								break;
							case XD_OBJECT:
								stack[++sp] = (DefObject) o;
								break;
							case XD_PARSER:
								stack[++sp] = (XDParser) o;
								break;
							case XD_PARSERESULT:
								stack[++sp] = (XDParseResult) o;
								break;
							case XD_ANY:
								stack[++sp] = (XDValue) o;
								break;
							default:
								//Internal error: &{0}
								throw new SError(XDEF.XDEF309,
									"Undefined result type on PC = "+
									(pc - 1) + "; " +
									cmd.getClass().getName() + "; code= " +
									code + "; type= " + dm.getItemId());
						}
					}
					return sp;
				}
				if (paramCount != 0) {
					parlist = new XDValue[paramCount];
					sp -= paramCount;
					System.arraycopy(stack, sp + 1, parlist, 0, paramCount);
				}
				return sp;
			}
			default:
				//Internal error: &{0}
				throw new XXException(XDEF.XDEF309,
					"Undefined code on PC = " + (pc - 1) + "; " +
					cmd.toString() + "; code=" +
					CodeDisplay.getCodeName(cmd.getCode()));
		}
	}
}