package buildtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;

/** Preprocessor for Java source files.
 * <p>Preprocessor commands separates source blocks by special Java comments.
 * Each preprocessor command must start with the sequence "/*#" followed by
 * the name of command and the whole command must be specified on a separated
 * line. The end of command must be either '*&#47;' or '*#/'. Each command block
 * must start with the "/*#if" command and it must be closed with the
 * "/*#end*&#47;" command. </p>
 * <p>There are following preprocessor commands:</p>
 * <ul>
 *  <li><tt>/*#if conditional expression *&#47; .......</tt>
 * start of the conditional section.</li>
 *  <li><tt>/*#elseif conditional expression *&#47; ...</tt>
 * conditioned alternative section (part of "if" command).</li>
 *  <li><tt>/*#else*&#47; .................</tt> optional alternative section
 * (part of "if" command).</li>
 *  <li><tt>/*#end*&#47; ..................</tt> end of section.</li>
 * <li><tt>/*#set switch_list*&#47; ......</tt> set switches separated by "," to
 * ON value.</li>
 * <li><tt>/*#unset switch_list*&#47; ....</tt> set switches separated by "," to
 * OFF value.</li>
 * </ul>
 * <p>
 * If command block of lines starts with 'if' command and ends with the
 * 'end' command. Inside in the command block there may be optionally specified
 * several sections 'elseif' and optionally there may be specified the an "else"
 * section (it must be the the last one section of the command block. Nested
 * "if" command blocks are permitted.
 * </p>
 * <p>Preprocessor works so that it modifies the character sequences ending
 * command lines from '*&#47;' to '*#/' and/or from '*#/' to '*&#47;' according
 * to result of evaluation of the boolean expression given by values of
 * switches specified from outside environment. This causes that some sections
 * of Java source code will either as comments or the others will be
 * "uncommented".
 * </p>
 * <p>The conditional expression contains a list of swith names bounded by
 * logical operators '&amp;' and '|'. Usage of brackets in the expression is
 * possible. Switch name must be composed from letters, digits, '.', '-' or '_'.
 * </p>
 * Examples of switch expressions:
 * <pre><code><b>
 *  Expression                  Explanation</b>
 *  Debug                       true if swith 'Debug' is set
 *  !BUILD                      true if swith 'BUILD' is not set
 *  Java_1.1 | Java_1.2         true if one of switches 'Java_1.1' or
 *                              'Java_1.2' is set
 *  Extended &amp; (J1.4 | J1.5)    true if swith 'Extended' and one of 'J1.4'
 *                              or 'J1.5' is set
 * </code></pre>
 * <p> The <tt>set</tt> command enables to set the value of switches on. The
 * names of switches in the list are separated by commas. The command MUST be
 * placed in the <tt>if ... end</tt> block and it is executed only if the block
 * is active.</p>
 * <p> The <tt>unset</tt> command enables to set the value of switches off.
 * The names of switches in the list are separated by commas. The command MUST
 * be placed in the <tt>if ... end</tt> block and it is executed only if the
 * block is active.</p>
 * <p>
 * Preprocessor may be invoked from command line with following parameters:</p>
 * <p>
 * <tt>-i input [-o outputDirectory] [-s switch[,switch..]] [-r] [-h]</tt>
 * </p>
 * <p>where:</p>
 * <ul>
 * <li> -i input Input may be specification of the file with input source or the
 * directory. If the parameter is not directory, also wildcards ('*' or '?' may
 * be used to specify group of files. If this parameter specified a directory
 * only files with the extension '.java' are processed. The parameter
 *  is obligatory.</li>
 * <li> -r dirTree input directory. The parameter is optional and forces to
 * process all subdirectories of the directory where process started.</li>
 * <li> -s switch[,switch..]: the comma separated list of switch names. Each
 * switch name is composed from letters, digits, '.' or '_'s. If switch should
 * be set to false it can be either prefixed with '!' or not specified.
 * The parameter is optional.</li>
 * <li> -o outputDirectory: The directory where output files are stored. The
 * parameter is optional. If it is not specified the source file is overwritten.
 * <li> -h The parameter displays help text.</li>
 * </ul>
 * The following example is result after application of the switch Boss"
 * by the command:
 * <pre><tt>
 *    java buildtools.JavaPreprocessor -i Example.java -s Boss</tt>
 * The modified section of the source after modification looks like:<tt>
 *          ....
 *          /*#if (Mary &amp; John) | (Alice &amp; Bob)*#/;
 *               System.out.println("Hello, friends!");
 *          /*#elseif Boss*&#47;
 *               System.out.println("Hi, Bill!");
 *          /*#else*#/;
 *               System.out.println("I do not know you!");
 *          /*#end*&#47;
 *          .... </tt></pre>
 * Note that the text printed will be "Hi, Bill!"; the other commands
 * are set as comments.
 * <p> JavaPreprocessor may be executed from command line with following
 * parameters:</p>
 * <p>[-h] [-r] [-t] [-v] -i input [-o output] [-c charset] [-s switches]</p>
 * <p>where</p>
 * <p>-r dirTree process directory tree. The parameter is optional.</p>
 * <p>-t delete trailing spaces. The parameter is optional.</p>
 * <p>-v make verbose output. The parameter is optional.</p>
 * <p>-s switches: The list of switch names. Each switch name is composed from
 *   letters, digits, '.' or '_'s. The switch can be either prefixed with
 *   '!' or not specified. The parameter is optional.</p>
 * <p>-i input: The file name list of the directories with the Java packages.
 *   Each directory is supposed to be the root of package. Only the files with
 *   the extension '.java' are processed. The parameter is obligatory.</p>
 * <p>-o output: The directory where the output files are stored. The
 *   parameter is optional. If it is missing the source files are replaced.</p>
 * <p>-c charset: name of character table, if it is not specified then the
 *   default system character set is used. The parameter is optional.</p>
 * <p>-h display the help text.</p>
 *
 * @author  Vaclav Trojan
 */
