package org.xdef.component;

import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.XDFactory;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.LinkedHashMap;
import java.util.HashSet;
import org.xdef.XDConstants;
import org.xdef.XDValueID;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XPool;
import org.xdef.impl.code.CodeTable;
import org.xdef.json.JsonUtil;

/** Generation of Java source code of XDComponents.
 * @author Vaclav Trojan
 */
public final class GenXComponent {
	/** Platform-dependent newline. */
	private static final String LN = XDConstants.LINE_SEPARATOR;
	/** Names that can't be used in generated code.*/
	private static final Set<String> RESERVED_NAMES = new HashSet<String>();
	/** Switch if byte array is encoded as base64 (1) or hexadecimal (2).*/
	private byte byteArrayEncoding;
	/** Switch to generate JavaDoc. */
	private boolean _genJavadoc;
	/** Builder to generate interface or null. */
	private StringBuilder _interface; // where to create interface.
	/** Map with components information. */
	private Map<String, String> _components;
	/** Map with binds information. */
	final Map<String, String> _binds;
	/** Internal reporter where to write messages. */
	final ArrayReporter _reporter;
	/** XDPool from which components are generated. */
	final XDPool _xp;

	static {
////////////////////////////////////////////////////////////////////////////////
// Names that can't be used in generated code
////////////////////////////////////////////////////////////////////////////////
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
		// Qualified names used in code generation
		RESERVED_NAMES.add("org.xdef.msg.XDEF");
		RESERVED_NAMES.add("org.xdef.sys.SDatetime");
		RESERVED_NAMES.add("org.xdef.sys.SDuration");
		RESERVED_NAMES.add("org.xdef.xml.KXmlUtils");
		RESERVED_NAMES.add("org.xdef.component.XComponent");
		RESERVED_NAMES.add("org.xdef.component.XComponentUtil");
		RESERVED_NAMES.add("org.xdef.XDParseResult");
		RESERVED_NAMES.add("org.xdef.proc.XXNode");
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
		RESERVED_NAMES.add("org.xdef.sys.SUtils");
		RESERVED_NAMES.add("org.xdef.sys.SException");
		RESERVED_NAMES.add("org.xdef.sys.SRuntimeException");
		RESERVED_NAMES.add("org.w3c.dom.Attr");
		RESERVED_NAMES.add("org.w3c.dom.Document");
		RESERVED_NAMES.add("org.w3c.dom.Element");
		RESERVED_NAMES.add("org.w3c.dom.Node");
	}

	/** New instance of this class.*/
	private GenXComponent(XDPool xp, ArrayReporter reporter) {
		_xp = xp;
		_binds = xp.getXComponentBinds();
		_reporter = reporter;
	}

