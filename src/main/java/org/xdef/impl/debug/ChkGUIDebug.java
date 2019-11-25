package org.xdef.impl.debug;

import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.SPosition;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDCallItem;
import org.xdef.XDConstants;
import org.xdef.XDDebug;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.XDSourceInfo;
import org.xdef.impl.XDSourceItem;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.CodeTable;
import org.xdef.model.XMDebugInfo;
import org.xdef.model.XMStatementInfo;
import org.xdef.model.XMVariable;
import org.xdef.proc.XXData;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Caret;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import org.xdef.impl.ChkElement;
import org.xdef.impl.XElement;
import org.xdef.impl.XVariableTable;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SManager;
import org.xdef.sys.SUtils;

/** Provides debugging tool for X-definition.
 * @author Vaclav Trojan
 */
public class ChkGUIDebug extends GUIBase implements XDDebug {

	private boolean _debugMode = true;
	private boolean _opened = false;

////////////////////////////////////////////////////////////////////////////////
// debug mode internal commands
////////////////////////////////////////////////////////////////////////////////
	private static final int DBG_CONTINUE = 0;							//0
	private static final int DBG_STEP = DBG_CONTINUE + 1;				//1
	private static final int DBG_STEPINTO = DBG_STEP + 1;				//2
	private static final int DBG_CANCEL = DBG_STEPINTO + 1;				//3
	private static final int DBG_DISABLLE = DBG_CANCEL + 1;				//4
	private static final int DBG_SHOWSTACK = DBG_DISABLLE  + 1;			//5
	private static final int DBG_SHOWLOCALVARS = DBG_SHOWSTACK + 1;		//6
	private static final int DBG_SHOWGLOBALVARS = DBG_SHOWLOCALVARS + 1;//7
	private static final int DBG_SHOWPOSITION = DBG_SHOWGLOBALVARS + 1;	//8
	private static final int DBG_SHOWTEXT = DBG_SHOWPOSITION + 1;		//9
	private static final int DBG_SETTEXT = DBG_SHOWTEXT + 1;			//10
	private static final int DBG_SHOWELEMENT = DBG_SETTEXT + 1;			//11
	private static final int DBG_SHOWCONTEXT = DBG_SHOWELEMENT + 1;		//12
	private static final int DBG_SHOWERRORS = DBG_SHOWCONTEXT + 1;		//13
	private static final int DBG_EXIT = DBG_SHOWERRORS + 1;				//14
	private static final int DBG_HELP = DBG_EXIT + 1;					//15

	private static final String[] DBG_COMMANDS = {
		"go/Continue/F5",						// DBG_CONTINUE
		"step/Step over/F8",					// DBG_STEP
		"stepInto/Step into/F7",				// DBG_STEPINTO
		"cancel/Cancel X-definition",			// DBG_CANCEL
		"disable/Disable this stop address",	// DBG_DISABLE
		"ps/Show stack/F2",						// SHOWSTACK
		"pl/Show local variables/F3",			// DBG_SHOWLOCALVARS
		"pg/Show global variables/F4",			// DBG_SHOWGLOBALVARS
		"pos/Show position",					// DBG_SHOWPOSITION
		"text/Show text value/F9",				// DBG_SHOWTEXT
		"setText/Set text value",				// DBG_SETTEXT
		"elem/Show actual element",				// DBG_SHOWELEMENT
		"context/Show context",					// DBG_SHOWCONTEXT
		"err/Show errors",						// DBG_SHOWERRORS
		"exit/Exit debugger",					// DBG_SHOWERRORS
		"help/Help/F1",							// DBG_HELP
	};

	/** Create instance for debug GUI.
	 * @param props properties used for initialization.
	 * @param xp XDPool object
	 */
	public ChkGUIDebug(final Properties props, final XDPool xp) {
		_xdpool = xp;
		_si = new XDSourceInfo();
		_si.copyFrom(xp.getXDSourceInfo());
		_sources = _si.getMap();
		_windowName = "Debug X-definition:";
		init(props);
	}

	/** Get command ID.
	 * @param cmd String with command.
	 * @return command ID.
	 */
	private static int decodeCommand(String cmd) {
		int result = -1;
		for (int i = 0; i < DBG_COMMANDS.length; i++) {
			String s = DBG_COMMANDS[i];
			if (s.startsWith(cmd + "/") ||
				s.indexOf("/" + cmd) > 0) {
				result = i;
				break;
			}
		}
		return result;
	}

