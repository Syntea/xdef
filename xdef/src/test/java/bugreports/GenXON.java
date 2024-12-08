package bugreports;

import java.util.HashMap;
import java.util.Map;
import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import org.xdef.xon.XonTools;
import org.xdef.xon.XonUtils;

/** Generate JSON from JSON model.
 * @author Vaclav Trojan
 */
public final class GenXON {

	private static void test(final String json) {
		String s = JsonModelToJson.parse(json, "STRING"); // to JSON conversion
		XonUtils.parseXON(s.trim());// just test syntax
		String t = JsonToJsonModel.parse(s, "STRING");
		if (!json.trim().equals(t.trim())) { //????
			System.out.println("***\n" + json);
			System.out.println("***\n" + s);
			System.out.println("***\n" + t);
			JsonModelToJson.parse(t, "STRING"); // test re-converted result
		}
	}

	/** Run test and display error information. */
	public static void main(String[] args) {
//		test("{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf= \" ref test\"]},\"jvalue();\"]}");
//		if(true) return;
		test(" { %anyName: %anyObj=\"*;\" } ");
		/////////////////////////////////////////
		test("{\"Genre\":[%oneOf,\"string()\",[\"occurs *; string()\"]]}");
		/////////////////////////////////////////
		test("[\n   [ %script = \"occurs 3\", \"occurs 3 jvalue()\" ]\n]");
		/////////////////////////////////////////
		test(" { %anyName: %anyObj=\"*;\" } ");
		/////////////////////////////////////////
		test("{%anyName:[%oneOf,[\"* jvalue();\" ],{%anyName:[%oneOf = \"ref test\"]},\"jvalue();\"]}");
		/////////////////////////////////////////
		test("{%anyName:[%oneOf,\"string()\",[\"occurs *; string()\"]]}");
		/////////////////////////////////////////
		test("/** Test */{%oneOf=\"optional;\",\"manager\":\"string()\",\"subordinates\":[\"* int();\"]}");
		/////////////////////////////////////////
		test("[%oneOf,\n"+
"    \"jvalue();\",\n"+
"    [\"* jvalue();\" ],\n"+
"    {%anyName:\n"+
"       [%oneOf,\n"+
"         \"jvalue();\",\n"+
"         [\"* jvalue();\" ],\n"+
"         {%anyName: [%oneOf=\" ref test\"]}\n"+
"       ]\n"+
"    }\n"+
"]");
		/////////////////////////////////////////
		test("/* xxx */ [\n"+
"  \"? jvalue()\",\n"+
"  { %script= \"occurs 1..*;\",\n"+
"    \"jmeno\": \"string();\",\n"+
"    \"IP\": [ %script=\"?\",\n"+
"             [ %script=\"*\", \"* ipAddr();\"]]\n"+
"  }\n"+
"]" +
" # yyy");
		/////////////////////////////////////////
		test("{ \"cities\"  : [\n" +
"    {%script=\"occurs 1..*\",\n" +
"      \"from\": [\n" +
"         \"string()\",\n" +
"         {%script=\"occurs 1..*\", \"to\": \"jstring()\", \"distance\": \"int()\" }\n" +
"	  ]\n" +
"    }\n" +
"  ]\n" +
"}");
	}

}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Calss with methods for conversions X-definition JSON model -> JSON, and JSON -> X-definition JSON model
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class JsonXDConversionsBase extends StringParser {
	final StringBuilder _sb; // here is result string

	/** Create instance of JsonModelToJson.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public JsonXDConversionsBase(final String source) {
		super(source, new ArrayReporter());
		_sb = new StringBuilder();
	}

	/** Write character to result.
	 * @param ch character to be written.
	 */
	final void out(final char ch) {_sb.append(ch);}

	/** Write string to result.
	 * @param s String to be written.
	 */
	final void out(final String s) {
		if (s != null && !s.isEmpty()) {
			_sb.append(s);
		}
	}

	/** Write parsed string from given positin to result.
	 * @param pos source position wrom which to copy data,
	 */
	final void out(final int pos) {
		out(getParsedBufferPartFrom(pos));
	}

	/** Write parsed white spaces and comments. */
	final void writeSpacesOrComments() {
		out(skipSpacesOrComments());
	}

	/** Skip white spaces and comments.
	 * @return string with parsed spaces and comments.
	 */
	final String skipSpacesOrComments() {
		int pos = getIndex();
		for (;;) {
			isSpaces();
			boolean wasLineComment;
			if ((wasLineComment = isChar('#')) || isToken("/*")) {
				if (wasLineComment) {
					while (!isNewLine() && !eos()) {
						nextChar();
					}
				} else {
					while (!isToken("*/") && !eos()) {
						if (eos()) {
							error(JSON.JSON015); //Unclosed comment
							return getParsedBufferPartFrom(pos);
						}
						nextChar();
					}
				}
			} else {
				return getParsedBufferPartFrom(pos);
			}
		}
	}

	/** Read simple simpleValue (here it can be only string). */
	final void readSimpleValue() {
		int pos = getIndex();
		if (isChar('"')) { // string
			XonTools.readJString(this);
			return;
		}
		setIndex(pos); // error
		error(JSON.JSON010, "[]{}"); //JSON simpleValue expected
	}
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Conversion JSON -> X-definition JSON model
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class JsonToJsonModel extends JsonXDConversionsBase {

	/** Create instance of JsonModelToJson.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public JsonToJsonModel(final String source) {
		super(source);
	}

	/** Read XON/JSON map.*/
	private void readMap() {
		Map<String, String> map = new HashMap<>();
		writeSpacesOrComments();
		if (isChar('}')) { // empty map
			out('}');
			return;
		}
		boolean wasItem = false;
		boolean wasAnyName = false;
		while(!eos()) {
			int i = isOneOfTokens('"' + SCRIPT_DIRECTIVE +'"', '"' + ONEOF_DIRECTIVE + '"');
			if (i >= 0) {
				out(new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE}[i]);
				writeSpacesOrComments();
				isChar(':');
				out('=');
				writeSpacesOrComments();
				if (isChar('"')) {
					out('"');
					int pos = getIndex();
					XonTools.readJString(this);
					out(pos);
				}
				writeSpacesOrComments();
				if (isChar(',')) {
					out(',');
					writeSpacesOrComments();
				} else if (isChar('}')) {
					out('}');
					return;
				}
				continue;
			}
			if (wasItem || i < 0) {
				wasItem = true;
				String name;
				if (isToken('"' + ANY_NAME + '"')) {
					if (wasAnyName) {
						 //Value pair &{0} already exists
						error(JSON.JSON022, new SBuffer(ANY_NAME, getPosition()));
					}
					wasAnyName = true;
					out(ANY_NAME);
					name = null;
				} else {
					int pos = getIndex();
					if (isChar('"')) {
						name = XonTools.readJString(this);
						out(pos);
					} else if (isNCName(StringParser.XMLVER1_0)) {
						name = getParsedString();
						out(pos);
					} else {
						error(JSON.JSON004); //Name of item expected
						setEos();
						return;
					}
				}
				writeSpacesOrComments();
				if (!isChar(':')) {
					error(JSON.JSON002, ":"); //"&{0}"&{1}{ or "}{"} expected
				}
				out(':');
				if (name != null) {
					if (map.put(name, "") != null) {
						error(JSON.JSON022, name); //Value pair &{0} already exists
					}
				}
				readItem();
			}
			writeSpacesOrComments();
			if (isChar('}')) {
				out('}');
				writeSpacesOrComments();
				return;
			}
			if (isChar(',')) {
				out(',');
				writeSpacesOrComments();
				if (isChar('}')) {
					out('}');
					return;
				}
			} else {
				if (eos()) {
					break;
				}
				error(JSON.JSON002, ",", "}");//"&{0}"&{1}{ or "}{"} expected
				if (getCurrentChar() != '"') {
					break;
				}
			}
		}
		fatal(JSON.JSON002, "}");//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
	}

	/** Read a directive.
	 * @return true if a directive was read.
	 */
	private int readDirective() {
		int i = isOneOfTokens('"' + SCRIPT_DIRECTIVE, '"' + ONEOF_DIRECTIVE);
		if (i >= 0) {
			out(new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE}[i]);
			writeSpacesOrComments();
			if (isChar('=')) {
				out('=');
				out('"');
				writeSpacesOrComments();
				int pos = getIndex();
				XonTools.readJString(this);
				out(pos);
			} else {
				isChar('"');
			}
		}
		return i;
	}

	/** Read JSON/XON array. */
	private void readArray() {
		writeSpacesOrComments();
		if (isChar(']')) { // empty array
			out(']');
			return;
		}
		boolean wasItem = false;
		boolean wasErrorReported = false;
		while(!eos()) {
			if (wasItem || readDirective() < 0) {
				readItem();
				wasItem = true;
			}
			writeSpacesOrComments();
			if (isChar(']')) {
				out(']');
				return;
			}
			if (isChar(',')) {
				out(',');
				writeSpacesOrComments();
				if (isChar(']')) {
					out(']');
					return;
				}
			} else {
				if (wasErrorReported) {
					break;
				}
				error(JSON.JSON002,",","]"); //"&{0}"&{1}{ or "}{"} expected
				if (eos()) {
					break;
				}
				wasErrorReported = true;
			}
		}
		error(JSON.JSON002, "]"); //"&{0}"&{1}{ or "}{"} expected
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
	}

	/** Read XON/JSON item. */
	private void readItem() {
		writeSpacesOrComments();
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			out('{');
			readMap();
		} else if (isChar('[')) {
			out('[');
			readArray();
		} else if (isToken('"' + ANY_OBJ)) {
			out(ANY_OBJ + "=\"");
			while(!isChar('"') && !eos()) {
				out(getCurrentChar());
				nextChar();
			}
			out('"');
		} else {
			int pos = getIndex();
			readSimpleValue();
			out(pos);
		}
	}

	/** Parse X-definition json model.
	 * @param in Reader with XON/JSON source data.
	 * @param sysId System ID of source position or null.
	 * @return parsed XON or JSON object.
	 */
	public final static String parse(final String source, final String sysId) {
		JsonToJsonModel xr = new JsonToJsonModel(source);
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.readItem();
		xr.writeSpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return xr._sb.toString();
	}
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Conversion X-definition JSON model -> JSON
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
class JsonModelToJson extends JsonXDConversionsBase {

