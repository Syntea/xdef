package org.xdef.component;

import java.util.List;
import java.util.Set;
import org.xdef.XDConstants;
import org.xdef.XDPool;
import org.xdef.XDValue;
import static org.xdef.component.XCGeneratorBase.LN;
import static org.xdef.component.XCGeneratorBase.RESERVED_NAMES;
import static org.xdef.component.XCGeneratorBase.getUniqueName;
import static org.xdef.component.XCGeneratorBase.modify;
import static org.xdef.component.XComponentUtil.xmlToJavaName;
import org.xdef.impl.XConstants;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.impl.XPool;
import static org.xdef.impl.code.CodeTable.CALL_OP;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import org.xdef.xon.XonTools;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import org.xdef.msg.SYS;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SRuntimeException;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;

/** Generation of Java source code methods for XON/JSON getters/setters.
 * @author Vaclav Trojan
 */
class XCGeneratorXON extends XCGeneratorBase1 {

	/** Create instance of the class XCGeneratorXON.
	 * @param xp XDPool from which to generate X-components.
	 * @param reporter Reporter where to write error and warning messages.
	 * @param genJavadoc if true generate Javadoc to X-definition source.
	 */
	XCGeneratorXON(final XDPool xp,
		final ArrayReporter reporter,
		final boolean genJavadoc) {
		super(xp, reporter, genJavadoc);
	}

	/** Generate direct getters/seters for value of the text child node.
	 * @param xel model of element (has only a text child and no attributes).
	 * @param name name of variable.
	 * @param iName name of item variable type.
	 * @param isRoot true if it is a root element of X-component.
	 * @param setters StringBuilder where to generate getters.
	 * @param getters StringBuilder where to generate setters.
	 * @param sbi StringBuilder where to generate interfaces (may be null).
	 */
	final void genDirectSetterAndGetter(final XElement xel,
		final String name,
		final String iName,
		final boolean isRoot,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi) {
		XMData xdata = (XMData) xel.getChildNodeModels()[0];
		String typeName = getJavaObjectTypeName(xdata);
		String descr = "text node from element";
		int max = isRoot ? 1 : xel.maxOccurs(); // root is always 1
		genDirectGetter(xel,
			typeName, name, iName, isRoot, max, descr, getters, sbi);
		genDirectSetter(typeName, name, iName, isRoot, max, descr, setters,sbi);
	}