	synchronized int getCommand() {
		if (_in == null) {
			waitFrame();
		}
		int result = _debugCommand;
		_debugCommand = -1;
		return result;
	}

	private static final class StopAddr implements Comparable<StopAddr> {
		int _stopAddr;
		int _startPos;
		int _endPos;
		boolean _active;
		String _sourceID;

		StopAddr(final int addr) {
			_stopAddr = addr;
			_startPos = -1;
			_endPos = -1;
			_sourceID = null;
		}

		StopAddr(final int addr,
			final int startPos,
			final int endPos,
			final String sourceId) {
			_stopAddr = addr;
			_startPos = startPos;
			_endPos = endPos;
			_sourceID = sourceId;
		}
		@Override
		public int compareTo(final StopAddr x) {
			if (x._stopAddr == _stopAddr) {
				return 0;
			}
			if (_startPos == x._startPos) {
				return _stopAddr < x._stopAddr ? -1 : 1;
			}
			return _startPos < x._startPos ? -1 : 1;
		}

		@Override
		public int hashCode() {return _stopAddr;}

		@Override
		public boolean equals(Object o) {
			if (o instanceof StopAddr) {
				return _stopAddr == ((StopAddr) o)._stopAddr;
			}
			return  false;
		}

		@Override
		public String toString() {
			return "stopAddr=" + _stopAddr + ", sourceID=" + _sourceID;
		}
	}

	// debugger command
	private int _debugCommand= -1;
	private StopAddr[] _stopAddresses = new StopAddr[0]; // Stop addresses.
	private String[] _xposItems = null; // List of stop addresses.
	private InputStream _in; // input in command line mode
	private PrintStream _out; // output in command line mode

