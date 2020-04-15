package org.xdef.sys;

import org.xdef.XDConstants;
import org.xdef.msg.SYS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Generates Java class source with registered report tables.
 * @author Vaclav Trojan
 */
public class RegisterReportTables {

	/** Platform-dependent new line. */
	private static final String LN = XDConstants.LINE_SEPARATOR;

	/** Implementation of storage of report tables. A report table can be
	 * localized for language specified by given language argument or the
	 * language from system property <i><tt>user.language</tt></i>.
	 * The language argument must be valid ISO-639 Language Code (see
	 * {@link java.util.Locale#getISO3Language()}).
	 * These codes are lower-case, three-letter codes as defined by ISO-639-2.
	 * You can find a full list of these codes at a number of sites, such as:
	 * <p><a href ="http://www.loc.gov/standards/iso639-2">
	 * <tt>http://www.loc.gov/standards/iso639-2</tt></a>
	 * </p>
	 * <p>The source report table is the properties file with following
	 * obligatory properties:</p>
	 * <pre><code>
	 * _prefix = the prefis of message names in the table
	 * _language = the 3 letters ISO-639 Language Code
	 * _defaultLanguag = the ISO-639 Language Code (must be specified only once)
	 * </code></pre>
	 * <p> All other property names must start with the prefix. </p>
	 * <p> Reports are divided to separate files for each language. Each report
	 * has a id. The id is composed of prefix and of a local ID (usually
	 * a number). The prefix must be a sequence of minimum 3 capital ASCII
	 * letters. Messages for the specific language and prefix must be stored to
	 * the separate file.
	 * The report files must be available in the package
	 * <tt>org.xdef.msg</tt>.</p>
	 * <p>The file name must be composed by following way:</p>
	 * <tt>prefix_CCC.properties</tt>
	 * <p>where prefix must be followed by the character
	 * '_' by three letters representing ISO-639-2 language code corresponding
	 * to given national language. See examples of the English and German
	 * version report files. CCC is the language code</p>
	 * <p>Example of English (MYAPP_eng.properties):</p>
	 * <pre><code>
	 * # Prefix of messages.
	 * _prefix=MYAPP
	 * # ISO name of language.
	 * _language=eng
	 * #
	 * MYAPP001=Message: &{0}{: }
	 * MYAPP002=Given name: &{0} Family name: &{1}
	 * MYAPP003=Mrs. &(#MYAPP002)
	 * MYAPP003=Mr. &(#MYAPP002)
	 * </code>
	 * <p>German MYAPP_deu.properties:</p>
	 * <pre><code>
	 * _prefix=MYAPP
	 * _language=deu
	 * #
	 * MYAPP001=Nachricht &{0}
	 * MYAPP002=Zuname: &{0} Vorname: &{1}
	 * MYAPP003=Frau &(#MYAPP002)
	 * MYAPP004=Herr &(#MYAPP002)
	 * </code></pre>
	 * <p>The report is described by an element which tag name is equal to the
	 * report id. The text of report is represented by value of the attribute
	 * which is equal to the language id. The text of report can be modified by
	 * parameters. The parameter is specified by "&amp;{name_of_parameter}". If
	 * name* of parameter starts with "#" then following part of the name is
	 * used as reference identifier to other report.</p>
	 * <p>Example:</p>
	 * <p>The report tables are searched by report manager in the package
	 * <tt>org.xdef.msg</tt>. For each prefix should exist a class
	 * with the name equal to the report prefix. This class may be empty, it
	 * just helps to find report files by class loader.</p>
	 * <p><b>example:</b></p>
	 * <pre><code>
	 * package org.xdef.msg;
	 * public final class MYAPP {}
	 * </code></pre>
	 * The report can be created by the following command:
	 * <pre><code>
	 *   Report rep = Report.error(
	 *		"MYAPP003", null, "{#MYAPP005}&amp;{g}&amp;{g}John&amp;{g}Brown");
	 *   System.out.println(report.toString());//print report in system language
	 *   System.out.println(report.toString("deu")); //print the german version
	 *   //if the file MYAPP_rus.xml is not available then the english version
	 *   // will be printed
	 *   System.out.println(report.toString("rus")); //print the russian ersion
	 * </code></pre>
	 * If no report or no report file is found then it is used the default text
	 * from the report.Therefore it is highly recommended to specify the default
	 * English text directly to the source code:
	 * <pre><code>
	 *   Report rep = Report.error("MYAPP001",
	 *		Given name: &amp;{g} Family name: &amp;{f}, //the english is default
	 *		"&amp;{g}John&amp;{g}Brown");
	 * </code></pre>
	 */
	private final static class ReportTableImpl extends ReportTable {
		private final Properties _msgs; // Properties with messages

