package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

/** Provides operations with date and time periods (see ISO 8061).
 * This class implements abstract methods from javax.xml.datatype.Duration.
 * @author Vaclav Trojan
 */
public class SDuration extends Duration implements Comparable<SDuration> {

	private int _years;
	private int _months;
	private int _days;
	private int _hours;
	private int _minutes;
	private int _seconds;
	private double _fraction;
	private int _recurrence = 1;
	private int _negative = 1;
	private SDatetime _start;
	private SDatetime _end;

	/** Creates a new empty instance of SDuration. */
	SDuration() {}

	/** Creates a new instance of SDuration as clone created from parameter.
	 * @param duration the parameter from which this clone will be generated.
	 */
	public SDuration(final SDuration duration) {copyValues(duration);}

	private void copyValues(final SDuration duration) {
		if (duration._start != null) {
			_start = new SDatetime(duration._start);
		}
		if (duration._end != null) {
			_end = new SDatetime(duration._end);
		}
		 _years = duration._years;
		_months = duration._months;
		_days = duration._days;
		_hours = duration._hours;
		_minutes = duration._minutes;
		_seconds = duration._seconds;
		_fraction = duration._fraction;
		_recurrence = duration._recurrence;
		_negative = duration._negative;
	}

	/** Creates a new instance of SDuration with start and end.
	 * @param start start of period (may be null).
	 * @param end end of period (may be null).
	 */
	public SDuration(final SDatetime start, final SDatetime end) {
		_start = start;
		_end = end;
	}

	/** Creates a new instance of SDuration from source string.
	 * @param source source data duration in ISO 8601 format.
	 * @throws SRuntimeException if an error occurs.
	 */
	public SDuration(final String source) throws SRuntimeException {
		StringParser p = new StringParser(source, new NullReportWriter(true));
		try {
			if (p.chkDuration(false) && p.eos()) {
				copyValues(p.getParsedSDuration());
			} else {
				//Icorrect format of time period
				throw new SRuntimeException(SYS.SYS056);
			}
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				throw new SRuntimeException(((SThrowable) ex).getReport());
			} else {
				//Program exception&{0}{: }
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		}
	}

	/** Get next datetime computed from parameters. If start is not defined then
	 * actual datetime is used.
	 * @return next datetime computed from parameters.
	 */
	public SDatetime getNextTime() {
		if (_start == null) {
			return getNextTime(SDatetime.now());
		}
		return getNextTime(_start);
	}

	/** add datetime computed from argument.
	 * @param datetime datetime to which interval is added.
	 * @return next datetime computed from argument.
	 */
	public SDatetime getNextTime(final SDatetime datetime) {
		SDatetime result = addDate(datetime);
		if (_end != null) {
			if (!_end.isAfter(result)) {
				return null;
			}
		}
		return result;
	}

	/** Set negative switch.
	 * @param negative value of switch.
	 */
	public void setNegative(final boolean negative) {_negative = negative?-1:1;}

	/** Check if value is negative.
	 * @return true if value is negative.
	 */
	public boolean isNegative() {return _negative == -1;}

	/** Set number of recurrences.
	 * @param recurrence number of recurrences.
	 */
	public void setRecurrence(final int recurrence) {_recurrence = recurrence;}

	/** Get number of recurrences.
	 * @return number of recurrences or 0.
	 */
	public int getRecurrence() {return _recurrence;}

	/** Set start of period.
	 * @param start datetime specifying start of period.
	 */
	public void setStart(final SDatetime start) {_start = start;}

	/** Get start of period.
	 * @return start datetime.
	 */
	public SDatetime getStart() {return _start;}

	/** Set end of period.
	 * @param end datetime specifying end of period.
	 */
	public void setEnd(final SDatetime end) {_end = end;}

	/** Get end of period.
	 * @return end datetime.
	 */
	public SDatetime getEnd() {return _end;}

