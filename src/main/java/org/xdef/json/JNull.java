package org.xdef.json;

/** Representation of JSON object "null".
 * @author Vaclav Trojan
 */
public class JNull {
	public static JNull JNULL = new JNull();
	private JNull() {}
	@Override
	public String toString() {return "null";}
}