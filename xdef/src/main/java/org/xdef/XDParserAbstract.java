package org.xdef;

import java.nio.charset.Charset;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_BIGINTEGER;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_BYTE;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_DECIMAL;
import static org.xdef.XDValueID.XD_DOUBLE;
import static org.xdef.XDValueID.XD_FLOAT;
import static org.xdef.XDValueID.XD_INT;
import static org.xdef.XDValueID.XD_LONG;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_PARSERESULT;
import static org.xdef.XDValueID.XD_SHORT;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueType.PARSER;
import static org.xdef.impl.code.CodeTable.LD_CONST;
import org.xdef.impl.code.DefContainer;
import org.xdef.impl.code.DefJNull;
import org.xdef.impl.code.DefParseResult;
import org.xdef.impl.code.DefString;
import org.xdef.proc.XXNode;
import org.xdef.msg.XDEF;
import org.xdef.sys.SException;
import org.xdef.sys.SParser;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SRuntimeException;
import org.xdef.xon.XonTools;

/** Abstract parser of string values.
 * @author Vaclav Trojan
 */
public abstract class XDParserAbstract extends XDValueAbstract implements XDParser {
	/** List of integer types id. */
	private static final short[] INTTYPES = {XD_BYTE, XD_SHORT, XD_INT, XD_LONG, XD_BIGINTEGER};
	/** List of float types id. */
	private static final short[] FLOATTYPES = {XD_FLOAT, XD_DOUBLE, XD_DECIMAL};
	/** Name of type how it was declared, */
	private String _declaredName;
	/** Base parser. */
	private XDParser _base;

	/** Get base parser.
	 * @return base XDParser or null.
	 */
	@Override
	public XDParser getBase() {return _base;}

	/** Set base parser.
	 * @param x base XDParser or null.
	 */
	@Override
	public void setBase(final XDParser x) {_base = x;};

	/** Set separator. (May be overwritten.)
	 * @param x string to be used as separator
	 */
	public void setSeparator(final String x) {} // default do nothing

	/** Check value of string.
	 * @param source string to be checked.
	 * @param xnode actual XXNode object or null.
	 */
	@Override
	public XDParseResult check(XXNode xnode, String source) {
		XDParseResult p = new DefParseResult(source);
		check(xnode, p);
		return p;
	}

	/** Create parseResult object from StringParser.
	 * @param p Parsed result to be checked.
	 * @param xnode actual XXNode object or null.
	 */
	@Override
	public void check(final XXNode xnode, final XDParseResult p) {
		if (xnode != null && xnode.getXMElement().getXonMode() != 0 && "null".equals(p.getSourceBuffer())) {
			p.setParsedValue(new DefJNull(XonTools.JNULL)); // set null
			p.setEos();
			return;
		}
		parseObject(xnode, p);
		if (p.matches()) {
			if (!p.eos()) {
				p.errorWithString(XDEF.XDEF804,//After the item '&{0}' follows an illegal character&{1}{: }
					parserName());
			} else {
				finalCheck(xnode, p);
			}
		}
	}

	/** This method provides some final checks which are dependent on the context of parsing (such as ENTITY,
	 * ENTITIES, ID, IDREF, IDREFS). Usually this method is empty and you do not need to override this method.
	 * @param p Parsed result to be checked.
	 * @param xnode Actual check node or null.
	 */
	@Override
	public void finalCheck(XXNode xnode, XDParseResult p) {}

	/** Get value of whiteSpace parameter.
	 * @return 0 .. preserve, 'r' .. replace, 'c' .. collapse
	 */
	@Override
	public byte getWhiteSpaceParam() {return 'c';} // the default value

	/** Get named parameters (pattern,enumeration,white spaces,total digits,..).
	 * @return named parameters.
	 */
	@Override
	public XDContainer getNamedParams() {return new DefContainer();}