		/** Create new instance of ReportTable.
		 * @param prefix prefix of ReportTable.
		 * @param language language of this table (code ISO-639 or ISO-639-2).
		 * @param defaultLanguage default language (code ISO-639 or ISO-639-2).
		 * @param msgs array of report items.
		 */
		private ReportTableImpl(final String prefix,
			final String language,
			final String defaultLanguage,
			final Properties msgs) throws RuntimeException {
			super(prefix, language, defaultLanguage);
			if (msgs == null || msgs.isEmpty()) {
				//In the report table &{0} are no reports
				throw new SRuntimeException(SYS.SYS203, getTableName());
			}
			_msgs = msgs;
		}

		@Override
		public String getReportText(final String reportID) {
			return _msgs.getProperty(reportID);
		}

		@Override
		/** Get report text (with resolved references).
		 * If the identifier doesn't exist the returned value is <i>null</i>.
		 * @param registeredID report registered ID.
		 * @return The text of report or null.
		 */
		public String getReportText(final long registeredID) {
			int index = getRegisteredReportId(registeredID);
			return getReportText(getPrefix() + _ids[index]);
		}

		@Override
		/** Get reportID from registered report ID.
		 * @param registeredID registered report ID.
		 * @return string with report ID or <tt>null</tt>.
		 */
		public String getReportID(final long registeredID) {
			int index = getRegisteredReportId(registeredID);
			if (_ids == null) return null;
			return getPrefix() + _ids[index];
		}

		@Override
		public boolean isRegistered() {return false;}

	}

	/** Abstract class of report table. The report table contains text models of
	 * reports associated to a prefix. The prefix is sequence of capital letters
	 * ASCII letters. Minimum length of prefix is 2 letters, maximum 10 letters.
	 * Each report in the table has an identifier starting with a small ASCII
	 * letter or a digit or character '_'. After this character may follow
	 * sequence of small letters, capital letters, digits or '_'. Let's have
	 * prefix "XYZ".
	 * Report identifiers may be: "XYZ0019", "XYZ_NAME", "XYZ0019_1", ...
	 * Report table may be constructed from external data (see
	 * {@link org.xdef.sys.RegisterReportTables}) or it can be created
	 * from the class of registered report table.
	 */
	abstract static class ReportTable implements Comparable<ReportTable> {
		/** Minimal length of prefix of registered report. */
		private static final int PREFIX_MINLENGTH = 2;
		/** Maximal length of prefix of registered report. */
		private static final int PREFIX_MAXLENGTH = 11;
		/** Bit mask for extracting if the index part of registered report ID.*/
		static final int IDMASK = 0xffff;
		/** Bit mask for extracting registered report ID for all languages. */
		static final long REGTABIDMASK = 0xffffffffffff0000L;
		/** Bit size of the index part of registered report ID. */
		static final int IDBITS = 16;
		/** Sorted array of message ids without prefix. */
		String[] _ids;
		/** Prefix of message identifiers. */
		private final String _prefix;
		/** Language ID of this table. */
		private final String _language;
		/** Available languages. */
		private String[] _languages;
		/** Default language. */
		private String _defaultLanguage;
		/** Identifier of this table. */
		private final long _tableID;
		/** Full name of this table. */
		private final String _tableName;

		/** Check if this table is registered.
		 * @return <tt>true</tt> if and only if the table is registered.
		 */
		abstract public boolean isRegistered();

		/** Get report text (with resolved references).
		 * If the identifier doesn't exist the returned value is <i>null</i>.
		 * @param reportID report ID.
		 * @return The text of report or null.
		 */
		abstract public String getReportText(final String reportID);

		/** Get report text (with resolved references).
		 * If the identifier doesn't exist the returned value is <i>null</i>.
		 * @param registeredID report registered ID.
		 * @return The text of report or null.
		 */
		abstract public String getReportText(final long registeredID);

		/** Get string of reportID from registered report ID.
		 * @param registeredID registered report ID.
		 * @return string created from registered report ID or <tt>null</tt>.
		 */
		abstract public String getReportID(final long registeredID);

		public ReportTable(final Class<?> baseClass,
			final Class<?> localizedClass) {
			String className = localizedClass.getName();
			int i = className.lastIndexOf('_');
			if (i < 0) {
				//Incorrect class of registered report table &{0}
				throw new SRuntimeException(SYS.SYS210, className);
			}
			int j = className.lastIndexOf('.');
			_prefix = className.substring(j + 1, i).intern();
			_language = className.substring(i + 1).intern();
			_tableName = (_prefix + "_" + _language).intern();
			String s;
			try {
				s = (String) baseClass.getDeclaredField(
					_prefix + "_DEFAULT_LANGUAGE").get(null);
			} catch (Exception ex) {
				s = "eng";
			}
			_defaultLanguage = s.intern();
			_tableID = RegisterReportTables.getTableID(_tableName);
			try {
				_languages = (String[]) baseClass.getDeclaredField(
					_prefix + "_LANGUAGES").get(null);
			} catch (Exception ex) {
				if (_defaultLanguage.equals(_language)) {
					_languages = new String[] {_language};
				} else {
					_languages =  new String[] {_language, _defaultLanguage};
					Arrays.sort(_languages);
				}
			}
		}

