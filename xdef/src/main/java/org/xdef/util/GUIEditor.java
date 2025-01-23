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
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.xml.KXmlUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.xdef.XDBuilder;
import org.xdef.impl.GenXDef;
import org.xdef.sys.FUtils;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import org.xdef.xon.XonUtils;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;

/** Provides interactive editing and debugging of Xdefinition.
 * @author Vaclav Trojan
 */
public class GUIEditor extends GUIScreen {

	private static final Class[] URLPARAMS = new Class[]{URL.class};

	private static final XDPool PROJECTXDPOOL;
	static {
		String xdef = // Xdefinition of project description
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"Project\" name=\"GUI\">\n" +
"  <Project\n" +
"    Show=\"? enum('true','false'); /*if true file with project is displayed"
			+ " and editable.*/\">\n" +
"\n" +
"    <xd:mixed>\n" +
"<!-- \"XDefinition\" - items with the sources of Xdefinition -->\n" +
"      <XDefinition xd:script=\"+\">\n" +
"        string(); /*this can be a file, url or XML with Xdefinition source*/\n"+
"      </XDefinition>\n" +
"\n" +
"<!-- \"External\" - items to be added to the classpath -->\n" +
"      <External xd:script=\"*\">\n" +
"        file() || url(); /*classpath item (url or pathname)*/\n"+
"      </External>\n" +
"\n" +
"<!-- \"Property\" items are used to set properties for compiling and"
			+ " executing the project -->\n" +
"      <Property xd:script=\"*\"\n" +
"        Name=\"string();  /*property name*/\"\n" +
"        Value=\"string(); /*property value*/\" />\n" +
"\n" +
"<!-- \"Execute\" items are used to specify the compiled XDPool is executed\n" +
"     according to the specified parameters -->\n" +
"      <Execute xd:script=\"*;\"\n" +
"        XDName=\"? string(1, 1000); /*name of root Xdefinition (may be"
			+ " missing)*/\"\n" +
"        DataType=\"? enum('XML', 'JSON', 'INI'); /*type of processed data*/\"\n" +
"        Mode=\"? enum('construct', 'validate'); /*mode of process*/\"\n" +
"        DisplayResult=\"? enum('true', 'false');/*if true the result of"
			+ " process is displayed*/\" >\n" +
"\n" +
"        <xd:mixed>\n" +
"<!-- \"Var\" items are used to set variables to the Xdefinition"
			+ " processor -->\n" +
"          <Var xd:script=\"*\" Name=\"string(); /*name of variable*/\">\n" +
"            string(); /*value of variable*/\n" +
"          </Var>\n" +
"\n" +
"<!-- \"Context\" item is used to specify a context in the construction"
			+ " mode. -->\n" +
"          <Context xd:script=\"?\"\n" +
"            Edit=\"? enum('true', 'false'); /*if true the context can"
			+ " be edited*/\" >\n" +
"            string(); /*the file or XML data used as a context*/\n" +
"          </Context>\n" +
"\n" +
"<!-- \"Input\" item is used to specify the input data in the validation"
			+ " mode. -->\n" +
"          <Input xd:script=\"?\"\n" +
"            Edit=\"? enum('true', 'false'); /*if true the input data can be"
			+ " edited*/\">\n" +
"            string(); /*file with data*/\n" +
"          </Input>\n" +
"\n" +
"<!-- \"SaveResult\" item is used to specify if the result saved to"
			+ " the specified file. -->\n" +
"          <SaveResult xd:script=\"?\"\n" +
"            Indent=\"? enum('true', 'false'); /*if true the result"
			+ " is indented*/\"\n" +
"            Encoding=\"? string(); /*name of char encoding*/\"\n" +
"            File=\"file(); /*where to store result of process*/\" />\n" +
"        </xd:mixed>\n" +
"      </Execute>\n" +
"<!-- \"WorkDir\" item is used to specify the directory where are stored\n" +
"    the temporary files. If it is not specified a working directory\n" +
"    is created and deleted on exit of GUI editor. -->\n" +
"      <WorkDir xd:script=\"?;\">\n" +
"          file(); /*Directory where to store temporary data.*/\n" +
"      </WorkDir>\n" +
"    </xd:mixed>\n" +
"  </Project>\n" +
"</xd:def>";
		PROJECTXDPOOL = XDFactory.compileXD(null, xdef);
	}

	/** Create instance of the screen to display the sources.
	 * @param si source information.
	 */
	private GUIEditor(final XDSourceInfo si) {super(si);}

