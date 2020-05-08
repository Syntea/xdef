package org.xdef.sys;

import org.xdef.XDConstants;
import org.xdef.msg.SYS;
import org.xdef.sys.RegisterReportTables.ReportTable;
import org.xdef.xml.KXmlUtils;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Provides container of a report. Report object has following fields:
 * <ul>
 * <li>- type of report (see constants)</li>
 * <li>- the identifier of the report (composed of the prefix and of the report
 * identifier)</li>
 * <li>- the primary text of report used if id was not found
 * in report table (see {@link ReportTable})or if _reportId is
 * <tt>null</tt></li>
 * <li>- modification string containing the information for setting parameters
 * of the report. The parameters have the format
 * "<tt>&amp;{parameter_name}</tt>" followed by parameter value</li>
 * <li>- the timestamp information added to report (if required)</li>
 * </ul>
 * <p>The manager of report texts is described in {@link ReportTable}). There is
 * described also the format of report files.</p>
 * <p>If the modification string is not equal to <tt>null</tt>, then it is used
 * for modification of parameters in the report text. If parameter specification
 * is followed by the sequence "{inserted_text}", the inserted_text is inserted
 * before the value of the parameter. Specification of
 * "{&amp;default text&amp;inserted_text}" may be used to specify  default text
 * and/or inserted text. After specification of the inserted text may follow the
 * specification of "(appended_text)" - in this case the appended_text is
 * appended after the value of parameter. If the parameter is not present in the
 * modification string then the whole parameter description is replaced by
 * the empty string.</p>
 * <p>Examples of source report text and modification strings:</p>
 * <pre><code><b>
 * report text: "a &amp;{0} &amp;{1} b"
 *   modification string: null
 *   result: "a   b"
 *   modification string: "&amp;{0}xy&amp;{1}z"
 *   result: "a xy z b"
 *
 * report text: "a&amp;{0}{x}b"
 *   modification string: null
 *   result: "ab"
 *   modification string: "&amp;{0}z"
 *   result: "axzb"
 *
 * report text: "a&amp;{0}{x}{y}b"
 *   modification string: null
 *   result: "ab"
 *   modification string: "&amp;{0}z"
 *   result: "axzyb"
 *
 * report text: "a&amp;{0}{&amp;u&amp;x}{y}b"
 *   modification string: null
 *   result: "aub"
 *   modification string: "&amp;{0}z"
 *   result: "axzyb"
 *
 * report text: "Error&amp;{0}{, pos=}"
 *   modification string: null
 *   result: "Error"
 *   modification string: "&amp;{0}123"
 *   result: "Error, pos= 123"
 *
 * report text: "Error&amp;{0}{&amp; on undefined position&amp; on position }"
 *   modification string: null
 *   result: "Error on undefined position"
 *   modification string: "&amp;{0}123"
 *   result: "Error on position 123"
 * </b></code></pre>
 * @author Vaclav Trojan
 */
public class Report {

	/** Manager of report tables. */
	private static final SManager MANAGER = SManager.getInstance();
	/** Text report object (byte value of the character 'S'). */
	public static final byte STRING = 'S';
	/** Text report object (byte value of the character 'T'). */
	public static final byte TEXT = 'T';
	/** Audit report object (byte value of the character 'A'). */
	public static final byte AUDIT = 'A';
	/** Message report object (byte value of the character 'M'). */
	public static final byte MESSAGE = 'M';
	/** Info report object (byte value of the character 'I'). */
	public static final byte INFO = 'I';
	/** Warning report object (byte value of the character 'W'). */
	public static final byte WARNING = 'W';
	/** Light error report object (byte value of the character 'L'). */
	public static final byte LIGHTERROR = 'L';
	/** LightError report object (byte value of the character 'E'). */
	public static final byte ERROR = 'E';
	/** Fatal error report object (byte value of the character 'F'). */
	public static final byte FATAL = 'F';
	/** Exception report object (byte value of the character 'X'). */
	public static final byte EXCEPTION = 'X';
	/** Trace report object (byte value of the character 'D'). */
	public static final byte TRACE = 'D';
	/** Kill report object (byte value of the character 'K'). */
	public static final byte KILL = 'K';
	/** Undefined report object (byte value of the character 'U'). */
	public static final byte UNDEF = 'U';
	/** Type of report (see constants above). */
	private final byte _type;
	/** Report identifier (used as link to associated item in report
	 * table). Set to <tt>null</tt> if no report ID is associated with
	 * the report (in this case the report can't be localized). */
	private final String _reportID;
	/** Primary text of report. If no report Id is specified or if
	 * the id is not found in the report table this text is used as default. */
	private String _text;
	/** String used for modification report of report text. */
	private String _modification;
	/** Timestamp of creation of report in milliseconds. Set to -1L if
	 * no time information is not relevant. */
	private long _timeMillis;

