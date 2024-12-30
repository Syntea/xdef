package bugreports;

import java.util.HashMap;
import java.util.Map;
import org.xdef.msg.JSON;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;
import org.xdef.sys.StringParser;
import static org.xdef.xon.XonNames.ANY_NAME;
import static org.xdef.xon.XonNames.ANY_OBJ;
import static org.xdef.xon.XonNames.ONEOF_DIRECTIVE;
import static org.xdef.xon.XonNames.SCRIPT_DIRECTIVE;
import org.xdef.xon.XonTools;

/** Conversion X-definition JSON model -> JSON. */
public class GenXJsonModelToJson extends GenXCommon {

	/** Create instance of JsonModelToJson.
	 * @param source String with source data.
	 */
	public GenXJsonModelToJson(final String source) {super(source);}

	/** Read map. */
	private void readMap() {
		Map<String, String> map = new HashMap<>();
		copySpacesOrComments();
		if (isChar('}')) { // empty map
			out(')');
			return;
		}
		boolean wasItem = false;
		boolean wasAnyName = false;
		final String[] directives = new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE};
		while (!eos()) {
			int i = isOneOfTokens(directives);
			if (i >= 0) {
				String name = directives[i];
				out('"' + name + '"');
				copySpacesOrComments();
				if (isChar('=')) {
					copySpacesOrComments();
					isChar('"');
					out(':');
					out('"');
					int pos = getIndex();
					XonTools.readJString(this);
					out(pos);
				}
			}
			if (wasItem || i < 0) {
				wasItem = true;
				String name;
				SPosition spos = getPosition();
				if (isToken(ANY_NAME)) {
					if (wasAnyName) {
						error(JSON.JSON022, new SBuffer(ANY_NAME, spos)); //Value pair &{0} already exists
					}
					out('"' + ANY_NAME + '"');
					wasAnyName = true;
					copySpacesOrComments();
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
				copySpacesOrComments();
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
			copySpacesOrComments();
			if (isChar('}')) {
				out('}');
				copySpacesOrComments();
				return;
			}
			if (isChar(',') || true) {
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

	/** Read array. */
	private void readArray() {
		copySpacesOrComments();
		if (isChar(']')) { // empty array
			out(']');
			return;
		}
		boolean wasItem = false;
		boolean wasErrorReported = false;
		final String[] directives = new String[]{SCRIPT_DIRECTIVE, ONEOF_DIRECTIVE};
		while (!eos()) {
			int i = isOneOfTokens(directives);
			if (i >= 0) {
				String name = directives[i];
				out('"' + name);
				String s = readSpacesOrComments();
				if (isChar('=')) {
					s += readSpacesOrComments();
					isChar('"');
					out(s);
					out('=');
					int pos = getIndex();
					XonTools.readJString(this);
					out(pos);
				} else {
					out('"');
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
		copySpacesOrComments();
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
			String s = readSpacesOrComments();
			if (isChar('=')) {
				out(s);
				copySpacesOrComments();
				int pos = getIndex();
				readStringValue();
				out(pos+1);
			} else {
				out('"');
				out(s);
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
		GenXJsonModelToJson xr = new GenXJsonModelToJson(source);
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