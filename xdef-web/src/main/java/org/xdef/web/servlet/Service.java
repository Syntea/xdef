package org.xdef.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.xdef.sys.FUtils;
import org.xdef.sys.Report;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SException;
import org.xdef.web.util.ServletUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/** The servlet for execution of server service commands.
 * @author Vaclav Trojan
 */
@MultipartConfig
public final class Service extends XdefServletAbs {

    private static final long serialVersionUID = 8846128427001680285L;

    /** Base directory. */
    protected File _baseDir         = null;
    /** Temporary directory for 100MB. */
    protected File _tempDir100MB    = null;
    /** Temporary directory for 400MB. */
    protected File _tempDir400MB    = null;
    /** Directory with data. */
    protected File _dataDir         = null;


    /** default constructor, calls super() only */
    public Service() {
        super();
    }

    /** Init this servlet. Set directories for temporary data. */
    @Override
    public void init() {
        File f = new File("/opt/tutorial");
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException( "Directory /opt/tutorial is not available");
        }
        _baseDir = f;
        f = new File(_baseDir, "temp100MB");
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Directory /opt/tutorial/temp100MB is not available");
        }
        _tempDir100MB = f;
        f = new File(_baseDir, "temp400MB");
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Directory /opt/tutorial/temp400MB is not available");
        }
        _tempDir400MB = f;
        f = new File(_baseDir, "data");
        if (!f.exists() || !f.isDirectory()) {
            throw new RuntimeException("Directory /opt/tutorial/data is not available");
        }
        _dataDir = f;
    }

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
    public final void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException,IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // This part we must synchronize to keep language settings for whole process of the X-definition.
        synchronized(Report.class) {
            Report.setLanguage("eng");
            String contentType = (contentType = req.getContentType()) == null ? "" : contentType;
            if (null != req.getParameter("fileDownLoad")) {
                String s = ServletUtil.getParam(req, "fileDownLoad");
                File f = new File(new File(_baseDir, "data"), s);
                if (!f.exists() || !f.isFile()) {
                    resp.getWriter().println("File " + s + " not exists");
                } else if (f.length() > 100000000) { // 100MB max
                    resp.getWriter().println("File "+s+" is longer then 100MB");
                } else {
                    byte[] bytes;
                    try {
                        bytes = FUtils.readBytes(f);
                    } catch (SException ex) {
                        resp.getWriter().println("Error:  " + ex);
                        return;
                    }
                    String chs = ServletUtil.getParam(req, "chset");
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
                return;
            }
            PrintWriter out = resp.getWriter();
            if (null != req.getParameter("fileDelete")) {
                String s = ServletUtil.getParam(req, "fileDelete");
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
                            ServletUtil.deleteFiles(ff);
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
"    <form style='background: #EAFFFD' method=\"post\"\n" +
"          action='Service' enctype='multipart/form-data'>");
                if (!ServletUtil.getParam(req, "upload").isEmpty()) {
                    out.println(
"      <b>Choose a file to upload:</b>\n" +
"      <input name='uploadfile' type='file' />\n" +
"      <br/>\n" +
"      <input type=\"submit\" value=\"Upload\" />");
                } else if (!ServletUtil.getParam(req, "download").isEmpty()) {
                    out.println(
"      <b>File name:</b>\n" +
"      <input type=\"text\" name=\"fileDownLoad\" value=\"\"/>\n" +
"      <input type=\"submit\" value=\"download\" />");
                } else if (!ServletUtil.getParam(req, "upload").isEmpty()) {
                    out.println(
"      <span style='font-family:\"Sylfaen\",\"serif\"'>\n" +
"        <b>Directory/filename:</b>\n" +
"        <input type=\"text\" name=\"fileUpload\" value=\"\"/>\n" +
"      </span>\n"+
"      <input type=\"submit\" value=\"upload\" />");
                } else if (!ServletUtil.getParam(req, "delete").isEmpty()) {
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