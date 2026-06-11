package org.xdef.web.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Determine latest version of X-definition z "Maven Central Repository"
 */
public final class LatestVersion extends AbstractMyServlet {
    private static final long serialVersionUID = 2277695929503402350L;

    private static final String  xdefMvnUri = "https://repo1.maven.org/maven2/org/xdef/xdef/maven-metadata.xml";
    private static final Pattern release    = Pattern.compile("<release>(.*?)</release>");

    /** default constructor, calls super() only */
    public LatestVersion() {
        super();
    }

    /**
     * Processes requests.
     *
     * @param req  servlet request object.
     * @param resp servlet response object.
     * @throws IOException if an error occurs.
     */
    @Override
    public final void processRequest(final HttpServletRequest req, final HttpServletResponse resp)
        throws ServletException, IOException
    {
        String result;

        try {
            String mvnMetadata = HttpClient.newHttpClient()
                .send(
                    HttpRequest.newBuilder(URI.create(xdefMvnUri)).build(),
                    HttpResponse.BodyHandlers.ofString()
                )
                .body()
            ;

            Matcher matcher = release.matcher(mvnMetadata);

            if (matcher.find()) {
                result = matcher.group(1);
            } else {
                result = "NOT FOUND";
            }

        } catch (IOException | InterruptedException e) {
            result = "NOT FOUND";
        }


        //return response
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print(result);
    }

    @Override
    public String getServletInfo() {
        return "Determine latest version of X-definition z \"Maven Central Repository\"";
    }

}
