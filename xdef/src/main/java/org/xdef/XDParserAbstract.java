package org.xdef;

import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_STRING;
import org.xdef.impl.code.CodeTable;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.impl.parsers.XSAbstractParser;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SException;

/** Abstract parser of string values.
 * @author Vaclav Trojan
 */
public abstract class XDParserAbstract extends XDValueAbstract
	implements XDParser {

	/** Name of type how it was declared, */
	private String _declaredName;
	/** Base parser. */
	private XDParser _base;

	@Override
	/** Get base parser.
	 * @return base XDParser or null.
	 */
	public XDParser getBase() {return _base;}

	@Override
	/** Set base parser.
	 * @param x base XDParser or null.
	 */
	public void setBase(final XDParser x) {_base = x;};

	@Override
	/** Check value of string.
	 * @param source string to be checked.
	 * @param xnode actual XXNode object or null.
	 */
	public XDParseResult check(XXNode xnode, String source) {
		XDParseResult p = new DefParseResult(source, (XDValue) null);
		check(xnode, p);
		return p;
	}

	@Override
	/** Create parseResult object from StringParser.
	 * @param p Parsed result to be checked.
	 * @param xnode actual XXNode object or null.
	 */
	public void check(final XXNode xnode, final XDParseResult p) {
		parseObject(xnode, p);
		if (p.matches()) {
			if (!p.eos()) {
				//After the item '&{0}' follows an illegal character&{1}{: }
				p.errorWithString(XDEF.XDEF804, parserName());
			} else {
				finalCheck(xnode, p);
			}
		}
	}

	@Override
	/** This method provides some final checks which are dependent on the
	 * context of parsing (such as ENTITY, ENTITIES, ID, IDREF, IDREFS).
	 * Usually this method is empty and you do not need to override this method.
	 * @param p Parsed result to be checked.
	 * @param xnode Actual check node or null.
	 */
	public void finalCheck(XXNode xnode, XDParseResult p) {}

	@Override
	/** Get value of whiteSpace parameter.
	 * @return 0 .. preserve, 'r' .. replace, 'c' .. collapse
	 */
	public byte getWhiteSpaceParam() {return 'c';} // the default value

	@Override
	/** Get named parameters (pattern,enumeration,white spaces,total digits,..).
	 * @return named parameters.
	 */
	public XDContainer getNamedParams() {return new DefContainer();}

	@Override
	/** Set named parameters.
	 * @param xnode actual XXNode object or null.
	 * @param params container with named items of parameters.
	 * @throws SException if an error occurs.
	 */
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		for (XDNamedValue nv: params.getXDNamedItems()) {
			XDValue val = nv.getValue();
			if (val != null) {
				switch (nv.getName()) {
					case "item" : 
						if (val.getItemId() == XD_CONTAINER) {
							XDContainer c = (XDContainer) val;
							for (int i = 0; i <= c.getXDItemsNumber(); i++) {
								c.replaceXDItem(i,
									XSAbstractParser.valueToParser(c.getXDItem(i)));
							}
							break;
						}
					case "base" :
						nv.setValue(XSAbstractParser.valueToParser(val));
						break;
					case "argument":
						if (val.getItemId() != XD_STRING) {
							nv.setValue(new DefString(val.toString()));
						}
				}
			}
		}
	}

	@Override
	/** Set value of "sequential" parameters of parser.
	 * @param param "sequential" parameters.
	 */
	public void setParseSQParams(Object... params) {}

	@Override
	public final short getItemId() {return XD_PARSER;} // do not override
	@Override
	public XDValueType getItemType() {return XDValueType.PARSER;}
	@Override
	public short parsedType() {return XD_STRING;}  // may be overrided
	@Override
	public final short getCode() {return CodeTable.LD_CONST;}
	@Override
	public String toString() {return parserName();}
	@Override
	public boolean equals(final XDValue arg) {
		if (arg != null && (arg instanceof XDParser)) {
			XDParser parser  = (XDParser) arg;
			if (parserName().equals(parser.parserName())) {
				XDContainer params = getNamedParams();
				XDContainer params1 = parser.getNamedParams();
				return params.equals(params1);
			}
		}
		return false;
	}

	@Override
	/** Set declared type name of parser.
	 * @param name the declared type name.
	 */
	public final void setDeclaredName(final String name) {_declaredName = name;}

	@Override
	/** Get declared type name of parser.
	 * @return declared type name of parser
	 */
	public final String getDeclaredName() {return _declaredName;}

	@Override
	/** Get integer with bits representing the allowed keyword parameters.
	 * @return integer with bits representing the allowed keyword parameters.
	 */
	public int getLegalKeys() {return 0;}
}