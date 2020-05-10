package org.xdef.sys;

import java.io.IOException;
import java.util.ArrayList;

/** Source position.
 * @author  Vaclav Trojan
 */
public class SPosition {

	/** Actual buffer position to source buffer. */
	private int _bufIndex;
	/** Line number. */
	private long _line;
	/** Relative file position to the beginning of source of start of line. */
	private long _startLine;
	/** Relative position of parsed text to the beginning of source text. */
	private long _filePos;
	/** File name or URL of source data (or null). Note that PublicId can be
	 appended to sysId as "$pubid[" ... "]".*/
	private String _sysId;
	/** list of replacements in the source buffer. */
	private ArrayList<Replacement> _modificationInfo;

	/** Creates a new empty instance of SPosition. */
	public SPosition() {}

	/** Creates a new instance of SPosition as copy of given position.
	 * @param line line number.
	 * @param column column number.
	 * @param sysId system ID or null.
	 * @param pubId public ID or null.
	 */
	public SPosition(final int line,
		final int column,
		final String sysId,
		final String pubId) {
		_bufIndex = column > 0 ? column - 1 : 0;
		_line = line == 0 ? 0 : line < 0 ? Integer.MAX_VALUE - line : line;
		_sysId = pubId == null ? sysId : sysId == null
			? "$pubid[" + pubId + ']' : (sysId + "$pubid[" + pubId + ']');
//		_modificationInfo = null; _filePos = _startLine = 0L; //Java makes it!
	}

	/** Creates a new instance of SPosition as copy of given position.
	 * @param bufIndex buffer position.
	 * @param line line number.
	 * @param startLine file position of the last start of line.
	 * @param filePos file position of the start of buffer.
	 * @param rawSysId the string with raw form of sysId and putId.
	 */
	public SPosition(final int bufIndex,
		final long line,
		final long startLine,
		final long filePos,
		final String rawSysId) {
		_bufIndex = bufIndex;
		_line = line;
		_startLine = startLine;
		_filePos = filePos;
		_sysId = rawSysId;
//		_modificationInfo = null; //Java makes it!
	}

	/** Creates a new instance of SPosition as copy of given position.
	 * @param spos The SPosition.
	 */
	public SPosition(final SPosition spos) {
		if (spos != null) {
			_bufIndex = spos._bufIndex;
			_line = spos._line;
			_startLine = spos._startLine;
			_filePos = spos._filePos;
			_sysId = spos._sysId;
			if (spos._modificationInfo != null) {
				_modificationInfo = spos.cloneModificationInfo();
			}
		}
	}

	/** Set position to this object.
	 * @param line line number.
	 * @param column column number.
	 * @param sysId system ID or null.
	 * @param pubId public ID or null.
	 */
	public final void setPosition(final int line,
		final int column,
		final String sysId,
		final String pubId) {
		_bufIndex = column > 0 ? column - 1 : 0;
		_line = line == 0 ? 0 : line < 0 ? Integer.MAX_VALUE - line : line;
		_sysId = pubId == null ? sysId : sysId == null
			? "$pubid[" + pubId + ']' : (sysId + "$pubid[" + pubId + ']');
		_filePos = 0L;
		_startLine = 0L;
		_modificationInfo = null;
	}

	public final void setNewLine() {
		_line++;
		_startLine = _filePos + _bufIndex + 1;
	}

	/** Set position from other position.
	 * @param spos other position.
	 */
	public final void setPosition(final SPosition spos) {
		_bufIndex = spos._bufIndex;
		_line = spos._line;
		_sysId = spos._sysId;
		_filePos = spos._filePos;
		_startLine = spos._startLine;
		cloneModificationInfo(spos);
	}
	/** Clone ModificationInfo.
	 * @return a clone of ModificationInfo.
	 */
	private ArrayList<Replacement> cloneModificationInfo() {
		int n;
		if (_modificationInfo == null || (n = _modificationInfo.size()) == 0) {
			return null;
		}
		// Note can't just copy!
		ArrayList<Replacement> result = new ArrayList<Replacement>(n);
		for (int i = 0; i < n; i++) {
			result.add(new Replacement(_modificationInfo.get(i)));
		}
		return result;
	}

