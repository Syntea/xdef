package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/** Implementation of ReportReader interface for input streams and/or files.
 * @author Vaclav Trojan
 */
public class FileReportReader implements ReportReader {

	/** Parser used for reading of messages. */
	private StringParser _parser;
	/** reader (if format is NOT XML. */
	private InputStreamReader _reader;

	/** Create new empty KFileReportReader.
	 * @param fname The pathname of file with the input data.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final String fname) throws SException {
		this(new File(fname));
	}

	/** Create new empty KFileReportReader.
	 * @param fname The pathname of file with the input data.
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final String fname,
		final boolean xmlFormat) throws SException {
		this(new File(fname), xmlFormat);
	}

	/** Create new empty KFileReportReader.
	 * @param fname The pathname of file with the input data.
	 * @param encoding The name of encoding table (<tt>null</tt> =&gt; default
	 * encoding).
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final String fname,
		final String encoding,
		final boolean xmlFormat) throws SException {
		this(new File(fname), encoding, xmlFormat);
	}

	/** Create new empty KFileReportReader.
	 * @param in The file with the input data.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final File in) throws SException {
		this(in, true);
	}

	/** Create new empty KFileReportReader.
	 * @param in The file with the input data.
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final File in,
		final boolean xmlFormat) throws SException {
		this(in, null, xmlFormat);
	}

	/** Create new empty KFileReportReader.
	 * @param in The file with the input data.
	 * @param encoding The name of encoding table (<tt>null</tt> =&gt; default
	 * encoding).
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 * @throws SException if an error occurs.
	 */
	public FileReportReader(final File in,
		final String encoding,
		final boolean xmlFormat) throws SException {
		try {
			if (encoding != null && encoding.length() > 0) {
				init(new InputStreamReader(new FileInputStream(in),
					encoding), xmlFormat);
			} else {
				init(new InputStreamReader(new FileInputStream(in)), xmlFormat);
			}
		} catch (FileNotFoundException ex) {
			throw new SException(SYS.SYS024, in);//File doesn't exist: &{0}
		} catch (UnsupportedEncodingException ex) {
			//Unsupported character set name: &{0}
			throw new SException(SYS.SYS035, encoding);
		}
	}

	/** Create new empty KFileReportReader.
	 * @param in The input stream reader.
	 */
	public FileReportReader(final InputStreamReader in) {
		init(in, true);
	}

	/** Create new empty KFileReportReader.
	 * @param in The input stream reader.
	 * @param xmlFormat if <tt>true</tt> the from of input data is in XML,
	 * otherwise the input data stream is processed as a stream of source lines.
	 */
	public FileReportReader(final InputStreamReader in,
		final boolean xmlFormat) {
		init(in, xmlFormat);
	}

	private void init(final InputStreamReader in, final boolean xmlFormat) {
		if (xmlFormat) {
			_parser = new StringParser(in, null);
			_reader = null;
		} else {
			_parser = null;
			_reader = in;
		}
	}

	private String parseXMLTextUntil(final char delimiter) {
		StringBuilder sb = new StringBuilder();
		while (!_parser.isChar(delimiter) && !_parser.eos()) {
			sb.append(_parser.peekChar());
		}
		int i = 0;
		for (;;) {
			int j;
			if ((j = sb.indexOf("&", i)) < 0) {
				return sb.toString();
			}
			if ((i = sb.indexOf("amp;", j + 1)) >= 0) {
				sb.replace(j, j + 5, "&");
			} else if ((i = sb.indexOf("apos;", j + 1)) >= 0) {
				sb.replace(j, j + 6, "'");
			} else if ((i = sb.indexOf("quot;", j + 1)) >= 0) {
				sb.replace(j, j + 6, "\"");
			} else if ((i = sb.indexOf("lt;", j + 1)) >= 0) {
				sb.replace(j, j + 4, "<");
			} else if ((i = sb.indexOf("gt;", j + 1)) >= 0) {
				sb.replace(j, j + 4, ">");
			} else if ((i = j + 1) >= sb.length()) {
				return sb.toString();
			}
		}
	}

	private static String[] REPORTSTARTTOKENS = new String[] {"<S>",
		"<U","<T","<A","<M","<W","<L","<E","<F","<X","<D","<K","<I"};

