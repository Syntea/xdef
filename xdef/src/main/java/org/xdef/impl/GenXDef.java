package org.xdef.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xdef.msg.XDEF;
import org.xdef.sys.SRuntimeException;
import org.xdef.xml.KXmlUtils;
import org.xdef.xon.XonUtils;

/** Generate X-definition from XML or JSON/XON data.
 * @author Vaclav Trojan
 */
public final class GenXDef {

	/** Get object from data.
	 * @param o input date
	 * @return XML Element or XON object.
	 * @throws RuntimeException if input chan't be read.
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

	private static Object objectFromString(final String s) {
		try {
			return KXmlUtils.parseXml(s.trim()).getDocumentElement();
		} catch (Exception ex) {}
		try {
			return XonUtils.parseXON(s);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Incorrect data form: neither XML nor XON");
		}
	}

	private static Object objectFromURL(URL u) {
		InputStream is;
		try {
			is = u.openStream();
		} catch (IOException ex) {
			throw new RuntimeException("Can't read data");
		}
		return objectFromStream(is);
	}

	private static Object objectFromFile(final File f) {
		try {
			return KXmlUtils.parseXml(f).getDocumentElement();
		} catch (Exception ex) {}
		try {
			return XonUtils.parseXON(f);
		} catch (Exception ex) {}
		throw new RuntimeException("Unexpected data form: neither XML nor XON");
	}

	private static Object objectFromStream(final InputStream in) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			int i;
			while((i=in.read()) != -1) {
				os.write(i);
			}
			os.close();
		} catch (Exception ex) {
			throw new RuntimeException("Can't read data", ex);
		}
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		try {
			return KXmlUtils.parseXml(is).getDocumentElement();
		} catch (Exception ex) {}
		is.reset();
		try {
			return XonUtils.parseXON(is);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Unexpected data form (neither XML nor XON)", ex);
		}
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj input data.
	 * @return Element with created XDefinition.
	 */
	public static final Element genXdef(final Object obj) {
		return genXdef(obj, null);
	}

	/** Generate X-definition from a document to given output stream writer.
	 * @param obj input data.
	 * @param xdName name XDefinition or null.
	 * @return Element with created XDefinition.
	 */
	public static final Element genXdef(final Object obj, final String xdName) {
		try {
			Object o = readData(obj);
			if (o != null && o instanceof Element) {
				return GenXDefXML.genXdef((Element) o, xdName);
			}
			return GenXDefXON.genXdef(o, xdName);
		} catch (Exception ex) {
			if (ex instanceof RuntimeException) {
				throw ((RuntimeException) ex);
			}
			//XDEF883=Incorrect type of input data
			throw new SRuntimeException(XDEF.XDEF883, ex);
		}
	}
}
