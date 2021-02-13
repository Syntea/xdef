package org.xdef.sys;

/** Value of GPS position.
 * @author Vaclav Trojan
 */
public class GPosition {
	private double _latitude;
	private double _longitude;
	private double _altitude;

	public GPosition(final double latitude, final double longitude) {
		this(latitude, longitude, Double.MIN_VALUE);
	}

	public GPosition(final double latitude,
		final double longitude,
		final double altitude) {
		_latitude = latitude; _longitude = longitude; _altitude = altitude;
	}

	public GPosition(final String s) {
		StringParser p = new StringParser(s);
		if (p.isToken("G(")) {
			p.isSpaces();
			if (p.isSignedFloat() || p.isSignedInteger()) {
				_latitude = p.getParsedDouble();
				p.skipSpaces();
				if (p.isChar(',')) {
					p.skipSpaces();
					if (p.isSignedFloat() || p.isSignedInteger()) {
						_longitude = p.getParsedDouble();
						p.skipSpaces();
						if (p.isChar(')')) {
							_altitude = Double.MIN_VALUE;
							return;
						} else if (p.isChar(',')) {
							p.skipSpaces();
							if (p.isSignedFloat() || p.isSignedInteger()) {
								_altitude = p.getParsedDouble();
								p.skipSpaces();
								if (p.isChar(')')) {
									return;
								}
							}
						}
					}
				}
			}
		}
		throw new RuntimeException("Incorrect GPosition: " + s);
	}

	public final double getLatitude() {return _latitude;}
	public final double getLongitude() {return _longitude;}
	public final double getAltitude() {return _altitude;}
	public final void setLatitude(final double x) {_latitude = x;}
	public final void setLongitude(final double x) {_longitude = x;}
	public final void setAltitude(final double x) {_altitude = x;}
	@Override
	public int hashCode() {
		return (int) Double.doubleToLongBits(27*_latitude +
			17*(_longitude+19*(_altitude == Double.MIN_VALUE ? 1 : _altitude)));
	}
	@Override
	public boolean equals(Object x) {
		if (x == null && !(x instanceof GPosition)) {
			return false;
		}
		GPosition y = (GPosition) x;
		return _latitude == y._latitude && _longitude == y._longitude
			&& _altitude == y._altitude;
	}
	@Override
	public String toString() {
		return "G(" + _latitude + ", " + _longitude
			+ (_altitude != Double.MIN_VALUE ? ", " + _altitude : "") + ')';
	}
}