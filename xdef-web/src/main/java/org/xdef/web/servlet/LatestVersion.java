package org.xdef.web.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xdef.xml.KXmlUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Determine latest version of X-definition from "Maven Central Repository" using cached request
 */
public final class LatestVersion extends HttpServlet {
    private static final long serialVersionUID = 2277695929503402350L;

    private static final String             xdefMvnUri      = "https://repo1.maven.org/maven2/org/xdef/xdef/maven-metadata.xml";
    private static final String             xdefMvnRelXPath = "/metadata/versioning/release";
    private static final XPathExpression    releaseXPath    = compileReleaseXPath();
    private static final Duration           requestTimeout  = Duration.ofSeconds(10);
    private static final long               cacheTTLms      = Duration.ofMinutes(10).toMillis();
    private static final HttpClient         httpClient      = HttpClient.newBuilder()
        .connectTimeout(requestTimeout)
        .build()
    ;

    /** last fetched result and when it was fetched; guarded by the class monitor, see {@link #getLatestVersion()} */
    private static String latestVersionCached   = null;
    private static long   latestVersionCachedAt = 0L;

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().print(getLatestVersion());
    }

    /**
     * Get the cached latest-version value, refreshing it from Maven Central if it is missing or stale.
     * Synchronized so that concurrent requests arriving while the cache is stale share a single outbound
     * HTTP call (and its timeout) instead of each blocking on their own request to Maven Central.
     *
     * @return latest released X-definition version, or "NOT FOUND" if it could not be determined.
     */
    private static synchronized String getLatestVersion() {
        long now = System.currentTimeMillis();
        if (latestVersionCached != null && now - latestVersionCachedAt < cacheTTLms) {
            return latestVersionCached;
        }

        String result;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(xdefMvnUri))
                .timeout(requestTimeout)
                .build()
            ;
            String   xdefMvnStr     = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            Document xdefMvnDoc     = KXmlUtils.parseXml(xdefMvnStr);
            String   xdefMvnVersion = (String)releaseXPath.evaluate(xdefMvnDoc, XPathConstants.STRING);

            result = xdefMvnVersion.isEmpty() ? "NOT FOUND" : xdefMvnVersion;
        } catch (IOException | InterruptedException | RuntimeException | XPathExpressionException e) {
            //keep serving the last known-good value on a transient failure, if there is one
            result = latestVersionCached != null ? latestVersionCached : "NOT FOUND";
        }

        latestVersionCached   = result;
        latestVersionCachedAt = now;

        return result;
    }

    /** Compile the XPath expression used to extract the release version from Maven metadata.
     * @return compiled XPath expression selecting "/metadata/versioning/release".
     */
    private static XPathExpression compileReleaseXPath() {
        try {
            return XPathFactory.newInstance().newXPath().compile(xdefMvnRelXPath);
        } catch (XPathExpressionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

}
