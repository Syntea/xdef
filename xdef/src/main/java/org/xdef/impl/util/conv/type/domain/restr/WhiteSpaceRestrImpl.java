package org.xdef.impl.util.conv.type.domain.restr;

import java.util.HashSet;
import java.util.Set;

/** Skeletal implementation of white space restricted type.
 * @author Ilia Alexandrov
 */
public class WhiteSpaceRestrImpl implements WhiteSpaceRestricted {

	/** Set of white space values. */
	private static final Set<String> _WSCONSTANTS = new HashSet<String>();

	static {
		_WSCONSTANTS.add(COLLAPSE_STR);
		_WSCONSTANTS.add(PRESERVE_STR);
		_WSCONSTANTS.add(REPLACE_STR);
	}
	/** White space restriction. */
	private String _whiteSpace;

	/** Sets white space.
	 *
	 * @param whiteSpace white space value.
	 * @throws NullPointerException if given white space value is
	 * <code>null</code>.
	 * @throws IllegalArgumentException if given white space value is invalid.
	 */
	@Override
	public void setWhiteSpace(String whiteSpace) {
		if (whiteSpace == null) {
			throw new NullPointerException("White space is null!");
		}
		if (!_WSCONSTANTS.contains(whiteSpace)) {
			throw new IllegalArgumentException("White space is illegal!");
		}
		_whiteSpace = whiteSpace;
	}

	/** Gets white space value.
	 * @return white space value or <code>null</code>.
	 */
	@Override
	public String getWhiteSpace() {return _whiteSpace;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof WhiteSpaceRestrImpl)) {
			return false;
		}
		WhiteSpaceRestrImpl w = (WhiteSpaceRestrImpl) obj;
		return !(_whiteSpace == null ?
			w._whiteSpace != null : !_whiteSpace.equals(w._whiteSpace));
	}
	@Override
	public int hashCode() {
		return 3*(this._whiteSpace != null ? this._whiteSpace.hashCode() : 0);
	}
	@Override
	public String toString() {
		return "WhiteSpaceRestrImpl[whiteSpace='" + _whiteSpace + "']";
	}
}