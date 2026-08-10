package org.xdef.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Bakes the shared header/footer fragments directly into static ".html" pages before they leave the
 * server, so they arrive already complete and never show the "NOT LOADED" placeholder / pop in visibly.
 * <p>
 * Pages keep their placeholder divs and client-side {@code loadHeaderFooter()} (see common.js), so they
 * still work unchanged when served by any plain static webserver where this filter does not run.
 *
 * @author V.Sisma (+chatbot)
 */
public final class HeaderFooterFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(HeaderFooterFilter.class);

    /** subsite language folders (with trailing "/"); root/default(English) is "" */
    private static final List<String> languages = Arrays.asList("", "cs/", "es/", "eo/");

    /** language folder -&gt; ready-to-splice {@code <div id="header">...</div>} markup */
    private final Map<String, String> headerByLang = new LinkedHashMap<>();
    /** language folder -&gt; ready-to-splice {@code <div id="footer">...</div>} markup */
    private final Map<String, String> footerByLang = new LinkedHashMap<>();

    /** default constructor, calls super() only */
    public HeaderFooterFilter() {
        super();
    }

    /** pre-load and pre-resolve header/footer markup for every language, once */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        ServletContext ctx         = filterConfig.getServletContext();
        String         contextPath = ctx.getContextPath();

        for (String lang : languages) {
            String rootPath = contextPath + "/" + lang;
            headerByLang.put(lang, "<div id=\"header\">" + loadFragment(ctx, lang, "header.html", rootPath) + "</div>");
            footerByLang.put(lang, "<div id=\"footer\">" + loadFragment(ctx, lang, "footer.html", rootPath) + "</div>");
        }

        logger.info("init(): pre-loaded header/footer for " + languages.size() + " languages " + languages);
    }

    /** read a "&lt;lang&gt;style/&lt;name&gt;" fragment and resolve its "${rootPath}" placeholders */
    private static String loadFragment(
        final ServletContext ctx, final String lang, final String name, final String rootPath
    ) throws ServletException {
        String path = "/" + lang + "style/" + name;
        try (InputStream is = ctx.getResourceAsStream(path)) {
            if (is == null) {
                throw new ServletException("HeaderFooterFilter: resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).replace("${rootPath}", rootPath);
        } catch (IOException ex) {
            throw new ServletException("HeaderFooterFilter: failed to read " + path, ex);
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

        HttpServletRequest       req     = (HttpServletRequest)  request;
        HttpServletResponse      resp    = (HttpServletResponse) response;
        BufferingResponseWrapper wrapper = new BufferingResponseWrapper(resp);

        chain.doFilter(req, wrapper);

        String lang     = detectLanguage(req);
        byte[] captured = wrapper.getCapturedBytes();
        String html     = new String(captured, StandardCharsets.UTF_8);

        String result = html;
        result = replaceDiv(result, "header", headerByLang.get(lang));
        result = replaceDiv(result, "footer", footerByLang.get(lang));

        //if no change then result == html really
        byte[] output = result == html ? captured : result.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(output.length);
        resp.getOutputStream().write(output);
    }

    /** @return the request's language-subsite folder (with trailing "/"), or "" for the root/English site */
    private static String detectLanguage(final HttpServletRequest req) {
        String path = req.getServletPath();

        return languages.stream()
            .filter(lang -> !lang.isEmpty() && path.startsWith("/" + lang))
            .findAny()
            .orElse("")
        ;
    }

    /** replace the content of the first {@code <div id="id">...</div>} with replacement; no-op if not found */
    private static String replaceDiv(final String html, final String id, final String replacement) {
        String tagOpen  = "<div id=\"" + id + "\">";
        String tagClose = "</div>";

        int start = html.indexOf(tagOpen);
        if (start < 0 || replacement == null) {
            return html;
        }

        int end = html.indexOf(tagClose, start);
        if (end < 0) {
            return html;
        }

        return html.substring(0, start) + replacement + html.substring(end + tagClose.length());
    }

    @Override
    public void destroy() {}

}
