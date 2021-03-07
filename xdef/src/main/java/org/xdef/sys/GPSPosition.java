package org.xdef.sys;

import org.xdef.msg.XDEF;

/** GPS position.
 * @author Vaclav Trojan
 */
public class GPSPosition {
	/** Earth radius in meters. */
	public static final double EARTH_RADIUS = 6376500.0D; // ???

	/** The constant used for conversion of degrees to radians. */
	private static final double DEG_TO_RAD = Math.PI / 180.0D;
	/** The latitude of the location; range from -90.0 to 90.0. */
	private final double _latitude;
	/** The longitude of the location; range from -180.0 to 180.0. */
	private final double _longitude;
	/** The altitude in meters; range from -EARTH_RADIUS in meters (6376500.0)
	 * to MAX_VALUE or Double.MIN_VALUE it it is unknown. */
	private final double _altitude;
	/** The altitude in meters; range from -EARTH_RADIUS in meters (6376500.0)
	 * to MAX_VALUE or Double.MIN_VALUE it it is unknown. */
	private final String _name;

	/** Create new instance of GPosition with latitude, longitude and altitude.
	 * @param latitude latitude of the location; range from -90.0 to 90.0.
	 * @param longitude longitude of the location; range from -180.0 to 180.0.
	 * @param altitude The altitude in meters; range from -EARTH_RADIUS
	 * in meters (6376500.0) to MAX_VALUE or Double.MIN_VALUE if it is unknown.
	 * @param name the name of position or null.
	 * @throws SRuntimeException if position is incorrect.
	 */
	public GPSPosition(final double latitude,
		final double longitude,
		final double altitude,
		final String name) {
		_latitude = latitude;
		_longitude = longitude;
		_altitude = altitude;
		_name = name;
		checkValue();
	}

	/** Check if value of GPosition is correct.
	 * @throws SRuntimeException with code XDEF222 if value is not correct.
	 */
	private void checkValue() throws SRuntimeException {
		if ((_latitude >= -90.0D && _latitude <= 90.0D)
			&& (_longitude >= -180.0D && _longitude <= 180.0D)
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

	/** Get name of this position.
	 * @return name of the position or null.
	 */
	public final String name() {return _name;}

	/** Get distance in meters from this position to position from the argument
	 * (altitude is ignored).
	 * @param x position to which the distance is computed.
	 * @return distance from this position to given position. Note the
	 * Earth radius used in Haversine formula is 6376500.0 m.
	 */
	public final double distanceTo(final GPSPosition x) {
		if (_latitude == x._latitude && _longitude == x._longitude) {
			return 0.0D; // equal positions
		}
		double lat1rad = latitude() * DEG_TO_RAD;
		double lat2rad = x.latitude() * DEG_TO_RAD;
		double deltaLong = (longitude() - x.longitude()) * DEG_TO_RAD;
		double deltaLat = Math.pow(Math.sin((lat2rad - lat1rad) / 2.0D), 2.0D)
			+ Math.cos(lat1rad) * Math.cos(lat2rad)
			* Math.pow(Math.sin(deltaLong / 2.0D), 2.0D);
		return EARTH_RADIUS
			* (2.0D*Math.atan2(Math.sqrt(deltaLat),Math.sqrt(1.0D-deltaLat)));
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
		if (x == null && !(x instanceof GPSPosition)) {
			return false;
		}
		GPSPosition y = (GPSPosition) x;
		return _latitude == y._latitude
			&& _longitude == y._longitude && _altitude == y._altitude;
	}

	@Override
	public String toString() {
		String result = _latitude + ", " + _longitude
			+ (_altitude != Double.MIN_VALUE ? ", " + _altitude : "");
		if (_name == null) {
			return result;
		}
		result += ", ";
		if (Character.isLetter(_name.charAt(0))) {
			boolean nodelimiter = true;
			for (int i = 1; i < _name.length(); i++) {
				char ch;
				if (!(Character.isLetterOrDigit(ch =_name.charAt(i))
					|| ch == '_' || ch == '-')) {
					nodelimiter = false;
					break;
				}
			}
			if (nodelimiter) {
				return result + _name;
			}
		}
		return result + '"' + SUtils.modifyString(_name, "\"", "\"\"") + '"';
	}
}