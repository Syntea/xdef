package org.xdef.component;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.xml.namespace.QName;
import static org.xdef.XDConstants.LINE_SEPARATOR;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_ANYURI;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTE;
import static org.xdef.XDValueID.XD_BYTES;
import static org.xdef.XDValueID.XD_CHAR;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_CURRENCY;
import static org.xdef.XDValueID.XD_DATETIME;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_DURATION;
import static org.xdef.XDValueID.XD_EMAIL;
import static org.xdef.XDValueID.XD_FLOAT;
import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueID.XD_INT;
import static org.xdef.XDValueID.XD_IPADDR;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_NULL;
import static org.xdef.XDValueID.XD_NUMBER;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PRICE;
import static org.xdef.XDValueID.XD_SHORT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_TELEPHONE;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMData;
import static org.xdef.model.XMNode.XMELEMENT;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;

/** Methods for generation Java source code of getters/setters.
 * @author Vaclav Trojan
 */
class XCGeneratorBase {
	/** Platform-dependent newline. */
	static final String LN = LINE_SEPARATOR;
	/** Names that can't be used in generated code.*/
	static final Set<String> RESERVED_NAMES = new HashSet<>();
	/** Switch to generate JavaDoc. */
	final boolean _genJavadoc;
	/** Internal reporter where to write messages. */
	final ArrayReporter _reporter;
	/** Map with binds information. */
	final Map<String, String> _binds;
	/** XDPool from which components are generated. */
	final XDPool _xp;

	/** Switch if byte array is encoded as base64 (1) or hexadecimal (2).*/
	byte _byteArrayEncoding;
	/** Map with components information. */
	Map<String, XComponentInfo> _components;
	/** Builder to generate interface or null. */
	StringBuilder _interfaces; // where to create interface.

	static {
//////////////////////////////////////////////////////////////////////////////////
//// Names that can't be used in generated code
//////////////////////////////////////////////////////////////////////////////////
		// Java keywords
		RESERVED_NAMES.add("abstract");
		RESERVED_NAMES.add("assert");
		RESERVED_NAMES.add("break");
		RESERVED_NAMES.add("case");
		RESERVED_NAMES.add("catch");
		RESERVED_NAMES.add("class");
		RESERVED_NAMES.add("continue");
		RESERVED_NAMES.add("do");
		RESERVED_NAMES.add("else");
		RESERVED_NAMES.add("enum");
		RESERVED_NAMES.add("extends");
		RESERVED_NAMES.add("final");
		RESERVED_NAMES.add("for");
		RESERVED_NAMES.add("goto");
		RESERVED_NAMES.add("if");
		RESERVED_NAMES.add("instanceof");
		RESERVED_NAMES.add("new");
		RESERVED_NAMES.add("private");
		RESERVED_NAMES.add("protected");
		RESERVED_NAMES.add("public");
		RESERVED_NAMES.add("static");
		RESERVED_NAMES.add("switch");
		RESERVED_NAMES.add("synchronized");
		RESERVED_NAMES.add("throw");
		RESERVED_NAMES.add("throws");
		RESERVED_NAMES.add("transient");
		RESERVED_NAMES.add("try");
		RESERVED_NAMES.add("void");
		RESERVED_NAMES.add("while");
		// Literals
		RESERVED_NAMES.add("false");
		RESERVED_NAMES.add("true");
		RESERVED_NAMES.add("null");
		// Primitive type names
		RESERVED_NAMES.add("boolean");
		RESERVED_NAMES.add("byte");
		RESERVED_NAMES.add("char");
		RESERVED_NAMES.add("double");
		RESERVED_NAMES.add("float");
		RESERVED_NAMES.add("int");
		RESERVED_NAMES.add("long");
		RESERVED_NAMES.add("short");
		// Class names (both unqualified and qualified)
		RESERVED_NAMES.add("Boolean");
		RESERVED_NAMES.add("java.lang.Boolean");
		RESERVED_NAMES.add("Byte");
		RESERVED_NAMES.add("java.lang.Byte");
		RESERVED_NAMES.add("Character");
		RESERVED_NAMES.add("java.lang.Character");
		RESERVED_NAMES.add("Class");
		RESERVED_NAMES.add("java.lang.Class");
		RESERVED_NAMES.add("Double");
		RESERVED_NAMES.add("java.lang.Double");
		RESERVED_NAMES.add("Float");
		RESERVED_NAMES.add("java.lang.Float");
		RESERVED_NAMES.add("Integer");
		RESERVED_NAMES.add("java.lang.Integer");
		RESERVED_NAMES.add("Long");
		RESERVED_NAMES.add("java.lang.Long");
		RESERVED_NAMES.add("Object");
		RESERVED_NAMES.add("java.lang.Object");
		RESERVED_NAMES.add("Short");
		RESERVED_NAMES.add("java.lang.Short");
		RESERVED_NAMES.add("String");
		RESERVED_NAMES.add("java.lang.String");
		RESERVED_NAMES.add("StringBuffer");
		RESERVED_NAMES.add("java.lang.StringBuffer");
		RESERVED_NAMES.add("StringBuilder");
		RESERVED_NAMES.add("java.lang.StringBuilder");
		RESERVED_NAMES.add("java.lang.Number");
		// Qualified names used in code generation
		RESERVED_NAMES.add("java.math.BigDecimal");
		RESERVED_NAMES.add("java.sql.Timestamp");
		RESERVED_NAMES.add("java.util.ArrayList");
		RESERVED_NAMES.add("java.util.Calendar");
		RESERVED_NAMES.add("java.util.Date");
		RESERVED_NAMES.add("java.util.GregorianCalendar");
		RESERVED_NAMES.add("java.util.List");
		RESERVED_NAMES.add("java.util.Map");
		RESERVED_NAMES.add("javax.xml.datatype.Duration");
		RESERVED_NAMES.add("javax.xml.datatype.XMLGregorianCalendar");
		//org.w3c.dom names
		RESERVED_NAMES.add("org.w3c.dom.Attr");
		RESERVED_NAMES.add("org.w3c.dom.Document");
		RESERVED_NAMES.add("org.w3c.dom.Element");
		RESERVED_NAMES.add("org.w3c.dom.Node");
		// X-definition names
		RESERVED_NAMES.add("org.xdef.msg.XDEF");
		RESERVED_NAMES.add("org.xdef.sys.SDatetime");
		RESERVED_NAMES.add("org.xdef.sys.SDuration");
		RESERVED_NAMES.add("org.xdef.xml.KXmlUtils");
		RESERVED_NAMES.add("org.xdef.component.XComponent");
		RESERVED_NAMES.add("org.xdef.component.XComponentUtil");
		RESERVED_NAMES.add("org.xdef.XDParseResult");
		RESERVED_NAMES.add("org.xdef.proc.XXNode");
		RESERVED_NAMES.add("org.xdef.sys.SUtils");
		RESERVED_NAMES.add("org.xdef.sys.SException");
		RESERVED_NAMES.add("org.xdef.sys.SRuntimeException");
	}

