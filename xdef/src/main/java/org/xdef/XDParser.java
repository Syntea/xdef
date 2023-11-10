package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.sys.SException;

/** Parsers of string values.
 * @author Vaclav Trojan
 */
public interface XDParser extends XDValue {
	/** Keyword "pattern" id.*/
	public final static int PATTERN =			0x00000000000001;
	/** Keyword "enumeration" id.*/
	public final static int ENUMERATION =		0x00000000000002;
	/** Keyword "whiteSpace" id.*/
	public final static int WHITESPACE =		0x00000000000004;
	/** Keyword "maxInclusive" id.*/
	public final static int MAXINCLUSIVE =		0x00000000000008;
	/** Keyword "maxExclusive" id.*/
	public final static int MAXEXCLUSIVE =		0x00000000000010;
	/** Keyword "minInclusive" id.*/
	public final static int MININCLUSIVE =		0x00000000000020;
	/** Keyword "minExclusive" id.*/
	public final static int MINEXCLUSIVE =		0x00000000000040;
	/** Keyword "totalDigits" id.*/
	public final static int TOTALDIGITS =		0x00000000000080;
	/** Keyword "fractionDigits" id.*/
	public final static int FRACTIONDIGITS =	0x00000000000100;
	/** Keyword "length" id.*/
	public final static int LENGTH =			0x00000000000200;
	/** Keyword "maxLength" id.*/
	public final static int MAXLENGTH =			0x00000000000400;
	/** Keyword "minLength" id.*/
	public final static int MINLENGTH =			0x00000000000800;
	/** Keyword "item" id.*/
	public final static int ITEM =				0x00000000001000;
	/** Keyword "base" id.*/
	public final static int BASE =				0x00000000002000;
	/** Keyword "separator" id.*/
	public final static int SEPARATOR =			0x00000000004000;
	/** Keyword "format" id.*/
	public final static int FORMAT =			0x00000000008000;
	/** Keyword "outFormat" id.*/
	public final static int OUTFORMAT =			0x00000000010000;
	/** Keyword "argument" id.*/
	public final static int ARGUMENT =			0x00000000020000;

	/** Table of keyword parameters.*/
	public final static String[] PARAM_NAMES = new String[] {
		"pattern",
		"enumeration",
		"whiteSpace",
		"maxInclusive",
		"maxExclusive",
		"minInclusive",
		"minExclusive",
		"totalDigits",
		"fractionDigits",
		"length",
		"maxLength",
		"minLength",
		"item",
		"base",
		"separator",
		"format",
		"outFormat",
		"argument",
	};
	/** White space preserve. */
	public final static byte WS_PRESERVE = 0;
	/** White space collapse. */
	public final static byte WS_COLLAPSE = 'c';
	/** White space replace. */
	public final static byte WS_REPLACE = 'r';

	/** Get base parser.
	 * @return base XDParser or null.
	 */
	public XDParser getBase();

	/** Set base parser.
	 * @param x base XDParser or null.
	 */
	public void setBase(final XDParser x);

	/** Create parseResult object from String. After parsing it skips white
	 * spaces (if whiteSpace='collapse') and provides final check.
	 * @param source string to be checked.
	 * @param xnode actual XXNode object or null.
	 * @return XDParseResult object as result of parsing.
	 */
	public XDParseResult check(XXNode xnode, String source);

	/** Create parseResult object from StringParser. The parser skip white
	 * spaces (if whiteSpace='collapse') and provides final check.
	 * @param p Parse result containing the position of the source.
	 * @param xnode actual XXNode object or null.
	 */
	public void check(XXNode xnode, XDParseResult p);

	/** Parse source and create XDParseResult object. Does not skip white
	 * spaces after parsing and does not execute final check.
	 * @param p Parse result containing the position of the source.
	 * @param xnode actual XXNode object or null.
	 */
	public void parseObject(XXNode xnode, XDParseResult p);

	/** This method provides some final checks which are dependent on the
	 * context of parsing (such as ENTITY, ENTITIES, ID, IDREF, IDREFS).
	 * Usually this method is empty.
	 * @param p Parsed result to be checked.
	 * @param xnode Actual check node or null.
	 */
	public void finalCheck(XXNode xnode, XDParseResult p);

	/** Get value of whiteSpace parameter.
	 * @return 0 .. preserve, 'r' .. replace, 'c' .. collapse
	 */
	public byte getWhiteSpaceParam();

	/** Get named parameters (pattern,enumeration,white spaces,total digits,..).
	 * @return named parameters.
	 */
	public XDContainer getNamedParams();

	/** Set named parameters.
	 * @param params XDContainer with named items of parameters.
	 * @param xnode actual XXNode object or null.
	 * @throws SException if an error occurs.
	 */
	public void setNamedParams(XXNode xnode, XDContainer params)
		throws SException;

	/** Set value of "sequential" parameters of parser.
	 * @param param "sequential" parameters.
	 */
	public void setParseSQParams(Object... param);

	/** Get type of parsed value.
	 * @return The id of parsed value type.
	 */
	public short parsedType();

	/** Get name of parser.
	 * @return The name parser.
	 */
	public String parserName();

	/** Set declared type name of parser.
	 * @param name the declared type name.
	 */
	public void setDeclaredName(final String name);

	/** Get declared type name of parser.
	 * @return declared type name of parser
	 */
	public String getDeclaredName();

	/** Get integer with bits representing the allowed keyword parameters.
	 * @return integer with bits representing the allowed keyword parameters.
	 */
	public int getLegalKeys();
}