	/** Get buffer index.
	 * @return buffer index.
	 */
	public final int getIndex() {return _bufIndex;}

	/** Set buffer index.
	 * @param pos buffer index.
	 */
	public final void setIndex(final int pos) {_bufIndex = pos;}

	/** Increase buffer index.
	 * @return increased index.
	 */
	public final int incIndex() {return ++_bufIndex;}

	/** Decrease buffer index.
	 * @return decreased index.
	 */
	public final int decIndex() {return --_bufIndex;}

	/** Get relative position to the beginning of start of line.
	 * @return relative position to the beginning of start of line.
	 */
	public final long getStartLine() {return _startLine;}

	/** Set relative position to the beginning of start of line.
	 * @param startLine relative position to the beginning of start of line.
	 */
	public final void setStartLine(final long startLine) {_startLine=startLine;}

	/** Get relative file position of parsed text.
	 * @return relative file position of parsed text.
	 */
	public final long getFilePos(){return _filePos;}

	/** Set relative file position of parsed text.
	 * @param filePos Relative file position of parsed text.
	 */
	public final void setFilePos(final long filePos){_filePos = filePos;}

	/** Get file name or URL of source data (or null). Note that public id can
	 * be appended to sysId as "$pubid["... public ..."]".
	 * @return sysId the file name or URL of source data (or null).
	 */
	public final String getSysId() {return _sysId;}

	/** Set file name or URL of source data (or null). Note that public id can
	 * be appended to sysId as "$pubid["... public ..."]".
	 * @param sysId the file name or URL of source data (or null).
	 */
	public final void setSysId(final String sysId) {_sysId = sysId;}

	/** Return the line number where the current document event ends. Lines are
	 * delimited by line ends (0x0D). Warning: The return value from the method
	 * is intended only as an approximation for the sake of diagnostics.
	 * If possible, the parser should provide the line position of the first
	 * character after the text associated with the document event. The first
	 * line is line 1.
	 * @return The line number, or -1 if none is available.
	 */
	public final long getLineNumber() { return _line; }

	/** Set the line number.
	 * @param x line number, or -1 if none is available.
	 */
	public final void setLineNumber(long x) { _line = x; }

	/** Return the column number where the current document event ends. This is
	 * one-based number of Java char values since the last line end. Warning:
	 * The return value from the method is intended only as an approximation for
	 * the sake of diagnostics.
	 * If possible, the parser should provide the line position of the first
	 * character after the text associated with the document event. The first
	 * column in each line is column 1.
	 * @return The column number, or -1 if none is available.
	 */
	public final long getColumnNumber() {
		return _filePos + _bufIndex - _startLine + 1;
	}

	/** Set the column number.
	 * @param x column number, or -1 if none is available.
	 */
	public final void setColumnNumber(long x) {
		if (x > 0) {
			_bufIndex = (int) (_startLine - _filePos - 1 + x);
		}
	}

	/** Return the system identifier for the current document. The return value
	 * is the system identifier (the name of source data) of the document. If
	 * the system identifier is a URL, the parser must resolve it fully before
	 * passing it to the application. For example, a file name must always be
	 * provided as a file:... URL, and other kinds of relative URI are also
	 * resolved against their bases.
	 * @return A string containing the system identifier, or null if none is
	 * available.
	 */
	public final String getSystemId() {
		int ndx;
		return _sysId != null && (ndx = _sysId.indexOf("$pubid[")) >= 0 &&
			_sysId.endsWith("]") ? _sysId.substring(0, ndx) : _sysId;
	}

