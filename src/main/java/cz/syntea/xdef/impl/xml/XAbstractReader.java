package cz.syntea.xdef.impl.xml;

import cz.syntea.xdef.sys.SPosition;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/** provide abstract class for implementation of readres.
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

//	public final StringBuilder getBuf() {return _bf;}

	public final int getPos() {return _pos;}
//
//	public final String getUnparsed() {return _bf.substring(_pos);}

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
		scanSpaces();
		while (scanComment() >= 0 || scanPI() >= 0) {
			scanSpaces();
		}
		scanDoctype();
		scanSpaces();
		while (scanComment() >= 0 || scanPI() >= 0) {
			scanSpaces();
		}
		return _pos > start ? _bf.substring(start, _pos) : "";
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
//		return new SPosition(_pos, _line, _startLine, _filePos, _sysId);
		return new SPosition(0, _line, _startLine, _filePos + _pos, _sysId);
	}

	public final void setSPosition(SPosition p) {
		_pos = p.getIndex();
		_line = p.getLineNumber();
		_startLine = p.getStartLine();
		_filePos = p.getFilePos();
		_sysId = p.getSysId();
	}

	// markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment
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
	// doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S? ('[' intSubset ']' S?)? '>'
	// DeclSep ::= PEReference | S
	// PEReference ::= '%' Name ';'
	// intSubset ::= (markupdecl | DeclSep)*
	// markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment
	// extSubset ::= TextDecl? extSubsetDecl
	// extSubsetDecl ::= ( markupdecl | conditionalSect | DeclSep)*
	// SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'") | ('"' ('yes' | 'no') '"'))
	// STag ::= '<' Name (S Attribute)* S? '>'
	// Attribute ::= Name Eq AttValue
	// ETag ::= '</' Name S? '>'
	// content ::= CharData? ((element | Reference | CDSect | PI | Comment) CharData?)*
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
		scanSpaces();
		while (scanPI() >= 0 || scanComment() >= 0) {
			scanSpaces();
		}
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
			scanSpaces();
			if (scanLiteral() >= 0)
				scanSpaces();
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
		while (scanPI() >= 0 || scanComment() >= 0) {
			scanSpaces();
		}
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
		scanSpaces();
		// skip to the start of element
		while (scanPI() >= 0 || scanCDATA() >= 0 || scanComment() >= 0
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

}