package org.xdef.impl.util.conv.xsd.xsd_1_0.type.domain;

import org.xdef.sys.SUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/** Represents final restriction, that combines all restrictions of a simple
 * type to one restriction.
 * @author Alexandrov
 */
public class FinalRestriction {

	/** Type name constant. */
	private static final short TYPE_NAME = 0;
	/** TypeMethod name constant. */
	private static final short METHOD_NAME = 1;
	/** Length restriction method name constant. */
	private static final short LENGTH_METHOD = 2;
	/** Value restriction method name constant. */
	private static final short VALUE_METHOD = 3;
	/** Contains information about simple types. n/a parameter means that
	 * current type does not support that kind of restriction.
	 * 0 - XML schema base type name.
	 * 1 - XDefinition type validation method name (w/o parameters).
	 * If is empty string - same name as type name.
	 * 2 - name of method, that is adding length restriction for current type.
	 * If is empty string - same method as type method.
	 * 3 - name of method, that is adding value range restriction for current
	 * type. If is empty string - same method as type method.
	 */
	private static final String[][] TYPE_METHODS = new String[][]{
/*VT1*/
		{"anyURI", "anyURI", "string", "n/a"},
/*VT1*/
		{"base64Binary", "base64", "", "n/a"},
		{"boolean", "", "n/a", "n/a"},
/*VT1*/
		{"byte", "byte", "n/a", ""},
/*VT1*/
/*VT1*/
		{"date", "date", "n/a", ""},
/*VT1*/
		{"dateTime", "dateTime", "n/a", ""},
		{"decimal", "dec", "", "int"},
/*VT1*/
		{"double", "double", "n/a", ""},
/*VT1*/
		{"duration", "duration", "n/a", ""},
		{"ENTITIES", "", "n/a", "n/a"},
		{"ENTITY", "", "string", "n/a"},
/*VT1*/
		{"float", "float", "decimal", ""},
/*VT1*/
		{"gDay", "gDay", "n/a", "n/a"},
		{"gMonth", "gMonth", "n/a", "n/a"},
		{"gMonthDay", "gMonthDay", "n/a", "n/a"},
		{"gYear", "gYear", "n/a", "n/a"},
		{"gYearMonth", "gYearMonth", "n/a", "n/a"},
/*VT1*/
		{"hexBinary", "hex", "", "n/a"},
		{"ID", "ID", "ID", "n/a"},
		{"IDREF", "ID", "IDREF", "n/a"},
		{"IDREFS", "ID", "n/a", "n/a"},
/*VT1*/
		{"int", "int", "decimal", ""},
/*VT1*/
		{"integer", "integer", "decimal", ""},
/*VT1*/
		{"language", "language", "string", "n/a"},
		{"long", "long", "decimal", ""},
		{"Name", "string", "", "n/a"},
		{"NCName", "NCname", "string", "n/a"},
		{"negativeInteger", "negativeInteger", "decimal", ""},
		{"NMTOKEN", "NMTOKEN", "string", "n/a"},
		{"NMTOKENS", "", "n/a", "n/a"},
		{"nonNegativeInteger", "nonNegativeInteger", "decimal", ""},
		{"nonPositiveInteger", "nonPositiveInteger", "decimal", ""},
		{"normalizedString", "normString", "string", "n/a"},
		{"NOTATION", "", "n/a", "n/a"},
		{"positiveInteger", "positiveInteger", "decimal", ""},
		{"QName", "Qname", "string", "n/a"},
		{"short", "short", "decimal", ""},
		{"string", "", "", "n/a"},
		{"time", "time", "n/a", "n/a"},
		{"token", "string", "", "n/a"},
		{"unsignedByte", "unsignedByte", "decimal", ""},
		{"unsignedInt", "unsignedInt", "decimal", ""},
		{"unsignedLong", "unsignedLong", "decimal", ""},
		{"unsignedShort", "unsignedShort", "decimal", ""}
	};
	/** Set of enumerations. */
	private HashSet<String> _enumerations = new HashSet<String>();
	/** List of sets of patterns. */
	private final ArrayList<HashSet<String>> _patterns =
		new ArrayList<HashSet<String>>();
	/** Base XML schema type. */
	private final BaseType _base;
	/** Array of restrictions. */
	private final String[] _restrictions = new String[10];