	XCGeneratorBase(final XDPool xp,
		final ArrayReporter reporter,
		final boolean genJavadoc) {
		_xp = xp;
		_reporter = reporter;
		_binds = xp.getXComponentBinds();
		_genJavadoc = genJavadoc;
	}

	/** Replace all occurrences of the key in the source by the value.
	 * @param source string source.
	 * @param replacements array of tuples of key an replacement. in the source.
	 * @return modified string.
	 */
	final static String modify(final String origin, final String... replace) {
		if (replace.length % 2 != 0) {
			throw new SIllegalArgumentException(SYS.SYS066,  //Internal error&{0}{: }
				"Length of array of keys and replacemetns differs");
		}
		String result = origin;
		for (int i = 0; i < replace.length; i+=2) {
			result = SUtils.modifyString(result, replace[i], replace[i+1]);
		}
		return result;
	}

	/** Check if this type is declared enumeration.
	 * @param xdata Model of data.
	 * @return fully qualified name of enumeration or null.
	 */
	private static String checkEnumType(final XMData xdata) {
		XDValue x = xdata.getParseMethod();
		if (x != null && x instanceof XDParser) {
			String typeName = ((XDParser) x).getDeclaredName();
			Map<String, String> enums = xdata.getXDPool().getXComponentEnums();
			if (typeName == null || enums == null || enums.isEmpty()) {
				return null;  // not declared enumeration
			}
			String enumName = enums.get(typeName);
			if (enumName == null) {
				return null;  // not declared enumeration
			}
			// we remove names of items.
			int ndx = enumName.indexOf(' ');
			if (ndx > 0) {
				enumName = enumName.substring(0, ndx);
			}
			// now we remove leading "%" (i.e. recerence flag)
			return enumName.indexOf('%') == 0 ? /* is reference */ enumName.substring(1) : enumName;
		}
		return null;
	}
	private static String getJavaType(final short type) {
		switch (type) {
			case XD_BOOLEAN: return "Boolean";
			case XD_CHAR: return "Character";
			case XD_BYTE: return "Byte";
			case XD_SHORT: return "Short";
			case XD_INT: return "Integer";
			case XD_LONG: return "Long";
			case XD_BIGINTEGER: return "java.math.BigInteger";
			case XD_FLOAT: return "Float";
			case XD_DOUBLE: return "Double";
			case XD_NUMBER: return "Number";
			case XD_DECIMAL: return "java.math.BigDecimal";
			case XD_DURATION: return "org.xdef.sys.SDuration";
			case XD_DATETIME: return "org.xdef.sys.SDatetime";
			case XD_GPSPOSITION: return "org.xdef.sys.GPSPosition";
			case XD_PRICE: return "org.xdef.sys.Price";
			case XD_ANYURI: return "java.net.URI";
			case XD_EMAIL: return "org.xdef.XDEmailAddr";
			case XD_CURRENCY: return "java.util.Currency";
			case XD_IPADDR: return "java.net.InetAddress";
			case XD_BYTES: return "byte[]";
			case XD_TELEPHONE: return "org.xdef.XDTelephone";
			case XD_NULL: //jnull
			case XD_ANY:
			case XD_CONTAINER: return "Object";
		}
		return "String"; // default
	}

