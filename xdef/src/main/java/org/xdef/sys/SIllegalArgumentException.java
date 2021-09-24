package org.xdef.sys;

/** Extension of IllegalArgumentException implementing SThrowable.
 * @author Vaclav Trojan
 */
public class SIllegalArgumentException
	extends IllegalArgumentException implements SThrowable {
	/** This constant is used in the ObjectStream reader/writer. */
	private static final long serialVersionUID = -3440796080459280150L;
	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of <code>IllegalArgumentException</code>
	 * without detail message.
	 */
	public SIllegalArgumentException() {this("");}

	/** Constructs an instance of <code>SIllegalArgumentException</code>
	 * with the specified detail message.
	 * @param msg the detail message.
	 */
	public SIllegalArgumentException(final String msg) {this(null, msg);}

	/** Constructs a new exception with the specified cause and a detail
	 * message of (cause==null ? null : cause.toString()) (which typically
	 * contains the class and detail message of cause). This constructor is
	 * useful for exceptions that are little more than wrappers for other
	 * throwables (for example, PrivilegedActionException).
	 * with the specified detail message.
	 * @param cause the cause (which is saved for later retrieval by the
	 * Throwable.getCause() method). (A null value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 */
	public SIllegalArgumentException(final Throwable cause) {
		this(null, cause == null ? "" : cause.getMessage(), cause);
	}

	/** Constructs a new exception with the specified detail message and cause.
	 * @param msg the detail message.
	 * @param cause - the cause (which is saved for later retrieval by the
	 * Throwable.getCause() method). (A null value is permitted, and indicates
	 * that the cause is nonexistent or unknown.)
	 */
	public SIllegalArgumentException(final String msg, final Throwable cause) {
		this(null, msg, cause);
	}

	/** Creates a new instance of SIllegalArgumentException with registered
	 * message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SIllegalArgumentException(final long registeredID,
		final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of SIllegalArgumentException.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SIllegalArgumentException(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of IllegalArgumentException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SIllegalArgumentException(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		this(id, msg, mod);
		_cause = ex;
	}

	/** Creates a new instance of IllegalArgumentException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SIllegalArgumentException(final String id,
		final String msg,
		final Object... mod) {
		this(Report.error(id, msg, mod));
	}

	/** Creates a new instance of IllegalArgumentException.
	 * @param report The Report object.
	 */
	public SIllegalArgumentException(final Report report) {
		super(SException.getMsg(report));
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
	}

	/** Creates a new instance of IllegalArgumentException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public SIllegalArgumentException(final Report report, Throwable ex) {
		this(report);
		_cause = ex;
	}

	@Override
	/** Set cause of exception.
	 * @param cause The object with cause data.
	 */
	public final void setCause(final Throwable cause) {_cause = cause;}

	@Override
	/** Get cause of exception. If cause was not set return <i>null</i>.
	 * @return cause The object with cause data.
	 */
	public final Throwable getCause() {return _cause;}

	@Override
	/** Set Report message.
	 * @param report Report of this object.
	 */
	public final void setReport(final Report report) {
		_msgID = report.getMsgID();
		_modification = report.getModification();
		_text = report.getText();
	}

	@Override
	/** Get Report object associated with this exception.
	 * @return The Report object.
	 */
	public final Report getReport() {
		return Report.error(_msgID, _text, _modification);
	}

	@Override
	/** Get id of message.
	 * @return The message id (may be <i>null</i>).
	 */
	public final String getMsgID() {return _msgID;}

	@Override
	/** Creates a message assigned to this exception.
	 * @return The text of localized message.
	 */
	public final String getMessage() {
		return _msgID == null
			? Report.text(null, _text, _modification).toString()
			: getReport().toString();
	}

	@Override
	/** Creates a localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public final String getLocalizedMessage() {return getMessage();}
}