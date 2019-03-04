package org.xdef.impl.compile;

import org.xdef.sys.Report;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SBuffer;
import org.xdef.sys.SPosition;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/** Interface for source pre compiler (XML, JSON etc)
 * @author Vaclav Trojan
 */
public interface PreCompiler {

	/** Parse string and addAttr it to the set of definitions.
	 * @param source The source string with definitions.
	 */
	public void parseString(final String source);

	/** Parse string and addAttr it to the set of X-definitions.
	 * @param source source string with X-definitions.
	 * @param srcName pathname of source (URL or an identifying name or null).
	 */
	public void parseString(final String source, final String srcName);

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param file The file with with X-definitions.
	 */
	public void parseFile(final File file);

	/** Parse InputStream source X-definition and addAttr it to the set
	 * of definitions.
	 * @param in input stream with the X-definition.
	 * @param srcName name of source data used in reporting (SysId) or
	 * <tt>null</tt>.
	 */
	public void parseStream(final InputStream in, final String srcName);

	/** Parse data with source X-definition given by URL and addAttr it
	 * to the set of X-definitions.
	 * @param url URL of the file with the X-definition.
	 */
	public void parseURL(final URL url);

	/** Parse file with source X-definition and addAttr it to the set
	 * of definitions.
	 * @param fileName pathname of file with with X-definitions.
	 */
	public void parseFile(final String fileName);

	/** Get code generator.
	 * @return the code generator.
	 */
	public CompileCode getCodeGenerator();

	/** Get sources of X-definitions.
	 * @return array with sources of X-definitions.
	 */
	public List<Object> getSources();

	/** Get precompiled sources (PNodes) of X-definition items.
	 * @return array with PNodes with X-definitions.
	 */
	public List<PNode> getPXDefs();

	/** Get precompiled sources (PNodes) of Lexicon items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPLexiconList();

	/** Get precompiled sources (PNodes) of collection items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPCollections();

	/** Get precompiled sources (PNodes) of declaration items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPDeclarations();

	/** Get precompiled sources (PNodes) of components items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPComponents();

	/** Get precompiled sources (PNodes) of BNF Grammar items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPBNFs();

	/** Check if the node has no nested child nodes.
	 * @param pnode PNode to be tested.
	 */
	public void chkNestedElements(final PNode pnode);

	/** Get namespace URI on given position.
	 * @param i position
	 * @return uri on this position or null.
	 */
	public String getNSURI(final int i);

	/** Get namespace URI index of given uri.
	 * @param uri uri to he found.
	 * @return index of uri from argument.
	 */
	public int getNSURIIndex(final String uri);

	/** Set URI on given index.
	 * @param i index where to set.
	 * @param uri URI to set.
	 * @return original URI or null.
	 */
	public String setURIOnIndex(final int i, final String uri);

	/** Set URI. If the URI already exists just return the index
	 * @param uri URI to be set.
	 * @return index of uri.
	 */
	public int setNSURI(final String uri);

	/** Report not legal attributes. All allowed attributes should be
	 * processed and removed. Not legal attributes generates an error message.
	 * @param pnode node to be checked.
	 */
	public void reportNotAllowedAttrs(final PNode pnode);

	/** Get attribute of given name with or without name space prefix from
	 * node. The attribute is removed from the list. If the argument
	 * required is set to true put error message that required attribute
	 * is missing.
	 * @param pnode where to find attribute.
	 * @param localName The local name of attribute.
	 * @param required if true the attribute is required.
	 * @param remove if true the attribute is removed.
	 * @return the object SParsedData with the attribute value or null.
	 */
	public SBuffer getXdefAttr(final PNode pnode,
		final String localName,
		final boolean required,
		final boolean remove);

	/** Prepare list of declared macros and expand macro references. */
	public void prepareMacros();

	/** Set System ID for error reporting.
	 * @param sysId System id.
	 */
	public void setSystemId(final String sysId);

	/** Get report writer.
	 * @return the report writer.
	 */
	public ReportWriter getReportWriter();

	/** Set report writer.
	 * @param x the report writer to be set.
	 */
	public void setReportWriter(final ReportWriter x);

	/** Put fatal error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void fatal(final SPosition pos,
		final long registeredID, final Object... mod);

	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(final SPosition pos,
		final long registeredID, final Object... mod);

	/** Put ligthError message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void lightError(final SPosition pos,
		final long registeredID, final Object... mod);

	/** Put error message.
	 * @param pos SPosition
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void warning(final SPosition pos,
		final long registeredID, final Object... mod);

	/** Put report to reporter.
	 * @param pos SPosition
	 * @param rep Report.
	 */
	public void putReport(final SPosition pos, final Report rep);

	/** Put error to compiler reporter.
	 * @param registeredID registered report id.
	 * @param mod Message modification parameters.
	 */
	public void error(final long registeredID, final Object... mod);

	/** Put report to compiler reporter.
	 * @param rep Report.
	 */
	public void putReport(final Report rep);

 }