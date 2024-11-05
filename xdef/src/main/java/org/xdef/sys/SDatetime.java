package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.Stack;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/** Implementation of date and time values. This class implements abstract methods from
 * javax.xml.datatype.XMLGregorianCalendar.
 * @author Trojan
 */
public class SDatetime extends XMLGregorianCalendar implements Comparable<SDatetime> {
	public static final DateFormatSymbols DFS = new DateFormatSymbols(Locale.US);
	public static final TimeZone UTC_ZONE = new SimpleTimeZone(0, "UTC");
	public static final TimeZone NULL_ZONE = new SimpleTimeZone(0, "_null_");
	private static final BigDecimal BILLION_D = new BigDecimal("1000000000");
	private static final BigInteger BILLION_I = new BigInteger("1000000000");
	int _day;
	int _month;
	int _year;
	int _hour;
	int _minute;
	int _second;
	double _fraction; //fraction of second
	int _eon;
	TimeZone _tz;
	DateFormatSymbols _dfs;
	Calendar _calendar;
	// original values for reset
	private boolean _origValues;
	private int _origday;
	private int _origmonth;
	private int _origyear;
	private int _orighour;
	private int _origminute;
	private int _origsecond;
	private double _origfraction; //fraction of second
	private int _origeon;
	private TimeZone _origtz;

	private void setOriginalValues() {
		_origValues = true;
		_origday = _day;
		_origmonth = _month;
		_origyear = _year;
		_orighour = _hour;
		_origminute = _minute;
		_origsecond = _second;
		_origfraction = _fraction;
		_origeon = _eon;
		_origtz = _tz;
	}

	private void resetOriginalValues() {
		if (_origValues) {
			_day = _origday;
			_month = _origmonth;
			_year = _origyear;
			_hour = _orighour;
			_minute = _origminute;
			_second = _origsecond;
			_fraction = _origfraction;
			_eon = _origeon;
			_tz = _origtz;
		}
	}
	/** Create new instance of SDatetime with empty parameters.*/
	public SDatetime() {init();}

	private void init() {
		_day = _month = _year = _hour = _minute = _second = Integer.MIN_VALUE;
		_fraction = 0.0D;
		_eon = 0;
		_tz = null;
		_dfs = null;
		_calendar = null;
	}

	/** Create new instance of SDatetime from source in ISO8601 format.
	 * @param source ISO8601 format of date.
	 * @throws SRuntimeException if argument is not ISO8601 format of date.
	 */
	public SDatetime(final String source) throws SRuntimeException {
		this(parse(source));
		setOriginalValues();
	}

	/** Create new instance of SDatetime with parameters from argument.
	 * @param c Calendar object with date.
	 */
	public SDatetime(final Calendar c) {
		setCalendar(c);
		setOriginalValues();
	}

	/** Create new instance of SDatetime with parameters from argument.
	 * @param d Date object with date.
	 */
	public SDatetime(final Date d) {
		this();
		Calendar c = new GregorianCalendar();
		c.setTime(d);
		setCalendar(c);
		setOriginalValues();
	}

	/** Create new instance of SDatetime with parameters from argument.
	 * @param d Timestamp object with date.
	 */
	public SDatetime(final Timestamp d) {
		this((Date) d);
		setNanos(d.getNanos());
		setOriginalValues();
	}

	/** Create new instance of SDatetime with parameters from argument.
	 * @param sd SDatetime object with date.
	 */
	public SDatetime(final SDatetime sd) {
		_day = sd._day;
		_month = sd._month;
		_year = sd._year;
		_hour = sd._hour;
		_minute = sd._minute;
		_second = sd._second;
		_fraction = sd._fraction;
		_eon = sd._eon;
		if (sd._tz != null) {
			_tz = (TimeZone) sd._tz.clone();
		}
		if (sd._dfs != null) {
			_dfs = sd._dfs;
		}
		if (sd._calendar != null) {
			_calendar = (Calendar) sd._calendar.clone();
		}
		setOriginalValues();
	}

	/** Creates a new instance of SDatetime from a year, month and day.
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 */
	public SDatetime(final int year, final int month, final int day) {
		_day = day;
		_month = month;
		_year = year;
		_hour = Integer.MIN_VALUE;
		_minute = Integer.MIN_VALUE;
		_second = Integer.MIN_VALUE;
//		_fraction = 0.0D; _tz = null;
		setOriginalValues();
	}

	/** Creates a new instance of SDatetime from a year, month and day.
	 * @param year year.
	 * @param month month.
	 * @param day day.
	 * @param hour hour of day.
	 * @param minute minute.
	 * @param second second.
	 * @param fraction fraction of second (must be greater or equal to 0
	 * @param tz TimeZone or null.
	 */
	public SDatetime(final int year, final int month, final int day,
		final int hour,
		final int minute,
		final int second,
		final double fraction,
		final TimeZone tz) {
		_day = day;
		_month = month;
		_year = year;
		_hour = hour;
		_minute = minute;
		_second = second;
		_fraction = fraction;
		_tz = tz;
		chkAndThrow();
		setOriginalValues();
	}

	/** Create SDatetime instance from SDatetime object.
	 * @param x SDatetime object from which SDatetime object will be created.
	 * @return SDatetime new instance created from SDatetime object.
	 */
	public static final SDatetime createFrom(final SDatetime x) {
		return x == null ? null : new SDatetime(x);
	}

	/** Create SDatetime instance from Date object.
	 * @param x Date object from which SDatetime object will be created.
	 * @return SDatetime new instance created from Date object.
	 */
	public static final SDatetime createFrom(final Date x) {
		return x == null ? null : new SDatetime(x);
	}

	/** Create SDatetime instance from Calendar object.
	 * @param x Calendar object from which SDatetime object will be created.
	 * @return SDatetime new instance created from Calendar object.
	 */
	public static final SDatetime createFrom(final Calendar x) {
		return x == null ? null : new SDatetime(x);
	}

	/** Create SDatetime instance from Timestamp object.
	 * @param x Timestamp object from which SDatetime object will be created.
	 * @return SDatetime new instance created from Timestamp object.
	 */
	public static final SDatetime createFrom(final Timestamp x) {
		return x == null ? null : new SDatetime(x);
	}

	/** Create SDatetime instance from String.
	 * @param x String from which SDatetime object will be created.
	 * @return SDatetime new instance created from String.
	 */
	public static final SDatetime createFrom(final String x) {
		return x == null ? null : new SDatetime(x);
	}
	/** fQuotient(a, b) = the greatest integer less than or equal to a/b */
	private static int iQuotient(final int a, final int b) {
		if (a < 0 && b < 0 || a >= 0 && b >= 0) {// positive result
			return a/b;
		}
		return (a/b - ((a%b != 0) ? 1 : 0)); // negative result
	}

	/** modulo(a, b) = a - iQuotient(a,b)*b */
	private static int modulo(final int a, final int b) {
		return a - iQuotient(a,b)*b;
	}

	/** fQuotient(a, low, high) = iQuotient(a - low, high - low) */
	private static int fQuotient(final int a, final int low, final int high) {
		return iQuotient(a - low, high - low);
	}

	/** modulo(a, low, high) = modulo(a - low, high - low) + low */
	private static int modulo(final int a, final int low, final int high) {
		return modulo(a - low, high - low) + low;
	}

	/** Create new datetime with added values of parameters (each parameter may be negative).
	 * @param years number of years to add.
	 * @param months number of monthss to add.
	 * @param days number of days to add.
	 * @param hours number of hours to add.
	 * @param minutes number of minutes to add.
	 * @param seconds number of seconds to add.
	 * @param fraction fraction of second (must be in opened interval -1.0 .. +1.0) to add.
	 * @return new datetime created from the actual datetime and parameters.
	 */
	public final SDatetime add(final int years,
		final int months,
		final int days,
		final int hours,
		final int minutes,
		final int seconds,
		final double fraction) {
		int nseconds = (_month != Integer.MIN_VALUE ? _month : 0) + months;
		int month = modulo(nseconds, 1, 13);
		int carry = fQuotient(nseconds, 1, 13);
		int year = (_year != Integer.MIN_VALUE ? _year : 0) + years + carry;
		nseconds = (_second != Integer.MIN_VALUE ? _second : 0) + seconds; // Seconds
		if (fraction >= 1.0D || fraction <= -1.0D) {
			throw new IllegalArgumentException("Fraction is not in interval -1.0 .. +1.0");
		}
		double fract = fraction + _fraction;
		if (fract > 1.0D) {
			fract -= 1.0D;
			nseconds++;
		} else if (fract < 0.0D) {
			nseconds--;
			fract = - fract;
		}
		int second = modulo(nseconds, 60);
		carry = iQuotient(nseconds,60);
		nseconds = (_minute != Integer.MIN_VALUE ? _minute : 0) + minutes+carry; // Minutes
		int minute = modulo(nseconds, 60);
		carry = iQuotient(nseconds,60);
		nseconds = (_hour != Integer.MIN_VALUE ? _hour : 0) + hours+carry; // Hours
		int hour = modulo(nseconds, 24);
		carry = iQuotient(nseconds, 24);
		// Days
		int tempDays = (_day != Integer.MIN_VALUE ? _day : 0);
		if (tempDays > maximumDayInMonthFor(year, month)) {
			tempDays = maximumDayInMonthFor(year, month);
		} else if (tempDays < 1) {
			tempDays = 1;
		}
		int day = tempDays + days + carry;
		for (;;) {
			if (day < 1) {
				day = day + maximumDayInMonthFor(year, month - 1);
				carry = -1;
			} else if (day > maximumDayInMonthFor(year, month)) {
				day = day - maximumDayInMonthFor(year, month);
				carry = 1;
			} else {
				return new SDatetime(year == 0 ? Integer.MIN_VALUE : year,
					month, day, hour, minute, second, fract, _tz);
			}
			nseconds = month + carry;
			month = modulo(nseconds, 1, 13);
			year = year + fQuotient(nseconds, 1, 13);
		}
	}

	/** Check parsed date.
	 * @return true if parsed date is OK.
	 */
	public final boolean chkDatetime() {
		if (_tz != null) {
			int i = _tz.getRawOffset();
			if (i < -50400000 /*-14:00*/ || i > 50400000 /*+14:00*/ 	|| i % 60000 != 0) {
				return false; //not multiple of minutes -> incorrect zone offset
			}
		}
		if (_hour >= 24) {
			if (_hour > 24 || _minute > 0 || _second > 0 ||	_fraction > 0.0D) {
				return false;
			}
		}
		if (_fraction < 0 || _fraction >= 1) {
			return false;
		}
		if (_year != Integer.MIN_VALUE && _year == 0) {
			return false; //invalid date
		}
		if (_day <= 0) {
			return _day != 0 && (_month < 0
				|| _month >= 1 && _month <= 12) && (_hour < 24 && _minute < 60 && _second < 60
				|| _hour == 24 && _minute == 0 && _second == 0);
		} else if (_month <= 0) {
			return _month != 0 && _day <= 31 && (_hour < 24 && _minute < 60 && _second < 60
				|| _hour == 24 && _minute == 0 && _second == 0);
		} else {
			if (_day <= 28) {
				return _month <= 12 && (_hour < 24 && _minute < 60 && _second < 60
					|| _hour == 24 && _minute == 0 && _second == 0);
			} else if (_day == 31) {
				if (_month == 1 || _month == 3 || _month == 5 || _month == 7 || _month == 8
					|| _month == 10 || _month == 12) {
					return _hour < 24 && _minute < 60 && _second < 60
						|| _hour == 24 && _minute == 0 && _second == 0;
				}
				return false;
			} else if (_month == 2) { // February
				if (_day > 29 || (_day==29 && _year!=Integer.MIN_VALUE && !isLeapYear(_year)) ) {
					return false;
				}
				return _hour < 24 && _minute < 60 && _second < 60
					|| _hour == 24 && _minute == 0 && _second == 0;
			} else { // 30 days
				return _month <= 12 && _day <= 30 && (_hour < 24 && _minute < 60 && _second < 60
					|| _hour == 24 && _minute == 0 && _second == 0);
			}
		}
	}

