package org.xdef.impl.code;

import org.xdef.msg.XDEF;
import org.xdef.sys.BNFRule;
import org.xdef.sys.SBuffer;
import org.xdef.sys.StringParser;
import org.xdef.XDBNFRule;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import java.math.BigDecimal;
import java.math.BigInteger;

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
			DefParseResult result = new DefParseResult(s);
			Object[] stack = _rule.getParsedObjects();
			if (stack != null) {
				DefContainer c = new DefContainer();
				for (Object o: stack) {
					if (o == null) {
						c.addXDItem(new DefNull());
					} if (o instanceof Number) {
						if (o instanceof BigInteger || o instanceof BigDecimal){
							c.addXDItem(new DefDecimal(o.toString()));
						} else if (o instanceof Long || o instanceof Integer) {
							c.addXDItem(new DefLong(o.toString()));
						} else if (o instanceof Float) {
							c.addXDItem(
								new DefDouble(((Float)o).doubleValue()));
						} else if (o instanceof Double) {
							c.addXDItem(
								new DefDouble(((Double)o)));
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
	public short getItemId() {return XD_BNFRULE;}

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
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_BNFRULE) {
			return false;
		}
		return arg == this || toString().equals(arg.toString());
	}

}