	/** Return the public identifier for the current document.
	 * @return A string containing the public identifier, or null if none is
	 * available.
	 */
	public final String getPublicId() {
		int ndx;
		return _sysId != null && (ndx = _sysId.indexOf("$pubid[")) >= 0 &&
			_sysId.endsWith("]") ?
			_sysId.substring(ndx + 7, _sysId.length() - 1) :  null;
	}

	/** Get position related to the start of source data.
	 * @return The position.
	 */
	public final long getSourcePosition() { return _filePos + _bufIndex; }

	/** Return the position corrected to original buffer (before modifications).
	 * Position modifications are resolved (the result does not contain them).
	 * @return the corrected position object.
	 */
	public final SPosition correctPosition() {
		return _modificationInfo == null ? this : correctPosition(_bufIndex);
	}

	/** Return the position corrected to original buffer (before modifications).
	 * Position modifications are resolved (the result does not contain them).
	 * @param pos Relative position in actual buffer.
	 * @return the position in original buffer.
	 */
	public final SPosition correctPosition(final int pos) {
		int n;
		Replacement lastItem;
		if (_modificationInfo == null || (n = _modificationInfo.size()) == 0
			|| pos < (lastItem = _modificationInfo.get(0))._bufIndex) {
			return new SPosition(pos, _line, _startLine, _filePos, _sysId);
		}
		// find Replacement with the lisne equal or greater then in the position
		int m = -1;
		for (int i = 0; i < n; i++) {
			Replacement item = _modificationInfo.get(i);
			if (item._line >= _line || item._fixed) {
				lastItem = item;
				m = i;
				break;
			}
		}
		if (m >= 0 && lastItem._bufIndex < pos) {
			// find the last replacement with index smaller then in the position
			for (int i = m + 1; i < n; i++) {
				Replacement item = _modificationInfo.get(i);
				if (item._bufIndex > pos) {
					break;
				}
				lastItem = item;
			}
		}
		return new SPosition((lastItem._fixed) // return corrected position
			? - lastItem._diff : pos - lastItem._bufIndex - lastItem._diff,
			lastItem._line,
			lastItem._startLine,
			lastItem._startLine,
			lastItem._sysId);
	}

	/** Create position information to report.
	 * @param report The report object.
	 * @param pos buffer position.
	 */
	public final void genPositionInfo(final int pos, final Report report) {
		correctPosition(pos).genPositionInfo(report);
	}

	/** Create position information to report.
	 * @param report The report object.
	 */
	public final void genPositionInfo(final Report report) {
		String text = report.getText();
		if (text == null) {
			text = Report.getReportText(report.getMsgID(), "eng");
			if (text == null) {
				text = "";
			}
		}
		String sysId;
		String modification = report.getModification();
		if (modification == null) {
			modification = (getSourcePosition() < 0 ?
				"": "&{pos}" + getSourcePosition()) +
				(_line <= 0 ? "" : "&{line}" + _line) +
				(_line <= 0 || getColumnNumber() < 0 ?
					"" : "&{column}" + getColumnNumber()) +
				((sysId = getSystemId()) == null || sysId.isEmpty() ?
					"" : "&{sysId}" + sysId);
			if (modification.indexOf("&{line}") >= 0 &&
				text.indexOf("&{#SYS000}") < 0 &&
				text.indexOf("&{line}") < 0) {
				text += "&{line}{; line=}";
			}
			if (modification.indexOf("&{column}") >= 0 &&
				text.indexOf("&{#SYS000}") < 0 &&
				text.indexOf("&{column}") < 0) {
				text += "&{column}{; column=}";
			}
			if (modification.indexOf("&{sysId}") >= 0 &&
				text.indexOf("&{#SYS000}") < 0 &&
				text.indexOf("&{sysId}") < 0) {
				text += "&{sysId}{; source='}{'}";
			}
			report.setModification(modification);
		} else {
			if (modification.indexOf("&{line}") < 0	&& _line > 0) {
				if (text.indexOf("&{line}") < 0 &&
					text.indexOf("&{#SYS000}") < 0) {
					text += "&{line}{; line=}";
				}
				modification += "&{line}" + _line;
			}
			if (_line > 0 && modification.indexOf("&{column}") < 0 &&
				getColumnNumber() > 0) {
				if (text.indexOf("&{column}") < 0 &&
					text.indexOf("&{#SYS000}") < 0) {
					text += "&{column}{; column=}";
				}
				modification += "&{column}" + getColumnNumber();
			}
			if (modification.indexOf("&{sysId}") < 0 &&
				(sysId = getSystemId()) != null && sysId.length() > 0) {
				if (text.indexOf("&{sysId}") < 0 &&
					text.indexOf("&{#SYS000}") < 0) {
					text += "&{sysId}{; source='}{'}";
				}
				modification += "&{sysId}" + sysId;
			}
			if (modification.indexOf("&{pos}") < 0 &&
				getSourcePosition() >= 0) {
				if (text.indexOf("&{pos}") < 0) {
					text += "&{pos}{; pos=}";
				}
				modification += "&{pos}" + getSourcePosition();
			}
			if (modification.indexOf("&{xpath}") >= 0 &&
				text.indexOf("&{xpath}") < 0 && text.indexOf("&{#SYS000}") < 0){
				text += "&{xpath}{; xpath=}";
			}
			if (modification.indexOf("&{xdpos}") >= 0 &&
				text.indexOf("&{xdpos}") < 0 && text.indexOf("&{#SYS000}") < 0){
				text += "&{xdpos}{; X-position=}";
			}
		}
		report.setText(text);
		report.setModification(modification);
	}

