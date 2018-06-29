/*
 * Copyright 2011 Syntea software group a.s. All rights reserved.
 *
 * File: XMVariablesTable.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.model;

/** Table of XMVariables.
 * @author Vaclav Trojan
 */
public interface XMVariableTable {

	/** Get names of variables.
	 * @return array of names of variables.
	 */
	public String[] getVariableNames();

	/** Get variable.
	 * @param name the name of variable.
	 * @return XMvariable or null.
	 */
	public XMVariable getVariable(final String name);

	/** Get array with variables.
	 * @return array with variables.
	 */
	public XMVariable[] toArray();

	/** Get number of items.
	 * @return number of items.
	 */
	public int size();

}
