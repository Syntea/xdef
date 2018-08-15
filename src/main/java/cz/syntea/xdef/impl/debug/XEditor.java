/*
 * Copyright 2013 Syntea software group a.s. All rights reserved.
 *
 * File: XDDebug.java
 * Package: cz.syntea.xd
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.impl.debug;

import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ArrayReporter;

/** Interface for X-definition editor.
 * @author Vaclav Trojan
 */
public interface XEditor {

	/** Open the GUI.
	 * @param xp XDPool.
	 * @param err error reporter.
	 * @return if true the GUI was finished else recompile is supposed.
	 */
	public boolean setXEditor(final XDPool xp, final ArrayReporter err);

}
