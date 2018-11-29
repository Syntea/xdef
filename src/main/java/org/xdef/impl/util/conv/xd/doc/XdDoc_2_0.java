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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
	 * @param xdNS namespace of X-definition.
	 */
	public XdDoc_2_0(Document xdef, String xdNS) {
		if (xdef == null) {
			throw new NullPointerException(
				"Given X-definition document is null");
		}
//		_xdNS = xdNS;
		validate(xdef);
		init(xdef);
	}

	/** Validates given X-definition document.
	 * @param xdef X-definition document to validate.
	 * @throws RuntimeException if given X-definition document is not valid or
	 * an error occurred during validation.
	 */
	private void validate(Document xdef) {
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
	private void init(Document xdef) {
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

	/** Initiates given X-definition <tt>collection</tt> element.
	 * @param collection X-definition <tt>collection</tt> element.
	 */
	private void initCollection(Element collection) {
		NodeList defs = KXmlUtils.getChildElementsNS(collection,
			XDConstants.XDEF20_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			Element def = (Element) defs.item(i);
			initDef(def);
		}
		defs = KXmlUtils.getChildElementsNS(collection,
			XDConstants.XDEF31_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			Element def = (Element) defs.item(i);
			initDef(def);
		}
		defs = KXmlUtils.getChildElementsNS(collection,
			XDConstants.XDEF32_NS_URI, XdNames.DEF);
		for (int i = 0; i < defs.getLength(); i++) {
			Element def = (Element) defs.item(i);
			initDef(def);
		}
	}

	/** Initiates given X-definition <tt>def</tt> element.
	 * @param def X-definition <tt>def</tt> element.
	 * @throws RuntimeException if error occurs during creating X-definition
	 * or model representation.
	 */
	private void initDef(Element def) {
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
				XdModel xdModel = XdUtils.createXdModel(model);
				if (xdModel != null) {
					_xdModels.put(xdModel, model);
				}
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
	public int getElemType(Element element) {
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
	public Map<XdModel, Element> getXdModels() {return _xdModels;}

	/** Returns instance of X-definition declaration representation if this
	 * X-definition document contains declaration with given name or null.
	 * @param xdDeclName name of X-definition declaration.
	 * @return instance of X-definition declaration representation or null.
	 */
	public XdDecl getXdDecl(String xdDeclName) {
		if (xdDeclName == null) {
			throw new NullPointerException(
				"Given X-definition declaration name is null");
		}
		if (xdDeclName.length() == 0) {
			throw new IllegalArgumentException(
				"Given X-definition declaration name is empty");
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
	public Map<XdDef, Element> getXdDefs() { return _xdDefs; }

}
