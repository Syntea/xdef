/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: XDUniquesetParseItem.java, created 2018-08-25.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef;

/**  Implements uniqueSet parse item.
 * @author Trojan
 */
public interface XDUniquesetParseItem extends XDValue {

	/** Get address of parsing method.
	 * @return the address of code.
	 */
	public int getParseMethodAddr();

	/** Get parsed type.
	 * @return the type id.
	 */
	public short getParsedType();

	/** Get parsed type.
	 * @return the type id.
	 */
	public String getParseName();

	/** Check if this item is optional or required.
	 * @return true if this item is required.
	 */
	public boolean isOptional();

}