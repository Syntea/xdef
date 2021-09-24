package org.xdef.sys;

/** Extension of java.lang.RuntimeException with SThrowable interface.
 * @author  Vaclav Trojan
 */
public class SRuntimeException extends RuntimeException implements SThrowable {
	/** This constant is used in the ObjectStream reader/writer. */
	private static final long serialVersionUID = -55319058851477379L;
	/** Cause of exception. */
	private Throwable _cause;
	/** Message identifier (may be null).*/
	private String _msgID;
	/** Report text (may be null).*/
	private String _text;
	/** Report modification (may be null).*/
	private String _modification;

	/** Creates a new instance of SException.
	 * @param ex The object which caused the error.
	 */
	public SRuntimeException(final Throwable ex) {this(null, "", ex);}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SRuntimeException(final String msg, final Throwable ex) {
		this(null, msg, ex);
	}

	/** Creates a new instance of SRuntimeException.
	 * @param id The message ID
	 * @param ex The object which caused the error.
	 * @param mod Message modification parameters.
	 */
	public SRuntimeException(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SException. */
	public SRuntimeException() {this(null, "");}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param ex The object Throwable which caused the error.
	 * @param mod Message modification parameters.
	 */
	public SRuntimeException(final String id,
		final String msg,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, msg, mod));
		_cause = ex;
	}

	/** Creates a new instance of SException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public SRuntimeException(final Report report, final Throwable ex) {
		this(report);
		_cause = ex;
	}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 */
	public SRuntimeException(final String msg) {this(null, msg);}

	/** Creates a new instance of SRuntimeException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SRuntimeException(final long registeredID, final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SRuntimeException(final String id,
		final String msg,
		final Object... mod) {
		this(Report.error(id, msg, mod));
	}

	/** Creates a new instance of SException.
	 * @param report The Report object.
	 */
	public SRuntimeException(final Report report) {
		super(SException.getMsg(report));
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
	}

	@Override
	/** Set cause of exception.
	 * @param cause The object with cause data.
	 */
	public final void setCause(final Throwable cause) {_cause = cause;}

	@Override
	/** Get cause of exception. If cause was not set return <i>null</i>.
	 * @return cause The bject with cause data.
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
	 * @return The text of message.
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