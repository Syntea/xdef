package org.xdef.proc;

import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;

/** Extension of java.lang.RuntimeException with SThrowable interface.
 * @author  Vaclav Trojan
 */
public class XXException extends SRuntimeException {

	/** Creates a new instance of XXException.
	 * @param ex The object which caused the error.
	 */
	public XXException(final Throwable ex) {super(null, "", ex);}

	/** Creates a new instance of XXException with text message.
	 * @param msg The text of message.
	 * @param ex The object which caused the error.
	 */
	public XXException(final String msg, final Throwable ex) {super(null, msg, ex);}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 * @param ex The object which caused the error.
	 */
	public XXException(final String id, final String msg, final Throwable ex, final Object... mod) {
		super(Report.error(id, msg, ex, mod));
	}

	/** Creates a new instance of XXException.
	 * @param report The Report object.
	 * @param ex The object which caused the error.
	 */
	public XXException(final Report report, final Throwable ex) {super(report, ex);}

	/** Creates a new instance of XXException. */
	public XXException() {super(null, "");}

	/** Creates a new instance of XXException with text message.
	 * @param msg The text of message.
	 */
	public XXException(final String msg) {super(null, msg);}

	/** Creates a new instance of XXException with registered message.
	 * @param registeredID registered message ID.
	 * @param mod Message modification parameters.
	 */
	public XXException(final long registeredID, final Object... mod) {super(Report.error(registeredID, mod));}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 */
	public XXException(final String id, final String msg) {super(Report.error(id, msg));}

	/** Creates a new instance of XXException.
	 * @param id The message ID
	 * @param msg The text of message.
	 * @param mod Message modification parameters.
	 */
	public XXException(final String id, final String msg, final Object... mod) {
		super(Report.error(id, msg, mod));
	}

	/** Creates a new instance of XXException.
	 * @param report The Report object.
	 */
	public XXException(final Report report) {super(report);}
}