	/** Initialize this debug GUI object.
	 * @param xp XDPool of running X-definition process.
	 */
	private void initGUI(final XDPool xp) {
		// Initialize SWING objects
		_xdpool = xp;
		_opened = true;
		if (_out != null || _in != null) { // command line mode
			if (_in == null) {
				_in = System.in;
			}
			if (_out == null) {
				_out = System.out;
			}
			return;
		}
		openGUI(xp.getXDSourceInfo()); // GUI mode
		_sourceArea.setEditable(false);
		_frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {windowClosed(e);}
			@Override
			public void windowClosed(WindowEvent e) {
				_debugMode = false;
			}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		});
		initFrame();
	}

	/** Initialize the main frame used in GUI. */
	private void initFrame() {
		initSourceMap();
		_sourceItem = null;
		_frame.addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {windowClosed(e);}
			@Override
			public synchronized void windowClosed(WindowEvent e) {
				_debugCommand = DBG_EXIT;
				if (_sourceItem != null) {
					_sourceItem._pos = _sourceArea.getCaretPosition();
				}
				notifyFrame();
			}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}
		});
		initSourceWindow();
		initInfoArea();
		initMenuBar();
		String key = null;
		if (_sourceID != null && _sources.containsKey(_sourceID)) {
			key = _sourceID;
			_sourceID = null;
		} else {
			for (String x: _sources.keySet()) {
				if (_sources.get(x)._active) {
					key = x;
					break;
				}
			}
		}
		if (key != null) {
			setSource(key);
		} else  {
			if (_sources != null && !_sources.isEmpty()) {
				setSource(_sources.keySet().iterator().next());
			}
		}
		_frame.pack();
		_frame.setLocationByPlatform(true);
		_frame.setBounds(_si._xpos, _si._ypos, _si._width, _si._height);
		_sourceArea.setCaretPosition(
			_sourceItem._pos>=0 ? _sourceItem._pos : 0);
		_sourceArea.requestFocus();
		_frame.setVisible(true);
		_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

	private StopAddr createStopAddr(SourcePos spos) {
		XMDebugInfo di = _xdpool.getDebugInfo();
		XMStatementInfo si = di.getStatementInfo(spos._line,
			spos._column, spos._sysId, null);
		if (si == null || si.getAddr() < 0) {
			return null;
		}
		return createStopAddr(di, si);
	}

	private StopAddr createStopAddr(final XMDebugInfo di,
		final XMStatementInfo xsi) {
		if (xsi == null) {
			return null;
		}
		XMStatementInfo si = xsi;
		XDSourceItem xi = _sources.get(si.getSysId());
		String txt;
		if (xi == null || (txt = xi._source) == null || si.getAddr() < 0) {
			return null;
		}
		SourcePos spos = new SourcePos(txt,
			(int) si.getLine(),	(int) si.getColumn());
		spos._sysId = si.getSysId();
		int pos = spos._pos;
		if (si.getEndLine() >= 0) {
			SourcePos spos1 = new SourcePos(txt,
				(int) si.getEndLine(), (int) si.getEndColumn());
			if (spos1._pos >= pos) {
				return new StopAddr(si.getAddr(),
					pos, spos1._pos, spos._sysId);
			}
		}
		XMStatementInfo nextsi = di.nextStatementInfo(si);
		int endPos;
		if (nextsi != null && spos._sysId.equals(nextsi.getSysId())
			&& nextsi.getLine() == spos._line) {
			long col = nextsi.getColumn() < 0 ? // macro ?
				(spos._column -  nextsi.getColumn()) : nextsi.getColumn();
			SourcePos sp = new SourcePos(txt,
				(int) nextsi.getLine(), (int) col);
			endPos = sp._pos > pos ? sp._pos : pos + 1;
		} else {
			endPos = pos + 1;
		}
		return new StopAddr(si.getAddr(), pos, endPos, spos._sysId);
	}

	/** Mark breakpoints in source area. */
	private void markStopAddresses() {
		try {
			int pos = _sourceArea.getCaretPosition();
			_sourceArea.setVisible(false);
			String s = _sourceItem._source;
			StyledDocument sdoc = _sourceArea.getStyledDocument();
			_sourceArea.setText(null);
			int p = 0;
			for (StopAddr x: _stopAddresses) {
				if (x._sourceID.equals(_sourceID)) {
					if (x._startPos > p) {
						sdoc.insertString(
							p, s.substring(p, x._startPos), STYLE_WHITE);
						p = x._startPos;
					}
					if (x._endPos > p) {
						Style style;
						if (x._active) {
							style = STYLE_ERROR;
							pos = p;
						} else {
							style = STYLE_BREAKPOINT;
						}
						sdoc.insertString(p, s.substring(p, x._endPos), style);
						p = x._endPos;
					}
				}
			}
			if (p < s.length()) {
				sdoc.insertString(p, s.substring(p), STYLE_WHITE);
			}
			_sourceArea.setStyledDocument(sdoc);
			_sourceArea.setCaretPosition(pos);
			_sourceArea.setFocusable(true);
			_sourceArea.setVisible(true);
			_frame.repaint();
		} catch (Exception ex) {}
	}

	/** Initialize window for source code of X-definitions. */
	private void initSourceWindow() {
		_sourceArea.addMouseListener(new MouseListener() {
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					int pos = _sourceArea.getCaretPosition();
					if (pos == 0) {
						return;
					}
					String txt = _sourceArea.getText();
					SourcePos spos = new SourcePos(txt, pos, _sourceID);
					StopAddr sa = createStopAddr(spos);
					if (sa != null) {
						XDSourceItem xi = _sources.get(sa._sourceID);
						if (xi == null || !_sourceID.equals(_sourceID)) {
							return;
						}
						spos = new SourcePos(txt, sa._startPos);
						int len = _stopAddresses.length;
						setStopAddr(sa);
						markStopAddresses();
						String s = "line:" + spos._line
							+ ", column:" + spos._column;
						if (_stopAddresses.length > len) {
							Object[] options = {"Set stop address", "Ignore"};
							int n = JOptionPane.showOptionDialog(_sourceArea,
								"Set stop address " + s, // window text
								"Set stop address", // window title
								JOptionPane.YES_NO_CANCEL_OPTION, // option type
								JOptionPane.QUESTION_MESSAGE, //message type
								null, // icon
								options, // options
								options[1]); //initial value
							if (n == 1) { // ingnore
								removeStopAddr(sa._stopAddr);
								markStopAddresses();
							}
						} else {
							Object[] options = {"Remove stop address","Ignore"};
							int n = JOptionPane.showOptionDialog(_sourceArea,
								"Remove stop address " + s, // window text
								"Remove stop address", // window title
								JOptionPane.YES_NO_CANCEL_OPTION, // option type
								JOptionPane.QUESTION_MESSAGE, //message type
								null, // icon
								options, // options
								options[1]); //initial value
							if (n != 1) { // remove stop adress
								removeStopAddr(sa._stopAddr);
								markStopAddresses();
							}
						}
					} else {
						JOptionPane.showMessageDialog(_frame,
							"No stop address available here");
					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});
		_sourceArea.requestFocus();
	}

	/** Prepare info area window (errors, debug information). */
	private void initInfoArea() {
		_infoArea.setFont(FONT_TEXT);
		_infoArea.setEditable(false);
		_infoArea.setFocusable(false);
		_infoArea.setBackground(new Color(245, 245, 245));
		_positions = new SourcePos[0];
		_infoArea.setRows(8);
	}

	/** Prepare menu bar in main frame. */
	private void initMenuBar() {
		// Create menubar
		JMenu fileMenu = _menuBar.add(new JMenu("File (F10)"));
		JMenuItem ji;
		JMenu jp;

		// Select source item (if more sources then one)
		if (_sources.size() > 1) {
			jp = new JMenu("Select Source...");
			jp.setMnemonic((int) 'S');
			ActionListener ssl = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem jc = (JMenuItem) e.getSource();
					setSource(jc.getText());
				}
			};
			for (String key: _sources.keySet()) {
				ji = new JMenuItem(key);
				ji.addActionListener(ssl);
				jp.add(ji);
			}
			fileMenu.add(jp);
			fileMenu.addSeparator();
		}
		// Debug commands
		jp = new JMenu("Debug commands...");
		jp.setMnemonic((int) 'D');
		ActionListener cmdListener = new ActionListener() {
			@Override
			public synchronized void actionPerformed(ActionEvent e) {
				JMenuItem jc = (JMenuItem) e.getSource();
				_debugCommand = decodeCommand(jc.getText());
				 notifyFrame();
			}
		};
		for (int i = 0; i < DBG_COMMANDS.length; i++) {
			if (i == DBG_STEPINTO) continue;
			String cmd = DBG_COMMANDS[i];
			int ndx = cmd.indexOf('/');
			String s = ndx < 0 ? cmd : cmd.substring(ndx + 1);
			ndx = s.indexOf('/');
			ji = new JMenuItem(ndx >= 0 ? s.substring(0, ndx) : s);
			if (ndx > 0) {
				ji.setAccelerator(
					KeyStroke.getKeyStroke(s.substring(ndx + 1).intern()));
			}
			ji.addActionListener(cmdListener);
			jp.add(ji);
		}
		fileMenu.add(jp);
		fileMenu.addSeparator();

		// Exit
		ji = new JMenuItem("Exit degugger");
		ji.setMnemonic((int) 'X');
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_debugMode = false;
				_frame.dispose();
			}
		});
		fileMenu.add(ji);

		// Source position info
		_menuBar.add(_sourcePositionInfo, BorderLayout.EAST);
		_sourcePositionInfo.setText("");
		_frame.setJMenuBar(_menuBar);
	}

	/** Set source to editor window.
	 * @param sourceID name of source item.
	 */
	private void setSource(String sourceID) {
		XDSourceItem newSrc = _sources.get(sourceID);
		if (!sourceID.equals(_sourceID) || newSrc._source == null
			|| newSrc._source.length() == 0) {
			_sourceID = sourceID;
			if (newSrc == null) {
				try {
					URL u = SUtils.getExtendedURL(sourceID);
					newSrc = _sources.get(u.toExternalForm());
					if (newSrc == null) {
						File f = new File(u.getFile());
						newSrc = _sources.get(
							f.getCanonicalPath().replace('\\','/'));
					}
				} catch (Exception ex) {
					return;
				}
			}
			if (_sourceItem != null) {
				_sourceItem._pos = _sourceArea.getCaretPosition();
				_sourceItem._active = false;
			}
			_frame.setTitle((_windowName != null ? _windowName + " " : "")
				+ sourceID);
			_sourceArea.setVisible(false);
			_sourceArea.setFocusable(false);
			_sourceArea.setText(newSrc._source);
			Caret c = _sourceArea.getCaret();
			c.setSelectionVisible(false);
			c.setDot(newSrc._pos >= 0 ? newSrc._pos : 0);
			c.setSelectionVisible(true);
			_sourceArea.setFocusable(true);
			_sourceArea.setVisible(true);
			_sourceItem = newSrc;
			setLineNumberArea();
			markStopAddresses();
			_sourceItem._active = true;
		}
	}