	/** Set named parameters.
	 * @param xnode actual XXNode object or null.
	 * @param params container with named items of parameters.
	 * @throws SException if an error occurs.
	 */
	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params) throws SException {
		for (XDNamedValue nv: params.getXDNamedItems()) {
			XDValue val = nv.getValue();
			if (val != null) {
				switch (nv.getName()) {
					case "item" :
						if (val.getItemId() == XD_CONTAINER) {
							XDContainer c = (XDContainer) val;
							for (int i = 0; i <= c.getXDItemsNumber(); i++) {
								c.replaceXDItem(i, valueToParser(c.getXDItem(i)));
							}
							break;
						}
					case "base" : nv.setValue(valueToParser(val)); break;
					case "argument":
						if (val.getItemId() != XD_STRING) {
							nv.setValue(new DefString(val.toString()));
						}
				}
			}
		}
	}

	/** Set value of "sequential" parameters of parser.
	 * @param params "sequential" parameters.
	 */
	@Override
	public void setParseSQParams(Object... params) {}

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
	public short parsedType() {return XD_STRING;}

	/** Get integer with bits representing the allowed keyword parameters.
	 * @return integer with bits representing the allowed keyword parameters.
	 */
	@Override
	public int getLegalKeys() {return 0;}

	@Override
	public short getAlltemsType() {return parsedType();} // default parsedType

	@Override
	public String getSeparator() {return null;} // default null (not set)

	////////////////////////////////////////////////////////////////////////////
	//Do not overwrite following methods
	////////////////////////////////////////////////////////////////////////////

	@Override
	public final short getItemId() {return XD_PARSER;} // do not override

	@Override
	public final short getCode() {return LD_CONST;}

	@Override
	public final XDValueType getItemType() {return PARSER;}

	/** Set declared type name of parser.
	 * @param name the declared type name.
	 */
	@Override
	public final void setDeclaredName(final String name) {_declaredName = name;}

	/** Get declared type name of parser.
	 * @return declared type name of parser
	 */
	@Override
	public final String getDeclaredName() {return _declaredName;}

	/** Check if value is parser and return it as a Parser or convert it to Parser (if it is possible).
	 * @param x value to be checked.
	 * @return return argument as a Parser.
	 * @throws SRuntimeException with message XDEF474 if conversion is not possible.
	 */
	public final XDParser valueToParser(final XDValue x) {
		if (x != null) {
			switch (x.getItemId()) {
				case XD_PARSER: return (XDParser) x;
				case XD_BOOLEAN:
					return new XDParserAbstract() {
						@Override
						public void parseObject(XXNode xnode, XDParseResult p) {
							p.setEos();
							if (!x.booleanValue()) {
								p.error(XDEF.XDEF809, parserName());//Inorrect value&{0}{ of '}{'}&{1}{: '}{'}
							}
						}
						@Override
						public String parserName() {return "generatedParser";}
					};
				case XD_PARSERESULT: {
					XDParseResult y = (XDParseResult) x;
					return new XDParserAbstract() {

						@Override
						public void parseObject(XXNode xnode, XDParseResult p) {
							XDParseResult y = (XDParseResult) x;
							p.setSourceBuffer(y.getSourceBuffer());
							p.setIndex(y.getIndex());
							p.setParsedValue(y.getParsedValue());
							p.addReports(y.getReporter());
						}

						@Override
						public short parsedType() {
							XDValue v = y.getParsedValue();
							return v == null ? XD_ANY : v.getItemId();
						}

						@Override
						public String parserName() {return "generatedParser";}
					};
				}
			}
		}
		//The value type in the named parameter '&{0}' of the parser&{1}{ '}{'} must be Parser
		throw new SRuntimeException(XDEF.XDEF474, "%item", parserName());
	}

	/** Check if separator follows.
	 * @param p parser used for parsing.
	 * @param separator string with separator characters.
	 * @return true if separator was found, otherwise return false.
	 */
	public final boolean isSeparator(final SParser p, final String separator) {
		p.isSpaces();
		boolean result;
		if (separator == null || separator.isEmpty()) {
			result = p.isSpaces();
		} else {
			if (!(result = (p.isOneOfChars(separator) != NOCHAR))) {
				p.isSpaces(); //if space charaters are not in separator parameter try it againn
				result = (p.isOneOfChars(separator) != NOCHAR);
			}
		}
		return !p.eos() && result;
	}

	/** Find index of type in the array of types.
	 * @param type find index of this type in array of types,
	 * @param types in array of names,
	 * @return index of this type in array of types or return -1 if not found.
	 */
	private static int isTypeOf(short type, final short[] types) {
		for (int i = 0; i < types.length; i++) {
			if (type == types[i]) {
				return i;
			}
		}
		return -1;
	}

	/** Find first index of type int the array of types and if it was found
	 * then find index of newType in the array and return corrected type.
	 * @param name name to be found.
	 * @param newType newName to be found.
	 * @param types array of names.
	 * @return index or -1.
	 */
	private static short getTypeIndex(final short type, final short newType, final short[] types) {
		int j = isTypeOf(type, types);
		if (j >= 0) {
			int k = isTypeOf(newType, types);
			if (k >= 0) {
				return types[k >= j ? k : j];
			}
		}
		return -1;
	}

	public static final short getItemsType(final XDParser[] x) {
		short result = x[0].parsedType();
		for (int i = 1; i < x.length; i++) {
			short type = x[i].parsedType();
			short j = getTypeIndex(result, type, INTTYPES);
			if (result != type) {
				if (j != -1) {
					result = j;
				} else if ((j = getTypeIndex(result, type, FLOATTYPES)) != -1) {
					result = j;
				} else {
					return XD_ANY;
				}
			}
		}
		return result;
	}

	/** Check if parsed data contains only specified characters and set error occurs an illegal character.
	 * The test is skipped if legal codes is missing.
	 * @param p Parsed result to be checked.
	 * @param xnode actual XXNode object or null.
	 */
	public static final void checkCharset(final XXNode xnode, final XDParseResult p) {
		Charset[] chsets = xnode != null ? xnode.getXDPool().getLegalStringCharsets() : null;
		if (chsets != null && chsets.length > 0) {
			String s = p.getParsedString();
			for (Charset chset : chsets) {
				if (s.equals(new String(s.getBytes(chset), chset))) {
					return; // OK, charset found
				}
			}
			s = "";
			for (int i = 0; i < chsets.length; i++) {
				if (i > 0) {
					s += ", ";
				}
				s += chsets[i].name();
			}
			//The parsed string contains a character that is not allowed in any of the code tables: &{0}
			p.error(XDEF.XDEF823, s);
		}
	}
}
