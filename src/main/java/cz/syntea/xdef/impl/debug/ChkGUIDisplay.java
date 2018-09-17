package cz.syntea.xdef.impl.debug;

import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.impl.XDSourceItem;
import cz.syntea.xdef.sys.ArrayReporter;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/** Provides GUI for editing of sources of X-definitions.
 * @author Vaclav Trojan
 */
public class ChkGUIDisplay extends GUIScreen implements XEditor {

	/** Create the instance of GUI to display sources of X-definitions.
	 * @param x X position of the left corner of the screen.
	 * @param y Y position of the left corner of the screen.
	 * @param width width of the screen.
	 * @param height height of the screen.
	 */
	public ChkGUIDisplay(int x, int y, int width, int height) {
		super(x, y, width, height);}

	@Override
	/** Open GUI.
	 * @param xp the XDPool object.
	 * @param err error reporter or null.
	 * @return true if the GUI was finished ane not continue. If false
	 * is returned then the compilation will be executed and the editor
	 * will be opened again.
	 */
	public final boolean setXEditor(final XDPool xp, final ArrayReporter err) {
		_xdpool = xp;
		_sources = xp.getXDSourcesMap();
		if (_sourceItem == null) { // first
			initSourceMap();
			initSourceWindow();
			_sourceItem = null;
			_undo.setLimit(UNDO_LIMIT);
			initInfoArea(err);
			initMenuBar();
			setInitialSource();
			_frame.pack();
			_frame.setBounds(_xpos, _ypos, _width, _height);
			_frame.setVisible(true);
			_frame.addComponentListener(new java.awt.event.ComponentAdapter() {
				@Override
				public void componentResized(java.awt.event.ComponentEvent evt){
					Rectangle r = _frame.getBounds();
					_xpos = r.x;
					_ypos = r.y;
					_width = r.width;
					_height = r.height;
				}
			});
		} else {
//			_frame.setSize(_width, _height);
//			_frame.pack();
			initInfoArea(err);
			setLineNumberArea();
			_frame.setBounds(_xpos, _ypos, _width, _height);
		}
		waitFrame();
		if (!_actionFinished) {
			_actionFinished = true; // value MUST be true for next action!
			return false;
		}
		closeXEditor(null);
		return true;
	}

	@Override
	/** Close XEditor.
	 * @param msg text of message to be shown. If null no message is shown.
	 */
	public void closeXEditor(String msg) {closeEdit(msg);}

////////////////////////////////////////////////////////////////////////////////


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

}