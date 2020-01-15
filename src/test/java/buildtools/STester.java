package buildtools;

import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportPrinter;
import org.xdef.xml.KXmlUtils;

/** Abstract class for creating test classes.
 * You can create a test class as an extension of this class. You have to
 * implement the public void method test(). In this method you implement fail
 * methods and/or assertEq, assertTrue methods. You start the testing if you
 * invoke static method runTest(args) (usually in main method). This method
 * returns number of errors. Example:
 * <pre><code>
 public class TestMyCode extends org.xdef.sys.STester {
   public TestMyCode() {}
   public void test() {
	 try {
	   int i = 2*5;
	   if (i != 10) {
		 fail("Result is not 10");
	   }
	   assertTrue(i == 10);
	   assertFalse(i != 10);
	   assertEq(i, 10);
	 } catch(Exception ex) {
	   fail(ex);
	 }
   }
   public static void main(String... args) {
	 if (runTest(args) &gt; 0) {System.exit(1);}
 *   }
 * }
 * </code></pre>
 * @author Vaclav Trojan
 */
public abstract class STester {

	/* Output printer. */
	private PrintStream _out;
	/** Log file printer. */
	private PrintStream _outStream;
	/* Error printer. */
	private PrintStream _err;
	/* Debug flag. If true the method <tt>error(exception)</tt> prints stack
	 * trace information on <tt>System.err</tt>. Default value is true. */
	private boolean _debug;
	/** Start time of test. */
	private long _timeStamp;
	/** Error counter. */
	private int _errors;
	/** Name of test. */
	private String _name;
	/** Source file name. */
	private String _sourceName;
	/** Home directory of test. */
	private String _homeDir;
	/** Source directory. */
	private String _sourceDir;
	/** Temporary directory. */
	private String _tempDir;
	/** Data directory. */
	private String _dataDir;
	/** Class name. */
	private String _className;
	/** Result info. */
	private String _resultInfo;
	/** Array with arguments. */
	private String[] _arguments;

	/** Create empty instance. */
	public STester() {}

	private void printlnOut(final String s) {
		flushErr();
		_out.println(s);
		if (_outStream != null) {
			_outStream.println(s);
		}
	}
	private void flushOut() {
		_out.flush();
		if (_outStream != null) {
			_outStream.flush();
		}
	}

	private void printErr(final String s) {
		flushOut();
		_err.print(s);
		if (_outStream != null) {
			_outStream.print(s);
		}
	}

	private void printlnErr(final String s) {
		flushOut();
		_err.println(s);
		if (_outStream != null) {
			_outStream.println(s);
		}
	}

	private void flushErr() {
		_err.flush();
		if (_outStream != null) {
			_outStream.flush();
		}
	}

	/** Create empty instance.
	 * @param out where to print messages.
	 * @param err where to print errors.
	 */
	public STester(final PrintStream out, final PrintStream err) {
		_out = out;
		_err = err;
	}

	/** Get arguments.
	 * @return array of command line arguments or <tt>null</tt>.
	 */
	public final String[] getArguments() {return _arguments;}

	/** Set arguments.
	 * @param args list of command line arguments.
	 */
	public final void setArguments(final String... args) {_arguments = args;}

	/** Get path to project home directory. If the directory doesn't exist put
	 *  an error message and return <tt>null</tt>.
	 *
	 * @return The string with the path to data directory or <tt>null</tt>.
	 */
	public final String getHomeDir() {return _homeDir;}

	/** Get path to Java source directory of this class source. If the directory
	 * doesn't exist put an error message and return <tt>null</tt>.
	 *
	 * @return The string with the path to source directory or <tt>null</tt>.
	 */
	public final String getSourceDir() {return _sourceDir;}

	/** Get path to data directory. If data doesn't exist put an error
	 * message and return <tt>null</tt>.
	 *
	 * @return The string with the path to data directory or <tt>null</tt>.
	 */
	public final String getDataDir() {return _dataDir;}

	/** Get path to temporary directory. If the directory doesn't exist try
	 * to create it. If the directory can't be created put error message
	 * and return <tt>null</tt>.
	 * @return The string with the path to temporary directory or <tt>null</tt>.
	 */
	public final String getTempDir() {
		if (_homeDir != null) {
			_tempDir = _homeDir + "temp/";
			File tempDir = new File(_tempDir);
			if (!tempDir.exists()) {
				if (!tempDir.mkdirs()) {
					throw new RuntimeException(
						"Can't create directory: " + _tempDir);
				}
			}
			return _tempDir;
		} else {
			throw new RuntimeException(
				"Home directory doesn't exist or isn't accessible");
		}
	}