	/** Set number of years of period. Weeks parameter is cleared if specified.
	 * @param years number of years.
	 */
	public void setYears(final int years) {_years = years;}

	@Override
	/** Get number of years of period.
	 * @return number of years.
	 */
	public int getYears() {return _years;}

	/** Set number of months of period. Weeks parameter is cleared if specified.
	 * @param months number of months.
	 */
	public void setMonths(final int months) {_months = months;}

	@Override
	/** Get number of months of period.
	 * @return number of months.
	 */
	public int getMonths() {return _months;}

	/** Set number of days of period. Weeks parameter is cleared if specified.
	 * @param days number of days.
	 */
	public void setDays(final int days) {_days = days;}

	@Override
	/** Get number of days of period.
	 * @return number of days.
	 */
	public int getDays() {return _days;}

	/** Set number of hours of period.
	 * @param hours number of hours.
	 */
	public void setHours(final int hours) {_hours = hours;}

	@Override
	/** Get number of hours of period.
	 * @return number of hours.
	 */
	public int getHours() {return _hours;}

	/** Set number of minutes of period.
	 * @param minutes number of minutes.
	 */
	public void setMinutes(final int minutes) {_minutes = minutes;}

	@Override
	/** Get number of minutes of period.
	 * @return number of minutes.
	 */
	public int getMinutes() {return _minutes;}

	/** Set number of seconds of period.
	 * @param seconds number of seconds.
	 */
	public void setSeconds(final int seconds) {_seconds = seconds;}

	@Override
	/** Get number of seconds of period.
	 * @return seconds.
	 */
	public int getSeconds() {return _seconds;}

	/** Set number of milliseconds of period.
	 * @param milliseconds number of milliseconds.
	 */
	public void setMilliseconds(final int milliseconds) {
		_fraction = milliseconds * 0.001D;
	}

	/** Get number of milliseconds of period.
	 * @return milliseconds.
	 */
	public int getMilliseconds() {
		return (int) java.lang.Math.round(_fraction * 1000.0D);
	}

	/** Set number of nanoseconds of period.
	 * @param nanoseconds number of nanoseconds.
	 */
	public void setNanoseconds(final int nanoseconds) {
		_fraction = nanoseconds * 0.000000001D;
	}

	/** Get number of nanoseconds of period.
	 * @return nanoseconds.
	 */
	public int getNanoseconds() {
		return (int) java.lang.Math.round(_fraction * 1000000000.0D);
	}

	/** Set fraction of second.
	 * @param fraction of second.
	 * @throws SRuntimeException if fraction is out of interval.
	 */
	public void setFraction(final double fraction)
	throws SRuntimeException {
		if (fraction < 0 || fraction >= 1.0D) {
			throw new SRuntimeException(SYS.SYS072, //Data error&{0}{: }
				"fraction of second out of interval 0..1");
		} else {
			_fraction = fraction;
		}
	}

	/** Get fraction of second.
	 * @return Double with fraction of second or <tt>null</tt>.
	 */
	public double getFraction() {return _fraction;}

	private void durationToString(final StringBuilder sb) {
		boolean empty = true;
		if (_years != 0 || _months != 0 || _days != 0) {
			if (_negative == -1) {
				sb.append('-');
			}
			sb.append('P');
			empty = false;
			if (_years != 0) {
				sb.append(String.valueOf(_years));
				sb.append('Y');
			}
			if (_months != 0) {
				sb.append(String.valueOf(_months));
				sb.append('M');
			}
			if (_days != 0) {
				sb.append(String.valueOf(_days));
				sb.append('D');
			}
		}
		if (_hours != 0 || _minutes != 0 || _seconds != 0) {
			if (empty) {
				if (_negative == -1) {
					sb.append('-');
				}
				sb.append('P');
//				empty = false;
			}
			sb.append("T");
			if (_hours != 0) {
				sb.append(String.valueOf(_hours));
				sb.append('H');
			}
			if (_minutes != 0) {
				sb.append(String.valueOf(_minutes));
				sb.append('M');
			}
			if (_seconds != 0 || _fraction != 0.0D) {
				if (_fraction > 0.0D) {
					sb.append(String.valueOf(_fraction + _seconds));
				} else {
					sb.append(String.valueOf(_seconds));
				}
				sb.append('S');
			}
		}
	}

