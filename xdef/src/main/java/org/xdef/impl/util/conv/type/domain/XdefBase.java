package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** X-definition base type.
 * @author Ilia Alexandrov
 */
public class XdefBase {

	/** <code>an</code> type.*/
	public static final XdefBase ALFA_NUMERIC =
		new XdefBase(Id.ALFA_NUMERIC, XdNames.ALFA_NUMERIC);
	/** <code>base64</code> type.*/
	public static final XdefBase BASE_64 =
		new XdefBase(Id.BASE_64, XdNames.BASE_64);
	/** <code>boolean</code> type.*/
	public static final XdefBase BOOLEAN =
		new XdefBase(Id.BOOLEAN, XdNames.BOOLEAN);
	//** <code>BNF</code> type.*/
	//public static final XdefBase BNF = new XdefBase(Id.BNF, XdNames.BNF);
	/** <code>contains</code> type.*/
	public static final XdefBase CONTAINS =
		new XdefBase(Id.CONTAINS, XdNames.CONTAINS);
	/** <code>containsi</code> type.*/
	public static final XdefBase CONTAINS_I =
		new XdefBase(Id.CONTAINS_I, XdNames.CONTAINS_I);
	/** <code>datetime</code> type.*/
	public static final XdefBase DATE_TIME =
		new XdefBase(Id.DATE_TIME, XdNames.DATE_TIME);
	/** <code>xdatetime</code> type.*/
	public static final XdefBase XDATE_TIME = /*VT*/
		new XdefBase(Id.XDATE_TIME, XdNames.XDATE_TIME);
	/** <code>dateYMDhms</code> type.*/
	public static final XdefBase DATE_YMDHMS =
		new XdefBase(Id.DATE_YMDHMS, XdNames.DATE_YMDHMS);
	/** <code>dec</code> type.*/
	public static final XdefBase DECIMAL =
		new XdefBase(Id.DECIMAL, XdNames.DECIMAL);
	/** <code>ENTITY</code> type.*/
	public static final XdefBase ENTITY =
		new XdefBase(Id.ENTITY, XdNames.ENTITY);
	/** <code>ENTITIES</code> type.*/
	public static final XdefBase ENTITIES =
		new XdefBase(Id.ENTITIES, XdNames.ENTITIES);
	/** <code>ID</code> type.*/
	public static final XdefBase ID =
		new XdefBase(Id.ID, XdNames.ID);
	/** <code>IDREF</code> type.*/
	public static final XdefBase IDREF =
		new XdefBase(Id.IDREF, XdNames.IDREF);
	/** <code>IDREFS</code> type.*/
	public static final XdefBase IDREFS =
		new XdefBase(Id.IDREFS, XdNames.IDREFS);
	/** <code>ISOdateTime</code> type.*/
	public static final XdefBase ISO_DATE_TIME =
		new XdefBase(Id.ISO_DATE_TIME, XdNames.ISO_DATE_TIME);
	/** <code>ISOdate</code> type.*/
	public static final XdefBase ISO_DATE =
		new XdefBase(Id.ISO_DATE, XdNames.ISO_DATE);
	/** <code>ISOtime</code> type.*/
	public static final XdefBase ISO_TIME =
		new XdefBase(Id.ISO_TIME, XdNames.ISO_TIME);
	/** <code>ISOday</code> type.*/
	public static final XdefBase ISO_DAY =
		new XdefBase(Id.ISO_DAY, XdNames.ISO_DAY);
	/** <code>ISOlanguage</code> type.*/
	public static final XdefBase ISO_LANGUAGE =
		new XdefBase(Id.ISO_LANGUAGE, XdNames.ISO_LANGUAGE);
	/** <code>ISOlanguages</code> type.*/
	public static final XdefBase ISO_LANGUAGES =
		new XdefBase(Id.ISO_LANGUAGES, XdNames.ISO_LANGUAGES);
	/** <code>ISOmonth</code> type.*/
	public static final XdefBase ISO_MONTH =
		new XdefBase(Id.ISO_MONTH, XdNames.ISO_MONTH);
	/** <code>ISOmonthDay</code> type.*/
	public static final XdefBase ISO_MONTH_DAY =
		new XdefBase(Id.ISO_MONTH_DAY, XdNames.ISO_MONTH_DAY);
	/** <code>ISOyear</code> type.*/
	public static final XdefBase ISO_YEAR =
		new XdefBase(Id.ISO_YEAR, XdNames.ISO_YEAR);
	/** <code>ISOyearMonth</code> type.*/
	public static final XdefBase ISO_YEAR_MONTH =
		new XdefBase(Id.ISO_YEAR_MONTH, XdNames.ISO_YEAR_MONTH);
	/** <code>ISOduration</code> type.*/
	public static final XdefBase ISO_DURATION =
		new XdefBase(Id.ISO_DURATION, XdNames.ISO_DURATION);
	/** <code>email</code> type.*/
	public static final XdefBase EMAIL =
		new XdefBase(Id.EMAIL, XdNames.EMAIL);
	/** <code>emailList</code> type.*/
	public static final XdefBase EMAIL_LIST =
		new XdefBase(Id.EMAIL_LIST, XdNames.EMAIL_LIST);
	/** <code>emailDate</code> type.*/
	public static final XdefBase EMAIL_DATE =
		new XdefBase(Id.EMAIL_DATE, XdNames.EMAIL_DATE);
	/** <code>ends</code> type.*/
	public static final XdefBase ENDS = new XdefBase(Id.ENDS, XdNames.ENDS);
	/** <code>endsi</code> type.*/
	public static final XdefBase ENDS_I =
		new XdefBase(Id.ENDS_I, XdNames.ENDS_I);
	/** <code>equals</code> type.*/
	public static final XdefBase EQUALS =
		new XdefBase(Id.EQUALS, XdNames.EQUALS);
	/** <code>equalsi</code> type.*/
	public static final XdefBase EQUALS_I =
		new XdefBase(Id.EQUALS_I, XdNames.EQUALS_I);
	/** <code>file</code> type.*/
	public static final XdefBase FILE =
		new XdefBase(Id.FILE, XdNames.FILE);
	/** <code>float</code> type.*/
	public static final XdefBase FLOAT =
		new XdefBase(Id.FLOAT, XdNames.FLOAT);
	/** <code>hex</code> type.*/
	public static final XdefBase HEX =
		new XdefBase(Id.HEX, XdNames.HEX);
	/** <code>int</code> type.*/
	public static final XdefBase INT =
		new XdefBase(Id.INT, XdNames.INT);
	/** <code>list</code> type.*/
	public static final XdefBase LIST =
		new XdefBase(Id.LIST, XdNames.ENUM);
	/** <code>listi</code> type.*/
	public static final XdefBase LIST_I =
		new XdefBase(Id.LIST_I, XdNames.ENUM_I);
	/** <code>tokens</code> type.*/
	public static final XdefBase TOKENS =
		new XdefBase(Id.TOKENS, XdNames.TOKENS);
	/** <code>tokensi</code> type.*/
	public static final XdefBase TOKENS_I =
		new XdefBase(Id.TOKENS_I, XdNames.TOKENS_I);
	/** <code>MD5</code> type.*/
	public static final XdefBase MD5 =
		new XdefBase(Id.MD5, XdNames.MD5);
	/** <code>NCName</code> type.*/
	public static final XdefBase NC_NAME =
		new XdefBase(Id.NC_NAME, XdNames.NC_NAME);
	/** <code>NCNameList</code> type.*/
	public static final XdefBase NC_NAME_LIST =
		new XdefBase(Id.NC_NAME_LIST, XdNames.NC_NAME_LIST);
	/** <code>NMTOKEN</code> type.*/
	public static final XdefBase NM_TOKEN =
		new XdefBase(Id.NM_TOKEN, XdNames.NM_TOKEN);
	/** <code>NMTOKENS</code> type.*/
	public static final XdefBase NM_TOKENS =
		new XdefBase(Id.NM_TOKENS, XdNames.NM_TOKENS);
	/** <code>NOTATION</code> type.*/
	public static final XdefBase NOTATION =
		new XdefBase(Id.NOTATION, XdNames.NOTATION);
	/** <code>normString</code> type.*/
	public static final XdefBase NORM_STRING =
		new XdefBase(Id.NORM_STRING, XdNames.NORM_STRING);
	/** <code>normToken</code> type.*/
	public static final XdefBase NORM_TOKEN
		= new XdefBase(Id.NORM_TOKEN, XdNames.NORM_TOKEN);
	/** <code>normTokens</code> type.*/
	public static final XdefBase NORM_TOKENS =
		new XdefBase(Id.NORM_TOKENS, XdNames.NORM_TOKENS);
	/** <code>num</code> type.*/
	public static final XdefBase NUMBER =
		new XdefBase(Id.NUMBER, XdNames.NUMBER);
	/** <code>pic</code> type.*/
	public static final XdefBase PICTURE =
		new XdefBase(Id.PICTURE, XdNames.PICTURE);
	/** <code>QName</code> type.*/
	public static final XdefBase Q_NAME =
		new XdefBase(Id.Q_NAME, XdNames.Q_NAME);
	/** <code>QnameURI</code> type.*/
	public static final XdefBase Q_NAME_URI =
		new XdefBase(Id.Q_NAME_URI, XdNames.Q_NAME_URI);
	/** <code>QnameList</code> type.*/
	public static final XdefBase Q_NAME_LIST =
		new XdefBase(Id.Q_NAME_LIST, XdNames.Q_NAME_LIST);
	/** <code>QnameListURI</code> type.*/
	public static final XdefBase Q_NAME_LIST_URI =
		new XdefBase(Id.Q_NAME_LIST_URI, XdNames.Q_NAME_LIST_URI);
	/** <code>regex</code> type.*/
	public static final XdefBase REGEX =
		new XdefBase(Id.REGEX, XdNames.REGEX);
	/** <code>statrs</code> type.*/
	public static final XdefBase STARTS =
		new XdefBase(Id.STARTS, XdNames.STARTS);
	/** <code>startsi</code> type.*/
	public static final XdefBase STARTS_I =
		new XdefBase(Id.STARTS_I, XdNames.STARTS_I);
	/** <code>string</code> type.*/
	public static final XdefBase STRING =
		new XdefBase(Id.STRING, XdNames.STRING);
	/** <code>uri</code> type.*/
	public static final XdefBase URI =
		new XdefBase(Id.URI, XdNames.URI);
	/** <code>uriList</code> type.*/
	public static final XdefBase URI_LIST =
		new XdefBase(Id.URI_LIST, XdNames.URI_LIST);
	/** <code>url</code> type.*/
	public static final XdefBase URL =
		new XdefBase(Id.URL, XdNames.URL);
	/** <code>urlList</code> type.*/
	public static final XdefBase URL_LIST =
		new XdefBase(Id.URL_LIST, XdNames.URL_LIST);