	/** Replace all occurrences of the key in the source by the value.
	 * @param source string source.
	 * @param replacements array of tuples of key an replacement.
	 * in the source.
	 * @return modified string.
	 */
	private static String modify(final String origin, final String... replace) {
		if (replace.length % 2 != 0) {
			//Internal error&{0}{: }
			throw new SIllegalArgumentException(SYS.SYS066,
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
			Map<String, String> enums =
				xdata.getXDPool().getXComponentEnums();
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
			return enumName.indexOf('%') == 0
				? /* is reference */ enumName.substring(1) : enumName;
		}
		return null;
	}

	/** Get Java Object name corresponding to XD type.
	 * @param xdata XMData object.
	 * @return Java Object name corresponding to XD type
	 */
	private String getJavaObjectTypeName(final XMData xdata) {
		String parserName = xdata.getParserName();
		if ("byte".equals(parserName)) {
			return "Byte";
		} else if ("short".equals(parserName)) {
			return "Short";
		} else if ("int".equals(parserName)
			|| "unsignedByte".equals(parserName)
			|| "unsignedShort".equals(parserName)) {
			return "Integer";
		} else if ("long".equals(parserName)||"unsignedInt".equals(parserName)){
			return "Long";
		} else if ("integer".equals(parserName)
			|| "negativeInteger".equals(parserName)
			|| "nonNegativeInteger".equals(parserName)
			|| "PositiveInteger".equals(parserName)
			|| "nonPositiveiveInteger".equals(parserName)) {
			return "java.math.BigInteger";
		} else if ("decimal".equals(parserName)) {
			return "java.math.BigDecimal";
		}
		switch (xdata.getParserType()) {
			case XDValueID.XD_BOOLEAN:
				return "Boolean";
			case XDValueID.XD_INT:
				return "Long";
			case XDValueID.XD_FLOAT:
				return "Double";
			case XDValueID.XD_DECIMAL:
				return "java.math.BigDecimal";
			case XDValueID.XD_DURATION:
				return "org.xdef.sys.SDuration";
			case XDValueID.XD_DATETIME:
				return "org.xdef.sys.SDatetime";
			case XDValueID.XD_BYTES:
				byteArrayEncoding |= getBytesType(xdata);
				return "byte[]";
			case XDValueID.XD_NULL: //jnull
				return "Object";
		}
		String result = checkEnumType(xdata);
		return (result != null) ? result : "String";  // default
	}

	/** Create ParsedResultGetter.
	 * @param xdata XData model.
	 * @return getter of ParsedResult.
	 */
	private static String getParsedResultGetter(final XMData xdata) {
		String result = "parseResult.";
		String parserName = xdata.getParserName();
		if ("byte".equals(parserName)) {
			return result + "getParsedValue().byteValue()";
		} else if ("short".equals(parserName)) {
			return result + "getParsedValue().shortValue()";
		} else if ("int".equals(parserName)
			|| "unsignedByte".equals(parserName)
			|| "unsignedShort".equals(parserName)) {
			return result + "getParsedValue().intValue()";
		} else if ("long".equals(parserName)||"unsignedInt".equals(parserName)){
			return result + "getParsedValue().longValue()";
		} else if ("integer".equals(parserName)
			|| "negativeInteger".equals(parserName)
			|| "nonNegativeInteger".equals(parserName)
			|| "PositiveInteger".equals(parserName)
			|| "nonPositiveiveInteger".equals(parserName)) {
			return result + "getParsedValue().integerValue()";
		} else if ("decimal".equals(parserName)) {
			return result + "getParsedValue().decimalValue()";
		}
		switch (xdata.getParserType()) {
			case XDValueID.XD_BOOLEAN:
				return result + "getParsedValue().booleanValue()";
			case XDValueID.XD_INT:
				return result + "getParsedValue().longValue()";
			case XDValueID.XD_FLOAT:
				return result + "getParsedValue().doubleValue()";
			case XDValueID.XD_DECIMAL:
				return result + "getParsedValue().decimalValue()";
			case XDValueID.XD_DURATION:
				return result + "getParsedValue().durationValue()";
			case XDValueID.XD_DATETIME:
				return result + "getParsedValue().datetimeValue()";
			case XDValueID.XD_BYTES:
				return result + "getParsedValue().getBytes()";
			case XDValueID.XD_PARSER:
				return result + "getParsedString()";
		}
		result += "getParsedValue().toString()";
		String enumType = checkEnumType(xdata);
		return enumType != null ? enumType+".toEnum("+ result+")" : result;
	}

	/** Get encoding parser (i.e. hex or base64) of type bytes. */
	private static byte getBytesType(final XMData xdata) {
		final String s = xdata.getParserName();
		return "base64Binary".equals(s) ?
			(byte) 1 : "hexBinary".equals(s) ? (byte) 2 : (byte) 0;
	}

	/** Generate declaration of variable as Java object from an attribute or
	 * text node(s).
	 * @param xdata XMData object.
	 * @param name name of variable.
	 * @param max maximal number of items .
	 * @param descr JavaDoc description.
	 * @param sb String builder where the code is generated.
	 */
	private void genBaseVariable(final XMData xdata,
		final String name,
		final int max,
		final String descr,
		final StringBuilder sb) {
		genVariableFromModel(getJavaObjectTypeName(xdata),name,max,descr,sb);
	}

	/** Generate declaration of variable of attribute name.
	 * @param name name of variable.
	 * @param sb String builder where the code is generated.
	 */
	private void genAttrNameVariable(final String name,
		final StringBuilder sb) {
		sb.append(modify(
(_genJavadoc ? "\t/** Name of attribute &{name} in data\".*/"+LN : "") +
"\tprivate String XD_Name_&{name}=\"&{name}\";"+LN,
			"&{name}", name));
	}

	/** Generate declaration of variable as Java object from child element.
	 * @param typeName name of child element Java class.
	 * @param name name of variable.
	 * @param descr JavaDoc description.
	 * @param sb String builder where the code is generated.
	 */
	private void genVariableFromModel(final String typeName,
		final String name,
		int max,
		final String descr,
		final StringBuilder sb) {
		String d = descr;
		String x;
		String typ = typeName;
		if (max > 1) {
			d += 's';
			String s = "new java.util.ArrayList<" + typ + ">()";
			x = " =" + (s.length() > 40 ? LN + "\t\t" : " ") + s;
			typ = "java.util.List<"+typ+">";
		} else {
			x = "";
		}
		sb.append(modify(
(_genJavadoc ? "\t/** Value of &{d} \"&{xmlName}\".*/"+LN : "")+
"\tprivate"+(max > 1?" final":"") +" &{typ} _&{name}&{x};"+LN,
			"&{d}", d,
			"&{xmlName}", name.replace('$', ':'),
			"&{typ}", typ,
			"&{x}", x,
			"&{name}", name));
	}

	/** Generate Java code of getter method for base types.
	 * @param xdata XMData object.
	 * @param name name of variable.
	 * @param max maximal number of items .
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 */
	private void genBaseGetterMethod(final XData xdata,
		final String name,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		final String typ = getJavaObjectTypeName(xdata);
		genGetterMethodFromChildElement(xdata, typ, name, max, descr, sb, sbi);
	}

	/** Generate java code of getter method for child element classes.
	 * @param typeName name of class representing the child element.
	 * @param name name of variable.
	 * @param max maximal number of items .
	 * set name of this model, otherwise this argument is null.
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 * @return generated code.
	 */
	private void genGetterMethodFromChildElement(XNode xn,
		final String typeName,
		final String name,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
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
"\tpublic &{typ} listOf&{name}();"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
			} else {
				sbi.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get&{name}();"+LN,
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
"\tpublic java.util.Date dateOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf&{name}();"+LN,
						"&{xmlName}", xmlName,
						"&{d}" , d,
						"&{name}", name));
				}
			}
		}
		if (ndx > 0) {
			typ = (max > 1) ? "java.util.List<" + typeName + ">" : typeName;
		}
		if (typ.startsWith("java.util.List<")) {
			sb.append(modify(
(_genJavadoc ? "\t/** Get list of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} listOf&{name}() {return _&{name};}"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
		} else {
			sb.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get&{name}() {return _&{name};}"+LN,
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
"\tpublic java.util.Date dateOf&{name}(){"+
"return org.xdef.sys.SDatetime.getDate(_&{name});}"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf&{name}(){"+
"return org.xdef.sys.SDatetime.getTimestamp(_&{name});}"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf&{name}(){"+
"return org.xdef.sys.SDatetime.getCalendar(_&{name});}"+LN,
					"&{xmlName}", xmlName,
					"&{d}" , d,
					"&{name}", name));
			}
		}
	}

	/** Generate Java code of setter method for base types.
	 * @param xdata XMData object.
	 * @param name name of variable.
	 * @param descr Description text.
	 * @param max maximal number of items .
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 */
	private void genBaseSetterMethod(final XMData xdata,
		final String name,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		genSetterMethodOfChildElement(getJavaObjectTypeName(xdata),
			name, max, null, null, null, descr, sb, sbi);
	}

	/** Generate java code of setter method for child element classes.
	 * @param typeName name typ (class etc).
	 * @param name name of variable.
	 * @param modelName if the node references other model of node
	 * @param modelURI if the node references other model of node
	 * @param modelXDPos if the node references other model of node
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 */
	private void genSetterMethodOfChildElement(final String typeName,
		final String name,
		final int max,
		final String modelName,
		final String modelURI,
		final String modelXDPos,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		String x;
		String d = descr;
		if (max > 1) {
			d += 's';
			if (modelName != null) {
				x = LN +
"\t\tif (x!=null) {"+LN +
"\t\t\t\tif (x.xGetXPos()==null)"+LN +
"\t\t\t\t\tx.xInit(this, \""+modelName+"\", "
				+ (modelURI != null ? "\"" + modelURI + "\"" : "null")
				+ ", \"" + modelXDPos + "\");"+LN
				+ "\t\t\t_&{name}.add(x);"+LN+"\t\t}"+LN+LN+'\t';
			} else {
				x = "if (x!=null) _&{name}.add(x);";
			}
		} else {
			if (modelName != null) {
				x = LN +
"\t\tif (x!=null && x.xGetXPos() == null)"+LN +
"\t\t\tx.xInit(this, \""+modelName+"\", "
				+ (modelURI != null ? "\"" + modelURI + "\"" : "null")
				+ ", \"" + modelXDPos + "\");"+LN
				+ "\t\t_&{name}=x;"+LN+"\t";
			} else {
				x = "_&{name}=x;";
			}
		}
		if (sbi != null) {
			sb.append("\t@Override").append(LN);
			if (max > 1) {
				String template =
(_genJavadoc ? ("\t/** Add value to list of \"&{xmlName}\"."+LN+
"\t * @param x value to added."+LN+
"\t */"+LN) : "")+
"\tpublic void add&{name}(&{typ} x);"+LN;
				sbi.append(modify(template,
					"&{name}", name,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName));
				if ("org.xdef.sys.SDatetime".equals(typeName)) {
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
"\tpublic void set&{name}(&{typ} x);"+LN;
				sbi.append(modify(template,
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName));
				if ("org.xdef.sys.SDatetime".equals(typeName)) {
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
"\tpublic void add&{name}(&{typ} x) {&{x}}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", typeName));
			if ("org.xdef.sys.SDatetime".equals(typeName)) {
				String typeName1 = "java.util.Date";
				sb.append(modify(template,
					"&{x}", modify(x,
						typeName, typeName1,
						"_&{name}.add(x)",
						"_&{name}.add(new org.xdef.sys.SDatetime(x))"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.sql.Timestamp";
				sb.append(modify(template,
					"&{x}", modify(x,
						typeName, typeName1,
						"_&{name}.add(x)",
						"_&{name}.add(new org.xdef.sys.SDatetime(x))"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.util.Calendar";
				sb.append(modify(template,
					"&{x}", modify(x,
						typeName, typeName1,
						"_&{name}.add(x)", "_&{name}.add(new SDatetime(x))"),
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
"\tpublic void set&{name}(&{typ} x){&{x}}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", typeName));
			if ("org.xdef.sys.SDatetime".equals(typeName)) {
				String typeName1 = "java.util.Date";
				sb.append(modify(template,
					"&{x}", modify(x,
						typeName, typeName1,
						"_&{name}=x;",
"_&{name}=x==null?null:new org.xdef.sys.SDatetime(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.sql.Timestamp";
				sb.append(modify(template,
					"&{x}", modify(x,
						typeName, typeName1,
						"_&{name}=x;",
"_&{name}=x==null?null:new org.xdef.sys.SDatetime(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.util.Calendar";
				sb.append(modify(template,
						"&{x}", modify(x,
							typeName, typeName1,
							"_&{name}=x;",
"_&{name}=x==null?null:new org.xdef.sys.SDatetime(x);"),
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
			}
		}
	}

	/** Generate Java code of getter of xpath for attributes and text nodes.
	 * @param name name of variable.
	 * @param descr Description text.
	 * @param max maximal number of items .
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated.
	 */
	private void genBaseXPosMethod(
		final String name,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		if (sbi != null) {
			sb.append("\t@Override").append(LN);
			sbi.append(modify(
(_genJavadoc ? ("\t/** Get XPath position of \"&{descr}\".*/"+LN) : "")+
"\tpublic String xposOf&{name}();"+LN,
				"&{name}", name,
				"&{descr}", descr));
		}
		final String x = "attribute".equals(descr) ? "@" + name : "$text";
		sb.append(modify(
(_genJavadoc ? ("\t/** Get XPath position of \"&{descr}\".*/"+LN) : "")+
"\tpublic String xposOf&{name}(){return XD_XPos+\"/&{x}\";}"+LN,
			"&{name}", name,
			"&{x}", x,
			"&{descr}", descr));
	}

	/** Generation Java code of attribute setting.
	 * @param xdata XMData object.
	 * @param name name of attribute.
	 * @param sb String builder where the code is generated.
	 */
	private static void genCreatorOfAttribute(final XMData xdata,
		final String name,
		final StringBuilder sb) {
		final String uri = xdata.getNSUri();
		final String fn =
			uri != null ? "AttributeNS(\"" + uri + "\", " : "Attribute(";
//		final String qName = xdata.getName();
		String x;
		switch (xdata.getParserType()) {
			case XDValueID.XD_BOOLEAN:
			case XDValueID.XD_INT:
			case XDValueID.XD_FLOAT:
				x = "String.valueOf(get&{name}()))";
				break;
			case XDValueID.XD_DECIMAL:
				x = "get&{name}().toString())";
				break;
			case XDValueID.XD_DURATION:
				x = "get&{name}().toString())";
				break;
			case XDValueID.XD_DATETIME: {
				String s = xdata.getDateMask();
				x = "get&{name}()." +
					(s == null ? "toISO8601())" : "formatDate("+s+"))");
				break;
			}
			case XDValueID.XD_BYTES:
				x = (getBytesType(xdata) == 2 ? "encodeHex"
					:"encodeBase64") + "(get&{name}()))";
				break;
			case XDValueID.XD_NULL: //jnull
				x = "\"null\")";
				break;
			default:
				x = checkEnumType(xdata) == null ?
					"get&{name}())" : "get&{name}().name())";
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
	private void genChildElementCreator(final String name,
		final StringBuilder sb,
		final boolean isList) {
		if (sb.length() == 0) {
			sb.append(
"\t\tjava.util.List<org.xdef.component.XComponent> a=")
			.append(LN).append(
"\t\t\tnew java.util.ArrayList<org.xdef.component.XComponent>();")
			.append(LN);
		}
		sb.append("\t\torg.xdef.component.XComponentUtil.addXC(a, ")
			.append(isList ? "listOf" : "get")
			.append(name).append("());").append(LN);
	}

	/** Generation Java code for creating child element with text node.
	 * @param xdata XMData object.
	 * @param name of variable (and text node).
	 * @param max of occurrences (in sequences).
	 * @param sb String builder where code is generated.
	 */
	private void genTextNodeCreator(final XMData xdata,
		final String name,
		final int max,
		final StringBuilder sb) {
		if (sb.length() == 0) {
			sb.append(
"\t\tjava.util.ArrayList<org.xdef.component.XComponent> a=").append(LN)
				.append(
"\t\t\tnew java.util.ArrayList<org.xdef.component.XComponent>();")
				.append(LN);
		}
		String x;
		final String y = max > 1? ".get(i)" : "";
		switch (xdata.getParserType()) {
			case XDValueID.XD_BOOLEAN:
			case XDValueID.XD_INT:
			case XDValueID.XD_FLOAT:
			case XDValueID.XD_DECIMAL:
			case XDValueID.XD_DURATION:
				x = (max > 1 ? "listOf" : "get") + "&{name}()"+y+".toString()";
				break;
			case XDValueID.XD_DATETIME: {
				String s = xdata.getDateMask();
				x = (max > 1 ? "listOf" : "get") + "&{name}()"+y+"." +
					(s!=null ? "formatDate(" + s : "toISO8601(") + ")";
				break;
			}
			case XDValueID.XD_BYTES:
				x = (getBytesType(xdata) == 2 ? "encodeHex(" : "encodeBase64(")
					+ (max > 1 ? "listOf" : "get") + "&{name}()"+y+")";
				break;
			case XDValueID.XD_NULL: //jnull
				x = "\"null\"";
				break;
			default:
				x = (max > 1 ? "listOf" : "get") + "&{name}()"+y;
				if (checkEnumType(xdata) != null) {
					x += ".name()";
				}
		}
		sb.append(modify(max == 1 ?
("\t\tif (get&{name}() != null)"+LN+
"\t\t\torg.xdef.component.XComponentUtil.addText(this,"+LN+
"\t\t\t\t\"&{xpos}\", a, &{x}, _$&{name});"+LN)
: ("\t\tfor (int i=0; i<listOf$value().size(); i++) {"+LN+
"\t\t\tif (listOf&{name}().get(i) != null)"+LN+
"\t\t\t\torg.xdef.component.XComponentUtil.addText(this,\"&{xpos}\",a,&{x}"+
	",_$&{name}.charAt(i));"+LN+
"\t\t}"+LN),
			"&{x}", x,
			"&{xpos}", xdata.getXDPosition(),
			"&{name}", name));
	}

	/** Check if the node is unique in the child list.
	 * @param nodes list of child nodes.
	 * @param index index of tested node.
	 * @return true if the node is unique in the child list of element model.
	 */
	private boolean checkUnique(final XNode[] nodes, final int index) {
		final int len = nodes.length;
		if (len <= 1) {
			return true;
		}
		final XNode xe = nodes[index];
		String name = xe.getName();
		int ndx = name.indexOf(':');
		if (ndx > 0) {
			name = name.substring(ndx + 1); //remove prefix
		}
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
			String name1 = node.getName();
			ndx = name1.indexOf(':');
			if (ndx > 0) {
				name1 = name1.substring(ndx + 1); //remove prefix
			}
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

	/** Generation of Java code comment separator.
	 * @param text text of comment.
	 * @param doit if true then comment separator is generated, otherwise
	 * it is returned just en empty string.
	 * @return string with the separator or an empty string.
	 */
	private static String genSeparator(final String text, final boolean doit) {
		return doit ? "// " + text+LN : "";
	}

	private static void addNSUri(final Properties nsmap, final XNode xnode) {
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
	private String getXDPosition(final XElement xe,
		final boolean genInterface) {
		if (genInterface) {
			return (xe.isReference()) ?	_components.get(xe.getReferencePos())
				: _components.get(xe.getXDPosition());
		} else {
			final String s = xe.getXDPosition();
			if (s == null) {// model still may be reference
				//if null model is a reference
				return _components.get(
					xe.isReference() ? xe.getReferencePos() : null);
			} else {
				final String t = xe.isReference() ? xe.getReferencePos() : null;
				if (t == null) { // if no reference exists
					return _components.get(s); // we return model class
				}
				// we have both, reference and model
				String u, v;
				if ((u = _components.get(s)) == null) {
					// if the class for model is not declared
					return _components.get(t); // so return reference
				}
				if ((v = _components.get(t)) == null) {
					// if the reference class is not declarted
					return u; // we return class of model
				}
				// Now we know there are declarations both of model and of ref.
				// We check if the model inplements a class and if the reference
				// implements an interface.
				// If the model implements the same interface as
				// the refernced model then we return the model declaration
				// otherwise we return the reference declaration
				final int ndxu, ndxv;
				return ((ndxu = u.indexOf(" implements ")) > 0
					&& (ndxv = v.indexOf(" interface ")) > 0 &&
					u.substring(ndxu+12).equals(v.substring(ndxv+11))) ? v : u;
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
	private String checkBind(final XElement xe, final XNode xn) {
		final String xdPos = xn.getXDPosition();
		String s = _binds.get(xdPos);
		int ndx = xdPos.lastIndexOf('/');
		if (ndx < 0) {
			return null;
		}
		if (s == null 
			&& (s = xe.isReference() ? xe.getReferencePos() : null) != null) {
			s = _binds.get(xe.getXDPosition() + xdPos.substring(ndx));
		}
		return s;
	}

	/** Add name of variable name to the set.
	 * @param set the set where to add.
	 * @param name name to add.
	 * @param xdPosition XDPosition of actual model.
	 * @param ext it it is external name.
	 * @return the unique name.
	 */
	private String addVarName(final Set<String> set,
		final String name,
		final String xdPosition,
		final boolean ext) {
		String iname = getUniqueName(name, set);
		set.add(iname);
		if (iname.equals(name)) {
			return name;
		}
		if (ext) {
			//Getter/setter name &{0} in &{1} can't be used.
			//Please change name by command %bind
			_reporter.error(XDEF.XDEF371, name, xdPosition);
			return name;
		} else {
			//Getter/setter name &{0} in &{1} was changed to
			//&{2}. You can define other name by command %bind
			_reporter.warning(XDEF.XDEF360, name, xdPosition,iname);
		}
		return iname;
	}

	/** Create new name if the name from argument already exists in the set.
	 * @param name the name to be checked.
	 * @param set set with names.
	 * @return new name if the the name from argument exists in the set or
	 * return the original name;
	 */
	private static String getUniqueName(final String name,
		final Set<String> set) {
		String s = name;
		for (int i = 1; set.contains(s); i++) {
			s = name + "_" + i;
		}
		return s;
	}

	/** Create getter and setter of the model which have only one child
	 * node which is a text node.
	 * @param xe Element model from which setter/getter is generated.
	 * @param xdata text model.
	 * @param iname name of getter/setter of this model.
	 * @param setters where to generate setter.
	 * @param getters where to generate getter.
	 * @param sbi where to generate interface.
	 */
	private void genJsonDirectGetterSetter(XElement xe,
		XData xdata,
		String iname,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi) {
		String name = javaName(xe.getName());
		String typ = getJavaObjectTypeName(xdata);
		String jGet = "String".equals(typ) ?
			"org.xdef.json.JsonUtil.jstringFromXML(get&{iname}())"
			: "get&{iname}()";
		// getter
		String template =
(_genJavadoc ? "\t/** Get JSON value of textnode of &{d}."+LN+
"\t * @return value of text of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} jget&{name}()";
		getters.append(modify(template
			+"{return _&{iname}==null?null:" + jGet + ";}"+LN,
			"&{name}", name,
			"&{iname}", iname,
			"&{d}", xe.getName(),
			"&{typ}", typ));
		if ("org.xdef.sys.SDatetime".equals(typ)) {
			getters.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Date jdateOf&{name}(){"+
"return org.xdef.sys.SDatetime.getDate(jget&{name}());}"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp jtimestampOf&{name}(){"+
"return org.xdef.sys.SDatetime.getTimestamp(jget&{name}());}"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar jcalendarOf&{name}(){"+
"return org.xdef.sys.SDatetime.getCalendar(jget&{name}());}"+LN,
				"&{d}" , xe.getName(),
				"&{name}", name));
		}
		if (sbi != null) { // generate interface
			sbi.append(modify(template + ";" + LN,
				"&{name}", name,
				"&{d}", xe.getName(),
				"&{typ}", typ));
			if ("org.xdef.sys.SDatetime".equals(typ)) {
				getters.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Date jdateOf&{name};"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp jtimestampOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar jcalendarOf&{name}();"+LN,
					"&{d}" , xe.getName(),
					"&{name}", name));
			}
		}
		String jSet = "String".equals(typ)
			? "org.xdef.json.JsonUtil.jstringToXML(x,false)":"x";
		// setter
		template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x)";
			setters.append(modify(template+"{"+LN+
"\t\tset&{iname}("+ jSet + ");"+LN+
"\t}"+LN,
				"&{name}", name,
				"&{iname}", iname,
				"&{d}", xe.getName(),
				"&{typ}", typ));
		if ("org.xdef.sys.SDatetime".equals(typ)) {
			template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x)"+
"{jset&{name}(x==null?null:new org.xdef.sys.SDatetime(x));}"+LN;
			setters.append(modify(template,
				"&{name}", name,
				"&{d}", xe.getName(),
				"&{typ}", "java.util.Date"));
			setters.append(modify(template,
				"&{name}", name,
				"&{d}", xe.getName(),
				"&{typ}", "java.sql.Timestamp"));
			setters.append(modify(template,
				"&{name}", name,
				"&{d}", xe.getName(),
				"&{typ}", "java.util.Calendar"));
		}
		if (sbi != null) { // generate interface
			sbi.append(modify(template +  ";" + LN,
				"&{name}", name,
				"&{d}", xe.getName(),
				"&{typ}", typ));
			if ("org.xdef.sys.SDatetime".equals(typ)) {
				template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x);"+LN;
				setters.append(modify(template,
					"&{name}", name,
					"&{d}", xe.getName(),
					"&{typ}", "java.util.Date"));
				setters.append(modify(template,
					"&{name}", name,
					"&{d}", xe.getName(),
					"&{typ}", "java.sql.Timestamp"));
				setters.append(modify(template,
					"&{name}", name,
					"&{d}", xe.getName(),
					"&{typ}", "java.util.Calendar"));
			}
		}
	}

	/** Check if the model is a JSON model and if it has only one child
	 * node which is a text node. Then JSON setter/getter can be generated.
	 * @param xe the Element model.
	 * @return true if and only if the model is a JSON model and it has only one
	 * child node which is a text node.
	 */
	private boolean isJsonItem(final XElement xe) {
		if (xe._json >= 1) {
			XNode[] nodes = (XNode[]) xe.getChildNodeModels();
			if (nodes.length == 1 && nodes[0].getKind() == XNode.XMTEXT) {
				return true;
			}
		}
		return false;
	}

	/** Create getter and setter of the model which have only one child
	 * node which is a text node.
	 * @param xe Element model from which setter/getter is generated.
	 * @param typeName the class name of this element X-component.
	 * @param iname name of getter/setter of this model.
	 * @param max maximal occurrence.
	 * @param setters where to generate setter.
	 * @param getters where to generate getter.
	 * @param sbi where to generate interface.
	 * @param classNames set with class names.
	 * @param varNames set with variable names.
	 */
	private void genJsonGetterAndSetters(final XElement xe,
		final String typeName,
		final String iname,
		final int max,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi,
		final Set<String> classNames,
		final Set<String> varNames) {
		int ndx = iname.indexOf('$');
		String name = iname.substring(ndx + 1);
			if (xe._json == 2) {
				name = javaName(xe.getName());
			} else if (xe._match >= 0) {
				XDValue[] code = ((XPool)xe.getXDPool()).getCode();
				for (int i = xe._match; i < code.length; i++) {
					XDValue item = code[i];
					if (item.getCode() == CodeTable.CMPEQ) {
						name = javaName(
							JsonUtil.toXmlName(code[i-1].stringValue()));
						break;
					}
				}
			}
			String typ =
				getJavaObjectTypeName((XData) xe.getChildNodeModels()[0]);
			name = javaName(name);
			name = getUniqueName(getUniqueName(getUniqueName(name,
				RESERVED_NAMES), classNames), varNames);
			String template;
			// has only a text child
			String jGet, jSet;
			if (max > 1) { // list of values
				String typ1 = "java.util.List<" + typ + ">";
				jGet = "String".equals(typ) ?
					"org.xdef.json.JsonUtil.jstringFromXML(y.get$value())"
					: "y.get$value()";
				// getter
				template =
(_genJavadoc ? "\t/** Get values of text nodes of &{d}."+LN+
"\t * @return value of text nodes of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ1} jlistOf&{name}()";
				getters.append(modify(template +
"{"+LN+
"\t\t&{typ1} x=new java.util.ArrayList<&{typ}>();"+LN+
"\t\tfor(&{typeName} y: _&{iname}) x.add(" + jGet + ");"+LN+
"\t\treturn x;"+LN+
"\t}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ,
					"&{typ1}", typ1,
					"&{typeName}", typeName));
				if (sbi != null) { // generate interface
					sbi.append(modify(template +";"+LN,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ1}", typ1));
				}
				// setter
				jSet = "String".equals(typ) ?
					"org.xdef.json.JsonUtil.jstringToXML(x,false)" : "x";
				template =
(_genJavadoc ? "\t/** Add values of textnodes of &{d}. */"+LN : "")+
"\tpublic void add&{name}(&{typ} x)";
				setters.append(modify(template +
"{"+LN+
"\t\tif (x!=null) {"+LN+
"\t\t\t&{typeName} y=new &{typeName}();"+LN+
"\t\t\ty.set$value(" + jSet + "); add&{iname}(y);"+LN+
"\t\t}"+LN+"\t}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ,
					"&{typ1}", typ1,
					"&{typeName}", typeName));
				if (sbi != null) { // generate interface
					sbi.append(modify(template +";"+LN,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", typ));
				}
				template =
(_genJavadoc ? "\t/** Add values of textnodes of &{d}. */"+LN : "")+
"\tpublic void set&{name}(&{typ1} x)";
				setters.append(modify(template +
"{"+LN+
"\t\t_&{iname}.clear(); if (x==null) return;"+LN+
"\t\tfor (&{typ} y:x){"+LN+
"\t\t\t&{typeName} z=new &{typeName}();"+LN+
"\t\t\tz._$value=y;add&{iname}(z);"+LN+
"\t\t}"+LN+
"\t}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ,
					"&{typ1}", typ1,
					"&{typeName}", typeName));
				if (sbi != null) { // generate interface
					sbi.append(modify(template +";"+LN,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ1}", typ1));
				}
			} else { // single value
				jGet = "String".equals(typ) ?
"org.xdef.json.JsonUtil.jstringFromXML(_&{iname}.get$value())"
					: "_&{iname}.get$value()";
				// getter
				template =
(_genJavadoc ? "\t/** Get JSON value of textnode of &{d}."+LN+
"\t * @return value of text of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} jget&{name}()";
				getters.append(modify(template
					+"{return _&{iname}==null?null:" + jGet + ";}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ));
				if ("org.xdef.sys.SDatetime".equals(typ)) {
					getters.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Date jdateOf&{name}(){"+
"return org.xdef.sys.SDatetime.getDate(jget&{name}());}"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp jtimestampOf&{name}(){"+
"return org.xdef.sys.SDatetime.getTimestamp(jget&{name}());}"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar jcalendarOf&{name}(){"+
"return org.xdef.sys.SDatetime.getCalendar(jget&{name}());}"+LN,
						"&{d}" , xe.getName(),
						"&{name}", name));
				}
				if (sbi != null) { // generate interface
					sbi.append(modify(template + ";" + LN,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", typ));
					if ("org.xdef.sys.SDatetime".equals(typ)) {
						getters.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Date jdateOf&{name};"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp jtimestampOf&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar jcalendarOf&{name}();"+LN,
							"&{d}" , xe.getName(),
							"&{name}", name));
					}
				}
				jSet = "String".equals(typ)
					? "org.xdef.json.JsonUtil.jstringToXML(x,false)":"x";
				// setter
				template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x)";
					setters.append(modify(template+"{"+LN+
"\t\tif(_&{iname}==null)set&{iname}(new &{typeName}());"+LN+
"\t\t_&{iname}.set$value("+ jSet + ");"+LN+
"\t}"+LN,
						"&{name}", name,
						"&{iname}", iname,
						"&{d}", xe.getName(),
						"&{typ}", typ,
						"&{typeName}", typeName));
				if ("org.xdef.sys.SDatetime".equals(typ)) {
					template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x)"+
"{jset&{name}(x==null?null:new org.xdef.sys.SDatetime(x));}"+LN;
					setters.append(modify(template,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", "java.util.Date"));
					setters.append(modify(template,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", "java.sql.Timestamp"));
					setters.append(modify(template,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", "java.util.Calendar"));
				}
				if (sbi != null) { // generate interface
					sbi.append(modify(template +  ";" + LN,
						"&{name}", name,
						"&{d}", xe.getName(),
						"&{typ}", typ));
					if ("org.xdef.sys.SDatetime".equals(typeName)) {
						template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void jset&{name}(&{typ} x);"+LN;
						setters.append(modify(template,
							"&{name}", name,
							"&{d}", xe.getName(),
							"&{typ}", "java.util.Date"));
						setters.append(modify(template,
							"&{name}", name,
							"&{d}", xe.getName(),
							"&{typ}", "java.sql.Timestamp"));
						setters.append(modify(template,
							"&{name}", name,
							"&{d}", xe.getName(),
							"&{typ}", "java.util.Calendar"));
					}
				}
			}
	}

	/** Generation of Java code of class composed from XDElement.
	 * @param xelem XDElement from which Java code is composed.
	 * @param index index of model.
	 * @param className class name.
	 * @param extClass class extension and interfaces or an empty string.
	 * @param interfaceName name of interface.
	 * @param classNameBase prefix for inner class names.
	 * @param packageName name of package.
	 * @param components Map with components.
	 * @param clsNames Set with class names or null.
	 * @param isRoot if true then this is root element.
	 * @return string wit Java code.
	 */
	private	String genComponent(final XElement xelem,
		final int index,
		final String className,
		final String extClass,
		final String interfaceName,
		final String classNameBase,
		final String packageName,
		final Map<String, String> components,
		final Set<String> clsNames,
		final boolean isRoot) {
		String extClazz = extClass;
		String interfcName = interfaceName;
		_components = components;
		XElement xe = xelem;
		if (isRoot && xe.getName().contains(":json")) {
			String u = xe.getNSUri();
			if ((XDConstants.JSON_NS_URI.equals(u)
				|| XDConstants.JSON_NS_URI_W3C.equals(u))
				&& xe._childNodes!=null && xe._childNodes.length==1
				&& xe._childNodes[0].getKind()==XMNode.XMELEMENT) {
				xe = (XElement) xe._childNodes[0];
			}
		}
		final String model = xe.getName();
		final Set<String> classNames = new HashSet<String>(RESERVED_NAMES);
		if (clsNames != null) {
			classNames.addAll(clsNames);
		}
		if (isRoot && className != null) {
			classNames.add(className);
		}
		final String xdname = xe.getXMDefinition().getName();
		int ndx = model.indexOf(':');
		final String localName = ndx >= 0 ? model.substring(ndx+1) : model;
		final String clazz = className == null ? localName : className;
		final StringBuilder vars = new StringBuilder();
		final Set<String> varNames = new HashSet<String>();
		final StringBuilder getters = new StringBuilder();
		final StringBuilder xpathes = new StringBuilder();
		final StringBuilder setters = new StringBuilder();
		final StringBuilder creators = new StringBuilder();
		final StringBuilder genNodeList = new StringBuilder();
		final StringBuilder innerClasses = new StringBuilder();
		final StringBuilder sbi = // interface
			interfcName.length() == 0 ? null : new StringBuilder();
		final Properties nsmap = new Properties();
		addNSUri(nsmap, xe);
		final Map<String, String> atttab = new LinkedHashMap<String, String>();
		// attributes
		for (XMData xmdata : xe.getAttrs()) {
			if (xmdata.isIgnore() || xmdata.isIllegal()) {
				continue;
			}
			XData xdata = (XData) xmdata;
			addNSUri(nsmap, xdata);
			String name = checkBind(xe, xdata);
			boolean ext = false;
			if (name != null) {
				ndx = name.indexOf(" %with ");
				if (ndx > 0) {
					if (extClazz.startsWith(" extends")) {
						ext = true;
						name = name.substring(0, ndx);
					} else {
						if (isRoot) {
							ext = true;
							extClazz=" extends "+name.substring(ndx+7)+extClazz;
							//"In command "%class &{0}" is missing parameter
							//"extends". In command "%bind &{2}" is
							//parameter "%with &{1}!
							_reporter.error(XDEF.XDEF375,
								className,
								name.substring(ndx+7),
								name.substring(0, ndx));
							name = name.substring(0, ndx);
						} else {
							//Class &{0} is not root. It can't be extended
							//to &{1} according to command %bind &{2}
							_reporter.error(XDEF.XDEF376,
								className,
								name.substring(ndx+7),
								name.substring(0, ndx));
						}
					}
				}
			} else {
				name = javaName(xdata.getName());
			}
			name = addVarName(varNames, name, xdata.getXDPosition(), ext);
			genAttrNameVariable(name, vars);
			if (!ext) {
				genBaseVariable(xdata, name, 1, "attribute", vars);
				genBaseGetterMethod(xdata, name, 1, "attribute", getters, sbi);
				genBaseSetterMethod(xdata, name, 1, "attribute", setters, sbi);
				genBaseXPosMethod(name, "attribute", xpathes, sbi);
			}
			genCreatorOfAttribute(xdata, name, creators);
			atttab.put(xdata.getXDPosition(),
				getParsedResultGetter(xdata) + ";" + name);
		}
		// Generate getters of namespace attributes
		for (Map.Entry<Object,Object> item : nsmap.entrySet()) {
			final String name = (String) item.getKey();
			final String value = (String) item.getValue();
			final String xmlname = addVarName(varNames,
				name.replace('$', ':'), xe.getXDPosition()+"/@"+name, false);
			getters.append(modify(
(_genJavadoc ? ("\t/** Get value of \"&{xmlname}\" attribute."+LN+
"\t * @return string with value of attribute"+LN+
"\t */"+LN) : "")+
"\tpublic String get&{name}() {return \"&{value}\";}"+LN,
				"&{name}", name,
				"&{xmlname}", xmlname,
				"&{value}", value));
			String s =
("\t\tel.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,"+LN+
"\t\t\t\"" + xmlname + "\", \"" + value + "\");"+LN);
			creators.append(s);
		}
		final XNode[] nodes = (XNode[]) xe.getChildNodeModels();
		final Map<String, String> xctab = new LinkedHashMap<String, String>();
		final Map<String, String> txttab = new LinkedHashMap<String, String>();
		final Stack<Integer> groupStack = new Stack<Integer>();
		for (int i = 0, txtcount = 0, groupMax = 1; i < nodes.length; i++) {
			final XNode node = nodes[i];
			if (node.isIgnore() || node.isIllegal()) {
				continue;
			}
			if (node.getKind() == XMNode.XMCHOICE ||
				node.getKind() == XMNode.XMMIXED ||
				node.getKind() == XMNode.XMSEQUENCE) {
				if (node.maxOccurs() > 1) {
					groupMax = groupMax > 1 ? groupMax : node.maxOccurs();
				}
				groupStack.push(groupMax);
				continue;
			}
			if (node.getKind() == XMNode.XMSELECTOR_END) {
				groupMax = groupStack.pop();
			}
			if (node.getKind() == XMNode.XMTEXT) {
				final XData xdata = (XData) node;
				String name, newClassName;
				newClassName = name = checkBind(xe, xdata);
				boolean ext = false;
				if (name != null) {
					if ((ndx = name.indexOf(" %with ")) > 0) {
						if (extClazz.startsWith(" extends ")) {
							ext = true;
							name = name.substring(0, ndx);
						} else {
							if (isRoot) {
								ext = true;
								extClazz = " extends "
									+ name.substring(ndx+7) + extClazz;
								//"In command "%class &{0}" is missing parameter
								//"extends". In command "%bind &{2}" is
								//parameter "%with &{1}!
								_reporter.error(XDEF.XDEF375,
									className,
									name.substring(ndx+7),
									name.substring(0, ndx));
								name = name.substring(0, ndx);
							} else {
								//Class &{0} is not root. It can't be extended
								//to &{1} according to command %bind &{2}
								_reporter.error(XDEF.XDEF376,
									className,
									name.substring(ndx+7),
									name.substring(0, ndx));
							}
						}
						newClassName = name;
					}
				} else {
					final boolean xunique = checkUnique(nodes, i);
					name = "$value"; // name of text value
					if (!xunique) {
						if (txtcount > 0) {
							name += String.valueOf(txtcount);
						}
						txtcount++;
					}
					newClassName = name;
				}
				name = addVarName(varNames, name, xdata.getXDPosition(), ext);
				classNames.add(newClassName);
				if (!ext) {
					genBaseVariable(xdata, name, groupMax, "text node", vars);
					genBaseGetterMethod(xdata,
						name, groupMax, "text node", getters, sbi);
					genBaseSetterMethod(xdata,
						name, groupMax, "text node", setters, sbi);
					genBaseXPosMethod(name, "text node", xpathes, sbi);
				}
				String s =
((_genJavadoc ? "\t/** Indexes of values of &{d} \""+name.replace('$', ':')+
"\".*/"+LN : "")+
"\tprivate " + (groupMax > 1 ? "StringBuilder":"char") + " _$" + name+"= "+
(groupMax > 1 ? "new StringBuilder()" : "(char) -1") + ";"+LN);
				vars.append(s);
				genTextNodeCreator(xdata, name, groupMax, genNodeList);
				txttab.put(node.getXDPosition(), (groupMax == 1 ? "1" : "2")
					+ "," + getParsedResultGetter(xdata) + ";" + name);
if (isRoot && xe._json == 2 && nodes.length == 1) {
	genJsonDirectGetterSetter(xe, xdata, name, setters, getters, sbi);
}
			} else if (node.getKind() == XMNode.XMELEMENT) {
				XElement xe1 = (XElement) node;
				final int max = groupMax > 1 ? groupMax : xe1.maxOccurs();
				String name = checkBind(xe, xe1);
				boolean ext = false;
				boolean isRecurseRef = name != null && name.isEmpty();
				if (isRecurseRef) {
					name = null;
				}
				String newClassName;
				if (name != null) {
					newClassName = name;
					if ((ndx = name.indexOf(';')) > 0) {
						newClassName = javaName(name.substring(0, ndx));
						name = javaName(name.substring(ndx+1));
					} else if ((ndx = name.indexOf(" %with ")) > 0) {
						if (extClazz.startsWith(" extends")) {
							ext = true;
							name = name.substring(0, ndx);
						} else {
							if (isRoot) {
								ext = true;
								extClazz = " extends "
									+ name.substring(ndx+7)+extClazz;
								//"In command "%class &{0}" is missing parameter
								//"extends". In command "%bind &{2}" is
								//parameter "%with &{1}!
								_reporter.error(XDEF.XDEF375,
									className,
									name.substring(ndx+7),
									name.substring(0, ndx));
								name = name.substring(0, ndx);
							} else {
								//Class &{0} is not root. It can't be extended
								//to &{1} according to command %bind &{2}
								_reporter.error(XDEF.XDEF376,
									className,
									name.substring(ndx+7),
									name.substring(0, ndx));
							}
						}
						newClassName = name;
					}
				} else {
					newClassName = name = javaName(xe1.getName());
				}
				// if the element is not processed by user XComponent
				// and if it is unique and if the only child node of this node
				// is this text node and if it has no attributes then we process
				// it is processed same way as an attribute of the parent class.
				final String xcClass0 = isRecurseRef
					? name : getXDPosition(xe1, interfcName.length() > 0);
				String xcClass = xcClass0;
				if (xcClass0 != null) {
					if (xcClass.indexOf("%ref ") ==0) {
						xcClass = xcClass.substring(5);
					}
					if (xcClass.startsWith("interface ")) {
						xcClass = xcClass.substring(10);
						ndx = xcClass.lastIndexOf('.');
						if (ndx > 0
							&& xcClass.substring(0, ndx).equals(packageName)) {
							xcClass = xcClass.substring(ndx + 1);
						}
						xcClass = xcClass.replace('#','.');
					} else if ((ndx = xcClass.indexOf(' ')) > 0) {
						// remove extends and interface
						xcClass =  xcClass.substring(0, ndx);
						ndx = xcClass.lastIndexOf('.');
						if (ndx > 0
							&& xcClass.substring(0, ndx).equals(packageName)) {
							xcClass = xcClass.substring(ndx + 1);
						}
						xcClass = xcClass.replace('#','.');
					}
				}
				String iname = getUniqueName(name, RESERVED_NAMES);
				boolean nameChanged = !iname.equals(name);
				String chgName = newClassName;
				newClassName = getUniqueName(chgName, classNames);
				nameChanged |= !chgName.equals(newClassName);
				chgName = iname;
				iname = getUniqueName(getUniqueName(iname,classNames),varNames);
				nameChanged |= !chgName.equals(iname);
				if (nameChanged) {
					if (ext) {
						//Getter/setter name &{0} in &{1} can't be used.
						//Please change name by command %bind
						_reporter.error(XDEF.XDEF371,
							name, node.getXDPosition());
					} else {
						//Getter/setter name &{0} in &{1} was changed to
						//&{2}. You can define other name by command %bind
						_reporter.warning(XDEF.XDEF360,
							name, node.getXDPosition(), iname);
					}
				}
				varNames.add(iname);
				String typeName;
				if (xcClass != null) {
					typeName = xcClass;
					if ((ndx = xcClass.lastIndexOf('.')) > 0
						&& (xcClass.substring(ndx + 1)).equals(className)) {
						typeName = xcClass.substring(ndx + 1);
					}
				} else {
					typeName = classNameBase + '#' + newClassName;
					_components.put(xe1.getXDPosition(),
					packageName.length() > 0
						? packageName+'.'+typeName : typeName);
				}
				ndx = typeName.lastIndexOf('.');
				if (ndx > 0
					&& packageName.equals(typeName.substring(0, ndx))) {
					typeName = typeName.substring(ndx + 1);
				}
				typeName = typeName.replace('#', '.');
				if (!ext) {
					genVariableFromModel(typeName, iname, max, "element", vars);
					String mname = null;
					String mURI = null;
					String mXDPos = null;
					if (xe1.isReference()) {
						mname = xe1.getName();
						mURI = xe1.getNSUri();
						mXDPos = xe1.getXDPosition();
					}
					genGetterMethodFromChildElement(node,
						typeName, iname, max, "element", getters, sbi);
					genSetterMethodOfChildElement(typeName, iname, max,
						mname, mURI, mXDPos, "element", setters, sbi);
				}
				if (isJsonItem(xe1)) {
					genJsonGetterAndSetters(xe1, typeName, iname, max,
						setters, getters, sbi, classNames, varNames);
				}
				genChildElementCreator(iname, genNodeList, max > 1);
				// generate if it was not declared as XComponent
				String xval = (max == 1 ? "1" : "2") + "," + iname + ";";
				if (xcClass0 == null || xcClass0.startsWith("interface ")) {
					xctab.put(node.getXDPosition(), xval + newClassName);
					classNames.add(newClassName);
					innerClasses.append(genComponent(xe1, //Elememnt model
						i, // index
						newClassName, //class name
						"", //no class extension and interfaces
						(xcClass0 != null)? xcClass0.substring(10): "",
						(packageName.length() > 0 ? packageName +"." : "")
							+ classNameBase + '#' + newClassName, //interface
						"", //classNameBase
						components, //Map with components
						classNames, //Set with class names or null
						false)); //not root element.
					innerClasses.append('}').append(LN);
				} else {//other root class
					xctab.put(node.getXDPosition(), xval + xcClass);
				}
			}
		}
		if (isRoot) {
			_interface = sbi;
			final int i = interfcName.lastIndexOf('.');
			if (i > 0 && interfcName.substring(0, i).equals(packageName)) {
				interfcName = interfcName.substring(i + 1);
			}
		}
		if (clazz.length() == 0) {
			return null;
		}
		if (xe.isReference()) {
			String xpos = _components.get(xe.getReferencePos());
			if (xpos != null && xpos.startsWith("interface ")) {
				xpos = xpos.substring(10);
				if (!xpos.equals(interfcName)) {
					ndx = extClazz.indexOf("implements ");
					if (ndx >= 0) {
						if (extClazz.indexOf(xpos) < 0) {
							extClazz = extClazz.substring(0, ndx+11)
								+ xpos + "," + extClazz.substring(ndx+11);
						}
					} else {
						extClazz += " implements " + xpos;
					}
				}
			}
		}
		String toXml =
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Create XML element or text node from default model"+LN+
"\t * as an element created from given document."+LN+
"\t * @param doc XML Document or <tt>null</tt>."+LN+
"\t * If the argument is null <tt>null</tt> then document is created with"+LN+
"\t * created document element."+LN+
"\t * @return XML element belonging to given document from default model."+LN+
"\t */"+LN) : "")+
"\tpublic org.w3c.dom.Node toXml(org.w3c.dom.Document doc) {"+LN;
		if (xe.getName().endsWith("$any") || "*".equals(xe.getName())) {
			toXml +=
"\t\tif (doc==null) {"+LN+
"\t\t\treturn org.xdef.xml.KXmlUtils.parseXml(XD_Any)"+LN+
"\t\t\t\t.getDocumentElement();"+LN+
"\t\t} else {"+LN+
"\t\t\treturn (org.w3c.dom.Element)"+LN+
"\t\t\t\tdoc.adoptNode(org.xdef.xml.KXmlUtils.parseXml(XD_Any)"+LN+
"\t\t\t\t\t.getDocumentElement());"+LN+
"\t\t}"+LN+
"\t}"+LN;
		} else if (creators.length() == 0 && genNodeList.length() == 0) {
			toXml +=
"\t\treturn doc!=null ? doc.createElementNS(XD_NamespaceURI, XD_NodeName)"+LN+
"\t\t\t: org.xdef.xml.KXmlUtils.newDocument("+LN+
"\t\t\t\tXD_NamespaceURI, XD_NodeName, null).getDocumentElement();"+LN+
"\t}"+LN;
		} else {
			toXml +=
"\t\torg.w3c.dom.Element el;"+LN+
"\t\tif (doc==null) {"+LN+
"\t\t\tdoc = org.xdef.xml.KXmlUtils.newDocument(XD_NamespaceURI,"+LN+
"\t\t\t\tXD_NodeName, null);"+LN+
"\t\t\tel = doc.getDocumentElement();"+LN+
"\t\t} else {"+LN+
(isRoot ? "\t\t\tel = doc.createElementNS(XD_NamespaceURI, XD_NodeName);"+LN+
"\t\t\tif (doc.getDocumentElement()==null) doc.appendChild(el);"+LN
: "\t\t\tel = doc.createElementNS(XD_NamespaceURI, XD_NodeName);"+LN
)+
"\t\t}"+LN+ creators;
			if (genNodeList.length() > 0) {
				toXml += "\t\tfor (org.xdef.component.XComponent x:"+
					" xGetNodeList())"+LN+
"\t\t\tel.appendChild(x.toXml(doc));"+LN;
			}
			toXml += "\t\treturn el;"+LN+"\t}"+LN;
		}
		toXml +=
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Create JSON object from this XComponent (marshal to JSON)"+LN+
"\t * @return JSON object created from this XComponent."+LN+
"\t */"+LN) : "")+
"\tpublic Object toJson() {"+
	"return org.xdef.json.JsonUtil.xmlToJson(toXml());}"+LN;
////////////////////////////////////////////////////////////////////////////////
		String result =
(_genJavadoc ?
"/** Object of XModel \""+model+"\" from X-definition \""+xdname+"\".*/"+LN
: "") +
"public "+(isRoot?"":"static ")+"class "+
			clazz + extClazz + (interfcName.length() > 0 ?
				extClazz.contains("implements ")
				? ", " + interfcName : (" implements " + interfcName)
				: extClazz.contains("implements ")
				? ",org.xdef.component.XComponent"
				: " implements org.xdef.component.XComponent")+ "{"+LN;
		result += genSeparator("Getters", _genJavadoc & getters.length() > 0)
			+ getters
			+ genSeparator("Setters", _genJavadoc & setters.length() > 0)
			+ setters
			+ xpathes.toString() +
"//<editor-fold defaultstate=\"collapsed\" desc=\"Implementation of XComponent interface\">"+LN+
////////////////////////////////////////////////////////////////////////////////
(_genJavadoc ? "\t/** Get JSON version: 0 not set, 1 .. W3C, 2 .. XDEF. */+LN"
: "") +
"\tpublic final static byte JSON = " + xe._json + ";" +LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Create XML element from this XComponent (marshal)."+LN+
"\t * If the argument is null <tt>null</tt> then document is created with"+LN+
"\t * created document element."+LN+
"\t * @return XML element created from thos object."+LN+
"\t */"+LN) : "") +
"\tpublic org.w3c.dom.Element toXml()"+LN+
"\t\t{return (org.w3c.dom.Element) toXml((org.w3c.dom.Document) null);}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Get name of XML node used for construction of this object."+LN+
"\t * @return name of XML node used for construction of this object."+LN+
"\t */"+LN) : "") +
"\tpublic String xGetNodeName() {return XD_NodeName;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Update parameters of XComponent."+LN+
"\t * @param parent p XComponent."+LN+
"\t * @param name name of element."+LN+
"\t * @param ns name space."+LN+
"\t * @param xPos XDPosition."+LN+
"\t */"+LN) : "") +
"\tpublic void xInit(org.xdef.component.XComponent p,"+LN+
"\t\tString name, String ns, String xdPos) {"+LN+
"\t\tXD_Parent=p; XD_NodeName=name; XD_NamespaceURI=ns; XD_Model=xdPos;"+LN+
"\t}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Get namespace of node used for construction of this object."+LN+
"\t * @return namespace of node used for construction of this object."+LN+
"\t */"+LN) : "") +
"\tpublic String xGetNamespaceURI() {return XD_NamespaceURI;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Get XPosition of node."+LN+
"\t * @return XPosition of node."+LN+
"\t */"+LN) : "") +
"\tpublic String xGetXPos() {return XD_XPos;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Set XPosition of node."+LN+
"\t * @param xpos XPosition of node."+LN+
"\t */"+LN) : "") +
"\tpublic void xSetXPos(String xpos){XD_XPos = xpos;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Get index of node."+LN+
"\t * @return index of node."+LN+
"\t */"+LN) : "") +
"\tpublic int xGetNodeIndex() {return XD_Index;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? (
"\t/** Set index of node."+LN+
"\t * @param index index of node."+LN+
"\t */"+LN) : "") +
"\tpublic void xSetNodeIndex(int index) {XD_Index = index;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Get parent XComponent."+LN+
"\t * @return parent XComponent object or null if this object is root."+LN+
"\t */"+LN) : "") +
"\tpublic org.xdef.component.XComponent xGetParent() {return XD_Parent;}"
+LN+"\t@Override"+LN+
(_genJavadoc ? ("\t/** Get user object."+LN+
"\t * @return assigned user object."+LN+
"\t */"+LN) : "") +
"\tpublic Object xGetObject() {return XD_Object;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Set user object."+LN+
"\t * @param obj assigned user object."+LN+
"\t */"+LN) : "") +
"\tpublic void xSetObject(final Object obj) {XD_Object = obj;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Create string about this object."+LN+
"\t * @return string about this object."+LN+
"\t */"+LN) : "") +
"\tpublic String toString() {return \"XComponent: \"+xGetModelPosition();}"+LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Get XDPosition of this XComponent."+LN+
"\t * @return string withXDPosition of this XComponent."+LN+
"\t */"+LN) : "") +
"\tpublic String xGetModelPosition() {return XD_Model;}"+LN+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Get index of model of this XComponent."+LN+
"\t * @return index of model of this XComponent."+LN+
"\t */"+LN) : "") +
"\tpublic int xGetModelIndex() {return "+index+";}"+LN+

////////////////////////////////////////////////////////////////////////////////
			genSeparator("Private methods", _genJavadoc) + toXml+
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Create list of XComponents for creation of XML."+LN+
"* @return list of XComponents."+LN+
"\t */"+LN) : "") +
"\tpublic java.util.List<org.xdef.component.XComponent> xGetNodeList() {"
			+LN;
		if (genNodeList.length() == 0) {
			result +=
"\t\treturn new java.util.ArrayList<org.xdef.component.XComponent>();"+LN+
"\t}"+LN;
		} else {
			result += genNodeList + "\t\treturn a;"+LN+"\t}"+LN;
		}
		if (isRoot) {
			if ((byteArrayEncoding & 1) != 0) { //base64
				result +=
(_genJavadoc ? ("\t/** Decode Base64 string."+LN+
"\t * @param s string with encoded value."+LN+
"\t * @return decoded byte array."+LN+
"\t */"+LN) : "")+
"\tprivate static byte[] decodeBase64(String s) {"+LN+
"\t\ttry {"+LN+
"\t\t\treturn org.xdef.sys.SUtils.decodeBase64(s);"+LN+
"\t\t} catch (org.xdef.sys.SException ex) {"+LN+
"\t\t\tthrow new org.xdef.sys.SRuntimeException(ex.getReport());"+LN+
"\t\t}"+LN+
"\t}"+LN+
"\t/** Encode byte array to Base64 string."+LN+
"\t * @param b byte array."+LN+
"\t * @return string with encoded byte array."+LN+
"\t */"+LN+
"\tprivate static String encodeBase64(byte[] b) {"+LN+
"\t\t\treturn new String(org.xdef.sys.SUtils.encodeBase64(b),"+LN+
"\t\t\tjava.nio.charset.Charset.forName(\"UTF-8\"));"+LN+
"\t}"+LN;
			}
			if ((byteArrayEncoding & 2) != 0) { //hex
				result +=
(_genJavadoc ? ("\t/** Decode hexadecimal string."+LN+
"\t * @param s string with encoded value."+LN+
"\t * @return decoded byte array."+LN+
"\t */"+LN) : "")+
"\tprivate static byte[] decodeHex(String s) {"+LN+
"\t\ttry {"+LN+
"\t\t\treturn org.xdef.sys.SUtils.decodeHex(s);"+LN+
"\t\t} catch (org.xdef.sys.SException ex) {"+LN+
"\t\t\tthrow new org.xdef.sys.SRuntimeException(ex.getReport());"+LN+
"\t\t}"+LN+
"\t}"+LN+
(_genJavadoc ? ("\t/** Encode byte array to hexadecimal string."+LN+
"\t * @param b byte array."+LN+
"\t * @return string with encoded byte array."+LN+
"\t */"+LN) : "")+
"\tprivate static String encodeHex(byte[] b) {"+LN+
"\t\treturn new String(org.xdef.sys.SUtils.encodeHex(b),"+LN+
"\t\t\tjava.nio.charset.Charset.forName(\"UTF-8\"));"+LN+
"\t}"+LN;
			}
		}
		result +=
(_genJavadoc ? ("\t/** Create an empty object."+LN+
"\t * @param xd XDPool object from which this XComponent was generated."+LN+
"\t */"+LN) : "")+
"\tpublic "+clazz+"() {}"+LN+
(_genJavadoc ? ("\t/** Create XComponent."+LN+
"\t * @param p parent component."+LN+
"\t * @param name name of element."+LN+
"\t * @param ns namespace URI of element."+LN+
"\t * @param xPos XPOS of actual element."+LN+
"\t * @param XDPos XDposition of element model."+LN+
"\t */"+LN) : "")+
"\tpublic " + clazz +
"(org.xdef.component.XComponent p,"+LN+
"\t\tString name, String ns, String xPos, String XDPos) {"+LN+
"\t\tXD_NodeName=name; XD_NamespaceURI=ns;"+LN+
"\t\tXD_XPos=xPos;"+LN+
"\t\tXD_Model=XDPos;"+LN+
"\t\tXD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;"+LN+
"\t}"+LN+
(_genJavadoc ? ("\t/** Create XComponent from XXNode."+LN+
"\t * @param p parent component."+LN+
"\t * @param x XXNode object."+LN+
"\t */"+LN) : "")+
"\tpublic " + clazz +
"(org.xdef.component.XComponent p,org.xdef.proc.XXNode x){"+LN+
"\t\torg.w3c.dom.Element el=x.getElement();"+LN+
"\t\tXD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();"+LN+
"\t\tXD_XPos=x.getXPos();"+LN+
"\t\tXD_Model=x.getXMElement().getXDPosition();"+LN+
"\t\tXD_Object = (XD_Parent=p)!=null ? p.xGetObject() : null;"+LN+
"\t\tif (!\"" + xe.getDigest() + "\".equals("+LN+ // check digest
"\t\t\tx.getXMElement().getDigest())) { //incompatible element model"+LN+
"\t\t\tthrow new org.xdef.sys.SRuntimeException("+LN+
"\t\t\t\torg.xdef.msg.XDEF.XDEF374);"+LN+
"\t\t}"+LN+
"\t}"+LN+
		vars +
(_genJavadoc ? "\t/** Parent XComponent node.*/"+LN : "") +
"\tprivate org.xdef.component.XComponent XD_Parent;"+LN+
(_genJavadoc ? "\t/** User object.*/"+LN : "") +
"\tprivate Object XD_Object;"+LN+
(_genJavadoc ? "\t/** Node name.*/"+LN : "") +
"\tprivate String XD_NodeName = \"" + xe.getName() + "\";"+LN+
(_genJavadoc ? "\t/** Node namespace.*/"+LN : "") +
"\tprivate String XD_NamespaceURI" +
	(xe.getNSUri() != null ? " = \"" + xe.getNSUri() + "\"" : "") + ";"+LN+
(_genJavadoc ? "\t/** Node index.*/"+LN : "") +
"\tprivate int XD_Index = -1;"+LN+
(genNodeList.length() == 0 ? "" :
(_genJavadoc ? "\t/** Internal use.*/"+LN : "") +
"\tprivate int XD_ndx;"+LN) +
(_genJavadoc ? "\t/** Node xpos.*/"+LN : "") +
"\tprivate String XD_XPos;"+LN+
(_genJavadoc ? "\t/** Node XD position.*/"+LN : "") +
"\tprivate String XD_Model=\"" + xe.getXDPosition() + "\";"+LN+
("$any".equals(xe.getName()) || "*".equals(xe.getName()) ?
(_genJavadoc ? "\t/** Content of xd:any.*/"+LN : "") +
"\tprivate String XD_Any;"+LN : "");
		result +=
"\t@Override"+LN+
(_genJavadoc ? "\t/** Set value of text node."+LN+
"\t * @param x Actual XXNode (from text node)."+LN+
"\t * @param parseResult parsed value."+LN+
"\t */"+LN : "");
		if (txttab.isEmpty()) {
			result +=
"\tpublic void xSetText(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){}"+LN;
		} else if (txttab.size() == 1) {
			Entry<String, String> e = txttab.entrySet().iterator().next();
			String val = e.getValue();
			ndx = val.indexOf(';');
			String name = val.substring(ndx + 1);
			String getter = val.substring(2, ndx);
			String s = val.startsWith("1") ?
				"\t\tset" + name +"("+getter+")"
				: "\t\tlistOf" + name + "().add("+getter+")";
			result +=
"\tpublic void xSetText(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){"+LN+
(val.startsWith("1") ?
"\t\t_$" + name + "=(char) XD_ndx++;"+LN+ s + ";"+LN+"\t}"+LN
:"\t\t_$" + name + ".append((char) XD_ndx++);"+LN+ s + ";"+LN+"\t}"+LN);
		} else {
			result +=
"\tpublic void xSetText(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){"+LN;
			String s = "";
			for(Entry<String, String> e: txttab.entrySet()) {
				s += (s.length() == 0 ? "\t\t" : "\t\t} else ")
					+ "if (\"" + e.getKey()
					+ "\".equals(x.getXMNode().getXDPosition())){"+LN;
				String val = e.getValue();
				ndx = val.indexOf(';');
				String name = val.substring(ndx + 1);
				String getter = val.substring(2, ndx);
				s += (val.startsWith("1")
					? "\t\t\t_$"+name+"=(char) XD_ndx++;"+LN+"\t\t\tset" + name
					: "\t\t\t_$" + name + ".append((char) XD_ndx++);"+LN+
						"\t\t\tget" + name + "().add") + "("+getter+");"+LN;
			}
			result += s + "\t\t}"+LN+"\t}"+LN;
		}
		result +=
"\t@Override"+LN+
(_genJavadoc ? "\t/** Set value of attribute."+LN+
"\t * @param x Actual XXNode (from attribute node)."+LN+
"\t * @param parseResult parsed value."+LN+
"\t */"+LN : "");
		if (atttab.isEmpty()) {
			result +=
"\tpublic void xSetAttr(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){}"+LN;
		} else if (atttab.size() == 1) {
			String val = atttab.entrySet().iterator().next().getValue();
			ndx = val.indexOf(';');
			String getter = val.substring(0, ndx);
			result +=
"\tpublic void xSetAttr(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){"+LN+
"\t\tXD_Name_" + val.substring(ndx + 1) + " = x.getNodeName();"+LN+
"\t\tset" + val.substring(ndx + 1) + "(" + getter + ");"+LN+"\t}"+LN;
		} else {
			result +=
"\tpublic void xSetAttr(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult) {"+LN;
			String s = "";
			for (Iterator<Entry<String, String>> it =
				atttab.entrySet().iterator(); it.hasNext();) {
				Entry<String, String> en = it.next();
				s += s.length() == 0 ? "\t\t" : " else ";
				String key = en.getKey();
				ndx = key.lastIndexOf('/');
				key = key.substring(ndx);
				s += (it.hasNext()
? "if (x.getXMNode().getXDPosition().endsWith(\"" + key + "\")) {" : "{")+LN;
				String val = en.getValue();
				ndx = val.indexOf(';');
				s += "\t\t\tXD_Name_" + val.substring(ndx + 1)
					+ " = x.getNodeName();"+LN;
				s += "\t\t\tset" + val.substring(ndx+1);
				String getter = val.substring(0, ndx);
				s += "(" + getter + ");"+LN+"\t\t}";
			}
			result += s+LN+"\t}"+LN;
		}
		result +=
"\t@Override"+LN+
(_genJavadoc ? "\t/** Create instance of child XComponent."+LN+
"\t * @param x actual XXNode."+LN+
"\t * @return new empty child XCopmponent."+LN+
"\t */"+LN : "");
		if (xctab.isEmpty()) {
			result +=
"\tpublic org.xdef.component.XComponent xCreateXChild("+LN+
"\t\torg.xdef.proc.XXNode x)"+LN+
"\t\t{return null;}"+LN;
		} else if (xctab.size() == 1) {
			Entry<String, String> e = xctab.entrySet().iterator().next();
			String s = e.getValue().replace('#', '.');
			s = s.length() != 0
				? "new "+s.substring(s.indexOf(";") + 1)+"(this, x)" : "this";
			result +=
"\tpublic org.xdef.component.XComponent xCreateXChild("+LN+
"\t\torg.xdef.proc.XXNode x)"+LN+
"\t\t{return " + s + ";}"+LN;
		} else {
			boolean dflt = false;
			result +=
"\tpublic org.xdef.component.XComponent xCreateXChild("+LN+
"\t\torg.xdef.proc.XXNode x) {"+LN;
			result +=
"\t\tString s = x.getXMElement().getXDPosition();"+LN;
			for (Iterator<Entry<String, String>>i=xctab.entrySet().iterator();
				i.hasNext();) {
				Entry<String, String> e = i.next();
				String s = e.getValue().replace('#', '.');
				if (s.length() == 0) {
					dflt = true;
				} else {
					result += ((i.hasNext() || dflt)
						? "\t\tif (\""+e.getKey()
							+ "\".equals(s))"+LN+"\t\t\treturn new "
							+ s.substring(s.indexOf(";") + 1)+"(this, x);"
						: ("\t\treturn new "
							+ s.substring(s.indexOf(";") + 1)+"(this, x); // "
							+ e.getKey()))
						+ LN;
				}
			}
			result += (dflt ? "\t\treturn " + dflt + ';'+LN : "") + "\t}"+LN;
		}
		result +=
"\t@Override"+LN+
(_genJavadoc ? "\t/** Add XComponent object to local variable."+LN+
"\t * @param x XComponent to be added."+LN+
"\t */"+LN : "");
		if ("$any".equals(xe.getName()) || "*".equals(xe.getName())) {
			result +=
"\tpublic void xAddXChild(org.xdef.component.XComponent x){}"+LN;
		} else if (xctab.isEmpty()) {
			result +=
"\tpublic void xAddXChild(org.xdef.component.XComponent x){}"+LN;
		} else if (xctab.size() == 1) {
			result +=
"\tpublic void xAddXChild(org.xdef.component.XComponent x){"+LN+
"\t\tx.xSetNodeIndex(XD_ndx++);"+LN;
			String s = xctab.values().iterator().next().replace('#', '.');
			String typ = s.substring(s.indexOf(";") + 1);
			String var = s.substring(2, s.indexOf(";"));
			result += s.charAt(0) == '1' ? "\t\tset" + var + "(" + "(" + typ
				: "\t\tlistOf" + var + "().add((" + typ;
			String key = xctab.keySet().iterator().next();
			result += ") x); //" + key + LN+"\t}"+LN;
		} else {
			boolean first = true;
			result +=
"\tpublic void xAddXChild(org.xdef.component.XComponent x){"+LN+
"\t\tx.xSetNodeIndex(XD_ndx++);"+LN+
"\t\tString s = x.xGetModelPosition();"+LN;
			for (Iterator<Entry<String, String>> i=xctab.entrySet().iterator();
				i.hasNext();) {
				Entry<String, String> e = i.next();
				String s = e.getValue().replace('#', '.');
				if (s.length() > 0) {
					String typ = s.substring(s.indexOf(";") + 1);
					String var = s.substring(2, s.indexOf(";"));
					s = s.charAt(0) == '1'
						? "set" + var + "(" + "(" + typ + ")x);"
						: "listOf" + var + "().add((" + typ + ")x);";
					s += !i.hasNext() ? " //" + e.getKey()+LN : LN;
					if (first) {
						result +=
"\t\tif (\"" + e.getKey() + "\".equals(s))"+LN+"\t\t\t" + s;
						first = false;
					} else {
						if (i.hasNext()) {
							result += "\t\telse if (\"" +
								e.getKey() + "\".equals(s))"+LN+"\t\t\t" + s;
						} else {
							result += "\t\telse"+LN+"\t\t\t" + s + "\t}"+LN;
							break;
						}
					}
				}
			}
		}
		result +=
"\t@Override"+LN+
(_genJavadoc ? "\t/** Set value of xd:any model."+LN+
"\t * @param el Element which is value of xd:any model."+LN+
"\t */"+LN : "")+
"\tpublic void xSetAny(org.w3c.dom.Element el) {";
		if ("$any".equals(xe.getName()) || "*".equals(xe.getName())) {
			result += LN+
"\t\tXD_Any = org.xdef.xml.KXmlUtils.nodeToString(el);"+LN+
"\t}"+LN;
		} else {
			result += "}"+LN;
		}
		result += "// </editor-fold>"+LN+ innerClasses;
		innerClasses.setLength(0); //clean
		varNames.clear();
		_interface = sbi;
		return result;
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param model name of model.
	 * @param className name of generated class.
	 * @param extClass class extension.
	 * @param interfaceName name of interface
	 * @param packageName the package of generated class (may be null).
	 * @param components Map with components.
	 * @param genJavadoc switch to generate JavaDoc.
	 * @return String with generated Java source code.
	 */
	private String genXComponent(final String model,
		final String className,
		final String extClass,
		final String interfaceName,
		final String packageName,
		final Map<String, String> components,
		final boolean genJavadoc) {
		_genJavadoc = genJavadoc;
		final XNode xn = (XElement) _xp.findModel(model);
		if (xn == null || xn.getKind() != XMNode.XMELEMENT) {
			//Model "&{0}" not exsists.
			throw new SRuntimeException(XDEF.XDEF373, model);
		}
		XElement xe = (XElement) xn;
		int ndx = model.indexOf('#');
		String definitionName = model.substring(0, ndx);
		String modelName = model.substring(ndx + 1);
		String result = genComponent(xe, //elememnt model
			-1, //index
			className, //class name
			extClass, // class extension and interfaces or an empty string
			interfaceName,  //interface
			className,  //classNameBase
			packageName, //classNameBase (package)
			components, //Map with components
			null, //Set with class names or null
			true); //root element.
		String hdrTemplate =
"// This file was generated by org.xdef.component.GenXComponent."+LN+
"// XDPosition: \"" +
(definitionName == null ? "" : definitionName) + '#' +	modelName + "\"."+LN+
"// Any modifications to this file will be lost upon recompilation."+LN;
		String packageName1 = packageName;
		String interfaceName1 = interfaceName;
		if (_interface != null) {
			packageName1 = "";
			if ((ndx = interfaceName1.lastIndexOf('.')) > 0) {
				packageName1 = interfaceName1.substring(0, ndx);
				interfaceName1 = interfaceName1.substring(ndx + 1);
			}
			String s = hdrTemplate;
			if (packageName1 != null && packageName1.length() > 0) {
				s += "package " + packageName1 + ";"+LN;
			}
			s += LN+"public interface "+interfaceName1
				+" extends org.xdef.component.XComponent {"+LN;
			_interface.insert(0, s).append("}");
		}
		if (className.length() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder(
			SUtils.modifyString(hdrTemplate, "&{xdpos}",
				(definitionName != null ? "" : definitionName)
					+ '#' + modelName));
		if (packageName1 != null && packageName1.length() > 0) {
			sb.append("package ").append(packageName1).append(';').append(LN);
		}
		return sb.append(result).append("}").toString();
	}

	/** Generate sources of enumerations.
	 * @param xdpool XDPool object.
	 * @param fdir directory where to generate.
	 * @param charset character of generated Java sources.
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param reporter where to write report,.
	 */
	private static void genEnumerations(final XDPool xdpool,
		final File fdir,
		final String charset,
		final boolean genJavadoc,
		final ArrayReporter reporter) {
		Map<String, String> enums =	xdpool.getXComponentEnums();
		if (enums == null || enums.isEmpty()) {
			return;
		}
		for (Entry<String, String> e: enums.entrySet()) {
			String cname = e.getValue();
			int ndx = cname.indexOf(' ');
			String enumName = cname.substring(0, ndx);
			if (enumName.charAt(0) == '%') {
				continue; // just reference
			}
			String values = cname.substring(ndx + 1);
			String packageName = "";
			if ((ndx = enumName.lastIndexOf('.')) > 0) {
				packageName = enumName.substring(0, ndx);
				enumName = enumName.substring(ndx + 1);
			}
			File fparent = new File(fdir, packageName.replace('.', '/'));
			fparent.mkdirs();
			File f = new File(fparent, enumName + ".java");
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				Writer out = charset == null || charset.length() == 0 ?
					new OutputStreamWriter(fos) :
					new OutputStreamWriter(fos, charset);
				out.write(
"//This enumeration was generated by org.xdef.component.GenXComponent"+LN+
"//from declared parser type name: " + e.getKey() + "."+LN+
"//Any modifications to this file will be lost upon recompilation."+LN);
				if (!packageName.isEmpty()) {
					out.write("package " + packageName +";"+LN);
				}
				if (genJavadoc) {
					out.write(LN+"/** This enumeration represents the type "
						+ e.getKey() + " from X-definition.*/"+LN);
				}
				out.write("public enum "
					+ enumName
					+ " implements org.xdef.component.XCEnumeration{"+LN);
				boolean notFirst = false;
				StringTokenizer st = new StringTokenizer(values);
				while (st.hasMoreTokens()) {
					if (notFirst) {
						out.write(","+LN);
					}
					notFirst = true;
					out.write("\t"+ st.nextToken());
				}
				final String template =";"+LN+
(genJavadoc ?
(LN+"\t@Override"+LN+
"\t/** Get object associated with this item of enumeration."+LN+
"\t * @return object associated with this item of enumeration."+LN+
"\t */"+LN) : "\t@Override"+LN) +
"\tpublic final Object itemValue() {return name();}"+LN+
(genJavadoc ?
(LN+"\t@Override"+LN+
"\t/** Get string which is used to create enumeration."+LN+
"\t * @return string which is used to create enumeration."+LN+
"\t */"+LN) : "\t@Override"+LN) +
"\tpublic final String toString() {return name();}"+LN+
(genJavadoc ?
(LN+"\t/** Create enumeration item from an object."+LN+
"\t * @param x Object to be converted."+LN+
"\t * @return the item of this  enumeration (or null)."+LN+
"\t */"+LN) : "") +
"\tpublic static final &{name} toEnum(final Object x) {"+LN+
"\t\tif (x!=null)"+LN+
"\t\t\tfor(&{name} y: values())"+LN+
"\t\t\t\tif (y.itemValue().toString().equals(x.toString())) return y;"+LN+
"\t\treturn null;"+LN+
"\t}"+LN+
"}";
				out.write(modify(template, "&{name}", enumName));
				out.close();
			} catch (Exception ex) {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception exx) {}
				}
				reporter.error(SYS.SYS036, ex); //Program exception &{0}
			}
		}
	}

	/** Convert XML name to Java name.
	 * @param xmlName XML name.
	 * @return Java name created from XML name,
	 */
	public static final String javaName(final String xmlName) {
		return "_".equals(xmlName) ? "$_" // Java 9 not allows indentifiers "_"
			: xmlName.replace(':','$').replace('-','_').replace('.','_');
	}

	@Deprecated
	/** Generate XComponent Java source class from X-definition.
	 * @deprecated switch to generate JAXB annotations  is ignored. Please use
	 * method GenXComponent.genXComponent(...) without "jaxb" parameter.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param jaxb switch to generate JAXB annotations.
	 * @param suppressPrintWarnings suppress print of warnings.
	 * @return ArrayReporter with errors and warnings
	 * @throws IOException if an error occurs.
	 */
	public static ArrayReporter genXComponent(XDPool xdpool,
		String dir,
		String charset,
		boolean genJavadoc,
		boolean jaxb,
		boolean suppressPrintWarnings) throws IOException {
		return genXComponent(xdpool,
			dir, charset, genJavadoc, suppressPrintWarnings);
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @param genJavadoc switch to generate JavaDoc.
	 * @param suppressPrintWarnings suppress print of warnings.
	 * @return ArrayReporter with errors and warnings
	 * @throws IOException if an error occurs.
	 */
	public static ArrayReporter genXComponent(XDPool xdpool,
		String dir,
		String charset,
		boolean genJavadoc,
		boolean suppressPrintWarnings) throws IOException {
		final ArrayReporter reporter = new ArrayReporter();
		File fdir = new File(dir);
		if (fdir.isDirectory()) {
			if (!dir.endsWith("/")) {
				dir += "/";
			}
		} else {
			//Argument &{0} must be a directory
			throw new SRuntimeException(XDEF.XDEF368, dir);
		}
		final Map<String, String> components =
			new LinkedHashMap<String, String>(xdpool.getXComponents());
		for (int runCount = 0; runCount < 2; runCount++) {
			// create HashSet with class names of X.components
			HashSet<String> classNames = new HashSet<String>();
			for (Entry<String, String> e: xdpool.getXComponents().entrySet()) {
				String s = e.getValue();
				int ndx = s.indexOf(" ");
				classNames.add(ndx > 0 ? s.substring(0, ndx): s);
			}
			// create array of all X-components so that first are the items
			// which are extensions of an other X-component and then follows
			// those not extendsd. This ensures that X-components which extends
			// other X-component are compiled first.
			ArrayList<Entry<String, String>> xcarray =
				new ArrayList<Entry<String, String>>();
			for (Entry<String, String> e: xdpool.getXComponents().entrySet()) {
				int ndx;
				String s = e.getValue();
				if ((ndx = s.indexOf(" extends ")) > 0) {
					s = s.substring(ndx + 9);
					ndx = s.indexOf(' ');
					if (ndx > 0) {
						s = s.substring(0, ndx);
					}
					if (classNames.contains(s)) {
						xcarray.add(e);
						continue;
					}
				}
				xcarray.add(0, e);
			}
			 // in first run we generate only component classes
			 // in second run we generate only interfaces
			for (Entry<String, String> e: xcarray) {
				final String model = e.getKey();
				String className = e.getValue();
				String extName = "", interfaceName = "";
				int ndx;
				String packageName = "";
				if ((ndx = className.indexOf(" interface ")) > 0) {
					if (runCount == 0) {
						String s = className.substring(ndx + 11);
						className = className.substring(0, ndx);
						int ndx1 = className.indexOf(" implements ");
						if (ndx1 < 0) {
							className += " implements " + s;
						} else {
							int ndx2 = className.indexOf(' ' + s);
							if (ndx2 < 0) {
								ndx2 = className.indexOf(',' + s);
							}
							if (ndx2 < 0) {
								className += ',' + s;
							}
						}
					} else {
						interfaceName = className.substring(ndx + 11);
						className = "";
						ndx = interfaceName.lastIndexOf('.');
						if (ndx > 0) {
							packageName = interfaceName.substring(0, ndx);
						}
					}
				} else if (className.startsWith("interface ")) {
					if (runCount == 0) {
						continue;
					}
					// never should happen to be here
					interfaceName = className.substring(10);
					className = "";
				} else if (className.startsWith("%ref ")) {
					if (runCount == 0) {
						className = className.substring(5).trim();
						components.put(model, className);
					}
					continue;
				} else if (runCount == 1) {
					continue;
				}
				if ((ndx = className.indexOf(' ')) > 0) {
					extName += className.substring(ndx);
					className = className.substring(0, ndx).trim();
				}
				File fparent;
				if ((ndx = className.lastIndexOf('.')) > 0) {
					packageName = className.substring(0, ndx);
					className = className.substring(ndx + 1);
				}
				if (packageName.length() == 0) {
					fparent = fdir;
				} else {
					fparent = new File(dir, packageName.replace('.', '/'));
					fparent.mkdirs();
				}
				String fName = className.replace('\n', ' ')
					.replace('\r', ' ').replace('\t', ' ');
				if ((ndx = fName.indexOf(' ')) > 0) {
					fName = className.substring(0 , ndx);
				}
				className = className + extName;
				GenXComponent genxc = new GenXComponent(xdpool, reporter);
				String extClass = "";
				if ((ndx = className.indexOf(" extends")) > 0) {
					extClass = " extends " + className.substring(ndx+8).trim();
					className = className.substring(0,ndx).trim();
				} else if ((ndx = className.indexOf(" implements")) > 0) {
					extClass=" implements "+className.substring(ndx+11).trim();
					className = className.substring(0,ndx).trim();
				}
				final String result = genxc.genXComponent(model, //model name
					className, //name of generated class
					extClass, //class extension
					interfaceName, //name of interface
					packageName, //package of generated class
					components, // Map with components
					genJavadoc); //switch to generate JavaDoc
				if (result != null) {
					File f = new File(fparent, fName + ".java");
					FileOutputStream fos = new FileOutputStream(f);
					Writer out = charset == null || charset.length() == 0 ?
						new OutputStreamWriter(fos) :
						new OutputStreamWriter(fos, charset);
					out.append(SUtils.modifyString(result, "\t", "  ")).close();
				}
				if (genxc._interface != null) {
					packageName = "";
					if ((ndx = interfaceName.lastIndexOf('.')) > 0) {
						packageName = interfaceName.substring(0, ndx);
						interfaceName = interfaceName.substring(ndx + 1);
					}
					fparent = new File(dir, packageName.replace('.', '/'));
					fparent.mkdirs();
					File f = new File(fparent, interfaceName + ".java");
					FileOutputStream fos = new FileOutputStream(f);
					Writer out = charset == null || charset.length() == 0 ?
						new OutputStreamWriter(fos) :
						new OutputStreamWriter(fos, charset);
					out.append(SUtils.modifyString(
						genxc._interface.toString(), "\t", "  ")).close();
				}
			}
		}
		genEnumerations(xdpool, fdir, charset, genJavadoc, reporter);
		reporter.checkAndThrowErrors();
		if (!suppressPrintWarnings && reporter.errorWarnings()) {
			reporter.printReports(System.err);
		}
		return reporter;
	}

	/** Generate XComponent Java source class from X-definition.
	 * @param xdpool XDPool object where is the X-definition with model
	 * from which Java source will be generated.
	 * @param dir path to directory where write the source code. The file name
	 * will be constructed from the argument className as "className.java".
	 * @param charset the character set name or null (if null then it is used
	 * the system character set name).
	 * @throws IOException if an error occurs.
	 */
	public static void genXComponent(final XDPool xdpool,
		final String dir,
		final String charset) throws IOException {
		genXComponent(xdpool, dir, charset, false, false);
	}

	/** Call generation of Java source code of XComponents from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li>-i X-definitions list of files, required. Wildcards are
	 * supported, required.</li>
	 * <li>-x Qualified name of class with XDPool which source will
	 *  be generated, optional (if not specified, source is not generated)</li>
	 * <li>-p package name, optional (if not specified no package is used)</li>
	 * <li>-o Output directory where the sources are generated, required</li>
	 * <li>-e Encoding name, optional (default is the Java system encoding)</li>
	 * <li>-d Generate JavaDoc, optional (default is not generate JavaDoc)</li>
	 * <li>-j Generate JAXB annotations. Optional, default is not generate.</li>
	 * <li>-h Help message, optional</li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"GenXComponent - generate XComponent Java source code from X-definition.\n"+
"Parameters:"+LN+
" -i X-definitions list of files, required. Wildcards may be used. Required.\n"+
" -o Output directory where XComponents are generated, required\n"+
" -p Output directory where the compiled XDPool will be stored,\n"+
"    optional (if not specified the XDPool object is not stored)"+LN+
" -e Encoding name, optional (default is the Java system encoding)\n"+
" -d Generate JavaDoc, optional (default is not generate JavaDoc)\n"+
" -h Help message, optional";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Missing parameters\n" + info);
		}
		if (args.length < 3) {
			if (args.length == 1 &&
				("-h".equals(args[0]) || "-?".equals(args[0]))) {
				System.out.println(info);
				return;
			} else {
				throw new RuntimeException("Incorrect parameters\b" + info);
			}
		}
		ArrayList<String> sources = new ArrayList<String>();
		File xcDir = null; // base directory where XComponents will be generated
		FileOutputStream xpFile = null; // the file where save compiled XDPool
		String encoding = null;
		boolean javadoc = false;
		int i = 0;
		while (i < args.length) {
			String arg = args[i];
			if (arg == null || arg.length() == 0
				|| arg.charAt(0) != '-' || arg.length() != 2) {
				throw new RuntimeException(
					"Incorrect parameter " + (i+1) + ": " + arg + '\n' + info);
			}
			switch (arg.charAt(1)) {
				case 'd': // Generate JavaDoc
					if (javadoc) {
						throw new RuntimeException(
							"Redefinition of key \"-d\"\n" + info);
					}
					javadoc = true;
					continue;
				case 'e': // Encoding
					if (encoding != null) {
						throw new RuntimeException(
							"Redefinition of key \"-e\".\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						encoding = arg;
						i++;
						continue;
					} else {
						throw new RuntimeException(
							"Parameter '-e' is not encoding name.\n" + info);
					}
				case 'i': // X-definitions list of files
					while (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						sources.add(arg);
					}
					continue;
				case 'j': // JAXB annotations
					System.err.println("Warning JAXB annotations swith"
						+ " is ignored in this version!");
					continue;
				case 'h': // help
					System.out.println(info);
					i++;
					continue;
				case 'o': // Output directory
					if (xcDir != null) {
						throw new RuntimeException(
							"Redefinition of key \"-o\"\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						try {
							xcDir = new File(arg);
							if (xcDir.exists() && xcDir.isDirectory()) {
								i++;
								continue;
							}
						} catch (Exception ex) {}
						throw new RuntimeException(
							"Parameter '-o' is not output directory.\n" + info);
					} else {
						throw new RuntimeException(
							"Parameter '-o' is not output directory.\n" + info);
					}
				case 'p': // where to write the XDPool
					if (xpFile != null) {
						throw new RuntimeException(
							"Redefinition of key \"-p\"\n" + info);
					}
					if (++i < args.length && (arg = args[i]) != null &&
						!arg.startsWith("-")) {
						File f = new File(arg);
						if (f.exists() && f.isDirectory()) {
							throw new RuntimeException(
								"The key \"-p\" must be file\n" + info);
						}
						try {
							i++;
							xpFile = new FileOutputStream(f);
							continue;
						} catch (Exception ex) {
							throw new RuntimeException(
								"Can't write to the file from tne key \"-p\"\n"
									+ info, ex);
						}
					} else {
						throw new RuntimeException(
							"Parameter '-p' is not file\n" + info);
					}
				default:
					throw new RuntimeException("Incorrect parameter \""
						+arg+"\" on position " + (i+1)+".\n" + info);
			}
		}
		if (sources.isEmpty()) {
			throw new RuntimeException(
				"No XDPool source is specified.\n" + info);
		}
		if (xcDir == null) {
			throw new RuntimeException(
				"No output directory specified.\n" + info);
		}
		try {
			Object[] xdefs = new String[sources.size()];
			sources.toArray(xdefs);
			XDPool x = XDFactory.compileXD(null, xdefs);
			genXComponent(x, xcDir.getCanonicalPath(), encoding,javadoc, false);
			if (xpFile != null) {
				ObjectOutputStream oos = new ObjectOutputStream(xpFile);
				oos.writeObject(x);
				oos.close();
			}
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}