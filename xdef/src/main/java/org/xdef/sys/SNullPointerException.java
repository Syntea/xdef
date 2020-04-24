package org.xdef.sys;

/** Extension of NullPointerException implementing SThrowable interface.
 * @author Vaclav Trojan
 */
public class SNullPointerException extends NullPointerException
	implements SThrowable {

	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SNullPointerException. */
	public SNullPointerException() {
		this(null, "");
	}

	/** Creates a new instance of SNullPointerException.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final Throwable ex) {
		this(null, ex == null ? "" : ex.getMessage(), ex);
	}

	/** Creates a new instance of SNullPointerException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final String msg, final Throwable ex) {
		this(null, msg, ex);
	}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		this(id, msg, mod);
		_cause = ex;
	}

	/** Creates a new instance of SNullPointerException with text message.
	 * @param msg The text of message.
	 */
	public SNullPointerException(final String msg) {
		this(null, msg);
	}

	/** Creates a new instance of SNullPointerException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SNullPointerException(final long registeredID, final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SNullPointerException(final String id,
		final String msg,
		final Object... mod) {
		this(Report.error(id, msg, mod));
	}

	/** Creates a new instance of SNullPointerException.
	 * @param report The Report object.
	 */
	public SNullPointerException(final Report report) {
		super(SException.getMsg(report));
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
	}

	/** Creates a new instance of SNullPointerException.
	 * @param report The Report object.
	 * @param ex The object Throwable which caused the error.
	 */
	public SNullPointerException(final Report report, final Throwable ex) {
		this(report);
		_cause = ex;
	}

	@Override
	/** Set cause of exception.
	 * @param cause The Throwable object with cause data.
	 */
	public void setCause(final Throwable cause) {
		_cause = cause;
	}

	@Override
	/** Get cause of exception. If cause was not set return <tt>null</tt>.
	 * @return cause The Throwable object with cause data.
	 */
	public Throwable getCause() {
		return _cause;
	}

	@Override
	/** Get Report object associated with this exception.
	 * @return The Report object.
	 */
	public Report getReport() {
		return Report.error(_msgID, _text, _modification);
	}

	@Override
	/** Get id of message.
	 * @return The message id (may be <tt>null</tt>).
	 */
	public String getMsgID() {
		return _msgID;
	}

	@Override
	/** Creates a message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getMessage() {
		if (_msgID == null) {
			return Report.text(null, _text, _modification).toString();
		}
		return getReport().toString();
	}

	@Override
	/** Creates a localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getLocalizedMessage() {
		if (_msgID == null) {
			return Report.text(null, _text, _modification).toString();
		}
		return getReport().toString();
	}

}