	/** Creates instance of final restriction from a given restriction object
	 * with base XML shema type as base type.
	 * @param restriction object of restriction.
	 */
	public FinalRestriction(Restriction restriction) {
		//adding base type
		_base = (BaseType) restriction.getBase();
		//adding enumeration values
		if (!restriction.getEnumerations().isEmpty()) {
			Iterator i = restriction.getEnumerations().iterator();
			while (i.hasNext()) {
				String string = (String) i.next();
				_enumerations.add(string);
			}
		}
		//adding patterns values
		if (!restriction.getPatterns().isEmpty()) {
			HashSet<String> patterns = new HashSet<String>();
			Iterator i = restriction.getPatterns().iterator();
			while (i.hasNext()) {
				String string = (String) i.next();
				patterns.add(string);
			}
			_patterns.add(patterns);
		}
		System.arraycopy(restriction.getRestrictions(),
			0, _restrictions, 0, restriction.getRestrictions().length);
	}

	/** Returns index of array with methods according to given type.
	 * @param typeName  name of searched type.
	 * @return          index of array in type methods array.
	 */
	private int getIndex(String typeName) {
		for (int i = 0; i < TYPE_METHODS.length; i++) {
			if (TYPE_METHODS[i][TYPE_NAME].equals(typeName)) {
				return i;
			}
		}
		throw new RuntimeException("Could not find given type!");
	}

	/** Returns method name by given type and given method type.
	 * @param typeName      name of type.
	 * @param methodType    type of method.
	 * @return              name of method.
	 */
	private String getMethodName(String typeName, short methodType) {
		if (methodType != METHOD_NAME
			&& methodType != LENGTH_METHOD && methodType != VALUE_METHOD) {
			throw new RuntimeException("Not supported method type!");
		}
		int index = getIndex(typeName);
		if ((methodType == LENGTH_METHOD || methodType == VALUE_METHOD)
			&& !"".equals(TYPE_METHODS[index][methodType])) {
			return TYPE_METHODS[index][methodType];
		} else {
			return getMethodName(index);
		}
	}

	/** Returns name of type method from type methods array by given index.
	 * @param index index of type.
	 * @return      name of type method.
	 */
	private String getMethodName(int index) {
		return "".equals(TYPE_METHODS[index][METHOD_NAME])
			? TYPE_METHODS[index][TYPE_NAME] : TYPE_METHODS[index][METHOD_NAME];
	}

	/** Adding restrictions from derived restriction.
	 * @param restriction   restriction object of derived restriction.
	 */
	public void addRestriction(Restriction restriction) {
		if (!restriction.getEnumerations().isEmpty()) {
			_enumerations = new HashSet<String>();
			Iterator i = restriction.getEnumerations().iterator();
			while (i.hasNext()) {
				String string = (String) i.next();
				_enumerations.add(string);
			}
		}
		//adding patterns values
		if (!restriction.getPatterns().isEmpty()) {
			HashSet<String> patterns = new HashSet<String>();
			Iterator i = restriction.getPatterns().iterator();
			while (i.hasNext()) {
				String string = (String) i.next();
				patterns.add(string);
			}
			_patterns.add(patterns);
		}
		//adding all other restrictions
		for (int i = 0; i < restriction.getRestrictions().length; i++) {
			if (restriction.getRestrictions()[i] != null) {
				_restrictions[i] = restriction.getRestrictions()[i];
			}
		}
	}

