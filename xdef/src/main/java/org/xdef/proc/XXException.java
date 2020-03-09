package org.xdef.proc;

import org.xdef.sys.Report;
import org.xdef.sys.SException;
import org.xdef.sys.SThrowable;
import java.io.PrintStream;
import java.io.PrintWriter;

/** Extension of java.lang.RuntimeException with SThrowable interface.
 * @author  Vaclav Trojan
 */
public class XXException extends RuntimeException implements SThrowable {
	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	public String _id;
	/** Report text (may be null). */
	public String _text;
	/** Report modification (may be null). */
	public String _modification;

	/** Creates a new instance of XXException.
	 * @param ex The object which caused the error.
	 */
	public XXException(final Throwable ex) {this(null, "", ex);}

	/** Creates a new instance of XXException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public XXException(final String msg, final Throwable ex) {
		this(null, msg, ex);
	}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public XXException(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, msg, mod));
		setCause(ex);
	}

	/** Creates a new instance of XXException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public XXException(final Report report, final Throwable ex) {
		this(report);
		setCause(ex);
	}

	/** Creates a new instance of XXException. */
	public XXException() {this(null, "");}

	/** Creates a new instance of XXException with text message.
	 * @param msg The text of message.
	 */
	public XXException(final String msg) {this(null, msg);}

	/** Creates a new instance of XXException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public XXException(final long registeredID, final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 */
	public XXException(final String id, final String msg) {
		this(Report.error(id, msg));
	}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public XXException(final String id, final String msg, final Object... mod) {
		this(Report.error(id, msg, mod));
	}

	/** Creates a new instance of XXException.
	 * @param report The Report object.
	 */
	public XXException(final Report report) {
		super(SException.getMsg(report));
		_id = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
	}

	@Override
	/** Set cause of exception.
	 * @param cause The object with cause data.
	 */
	public final void setCause(final Throwable cause) {_cause = cause;}

	@Override
	/** Get cause of exception. If cause was not set return <tt>null</tt>.
	 * @return cause The object with cause data.
	 */
	public Throwable getCause() {return _cause;}

	@Override
	/** Get Report object associated with this exception.
	 * @return The Report object.
	 */
	public Report getReport() {return Report.error(_id, _text, _modification);}

	@Override
	/** Get error id.
	 * @return The id.
	 */
	public String getMsgID() {return _id;}

	@Override
	/** Creates a message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getMessage() {
		if (_id == null) {
			return Report.text(null, _text, _modification).toString();
		}
		return getReport().toString();
	}

	@Override
	/** Creates a localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getLocalizedMessage() {
		if (_id == null) {
			return Report.text(null, _text, _modification).toString();
		}
		return getReport().toString();
	}

	@Override
	/* Print stacktrace on stream.
	 * @param out The PrintStream where stack trace is printed.
	 */
	public void printStackTrace(final PrintStream out) {
		super.printStackTrace(out);
		if (_cause != null) {
			out.println(" caused:");
			_cause.printStackTrace(out);
		}
	}

	@Override
	/* Print stacktrace on stream.
	 * @param out The PrintWriter where stack trace is printed.
	 */
	public void printStackTrace(final PrintWriter out) {
		super.printStackTrace(out);
		if (_cause != null) {
			out.println(" caused:");
			_cause.printStackTrace(out);
		}
	}

	@Override
	/* Print stacktrace on System.err stream. */
	public void printStackTrace() {printStackTrace(System.err);}

}