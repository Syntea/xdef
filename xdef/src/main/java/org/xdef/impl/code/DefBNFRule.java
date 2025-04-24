package org.xdef.impl.code;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.xdef.XDBNFRule;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.BNFRULE;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import org.xdef.msg.XDEF;
import org.xdef.sys.BNFRule;
import org.xdef.sys.SBuffer;
import org.xdef.sys.StringParser;

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
			DefParseResult result = new DefParseResult(s);
			Object[] stack = _rule.getParsedObjects();
			if (stack != null) {
				DefContainer c = new DefContainer();
				for (Object o: stack) {
					if (o == null) {
						c.addXDItem(new DefNull());
					} if (o instanceof Number) {
						if (o instanceof BigDecimal) {
							c.addXDItem(new DefDecimal(o.toString()));
						} else if (o instanceof BigInteger) {
							c.addXDItem(new DefBigInteger(o.toString()));
						} else if (o instanceof Long || o instanceof Integer) {
							c.addXDItem(new DefLong(o.toString()));
						} else if (o instanceof Float) {
							c.addXDItem(new DefDouble(((Float)o).doubleValue()));
						} else if (o instanceof Double) {
							c.addXDItem(new DefDouble(((Double)o)));
						} else {
							c.addXDItem(new DefString(o.toString()));
						}
					} else {
						c.addXDItem(new DefString(o.toString()));
					}
				}
				result.setParsedValue(c);
			}
			return result;
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

	/** Check if the object is null.
	 * @return true if tje object is null.
	 */
	@Override
	public boolean isNull() {return _rule == null;}

	/** Get associated object.
	 * @return the associated object or null.
	 */
	@Override
	public Object getObject() {return _rule;}

	/** Get type of value.
	 * @return The id of item type.
	 */
	@Override
	public short getItemId() {return XD_BNFRULE;}

	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	@Override
	public XDValueType getItemType() {return BNFRULE;}

	/** Get value as String.
	 * @return The string from value.
	 */
	@Override
	public String toString() {return stringValue();}

	/** Get string value of this object.
	 * @return string value of this object.
	 * string value.
	 */
	@Override
	public String stringValue() {return _rule == null ? "null" : _rule.toString();}

	/** Clone the item (returns this object here).
	 * @return this object.
	 */
	@Override
	public XDValue cloneItem() {return this;}

	/** Get code of operation.
	 * @return code of operation.
	 */
	@Override
	public short getCode() {return LD_CONST;}

	@Override
	public int hashCode() {return _rule.hashCode();}

	@Override
	public boolean equals(final Object arg) {return arg instanceof XDValue ? equals((XDValue) arg) : false;}

	/** Check whether some other XDValue object is "equal to" this one.
	 * @param arg other XDValue object to which is to be compared.
	 * @return always <i>false</i>.
	 */
	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_BNFRULE) {
			return false;
		}
		return arg == this || toString().equals(arg.toString());
	}
}