package org.xdef.sys;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Properties;
import java.util.Set;
import org.xdef.XDConstants;
import org.xdef.msg.SYS;
import org.xdef.sys.RegisterReportTables.ReportTable;
import org.xdef.xml.KXmlUtils;

/** Provides managing of properties, languages and report tables. It exists
 * the singleton instance of SManager. You can get this instance by the static
 * method {@link org.xdef.sys.SManager#getInstance()}.
 *
 * Note the SManager is used as manager of reports. Therefore in such
 * multi thread applications which requires to have well defined environment for
 * the language, properties and/or message tables you have to synchronize run
 * of such application by:
 * <pre><code><b>
 * synchronize(SManager.getSManager()) {
 * ...
 * }
 * </b></code></pre>
 *
 * @author Vaclav Trojan &lt;vaclav.trojan@syntea.cz&gt;
 */
public final class SManager implements XDConstants {
	/** The unique instance of SManager. */
	private static final SManager MANAGER = new SManager();
	/** Length of array of report tables. */
	private int _tableHighIndex = -1;
	/** Increment of table length. */
	private final static int TABLE_INC = 16;
	/** Array of package names. */
	private String[] _packages = new String[] {"org.xdef.msg"};
	/** Sorted array of report tables (key is registered table ID). */
	private ReportTable[] _tables = new ReportTable[TABLE_INC];
	/** Properties. */
	private Properties _properties;
	/** Language information (the actual and the primary language). */
	private SLanguage _language;

	private static final class SLanguage {
		private final String _language;
		private final int _languageID;
		private final String _defaultLanguage;
		private final int _defaultLanguageID;
		SLanguage(final String language, final String primaryLanguage) {
			_language = language;
			_defaultLanguage = primaryLanguage;
			_languageID = ReportTable.getLanguageID(_language);
			_defaultLanguageID = ReportTable.getLanguageID(_defaultLanguage);
		}
		private String getLanguage() {return _language;}
		private int getID() {return _languageID;}
		private String getDefaultLanguage() {return _defaultLanguage;}
		private int getDefaultID() {return _defaultLanguageID;}
	}

	/** This constructor can be invoked only from this class .*/
	private SManager() {
		_properties = (Properties) System.getProperties().clone();
		String s = getProperty(_properties, XDPROPERTY_MSGLANGUAGE);
		s = (s == null) ? SUtils.getISO3Language() : SUtils.getISO3Language(s);
		_language = new SLanguage(s, "eng");
	}

	/** Get value property or environment variable.
	 * @param props Object with properties.
	 * @param key name of property.
	 * @return value property or environment variable.
	 */
	public final static String getProperty(final Properties props,
		final String key) {
		String val = null;
		if (props != null) {
			String newKey = key.startsWith("xdef.") ? key.replace('.','_'): key;
			val = props.getProperty(key);
			if (key.equals(newKey)) {
				if (val == null) {
					String oldKey = key.startsWith("xdef_")
						? key.replace('_','.'): key;
					val = props.getProperty(oldKey);
					if (val != null) {
						props.remove(key);
						props.setProperty(newKey, val);
					}
				}
			}
		}
		if (val == null || (val = val.trim()).isEmpty()) {
			val = System.getenv(key);
		}
		return (val != null && (val=val.trim()).isEmpty()) ? null : val;
	}

	/** Get SManager property.
	 * @param name property name.
	 * @return string with property value or <tt>null</tt>.
	 */
	public static final String getProperty(final String name) {
		return getProperty(getInstance()._properties, name);
	}

	/** Set properties to the SManager .
	 * @param properties the clone from this argument and set to SManager.
	 */
	public static final void setProperties(Properties properties) {
		SManager sm = getInstance();
		synchronized (sm) {
			sm._properties = (Properties) properties.clone();
			String s = getProperty(sm._properties,XDPROPERTY_MSGLANGUAGE);
			if (s != null) {
				s = (s == null)
					? sm._language.getLanguage() : SUtils.getISO3Language(s);
				sm._language = new SLanguage(s, "eng");
			}
		}
	}

