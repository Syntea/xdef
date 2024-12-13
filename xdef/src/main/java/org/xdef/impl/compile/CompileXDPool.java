package org.xdef.impl.compile;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import org.xdef.XDContainer;
import org.xdef.XDParser;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import static org.xdef.XDValueID.XD_ANY;
import static org.xdef.XDValueID.XD_BOOLEAN;
import static org.xdef.XDValueID.XD_CONTAINER;
import static org.xdef.XDValueID.XD_PARSER;
import static org.xdef.XDValueID.XD_STRING;
import static org.xdef.XDValueID.XD_VOID;
import org.xdef.impl.XChoice;
import org.xdef.impl.XComment;
import org.xdef.impl.XConstants;
import org.xdef.impl.XData;
import org.xdef.impl.XDebugInfo;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XLexicon;
import org.xdef.impl.XMixed;
import org.xdef.impl.XNode;
import org.xdef.impl.XOccurrence;
import org.xdef.impl.XPool;
import org.xdef.impl.XSelector;
import org.xdef.impl.XSelectorEnd;
import org.xdef.impl.XSequence;
import org.xdef.impl.XVariableTable;
import org.xdef.impl.code.CodeS1;
import org.xdef.impl.code.CodeTable;
import static org.xdef.impl.code.CodeTable.GETELEMS_FROM_CONTEXT;
import static org.xdef.impl.compile.CompileBase.ELEM_MODE;
import static org.xdef.impl.compile.CompileBase.GLOBAL_MODE;
import static org.xdef.impl.compile.XScriptParser.CREATE_SYM;
import static org.xdef.impl.compile.XScriptParser.FINALLY_SYM;
import static org.xdef.impl.compile.XScriptParser.INIT_SYM;
import static org.xdef.impl.compile.XScriptParser.MATCH_SYM;
import org.xdef.impl.parsers.XDParseEnum;
import org.xdef.model.XMData;
import org.xdef.model.XMElement;
import org.xdef.model.XMNode;
import static org.xdef.model.XMNode.XMATTRIBUTE;
import static org.xdef.model.XMNode.XMCHOICE;
import static org.xdef.model.XMNode.XMDEFINITION;
import static org.xdef.model.XMNode.XMELEMENT;
import static org.xdef.model.XMNode.XMMIXED;
import static org.xdef.model.XMNode.XMSELECTOR_END;
import static org.xdef.model.XMNode.XMSEQUENCE;
import static org.xdef.model.XMNode.XMTEXT;
import org.xdef.model.XMVariable;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SError;
import org.xdef.sys.SPosition;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;

/** Compile X-definitions from source data.
 * @author Vaclav Trojan
 */
public final class CompileXDPool implements CodeTable, XDValueID {

	/** MAX_REFERENCE max level of nested references */
	private static final int MAX_REFERENCE = 4096;
	/** No XON/JSON mode. */
	private static final byte NOXON = 0;
	/** XPreCompiler instance.  */
	private final PreCompiler _precomp;
	/** Table of definitions */
	private final Map<String, XDefinition> _xdefs;
	/** PNodes with parsed source items. */
	private final List<PNode> _xdefPNodes;
	/** Array of lexicon sources item. */
	private final List<PNode> _lexicon;
	/** Array of BNF sources. */
	private final List<PNode> _listBNF;
	/** Array of declaration source items. */
	private final List<PNode> _listDecl;
	/** Array of collection source items. */
	private final List<PNode> _listCollection;
	/** Array of component sources. */
	private final List<PNode> _listComponent;
	/** Actual node stack. */
	private final List<XNode> _nodeList;
	/** The script compiler. */
	private final CompileXScript _scriptCompiler;
	/** Code generator. */
	private final CompileCode _codeGenerator;

	/** Creates a new instance of XDefCompiler
	 * @param xp The XDefPool object.
	 * @param reporter The reporter.
	 * @param extClasses The external classes.
	 * @param xdefs Table of X-definitions.
	 */
	public CompileXDPool(final XDPool xp,
		final ReportWriter reporter,
		final Class<?>[] extClasses,
		final Map<String, XDefinition> xdefs) {
		_precomp = new XPreCompiler(reporter,
			extClasses,
			xp.getDisplayMode(),
			xp.isChkWarnings(),
			xp.isDebugMode(),
			xp.isIgnoreUnresolvedExternals());
		_xdefs = xdefs;
		_nodeList = new ArrayList<>();
		_codeGenerator = _precomp.getCodeGenerator();
		_xdefPNodes = _precomp.getPXDefs();
		_lexicon = _precomp.getPLexiconList();
		_listBNF = _precomp.getPBNFs();
		_listDecl = _precomp.getPDeclarations();
		_listCollection = _precomp.getPCollections();
		_listComponent = _precomp.getPComponents();
		ClassLoader cloader = Thread.currentThread().getContextClassLoader();
		_scriptCompiler = new CompileXScript(_codeGenerator,
			StringParser.XMLVER1_0, XPreCompiler.DEFINED_PREFIXES, cloader);
		_scriptCompiler.setReportWriter(reporter);
	}

	/** Get external classes used in x-definition methods.
	 * @return array of objects.
	 */
	public Class<?>[] getExternals() {return _codeGenerator._extClasses;}

	/** Set User objects. This method is just to keep compatibility with previous versions.
	 * @param extObjects array of objects.
	 */
	public void setExternals(final Class<?>... extObjects) {_codeGenerator.setExternals(extObjects);}

	/** Set class loader. The class loader must be set before setting sources.
	 * @param loader the ClassLoader.
	 */
	public final void setClassLoader(final ClassLoader loader) {_scriptCompiler.setClassLoader(loader);}

	/** Get the ClassLoader used to load Java classes.
	 * @return ClassLoader used to load Java classes.
	 */
	public final ClassLoader getClassLoader() {return _scriptCompiler.getClassLoader();}

	public final ReportWriter getReportWriter() {return _precomp.getReportWriter();}

	public final void setReportWriter(ReportWriter reporter) {_precomp.setReportWriter(reporter);}

	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 */
	public final void parseString(final String source, final String srcName) {
		_precomp.parseString(source, srcName);
	}

	/** Parse file with source X-definition and addAttr it to the set of definitions.
	 * @param file The file with with X-definitions.
	 */
	public final void parseFile(final File file) {_precomp.parseFile(file);}

	/** Parse InputStream source X-definition and addAttr it to the set of definitions.
	 * @param in input stream with the X-definition.
	 * @param name name of source data used in reporting (SysId) or null.
	 */
	public final void parseStream(final InputStream in, final String name) {_precomp.parseStream(in, name);}

	/** Parse data with source X-definition given by URL and addAttr it to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 */
	public final void parseURL(final URL url) {_precomp.parseURL(url);}

	/** Get prepared sources (PNodes) of X-definition items.
	 * @return array with PNodes with X-definitions.
	 */
	public List<PNode> getPXDefs() {return _xdefPNodes;}

	/** Get prepared sources (PNodes) of XDLexicon items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPLexicons() {return _lexicon;}

	/** Get prepared sources (PNodes) of collection items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPCollections() {return _listCollection;}

	/** Get prepared sources (PNodes) of declaration items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPDeclarations() {return _listDecl;}

	/** Get prepared sources (PNodes) of components items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPComponents() {return _listComponent;}

	/** Get prepared sources (PNodes) of BNF Grammar items.
	 * @return array with PNodes.
	 */
	public final List<PNode> getPBNFs() {return _listBNF;}

	/** Prepare list of declared macros and expand macro references. */
	public final void prepareMacros() {_precomp.prepareMacros();}

	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	private void error(final SPosition pos, final long registeredID, final Object... mod) {
		_precomp.error(pos, registeredID, mod);
	}

	/** Put error to compiler reporter.
	 * @param regID registered report id.
	 * @param mod Message modification parameters.
	 */
	private void error(final long registeredID, final Object... mod) {_precomp.error(registeredID, mod);}

	/** Add node to parent.
	 * @param parentNode The node where the new node will be added.
	 * @param xNode The node to be added.
	 * @param level The nesting level of new node.
	 */
	private void addNode(final XNode parentNode, final XNode xNode, final int level, final SPosition pos) {
		short parentKind = parentNode.getKind();
		short nodeKind = xNode.getKind();
		if (parentKind == XMDEFINITION) { //"model" level
			if (nodeKind == XMELEMENT) {
				((XElement) xNode)._definition = (XDefinition) parentNode;
				((XElement) xNode).setXDNamespaceContext(_scriptCompiler._g.getXDNamespaceContext());
			} else if (nodeKind == XMTEXT) {
				_precomp.lightError(pos, XDEF.XDEF260);//Text value not allowed here
			}
		} else {
			XElement parentXel;
			if (parentKind == XMELEMENT) {
				parentXel = (XElement) parentNode;
			} else {
				// here it can be only group, we find the parent element model
				parentXel = null;
				int lev = level - 1;
				while (lev >= 0) {
					XNode xnode = _nodeList.get(lev);
					if (xnode.getKind()!= XMELEMENT) {
						lev--;
						continue;
					}
					parentXel = (XElement) xnode;
					break;
				}
				if (parentXel == null) {
					throw new SError(XDEF.XDEF202, "No XElement"); //Internal error: &{0}
				}
			}
			if (nodeKind == XMELEMENT) {
				((XElement) xNode)._definition = parentXel._definition;
				((XElement) xNode).setXDNamespaceContext(_scriptCompiler._g.getXDNamespaceContext());
			}
			parentXel.addNode(xNode);
		}
		_nodeList.add(level, xNode);
	}

	/** Report deprecated item.
	 * @param spos position where to report.
	 * @param old deprecated item.
	 * @param replace what should be done.
	 */
	private void reportDeprecated(final SPosition spos, final String old, final String replace) {
		if (_precomp.isChkWarnings()) {
			//&{0} is deprecated.&{1}{ Please use }{ instead.}
			_precomp.warning(spos, XDEF.XDEF998, old, replace);
		}
	}

