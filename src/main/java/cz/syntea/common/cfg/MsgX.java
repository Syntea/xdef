/*
 * Copyright 2007 Syntea software group a.s.
 *
 * File: MsgX.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.common.cfg;

import cz.syntea.xdef.msg.SYS;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.SThrowable;
import cz.syntea.xdef.xml.KEmptyNodeList;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.XXElement;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.model.XMNode;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/** Implementation of the interface to the Syntea projects. NOTE: because Syntea
 * projects doesn't use XML namespace features this class doesn't provide
 * any kind of namespace support.
 * <p>This class doesn't provide any kind of namespace support and there are
 * other limitations connected with SYNTEA projects. The user can use those
 * methods AS IT IS without any kind of warranty.
 * The class MsgX implements two different kinds of object which
 * are distinguished by the constructor's parameter <i>mode</i>: </p>
 *
 * <p>1. <i>mode</i> = "r": MsgX used for parsing XML source strings. The string
 * given by constructor parameter <i>msg</i> is first parsed and checked
 * according to given XML definition. After the object was created there are
 * available individual components of the parsed result (the DOM object).</p>
 *
 * <p>2. <i>mode</i> = "w": MsgX used for composing XML structure. The parameter
 * <i>msg</i> of constructor specifies the name of message and constructor looks
 * for corresponding XML definition. After the object was created there is
 * possible to compose a XML structure. After all parts of XML has been
 * constructed it is possible to check the correctness of created result and to
 * convert it into the string in the XML format.</p>
 * <p> The "message" is represented as a XML source string. In the
 * "read" mode the object is first validated and the program throws
 * the exception if the object doesn't pass to the given X-definition. In
 * the "write" mode the message is created with number of various methods
 * and the message can be retrieved with "getMsg" method.</p>
 *
 * <p><b>Example:</b></p>
 * <pre><tt>
 *     File[] file;
 *     String name;
 *     ...
 *     XArchive xar = new DefPool(files);
 *     X-definition xdef = xar.getDefinition(name);
 *     ...
 *     try {
 *         Msgx xmsg = new MsgX("MyMsg", xdef, MsgX.MODE_WRITE);
 *         xmsg.putAttribute("Program","TEST");
 *         xmsg.putAttribute("Description","This is test");
 *         String msg = xmsg.getMsg();
 *         //result: &lt;MyMsg Program="TEST" Description= "This is test" /&gt;
 *         ...
 *         xmsg = new MsgX(msg, xdef, MsgX.MODE_READ);
 *         String program = xmsg.getAttribute("Program");//TEST
 *         String description = xmsg.getAttribute("Description"); //This is test
 *     } catch (SRuntimeException ex) {
 *         System.err.println("Error " + ex);
 *     }
 * </tt></pre>
 * @author Vaclav Trojan
 */
public class MsgX {

	/** MODE_READ The parsing mode. String given by constructor is first parsed
	 * according to the given XML definition and prepares access to parsed data.
	 */
	public static final String MODE_READ = "r";
	/** MODE_WRITE The constructing mode. The MsgX object is created for "write"
	 * (construction) of XML object according to given definition. The name
	 * of root element of resulting message is given by parameter <i>msg</i>.*/
	public static final String MODE_WRITE = "w";
	/** root XXElement */
	private final XXElement _rootXXElement;
	/** XDDocument. */
	private final XDDocument _chkdoc;
	/** Tag name of the root element of the message. */
	private final String _msgName;
	/** Flag if created object was checked. */
	private boolean _checked;
	/** Message parsed to DOM element. */
	private Element _rootElement;
	/** Actual XXElement in create mode. */
	private XXElement _actXXElement;

