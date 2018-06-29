/*
 * File: CodeUndefined.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.impl.code.CodeOp;
import cz.syntea.xdef.XDValueID;

/** CodeUndefined
 *
 * @author  Vaclav Trojan
 */
class CodeUndefined extends CodeOp {

	/** Creates a new instance of CodeUndefined. */
	public CodeUndefined() {
		super(CompileBase.UNDEF_CODE, XDValueID.XD_ANY);
	}
	@Override
	public String toString() {return "UNDEF";}
	@Override
	public String stringValue() {return toString();}
}
