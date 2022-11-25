package org.xdef.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XON_NS_PREFIX;
import static org.xdef.XDConstants.XON_NS_URI_W;
import static org.xdef.XDConstants.XON_NS_URI_XD;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.xml.KNamespace;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;
import org.xdef.xon.XonTools;

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
			final Method method = clazz.getDeclaredMethod("get" + name);
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
			Method method = clazz.getDeclaredMethod("get" + name);
			method.setAccessible(true);
			final Class<?> result = method.getReturnType();
			method = clazz.getDeclaredMethod("set" + name, result);
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
		return xd.xparseXComponent(el, null, null);
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
		return xd.xparseXComponent(el, null,  null);
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
		return xd1.xparseXComponent(el, null, null);
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
				addXC(xc, new XCTextComponent(txt.toString(),xdPos,xpos,index));
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
				XComponent xci = childNodes.get(i);
				String iname = xci.xGetNodeName();
				for (int j = i+1; j < childNodes.size(); j++) {
					XComponent xcj = childNodes.get(j);
					if (iname.equals(xcj.xGetNodeName())) {
						updateXPos(xcj, myXPos, j - i);
					}
				}
				for (int j = i+1; j < childNodes.size(); j++) {
					if (iname.equals(childNodes.get(j).xGetNodeName())) {
						childNodes.remove(j);
					}
				}
				updateXPos(xci, myXPos, 0);
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
	private static Object toXonObject(final Object o) {
		if (o instanceof String) {
			return XonTools.xmlToJValue((String) o);
		} else if (o instanceof XonTools.JNull) {
			return null;
		}
		return o;
	}
	/** Create XON simple value from XComponent.
	 * @param xc XComponent
	 * @return object with XON simple value.
	 */
	private static Object toXonItem(final XComponent xc) {
		Class<?> cls = xc.getClass();
		try {
			Method m = cls.getDeclaredMethod("get" + X_VALATTR);
			m.setAccessible(true);
			return toXonObject(m.invoke(xc));
		} catch (Exception ex) {
			new RuntimeException("Can't access value", ex);
		}
		return null;
	}

	/** Create XON array from XComponent.
	 * @param xc XComponent
	 * @return object with XON array.
	 */
	public final static List<Object> toXonArray(final XComponent xc) {
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
		int i = (s.charAt(0) == '0') ? 1 : 0;
		int len =  s.length();
		for (; i < len; i++) {
			char ch = s.charAt(i);
			if (ch < '0' || ch > '9') {
				i = -1;
				break;
			}
		}
		return i > 0 ? '"' + s + '"' : s; // if it is integer put it into quotes
	}

	/** Put items to XON map from XComponent.
	 * @param xc XComponent
	 * @param methods array with methods.
	 * @param result the map where put items.
	 */
	private static void toXonMap(final XComponent xc,
		final Method[] methods,
		final Map<String, Object> result) {
		for (Method x: methods) {
			String methodName = x.getName();
			Object o;
			if (methodName.startsWith("get" + XON_NS_PREFIX + "$")
				&& x.getParameterTypes().length == 0) {
				o = null;
				String key = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					Method m = o.getClass().getDeclaredMethod("get"+X_KEYATTR);
					m.setAccessible(true);
//					key = XonTools.xmlToJName((String) m.invoke(o));
					key = XonTools.xmlToJName((String) m.invoke(o));
					if (o instanceof XComponent) {
						XComponent xc1 = (XComponent) o;
						result.put(key, xc1.toXon()); /*xx*/
					} else {
						result.put(key, o);
					}
				} catch (Exception ex) {}
			} else if (methodName.startsWith("entriesOf$")) {
				o = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					if (o instanceof Map) {
						for (Object y: ((Map) o).entrySet()) {
							Map.Entry z = (Map.Entry) y;
							String key = /*xx*/
								XonTools.xmlToJName((String) z.getKey());
							result.put(key, z.getValue());
						}
					}
				} catch (Exception ex) {}
			} else if (methodName.startsWith("listOf$")) {
				o = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					String key = methodName.substring(7);
					result.put(key, o);
					break;
				} catch (Exception ex) {}
			} else if (x.getParameterTypes().length == 0
				&& methodName.startsWith("listOf"+XON_NS_PREFIX+"$")) {
				o = null;
				String key = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					if (o instanceof XComponent) {
						Method m = o.getClass().getDeclaredMethod(
							"get" + X_KEYATTR);
						m.setAccessible(true);
						key = XonTools.xmlToJName((String) m.invoke(o));
						XComponent xc1 = (XComponent) o;
						result.put(key, xc1.toXon());
					} else {/*xx*/
						if (o instanceof ArrayList) {
							for(Object oo : (ArrayList)o) {
								if (oo instanceof XComponent) {
									XComponent xx = (XComponent) oo;
									Method m = xx.getClass().getDeclaredMethod(
										"get" + X_KEYATTR);
									m.setAccessible(true);
									key = XonTools.xmlToJName((String) m.invoke(oo));
									result.put(key, xx.toXon());
								}
							}
						}
					}
				} catch (Exception ex) {}
			}
		}
	}

	/** Create XON map from XComponent.
	 * @param xc XComponent
	 * @return object with XON map.
	 */
	public static Map<String, Object> toXonMap(final XComponent xc) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		Class<?> cls = xc.getClass();
		toXonMap(xc, cls.getDeclaredMethods(), result);
		return result;
	}
	/** Create XON map from XComponent.
	 * @param xc XComponent
	 * @param nsStack namespace prefixes stack.
	 * @return object with XON map.
	 */
	private static Map<String, Object> toXonMapXD(final XComponent xc,
		final KNamespace nsStack) {
		Class<?> cls = xc.getClass();
		Method[] methods = cls.getDeclaredMethods();
		Map<String, Object> result = getXonAttrs(xc);
		for (Method x: methods) {
			String name = x.getName();
			if (!name.startsWith("get$") && name.startsWith("get")
				&& x.getParameterTypes().length == 0) {
				Object o = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
				} catch (Exception ex) {
					new RuntimeException("Can't access getter: " + x.getName());
				}
				if (o instanceof XComponent) {
					XComponent y = (XComponent) o;
					String key = null;
					if (XON_NS_URI_XD.equals(y.xGetNamespaceURI())){
						try {
							Class<?> cls1 = o.getClass();
							Method m = cls1.getDeclaredMethod(
								"get" + X_KEYATTR);
							m.setAccessible(true);
							key = XonTools.xmlToJName((String) m.invoke(o));
						} catch (Exception ex) {
							new RuntimeException("Not key", ex);
						}
						o = toXon((XComponent) o);
						result.put(key, o);
					} else {
						Map z = (Map) toXonXD(y, nsStack);
						for (Object k: z.keySet()) {
							key = (String)k;
							if (!key.startsWith("xmlns")) {
								break;
							}
						}
						o = z.get(key);
						if (o instanceof List && ((List) o).size()==1
							 && ((List) o).get(0) instanceof String) {
							o = XonTools.xmlToJValue((String)((List) o).get(0));
						}
						result.put(XonTools.xmlToJName(key), o);
					}
				} else {
					new RuntimeException("Not XComponent: " + o);
				}
			}
		}
		return result;
	}

	/** Create XON array from XComponent.
	 * @param xc XComponent
	 * @param nsStack namespace prefixes stack.
	 * @return object with XON array.
	 */
	private static List<Object> toXonArrayXD(final XComponent xc,
		final KNamespace nsStack) {
		List<Object> result = new ArrayList<Object>();
		Map<String, Object> attrs = getXonAttrs(xc);
		if (!attrs.isEmpty()) {
			result.add(attrs);
		}
		List<XComponent> components = xc.xGetNodeList();
		int textIndex = 0;
		if (components != null) {
			for (XComponent x : components) {
				if ("$text".equals(x.xGetNodeName())) {
					Class<?> cls = xc.getClass();
					String mName = "get$value";
					if (textIndex > 0) {
						mName += textIndex;
					}
					textIndex++;
					try {
						Method m = cls.getDeclaredMethod(mName);
						m.setAccessible(true);
						Object o = m.invoke(xc);
						if (o instanceof String) {
							o = toXonObject(o);
							if (o instanceof List && !((List) o).isEmpty()) {
								for (Object y: (List) o) {
									result.add(y);
								}
								continue;
							}
						}
						result.add(o);
					} catch (Exception ex) {
						new RuntimeException("Can't access getter: " + mName);
					}
				} else {
					result.add(toXonXD(x, nsStack));
				}
			}
		}
		return result;
	}

	/** Create map from attributes of an element.
	 * @param xc XComponent.
	 * @return object with XON.
	 */
	private static Map<String, Object> getXonAttrs(final XComponent xc) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		Class<?> cls = xc.getClass();
		Method[] methods = cls.getDeclaredMethods();
		for (Method m: methods) {
			String name = m.getName();
			if (name.startsWith("get")
				&& m.getParameterTypes().length == 0) {
				if (name.startsWith("get$")) {
					continue;
				}
				Object o = null;
				try {
					m.setAccessible(true);
					o = m.invoke(xc);
				} catch (Exception ex) {
					new RuntimeException("Can't access getter: " + m.getName());
				}
				if (!(o instanceof XComponent || o instanceof List
					|| o instanceof Map)) {
					result.put(XonTools.xmlToJName(name.substring(3)), o);
				}

			}
		}
		return result;
	}

	/** Create array from child nodes of an element.
	 * @param xc XComponent.
	 * @param nsStack namespace prefixes stack.
	 * @return object with XON.
	 */
	private static void getXonBody(final XComponent xc,
		final List<Object> body,
		final KNamespace nsStack){
		List<XComponent> components = xc.xGetNodeList();
		if (components != null) {
			int textIndex = 0;
			for (XComponent x : components) {
				if ("$text".equals(x.xGetNodeName())) {
					Class<?> cls = xc.getClass();
					String mName = "get$value";
					if (textIndex > 0) {
						mName += textIndex;
					}
					textIndex++;
					try {
						Method m = cls.getDeclaredMethod(mName);
						m.setAccessible(true);
						Object o = m.invoke(xc);
						if (o instanceof String) {
							o = toXonObject(o);
							if (o instanceof List && !((List) o).isEmpty()) {
								for (Object y: (List) o) {
									body.add(y);
								}
								continue;
							}
						}
						body.add(o);
					} catch (Exception ex) {
						new RuntimeException("Can't access getter: " + mName);
					}
				} else {
					body.add(toXonXD(x, nsStack));
				}
			}
		}
	}

	/** Create XON from XComponent (not XON/JSON).
	 * @param xc XComponent.
	 * @param nsStack namespace prefixes stack.
	 * @return object with XON.
	 */
	private static Object toXonXD(final XComponent xc,
		final KNamespace nsStack) {
		String ns = xc.xGetNamespaceURI();
		String name = xc.xGetNodeName();
		int ndx = name.indexOf(':');
		if (XON_NS_URI_XD.equals(ns)) {
			String localName = ndx >= 0 ? name.substring(ndx + 1) : name;
			if (X_MAP.equals(localName)) {
				return toXonMapXD(xc, nsStack);
			} else if (X_ARRAY.equals(localName)) {
				return toXonArrayXD(xc, nsStack);
			} else if (X_VALUE.equals(localName)) {
				return toXonItem(xc);
			}
		}
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		List<Object> body = new ArrayList<Object>();
		Map<String, Object> namedValues = getXonAttrs(xc);
		String prefix = null;
		nsStack.pushContext();
		if (ns != null) {
			prefix = ndx>0 ? name.substring(0,ndx) : "";
			if (!ns.equals(nsStack.getNamespaceURI(prefix))) {
				String nsattr = "xmlns"+(!prefix.isEmpty() ? ":" + prefix : "");
				namedValues.put(nsattr, ns);
				nsStack.setPrefix(prefix, ns);
			}
		}
		if (!namedValues.isEmpty()) {
			body.add(namedValues);
		}
		getXonBody(xc, body, nsStack);
		result.put(XonTools.xmlToJName(name), body);
		nsStack.popContext();
		return result;
	}

	/** Create XON object from X-component.
	 * @param xc X-component.
	 * @return XON object.
	 */
	public final static Object toXon(final XComponent xc) {
		String ns = xc.xGetNamespaceURI();
		if (XON_NS_URI_W.equals(ns)) {
			String localName = xc.xGetNodeName();
			int ndx = localName.indexOf(':');
			if (ndx >= 0) {
				localName = localName.substring(ndx + 1);
			}
			if (X_MAP.equals(localName)) {
				return toXonMap(xc);
			} else if (X_ARRAY.equals(localName)) {
				return toXonArray(xc);
			} else if (X_VALUE.equals(localName)) {
				return toXonItem(xc);
			}
		}
		return toXonXD(xc, new KNamespace());
	}

	/** Convert XML name to Java name.
	 * @param xmlName XML name to be converted.
	 * @return Java name created from XML name,
	 */
	public static final String xmlToJavaName(final String xmlName) {
		return "_".equals(xmlName) ? "$_" // Java 9 not allows indentifiers "_"
			: xmlName.replace(':','$').replace('-','_').replace('.','_');
	}
}