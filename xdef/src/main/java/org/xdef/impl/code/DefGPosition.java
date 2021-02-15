package org.xdef.impl.code;

import org.xdef.XDGPosition;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.msg.SYS;
import org.xdef.sys.GPosition;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SRuntimeException;

/** The class DefBoolean implements the internal object with GPosition value.
 * @author Vaclav Trojan
 */
public class DefGPosition extends XDValueAbstract implements XDGPosition {
	/** Value of GPosition. */
	private final GPosition _position;

	/** Create new instance of DefGPosition (null). */
	public DefGPosition() {_position = new GPosition();}

	/** Create new instance of DefGPosition (the value of altitude is set
	 * as unknown).
	 * @param latitude latitude of the location; range from -90.0 to 90.0
	 * (or MIN_VALUE if unknown).
	 * @param longitude longitude of the location; range from -180.0 to 180.0
	 * (or MIN_VALUE if unknown).
	 * @throws SRuntimeException (code XDEF222) if parameters are incorrect.
	 */
	public DefGPosition(final double latitude, final double longitude)
		throws SRuntimeException {
		_position = new GPosition(latitude, longitude);
	}

	/** Create new instance of DefGPosition.
	 * @param latitude latitude of the location; range from the range
	 * from -90.0 to 90.0 (or MIN_VALUE if unknown).
	 * @param longitude longitude of the location; range from the range
	 * from -180.0 to 180.0 (or MIN_VALUE if unknown).
	 * @param altitude altitude in meters from the range from -EARTH_RADIUS
	 * in meters (-6376500) to MAX_VALUE (or MIN_VALUE if unknown).
	 * @throws SRuntimeException (code XDEF222) if parameters are incorrect.
	 */
	public DefGPosition(final double latitude,
		final double longitude,
		final double altitude) throws SRuntimeException {
		_position = new GPosition(latitude, longitude, altitude);
	}

	@Override
	/** Get value of GPS. */
	public final GPosition gpsValue() {return _position;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDGPosition interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Get latitude of this position.
	 * @return latitude latitude of the location; range from -90.0 to 90.0
	 * or MIN_VALUE if unknown.
	 */
	public double latitude() {return _position.latitude();}

	@Override
	/** Get longitude of this position.
	 * @return longitude of the location; range from -180.0 to 180.0
	 * or MIN_VALUE if unknown.
	 */
	public double longitude() {return _position.longitude();}


	@Override
	/** Get altitude of this position.
	 * @return altitude value is in meters in the range from -EARTH_RADIUS
	 * in meters (-6376500) to MAX_VALUE (or MIN_VALUE if unknown).
	 */
	public double altitude() {return _position.altitude();}

	@Override
	/** Get distance between this position and position from the argument.
	 * @param x position from which the distance is computed.
	 * @return distance between this position and position from the argument
	 * in meters. Note the Earth radius used in calculation is 6376500 m.
	 */
	public double distance(final GPosition x)  {return _position.distance(x);}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final XDValue arg) {
		if (arg instanceof DefGPosition) {
			DefGPosition x = (DefGPosition) arg;
			return _position.equals(x._position);
		}
		return false;
	}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		if (arg instanceof DefGPosition) {
			if (this.equals((DefGPosition) arg)) {
				return 0;
			}
		}
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public short getItemId() {return XD_GPOSITION;}
	@Override
	public XDValueType getItemType() {return XDValueType.GPOSITION;}
	@Override
	public String stringValue() {return isNull() ? "" : _position.toString();}
	@Override
	public XDValue cloneItem() {
		return new DefGPosition(
			_position.latitude(), _position.longitude(), _position.altitude());
	}
	@Override
	public short getCode() {return CodeTable.LD_CONST;}
	@Override
	public boolean isNull() {return _position.latitude() == Double.MIN_VALUE;}
	@Override
	public Object getObject() {return _position;}
	@Override
	public String toString() {return _position.toString();}
}