package org.xdef.impl.util.conv.xd.doc;

import org.xdef.XDConstants;
import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KDOMBuilder;
import org.xdef.msg.XDEF;
import org.xdef.impl.util.gencollection.XDGenCollection;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Represents any X-definition document.
 * @author Ilia Alexandrov
 */
public abstract class XdDoc {

	/** Returns proper implementation of X-definition document representation
	 * according to given document.
	 * @param xdef X-definition document.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of X-definition representation.
	 * @throws NullPointerException if given reporter is <tt>null</tt>.
	 * @throws IllegalArgumentException if given document is not a valid
	 * X-definition document.
	 */
	public final static XdDoc getXdDoc(final Document xdef,
		final SReporter reporter,
		final boolean debugMode) {
		if (reporter == null) {
			throw new SRuntimeException(XDEF.XDEF707); //Reporter is null
		}
		String namespace = xdef.getDocumentElement().getNamespaceURI();
		if (XDConstants.XDEF20_NS_URI.equals(namespace)
			|| XDConstants.XDEF31_NS_URI.equals(namespace)
			|| XDConstants.XDEF32_NS_URI.equals(namespace)
			|| XDConstants.XDEF40_NS_URI.equals(namespace)) {
			return new XdDoc_2_0(xdef);
		} else {
			//Not valid X-definition document
			throw new SRuntimeException(XDEF.XDEF704);
		}
	}

	/** Returns proper implementation of X-definition document.
	 * @param xdef X-definition as string or path to X-definition file as string.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of X-definition document.
	 * @throws NullPointerException if given X-definition as string
	 * is <tt>null</tt>.
	 * @throws IllegalArgumentException if given X-definition as string is empty.
	 * @throws RuntimeException if error occurs during creating
	 * collection element.
	 */
	public final static XdDoc getXdDoc(final String xdef,
		final SReporter reporter,
		final boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDEF.XDEF705); //X-definition is null
		}
		if (xdef.length() == 0) {
			//X-definition is empty string
			throw new SRuntimeException(XDEF.XDEF706);
		}
		try {
			Element collection = XDGenCollection.genCollection(
				new String[]{xdef}, true, true, false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDEF.XDEF708, ex);
		}
	}

	/** Returns proper implementation of X-definition document.
	 * @param xdef X-definition URL.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of X-definition document.
	 * @throws NullPointerException if given X-definition URL is <tt>null</tt>.
	 * @throws RuntimeException if error occurs during creating collection
	 * element.
	 */
	public final static XdDoc getXdDoc(final URL xdef,
		final SReporter reporter,
		final boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDEF.XDEF705); //X-definition is null
		}
		try {
			Element collection = XDGenCollection.genCollection(
				new URL[]{xdef}, true, true, false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDEF.XDEF708, ex);
		}
	}

	/** Returns proper implementation of X-definition document.
	 * @param xdef X-definition file.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of X-definition document.
	 * @throws NullPointerException if given X-definition file is <tt>null</tt>.
	 * @throws RuntimeException if error occurs during creating
	 * collection element.
	 */
	public final static XdDoc getXdDoc(final File xdef,
		final SReporter reporter,
		final boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDEF.XDEF705); //X-definition is null
		}
		try {
			Element collection = XDGenCollection.genCollection(
				new File[]{xdef}, true, true, false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDEF.XDEF708, ex);
		}
	}

	/** Returns proper implementation of X-definition document.
	 * @param xdef X-definition input stream.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of X-definition document.
	 * @throws NullPointerException if given X-definition input stream
	 * is <tt>null</tt>.
	 * @throws RuntimeException if error occurs during parsing X-definition from
	 * input stream.
	 */
	public final static XdDoc getXdDoc(final InputStream xdef,
		final SReporter reporter,
		final boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDEF.XDEF705); //X-definition is null
		}
		KDOMBuilder builder = new KDOMBuilder();
		try {
			Document xdefDocument = builder.parse(xdef);
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDEF.XDEF708, ex);
		}
	}
}