public class JavaPreprocessor {

	private final static int MAX_STACK = 256; //max. stack for expressions

	private static final int END_COMMAND = 1;
	private static final int ELSEIF_COMMAND = 2;
	private static final int ELSE_COMMAND = 3;
	private static final int IF_COMMAND = 4;
//	private static final int COMMENT_LINE = 5;
	private static final int SRC_LINE = 6;
	private static final int SET_COMMAND = 7;
	private static final int UNSET_COMMAND = 8;
	private static final int EOF = 9;

	private BufferedReader _in;
	private String _charset;
	private MyStringList _keys;
	private String _line;
	private int _lineNumber;
	private int _pos;
	private int _endPos;

	private boolean[] _exprStack;
	private int _sp;

	private int _errors;
	private StringBuffer _sb;
	private boolean _modified;
	private int _count;
	private int _modifyCount;
	private int _processedCount;
	private PrintStream _out;
	private PrintStream _err;
	private boolean _verbose;
	private boolean _extract; //not used
	private boolean _cutTrailingSpaces;

	/** Creates a new instance of JavaPreprocessor - used internally for
	 * parsing of switches. */
	private JavaPreprocessor(final String line) {
		_lineNumber = 0;
		_pos = 0;
		_line = line;
		_endPos = line.length();
		_verbose = false;
		_extract = false;
		_cutTrailingSpaces = false;
	}

	/** Creates a new instance of JavaPreprocessor */
	private JavaPreprocessor() {}

	/** Create new instance of JavaPreprocessor.
	 * @param charset character set name.
	 * @param keys array of strings with key names.
	 * @param out PrintStream where will be printed output messages.
	 * @param err PrintStream where will be printed error messages.
	 */
	public JavaPreprocessor(final String charset,
		final String[] keys,
		final PrintStream out,
		final PrintStream err) {
		_errors = 0;
		_exprStack = new boolean[MAX_STACK];
		_sp = -1;
		_line = null;
		_lineNumber = 0;
		_modified = false;
		_out = out == null ? System.out : out;
		_err = err == null ? System.err : err;
		_sb = null;

		setKeys(keys);
		setCharset(charset);
		setVerbose(true);
		setExtract(false);
		setCutTrailingSpaces(true);
	}

	/** Process input stream and create modified result.
	 * @param is input stream.
	 * @param fname name of input file (information for messages).
	 * @return <tt>true</tt> if and only if the modified result was created.
	 */
	public boolean processStream(final InputStream is, final String fname) {
		_errors = 0;
		try {
			BufferedReader br = new BufferedReader(
				_charset == null || _charset.length() == 0 ?
					new InputStreamReader(is) :
					new InputStreamReader(is, _charset));
			processFile(br);
		} catch (Exception ex) {
			error("Can't read input file " + fname, false);
			_sb = null;
			return false;
		}
		if (_errors > 0) {
			_err.println(_errors +  " error(s) detected in " + fname);
			_sb = null;
			return false;
		}
		return _modified;
	}

	/** Get StringBuffer with result.
	 * @return the StringBuffer with result.
	 */
	public StringBuffer getResult() { return _sb; }

	/** Get number of recognized errors.
	 * @return number of recognized errors or 0.
	 */
	public int getErrorCount() { return _errors; }
	/** Return <tt>true</tt> if modified source was created.
	 * @return <tt>true</tt> if and only if modified source was created.
	 */
	public boolean isModified() { return _modified; }

	/** Get name of character set.
	 * @return the name of character set or <tt>null</tt>.
	 */
	public String getCharset() { return _charset; }

	/** Get value of verbose switch.
	 * @return value of verbose switch.
	 */
	public boolean isVerbose() { return _verbose; }

	/** Get value of cutTrailingSpaces switch.
	 * @return value of cutTrailingSpaces switch.
	 */
	public boolean isCutTrailingSpaces() { return _cutTrailingSpaces; }

	/** Set verbose output switch.
	 * @param verbose  verbose output switch.
	 */
	public final void setVerbose(boolean verbose) { _verbose = verbose; }

	/** Set extract switch.
	 * @param extract the extract switch.
	 */
	public final void setExtract(boolean extract) { _extract = extract; }

	/** Set cutTrailingSpaces switch.
	 * @param cutTrailingSpaces cutTrailingSpaces switch.
	 */
	public final void setCutTrailingSpaces(boolean cutTrailingSpaces) {
		_cutTrailingSpaces = cutTrailingSpaces;
	}

	/** Set character set name.
	 * @param charset  character set name.
	 */
	public final void setCharset(String charset) {_charset = charset; }

