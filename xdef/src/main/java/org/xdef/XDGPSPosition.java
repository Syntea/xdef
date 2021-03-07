package org.xdef;

/** Value of GPS position in X-script.
 * @author Vaclav Trojan
 */
public interface XDGPSPosition extends XDValue {

	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0.
	 */
	public double latitude();

	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0.
	 */
	public double longitude();

	/** Get altitude of this position.
	 * @return altitude value is in meters may by in meters
	 * (-6376500.0 to MAX_VALUE) or Double.MIN_VALUE if unknown; note
	 * 6376500.0 is used as the Earth radius in meters).
	 */
	public double altitude();

	/** Get name of this position.
	 * @return name of the position or null.
	 */
	public String name();

	/** Get distance in meters from this position to position from the argument.
	 * @param x GPS position to which the distance is computed.
	 * @return distance from this position to given position. Note the
	 * Earth radius used in Haversine formula is 6376500 m.
	 */
	public double distanceTo(final XDGPSPosition x);
}