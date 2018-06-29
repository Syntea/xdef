/*
 * Copyright 2018 Syntea software group a.s. All rights reserved.
 *
 * File: XHandler.java, created 2018-04-08.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited licence contained in the accompanying
 * file LICENSE.TXT.
 */
package cz.syntea.xdef.impl.xml;

import org.xml.sax.InputSource;

/** Interface for XML handler
 * @author trojan
 */
public interface XHandler {
	public void popReader();
	public InputSource pushReader(final XAbstractReader mr);
}