	@Override
	/** Get next report from the list or null.
	 * @return The report or null.
	 * @throws SRuntimeException if error occurs.
	 */
	public final Report getReport() {
		if (_parser != null) {
			if (_parser.eos()) {
				close();
				return null;
			}
			_parser.skipSpaces();
			if (_parser.eos()) {
				close();
				return null;
			}
			try {
				int index = _parser.isOneOfTokens(REPORTSTARTTOKENS);
				switch (index) {
					case -1:
						break;
					case 0: {
						String text = parseXMLTextUntil('<');
						if (_parser.isToken("/S>")) {
							if (_parser.eos()) {
								close();
							}
							return Report.string(null, text);
						}
						//Incorrect format of report
						throw new SIOException(SYS.SYS042);
					}
					default: {
						String id = _parser.isToken(" id=\"") ?
							parseXMLTextUntil('"') : null;
						String txt = _parser.isToken(" txt=\"") ?
							parseXMLTextUntil('"') : null;
						String mod = _parser.isToken(" mod=\"") ?
							parseXMLTextUntil('"') : null;
						long time = _parser.isToken(" time=\"") ?
							Long.parseLong(parseXMLTextUntil('"'), 16) : -1L;
						if (_parser.isToken("/>")) {
							Report result = new Report(
								(byte) REPORTSTARTTOKENS[index].charAt(1),
								id, txt, mod);
							if (time != -1L) {
								result.setTimestamp(time);
							}
							if (_parser.eos()) {
								close();
							}
							return result;
						}
					}
				}
				if (_parser.eos()) {
					close();
				}
				throw new SIOException(SYS.SYS042);//Incorrect format of report
			} catch (Exception ex) {
				//Program exception&{0}{: }
				throw new SRuntimeException(SYS.SYS036, ex);
			}
		} else if (_reader == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int ch, oldch;
		oldch = 0;
		try {
			while ((ch = _reader.read()) >= 0 && ch != '\n') {
				if (ch == '\r') {
					oldch = ch;
					continue;
				}
				if (oldch == '\r') {
					sb.append('\r');
				}
				sb.append((char) ch);
				oldch = ch;
			}
			if (ch <= 0) {
				close();
				if (sb.length() == 0) {
					return null;
				}
			}
		} catch (Exception ex) {
			//Program exception&{0}{: }
			throw new SRuntimeException(SYS.SYS036, ex);
		}
		return Report.string(null, sb.toString());
	}

	@Override
	/* Close the stream. */
	public final void close() {
		if (_reader != null) {
			try {
				_reader.close();
			} catch (IOException ex) {}
			_reader = null;
		} else if (_parser != null) {
			_parser.closeReader();
			_parser = null;
		}

	}

	@Override
	/** Write reports to String.
	 * @return the String with reports.
	 */
	public final String printToString() {
		return printToString(null);
	}

	@Override
	/** Write reports to String in specified language.
	 * @param language language id (ISO-639).
	 * @return the String with reports.
	 */
	public final String printToString(final String language) {
		StringBuilder sb = new StringBuilder();
		Report rep;
		boolean wasFirst = false;
		while ((rep = getReport()) != null) {
			if (wasFirst) {
				sb.append('\n');
			} else {
				wasFirst = true;
			}
			sb.append(language == null ?
				rep.toString() : rep.toString(language));
		}
		return sb.toString();
	}

	@Override
	/** Write reports to output stream.
	 * @param out The PrintStream where reports are printed.
	 * @throws SRuntimeException if an error occurs.
	 */
	public final void printReports(final PrintStream out) {
		Report rep;
		while ((rep = getReport()) != null) {
			out.println(rep.toString());
		}
	}

	@Override
	/** Write reports to output stream.
	 * @param out The PrintStream where reports are printed.
	 * @param language language id (ISO-639).
	 * @throws SRuntimeException if an error occurs.
	 */
	public final void printReports(final PrintStream out,final String language){
		Report rep;
		while ((rep = getReport()) != null) {
			out.println(rep.toString(language));
		}
	}

	@Override
	/** Write reports from this reporter reader to report writer.
	 * @param reporter OutputStreamWriter where to write,
	 */
	public final void writeReports(final ReportWriter reporter) {
		Report rep;
		while((rep = getReport()) != null) {
			reporter.putReport(rep);
		}
	}

}