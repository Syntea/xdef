/*
 * Copyright 2013 Syntea software group a.s. All rights reserved.
 *
 * File: XMStatementInfo.java, created 2013-07-17.
 * Package: cz.syntea.xm
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.model;

/** Statement information for debugging.
 *
 * @author Vaclav Trojan
 */
public interface XMStatementInfo {

	/** Get source column.
	 * @return source column.
	 */
	public long getColumn();

	/** Get source line.
	 * @return source line.
	 */
	public long getLine();

	/** Get source name (or URL).
	 * @return source name (or URL) or null.
	 */
	public String getSysId();

	/** Get column of end source.
	 * @return end source column.
	 */
	public long getEndColumn();

	/** Get line of end source.
	 * @return end source line.
	 */
	public long getEndLine();

	/** Update source end position of this item.
	 * @param line end line.
	 * @param column end column.
	 */
	public void updateEndPos(long line, long column);

	/** Get name of X-definition.
	 * @return name of X-definition.
	 */
	public String getXDName();

	/** Get code address.
	 * @return code address.
	 */
	public int getAddr();

	/** Get array of local variables.
	 * @return  array of local variables.
	 */
	public XMVariable[] getLocalVariables();

}