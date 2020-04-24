package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.sys.SUtils;
import java.util.ArrayList;

/** Represents type method
 * @author Alexandrov
 */
public class TypeMethod {
	/** Name of method. */
	private final String _methodName;
	/** List of parameters of method. */
	private final java.util.List<String> _parameters = new ArrayList<String>();
	/** Type of value ('B' boolean, 'D' date/time), 'N' numeric,
	 * 'U' duration, 'X' hex, base64).*/
	private final char _valueType; /*VT1*/

	/** Creates instance of method with given name and parameters.
	 * @param methodName    name of method.
	 * @param parameters    list of parameters.
	 * @param valueType     Type of value ('B' boolean, 'D' date/time),
	 * 'N' numeric, 'U' duration, 'X' hex, base64).
	 */
	public TypeMethod(String methodName,
		char valueType,
		String... parameters) {
		_methodName = methodName;
		_valueType = valueType;
		for (int i = 0; i < parameters.length; i++) {
			String string = parameters[i];
			_parameters.add(string);
		}
	}

	/** Gets method name.
	 * @return  name of method.
	 */
	public String getMethodName() {return _methodName;}

	/** Gets method parameter at given position or <tt>null</tt> if position
	 * is not in bounds of parameters list.
	 * @param position  position of parameter.
	 * @return          parameter value.
	 */
	public String getParameter(int position) {
		position--;
		return position < _parameters.size() ? _parameters.get(position ): null;
	}

	/** Sets parameter to current method at given position with given value.
	 * @param position  position of parameter.
	 * @param value     parameter value.
	 */
	public void setParameter(int position, String value) {
		position--;
		if (position < _parameters.size()) {
			_parameters.set(position, value);
			return;
		}
		throw new RuntimeException("Parameter error: index out of bounds: "
			+ _methodName + "; " + position + ", value=" + value);
	}

/*VT1*/
	/** Get type of value.
	 * @return 'N' if parsed value is numeric or  otherwise 'A'.
	 */
	public char getValueType() {return _valueType;}
/*VT1*/

	/** Adds parameter with given value at the end of parameters list.
	 * @param value parameter value.
	 */
	public void addParameter(String value) {_parameters.add(value);}

	@Override
	public String toString() {
		String ret = _methodName + "(";
		for (int i = 0; i < _parameters.size(); i++) {
			String parameter = _parameters.get(i);
			if (parameter.indexOf("\\") >= 0) {
				parameter = SUtils.modifyString(parameter, "\\", "\\\\");
			}
			ret += (i == 0 ? "" : ", ") + parameter;
		}
		ret += ")";
		return ret;
	}
}