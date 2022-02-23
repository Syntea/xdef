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
import org.xdef.impl.GenXDef;
import org.xdef.xon.XonUtil;
import org.xdef.sys.SThrowable;
import org.xdef.sys.SUtils;

/** Provides interactive editing and debugging of X-definitions.
 * @author Vaclav Trojan
 */
public class GUIEditor extends GUIScreen {

	/** Create instance of the screen to display the sources.
	 * @param si source information.
	 */
	private GUIEditor(final XDSourceInfo si) {super(si);}

	private static final XDPool PROJECTXDPOOL;

	static {
		String xdef = // X-definition of project description
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.1\" root=\"Project\">\n" +
"  <Project\n" +
"    Show=\"? enum('true','false'); /*if true file with project is displayed"
			+ " and editable.*/\">\n" +
"\n" +
"    <xd:mixed>\n" +
"<!-- \"XDefinition\" - items with the sources of X-definitions -->\n" +
"      <XDefinition xd:script=\"+\">\n" +
"        string(); /*this can be a file, url or XML with X-definition source*/\n"+
"      </XDefinition>\n" +
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
"        XDName=\"? string(1, 1000); /*name of root X-definition (may be"
			+ " missing)*/\"\n" +
"        DataType=\"? enum('XML', 'JSON', 'INI'); /*type of processed data*/\"\n" +
"        Mode=\"? enum('construct', 'validate'); /*mode of process*/\"\n" +
"        DisplayResult=\"? enum('true', 'false');/*if true the result of"
			+ " process is displayed*/\" >\n" +
"\n" +
"        <xd:mixed>\n" +
"<!-- \"Var\" items are used to set variables to the X-definition"
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
"<!-- \"TempDir\" item is used to specify the directory where are stored\n" +
"    the temporary files. If it is not specified a temporary directory\n" +
"    is created and deleted on exit of GUI editor. -->\n" +
"      <TempDir xd:script=\"?;\">\n" +
"          file(); /*Directory where to store temporary data.*/\n" +
"      </TempDir>\n" +
"    </xd:mixed>\n" +
"  </Project>\n" +
"</xd:def>";
		PROJECTXDPOOL = XDFactory.compileXD(null, xdef);
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
		ji.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (updateSourceItem()) {
					_sourceItem._saved = true;
				}
				saveSource(_sourceItem);
			}
		});
		fileMenu.add(ji);
		if (text != null) {
			fileMenu.addSeparator();
			ji = new JMenuItem(text);
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
	 * @throws Exception if an error occurs.
	 */
	private void display(final ArrayReporter err,
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
	}

	/** Display (and optionally edit) string.
	 * @param msg Text of header
	 * @param tempDir directory for temporary files.
	 * @param deleteOnExit if true the new file is set to be deleted on exit.
	 * @param s string displayed.
	 * @param si source information.
	 * @param editable true if editing is allowed.
	 * @throws Exception if an error occurs.
	 */
	private static void displayString(final String msg,
		final File tempDir,
		final boolean deleteOnExit,
		final String s,
		final XDSourceInfo si,
		final boolean editable) throws Exception {
		Object o = s;
		if (editable && tempDir != null) {
			o = new File(genTemporaryFile(s,
				tempDir, "result.tmp", deleteOnExit, "UTF-8"));
		}
		new GUIEditor(si).display(null, msg, o, si, editable, null);
	}

	/** Display editable window with XML.
	 * @param err ArrayReporter with reported errors and messages.
	 * @param msg Text of header.
	 * @param o Object with XML (may be Element, file etc.)
	 * @param si source information.
	 * @param text the text of the <code>JMenuItem</code>
	 * @throws Exception if an error occurs.
	 */
	private static void editXml(final ArrayReporter err,
		final String msg,
		final Object o,
		final XDSourceInfo si,
		final String text) throws Exception {
		new GUIEditor(si).display(err, msg, o, si, true, text);
	}

	/** Display and edit XMl given by the argument source.
	 * @param title title of window.
	 * @param source may be pathname of file, url or XML.
	 * @return source item of edited XML.
	 * @throws Exception if an error occurs.
	 */
	private static String editData(final String title,
		final String source) throws Exception {
		String data = source;
		XDSourceInfo xi = new XDSourceInfo();
		File f = new File(source);
		Object o = f.exists() ? f : data;
		XDSourceItem xsi = new XDSourceItem(o);
		xi.getMap().put(data, xsi);
		ArrayReporter rep = new ArrayReporter();
		editXml(rep, title, o, xi, null);
		xsi = xi.getMap().values().iterator().next();
		if (xsi._saved || !source.equals(xsi._source)) {
			if (xsi._url != null) {
				if (f.getCanonicalFile().equals(
					new File(xsi._url.getFile()).getCanonicalFile())) {
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
			} catch (Exception ex) {}
		}
		return null;
	}

	/** Canonize project description.
	 * @param project Element with project description.
	 * @return canonized form of project description.
	 */
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
			// compile X-definitions
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
					} catch (Exception ex) {
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
		// get X-definition sources
		NodeList nl = project.getElementsByTagName("XDefinition");
		try {
			ArrayList<String> xdefs = new ArrayList<String>();
			for (int i = 0; i < nl.getLength(); i++) {
				Element e = (Element) nl.item(i);
				String t = e.getTextContent().trim();
				if (t != null && !t.isEmpty()) {
					xdefs.add(t);
				}
			}
			XDPool xp = XDFactory.compileXD(props,
				xdefs.toArray(new String[xdefs.size()]));
			XDSourceInfo si = xp.getXDSourceInfo();
			boolean changed = false;
			for (String x: si.getMap().keySet()) {
				XDSourceItem xsi = si.getMap().get(x);
				changed |= xsi._changed | xsi._saved;
			}
			if (changed) {
				updateXdefList(project, si); //Update X-definitions elements
			}
			return xp;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
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
		File tempDir;
		boolean deleteOnExit;

		// create temporary directory
		nl = project.getElementsByTagName("TempDir");
		File f = null;
		if (nl.getLength() != 0) {
			e = (Element) nl.item(0); 
			f = new File(e.getTextContent());
		}
		tempDir = getTempDir(f);
		deleteOnExit = !tempDir.equals(f);

		// execute project
		nl = project.getElementsByTagName("Execute");
		XDSourceInfo si = xp.getXDSourceInfo();
		for (int i = 0; i < nl.getLength(); i++) {
			Element exe = (Element) nl.item(i);
			// get name of X-definition
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
				if (type == 'i') {
					result = xd.jcreate(name, reporter);
				} else if (type == 'j') {
					result = xd.jcreate(name, reporter);
				} else { // type = x
					result = xd.xcreate(new QName(uri, name), reporter);
				}
			} else {  // run validation mode
				if (type == 'i') {
					result = xd.iparse(data, reporter);
				} else if (type == 'j') {
					result = xd.jparse(data, reporter);
				} else { // type = x
					result = xd.xparse(data, reporter);
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
					if (type == 'i') {
						String s =
							XonUtil.toIniString((Map<String, Object>) result);
						SUtils.writeString(new File(name), s, "ASCII");
					} else if (type == 'j') {
						String s = XonUtil.toJsonString(result, indent);
						SUtils.writeString(new File(name), s, "UTF-8");
					} else { // type = 'x'
						KXmlUtils.writeXml(new File(name), encoding,
							(Element) result, indent, true);
					}
				} catch (Exception ex) {
					//GUIEditor can't write XML data to file &{0}
					JOptionPane.showMessageDialog(null,
						Report.error(XDEF.XDEF851,name).toString());
				}
			}
			if ("true".equals(exe.getAttribute("DisplayResult"))) {
				String s;
				if (type == 'i') { // display as INI
					s = XonUtil.toIniString((Map<String, Object>) result);
				} else if (type == 'j') { // display as JSON
					s = XonUtil.toJsonString(result, true);
				} else {// display result XML
					s = result == null
						? "" : KXmlUtils.nodeToString((Element) result, true);
				}
				if (!strw.toString().isEmpty()) {
					s += s.isEmpty() ? "" : "\n\n";
					s += "=== System.out ===\n" + strw.toString();
				}
				if (!s.isEmpty()) {// display result, allow editing
					displayString("Result of processing:",
						tempDir, deleteOnExit, s, si, true);
				}
			}
		}
	}

	private static String genTemporaryFile(final String data,
		final File dir,
		final String name,
		final boolean deleteOnExit,
		final String charset) {
		if (dir != null && dir.exists() && dir.isDirectory()) {
			try {
				File f = new File(dir, name);
				if (deleteOnExit) {
					f.deleteOnExit();
				}
				SUtils.writeString(f, data);
				return deleteOnExit ? f.getAbsolutePath() : f.getPath();
			} catch (Exception ex) {}
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
				File tempDir =tempDir = File.createTempFile("GUI", ".tmp");
				tempDir.delete();
				tempDir.mkdirs();
				tempDir.deleteOnExit();
				tempDir = new File(tempDir, "GUITemp");
				tempDir.mkdirs();
				tempDir.deleteOnExit();
				genTemporaryFile(".", tempDir, "~.~", true, "UTF-8");
				return tempDir;
			}
		} catch (Exception ex) {}
		throw new RuntimeException("Incorrect temp directory");
	}

	/** Call generation of a collection of X-definitions from a command line.
	 * @param args array with command line arguments:
	 * <ul>
	 * <li><i>-p </i>file with the project
	 * <li><i>-v [switches]</i>run validation mode
	 * <li><i>-c [switches]</i>run construction mode
	 * <li><i>-g [XML source]</i>Generate X-definition form XML data
	 * </ul>
	 * Switches:
	 * <ul>
	 * <li><i>-xdef file </i>specifies the source with the X-definition
	 * <li><i>-data file</i>specifies input data or context
	 * <li><i>-debug </i>sets the debug mode
	 * <li><i>-editInput </i>enables to runEditor input data before execution
	 * <li><i>-displayResult </i>displays result XML element
	 * </ul>
	 */
	public static void main(String... args) {
		final String info =
"Edit and run X-definition in graphical user interface.\n\n"+
"Command line arguments:\n"+
" -p project_file | -v [switches] |\n"+
" -c [switches] | -g [xml source] [-workDir]\n"+
"\n"+
" -p run a project file\n"+
" -v compile X-definition and runs validation mode\n"+
" -c compile X-definition and runs construction mode\n"+
" -g generate X-definition and project from input data (optionally follows\n"+
"  the source file name may follow).\n\n"+
"Switches:\n"+
" -xdef source with X-definition (input file or data; it may be\n"+
"    specified more times)\n"+
" -format specification of data format XML or JSON (default XML) \n"+
" -data source (input file or data used for validation mode and as\n"+
"    the context for construction mode or for generation of X-definition).\n"+
" -debug sets debugging mode when project is executed\n"+
" -editInput enables to edit input data before execution\n"+
" -displayResult displays result data\n"+
" -workDir directory where to store temporary data. This switch is required\n"+
"    if a data item is generated.\n";
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
			} else if (args.length == 2) {
				runEditor('p', (char) 0, args[1]);
			} else {
				System.err.println("More parameters not allowed here\n" + info);
			}
			return;
		}
		ArrayList<String> xdefs = new ArrayList<String>();
		String dataPath = null;
		String debug = null;
		String editInput = null;
		String displayResult = null;
		File tempDir = null;
		int i = 1;
		char param;
		char format = (char) 0;
		if ("-c".equals(arg)) {
			param = 'c';
		} else if ("-v".equals(arg)) {
			param = 'v';
		} else if ("-g".equals(arg)) { // generate X-definition
			param = 'g';
			if (args.length >= 2) {
				String x = args[1].trim();
				if (!x.isEmpty() && x.charAt(0)!= '-') {
					dataPath = x;
					i = i++;
				}
			}
		} else {
			System.err.println("Incorrect parameter: " + arg + "\n" + info);
			return;
		}
		while (i < args.length) {
			arg = args[i++];
			switch(arg) {
				case "-xdef":
					xdefs.add(args[i++]);
					while (i < args.length && !args[i].startsWith("-")) {
						xdefs.add(args[i++]);
					}
					continue;
				case "-XML":
					if (format != 0) {
						System.err.println(
							"Redefinition of format parameter\n" + info);
						return;
					}
					format = 'x';
					continue;
				case "-format":
					if (format != 0) {
						System.err.println(
							"Redefinition of format parameter -format\n"+info);
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
							"Incorrect parameter -format\n" + info);
						return;
					}
					continue;
				case "-data":
					if (dataPath != null) {
						System.err.println(
							"Redefinition of parameter \"-data\"\n" + info);
						return;
					}
					dataPath = args[i++];
					continue;
				case "-debug":
					if (debug != null) {
						System.err.println(
							"Redefinition of parameter \"-debug\"\n" + info);
						return;
					}
					debug = "true";
					continue;
				case "-editInput":
					if (editInput != null) {
						System.err.println(
							"Redefinition of parameter \"-editInput\"\n"+info);
						return;
					}
					editInput = "true";
					continue;
				case "-displayResult":
					if (displayResult != null) {
						System.err.println(
							"Redefinition of parameter \"-displayResult\"\n"
								+info);
						return;
					}
					displayResult = "true";
					continue;
				case "-tempDir":
					if (tempDir != null) {
						System.err.println(
							"Redefinition of parameter \"-tempDir\"\n" +info);
						return;
					}
					tempDir = new File(args[i++]);
					continue;
			}
		}
		String msg = "";
		if (format == 0) {
			format = 'x'; // default is XML
		}
		File f = tempDir;
		tempDir = getTempDir(f);
		boolean deleteOnExit = !tempDir.equals(f);
		String src = "<Project>\n"+
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
"&lt;xd:def xmlns:xd=\"" + XDConstants.XDEF41_NS_URI
	+ "\" name=\"test\" root=\"HTML\">\n" +
