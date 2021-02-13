package org.xdef.impl.code;

import org.xdef.XDGPosition;
import org.xdef.XDValue;
import org.xdef.XDValueAbstract;
import org.xdef.XDValueType;
import org.xdef.sys.GPosition;

/** The class DefBoolean implements the internal object with GPosition value.
 * @author Vaclav Trojan
 */
public class DefGPosition extends XDValueAbstract implements XDGPosition {
	/** Value of GPosition. */
	private GPosition _position;

	/** Create new instance of DefGPosition (null). */
	public DefGPosition() {}

	/** Create new instance of DefGPosition.
	 * @param latitude latitude value.
	 * @param longitude longitude value.
	 */
	public DefGPosition(final double latitude, final double longitude) {
		_position = new GPosition(latitude, longitude);
	}

	/** Create new instance of DefGPosition.
	 * @param latitude latitude value.
	 * @param longitude longitude value.
	 * @param altitude altitude value.
	 */
	public DefGPosition(final double latitude,
		final double longitude,
		final double altitude) {
		_position = new GPosition(latitude, longitude, altitude);
	}

	@Override
	/** Get value of GPS. */
	public final GPosition gpsValue() {return _position;}

	@Override
	public double getLatitude() {
		return _position == null ? Double.NaN : _position.getLatitude();
	}

	@Override
	public double getLongitude() {
		return _position == null ? Double.NaN : _position.getLongitude();
	}

	@Override
	public double getAltitude() {
		return _position == null ? Double.NaN : _position.getAltitude();
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue interface
////////////////////////////////////////////////////////////////////////////////
	@Override
	public short getItemId() {return XD_GPOSITION;}
	@Override
	public XDValueType getItemType() {return XDValueType.GPOSITION;}
	@Override
	public String stringValue() {
		return _position == null ? "" : _position.toString();
	}
	@Override
	public XDValue cloneItem() {
		return _position==null ? new DefGPosition()
			: new DefGPosition(_position.getLatitude(),
				_position.getLongitude(), _position.getAltitude());
	}
	@Override
	public short getCode() {return CodeTable.LD_CONST;}
	@Override
	public boolean isNull() {return _position == null;}
	@Override
	public Object getObject() {return _position;}
	@Override
	public String toString() {return "" +  _position;}
}