	/** Create instance of JsonModelToJson.
	 * @param jp parser of XON source.
	 * @param source Reader with source data.
	 */
	public JsonModelToJson(final String source) {
		super(source);
	}

	/** Read a directive.
	 * @return true if a directive was read.
	 */
	private int readDirective(final char sepChar) {
		final String[] directives = new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE};
		int i = isOneOfTokens(directives);
		if (i >= 0) {
			String name = directives[i];
			out('"' + name);
			writeSpacesOrComments();
			if (isChar('=')) {
				if (sepChar == ':') {
					out('"');
				}
				out(sepChar);
				writeSpacesOrComments();
				isChar('"');
				if (sepChar == ':') {
					out('"');
				}
				int pos = getIndex();
				XonTools.readJString(this);
				out(pos);
			} else {
				out('"');
			}
		}
		return i;
	}

	/** Read map. */
	private void readMap() {
		Map<String, String> map = new HashMap<>();
		writeSpacesOrComments();
		if (isChar('}')) { // empty map
			out(')');
			return;
		}
		boolean wasItem = false;
		boolean wasAnyName = false;
		while (!eos()) {
			if (wasItem || readDirective(':') < 0) {
				wasItem = true;
				String name;
				SPosition spos = getPosition();
				if (isToken(ANY_NAME)) {
					if (wasAnyName) {
						error(JSON.JSON022, new SBuffer(ANY_NAME, spos)); //Value pair &{0} already exists
					}
					out('"' + ANY_NAME + '"');
					wasAnyName = true;
					writeSpacesOrComments();
					name = null;
				} else {
					if (isChar('"')) {
						out('"');
						int pos = getIndex();
						name = XonTools.readJString(this);
						out(pos);
					} else if (isNCName(StringParser.XMLVER1_0)) {
						out(name = getParsedString());
					} else {
						error(JSON.JSON004); //Name of item expected
						setEos();
						return;
					}
				}
				writeSpacesOrComments();
				if (!isChar(':')) {
					error(JSON.JSON002, ":"); //"&{0}"&{1}{ or "}{"} expected
				} else {
					out(':');
				}
				if (name != null) {
					if (map.put(name, "") != null) {
						error(JSON.JSON022, name); //Value pair &{0} already exists
					}
				}
				readItem();
			}
			writeSpacesOrComments();
			if (isChar('}')) {
				out('}');
				writeSpacesOrComments();
				return;
			}
			if (isChar(',') || true) {
				out(',');
				writeSpacesOrComments();
				if (isChar('}')) {
					out('}');
					return;
				}
			} else {
				if (eos()) {
					break;
				}
				error(JSON.JSON002, ",", "}");//"&{0}"&{1}{ or "}{"} expected
				if (getCurrentChar() != '"') {
					break;
				}
			}
		}
		fatal(JSON.JSON002, "}");//"&{0}"&{1}{ or "}{"} expected&{#SYS000}
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
	}

	/** Read array. */
	private void readArray() {
		writeSpacesOrComments();
		if (isChar(']')) { // empty array
			out(']');
			return;
		}
		boolean wasItem = false;
		boolean wasErrorReported = false;
		while (!eos()) {
			if (wasItem || readDirective('=') < 0) {
				readItem();
				wasItem = true;
			}
			writeSpacesOrComments();
			if (isChar(']')) {
				out(']');
				return;
			}
			if (isChar(',')) {
				out(',');
				writeSpacesOrComments();
				if (isChar(']')) {
					out(']');
					return;
				}
			} else {
				if (wasErrorReported) {
					break;
				}
				error(JSON.JSON002, ",", "]"); //"&{0}"&{1}{ or "}{"} expected
				if (eos()) {
					break;
				}
				wasErrorReported = true;
			}
		}
		error(JSON.JSON002, "]"); //"&{0}"&{1}{ or "}{"} expected
		if (findOneOfChars("[]{}") == NOCHAR) {// skip to next item
			setEos();
		}
	}

	/** Read item. */
	private void readItem() {
		writeSpacesOrComments();
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			out('{');
			readMap();
		} else if (isChar('[')) {
			out('[');
			readArray();
		} else if (isToken(ANY_OBJ)) {
			SPosition spos = getPosition(); // xdef %anyObj
			spos.setIndex(getIndex() - ANY_OBJ.length());
			out('"' + ANY_OBJ);
			writeSpacesOrComments();
			if (isChar('=')) {
				writeSpacesOrComments();
				int pos = getIndex();
				readSimpleValue();
				out(pos+1);
			} else {
				out('"');
			}
		} else {
			int pos = getIndex();
			readSimpleValue();
			out(pos);
		}
	}

	/** Parse X-definition json model.
	 * @param in Reader with XON/JSON source data.
	 * @param sysId System ID of source position or null.
	 * @return parsed XON or JSON object.
	 */
	public final static String parse(final String source, final String sysId) {
		JsonModelToJson xr = new JsonModelToJson(source);
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.readItem();
		xr.writeSpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return xr._sb.toString();
	}
}
