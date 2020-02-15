package org.xdef.impl.compile;

import org.xdef.XDConstants;
import org.xdef.impl.XDefinition;
import org.xdef.msg.JSON;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.sys.StringParser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import org.xdef.impl.XConstants;
import org.xdef.sys.SUtils;

/** Reads source X-definitions and prepares list of PNodes with X-definitions
 * from JSON source data.
 * @author Trojan
 */
class PreReaderJSON implements PreReader {
	/** Instance of PreCompiler. */
	private final XPreCompiler _pcomp;

	/** Creates a new instance of XDefCompiler
	 * @param pcomp pre compiler.
	 */
	PreReaderJSON(XPreCompiler pcomp) {_pcomp = pcomp;}

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param fileName pathname of file with with X-definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseFile(final String fileName) {
		parseFile(new File(fileName));
	}

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param file The file with with X-definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseFile(final File file) {
		try {
			URL url = file.getCanonicalFile().toURI().toURL();
			url = SUtils.getExtendedURL(url.toExternalForm());
			for (Object o: _pcomp.getSources()) {
				if (o instanceof URL && url.equals(o)) {
					return; // nothing parse, found in the list of sources
				}
			}
			_pcomp.getSources().add(url);
			parseStream(new FileInputStream(file), url.toExternalForm());
		} catch (RuntimeException ex) {
			throw ex;
		} catch (IOException ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902,
				(file == null ? (String) null : file.getAbsolutePath()));
		}
	}

	/** Parse InputStream source X-definition and addAttr it to the set
	 * of definitions.
	 * @param in input stream with the X-definition.
	 * @param srcName name of source data used in reporting (SysId) or
	 * <tt>null</tt>.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseStream(final InputStream in, final String srcName) {
		if (!_pcomp.getSources().contains(in)) {
			_pcomp.getSources().add(in);
			try {
				doParse(in, srcName);
			} catch (Exception ex) {
				if (ex instanceof SThrowable) {
					throw new SRuntimeException(((SThrowable) ex).getReport());
				} else {
					//Internal error: &{0}
					throw new SRuntimeException(SYS.SYS066,
						ex, "when parsing document\n" + ex);
				}
			}
		}
	}

	/** Parse data with source X-definition given by URL and addAttr it
	 * to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 */
	public final void parseURL(final URL url) {
		for (Object o: _pcomp.getSources()) {
			if (o instanceof URL && url.equals((URL) o)) {
				return; //prevents to doParse the source twice.
			}
		}
		String srcName = url.toExternalForm();
		_pcomp.getSources().add(srcName);
		try {
			parseStream(url.openStream(), srcName);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			//Can't read X-definition from the file &{0}
			throw new SRuntimeException(XDEF.XDEF902, srcName);
		}
	}

	/** Check if the name of X-definition is OK.
	 * @param name name of X-definition
	 * @return true if the name of X-definition is OK.
	 */
	final static boolean chkDefName(final String name, byte xmlVersion) {
		if (name.length() == 0) {
			return true; // empty name is also a name of X-definition
		}
		if (StringParser.getXmlCharType(name.charAt(0),  xmlVersion) !=
			StringParser.XML_CHAR_NAME_START) {
			return false; // must start with MXL start name
		}
		char c;
		boolean wasColon = false;
		for (int i = 1; i < name.length(); i++) {
			if (StringParser.getXmlCharType(c = name.charAt(i),  xmlVersion) !=
				StringParser.XML_CHAR_NAME_START && (c  < '0' && c > '9')) {
				if (!wasColon && c == ':') { // we allow one colon inside name
					wasColon = true;
					if (i + 1 < name.length()
						&& StringParser.getXmlCharType(
							name.charAt(++i), xmlVersion)
						!= StringParser.XML_CHAR_NAME_START){//must follow name
						continue;
					}
				}
				return false;
			}
		}
		return true;
	}

	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseString(final String source) {
		parseString(source, null);
	}

	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 * @throws RutimeException if an error occurs.
	 */
	public final void parseString(final String source, final String srcName) {
		if (_pcomp.getSources().indexOf(source) >= 0 || source.length() == 0) {
			return;  //we ignore already declared or empty strings
		}
		char c;
		if ((c = source.charAt(0)) == '[' || c == '{') {
			_pcomp.getSources().add(source);
			try {
				parseStream(new ByteArrayInputStream(
					source.getBytes(Charset.forName("UTF-8"))), srcName);
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			parseFile(source);
		}
	}

	@Override
	/** Parse source input stream.
	 * @param in input stream with source data.
	 * @param sysId system ID of source data.
	 * @throws RutimeException if an error occurs.
	 */
	public final void doParse(final InputStream in, final String srcName) {
		Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
		doParse(reader, srcName);
	}


	/** JSON parser */
	private static final class JParser {

		/** parser used for parsing of source. */
		private final StringParser _p;

		/** Skip white spaces and comments. */
		public final void skipBlanksAndComments() {
			_p.isSpaces();
			boolean b = false;
			while(_p.isToken("/*") || (b=_p.isToken("//"))) {
				if (b) {
					_p.skipToNextLine();
				} else {
					if (!_p.findTokenAndSkip("*/")) {
						//Unclosed comment&amp;
						throw new SRuntimeException(JSON.JSON015, genPosMod());
					}
				}
				b = false;
				_p.isSpaces();
			}
		}

		/** Create instance of JSON parser build with reader,
		 * @param p parser of source data,
		 */
		private JParser(final StringParser p) {_p = p;}

		/** Check if argument is a hexadecimal digit. */
		private static int hexDigit(final char ch) {
			int i = "0123456789abcdefABCDEF".indexOf(ch);
			return i >= 16 ? i - 6 : i;
		}

		/** Create modification string with source position.
		 * @return modification string with source position.
		 */
		private String genPosMod() {
			if (_p instanceof StringParser) {
				StringParser p = (StringParser) _p;
				return "&{line}" + p.getLineNumber()
					+ "&{column}" + p.getColumnNumber()
					+ "&{sysId}" + p.getSysId();
			} else {
				return null;
			}
		}

		/** Read JSON value.
		 * @return parsed value: List, Map, String, Number, Boolean or null.
		 * @throws SRuntimeException is an error occurs.
		 */
		private JObject readValue() throws SRuntimeException {
			if (_p.eos()) {
				//unexpected eof
				throw new SRuntimeException(JSON.JSON007, genPosMod());
			}
			SPosition spos = _p.getPosition();
			if (_p.isChar('{')) { // Map
				JMap result = new JMap(spos);
				skipBlanksAndComments();
				if (_p.isChar('}')) {
					return result;
				}
				for (;;) {
					JObject o = readValue();
					if (o != null && o.getType() == 'S') {
						 // parse JSON named pair
						JString name = (JString) o;
						skipBlanksAndComments();
						if (!_p.isChar(':')) {
							//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
							throw new SRuntimeException(JSON.JSON002, ":",
								genPosMod());
						}
						skipBlanksAndComments();
						result.put(name, readValue());
						skipBlanksAndComments();
						if (_p.isChar('}')) {
							skipBlanksAndComments();
							return result;
						}
						if (_p.isChar(',')) {
							skipBlanksAndComments();
						} else {
							//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
							throw new SRuntimeException(
								JSON.JSON002, ",", "}", genPosMod());
						}
					} else {
						// String with name of item expected
						throw new SRuntimeException(JSON.JSON004, genPosMod());
					}
				}
			} else if (_p.isChar('[')) {
				JList result = new JList(spos);
				skipBlanksAndComments();
				if (_p.isChar(']')) {
					return result;
				}
				for(;;) {
					result.add(readValue());
					skipBlanksAndComments();
					if (_p.isChar(']')) {
						return result;
					}
					if (_p.isChar(',')) {
						skipBlanksAndComments();
					}
				}
			} else if (_p.isChar('"')) { // string
				spos = _p.getPosition();
				StringBuilder sb = new StringBuilder();
				while (!_p.eos()) {
					if (_p.isToken("\"\"")) {
						sb.append('"');
					} else if (_p.isChar('"')) {
						String s = sb.toString();
						return new JString(new SBuffer(s, spos));
					} else if (_p.isChar('\\')) {
						char c = _p.peekChar();
						if (c == 'u') {
							int x = 0;
							for (int j = 1; j < 4; j++) {
								int y = hexDigit(_p.peekChar());
								if (y < 0) {
									// hexadecimal digit expected
									throw new SRuntimeException(JSON.JSON005,
										genPosMod());
								}
								x = (x << 4) + y;
							}
							sb.append((char) x);
						} else {
							int i = "\"\\/bfnrt".indexOf(c);
							if (i >= 0) {
								sb.append("\"\\/\b\f\n\r\t".charAt(i));
							} else {
								 // Incorrect control character in string
								throw new SRuntimeException(JSON.JSON006,
									genPosMod());
							}
						}
					} else {
						sb.append(_p.peekChar());
					}
				}
				// end of string ('"') is missing
				throw new SRuntimeException(JSON.JSON001, genPosMod());
			} else if (_p.isToken("null")) {
				return new JNull(spos);
			} else if (_p.isToken("true")) {
				return new JBoolean(new SBuffer("true", spos));
			} else if (_p.isToken("false")) {
				return new JBoolean(new SBuffer("false", spos));
			} else {
				boolean minus = _p.isChar('-');
				int pos = _p.getIndex();
				Number n;
				String s;
				if (_p.isFloat()) {
					s = _p.getBufferPart(pos, _p.getIndex());
					n = new BigDecimal((minus ? "-" : "") + s);
				} else if (_p.isInteger()) {
					s = _p.getBufferPart(pos, _p.getIndex());
					n = new BigInteger((minus ? "-" : "") + s);
				} else {
					if (minus) {
						// number expected
						throw new SRuntimeException(JSON.JSON003, genPosMod());
					} else {
						//JSON value expected
						throw new SRuntimeException(JSON.JSON010, genPosMod());
					}
				}
				if (s.charAt(0) == '0' && s.length() > 1 &&
					Character.isDigit(s.charAt(1))) {
						// Illegal leading zero in number
						throw new SRuntimeException(JSON.JSON014, genPosMod());
				}
				return new JNumber(new SBuffer(s, spos), n);
			}
		}

		/** Parse source data.
		 * @return parsed JSON object.
		 * @throws SRuntimeException if an error occurs,
		 */
		private JObject parse() throws SRuntimeException {
			skipBlanksAndComments();
			char c = _p.getCurrentChar();
			if (c != '{' && c != '[' ) {
				// JSON object or array expected"
				throw new SRuntimeException(JSON.JSON009, genPosMod());
			}
			JObject result = readValue();
			skipBlanksAndComments();
			if (!_p.eos()) {
				//Text after JSON not allowed
				throw new SRuntimeException(JSON.JSON008, genPosMod());
			}
			return result;
		}
	}

	private interface JObject {

		public SPosition getPosition();

		public char getType();

		@Override
		public int hashCode();

		@Override
		public boolean equals(Object o);
	}

	private static class JString implements JObject {

		private final SBuffer _val;

		JString(SBuffer val) {_val = val;}

		@Override
		public SPosition getPosition() {return _val;}

		@Override
		public char getType() {return 'S';}

		public SBuffer getValue() {return _val;}

		public String getString() {return _val.getString();}

		@Override
		public int hashCode() {return _val.getString().hashCode();}

		@Override
		public boolean equals(Object o) {
			if (o instanceof JString) {
				return _val.getString().equals(((JString) o)._val.getString());
			}
			return false;
		}
	}

	private static class JNumber implements JObject {

		private final SBuffer _val;

		private final Number _n;

		JNumber(SBuffer val, Number n) {_val = val; _n = n;}

		@Override
		public SPosition getPosition() {return _val;}

		@Override
		public char getType() {return 'N';}

		public SBuffer getValue() {return _val;}

		public Number getNumber() {return _n;}

		@Override
		public int hashCode() {return _n.hashCode();}

		@Override
		public boolean equals(Object o) {
			if (o instanceof JNumber) {
				return _n.equals(((JNumber) o)._n);
			}
			return false;
		}
	}

	private static class JBoolean implements JObject {

		private final SBuffer _val;

		JBoolean(SBuffer val) {_val = val;}

		@Override
		public SPosition getPosition() {return _val;}

		@Override
		public char getType() {return 'B';}

		public boolean getValue() {return "true".equals(_val.getString());}
	}

	private static class JNull implements JObject {

		private final SBuffer _val;

		JNull(SPosition val) {_val = new SBuffer("null", val);}

		@Override

		public SPosition getPosition() {return _val;}

		@Override
		public char getType() {return '_';}

		@Override
		public int hashCode() {return 1;}

		@Override
		public boolean equals(Object o) {return (o instanceof JNull);}
	}

	private static class JMap extends HashMap<JString, JObject>
		implements JObject {

		private final SPosition _spos;

		private JMap(SPosition spos) {
			super();
			_spos = spos;
		}

		@Override
		public SPosition getPosition() {return _spos;}

		@Override
		public char getType() {return 'M';}
	}

	private static class JList extends ArrayList<JObject> implements JObject {

		private final SPosition _spos;

		private JList(SPosition spos) {
			super();
			_spos = spos;
		}

		@Override
		public SPosition getPosition() {return _spos;}

		@Override
		public char getType() {return 'L';}

	}
////////////////////////////////////////////////////////////////////////////////
	private Entry<JString, JObject> getJAttrWithPrefix(final JMap map,
		final String prefix,
		final boolean remove) {
		for (Entry<JString, JObject> e: map.entrySet()) {
			if (e.getKey().getString().startsWith(prefix + ':')
				&& e.getValue().getType() == 'S') {
				if (remove) {
					map.remove(e.getKey());
				}
				return e;
			}
		}
		return null;
	}

	private JString getJAttr(final JMap map,
		final String name,
		final boolean remove) {
		JString jname = new JString(new SBuffer(name));
		JObject o = map.get(jname);
		if (o != null) {
			if (remove) {
				map.remove(jname);
			}
			if (o.getType() != 'S') {
				throw new RuntimeException("String expected");
			}
		}
		return (JString) o;
	}

	private JString getXDAttrWithOrWithotPrefix(final JMap map,
		final String prefix,
		final String name,
		final boolean remove) {
		JString jname = new JString(new SBuffer(name));
		JObject o = map.get(jname);
		if (o != null) {
			if (remove) {
				map.remove(jname);
			}
			if (o.getType() != 'S') {
				throw new RuntimeException("String expected");
			}
		} else {
			jname = new JString(new SBuffer(prefix + ':' + name));
			o = map.get(jname);
			if (o != null) {
				if (remove) {
					map.remove(jname);
				}
				if (o.getType() != 'S') {
					throw new RuntimeException("String expected");
				}
			}
		}
		return (JString) o;
	}

	private PAttr setPAttr(final PNode pnode,
		final JMap map,
		final String name,
		final boolean alsoNoPrefixed,
		final boolean remove) {
		int ndx = name.indexOf(':');
		JString o = getJAttr(map, name, remove);
		if (o != null) {
			SBuffer sbf = o.getValue();
			PAttr pattr = new PAttr(name, sbf, null, 0);
			if (ndx > 0) {
				pattr._localName = name.substring(ndx + 1);
				pattr._nsURI = XDConstants.XDEF31_NS_URI;
				pattr._nsindex = _pcomp.getNSURIIndex(pattr._nsURI);
			} else {
				pattr._localName = name;
				pattr._nsURI = null;
				pattr._nsindex = -1;
			}
			pnode._attrs.add(pattr);
			return pattr;
		}
		if (ndx > 0 && alsoNoPrefixed) {
			return setPAttr(pnode,
				map, name.substring(ndx + 1), alsoNoPrefixed, remove);
		}
		return null;
	}

	private static PNode createXdefPNode(final PNode parent,
		final String xdPrefix,
		final String localName,
		final SPosition spos) {
		PNode pn = new PNode(xdPrefix + ":" + localName,
			spos, parent, XConstants.XD31, (byte) 10);
		pn._localName = localName;
		pn._nsURI = XDConstants.XDEF31_NS_URI;
		pn._xdVersion = XConstants.XD31;
		return pn;
	}

	/** Generate declaration node to X-definition.
	 * @param list value of declaration.
	 * @param name
	 * @param decl
	 * @param parent
	 * @param level
	 */
	private void genXDDeclaration(final JList list,
		final String name,
		final JMap decl,
		final PNode parent,
		final int level) {
		int ndx = name.indexOf(':');
		String xdPrefix = name.substring(0, ndx);
		if (level > 1) {
			throw new RuntimeException("xd:declaration not allowe here");
		}
		if (list.size() < 2) {
			throw new RuntimeException("empty declaration");
		}
		PNode pn =
			createXdefPNode(parent, xdPrefix, "declaration",list.getPosition());
		JObject jo = list.get(1);
		if (jo.getType() != 'S') {
			throw new RuntimeException("Must be string!");
		} else {
			pn._value = ((JString) jo).getValue();
		}
		jo = decl.get(new JString(new SBuffer(name)));
		if (jo.getType() == 'M') {
			copyAttrs(pn, (JMap) jo);
		}
		pn._xdef = parent._xdef;
		parent._childNodes.add(pn);
		_pcomp.getPDeclarations().add(pn);
	}

	/** Generate a X-definition group.
	 * @param list list of child nodes.
	 * @param name name of group.
	 * @param decl Map with attributes.
	 * @param parent the node where will be generated the item as a child node.
	 * @param level nesting level.
	 */
	private void genXDGroup(final JList list,
		final String xdPrefix,
		final String name,
		final JMap decl,
		final PNode parent,
		final int level) {
		int ndx = name.indexOf(':');
		PNode pn = createXdefPNode(parent,
			xdPrefix, name.substring(ndx + 1),list.getPosition());
		JObject jo = decl.get(new JString(new SBuffer(name)));
		if (jo.getType() == 'M') {
			copyAttrs(pn, (JMap) jo);
		}
		for (int i = 1; i < list.size(); i++) {
			jo = list.get(i);
			if (jo.getType() == 'A') {
				JList jl = (JList) jo;
				jo = jl.get(0);
				if (jo.getType() == 'M') {
					genChild(jl, xdPrefix, pn, level + 1);
				}
			}
		}
	}

	/** Generate XDef item to parent node.
	 * @param list of child nodes.
	 * @param xdPrefix prefix of X-definition
	 * @param decl map of parameters.
	 * @param parent the node where will be generated the item as a child node.
	 * @param level nesting level.
	 */
	private void genItem(JList list,
		String xdPrefix,
		JMap decl,
		PNode parent,
		int level) {
		List<JString> params = new ArrayList<JString>();
		Map<String, String> prefixes = new HashMap<String, String>();
		for (JString x: decl.keySet()) {
			String name = x.getValue().getString();
			if (name.startsWith("xmlns")) {
				JObject jo = decl.get(x);
				if (jo.getType() == 'S') {
					String s = ((JString) jo).getValue().getString();
					int ndx = name.indexOf(':');
					String prefix;
					if (ndx > 0) {
						if (ndx == 5) {
							prefix = name.substring(6);
						} else {
							throw new RuntimeException("Incorrect xmlns");
						}
					} else {
						prefix = "";
					}
					prefixes.put(prefix, s);
				} else {
					throw new RuntimeException("Name expected");
				}
			} else {
				params.add(x);
			}
		}
		if (params.isEmpty()) {
			throw new RuntimeException("no named item");
		}
		for (JString x: params) {
			String name = x.getValue().getString();
			if (name.startsWith(xdPrefix + ':')) {
				if (name.endsWith(":declaration")) {
					genXDDeclaration(list, name, decl, parent, level);
				}
				if (name.endsWith(":mixed") || name.endsWith(":choice")
					|| name.endsWith(":sequence") || name.endsWith(":list")) {
					genXDGroup(list, xdPrefix, name, decl, parent, level);
				}
			} else if (!name.startsWith(xdPrefix + ':')) {
				genModel(list, x, decl, parent, level);
			}
		}
	}

	/** Add PNode child.
	 * @param pnode to this node add child.
	 * @param child node to be added.
	 */
	private void addNode(PNode pnode,
		PNode child) {
		String name = child._name.getString();
		int ndx = name.indexOf(':');
		child._localName = ndx > 0 ? name.substring(ndx + 1) : name;
		child._xdVersion = pnode._xdVersion;
		child._xmlVersion = pnode._xmlVersion;
		pnode._childNodes.add(child);
	}

	private void genModel(final JList list,
		final JString name,
		final JMap decl,
		final PNode parent,
		final int level) {
		String modelName = name.getValue().getString();
		PNode pn = new PNode(modelName, name._val, parent,
			parent._xdVersion, parent._xmlVersion);
		int ndx = modelName.indexOf(':');
		String modelPrefix;
		if (ndx > 0) {
			modelPrefix = modelName.substring(0, ndx);
			pn._localName = modelName.substring(ndx + 1);
		} else {
			modelPrefix = "";
			pn._localName = modelName;
		}
		int nsndx = parent._nsPrefixes.get(modelPrefix);
		pn._nsindex = nsndx;
		pn._nsURI = _pcomp.getNSURI(nsndx);
		addNode(parent, pn);
	}

	private void genChild(final JList list,
		final String xdPrefix,
		final PNode parent,
		final int level) {
		JObject jo = list.get(0);
		if (jo.getType() == 'M') {
			JMap decl = (JMap) jo;
			if (decl.isEmpty()) {
				throw new RuntimeException("Empty map");
			}
			genItem(list, xdPrefix, decl, parent, level);
		} else {
			throw new RuntimeException("List expected");
		}
	}

	private void copyAttrs(PNode pNode, JMap pars) {
		PAttr patt;
		JString jo;
		// set xmlns attributes
		if ((jo = getJAttr(pars, "xmlns", false)) != null
			&& jo.getType() == 'S') {
			patt = new PAttr("xmlns",
				((JString) jo).getValue(),
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XPreCompiler.NS_XMLNS_INDEX);
			patt._localName = "xmlns";
			pNode._attrs.add(patt);
			getJAttr(pars, "xmlns", true);
			int ndx = _pcomp.setNSURI(
				((JString) jo).getValue().getString());
			pNode._nsPrefixes.put("", ndx);
		}
		Entry<JString, JObject> e;
		while ((e = getJAttrWithPrefix(pars,"xmlns",true)) != null){
			String name = e.getKey().getString();
			patt = new PAttr(name,
				((JString) jo).getValue(),
				XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XPreCompiler.NS_XMLNS_INDEX);
			String ns = patt._localName = name.substring(6);
			pNode._attrs.add(patt);
			int ndx = _pcomp.setNSURI(
				((JString) jo).getValue().getString());
			pNode._nsPrefixes.put(ns, ndx);
		}
		// set other attributes
		JString[] attNames = pars.keySet().toArray(new JString[0]);
		for (JString x: attNames) {
			setPAttr(pNode, pars, x._val.getString(), false, true);
		}
	}

	private void doParse(final Reader reader, final String srcName) {
		StringParser parser = new StringParser(reader,_pcomp.getReportWriter());
		parser.setSysId(srcName);
		try {
			JParser jparser = new JParser(parser);
			JObject jparsed = jparser.parse();
			String defName = null;
			PNode pNode = null;
			if (jparsed.getType() == 'L') {
				JList jl = (JList) jparsed;
				if (jl.isEmpty()) {
					throw new RuntimeException("Empty list");
				}
				JObject jo =  jl.get(0);
				if (jo.getType() != 'M') {
					throw new RuntimeException("Map expected");
				}
				JMap jm = (JMap) jl.get(0);
				JString js = null;
				for (JString x: jm.keySet()) {
					String name = x.getValue().getString();
					if (name.endsWith(":def")) {
						js = x;
					}
				}
				if (js == null) {
					throw new RuntimeException("XDef missing");
				}
				String prefix = js.getValue().getString().split(":")[0];
				jo = jm.get(js);
				pNode = new PNode(prefix + ":def",
					jl.getPosition(), null, XConstants.XD31, (byte) 10);
				pNode._localName = "def";
				JMap pars;
				if (jo.getType() == 'M') { // map
					pars = (JMap) jo;
					js = getJAttr(pars, "xmlns:" + prefix, false);
					if (js == null
						|| !XDConstants.XDEF31_NS_URI.equals(js.getString())){
						throw new RuntimeException("Incorrect namespace: "+js);
					}
					//remove this attribute from pars
					pars.remove(new JString(new SBuffer("xmlns:" + prefix)));
					pNode._nsURI = XDConstants.XDEF31_NS_URI;
					pNode._xdVersion = XConstants.XD31;
					_pcomp.setURIOnIndex(0, pNode._nsURI);
					int nsndx = 0;
					pNode._nsPrefixes.put(prefix, nsndx);
					pNode._xdef = new XDefinition(defName,
						null, null, null, pNode._xmlVersion);
					pNode._nsindex = nsndx;
					pNode._xdef = new XDefinition(defName,
						null, null, null, pNode._xmlVersion);
					PAttr patt = new PAttr("xmlns:" + prefix,
						js.getValue(),
						XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
						XPreCompiler.NS_XMLNS_INDEX);
					patt._localName = prefix;
					pNode._attrs.add(patt);
					// copy other attributes to pNode (xmlns:j was processed)
					copyAttrs(pNode, pars);
					// get name of X-definition
					patt = pNode.getAttrNS("name", -1);
					if (patt == null) {
						patt = pNode.getAttrNS("name", 0);
					}
					defName = (patt != null) ? patt._value.getString() : "";
					// process models and child nodes
					for (int i = 1; i < jl.size(); i++) {
						JObject jx = jl.get(i);
						if (jx.getType() == 'L') {
							genChild((JList) jx, prefix, pNode, 1);
						} else if (jx.getType() == 'M') {
//							genChild((JList) jx, prefix, pNode, 0);
						} else {
							throw new RuntimeException("Model expected");
						}
					}
				}
			} else {
				throw new RuntimeException("Not correct JDefinition");
			}
			for (PNode p: _pcomp.getPXDefs()) {
				if (defName.equals(p._xdef.getName())) {
					defName = null;
				}
			}
			if (defName != null && pNode != null) {
				_pcomp.getPXDefs().add(pNode);
			}
		} catch (SRuntimeException ex) {
			throw new SRuntimeException(ex.getReport());
		}
		parser.closeReader();
	}
}