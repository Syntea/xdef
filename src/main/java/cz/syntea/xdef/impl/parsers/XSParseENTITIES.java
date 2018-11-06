package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.impl.code.DefString;

/** Parser of Schema "ENTITIES" type.
 * @author Vaclav Trojan
 */
public class XSParseENTITIES extends XSAbstractParseString {
	private static final String ROOTBASENAME = "ENTITIES";

	public XSParseENTITIES() {
		super();
		_whiteSpace = 'c';
	}
	@Override
	public int getLegalKeys() {
		return PATTERN +
			ENUMERATION +
			WHITESPACE + //fixed to collapse
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
//			SEPARATOR +
//			ITEM +
			BASE +
			+0;
	}
	@Override
	public  void initParams() {
		_whiteSpace = 'c';
		_patterns = null;
		_enumeration = null;
		_minLength = _maxLength = -1;
	}
	@Override
	public byte getDefaultWhiteSpace() {return 'c';}
	@Override
	public void check(final XXNode xnode, final XDParseResult p) {
		parse(xnode, p, true);
	}
	@Override
	public void parseObject(final XXNode xnode, final XDParseResult p){
		parse(xnode, p, false);
	}
	public void parse(final XXNode xnode,
		final XDParseResult p,
		final boolean isFinal) {
		int pos0 = p.getIndex();
		p.isSpaces();
		DefContainer val = new DefContainer();
		p.setParsedValue(val);
		String token = p.nextToken();
		if (token == null || !StringParser.chkXMLName(token, (byte) 10)) {
			p.error(XDEF.XDEF809, parserName()); //Incorrect value of '&{0}'
			return;
		}
		val.addXDItem(new DefString(token));
		StringBuilder sb = new StringBuilder(token);
		for (;;) {
			if (!p.isSpaces()) {
				break;
			}
			int pos = p.getIndex();
			token = p.nextToken();
			if (token != null && StringParser.chkXMLName(token, (byte) 10)) {
				sb.append(' ').append(token);
				val.addXDItem(new DefString(token));
			} else {
				p.setBufIndex(pos);
				break;
			}
		}
		p.isSpaces();
		p.replaceParsedBufferFrom(pos0, sb.toString());
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(val)){
					found = true;
					break;
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of '&{0}'
				p.error(XDEF.XDEF810, parserName());
			}
		}
		if (_minLength!=-1 && val.getXDItemsNumber() < _minLength) {
			//Length of value of '&{0}' is too short
			p.error(XDEF.XDEF814, parserName());
		} else if (_maxLength!=-1 && val.getXDItemsNumber() > _maxLength) {
			//Length of value of '&{0}' is too long
			p.error(XDEF.XDEF815,parserName());
		}
		if (_enumeration != null) {
			boolean found = false;
			for (int i = 0; i < _enumeration.length; i++) {
				if (_enumeration[i].equals(p.getParsedValue())){
					found = true;
					break;
				}
			}
			if (!found) {
				//Doesn't fit enumeration list of '&{0}'
				p.error(XDEF.XDEF810, parserName());
			}
		}
		if (_minLength != -1 && val.getXDItemsNumber() < _minLength) {
			//Length of value of '&{0}' is too short",
			p.error(XDEF.XDEF814, parserName());
		}
		if (_maxLength != -1 && val.getXDItemsNumber() > _maxLength) {
			//Length of value of '&{0}' is too long
			p.error(XDEF.XDEF815, parserName());
		}
		if (isFinal) {
			if (!p.eos()) {
				//After the item '&{0}' follows an illegal character
				p.error(XDEF.XDEF804, parserName());
			} else {
				finalCheck(xnode, p);
			}
		}
	}
	@Override
	public void finalCheck(final XXNode xnode, final XDParseResult result) {
		if (xnode == null) {
			result.error(XDEF.XDEF573, //Null value of &{0}"
				"xnode; in XSParseENTITIES.check(parser, xnode);");
			return;
		}
		DefContainer val = (DefContainer) result.getParsedValue();
		for (int i = 0; i < val.getXDItemsNumber(); i++) {
			String id = val.getXDItem(i).toString();
			if (!XSParseENTITY.chkEntity(id, xnode.getElement())) {
				//Incorrect value of '&{0}'
				result.error(XDEF.XDEF809, parserName()+": "+id);
			}
		}
	}
	@Override
	public String parserName() {return ROOTBASENAME;}
}