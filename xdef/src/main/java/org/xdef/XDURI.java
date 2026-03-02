package org.xdef;

/**
 * @author trojan
 */
public interface XDURI extends XDValue {
    /** Get URI value from this object.
     * @return the associated object, or return null.
     */
    java.net.URI getURI();

    /** Get the content of this URI as a US-ASCII string.
     * @return the content of this URI as a US-ASCII string.
     */
    public String toASCIIString();
}
