/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.syntea.xdef.impl.compile;

import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SPosition;
import java.util.List;

/**
 *
 * @author Vaclav Trojan
 */
public interface PPreCompiler {

	/** Get code generator.
	 * @return the code generator.
	 */
	public CompileCode getCodeGenerator();

	/** Get sources of X-defintions.
	 * @return array with sources of X-defintions.
	 */
	public List<Object> getSources();

	/** Get precompiled sources (PNodes) of X-definition items.
	 * @return array with PNodes with X-definitions.
	 */
	public List<PNode> getPXDefs();

	/** Get precompiled sources (PNodes) of Thesaurus items.
	 * @return array with PNodes.
	 */
	public List<PNode> getPThesaurusList();

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

	/** Prepare list of declared macros and expand macro references. */
	public void prepareMacros();

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