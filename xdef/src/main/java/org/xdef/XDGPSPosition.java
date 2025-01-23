package org.xdef;

import static org.xdef.XDValueID.XD_GPSPOSITION;
import static org.xdef.XDValueType.GPSPOSITION;
import org.xdef.msg.SYS;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SIllegalArgumentException;

/** Value of GPS position in Xscript.
 * @author Vaclav Trojan
 */
public final class XDGPSPosition extends XDValueAbstract {

	/** Value of GPosition. */
	private final GPSPosition _position;

	/** Create instance of null XDGPSPosition. */
	public XDGPSPosition() {_position = null;}

	/** Create new instance of XDGPSPosition with given position.
	 * @param position GPSPosition object.
	 */
	public XDGPSPosition(final GPSPosition position) {_position = position;}

////////////////////////////////////////////////////////////////////////////////
// Implemented methods of XDGPosition
////////////////////////////////////////////////////////////////////////////////

	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 */
	public final double latitude() {return _position.latitude();}

	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public final double longitude() {return _position.longitude();}

	/** Get altitude of this position.
	 * @return altitude value is in meters in the range from -EARTH_RADIUS
	 * in meters (-6376500) to MAX_VALUE (or MIN_VALUE if unknown).
	 */
	public final double altitude() {return _position.altitude();}

	/** Get distance in meters from this position to position from the argument (altitude is ignored).
	 * @param x GPS position to which the distance is computed.
	 * @return distance from this position to given position (note the Earth radius used in Haversine formula
	 * is 6376500 m).
	 */
	public final double distanceTo(final XDGPSPosition x) {
		return _position.distanceTo((GPSPosition) x.getObject());
	}

	/** Get name of this position.
	 * @return name of the position or null.
	 */
	public final String name() {return _position.name();}

////////////////////////////////////////////////////////////////////////////////
// Implementation of methods from XDValue interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final boolean equals(final XDValue arg) {
		if (arg instanceof XDGPSPosition) {
			XDGPSPosition x = (XDGPSPosition) arg;
			return _position != null ? _position.equals(x._position) : x._position == null;
		}
		return false;
	}
	@Override
	public final int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof XDGPSPosition) {
			if (this.equals((XDGPSPosition) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public final short getItemId() {return XD_GPSPOSITION;}
	@Override
	public final XDValueType getItemType() {return GPSPOSITION;}
	@Override
	public final String stringValue() {return isNull() ? "" : _position.toString();}
	@Override
	public final boolean isNull() {return _position == null;}
	@Override
	public final GPSPosition getObject() {return _position;}
	@Override
	public final String toString() {return _position.toString();}
}