	/** Create new MsgX object. Parameter <i>msg</i> has different meaning
	 * according to value of parameter <i>mode</i>: in the case of "composition"
	 * (<i>mode="w"</i>) it contains the name of message (tag name of root
	 * element); in the case of "read" (<i>mode="r"</i>) it contain the source
	 * string of message to be parsed.
	 * @param msg The name of definition or of the source message.
	 * @param xpool XDPool.
	 * @param xname name of X-definition or <tt>null</tt>.
	 * @param mode The mode: "r" for parsing, "w" for composing.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public MsgX(final String msg,
		final XDPool xpool,
		final String xname,
		final String mode) {
		this(msg, xpool, xname, mode, null, null, false);
	}

	/** Create new MsgX object. Parameter <i>msg</i> has different meaning
	 * according to value of parameter <i>mode</i>: in the case of "composition"
	 * (<i>mode="w"</i>) it contains the name of message (tag name of root
	 * element); in the case of "read" (<i>mode="r"</i>) it contain the source
	 * string of message to be parsed.
	 * @param msg The name of definition or of the source message.
	 * @param xpool XDPool.
	 * @param xname name of X-definition or <tt>null</tt>.
	 * @param mode The mode: "r" for parsing, "w" for composing.
	 * @param os output writer or <tt>null</tt>.
	 * @param encoding encoding of output stream or <tt>null</tt>.
	 * @param writeHdr if XML header should be written to the output stram.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public MsgX(final String msg,
		final XDPool xpool,
		final String xname,
		final String mode,
		Writer os,
		String encoding,
		boolean writeHdr) {
		if (!xpool.exists(xname)) {
			//X-definition not found
			throw new SRuntimeException("MSGX011",
				"X-definition for message '&{0}' is missing", xname);
		}
		if (mode.equals(MODE_READ)) {
			_actXXElement = null; //not used in read mode
			_checked = true; //in the read mode always true
			_chkdoc = xpool.createXDDocument(xname);
			Document doc = _chkdoc.getDocument();
			if (os != null) {
				try {
					_chkdoc.setStreamWriter(os, encoding, writeHdr);
				} catch (Exception ex) {
					if (ex instanceof SThrowable) {
						throw new SRuntimeException(
							((SThrowable) ex).getReport());
					}
					//Program exception&{0}{: }
					throw new SRuntimeException(SYS.SYS036, ex);
				}
			}
			ArrayReporter reporter = new ArrayReporter();
			_chkdoc.xparse(msg, reporter);
			if (reporter.errors()) {
				String s = _chkdoc != null && _chkdoc.getElement() != null ?
					_chkdoc.getElement().getNodeName() : xname;
				throw new SRuntimeException("MSGX012",
					"Errors detected while reading message '&{0}'&{1}{; }",
					s,
					reporter.printToString());
			}
			_actXXElement = _rootXXElement = _chkdoc.getRootXXElement();
			if (_chkdoc.getXXElement() == null) {
				//nenasla se definice
				throw new SRuntimeException("MSGX013",
					"Message '&{0}' is not defined", xname);
			}
			_rootElement = _chkdoc.getElement();
			_msgName = _rootElement.getTagName();
		} else if (mode.equals(MODE_WRITE)) {
			_rootElement = null; //in write mode null until check is called
			_checked = false; //in write mode set to false until check is called
			_msgName = msg;
			_chkdoc = xpool.createXDDocument(xname);
			if (os != null) {
				try {
					_chkdoc.setStreamWriter(os, encoding, writeHdr);
				} catch (Exception ex) {
					if (ex instanceof SThrowable) {
						throw new SRuntimeException(
							((SThrowable) ex).getReport());
					}
					//Program exception&{0}{: }
					throw new SRuntimeException(SYS.SYS036, ex);
				}
			}
			Document doc = _chkdoc.getDocument();
			//now create the root element
			_actXXElement = _rootXXElement =
				_chkdoc.prepareRootXXElement(_msgName, true);
			if (_rootXXElement.errors()) {
				_chkdoc.getReporter().checkAndThrowErrorWarnings();
				throw new SRuntimeException("MSGX014", "Errors detected"
					+ " while creating message '&{0}'&{1}{; }",
					_msgName, _rootXXElement.getReporter().printToString());
			}
			if (_chkdoc.getXXElement() == null) {
				throw new SRuntimeException("MSGX013",
					"Message '&{0}' is not defined", _msgName);
			}
		} else {
			throw new SRuntimeException("MSGX015",
				"Invalid parameter mode: '&{0}'", mode);
		}
	}

	/** Create new MsgX object from given org.w3c.dom.Element.
	 * @param el The element with source message.
	 * @param xpool XDPool.
	 * @param xname name of X-definition or <tt>null</tt>.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public MsgX(final Element el,
		final XDPool xpool,
		final String xname) {
		this(el, xpool, xname, null, null, false);
	}

	/** Create new MsgX object from given org.w3c.dom.Element.
	 * @param el The element with source message.
	 * @param xpool XDPool.
	 * @param xname name of X-definition or <tt>null</tt>.
	 * @param out output writer or <tt>null</tt>.
	 * @param encoding encoding of output stream or <tt>null</tt>.
	 * @param writeHdr if XML header should be written to the output stram.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public MsgX(final Element el,
		final XDPool xpool,
		final String xname,
		Writer out,
		String encoding,
		boolean writeHdr) {
		if (!xpool.exists(xname)) {
			//nenasla se definice
			throw new SRuntimeException("MSGX011",
				"X-definition for message '&{0}' is missing", xname);
		}
		_actXXElement = null; //not used in read mode
		_checked = true; //in the read mode always true
		ArrayReporter reporter = new ArrayReporter();
		_chkdoc = xpool.createXDDocument(xname);
		if (out != null) {
			try {
				_chkdoc.setStreamWriter(out, encoding, writeHdr);
			} catch (Exception ex) {
				if (ex instanceof SThrowable) {
					throw new SRuntimeException(((SThrowable) ex).getReport());
				}
				//Program exception&{0}{: }
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		}
		_chkdoc.xparse(el, reporter);
		if (reporter.errors()) {
			String s = _chkdoc != null && _chkdoc.getElement() != null ?
				_chkdoc.getElement().getNodeName() : xname;
			throw new SRuntimeException("MSGX012",
				"Errors detected while reading message '&{0}'&{1}{; }",
				s, reporter.printToString());
		}
		_actXXElement = _rootXXElement = _chkdoc.getRootXXElement();
		if (_chkdoc.getXXElement() == null) {
			//nenasla se definice
			throw new SRuntimeException("MSGX013",
				"Message '&{0}' is not defined", xname);
		}
		_rootElement = _chkdoc.getElement();
		_msgName = _rootElement.getTagName();
	}

	/** Create new empty element from actual Document object.
	 * @param tagName The name of element.
	 * @return The created element.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public Element createElement(final String tagName) {
		return _chkdoc.getDocument().createElementNS(null, tagName);
	}

	/** Get name of message (the tag name of root element).
	 * @return The message name.
	 */
	public String getMsgName() {return _msgName;}

