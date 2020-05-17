package buildtools;

import org.xdef.sys.NameWildCardFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Provides canonizing of text files.<p>
 * All lines of the source is checked for leading and trailing spaces and tabs.
 * First, trailing spaces and tabs are removed. Then behavior depends on the
 * switches -t and -s. If switch - t is set all sequences of leading spaces are
 * replaced by tabs, otherwise the tabs are replaced by spaces.
 * All occurrences of couples id carriage return characters (CR - 0x0d) and line
 * feed characters (LF - 0x0a) are replaced by line feed characters. If the
 * switch "-CR" is specified, the couple of both characters is generated.</p>
 * <p>
 * The program may be invoked from command line with following parameters:</p>
 * <p><tt>-i input [-r] [-o outputDirectory] (-t |-s) [-n indent] [-h]</tt></p>
 * <p>where:</p>
 * <ul>
 * <li> -i input Input may be specified as the file with input source or as the
 * directory. If the parameter is not directory, also wildcards '*' or '?' may
 * be used to specify group of files. The parameter is obligatory.</li>
 * <li> -r process directory tree. The parameter is optional and forces to
 * process all subdirectories of the directory where process started.</li>
 * <li> -o outputDirectory: The directory where output files are stored. The
 * parameter is optional. If it is not specified the source file is
 * overwritten.</li>
 * <li> -t spaces are replaced by tabs The parameter is optional.</li>
 * <li> -s tabs are replaced by spaces The parameter is optional.</li>
 * <li> -cr lines are separated by the couple of CR and LF The parameter
 * is optional.</li>
 * <li> -h The parameter displays help text.</li>
 * </ul>
 * <i>Switches -t and  -s are exclusive. If none of them is specified the -s is
 * set as the default value.</i>
 *
 * @author  Vaclav Trojan
 */
public class CanonizeSource {

	//parameters:
	private PrintStream _out;
	private PrintStream _err;
	private boolean _verbose;
	private boolean _spacesToTab;
	private int _indentSize;
	private int _oldIndent;
	private String _indentSpaces;
	private String _mask;
	private boolean _genCR;
	private String _header;
	private boolean _headerKeep;
	private String _tail;
	private boolean _tailKeep;
	private String _charset;

	//result information:
	private int _processedCount;
	private int _modifyCount;

	//work fields:
	private int _errors;
	private StringBuffer _sb;
	private int _linePos;
	int _lastCommentStart;
	int _lastCommentEnd;
	int _lastCommentLine;
	int _firstCommentStart;
	int _firstCommentEnd;

	/** Creates new instance of ToNewPrepoc - just prevent the user to do it.*/
	private CanonizeSource() {}

