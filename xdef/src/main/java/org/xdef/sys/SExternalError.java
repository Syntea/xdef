package org.xdef.sys;

import java.lang.reflect.InvocationTargetException;

/** Extension of java.lang.Error with SThrowable interface.
 * @author Vaclav Trojan
 */
public class SExternalError extends SError implements SThrowable {

	/** Creates a new instance of SExternalError.
	 * @param ex The object which caused the error.
	 */
	SExternalError(final Throwable ex) {
		this(null, ex.getMessage() == null ? "" : ex.getMessage(), ex);
	}

	/** Creates a new instance of SExternalError with registered message.
	 * @param registeredID registered message ID.
	 * @param ex The object which caused the error.
	 * @param mod Message modification parameters.
	 */
	public SExternalError(final long registeredID,
		final Throwable ex,
		final Object... mod) {
		super(Report.fatal(registeredID, mod), ex);
	}

	/** Creates a new instance of SExternalError with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SExternalError(final String msg, final Throwable ex) {
		this(null, msg, ex);
	}

	/** Creates new instance of SExternalError with modified registered message.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 * @param mod Message modification parameters.
	 */
	public SExternalError(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		super(id, msg, mod);
		if (ex != null && ex instanceof InvocationTargetException) {
			setCause(((InvocationTargetException) ex).getTargetException());
		} else {
			setCause(ex);
		}
	}

	/** Creates a new instance of SExternalError with report.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public SExternalError(final Report report, final Throwable ex) {
		super(report);
		if (ex != null && ex instanceof InvocationTargetException) {
			setCause(((InvocationTargetException) ex).getTargetException());
		} else {
			setCause(ex);
		}
	}

	@Override
	/** Creates the localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getLocalizedMessage() {
		return super.getLocalizedMessage()
			+ (getCause() != null
				? "; Caused by " + getCause().getClass().getName()
				: "");
	}

	@Override
	/** Creates the message assigned to this exception.
	 * @return The text of message.
	 */
	public String getMessage() {
		return super.getMessage()
			+ (getCause() != null
				? "; Caused by " + getCause().getClass().getName()
				: "");
	}

}