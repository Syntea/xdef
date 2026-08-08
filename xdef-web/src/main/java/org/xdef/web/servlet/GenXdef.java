package org.xdef.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.xdef.sys.STester;
import org.xdef.util.GenXDefinition;
import org.xdef.web.util.ServletUtil;
import org.xdef.xml.KXmlUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to create an X-definition from given XML.
 *
 * @author Vaclav Trojan
 */
public final class GenXdef extends XdefServletAbs {

    private static final long serialVersionUID = -815756752335589510L;

    private static final String responseHtmlTempl =
        ServletUtil.readRsrcAsString(GenXdef.class, "webapp/playground/genxdef-response-template.html");

    /** submit-control shown when the input data is XML. */
    private static final String SUBMIT_XML =
        "<input name='submit' value='Execute' type='submit' />"
    ;
    /** submit-control shown when the input data is JSON (or another XON-like format). */
    private static final String SUBMIT_XON =
        "<input type='hidden' name='dataFormat' value='xon'/>\n" +
        "<input type='hidden' name='xonDisplayAs' value='json yaml xon xml'/>\n" +
        "<button type='submit'>Execute</button>"
    ;

    /** default constructor, calls super() only */
    public GenXdef() {
        super();
    }

    /** see {@link XdefServletAbs#getServletInfo()}
     * @return ...
     */
    @Override
    public final String getServletInfo() {
        return "This servlet creates an X-definition from given XML";
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

        String data = ServletUtil.getParam(req, "data");
        PrintWriter out = resp.getWriter();
        try {
            out.print(assembleResponse(data));
        } catch (IOException | RuntimeException ex) {
            out.print(ServletUtil.genHtmlMessage("Exception",
                "<pre><tt><b>" + ServletUtil.preTextToPreCont(STester.printThrowable(ex)) + "</b></tt></pre>"));
        }
    }

    /**
     * Generate the X-definition from given data and assemble the html-response with the generated
     * X-definition and the input data, ready for validation via the Playground servlet.
     *
     * @param data input data (XML or JSON) from which the X-definition is generated.
     * @return html-response
     * @throws IOException if an error occurs.
     */
    private String assembleResponse(final String data) throws IOException {
        Element el = GenXDefinition.genXdef(data.trim(), "Example");
        StringWriter swr = new StringWriter();
        KXmlUtils.writeXml(swr, "UTF-8", el, " ", false, false, true);
        swr.close();
        String xdef = '\n' + swr.toString().trim() + '\n';
        String trimmedData = data.trim();

        Map<String, String> values = new HashMap<>();
        values.put("xdef",           ServletUtil.preTextToPreCont(xdef));
        values.put("xdefLines",      Integer.toString(xdef.split("\n").length + 1));
        values.put("data",           ServletUtil.preTextToPreCont(trimmedData));
        values.put("dataLines",      Integer.toString(trimmedData.split("\n").length + 1));
        values.put("submit-control", trimmedData.startsWith("<") ? SUBMIT_XML : SUBMIT_XON);

        return ServletUtil.mustache(responseHtmlTempl, values);
    }

}