	/** Put report with position information to the report writer.
	 * If report writer is null and report type is ERROR or FATAL
	 * is thrown the SRuntimeException created from the report.
	 * @param pos The source buffer position.
	 * @param report Report to be sent to reporter or thrown.
	 * @param reportWriter Report writer or <tt>null</tt>.
	 * @throws SRuntimeException if report writer is null and report type
	 * is ERROR or FATAL.
	 */
	public final void putReport(final int pos,
		final Report report,
		final ReportWriter reportWriter) throws SRuntimeException {
		SPosition p = correctPosition(pos);
		p.genPositionInfo(report);
		if (reportWriter == null) {
			if (report.getType() == Report.WARNING) {
				return;
			}
			throw new SRuntimeException(report);
		}
		reportWriter.putReport(report);
		if (report.getType() != Report.FATAL) {
			return;
		}
		reportWriter.checkAndThrowErrors();
	}

	/** Put report with position information to the report writer.
	 * If report writer is null and report type is ERROR or FATAL
	 * is thrown the SRuntimeException created from the report.
	 * @param report Report to be sent to reporter or thrown.
	 * @param reportWriter Report writer or <tt>null</tt>.
	 * @throws SRuntimeException if report writer is null and report type
	 * is ERROR or FATAL.
	 */
	public final void putReport(final Report report,
		final ReportWriter reportWriter) throws SRuntimeException {
		correctPosition().genPositionInfo(report);
		if (reportWriter == null) {
			if (report.getType() == Report.WARNING) {
				return;
			}
			throw new SRuntimeException(report);
		}
		reportWriter.putReport(report);
		if (report.getType() != Report.FATAL) {
			return;
		}
		reportWriter.checkAndThrowErrors();
	}

