package org.xdef.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xdef.XDBuilder;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.XML;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.web.util.ServletUtil;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import org.yaml.snakeyaml.Yaml;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Servlet for execution "Playground-online".
 * <p>
 * For example for playing users or for tester-users or
 * for tutorial examples or for other examples on internet
 *
 * @author Vaclav Trojan
 */
public final class Playground extends XdefServletAbs {
    private static final long serialVersionUID = 2277695929503402350L;

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(Playground.class);

    private static final String responseHtmlTempl =
        ServletUtil.readRsrcAsString(Playground.class, "webapp/playground/playground-response-template.html");

    /** default constructor, calls super() only */
    public Playground() {
        super();
    }

    /**
     * Processes requests.
     *
     * @param req  servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
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

    private ProcessParams processRequest(RequestParams rp)
        throws ServletException, IOException
    {
        ProcessParams pp = new ProcessParams();

        String        data4Xd  = rp.data;
        XDPool        xdPool   = null;
        ArrayReporter reporter = new ArrayReporter();

        try {
            try {
                XDBuilder xdBuilder = XDFactory.getXDBuilder(reporter, XdPropsDefault);
                xdBuilder.setSource(rp.xdef);
                xdPool = xdBuilder.compileXD();

                //timer after xdef-compilation
                pp.timerXdef = new Date().getTime();

                if (reporter.errorWarnings()) {
                    //incorrect X-definition
                    pp.status  = "Error";
                    pp.title   = "X-definition error(s)";
                    pp.message = printReports(reporter, rp.xdef);

                } else {
                    String  mode4Xd;
                    Element resultElement = null;

                    reporter.clear();
                    CharArrayWriter caw    = new CharArrayWriter();
                    XDOutput        stdout = XDFactory.createXDOutput(caw, false);

                    XDDocument xd = xdPool.createXDDocument(rp.xdefRoot);

                    xd.setProperties(XdPropsDefault);
                    xd.setStdOut(stdout);

                    //xdef-process
                    if ("compose".equals(rp.mode)) {
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

                        mode4Xd       = "compose";
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
                                data4Xd = XonUtils.toJsonString(yamlToJson(XonUtils.parseYAML(data4Xd)), true);
                            }

                            mode4Xd   = "validate-json";
                            pp.resultXon = xd.jparse(data4Xd, reporter);
                        } else if (rp.dataFormat == XdDataFormat.ini) {
                            mode4Xd   = "validate-ini";
                            pp.resultXon = xd.iparse(data4Xd, reporter);
                        } else if (rp.dataFormat == XdDataFormat.csv) {
                            mode4Xd   = "validate-csv";
                            pp.resultXon = xd.cparse(
                                new StringReader(data4Xd),
                                ',', // separator
                                rp.csvHeader.equals("no"),
                                null, // source name
                                reporter
                            );
                        } else if (!rp.langOut.isEmpty()) {
                            mode4Xd       = "translate";
                            resultElement = xd.xtranslate(data4Xd, rp.langInp, rp.langOut, reporter);
                        } else {
                            if (!rp.langInp.isEmpty()) {
                                xd.setLexiconLanguage(rp.langInp);
                                xd.getLexiconLanguage();
                            }
                            mode4Xd       = "validate";
                            resultElement = xd.xparse(data4Xd, reporter);
                        }
                    }
                    caw.close();

                    //timer after xdef-processing
                    pp.timerProcess = new Date().getTime();

                    //create text result from xdef-process result
                    if (reporter.errors()) {
                        pp.status  = "Error";
                        pp.title   = "Input data error(s)";
                        pp.message = printReports(reporter, data4Xd);
                    } else {
                        pp.status = "OK";
                        pp.title  = "Result — mode \"" + mode4Xd + "\"";

                        if (reporter.errorWarnings()) {
                            //reporter contains some warnings
                            pp.message = printReports(reporter, data4Xd);
                        }

                        if (resultElement != null) {
                            pp.result = KXmlUtils.nodeToString(resultElement, true, false, true, 120);
                        } else if (pp.resultXon != null) {
                            pp.result = convertXon2Str(pp.resultXon, rp.dataFormat);
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
            }
        } catch (SRuntimeException ex) {
            pp.status = "Error";
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
            pp.status  = "Error";
            pp.title   = "Unhandled Exception";
            pp.message = STester.printThrowable(ex);
        }

        return pp;
    }

    /**
     * assemble html-response
     *
     * @return html-response
     */
    private String assembleResponse(RequestParams rp, ProcessParams pp) {
        boolean stdOutputEx  = pp.stdOutput != null && !pp.stdOutput.isEmpty();
        boolean resultIsHtml = pp.result != null && rp.dataFormat == XdDataFormat.xml && pp.result.startsWith("<html");
        boolean lexEx        = rp.mode.equals("validate") && (
                                   !rp.langInp.isEmpty() || !rp.langOut.isEmpty() || rp.xdef.contains("lexicon")
                               )
        ;
        String  dataHili = rp.dataFormat == XdDataFormat.csv ? "plaintext" : rp.dataFormat.name();

        Map<String, String> values = new HashMap<>();
        values.put("xdef-lib-id",       XDConstants.BUILD_IDENTIFIER);

        values.put("xdefRoot",          Optional.ofNullable(rp.xdefRoot).orElse(""));
        values.put("xdef",              ServletUtil.preTextToPreCont(rp.xdef));
        values.put("xdefLines",         Integer.toString(rp.xdef.split("\n").length + 1));
        values.put("dataFormat",        rp.dataFormat.name());
        values.put("dataFormatUp",      rp.dataFormat.name().toUpperCase());
        values.put("langInp-disp",      lexEx ? "block" : "none");
        values.put("langInp",           rp.langInp);
        values.put("data",              ServletUtil.preTextToPreCont(rp.data));
        values.put("dataLines",         Integer.toString(rp.data.split("\n").length + 1));
        values.put("model-disp",        "compose".equals(rp.mode) ? "block" : "none");
        values.put("modelName",         rp.modelName);
        values.put("modelURI",          rp.modelURI);
        values.put("csvHeader-disp",    rp.dataFormat == XdDataFormat.csv ? "block" : "none");
        values.put("csvHeader-sel",     "yes".equals(rp.csvHeader)  ? "csvHeaderYes" : "csvHeaderNo");
        values.put("xonDisplayAs-disp", !rp.xonDisplayAs.isEmpty() ? "block" : "none");
        values.put("xonDisplayAs",      rp.xonDisplayAs.stream().map(XdDataFormat::name) .collect(Collectors.joining(" ")));
        values.put("langOut-disp",      lexEx ? "block" : "none");
        values.put("langOut",           rp.langOut);
        values.put("mode",              rp.mode);

        values.put("status",            pp.status);
        values.put("title",             pp.title);
        values.put("message-disp",      pp.message != null ? "block" : "none");
        if (pp.message != null) {
            values.put("message",       ServletUtil.preTextToPreCont(pp.message));
        }
        values.put("result-disp",       pp.result != null ? "block" : "none");
        if (pp.result != null) {
            values.put("result-formatUp", rp.dataFormat.name().toUpperCase());
            values.put("result-hili",   dataHili);
            values.put("result",        ServletUtil.preTextToPreCont(pp.result));
        }
        values.put("display-html-disp", resultIsHtml ? "block" : "none");
        if (resultIsHtml) {
            values.put("display-html",  ServletUtil.htmlToAttrVal(pp.result));
        }
        values.put("stdout-disp",       stdOutputEx ? "block" : "none");
        if (stdOutputEx) {
            values.put("stdout",        ServletUtil.preTextToPreCont(pp.stdOutput));
        }
        for (XdDataFormat df : XdDataFormat.values()) {
            String  dfDisp   = null;
            boolean dfDispEx =
                pp.result != null && pp.resultXon != null &&
                rp.xonDisplayAs.contains(df) && df != rp.dataFormat &&
                (dfDisp = convertXon2Str(pp.resultXon, df)) != null
            ;
            values.put("display-" + df + "-disp", dfDispEx ? "block" : "none");
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

    /** Returns a short description of this servlet.
     * @return short description of this servlet.
     */
    @Override
    public final String getServletInfo() {
        return "This servlet executes a X-definition with given XML/XON data";
    }

    /** Convert result of YAML parser to JSON.
     * @param o result of YAML parser.
     * @return JSON result.
     */
    private static Object yamlToJson(final Object o) {
        if (null == o) {
            return null;
        }
        if (o instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> om     = (Map<Object, Object>)o;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> en : om.entrySet()) {
                result.put(
                    (String)yamlToJson(en.getKey()),
                    yamlToJson(en.getValue())
                );
            }
            return result;
        } else if (o instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> ol     = (List<Object>)o;
            List<Object> result = new ArrayList<>();
            for (int i=0; i < ol.size(); i++ ) {
                result.add(yamlToJson(ol.get(i)));
            }
            return result;
        } else if (o instanceof byte[]) {
            byte[] oba = (byte[])o;
            return new String(oba, StandardCharsets.UTF_8);
        }
        return o;
    }

    private static String convertXon2Str(Object xon, XdDataFormat outFormat) {
        if (outFormat == null) {
            return null;
        }

        String result = null;

        switch (outFormat) {
            case json:
                result = XonUtils.toJsonString(xon, true);
                break;
            case yaml:
                Yaml yaml = new Yaml();
                result = yaml.dump(XonUtils.xonToJson(xon));
                break;
            case csv:
                if (xon instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> xonCsv = (List<Object>)xon;
                    try {
                        result = XonUtils.toCsvString(xonCsv);
                    } catch (Exception ex) {
                        //return null
                    }
                }
                break;
            case ini:
                if (xon instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> xonIni = (Map<String, Object>)xon;
                    try {
                        result = XonUtils.toIniString(xonIni);
                    } catch (Exception ex) {
                        //return null
                    }
                }
                break;
            case xon:
                result = XonUtils.toXonString(xon, true);
                break;
            case xml:
                result = KXmlUtils.nodeToString(
                    XonUtils.xonToXml(xon),
                    true, false, true, 120
                );
                /* * /
                //DBG:
                result += "\n\n==json-xml==\n" + KXmlUtils.nodeToString(
                    XonUtils.xonToXml(XonUtils.xonToJson(xon)),
                    true, false, true, 120
                );
                if (xon instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> xonCsv = (List<Object>)xon;
                    result += "\n\n==csv-xml==\n" + KXmlUtils.nodeToString(
                        XonUtils.csvToXml(xonCsv),
                        true, false, true, 120
                    );
                }
                if (xon instanceof Map) {
                    result += "\n\n==ini-xml==\n" + KXmlUtils.nodeToString(
                        XonUtils.iniToXml(XonUtils.xonToJson(xon)),
                        true, false, true, 120
                    );
                }
                /**/
                break;
        }

        return result;
    }


    /** list of X-definition input data formats */
    private static enum XdDataFormat {
        xml,
        json,
        xon,
        yaml,
        csv,
        ini,
        ;

        static XdDataFormat valueOfN(String val) {
            try {
                return XdDataFormat.valueOf(val);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        static XdDataFormat valueOfN(String val, XdDataFormat defaultt) {
            return Optional.ofNullable(XdDataFormat.valueOfN(val))
                .orElse(defaultt)
            ;
        }
    }


    /**
     * Request parameters:<ul>
     *   <li>xdefRoot: name of root X-definition, in case of X-definition collection</li>
     *   <li>xdef: X-definition (xml-format)</li>
     *   <li>dataFormat: values: xml/"", json, xon, yaml, csv, ini</li>
     *   <li>data: input data, in format "dataFormat",
     *      for dataFormat in json, xon, yaml, data can be in format "xon-xml"
     *   </li>
     *   <li>mode: X-definition processing mode, values: validate/"", compose</li>
     *   <li>langInp: value: language of input data (only for mode validate)</li>
     *   <li>langOut: value: language of processed data (only for mode validate)</li>
     *   <li>modelName</li>
     *   <li>modelURI</li>
     *   <li>xonDisplayAs: set of values: json, xon, yaml, xml, csv, ini (only for mode=validate and xon-like input
     *      (i.e. json, xon, yaml, csv, ini))
     *   </li>
     *   <li>csvHeader: values: no/"", yes</li>
     * </ul>
     */
    private static class RequestParams {
        String              xdefRoot;
        String              xdef;
        XdDataFormat        dataFormat;
        String              data;
        String              mode;
        String              langInp;
        String              langOut;
        String              modelName;
        String              modelURI;
        List<XdDataFormat>  xonDisplayAs;
        String              csvHeader;

        private RequestParams(HttpServletRequest req) {
            //request parameters: see javadoc
            xdefRoot            = ServletUtil.getParam(req, "xdefRoot");
            xdef                = ServletUtil.getParam(req, "xdef");
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
            xdefRoot    = xdefRoot.isEmpty() ? null : xdefRoot;
            dataFormat  = XdDataFormat.valueOfN(dataFormatS, XdDataFormat.xml);
            mode        = mode.equals("compose") ? mode : "validate";
            csvHeader   = csvHeader.isEmpty() || csvHeader.equals("no") ? "no" : "yes";
        }
    }


    /**
     * processing parameters for assembling response-output
     */
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

}
