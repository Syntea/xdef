package org.xdef.component;

import java.util.Set;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.XConstants;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XPool;
import org.xdef.impl.code.CodeTable;
import org.xdef.json.JsonToXml;
import org.xdef.json.JsonUtil;
import org.xdef.model.XMData;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;

/** Generation of Java source code methods for JSON getters/setters.
 * @author Vaclav Trojan
 */
class XCGeneratorJSON extends XCGeneratorBase1 {

	XCGeneratorJSON(final XDPool xp,
		final ArrayReporter reporter,
		final boolean genJavadoc) {
		super(xp, reporter, genJavadoc);
	}

	/** Generate direct getters/seters for value of the text child node.
	 * @param xel model of element (has only a text child and no attributes).
	 * @param name name of variable.
	 * @param iType name of item variable type.
	 * @param isRoot true if it is a root element of X-component.
	 * @param setters StringBuilder where to generate getters.
	 * @param getters StringBuilder where to generate setters.
	 * @param sbi StringBuilder where to generate interfaces (may be null).
	 */
	final void genDirectSetterAndGetter(final XElement xel,
		final String name,
		final String iType,
		final boolean isRoot,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi) {
		XMData xdata = (XMData) xel.getChildNodeModels()[0];
		String typeName = getJavaObjectTypeName(xdata);
		String descr = "text node from element";
		int max = xel.maxOccurs();
		genDirectGetter(xel, typeName, name, isRoot, max, descr, getters, sbi);
		genDirectSetter(typeName, name, iType, isRoot, max, descr, setters,sbi);
	}

