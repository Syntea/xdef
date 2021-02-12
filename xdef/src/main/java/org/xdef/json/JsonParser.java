package org.xdef.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xdef.impl.compile.CompileJsonXdef;
import org.xdef.msg.JSON;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;

/** Parser of JSON source.
 * @author Vaclav Trojan
 */
public class JsonParser extends StringParser {

	/** Flag to accept comments in JSON. */
	private boolean _acceptComments; // default value = false
	/** Flag to generate SPositions when parsing JSON. */
	private boolean _genJObjects; // default value = false
	/** Flag if the parsed data are in X-definition. */
	private boolean _jdef; // default value = false
	/** Position of processed item.`*/
	private SPosition _sPosition;

	/** Create instance of parser. */
	public JsonParser() {}

	/** Set genJObjects flag (and the _acceptComments flag is also set). */
	public final void setGenJObjects() {
		_genJObjects = true;
		_acceptComments = true;
	}

	/** Set mode that JSON is parsed in X-definition compiler. */
	public final void setXJsonMode() {
		setGenJObjects();
		_jdef = true;
	}

	/** Create modification string with source position.
	 * @return modification string with source position.
	 */
	public final String genPosMod() {
		return "&{line}" + getLineNumber()
			+ "&{column}" + getColumnNumber()
			+ "&{sysId}" + getSysId();
	}

	/** Skip white space separators (and comments if accepted).
	 * @return true if a space or comment was found.
	 */
	public final boolean isSpacesOrComments() {
		boolean result = isSpaces();
		while(isToken("/*")) {
			result = true;
			if (!_acceptComments) { // omments not allowed
				warning(JSON.JSON019);  //Comments are not allowed here
			}
			if (!findTokenAndSkip("*/")) {
				error(JSON.JSON015); //Unclosed comment
				setEos();
				return result;
			}
			isSpaces();
		}
		return result;
	}

