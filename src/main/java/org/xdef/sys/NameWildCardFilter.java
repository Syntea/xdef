package org.xdef.sys;

import java.io.File;

/** File names filter for names with wild card chars (i.e '*' and/or '?').
 * @author Vaclav Trojan
 */
class NameWildCardFilter implements java.io.FileFilter {

	/** The name with wildcards. */
	private final String _wildName;
	/** Length of the name with wildcards. */
	private final int _wildNameLen;
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
		_wildNameLen = _wildName.length() - 1;
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
		String fname;
		if (_caseInsensitive) {
			fname = file.getName().toLowerCase();
		} else {
			fname = file.getName();
		}
		int fnameLen = fname.length() - 1;
		int i = 0;
		char ch = _wildName.charAt(0);
		int j = 1;
		while (i <= fnameLen) {
			switch (ch) {
				case '*':
					if (j > _wildNameLen) {
						return true;
					}
					if ((ch = _wildName.charAt(j++)) == '.'
						&& _wildName.indexOf('.',j) < 0) {
						int ndx = fname.lastIndexOf('.');
						if (ndx < i) {
							return false;
						}
						i = ndx + 1;
						ch = _wildName.charAt(j++);
						continue;
					} else {
						i = fname.indexOf(ch, i);
						if (i < 0) {
							return false;
						}
						if (j > _wildNameLen) {
							return (i == fnameLen);
						}
						continue;
					}
				case '?':
					if (i > fnameLen) {
						return false;
					}
					if (j > _wildNameLen) {
						return (i == fnameLen);
					}
					i++;
					ch = _wildName.charAt(j++);
					continue;
				default:
					if (ch != fname.charAt(i++)) {
						return false;
					}
					if (j > _wildNameLen) {
						return (i > fnameLen);
					}
					ch = _wildName.charAt(j++);
			}
		}
		return false;
	}

}