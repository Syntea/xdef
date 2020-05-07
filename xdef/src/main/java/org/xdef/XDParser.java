package org.xdef;

import org.xdef.proc.XXNode;
import org.xdef.sys.SException;

/** Parsers of string values.
 * @author Vaclav Trojan
 */
public interface XDParser extends XDValue {
	/** Keyword "pattern" id.*/
	public final static int PATTERN = 1;
	/** Keyword "enumeration" id.*/
	public final static int ENUMERATION = 2;
	/** Keyword "whiteSpace" id.*/
	public final static int WHITESPACE = 4;
	/** Keyword "maxInclusive" id.*/
	public final static int MAXINCLUSIVE = 8;
	/** Keyword "maxExclusive" id.*/
	public final static int MAXEXCLUSIVE = 16;
	/** Keyword "minInclusive" id.*/
	public final static int MININCLUSIVE = 32;
	/** Keyword "minExclusive" id.*/
	public final static int MINEXCLUSIVE = 64;
	/** Keyword "totalDigits" id.*/
	public final static int TOTALDIGITS = 128;
	/** Keyword "fractionDigits" id.*/
	public final static int FRACTIONDIGITS = 256;
	/** Keyword "length" id.*/
	public final static int LENGTH = 512;
	/** Keyword "maxLength" id.*/
	public final static int MAXLENGTH = 1024;
	/** Keyword "minLength" id.*/
	public final static int MINLENGTH = 2048;
	/** Keyword "format" id.*/
	public final static int NORMALIZE = 4096;
	/** Keyword "item" id.*/
	public final static int ITEM = 8192;
	/** Keyword "base" id.*/
	public final static int BASE = 16384;
	/** Keyword "separator" id.*/
	public final static int SEPARATOR = 32768;
	/** Keyword "format" id.*/
	public final static int FORMAT = 65536;
	/** Keyword "outFormat" id.*/
	public final static int OUTFORMAT = 131072;
	/** Keyword "argument" id.*/
	public final static int ARGUMENT = 262144;

	/** Table of keyword parameters.*/
	public final static String[] PARAM_NAMES = new String[] {
		"pattern",			// p
		"enumeration",		// e
		"whiteSpace",		// w
		"maxInclusive",		// m
		"maxExclusive",		// m
		"minInclusive",		// m
		"minExclusive",		// m
		"totalDigits",		// t
		"fractionDigits",	// f
		"length",			// l
		"maxLength",		// l
		"minLength",		// l
		"normalize",		// n
		"item",				// i
		"base",				// b
		"separator",		// s
		"format",			// f
		"outFormat",		// o
		"argument",			// a
	};
	/** Whitespace preserve. */
	public final static byte WS_PEESERVE = 0;
	/** Whitespace collapse. */
	public final static byte WS_COLLAPSE = 'c';
	/** Whitespace replace. */
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
	 * Usualy this method is empty.
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
	 * @param params context with named items of parameters.
	 * @param xnode actual XXNode object or null.
	 * @throws SException if an error occurs.
	 */
	public void setNamedParams(XXNode xnode, XDContainer params)
		throws SException;

	/** Set value of one "sequential" parameter of parser.
	 * @param param "sequential" parameters.
	 */
	public void setParseParam(Object param);

	/** Set value of two "sequential" parameters of parser.
	 * @param par1 the first "sequential" parameter.
	 * @param par2 the second "sequential" parameter.
	 */
	public void setParseParams(Object par1, Object par2);

	/** Set value of three "sequential" parameters of parser.
	 * @param params array with sequential parameters.
	 */
	public void setParseParams(Object[] params);

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