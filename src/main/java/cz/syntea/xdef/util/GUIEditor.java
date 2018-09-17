package cz.syntea.xdef.util;

import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDConstants;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDOutput;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.impl.XDSourceItem;
import cz.syntea.xdef.impl.debug.GUIScreen;
import cz.syntea.xdef.model.XMDefinition;
import cz.syntea.xdef.model.XMElement;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.xml.KXmlUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Provides utility for interactive editing and debugging X-definitions.
 * @author Vaclav Trojan
 */
public class GUIEditor extends GUIScreen {

	/** Create instance of the screen to display the sources of X-definitions.
	 * @param x X position of the left corner of the screen.
	 * @param y Y position of the left corner of the screen.
	 * @param width width of the screen.
	 * @param height height of the screen.
	 */
	private GUIEditor(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	private static final XDPool PROJECTXDPOOL;

	static {
		String xdef =
"<xd:def xmlns:xd=\"http://www.syntea.cz/xdef/3.1\" root=\"Project\">\n" +
"\n" +
"  <Project>\n" +
"\n" +
"    <xd:mixed>\n" +
"\n" +
"      <!-- Add a class to classpath -->\n" +
"      <XDefinition xd:script = \"+\">\n" +
"        string();\n" +
"      </XDefinition>\n" +
"\n" +
"      <!-- Set property -->\n" +
"      <Property xd:script = \"*;\"\n" +
"        Name = \"string();\"\n" +
"        Value = \"string()\" />\n" +
"\n" +
"      <!-- If the \"Execute\" elements are specified the compied XDPool\n" +
"        is executed according to the desciged parameters. -->\n" +
"      <Execute xd:script = \"*; ref Execute\"/>\n" +
"\n" +
"    </xd:mixed>\n" +
"\n" +
"  </Project>\n" +
"\n" +
"  <Execute\n" +
"    XDName = \"? string(0, 1000);\"\n" +
"    Mode = \"? enum('construct', 'validate');\"\n" +
"    DisplayResult = \"? enum('true', 'false'); \" >\n" +
"\n" +
"    <xd:mixed>\n" +
"\n" +
"      <!-- If the \"Var\" elements are specified the specified\n" +
"        variables are set to the X-definition processor. -->\n" +
"      <Var  xd:script = \"*;\" Name = \"string();\">\n" +
"        string();\n" +
"      </Var>\n" +
"\n" +
"      <!-- If the \"Context\" element is specified the context\n" +
"        is set to the X-definition processor. -->\n" +
"      <Context xd:script = \"?;\" Edit = \"? enum('true', 'false');\" >\n" +
"        string();\n" +
"      </Context>\n" +
"\n" +
"      <!-- If the \"Input\" element is specified the input data\n" +
"        is set to the X-definition processor. -->\n" +
"      <Input xd:script = \"?;\" Edit = \"? enum('true', 'false');\">\n" +
"        string();\n" +
"      </Input>\n" +
"\n" +
"      <!-- If the \"SaveResult\" element is specified the result\n" +
"        element will be saved to the specified file. -->\n" +
"      <SaveResult xd:script = \"?\"\n" +
"        Indent = \"? string();\"\n" +
"        Encoding = \"? string();\"\n" +
"        File = \"string();\" />\n" +
"\n" +
"    </xd:mixed>\n" +
"\n" +
"  </Execute>\n" +
"\n" +
"</xd:def>";
		Properties props = new Properties();
		props.setProperty(XDConstants.XDPROPERTY_WARNINGS,
			XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE);
		props.setProperty(XDConstants.XDPROPERTY_DEBUG,
			XDConstants.XDPROPERTYVALUE_DEBUG_FALSE);
		props.setProperty(XDConstants.XDPROPERTY_DISPLAY,
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);
		PROJECTXDPOOL = XDFactory.compileXD(props, xdef);
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

	private void initMenuBar() {
		JMenu fileMenu = _menuBar.add(new JMenu("File (F10)"));
		JMenuItem ji;
		// Save as... menu item
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

	private void display( final ArrayReporter err,
		final String msg, final Object obj) throws Exception {

		_sources = new TreeMap<String, XDSourceItem>();
		XDSourceItem xsi = new XDSourceItem(obj);
		_sourceItem = null;
		_sources.put(msg, xsi);
		initSourceMap();
		initSourceWindow();
		setInitialSource();
		if (err != null) {
			_sourceItem = xsi;
			initInfoArea(err);
		}
		initMenuBar();
		_frame.pack();
		_frame.setBounds(10, 10, 1200, 700);
		_frame.setVisible(true);
		waitFrame();
		closeEdit(null);
	}

	private static void displayString(final String msg,
		final String s) throws Exception {
		new GUIEditor(10, 10, 1200, 700).display(null, msg, s);
	}

	private static void editXml(
		final ArrayReporter err,
		final String msg,
		final String fileName) throws Exception {
		new GUIEditor(10, 10, 1200, 700).display(
			err, msg + fileName, new File(fileName));
	}

	/** Run project with GUIEditor.
	 * @param src source with the project.
	 *
	 */
	public static final void runEditor(final String src) {
		try {
			// Create element with project according to X-definition
			XDDocument pxd = PROJECTXDPOOL.createXDDocument();
			Element project = pxd.xparse(src, null);
			NodeList nl;

			// set properties
			nl = project.getElementsByTagName("Property");
			Properties props = new Properties();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				props.setProperty(e.getAttribute("Name"),
					e.getAttribute("Value"));
			}

			// get X-definition sources
			nl = project.getElementsByTagName("XDefinition");
			ArrayList<String> axdefs = new ArrayList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				String t = e.getTextContent().trim();
				if (t != null && !t.isEmpty()) {
					axdefs.add(t);
				}
			}

			// compile X-definitions
			XDBuilder xb = XDFactory.getXDBuilder(props);
			xb.setSource(axdefs.toArray(new String[axdefs.size()]));
			XDPool xp = xb.compileXD();

			// execute project
			nl = project.getElementsByTagName("Execute");
			for (int i = 0; i < nl.getLength(); i++) {
				Element exe = (Element) nl.item(i);
				String xdName = exe.getAttribute("XDName").trim();
				NodeList nl1 = exe.getElementsByTagName("Input");
				String data = null;
				if (nl1.getLength() == 1) {
					Element e = (Element) nl1.item(0);
					data = e.getTextContent().trim();
					if ("true".equals(e.getAttribute("Edit"))) {
						editXml(null, "Input data: ", data);
					}
				}
				XDDocument xd = xp.createXDDocument(xdName);
				nl1 = KXmlUtils.getChildElements(exe, "Var");
				for (int j = 0; j < nl1.getLength(); j++) {
					Element e = (Element) nl1.item(j);
					String varName = e.getAttribute("Name");
					String value = e.getTextContent();
					xd.setVariable(varName, value);
				}
				nl1 = exe.getElementsByTagName("Context");
				if (nl1.getLength() == 1) {
					Element e = (Element) nl1.item(0);
					String context = e.getTextContent().trim();
					if (context != null && !context.isEmpty()) {
						if ("true".equals(e.getAttribute("Edit"))) {
							editXml(null, "Context data: ", context);
						}
					}
					xd.setXDContext(context);
				}
				Element result;
				ArrayReporter reporter = new ArrayReporter();
				StringWriter strw = new StringWriter();
				XDOutput stdout =
					XDFactory.createXDOutput(new PrintWriter(strw), false);
				xd.setProperties(props);
				xd.setStdOut(stdout);
				if ("construct".equals(exe.getAttribute("Mode"))) {
					String name;
					String uri;
					XMDefinition def =  xp.getXMDefinition();
					XMElement[] x = xp.getXMDefinition().getModels();
					name = x[0].getName();
					uri = x[0].getNSUri();
					if (data != null && data.trim().length() > 0) {
						Element el = KXmlUtils.parseXml(data).
							getDocumentElement();
						xd.setXDContext(el);
						String n = el.getLocalName();
						String u = el.getNamespaceURI();
						if (def != null && def.getModel(u, n) != null) {
							uri = u;
							name = n;
						}
					}
					result = xd.xcreate(new QName(uri, name), reporter);
				} else {
					result = xd.xparse(data, reporter);
				}
				stdout.close();
				if (reporter.errorWarnings()) {
					File f = new File(data);
					if (data == null || !f.isFile()
						|| !f.exists() || !f.canRead()) {
						displayString("ERROR", reporter.printToString());
					} else {
						editXml(reporter,
							"ERROR"
							+ (reporter.getErrorCount()
								+ reporter.getWarningCount() > 1 ? "S" : "")
							+ " IN: ",
							data);
					}
				} else {
					nl1 = exe.getElementsByTagName("SaveResult");
					if (nl1.getLength() >= 1) {
						Element e = (Element) nl1.item(0);
						String name = e.getAttribute("File").trim();
						boolean indent =
							"true".equals(e.getAttribute("Indent"));
						String encoding = e.getAttribute("Encoding");
						if (encoding.isEmpty()) {
							encoding = "UTF-8";
						}
						try {
							KXmlUtils.writeXml(
								new File(name), encoding, result, indent, true);
						} catch (Exception ex) {
							throw new RuntimeException("Can't write result to "
								+ name);
						}
					}
				}
				if ("true".equals(exe.getAttribute("DisplayResult"))) {
					String s = "";
					if (!strw.toString().isEmpty()) {
						s += "SYSTEM OUTPUT:\n" + strw.toString() + "\n"
							+ "\n";
					}
					displayString("Result of processing",
						s + "RESULT XML ELEMENT:\n"
							+ KXmlUtils.nodeToString(result, true));
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected exception in GUIEditor", ex);
		}
	}

	/** Call generation of a collection of X-definitions from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><tt>-p </tt>file with the project</li>
	 * <li><tt>-v [switches]</tt>run validation mode</li>
	 * <li><tt>-c [switches]</tt>run construction mode</li>
	 * </ul>
	 * Switches:
	 * <ul>
	 * <li><tt>-xdef file </tt>specifies the source with the X-definition</li>
	 * <li><tt>-data file file</tt>specifies input data or context</li>
	 * <li><tt>-debug </tt>sets the debug mode</li>
	 * <li><tt>-editInput </tt>enables to runEditor input data before execution</li>
	 * <li><tt>-displayResult </tt>displays result XML element</li>
	 * </ul>
	 */
	public static void main(String[] args) {
		final String info =
"GUI - Edit and run X-definition in graphical user interface.\n"+
"Usage: -p project_file | -v [switches] | [-c [switches\n\n"+
" -p runs a project file\n"+
" -v compiles X-definition and runs validation mode\n"+
" -c compiles X-definition and runs construction mode\n\n"+
"Switches\n"+
" -xdef file with X-definition (may be specified more times)\n"+
" -data file XML data (input file for validation mode and context\n"+
"     for construction mode)\n"+
" -debug sets debugging mode when project is executed\n"+
" -editInput enables to edit input data before execution\n"+
" -displayResult displays result XML data\n";
		if (args == null || args.length == 0) {
//			args = new String[] {"-p",
//				"test/test/xdef/data/validate/project.xml"};
//				"test/test/xdef/data/construct/project.xml"};
			System.err.println("No parameters.\n" + info);
			return;
		}
		String arg = args[0];
		if ("-h".equals(arg)) {
			System.out.println(info);
			return;
		}
		if ("-p".equals(arg)) {
			if (args.length == 1) {
				System.err.println("Missing parameter with project\n" + info);
			} else {
				runEditor(args[1]);
			}
			return;
		}
		char param;
		if ("-c".equals(arg)) {
			param = 'c';
		} else if ("-v".equals(arg)) {
			param = 'v';
		} else {
			System.err.println("Incorrect parameter: " + arg + "\n" + info);
			return;
		}
		int i = 1;
		ArrayList<String> xdefs = new ArrayList<String>();
		String dataPath = null;
		String debug = null;
		String editInput = null;
		String displayResult = null;
		while (i < args.length) {
			arg = args[i++];
			if ("-xdef".equals(arg)) {
				xdefs.add(args[i++]);
				continue;
			}
			if ("-data".equals(arg)) {
				if (dataPath != null) {
					System.err.println(
						"Redefinition of parameter \"-data\"\n" + info);
					return;
				}
				dataPath = args[i++];
				continue;
			}
			if ("-debug".equals(arg)) {
				if (debug != null) {
					System.err.println(
						"Redefinition of parameter \"-debug\"\n" + info);
					return;
				}
				debug = "true";
				continue;
			}
			if ("-editInput".equals(arg)) {
				if (debug != null) {
					System.err.println(
						"Redefinition of parameter \"-editInput\"\n" + info);
					return;
				}
				editInput = "true";
				continue;
			}
			if ("-displayResult".equals(arg)) {
				if (displayResult != null) {
					System.err.println(
						"Redefinition of parameter \"-displayResult\"\n" +info);
					return;
				}
				displayResult = "true";
				continue;
			}
			System.err.println("Incorrect parameter \"" + arg + "\"\n" + info);
			return;
		}
		String src = "<Project>\n";
		String msg = "";
		switch (param) {
			case 'c': { // create
				if (xdefs.isEmpty()) {
					msg += "Missing X-definition\n";
				}
				src += "  <Execute Mode = \"create\"";
				if (displayResult != null) {
					src += " DisplayResult = \"true\"";
				}
				src += ">\n";
				if (dataPath != null) {
					src += "    <Context";
					if (editInput!= null) {
						src += " Edit='true'";
					}
					src += ">" + dataPath + "</Context>\n";
				}
				src += "  </Execute>\n";
				break;
			}
			case 'v': { // validate
				if (xdefs.isEmpty()) {
					msg += "Missing X-definition\n";
				}
				src += "  <Execute Mode = \"validate\"";
				if (displayResult != null) {
					src += " DisplayResult = \"true\"";
				}
				src += ">\n";
				if (dataPath == null) {
					msg += "Missing input data\n\n";
				} else {
					src += "    <Input";
					if (editInput!= null) {
						src += " Edit='true'";
					}
					src += ">" + dataPath + "</Input>\n";
				}
				src += "  </Execute>\n";
				break;
			}
		}
		if (!msg.isEmpty()) {
			System.err.println(msg +	"\n" + info);
			return;
		}
		for (String x: xdefs) {
			src += "  <XDefinition>" + x + "</XDefinition>\n";
		}
		src +=
"  <Property Name = \"" + XDConstants.XDPROPERTY_WARNINGS
			+ "\" Value = \""
			+ XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE	+ "\"/>\n" +
"  <Property Name = \"" + XDConstants.XDPROPERTY_DISPLAY
			+ "\" Value = \""
			+ XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE + "\"/>\n" +
"  <Property Name = \"" + XDConstants.XDPROPERTY_DEBUG
			+ "\" Value = \"" + (debug == null
			? XDConstants.XDPROPERTYVALUE_DEBUG_FALSE
			: XDConstants.XDPROPERTYVALUE_DEBUG_TRUE) + "\"/>\n" +
		"</Project>";
		runEditor(src);
	}
}