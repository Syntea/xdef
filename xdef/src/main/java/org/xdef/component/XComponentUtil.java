package org.xdef.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xdef.XDConstants;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.json.XonTools;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.json.XonNames;

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
		final Object txt,
		final int index) {
		if (txt != null) {
			String s = txt.toString();
			if (s.length() > 0){
				int xp = 1;
				for (XComponent c : xc) {
					if ("$text".equals(c.xGetNodeName())) {
						xp++;
					}
				}
				final String xpos = parent.xGetXPos() + "/$text[" + xp + "]";
				addXC(xc, new XCTextComponent(txt.toString(), xdPos, xpos, index));
			}
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

	/** Convert XDContainer to jlist string.
	 * @param c XDContainer to be converted.
	 * @return string with values of the container from argument.
	 */
	private static String containerJlist(final XDContainer c) {
		int len = c.getXDItemsNumber();
		if (len == 0) {
			return "[ ]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(' ');
			XDValue y = c.getXDItem(i);
			if (y == null || y.isNull()) {
				sb.append("null");
			} else {
				if (y.getItemId() == XDValue.XD_STRING) {
					sb.append(y.toString());
				} else if (y.getItemId() == XDValue.XD_CONTAINER) {
					sb.append(containerJlist((XDContainer) y));
				} else {
					sb.append(y.toString());
				}
			}
		}
		return sb.append(" ]").toString();
	}

	/** Convert parsed value jlist to the jlist string (text of an XML element).
	 * @param parsedValue parsed value of jlist.
	 * @return the string with parsed value converted to string in jlist form.
	 */
	public static final String jlistToString(final XDParseResult parsedValue) {
		if (parsedValue == null) {
			return "null";
		}
		XDValue x = parsedValue.getParsedValue();
		if (x.getItemId() == XDValue.XD_CONTAINER) {
			return containerJlist((XDContainer) x);
		} else {
			return x.toString();
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Create XON object from X-component.
////////////////////////////////////////////////////////////////////////////////

	/** Create XON simple value from XComponent.
	 * @param xc XComponent
	 * @return object with XON simple value.
	 */
	private static Object toXonItem(final XComponent xc) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("get" + XonNames.X_VALUEATTR);
			Object o = m.invoke(xc);
			if (o instanceof String) {
				String s = (String) o;
				int len = s.length();
				if (len > 1 && s.charAt(0) == '"' && s.charAt(len-1) == '"') {
					StringParser p = new StringParser(s);
					p.setIndex(1);
					return XonTools.readJString(p);
				}
			}
			return o;
		} catch (Exception ex) {
			new RuntimeException("Can't access value", ex);
		}
		return null;
	}

	/** Create XON array from XComponent.
	 * @param xc XComponent
	 * @return object with XON array.
	 */
	private static List<Object> toXonArray(final XComponent xc) {
		List<Object> result = new ArrayList<Object>();
		List list = (List) xc.xGetNodeList();
		for (Object x : list) {
			Object o = toXon((XComponent) x);
			if (o instanceof String) {
				String s = (String) o;
				int len = s.length();
				if (len >= 2 && s.charAt(0) == '"' && s.charAt(len-1) == '"') {
					o = s.substring(1, len-1);
				}
			}
			result.add(o);
		}
		return result;
	}

	public static final String dateToJstring(final SDatetime x) {
		String s = x.toISO8601();
		if (s.matches("-?\\d+\\z")) { // year without zone
			s = '"' + s + '"'; // if year is without zone we put it into quotes
		}
		return s;
	}

	/** Create XON map from XComponent.
	 * @param xc XComponent
	 * @return object with XON map.
	 */
	private static Map<String, Object> toXonMap(final XComponent xc) {
		Class<?> cls = xc.getClass();
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		Method[] methods = cls.getDeclaredMethods();
		for (Method x: methods) {
			if (x.getName().startsWith("getjs$")
				&& x.getParameterTypes().length == 0) {
				x.setAccessible(true);
				Object o = null;
				try {
					o = x.invoke(xc);
				} catch (Exception ex) {
					new RuntimeException("Can't access getter: " + x.getName());
				}
				if (o instanceof XComponent) {
					String key = null;
					try {
						Class<?> cls1 = o.getClass();
						Method m = cls1.getMethod("get" + XonNames.X_KEYATTR);
						m.setAccessible(true);
						key = XonTools.xmlToJName((String) m.invoke(o));
					} catch (Exception ex) {
						new RuntimeException("Not key", ex);
					}
					o = toXon((XComponent) o);
					result.put(key, o);
				} else {
					new RuntimeException("Not XComponent: " + o);
				}
			}
		}
		return result;
	}

	/** Create XON object from X-component.
	 * @param xc X-component.
	 * @return XON object.
	 */
	public final static Object toXon(final XComponent xc) {
		String ns = xc.xGetNamespaceURI();
		String name = xc.xGetNodeName();
		if (XDConstants.JSON_NS_URI_W3C.equals(ns)) {
			if (XonNames.X_MAP.equals(name)) {
				return toXonMap(xc);
			} else if (XonNames.X_ARRAY.equals(name)) {
				return toXonArray(xc);
			} else if (XonNames.X_ITEM.equals(name)) {
				return toXonItem(xc);
			}
		}
		throw new RuntimeException("Unknown item: " + name + "; ns=" + ns);
	}
}