package org.xdef.impl.util.conv.xsd.doc;

/** Enumeration of XML Schema versions.
 * @author Ilia Alexandrov
 */
public class XsdVersion {

	/** XML Schema <code>1.0</code> version. */
	public static final XsdVersion SCHEMA_1_0 =
		new XsdVersion(Id.SCHEMA_1_0, "http://www.w3.org/2001/XMLSchema");
	/** Version namespace URI. */
	private final String _nsURI;
	/** Version id. */
	private final int _id;

	/** Creates enumeration of XML Schema version with given namespace URI.
	 * @param id version id.
	 * @param nsURI version namespace URI.
	 * @throws NullPointerException if given version namespace URI is <tt>null</tt>.
	 * @throws IllegalArgumentException if given version namespace URI is empty.
	 */
	private XsdVersion(int id, String nsURI) {
		if (nsURI == null) {
			throw new NullPointerException(
				"Given XML Schema version namespace URI is null!");
		}
		if (nsURI.length() == 0) {
			throw new IllegalArgumentException(
				"Given XML Schema version namespace URI is empty!");
		}
		_id = id;
		_nsURI = nsURI;
	}

	/** XML Schema version namespace URI getter.
	 * @return version namespace URI.
	 */
	public String getNSURI() {return _nsURI;}

	/** XML Schema version id getter.
	 * @return version id.
	 */
	public int getId() {return _id;}

	/** XML Schema version id. */
	public static interface Id {
		/** XML Schema version <code>1.0</code> id. */
		public static final int SCHEMA_1_0 = 1;
	}
}