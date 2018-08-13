/*
 * File: ChkGUIDisplay.java
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

import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/** Provides GUI for editing of sources of X-definitions. */
class ChkGUIDisplay extends ChkGUIBase implements XDGUI {
	/** Limit of undo events in text area. */
	private static final int UNDO_LIMIT = 256;
	/** UNDO manager of source window. */
	private UndoManager _undo = new UndoManager();
	/** Menu item select source.*/
	private JMenu _selectSource, _removeSource;
	/** If GUI action is finished. */
	private boolean _actionFinished = true;

	/** Create instance of GUI to display sources of X-definitions. */
	ChkGUIDisplay() {
		openGUI();
		_frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {windowClosed(e);}
			@Override
			public void windowClosed(WindowEvent e) {notifyFrame();}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		});
	}

	@Override
	/** Open the GUI.
	 * @param xp XDPool.
	 * @param err error reporter.
	 * @return if true the GUI was finished else recompile is supposed.
	 */
	public final boolean setGUI(final XDPool xp, final ArrayReporter err) {
		_xdpool = xp;
		_sources = xp.getXDSourcesMap();
		if (_sourceItem == null) { // first
			initSourceMap();
			initSourceWindow();
			_sourceItem = null;
			_undo.setLimit(UNDO_LIMIT);
			initInfoArea(err);
			initMenuBar();
			_frame.setSize(_width, _height);
			_frame.pack();
			setInitialSource();
			_frame.setVisible(true);
		} else {
//			_frame.setSize(_width, _height);
//			_frame.pack();
			initInfoArea(err);
			setLineNumberArea();
		}
		waitFrame();
		if (!_actionFinished) {
			_actionFinished = true; // value MUST be true for next action!
			return false;
		}
//		closeGUI("XDPool was compiled"); // show message that SDPool compiled.
		closeGUI(null);
		_undo = null;
		_selectSource = _removeSource = null;
		_positions = null;
		_actionFinished = true;
		return true;
	}