	/** Get name of test (the name of class without package prefix).
	 * @return The name of test class.
	 */
	public final String getName() {return _name;}

	/** Get path and name of Java source file. If the file is unknown
	 * return <tt>null</tt>.
	 * @return The string with the path to source directory or <tt>null</tt>.
	 */
	public final String getSourceName() {return _sourceName;}

	/** Get output stream.
	 * @return The print stream or <tt>null</tt> if the output is not defined.
	 */
	public final PrintStream getOutStream() {return _out;}

	/** Set output stream.
	 * @param out print stream or <tt>null</tt>.
	 */
	public final void setOutStream(final PrintStream out) {
		if ((_out = out) == null) {
			_out = System.out;
		}
	}

	/** Get error stream.
	 * @return The print stream or <tt>null</tt> if err stream is not defined.
	 */
	public final PrintStream getErrStream() {return _err;}

	/** Set error stream.
	 * @param err print stream or <tt>null</tt>.
	 */
	public final void setErrStream(final PrintStream err) {
		if ((_err = err) == null) {
			_err = System.err;
		}
	}

	/** Get time in milliseconds when the instance of this object
	 * was initialized.
	 * @return The time in milliseconds when the instance of this object was
	 * created.
	 */
	public final long getTimeStamp() {return _timeStamp;}

	/** Set new value of time in time stamp which is considered as the initial
	 * time of test.
	 * @return The value of time stamp (in milliseconds).
	 */
	public final long newTimeStamp() {
		return _timeStamp = System.currentTimeMillis();
	}

	/** Get number of errors.
	 * @return The number of errors.
	 */
	public final int getFailCount() {return _errors;}

	/** Get errors counter. */
	public final void clearFailCount() {_errors = 0;}

	/** Increase number of errors by one. */
	public final void incFailCount() {_errors++;}

	/** Get the value of debug flag.
	 * @return The debug flag.
	 */
	public final boolean isDebug() {return _debug;}

	/** Set debug flag. If true some additional information is printed (such as
	 * the stack trace after fail method with an exception is invoked).
	 * @param debug the debug flag.
	 */
	public final void setDebug(final boolean debug) {_debug = debug;}

	/** Set additional result information (printed after test method
	 * is finished).
	 * @param info the string with result information.
	 */
	public final void setResultInfo(final String info) {
		if (info != null && !info.isEmpty()) {
			if (_resultInfo == null || _resultInfo.isEmpty()) {
				_resultInfo = info;
			} else {
				if (!_resultInfo.endsWith("\n")) {
					_resultInfo += "\n";
				}
				_resultInfo += info;
			}
		}
	}

	/** Increase error counter and write the information to the error stream.
	 * The class from which the error was reported is taken from
	 * <tt>_className</tt> field.
	 * @param ex Exception to be printed.
	 */
	public final void putErrInfo(final Throwable ex) {
		java.io.CharArrayWriter caw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(caw);
		ex.printStackTrace(pw);
		pw.close();
		String s = caw.toString();
		putErrInfo(ex.toString());
		int i = s.indexOf("\n\tat ");
		flushOut();
		printErr(s.substring(i+1));
	}

	/** Increase error counter and write the information to the error stream.
	 * The class from which the error was reported is taken from
	 * <tt>_className</tt> field.
	 * @param msg Text of error message.
	 */
	public final void putErrInfo(final String msg) {
		String text = msg;
		if (msg == null || msg.trim().length() == 0) {
			text = _name + " fail";
		} else {
			//we remove all leading new lines;
			char c;
			int i = 0;
			int len = text.length();
			while (i < len && ((c=text.charAt(i))=='\n' || c=='\r')){
				i++;
			}
			if (i > 0) {
				text = text.substring(i);
			}
			//we remove all trailing new lines;
			i = text.length() - 1;
			while (i > 0 && text.charAt(i) <=' '){
				i--;
			}
			if (i < text.length() - 1) {
				text = text.substring(0, i+1);
			}
			text = _name + " fail\n" + text;
		}
		_errors++;
		// in Java 1.3 is not avalable the method Throwable.getStackTrace()
		// so we grab the information from printStackTrace and we create
		// the info string from it.
		CharArrayWriter caw = new CharArrayWriter();
		PrintWriter pw = new PrintWriter(caw);
		new Throwable("").printStackTrace(pw);
		pw.close();
		String s = caw.toString();
		int i = s.lastIndexOf(_className + ".");
		int j = s.indexOf('\n', i) + 1;
		j = s.indexOf('\n', j);
		if (j < 0) {
			i = s.indexOf(_className + ".");
			while (i > 0 && s.charAt(i-1) != '\n') {i--;}
			j = s.indexOf('\n', i);
		}
		if (i >= 0 && j > 0) {
			if (s.charAt(j - 1) == '\r') {
				j--; //windows ends line with CR LF!
			}
			s = s.substring(i, j);
			j = s.indexOf('\n');
			if (j > 0 && s.charAt(j - 1) == '\r') {
				j--; //windows ends line with CR LF!
			}
			if (j > 0) {
				s = s.substring(0, j);
			}
		}
		printErr(text + "\n" + s + '\n');
	}

