package org.xdef.util;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDOutput;
import org.xdef.XDPool;
import org.xdef.impl.XDSourceInfo;
import org.xdef.impl.XDSourceItem;
import org.xdef.impl.debug.GUIScreen;
import org.xdef.model.XMDefinition;
import org.xdef.model.XMElement;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xdef.impl.GenXDef;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;

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
	private GUIEditor(final XDSourceInfo si) {super(si);}

	private static final XDPool PROJECTXDPOOL;

	static {
		String xdef =
"<xd:def xmlns:xd=\"" + XDConstants.XDEF32_NS_URI + "\" root=\"Project\">\n" +
"  <Project Show=\"? enum('true', 'false');\">\n" +
"    <xd:mixed>\n" +
"      <!-- Add a class to classpath -->\n" +
"      <XDefinition xd:script=\"+\">\n" +
"        string();\n" +
"      </XDefinition>\n" +
"      <!-- Set property -->\n" +
"      <Property xd:script=\"*\"\n" +
"        Name=\"string();\"\n" +
"        Value=\"string()\" />\n" +
"      <!-- If the \"Execute\" elements are specified the compied XDPool\n" +
"        is executed according to the desciged parameters. -->\n" +
"      <Execute xd:script=\"*; ref Execute\"/>\n" +
"    </xd:mixed>\n" +
"  </Project>\n" +
"\n" +
"  <Execute\n" +
"    XDName=\"? string(1, 1000);\"\n" +
"    Mode=\"? enum('construct', 'validate');\"\n" +
"    DisplayResult=\"? enum('true', 'false'); \" >\n" +
"    <xd:mixed>\n" +
"      <!-- If the \"Var\" elements are specified the specified\n" +
"        variables are set to the X-definition processor. -->\n" +
"      <Var xd:script=\"*\" Name=\"string();\">\n" +
"        string();\n" +
"      </Var>\n" +
"      <!-- If the \"Context\" element is specified the context\n" +
"        is set to the X-definition processor. -->\n" +
"      <Context xd:script=\"?\" Edit=\"? enum('true', 'false');\" >\n" +
"        string();\n" +
"      </Context>\n" +
"      <!-- If the \"Input\" element is specified the input data\n" +
"        is set to the X-definition processor. -->\n" +
"      <Input xd:script=\"?\" Edit=\"? enum('true', 'false');\">\n" +
"        string();\n" +
"      </Input>\n" +
"      <!-- If the \"SaveResult\" element is specified the result\n" +
"        element will be saved to the specified file. -->\n" +
"      <SaveResult xd:script=\"?\"\n" +
"        Indent=\"? string();\"\n" +
"        Encoding=\"? string();\"\n" +
"        File=\"string();\" />\n" +
"    </xd:mixed>\n" +
"  </Execute>\n" +
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

	private void initMenuBar(final String runMenu) {
		JMenu fileMenu = _menuBar.add(new JMenu("File (F10)"));
		fileMenu.getActionMap().remove("Compile");
		_sourceArea.getActionMap().remove("Compile");
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
		if (runMenu != null) {
			fileMenu.addSeparator();
			ji = new JMenuItem(runMenu);
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
		}
		fileMenu.add(ji);
		fileMenu.addSeparator();
		// Exit menu item
		ji = new JMenuItem("Exit");
		ji.setMnemonic((int) 'X');
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_actionFinished = true;
				if (_sourceItem != null && _sourceItem._changed) {
					String s;
					if (_sourceArea == null
						|| (s = _sourceArea.getText()) == null
						|| s.equals(_sourceItem._source)){
						_sourceItem._changed = false;
					}
				}
				notifyFrame();
			}
		});
		fileMenu.add(ji);
		// Source position info
		_menuBar.add(_sourcePositionInfo, BorderLayout.EAST);
		_frame.setJMenuBar(_menuBar);
		_sourceArea.getActionMap().remove("Compile");
		if (runMenu != null) {
			_sourceArea.getActionMap().put(runMenu,
				new AbstractAction(runMenu){
				private static final long serialVersionUID =
					4377386270269629176L;
				@Override
				public void actionPerformed(ActionEvent evt) {
					updateSourceItem();
					_actionFinished = false;
					notifyFrame();
				}
			});
		}
		_sourceArea.setCaretPosition(0);
	}

	private void display(final ArrayReporter err,
		final String msg,
		final Object obj,
		XDSourceInfo si,
		boolean editable,
		String runMenu) throws Exception {
		_windowName = msg;
		_si = si;
		XDSourceItem xsi = new XDSourceItem(obj);
		_sources = _si.getMap();
		_sources.clear();
		String sourceId;
		if (obj instanceof File) {
			sourceId = ((File) obj).getCanonicalPath();
		} else if (obj instanceof String) {
			sourceId = "STRING";
		} else if (obj instanceof URL) {
			sourceId = SUtils.getExtendedURL(((URL) obj).toExternalForm())
				.toExternalForm();
		} else {
			sourceId = obj.getClass().getName();
		}
		_sources.put(sourceId, xsi);
		_sourceItem = null;
		initSourceMap();
		initSourceWindow();
		setInitialSource();
		initMenuBar(runMenu);
		if (err != null) {
			initInfoArea(err);
		}
		setLineNumberArea();
		if (!editable) {
			_sourceArea.setEditable(false);
			_infoArea.setRows(0);
			_infoArea.setVisible(false);
		}
		_frame.pack();
		_frame.setBounds(_si._xpos, _si._ypos, _si._width, _si._height);
		_frame.setVisible(true);
		waitFrame();
		si._xpos = _frame.getX();
		si._ypos = _frame.getY();
		si._width = _frame.getWidth();
		si._height = _frame.getHeight();
		closeEdit();
	}

	private static void displayString(final String msg,
		final String s,
		final XDSourceInfo si) throws Exception {
		GUIEditor ge = new GUIEditor(si);
		ge.display(null, msg, s, si, false, null);
	}

	private static void editXml(final ArrayReporter err,
		final String msg,
		final Object o,
		final XDSourceInfo si,
		final String runMenu) throws Exception {
		new GUIEditor(si).display(err, msg, o, si, true, runMenu);
	}

	/** Display and edit XMl given by src.
	 * @param title title of window.
	 * @param src may be pathname of file, url or XML.
	 * @return source item of edited XML.
	 * @throws Exception if an error occurs.
	 */
	private static String editData(final String title,
		final String src) throws Exception {
		String data = src.trim();
		XDSourceInfo xi = new XDSourceInfo();
		Object o;
		if (data.startsWith("<")) {
			o = data;
		} else {
			String[] ss = SUtils.getSourceGroup(data);
			String s = (ss != null && ss.length == 1) ? ss[0] : data;
			if (!((File)(o = new File(s))).exists()) {
				o = SUtils.getExtendedURL(s);
			}
		}
		XDSourceItem xsi = new XDSourceItem(o);
		xi.getMap().put(data, xsi);
		ArrayReporter rep = new ArrayReporter();
		editXml(rep, title, o, xi, null);
		xsi = xi.getMap().values().iterator().next();
		if (xsi._saved || !data.equals(xsi._source.trim())) {
			if (xsi._url != null) {
				data = xsi._url.toExternalForm();
			} else {
				data = xsi._source.trim();
			}
		}
		return data;
	}

	/** Read input data or context.
	 * @param e element with data description.
	 * @param si Source information.
	 * @return string with data.
	 * @throws Exception if an error occurs.
	 */
	private static String getData(final Element e, final XDSourceInfo si)
		throws Exception {
		if (e == null) {
			return null;
		}
		String data = e.getTextContent();
		if (data != null) {
			data = data.trim();
			if ("true".equals(e.getAttribute("Edit"))) {
				String s = editData("Input XML data", data);
				if (!data.equals(s)) {
					e.setTextContent(s);
					data = s;
				}
			}
		}
		return data;
	}

	/** Update list of XDefinitions in the project (assure the sequence
	 * from the map in XDSourceInfo).
	 * @param project project XML.
	 * @param si XDSourceInfo object.
	 */
	private static void updateXdefList(final Element project,
		final XDSourceInfo si) {
		NodeList nl = project.getElementsByTagName("XDefinition");
		for (int i = nl.getLength() - 1; i >= 0 ; i--) {
			project.removeChild(nl.item(i));  // romove all XDefinition items
		}
		Document doc = project.getOwnerDocument();
		for (String x: si.getMap().keySet()) {
			XDSourceItem xsi = si.getMap().get(x);
			String s = xsi._url!=null ? xsi._url.toExternalForm() : xsi._source;
			if (s != null && !(s = s.trim()).isEmpty()) {
				Element e = doc.createElement("XDefinition");
				e.setTextContent(s);
				project.appendChild(e);
			}
		}
	}

	/** Get canonized form of source item as the external form of URL.
	 * @param s source item.
	 * @return canonized form of source item as the external form of URL.
	 */
	private static String getSourceURL(final String s) {
		String t;
		if (s != null && !(t = s.trim()).isEmpty()) {
			try {
				File f = new File(t);
				if (f.exists()) {
					f = f.getCanonicalFile();
					return SUtils.getExtendedURL(
						f.toURI().toURL().toExternalForm()).toExternalForm();
				}
				return SUtils.getExtendedURL(t).toExternalForm();
			} catch (Exception ex) {}
		}
		return null;
	}

	private static Element canonizeProject(final Element project) {
		NodeList nl = project.getElementsByTagName("XDefinition");
		ArrayList<String> sources = new ArrayList<String>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String s = e.getTextContent();
			if (s != null && !(s=s.trim()).isEmpty()) {
				String[] x;
				if (s.startsWith("<")) {
					x = new String[]{s};
				} else {
					try {
						x = SUtils.getSourceGroup(s);
					} catch (Exception ex) {
						x = new String[]{s};
					}
				}
				for (String t : x) {
					if (!sources.contains(t)) {
						sources.add(t);
					}
				}
			}
		}
		for (int i = nl.getLength() - 1; i >= 0; i--) {
			project.removeChild(nl.item(i));
		}
		Document doc = project.getOwnerDocument();
		for (String s: sources) {
			Element e = doc.createElement("XDefinition");
			e.setTextContent(s);
			project.appendChild(e);
		}
		// remove sources with the equal text content.
		ArrayList<Element> ar = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			String s = e.getTextContent();
			for (int j = i + 1; j < nl.getLength(); j++) {
				if (s.equals(((Element) nl.item(j)).getTextContent())){
					ar.add(e);
				}
			}
		}
		for (Element e: ar) {
			project.removeChild(e);
		}
		nl = project.getElementsByTagName("XDefinition");
		ar = new ArrayList<Element>();
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			ar.add(e);
		}
		nl = project.getElementsByTagName("Execute");
		for (int i = 0; i < nl.getLength(); i++) {
			NodeList nl1 = ((Element) nl.item(i)).getElementsByTagName("Input");
			for (int j = 0; j < nl1.getLength(); j++) {
				Element e = (Element) nl1.item(j);
				String s = getSourceURL(e.getTextContent());
				if (s != null) {
					e.setTextContent(s);
				}
			}
			nl1 = ((Element) nl.item(i)).getElementsByTagName("SaveResult");
			for (int j = 0; j < nl1.getLength(); j++) {
				Element e = (Element) nl.item(j);
				String s = getSourceURL(e.getAttribute("File"));
				if (s != null) {
					e.setAttribute("File", s);
				}
			}
		}
		return project;
	}

	private static boolean compareNodes(final Element p1,
		final Element p2,
		final String tagname) {
		NodeList nl1,nl2;
		nl1 = p1.getElementsByTagName(tagname);
		nl2 = p2.getElementsByTagName(tagname);
		if (nl1.getLength() != nl2.getLength()) {
			return false;
		}
		ArrayReporter ar = new ArrayReporter();
		for (int i = 0; i < nl1.getLength(); i++) {
			Element el1 = (Element) nl1.item(i);
			Element el2 = (Element) nl2.item(i);
			KXmlUtils.compareElements(el1, el2, true, ar);
			if (!"OK".equals(ar.printToString())) {
				return false;
			}
			ar.clear();
		}
		return true;
	}

	private static boolean compareProjects(final Element p1, final Element p2) {
		if (!p1.getAttribute("Show").equals(p2.getAttribute("Show"))) {
			return false;
		}
		if (!compareNodes(p1, p2, "Property")) {
			return false;
		}
		if (!compareNodes(p1, p2, "XDefinition")) {
			return false;
		}
		return compareNodes(p1, p2, "Execute");
	}
	/** Run project with GUIEditor.
	 * @param param 'c' (compose) , 'v' (validate) or 'g' (generate)
	 * @param src source with the project.
	 */
	public static final void runEditor(final char param, final String src) {
		try {
			Element e;
			// Create element with project according to X-definition
			XDDocument pxd = PROJECTXDPOOL.createXDDocument();
			Element project = pxd.xparse(src, null);
			if ("true".equals(project.getAttribute("Show"))) {
				XDSourceInfo sinfo = new XDSourceInfo();
				Object o = src.charAt(0) == '<' ? src : new File(src);
				editXml(null, "Project", o, sinfo, null);
				project = pxd.xparse(src, null);
			}
			project  = canonizeProject(project);
			Element originalProject = (Element) project.cloneNode(true);
			NodeList nl;
			// set properties
			nl = project.getElementsByTagName("Property");
			Properties props = new Properties();
			for (int i = 0; i < nl.getLength(); i++) {
				e = (Element) nl.item(i);
				props.setProperty(e.getAttribute("Name"),
					e.getAttribute("Value"));
			}
			// get X-definition sources
			nl = project.getElementsByTagName("XDefinition");
			ArrayList<String> axdefs = new ArrayList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				e = (Element) nl.item(i);
				String t = e.getTextContent().trim();
				if (t != null && !t.isEmpty()) {
					axdefs.add(t);
				}
			}
			// compile X-definitions
			XDPool xp = XDFactory.compileXD(props,
				axdefs.toArray(new String[axdefs.size()]));
			XDSourceInfo si = xp.getXDSourceInfo();
			boolean changed = false;
			for (String x: si.getMap().keySet()) {
				XDSourceItem xsi = si.getMap().get(x);
				changed |= xsi._changed | xsi._saved;
			}
			if (changed) {
				updateXdefList(project, si); //Update X-definitions elements
			}
			// execute project
			nl = project.getElementsByTagName("Execute");
			for (int i = 0; i < nl.getLength(); i++) {
				Element exe = (Element) nl.item(i);
				// get name of X-definition
				String xdName = exe.getAttribute("XDName").trim();
				// get inpout data
				e = KXmlUtils.firstElementChild(exe, "Input");
				String data = getData(e, si);
				// create XDDocument
				XDDocument xd = xp.createXDDocument(xdName);
				NodeList nl1 = KXmlUtils.getChildElements(exe, "Var");
				// set variables
				for (int j = 0; j < nl1.getLength(); j++) {
					e = (Element) nl1.item(j);
					String varName = e.getAttribute("Name");
					String value = e.getTextContent();
					xd.setVariable(varName, value);
				}
				// set context
				e = KXmlUtils.firstElementChild(exe,"Context");
				String context = getData(e, si);
				if (context != null) {
					xd.setXDContext(context);
				}
				Element result;
				ArrayReporter reporter = new ArrayReporter();
				// set properties
				xd.setProperties(props);
				// set stdout
				StringWriter strw = new StringWriter();
				XDOutput stdout =
					XDFactory.createXDOutput(new PrintWriter(strw), false);
				xd.setStdOut(stdout);
				String mode = exe.getAttribute("Mode");
				if ("construct".equals(mode)) { // run construction mode
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
				} else {  // run validation mode
					result = xd.xparse(data, reporter);
				}
				// set bounds of the window from previous steps
				if (xd.getXDPool().getXDSourceInfo() != si) {
					XDSourceInfo si1 = xd.getXDPool().getXDSourceInfo();
					si1._xpos = si._xpos;
					si1._ypos = si._ypos;
					si1._width = si._width;
					si1._height = si._height;
					si = si1;
				}
				stdout.close();
				if (reporter.errorWarnings()) {
					if (data != null && "validate".equals(mode)) {
						// show result
						File f = new File(new URL(data).getFile());
						if (data.startsWith("<") && !f.isFile()) {
							editXml(reporter, "ERROR:", data, si,
								"Input data changed, run again?");
						} else {
							editXml(reporter, "ERROR:", f, si, null);
						}
						XDSourceItem x = si.getMap().values().iterator().next();
						if (x._changed) {
							data = x._source;
							if (JOptionPane.showConfirmDialog(null,
								"Input data changed, run again?",
								null, JOptionPane.OK_CANCEL_OPTION) == 0) {
								i--;
								e = KXmlUtils.firstElementChild(exe, "Input");
								e.setTextContent(data);
								continue;
							}
						}
					} else { // error, sixpaly result
						displayString("ERROR:", reporter.printToString(), si);
					}
				}
				e = KXmlUtils.firstElementChild(exe, "SaveResult");
				if (e != null) { // save result XML
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
						//GUIEditor can't write XML data to file &{0}
						JOptionPane.showMessageDialog(null,
							Report.error(XDEF.XDEF851,name).toString());
					}
				}
				if ("true".equals(exe.getAttribute("DisplayResult"))) {
					// display result XML
					String s = result == null
						? "" : KXmlUtils.nodeToString(result, true);
					if (!strw.toString().isEmpty()) {
						s += s.isEmpty() ? "" : "\n\n";
						s += "=== System.out ===\n" + strw.toString();
					}
					if (!s.isEmpty()) {
						displayString("Result of processing:", s, si);
					}
				}
			}
			if (param == 'g') {
				editData("Generated project",
					KXmlUtils.nodeToString(project, true));
			} else if (!compareProjects(
				project = canonizeProject(project), originalProject)) {
				// if something changed in the project ask to save it
				JFileChooser jf;
				if (src.charAt(0) != '<') {
					jf = new JFileChooser(src);
				} else {
					jf = new JFileChooser(new File(".").getCanonicalFile());
				}
				jf.setDialogTitle(
					"Project changed. Do you want to save the project?");
				jf.setToolTipText("Save THE PROJECT to a file");
				int retval = jf.showSaveDialog(null);
				jf.setEnabled(false);
				if (retval == JFileChooser.APPROVE_OPTION) {
					try {
						File f = jf.getSelectedFile();
						KXmlUtils.writeXml(f, "UTF-8", project, true, true);
					} catch (Exception ex) {
						ex.printStackTrace(System.err);
						JOptionPane.showMessageDialog(null,//Can't write
							Report.error(SYS.SYS036,"Can't write data to file: "
								+ jf.getSelectedFile() + "\n" + ex));
					}
				}
			}
		} catch (Exception ex) {
			if (ex instanceof SThrowable) {
				JOptionPane.showMessageDialog(null,
					((SThrowable) ex).getReport().toString());
			} else {
				JOptionPane.showMessageDialog(null, //Program exception &{0}
					Report.error(SYS.SYS036, ex.toString()).toString());
			}
		}
	}

	/** Call generation of a collection of X-definitions from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><tt>-p </tt>file with the project</li>
	 * <li><tt>-v [switches]</tt>run validation mode</li>
	 * <li><tt>-c [switches]</tt>run construction mode</li>
	 * <li><tt>-g [XML source]</tt>Generate X-definition form XML data</li>
	 * </ul>
	 * Switches:
	 * <ul>
	 * <li><tt>-xdef file </tt>specifies the source with the X-definition</li>
	 * <li><tt>-data file file</tt>specifies input data or context</li>
	 * <li><tt>-debug </tt>sets the debug mode</li>
	 * <li><tt>-editInput </tt>enables to runEditor input data before execution
	 * </li>
	 * <li><tt>-displayResult </tt>displays result XML element</li>
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"Edit and run X-definition in graphical user interface.\n"+
" -p project_file | -v [switches] | [-c [switches] | -g [xml source]\n\n"+
" -p runs a project file\n"+
" -v compile X-definition and runs validation mode\n"+
" -c compile X-definition and runs construction mode\n"+
" -g generate X-definition and project from XML.\n"+
"  Optionally the XML source specification may follow.\n\n"+
"Switches\n"+
" -xdef source with X-definition (input file or XML data; it may be\n"+
"    specified more times)\n"+
" -data xml source (input file or XML data used for validation\n"+
"    mode and as the context for construction mode or for generation of\n"+
"    X-definition).\n"+
" -debug sets debugging mode when project is executed\n"+
" -editInput enables to edit input data before execution\n"+
" -displayResult displays result XML data\n";
		if (args == null || args.length == 0) {
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
				runEditor('p', args[1]);
			}
			return;
		}
		String src = 
"<Project>\n";
		ArrayList<String> xdefs = new ArrayList<String>();
		String dataPath = null;
		String debug = null;
		String editInput = null;
		String displayResult = null;
		int i = 1;
		char param;
		if ("-c".equals(arg)) {
			param = 'c';
		} else if ("-v".equals(arg)) {
			param = 'v';
		} else if ("-g".equals(arg)) { // generate X-definition
			param = 'g';
			String xml = "<A><B b=\"1\"/><B/></A>";
			i = args.length;
			if (i > 2) {
				System.err.println("Too many parameters: " + arg + "\n" + info);
				return;
			} else if (i == 2) {
				String x = args[1].trim();
				if (!x.isEmpty()) {
					xml = x;
				}
			}
			try {
				xml = editData("Input XML data", xml);
				Document d = KXmlUtils.parseXml(xml);
				Element w = d.createElement("W");
				w.setTextContent(xml);
				String s = KXmlUtils.nodeToString(w, true).substring(3);
				s = s.substring(0, s.length()-4);
				dataPath = s.trim();
				Element e = d.getDocumentElement();
				Element xd = GenXDef.genXdef(e);
				xd.setAttribute("name", "test");
				w.setTextContent(KXmlUtils.nodeToString(xd, true));
				s = KXmlUtils.nodeToString(w).substring(3);
				s = s.substring(0, s.length()-4).trim();
				xdefs.add(s);
//				editInput = "true";
				displayResult = "true";
				debug = "true";
			} catch (Exception ex) {
				ex.printStackTrace(System.err);
				return;
			}
		} else {
			System.err.println("Incorrect parameter: " + arg + "\n" + info);
			return;
		}
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
				if (editInput != null) {
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
		String msg = "";
		switch (param) {
			case 'c': { // create
				if (xdefs.isEmpty()) {
					xdefs.add(
"&lt;xd:def xmlns:xd=\"" + XDConstants.XDEF32_NS_URI + "\" name=\"test\" root=\"HTML\">\n" +
"&lt;HTML>\n" +
"  &lt;HEAD>&lt;TITLE> create \"Generated today message\"; &lt;/TITLE>&lt;/HEAD>\n" +
"  &lt;BODY>\n" +
"    &lt;h1>\n" +
"      create \"Hello! Today is \" + now().toString(\"EEEE, d. MMMM GG yyyy, hh:mm a.\");\n" +
"    &lt;/h1>\n" +
"    &lt;h2>\n" +
"      create \"This is an example of compose mode.\";\n" +
"    &lt;/h2>\n" +
"    &lt;i xd:script=\"*; create from('//i');\">\n" +
"      create from(\"@x\");\n" +
"      &lt;br/>\n" +
"    &lt;/i>\n" +
"  &lt;/BODY>\n" +
"&lt;/HTML>\n" +
"&lt;/xd:def>");
					if (displayResult == null) {
						displayResult = "true";
					}
				}
				if (dataPath == null) {
					debug = editInput = displayResult = "true";
					dataPath =
"&lt;x>\n  &lt;i x=\"Hello\"/>\n  &lt;i x=\"World!\"/>\n&lt;/x>";
				}
				src += "  <Execute Mode = \"construct\"";
				if (displayResult != null) {
					src += " DisplayResult = \"true\"";
				}
				src += ">\n";
				src += "    <Context";
				if (editInput!= null) {
					src += " Edit='true'";
				}
				src += ">" + dataPath + "</Context>\n";
				src += "  </Execute>\n";
				break;
			}
			case 'g':  // generate X-definition
			case 'v': { // validate
				if (xdefs.isEmpty()) {
					xdefs.add(
"&lt;xd:def xmlns:xd=\"" + XDConstants.XDEF32_NS_URI + "\" name=\"test\" root=\"root\">\n" +
"  &lt;root a=\"int();\" >\n" +
"    &lt;b xd:script=\"*\" >\n" +
"      ? string(2,3);\n" +
"    &lt;/b>\n" +
"  &lt;/root>\n" +
"&lt;/xd:def>");
					if (displayResult == null) {
						displayResult = "true";
					}
				}
				if (dataPath == null) {
					dataPath =
"&lt;root a=\"1\">\n  &lt;b>xyz&lt;/b>\n&lt;/root>";
					debug = editInput = displayResult = "true";
				}
				src += "  <Execute Mode = \"validate\"";
				if (displayResult != null) {
					src += " DisplayResult = \"true\"";
				}
				src += ">\n";
				src += "    <Input";
				if (editInput!= null) {
					src += " Edit='true'";
				}
				src += ">" + dataPath + "</Input>\n";
				src += "  </Execute>\n";
				break;
			}
		}
		if (!msg.isEmpty()) {
			System.err.println(msg + "\n" + info);
			return;
		}
		// Sources
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
			+ "\" Value = \"" 
			+ (debug == null ? XDConstants.XDPROPERTYVALUE_DEBUG_FALSE
				: XDConstants.XDPROPERTYVALUE_DEBUG_TRUE)
			+ "\"/>\n"+
"</Project>";
		runEditor(param, src);
	}
}