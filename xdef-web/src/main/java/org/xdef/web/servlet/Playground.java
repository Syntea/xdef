package org.xdef.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    private static final String HTML_RESULT = readRsrcAsString(Playground.class, "playground-result.html");

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
     *   <li>input: values: xml/"", xon, json, yaml, ini, csv</li>
     *   <li>data: input data, in format xml, xon, json, yaml, ini, csv</li>
     *   <li>mode: X-definition processing mode, values: validate/"", compose</li>
     *   <li>langInp: value: language of input data (only for mode validate)</li>
     *   <li>langOut: value: language of processed data (only for mode validate)</li>
     *   <li>modelName</li>
     *   <li>modelURI</li>
     *   <li>xonDisplayAs: set of values: xon, json, yaml, ini, csv, xml</li>
     *   <li>csvHeaderExport: values: no/"", yes</li>
     * </ul>
     *
     * @param req servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void procReq(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException
    {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        //This part we must synchronize to keep language settings for whole process.
        synchronized(MANAGER) {
            ArrayReporter reporter = new ArrayReporter();

            //request parameters: see javadoc
            String rootName = getParam(req, "rootName");
            String xdef = getParam(req, "xdef");
            String input = getParam(req, "input").toLowerCase();
            String data = getParam(req, "data");
            String mode = getParam(req, "mode").toLowerCase();
            String langInp = getParam(req, "langInp").toLowerCase();
            String langOut = getParam(req, "langOut").toLowerCase();
            String modelName = getParam(req, "modelName");
            String modelURI = getParam(req, "modelURI");
            Set<String> xonDisplayAs = Stream.of(getParam(req, "xonDisplayAs").toLowerCase().split("\\s+"))
                .collect(Collectors.toSet());
            String csvHeaderExport = getParam(req, "csvHeaderExport").toLowerCase();

            //process default values
            mode            = mode .isEmpty() ? "validate" : mode;
            input           = input.isEmpty() ? "xml"      : input;
            csvHeaderExport = csvHeaderExport.isEmpty() || csvHeaderExport.equals("no") ? "" : "yes";

            String status;
            String title;
            String message = null;
            String result = null;
            String stdOutput = null;
            PrintWriter out = resp.getWriter();
            XDPool xp = null;

            Properties props = new Properties();
            props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);

            try {
                XDBuilder xb = XDFactory.getXDBuilder(reporter, props);
                xb.setSource(xdef);
                xp = xb.compileXD();
            } catch (Exception ex) {
                if (xp == null || !reporter.errorWarnings()) {
                    out.print(genHtmlMessage(
                        "Exception",
                        "<pre><b>" + preStringToPre(STester.printThrowable(ex)) + "</b></pre>")
                    );
                    return;
                } else {
                    reporter.reset();
                }
            }

            try {
                if (reporter.errorWarnings()) {//incorrect xdef
                    Report rep = reporter.getReport();
                    if (null != rep && "XDEF903".equals(rep.getMsgID()) && null != rep.getModification()
                        && !rep.getModification().startsWith("&{0}<")) {
                        return;
                    } else {
                        reporter.reset();
                    }
                    status = "Error";
                    title = "X-definition error(s)";
                    message = printReports(reporter, xdef);

                } else {
                    Element             resultElement = null;
                    Object              resultXon     = null;
                    Map<String, Object> resultIni     = null;
                    List<Object>        resultCsv     = null;

                    reporter.clear();
                    CharArrayWriter caw = new CharArrayWriter();
                    XDOutput stdout = XDFactory.createXDOutput(caw, false);

                    XDDocument xd = xp.createXDDocument(rootName);

                    xd.setProperties(props);
                    xd.setStdOut(stdout);

                    if ("compose".equals(mode)) {
                        String name;
                        String uri;
                        XMDefinition def = xd.getXMDefinition();
                        if (!modelName.isEmpty()) {
                            name = modelName;
                            uri = !modelURI.isEmpty() ? modelURI : null;
                        } else {
                            XMElement[] x = xd.getXMDefinition().getModels();
                            name = x[0].getName();
                            uri = x[0].getNSUri();
                        }
                        if (data.length() > 0) {
                            Element el = KXmlUtils.parseXml(data).getDocumentElement();
                            xd.setXDContext(el);
                            String n = el.getLocalName();
                            String u = el.getNamespaceURI();
                            if (null != def && null != def.getModel(u, n)) {
                                uri = u;
                                name = n;
                            }
                        }
                        resultElement = xd.xcreate(new QName(uri, name), reporter);
                    } else {
                        if ("json".equals(input) || "yaml".equals(input)) {
                            String s;
                            if ("json".equals(input)) {
                                if (data.startsWith("<") && data.endsWith(">")) { //JSON in XML-format
                                    s = XonUtils.toJsonString(XonUtils.xmlToXon(data), true);
                                } else { //XON/JSON
                                    XonUtils.parseJSON(data);
                                    s = data;
                                }
                            } else { //XON/YAML
                                s = XonUtils.toJsonString(yamlToJson(XonUtils.parseYAML(data)), true);
                            }

                            data = s;
                            resultXon = xd.jparse(data, reporter);
                        } else if ("ini".equals(input)) {
                            resultXon = resultIni = xd.iparse(data, reporter);
                        } else if ("csv".equals(input)) {
                            resultXon = resultCsv = xd.cparse(
                                new StringReader(data),
                                ',', // separator
                                csvHeaderExport.isEmpty(),
                                null, // source name
                                reporter
                            );
                        } else if (!langOut.isEmpty()) {
                            resultElement = xd.xtranslate(data, langInp, langOut, reporter);
                        } else {
                            if (!langInp.isEmpty()) {
                                xd.setLexiconLanguage(langInp);
                            }
                            resultElement = xd.xparse(data, reporter);
                        }
                    }
                    caw.close();

                    if (reporter.errors()) {
                        status = "Error";
                        title = "Input data error(s)";
                        message = printReports(reporter, data);
                    } else {
                        status = "OK";
                        title = "Result (mode \"" + mode + "\")";

                        if (resultElement != null) {
                            result = KXmlUtils.nodeToString(resultElement, true, false, true, 120);
                        } else if (resultXon != null) {
                            result = convertXon(resultXon, input);
                        }
                    }

                    if (caw.size() > 0) {
                        stdOutput = caw.toString();
                    }
                }
            } catch (SRuntimeException ex) {
                status = "Error";
                title = "Input data error(s)";
                if ("SYS024".equals(ex.getMsgID())) {
                    reporter.putReport(Report.fatal(XML.XML080, //XML parser was canceled by error&{0}{: }
                        "The XML document must start with '<'", "&{line}1&{column}1"));
                } else if (!reporter.errorWarnings()) {
                    reporter.putReport(Report.fatal(ex.getMsgID(),
                        ex.getReport().getText(), ex.getReport().getModification()));
                }
                message = printReports(reporter, data);
                reporter.reset();
            }

            boolean stdOutputEx  = stdOutput != null && !stdOutput.isEmpty();
            boolean resultIsHtml = result != null && input.equals("xml") && result.startsWith("<html");
            xonDisplayAs.remove(input);

            String outHtml;
            outHtml = SUtils.modifyFirst(HTML_RESULT,   "((xdef-lib-id))",      XDConstants.BUILD_IDENTIFIER);
            outHtml = SUtils.modifyString(outHtml,      "((status))",           status);
            outHtml = SUtils.modifyFirst(outHtml,       "((title))",            title);
            outHtml = SUtils.modifyFirst(outHtml,       "((message-disp))",     message != null ? "block" : "none");
            if (message != null) {
                outHtml = SUtils.modifyFirst(outHtml,   "((message))",          preStringToPre(message));
            }
            outHtml = SUtils.modifyFirst(outHtml,       "((result-disp))",      result != null ? "block" : "none");
            if (result != null) {
                outHtml = SUtils.modifyFirst(outHtml,   "((result-format))",    preStringToPre(input.toUpperCase()));
                outHtml = SUtils.modifyFirst(outHtml,   "((result))",           preStringToPre(result));
            }
            outHtml = SUtils.modifyFirst(outHtml,       "((display-html-disp))", resultIsHtml ? "block" : "none");
            if (resultIsHtml) {
                outHtml = SUtils.modifyFirst(outHtml,   "((display-html))",     htmlStringToAttr(result));
            }
            outHtml = SUtils.modifyFirst(outHtml,       "((stdout-disp))",      stdOutputEx ? "block" : "none");
            if (stdOutputEx) {
                outHtml = SUtils.modifyFirst(outHtml,   "((stdout))",           preStringToPre(stdOutput));
            }
            out.print(outHtml);
        }
    }

    /** Returns a short description of this servlet.
     * @return short description of this servlet.     */
    @Override
    public final String getServletInfo() {
        return "This servlet executes a X-definition with given XML data";
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

    private static String convertXon(Object xon, String format) {
        if (format == null) {
            return null;
        }

        String result = null;

        switch (format) {
        case "json":
            result = XonUtils.toJsonString(xon, true);
            break;
        case "yaml":
            Yaml yaml = new Yaml();
            result = yaml.dump(XonUtils.xonToJson(xon));
            break;
        case "csv":
            if (xon instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> xonCsv = (List<Object>)xon;
                result = XonUtils.toCsvString(xonCsv);
            }
            break;
        case "ini":
            @SuppressWarnings("unchecked")
            Map<String, Object> xonIni = (Map<String, Object>)xon;
            result = XonUtils.toIniString(xonIni);
            break;
        case "xon":
            result = XonUtils.toXonString(xon, true);
            break;
        case "csv-xml":
            @SuppressWarnings("unchecked")
            List<Object> xonCsv = (List<Object>)xon;
            result = KXmlUtils.nodeToString(
                XonUtils.csvToXml(xonCsv)
            );
            break;
        case "ini-xml":
            result = KXmlUtils.nodeToString(
                XonUtils.iniToXml(XonUtils.xonToJson(xon)),
                true, false, true, 110
            );
            break;
        case "xon-xml":
            result = KXmlUtils.nodeToString(
                XonUtils.xonToXml(XonUtils.xonToJson(xon)),
                true, false, true, 110
            );
            break;
        }

        return result;
    }

}
