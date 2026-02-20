package org.xdef.sys;

import java.text.ParseException;

/** Extension of java.text.ParseException with SThrowable interface.
 * Constructors allows to create SException object with parameters for
 * associated Report objects.
 * @author  Vaclav Trojan
 */
public class SParseException extends ParseException implements SThrowable {
    /** This constant is used in the ObjectStream reader/writer. */
    private static final long serialVersionUID = 8969711745297269596L;
    /** Cause of exception. */
    private Throwable _cause;
    /** Report identifier (may be null). */
    private String _msgID;
    /** Report text (may be null). */
    private String _text;
    /** Report modification (may be null). */
    private String _modification;

    /** Creates a new instance of SException.
     * @param errorOffset offset of error.
     */
    public SParseException(final int errorOffset) {this(null, "", errorOffset);}

    /** Creates a new instance of SException with text message.
     * @param msg The text of message.
     * @param ex the cause.
     * @param errorOffset the position where the error is found while parsing.
     */
    public SParseException(final String msg, final Throwable ex, final int errorOffset) {
        this(null, msg, ex, errorOffset);
    }

    /** Creates a new instance of SParseException.
     * @param id The message ID
     * @param ex The object which caused the error.
     * @param errorOffset the position where the error is found while parsing.
     * @param mod Message modification parameters.
     */
    public SParseException(final long id, final Throwable ex, final int errorOffset, final Object... mod) {
        this(Report.error(id, mod), ex, errorOffset);
    }

    /** Creates a new instance of SException.
     * @param id The message ID
     * @param msg The text of message.
     * @param ex the cause.
     * @param errorOffset the position where the error is found while parsing.
     * @param mod Message modification parameters.
     */
    public SParseException(final String id,
        final String msg,
        final Throwable ex,
        final int errorOffset,
        final Object... mod) {
        this(id, msg, errorOffset, mod);
        _cause = ex;
    }

    /** Creates a new instance of SException.
     * @param report The Report object.
     * @param ex the cause.
     * @param errorOffset the position where the error is found while parsing.
     */
    public SParseException(final Report report, final Throwable ex, final int errorOffset) {
        this(report, errorOffset);
        _cause = ex;
    }

    /** Creates a new instance of SException with text message.
     * @param msg The text of message.
     * @param errorOffset the position where the error is found while parsing.
     */
    public SParseException(final String msg, final int errorOffset) {this(null, msg, errorOffset);}

    /** Creates a new instance of SParseException with registered message.
     * @param id registered message ID.
     * @param offset the position where the error is found while parsing.
     * @param mod Message modification parameters.
     */
    public SParseException(final long id, final int offset, final Object... mod) {
        this(Report.error(id, mod), offset);
    }

    /** Creates a new instance of SException.
     * @param id The message ID
     * @param msg The text of message.
     * @param offset the position where the error is found while parsing.
     * @param mod Message modification parameters.
     */
    public SParseException(final String id, final String msg, final int offset, final Object... mod) {
        this(Report.error(id, msg, mod), offset);
    }

    /** Creates a new instance of SException.
     * @param report The Report object.
     * @param errorOffset the position where the error is found while parsing.
     */
    public SParseException(final Report report, int errorOffset) {
        super(SException.getMsg(report), errorOffset);
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
    public final void setReport(final Report report) {
        _msgID = report.getMsgID();
        _modification = report.getModification();
        _text = report.getText();
    }

    @Override
    /** Get Report object associated with this exception.
     * @return The Report object.
     */
    public final Report getReport() { return Report.error(_msgID, _text, _modification);}

    @Override
    /** Get id of message.
     * @return The message id (may be <i>null</i>).
     */
    public final String getMsgID() {return _msgID;}

    @Override
    /** Creates string with message from this exception.
     * @return The text of message.
     */
    public final String getMessage() {
        return _msgID == null ? Report.text(null,_text,_modification).toString() : getReport().toString();
    }

    @Override
    /** Creates a localized message assigned to this exception.
     * @return The text of localized message.
     */
    public final String getLocalizedMessage() {return getMessage();}
}