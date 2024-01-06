package org.xdef.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SThrowable;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Generate X-definition from XML or JSON/XON data.
 * @author Vaclav Trojan
 */
public final class GenXDef {

	/** Get object from data.
	 * @param o XML, JSON/XON, YAML input data (or path to source data)
	 * @return XML Element or XON object.
	 * @throws RuntimeException if input can't be read.
	 */
	public static final Object readData(final Object o) throws RuntimeException{
		Object x = o == null ? null
			: o instanceof Element ? o
			: o instanceof Document ? ((Document) o).getDocumentElement()
			: o instanceof String ? objectFromString((String) o)
			: o instanceof URL ? objectFromURL((URL) o)
			: o instanceof File ? objectFromFile((File) o)
			: o instanceof File ? objectFromStream((InputStream) o)
			: o;
		return x;
	}

	/** Get RuntimeException from throwable object.
	 * @param ex throwable object
	 * @return RuntimeException created from throwable object.
	 */
	private static RuntimeException getRuntimeExeception(final Throwable ex) {
		if (ex instanceof SThrowable &&
			"JSON101".equals(((SThrowable) ex).getMsgID())) {
			return new RuntimeException("Neither XML nor JSON");
		}
		throw new RuntimeException("Neither XML nor JSON or YAML", ex);
	}

	/** Parse string and return parsed object.
	 * @param s The string to be parsed.
	 * @return parsed object.
	 * @throws RuntimeException if an error occurs.
	 */
	private static Object objectFromString(final String s) {
		try {
			return KXmlUtils.parseXml(s.trim()).getDocumentElement();
		} catch (Exception ex) {}
		try {
			return XonUtils.parseXON(s);
		} catch (SRuntimeException ex) {}
		try {
			return XonUtils.parseYAML(s);
		} catch (SRuntimeException ex) {
			throw getRuntimeExeception(ex);
		}
	}

	/** Parse object from URL.
	 * @param u location with source data..
	 * @return parsed object.
	 */
	private static Object objectFromURL(URL u) {
		InputStream is;
		try {
			is = u.openStream();
		} catch (IOException ex) {
			throw new RuntimeException("Can't read data");
		}
		return objectFromStream(is);
	}

	/** Parse object from file.
	 * @param f file with source data..
	 * @return parsed object.
	 */
	private static Object objectFromFile(final File f) {
		try {
			return KXmlUtils.parseXml(f).getDocumentElement();
		} catch (Exception ex) {}
		try {
			return XonUtils.parseXON(f);
		} catch (SRuntimeException ex) {}
		try {
			return XonUtils.parseYAML(f);
		} catch (SRuntimeException ex) {
			throw getRuntimeExeception(ex);
		}
	}

	/** Parse object from input stream.
	 * @param f input stream with source data..
	 * @return parsed object.
	 */
	private static Object objectFromStream(final InputStream in) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			int i;
			while((i=in.read()) != -1) {
				os.write(i);
			}
			os.close();
		} catch (IOException ex) {
			throw new RuntimeException("Can't read data", ex);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		try {
			return KXmlUtils.parseXml(is).getDocumentElement();
		} catch (Exception ex) {}
		is.reset();
		try {
			return XonUtils.parseXON(is);
		} catch (SRuntimeException ex) {}
		is.reset();
		try {
			return XonUtils.parseYAML(is);
		} catch (SRuntimeException ex) {
			throw getRuntimeExeception(ex);
		}
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj XML, JSON/XON, YAML input data (or path to source data).
	 * @return Element with created XDefinition.
	 */
	public static final Element genXdef(final Object obj) {
		return genXdef(obj, null);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj XML, JSON/XON, YAML input data (or path to source data).
	 * @param xdName name XDefinition or null.
	 * @return Element with created XDefinition.
	 */
	public static final Element genXdef(final Object obj, final String xdName) {
		Object o = readData(obj);
		return o != null && o instanceof Element
			? GenXDefXML.genXdef((Element) o, xdName)
			: GenXDefXON.genXdef(o, xdName);
	}
}
