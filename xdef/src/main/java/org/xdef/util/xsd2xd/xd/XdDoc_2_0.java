package org.xdef.util.xsd2xd.xd;

import org.xdef.sys.SRuntimeException;
import org.xdef.XDConstants;
import org.xdef.xml.KXmlUtils;
import org.xdef.impl.util.gencollection.XDGenCollection;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Represents implementation of Xdefinition document version 2.0.
 * @author Ilia Alexandrov
 */
public final class XdDoc_2_0 extends XdDoc {

	/** Xdefinition model (XdModel) representation to model element mapping. */
	private final Map<XdModel, Element> _xdModels =
		new HashMap<XdModel, Element>();
	/** Xdefinition (XdDef) representation to Xdefinition element mapping.*/
	private final Map<XdDef, Element> _xdDefs = new HashMap<XdDef, Element>();

	/** Creates instance of Xdefinition version 2.0 document.
	 * @param xdef Xdefinition document.
	 * @param xdNS namespace of Xdefinition.
	 */
	public XdDoc_2_0(Document xdef, String xdNS) {
		if (xdef == null) {
			throw new NullPointerException(
				"Given Xdefinition document is null");
		}
		validate(xdef);
		init(xdef);
	}

	/** Validates given Xdefinition document.
	 * @param xdef Xdefinition document to validate.
	 * @throws RuntimeException if given Xdefinition document is not valid or
	 * an error occurred during validation.
	 */
	private void validate(Document xdef) {
		try {
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(xdef, true));
		} catch (SRuntimeException ex) {
			throw new RuntimeException(
				"Error during validation of Xdefinition", ex);
		}
	}

	/** Initiates given Xdefinition document.
	 * @param xdef Xdefinition document to initiate.
	 * @throws IllegalArgumentException if given document is not a valid
	 * Xdefinition document.
	 */
	private void init(Document xdef) {
		Element root = xdef.getDocumentElement();
		if (XdUtils.isCollection(root)) {
			initCollection(root);
		} else if (XdUtils.isDef(root)) {
			initDef(root);
		} else {
			throw new IllegalArgumentException(
				"Given document is not a valid Xdefinition document");
		}
	}

	/** Initiates given Xdefinition collection element.
	 * @param collection Xdefinition collection element.
	 */
	private void initCollection(Element collection) {
		NodeList defs = Utils.getChildElementsNS(collection,
			XDConstants.XDEF31_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			initDef((Element) defs.item(i));
		}
		defs = Utils.getChildElementsNS(collection,
			XDConstants.XDEF32_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			initDef((Element) defs.item(i));
		}
		defs = Utils.getChildElementsNS(collection,
			XDConstants.XDEF40_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			initDef((Element) defs.item(i));
		}
		defs = Utils.getChildElementsNS(collection,
			XDConstants.XDEF41_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			initDef((Element) defs.item(i));
		}
		defs = Utils.getChildElementsNS(collection,
			XDConstants.XDEF42_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			initDef((Element) defs.item(i));
		}
	}

	/** Initiates given Xdefinition def element.
	 * @param def Xdefinition def element.
	 * @throws RuntimeException if error occurs during creating Xdefinition
	 * or model representation.
	 */
	private void initDef(Element def) {
		XdDef xdDef;
		try {
			xdDef = XdUtils.getXdDef(def);
			_xdDefs.put(xdDef, def);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Error during creating Xdefinition def representation", ex);
		}
		NodeList models = Utils.getChildElements(def);
		for (int i = 0; i < models.getLength(); i++) {
			Element model = (Element) models.item(i);
			try {
				XdModel xdModel = XdUtils.createXdModel(model);
				_xdModels.put(xdModel, model);
			} catch (Exception ex) {
				throw new RuntimeException(
					"Error during cretaing model representation", ex);
			}
		}
	}

	/** Gets full element type of given element including referenced element.
	 * @param element element to get type of.
	 * @return element type constant.
	 * @throws NullPointerException if given element is null.
	 */
	public int getElemType(Element element) {
		if (element == null) {
			throw new NullPointerException("Given element is null");
		}
		int elemType = XdUtils.getElemType(element); //get element type
		//get referenced element
		XdElem refXdElem = XdUtils.getRefXdElem(element);
		if (refXdElem == null) {
			return elemType; //no reference
		}
		Element refElem = _xdModels.get(refXdElem); //get ref element
		int refElemType = getElemType(refElem); //get ref element type
		//return type union
		return XdUtils.getElemTypeUnion(elemType, refElemType);
	}

	/** Xdefinition model representation to element map getter.
	 * @return Xdefinition model representation (XdModel) to element map.
	 */
	public Map<XdModel, Element> getXdModels() {return _xdModels;}

	/** Returns instance of Xdefinition declaration representation if this
	 * Xdefinition document contains declaration with given name or null.
	 * @param xdDeclName name of Xdefinition declaration.
	 * @return instance of Xdefinition declaration representation or null.
	 */
	public XdDecl getXdDecl(String xdDeclName) {
		if (xdDeclName == null) {
			throw new NullPointerException(
				"Given Xdefinition declaration name is null");
		}
		if (xdDeclName.length() == 0) {
			throw new IllegalArgumentException(
				"Given Xdefinition declaration name is empty");
		}
		for (XdModel xdModel : _xdModels.keySet()) {
			if (XdModel.Type.DECLARATION == xdModel.getType()) {
				XdDecl xdDecl = (XdDecl) xdModel;
				if (xdDeclName.equals(xdDecl.getName())) {
					return xdDecl;
				}
			}
		}
		return null;
	}

	/** Xdefinition def representation to element map getter.
	 * @return Xdefinition def (XdDef) representation to element (Element) map.
	 */
	public Map getXdDefs() {return _xdDefs;}
}