	/** Get SManager properties.
	 * @return properties from the SManager.
	 */
	public static final Properties getProperties() {
		return getInstance()._properties;
	}

	/** Set SManager property.
	 * @param key property name.
	 * @param value property value (may be <tt>null</tt> - then the property
	 * is removed).
	 * @return string with original property value or <tt>null</tt>.
	 */
	public static final String setProperty(final String key,
		final String value) {
		SManager sm = getInstance();
		String newKey = key.startsWith("xdef.") ? key.replace('.', '_') : key;
		synchronized (sm) {
			String result = getProperty(sm._properties, newKey);
			if (result == null) {
				result = getProperty(sm._properties, key);
			}
			sm._properties.remove(key);
			sm._properties.remove(newKey);
			if (value != null) {
				sm._properties.setProperty(newKey, value);
			}
			if (XDPROPERTY_MSGLANGUAGE.equals(newKey)) {
				String v = value;
				if (v == null) {
					v = SUtils.getISO3Language();
				}
				sm._language = new SLanguage(SUtils.getISO3Language(v),
					sm._language.getDefaultLanguage());
			}
			return result;
		}
	}

	/** Set actual language and primary language for reports.
	 * @param language language for reports.
	 * @return the language object.
	 */
	public static final Object setLanguage(final String language) {
		SManager sm = getInstance();
		synchronized (sm) {
			String lang = SUtils.getISO3Language(language);
			String plang = sm._language.getDefaultLanguage();
			if (!sm._language.getLanguage().equals(lang)
				|| !sm._language.getDefaultLanguage().equals(plang)) {
				sm._language = new SLanguage(lang, plang);
			}
			return sm._language;
		}
	}

	/** Get actual language for reports.
	 * @return actual language for reports.
	 */
	public static final String getLanguage() {
		return getInstance()._language.getLanguage();
	}

	/** Get default language.
	 * @return default language.
	 */
	public static final String getDefaultLanguage() {
		return getInstance()._language.getDefaultLanguage();
	}

	/** Add package with registered reports.
	 * @param packageName new package to be added.
	 */
	public final static void addReportPackage(final String packageName) {
		SManager sm = getInstance();
		synchronized (sm) {
			for (String s : sm._packages) {
				if (s.equals(packageName)) return; // already exists
			}
			String[] newPackages = new String[sm._packages.length + 1];
			System.arraycopy(sm._packages,0,newPackages,0,sm._packages.length);
			newPackages[sm._packages.length] = packageName;
			sm._packages = newPackages;
		}
	}

	/** Remove package with registered reports.
	 * @param packageName package to be removed.
	 */
	public final static void removeReportPackage(final String packageName) {
		SManager sm = getInstance();
		synchronized (sm) {
			String[] newPackages = new String[sm._packages.length - 1];
			int j = 0;
			for (int i = 0; i < sm._packages.length; i++) {
				String s = sm._packages[i];
				if (j < sm._packages.length && !s.equals(packageName)) {
					newPackages[j++] = s;
				}
			}
			if (j < sm._packages.length) {
				sm._packages = newPackages;
			}
		}
	}

	/** Add report table created from properties.
	 * @param reportTable properties with reports.
	 * @throws RuntimeException if an error occurs.
	 */
	public static final void addReports(final Properties reportTable) {
		addReportTable(RegisterReportTables.genReportTable(reportTable));
	}

	/** Get ReportTable.
	 * @param tableNameID name of report table.
	 * @return report table.
	 */
	final ReportTable getReportTable(final String tableNameID) {
		return findReportTable(RegisterReportTables.getTableID(tableNameID));
	}

	/** Get report table associated with given report. If the argument
	 * language is null, the primary langue table is searched.
	 * @param reportID report ID.
	 * @param language language code (ISO-639) or <tt>null</tt>.
	 * @return report table or <tt>null</tt>.
	 */
	final ReportTable getReportTable(final String reportID,
		final String language) {
		String langId = language == null ? getLanguage() :
			SUtils.getISO3Language(language);
		String prefix = ReportTable.getPrefixFromID(reportID);
		if (prefix == null) {
			return null;
		}
		return getReportTable(prefix + "_" + langId);
	}

