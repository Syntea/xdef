package org.xdef.impl.code;

import org.xdef.XDBNFGrammar;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import static org.xdef.XDValueType.BNFGRAMMAR;
import static org.xdef.impl.code.CodeTable.COMPILE_BNF;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SRuntimeException;

/** Implementation object containing compiled grammar extended
 * Backus-Naur form.
 * @author Vaclav Trojan
 */
public class DefBNFGrammar extends XDValueAbstract implements XDBNFGrammar {

	/** Object with compiled extended BNF grammar. */
	private final BNFGrammar _value;
	/** Index to global variable with the grammar to be extended or -1. */
	private int _extVar;
	/** Source of the grammar. */
	private String _source;

	/** Creates a new empty instance of BNFGrammar.*/
	public DefBNFGrammar() {_value = null; _extVar = -1; _source = null;}

	/** Creates a new instance of BNFGrammar from .
	 * @param value grammar.
	 */
	public DefBNFGrammar(final BNFGrammar value) {
		_value = value;
		_extVar = -1;
		 _source = null;
	}

	/** Creates a new instance of BNFGrammar from .
	 * @param source SBuffer with extended BNF source code.
	 * @param reporter Report writer or <i>null</i>. If this argument is
	 * <i>null</i> and an error occurs then SRuntimeException is thrown.
	 * @throws SRuntimeException if an error occurs and if reporter is
	 * <i>null</i>.
	 */
	public DefBNFGrammar(final String source,
		final ReportWriter reporter) throws SRuntimeException {
			_source = source;
			_extVar = -1;
			_value = BNFGrammar.compile(null, source, reporter);
	}

	/** Creates a new instance of BNFGrammar from .
	 * @param grammar the grammar to be extended or <i>null</i>.
	 * @param extVar index of global variable with grammar to be extended or -1.
	 * @param source SBuffer with extended BNF source code.
	 * @param reporter Report writer or <i>null</i>. If this argument is
	 * <i>null</i> and an error occurs then SRuntimeException is thrown.
	 * @throws SRuntimeException if an error occurs and if reporter is
	 * <i>null</i>.
	 */
	public DefBNFGrammar(final DefBNFGrammar grammar,
		final int extVar,
		final SBuffer source,
		final ReportWriter reporter) throws SRuntimeException {
		_source = source.getString();
		_extVar = extVar;
		_value = BNFGrammar.compile(grammar == null ? null : grammar._value,
			source, reporter);
	}

	/** Creates a new instance of BNFGrammar from .
	 * @param grammar the grammar to be extended or <i>null</i>.
	 * @param extVar index of global variable with grammar to be extended or -1.
	 * @param source String with extended BNF source code.
	 * @param reporter Report writer or <i>null</i>. If this argument is
	 * <i>null</i> and an error occurs then SRuntimeException is thrown.
	 * @throws SRuntimeException if an error occurs and if reporter is
	 * <i>null</i>.
	 */
	public DefBNFGrammar(final DefBNFGrammar grammar,
		final int extVar,
		final String source,
		final ReportWriter reporter) throws SRuntimeException {
		_source = source;
		_extVar = extVar;
		_value = BNFGrammar.compile(grammar == null ? null : grammar._value,
			source, reporter);
	}

	/** Set source.
	 * @param source set source form of this grammar.
	 */
	public void setSource(final String source) {
		_source = source;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDBNFGrammar interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public DefBNFRule getRule(final String name) {
		if (_value == null) {
			return null;
		}
		return new DefBNFRule(_value.getRule(name));
	}

	@Override
	/** Get this object as BNFGrammar.
	 * @return value of this item as BNFGrammar object.
	 */
	public BNFGrammar grammarValue() { return _value;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get associated object.
	 * @return the associated object or null.
	 */
	public Object getObject() {return _value;}

	@Override
	/** Get type of value.
	 * @return The id of item type.
	 */
	public short getItemId() {return XD_BNFGRAMMAR;}

	@Override
	/** Get ID of the type of value
	 * @return enumeration item of this type.
	 */
	public XDValueType getItemType() {return BNFGRAMMAR;}

	@Override
	/** Get type of value.
	 * @return id of item type.
	 */
	public int getParam() {return _extVar;}

	@Override
	/** Set type of value.
	 * @param extVar id of item type.
	 */
	public void setParam(final int extVar) {_extVar = extVar;}

	@Override
	public boolean equals(final XDValue arg) {
		if (isNull()) {
			return arg == null || arg.isNull();
		}
		if (arg == null || arg.isNull() || arg.getItemId() != XD_BNFGRAMMAR) {
			return false;
		}
		return arg == this || toString().equals(arg.toString());
	}

	@Override
	public String toString() {return stringValue();}

	@Override
	public String stringValue() {
		return (_source!=null)
			? _source : _value==null ? "null" : _value.display(false);
	}

	@Override
	public XDValue cloneItem() {return this;}

	@Override
	public short getCode() {return COMPILE_BNF;}

	@Override
	public void setCode(final short code) {}
}