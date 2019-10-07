package org.xdef.sys;

import java.io.File;

/** File names filter for names with wild card chars (i.e '*' and/or '?').
 * @author Vaclav Trojan
 */
public class NameWildCardFilter implements java.io.FileFilter {

	/** The name with wildcards. */
	private final String _wildName;
	/** switch if test is case insensitive. */
	private final boolean _caseInsensitive;

	/** Creates new instance of FNameWildCardFilter. Supported wildcards are
	 * Microsoft style: '*' (skip zero or more characters)
	 * and '?' (any character).
	 * @param wildName The string with (possible) wildcards.
	 * @param caseInsensitive if true then name comparing is case insensitive.
	 */
	public NameWildCardFilter(final String wildName, boolean caseInsensitive) {
		_wildName = (_caseInsensitive = caseInsensitive) ?
			wildName.toLowerCase() : wildName;
	}
	
	@Override
	/** Check if the file suits wildcard conditions.
	 * @param file The file to be checked.
	 * @return true if and only if file suits.
	 */
	public boolean accept(final File file) {
		if (file.isDirectory()) {
			return false;
		}
		if (_wildName.length() == 0) {
			return true;
		}
		return chkWildcard(_wildName,
			_caseInsensitive ? file.getName().toLowerCase() : file.getName());
	}

	/** Check if the file name represents the with name with wildcard.
	 * @param wc wildcard name.
	 * @param fn file name.
	 * @return true if the file name represents the with name with wildcard.
	 */
	public static final boolean chkWildcard(final String wc, final String fn) {
		int j = 1, i = 0;
		int fnLen = fn.length() - 1, wcLen = wc.length() - 1;
		char ch = wc.charAt(0);
		while (i <= fnLen) {
			switch (ch) {
				case '*':
					if (j > wcLen) {
						return true;
					}
					if ((ch = wc.charAt(j++)) == '.'
						&& wc.indexOf('.',j) < 0) {
						int ndx = fn.lastIndexOf('.');
						if (ndx < i) {
							return false;
						}
						i = ndx + 1;
						ch = wc.charAt(j++);
						continue;
					} else {
						i = fn.indexOf(ch, i);
						if (i < 0) {
							return false;
						}
						if (j > wcLen) {
							return (i == fnLen);
						}
						continue;
					}
				case '?':
					if (i > fnLen) {
						return false;
					}
					if (j > wcLen) {
						return (i == fnLen);
					}
					i++;
					ch = wc.charAt(j++);
					continue;
				default:
					if (ch != fn.charAt(i++)) {
						return false;
					}
					if (j > wcLen) {
						return (i > fnLen);
					}
					ch = wc.charAt(j++);
			}
		}
		return false;
	}
}