	/** Remove all report tables from manager. */
	public static final void removeAllReportTables() {
		SManager sm = getInstance();
		synchronized (sm) {
			if (sm._tableHighIndex >= 0) {
				if (sm._tables.length > TABLE_INC) {
					sm._tables = new ReportTable[TABLE_INC];
				} else {
					for (int i = 0; i <= sm._tableHighIndex; i++) {
						sm._tables[i] = null;
					}
				}
				sm._tableHighIndex = -1;
			}
		}
	}

	/** Remove report tables with given prefix for all languages.
	 * @param prefix Prefix of report tables.
	 */
	public static final void removeReportTables(final String prefix) {
		SManager sm = getInstance();
		synchronized (sm) {
			String pfx = prefix + '_';
			for (int i = sm._tableHighIndex; i >= 0 ; i--) {
				String tableName = sm._tables[i].getTableName();
				if (tableName.startsWith(pfx)) {
					sm.removeReportTable(tableName);
				}
			}
		}
	}

	/** Remove specified report table.
	 * @param prefix prefix of messages in table.
	 * @param language language of table.
	 */
	public static final void removeReportTable(final String prefix,
		final String language) {
		SManager sm = getInstance();
		synchronized (sm) {
			sm.removeReportTable(prefix + "_" + language);
		}
	}

	/** Get array of langue identifiers of available languages for given
	 * report ID.
	 * @param reportID the report ID
	 * @return array of langue identifiers of available languages.
	 */
	public static final String[] getAvailableLanguages(final String reportID) {
		Set<String> x = new TreeSet<String>();
		String prefix = ReportTable.getPrefixFromID(reportID) + '_';
		SManager sm = getInstance();
		synchronized (sm) {
			for (int i = 0; i <= sm._tableHighIndex; i++) {
				String tableName = sm._tables[i].getTableName();
				if (tableName.startsWith(prefix)) {
					x.add(tableName.substring(0,prefix.length() - 1));
				}
			}
		}
		String[] result = new String[x.size()];
		x.toArray(result);
		return result;
	}

	/** Get instance of SManager.
	 * @return instance of SManager.
	 */
	public final static SManager getInstance() {return MANAGER;}