	private void processFiles(final File dir,
		final String outDir,
		final boolean createDirectory) {
		if (dir != null) {
			File[] files;
			String input = dir.getAbsolutePath().replace('\\','/');
			if (!input.endsWith("/")) {
				input += '/';
			}
			input += _mask;
			files = getWildCardFiles(input);
			if (files != null) {
				for (File f: files) {
					processFile(f, outDir, createDirectory);
				}
			}
			String s;
			if (outDir != null) {
				s = outDir + dir.getName() + File.separatorChar;
			} else {
				s = null;
			}
			files = dir.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						processFiles(files[i], s, true);
					}
				}
			}
		}
	}

	/** Read and canonize all lines form the file. The result of the file is
	 * in the StringBuffer <tt>_sb</tt>.
	 * @param file The file with the source.
	 * @return <tt>true</tt> if an modification of the source was processed.
	 * @throws IOException if an error occurs.
	 */
	private boolean readLines(final File file) throws IOException {
		_lastCommentStart = -1;
		_lastCommentEnd = -1;
		_lastCommentLine = -1;
		_firstCommentStart = -1;
		_firstCommentEnd = -1;
		_linePos = -1;
		BufferedReader in = _charset == null
			? new BufferedReader(new InputStreamReader(
				new FileInputStream(file)))
			: new BufferedReader(new InputStreamReader(
				new FileInputStream(file), _charset));
		String line;
		boolean modified = false;
		while ((line = in.readLine()) != null) {
			//cut final white spaces
			int len = line.length();
			int i = len;
			int k;
			while((len > 0)
				&& line.charAt(len - 1) <= ' ') {
				len--;
			}
			_linePos = _sb.length();
			if (len == 0) {
				if (_linePos == 0) { //ignore leading empty lines
					modified = true;
				} else {
					modified |= i != len;
					if (_genCR) {
						_sb.append('\r'); //add CR
					}
					_sb.append('\n'); //add LF
				}
				continue;
			}
			if (_spacesToTab) { //makeTabsFromSpaces
				//replace leading spaces by tabs
				for (i = 0, k = 0; i < len; i++ ) {
					char ch = line.charAt(i);
					if (ch == '\t') {
						k = (((k + _indentSize - 1)/_indentSize) + 1)
							*_indentSize;
					} else if (ch == ' ') {
						k++;
					} else {
						break;
					}
				}
				if (i < len) { //not empty line
					for (int j = 0; j < (k / _indentSize); j++) {
						_sb.append('\t');
					}
					if ((k = k % _indentSize) > 0) {
						_sb.append(_indentSpaces.substring(0, k));
					}
					_sb.append(line.substring(i,len));
				}
			} else { //makeSpacesFromTabs
				//replace leading tabs by spaces
				for (i = 0, k = 0; i < len; i++ ) {
					char ch = line.charAt(i);
					if (ch == '\t') {
						k = (((k+_oldIndent-1)/_oldIndent)+1)*_oldIndent;
					 } else if (ch == ' ') {
						k++;
					 } else {
						break;
					 }
				}
				if (i < len) { //not empty line
					for (int j = 0; j < (k / _oldIndent); j++) {
						_sb.append(_indentSpaces);
					}
					if ((k = k % _oldIndent) > 0) {
						_sb.append(_indentSpaces.substring(0, k));
					}
					_sb.append(line.substring(i,len));
				}
			}
			modified |= !line.equals(_sb.substring(_linePos));
			if (_lastCommentStart >= 0) {
				if (line.indexOf("*/") >= 0) {
					if (line.endsWith("*/")) { //only comment lines blocks
						_lastCommentLine = _linePos;
						_lastCommentEnd = _sb.length();
						if (_firstCommentEnd == -1) {
							_firstCommentEnd = _lastCommentEnd;
						}
					} else {
						if (_firstCommentStart == _lastCommentStart) {
							_firstCommentStart = -1;
						}
						_lastCommentStart = -1;
						_lastCommentEnd = -1;
						_lastCommentLine = -1;
					}
				}
			}
			if (line.trim().startsWith("/*")) {
				if (line.indexOf("*/") < 0) { //only more lines blocks
					_lastCommentStart = _linePos;
					_lastCommentLine = -1;
					_lastCommentEnd = -1;
					if (_firstCommentStart == -1) {
						_firstCommentStart = _linePos;
					}
				}
			}
			if (_genCR) {
				_sb.append('\r'); //add CR
			}
			_sb.append('\n'); //add LF
		}
		in.close();
		return modified;
	}

	private void processFile(final File fi,
		final String outDir,
		final boolean createDirectory) {
		_errors = 0;
		if (!fi.canRead()) {
			error("Can't read file " + fi.getAbsolutePath());
			return;
		}
		if (fi.length() >= Integer.MAX_VALUE) {
			error("Too large file " + fi.getAbsolutePath());
			return;
		}
		_sb = new StringBuffer((int)fi.length());
		boolean modified;
		try {
			if (!fi.exists()) {
				return;
			}
			if (!fi.canRead()) {
				throw new Exception("Can't access file");
			}
			modified = readLines(fi);
		} catch (Exception ex) {
			error("Can't read input file: " + fi.getAbsolutePath()
				+ "\n" + ex);
			return;
		}
		// remove trailing blanks
		int len0 = _sb.length();
		int len = len0;
		char c;
		while (len > 0 && (
			(c = _sb.charAt(--len)) == '\n'
			|| c == ' ' || c == '\t' || c == '\r' || c == '\f')) {
			_sb.deleteCharAt(len);
		}
		if (len0 - 2 > len) {
			_sb.delete(len + 1, len0);
			modified = true;
		}
		//prepare file name and path names for modifications
		String pathname;
		if (outDir == null) {
			pathname = fi.getAbsolutePath();
		} else {
			pathname = new File(outDir).getAbsolutePath() + fi.getName();
		}
		pathname = pathname.replace('\\', '/');
		//NOTE tail MUST be processed first!
		int ndx = pathname.lastIndexOf('/');
		String fname;
		if (ndx >= 0) {
			fname = pathname.substring(ndx + 1);
		} else {
			fname = pathname;
		}
		if (_tail != null) {
			ndx = 0;
			int i;
			String s = _tail;
			while ((i = s.indexOf("&{FILENAME}", ndx)) > 0) {
				s = s.substring(ndx, i)
					+ fname + s.substring(i + 11);
				ndx = i + fname.length();
			}
			ndx = 0;
			while ((i = s.indexOf("&{PATHNAME}", ndx)) > 0) {
				s = s.substring(ndx, i)
					+ pathname + s.substring(i + 11);
				ndx = i + pathname.length();
			}
			if (_linePos >= 0
				&& _lastCommentLine == _linePos
				&& _lastCommentStart <= _linePos) {
				if (!_tailKeep) {
					_sb.delete(_lastCommentStart, _sb.length());
					//remove final empty lines
					while((i = _sb.length()) > 0
						&& _sb.charAt(--i) <= ' ') {
						_sb.deleteCharAt(i);
					}
					if (s.length() > 0) {
						_sb.append(s);
					}
					modified = true;
				}
			} else {
				if (s.length() > 0) {
					_sb.append(s);
					modified = true;
				}
			}
			if (_firstCommentStart == _lastCommentStart) {
				_firstCommentStart = -1;
			}
		}
		// remove all final white spaces
		while(_sb.length() > 0
			&& _sb.charAt(_sb.length() - 1) <= ' ') {
			_sb.deleteCharAt(_sb.length() - 1);
			modified = true;
		}
		if (_header != null) {
			ndx = 0;
			int i;
			String s = _header;
			while ((i = s.indexOf("&{FILENAME}", ndx)) > 0) {
				s = s.substring(ndx, i)
					+ fname + s.substring(i + 11);
				ndx = i + fname.length();
			}
			if (_firstCommentStart == 0) {
				if (!_headerKeep) {
					_sb.delete(_firstCommentStart, _firstCommentEnd);
					//remove leading empty lines
					while(_sb.length() > 0 && _sb.charAt(0) == '\n') {
						_sb.deleteCharAt(0);
						modified = true;
					}
					if (s.length() != 0) {
						_sb.insert(0,s);
						modified = true;
					}
				}
			} else {
				if (s.length() > 0) {
					//remove leading empty lines
					while(_sb.length() > 0 && _sb.charAt(0) == '\n') {
						_sb.deleteCharAt(0);
					}
					_sb.insert(0,s);
					modified = true;
				}
			}
		}
		//remove leading empty lines
		while(_sb.length() > 0 && _sb.charAt(0) == '\n') {
			_sb.deleteCharAt(0);
			modified = true;
		}
		if (_errors > 0) {
			_out.flush();
			_err.println(
				_errors +  " error(s) detected in " + fi.getAbsolutePath());
			return;
		}
		_processedCount++;
		if (modified) {
			// write modified file
			File fo;
			File renamed = null;
			if (outDir == null) {
				fo = new File(fi.getAbsolutePath());
			} else {
				fo = new File(outDir);
				if (!fo.exists()) {
					if (!createDirectory) {
						error("Output directory doesn't exist: " + outDir);
						_sb = null; //let's gc do the job
						return;
					} else {
						if (!fo.mkdirs()) {
							error("Can't create output directory: "+outDir);
							_sb = null; //let's gc do the job
							return;
						}
					}
				}
				fo = new File(outDir + fi.getName());
			}
			try {
				if (fi.equals(fo)) {
					renamed = new File(fi.getAbsoluteFile() + ".bak");
					if (renamed.exists()) {
						if (!renamed.delete()) {
							error("Can't delete file "
								+ renamed.getAbsoluteFile());
						}
					}
					if (!fi.renameTo(renamed)) {
						error("Can't rename input file "+fi.getAbsoluteFile());
						return;
					}
				}
				Writer fw = _charset == null ? new FileWriter(fo)
					: new OutputStreamWriter(new FileOutputStream(fo),_charset);
				fw.write(_sb.toString());
				fw.close();
				_sb = null; //let's gc do the job
				_modifyCount++;
				if (renamed != null) {
					if (!renamed.delete()) {
						error("Can't delete file " + renamed.getAbsoluteFile());
					}
				}
				_err.flush();
				if (_verbose) {
					if (fi.getAbsolutePath().equals(fo.getAbsolutePath())) {
						_out.println("Modified file: " + fi.getAbsolutePath());
					} else {
						_out.println("Input file:  " + fi.getAbsolutePath());
						_out.println("Output file: " + fo.getAbsolutePath());
					}
					_out.flush();
				}
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
				error("Can't write to output file: " + fo.getAbsolutePath());
			}
		}
	}

	/** Print error message.
	 * @param msg The default text of message.
	 */
	private void error(final String msg) {
		_out.flush();
		_err.println(msg);
		_err.flush();
		_errors++;
	}

	/**
	 * Get array of existing files represented by given argument. The argument
	 * can either represent one concrete file or it can represent a set of files
	 * with wildcards '*' and/or '?'.
	 * @param wildName The file name (wildcards are accepted) .
	 * @return The array of existing files represented by argument.
	 */
	private File[] getWildCardFiles(final String wildName) {
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
			try {
				File f = new File(".");
				if (f.isDirectory()) {
					dir = new File(f.getCanonicalPath());
				} else {
					throw new RuntimeException("Actual path isn't accessable");
				}
			} catch (Exception ex) {
				throw new RuntimeException("Actual path isn't accessable");
			}

		}
		return dir.listFiles(new NameWildCardFilter(wn, false));
	}

	private static String updateInfo(final String info,
		final Date datetime,
		final String user,
		final String name,
		final String charset) throws Exception {
		if (info == null) {
			return null; //leave without change
		}
		if ("nul".equalsIgnoreCase(info)) {
			return ""; //remove (replace by empty)
		}
		File fi = new File(info);
		if (!fi.exists()) {
			throw new Exception(
				"Input file doesn't exist: " + fi.getAbsolutePath());
		}
		if (!fi.canRead()) {
			throw new Exception("Can't access file: " +  fi.getAbsolutePath());
		}
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = charset != null
				? new BufferedReader(new InputStreamReader(
					new FileInputStream(fi), charset))
				: new BufferedReader(new FileReader(fi));
			String line;
			while ((line = in.readLine()) != null) { //process all lines
				//cut final white spaces
				int len = line.length();
				//remove final white spaces.
				while((len > 0)
					&& line.charAt(len - 1) <= ' ') {
					len--;
				}
				int linePos = sb.length();
				if (len == 0) {
					if (linePos == 0) { //ignore leading empty lines
					} else {
						sb.append('\n'); //add empty line
					}
					continue;
				}
				sb.append(line.substring(0, len));
				sb.append('\n'); //add new line
			}
		} catch (Exception ex) {
			throw new Exception("Can't read "+name+" template file:\n"+info);
		}
		String result = sb.toString().trim();
		if (result.length() == 0) {
			return "";
		}
		if (!result.startsWith("/*") || !result.endsWith("*/")) {
			throw new Exception("Error in " + name + " file");
		}
		try {
			int i;
			// replace &{DATE} parameters
			int ndx = 0;
			while ((i = result.indexOf("&{DATE", ndx)) > 0) {
				int j = result.indexOf("}", i + 6);
				if (j < 0) {
					throw new Exception("?");
				}
				String mask = result.substring(i + 6, j).trim();
				String date;
				if (mask.length() == 0) {
					date = DateFormat.getDateTimeInstance().format(datetime);
				} else {
					date =
						new SimpleDateFormat(mask).format(datetime);
				}
				result = result.substring(ndx, i)
					+ date + result.substring(j + 1);
				ndx = i + date.length();
			}
			// replace &{ISODATE} parameters
			ndx = 0;
			while ((i = result.indexOf("&{ISODATE}", ndx)) > 0) {
				String date = new SimpleDateFormat("yyyy-MM-DD'T'HH:mm:ssZ")
					.format(datetime);
				result = result.substring(ndx, i)
					+ date + result.substring(i + 10);
				ndx = i + date.length();
			}
			// replace &{USER} parameters
			ndx = 0;
			while ((i = result.indexOf("&{USER}", ndx)) > 0) {
				result = result.substring(ndx, i)
					+ user + result.substring(i + 7);
				ndx = i + user.length();
			}
			return result;
		} catch (Exception ex) {
			throw new Exception("Error in " + name + " file");
		}
	}

	/** Call CanonizeSource from program.
	 * @param input The string with specification of the input file(s) or the
	 * directory.
	 * @param outDir The directory where put the changed files. If this argument
	 * is <tt>null</tt> the changed file will replace the input file.
	 * @param dirTree If the value of this argument is <tt>true</tt> the
	 * program will scan also subdirectories..
	 * @param out The printstream where will be printed output messages.
	 * @param err The printstream where will be printed input messages.
	 * @param verbose If the value of this argument is <tt>true</tt> the
	 * @param genCR If the value of this argument is <tt>true</tt> the
	 * program will generate the couple of CR LF as line separators. Otherwise
	 * only LF characters are generated.
	 * @param spacesToTab If the value of this argument is <tt>true</tt>
	 * the program will convert all leading spaces to tabs.
	 * @param indentSize The number of position for indenting used in original
	 * source.
	 * @param newIndent Tne new indenting parameter if it should be changed (or
	 * <tt>0</tt>).
	 * @param header The file name of the header template.
	 * @param headerKeep if true the existing header will not be changed.
	 * @param tail The file name of tail info template.
	 * @param tailKeep if true the existing tail info will not be changed.
	 * @param charset name of character set of data. If it is null then the
	 * default system encoding is used.
	 * @return string with error message if the program can't work due to
	 * parameter error or return <tt>null</tt>.
	 */
	public static String canonize(final String input,
		final String outDir,
		final boolean dirTree,
		final PrintStream out,
		final PrintStream err,
		final boolean verbose,
		final boolean genCR,
		final boolean spacesToTab,
		final int indentSize,
		final int newIndent,
		final String header,
		final boolean headerKeep,
		final String tail,
		final boolean tailKeep,
		final String charset) {
		CanonizeSource cs = new CanonizeSource();
		cs._charset = charset;
		File f;
		String s = outDir;
		if (s == null || (s = s.trim()).length() == 0) {
			s = null;
		} else {
			f = new File(s);
			if (!f.exists() || !f.isDirectory()) {
				return "Incorrect output directory.";
			}
			s = f.getAbsolutePath() + File.separatorChar;
		}
		Date datetime = new Date();
		String user = System.getProperties().getProperty("user.name");
		try {
			//read header template file
			cs._header = updateInfo(header, datetime, user, "header", charset);
			if (cs._header != null && cs._header.length() > 0) {
				if (genCR) {
					cs._header += "\r\n\r\n";
				} else  {
					cs._header += "\n\n";
				}
			}
			cs._headerKeep = headerKeep;
			//read tail template file
			cs._tail = updateInfo(tail, datetime, user, "tail", charset);
			if (cs._tail != null && cs._tail.length() > 0) {
				cs._tail = (genCR ? "\r\n\r\n" :"\n\n") + cs._tail;
			}
			cs._tailKeep = tailKeep;
		} catch (Exception ex) {
			return ex.toString();
		}
		cs._out = out;
		cs._err = err;
		cs._processedCount = cs._modifyCount = 0;
		cs._verbose = verbose;
		cs._spacesToTab = spacesToTab;
		if (indentSize < 0 || indentSize >= 20) {
			return "Incorrect value of indenting size: " + indentSize;
		}
		if (newIndent <= 0) {
			cs._oldIndent = cs._indentSize = indentSize;
		} else {
			if (newIndent < 0 || newIndent >= 20) {
				return "Incorrect value of new indenting size: " + indentSize;
			}
			cs._oldIndent = indentSize;
			cs._indentSize = newIndent;
		}
		cs._indentSpaces = "";
		for (int i = 0; i < cs._indentSize; i++) {
			cs._indentSpaces += " ";
		}
		String inp = input.replace('\\', '/');
		cs._genCR = genCR;
		File dir = null;
		if (dirTree) {
			int i = inp.lastIndexOf('/');
			if (i >= 0) {
				cs._mask = inp.substring(i + 1);
				dir = new File(inp.substring(0,i));
			} else {
				cs._mask = inp;
				try {
					f = new File(".");
					if (f.isDirectory()) {
						dir = f;
					}
				} catch (Exception ex) {}
			}
		}
		if (dir != null) {
			cs.processFiles(dir, s, false);
		} else {
			File[] files = cs.getWildCardFiles(inp);
			if (files == null || files.length == 0) {
				return null;
			}
			for (int i = 0; i < files.length; i++) {
				cs.processFile(files[i], s, false);
			}
		}
		if (err != null) {
			err.flush();
		}
		if (verbose && out != null && cs._processedCount > 0) {
			out.println("Inspected " + cs._processedCount
				+ " file(s), changed " + cs._modifyCount + ".");
			out.flush();
		}
		return null;
	}

	/** Call CanonizeSource from program.
	 * @param args Array of strings with command line parameters (see
	 * {@link CanonizeSource}).
	 * @param out The printstream where will be printed output messages.
	 * @param err The printstream where will be printed input messages.
	 * @return string with error message if the program can't work due to
	 * parameter error or return <tt>null</tt>.
	 */
	public static String canonize(final String[] args,
		final PrintStream out,
		final PrintStream err) {
		if (args.length < 1) {
			return "Required parameters are missing.";
		}
		String input = null;
		String outDir = null;
		boolean dirTree = false;
		boolean verbose = false;
		String header = null;
		boolean headerKeep = false;
		String tail = null;
		boolean tailKeep = false;
		int spacesToTab = 0;
		int replacement = 4;
		int newIndent = -1;
		boolean genCR = false;
		String charset = null;
		for (int i = 0; i < args.length; i++) {
			if ("-r".equals(args[i])) {
				if (dirTree) {
					return "'-r' is redefined (directory tree switch).";
				}
				dirTree = true;
			} else if (args[i].equals("-encoding")) {
				if (charset != null) {
					return "'-encoding' is redefined.";
				}
				charset = args[++i];
				if (!Charset.isSupported(charset)) {
					return "-encoding \"" + charset + "\" is incorrect.";
				}
			} else if (args[i].equals("-n")) {
				if (newIndent != -1) {
					return "'-n' redefined (new indentation).";
				}
				String s = "";
				if (args[i].length() > 2) {
					s = args[i].substring(2);
				} else {
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						s = args[++i];
					}
				}
				if (s.length() > 0) {
					try {
						newIndent = Integer.parseInt(s);
						if (newIndent < 1 || newIndent > 40) {
							return "Incorrect value of switch '-t' or '-s'";
						}
					} catch (NumberFormatException ex) {
						return "after the switch '-n' should be number.";
					}
				} else {
					return "after the switch '-n' is required a number.";
				}
			} else if (args[i].equals("-s") || args[i].equals("-t")) {
				if (args[i].equals("-s")) {
					if (spacesToTab == 1) {
						return "'-s' is redefined.";
					} else if (spacesToTab == 2) {
						return "both '-t' and '-s' can't be specified.";
					}
					spacesToTab = 1;
				} else {
					if (spacesToTab == 2) {
						return "'-t' is redefined (set tabs).";
					} else if (spacesToTab == 1) {
						return "both '-t' and '-s' can't be specified.";
					}
					spacesToTab = 2;
				}
				String s = "";
				if (args[i].length() > 2) {
					s = args[i].substring(2);
				} else {
					if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
						s = args[++i];
					}
				}
				if (s.length() > 0) {
					try {
						replacement = Integer.parseInt(s);
					} catch (NumberFormatException ex) {
						return
							"after the switch '-t' or '-s' should be number.";
					}
				}
			} else if (args[i].equals("-v")) {
				if (verbose) {
					return "'-v' is redefined (verbose).";
				}
				verbose = true;
			} else if (args[i].equals("-cr")) {
				if (genCR) {
					return "'-cr' is redefined (generate CR and LF).";
				}
				genCR = true;
			} else if (args[i].startsWith("-o")) {
				if (outDir != null) {
					return "-o is redefined (output directrory).";
				}
				String s;
				if (args[i].length() > 2) {
					s = args[i].substring(2);
				} else {
					if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
						return "Output directrory parameter is missing.";
					} else {
						s = args[++i];
					}
				}
				outDir = s;
			} else if (args[i].startsWith("-i")) {
				if (input != null) {
					return "-i is redefined (input data).";
				}
				if (args[i].length() > 2) {
					input = args[i].substring(2);
				} else {
					if (i + 1 > args.length || args[i + 1].startsWith("-")) {
						return "Incorrect input file parameter.";
					}
					input = args[++i];
				}
			} else if (args[i].equals("-c") || args[i].equals("-cc")) {
				if (header != null) {
					return "-c is redefined (header file).";
				}
				headerKeep = args[i].equals("-cc");
				String s;
				if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					s = args[++i];
				} else {
					return "Missing specification of header information file("
						+ args[i] + ")";
				}
				if (s.length() > 0) {
					if ("nul".equalsIgnoreCase(s)) {
						header = "nul";
					} else {
						File f = new File(s);
						if (!f.exists() || !f.canRead()) {
							return "The copyrignt file is not accessible";
						}
						header = s;
					}
				} else {
					return "Incorrect header file name";
				}
			} else if (args[i].equals("-e") || args[i].equals("-ee")) {
				if (tail != null) {
					return "-e is redefined (tail file).";
				}
				String s;
				tailKeep = args[i].equals("-ee");
				if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					s = args[++i];
				} else {
					return "Missing specification of tail information file ("
						+ args[i] + ")";
				}
				if (s.length() > 0) {
					if ("nul".equalsIgnoreCase(s)) {
						tail = "nul";
					} else {
						File f = new File(s);
						if (!f.exists() || !f.canRead()) {
							return "The tail file is not accessible";
						}
						tail = s;
					}
				} else {
					return "Incorrect tail file name";
				}
			} else if (args[i].startsWith("-h")) {
				cancel(0, null); // help
			} else {
				return "Incorrect parameter: " + args[i];
			}
		}
		if (input == null || input.length() == 0) {
			return "Input not specified";
		}
		File f = new File(input);
		if (f.isDirectory()) {
			return "Input file can't be directory.";
		}

		return canonize(input,
			outDir,
			dirTree,
			out,
			err,
			verbose,
			genCR,
			spacesToTab == 2,
			replacement,
			newIndent,
			header,
			headerKeep,
			tail,
			tailKeep,
			charset);
	}

	/** Canonize sources according to arguments. and print results.
	 * @param sources Source file(s) (may contain wildcards).
	 * @param dirTree if <tt>true</tt> the child directories are processed too.
	 * @param tabs if true, leading spaces are replaced by tabs, if false
	 * the leading tabs are replaced by spaces.
	 * @param n the number of spaces per tab. If this argument is -1 then
	 * no modifications of leading spaces is done.
	 * @param header the name of file containing string with header ("copyright")
	 * information which will be inserted or replaced as the top of the source.
	 * If the file is the empty (i.e. it has the zero length) the header
	 * information is deleted. If this argument is <tt>null</tt> no header
	 * information is processed. Note the information is considered in the form
	 * of Java or "C" comment.
	 * @param tail the name of file containing string with tail ("log")
	 * information which will be added to the end of source or it will replace
	 * the existing information. If the file is the empty (i.e. it has zero
	 * length) then the existing tail will be information is deleted.
	 * If this argument is <tt>null</tt> no tail information is processed.
	 * Note the information is considered in the form of Java or "C" comment.
	 * @param charset name of character set of data. If it is null then the
	 * default system encoding is used.
	 */
	public static void canonize(final String sources,
		final boolean dirTree,
		final boolean tabs,
		final int n,
		final String header,
		final String tail,
		final String charset) {
		try {
			int len = 3;
			if (dirTree) {
				len++;
			}
			if (n >= 0) {
				len += 2;
			}
			if (header != null) {
				len += 2;
			}
			if (tail != null) {
				len += 2;
			}
			if (charset != null && !charset.trim().isEmpty()) {
				len += 2;
			}
			String[] myArgs = new String[len];
			myArgs[0] = "-i";
			myArgs[1] = sources;
			myArgs[2] = "-v";
			int ndx = 3;
			if (dirTree) {
				myArgs[ndx++] = "-r";
			}
			if (n >= 0) {
				if (tabs) {
					myArgs[ndx++] = "-t";
					myArgs[ndx++] = String.valueOf(n);
				} else {
					myArgs[ndx++] = "-s";
					myArgs[ndx++] = String.valueOf(n);
				}
			}
			if (header != null) {
				myArgs[ndx++] = "-c";
				if (header.length() == 0) {
					myArgs[ndx++] = "nul";
				} else {
					myArgs[ndx++] = header;
				}
			}
			if (tail != null) {
				myArgs[ndx++] = "-e";
				if (tail.length() == 0) {
					myArgs[ndx++] = "nul";
				} else {
					myArgs[ndx++] = tail;
				}
			}
			if (charset != null && !charset.trim().isEmpty()) {
				myArgs[ndx++] = "-encoding";
				myArgs[ndx++] = charset;
			}
			String errMsg = canonize(myArgs, System.out, System.err);
			if (errMsg != null) {
				System.err.println(errMsg);
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}

	/** Print message and cancel program with exit code.
	 * @param exitCode The exit code.
	 * @param msg The default text of message.
	 */
	private static void cancel(final int exitCode, final String msg) {
		System.out.flush();
		System.err.flush();
		System.err.println((msg == null ?
		"CanonizeSource. Copyright 2004 Syntea Software Group.\n"
+"All lines of the source data are checked for leading and trailing white spaces.\n"
+"Trailing spaces and tabs are removed and leading spaces are replaced by\n"
+"tabs or by spaces according to the value of switches -t or -s.If the\n"
+"switch -n n is defined it is used as number of indenting positions for\n"
+"tabs. If -c switch is set on then the header (copyright) information is\n"
+"inserted (or the old one is replaced) as a comment on the top of source data.\n"
+"If -e switch is set on then the final information is added to the end of source\n"
+"data. If the switch is -cc or -ee the existing information is not replaced.\n"
		: "Error: " + msg + "\n"
		)
+"usage: -i input [-r] [-o outDir] [(-t|-s) [n]] [-n n] [-h] [-c hdr] [-e tail]\n"
+"where\n"
+"-i input The input may be specified as a file or as the group of files\n"
+"   specified by wildcards '*' or '?'. The parameter is obligatory.\n"
+"-r process directory tree. The parameter is optional.\n"
+"-o outDir The directory where the output files are stored. The switch\n"
+"   is optional. If it is missing the source files are overwritten.\n"
+"-t [n] n is the number of spaces which are replaced by tab key codes.\n"
+"   The switch is optional. If n is missing the value '4' is set as default.\n"
+"-s [n] n is the number of spaces replacing tab key codes. The parameter\n"
+"   is optional. If it is missing the value '4' is set as default. \n"
+"-n n n is the new number of spaces replacing tab key. The parameter\n"
+"   is optional. If it is missing the value or -t or -s is set as default.\n"
+"-cr lines are separated by the couple of CR LF. The parameter is optional.\n"
+"-c hdr the file with the header information (existing one will be replaced)\n"
+"-cc hdr the file with the header information (existing one is not replaced)\n"
+"-e end the file with the final information (existing one will be replaced)\n"
+"-ee end the file with the final information (existing one is not replaced)\n"
+"-encoding charset name of character setof data, if it is not specified then\n"
+"   the system character set is used. The parameter is optional.\n"
+"-h: displays the help text.\n"
+"The switches -t and -s are exclusive. If none of them is specified the\n"
+"the default value is set to '-s 4'.\n"
+"The switches -c, -cc and -e, -ee are exclusive.\n"
+"\n"
+"Both header an final information may contain following tags which will\n"
+"be replaced by adequate information:\n"
+"&{USER} ........ user login name\n"
+"&{DATE} ........ actual date and time in localized Java format\n"
+"&{ISODATE} ..... actual date and time in ISO 8601 format\n"
+"&{DATE mask} ... actual date and/or time in format given by Java mask\n"
+"&{FILENAME} .... The name of file\n"
+"&{PATHNAME} .... The absolute path and name of file\n");
		System.err.flush();
		if (exitCode >= 0) {
			System.exit(exitCode);
		}
	}

	/** Call CanonizeSource from command line.
	 * @param args Array of strings with command line parameters (see
	 * {@link CanonizeSource}).
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 1 && "-h".equals(args[0])) {
			cancel(0, null);
		} else {
			String s = canonize(args, System.out, System.err);
			if (s != null) {
				cancel(1,  s);
			}
		}
	}
}