	private void chkAndThrow() {
		if (!chkDatetime()) {
			throw new SRuntimeException(SYS.SYS072, "Incorrect datetime value");//Data error&{0}{: }
		}
		synchronized(this) {
			_calendar = null;
		}
	}

	/** Set instance Calendar values to this object.
	 * @param c Calendar.
	 * @throws SRuntimeException SYS072 Data error
	 */
	public final void setCalendar(final Calendar c) {
		 synchronized(this) {
			if (c.isSet(Calendar.YEAR)) {
				_year = (c.get(Calendar.ERA)==0) ? -(c.get(Calendar.YEAR)-1) : c.get(Calendar.YEAR);
			} else {
				_year = Integer.MIN_VALUE;
			}
			_month = c.isSet(Calendar.MONTH) ? c.get(Calendar.MONTH) + 1 : Integer.MIN_VALUE;
			_day = c.isSet(Calendar.DAY_OF_MONTH) ?c.get(Calendar.DAY_OF_MONTH) :Integer.MIN_VALUE;
			_hour = c.isSet(Calendar.HOUR_OF_DAY) ? c.get(Calendar.HOUR_OF_DAY) : Integer.MIN_VALUE;
			_minute = c.isSet(Calendar.MINUTE) ? c.get(Calendar.MINUTE) : Integer.MIN_VALUE;
			_second = c.isSet(Calendar.MINUTE) ? c.get(Calendar.SECOND) : Integer.MIN_VALUE;
			if (c.isSet(Calendar.MILLISECOND )) {
				_fraction = c.get(Calendar.MILLISECOND)/1000.0D;
				_fraction = _fraction == 0.0D ? Double.MIN_NORMAL : _fraction;
			}
			if ((_tz=c.getTimeZone()) != null && "_null_".equals(_tz.getID())) {
				_tz = null;
			}
			_calendar = c;
		 }
	}

	/** Set local parameters.
	 * @param x datetime symbols.
	 * @throws SRuntimeException SYS072 Data error
	 */
	public final void setLocaleFormatSymbols(final DateFormatSymbols x){_dfs=x;}

	/** Get locale date format symbols.
	 * @return locale date format symbols.
	 */
	public final DateFormatSymbols getLocaleFormatSymbols() {
		return _dfs != null ? _dfs : DFS; //default
	}

	/** Get instance of Calendar with actual values. Values which were not set are set to zero.
	 * @return Calendar with actual values.
	 * @throws SRuntimeException SYS072 Data error
	 */
	public final Calendar getCalendar() {
		if (_calendar != null) {
			return _calendar;
		}
		if (!chkDatetime()) {
			throw new SRuntimeException(SYS.SYS040); //Datetime error&{0}{: }
		}
		try {
			synchronized (this) {
				if (_year != Integer.MIN_VALUE && _month >= 1 && _day >= 1) {// year,month,date
					if (_hour >= 0 && _minute >= 0) {// and hour,minute,second
						_calendar = new GregorianCalendar(
							_year, _month - 1, _day, _hour, _minute, _second >= 0 ? _second : 0);
					} else if (_hour < 0 && _minute < 0 && _second < 0) {//no hour, minute, second
						_calendar = new GregorianCalendar(_year, _month - 1, _day);
					}
					if (_calendar != null) {
						if (_tz != null) {
							_calendar.setTimeZone(_tz);
						}
						if (_fraction != 0.0D) {// set milliseconds
							_calendar.set(Calendar.MILLISECOND, (int) (_fraction * 1000.0D));
						}
						return _calendar;
					}
				}
				//(year,month,date)
				_calendar = new GregorianCalendar();
				_calendar.clear();
				if (_tz != null) {
					_calendar.setTimeZone(_tz);
				} else {
					_calendar.setTimeZone(NULL_ZONE);
				}
				if (_year != Integer.MIN_VALUE) {
					_calendar.set(Calendar.YEAR, _year);
				}
				if (_month >= 1) {
					_calendar.set(Calendar.MONTH, _month - 1);
				}
				if (_day >= 1) {
					_calendar.set(Calendar.DAY_OF_MONTH, _day);
				}
				if (_hour >= 0) {
					_calendar.set(Calendar.HOUR_OF_DAY, _hour);
				}
				if (_minute >= 0) {
					_calendar.set(Calendar.MINUTE, _minute);
				}
				if (_second >= 0) {
					_calendar.set(Calendar.SECOND, _second);
				}
				if (_fraction != 0.0D) {
					_calendar.set(Calendar.MILLISECOND,
						(int) (_fraction*1000.0D));
				}
				return _calendar;
			}
		} catch(Exception ex) {
			_calendar = null;
			throw new SRuntimeException(SYS.SYS072, ex); //Data error&{0}{: }
		}
	}

	/** Get java.util.Date object .
	 * @return java.util.Date object.
	 */
	public final Date getTime() {
		return getCalendar().getTime();
	}

	/** Get time in milliseconds from the January 1, 1970, 00:00:00 GMT.
	 * @return number of milliseconds.
	 */
	public final long getTimeInMillis() {
		return getCalendar().getTimeInMillis();
	}

