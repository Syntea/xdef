package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdNames;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** XML Schema base type.
 * @author Ilia Alexandrov
 */
public class XsdBase extends ValueType {

/*VT*/
	/** Sequential parameters with length specification. */
	private final static String[][] SQ_LEN = new String[][] {
		new String[] {"length"},
		new String[] {"minLength", "maxLength"}};
	/** Sequential parameters with min/max specification. */
	private final static String[][] SQ_MINMAX = new String[][] {
		new String[] {"minInclusive", "maxInclusive"},
		new String[] {"minInclusive", "maxInclusive"}};
	/** Sequential parameters with min/max specification. */
	private final static String[][] SQ_DEC = new String[][] {
		new String[] {"totalDigits"},
		new String[] {"totalDigits", "fractionDigits"}};
/*VT*/

	/** Schema <code>anyURI</code> type. */
	public static final XsdBase ANY_URI =
		new XsdBase(Id.ANY_URI, XsdNames.ANY_URI, XdNames.XS_ANY_URI, null);
	/** Schema <code>base64Binary</code> type. */
	public static final XsdBase BASE64_BINARY = new XsdBase(Id.BASE64_BINARY,
		XsdNames.BASE_64_BINARY, XdNames.XS_BASE_64_BINARY, SQ_LEN);
	/** Schema <code>boolean</code> type. */
	public static final XsdBase BOOLEAN =
		new XsdBase(Id.BOOLEAN, XsdNames.BOOLEAN, XdNames.XS_BOOLEAN, null);
	/** Schema <code>byte</code> type. */
	public static final XsdBase BYTE =
		new XsdBase(Id.BYTE, XsdNames.BYTE, XdNames.XS_BYTE, SQ_MINMAX);
	/** Schema <code>date</code> type. */
	public static final XsdBase DATE =
		new XsdBase(Id.DATE, XsdNames.DATE, XdNames.XS_DATE, SQ_MINMAX);
	/** Schema <code>dateTime</code> type.*/
	public static final XsdBase DATE_TIME = new XsdBase(Id.DATE_TIME,
		XsdNames.DATE_TIME, XdNames.XS_DATE_TIME, SQ_MINMAX);
	/** Schema <code>decimal</code> type. */
	public static final XsdBase DECIMAL = new XsdBase(Id.DECIMAL,
		XsdNames.DECIMAL, XdNames.XS_DECIMAL, SQ_DEC);
	/** Schema <code>double</code> type. */
	public static final XsdBase DOUBLE =
		new XsdBase(Id.DOUBLE, XsdNames.DOUBLE, XdNames.XS_DOUBLE, SQ_MINMAX);
	/** Schema <code>duration</code> type. */
	public static final XsdBase DURATION =
		new XsdBase(Id.DURATION, XsdNames.DURATION, XdNames.XS_DURATION, null);
	/** Schema <code>ENTITY</code> type. */
	public static final XsdBase ENTITY =
		new XsdBase(Id.ENTITY, XsdNames.ENTITY, null, null);
	/** Schema <code>ENTITIES</code> type. */
	public static final XsdBase ENTITIES =
		new XsdBase(Id.ENTITIES, XsdNames.ENTITIES, null, SQ_LEN);
	/** Schema <code>float</code> type. */
	public static final XsdBase FLOAT =
		new XsdBase(Id.FLOAT, XsdNames.FLOAT, XdNames.XS_FLOAT, SQ_MINMAX);
	/** Schema <code>gDay</code> type. */
	public static final XsdBase G_DAY =
		new XsdBase(Id.G_DAY, XsdNames.G_DAY, XdNames.XS_G_DAY, SQ_MINMAX);
	/** Schema <code>gMonth</code> type. */
	public static final XsdBase G_MONTH =
		new XsdBase(Id.G_MONTH, XsdNames.G_MONTH, XdNames.XS_G_MONTH,SQ_MINMAX);
	/** Schema <code>gMonthDay</code> type. */
	public static final XsdBase G_MONTH_DAY = new XsdBase(Id.G_MONTH_DAY,
			XsdNames.G_MONTH_DAY, XdNames.XS_G_MONTH_DAY, SQ_MINMAX);
	/** Schema <code>gYear</code> type. */
	public static final XsdBase G_YEAR =
		new XsdBase(Id.G_YEAR, XsdNames.G_YEAR, XdNames.XS_G_YEAR, SQ_MINMAX);
	/** Schema <code>gYearMonth</code> type. */
	public static final XsdBase G_YEAR_MONTH = new XsdBase(Id.G_YEAR_MONTH,
		XsdNames.G_YEAR_MONTH, XdNames.XS_G_YEAR_MONTH, SQ_MINMAX);
	/** Schema <code>hexBinary</code> type. */
	public static final XsdBase HEX_BINARY = new XsdBase(Id.HEX_BINARY,
		XsdNames.HEX_BINARY, XdNames.XS_HEX_BINARY, SQ_LEN);
	/** Schema <code>ID</code> type. */
	public static final XsdBase ID = new XsdBase(Id.ID, XsdNames.ID, null, null);
	/** Schema <code>IDREF</code> type. */
	public static final XsdBase IDREF =
		new XsdBase(Id.IDREF, XsdNames.IDREF, null, null);
	/** Schema <code>IDREFS</code> type. */
	public static final XsdBase IDREFS =
		new XsdBase(Id.IDREFS, XsdNames.IDREFS, null, SQ_LEN);
	/** Schema <code>int</code> type. */
	public static final XsdBase INT =
		new XsdBase(Id.INT, XsdNames.INT, XdNames.XS_INT, SQ_MINMAX);
	/** Schema <code>integer</code> type. */
	public static final XsdBase INTEGER =
		new XsdBase(Id.INTEGER, XsdNames.INTEGER, XdNames.XS_INTEGER,SQ_MINMAX);
	/** Schema <code>language</code> type. */
	public static final XsdBase LANGUAGE =
		new XsdBase(Id.LANGUAGE, XsdNames.LANGUAGE, XdNames.XS_LANGUAGE, null);
	/** Schema <code>long</code> type. */
	public static final XsdBase LONG =
		new XsdBase(Id.LONG, XsdNames.LONG, XdNames.XS_LONG, SQ_MINMAX);
	/** Schema <code>Name</code> type. */
	public static final XsdBase NAME =
		new XsdBase(Id.NAME, XsdNames.NAME_TYPE, XdNames.XS_NAME, SQ_LEN);
	/** Schema <code>NCName</code> type. */
	public static final XsdBase NCNAME =
		new XsdBase(Id.NCNAME, XsdNames.NCNAME, XdNames.XS_NCNAME, SQ_LEN);
	/** Schema <code>negativeInteger</code> type. */
	public static final XsdBase NEGATIVE_INTEGER =
		new XsdBase(Id.NEGATIVE_INTEGER,
			XsdNames.NEGATIVE_INTEGER, XdNames.XS_NEGATIVE_INTEGER, SQ_MINMAX);
	/** Schema <code>NMTOKEN</code> type. */
	public static final XsdBase NMTOKEN =
		new XsdBase(Id.NMTOKEN, XsdNames.NMTOKEN, XdNames.XS_NMTOKEN, SQ_LEN);
	/** Schema <code>NMTOKENS</code> type. */
	public static final XsdBase NMTOKENS =
		new XsdBase(Id.NMTOKENS, XsdNames.NMTOKENS, null, SQ_LEN);
	/** Schema <code>nonNegativeInteger</code> type. */
	public static final XsdBase NON_NEGATIVE_INTEGER =
		new XsdBase(Id.NEGATIVE_INTEGER,
			XsdNames.NEGATIVE_INTEGER, XdNames.XS_NEGATIVE_INTEGER, SQ_MINMAX);
	/** Schema <code>nonPositiveInteger</code> type. */
	public static final XsdBase NON_POSITIVE_INTEGER =
		new XsdBase(Id.NON_POSITIVE_INTEGER, XsdNames.NON_POSITIVE_INTEGER,
			XdNames.XS_NON_POSITIVE_INTEGER, SQ_MINMAX);
	/** Schema <code>normalizedString</code> type. */
	public static final XsdBase NORMALIZED_STRING =
		new XsdBase(Id.NORMALIZED_STRING,
			XsdNames.NORMALIZED_STRING, XdNames.XS_NORMALIZED_STRING, SQ_LEN);
	/** Schema <code>NOTATION</code> type. */
	public static final XsdBase NOTATION =
		new XsdBase(Id.NOTATION, XsdNames.NOTATION, null, null);
	/** Schema <code>positiveInteger</code> type. */
	public static final XsdBase POSITIVE_INTEGER =
		new XsdBase(Id.POSITIVE_INTEGER,
			XsdNames.POSITIVE_INTEGER, XdNames.XS_POSITIVE_INTEGER, SQ_MINMAX);
	/** Schema <code>QName</code> type. */
	public static final XsdBase QNAME =
		new XsdBase(Id.QNAME, XsdNames.QNAME, null, SQ_LEN);
	/** Schema <code>short</code> type. */
	public static final XsdBase SHORT =
		new XsdBase(Id.SHORT, XsdNames.SHORT, XdNames.XS_SHORT, SQ_MINMAX);
	/** Schema <code>string</code> type. */
	public static final XsdBase STRING =
		new XsdBase(Id.STRING, XsdNames.STRING, XdNames.XS_STRING, SQ_LEN);
	/** Schema <code>time</code> type. */
	public static final XsdBase TIME =
		new XsdBase(Id.TIME, XsdNames.TIME, XdNames.XS_TIME, SQ_MINMAX);
	/** Schema <code>token</code> type. */
	public static final XsdBase TOKEN =
		new XsdBase(Id.TOKEN, XsdNames.TOKEN, XdNames.XS_TOKEN, SQ_LEN);
	/** Schema <code>unsignedByte</code> type. */
	public static final XsdBase UNSIGNED_BYTE = new XsdBase(Id.UNSIGNED_BYTE,
		XsdNames.UNSIGNED_BYTE, XdNames.XS_UNSIGNED_BYTE, SQ_MINMAX);
	/** Schema <code>unsignedShort</code> type. */
	public static final XsdBase UNSIGNED_SHORT = new XsdBase(Id.UNSIGNED_SHORT,
		XsdNames.UNSIGNED_SHORT, XdNames.XS_UNSIGNED_SHORT, SQ_MINMAX);
	/** Schema <code>unsignedInt</code> type. */
	public static final XsdBase UNSIGNED_INT = new XsdBase(Id.UNSIGNED_INT,
		XsdNames.UNSIGNED_INT, XdNames.XS_UNSIGNED_INT, SQ_MINMAX);
	/** Schema <code>unsignedLong</code> type. */
	public static final XsdBase UNSIGNED_LONG = new XsdBase(Id.UNSIGNED_LONG,
		XsdNames.UNSIGNED_LONG, XdNames.XS_UNSIGNED_LONG, SQ_MINMAX);
	/** Schema base type set.*/
	private static final Set<XsdBase> XSDBASES = new HashSet<XsdBase>();
	/** Schema base type id to instance map. */
	private static final Map<Integer, XsdBase> IDTOBASETYPES =
		new HashMap<Integer, XsdBase>();
	/** Schema base type name to instance map. */
	private static final Map<String, XsdBase> XSDNAMETOBASETYPES =
		new HashMap<String, XsdBase>();
	/** Schema base type XDefinition name to instance map. */
	private static final Map<String, XsdBase> XDNAMETOBASETYPE =
		new HashMap<String, XsdBase>();