	/** Modify given text with modification string (see description
	 * above {@link Report}).
	 * @param text The original text.
	 * @param modification The modification string.
	 * @param language language id (ISO-639).
	 * @return The modified text.
	 */
	public final static String getModifiedText(final String text,
		final String modification,
		final String language) {
		int indx1, indx;
		if (text == null || (indx1 = text.indexOf("&{")) < 0) {
			return text;
		}
		indx = 0;
		String mod;
		//resolve report references in the modification string
		if ((mod = modification) != null && modification.length() > 0) {
			int i = mod.indexOf("&{#");
			while (i >= 0) {
				int j = mod.indexOf('}', i + 3);
				if (j < 0) {
					break; //error
				}
				String id;
				String t = "";
				String m = null;
				int p = mod.indexOf('{', i + 3);
				if (p > 0 && p < j) {
					id = mod.substring(i + 3, p).trim();
					j = getParamaterEnd(mod, p);
					if (j < 0) {
						break; //error
					}
					t = mod.substring(p + 1, j++);
					p = mod.indexOf('{', j);
					j = mod.indexOf('}', j);
					if (p > 0 && p < j) {
						int q = p;
						do {
							if ((j = getParamaterEnd(mod, q)) < 0) {
								break; //error
							}
							q = mod.indexOf('{', j);
							j = mod.indexOf('}', j + 1);
						} while (q > 0 && q < j);
						if (j < 0) {
							break; //error
						}
						m = mod.substring(p + 1, j - 1);
					}
				} else {
					id = mod.substring(i + 3, j).trim();
				}
				Report r = Report.text(id.trim(), t, m);
				mod = mod.substring(0, i) + r.toString(language)
					+ mod.substring(j + 1);
				i = mod.indexOf("&{#", i);
			}
		} else {
			mod = null;
		}
		StringBuilder sb = new StringBuilder();
		int len = text.length();
		while (true) {
			sb.append(text.substring(indx, indx1)); //part without parameter
			int keyEnd = text.indexOf('}', indx1 + 2);
			if (keyEnd < 0) {
				break; //error
			}
			String param = text.substring(indx1, keyEnd + 1); //whole param
			indx1 = keyEnd + 1;
			int paramPos;
			if (mod == null || (paramPos = mod.indexOf(param)) < 0) {
				//parameter was not found in modification string
				if (indx1 < len && text.charAt(indx1) == '{') {//follows '{'
					indx1++; //yes, next char
					int max = text.indexOf('}', indx1); //end of section
					if (max < 0) {
						break; //error
					}
					if (indx1 < max && text.charAt(indx1) == '&') {
						//copy key
						indx1++;
						indx = text.indexOf('&', indx1); //next param
						if (indx < 0) {
							indx = max; //next param
						}
						if (indx > indx1 && indx <= max) {
							sb.append(text.substring(indx1, indx));
						}
					}
					indx1 = max;
					if (max < len) {
						indx1++;
						if (indx1 < len && text.charAt(indx1) == '{') {
							indx1++;
							max = text.indexOf('}', indx1);
							indx1 = max > 0 ? max + 1 : len;
						}
					}
				}
			} else { //parameter was found in modification string
				String postfix = null;
				if (indx1 < len && text.charAt(indx1) == '{') {
					indx1++;
					int max = text.indexOf('}', indx1);
					if (max < 0) {
						max = len;
					}
					if (indx1 < max && text.charAt(indx1) == '&') {
						//copy key
						indx1++;
						indx = text.indexOf('&', indx1);
						indx1 = (indx >= indx1 && indx < max) ? indx + 1 : max;
					}
					sb.append(text.substring(indx1, max));
					indx1 = max;
					if (max < len) {
						indx1++;
						if (indx1 < len && text.charAt(indx1) == '{') {
							indx1++;
							max = text.indexOf('}', indx1);
							if (max < 0) {
								postfix = text.substring(indx1);
								indx1 = len;
							} else {
								postfix = text.substring(indx1, max);
								indx1 = max + 1;
							}
						}
					}
				}
				paramPos += param.length();
				int paramPosEnd = mod.indexOf("&{", paramPos);
				if (paramPosEnd < 0) {
					paramPosEnd = mod.length();
				}
				sb.append(mod.substring(paramPos, paramPosEnd));
				if (postfix != null) {
					sb.append(postfix);
				}
			}
			indx = indx1;
			if ((indx1 = text.indexOf("&{", indx1)) < 0) {
				break;
			}
		}
		return sb.append(text.substring(indx)).toString();
	}

	////////////////////////////////////////////////////////////////////////////
	// Auxiliary methos called from this package.
	////////////////////////////////////////////////////////////////////////////

	/** Get text of report from report table. All references to other reports
	 * are resolved if the argument <tt>resolveReferences</tt> is <tt>true</tt>.
	 * @param regID registered report ID.
	 * @param language language code.
	 * @param resolveReferences if <tt>true</tt> then all references to other
	 * reports are resolved.
	 * @return text of report.
	 */
	final String getReportText(final long regID,
		final String language,
		final boolean resolveReferences) {
		return getReportText(regID,
			language != null ?
				ReportTable.getLanguageID(language) : 0, resolveReferences);
	}

	/** Get text of report from report table. All references to other reports
	 * are resolved if the argument <tt>resolveReferences</tt> is <tt>true</tt>.
	 * @param reportID report ID.
	 * @param language code of required language or <tt>null</tt> (then
	 * the actual setting language is used).
	 * @param resolveReferences if <tt>true</tt> then all references to other
	 * reports are resolved.
	 * @return text of report.
	 */
	final String getReportText(final String reportID,
		final String language,
		final boolean resolveReferences) {
		String id;
		if ((id = reportID) == null || (id = id.trim()).length() == 0) {
			return null;
		}
		ReportTable table = getReportTable(id, language);
		if (table == null) {
			if ((table = getReportTable(id, getDefaultLanguage())) == null) {
				return null;
			}
		}
		String text = table.getReportText(id);
		if (text == null) {
			 if (getDefaultLanguage().equals(table.getLanguage())) {
				 return null;
			 }
			table = getReportTable(id, getDefaultLanguage());
			text = table == null ? null : table.getReportText(id);
		}
		String lang = language != null
			? SUtils.getISO3Language(language) : getLanguage();
		return resolveReferences ? resolveReportReferences(text, lang) : text;
	}

