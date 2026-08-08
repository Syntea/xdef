package org.xdef.web.servlet;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.xdef.sys.STester;
import org.xdef.util.XdefToXsd;
import org.xdef.web.util.ServletUtil;
import org.xdef.xml.KXmlUtils;
import org.xml.sax.SAXException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to create a XML-schema from given X-definition.
 *
 * @author Vaclav Trojan
 */
public final class GenSchema extends XdefServletAbs {

    private static final long serialVersionUID = -7389516366202036753L;

    private static final String responseHtmlTempl =
        ServletUtil.readRsrcAsString(GenSchema.class, "webapp/playground/genschema-response-template.html");

    /** default constructor, calls super() only */
    public GenSchema() {
        super();
    }

    /** see {@link XdefServletAbs#getServletInfo()}
     * @return ...
     */
    @Override
    public final String getServletInfo() {
        return "This servlet creates a XML-schema from given X-definition";
    }

    /** Generate X-definition and run validation of given object with created X-definition.
     * @param req servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    protected void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException
    {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        RequestParams rp = new RequestParams(req);
        String respHtml;
        try {
            respHtml = "toSchema".equals(rp.schema) ? processToSchema(rp) : processCheckSchema(rp);
        } catch (RuntimeException ex) {
            respHtml = ServletUtil.genHtmlMessage("Exception",
                "<pre><tt><b>" + ServletUtil.preTextToPreCont(STester.printThrowable(ex)) + "</b></tt></pre>");
        }
        resp.getWriter().print(respHtml);
    }

    /**
     * Generate an XML schema from the given X-definition and either display it in the edit form
     * (view "Generate XML schema") or validate the given XML data with the given X-definition
     * (view "Check XML data with X-definition").
     *
     * @param rp request parameters.
     * @return html-response
     */
    private String processToSchema(final RequestParams rp) {
        XDPool xp = XDFactory.compileXD(null, rp.xdef);
        Map<String, Element> map = XdefToXsd.genSchema(xp, null, null, null, null, true, true);
        StringBuilder xd = new StringBuilder();
        for (Entry<String, Element> x : map.entrySet()) {
            if (map.size() > 1) {
                xd.append("==========  Name: ").append(x.getKey()).append("  ==========\n");
            }
            xd.append(KXmlUtils.nodeToString(x.getValue(), true, true, true, 110));
        }

        if ("Generate XML schema".equals(rp.view)) {
            return assembleSchemaFormResponse(xd.toString(), rp.data);
        } else if ("Check XML data with X-definition".equals(rp.view)) {
            try {
                XDDocument xdoc;
                if (rp.xdName.isEmpty()) {
                    try {
                        xdoc = xp.createXDDocument();
                    } catch (RuntimeException ex) {
                        xdoc = xp.createXDDocument("Example");
                    }
                } else {
                    xdoc = xp.createXDDocument(rp.xdName);
                }
                xdoc.xparse(rp.data.trim(), null);
                return ServletUtil.genHtmlMessage("OK", "");
            } catch (RuntimeException ex) {
                return ServletUtil.genHtmlMessage("Exception",
                    "<b>Error:</b><pre><tt><b>" + ServletUtil.preTextToPreCont(ex.toString()) + "</b></tt></pre>");
            }
        } else {
            return ServletUtil.genHtmlMessage("Exception", "<b>Error: UNKNOWN COMMAND</b>");
        }
    }

    /**
     * Validate the given XML data against the given XML schema (the "checkSchema" flow).
     *
     * @param rp request parameters.
     * @return html-response
     */
    private String processCheckSchema(final RequestParams rp) {
        Validator validator;
        try {
            SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaSource = new StreamSource(new StringReader(rp.schemaResult));
            Schema nschema = sFactory.newSchema(schemaSource);
            validator = nschema.newValidator();
        } catch (SAXException ex) {
            return ServletUtil.genHtmlMessage("Exception",
                "<b>Error:</b><pre><tt><b>" + ServletUtil.preTextToPreCont(ex.toString()) + "</b></tt></pre>");
        }
        try {
            validator.validate(new StreamSource(new StringReader(rp.data)));
            return ServletUtil.genHtmlMessage("OK", "");
        } catch (IOException | SAXException ex) {
            return ServletUtil.genHtmlMessage("Exception",
                "<b>Error:</b><pre><tt><b>" + ServletUtil.preTextToPreCont(ex.toString()) + "</b></tt></pre>");
        }
    }

    /**
     * Assemble html-response with the generated XML schema and input data ready for a check-schema round-trip.
     *
     * @param schemaResult generated XML schema.
     * @param data XML data to be validated against the schema.
     * @return html-response
     */
    private String assembleSchemaFormResponse(final String schemaResult, final String data) {
        Map<String, String> values = new HashMap<>();
        values.put("schemaResult", ServletUtil.preTextToPreCont(schemaResult));
        values.put("schemaLines",  Integer.toString(schemaResult.split("\n").length + 1));
        values.put("data",         ServletUtil.preTextToPreCont(data));
        values.put("dataLines",    Integer.toString(data.split("\n").length + 1));

        return ServletUtil.mustache(responseHtmlTempl, values);
    }

    /** request parameters */
    private static class RequestParams {
        /** values: "Generate XML schema", "Check XML data with X-definition" */
        String view;
        /** name of root X-definition, in case of X-definition collection */
        String xdName;
        /** X-definition (xml-format) */
        String xdef;
        /** XML data */
        String data;
        /** generated (or edited) XML schema */
        String schemaResult;
        /** values: "toSchema" (generate/check with X-definition), "checkSchema" (check with XML schema) */
        String schema;

        private RequestParams(final HttpServletRequest req) {
            view         = ServletUtil.getParam(req, "view");
            xdName       = ServletUtil.getParam(req, "xdName");
            xdef         = ServletUtil.getParam(req, "xdef");
            data         = ServletUtil.getParam(req, "data");
            schemaResult = ServletUtil.getParam(req, "schemaResult");
            schema       = ServletUtil.getParam(req, "schema");
        }
    }
}
