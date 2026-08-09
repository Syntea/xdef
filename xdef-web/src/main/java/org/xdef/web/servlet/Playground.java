package org.xdef.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.XDService;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.web.util.ServletUtil;
import org.xdef.web.util.XdDataFormat;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Servlet for execution online playground, called "Playground-online".
 * For request parameters see {@link RequestParams}.
 * <p>
 * For examples, for playing users, for tester-users,
 * for tutorial examples, for other examples on Internet.
 * <p>
 * Handles in-memory derby-databases used in X-definitions. Databases are created on the first usage and
 * shutdown after 30 minute of inactivity by thread "db-cleanup" {@link #dbCleanupTimer}
 * with fixed rate 1 minute and initial delay 1 minute.
 *
 * @author Vaclav Trojan, V.Sisma
 */
public final class Playground extends XdefServletAbs {
    private static final long serialVersionUID = 2277695929503402350L;

    private static final Logger             logger          = LoggerFactory.getLogger(Playground.class);

    /** html-template for the servlet response */
    private static final String             responseHtmlTempl =
        ServletUtil.readRsrcAsString(Playground.class, "webapp/playground/playground-response-template.html");

    /** the exact Derby driver instance to de/registered */
    private static final EmbeddedDriver     dbDriver        = new EmbeddedDriver();
    /** credentials for the optional in-memory Derby database - database-user */
    private static final String             dbUser          = "myself";
    /** credentials for the optional in-memory Derby database - password for the database-user */
    private static final String             dbPassw         = "blatla6738";
    /** how long an unused Playground database is kept alive before it is shut down. */
    private static final Duration           dbTTL           = Duration.ofMinutes(30);
    /** map database name -> time (millis) it was last used */
    private static final Map<String, Long>  dbLastUsed      = new ConcurrentHashMap<>();

    /** single background thread db-cleanup {@link #dbLastUsed} for expired databases once a minute */
    private static final ScheduledExecutorService dbCleanupTimer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "playground-db-cleanup");
        t.setDaemon(true);
        return t;
    });


    static {
        //register db-drivers
        try {
            DriverManager.registerDriver(dbDriver);
        } catch (SQLException ex) {
            logger.warn("static: failed to register db-drivers", ex);
        }

        //start db-cleanup thread - with fixed rate 1min and initial delay 1min
        dbCleanupTimer.scheduleAtFixedRate(Playground::shutdownDatabasesOld, 1, 1, TimeUnit.MINUTES);
    }

    /** default constructor, calls super() only */
    public Playground() {
        super();
    }


    /** destroy servlet resources */
    @Override
    public void destroy() {
        //stop db-cleanup thread
        try {
            dbCleanupTimer.shutdownNow();
        } catch (RuntimeException ex) {
            logger.warn("destroy(): failed to shutdown the db-cleanup timer", ex);
        }

        //shutdown all databases
        shutdownDatabasesOld(true);

        //deregister db-drivers
        try {
            DriverManager.deregisterDriver(dbDriver);
        } catch (SQLException ex) {
            logger.warn("destroy(): failed to deregister db-drivers", ex);
        }

        super.destroy();
    }

    /** see {@link XdefServletAbs#getServletInfo()}
     * @return ... */
    @Override
    public final String getServletInfo() {
        return "This servlet executes a X-definition with given XML/XON data";
    }

    /**
     * Processes requests.
     *
     * @param req  servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    protected final void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");

        //read parameters, process request, assemble html-response
        RequestParams reqParams  = new RequestParams(req);
        ProcessParams procParams = processRequest(reqParams);
        String        respHtml   = assembleResponse(reqParams, procParams);

        //return response
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print(respHtml);
    }



    private static ProcessParams processRequest(RequestParams rp) {
        ProcessParams pp = new ProcessParams();

        String        data4Xd  = rp.data;
        XDPool        xdPool   = null;
        ArrayReporter reporter = new ArrayReporter();
        XDService     dbservice  = null;

        try {
            try {
                XDBuilder xdBuilder = XDFactory.getXDBuilder(reporter, XdPropsDefault);
                xdBuilder.setSource(rp.xdef);
                xdPool = xdBuilder.compileXD();

                //timer after xdef-compilation
                pp.timerXdef = new Date().getTime();

                if (reporter.errorWarnings()) {
                    //incorrect X-definition
                    pp.status  = CT.stError;
                    pp.title   = "X-definition error(s)";
                    pp.message = printReports(reporter, rp.xdef);

                } else {
                    String  mode4Xd;
                    Element resultElement = null;

                    reporter.clear();
                    CharArrayWriter caw    = new CharArrayWriter();
                    XDOutput        stdout = XDFactory.createXDOutput(caw, false);
                    XDDocument      xd     = xdPool.createXDDocument(rp.xdefRoot);
                    xd.setProperties(XdPropsDefault);
                    xd.setStdOut(stdout);

                    //make an in-memory database available to the X-definition as "external Service dbservice"
                    if (rp.databaseName != null) {
                        dbLastUsed.put(rp.databaseName, System.currentTimeMillis());
                        dbservice = XDFactory.createSQLService(
                            genConnectionURL(rp.databaseName, "create=true"), dbUser, dbPassw
                        );
                        xd.setVariable("dbservice", dbservice);
                    }

                    //xdef-process
                    if (CT.modeCompose.equals(rp.mode)) {
                        String uri;
                        String name;

                        if (!rp.modelName.isEmpty()) {
                            uri  = !rp.modelURI.isEmpty() ? rp.modelURI : null;
                            name = rp.modelName;
                        } else {
                            XMElement[] x = xd.getXMDefinition().getModels();

                            if (x.length < 1) {
                                throw new RuntimeException("Error: X-definition contains no models");
                            }

                            uri  = x[0].getNSUri();
                            name = x[0].getName();
                        }

                        if (data4Xd.length() > 0) {
                            Element      el  = KXmlUtils.parseXml(data4Xd).getDocumentElement();
                            xd.setXDContext(el);
                            String       u   = el.getNamespaceURI();
                            String       n   = el.getLocalName();
                            XMDefinition def = xd.getXMDefinition();

                            if (def != null && def.getModel(u, n) != null) {
                                uri  = u;
                                name = n;
                            }
                        }

                        mode4Xd       = CT.modeCompose;
                        resultElement = xd.xcreate(new QName(uri, name), reporter);
                    } else {
                        if (rp.dataFormat == XdDataFormat.json || rp.dataFormat == XdDataFormat.xon ||
                            rp.dataFormat == XdDataFormat.yaml
                        ) {
                            if (data4Xd.startsWith("<") && data4Xd.endsWith(">")) { //XON in XML-format
                                data4Xd = XonUtils.toJsonString(XonUtils.xmlToXon(data4Xd), true);
                            } else if (rp.dataFormat == XdDataFormat.json) { //JSON
                                XonUtils.parseJSON(data4Xd);
                            } else if (rp.dataFormat == XdDataFormat.xon) { //XON
                                data4Xd = XonUtils.toJsonString(XonUtils.parseXON(data4Xd), true);
                            } else if (rp.dataFormat == XdDataFormat.yaml) { //YAML
                                data4Xd = XonUtils.toJsonString(
                                    ServletUtil.convertYamlToJson(XonUtils.parseYAML(data4Xd)), true
                                );
                            }

                            mode4Xd      = CT.modeValidate + "-" + XdDataFormat.json.name();
                            pp.resultXon = xd.jparse(data4Xd, reporter);
                        } else if (rp.dataFormat == XdDataFormat.ini) {
                            mode4Xd      = CT.modeValidate + "-" + XdDataFormat.ini.name();
                            pp.resultXon = xd.iparse(data4Xd, reporter);
                        } else if (rp.dataFormat == XdDataFormat.csv) {
                            mode4Xd      = CT.modeValidate + "-" + XdDataFormat.csv.name();
                            pp.resultXon = xd.cparse(
                                new StringReader(data4Xd),
                                ',', // separator
                                rp.csvHeader.equals(CT.lNo),
                                null, // source name
                                reporter
                            );
                        } else if (!rp.langOut.isEmpty()) {
                            mode4Xd       = CT.modeTranslate;
                            resultElement = xd.xtranslate(data4Xd, rp.langInp, rp.langOut, reporter);
                        } else {
                            if (!rp.langInp.isEmpty()) {
                                xd.setLexiconLanguage(rp.langInp);
                                xd.getLexiconLanguage();
                            }
                            mode4Xd       = CT.modeValidate;
                            resultElement = xd.xparse(data4Xd, reporter);
                        }
                    }
                    caw.close();

                    //timer after xdef-processing
                    pp.timerProcess = new Date().getTime();

                    //create text result from xdef-process result
                    if (reporter.errors()) {
                        pp.status  = CT.stError;
                        pp.title   = "Input data error(s)";
                        pp.message = printReports(reporter, data4Xd);
                    } else {
                        pp.status = CT.stOk;
                        pp.title  = "Result — mode \"" + mode4Xd + "\"";

                        if (reporter.errorWarnings()) {
                            //reporter contains some warnings
                            pp.message = printReports(reporter, data4Xd);
                        }

                        if (resultElement != null) {
                            pp.result = KXmlUtils.nodeToString(resultElement, true, false, true, 120);
                        } else if (pp.resultXon != null) {
                            pp.result = ServletUtil.convertXon2Str(pp.resultXon, rp.dataFormat);
                        }
                    }

                    //create text std-output
                    if (caw.size() > 0) {
                        pp.stdOutput = caw.toString();
                    }
                }
            } catch (Exception ex) {
                if (pp.timerXdef == null) {
                    //timer of xdef-compilation during exception
                    pp.timerXdef = new Date().getTime();
                } else if (pp.timerProcess == null) {
                    //timer of xdef-process during exception
                    pp.timerProcess = new Date().getTime();
                }
                throw ex;
            } finally {
                if (dbservice != null) {
                    try {
                        dbservice.commit();
                    } catch (Exception ex) {
                        //ignore commit errors on an already-failed/rolled-back dbservice
                    }
                    dbservice.close();
                }
            }
        } catch (SRuntimeException ex) {
            pp.status = CT.stError;
            pp.title  = "Unexpected or fatal input data error(s)";
            if ("SYS024".equals(ex.getMsgID())) {
                reporter.putReport(Report.fatal(XML.XML080, //XML parser was canceled by error&{0}{: }
                    "The XML document must start with '<'", "&{line}1&{column}1"));
            } else if (!reporter.errorWarnings()) {
                reporter.putReport(Report.fatal(ex.getMsgID(),
                    ex.getReport().getText(), ex.getReport().getModification()));
            }
            reporter.reset();
            pp.message =
                printReports(reporter, data4Xd) +
                "\n\nException:\n" +
                STester.printThrowable(ex)
            ;
        } catch (Exception ex) {
            pp.status  = CT.stError;
            pp.title   = "Unhandled Exception";
            pp.message = STester.printThrowable(ex);
        }

        return pp;
    }

    /**
     * assemble html-response
     *
     * @return html-response
     * @throws Exception template process fails
     */
    private static String assembleResponse(RequestParams rp, ProcessParams pp) throws ServletException {
        boolean stdOutputEx  = pp.stdOutput != null && !pp.stdOutput.isEmpty();
        boolean resultIsHtml = pp.result != null && rp.dataFormat == XdDataFormat.xml && pp.result.startsWith("<html");
        boolean lexEx        = rp.mode.equals(CT.modeValidate) && (
                                   !rp.langInp.isEmpty() || !rp.langOut.isEmpty() || rp.xdef.contains(CT.mwLexicon)
                               )
        ;
        String  dataHili = rp.dataFormat == XdDataFormat.csv ? "plaintext" : rp.dataFormat.name();

        Map<String, String> values = new HashMap<>();
        values.put("xdef-lib-id",       XDConstants.BUILD_IDENTIFIER);

        values.put("xdefRoot",          ServletUtil.htmlToAttrVal(rp.xdefRoot));
        values.put("databaseName-disp", rp.databaseName != null ? CT.cssDispBlock : CT.cssDispNone);
        values.put("databaseName",      ServletUtil.htmlToAttrVal(rp.databaseName));
        values.put("xdef",              ServletUtil.preTextToPreCont(rp.xdef));
        values.put("xdefLines",         Integer.toString(rp.xdef.split("\n").length + 1));
        values.put("dataFormat",        rp.dataFormat.name());
        values.put("dataFormatUp",      rp.dataFormat.name().toUpperCase());
        values.put("langInp-disp",      lexEx ? CT.cssDispBlock : CT.cssDispNone);
        values.put("langInp",           ServletUtil.htmlToAttrVal(rp.langInp));
        values.put("data",              ServletUtil.preTextToPreCont(rp.data));
        values.put("dataLines",         Integer.toString(rp.data.split("\n").length + 1));
        values.put("model-disp",        CT.modeCompose.equals(rp.mode) ? CT.cssDispBlock : CT.cssDispNone);
        values.put("modelName",         ServletUtil.htmlToAttrVal(rp.modelName));
        values.put("modelURI",          ServletUtil.htmlToAttrVal(rp.modelURI));
        values.put("csvHeader-disp",    rp.dataFormat == XdDataFormat.csv ? CT.cssDispBlock : CT.cssDispNone);
        values.put("csvHeader-sel",     CT.lYes.equals(rp.csvHeader)  ? "csvHeaderYes" : "csvHeaderNo");
        values.put("xonDisplayAs-disp", !rp.xonDisplayAs.isEmpty() ? CT.cssDispBlock : CT.cssDispNone);
        values.put("xonDisplayAs",      rp.xonDisplayAs.stream().map(XdDataFormat::name).collect(Collectors.joining(" ")));
        values.put("langOut-disp",      lexEx ? CT.cssDispBlock : CT.cssDispNone);
        values.put("langOut",           ServletUtil.htmlToAttrVal(rp.langOut));
        values.put("mode",              rp.mode);

        values.put("status",            pp.status);
        values.put("title",             pp.title);
        values.put("message-disp",      pp.message != null ? CT.cssDispBlock : CT.cssDispNone);
        if (pp.message != null) {
            values.put("message",       ServletUtil.preTextToPreCont(pp.message));
        }
        values.put("result-disp",       pp.result != null ? CT.cssDispBlock : CT.cssDispNone);
        if (pp.result != null) {
            values.put("result-formatUp", rp.dataFormat.name().toUpperCase());
            values.put("result-hili",   dataHili);
            values.put("result",        ServletUtil.preTextToPreCont(pp.result));
        }
        values.put("display-html-disp", resultIsHtml ? CT.cssDispBlock : CT.cssDispNone);
        if (resultIsHtml) {
            values.put("display-html",  ServletUtil.htmlToAttrVal(pp.result));
        }
        values.put("stdout-disp",       stdOutputEx ? CT.cssDispBlock : CT.cssDispNone);
        if (stdOutputEx) {
            values.put("stdout",        ServletUtil.preTextToPreCont(pp.stdOutput));
        }
        for (XdDataFormat df : XdDataFormat.values()) {
            String  dfDisp   = null;
            boolean dfDispEx =
                pp.result != null && pp.resultXon != null &&
                rp.xonDisplayAs.contains(df) && df != rp.dataFormat &&
                (dfDisp = ServletUtil.convertXon2Str(pp.resultXon, df)) != null
            ;
            values.put("display-" + df + "-disp", dfDispEx ? CT.cssDispBlock : CT.cssDispNone);
            if (dfDispEx) {
                values.put("display-" + df, ServletUtil.preTextToPreCont(dfDisp));
            }
        }

        //display timers
        values.put("timer-xdef",
            pp.timerXdef    != null ? Long.toString(pp.timerXdef    - pp.timerStart) + " ms": "not started");
        values.put("timer-process",
            pp.timerProcess != null ? Long.toString(pp.timerProcess - pp.timerXdef)  + " ms": "not started");
        //timer end
        long timerEnd = new Date().getTime();
        values.put("timer-total",       Long.toString(timerEnd - pp.timerStart) + " ms");

        return ServletUtil.mustache(responseHtmlTempl, values);
    }

    /**
     * assemble JDBC-URL string
     * @param dbName    name of the in-memory Derby database.
     * @param options   connection-url options
     * @return JDBC connection URL of the in-memory Derby database
     */
    public static String genConnectionURL(final String dbName, final String options) {
        return
            "jdbc:derby:memory:" + dbName +
            ((options == null || options.isEmpty()) ? "" : (";" + options))
        ;
    }

    /**
     * shutdown any dbLastUsed database not used for at least {@link #dbTTL}. A database whose shutdown
     * fails for an unexpected reason (e.g. still in use) is kept in {@link #dbLastUsed} so the next
     * db-cleanup retries it, instead of being silently dropped from tracking while still alive in memory.
     *
     * @param allAges whether shutdown all databases regardless of age, not only old
     */
    private static void shutdownDatabasesOld(boolean allAges) {
        logger.debug("shutdownDatabasesOld(): started: allAges: " + allAges + ", dbs: " + dbLastUsed.toString());

        long cutoff = System.currentTimeMillis() - dbTTL.toMillis();
        dbLastUsed.entrySet().removeIf(e -> (allAges || e.getValue() < cutoff) && shutdownDatabase(e.getKey()));

        logger.debug("shutdownDatabasesOld(): finished: dbs: " + dbLastUsed.toString());
    }

    /** see {@link #shutdownDatabasesOld(boolean)} with {@code allAges = false}*/
    private static void shutdownDatabasesOld() {
        shutdownDatabasesOld(false);
    }

    /**
     * physically shutdown the in-memory Derby database dbName, freeing its resources
     *
     * @param dbName database name
     * @return true if the database was (or already had been) successfully shutdown; false if the
     *         shutdown failed for an unexpected reason and should be retried later.
     */
    private static boolean shutdownDatabase(final String dbName) {
        final String mtd = "shutdownDatabase(): ";
        try {
            DriverManager.getConnection(genConnectionURL(dbName, "drop=true"));
            logger.warn(mtd + "dropping \"" + dbName +
                "\" unexpectedly returned a live connection instead of throwing");
            return true;
        } catch (SQLException ex) {
            if (derbySQLStateSuccessfulDrop.equals(ex.getSQLState())) {
                logger.debug(mtd + "database \"" + dbName + "\" was dropped");
                return true;
            }
            logger.warn(mtd + "failed to drop database \"" + dbName + "\", will retry later", ex);
            return false;
        }
    }

    /** SQLState Derby reports on a successful in-memory database shutdown/drop (not an actual error). */
    private static final String derbySQLStateSuccessfulDrop = "08006";



    /** request parameters, see class-properties */
    private static class RequestParams {
        /** name of root X-definition, in case of X-definition collection */
        String              xdefRoot;
        /** X-definition (xml-format) */
        String              xdef;
        /** name of an in-memory Derby database made available for X-definition as "external Service dbservice"
         * (empty => no database, "dbservice" variable is not set) */
        String              databaseName;
        /** values: xml/"", json, xon, yaml, csv, ini */
        XdDataFormat        dataFormat;
        /** input data, in format "dataFormat", for dataFormat in json, xon, yaml, data can be in format "xon-xml" */
        String              data;
        /** X-definition processing mode, values: validate/"", compose */
        String              mode;
        /** value: language of input data (only for mode-validate) */
        String              langInp;
        /** value: language of processed data (only for mode-validate) */
        String              langOut;
        /** model-name, only for mode-construction */
        String              modelName;
        /** model-URI, only for mode-construction */
        String              modelURI;
        /** list of values: json, xon, yaml, xml, csv, ini (only for mode-validate and xon-like input
         *    (i.e. json, xon, yaml, csv, ini)), see {@link XdDataFormat} */
        List<XdDataFormat>  xonDisplayAs;
        /** values: no/"", yes */
        String              csvHeader;

        /** regexp for value of parameter {@link #databaseName} */
        private static final Pattern databaseNameRE = Pattern.compile("[A-Za-z0-9_-]+");

        private RequestParams(HttpServletRequest req) {
            //request parameters: see javadoc
            xdefRoot            = ServletUtil.getParam(req, "xdefRoot");
            xdef                = ServletUtil.getParam(req, "xdef");
            databaseName        = ServletUtil.getParam(req, "databaseName");
            String dataFormatS  = ServletUtil.getParam(req, "dataFormat").toLowerCase();
            data                = ServletUtil.getParam(req, "data");
            mode                = ServletUtil.getParam(req, "mode").toLowerCase();
            langInp             = ServletUtil.getParam(req, "langInp").toLowerCase();
            langOut             = ServletUtil.getParam(req, "langOut").toLowerCase();
            modelName           = ServletUtil.getParam(req, "modelName");
            modelURI            = ServletUtil.getParam(req, "modelURI");
            xonDisplayAs        = Stream.of(ServletUtil.getParam(req, "xonDisplayAs").toLowerCase().split("(\\s|,)+"))
                .map(xdfs -> XdDataFormat.valueOfN(xdfs))
                .filter(xdf -> xdf != null)
                .collect(Collectors.toList())
            ;
            csvHeader           = ServletUtil.getParam(req, "csvHeader").toLowerCase();

            //process default values and conversions
            xdefRoot            = xdefRoot    .isEmpty() ? null : xdefRoot;
            databaseName        = databaseNameRE.matcher(databaseName).matches() ? databaseName : null;
            dataFormat          = XdDataFormat.valueOfN(dataFormatS, XdDataFormat.xml);
            mode                = mode.equals(CT.modeCompose) ? mode : CT.modeValidate;
            csvHeader           = csvHeader.isEmpty() || csvHeader.equals(CT.lNo) ? CT.lNo : CT.lYes;
        }
    }


    /** processing parameters for assembling response-output */
    private static class ProcessParams {
        String  status;
        String  title;
        String  message;
        String  result;
        Object  resultXon;
        String  stdOutput;
        Long    timerStart = new Date().getTime();
        Long    timerXdef;
        Long    timerProcess;
    }

    /** commonly used string constants */
    private static class CT {
        /** logical no */
        private static final String lNo             = "no";
        /** logical yes */
        private static final String lYes            = "yes";
        /** status OK */
        private static final String stOk            = "OK";
        /** status Error */
        private static final String stError         = "Error";
        /** css style display value "block" */
        private static final String cssDispBlock    = "block";
        /** css style display value "none" */
        private static final String cssDispNone     = "none";
        /** form parameter "mode" value "validate" */
        private static final String modeValidate    = "validate";
        /** form parameter "mode" value "compose" */
        private static final String modeCompose     = "compose";
        /** form parameter "mode" value "translate" */
        private static final String modeTranslate   = "translate";
        /** mark-word "lexicon" for X-lexicon */
        private static final String mwLexicon       = "lexicon";
    }

}
