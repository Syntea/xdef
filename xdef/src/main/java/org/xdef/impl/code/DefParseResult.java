package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import java.math.BigDecimal;
import static org.xdef.XDValueID.XD_PARSERESULT;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.PARSERESULT;
import org.xdef.msg.SYS;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SRuntimeException;

/** DefParseResult contains the source and results of parsing.
 * @author Vaclav Trojan
 */
public final class DefParseResult extends XDValueAbstract implements XDParseResult {
	/** Actual index to source buffer. */
	private int _pos;
	/** Parsed source string (null if parsed result failed). */
	private String _src;
	/** Parsed result object. */
	private Object _value;
	/** Messages reported by parser or null. */
	private ArrayReporter _ar;

	/** Creates a new empty instance of DefParseResult. */
	public DefParseResult() {}

	/** Creates a new instance of DefParseResult, both source and value are set initialized with source.
	 * @param source the source string.
	 */
	public DefParseResult(final String source) {_src = source;}

	/** Creates a new instance of DefParseResult.
	 * @param parsedString parsed string.
	 * @param parsedObject  parsed object.
	 */
	public DefParseResult(final String parsedString, final XDValue parsedObject) {
		_src = parsedString;
		_value = parsedObject;
	}

////////////////////////////////////////////////////////////////////////////////
//  Implementation of XDParseResult interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public final void replaceParsedBufferFrom(final int from, final String s) {
		_src = _src.substring(0, from) + s + _src.substring(_pos);
		_pos = from + s.length();
	}
