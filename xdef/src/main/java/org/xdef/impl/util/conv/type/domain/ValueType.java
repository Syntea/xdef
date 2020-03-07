package org.xdef.impl.util.conv.type.domain;

/** Represents any value type.
 * @author Ilia Alexandrov
 */
public abstract class ValueType {

	/** Schema base type kind. */
	public static final int SCHEMA_BASE = 1;
	/** Schema restricted type kind. */
	public static final int SCHEMA_RESTRICTION = SCHEMA_BASE + 1;
	/** Schema list type kind. */
	public static final int SCHEMA_LIST = SCHEMA_RESTRICTION + 1;
	/** Schema union type kind. */
	public static final int SCHEMA_UNION = SCHEMA_LIST + 1;
	/** X-definition type kind. */
	public static final int XDEF_TYPE = SCHEMA_UNION + 1;
	/** X-definition list type kind. */
	public static final int XDEF_LIST = XDEF_TYPE + 1;
	/** Other type kind. */
	public static final int OTHER = XDEF_LIST + 1;
	/** Parsed type string. */
	private String _typeString;

	/** Gets type string.
	 * @return type string or <code>null</code>.
	 */
	public final String getTypeString() {return _typeString;}

	/** Sets type string.
	 * @param typeString type string.
	 * @throws NullPointerException if given type string is <code>null</code>.
	 * @throws IllegalArgumentException if given type string is empty.
	 */
	public final void setTypeString(String typeString) {
		if (typeString == null || typeString.length() == 0) {
			throw new NullPointerException("Given type string is empty");
		}
		_typeString = typeString;
	}

	/** Returns value type kind of implementation.
	 * @return value type kind.
	 */
	public abstract int getKind();
}