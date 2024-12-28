package bugreports;

import org.xdef.msg.JSON;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.StringParser;
import org.xdef.xon.XonTools;

/** Methods used for conversions X-definition JSON model -> JSON, and JSON -> X-definition JSON model. */
public class GenXCommon extends StringParser {
	final StringBuilder _sb; // here is result string

	/** Create instance of JsonModelToJson.
	 * @param source String with source data.
	 */
	public GenXCommon(final String source) {
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

	/** Write parsed string from given position to result.
	 * @param pos source position from which to copy data,
	 */
	final void out(final int pos) {
		out(getParsedBufferPartFrom(pos));
	}

	/** Copy parsed white spaces and comments to out. */
	final void copySpacesOrComments() {
		out(readSpacesOrComments());
	}

	/** Read white spaces and comments.
	 * @return string with parsed spaces and comments.
	 */
	final String readSpacesOrComments() {
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

	/** Read simple simpleValue (here it can be only string!). */
	final void readStringValue() {
		if (isChar('"')) { // string
			XonTools.readJString(this);
			return;
		}
		error(JSON.JSON010, "[]{}"); //JSON simpleValue expected
	}
}