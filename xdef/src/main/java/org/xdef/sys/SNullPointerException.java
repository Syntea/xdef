package org.xdef.sys;

/** Extension of NullPointerException implementing SThrowable.
 * @author Vaclav Trojan
 */
public class SNullPointerException extends NullPointerException implements SThrowable {
	/** This constant is used in the ObjectStream reader/writer. */
	private static final long serialVersionUID = 8580697295102513610L;
	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SNullPointerException. */
	public SNullPointerException() {this(null, "");}

	/** Creates a new instance of SNullPointerException.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final Throwable ex) {this(null, ex == null ? "" : ex.getMessage(), ex);}

	/** Creates a new instance of SNullPointerException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final String msg, final Throwable ex) {this(null, msg, ex);}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final long id, final Throwable ex, final Object... mod) {
		this(Report.error(id, mod), ex);
	}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SNullPointerException(final String id, final String msg, final Throwable ex, final Object... mod) {
		this(id, msg, mod);
		_cause = ex;
	}

	/** Creates a new instance of SNullPointerException with text message.
	 * @param msg The text of message.
	 */
	public SNullPointerException(final String msg) {this(null, msg);}

	/** Creates a new instance of SNullPointerException with registered message.
	 * @param ID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SNullPointerException(final long ID, final Object... mod) {this(Report.error(ID, mod));}

	/** Creates a new instance of SNullPointerException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SNullPointerException(final String id, final String msg, final Object... mod) {
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
	public SNullPointerException(final Report report, final Throwable ex) {this(report); _cause = ex;}

	@Override
	/** Set cause of exception.
	 * @param cause The Throwable object with cause data.
	 */
	public final void setCause(final Throwable cause) {_cause = cause;}

	@Override
	/** Get cause of exception. If cause was not set return <i>null</i>.
	 * @return cause The Throwable object with cause data.
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
	public final Report getReport() {return Report.error(_msgID, _text, _modification);}

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
		return _msgID == null ? Report.text(null, _text, _modification).toString() : getReport().toString();
	}

	@Override
	/** Creates a localized message assigned to this exception.
	 * @return The text of localized message.
	 */
	public final String getLocalizedMessage() {return getMessage();}
}