////////////////////////////////////////////////////////////////////////////////
// XDDebug interface
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** Open debugger.
	 * @param props Properties or null.
	 * @param xp XDPool.
	 */
	public void openDebugger(Properties props, XDPool xp) {
		if (xp == null || !xp.isDebugMode()
			|| xp.getDisplayMode() == XDPool.DISPLAY_FALSE) {
			return;
		}
		init(props);
		if (!_opened) {
			initGUI(xp);
		}
		if (_out == null) {
			initInfoArea();
			_infoArea.setText("Select break points");
			getCommand();
			_infoArea.setText("");
		}
	}

	@Override
	/** Close debugger and display message.
	 * @param msg message to be displayed.
	 */
	public void closeDebugger(String msg) {
		closeDebugger();
		if (msg != null && !msg.isEmpty()) {
			JOptionPane.showMessageDialog(null, msg);
		}
	}

	@Override
	/** Close debugger */
	public void closeDebugger() {
		closeGUI(); // do not show mesage the trogram finished
		_debugMode = false;
		_opened = false;
		_in = null;
		_out = null;
	}

	/** Set defaults according to Properties.
	 * @param props Properties or null.
	 */
	private void init(final Properties props) {
		_in = null;
		_out = null;
		String s = SManager.getProperty(props,XDConstants.XDPROPERTY_DEBUG_OUT);
		if (s != null && !(s = s.trim()).isEmpty()) {
			try {
				_out = new PrintStream(new FileOutputStream(s), true);
			} catch (Exception ex) {
				_out = System.out;
			}
		}
		s = SManager.getProperty(props, XDConstants.XDPROPERTY_DEBUG_IN);
		if (s != null && !(s = s.trim()).isEmpty()) {
			if (_out == null) {
				_out = System.out;
			}
			try {
				_in = new FileInputStream(s);
			} catch (Exception ex) {
				_in = System.in;
			}
		}
		if (_out != null && _in == null) {
			_in = System.in;
		}
	}

	@Override
	/** Get debug PrintStream.
	 * @return debug PrintStream.
	 */
	public final PrintStream getOutDebug() {return _out;}

	@Override
	/** Get debug InputStream.
	 * @return debug InputStream.
	 */
	public InputStream getInDebug() {return _in;}

	@Override
	/** Set debug PrintStream.
	 * @param outDebug debug PrintStream.
	 */
	public void setOutDebug(PrintStream outDebug) {
		_out = outDebug;
		if (_out != null && _in == null) {
			_in = System.in;
		}
		_debugMode = true;
	}

	@Override
	/** Set debug InputStream.
	 * @param inDebug debug InputStream.
	 */
	public void setInDebug(InputStream inDebug) {
		_in = inDebug;
		if (_in != null && _out == null) {
			_out = System.out;
		}
		_debugMode = true;
	}

	/** Display information.
	 * @param s text to be displayed
	 */
	private void display(final String s) {
		if (_out != null) {
			_out.println(s);
			_out.flush();
		} else if (_infoArea != null) {
			String txt = _infoArea.getText();
			if (txt == null || (txt = txt.trim()).length() == 0) {
				txt = s;
			} else {
				txt = txt + "\n" + s;
			}
			_infoArea.setText(txt);
			_infoArea.setCaretPosition(txt.length()-1);
		}
	}

	/** Read line in command line mode.
	 * @return string with command.
	 */
	private String readLine() {
		String s = "";
		int ich;
		try {
			ich = _in.read();
			if (ich > 0) {
				s += (char) ich;
				while ((ich = _in.read()) >= ' ') {
					s += (char) ich;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
		return s;
	}

	@Override
	/** This method is called from x-script on breakpoint or
	 * from methods pause and trace.
	 * @param xnode actual XXNode object.
	 * @param code executive code.
	 * @param pcounter program counter.
	 * @param stackPointer stack pointer.
	 * @param stack stack.
	 * @param localVariables array with local variables.
	 * @param debugInfo XMDebugInfo object.
	 * @param callList call list.
	 * @param stepMode step mode (NOSTEP, STEPINTO, STEPOVER).
	 * @return step mode.
	 * @throws Error if the X-definition is canceled.
	 */
	public int debug(final XXNode xnode,
		final XDValue[] code,
		final int pcounter,
		final int stackPointer,
		final XDValue[] stack,
		final XDValue[] localVariables,
		final XMDebugInfo debugInfo,
		final XDCallItem callList,
		final int stepMode) throws Error {
		if (!_debugMode) {
			return NOSTEP;
		}
		if (xnode == null) {
			return NOSTEP;
		}
		if (!_opened) {
			initGUI(xnode.getXDPool());
		}
		int pc = pcounter;
		XDValue codeItem;
		boolean trace;
		boolean pause;
		XMStatementInfo si = debugInfo==null ? null : debugInfo.getInfo(pc);
		String xpos = xnode.getXPos();
		String txt;
		if (code == null) {
			codeItem = null;
			trace = pause = false;
			txt = "PAUSE " + xpos;
		} else {
			codeItem = code[pcounter];
			trace = codeItem.getCode() == CodeTable.DEBUG_TRACE;
			pause = codeItem.getCode() == CodeTable.DEBUG_PAUSE;
			if (trace || pause && codeItem.getParam() > 0) {
				si = debugInfo.prevStatementInfo(si);
			}
			txt = (trace ? "TRACE " : pause ?	"PAUSE " : codeItem.toString())
				+ xpos + "; pc=" + pcounter
				+ ((trace || pause) && codeItem.getParam() > 0 ?
				"; \"" + stack[stackPointer].toString() + "\"; " : "");
		}
		SourcePos spos;
		String stmtInfo;
		if (si != null) {
			spos = new SourcePos(si.getLine(),si.getColumn(),si.getSysId());
			stmtInfo = spos.toString() + ", xdef: " + si.getXDName() +
				", pc: " + si.getAddr();
		} else {
			SPosition x = xnode.getXMNode().getSPosition();
			spos = new SourcePos(x.getLineNumber(),
				x.getColumnNumber(), x.getSystemId());
			stmtInfo = spos.toString();
		}
		StopAddr sa = null;
		if (_out == null && spos != null) {
			sa = getStopAddr(pc);
			if (sa != null) {
				setSource(sa._sourceID);
				spos = new SourcePos(_sourceItem._source, sa._startPos);
				sa._active = true;
				markStopAddresses();
			} else {
				setSource(spos._sysId);
			}
			_sourcePositionInfo.setText(stmtInfo);
			markStopAddresses();
			setCaretPositionToSource(spos);
		}
		display(txt);
		if (trace) {
			if (_out == null) {
				markStopAddresses();
				getCommand();
				if (sa != null) {
					sa._active = false;
					markStopAddresses();
				}
			}
			return stepMode;
		}
		while (true) {
			int command;
			if (_out != null) {
				_out.println("command ('?' for help): ");
				command = decodeCommand(readLine());
			} else {
				command = getCommand();
			}
			switch (command) {
				case DBG_CANCEL:
					if (_out == null && sa != null) {
						sa._active = false;
						markStopAddresses();
					}
					return KILL;
				case DBG_EXIT: {
					_debugMode = false;
					return NOSTEP;
				}
				case DBG_CONTINUE:
					if (_out == null && sa != null) {
						sa._active = false;
						markStopAddresses();
					}
					return NOSTEP;
				case DBG_STEP:
					if (_out == null && sa != null) {
						sa._active = false;
						markStopAddresses();
					}
					return STEPOVER;
				case DBG_STEPINTO:
					if (_out == null && sa != null) {
						sa._active = false;
						markStopAddresses();
					}
					return STEPINTO;
				case DBG_DISABLLE:
					if (pause) {
						if (codeItem != null) {
							short codeId = codeItem.getParam() == 0 ?
								CodeTable.NO_OP
								: CodeTable.POP_OP;
							codeItem.setCode(codeId);
						}
						display("Nothing to disable");
						continue;
					} else {
						removeStopAddr(pcounter);
						markStopAddresses();
					}
					if (_out == null && sa != null) {
						sa._active = false;
						markStopAddresses();
					}
					return NOSTEP;
				case DBG_SHOWSTACK: {
					if (codeItem != null) {
						if (stackPointer < 0 ||
							stackPointer-codeItem.getParam() < 0) {
							display("Stack is empty");
						} else {
							display("Stack:");
							for (int i = 0; i<=stackPointer-codeItem.getParam();
								i++) {
								display("[" + i + "]: " + stack[i]);
							}
						}
					}
					XDCallItem list = callList;
					while (list != null) {
						display("Called from: " + list.getReturnAddr());
						list = list.getParentCallItem();
					}
					continue;
				}
				case DBG_SHOWGLOBALVARS: {
					String[] names = xnode.getVariableNames();
					if (names == null || names.length == 0) {
						display("No global variables");
					} else {
						display("Global variables:");
						for (String name:  names) {
							display(name + ": " + xnode.getVariable(name));
						}
					}
					if (xnode instanceof ChkElement) {
						ChkElement xc =(ChkElement) xnode;
						for (;;) {
							XVariableTable vars =
								((XElement) xc.getXMElement())._vartable;
							names = vars!=null ? vars.getVariableNames() : null;
							if (names != null && names.length > 0) {
								display("Variables of " + xc.getXPos() + ":");
								for (String name: names) {
									display(name + ": "
										+ xc.loadModelVariable(name));
								}
							}
							XXNode xn = xc.getParent();
							if (xn instanceof ChkElement) {
								xc =(ChkElement) xn;
							} else {
								break;
							}
						}
					}
					continue;
				}
				case DBG_SHOWLOCALVARS: {
					XMVariable[] vars = si==null ? null:si.getLocalVariables();
					if (vars == null || vars.length == 0) {
						display("No local variables");
					} else {
						display("Local variables:");
						for (int i = 0; i < vars.length; i++) {
							XMVariable v = vars[i];
							String val;
							try {
								val = localVariables[v.getOffset()].toString();
							} catch (Exception ex) {
								val = "UNDEFINED VALUE";
							}
							display(v.getName()+ ": " + val);
						}
					}
					continue;
				}
				case DBG_SHOWPOSITION:
					display(stmtInfo); // only if command line mode
					continue;
				case DBG_SHOWERRORS: {
					ReportWriter reporter = xnode.getReportWriter();
					ArrayReporter ireporter = xnode.getTemporaryReporter();
					if ((reporter != null && reporter.errorWarnings()) ||
						(ireporter != null && ireporter.errorWarnings())) {
						int i;
						if (reporter != null && reporter.errorWarnings()) {
							txt = "";
							i = reporter.getWarningCount()
								+ ireporter.getWarningCount();
							if (i > 0) {
								txt += "warnings: " + i + " ";
							}
							i = reporter.getErrorCount()
								+ ireporter.getErrorCount();
							if (i > 0) {
								txt += "errors: " + i + " ";
							}
							i = reporter.getFatalErrorCount()
								+ ireporter.getFatalErrorCount();
							if (i > 0) {
								txt += "fatals: " + i + " ";
							}
							if (txt.length() != 0) {
								display(txt);
							}
						}
						if (ireporter != null && ireporter.errorWarnings()) {
							Report rep = ireporter.getLastErrorReport();
							if (rep == null && reporter != null) {
								rep = reporter.getLastErrorReport();
							}
							if (rep != null) {
								display("Last temporary error: " +
									rep.toString());
							}
						}
					} else {
						display("No errors");
					}
					continue;
				}
				case DBG_SHOWTEXT:
				case DBG_SETTEXT:
					if (command == DBG_SHOWTEXT) {
						display(xnode.getXPos()
							+ ": = '" + ((XXData) xnode).getTextValue() + "'");
					} else {
						try {
							if (_out != null) {
								display("Set value: ");
								((XXData) xnode).setTextValue(readLine());
							} else {
								((XXData) xnode).setTextValue(JOptionPane
									.showInputDialog(_frame,"Set value"));
							}
						} catch (Exception ex) {
							display("Error: can't set text value here");
						}
					}
					continue;
				case DBG_SHOWCONTEXT: {
					XDValue val = xnode.getXDContext();
					display("" + val);
					continue;
				}
				case DBG_SHOWELEMENT:
					if (xnode.getElement() == null) {
						display("null");
					} else {
						display(KXmlUtils.nodeToString(xnode.getElement()));
					}
					continue;
				default: {
					if (_out != null) {
						display(
"Commands:\n" +
"  go       - continue\n" +
"  ps       - print stack\n" +
"  pl       - print values of local  variables\n" +
"  pg       - print global variables\n" +
"  err      - last error\n" +
"  text     - print value of actual attribute or text node\n" +
"  elem     - print value of actual element\n" +
"  context  - context in create mode\n" +
"  setText  - set actual text value (attribute or text node)\n" +
"  disable  - disable this breakpoint and continue (no more stop here)\n" +
"  step     - stop at next command (do not step into methods)\n" +
"  stepInto - stop at next command and step also into methods\n" +
"  pos      - source position\n"+
"  exit     - exit from debugging mode\n"+
"  can      - cancel\n");
					} else {
						JOptionPane.showMessageDialog(_frame,
							"See \"File (F10)\" in menu bar");
					}
				}
			}
		}
	}

	@Override
	/** Clear XScript breakpoint area. */
	public void clearStopAddrs() {
		_stopAddresses = new StopAddr[0];
	}

	@Override
	/** Check if breakpoint area contains the stop address.
	 * @return true if breakpoint area contains the stop address.
	 */
	public boolean hasStopAddr(int addr) {
		if (_debugMode) {
			for (StopAddr x: _stopAddresses) {
				if (x._stopAddr == addr) {
					return true;
				}
			}
		}
		return false;
	}

	private StopAddr getStopAddr(int addr) {
		for (StopAddr x: _stopAddresses) {
			if (x._stopAddr == addr) {
				return x;
			}
		}
		return null;
	}

	@Override
	/** Set stop address to the breakpoint area.
	 * @param addr stop address.
	 */
	public void setStopAddr(int addr) {
		XMDebugInfo di = _xdpool.getDebugInfo();
		if (di == null) {
			return;
		}
		XMStatementInfo[] si = di.getStatementInfo();
		if (si == null) {
			return;
		}
		for (XMStatementInfo x: si) {
			int i = x.getAddr();
			if (i >= addr) {
				XDSourceItem xsi = _sources.get(x.getSysId());
				if (xsi == null) {
					setStopAddr(new StopAddr(addr));
				} else {
					setStopAddr(createStopAddr(di, x));
				}
				return;
			}
		}
	}

	/** Set stop address to the breakpoint area.
	 * @param addr stop address.
	 */
	private void setStopAddr(StopAddr newAddr) {
		if (_stopAddresses.length == 0) {
			_stopAddresses = new StopAddr[1];
			_stopAddresses[0] = newAddr;
		} else {
			int i = Arrays.binarySearch(_stopAddresses, newAddr);
			if (i < 0) {
				StopAddr[] old = _stopAddresses;
				_stopAddresses = new StopAddr[old.length + 1];
				i = -(i + 1);
				if (i > 0) {
					System.arraycopy(old, 0, _stopAddresses, 0, i);
				}
				if (i < old.length) {
					System.arraycopy(old, i, _stopAddresses, i+1, old.length-i);
				}
				_stopAddresses[i] = newAddr;
			}
		}
	}

	@Override
	/** Remove stop address from the breakpoint area.
	 * @param addr stop address.
	 * @return true stop address was removed.
	 */
	public boolean removeStopAddr(int addr) {
		if (_stopAddresses.length == 0) {
			return false;
		}
		if (_stopAddresses != null) {
			for (int i = 0; i < _stopAddresses.length; i++) {
				StopAddr x = _stopAddresses[i];
				if (x._stopAddr == addr) {
					if (_stopAddresses.length == 1) {
						_stopAddresses = new StopAddr[0];
					} else {
						StopAddr[] old = _stopAddresses;
						_stopAddresses = new StopAddr[old.length - 1];
						if (i > 0) {
							System.arraycopy(old, 0, _stopAddresses, 0, i);
						}
						if (i < _stopAddresses.length) {
							System.arraycopy(old,
								i+1, _stopAddresses,i,_stopAddresses.length-i);
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	/** Clear XPos breakpoint area. */
	public void clearXPosArea() {_xposItems = null;}

	@Override
	/** Check if breakpoint area contains the XPos item.
	 * @return true if breakpoint area contains the XPos item.
	 */
	public boolean hasXPos(String xpos) {
		return !_debugMode || _xposItems == null ? false :
			Arrays.binarySearch(_xposItems, xpos) >= 0;
	}

	@Override
	/** Set XPos item to the breakpoint area.
	 * @param xpos the string with XPos item.
	 */
	public void setXpos(String xpos) {
		if (_xposItems == null) {
			_xposItems = new String[]{xpos};
		} else {
			int i = Arrays.binarySearch(_xposItems, xpos);
			if (i < 0) {
				String[] old = _xposItems;
				_xposItems = new String[old.length + 1];
				i = -(i + 1);
				if (i > 0) {
					System.arraycopy(old, 0, _xposItems, 0, i);
				}
				_xposItems[i] = xpos;
				if (i < old.length) {
					System.arraycopy(old, i, _xposItems, i+1, old.length-i);
				}
			}
		}
	}

	@Override
	/** Remove all XPos items with given stop address from the breakpoint area.
	 * @param xpos the string with XPos item.
	 */
	public void removeXpos(String xpos) {
		if (_xposItems == null) {
			return;
		}
		int i = Arrays.binarySearch(_xposItems, xpos);
		if (i >= 0) {
			if (_xposItems.length == 1) {
				_xposItems = null;
			} else {
				String[] old = _xposItems;
				_xposItems = new String[old.length - 1];
				if (i > 0) {
					System.arraycopy(old, 0, _xposItems, 0, i);
				}
				if (i < _xposItems.length) {
					System.arraycopy(old,
						i + 1, _xposItems, i, _xposItems.length - i);
				}
			}
		}
	}
}