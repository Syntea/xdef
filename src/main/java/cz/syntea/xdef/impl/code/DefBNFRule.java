package cz.syntea.xdef.impl.code;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.BNFRule;
import cz.syntea.xdef.sys.SBuffer;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDBNFRule;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.XDValueAbstract;
import cz.syntea.xdef.XDValueID;
import cz.syntea.xdef.XDValueType;

/** Implementation object containing compiled grammar of source of extended
 * Backus-Naur form.
 * @author Vaclav Trojan
 */
public final class DefBNFRule extends XDValueAbstract implements XDBNFRule {
	private final BNFRule _rule;

	/** Creates a new empty instance of BNFRule.*/
	public DefBNFRule() {_rule = null;}

	/** Creates a new empty instance of BNFRule.
	 * @param rule The BNF rule.
	 */
	public DefBNFRule(final BNFRule rule) {_rule = rule;}

	@Override
	public DefParseResult perform(final XDValue source) {
		StringParser p = new StringParser(source.toString());
		return perform(p);
	}

	@Override
	public DefParseResult perform(final String source) {
		StringParser p = new StringParser(source);
		return perform(p);
	}

	@Override
	public DefParseResult perform(final SBuffer source) {
		StringParser p = new StringParser(source);
		return perform(p);
	}

	@Override
	public DefParseResult perform(final StringParser p) {
		if (_rule.parse(p) && p.eos()) {
			String s = _rule.getParsedString();
			return new DefParseResult(s);
		}
		DefParseResult result = new DefParseResult();
		//Value doesn't fit to BNF rule '&{0} at position &{1}'
		result.error(XDEF.XDEF566, _rule.getName(), _rule.getParsedPosition());
		return result;
	}

	@Override
	public String getName() {return _rule == null ? null : _rule.getName();}

	@Override
	public BNFRule ruleValue() {return _rule;}

	@Override
	public String getParsedString() {return _rule.getParsedString();}

	@Override
	public int getParsedPosition() {return _rule.getParsedPosition();}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Check if the object is null.
	 * @return true if tje object is null.
	 */
	public boolean isNull() {return _rule == null;}

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _rule;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XDValueID.XD_BNFRULE;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return XDValueType.BNFRULE;}

	@Override
	/** Get value as String.
	 * @return The string from value.
	 */
	public String toString() {return stringValue();}

	@Override
	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	public String stringValue() {
		return _rule == null ? "null" : _rule.toString();
	}

	@Override
	/** Clone the item (returns this object here).
	 * @return this object.
	 */
	public XDValue cloneItem() {return this;}

	@Override
	/** Get code of operation.
	 * @return code of operation.
	 */
	public short getCode() {return CodeTable.LD_CONST;}

	@Override
	public int hashCode() {return _rule.hashCode();}

	@Override
	public boolean equals(final Object arg) {
		if (!(arg instanceof XDValue)) {
			return false;
		}
		return equals((XDValue) arg);
	}

	@Override
	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return always <tt>false</tt>.
	 */
	public boolean equals(final XDValue arg) {
		return arg == this || arg instanceof DefBNFRule
			&& toString().equals(((DefBNFRule) arg).toString());
	}

}