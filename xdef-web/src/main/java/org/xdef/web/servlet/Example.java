package org.xdef.web.servlet;

import static org.xdef.sys.SUtils.modifyFirst;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;
import org.yaml.snakeyaml.Yaml;

/** Servlet for execution of examples from tutorial.
 * @author Vaclav Trojan
 */
public final class Example extends AbstractMyServlet {
    private static final long serialVersionUID = 2277695929503402350L;
    private static final String HTML_RESULT =
"<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
"  <head>\n" +
"     <meta http-equiv='content-type' content='text/html; charset=UTF-8'/>\n"+
"     <title>&{title}</title>\n" +
"  </head>\n" +
"  <body>\n" +
"    <div align=\"right\">\n" +
"      <font size=1><i style=\"test-align:right\"><b>X-definition version: "
            + XDConstants.BUILD_IDENTIFIER
            + "</b></i>\n" +
"      </font>\n" +
"    </div>\n" +
"    <p><font size=4><b>&{result-title}</b></font></p>\n" +
"    <pre><tt>&{result}</tt></pre>&{stdout}"+
"  </body>\n" +
"</html>";

    /** Convert result of YAML parser to JSON.
     * @param o result of YAML parser.
     * @return JSON result.
     */
    private static Object yamlToJson(final Object o) {
        if (null == o) return null;
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
            String mode = getParam(req, "mode");
            String xdef = getParam(req, "xdef");
            String data = getParam(req, "data");
            String xdName = getParam(req, "xdname");
            String mName = getParam(req, "mName");
            String mURI = getParam(req, "mURI");
            String view = getParam(req, "view");
            String json = getParam(req, "json");
            String ini = getParam(req, "ini");
            String csv = getParam(req, "csv");
            String csvMode = getParam(req, "csvMode");
            String inLex = getParam(req, "inLex");
            String outLex = getParam(req, "outLex");

            String outHtml = HTML_RESULT;
            String result;
            String stdOutput = "";
            PrintWriter out = resp.getWriter();
            XDPool xp = null;
            Properties props = new Properties();
            props.setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
            try {
                XDBuilder xb = XDFactory.getXDBuilder(reporter, props);
                xb.setSource(xdef);
                xp = xb.compileXD();
            } catch (Throwable ex) {
                if (null == xp || !reporter.errorWarnings()) {
                    out.print(genHtmlMessage("Exception",
                        "<pre><tt><b>" + stringToHTml(STester.printThrowable(ex), true) + "</b></tt></pre>"));
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
                    result = printReports(reporter, xdef);
                    outHtml = modifyFirst(outHtml, "&{title}", "Error");
                    outHtml = modifyFirst(outHtml, "&{result-title}", "X-definition error(s):");
                } else {
                    Object resultXon = null;
                    Map<String, Object> resultIni = null;
                    List<Object> resultCsv = null;
                    Element resultElement = null;
                    reporter.clear();
                    CharArrayWriter caw = new CharArrayWriter();
                    XDOutput stdout = XDFactory.createXDOutput(caw, false);
                    XDDocument xd;
                    try {
                        xd = xp.createXDDocument(xdName);
                    } catch (RuntimeException ex) {
                        xd = xp.createXDDocument("Example");
                    }
                    xd.setProperties(props);
                    xd.setStdOut(stdout);
                    if ("compose".equals(mode)) {
                        String name;
                        String uri;
                        XMDefinition def =  xd.getXMDefinition();
                        if (null != mName && !(mName = mName.trim()).isEmpty()) {
                            name = mName;
                            uri = null != mURI  && !(mURI=mURI.trim()).isEmpty() ? mURI : null;
                        } else {
                            XMElement[] x = xd.getXMDefinition().getModels();
                            name = x[0].getName();
                            uri = x[0].getNSUri();
                        }
                        if (null != data && data.trim().length() > 0) {
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
                        if ("json".equals(json)) {
                            String s;
                            if (data.trim().startsWith("<") && data.trim().endsWith(">")) { // XML
                                s = XonUtils.toJsonString(XonUtils.xmlToXon(data), true);
                            } else {
                                s = data;
                                try { // try XON/JSON
                                    XonUtils.parseXON(data);
                                } catch (SRuntimeException ex) {
                                    try {
                                        Object o;
                                        try (StringReader sr = new StringReader(data)) {
                                            Yaml yaml = new Yaml();
                                            o = yaml.load(sr);
                                        }
                                        o = yamlToJson(o);
                                        s = XonUtils.toJsonString(o, true);
                                    } catch (Exception exy) {} // will be error
                                }
                            }
                            xd.jparse(s, reporter);
                            data = s;
                            resultXon = xd.getXon();
                        } else if ("ini".equals(ini)) {
                            resultXon = resultIni = xd.iparse(data.trim(), reporter);
                        } else if ("csv".equals(csv)) {
                            resultXon = resultCsv = xd.cparse(new StringReader(data.trim()),
                                ',', // separator
                                csvMode.isEmpty() || csvMode.equals("no"),
                                null, // source name
                                reporter);
                        } else {
                            if (inLex.isEmpty() && outLex.isEmpty() || inLex.equals(resultXon)) {
                                resultElement = xd.xparse(data, reporter);
                            } else {
                                if (inLex.equals(resultXon)|| outLex.isEmpty()) {
                                    xd.setLexiconLanguage(inLex);
                                    resultElement = xd.xparse(data, reporter);
                                } else {
                                    resultElement = xd.xtranslate(data, inLex, outLex, reporter);
                                }
                            }
                        }
                    }
                    caw.close();
                    if (reporter.errors()) {
                        outHtml = modifyFirst(outHtml, "&{title}", "Error");
                        outHtml = modifyFirst(outHtml, "&{result-title}", "Input data error(s):");
                        result = printReports(reporter, data);
                    } else {
                        outHtml = modifyFirst(outHtml, "&{title}", "Result");
                        if (null!=view && view.toLowerCase().contains("html")) {
                            out.println(KXmlUtils.nodeToString(resultElement,true,false,true,130));
                            return;
                        } else if(json.isEmpty() && csv.isEmpty() && ini.isEmpty()) {
                            outHtml = modifyFirst(outHtml, "&{result-title}", "XML result:");
                            result = KXmlUtils.nodeToString(resultElement, true, false, true, 110);
                        } else {
                            if (null!=view && view.isEmpty()) {
                                view = !ini.isEmpty() ? "INI" : !csv.isEmpty() ? "CSV" : "JSON";
                            }
                            if (null!=view && view.contains("YAML")) {
                                outHtml = modifyFirst(outHtml, "&{result-title}", "YAML result:");
                                Yaml yaml = new Yaml();
                                result = yaml.dump(XonUtils.xonToJson(resultXon));
                            } else if (null!=view && view.contains("XON")) {
                                outHtml = modifyFirst(outHtml, "&{result-title}", "XON result:");
                                result = XonUtils.toXonString(resultXon, true);
                            } else if (null!=view && view.contains("XML")) {
                                outHtml = modifyFirst(outHtml, "&{result-title}", "XML result:");
                                if (!csv.isEmpty() && resultCsv!=null) {
                                    result = KXmlUtils.nodeToString(XonUtils.csvToXml(resultCsv));
                                } else if (!ini.isEmpty() && resultIni != null) {
                                    result = KXmlUtils.nodeToString(XonUtils.iniToXml(
                                        XonUtils.xonToJson(resultXon)), true, false, true, 110);
                                } else {
                                    result = KXmlUtils.nodeToString(XonUtils.xonToXml(
                                        XonUtils.xonToJson(resultXon)), true, false, true, 110);
                                }
                            } else if (null!=view && view.contains("CSV") && resultCsv!=null){
                                outHtml = modifyFirst(outHtml, "&{result-title}", "CSV result:");
                                result = XonUtils.toCsvString(resultCsv);
                            } else if (null!=view && view.contains("INI") && resultIni!=null){
                                outHtml = modifyFirst(outHtml, "&{result-title}", "INI result:");
                                result = XonUtils.toIniString(resultIni);
                            } else {
                                outHtml = modifyFirst(outHtml, "&{result-title}", "JSON result:");
                                result = XonUtils.toJsonString(resultXon,true);
                            }
                        }
                    }
                    if (caw.size() > 0) {
                        stdOutput = "<h3>Output stream (System.out):</h3><pre><tt>"
                            + stringToHTml(caw.toString(), true) + "</tt></pre>";
                    }
                }
            } catch (SRuntimeException ex) {
                outHtml = modifyFirst(outHtml, "&{title}", "Error");
                outHtml = modifyFirst(outHtml, "&{result-title}", "Input data error(s):");
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
            outHtml = modifyFirst(outHtml, "&{result}", stringToHTml(result, true));
            outHtml = modifyFirst(outHtml, "&{stdout}", stdOutput);
            out.print(outHtml);
        }
    }

    /** Returns a short description of this servlet.
     * @return short description of this servlet.     */
    @Override
    public final String getServletInfo() {return "This servlet executes a X-definition with given XML data";}
}