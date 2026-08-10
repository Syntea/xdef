package org.xdef.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.xdef.web.util.ServletUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Redirects the default "/" landing page to a localized "lang/" landing page, based on the
 * "lang" cookie or else the browser's {@code Accept-Language} header (see {@link ServletUtil#detectLanguage}).
 * Lets the request through unchanged if the resolved language is English, unset, or not one of the localized subsites.
 * The url's query-string is carried over to the localized page.
 * <p>
 * Complements {@code redirectToLangIndex()} called from index.html (see common.js): when this filter runs
 * (deployed behind a servlet container), it redirects before any content is sent at all, so that function
 * never even executes; on plain static hosting (no filter) it remains the only mechanism.
 *
 * @author V.Sisma (+chatbot)
 */
public final class LangRedirectFilter implements Filter {
    /** localized subsites this redirect targets */
    private static final List<String> supported = Arrays.asList("cs", "es", "eo");

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest  req  = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            String              lang = ServletUtil.detectLanguage(req);

            if (lang != null && supported.contains(lang)) {
                //the query-string is carried, the url-fragment needs no handling due to browser never sends it
                String query = req.getQueryString();
                resp.sendRedirect(
                    req.getContextPath() + "/" + lang + "/" +
                    (query == null || query.isEmpty() ? "" : "?" + query)
                );
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
