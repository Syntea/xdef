package org.xdef.impl.util.conv.type.domain.restr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Skeletal implementation of enumerations restricted type.
 * @author Ilia Alexandrov
 */
public class EnumerationRestrImpl implements EnumerationRestricted {

	/**
	 * Set of enumeration values.
	 */
	private final Set<String> _enumerations = new HashSet<String>();

	@Override
	public void addEnumeration(String enumeration) {
		if (enumeration == null) {
			throw new NullPointerException("Enumeration is null!");
		}
		_enumerations.add(enumeration);
	}
	@Override
	public Set<String> getEnumerations() {
		return _enumerations;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EnumerationRestrImpl)) {
			return false;
		}
		EnumerationRestrImpl e = (EnumerationRestrImpl) obj;
		if (_enumerations.size() != e._enumerations.size()) {
			return false;
		}
		Iterator<String> it = _enumerations.iterator();
		while (it.hasNext()) {
			if (!e._enumerations.contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return (this._enumerations != null ? this._enumerations.hashCode() : 0);
	}

	@Override
	public String toString() {
		return "EnumerationRestrImpl[enumerations='"+_enumerations.size()+"']";
	}
}