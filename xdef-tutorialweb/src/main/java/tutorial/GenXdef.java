package tutorial;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;
import org.xdef.sys.Report;
import org.xdef.sys.STester;
import org.xdef.util.GenXDefinition;
import org.xdef.xml.KXmlUtils;

/** Servlet for execution of examples from tutorial.
 * @author Vaclav Trojan
 */
public final class GenXdef extends AbstractMyServlet {

	private static final long serialVersionUID = -815756752335589510L;

    /** Generate X-definition and run validation of given object with created X-definition.
	 * @param req servlet request object.
	 * @param resp servlet response object.
	 * @throws IOException if an error occurs.
	 */
	@Override
	public void procReq(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException,IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		// This part we must synchronize to keep language settings for whole process of the X-definition.
		synchronized(MANAGER) {
			Report.setLanguage("eng");
			String data = getParam(req, "data");
			PrintWriter out = resp.getWriter();
			try {
				Element el = GenXDefinition.genXdef(data.trim(), "Example");
				StringWriter swr = new StringWriter();
				KXmlUtils.writeXml(swr, "UTF-8", el, " ", false, false, true);
				swr.close();
				String xdef = '\n' + swr.toString().trim() + '\n';
				out.print(
"<html xmlns='http://www.w3.org/1999/xhtml'>\n"+
"  <head>\n"+
"    <meta http-equiv='content-type' content='text/html; charset=UTF-8' />\n"+
"    <script src='/tutorial/styles/lineNumbers.js'> </script>\n" +
"    <script src='/tutorial/styles/lineNumbers_1.js'> </script>\n" +
"    <style>.container{min-height:17rem;max-height:17rem;display:flex;overflow:hidden;}</style>\n" +
"    <title>Validate data witn created X-definition</title>\n" +
"  </head>\n" +
"  <body style='background: #EAFFFD'>\n" +
//"    <form method='post' action='/tutorial/Examples' target=\"_blank\">\n" +
"    <form method='post' action='/tutorial/Examples'>\n" +
"      <b>Created X-definition</b>\n" +
"      <div class='container'>\n" +
"        <div id='line-numbers' class='container_1'></div>\n" +
"        <textarea id='textarea' style='width: 100%;' name='xdef'>\n" +
				stringToHTml(xdef, true)+
"</textarea>\n" +
"      </div>\n"+
"      <b>Input data</b>\n" +
"      <div class=\"container\">\n" +
"        <div id=\"line-numbers_1\" class=\"container_1\"></div>\n" +
"        <textarea id=\"textarea_1\" style=\"width: 100%;\" name=\"data\">\n" +
				stringToHTml(data, true).trim()+
"</textarea>\n" +
"      </div>\n");
				if (data.startsWith("<")) { //data is XML format
					out.print(
"      <input name='submit' value='Execute' type='submit' />\n");
				} else {  // data is JSON format (??? - TODO other formats)
					out.print(
"      <input type='hidden' name='json' value='json' />\n" +
"      <input name='view' value='Display result as JSON' type='submit'/>\n" +
"      <input name='view' value='Display result as YAML' type='submit'/>\n" +
"      <input name='view' value='Display result as XON' type='submit'/>\n" +
"      <input name='view' value='Display result as XML' type='submit'/>\n");
				}
				out.print(
"      <i>&nbsp;\n" +
"        You can edit <b>Created X-definition</b> or <b>Input</b> window\n" +
"      </i>\n" +
"    </form>\n" +
"  </body>\n" +
"</html>");
			} catch (IOException | RuntimeException ex) {
				out.print("<html><body><h1>Exception</h1>" +
					"<pre><tt><b>"+stringToHTml(STester.printThrowable(ex),true)
					+ "</b></tt></pre></body></html>");
			}
		}
	}

	/** Returns a short description of this servlet.
	 * @return short description of this servlet.	 */
	@Override
	public final String getServletInfo() {return "This servlet creates an X-definition from given XML";}
}