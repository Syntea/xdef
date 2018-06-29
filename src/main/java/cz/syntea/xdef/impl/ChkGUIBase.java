/*
 * File: ChkGUIBase.java
 *
 * Copyright 2013 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.impl.xml.XInputStream;
import cz.syntea.xdef.sys.SUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/** Base of Swing frame for GUI. */
class ChkGUIBase {

	/** Font used for source and information area. */
	Font FONT_TEXT;
	/** Font used for source position information. */
	Font FONT_POSITIONINFO;
	/** Line number area background color. */
	Color COLOR_LINENUMBERS;
	/** Error background color in linenumber area . */
	Color COLOR_ERROR;
	/** Breakpoint background color in linenumber area. */
	Color COLOR_BREAKPOINT;
	/** Foreground of position information. */
	Color COLOR_POSITION;
	/** Information area background color. */
	Color COLOR_INFO;
	/** Source area background color. */
	Color COLOR_SOURCE;

	/** Error marked line number.*/
	Style STYLE_ERROR;
	/** Normal line number.*/
	Style STYLE_NORMAL;
	/** Line with break point. */
	Style STYLE_BREAKPOINT;
	/** Source area white. */
	Style STYLE_WHITE;

	/** The frame. */
	JFrame _frame;
	/** Menu bar of frame. */
	JMenuBar _menuBar;
	/** Width of frame. */
	int _width;
	/** Height of frame. */
	int _height;
	// Source window
	/** Source pane. */
	JPanel _sourcePane;
	/** Source window. */
	JTextPane _sourceArea;
	/** Line numbers. */
	JTextPane _lineNumberArea;

	// Position information
	/** Information about caret position in source area. */
	JLabel _sourcePositionInfo;

	// Informaton text window
	/** Information window. */
	JTextArea _infoArea;

	/** Actual source item. */
	XSourceItem _sourceItem;
	/** Name of actual source item. */
	String _sourceID;
	/** Array of position information.*/
	SourcePos[] _positions;
	/** XDPool object.*/
	XPool _xdpool;

	/** Create empty instance of GUI. */
	ChkGUIBase() {}

	/** Initialize GUI.*/
	final void openGUI() {
		// GUI fonts, colors and styles
		FONT_TEXT = new Font("monospaced", Font.BOLD, 14);
		FONT_POSITIONINFO = new Font("monospaced", Font.BOLD, 15)
			.deriveFont(Font.TYPE1_FONT);

		COLOR_LINENUMBERS = new Color(222, 222, 222);
		COLOR_ERROR = Color.RED;
		COLOR_BREAKPOINT = Color.CYAN;
		COLOR_POSITION = Color.MAGENTA;
		COLOR_INFO = new Color(200, 200, 255);
		COLOR_SOURCE = Color.WHITE;

		StyleContext context = new StyleContext();
		STYLE_ERROR = context.addStyle("marked", null);
		StyleConstants.setBackground(STYLE_ERROR, COLOR_ERROR);
		STYLE_NORMAL = context.addStyle("normal", null);
		StyleConstants.setBackground(STYLE_NORMAL, COLOR_LINENUMBERS);
		STYLE_BREAKPOINT = context.addStyle("breakpoint", null);
		StyleConstants.setBackground(STYLE_BREAKPOINT, COLOR_BREAKPOINT);
		STYLE_WHITE = context.addStyle("white", null);
		StyleConstants.setBackground(STYLE_WHITE, COLOR_SOURCE);

		// GUI variables
		_width = 870; //default width of frame
		_height = 600; //default height of frame
//		_width = 900; //default width of frame
//		_height = 596; //default height of frame
		_positions = new SourcePos[0];

		// GUI SWING components
		_frame = new JFrame();
		_menuBar = new JMenuBar();
		_sourceArea = new JTextPane();		// source window
		_sourceArea.setBackground(COLOR_SOURCE);
		_lineNumberArea = new JTextPane();	// line numbers
		_lineNumberArea.setFocusable(false);
		_lineNumberArea.setEditable(false);
		_sourcePositionInfo = new JLabel();	// position information
		_infoArea = new JTextArea(); //errors or trace information
		_frame.setVisible(false);
		_frame.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				int height = evt.getComponent().getHeight();
				int width = evt.getComponent().getWidth();
				if (width < 300 || height < 200) {
					if (width < 300) {
						width = 300;
					}
					if (height < 200) {
						height = 200;
					}
					_frame.setSize(_width, _height);
				}
				_width = width;
				_height = height;
			}
		});
		_infoArea.setFont(FONT_TEXT);
		_infoArea.setBackground(COLOR_INFO);
		_infoArea.setForeground(Color.BLUE);
		_lineNumberArea.setFont(FONT_TEXT);
		_sourcePositionInfo.setForeground(COLOR_POSITION);
		_sourcePositionInfo.setFont(FONT_POSITIONINFO);
