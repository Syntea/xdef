package org.xdef.impl.xml;

import org.xdef.sys.SPosition;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** Provide abstract class for implementation of XML readers.
 * @author Vaclav Trojan
 */
public abstract class XAbstractReader extends Reader {
	XHandler _handler;
	boolean _closed;
	private String _sysId;
	private String _encoding;
	private String _xinclude;
	/** Gen detailed position flag. */
	private boolean _genPositions = true;

	public XAbstractReader() {}

	public final void setEncoding(final String x) {_encoding = x;}

	public final String getEncoding() {return _encoding;}

	public final void setHandler(final XHandler x) {_handler = x;}

	public final String getXInclude() {return _xinclude;}

	public final void setXInclude(final String x) {_xinclude = x;}

	public final Object getHandler() {return _handler;}

	public final void setSysId(final String x) {_sysId = x;}

	public final String getSysId() {return _sysId;}

	public final boolean isClosed() {return _closed;}

	public final void stopGenPositions() {
		if (_genPositions) {
			_bf.setLength(0);
			_filePos = 0;
			_len = 0;
			_pos = 0;
			_genPositions = false;
			stopScanning();
		}
	}

	abstract void stopScanning();

	////////////////////////////////////////////////////////////////////////////
	// Implementation of methods from Reader
	////////////////////////////////////////////////////////////////////////////

	@Override
	abstract public int read() throws IOException;

	@Override
	abstract public int read(char[] cbuf) throws IOException;

	@Override
	abstract public int read(char[] cbuf, int off, int len) throws IOException;

	@Override
	abstract public void close() throws IOException;

	////////////////////////////////////////////////////////////////////////////
	// Methods used for parsing of character buffer
	////////////////////////////////////////////////////////////////////////////

	private final StringBuilder _bf = new StringBuilder();
	private int _len = 0;
	private int _pos = 0;
	private long _line = 1;
	private long _filePos = 0;
	private long _startLine = 0;
	private boolean _prologParsed = false;
	private boolean _wasEndTag = false;
	private boolean _includedText = false;
	private boolean _unresolved = false;

	public final int getPos() {return _pos;}

	public final String getBufPart(final int start, final int end) {
		return _bf.substring(start, end);
	}

	public final boolean wasEndTag() {return _wasEndTag;}

	public final void setWasEndTag(final boolean x) {_wasEndTag = x;}

	public final boolean prologParsed() {return _prologParsed;}

	public final boolean includedText() {return _includedText;}

	public final void setIncludedText(final boolean x) {_includedText = x;}

	public final boolean unresolved() {return _unresolved;}

	public final void setUnresolved(final boolean x) {_unresolved = x;}

	public final String getProlog() {
		int start = _pos;
		scanXMLDecl();
		scanSpacesCommentsAndPIs();
		scanDoctype();
		scanSpacesCommentsAndPIs();
		return _pos > start ? _bf.substring(start, _pos) : "";
	}

	final int scanSpacesCommentsAndPIs() {
		int start = _pos;
		scanSpaces();
		while (scanComment() >= 0 || scanPI() >= 0) {
			scanSpaces();
		}
		return start == _pos ? -1 : start;
	}

	final void addBuf(char c) {
		if (_genPositions) {
			_bf.append(c);
			_len++;
		}
	}

	final void addBuf(char[] buf) {
		if (_genPositions) {
			_bf.append(buf);
			_len += buf.length;
		}
	}

	final void addBuf(final char[] buf, final int off, final int len) {
		if (_genPositions) {
			_bf.append(buf, off, len);
			_len += len;
		}
	}

	public final boolean isEndBuf() {
		return _pos >= _len;
	}

	public final void releaseScanned() {
		if (_pos > 4) { // do it only if it makes sense
			_bf.delete(0, _pos);
			_filePos +=  _pos;
			_len = _bf.length();
			_pos = 0;
		}
	}

	public final char nextChar() {
		if (_pos >= _len) {
			return 0;
		}
		char ch = _bf.charAt(_pos++);
		if (ch == '\r') {
			if (isChar('\n')) {
				ch = '\n';
			}
		}
		if (ch == '\n') {
			_line++;
			_startLine = _filePos + _pos;
		}
		return ch;
	}

	public boolean isToken(final String s) {
		int len = s.length();
		if (_pos + len < _len + 1 && s.equals(_bf.substring(_pos, _pos+len))) {
			_pos += len;
			return true;
		}
		return false;
	}

