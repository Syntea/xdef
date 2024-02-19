package org.xdef.util.xsd2xd.xd;

import org.xdef.sys.SReporter;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KDOMBuilder;
import org.xdef.XDConstants;
import org.xdef.util.GenCollection;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.msg.XDCONV;

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
	 * @throws NullPointerException if given reporter is null.
	 * @throws IllegalArgumentException if given document is not a valid
	 * X-definition document.
	 */
	public static XdDoc getXdDoc(Document xdef,
		final SReporter reporter,
		boolean debugMode) {
		if (reporter == null) {
			throw new SRuntimeException(XDCONV.XDCONV107); //Reporter is null
		}
		String namespace = xdef.getDocumentElement().getNamespaceURI();
		if (XDConstants.XDEF31_NS_URI.equals(namespace)
			|| XDConstants.XDEF32_NS_URI.equals(namespace)
			|| XDConstants.XDEF40_NS_URI.equals(namespace)
			|| XDConstants.XDEF41_NS_URI.equals(namespace)
			|| XDConstants.XDEF42_NS_URI.equals(namespace)) {
			return new XdDoc_2_0(xdef, namespace);
		} else {
			//Not valid X-definition document
			throw new SRuntimeException(XDCONV.XDCONV104);
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
	public static XdDoc getXdDoc(String xdef, final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//X-definition is null
		}
		if (xdef.length() == 0) {
			//X-definition is empty string
			throw new SRuntimeException(XDCONV.XDCONV106);
		}
		try {
			Element collection = GenCollection.genCollection(new String[]{xdef},
				true, true, false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDCONV.XDCONV108, ex);
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
	public static XdDoc getXdDoc(URL xdef,
		final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//X-definition is null
		}
		try {
			Element collection =
				GenCollection.genCollection(new URL[]{xdef}, true, true, false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDCONV.XDCONV108, ex);
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
	public static XdDoc getXdDoc(File xdef,
		final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//X-definition is null
		}
		try {
			Element collection =
				GenCollection.genCollection(new File[]{xdef}, true, true,false);
			Document xdefDocument = collection.getOwnerDocument();
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDCONV.XDCONV108, ex);
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
	public static XdDoc getXdDoc(InputStream xdef,
		SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//X-definition is null
		}
		KDOMBuilder builder = new KDOMBuilder();
		try {
			Document xdefDocument = builder.parse(xdef);
			return getXdDoc(xdefDocument, reporter, debugMode);
		} catch (Exception ex) {
			//Error occurred when creating collection element: &{0}
			throw new SRuntimeException(XDCONV.XDCONV108, ex);
		}
	}
}
