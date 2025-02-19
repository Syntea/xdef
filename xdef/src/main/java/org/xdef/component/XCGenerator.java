package org.xdef.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import static org.xdef.XDConstants.XON_NS_URI_W;
import org.xdef.XDPool;
import static org.xdef.component.XCGeneratorBase.LN;
import static org.xdef.component.XCGeneratorBase.RESERVED_NAMES;
import static org.xdef.component.XCGeneratorBase.addNSUri;
import static org.xdef.component.XCGeneratorBase.checkUnique;
import static org.xdef.component.XCGeneratorBase.correctClassName;
import static org.xdef.component.XCGeneratorBase.genCreatorOfAttribute;
import static org.xdef.component.XCGeneratorBase.getParsedResultGetter;
import static org.xdef.component.XCGeneratorBase.getUniqueName;
import static org.xdef.component.XComponentUtil.xmlToJavaName;
import org.xdef.impl.XConstants;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;
import org.xdef.xon.XonNames;
import static org.xdef.xon.XonNames.X_KEYATTR;

/** Generation of Java source code of XDComponents.
 * @author Vaclav Trojan
 */
final class XCGenerator extends XCGeneratorXON {

	/** New instance of this class.*/
	XCGenerator(final XDPool xp, final ArrayReporter rep, final boolean genDoc){
		super(xp, rep, genDoc);
	}