		/** Create new instance of ReportTable.
		 * @param prefix prefix of ReportTable.
		 * @param language language of this table (code ISO-639 or ISO-639-2).
		 * @param defaultLanguage default language (code ISO-639 or ISO-639-2).
		 */
		public ReportTable(final String prefix,
			final String language,
			final String defaultLanguage) {
			String lang = getISO3Language(language);//three letters
				_tableName = prefix + '_' + lang;
				long regId;
				if ((regId = prefix.charAt(0) - '@') <= 0 || regId >= 27) {
					//In the report table &{0} are no reports
					throw new SRuntimeException(SYS.SYS203, _tableName);
				}
				_prefix = prefix;
				_tableID = getTableID(prefix, lang);
				_language = lang;
				_defaultLanguage = defaultLanguage;
				_languages = new String[] {lang};
		}

		/** Get language of this table.
		 * @return language of this table.
		 */
		public final String getLanguage() {return _language;}

		/** Get language codes of all supported languages (three letters
		 * ISO-639-2 Language Codes).
		 * @return array with language codes if table is registered or the empty
		 * array.
		 */
		public final String[] getLanguages() {return _languages;}

		/** Add language code to list of supported languages.
		 * @param language language code to be added (three letters ISO-639-2
		 * Language Code).
		 */
		public final void addLanguage(final String language) {
			String lang = getISO3Language(language);//three letters
			for (String x: _languages) {
				if (x.equals(lang)) {
					return;
				}
			}
			String[] langs = _languages;
			_languages = new String[langs.length + 1];
			System.arraycopy(langs, 0, _languages, 0,  langs.length);
			_languages[langs.length] = lang;
			Arrays.sort(_languages);
		}

		public final String getDefaultLanguage() {return _defaultLanguage;}

		public final void setDefaultLanguage(final String s) {
			_defaultLanguage = (s != null) ? getISO3Language(s) : null;
		}

		public final String getPrefix() {return _prefix;}

		/** Get table name.
		 * @return string table name.
		 */
		public final String getTableName() {return _tableName;}

		/** Get id of registered table including.
		 * @return id of registered table.
		 */
		public final long getRegisteredTableID() {return _tableID;}

		/** Get sorted array of all parameter names of report.
		 * @param reportID The report id.
		 * @return The array of parameter names.
		 */
		public final String[] getReportParamNames(final String reportID) {
			return getParams(getReportText(reportID));
		}

		/** Get sorted array of all parameter names of report.
		 * @param registeredID report registered ID.
		 * @return The array of parameter names.
		 */
		public final String[] getReportParamNames(final long registeredID) {
			return getParams(getReportText(registeredID));
		}

		@Override
		public String toString() {return "ReportTable: " + _tableName;}

		@Override
		public final int hashCode() {
			return (int) (_tableID >>> 32) ^ (int) _tableID;
		}

		@Override
		public final boolean equals(final Object o) {
			return (o instanceof ReportTable)
				&& _tableID == ((ReportTable) o).getRegisteredTableID();
		}

		@Override
		public final int compareTo(final ReportTable table) {
			long id = table.getRegisteredTableID();
			return _tableID < id ? -1 : _tableID > id ? 1 : 0;
		}

	////////////////////////////////////////////////////////////////////////////
		/** Get ISO 639-2 3 letters language ID.
		 * @param language The language code (ISO 639 2 letters) or
		 * (ISO 639-2 3 letters).
		 * @return the ISO 639-2 language ID (three letters).
		 * @throws RuntimeException if language code is not found.
		 */
		static final String getISO3Language(final String language)
			throws RuntimeException {
			String result;
			if (language.length() == 3) {
				for (Locale x: Locale.getAvailableLocales()) {
					if (language.equals(x.getISO3Language())) {
						result = x.getISO3Language();
						return result;
					}
				}
			} else {
				try {
					result= new Locale(
						language.toLowerCase(),"").getISO3Language();
					if (result != null && result.length() == 3) {
						return result;
					}
				} catch (Exception ex) {}
			}
			//Unsupported language code: &{0}
			throw new SRuntimeException(SYS.SYS018, language);
		}

