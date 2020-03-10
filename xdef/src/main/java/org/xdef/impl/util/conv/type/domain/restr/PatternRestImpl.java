package org.xdef.impl.util.conv.type.domain.restr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** Skeletal implementation of pattern restricted type.
 * @author Ilia Alexandrov
 */
public class PatternRestImpl implements PatternRestricted {

	/** Set of patterns. */
	private final Set<String> _paterns = new HashSet<String>();

	@Override
	public void addPattern(String pattern) {
		if (pattern == null || pattern.length() == 0) {
			throw new NullPointerException("Given pattern is empty!");
		}
		_paterns.add(pattern);
	}
	@Override
	public Set<String> getPatterns() {return _paterns;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PatternRestImpl)) {
			return false;
		}
		PatternRestImpl p = (PatternRestImpl) obj;
		if (_paterns.size() != p._paterns.size()) {
			return false;
		}
		Iterator<String> it = _paterns.iterator();
		while (it.hasNext()) {
			if (!p._paterns.contains(it.next())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 3*(this._paterns != null ? this._paterns.hashCode() : 0);
	}

	@Override
	public String toString() {
		return "PatternRestImpl[patterns='" + _paterns.size() + "']";
	}
}