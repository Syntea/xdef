package org.xdef.sys;

/** Extension of java.lang.Exception with SThrowable interface.
 * Constructors allows to create SException object with parameters for
 * associated Report objects.
 * @author  Vaclav Trojan
 */
public class SException extends Exception implements SThrowable {

	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SException.
	 * @param ex The object which caused the error.
	 */
	public SException(Throwable ex) {
		this();
		_cause = ex;
	}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SException(final String msg, final Throwable ex) {
		this(null, msg, ex);
	}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SException(final long id,
		final Throwable ex,
		final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param modif The modification string.
	 * @param ex The object which caused the error.
	 */
	public SException(final String id,
		final String msg,
		final Throwable ex,
		final Object... modif) {
		this(Report.error(id, msg, modif), ex);
	}

	/** Creates a new instance of SException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public SException(final Report report, final Throwable ex) {
		this(report);
		_cause = ex;
	}

	/** Creates a new instance of SException. */
	public SException() {
		this(null, "");
	}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 */
	public SException(final String msg) {
		this(null, msg);
	}

	/** Creates a new instance of SException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SException(final long registeredID, final Object... mod) {
		this(Report.error(registeredID, mod));
	}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SException(final String id, final String msg, final Object... mod){
		this(Report.error(id, msg, mod));
		_cause = null;
	}

	/** Creates a new instance of SException.
	 * @param report The Report object.
	 */
	public SException(final Report report) {
		super(SException.getMsg(report));
		_msgID = report.getMsgID();
		_text = report.getText();
		_modification = report.getModification();
		_cause = null;
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
	/** Creates string with message from this exception.
	 * @return The text of message.
	 */
	public String getMessage() {
		if (_msgID == null) {
			return Report.text(null,_text,_modification).toString();
		}
		return getReport().toString();
	}

	@Override
	/** Creates string with localized text of message from this exception.
	 * @return The text of message.
	 */
	public String getLocalizedMessage() {
		if (_msgID == null) {
			return Report.text( null,_text,_modification).toString();
		}
		return getReport().toString();
	}

	public static String getMsg(final String id,
		final String msg,
		final Object... mod) {
		return Report.error(id, msg).getText();
	}

	public static String getMsg(Report report) {
		if (report == null) {
			return "";
		}
		if (report.getType() == Report.TEXT) {
			return report.getText();
		}
		if (report.getMsgID() == null) {
			return "";
		}
		return report.toString();
	}

}