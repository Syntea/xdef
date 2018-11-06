package org.xdef.sys;

import java.io.PrintStream;
import java.io.PrintWriter;

/** Interface for exceptions extended with Syntea reporting tools.
 * @author Trojan
 */
public interface SThrowable {

	/** Set cause of exception.
	 * @param cause Throwable object.
	 */
	public void setCause(final Throwable cause);

	/** Get cause of exception. If cause was not set return <tt>null</tt>.
	 * @return cause The Throwable object with cause data.
	 */
	public Throwable getCause();

	/** Get Report object associated with this implementation.
	 * @return Report object.
	 */
	public Report getReport();

	/** Get id of message.
	 * @return The message id (may be <tt>null</tt>).
	 */
	public String getMsgID();

	/** Creates string with message from  Report object.
	 * @return text of message.
	 */
	public String getMessage();

	/** Creates the localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getLocalizedMessage();

	/* Print stacktrace on PrintStream.
	 * @param out PrintStream where stack trace is printed.
	 */
	public void printStackTrace(PrintStream out);

	/* Print stacktrace on PrintWriter.
	 * @param out PrintWriter where stack trace is printed.
	 */
	public void printStackTrace(PrintWriter out);

	/* Print stacktrace on System.err stream. */
	public void printStackTrace();

}