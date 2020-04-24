package org.xdef.component;

import java.util.Iterator;
import java.util.Map;
import org.xdef.XDPool;
import org.xdef.impl.XElement;
import org.xdef.sys.ArrayReporter;

/** Methods for generation Java source code of getters/setters.
 * @author Vaclav Trojan
 */
class XCGeneratorBase1 extends XCGeneratorBase {

	XCGeneratorBase1(final XDPool xp,
		final ArrayReporter reporter,
		final boolean genJavadoc) {
		super(xp, reporter, genJavadoc);
	}

	private String genToXmlMethods(final XElement xe,
		final boolean isRoot,
		final StringBuilder creators,
		final StringBuilder genNodeList) {
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
		return toXml;
	}

	/** Final generation of Java sources from prepared data. */
	final String genSource(final XElement xe,
		final String xelName,
		final String model,
		final int index,
		final String xdname,
		final boolean isRoot,
		final String clazz,
		final String extClazz,
		final String interfcName,
		final StringBuilder vars,
		final StringBuilder creators,
		final StringBuilder getters,
		final StringBuilder setters,
		final StringBuilder xpathes,
		final StringBuilder listNodes,
		final StringBuilder innerClasses,
		final Map<String, String> atttab,
		final Map<String, String> txttab,
		final Map<String, String> xctab) {
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
"\tpublic int xGetModelIndex() {return "+index+";}"+LN;

////////////////////////////////////////////////////////////////////////////////
		result += genSeparator("Private methods", _genJavadoc) +
"\t@Override"+LN+
(_genJavadoc ? ("\t/** Create list of XComponents for creation of XML."+LN+
"* @return list of XComponents."+LN+
"\t */"+LN) : "") +
"\tpublic java.util.List<org.xdef.component.XComponent> xGetNodeList() {"
			+LN;
		if (listNodes.length() == 0) {
			result +=
"\t\treturn new java.util.ArrayList<org.xdef.component.XComponent>();"+LN+
"\t}"+LN;
		} else {
			result += listNodes + "\t\treturn a;"+LN+"\t}"+LN;
		}
		// generate toXml methods
		result += genToXmlMethods(xe, isRoot, creators, listNodes);
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
(listNodes.length() == 0 ? "" :
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
		int ndx;
		if (txttab.isEmpty()) {
			result +=
"\tpublic void xSetText(org.xdef.proc.XXNode x,"+LN+
"\t\torg.xdef.XDParseResult parseResult){}"+LN;
		} else if (txttab.size() == 1) {
			Map.Entry<String, String> e = txttab.entrySet().iterator().next();
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
			for(Map.Entry<String, String> e: txttab.entrySet()) {
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
			for (Iterator<Map.Entry<String, String>> it =
				atttab.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, String> en = it.next();
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
			Map.Entry<String, String> e = xctab.entrySet().iterator().next();
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
			for (Iterator<Map.Entry<String, String>> it =
				xctab.entrySet().iterator();
				it.hasNext();) {
				Map.Entry<String, String> e = it.next();
				String s = e.getValue().replace('#', '.');
				if (s.isEmpty()) {
					dflt = true;
				} else {
					result += ((it.hasNext() || dflt)
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
			for (Iterator<Map.Entry<String, String>> it =
				xctab.entrySet().iterator();
				it .hasNext();) {
				Map.Entry<String, String> e = it .next();
				String s = e.getValue().replace('#', '.');
				if (s.length() > 0) {
					String typ = s.substring(s.indexOf(";") + 1);
					String var = s.substring(2, s.indexOf(";"));
					s = s.charAt(0) == '1'
						? "set" + var + "(" + "(" + typ + ")x);"
						: "listOf" + var + "().add((" + typ + ")x);";
					s += !it .hasNext() ? " //" + e.getKey()+LN : LN;
					if (first) {
						result +=
"\t\tif (\"" + e.getKey() + "\".equals(s))"+LN+"\t\t\t" + s;
						first = false;
					} else {
						if (it .hasNext()) {
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
		return result + "// </editor-fold>"+LN+ innerClasses;
	}
}