	/** Get Java Object name corresponding to XD type.
	 * @param xdata XMData object.
	 * @return Java Object name corresponding to XD type
	 */
	final String getJavaObjectTypeName(final XMData xdata) {
		String result = checkEnumType(xdata);
		if (result != null) {
			return result;
		}
		String parserName = xdata.getParserName();
		short type = xdata.getParserType();
		if (type == XD_BYTES) {
			_byteArrayEncoding |= "base64Binary".equals(parserName) ? 1 : 2;
		}
		if (type == XD_CONTAINER) {
			return "java.util.List<" + getJavaType(xdata.getAlltemsType()) + ">";
		} else if (type == XDValue.XD_ANY) {
			type = xdata.getAlltemsType();
		}
		return getJavaType(type);
	}

	/** Create ParsedResultGetter.
	 * @param xdata XData model.
	 * @return getter of ParsedResult.
	 */
	final static String getParsedResultGetter(final XMData xdata) {
		String result = "value.getParsedValue().isNull()? null: value.getParsedValue().";
		String enumType = checkEnumType(xdata);
		if (enumType != null) {
			return enumType+".toEnum("+ result+"toString())";
		}
		if ("jlist".equals(xdata.getParserName())) {
			return "org.xdef.component.XComponentUtil.parseResultToList(value)";
		}
		short type = xdata.getParserType();
		if (type == XD_CONTAINER) {
			return "(java.util.List<" + getJavaType(xdata.getAlltemsType())
				+ ">) org.xdef.component.XComponentUtil.valueToList(value, "
				+ ((XDParser)xdata.getParseMethod()).getAlltemsType() + ")";
		}
		if (type == XDValue.XD_ANY) {
			type = xdata.getAlltemsType();
		}
		switch (type) {
			case XD_BOOLEAN: return result + "booleanValue()";
			case XD_CHAR: return result + "charValue()";
			case XD_BYTE: return result + "byteValue()";
			case XD_SHORT: return result + "shortValue()";
			case XD_INT: return result + "intValue()";
			case XD_LONG: return result + "longValue()";
			case XD_BIGINTEGER: return result + "integerValue()";
			case XD_FLOAT: return result + "floatValue()";
			case XD_DOUBLE: return result + "doubleValue()";
			case XD_DECIMAL: return result + "decimalValue()";
			case XD_NUMBER: return "(Number)(" + result + "getObject())";
			case XD_DURATION: return result + "durationValue()";
			case XD_DATETIME: return result + "datetimeValue()";
			case XD_GPSPOSITION: return "(org.xdef.sys.GPSPosition)(" + result + "getObject())";
			case XD_PRICE: return "(org.xdef.sys.Price)(" + result + "getObject())";
			case XD_ANYURI: return "(java.net.URI)(" + result + "getObject())";
			case XD_EMAIL: return "(org.xdef.XDEmailAddr)(" + result + "getObject())";
			case XD_CURRENCY: return "(java.util.Currency)(" + result + "getObject())";
			case XD_IPADDR: return "(java.net.InetAddress)(" + result + "getObject())";
			case XD_BYTES: return result + "getBytes()";
			case XD_TELEPHONE: return "(org.xdef.XDTelephone)(" + result + "getObject())";
			case XD_PARSER: return "value.getParsedString()";
			case XD_NULL: //jnull
			case XD_ANY: return "value.getParsedValue().getObject()";
		}
		return result + "toString()";
	}

	/** Generate declaration of variable of attribute name.
	 * @param name name of variable.
	 * @param xdata XData model of data.
	 * @param sb String builder where the code is generated.
	 */
	final void genAttrNameVariable(final String name, final XData xdata, final StringBuilder sb) {
		String modelName = XComponentUtil.xmlToJavaName(xdata.getName());
		sb.append(modify(
(_genJavadoc ? "\t/** Name of attribute &{name} in data\".*/"+LN : "") +
"\tprivate String XD_Name_&{name}=\"&{name1}\";"+LN,
			"&{name}", name,
			"&{name1}", modelName));
	}