	/** Read JSON value.
	 * @return parsed value: List, Map, String, Number, Boolean or null.
	 * @throws SRuntimeException is an error occurs.
	 */
	private Object readValue() throws SRuntimeException {
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
			return _genJObjects
				? new CompileJsonXdef.JValue(_sPosition, null) : null;
		}
		if (_genJObjects) {
			_sPosition = getPosition();
		}
		if (isChar('{')) { // Map
			Map<String, Object> result;
			result = _genJObjects ? new CompileJsonXdef.JMap(_sPosition)
				: new LinkedHashMap<String,Object>();
			isSpacesOrComments();
			if (isChar('}')) { // empty map
				return result;
			}
			boolean wasScript = false;
			while(!eos()) {
				int i;
				if (!wasScript && _jdef
					&& (i = isOneOfTokens(CompileJsonXdef.SCRIPT_NAME,
						CompileJsonXdef.ONEOF_NAME)) >= 0) {
					wasScript = true;
					SPosition spos = getPosition();
					isSpacesOrComments();
					Object o;
					if (i == 1) {
						String s;
						if (isChar(':')) {
							spos.setIndex(spos.getIndex() - 1);
							isSpacesOrComments();
							o = readValue();
							if (o instanceof CompileJsonXdef.JValue
								&& ((CompileJsonXdef.JValue)o).getValue()
								instanceof String) {
								s = CompileJsonXdef.ONEOF_KEY +
									((CompileJsonXdef.JValue)o).getValue();
							} else {
								//Value of $script must be string with X-script
								error(JSON.JSON018);
								s = CompileJsonXdef.ONEOF_KEY;
							}
						} else {
							s = CompileJsonXdef.ONEOF_KEY;
						}
						o = new CompileJsonXdef.JValue(spos, s);
					} else {
						if (!isChar(':') && i != 1) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002, ":");
						}
						isSpacesOrComments();
						o = readValue();
					}
					if (o != null && o instanceof CompileJsonXdef.JValue
						&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String) {
						if (result.containsKey(CompileJsonXdef.SCRIPT_KEY)) {
							//Value pair &{0} already exists
							error(JSON.JSON022, CompileJsonXdef.SCRIPT_KEY);
						} else {
							result.put(CompileJsonXdef.SCRIPT_KEY, o);
						}
					} else {
						//Value of $script must be string with X-script
						error(JSON.JSON018);
					}
				} else {
					Object o = readValue();
					if (o != null && (o instanceof String ||
						(_genJObjects && o instanceof CompileJsonXdef.JValue)
						&& ((CompileJsonXdef.JValue) o).getValue()
							instanceof String)) {
						 // parse JSON named pair
						String name = _genJObjects ? o.toString() : (String) o;
						isSpacesOrComments();
						if (!isChar(':')) {
							//"&{0}"&{1}{ or "}{"} expected
							error(JSON.JSON002, ":");
						}
						isSpacesOrComments();
						o = readValue();
						if (result.containsKey(name)) {
							String s = JsonUtil.jstringToSource(name);
							if (!s.startsWith("\"") || !s.endsWith("\"")) {
								s = '"' + s + '"';
							}
							//Value pair &{0} already exists
							error(JSON.JSON022, s);
						} else {
							result.put(name, o);
						}
					} else {
						fatal(JSON.JSON004); //String with name of item expected
						return result;
					}
				}
				isSpacesOrComments();
				if (isChar('}')) {
					isSpacesOrComments();
					return result;
				}
				if (isChar(',')) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar('}')) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
						setPosition(spos1);
						return result;
					}
				} else {
					if (eos()) {
						break;
					}
					//"&{0}"&{1}{ or "}{"} expected
					error(JSON.JSON002, ",", "}");
					if (getCurrentChar() != '"') {
						break;
					}
				}
			}
			//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
			fatal(JSON.JSON002, "}");
			return result;
		} else if (isChar('[')) {
			List<Object> result;
			result = _genJObjects ? new CompileJsonXdef.JArray(_sPosition)
				: new ArrayList<Object>();
			isSpacesOrComments();
			if (isChar(']')) { // empty array
				return result;
			}
			boolean wasScript = false;
			boolean wasErrorReported = false;
			while(!eos()) {
				int i;
				if (!wasScript &&_jdef
					&& (i = isOneOfTokens(CompileJsonXdef.SCRIPT_NAME,
						CompileJsonXdef.ONEOF_NAME)) >= 0) {
					wasScript = true;
					if (isChar(':')) {
						isSpacesOrComments();
						Object o = readValue();
						if (o instanceof CompileJsonXdef.JValue
							&& ((CompileJsonXdef.JValue)o).getValue()
							instanceof String){
							CompileJsonXdef.JValue jv =
								(CompileJsonXdef.JValue) o;
							if (i == 1) {
								SPosition spos = jv.getPosition();
								spos.setIndex(spos.getIndex() - 1);
								String s =
									CompileJsonXdef.ONEOF_KEY + jv.getValue();
								jv = new CompileJsonXdef.JValue(spos, s);
							}
							result.add(new CompileJsonXdef.JValue(null, jv));
						} else {
							//Value of $script must be string with X-script
							error(JSON.JSON018);
						}
					} else {
						if (i == 0) {
							//"&{0}"&{1}{ or "}{"} expected
						   error(JSON.JSON002, ":");
						} else {
							SPosition spos = getPosition();
							spos.setIndex(spos.getIndex() - 1);
							result.add(new CompileJsonXdef.JValue(null,
								new CompileJsonXdef.JValue(spos,
									CompileJsonXdef.ONEOF_KEY)));
						}
					}
				} else {
					result.add(readValue());
				}
				isSpacesOrComments();
				if (isChar(']')) {
					return result;
				}
				if (isChar(',')) {
					SPosition spos = getPosition();
					isSpacesOrComments();
					if (isChar(']')) {
						SPosition spos1 = getPosition();
						setPosition(spos);
						error(JSON.JSON020); //redundant comma
						setPosition(spos1);
						return result;
					}
				} else {
					if (wasErrorReported) {
						break;
					}
					error(JSON.JSON002,",","]"); //"&{0}"&{1}{ or "}{"} expected
					if (eos()) {
						break;
					}
					wasErrorReported = true;
				}
			}
			error(JSON.JSON002, "]"); //"&{0}"&{1}{ or "}{"} expected
			return result;
		} else if (isChar('"')) { // string
			String s = JsonUtil.readJSONString(this);
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, s) : s;
		} else if (isToken("null")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, null)
				: null;
		} else if (isToken("true")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, true)
				: true;
		} else if (isToken("false")) {
			return _genJObjects ? new CompileJsonXdef.JValue(_sPosition, false)
				: false;
		} else if (_jdef && isToken(CompileJsonXdef.ANY_NAME)) {
			isSpacesOrComments();
			if (isChar(':')) {
				isSpacesOrComments();
				Object val = readValue();
				if (!(val instanceof CompileJsonXdef.JValue)
					|| (((CompileJsonXdef.JValue) val).getValue()
						instanceof String)) {
					//After ":" in the command $any must follow a string value
					error(JSON.JSON021);
				} else {
					return new CompileJsonXdef.JAny(
						_sPosition,((CompileJsonXdef.JValue) val).getSBuffer());
				}
			}
			return new CompileJsonXdef.JAny(_sPosition, null);
		} else {
			int pos;
			boolean wasError = false;
			if (isChar('+')) {
				error(JSON.JSON017, "+");//Not allowed character '&{0}'
				wasError = true;
				pos = getIndex();
			} else {
				pos = getIndex();
				isChar('-');
			}
			int pos1 = getIndex() - pos;
			if (isInteger()) {
				Number number;
				boolean isfloat = false;
				if (isChar('.')) { // decimal point
					isfloat = true;
					if (!isInteger()) {
						error(JSON.JSON017, ".");//Not allowed character '&{0}'
						wasError = true;
					}
				}
				char ch = getCurrentChar();
				if (isChar('e') || isChar('E')) {//exponent
					isfloat = true;
					if (!isSignedInteger()) {
						error(JSON.JSON017,""+ch);//Not allowed character '&{0}'
						wasError = true;
					}
				}
				String s = getBufferPart(pos, getIndex());
				if (s.charAt(pos1) == '0' && s.length() > 1 &&
					Character.isDigit(s.charAt(pos1 + 1))) {
						error(JSON.JSON014); // Illegal leading zero in number
				}
				number = wasError ? 0
					: isfloat ? new BigDecimal(s) : new BigInteger(s);
				return _genJObjects
					? new CompileJsonXdef.JValue(_sPosition,number) : number;
			}
			error(JSON.JSON010); //JSON value expected
			findOneOfChars(",[]{}"); // skip to next item
			return _genJObjects
				? new CompileJsonXdef.JValue(_sPosition, null) : null;
		}
	}

	/** Parse JSON source data.
	 * @return parsed JSON object.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final Object parse() throws SRuntimeException {
		isSpacesOrComments();
		Object result = readValue();
		isSpacesOrComments();
		_sPosition = getPosition();
		if (!eos()) {
			error(JSON.JSON008, genPosMod()); //Text after JSON not allowed
		}
		return result;
	}

}