package org.xdef.sys;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.msg.SYS;
import org.xdef.xml.KXmlUtils;

/** Abstract class for creating test classes. This class enables to run tests
 * without test tools and it provides few more tests using xdef (junit etc.).
 * You can create a test class as an extension of this class. You have to
 * implement the public void method test(). In this method you implement fail
 * methods and/or assertEq, assertTrue methods. You start the testing if you
 * invoke static method runTest(args) (usually in main method). This method
 * returns number of errors. Example:
 * <pre><code>
 public class TestMyCode extends org.xdef.sys.STester {
   TestMyCode() {}
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
   }
 }</code></pre>
 * @author Vaclav Trojan
 */
public abstract class STester {

	/* Output printer. */
	private PrintStream _out;
	/** Log file printer. */
	private PrintStream _outStream;
	/* Error printer. */
	private PrintStream _err;
	/* Debug flag. If true the method <i>error(exception)</i> prints stack
	 * trace information on <i>System.err</i>. Default value is true. */
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
	private File _tempDir;
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

	/** Create empty instance.
	 * @param out where to print messages.
	 * @param err where to print errors.
	 */
	public STester(final PrintStream out, final PrintStream err) {
		_out = out;
		_err = err;
	}
	/** Get arguments.
	 * @return array of command line arguments or null.
	 */
	public final String[] getArguments() {return _arguments;}
	/** Set arguments.
	 * @param args list of command line arguments.
	 */
	public final void setArguments(final String... args) {_arguments = args;}
	/** Get path to project home directory. If the directory doesn't exist put
	 *  an error message and return null.
	 *
	 * @return The string with the path to data directory or null.
	 */
	public final String getHomeDir() {return _homeDir;}
	/** Get path to Java source directory of this class source. If the directory
	 * doesn't exist put an error message and return null.
	 * @return The string with the path to source directory or null.
	 */
	public final String getSourceDir() {return _sourceDir;}
	/** Get path to data directory. If data doesn't exist put an error
	 * message and return null.
	 * @return The string with the path to data directory or null.
	 */
	public final String getDataDir() {return _dataDir;}
	/** Get path to temporary directory. If the directory doesn't exist try
	 * to create it. If the directory can't be created put error message
	 * and return null.
	 * @return The string with the path to temporary directory.
	 * @throws SRuntimeException if temporary directory is not available.
	 */
	public final String getTempDir() throws RuntimeException {
		if (_homeDir != null) {
			_tempDir = new File(_homeDir + "temp/");
			if (!_tempDir.exists()) {
				if (!_tempDir.mkdirs()) {
					//Can't create directory: &{0}
					throw new SRuntimeException(SYS.SYS020, _tempDir);
				}
			}
			try {
				String s = _tempDir.getCanonicalPath().replace('\\', '/');
				return s.endsWith("/") ? s : s + '/';
			} catch (Exception ex) {
				throw new SRuntimeException(ex); // never happens
			}
		} else {
			_tempDir = null;
			//Can't create directory: &{0}
			throw new SRuntimeException(SYS.SYS020, "null");
		}
	}
	/** Delete all files and subdirectories from temporary directory (only those
	 * which can be deleted).
	 * @return File object with temporary directory.
	 * @throws SRuntimeException if temporary directory is not available.
	 */
	public final File clearTempDir() {
		getTempDir();
		if (!_tempDir.exists()) {
			_tempDir.mkdirs();
		} else if (_tempDir.isDirectory()) {
			File[] files = _tempDir.listFiles();
			for (File x: files) {
				try { // try to delete this file
					if (x.isDirectory()) {
						FUtils.deleteAll(x, true);
					} else if (x.isFile()){
						x.delete();
					}
				} catch (Exception ex) {}
			}
		} else {
			//Can't create directory: &{0}
			throw new SRuntimeException(SYS.SYS020, _tempDir.getAbsolutePath());
		}
		return _tempDir;
	}
	/** Get name of test (the name of class without package prefix).
	 * @return The name of test class.
	 */
	public final String getName() {return _name;}
	/** Get path and name of Java source file. If the file is unknown
	 * return null.
	 * @return The string with the path to source directory or null.
	 */
	public final String getSourceName() {return _sourceName;}
	/** Get output stream.
	 * @return print stream or null if the output is not defined.
	 */
	public final PrintStream getOutStream() {return _out;}
	/** Set output stream.
	 * @param out print stream or null.
	 */
	public final void setOutStream(final PrintStream out) {
		if ((_out = out) == null) {
			_out = System.out;
		}
	}
	/** Get error stream.
	 * @return The print stream or null if err stream is not defined.
	 */
	public final PrintStream getErrStream() {return _err;}
	/** Set error stream.
	 * @param err print stream or null.
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
	private void printlnOut(final String s) {
		flushAll();
		_out.println(s);
		if (_outStream != null) {
			_outStream.println(s);
		}
		flushAll();
	}
	private void flushAll() {
		_out.flush();
		_err.flush();
		if (_outStream != null) {
			_outStream.flush();
		}
	}
	private void printErr(final String s) {
		flushAll();
		_err.print(s);
		if (_outStream != null) {
			_outStream.print(s);
		}
		flushAll();
	}
	/** Write the information message to the out stream.
	 * The class from which an error was reported is taken from the field
	 * <code>_className</code>.
	 * @param msg Text of information message.
	 */
	public final void putInfo(final String msg) {
		printlnOut("[INFO] " + _name +
			(msg != null && !msg.trim().isEmpty() ? ": " + msg : ""));
	}
	/** Increase error counter and write the information to the error stream.
	 * The class from which the error was reported is taken from
	 * <code>_className</code> field.
	 * @param ex Exception to be printed.
	 */
	public final void putErrMsg(final Throwable ex) {
		String s = printThrowable(ex);
		putErrMsg(ex.toString());
		int i = s.indexOf("\n\tat ");
		printErr(s.substring(i+1));
	}
	/** Increase error counter and write the information to the error stream.
	 * The class from which an error was reported is taken from the field
	 * <code>_className</code>.
	 * @param msg Text of error message.
	 */
	public final void putErrMsg(final String msg) {
		String text = "[ERROR] " + _name +
			(msg != null && !msg.trim().isEmpty() ? '\n' + msg.trim() : "");
		_errors++;
		// in Java 1.6 is not avalable the method Throwable.getStackTrace()
		// so we grab the information from printStackTrace and we create
		// the info string from it.
		String s = printThrowable(new Throwable(""));
		int i = s.indexOf(_className + ".");
		i = s.indexOf('\n', i);
		if (i >= 0) {
			s = s.substring(0, i);
		}
		i = s.lastIndexOf('\n');
		if (i >= 0) {
			s = s.substring(i +1);
		}
		printErr(text + "\n" + s + '\n');
	}
	/** Increase error counter and write the default information to the print
	 * stream. If the print stream is null the message is ignored.
	 */
	public final void fail() {putErrMsg("*");}
	/** Increase error counter and write information of given object.
	 * If the print stream is null the message is ignored.
	 * @param obj the report to be displayed as a fail information.
	 */
	public final void fail(final Object obj) {
		if (obj == null) {
			fail();
		} else {
			if (obj instanceof String) {
				putErrMsg((String) obj);
			} else if (obj instanceof ReportReader) {
				putErrMsg(((ReportReader) obj).printToString());
			} else if (obj instanceof Report) {
				putErrMsg(((Report) obj).toString());
			} else if (obj instanceof Throwable) {
				STester.this.putErrMsg((Throwable) obj);
			} else {
				putErrMsg(obj.getClass().getName() + ": " + obj.toString());
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
		if ((a1 == null && a2 != null) || (a1 != null && a2 == null)
			|| (a1 == null && a2 == null) || (!a1.equals(a2))) {
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
		if ((a1 != null && a2 != null && !a1.equals(a2))
			|| (a1 != null && a1 == null) || (a1 == null && a1 != null)) {
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
		if ((a1 != null && a2 != null && a1.compareTo(a2) != 0)
			|| (a1 != null && a1 == null) || (a1 == null && a1 != null)) {
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
		if ((a1 != null && a2 != null && a1.compareTo(a2) != 0)
			|| (a1 != null && a1 == null) || (a1 == null && a1 != null)) {
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
		if ((a1 != null && a2 != null && a1.compareTo(a2) != 0)
			|| (a1 != null && a1 == null) || (a1 == null && a1 != null)) {
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
		if ((a1 != null && a2 != null && !a1.equals(a2))
			|| (a1 != null && a1 == null) || (a1 == null && a1 != null)) {
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
	/** Check if the argument is null. If not then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked for true.
	 */
	public final void assertNull(final Object x) {assertNull(x, "" + x);}
	/** Check if the argument is null. If not then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked for true.
	 * @param msg message to be printed or null.
	 */
	public final void assertNull(final Object x, final Object msg) {
		if (x != null) {
			fail(msg);
		}
	}
	/** Check if the argument is not null. If it is null then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked.
	 */
	public final void assertNotNull(final Object x) {assertNotNull(x, "null");}
	/** Check if the argument is not null. If it is null then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked.
	 * @param msg message to be printed or null.
	 */
	public final void assertNotNull(final Object x, final Object msg) {
		if (x == null) {
			fail(msg);
		}
	}
	/** Check if the argument is true. If not then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked for true.
	 */
	public final void assertTrue(final boolean x) {assertFalse(!x, null);}
	/** Check if the argument <code>x</code> is true. If not then invoke
	 * the method <code>fail</code> with the argument msg.
	 * @param x argument to be checked for true.
	 * @param msg message to be printed or null.
	 */
	public final void assertTrue(final boolean x, final Object msg) {
		assertFalse(!x, msg);
	}
	/** Check if the argument is false. If not then invoke the method
	 * <code>fail</code>.
	 * @param x argument to be checked for false.
	 */
	public final void assertFalse(final boolean x) {assertFalse(x, null);}
	/** Check if the argument <code>x</code> is false. If not then invoke
	 * the method <code>fail</code> with the argument msg.
	 * @param x argument to be checked for false.
	 * @param msg message to be printed or null.
	 */
	public final void assertFalse(final boolean x, final Object msg) {
		if (x) {
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
	/** Check if the reporter does not contain an error and clear reporter
	 * after message is reported.
	 * @param reporter the reporter to be checked for no errors.
	 */
	public final void assertNoErrorsAndClear(final ArrayReporter reporter) {
		if (reporter.errors()) {
			fail(reporter.toString());
			reporter.clear();
		}
	}
	/** Check if the reporter does not contain an error or warning. If yes then
	 * invoke the method <code>fail</code> with the argument msg. Clear reporter
	 * after message is reported.
	 * @param reporter the reporter to be checked for no errors and no warnings.
	 */
	public final void assertNoErrorwarningsAndClear(
		final ArrayReporter reporter) {
		if (reporter.errorWarnings()) {
			fail(reporter.toString());
			reporter.clear();
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
	 * the method <code>fail</code> with the argument msg.
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
	 * invoke the method <code>fail</code> with the argument msg.
	 * @param reporter the reporter to be checked for no errors and no warnings.
	 */
	public final void assertNoErrorwarnings(
		final ReportWriter reporter) {
		if (reporter.errorWarnings()) {
			fail(reporter.toString());
		}
	}
	/** Check if the reporter does not contain an error or warning. If yes then
	 * invoke the method <code>fail</code> with the argument msg.
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
	 * @param out The print stream for result information or null
	 * @param err The print stream for error messages or null.
	 * @param log The print stream all messages or null.
	 * @param clazz The class from which the method is called.
	 * @param arguments list of arguments.
	 */
	private void init(final PrintStream out,
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
				ndx = s.indexOf("/build/test/classes/" + cname);
			}
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
				}
			}
		}
		if (!new File(_sourceName).exists()) {
			_sourceName = null;
			_timeStamp = System.currentTimeMillis();
			return;
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
		getTempDir();
		_timeStamp = System.currentTimeMillis();
	}
	/** Run test and print result information.
	 * @param out The print stream for result information or null
	 * @param err The print stream for error messages or null.
	 * @param log The print stream all messages or null.
	 * @param printOK if false then the result if printed only if
	 * an error was reported.
	 * @param arguments array with arguments or null.
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
			String s = "[INFO] " + (_errors == 0
				? "OK " : _errors + " error"+(_errors>1?"s":"") + " in ");
			s += _name + (_resultInfo.isEmpty() ? "" : ", " + _resultInfo)
				+ ", time=" + new DecimalFormat("0.00").format(duration) + "s";
			out.flush();
			System.err.flush();
			if (log != null) {
				log.println(s);
				log.flush();
			}
			out.println(s);
			out.flush();
		}
		return _errors;
	}
	/** Creates the instance of the class from which this method was called.
	 * @return The new instance of the class (from which the method was called).
	 */
	private static STester getInstance() {
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
			Constructor c = Class.forName(className).getDeclaredConstructor(
				new Class<?>[0]);
			c.setAccessible(true);
			return (STester) c.newInstance(new Object[0]);
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
		System.err.flush();
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
	/** Run tests of the object given by argument and print result information.
	 * @param out The print stream for result information or null
	 * @param err The print stream for error messages or null.
	 * @param log The print stream all messages or null.
	 * @param tests The array of objects to be tested.
	 * @param info The information text.
	 * @param printOK if false then the result is printed only if
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
		for (STester x: tests) {
			errors += x.runTest(out, err, log, printOK, args);
		}
		DecimalFormat df = new DecimalFormat("0.00");
		df.setDecimalSeparatorAlwaysShown(true);
		float duration = ((float)((System.currentTimeMillis() - t) / 1000.0));
		System.err.flush();
		out.flush();
		String s = "[INFO] " +
			(errors > 0 ? errors + " error" + (errors > 1 ? "s,": ",") : "OK")
			+ " " + (info != null ? info + ", ": "") +
			"total time: " + df.format(duration) + "s";
		if (log != null) {
			log.println(s);
			log.flush();
		}
		out.println(s);
		out.flush();
		return errors;
	}

	/** Return a string with printable representation of Throwable object.
	 * @param exception the Exception object to be printed.
	 * @return string with printable representation of Throwable.
	 */
	public final static String printThrowable(final Throwable exception) {
		java.io.CharArrayWriter chw = new java.io.CharArrayWriter();
		java.io.PrintWriter pw = new java.io.PrintWriter(chw);
		exception.printStackTrace(pw);
		pw.close();
		return chw.toString();
	}

	/** Add Java sources to parameter list of the Java compiler.
	 * @param f the file or directory.
	 * @param params parameter list of Java compiler.
	 */
	private static void addJavaSource(final File f, final List<String> params) {
		if (f.isDirectory()) {
			for (File x: f.listFiles()) {
				addJavaSource(x, params);
			}
		} else if (f.getName().endsWith(".java")) {
			params.add(f.getAbsolutePath());
		}
	}

	/** Compile sources from parameter and save files to the classes directory
	 *  of tester.
	 * @param classpath the string with classpath.
	 * @param classDir the string with directory where to create classes.
	 * @param files files with Java sources (may be a file or a directory).
	 * @return string with path to compiled classes.
	 */
	public static String compileSources(final String classpath,
		final String classDir,
		final File... files) {
		String sources[] = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			sources[i] = files[i].getAbsolutePath();
		}
		return compileSources(classpath, classDir, sources);
	}

	/** Get string with the path where the class is in the current classpath.
	 * @param clazz the class to be checked.
	 * @return Get string with path where the class is in the current classpath.
	 */
	public static String getClassSource(final Class<?> clazz) {
		String className = clazz.getName().replace('.', '/') + ".class";
		URL u = clazz.getClassLoader().getResource(className);
		String classpath = u.toExternalForm();
		if (classpath.startsWith("jar:file:") && classpath.indexOf('!')>0) {
			classpath = classpath.substring(9,classpath.lastIndexOf('!'));
			return new File(classpath).getAbsolutePath().replace('\\','/');
		} else {
			classpath =
				new File(u.getFile()).getAbsolutePath().replace('\\','/');
			return classpath.substring(0, classpath.indexOf(className));
		}
	}

	/** Compile sources from parameter and save files to the classes directory
	 *  of tester.
	 * @param classpath the string with classpath.
	 * @param classDir the string with directory where to create classes.
	 * @param sources paths of Java sources (may be a file or a directory).
	 * @return string with path to compiled classes.
	 */
	public static String compileSources(final String classpath,
		final String classDir,
		final String... sources) {
		// where are compiled classes of X-definitions
		// prepare parameters
		ArrayList<String> ar = new ArrayList<String>();
		ar.add("-classpath");
		ar.add((classpath.isEmpty() ? "" : classpath + File.pathSeparatorChar)
			+ classDir); // classpath
		ar.add("-d");
		ar.add(classDir); // where to write compiled classes
		// source files
		for (String source: sources) {
			addJavaSource(new File (source), ar);
		}
		// prepare compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			throw new RuntimeException("Java compiler is not available");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		// compile sources
		if (compiler.run(null, out, err, ar.toArray(new String[0])) != 0) {
			try {
				err.write("Compiler params:\n".getBytes());
				for (String s : ar.toArray(new String[0])) {
					err.write((s + '\n').getBytes());
				}
				err.write("End params\n".getBytes());
			} catch (Exception ex) {} // never sould happen
			throw new RuntimeException(
				"Java compilation failed:\n" + err.toString());
		}
		return classDir;
	}
}
