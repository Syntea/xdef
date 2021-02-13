package org.xdef;

/** Value of GPS position in X-script.
 * @author Vaclav Trojan
 */
public interface XDGPosition extends XDValue {
	public double getLatitude();
	public double getLongitude();
	public double getAltitude();
}