	/** Set values of preprocessing keys.
	 * @param keys array with values of preprocessing keys.
	 */
	public final void setKeys(String[] keys) {
		_keys = new MyStringList();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				String s = keys[i];
				if (s != null && s.length() > 0) {
					_keys.add(s);
				}
			}
		}
	}

	private void processFiles(final File[] files,
		final String outDir,
		final boolean dirTree,
		final boolean createDirectory) {
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (!f.isDirectory() && f.getName().endsWith(".java")) {
				processFile(f, outDir, createDirectory);
			}
		}
		if (dirTree) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					String s = files[i].getName();
					if (!s.startsWith(".")) {
						if (outDir != null) {
							s = outDir + s + File.separatorChar;
						} else {
							s = null;
						}
						processFiles(files[i].listFiles(), //process file list
							s, //outDir
							true, //dirTree
							true);// subdirectories can be created
					}
				}
			}
		}
	}

	private void processFile(final File fi,
		final String outDir,
		final boolean createDirectory) {
		_errors = 0;
		if (!fi.canRead()) {
			error("Can't read file " + fi.getAbsolutePath(), false);
			return;
		}
		try {
			BufferedReader br = new BufferedReader(
				_charset == null || _charset.length() == 0 ?
					new FileReader(fi) :
					new InputStreamReader(
						new FileInputStream(fi), _charset));
			processFile(br);
		} catch (Exception ex) {
			error("Can't read input file " + fi.getAbsolutePath(), false);
			_sb = null;
			return;
		}
		if (_errors > 0) {
			_out.flush();
			_err.println(_errors +
				" error(s) detected in " + fi.getAbsolutePath());
			_err.flush();
			_sb = null;
			return;
		}
		if (!_modified) {
			_sb = null;
			return;
		}
		// write modified file
		File fo;
		File renamed = null;
		if (outDir == null) {
			fo = new File(fi.getAbsolutePath());
		} else {
			fo = new File(outDir);
			if (!fo.exists()) {
				if (!createDirectory) {
					error("Output directory doesn't exist: " + outDir, false);
					_sb = null;
					return;
				} else {
					if (!fo.mkdirs()) {
						error("Can't create output directory: "+outDir, false);
						_sb = null;
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
						error("Can't delete file " + renamed.getAbsoluteFile(),
						false);
						return;
					}
				}
				if (!fi.renameTo(renamed)) {
					error("Can't rename input file "+fi.getAbsoluteFile(),
						false);
					_sb = null;
					return;
				}
			}

			Writer out = _charset == null
				? new OutputStreamWriter(new FileOutputStream(fo))
				: new OutputStreamWriter(new FileOutputStream(fo), _charset);
			out.write(_sb.toString());
			_sb = null;
			out.close();
			_modifyCount++;
			if (renamed != null) {
				if (!renamed.delete()) {
					error("Can't delete file " + renamed.getAbsoluteFile(),
						false);
					return;
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
			error("Can't write to output file: " + fo.getAbsolutePath(), false);
			_sb = null;
		}
	}

	/** Process file. */
	private void processFile(final BufferedReader br) throws IOException {
		_exprStack = new boolean[MAX_STACK];
		_sp = -1;
		_line = null;
		_lineNumber = 0;
		_sb = new StringBuffer();
		_modified = false;
		_in = br;
		boolean preproc = false;
		int command;
		while ((command = parseLine()) != EOF) {
			//process all commands until the IF command is found
			if (command == IF_COMMAND) {
				preproc = true;
				ifCommand(evaluate());
			} else if (command == SET_COMMAND || command == UNSET_COMMAND) {
				commentLineEndUncomments();
				setOrUnsetCommand(command);
			} else {
				if (command != SRC_LINE) {
					//preprocessor command without "IF".
					error("'if' command is missing.", true);
				}
				_sb.append(_line);
			}
		}
		_in.close();
		_in = null;
		_count++;
		if (preproc) {
			_processedCount++;
		}
	}

	/** Process IF command. */
	private void ifCommand(boolean ignore) throws IOException {
		boolean modify = canonizeAndChageCommand(ignore);
		int command = modifyToCommand(ignore, modify);
		boolean sectionWasGenerated = !ignore;
		int startLine = _lineNumber;
		boolean wasElse = false;
		while (command != EOF) {
			switch (command) {
				case ELSEIF_COMMAND: //elseif
					ignore = evaluate();
					if (sectionWasGenerated) {
						//A section was already generated.
						ignore = true;
					} else if (!ignore) {
						sectionWasGenerated = true;
					}
					modify = canonizeAndChageCommand(ignore);
					command = modifyToCommand(ignore, modify);
					continue;
				case ELSE_COMMAND: {//else
					if (wasElse) {
						error("More then one 'else' inside the section", true);
						_sb.append(_line);
						return;
					} else {
						wasElse = true; //set flag else was already processed
					}
					ignore = sectionWasGenerated;
					modify = canonizeAndChageCommand(ignore);
					command = modifyToCommand(ignore, modify);
					sectionWasGenerated = true;
					if (command != END_COMMAND && command != IF_COMMAND) {
						error("'end' or 'if' expected after 'else'", true);
					}
					continue;
				}
				case END_COMMAND: //end
					commentLineEndUncomments();
					return;
				case SET_COMMAND:
				case UNSET_COMMAND:
					if (!ignore) {
						if (modify) {
							commentLineEndUncomments();
						} else {
							_sb.append(_line);
						}
						setOrUnsetCommand(command);
					} else {
						if (modify) {
							uncommentLineEndComments();
						} else {
							_sb.append(_line);
						}
					}
					command = parseLine();
					continue;
				case IF_COMMAND: //if
					if (ignore) {
						ignoreNestedIfCommand();
					} else {
						ifCommand(evaluate());
					}
					command = uncommentSection(modify);
					continue;
				case SRC_LINE:
					// This should never be!
					error("Internal error", true);
					_sb.append(_line);
					command = parseLine();
			}
		}
		error("Missing 'end' comand for the section which started at line " +
			startLine,
			false);
	}

	/** Modify nested "if" comand as comment. */
	private void ignoreNestedIfCommand() throws IOException {
		boolean modify = canonizeAndChageCommand(true);
		int command = modifyToCommand(true, modify);
		int startLine = _lineNumber;
		boolean wasElse = false;
		while (command != EOF) {
			switch (command) {
				case ELSEIF_COMMAND: //elseif
					evaluate();
					modify = canonizeAndChageCommand(true);
					command = modifyToCommand(true, modify);
					continue;
				case ELSE_COMMAND: {//else
					if (wasElse) {
						error("More then one 'else' inside the section", true);
						_sb.append(_line);
						return;
					} else {
						wasElse = true; //set flag else was already processed
					}
					modify = canonizeAndChageCommand(true);
					command = modifyToCommand(true, modify);
					if (command != END_COMMAND)	{
						error("'else' section was not finished with 'end'",
							true);
						_sb.append(_line);
					}
					continue;
				}
				case END_COMMAND: //end
					canonizeAndChageCommand(true);
					return;
				case SET_COMMAND:
				case UNSET_COMMAND:
					canonizeAndChageCommand(true);
					continue;
				case IF_COMMAND: //if
					ignoreNestedIfCommand();
					command = uncommentSection(modify);
					continue;
				case SRC_LINE:
					// This should never be!
					error("Internal error", true);
					_sb.append(_line);
					command = parseLine();
			}
		}
		error("Missing 'end' comand for the section which started at line " +
			startLine,
			false);
	}

	/** Process SET or UNSET command. */
	private void setOrUnsetCommand(final int command) throws IOException {
		for(;;) {
			skipBlanks();
			String swName = readKey();
			if (swName == null) {
				error("Switch name expected in set command", true);
			} else {
				if (command == SET_COMMAND) {
					if (!_keys.contains(swName)) {
						_keys.add(swName);
					}
				} else {// unset command
					_keys.remove(swName);
				}
				skipBlanks();
				if (!isChar(',')) {
					if (!isChar('*')) {
						error("Error in set command", true);
					} else {
						_pos--;
					}
					break;
				}
			}
		}
	}

	/** Canonize command line (i.e. ends with "*#/" or with "*&#47;") and return
	 * true if status of section had been changed.
	 * @param ignore if the nested status is "ignore".
	 * @return true if status of section had been changed.
	 */
	private boolean canonizeAndChageCommand(final boolean ignore) {
		int len =  _line.length() - 1;
		for (int i = len; i >= _pos - 1; i--) {
			if (_line.charAt(i) <= ' ') {
				continue; //skip leading whitespaces
			}
			if (_line.charAt(i) == '*') {
				_line = _line.substring(0, i + 1) + "#/\n";
				_modified = true;
				break;
			} else {
				if (i + 1 < len) {
					_line = _line.substring(0, i + 1) + "\n";
					_modified = true;
				}
				break;
			}
		}
		if (ignore ? _line.indexOf("*/") < 0 : _line.indexOf("*/") >= 0) {
			//no modification of section
			_sb.append(_line);
			return false;
		}
		//section will be modified.
		if (ignore) {
			uncommentLineEndComments();
		} else {
			commentLineEndUncomments();
		}
		return true;
	}

	/** Modify section to next command.
	 * @param ignore if true the section will be set as comment otherwise
	 * it will be "uncommented".
	 * @return type ID of last (unprocessed) line.
	 */
	private int modifyToCommand(final boolean ignore,
		final boolean modify) throws IOException {
		//canonize command line
		// Read and modify source section until it finds a preprocessor command.
		if (ignore) {
			return commentSection(modify); //make section as a comment
		} else {
			return uncommentSection(modify); //uncomment section
		}
	}

	/** Make section as comment. */
	private int commentSection(final boolean modify) throws IOException {
		while (true) {
			int command = parseLine();
			switch (command) {
				case EOF:
					return EOF;
				case END_COMMAND:
					return END_COMMAND;
				case IF_COMMAND:
					ignoreNestedIfCommand();
					continue;
				case SET_COMMAND:
				case UNSET_COMMAND:
					uncommentLineEndComments();
					continue;
				case ELSE_COMMAND:
				case ELSEIF_COMMAND:
					return command;
				default:
					if (modify) {
						uncommentLineEndComments();
					} else {
						_sb.append(_line);
					}
			}
		}
	}

	/** Change end of comments to "*#/" on actual line. */
	private void uncommentLineEndComments() {
		int ndx;
		if ((ndx = _line.indexOf("*/")) >= 0 ||
			(ndx = _line.indexOf("#/")) >= 0) {
			_modified = true;
			int i = 0;
			String s = _line.trim();
			if (s.startsWith("/*#") && s.endsWith("*#/")) {
				return; //do not add unnecessary '#'!
			}
			while (true) {
				_sb.append(_line.substring(i, ndx + 1));
				_sb.append("#");
				i = ndx + 1;
				ndx = _line.indexOf("*/", i);
				if (ndx < 0) {
					_sb.append(_line.substring(i));
					break;
				}
			}
		} else {
			_sb.append(_line);
		}
	}

	/** Uncomment section. */
	private int uncommentSection(final boolean modify) throws IOException{
		while (true) {
			int command = parseLine();
			switch (command) {
				case EOF:
					return EOF;
				case SET_COMMAND:
				case UNSET_COMMAND:
					commentLineEndUncomments();
					setOrUnsetCommand(command);
					continue;
				case IF_COMMAND:
				case END_COMMAND:
				case ELSE_COMMAND:
				case ELSEIF_COMMAND:
					return command;
				default:
					if (modify) {
						commentLineEndUncomments();
					} else {
						_sb.append(_line);
					}
			}
		}
	}

	/** Change "*#/" to end of comments on actual line. */
	private void commentLineEndUncomments() {
		int ndx;
		if ((ndx = _line.indexOf("#/")) >= 0) {
			_modified = true;
			int i = 0;
			while (true) {
				_sb.append(_line.substring(i, ndx));
				i = ndx + 1;
				ndx = _line.indexOf("#/", i);
				if (ndx < 0) {
					_sb.append(_line.substring(i));
					break;
				}
			}
		} else {
			_sb.append(_line);
		}
	}

	/** Skip white blanks. */
	private void skipBlanks() {
		char c;
		if (_pos < _endPos && (c = _line.charAt(_pos)) <= ' ' &&
			(c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\f')) {
			while (++_pos < _endPos && (c = _line.charAt(_pos)) <= ' ' &&
				(c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == 'f')){}
		}
	}

	private boolean isEndOfCommand() {
		skipBlanks();
		return _pos >= _endPos;
	}

	/** Check if actual position points to given character. Set the actual
	 * position to the next character if given character was recognized.
	 * @param ch Character to be checked.
	 * @return <tt>true</tt> if character was present at actual position,
	 * otherwise return <tt>false</tt>.
	 */
	private boolean isChar(final char ch) {
		if (_pos < _endPos && _line.charAt(_pos) == ch) {
			_pos++;
			return true;
		}
		return false;
	}

	/** Check if actual position points to given token. Set the actual
	 * position to the next character after the token if given token was
	 * recognized.
	 * @param token The token.
	 * @return <i>true</i> if token was present at actual position.
	 */
	private boolean isToken(final String token) {
		int len = token.length();
		if (_pos + len - 1 < _endPos && _line.startsWith(token,_pos)) {
			_pos += len;
			return true;
		}
		return false;
	}

	private String readKey() {
		StringBuilder sb = new StringBuilder();
		char c;
		while (_pos < _endPos &&
			(Character.isLetterOrDigit(c = _line.charAt(_pos)) ||
			c == '_' || c == '.')) {
			_pos++;
			sb.append(c);
		}
		if (sb.length() == 0) {
			return null;
		}
		return sb.toString().intern();
	}

	private boolean value() {
		boolean not = false;
		char c;
		if (isChar('!')) {
			not = true;
			skipBlanks();
		}
		if (isChar('(')) {
			skipBlanks();
			if (!expr()) {
				error("Error in expression", true);
			}
			skipBlanks();
			if (!isChar(')')) {
				error("Closing ')' missing", true);
			}
		} else {
			String key = readKey();
			if (key == null) {
				if (not) {
					error("Error in expression", true);
				}
				return not; //if not is false then nothing was parsed
			}
			_sp++;
			if (_sp >= MAX_STACK) {
				error("Too complex expression", true);
			}
			_exprStack[_sp] = _keys.contains(key);
			skipBlanks();
		}
		if (not) {
			_exprStack[_sp] = !_exprStack[_sp];
		}
		return true;
	}

	private boolean term() {
		if (!value()) {
			return false;
		}
		skipBlanks();
		while (isChar('&')) {
			skipBlanks();
			if (!value()) {
				error("Error in expression", true);
				return false;
			}
			_sp--;
			_exprStack[_sp] = _exprStack[_sp] & _exprStack[_sp + 1];
		}
		return true;
	}

	private boolean expr() {
		if (!term()) {
			return false;
		}
		skipBlanks();
		while (isChar('|')) {
			skipBlanks();
			if (!term()) {
				error("Error in expression", true);
				return false;
			}
			_sp--;
			_exprStack[_sp] = _exprStack[_sp] | _exprStack[_sp + 1];
		}
		return true;
	}

	private boolean evaluate() {
		_sp = -1;
		skipBlanks();
		if (!expr()) {
			error("Error in expression", true);
			return false;
		}
		skipBlanks();
		if (isChar('*')) {
			if (_sp != 0) {
				throw new Error("INTERNAL ERROR: stack");
			}
			return !_exprStack[0];
		}
		error("Incorrect command", true);
		return false;
	}

	/** Parse line. */
	private int parseLine() throws IOException {
		_pos = 0;
		if ((_line = _in.readLine()) == null) {
			_endPos = -1;
			return EOF; //end of source
		}
		_lineNumber++;
		int len = _line.length() - 1;
		if (len < 0) {
			_line = "\n";
			_endPos = 0;
			return SRC_LINE;
		} else {
			//cut trailing white spaces
			char c;
			if (_cutTrailingSpaces && len >= 0) {
				int i;
				for (i = len; i >= 0; i--) {
					if (_line.charAt(i) > ' ') {
						break;
					}
				}
				if (i != len) {
					_modified = true;
					if (i < 0) {
						_line = "\n";
						return SRC_LINE;
					} else {
						_line = _line.substring(0, i + 1);
						len = i;
					}
				}
			}
			_endPos = len + 1;
			_line += '\n';
		}
		skipBlanks();
		if (isToken("/*#")) {
			if (isToken("if")) {
				skipBlanks();
				int pos = _pos;
				evaluate();
				skipBlanks();
				if ((isChar('/') || isToken("#/")) && isEndOfCommand()) {
					_pos = pos;
					return IF_COMMAND; //if
				}
				error("Command incorrectly closed", true);
			} else if (isToken("end")) {
				skipBlanks();
				int pos = _pos;
				if ((isToken("*/") || isToken("*#/")) && isEndOfCommand()) {
					_pos = pos;
					return END_COMMAND; //end
				}
				error("Command incorrectly closed", true);
			} else if (isToken("else")) {
				skipBlanks();
				if (isToken("if")) {
					skipBlanks();
					int pos = _pos;
					evaluate();
					if ((isChar('/') || isToken("#/")) && isEndOfCommand()) {
						_pos = pos;
						return ELSEIF_COMMAND; //elseif
					}
					error("Command incorrectly closed", true);
				} else {
					int pos = _pos;
					if ((isToken("*/") || isToken("*#/")) && isEndOfCommand()) {
						_pos = pos;
						return ELSE_COMMAND; //else
					}
					error("Command incorrectly closed", true);
				}
			} else if (isToken("set")) {
				if (checkKeyList()) {
					return SET_COMMAND; //set
				}
			} else if (isToken("unset")) {
				if (checkKeyList()) {
					return UNSET_COMMAND; //unset
				}
			} else {
				error("Incorrect command", true);
			}
		}
		return SRC_LINE;
	}

	/** Check if key list is correct. */
	private boolean checkKeyList() {
		skipBlanks();
		int pos = _pos;
		for(;;) {
			String swName = readKey();
			if (swName == null) {
				error("Switch name expected in set command", true);
				break;
			} else {
				skipBlanks();
				if (isChar(',')) {
					skipBlanks();
				} else if ((isToken("*/") || !isToken("*#/")) &&
					isEndOfCommand()) {
					_pos = pos;
					return true;
				} else {
					error("Command incorrectly closed", true);
					break;
				}
			}
		}
		_pos = pos;
		return false;
	}

	/** Print error message.
	 * @param msg The default text of message.
	 * @param position if <tt>true</tt> the position information is generated.
	 */
	private void error(final String msg, final boolean position) {
		_out.flush();
		_err.flush();
		if (position) {
			_err.print(msg);
			_err.println("; line: " + _lineNumber + ", column: " + _pos);
		} else {
			_err.println(msg);
		}
		_err.flush();
		_errors++;
	}

	/** Get array of existing files represented by given argument. The argument
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
		return dir.listFiles(new FNameWildCardFilter(wn));
	}

	/** File names filter for names with wildcards (i.e '*' and/or '?').  */
	private class FNameWildCardFilter implements java.io.FileFilter {
		/** The name with wildcards */
		private final String _wildName;
		/** Length of the name with wildcards */
		private int _wildNameLen;

		/** Creates new instance of FNameWildCardFilter. Supported wildcards are
		 * Microsoft style: '*' (skip zero or more characters)
		 * and '?' (any character).
		 * @param wildName The string with (possible) wildcards.
		 */
		FNameWildCardFilter(final String wildName) {
			_wildNameLen = (_wildName = wildName).length() - 1;
		}

		@Override
		/** Check if the file suits wildcard conditions.
		 * @param file The file to be checked.
		 * @return true if and only if file suits.
		 */
		public final boolean accept(final File file) {
			if (file.isDirectory()) {
				return false;
			}
			if (_wildName.length() == 0) {
				return true;
			}
			String fname = file.getName();
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
						ch = _wildName.charAt(j++);
						while (fname.charAt(i) != ch && i <= fnameLen) {
							i++;
						}
						if (j > _wildNameLen) {
							return (i == fnameLen);
						}
						continue;
					case '?':
						if (j >= _wildNameLen) {
							return true;
						}
						ch = _wildName.charAt(j++);
						if (fname.charAt(i) != ch && i < fnameLen) {
							i++;
						}
						if (j >= _wildNameLen) {
							return (i == fnameLen);
						}
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

	private static void help() {
		System.err.flush();
		System.out.flush();
		System.out.println(
"JavaPreprocessor. Copyright 2004 Syntea Software Group.\n" +
"Preprocessor commands separates source sections by special Java comments.\n"+
"Comments in the form '/* ... */' inside of processed sections are allowed\n"+
"only on separated lines. Preprocessor modifies last character of the command\n"+
" lines to '/' or ' '. This causes following lines are interpreted either as the\n"+
"comment block or as part of the Java code. Preprocessor commands are:\n"+
"         start of processed section .......... /*#if expression */\n"+
"         conditioned alternative ............. /*#elseif expression */\n"+
"         optional alternative ................ /*#else*/\n"+
"         end of processed section ............ /*#end*/\n"+
"Expression contains list of swith names, connected by logical operators '&'\n"+
"and '|'. Usage of brackets in the expression is possible. Switch names must be\n"+
"composed from letters, digits, '.' or '_'s. Following example is result after\n"+
"application of switch _Boss_:\n"+
"         /*#if (Mary & John) | (Alice & Bob) *\n"+
"              System.out.println(\"Hello, friends!\");\n"+
"         /*#elseif _Boss_ */\n"+
"              System.out.println(\"Hi, Bill!\");\n"+
"         /*#else*\n"+
"              System.out.println(\"I do not know you!\");\n"+
"         /*#end*/");
		System.out.flush();
		cancel (0, "");
	}

	/** Print message and cancel program with exit code.
	 * @param exitCode The exit code.
	 * @param msg The default text of message.
	 */
	private static void cancel(final int exitCode, final String msg) {
		System.out.flush();
		System.err.println(
"JavaPreprocessor. Copyright 2004 Syntea Software Group.\n" +
			(msg == null ? "" :
				msg.length() > 0 ? "Error: " + msg + "\n" : "\n")+
"usage: [-h] [-r] [-t] [-v] [-x] -i input [-o output] [-encoding charset]\n"+
"       [-s switches]\n"+
"where:\n"+
"-r process directory tree. The parameter is optional.\n"+
"-t delete trailing spaces. The parameter is optional.\n"+
"-v make verbose output. The parameter is optional.\n"+
"-s switches: The list of switch names. Each switch name is composed from \n"+
"   letters, digits, '.' or '_'s. The switch can be either prefixed with '!'\n"+
"   or not specified. The parameter is optional.\n"+
"-i input: The file name list of the directories with the Java packages.\n"+
"   Each directory is supposed to be the root of package. Only the files\n"+
"   with the extension '.java' are processed. The parameter is obligatory.\n"+
"-o output: The directory where the output files are stored. The\n"+
"   parameter is optional. If it is missing the source files are replaced.\n"+
"-encoding charset name: if it is not specified then the system\n"+
"   default character set is used. The parameter is optional.\n"+
"-x extract preprocessor commands\n"+
"-h display the help text\n");
		System.err.flush();
		if (exitCode >= 0) {
			System.exit(exitCode);
		}
	}

	/** Call JavaPreprocessor.
	 * @param input The string with specification of the input file(s) or the
	 * directory.
	 * @param outDir The directory where put the changed files. If this argument
	 * is <tt>null</tt> the changed file will replace the input file.
	 * @param keys The array with switches used for preprocessing.
	 * @param dirTree If the value of this argument is <tt>true</tt> the
	 * preprocessor will scan also subdirectories..
	 * @param out PrintStream where will be printed output messages.
	 * @param err PrintStream where will be printed error messages.
	 * @param verbose If the value of this argument is <tt>true</tt> the
	 * preprocessor will print detailed information to <tt>out</tt>.
	 * @param extract If the value of this argument is <tt>true</tt> the
	 * preprocessor will extract all preprocessor commands.
	 * @param cutTrailingSpaces If the value of this argument is <tt>true</tt>
	 * the preprocessor will cut all whiteSpaces at the end of lines.
	 * @return string with error message if the program can't work due to
	 * parameter error or return <tt>null</tt>.
	 */
	private static String proc(final String input,
		final String outDir,
		final MyStringList keys,
		final boolean dirTree,
		final PrintStream out,
		final PrintStream err,
		final String charset,
		final boolean verbose,
		final boolean extract,
		final boolean cutTrailingSpaces) {
		JavaPreprocessor jp = new JavaPreprocessor();
		jp._out = out == null ? System.out : out;
		jp._err = err == null ? System.err : err;
		jp._processedCount = jp._modifyCount = jp._count = 0;
		jp._verbose = verbose;
		jp._extract = extract;
		jp._cutTrailingSpaces = cutTrailingSpaces;
		jp._keys = keys;
		if (charset != null) {
			jp._charset = charset;
		}
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
		if ((f = new File(input)).isDirectory()) {
			jp.processFiles(f.listFiles(), s, dirTree, false);
		} else {
			File[] files = jp.getWildCardFiles(input);
			if (files == null || files.length == 0) {
				jp._err.flush();
				jp._out.flush();
				return "Intput file doesn't exist: " + input;
			}
			for (int i = 0; i < files.length; i++) {
				jp.processFile(files[i], s, false); //not create first directory
			}
			if (dirTree) {
				String inp = input.replace('\\', '/');
				int i = inp.lastIndexOf('/');
				if (i < 0) {
					f = new File(".");
				} else {
					f = new File(inp.substring(0,i));
				}
				if (f.isDirectory()) {
					//set actual directory
					try {
						f = new File(f.getCanonicalPath());
						files = f.listFiles();
						for (int j = 0; j < files.length; j++) {
							if (files[j].isDirectory()) {
								jp.processFiles(new File[]{files[j]},
									s,
									true,
									false); //not create first directory
							}
						}
					} catch (Exception ex) {
						jp._err.flush();
						jp._out.flush();
						return "Path isn't accessable:" + f.getAbsolutePath();
					}
				}
			}
		}
		jp._err.flush();
		jp._out.flush();
		if (jp._verbose) {
			jp._out.println("Inspected " + jp._count +
				" file(s), preprocessor commands detected in " +
				jp._processedCount + ", changed " +
				jp._modifyCount + ".");
			jp._out.flush();
		}
		return jp._errors > 0 ?
			"Error " + (jp._errors > 1 ? "s" : "") + " detected." : null;
	}

	/** Call JavaPreprocessor from program.
	 * @param args Array of strings with command line parameters (see
	 * {@link JavaPreprocessor}).
	 * @param out PrintStream where will be printed output messages.
	 * @param err PrintStream where will be printed error messages.
	 * @return string with error message if the program can't work due to
	 * parameter error or return <tt>null</tt>.
	 */
	public static String proc(final String[] args,
		final PrintStream out,
		final PrintStream err) {
		if (args == null || args.length == 0) {
			cancel(0, "Missing parameters");
		}
		String input = null;
		String outDir = null;
		MyStringList keys = new MyStringList();
		boolean dirTree = false;
		String switches = "";
		boolean verbose = false;
		boolean extract = false;
		String charset = null;
		boolean cutTrailingSpaces = false;
		for (int i = 0; i < args.length; i++) {
			if ("-r".equals(args[i])) {
				if (dirTree) {
					return "'-r' redefined.";
				}
				dirTree = true;
			} else if (args[i].equals("-t")) {
				if (cutTrailingSpaces) {
					return "'-t' redefined.";
				}
				cutTrailingSpaces = true;
			} else if (args[i].equals("-v")) {
				if (verbose) {
					return "'-v' redefined.";
				}
				verbose = true;
			} else if (args[i].equals("-x")) {
				if (extract) {
					return "'-v' redefined.";
				}
				extract = true;
			} else if (args[i].equals("-encoding")) {
				if (charset != null) {
					return "'-encoding' redefined.";
				}
				String s;
				if (i + 1 >= args.length || args[i + 1].startsWith("-")) {
					return "\"-encoding\" parameter is missing.";
				} else {
					s = args[++i];
				}
				if (!Charset.isSupported(s)) {
					return "\"-encoding\" parameter \""+s+"\" is incorrect.";
				}
				charset = s;
			} else if (args[i].startsWith("-o")) {
				if (outDir != null) {
					return "Output directrory redefined.";
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
			} else if (args[i].startsWith("-s")) {
				StringBuilder sb = new StringBuilder();
				if (args[i].length() > 2) {
					sb.append(args[i].substring(2).trim());
				}
				if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					if (sb.length() > 0) {
						sb.append(',');
					}
					sb.append(args[++i]);
					while (i + 1 < args.length && !args[i + 1].startsWith("-")){
						sb.append(',');
						sb.append(args[++i].trim());
					}
				}
				// read switches - use JavaPreprocessor for parsing of them.
				JavaPreprocessor p = new JavaPreprocessor(sb.toString());
				p.skipBlanks();
				MyStringList notKeys = new MyStringList();
				while (p._pos < p._endPos) {
					boolean not;
					if (not = p.isChar('!')) {
						p.skipBlanks();
					}
					String s = p.readKey();
					if (s == null) {
						return "Incorrect switch";
					} else {
						if (switches.length() > 0) {
							switches += ", ";
						}
						if (not) {
							switches += "!";
							if (keys.contains(s)) {
								return "Confusion of the switch '" + s + "'";
							}
							if (!notKeys.contains(s)) {
								notKeys.add(s);
							}
						} else {
							if (notKeys.contains(s)) {
								return "Confusion of the switch '" + s + "'";
							}
							if (!keys.contains(s)) {
								keys.add(s);
							}
						}
						switches += s;
					}
					p.skipBlanks();
					if (p.isChar(',')) {
						p.skipBlanks();
					}
				}
			} else if (args[i].startsWith("-i")) {
				if (input != null) {
					return "Input redefined.";
				}
				if (args[i].length() > 2) {
					input = args[i].substring(2);
				} else {
					if (i + 1 > args.length || args[i + 1].startsWith("-")) {
						return "Incorrect input file parameter.";
					}
					input = args[++i];
				}
			} else {
				return "Incorrect parameter: " + args[i];
			}
		}
		if (input == null || input.length() == 0) {
			return "Input not specified";
		}
		File f = new File(input);
		if (!f.exists() || !f.isDirectory()) {
			if (dirTree) {
				return "Recurse parameter \"-r\" allowed only for directories.";
			}
		}
		if (charset != null && !Charset.isSupported(charset)) {
			return "Unsupported encoding: \"" + charset + "\".";
		}
		if (switches.length() == 0) {
			if (verbose) {
				out.println("Java preprocessor switches: not set");
				out.flush();
			}
		} else {
			if (verbose) {
				out.println("Java preprocessor switches: " + switches);
				out.flush();
			}
		}
		return proc(input, outDir, keys, dirTree, out, err,
			charset, verbose, extract, cutTrailingSpaces);
	}

	/** Call JavaPreprocessor from command line.
	 * @param args Array of strings with command line parameters (see
	 * {@link JavaPreprocessor}).
	 */
	public static void main(String[] args) {
		if (args == null || args.length == 1 && "-h".equals(args[0])) {
			help();
		} else {
			String s = proc(args, System.out, System.err);
			// returns message or null
			if (s != null) {
				cancel(1,  s);
			}
		}
	}

	/** This class is designed for the internal usage. */
	private static class MyStringList {

		private String[] _strings;

		/** Creates new instance of MyStringList. */
		private MyStringList() { _strings = new String[0]; }

		/** Creates new instance of MyStringList from other MyStringList. */
		private MyStringList(final MyStringList list) {
			_strings = new String[list._strings.length];
			System.arraycopy(list._strings, 0, _strings, 0, list.size());
		}

		/** Creates new instance of MyStringList from array of strings. */
		private MyStringList(final String[] list) {
			_strings = new String[list.length];
			System.arraycopy(list, 0, _strings, 0, list.length);
		}

		/** Add string to list*/
		private void add(final String s) {
			String[] strings = _strings;
			_strings = new String[strings.length + 1];
			System.arraycopy(strings, 0, _strings, 0, strings.length);
			_strings[strings.length] = s;
		}

		/** Get index of given string. */
		private int indexOf(final String s) {
			for (int i = 0; i < _strings.length; i++) {
				if (s.equals(_strings[i])) {
					return i;
				}
			}
			return -1;
		}

		/** check if list contains given string. */
		boolean contains(final String s) {
			return indexOf(s) >= 0;
		}

		/** Get number of items in the list. */
		private int size() {return _strings.length;}

		/** remove string from the list. */
		private boolean remove(final int i) {
			if (i < 0 || i >= _strings.length) {
				return false;
			}
			int newLen;
			if ((newLen = _strings.length - 1) == 0) {
				_strings = new String[0];
				return true;
			}
			String[] strings = _strings;
			_strings = new String[newLen];
			if (i == 0) {
				System.arraycopy(strings, 1, _strings, 0, newLen);
				return true;
			} else if (i == newLen) {
				System.arraycopy(strings, 0, _strings, 0, newLen);
				return true;
			}
			System.arraycopy(strings, 0, _strings, 0, i);
			System.arraycopy(strings, i + 1, _strings, i, newLen - i);
			return true;
		}

		/** remove given string from the list. */
		private boolean remove(final String s) {
			return remove(indexOf(s));
		}
	}
}