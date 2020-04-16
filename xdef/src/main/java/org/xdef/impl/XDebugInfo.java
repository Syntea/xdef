package org.xdef.impl;

import org.xdef.sys.SPosition;
import org.xdef.impl.code.CodeDisplay;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMStatementInfo;
import org.xdef.model.XMVariable;
import java.io.IOException;
import java.util.ArrayList;

/** Debug information.
 * @author Vaclav Trojan
 */
public class XDebugInfo implements XMDebugInfo {
	XVariable[][] _varTables;
	/** List of statement info. */
	ArrayList<XStatementInfo> _statementList;

	public XDebugInfo() {
		_varTables = null;
		_statementList = new ArrayList<XStatementInfo>();
	}

	/** Add debug info item and return index.
	 * @param spos Source position.
	 * @param defName name of X-definition.
	 * @param codeAdr code address.
	 * @param varTable local variables table.
	 * @return index of debug info item or -1.
	 */
	public final int addInfo(final SPosition spos,
		final String defName,
		final int codeAdr,
		final XVariable[] varTable) {
		SPosition sp = spos.correctPosition();
		XStatementInfo x = new XStatementInfo(sp.getLineNumber(),
			sp.getColumnNumber(), sp.getSystemId(), defName, codeAdr, varTable);
		if (_statementList.isEmpty()
			|| !_statementList.get(_statementList.size() - 1).equals(x)) {
			int result = _statementList.size();
			_statementList.add(x);
			return result;
		}
		return -1;
	}

	/** Set source end position to debug info item;
	 * @param index index of debug info item.
	 * @param spos end source position.
	 */
	public final void setEndPosition(final int index, final SPosition spos) {
		if (index < 0) return;
		XStatementInfo x = _statementList.get(index);
		if (x == null) return;
		SPosition sp = spos.correctPosition();
		String sysId = sp.getSystemId();
		long line = sp.getLineNumber();
		long column = sp.getColumnNumber();
		if ((x._sysId != null && x._sysId.equals(sysId) ||
			x._sysId == null && sysId == null)
			&& (x._line == line  && x._column < column || x._line <= line)) {
			x._end_line = line;
			x._end_column = column;
		}
	}

	@Override
	public final XMStatementInfo getInfo(final int codeAdr) {
		int len = _statementList.size();
		for (int i = 0; i < len; i++) {
			XStatementInfo info = _statementList.get(i);
			if (codeAdr >= info._codeAdr) {
				if (i+1 == len) {
					return info;
				}
				XStatementInfo info1 = _statementList.get(i+1);
				if (codeAdr <= info1._codeAdr) {
					return info1;
				}
			}
		}
		return null;
	}

	/** Get statement information assigned to code address.
	 * @param addr code address.
	 * @return XMStatementInfo object.
	 */
	public final XMStatementInfo getStatementInfo(final int addr) {
		for (int i = 0; i < _statementList.size(); i++) {
			XStatementInfo si = _statementList.get(i);
			if (addr >= si.getAddr()) {
				return si;
			}
		}
		return null;
	}

	@Override
	/** Get array of statement information objects assigned to given
	 * X-definition and source line.
	 * @param line source line.
	 * @param xdName name of X-definition.
	 * @return array XMStatementInfo objects (if no statement information
	 * is found the array is empty).
	 */
	public final XMStatementInfo[] getStatementInfo(final long line,
		final String xdName) {
		for (int i = 0; i < _statementList.size(); i++) {
			XStatementInfo x = _statementList.get(i);
			if (line == x._line
				&& ((xdName==null && (x._xdName==null || x._xdName.isEmpty()))
				|| xdName.equals(x._xdName == null ? "" : x._xdName))) {
				int min = i, max = i;
				for (int j = i + 1; j < _statementList.size(); j++) {
					XStatementInfo y = _statementList.get(j);
					if (line == y._line && ((xdName==null &&
						(y._xdName==null || y._xdName.isEmpty()))
						|| xdName.equals(y._xdName == null ? "" : y._xdName))) {
						max = j;
					} else {
						break;
					}
				}
				XMStatementInfo[] result = new XMStatementInfo[max-min+1];
				_statementList.subList(min, max + 1).toArray(result);
				return result;
			}
		}
		return new XMStatementInfo[0];
	}

