package org.xdef.sys;

import java.io.IOException;

/** Extension of java.io.IOException with SThrowable interface.
 *
 * @author Trojan
 */
public class SIOException extends IOException implements SThrowable {

	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SIOException. */
	public SIOException() {
		this(null, "");
	}

	/** Creates a new instance of SIOException with cause.
	 * @param ex cause of exception.
	 */
	public SIOException(Throwable ex) {
		this(ex.getMessage() == null ? "" : ex.getMessage(), ex);
	}

	/** Creates a new instance of SIOException with text message.
	 * @param msg The text of message.
	 */
	public SIOException(final String msg) {
		this(null, msg);
	}

	/** Creates a new instance of SIOException with text message.
	 * @param msg The text of message.
	 * @param ex cause of exception.
	 */
	public SIOException(final String msg, Throwable ex) {
		this(null, msg);
		_cause = ex;
	}

	/** Creates a new instance of SIOException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SIOException(final long registeredID, final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of SIOException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SIOException(final String id, final String msg, final Object... mod){
		this(Report.error(id, msg, mod));
	}

	/** Creates a new instance of SExternalError.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SIOException(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SIOException.
	 * @param rep The Report object.
	 */
	public SIOException(final Report rep) {
		super(SException.getMsg(rep), null);
	}

	/** Creates a new instance of SIOException.
	 * @param rep The Report object.
	 * @param ex The object which caused the error.
	 */
	public SIOException(final Report rep, final Throwable ex) {
		super(SException.getMsg(rep));
		_msgID = rep.getMsgID();
		_text = rep.getText();
		_modification = rep.getModification();
		_cause = ex;
	}

	@Override
	/** Set cause of exception.
	 * @param cause The object with cause data.
	 */
	public void setCause(final Throwable cause) {
		_cause = cause;
	}

	@Override
	/** Get cause of exception. If cause was not set return <tt>null</tt>.
	 * @return cause The object with cause data.
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
	/** Creates the message assigned to this exception.
	 * @return The text of localized message.
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