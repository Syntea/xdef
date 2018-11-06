package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefDuration;

/** Parser of Schema "duration" type.
 * @author Vaclav Trojan
 */
public class XSParseDuration extends XSAbstractParseComparable {
	private static final String ROOTBASENAME = "duration";

	public XSParseDuration() {
		super();
		_whiteSpace = 'c';
	}

	@Override
	public  void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minExcl = _minIncl = _maxExcl = _maxIncl = null;
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to collapse
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
			BASE +
			0;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		int pos0 = p.getIndex();
		p.isSpaces();
		int pos = p.getIndex();
/*
-?P( ( ( [0-9]+Y([0-9]+M)?([0-9]+D)?
	   | ([0-9]+M)([0-9]+D)?
	   | ([0-9]+D)
	   )
	   (T ( ([0-9]+H)([0-9]+M)?([0-9]+(\.[0-9]+)?S)?
		  | ([0-9]+M)([0-9]+(\.[0-9]+)?S)?
		  | ([0-9]+(\.[0-9]+)?S)
		  )
	   )?
	)
  | (T ( ([0-9]+H)([0-9]+M)?([0-9]+(\.[0-9]+)?S)?
	   | ([0-9]+M)([0-9]+(\.[0-9]+)?S)?
	   | ([0-9]+(\.[0-9]+)?S)
	   )
	)
  )
*/
		StringParser parser = new StringParser(p.getSourceBuffer(), pos);
		if (!parser.isXMLDuration()) {
			p.error(XDEF.XDEF809, parserName());//Incorrect value of '&{0}'
			return;
		}
		p.setBufIndex(parser.getIndex());
		String s = p.getParsedBufferPartFrom(pos);
		p.setParsedValue(new DefDuration(parser.getParsedSDuration()));
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, s);
		checkPatterns(p);
		checkComparable(p);
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_DURATION;}
}