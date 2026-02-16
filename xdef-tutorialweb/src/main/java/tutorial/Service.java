package tutorial;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.xdef.sys.FUtils;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;

/** The servlet for execution of server service commands.
 * @author Vaclav Trojan
 */
@MultipartConfig
public final class Service extends AbstractMyServlet {

	private static final long serialVersionUID = 8846128427001680285L;

    private static String getDirInfo(final File dir) {
		if (dir == null) {
			return "directory parameter is null<br/>\n";
		}
		String result = "directory: " + dir.getAbsolutePath();
		File[] files = dir.listFiles();
		if (null == files || files.length == 0) {
			 return result + " (empty)<br/>\n";
		}
		result += "<br/>\n";
		for (File x : files) {
			if (x.isDirectory()) {
				result += "- " + getDirInfo(x);
			} else {
				result += "- " + x.getName() + " , size = " + x.length() + ", date: ";
				SDatetime date = new SDatetime(new Date(x.lastModified()));
				result += date.formatDate("yyyy-MM-dd HH:mm") + "<br/>\n";
			}
		}
		return result;
	}

	/** Display information about request.
	 * @param out Print writer from request/
	 * @param title name request/
	 */
	private static void writeHttpHdr( final PrintWriter out, final String title) {
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n  <head><title>"
			+ title + "</title></head>\n<body>\n");
	}

	/** Processes requests with respect to required language. The Language is set according to request
	 * parameter "submit".
	 * @param req servlet request.
	 * @param resp servlet response.
	 * @throws ServletException if servlet error occurs.
	 * @throws IOException if an IO error occurs.
	 */
	@Override
	public final void procReq(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException,IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		// This part we must synchronize to keep language settings for whole process of the X-definition.
		synchronized(MANAGER) {
			Report.setLanguage("eng");
			String contentType = (contentType = req.getContentType()) == null ? "" : contentType;
			if (null != req.getParameter("fileDownLoad")) {
				String s = getParam(req, "fileDownLoad");
				File f = new File(new File(_baseDir, "data"), s);
				if (!f.exists() || !f.isFile()) {
					resp.getWriter().println("File " + s + " not exists");
				} else if (f.length() > 100000000) { // 100MB max
					resp.getWriter().println("File "+s+" is longer then 100MB");
				} else {
					synchronized (MANAGER) {
						byte[] bytes;
						try {
							bytes = FUtils.readBytes(f);
						} catch (SException ex) {
							resp.getWriter().println("Error:  " + ex);
							return;
						}
						String chs = getParam(req, "chset");
						if (!chs.isEmpty()) {
							try {
								bytes = new String(bytes,"UTF-8").getBytes(chs);
							} catch (UnsupportedEncodingException ex) {
								resp.getWriter().println("Error:  " + ex);
								return;
							}
						}
						resp.setHeader("Content-Disposition",
							"attachment; filename=\"" + f.getName() + "\";");
						try (ServletOutputStream os = resp.getOutputStream()) {
							os.write(bytes);
							os.flush();
							os.close();
						}
					}
				}
				return;
			}
			PrintWriter out = resp.getWriter();
			if (null != req.getParameter("fileDelete")) {
				String s = getParam(req, "fileDelete");
				if (s.isEmpty()) {
					out.println("Error: No file selected.");
					return;
				}
				writeHttpHdr(out, "FileDelete");
				File f = new File(_baseDir, s);
				if (f.exists()) {
					char ch = s.charAt(s.length()-1);
					if (f.isDirectory() && (ch == '/' || ch =='\\')) {
						String name;
						if ("temp400MB".equals(name=f.getName()) || "temp100MB".equals(name)
							|| "data".equals(name)) {
							out.println("ERROR: directory " + s + " is not alowed to delete");
						} else {
							File[] ff = new File[] {f};
							deleteFiles(ff);
							out.println(f.exists() ? "ERROR: Directory " + s + " can't delete"
								: "Directory " + s + " was deleted");
						}
					} else {
						out.println(f.isFile() ? f.delete()
							? "File " + s + " deleted" : "ERROR: " + s + " can't delete"
							: "ERROR: " + s + " is not not file");
					}
				} else {
					out.println("File " + s + " not exists");
				}
				out.print("</body>\n</html>");
			} else if (contentType.startsWith("multipart/form-data")) {
				writeHttpHdr(out, "Upload");
				Part part;
				if (null != (part = req.getPart("uploadfile"))) {
					String fileName = part.getSubmittedFileName();
					if (null == fileName || fileName.isEmpty()) {
						out.println("Error : no file selected");
					} else {
						for (Part p : req.getParts()) {
							out.println(" Part: "+ p.getName() +", SubmittedFileName: "+ fileName +"<br/>");
							if (null == _dataDir) {
								out.println(" Error: upload directory is null");
								return;
							}
							File f = new File(_dataDir, fileName);
							boolean exists = f.exists();
							p.write(f.getCanonicalPath());
							out.println("The file " + fileName + " was "
								+ (exists ? "replaced in data directory" : "uploaded to data directory"));
						}
					}
				} else {
					out.println("Error: expected part name 'uploadfile'");
				}
				out.print("<body/>\n<html/>");
			} else if (null != req.getParameter("info")) {
				out.println(
"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <head>\n" +
"    <title>Server info</title>\n" +
"  </head>\n" +
"  <body>");
				out.println(getDirInfo(_tempDir100MB) + "\n<br/>");
				out.println(getDirInfo(_tempDir400MB) + "\n<br/>");
				out.println(getDirInfo(_dataDir));
				out.print("</body>\n</html>");
			} else {
				out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
"  <head> <title>Server command</title> </head>\n" +
"  <body>     \n" +
//"    <form style='background: #EAFFFD' method=\"post\" target='_blank'\n" +
"    <form style='background: #EAFFFD' method=\"post\"\n" +
"          action='/tutorial/Service' enctype='multipart/form-data'>");
				if (!getParam(req, "upload").isEmpty()) {
					out.println(
"      <b>Choose a file to upload:</b>\n" +
"      <input name='uploadfile' type='file' />\n" +
"      <br/>\n" +
"      <input type=\"submit\" value=\"Upload\" />");
				} else if (!getParam(req, "download").isEmpty()) {
					out.println(
"      <b>File name:</b>\n" +
"      <input type=\"text\" name=\"fileDownLoad\" value=\"\"/>\n" +
"      <input type=\"submit\" value=\"download\" />");
				} else if (!getParam(req, "upload").isEmpty()) {
					out.println(
"      <span style='font-family:\"Sylfaen\",\"serif\"'>\n" +
"        <b>Directory/filename:</b>\n" +
"        <input type=\"text\" name=\"fileUpload\" value=\"\"/>\n" +
"      </span>\n"+
"      <input type=\"submit\" value=\"upload\" />");
				} else if (!getParam(req, "delete").isEmpty()) {
					out.println(
"      <span style='font-family:\"Sylfaen\",\"serif\"'>\n" +
"        <b>Directory/filename:</b>\n" +
"        <input type=\"text\" name=\"fileDelete\" value=\"\"/>\n" +
"      </span>\n"+
"      <input type=\"submit\" value=\"delete\" />");
				} else {
					out.println("Unknown command");
				}
				out.print("    </form>\n  </body>\n</html>");
			}
		}
	}

	/** Returns a short description of this servlet.
	 * @return short description of this servlet.
	 */
	@Override
	public final String getServletInfo() {return "This servlet supports service commands on server";}
}