	/** Get attribute from the root element.
	 * @param name The name of attribute.
	 * @return The value of attribute or the empty string if the value
	 * doesn't exist.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public String getRootAttribute(final String name) {
		String sname = name.charAt(0) == '@' ? name.substring(1) : name;
		if (_rootXXElement.checkAttributeLegal(sname)) {
			Element el = _rootXXElement.getElement();
			String result = el.getAttribute(sname);
			return result.length() > 0 ? result :
				el.hasAttribute(sname) ? "" : null;
		}
		//Attempt to get undeclared item
		throw new SRuntimeException(XDEF.XDEF581,
			"&{xpath}" + '/' + _rootXXElement.getElement().getTagName() +
			"/@" + sname);
	}

	/** Get Map with all attributes from root element.
	 * @return  Map with attributes.
	 */
	public Map<String, String> getRootAttributes() {
		NamedNodeMap nm = _rootElement.getAttributes();
		Map<String, String> msgData = new TreeMap<String, String>();
		for (int i = nm.getLength() - 1; i >= 0; i--) {
			Attr attr = (Attr) nm.item(i);
			msgData.put(attr.getName(), attr.getValue());
		}
		return msgData;
	}

	/** Get attribute from actual element.
	 * @param name The name of attribute.
	 * @return The value of attribute or the empty string if the value
	 * doesn't exist.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public String getAttribute(final String name) {
		return name.indexOf('@') < 0 ?
			getDefinedAttribute(_actXXElement, name) :
			(String) getValue(name); //the path
	}

	/** Add element to root.
	 * @param element The Element to be added.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void addElementToRoot(final Element element) {
		addElement(_rootXXElement, element);
	}

	/** Add element to XXElement base.
	 * @param xdElem The XXElement base.
	 * @param element The Element to be added.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	private void addElement(final XXElement xdElem,
		final Element element) {
		if (element == null) {
			return;
		}
		if (_checked) {
			throw new SRuntimeException("MSGX023",
				"Attempt to change already checked message");
		}
		XXElement ins = xdElem.prepareXXElementNS(
			element.getNamespaceURI(), element.getTagName());
		NamedNodeMap nm = element.getAttributes();
		for (int i = 0; i < nm.getLength(); i++) {
			Attr attr = (Attr) nm.item(i);
			ins.addAttribute(attr.getName(), attr.getValue());
		}
		if (ins.errors()) {
			ReportWriter reporter =
				ins.getReporter().getReportWriter();
			if (reporter != null) {
				reporter.error("MSGX016",
				  "Error when iserting element '&{0}' into '&{1}'&{2}{; }",
				  element.getTagName(), xdElem.getXXName());
				reporter.checkAndThrowErrors();
			} else {// reporter is not available
				throw new SRuntimeException("MSGX016",
				  "Error when iserting element '&{0}' into '&{1}'&{2}{; }",
				  element.getTagName(), xdElem.getXXName());
			}
		}
		if (!ins.checkElement()) {
			throw new SRuntimeException("MSGX016",
			  "Error when iserting element '&{0}' into '&{1}'&{2}{; }",
			  element.getTagName(), xdElem.getXXName(),
			  ins.getReporter().printToString());
		}
		NodeList nl = element.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				addElement(ins, (Element) nl.item(i));
			} else if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
				if (!ins.addText(((Text) nl.item(i)).getNodeValue())) {
					throw new SRuntimeException("MSGX016",
					  "Error when iserting element '&{0}' into '&{1}'&{2}{; }",
					  element.getTagName(), xdElem.getXXName(),
					  ins.getReporter().printToString());
				}
			}
		}
		if (ins.addElement()) {
			return;
		}
		throw new SRuntimeException("MSGX016",
			  "Error when iserting element '&{0}' into '&{1}'&{2}{; }",
			element.getTagName(), xdElem.getXXName(),
			ins.getReporter().printToString());
	}

	/** Put items from Map as attributes to current element.
	 * @param data Map with attributes.
	 */
	public void putAttributes(final Map<?,?> data) {
		for (Iterator<?> e = data.keySet().iterator(); e.hasNext();) {
			String key = (String) e.next();
			putAttribute(key, (String) data.get(key));
		}
	}