	@Override
	/** Get statement information according to the source position.
	 * @param line source line.
	 * @param column source column.
	 * @param sysId source ID (pathname, URL).
	 * @param xdName name of X-definition (may be null).
	 * @return XMStatementInfo object or null.
	 */
	public final XMStatementInfo getStatementInfo(final long line,
		final long column,
		final String sysId,
		final String xdName) {
		// find the statement at the line position
		for (int i = 0; i < _statementList.size(); i++) {
			XStatementInfo inf = _statementList.get(i);
			String n1 = inf.getXDName();
			if (xdName != null && !xdName.equals(n1)) {
				continue;
			}
			String s1 = inf.getSysId();
			if (sysId != null && !sysId.equals(s1)) {
				continue;
			}
			if (inf.posIn(line, column)) {
				if (line == inf._line) {
					return inf;
				}
			}
		}
		return null;
	}

	@Override
	/** Get array of all statement information objects.
	 * @return array of XMStatementInfo objects.
	 */
	public final XMStatementInfo[] getStatementInfo() {
		XStatementInfo[] result = new XStatementInfo[_statementList.size()];
		_statementList.toArray(result);
		return result;
	}

	@Override
	/** Get statement information of the next statement after argument.
	 * @param si XMStatementInfo object or null.
	 * @return next XMStatementInfo object after argument or null.
	 */
	public final XMStatementInfo nextStatementInfo(final XMStatementInfo si) {
		if (si != null) {
			for (int i = 0; i < _statementList.size(); i++) {
				if (si == _statementList.get(i)) {
					return i >= _statementList.size() - 1 ? null :
						_statementList.get(i + 1);
				}
			}
		}
		return null;
	}

	@Override
	/** Get statement information of the previous statement before argument.
	 * @param si XMStatementInfo object or null.
	 * @return previous XMStatementInfo object before argument or null.
	 */
	public final XMStatementInfo prevStatementInfo(final XMStatementInfo si) {
		if (si != null) {
			for (int i = 0; i < _statementList.size(); i++) {
				if (si == _statementList.get(i)) {
					return i == 0 ? null : _statementList.get(i - 1);
				}
			}
		}
		return null;
	}

	final void writeXD(final XDWriter xw) throws IOException {
		int size = _varTables == null ? 0 :_varTables.length;
		xw.writeInt(size);
		for (int i = 0; i < size; i++) {
			XVariable[] x = _varTables[i];
			xw.writeInt(x.length);
			for (int j = 0; j < x.length; j++) {
				XVariable y = x[j];
				y.writeXD(xw);
			}
		}
		size = _statementList.size();
		xw.writeInt(size);
		for (int i = 0; i < size; i++) {
			XStatementInfo x = _statementList.get(i);
			xw.writeLong(x._line);
			xw.writeLong(x._column);
			xw.writeInt(x._codeAdr);
			xw.writeString(x._sysId);
			xw.writeString(x._xdName);
			xw.writeInt(x._varTableIndex);
			xw.writeLong(x._end_line);
			xw.writeLong(x._end_column);
		}
	}

	private XStatementInfo newXStatementInfo(final long line,
		final long column,
		final String spos,
		final String xdName,
		final int codeAdr,
		final int varTableIndex) {
		return new XStatementInfo(line,
			column, spos, xdName, codeAdr, varTableIndex);
	}

	final static XDebugInfo readXDebugInfo(final XDReader xr)
		throws IOException {
		XDebugInfo xdi = new XDebugInfo();
		int size = xr.readInt();
		XVariable[][] tab = new XVariable[size][0];
		for (int i = 0; i < size; i++) {
			int len = xr.readInt();
			tab[i] = new XVariable[len];
			for (int j = 0; j < len; j++) {
				tab[i][j] = XVariable.readXD(xr);
			}
			xdi._varTables = tab;
		}
		size = xr.readInt();
		for (int i = 0; i < size; i++) {
			long line = xr.readLong();
			long column = xr.readLong();
			int codeAddr = xr.readInt();
			String spos = xr.readString();
			String xdName = xr.readString();
			int varTabIndex = xr.readInt();
			XStatementInfo x = xdi.newXStatementInfo(line,
				column, spos, xdName, codeAddr, varTabIndex);
			x._end_line = xr.readLong();
			x._end_column = xr.readLong();
			xdi._statementList.add(x);
		}
		return xdi;
	}

	class XStatementInfo implements XMStatementInfo {
		final long _line;
		final long _column;
		final int _codeAdr;
		long _end_line;
		long _end_column;
		final String _sysId;
		final int _varTableIndex;
		final String _xdName;

