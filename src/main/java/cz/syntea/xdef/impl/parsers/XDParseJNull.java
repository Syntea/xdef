package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefNull;

/** Parser of X-Script "jnull" type.
 * @author Vaclav Trojan
 */
public class XDParseJNull  extends XSAbstractParser {
	private static final String ROOTBASENAME = "jnull";

	public XDParseJNull() {
		super();
		_whiteSpace = 'c';
	}
	@Override
	public void initParams() {
		_whiteSpace = 'c';
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
//			ENUMERATION +
			WHITESPACE + //fixed collapse
//			MAXINCLUSIVE +
//			MAXEXCLUSIVE +
//			MININCLUSIVE +
//			MINEXCLUSIVE +
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
		if (!p.isToken("null")) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
		} else {
			String s = p.getParsedBufferPartFrom(pos);
			p.isSpaces();
			p.replaceParsedBufferFrom(pos0, s);
			p.setParsedValue(DefNull.genNullValue(XD_ANY));
			checkPatterns(p);
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_BOOLEAN;}
}
