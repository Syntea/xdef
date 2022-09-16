package org.xdef.impl.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.xdef.impl.xml.XAbstractInputStream.bytesToString;
import static org.xdef.impl.xml.XAbstractInputStream.detectBOM;
import static org.xdef.impl.xml.XAbstractInputStream.nextChar;

/** Input stream used in SAX parser.
 * @author Vaclav Trojan
 */
public class XInputStream extends XAbstractInputStream {
	private final String _encoding;
	private final String _version;
	private final boolean _standalone;

	public XInputStream(final InputStream in, final String encoding)
		throws IOException {
		super(in, encoding);
		_encoding = encoding == null ? "UTF-8" : encoding;
		_standalone = false;
		_version = null;
	}

	public XInputStream(final InputStream in) throws IOException {
		super(in);
		byte[] buf = new byte[4];
		String encoding = detectBOM(in, buf);
		int len = encoding.charAt(0) - '0'; // number of bytes read
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (len > 0) {
			baos.write(buf, 0, len);
		}
		int count = encoding.charAt(1) - '0'; // bytes nead to read next
		encoding = encoding.substring(2);
		String s = "";
		if (count > 0 && !"X-ISO-10646-UCS-4-2143".equals(encoding)
			&& !"X-ISO-10646-UCS-4-3412".equals(encoding)) {
			s = bytesToString(buf, 0, len, encoding);
			int i;
			while (s.length() < 2
				&& (i = nextChar(in, encoding, buf, count, baos)) != -1) {
				s += (char) i;
			}
			if (s.startsWith("<?")) {
				while (s.length() < 5
					&& (i=nextChar(in, encoding,buf,count,baos)) != -1) {
					if (i == -1) {
						break;
					}
					s += (char) i;
					if (i == '>') {
						break;
					}
				}
			}
			if ("<?xml".equals(s)) {
				while (s.indexOf("?", 5) == -1) { // find "?"
					i = nextChar(in, encoding,buf,count,baos);
					if (i == -1) {
						break;
					}
					s += (char) i;
					if(i == '?' || i == '>') {
						break;
					}
				}
			}
		}
		String val = getXMLDeclParam("encoding", s);
		_encoding = val != null ? val : encoding;
		val = getXMLDeclParam("version", s);
		_version = val != null ? val : "1.0";
		_standalone = "yes".equals(getXMLDeclParam("standalone", s));
		setBuffer(baos.toByteArray());
	}

	public final String getXMLEncoding() {return _encoding;}
	public final String getXMLVersion() {return _version;}
	public final boolean getXMLStandalone() {return _standalone;}

	/** Get XMLDecl parameter.
	 * @param paramName
	 * @param source
	 * @return value of required parameter or null.
	 */
	private static String getXMLDeclParam(final String paramName,
		final String source) {
		int ndx = source.indexOf(paramName);
		if (ndx > 0) {
			int ndx1 = source.indexOf('=', ndx + paramName.length());
			if (ndx1 > 0) {
				int ndx2 = source.indexOf('"', ndx1 + 1);
				if (ndx2 == -1) {
					ndx2 = source.indexOf('\'', ndx1 + 1);
				}
				if (ndx2 >= 0) {
					char quote = source.charAt(ndx2++);
					int ndx3 = source.indexOf(quote, ndx2);
					if (ndx3 > 0) {
						return source.substring(ndx2, ndx3);
					}
				}
			}
		}
		return null;
	}
}