	/** Initialize menu bar
	 * @param text the text of the <code>JMenuItem</code>
	 */
	private void initMenuBar(final String text) {
		JMenu fileMenu = _menuBar.add(new JMenu("File (F10)"));
		fileMenu.getActionMap().remove("Compile");
		_sourceArea.getActionMap().remove("Compile");
		JMenuItem ji;
		// Save as... menu item
		ji = new JMenuItem("Save as...");
		ji.setAccelerator(KeyStroke.getKeyStroke("control S"));
		ji.addActionListener((ActionEvent e) -> {
			if (updateSourceItem()) {
				_sourceItem._saved = true;
			}
			saveSource(_sourceItem);
		});
		fileMenu.add(ji);
		if (text != null) {
			fileMenu.addSeparator();
			ji = new JMenuItem(text);
			ji.setAccelerator(KeyStroke.getKeyStroke("F9"));
			ji.addActionListener((ActionEvent e) -> {
				if (((JMenuItem)e.getSource()).isEnabled()) {
					updateSourceItem();
					_actionFinished = false;
					notifyFrame();
				}
			});
		}
		fileMenu.add(ji);
		fileMenu.addSeparator();
		// Exit menu item
		ji = new JMenuItem("Exit");
		ji.setMnemonic((int) 'X');
		ji.addActionListener((ActionEvent e) -> {
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
		});
		fileMenu.add(ji);
		// Kill project
		ji = new JMenuItem("Kill process");
		ji.setMnemonic((int) 'K');
		ji.addActionListener((ActionEvent e) -> {
			_sourceItem._changed = false;
			_frame.dispose();
			_kill = true;
		});
		fileMenu.add(ji);
		// Source position info
		_menuBar.add(_sourcePositionInfo, BorderLayout.EAST);
		_frame.setJMenuBar(_menuBar);
		_sourceArea.getActionMap().remove("Compile");
		if (text != null) {
			_sourceArea.getActionMap().put(text,
				new AbstractAction(text){
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

	/** Display object.
	 * @param err Array of reported errors and messages.
	 * @param msg Text of header.
	 * @param obj Object to be displayed/
	 * @param si source information.
	 * @param editable true if editing is allowed.
	 * @param text the text of the <code>JMenuItem</code>
	 * @return source item.
	 * @throws Exception if an error occurs.
	 */
	private XDSourceItem display(final ArrayReporter err,
		final String msg,
		final Object obj,
		final XDSourceInfo si,
		final boolean editable,
		final String text) throws Exception {
		_windowName = msg;
		_si = si;
		XDSourceItem xsi = new XDSourceItem(obj);
		_sources = _si.getMap();
		_sources.clear();
		String sourceId;
		if (obj == null) {
			throw new SRuntimeException(SYS.SYS036, "Object is null");
		}
		if (obj instanceof File) {
			sourceId = ((File) obj).getCanonicalPath();
		} else if (obj instanceof String) {
			sourceId = "STRING";
		} else if (obj instanceof URL) {
			URL u = SUtils.getExtendedURL(((URL) obj).toExternalForm());
			sourceId = u.toExternalForm();
		} else {
			sourceId = obj.getClass().getName();
		}
		_sources.put(sourceId, xsi);
		_sourceItem = null;
		initSourceMap();
		initSourceWindow();
		setInitialSource();
		initMenuBar(text);
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
		if (_kill) {
			throw new Error("Process killed by user");
		}
		return xsi;
	}

	/** Display (and optionally edit) string.
	 * @param msg Text of header
	 * @param workDir directory for temporary files.
	 * @param deleteOnExit if true the new file is set to be deleted on exit.
	 * @param s string displayed.
	 * @param si source information.
	 * @param editable true if editing is allowed.
	 * @return source item.
	 * @throws Exception if an error occurs.
	 */
	private static XDSourceItem displayString(final String msg,
		final File workDir,
		final boolean deleteOnExit,
		final String s,
		final XDSourceInfo si,
		final boolean editable) throws Exception {
		Object o = s;
		if (editable && workDir != null) {
			o = new File(genTemporaryFile(s,workDir,"result.tmp",deleteOnExit));
		}
		XDSourceItem xsi =
			new GUIEditor(si).display(null, msg, o, si, editable, null);
		Map<String, XDSourceItem> m = si.getMap();
		if (m.size() == 1) { //delete the file result.tmp if it was not saved
			if (!m.values().iterator().next()._saved) {
				new File(workDir, "result.tmp").delete();
			}
		}
		return xsi;
	}

	/** Display editable window with object (XML, JSON, text etc.).
	 * @param err ArrayReporter with reported errors and messages.
	 * @param msg Text of header.
	 * @param o Object with XML (may be Element, file etc.)
	 * @param si source information.
	 * @param text the text of the <code>JMenuItem</code>
	 * @return source item.
	 * @throws Exception if an error occurs.
	 */
	private static XDSourceItem editObject(final ArrayReporter err,
		final String msg,
		final Object o,
		final XDSourceInfo si,
		final String text) throws Exception {
		return new GUIEditor(si).display(err, msg, o, si, true, text);
	}

	/** Display and edit XMl given by the argument source.
	 * @param title title of window.
	 * @param source may be pathname of file, url or XML.
	 * @return source item of edited XML.
	 * @throws Exception if an error occurs.
	 */
	private static String editData(final String title,
		final String source) throws Exception {
		XDSourceInfo xi = new XDSourceInfo();
		Object o = source;
		String data = source.trim();
		String s = getSourceURL(data);
		File f = null;
		if (s != null) {
			URL u = new URL(s);
			String t = u.getFile();
			if (t != null) {
				f = new File(t);
				if (f.exists()) {
					o = f;
				}
			} else {
				f = new File(s);
				if (f.exists()) {
					o = f;
				} else if ((f = new File(data)).exists()) {
					o = f;
				} else {
					f = null;
					o = u;
				}
			}
		}
		XDSourceItem xsi = new XDSourceItem(o);
		xi.getMap().put(source, xsi);
		editObject(null, title, o, xi, null);
		if (xsi._saved || !source.equals(xsi._source)) {
			if (xsi._url != null && f != null) {
				if (new File(xsi._url.getFile()).getCanonicalFile().equals(
					f.getCanonicalFile())) {
					return data;
				}
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
	private static String getData(final Element e)
		throws Exception {
		if (e == null) {
			return null;
		}
		String data = e.getTextContent();
		if (data != null) {
			data = data.trim();
			if ("true".equals(e.getAttribute("Edit"))) {
				String s = editData("Input data", data);
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
			} catch (IOException ex) {}
		}
		return null;
	}

	/** Canonize project description.
	 * @param project Element with project description.
	 * @return canonized form of project description.
	 */
	private static Element canonizeProject(final Element project) {
		NodeList nl = project.getElementsByTagName("XDefinition");
		List<String> sources = new ArrayList<>();
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
		List<Element> ar = new ArrayList<>();
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
		ar = new ArrayList<>();
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

	/** Compare given element child elements in two elements.
	 * @param p1 first element.
	 * @param p2 second element.
	 * @param tagname name of child  elements to be compared.
	 * @return true if all child elements are equal.
	 */
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

	/** Compare two elements with project description.
	 * @param p1 first project.
	 * @param p2 second project.
	 * @return true if both projects are equal.
	 */
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

	@SuppressWarnings("unchecked")
	/** Run project with GUIEditor.
	 * @param param 'c' (compose) , 'v' (validate) or 'g' (generate)
	 * @param dataType 'x' (XML) or 'j' (XON/JSON)
	 * @param src source with the project.
	 */
	public static final void runEditor(final char param,
		final char dataType,
		final String src) {
		try {
			Element e;
			// Create element with project according to Xdefinition
			XDDocument pxd = PROJECTXDPOOL.createXDDocument();
			Element project = pxd.xparse(src, null);
			if ("true".equals(project.getAttribute("Show"))) {
				XDSourceInfo sinfo = new XDSourceInfo();
				Object o = src.charAt(0) == '<' ? src : new File(src);
				editObject(null, "Project", o, sinfo, null);
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
			// add classspath items
			nl = project.getElementsByTagName("External");
			for (int i = 0; i < nl.getLength(); i++) {
				e = (Element) nl.item(i);
				String s = e.getTextContent();
				File f = new File(s);
				try {
					URL u = f.exists() ? f.toURI().toURL() : new URL(s);
					URLClassLoader sysloader =
						(URLClassLoader) ClassLoader.getSystemClassLoader();
					Class sysclass = URLClassLoader.class;
					Method method = sysclass.getDeclaredMethod(
						"addURL", URLPARAMS);
					method.setAccessible(true);
					method.invoke(sysloader, new Object[]{u});
				} catch (IllegalAccessException | IllegalArgumentException
					| NoSuchMethodException | SecurityException
					| InvocationTargetException | MalformedURLException ex) {
					throw new RuntimeException("Incorrect ClassPath: " + s);
				}
			}

			// compile Xdefinitions
			XDPool xp = compileProject(project, props);
			// execute project
			executeProject(project, xp, props);

			if (param == 'g') { // project was generated
				editData("Generated project", new File(src).exists()
					? src : KXmlUtils.nodeToString(project, true));
			} else if (!compareProjects(
				project = canonizeProject(project), originalProject)) {
				// something changed in the project; so ask to save it
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
					} catch (IOException ex) {
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

	/** Compile project.
	 * @param project element with project.
	 * @param props properties.
	 * @throws Exception if an error occurs.
	 */
	private static XDPool compileProject(final Element project,
		final Properties props) throws Exception {
		// get Xdefinition sources
		NodeList nl = project.getElementsByTagName("XDefinition");
		try {
			String missingDefs = "";
				List<String> xdefs = new ArrayList<>();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				String t = e.getTextContent().trim();
				if (t != null && !t.isEmpty()) {
					if (t.charAt(0) == '<') {
						xdefs.add(t);
					} else { // file or grou of files
						String[] sources = SUtils.getSourceGroup(t);
						if (sources == null || sources.length == 0) {
							if (!missingDefs.isEmpty()) {
								missingDefs += '\n';
							}
							missingDefs += t;
						} else {
							for (String s: sources) {
								URL u = new URL(s);
								String pathname = u.getPath();
								File f = new File(pathname);
								if (f.exists() && f.isFile()) {
									xdefs.add(pathname);
								} else {
									if (!missingDefs.isEmpty()) {
										missingDefs += '\n';
									}
									missingDefs += pathname;
								}
							}
						}
					}
				}
			}
			if (!missingDefs.isEmpty()) {
				throw new RuntimeException("UNAVAILABLE XDEFINITION:\n\n"+
						missingDefs + "\nPLEASE CORRECT PROJECT DATA");
			}
			boolean changed = false;
			XDSourceInfo si;
			XDPool xp;
			for(;;) {
				ArrayReporter reporter = new ArrayReporter();
				XDBuilder builder = XDFactory.getXDBuilder(reporter, props);
				builder.setSource(xdefs.toArray(new String[0]));
				xp = builder.compileXD();
				si = xp.getXDSourceInfo();
				for (String x: si.getMap().keySet()) {
					XDSourceItem xsi = si.getMap().get(x);
					changed |= xsi._changed | xsi._saved;
				}
				if (changed) { //Update Xdefinitions elements
					updateXdefList(project, si);
				}
				if (reporter.errors()) {
					XDSourceItem xsi = null;
					for (XDSourceItem x: si.getMap().values()) {
						for (Report r: reporter) {
							if (x._url == null) {
								if (!missingDefs.isEmpty()) {
									missingDefs += '\n';
								}
								missingDefs += x._source;
							} else if (r.getModification().contains(
								x._url.toString())) {
								xsi = x;
								break;
							}
						}
						if (xsi != null) {
							break;
						}
					}
					if (!missingDefs.isEmpty()) {
						throw new RuntimeException(
							"UNAVAILABLE XDEFINITION:\n\n"+
								missingDefs + "\nPLEASE CORRECT PROJECT DATA");
					}
					if (xsi == null) {
						Iterator<XDSourceItem> it=
							si.getMap().values().iterator();
						if (it.hasNext()) {
							xsi = it.next();
						} else {
							throw new RuntimeException(
								reporter.printToString());
						}
					}
					GUIEditor ge = new GUIEditor(si);
					ge.display(reporter,
						"Error in Xdefinition", xsi._url, si, true, null);
					ge.closeEdit();
					reporter.clear();
					if (!changed) {
						return xp;
					}
				} else {
					return xp;
				}
			}
		} catch (Exception ex) {throw new RuntimeException(ex);}
	}

	@SuppressWarnings("unchecked")
	/** Execute project.
	 * @param project element with the project.
	 * @param xp compiled XDPool.
	 * @param props properties.
	 * @throws Exception if an error occurs.
	 */
	private static void executeProject(final Element project,
		final XDPool xp,
		final Properties props) throws Exception {
		Element e;
		NodeList nl;
		File workDir;
		boolean deleteOnExit;

		// create work directory
		nl = project.getElementsByTagName("WorkDir");
		File f = null;
		if (nl.getLength() != 0) {
			e = (Element) nl.item(0);
			f = new File(e.getTextContent());
		}
		workDir = getTempDir(f);
		deleteOnExit = !workDir.equals(f);

		// execute project
		nl = project.getElementsByTagName("Execute");
		XDSourceInfo si = xp.getXDSourceInfo();
		for (int i = 0; i < nl.getLength(); i++) {
			Element exe = (Element) nl.item(i);
			// get name of Xdefinition
			String xdName = exe.getAttribute("XDName").trim();
			// get data type
			String t = exe.getAttribute("DataType");
			char type = "JSON".equals(t) ? 'j' : "INI".equals(t) ? 'i' : 'x';
			// get input data
			e = KXmlUtils.firstElementChild(exe, "Input");
			String data = getData(e);
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
			String context = getData(e);
			if (context != null) {
				xd.setXDContext(context);
			}
			ArrayReporter reporter = new ArrayReporter();
			// set properties
			xd.setProperties(props);
			// set stdout
			StringWriter strw = new StringWriter();
			XDOutput stdout =
				XDFactory.createXDOutput(new PrintWriter(strw), false);
			xd.setStdOut(stdout);
			String mode = exe.getAttribute("Mode");
			Object result;
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
				switch (type) {
					case 'i':
						result = xd.jcreate(name, reporter);
						break;
					case 'j':
						result = xd.jcreate(name, reporter);
						break;
					default:
						// type = x
						result = xd.xcreate(new QName(uri, name), reporter);
						break;
				}
			} else {  // run validation mode
				switch (type) {
					case 'i':
						result = xd.iparse(data, reporter);
						break;
					case 'j':
						result = xd.jparse(data, reporter);
						break;
					default:
						// type = x
						result = xd.xparse(data, reporter);
						break;
				}
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
					f = new File(new URL(data).getFile());
					XDSourceItem xsi = !f.isFile() || !f.exists()
						? editObject(reporter, "ERROR:", data, si,
							"Input data changed, run again?")
						: editObject(reporter, "ERROR:", f, si, null);
					if (xsi._saved && xsi._url != null) {
						if (JOptionPane.showConfirmDialog(null,
							"Input data changed, run again?",
							null, JOptionPane.OK_CANCEL_OPTION) == 0) {
							e = KXmlUtils.firstElementChild(exe, "Input");
							e.setTextContent(xsi._url.toExternalForm());
							i--;
							continue;
						}
					}
				} else { // error, display result, not editable
					displayString("ERROR:", null, false,
						reporter.printToString(), si, false);
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
					switch (type) {
						case 'i': {
							String s = XonUtils.toIniString(
								(Map<String, Object>) result);
							SUtils.writeString(new File(name), s, "ASCII");
							break;
						}
						case 'j': {
							String s = XonUtils.toJsonString(result, indent);
							SUtils.writeString(new File(name), s, "UTF-8");
							break;
						}
						default:
							// type = 'x'
							KXmlUtils.writeXml(new File(name), encoding,
								(Element) result, indent, true);
					}
				} catch (IOException | SException ex) {
//					//GUIEditor can't write XML data to file &{0}
//					JOptionPane.showMessageDialog(null,
//						Report.error(XDEF.XDEF851,name).toString());
				}
			}
			if ("true".equals(exe.getAttribute("DisplayResult"))) {
				String s;
				if (result == null) {
					s = "Result of process is null\n";
				} else {
					s = "=== Result of process ===\n";
					switch (type) {
						case 'i':
							// display as INI
							s += XonUtils.toIniString(
								(Map<String, Object>) result);
							break;
						case 'j':
							// display as JSON
							s += XonUtils.toJsonString(result, true);
							break;
						default:
							// display result XML
							s += KXmlUtils.nodeToString((Element) result, true);
					}
				}
				if (!strw.toString().isEmpty()) {
					s += s.isEmpty() ? "" : "\n";
					s += "\n=== System output ===\n" + strw.toString();
				}

				if (!s.isEmpty()) {// display result, allow editing
					displayString("Result of processing:",
						workDir, deleteOnExit, s, si, true);
				}
			}
		}
	}

	private static String genTemporaryFile(final String data,
		final File dir,
		final String name,
		final boolean deleteOnExit) {
		if (dir != null && dir.exists() && dir.isDirectory()) {
			try {
				File f = new File(dir, name);
				if (deleteOnExit) {
					f.deleteOnExit();
				}
				SUtils.writeString(f, data);
				return deleteOnExit ? f.getAbsolutePath() : f.getPath();
			} catch (SException ex) {}
		}
		throw new RuntimeException("Can't create file " + name +
			"to work direcory "+ dir);
	}

	private static File getTempDir(final File dir) {
		try {
			if (dir != null) {
				if (dir.exists() && dir.isDirectory()) {
					return dir;
				}
			} else {
				File tempDir = File.createTempFile("GUI", ".tmp");
				tempDir.delete();
				tempDir.mkdirs();
				tempDir.deleteOnExit();
				tempDir = new File(tempDir, "GUITemp");
				tempDir.mkdirs();
				tempDir.deleteOnExit();
				return tempDir;
			}
		} catch (IOException ex) {}
		throw new RuntimeException("Incorrect temp directory");
	}

	/** String with command line information. */
	private static final String INFO =
"Edit and run Xdefinition in graphical user interface.\n\n"+
"Command line arguments:\n"+
" -p project_file | -v [switches] |\n"+
" -c [switches] | -g [xml source] [-workDir dird]\n"+
"\n"+
" -p run a project file\n"+
" -v compile Xdefinition and runs validation mode\n"+
" -c compile Xdefinition and runs construction mode\n"+
" -g generate Xdefinition and project from input data (see switch -data).\n\n"+
"Switches:\n"+
" -xdef source with Xdefinition (input file or data; it may be\n"+
"    specified more times)\n"+
" -external list of classpath items with external resources (may be filenames\n"+
"  or urls).\n"+
" -format format of processed data (XML or JSON; default is XML)\n"+
" -data source (input file or data used for validation mode and as\n"+
"    the context for construction mode or for generation of Xdefinition).\n"+
" -debug sets debugging mode when project is executed\n"+
" -editInput enables to edit input data before execution\n"+
" -displayResult displays result data\n"+
" -workDir directory where to store created data. This switch is optional;\n"+
"    if not specified the work directory is created and deleted on exit.";

	/** Call generation of a collection of Xdefinitions from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><i>-p </i>file with the project
	 * <li><i>-v [switches]</i>run validation mode
	 * <li><i>-c [switches]</i>run construction mode
	 * <li><i>-g [XML source]</i>Generate Xdefinition form XML data
	 * </ul>
	 * Switches:
	 * <ul>
	 * <li><i>-xdef file </i>specifies the source with the Xdefinition
	 * <li><i>-data file</i>specifies input data or context
	 * <li><i>-debug </i>sets the debug mode
	 * <li><i>-editInput </i>enables to runEditor input data before execution
	 * <li><i>-displayResult </i>displays result XML element
	 * </ul>
	 */
	public static void main(String... args) {
		if (args == null || args.length == 0) {
			System.err.println("No parameters.\n" + INFO);
			return;
		}
		String arg = args[0];
		if ("-h".equals(arg)) {
			System.out.println(INFO);
			return;
		}
		if ("-p".equals(arg)) {
			switch (args.length) {
				case 1:
					System.err.println("Missing parameter with project\n"+INFO);
					break;
				case 2:
					runEditor('p', (char) 0, args[1]);
					break;
				default:
					System.err.println("More parameters not allowed here\n"
						+ INFO);
			}
			return;
		}
		List<String> xdefs = new ArrayList<>();
		String dataPath = null;
		boolean wasDataPath = false;
		boolean wasXDefinition = false;
		String debug = null;
		String editInput = null;
		String displayResult = null;
		File workDir = null;
		List<String> external = new ArrayList<>();
		int i = 1;
		char param;
		char format = (char) 0;
		if (null == arg) {
			System.err.println("Incorrect parameter: " + arg + "\n" + INFO);
			return;
		} else switch (arg) {
			case "-c":
				param = 'c';
				break;
			case "-v":
				param = 'v';
				break;
			case "-g":
				// generate Xdefinition
				param = 'g';
				if (args.length >= 2) {
					String x = args[1].trim();
					if (!x.isEmpty() && x.charAt(0)!= '-') {
						i++;
						dataPath = x;
					}
				}	break;
			default:
				System.err.println("Incorrect parameter: " + arg + "\n" + INFO);
				return;
		}
		while (i < args.length) {
			arg = args[i++];
			switch(arg) {
				case "-xdef":
					if (param == 'g') {
						System.err.println(
							"Parameter -xdef not allowed with -g\n" + INFO);
						return;
					}
					wasXDefinition = true;
					xdefs.add(args[i++]);
					while (i < args.length && !args[i].startsWith("-")) {
						xdefs.add(args[i++]);
					}
					continue;
				case "-external":
					wasXDefinition = true;
					external.add(args[i++]);
					while (i < args.length && !args[i].startsWith("-")) {
						external.add(args[i++]);
					}
					continue;

				case "-format":
					if (format != 0) {
						System.err.println(
							"Redefinition of format parameter -format\n"+INFO);
						return;
					}
					String s = args[i++];
					if (s.equalsIgnoreCase("JSON")) {
						format = 'j';
					} else if (s.equalsIgnoreCase("XML")) {
						format = 'x';
					} else if (s.equalsIgnoreCase("INI")) {
						format = 'i';
					} else {
						System.err.println(
							"Incorrect parameter -format\n" + INFO);
						return;
					}
					continue;
				case "-data":
					if (wasDataPath) {
						System.err.println(
							"Redefinition of parameter \"-data\"\n" + INFO);
						return;
					}
					wasDataPath = true;
					dataPath = args[i++].trim();
					continue;
				case "-debug":
					if (debug != null) {
						System.err.println(
							"Redefinition of parameter \"-debug\"\n" + INFO);
						return;
					}
					debug = "true";
					continue;
				case "-editInput":
					if (editInput != null) {
						System.err.println(
							"Redefinition of parameter \"-editInput\"\n"+INFO);
						return;
					}
					editInput = "true";
					continue;
				case "-displayResult":
					if (displayResult != null) {
						System.err.println(
							"Redefinition of parameter \"-displayResult\"\n"
								+INFO);
						return;
					}
					displayResult = "true";
					continue;
				case "-workDir":
					if (workDir != null) {
						System.err.println(
							"Redefinition of parameter \"-tempDir\"\n" +INFO);
						return;
					}
					workDir = new File(args[i++]);
			}
		}
		String msg = "";
		if (format == 0) {
			format = 'x'; // default is XML
		}
		File f = workDir;
		workDir = getTempDir(f);
		boolean deleteOnExit = !workDir.equals(f);
		String src =
			"<Project Show=\"true\">\n"+
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
			+ "\"/>\n";
		switch (param) {
			case 'c': { // create
				if (xdefs.isEmpty()) {
					xdefs.add(genTemporaryFile(
"<xd:def xmlns:xd=\"" + XDConstants.XDEF42_NS_URI
	+ "\" name=\"test\" root=\"HTML\">\n" +
"<HTML>\n" +
"  <HEAD><TITLE>"
		+ " create \"Generated today message\"; </TITLE></HEAD>\n" +
"  <BODY>\n" +
"    <h1>\n" +
"      create \"Hello! Today is"
	+ " \"+now().toString(\"EEEE,d. MMMM GG yyyy, hh:mm a.\");\n" +
"    </h1>\n" +
"    <h2>\n" +
"      create \"This is an example of compose mode.\";\n" +
"    </h2>\n" +
"    <i xd:script=\"*; create from('//i');\">\n" +
"      create from(\"@x\");\n" +
"      <br/>\n" +
"    </i>\n" +
"  </BODY>\n" +
"</HTML>\n" +
"</xd:def>", workDir, "xdef.xml", deleteOnExit));
				}
				if (!wasDataPath) {
					dataPath = genTemporaryFile(
"<x>\n  <i x=\"Hello\"/>\n  <i x=\"World!\"/>\n</x>",
						workDir, "data.xml", deleteOnExit);
				}
				src +=
"  <Execute Mode = \"construct\" DisplayResult = \"true\">\n" +
"    <Context Edit='true'>\n";
				src += dataPath + "\n</Context>\n";
				src += "  </Execute>\n";
				break;
			}
			case 'g':  // generate Xdefinition
				try {
					if (!wasDataPath) {
						dataPath = genTemporaryFile(
"<!-- This is just an example of XML data. You can modify it. -->\n" +
"<root attr = \"123\">\n" +
"  <a>text</a>\n" +
"  <a/>\n" +
"</root>", workDir, "data.xml", deleteOnExit);
					}
					dataPath = editData("Input data", dataPath);
					Document d = KXmlUtils.parseXml(dataPath);
					Element w = d.createElement("W");
					w.setTextContent(dataPath);
					String s = KXmlUtils.nodeToString(w, true).substring(3);
					s = s.substring(0, s.length()-4);
					dataPath = s.trim();
					Element e = d.getDocumentElement();
					Element xd = GenXDef.genXdef(e);
					xd.setAttribute("name", "test");
					s = KXmlUtils.nodeToString(xd, true);
					xdefs.add(genTemporaryFile(
						s, workDir, "xdef.xml", deleteOnExit));
					editInput = displayResult = "true";
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			case 'v': { // validate
				if (xdefs.isEmpty()) {
					xdefs.add(genTemporaryFile(
"<xd:def xmlns:xd=\"" + XDConstants.XDEF42_NS_URI +
	"\" name=\"test\" root=\"root\">\n" +
(format == 'x'
? "  <root a=\"int();\" >\n" +
"    <b xd:script=\"*\" >\n" +
"      ? string();\n" +
"    </b>\n" +
"  </root>\n"
: format == 'j'
? "<xd:json name=\"root\">\n"+
"{\"a\": [\"occurs 3 int();\"]}\n"+
"</xd:json>\n"
: "<xd:ini name=\"root\">\n"+
"[\n"+
"  [\"occurs + string();\"]\n"+
"]\n"+
"</xd:ini>\n") +
"</xd:def>", workDir, "xdef.xml", deleteOnExit));
					if (displayResult == null) {
						displayResult = "true";
					}
				}
				if (dataPath == null) {
					switch (format) {
						case 'i':
							dataPath = genTemporaryFile("a=1\nb=2",
								workDir, "data.ini", deleteOnExit);
							break;
						case 'x':
							dataPath = genTemporaryFile("<root a=\"123\" >\n" +
"  <b>text</b>\n" +
"  <b/>\n" +
"</root>", workDir, "data.xml", deleteOnExit);
							break;
						case 'j':
							dataPath = genTemporaryFile("{\"a\": [1, 2, 3]}",
								workDir, "data.json", deleteOnExit);
							break;
						default:
							break;
					}
					editInput = displayResult = "true";
				}
				src += "  <Execute Mode=\"validate\"";
				if (format == 'i') {
					src += " DataType=\"INI\"";
				} else if (format == 'j') {
					src += " DataType=\"JSON\"";
				}
				if (displayResult != null) {
					src += " DisplayResult=\"true\"";
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
			System.err.println(msg + "\n" + INFO);
			return;
		}
		// Sources
		for (String x: xdefs) {
			src += "  <XDefinition>" + x + "</XDefinition>\n";
		}
		// External items added to classPath
		for (String x: external) {
			f = new File(x);
			if (f.exists()) {
				src += "  <External>" + x + "</External>\n";
				continue;
			} else {
				try {
					src +=  "  <External>"
						+ new URL(x).toExternalForm()
						+ "</External>\n";
					continue;
				} catch (MalformedURLException ex) {}
			}
			throw new RuntimeException("Incorrect classpath: " + x);
		}
		if (!deleteOnExit) { // work directory was specified
			String s = SUtils.modifyString(workDir.getPath(), "&", "&amp;");
			src += "  <WorkDir>" + s + "</WorkDir>\n";
		}
		src += "</Project>";
		// run generated project
		runEditor(param, format,
			genTemporaryFile(src, workDir, "project.xml", deleteOnExit));
		if (deleteOnExit) {
			JFileChooser jf = new JFileChooser(new File("."));
			jf.setDialogTitle("Do you want to save the created project?");
			jf.setToolTipText("Save THE PROJECT to a directory");
			jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int retval = jf.showSaveDialog(null);
			jf.setEnabled(false);
			if (retval == JFileChooser.APPROVE_OPTION) {
				try {
					File dir = jf.getSelectedFile();
					int ndx1, ndx2;
					ndx1 = src.indexOf("<XDefinition");
					if (!wasXDefinition && ndx1 >= 0) {
						ndx1 = src.indexOf('>', ndx1 + 6);
						ndx2 = src.indexOf("</XDefinition");
						src = src.substring(0, ndx1 + 1)
							+ new File(dir, "xdef.xml").getAbsolutePath()
							+ src.substring(ndx2);
					}
					FUtils.xcopy(workDir.listFiles(), dir, true, new String[0]);
					SUtils.writeString(new File(dir, "project.xml"),
						src, "UTF-8");
					workDir = dir;
				} catch (SException ex) {
					JOptionPane.showMessageDialog(null,//Can't write
						Report.error(SYS.SYS036,"Can't write data to file: "
							+ jf.getSelectedFile() + "\n" + ex));
				}
			}
		}
		if (JOptionPane.showConfirmDialog(null,
			"Run created project", "Do you want to run created project?",
			JOptionPane.YES_NO_OPTION) == 0) {
			runEditor('p', (char)0, new File(workDir, "project.xml").getPath());
		}
	}
}