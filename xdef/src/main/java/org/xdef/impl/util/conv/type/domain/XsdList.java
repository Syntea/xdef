package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestricted;
import org.xdef.impl.util.conv.type.domain.restr.LengthRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.LengthRestricted;
import org.xdef.impl.util.conv.type.domain.restr.PatternRestImpl;
import org.xdef.impl.util.conv.type.domain.restr.PatternRestricted;
import org.xdef.impl.util.conv.type.domain.restr.WhiteSpaceRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.WhiteSpaceRestricted;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import java.util.HashSet;
import java.util.Set;

/** Represents XML Schema list type.
 * @author Ilia Alexandrov
 */
public class XsdList extends ValueType implements
		LengthRestricted, EnumerationRestricted, PatternRestricted,
		WhiteSpaceRestricted {

	/**
	 * Schema list type aviable facet set.
	 */
	private static final Set<XsdFacet> _xsdListFacets = new HashSet<XsdFacet>();

	static {
		_xsdListFacets.add(XsdFacet.ENUMERATION);
		_xsdListFacets.add(XsdFacet.LENGTH);
		_xsdListFacets.add(XsdFacet.MAX_LENGTH);
		_xsdListFacets.add(XsdFacet.MIN_LENGTH);
		_xsdListFacets.add(XsdFacet.PATTERN);
		_xsdListFacets.add(XsdFacet.WHITE_SPACE);
	}

	/** Returns <code>true</code> if given facet is aviable for schema list type.
	 *
	 * @param facet facet to test.
	 * @return <code>true</code> if given facet is aviable for schema list type.
	 */
	public static boolean isAviableFacet(XsdFacet facet) {
		if (facet == null) {
			throw new NullPointerException("Given facet is null!");
		}
		return _xsdListFacets.contains(facet);
	}

	/** Returns <code>true</code> if given type name is schema list type name in
	 * XDefinition.
	 *
	 * @param typeName type name to test.
	 * @return <code>true</code> if given type name is schema list type name in
	 * XDefinition.
	 */
	public static boolean isXsdList(String typeName) {
		return XdNames.XS_LIST.equals(typeName);
	}

	/** Returns <code>true</code> if given parameter name is schema list type
	 * <code>itemType</code> parameter name in XDefinition.
	 *
	 * @param paramName parameter name to test.
	 * @return <code>true</code> if given parameter name is schema list type
	 * <code>itemType</code> parameter name in XDefinition.
	 */
	public static boolean isItemParam(String paramName) {
		return XdNames.ITEM_LIST.equals(paramName);
	}
	/**
	 * List item type.
	 */
	private ValueType _itemType;
	/**
	 * Skeletal implementation of length restricted type.
	 */
	private final LengthRestrImpl _len = new LengthRestrImpl();
	/**
	 * Skeletal implementation of enumeration restricted type.
	 */
	private final EnumerationRestrImpl _enums = new EnumerationRestrImpl();
	/**
	 * Skeletal implementation of pattern restricted type.
	 */
	private final PatternRestImpl _pattern = new PatternRestImpl();
	/**
	 * Skeletal implementation of white space restricted type.
	 */
	private final WhiteSpaceRestrImpl _whiteSpace = new WhiteSpaceRestrImpl();

	/** Gets list item type.
	 *
	 * @return item type or <code>null</code>.
	 */
	public ValueType getValueType() {
		return _itemType;
	}

	/** Sets item type.
	 *
	 * @param itemType item type to set.
	 * @throws NullPointerException if given item type is <code>null</code>.
	 */
	public void setItemType(ValueType itemType) {
		if (itemType == null) {
			throw new NullPointerException("Given item type is null!");
		}
		_itemType = itemType;
	}

	@Override
	public Set<String> getPatterns() {
		return _pattern.getPatterns();
	}
	@Override
	public void addPattern(String pattern) {
		_pattern.addPattern(pattern);
	}
	@Override
	public Set<String> getEnumerations() {
		return _enums.getEnumerations();
	}
	@Override
	public void addEnumeration(String enumeration) {
		_enums.addEnumeration(enumeration);
	}
	@Override
	public Integer getLength() {
		return _len.getLength();
	}
	@Override
	public void setLength(int length) {
		_len.setLength(length);
	}
	@Override
	public Integer getMinLength() {
		return _len.getMinLength();
	}
	@Override
	public void setMinLength(int length) {
		_len.setLength(length);
	}
	@Override
	public Integer getMaxLength() {
		return _len.getMaxLength();
	}
	@Override
	public void setMaxLength(int maxLength) {
		_len.setMaxLength(maxLength);
	}
	@Override
	public void setWhiteSpace(String whiteSpace) {
		_whiteSpace.setWhiteSpace(whiteSpace);
	}
	@Override
	public String getWhiteSpace() {
		return _whiteSpace.getWhiteSpace();
	}
	@Override
	public int getKind() {
		return SCHEMA_LIST;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdList)) {
			return false;
		}
		XsdList l = (XsdList) obj;
		if (_itemType == null ? l._itemType != null : !_itemType.equals(l._itemType)) {
			return false;
		}
		if (_enums == null ? l._enums != null : !_enums.equals(l._enums)) {
			return false;
		}
		if (_len == null ? l._len != null : !_len.equals(l._len)) {
			return false;
		}
		if (_pattern == null ? l._pattern != null : !_pattern.equals(l._pattern)) {
			return false;
		}
		if (_whiteSpace == null ? l._whiteSpace != null : !_whiteSpace.equals(l._whiteSpace)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 31 * hash + (this._itemType != null ? this._itemType.hashCode() : 0);
		hash = 31 * hash + (this._len != null ? this._len.hashCode() : 0);
		hash = 31 * hash + (this._enums != null ? this._enums.hashCode() : 0);
		hash = 31 * hash + (this._pattern != null ? this._pattern.hashCode() : 0);
		hash = 31 * hash + (this._whiteSpace != null ? this._whiteSpace.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "SchemaList[itemType='" + _itemType + "', "
				+ "length='" + _len.getLength() + "', "
				+ "minLength='" + _len.getMinLength() + "', "
				+ "maxLength='" + _len.getMaxLength() + "', "
				+ "enumerations='" + _enums.getEnumerations().size() + "', "
				+ "patterns='" + _pattern.getPatterns().size() + "', "
				+ "whiteSpace='" + _whiteSpace.getWhiteSpace() + "']";
	}
}