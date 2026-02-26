 package org.xdef.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Element;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.Report;
import org.xdef.sys.STester;
import org.xdef.util.XdefToXsd;
import org.xdef.xml.KXmlUtils;
import org.xml.sax.SAXException;

/** Servlet for execution of examples from tutorial.
 * @author Vaclav Trojan
 */
public final class GenSchema extends AbstractMyServlet {

    private static final long serialVersionUID = -7389516366202036753L;

    /** Generate X-definition and run validation of given object with created X-definition.
     * @param req servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public void procReq(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException{
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // This part we must synchronize to keep language settings for whole process of the X-definition.
        synchronized(MANAGER) {
            Report.setLanguage("eng");
            String view = getParam(req, "view");
            String xdName = getParam(req, "xdName");
            String xdef = getParam(req, "xdef");
            String data = getParam(req, "data");
            String schemaResult = getParam(req, "schemaResult");
            String schema = getParam(req, "schema");
            PrintWriter out = resp.getWriter();
            try {
                if ("toSchema".equals(schema)) {
                    XDPool xp = XDFactory.compileXD(null, xdef);
                    Map<String, Element> map = XdefToXsd.genSchema(xp, null, null, null, null, true, true);
                    String xd = "";
                    for (Entry<String, Element> x : map.entrySet()) {
                        if (map.entrySet().size() > 1) {
                            xd += "==========  Name: " + x.getKey() + "  ==========\n";
                        }
                        xd += KXmlUtils.nodeToString(x.getValue(), true, true, true, 110);
                    }
                    if ("Generate XML schema".equals(view)) {
                        out.print(
"<html xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <head>\n"+
"    <meta http-equiv='content-type' content='text/html; charset=UTF-8' />\n"+
"    <script src='/tutorial/styles/lineNumbers.js'> </script>\n" +
"    <script src='/tutorial/styles/lineNumbers_1.js'> </script>\n" +
"    <style>.container{min-height:17;max-height:17rem;display:flex;overflow:hidden;}</style>\n" +
"    <title>XML schema</title>\n" +
"  </head>\n" +
"  <body style='background: #EAFFFD'>\n" +
//"    <form method='post' action='/tutorial/GenSchema' target='_blank'>\n" +
"    <form method='post' action='/tutorial/GenSchema'>\n" +
"      <b>Created XML schema</b>\n" +
"      <div class='container'>\n" +
"        <div id='line-numbers' class='container_1'></div>\n" +
"        <textarea id='textarea' style='width: 100%;' name='schemaResult'>\n" +
                    stringToHTml(xd, true) + "\n" +
"</textarea>\n" +
"      </div>\n"+
"      <b>XML data</b>\n" +
"      <div class=\"container\">\n" +
"        <div id=\"line-numbers_1\" class=\"container_1\"></div>\n" +
"        <textarea id=\"textarea_1\" style=\"width: 100%;\" name=\"data\">\n" +
                stringToHTml(data, true) + "\n" +
"</textarea>\n" +
"      </div>\n" +
"      <input type='hidden' name='schema' value='checkSchema' />\n" +
"      <input name='view' value='Check created XML schema with XML data'\n" +
"             type='submit'/>\n"+
"    </form>\n" +
"  </body>\n" +
"</html>");
                    } else if ("Check XML data with X-definition".equals(view)){
                        try {
                            XDDocument xdoc;
                            if (xdName.isEmpty()) {
                                try {
                                    xdoc = xp.createXDDocument();
                                } catch (RuntimeException ex) {
                                    xdoc = xp.createXDDocument("Example");
                                }
                            } else {
                                xdoc = xp.createXDDocument(xdName);
                            }
                            xdoc.xparse(data.trim(), null);
                            out.print("<html><body><h1>OK</h1></body></html>");
                        } catch (RuntimeException ex) {
                            out.print("<html><body><h1>Exception</h1><b>Error:</b><pre><tt><b>"
                                + stringToHTml(ex.toString(),true) + "</b></tt></pre></body></html>");
                        }
                    } else {
                        out.print("<html><body><h1>Exception</h1>"
                            + "<b>Error:UNKNOWN COMMAND</b></body></html>");
                    }
                } else {
                    Validator validator;
                    try {// create validator
                        SchemaFactory sFactory= SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        Source schemaSource = new StreamSource(new StringReader(schemaResult));
                        Schema nschema =  sFactory.newSchema(schemaSource);
                        validator = nschema.newValidator();
                    } catch (SAXException ex) {
                        out.print("<html><body><h1>Exception</h1><b>Error:</b><pre><tt><b>"
                            + stringToHTml(ex.toString(),true) + "</b></tt></pre></body></html>");
                        return;
                    }
                    try {//check by XML schema
                        validator.validate(new StreamSource(new StringReader(data)));
                        out.print("<html><body><h1>OK</h1></body></html>");
                    } catch (IOException | SAXException ex) {
                        out.print("<html><body><h1>Exception</h1><b>Error:</b><pre><tt><b>"
                            + stringToHTml(ex.toString(),true) + "</b></tt></pre></body></html>");
                    }
                }
            } catch (RuntimeException ex) {
                out.print("<html><body><h1>Exception</h1><pre><tt><b>"
                    + stringToHTml(STester.printThrowable(ex),true) + "</b></tt></pre></body></html>");
            }
        }
    }

    /** Returns a short description of this servlet.
     * @return short description of this servlet.     */
    @Override
    public final String getServletInfo() {return "This servlet creates an X-definition from given XML";}
}