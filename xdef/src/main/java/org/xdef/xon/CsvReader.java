package org.xdef.xon;

import java.io.Reader;
import java.net.URL;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;

/** Methods for CSV data.
 * @author Vaclav Trojan
 */
public class CsvReader extends StringParser implements XonParsers {
	/** Value separator character. */
	private char _separator = ',';
	/** if true the header line is skipped.*/
	private boolean _skipHeader;
	/** Parser of XON source. */
	private final XonParser _xp;

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public CsvReader(final SBuffer source, final XonParser jp) {super(source); _xp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source String with source data.
	 */
	public CsvReader(final String source, final XonParser jp) {super(source); _xp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source Reader with source data.
	 */
	public CsvReader(final Reader source, final XonParser jp) {super(source, new ArrayReporter()); _xp = jp;}

	/** Create instance of parser.
	 * @param jp parser of INI/Properties source.
	 * @param source URL with source data.
	 */
	public CsvReader(final URL source, final XonParser jp) {super(source, new ArrayReporter(), 0); _xp = jp;}

////////////////////////////////////////////////////////////////////////////////

	/** Parse CSV from reader
	 * @param source string with CSV data, filename, URL, File
	 * @param separator value separator character.
	 * @param skipHdr if true the header line is skipped.
	 * @return list with parsed CSV data.
	 */
	public static final List<Object> parseCSV(final Object source,final char separator,final boolean skipHdr){
		return parseCSV(source, separator, skipHdr, null);
	}

	@SuppressWarnings("unchecked")
	/** Parse CSV from reader
	 * @param source if it is string check file name, URL or input data otherwise it can be a File,
	 * InputStream or Reader.
	 * @param separator value separator character.
	 * @param skipHeader if true the header line is skipped.
	 * @param sysId System ID (or null).
	 * @return list with parsed CSV data.
	 */
	public static final List<Object> parseCSV(final Object source,
		final char separator,
		final boolean skipHeader,
		final String sysId) {
		XonTools.InputData indata = XonTools.getInputFromObject(source, sysId);
		XonParser jp = new XonObjParser(true);
		CsvReader xr = new CsvReader(indata._reader, jp);
		xr._separator = separator;
		xr._skipHeader = skipHeader;
		xr.setSysId(indata._sysId);
		xr.parse();
		xr.isSpaces();
		if (!xr.eos()) {
			xr.error(JSON.JSON008); //Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return (List<Object>) jp.getResult();
	}

	/** Put parsed value to XON parser.
	 * @param sb string with value.
	 * @param pos source position of parsed value.
	 */
	private void putValue(final StringBuilder sb, final SPosition pos) {
		if (sb.length() == 0) {
			_xp.putValue(new XonTools.JValue(pos, XonTools.JNULL));
		} else {
			_xp.putValue(new XonTools.JValue(pos, sb.toString().trim()));
			sb.setLength(0);
		}
	}

	/** Skip spaces and tabs. */
	private void skipLeadingSpaces() {
		while (isChar(' ') || isChar('\t') || isChar('\r') || isChar('\f')) {}
	}

	/** Read line from CSV source. */
	private void readCSVLine() {
		StringBuilder sb = new StringBuilder();
		SPosition pos = getPosition();
		for (;;) {
			skipLeadingSpaces();
			if (isNewLine()) { // new line or end of source
				_xp.arrayStart(this);
				_xp.arrayEnd(this);
				if (eos()) {
					return;
				}
			} else {
				break;
			}
		}
		_xp.arrayStart(this);
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
						throw new RuntimeException(	"CSV Escape character error");
					}
					c = getCurrentChar();
				} else if (c == '\"') {
					peekChar();
					if (eos()) {
						throw new RuntimeException("CSV Quote character missing");
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
				} else if (c == '\n' || eos()) {
					break;
				} else {
					sb.append(c);
					c = nextChar();
				}
			}
			putValue(sb, pos);
			if(eos() || isNewLine()) {
				_xp.arrayEnd(this);
				return;
			}
			isSpaces();
			if (!isChar(_separator)) {
				throw new RuntimeException("incorrect CSV line");
			}
		}
	}

	/** Read CSV data from source. */
	private void readCSV() {
		if (_skipHeader) {
			skipToNextLine();
			isNewLine();
		}
		_xp.arrayStart(this);
		while (!eos()) {
			readCSVLine();
		}
		_xp.arrayEnd(this);

	}

////////////////////////////////////////////////////////////////////////////////
// CVS to String
////////////////////////////////////////////////////////////////////////////////

	/** Create line with CSV data.
	 * @param csvLine the array with CSV data from a row.
	 * @param sb StringBuilder to which line is added.
	 * @param separator separator character.
	 */
	private static void addCsvLine(final List csvLine, final StringBuilder sb, final char separator) {
		boolean first = true;
		for (Object o: csvLine) {
			if (!first) {
				sb.append(separator);
			} else {
				first = false;
			}
			if (o != null) {
				String s = (o instanceof String) ? (String) o : o.toString();
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
	public static final String toCsvString(final List<Object> csv) {
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
	 * @param separator separator character.
	 * @return CSV string created from CSV object.
	 */
	public static final String toCsvString(final List<Object> csv, final char separator) {
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
	public static final Element csvToXml(final List csv) {
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

	@SuppressWarnings("unchecked")
	/** Create CSV object from XML element.
	 * @param el element from which the CSV object is created.
	 * @return created CSV object.
	 */
	public static final List<Object> xmlToCsv(final Element el) {
		String s = el.getTextContent();
		return (List<Object>) XonUtils.parseXON(s);
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
	public final void setXdefMode() {}
	@Override
	public final void setXonMode() {}
	@Override
	public void setJsonMode() {} // not used
}