	/** Generate java code of getter method for child element classes.
	 * @param typeName name of class representing the child element.
	 * @param name name of variable.
	 * @param iType name of item variable type.
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
		final String iType,
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
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
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
		if (typ.startsWith("java.util.List<") && max > 1) {
			sb.append(modify(
(_genJavadoc ? "\t/** Get list of &{d} \"&{xmlName}\"."+LN+
"\t * @return value of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}() {"+LN+
"\t\t&{typ} y=new &{typ1}();"+LN+
"\t\tfor (&{iname} z : listOf&{name}()) {"+LN+
"\t\t\ty.add(z.get$value());"+LN+
"\t\t}"+LN+
"\t\treturn y;"+LN+
"\t}"+LN,
				"&{xmlName}", xmlName,
				"&{d}" , d,
				"&{iname}", iType,
				"&{name}", name,
				"&{typ}", typ,
				"&{typ1}", "java.util.ArrayList<>"));
		} else {
			String x = isRoot ? "" : "_&{name}==null? null: _&{name}.";
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
(_genJavadoc ? "\t/** Get &{d} \"&{xmlName}\" as java.util.Calendar."+LN+
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
	 * @param iName name of item variable type.
	 * @param isRoot true if it is a root element of X-component.
	 * @param max maximal number of items.
	 * @param descr Description text.
	 * @param sb String builder where the code is generated.
	 * @param sbi String builder where the code is generated for interface.
	 */
	private void genDirectSetter(final String typeName,
		final String name,
		final String iName,
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
				+ iName + "();_&{name}.") + "set$value(x);";
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
"\t\tjava.util.List<&{iname}> y = listOf&{name}();"+LN+
"\t\ty.clear();"+LN+
"\t\tif (x==null)return;"+LN+
"\t\tfor (&{typ} w : x) {"+LN+
"\t\t\tif (w != null) {"+LN+
"\t\t\t\t&{iname} z = new &{iname}();"+LN+
"\t\t\t\tz.set$value(w);"+LN+
"\t\t\t\ty.add(z);"+LN+
"\t\t\t}"+LN+
"\t\t}"+LN+
"\t}"+LN;
			sb.append(modify(template,
				"&{x}", x,
				"&{iname}", iName,
				"&{name}", name,
				"&{d}" , d,
				"&{xmlName}", name.replace('$', ':'),
				"&{typ}", typeName));
		} else {
			final String template =
(_genJavadoc ? ("\t/** Set value of &{d} \"&{xmlName}\"."+LN+
"\t * @param x value to be set."+LN+
"\t */"+LN) : "")+
"\tpublic void set$&{name}(&{typ} x) {&{x}}"+LN;
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

	/** Create named values getters.
	 * @param keys array with tripples: key, getter value, result type value.
	 * @param varNames set with variable names.
	 * @param getters where to generate getters.
	 */
	final void genNamedValueGetters(final List<String> keys,
		final Set<String> varNames,
		final StringBuilder getters) {
		String key = keys.get(0);
		String name = xmlToJavaName("get$" + XonTools.toXmlName(key));
		name = getUniqueName(getUniqueName(name, RESERVED_NAMES), varNames);
		varNames.add(name);
		String s =
(_genJavadoc ? "\t/** Getter of named value "+key+".*/"+LN : "")+
"\tpublic Object "+name+ "() {"+LN;
		for (int k = 0; k < keys.size(); k += 3) {
			if (!key.equals(keys.get(k))) {
				s += "\t\treturn null;"+LN + "\t}"+LN;
				key = keys.get(k);
				name = XonTools.toXmlName(key);
				s +=
(_genJavadoc ? "\t/** Getter of named value '"+key+"'.*/"+LN : "")+
"\tpublic Object get$"+name+ "() {"+LN;
			}
			s +=
"\t\tif(get" + keys.get(k+2) + "()!= null) {"+LN+
"\t\t\treturn get" + keys.get(k+2) + "().toXon();"+LN+
"\t\t}"+LN;
		}
		s += "\t\treturn null;"+LN + "\t}"+LN;
		getters.append(s);
	}

	/** Create unique model name.
	 * @param xe Element model from which setter/getter is generated.
	 * @param namePrefix prefix of the name (eg. "get$")
	 * @param varNames set with variable names.
	 * @return unique model name.
	 */
	private static String getXonItemName(final XElement xe,
		final String namePrefix,
		final Set<String> varNames) {
		XData keyAttr = (XData) xe.getAttr(X_KEYATTR);
		String name = null;
		if (xe._xon==XConstants.XON_MODE_W && xe._match>=0 && keyAttr!=null
			&& keyAttr._check >= 0) {
			XDValue[] code = ((XPool)xe.getXDPool()).getCode();
			for (int i = keyAttr._check; i < code.length; i++) {
				XDValue item = code[i];
				if (item.getCode() == CALL_OP) {
					i = item.getParam();
					continue;
				}
				if (item.getCode() == LD_CONST) {
					name = namePrefix + code[i].stringValue();
					break;
				}
			}
		}
		if (name == null) {
			name = namePrefix + xe.getLocalName();
		}
		name = getUniqueName(
			getUniqueName(xmlToJavaName(name), RESERVED_NAMES), varNames);
		varNames.add(name);
		name = name.substring(4);
		return name;
	}

	/** Create getter and setter of the item model of js:item.
	 * @param xe Element model from which setter/getter is generated.
	 * @param typeName the class name of this element X-component.
	 * @param iname name of getter/setter of this model.
	 * @param max maximal occurrence.
	 * @param setters where to generate setter.
	 * @param getters where to generate getter.
	 * @param sbi where to generate interface.
	 * @param varNames set with variable names.
	 */
	final void genXonItemGetterAndSetter(final XElement xe,
		final String typeName,
		final String iname,
		final int max,
		final StringBuilder setters,
		final StringBuilder getters,
		final StringBuilder sbi,
		final Set<String> varNames) {
		String name = getXonItemName(xe, "get$", varNames);
		String typ =
			getJavaObjectTypeName((XData) xe.getAttr(X_VALATTR));
		String template;
		// has only a text child
		String jGet, jSet;
		String s;
		XMData keyAttr = xe.getAttr(X_KEYATTR);
		if (max > 1) { // list of values
			String typ1 = "java.util.List<" + typ + ">";
			jGet = xe.getXonMode() != 0 && "String".equals(typ)
				?"org.xdef.xon.XonTools.jstringFromSource(y.get"+X_VALATTR+"())"
				: "y.get"+X_VALATTR+"()";
			if (keyAttr != null && keyAttr.getFixedValue() == null) {//%anyName
				template =
(_genJavadoc ? "\t/** Get map with %anyName entries of the map &{d}."+LN+
"\t * @return map with entries to be set to map &{d}"+LN+
"\t */"+LN : "")+
"\tpublic java.util.Map<String, &{typ}> anyItem$() ";
				getters.append(modify(template +
"{"+LN+
"\t\tjava.util.Map<String, &{typ}> x="+LN+
"\t\t\tnew java.util.LinkedHashMap<>();"+LN+
"\t\tfor(&{typeName} y: _&{iname}) {"+LN+
"\t\t\tx.put(org.xdef.xon.XonTools.xmlToJName(y.get"+X_KEYATTR+"()),"
	+ " y.get"+X_VALATTR+"());"+LN+
"\t\t}"+LN+
"\t\treturn x;"+LN+
"\t}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ,
					"&{typeName}", typeName));
				if (sbi != null) { // generate interface
					sbi.append(modify(template +";"+LN,
						"&{name}", name,
						"&{d}", xe.getName()));
				}
			} else {
				if (keyAttr!=null && keyAttr.getFixedValue()==null) {//%anyName
					template =
(_genJavadoc ? "\t/** Get map with %anyName entries of the map &{d}."+LN+
"\t * @return map with entries to be set to map &{d}"+LN+
"\t */"+LN : "")+
"\tpublic java.util.Map<String, &{typ}> anyItem$() ";
					getters.append(modify(template +
"{"+LN+
"\t\tjava.util.Map<String, &{typ}> x="+LN+
"\t\t\tnew java.util.LinkedHashMap<>();"+LN+
"\t\t\tx.put(y.get"+X_KEYATTR+"(), _&{iname}.get"+X_VALATTR+"());"+LN+
"\t\treturn x;"+LN+
"\t}"+LN,
						"&{name}", name,
						"&{iname}", iname,
						"&{d}", xe.getName(),
						"&{typ}", typ,
						"&{typeName}", typeName));
					if (sbi != null) { // generate interface
						sbi.append(modify(template +";"+LN,
							"&{name}", name,
							"&{d}", xe.getName()));
					}
				}
				// getter
				template =
(_genJavadoc ? "\t/** Get values of text nodes of &{d}."+LN+
"\t * @return value of text nodes of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ1} listOf$&{name}()";
				s = jGet;
				getters.append(modify(template +
"{"+LN+
"\t\t&{typ1} x=new java.util.ArrayList<>();"+LN+
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
			}
			// setter
			if (xe.getXonMode() != 0) {
				if ("String".equals(typ)) {
					jSet = "org.xdef.xon.XonUtils.toJsonString(x,false)";
				} else {
					jSet = "x";
				}
			} else {
				jSet = "x";
			}
			template =
(_genJavadoc ? "\t/** Add values of textnodes of &{d}. */"+LN : "")+
"\tpublic void add&{name}(&{typ} x)";
			setters.append(modify(template +
"{"+LN+
"\t\tif (x!=null) {"+LN+
"\t\t\t&{typeName} y=new &{typeName}();"+LN+
"\t\t\ty.set"+X_VALATTR+"(" + jSet + "); add&{iname}(y);"+LN+
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
"\t\tfor ("+
("&{typ} y:x) {"+LN+
"\t\t\t&{typeName} z=new &{typeName}();"+LN+
"\t\t\tz.set"+X_VALATTR+"(y); add&{iname}(z);")+LN+
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
			if (keyAttr != null && keyAttr.getFixedValue() == null) {//%anyName
				template =
(_genJavadoc ? "\t/** Get map with %anyName entries of the map &{d}."+LN+
"\t * @return map with entries to be set to map &{d}"+LN+
"\t */"+LN : "")+
"\tpublic java.util.Map<String, &{typ}> anyItem$() ";
				getters.append(modify(template +
"{"+LN+
"\t\tjava.util.Map<String,&{typ}>x=new java.util.LinkedHashMap<>();"+LN+
"\t\tif (_&{iname} != null) {"+LN+
"\t\t\tx.put(org.xdef.xon.XonTools.xmlToJName(_&{iname}.getkey()), _&{iname}.getval());\n" +
"\t\t}\n" +
"\t\treturn x;"+LN+
"\t}"+LN,
					"&{name}", name,
					"&{iname}", iname,
					"&{d}", xe.getName(),
					"&{typ}", typ,
					"&{typeName}", typeName));
				if (sbi != null) { // generate interface
					sbi.append(modify(template +";"+LN,
						"&{name}", name,
						"&{d}", xe.getName()));
				}
			}
			// getter
			template =
(_genJavadoc ? "\t/** Get XON value of textnode of &{d}."+LN+
"\t * @return value of text of &{d}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}(){"+LN+
"\t\treturn _&{iname}==null? null: " +
	("String".equals(typ) && xe.getXonMode() != 0 ?
	"org.xdef.xon.XonTools.jstringFromSource(_&{iname}.get"+X_VALATTR+"())"
	: "_&{iname}.get"+X_VALATTR+"()") + ";" + LN
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
			// setter
			template =
(_genJavadoc ? "\t/** Set value of textnode of &{d}.*/"+LN : "")+
"\tpublic void set$&{name}(&{typ} x)";
			s =
"\t\tif(x==null) _&{iname}=null; else {"+LN+
"\t\t\tif(_&{iname}==null) set&{iname}(new &{typeName}());"+LN+
"\t\t\t_&{iname}.set"+X_VALATTR+"(x);"+LN+
"\t\t}"+LN;
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
"{set$&{name}(org.xdef.sys.SDatetime.createFrom(x));}"+LN;
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

	/** Create method entriesOf fro array or map.
	 * @param xe Element model from which method entriesOf is generated.
	 * @param typeName the class name of this element X-component.
	 * @param iname name of getter/setter of this model.
	 * @param max maximal occurrence.
	 * @param getters where to generate getter.
	 * @param sbi where to generate interface.
	 * @param varNames set with variable names.
	 */
	final void genXonEntryMethod(final XElement xe,
		final String typeName,
		final String iname,
		final int max,
		final StringBuilder getters,
		final StringBuilder sbi,
		final Set<String> varNames) {
		if (xe._xon == 0) {
			return;
		}
		String template;
		String typ;
		XData keyAttr = (XData) xe.getAttr(X_KEYATTR);
		String name;
		if (keyAttr != null && keyAttr.getFixedValue() == null) { // %anyName
			String prefix = "anyItem$";
			name = getUniqueName(prefix, RESERVED_NAMES);
			name = getUniqueName(name, varNames);
			varNames.add(name);
			name = name.substring(prefix.length());
			template =
(_genJavadoc ? "\t/** Get map with %anyName entries from map &{d}."+LN+
"\t * @return map with entries to be set to map &{d}"+LN+
"\t */"+LN : "");
			switch (xe.getLocalName()) {
				case X_ARRAY:
					typ = "java.util.List<?>";
					template +=
"\tpublic java.util.Map<String, &{typ}> anyItem$&{name}() {"+LN+
"\t\tjava.util.Map<String, &{typ}> x="+LN+
"\t\t\tnew java.util.LinkedHashMap<>();"+LN;
					if (max == 1) {
						template +=
"\t\tif (_&{iname} != null) {"+LN+
"\t\t\tx.put(_&{iname}.get"+X_KEYATTR+"(), _&{iname}.toXon());"+LN+
"\t\t}"+LN;
					} else { // max > 1
						template +=
"\t\tfor(&{typeName} y: _&{iname}) {"+LN+
"\t\t\tx.put(y.get"+X_KEYATTR+"(), y.toXon());"+LN+
"\t\t}"+LN;
					}
					break;
				case X_MAP:
					//X_MAP
					typ = "java.util.Map<String, Object>";
					template +=
"\tpublic java.util.Map<String, &{typ}> get$&{name}() {"+LN+
"\t\tjava.util.Map<String, &{typ}> x="+LN+
"\t\t\tnew java.util.LinkedHashMap<>();"+LN;
					if (max == 1) {
						template +=
"\t\tif (_&{iname} != null) {"+LN+
"\t\t\tx.put(_&{iname}.get"+X_KEYATTR+"(), _&{iname}.toXon());"+LN+
"\t\t}"+LN;
					} else { // max > 1
						template +=
"\t\tfor(&{typeName} y: _&{iname}) {"+LN+
"\t\t\tx.put(y.get"+X_KEYATTR+"(), y.toXon());"+LN+
"\t\t}"+LN;
					}
					break;
				default:
					return;
			}
			template +=
"\t\treturn x;"+LN+
"\t}"+LN;
			getters.append(modify(template,
				"&{name}", name,
				"&{iname}", iname,
				"&{d}", xe.getName(),
				"&{typ}", typ,
				"&{typeName}", typeName));
			if (sbi != null) { // generate interface
				sbi.append(modify(template +";"+LN,
					"&{name}", name,
					"&{d}", xe.getName()));
			}
		} else {
			name = getXonItemName(xe, "get$", varNames);
			switch (xe.getLocalName()) {
				case X_ARRAY:
					typ = "java.util.List<Object>";
					template =
(_genJavadoc ? "\t/** Get array from map entry &{name}."+LN+
"\t * @return array from map entry &{name}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}() {"+LN+
"\t\treturn _&{iname} == null? null: _&{iname}.toXon();"+LN+
"\t}"+LN;
					break;
				case X_MAP:
					//X_MAP
					typ = "java.util.Map<String, Object>";
					template =
(_genJavadoc ? "\t/** Get array from map entry &{name}."+LN+
"\t * @return array from map entry &{name}"+LN+
"\t */"+LN : "")+
"\tpublic &{typ} get$&{name}() {"+LN+
"\t\treturn _&{iname} == null? null: _&{iname}.toXon();"+LN+
"\t}"+LN;
					break;
				default:
					return;
			}
			getters.append(modify(template,
				"&{name}", name,
				"&{iname}", iname,
				"&{typ}", typ,
				"&{typeName}", typeName));
			if (sbi != null) { // generate interface
				sbi.append(modify(template +";"+LN,
					"&{name}", name,
					"&{d}", xe.getName()));
			}
		}
////////////////////////////////////////////////////////////////////////////////
	}

	/** Generate toXOn() method.
	 * @param xe Element model from which setter/getter is generated.
	 * @param getters where to generate getter methods.
	 * @param vars where to generate toXOn() method.
	 */
	final void genToXonMethod(final XElement xe,
		final StringBuilder getters,
		final StringBuilder vars) {
		boolean any = false;
		XMNode[] nodes = xe.getChildNodeModels();
		String s;
		if (xe._xon != 0 && nodes.length == 5 //anyObj?
			&& nodes[0].getKind() == XMCHOICE && nodes[1].getKind() == XMELEMENT
			&& X_VALUE.equals(nodes[1].getLocalName())
			&& nodes[2].getKind() == XMELEMENT
			&& X_ARRAY.equals(nodes[2].getLocalName())
			&& ((XElement) nodes[2]).getChildNodeModels().length == 5
			&& nodes[3].getKind() == XMELEMENT
			&& X_MAP.equals(nodes[3].getLocalName())
			&& ((XElement) nodes[3]).getChildNodeModels().length == 5) {
			s =
(_genJavadoc ? "\t/** Get XON value of this %anyObj item."+LN+
"\t * @return value of this %anyObj item."+LN+
"\t */"+LN : "")+
"\tpublic Object getAnyObj$(){return toXon();}"+LN;
			getters.append(s); //%anyObj getter
			any = true;
		}
		s = // toXon() method
(_genJavadoc ? "\t/** Get XON value of this item."+LN+
"\t * @return value of this item."+LN+
"\t */"+LN : "")+
"\t@Override"+LN;
		if (xe.getXonMode()>0&&XDConstants.XON_NS_URI_W.equals(xe.getNSUri())) {
			String x;
			String typ;
			if (X_VALUE.equals(xe.getLocalName())) {
				typ = getJavaObjectTypeName(xe.getAttr(X_VALATTR));
				s =
(_genJavadoc ? "\t/** Get XON value of this item."+LN+
"\t * @return value of this item."+LN+
"\t */"+LN : "")+
"\t@Override"+LN+
"\tpublic " + typ + " toXon() {" +LN+
"\t\tObject o = get"+X_VALATTR+"();"+LN+
"\t\treturn (o instanceof org.xdef.xon.XonTools.JNull)? null"+LN+
"\t\t\t: ";
				switch (typ) {
					case "String":
						s +=
"(String) org.xdef.xon.XonTools.xmlToJValue((String)o);";
						break;
					case "Object":
						s +=
"o instanceof String? org.xdef.xon.XonTools.xmlToJValue((String) o): o;";
						break;
					default:
						s += "("+typ+")o;";
				}
				s += LN+"\t}"+LN;
			} else {
				if (X_ARRAY.equals(xe.getLocalName())) {
					typ = "java.util.List<Object>";
					s += "\tpublic " + typ + " toXon(){"+LN+
"\t\treturn org.xdef.component.XComponentUtil.toXonArray(this);"+LN+"\t}"+LN;
					x = // getMap$ method
(_genJavadoc ? "\t/** Get XON array of this item."+LN+
"\t * @return XON array of this item."+LN+
"\t */"+LN : "")+
"\tpublic "+typ+" getArray$() {return toXon();}"+LN;
				} else { // map
					typ = "java.util.Map<String, Object>";
					s += "\tpublic " + typ + " toXon(){"+LN+
"\t\treturn org.xdef.component.XComponentUtil.toXonMap(this);"+LN+"\t}"+LN;
					x = // getMap$ method
(_genJavadoc ? "\t/** Get XON map of this item."+LN+
"\t * @return XON map of this item."+LN+
"\t */"+LN : "")+
"\tpublic "+typ+" getMap$() {return toXon();}"+LN;
				}
				getters.append(x); // getMap$ method
			}
		} else {
			s += "\tpublic Object toXon() {";
			if (any) { // %anyObj
				s += LN;
				s += nodes[2].maxOccurs() > 1
?("\t\tif (!_jx$array.isEmpty()) {"+LN+
"\t\t\tjava.util.List<Object> result = new java.util.ArrayList<>();"+LN+
"\t\t\tfor (jx$array x: _jx$array) result.add(x.toXon());"+LN+
"\t\t\treturn result;"+LN+
"\t\t}"+LN)
:("\t\tif (_jx$array != null) return _jx$array.toXon();"+LN);
				s += nodes[3].maxOccurs() > 1
?("\t\tif (!_jx$map.isEmpty()) return _jx$map.get(0).toXon();"+LN)
:("\t\tif (_jx$map != null) return _jx$map.toXon();"+LN);
				s +=
"\t\treturn _jx$item != null? _jx$item.toXon(): null;"+LN+"\t";
			} else {
				s += "return org.xdef.component.XComponentUtil.toXon(this);";
			}
			s += "}"+LN;
		}
		vars.append(s); //toXon method
	}
}