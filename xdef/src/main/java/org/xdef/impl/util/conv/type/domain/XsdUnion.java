package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestrImpl;
import org.xdef.impl.util.conv.type.domain.restr.EnumerationRestricted;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Represents XML Schema <code>union</code> type.
 * @author Ilia Alexandrov
 */
public class XsdUnion extends ValueType implements EnumerationRestricted {

	/** Schema union type facet set. */
	private static final Set<XsdFacet> _xsdUnionFacets = new HashSet<XsdFacet>();

	static {
		_xsdUnionFacets.add(XsdFacet.ENUMERATION);
	}

	/** Returns <code>true</code> if given facet is aviable for schema union
	 * type.
	 *
	 * @param facet facet to test.
	 * @return <code>true</code> if given facet is aviable for schema union
	 * type.
	 */
	public static boolean isAviableFacet(XsdFacet facet) {
		if (facet == null) {
			throw new NullPointerException("Given facet is null!");
		}
		return _xsdUnionFacets.contains(facet);
	}

	/** Returns <code>true</code> if given parameter name is XDefinition
	 * <code>memberTypes</code> parameter name.
	 *
	 * @param name name to test.
	 * @return <code>true</code> if given parameter name is XDefinition
	 * <code>memberTypes</code> parameter name.
	 */
	public static boolean isItemParam(String name) {
		return XdNames.ITEM_UNION.equals(name);
	}

	/** Returns <code>true</code> if given type name is XDefinition <code>union</code>
	 * name.
	 *
	 * @param name type name to test.
	 * @return <code>true</code> if given type name is XDefinition <code>union</code>
	 * name.
	 */
	public static boolean isXsdUnion(String name) {
		return XdNames.XS_UNION.equals(name);
	}
	/**
	 * Set of union member types.
	 */
	private final Set<ValueType> _memberTypes = new HashSet<ValueType>();
	/**
	 * Skeletal implementation of enumeration restricted type.
	 */
	private final EnumerationRestrImpl _enum = new EnumerationRestrImpl();

	/** Gets set of member types.
	 *
	 * @return member type set.
	 */
	public Set<ValueType> getMemberTypes() {
		return _memberTypes;
	}

	/** Adds member type.
	 *
	 * @param memberType member type.
	 */
	public void addMemberType(ValueType memberType) {
		_memberTypes.add(memberType);
	}
@Override

	public void addEnumeration(String enumeration) {
		_enum.addEnumeration(enumeration);
	}
@Override

	public Set<String> getEnumerations() {
		return _enum.getEnumerations();
	}

	@Override
	public int getKind() {return SCHEMA_UNION;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdUnion)) {
			return false;
		}
		XsdUnion u = (XsdUnion) obj;
		if (_memberTypes.size() != u._memberTypes.size()) {
			return false;
		}
		Iterator<ValueType> it = _memberTypes.iterator();

		while (it.hasNext()) {
			if (!u._memberTypes.contains(it.next())) {
				return false;
			}
		}
		return !(_enum == null ? u._enum != null : !_enum.equals(u._enum));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 17 * hash + (this._memberTypes != null ? this._memberTypes.hashCode() : 0);
		hash = 17 * hash + (this._enum != null ? this._enum.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString() {
		return "XsdUnion[memberTypes='" + _memberTypes.size() + "', "
				+ "enumerations='" + _enum.getEnumerations().size() + "']";
	}
}