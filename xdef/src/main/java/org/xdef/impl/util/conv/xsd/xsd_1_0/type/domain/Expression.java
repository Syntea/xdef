package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import java.util.ArrayList;
import java.util.Iterator;

/** Represents expression, that contains one or more methods, joined by specified operand
 * (&amp; - AND as default).
 * @author Alexandrov
 */
public class Expression {

	/** Constant for identifying AND operator. */
	public static final short AND = 1;
	/** Constant for identifying OR operator. */
	public static final short OR = 2;
	/** Variable for operand. */
	private short _operand;
	/** List of methods. */
	private final java.util.List<TypeMethod> _methods;

	/** Creates instance of expression with no methods. Methods added later
	 * are joined by given operator.
	 * @param operand   operand for joining methods.
	 */
	public Expression(short operand) {
		_methods = new ArrayList<TypeMethod>();
		_operand = operand;
	}

	/** Creates instance of expression with given method. All methods are joined
	 * by the default operand (AND).
	 * @param method method in expression.
	 */
	public Expression(TypeMethod method) {
		this._methods = new ArrayList<TypeMethod>();
		_methods.add(method);
		_operand = AND;
	}

	/** Creates instance of expression with given method and given operand.
	 * @param method    method in expression.
	 * @param operand   operand in expression.
	 */
	public Expression(TypeMethod method, short operand) {
		this._methods = new ArrayList<TypeMethod>();
		_methods.add(method);
		_operand = operand;
	}

	/** Returns first method with given name if it is found or creates method
	 * with given name, adds it to method list and returns it.
	 * @param methodName    name of method.
	 * @return              instance of method.
	 */
	public TypeMethod getMethod(String methodName) {
		Iterator<TypeMethod> i = _methods.iterator();
		while (i.hasNext()) {
			TypeMethod method = i.next();
			if (method.getMethodName().equals(methodName)) {
				return method;
			}
		}
//		TypeMethod newMethod = new TypeMethod(methodName, 'A', new String[0]);
/*VT1*/
		TypeMethod newMethod = new BaseType(methodName).getMethod();
/*VT1*/
		_methods.add(newMethod);
		return newMethod;
	}

	/** Adds method to method list.
	 * @param method    instance of method.
	 */
	public void addMethod(TypeMethod method) {_methods.add(method);}

	/** Returns operand that had been set.
	 * @return  operand identifier.
	 */
	public short getOperand() {return _operand;}

	/** Sets operand for current expression.
	 * @param operand   operand identifier.
	 */
	public void setOperand(short operand) {_operand = operand;}

	@Override
	public String toString() {
		String ret = _methods.size() > 1 ? "(" : "";
		String operand = (_operand == AND ? " & " : " | ");
		for (int i = 0; i < _methods.size(); i++) {
			ret += (i == 0 ? "" : operand) + _methods.get(i).toString();
		}
		ret += _methods.size() > 1 ? ")" : "";
		return ret;
	}
}