	/** Find available report table for the the actual language or for the
	 * primary language. If the actual language table is not available then
	 * a table for the primary language is searched. If none of those was found
	 * then return <tt>null</tt>.
	 * @param regID registered report ID.
	 * @return report table or <tt>null</tt>.
	 */
	final ReportTable getReportTable(final long regID) {
		return findReportTable(regID
			& ReportTable.REGTABIDMASK | _language.getID());
	}

	/** Get modified text of localized registered report (see description of
	 * this class above) .
	 * @param regID registered report ID.
	 * @param modification The modification string.
	 * @param language language id (ISO-639).
	 * @return The text of localized report in given language or <tt>null</tt>.
	 */
	final String getLocalizedText(final long regID,
		final String modification,
		final String language) {
		long tabid = regID & ReportTable.REGTABIDMASK |
			(language != null ? ReportTable.getLanguageID(language) : 0);
		ReportTable table = findReportTable(tabid);
		if (table == null) {
			table =
				findReportTable(regID
					& ReportTable.REGTABIDMASK | _language.getDefaultID());
			if (table == null) {
				return null;
			}
		}
		return getLocalizedText(table.getReportID(regID),
			null, modification,	table.getLanguage());
	}

	/** Get modified text of localized report (see description of this
	 * class above) .
	 * @param reportID report id.
	 * @param modelText model text of report.
	 * @param modification modification string.
	 * @param language language id (ISO-639).
	 * @return localized text of report in given language or <tt>null</tt>.
	 */
	final String getLocalizedText(final String reportID,
		final String modelText,
		final String modification,
		final String language) {
		String mod = modification;
		String text;
		String id;
		if ((id = reportID) != null && (id = id.trim()).length() > 0) {
			text = getReportText(id, language, true);
			if (text == null) {
				text = modelText;
			}
		} else {
			text = resolveReportReferences(modelText, language);
		}
		if (text == null || text.indexOf("&{") < 0) {
			return text;
		}
		if (mod != null) {
			int i = mod.indexOf("&{&&");
			 // process reports included in the modifier and
			while (i >= 0) {
				int j =  mod.indexOf("&}", i + 4);
				StringBuffer sb = (j < 0) ?
					new StringBuffer(mod.substring(i + 4)) :
					new StringBuffer(mod.substring(i + 4, j));
				// resolve entities for characters '<', '>', '"', ''', '&'
				SUtils.modifyStringBuffer(sb,"&lt;","<");
				SUtils.modifyStringBuffer(sb,"&gt;",">");
				SUtils.modifyStringBuffer(sb,"&quot;","\"");
				SUtils.modifyStringBuffer(sb,"&apos;",",");
				SUtils.modifyStringBuffer(sb,"&amp;","&");
				try {
					Report r = new Report(//Read element with report item.
						KXmlUtils.parseXml(sb.toString()).getDocumentElement());
					sb = new StringBuffer(i > 0 ? mod.substring(0, i) : "");
					sb.append(r.toString(language));
				} catch (Exception ex) {
					//Can't read report source
					sb.append(Report.error(SYS.SYS201).toString(language));
				}
				i = sb.length();
				if (j >= 0) {
					sb.append(mod.substring(j + 2));
				}
				mod = sb.toString();
				i = mod.indexOf("&{&", i);
			}
		}
		return getModifiedText(text, mod, language);
	}

	/** Get sorted array of parameter names from report text.
	 * @param reportID report ID.
	 * @param language language id (ISO-639) or <tt>null</tt>.
	 * @return The sorted array of parameter names or <tt>null</tt>.
	 */
	final String[] getReportParamNames(final String reportID,
		final String language) {
		ReportTable table = getReportTable(reportID, language);
		return table == null ? null : table.getReportParamNames(reportID);
	}

