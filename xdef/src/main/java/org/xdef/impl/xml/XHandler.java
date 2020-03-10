package org.xdef.impl.xml;

import org.xml.sax.InputSource;

/** Interface for XML handler
 * @author Vaclav Trojan
 */
public interface XHandler {
	public void popReader();
	public InputSource pushReader(final XAbstractReader mr);
}