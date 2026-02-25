package org.xdef.web;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.xdef.sys.ReportPrinter;
import org.xdef.sys.ReportReader;
import org.xdef.sys.SManager;

/** Abstract servlet used for servlet implementation.
 * @author Vaclav Trojan
 */
@MultipartConfig
public abstract class AbstractMyServlet extends HttpServlet {
	private static final long serialVersionUID = -8154631839408075000L;
    /** Base directory. */
	protected static File _baseDir = null;
	/** Temporary directory for 100MB. */
	protected static File _tempDir100MB = null;
	/** Temporary directory for 400MB. */
	protected static File _tempDir400MB = null;
	/** Directory with data. */
	protected static File _dataDir = null;
	/** SManager used for reporting.*/
	static final SManager MANAGER = SManager.getInstance();

	/** Get listing from reporter.
	 * @param reporter reporter with error and warning messages.
	 * @param data string with source data.
	 * @return string with listing form of source data.
	 */
	public static final String printReports(final ReportReader reporter, final String data) {
		Writer writer = new CharArrayWriter();
		Reader car = new CharArrayReader(data.toCharArray());
		ReportPrinter.printListing(writer,car,reporter,null,90,false,"eng");
		return writer.toString();
	}

	/** Create string from data which can be part of HTML.
	 * @param data string to be converted.
	 * @param pre if true new lines are converted to "&amp;br/>".
	 * @return data convertod be displayed in HTML.
	 */
	public static final String stringToHTml(final String data, final boolean pre) {
		StringBuilder sb = new StringBuilder(null == data ? "" : data.trim());
		int i = 0;
		while (i < sb.length()) {
			char c = sb.charAt(i);
			if (!pre && '\r' == c) {
				sb.deleteCharAt(i);
				continue;
			}
			if (!pre && '\n' == c) {
				sb.insert(i, "<br/>");
				i += 6;
			} else if (!pre && ' ' == c) {
				sb.replace(i, i + 1, "&nbsp;");
				i += 5;
			} else if (!pre && '\t' == c) {
				sb.replace(i, i + 1, "&nbsp;&nbsp;&nbsp;"); //three spaces
				i += 17;
			} else if ('<' == c) {
				sb.replace(i, i + 1, "&lt;");
				i += 3;
			} else if ('&' == c) {
				sb.replace(i, i + 1, "&amp;");
				i += 4;
			} else if ('>' == c) {
				sb.replace(i, i + 1, "&gt;");
				i += 3;
			}
			i++;
		}
		return sb.toString();
	}

	/** Get parameter from servlet request.
	 * @param request servlet request.
	 * @param name name of parameter.
	 * @return trimmed value of parameter or an empty string.
	 */
	public static final String getParam(final HttpServletRequest request, final String name) {
		String result = request.getParameter(name);
		return null == result ? "" : result.trim();
	}


	/** Delete all files and subdirectories from argument.
	 * @param dir directory to be cleared.
	 */
	public static final void clearDirectory(final File dir) {
		if (dir != null && dir.isDirectory()) {
			deleteFiles(dir.listFiles());
		}
	}

	/** Delete all files and subdirectories from argument.
	 * @param files files and directories to be cleared.
	 */
	public static final void deleteFiles(final File[] files) {
		for (File f: files) {
			if (f.exists()) {
				if (f.isDirectory()) {
					clearDirectory(f);
				}
				f.delete();
			}
		}
	}

	/** This class implements thread in which runs XDDocument.
	 * We need to run it in the separate thread to prevent long servlet
	 * response.
	 */
	private static class ProcReq extends Thread {
		private final AbstractMyServlet _x;
		private final HttpServletRequest _request;
		private final HttpServletResponse _response;
		private boolean _finished = false;
		private Exception _exception = null;

		ProcReq(final HttpServletRequest request, final HttpServletResponse response, AbstractMyServlet x) {
			_request = request;
			_response = response;
			_exception = null;
			_finished = false;
			_x = x;
			setPriority(Thread.MAX_PRIORITY);
		}

////////////////////////////////////////////////////////////////////////////////
// implementation of HttpServlet methods
////////////////////////////////////////////////////////////////////////////////
		/** Run servlet. */
		@Override
		public final void run() {
			try {
				_x.procReq(_request, _response);
			} catch (Error ex) {
				_exception = new Exception(ex.toString());
				_exception.setStackTrace(new StackTraceElement[0]);
			} catch (IOException | ServletException | RuntimeException ex) {
				_exception = ex;
			}
			synchronized(this) {
				_finished = true;
				notify();
			}
		}
		synchronized boolean isFinished() {return _finished;}
		synchronized Exception getException() {return _exception;}
	}

	/** Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * @param request servlet request.
	 * @param response servlet response.
	 * @throws ServletException if an error occurs.
	 * @throws IOException if IO error occurs.
	 */
	private void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		ProcReq p = new ProcReq(request, response, this);
		synchronized(p) {
			IOException ioex = null;
			try {
				p.start();
				p.join(25000);
				if (!p.isFinished()) {
					p.interrupt(); //???
					ioex = new IOException("Interrupted - timeout");
					ioex.setStackTrace(new StackTraceElement[0]);
				}
				if (null != p.getException()) {
					ioex = new IOException(p.getException().toString());
				}
			} catch (InterruptedException ex) {
				ioex = new IOException("Interrupted", ex);
			}
			if (null != ioex) {
				throw ioex;
			}
		}
	}

	/** Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws javax.servlet.ServletException if servlet error occurs.
	 * @throws java.io.IOException if IO error occurs.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	/** Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws javax.servlet.ServletException if servlet error occurs.
	 * @throws java.io.IOException if IO error occurs.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		processRequest(request, response);
	}

	/** Init this servlet. Set directories for temporary data. */
	@Override
	public void init() {
		File f = new File("/opt/tutorial");
		if (!f.exists() || !f.isDirectory()) {
			throw new RuntimeException( "Directory /opt/tutorial is not available");
		}
		_baseDir = f;
		f = new File(_baseDir, "temp100MB");
		if (!f.exists() || !f.isDirectory()) {
			throw new RuntimeException("Directory /opt/tutorial/temp100MB is not available");
		}
		_tempDir100MB = f;
		f = new File(_baseDir, "temp400MB");
		if (!f.exists() || !f.isDirectory()) {
			throw new RuntimeException("Directory /opt/tutorial/temp400MB is not available");
		}
		_tempDir400MB = f;
		f = new File(_baseDir, "data");
		if (!f.exists() || !f.isDirectory()) {
			throw new RuntimeException("Directory /opt/tutorial/data is not available");
		}
		_dataDir = f;
	}

	public final static String genHtmlMessage(final String title, final String body) {
		return "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
"  <head>\n" +
"     <meta http-equiv='content-type' content='text/html; charset=UTF-8'/>\n"+
"     <title>" + title + "</title>\n" +
"  </head>\n" +
"  <body>\n" +
"    <h1>" + title + "</h1>\n" + body +
"  </body>\n" +
"</html>";
	}
////////////////////////////////////////////////////////////////////////////////
// Abstract methods
////////////////////////////////////////////////////////////////////////////////

	/** Returns a short description of this servlet.
	 * @return short description of this servlet.
	 */
	@Override
	abstract public String getServletInfo();

	/** Processes requests.
	 * @param req servlet request.
	 * @param resp servlet response.
	 * @throws javax.servlet.ServletException if servlet error occurs.
	 * @throws IOException if a IO error occurs.
	 */
	public abstract void procReq(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException,IOException;
}