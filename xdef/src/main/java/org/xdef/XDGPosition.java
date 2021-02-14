package org.xdef;

import org.xdef.sys.GPosition;

/** Value of GPS position in X-script.
 * @author Vaclav Trojan
 */
public interface XDGPosition extends XDValue {

	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 */
	public double latitude();

	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public double longitude();

	/** Get altitude of this position.
	 * @return altitude value is in meters may by in range from EARTH_RADIUS
	 * in meters (6376500) to MAX_VALUE or MIN_VALUE if unknown.
	 */
	public double altitude();

	/** Get distance between this position and position from the argument.
	 * @param x position from which the distance is computed.
	 * @return distance between this position and position from the argument
	 * in meters. Note Earth radius used in calculation is 6376500 m.
	 */
	public double distance(final GPosition x);
}