	@Override
	/** Get ISO 8601 format of duration.
	 * @return ISO 8601 format of this object.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (_recurrence > 1) {
			sb.append('R').append(_recurrence).append('/');
		}
		if (_start != null) {
			sb.append(_start.toString());
		}
		if (_years != 0 || _months != 0 || _days != 0 ||
			_hours != 0 || _minutes != 0 || _seconds != 0) {
			if (_start != null) {
				sb.append('/');
			}
			durationToString(sb);
		}
		if (_end != null) {
			sb.append('/');
			sb.append(_end.toString());
		}
		return sb.toString();
	}

	@Override
	/** Clone SDuration object.
	 * @return new SDuration object as a clone this one.
	 */
	public Object clone() throws CloneNotSupportedException {
		return new SDuration(this);
	}

	@Override
	/** Partial order relation comparison with this <tt>SDuration</tt>
	 * instance.
	 * <p>Comparison result must be in accordance with
	 * <a href="http://www.w3.org/TR/xmlschema-2/#duration-order">W3C
	 * XML Schema 1.0 Part 2, Section 3.2.7.6.2,
	 * <i>Order relation on duration</i></a>.</p>
	 *
	 * <p>Return:</p>
	 * <ul>
	 *  <li>-1 if this <tt>SDuration</tt> is shorter than parameter rhs</li>
	 *  <li>0 if this <tt>SDuration</tt> is equal to parameter rhs</li>
	 *  <li>+1 if this <tt>SDuration</tt> is longer than parameter rhs</li>
	 * </ul>
	 * @param other duration to compare
	 * @return the relationship between <tt>this</tt> <tt>SDuration</tt>
	 * and <tt>other</tt> parameter.
	 * @throws NullPointerException if <tt>hs</tt> is <tt>null</tt>.
	 * @throws SIllegalArgumentException if this object is not comparable
	 * with the argument <tt>rhs</tt>.
	 */
	public int compareTo(final SDuration other)
		throws NullPointerException, SIllegalArgumentException {
		if (_start == null && other._start == null) {
			int i,j,k,l;
			i = compare(other, new SDatetime(1696, 9, 1));
			j = compare(other, new SDatetime(1697, 2, 1));
			k = compare(other, new SDatetime(1903, 3, 1));
			l = compare(other, new SDatetime(1903, 7, 1));
			if (i == j && i == k && i == l) {
				 if (i == 0) {
					 return _fraction == other._fraction ? 0 :
						 _fraction < other._fraction ? -1 : 1;
				 } else {
					 return i;
				 }
			}
			//Incomparable arguments
			throw new SIllegalArgumentException(SYS.SYS085);
		}
		if (_start == null) {
			return 1;
		} else if (other._start == null) {
			return -1;
		}
		return addDate(_start).compareTo(other.addDate(other._start));
	}

	private int compare(final SDuration other, final SDatetime d) {
		SDatetime thisD = addDate(d);
		SDatetime otherD = other.addDate(d);
		return thisD.compareTo(otherD);
	}

	@Override
	public boolean equals(final Object obj) {
		return (obj != null && obj instanceof SDuration)
			? equals((SDuration) obj) : false;
	}

	/** Compare is this duration object and the other duration object are equal.
	 * @param other the other duration object.
	 * @return true if is this duration object and the other duration object
	 * are equal.
	 */
	public boolean equals(SDuration other) {return compareTo(other) == 0;}