	/** Increase error counter and write the default information to the print
	 * stream. If the print stream is <tt>null</tt> the message is ignored.
	 */
	public final void fail() {putErrInfo("*");}

	/** Increase error counter and write information of given object.
	 * If the print stream is <tt>null</tt> the message is ignored.
	 * @param obj the report to be displayed as a fail information.
	 */
	public final void fail(final Object obj) {
		if (obj == null) {
			fail();
		} else {
			if (obj instanceof String) {
				putErrInfo((String) obj);
			} else if (obj instanceof ReportReader) {
				putErrInfo(((ReportReader) obj).printToString());
			} else if (obj instanceof Report) {
				putErrInfo(((Report) obj).toString());
			} else if (obj instanceof Throwable) {
				putErrInfo((Throwable) obj);
			} else {
				putErrInfo(obj.getClass().getName() + ": " + obj.toString());
			}
		}
	}

	/** Check booleans.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final Boolean a1, final Boolean a2) {
		assertEq(a1, a2, null);
	}

	/** Check booleans.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public void assertEq(Boolean a1, Boolean a2, Object msg) {
		if ((a1 == null && a2 != null) || (a1 != null && a1.compareTo(a2)!=0)) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + a1 + "; a2=" + a2);
		}
	}

	/** Check characters.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final Character a1, final Character a2) {
		assertEq(a1, a2, null);
	}

	/** Check characters.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Character a1,
		final Character a2,
		final Object msg) {
		if ((a1 == null && a2 != null) || (a1 != null && a1.compareTo(a2)!=0)) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+"a1='" + a1 + "'(" + ((int) a1) + "); a2='" +
				a2 + "'(" + ((int) a2) + ")");
		}
	}

	/** Check integer numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final Long a1, final Long a2) {
		assertEq(a1, a2, null);
	}

	/** Check integer numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Long a1, final Long a2, final Object msg) {
		if ((a1 == null && a2 != null) || (a1 != null && a1.compareTo(a2)!=0)) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + a1 + "; a2=" + a2);
		}
	}

	/** Check float numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final Double a1, final Double a2) {
		assertEq(a1, a2, null);
	}

	/** Check float numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Double a1,
		final Double a2,
		final Object msg) {
		if ((a1 == null && a2 != null) || (a1 != null && a1.compareTo(a2)!=0)) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + a1 + "; a2=" + a2);
		}
	}

	/** Check BigDecimal numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final BigDecimal a1, final BigDecimal a2) {
		assertEq(a1, a2, null);
	}

	/** Check BigDecimal numbers.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final BigDecimal a1,
		final BigDecimal a2,
		final Object msg) {
		if ((a1 == null && a2 != null) || (a1 != null && a1.compareTo(a2)!=0)) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + a1 + "; a2=" + a2);
		}
	}

	/** Check strings.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final String a1, final String a2) {
		assertEq(a1, a2, null);
	}

	/** Check strings.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final String a1,
		final String a2,
		final Object msg) {
		if ((a1 == null && a2 != null) ||
			(a1 != null && !a1.equals(a2))) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + (a1 == null ? "null" : "'" + a1 + "'")
				+ "; a2=" + (a2 == null ? "null" : "'" + a2 + "'"));
		}
	}

	/** Check elements.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final String a1, final Element a2) {
		assertEq(KXmlUtils.parseXml(a1).getDocumentElement(), a2);
	}

	/** Check elements.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final String a1,
		final Element a2,
		final String msg) {
		assertEq(KXmlUtils.parseXml(a1).getDocumentElement(), a2, msg);
	}

	/** Check elements.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public final void assertEq(final Element a1, final String a2) {
		assertEq(a1, KXmlUtils.parseXml(a2).getDocumentElement());
	}

	/** Check elements.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Element a1,
		final String a2,
		final String msg) {
		assertEq(a1, KXmlUtils.parseXml(a2).getDocumentElement(), msg);
	}

	/** Check elements.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public void assertEq(Element a1, Element a2) {assertEq(a1, a2, null);}

	/** Check elements are equal (text nodes are trimmed).
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Element a1,
		final Element a2,
		final String msg) {
		assertEq(a1, a2, msg, true);
	}

	/** Check elements are equal (text nodes are trimmed if argument trim
	 * is true).
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 * @param trim if true elements are trimmed.
	 */
	public final void assertEq(final Element a1,
		final Element a2,
		final Object msg,
		final boolean trim) {
		if (KXmlUtils.compareElements(a1, a2, true, null).errorWarnings()) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "arg1:\n"+KXmlUtils.nodeToString(a1)+
				"\narg2:\n"+KXmlUtils.nodeToString(a2));
		}
	}

	/** Check objects.
	 * @param a1 first value.
	 * @param a2 second value.
	 */
	public void assertEq(Object a1, Object a2) {assertEq(a1, a2, null);}

	/** Check objects.
	 * @param a1 first value.
	 * @param a2 second value.
	 * @param msg message to be printed or null.
	 */
	public final void assertEq(final Object a1,
		final Object a2,
		final Object msg) {
		if (a1 != null && a1 != null) {
			if (!equals(a1, a2)) {
				fail((msg != null ? msg.toString().trim() + '\n' : "")
					+ "a1=" + a1 + "; a2=" + a2);
			}
		} else if (a1 != null || a2 != null) {
			fail((msg != null ? msg.toString().trim() + '\n' : "")
				+ "a1=" + a1 + "; a2=" + a2);
		}
	}

	/** Check if objects are equal.
	 * @param a1 first object.
	 * @param a2 second object.
	 * @return true if and only if both objects are equal.
	 */
	public static boolean equals(Object a1, final Object a2) {
		if (a1 instanceof Number && a2 instanceof Number) {
			if (a1 instanceof BigDecimal) {
				return (a2 instanceof BigDecimal) 
					? a1.equals(a2) : a1.equals(new BigDecimal(a2.toString()));
			} else if (a2 instanceof BigDecimal) {
				return (a1 instanceof BigDecimal) 
					? a2.equals(a1) : a2.equals(new BigDecimal(a1.toString()));
			}
			if ((a1 instanceof Byte || a1 instanceof Short ||
				a1 instanceof Integer || a1 instanceof Long)
				&& (a2 instanceof Byte || a2 instanceof Short ||
				a2 instanceof Integer || a2 instanceof Long)) {
				return ((Number) a1).longValue() == ((Number) a2).longValue();
			}
			if ((a1 instanceof Float || a1 instanceof Double)
				&& (a2 instanceof Float || a2 instanceof Double)) {
				return ((Number) a1).doubleValue()==((Number) a2).doubleValue();
			}
		} else if (a1 instanceof byte[] && a2 instanceof byte[]) {
			return Arrays.equals((byte[]) a1, (byte[]) a2);
		} else if (a1 instanceof boolean[] && a2 instanceof boolean[]) {
			return Arrays.equals((boolean[]) a1, (boolean[]) a2);
		} else if (a1 instanceof char[] && a2 instanceof char[]) {
			return Arrays.equals((char[]) a1, (char[]) a2);
		} else if (a1 instanceof short[] && a2 instanceof short[]) {
			return Arrays.equals((int[]) a1, (int[]) a2);
		} else if (a1 instanceof int[] && a2 instanceof int[]) {
			return Arrays.equals((int[]) a1, (int[]) a2);
		} else if (a1 instanceof long[] && a2 instanceof long[]) {
			return Arrays.equals((long[]) a1, (long[]) a2);
		} else if (a1 instanceof float[] && a2 instanceof float[]) {
			return Arrays.equals((float[]) a1, (float[]) a2);
		} else if (a1 instanceof double[] && a2 instanceof double[]) {
			return Arrays.equals((double[]) a1, (double[]) a2);
		} else if (a1 instanceof Object[] && a2 instanceof Object[]) {
			return Arrays.equals((Object[]) a1, (Object[]) a2);
		}
		return a1.equals(a2);
	}

	/** Check if the argument is <tt>null</tt>. If not then invoke the method
	 * <tt>fail</tt>.
	 * @param a argument to be checked for true.
	 */
	public final void assertNull(final Object a) {assertNull(a, null);}

	/** Check if the argument is <tt>null</tt>. If not then invoke the method
	 * <tt>fail</tt>.
	 * @param a argument to be checked for true.
	 * @param msg message to be printed or null.
	 */
	public final void assertNull(final Object a, final Object msg) {
		if (a != null) {
			fail(msg);
		}
	}

	/** Check if the argument is <tt>true</tt>. If not then invoke the method
	 * <tt>fail</tt>.
	 * @param a argument to be checked for true.
	 */
	public final void assertTrue(final boolean a) {assertFalse(!a, null);}

	/** Check if the argument <tt>a</tt> is <tt>true</tt>. If not then invoke
	 * the method <tt>fail</tt> with the argument msg.
	 * @param a argument to be checked for true.
	 * @param msg message to be printed or null.
	 */
	public final void assertTrue(final boolean a, final Object msg) {
		assertFalse(!a, msg);
	}

	/** Check if the argument is <tt>false</tt>. If not then invoke the method
	 * <tt>fail</tt>.
	 * @param a argument to be checked for false.
	 */
	public final void assertFalse(final boolean a) {assertFalse(a, null);}

	/** Check if the argument <tt>a</tt> is <tt>false</tt>. If not then invoke
	 * the method <tt>fail</tt> with the argument msg.
	 * @param a argument to be checked for false.
	 * @param msg message to be printed or null.
	 */
	public final void assertFalse(final boolean a, final Object msg) {
		if (a) {
			fail(msg);
		}
	}

	/** Check if the reporter does not contain an error.
	 * @param reporter the reporter to be checked for no errors.
	 */
	public final void assertNoErrors(final ReportWriter reporter) {
		if (reporter.errors()) {
			fail(reporter.toString());
		}
	}

	/** Check if the reporter contains an error.
	 * @param reporter reporter to be checked for no errors.
	 */
	public final void assertErrors(final ReportWriter reporter) {
		if (!reporter.errors()) {
			fail("Error not reported");
		}
	}

	private static String getListing(final ReportWriter reporetr,
		final Object msg) {
		if (msg == null) {
			return null;
		}
		String xml;
		if (msg instanceof Node) {
			xml = KXmlUtils.nodeToString((Node) msg);
		} else if (msg instanceof String) {
			xml = (String) msg;
		} else {
			return msg.toString();
		}
		if (reporetr instanceof ArrayReporter) {
			StringWriter strw = new StringWriter();
			if (xml.charAt(0) == '<') {
				ReportPrinter.printListing(strw,
					xml, (ArrayReporter)reporetr, true);
			} else {
				try {
					ReportPrinter.printListing(strw,
						new FileReader(xml), (ArrayReporter) reporetr, true);
				} catch (Exception ex) {
					return xml;
				}
			}
			return strw.toString();
		}
		return xml;
	}

	/** Check if the reporter does not contain an error. If yes then invoke
	 * the method <tt>fail</tt> with the argument msg.
	 * @param reporter reporter to be checked for no errors.
	 * @param msg message to be printed or null.
	 */
	public final void assertNoErrors(final ReportWriter reporter,
		final Object msg) {
		if (reporter.errors()) {
			if (msg == null) {
				fail();
			} else {
				fail(getListing(reporter, msg));
			}
		}
	}

	/** Check if the reporter does not contain an error or warning. If yes then
	 * invoke the method <tt>fail</tt> with the argument msg.
	 * @param reporter the reporter to be checked for no errors and no warnings.
	 */
	public final void assertNoErrorwarnings(
		final ReportWriter reporter) {
		if (reporter.errorWarnings()) {
			fail(reporter.toString());
		}
	}

	/** Check if the reporter does not contain an error or warning. If yes then
	 * invoke the method <tt>fail</tt> with the argument msg.
	 * @param reporter the reporter to be checked for no errors and no warnings.
	 * @param msg message to be printed or null.
	 */
	public final void assertNoErrorwarnings(
		final ReportWriter reporter,
		final Object msg) {
		if (reporter.errorWarnings()) {
			if (msg == null) {
				fail();
			} else {
				fail(getListing(reporter, msg));
			}
		}
	}

	/** Run test and print result information. This method executes all tests
	 * and must be implemented by user extension of this class. */
	public abstract void test();

	/** Initialize fields of this class. It is automatically called by the
	 * method runTest, or it may be called by user. Note all fields are
	 * reinitialized.
	 * @param out The print stream for result information or <tt>null</tt>
	 * @param err The print stream for error messages or <tt>null</tt>.
	 * @param log The print stream all messages or <tt>null</tt>.
	 * @param clazz The class from which the method is called.
	 * @param arguments list of arguments.
	 */
	public final void init(final PrintStream out,
		final PrintStream err,
		final PrintStream log,
		final Class<?> clazz,
		final String... arguments) {
		_arguments = arguments;
		_out = out == null ? System.out : out;
		_err = err == null ? System.err : err;
		_outStream = log;
		_debug = true;
		_sourceName = null;
		_homeDir = null;
		_dataDir = null;
		_tempDir = null;
		_sourceDir = null;
		_resultInfo = "";
		_errors = 0;

		_className = clazz.getName();
		_name = clazz.getName();
		int ndx = _name.lastIndexOf('.');
		if (ndx >= 0) {
			_name = _name.substring(ndx + 1);
		}
		String s = _className.replace('.', '/') + ".class";
		URL url = ClassLoader.getSystemClassLoader().getResource(s);
		s = new File( url.getFile()).getAbsolutePath().replace('\\', '/');
		String cname = _className.replace('.', '/');
		ndx = s.indexOf("/build/web/WEB-INF/classes/" + cname);
		if (ndx >= 0) {
			s = s.substring(0, ndx + 1);
			_sourceName = s + "src/java/" + cname + ".java";
		} else {
			ndx = s.indexOf("/build/classes/" + cname);
			if (ndx < 0) {
				ndx = s.indexOf("/target/test-classes/" + cname);
				if (ndx < 0) {
					ndx = s.indexOf("/temp/classes/" + cname);
					if (ndx < 0) {
						ndx = s.indexOf("/classes/" + cname);
						if (ndx < 0) {
							_sourceName = null;
							_timeStamp = System.currentTimeMillis();
							return; //no homeDir, dataDir, sourceDir, sourceName
						}
					}
				}
			}
			s = s.substring(0, ndx + 1);
			_sourceName = s + "src/"+ cname + ".java";
		}
		_homeDir = s;
		if (!new File(_sourceName).exists()) {
			_sourceName = s + "test/" + cname + ".java";
			if (!new File(_sourceName).exists()) {
				if (s.endsWith("/build/test/")) {
					_homeDir = s.substring(0, s.length() - 11);
					s = _homeDir + "test/";
					_sourceName = s + cname + ".java";
				} else {
					_sourceName = s + "src/test/java/" + cname + ".java";
					if (!new File(_sourceName).exists()) {
						_sourceName = null;
						_timeStamp = System.currentTimeMillis();
						return;
					}
				}
				if (!new File(_sourceName).exists()) {
					_sourceName = null;
					_timeStamp = System.currentTimeMillis();
					return;
				}
			}
		}
		_sourceDir = _sourceName.substring(0, _sourceName.lastIndexOf('/') + 1);
		if (!new File(_sourceDir).isDirectory()) {
			_sourceName = null;
			_sourceDir = null;
			_timeStamp = System.currentTimeMillis();
			return;
		}
		File f;
		if (_sourceDir.contains("src/test/java/test/")
			&& (f = new File(s = SUtils.modifyString(_sourceDir,
			"src/test/java/test/" ,
			"src/test/resources/test/")+"data/")).exists() && f.isDirectory()) {
			_dataDir = s;
		} else if (_sourceDir.contains("/test/test/")
			&& (f = new File(s = SUtils.modifyString(
			_sourceDir, "test/test/" ,
			"test/resources/test/") + "data/")).exists() && f.isDirectory()) {
			_dataDir = s;
		} else if (_sourceDir.contains("/test/test/")
			&& (f = new File(s = SUtils.modifyString(
			_sourceDir, "test/test/" ,
			"resources/test/") + "data/")).exists() && f.isDirectory()) {
			_dataDir = s;
		} else {
			f = new File(s = _homeDir + "test/data/");
			if (!f.exists() || !f.isDirectory()) {
				s = _sourceDir + "data/";
				f = new File(s);
				_dataDir = (!f.exists() || !f.isDirectory()) ? null : s;
			} else {
				_dataDir = s;
			}
		}
		_tempDir =  _homeDir + "temp/";
		_timeStamp = System.currentTimeMillis();
	}

	/** Run test and print result information.
	 * @param out The print stream for result information or <tt>null</tt>
	 * @param err The print stream for error messages or <tt>null</tt>.
	 * @param log The print stream all messages or <tt>null</tt>.
	 * @param printOK if <tt>false</tt> then the result if printed only if
	 * an error was reported.
	 * @param arguments array with arguments or <tt>null</tt>.
	 * @return the number of errors.
	 */
	public final int runTest(final PrintStream out,
		final PrintStream err,
		final PrintStream log,
		final boolean printOK,
		final String... arguments) {
		try {
			init(out, err, log, getClass(), arguments);
			test();
		} catch (Exception ex) {
			boolean debug = _debug;
			_debug = true;
			fail(ex);
			_debug = debug;
		} catch (Error ex) {
			boolean debug = _debug;
			_debug |= true;
			fail(ex);
			_debug = debug;
		}
		if (_resultInfo == null) {
			_resultInfo = "";
		}
		if (printOK && out != null) {
			float duration =
				((float) ((System.currentTimeMillis() - _timeStamp) / 1000.0));
			if (!_resultInfo.isEmpty()) {
				_resultInfo = "; " + _resultInfo;
			}
			if (_errors == 0) {
				flushErr();
				printlnOut("OK " + _name
					+ "; time=" + new DecimalFormat("0.00").format(duration)
					+ "s" + _resultInfo);
			} else {
				flushOut();
				printlnErr(
					"Errors in " + _name + ": " + _errors + _resultInfo);
			}
		}
		return _errors;
	}

	/** Creates the instance of the class from which this method was called.
	 * @return The new instance of the class (from which the method was called).
	 */
	public final static STester getInstance() {
		String className;
		//get class name
		StackTraceElement[] st = new Throwable().getStackTrace();
		int i = 0;
		while (i < st.length && st[i].getClassName().equals(
			STester.class.getName())) {
			i++;
		}
		className = st[i].getClassName();
		try {
			return (STester) Class.forName(className).getConstructor(
				new Class<?>[0]).newInstance(new Object[0]);
		} catch (Exception ex) {
			throw new RuntimeException("Can't invoke: new " + className + "()");
		}
	}

	private static void cancel(final String msg) {
		System.out.flush();
		if (msg != null) {
			System.err.println(msg);
		}
		System.err.println(
"Usage:\n"+
" [-d home directory] the home directory.\n"+
" [-o out file] the file where information messages are recorded.\n"+
" [-e error file] the file where error messages are recorded.\n"+
" [-a] arguments for tested class follows - this must be the last item.\n"+
" [-h] help");
		System.exit(msg == null ? 0 : 1);
	}

	/** Run test and print result information on System.out (both, OK and error
	 * information).
	 * @param args the command line arguments (the first one is home directory).
	 * Usage:
	 * [-d home directory] the home directory.
	 * [-o out file] the file where information messages are recorded.
	 * [-e error file] the file where error messages are recorded.
	 * [-a] arguments for tested class follows - this must be the last item.
	 * [-h] help.
	 * @return the number of errors.
	 */
	public final static int runTest(final String... args) {
		STester at = getInstance();
		PrintStream out = System.out;
		PrintStream err = System.err;
		PrintStream log = null;
		String s;
		if (args == null || args.length == 0) {
			return at.runTest(out, err, log, true);
		}
		boolean printOK = true;
		for (int i = 0; i < args.length; i++) {
			if (args[i].length() >= 2 && args[i].charAt(0) == '-') {
				switch (args[i].charAt(1)) {
					case 'a':
						String[] arguments = new String[args.length - i + 1];
						for (int j = i + 1; j < args.length; j++) {
							arguments[j - i + 1] = args[j];
						}
						//must be the last parameter
						return at.runTest(out, err, log, printOK, arguments);
					case 'd':
						s = args[++i];
						File f = new File(s);
						if (!f.isDirectory()) {
							cancel("File is not home directory:" + s);
						}
						at._homeDir = f.getAbsolutePath().replace('\\','/');
						if (!at._homeDir.endsWith("/")) {
							at._homeDir += '/';
							at._sourceDir = at._homeDir + "src/";
							if (!new File(at._sourceDir).isDirectory()) {
								at._sourceDir = null;
							}
							at._tempDir = null;
							at._dataDir = at._homeDir + "data/";
						}
						continue;
					case 'o':
						s = args[++i];
						try {
							FileOutputStream fos =
								new FileOutputStream(s, true);
							out = new PrintStream(fos, true);
						} catch (Exception ex) {
							cancel("Can't create output stream:" + s);
						}
						continue;
					case 'e':
						s = args[++i];
						try {
							FileOutputStream fos =
								new FileOutputStream(s, true);
							err = new PrintStream(fos, true);
						} catch (Exception ex) {
							cancel("Can't create error stream:" + s);
						}
						continue;
					case 'l':
						s = args[++i];
						try {
							FileOutputStream fos =
								new FileOutputStream(s, true);
							log = new PrintStream(fos, true);
						} catch (Exception ex) {
							cancel("Can't create log stream:" + s);
						}
						continue;
					case 'h':
						cancel(null);
						continue;
					default:
						cancel("Incorrect parameter: " + args[i]);
				}
			} else {
				cancel("Incorrect parameter: " + args[i]);
			}
		}
		return at.runTest(out, err, log, printOK);
	}

	/** Run test and print result information on System.out. If the argument
	 * printOK is false only error information is printed.
	 * @param printOK if <tt>false</tt> then the result is printed only if
	 * an error was reported.
	 * @param args list of arguments or <tt>null</tt>.
	 * @return the number of errors.
	 */
	public final static int runTest(final boolean printOK,final String... args){
		return getInstance().runTest(
			System.out, System.err, (PrintStream) null, printOK);
	}

	/** Run test of the object given by argument and print result information.
	 * @param out The print stream for result information or <tt>null</tt>
	 * @param err The print stream for error messages or <tt>null</tt>.
	 * @param log The print stream all messages or <tt>null</tt>.
	 * @param test The object to be tested.
	 * @param printOK if <tt>false</tt> then the result is printed only if
	 * an error was reported.
	 * @param arguments list of arguments.
	 * @return the number of errors.
	 */
	public final static int runTest(final PrintStream out,
		final PrintStream err,
		final PrintStream log,
		final STester test,
		final boolean printOK,
		final String... arguments) {
		return test.runTest(out, err, log, printOK, arguments);
	}

	/** Run tests of the object given by argument and print result information.
	 * @param out The print stream for result information or <tt>null</tt>
	 * @param err The print stream for error messages or <tt>null</tt>.
	 * @param log The print stream all messages or <tt>null</tt>.
	 * @param tests The array of objects to be tested.
	 * @param info The information text.
	 * @param printOK if <tt>false</tt> then the result is printed only if
	 * an error was reported.
	 * @param args list of arguments.
	 * @return the number of errors.
	 */
	public final static int runTests(final PrintStream out,
		final PrintStream err,
		final PrintStream log,
		final STester[] tests,
		final String info,
		final boolean printOK,
		final String... args) {
		int errors = 0;
		long t = System.currentTimeMillis();
		for (int i = 0, j = tests.length; i < j; i++) {
			errors += tests[i].runTest(out, err, log, printOK, args);
		}
		DecimalFormat df = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		float duration = ((float)((System.currentTimeMillis() - t) / 1000.0));
		out.flush();
		String s;
		if (errors > 0) {
			s = String.valueOf(errors) + " error" + (errors > 1 ? "s": "") +
				(info != null ? " " + info : "") +
				", total time: " + df.format(duration) + "s";
			if (log != null) {
				log.println(s);
			}
			err.println(s);
		} else {
			s = "OK " + (info != null ? " " + info : "") +
				", total time: " + df.format(duration) + "s";
			err.flush();
			if (log != null) {
				log.println(s);
			}
			out.println(s);
		}
		return errors;
	}

	/** Run tests of the object given by argument and print detailed
	 * information.
	 * @param out The print stream for result information or <tt>null</tt>
	 * @param err The print stream for error messages or <tt>null</tt>.
	 * @param log The print stream all messages or <tt>null</tt>.
	 * @param tests The array of objects to be tested.
	 * @param info The information text.
	 * an error was reported.
	 * @param args array with arguments or <tt>null</tt>.
	 * @return the number of errors.
	 */
	public final static int runTests(final PrintStream out,
		final PrintStream err,
		final PrintStream log,
		final STester[] tests,
		final String info,
		final String... args) {
		return runTests(out, err, log, tests, info, true, args);
	}

}