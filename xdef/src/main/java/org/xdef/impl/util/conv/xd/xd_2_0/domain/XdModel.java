package org.xdef.impl.util.conv.xd.xd_2_0.domain;

/** Represents any X-definition model.
 * @author Ilia Alexandrov
 */
public abstract class XdModel {

	/** Model X-definition. */
	protected final XdDef _def;
	/** Model name. */
	protected final String _name;
	/** Processed model switch. */
	private boolean _processed = false;

	/** Creates instance of X-definition model.
	 * @param def model X-definition.
	 * @param name X-definition model name.
	 * @throws NullPointerException if given model X-definition or name is
	 * <tt>null</tt>.
	 * @throws IllegalArgumentException if given model name is empty.
	 */
	public XdModel(XdDef def, String name) {
		if (def == null) {
			throw new NullPointerException("Given model X-definition is null!");
		}
		if (name == null) {
			throw new NullPointerException("Given model name is null!");
		}
		if (name.length() == 0) {
			throw new IllegalArgumentException("Given model name is empty!");
		}
		_def = def;
		_name = name;
	}

	/** Model X-definition getter.
	 * @return model X-definition.
	 */
	public final XdDef getDef() {return _def;}

	/** Model name getter.
	 * @return model name.
	 */
	public final String getName() {return _name;}

	/** Returns <tt>true</tt> if given model is processed.
	 * @return <tt>true</tt> if given model is processed.
	 */
	public final boolean isProcessed() {return _processed;}

	/** Sets processed model switch to <tt>true</tt>. */
	public final void setProcessed() {_processed = true;}

	/** Returns type constant of current X-definition model implementation.
	 * @return X-definition model type constant.
	 */
	public abstract int getType();

	/** Model type enumeration type. */
	public static interface Type {
		/** X-definition <tt>declaration</tt> model type. */
		public static final int DECLARATION = 1;
		/** X-definition <tt>group</tt> model type. */
		public static final int GROUP = DECLARATION + 1;
		/** X-definition element model type. */
		public static final int ELEMENT = GROUP + 1;
	}
}