package org.xdef.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import static org.xdef.XDConstants.XON_NS_PREFIX;
import static org.xdef.XDConstants.XON_NS_URI_W;
import static org.xdef.XDConstants.XON_NS_URI_XD;
import org.xdef.XDContainer;
import org.xdef.XDDocument;
import org.xdef.XDParseResult;
import org.xdef.XDPool;
import org.xdef.XDValue;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefJNull;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefString;
import org.xdef.impl.xml.KNamespace;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMELEMENT;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonNames.X_ARRAY;
import static org.xdef.xon.XonNames.X_KEYATTR;
import static org.xdef.xon.XonNames.X_MAP;
import static org.xdef.xon.XonNames.X_VALATTR;
import static org.xdef.xon.XonNames.X_VALUE;
import org.xdef.xon.XonTools;

/** Utilities used with XComponents.
 * @author Vaclav Trojan
 */
@SuppressWarnings("unchecked")
public class XComponentUtil {

	/** Get value of variable of given name from XComponent.
	 * @param xc XComponent.
	 * @param name name of variable.
	 * @return object with value of variable.
	 * @throws Exception if variable not exists.
	 */
	public static final Object getVariable(final XComponent xc, final String name) throws Exception{
		Class<?> clazz = xc.getClass();
		final Method method = clazz.getDeclaredMethod("get" + name);
		method.setAccessible(true);
		return method.invoke(xc);
	}

	/** Set value of variable of given name from XComponent.
	 * @param xc XComponent.
	 * @param name name of variable.
	 * @param value value of variable.
	 * @throws Exception if variable not exists.
	 */
	public static final void setVariable(final XComponent xc, final String name, final Object value)
		throws Exception {
		final Class<?> clazz = xc.getClass();
		Method method = clazz.getDeclaredMethod("get" + name);
		method.setAccessible(true);
		Class<?> result = method.getReturnType();
		method = clazz.getDeclaredMethod("set" + name, result);
		method.setAccessible(true);
		method.invoke(xc, value);
	}

	/** Create XComponent from XComponent according to given model in XDPool.
	 * @param xc XComponent.
	 * @param xp XDPool.
	 * @param xdPos XDPosition of model in XDPool.
	 * @return created XComponent.
	 */
	public static final XComponent toXComponent(final XComponent xc, final XDPool xp,final String xdPos) {
		XMNode xm = xp.findModel(xdPos);
		if (xm.getKind() != XMELEMENT) {
			throw new SRuntimeException(XDEF.XDEF372, xm.getXDPosition()); //Argument is not model of element: &{0}
		}
		Element el = toXml(xc, (XMElement) xm);
		XDDocument xd = xp.createXDDocument(xdPos);
		return xd.xparseXComponent(el, null, null);
	}

	/** Create the new XML element from XComponent according to model.
	 * @param xc XComponent.
	 * @param xm model according which element will be constructed.
	 * @return XML element created from this object according to given model.
	 */
	public static final Element toXml(final XComponent xc, final XMElement xm) {
		XDDocument xd = xm.createXDDocument();
		xd.setXDContext(xc.toXml());
		return xd.xcreate(xm.getQName(), null);
	}

	/** Create the new XML element from XComponent according to model.
	 * @param xc XComponent.
	 * @param xd XDDocument for creation on new Element.
	 * @param model name of model to be created.
	 * @return new Element.
	 */
	public static final Element toXml(final XComponent xc, final XDDocument xd, final String model) {
		xd.setXDContext(xc.toXml());
		return xd.xcreate(model, null);
	}