	/** Create new Report object; no time timestamp is generated.
	 * @param type kind of report.
	 * @param reportID id of report.
	 * @param text text of report.
	 * @param mod modification array.
	 */
	public Report(final byte type,
		final String reportID,
		final String text,
		final Object... mod) {
		_type = registeredType(type);
		_reportID  = reportID;
		_text = text;
		_modification = genModification(mod);
		_timeMillis = -1L;
	}

	/** Create new Report object (registered); no time timestamp is generated.
	 * @param type kind of report.
	 * @param regID registered report Id.
	 * @param mod modification string or <tt>null</tt>.
	 */
	public Report(final byte type, final long regID, final Object... mod) {
		_type = registeredType(type);
		_modification = genModification(mod);
		ReportTable t = null;
		try {
			t = MANAGER.getReportTable(regID);
		} catch (Exception ignore) {}
		if (t == null) {
			_reportID = String.valueOf(regID);
			_text = "";
		} else {
			_reportID = t.getReportID(regID);
			_text = null;
		}
		_timeMillis = -1L;
	}

	/** Return registered report type.
	 * @param type unregistered type.
	 * @return registered report type.
	 */
	private static byte registeredType(final byte type) {
		switch (type) {
			case STRING:
			case TEXT:
			case AUDIT:
			case MESSAGE:
			case INFO:
			case WARNING:
			case LIGHTERROR:
			case ERROR:
			case FATAL:
			case EXCEPTION:
			case TRACE:
			case KILL:
				return type;
		}
		return UNDEF;
	}

	/** Create new report object from an exception object.
	 * @param ex exception (i.e. Throwable).
	 */
	public Report(final Throwable ex) {
		_type = EXCEPTION;
		_reportID = (ex instanceof SThrowable) ?
			((SThrowable) ex).getMsgID() : null;
		CharArrayWriter caw = new CharArrayWriter();
		ex.printStackTrace(new PrintWriter(caw));
		_text = caw.toString();
		_timeMillis = System.currentTimeMillis();
	}

