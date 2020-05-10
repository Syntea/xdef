package org.xdef.impl.util.conv.xd.doc;

import org.xdef.XDConstants;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.gencollection.XDGenCollection;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDecl;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDef;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdElem;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdModel;
import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xd.xd_2_0.XdUtils;
import org.xdef.impl.util.conv.Util;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xdef.impl.XConstants;
import org.xdef.impl.compile.XScriptParser;
import org.xdef.sys.SBuffer;

/** Represents implementation of X-definition document version 2.0.
 * @author Ilia Alexandrov
 */
public final class XdDoc_2_0 extends XdDoc {

	/** X-definition model (XdModel) representation to model element mapping. */
	private final Map<XdModel, Element> _xdModels =
		new HashMap<XdModel, Element>();
	/** X-definition (XdDef) representation to X-definition element mapping.*/
	private final Map<XdDef, Element> _xdDefs = new HashMap<XdDef, Element>();

	/** Creates instance of X-definition version 2.0 document.
	 * @param xdef X-definition document.
	 */
	public XdDoc_2_0(final Document xdef) {
		if (xdef == null) {
			throw new NullPointerException(
				"Given X-definition document is null");
		}
		validate(xdef);
		init(xdef);
	}

	/** Validates given X-definition document.
	 * @param xdef X-definition document to validate.
	 * @throws RuntimeException if given X-definition document is not valid or
	 * an error occurred during validation.
	 */
	private void validate(final Document xdef) {
		try {
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(xdef, true));
		} catch (SRuntimeException ex) {
			throw new RuntimeException(
				"Error during validation of X-definition", ex);
		}
	}

	/** Initiates given X-definition document.
	 * @param xdef X-definition document to initiate.
	 * @throws IllegalArgumentException if given document is not a valid
	 * X-definition document.
	 */
	private void init(final Document xdef) {
		Element root = xdef.getDocumentElement();
		if (XdUtils.isCollection(root)) {
			initCollection(root);
		} else if (XdUtils.isDef(root)) {
			initDef(root);
		} else {
			throw new IllegalArgumentException(
				"Given document is not a valid X-definition document");
		}
	}

	private void initCollection(final Element collection, final String nsURI) {
/*VT*/
		// process global declasrations
		NodeList nodes = KXmlUtils.getChildElementsNS(collection, //declarations
			nsURI,XdNames.DECLARATION);
		Map<String, String> xdTypes = new HashMap<String, String>();
		if (nodes.getLength() > 0) { // find declared types.
			NodeList defs = // xdefinitions
				KXmlUtils.getChildElementsNS(collection, nsURI,XdNames.DEF);
			String xdname = "_";
			int n = 0;
			// check if a X-definition have the name "_"; if yes, change it
			for (int i = 0; i < defs.getLength(); i++) {
				Element def = (Element) defs.item(i);
				if (xdname.equals(XDGenCollection.getXDName(def))) {
					xdname = "_" + (++n); // nsme already exists, change it
					i = 0; //and try again
				}
			}
			for (int i = 0; i < nodes.getLength(); i++) {
				Element declaration = (Element) nodes.item(i);
				XScriptParser p = new XScriptParser(XConstants.XML10);
				p.setSource(new SBuffer(KXmlUtils.getTextValue(declaration)),
					xdname, null, XConstants.XD32, null);
				// find dedlartation of types
				while (!p.eos()) {
				   if (XScriptParser.TYPE_SYM == p.nextSymbol()) {
						int pos = p.getIndex();
						if (XScriptParser.IDENTIFIER_SYM == p.nextSymbol()) {
							char sym;
							String name = p.getParsedBufferPartFrom(pos).trim();
							pos = p.getIndex();
							while ((sym=p.nextSymbol())
								!= XScriptParser.SEMICOLON_SYM
								&& sym != XScriptParser.END_SYM
								&& sym != XScriptParser.NOCHAR){}
							String typeDecl =
								p.getParsedBufferPartFrom(pos).trim();
							if (sym != XScriptParser.NOCHAR) {
								typeDecl =
									typeDecl.substring(0, typeDecl.length()-1);
							}
							xdTypes.put(name, typeDecl);
						}
					}
				}
			}
			if (!xdTypes.isEmpty()) {
				String s = "";
				for (Entry<String, String> e: xdTypes.entrySet()) {
					s += "type " + e.getKey() + " " + e.getValue() + ";\n";
				}
				Element def = collection.getOwnerDocument().createElementNS(
					collection.getNamespaceURI(), "xd:def");
				def.setAttribute("name", xdname);
				Element decl = collection.getOwnerDocument()
					.createElementNS(collection.getNamespaceURI(),
						"xd:declaration");
				decl.appendChild(
					collection.getOwnerDocument().createTextNode(s));
				def.appendChild(decl);
				collection.appendChild(def);
			}
		}
/*VT*/
		nodes = // xdefinitions
			KXmlUtils.getChildElementsNS(collection, nsURI, XdNames.DEF);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element def = (Element) nodes.item(i);
			initDef(def);
		}
	}

	/** Initiates given X-definition <tt>collection</tt> element.
	 * @param collection X-definition <tt>collection</tt> element.
	 */
	private void initCollection(final Element collection) {
		initCollection(collection, XDConstants.XDEF20_NS_URI);
		initCollection(collection, XDConstants.XDEF31_NS_URI);
		initCollection(collection, XDConstants.XDEF32_NS_URI);
		initCollection(collection, XDConstants.XDEF40_NS_URI);
	}

	/** Initiates given X-definition <tt>def</tt> element.
	 * @param def X-definition <tt>def</tt> element.
	 * @throws RuntimeException if error occurs during creating X-definition
	 * or model representation.
	 */
	private void initDef(final Element def) {
		XdDef xdDef;
		try {
			xdDef = XdUtils.getXdDef(def);
			_xdDefs.put(xdDef, def);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Error during creating X-definition def representation", ex);
		}
		NodeList models = KXmlUtils.getChildElements(def);
		for (int i = 0; i < models.getLength(); i++) {
			Element model = (Element) models.item(i);
			try {
/*VT*/
				if (XdUtils.isDeclaration(model)) {
					// get map with declared types
					Map<String, String> map = new HashMap<String, String>();
					Util.getDeclaredTypes(model, map);
					for (Entry<String, String> e: map.entrySet()) {
						_xdModels.put(new XdDecl(xdDef, e.getKey()), model);
					}
				} else {
					XdModel xdModel = XdUtils.createXdModel(model);
					if (xdModel != null) {
						_xdModels.put(xdModel, model);
					}
				}
/*VT*/
			} catch (Exception ex) {
				throw new RuntimeException(
					"Error during cretaing model representation", ex);
			}
		}
	}

	/** Gets full element type of given element including referenced element.
	 * @param element element to get type of.
	 * @return element type constant.
	 * @throws NullPointerException if given element is <tt>null</tt>.
	 */
	public final int getElemType(final Element element) {
		if (element == null) {
			throw new NullPointerException("Given element is null");
		}
		//get element type
		int elemType = XdUtils.getElemType(element);
		//get referenced element
		XdElem refXdElem = XdUtils.getRefXdElem(element);
		//no reference
		if (refXdElem == null) {
			return elemType;
		}
		//get ref element
		Element refElem = _xdModels.get(refXdElem);
		//get ref element type
		int refElemType = getElemType(refElem);
		//return type union
		return XdUtils.getElemTypeUnion(elemType, refElemType);
	}

	/** X-definition model representation to element map getter.
	 * @return X-definition model representation (XdModel) to element map.
	 */
	public final Map<XdModel, Element> getXdModels() {return _xdModels;}

	/** Returns instance of X-definition declaration representation if this
	 * X-definition document contains declaration with given name or null.
	 * @param xdDeclName name of X-definition declaration.
	 * @return instance of X-definition declaration representation or null.
	 */
	public final XdDecl getXdDecl(final String xdDeclName) {
		if (xdDeclName == null || xdDeclName.length() == 0) {
			throw new NullPointerException(
				"Given X-definition declaration name is null or ompty string");
		}
		Iterator<XdModel> it = _xdModels.keySet().iterator();
		while (it.hasNext()) {
			XdModel xdModel = it.next();
			if (XdModel.Type.DECLARATION == xdModel.getType()) {
				XdDecl xdDecl = (XdDecl) xdModel;
				if (xdDeclName.equals(xdDecl.getName())) {
					return xdDecl;
				}
			}
		}
		return null;
	}

	/** X-definition def representation to element map getter.
	 * @return X-definition def (XdDef) representation to element (Element) map.
	 */
	public final Map<XdDef, Element> getXdDefs() { return _xdDefs; }

}