	/** Generate java code of getter method for child element classes.
	 * @param typeName name of class representing the child element.
	 * @param name name of variable.
	 * @param isRoot true if it is a root element of X-component.
	 * @param max maximal number of items.
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 * @return generated code.
	 */
	private void genDirectGetter(XNode xn,
		final String typeName,
		final String name,
		final boolean isRoot,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		final int ndx = typeName.lastIndexOf('.');
		if (ndx == 0) { // never should happen
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
"\tpublic &{typ} listOf$&{name}();"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ));
			} else {
				sbi.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}();"+LN,
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
"\tpublic java.util.Date dateOf$&{name}();"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf$&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf$&{name}();"+LN,
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
"\tpublic &{typ} get$&{name}() {"+LN+
"\t\t&{typ} y ="+LN+
"\t\t\tnew &{typ1}();"+LN+
"\t\tfor (&{name} z : listOf&{name}()) {"+LN+
"\t\t\ty.add(z.get$value());"+LN+
"\t\t}"+LN+
"\t\treturn y;"+LN+
"\t}"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{name}", name,
				"&{typ}", typ,
				"&{typ1}", typ.replace("List<", "ArrayList<")));
		} else {
			String x = isRoot ? "" : "_&{name}==null?null:_&{name}.";
			sb.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}() {return "+ x +"get$value();}"+LN,
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
"\tpublic java.util.Date dateOf$&{name}(){"+
"return "+x+"dateOf$value();}"+LN+
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf$&{name}(){"+
"return "+x+"timestampOf$value();}"+LN+
(_genJavadoc ? "\t/** Get  &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf$&{name}(){"+
"return "+x+"calendarOf$value();}"+LN,
					"&{xmlName}", xmlName,
					"&{d}" , d,
					"&{name}", name));
			}
		}
	}

	/** Generate java code of setter method for child element classes.
	 * @param typeName name typ (class etc).
	 * @param name name of variable.
	 * @param iType name of item variable type.
	 * @param isRoot true if it is a root element of X-component.
	 * @param max maximal number of items.
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 */
	private void genDirectSetter(final String typeName,
		final String name,
		final String iType,
		final boolean isRoot,
		final int max,
		final String descr,
		final StringBuilder sb,
		final StringBuilder sbi) {
		String x;
		String d = descr;
		if (max > 1) {
			d += 's';
			x = "if(x!=null)_&{name}.add(x);";
		} else {
			x = (isRoot ? "" : "if(_&{name}==null)_&{name}=new "
				+ iType + "();_&{name}.") + "set$value(x);";
		}
		if (sbi != null) {
			sb.append("\t@Override").append(LN);
			if (max > 1) {
				String template =
(_genJavadoc ? ("\t/** Set value of list of values to \"&{xmlName}\"."+LN+
"\t * @param x list to be set."+LN+
"\t */"+LN) : "")+
"\tpublic void set$&{name}(&{typ} x);"+LN;
				sbi.append(modify(template,
					"&{name}", name,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", "java.util.List<" + typeName + '>'));
			} else {
				final String template =
(_genJavadoc ? ("\t/** Set value of &{d} \"&{xmlName}\"."+LN+
"\t * @param x value to be set."+LN+
"\t */"+LN) : "")+
"\tpublic void set$&{name}(&{typ} x);"+LN;
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
(_genJavadoc ? ("\t/** Set values from list to \"&{xmlName}\"."+LN+
"\t * @param x value to be added."+LN+
"\t */"+LN) : "")+
"\tpublic void set$&{name}(java.util.List<&{typ}> x) {"+LN+
"\t\tjava.util.List<&{name}> y = listOf&{name}();"+LN+
"\t\ty.clear();"+LN+
"\t\tif (x==null)return;"+LN+
"\t\tfor (&{typ} w : x) {"+LN+
"\t\t\tif (w != null) {"+LN+
"\t\t\t\t&{name} z = new &{name}();"+LN+
"\t\t\t\tz.set$value(w);"+LN+
"\t\t\t\ty.add(z);"+LN+
"\t\t\t}\n" +
"\t\t}\n" +
"\t}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", typeName));
		} else {
			final String template =
(_genJavadoc ? ("\t/** Set value of &{d} \"&{xmlName}\"."+LN+
"\t * @param x value to be set."+LN+
"\t */"+LN) : "")+
"\tpublic void set$&{name}(&{typ} x){&{x}}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", typeName));
			if ("org.xdef.sys.SDatetime".equals(typeName)) {
				String typeName1 = "java.util.Date";
				sb.append(modify(template,
					"&{x}", x,
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.sql.Timestamp";
				sb.append(modify(template,
					"&{x}", x,
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
				typeName1 = "java.util.Calendar";
				sb.append(modify(template,
					"&{x}", x,
					"&{name}", name,
					"&{d}" , d,
					"&{xmlName}", name.replace('$', ':'),
					"&{typ}", typeName1));
			}
		}
	}

	/** Create getter and setter of the item model of js:item.
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
	final void genJsonItemGetterAndSetter(final XElement xe,
		final String typeName,
		final String iname,
		final int max,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi,
		final Set<String> classNames,
		final Set<String> varNames) {
		String name = null;
		XData keyAttr = (XData) xe.getAttr(JsonToXml.J_KEYATTR);
		if (xe._json==XConstants.JSON_MODE && xe._match>=0 && keyAttr!=null
			&& keyAttr._check >= 0) {
			XDValue[] code = ((XPool)xe.getXDPool()).getCode();
			for (int i = keyAttr._check; i < code.length; i++) {
				XDValue item = code[i];
				if (item.getCode() == CodeTable.CALL_OP) {
					i = item.getParam();
					continue;
				}
				if (item.getCode() == CodeTable.LD_CONST) {
					name = javaName(
						"get$" + JsonUtil.toXmlName(code[i].stringValue()));
					name = getUniqueName(getUniqueName(getUniqueName(name,
						RESERVED_NAMES), classNames), varNames);
					varNames.add(name);
					name = name.substring(4);
					break;
				}
			}
		}
		if (name == null) {
			name = getUniqueName(getUniqueName("get$item",classNames),varNames);
			varNames.add(name);
			name = name.substring(4);
		}
		String typ =
			getJavaObjectTypeName((XData) xe.getAttr(JsonToXml.J_VALUEATTR));
		boolean isNull = false;
		String template;
		// has only a text child
		String jGet, jSet;
		String s;
		if (max > 1) { // list of values
			String typ1 = "java.util.List<" + typ + ">";
			jGet = "String".equals(typ) && xe.getJsonMode() != 0 ?
				"org.xdef.json.JsonUtil.jstringFromSource(y.getvalue())"
				: "y.getvalue()";
			// getter
			template =
(_genJavadoc ? "\t/** Get values of text nodes of &{d}."+LN+
"\t * @return value of text nodes of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ1} listOf$&{name}()";
			s = isNull ? typ + ".JNULL" : jGet;
			getters.append(modify(template +
"{"+LN+
"\t\t&{typ1} x=new java.util.ArrayList<&{typ}>();"+LN+
"\t\tfor(&{typeName} y: _&{iname}) x.add(" + s + ");"+LN+
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
			jSet = "String".equals(typ) && xe.getJsonMode() != 0 ?
				"org.xdef.json.JsonUtil.jstringToXML(x,false)" : "x";
			template =
(_genJavadoc ? "\t/** Add values of textnodes of &{d}. */"+LN : "")+
"\tpublic void add&{name}(&{typ} x)";
			setters.append(modify(template +
"{"+LN+
"\t\tif (x!=null) {"+LN+
(isNull ? "\t\t\tadd&{iname}(new &{typeName}());"+LN
:("\t\t\t&{typeName} y=new &{typeName}();"+LN+
"\t\t\ty.setvalue(" + jSet + "); add&{iname}(y);"+LN))+
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
(isNull ? "\t\t\tadd&{iname}(new &{typeName}());"+LN
:("\t\t\t&{typeName} z=new &{typeName}();"+LN+
"\t\t\tadd&{iname}(z);"+LN)) +
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
			// getter
			template =
(_genJavadoc ? "\t/** Get JSON value of textnode of &{d}."+LN+
"\t * @return value of text of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}(){"+LN+
"\t\treturn _&{iname}==null?null:" +
	("String".equals(typ) && xe.getJsonMode() != 0 ?
	"org.xdef.json.JsonUtil.jstringFromSource(_&{iname}.getvalue())"
	: isNull ? typ + ".JNULL" : "_&{iname}.getvalue()") + ";" + LN
+"\t}"+LN;
			getters.append(modify(template,
				"&{name}", name,
				"&{iname}", iname,
				"&{d}", xe.getName(),
				"&{typ}", typ));
			if ("org.xdef.sys.SDatetime".equals(typ)) {
				getters.append(modify(
(_genJavadoc ? "\t/** Get value of &{d} as java.util.Date."+LN+
"\t * @return value of &{d} as java.util.Date or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Date dateOf$&{name}(){"+
"return org.xdef.sys.SDatetime.getDate(get$&{name}());}"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf$&{name}(){"+
"return org.xdef.sys.SDatetime.getTimestamp(get$&{name}());}"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf$&{name}(){"+
"return org.xdef.sys.SDatetime.getCalendar(get$&{name}());}"+LN,
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
"\tpublic java.util.Date dateOf$&{name};"+LN+
(_genJavadoc ? "\t/** Get &{d} as java.sql.Timestamp."+LN+
"\t * @return value of &{d} as java.sql.Timestamp or null."+LN+
"\t */"+LN : "")+
"\tpublic java.sql.Timestamp timestampOf$&{name}();"+LN+
(_genJavadoc ? "\t/** Get  &{d}  as java.util.Calendar."+LN+
"\t * @return value of &{d} as java.util.Calendar or null."+LN+
"\t */"+LN : "")+
"\tpublic java.util.Calendar calendarOf$&{name}();"+LN,
						"&{d}" , xe.getName(),
						"&{name}", name));
				}
			}
			jSet = "String".equals(typ) && xe.getJsonMode() != 0
				? "org.xdef.json.JsonUtil.jstringToXML(x,false)":"x";
			// setter
			template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void set$&{name}(&{typ} x)";
			s = isNull
? "\t\tif(_&{iname}==null)set&{iname}(x==null?null:new &{typeName}());"+LN
: ("\t\tif(x==null) _&{iname}=null; else {"+LN+
"\t\t\tif(_&{iname}==null) set&{iname}(new &{typeName}());"+LN+
"\t\t\t_&{iname}.setvalue(x);"+LN+
"\t\t}"+LN);
			setters.append(modify(template+"{"+LN+ s + "\t}"+LN,
				"&{name}", name,
				"&{iname}", iname,
				"&{d}", xe.getName(),
				"&{typ}", typ,
				"&{typeName}", typeName));
			if ("org.xdef.sys.SDatetime".equals(typ)) {
				template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void set$&{name}(&{typ} x)"+
"{set$&{name}(x==null?null:new org.xdef.sys.SDatetime(x));}"+LN;
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
"\tpublic void set$&{name}(&{typ} x);"+LN;
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
}