	/** Create modification string from parameter list.
	 * @param mod array of modifications.
	 * @return string with modification information.
	 */
	public static String genModification(final Object... mod) {
		if (mod != null && mod.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mod.length; i++) {
				Object o = mod[i];
				if (o != null) {
					String s =
						o instanceof String ?
						(String) o : o instanceof File ?
						((File) o).getAbsolutePath() : o.toString();
					if (s.startsWith("&{") && !s.startsWith("&{#")) {
						sb.append(s);
					} else if (s.length() > 2 && s.charAt(0) == '{'
						&& Character.isDigit(s.charAt(1)) && s.charAt(2)=='}') {
						sb.append('&').append(s);
					} else { // we create &{i} prefix
						sb.append("&{")
							.append((char) (i + '0'))
							.append('}')
							.append(s);
					}
				}
			}
			return sb.toString();
		}
		return null;
	}

	/** Create new report item from XML element.
	 * @param element element.
	 */
	public Report(final Element element) {
		String s = element.getTagName();
		byte c = (byte) s.charAt(0);
		if (s.length() != 1) {
			c = 'U';
		}
		switch (c) {
			case STRING: {
				NodeList nl = element.getChildNodes();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < nl.getLength(); i++) {
					Node item = nl.item(i);
					if (item.getNodeType() == Node.TEXT_NODE) {
						sb.append(item.getNodeValue());
					}
				}
				_text = sb.toString();
				_type = STRING;
				_reportID = null;
				_timeMillis = -1L;
				_modification = null;
				return;
			}
			case TEXT:
			case AUDIT:
			case MESSAGE:
			case INFO:
			case WARNING:
			case LIGHTERROR:
			case ERROR:
			case FATAL:
			case EXCEPTION:
			case TRACE:
			case KILL:
				_type = c;
				break;
			default:
				_type = UNDEF;
		}//switch
		String id = element.getAttribute("id");
		_reportID = id.isEmpty() ? null : id;
		if ((_text = element.getAttribute("txt")).isEmpty()) {
			_text = null;
		}
		if ((_modification = element.getAttribute("mod")).isEmpty()) {
			_modification = null;
		}
		if ((s = element.getAttribute("time")).length() > 0) {
			try {
				_timeMillis = Long.parseLong(s,16);
			} catch(Exception ex) {
				_timeMillis = System.currentTimeMillis();
			}
		} else {
			_timeMillis = -1L;
		}
	}

	@Override
	/** Get text of report in the local language defined by the system.
	 * @return report as a string.
	 */
	public String toString() {return toString(null);}

	/** Get localized text of report.
	 * @param language language code (ISO-639).
	 * @return report as a string.
	 */
	public String toString(final String language) {
		StringBuilder sb = new StringBuilder();
		switch (_type) {
			case STRING:
				return ((_text == null || _text.isEmpty()) ?
					sb : sb.append(_text)).toString();
			case TEXT:
			   break;
			case AUDIT:
			case MESSAGE:
			case INFO:
			case WARNING:
			case LIGHTERROR:
			case ERROR:
			case FATAL:
			case EXCEPTION:
			case KILL:
			case TRACE:
			   sb.append((char) _type);
				if (_reportID != null && _reportID.length() > 0) {
					sb.append(' ');
					sb.append(_reportID);
				}
				sb.append(": ");
			   break;
			default:
			   sb.append('U');
				if (_reportID != null && _reportID.length() > 0) {
					sb.append(' ');
					sb.append(_reportID);
				}
				sb.append(": ");
		}
		return sb.append(MANAGER.getLocalizedText(_reportID,
			_text, _modification, language)).toString();
	}

	/** Get type of the report.
	 * @return type of the report.
	 */
	public final byte getType() {return _type;}

	/** Get report ID.
	 * @return report ID or <tt>null</tt>.
	 */
	public final String getMsgID() {return _reportID;}

	/** Get primary text.
	 * @return the primary text or of the report <tt>null</tt>.
	 */
	public final String getText() {return _text;}

	/** Set primary text.
	 * @param text the argument will be set as primary text
	 * (may be<tt>null</tt>).
	 */
	public final void setText(String text) {_text = text;}

	/** Get timestamp in milliseconds.
	 * @return time in milliseconds or <tt>-1</tt>.
	 */
	public final long getTimestamp() {return _timeMillis;}

	/** Set actual timestamp (milliseconds). */
	public final void setTimestamp() {setTimestamp(System.currentTimeMillis());}

	/** Set timestamp in milliseconds.
	 * @param millis time in milliseconds from 1990 or <tt>-1</tt>.
	 */
	public final void setTimestamp(long millis) {_timeMillis = millis;}

	/** Get modification part of the report.
	 * @return value of the modification or <tt>null</tt>.
	 */
	public final String getModification() {return _modification;}

	/** Set modification part of the report.
	 * @param mod value of the modification or <tt>null</tt>.
	 */
	public final void setModification(String mod) {_modification = mod;}

	/** Get value of parameter from modification string.
	 * @param name parameter name.
	 * @return value of the parameter or <tt>null</tt>.
	 */
	public final String getParameter(final String name) {
		int ndx;
		if (_modification != null &&
			(ndx = _modification.indexOf("&{" + name + "}")) >= 0) {
			int ndx1 = ndx + name.length() + 3;
			int ndx2;
			if ((ndx2 = _modification.indexOf("&{", ndx1)) < 0) {
				return _modification.substring(ndx1);
			}
			while (_modification.indexOf("&{#", ndx2) == ndx2) {
				ndx2+= 3;
				int ndx3;
				if ((ndx3 = _modification.indexOf('}', ndx2+ 3)) > 0) {
					ndx2 = ndx3 + 1;
					if ((ndx2 = _modification.indexOf("&{", ndx2)) < 0) {
						return _modification.substring(ndx1);
					}
				}
			}
			return _modification.substring(ndx1, ndx2);
		}
		return null;
	}


	/** Set modification parameter.
	 * @param name name of the parameter.
	 * @param value value of the parameter. If the parameter exists, then
	 *  if this argument is null, the original value of message modification
	 * is removed or if the argument is not null the original value is replaced.
	 */
	public final void setParameter(final String name, final String value) {
		String p = name == null ? "" : name.trim();
		if (p.startsWith("&{")) {
			if (!p.endsWith("}")) {
				p = p + '}';
			}
		} else {
			p = "&{" + p + "}";
		}
		if (name.isEmpty() || "&{}".equals(name)) {
			return;
		}
		if (_modification == null) {
			if (value != null) {
				_modification = p + value;
			}
			return;
		}
		int ndx;
		if ((ndx = _modification.indexOf(p)) >= 0) {
			int ndx1 = ndx + p.length();
			int ndx2;
			if ((ndx2 = _modification.indexOf("&{", ndx1)) < 0) {//last param
				_modification = value == null ?
					ndx == 0 ? null : _modification.substring(0, ndx) :
					_modification.substring(0, ndx1) + value;
				return;
			}
			while (_modification.indexOf("&{#", ndx2) == ndx2) {//reference
				ndx2+= 3;
				int ndx3;
				if ((ndx3 = _modification.indexOf('}', ndx2+ 3)) > 0) {
					ndx2 = ndx3 + 1;
					if ((ndx2 = _modification.indexOf("&{", ndx2)) < 0) {//last
						_modification = value == null ?
							ndx == 0 ? null : _modification.substring(0, ndx) :
							_modification.substring(0, ndx1) + value;
						return;
					}
				}
			}
			if (value == null) {
				if (ndx == 0) {
					_modification = _modification.substring(ndx2 + 1);
				} else {
					_modification = _modification.substring(0, ndx) +
						_modification.substring(ndx2);
				}
			} else {
				_modification = _modification.substring(0, ndx1) + value +
					_modification.substring(ndx2);
			}
			return;
		}
		if (value != null) {// not found, add
			_modification += p + value;
		}
	}

	/** Create localized text of report in the language specified by system.
	 * @return text of report in the language specified by system.
	 */
	public final String getLocalizedText() {
		return MANAGER.getLocalizedText(_reportID, _text, _modification, null);
	}

	/** Create localized text of report in the language specified by argument.
	 * If the text with this language is not available it will be returned the
	 * in primary language (usually English).
	 * @param language language id (ISO-639). If this argument is <tt>null</tt>
	 * then it will be used the local language from system settings).
	 * @return text of report in specified language.
	 */
	public final String getLocalizedText(final String language) {
		return MANAGER.getLocalizedText(_reportID,
			_text, _modification, language);
	}

	/** Create string with XML element from the report.
	 * @return string with XML element from the report.
	 */
	public final String toXmlString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append((char) _type);
		if (_type == STRING) {
			sb.append(">");
			sb.append(KXmlUtils.toXmlText(_text, '<', false));
			return sb.append("</S>").toString();
		}
		if (_reportID != null && _reportID.length() > 0) {
			sb.append(" id=\"");
			sb.append(_reportID);
			sb.append('"');
		}
		if (_text != null && _text.length() > 0) {
			sb.append(" txt=\"");
			sb.append(KXmlUtils.toXmlText(_text, '"', false));
			sb.append('"');
		}
		if (_modification != null) {
			sb.append(" mod=\"");
			sb.append(KXmlUtils.toXmlText(_modification, '"', false));
			sb.append('"');
		}
		if (_timeMillis != -1L) {
			sb.append(" time=\"");
			sb.append(Long.toHexString(_timeMillis));
			sb.append('"');
		}
		return sb.append("/>").toString();
	}

	/** Create XML node from the report.
	 * @param doc document in which the element is created.
	 * @return XML element with the report.
	 */
	public final Element toXmlElement(final Document doc) {
		Element el = doc.createElement(String.valueOf((char) _type));
		if (_type == STRING) {
			el.appendChild(doc.createTextNode(_text));
			return el;
		}
		if (_reportID != null) {
			el.setAttribute("id", _reportID);
		}
		if (_text != null) {
			el.setAttribute("txt", _text);
		}
		if (_modification != null) {
			el.setAttribute("mod", _modification);
		}
		if (_timeMillis != -1L) {
			el.setAttribute("time", Long.toHexString(_timeMillis));
		}
		return el;
	}

	////////////////////////////////////////////////////////////////////////////
	// Static methods
	////////////////////////////////////////////////////////////////////////////

	/** Set default language assigned to report table.
	 * @param language language code (ISO-639 two letters
	 * or ISO-639-2 three letters).
	 * @return object which may be used for synchronization purposes
	 */
	public static Object setLanguage(final String language) {
		return SManager.setLanguage(language);
	}

	/** Create new Report object with type AUDIT.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report audit(final String id,
		final String msg,
		final Object... mod) {
		return new Report(AUDIT, id, msg, mod);
	}

	/** Create new Report object with type FATAL.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report fatal(final String id,
		final String msg,
		final Object... mod) {
		return new Report(FATAL, id, msg, mod);
	}

	/** Create new Report object with type ERROR.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report error(final String id,
		final String msg,
		final Object... mod) {
		return new Report(ERROR, id, msg, mod);
	}

	/** Create new Report object with type LIGHT (light error).
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report lightError(final String id,
		final String msg,
		final Object... mod) {
		return new Report(LIGHTERROR, id, msg, mod);
	}

	/** Create new Report object with type WARNING.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report warning(final String id,
		final String msg,
		final Object... mod) {
		return new Report(WARNING, id, msg, mod);
	}

	/** Create new Report object with type MESSAGE.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report message(final String id,
		final String msg,
		final Object... mod) {
		return new Report(MESSAGE, id, msg, mod);
	}

	/** Create new Report object with type INFO.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report info(final String id,
		final String msg,
		final Object... mod) {
		return new Report(INFO, id, msg, mod);
	}

	/** Create new Report object with type STRING.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report string(final String id,
		final String msg,
		final Object... mod) {
		return new Report(STRING, id, msg, mod);
	}

	/** Create new Report object with type TEXT.
	 * @param id report id. If id is null the default text is used.
	 * @param msg Default text of report. If id is not found in report files
	 * this text is used.
	 * @param mod Message modification parameters.
	 * @return generated report.
	 */
	public static Report text(final String id,
		final String msg,
		final Object... mod) {
		return new Report(TEXT, id, msg, mod);
	}

	/** Get "raw" text of report from report table {i.e. references to other
	 * reports are not resolved).
	 * @param reportID report ID.
	 * @param language language code (ISO-639) or null (i.e. default language).
	 * @return text of report.
	 */
	public static String getRawReportText(final String reportID,
		final String language) {
		return MANAGER.getReportText(reportID, language, false);
	}

	/** Get text of report from report table. All references to other reports
	 * are resolved.
	 * @param reportID report ID.
	 * @param language language code (ISO-639) or null (i.e. default language).
	 * @return text of report.
	 */
	public static String getReportText(final String reportID,
		final String language) {
		return MANAGER.getReportText(reportID, language, true);
	}

	/** Get text of report from report table in the actual language. All
	 * references to other reports are resolved.
	 * @param reportID report ID.
	 * @return text of report.
	 */
	public static String getReportText(final String reportID) {
		return MANAGER.getReportText(reportID, null, true);
	}

	/** Get modified text of localized report (see description of this
	 * class above) .
	 * @param reportID report id.
	 * @param msgText default report text.
	 * @param modification modification string.
	 * @param language language code (ISO-639) or null (i.e. default language).
	 * @return The text of localized report in given language or <tt>null</tt>.
	 */
	public static String getLocalizedText(final String reportID,
		final String msgText,
		final String modification,
		final String language) {
		return MANAGER.getLocalizedText(
			reportID, msgText, modification, language);
	}

	/** Get sorted array of parameter names from report text.
	 * @param reportID report ID.
	 * @param language language code (ISO-639) or null (i.e. default language).
	 * @return sorted array of parameter names or <tt>null</tt>.
	 */
	public static String[] getReportParamNames(final String reportID,
		final String language) {
		return MANAGER.getReportParamNames(reportID, language);
	}

	/** Get modified text of localized registered report (see description of
	 * this class above) .
	 * @param registeredID registered report ID.
	 * @param modification modification string.
	 * @param language language code (ISO-639) or null (i.e. default language).
	 * @return text of localized report in given language or <tt>null</tt>.
	 */
	public static String getLocalizedText(final long registeredID,
		final String modification,
		final String language) {
		return MANAGER.getLocalizedText(registeredID, modification, language);
	}

	////////////////////////////////////////////////////////////////////////////
	// registered reports
	////////////////////////////////////////////////////////////////////////////

	/** Create new registered report object with type AUDIT.
	 * @param registeredID registered report id.
	 * @param mod modification string of report text.
	 * @return generated report.
	 */
	public static Report audit(final long registeredID,
		final Object... mod) {
		return new Report(AUDIT, registeredID, mod);
	}

	/** Create new registered report object with type FATAL.
	 * @param registeredID Registered report id.
	 * @param mod modification string of report text.
	 * @return generated report.
	 */
	public static Report fatal(final long registeredID,
		final Object... mod) {
		return new Report(FATAL, registeredID, mod);
	}

	/** Create new registered report object with type ERROR.
	 * @param registeredID registered report id.
	 * @param mod modification string of report text.
	 * @return generated report.
	 */
	public static Report error(final long registeredID,
		final Object... mod) {
		return new Report(ERROR, registeredID, mod);
	}

	/** Create new registered report object with type LIGHT (light error).
	 * @param registeredID Registered report id.
	 * @param mod modification parameters.
	 * @return generated report.
	 */
	public static Report lightError(final long registeredID,
		final Object... mod) {
		return new Report(LIGHTERROR, registeredID, mod);
	}

	/** Create new registered report object with type WARNING.
	 * @param registeredID Registered report id.
	 * @param mod modification parameters.
	 * @return generated report.
	 */
	public static Report warning(long registeredID,
		final Object... mod) {
		return new Report(WARNING, registeredID, mod);
	}

	/** Create new registered report object with type MESSAGE.
	 * @param registeredID Registered report id.
	 * @param mod modification parameters.
	 * @return generated report.
	 */
	public static Report message(final long registeredID, final Object... mod) {
		return new Report(MESSAGE, registeredID, mod);
	}

	/** Create new registered report object with type INFO.
	 * @param registeredID Registered report id.
	 * @param mod modification string of report text.
	 * @return generated report.
	 */
	public static Report info(final long registeredID, final Object... mod) {
		return new Report(INFO, registeredID, mod);
	}

	/** Create new registered report object with type STRING.
	 * @param registeredID Registered report id.
	 * @param mod modification string of report text.
	 * @return generated report.
	 */
	public static Report string(final long registeredID, final Object... mod) {
		return new Report(STRING, registeredID, mod);
	}

	/** Create new registered report object with type TEXT.
	 * @param registeredID Registered report id.
	 * @param mod modification parameters.
	 * @return generated report.
	 */
	public static Report text(final long registeredID, final Object... mod) {
		return new Report(TEXT, registeredID, mod);
	}

	/** Get string with the report ID created from registered ID.
	 * @param registeredID registered report ID.
	 * @return string with report ID or <tt>null</tt>.
	 */
	public static String getReportID(final long registeredID) {
		return SManager.getReportID(registeredID);
	}

	/** Get "raw" text of report from report table {i.e. references to other
	 * reports are not resolved).
	 * @param registeredID registered report ID.
	 * @param language language code.
	 * @return text of report.
	 */
	public static String getRawReportText(final long registeredID,
		final String language) {
		return MANAGER.getReportText(registeredID, language, false);
	}

	/** Get text of report from report table. All references to other reports
	 * are resolved.
	 * @param registeredID registered report ID.
	 * @param language language code.
	 * @return text of report.
	 */
	public static String getReportText(final long registeredID,
		final String language) {
		return MANAGER.getReportText(registeredID, language, true);
	}

	/** Get text of report from report table in the actual language.
	 * All references to other reports are resolved.
	 * @param registeredID registered report ID.
	 * @return text of report.
	 */
	public static String getReportText(final long registeredID) {
		return MANAGER.getReportText(registeredID, null, true);
	}

	/** Get info report with system build information.
	 * @return report with build information.
	 */
	public static Report buildInfo() {
		//Compiled: &{c}, build version: &{v}, date: &{d}
		return Report.info(SYS.SYS010,
			"&{c}" + XDConstants.JAVA_VERSION +
			"&{v}" + XDConstants.BUILD_VERSION +
			"&{d}" + XDConstants.BUILD_DATE);
	}

////////////////////////////////////////////////////////////////////////////////

	/** Writes the report to SObjectWriter.
	 * @param w SObjectWriter where to write.
	 * @throws IOException if an error occurs.
	 */
	public void writeObj(SObjectWriter w) throws IOException {
		w.writeByte(_type);
		w.writeString(_reportID);
		w.writeString(_text);
		w.writeString(_modification);
		w.writeLong(_timeMillis);
	}

	/** Read report from the SObjectReader.
	 * @param r SObjectReader where to read.
	 * @return report from the SObjectReader
	 * @throws IOException if an error occurs.
	 */
	public static Report readObj(SObjectReader r) throws IOException {
		Report x = new Report(r.readByte(),
			r.readString(),
			r.readString(),
			r.readString());
		x._timeMillis = r.readLong();
		return x;
	}

}