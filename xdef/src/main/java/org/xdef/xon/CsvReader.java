package org.xdef.xon;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;

/** This class contains methods for CSV data.
 * @author Vaclav Trojan
 */
public class CsvReader extends StringParser implements XonParsers {
	/** Flag if the parsed data are in X-definition (default false). */
	private boolean _jdef;
	/** Value separator character. */
	private char _separator = ',';
	/** if true the header line is skipped.*/
	private boolean _skipHeader;
	/** Parser of XON source. */
	private final XonParser _jp;

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public CsvReader(final SBuffer source, final XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public CsvReader(final String source, final XonParser jp) {
		super(source);
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source Reader with source data.
	 */
	public CsvReader(final Reader source, final XonParser jp) {
		super(source, new ArrayReporter());
		_jp = jp;
	}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source URL with source data.
	 */
	public CsvReader(final URL source, final XonParser jp) {
		super(source, new ArrayReporter(), 0);
		_jp = jp;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Parse CSV from reader
	 * @param in string with CSV data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @return list with parsed CSV data.
	 */
	public final static List<Object> parseCSV(final String in,
		final char separator,
		final boolean skipHeader) {
		return parseCSV(new StringReader(in), separator, skipHeader, "STRING");
	}

	@SuppressWarnings("unchecked")
	/** Parse CSV from reader
	 * @param in reader with CSV source data.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sysId system ID
	 * @return list with parsed CSV data.
	 */
	public final static List<Object> parseCSV(final Reader in,
		final char separator,
		final boolean skipHeader,
		final String sysId) {
		XonParser jp = new XonObjParser();
		CsvReader xr = new CsvReader(in, jp);
		xr._separator = separator;
		xr._skipHeader = skipHeader;
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.parse();
		xr.isSpaces();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return (List<Object>) jp.getResult();
	}
//
//	/** Convert index of column to column name.
//	 * @param index index of column.
//	 * @return column name.
//	 */
//	private static String genColumnName(final int index) {
//		String result = "";
//		int i = index;
//		for (;;) {
//			result = (char) ('A' + (i % ('Z' - 'A' + 1))) + result;
//			if ((i = i / ('Z' - 'A' + 1)) == 0) {
//				return result;
//			}
//			i--;
//		}
//	}
//
//	/** Get index of column from column name.
//	 * @param name column name.
//	 * @return  index of column.
//	 */
//	private static int getColumnIndex(final String name) {
//		int result = 0;
//		for (int i = 0; i < name.length(); i++) {
//			result = result*24 + name.charAt(i) - 'A';
//		}
//		return result;
//	}

	/** Put parsed value to XON parser.
	 * @param sb string with value.
	 * @param pos source position of parsed value.
	 */
	private void putValue(final StringBuilder sb, final SPosition pos) {
		if (sb.length() == 0) {
			_jp.putValue(new XonTools.JValue(pos, XonTools.JNULL));
		} else {
			_jp.putValue(new XonTools.JValue(pos, sb.toString().trim()));
			sb.setLength(0);
		}
	}

	/** Skip spaces and tabs. */
	private void skipLeadingSpaces() {
		while (isChar(' ') || isChar('\t')){} // skip leading spaces
	}

	/** Read line from CSV source. */
	private void readCSVLine() {
		StringBuilder sb = new StringBuilder();
		SPosition pos = getPosition();
		for (;;) {
			skipLeadingSpaces();
			if (isNewLine()) { // new line or end of source
				if (eos()) {
					return;
				}
				_jp.arrayStart(this);
				_jp.arrayEnd(this);
			} else {
				break;
			}
		}
		_jp.arrayStart(this);
		for (;;) {
			skipLeadingSpaces();
			char c = getCurrentChar();
			for (;;) {
				if (c == _separator) { // separator
					break;
				} else if (c == '\\') {
					c = peekChar();
					if (c == _separator || c == '"') {
						sb.append(c);
					} else {
						throw new RuntimeException("Escape character error");
					}
					c = getCurrentChar();
				} else if (c == '\"') {
					peekChar();
					if (eos()) {
						throw new RuntimeException("Quote character missing");
					}
					for(;;) {
						if (isChar('\"')) {
							if (isChar('\"')) {
								sb.append('\"');
							} else {
								break;
							}
						} else {
							sb.append(peekChar());
						}
					}
					c = getCurrentChar();
				} else if (c == '\n' || c == NOCHAR) {
					putValue(sb, pos);
					_jp.arrayEnd(this);
					isNewLine();
					return;
				} else {
					sb.append(c);
					c = nextChar();
				}
			}
			putValue(sb, pos);
			if(eos() || isNewLine()) {
				_jp.arrayEnd(this);
				return;
			}
			isSpaces();
			if (!isChar(_separator)) {
				throw new RuntimeException("incorrect line");
			}
		}
	}

	/** Read CSV data from source. */
	private void readCSV() {
		if (_skipHeader) {
			skipToNextLine();
			isNewLine();
		}
		_jp.arrayStart(this);
		while (!eos()) {
			readCSVLine();
		}
		_jp.arrayEnd(this);

	}

////////////////////////////////////////////////////////////////////////////////
// CVS to String
////////////////////////////////////////////////////////////////////////////////

	/** Create line with CSV data.
	 * @param csvLine the array with CSV data from a row.
	 * @param sb StringBuilder to which line is added.
	 * @param separator separator character.
	 */
	private static void addCsvLine(final List csvLine,
		final StringBuilder sb,
		final char separator) {
		boolean first = true;
		for (Object o: csvLine) {
			if (!first) {
				sb.append(separator);
			} else {
				first = false;
			}
			if (o != null) {
				String s = (String) o;
				if (s.indexOf('"') >= 0 || s.indexOf('"') >= 0) {
					s = '"' + SUtils.modifyString(s, "\"", "\"\"") + '"';
				}
				sb.append(s);
			}
		}
		sb.append('\n');
	}

	/** Create CSV string from CSV object (separator is comma).
	 * @param csv CSV object.
	 * @return CSV string created from CSV object.
	 */
	public final static String toCsvString(final List<Object> csv) {
		StringBuilder sb = new StringBuilder();
		for (Object o : csv) {
			if (o instanceof List) {
				addCsvLine((List) o, sb, ',');
			} else {
				throw new RuntimeException("Incorrect line: " + o);
			}
		}
		return sb.toString();
	}

	/** Create CSV string from CSV object (separator is declared by argument).
	 * @param csv CSV object.
	 * @return CSV string created from CSV object.
	 */
	public final static String toCsvString(final List<Object> csv,
		final char separator) {
		StringBuilder sb = new StringBuilder();
		for (Object o : csv) {
			if (o instanceof List) {
				addCsvLine((List) o, sb, separator);
			} else {
				throw new RuntimeException("Incorrect line: " + o);
			}
		}
		return sb.toString();
	}

////////////////////////////////////////////////////////////////////////////////
// CSV to XML
////////////////////////////////////////////////////////////////////////////////

	/** Create XML element with CSV data.
	 * @param csv object with CSV data.
	 * @return Element created from CSV data.
	 */
	public final static Element csvToXml(final List csv) {
		Document doc = KXmlUtils.newDocument(null, "csv", null);
		Element root = doc.getDocumentElement();
		StringBuilder sb = new StringBuilder("[\n");
		boolean first = true;
		for (Object o : csv) {
			if (first) {
				first = false;
			} else {
				sb.append(",\n");
			}
			sb.append(XonUtils.toXonString(o));
		}
		root.appendChild(doc.createTextNode(sb.toString()+ "\n]"));
		return root;
	}

////////////////////////////////////////////////////////////////////////////////
// XML to CSV
////////////////////////////////////////////////////////////////////////////////

	/** Create CSV object from XML element.
	 * @param el element from which the CSV object is created.
	 * @return created CSV object.
	 */
	public final static List<Object> xmlToCsv(final Element el) {
		String s = el.getTextContent();
		return (List) XonUtils.parseXON(s);
	}

////////////////////////////////////////////////////////////////////////////////
// interface XONParsers
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Parse INI/Properties source data.
	 * @throws SRuntimeException if an error occurs,
	 */
	public final void parse() throws SRuntimeException {
		readCSV();
		if (!eos()) {
			error(JSON.JSON008);//Text after JSON not allowed
		}
	}
	@Override
	/** Set mode that INI file is parsed in X-definition compiler. */
	public final void setXdefMode() { _jdef = true;}
	@Override
	public final void setXonMode() {} // not used
	@Override
	public void setJsonMode() {} // not used
}