	/** Generation of Java code of class composed from XDElement.
	 * @param xelem XDElement from which Java code is composed.
	 * @param index index of model.
	 * @param className class name.
	 * @param extClass class extension and interfaces or an empty string.
	 * @param interfaceName name of interface.
	 * @param classNameBase prefix for inner class names.
	 * @param packageName name of package.
	 * @param  Map with components.
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
		final Map<String, XComponentInfo> components,
		final Set<String> clsNames,
		final boolean isRoot) {
		String extClazz = extClass;
		String interfcName = interfaceName;
		_components = components;
		XElement xe;
		String xelName = xelem.getName();
		if (isRoot && xelem.getXonMode() > 0 //XON/JSON
			&& xelem._childNodes.length == 1 && xelem._childNodes[0].getKind() == XMELEMENT) {
			xe = (XElement) xelem._childNodes[0]; /**/
			xelName = xelem.getLocalName();
		} else {
			xe = xelem;
		}
		final String model = xe.getName();
		final Set<String> classNames = new HashSet<>(RESERVED_NAMES);
		if (clsNames != null) {
			classNames.addAll(clsNames);
		}
		if (isRoot && className != null) {
			classNames.add(className);
		}
		final String xdname = xe.getXMDefinition().getName();
		final String localName = xe.getLocalName();
		final String clazz = className == null ? localName : className;
		final StringBuilder vars = new StringBuilder();
		final Set<String> varNames = new HashSet<>();
		final StringBuilder getters = new StringBuilder();
		final StringBuilder xpathes = new StringBuilder();
		final StringBuilder setters = new StringBuilder();
		final StringBuilder creators = new StringBuilder();
		final StringBuilder  listNodes = new StringBuilder();
		final StringBuilder innerClasses = new StringBuilder();
		final StringBuilder sbi = interfcName.isEmpty() ? null : new StringBuilder(); // interface
		final Properties nsmap = new Properties();
		addNSUri(nsmap, xe);
		final Map<String, String> atttab = new LinkedHashMap<>();
		int ndx;
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
							//In command "%class &amp;{0}" is missing parameter "extends". In command
							// "%bind &amp;{2}" is parameter "%with &amp;{1}"!
							_reporter.error(XDEF.XDEF375,
								className, name.substring(ndx+7), name.substring(0, ndx));
							name = name.substring(0, ndx);
						} else {
							//Class &{0} is not root. It can't be extended
							// to &{1} according to command %bind &{2}
							_reporter.error(XDEF.XDEF376,
								className, name.substring(ndx+7), name.substring(0, ndx));
						}
					}
				}
			} else {
				name = xmlToJavaName(xdata.getName());
			}
			name = addVarName(varNames, name, xdata.getXDPosition(), ext);
			genAttrNameVariable(name, xdata, vars);
			if (!ext) {
				genBaseVarsGettersSetters(xdata, name, 1, "attribute", vars, getters, setters, xpathes, sbi);
			}
			genCreatorOfAttribute(xdata, name, creators);
			atttab.put(xdata.getXDPosition(), getParsedResultGetter(xdata) + ";" + name);
		}
		// Generate namespace attributes
		for (Map.Entry<Object, Object> item : nsmap.entrySet()) {
			final String value = (String) item.getValue();
			if (xe._xon == XConstants.XON_MODE_W && XON_NS_URI_W.equals(value)){
				continue;
			}
			final String name = ((String) item.getKey());
			int i = name.indexOf('$'); // The ":" in name is replaced with "$"!
			String nsname = "xmlns" + (i>0 ? ':' + name.substring(i + 1) : "");
			String s =
"\t\tel.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,"+LN+
"\t\t\t\"" + nsname + "\", \"" + value + "\");"+LN;
			creators.append(s);
		}
		final XNode[] nodes = (XNode[]) xe.getChildNodeModels();
		final Map<String, String> xctab = new LinkedHashMap<>();
		final Map<String, String> txttab = new LinkedHashMap<>();
		final Stack<Integer> groupStack = new Stack<>();
		final Stack<Object> choiceStack = new Stack<>();
		for (int i=0, txtcount=0, groupMax=1, groupFirst=-1, groupKind=-1;
			i < nodes.length; i++) {
			final XNode node = nodes[i];
			if (node.isIgnore() || node.isIllegal()) {
				continue;
			}
			switch (node.getKind()) {
				case XMCHOICE:
				case XMMIXED:
				case XMSEQUENCE: {
					groupStack.push(groupFirst); // index of first item
					groupStack.push(groupKind); // kind
					groupStack.push(groupMax); // groupMax
					if (node.maxOccurs() > 1) {
						groupMax = groupMax > 1 ? groupMax : node.maxOccurs();
					}
					groupFirst = i+1;
					groupKind = node.getKind();
					continue;
				}
				case XMSELECTOR_END: {
					if (groupKind == XMCHOICE) {
						String xclear = "";
						for (int j = choiceStack.size() - 1; j > 0; j -= 5) {
							xclear += (String) choiceStack.get(j-1) //iname
								+ ((Integer)choiceStack.get(j) > 1
								? ".clear();"//it is final List so clear it!
								: "=null;"); //othrewise set null
							if ((Integer) choiceStack.get(j-3) == groupFirst) {
								break; // index == first, finish;
							}
						}
						List<String> keys = new ArrayList<>();
						for (;choiceStack.size() >= 5;) {
							int max = (Integer) choiceStack.pop();
							String iname = (String) choiceStack.pop();
							String typeName = (String) choiceStack.pop();
							int k = (Integer)choiceStack.pop();
							XElement xe1 = (XElement) nodes[k];
							boolean ext = (Boolean) choiceStack.pop();
							if (!ext) {
								genChildElementGetterSetter(xe1,
									typeName, iname, max, "element", getters, setters, sbi, xclear);
							}
							XMData keyAttr;
							if (xe1.getXonMode() != 0 && (keyAttr=xe1.getAttr(XonNames.X_KEYATTR)) != null
								&& keyAttr.getFixedValue() != null) {
								keys.add(keyAttr.getFixedValue().stringValue());
								keys.add(typeName);
								keys.add(iname);
							}
							if (choiceStack.isEmpty() || k == groupFirst) {
								break;
							}
						}
						if (!keys.isEmpty()) {// generate named value getters
							genNamedValueGetters(keys, varNames, getters);
						}
					}
					if (!groupStack.isEmpty()) {
						groupMax = groupStack.pop(); // groupMax
						groupKind = groupStack.pop(); // kind
						groupFirst = groupStack.pop(); // index of first item
					}
					continue;
				}
				case XMTEXT: {
					final XData xdata = (XData) node;
					String name = checkBind(xe, xdata);
					boolean ext = false;
					if (name != null) {
						if ((ndx = name.indexOf(" %with ")) > 0) {
							if (extClazz.startsWith(" extends ")) {
								ext = true;
								name = name.substring(0, ndx);
							} else {
								if (isRoot) {
									ext = true;
									extClazz = " extends " + name.substring(ndx+7) + extClazz;
									//"In command "%class &{0}" is missing parameter
									//"extends". In command "%bind &{2}" is parameter "%with &{1}!
									_reporter.error(XDEF.XDEF375,
										className, name.substring(ndx+7), name.substring(0, ndx));
									name = name.substring(0, ndx);
								} else {
									//Class &{0} is not root. It can't be extended
									//to &{1} according to command %bind &{2}
									_reporter.error(XDEF.XDEF376,
										className, name.substring(ndx+7), name.substring(0, ndx));
								}
							}
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
					}
					name = addVarName(varNames, name,xdata.getXDPosition(),ext);
					if (!ext) {
						genBaseVarsGettersSetters(xdata,
							name, groupMax, "text node", vars, getters, setters, xpathes, sbi);
					}
					String s =
((_genJavadoc ? "\t/** Indexes of values of &{d} \""+name.replace('$', ':')+
"\".*/"+LN : "")+
"\tprivate " + (groupMax > 1 ? "StringBuilder":"char") + " _$" + name+"= "+
(groupMax > 1 ? "new StringBuilder()" : "(char) -1") + ";"+LN);
					vars.append(s);
					genTextNodeCreator(xdata, name, groupMax,  listNodes);
					txttab.put(node.getXDPosition(),
						(groupMax == 1 ? "1" : "2") + "," + getParsedResultGetter(xdata) + ";" + name);
					if (isRoot && nodes.length==1 && xe.getAttrs().length==0) {
						 // no attrs,only text; direct getters/setters for text child
						genDirectSetterAndGetter(
							xe, xmlToJavaName(xe.getName()), null, true, setters, getters, sbi);
					}
					continue;
				}
				case XMELEMENT: {
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
							newClassName = xmlToJavaName(name.substring(0,ndx));
							name = xmlToJavaName(name.substring(ndx+1));
						} else if ((ndx = name.indexOf(" %with ")) > 0) {
							if (extClazz.startsWith(" extends")) {
								ext = true;
								name = name.substring(0, ndx);
							} else {
								if (isRoot) {
									ext = true;
									extClazz = " extends "
										+ name.substring(ndx+7)+extClazz;
									//"In command "%class &{0}" is missing parameter "extends". In command
									// "%bind &{2}" is parameter "%with &{1}!
									_reporter.error(XDEF.XDEF375,
										className, name.substring(ndx+7), name.substring(0, ndx));
									name = name.substring(0, ndx);
								} else {
									//Class &{0} is not root. It can't extend
									//to &{1} according to command %bind &{2}
									_reporter.error(XDEF.XDEF376,
										className, name.substring(ndx+7), name.substring(0, ndx));
								}
							}
							newClassName = name;
						}
					} else {
						newClassName = name = xmlToJavaName(xe1.getName());
					}
					//if this element is not processed by user XComponent and if it is unique and if the only
					// child node of this node is text node and if it has no attributes then process
					//it is processed same way as an attribute of parent class.
					final String xcClass0 = isRecurseRef ? name : getXDPosition(xe1, interfcName.length()>0);
					String xcClass = xcClass0;
					if (xcClass0 != null) {
						if (xcClass.indexOf("%ref ") ==0) {
							xcClass = xcClass.substring(5);
						}
						if (xcClass.startsWith("interface ")) {
							xcClass = xcClass.substring(10);
							ndx = xcClass.lastIndexOf('.');
							if (ndx > 0 && xcClass.substring(0, ndx).equals(packageName)) {
								xcClass = xcClass.substring(ndx + 1);
							}
							xcClass = xcClass.replace('#','.');
						} else if ((ndx = xcClass.indexOf(' ')) > 0) {
							// remove extends and interface
							xcClass =  xcClass.substring(0, ndx);
							ndx = xcClass.lastIndexOf('.');
							if (ndx > 0 && xcClass.substring(0, ndx).equals(packageName)) {
								xcClass = xcClass.substring(ndx + 1);
							}
							xcClass = xcClass.replace('#','.');
						}
					}
					String iname = correctClassName(newClassName,
						classNameBase, classNames);
					if (!newClassName.equals(iname)) {
						//The name of the inner class of the X-component &{0} has been changed to &{1}
						_reporter.info(XDEF.XDEF377, node.getXDPosition(), iname);
						newClassName = iname;
					}
					iname = getUniqueName(name, RESERVED_NAMES);
					iname = getUniqueName(iname, varNames);
					if (!name.equals(iname)) {
						if (ext) {
							//Getter/setter name &{0} in &{1} can't be used.
							// Please change name by command %bind
							_reporter.error(XDEF.XDEF371, name, node.getXDPosition());
						} else {
							//Getter/setter name &{0} in &{1} was changed to &{2}.
							// You can define other name by command %bind
							_reporter.warning(XDEF.XDEF360, name, node.getXDPosition(), iname);
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
							new XComponentInfo(packageName.length() > 0
								? packageName+'.'+typeName : typeName, xe1.getNSUri()));
					}
					ndx = typeName.lastIndexOf('.');
					if (ndx > 0 && packageName.equals(typeName.substring(0, ndx))) {
						typeName = typeName.substring(ndx + 1);
					}
					typeName = typeName.replace('#', '.');
					if (groupKind == XMCHOICE) {
						choiceStack.push(ext);
						choiceStack.push(i);
						choiceStack.push(typeName);
						choiceStack.push(iname);
						choiceStack.push(max);
					}
					XNode[] xnds = (XNode[]) xe1.getChildNodeModels();
					if (!ext) {
						genVariableFromModel(null,
							typeName, iname, max, "element", vars);
						if (xnds.length==1 && xnds[0].getKind()==XMTEXT && groupKind != XMCHOICE
							&& xe1.getAttrs().length == 0) {//no attrs,only text
							// direct getters/setters for text child
							genDirectSetterAndGetter(xe1, iname, typeName, false, setters, getters, sbi);
						}
						if (groupKind != XMCHOICE){
							genChildElementGetterSetter(
								xe1, typeName, iname, max, "element", getters, setters, sbi,"");
						}
					}
					genChildElementCreator(iname,  listNodes, max > 1);
					if (xe1._xon == XConstants.XON_MODE_W) {
						if (XON_NS_URI_W.equals(xe1.getNSUri())) {
							if (groupKind != XMCHOICE) {
								if (XonNames.X_VALUE.equals(xe1.getLocalName())){
									genXonItemGetterAndSetter(
										xe1, typeName, iname, max, setters, getters, sbi, varNames);
								} else if (xe1.getAttr(X_KEYATTR) != null) {
									genXonEntryMethod(xe1, typeName, iname, max, getters, sbi, varNames);
								}
							}
						} else { // XON map items
							if (groupKind != XMCHOICE) {
								genXonItemGetterAndSetter(
									xe1, typeName, iname, max, setters, getters, sbi, varNames);
							}
						}
					}
					// generate if it was not declared as XComponent
					String xval = (max == 1 ? "1" : "2") + "," + iname + ";";
					if (xcClass0 == null || xcClass0.startsWith("interface ")) {
						xctab.put(node.getXDPosition(), xval + newClassName);
						innerClasses.append(genComponent(xe1, //Elememnt model
							i, // index
							newClassName, //class name
							"", //no class extension and interfaces
							(xcClass0 != null)? xcClass0.substring(10): "",
							(packageName.length() > 0 //interface
								? packageName+"." : "")+classNameBase+'#'+newClassName,
							"", //classNameBase
							_components, //Map with components
							classNames, //Set with class names or null
							false)); //not root element.
						innerClasses.append('}').append(LN);
					} else {//other root class
						xctab.put(node.getXDPosition(), xval + xcClass);
					}
				}
			}
		}
		genToXonMethod(xe, getters, vars);
		// attributes and child nodes processed
		if (isRoot) {
			_interfaces = sbi;
			ndx = interfcName.lastIndexOf('.');
			if (ndx > 0 && interfcName.substring(0, ndx).equals(packageName)) {
				interfcName = interfcName.substring(ndx + 1);
			}
		}
		if (clazz.isEmpty()) {
			return null;
		}
		if (xe.isReference()) {
			XComponentInfo x = _components.get(xe.getReferencePos());
			String xpos = x != null ? x.getName() : null;
			if (xpos != null && xpos.startsWith("interface ")) {
				xpos = xpos.substring(10);
				if (!xpos.equals(interfcName)) {
					ndx = extClazz.indexOf("implements ");
					if (ndx >= 0) {
						if (!extClazz.contains(xpos)) {
							extClazz = extClazz.substring(0, ndx+11)+ xpos +","+ extClazz.substring(ndx+11);
						}
					} else {
						extClazz += " implements " + xpos;
					}
				}
			}
		}
		_interfaces = sbi;
		// generate Java source
		return genSource(xe,
			xelName,
			model,
			index,
			xdname,
			isRoot,
			clazz,
			extClazz,
			interfcName,
			vars,
			creators,
			getters,
			setters,
			xpathes,
			listNodes,
			innerClasses,
			atttab,
			txttab,
			xctab);
	}

	/** Generate XComponent Java source class from Xdefinition.
	 * @param model name of model.
	 * @param className name of generated class.
	 * @param extClass class extension.
	 * @param interfaceName name of interface
	 * @param packageName the package of generated class (may be null).
	 * @param components Map with components.
	 * @param genJavadoc switch to generate JavaDoc.
	 * @return String with generated Java source code.
	 */
	final String genXComponent(final String model,
		final String className,
		final String extClass,
		final String interfaceName,
		final String packageName,
		final Map<String, XComponentInfo> components) {
		final XMNode xn = _xp.findModel(model);
		if (xn == null || xn.getKind() != XMELEMENT) {
			_reporter.add(Report.fatal(XDEF.XDEF373, model)); //Model "&{0}" not exsists.
			return null;
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
"// Xposition: \"" +
(definitionName == null ? "" : definitionName) + '#' +	modelName + "\"."+LN+
"// Any modifications to this file will be lost upon recompilation."+LN;
		String packageName1 = packageName;
		String interfaceName1 = interfaceName;
		if (_interfaces != null) {
			packageName1 = "";
			if ((ndx = interfaceName1.lastIndexOf('.')) > 0) {
				packageName1 = interfaceName1.substring(0, ndx);
				interfaceName1 = interfaceName1.substring(ndx + 1);
			}
			String s = hdrTemplate;
			if (packageName1 != null && packageName1.length() > 0) {
				s += "package " + packageName1 + ";"+LN;
			}
			s += LN+
"public interface "+interfaceName1+" extends org.xdef.component.XComponent {"+LN;
			_interfaces.insert(0, s).append("}");
		}
		if (className.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder(
			SUtils.modifyString(hdrTemplate,
				"&{xdpos}", (definitionName != null ? "" : definitionName) + '#' + modelName));
		if (packageName1 != null && packageName1.length() > 0) {
			sb.append("package ").append(packageName1).append(';').append(LN);
		}
		return sb.append(result).append("}").toString();
	}
}