////////////////////////////////////////////////////////////////////////////////
//  Implementation of SParser interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public final int endPos() {return _src == null ? 0 : _src.length();}
	@Override
	public final String getSourceBuffer() {return _src;}
	@Override
	public final void setSourceBuffer(final String source) {_src = source;}
	@Override
	public final void setParsedValue(final String s) {_value = s;}
	@Override
	public final void setParsedValue(final XDValue obj) {_value = obj;}
	@Override
	public final XDValue getParsedValue() {
		return errors() ? (XDValue) (_value = null) : _value == null
			? (XDValue)(_value = new DefString(_src)) : _value instanceof String
			? (XDValue)(_value = new DefString((String) _value)) : (XDValue)_value;
	}
	@Override
	public final String getParsedString() {return _src.substring(0, _pos);}
	/** Get value of parsed integer.
	 * @return the parsed integer.
	 * @throws SRuntimeException SYS072 Data error
	 */
	public final int getParsedInt() {
		try {
			return Integer.parseInt(_src.charAt(0) == '+' ? _src.substring(1,_pos) : _src.substring(0,_pos));
		} catch(NumberFormatException ex) {
			throw new SRuntimeException(SYS.SYS072, ex); //Data error&{0}{: }
		}
	}
	@Override
	public final String getUnparsedBufferPart() {
		if (_src != null && _pos < _src.length()) {
			String result = _src.substring(_pos);
			_pos = _src.length();
			return result;
		}
		return "";
	}
	@Override
	public final String getParsedBufferPartFrom(final int pos) {
		return (pos < _pos && _src != null && _pos <= _src.length()) ? _src.substring(pos, _pos) : "";
	}
	@Override
	public final String getBufferPart(final int from, final int to) {
		return (_src != null && from < _pos && _pos <= _src.length()) ? _src.substring(from, to): "";
	}
	@Override
	public final ArrayReporter getReporter() {return _ar;}
	@Override
	public final int getIndex() {return _pos;}
	@Override
	public final void setIndex(final int index) {_pos = index;}
	@Override
	public final boolean isSpace() {
		char c;
		if (_pos < _src.length() && ((c= _src.charAt(_pos)) == ' ' || c == '\n' || c == '\r' || c == '\t')) {
			_pos++;
			return true;
		}
		return false;
	}
	@Override
	public final boolean isSpaces() {
		char c;
		if (_pos < _src.length() && ((c = _src.charAt(_pos)) == ' ' || c == '\n' || c == '\r' || c == '\t')) {
			_pos++;
			while (_pos < _src.length() && ((c = _src.charAt(_pos)) == ' '
				|| c == '\n' || c == '\r' || c == '\t')) {
				_pos++;
			}
			return true;
		}
		return false;
	}
	@Override
	public final boolean isChar(final char ch) {
		if (_pos >= _src.length() || ch != _src.charAt(_pos)) {
			return false;
		}
		_pos++;
		return true;
	}
	@Override
	public final char notChar(final char ch) {
		char c;
		if (_pos >= _src.length() || ch==(c=_src.charAt(_pos))) {
			return NOCHAR;
		}
		_pos++;
		return c;
	}
	@Override
	public final char isOneOfChars(final String chars) {
		if (_pos >= _src.length() || (chars.indexOf(_src.charAt(_pos))) < 0) {
			return NOCHAR;
		}
		return _src.charAt(_pos++);
	}
	@Override
	public final char isUpperCaseLetter() {
		char c;
		if (_pos < _src.length()) {
			if (Character.isLetter(c = _src.charAt(_pos)) && c == Character.toUpperCase(c)) {
				char d = c;
				return d;
			}
		}
		return NOCHAR;
	}
	@Override
	public final char isLowerCaseLetter() {
		char c;
		if (_pos < _src.length()) {
			if (Character.isLetter(c=_src.charAt(_pos)) && c==Character.toLowerCase(c)) {
				char d = c;
				return d;
			}
		}
		return NOCHAR;
	}
	@Override
	public final char isInInterval(final char minCh, final char maxCh) {
		char c;
		if (_pos >= _src.length() || (c=_src.charAt(_pos)) < minCh || c > maxCh) {
			return NOCHAR;
		}
		_pos++;
		return c;
	}
	@Override
	public final char notInInterval(final char minCh, final char maxCh) {
		char c;
		if (_pos >= _src.length() || (c=_src.charAt(_pos)) >= minCh || c <= maxCh) {
			return NOCHAR;
		}
		_pos++;
		return c;
	}
	@Override
	public final int isDigit() {
		char c;
		if (_pos>=_src.length() || (c=_src.charAt(_pos))<'0' || c>'9') {
			return -1;
		}
		_pos++;
		return c - '0';
	}
	@Override
	public final boolean isInteger() {
		char c;
		if (_pos >= _src.length() || (c=_src.charAt(_pos))<'0' || c>'9') {
			return false;
		}
		while (++_pos < _src.length() && (c=_src.charAt(_pos)) >= '0' && c <= '9'){}
		return true;
	}
	@Override
	public final boolean isSignedInteger() {
		int pos = _pos;
		isOneOfChars("+-");
		if (!isInteger()) {
			_pos = pos;
			return false;
		}
		return true;
	}
	@Override
	public final boolean isFloat() {
		//(\+|-)?([0-9]+(\.[0-9]*)?|\.[0-9]+)([Ee](\+|-)?[0-9]+)?
		int pos = _pos;
		if (!isInteger() && (eos() || getCurrentChar() != '.')) {
			return false;
		}
		if (isChar('.')) {
			isInteger();
		}
		if (isOneOfChars("eE") != 0) {
			isOneOfChars("+-");
			if (!isInteger()) {
				_pos = pos;
				return false;
			}
		}
		return true;
	}
	@Override
	public final boolean isSignedFloat() {
		int pos = _pos;
		if (isOneOfChars("+-") != 2 && isFloat()) {
			return true;
		}
		_pos = pos;
		return false;
	}
	@Override
	public final char isLetter() {
		char c;
		if (_pos >= _src.length() || !Character.isLetter(c = _src.charAt(_pos))) {
			return NOCHAR;
		}
		_pos++;
		return c;
	}
	@Override
	public final char isLetterOrDigit() {
		char c;
		if (_pos >= _src.length() || !Character.isLetterOrDigit(c = _src.charAt(_pos))) {
			return NOCHAR;
		}
		_pos++;
		return c;
	}
	@Override
	public final char peekChar() {return _pos >= _src.length()? NOCHAR: _src.charAt(_pos++);}
	@Override
	public final char getCurrentChar() {return _pos < _src.length() ? _src.charAt(_pos) : NOCHAR;}
	@Override
	public final char nextChar() {return _pos < _src.length()?_src.charAt(_pos++):NOCHAR;}
	@Override
	public final boolean isToken(final String s) {
		if (_pos >= _src.length() || !_src.startsWith(s, _pos)) {
			return false;
		}
		_pos += s.length();
		return true;
	}
	@Override
	public final int isOneOfTokens(final String... tokens) {
		int result = -1, len = -1;
		for (int i = 0; i < tokens.length; i++) {
			if (_src.startsWith(tokens[i], _pos)) {
				if (tokens[i].length() > len) {
					result = i;
					len = tokens[i].length();
				}
			}
		}
		if (result != -1) {
			_pos += len;
		}
		return result;
	}
	@Override
	public final int isOneOfTokensIgnoreCase(final String... tokens) {
		int result = -1, len = -1;
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			int tlen = token.length();
			if (_pos + tlen < _src.length() && token.equalsIgnoreCase(_src.substring(_pos, _pos + tlen))) {
				if (tlen > len) {
					result = i;
					len = tlen;
				}
			}
		}
		if (result != -1) {
			_pos += len;
		}
		return result;
	}
	@Override
	public final boolean isTokenIgnoreCase(final String token) {
		int len = token.length();
		if (_pos + len <= _src.length() && token.equalsIgnoreCase(_src.substring(_pos, _pos + len))) {
			_pos+= len;
			return true;
		}
		return false;
	}
	@Override
	public final String nextToken() {
		if (_pos >= _src.length()) {
			return null;
		}
		int start = _pos;
		char c;
		while ((c = _src.charAt(_pos)) != ' ' && c != '\n' && c != '\t' && c != '\r') {
			if (++_pos >= _src.length()) {
				break;
			}
		}
		if (start == _pos) {
			return null;
		}
		return _src.substring(start, _pos);
	}
	@Override
	public final boolean findChar(final char c) {
		if (_pos < _src.length() && _src.charAt(_pos) == c) {
			return true;
		}
		while (++_pos < _src.length()) {
			if ((_src.charAt(_pos)) == c) {
				return true;
			}
		}
		return false;
	}
	@Override
	public final char findOneOfChars(final String chars) {
		while (_pos < _src.length()) {
			char c;
			if (chars.indexOf(c = _src.charAt(_pos)) >= 0) {
				return c;
			}
			_pos++;
		}
		return NOCHAR;
	}
	@Override
	public final boolean findToken(final String token) {
		int len = token.length();
		char c = token.charAt(0);
		while (_pos + len <= _src.length()) {
			for (int j = _pos; j + len <= _src.length(); j++) {
			id:
				if (_src.charAt(j) == c) {
					for (int i = 1; i < len; i++) {
						if (_src.charAt(i+j) != token.charAt(i)) {
							break id;
						}
					}
					_pos = j;
					return true;
				}
			}
			nextChar();
		}
		_pos = _src.length();
		return false;
	}
	@Override
	public final boolean eos() {return _src == null ? true : _pos >=_src.length();}
	@Override
	public final void setEos() {_pos=_src == null ? -1: _src.length();}
	@Override
	public final boolean errors() {return !matches();}
	@Override
	public final boolean matches() {
		if (_src != null && (_ar == null || !_ar.errors())) {
			return true;
		}
		if (_ar == null) {
			putDefaultParseError(); //XDF515 Value error&{0}{ :}
		}
		return false;
	}
	@Override
	public final void error(final String id) {error(id, null);}
	@Override
	public final void error(final String id, final String msg, final Object... mod) {
		putReport(Report.error(id, msg, mod));
	}
	@Override
	public final void error(final long ID, final Object... mod) {putReport(Report.error(ID, mod));}
	@Override
	public final void putReport(final Report report) {
		if (_ar == null) {
			_ar = new ArrayReporter();
		}
		_ar.putReport(report);
	}
	@Override
	/** Put the registered report object with type ERROR and add the last parameter containing the string
	 * from the ParseResult object.
	 * @param registeredID registered report id.
	 * @param mod modification string of report text.
	 */
	public final void errorWithString(final long registeredID, final Object... mod) {
		int len = mod == null ? 1 : mod.length + 1;
		Object[] modpars = new Object[len];
		if (len > 1) {
			System.arraycopy(mod, 0, modpars, 0, len - 1);
		}
		modpars[len-1] = _src == null ? "null"
			: (_src.length() > 32 ? _src.substring(0,24)+" ... " + _src.substring(_src.length() - 3): _src);
		error(registeredID, modpars);
	}
	@Override
	/** Put default parse error message (XDEF515). */
	public final void putDefaultParseError() {errorWithString(XDEF.XDEF515);} //Value error&{0}{: }
	@Override
	public final void addReports(final ArrayReporter reporter) {
		if (reporter != null && reporter.errors()) {
			if (_ar == null) {
				_ar = new ArrayReporter();
			}
			_ar.addAll(reporter);
			reporter.clear();
		}
	}
	@Override
	public final boolean booleanValue() {return _value == null ? false : ((XDValue)_value).booleanValue();}
	@Override
	public final byte byteValue() {return _value == null ? 0 : ((XDValue)_value).byteValue();}
	@Override
	public final short shortValue() {return _value == null ? 0 : ((XDValue)_value).shortValue();}
	@Override
	public final int intValue() {return _value == null ? 0 : ((XDValue)_value).intValue();}
	@Override
	public final long longValue() {return _value == null ? 0 : ((XDValue)_value).longValue();}
	@Override
	public final float floatValue() {return _value == null ? 0 : ((XDValue)_value).floatValue();}
	@Override
	public final double doubleValue() {return _value == null ? 0 : ((XDValue)_value).doubleValue();}
	@Override
	public final BigDecimal decimalValue() {return _value==null ?null :((XDValue)_value).decimalValue();}
	@Override
	public final String stringValue() {return _value == null ? null : ((XDValue)_value).stringValue();}
	@Override
	public final SDatetime datetimeValue() {return _value==null ?null :((XDValue)_value).datetimeValue();}
	@Override
	public final SDuration durationValue() {return _value==null ?null :((XDValue)_value).durationValue();}
////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public final short getItemId() {return XD_PARSERESULT;}
	@Override
	public final XDValueType getItemType() {return PARSERESULT;}
	@Override
	public final String toString() {return _value == null ? "" : _src;}
	@Override
	public final void clearReports() {_ar = null;}
	@Override
	public final boolean isNull() { return _value == null && _src == null; }
}