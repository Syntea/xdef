package org.xdef.web.filter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Range;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xdef.xml.KXmlUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Replaces the version-placeholder {@value #versionPlaceholder} in html-pages by the latest released
 * X-definition version, before the page leaves the server. Only the html-elements marked with the
 * css-class {@value #latestVerCssClass} are affected - their attributes (e.g. the href of a link) and the
 * text-content of a span (see e.g. other/download.html); elsewhere the placeholder is left untouched.
 * <p>
 * The version is determined from the "Maven Central Repository" and is never fetched while a request is
 * being processed: it is kept in {@link #latestVersionCached} by the background timer "version-refresh"
 * {@link #refreshTimer} with fixed rate {@link #refreshRate} and no initial delay, and a
 * request only reads whatever is cached at that moment. Until the very first fetch succeeds the
 * placeholder is left in the page unchanged; a later failed fetch keeps the last known-good version.
 *
 * @author V.Sisma (+chatbot)
 */
public final class LatestVersionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(LatestVersionFilter.class);

    /** placeholder of the latest released version, as written in the html-pages */
    private static final String             versionPlaceholder  = "--.--.--";
    /** css-class marking the html-elements the placeholder is replaced in */
    private static final String             latestVerCssClass   = "latestVersion";
    /** css-selector of the marked html-elements, see {@link #latestVerCssClass} */
    private static final String             latestVerElemSelect = "." + latestVerCssClass;
    /** how often the latest version is re-read from the "Maven Central Repository" in the background */
    private static final Duration           refreshRate         = Duration.ofMinutes(10);
    /** html-parser tracking source-positions configuration template only, parser get as {@link Parser#newInstance()} */
    private static final Parser             htmlParserCfgTempl  = Parser.htmlParser().setTrackPosition(true);
    private static final Duration           requestTimeout      = Duration.ofSeconds(10);
    private static final HttpClient         httpClient          = HttpClient.newBuilder()
        .connectTimeout(requestTimeout)
        .build()
    ;
    private static final String             xdefMvnUri          = "https://repo1.maven.org/maven2/org/xdef/xdef/maven-metadata.xml";
    private static final String             xdefMvnRelXPathS    = "/metadata/versioning/release";
    private static final XPathExpression    xdefMvnRelXPath     = compileReleaseXPath();

    /** last successfully fetched version, {@code null} until the first fetch succeeds,
     * with getter/setter synchronized by {@link #latestVersionCachedGuard} */
    private       String                    latestVersionCached      = null;
    private final Object                    latestVersionCachedGuard = new Object();

    /** single background timer refreshing {@link #latestVersionCached} */
    private final ScheduledExecutorService  refreshTimer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "latest-version-refresh");
        t.setDaemon(true);
        return t;
    });



    /** compile the XPath expression used to extract the release version from Maven metadata
     * @return compiled XPath expression selecting {@link #xdefMvnRelXPathS} */
    private static XPathExpression compileReleaseXPath() {
        try {
            return XPathFactory.newInstance().newXPath().compile(xdefMvnRelXPathS);
        } catch (XPathExpressionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }


    private String getLatestVersionCached() {
        synchronized (latestVersionCachedGuard) {
            return latestVersionCached;
        }
    }

    private void setLatestVersionCached(String versionCached) {
        synchronized (latestVersionCachedGuard) {
            this.latestVersionCached = versionCached;
        }
    }



    /** default constructor, calls super() only */
    public LatestVersionFilter() {
        super();
    }



    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        //start timer refresh
        refreshTimer.scheduleAtFixedRate(
            this::refreshLatestVersion,
            0, refreshRate.toMinutes(), TimeUnit.MINUTES
        );
        logger.info("init(): timer refresh started, rate " + refreshRate);
    }

    @Override
    public void destroy() {
      //stop timer version-refresh
        try {
            refreshTimer.shutdownNow();
            logger.info("destroy(): timer refresh stopped");
        } catch (RuntimeException ex) {
            logger.warn("destroy(): failed to shutdown timer refresh", ex);
        }
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse      resp    = (HttpServletResponse) response;
        BufferingResponseWrapper wrapper = new BufferingResponseWrapper(resp);

        chain.doFilter(request, wrapper);

        String latestVersion = getLatestVersionCached();
        byte[] captured      = wrapper.getCapturedBytes();
        String html          = new String(captured, StandardCharsets.UTF_8);

        String result = latestVersion == null || !html.contains(versionPlaceholder)
            ? html
            : replaceVersion(html, latestVersion)
        ;

        //if no change then result == html really
        byte[] output = result == html ? captured : result.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(output.length);
        resp.getOutputStream().write(output);
    }

    /**
     * Read the latest released version from the "Maven Central Repository" into {@link #latestVersionCached}.
     * On any failure the last known-good version is kept (and the placeholder stays in the pages as long
     * as there is none), and the error is never propagated, so that it cannot kill the refresh thread.
     */
    private void refreshLatestVersion() {
        final String mtd                  = "refreshVersion(): ";
        final String latesetVersionCached = getLatestVersionCached();

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(xdefMvnUri))
                .timeout(requestTimeout)
                .build()
            ;
            String xdefMvnStr     = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
            String xdefMvnRelease = (String) xdefMvnRelXPath.evaluate(KXmlUtils.parseXml(xdefMvnStr), XPathConstants.STRING);

            if (xdefMvnRelease.isEmpty()) {
                logger.warn(mtd + "no latest version found in " + xdefMvnUri + ", keeping \"" + latesetVersionCached + "\"");
                return;
            }

            setLatestVersionCached(xdefMvnRelease);
            logger.debug(mtd + "version refreshed to \"" + xdefMvnRelease + "\"");
        } catch (IOException | InterruptedException | RuntimeException | XPathExpressionException ex) {
            logger.warn(mtd + "failed to read the version, keeping \"" + latesetVersionCached + "\"", ex);
        }
    }

    /**
     * Replace {@link #versionPlaceholder} by version, but only inside the html-elements marked with the
     * css-class {@value #latestVerCssClass} - in their own attributes (e.g. the href of a link) and in their
     * whole text-content. Anywhere else in the page the placeholder is left untouched.
     * <p>
     * The page is parsed only to locate those places: the parsed document is never written back, the
     * replacement is done in the original page-text at the source-positions reported by the parser. So
     * everything the page contains but the replaced placeholders stays exactly as it was generated.
     *
     * @param html      the whole generated page
     * @param version   latest released version to be set
     * @return the page with the placeholder replaced; the very same instance if there was nothing to replace
     */
    private static String replaceVersion(final String html, final String version) {
        //parse html by parser got from config-template, see htmlParserCfgTempl
        Document doc = Jsoup.parse(html, "", htmlParserCfgTempl.newInstance());

        //source-ranges to replace in, as start-position -> end-position, sorted, deduplicated and not-nested (be-asure)
        NavigableMap<Integer, Integer> ranges = new TreeMap<>();

        for (Element elem : doc.select(latestVerElemSelect)) {
            //attributes of the marked element, e.g. <a class="latestVersion" href="...--.--.--...">
            for (Attribute attr : elem.attributes()) {
                addRange(ranges, html, elem.attributes().sourceRange(attr.getKey()).valueRange());
            }
            //text-content of the marked element, e.g. <span class="latestVersion">--.--.--</span>
            for (Element elemInner : elem.getAllElements()) {
                for (TextNode text : elemInner.textNodes()) {
                    addRange(ranges, html, text.sourceRange());
                }
            }
        }

        if (ranges.isEmpty()) {
            return html;
        }

        //replace from the end of the page, so that the not yet processed positions stay valid
        StringBuilder sb = new StringBuilder(html);
        for (Map.Entry<Integer, Integer> range : ranges.descendingMap().entrySet()) {
            int start = range.getKey();
            int end   = range.getValue();
            sb.replace(start, end, html.substring(start, end).replace(versionPlaceholder, version));
        }

        return sb.toString();
    }

    /** add the source-range of an attribute-value or of a text to ranges, if the placeholder occurs in it
     * @param ranges    collected source-ranges, as start-position -> end-position
     * @param html      the whole generated page
     * @param range     source-range to be added */
    private static void addRange(final Map<Integer, Integer> ranges, final String html, final Range range) {
        if (!range.isTracked()) {
            //without a source-position there is nothing to replace in
            return;
        }

        int start = range.start().pos();
        int end   = range.end().pos();
        if (start >= 0 && end <= html.length() && start <= end
            && html.substring(start, end).contains(versionPlaceholder)
        ) {
            ranges.put(start, end);
        }
    }

}
