package org.xdef.impl;

import org.xdef.model.XMVariable;
import org.xdef.model.XMVariableTable;
import java.io.IOException;

/** Contains table of variables.
 * deprecated - will be not public in future versions
 * @author Vaclav Trojan
 */
public class XVariableTable implements XMVariableTable {

	/** Increase step of size of the table of variables. */
	private final int STEP = 4;
	/** Table of variables. */
	private XVariable[] _variables;
	/** Number of items in the table of variables. */
	private int _size;
	/** Actual index to last declared variable in the block. */
	private int _lastOffset;
	/** Parent variable table. */
	private XVariableTable _parent;
	/** SqId of this object. */
	private final int _sqId;

	/** Create new instance of the variable table.
	 * @param sqId ID of this table.
	 */
	public XVariableTable(int sqId) {
		_variables = new XVariable[0];
		_lastOffset = -1;
		_sqId = sqId;
//		_size = 0; _parent = null; // java makes it
	}

	/** Create new instance of the variable table.
	 * @param parent parent variable table.
	 * @param sqId ID of this table.
	 */
	public XVariableTable(final XVariableTable parent, final int sqId) {
		_variables = new XVariable[0];
		_size = 0;
		_parent = parent;
		_lastOffset = -1;
		_sqId = sqId;
	}

	/** Add variable to this table.
	 * @param v variable to be added.
	 * @return true if variable was added.
	 */
	public final boolean addVariable(final XVariable v) {
		String name = v.getName();
		for (int i = 0; i < _size; i++) {
			if (name.equals(_variables[i].getName())) {
				return false; //variable already exists
			}
		}
		if (_size >= _variables.length) {
			XVariable[] x = new XVariable[_variables.length + STEP];
			System.arraycopy(_variables, 0, x, 0, _size);
			_variables = x;
		}
		_variables[_size++] = v;
		return true;  //variable was added to the table
	}

	/* Get declared variable.
	 * @param name The name of variable.
	 */
	public final XVariable getXVariable(final String name) {
		XVariable result = (XVariable) getVariable(name);
		if (result != null) {
			return result;
		}
		XVariableTable parent = getParent();
		while (parent != null) {
			result = (XVariable) parent.getVariable(name);
			if (result != null) {
				return result;
			}
			parent = parent.getParent();
		}
		return null;
	}

	/* Get declared variable with given offset.
	 * @param offset offset of declared variable.
	 */
	public final XVariable getXVariable(int offset) {
		for (int i = 0; i < _size; i++) {
			XVariable v = _variables[i];
			if (v != null && v.getOffset() == offset) {
				return v;
			}
		}
		return null;
	}

	/** Check if the name from argument represents a variable
	 * (search in the whole chain of variable tables).
	 * @param name the name of variable.
	 * @return <tt>true</tt> if the name from argument represents a variable.
	 */
	public final boolean isVariable(final String name) {
		if (getVariable(name) != null) {
			return true;
		}
		XVariableTable parent = getParent();
		return parent == null ? false : parent.isVariable(name);
	}

	/** Get SqId of this object.
	 * @return SqId of this object.
	 */
	public final int getSqId() {return _sqId;}

	@Override
	/** Get number of variables in the table.
	 * @return number of variables in the table.
	 */
	public final int size() {return _size;}

	/** Clear variable table. */
	public final void clear() {
		_variables = new XVariable[0];
		_size = 0;
	}

	/** Clone variable table.
	 * @return clone of this variable table.
	 */
	public final XVariableTable cloneTable() {
		XVariableTable result = new XVariableTable(_sqId);
		result._variables = new XVariable[_variables.length];
		System.arraycopy(_variables, 0, result._variables, 0,_variables.length);
		result._lastOffset = _lastOffset;
		result._size = _size;
		result._parent = _parent;
		return result;
	}

	/** Reset table to given size
	 * @param size the size to be reset.
	 * @param lastOffset last variable offset.
	 */
	public void resetTo(final int size, final int lastOffset) {
		if (size >= _size) {
			return; //do nothing
		}
		for (int i = size + 1; i < _variables.length; i++) {
			_variables[i] = null;
		}
		_size = size;
		_lastOffset = lastOffset;
	}

	/** Get parent table.
	 * @return parent table.
	 */
	public final XVariableTable getParent() {return _parent;}

	/** Get next variable offset.
	 * @return next variable offset.
	 */
	public final int getNextOffset() {return ++_lastOffset;}

	/** Get last variable offset.
	 * @return last variable offset.
	 */
	public final int getLastOffset() {return _lastOffset;}

	/** Set last variable offset.
	 * @param x last variable offset.
	 */
	public final void setLastOffset(final int x) {_lastOffset = x;}

	/** Set parent of the variable table. */
	final void setParent(final XVariableTable p) {_parent = p;}

	/** Write variable table to XDWriter.
	 * @param xw the XDWriter.
	 * @throws IOException if an error occurs.
	 */
	final void writeXD(final XDWriter xw) throws IOException {
		XVariable[] variables = (XVariable[]) toArray();
		xw.writeInt(_sqId);
		xw.writeLength(variables.length);
		for (int i = 0; i < variables.length; i++) {
			variables[i].writeXD(xw);
		}
	}

	/** Read variable table from XDReader.
	 * @param xr the XDReader.
	 * @return variable table.
	 * @throws IOException if an error occurs.
	 */
	final static XVariableTable readXD(final XDReader xr) throws IOException {
		XVariableTable result = new XVariableTable(xr.readInt());
		int len = xr.readLength();
		for (int i = 0; i < len; i++) {
			result.addVariable(XVariable.readXD(xr));
		}
		return result;
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XMVariableTable
////////////////////////////////////////////////////////////////////////////////
	@Override
	/** Get variable.
	 * @param name the name of variable.
	 * @return variable or null.
	 */
	public final XMVariable getVariable(final String name) {
		for (int i = 0; i < _size; i++) {
			if (name.equals(_variables[i].getName())) {
				return _variables[i];
			}
		}
		return null;
	}

	@Override
	/** Get names of variables.
	 * @return array of names of variables.
	 */
	public final String[] getVariableNames() {
		String[] result = new String[_size];
		for (int i = 0; i < _size; i++) {
			result[i] = _variables[i].getName();
		}
		return result;
	}

	@Override
	/** Get all variables in the table.
	 * @return array of variables in the table.
	 */
	public final XMVariable[] toArray() {
		XVariable[] x = new XVariable[_size];
		System.arraycopy(_variables, 0, x, 0, _size);
		return x;
	}
}