	/** Get instance of XMLGregorianCalendar with actual values.
	 * @return XMLGregorianCalendar created from this object.
	 */
	public final XMLGregorianCalendar toXMLGregorianCalendar() {
		try {
			DatatypeFactory dtf = DatatypeFactory.newInstance();
			return dtf.newXMLGregorianCalendar(toGregorianCalendar());
		} catch (DatatypeConfigurationException ex) {
			throw new SRuntimeException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Conversion to ISO 8601 string
	 * @return string with standard date and time format.
	 */
	public final String toISO8601() {
		if (_hour == Integer.MIN_VALUE) {
			if (_year != Integer.MIN_VALUE) {
				if (_month == Integer.MIN_VALUE) {
					return formatDate("yyyy" + (_tz==null?"":"Z"));
				}
				if (_day == Integer.MIN_VALUE) {
					return formatDate("yyyy-MM" + (_tz==null?"":"Z"));
				}
				return formatDate("yyyy-MM-dd" + (_tz==null?"":"Z"));
			}
			if (_month != Integer.MIN_VALUE) {
				if (_day != Integer.MIN_VALUE) {
					return formatDate("--MM-dd" + (_tz==null?"":"Z"));
				}
				return formatDate("--MM" + (_tz==null?"":"Z"));
			}
			if (_day != Integer.MIN_VALUE) {
				return formatDate("---dd" + (_tz==null?"":"Z"));
			}
			return formatDate("" + (_tz==null?"":"Z"));
		} else if (_year == Integer.MIN_VALUE && _minute != Integer.MIN_VALUE) {
			if (_second == Integer.MIN_VALUE) {
				return formatDate("HH:mm" + (_tz==null?"":"Z"));
			}
			return formatDate("HH:mm:ss" + (_fraction==0.0D? "":".S") + (_tz==null?"":"Z"));
		}
		return formatDate("yyyy-MM-ddTHH:mm:ss" + (_fraction==0.0D? "":".S") + (_tz==null?"":"Z"));
	}

	/** Conversion to RFC822 string,
	 * @return string with RFC822 date and time format.
	 */
	public final String toRFC822() {return formatDate("EEE, dd MMM yyyy HH:mm:ss ZZZZZ");}

	@Override
	/** Get year from this date. If year is undefined returns -Integer.MAXINT.
	 * @return year from this date or -Integer.MAXINT.
	 */
	public final int getYear() {return _year;}

	@Override
	/** Get month from this date. If month is undefined returns -Integer.MAXINT.
	 * @return month from this date or -Integer.MAXINT.
	 */
	public final int getMonth() {return _month;}

	@Override
	/** Get day from this date. If day is undefined returns -Integer.MAXINT.
	 * @return day from this date or -Integer.MAXINT.
	 */
	public final int getDay() {return _day;}

	@Override
	/** Get hour from this date. If hour is undefined returns -Integer.MAXINT.
	 * @return hour from this date or -Integer.MAXINT.
	 */
	public final int getHour() {return _hour;}

	@Override
	/** Get minute from this date. If minute is undefined return -Integer.MAXINT.
	 * @return minute from this date or -Integer.MAXINT.
	 */
	public final int getMinute() {return _minute;}

	@Override
	/** Get second from this date. If second is undefined returns -Integer.MAXINT.
	 * @return second from this date or -Integer.MAXINT.
	 */
	public final int getSecond() {return _second;}

	@Override
	/** Get milliseconds from this date. If millisecond is undefined return 0.
	 * @return millisecond from this date or 0.
	 */
	public final int getMillisecond() {
		return _fraction!=0.0D ? (int) java.lang.Math.round(_fraction*1000.0D) : Integer.MIN_VALUE;
	}

	/** Get nanoseconds from this date. If nanosecond is undefined return 0.
	 * @return millisecond from this date or 0.
	 */
	public final int getNanos() {
		return (int) java.lang.Math.round(_fraction * 1000000000.0D);
	}

	/** Get the fraction of second from this date.
	 * @return fraction of the second.
	 */
	public final double getFraction() {return _fraction;}

	/** Get TimeZone from this date. If TimeZone is undefined returns null.
	 * @return TimeZone from this date or null.
	 */
	public final TimeZone getTZ() {return _tz;}

	/** Get value of parsed daytime date in milliseconds. Values which was not set are set to zero.
	 * If day time is not available return Integer.MIN_VALUE (DatatypeConstants.FIELD_UNDEFINED).
	 * @return Milliseconds with parsed values started from midnight.
	 */
	public final int getDaytimeInMillis() {
		int result = _hour >= 0 ? _hour * 3600000 : 0;
		if (_minute >= 0) {
			result += _minute * 60000;
		}
		if (_second >= 0) {
			result += _second * 1000;
		}
		if (_fraction > Double.MIN_NORMAL) {
			result += getMillisecond();
		}
		return result;
	}

	/** Set value of daytime date in milliseconds.
	 * @param millis milliseconds started from midnight.
	 */
	public final void setDaytimeInMillis(final int millis) {
		synchronized(this) {
			_fraction = millis != 0 ? (millis%1000)/1000.0D : Double.MIN_NORMAL;
			int nmillis;
			_second = (nmillis = millis/1000)%60;
			_minute = (nmillis = nmillis/60)%60;
			_hour = (nmillis/60)%24;
			_calendar = null;
		}
	}

	@Override
	/** Set year value.
	 * @param year to set.
	 */
	public final void setYear(final int year) {
		synchronized(this) {
			if (year == Integer.MIN_VALUE) {
				_year = Integer.MIN_VALUE;
				_eon = 0;
			} else if (Math.abs(year) < BILLION_I.intValue()) {
				_year = year;
				_eon = 0;
			} else {
				BigInteger theYear = BigInteger.valueOf((long) year);
				BigInteger remainder = theYear.remainder(BILLION_I);
				_year = remainder.intValue();
				_eon = theYear.subtract(remainder).intValue();
			}
			_year = year; chkAndThrow();
		}
	}

	@Override
	/** Set month value.
	 * @param month month to set.
	 */
	public final void setMonth(final int month) {synchronized(this) {_month=month; chkAndThrow();}}

	@Override
	/** Set date (day) value.
	 * @param day day to set.
	 */
	public final void setDay(final int day) {synchronized(this) {_day = day; chkAndThrow();}}

	@Override
	/** Set hour value.
	 * @param hour hour of day (0..23) to set.
	 */
	public final void setHour(final int hour) {synchronized(this) {_hour = hour; chkAndThrow();}}

	@Override
	/** Set minute value.
	 * @param min minute (0..59) to set.
	 */
	public final void setMinute(final int min) {synchronized(this) {_minute = min; chkAndThrow();}}

	@Override
	/** Set second value.
	 * @param sec second (0..59) to set.
	 */
	public final void setSecond(final int sec) {synchronized(this) {_second = sec; chkAndThrow();}}

	@Override
	/** Set millisecond value.
	 * @param millis millisecond (0..999) or Integer.MIN_VALUE (DatatypeConstants.FIELD_UNDEFINED).
	 * @throws SRuntimeException if milliseconds is not in interval 0..999.
	 */
	public final void setMillisecond(final int millis) throws SRuntimeException {
		if (millis != Integer.MIN_VALUE && (millis < 0 || millis > 1000)) {
			//Data error&{0}{: }
			throw new SRuntimeException(SYS.SYS072, "milliseconds out of interval 0..999");
		}
		synchronized(this) {
			_fraction = millis != Integer.MIN_VALUE ? millis/1000.0D : 0.0D;
			chkAndThrow();
		}
	}

	/** Set nanosecond value.
	 * @param nanos nanosecond (0..999999999).
	 * @throws SRuntimeException if milliseconds is not in interval 0..999.
	 */
	public final void setNanos(final int nanos) throws SRuntimeException {
		if (nanos != Integer.MIN_VALUE && (nanos < 0 || nanos > 1000000000)) {
			 //Data error&{0}{: }
			throw new SRuntimeException(SYS.SYS072, "milliseconds out of interval 0..999");
		}
		synchronized(this) {
			_fraction = nanos!=Integer.MIN_VALUE ? nanos/1000000000.0D : Double.MIN_NORMAL;
			chkAndThrow();
		}
	}

	/** Set fraction of the second.
	 * @param fract fraction of second &lt;..1).
	 * @throws SRuntimeException if fraction is not in interval &lt;..1).
	 */
	public final void setFraction(final double fract) throws SRuntimeException {
		if (fract < 0.0D || fract >= 1.0D) {
			 //Data error&{0}{: }
			throw new SRuntimeException(SYS.SYS072, "fraction of second out of interval 0..1");
		}
		synchronized(this) {
			_fraction = fract == 0.0D ? Double.MIN_NORMAL : fract;
			chkAndThrow();
		}
	}

	/** Set time zone value.
	 * @param tz TimeZone to be set.
	 */
	public final void setTZ(final TimeZone tz) {setTZ(_tz, tz);}

	/** Set time zone value.
	 * @param defaultZone default TimeZone.
	 * @param newZone TimeZone to be set.
	 */
	public final void setTZ(final TimeZone defaultZone, final TimeZone newZone) {
		synchronized(this) {
			if (newZone == null) {
				_tz = null;
				_calendar = null;
				return;
			}
			if (_tz == null) {
				_tz = newZone;
				_calendar = null;
				return;
			}
			if (!_tz.equals(newZone)) {
				int diff = newZone.getRawOffset() + newZone.getDSTSavings()
					- (_tz.getRawOffset()  + _tz.getDSTSavings());
				int hour = _hour;
				int minute = _minute;
				Calendar c = getCalendar();
				c.setTimeZone(newZone);
				_tz = (TimeZone) newZone.clone();
				setCalendar(c);
				if (diff != 0 && _hour == hour && _minute == minute) {
					c.add(Calendar.MILLISECOND, diff);
				}
			}
		}
	}

	/** Sets the week of year on the date.
	 * @param weekOfYear week of year to set.
	 */
	public final void setWeekOfYear(final int weekOfYear) {
		synchronized(this) {
			Calendar c = getCalendar();
			c.set(Calendar.WEEK_OF_YEAR, weekOfYear);
			setCalendar(c);
		}
	}

	/** Sets the week of month on the date.
	 * @param weekOfMonth week of month to set.
	 */
	public final void setWeekOfMonth(final int weekOfMonth) {
		synchronized(this) {
			Calendar c = getCalendar();
			c.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
			setCalendar(c);
		}
	}

	/** Sets the week of month on the date.
	 * @param dayOfYear day of year to set.
	 */
	public final void setDayOfYear(final int dayOfYear) {
		synchronized(this) {
			Calendar c = getCalendar();
			c.set(Calendar.DAY_OF_YEAR, dayOfYear);
			setCalendar(c);
		}
	}

	/** Sets the day of week.
	 * @param dayOfWeek day of week to set.
	 */
	public final void setDayOfWeek(final int dayOfWeek) {
		synchronized(this) {
			Calendar c = getCalendar();
			c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			setCalendar(c);
		}
	}

	/** Sets the day of month.
	 * @param dayOfMonth day of month to set.
	 */
	public final void setDayOfMonth(final int dayOfMonth) {
		synchronized(this) {
			Calendar c = getCalendar();
			c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			setCalendar(c);
		}
	}

	/** Get the week of year from this date.
	 * @return week of year from this date.
	 */
	public final int getWeekOfYear() {return getCalendar().get(Calendar.WEEK_OF_YEAR);}

	/** Get the week of month from this date.
	 * @return week of month from this date.
	 */
	public final int getWeekOfMonth() {return getCalendar().get(Calendar.WEEK_OF_MONTH);}

	/** Get the time zone offset from this date. The offset represents the milliseconds from GMT.
	 * @return time zone offset from the date.
	 */
	public final int getTimeZoneOffset() {return _tz == null ? 0 : getTimezone()*60000;}

	/** Get time zone ID.
	 * @return time zone ID or the empty string.
	 */
	public final String getTimeZoneID() {return _tz == null ? "" : _tz.getID();}

	/** Set time zone ID.
	 * @param id time zone ID.
	 */
	public final void setTimeZoneID(final String id) {
		synchronized (this) {
			if (_tz == null) {
				_tz = TimeZone.getTimeZone("GMT");
			}
			_tz.setID(id);
			_calendar = null;
		}
	}

	/** Get a day of month which represents the last date of the month in this date.
	 * @return day of month which represents the last date of the month.
	 */
	public final int getLastDayOfMonth() {
		Calendar c = (Calendar) getCalendar().clone();
		c.add(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.DAY_OF_MONTH, -1);
		return c.get(Calendar.DAY_OF_MONTH);
	}

	/** Set a day of month which represents the last date of the month in this date. */
	public final void setLastDayOfMonth() {setDay(getLastDayOfMonth());}

	/** Get Easter Monday. The algorithm used here was published in
	 * the Explanatory Supplement to the Astronomical Almanac,
	 * Oudin J.M. (1940),  ed. Seidelmann P. K. (1992).
	 * @return SDatetime object with Easter Monday.
	 * @throws SRuntimeException if Easter Monday can't be computed.
	 */
	public final SDatetime getEasterMonday() {
		if (_year == Integer.MIN_VALUE) {
			throw new SRuntimeException(SYS.SYS092); //Year is undefined
		}
		return getEasterMonday(_year);
	}

	/** Get the day of week in month.
	 * Get the ordinal number of the day of the week within the current month. Together with the DAY_OF_WEEK
	 * field, this uniquely specifies a day within a month. Unlike WEEK_OF_MONTH and WEEK_OF_YEAR, this field
	 * value does not depend on getFirstDayOfWeek() or getMinimalDaysInFirstWeek(). DAY_OF_MONTH 1 through 7
	 * always correspond to DAY_OF_WEEK_IN_MONTH 1; 8 through 14 correspond to DAY_OF_WEEK_IN_MONTH 2,
	 * and so on. DAY_OF_WEEK_IN_MONTH 0 indicates the week before DAY_OF_WEEK_IN_MONTH 1. Negative values
	 * count back from the end of the month, so the last Sunday* of a month is specified as
	 * DAY_OF_WEEK = SUNDAY, DAY_OF_WEEK_IN_MONTH=-1. Because negative values count backward they will usually
	 * be aligned differently within the month than positive values. For example, if a month has 31 days,
	 * DAY_OF_WEEK_IN_MONTH -1 will overlap DAY_OF_WEEK_IN_MONTH 5 and the end of 4.
	 * @return day of week in month.
	 */
	public final int getDayOfWeekInMonth() {
		Calendar c = getCalendar();
		return c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}

	/** Get the day of week from the date. The days of the week begin with 1 which represents
	 * Sunday. Monday is represented by 2, Tuesday is represented by 3 and so on.
	 * @return The day of week for the input date.
	 */
	public final int getDayOfWeek() {return getCalendar().get(Calendar.DAY_OF_WEEK);}

	/** Returns the day of year from the date. The days of the week begin with 1 which represents
	 * Sunday. Monday is represented by 2, Tuesday is represented by 3 and so on.
	 * @return The day of week for the input date.
	 */
	public final int getDayOfYear() {return getCalendar().get(Calendar.DAY_OF_YEAR);}

	/** Check if the year from this object is a leap year or not.
	 * @return true if is year is a leap year, otherwise returns false.
	 */
	public final boolean isLeapYear() {return isLeapYear(_year);}

	/** Get the number of milliseconds the date differs from this object. The number returned
	 * is based on the number of milliseconds the date is from the January 1, 1970, 00:00:00 GMT.
	 * @param x time to be subtracted.
	 * @return The number of milliseconds the date from the argument differs the date from this
	 * object. If the date from the argument is before the date from this object the returned value
	 * is negative.
	 */
	public final long getTimeDifference(final SDatetime x) {
		return getTimeInMillis() - x.getTimeInMillis();
	}

	/** Get the number of milliseconds the date differs from this object. The number returned
	 * is based on the number of milliseconds the date is from the January 1, 1970, 00:00:00 GMT.
	 * @param x time to be subtracted.
	 * @return differs of milliseconds this date from the argument. If the date from the argument
	 * is before the date from this object the returned value is negative.
	 */
	public final long timeDifference(final Calendar x) {
		return getTimeInMillis() - x.getTimeInMillis();
	}

	/** Add a specified number of years to this date.
	 * @param amount positive, zero or negative number of years to add to the original date.
	 */
	public final void addYear(final int amount) {
		synchronized(this) {
			SDatetime d = add(amount, 0, 0, 0, 0, 0, 0.0D);
			_year = d._year;
			_calendar = d._calendar;
		}
	}

	/** Add a specified number of months to this date.
	 * @param amount positive, zero or negative number of months to add to the original date.
	 */
	public final void addMonth(final int amount) {
		SDatetime d = add(0, amount, 0, 0, 0, 0, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_calendar = d._calendar;
		}
	}

	/** Add a specified number of weeks to this date.
	 * @param amount positive, zero or negative number of months to add to the original date.
	 */
	public final void addWeek(final int amount) {
		SDatetime d = add(0, 0, amount*7, 0, 0, 0, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_calendar = d._calendar;
		}
	}

	/** Add a specified number of days to this date.
	 * @param amount positive, zero or negative number of months to add to the original date.
	 */
	public final void addDay(final int amount) {
		SDatetime d = add(0, 0, amount, 0, 0, 0, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_calendar = null;
		}
	}

	/** Add a specified number of hours to this date.
	 * @param amount positive, zero or negative number of hours to add to the original date.
	 */
	public final void addHour(final int amount) {
		SDatetime d = add(0, 0, 0, amount, 0, 0, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_hour = d._hour;
			_calendar = null;
		}
	}

	/** Add a specified number of minutes to this date.
	 * @param amount positive, zero or negative number of minutes to add to the original date.
	 */
	public final void addMinute(final int amount) {
		SDatetime d = add(0, 0, 0, 0, amount, 0, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_hour = d._hour;
			_minute = d._minute;
			_calendar = null;
		}
	}

	/** Add a specified number of seconds to this date.
	 * @param amount positive, zero or negative number of seconds to add to the original date.
	 */
	public final void addSecond(final int amount) {
		SDatetime d = add(0, 0, 0, 0, 0, amount, 0.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_hour = d._hour;
			_minute = d._minute;
			_second = d._second;
			_calendar = null;
		}
	}

	/** Add a specified number of milliseconds to this date.
	 * @param amount positive, zero or negative number of milliseconds to add to the original date.
	 */
	public final void addMillis(final long amount) {
		SDatetime d = add(0, 0, 0, 0, 0, (int) amount / 1000, (amount % 1000)/1000.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_hour = d._hour;
			_minute = d._minute;
			_second = d._second;
			_calendar = null;
		}
	}

	/** Add a specified number of nanoseconds to this date.
	 * @param amount positive, zero or negative number of nanoseconds to add to the original date.
	 */
	public final void addNanos(final long amount) {
		SDatetime d = add(0,0,0,0,0, (int)(amount/1000000000),(amount % 1000000000)/1000000000.0D);
		synchronized(this) {
			_year = d._year;
			_month = d._month;
			_day = d._day;
			_hour = d._hour;
			_minute = d._minute;
			_second = d._second;
			_calendar = null;
		}
	}

	/** Compare current object with given Calendar (both values are first converted to UTC).
	 * @param x Calendar object to be compared.
	 * @return true if the current time of this SDatetime is isAfter the time of argument.
	 */
	public final boolean isAfter(final Calendar x) {return getCalendar().after(x);}

	/** Compare current object with given argument (both values are first converted to UTC).
	 * @param x object to be compared.
	 * @return true if the current time of this SDatetime is isAfter the time of argument.
	 */
	public final boolean isAfter(final SDatetime x) {return getCalendar().after(x.getCalendar());}

	@Override
	/** Compare this SDatetime with the other one.
	 * <p>Return:
	 *  <br>-1 if this <i>SDatetime</i> is smaller than <i>other</i>
	 *  <br>0 if this <i>SDatetime</i> is equal to <i>other</i>
	 *  <br>+1 if this <i>SDatetime</i> is bigger than <i>other</i>
	 *  <br>throws SIllegalArgumentException if one of arguments has zone and
	 *  the second one has no zone and the difference is less then 14 hours
	 * @param arg the other SDatetime
	 * @return the relationship between this SDatetime and the parameter.
	 * @throws SIllegalArgumentException if  this object is not comparable with the argument.
	 */
	public final int compareTo(final SDatetime arg)
		throws SIllegalArgumentException {
		SDatetime a1 = new SDatetime(this);
		SDatetime a2 = new SDatetime(arg);
		TimeZone tz1 = a1._tz;
		TimeZone tz2 = a2._tz;
		a1._calendar =  a2._calendar = null;
		double f1 = a1._fraction == Double.MIN_NORMAL ? 0.0D : a1._fraction;
		a1._fraction = f1;
		double f2 = arg._fraction == Double.MIN_NORMAL ? 0.0D : arg._fraction;
		a2._fraction = f2;
		a1._fraction = a2._fraction = 0.0D;
		Calendar c1 = a1.getCalendar();
		if (tz1 == null || "_null_".equals(tz1.getID())) {
			c1.setTimeZone(NULL_ZONE);
			tz1 = a1._tz = null;
		}
		Calendar c2 = a2.getCalendar();
		if (tz2 == null || "_null_".equals(tz2.getID())) {
			c2.setTimeZone(NULL_ZONE);
			tz2 = a2._tz = null;
		}
		BigDecimal t1 = new BigDecimal(c1.getTimeInMillis());
		t1 = t1.add(new BigDecimal(f1));
		BigDecimal t2 = new BigDecimal(c2.getTimeInMillis());
		t2 = t2.add(new BigDecimal(f2));
		if (tz1 == null) {
			if (tz2 != null) {
				BigDecimal diff = new BigDecimal(50400000);
				if (t1.subtract(diff).compareTo(t2) <= 0 && t2.compareTo(t1.add(diff)) <= 0) {
					throw new SIllegalArgumentException(SYS.SYS085); //Incomparable arguments
				}
			}
		} else if (tz2 == null) {
			BigDecimal diff = new BigDecimal(50400000);
			if (t2.subtract(diff).compareTo(t1) <= 0 && t1.compareTo(t2.add(diff)) <= 0) {
				throw new SIllegalArgumentException(SYS.SYS085); //Incomparable arguments
			}
		}
		if (a1._eon != 0) {
			t1.add(BigDecimal.valueOf(a1._eon).multiply(BILLION_D));
		}
		if (a2._eon != 0) {
			t2.add(BigDecimal.valueOf(a2._eon).multiply(BILLION_D));
		}
		return t1.compareTo(t2);
	}

	/** Compare current object with given argument (both values are first converted to UTC).
	 * @param arg object to be compared.
	 * @return true if current time of this SDatetime is equal to the time and date from argument.
	 */
	public final boolean equals(final SDatetime arg) {return compareTo(arg) == 0;}

	/** Compare current object with given argument (both values are first converted to UTC).
	 * @param arg object to be compared.
	 * @return true if current time of this SDatetime is equal to the time and date from argument.
	 */
	@Override
	public final boolean equals(final Object arg) {
		if (arg != null) {
			if (arg instanceof SDatetime) {
				return equals((SDatetime) arg);
			} else if (arg instanceof XMLGregorianCalendar) {
				return new SDatetime(this).getCalendar().equals(
					((XMLGregorianCalendar) arg).toGregorianCalendar());
			} else if (arg instanceof Calendar) {
				return new SDatetime(this).getCalendar().equals((Calendar) arg);
			} else if (arg instanceof Timestamp) {
				Timestamp t = (Timestamp) arg;
				if (t.getTime() != new SDatetime(this).getTime().getTime()) {
					return false;
				}
				if (t.getNanos() != getNanos()) {
					return false;
				}
				return ((Timestamp) arg).equals(getTimestamp(this));
			} else if (arg instanceof Date) {
				return ((Date) arg).equals(new SDatetime(this).getTime());
			}
		}
		return false;
	}

	@Override
	public final int hashCode() {
		synchronized(this) {
			int hash = ((((((_day*3+_month)*5+_year)*7+_hour)*11+_minute)*13+_second)*17+getMillisecond())*23;
			return  (_tz != null ? hash * 31 + _tz.hashCode() : hash);
		}
	}

	/** Compare current date with given argument.
	 * @param obj object to be compared (can be instance of SDatetime, Date or Calendar).
	 * @return true if current date of this SDatetime is equal to the date from argument.
	 */
	public final boolean isSameDate(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof SDatetime) {
			SDatetime sd = (SDatetime) obj;
			return sd._day == _day && sd._month == _month  && sd._year == _year;
		} else {
			Calendar c;
			if (obj instanceof Date) {
				c = new GregorianCalendar();
				c.setTime((Date) obj);
			} else {
				c = (Calendar) obj;
			}
			return c.get(Calendar.DAY_OF_MONTH)==_day && (c.get(Calendar.MONTH)+1)==_month
				&& (c.get(Calendar.ERA)==0 ?-(c.get(Calendar.YEAR)-1) :c.get(Calendar.YEAR))==_year;
		}
	}

	/** Compare current day time with given argument (local time is compared, both values
	 * are NOT converted to UTC).
	 * @param obj object to be compared (can be instance of SDatetime, Date or Calendar).
	 * @return true if current day time of this SDatetime is equal to the day time from argument.
	 */
	public final boolean isSameLocalDayTime(final Object obj) {
		if (obj == null) {
			return false;
		}
		Calendar c;
		if (obj instanceof SDatetime) {
			c = ((SDatetime) obj).getCalendar();
		} else {
			if (obj instanceof Date) {
				c = new GregorianCalendar();
				c.setTime((Date) obj);
			} else {
				c = (Calendar) obj;
			}
		}
		Calendar c1 = getCalendar();
		return c.get(Calendar.HOUR_OF_DAY) == c1.get(Calendar.HOUR_OF_DAY) &&
			c.get(Calendar.MINUTE) == c1.get(Calendar.MINUTE)  &&
			c.get(Calendar.SECOND) == c1.get(Calendar.SECOND) &&
			c.get(Calendar.MILLISECOND) == c1.get(Calendar.MILLISECOND);
	}

	/** Get difference of calendar days with given argument without respect to
	 * time zone offset.
	 * @param obj object to be subtracted (can be instance of SDatetime,
	 * Date or Calendar).
	 * @return difference of calendar days.
	 */
	public final int getCalendarDaysDifference(final Object obj) {
		Calendar c1 = getCalendar();
		Calendar c2;
		if (obj instanceof SDatetime) {
			c2 = ((SDatetime) obj).getCalendar();
		} else {
			if (obj instanceof Date) {
				c2 = new GregorianCalendar();
				c2.setTime((Date) obj);
			} else if (obj instanceof Calendar) {
				c2 = (Calendar) obj;

			} else if (obj instanceof Timestamp) {
				c2 = new GregorianCalendar();
				c2.setTime((Timestamp) obj);
			} else {
				throw new IllegalArgumentException(obj == null ? "null" : obj.getClass().getName());
			}
		}
		GregorianCalendar cc1 = new GregorianCalendar(
			c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		GregorianCalendar cc2 = new GregorianCalendar(
			c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		cc1.set(Calendar.MILLISECOND, 0);
		cc2.set(Calendar.MILLISECOND, 0);
		cc1.setTimeZone(TimeZone.getTimeZone("UTC"));
		cc2.setTimeZone(TimeZone.getTimeZone("UTC"));
		return (int) ((cc1.getTimeInMillis() - cc2.getTimeInMillis()) / (24*60*60*1000));
	}

	/** Convert day time to given time zone.
	 * @param tz time zone to be converted to.
	 */
	public final void toTimeZone(final TimeZone tz) {
		synchronized(this) {
			try {
				Calendar c = getCalendar();
				long t = c.getTimeInMillis();
				c.setTimeZone(tz);
				c.setTimeInMillis(t);
				setCalendar(c);
			} catch (Exception ex) {
				_tz = tz;
			}
		}
	}

	/** Convert day time to local time zone. */
	public final void toLocalTime() {toTimeZone(TimeZone.getDefault());}

	/** Convert day time to UTC. */
	public final void toUTC() {toTimeZone(TimeZone.getTimeZone("UTC"));}

	@Override
	/** Get printable format of SDatetime.
	 * @return string with ISO 8601 format of SDatetime object.
	 */
	public final String toString() {return toISO8601();}

	/** Clone SDatetime object.
	 * @return new SDatetime object as a clone this one.
	 */
	@Override
	public final Object clone() {return new SDatetime(this);}

	/** Check format of datetime.
	 * @param format string with format.
	 * @return integer where upper byte contain zone information and lower three bytes contains
	 * number of repetition of millisecond pattern.
	 * @throws SRuntimeException if format is not correct.
	 */
	public final static int checkFormat(final String format) throws SRuntimeException {
		int flen = format.length();
		int ms = 0; //number of repetition of millisecond pattern
		int zone = 0; //number of repetition of zone pattern
		// Scan format and find milliseconds format to be able eventualy to round up millisendonds
		// and seconds fields
		int optional = 0;
		char pat;
		for (int fpos = 0; fpos < flen;) {
			switch (pat = format.charAt(fpos++)) {
				case '|': //variant ???
					continue;
				case '[':
					optional++;
					continue;
				case ']':
					if (--optional < 0) {
						//Datetime mask format: unclosed quoted literal or section&{0}{, position: }
						throw new SRuntimeException(SYS.SYS049, fpos);
					}
					continue;
				case '{': {
					StringParser p = new StringParser(format, fpos);
					Report r;
					if ((r = p.checkDateFormatDefaults()) != null) {
						throw new SRuntimeException(r);
					}
					fpos = p.getIndex();
					continue;
				}
				case '?': //one of chars
					if (fpos < flen && (format.charAt(fpos)=='\''||format.charAt(fpos)=='"')) {
						pat = format.charAt(fpos++);
					} else {
						//Datetime mask format: unclosed quoted literal or section&{0}{, position: }
						throw new SRuntimeException(SYS.SYS049, fpos);
					}
				case '"':
				case '\'': { //quoted string (literal)
					char delim = pat;
					int beg = fpos;
					while (fpos < flen && (pat = format.charAt(fpos++)) != delim) {
						if (pat == delim) {
							if (fpos >= flen || format.charAt(fpos) != delim) {
								break;
							}
							pat = format.charAt(fpos++);
						}
					}
					if (pat != delim) {
						//Datetime mask format: unclosed quoted literal or section&{0}{, position: }
						throw new SRuntimeException(SYS.SYS049, beg+1);
					}
					continue;
				}
				case 'H': //hour 0..24
				case 'h': //hour 0..12
				case 'K': //hour 0..24
				case 'k': {//hour 0..12
					int i = 1;
					while (fpos < flen && format.charAt(fpos) == pat) {
						fpos++;
						i++;
					}
					continue;
				}
				case 'a': //AM,PM
				case 'D': //day in year
				case 'd': //day
				case 'E': //day in week
				case 'e': //day in week
				case 'F': //day of week in month
				case 'G': //era AD,BC
				case 'M': //month
				case 'm': //minute
				case 'R': //year - two digits (database)
				case 'S': //millisecond
				case 's': //second
				case 'Y': //year
				case 'y': //year
				case 'z': //RFC822 zone
				case 'Z': //ISO zone
				case 'w': //week in year
				case 'W': {//week in month
					int i = 1;
					while (fpos < flen && format.charAt(fpos) == pat) {
						fpos++;
						i++;
					}
					if (pat == 'S') {
						ms = i;
					} else if (pat == 'Z' || pat == 'z') {
						if (pat=='Z' && i != 1 && i != 2 && i != 5 && i != 6 | pat=='z' && i > 6) {
							//Datetime mask format: incorrect zone format&{0}{, position: }
							throw new SRuntimeException(SYS.SYS050, fpos - i);
						}
						zone = i;
					} else if ((pat=='R' && i!=2) || (pat=='y' && i==3) || (pat=='Y' && i!=2)) {
						//Datetime mask: incorrect year specification&{0}{, position: }
						throw new SRuntimeException(SYS.SYS059, fpos - 3);
					}
				}
			}
		}
		if (optional != 0) {
			//Datetime mask format: unclosed quoted literal or section&{0}{, position: }
			throw new SRuntimeException(SYS.SYS049);
		}
		return (zone << 16) | ms;
	}

	private static void setOptionInvalid(final Stack<Integer> options) {
		int size;
		int x;
		if ((size = options.size()) > 0 && (x = options.peek()) > 0) {
			options.set(size - 1, -x);
		}
	}

	/** Append  string with date according to given format to StringBuffer.
	 * String with format mask contains characters interpreted as follows:
	 * <br><b>a</b> AM/PM marker
	 * <br><b>D</b> day in year
	 * <br><b>d</b> day of month (1 through 31)
	 * <br><b>E</b> day of week (text)
	 *  <p>E, EE, EEE - abbreviated day name (Mon, Tue, .. Sun
	 *  <p>EEEE (and more)- full month name (Monday, Tuesday, .. Sunday
	 * <br><b>e</b> day of week (number 1=Monday, 7=Sunday)
	 * <br><b>F</b> day of week in month
	 * <br><b>G</b> era (0=BC, 1=AD)
	 * <br><b>H</b> hour (0 through 23)
	 * <br><b>h</b> hour (1..12 with am/pm)
	 * <br><b>K</b> hour 0..11 with am/pm)
	 * <br><b>k</b> hour 1..23
	 * <br><b>M</b> month in year (1=January .. 12=December).
	 *  <p>M - number without leading zero
	 *  <p>MM - number with leading zero
	 *  <p>MMM - abbreviated month name (Jan, Feb, .. Dec
	 *  <p>MMMM (and more)- full month name (January, February, .. December
	 * <br><b>m</b> minute (0 through 59)
	 * <br><b>s</b> second (0 through 59)
	 * <br><b>S</b> digits representing a decimal fraction of a second
	 * <br><b>y</b> year
	 * <br><b>W</b> week in month
	 * <br><b>w</b> week in year
	 * <br><b>Z</b> time zone designator (Z or +hh:mm or -hh:mm)
	 * <p>ZZ time zone designator (Z or +h:m or -h:m)
	 * <p>ZZZZZ time zone designator (Z or +hhmm or -hhmm)
	 * <p>ZZZZZZ time zone designator (Z or +hh:mm or -hh:mm)
	 * <br><b>z</b> zone name
	 * <p>z, zz, zzz abbreviated zone name (CET)
	 * <p>zzzz and more full zone name (Central European Time)
	 * @param sb The StringBuffer where to append a string with date.
	 * @param format The format of created date.
	 * @throws SRuntimeException
	 * <br>SYS049 Unclosed string quotation.
	 * <br>SYS050 Incorrect zone specification in declaration of date and/or time format.
	 */
	public final void formatDate(final StringBuffer sb, final String format) {
		int flen = format.length();
		if (flen == 0) {
			return;
		}
		int ms = checkFormat(format) & 0xffff; // number of repetition of millisecond pattern
		DateFormatSymbols dfs = SDatetime.DFS;
		Calendar calendar = getCalendar();
		int year = calendar.get(Calendar.ERA) == 0
			? - (calendar.get(Calendar.YEAR) - 1) : calendar.get(Calendar.YEAR);
		int sec = calendar.get(Calendar.SECOND);
		int min = calendar.get(Calendar.MINUTE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String millis = null;
		double fraction = _fraction>0.0D && _hour>=0 && _minute>=0 && _second>=0 ? _fraction : 0.0D;
		if (ms == 0) {// round seconds according to fraction
			if (fraction >= 0.5D) {
				sec++; //seconds can be 60 now - we solve it later!
			}
		} else {
			DecimalFormat df = ms == 1 ? new DecimalFormat("0.0##############")
				: new DecimalFormat("0."+SUtils.makeStringOfChars(ms==1 ? 3:ms, ms==1 ? '#' : '0'));
			millis = df.format(fraction);
			if (millis.charAt(0) == '1') {
				sec++; //seconds can be 60 now - we solve it later!
			}
			millis = millis.substring(2);
		}
		if (sec == 60) {
			min++;
			sec = 0;
			if (min == 60) {
				min = 0;
				hour++; //it may be 24:00:00!
			}
		}
		int fpos = 0;
		Stack<Integer> optionals = new Stack<>();
		boolean valid = true;
		while (fpos < flen) {
			char pat = format.charAt(fpos++);
			if (pat == '\'' || pat == '"') { //quoted string (literal)
				char delim = pat;
				while(fpos + 1 < flen) {
					char ch;
					if ((ch = format.charAt(fpos++)) == delim) {
						if (fpos >= flen || format.charAt(fpos) != delim) {
							break;
						}
						fpos++;
					}
					sb.append(ch);
				}
				continue;
			}
			int i = 1;
			while (fpos < flen && format.charAt(fpos) == pat) {
				fpos++;
				i++;
			}
			switch (pat) {
				case '[':
					for (int j = 0; j < i; j++) {
						optionals.push(sb.length() + 1);
					}
					continue;
				case ']': {
					int x = 0;
					for (int j = 0; j < i; j++) {
						x = optionals.pop();
						if (x < 0) {
							sb.setLength(-x -1);
						}
					}
					if (x > 0) {
						valid = true;
					}
					continue;
				}
				case '|':
					if (valid) {
						fpos = flen; // break while!
					} else {
						sb.setLength(0);
						if (!optionals.empty()) {
							optionals.pop();
						}
					}
					continue;
				case '?': {//one of chars
					char delim;
					if (fpos < flen && ((delim=format.charAt(fpos)) == '\'' || delim == '"')) {
						sb.append(format.charAt(++fpos)); //appends the first
						while(fpos < flen) {
							if (format.charAt(fpos++) == delim) {
								if (fpos >= flen ||
									format.charAt(fpos) != delim) {
									break;
								}
								fpos++;
							}
						}
					} else {
						//Datetime mask format: unclosed quoted literal or section&{0}{, position: }
						throw new SRuntimeException(SYS.SYS049, fpos);

					}
					continue;
				}
				case '{': {
					StringParser p = new StringParser(format, fpos);
					Report r = p.checkDateFormatDefaults();
					if (r != null) {
						throw new SRuntimeException(r);
					}
					fpos = p.getIndex();
					SDatetime x = p.getParsedSDatetime();
					dfs = x == null ? SDatetime.DFS : x.getLocaleFormatSymbols();
					continue;
				}
				case 'a': //AM/PM
					if (hour == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					sb.append(dfs.getAmPmStrings()[hour<12?0:1]);
					continue;
				case 'D': //day in year
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					formatInt(sb, calendar.get(Calendar.DAY_OF_YEAR), i);
					continue;
				case 'd': //day
					if (_day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					formatInt(sb, calendar.get(Calendar.DAY_OF_MONTH), i);
					continue;
				case 'E': //day in week (text)
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					if (i <= 3) {
						sb.append(dfs.getShortWeekdays()[
						calendar.get(Calendar.DAY_OF_WEEK)]);
					} else {
						sb.append(dfs.getWeekdays()
						[calendar.get(Calendar.DAY_OF_WEEK)]);
					}
					continue;
				case 'e': {//day of week (number)
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					int j = (j = calendar.get(Calendar.DAY_OF_WEEK))
						 == Calendar.SUNDAY ? 7 : --j;
					formatInt(sb, j, i);
					continue;
				}
				case 'F': //day of week in month
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					formatInt(sb, calendar.get(Calendar.WEEK_OF_MONTH), i);
					continue;
				case 'G': //era
					if (year == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					sb.append(dfs.getEras()[year < 0 ? 0 : 1]);
					continue;
				case 'H': //hour 0..24
				case 'h': //hour 1..12
				case 'k': //hour in day (1..24)
				case 'K': {//hour 0..11
					if (_hour == Integer.MIN_VALUE) {
						if (!optionals.empty()) {
							setOptionInvalid(optionals);
							valid = false;
							continue;
						}
					}
					int j;
					if (pat == 'H' || pat == 'k') {//hour 0..23 or 1 .. 24
						j = (hour == 0 && pat == 'k') ? 24 : hour;
					} else {
						j = hour > 12 ? hour - 12 : hour;
						if (j == 0 && pat == 'h') {
							j = 12;
						}
					}
					formatInt(sb, j, i);
					continue;
				}
				case 'M': //month
					if (_month == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					if (i < 3) {
						formatInt(sb, calendar.get(Calendar.MONTH) + 1, i);
					} else if (i == 3) {
						sb.append(dfs.getShortMonths()[calendar.get(Calendar.MONTH)]);
					} else {
						sb.append(dfs.getMonths()[calendar.get(Calendar.MONTH)]);
					}
					continue;
				case 'm': //minute
					if (_minute == Integer.MIN_VALUE) {
						if (!optionals.empty()) {
							setOptionInvalid(optionals);
							valid = false;
							continue;
						}
					}
					formatInt(sb, min, i);
					continue;
				case 'R': {
					if (year == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
/*
 Two digits year (see Oracle). Century is generated accroding following rules:
 If RR is from the interval  00 .. 49 then
 a) if last two digits of the actual year are 00..49 then the century is taken from the actual year.
 b) if last two digits of the actual year are 50..99 then the century is taken
	from the actual year increased by 1.
 If RR is from the interval  50 .. 99 then
 c) if last two digits of the actual year are 00..49 then the century is taken
	from the actual year decreased by 1.
 d) if last two digits of the actual year are 50..99 then the century is taken from the actual year.
*/
					int r = new GregorianCalendar().get(Calendar.YEAR);
					int c = r / 100;
					r = r % 100; //two last digits from the actual year
					int y = year % 100; //last two digits of the year from date
					if (y < 50) {
						if (year != (r < 50 ? c * 100 + y : ((c + 1) * 100  + y))) {
							formatInt(sb, year, 0);
							continue;
						}
					} else {
						if (year != (r < 50 ? (c - 1) * 100 + y : (c * 100  + y))) {
							formatInt(sb, year, 0);
							continue;
						}
					}
					formatInt(sb, y, 2);
					continue;
				}
				case 'S': //millisecond
					if (_second == Integer.MIN_VALUE || (_fraction == 0.0D && ms <= 1)) {
						if (optionals.empty()) {
							sb.append('0'); // not oprional -> force 0 millis
						} else { //skip this option if it is optional
							setOptionInvalid(optionals);
							valid = false;
						}
						continue;
					}
					sb.append(millis);
					continue;
				case 's': //second
					if (_second == Integer.MIN_VALUE) {
						if (!optionals.empty()) {
							valid = false;
							setOptionInvalid(optionals);
							continue;
						}
					}
					formatInt(sb, sec, i);
					continue;
				case 'y': {//year
					if (year == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					int j;
					if (year < 0) {
						j = i < 5 ? 5 : i;
					} else {
						j = i == 2 ? 2 : i < 4 ? 4 : i;
					}
					formatInt(sb,i==2 ? year%100 : year, j);
					continue;
				}
				case 'Y': {//year
					if (year == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					int y;
					if (year < 0) {
						sb.append('-');
						y = -year;
						formatInt(sb, y, 4);
					} else {
						y = year;
					}
					int r = new GregorianCalendar().get(Calendar.YEAR)/100;
					if (y / 100 != r) {
						formatInt(sb, y, 4);
					} else {
						formatInt(sb, y%100, 2);
					}
					continue;
				}
				case 'w': //week in year
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					int w = calendar.get(Calendar.WEEK_OF_YEAR);
					if (w == 1 && calendar.get(Calendar.MONTH) == 11) {
						GregorianCalendar c1 = new GregorianCalendar(
							calendar.get(Calendar.YEAR), 11, calendar.get(Calendar.DAY_OF_MONTH));
						c1.add(Calendar.DAY_OF_MONTH, -7);//last week in year
						w = c1.get(Calendar.WEEK_OF_YEAR) + 1;
					}
					int v;
					if ((v = calendar.get(Calendar.DAY_OF_WEEK)) >= calendar.getFirstDayOfWeek()
						&& v == Calendar.SUNDAY) {
						w--; //starts from monday
					}
					formatInt(sb, w, i);
					continue;
				case 'W': //week in month
					if (year == Integer.MIN_VALUE || _month == Integer.MIN_VALUE
						|| _day == Integer.MIN_VALUE) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					formatInt(sb, calendar.get(Calendar.WEEK_OF_MONTH), i);
					continue;
				case 'Z': {//ISO zone
					if (_tz == null || "_null_".equals(_tz.getID())) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					int zoneHour = calendar.get(Calendar.DST_OFFSET)
						+ calendar.get(Calendar.ZONE_OFFSET);
					if (zoneHour == 0 && i == 1) {
						sb.append('Z');
						continue;
					}
					if (zoneHour < 0) {
						sb.append('-');
						zoneHour = -zoneHour;
					} else {
						sb.append('+');
					}
					zoneHour /= 60000;
					int zoneMinute = zoneHour % 60;
					zoneHour /= 60;
					formatInt(sb, zoneHour, 2);
					if (i == 1 || i == 2 || i == 6) {
						sb.append(':');
					}
					formatInt(sb, zoneMinute, 2);
					continue;
				}
				case 'z': { // RFC822 zone (CET etc...)
					if (_tz == null || "_null_".equals(_tz.getID())
						// if zone is UTC and field is optional then skip it
						|| "UTC".equals(_tz.getID()) && !optionals.empty()) {
						setOptionInvalid(optionals);
						valid = false;
						continue;
					}
					sb.append(_tz.getDisplayName(
						_tz.useDaylightTime(), i > 1 ? TimeZone.LONG : TimeZone.SHORT));
					continue;
				}
				default:
					for (int j = 0; j < i; j++) {
						sb.append(pat);
					}
			}
		}
	}

	/** Create string with date according to given format.
	 * @param format format of created date.
	 * @return string with formatted date.
	 */
	public final String formatDate(final String format) {
		StringBuffer sb;
		formatDate(sb = new StringBuffer(), format);
		return sb.toString();
	}

	/** Convert integer to StringBuffer; insert leading zeroes.
	 * @param sb The StringBuffer.
	 * @param value The integer value.
	 * @param size size of result (if result has more valid digits the size will be increased
	 * to the number of valid digits).
	 */
	private static void formatInt(final StringBuffer sb, final int value, final int size) {
		int startPos = sb.length();
		sb.append(value);
		int pad;
		if (size == 0 || (pad = size - (sb.length() - startPos)) <= 0) {
			return;
		}
		if (value < 0) { //minus
			startPos++;
		}
		while (pad-- > 0) {
			sb.insert(startPos, '0'); //isnsert leading zeroes
		}
	}

////////////////////////////////////////////////////////////////////////////////
// Static methods
////////////////////////////////////////////////////////////////////////////////

	/** Get java.util.Calendar created from SDatetime object.
	 * @param sd SDatetime object or null.
	 * @return java.util.Calendar created from SDatetime object or null.
	 */
	public static final Calendar getCalendar(final SDatetime sd) {
		return sd != null ? sd.getCalendar() : null;
	}

	/** Get java.util.Date created from SDatetime object.
	 * @param sd SDatetime object or null.
	 * @return java.util.Date created from SDatetime object or null.
	 */
	public static final Date getDate(final SDatetime sd) {
		return sd != null ? sd.getCalendar().getTime() : null;
	}

	/** Get java.sql.Timestamp created from SDatetime object.
	 * @param sd SDatetime object or null.
	 * @return java.sql.Timestamp created from SDatetime object or null.
	 */
	public static final Timestamp getTimestamp(final SDatetime sd) {
		if (sd == null) {
			return null;
		}
		Timestamp t = new Timestamp(sd.getTimeInMillis());
		t.setNanos(sd.getNanos());
		return t;
	}

	/** Returns the current date and time.
	 * @return A date representing the current date and time.
	 */
	public static final SDatetime now() {
		return new SDatetime(new GregorianCalendar());
	}

	/** Get a day of month which represents the last date of the month of given date.
	 * @param date date from which last day of month is to be computed.
	 * @return day of month.
	 */
	public static final int getLastDayOfMonth(final SDatetime date) {
		return date.getLastDayOfMonth();
	}

	/** Get a day of month which represents the last date of the month of given date.
	 * @param date date from which last day of month is to be computed.
	 * @return day of month.
	 */
	public static final int getLastDayOfMonth(final Calendar date) {
		return new SDatetime(date).getLastDayOfMonth();
	}

	/** Get Easter Monday. The algorithm used here was published in the Explanatory Supplement
	 * to the Astronomical Almanac, Oudin J.M. (1940),  ed. Seidelmann P. K. (1992).
	 * @param date the date containing year where we compute Easter Monday.
	 * @return SDatetime object with Easter Monday.
	 * @throws SRuntimeException if Easter Monday can't be computed.
	 */
	public static final SDatetime getEasterMonday(final Calendar date) {
		if (date.isSet(Calendar.YEAR)) {
			return getEasterMonday(date.get(Calendar.YEAR));
		}
		throw new SRuntimeException(SYS.SYS092); //Year is undefined
	}

	/** Get Easter Monday. The algorithm used here was published in the Explanatory Supplement
	 * to the Astronomical Almanac, Oudin J.M. (1940),  ed. Seidelmann P. K. (1992).
	 * @param year the year where we compute Easter Monday
	 * @return SDatetime object with Easter Monday. If Easter Monday can't
	 * be computed then returns null.
	 */
	public static final SDatetime getEasterMonday(final int year) {
		int julian_start = 325;
//		int gregorian_start = 1583;
		if (year < julian_start) {
			return null;
		}
		//www.mathworks.com/matlabcentral/fileexchange/30885-easter-easter-sunday/content/easter.m
		int c = year / 100;
		int n = year - 19 * (year / 19);
		int k = (c - 17) / 25;
		int i = c - c / 4 - (c - k) / 3 + 19 * n + 15;
		i = i - 30 * (i / 30);
		i = i - (i / 28) * (1 - (i / 28) * (29 / (i + 1)) * (( 21 - n) / 11));
		int j = year + year / 4 + i + 2 - c + c / 4;
		j = j - 7 * (j / 7);
		int p = i - j;
		int m = 3 + (p + 40) / 44;
		int d = p + 28 - 31 * (m / 4) + 1;
		if (d > 31) {
			m++;
			d -= 31;
		}
		return new SDatetime(year, m, d);
	}

	/** Check if the year given by argument is a leap year or not.
	 * @param year year which is examined.
	 * @return true if is year is a leap year, otherwise return false.
	 */
	public static final boolean isLeapYear(final int year) {
		return ((year % 4 == 0) && (year % 100 != 0)) || year % 400 == 0;
	}

	/** Get the number of calendar years date2 after date1. If date2 is before date1 a negative
	 * number is returned. This method does not take into account time, days or months. Therefore,
	 * the difference between 2000-12-31 11:23:54 PM and 2001-01-01 00:01:12 AM is one year.
	 * @param d1 first date in the range.
	 * @param d2 second date in the range.
	 * @return number of years date2 after date1. If date2 is before date1 return negative integer.
	 */
	public static final int getCalendarYearsDifference(final SDatetime d1, final SDatetime d2) {
		return d2._year - d1._year;
	}

	/** Get the number of calendar months date2 is isAfter date1. If date2 is before date1, then
	 * negative number is returned. This method does not take into account time or days. Therefore,
	 * the difference between 2001-04-31 11:23:54 PM and 2001-05-01 00:01:12 AM is one month.
	 * @param d1 first date in the range.
	 * @param d2 second date in the range.
	 * @return number of months date2 after date1. If date2 is before date1 return negative integer.
	 */
	public static final int getCalendarMonthsDifference(final SDatetime d1, final SDatetime d2) {
		if (d1._year == d2._year) {
			return d2._month - d1._month;
		} else if (d1._year < d2._year) {
			int yearCtr = d2._year - 1;
			int difference = d2._month;
			while (yearCtr > d1._year) {
				difference = difference + 12;
				yearCtr--;
			}
			return difference + 12 - d1._month;
		} else { //if (date1._year > date2._year)
			int yearCtr = d1._year - 1;
			int difference = d1._month;
			while (yearCtr > d2._year) {
				difference = difference + 12;
				yearCtr--;
			}
			return - (difference + 12 - d2._month);
		}
	}

	/** Get the number of calendar days date2 is isAfter date1. If date2 is* before date1, then
	 * negative number is returned. This method does not take into account time or days. Therefore,
	 * the difference between 2001-04-31 11:23:54Z PM and 2001-04-02 00:01:12Z AM is one day.
	 * @param d1 first date in the range.
	 * @param d2 second date in the range.
	 * @return number of months date2 after date1. If date2 is before date1 return negative integer.
	 */
	public static final int getCalendarDaysDifference(final SDatetime d1, final SDatetime d2) {
		SDatetime c1 = new SDatetime(d1._year, d1._month -1 , d1._day, 0, 0, 0, 0.0D, d1._tz);
		SDatetime c2 = new SDatetime(d2._year, d2._month -1 , d2._day, 0, 0, 0, 0.0D, d2._tz);
		return (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / 86400000);
	}

	/** Create string with date according to given format.
	 * @param calendar date and/or time.
	 * @param format format of created date.
	 * @return string with formatted date.
	 */
	public static final String formatDate(final Calendar calendar,
		final String format) {
		StringBuffer sb = new StringBuffer();
		new SDatetime(calendar).formatDate(sb, format);
		return sb.toString();
	}

	/** Conversion of Calendar object to ISO 8601 string.
	 * @param time Calendar object with date and time.
	 * @return string with ISO 8601 date and time format.
	 */
	public static final String toISO8601(final Calendar time) {
		return formatDate(time, "yyyy-MM-ddTHH:mm:ssZ");
	}

	/** Conversion to RFC822 string.
	 * @param time Calendar object with date and time.
	 * @return string with RFC822 date and time format.
	 */
	public static final String toRFC822(final Calendar time) {
		return new SDatetime(time).toRFC822();
	}

	/** Conversion of string with date in ISO 8601 format to Calendar.
	 * @param source string with date.
	 * @return Calendar object.
	 */
	public static final Calendar fromISO8601(final String source) {
		StringParser p = new StringParser(source);
		return p.isISO8601Datetime() && p.testParsedDatetime() ? p.getParsedCalendar() : null;
	}

	/** Conversion of string with date in RFC822 format to Calendar.
	 * @param source string with date.
	 * @return created Calendar object.
	 */
	public static final Calendar fromRFC822(final String source) {
		StringParser p = new StringParser(source);
		return p.isRFC822Datetime() && p.testParsedDatetime() ? p.getParsedCalendar() : null;
	}

	/** Parse string with date according given format pattern and convert it to Calendar object.
	 * @param source The source string with date.
	 * @param format The pattern with format of source.
	 * @return The calendar object or null if date was not recognized.
	 * @throws SRuntimeException
	 * <br>SYS049 Unclosed string quotation.
	 * <br>SYS050 Incorrect zone specification in declaration of date and/or time format.
	 */
	public static final Calendar parseDatetime(final String source, final String format) {
		StringParser p = new StringParser(source);
		return p.isDatetime(format) && p.testParsedDatetime() ? p.getParsedCalendar() : null;
	}

	/** Parse datetime according to ISO8601.
	 * @param source source format of datetime.
	 * @return SDatetime object result of parsing.
	 */
	private static SDatetime parse(final String source) {
		StringParser p = new StringParser(source);
		if (p.isISO8601Datetime() && p.eos() && p.testParsedDatetime()) {
			return p.getParsedSDatetime();
		}
		throw new SRuntimeException(SYS.SYS040); //Datetime error&{0}{: }
	}

	/** Parse date in ISO8601 format "yyyy-M-dTH:m[:s[.S]][Z]" (see
	 * <a href = "http://www.w3.org/TR/NOTE-datetime"> www.w3.org/TR/NOTE-datetime</a>).
	 * @param src String with date.
	 * @param pos Position from which date is parsed.
	 * @return true if date on current position suits to required format.
	 */
	public static final SDatetime parseISO8601(final String src, final int pos){
		StringParser p = new StringParser(src);
		p.setIndex(pos);
		return p.isISO8601Datetime() ? p.getParsedSDatetime() : null;
	}

	/** Parse date in ISO8601 format "yyyy-M-dTH:m[:s[.S]][Z]" (see
	 * <a href = "http://www.w3.org/TR/NOTE-datetime"> www.w3.org/TR/NOTE-datetime</a>).
	 * @param src String with date.
	 * @return true if date on current position suits to required format, otherwise return false.
	 */
	public static final SDatetime parseISO8601(final String src) {
		return parseISO8601(src, 0);
	}

	/** Parse date and time in RFC822 format.
	 * @param src String with date.
	 * @param pos Position from which date is parsed.
	 * @return true if date on current position suits to required format, otherwise return false.
	 */
	public static final SDatetime parseRFC822(final String src, final int pos) {
		StringParser p = new StringParser(src);
		p.setIndex(pos);
		return p.isRFC822Datetime() ? p.getParsedSDatetime() : null;
	}

	/** Parse date and time in RFC822 format.
	 * @param src String with date.
	 * @return true if date on current position suits to required format, otherwise return false.
	 */
	public static final SDatetime parseRFC822(final String src) {return parseRFC822(src, 0);}

	/** Parse date and time from a string according to given format.
	 * @param src string with source data.
	 * @param format format of source data.
	 * @return SDatetime object.
	 */
	public static final SDatetime parse(final String src, final String format) {
		return parse(src, 0, format);
	}

	/** Parse date and time from a string according to given format.
	 * @param src the string with source data.
	 * @param pos the position of source data to be parsed.
	 * @param format the format of source data.
	 * @return the SDatetime object.
	 */
	public static final SDatetime parse(final String src, final int pos, final String format) {
		StringParser p = new StringParser(src);
		p.setIndex(pos);
		return p.isDatetime(format) ? p.getParsedSDatetime() : null;
	}

	/** maximumDayInMonthFor(yearValue, monthValue) =
	 *    M := modulo(monthValue, 1, 13)
	 *    Y := yearValue + fQuotient(monthValue, 1, 13)
	 * Return a value based on M and Y:
	 * M = January, March, May, July, August, October, or December
	 * M = April, June, September, or November
	 * M = February AND (modulo(Y, 400) = 0 OR (modulo(Y, 100) != 0) AND modulo(Y, 4) = 0)
	 */
	private static int maximumDayInMonthFor(final int year, final int month) {
		int m = modulo(month, 1, 13);
		if (m==1 || m==3 || m==5 || m==7 || m==8 || m==10 || m==12) {
			return 31;
		}
		if (m != 2) {
			return 30;
		}
		int y = year + fQuotient(month, 1, 13);
		if ((y % 4 == 0) && (y % 100 != 0 || y % 400 == 0)) {
			return 29;
		}
		return 28;
	}

	/** Write this object to given SObjectWriter.
	 * @param w where to write.
	 * @throws IOException if an error occurs.
	 */
	public final void writeObj(final SObjectWriter w) throws IOException {
		w.writeInt(_year);
		w.writeByte(_month < 0 ? -1 : (byte) _month);
		w.writeByte(_day < 0 ? -1 : (byte) _day);
		w.writeByte(_hour < 0 ? -1 : (byte) _hour);
		w.writeByte(_minute < 0 ? -1 : (byte) _minute);
		w.writeByte(_second < 0 ? -1 : (byte) _second);
		w.writeDouble(_fraction);
		w.writeInt(_eon);
		if (_tz == null) {
			w.writeBoolean(false);
		} else {
			w.writeBoolean(true);
			w.writeString(_tz.getDisplayName());
			w.writeInt(_tz.getRawOffset());
		}
	}

	/** Read new instance of SDatetime from given SObjectReader.
	 * @param r where to read.
	 * @return new instance of SDatetime from argument.
	 * @throws IOException if an error occurs.
	 */
	public static final SDatetime readObj(final SObjectReader r) throws IOException {
		SDatetime x = new SDatetime();
		x._year = r.readInt();
		byte b = r.readByte();
		x._month = b < 0 ? Integer.MIN_VALUE : b;
		b = r.readByte();
		x._day = b < 0 ? Integer.MIN_VALUE : b;
		b = r.readByte();
		x._hour = b < 0 ? Integer.MIN_VALUE : b;
		b = r.readByte();
		x._minute = b < 0 ? Integer.MIN_VALUE : b;
		b = r.readByte();
		x._second = b < 0 ? Integer.MIN_VALUE : b;
		x._fraction = r.readDouble();
		x._eon = r.readInt();
		if (r.readBoolean()) {
			x._tz = TimeZone.getTimeZone(r.readString());
			x._tz.setRawOffset(r.readInt());
		} else {
			x._tz = null;
		}
		return x;
	}

	/** Get time zone from this instance of datetime.
	 * @return TimeZone object.
	 */
	public final TimeZone getTimeZone() {return _tz;}

////////////////////////////////////////////////////////////////////////////////
// Implementation of javax.xml.datatype.XMLGregorianCalendar
////////////////////////////////////////////////////////////////////////////////

	@Override
	public final void reset() {
		synchronized(this) {
			resetOriginalValues();
			_dfs = null;
			_calendar = null;
		}
	}

	@Override
	public final void clear() {init();}

	@Override
	public final void setYear(final BigInteger year) {
		synchronized(this) {
			if (year == null) {
				_eon = 0;
				_year = Integer.MIN_VALUE;
			} else {
				BigInteger temp = year.remainder(BILLION_I);
				_year = temp.intValue();
				temp = year.subtract(temp);
				_eon = temp.compareTo(BigInteger.ZERO) == 0 ? 0 : year.divide(BILLION_I).intValue();
			}
		}
	}

	/** Set the Raw time zone offset.
	 * @param offset The time zone offset in millisecond.
	 */
	public final void setRawZoneOffset(final int offset) {
		synchronized(this) {
			Calendar c = getCalendar();
			TimeZone tz = (TimeZone) TimeZone.getTimeZone("UTC").clone();
			tz.setRawOffset(offset);
			final int maxOffset = 14*24*60*60*1000;
			if (offset > maxOffset || maxOffset < -maxOffset || (offset % 1000) != 0) {
				throw new RuntimeException("Incorrect time zone RawOffset: " + offset);
			}
			tz.setID("");
			int diff = offset - (_tz != null ? _tz.getRawOffset() + _tz.getDSTSavings() : 0);
			int hour = _hour;
			int minute = _minute;
			c.setTimeZone(tz);
			_tz = (TimeZone) tz.clone();
			setCalendar(c);
			if (diff != 0 && _hour == hour && _minute == minute) {
				c.add(Calendar.MILLISECOND, diff);
			}
		}
	}

	@Override
	/** Number of minutes of time zone offset or
	 * (DatatypeConstants.FIELD_UNDEFINED, i.e. Integer.MIN_VALUE).
	 * @param minutes of time zone offset; range from -14 hours (-14 * 60) to 14 hours (14 * 60).
	 */
	public final void setTimezone(final int offset) {
		synchronized(this) {
			if(offset<-14*60 || 14*60<offset) {
				if(offset == Integer.MIN_VALUE) {
					_tz = null;
				} else {
					throw new SRuntimeException(SYS.SYS055); //Invalid value of timezone offset
				}
			} else {
				int x = offset < 0 ? -offset: offset;
				int hour = x / 60;
				int minutes = x - (hour * 60);
				String timezoneId =
					"GMT" + (offset>0 ? "+" : "-") + (hour<0 ? -hour : hour);
				if (minutes != 0) {
					timezoneId += ":" + minutes;
				}
				_tz = TimeZone.getTimeZone(timezoneId);
			}
		}
	}

	@Override
	public final void setFractionalSecond(final BigDecimal fractional) {
		setFraction(fractional == null ? 0 : fractional.doubleValue());
	}

	@Override
	public final BigInteger getEon() {
		return _eon == 0 ? null : BigInteger.valueOf(_eon).multiply(BILLION_I);
	}

	@Override
	public final BigInteger getEonAndYear() {
		return _year == Integer.MIN_VALUE ? null
			: _eon != 0 ? getEon().add(BigInteger.valueOf(_year)) : BigInteger.valueOf(_year);
	}

	@Override
	/** Get time zone offset in minutes.
	 * @return minutes of time zone offset or Integer.MIN_VALUE (DatatypeConstants.FIELD_UNDEFINED).
	 */
	public final int getTimezone() {
		if (_tz == null) {
			return Integer.MIN_VALUE;
		}
		if (_year != Integer.MIN_VALUE && _month != Integer.MIN_VALUE && _day != Integer.MIN_VALUE){
			return _tz.getOffset(getTime().getTime()) / 60000;
		}
		return _tz.getRawOffset() / 60000;
	}

	@Override
	public final BigDecimal getFractionalSecond() {
		return new BigDecimal(getFraction());
	}

	@Override
	public final int compare(final XMLGregorianCalendar x) {
		if (x instanceof SDatetime) {
			return compareTo((SDatetime) x);
		} else if (null == x) {
			return compareTo(null);
		} else {
			SDatetime y = new SDatetime(x.toGregorianCalendar());
			BigInteger e = x.getEon();
			if (e != null) {
				y._eon = e.divide(BILLION_I).intValue();
			}
			return compareTo(y);
		}
	}

	@Override
	/** Normalize this instance to UTC.
	 * @return normalized XMLGregorianCalendar.
	 */
	public final SDatetime normalize() {
		SDatetime result = (SDatetime) clone();
		if (_tz != null && _tz != NULL_ZONE) {
			result.addMillis(-getTimezone() * 60000);
			result._tz = UTC_ZONE;
		}
		return result;
	}

	@Override
	public final String toXMLFormat() {
		QName typekind = getXMLSchemaType();
		String mask = typekind == DatatypeConstants.DATE ? "y-MM-dd"
			: typekind == DatatypeConstants.TIME ? "HH:mm:ss"
			: typekind == DatatypeConstants.GMONTH ? "--MM"
			: typekind == DatatypeConstants.GDAY ? "---dd"
			: typekind == DatatypeConstants.GYEAR ? "Y"
			: typekind == DatatypeConstants.GYEARMONTH ? mask = "y-MM"
			: typekind == DatatypeConstants.GMONTHDAY ?  "--MM-dd"
			: "y-MM-dd'T'HH:mm:ss"; //DATETIME
		return formatDate(mask + (_tz != null ? "Z" : ""));
	}

	@Override
	public final QName getXMLSchemaType() {
		switch ((_year!=Integer.MIN_VALUE ? 0x20 : 0)
			| (_month!=Integer.MIN_VALUE ? 0x10 : 0)
			| (_day!=Integer.MIN_VALUE ? 0x08 : 0)
			| (_hour!=Integer.MIN_VALUE ? 0x04 : 0)
			| (_minute!= Integer.MIN_VALUE ? 0x02 : 0)
			| (_second!=Integer.MIN_VALUE ? 0x01 : 0)) {
			case 0x3F: return DatatypeConstants.DATETIME;
			case 0x38: return DatatypeConstants.DATE;
			case 0x07: return DatatypeConstants.TIME;
			case 0x30: return DatatypeConstants.GYEARMONTH;
			case 0x18: return DatatypeConstants.GMONTHDAY;
			case 0x20: return DatatypeConstants.GYEAR;
			case 0x10: return DatatypeConstants.GMONTH;
			case 0x08: return DatatypeConstants.GDAY;
		}
		throw new IllegalStateException(
			this.getClass().getName() + "#getXMLSchemaType(): InvalidFields");
	}

	@Override
	public final boolean isValid() {return chkDatetime();}

	@Override
	public final void add(final Duration duration) {
		int sign = duration.getSign();
		int years = duration.getYears();
		years = years == Integer.MIN_VALUE ? 0 : sign * years;
		int months = duration.getMonths();
		months = months == Integer.MIN_VALUE ? 0 : sign * months;
		int days = duration.getDays();
		days = days == Integer.MIN_VALUE ? 0 : sign * days;
		int hours = duration.getHours();
		hours = hours == Integer.MIN_VALUE ? 0 : sign * hours;
		int minutes = duration.getMinutes();
		minutes = minutes == Integer.MIN_VALUE ? 0 : sign * minutes;
		int seconds = duration.getSeconds();
		seconds = seconds == Integer.MIN_VALUE ? 0 : sign * seconds;
		double fraction = 0.0D;
		if (duration instanceof SDuration) {
			fraction = sign * ((SDuration) duration).getFraction();
		}
		add(years, months, days, hours, minutes, seconds, fraction);
	}

	@Override
	public final GregorianCalendar toGregorianCalendar() {return (GregorianCalendar) getCalendar();}

	@Override
	public final GregorianCalendar toGregorianCalendar(final TimeZone timezone,
		final Locale locale,
		final XMLGregorianCalendar defaults) {
		TimeZone tz = timezone;
		if (tz == null) {
			int defaultZoneoffset = Integer.MIN_VALUE;
			if (defaults != null) {
				defaultZoneoffset = defaults.getTimezone();
			}
			tz = getTimeZone(defaultZoneoffset);
		}
		Locale aLocale = locale == null ? Locale.getDefault() : locale;
		GregorianCalendar result = new GregorianCalendar(tz, aLocale);
		result.clear();
		result.setGregorianChange(new Date(Long.MIN_VALUE));
		// if year( and eon) are undefined, leave default Calendar values
		BigInteger year = getEonAndYear();
		if (year != null) {
			result.set(Calendar.ERA,
				year.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
			result.set(Calendar.YEAR, year.abs().intValue());
		} else {
			// use default if set
			BigInteger defaultYear = (defaults != null) ?
				defaults.getEonAndYear() : null;
			if (defaultYear != null) {
				result.set(Calendar.ERA,
					defaultYear.signum() == -1 ? GregorianCalendar.BC : GregorianCalendar.AD);
				result.set(Calendar.YEAR, defaultYear.abs().intValue());
			}
		}

		if (_month != Integer.MIN_VALUE) { // only set month if it is set
			// Calendar.MONTH is zero based while XMLGregorianCalendar month field is not.
			result.set(Calendar.MONTH, _month - 1);
		} else { // use default if set
			int defaultMonth = (defaults != null) ?
				defaults.getMonth() : Integer.MIN_VALUE;
			if (defaultMonth != Integer.MIN_VALUE) {
				// Calendar.MONTH is zero based while XMLGregorianCalendar month field is not.
				result.set(Calendar.MONTH, defaultMonth - 1);
			}
		}
		if (_day != Integer.MIN_VALUE) {// only set day if it is set
			result.set(Calendar.DAY_OF_MONTH, _day);
		} else { // use default if set
			int defaultDay = (defaults != null) ? defaults.getDay() : Integer.MIN_VALUE;
			if (defaultDay != Integer.MIN_VALUE) {
				result.set(Calendar.DAY_OF_MONTH, defaultDay);
			}
		}
		if (_hour != Integer.MIN_VALUE) { // only set hour if it is set
			result.set(Calendar.HOUR_OF_DAY, _hour);
		} else { // use default if set
			int defaultHour = (defaults != null) ? defaults.getHour() : Integer.MIN_VALUE;
			if (defaultHour != Integer.MIN_VALUE) {
				result.set(Calendar.HOUR_OF_DAY, defaultHour);
			}
		}
		if (_minute != Integer.MIN_VALUE) { // only set minute if it is set
			result.set(Calendar.MINUTE, _minute);
		} else { // use default if set
			int defaultMinute = (defaults != null) ? defaults.getMinute() : Integer.MIN_VALUE;
			if (defaultMinute != Integer.MIN_VALUE) {
				result.set(Calendar.MINUTE, defaultMinute);
			}
		}
		if (_second != Integer.MIN_VALUE) { // only set second if it is set
			result.set(Calendar.SECOND, _second);
		} else { // use default if set
			int defaultSecond = (defaults != null) ? defaults.getSecond() : Integer.MIN_VALUE;
			if (defaultSecond != Integer.MIN_VALUE) {
				result.set(Calendar.SECOND, defaultSecond);
			}
		}
		if (_fraction != 0) { // only set millisend if it is set
			result.set(Calendar.MILLISECOND, getMillisecond());
		} else { // use default if set
			if (defaults != null) {
				BigDecimal defaultFractSecond = defaults.getFractionalSecond();
				if (defaultFractSecond != null) {
					result.set(Calendar.MILLISECOND, defaults.getMillisecond());
				}
			}
		}
		return result;
	}

	@Override
	public final TimeZone getTimeZone(final int defaultZoneoffset) {return _tz;}

////////////////////////////////////////////////////////////////////////////////////////////////////
//	@Deprecated
//	/** Get the first day of the week is; e.g., SUNDAY in the U.S., MONDAY in France.
//	 * This method is deprecated, will be removed in future versions.
//	 * @return first day of the week (e.g., SUNDAY in the U.S., MONDAY in France).
//	 */
//	public final int getFirstDayOfWeek() {return getCalendar().getFirstDayOfWeek();}
}