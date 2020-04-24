package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

/** Static methods for printing listings of parsed source.
 * @author Vaclav Trojan
 */
public class ReportPrinter extends Report implements Comparable<ReportPrinter> {
	private final static DecimalFormat LINE_NUM_FORMAT =
		new DecimalFormat(" 00000  ");
	/** Saved actual position. */
	private final long _pos;
	/** Saved actual line number. */
	private final long _line;
	/** Saved column position. */
	private final long _column;
	/** Saved system id. */
	private final String _sysId;

	/** Create report with extracted position information. */
	private ReportPrinter(final Report report) {
		super(report.getType(),
			report.getMsgID(), report.getText(), report.getModification());
		_pos = extractNumParameter("&{pos}");
		_line = extractNumParameter("&{line}");
		_column = extractNumParameter("&{column}");
		_sysId = extractStringParameter("&{sysId}");
	}

	private long extractNumParameter(final String param) {
		try {
			String s = getModification();
			int i = s.indexOf(param);
			if (i < 0) {
				return -1L;
			}
			DecimalFormat f = new DecimalFormat();
			ParsePosition p = new ParsePosition(i + param.length());
			return f.parse(s, p).longValue();
		} catch (Exception ex) {
		}
		return -1L;
	}

	private String extractStringParameter(final String param) {
		int i;
		String s = getModification();
		if (s == null || (i = s.indexOf(param)) < 0) {
			return "";
		}
		int j = i + param.length();
		int k = s.indexOf("&{", j);
		return (k < 0) ? s.substring(j) : s.substring(j,k);
	}

	private static String extractPositionItem(final String s,
		final String item) {
		int i = s.indexOf(item);
		if (i < 0) {
			return s;
		}
		int k = s.indexOf("&{", i + item.length());
		return (k < 0) ? s.substring(0,i) : s.substring(0,i) + s.substring(k);
	}

	private static String extractPosition(final String s) {
		if (s == null) {
			return "";
		}
		String result = extractPositionItem(s, "&{pos}");
		result = extractPositionItem(result, "&{line}");
		result = extractPositionItem(result, "&{column}");
		result = extractPositionItem(result, "&{sysId}");
		return result;
	}