		/** Get sorted array of parameters from the report text.
		 * @param text report text
		 * @return sorted array of parameters from the report text. Returns
		 * empty array if no parameters are present or  <tt>null</tt> if
		 * an error occurs.
		 */
		static final String[] getParams(final String text) {
			int pos;
			if (text == null || (pos = text.indexOf("&{")) < 0) {
				return new String[0]; //no params
			}
			Set<String> hs = new TreeSet<String>();
			while (pos >= 0) {
				int pos1;
				if ((pos1 = text.indexOf('}',(pos +=2))) <= 0) {
					return null; //error
				}
				hs.add(text.substring(pos,pos1));
				pos = text.indexOf("&{", pos1 + 1);
			}
			String[] result = new String[hs.size()];
			hs.toArray(result);
			Arrays.sort(result);
			return result;
		}

		/** Compute registered localized table ID.
		 * @param prefix string with the prefix of table.
		 * @param language string with language ISO-639 ID.
		 * @return registered localized table ID.
		 */
		static final long getTableID(final String prefix,
			final String language) {
			return getPrefixID(prefix) | getLanguageID(language);
		}

		/** Compute registered prefix ID.
		 * @param prefix string with the prefix of table.
		 * @return registered table ID.
		 */
		static final long getPrefixID(final String prefix) {
			long regId = prefix.charAt(0) - '@';
			int len = prefix.length();
			for (int i = 1; i < len; i++) {
				int j = prefix.charAt(i) - '@';
				regId = regId*27 + j;
			}
			return (regId << IDBITS);
		}

		/** Compute registered language hash ID.
		 * @param language string with language ISO-639 ID.
		 * @return registered language ID.
		 */
		static final int getLanguageID(final String language) {
			String s = language == null ? "eng" : getISO3Language(language);
			int lngid = (s.charAt(0) - (char)('a' - 1));
			lngid = lngid*27 + s.charAt(1) - (char) ('a' - 1);
			return lngid*27 + s.charAt(2) - (char) ('a' - 1);
		}

		/** Get prefix from report ID or report table name.
		 * @param reportID string with report ID.
		 * @return prefix extracted from report ID or <tt>null</tt>
		 */
		protected static final String getPrefixFromID(final String reportID) {
			for (int i = 0; i < reportID.length(); i++) {
				char c;
				if ((c = reportID.charAt(i)) < 'A' || c > 'Z') {
					return i < PREFIX_MINLENGTH || i > PREFIX_MAXLENGTH
						? null : reportID.substring(0, i);
				}
			}
			return null;
		}

		/** Read properties from file.
		 * @param fileName pathname to file with properties/
		 * @return Properties object.
		 */
		final static Properties readProperties(final String fileName) {
			FileInputStream in = null;
			Properties props = null;
			try {
				in = new FileInputStream(fileName);
				props = readProperties(in);
			} catch (IOException ex) {
				//Can't read properties with reports: &{0}
				throw new SRuntimeException(SYS.SYS226, fileName);
			}
			try {in.close();} catch (Exception ex) {}
			return props;
		}

		/** Read properties from file.
		 * @param inStream input stream with properties.
		 * @return Properties object.
		 */
		final static Properties readProperties(final InputStream inStream) {
			try {
				InputStreamReader in = new InputStreamReader(inStream,
					Charset.forName("UTF-8"));
				try {
					Properties props = new Properties();
					props.load(in);
					return props;
				} finally {
					in.close();
				}
			} catch (Exception ex) {
				//Can't read properties with reports: &{0}
				throw new SRuntimeException(SYS.SYS226, ex.getMessage());
			}
		}

		/** Get registered report ID.
		 * @param table Report table.
		 * @param index message index.
		 * @return registered report ID.
		 */
		final static long getRegisteredReportId(final ReportTable table,
			int index) {
			return table.getRegisteredTableID()
				& ReportTable.REGTABIDMASK | index;
		}

		/** Get index from registered report ID.
		 * @return registered report ID.
		 */
		final static int getRegisteredReportId(final long registeredID) {
			return (int) registeredID & IDMASK;
		}
	}

	/** Prevent creation of instance of this object. */
	private RegisterReportTables() {}