	/** Get reportID from registered report ID.
	 * @param regID registered report ID with.
	 * @return reportID decoded from registered report ID.
	 */
	final static String getReportID(final long regID) {
		return getTextFromRegisteredForm(regID >>> ReportTable.IDBITS)
			+ getTextFromRegisteredForm(regID & ReportTable.IDMASK);
	}

	/** Add report tables.
	 * @param reportTables array with report tables.
	 */
	static void addReportTables(final ReportTable[] reportTables) {
		for (ReportTable t: reportTables) {
			addReportTable(t);
		}
	}
	/** Add report table.
	 * @param reportTable report table.
	 * @return report table
	 */
	private static ReportTable addReportTable(final ReportTable reportTable) {
		SManager sm = getInstance();
		synchronized(sm) {
			if (sm._tableHighIndex < 0) {
				sm._tables[0] = reportTable;
				sm._tableHighIndex = 0;
				return reportTable;
			}
			int i = sm.indexOfTable(reportTable.getRegisteredTableID());
			if (i >= 0) {
				ReportTable result = sm._tables[i];
				sm._tables[i] = reportTable; //already exists, we replace it
				return result; //we return the original one;
			}
			//new report table
			int len = sm._tables.length;
			if (len <= ++sm._tableHighIndex) {
				ReportTable[] tables = sm._tables;
				sm._tables = new ReportTable[len + TABLE_INC];
				System.arraycopy(tables, 0, sm._tables, 0, sm._tableHighIndex);
			}
			sm._tables[sm._tableHighIndex] = reportTable;
			sm.sortTables(0, sm._tableHighIndex);
			return reportTable; //not exists yet, we return the table
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// Private methods
	////////////////////////////////////////////////////////////////////////////


	/** Remove specified report table.
	 * @param tableID Report table name.
	 * @return removed table or <tt>null</tt>.
	 */
	private ReportTable removeReportTable(final String tableID) {
		long id  = RegisterReportTables.getTableID(tableID);
		synchronized(this) {
			int i = indexOfTable(id);
			if (i < 0) {
				return null;
			}
			ReportTable result;
			if (_tableHighIndex == 0) {
				result = _tables[0];
				_tableHighIndex = -1;
				_tables[0] = null;
				return result;
			}
			result = _tables[i];
			if (_tables.length - _tableHighIndex > TABLE_INC) {
				ReportTable[] oldTables = _tables;
				_tables = new ReportTable[_tableHighIndex + TABLE_INC];
				if (i > 0) {
					System.arraycopy(oldTables, 0, _tables, 0, i);
				}
				if (i < _tableHighIndex) {
					System.arraycopy(oldTables,
					i + 1, _tables, i, _tableHighIndex - i);
				}
			} else {
				if (i < _tableHighIndex) {
					System.arraycopy(_tables,
						i + 1, _tables, i, _tableHighIndex - i);
				}
				_tables[_tableHighIndex] = null;
			}
			_tableHighIndex--;
			return result;
		}
	}

	/** Get text of report from report table. All references to other reports
	 * are resolved if the argument <tt>resolveReferences</tt> is <tt>true</tt>.
	 * @param regID registered report ID.
	 * @param languageID computed language ID or 0.
	 * @param resolveReferences if <tt>true</tt> then all references to other
	 * reports are resolved.
	 * @return text of report.
	 */
	private String getReportText(final long regID,
		final int languageID,
		final boolean resolveReferences) {
		long tabid = regID & ReportTable.REGTABIDMASK
			| (languageID == 0 ? _language.getID() : languageID);
		ReportTable table = findReportTable(tabid);
		if (table == null) {
			table = findReportTable(
				regID & ReportTable.REGTABIDMASK |_language.getDefaultID());
			if (table == null) {
				return null;
			}
		}
		String text = table.getReportText(regID);
		if (text == null) {
			 if (getDefaultLanguage().equals(table.getLanguage())) {
				 return null;
			 }
			table = findReportTable(
				regID & ReportTable.REGTABIDMASK | _language.getDefaultID());
		}
		return getReportText(
			table.getReportID(regID), table.getLanguage(), resolveReferences);
	}

	/** Get the end of parameter. Parameter may contain inner parts.
	 * @param mod modification string.
	 * @param start starting position of the parameter.
	 * @return end position of parameter
	 */
	private static int getParamaterEnd(final String mod, final int start) {
		int j = start + 1;
		int end = mod.indexOf('}', j);
		while ((j = mod.indexOf('{', ++j)) >= 0 && j < end) {
			int k = mod.indexOf('}', ++end);
			if (k < 0) {
				return end;
			}
			end = k;
		}
		return end;
	}

	/** Resolve report references in the report text.
	 * @param reportText default text of report.
	 * @param language language id (ISO-639).
	 * @return text of report with resolved references.
	 */
	private String resolveReportReferences(final String reportText,
		final String language) {
		int i;
		if (reportText == null || (i = reportText.indexOf("&{#")) < 0) {
			return reportText;
		}
		String text = reportText;
		int level = 0; //recursion level
		int j;
		do {
			j = text.indexOf("}", i + 3);
			if (j < 0) {
				return text; //error in report: reference is not closed
			}
			String s;
			String myId = text.substring(i + 3, j++);
			if (myId.length() > 0 && ++level < 100) {
				ReportTable table = getReportTable(myId, language);
				if (table == null) {
					table = getReportTable(myId, getDefaultLanguage());
					if (table == null) {
						return null;
					}
				}
				s = table.getReportText(myId);
				if (s == null) {
					table = getReportTable(myId, getDefaultLanguage());
					s = table == null ? "" : table.getReportText(myId);
				}
			} else {
				s = "";
			}
			text = text.substring(0, i) + s + text.substring(j);
		} while ((i = text.indexOf("&{#", j)) >= 0);
		return text;
	}

	/** Add report table with the specified table name.
	 * @param tableName table ID (prefix + '_' + language)
	 * @return ReportTable or <tt>null</tt> if no table can be added.
	 */
	private ReportTable addReportTable(final String tableName){
		synchronized(this) {
			int i = indexOfTable(RegisterReportTables.getTableID(tableName));
			if (i >= 0) {
				return _tables[i];
			}
		}
		String[] ids = null; // list of report names in the registred table.
		for (String packageName : _packages) {
			try { // try to read properties from the package
				String prefix = tableName.substring(0, tableName.length() - 4);
				// get class of registered table.
				Class<?> c = Class.forName(packageName + '.' + prefix);
				// get fields of this class.
				Field[] fields = c.getDeclaredFields();
				ArrayList<String> ar = new ArrayList<String>(fields.length-1);
				for (int i = 0, j = 0; i < fields.length; i++) {
					String name = fields[i].getName();
					if (name.startsWith(prefix) && !prefix.equals(name)) {
						// only fields of message identifiers
						ar.add(name.substring(prefix.length()));
					}
				}
				// create sorted array of message identifiers
				ids = new String[ar.size()];
				ar.toArray(ids);
				Arrays.sort(ids);
				// read properties with mmessages
				InputStream input =
					c.getResourceAsStream(tableName + ".properties");
				InputStreamReader in =
					new InputStreamReader(input, Charset.forName("UTF-8"));
				Properties props = new Properties();
				props.load(in);
				in.close();
				ReportTable table = RegisterReportTables.genReportTable(props);
				table._ids = ids;
				return addReportTable(table);
			} catch (Exception ex) {}
			// not found, so we try to read the external data
			String s = getProperty(_properties, XDPROPERTY_MESSAGES+tableName);
			if (s == null) {
				s = getProperty(_properties, XDPROPERTY_MESSAGES
					+ ReportTable.getPrefixFromID(tableName));
				if (s != null) {
					File[] files = SUtils.getFileGroup(s);
					s = null;
					if (files != null) {
						for (File f: files) {
							String name = f.getName();
							if (name.equals(tableName+".properties")) {
								try {
									ReportTable table =
										RegisterReportTables.genReportTable(
											ReportTable.readProperties(
												new FileInputStream(f)));
									table._ids = ids;
									return addReportTable(table);
								} catch (IOException ex) {
									break;
								}
							}
						}
					}
				}
			}
			if (s != null) {
				ReportTable[] tables = null;
				try {
					ReportTable table = RegisterReportTables.genReportTable(
						ReportTable.readProperties(s));
					table._ids = ids;
					return addReportTable(table);
				} catch (Exception ex) {}
				for (int i = 0; i <= _tableHighIndex; i++) {
					if (tableName.equals(tables[i].getTableName())) {
						tables[i]._ids = ids;
						return addReportTable(tables[i]);
					}
				}
			}
		}
		return null;
	}

	/** Get string from registered format.
	 * @param registered encoded registered string.
	 * @return decoded string from registered format.
	 */
	private static String getTextFromRegisteredForm(final long registered) {
		StringBuilder sb = new StringBuilder(10);
		long i = registered;
		while (i > 0) {
			sb.insert(0, (char) (i % 27 + '@'));
			i /= 27;
		}
		return sb.toString();
	}

	/** Get ReportTable for the actual language or for the primary language.
	 * If the actual language table is not available then a table for the
	 * primary language is returned. If none of those was found then return
	 * <tt>null</tt>.
	 * @param tableID registered table ID.
	 * @return report table or <tt>null</tt>.
	 */
	private ReportTable findReportTable(final long tableID) {
		synchronized(this) {
			int i = indexOfTable(tableID);
			if (i >= 0) {
				return _tables[i];
			}
		}
		String prefix =
			getTextFromRegisteredForm(tableID >>> ReportTable.IDBITS);
		//Get language name from registered format
		StringBuilder sb = new StringBuilder(3);
		int i = (int) tableID & ReportTable.IDMASK;
		int x = 'a' - 1;
		while (i > 0) {
			sb.insert(0, (char) (i % 27 + x));
			i /= 27;
		}
		String lang = sb.toString();
		ReportTable result = addReportTable(prefix + '_' + lang);
		if (result != null) {
			return result;
		}
		long primaryID =
			tableID & ReportTable.REGTABIDMASK | _language.getDefaultID();
		if (tableID == primaryID){
			return null;
		}
		synchronized(this) {
			i = indexOfTable(primaryID);
			if (i >= 0) {
				return _tables[i];
			}
		}
		return addReportTable(prefix + '_' + _language.getDefaultLanguage());
	}

	/** Find index of table in sorted array of tables.
	 * @param id table ID.
	 * @return index of table if table was found otherwise return <tt>null</tt>.
	 */
	private int indexOfTable(final long id) {
		int last;
		if ((last = _tableHighIndex) < 0) {
			return -1;
		}
		int first = 0;
		while (first < last) {
			int mid;
			long x = _tables[mid = (last + first) / 2].getRegisteredTableID();
			if (id > x) {
				first = mid + 1;  // repeat search in top half.
			} else if (id < x) {
				last = mid - 1; // repeat search in bottom half.
			} else {
				return mid;  // found it. return position
			}
		}
		return id == _tables[first].getRegisteredTableID() ? first : -1;
	}

	/** Sort array of tables in index sharp interval.
	 * @param low low index.
	 * @param high high index.
	 */
	private void sortTables(final int low, final int high) {
		int lo = low, hi = high;
		long pivot = _tables[(lo + hi) / 2].getRegisteredTableID();
		while (lo <= hi) {
			while (_tables[lo].getRegisteredTableID() < pivot) {
				lo++;
			}
			while (_tables[hi].getRegisteredTableID() > pivot) {
				hi--;
			}
			if (lo <= hi) {
				if (lo < hi) {
					ReportTable tmp = _tables[lo];
					_tables[lo] = _tables[hi];
					_tables[hi] = tmp;
				}
				lo++;
				hi--;
			}
		}
		if (low < hi) sortTables(low, hi);
		if (lo < high) sortTables(lo, high);
	}
}