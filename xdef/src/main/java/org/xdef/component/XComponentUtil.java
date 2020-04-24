package org.xdef.component;

import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import java.lang.reflect.Method;
import java.util.List;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xdef.json.JsonUtil;

/** Utilities used with XComponents.
 * @author Vaclav Trojan
 */
public class XComponentUtil {

	/** Get value of variable of given name from XComponent.
	 * @param xc XComponent.
	 * @param name name of variable.
	 * @return object with value of variable.
	 * @throws Exception if variable not exists.
	 */
	public static final Object getVariable(final XComponent xc,
		final String name) throws Exception {
		try {
			Class<?> clazz = xc.getClass();
			final Method method = clazz.getMethod("get" + name);
			method.setAccessible(true);
			return method.invoke(xc);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/** Set value of variable of given name from XComponent.
	 * @param xc XComponent.
	 * @param name name of variable.
	 * @param value value of variable.
	 * @throws Exception if variable not exists.
	 */
	public static final void setVariable(final XComponent xc,
		final String name,
		final Object value) throws Exception {
		try {
			final Class<?> clazz = xc.getClass();
			Method method = clazz.getMethod("get" + name);
			method.setAccessible(true);
			final Class<?> result = method.getReturnType();
			method = clazz.getMethod("set" + name, result);
			method.setAccessible(true);
			method.invoke(xc, value);
		} catch (Exception ex) {
			throw ex;
		}
	}

	/** Create XComponent from XComponent according to given model in XDPool.
	 * @param xc XComponent.
	 * @param xp XDPool.
	 * @param xdPosition the XDPosition of model in XDPool.
	 * @return created XComponent.
	 */
	public static final XComponent toXComponent(final XComponent xc,
		final XDPool xp,
		final String xdPosition) {
		final XMNode xm = xp.findModel(xdPosition);
		if (xm.getKind() != XMNode.XMELEMENT) {
			//Argument is not model of element: &{0}
			throw new SRuntimeException(XDEF.XDEF372, xm.getXDPosition());
		}
		final Element el = toXml(xc, (XMElement) xm);
		final XDDocument xd = xp.createXDDocument(xdPosition);
		return xd.parseXComponent(el, null, null);
	}

	/** Create the new XML element from XComponent according to model.
	 * @param xc XComponent.
	 * @param xm model according which element will be constructed.
	 * @return XML element created from this object according to given model.
	 */
	public static final Element toXml(final XComponent xc, final XMElement xm) {
		final XDDocument xd = xm.createXDDocument();
		xd.setXDContext(xc.toXml());
		return xd.xcreate(new QName(xm.getNSUri(), xm.getName()), null);
	}

	/** Create the new XML element from XComponent according to model.
	 * @param xc XComponent.
	 * @param xd XDDocument for creation on new Element.
	 * @param modelName name of model to be created.
	 * @return new Element.
	 */
	public static final Element toXml(final XComponent xc,
		final XDDocument xd,
		final String modelName) {
		xd.setXDContext(xc.toXml());
		return xd.xcreate(modelName, null);
	}

	/** Create XML element from XComponent according to given model from XDPool.
	 * @param xc XComponent.
	 * @param xp XDPool.
	 * @param xdPosition the XDPosition of model in XDPool.
	 * @return created Element.
	 */
	public static final Element toXml(final XComponent xc,
		final XDPool xp,
		final String xdPosition) {
		final XMNode xm = xp.findModel(xdPosition);
		if (xm.getKind() != XMNode.XMELEMENT) {
			//Argument is not model of element: &{0}
			throw new SRuntimeException(XDEF.XDEF372, xm.getXDPosition());
		}
		return toXml(xc, (XMElement) xm);
	}

	/** Create the new JSON object from XComponent according to model.
	 * @param xc XComponent.
	 * @param xm model according which element will be constructed.
	 * @return JSON object created from XComponent created from XComponent.
	 */
	public static final Object toJson(final XComponent xc, final XMElement xm) {
		return JsonUtil.xmlToJson(toXml(xc, xm));
	}

	/** Create the JSON object from XComponent according to model.
	 * @param xc XComponent.
	 * @param xd XDDocument for creation on new Element.
	 * @param modelName name of model to be created.
	 * @return JSON object created from XComponent according to model..
	 */
	public static final Object toJson(final XComponent xc,
		final XDDocument xd,
		final String modelName) {
		return JsonUtil.xmlToJson(toXml(xc, xd, modelName));
	}

	/** Create JSON object from XComponent according to given model from XDPool.
	 * @param xc XComponent.
	 * @param xp XDPool.
	 * @param xdPosition the XDPosition of model in XDPool.
	 * @return JSON object created from XComponent.
	 */
	public static final Object toJson(final XComponent xc,
		final XDPool xp,
		final String xdPosition) {
		return JsonUtil.xmlToJson(toXml(xc, xp, xdPosition));
	}

	/** Create XComponent from XComponent according to given model in XDPool.
	 * @param xc XComponent
	 * @param xm model of required result.
	 * @return created new XComponent.
	 */
	public static XComponent toXComponent(final XComponent xc,
		final XMElement xm) {
		final XDDocument xd = xm.createXDDocument();
		xd.setXDContext(xc.toXml());
		final Element el =
			xd.xcreate(new QName(xm.getNSUri(), xm.getName()), null);
		return xd.parseXComponent(el, null,  null);
	}

	/** Create XComponent from XComponent according to model.
	 * @param xc XComponent.
	 * @param xd XDDocument for creation on new Element.
	 * @param modelName name of model to be created.
	 * @return new XComponent.
	 */
	public static final XComponent toXComponent(final XComponent xc,
		final XDDocument xd,
		final String modelName) {
		xd.setXDContext(xc.toXml());
		final Element el = xd.xcreate(modelName, null);
		final XDDocument xd1 = xd.getXDPool().createXDDocument(modelName);
		return xd1.parseXComponent(el, null, null);
	}

	/** Add XComponent to the child list
	 * @param childList child list.
	 * @param xc XComponent.
	 */
	public static final void addXC(final List<XComponent> childList,
		final XComponent xc) {
		if (xc != null) {
			final int y = xc.xGetNodeIndex();
			if (y >= 0) {
				for (int i = 0; i < childList.size(); i++) {
					final int ndx = childList.get(i).xGetNodeIndex();
					if (ndx == -1) continue;
					if (ndx > y) {
						childList.add(i, xc);
						return;
					}
				}
			}
			childList.add(xc);
		}
	}

	/** Add list of XComponents to the child list
	 * @param childList child list.
	 * @param xc XComponent list.
	 */
	public static final void addXC(final List<XComponent> childList,
		final List<?>xc){
		for (int i = 0; i < xc.size(); i++) {
			XComponent y;
			if ((y = (XComponent) xc.get(i)) != null) {
				addXC(childList, y);
			}
		}
	}

	/** Add text the child list
	 * @param parent parent XComponent
	 * @param xdPos XDPosition
	 * @param xc XComponent.
	 * @param txt text
	 * @param index index where to add.
	 */
	public static final void addText(final XComponent parent,
		final String xdPos,
		final List<XComponent> xc,
		final String txt,
		final int index) {
		if (txt != null && txt.length() > 0) {
			int xp = 1;
			for (XComponent c : xc) {
				if ("$text".equals(c.xGetNodeName())) {
					xp++;
				}
			}
			final String xpos = parent.xGetXPos() + "/$text[" + xp + "]";
			addXC(xc, new XCTextComponent(txt, xdPos, xpos, index));
		}
	}

	/** Set seq. numbers to the list of XComponents.
	 * @param childList list of components.
	 */
	public static final void canonizeXC(final List<XComponent> childList) {
		for (int i = 0; i < childList.size(); i++) {
			childList.get(i).xSetNodeIndex(i);
		}
	}

	/** Update XPostitions.
	 * @param xc X-component.
	 * @param xpos X-position.
	 * @param index index of the referred item of X-position.
	 */
	private static void updateXPos(final XComponent xc,
		final String xpos,
		final int index) {
		String myXPos = xc.xGetNodeName();
		if (index > 0) {
			myXPos += '[' + String.valueOf(index + 1) + ']';
		}
		myXPos = xpos + "/" + myXPos;
		xc.xSetXPos(myXPos);
		List<XComponent> childNodes = xc.xGetNodeList();
		if (childNodes != null && !childNodes.isEmpty()) {
			for (int i = 0; i > childNodes.size(); i++) {
				XComponent xxi = childNodes.get(i);
				String iname = xxi.xGetNodeName();
				for (int j = i+1; j < childNodes.size(); j++) {
					XComponent xxj = childNodes.get(j);
					if (iname.equals(xxj.xGetNodeName())) {
						updateXPos(xxj, myXPos, j - i);
					}
				}
				for (int j = i+1; j < childNodes.size(); j++) {
					if (iname.equals(childNodes.get(j).xGetNodeName())) {
						childNodes.remove(j);
					}
				}
				updateXPos(xxi, myXPos, 0);
			}
		}
	}

	/** Updates XPositions in the subtree of XComponents starting
	 * with given argument.
	 * @param xc XComponent where updating starts.
	 */
	public static final void updateXPos(final XComponent xc) {
		updateXPos(xc, "", 0);
	}
}