	/** Generate Java source with the interface of registered IDs.
	 * @param table report table.
	 * @param dir directory where java source is stored.
	 * @param pckg name of package or <tt>null</tt> (i.e. org.xdef.msg).
	 * @param encoding character encoding of source file or <tt>null</tt> (then
	 * the system encoding is used).
	 * @param reporter where error reports are written.
	 */
	private static void genRegIDsInterface(final ReportTableImpl table,
		final File dir,
		final String pckg,
		String encoding,
		ReportWriter reporter) {
		String prefix = table.getPrefix();
		int prefixLen = prefix.length();
		ArrayList<String> ar = new ArrayList<String>();
		for (Object o: table._msgs.keySet()) {
			String key = (String) o;
			if (!"_prefix".equals(key) && !"_language".equals(key)
				&& !"_defaultLanguage".equals(key)) {
				ar.add(key.substring(prefixLen));
			}
		}
		String[] ids = ar.toArray(new String[0]);
		Arrays.sort(ids); // sort it!
		table._ids = ids;
		String fname = dir.getAbsolutePath().replace('\\', '/');
		if (!fname.endsWith("/")) {
			fname += "/";
		}
		fname += prefix + ".java";
		File f = new File(fname);
		try {
			OutputStreamWriter out = new OutputStreamWriter(
				new FileOutputStream(f), encoding == null ? "UTF-8" : encoding);
			out.write(
"// This file was generated automatically, DO NOT modify it!"+LN+
"package " + (pckg == null ? "org.xdef.msg" : pckg) + ";"+LN+LN+
"/** Registered identifiers of reports with the prefix " + prefix + ". */"+LN+
"public interface " + prefix + " {"+LN);
			for (int i = 0; i < table._ids.length; i++) {
				String id = prefix + table._ids[i];
				String s = table.getReportText(id);
				// generate comment in the default language for this item
				if (s != null && s.length() > 0) {
					s = s.replace("&", "&amp;");
					s = s.replace("<", "&lt;");
					s = s.replace(">", "&gt;");
					s = s.replace("*/", "*&#47;");
					out.write("\t/** " + s + " */"+LN);
				}
				// generate the field with the value of the registered item
				long regID = ReportTable.getRegisteredReportId(table, i);
				out.write(
"\tpublic static final long " + id + " = " + regID + "L;"+LN);
			}
			// identifier which is equal to the prefix contans default language
			out.write(LN+"\t/** Default language. */"+LN+
"\tpublic static final String "+prefix+" = \""+table.getLanguage()+"\";"+LN+
'}');
			out.close();
		} catch (Exception ex) {
			reporter.fatal(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Generate Java source class with reports.
	 * @param table report table.
	 * @param dir output directory where generated source will be written.
	 * @param pckg name of Java package or <tt>null</tt>. If the parameter is
	 * <tt>null</tt> it is used the default value "org.xdef.msg").
	 * @param encoding character set encoding of output file or <tt>null</tt>.
	 * If the argument is <tt>null</tt> it is used the default system character
	 * set from Java VM.
	 * @param registeredTable the registered report table.
	 * @param reporter where error reports are written.
	 */
	private static void genJavaSource(final ReportTableImpl table,
		final File dir,
		final String pckg,
		final String encoding,
		final ReportTableImpl registeredTable,
		final ReportWriter reporter) {
		String prefix = table.getPrefix();
		if (registeredTable == null
			|| !prefix.equals(registeredTable.getPrefix())) {
			//Default report table is incorrect or missing
			//&{0}{, localized table:}
			reporter.error(SYS.SYS220,
				registeredTable==null ? null : registeredTable.getTableName());
			return;
		}
		if (table == registeredTable) {
			//generate registration java source
			genRegIDsInterface(table, dir, pckg, encoding, reporter);
		}
		try {
			if (table != registeredTable) {
				for (Object x: table._msgs.keySet()) {
					String text = registeredTable.getReportText((String) x);
					if (text == null) {
						//Report &{0} from table &{1} is missing in
						//the default table
						reporter.error(SYS.SYS221,
							prefix + x, table.getTableName());
					}
				}
				for (String key: registeredTable._ids) {
					String text = table._msgs.getProperty(prefix + key);
					if (text == null) {
						//Report &{0} which is in the default table is missing
						// in table &{1}
						reporter.warning(SYS.SYS225,
							prefix+key, table.getTableName());
					}
				}
			}
			for (String key: registeredTable._ids) {
				String id = prefix + key;
				String text = table._msgs.getProperty(id);
				if (text != null) {
					String[] p1 = table.getReportParamNames(id);
					if (p1 == null) {
						// Unclosed parameter in text of report &{0}, table &{1}
						reporter.error(SYS.SYS212,
							prefix+id, table.getTableName());
						p1 =  new String[0];
					}
					if (table != registeredTable) { //not default table
						String[] p2 = registeredTable.getReportParamNames(id);
						if (p2 == null) {
							p2 = new String[0];
						}
						if (p1.length == p2.length) {
							for (int j = 0; j < p1.length; j++) {
								if (!p1[j].equals(p2[j])) {
									//Parameters in tables &{0} and
									//&{1} differs, report: &{2}
									reporter.error(SYS.SYS217,
										table.getTableName(),
										registeredTable.getTableName(),
										id);
									break;
								}
							}
						} else {
							//Parameters in tables &{0} and &{1} differs,
							//report: &{2}
							reporter.error(SYS.SYS217, table.getTableName(),
								registeredTable.getTableName(), id);
						}
					}
				}
			}
		} catch (Exception ex) {
			String msg = ex.getMessage();
			if (!reporter.toString().isEmpty()) {
				msg = (msg != null ? msg + LN : "") + reporter.toString();
			}
			throw new RuntimeException(msg, ex);
		}
	}

	static final boolean chkReportID(final String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}
		for (int j = 0; j < s.length(); j++) {
			char c;
			if (!Character.isLetterOrDigit(c = s.charAt(j)) && c != '_') {
				return false;
			}
		}
		return true;
	}

	/** Get array of existing files represented by given argument. The argument
	 * can either represent one concrete file or it can represent a set of files
	 * with wildcards '*' and/or '?'.
	 * @param wildName file name (wildcards are accepted) .
	 * @param caseInsensitive if true then name comparing is case insensitive.
	 * @return array of existing files according to argument.
	 */
	private static File[] getFileGroup(final String wildName,
		final boolean caseInsensitive) {
		if (wildName.indexOf('*') < 0 && wildName.indexOf('?') < 0) {
			File f = new File(wildName);
			return f.exists() ? new File[]{f} : new File[0];
		}
		String wn = wildName.replace('\\','/');
		File dir;
		int i;
		if ((i = wn.lastIndexOf('/')) >= 0) {
			dir = new File(wn.substring(0,i));
			wn = wn.substring(i + 1);
		} else {
			dir = new File(getActualPath());
		}
		return dir.listFiles(new NameWildCardFilter(wn, caseInsensitive));
	}

	/** Get actual path as string.
	 * @return string with actual path.
	 */
	private static String getActualPath() {
		try {
			File f = new File(".");
			if (f.isDirectory()) {
				String s = f.getCanonicalPath();
				return (!s.endsWith(File.separator)) ? s + File.separator : s;
			}
		} catch (Exception ex) {}
		throw new SRuntimeException(SYS.SYS051); //Actual path isn't accessible
	}

	/** Create report table from properties.
	 * @param reportTable properties wit reports.
	 * @return created ReportTable.
	 * @throws RuntimeException if an error occurs.
	 */
	static ReportTableImpl genReportTable(final Properties reportTable) {
		String prefix = reportTable.getProperty("_prefix");
		if (prefix == null || prefix.length() < 3) {
			//SYS214 Message prefix is incorrect or not specified: &{0}
			throw new SRuntimeException(SYS.SYS214, prefix);
		}
		int prefixLen = prefix.length();
		for (Object o: reportTable.keySet()) {
			String key = (String) o;
			if ("_prefix".equals(key) || "_language".equals(key)
				|| "_defaultLanguage".equals(key)) {
				continue;
			}
			String id;
			if (!key.startsWith(prefix)
				|| !chkReportID(id = key.substring(prefixLen))) {
				//Incorrect message key: &{0}
				throw new SRuntimeException(SYS.SYS216, key);
			}
			String text = reportTable.getProperty(key);
			if (text != null) {
				int j = text.indexOf("&{");
				while (j >= 0) {
					int k = text.indexOf('}', j + 2);
					if (k < 0) {
						//Unclosed parameter in the modification of report &{0}
						throw new SRuntimeException(SYS.SYS218, prefix+id);
					}
					if (text.charAt(j + 2) == '#') {//report reference
						if (j + 6 >= k) {
							//Incorrect parameter reference on position: &{0}
							//(report ID: &{1});
							throw new SRuntimeException(SYS.SYS219, j, key);
						}
						String refid = text.substring(j + 3, k);
						String refPrefix = ReportTable.getPrefixFromID(refid);
						if (refPrefix == null || refPrefix.length() < 3) {
							//Incorrect parameter reference on position: &{0}
							//(report ID: &{1});
							throw new SRuntimeException(SYS.SYS219, j, key);
						}
					}
					j = text.indexOf("&{", k + 1);
				}
			}
		}
		String language = reportTable.getProperty("_language");
		if (language == null || language.length() < 3) {
			//Message language is incorrect or not specified: &{0}
			throw new SRuntimeException(SYS.SYS215, language);
		}
		String defaultLanguage = reportTable.getProperty("_defaultLanguage");
		ReportTableImpl result =
			new ReportTableImpl(prefix, language, defaultLanguage, reportTable);
		if (defaultLanguage != null) {
			result.setDefaultLanguage(defaultLanguage);
		}
		return result;
	}

	/** Generate report table from property file.
	 * @param file file name of properties.
	 * @return ReportTable created from properties.
	 */
	static final ReportTable readReporTable(final String file) {
		return genReportTable(ReportTable.readProperties(file));
	}

	/** Generate report tables from property files.
	 * @param files array of name of source files.
	 * @return array with ReportTable objects created from properties.
	 */
	static final ReportTable[] readReporTables(final String[] files,
		ReportWriter reporter) {
		ReportTableImpl[] msgTables = null;
		for (int i = 0; i < files.length; i++) {
			String s = files[i].replace('\\', '/');
			try {
				ReportTableImpl x = (ReportTableImpl) readReporTable(s);
				if (msgTables == null) {
					msgTables = new ReportTableImpl[] {x};
				} else {
					ReportTable[] old = msgTables;
					int oldLen = old.length;
					msgTables = new ReportTableImpl[oldLen + 1];
					System.arraycopy(old, 0, msgTables, 0, oldLen);
					msgTables[oldLen] = x;
				}
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				}
				String msg = ex.getMessage();
				if (msg == null) {
					throw new RuntimeException("Program exception", ex);
				} else {
					throw new RuntimeException(msg, ex);
				}
			}
		}
		String defaultLanguage = null;
		for (ReportTableImpl x: msgTables) {
			String s = x.getDefaultLanguage();
			if (s != null) {
				if (defaultLanguage == null) {
					defaultLanguage = s;
				} else if (!defaultLanguage.equals(s)) {
					//Ambiguous default language of reports" &{0}
					//in the table &{1}, set &{2}
					reporter.warning(SYS.SYS213,
						x.getLanguage(), defaultLanguage);

				}
			}
		}
		if (defaultLanguage == null) {
			defaultLanguage = "eng";
		}

		// get three letters language code
		defaultLanguage = ReportTable.getISO3Language(defaultLanguage);
		for (ReportTableImpl x: msgTables) {
			x.setDefaultLanguage(defaultLanguage);
		}
		return msgTables;
	}
	/** Compute registered localized table ID.
	 * @param tableName table name.
	 * @return registered localized table ID.
	 */
	final static long getTableID(final String tableName) {
		int i = tableName.indexOf('_');
		return ReportTable.getPrefixID(tableName.substring(0, i))
			| ReportTable.getLanguageID(tableName.substring(i + 1));
	}

