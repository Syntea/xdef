package org.xdef.sys;

import java.io.File;
import java.io.FileFilter;

/** Implements FileFilter for files with given extension (suffix).
 * @author Vaclav Trojan
 */
public class ExtensionFileFilter implements FileFilter {

	/** The extension (or last characters of file names). */
	private final String _extension;

	/** Creates a new instance of MsgFileFilter
	 * @param extension String containing extension (including '.').
	 */
	public ExtensionFileFilter(final String extension) {_extension = extension;}

	/** Creates a new instance of MsgFileFilter
	 * @param ch character representing extension.
	 */
	public ExtensionFileFilter(final char ch) {_extension = "." + ch;}

	@Override
	/** Method implementing java.io.FileFilter.accept(file).
	 * @param pathname The file to be checked.
	 * @return <tt>true</tt> if file name ends with string specified by
	 * constructor.
	 */
	public boolean accept(final File pathname) {
		return pathname.getName().endsWith(_extension);
	}

}