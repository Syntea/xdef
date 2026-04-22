package org.xdef.web.servlet;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
 * Servlet for execution "Playground-online". For example for playing users or for tester-users or
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

    /** Processes requests with respect to required language. The Language is set according to request
     * parameter "submit".
     * @param req servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void procReq(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        // This part we must synchronize to keep language settings for whole process.
        synchronized(MANAGER) {
            Report.setLanguage("eng");
            ArrayReporter reporter = new ArrayReporter();
            String mode = getParam(req, "mode").toLowerCase(); //values: "", validate, compose
            String xdef = getParam(req, "xdef"); //values: xml with X-definition
            String data = getParam(req, "data"); //values: input data xml, xon, json, yaml, ini, csv
            String xdName = getParam(req, "xdname");
            String mName = getParam(req, "mName");
            String mURI = getParam(req, "mURI");
            List<String> xonDisplayAs = Arrays.asList(getParam(req, "xonDisplayAs").toLowerCase().split("\\s+")); //list of values: xon, json, yaml, ini, csv, xml
            String input = getParam(req, "input").toLowerCase(); //values: "", xml, xon, json, yaml, ini, csv
            String csvHeaderExport = getParam(req, "csvHeaderExport").toLowerCase(); //values: "", no, yes
            String langInp = getParam(req, "langInp").toLowerCase(); //values: langueage of input data (only for mode validate)
            String langOut = getParam(req, "langOut").toLowerCase(); //values: langueage of processed data (only for mode validate)

            mode  = mode .isEmpty() ? "validate" : mode;
            input = input.isEmpty() ? "xml"      : input;

            String status;
            String resultTitle;
            String result;
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
                    resultTitle = "X-definition error(s)";
                    result = printReports(reporter, xdef);

                } else {
                    Element             resultElement = null;
                    Object              resultXon     = null;
                    Map<String, Object> resultIni     = null;
                    List<Object>        resultCsv     = null;

                    reporter.clear();
                    CharArrayWriter caw = new CharArrayWriter();
                    XDOutput stdout = XDFactory.createXDOutput(caw, false);

                    XDDocument xd = xp.createXDDocument(xdName);

                    xd.setProperties(props);
                    xd.setStdOut(stdout);

                    if ("compose".equals(mode)) {
                        String name;
                        String uri;
                        XMDefinition def = xd.getXMDefinition();
                        if (!mName.isEmpty()) {
                            name = mName;
                            uri = !mURI.isEmpty() ? mURI : null;
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
                                csvHeaderExport.isEmpty() || csvHeaderExport.equals("no"),
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
                        resultTitle = "Input data error(s)";
                        result = printReports(reporter, data);
                    } else {
                        status = "OK";
                        resultTitle = "Result " + input.toUpperCase() + " - mode \"" + mode + "\"";

                        if (resultElement != null) {
                            result = KXmlUtils.nodeToString(resultElement, true, false, true, 120);
                        } else if (resultXon != null) {
                            if (input.equals("json")) {
                                result = XonUtils.toJsonString(resultXon, true);
                            } else if (input.equals("yaml")) {
                                Yaml yaml = new Yaml();
                                result = yaml.dump(XonUtils.xonToJson(resultXon));
                            } else if (resultCsv != null) {
                                result = XonUtils.toCsvString(resultCsv);
                            } else if (resultIni != null){
                                result = XonUtils.toIniString(resultIni);
                            } else {
                                result = XonUtils.toXonString(resultXon, true);
                            }

                            if (xonDisplayAs.contains("xml")) {
                                if (resultCsv != null) {
                                    result = KXmlUtils.nodeToString(
                                        XonUtils.csvToXml(resultCsv)
                                    );
                                } else if (resultIni != null) {
                                    result = KXmlUtils.nodeToString(
                                        XonUtils.iniToXml(XonUtils.xonToJson(resultXon)),
                                        true, false, true, 110
                                    );
                                } else {
                                    result = KXmlUtils.nodeToString(
                                        XonUtils.xonToXml(XonUtils.xonToJson(resultXon)),
                                        true, false, true, 110
                                    );
                                }
                            }
                        } else {
                            throw new RuntimeException("No result - shouldn't happen");
                        }
                    }
                    if (caw.size() > 0) {
                        stdOutput = caw.toString();
                    }
                }
            } catch (SRuntimeException ex) {
                status = "Error";
                resultTitle = "Input data error(s)";
                if ("SYS024".equals(ex.getMsgID())) {
                    reporter.putReport(Report.fatal(XML.XML080, //XML parser was canceled by error&{0}{: }
                        "The XML document must start with '<'", "&{line}1&{column}1"));
                } else if (!reporter.errorWarnings()) {
                    reporter.putReport(Report.fatal(ex.getMsgID(),
                        ex.getReport().getText(), ex.getReport().getModification()));
                }
                result = printReports(reporter, data);
                reporter.reset();
            }

            boolean stdOutputEx = stdOutput != null && !stdOutput.isEmpty();
            boolean isResHtml   = result.startsWith("<html");
            xonDisplayAs.remove(input);

            String outHtml;
            outHtml = SUtils.modifyFirst(HTML_RESULT, "((xdef-lib-id))", XDConstants.BUILD_IDENTIFIER);
            outHtml = SUtils.modifyString(outHtml, "((status))", 	     status);
            outHtml = SUtils.modifyFirst(outHtml,  "((result-title))",   resultTitle);
            outHtml = SUtils.modifyFirst(outHtml,  "((result))",    	 preStringToPre(result));
            outHtml = SUtils.modifyFirst(outHtml,  "((html-div))", 	     isResHtml ? "block" : "none");
            if (isResHtml) {
                outHtml = SUtils.modifyFirst(outHtml, "((result-html))", htmlStringToAttr(result));
            }
        	outHtml = SUtils.modifyFirst(outHtml, "((stdout-div))",      stdOutputEx ? "block" : "none");
        	outHtml = SUtils.modifyFirst(outHtml, "((stdout-none))",     stdOutputEx ? "none"  : "block");
            if (stdOutputEx) {
            	outHtml = SUtils.modifyFirst(outHtml, "((stdout))",      preStringToPre(stdOutput));
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

}