	/** Add attribute to root element.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void putRootAttribute(final String name, final String value) {
		if (value == null) {
			return;
		}
		if (!_rootXXElement.addAttribute(name, value)) {
			if (_chkdoc.errors()) {
				_chkdoc.getReporter().checkAndThrowErrorWarnings();
			}
			throw new SRuntimeException("MSGX017",
				"Error when inserting attribute '&{0}'&{1}{; }",
				_rootXXElement.getXPos() + "/@" + name);
		}
	}

	/** Put items from Map as attributes to root element.
	 * @param data Map with attributes.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void putRootAttributes(final Map<?,?> data) {
		for (Iterator<?> e = data.keySet().iterator(); e.hasNext();) {
			String key = (String) e.next();
			putRootAttribute(key, (String) data.get(key));
		}
	}

	/** Put attribute to current element.
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void putAttribute(final String name, final String value) {
		if (value == null) {
			return;
		}
		if (!_actXXElement.addAttribute(name, value)) {
			if (_chkdoc.errors()) {
				_chkdoc.getReporter().checkAndThrowErrorWarnings();
			}
			throw new SRuntimeException("MSGX017",
				"Error when iserting attribute '&{0}'&{1}{; }",
				_rootXXElement.getXPos() + "/@" + name);
		}
	}

	/** Check constructed element if it corresponds to the definition.
	 * @throws SRuntimeException if constructed element doesn't correspond to
	 * XML definition.
	 */
	private void check() {
		if (_checked) {
			return;
		}
		_checked = true;
		boolean result = _rootXXElement.checkElement();
		result &= _rootXXElement.addElement();
		if (_chkdoc.errors()) {
			_chkdoc.getReporter().checkAndThrowErrorWarnings();
		}
		if (!result) {
			throw new SRuntimeException("MSGX018",
				"Message is incorrect&{0}{; }",
				_rootXXElement.getReporter().printToString());
		}
		_rootElement = _rootXXElement.getElement();
	}

	/** Check message attributes.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	private void checkAttributes() {
		_rootXXElement.checkElement();
		if (_chkdoc.errors()) {
			_chkdoc.getReporter().checkAndThrowErrorWarnings();
		}
		if (!_actXXElement.checkElement()) {
			if (_chkdoc.errors()) {
				_chkdoc.getReporter().checkAndThrowErrorWarnings();
			}
			throw new SRuntimeException("MSGX018",
				"Message is incorrect&{0}{; }",
				_actXXElement.getReporter().printToString());
		}
	}

	/** Create the new element as child of the current one. The actual element
	 * is set as parent of the new one and the created element is set as the
	 * current one.
	 * @param name The tag name of the element.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 * @see #addElement()
	 */
	public void newElement(final String name) {
		checkAttributes();
		XXElement newXXElement = _actXXElement.prepareXXElement(name);
		if (_chkdoc.errors()) {
			_chkdoc.getReporter().checkAndThrowErrorWarnings();
		}
		if (newXXElement.errors()) {
			throw new SRuntimeException("MSGX019",
			 "Attempt to insert undeclared element '&{0}' to '&{1}'",
				name, _actXXElement.getXPos());
		}
		_actXXElement = newXXElement;
	}

