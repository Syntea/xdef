package org.xdef.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

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
import org.xdef.sys.SUtils;
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
public final class Playground extends AbstractMyServlet {
    private static final long serialVersionUID = 2277695929503402350L;
    //private static final Logger logger = LoggerFactory.getLogger(Playground.class);

    private static final String RESPONSE_HTML_TEMPL =
        readRsrcAsString(Playground.class, "webapp/playground/playground-response-template.html");

    /** default constructor, calls super() only */
    public Playground() {
        super();
    }

    /**
     * Processes requests.
     * <p>
     * Request params:<ul>
     *   <li>rootName: name of root X-definition, in case of X-definition collection</li>
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
 *      </li>
     *   <li>csvHeader: values: no/"", yes</li>
     *   <li>reportLang: optionally report language (ISO-639 two letters or ISO-639-2 three letters,
     *       e.g. "", "eng", "ces", "slk")</li>
     * </ul>
     *
     * @param req servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException
    {
        req.setCharacterEncoding("UTF-8");

        //request parameters: see javadoc
        String xdefRoot     = getParam(req, "xdefRoot");
        String xdef         = getParam(req, "xdef");
        String dataFormatS  = getParam(req, "dataFormat").toLowerCase();
        String data         = getParam(req, "data");
        String mode         = getParam(req, "mode").toLowerCase();
        String langInp      = getParam(req, "langInp").toLowerCase();
        String langOut      = getParam(req, "langOut").toLowerCase();
        String modelName    = getParam(req, "modelName");
        String modelURI     = getParam(req, "modelURI");
        Set<XdDataFormat> xonDisplayAs = Stream.of(getParam(req, "xonDisplayAs").toLowerCase().split("(\\s|,)+"))
            .map(xdfs -> XdDataFormat.valueOfN(xdfs))
            .filter(xdf -> xdf != null)
            .collect(Collectors.toSet());
        String csvHeader = getParam(req, "csvHeaderExport").toLowerCase();

        //process default values and conversions
        xdefRoot  = xdefRoot.isEmpty() ? null : xdefRoot;
        mode      = mode.equals("compose") ? mode : "validate";
        csvHeader = csvHeader.isEmpty() || csvHeader.equals("no") ? "no" : "yes";
        XdDataFormat dataFormat = XdDataFormat.valueOfN(dataFormatS, XdDataFormat.xml);
        xonDisplayAs.remove(dataFormat);

        String          data2Xd         = data;
        String          status;
        String          title;
        String          message         = null;
        String          result          = null;
        Object          resultXon       = null;
        String          stdOutput       = null;

        XDPool          xdPool          = null;
        ArrayReporter   reporter        = new ArrayReporter();

        try {
            XDBuilder xdBuilder = XDFactory.getXDBuilder(reporter, XdPropsDefault);
            xdBuilder.setSource(xdef);
            xdPool = xdBuilder.compileXD();

            if (reporter.errorWarnings()) { //incorrect xdef
                status = "Error";
                title = "X-definition error(s)";
                message = printReports(reporter, xdef);

            } else {
                String  mode2Xd;
                Element resultElement = null;

                reporter.clear();
                CharArrayWriter caw = new CharArrayWriter();
                XDOutput stdout = XDFactory.createXDOutput(caw, false);

                XDDocument xd = xdPool.createXDDocument(xdefRoot);

                xd.setProperties(XdPropsDefault);
                xd.setStdOut(stdout);

                //xdef-process
                if ("compose".equals(mode)) {
                    String name;
                    String uri;
                    XMDefinition def = xd.getXMDefinition();

                    if (!modelName.isEmpty()) {
                        name = modelName;
                        uri  = !modelURI.isEmpty() ? modelURI : null;
                    } else {
                        XMElement[] x = xd.getXMDefinition().getModels();
                        name = x[0].getName();
                        uri  = x[0].getNSUri();
                    }

                    if (data2Xd.length() > 0) {
                        Element el = KXmlUtils.parseXml(data2Xd).getDocumentElement();
                        xd.setXDContext(el);
                        String n = el.getLocalName();
                        String u = el.getNamespaceURI();

                        if (null != def && null != def.getModel(u, n)) {
                            name = n;
                            uri  = u;
                        }
                    }

                    mode2Xd       = "compose";
                    resultElement = xd.xcreate(new QName(uri, name), reporter);
                } else {
                    if (dataFormat == XdDataFormat.json || dataFormat == XdDataFormat.xon || dataFormat == XdDataFormat.yaml) {
                        if (data2Xd.startsWith("<") && data2Xd.endsWith(">")) { //XON in XML-format
                            data2Xd = XonUtils.toJsonString(XonUtils.xmlToXon(data2Xd), true);
                        } else if (dataFormat == XdDataFormat.json) { //JSON
                            XonUtils.parseJSON(data2Xd);
                        } else if (dataFormat == XdDataFormat.xon) { //XON
                            data2Xd = XonUtils.toJsonString(XonUtils.parseXON(data2Xd), true);
                        } else if (dataFormat == XdDataFormat.yaml) { //YAML
                            data2Xd = XonUtils.toJsonString(yamlToJson(XonUtils.parseYAML(data2Xd)), true);
                        }

                        mode2Xd   = "validate-json";
                        resultXon = xd.jparse(data2Xd, reporter);
                    } else if (dataFormat == XdDataFormat.ini) {
                        mode2Xd   = "validate-ini";
                        resultXon = xd.iparse(data2Xd, reporter);
                    } else if (dataFormat == XdDataFormat.csv) {
                        mode2Xd   = "validate-csv";
                        resultXon = xd.cparse(
                            new StringReader(data2Xd),
                            ',', // separator
                            csvHeader.equals("no"),
                            null, // source name
                            reporter
                        );
                    } else if (!langOut.isEmpty()) {
                        mode2Xd       = "translate";
                        resultElement = xd.xtranslate(data2Xd, langInp, langOut, reporter);
                    } else {
                        if (!langInp.isEmpty()) {
                            xd.setLexiconLanguage(langInp);
                        }
                        mode2Xd       = "validate";
                        resultElement = xd.xparse(data2Xd, reporter);
                    }
                }
                caw.close();

                //create text result from xdef-process result
                if (reporter.errors()) {
                    status = "Error";
                    title = "Input data error(s)";
                    message = printReports(reporter, data2Xd);
                } else {
                    status = "OK";
                    title = "Result &mdash; mode \"" + mode2Xd + "\"";

                    if (resultElement != null) {
                        result = KXmlUtils.nodeToString(resultElement, true, false, true, 120);
                    } else if (resultXon != null) {
                        result = convertXon2Str(resultXon, dataFormat);
                    }
                }

                //create text std-output
                if (caw.size() > 0) {
                    stdOutput = caw.toString();
                }
            }
        } catch (SRuntimeException ex) {
            status = "Error";
            title  = "Unexpected or fatal input data error(s)";
            if ("SYS024".equals(ex.getMsgID())) {
                reporter.putReport(Report.fatal(XML.XML080, //XML parser was canceled by error&{0}{: }
                    "The XML document must start with '<'", "&{line}1&{column}1"));
            } else if (!reporter.errorWarnings()) {
                reporter.putReport(Report.fatal(ex.getMsgID(),
                    ex.getReport().getText(), ex.getReport().getModification()));
            }
            reporter.reset();
            message =
                printReports(reporter, data2Xd) +
                "\n\nException:\n" +
                STester.printThrowable(ex)
            ;
        } catch (Exception ex) {
            status = "Error";
            title  = "Unhandled Exception";
            message = STester.printThrowable(ex);
        }

        //assembly html-response
        boolean stdOutputEx  = stdOutput != null && !stdOutput.isEmpty();
        boolean resultIsHtml = result != null && dataFormat == XdDataFormat.xml && result.startsWith("<html");

        String respHtml = RESPONSE_HTML_TEMPL;
        respHtml = SUtils.modifyFirst(respHtml,   	"((xdef-lib-id))",      XDConstants.BUILD_IDENTIFIER);
        respHtml = SUtils.modifyString(respHtml,    "((status))",           status);
        respHtml = SUtils.modifyFirst(respHtml,     "((title))",            title);
        respHtml = SUtils.modifyFirst(respHtml,     "((message-disp))",     message != null ? "block" : "none");
        if (message != null) {
            respHtml = SUtils.modifyFirst(respHtml, "((message))",          preStringToPre(message));
        }
        respHtml = SUtils.modifyFirst(respHtml,     "((result-disp))",      result != null ? "block" : "none");
        if (result != null) {
            respHtml = SUtils.modifyFirst(respHtml, "((result-format))",    preStringToPre(dataFormat.toString().toUpperCase()));
            respHtml = SUtils.modifyFirst(respHtml, "((result))",           preStringToPre(result));
        }
        respHtml = SUtils.modifyFirst(respHtml,     "((display-html-disp))", resultIsHtml ? "block" : "none");
        if (resultIsHtml) {
            respHtml = SUtils.modifyFirst(respHtml, "((display-html))",     htmlStringToAttr(result));
        }
        respHtml = SUtils.modifyFirst(respHtml,     "((stdout-disp))",      stdOutputEx ? "block" : "none");
        if (stdOutputEx) {
            respHtml = SUtils.modifyFirst(respHtml, "((stdout))",           preStringToPre(stdOutput));
        }
        for (XdDataFormat df : XdDataFormat.values()) {
            String  dfDisp   = null;
            boolean dfDispEx =
                result != null && resultXon != null && xonDisplayAs.contains(df) &&
                (dfDisp = convertXon2Str(resultXon, df)) != null
            ;
            respHtml = SUtils.modifyFirst(
                respHtml,
                "((display-" + df.toString() + "-disp))",
                dfDispEx ? "block" : "none"
            );
            if (dfDispEx) {
                respHtml = SUtils.modifyFirst(
                    respHtml,
                    "((display-" + df.toString() + "))",
                    preStringToPre(dfDisp)
                );
            }
        }

        //return response
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print(respHtml);
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
                //DEBUG:
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
}
