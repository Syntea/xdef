package bugreports;

import java.util.HashMap;
import java.util.Map;
import org.xdef.msg.JSON;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import org.xdef.xon.XonTools;


/** Conversion JSON -> X-definition JSON model. */
public class GenXJsonToJsonModel extends GenXCommon {

	/** Create instance of JsonModelToJson.
	 * @param source String with source data.
	 */
	public GenXJsonToJsonModel(final String source) {super(source);}

	/** Read XON/JSON map.*/
	private void readMap() {
		Map<String, String> map = new HashMap<>();
		copySpacesOrComments();
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
				String s = readSpacesOrComments();
				isChar(':');
				s += readSpacesOrComments();
				if (s.length() > 0) {
					out(s.substring(0, 1));
				}
				out('=');
				if (s.length() > 1) {
					out(s.substring(1));
				}
				if (isChar('"')) {
					out('"');
					int pos = getIndex();
					XonTools.readJString(this);
					out(pos);
				}
				copySpacesOrComments();
				if (isChar(',')) {
					out(',');
					copySpacesOrComments();
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
				copySpacesOrComments();
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
			copySpacesOrComments();
			if (isChar('}')) {
				out('}');
				copySpacesOrComments();
				return;
			}
			if (isChar(',')) {
				out(',');
				copySpacesOrComments();
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

	/** Read JSON/XON array. */
	private void readArray() {
		copySpacesOrComments();
		if (isChar(']')) { // empty array
			out(']');
			return;
		}
		boolean wasItem = false;
		boolean wasErrorReported = false;
		while(!eos()) {
			int i = isOneOfTokens('"' + SCRIPT_DIRECTIVE, '"' + ONEOF_DIRECTIVE);
			if (i >= 0) {
				out(new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE}[i]);
				String s = readSpacesOrComments();
				if (isChar('=')) {
					if (s.length() > 0) {
						out(s.substring(0,1));
					}
					out('=');
					if (s.length() > 1) {
						out(s.substring(1));
					}
					out('"');
					copySpacesOrComments();
					int pos = getIndex();
					XonTools.readJString(this);
					out(pos);
				} else {
					isChar('"');
					out(s);
				}
			}
			if (wasItem || i < 0) {
				readItem();
				wasItem = true;
			}
			copySpacesOrComments();
			if (isChar(']')) {
				out(']');
				return;
			}
			if (isChar(',')) {
				out(',');
				copySpacesOrComments();
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
		copySpacesOrComments();
		if (eos()) {
			fatal(JSON.JSON007); //unexpected eof
		} else if (isChar('{')) { // Map
			out('{');
			readMap();
		} else if (isChar('[')) {
			out('[');
			readArray();
		} else if (isToken('"' + ANY_OBJ)) {
			out(ANY_OBJ);
			copySpacesOrComments();
			String s = "";
			while(!isChar('"') && !eos()) {
				s += getCurrentChar();
				nextChar();
			}
			if (!s.isEmpty()) {
				out("=\"" + s + '"');
			}
		} else {
			int pos = getIndex();
			readStringValue();
			out(pos);
		}
	}

	/** Parse X-definition json model.
	 * @param source String with XON/JSON source data.
	 * @param sysId System ID of source position or null.
	 * @return parsed XON or JSON object.
	 */
	public final static String parse(final String source, final String sysId) {
		GenXJsonToJsonModel xr = new GenXJsonToJsonModel(source);
		if (sysId != null) {
			xr.setSysId(sysId);
		}
		xr.readItem();
		xr.copySpacesOrComments();
		if (!xr.eos()) {
			xr.error(JSON.JSON008);//Text after JSON not allowed
		}
		xr.getReportWriter().checkAndThrowErrorWarnings();
		return xr._sb.toString();
	}
}