	/** Add text node to actual element.
	 * @param text The value of the text node.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void addText(final String text) throws SRuntimeException {
		checkAttributes();
		if (text == null) {
			return;
		}
		if (!_actXXElement.addText(text)) {
			throw new SRuntimeException("MSGX020",
				"Attempt to insert undeclared text to '&{0}'",
				_actXXElement.getXPos());
		}
		if (_chkdoc.errors()) {
			_chkdoc.getReporter().checkAndThrowErrorWarnings();
		}
	}

	/** Final method for adding the current element which was created by method
	 * <tt>newElement(String)</tt> to the parent. After the element has been
	 * added it's parent is set as the current element.
	 * @see #newElement(String)
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void addElement() {
		checkAttributes();
		if (!_actXXElement.addElement() ||
			_actXXElement.errors()) {
			if (_chkdoc.errors()) {
				_chkdoc.getReporter().checkAndThrowErrorWarnings();
			}
			throw new SRuntimeException("MSGX021",
				"Inserted element &{0} is incomplete&{1}{; }",
				_actXXElement.getXPos(),
				_actXXElement.getReporter().printToString());
		}
		_actXXElement = (XXElement) _actXXElement.getParent();
	}

	/** Check if name of the current element is equal to argument value
	 * and add element. See description of {@link #addElement()} and also
	 * {@link #newElement(String)}.
	 * @param name The name to be checked.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public void addElement(final String name) {
		if (!_actXXElement.getXXName().equals(name)) {
			throw new SRuntimeException("MSGX022",
			 "Error when inserting element '&{0}' to '&{1}'",
				name, _actXXElement.getXPos());
		}
		addElement();
	}

	/** Get root element of the message.
	 * @return The root element of constructed object.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public Element getRootElement() {
		check();
		return _rootElement;
	}

	/** Get string with XML form of constructed message.
	 * @return The string with XML representation of constructed object.
	 * @throws SRuntimeException if constructed object doesn't correspond to
	 * X-definition.
	 */
	public String getMsg() {
		check();
		return KXmlUtils.nodeToString(_rootElement);
	}

	/** Get value specified by xpath. The xpath specification must denote
	 * a string (i.e. the attribute or node text value).
	 * @param xpath The xpath of attribute.
	 * @return The value of content.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public Object getValue(final String xpath) {
		return getValue(_actXXElement, xpath);
	}

	/** Get text value of the root element.
	 * @return The string with the text value of the root element.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public String getRootText() {
		return KXmlUtils.getTextValue(_rootXXElement.getElement());
	}

	/** Get text value of element given by argument.
	 * @param xpath The xpath of the element.
	 * @return The string with the text value.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public String getText(final String xpath) {
		if (xpath.endsWith("text()")) {
			return (String) getValue(_rootXXElement, xpath);
		}
		return (String) getValue(_rootXXElement, xpath + "/text()");
	}

	/** Return <tt>org.w3c.dom.NodeList</tt> with child elements of element
	 * given by name.
	 * @param name The specification of path to element.
	 * @return The list of child elements.
	 */
	public NodeList getChildElements(final String name) {
		return getChildElements(_actXXElement, name);
	}

	/** Return <tt>org.w3c.dom.NamedNodeMap</tt> with attributes of element
	 * given by xpath argument.
	 * @param xpath The specification of path to element.
	 * @return The list of attributes.
	 */
	public NamedNodeMap getAttributes(final String xpath) {
		return getAttributes(_actXXElement, xpath);
	}

	/** Return <tt>org.w3c.dom.NodeList</tt> with all child nodes (i.e.
	 * elements, text nodes etc) of element given by xpath argument.
	 * @param xpath The specification of path to element.
	 * @return The list of child nodes.
	 */
	public NodeList getChildNodes(final String xpath) {
		return getChildNodes(_actXXElement, xpath);
	}

	/** Get Element specified by xpath starting from root.
	 * @param xpath The path to Element starting from root.
	 * @return The specified element.
	 */
	public Element getElement(final String xpath) {
		return getElement(xpath, _chkdoc.getDocument());
	}

	/** Get Element.
	 * @param xpath The path to XXElement object.
	 * @param base The parent element.
	 * @return The specified element.
	 */
	public static Element getElement(final String xpath, final Node base) {
		String x = xpath;
		String y = "";
		int i = xpath.indexOf('/');
		if (i > 0) {
			x = xpath.substring(0,i).trim();
			y = xpath.substring(i + 1).trim();
		}

		String ndx = null;
		int j = x.indexOf('[');
		if (j > 0) {
			ndx = x.substring(j+1,x.length() - 1).trim();
			x = x.substring(0,j).trim();
		}
		NodeList nl = base.getNodeType() == Node.DOCUMENT_NODE ?
			((Document)base).getElementsByTagName(x) :
			((Element)base).getElementsByTagName(x);
		if (nl == null) {
			return null;
		}
		int nodesNum = nl.getLength();
		if (nodesNum <= 0) {
			return null;
		}
		int index =	0;
		if (ndx != null) {
			if ("last()".equals(ndx)) {
				index = nodesNum - 1;
			} else {
				index = Integer.parseInt(ndx);
				if (index > nodesNum) {
					return null;
				}
				index--;
				if (index < 0) {
					return null;
				}
			}
		}
		return i < 0 ? (Element) nl.item(index) : getElement(y, nl.item(index));
	}