	/** Gets base type instance with given schema base type name.
	 * @param xsdName schema base type name.
	 * @return base type instance or <code>null</code>.
	 */
	public static XsdBase getByXsdName(String xsdName) {
		return XSDNAMETOBASETYPES.get(xsdName);
	}

	/** Gets base type instance with given schema base type XDefinition name.
	 * @param xdefName schema base type XDefinition name.
	 * @return base type instance or <code>null</code>.
	 */
	public static XsdBase getByXdefName(String xdefName) {
		return XDNAMETOBASETYPE.get(xdefName);
	}

	static {
		XSDBASES.add(ANY_URI);
		XSDBASES.add(BASE64_BINARY);
		XSDBASES.add(BOOLEAN);
		XSDBASES.add(BYTE);
		XSDBASES.add(DATE);
		XSDBASES.add(DATE_TIME);
		XSDBASES.add(DECIMAL);
		XSDBASES.add(DOUBLE);
		XSDBASES.add(DURATION);
		XSDBASES.add(ENTITIES);
		XSDBASES.add(ENTITY);
		XSDBASES.add(FLOAT);
		XSDBASES.add(G_DAY);
		XSDBASES.add(G_MONTH);
		XSDBASES.add(G_MONTH_DAY);
		XSDBASES.add(G_YEAR);
		XSDBASES.add(G_YEAR_MONTH);
		XSDBASES.add(HEX_BINARY);
		XSDBASES.add(ID);
		XSDBASES.add(IDREF);
		XSDBASES.add(IDREFS);
		XSDBASES.add(INT);
		XSDBASES.add(INTEGER);
		XSDBASES.add(LANGUAGE);
		XSDBASES.add(LONG);
		XSDBASES.add(NAME);
		XSDBASES.add(NCNAME);
		XSDBASES.add(NEGATIVE_INTEGER);
		XSDBASES.add(NMTOKEN);
		XSDBASES.add(NMTOKENS);
		XSDBASES.add(NON_NEGATIVE_INTEGER);
		XSDBASES.add(NON_POSITIVE_INTEGER);
		XSDBASES.add(NORMALIZED_STRING);
		XSDBASES.add(NOTATION);
		XSDBASES.add(POSITIVE_INTEGER);
		XSDBASES.add(QNAME);
		XSDBASES.add(SHORT);
		XSDBASES.add(STRING);
		XSDBASES.add(TIME);
		XSDBASES.add(TOKEN);
		XSDBASES.add(UNSIGNED_BYTE);
		XSDBASES.add(UNSIGNED_INT);
		XSDBASES.add(UNSIGNED_LONG);
		XSDBASES.add(UNSIGNED_SHORT);

		Iterator<XsdBase> it = XSDBASES.iterator();
		while (it.hasNext()) {
			XsdBase type = it.next();
			Integer id = type.getId();
			String xsdName = type.getName();
			String xdefName = type.getXdefName();
			if (IDTOBASETYPES.containsKey(id)) {
				throw new IllegalArgumentException(
					"Given id '" + id + "' already exists!");
			}
//			if (_xsdNameToBaseType.containsKey(id)) {
//				throw new IllegalArgumentException(
//					"Given name '" + xsdName + "' already exists!");
//			}
			if (xdefName != null) {
				if (XDNAMETOBASETYPE.containsKey(xdefName)) {
					throw new IllegalArgumentException(
						"Given XDefinition name \""
						+ xdefName + "\" already exists!");
				}
				XDNAMETOBASETYPE.put(xdefName, type);
			}
			IDTOBASETYPES.put(id, type);
			XSDNAMETOBASETYPES.put(xsdName, type);
		}
	}
	/** Base type id. */
	private final int _id;
	/** Base type name. */
	private final String _xsdName;
	/** Type name in XDefinition. */
	private final String _xdefName;
/*VT*/
	/** Type name in XDefinition. */
	private final String[][] _sqParams;
/*VT*/

