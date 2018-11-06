package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents XML Schema container.
 * @author Ilia Alexandrov
 */
public interface XsdSchemaContainer {

	/**
	 * Schema container type enumeration.
	 */
	public static interface Type {

		/**
		 * Single schema type.
		 */
		public static final int SINGLE_SCHEMA = 1;
		/**
		 * Schema set type.
		 */
		public static final int SCHEMA_SET = SINGLE_SCHEMA + 1;
	}

	/** Returns type constant of current schema container implementation.
	 *
	 * @return type constant of current schema container implementation.
	 */
	public int getType();
}