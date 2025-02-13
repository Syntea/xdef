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

/** Represents any Xdefinition document.
 * @author Ilia Alexandrov
 */
public abstract class XdDoc {

	/** Returns proper implementation of Xdefinition document representation
	 * according to given document.
	 * @param xdef Xdefinition document.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of Xdefinition representation.
	 * @throws NullPointerException if given reporter is null.
	 * @throws IllegalArgumentException if given document is not a valid
	 * Xdefinition document.
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
			//Not valid Xdefinition document
			throw new SRuntimeException(XDCONV.XDCONV104);
		}
	}

	/** Returns proper implementation of Xdefinition document.
	 * @param xdef Xdefinition as string or path to Xdefinition file as string.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of Xdefinition document.
	 * @throws NullPointerException if given Xdefinition as string
	 * is null.
	 * @throws IllegalArgumentException if given Xdefinition as string is empty.
	 * @throws RuntimeException if error occurs during creating
	 * collection element.
	 */
	public static XdDoc getXdDoc(String xdef, final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//Xdefinition is null
		}
		if (xdef.length() == 0) {
			//Xdefinition is empty string
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

	/** Returns proper implementation of Xdefinition document.
	 * @param xdef Xdefinition URL.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of Xdefinition document.
	 * @throws NullPointerException if given Xdefinition URL is null.
	 * @throws RuntimeException if error occurs during creating collection
	 * element.
	 */
	public static XdDoc getXdDoc(URL xdef,
		final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//Xdefinition is null
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

	/** Returns proper implementation of Xdefinition document.
	 * @param xdef Xdefinition file.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of Xdefinition document.
	 * @throws NullPointerException if given Xdefinition file is null.
	 * @throws RuntimeException if error occurs during creating
	 * collection element.
	 */
	public static XdDoc getXdDoc(File xdef,
		final SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//Xdefinition is null
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

	/** Returns proper implementation of Xdefinition document.
	 * @param xdef Xdefinition input stream.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param debugMode debug mode switch.
	 * @return proper implementation of Xdefinition document.
	 * @throws NullPointerException if given Xdefinition input stream
	 * is null.
	 * @throws RuntimeException if error occurs during parsing Xdefinition from
	 * input stream.
	 */
	public static XdDoc getXdDoc(InputStream xdef,
		SReporter reporter,
		boolean debugMode) {
		if (xdef == null) {
			throw new SRuntimeException(XDCONV.XDCONV105);//Xdefinition is null
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