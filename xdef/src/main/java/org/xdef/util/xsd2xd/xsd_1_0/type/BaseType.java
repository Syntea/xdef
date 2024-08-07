package org.xdef.util.xsd2xd.xsd_1_0.type;

/** Represents base XML schema type.
 * @author Ilia Alexandrov
 */
public class BaseType extends Type {

	/** TypeMethod representation of base type. */
	private final TypeMethod _method;

	/** Creates instance of base XML schema type according given name.
	 * @param name  name of base type.
	 */
	public BaseType(String name) {
		_name = name;
		if ("anySimpleType".equals(name)) {
			_method = new TypeMethod("string", 'A',new String[]{"0","$MAXINT"});
		} else if ("anyURI".equals(name)) {
			_method = new TypeMethod("uri", 'A', new String[0]);
		} else if ("boolean".equals(name)) {
			_method = new TypeMethod("boolean", 'B', new String[0]);
		} else if ("byte".equals(name)) {
			_method = new TypeMethod("byte", 'N', new String[0]);
		} else if ("date".equals(name)) {
			_method = new TypeMethod("date", 'D', new String[0]);
		} else if ("dateTime".equals(name)) {
			_method = new TypeMethod("dateTime", 'D', new String[0]);
		} else if ("decimal".equals(name)) {
			_method = new TypeMethod("dec", 'N', new String[0]);
		} else if ("double".equals(name)) {
			_method = new TypeMethod("float", 'N', new String[0]);
		} else if ("duration".equals(name)) {
			_method = new TypeMethod("duration", 'U', new String[0]);
		} else if ("ENTITIES".equals(name)) {
			_method = new TypeMethod("ENTITIES", 'A', new String[0]);
		} else if ("ENTITY".equals(name)) {
			_method = new TypeMethod("ENTITY", 'A', new String[0]);
		} else if ("float".equals(name)) {
			_method = new TypeMethod("float", 'N', new String[0]);
		} else if ("gDay".equals(name)) {
			_method = new TypeMethod("gDay", 'D', new String[0]);
		} else if ("gMonth".equals(name)) {
			_method = new TypeMethod("gMonth", 'D', new String[0]);
		} else if ("gMonthDay".equals(name)) {
			_method = new TypeMethod("gMonthDay", 'D', new String[0]);
		} else if ("gYear".equals(_name)) {
			_method = new TypeMethod("gYear", 'D', new String[0]);
		} else if ("gYearMonth".equals(_name)) {
			_method = new TypeMethod("gYearMonth", 'D', new String[0]);
		} else if ("base64Binary".equals(_name)) {
			_method = new TypeMethod("base64Binary", 'X', new String[0]);
		} else if ("hexBinary".equals(_name)) {
			_method = new TypeMethod("hexBinary", 'X', new String[0]);
		} else if ("ID".equals(_name)) {
			_method = new TypeMethod("ID", 'A', new String[0]);
		} else if ("IDREF".equals(_name)) {
			_method = new TypeMethod("IDREF", 'A', new String[0]);
		} else if ("IDREFS".equals(_name)) {
			_method = new TypeMethod("IDREFS", 'A', new String[0]);
		} else if ("int".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[0]);
		} else if ("integer".equals(_name)) {
			_method = new TypeMethod("integer", 'N', new String[0]);
		} else if ("language".equals(_name)) {
			_method = new TypeMethod("language", 'A', new String[0]);
		} else if ("long".equals(_name)) {
			_method = new TypeMethod("long", 'N', new String[0]);
		} else if ("Name".equals(_name)) {
			_method = new TypeMethod("string", 'A', new String[0]);
		} else if ("NCName".equals(_name)) {
			_method = new TypeMethod("NCName", 'A', new String[0]);
		} else if ("negativeInteger".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"$MININT", "-1"});
		} else if ("NMTOKEN".equals(_name)) {
			_method = new TypeMethod("NMTOKEN", 'A', new String[0]);
		} else if ("NMTOKENS".equals(_name)) {
			_method = new TypeMethod("NMTOKENS", 'A', new String[0]);
		} else if ("nonNegativeInteger".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"0", "$MAXINT"});
		} else if ("nonPositiveInteger".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"$MININT", "0"});
		} else if ("normalizedString".equals(_name)) {
			_method = new TypeMethod("normalizedString", 'A', new String[0]);
		} else if ("NOTATION".equals(_name)) {
			_method = new TypeMethod("NOTATION", 'A',  new String[0]);
		} else if ("positiveInteger".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"1", "$MAXINT"});
		} else if ("QName".equals(_name)) {
			_method = new TypeMethod("QName", 'A',  new String[0]);
		} else if ("short".equals(_name)) {
			_method = new TypeMethod("short",
				'N', new String[]{"-32768", "32767"});
		} else if ("string".equals(_name)) {
			_method = new TypeMethod("string", 'A',  new String[0]);
		} else if ("time".equals(_name)) {
			_method = new TypeMethod("time", 'D',  new String[0]);
		} else if ("token".equals(_name)) {
			_method = new TypeMethod("token", 'A',  new String[0]);
		} else if ("unsignedByte".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"0", "255"});
		} else if ("unsignedInt".equals(_name)) {
			_method = new TypeMethod("int", 'N',new String[]{"0","4294967295"});
		} else if ("unsignedLong".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"0", "$MAXINT"});
		} else if ("unsignedShort".equals(_name)) {
			_method = new TypeMethod("int", 'N', new String[]{"0", "65535"});
		} else {
			_method = new TypeMethod(name, 'A', new String[0]);
		}
	}

	/** Gets method representation of given type.
	 * @return method representation of given type.
	 */
	public TypeMethod getMethod() {return _method;}

	@Override
	public String getTypeMethod() {return _method.toString();}

	@Override
	public String toString() {return "BaseType [" + _name + "]";}
}