package org.xdef.sys;

/** Extension of java.lang.Error with implementation of SThrowable interface.
 * @author  Vaclav Trojan
 */
public class SError extends Error implements SThrowable {

	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 * @param ex cause object.
	 */
	public SError(final String msg, final Throwable ex) {
		this(Report.fatal(null, msg), ex);
	}

	/** Creates a new instance of SException with modified registered message.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex cause object.
	 */
	public SError(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		this(Report.fatal(id, msg, mod), ex);
	}

	/** Creates a new instance of SError.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SError(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SException with report.
	 * @param report The Report object.
	 * @param ex cause object.
	 */
	public SError(final Report report, final Throwable ex) {
		super(SException.getMsg(report), ex);
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
		_cause = ex;
	}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 */
	public SError(final String msg) {
		this(null, msg);
	}

	/** Creates a new instance of SError with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SError(final long registeredID, final Object... mod) {
		this(Report.fatal(registeredID, mod));
	}

	/** Creates a new instance of SException with modified registered message.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SError(final String id, final String msg, final Object... mod) {
		this(Report.fatal(id, msg, mod));
	}

	/** Creates a new instance of SException with report.
	 * @param report The Report object.
	 */
	public SError(final Report report) {
		super(SException.getMsg(report));
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
	}

	@Override
	/** Set cause of exception.
	 * @param cause The object with cause data.
	 */
	public void setCause(final Throwable cause) {_cause = cause;}

	@Override
	/** Get cause of exception. If cause was not set return <tt>null</tt>.
	 * @return cause The object with cause data.
	 */
	public Throwable getCause() {return _cause;}

	@Override
	/** Get Report object associated with this exception.
	 * @return The Report object.
	 */
	public Report getReport() {
		return Report.fatal(_msgID, _text, _modification);
	}

	@Override
	/** Get id of message.
	 * @return The message id (may be <tt>null</tt>).
	 */
	public String getMsgID() {return _msgID;}

	@Override
	/** Creates the message assigned to this exception.
	 * @return The text of message.
	 */
	public String getMessage() {
		if (_msgID == null) {
			return Report.text(null,_text,_modification).toString();
		}
		return getReport().toString();
	}

	@Override
	/** Creates the localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public String getLocalizedMessage() {
		if (_msgID == null) {
			return Report.text(null,_text,_modification).toString();
		}
		return getReport().toString();
	}

}