	/** Creates instance of schema base type.
	 * @param id base type id.
	 * @param xsdName base type name in schema.
	 * @param xdefName base type name in XDefinition.
	 * @param sqParams model of XDefinition sequential parameters.
	 */
	private XsdBase(int id,
		String xsdName,
		String xdefName,
		String[][] sqParams) {
		_id = id;
		_xsdName = xsdName;
		_xdefName = xdefName;
/*VT*/
		_sqParams = sqParams;
/*VT*/
	}

	/** Gets base type id.
	 * @return base type id.
	 */
	public int getId() {return _id;}

	/** Get base type schema name.
	 * @return base type schema name.
	 */
	public String getName() {return _xsdName;}

	/** Get base type XDefinition name.
	 * @return base type XDefinition name or <code>null</code>.
	 */
	public String getXdefName() {return _xdefName;}
/*VT*/
	/** Get sequential parameters.
	 * @return sequential parameters name template.
	 */
	public String[][] getSqParams() {return _sqParams;}
/*VT*/

	@Override
	/** Get king of schema.
	 * @return ValueType.
	 */
	public int getKind() {return ValueType.SCHEMA_BASE;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdBase)) {
			return false;
		}
		XsdBase t = (XsdBase) obj;
		return _xsdName.equals(t._xsdName);
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 11 * hash + _xsdName.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "SchemaBase[name='" + _xsdName + "']";
	}

	/** Contains XML Schema base type ids. */
	public static interface Id {

		/** Schema <code>anyURI</code> type id. */
		public static final int ANY_URI = 1;
		/** Schema <code>base64Binary</code> type id. */
		public static final int BASE64_BINARY = ANY_URI + 1;
		/** Schema <code>boolean</code> type id. */
		public static final int BOOLEAN = BASE64_BINARY + 1;
		/** Schema <code>byte</code> type id. */
		public static final int BYTE = BOOLEAN + 1;
		/** Schema <code>date</code> type id. */
		public static final int DATE = BYTE + 1;
		/** Schema <code>dateTime</code> type id. */
		public static final int DATE_TIME = DATE + 1;
		/** Schema <code>decimal</code> type id. */
		public static final int DECIMAL = DATE_TIME + 1;
		/** Schema <code>double</code> type id. */
		public static final int DOUBLE = DECIMAL + 1;
		/** Schema <code>duration</code> type id. */
		public static final int DURATION = DOUBLE + 1;
		/** Schema <code>ENTITY</code> type id. */
		public static final int ENTITY = DURATION + 1;
		/** Schema <code>ENTITIES</code> type id. */
		public static final int ENTITIES = ENTITY + 1;
		/** Schema <code>float</code> type id. */
		public static final int FLOAT = ENTITIES + 1;
		/** Schema <code>gDay</code> type id. */
		public static final int G_DAY = FLOAT + 1;
		/** Schema <code>gMonth</code> type id. */
		public static final int G_MONTH = G_DAY + 1;
		/** Schema <code>gMonthDay</code> type id. */
		public static final int G_MONTH_DAY = G_MONTH + 1;
		/** Schema <code>gYear</code> type id. */
		public static final int G_YEAR = G_MONTH_DAY + 1;
		/** Schema <code>gYearMonth</code> type id. */
		public static final int G_YEAR_MONTH = G_YEAR + 1;
		/** Schema <code>hexBinary</code> type id. */
		public static final int HEX_BINARY = G_YEAR_MONTH + 1;
		/** Schema <code>ID</code> type id. */
		public static final int ID = HEX_BINARY + 1;
		/** Schema <code>IDREF</code> type id. */
		public static final int IDREF = ID + 1;
		/** Schema <code>IDREFS</code> type id. */
		public static final int IDREFS = IDREF + 1;
		/** Schema <code>int</code> type id. */
		public static final int INT = IDREFS + 1;
		/** Schema <code>integer</code> type id. */
		public static final int INTEGER = INT + 1;
		/** Schema <code>language</code> type id. */
		public static final int LANGUAGE = INTEGER + 1;
		/** Schema <code>long</code> type id. */
		public static final int LONG = LANGUAGE + 1;
		/** Schema <code>Name</code> type id. */
		public static final int NAME = LONG + 1;
		/** Schema <code>NCName</code> type id. */
		public static final int NCNAME = NAME + 1;
		/** Schema <code>negativeInteger</code> type id. */
		public static final int NEGATIVE_INTEGER = NCNAME + 1;
		/** Schema <code>NMTOKEN</code> type id. */
		public static final int NMTOKEN = NEGATIVE_INTEGER + 1;
		/** Schema <code>NMTOKENS</code> type id. */
		public static final int NMTOKENS = NMTOKEN + 1;
		/** Schema <code>nonNegativeInteger</code> type id. */
		public static final int NON_NEGATIVE_INTEGER = NMTOKENS + 1;
		/** Schema <code>nonPositiveInteger</code> type id. */
		public static final int NON_POSITIVE_INTEGER = NON_NEGATIVE_INTEGER + 1;
		/** Schema <code>normalizedString</code> type id. */
		public static final int NORMALIZED_STRING = NON_POSITIVE_INTEGER + 1;
		/** Schema <code>NOTATION</code> type id. */
		public static final int NOTATION = NORMALIZED_STRING + 1;
		/** Schema <code>positiveInteger</code> type id. */
		public static final int POSITIVE_INTEGER = NOTATION + 1;
		/** Schema <code>QName</code> type id. */
		public static final int QNAME = POSITIVE_INTEGER + 1;
		/** Schema <code>short</code> type id. */
		public static final int SHORT = QNAME + 1;
		/** Schema <code>string</code> type id. */
		public static final int STRING = SHORT + 1;
		/** Schema <code>time</code> type id. */
		public static final int TIME = STRING + 1;
		/** Schema <code>token</code> type id. */
		public static final int TOKEN = TIME + 1;
		/** Schema <code>unsignedByte</code> type id. */
		public static final int UNSIGNED_BYTE = TOKEN + 1;
		/** Schema <code>unsignedShort</code> type id. */
		public static final int UNSIGNED_SHORT = UNSIGNED_BYTE + 1;
		/** Schema <code>unsignedInt</code> type id. */
		public static final int UNSIGNED_INT = UNSIGNED_SHORT + 1;
		/** Schema <code>unsignedLong</code> type id. */
		public static final int UNSIGNED_LONG = UNSIGNED_INT + 1;
	}
}