	/** Generate declaration of variable as Java object from child element.
	 * @param xdata data model.
	 * @param typeName name of child element Java class.
	 * @param name name of variable.
	 * @param descr JavaDoc description.
	 * @param sb String builder where the code is generated.
	 */
	final void genVariableFromModel(final XData xdata,
		final String typeName,
		final String name,
		int max,
		final String descr,
		final StringBuilder sb) {
		String d = descr;
		String x;
		String typ;
		if (max > 1) {
			d += 's';
			x = "=new java.util.ArrayList<>()";
			typ = "java.util.List<" + typeName + ">";
		} else {
			x = "";
			typ = typeName;
		}
		sb.append(modify(
(_genJavadoc ? "\t/** Value of &{d} \"&{xmlName}\".*/"+LN : "")+
"\tprivate" + (max > 1?" final":"") + " &{typ} _&{name}&{x}",
			"&{d}", d,
			"&{xmlName}", name.replace('$', ':'),
			"&{typ}", typ,
			"&{x}", x,
			"&{name}", name));
		XDValue xfixed = xdata != null ? xdata.getFixedValue() : null;
		if (xfixed != null && xfixed.getItemId() == XD_STRING) {
			sb.append("=\"").append(xfixed.toString()).append('"');
		}
		sb.append(';').append(LN);
	}

	final void genBaseVarsGettersSetters(final XData xdata,
		final String name,
		final int max,
		final String descr,
		final StringBuilder vars,
		final StringBuilder getters,
		final StringBuilder setters,
		final StringBuilder xpathes,
		final StringBuilder sbi) {
		final String typ = getJavaObjectTypeName(xdata);
		genVariableFromModel(xdata,typ,name,max,descr,vars);
		genGetterMethodFromChildElement(xdata,typ,name,max,descr,getters,sbi);
		genSetterMethodOfChildElement(
			typ, name, max, null, null, null, descr, setters, sbi, "");
		// gen "xposOf" method
		if (sbi != null) {
			xpathes.append("\t@Override").append(LN);
			sbi.append(modify(
(_genJavadoc ? ("\t/** Get XPath position of &{descr} \"&{name}\"."+LN+
"\t* @return string with XPath position."+LN+
"\t */"+LN) : "")+
"\tpublic String xposOf&{name}();"+LN,
				"&{name}", name,
				"&{descr}", descr));
		}
		final String x = "attribute".equals(descr) ? "@" + name : "$text";
		xpathes.append(modify(
(_genJavadoc ? ("\t/** Get XPath position of \"&{descr}\".*/"+LN) : "")+
"\tpublic String xposOf&{name}(){return XD_XPos+\"/&{x}\";}"+LN,
			"&{name}", name,
			"&{x}", x,
			"&{descr}", descr));
	}

	/** Generate java code of getter method for child element classes.
	 * @param xel node from which to generate methods.
	 * @param className name of class representing the child element.
	 * @param name name of variable.
	 * @param max maximal occurrence number of items.
	 * @param descr Description text.
	 * @param getters String builder where generate getters.
	 * @param setters String builder where generate setters.
	 * @param sbi String builder where the code is generated for interface.
	 * @return generated code.
	 */
	final void genChildElementGetterSetter(final XElement xel,
		final String className,
		final String name,
		final int max,
		final String descr,
		final StringBuilder getters,
		final StringBuilder setters,
		final StringBuilder sbi,
		final String nullChoice) {
		genGetterMethodFromChildElement(xel,
			className, name, max, descr, getters, sbi);
		String mname = null;
		String mURI = null;
		String mXDPos = null;
		if (xel.isReference()) {
			mname = xel.getName();
			mURI = xel.getNSUri();
			mXDPos = xel.getXDPosition();
		}
		genSetterMethodOfChildElement(className, name, max,
			mname, mURI, mXDPos, descr, setters, sbi, nullChoice);
	}

