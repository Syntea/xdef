package org.xdef.component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import org.xdef.XDPool;
import org.xdef.impl.XData;
import org.xdef.impl.XElement;
import org.xdef.impl.XNode;
import org.xdef.model.XMData;
import org.xdef.model.XMNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;

/** Generation of Java source code of XDComponents.
 * @author Vaclav Trojan
 */
class XCGeneratorOld extends XCGeneratorBase implements XCGenerator {

	/** New instance of this class.*/
	XCGeneratorOld(final XDPool xp,
		final ArrayReporter reporter,
		final boolean genJavadoc) {
		super(xp, reporter, genJavadoc);
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
		XElement xe;
		String xelName = xelem.getName();
		if (isRoot && xelem.getJsonMode() > 0 //JSON
			&& xelem._childNodes != null && xelem._childNodes.length == 1
			&& xelem._childNodes[0].getKind() == XMNode.XMELEMENT) {
			xe = (XElement) xelem._childNodes[0];
			xelName = xelem.getLocalName();
		} else {
			xe = xelem;
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
		final String localName = xe.getLocalName();
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
			interfcName.isEmpty() ? null : new StringBuilder();
		final Properties nsmap = new Properties();
		addNSUri(nsmap, xe);
		final Map<String, String> atttab = new LinkedHashMap<String, String>();
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
				genBaseVarsGettersSetters(xdata, name,
					1, "attribute", vars, getters, setters, xpathes, sbi);
			}
			genCreatorOfAttribute(xdata, name, creators);
			atttab.put(xdata.getXDPosition(),
				getParsedResultGetter(xdata) + ";" + name);
		}
		// Generate namespace attributes
		for (Map.Entry<Object, Object> item : nsmap.entrySet()) {
			final String value = (String) item.getValue();
			final String name = ((String) item.getKey());
			int i = name.indexOf('$'); // The ":" in name is replaced with "$"!
			String nsname = "xmlns" + (i>0 ? ':' + name.substring(i + 1) : "");
			String s =
"\t\tel.setAttributeNS(javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI,"+LN+
"\t\t\t\"" + nsname + "\", \"" + value + "\");"+LN;
			creators.append(s);
		}
		final XNode[] nodes = (XNode[]) xe.getChildNodeModels();
		final Map<String, String> xctab = new LinkedHashMap<String, String>();
		final Map<String, String> txttab = new LinkedHashMap<String, String>();
		final Stack<Integer> groupStack = new Stack<Integer>();
		final Stack<Object> choiceStack = new Stack<Object>();
		for (int i=0, txtcount=0, groupMax=1, groupFirst=-1, groupKind=-1;
			i < nodes.length; i++) {
			final XNode node = nodes[i];
			if (node.isIgnore() || node.isIllegal()) {
				continue;
			}
			if (node.getKind() == XMNode.XMCHOICE ||
				node.getKind() == XMNode.XMMIXED ||
				node.getKind() == XMNode.XMSEQUENCE) {
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
			if (node.getKind() == XMNode.XMSELECTOR_END) {
				if (groupKind == XMNode.XMCHOICE) {
					String s = "\t\t";
					ArrayList<String> ar = new ArrayList<String>();
					for (int j = choiceStack.size() - 1; j > 0; j -= 5) {
						s += '_' + (String) choiceStack.get(j-1) + "=null;";
						String t = (String) choiceStack.get(j-2);
						int k = (Integer) choiceStack.get(j-3);
						XElement xe1 = (XElement) nodes[k];
						XNode[] x = (XNode[]) xe1.getChildNodeModels();
						if ((x.length==1 && x[0].getKind() == XNode.XMTEXT)
							|| (x.length == 0 && "null".equals(
								xe1.getQName().getLocalPart()))) {
//						int n = t.lastIndexOf('.');
//						t = '_' + t.substring(n+1);
							ar.add(t);
						}
						if (k == groupFirst) {
							break;
						}
					}
					s += LN;
					for (;choiceStack.size() >= 5;) {
						int max = (Integer) choiceStack.pop();
						String iname = (String) choiceStack.pop();
						String typeName = (String) choiceStack.pop();
						int k = (Integer)choiceStack.pop();
						XElement xe1 = (XElement) nodes[k];
						boolean ext = (Boolean) choiceStack.pop();
						if (!ext) {
							genChildElementGetterSetter(xe1, typeName, iname,
								max, "element", getters, setters, sbi, s);
						}
						if (choiceStack.isEmpty() || k == groupFirst) {
							break;
						}
					}
				}
				groupMax = groupStack.pop(); // groupMax
				groupKind = groupStack.pop(); // kind
				groupFirst = groupStack.pop(); // index of first item
				continue;
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
					genBaseVarsGettersSetters(xdata, name, groupMax,
						"text node", vars, getters, setters, xpathes, sbi);
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
				if (groupKind == XMNode.XMCHOICE) {
					choiceStack.push(ext);
					choiceStack.push(i);
					choiceStack.push(typeName);
					choiceStack.push(iname);
					choiceStack.push(max);
				}
				if (!ext) {
					genVariableFromModel(typeName, iname, max, "element", vars);
					if (groupKind != XMNode.XMCHOICE){
						genChildElementGetterSetter(xe1, typeName,
							iname, max, "element", getters, setters, sbi, "");
					}
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
			_interfaces = sbi;
			final int i = interfcName.lastIndexOf('.');
			if (i > 0 && interfcName.substring(0, i).equals(packageName)) {
				interfcName = interfcName.substring(i + 1);
			}
		}
		if (clazz.isEmpty()) {
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
		// generate toXml methods
		String toXml = genToXmlMethods(xe, isRoot, creators, genNodeList);
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
"\tpublic final static byte JSON = 0;" +LN+
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
			if ((_byteArrayEncoding & 1) != 0) { //base64
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
			if ((_byteArrayEncoding & 2) != 0) { //hex
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
(_genJavadoc ? "\t/** Name of element model.*/"+LN : "") +
"\tpublic static final String XD_NAME=\"" + xelName + "\";"+LN+
(_genJavadoc ? "\t/** Parent XComponent node.*/"+LN : "") +
"\tprivate org.xdef.component.XComponent XD_Parent;"+LN+
(_genJavadoc ? "\t/** User object.*/"+LN : "") +
"\tprivate Object XD_Object;"+LN+
(_genJavadoc ? "\t/** Node name.*/"+LN : "") +
"\tprivate String XD_NodeName = \"" + xe.getName() + "\";"+LN+
(_genJavadoc ? "\t/** Node namespace.*/"+LN : "") +
"\tprivate String XD_NamespaceURI" +
	(xe.getNSUri() != null ? " = \"" + xe.getNSUri() + '"' : "") + ";"+LN+
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
				s += (s.isEmpty() ? "\t\t" : "\t\t} else ")
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
				s += s.isEmpty() ? "\t\t" : " else ";
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
				if (s.isEmpty()) {
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
		_interfaces = sbi;
		return result;
	}

	@Override
	/** Generate XComponent Java source class from X-definition.
	 * @param model name of model.
	 * @param className name of generated class.
	 * @param extClass class extension.
	 * @param interfaceName name of interface
	 * @param packageName the package of generated class (may be null).
	 * @param components Map with components.
	 * @return String with generated Java source code.
	 */
	public final String genXComponent(final String model,
		final String className,
		final String extClass,
		final String interfaceName,
		final String packageName,
		final Map<String, String> components) {
		final XMNode xn = _xp.findModel(model);
		if (xn == null || xn.getKind() != XMNode.XMELEMENT) {
			//Model "&{0}" not exsists.
			_reporter.add(Report.fatal(XDEF.XDEF373, model));
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
"// XDPosition: \"" +
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
			s += LN+"public interface "+interfaceName1
				+" extends org.xdef.component.XComponent {"+LN;
			_interfaces.insert(0, s).append("}");
		}
		if (className.isEmpty()) {
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

	@Override
	/** Get StringBuilder with interface specifications. */
	public final StringBuilder getIinterfaces() {
		return _interfaces;
	}
}