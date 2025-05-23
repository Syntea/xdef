package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefString;
import org.xdef.XDContainer;
import static org.xdef.XDParser.ENUMERATION;
import static org.xdef.XDParser.LENGTH;
import static org.xdef.XDParser.MAXLENGTH;
import static org.xdef.XDParser.MINLENGTH;
import static org.xdef.XDParser.PATTERN;
import static org.xdef.XDParser.SEPARATOR;
import static org.xdef.XDParser.WHITESPACE;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_STRING;

/** Parser of X-script "NCNameList" type.
 * @author Vaclav Trojan
 */
public class XDParseNCNameList extends XSAbstractParseToken {
	private static final String ROOTBASENAME = "NCNameList";
	private String _separator;

	public XDParseNCNameList() {super();}

	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed collapse
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
			LENGTH +
			MAXLENGTH +
			MINLENGTH +
//			NORMALIZE +
//			ITEM +
//			BASE +
			SEPARATOR +
			0;
	}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		XDContainer results = new DefContainer();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		XDValue val;
		if ((val = parse(xnode, parser)) == null) {
			p.addReports((ArrayReporter) parser.getReportWriter());
			if (_minLength == 0 && _enumeration == null) {
				p.replaceParsedBufferFrom(pos0, "");
				p.setParsedValue(results);
				checkPatterns(p);
				return;
			}
			p.errorWithString(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'&{1}{: }
			return;
		}
		p.addReports((ArrayReporter) parser.getReportWriter());
		results.addXDItem(val);
		StringBuilder sb=new StringBuilder(parser.getParsedBufferPartFrom(pos));
		for (;;) {
			int pos1 = parser.getIndex();
			char separator;
			if (_separator != null) {
				if ((separator = parser.isOneOfChars(_separator)) == 0) {
					parser.setIndex(pos1);
					break;
				}
				while(parser.isOneOfChars(_separator) != 0) {}
			} else if (parser.isSpaces()) {
				separator = ' ';
			} else {
				parser.setIndex(pos1);
				break;
			}
			int pos2 = parser.getIndex();
			if ((val = parse(xnode, parser)) == null) {
				parser.setIndex(pos1);
				break;
			}
			sb.append(separator);
			sb.append(parser.getParsedBufferPartFrom(pos2));
			results.addXDItem(val);
		}
		p.setIndex(parser.getIndex());
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, sb.toString());
		p.setParsedValue(results);
		checkItem(p);
	}

	@Override
	public void addNamedParams(final XDContainer map) {
		if (_separator != null) {
			map.setXDNamedItem("separator", new DefString(_separator));
		}
	}

	XDValue parse(final XXNode xnode, final StringParser parser) {
		if (!parser.isNCName((byte) 10)) {
			return null;
		}
		return new DefString(parser.getParsedString());
	}

	@Override
	public void setSeparator(final String x) {_separator = x;}

	@Override
	public String getSeparator() {return _separator;}

	@Override
	public short parsedType() {return XD_CONTAINER;}

	@Override
	public String parserName() {return ROOTBASENAME;}

	@Override
	public short getAlltemsType() {return XD_STRING;}
}