	/** Returns string containing type definition according XDefinition rules.
	 * @return  XDefinition style type definition string.
	 */
	public String getTypeMethod() {
		String baseType = _base.getName();
		Expression methodAndRestrictions = new Expression(_base.getMethod());
		boolean isXDMethod = false;
		if (baseType.equals(_base.getMethod().getMethodName())) {
			isXDMethod = true;
			methodAndRestrictions = new Expression((short) 0);
		}
		//resolving type method and restrictions
		//length restriction is present
		if (_restrictions[Restriction.LENGTH] != null) {
			//name of method that restricts length
			String methodName = getMethodName(baseType, LENGTH_METHOD);
			//adding restriction to the restriction method
			methodAndRestrictions.getMethod(methodName).addParameter(
				_restrictions[Restriction.LENGTH]);
			//max or min length restriction is present
		} else if (_restrictions[Restriction.MIN_LENGTH] != null
			|| _restrictions[Restriction.MAX_LENGTH] != null) {
			//name of method that restricts length
			String methodName = getMethodName(baseType, LENGTH_METHOD);
			TypeMethod lengthMethod=methodAndRestrictions.getMethod(methodName);
			lengthMethod.addParameter(_restrictions
				[Restriction.MIN_LENGTH] != null
					? _restrictions[Restriction.MIN_LENGTH] : "0");
			lengthMethod.addParameter(_restrictions[
				Restriction.MAX_LENGTH] != null
					? _restrictions[Restriction.MAX_LENGTH] : "$MAXINT");
		}

		//value restriction is present
		if (_restrictions[Restriction.MIN_EXCLUSIVE] != null
			|| _restrictions[Restriction.MIN_INCLUSIVE] != null
			|| _restrictions[Restriction.MAX_INCLUSIVE] != null
			|| _restrictions[Restriction.MAX_EXCLUSIVE] != null) {
			String methodName = getMethodName(baseType, VALUE_METHOD);
			//method with value restriction
			TypeMethod valueMethod =
				methodAndRestrictions.getMethod(methodName);
			//resolving minimum value
			String min = "float".equals(baseType) || "double".equals(baseType)
				? "$MINFLOAT" : "$MININT";
			if (valueMethod.getParameter(1) != null) {
				min = valueMethod.getParameter(1);
			}
			if (_restrictions[Restriction.MIN_EXCLUSIVE] != null) {
/*VT1*/
				min = _restrictions[Restriction.MIN_EXCLUSIVE];
/*VT1*/
			} else if (_restrictions[Restriction.MIN_INCLUSIVE] != null) {
				_base.getTypeMethod();
				min = _restrictions[Restriction.MIN_INCLUSIVE];
			}
/*VT1*/
			if (_base.getMethod().getValueType() != 'N'
				|| "INF".equals(min) || "-INF".equals(min)) {
				min = '\'' + min + '\'';  // INF is contertyed to string
			}
			if (_restrictions[Restriction.MIN_EXCLUSIVE] != null) {
				min = "%minExclusive=" + min;
			} else {
				min = "%minInclusive=" + min;
			}
/*VT1*/
			//resolving maximum value
			String max = "float".equals(baseType) || "double".equals(baseType)
				? "$MAXFLOAT" : "$MAXINT";
			if (valueMethod.getParameter(2) != null) {
				max = valueMethod.getParameter(2);
			}
/*VT1*/
			if (_restrictions[Restriction.MAX_INCLUSIVE] != null) {
				max = _restrictions[Restriction.MAX_INCLUSIVE];
			} else if (_restrictions[Restriction.MAX_EXCLUSIVE] != null) {
//				max = String.valueOf(Integer.parseInt(
//					_restrictions[Restriction.MAX_EXCLUSIVE]) - 1);
				max = _restrictions[Restriction.MAX_EXCLUSIVE];
			}
			if (_base.getMethod().getValueType() != 'N'
				|| "INF".equals(max) || ("-INF").equals(max)) {
				max = '\'' + max + '\''; // INF is contertyed to string
			}
			if (_restrictions[Restriction.MAX_EXCLUSIVE] != null) {
				max = "%maxExclusive=" + max;
			} else {
				max = "%maxInclusive=" + max;
			}
/*VT1*/
			//setting maximum and minimum value
			if (valueMethod.getParameter(1) != null) {
				valueMethod.setParameter(1, min);
			} else {
				valueMethod.addParameter(min);
			}
			if (valueMethod.getParameter(2) != null) {
				valueMethod.setParameter(2, max);
			} else {
				valueMethod.addParameter(max);
			}
		}
		//resolving digits
		if (_restrictions[Restriction.FRACTION_DIGITS] != null
			|| _restrictions[Restriction.TOTAL_DIGITS] != null) {
			String methodName = getMethodName(baseType, LENGTH_METHOD);
			TypeMethod digitsMethod=methodAndRestrictions.getMethod(methodName);
			if (_restrictions[Restriction.TOTAL_DIGITS] != null
				&& _restrictions[Restriction.FRACTION_DIGITS] == null) {
				digitsMethod.addParameter(
					_restrictions[Restriction.TOTAL_DIGITS]);
			} else if (_restrictions[Restriction.TOTAL_DIGITS] == null
				&& _restrictions[Restriction.FRACTION_DIGITS] != null) {
				digitsMethod.addParameter("$MAXINT");
				digitsMethod.addParameter(
					_restrictions[Restriction.FRACTION_DIGITS]);
			} else {
				digitsMethod.addParameter(
					_restrictions[Restriction.TOTAL_DIGITS]);
				digitsMethod.addParameter(
					_restrictions[Restriction.FRACTION_DIGITS]);
			}
		}
		String ret = methodAndRestrictions.toString();
		if (ret.isEmpty()) {
			ret = getMethodName(baseType, VALUE_METHOD);
			ret = ret.isEmpty() || "n/a".equals(ret) ? "" : (ret + "()");
		}
		//resolving patterns
		if (!_patterns.isEmpty()) {
/*VT2*/
			ArrayList<Expression> patternsList = new ArrayList<Expression>();
			String xdParams = isXDMethod && ret.endsWith(")") ? "" : null;
			Iterator i = _patterns.iterator();
			while (i.hasNext()) {
				HashSet patternSet = (HashSet) i.next();
				Expression patterns =
					xdParams == null ? new Expression(Expression.OR) : null;
				Iterator it = patternSet.iterator();
				while (it.hasNext()) {
					String string = "'" +
						SUtils.modifyString((String) it.next(), "'", "\\'")+"'";
					if (xdParams != null) {
						if (xdParams.isEmpty()) {
							xdParams = "%pattern=[";
						} else {
							xdParams += ", ";
						}
						xdParams+= SUtils.modifyString(string,"\\","\\\\");
					} else {
						patterns.addMethod(
							new TypeMethod("regex", 'A', new String[]{string}));
						patternsList.add(patterns);
					}
				}
			}
			if (xdParams != null) {
				ret = ret.substring(0, ret.length() - 1);
				if (!ret.endsWith("(")) {
					ret += ", ";
				}
				ret += xdParams + "])";
			} else {
				if (!ret.isEmpty()) {
					ret += " & ";
				}
				ret += (patternsList.size() > 1 ? "(" : "");
				Iterator it = patternsList.iterator();
				while (it.hasNext()) {
					Expression expression = (Expression) it.next();
					ret += expression.toString();
				}
				ret += patternsList.size() > 1 ? ")" : "";
			}
		}
		//adding enumeration values is present
		if (!_enumerations.isEmpty()) {
			String xdParams = isXDMethod && ret.endsWith(")") ? "" : null;
//			String patts = null;
			TypeMethod enumerations = xdParams == null
				? new TypeMethod("enum",'A',new String[0]) : null;
			Iterator i = _enumerations.iterator();
			while (i.hasNext()) {
				String enumeration = (String) i.next();
				if (xdParams != null) {
					if (xdParams.isEmpty()) {
						xdParams = "%enumeration=[";
					} else {
						xdParams += xdParams += ", ";
					}
					if (_base.getMethod() != null &&
						_base.getMethod().getValueType() != 'N') {
						enumeration = "'" + enumeration + "'";
					}
					xdParams += enumeration;
				} else {
					enumerations.addParameter('"' + enumeration + '"');
				}
			}
			if (xdParams != null) {
				ret = ret.substring(0, ret.length() - 1);
				if (!ret.endsWith("(")) {
					ret += ", ";
				}
				ret += xdParams + "])";
			} else {
				if (!ret.isEmpty()) {
					ret += " & ";
				}
				ret += enumerations.toString();
			}
/*VT2*/
		}
		return ret;
	}
}