	@Override
	/** compares position of report - internally used for sorting. */
	public boolean equals(final Object o) {
		if (o != null && o instanceof ReportPrinter) {
			ReportPrinter rep = (ReportPrinter)o;
			return rep._sysId.equals(_sysId)
				&& rep._line==_line && rep._column==_column;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 53 * 7 + (int) (_line ^ (_line >>> 32));
		hash = 53 * hash + (int) (_column ^ (_column >>> 32));
		return 53 * hash + (_sysId != null ? _sysId.hashCode() : 0);
	}

	@Override
	/** Compares this object with the specified object for order. Comparison
	 * respects 1. position (if specified), 2. time (if specified). This serves
	 * to sort messages according to source position.
	 * @param rep Object to be compared.
	 * @return A negative integer, zero, or a positive integer as this object
	 * is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final ReportPrinter rep) {
		if (rep._line == -1) {
			return -1;
		}
		if (_line == -1) {
			return 1;
		}
		if ((_sysId == null || rep._sysId == null)
			|| _sysId.compareTo(rep._sysId) != 0){
			return -1;
		}
		if (_line != rep._line) {
			return _line < rep._line ? -1 : 1;
		}
		return _column == rep._column ? 0 : _column < rep._column ? -1 : 1;
	}

	/** Print message sources of all registered messages with given prefix.
	 * @param prefix Message prefix.
	 * @param out where to print.
	 * @param language language of messages.
	 * @param resolveReportReferences if true then all references to other
	 * reports are resolved.
	 * @throws Exception if given prefix is not registered.
	 */
	public static void printMessages(final String prefix,
		final PrintStream out,
		final String language,
		final boolean resolveReportReferences) throws Exception {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		Class<?> clazz = cl.loadClass("org.xdef.msg." + prefix);
		Field field = clazz.getField(prefix);
		String[] ids = (String[]) field.get(new String[]{});
		for (String id: ids) {
			out.println(prefix+id + ": " +
				getMsgSource(prefix+id, language, resolveReportReferences));
		}
	}

	/** Get message source.
	 * @param id message id .
	 * @param language language of message.
	 * @param resolveReferences if true then all references to other
	 * reports are resolved.
	 * @return string with message source.
	 */
	public static String getMsgSource(final String id,
		final String language,
		final boolean resolveReferences) {
		return resolveReferences ? Report.getReportText(id, language) :
			Report.getRawReportText(id, language);
	}

	/** Print listing (with line numbers) from source string with message table
	 * to a string.
	 * @param source The source string (may be a string with XML or pathname).
	 * @param reports Report file.
	 * @return listing as a string.
	 */
	public static String printListing(final String source,
		final ReportReader reports) {
		CharArrayWriter out = new CharArrayWriter();
		printListing(out, source, reports, true);
		return out.toString();
	}

	/** Print listing from source string with message table to output stream.
	 * @param out The PrintStream where reports are printed.
	 * @param source The source string (may be a string with XML or pathname).
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final PrintStream out,
		final String source,
		final ReportReader reports,
		final boolean lineNumbers) {
		if (source.charAt(0) == '<' || source.charAt(0) <= ' ') {
			printListing(out,
				new CharArrayReader(source.toCharArray()), reports,lineNumbers);
		} else {
			printListing(out, new File(source), reports, lineNumbers);
		}
	}

	/** Print listing from source string with message table to output stream.
	 * @param out The PrintStream where reports are printed.
	 * @param url The URL of the source.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final PrintStream out,
		final URL url,
		final ReportReader reports,
		final boolean lineNumbers) {
		Reader in;
		try {
			in = new java.io.InputStreamReader(url.openStream());
		} catch (Exception ex) {
			out.println("Can't read the source");
			ex.printStackTrace(out);
			return;
		}
		printListing(new OutputStreamWriter(out), in, reports, lineNumbers);
		try {in.close();} catch (Exception ex) {}
	}

	/** Print listing from source string with message table to output stream.
	 * @param out The PrintStream where reports are printed.
	 * @param file The file with the source.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final PrintStream out,
		final File file,
		final ReportReader reports,
		final boolean lineNumbers) {
		Reader in;
		try {
			in = new FileReader(file);
		} catch (Exception ex) {
			out.println("Can't read the source");
			ex.printStackTrace(out);
			return;
		}
		printListing(out, in, reports, null, lineNumbers);
		try {in.close();} catch (Exception ex) {}
	}

	/** Print listing of input source lines with error reports
	 * to the OutputStream.
	 * @param out The PrintStream where reports are printed.
	 * @param in The input stream with the source.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final PrintStream out,
		final Reader in,
		final ReportReader reports,
		final boolean lineNumbers) {
		printListing(out, in, reports, null, lineNumbers);
	}

	/** Print listing of input source lines with error reports
	 * to the OutputStream.
	 * @param out The PrintStream where reports are printed.
	 * @param in The input stream with the source.
	 * @param reports Report file.
	 * @param name Filter source name (sysId).
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final PrintStream out,
		final Reader in,
		final ReportReader reports,
		final String name,
		final boolean lineNumbers) {
		printListing(
			new OutputStreamWriter(out), in, reports, name, lineNumbers);
	}

	/** Print listing from source string with message table to output stream.
	 * @param out The output writer.
	 * @param file The source file.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final Writer out,
		final File file,
		final ReportReader reports,
		final boolean lineNumbers) {
		Reader in = null;
		try {
			in = new java.io.FileReader(file);
		} catch (Exception ex) {
			String s = "Can't read the source: " + ex;
			try {
				out.write(s);
			} catch (Exception ex1) {
			}
			return;
		}
		printListing(out, in, reports, lineNumbers);
	}

	/** Print listing from source string with message table to output stream.
	 * @param out The output writer.
	 * @param source The source string.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final Writer out,
		final String source,
		final ReportReader reports,
		final boolean lineNumbers) {
		printListing(out,
			new CharArrayReader(source.toCharArray()),
			reports,
			lineNumbers);
	}

	/** Print listing of input source lines with error reports
	 * to the OutputStream.
	 * @param out The output print stream.
	 * @param in The input stream with the source.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final Writer out,
		final Reader in,
		final ReportReader reports,
		final boolean lineNumbers) {
		printListing(out, in, reports, null, 80, lineNumbers, null);
	}

	/** Print listing of input source lines with error reports
	 * to the OutputStream.
	 * @param out The output print stream.
	 * @param in The input stream with the source.
	 * @param reports Report file.
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 * @param name Filter source name (sysId).
	 */
	public static void printListing(final Writer out,
		final Reader in,
		final ReportReader reports,
		final String name,
		final boolean lineNumbers) {
		printListing(out, in, reports, name, 80, lineNumbers, null);
	}

	/** Print listing of input source line range with error reports
	 * to the output stream.
	 * @param out The output writer.
	 * @param reader The reader with the source.
	 * @param reports reader of messages.
	 * @param sysId Filter source name (sysId), if null no filter.
	 * @param maxLineLength maximal length of printed line (longer lines are
	 * wrapped).
	 * @param language Language ID or null (i.e. default).
	 * @param lineNumbers if <tt>true</tt> then line numbers are printed.
	 */
	public static void printListing(final Writer out,
		final Reader reader,
		final ReportReader reports,
		final String sysId,
		final int maxLineLength,
		final boolean lineNumbers,
		final String language) {
		LineNumberReader ln = null;
		try {
			ArrayList<ReportPrinter> arr = new ArrayList<ReportPrinter>();
			ReportPrinter sp;
			Report rep;
			while ((rep = reports.getReport()) != null) {
				sp = new ReportPrinter(rep);
				if (sysId == null ||
					sp._sysId != null && sysId.equals(sp._sysId)) {
					arr.add(sp);
				}
			}
			ReportPrinter[] sortedArray = new ReportPrinter[arr.size()];
			arr.toArray(sortedArray);
			arr.clear();
			Arrays.sort(sortedArray);
			int index = 0;
			int warnings = 0;
			int errors = 0;
			int fatals = 0;
			sp = (sortedArray.length > 0) ? sortedArray[0] : null;
			int lineNum = 0;
			ln = new LineNumberReader(reader);
			String line;
			while ((line = ln.readLine()) != null) {
				lineNum++;
				if (lineNumbers) {
					out.write(LINE_NUM_FORMAT.format(lineNum));
				}
				out.write(line);
				out.write('\n');
				while (sp != null && sp._line == lineNum) {
					switch (sp.getType()) {
						case Report.FATAL:
							fatals++;
							break;
						case Report.ERROR:
							errors++;
							break;
						case Report.WARNING:
							warnings++;
							break;
					}
					sp.setModification(extractPosition(sp.getModification()));
					if (lineNumbers) {
						out.write(" *****  ");
					}
					for (int i = 1; i < sp._column; i++) {
						out.write(' ');
					}
					out.write("|\n");
					out.write(sp.toString(language));
					out.write('\n');
					sp = (++index < sortedArray.length)
						? sortedArray[index] : null;
				}
			}
			// print messages which were not reported yet
			boolean wasNoPos = false;
			while (index < sortedArray.length) {
				sp = sortedArray[index++];
				if (!wasNoPos) {
					out.write("=================\n");
					wasNoPos = true;
				}
				out.write(sp.toString(language));
				out.write('\n');
				switch (sp.getType()) {
					case Report.FATAL:
						fatals++;
						break;
					case Report.ERROR:
						errors++;
						break;
					case Report.WARNING:
						warnings++;
						break;
					default:
				}
				if (sp._pos > -1) {
					break;
				}
			}
			if (wasNoPos) {
				out.write("=================\n\n");
			}
			//if errors are after last line print them!
			if (index < sortedArray.length) {
				out.write("\n  ");
				do {
					sp = sortedArray[index];
					switch (sp.getType()) {
						case Report.FATAL:
							fatals++;
							break;
						case Report.ERROR:
							errors++;
							break;
						case Report.WARNING:
							warnings++;
							break;
						default:
					}
					out.write(sp.toString(language));
					out.write('\n');
				} while (++index < sortedArray.length);
			}
			if (fatals + errors + warnings > 0) {
				//Fatal errors: &{0}, errors: &{1}, warnings: &{2}
				out.write(Report.text(SYS.SYS068, fatals, errors, warnings)
					.toString(language));
				out.write('\n');
			}
			out.flush();
		} catch (IOException ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
		try {ln.close();}catch (Exception ex) {}
	}

	/** print message and help information and finish program.
	 * @param msg The message.
	 */
	private static void printUsage(final String msg) {
		System.err.println(
"ReportPrinter                                    (c)2006 Syntea Software Group"
			);
		System.err.println(msg);
		System.err.println("Usage: -i errfile [-o output]\n"
+"where:\n"
+" -i errfile ... file name with the error records\n"
+" -o output  ... name of output file (default is set to the standard output)\n"
		);
		System.exit(1);
	}

	/** Main - print error file from command line.
	 * @param args The array of strings with following structure:
	 * <p>source [output] [-i n]] [-o]</p>
	 * <p>where:</p>
	 * <p>-i errfile ... file name with error records</p>
	 * <p>-o output  ... name of output file (default is standard output)</p>
	 */
	public static void main(String... args) {
		if (args.length == 0) {
			printUsage("Error: missing parameters");
		}
		int len = args.length - 1;
		String inputFname = null;
		String outputFname = null;
		for (int i = 0, j = 0; i <= len; i++) {
			if (args[i].startsWith("-")) {
				if (args[i].length() >= 2) {
					switch (args[i].charAt(1)) {
						case 'i':
							if (inputFname != null) {
								printUsage("Duplicated parameter: " + args[i]);
							}
							if (args[i].length() > 2) {
								inputFname = args[i].substring(2);
							} else {
								if (++i <= len && !args[i].startsWith("-")) {
									inputFname = args[i];
								} else {
									printUsage("Missing input");
								}
							}
							continue;
						case 'o':
							if (outputFname != null) {
								printUsage("Duplicated parameter: " + args[i]);
							}
							if (args[i].length() > 2) {
								outputFname = args[i].substring(2);
							} else {
								if (++i <= len && !args[i].startsWith("-")) {
									outputFname = args[i];
								} else {
									printUsage("Missing output");
								}
							}
							continue;
						default:
							continue;
					}
				}
			}
			printUsage("Incorrect parameter: " + args[i]);
		}
		if (inputFname == null) {
			printUsage("Input file parameter is missing");
		}
		FileReportReader frr = null;
		PrintStream out = null;
		try {
			frr = new FileReportReader(
				new InputStreamReader(new FileInputStream(inputFname)), true);
			if (outputFname != null) {
				out = new PrintStream(new FileOutputStream(outputFname));
			} else {
				out = System.out;
			}
			Report rep;
			while ((rep = frr.getReport()) != null) {
				out.println(rep.toString());
			}
		} catch (Exception ex) {
			printUsage("Error: " + ex);
		}
		try {frr.close();} catch (Exception ex) {}
		if (outputFname != null) {
			try {out.close();} catch (Exception ex) {}
		} else {
			out.flush();
		}
	}

}