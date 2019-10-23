package org.xdef.impl.debug;

import org.xdef.impl.XDSourceInfo;
import org.xdef.impl.XDSourceItem;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SUtils;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/** Provides screen (swing Frame) for editing of XML sources.
 * @author Vaclav Trojan
 */
public class GUIScreen extends GUIBase {
	/** Limit of undo events in text area. */
	public static final int UNDO_LIMIT = 256;
	/** UNDO manager of source window. */
	public UndoManager _undo = new UndoManager();
	/** Menu item select source.*/
	public JMenu _selectSource, _removeSource;
	/** If GUI action is finished. */
	public boolean _actionFinished = true;

	/** Create instance of the screen to display the sources of X-definitions.
	 * @param si source info.
	 */
	public GUIScreen(final XDSourceInfo si) {
		openGUI(si);
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

	/** Find which source will be the initial. */
	public void setInitialSource() {
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
	/** Prepare info area window.
	 * @param err Array reporter with error messages.
	 */
	public final void initInfoArea(final ArrayReporter err) {
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
				errRows++; // add line for horizontal scroll bar
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
			_infoArea.setText("No errors");
			errRows = 1;
		}
		_infoArea.setRows(errRows);
		_infoArea.setVisible(true);
	}

	public void initSourceWindow() {
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
			}
			@Override
			public void removeUpdate(DocumentEvent de) {
				if (_sourceItem != null) {
					_sourceItem._changed = true;
				}
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
			private static final long serialVersionUID = 4377386270269629176L;
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
			private static final long serialVersionUID = 4377386270269629176L;
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
			private static final long serialVersionUID = 4377386270269629176L;
			@Override
			public void actionPerformed(ActionEvent evt) {
				updateSourceItem();
				_actionFinished = false;
				notifyFrame();
			}
		});
		// Create action Exit the text component
		_sourceArea.getActionMap().put("Save", new AbstractAction("Save") {
			private static final long serialVersionUID = 4377386270269629176L;
			@Override
			public void actionPerformed(ActionEvent evt) {
				updateSourceItem();
				if (_sourceItem!=null
					&& _sourceItem._changed && _sourceItem._url==null) {
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

	/** Set source area (from error information positions array).
	 * @param index the index of _sourceNames array.
	 */
	public final void setSource(int index) {
		setSource(_positions[index]._sysId);
	}

	/** Set source area from sourceID name.
	 * @param sourceID name of source item.
	 */
	public final void setSource(String sourceID) {
		if (sourceID == null || sourceID.isEmpty()) {
			_sourceID = null;
			_frame.setTitle((_windowName != null ? _windowName + " " : "")
				+ "UNKNOWN SOURCE");
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
					URL u = SUtils.getExtendedURL(sourceID);
					newSrc = _sources.get(u.toExternalForm());
					if (newSrc == null) {
						File f = new File(u.getFile());
						newSrc = _sources.get(
							f.getCanonicalPath().replace('\\','/'));
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
			_frame.setTitle((_windowName != null
				? _windowName + " " : "") + sourceID);
			_undo.discardAllEdits();
			_undo.setLimit(UNDO_LIMIT);
			_sourceArea.setText(newSrc._source);
			setLineNumberArea();
			_sourceArea.setCaretPosition(newSrc._pos >= 0 ? newSrc._pos : 0);
			_sourceArea.requestFocus();
			_undo.die();
			_sourceArea.setEditable(true);
			newSrc._active = true;
		}
		_sourceItem = newSrc;
	}

	/** Save source text from XDSourceItem to a file.
	 * @param src XDSourceItem object.
	 */
	public final void saveSource(final XDSourceItem src) {
		updateSourceItem();
		JFileChooser jf = new JFileChooser();
		if (src._url != null && "file".equals(src._url.getProtocol())) {
			jf.setSelectedFile(new File(src._url.getFile()));
		} else {
			jf.setSelectedFile(new File(".").getAbsoluteFile());
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

	/** Close GUI: dispose window and remove allocated objects. */
	public void closeEdit() {
		closeGUI();
		_undo = null;
		_selectSource = _removeSource = null;
		_positions = null;
		_actionFinished = true;
	}

}