//		_sourcePositionInfo.setToolTipText(
//			"Position of cursor in source window.");
		_sourceArea.setFont(FONT_TEXT);
		_sourceArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				SourcePos spos =
					new SourcePos(_sourceArea.getText(), e.getDot());
				_sourcePositionInfo.setText("  (Line:" + spos._line
					+ ", column:" + spos._column + ")");
			}
		});

		// Source window
		_sourcePane = new JPanel(new BorderLayout());
		_sourcePane.add(_sourceArea);
		JScrollPane jsp = new JScrollPane(_sourcePane);
		jsp.setRowHeaderView(_lineNumberArea);
		_frame.getContentPane().add(jsp);

		// Errors info window
		jsp = new JScrollPane();
		jsp.getViewport().add(_infoArea);
		_frame.add(jsp, BorderLayout.AFTER_LAST_LINE);
		_frame.setSize(_width, _height);
	}

	/** Initialize map with source items. */
	void initSourceMap() {
		for (Map.Entry<String, XSourceItem> e: _xdpool._sourcesMap.entrySet()) {
			XSourceItem src = e.getValue();
			String key = e.getKey();
			initSourceItem(key, src);
		}
	}

	/** Initialize the source item. */
	void initSourceItem(final String key, final XSourceItem src) {
		if (src._source != null) return;
		InputStream is = null;
		for (;;) {
			try {
				is = src._url != null ? src._url.openStream() : null;
				XInputStream myInputStream = new XInputStream(is);
				src._encoding = myInputStream.getXMLEncoding();
				src._source = SUtils.readString(myInputStream, src._encoding);
				myInputStream.close();
				return;
			} catch (Exception ex) {
				if (is != null) try {is.close();} catch (IOException x) {}
				String s = (src._url != null
					&& "file".equals(src._url.getProtocol())) ?
					src._url.getFile() : key;
				s = JOptionPane.showInputDialog("Source not found, change:", s);
				if(s != null) {//yes
					if (s.length() > 0 && s.charAt(0) == '<') {
						src._source = s;
						src._url = null;
						break;
					} else {
						try {
							File f = new File(s);
							src._url = f.toURI().toURL();
							src._changed = true;
						} catch (Exception exx) {
							src._source = "SOURCE NOT AVAILABLE!";
							src._url = null;
							break;
						}
					}
				} else {
					src._source = "SOURCE NOT AVAILABLE!";
					src._url = null;
					break;
				}
			}
		}
	}

	/** Close GUI: dispose window and remove allocated objects.
	 * @param ask if true, then it is shown the message that program finished.
	 */
	void closeGUI(boolean ask) {
		if (_frame != null) {
			_frame.setVisible(true);
			if (ask) {
				JOptionPane.showMessageDialog(_frame, "Program finished");
			}
			_frame.setVisible(false);
			for (Component component: _frame.getComponents()) {
				component.setEnabled(false);
			}
			do {_frame.dispose();} while(_frame.isActive());
			_frame.setEnabled(false);
			_frame.removeAll();
			_menuBar = null;
			_sourceArea = null;
			_lineNumberArea = null;
			_infoArea = null;
			_sourcePositionInfo = null;
			_frame = null;
			_sourceItem = null;
			_xdpool = null;
			_sourceID = null;
		}
	}

	/** Set to textPos the relative position in the source
	 * string according to line number and column number.
	 * If not found the value is -1.
	 * @param spos position to a source.
	 */
	void setCaretPositionToSource(SourcePos spos) {
		if (spos == null) return;
		int textPos;
		if (spos._line == 1) {
			textPos = (int) spos._column - 1;
		} else {
			String source =_sourceArea.getText();
			textPos = -1;
			int n = 1;
			int i = 0;
			while((i = source.indexOf('\n', i)) >=0) {
				if (++n >= spos._line) {
					textPos = i + (int) spos._column;
					break;
				}
				i++;
			}
		}
		if (textPos >= 0) {
			_sourceArea.setCaretPosition(textPos);
			_sourceArea.requestFocus();
		}
	}

	/** Set line numbers to line number area.*/
	void setLineNumberArea() {
		String t = _sourceArea.getText();
		_lineNumberArea.setFocusable(false);
		_lineNumberArea.setVisible(false);
		_lineNumberArea.setEditable(false);
		_lineNumberArea.setText("");
		StyledDocument sdoc = _lineNumberArea.getStyledDocument();
		for (int n = 1, pos = 0, offset = 0; pos >= 0;
			pos = t.indexOf('\n', pos+1), n++) {
			Style stype = STYLE_NORMAL;
			// mark line (errors, breakpoints etc)
			for (int j = 0; j < _positions.length; j++) {
				if (_sourceID.equals(_positions[j]._sysId) &&
					_positions[j]._line == n) {
					stype = STYLE_ERROR;
					break;
				}
			}
			String s = String.format("%3d\n", n);
			try {
				sdoc.insertString(offset, s, stype);
			} catch (Exception ex) {ex.printStackTrace(System.err);}
			offset += s.length();
		}
		_lineNumberArea.setStyledDocument(sdoc);
		_lineNumberArea.setIgnoreRepaint(false);
		_lineNumberArea.repaint();
		_lineNumberArea.setVisible(true);
		_sourcePane.repaint();
	}

	/** Wait for event finished. */
	synchronized void waitFrame() {
		try {this.wait();} catch (Exception ex) {}
	}

	/** Notify event action performed. */
	synchronized void notifyFrame() {notifyAll();}

	/** Find position in the array of positions.
	 * @param spos line position to be found.
	 * @return index of position in the array of positions or return -1.
	 */
	int findLinePosition(SourcePos sp) {
		for (int i = 0; i < _positions.length; i++) {
			if (_positions[i] != null && _positions[i].equals(sp)) return i;
		}
		return -1;
	}

	/** Add new line position to array of positions (only if not exists there).
	 * @param spos line position to be added.
	 * @return true if new position was added and return false if position
	 * already exists in the array of positions.
	 */
	boolean addLinePosition(long line, long column, String sourceID) {
		return addLinePosition(new SourcePos(line, column, sourceID));
	}

	/** Add line position to array of positions (only if not exists there).
	 * @param spos line position to be added.
	 * @return true if new position was removed and return false if position
	 * not exists in
	 */
	boolean addLinePosition(SourcePos spos) {
		if (_positions.length == 0) {
			_positions = new SourcePos[1];
			_positions[0] = spos;
		} else {
			if (findLinePosition(spos) < 0) return false;
			SourcePos[] x = _positions;
			_positions = new SourcePos[x.length + 1];
			System.arraycopy(x, 0, _positions, 0, x.length);
			_positions[x.length] = spos;
		}
		return true;
	}

	/** Remove line position from array of positions.
	 * @param spos line position to be removed.
	 */
	boolean removeLinePosition(SourcePos spos) {
		int i = findLinePosition(spos);
		if (i < 0) {
			return false;
		}
		SourcePos[] x = _positions;
		_positions = new SourcePos[x.length - 1];
		if (i > 0) {
			System.arraycopy(x, 0, _positions, 0, i);
		}
		if (i < _positions.length) {
			System.arraycopy(x, i + 1, _positions, i, _positions.length - i);
		}
		return true;
	}

	/** Source position container. */
	static class SourcePos implements Comparable<SourcePos> {
		int _pos;
		long _line;
		long _column;
		String _sysId;

		/** Create empty object.*/
		SourcePos() {_pos = -1; _sysId = "";}

		/** Create object with line and column position and sourceID.
		 * @param source the text where we search the line number
		 * and column number.
		 * @param line line number (starting with 1).
		 * @param column column number (starting with 1).
		 * @param sourceID name of source item.
		 */
		SourcePos(long line, long column, String sourceID) {
			_pos = -1;
			_line = line;
			_column = column;
			_sysId = sourceID;
		}

		/** Create object with computed line and column numbers from the
		 * position in source text.
		 * @param source string with source text.
		 * @param pos position to source.
		 * @param sourceID name of source item.
		 */
		SourcePos(String source, int pos, String sourceID) {
			this(source, pos);
			_sysId = sourceID;
		}

		/** Create object with computed line and column numbers from the
		 * position in source text.
		 * @param source string with source text.
		 * @param pos position to source.
		 */
		SourcePos(String source, int pos) {
			_pos = pos;
			int newi = source.indexOf('\n');
			int oldi = -1;
			int line = 1;
			while (newi <= pos) {
				newi = source.indexOf('\n', oldi + 1);
				if (newi < 0 || pos <= newi) break;
				oldi = newi;
				line++;
			}
			_line = line;
			_column = pos - oldi;
		}

		/** Create object and compute position in source text
		 * from line number and column number.
		 * @param source string with source text.
		 * @param line line number (starting with 1);
		 * @param column column number (starting with 1).
		 */
		SourcePos(String source, int line, int column) {
			_line = line;
			_column = column;
			int pos = -1;
			for (int i = 1; i <= line; i++) {
				if (i >= line) {
					_pos = pos + column;
					return;
				}
				pos = source.indexOf('\n', pos + 1);
				if (pos < 0) break;
			}
			_pos = source.length() - 1;
		}

		@Override
		public int hashCode() {
			int result = _sysId == null ? 1 : _sysId.hashCode();
			return  result*5 + (int) _line*3 + (int) _column;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof SourcePos)) return false;
			SourcePos x = (SourcePos) obj;
			if (_sysId == null)
				return x._sysId == null
					&& _line == x._line && _column == x._column;
			return _sysId.equals(x._sysId)
				&& _line == x._line && _column == x._column;
		}

		@Override
		/** Compare this object with another SourcePos.
		 * @param x other SourcePos object.
		 * @return -1 this object is less then x, 0 if it is equal otherwise 1.
		 */
		public int compareTo(SourcePos x) {
			if (_sysId.equals(x._sysId)) {
				if (_pos >= 0 && x._pos >= 0)
					return (_pos == x._pos) ? 0	: (_pos < x._pos) ? -1 : 1;
				if (_line < x._line) return -1;
				if (_line > x._line) return 1;
				if (_column > x._column) return 1;
				return _column == x._column ? 0 : -1;
			}
			return _sysId.compareTo(x._sysId);
		}

		@Override
		public String toString() {return "line: "+_line+", column: "+_column;}
	}
}