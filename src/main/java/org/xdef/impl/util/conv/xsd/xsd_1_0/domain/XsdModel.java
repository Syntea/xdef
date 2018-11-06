package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

/** Represents any XML Schema model.
 * @author Ilia Alexandrov
 */
public abstract class XsdModel {

	/**
	 * Schema model type enumeration.
	 */
	public static interface Type {

		/**
		 * Schema <tt>complexType</tt> model type constant.
		 */
		public static final int COMPLEX_TYPE = 1;
		/**
		 * Schema <tt>group</tt> model type constant.
		 */
		public static final int GROUP = COMPLEX_TYPE + 1;
		/**
		 * Schema <tt>simpleType</tt> model type constant.
		 */
		public static final int SIMPLE_TYPE = GROUP + 1;
	}
	/**
	 * Model schema.
	 */
	protected final XsdSchema _schema;
	/**
	 * Model name.
	 */
	protected final String _name;

	/** Creates instance of schema model.
	 *
	 * @param schema model schema.
	 * @param name schema model name.
	 * @throws NullPointerException if given schema object or model name is
	 * <code>null</code>.
	 * @throws IllegalArgumentException if given model name is empty.
	 */
	public XsdModel(XsdSchema schema, String name) {
		if (schema == null) {
			throw new NullPointerException("Given model schema is null!");
		}
		if (name == null) {
			throw new NullPointerException("Given model name is null!");
		}
		if (name.length() == 0) {
			throw new NullPointerException("Given model name is emptyu!");
		}
		_schema = schema;
		_name = name;
	}

	/** Model schema getter.
	 *
	 * @return model schema.
	 */
	public final XsdSchema getSchema() {
		return _schema;
	}

	/** Model name getter.
	 *
	 * @return model name.
	 */
	public final String getName() {
		return _name;
	}

	/** Returns type constant of current Schema model.
	 *
	 * @return model type constant.
	 */
	public abstract int getType();
}