package org.xdef.impl.util.conv.type.domain;

import org.xdef.impl.util.conv.xd.xd_2_0.XdNames;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdNames;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Represents schema type facet.
 * @author Ilia Alexandrov
 */
public class XsdFacet {

	/** Schema <code>enumeration</code> facet. */
	public static final XsdFacet ENUMERATION = new XsdFacet(Id.ENUMERATION,
		XsdNames.ENUMERATION, XdNames.ENUMERATION);
	/** Schema <code>fractionDigits</code> facet. */
	public static final XsdFacet FRACTION_DIGITS = new XsdFacet(
		Id.FRACTION_DIGITS, XsdNames.FRACTION_DIGITS, XdNames.FRACTION_DIGITS);
	/** Schema <code>length</code> facet. */
	public static final XsdFacet LENGTH =
		new XsdFacet(Id.LENGTH, XsdNames.LENGTH, XdNames.LENGTH);
	/** Schema <code>maxExclusive</code> facet. */
	public static final XsdFacet MAX_EXCLUSIVE = new XsdFacet(Id.MAX_EXCLUSIVE,
		XsdNames.MAX_EXCLUSIVE, XdNames.MAX_EXCLUSIVE);
	/** Schema <code>maxInclusive</code> facet. */
	public static final XsdFacet MAX_INCLUSIVE = new XsdFacet(Id.MAX_INCLUSIVE,
		XsdNames.MAX_INCLUSIVE, XdNames.MAX_INCLUSIVE);
	/** Schema <code>maxLength</code> facet. */
	public static final XsdFacet MAX_LENGTH = new XsdFacet(Id.MAX_LENGTH,
		XsdNames.MAX_LENGTH, XdNames.MAX_LENGTH);
	/** Schema <code>minExclusive</code> facet. */
	public static final XsdFacet MIN_EXCLUSIVE = new XsdFacet(Id.MIN_EXCLUSIVE,
		XsdNames.MIN_EXCLUSIVE, XdNames.MIN_EXCLUSIVE);
	/** Schema <code>minInclusive</code> facet. */
	public static final XsdFacet MIN_INCLUSIVE = new XsdFacet(Id.MIN_INCLUSIVE,
		XsdNames.MIN_INCLUSIVE, XdNames.MIN_INCLUSIVE);
	/** Schema <code>minLength</code> facet. */
	public static final XsdFacet MIN_LENGTH = new XsdFacet(Id.MIN_LENGTH,
		XsdNames.MIN_LENGTH, XdNames.MIN_LENGTH);
	/** Schema <code>pattern</code> facet. */
	public static final XsdFacet PATTERN = new XsdFacet(Id.PATTERN,
		XsdNames.PATTERN, XdNames.PATTERN);
	/** Schema <code>totalDigits</code> facet. */
	public static final XsdFacet TOTAL_DIGITS = new XsdFacet(Id.TOTAL_DIGITS,
		XsdNames.TOTAL_DIGITS, XdNames.TOTAL_DIGITS);
	/** Schema <code>whiteSpace</code> facet. */
	public static final XsdFacet WHITE_SPACE = new XsdFacet(Id.WHITE_SPACE,
		XsdNames.WHITE_SPACE, XdNames.WHITE_SPACE);

	/** Returns <code>true</code> if given facet is multiple value facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is multiple value facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isMultipleValue(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		switch (f.getId()) {
			case Id.ENUMERATION:
			case Id.PATTERN:
				return true;
		}
		return false;
	}

	/** Returns <code>true</code> if given facet is value restriction facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is value restriction facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isValueRestricted(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		switch (f.getId()) {
			case Id.MAX_EXCLUSIVE:
			case Id.MAX_INCLUSIVE:
			case Id.MIN_EXCLUSIVE:
			case Id.MIN_INCLUSIVE:
				return true;
		}
		return false;
	}

	/** Returns <code>true</code> if given facet is single numeric value
	 * restriction facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is single numeric value
	 * restriction facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isSingleNumericRestriction(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		return isDigitCountRestriction(f) || isLengthRestriction(f);
	}

	/** Returns <code>true</code> if given facet is single string value
	 * restriction facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is single string value
	 * restriction facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isSingleStringRestriction(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		switch (f.getId()) {
			case Id.MAX_EXCLUSIVE:
			case Id.MAX_INCLUSIVE:
			case Id.MIN_EXCLUSIVE:
			case Id.MIN_INCLUSIVE:
			case Id.WHITE_SPACE:
				return true;
		}
		return false;
	}

	/** Returns <code>true</code> if given facet is digit count restriction
	 * facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is digit count restriction
	 * facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isDigitCountRestriction(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		switch (f.getId()) {
			case Id.FRACTION_DIGITS:
			case Id.TOTAL_DIGITS:
				return true;
		}
		return false;
	}

	/** Returns <code>true</code> if given facet is length restriction facet.
	 * @param f facet to test.
	 * @return <code>true</code> if given facet is length restriction facet.
	 * @throws NullPointerException if given facet is <code>null</code>.
	 */
	public static boolean isLengthRestriction(XsdFacet f) {
		if (f == null) {
			throw new NullPointerException("Given facet is null!");
		}
		switch (f.getId()) {
			case Id.LENGTH:
			case Id.MAX_LENGTH:
			case Id.MIN_LENGTH:
				return true;
		}
		return false;
	}

