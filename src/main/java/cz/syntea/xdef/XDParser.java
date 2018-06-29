/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XDParser.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef;

import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.SException;

/** Parsers of string values.
 * @author Vaclav Trojan
 */
public interface XDParser extends XDValue {

	/** Get base parser.
	 * @return base XDParser or null.
	 */
	public XDParser getBase();

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

}