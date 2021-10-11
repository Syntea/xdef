package org.xdef.xon;

/** Representation of JSON/XON object "null".
 * @author Vaclav Trojan
 */
public final class XonNull {
	public static final XonNull JNULL = new XonNull();
	private XonNull() {}
	@Override
	public final String toString() {return "null";}
	@Override
	public final int hashCode(){return 0;}
	@Override
	public final boolean equals(Object o){return o==null||o instanceof XonNull;}
}