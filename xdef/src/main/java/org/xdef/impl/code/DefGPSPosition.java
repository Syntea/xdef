package org.xdef.impl.code;

import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.GPSPosition;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.XDGPSPosition;

/** The class implements the internal object with GpsPosition value.
 * @author Vaclav Trojan
 */
public class DefGPSPosition extends XDValueAbstract implements XDGPSPosition {
	/** Value of GPosition. */
	private final GPSPosition _position;

	/** Create new instance of null GPSPosition. */
	public DefGPSPosition() {_position = null;}

	/** Create new instance of GPSPosition given position.
	 * @param position GPSPosition object.
	 */
	public DefGPSPosition(final GPSPosition position) {_position = position;}

	@Override
	/** Get value of GPS. */
	public final GPSPosition GPSValue() {return _position;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDGPosition interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 */
	public final double latitude() {return _position.latitude();}

	@Override
	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public final double longitude() {return _position.longitude();}

	@Override
	/** Get altitude of this position.
	 * @return altitude value is in meters in the range from -EARTH_RADIUS
	 * in meters (-6376500) to MAX_VALUE (or MIN_VALUE if unknown).
	 */
	public final double altitude() {return _position.altitude();}

	@Override
	/** Get distance in meters from this position to position from the argument
	 * (altitude is ignored).
	 * @param x GPS position to which the distance is computed.
	 * @return distance from this position to given position (note the
	 * Earth radius used in Haversine formula is 6376500 m).
	 */
	public final double distanceTo(final XDGPSPosition x) {
		return _position.distanceTo(x.GPSValue());
	}

	@Override
	/** Get name of this position.
	 * @return name of the position or null.
	 */
	public String name() {return _position.name();}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefGPSPosition) {
			DefGPSPosition x = (DefGPSPosition) arg;
			return _position != null ? _position.equals(x._position)
				: x._position == null;
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefGPSPosition) {
			if (this.equals((DefGPSPosition) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_GPSPOSITION;}
	@Override
	public XDValueType getItemType() {return XDValueType.GPSPOSITION;}
	@Override
	public String stringValue() {return isNull() ? "" : _position.toString();}
	@Override
	public boolean isNull() {return _position == null;}
	@Override
	public Object getObject() {return _position;}
	@Override
	public String toString() {return _position.toString();}
}