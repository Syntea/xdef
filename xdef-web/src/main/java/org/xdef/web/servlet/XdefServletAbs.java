package org.xdef.web.servlet;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.XDConstants;
import org.xdef.sys.Report;
import org.xdef.sys.ReportPrinter;
import org.xdef.sys.ReportReader;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Abstract servlet used for servlet implementation.
 * <p>
 * It sets globally and localy X-definition report-language
 * (ISO-639 two letters or ISO-639-2 three letters, e.g. "eng", "ces", "slk").
 *
 * @author Vaclav Trojan
 */
@MultipartConfig
public abstract class XdefServletAbs extends HttpServlet {
    private static final long serialVersionUID = -8154631839408075000L;

    private static final Logger logger = LoggerFactory.getLogger(XdefServletAbs.class);

    /** internal default X-definition reporter language */
    private   static final  String      reportLangDefault   = "eng";
    /** default X-definition properties */
    protected static final  Properties  XdPropsDefault      = getXdPropsDefault();


    static {
        //set X-definition report-language globally
        Report.setLanguage(reportLangDefault);
        logger.info("set X-definition report-language globally to: " + reportLangDefault);
    }


    /** default constructor, calls super() only */
    protected XdefServletAbs() {
        super();
    }


    /**
     * Get listing from reporter.
     *
     * @param reporter  reporter with error and warning messages
     * @param data      string with source data
     * @return string with listing form of source data
     */
    public static final String printReports(final ReportReader reporter, final String data) {
        Writer writer = new CharArrayWriter();
        Reader car = new CharArrayReader(data.toCharArray());
        ReportPrinter.printListing(
            writer, car, reporter,
            null, 120, false,
            reportLangDefault
        );
        return writer.toString();
    }

    /** @return default X-definition properties */
    private static Properties getXdPropsDefault() {
        Properties props = new Properties();
        //process warnings
        props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
        //disable doctype, xinclude by security-reasons, prevent of (not functional):
        // - XSS (Cross-Site Scripting) Attacks)
        // - XXE (XML eXternal Entity) injection?)
        props.setProperty(XDConstants.XDPROPERTY_DOCTYPE,  XDConstants.XDPROPERTYVALUE_DOCTYPE_FALSE);
        props.setProperty(XDConstants.XDPROPERTY_XINCLUDE, XDConstants.XDPROPERTYVALUE_XINCLUDE_FALSE);

        return props;
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if servlet error occurs.
     * @throws java.io.IOException if IO error occurs.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //processRequestInNewThread(request, response);
        processRequest(request, response);
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if servlet error occurs.
     * @throws java.io.IOException if IO error occurs.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        //processRequestInNewThread(request, response);
        processRequest(request, response);
    }

    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods in new thread.
     * Try to interrupt the new thread after 25s, but it's not functional (the new thread would control
     * its interrupt-status frequently)
     *
     * @param request servlet request.
     * @param response servlet response.
     * @throws ServletException if an error occurs.
     * @throws IOException if IO error occurs.
     */
    @SuppressWarnings("unused")
    private void processRequestInNewThread(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        ProcReq     p    = new ProcReq(request, response, this);
        IOException ioex = null;

        try {
            p.start();
            p.join(25000);
            if (!p.isFinished()) {
                p.interrupt(); //not functional, "p" don't check its interrupt-status frequently
                ioex = new IOException("Interrupted - timeout");
                ioex.setStackTrace(new StackTraceElement[0]);
            }
            if (p.getException() != null) {
                ioex = new IOException(p.getException().toString());
            }
        } catch (InterruptedException ex) {
            ioex = new IOException("Interrupted", ex);
        }
        if (ioex != null) {
            throw ioex;
        }
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
     * @throws ServletException if servlet error occurs.
     * @throws IOException if a IO error occurs.
     */
    public abstract void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException;



    /** This class implements thread in which runs XDDocument.
     * We need to run it in the separate thread to prevent long servlet
     * response.
     */
    private static class ProcReq extends Thread {
        private final XdefServletAbs _x;
        private final HttpServletRequest _request;
        private final HttpServletResponse _response;
        private boolean _finished = false;
        private Exception _exception = null;

        ProcReq(final HttpServletRequest request, final HttpServletResponse response, XdefServletAbs x) {
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
                _x.processRequest(_request, _response);
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

}