	private SDatetime addDate(SDatetime S) {
		if (_negative < 0) {
			return S.add(-_years,
				-_months, -_days, -_hours, -_minutes, -_seconds, -_fraction);
		} else {
			return S.add(_years,
				_months, _days, _hours, _minutes, _seconds, _fraction);
		}
	}

	public void writeObj(final SObjectWriter w) throws IOException {
		w.writeInt(_years);
		w.writeInt(_months);
		w.writeInt(_days);
		w.writeInt(_hours);
		w.writeInt(_minutes);
		w.writeInt(_seconds);
		w.writeDouble(_fraction);
		w.writeInt(_recurrence);
		w.writeInt(_negative);
		w.writeSDatetime(_start);
		w.writeSDatetime(_end);
	}

	public static SDuration readObj(final SObjectReader r) throws IOException {
		SDuration x = new SDuration();
		x._years = r.readInt();
		x._months = r.readInt();
		x._days = r.readInt();
		x._hours = r.readInt();
		x._minutes = r.readInt();
		x._seconds = r.readInt();
		x._fraction = r.readDouble();
		x._recurrence = r.readInt();
		x._negative = r.readInt();
		x._start = r.readSDatetime();
		x._end = r.readSDatetime();
		return x;
	}


	@Override
	public int hashCode() {
		int result = _start == null ? 0 : _start.hashCode();
		result += _end == null ? 0 : 3*_end.hashCode();
		return 5*(result + 7*(_years + 13*(_months + 17*(_days +
			19*(_hours + 23*(_minutes + 29*(_seconds + 31*(_recurrence +
			(_fraction == 0.0D ? 0 : 37) + (_negative == -1 ? 1 : 0)))))))));
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of javax.xml.datatype.Duration
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** getSignum of value.
	 * @return true if and only if the interval is negative.
	 */
	public int getSign() {return _negative;}

	@Override
	public Number getField(final DatatypeConstants.Field field) {
		if (DatatypeConstants.YEARS.getId() == field.getId()) {
			return _years*_negative;
		}
		if (DatatypeConstants.MONTHS.getId() == field.getId()) {
			return _months*_negative;
		}
		if (DatatypeConstants.DAYS.getId() == field.getId()) {
			return _days*_negative;
		}
		if (DatatypeConstants.HOURS.getId() == field.getId()) {
			return _hours*_negative;
		}
		if (DatatypeConstants.MINUTES.getId() == field.getId()) {
			return _minutes*_negative;
		}
		if (DatatypeConstants.SECONDS.getId() == field.getId()) {
			return _seconds*_negative;
		}
		throw new UnsupportedOperationException("Field: " + field);
	}

	@Override
	public boolean isSet(final DatatypeConstants.Field field) {
		if (DatatypeConstants.YEARS.getId() == field.getId()) {
			return _years != 0;
		}
		if (DatatypeConstants.MONTHS.getId() == field.getId()) {
			return _months != 0;
		}
		if (DatatypeConstants.DAYS.getId() == field.getId()) {
			return _days != 0;
		}
		if (DatatypeConstants.HOURS.getId() == field.getId()) {
			return _hours != 0;
		}
		if (DatatypeConstants.MINUTES.getId() == field.getId()) {
			return _minutes != 0;
		}
		if (DatatypeConstants.SECONDS.getId() == field.getId()) {
			return _seconds != 0;
		}
		throw new UnsupportedOperationException("Field: " + field);
	}

	@Override
	public Duration add(final Duration rhs) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addTo(final Calendar calendar) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Duration multiply(final BigDecimal factor) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Duration negate() {
		SDuration x = new SDuration(this);
		x._negative = -_negative;
		return x;
	}

	@Override
	public Duration normalizeWith(final Calendar startTimeInstant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int compare(final Duration duration) {
		if (duration instanceof SDuration) {
			return compareTo((SDuration) duration);
		}
		throw new IllegalArgumentException(
			"Not supported for " + duration.getClass());
	}
}