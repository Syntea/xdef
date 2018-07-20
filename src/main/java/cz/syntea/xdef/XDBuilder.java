/*
 * Copyright 2010 Syntea software group a.s. All rights reserved.
 *
 * File: XDBuilder.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import cz.syntea.xdef.sys.ReportWriter;

/** Provides building of {@link cz.syntea.xdef.XDPool}. This object you need to
 * use only in special case you want to make incremental building of
 * {@link cz.syntea.xdef.XDPool}. In most of cases you can create
 * {@link cz.syntea.xdef.XDPool} with static methods of
 * {@link cz.syntea.xdef.XDFactory}.
 * <p>Typical use of XDBuilder:</p>
 * <pre><tt>
 * // 1. Create XDBuilder with properties.
 * Properties props = new Properties();
 * props.setProperty(key, value); //see {@link cz.syntea.xdef.XDConstants}
 * ...
 * XDBuilder buider = XDFactory.getXDBuilder(props);
 * builder.setClassLoader(classloader); // set class loader for externals
 * builder.setReporter(reporter); // set reporter for builder error reports
 * builder.setExternals(externals); // set external objects
 * ...
 * builder.setSource(source 1); //compile source with X-definition
 * ...
 * builder.setSource(source n); //compile source with X-definition
 * ...
 * XDPool xd = builder.compileXD(); //build XDPool
 * ...
 * </tt></pre>
 *
 * @author Vaclav Trojan
 */
public interface XDBuilder {

	/** Add source data of X-definition or collection. If the argument starts
	 * with "&lt;" character then it is interpreted as source X-definition data,
	 * otherwise it can be the pathname of the file or URL. If it is a pathname
	 * format then it may contain also wildcard characters representing a group
	 * of files.
	 * @param source The string with source X-definition.
	 * @param sourceId name of source source data corresponding to
	 * the argument source (may be null).
	 */
	public void setSource(String source, String sourceId);

	/** Add source data of X-definitions or collections. If an item starts with
	 * "&lt;" character then it is interpreted as source data, otherwise
	 * it can be the pathname of the file or URL. If it is a pathname format,
	 * then it may contain also wildcard characters representing a group
	 * of files.
	 * @param sources The string with sources.
	 */
	public void setSource(String... sources);

	/** Add source data of X-definitions or collections. If an item starts with
	 * "&lt;" character then it is interpreted as source data, otherwise
	 * it can be the pathname of the file or URL. If it is a pathname format,
	 * then it may contain also wildcard characters representing a group
	 * of files.
	 * @param sources The string with sources.
	 * @param sourceIds array of names of source source data corresponding to
	 * the sources argument (any item or even this argument
	 * may be <tt>null</tt>).
	 */
	public void setSource(String[] sources, String[] sourceIds);

	/** Add files with source data of  X-definitions or collections.
	 * @param sources array of files with sources.
	 */
	public void setSource(File... sources);

	/** Add URLs with source data of X-definitions or collections.
	 * @param sources array of URLs with sources.
	 */
	public void setSource(URL... sources);

	/** Add input stream with source data of a X-definition or collection.
	 * @param source The input stream with source.
	 * @param sourceId name of source source data corresponding to
	 * stream (may be null).
	 */
	public void setSource(InputStream source, String sourceId);

	/** Add input streams with sources data of X-definitions or collections.
	 * @param sources array of input streams with sources.
	 * @param sourceIds array of names of source source data corresponding to
	 * streams from the argument sources (any item or even this argument
	 * may be <tt>null</tt>).
	 */
	public void setSource(InputStream[] sources, String[] sourceIds);

	/** Set class loader. The class loader must be set before setting sources.
	 * @param loader class loader.
	 */
	public void setClassLoader(ClassLoader loader);

	/** Set external classes with external methods.
	 * @param extClasses array of classes with external methods.
	 */
	public void setExternals(Class<?>... extClasses);

	/** Set reporter. This method is should be used only for incremental
	 * message reporting. The reporter must be set before setting sources.
	 * @param reporter the reporter to be set to this builder.
	 */
	public void setReporter(ReportWriter reporter);

	/** Compile XDefPool from prepared sources.
	 * @return compiled XDefPool.
	 */
	public XDPool compileXD();

}
