package org.xdef.util.conv.utils.xd.doc;

/** Enumeration of X-definition versions. */
public class XdVersion {
	/** Version name space URI. */
	private final String _nsURI;
	private final int _id;

	/** Creates X-definition version enumeration with given name space URI.
	 * @param id id.
	 * @param nsURI version name space URI.
	 * @throws NullPointerException if given version names pace URI is null.
	 * @throws IllegalArgumentException if given version name space is empty.
	 */
	private XdVersion(int id, String nsURI) {
		_id = id;
		_nsURI = nsURI;
	}

	/** X-definition version name space URI getter.
	 * @return version name space URI.
	 */
	public String getNSURI() {return _nsURI;}

	/** X-definition version id getter.
	 * @return X-definition version id.
	 */
	public int getId() {return _id;}

	@Override
	public String toString() {return "XdVersion['" + _nsURI + "']";}
}