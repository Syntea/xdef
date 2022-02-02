package org.xdef.xon;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import org.xdef.xml.KXmlUtils;

/** This class contains methods for CSV data.
 * @author Vaclav Trojan
 */
public class CsvUtil {

	/** Parse line from CSV file.
	 * @param line the string with line.
	 * @return array with values from the line.
	 */
	private static List<Object> readCsvLine(final String line) {
		List<Object> result = new ArrayList<Object>();
		StringParser p = new StringParser(line);
		StringBuilder sb = new StringBuilder();
		for (;;) {
			p.isSpaces();
			while (p.isChar(',')) {
				p.isSpaces();
				if (sb.length() == 0) {
					result.add(null);
				} else {
					result.add(sb.toString().trim());
					sb.setLength(0);
				}
				if(p.eos()) {
					result.add(null);
					return result;
				}
			} 
			if (sb.length() != 0) {
				result.add(sb.toString().trim());
				sb.setLength(0);
			}
			if(p.eos()) {
				return result;
			}
			char c = p.getCurrentChar();
			for (;;) {
				if (c == '\\') {
					c = p.peekChar();
					if (c == ',' || c == '"') {
						sb.append(c);
					} else {
						throw new RuntimeException("Escape character error");
					}
					c = p.getCurrentChar();
				} else if (c == '\"') {
					p.peekChar();
					if (p.eos()) {
						throw new RuntimeException("Quote character missing");
					}
					for(;;) {
						if (p.isChar('\"')) {
							if (p.isChar('\"')) {
								sb.append('\"');
							} else {
								break;
							}
						} else {
							sb.append(p.peekChar());
						}
					}
					c = p.getCurrentChar();
				} else if (c == NOCHAR || c == ',') {
					break;
				} else {
					sb.append(c);
					c = p.nextChar();
				}
			}
		}
	}

	/** Parse CSV from string.
	 * @param source reader with CSV source data.
	 * @return parsed object (array of rows).
	 */
	public final static List<Object> parseCsv(final String source) {
		return parseCsv(new StringReader(source));
	}

	/** Parse CSV from reader.
	 * @param in input stream with CSV source data.
	 * @param encoding encoding of input data.
	 * @return parsed object (array of rows).
	 */
	public final static List<Object> parseCsv(final InputStream in,
		final String encoding) {
		try {
			Reader r = encoding == null 
				?new InputStreamReader(in) : new InputStreamReader(in,encoding);
			return parseCsv(r);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Parse CSV from reader.
	 * @param in reader with CSV source data.
	 * @return parsed object (array of rows).
	 */
	public final static List<Object> parseCsv(final Reader in) {
		List<Object> result = new ArrayList<Object>();
		try {
			BufferedReader br = new BufferedReader(in);
			String line;
			while((line=br.readLine()) != null) {
				if (!(line = line.trim()).isEmpty()) {
					List<Object> row = readCsvLine(line);
					result.add(row);
				}
			}
			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}		
	}
	
	/** Create CSV object from XML element.
	 * @param el element from which the CSV object is created.
	 * @return created CSV object.
	 */
	public final static List<Object> xmlToCsv(final Element el) {
		List<Object> result = new ArrayList<Object>();
		Node node = el.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result.add(XonUtil.parseXON(node.getTextContent()));
			}
			node = node.getNextSibling();
		}
		return result;
	}

	/** Create line with CSV data.
	 * @param csvLine the array with CSV data from a row.
	 * @param sb StringBuilder to which line is added.
	 */
	private static void addCsvLine(List csvLine, StringBuilder sb) {
		boolean first = true;
		for (Object o: csvLine) {
			if (!first) {
				sb.append(',');
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
	
	/** Create CSV string from CSV object.
	 * @param csv CSV object.
	 * @return CSV string created from CSV object.
	 */
	public final static String toCsvString(final List csv) {
		StringBuilder sb = new StringBuilder();
		for (Object o : csv) {
			if (o instanceof List) {
				addCsvLine((List) o, sb);
			} else {
				throw new RuntimeException("Incorrect line: " + o);
			}
		}
		return sb.toString();
	}
	
	/** Create XML element with CSV data.
	 * @param csv object with CSV data.
	 * @return Element created from CSV data.
	 */
	public final static Element csvToXml(final List csv) {
		Document doc = KXmlUtils.newDocument(null, "csv", null);
		Element root = doc.getDocumentElement();
		for (Object o : csv) {
			Element row = doc.createElement("row");
			row.appendChild(
				doc.createTextNode(XonUtil.toJsonString(XonUtil.xonToJson(o))));
			root.appendChild(row);
		}
		return root;
	}
}
