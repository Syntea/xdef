package org.xdef.impl.util.conv.type.domain;

import java.util.ArrayList;
import java.util.List;

/** Represents XDefinition type.
 * @author Ilia Alexandrov
 */
public class XdefType extends ValueType {

	/** Type base. */
	private final XdefBase _base;
	/** Type parameters. */
	private final List<String> _params = new ArrayList<String>();

	/** Creates instance of XDefinition type.
	 * @param base type base.
	 * @throws NullPointerException if given base is <code>null</code>.
	 */
	public XdefType(XdefBase base) {
		if (base == null) {
			throw new NullPointerException("Given type is null!");
		}
		_base = base;
	}

	/** Get base type.
	 * @return base type.
	 */
	public XdefBase getBase() {return _base;}

	/** Gets list of type parameters.
	 * @return list of type parameters.
	 */
	public List<String> getParams() {return _params;}

	/** Adds parameter.
	 * @param param parameter.
	 * @throws NullPointerException if given parameter is <code>null</code>.
	 */
	public void addParam(String param) {
		if (param == null) {
			throw new NullPointerException("Given parameter is null!");
		}
		_params.add(param);
	}

	@Override
	public int getKind() {return XDEF_TYPE;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdefType)) {
			return false;
		}
		XdefType x = (XdefType) obj;
		if (!_base.equals(x._base)) {
			return false;
		}
		if (_params.size() != x._params.size()) {
			return false;
		}
		for (int i = 0; i < _params.size(); i++) {
			if (!_params.get(i).equals(x._params.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = (this._base != null ? this._base.hashCode() : 0);
		return 3 * hash + (this._params != null ? this._params.hashCode() : 0);
	}

	@Override
	public String toString() {
		return "XdefType[base='" + _base + "', params='" + _params.size()+"']";
	}
}