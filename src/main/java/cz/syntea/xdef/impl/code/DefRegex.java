package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDRegex;
import cz.syntea.xdef.XDRegexResult;
import cz.syntea.xdef.XDValueAbstract;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;
import cz.syntea.xdef.msg.XDEF;

/** Regular expression (compilation). */
public final class DefRegex extends XDValueAbstract implements XDRegex {
	/** The source of regular expression. */
	private String _source;
	/** Compiled pattern of regular expression. */
	private Pattern _value;

	/** Creates a new instance of DefRegex. */
	public DefRegex() {this(".*");}

	/** Creates a new instance of DefRegex.
	 * @param source The string with regular expression.
	 */
	public DefRegex(final String source) {
		_source = source;
		try {
			_value = Pattern.compile(DefRegexTranslator.translate(source));
		} catch (Exception ex) {
			String s = ex.getMessage();
			if (s == null) {
				s = "" + ex;
			}
			//Incorrect regular expression: &{0}
			throw new SRuntimeException(XDEF.XDEF650, source + "; ("+s+")");
		}
	}

	@Override
	/** Check if given data matches the regular expression.
	 * @param data The data to be checked.
	 * @return <tt>true</tt> if and only if the data matches regular expression.
	 */
	public boolean matches(final String data) {
		return (_value.matcher(data)).matches();
	}

	@Override
	/** Return regular expression result.
	 * @param source string to be processed with this regular expression.
	 * @return XDRegexResult object.
	 */
	public final XDRegexResult getRegexResult(final String source) {
		return new DefRegexResult(getMatchResult(source));
	}

	/** Get matcher from regular expression.
	 * @param data data to be matched.
	 * @return matcher from regular expression.
	 */
	private Matcher getMatchResult(final String data) {
		return _value.matcher(data);
	}

	@Override
	/** Get value of item as String representation of value.
	 * @return The string representation of value of the object.
	 */
	public String sourceValue() {return _source;}


////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_REGEX;}
	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.REGEX;}
	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return _source;}
	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {return _source;}
	@Override
	public boolean equals(final XDValue arg) {
		return arg != null && arg instanceof DefRegex &&
			_source != null && _source.equals(arg.stringValue());
	}
	@Override
	/** Clone the item (here return just this item).
	 * @return the object with the copy of this one.
	 */
	public XDValue cloneItem() {return this;}
}