	public final boolean isChar(final char c) {
		if (_pos < _len) {
			if (c == _bf.charAt(_pos)) {
				_pos++;
				return true;
			}
		}
		return false;
	}

	public final boolean chkChar(final char c) {
		if (_pos < _len) {
			return c == _bf.charAt(_pos);
		}
		return false;
	}

	public int scanSpaces() {
		int start = _pos;
		while (_pos < _len) {
			switch (_bf.charAt(_pos)) {
				case '\n':
					_line++;
					_startLine = ++_pos + _filePos;
					continue;
				case '\t':
				case ' ':
				case '\f':
				case '\r':
					break;
				default:
					return start == _pos ? -1 : start;
			}
			_pos++;
		}
		return -1;
	}

	public final int scanName() {
		int start = _pos;
		while (_pos < _len) {
			char ch = _bf.charAt(_pos);
			if (_pos == start && ((ch >= '0' && ch <= '9')
				|| ch == '-' || ch == '.')) {
				return -1; // name can't start with digit, "-", "."
			}
			if (" \n\t\r\f~@#$%^&*()+=``{}[]=/\\;,\"'!?><|".indexOf(ch) >= 0) {
				break; // not name character
			}
			_pos++;
		}
		return start == _pos ? -1 : start;
	}

	public final int scanStringToChar(final char quote) {
		int pos = _pos;
		long line = _line;
		long startLine = _startLine;
		char ch;
		while ((ch = nextChar()) != 0 && ch != quote) {}
		if (ch != 0) {
			return pos;
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	public final int scanLiteral() {
		int pos = _pos;
		long line = _line;
		long startLine = _startLine;
		char quote;
		if (isChar('\'')) {
			quote = '\'';
		} else if (isChar('"')) {
			quote = '"';
		} else {
			return -1;
		}
		if (skipTo(quote) > 0) {
			return pos;
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	private long skipTo(final char c) {
		int pos = _pos;
		long line = _line;
		long startLine = _startLine;
		for (;;) {
			if (isChar(c)) {
				return pos;
			}
			if (nextChar() == 0) {
				_line = line;
				_pos = pos;
				_startLine = startLine;
				return -1;
			}
		}
	}

	private int skipTo(final String s) {
		int pos = _pos;
		long line = _line;
		long startLine = _startLine;
		for (;;) {
			if (isToken(s)) {
				return pos;
			}
			if (nextChar() == 0) {
				_pos = pos;
				_line = line;
				_startLine = startLine;
				return -1;
			}
		}
	}

	public final int scanPI() {
		if (!isToken("<?")) {
			return -1;
		}
		int pos = _pos - 2;
		long line = _line;
		long startLine = _startLine;
		if (skipTo("?>") >= 0) {
			return pos;
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	private int scanPEReference() {
		if (!isChar('%')) {
			return -1;
		}
		int pos = _pos - 1;
		long line = _line;
		if (scanName() > 0 && isChar(';')) {
			return pos;
		}
		_pos = pos;
		_line = line;
		return -1;
	}

	private int scanElementdecl() {
		if (!isToken("<!ELEMENT")) {
			return -1;
		}
		int pos = _pos - 9;
		long line = _line;
		if (skipTo('>') >= 0) {
			return pos;
		}
		_pos = pos;
		_line = line;
		return -1;
	}

	private int scanAttlistDecl() {
		if (!isToken("<!ATTLIST")) {
			return -1;
		}
		int pos = _pos - 9;
		long line = _line;
		long startLine = _startLine;
		for (;;) {
			scanSpaces();
			if (scanName() >= 0) {
				continue;
			}
			if (scanLiteral() >= 0) {
				continue;
			}
			break;
		}
		if (skipTo('>') >= 0) {
			return _pos;
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	// GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
	// PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
	// PEDef ::= EntityValue | ExternalID
	// EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
	//             |  "'" ([^%&'] | PEReference | Reference)* "'"
	// PEReference ::= '%' Name ';'
	// ExternalID ::= 'SYSTEM' S SystemLiteral
	//            | 'PUBLIC' S PubidLiteral S SystemLiteral
	// SystemLiteral ::= '"' [^"]* '"') | ("'" [^']* "'")
	// PubidLiteral ::= '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
	// PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]
	// EntityDef ::= EntityValue | (ExternalID NDataDecl?)
	// NDataDecl ::= S 'NDATA' S Name
	private int scanEntityDecl() {
		if (!isToken("<!ENTITY")) {
			return -1;
		}
		int pos = _pos - 8;
		long line = _line;
		long startLine = _startLine;
		scanSpaces();
		boolean isPEDecl = isChar('%');
		if (isPEDecl) {
			scanSpaces();
		}
		scanName();
		for (;;) {
			scanSpaces();
			if (scanName() >= 0) {
				continue;
			}
			if (scanPEReference() >= 0) {
				continue;
			}
			if (scanLiteral() >= 0) {
				continue;
			}
			if (isChar('>')) {
				return pos;
			}
			if (nextChar() == 0) {
				break;
			}
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	// NotationDecl ::= '<!NOTATION' S Name S (ExternalID | PublicID) S? '>'
	private int scanNotationDecl() {
		if (!isToken("<!NOTATION")) {
			return -1;
		}
		int pos = _pos - 10;
		long line = _line;
		long startLine = _startLine;
		for (;;) {
			if (isChar('>')) {
				return pos;
			}
			if (nextChar() == 0) {
				break;
			}
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	public final SPosition getSPosition() {
		return new SPosition(0, _line, _startLine, _filePos + _pos, _sysId);
	}

	public final void setSPosition(SPosition p) {
		_pos = p.getIndex();
		_line = p.getLineNumber();
		_startLine = p.getStartLine();
		_filePos = p.getFilePos();
		_sysId = p.getSysId();
	}

	// markupdecl ::= elementdecl | AttlistDecl | EntityDecl
	//                | NotationDecl | PI | Comment
	private int scanMarkupDecl() {
		int result;
		if ((result = scanPEReference()) >= 0
			|| (result = scanElementdecl()) >= 0
			|| (result = scanAttlistDecl()) >= 0
			|| (result = scanEntityDecl()) >= 0
			|| (result = scanNotationDecl()) >= 0
			|| (result = scanPI()) >= 0 || (result = scanComment()) >= 0) {
			return result;
		}
		return -1;
	}

	public final int scanXMLDecl() {
		if (!isToken("<?xml")) {
			return -1;
		}
		int pos = _pos - 5;
		long line = _line;
		long startLine = _startLine;
		if (skipTo("?>") >= 0) {
			return pos;
		}
		_pos = pos;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	// document ::= prolog element Misc*
	// EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
	//             |  "'" ([^%&'] | PEReference | Reference)* "'"
	// AttValue ::= '"' ([^<&"] | Reference)* '"'
	//          |  "'" ([^<&'] | Reference)* "'"
	// SystemLiteral ::= '"' [^"]* '"') | ("'" [^']* "'")
	// PubidLiteral ::= '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
	// PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]
	// Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
	// PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
	// PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
	// prolog ::= XMLDecl? Misc* (doctypedecl Misc*)?
	// XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
	// VersionInfo ::= S 'version' Eq ("'" VersionNum "'" | '"' VersionNum '"')
	// Eq ::= S? '=' S?
	// VersionNum  ::= '1.' [0-9]+
	// EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' | "'" EncName "'" )
	// EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')
	// Misc ::= Comment | PI | S
	// doctypedecl ::= '<!DOCTYPE' S Name
	//             (S ExternalID)? S? ('[' intSubset ']' S?)? '>'
	// DeclSep ::= PEReference | S
	// PEReference ::= '%' Name ';'
	// intSubset ::= (markupdecl | DeclSep)*
	// markupdecl ::= elementdecl | AttlistDecl | EntityDecl
	//            | NotationDecl | PI | Comment
	// extSubset ::= TextDecl? extSubsetDecl
	// extSubsetDecl ::= ( markupdecl | conditionalSect | DeclSep)*
	// SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
	//        | ('"' ('yes' | 'no') '"'))
	// STag ::= '<' Name (S Attribute)* S? '>'
	// Attribute ::= Name Eq AttValue
	// ETag ::= '</' Name S? '>'
	// content ::= CharData? ((element | Reference | CDSect
	//         | PI | Comment) CharData?)*
	// EmptyElemTag ::= '<' Name (S Attribute)* S? '/>'
	// elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>
	// contentspec ::= 'EMPTY' | 'ANY' | Mixed | children
	// children ::= (choice | seq) ('?' | '*' | '+')?
	// cp ::= (Name | choice | seq) ('?' | '*' | '+')?
	// choice ::= '(' S? cp ( S? '|' S? cp )+ S? ')'
	// seq ::= '(' S? cp ( S? ',' S? cp )* S? ')'
	// Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
	//       | | '(' S? '#PCDATA' S? ')'
	// AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
	// AttDef ::= S Name S AttType S DefaultDecl
	// AttType ::= StringType | TokenizedType | EnumeratedType
	// StringType ::= 'CDATA'
	// TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES'
	//               | 'NMTOKEN' | 'NMTOKENS'
	// EnumeratedType ::= NotationType | Enumeration
	// NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
	// Enumeration ::= '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
	// DefaultDecl ::= '#REQUIRED' | '#IMPLIED' | (('#FIXED' S)? AttValue)
	// conditionalSect ::= includeSect | ignoreSect
	// includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'
	// ignoreSect ::= '<![' S? 'IGNORE' S? '[' ignoreSectContents* ']]>'
	// ignoreSectContents ::= Ignore ('<![' ignoreSectContents ']]>' Ignore)*
	// Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)
	// CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
	// Reference ::= EntityRef | CharRef
	// EntityRef ::= '&' Name ';'
	// PEReference ::= '%' Name ';'
	// EntityDecl ::= GEDecl | PEDecl
	// GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
	// PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
	// EntityDef ::= EntityValue | (ExternalID NDataDecl?)
	// PEDef ::= EntityValue | ExternalID
	// ExternalID ::= 'SYSTEM' S SystemLiteral
	//            | 'PUBLIC' S PubidLiteral S SystemLiteral
	// NDataDecl ::= S 'NDATA' S Name
	// TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
	// extParsedEnt ::= TextDecl? content
	// EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' | "'" EncName "'" )
	// EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
	// NotationDecl ::= '<!NOTATION' S Name S (ExternalID | PublicID) S? '>'
	// PublicID ::= 'PUBLIC' S PubidLiteral
	private int scanDoctype() {
		scanSpaces();
		if (!isToken("<!DOCTYPE")) {
			return -1;
		}
		int start = _pos - 9;
		long startLine = _startLine;
		scanSpaces();
		scanName();
		scanSpaces();
		if (isToken("SYSTEM")) {
			scanSpaces();
			scanLiteral();
		} else if (isToken("PUBLIC")) {
			scanSpaces();
			if (scanLiteral() >= 0) {
				scanSpaces();
			}
			scanLiteral();
		}
		scanSpaces();
		if (isChar('[')) {
			scanSpaces();
			// intSubset ::= (markupdecl | DeclSep)*
			// DeclSep ::= PEReference | S
			while(scanMarkupDecl() >= 0 || scanPEReference() >= 0) {
				scanSpaces();
			}
			scanStringToChar(']');
			scanSpaces();
		}
		if (isChar('>')) {
			scanSpaces();
			return start;
		}
		scanSpacesCommentsAndPIs();
		_pos = start;
		_startLine = startLine;
		return -1;
	}

	public final int scanComment() {
		if (!isToken("<!--")) {
			return -1;
		}
		int start = _pos - 4;
		long startLine = _startLine;
		do {
			if (isToken("-->")) {
				return start;
			}
		} while (nextChar() != 0);
		_pos = start;
		_startLine = startLine;
		return -1;
	}

	public final int scanCDATA() {
		if (!isToken("<![CDATA[")) {
			return -1;
		}
		int start = _pos - 9;
		long startLine = _startLine;
		do {
			if (isToken("]]>")) {
				return start;
			}
		} while (nextChar() != 0);
		_pos = start;
		_startLine = startLine;
		return -1;
	}

	public final int scanText() {
		int start = _pos;
		while(!chkChar('<') && !isEndBuf()) {
			if (chkChar('&')) {
				if (!isToken("&#")) {
					break;
				}
				continue;
			}
			nextChar();
		}
		return _pos > start ? start : -1;
	}

	public final int scanEntity() {
		if (!isChar('&')) {
			return -1;
		}
		int start = _pos - 1;
		if (scanName() > 0 && isChar(';')) {
			return start;
		}
		_pos = start;
		return -1;
	}

	public final int scanEndElement() {
		if (!isToken("</")) {
			return -1;
		}
		int start = _pos - 2;
		if (scanName() < 0) {
			_pos = start;
			return -1;
		}
		long line = _line;
		long startLine = _startLine;
		scanSpaces();
		if (isChar('>')) {
			return start;
		}
		_pos = start;
		_line = line;
		_startLine = startLine;
		return -1;
	}

	public final void scanProlog() {
		if (scanPI() >= 0) { // <?xml ... ?>
			scanSpaces();
		}
		scanSpacesCommentsAndPIs();
		scanDoctype();
		_prologParsed = true;
	}

	private SPosition getBufferPosition1() {
		return new SPosition(0, _line, _startLine, _filePos + _pos + 1, _sysId);
	}

	public final List<Object[]> getElementPositions(final String qName) {
		if (!_prologParsed) {
			scanProlog();
			releaseScanned();
		}
		// skip to the start of element
		while (scanSpacesCommentsAndPIs() > 0 || scanCDATA() >= 0
			|| scanEndElement() >= 0 || scanText() > 0 || scanEntity() > 0) {}
		List<Object[]> result = new ArrayList<Object[]>();
		String name;
		SPosition spos = getBufferPosition1();
		if ("*".equals(qName)) {
			int i = -1;
			if (!isChar('<') || (i = scanName()) < 0) {
				return result;
			}
			name = _bf.substring(i, _pos);
		} else if (!isToken("<" + qName)) {
			return result;
		} else {
			name = qName;
		}
		if (!"*".equals(qName)) {
			releaseScanned();
		}
		result.add(new Object[]{name, spos});
		while(!isEndBuf()) {
			int scanned = scanSpaces();
			boolean wasEndTag = false;
			if (isChar('>') || (wasEndTag = isToken("/>"))) { // attrs end
				_wasEndTag = wasEndTag;
				if (!"*".equals(qName)) {
					releaseScanned();
				}
				return result;
			}
			if (scanned < 0 || (scanned = scanName()) < 0) {
				break;  //error - no attr name
			}
			Object[] item = new Object[3];
			item[0] = _bf.substring(scanned, _pos); //name
			// is EQ
			scanSpaces();
			if (!isChar('=')) {
				break; //error - no eq
			}
			scanSpaces();
			item[1] = getBufferPosition1();
			if ((scanned = scanLiteral()) < 0) {
				break; // error no quoted literal
			}
			item[2] = _pos-1 > scanned +1
				? _bf.substring(scanned +1, _pos-1) : "";
			result.add(item);
			if (!"*".equals(qName)) {
				releaseScanned();
			}
		}
		// never should happen!
		if (!"*".equals(qName)) {
			releaseScanned();
		}
		return result;
	}

	/** Detect encoding from Byte Order Mark (BOM)
	 * (see http://www.w3.org/TR/REC-xml/#charsets).
	 * <UL>
	 * <LI>With a Byte Order Mark (BOM):
	 * <p>EF BB BF: UTF-8</p>
	 * <p>00 00 FE FF: UCS-4, big-endian machine (1234 order)</p>
	 * <p>FF FE 00 00: UCS-4, little-endian machine (4321 order)</p>
	 * <p>00 00 FF FE: UCS-4, unusual octet order (2143)</p>
	 * <p>FE FF 00 00: UCS-4, unusual octet order (3412)</p>
	 * <p>FE FF ## ##: UTF-16, big-endian</p>
	 * <p>FF FE ## ##: UTF-16, little-endian</p>
	 * </LI>
	 * <LI><p>Without a Byte Order Mark:</p>
	 * <b>00 00 00 3C, 3C 00 00 00, 00 00 3C 00, 00 3C 00 00:</b>
	 * <p>UCS-4 or other encoding with a 32-bit code unit and ASCII
	 * characters encoded as ASCII values, in respectively big-endian(1234),
	 * little-endian(4321) and two unusual byte orders (2143 and 3412).
	 * The encoding declaration must be read to determine which of UCS-4 or
	 * other supported 32-bit encodings applies.</p>
	 * <b>00 3C 00 3F</b>
	 * <p>UTF-16BE or big-endian ISO-10646-UCS-2 or other encoding with a
	 * 16-bit code unit in big-endian order and ASCII characters encoded as
	 * ASCII values (the encoding declaration must be read to determine
	 * which)</p>
	 * <b>3C 00 3F 00:</b>
	 * <p>UTF-16LE or little-endian ISO-10646-UCS-2 or other encoding with a
	 * 16-bit code unit in little-endian order and ASCII characters encoded
	 * as ASCII values (the encoding declaration must be read to determine
	 * which)</p>
	 * <b>3C 3F 78 6D:</b>
	 * <p>UTF-8, ISO 646, ASCII, some part of ISO 8859, Shift-JIS, EUC, or
	 * any other 7-bit, 8-bit, or mixed-width encoding which ensures that
	 * the characters of ASCII have their normal positions, width, and
	 * values; the actual encoding declaration must be read to detect which
	 * of these applies, but since all of these encodings use the same bit
	 * patterns for the relevant ASCII characters, the encoding declaration
	 * itself may be read reliably</p>
	 * <b>4C 6F A7 94:</b>
	 * <p>EBCDIC (in some flavor; the full encoding declaration must be read
	 * to tell which code page is in use)</p>
	 * <b>Other:</b>
	 * <p>UTF-8 without an encoding declaration, or else the data stream
	 * is mislabeled (lacking a required encoding declaration), corrupt,
	 * fragmentary, or enclosed in a wrapper of some kind.</p>
	 * </LI>
	 * </UL>
	 * @param in InputStream where to read.
	 * @param buf array of four bytes to which first bytes are read.
	 * @return character set name. First two characters have a special mesaning.
	 * The first character is number of read bytes and the second character
	 * is the number of bytes to read next character.
	 * @throws IOException if an IO error occurs.
	 */
	public static final String detectBOM(final InputStream in,
		final byte[] buf) throws IOException {
		int i1, i2, i3, i4;
		buf[0] = (byte) (i1 = in.read());
		if (i1 == -1) {
			return ((char)('0' - 1)) + "0UTF-8"; // first character - '0' = -1!
		}
		buf[1] = (byte) (i2 = in.read());
		if (i2 == -1) {
			return "11UTF-8"; // only one byte in the input stream -> 1
		}
		buf[2] = (byte) (i3 = in.read());
		if (i3 == -1) {
			return  (i1 == 0xFE && i2 == 0xFF) ? "02UTF-16BE" //BOM
				: (i1 == 0xFF && i2 == 0xFE) ?   "02UTF-16LE" //BOM
				: "22UTF-8";
		}
		if (i1 == 0xEF && i2 == 0xBB && i3 == 0xBF) {
			return "01UTF-8"; //BOM UTF-8 (3 bytes)
		}
		buf[3] = (byte) (i4 = in.read());
		if (i4 == -1) {
			return "31UTF-8";
		} else if (i1 == 0xFF && i2 == 0xFE) { // BOM
			if (i3 == 0 && i4 == 0) { // FF FE 00 00
				return "04UTF-32LE"; // BOM 4321, 4 bytes
			}
			buf[0] = buf[2]; // FF FE ## ## => UTF-16LE and skip two bytes
			buf[1] = buf[3];
			return "22UTF-16LE"; // BOM + '<'
		} else if (i1 == 0xFE && i2 == 0xFF) { // BOM
			if (i3 == 0 && i4 == 0) { // FE FF 00 00
				return "04X-ISO-10646-UCS-4-3412"; //BOM 3412
			}
			buf[0] = buf[2]; // FE FF ## ## => UTF-16BE and skip two bytes
			buf[1] = buf[3];
			return "22UTF-16BE"; // BOM + '<'
		} else if (i1 == 0 && i2 == 0) {
			return (i3 == 0xFE && i4 == 0xFF) ? "04UTF-32BE"  //BOM 1234
				: (i3 == 0xFF && i4 == 0xFE) ? "04X-ISO-10646-UCS-4-2143" //BOM
				: (i3 != 0 && i4 == 0) ? "44X-ISO-10646-UCS-4-2143" //00<0
				: (i3 == 0 && i4 != 0) ? "44UTF-32BE" // 000<
				: "41UTF-8"; // other
		} else if (i1 == 0 && i2 != 0) {
			return (i3 == 0 && i4 == 0) ? "44X-ISO-10646-UCS-4-3412" //0<00
				: "42UTF-16BE";
		} else if (i1 != 0 && i2 == 0) {
			return (i3 == 0 && i4 == 0) ? "44UTF-32LE" : "42UTF-16LE";
		} else if (i1 == 0x4C && i2 == 0x6F && i3 == 0xA7 && i4 == 0x94) {
			return "41CP037"; // EBCDIC ("<?xm")
		}
		return "41UTF-8"; // other
	}

}