	private void compileComponentDeclaration() {
		for (;;) {
			_scriptCompiler.skipBlanksAndComments();
			if (_scriptCompiler.eos()) {
				break;
			}
			int ndx;
			if (!_scriptCompiler.isChar('%')) {
				error(XDEF.XDEF356, //'&{0}' expected
					"\"%class\",\"%interface\",\"%bind\",\"%ref\",\"%enum\"");
				if (_scriptCompiler.findCharAndSkip(';')) {
					continue;
				}
				break;
			}
			SPosition spos = _scriptCompiler.getPosition();
			String result = _scriptCompiler.parseComponent(true);
			if (result == null) {
				if (_scriptCompiler.findCharAndSkip(';')) {
					continue;
				}
				break;
			}
			if (result.contains("[1]")) {
				result = SUtils.modifyString(result, "[1]", ""); // remove all occurrences of "[1]"
			}
			_scriptCompiler.skipBlanksAndComments();
			if (result.startsWith("%enum ")) {
				ndx = result.indexOf(' ', 6);
				// xdef name + class
				String enumClass = 	_codeGenerator._parser._actDefName + "#" + result.substring(6, ndx);
				SBuffer sbf = new SBuffer(enumClass, spos);
				String enumTypeName = result.substring(ndx+1);
				if (_codeGenerator._enums.put(enumTypeName, sbf) != null) {
					_scriptCompiler.error(spos, XDEF.XDEF379, enumTypeName);//Duplicity of enumeration &{0}
				}
			} else if (result.startsWith("%bind")) {
				ndx = result.indexOf(" %link ");
				StringTokenizer st = new StringTokenizer(result.substring(ndx+7), "\t\n\r ,");
				SBuffer sbf = new SBuffer(result.substring(6, ndx), spos);
				while(st.hasMoreTokens()) {
					if (_codeGenerator._binds.put(st.nextToken(),sbf) != null) {
						_scriptCompiler.error(spos,//Duplicity of reference &{0}
							XDEF.XDEF355, sbf.getString());
					}
				}
			} else if (result.startsWith("%ref ")) {
				ndx = result.indexOf(" %link ");
				String className = result.substring(0, ndx);
				String modelName = result.substring(ndx + 7);
				ndx = modelName.indexOf('#');
				String xdName = modelName.substring(0, ndx);
				modelName = modelName.substring(ndx + 1);
				String model = xdName + '#' + modelName;
				SBuffer s = _codeGenerator._components.put(model, new SBuffer(className, spos));
				if (s != null) {
					error(spos, XDEF.XDEF351, "interface;"+model); //Duplicate declaration of &{0}
				}
				_scriptCompiler.skipBlanksAndComments();
				_scriptCompiler.isChar(';');
				continue;
			} else {
				ndx = result.indexOf(" %link ");
				String modelName = result.substring(ndx + 7);
				result = result.substring(0, ndx);
				ndx = modelName.indexOf('#');
				String xdName = modelName.substring(0, ndx);
				modelName = modelName.substring(ndx + 1);
				_scriptCompiler.skipBlanksAndComments();
				String className, extension;
				if (result.startsWith("%interface ")) {
					extension = result.substring(1);
					className = "";
				} else { // %class
					ndx = result.indexOf(' ', 7);
					if (ndx < 0) {
						extension = "";
						className = result.substring(7);
					} else {
						extension = result.substring(ndx); //with space!
						int ndx1;
						if ((ndx1 = extension.indexOf("%interface ")) > 0) {
							extension = extension.substring(0, ndx1) + extension.substring(ndx1+1);
						}
						className = result.substring(7, ndx);
					}
				}
				String model = xdName + '#' + modelName;
				SBuffer s = _codeGenerator._components.get(model);
				if (s != null) {
					String t = s.getString();
					if (result.startsWith("%class ")
						&& t.startsWith("interface ")) {
						if (result.indexOf(" %interface ") > 0) {
							error(spos, XDEF.XDEF351, "interface;"+model);//Duplicate declaration of &{0}
						} else {
							t = result.substring(7) + ' ' + t;
							s = new SBuffer(t, spos);
							_codeGenerator._components.put(model, s);
						}
					} else if (result.startsWith("%interface ")) {
						if (t.contains(" interface ")) {
							//Duplicate declaration of &{0}
							error(spos, XDEF.XDEF351, "interface;"+model);
						} else {
							t += ' ' + result.substring(1);
							s = new SBuffer(t, s);
							_codeGenerator._components.put(model, s);
						}
					} else {
						s = _codeGenerator._components.get(model);
						if (s != null) {
							if (!s.getString().startsWith("interface ")) {
								error(s, XDEF.XDEF351, model); //Duplicate declaration of &{0}
								error(spos, XDEF.XDEF351, model); //Duplicate declaration of &{0}
								// do not repeat this message
								_codeGenerator._components.remove(model);
							}
						}
					}
				} else {
					for (Entry<String, SBuffer> e:
						_codeGenerator._components.entrySet()) {
						String cn = e.getValue().getString();
						ndx = cn.indexOf(" extends ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						ndx = cn.indexOf(" implements ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						ndx = cn.indexOf(" interface ");
						if (ndx > 0) {
							cn = cn.substring(0, ndx);
						}
						if (className.equals(cn) && !model.equals(e.getKey())) {
							//Duplicate declaration of class &{0} for XComponent &{1}
							error(spos, XDEF.XDEF352, className,model);
						}
					}
					if (!className.contains(" implements ") || !extension.startsWith(" implements ")) {
						className += extension;
					} else {
						className += extension.substring(12);
					}
					s = _codeGenerator._components.put(
						model, new SBuffer(className, spos));
					if (s != null && !className.equals(s.getString())) {
						//Duplicate declaration of class &{0} // for XComponent &{1}
						error(spos, XDEF.XDEF352, className, model);
					}
				}
			}
			if (!_scriptCompiler.isChar(';')) {
				spos = _scriptCompiler.getPosition();
				if (!_scriptCompiler.eos()) {
					_scriptCompiler.error(spos, XDEF.XDEF356, ";"); //'&{0}' expected
					if (!_scriptCompiler.findCharAndSkip(';')) {
						break;
					}
				}
			}
		}
	}

	private void compileMethodsAndClassesAttrs() {
		if (getExternals() != null && getExternals().length > 0) {
			reportDeprecated(new SPosition(),
				"Class parameter of compileXD method", "<xd:declaration> external method { ... } ...");
		}
		for (PNode pnode : _xdefPNodes) {
			PAttr pa = _precomp.getXdefAttr(pnode, "methods", false, true);
			if (pa!= null) {
				if (pnode._xdVersion > XConstants.XD31) {
					reportDeprecated(pa._value,
						"Attribute \"methods\"", "<xd:declaration> external method { ... } ...");
				}
				if (!_codeGenerator._ignoreUnresolvedExternals) {
					_scriptCompiler.setSource(pa._value,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.compileExtMethods();
				}
			}
			pa = _precomp.getXdefAttr(pnode, "classes", false, true);
			if (pa != null) {
				if (pnode._xdVersion > XConstants.XD31) {
					reportDeprecated(pa._value,
						"Attribute \"classes\"", "<xd:declaration> external method ...");
				}
				if (!_codeGenerator._ignoreUnresolvedExternals) {
					String value = pa._value.getString();
					Map<String, Class<?>> ht = new LinkedHashMap<>();
					for (Class<?> clazz : _codeGenerator._extClasses) {
						ht.put(clazz.getName(), clazz);
					}
					StringTokenizer st = new StringTokenizer(value," \t\r\n,;");
					while (st.hasMoreTokens()) {
						String clsname = st.nextToken();
						if (!ht.containsKey(clsname)) {
							Class<?> clazz;
							try {
								clazz = Class.forName(clsname, false, _scriptCompiler.getClassLoader());
							} catch (ClassNotFoundException ex) {
								clazz = null;
							}
							if (clazz != null) {
								ht.put(clazz.getName(), clazz);
							} else {
								error(pa._value, XDEF.XDEF267, clsname); //Class &{0} is not available
							}
						}
					}
					if (_codeGenerator._extClasses.length == 0) {
						Class<?>[] exts = new Class<?>[ht.values().size()];
						ht.values().toArray(exts);
						_codeGenerator.setExternals(exts);
					}
				}
			}
			pa = _precomp.getXdefAttr(pnode, "importLocal", false, true);
			List<String> locals = new ArrayList<>();
			locals.add(pnode._xdef.getName() + '#');
			if (pa != null) {
				_scriptCompiler.setSource(pa._value,
					pnode._xdef.getName(), pnode._xdef, pnode._xdVersion, pnode._nsPrefixes, pnode._xpathPos);
				_scriptCompiler.compileAcceptLocal(locals);
			}
			pnode._xdef._importLocal = new String[locals.size()];
			locals.toArray(pnode._xdef._importLocal);
		}
	}

	/** Check if declaration section has an attribute "scope".
	 * @param nodei node with declaration section.
	 * @return true if the attribute "scope" is "local".
	 */
	private boolean isLocalScope(final PNode nodei, final boolean removeAttr) {
		boolean local = nodei._xdef!=null && nodei._xdef.getXDVersion() >= XConstants.XD40;
		PAttr scope = _precomp.getXdefAttr(nodei, "scope", false, removeAttr);
		if (scope != null) {
			String s = scope._value.getString();
			switch (s) {
				case "global":
					return false; // OK, it is global anyay
				case "local":
					if (nodei._xdef == null) {
						//Attribute "scope" in declaration section is&{0}{ '}{'}.
						// It can be only "global"&{1}{ or }.
						error(scope._value, XDEF.XDEF221, s);
						return false; // must be global
					}
					return true;
				default:
					//Attribute "scope" in declaration section is&{0}{ '}{'}.
					//It can be only "global"&{1}{ or }.
					error(scope._value, XDEF.XDEF221, s, "'local'");
			}
		}
		return local;
	}

	/** Precompile list of BNF declarations and then the list of variable declarations. If there is an
	 * undefined object in an item of the list then put this item to the end of list and try to recompile it.
	 * This nasty trick ensures the declarations on object to process object references. However,
	 * it should be resolved with a reference list connected to the variable declaration.
	 */
	private void preCompileDeclarations() {
		// now not generate code of external methods, make them undefined
		_codeGenerator.setIgnoreExternalMethods(true);
		//TODO Smid scope of externals....
		// do not ignore unresolved externals at this point of compilation ->
		// leads to mismatch result in case of using 'true' value for
		// XDConstants.XDPROPERTY_IGNORE_UNDEF_EXT
		boolean ignoreUnresolvedExternals = _codeGenerator._ignoreUnresolvedExternals;
		_codeGenerator._ignoreUnresolvedExternals = false;
		// first process BNFGrammar declarations, then declaration sections.
		for (List<PNode> list = _listBNF; list != null;
			list = list == _listBNF ? _listDecl : null) {
			int len;
			if ((len = list.size()) > 0) {
				int size = _codeGenerator._globalVariables.size();
				int lastOffset =
					_codeGenerator._globalVariables.getLastOffset();
				for (int n = 0; ; n++) {
					int errndx = -1;
					// now we check items from the list. We break the cycle when we find an item with an
					// undefined variable referrence. We put this item to the end of list and we try it again.
					for (int i = 0; i < len; i++) {
						PNode nodei = list.get(i);
						// name of X-definition
						if (list == _listBNF) { //BNFs
							compileBNFGrammar(nodei, false);
						} else { //declarations
							compileDeclaration(nodei, false);
						}
						if (_scriptCompiler.errors()) { //if errors reported
							ArrayReporter reporter = (ArrayReporter) _scriptCompiler.getReportWriter();
							Report report;
							//check if there is undefined variable report
							while ((report = reporter.getReport()) != null) {
								//XDEF424 ... Undefined variable &{0}
								//XDEF443 ... Unknown method &{0}
								if ("XDEF424".equals(report.getMsgID())||("XDEF443".equals(report.getMsgID())
									&& list == _listDecl)) {
									errndx = i; //set index of this item
									break;
								}
							}
							reporter.clear();
							if (errndx == i) {
								break;
							}
						}
					}
					if (errndx == -1) {
						break;  // finished, => OK
					}
					if (errndx == len - 1 || n >= len) {
						//we must now compile all items not compiled yet
						for (int i = errndx + 1; i < len; i++) {
							PNode nodei = list.get(i);
							if (list == _listBNF) { //BNFs
								compileBNFGrammar(nodei, false);
							} else { //declarations
								compileDeclaration(nodei, false);
							}
						}
						break;
					}
					//initRecompilation
					_codeGenerator._globalVariables.resetTo(size, lastOffset);
					_codeGenerator.reInit(); //clear the generated code
					_scriptCompiler.initCompilation(GLOBAL_MODE, XD_VOID);
					//move the node with error to the end of list and try again
					list.add(list.remove(errndx));
				}
			}
		}
		// set value of _ignoreUnresolvedExternals back to original
		_codeGenerator._ignoreUnresolvedExternals = ignoreUnresolvedExternals;
		// set normal generation of code of external methods
		_codeGenerator.setIgnoreExternalMethods(false);
	}

	/** Compile lexicons.
	 * @param lexicon list of lexicon declarations.
	 * @param xp XDPool object.
	 */
	private void compileLexicons(final List<PNode> lexicon, final XDPool xp) {
		if (!lexicon.isEmpty()) { //Compile lexicon section
			/** Array of properties for lexicon languages. */
			List<Map<String,String>> languages = new ArrayList<>();
			for (PNode nodei : lexicon) {
				PAttr pa = _precomp.getXdefAttr(nodei, "language", true, true);
				SBuffer lang = pa == null ? null : pa._value;
				pa = _precomp.getXdefAttr(nodei, "default", false, true);
				SBuffer deflt = pa == null ? null : pa._value;
				_scriptCompiler.compileLexicon(nodei,lang,deflt,xp,languages);
				_precomp.reportNotAllowedAttrs(nodei);
			}
			if (!languages .isEmpty()) {
				String[] langs = new String[languages.size()];
				boolean deflt = false;
				for (int i = 0; i < langs.length; i++) {
					Map<String,String> p = languages.get(i);
					if ("!".equals(p.get("%{default}"))) {
						deflt = true;
						p.remove("%{default}");
						if (i != 0) {
							Map<String,String> pp = languages.get(0);
							languages.set(0, p);
							languages.set(i, pp);
							PNode nodei = lexicon.get(0);
							lexicon.set(0, lexicon.get(i));
							lexicon.set(i, nodei);
						}
						break;
					}
				}
				for (int i = 0; i < langs.length; i++) {
					Map<String,String> p = languages .get(i);
					langs[i] = p.get("%{language}");
					p.remove("%{language}");
				}
				XLexicon t = new XLexicon(langs);
				for (int i = deflt ? 1 : 0; i < langs.length; i++) {
					Map<String,String> p = languages .get(i);
					for (Object o: p.keySet()) {
						String s = (String) o;
						t.setItem(s, i, p.get(s));
					}
				}
				boolean[] badIndexes = new boolean[languages.size()];
				for (String key: t.getKeys()) {
					String[] texts = t.findTexts(key);
					for (int i = deflt ? 1 : 0; i < texts.length; i++) {
						if (texts[i] == null) {
							badIndexes[i] = false;
							//Lexicon item "&{0}" is missing for language &{1}
							error(lexicon.get(i)._name, XDEF.XDEF149, key, t.getLanguages()[i]);
						}
					}
				}
				if (deflt) {
					int okIndex = 1;
					for (int i = 1; i < badIndexes.length; i++) {
						if (!badIndexes[i]) {
							okIndex = i;
							break;
						}
					}
					Map<String,String> p = languages .get(okIndex);
					for (String s : p.keySet()) {
						String v = s;
						int ndx = v.indexOf('#');
						if (ndx >= 0) {
							v = v.substring(ndx+1);
						}
						ndx = v.lastIndexOf('/');
						if (ndx >= 0) {
							v = v.substring(ndx+1);
						}
						if (v.startsWith("@")) {
							v = v.substring(1);
						}
						t.setItem(s, 0, v);
					}
				}
				_scriptCompiler._g._lexicon = t;
			}
			lexicon.clear();
		}
	}

	private void compileDeclaration(final PNode nodei, final boolean remove) {
		boolean local = isLocalScope(nodei, remove);
		String defName = nodei._xdef == null ? null : nodei._xdef.getName();
		_scriptCompiler.setSource(
			nodei._value, defName, nodei._xdef, nodei._xdVersion, nodei._nsPrefixes, nodei._xpathPos);
		_scriptCompiler.compileDeclaration(local);
	}

	private void compileBNFGrammar(final PNode nodei, final boolean remove) {
		PAttr pa = _precomp.getXdefAttr(nodei, "name", true, remove);
		if (pa == null) {
			return; //required name is missing.
		}
		SBuffer sName = pa._value;
		pa = _precomp.getXdefAttr(nodei, "extends", false, remove);
		_scriptCompiler.compileBNFGrammar(sName, pa==null?null:pa._value, nodei, isLocalScope(nodei, remove));
	}

	/** Compile list of BNFs declaration and of variables/methods declaration.
	 * @param listBNF list of BNF declarations.
	 * @param listDecl list of variable declarations.
	 */
	private void compileDeclarations(final List<PNode> listBNF,
		final List<PNode> listDecl,
		final List<PNode> listComponent) {
		for (List<PNode> list = listBNF; list != null;
			list = list == listBNF ? listDecl : null) {
			if (!list.isEmpty()) {
				for (PNode nodei: list) {
					if (list == listBNF) { //BNFs
						compileBNFGrammar(nodei, true);
					} else { //declarations
						compileDeclaration(nodei, true);
					}
					_precomp.reportNotAllowedAttrs(nodei);
				}
			}
		}
		for (PNode nodei : listComponent) {
			String defName = nodei._xdef == null ? "" : nodei._xdef.getName();
			_scriptCompiler.setSource(
				nodei._value, defName, nodei._xdef, nodei._xdVersion, nodei._nsPrefixes, nodei._xpathPos);
			compileComponentDeclaration();
		}
	}

	/** First step: prepare and expand macros and compile declarations. */
	private void precompile() {
		ReportWriter reporter = getReportWriter();
		// set reporter to the compiler of script
		_scriptCompiler.setReportWriter(reporter);
		_scriptCompiler.initCompilation(GLOBAL_MODE, XD_VOID);
		compileMethodsAndClassesAttrs();
		// Move all declarations of BNF grammars and variables from the list of X-definitions to the separated
		// lists of variable declarations and of BNF grammar declarations.
		for (PNode def : _xdefPNodes) {
			// since we are removing childnodes from X-definition we must process the childnodes list
			// downwards! However, we insert the item to the first position of the created
			// list to assure the original sequence of items in the X-definition
			for (int j = def.getChildNodes().size() - 1;  j >= 0; j--) {
				PNode nodei = def.getChildNodes().get(j);
				if (nodei._nsindex != XPreCompiler.NS_XDEF_INDEX) {
					continue;
				}
				String nodeName = nodei._localName;
				switch (nodeName) {
					case "lexicon": _lexicon.add(nodei); break;
					case "thesaurus":
						reportDeprecated(nodei._name, "\"thesaurus\"","\"lexicon\"");
						_lexicon.add(nodei);
						break;
					case "BNFGrammar": _listBNF.add(0, nodei); break;
					case "component":
						if (nodei._value != null && nodei._value.getString().trim().length() > 0) {
							_listComponent.add(0, nodei); // not empty
						}
						break;
					case "declaration":
						if (nodei._value != null && nodei._value.getString().length() > 0) {
							_listDecl.add(0, nodei); // not empty
						}
						break;
					default: continue;
				}
				nodei._xdef = def._xdef;
				// remove this node from the X-definition PNode
				def.getChildNodes().remove(j);
			}
		}
		//we set a temporary reporter which we throw out.
		_scriptCompiler.setReportWriter(new ArrayReporter());
		//Compile declarations and we throw errors out. Due to this trick after this step there will be
		//resolved postdefines and we'll know all types of declared variables and methods.

		// precompile declarations and BNF gramars - just to prepare variable list and to sort item
		//to resolve cross references.
		preCompileDeclarations();

		//Now forget the generated code and compile declatations again with known types of declared objects
		//and with the original error reporter. After compilation the nodes containing declarations
		//are removed from the tree.
		setReportWriter(reporter);//reset original reporter
		_codeGenerator._debugInfo = new XDebugInfo();
		_scriptCompiler.setReportWriter(reporter); //reset original reporter
		_codeGenerator.reInit(); //clear the generated code
		_scriptCompiler.initCompilation(GLOBAL_MODE, XD_VOID);
		// now compile all: BNF gramars, declarations, components, lexicon
		compileDeclarations(_listBNF, _listDecl, _listComponent);
		// clear all postdefines (should be already dleared, but who knows?)
		_codeGenerator.clearPostdefines();
	}

	/** Add the name of X-definition if it is not yet there. */
	private String canonizeReferenceName(final String refName,
		final XDefinition xdef) {
		String name = refName;
		String defName = xdef.getName();
		int i = name.indexOf('#');
		if (i == 0) {
			name = name.substring(1);
		} else if (i > 0) {
			defName = name.substring(0,i);
			name = name.substring(i+1);
		}
		if (!XPreCompiler.chkDefName(defName, xdef.getXmlVersion())) {
			return null;
		}
		if (!StringParser.chkNCName(name, xdef.getXmlVersion())) {
			return null;
		}
		return defName + '#' + name;
	}

	/** Create copy of SPosition (without modifications). */
	private SPosition copySPosition(final SPosition sval) {
		return new SPosition(sval.getIndex(),
			sval.getLineNumber(), sval.getStartLine(), sval.getFilePos(), sval.getSystemId());
	}

	private void compileAttrs(final PNode pnode,
		final String defName,
		final XNode xNode,
		final boolean isAttlist) {
		XElement xel;
		XData xtxt;
		short newKind;
		if ((newKind = xNode.getKind()) == XMELEMENT) {
			xel = (XElement) xNode;
			xtxt = null;
			//compile first script - we must recognize template!
			PAttr pattr = pnode.getAttrNS("script", XPreCompiler.NS_XDEF_INDEX);
			if (pattr != null) {
				_scriptCompiler.setSource(pattr._value,
					defName, xel.getDefinition(), pnode._xdVersion, pnode._nsPrefixes, pnode._xpathPos);
				if (xel._template) {
					_scriptCompiler.isSpaces();
					if (_scriptCompiler.isToken("$$$script:")) {
						_scriptCompiler.isSpaces();
						_scriptCompiler.isChar(';');
						xel._template = false;
					}
				}
				_scriptCompiler.compileElementScript(xel);
			}
		} else if (newKind == XMTEXT) {
			xel = null;
			xtxt = (XData) xNode;
		} else {
			return;
		}
		for (PAttr pattr : pnode.getAttrs()) {
			String key = pattr._name;
			SBuffer sval = pattr._value;
			_scriptCompiler.setSource(sval,
				_scriptCompiler._actDefName,
				(XDefinition) xNode.getXMDefinition(),
				pnode._xdVersion,
				pnode._nsPrefixes,
				pattr._xpathPos);
			if (pattr._nsindex == XPreCompiler.NS_XDEF_INDEX) {
				String localName = pattr._localName;
				if ("script".equals(localName)) {
					if (isAttlist) {
						error(sval, XDEF.XDEF254, key); //Attribute '&{0}' not allowed here
					} else if (newKind == XMTEXT) {
						_scriptCompiler.compileDataScript(xtxt);
					}
				} else if ("attr".equals(localName)) {
					//any Attribute - script for "moreAtttributes"
					if (newKind == XMELEMENT && xel != null/*must be!*/) {
						//any attributes in Element
						XData xattr = new XData("$attr", null, xel.getXDPool(), XMATTRIBUTE);
						xattr.setSPosition(copySPosition(sval));
						xattr.setXDPosition(xel.getXDPosition()+"/$attr");
						_scriptCompiler.compileDataScript(xattr);
						xel.setDefAttr(xattr);
					} else {
						error(sval, XDEF.XDEF254, key); //Attribute '&{0}' not allowed here
					}
				} else if ("text".equals(localName)
					|| "textcontent".equals(localName)) {
					if (newKind ==XMELEMENT && xel != null/*must be!*/ && !isAttlist) {
						//here is "text" or "textcontent"
						XData xdata = new XData('$' + localName, null, xel.getXDPool(), XMTEXT);
						xdata.setSPosition(copySPosition(sval));
						xdata.setXDPosition(xel.getXDPosition() + "/$" + localName);
						_scriptCompiler.compileDataScript(xdata);
						xel.setDefAttr(xdata);
						xel._moreText = 'T';
						if ("textcontent".equals(localName) && xdata.maxOccurs() > 1) {
							//Maximum occurrence of item "&{0}" can not be higher then 1
							error(sval, XDEF.XDEF535, "textcontent");
						}
					} else {
						error(sval, XDEF.XDEF254, key); //Attribute '&{0}' not allowed here
					}
//				} else if ("PI".equals(localName)) {//TODO
//				} else if ("comment".equals(localName)) {//TODO
//				} else if ("document".equals(localName)) {//TODO
//				} else if ("value".equals(localName)) {//TODO
//				} else if ("attlist".equals(localName)) {//TODO
				} else if (newKind == XMELEMENT) {
					error(sval, XDEF.XDEF254, key); //Attribute '&{0}' not allowed here
				}
			} else {
				// attributes which are not from our namespace
				if (newKind == XMELEMENT && xel != null /*must be!*/) {
					XData xattr = new XData(key, pattr._nsURI, xel.getXDPool(), XMATTRIBUTE);
					xattr.setSPosition(copySPosition(sval));
					xattr.setXDPosition(xel.getXDPosition()+ "/@" + key);
					boolean template;
					_scriptCompiler.isSpaces();
					if (template = xel._template) {
						_scriptCompiler.isSpaces();
						if (_scriptCompiler.isToken("$$$script:")) {
							_scriptCompiler.isSpaces();
							_scriptCompiler.isChar(';');
							template = false;
						}
					}
					if (template) {
						_scriptCompiler.genTemplateData(xattr, xNode);
					} else {//"normal" attributes
						_scriptCompiler.compileDataScript(xattr);
					}
					xel.setDefAttr(xattr);
				} else {
					error(sval, XDEF.XDEF254,key); //Attribute '&{0}' not allowed here
				}
			}
		}
	}

	/** Create reference node.
	 * @param pnode source node.
	 * @param refName Local name of p-node.
	 * @param xdef X-definition.
	 * @return generated ParsedReference object.
	 */
	private XNode createReference(final PNode pnode, final String refName, final XDefinition xdef) {
		XSelector newNode;
		XOccurrence defaultOcc = new XOccurrence(1,1); //required as default
		if (null == refName) {//include
			newNode = new XSequence();
			defaultOcc.setUnspecified();
		} else switch (refName) {
			case "mixed":
				newNode = new XMixed();
				defaultOcc.setUnspecified();
				break;
			case "choice":
				newNode = new XChoice(); //min=1; max=1
				break;
			case "sequence":
				newNode = new XSequence(); //min=1;max=1
				break;
			default: //include
				newNode = new XSequence();
				defaultOcc.setUnspecified();
		}
		newNode.setUnspecified();
		newNode.setSPosition(copySPosition(pnode._name));
		PAttr pa = _precomp.getXdefAttr(pnode, "ref", false, true);
		SBuffer ref = pa == null ? null : pa._value;
		newNode.setXDPosition(xdef.getXDPosition()+'$' + pnode._name.getString()
			+ (ref != null ? "("+ref.getString()+")" : ""));
		short kind = newNode.getKind();
		if (kind == XMCHOICE || kind == XMMIXED || kind == XMSEQUENCE) {
			pa = _precomp.getXdefAttr(pnode, "script", false, true);
			if (pa != null) {
				SBuffer sval = pa._value;
				_scriptCompiler.setSource(sval,
					_scriptCompiler._actDefName,
					pnode._xdef,
					pnode._xdVersion,
					pnode._nsPrefixes,
					pnode._xpathPos);
				SBuffer s = _scriptCompiler.compileGroupScript(newNode);
				if (s != null) {
					if (ref != null) {
						//Reference ca'nt be specified both in attributes 'ref' and 'script'
						error(ref, XDEF.XDEF117);
					}
					ref = s;
				}
			} else {
				pa = _precomp.getXdefAttr(pnode, "init", false, true);
				if (pa != null) {
					SBuffer sval = pa._value;
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.nextSymbol();
					newNode.setInitCode(_scriptCompiler.compileSection(
						ELEM_MODE, XD_VOID,	INIT_SYM));
				}
				pa = _precomp.getXdefAttr(pnode, "occurs", false, true);
				if (pa != null) {
					SBuffer sval = pa._value;
					if (kind == XMMIXED) {
						reportDeprecated(pnode._name, "attribute \"occurs\"", "attribute \"script\"");
					}
					XOccurrence occ = new XOccurrence();
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.nextSymbol();
					if (!_scriptCompiler.isOccurrenceInterval(occ)) {
						//After 'occurs' is expected the interval
						error(sval, XDEF.XDEF429);
					}
					newNode.setOccurrence(occ);
					if (!_scriptCompiler.eos()) {
						 error(sval, XDEF.XDEF425); //Script error
					}
				}
				pa = _precomp.getXdefAttr(pnode, "finally", false, true);
				if (pa != null) {
					SBuffer sval = pa._value;
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.nextSymbol();
					newNode.setFinallyCode(_scriptCompiler.compileSection(ELEM_MODE, XD_VOID, FINALLY_SYM));
				}
				pa = _precomp.getXdefAttr(pnode, "create", false, true);
				if (pa != null) {
					SBuffer sval = pa._value;
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.nextSymbol();
					newNode.setComposeCode(_scriptCompiler.compileSection(ELEM_MODE, XD_ANY, CREATE_SYM));
				}
				pa = _precomp.getXdefAttr(pnode, "match", false, true);
				if (pa != null) {
					SBuffer sval = pa._value;
					_scriptCompiler.setSource(sval,
						_scriptCompiler._actDefName,
						pnode._xdef,
						pnode._xdVersion,
						pnode._nsPrefixes,
						pnode._xpathPos);
					_scriptCompiler.nextSymbol();
					newNode.setMatchCode(_scriptCompiler.compileSection(ELEM_MODE, XD_BOOLEAN, MATCH_SYM));
				}
			}
			pa = _precomp.getXdefAttr(pnode, "empty", false, true);
			if (pa != null) {
				SBuffer sval = pa._value;
				String s = sval.getString().trim();
				if (s.length() > 0) {
					if (newNode.isSpecified()) {//specified
						//If occurrence is specified it can't be changed by specification of 'empty'"
						error(sval, XDEF.XDEF264);
					}
				}
				switch (s) {
					case "true":
						if (kind != XMMIXED) {
							//Attribute 'empty' is allowed only in the group 'xd:mixed'
							_precomp.lightError(pnode._name, XDEF.XDEF263);
							newNode.setOccurrence(0, 1);
						} else {
							newNode.setEmptyDeclared(true);
							newNode.setMinOccur(0);
						}
						break;
					case "false":
						if (kind != XMMIXED) {
							//Attribute 'empty' is allowed only in the group 'xd:mixed'
							_precomp.lightError(pnode._name, XDEF.XDEF263);
							newNode.setOccurrence(1, 1);
						} else {
							newNode.setEmptyDeclared(false);
							newNode.setMinOccur(1);
						}
						break;
					default:
						error(sval, XDEF.XDEF423, "boolean"); //Value of type '&{0}' expected
				}
			}
		}
		_precomp.reportNotAllowedAttrs(pnode);
		if (!newNode.isSpecified()) {
			if (ref != null) {
				newNode.setUnspecified();
			} else {
				newNode.setOccurrence(defaultOcc);
			}
		}
		String name;
		if ("includeChildNodes".equals(refName)) {
			if (ref == null) {
				error(pnode._name, XDEF.XDEF218);//Incorrect or missing attribute 'ref'
				return null;
			}
			if (!pnode.getChildNodes().isEmpty()) {
				//Child nodes of the element 'xd:includeChildNodes' are not allowed
				error(pnode._name, XDEF.XDEF232);
			}
			name = canonizeReferenceName(ref.getString(), xdef);
			if (name == null) {
				error(ref, XDEF.XDEF258); //Incorrect name
				name = ref.getString();
			}
		} else {
			if (ref == null) {
				return "list".equals(refName) ? null : newNode;
			}
			name = ref.getString() + '$' + refName;
		}
		return new CompileReference(CompileReference.XMINCLUDE, xdef, null, name, ref, newNode);
	}

	private static void setXDPosition(final XNode parent, final XElement parentElement, final XNode xn) {
		String name = xn.getName();
		if (parent.getKind() == XMDEFINITION) {
			String nsUri = xn.getNSUri();
			if (nsUri != null) {
				XDefinition xd = (XDefinition) parent;
				for (Entry<String, String> e: xd._namespaces.entrySet()) {
					if (nsUri.equals(e.getValue())) {
						String pfx = e.getKey();
						int ndx = name.indexOf(':');
						if (ndx > 0) {
							if (pfx.isEmpty()) {
								name = name.substring(ndx + 1);
							} else {
								name = pfx + name.substring(ndx);
							}
						} else if (!pfx.isEmpty()) {
							name = pfx + ':' + name;
						}
						break;
					}
				}
			}
			xn.setXDPosition(parent.getXDPosition() + name);
			return;
		}
		String xdPos = parent.getXDPosition();
		if (!xdPos.endsWith("#")) {
			xdPos += "/";
		}
		xdPos += (xn.getKind() == XMTEXT) ? "$text" : name;
		int n = 1;
		for (XNode x : parentElement._childNodes) {
			String xpos = x.getXDPosition();
			if (xpos != null) { // not #selector_end
				if (xpos.endsWith("]")) {
					xpos = xpos.substring(0, xpos.lastIndexOf('['));
				}
				if (xdPos.equals(xpos)) {
					n++;
				}
			}
		}
		if (n > 1) {
			xdPos += "[" + n + "]";
		}
		xn.setXDPosition(xdPos);
	}

	/** Change namespace ns1 to ns of model in attribudes and elements witn ns1. If the new namespace is null,
	 * then attributes and child nodes with the namespace are removed.
	 * @param xe Element to be changed.
	 * @param nsOrig original namespace.
	 * @param nsNew new namespace.
	 * @param hs HashSet with processed nodes (revent unlimited recursive call).
	 */
	private void changeModelNS(final XElement xe,final String nsOrig,final String nsNew,final Set<XNode> hs){
		if (!hs.add(xe)) {
			return; //already processed
		}
		// change attributes
		XMData[] attrs = xe.getAttrs();
		for (XMData x: attrs) {
			String ns = x.getNSUri();
			if (nsOrig != null && nsOrig.equals(ns)) {
				XData y = (XData) xe.getAttrNS(nsOrig, x.getName());
				xe.removeDefAttr(y);
				if (nsNew != null) {
					XData z = new XData(y);
					z.changeNS(nsNew);
					xe.setDefAttr(z);
				}
			}
		}
		// change child nodes
		XNode[] nodes = new XNode[xe._childNodes.length];
		System.arraycopy(xe._childNodes, 0, nodes, 0, nodes.length);
		xe._childNodes = nodes;
		for (int i = 0; i < nodes.length; i++) {
			XNode x = nodes[i];
			if (x.getKind() == XMELEMENT) {
				String ns = x.getNSUri();
				if (ns != null && ns.equals(nsOrig)) {
					if (nsNew == null) {
						nodes = new XNode[xe._childNodes.length - 1];
						System.arraycopy(xe._childNodes, 0, nodes, 0, i);
						for (int j = i+1; j < xe._childNodes.length ; j++) {
							nodes[j-1] = xe._childNodes[j];
						}
						xe._childNodes = nodes;
						i--;
					} else {
						XElement z = new XElement((XElement) x);
						z.changeNS(nsNew);
						z.setReference(false);
						nodes[i] = z;
						changeModelNS((XElement) nodes[i], nsOrig, nsNew, hs);
					}
				}
			}
		}
	}

	private void compileXChild(final XNode parentNode,
		final XElement lastElement,
		final PNode pnode,
		final XDefinition xdef,
		final int level,
		final byte xon) {
		String xchildName = pnode._name.getString();
		XNode newNode;
		SBuffer sval;
		short parentKind = parentNode.getKind();
		XElement xel;
		if (parentKind == XMELEMENT
			&& (xel = (XElement) parentNode)._template) {
			if (pnode._nsindex == XPreCompiler.NS_XDEF_INDEX) {
				switch (pnode._localName) {
					case "text": {
						_precomp.chkNestedElements(pnode);
						sval = pnode._value;
						if (xel._trimText == 'T' || xel._textWhiteSpaces == 'T') {
							if (sval.getString().trim().length() == 0) {
								return;
							}
						}
						XData xtxt = new XData("$text",
						null, xdef.getXDPool(), XMTEXT);
						xtxt.setXDPosition(parentNode.getXDPosition()+"/$text");
						_scriptCompiler.setSource(sval,
							_scriptCompiler._actDefName,
							pnode._xdef,
							pnode._xdVersion,
							pnode._nsPrefixes,
							pnode._xpathPos+"/text()");
						_scriptCompiler.isSpaces();
						if (_scriptCompiler.isToken("$$$script:")) {
							_scriptCompiler.isSpaces();
							_scriptCompiler.isChar(';');
							_scriptCompiler.compileDataScript(xtxt);
						} else {
							_scriptCompiler.genTemplateData(xtxt, xel);
						}
						pnode._value = null;
						newNode = xtxt;
					}
					break;
					case "comment": {//TODO
						sval = pnode._value;
						XComment xcomment = new XComment(xdef.getXDPool());
						xcomment.setSPosition(copySPosition(sval));
						_scriptCompiler.setSource(sval,
							_scriptCompiler._actDefName,
							pnode._xdef,
							pnode._xdVersion,
							pnode._nsPrefixes,
							pnode._xpathPos);
						_scriptCompiler.isSpaces();
						if (_scriptCompiler.isToken("$$$script:")) {
							_scriptCompiler.isSpaces();
							_scriptCompiler.isChar(';');
							_scriptCompiler.compileDataScript(xcomment);
						} else {
							_scriptCompiler.genTemplateData(xcomment, xel);
						}
						newNode = xcomment;
					}
					break;
//					case "PI":.equals(_actPNode._localName)) { //TODO
//						sval = pnode._value;
//						XPI newPI = new XPI(xchildName, xdef.getDefPool());
//						newPI.setSPosition(new SPosition(sval, false));
//						_scriptCompiler.setSource(sval,
//							_scriptCompiler._actDefName, pnode._nsPrefixes);
//						_scriptCompiler.isSpaces();
//						if (_scriptCompiler.isToken("$$$script:")) {
//							_scriptCompiler.isSpaces();
//							_scriptCompiler.isChar(';');
//							_scriptCompiler.compileDataScript(newPI);
//						} else {
//							_scriptCompiler.genTemplateData(newPI, xel);
//						}
//						newNode = newPI;
//						break;
//					}
//					case "document".equals(_actPNode._localName)) //TODO
//						newNode = new XDocument("$document", null, xdef);
//						break;
//					case "value".equals(_actPNode._localName)) //TODO
//						newNode = new XData("$text",
//							null, xdef.getDefPool(), XMTEXT);
//						break;
//					case "attlist".equals(_actPNode._localName)) //TODO
//						newNode = createReference(pnode, pnode._localName,xdef);
//						break;
					default:
						//Element from namespace of XDefinitions is not
						//allowed here
						error(pnode._name, XDEF.XDEF322);
						return;
				}
			} else {//XElement
				xel = new XElement(xchildName, pnode._nsURI, xdef);
				_scriptCompiler._g._varBlock = new XVariableTable(_scriptCompiler._g._varBlock,xel.getSqId());
				xel.setSPosition(copySPosition(pnode._name));
				_scriptCompiler.genTemplateElement(xel, parentNode);
				newNode = xel;
			}
		} else if ((pnode._xonMode & XConstants.XON_ROOT) != 0) {// xon, ini
			pnode._xonMode = XConstants.XON_MODE_W;
			compileXChild(xdef, null, pnode, xdef, 1, XConstants.XON_MODE_W);
			pnode._xonMode = (XConstants.XON_MODE_W | XConstants.XON_ROOT);
			return;
		} else if (pnode._nsindex == XPreCompiler.NS_XDEF_INDEX) {
			switch (pnode._localName) {
				case "data":
					reportDeprecated(pnode._name, "\"data\"", "\"text\"");
				case "text":
					_precomp.chkNestedElements(pnode);
					XData xtext = new XData("$text", null, xdef.getXDPool(), XMTEXT);
					xtext.setSPosition(copySPosition(pnode._name));
					newNode = xtext;
					PAttr pa = _precomp.getXdefAttr(pnode, "script", false, true);
					sval = pa == null ? null : pa._value;
					if (sval != null) {
						reportDeprecated(sval, "<xd:text xd:script=...","declaration of text value of model");
						_scriptCompiler.setSource(sval,
							_scriptCompiler._actDefName,
							xdef,
							pnode._xdVersion,
							pnode._nsPrefixes,
							pnode._xpathPos);
						_scriptCompiler.compileDataScript(xtext);
					} else if (pnode._value != null) {
						_scriptCompiler.setSource(pnode._value,
							_scriptCompiler._actDefName,
							xdef,
							pnode._xdVersion,
							pnode._nsPrefixes,
							pnode._xpathPos+"/text()");
						pnode._value = null;
						_scriptCompiler.compileDataScript(xtext);
						if (!xtext.getOccurence().isIgnore()
							&& xtext.getOccurence().maxOccurs() > 1) {
							//Occurrence of attribute or text value can't be more then 1
							_scriptCompiler.error(XDEF.XDEF262);
						}
					} else { //default text script
						_scriptCompiler.setSourceBuffer("optional string()");
						_scriptCompiler.compileDataScript(xtext);
					}
					_precomp.reportNotAllowedAttrs(pnode);
					break;
				case "includeChildNodes":
					reportDeprecated(
						pnode._name, "\"includeChildNodes\"", "\"list\"");
				case "list":
					_precomp.chkNestedElements(pnode);
					if (level == 1) {
						//Node '&{0}' from the name space of X-definition is not allowed here
						error(pnode._name, XDEF.XDEF265, pnode._localName);
						return;
					}
					newNode = createReference(pnode, pnode._localName, xdef);
					break;
				case "mixed":
				case "choice":
				case "sequence":
					newNode = createReference(pnode, pnode._localName, xdef);
					newNode.setSPosition(copySPosition(pnode._name));
					break;
				case "any":
					newNode = new XElement("$any", null, xdef);
					_scriptCompiler._g._varBlock =
						new XVariableTable(_scriptCompiler._g._varBlock, ((XElement)newNode).getSqId());
					((XElement) newNode).setSPosition(copySPosition(pnode._name));
					if (level == 1) {
						//Node '&{0}' from the name space of X-definition is not allowed here
						error(pnode._name, XDEF.XDEF265, pnode._localName);
						return;
					}
					break;
//				case "PI": //TODO
//					newNode = new XPI(pnode._name._source, xdef.getDefPool());
//					break;
//				case "comment": //TODO
//					newNode = new XComment(xdef.getXDPool());
//					break;
//				case "document": //TODO
//					newNode = new XDocument("$document", null, xdef);
//					break;
//				case "value": //TODO
//					newNode = new XData("$text",null, xdef.getDefPool(),XMTEXT);
//					break;
//				case "attlist": //TODO
//					newNode = createReference(pnode, pnode._localName, xdef);
//					break;
				default:
					if (level > 1 || !"macro".equals(pnode._localName)) {
						//Node '&{0}' from the name space of X-definition is not allowed here
						error(pnode. _name, XDEF.XDEF265, xchildName);
					}
					return;
			}
		} else {
			XElement x = new XElement(xchildName, pnode._nsURI, xdef);
			newNode = x;
			_scriptCompiler._g._varBlock =
				new XVariableTable(_scriptCompiler._g._varBlock, ((XElement)newNode).getSqId());
			x.setSPosition(copySPosition(pnode._name));
			if (parentKind != XMDEFINITION) {
				x.setRequired();
			}
		}
		if (newNode == null) {
			error(pnode._name, XDEF.XDEF217, pnode._name.getString()); //Unknown node '&{0}'
			return;
		}
		setXDPosition(parentNode, lastElement, newNode);
		//process attributes
		compileAttrs(pnode, _scriptCompiler._actDefName, newNode, false);
		addNode(parentNode, newNode, level, pnode._name);
		//compile child nodes
		for (PNode nodei : pnode.getChildNodes()) {
			XElement x = newNode.getKind() == XMELEMENT ? (XElement) newNode : lastElement;
			if (nodei._xdef == null) {
				nodei._xdef = xdef;
			}
			compileXChild(newNode, x, nodei, xdef, level + 1, xon);
		}
		short newKind = newNode.getKind();
		if (level == 1) {
			if (newKind == XMELEMENT) {
				if (!xdef.addModel((XElement) newNode)) {
					//Repeated specification of element '&{0}'
					error(pnode._name,XDEF.XDEF236, newNode.getName(), "&{xpath}" + pnode._xpathPos);
				}
			}
		}
		if (newKind == XMCHOICE || newKind == XMSEQUENCE || newKind == XMMIXED){
			addNode(parentNode, new XSelectorEnd(), level, pnode._name);
		}
		if (pnode._value != null && pnode._value.getString() != null) {
			_scriptCompiler.setSourceBuffer(pnode._value);
			_scriptCompiler.isSpaces();
			if (!_scriptCompiler.eos()) {
				_scriptCompiler.lightError( //Text value not allowed here
					XDEF.XDEF260, "&{xpath}" + pnode._xpathPos);
			}
			pnode._value = null; //prevent repeated message
		}
		if (newKind == XMELEMENT) {
			if (_scriptCompiler._g._varBlock != null) {
				_scriptCompiler._g._varBlock = _scriptCompiler._g._varBlock.getParent();
			}
			((XElement) newNode)._xon = xon;
		}
	}

	/** Compile header attributes of xd:def and xd:collection.
	 * @param pnode PNode item.
	 * @param xdp defPool.
	 */
	private void compileXdefHeader(final PNode pnode, final XDPool xdp) {
		String defName = pnode._xdef.getName();
		_scriptCompiler._actDefName = defName;
		XDefinition def = new XDefinition(defName,
			xdp, pnode._nsURI, pnode._name, pnode._xmlVersion);
		def.setSPosition(new SPosition(pnode._name));
		//copy _importLocal!
		_scriptCompiler._importLocals =
			def._importLocal = pnode._xdef._importLocal;
		pnode._xdef = def;
		for (Entry<String, Integer> e : pnode._nsPrefixes.entrySet()) {
			def._namespaces.put(e.getKey(),
				_codeGenerator._namespaceURIs.get(e.getValue()));
		}
		PAttr pa = _precomp.getXdefAttr(pnode, "script", false, true);
		SBuffer sval = pa == null ? null : pa._value;
		if (sval != null) {
			_scriptCompiler.setSource(sval,
				defName, pnode._xdef._importLocal, def.getXDVersion(), pnode._xpathPos);
			_scriptCompiler.compileXDHeader(def);
		}
		if (_xdefs.containsKey(def.getName())) {
			error(XDEF.XDEF268, def.getName());//XDefinition '&{0}' already exists
		}
		_xdefs.put(def.getName(), def);
	}

	private void compileXDefinition(final PNode pnode) {
		XDefinition def = pnode._xdef;
		String defName = def.getName();
		String actDefName = _scriptCompiler._actDefName;
		_scriptCompiler._actDefName = defName;
		_nodeList.add(0,def);
		//compile xmodels
		for (PNode nodei: pnode.getChildNodes()) {
			String name = nodei._localName;
			PAttr pa = nodei.getAttrNS("name", XPreCompiler.NS_XDEF_INDEX);
			SBuffer gname = pa == null ? null : pa._value;
			if (nodei._nsindex == XPreCompiler.NS_XDEF_INDEX
				&& ("choice".equals(name)
				|| "mixed".equals(name) || "sequence".equals(name)
//				|| "PI".equals(name) //TODO
//				|| "comment".equals(name) //TODO
//				|| "value".equals(name) //TODO
//				|| "document".equals(name) //TODO
//				|| "attlist".equals(name) //TODO
				|| "list".equals(name) || "any".equals(name))) {
				if ("any".equals(name)) {//any MUST use prefixed name attribute!
					if (pa == null) {
						error(nodei._name, XDEF.XDEF323, "xd:name");//Required attribute '&{0}' is missing
					} else {
						nodei.removeAttr(pa);
					}
				} else {
					pa = _precomp.getXdefAttr(nodei, "name", true, true);
					gname = pa == null ? null : pa._value;
				}
				if (gname != null) { //we create dummy element
					String dname = gname.getString() + '$' + name;
					XElement dummy = new XElement(dname, null, def);
					dummy.setSPosition(copySPosition(pnode._name));
					dummy.setXDPosition(def.getXDPosition() + dname);
					dummy._xon = nodei._xonMode; /*xx*/
					addNode(def, dummy, 1, nodei._name);
					if (!def.addModel(dummy)) {
						//Repeated specification of element '&{0}'
						error(gname, XDEF.XDEF236, gname.getString());
					} else {
						if ("list".equals(name)) {
							for (PNode pn: nodei.getChildNodes()){
								compileXChild(dummy, dummy, pn, def, 2, NOXON);
							}
						} else if (name.startsWith("att")) {
							compileAttrs(nodei, defName, dummy, true);
						} else {
							compileXChild(dummy,dummy,nodei,def,2, dummy._xon);
						}
					}
				}
				continue;
			}
			compileXChild(def, null, nodei, def, 1, NOXON);
		}
		_nodeList.clear();
		_scriptCompiler._actDefName = actDefName;
	}

	/** Compile root selection from xd:def header.
	 * @param pnode PNode item.
	 */
	private void compileRootSelection(final PNode pnode) {
		String defName = pnode._xdef.getName();
		_scriptCompiler._actDefName = defName;
		XDefinition def = pnode._xdef;
		PAttr pa = _precomp.getXdefAttr(pnode, "root", false, true);
		SBuffer sval = pa == null ? null : pa._value;
		if (sval != null) {
			_scriptCompiler.setSource(
				sval, defName, pnode._xdef, pnode._xdVersion, pnode._nsPrefixes, pnode._xpathPos);
			while (true) {
				_scriptCompiler.isSpaces();
				SPosition pos = new SPosition(_scriptCompiler);
				String refName;
				String nsURI = null;
				if (_scriptCompiler.isChar('*')) {
					refName = "*"; //any
				} else if (_scriptCompiler.isXModelPosition()) {
					refName = _scriptCompiler.getParsedString();
					//get NSUri of the reference identifier.
					int ndx = refName.indexOf('#') + 1;
					int ndx1 = refName.indexOf(':', ndx);
					Object obj;
					if (ndx1 > 0) {// get nsURI assigned to the prefix
						String prefix = refName.substring(ndx, ndx1);
						if ((obj = pnode._nsPrefixes.get(prefix)) == null) {
							//Namespace for prefix '&{0}' is undefined
							sval.putReport(Report.error(XDEF.XDEF257,
								prefix), _scriptCompiler.getReportWriter());
						}
					} else {
						obj = pnode._nsPrefixes.get("");
					}
					if (obj != null) {
						nsURI = _scriptCompiler._g._namespaceURIs.get(((Integer) obj));
					}
				} else {
					_scriptCompiler.error(pos, XDEF.XDEF213);//Reference to element model expected
					break;
				}
				CompileReference xref =
					new CompileReference(CompileReference.XMREFERENCE, def, nsURI, refName, pos);
				if (def._rootSelection.containsKey(xref.getName())) {
					_scriptCompiler.error(pos, XDEF.XDEF231, refName);//Repeated root selection &{0}
				} else {
					XNode x = xref.getTargetModel();
					if (x == null) { //Unresolved reference
						xref.putTargetError(getReportWriter());
					} else {
						if (pnode._xdVersion < XConstants.XD40
							&& x.getName().endsWith("$choice")) {
							//Reference to "xd:choice" in the "xd:root" attribute is allowed in versions 4.0
							// and higher
							_precomp.warning(pos, XDEF.XDEF803);
						}
						def._rootSelection.put(xref.getName(), x);
					}
				}
				_scriptCompiler.skipBlanksAndComments();
				if (!_scriptCompiler.isChar('|')) {
					break;
				}
			}
			_scriptCompiler.skipBlanksAndComments();
			if (!_scriptCompiler.eos()) {
				_scriptCompiler.error(sval,XDEF.XDEF216);//Unexpected character&{0}{: '}{'}
			}
		}
		//process attributes of XDefinition
		for (PAttr pattr:  pnode.getAttrs()) {
			if (pattr._name.startsWith("impl-") && pattr._localName.length()>5){
				def._properties.put(pattr._name.substring(5), pattr._value.getString());
			} else {// unknown name
				error(pattr._value, XDEF.XDEF254, pattr._name);//Attribute '&{0}' not allowed here
			}
		}
	}

	/** Get identifier of type from a model.*/
	private static short getTypeId(XMNode xn) {
		if (xn.getKind() == XMELEMENT) {
			// all elements have same type (i.e XComponent or List<XComponent>)
			return (short) (250 + (((XMElement) xn).maxOccurs() > 1 ? 0 : 1));
		} else {
			XDValue p = ((XData)xn).getParseMethod();
			return p.getItemId() == XD_PARSER ? ((XDParser) p).parsedType() : XD_STRING;
		}
	}

	/** Compile parsed definitions to the XPool.
	 * @param xdp the XPool.
	 */
	public final void compileXPool(final XDPool xdp) {
		if (_scriptCompiler == null) {
			throw new SRuntimeException(XDEF.XDEF203);//Attempt to recompile compiled pool
		}
		if (_precomp.getSources().isEmpty()) {
			getReportWriter().checkAndThrowErrorWarnings();
			throw new SRuntimeException(XDEF.XDEF903);//X-definition source is missing or incorrect&{0}{: }
		}
		_precomp.prepareMacros(); //find macro definitions and resolve macros
		precompile(); //compile definitions and groups.
		for (PNode p: _xdefPNodes) {
			compileXdefHeader(p, xdp);
		}
		for (PNode p: _xdefPNodes) {
			compileXDefinition(p);
		}
		boolean result = true;
		//just let GC do the job;
		//check integrity of all XDefinitions
		Set<XNode> hs = new HashSet<>();
		for (XDefinition x : _xdefs.values()) {
			for (XMElement xel: x.getModels()) {
				result &= checkIntegrity((XElement) xel, 0, hs);
			}
		}
		//process clearing of adopted forgets
		hs.clear();
		for (XDefinition x: _xdefs.values()) {
			for (XMElement xel: x.getModels()) {
				clearAdoptedForgets((XElement) xel, false, hs);
			}
		}
		hs.clear();
		//update selectors
		for (XDefinition x : _xdefs.values()) {
			for (XMElement xel: x.getModels()) {
				updateSelectors((XElement) xel, 0, null, false, false, hs);
			}
		}
		hs.clear(); //let's gc do the job
		//resolve root references for all XDefinitions
		for (PNode p: _xdefPNodes) {
			compileRootSelection(p);
			for (int i = 1; i < p._xdef._importLocal.length; i++) {
				String s = p._xdef._importLocal[i];
				if (xdp.getXMDefinition(s.substring(0,s.length()-1)) == null) {
					//Item "&{0}" in the attribute "xd:importLocal" is not name of X-definition
					_scriptCompiler.error(p._name, XDEF.XDEF413, s.substring(0,s.length()-1));
				}
			}
		}
		compileLexicons(_lexicon, xdp); // compile lexicon
		_xdefPNodes.clear(); // Let GC make the job
		_precomp.getSources().clear();
		_precomp.getIncluded().clear();
		_nodeList.clear();
		if (!result) {
			error(XDEF.XDEF201); //Error of XDefinitions integrity
		} else {
			try {
				// set code to xdp
				((XPool) xdp).setCode(_codeGenerator._code,
					_codeGenerator._globalVariables.getLastOffset()+1,
					_codeGenerator._localVariablesMaxIndex + 1,
					_codeGenerator._spMax + 1,
					_codeGenerator._init,
					_codeGenerator._parser._xdVersion,
					_codeGenerator._lexicon);
				XVariableTable variables = new XVariableTable(0);
				// set variables to xdp
				for (XMVariable xv:
					_codeGenerator._globalVariables.toArray()) {
					CompileVariable v = (CompileVariable) xv;
					v.setValue(null); //Clear assigned value
					v.clearPostdefs();//Clear postdefs (should be already clear)
					variables.addVariable(v);
				}
				((XPool) xdp).setVariables(variables);
				// set debug info to xdp
				if (_codeGenerator._debugInfo != null) {
					((XPool) xdp).setDebugInfo(_codeGenerator._debugInfo);
				}
				// set X-components to xdp
				Set<String> classNames = new HashSet<>();
				// create map of components
				Map<String, String> x = new LinkedHashMap<>();
				for (Map.Entry<String, SBuffer> en:
					_codeGenerator._components.entrySet()) {
					XMNode xn = (XMElement) xdp.findModel(en.getKey());
					if (xn == null || xn.getKind() != XMELEMENT) {
						error(en.getValue(), XDEF.XDEF353, en.getKey());//Unresolved reference &{0}
					} else {
						String s = en.getValue().getString();
						x.put(en.getKey(), s);
						if (!s.startsWith("%ref ")) {
							// Extract qualified class name to be generated
							if (s.startsWith("interface ")) {
								s = s.substring(10); //remove inteface
							}
							int ndx = s.indexOf(' ');
							if (ndx >= 0) { //remove rest of command
								s = s.substring(0, ndx);
							}
							if (!classNames.add(s)) {
								//Class name &{0} is used in other command
								error(en.getValue(), XDEF.XDEF383, s);
							}
						}
					}
				}
				((XPool) xdp).setXComponents(x);
				// binds
				x = new LinkedHashMap<>();
				for (Map.Entry<String, SBuffer> en:
					_codeGenerator._binds.entrySet()) {
					XMNode xn = xdp.findModel(en.getKey());
					if (xn == null || xn.getKind() != XMELEMENT && xn.getKind() != XMATTRIBUTE
						&& xn.getKind() != XMTEXT) {
						error(en.getValue(), XDEF.XDEF353, en.getKey());//Unresolved reference &{0}
						continue;
					}
					// if this bind item is connected to a class (and extends component)
					String s = en.getValue().getString();
					int ndx = s.indexOf(" %with ");
					if (ndx > 0) {
						short typ = getTypeId(xn);
						// Check if all binds conneted to the same class have the same type.
						for (Map.Entry<String, SBuffer> en1:
							_codeGenerator._binds.entrySet()) {
							if (!en.getKey().equals(en1.getKey())
								&& en.getValue().getString().equals(en1.getValue().getString())) {
								XMNode xm = xdp.findModel(en1.getKey());
								if (xm == null) {
									error(en1.getValue(),XDEF.XDEF353,en.getKey());//Unresolved reference &{0}
								} else if (typ != getTypeId(xdp.findModel(en1.getKey()))) {
									// same name in same class must have same typ
									s = s.substring(ndx + 7, s.indexOf(' '));
									//Types of items &{0},&{1} bound to class &{2} differs
									error(en1.getValue(),XDEF.XDEF358, en.getKey(), en1.getKey(), s);
								}
							}
						}
					}
					x.put(en.getKey(), en.getValue().getString());
				}
				((XPool) xdp).setXComponentBinds(x);
				// enumerations
				x = new LinkedHashMap<>();
				for (String name: _codeGenerator._enums.keySet()) {
					int ndx;
					SBuffer sbf = _codeGenerator._enums.get(name);
					String s = sbf.getString();
					ndx = s.indexOf('#');
					// set XDefinition name
					_codeGenerator._parser._actDefName = s.substring(0, ndx);
					// qualified name of class
					String clsname = s.substring(ndx + 1);
					CompileVariable var = null;
					XDefinition xdef = (XDefinition) xdp.getXMDefinition(s.substring(0, ndx));
					if (xdef != null) {
						_codeGenerator._parser._importLocals = xdef._importLocal;
						var = _codeGenerator.getVariable(name);
					}
					if (var == null) {
						error(sbf, XDEF.XDEF380, name);//Enumeration &{0} is not declared as a type
					} else {
						XDValue xv = _codeGenerator._code.get(var.getParseMethodAddr());
						if (xv.getItemId() == XD_PARSER) {
							XDParser p = (XDParser) xv;
							XDContainer xc = p.getNamedParams();
							if (xc != null && (p instanceof XDParseEnum
								&& (xv = xc.getXDNamedItemValue("argument")) != null)) {
								xc = (XDContainer) xv;
							} else {
								error(sbf, XDEF.XDEF381, name);//Type &{0} can't be converted to enum
								continue;
							}
							XDValue[] names = xc.getXDItems();
							if (names!=null && names.length > 0) {
								boolean wasError = false;
								for (XDValue item: names) {
									s = item==null ? null : item.stringValue();
									if (!StringParser.chkJavaName(s)) {
										wasError = true;
										//Type &{0} can't be converted toenumeration &{1} because value
										//"&{2}" is not Java identifier
										error(sbf, XDEF.XDEF382, name, clsname, s);
									}
								}
								if (!wasError) {
									s = clsname;
									for (XDValue item: names) {
										s += " " + item.stringValue();
									}
									x.put(p.getDeclaredName(), s);
								}
							}
							if (!classNames.add(clsname)) {
								error(sbf, XDEF.XDEF383, clsname);//Class name &{0} is used in other command
							}
						} else {
							error(sbf, XDEF.XDEF380, name);//Enumeration &{0} is not declared as a type
						}
					}
				}
				((XPool) xdp).setXComponentEnums(x);
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new SRuntimeException(SYS.SYS066,ex,ex); //Internal error: &{0}
			}
			// finally check "implements" and "uses" requests
			// Note this must be done after all referrences are resolved
			boolean errs = getReportWriter().errors();
			for (CompileReference xref : _scriptCompiler._implList) {
				XNode xn = xref.getTargetModel();
				if (xn == null || xn.getKind() != XMELEMENT) {
					xref.putTargetError(getReportWriter()); //Unresolved reference
				} else {
					SPosition spos = xref.getSPosition();
					_precomp.setSystemId(spos.getSysId());
					if (errs) { // previous errors were reported
						//Comparing of models is skipped due to previous errors
						_precomp.putReport(Report.lightError(XDEF.XDEF229));
					} else {
						ArrayReporter rp =
							((XElement) xn).compareModel((XElement) xref._parent, xref.getKind()==1);
						if (rp != null) {
							Report rep;
							while((rep = rp.getReport()) != null) {
								_precomp.putReport(rep);
							}
						}
					}
				}
			}
		}
	}

	/** Get PreCompiler.
	 * @return PreCompiler object.
	 */
	public final PreCompiler getPrecompiler() {return _precomp;}

	/** Modify new selector and return null or new selector. */
	private XSelector modifyReferredSelector(final CompileReference xref, final XSelector oldSelector) {
		XSelector newSelector = new XSelector(oldSelector);
		if (xref._empty != -1) {//was specified
			newSelector.setEmptyFlag(xref._empty == 1);
		}
		if (xref.isSpecified()) {//was specified
			newSelector.setOccurrence(xref.minOccurs(), xref.maxOccurs());
		}
		if (xref._matchMethod != -1) {
			newSelector.setMatchCode(xref._matchMethod);
		}
		if (xref._initMethod != -1) {
			newSelector.setInitCode(xref._initMethod);
		}
		if (xref._absenceMethod != -1) {
			newSelector.setOnAbsenceCode(xref._absenceMethod);
		}
		if (xref._excessMethod != -1) {
			newSelector.setOnExcessCode(xref._excessMethod);
		}
		if (xref._setSourceMethod != -1) {
			newSelector.setComposeCode(xref._setSourceMethod);
		}
		if (xref._finallyMethod != -1) {
			newSelector.setFinallyCode(xref._finallyMethod);
		}
		return newSelector;
	}

	/** Copy child nodes, if position in the source and destination differs then
	 * replace selectors by a clone.
	 * @param fromList - the source array.
	 * @param fromIndex - start position in the source array.
	 * @param toList - the destination array.
	 * @param toIndex - start position in the destination data.
	 * @param length - the number of array elements to be copied.
	 */
	private void copyChildNodes(final XNode[] fromList,
		final int fromIndex,
		final XNode[] toList,
		final int toIndex,
		final int length) {
		if (length <= 0) {
			return;
		}
		System.arraycopy(fromList, fromIndex, toList, toIndex, length);
		if (fromIndex == toIndex) {
			return;
		}
		for (int i = toIndex, endIndex = toIndex + length; i < endIndex; i++) {
			XNode xn;
			if ((xn = toList[i]) != null) {
				switch (xn.getKind()) {
					case XMCHOICE:
					case XMSEQUENCE:
					case XMMIXED: toList[i] = new XSelector((XSelector) xn);
				}
			}
		}
	}

	/** Resolve references.
	 * @param xel the XElement.
	 * @param level The recursion level.
	 * @param ignoreOccurrence if <i>true</i> the occurrence specification
	 * from the referred object is ignored.
	 * @param ar node list.
	 * @return true if reverence was resolved.
	 */
	private boolean resolveReference(final XElement xel,
		final int level,
		final boolean ignoreOccurrence,
		final Set<XNode> hs) {
		boolean result = true;
		int lenx;
		if ((lenx = xel._childNodes.length) > 0
			&& (xel._childNodes[0].getKind() == CompileReference.XMREFERENCE)) {
			CompileReference xref = (CompileReference) xel._childNodes[0];
			XNode x = xref.getTargetModel();
			if (x == null || x.getKind() != XMELEMENT) {
				xref.putTargetError(getReportWriter()); //Unresolved reference
				xel._childNodes = new XNode[0];
				return false;
			}
			XElement y;
			if ((y = (XElement) x)==xel && lenx==1 && xel.getAttrs().length==0) {
				//Self reference is not allowed: &{0}
				error(xref.getSPosition(), XDEF.XDEF321, xref.getXDPosition());
				XNode[] childNodes = xel._childNodes;
				int newLen = childNodes.length -1;
				xel._childNodes = new XNode[newLen];
				if (newLen > 0) {
					copyChildNodes(childNodes, 1, xel._childNodes, 0, newLen);
				}
				return true;
			} else if (level > MAX_REFERENCE) {
				//Too many nested references or reference loop in &{0}
				error(xref.getSPosition(), XDEF.XDEF320, xref.getXDPosition());
				return false;
			} else if (!resolveReference(y, level+1, ignoreOccurrence && xel.isSpecified(), hs)) {
				return false;
			} else if (!checkIntegrity(y, level+1, hs)) {
				return false;
			} else {
				if (y.getName().indexOf('$') > 0) { //dummy element?
					XMNode xmn = y.getChildNodeModels()[0];
					if (xmn instanceof XElement) {
						y = (XElement) xmn;
					} else {
						//Reference to element model expected&
						error(xref.getSPosition(), XDEF.XDEF213, xref.getXDPosition());
					}
				}
			}
			xel.setSqId(y.getSqId());
			xel._vartable = y._vartable;
			xel._varsize = y._varsize;
			xel._varinit = y._varinit;
			xel._xon = y._xon;
			//copy specified options from target to unspecified options
			if (xel._trimAttr == 0) {// _trimAttr not set
				if (y._trimAttr != 0) {
					xel._trimAttr = y._trimAttr;
				} else if (xel._definition._trimAttr != 0) {
					xel._trimAttr = xel._definition._trimAttr;
				}
			}
			if (xel._trimText == 0) {// _trimText not set
				if (y._trimText != 0) {
					xel._trimText = y._trimText;
				} else if (xel._definition._trimText != 0) {
					xel._trimText = xel._definition._trimText;
				}
			}
			if (xel._attrWhiteSpaces == 0) { //not _attrWhiteSpaces
				if (y._attrWhiteSpaces != 0) {
					xel._attrWhiteSpaces = y._attrWhiteSpaces;
				} else if (xel._definition._attrWhiteSpaces != 0) {
					xel._attrWhiteSpaces = xel._definition._attrWhiteSpaces;
				}
			}
			if (xel._textWhiteSpaces == 0) { //TextWhiteSpaces
				if (y._textWhiteSpaces != 0) {
					xel._textWhiteSpaces = y._textWhiteSpaces;
				} else if (xel._definition._textWhiteSpaces != 0) {
					xel._textWhiteSpaces = xel._definition._textWhiteSpaces;
				}
			}
			if (xel._ignoreEmptyAttributes == 0) { //not _ignoreEmptyAttributes
				if (y._ignoreEmptyAttributes != 0) {
					xel._ignoreEmptyAttributes = y._ignoreEmptyAttributes;
				} else if (xel._definition._ignoreEmptyAttributes != 0) {
					xel._ignoreEmptyAttributes = xel._definition._ignoreEmptyAttributes;
				}
			}
			if (xel._attrValuesCase == 0) { // _attrValuesCase not set
				if (y._attrValuesCase != 0) {
					xel._attrValuesCase = y._attrValuesCase;
				} else if (xel._definition._attrValuesCase != 0) {
					xel._attrValuesCase = xel._definition._attrValuesCase;
				}
			}
			if (xel._textValuesCase == 0) {//_setTextValuesCase not set
				if (y._textValuesCase != 0) {
					xel._textValuesCase = y._textValuesCase;
				} else if (xel._definition._textValuesCase != 0) {
					xel._textValuesCase = xel._definition._textValuesCase;
				}
			}
			if (xel._acceptQualifiedAttr == 0) {//_acceptQualifiedAttr not set
				if (y._acceptQualifiedAttr != 0) {
					xel._acceptQualifiedAttr = y._acceptQualifiedAttr;
				} else if (xel._definition._acceptQualifiedAttr != 0) {
					xel._acceptQualifiedAttr = xel._definition._acceptQualifiedAttr;
				}
			}
			if (xel._ignoreComments == 0 && y._ignoreComments != 0) {
				xel._ignoreComments = y._ignoreComments;
			}
			if (xel._moreAttributes == 0 && y._moreAttributes != 0) {
				xel._moreAttributes = y._moreAttributes;
			}
			if (xel._moreElements == 0 && y._moreElements != 0) {
				xel._moreElements = y._moreElements;
			}
			if (xel._moreText == 0 && y._moreText != 0) {
				xel._moreText = y._moreText;
			}
			if (xel._nillable == 0 && y._nillable != 0) {
				xel._nillable = y._nillable;
			}
			if (xel._varinit == -1 && y._varinit != 0) {
				xel._varinit = y._varinit;
			}
			if (xel._varsize == 0 && y._varsize != 0) {// varsize not set
				xel._varsize = y._varsize;
			}
			if (xel._finaly == -1 && y._finaly != -1) {
				xel._finaly = y._finaly;
			}
			if (xel._compose == -1 && y._compose != -1) {
				xel._compose = y._compose;
			} else if (xel._compose == -1 && _scriptCompiler != null) {
				// default compose code in the refered model
				String uri = xel.getNSUri();
				String name = uri != null && !uri.isEmpty() ? '{'+uri+'}'+ xel.getLocalName() : xel.getName();
				_scriptCompiler._g.addCode(new CodeS1(XD_CONTAINER, GETELEMS_FROM_CONTEXT, 1, name));
				xel._compose = _scriptCompiler._g._lastCodeIndex;
				_scriptCompiler._g.genStop();
			}
			if (xel._init == -1 && y._init != -1) {
				xel._init = y._init;
			}
			if (xel._onAbsence == -1 && y._onAbsence != -1) {
				xel._onAbsence = y._onAbsence;
			}
			if (xel._onExcess == -1 && y._onExcess != -1) {
				xel. _onExcess = y._onExcess;
			}
			if (xel._onIllegalText == -1 && y._onIllegalText != -1) {
				xel._onIllegalText = y._onIllegalText;
			}
			if (xel._onIllegalElement == -1 && y._onIllegalElement != -1) {
				xel._onIllegalElement = y._onIllegalElement;
			}
			if (xel._onStartElement == -1 && y._onStartElement != -1) {
				xel._onStartElement = y._onStartElement;
			}
			if (xel._onIllegalAttr == -1 && y._onIllegalAttr != -1) {
				xel._onIllegalAttr = y._onIllegalAttr;
			}
			if (xel._match == -1 && y._match != -1) {
				xel._match = y._match;
			}
			if (xel._forget == 0 && xel._forget != 0 && xel._clearAdoptedForgets == 0) {// forget
				xel._forget = y._forget;
			}
			if (xel._deflt == -1 && y._deflt != -1) {
				xel._deflt = y._deflt;
			}
			if (xel._vartable == null && y._vartable != null) {
				xel._vartable = y._vartable;
			}
			if (xel._varinit == -1 && y._varinit != -1) {
				xel._varinit = y._varinit;
			}
			if (xel._varsize == -1 && y._varsize != -1) {
				xel._varsize = y._varsize;
			}
			if (!xel.isSpecified() && y.isSpecified()) {
				xel.setOccurrence(y);
			}
			xel.setReferencePos(y.getXDPosition());
			int leny = y._childNodes.length;
			String nsOrig = y.getNSUri(), nsNew = xel.getNSUri();
			if (xel._childNodes.length == 1 && xel.getAttrs().length == 0) {
				xel._attrs.putAll(y._attrs);
				xel._childNodes = y._childNodes;
				if (nsOrig != null && nsNew != null && !nsOrig.equals(nsNew)) {
					xel.setReference(false);
					// namespace of root element changed
					changeModelNS(xel, nsOrig, nsNew, new HashSet<>());
				} else {
					if (nsOrig != null && nsNew == null) {
						xel.setReference(false);
					} else {
						xel.setReference(true);
					}
				}
				return true;
			} else {
				xel.setReference(false);
				//copy old attributes and update XDPositions
				String basePos = xel.getXDPosition();
				for (String name: y.getXDAttrNames()) {
					if (!xel.hasDefAttr(name)) {// the (new) declared we leave
						// update XDPosition
						XData attr = new XData(y.getDefAttr(name, -1));
						attr.setXDPosition(basePos + "/@" + name);
						xel.setDefAttr(attr);
					}
				}
				//replace reference with child nodes of the referred node
				lenx--; //reference itself we remove
				XNode[] childNodes = new XNode[lenx + leny];
				if (leny > 0) {
					copyChildNodes(y._childNodes, 0, childNodes, 0, leny);
				}
				copyChildNodes(xel._childNodes, 1, childNodes, leny, lenx);
				xel._childNodes = childNodes;
				if (nsOrig != null && nsNew != null && !nsOrig.equals(nsNew)) {
					// namespace of root element changed
					changeModelNS(xel, nsOrig, nsNew, new HashSet<>());
				}
			}
		}
		if (!xel.isSpecified()) {
			xel.setRequired(); //interval not set, let's set defaults
		}
		int i = 0;
		lenx = xel._childNodes.length;
		while(i < lenx) {//resolve include references
			if (xel._childNodes[i].getKind() != CompileReference.XMINCLUDE) {
				i++;
				continue;
			}
			CompileReference xref = (CompileReference) xel._childNodes[i];
			if (level > MAX_REFERENCE) {
				//Too many nested references or reference loop in &{0}
				error(xref.getSPosition(), XDEF.XDEF320, xref.getXDPosition());
				return false;
			}
			XNode x = xref.getTargetModel();
			if (x == null) {
				xref.putTargetError(getReportWriter());//Unresolved reference
				XElement xe = new XElement("?", null, xel._definition);
				xe.setSPosition(xref.getSPosition());
				xe.setXDPosition(xel.getXDPosition() + "/?");
				xel._childNodes[i] = xe;
				i++;
				result = false;
				continue;
			}
			XElement y = (XElement) x;
			result &= resolveReference(y, level+1, ignoreOccurrence && xel.isSpecified(), hs);
			int leny = y._childNodes.length;
			boolean isList = y.getName().endsWith("!list");
			y = new XElement(y); //create clone of an element
			y.setXDPosition(xel.getXDPosition());
			XNode[] childNodes = new XNode[y._childNodes.length];
			int nestedSelectors = 0;
			for (int j = 0; j < childNodes.length; j++) {//modify min, max
				XNode xn = y._childNodes[j];
				switch (xn.getKind()) {
					case XMELEMENT:
						if (isList && xref.isSpecified()) {
							XElement xe = (XElement) xn;
							if (xe.minOccurs() != xref.minOccurs() || xe.maxOccurs() != xref.maxOccurs()) {
								xe = new XElement(xe);
								xe.setOccurrence(xref);
								xn = xe;
							}
						}
						break;
					case XMTEXT:
						if (isList && xref.isSpecified()) {
							XData xa = (XData) xn;
							if (xa.minOccurs() != xref.minOccurs() || xa.maxOccurs() != xref.maxOccurs()) {
								xa = new XData(xa);
								xa.setOccurrence(xref);
								xn = xa;
							}
						}
						break;
					case XMCHOICE:
					case XMMIXED:
					case XMSEQUENCE: {
						if (nestedSelectors == 0) {
							XNode xs = modifyReferredSelector(xref, (XSelector) xn);
							if (xs != null) {
								xn = xs;
							}
						} else { //we just clone nested selectors
							xn = new XSelector((XSelector) xn);
						}
						nestedSelectors++;
						break;
					}
					case XMSELECTOR_END:
						nestedSelectors--;
						break;
					default:
						break;
				}
				childNodes[j] = xn;
			}
			y._childNodes = childNodes;
			childNodes = xel._childNodes;
			if (leny == 1) {
				xel._childNodes[i] = y._childNodes[0];
			}
			int newLen = lenx + leny - 1;
			xel._childNodes = new XNode[newLen];
			if (i > 0) {
				copyChildNodes(childNodes, 0, xel._childNodes, 0, i);
			}
			if (leny > 0) {
				copyChildNodes(y._childNodes, 0, xel._childNodes, i, leny);
			}
			if (i < lenx - 1) {
				copyChildNodes(childNodes,i+1,xel._childNodes,i+leny,lenx-i-1);
			}
			lenx = newLen;
		}
		return result;
	}

	/** Check integrity of the node and resolve references.
	 * @param xel the XElement.
	 * @param level The recursion level.
	 * @param ar node list.
	 * @return true if check was successful.
	 */
	private boolean checkIntegrity(final XElement xel, final int level, final Set<XNode> hs) {
		if (!hs.add(xel)) {
			return true; //already done
		}
		boolean result = resolveReference(xel, level+1, xel.isSpecified(), hs);
		if (result) {
			for (XNode xn: xel._childNodes) {
				if (xn.getKind() == XMELEMENT && !hs.contains(xn)) {
					result &= checkIntegrity((XElement) xn, level+1, hs);
				} else if (xn.getKind() == XMTEXT) {
					if (!xn.isSpecified()) {
						xn.setOptional();
					}
				}
			}
		}
		if (!xel.isSpecified()) {
			xel.setRequired(); //interval not set, let's set default required
		}
		return result;
	}

	/** Update selectors.
	 * @param xel the XElement.
	 * @param index index of item where updating starts.
	 * @param selector selector or <i>null</i>.
	 * @param ignorableFlag flag if the item can be ignored.
	 * @param selectiveFlag flag if the item is selective in choice section.
	 * @param hs hash map with processed X-nodes.
	 * @return index of last processed item.
	 */
	private int updateSelectors(final XElement xel,
		final int index,
		final XSelector selector,
		final boolean ignorableFlag,
		final boolean selectiveFlag,
		final Set<XNode> hs) {
		hs.add(xel);
		Map<String, Integer> groupItems = new LinkedHashMap<>();
		boolean ignorable = ignorableFlag;
		boolean selective = selectiveFlag;
		boolean notReported = true;
		boolean empty = true;
		short selectorKind = selector == null ? XMSEQUENCE : selector.getKind();
		for (int i = index; i < xel._childNodes.length; i++) {
			XNode xn = xel._childNodes[i];
			short kind;
			switch (kind = xn.getKind()) {
				case XMTEXT: {
					XData x;
					int min;
					min = (x = (XData) xn).minOccurs();
					if (selectorKind==XMCHOICE) {
						ignorable |= min <= 0;
					} else {
						ignorable &= min <= 0;
					}
					empty &= min <= 0;
					selective = false;
					if ((selectorKind==XMCHOICE || selectorKind==XMMIXED) && x._match < 0) {
						//we igore items with match
						String s = "$text";
						Integer j;
						if ((j = groupItems.get(s)) != null && notReported) {
							XData y = (XData) xel._childNodes[j];
							if (y._match == -1) { // we accept items with match
								//Ambiguous group '&{0}' (equal items) in XDefinition '&{1}'
								error(x.getSPosition(), XDEF.XDEF234,
									selectorKind==XMCHOICE ? "choice" : "mixed", xel._definition.getName());
								notReported = false;
							}
						}
						groupItems.put(s, i);
					} else if (selectorKind==XMSEQUENCE && i > 0
						&& xel._childNodes[i-1].getKind() == XMTEXT
						&& ((XData) xel._childNodes[i-1])._match < 0) {
						//Ambiguous X-definition: text node can´t follow previous text node
						error(x.getSPosition(), XDEF.XDEF239);
						notReported = false;
					}
					continue;
				}
				case XMELEMENT: {
					int min;
					XElement x;
					min = (x = (XElement) xn).minOccurs();
					if (!hs.contains(x)) {
						updateSelectors(x, 0, null, false, false, hs);
					}
					if (selectorKind == XMCHOICE) {
						ignorable |= min == 0;
					} else {
						ignorable &= min <= 0;
					}
					empty &= min <= 0;
					selective = false;
					if (selectorKind==XMCHOICE || selectorKind==XMMIXED) {
						String s = x.getNSUri() == null
							? x.getName() : ('{'+x.getNSUri()+'}'+x.getName());
						Integer j;
						if ((j = groupItems.get(s)) != null && notReported) {
							XElement y = (XElement) xel._childNodes[j];
							if (y._match == -1) {// we accept items with match
								error(x.getSPosition(),
									XDEF.XDEF234,//Ambiguous group '&{0}' (equal items) in XDefinition '&{1}'
									selectorKind==XMCHOICE ? "choice" : "mixed",
									xel._definition.getName());
								notReported = false;
							}
						}
						groupItems.put(s, i);
					} else if (selectorKind==XMSEQUENCE && i > 0
						&& xel._childNodes[i-1].getKind() == XMELEMENT
						&& x.getName().equals(xel._childNodes[i-1].getName())) {
						// get previous node (we know it is XElement)
						XElement y = (XElement) xel._childNodes[i-1];
						if (y.isSpecified() && y.maxOccurs() != y.minOccurs() && y._match==-1) {
							if (y.maxOccurs() == Integer.MAX_VALUE) {
								//Ambiguous X-definition: previous element with same name has unlimited
								// occurrence
								error(x.getSPosition(), XDEF.XDEF238);
							} else if (!x.isSpecified()  // (occurrence)
								|| x.minOccurs() > 0){
								//Ambiguous X-definition: minimum occurrence must be zero
								error(x.getSPosition(),XDEF.XDEF235);
								notReported = false;
							}
						}
					}
					continue;
				}
				case XMCHOICE:
				case XMSEQUENCE:
				case XMMIXED: {
					XSelector xs = (XSelector) xn;
					if (hs.add(xs)) { //not processed yet
						xs.setBegIndex(i);
						selective = kind==XMCHOICE||selective&&kind==XMSEQUENCE;
						xs.setSelective(selective);
						i = updateSelectors(xel,
							i + 1,
							xs,
							kind == XMCHOICE || ignorable,
							selective,
							hs);
						xs.setEndIndex(i);
						if (i - xs.getBegIndex() <= 1) {

							error(xs.getSPosition(), XDEF.XDEF325, //Empty group '&{0}' in XDefinition '&{1}'
								xs.getName().substring(1), xel._definition.getName());
							xs.setIgnorable(ignorable = true);
						}
						if (xs.getKind() == XMCHOICE
							&& xs.minOccurs() <= 0) {
							xs.setIgnorable(ignorable);
						} else {
							xs.setIgnorable(ignorable || xs.minOccurs() <= 0);
						}
						if (xs.isEmptyDeclared()) {
							xs.setEmptyFlag(!xs.isEmptyFlag());
						}
						ignorable &= xs.isIgnorable();
						empty &= xs.minOccurs() <= 0;
						continue;
					} else {//already processed
						return xs.getEndIndex();
					}
				}
				case XMSELECTOR_END:
					if (selector != null && selectorKind == XMMIXED) {
						if (!selector.isSpecified()) {
							selector.setOccurrence(empty ? 0 : 1, 1);
						} else if (selector.minOccurs() > 1 || selector.maxOccurs() > 1) {
							error(selector.getSPosition(), XDEF.XDEF115);
						}
					}
					return i;
				default:
			}
		}
		return xel._childNodes.length;
	}

	/** Clear adopted forgets.
	 * @param xel XElement.
	 * @param clear adopted clear flag.
	 * @param hs set with nodes.
	 */
	private void clearAdoptedForgets(final XElement xel, final boolean clear, final Set<XNode> hs) {
		hs.add(xel);
		boolean clr = clear | xel._clearAdoptedForgets == 'T';
		boolean newChildNodes = false;
		for (int i = 0; i < xel._childNodes.length; i++) {
			XNode dn = xel._childNodes[i];
			if (dn.getKind() == XMELEMENT) {
				XElement xe = (XElement) dn;
				if (clr) {
					if (xe._forget == 'T') {
						if (!newChildNodes) {
							XNode[] oldNodes = xel._childNodes;
							copyChildNodes(oldNodes, 0, xel._childNodes, 0, oldNodes.length);
							newChildNodes = true;
						}
						xe = new XElement(xe);
						xe._forget = 0;
						xel._childNodes[i] = xe;
					}
				}
				if (!hs.contains(xe)) {
					clearAdoptedForgets(xe, clr, hs);
				}
			}
		}
	}
}