	/** base type set.*/
	private static final Set<XdefBase> XDEFBASES = new HashSet<XdefBase>();
	/** base type id to instance map.*/
	private static final Map<Integer, XdefBase> IDTOBASE =
		new HashMap<Integer, XdefBase>();
	/** base type name to instance map.*/
	private static final Map<String, XdefBase> NAMETOBASE =
		new HashMap<String, XdefBase>();

	/** Gets instance of X-definition base type with given name.
	 * @param name X-definition type name.
	 * @return instance of X-definition base type or <code>null</code>.
	 */
	public static XdefBase get(String name) {return NAMETOBASE.get(name);}

	static {
		XDEFBASES.add(ALFA_NUMERIC);
		XDEFBASES.add(BASE_64);
		XDEFBASES.add(BOOLEAN);
		XDEFBASES.add(CONTAINS);
		XDEFBASES.add(CONTAINS_I);
		XDEFBASES.add(DATE_TIME);
		XDEFBASES.add(XDATE_TIME); /*VT*/
		XDEFBASES.add(DATE_YMDHMS);
		XDEFBASES.add(DECIMAL);
		XDEFBASES.add(EMAIL);
		XDEFBASES.add(EMAIL_DATE);
		XDEFBASES.add(EMAIL_LIST);
		XDEFBASES.add(ENDS);
		XDEFBASES.add(ENDS_I);
		XDEFBASES.add(ENTITIES);
		XDEFBASES.add(ENTITY);
		XDEFBASES.add(EQUALS);
		XDEFBASES.add(EQUALS_I);
		XDEFBASES.add(FILE);
		XDEFBASES.add(FLOAT);
		XDEFBASES.add(HEX);
		XDEFBASES.add(ID);
		XDEFBASES.add(IDREF);
		XDEFBASES.add(IDREFS);
		XDEFBASES.add(INT);
		XDEFBASES.add(ISO_DATE);
		XDEFBASES.add(ISO_DATE_TIME);
		XDEFBASES.add(ISO_DAY);
		XDEFBASES.add(ISO_DURATION);
		XDEFBASES.add(ISO_LANGUAGE);
		XDEFBASES.add(ISO_LANGUAGES);
		XDEFBASES.add(ISO_MONTH);
		XDEFBASES.add(ISO_MONTH_DAY);
		XDEFBASES.add(ISO_TIME);
		XDEFBASES.add(ISO_YEAR);
		XDEFBASES.add(ISO_YEAR_MONTH);
		XDEFBASES.add(LIST);
		XDEFBASES.add(LIST_I);
		XDEFBASES.add(MD5);
		XDEFBASES.add(NC_NAME);
		XDEFBASES.add(NC_NAME_LIST);
		XDEFBASES.add(NM_TOKEN);
		XDEFBASES.add(NM_TOKENS);
		XDEFBASES.add(NORM_STRING);
		XDEFBASES.add(NORM_TOKEN);
		XDEFBASES.add(NORM_TOKENS);
		XDEFBASES.add(NOTATION);
		XDEFBASES.add(NUMBER);
		XDEFBASES.add(PICTURE);
		XDEFBASES.add(Q_NAME);
		XDEFBASES.add(Q_NAME_LIST);
		XDEFBASES.add(Q_NAME_LIST_URI);
		XDEFBASES.add(Q_NAME_URI);
		XDEFBASES.add(REGEX);
		XDEFBASES.add(STARTS);
		XDEFBASES.add(STARTS_I);
		XDEFBASES.add(STRING);
		XDEFBASES.add(TOKENS);
		XDEFBASES.add(TOKENS_I);
		XDEFBASES.add(URI);
		XDEFBASES.add(URI_LIST);
		XDEFBASES.add(URL);
		XDEFBASES.add(URL_LIST);

		Iterator<XdefBase> it = XDEFBASES.iterator();
		while (it.hasNext()) {
			XdefBase xb = it.next();
			Integer id = xb.getId();
			String name = xb.getName();
			if (IDTOBASE.containsKey(id)) {
				throw new IllegalArgumentException("Given id already exists!");
			}
			if (NAMETOBASE.containsKey(name)) {
				throw new IllegalArgumentException(
					"Given name already exists!");
			}
			IDTOBASE.put(id, xb);
			NAMETOBASE.put(name, xb);
		}
	}

