package org.xdef.impl.parsers;

import org.xdef.XDValue;

/** Parser of X-Script "empty" type.
 * @author Vaclav Trojan
 */
public class XDParseEmpty extends XDParseCDATA {
	private static final String ROOTBASENAME = "empty";
	public XDParseEmpty() {
		super();
		_minLength = _maxLength = 0;
	}
	@Override
	public int getLegalKeys() {return 0;}
	@Override
	public String parserName() {return ROOTBASENAME;}
	@Override
	public boolean equals(final XDValue o) {return o instanceof XDParseEmpty;}
}