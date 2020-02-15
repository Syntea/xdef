package org.xdef.impl.compile;

import org.xdef.msg.XDEF;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import java.util.Map;

/** Provides resolving of X-script macro calls.
 * @author Trojan
 */
public class XScriptMacroResolver extends StringParser {

	private static final int MAX_NESTED_MACRO = 64;

	private final String _actDefName;
	private final byte _xmlVersion;
	private final Map<String, XScriptMacro> _macros;

	public XScriptMacroResolver(final String actDefName,
		final byte xmlVersion,
		final Map<String, XScriptMacro> macros,
		final ReportWriter reporter) {
		super();
		setLineInfoFlag(true);
		setReportWriter(reporter);
		_xmlVersion = xmlVersion;
		_macros = macros;
		_actDefName = actDefName;
	}

	private boolean isXScriptName(final StringParser p) {
		if (p.getXmlCharType(_xmlVersion) != StringParser.XML_CHAR_NAME_START) {
			return false;
		}
		StringBuilder sb = new StringBuilder(String.valueOf(p.peekChar()));
		char c;
		boolean wasColon = false;
		while (StringParser.getXmlCharType(c = p.getCurrentChar(),
			_xmlVersion) == StringParser.XML_CHAR_NAME_START
			|| (c >= '0' && c <= '9') || (!wasColon && c == ':')) {
			if (c == ':') { // we allow one colon inside the name
				wasColon = true;
				c = p.nextChar();
				if (p.getXmlCharType(_xmlVersion)
					!= StringParser.XML_CHAR_NAME_START) {
					// must follow name, ignore ':'
					p.setBufIndex(p.getIndex() - 1);
					break;
				}
				sb.append(':');
			}
			sb.append(c);
			p.nextChar();
		}
		p.setParsedString(sb.toString());
		return true;
	}

	/** Parse macro references in the source text.
	 * @param nestingLevel level of nested reference (to prevent cycle).
	 * @param p String parser.
	 * @param macros Map with macro definitions.
	 * @return string with expanded macro references.
	 */
	private String parseMacro(int nestingLevel,
		final StringParser p) throws SRuntimeException {
		if (nestingLevel >= MAX_NESTED_MACRO) {
			macError(0, XDEF.XDEF486); //Too many nested macros
			return null;
		}
		if (!isXScriptName(p)) {
			 //Macro name error
			macError(nestingLevel, XDEF.XDEF484);
			return null;
		}
		String macName = p.getParsedString();
		if (p.isChar('#')) {
			if (!isXScriptName(p)) {
				//Macro name error
				macError(nestingLevel, XDEF.XDEF484);
				return null;
			}
			macName += '#' + p.getParsedString();
		} else {
			macName = _actDefName + '#' + macName;
		}
		XScriptMacro macro = _macros.get(macName);
		if (macro == null) {
			macName = macName.substring(macName.indexOf('#') + 1);
			if ((macro = _macros.get(macName)) == null) {
				//Macro '&{0}' doesn't exist
				macError(nestingLevel, XDEF.XDEF483,macName);
				return null;
			}
		}
		String[] params = null;
		StringBuilder sb;
		String s, s1;
		StringParser p1;
		int ndx;
		int level;
		p.skipSpaces();
		if (p.isChar('(')) { // parameters
			p.skipSpaces();
			while (isXScriptName(p)) {
				String parName = p.getParsedString();
				int index = macro.getParamNames().indexOf(parName);
				if (index < 0) {
					//Unknown parameter '&{0}' of macro '&{1}'
					macError(nestingLevel, XDEF.XDEF497, parName, macName);
					return null;
				}
				p.skipSpaces();
				if (!p.isChar('=')) {
					 //'&{0}' expected
					macError(nestingLevel, XDEF.XDEF410, "=");
					return null;
				}
				p.skipSpaces();
				char delimiter;
				if ((delimiter = p.isOneOfChars("'\"")) == NOCHAR) {
					//String specification expected
					macError(nestingLevel, XDEF.XDEF493);
					return null;
				}
				// parse the string constant, but we copy all escapes
				sb = new StringBuilder();
				for(;;) {
					if (!p.chkBufferIndex()) {
						//Unclosed string specification
						macError(nestingLevel, XDEF.XDEF403);
						return null;
					}
					char c;
					if ((c = p.peekChar()) == delimiter) {
						break;
					}
					if (c == '\\') {
						if (!p.chkBufferIndex()) {
							//Unclosed string specification
							macError(nestingLevel,XDEF.XDEF403);
							return null;
						}
						c = p.peekChar();
					}
					sb.append(c);
				}
				if (index >= 0) {
					if (params == null) {
						params = new String[macro.getParamValues().length];
						System.arraycopy(macro.getParamValues(),
							0, params, 0, params.length);
					}
					s = sb.toString();
					level = nestingLevel;
					while ((ndx = s.indexOf("${")) >= 0) {
						p1 = new StringParser(s.substring(ndx));
						p1.nextChar(); p1.nextChar(); //pos + 2
						s1 = parseMacro(++level, p1);
						if (s1 == null) {
							return null;
						}
						sb = new StringBuilder(s.substring(0,ndx));
						sb.append(s1);
						if (!p1.eos()) {
							sb.append(p1.getBufferPartFrom(p1.getIndex()));
						}
						s = sb.toString();
						sb.setLength(0);
					}
					params[index] = s;
				}
				p.skipSpaces();
				if (p.isChar(',')) {
					p.skipSpaces();
				} else {
					break;
				}
			}
			if (!p.isChar(')')) {
				 //'&{0}' expected
				macError(nestingLevel, XDEF.XDEF410, ")");
				return null;
			}
		}
		if (!p.isChar('}')) {
			//'&{0}' expected
			macError(nestingLevel, XDEF.XDEF410, "}");
			return null;
		}
		if (params == null) {
			params = macro.getParamValues();
		}
		s = macro.expand(params);
		level = nestingLevel;
		while ((ndx = s.indexOf("${")) >= 0) {
			p1 = new StringParser(s.substring(ndx));
			p1.nextChar(); p1.nextChar(); //pos + 2
			s1 = parseMacro(++level, p1);
			if (s1 == null) {
				return null;
			}
			sb = new StringBuilder(s.substring(0,ndx));
			sb.append(s1);
			if (!p1.eos()) {
				sb.append(p1.getBufferPartFrom(p1.getIndex()));
			}
			s = sb.toString();
		}
		return s;
	}

