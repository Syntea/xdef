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
import org.xdef.XDValueType;

/** DefParseResult contains information about parsing.
 * @author Vaclav Trojan
 */
public final class DefParseResult extends XDValueAbstract
	implements XDParseResult {
	private int _srcIndex;
	/** Parsed source string (null if parsed result failed). */
	private String _source;
	/** Parsed result object. */
	private Object _value;
	/* Messages reported by parser or <tt>null</tt>. */
	private ArrayReporter _ar;

	/** Creates a new empty instance of DefParseResult. */
	public DefParseResult() {}

	/** Creates a new instance of DefParseResult, both source and value are
	 * set initialized with source.
	 * @param source the source string.
	 */
	public DefParseResult(final String source) {_source = source;}

	/** Creates a new instance of DefParseResult.
	 * @param parsedString the parsed string.
	 * @param parsedObject the parsed object.
	 */
	public DefParseResult(final String parsedString,
		final XDValue parsedObject) {
		_source = parsedString;
		_value = parsedObject;
	}

////////////////////////////////////////////////////////////////////////////////
//  Implementation of XDParseResult interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final void replaceParsedBufferFrom(final int from, final String s) {
		_source = _source.substring(0, from) + s + _source.substring(_srcIndex);
		_srcIndex = from + s.length();
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public final short getItemId() {return XD_PARSERESULT;}

	@Override
	public XDValueType getItemType() {return XDValueType.PARSERESULT;}

	@Override
	public final String toString() {return _value==null ? "" : _source;}

	@Override
	public final void clearReports() {_ar = null;}

	@Override
	public final boolean isNull() { return _value == null && _source == null; }

////////////////////////////////////////////////////////////////////////////////
//  Implementation of SimpleParser interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final String getSourceBuffer() {return _source;}

	@Override
	public final void setSourceBuffer(final String source) {_source = source;}

	@Override
	public final void setParsedValue(final String s) {_value = s;}

	@Override
	public final void setParsedValue(final XDValue obj) {_value = obj;}

	@Override
	public final XDValue getParsedValue() {
		return errors() ? (XDValue) (_value = null) :
			_value == null ? (XDValue)(_value=new DefString(_source)) :
			_value instanceof String ?
			(XDValue)(_value=new DefString((String) _value)) : (XDValue)_value;
	}

	@Override
	public String getParsedString() {return _source.substring(0, _srcIndex);}

	@Override
	public final String getUnparsedBufferPart() {
		if (_source != null && _srcIndex < _source.length()) {
			String result = _source.substring(_srcIndex);
			_srcIndex = _source.length();
			return result;
		}
		return null;
	}

	@Override
	public final String getParsedBufferPartFrom(final int pos) {
		return (pos < _srcIndex && _source != null
			&& _srcIndex <= _source.length()) ?
			_source.substring(pos, _srcIndex) : "";
	}

	@Override
	public final String getBufferPart(final int from, final int to) {
		return (_source != null && from < _srcIndex
			&& _srcIndex<=_source.length()) ? _source.substring(from, to): null;
	}

	@Override
	public final ArrayReporter getReporter() {return _ar;}

	@Override
	public final int getIndex() {return _srcIndex;}

	@Override
	public final void setBufIndex(final int index) {_srcIndex = index;}

	@Override
	public final boolean isSpace() {
		char ch;
		if (_srcIndex < _source.length()
			&& ((ch = _source.charAt(_srcIndex)) == ' '
			|| ch == '\n' || ch == '\r' || ch == '\t')) {
			_srcIndex++;
			return true;
		}
		return false;
	}

	@Override
	public final boolean isSpaces() {
		char ch;
		if (_srcIndex < _source.length()
			&& ((ch = _source.charAt(_srcIndex)) == ' '
			|| ch == '\n' || ch == '\r' || ch == '\t')) {
			_srcIndex++;
			while (_srcIndex < _source.length()
				&& ((ch=_source.charAt(_srcIndex)) == ' '
				|| ch == '\n' || ch == '\r' || ch == '\t')) {
				_srcIndex++;
			}
			return true;
		}
		return false;
	}

	@Override
	public final boolean isChar(final char ch) {
		if (_srcIndex >= _source.length() || ch != _source.charAt(_srcIndex)) {
			return false;
		}
		_srcIndex++;
		return true;
	}

	@Override
	public final char notChar(final char ch) {
		char c;
		if (_srcIndex>=_source.length() || ch==(c=_source.charAt(_srcIndex))){
			return NOCHAR;
		}
		_srcIndex++;
		return c;
	}

	@Override
	public final char isOneOfChars(String chars) {
		if (_srcIndex >= _source.length()
			|| (chars.indexOf(_source.charAt(_srcIndex))) < 0) {
			return NOCHAR;
		}
		return _source.charAt(_srcIndex++);
	}

	@Override
	public final char isUpperCaseLetter() {
		char ch;
		if (_srcIndex < _source.length()) {
			if (Character.isLetter(ch = _source.charAt(_srcIndex)) &&
				ch == Character.toUpperCase(ch)) {
				char c = ch;
				return c;
			}
		}
		return NOCHAR;
	}

	@Override
	public final char isLowerCaseLetter() {
		char ch;
		if (_srcIndex < _source.length()) {
			if (Character.isLetter(ch = _source.charAt(_srcIndex)) &&
				ch == Character.toLowerCase(ch)) {
				char c = ch;
				return c;
			}
		}
		return NOCHAR;
	}

	@Override
	public final char isInInterval(final char minCh, final char maxCh) {
		char c;
		if (_srcIndex >= _source.length() ||
			(c = _source.charAt(_srcIndex)) < minCh || c > maxCh) {
			return NOCHAR;
		}
		_srcIndex++;
		return c;
	}

	@Override
	public final char notInInterval(final char minCh, final char maxCh) {
		char c;
		if (_srcIndex >= _source.length() ||
			(c = _source.charAt(_srcIndex)) >= minCh || c <= maxCh) {
			return NOCHAR;
		}
		_srcIndex++;
		return c;
	}

	@Override
	public final int isDigit() {
		char c;
		if (_srcIndex>=_source.length()
			|| (c=_source.charAt(_srcIndex))<'0' || c>'9') {
			return -1;
		}
		_srcIndex++;
		return c - '0';
	}

	@Override
	public final boolean isInteger() {
		if (isDigit() == -1) {
			return false;
		}
		while(isDigit() != -1) {}
		return true;
	}

	@Override
	public final boolean isSignedInteger() {
		int pos = _srcIndex;
		isOneOfChars("+-");
		if (!isInteger()) {
			_srcIndex = pos;
			return false;
		}
		return true;
	}

	@Override
	public final boolean isFloat() {
//(\+|-)?([0-9]+(\.[0-9]*)?|\.[0-9]+)([Ee](\+|-)?[0-9]+)?
//|(\+|-)?INF|NaN
		if (isToken("INF") || isToken("NaN")) {
			return true;
		}
		int pos = _srcIndex;
		if (!isInteger()) {
			char c;
			if (eos() || ((c=_source.charAt(pos)) != '.' && c!='e' && c!='E')) {
				return false;
			}
		}
		if (isChar('.')) {
			isInteger();
		}
		if (isOneOfChars("eE") != 0) {
			isOneOfChars("+-");
			if (!isInteger()) {
				_srcIndex = pos;
				return false;
			}
		}
		return true;
	}

	@Override
	public final boolean isSignedFloat() {
		int pos = _srcIndex;
		isOneOfChars("+-");
		String s;
		if (!isFloat() || "+NaN".equals(s = getParsedBufferPartFrom(pos))
			|| "-NaN".equals(s) || "+INF".equals(s)) {
			_srcIndex = pos;
			return false;
		}
		return true;
	}

	@Override
	public final char isLetter() {
		char c;
		if (_srcIndex >= _source.length()
			|| !Character.isLetter(c = _source.charAt(_srcIndex))) {
			return NOCHAR;
		}
		_srcIndex++;
		return c;
	}

	@Override
	public final char isLetterOrDigit() {
		char c;
		if (_srcIndex >= _source.length()
			|| !Character.isLetterOrDigit(c = _source.charAt(_srcIndex))) {
			return NOCHAR;
		}
		_srcIndex++;
		return c;
	}

	@Override
	public final char peekChar() {
		return _srcIndex >= _source.length()
			? NOCHAR : _source.charAt(_srcIndex++);
	}

	@Override
	public final char getCurrentChar() {
		return _srcIndex < _source.length() ? _source.charAt(_srcIndex): NOCHAR;
	}

	@Override
	public final char nextChar() {
		return _srcIndex < _source.length()
			? _source.charAt(_srcIndex++) : NOCHAR;
	}

	@Override
	public final boolean isToken(final String s) {
		if (_srcIndex >= _source.length() || !_source.startsWith(s, _srcIndex)){
			return false;
		}
		_srcIndex += s.length();
		return true;
	}

	@Override
	public final int isOneOfTokens(final String... tokens) {
		int result = -1, len = -1;
		for (int i = 0; i < tokens.length; i++) {
			if (_source.startsWith(tokens[i], _srcIndex)) {
				if (tokens[i].length() > len) {
					result = i;
					len = tokens[i].length();
				}
			}
		}
		if (result != -1) {
			_srcIndex += len;
		}
		return result;
	}

	@Override
	public final boolean isTokenIgnoreCase(String token) {
		int len = token.length();
		int end = _source.length();
		if (_srcIndex + len <= end &&
			token.equalsIgnoreCase(_source.substring(_srcIndex,_srcIndex+len))){
			_srcIndex+= len;
			return true;
		}
		return false;
	}

	@Override
	public final String nextToken() {
		if (_srcIndex >= _source.length()) {
			return null;
		}
		int start = _srcIndex;
		char ch;
		while ((ch=_source.charAt(_srcIndex)) != ' '
			&& ch != '\n' && ch != '\t'&&ch!='\r') {
			if (++_srcIndex >= _source.length()) {
				break;
			}
		}
		if (start == _srcIndex) {
			return null;
		}
		return _source.substring(start, _srcIndex);
	}

	@Override
	public final boolean findChar(final char ch) {
		if (_srcIndex < _source.length() && _source.charAt(_srcIndex) == ch) {
			return true;
		}
		while (++_srcIndex < _source.length()) {
			if ((_source.charAt(_srcIndex)) == ch) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final char findOneOfChars(final String chars) {
		while (_srcIndex < _source.length()) {
			char ch;
			if (chars.indexOf(ch = _source.charAt(_srcIndex)) >= 0) {
				return ch;
			}
			_srcIndex++;
		}
		return NOCHAR;
	}

	@Override
	public final boolean findToken(final String token) {
		int len = token.length();
		char c = token.charAt(0);
		int endPos = _source.length();
		while (_srcIndex + len <= endPos) {
			for (int j = _srcIndex; j + len <= endPos; j++) {
			id:
				if (_source.charAt(j) == c) {
					for (int i = 1; i < len; i++) {
						if (_source.charAt(i+j) != token.charAt(i)) {
							break id;
						}
					}
					_srcIndex = j;
					return true;
				}
			}
			nextChar();
		}
		_srcIndex = endPos;
		return false;
	}

	@Override
	public final boolean eos() {
		return _source == null ? true : _srcIndex >=_source.length();
	}

	@Override
	public final void setEos() {_srcIndex=_source==null ? -1: _source.length();}

	@Override
	public final boolean errors() {return !matches();}

	@Override
	public final boolean matches() {
		if (_source != null && (_ar == null || !_ar.errors())) {
			return true;
		}
		if (_ar == null) {
			putDefaultParseError(); //XDF515 Value error&{0}{ :}
		}
		return false;
	}

	@Override
	public final void error(final String id,
		final String msg,
		final Object... mod) {
		if (_ar == null) {
			_ar = new ArrayReporter();
		}
		_ar.error(id, msg, mod);
	}

	@Override
	public final void error(final long registeredID, final Object... mod) {
		if (_ar == null) {
			_ar = new ArrayReporter();
		}
		_ar.error(registeredID, mod);
	}
	@Override
	public final void putReport(final Report report) {
		if (_ar == null) {
			_ar = new ArrayReporter();
		}
		_ar.putReport(report);
	}
	@Override
	/** Put the registered report object with type ERROR and add the last
	 * parameter containing the string from the ParseResult object.
	 * @param registeredID registered report id.
	 * @param mod modification string of report text.
	 */
	public void errorWithString(final long registeredID, final Object... mod) {
		int len = mod == null ? 1 : mod.length + 1;
		Object[] modpars = new Object[len];
		if (len > 1) {
			System.arraycopy(mod, 0, modpars, 0, len - 1);
		}
		modpars[len-1] = _source == null ? "null"
			: ( '"' + (_source.length() > 32 ? _source.substring(0,24)+"\"...\""
				+ _source.substring(_source.length() - 3): _source) + '"');
		error(registeredID, modpars);
	}
	@Override
	/** Put default parse error message (XDEF515). */
	public final void putDefaultParseError() {
		errorWithString(XDEF.XDEF515); //Value error&{0}{: }
	}
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
	public final boolean booleanValue() {
//		return _source != null && (_ar == null || !_ar.errors());
		return _value == null ? false : ((XDValue)_value).booleanValue();
	}
	@Override
	public byte byteValue() {
		return _value == null ? 0 : ((XDValue)_value).byteValue();
	}
	@Override
	public short shortValue() {
		return _value == null ? 0 : ((XDValue)_value).shortValue();
	}
	@Override
	public int intValue() {
		return _value == null ? 0 : ((XDValue)_value).intValue();
	}
	@Override
	public long longValue() {
		return _value == null ? 0 : ((XDValue)_value).longValue();
	}
	@Override
	public float floatValue() {
		return _value == null ? 0 : ((XDValue)_value).floatValue();
	}
	@Override
	public double doubleValue() {
		return _value == null ? 0 : ((XDValue)_value).doubleValue();
	}
	@Override
	public BigDecimal decimalValue() {
		return _value == null ? null : ((XDValue)_value).decimalValue();
	}
	@Override
	public String stringValue() {
		return _value == null ? null : ((XDValue)_value).stringValue();
	}
	@Override
	public SDatetime datetimeValue() {
		return _value == null ? null : ((XDValue)_value).datetimeValue();
	}
	@Override
	public SDuration durationValue() {
		return _value == null ? null : ((XDValue)_value).durationValue();
	}
}