		XStatementInfo(final long line,
			final long column,
			final String spos,
			final String xdName,
			final int codeAdr,
			final int varTableIndex) {
			_column = column;
			_line = line;
			_codeAdr = codeAdr;
			_sysId = spos;
			_xdName = xdName;
			_varTableIndex = varTableIndex;
			_end_line = _end_column = -1;
		}

		XStatementInfo(final long line,
			final long column,
			final String spos,
			final String xdName,
			final int codeAdr,
			final XVariable[] varTable) {
			_column = column;
			_line = line;
			_codeAdr = codeAdr;
			_sysId = spos;
			_xdName = xdName;
			_end_line = _end_column = -1;
			if (varTable == null) {
				_varTableIndex = -1;
			} else if (_varTables == null) {
				_varTables = new XVariable[1][0];
				_varTableIndex = 0;
			} else {
				_varTableIndex = getVartabIndex(varTable);
			}
		}

		private boolean equalVartab(final XMVariable[] tab1,
			final XMVariable[] tab2) {
			int size = tab1.length;
			if (tab2.length != size) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (!tab1[i].equals(tab2[i])) {
					return false;
				}
			}
			return true;
		}

		private int getVartabIndex(final XVariable[] varTable) {
			if (varTable == null || varTable.length == 0) {
				return 0;
			}
			for (int i = 0; i < _varTables.length; i++) {
				if (equalVartab(_varTables[i], varTable)) {
					return i;
				}
			}
			XVariable[][] old = _varTables;
			_varTables = new XVariable[old.length + 1][0];
			System.arraycopy(old, 0, _varTables, 0, old.length);
			_varTables[old.length] = varTable;
			return old.length;
		}
		@Override
		public final String getSysId() {return _sysId;}
		@Override
		public final long getColumn() {return _column;}
		@Override
		public final long getLine() {return _line;}
		@Override
		public final long getEndColumn() {return _end_column;}
		@Override
		public final void updateEndPos(final long line, final long column) {
			_end_line = line;
			_end_column = column;
		}
		@Override
		public final long getEndLine() {return _end_line;}
		@Override
		public final int getAddr() {return _codeAdr;}
		@Override
		/** Get name of X-definition.
		 * @return name of X-definition.
		 */
		public final String getXDName() {return _xdName;}
		@Override
		public final XMVariable[] getLocalVariables() {
			return _varTableIndex < 0 ? null : _varTables[_varTableIndex];
		}
		@Override
		public int hashCode() {
			return (int) (11*_codeAdr + 7*(_line + 5*(_column+_varTableIndex)));
		}
		@Override
		public boolean equals(final Object obj) {
			if (obj == null || !(obj instanceof XStatementInfo)) {
				return false;
			}
			XStatementInfo x = (XStatementInfo) obj;
			return _line == x._line && _column == x._column &&
				(_xdName != null && _xdName.equals(x._xdName)
					|| _xdName == null && x._xdName == null)
				&& _codeAdr == x._codeAdr && _varTableIndex == x._varTableIndex;
		}
		@Override
		public String toString() {
			String s = "Line:" + getLine() + ", column: " + getColumn() +
				", source: " + _sysId +
				", addr: " + getAddr() + ", tabindex: " + _varTableIndex +
				", xdef: " + _xdName;
			if (_varTableIndex >= 0) {
				XVariable[] vars = _varTables[_varTableIndex];
				boolean first = true;
				for (XVariable v: vars) {
					if (first) {
						s += '\n';
						first = false;
					} else {
						s += "\", ";
					}
					s += CodeDisplay.getTypeName(v.getType())+" "+v.getName();
				}
			}
			return s;
		}

		final boolean posIn(final long line, final long column) {
			if (_end_line == -1) {
				return line == _line
					&& column >= _column  && column <= _column+2;
			}
			if (line == _line) {
				if (_end_line > line) {
					return true;
				}
				return column <= _end_column;
			}
			return line <= _end_line;
		}

		final int comparePos(final XStatementInfo x) {
			return comparePos(x._line, x._column);
		}

		final int comparePos(final long line, final long column) {
			if (_line < line) {
				return -1;
			}
			if (_line > line) {
				return -1;
			}
			return _column < column ? -1 : _column > column ? 1 : 0;
		}
	}
}