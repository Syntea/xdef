package org.xdef.json;

/** Representation of JSON object "null".
 * @author Vaclav Trojan
 */
public final class JNull {
	public static final JNull JNULL = new JNull();
	private JNull() {}
	@Override
	public final String toString() {return "null";}
	@Override
	public final int hashCode(){return 0;}
	@Override
	public final boolean equals(Object o){return o==null || o instanceof JNull;}
}