	/** Get value specified by xpath. The xpath specification must denote
	 * a string (i.e. the attribute or node text value). If given xpath doesn't
	 * fit to specified value return null. I result of xpath expression is
	 * a set of values the returned value is the first item.
	 * @param base XXElement where search starts.
	 * @param xpath The xpath to an attribute or a text value.
	 * @return The result of xpath expression.
	 * @throws SRuntimeException the check swith is set on and the attribute
	 * is not specified in definition.
	 */
	public static Object getValue(final XXElement base,
		final String xpath) throws SRuntimeException {
		String myPath = xpath.trim();
		int charPos = myPath.lastIndexOf('@');
		if (charPos == 0) {
			return getDefinedAttribute(base, myPath.substring(1).trim());
		}
		String attrName;
		if (charPos > 0) {
			attrName = myPath.substring(charPos + 1).trim();
			myPath = myPath.substring(0,charPos).trim();
		} else {
			attrName = null;
		}
		boolean text;
		if ((text = myPath.endsWith("text()"))) {
			myPath = myPath.substring(0,xpath.length() - 7).trim();
		}
		if (myPath.endsWith("/") || myPath.endsWith(".")) {
			myPath = myPath.substring(0,myPath.length() - 1);
		}
		XXElement xxElement;
		if (myPath.startsWith("/")) {
			myPath = myPath.substring(1);
			xxElement = getChkElement(myPath, base.getXDDocument());
			if (xxElement == null) {
				//Attempt to get undeclared item
				throw new SRuntimeException(XDEF.XDEF581,
					"&{xpath}" + xpath);
			}
		} else {
			if (myPath.length() > 0) {
				xxElement = getChkElement(myPath, base);
				if (xxElement == null) {
					//Attempt to get undeclared item
					throw new SRuntimeException(XDEF.XDEF581,
						"&{xpath}" + myPath);
				}
			} else {
				xxElement = base;
			}
		}
		Element el = xxElement.getElement();
		if (el == null) {
			return null;
		}
		if (attrName == null) {//no attribute
			if (!text) {
				return el;
			}
			XMElement xxElem = xxElement.getXMElement();
			if (!xxElem.hasOtherElements()) {
				XMNode[] defList = xxElem.getChildNodeModels();
				boolean hasText = false;
				if (defList != null) {
					for (int i = 0; i < defList.length; i++) {
						if (defList[i].getKind() == XMNode.XMTEXT) {
							hasText = true;
							break;
						}
					}
					if (!hasText) {
						//Attempt to get undeclared item
						throw new SRuntimeException(XDEF.XDEF581,
							"&{xpath}" + xpath);
					}
				}
			}
			NodeList nl = el.getChildNodes();
			StringBuilder sb = new StringBuilder();
			for (int i=0; i < nl.getLength(); i++) {
				if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(nl.item(i).getNodeValue());
				}
			}
			return sb.toString();
		}
		return getDefinedAttribute(xxElement, attrName);
	}

	/** Get ChkElement.
	 * @param xpath The path to ChkElement object.
	 * @param base The parent object.
	 * @return XXElement defined by the given path.
	 */
	private static XXElement getChkElement(final String xpath,
		final XXNode base) {
		String x = xpath;
		String y = "";
		int i = xpath.indexOf('/');
		if (i > 0) {
			x = xpath.substring(0,i).trim();
			y = xpath.substring(i + 1).trim();
		}
		XXNode[] childList = base.getChildXXNodes();
		XXElement result = null;
		int j = x.indexOf('[');
		int ndx = -1;
		if (j > 0) {
			String s = x.substring(j+1,x.length() - 1).trim();
			if ("last()".equals(s)) {
				ndx = Integer.MAX_VALUE;
			} else {
				try {
					ndx = Integer.parseInt(s);
				} catch(Exception ex) {
					if (ex instanceof SThrowable) {
						throw new SRuntimeException(
							((SThrowable) ex).getReport());
					}
					//Invalid XPath syntax in index expression
					throw new SRuntimeException(XDEF.XDEF519,
						"&{xpath}" + xpath);
				}
			}
			x = x.substring(0,j).trim(); //here just extract index
		}
		int count = 0;
		for (int k = 0; childList != null && k < childList.length; k++) {
			XXNode chkNode = childList[k];
			if (x.equals(chkNode.getXXName())) {
				result = (XXElement) chkNode;
				if (++count >= ndx) {
					break;
				}
			}
		}
		return result == null ? null :
			i < 0 ? result : getChkElement(y, result);
	}

	/** Get attribute from the XXElement object.
	 * @param chkElem The check element (the definition).
	 * @param name The name of attribute.
	 * @return The value of attribute or the empty string if the value
	 * doesn't exist or return null if required attribute is defined in the
	 * X-definition model, however it does not exist in the actual element.
	 * @throws SRuntimeException if required attribute is not defined
	 * in the X-definition.
	 */
	private static String getDefinedAttribute(final XXElement xElem,
		final String name) throws SRuntimeException {
		String sname = name.charAt(0) == '@' ? name.substring(1) : name;
		Element el = xElem.getElement();
		String s = el.getAttribute(sname);
		if (s.length() > 0) {
			return s;
		}
		if (el.hasAttribute(sname)) {
			return "";
		}
		//attribute not exist in element.
		//prepare path for error message
		s = sname;
		Node parent = el;
		while (((parent = parent.getParentNode()) != null) &&
			(parent.getNodeType() == Node.ELEMENT_NODE)) {
			s = parent.getNodeName() + "/" + s;
		}
		if (xElem.checkAttributeLegal(sname)) {
			return null;
		}
		//Attempt to get undeclared item
		throw new SRuntimeException(XDEF.XDEF581,
			"&{xpath}" + '/' + s + "/@" + sname);
	}

	/** Return <tt>org.w3c.dom.NodeList</tt> with child elements of element
	 * given by xpath argument.
	 * @param path The specification of path to element.
	 * @return The list of child elements.
	 */
	private static NodeList getChildElements(final XXElement base,
		final String path) {
		String xpath = path.charAt(0) == '/' ? path.substring(1) : path;
		int len = xpath.length();
		if (xpath.charAt(len - 1) == '/') {
			xpath = xpath.substring(0, --len);
		}
		Element el;
		XDDocument xd = base.getXDDocument();
		if (len > 0) {
			XXElement xxElement = getChkElement(xpath, xd);
			if (xxElement == null) {
				//Attempt to get undeclared item
				throw new SRuntimeException(XDEF.XDEF581, "&{xpath}" + xpath);
			}
			el = xxElement.getElement();
		} else {
			el = xd.getElement();
		}
		return el == null ? new KEmptyNodeList() : el.getElementsByTagName("*");
	}

	/** Return <tt>org.w3c.dom.NamedNodeMap</tt> with attributes of element
	 * given by xpath argument.
	 * @param xelem Element model.
	 * @param path The specification of path to element.
	 * @return The list of attributes.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final NamedNodeMap getAttributes(final XXElement xelem,
		final String path) throws SRuntimeException {
		String xpath = path.charAt(0) == '/' ? path.substring(1) : path;
		if (xpath.endsWith("text()")) {
			xpath = xpath.substring(0,xpath.length() - 6).trim();
		}
		if (xpath.endsWith("/")) {
			xpath = xpath.substring(0,xpath.length() - 1);
		}
		Element el;
		XDDocument xd = xelem.getXDDocument();
		if (xpath.length() > 0) {
			XXElement xxElement = getChkElement(xpath, xd);
			if (xxElement == null) {
				//Attempt to get undeclared item
				throw new SRuntimeException(XDEF.XDEF581, "&{xpath}" + xpath);
			}
			el = xxElement.getElement();
		} else {
			el = xd.getElement();
		}
		return el == null ? null : el.getAttributes();
	}

	/** Return <tt>org.w3c.dom.NodeList</tt> with all child nodes (i.e.
	 * elements, text nodes etc) of element given by XPath argument.
	 * @param xelem Element model.
	 * @param path The specification of path to element.
	 * @return The list of child nodes.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final NodeList getChildNodes(final XXElement xelem,
		final String path) throws SRuntimeException {
		String xpath = path.charAt(0) == '/' ? path.substring(1) : path;
		if (xpath.endsWith("/")) {
			xpath = xpath.substring(0,xpath.length() - 1);
		}
		Element el;
		XDDocument xd = xelem.getXDDocument();
		if (xpath.length() > 0) {
			XXElement xxElement = getChkElement(xpath, xd);
			if (xxElement == null) {
				//Attempt to get undeclared item
				throw new SRuntimeException(XDEF.XDEF581, "&{xpath}" + xpath);
			}
			el = xxElement.getElement();
		} else {
			el = xd.getElement();
		}
		return el == null ? new KEmptyNodeList() : el.getChildNodes();
	}
}
/* *****************************************************************************
# Prefix of messages.
_prefix=MSGX

# ISO name of language.
_language=ces

# Description of messages.
MSGX_DESCRIPTION=Chybov\u00e1 hl\u00e1\u0161en\u00ed pro MsgX class

# Localized name of language.
MSGX_LANGUAGE=\u010desky

# *************** Messages: ***************
MSGX011=Chyb\u00ed X-definice pro zpr\u00e1vu '&{0}'
MSGX012=P\u0159i \u010dten\u00ed zpr\u00e1vy '&{0}' byly detekov\u00e1ny chyby&{1}{; }
MSGX013=Zpr\u00e1va '&{0}' nen\u00ed definovan\u00e1
MSGX014=P\u0159i tvorb\u011b zpr\u00e1vy '&{0}' byly detekov\u00e1ny chyby&{1}{; }
MSGX015=Nespr\u00e1vn\u00fd parametr mode: '&{0}'
MSGX016=Chyba p\u0159i vkl\u00e1d\u00e1n\u00ed elementu '&{0}' do '&{1}'&{2}{; }
MSGX017=Chyba p\u0159i vkl\u00e1d\u00e1n\u00ed atributu '&{0}'&{1}{; }
MSGX018=Zpr\u00e1va je chybn\u00e1&{0}{; }
MSGX019=Pokus o vlo\u017een\u00ed nedeklarovan\u00e9ho elementu '&{0}' do '&{1}'
MSGX020=Pokus o vlo\u017een\u00ed nedeklarovan\u00e9ho textu do '&{0}'
MSGX021=Vlo\u017een\u00fd element &{0} je ne\u00fapln\u00fd&{1}{; }
MSGX022=Chyba p\u0159i vkl\u00e1d\u00e1n\u00ed elementu '&{0}' do '&{1}'
MSGX023=Pokus o zm\u011bnu ji\u017e zkontrolovan\u00e9 zpr\u00e1vy
********************************************************************************
# Prefix of messages.
_prefix=MSGX

# ISO name of language.
_language=eng

# Description of messages.
MSGX_DESCRIPTION=Error messages for MsgX class

# Localized name of language.
MSGX_LANGUAGE=English

# *************** Messages: ***************
MSGX011=X-definition for message '&{0}' is missing
MSGX012=Errors detected while reading message '&{0}'&{1}{; }
MSGX013=Message '&{0}' is not defined
MSGX014=Errors detected while creating message '&{0}'&{1}{; }
MSGX015=Invalid parameter mode: '&{0}'
MSGX016=Error when inserting element '&{0}' into '&{1}'&{2}{; }
MSGX017=Error when inserting attribute '&{0}'&{1}{; }
MSGX018=Message is incorrect&{0}{; }
MSGX019=Attempt to insert undeclared element '&{0}' to '&{1}'
MSGX020=Attempt to insert undeclared text to '&{0}'
MSGX021=Inserted element &{0} is incomplete&{1}{; }
MSGX022=Error when inserting element '&{0}' to '&{1}'
MSGX023=Attempt to change already checked message
********************************************************************************
# Prefix of messages.
_prefix=MSGX

# ISO name of language.
_language=slk

# Description of messages.
MSGX_DESCRIPTION=Chybov\u00e9 hl\u00e1senia pre MsgX class

# Localized name of language.
MSGX_LANGUAGE=slovensky

# *************** Messages: ***************
MSGX011=Ch\u00fdba X-defin\u00edcia pre spr\u00e1vu '&{0}'
MSGX012=Pri \u010d\u00edtan\u00ed spr\u00e1vy '&{0}' boli detekovan\u00e9 chyby&{1}{; }
MSGX013=Spr\u00e1va '&{0}' nie je definovan\u00e1
MSGX014=Pri tvorbe spr\u00e1vy '&{0}' boli detekovan\u00e9 chyby&{1}{; }
MSGX015=Nespr\u00e1vny parameter mode: '&{0}'
MSGX016=Chyba pri vkladan\u00ed elementu '&{0}' do '&{1}'&{2}{; }
MSGX017=Chyba pri vkladan\u00ed atrib\u00fatu '&{0}'&{1}{; }
MSGX018=Spr\u00e1va je chybn\u00e1&{0}{; }
MSGX019=Pokus o vlo\u017eenie nedeklarovan\u00e9ho elementu '&{0}' do '&{1}'
MSGX020=Pokus o vlo\u017eenie nedeklarovan\u00e9ho textu do '&{0}'
MSGX021=Vlo\u017een\u00fd element &{0} je ne\u00fapln\u00fd&{1}{; }
MSGX022=Chyba pri vkladan\u00ed elementu '&{0}' do '&{1}'
MSGX023=Pokus o zmenu u\u017e skontrolovanej spr\u00e1vy

********************************************************************************/