	/** Report macro error with modification.
	 * @param level nesting level.
	 * @param id registered message ID.
	 * @param mod Message modification parameters.
	 */
	private static void macError(final int level,
		final long id,
		final Object... mod) {
		if (level > 1) {
			return;
		}
		if (level == 1) {
			//Error in nested macro
			throw new SRuntimeException(XDEF.XDEF498);
		} else {
			throw new SRuntimeException(id, mod);
		}
	}

	public final void expandMacros(final SBuffer sb) {
		setSourceBuffer(sb);
		int ndx;
		if ((ndx = sb.getString().lastIndexOf("${")) < 0 ||
			ndx + 3 >= sb.getString().length()) {
			return; //no macro
		}
		setSourceBuffer(sb);
		int savedPos = getIndex();
		long savedLine = getLineNumber();
		long savedStartLine = getStartLine();
		long savedFilePos = getFilePos();
		//Expand macro references (backward from the end of buffer).
		try {
			do {
				setBufIndex(ndx + 2);
				// Parse macro
				String replacement = parseMacro(0, this);
				if (replacement == null) {
					break; //an error detected, finish
				}
				// Replace the macro reference with result
				changeBuffer(ndx, getIndex() - ndx, replacement, true);
				// check if the new macro occurred after this replacement
				int level = 1;
				while (--ndx >= 0 && getCharAtPos(ndx) == '$' &&
					replacement.startsWith("{")) {
					// A new nested macro occurred after this replacement
					// Set parser positinon at the macro
					if (ndx + 2 < getEndBufferIndex()) {
						setBufIndex(ndx + 2);
					} else {
						setEos();
					}
					// Parse macro
					replacement = parseMacro(level++, this);
					if (replacement == null) {
						break; //an error detected, finish
					}
					// Replace the macro reference with result
					changeBuffer(ndx, getIndex() - ndx, replacement, true);
					if (!chkBufferIndex()) {
						setEos();
					}
				}
			} while ((ndx = getSourceBuffer().lastIndexOf("${")) >= 0 &&
				ndx + 3 < getEndBufferIndex());
		} catch (SRuntimeException ex) {
			putReport(ex.getReport());
		}
		//reset parser to original position
		setLineNumber(savedLine);
		setStartLine(savedStartLine);
		setFilePos(savedFilePos);
		setBufIndex(savedPos);
		sb.setPosition(this);
		sb.setString(getSourceBuffer());
	}

	/**	Expands all macro references in the source buffer.
	 * @param sb source buffer.
	 * @param actDefName name of the actual X-definition where macro call
	 * is specified
	 * @param reporter reporter where to write errors.
	 * @param macros map with macro definitions
	 * @param xmlVersion version of XML (10 .. "1.0", 11 .. "1.0";
	 * see org.xdef.impl.XConstants.XMLxx).
	 */
	public static void expandMacros(final SBuffer sb,
		final String actDefName,
		final ReportWriter reporter,
		final byte xmlVersion,
		final Map<String, XScriptMacro> macros) {
		XScriptMacroResolver p =
			new XScriptMacroResolver(actDefName, xmlVersion, macros, reporter);
		p.expandMacros(sb);
	}

}