package org.xdef.sys;

import org.xdef.msg.XDEF;

/** Value of GPS position.
 * @author Vaclav Trojan
 */
public class GPosition {
	/** The constant used for conversion of degrees to radians. */
	private final double DEG_RAD = Math.PI / 180.0D;
	/** Earth radius in meters. */
	private final double EARTH_RADIUS = 6376500.0D; // 6373000.0D?
	/** The latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown. */
	private final double _latitude;
	/** The longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown. */
	private final double _longitude;
	/** The altitude in meters; range from EARTH_RADIUS in meters (6376500)
	 * to MAX_VALUE or MIN_VALUE if unknown. */
	private final double _altitude;

	/** Create new instance of GPosition with all parameters unknown.*/
	public GPosition() {_latitude = _longitude = _altitude = Double.MIN_VALUE;}

	/** Create new instance of GPosition with latitude and longitude. The value
	 * of altitude is set fo unknown.
	 * @param latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 * @param longitude longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public GPosition(final double latitude, final double longitude) {
		this(latitude, longitude, Double.MIN_VALUE);
	}

	/** Create new instance of GPosition with latitude, longitude and altitude.
	 * @param latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 * @param longitude longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 * @param altitude The altitude in meters; range from EARTH_RADIUS
	 * in meters (6376500) to MAX_VALUE or MIN_VALUE if unknown.
	 */
	public GPosition(final double latitude,
		final double longitude,
		final double altitude) {
		_latitude = latitude; _longitude = longitude; _altitude = altitude;
		checkValue();
	}

	/** Create new instance of GPosition from string.
	 * @param gps string in the form:<br>
	 * "gps(latitude, longitude)" or "gps(latitude, longitude, altitude)".<br>
	 * The latitude value of the location in degrees may be in range from
	 * -90.0 to 90.0 or MIN_VALUE if unknown.<br>
	 * The longitude value of the location in degrees may be in range from
	 * -180.0 to 180.0 or MIN_VALUE if unknown.<br>
	 * The altitude value is in meters may by in range from EARTH_RADIUS
	 * in meters (6376500) to MAX_VALUE or MIN_VALUE if unknown.
	 */
	public GPosition(final String gps) {
		StringParser p = new StringParser(gps);
		if (p.isToken("gps(")) {
			p.isSpaces();
			if (p.isChar(')')) {
				_latitude = _longitude = _altitude = Double.MIN_VALUE;
				return;
			}
			if (p.isSignedFloat() || p.isSignedInteger()) {
				_latitude = p.getParsedDouble();
				p.skipSpaces();
				if (p.isChar(',') && (p.skipSpaces() || true)
					&& (p.isSignedFloat() || p.isSignedInteger())) {
					_longitude = p.getParsedDouble();
					p.skipSpaces();
					if (p.isChar(')')) {
						_altitude = Double.MIN_VALUE;
						checkValue();
						return;
					} else if (p.isChar(',') && (p.skipSpaces() || true)
						&& (p.isSignedFloat() || p.isSignedInteger())) {
						_altitude = p.getParsedDouble();
						p.skipSpaces();
						if (p.isChar(')')) {
							checkValue();
							return;
						}
					}
				}
			}
		}
		throw new SRuntimeException(XDEF.XDEF222, gps);
	}

	/** Check if value of GPosition is correct.
	 * @throws SRuntimeException with code XDEF222 if value is not correct.
	 */
	private void checkValue() {
		if ((_latitude == Double.MIN_VALUE
			|| (_latitude >= -90.0D && _latitude <= 90.0D))
			&& (_longitude == Double.MIN_VALUE
			|| (_longitude >= -180.0D && _longitude <= 180.0D))
			&& (_altitude == Double.MIN_VALUE || _altitude > - EARTH_RADIUS)) {
			return;
		}
		 // Incorrect GPosition &{0}{: }
		throw new SRuntimeException(XDEF.XDEF222, toString());
	}

	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 */
	public final double latitude() {return _latitude;}

	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public final double longitude() {return _longitude;}

	/** Get altitude of this position.
	 * @return altitude value is in meters in the range from -EARTH_RADIUS
	 * in meters (-6376500) to MAX_VALUE or MIN_VALUE if unknown.
	 */
	public final double altitude() {return _altitude;}

	/** Get distance between this position and position from the argument.
	 * @param x position from which the distance is computed.
	 * @return distance between this position and position from the argument
	 * in meters. Note the Earth radius used in calculation is 6376500 m.
	 */
	public final double distance(final GPosition x) {
		if (_latitude == Double.MIN_VALUE
			|| x._latitude == Double.MIN_VALUE) {
			return Double.MIN_VALUE; // not computed
		} else if (_latitude == x._latitude && _longitude == x._longitude) {
			return 0.0D; // equal positions
		}
		double d1 = latitude() * DEG_RAD;
		double d2 = x.latitude() * DEG_RAD;
		double theta = (longitude() - x.longitude()) * DEG_RAD;
		double z = Math.pow(Math.sin((d2 - d1) / 2.0D), 2.0D)
			+ Math.cos(d1) * Math.cos(d2)
			* Math.pow(Math.sin(theta / 2.0D), 2.0D);
		return EARTH_RADIUS
			* (2.0D * Math.atan2(Math.sqrt(z), Math.sqrt(1.0D - z)));
	}

	@Override
	public int hashCode() {
		return (int) Double.doubleToLongBits(101*_latitude +
			97*(_longitude+19*(_altitude == Double.MIN_VALUE ? 1 : _altitude)));
	}

	@Override
	/** Check if the value of this object and of the object from argument are
	 * are equal.
	 * @return true if value of this object is equal to the value of the object
	 * from argument
	 */
	public boolean equals(Object x) {
		if (x == null && !(x instanceof GPosition)) {
			return false;
		}
		GPosition y = (GPosition) x;
		return _latitude == y._latitude
			&& _longitude == y._longitude && _altitude == y._altitude;
	}

	@Override
	public String toString() {
		return _latitude == Double.MIN_VALUE ? "gps()"
			: "gps(" + _latitude + ", " + _longitude
				+ (_altitude != Double.MIN_VALUE ? ", " + _altitude : "") + ')';
	}
}