////////////////////////////////////////////////////////////////////////////////

	/** Find which source will be the initial. */
	private void setInitialSource() {
		if (_sourceID == null) {
			String key = null;
			if (_sourceID != null
				&& _sources.containsKey(_sourceID)) {
				key = _sourceID;
				_sourceID = null;
			} else {
				for (String x: _sources.keySet()) {
					if (_sources.get(x)._active) {
						key = x;
						break;
					}
				}
				if (key == null && _positions != null) {
					// set the first one not null source item with errors
					for (SourcePos spos: _positions) {
						if (spos._sysId != null) {
							key = spos._sysId;
							break;
						}
					}
				}
			}
			if (key != null) {
				setSource(key);
			} else  {
				setSource(_sources.keySet().iterator().next());
			}
			_sourceArea.setCaretPosition(
				_sourceItem._pos >= 0 ? _sourceItem._pos : 0);
			_sourceArea.requestFocus();
		}
	}

	private void initSourceWindow() {
		_lineNumberArea.setEditable(false);
		_sourceArea.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent de) {
				if (_sourceItem != null) {
					_sourceItem._changed = true;
				}
			}
			@Override
			public void insertUpdate(DocumentEvent de) {
				if (_sourceItem != null) {
					_sourceItem._changed = true;
				}
//				setLinesText();
			}
			@Override
			public void removeUpdate(DocumentEvent de) {
				if (_sourceItem != null) {
					_sourceItem._changed = true;
				}
//				setLinesText();
			}
		});
		_sourceArea.getDocument().addUndoableEditListener(
			new UndoableEditListener() {
			@Override
			public void undoableEditHappened(UndoableEditEvent evt) {
				_undo.addEdit(evt.getEdit());
				_undo.setLimit(_undo.getLimit() + 1);
			}
		});
		// Create an undo action and add it to the text component
		_sourceArea.getActionMap().put("Undo", new AbstractAction("Undo") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					int i = _undo.getLimit() - 1;
					if (_undo.canUndo() && i > UNDO_LIMIT) {
						_undo.undo();
						_undo.setLimit(i);
					}
				} catch (CannotUndoException e) {}
			}
		});
		// Create an redo action and add it to the text component
		_sourceArea.getActionMap().put("Redo", new AbstractAction("Redo") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					if (_undo.canRedo()) {
						_undo.setLimit(_undo.getLimit() + 1);
						_undo.redo();
					}
				} catch (CannotUndoException e) {}
			}
		});
		// Create action compile the text component
		_sourceArea.getActionMap().put("Compile", new AbstractAction("Compile"){
			@Override
			public void actionPerformed(ActionEvent evt) {
				updateSourceItem();
				_actionFinished = false;
				notifyFrame();
			}
		});
		// Create action Exit the text component
		_sourceArea.getActionMap().put("Save", new AbstractAction("Save") {
			@Override
			public void actionPerformed(ActionEvent evt) {
				updateSourceItem();
				if (_sourceItem!=null
					&& _sourceItem._changed&&_sourceItem._url==null) {
					_sourceItem._source = _sourceArea.getText();
					_sourceItem._saved = true;
				}
				saveSource(_sourceItem);
			}
		});
		// Bind the undo actions
		_sourceArea.getInputMap().put(
			KeyStroke.getKeyStroke("control Z"), "Undo");
		_sourceArea.getInputMap().put(
			KeyStroke.getKeyStroke("control Y"), "Redo");
		_sourceArea.getInputMap().put(
			KeyStroke.getKeyStroke("F9"), "Compile");
		_sourceArea.getInputMap().put(
			KeyStroke.getKeyStroke("control S"), "Save");
	}

	/** Prepare info area window.
	 * @param err Array reporter with error messages.
	 */
	private void initInfoArea(ArrayReporter err) {
		int errRows = 0;
		_infoArea.setVisible(false);
		// remove all CaretListeners
		for (CaretListener x: _infoArea.getCaretListeners()) {
			_infoArea.removeCaretListener(x);
		}
		if (err != null && err.size() > 0) { //error reporter is not empty
			_positions = new SourcePos[err.size()];
			err.reset();
			int maxlen = 0;
			Report report;
			StringBuilder errors = new StringBuilder();
			while ((report = err.getReport()) != null) {
				SourcePos spos = new SourcePos();
				_positions[errRows] = spos;
				String s = report.getModification();
				if (s != null) {
					int i,j;
					i = s.indexOf("&{line}");
					if (i >=0) {
						i += 7;
						j = s.indexOf("&{", i);
						spos._line = (j > 0) ?
							Integer.parseInt(s.substring(i, j)) :
							Integer.parseInt(s.substring(i));
					} else {
						spos._line = 1;
					}
					i = s.indexOf("&{column}");
					if (i >=0) {
						i += 9;
						j = s.indexOf("&{", i);
						spos._column = (j > 0) ?
							Integer.parseInt(s.substring(i, j)) :
							Integer.parseInt(s.substring(i));
					} else {
						spos._column = 1;
					}
					i = s.indexOf("&{sysId}");
					if (i >=0) {
						i += 8;
						j = s.indexOf("&{", i);
						spos._sysId = (j > 0) ?
							s.substring(i, j) : s.substring(i);
					} else {
						if ("XDEF903".equals(report.getMsgID())) {
							i = s.indexOf("&{0}");
							if (i >=0) {
								i += 4;
								j = s.indexOf("&{", i);
								spos._sysId =
									(j > 0) ? s.substring(i,j) : s.substring(i);
							}
						}
					}
				}
				if (errRows > 0) {
					errors.append('\n');
				}
				spos._pos = errors.length();
				errRows++;
				s = report.toString().trim();
				errors.append(s);
				if (s.length() > maxlen) {
					maxlen = s.length();
				}
			}
			err.reset();
			if (errRows > 5) { //we display 8 lines of errors
				errRows = 5;
			}
			if (maxlen >= 110) { //too long line
				errRows--; // add line for horizontal scroll bar
			}

			if (errRows == 1) {
				errRows = 2;
			}
			_infoArea.setText(errors.toString());
			_infoArea.addCaretListener(new CaretListener(){
				@Override
				public void caretUpdate(CaretEvent e) {
					int pos = e.getDot();
					int index = -1;
					for (int i = 1; i < _positions.length; i++) {
						if (_positions[i]._pos > pos) {
							index = i - 1;
							break;
						}
					}
					if (index == -1) {
						index = _positions.length - 1;
					}
					setSource(index);
					SourcePos sp = _positions[index];
					setCaretPositionToSource(sp);
				}});
		} else {
			_positions = new SourcePos[0];
			_infoArea.setText("No errors found");
			errRows = 1;
		}
		_infoArea.setRows(errRows);
		_infoArea.setVisible(true);
		_frame.pack();
	}

	private void initMenuBar() {
		JMenu fileMenu = _menuBar.add(new JMenu("File (F10)"));
		JMenuItem ji;

		// Select Source menu item
		_selectSource = new JMenu("Select Source...");
		fileMenu.add(_selectSource);
		fileMenu.addSeparator();

		// Remove Source menu item
		_removeSource = new JMenu("Remove Source...");
		fileMenu.add(_removeSource);
		fileMenu.addSeparator();
		prepareSourceMenuItems();

		// Add source menu item
		ji = new JMenuItem("Add Source...");
		ji.setMnemonic((int) 'A');
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSourceItem();
				JFileChooser jf = new JFileChooser();
				jf.setDialogTitle("Add source...");
				int retval = jf.showDialog(_frame, "Open");
				jf.setEnabled(false);
				if (retval == JFileChooser.APPROVE_OPTION) {
					File f = jf.getSelectedFile();
					if (f != null && f.exists()) {
						for (XDSourceItem src: _sources.values()) {
							try {
							if (src._url != null
								&& src._url.equals(f.toURI().toURL())) {
								_actionFinished = false;
								notifyFrame();
								break;
							}
							} catch (Exception ex) {}
						}
						try {
							XDSourceItem src = new XDSourceItem(f);
							String key = f.getAbsolutePath();
							_sources.put(key, src);
							initSourceItem(key, src);
							if (_sourceItem != null) {
								_sourceItem._pos =
									_sourceArea.getCaret().getDot();
								_sourceItem._active = false;
							}
							src._pos = 0;
							src._active = true;
							prepareSourceMenuItems();
							setSource(key);
						} catch (Exception ex) {
							ex.printStackTrace(System.err);
						}
					}
				}
				_actionFinished = false;
				notifyFrame();
			}
		});
		fileMenu.add(ji);
		fileMenu.addSeparator();

		// Save as menu item
		ji = new JMenuItem("Save as...");
		ji.setAccelerator(KeyStroke.getKeyStroke("control S"));
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSourceItem();
				if (_sourceItem!=null
					&& _sourceItem._changed&&_sourceItem._url == null) {
					_sourceItem._source = _sourceArea.getText();
					_sourceItem._saved = true;
				}
				saveSource(_sourceItem);
			}
		});
		fileMenu.add(ji);
		fileMenu.addSeparator();

		// Compile menu item
		ji = new JMenuItem("Compile");
		ji.setAccelerator(KeyStroke.getKeyStroke("F9"));
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (((JMenuItem)e.getSource()).isEnabled()) {
					updateSourceItem();
					_actionFinished = false;
					notifyFrame();
				}
			}
		});
		fileMenu.add(ji);
		fileMenu.addSeparator();

		// Exit menu item
		ji = new JMenuItem("Exit");
		ji.setMnemonic((int) 'X');
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_actionFinished = true;
				notifyFrame();
			}
		});
		fileMenu.add(ji);

		// Source position info
		_menuBar.add(_sourcePositionInfo, BorderLayout.EAST);
		_frame.setJMenuBar(_menuBar);
		_sourceArea.setCaretPosition(0);
	}

	/** Prepare menu items connected with more sources. */
	private void prepareSourceMenuItems() {
		if (_sources != null && _sources.size() > 1) {
			// Select source item
			_selectSource.setMnemonic((int) 'S');
			ActionListener alistener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem jc = (JMenuItem) e.getSource();
					setSource(jc.getText());
				}
			};
			for (String key: _sources.keySet()) {
				JMenuItem ji = new JMenuItem(key);
				ji.addActionListener(alistener);
				_selectSource.add(ji);
			}
			_selectSource.setEnabled(true);
			_removeSource.setMnemonic((int) 'R');
			alistener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem jc = (JMenuItem) e.getSource();
					XDSourceItem item = _sources.remove(jc.getText());
					updateSourceItem();
					prepareSourceMenuItems();
					if (item == _sourceItem) {
						_sourceItem = null;
						_sourceID = null;
						setInitialSource();
					}
					_actionFinished = false;
					notifyFrame();
				}
			};
			for (String key: _sources.keySet()) {
				JMenuItem ji = new JMenuItem(key);
				ji.addActionListener(alistener);
				_removeSource.add(ji);
			}
			_removeSource.setEnabled(true);
		} else {
			_selectSource.setEnabled(false);
			_selectSource.removeAll();
			_removeSource.setEnabled(false);
			_removeSource.removeAll();
		}
	}

	/** Update actual source item. */
	private boolean updateSourceItem() {
		String s;
		if (_sourceArea == null || _sourceItem == null ||
			(s = _sourceArea.getText()) == null || s.equals(_sourceItem._source)) {
			return false;
		}
		_sourceItem._pos = _sourceArea.getCaretPosition();
		_sourceItem._changed = true;
		_sourceItem._source = s;
		_sourceItem._pos = _sourceArea.getCaretPosition();
		return true;
	}

	/** Set source area (from error information positions array).
	 * @param index the index of _sourceNames array.
	 */
	private void setSource(int index) {
		setSource(_positions[index]._sysId);
	}

	/** Set source area from sourceID name.
	 * @param sourceID name of source item.
	 */
	private void setSource(String sourceID) {
		if (sourceID == null) {
			_sourceID = null;
			_frame.setTitle("UNKNOWN SOURCE");
			_sourceArea.setText(null);
			_sourceArea.setEditable(false);
			_undo.discardAllEdits();
			return;
		}
		XDSourceItem newSrc = _sources.get(sourceID);
		if (!sourceID.equals(_sourceID) || newSrc == null ||
			newSrc._source == null || newSrc._source.length() == 0) {
			if (updateSourceItem() && _sourceItem._url != null
				&& "file".equals(_sourceItem._url.getProtocol())) {
				saveSource(_sourceItem);
			}
			_sourceID = sourceID;
			if (newSrc == null) {
				try {
					URL u = new URL(sourceID);
					newSrc = _sources.get(u.toExternalForm());
					if (newSrc == null) {
						File f = new File(u.getFile());
						newSrc = _sources.get(
							f.getAbsolutePath().replace('\\','/'));
						if (newSrc == null) {
							return;
						}
					}
				} catch (Exception ex) {return;}
			}
			if (_sourceItem != null) {
				_sourceItem._pos = _sourceArea.getCaret().getDot();
				_sourceItem._active = false;
			}
			_frame.setTitle(sourceID);
			_undo.discardAllEdits();
			_undo.setLimit(UNDO_LIMIT);
			_sourceArea.setText(newSrc._source);
			_sourceArea.setEditable(true);
			setLineNumberArea();
			_sourceArea.setCaretPosition(newSrc._pos >= 0 ? newSrc._pos : 0);
			_sourceArea.requestFocus();
			newSrc._active = true;
		}
		_sourceItem = newSrc;
	}

	/** Save source text from XDefSourceItem to a file.
	 * @param src XDefSourceItem object.
	 */
	private void saveSource(XDSourceItem src) {
		updateSourceItem();
		JFileChooser jf = new JFileChooser();
		if (src._url != null && "file".equals(src._url.getProtocol())) {
			jf.setSelectedFile(new File(src._url.getFile()));
		}
		jf.setDialogTitle("Save to file");
		jf.setToolTipText("Save content of the active window to a file");
		int retval = jf.showSaveDialog(_frame);
		jf.setEnabled(false);
		if (retval == JFileChooser.APPROVE_OPTION) {
			try {
				File f = jf.getSelectedFile();
				SUtils.writeString(f, src._source, src._encoding);
				src._saved = true;
				src._url = f.toURI().toURL();
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
			}
		}
	}
}