	/** Create XML element from XComponent according to given model from XDPool.
	 * @param xc XComponent.
	 * @param xp XDPool.
	 * @param xdPos the XDPosition of model in XDPool.
	 * @return created Element.
	 */
	public static final Element toXml(final XComponent xc, final XDPool xp, final String xdPos) {
		XMNode xm = xp.findModel(xdPos);
		if (xm.getKind() != XMELEMENT) {
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
	public static XComponent toXComponent(final XComponent xc, final XMElement xm) {
		XDDocument xd = xm.createXDDocument();
		xd.setXDContext(xc.toXml());
		return xd.xparseXComponent(xd.xcreate(xm.getQName(), null), null, null);
	}

	/** Create XComponent from XComponent according to model.
	 * @param xc XComponent.
	 * @param xd XDDocument for creation on new Element.
	 * @param model name of model to be created.
	 * @return new XComponent.
	 */
	public static final XComponent toXComponent(final XComponent xc,
		final XDDocument xd,
		final String model) {
		xd.setXDContext(xc.toXml());
		Element el = xd.xcreate(model, null);
		XDDocument xd1 = xd.getXDPool().createXDDocument(model);
		return xd1.xparseXComponent(el, null, null);
	}

	/** Add XComponent to the child list
	 * @param childList child list.
	 * @param xc XComponent.
	 */
	public static final void addXC(final List<XComponent> childList, final XComponent xc) {
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
		final List xc) {
		for (int i = 0; i < xc.size(); i++) {
			Object y;
			if ((y =  xc.get(i)) != null) {
				addXC(childList, (XComponent) y);
			}
		}
	}

	/** Add text to child list
	 * @param parent parent XComponent
	 * @param xdPos XDPosition
	 * @param xc XComponent.
	 * @param x object from which text will be created/
	 * @param ndx index where to add.
	 */
	public static final void addText(final XComponent parent,
		final String xdPos,
		final List<XComponent> xc,
		final Object x,
		final int ndx) {
		if (x != null) {
			String s = x.toString();
			if (s.length() > 0){
				int xp = 1;
				for (XComponent c : xc) {
					if ("$text".equals(c.xGetNodeName())) {
						xp++;
					}
				}
				final String xpos = parent.xGetXPos() + "/$text[" + xp + "]";
				addXC(xc, new XCTextComponent(x.toString(), xdPos, xpos, ndx));
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
	private static void updateXPos(final XComponent xc, final String xpos, final int index) {
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
	public static final void updateXPos(final XComponent xc) {updateXPos(xc, "", 0);}

	/** Convert parsed value with XDConnpainer to java.util.List.
	 * @param value parsed value.
	 * @param typeId separator of items.
	 * @return converted list.
	 */
	public static final List valueToList(final XDParseResult value, final int typeId) {
		if (value == null) {
			return null;
		}
		return valueToList((XDContainer) value.getParsedValue(), typeId);
	}

	/** Convert XDContainer to the java.util.List.
	 * @param  c Container value.
	 * @param typeId type of items..
	 * @return converted list.
	 */
	public static final List valueToList(final XDContainer c, final int typeId) {
		int len = c.getXDItemsNumber();
		List result = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			Object o = c.getXDItem(i).getObject();
			if (o instanceof XDContainer) {
				o = valueToList((XDContainer) o, typeId);
			}
			result.add(o);
		}
		return result;
	}

	/** Create list of JSON/XON objects from the list of binary objects.
	 * @param lst list of binary objects.
	 * @return created list of JSON/XON objects.
	 */
	public static final List listToJlist(final List lst) {
		List result = new ArrayList<>();
		for (Object o: lst) {
			if (o == null) {
				result.add(new DefJNull());
			} else if (o instanceof String) {
				String s = XonTools.jstringToSource((String) o);
				if ("false".equals(s) || "true".equals(s) || "null".equals(s) || s.isEmpty()
					|| s.indexOf('\\')>=0 || s.indexOf(' ')>=0 || new StringParser(s).isSignedInteger()) {
					s = '"' + XonTools.jstringToSource(s) + '"';
				}
				result.add(new DefString(s));
			} else if (o instanceof Number) {
				result.add(o instanceof BigDecimal ? new DefDecimal((BigDecimal)o)
					: o instanceof Double ? new DefDouble(((Number)o).doubleValue())
					: new DefLong(((Number)o).longValue()));
			} else {
				result.add(o instanceof Boolean ? new DefBoolean((Boolean)o)
					: o instanceof List ? listToJlist((List) o) : o);
			}
		}
		return result;
	}

	/** Create java.util.List from list with parsed XDValues.
	 * @param jlst list with XDValues.
	 * @return list wirh binary objects.
	 */
	public static final List jlistToList(final List jlst) {
		List result = new ArrayList<>();
		for (Object o: jlst) {
			if (o instanceof DefString) {
				String s = o.toString();
				if (s.startsWith("\"") && s.endsWith("\"")) {
					StringParser p = new StringParser(s);
					p.setIndex(1);
					s = XonTools.readJString(p);
				}
				result.add(s);
			} else {
				result.add(o instanceof DefJNull ? null
					: o instanceof List ? jlistToList((List) o)
					: o instanceof XDValue ? ((XDValue) o).getObject() : o);
			}
		}
		return result;
	}

	/** Add XDValue from argument to the list. If the argument is XDContainer add items to the list.
	 * @param lst list where to add items.
	 * @param value X-definition value.
	 */
	private static void parseResultToList(final List lst, final XDValue value) {
		if (value instanceof org.xdef.XDContainer) {
			XDContainer x = (XDContainer) value;
			List y = new ArrayList<>();
			for (int i = 0; i < x.getXDItemsNumber(); i++) {
				parseResultToList(y, x.getXDItem(i));
			}
			lst.add(y);
		} else {
			lst.add(value);
		}
	}

	/** Create Java.util.List and add to it items from parsed result.
	 * @param val parsed result.
	 * @return created List with items from argument val.
	 */
	public static final List parseResultToList(final XDParseResult val) {
		List result = new ArrayList<>();
		org.xdef.XDContainer x = (org.xdef.XDContainer) val.getParsedValue();
		for (int i = 0; i < x.getXDItemsNumber(); i++) {
			parseResultToList(result, x.getXDItem(i));
		}
		return jlistToList(result);
	}

	/** Create source list of ID of IDREFS od CHKIDS.
	 * @param list pasrsed list of ID.
	 * @return source form of list of ID.
	 */
	public static final String idsToString(final List list) {
		if (list == null) return "null";
		StringBuilder sb = new StringBuilder();
		boolean wasFirst = false;
		for (Object o: list) {
			if (wasFirst) {
				sb.append(" ");
			}
			wasFirst = true;
			String s = (o == null) ? "" : o.toString();
			if (s.contains(" ") || s.contains("'")) {
				s = s.replaceAll("'", "''");
				s = '\'' + s + '\'';
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/** Create source list of items with separators (value of parsed list).
	 * @param list pasrsed list.
	 * @param isJlist if true generate jlist format, otherwise just list.
	 * @return list of items with separatort.
	 */
	public static final String listToString(final List list, final boolean isJlist) {
		if (list == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder(isJlist ? "[" : "");
		boolean wasFirst = false;
		for (Object o: list) {
			if (wasFirst) {
				sb.append(",");
			} else {
				wasFirst = true;
			}
			if (o == null) {
				sb.append("null");
			} else if (o instanceof XDContainer) {
				return containerToJlist((XDContainer) o);
			} else if (o instanceof List) {
				sb.append(listToString((List) o, true));
			} else if (o instanceof String) {
				String s = (String) o;
				if ("false".equals(s) || "true".equals(s) || "null".equals(s) || s.isEmpty()
					|| s.indexOf('\\') >= 0 || s.indexOf(' ') >= 0 || s.indexOf('\t') >= 0
					|| s.indexOf('\n') >=0 || s.indexOf('"') >= 0 || new StringParser(s).isSignedInteger()) {
					s = '"' + XonTools.jstringToSource(s) + '"';
				}
				sb.append(s);
			} else {
				sb.append(o.toString());
			}
		}
		if (isJlist) {
			sb.append("]");
		}
		return sb.toString();
	}

	/** Convert XDContainer to jlist string.
	 * @param c XDContainer to be converted.
	 * @return string with values of the container from argument.
	 */
	public static final String containerToJlist(final XDContainer c) {
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
				switch (y.getItemId()) {
					case XD_STRING:
						sb.append(y.toString());
						break;
					case XD_CONTAINER:
						sb.append(containerToJlist((XDContainer) y));
						break;
					default:
						sb.append(y.toString());
				}
			}
		}
		return sb.append(" ]").toString();
	}

	/** Convert XML name to Java name.
	 * @param xmlName XML name to be converted.
	 * @return Java name created from XML name,
	 */
	public static final String xmlToJavaName(final String xmlName) {
		// Java 9 not allows indentifiers "_" ???
		return "_".equals(xmlName) ? "$_" : xmlName.replace(':', '$').replace('-', '_').replace('.', '_');
	}

////////////////////////////////////////////////////////////////////////////////
// Create XON object from X-component.
////////////////////////////////////////////////////////////////////////////////
	private static Object toXonObject(final Object o) {
		return o instanceof String? XonTools.xmlToJValue((String) o) : o instanceof XonTools.JNull? null : o;
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
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
			| InvocationTargetException ex) {
			throw new RuntimeException("Can't access value", ex);
		}
	}

	/** Create XON array from XComponent.
	 * @param xc XComponent
	 * @return object with XON array.
	 */
	public static final List<Object> toXonArray(final XComponent xc) {
		List<Object> result = new ArrayList<>();
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

	/** Convert argument with date to the ISO8601 fromat.
	 * @param x date to be converted/
	 * @return date in the ISO8601 fromat.
	 */
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
				String key;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					Method m = o.getClass().getDeclaredMethod("get" + X_KEYATTR);
					m.setAccessible(true);
					key = XonTools.xmlToJName((String) m.invoke(o));
					if (o instanceof XComponent) {
						result.put(key, ((XComponent) o).toXon());
					} else {
						result.put(key, o);
					}
				} catch (IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException | SecurityException | InvocationTargetException ex) {}
			} else if (methodName.startsWith("listOf$")) {
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					String key = methodName.substring(7);
					result.put(key, o);
					break;
				} catch (IllegalAccessException | IllegalArgumentException
					| SecurityException | InvocationTargetException ex) {}
			} else if (x.getParameterTypes().length == 0
				&& methodName.startsWith("listOf" + XON_NS_PREFIX + "$")) {
				String key;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
					if (o == null) {
						continue;
					}
					if (o instanceof XComponent) {
						Method m = o.getClass().getDeclaredMethod("get" + X_KEYATTR);
						m.setAccessible(true);
						key = XonTools.xmlToJName((String) m.invoke(o));
						result.put(key, ((XComponent) o).toXon());
					} else {
						if (o instanceof List) {
							for(Object oo : (List)o) {
								if (oo instanceof XComponent) {
									Method m = oo.getClass().getDeclaredMethod("get" + X_KEYATTR);
									m.setAccessible(true);
									key = XonTools.xmlToJName((String) m.invoke(oo));
									result.put(key, ((XComponent) oo).toXon());
								}
							}
						}
					}
				} catch (IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException | SecurityException | InvocationTargetException ex) {}
			}
		}
	}

	/** Create XON map from XComponent.
	 * @param xc XComponent
	 * @return object with XON map.
	 */
	public static final Map<String, Object> toXonMap(final XComponent xc) {
		Map<String, Object> result = new LinkedHashMap<>();
		Class<?> cls = xc.getClass();
		toXonMap(xc, cls.getDeclaredMethods(), result);
		return result;
	}

	/** Create XON map from XComponent.
	 * @param xc XComponent
	 * @param nsStack namespace prefixes stack.
	 * @return object with XON map.
	 */
	private static Map<String, Object> toXonMapXD(final XComponent xc, final KNamespace nsStack) {
		Class<?> cls = xc.getClass();
		Method[] methods = cls.getDeclaredMethods();
		Map<String, Object> result = getXonAttrs(xc);
		for (Method x: methods) {
			String name = x.getName();
			if (name.startsWith("get")&&!name.startsWith("get$")&&x.getParameterTypes().length==0) {
				Object o = null;
				try {
					x.setAccessible(true);
					o = x.invoke(xc);
				} catch (IllegalAccessException | IllegalArgumentException
					| SecurityException | InvocationTargetException ex) {
					throw new RuntimeException("Can't access getter: " + x.getName());
				}
				if (o instanceof XComponent) {
					XComponent y = (XComponent) o;
					String key = null;
					if (XON_NS_URI_XD.equals(y.xGetNamespaceURI())){
						try {
							Class<?> cls1 = o.getClass();
							Method m = cls1.getDeclaredMethod("get"+X_KEYATTR);
							m.setAccessible(true);
							key = XonTools.xmlToJName((String) m.invoke(o));
						} catch (IllegalAccessException | IllegalArgumentException
							| NoSuchMethodException	| SecurityException
							| InvocationTargetException ex){
							throw new RuntimeException("Not key", ex);
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
					throw new RuntimeException("Not XComponent: " + o);
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
	private static List toXonArrayXD(final XComponent xc, final KNamespace nsStack) {
		List result = new ArrayList<>();
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
						Method m = cls.getDeclaredMethod(mName); // this method may not exist
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
					} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
						| SecurityException | InvocationTargetException ex) {} // ignore it
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
		Map<String, Object> result = new LinkedHashMap<>();
		Class<?> cls = xc.getClass();
		Method[] methods = cls.getDeclaredMethods();
		for (Method m: methods) {
			String name = m.getName();
			if (name.startsWith("get") && m.getParameterTypes().length == 0) {
				if (name.startsWith("get$")) {
					continue;
				}
				Object o = null;
				try {
					m.setAccessible(true);
					o = m.invoke(xc);
				} catch (IllegalAccessException | IllegalArgumentException
					| SecurityException | InvocationTargetException ex) {
					throw new RuntimeException("Can't access getter: " + m.getName());
				}
				if (!(o instanceof XComponent || o instanceof List || o instanceof Map)) {
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
		final List body,
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
						Method m = cls.getDeclaredMethod(mName); // this method may not exist
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
					} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
						| SecurityException | InvocationTargetException ex) {} // ignore it
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
	private static Object toXonXD(final XComponent xc, final KNamespace nsStack) {
		String ns = xc.xGetNamespaceURI();
		String name = xc.xGetNodeName();
		int ndx = name.indexOf(':');
		if (XON_NS_URI_XD.equals(ns)) {
			String localName = ndx >= 0 ? name.substring(ndx + 1) : name;
			switch (localName) {
				case X_MAP: return toXonMapXD(xc, nsStack);
				case X_ARRAY: return toXonArrayXD(xc, nsStack);
				case X_VALUE: return toXonItem(xc);
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		List body = new ArrayList<>();
		Map<String, Object> namedValues = getXonAttrs(xc);
		String prefix;
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
	public static final Object toXon(final XComponent xc) {
		String ns = xc.xGetNamespaceURI();
		if (XON_NS_URI_W.equals(ns)) {
			String localName = xc.xGetNodeName();
			int ndx = localName.indexOf(':');
			if (ndx >= 0) {
				localName = localName.substring(ndx + 1);
			}
			switch (localName) {
				case X_MAP: return toXonMap(xc);
				case X_ARRAY: return toXonArray(xc);
				case X_VALUE: return toXonItem(xc);
			}
		}
		return toXonXD(xc, new KNamespace());
	}

	/** Invoke the method with name from argument from XComponent and return the value from method.
	 * @param xc where to find getter.
	 * @param name name of getter.
	 * @return value returned by getter.
	 */
	public static final Object getx(final XComponent xc, final String name) {
		Class<?> cls = xc.getClass();
		for (;;) {
			try {
				Method m = cls.getDeclaredMethod(name);
				m.setAccessible(true);
				return m.invoke(xc);
			} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException
				| SecurityException | InvocationTargetException ex) {
				if ((cls = cls.getSuperclass()) == null) {
					break;
				}
			}
		}
		//Getter &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS104, name, xc.getClass().getName());
	}

	/** Return value returned by getter from XComponent with name from argument.
	 * @param xc must be XComponent.
	 * @param name name of getter.
	 * @return value returned by getter.
	 */
	public static final Object get(final XComponent xc, final String name) {return getx(xc, "get"+name);}

	/** Return list returned by listOf getter from XComponent with name from argument.
	 * @param xc must be XComponent.
	 * @param name name of listOf getter.
	 * @return list returned by getter.
	 */
	public static final List listOf(final XComponent xc, final String name){
		return (List) getx(xc, "listOf"+name);
	}

	/** Get value of XComponent as the java.util.Map.
	 * @param xc the XComponent.
	 * @return value of XComponent as the java.util.Map..
	 */
	public static final Map getMap(final XComponent xc) {return (Map) getx(xc, "anyItem$");}

	/** Invoke method with name and parameter from argument from XComponent (typically a setter).
	 * @param xc XComponent where to fine method.
	 * @param name name of method.
	 * @param v value of parameter.
	 */
	public static final void setx(final XComponent xc, final String name, final Object v) {
		Class<?> cls = xc.getClass();
		for (;;) {
			for (Method m: cls.getDeclaredMethods()) {
				Class<?>[] params = m.getParameterTypes();
				if ((name).equals(m.getName()) && params!=null && params.length==1) {
					try {
						m.setAccessible(true);
						m.invoke(xc, v);
						return;
					} catch (IllegalAccessException | IllegalArgumentException | SecurityException
						| InvocationTargetException ex) {
					}
				}
			}
			if ((cls = cls.getSuperclass()) == null) {
				break;
			}
		}
		//Setter &{0} not found in class &{1}
		throw new SRuntimeException(SYS.SYS105, name, xc.getClass().getName());
	}

	/** Invoke setter from XComponent with given value.
	 * @param xc must be XComponent.
	 * @param name name of setter.
	 * @param v value to be set.
	 */
	public static final void set(final XComponent xc, final String name, final Object v) {
		setx(xc, "set"+name, v);
	}

	/** Add value from argument to the list of given name from XComponent.
	 * @param xc XComponent with the list.
	 * @param name name of list in the XComponent.
	 * @param v value to be added.
	 */
	public static final void add(final XComponent xc, final String name, final Object v) {
		setx(xc, "add"+name, v);
	}
}
