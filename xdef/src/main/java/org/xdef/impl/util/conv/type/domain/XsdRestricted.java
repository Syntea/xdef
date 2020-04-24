package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.type.domain.restr.DigitCountRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.DigitCountRestricted;
import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestricted;
import org.xdef.impl.util.conv.type.domain.restr.LengthRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.LengthRestricted;
import org.xdef.impl.util.conv.type.domain.restr.PatternRestImpl;
import org.xdef.impl.util.conv.type.domain.restr.PatternRestricted;
import org.xdef.impl.util.conv.type.domain.restr.ValueRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.ValueRestricted;
import org.xdef.impl.util.conv.type.domain.restr.WhiteSpaceRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.WhiteSpaceRestricted;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import java.util.Set;

/** Represents XML Schema restricted type.
 * @author Ilia Alexandrov
 */
public class XsdRestricted extends ValueType
	implements DigitCountRestricted, EnumerationRestricted, LengthRestricted,
	PatternRestricted, ValueRestricted, WhiteSpaceRestricted {

	/** Returns <code>true</code> if given parameter name is XDefinition
	 * <code>base</code> parameter name.
	 * @param paramName parameter name to test.
	 * @return <code>true</code> if given parameter name is XDefinition
	 * <code>base</code> parameter name.
	 */
	public static boolean isXdefBaseFacet(String paramName) {
		return XdNames.BASE.equals(paramName);
	}
	/** Restricted type's base type. */
	private ValueType _base;
	/** XDefionition base type (type name in XDefinition). */
	private XsdBase _xdefBase;
	/** Skeletal implementation of digit count restricted type. */
	private final DigitCountRestrImpl _digit = new DigitCountRestrImpl();
	/** Skeletal implementation of enumeration restricted type. */
	private final EnumerationRestrImpl _enum = new EnumerationRestrImpl();
	/** Skeletal implementation of length restricted type. */
	private final LengthRestrImpl _len = new LengthRestrImpl();
	/** Skeletal implementation of pattern restricted type. */
	private final PatternRestImpl _pattern = new PatternRestImpl();
	/** Skeletal implementation of value restricted type. */
	private final ValueRestrImpl _val = new ValueRestrImpl();
	/** Skeletal implementation of white space restricted type. */
	private final WhiteSpaceRestrImpl _whiteSpace = new WhiteSpaceRestrImpl();

	/** Sets base type.
	 * @param base base type.
	 * @throws NullPointerException if base type is <code>null</code>.
	 */
	public void setBase(ValueType base) {
		if (base == null) {
			throw new NullPointerException("Given base type is null");
		}
		_base = base;
	}

	/** Gets base type.
	 * @return base type or <code>null</code>.
	 */
	public ValueType getBase() {return _base;}

	/** Sets XDefinition base type (type name).
	 * @param xdefBase XDefinition base type.
	 * @throws NullPointerException if XDefinition base type
	 * is <code>null</code>.
	 */
	public void setXdefBase(XsdBase xdefBase) {
		if (xdefBase == null) {
			throw new NullPointerException("Given XDefinition base type null");
		}
		_xdefBase = xdefBase;
	}

	/** Get XDefinition base type.
	 * @return XDefinition base type or <code>null</code>.
	 */
	public XsdBase getXdefBase() {return _xdefBase;}
	@Override
	public Set<String> getEnumerations() {return _enum.getEnumerations();}
	@Override
	public void addEnumeration(String enumeration) {
		_enum.addEnumeration(enumeration);
	}
	@Override
	public Set<String> getPatterns() { return _pattern.getPatterns();}
	@Override
	public void addPattern(String pattern) {_pattern.addPattern(pattern);}
	@Override
	public Integer getFractionDigits() {return _digit.getFractionDigits();}
	@Override
	public void setFractionDigits(int fractionDigits) {
		_digit.setFractionDigits(fractionDigits);
	}
	@Override
	public Integer getLength() {return _len.getLength();}
	@Override
	public void setLength(int length) {_len.setLength(length);}
	@Override
	public String getMaxExclusive() {return _val.getMaxExclusive();}
	@Override
	public void setMaxExclusive(String maxExclusive) {
		_val.setMaxExclusive(maxExclusive);
	}
	@Override
	public String getMaxInclusive() {return _val.getMaxInclusive();}
	@Override
	public void setMaxInclusive(String maxInclusive) {
		_val.setMaxInclusive(maxInclusive);
	}
	@Override
	public Integer getMaxLength() {return _len.getMaxLength();}
	@Override
	public void setMaxLength(int maxLength) {_len.setMaxLength(maxLength);}
	@Override
	public String getMinExclusive() {return _val.getMinExclusive();}
	@Override
	public void setMinExclusive(String minExclusive) {
		_val.setMinExclusive(minExclusive);
	}
	@Override
	public String getMinInclusive() {return _val.getMinInclusive();}
	@Override
	public void setMinInclusive(String minInclusive) {
		_val.setMinInclusive(minInclusive);
	}
	@Override
	public Integer getMinLength() {return _len.getMinLength();}
	@Override
	public void setMinLength(int minLength) {_len.setMinLength(minLength);}
	@Override
	public Integer getTotalDigits() {return _digit.getTotalDigits();}
	@Override
	public void setTotalDigits(int totalDigits) {
		_digit.setTotalDigits(totalDigits);
	}
	@Override
	public String getWhiteSpace() {return _whiteSpace.getWhiteSpace();}
	@Override
	public void setWhiteSpace(String whiteSpace) {
		_whiteSpace.setWhiteSpace(whiteSpace);
	}
	@Override
	public int getKind() {return SCHEMA_RESTRICTION;}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdRestricted)) {
			return false;
		}
		XsdRestricted r = (XsdRestricted) obj;
		if (_base == null ? r._base != null : !_base.equals(r._base)) {
			return false;
		}
		if (_xdefBase == null ?
			r._xdefBase != null : !_xdefBase.equals(r._xdefBase)) {
			return false;
		}
		if (_digit == null ? r._digit != null : !_digit.equals(r._digit)) {
			return false;
		}
		if (_enum == null ? r._enum != null	: !_enum.equals(r._enum)) {
			return false;
		}
		if (_len == null ? r._len != null : !_len.equals(r._len)) {
			return false;
		}
		if (_pattern==null ? r._pattern!=null : !_pattern.equals(r._pattern)) {
			return false;
		}
		if (_val == null ? r._val != null : !_val.equals(r._val)) {
			return false;
		}
		return !(_whiteSpace == null ?
			r._whiteSpace != null : !_whiteSpace.equals(r._whiteSpace));
	}
	@Override
	public int hashCode() {
		int hash = (this._base != null ? this._base.hashCode() : 0);
		hash = 43*hash+(this._xdefBase != null ? this._xdefBase.hashCode() : 0);
		hash = 43*hash+(this._digit != null ? this._digit.hashCode() : 0);
		hash = 43*hash+(this._enum != null ? this._enum.hashCode() : 0);
		hash = 43*hash+(this._len != null ? this._len.hashCode() : 0);
		hash = 43*hash+(this._pattern != null ? this._pattern.hashCode() : 0);
		hash = 43*hash+(this._val != null ? this._val.hashCode() : 0);
		return 3*hash+(this._whiteSpace!=null ? this._whiteSpace.hashCode():0);
	}
	@Override
	public String toString() {
		return "SchemaRestricted[base='" + _base + "', "
			+ "xdefBase='" + _xdefBase + "', "
			+ "enumerations='" + _enum.getEnumerations().size() + "', "
			+ "patterns='" + _pattern.getPatterns().size() + "', "
			+ "fractionDigits='" + _digit.getFractionDigits() + "', "
			+ "length='" + _len.getLength() + "', "
			+ "maxExclusive='" + _val.getMaxExclusive() + "', "
			+ "maxInclusive='" + _val.getMaxInclusive() + "', "
			+ "maxLength='" + _len.getMaxLength() + "', "
			+ "minExclusive='" + _val.getMinExclusive() + "', "
			+ "minInclusive='" + _val.getMinInclusive() + "', "
			+ "minLength='" + _len.getMinLength() + "', "
			+ "totalDigits='" + _digit.getTotalDigits() + "', "
			+ "whiteSpace='" + _whiteSpace.getWhiteSpace() + "']";
	}
}