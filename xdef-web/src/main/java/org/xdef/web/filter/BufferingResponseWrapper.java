package org.xdef.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Buffers the wrapped response's body instead of sending it, so that a filter can post-process the whole
 * generated page before writing it out. Handles both response-output ways - {@link #getOutputStream()}
 * (used when serving static files) and {@link #getWriter()} (used by the servlets).
 * <p>
 * The filter is responsible for setting the real content-length and writing the buffered bytes; see
 * {@link HeaderFooterFilter} or {@link LatestVersionFilter}.
 *
 * @author V.Sisma (+chatbot)
 */
final class BufferingResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final ServletOutputStream   out    = new ServletOutputStream() {
        @Override public void write(final int b)                                  { buffer.write(b); }
        @Override public void write(final byte[] b, final int off, final int len) { buffer.write(b, off, len); }
        @Override public boolean isReady()                                        { return true; }
        @Override public void setWriteListener(final WriteListener listener)      {}
    };
    private final PrintWriter           writer =
        new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8), true)
    ;

    BufferingResponseWrapper(final HttpServletResponse response) {
        super(response);
    }

    @Override public ServletOutputStream getOutputStream()                    { return out; }
    @Override public PrintWriter         getWriter()                          { return writer; }
    /** the real content-length is set by the filter once buffering is done */
    @Override public void                setContentLength(final int len)      {}
    /** see {@link #setContentLength(int)} */
    @Override public void                setContentLengthLong(final long len) {}

    /** @return everything written to this response so far */
    byte[] getCapturedBytes() {
        writer.flush();
        return buffer.toByteArray();
    }
}
