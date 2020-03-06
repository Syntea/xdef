package org.xdef.impl.parsers;

import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.sys.StringParser;
import org.xdef.XDParseResult;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefString;
import org.xdef.XDContainer;

/** Parser of X-Script "xdatetime" type.
 * @author Vaclav Trojan
 */
public class XDParseXDatetime extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "xdatetime";
	private String _format;
	private String _outFormat;

	public XDParseXDatetime() {
		super();
//		_format = null;
//		_outFormat = null;
	}
	@Override
	public void initParams() {
		super.initParams();
		_format = null;
		_outFormat = null;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE +
			MAXINCLUSIVE +
			MAXEXCLUSIVE +
			MININCLUSIVE +
			MINEXCLUSIVE +
//			TOTALDIGITS +
//			FRACTIONDIGITS +
//			LENGTH +
//			MAXLENGTH +
//			MINLENGTH +
//			NORMALIZE +
//			SEPARATOR +
//			ITEM +
//			BASE +
			FORMAT +
			OUTFORMAT +
			0;
	}

	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!(parser.isDatetime(_format) && parser.testParsedDatetime())) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName() + " ("+_format+")");
			return;
		}
		SDatetime d = parser.getParsedSDatetime();
		//xdatetime errors
		p.addReports((ArrayReporter) parser.getReportWriter());
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		if (!d.chkDatetime()) {
			//Incorrect value of '&{0}'&{1}{: }
			p.errorWithString(XDEF.XDEF809, parserName());
			return;
		} else if (xnode != null && !xnode.getXDDocument().isLegalDate(d)) {
			//Range of values of year of date must be from &{0} to &{1}'
			p.error(XDEF.XDEF818, xnode.getXDDocument().getMinYear(),
				xnode.getXDDocument().getMaxYear());
			return;
		}
		p.isSpaces();
		if (_outFormat != null) {
			s = d.formatDate(_outFormat);
		}
		p.replaceParsedBufferFrom(pos0, s);
		p.setParsedValue(new DefDate(d));
		checkPatterns(p);
		checkComparable(p);
	}

	@Override
	/** Set named parameters.
	 * @param params context with named items of parameters.
	 * @param xnode actual XXNode object.
	 * @throws SException if an error occurs.
	 */
	public void setNamedParams(XXNode xnode, XDContainer params)
		throws SException {
		super.setNamedParams(xnode, params);
		if (_format == null) {
			//Missing required parameter: &{0}
			throw new SException(XDEF.XDEF545, "format");
		}
	}
	@Override
	public void setFormat(String x) {_format = x;}
	@Override
	public String getFormat() { return _format; }
	@Override
	public void setOutFormat(String x) {_outFormat = x;}
	@Override
	public String getOutFormat() { return _outFormat; }
	@Override
	public void addNamedParams(XDContainer map) {
		if (_format != null) {
			map.setXDNamedItem("format", new DefString(_format));
		}
		if (_outFormat != null) {
			map.setXDNamedItem("outFormat", new DefString(_outFormat));
		}
	}
	@Override
	public short parsedType() {return XD_DATETIME;}
	@Override
	public String parserName() {return ROOTBASENAME;}
}