	@Override
	/** Check if some object is equal to this position.
	 * @param obj Object to be compared.
	 * @return <tt>true</tt> if the argument is considered as the same
	 * position as this one position; otherwise return <tt>false</tt>.
	 */
	public final boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof SPosition)) {
			return false;
		}
		SPosition pos = (SPosition) obj;
		if (_sysId != null) {
			if (!_sysId.equals(pos._sysId)) {
				return false;
			}
		}  else if (pos._sysId != null) {
			return false;
		}
		return pos._filePos + pos._bufIndex == _filePos + _bufIndex;
	}

	@Override
	public int hashCode() {
		int hash = 79 * 7 + _bufIndex;
		hash = 79 * hash + (_sysId != null ? _sysId.hashCode() : 0);
		return 79 * hash + (int) (_filePos ^ (_filePos >>> 32));
	}

	/** Compares this position with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this position is less
	 * than, equal to, or greater than the specified object.<p>
	 * The compare algorithm checks
	 * <ul>
	 * <li>1. source name</li>
	 * <li>2. line</li>
	 * <li>2. column</li>
	 * </ul>
	 * This helps to sort messages according to source position.
	 * @param pos the SPosition to be compared.
	 * @return  <tt>-1</tt>, zero, or <tt>+1</tt> as this position
	 * is less than, equal to, or greater than the specified object.
	 * @throws ClassCastException if the specified object's type prevents it
	 * from being compared to this Object.
	 */
	public int compareTo(final SPosition pos) {
		if (_sysId != null) {
			if (!_sysId.equals(pos._sysId)) {
				return _sysId.compareTo(pos._sysId);
			} else if (pos._sysId != null) {
				return -1;
			}
		}
		long fpos1 = pos._filePos + pos._bufIndex;
		long fpos = _filePos + _bufIndex;
		return fpos1 == fpos ? 0 : fpos1 < fpos ? 1 : -1;
	}

	/** Add line position. All line positions MUST be set at the end of actual
	 * position modifications list.
	 * @param pos relative position in the buffer.
	 * @param line line number.
	 * @param startLine position of actual line in the file.
	 * @param diff offset difference,
	 * @param fixed switch if position is fixed.
	 */
	public final void addPos(final int pos,
		final long line,
		final long startLine,
		final int diff,
		final boolean fixed) {
		if (_modificationInfo == null) {
			//first replacement
			_modificationInfo = new ArrayList<Replacement>();
		}
		_modificationInfo.add(
			new Replacement(pos, diff, line, startLine, fixed, _sysId));
	}

	/** Add line position. All line positions MUST be set at the end of actual
	 * position modifications list.
	 * @param pos The position of line in the buffer.
	 * @param line line number.
	 * @param startLine position of actual line in the file.
	 */
	public final void addLine(final int pos,
	final long line,
	final long startLine) {
		if (_modificationInfo == null) {
			//first replacement
			_modificationInfo = new ArrayList<Replacement>();
		}
		_modificationInfo.add(
			new Replacement(pos, 0, line, startLine, false, _sysId));
	}

	/** Add position to internal list of positions. (This method is not public,
	 * it is used only in SBuffer).
	 * @param pos position in buffer.
	 * @param spos source position object.
	 * @param len length of text in buffer described by this position.
	 * @param fixed if true then internal position is not printed.
	 */
	final void appendPos(final int pos,
		final SPosition spos,
		final int len,
		final boolean fixed) {
		if (_modificationInfo == null) {
			_modificationInfo = new ArrayList<Replacement>();
		}
		int diff = (int) (spos._startLine + spos._bufIndex - spos._filePos);
		_modificationInfo.add(new Replacement(pos,
			diff,
			spos._line,
			spos._startLine,
			fixed,
			spos._sysId));
		int n;
		if (spos._modificationInfo != null &&
			(n = spos._modificationInfo.size()) > 0) {
			for (int i = 0; i < n; i++) {
				Replacement item = spos._modificationInfo.get(i);
				_modificationInfo.add(new Replacement(
					pos + item._bufIndex - diff,
					diff,
					item._line,
					item._startLine,
					fixed,
					item._sysId));
			}
		}
	}

	/** Update list of modifications when given interval is changed to
	 * the new length. (This method is not public, it is used only in
	 * StringParser).
	 * @param pos The buffer position of modification.
	 * @param length The length of modified original.
	 * @param newLength The length of modification.
	 * @param fixed if true one position will be used for whole range.
	 */
	public final void updatePositions(final int pos,
		final int length,
		final int newLength,
		final boolean fixed) {
		int diff = length - newLength;
		int newEndPos = pos + newLength;
		int endPos = pos + length;
		// The replacement may be a modification of existing source
		// (newPos == null) or it may be considered as the independent
		// part (newPos != null). Overlapnig replacements are considered
		// as the part of the first replacement.

		// There are 5 situations of replacement position:
		// 1. There is no replacement block yet -> add first replacement
		// 2. is between two existing or before the first one -> insert before
		// 3. covers whole area between the existing ones -> join and/or extend
		// 4. extends the existing one -> update
		// 5. is after the last one -> add to the end of list
		// 6. text is added to the end of source buffer
		int n; //number of items in replacements
		if (_modificationInfo == null) {
			//first replacement
			_modificationInfo = new ArrayList<Replacement>();
			n = 0; // empty list
		} else {
			n = _modificationInfo.size(); // number of items in the list
		}
		if (n == 0) { // create first replacement - situation 0
			_modificationInfo.add(new Replacement(
				pos, 0, _line, _startLine, true, _sysId));
			_modificationInfo.add(new Replacement(
				pos + newLength, diff, _line, _startLine, true, _sysId));
			return;
		}
		Replacement item = null;
		for (int i = 0; i <  n; i++) {
			Replacement lastItem = item;
			item = _modificationInfo.get(i);
			Replacement nitem;
			if (item._bufIndex > pos) {
				if (endPos <= item._bufIndex) { // insert before an item
					if (i == 0) { // insert before the first item
						_modificationInfo.add(0, new Replacement(pos,
							0, _line, _startLine, true, _sysId));
						_modificationInfo.add(1, new Replacement(pos+newLength,
							diff, _line, _startLine, true, _sysId));
						return;
					} else {
						// insert between lastItem and item
						_modificationInfo.add(i, new Replacement(pos,
							lastItem._bufIndex - pos, lastItem._line,
							lastItem._startLine, true, lastItem._sysId));
					}
					if (endPos < item._bufIndex) {
						_modificationInfo.add(++i, new Replacement(newEndPos,
							lastItem._bufIndex - endPos,
							lastItem._line,
							lastItem._startLine,
							lastItem._fixed,
							lastItem._sysId));
					}
					i++; n++;
					while (i < n) {
						nitem = _modificationInfo.get(i);
						nitem._bufIndex -= diff;
						if (nitem._startLine == lastItem._startLine) {
							nitem._diff += diff;
						}
						i++;
					}
					return;
				} else {
					_modificationInfo.add(0, new Replacement(
						pos, 0, item._line, item._startLine, true,item._sysId));
					n++; i++;
					//update all following items
					while (i < n) {
						nitem = _modificationInfo.get(i);
						if (endPos < nitem._bufIndex) {
							_modificationInfo.remove(i);
							n--;
						} else {
							if (endPos == item._bufIndex) {
							} else {
								_modificationInfo.add(i,
									new Replacement(newEndPos,
										diff, item._line,
										item._startLine, true, item._sysId));
							}
							break;
						}
					}
					while (i < n) {
						nitem = _modificationInfo.get(i);
						nitem._bufIndex -= diff;
						if (item._startLine == nitem._startLine) {
							nitem._diff += diff;
						}
						i++;
					}
					return;
				}
			}
		}
		_modificationInfo.add(new Replacement(pos,
			0, item._line, item._startLine, true, item._sysId));
		_modificationInfo.add(new Replacement(pos,
			diff, item._line, item._startLine, true, item._sysId));
	}


	/** Copy modification information from given position to this position.
	 * (raw copy). (This method is not public, it is used only in StringParser
	 * and StreamParser).
	 * @param pos position from which copy should be copied.
	 */
	final void copyModificationInfo(final SPosition pos) {
		_modificationInfo = pos._modificationInfo;
	}

	/** Set clone of modification information from given position to this
	 * position. (This method is not public, it is used only in StringParser
	 * and StreamParser).
	 * @param pos position from which copy should be copied.
	 */
	final void cloneModificationInfo(final SPosition pos) {
		_modificationInfo = pos.cloneModificationInfo();
	}

	/** Clear modification information (used only internally). */
	final void clearModificationInfo() { _modificationInfo = null; }


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Line: ").append(String.valueOf(_line));
		sb.append(", column: ").append(String.valueOf(getColumnNumber()));
		sb.append(", position: ").append(String.valueOf(getSourcePosition()));
		sb.append(", startLine: ").append(String.valueOf(_startLine));
		if (_sysId != null) {
			sb.append(", source: ").append(_sysId);
		}
		if (_modificationInfo == null || _modificationInfo.isEmpty()) {
			return sb.toString();
		}
		sb.append("; replacements:\n");
		for (int i = 0; i < _modificationInfo.size(); i++) {
			sb.append("[").append(i <= 9 ? "0"+i : String.valueOf(i)).
				append("] ");
			sb.append(_modificationInfo.get(i));
		}
		return sb.toString();
	}

	public final void writeObj(final SObjectWriter w) throws IOException {
		w.writeInt(_bufIndex);
		w.writeLong(_line);
		w.writeLong(_startLine);
		w.writeLong(_filePos);
		w.writeString(_sysId);
		int len = _modificationInfo == null ? 0 : _modificationInfo.size();
		w.writeLength(len);
		for (int i = 0; i < len; i++) {
			Replacement item = _modificationInfo.get(i);
			w.writeInt(item._bufIndex);
			w.writeInt(item._diff);
			w.writeLong(item._line);
			w.writeLong(item._startLine);
			w.writeBoolean(item._fixed);
			w.writeString(item._sysId);
		}
	}

	public static final SPosition readObj(final SObjectReader r)
		throws IOException {
		SPosition x = new SPosition();
		x._bufIndex = r.readInt();
		x._line = r.readLong();
		x._startLine = r.readLong();
		x._filePos = r.readLong();
		x._sysId = r.readString();
		int len = r.readLength();
		if (len > 0) {
			x._modificationInfo = new ArrayList<Replacement>(len);
			for (int i = 0; i < len; i++) {
				int pos = r.readInt();
				int diff = r.readInt();
				long line = r.readLong();
				long startLine = r.readLong();
				boolean fixed = r.readBoolean();
				String sysId = r.readString();
				x._modificationInfo.add(new Replacement(
					pos, diff, line, startLine, fixed, sysId));
			}
		}
		return x;
	}
	/** The object with modification information. */
	private final static class Replacement {
		/** Start position of replacement. */
		private int _bufIndex;
		/** Difference to start line position. */
		private int _diff;
		/** Original line number. */
		private final long _line;
		/** Original start line position. */
		private final long _startLine;
		/** If true use this position for whole range. */
		private final boolean _fixed;
		/** System ID. */
		private final String _sysId;

		/** create new Replacement object including a position.
		 * @param bufIndex position of replacement.
		 * @param diff difference to start line position.
		 * @param line line number.
		 * @param startLine starting position of line.
		 * @param fixed if true one position will be used for whole range.
		 * @param sPosition source position of replacement.
		 */
		Replacement(final int bufIndex,
			final int diff,
			final long line,
			final long startLine,
			final boolean fixed,
			final String sysId) {
			_bufIndex = bufIndex;
			_diff = diff;
			_line = line;
			_startLine = startLine;
			_fixed = fixed;
			_sysId = sysId;
		}

		/** Create new Replacement object as copy of given argument.
		 * @param r The replacement to be cloned.
		 */
		private Replacement(final Replacement r) {
			_bufIndex = r._bufIndex;
			_diff = r._diff;
			_line = r._line;
			_startLine = r._startLine;
			_fixed = r._fixed;
			_sysId = r._sysId;
		}

		@Override
		/** Get printable format of source position. */
		public String toString() {
			return "bufIdex" + _bufIndex +
				", line:" + _line +
				", diff:" + _diff + ", startLine:" + _startLine +
				", fixed:" + _fixed + ", sysId:" + _sysId;
		}
	}
}