	/** Generation of Java sources of "registered" message tables from
	 * files with properties.
	 * @param msgTables files with properties.
	 * @param outDir directory where to generate.
	 * @param encoding required code table name for generated Java code.
	 * If null the actual code of system is used.
	 * @param pckg package name of generated classes. If null the package
	 * name will be org.common.msg.
	 */
	private static void genRegisteredJavaTables(
		final ReportTableImpl[] msgTables,
		final File outDir,
		final String encoding,
		final String pckg,
		ArrayReporter reporter) {
		//gen default tables
		for (int j = 0; j < msgTables.length; j++) {
			ReportTableImpl table = msgTables[j]; //default table
			if (table.getDefaultLanguage().equals(table.getLanguage())) {
				//set all languages to default table
				for (int k = 0; k <  msgTables.length; k++) {
					if (k != j) {
						ReportTable t = msgTables[k];
						if (table.getPrefix().equals(t.getPrefix())) {
							table.addLanguage(t.getLanguage());
						}
					}
				}
				genJavaSource(table, outDir, pckg, encoding, table, reporter);
			}
		}
		for (int j = 0; j < msgTables.length; j++) {
			ReportTable table = msgTables[j];
			if (table.getDefaultLanguage().equals(table.getLanguage())) {
				//set all languages from default table
				for (int k = 0; k <  msgTables.length; k++) {
					if (k != j) {
						ReportTable t = msgTables[k];
						if (table.getPrefix().equals(t.getPrefix())) {
							String[] languages = table.getLanguages();
							for (int m = 0; m < languages.length; m++) {
								t.addLanguage(languages[m]);
							}
						}
					}
				}
			}
		}
		//gen tables for other localizations
		for (int j = 0; j < msgTables.length; j++) {
			ReportTableImpl table = msgTables[j];
			if (table.getDefaultLanguage().equals(table.getLanguage())) {
				continue; //already generated
			}
			ReportTableImpl table1 = null;
			//find default table
			for (int k = 0; k < msgTables.length; k++) {
				ReportTableImpl t = msgTables[k];
				if (table.getPrefix().equals(t.getPrefix())
					&& table.getDefaultLanguage().equals(t.getLanguage())) {
					table1 = t;
				}
			}
			genJavaSource(table, outDir, pckg, encoding, table1, reporter);
		}
		reporter.checkAndThrowErrors();
		if (reporter.errorWarnings()) {
			// only warnings or information
			System.out.println(reporter.printToString());
		}
	}

