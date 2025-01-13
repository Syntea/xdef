package org.xdef.sys;

/** Extension of java.lang.Exception with SThrowable interface.
 * Constructors allows to create SException object with parameters for
 * associated Report objects.
 * @author  Vaclav Trojan
 */
public class SException extends Exception implements SThrowable {
	/** This constant is used in the ObjectStream reader/writer. */
	private static final long serialVersionUID = -3077960719717339686L;
	/** Cause of exception. */
	private Throwable _cause;
	/** Report identifier (may be null). */
	private String _msgID;
	/** Report text (may be null). */
	private String _text;
	/** Report modification (may be null). */
	private String _modification;

	/** Creates a new instance of SException. */
	public SException() {this(null, "");}

	/** Creates a new instance of SException.
	 * @param ex The object which caused the error.
	 */
	public SException(final Throwable ex) {this(); _cause = ex;}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public SException(final String msg, final Throwable ex) {this(null,msg,ex);}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public SException(final long id,final Throwable ex,final Object... mod) {this(Report.error(id,mod), ex);}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param modif The modification string.
	 * @param ex The object which caused the error.
	 */
	public SException(final String id, final String msg, final Throwable ex, final Object... modif) {
		this(Report.error(id, msg, modif), ex);
	}

	/** Creates a new instance of SException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public SException(final Report report, final Throwable ex) {this(report); _cause = ex;}

	/** Creates a new instance of SException with text message.
	 * @param msg The text of message.
	 */
	public SException(final String msg) {this(null, msg);}

	/** Creates a new instance of SException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public SException(final long registeredID, final Object... mod) {this(Report.error(registeredID, mod));}

	/** Creates a new instance of SException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public SException(final String id, final String msg, final Object... mod) {
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
	public final void setReport(Report report) {
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

	public static String getMsg(final String id, final String msg, final Object... mod) {
		return Report.error(id, msg).getText();
	}

	public static String getMsg(Report report) {
		if (report == null) {
			return "";
		}
		if (report.getType() == Report.TEXT) {
			return report.getText();
		}
		return report.getMsgID() == null ? "" : report.toString();
	}
}