	/** base type id. */
	private final int _id;
	/** base type name. */
	private final String _name;

	/** Creates instance of X-definition base type.
	 * @param id X-definition base type id.
	 * @param name X-definition base type name.
	 * @throws IllegalArgumentException if given type name is <code>null</code> or empty.
	 */
	private XdefBase(int id, String name) {
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Given name is empty!");
		}
		_id = id;
		_name = name;
	}

	/** Gets base type id.
	 * @return base type id.
	 */
	public int getId() {return _id;}

	/** Gets base type name.
	 * @return base type name.
	 */
	public String getName() {return _name;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdefBase)) {
			return false;
		}
		XdefBase x = (XdefBase) obj;
		return _id == x._id;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + this._id;
		return hash;
	}

	@Override
	public String toString() {return "XdefBase[name='" + _name + "']";}

	/** type id numbers. */
	public static interface Id {
		/** <code>an</code> type id.*/
		public static final int ALFA_NUMERIC = 1;
		/** <code>base64</code> type id.*/
		public static final int BASE_64 = ALFA_NUMERIC + 1;
		/** <code>boolean</code> type id.*/
		public static final int BOOLEAN = BASE_64 + 1;
		/** <code>BNF</code> type id.*/
		//public static final int BNF = BOOLEAN + 1;
		/** <code>contains</code> type id.*/
		public static final int CONTAINS = BOOLEAN + 1;
		/** <code>containsi</code> type id.*/
		public static final int CONTAINS_I = CONTAINS + 1;
		/** <code>datetime</code> type id.*/
		public static final int DATE_TIME = CONTAINS_I + 1;
		/** <code>datetime</code> type id.*/
		public static final int XDATE_TIME = DATE_TIME + 1;
		/** <code>dateYMDhms</code> type id.*/
		public static final int DATE_YMDHMS = XDATE_TIME + 1;
		/** <code>dec</code> type id.*/
		public static final int DECIMAL = DATE_YMDHMS + 1;
		/** <code>ENTITY</code> type id.*/
		public static final int ENTITY = DECIMAL + 1;
		/** <code>ENTITIES</code> type id.*/
		public static final int ENTITIES = ENTITY + 1;
		/** <code>ID</code> type id.*/
		public static final int ID = ENTITIES + 1;
		/** <code>IDREF</code> type id.*/
		public static final int IDREF = ID + 1;
		/** <code>IDREFS</code> type id.*/
		public static final int IDREFS = IDREF + 1;
		/** <code>ISOdateTime</code> type id.*/
		public static final int ISO_DATE_TIME = IDREFS + 1;
		/** <code>ISOdate</code> type id.*/
		public static final int ISO_DATE = ISO_DATE_TIME + 1;
		/** <code>ISOtime</code> type id.*/
		public static final int ISO_TIME = ISO_DATE + 1;
		/** <code>ISOday</code> type id.*/
		public static final int ISO_DAY = ISO_TIME + 1;
		/** <code>ISOlanguage</code> type id.*/
		public static final int ISO_LANGUAGE = ISO_DAY + 1;
		/** <code>ISOlanguages</code> type id.*/
		public static final int ISO_LANGUAGES = ISO_LANGUAGE + 1;
		/** <code>ISOmonth</code> type id.*/
		public static final int ISO_MONTH = ISO_LANGUAGES + 1;
		/** <code>ISOmonthDay</code> type id.*/
		public static final int ISO_MONTH_DAY = ISO_MONTH + 1;
		/** <code>ISOyear</code> type id.*/
		public static final int ISO_YEAR = ISO_MONTH_DAY + 1;
		/** <code>ISOyearMonth</code> type id.*/
		public static final int ISO_YEAR_MONTH = ISO_YEAR + 1;
		/** <code>ISOduration</code> type id.*/
		public static final int ISO_DURATION = ISO_YEAR_MONTH + 1;
		/** <code>email</code> type id.*/
		public static final int EMAIL = ISO_DURATION + 1;
		/** <code>emailList</code> type id.*/
		public static final int EMAIL_LIST = EMAIL + 1;
		/** <code>emailDate</code> type id.*/
		public static final int EMAIL_DATE = EMAIL_LIST + 1;
		/** <code>ends</code> type id.*/
		public static final int ENDS = EMAIL_DATE + 1;
		/** <code>endsi</code> type id.*/
		public static final int ENDS_I = ENDS + 1;
		/** <code>equals</code> type id.*/
		public static final int EQUALS = ENDS_I + 1;
		/** <code>equalsi</code> type id.*/
		public static final int EQUALS_I = EQUALS + 1;
		/** <code>file</code> type id.*/
		public static final int FILE = EQUALS_I + 1;
		/** <code>float</code> type id.*/
		public static final int FLOAT = FILE + 1;
		/** <code>hex</code> type id.*/
		public static final int HEX = FLOAT + 1;
		/** <code>int</code> type id.*/
		public static final int INT = HEX + 1;
		/** <code>list</code> type id.*/
		public static final int LIST = INT + 1;
		/** <code>listi</code> type id.*/
		public static final int LIST_I = LIST + 1;
		/** <code>tokens</code> type id.*/
		public static final int TOKENS = LIST_I + 1;
		/** <code>tokensi</code> type id.*/
		public static final int TOKENS_I = TOKENS + 1;
		/** <code>MD5</code> type id.*/
		public static final int MD5 = TOKENS_I + 1;
		/** <code>NCName</code> type id.*/
		public static final int NC_NAME = MD5 + 1;
		/** <code>NCnameList</code> type id.*/
		public static final int NC_NAME_LIST = NC_NAME + 1;
		/** <code>NMTOKEN</code> type id.*/
		public static final int NM_TOKEN = NC_NAME_LIST + 1;
		/** <code>NMTOKENS</code> type id.*/
		public static final int NM_TOKENS = NM_TOKEN + 1;
		/** <code>NOTATION</code> type id.*/
		public static final int NOTATION = NM_TOKENS + 1;
		/** <code>normString</code> type id.*/
		public static final int NORM_STRING = NOTATION + 1;
		/** <code>normToken</code> type id.*/
		public static final int NORM_TOKEN = NORM_STRING + 1;
		/** <code>normTokens</code> type id.*/
		public static final int NORM_TOKENS = NORM_TOKEN + 1;
		/** <code>num</code> type id.*/
		public static final int NUMBER = NORM_TOKENS + 1;
		/** <code>pic</code> type id.*/
		public static final int PICTURE = NUMBER + 1;
		/** <code>Qname</code> type id.*/
		public static final int Q_NAME = PICTURE + 1;
		/** <code>QnameURI</code> type id.*/
		public static final int Q_NAME_URI = Q_NAME + 1;
		/** <code>QnameList</code> type id.*/
		public static final int Q_NAME_LIST = Q_NAME_URI + 1;
		/** <code>QnameListURI</code> type id.*/
		public static final int Q_NAME_LIST_URI = Q_NAME_LIST + 1;
		/** <code>regex</code> type id.*/
		public static final int REGEX = Q_NAME_LIST_URI + 1;
		/** <code>statrs</code> type id.*/
		public static final int STARTS = REGEX + 1;
		/** <code>startsi</code> type id.*/
		public static final int STARTS_I = STARTS + 1;
		/** <code>string</code> type id.*/
		public static final int STRING = STARTS_I + 1;
		/** <code>uri</code> type id.*/
		public static final int URI = STRING + 1;
		/** <code>uriList</code> type id.*/
		public static final int URI_LIST = URI + 1;
		/** <code>url</code> type id.*/
		public static final int URL = URI_LIST + 1;
		/** <code>urlList</code> type id.*/
		public static final int URL_LIST = URL + 1;
	}
}