	/** Generate java code of getter method for child element classes.
	 * @param xn model of actual node.
	 * @param typeName name of class representing the child element.
	 * @param typeName name of class representing the child element.
	 * @param name name of variable.
	 * @param max maximal number of items .
	 * @param descr Description text.
	 * @param sb String builder where to generate code.
	 * @param sbi String builder where the code is generated for interface.
	 * @return generated code.
	 */
	private void genGetterMethodFromChildElement(final XNode xn,
		final String typeName,
		final String name,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		String publ = "public";
		final int ndx = typeName.lastIndexOf('.');
		if (ndx == 0) {
			throw new SRuntimeException(SYS.SYS066,// Internal error&{0}{: }
				"Error in getter: " + xn.getXDPosition());
		}
		String d = descr;
		String typ = typeName;
		if (max > 1) {
			typ = "java.util.List<"+typ+">";
			d += 's';
		}
		final String xmlName = xn.getQName().getLocalPart();
		if (sbi != null) {
			sb.append("\t@Override").append(LN);
			if (typ.startsWith("java.util.List<")) {
				sbi.append(modify(
(_genJavadoc ? "\t/** Get list of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\t"+publ+" &{typ} listOf&{name}();"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
			} else {
				sbi.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\t"+publ+" &{typ} get&{name}();"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
				if (typeName.contains("org.xdef.sys.SDatetime")) {
					// datetime getters
					sb.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\" as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.util.Date dateOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.sql.Timestamp timestampOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.util.Calendar calendarOf&{name}();"+LN,
						"&{xmlName}", xmlName,
						"&{d}" , d,
						"&{name}", name));
				}
			}
		}
		if (ndx > 0) {
			typ = (max > 1) ? "java.util.List<" + typeName + ">" : typeName;
		}
		if (typ.startsWith("java.util.List<") && max > 1) {
			sb.append(modify(
(_genJavadoc ? "\t/** Get list of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\t"+publ+" &{typ} listOf&{name}() {"+LN+"\t\treturn _&{name};"+LN+"\t}"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
		} else {
			sb.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\t"+publ+" &{typ} get&{name}() {return _&{name};}"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
			if (typeName.contains("org.xdef.sys.SDatetime")) {
				// datetime getters
				sb.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\" as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.util.Date dateOf&{name}(){"+
"return org.xdef.sys.SDatetime.getDate(_&{name});}"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.sql.Timestamp timestampOf&{name}(){"+
"return org.xdef.sys.SDatetime.getTimestamp(_&{name});}"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\t"+publ+" java.util.Calendar calendarOf&{name}(){"+
"return org.xdef.sys.SDatetime.getCalendar(_&{name});}"+LN,
					"&{xmlName}", xmlName,
					"&{d}" , d,
					"&{name}", name));
			}
		}
	}

	/** Generate java code of setter method for child element classes.
	 * @param className name typ (class etc).
	 * @param name name of variable.
	 * @param modelName if the node references other model of node
	 * @param modelURI if the node references other model of node
	 * @param modelXDPos if the node references other model of node
	 * @param descr Description text.
	 * @param sb String builder where to generate code.
	 * @param sbi String builder where the code is generated for interface.
	 * @param nullchoice the command to set all variables of choice to null or the empty string.
	 */
	private void genSetterMethodOfChildElement(
		final String className,
		final String name,
		final int max,
		final String modelName,
		final String modelURI,
		final String modelXDPos,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi,
		final String nullChoice) {
		String publ = "public";
		String x;
		String d = descr;
		String clearChoice = "";
		if (!nullChoice.isEmpty()) {
			for (String s : nullChoice.split(";")) {
				if (!((name+"=null").equals(s) || (name+".clear()").equals(s))){
					clearChoice += '_' + s + ';';
				}
			}
			if (!clearChoice.isEmpty()) {
				clearChoice = "\t\t" + clearChoice ;
			}
		}
		if (max > 1) {
			d += 's';
			if (modelName != null) {
				x = LN +
"\t\tif (x!=null) {"+LN +
"\t\t\t\tif (x.xGetXPos()==null)"+LN +
"\t\t\t\t\tx.xInit(this, \""+modelName+"\", "
				+ (modelURI != null ? '"' + modelURI + '"' : "null") + ", \"" + modelXDPos + "\");"+LN
				+ "\t\t\t_&{name}.add(x);"+LN+"\t\t}"+LN+'\t';
			} else {
				x = LN+"\t\tif (x!=null) _&{name}.add(x);"+LN+'\t';
			}
		} else {
			if (modelName != null) {
				x = LN + (clearChoice.isEmpty() ? "" : clearChoice + LN) +
"\t\tif (x!=null && x.xGetXPos() == null)"+LN +
"\t\t\tx.xInit(this, \""+modelName+"\", " + (modelURI != null ? '"' + modelURI + '"' : "null")
				+ ", \"" + modelXDPos + "\");"+LN
				+ "\t\t_&{name}=x;"+LN+"\t";
			} else {
				x = (clearChoice.isEmpty() ? "_&{name}=x;"
					: (LN + clearChoice + LN+"\t\t_&{name}=x;"+LN + "\t"));
			}
		}
		if (sbi != null) {
			sb.append("\t@Override").append(LN);
			if (max > 1) {
				String template =
(_genJavadoc ? ("\t/** Add value to list of \"&{xmlName}\"."+LN+
"\t * @param x value to added."+LN+
"\t */"+LN) : "")+
"\t"+publ+" void add&{name}(&{typ} x);"+LN;
				sbi.append(modify(template,
					"&{name}", name,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", className));
				if ("org.xdef.sys.SDatetime".equals(className)) {
					sbi.append(modify(template,
						"&{name}", name,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.util.Date"));
					sbi.append(modify(template,
						"&{name}", name,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.sql.Timestamp"));
					sbi.append(modify(template,
						"&{name}", name,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.util.Calendar"));
				}
			} else {
				final String template =
(_genJavadoc ? ("\t/** Set value of &{d} \"&{xmlName}\"."+LN+
"\t * @param x value to be set."+LN+
"\t */"+LN) : "")+
"\t"+publ+" void set&{name}(&{typ} x);"+LN;
				sbi.append(modify(template,
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", className));
				if ("org.xdef.sys.SDatetime".equals(className)) {
					sbi.append(modify(template,
						"&{name}", name,
						"&{d}" , d,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.util.Date"));
					sbi.append(modify(template,
						"&{name}", name,
						"&{d}" , d,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.sql.Timestamp"));
					sbi.append(modify(template,
						"&{name}", name,
						"&{d}" , d,
						"&{xmlName}", name.replace('$', ':'),
						"&{typ}", "java.util.Calendar"));
				}
			}
		}
		if (max > 1) {
			final String template =
(_genJavadoc ? ("\t/** Add value to list of \"&{xmlName}\"."+LN+
"\t * @param x value to be added."+LN+
"\t */"+LN) : "")+
"\t"+publ+" void add&{name}(&{typ} x) {&{x}}"+LN;
			if (!clearChoice.isEmpty()) {
				x = LN + clearChoice + x;
			}
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", className));
			if ("org.xdef.sys.SDatetime".equals(className)) {
				String typeName1 = "java.util.Date";
				sb.append(modify(template,
					"&{x}", modify(x, "_&{name}.add(x)", "_&{name}.add(new org.xdef.sys.SDatetime(x))"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.sql.Timestamp";
				sb.append(modify(template,
					"&{x}", modify(x,"_&{name}.add(x)", "_&{name}.add(new org.xdef.sys.SDatetime(x))"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.util.Calendar";
				sb.append(modify(template,
					"&{x}", modify(x, "_&{name}.add(x)", "_&{name}.add(new SDatetime(x))"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
			}
		} else {
			final String template =
(_genJavadoc ? ("\t/** Set value of &{d} \"&{xmlName}\"."+LN+
"\t * @param x value to be set."+LN+
"\t */"+LN) : "")+
"\t"+publ+" void set&{name}(&{typ} x) {&{x}}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", className));
			if ("org.xdef.sys.SDatetime".equals(className)) {
				String typeName1 = "java.util.Date";
				sb.append(modify(template,
					"&{x}", modify(x, "_&{name}=x;", "_&{name}=org.xdef.sys.SDatetime.createFrom(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.sql.Timestamp";
				sb.append(modify(template,
					"&{x}", modify(x, "_&{name}=x;", "_&{name}=org.xdef.sys.SDatetime.createFrom(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.util.Calendar";
				sb.append(modify(template,
					"&{x}", modify(x, "_&{name}=x;", "_&{name}=org.xdef.sys.SDatetime.createFrom(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
			}
		}
	}

	/** Generation Java code of attribute setting.
	 * @param xdata XMData object.
	 * @param name name of attribute.
	 * @param sb String builder where the code is generated.
	 */
	final static void genCreatorOfAttribute(final XMData xdata,
		final String name,
		final StringBuilder sb) {
		final String uri = xdata.getNSUri();
		final String fn = uri != null ? "AttributeNS(\"" + uri + "\", " : "Attribute(";
		String x;
		if ((checkEnumType(xdata)) != null) {
			x = "get&{name}().name())";
		} else {
			XDParser xp  = (XDParser) xdata.getParseMethod();
			String parseName = xp.parserName();
			switch ("union".equals(parseName) ? xp.getAlltemsType() : xdata.getParserType()) {
				case XD_CHAR:
					x = "org.xdef.xon.XonTools.genXMLValue(get&{name}()))";
					break;
				case XD_DATETIME: {
					String s = xdata.getDateMask();
					x = s == null ? "org.xdef.component.XComponentUtil.dateToJstring(get&{name}()))"
						: "get&{name}().formatDate("+s+"))";
					break;
				}
				case XD_BYTES:
					x = ("base64Binary".equals(xdata.getParserName())
						? "encodeBase64" : "encodeHex") + "(get&{name}()))";
					break;
				case XD_IPADDR:
					x = "get&{name}().toString().substring(1))";
					break;
				case XD_NULL: //jnull
					x = "\"null\")";
					break;
				case XD_STRING:
					x = "get&{name}())";
					break;
				case XD_CONTAINER: {
					x = "org.xdef.component.XComponentUtil.listToString(get&{name}(), "
						+ (parseName.equals("jlist") ? "true" : "false") + "))";
					break;
				}

				default: x = "get&{name}().toString())";
			}
		}
		sb.append(modify(
"\t\tif (get&{name}() != null)"+LN+
"\t\t\tel.set&{fn}&{qName}, &{x};"+LN,
			"&{x}", x,
			"&{fn}", fn,
			"&{qName}", "XD_Name_" + name,
			"&{name}", name));
	}

	/** Generation Java code for creating child element.
	 * @param name name of variable.
	 * @param sb String builder where code is generated.
	 * @param isList if true then object is list.
	 */
	final void genChildElementCreator(final String name,
		final StringBuilder sb,
		final boolean isList) {
		if (sb.length() == 0) {
			sb.append("\t\t").append("java.util.List<org.xdef.component.XComponent> a=")
				.append("new java.util.ArrayList<>();").append(LN);
		}
		sb.append("\t\torg.xdef.component.XComponentUtil.addXC(a, ").append(isList ? "listOf" : "get")
			.append(name).append("());").append(LN);
	}

	/** Generation Java code for creating child element with text node.
	 * @param xdata XMData object.
	 * @param name of variable (and text node).
	 * @param max of occurrences (in sequences).
	 * @param sb String builder where code is generated.
	 */
	final void genTextNodeCreator(final XMData xdata,
		final String name,
		final int max,
		final StringBuilder sb) {
		if (sb.length() == 0) {
			sb.append("java.util.List<org.xdef.component.XComponent> a=")
				.append("new java.util.ArrayList<>();").append(LN);
		}
		String x;
		final String y = max > 1? ".get(i)" : "";
		final String z = (max > 1 ? "listOf" : "get") + "&{name}()";
		XDParser xp  = (XDParser) xdata.getParseMethod();
		String parseName = xp.parserName();
		switch ("union".equals(parseName) ? xp.getAlltemsType() : xdata.getParserType()) {
			case XD_BOOLEAN:
			case XD_BYTE:
			case XD_SHORT:
			case XD_INT:
			case XD_LONG:
			case XD_FLOAT:
			case XD_DOUBLE:
			case XD_DECIMAL:
			case XD_NUMBER:
			case XD_DURATION:
				x = z+y+".toString()";
				break;
			case XD_CHAR:
				x = "org.xdef.xon.XonTools.genXMLValue(" + z + y +")";
				break;
			case XD_IPADDR:
				x = "get&{name}().toString().substring(1)";
				break;
			case XD_DATETIME: {
				String s = xdata.getDateMask();
				x = s == null ? "org.xdef.component.XComponentUtil.dateToJstring(" + z+y + ")"
					: z+y+"." +	"formatDate(" + s + ")";
				break;
			}
			case XD_BYTES:
				x = ("base64Binary".equals(xdata.getParserName())
					? "encodeBase64(" : "encodeHex(") + z + y + ")";
				break;
			case XD_NULL: //jnull
				x = "\"null\"";
				break;
			case XD_ANY: //jvalue
				if ("jvalue".equals(xdata.getParserName())) {
					x = z +y + (max <= 1 ? ".toString()" : "");
					break;
				}
			case XD_CONTAINER: {
				x = "org.xdef.component.XComponentUtil.listToString(" + z + ","
					+ (parseName.equals("jlist") ? "true" : "false") + ")";
				break;
			}
			default:
				x = z+y;
				if (checkEnumType(xdata) != null) {
					x += ".name()";
				}
		}
		sb.append(modify(max == 1 ?
("\t\tif (get&{name}() != null)"+LN+
"\t\t\torg.xdef.component.XComponentUtil.addText(this,\"&{xpos}\", a,"+LN+
"\t\t\t\t &{x}, _$&{name});"+LN)
: ("\t\tfor (int i=0; i<listOf$value().size(); i++) {"+LN+
"\t\t\tif (listOf&{name}().get(i) != null)"+LN+
"\t\t\t\torg.xdef.component.XComponentUtil.addText(this,\"&{xpos}\",a,"+LN+
"\t\t\t&{x},_$&{name}.charAt(i));"+LN+
"\t\t}"+LN),
			"&{x}", x,
			"&{xpos}", xdata.getXDPosition(),
			"&{name}", name));
	}

	/** Generation of Java code comment separator.
	 * @param text text of comment.
	 * @param doit if true then comment separator is generated, otherwise
	 * it is returned just en empty string.
	 * @return string with the separator or an empty string.
	 */
	final static String genSeparator(final String text, final boolean doit) {
		return doit ? "// " + text+LN : "";
	}

	final static void addNSUri(final Properties nsmap, final XNode xnode) {
		final String uri = xnode.getNSUri();
		if (uri == null) return;
		final String name = xnode.getName();
		final int ndx = name.indexOf(':');
		String nsName = ndx > 0 ? "xmlns$"+name.substring(0, ndx) : "xmlns";
		nsmap.put(nsName, uri);
	}

	/** Get XDPosition of the node or of its reference.
	 * @param xe Model
	 * @return name of class if reference exists, otherwise return null.
	 */
	final String getXDPosition(final XElement xe,
		final boolean genInterface) {
		if (genInterface) {
			return ((xe.isReference())
				? _components.get(xe.getReferencePos()) : _components.get(xe.getXDPosition())).getName();
		} else {
			String s = xe.getXDPosition();
			if (s == null) {// model still may be a reference
				//if null model is a reference
				s = xe.isReference() ? xe.getReferencePos() : null;
				return (_components.get(s)).getName();
			} else {
				String t = xe.isReference() ? xe.getReferencePos() : null;
				if (t == null) { // if no reference exists
					QName u1 = xe.getQName();
					XNode n = (XNode) xe.getXDPool().findModel(s);
					if (n != null && n.getKind() == XMELEMENT) {
						XComponentInfo x = _components.get(s);
						QName u2 = x == null
							? ((XElement) n).getQName() : new QName(x.getNS(),((XElement)n).getLocalName());
						if (u1 == null ? u2 == null : u1.equals(u2)) {
							t = x != null ? x.getName() : null;
							return t;
						} else {
							return null;
						}
					} else { // we return model class
						return _components.get(s).getName();
					}
				}
				// we have both, reference and model
				XComponentInfo u, v;
				if ((u = _components.get(s)) == null) {
					// if the class for model is not declared
					XComponentInfo x = _components.get(t);
					if (x == null) {
						return null;
					}
					return x.getName(); // so return reference
				}
				if ((v = _components.get(t)) == null) {
					// if the reference class is not declarted
					return u.getName(); // we return class of model
				}
				// Now we know there are declarations both of model and of ref. Check if the model inplements
				// a class and if the reference implements an interface. If the model implements the same
				// interface as the refernced model then return the model declaration otherwise return
				// the reference declaration
				final int ndxu, ndxv;
				String uu = u.getName();
				String vv = v.getName();
				return (ndxu = uu.indexOf(" implements ")) > 0 && (ndxv = vv.indexOf(" interface ")) > 0
					&& uu.substring(ndxu+12).equals(vv.substring(ndxv+11)) ? vv : uu;
			}
		}
	}

	/** Get bind name.
	 * @param xe parent XElement.
	 * @param xn checked node.
	 * @return 1. the bind name if node is bound or forced to bind,<br/>
	 * 2. null if no bind,<br/>
	 * 3. the empty string if node is recursive reference.
	 */
	final String checkBind(final XElement xe, final XNode xn) {
		final String xdPos = xn.getXDPosition();
		String s = _binds.get(xdPos);
		int ndx = xdPos.lastIndexOf('/');
		if (ndx < 0) {
			return null;
		}
		if (s == null && (s = xe.isReference() ? xe.getReferencePos() : null) != null) {
			s = _binds.get(xe.getXDPosition() + xdPos.substring(ndx));
		}
		return s;
	}

	/** Create new name if the name from argument already exists in the set.
	 * @param name the name to be checked.
	 * @param set set with names.
	 * @return new name if the the name from argument exists in the set or return the original name;
	 */
	final static String getUniqueName(final String name,
		final Set<String> set) {
		String s = name;
		for (int i = 1; set.contains(s); i++) {
			s = name + "_" + i;
		}
		return s;
	}

	/** Return correct name of inner class in the X-component.
	 * @param className the name to be corrected.
	 * @param classNameBase base to chich tne new inner class is added.
	 * @param classNames set with class names.
	 * @return correct class name.
	 */
	final static String correctClassName(final String className,
		final String classNameBase,
		final Set<String> classNames) {
		int ndx = classNameBase.lastIndexOf('.');
		String s = ndx >= 0 ? classNameBase.substring(ndx + 1) : classNameBase;
		ndx=0;
		String[] ss = s.split("#");
		String t = className;
		for (int j = 0; j < ss.length; j++) {
			if (t.equals(ss[j])) {
				t = className + "_" + (++ndx);
				j = 0;
			}
		}
		String s1 = s + '#' + t;
		for (; classNames.contains(s1);) {
			s1 = s + '#' + className + "_" + (++ndx);
		}
		classNames.add(s1);
		ndx = s1.lastIndexOf('#');
		return s1.substring(ndx+1);
	}

	/** Add name of variable name to the set of variable names.
	 * @param varNames set of variable names.
	 * @param name name to add.
	 * @param xdPosition XDPosition of actual model.
	 * @param ext it it is external name.
	 * @return the unique name.
	 */
	final String addVarName(final Set<String> varNames,
		final String name,
		final String xdPosition,
		final boolean ext) {
		String iname = getUniqueName(name, varNames); //get unique variable name
		varNames.add(iname);
		if (iname.equals(name)) {
			return name;
		}
		if (ext) {
			//Getter/setter name &{0} in &{1} can't be used. Please change name by command %bind
			_reporter.error(XDEF.XDEF371, name, xdPosition);
			return name;
		} else {
			//Getter/setter name &{0} in &{1} was changed to &{2}. You can define other name by command %bind
			_reporter.warning(XDEF.XDEF360, name, xdPosition,iname);
		}
		return iname;
	}

	/** Check if the node is unique in the child list.
	 * @param nodes list of child nodes.
	 * @param index index of tested node.
	 * @return true if the node is unique in the child list of element model.
	 */
	final static boolean checkUnique(final XNode[] nodes, final int index) {
		final int len = nodes.length;
		if (len <= 1) {
			return true;
		}
		final XNode xe = nodes[index];
		String name = xe.getLocalName();
		String ns = xe.getNSUri();
		short kind = xe.getKind();
		for ( int i = 0; i < nodes.length; i++) {
			if (i == index) {
				continue; //skip the checked node itself;
			}
			final XNode node = nodes[i];
			if (node.getKind() != kind) {
				continue;
			}
			String name1 = node.getLocalName();
			if (!name1.equals(name)) {
				continue; // not equal names
			}
			if (ns != null) {
				if (!ns.equals(node.getNSUri())) {
					continue; // not equal namespace URI
				}
			} else if (xe.getNSUri() != null) {
				continue; // not equal namespace URI (both null)
			}
			return false;
		}
		return true;
	}
}