	/** Gets instance of facet with given schema name.
	 * @param name schema facet name.
	 * @return facet instance or <code>null</code>.
	 */
	public static XsdFacet getByXsdName(String name) {
		return XS_NAMETOFACET.get(name);
	}

	/** Gets instance of facet with given XDefinition name.
	 * @param name XDefinition facet name.
	 * @return facet instance or <code>null</code>.
	 */
	public static XsdFacet getByXdefName(String name) {
		return XS_NAMETOFACET.get(name);
	}

	/** Facet set. */
	private static final Set<XsdFacet> FACETSET = new HashSet<XsdFacet>();

	/** Facet id to instance map. */
	private static final Map<Integer, XsdFacet> IDTOFACET =
		new HashMap<Integer, XsdFacet>();

	/** Facet schema name to instance map. */
	private static final Map<String, XsdFacet> XS_NAMETOFACET =
		new HashMap<String, XsdFacet>();
	/** Facet XDefinition name to instance map. */
	private static final Map<String, XsdFacet> XD_NAMETOFACET =
		new HashMap<String, XsdFacet>();

	static {
		FACETSET.add(ENUMERATION);
		FACETSET.add(FRACTION_DIGITS);
		FACETSET.add(LENGTH);
		FACETSET.add(MAX_EXCLUSIVE);
		FACETSET.add(MAX_INCLUSIVE);
		FACETSET.add(MAX_LENGTH);
		FACETSET.add(MIN_EXCLUSIVE);
		FACETSET.add(MIN_INCLUSIVE);
		FACETSET.add(MIN_LENGTH);
		FACETSET.add(PATTERN);
		FACETSET.add(TOTAL_DIGITS);
		FACETSET.add(WHITE_SPACE);
		Iterator<XsdFacet> it = FACETSET.iterator();
		while (it.hasNext()) {
			XsdFacet f = it.next();
			Integer id = f.getId();
			String xsdName = f.getXsdName();
			String xdefName = f.getXdefName();
			if (IDTOFACET.containsKey(id)) {
				throw new IllegalArgumentException(
					"Id to facet map already contains entry with given id!");
			}
			if (XS_NAMETOFACET.containsKey(xsdName)) {
				throw new IllegalArgumentException(	"Schema name to facet map"
					+ " already contains entry with given name!");
			}
			if (XD_NAMETOFACET.containsKey(xdefName)) {
				throw new IllegalArgumentException("XDefinition name to facet"
					+ " map already contains entry with given name!");
			}
			IDTOFACET.put(id, f);
			XS_NAMETOFACET.put(xsdName, f);
			XD_NAMETOFACET.put(xdefName, f);
		}
	}
	/** Facet id. */
	private final int _id;
	/** Facet schema name. */
	private final String _xsdName;
	/** Facet XDefinition name. */
	private final String _xdefName;

	/** Creates instance of facet.
	 * @param id facet id.
	 * @param xsdName facet schema name.
	 * @param xdefName facet XDefinition name.
	 */
	private XsdFacet(int id, String xsdName, String xdefName) {
		_id = id;
		_xsdName = xsdName;
		_xdefName = xdefName;
	}

	/** Gets facet id.
	 * @return facet id.
	 */
	public int getId() {return _id;}

	/** Gets facet schema name.
	 * @return facet schema name.
	 */
	public String getXsdName() {return _xsdName;}

	/** Gets facet XDefinition name.
	 * @return facet XDefinition name.
	 */
	public String getXdefName() {return _xdefName;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XsdFacet)) {
			return false;
		}
		return _id == ((XsdFacet) obj)._id;
	}

	@Override
	public int hashCode() {return 3*_id;}

	@Override
	public String toString() {return "XsdFacet['" + _xsdName + "']";}

	/** Contains numeric identifiers of facets. */
	public static interface Id {
		/** Schema <code>enumeration</code> facet id. */
		public static final int ENUMERATION = 1;
		/** Schema <code>fractionDigits</code> facet id. */
		public static final int FRACTION_DIGITS = ENUMERATION + 1;
		/** Schema <code>length</code> facet id. */
		public static final int LENGTH = FRACTION_DIGITS + 1;
		/** Schema <code>maxExclusive</code> facet id. */
		public static final int MAX_EXCLUSIVE = LENGTH + 1;
		/** Schema <code>maxInclusive</code> facet id. */
		public static final int MAX_INCLUSIVE = MAX_EXCLUSIVE + 1;
		/** Schema <code>maxLength</code> facet id. */
		public static final int MAX_LENGTH = MAX_INCLUSIVE + 1;
		/** Schema <code>minExclusive</code> facet id. */
		public static final int MIN_EXCLUSIVE = MAX_LENGTH + 1;
		/** Schema <code>minInclusive</code> facet id. */
		public static final int MIN_INCLUSIVE = MIN_EXCLUSIVE + 1;
		/** Schema <code>minLength</code> facet id. */
		public static final int MIN_LENGTH = MIN_INCLUSIVE + 1;
		/** Schema <code>pattern</code> facet id. */
		public static final int PATTERN = MIN_LENGTH + 1;
		/** Schema <code>totalDigits</code> facet id. */
		public static final int TOTAL_DIGITS = PATTERN + 1;
		/** Schema <code>whiteSpace</code> facet id. */
		public static final int WHITE_SPACE = TOTAL_DIGITS + 1;
	}
}