	/** Create Java source class with registered reports from XML source file.
	 * If output directory is not specified then the created source is stored
	 * to the same directory as XML source). The errors recognized while
	 * processing if input data are reported on given stream. If the stream
	 * is not specified it is set the System.err stream. The output file is
	 * generated in specified encoding. If encoding is not specified then it is
	 * used the default system character encoding.
	 * @param args the command line arguments with following structure:
	 * <p>Parameters: source -i input -o outDir [-p package] [-c encoding] [-r]
	 * </p>
	 * <p>-i -i input pathname(s) of property file(s) with report texts (the
	 * file name may contain wildcard characters).</p>
	 * <p>-o the directory where Java source with report tables
	 * are generated</p>
	 * <p>-p package name where tables will be generated. Default value is
	 * "org.xdef.msg"</p>
	 * <p>-r: generate interface with registered identifiers of given
	 * table from input.</p>
	 *<p>-c encoding: character set name of output file (default is the
	 * system character set).</p>
	 *<p>-l ISO code of obligatory language of reports(default is "eng")</p>
	 *<p>-h: help.</p>
	 */
	public static void main(String... args) {
		final String HDRMSG =
"RegisterReportTables - generator of registered report tables.\n"+
"Parameters: source -i input -o outDir [-p package] [-c encoding] [-r]\n"+
"where:\n"+
"-i input pathname(s) of property file(s) with report texts (the file name\n"+
"   may contain wildcard characters).\n"+
"-o the directory where Java source with report tables are generated\n"+
"-p package name of generated tables. Default value: \"org.xdef.msg\"\n"+
"-c endoding: character set name of output file (default is UTF-8).\n"+
"-h: help.";
		if (args == null || args.length == 0) {
			throw new RuntimeException("Missing parameters.\n\n" + HDRMSG);
		}
		String[] files = null;
		String pckg = null;
		String encoding = null;
		File outDir = null;
		int len = args.length - 1;
		StringWriter errWriter = new StringWriter();
		PrintWriter errors = new PrintWriter(errWriter);
		for (int i = 0, j = 0; i <= len; i++) {
			if (args[i].startsWith("-")) {
				if (args[i].length() >= 2) {
					switch (args[i].charAt(1)) {
						case 'h':
							System.out.println(HDRMSG);
							return;
						case 'i':
							ArrayList<String> ar = new ArrayList<String>();
							while (i + 1 <= len && !args[i+1].startsWith("-")) {
								File[] ff = getFileGroup(args[++i], false);
								if (ff.length == 1
									&& ff[0].exists() && ff[0].isDirectory()) {
									ff = ff[0].listFiles();
									ArrayList<File> af = new ArrayList<File>();
									for (File fi: ff){
										if (fi.exists() && fi.isFile()
											&& fi.getName().endsWith(
												".properties")) {
											af.add(fi);
										}
									}
									ff = af.toArray(new File[ar.size()]);
								}
								if (ff == null || ff.length == 0) {
									errors.println("No input file(s): "
										+ args[i]);
									continue;
								}
								for (File fi: ff) {
									if (!fi.isFile()) {
										errors.println(fi.getAbsolutePath()
											+ " is not a file.");
										continue;
									}
									try {
										String s = fi.getCanonicalPath();
										if (!ar.contains(s)) {
											ar.add(s);
										} else {
											errors.println(s+" is duplicated.");
										}
									} catch (Exception ex) {
										errors.println(fi.getAbsolutePath()
											+ " is not correct file.");
									}
								}
							}
							ar.toArray(files = new String[ar.size()]);
							continue;
						case 'o':
							if (outDir != null) {
								errors.println(
									"Duplicated parameter -i: " + args[i]);
							}
							if (i + 1 <= len && !args[i + 1].startsWith("-")) {
								outDir = new File(args[++i]);
								if (!outDir.isDirectory()) {
									errors.println("Incorrect output directory:"
										+ args[i-1]);
								}
							}
							continue;
						case 'c':
							if (encoding != null) {
								errors.println("Duplicated parameter -c: "
									+ args[i]);
							}
							if (args[i].length() > 2) {
								encoding = args[i].substring(2);
							} else {
								if (++i <= len && !args[i].startsWith("-")) {
									encoding = args[i];
								} else {
									errors.println("Missing encoding");
								}
							}
							continue;
						case 'p':
							if (pckg != null) {
								errors.println(
									"Duplicated parameter -o: " + args[i]);
							}
							if (args[i].length() > 2) {
								pckg = args[i].substring(2);
							} else {
								if (++i <= len && !args[i].startsWith("-")) {
									pckg = args[i];
								} else {
									errors.println("Missing package name");
								}
							}
							continue;
						default:
							errors.println("Unknown switch: " + args[i]);
					}
				}
			}
			errors.println("Incorrect parameter: " + args[i]);
		}
		if (files == null || files.length == 0) {
			errors.println("No input file available");
		}
		if (outDir == null || !outDir.isDirectory()) {
			errors.println(
				"Output directory: is not specified or incorrect: " +  outDir);
		}
		errors.close();
		if (!errWriter.toString().isEmpty()) {
			throw new RuntimeException(errWriter.toString() + LN + HDRMSG);
		}
		ArrayReporter reporter = new ArrayReporter();
		ReportTableImpl[] msgTables = (ReportTableImpl[])
			readReporTables(files, reporter);
		if (msgTables != null) {
			genRegisteredJavaTables(msgTables,
				outDir, encoding, pckg, reporter);
		} else {
			//No report tables generated
			throw new SRuntimeException(SYS.SYS223);
		}
	}
}