"&lt;HTML>\n" +
"  &lt;HEAD>&lt;TITLE>"
		+ " create \"Generated today message\"; &lt;/TITLE>&lt;/HEAD>\n" +
"  &lt;BODY>\n" +
"    &lt;h1>\n" +
"      create \"Hello! Today is"
	+ " \"+now().toString(\"EEEE,d. MMMM GG yyyy, hh:mm a.\");\n" +
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
"&lt;/xd:def>", tempDir, "xdef", deleteOnExit, "UTF-8"));
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
				try {
					if (dataPath == null) {
						dataPath = genTemporaryFile(
"<!-- This is just an example of XML data. You can modify it. -->\n" +
"<root attr = \"123\">\n" +
"  <a>text</a>\n" +
"  <a/>\n" +
"</root>", tempDir, "data.xml", deleteOnExit, "UTF-8");
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
					xdefs.add(genTemporaryFile(s,
						tempDir, "xdef.xml", deleteOnExit, "UTF-8"));
					displayResult = "true";
					debug = "true";
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			case 'v': { // validate
				if (xdefs.isEmpty()) {
					xdefs.add(genTemporaryFile(
"&lt;xd:def xmlns:xd=\""
	+ XDConstants.XDEF41_NS_URI + "\" name=\"test\" root=\"root\">\n" +
(format == 'x'
? "  &lt;root a=\"int();\" >\n" +
"    &lt;b xd:script=\"*\" >\n" +
"      ? string();\n" +
"    &lt;/b>\n" +
"  &lt;/root>\n"
: format == 'j'
? "&lt;xd:json name=\"root\">\n"+
"{\"a\": [\"occurs 3 int(;)\"]}\n"+
"&lt;/xd:json>\n"
: "&lt;xd:ini name=\"root\">\n"+
"[\n"+
" [\"occurs + string();\"]\n"+
"]\n"+
"&lt;/xd:ini>\n") +
"&lt;/xd:def>", tempDir, "xdef.xml", deleteOnExit, "UTF-8"));
					if (displayResult == null) {
						displayResult = "true";
					}
				}
				if (dataPath == null) {
					if (format == 'i') {
						dataPath = genTemporaryFile("a=1\nb=2",
							tempDir, "data.ini", deleteOnExit, "ascii");
					} else if (format == 'x') {
						dataPath = genTemporaryFile(
"<root a=\"123\" >\n" +
"  <b>text</b>\n" +
"  <b/>\n" +
"</root>", tempDir, "data.xml", deleteOnExit, "UTF-8");
					} else if (format == 'j') {
						dataPath = genTemporaryFile("{\"a\": [1, 2, 3]}",
							tempDir, "data.json", deleteOnExit, "UTF-8");
					}
					debug = editInput = displayResult = "true";
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
			System.err.println(msg + "\n" + info);
			return;
		}
		// Sources
		for (String x: xdefs) {
			src += "  <XDefinition>" + x + "</XDefinition>\n";
		}
		if (!deleteOnExit) { // temp directory was specified
			String s = SUtils.modifyString(tempDir.getPath(), "&", "&amp;");
			s = SUtils.modifyString(s, "<", "&lt;");
			src += "  <TempDir>" + s + "</TempDir>\n";
		}
		src += "</Project>";
		// run generated project
		runEditor(param, format,
			genTemporaryFile(src, tempDir, "project.xml",deleteOnExit,"UTF-8"));
	}
}