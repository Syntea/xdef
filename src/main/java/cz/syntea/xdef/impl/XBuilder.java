/*
 * File: XBuilder.java.
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.impl.code.DefDuration;
import cz.syntea.xdef.impl.code.DefBoolean;
import cz.syntea.xdef.impl.code.DefDecimal;
import cz.syntea.xdef.impl.code.DefString;
import cz.syntea.xdef.impl.code.DefNull;
import cz.syntea.xdef.impl.code.DefBNFRule;
import cz.syntea.xdef.impl.code.DefBytes;
import cz.syntea.xdef.impl.code.DefDate;
import cz.syntea.xdef.impl.code.DefLong;
import cz.syntea.xdef.impl.code.DefBNFGrammar;
import cz.syntea.xdef.impl.code.DefDouble;
import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.BNFGrammar;
import cz.syntea.xdef.sys.BNFRule;
import cz.syntea.xdef.sys.Report;
import cz.syntea.xdef.sys.SDatetime;
import cz.syntea.xdef.sys.SDuration;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.XDBuilder;
import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.XDValue;
//import cz.syntea.xd.impl.compile.CompileParser;
//import cz.syntea.xd.impl.compile.CompileXdefPool;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import cz.syntea.xdef.sys.ReportWriter;

/** Builder of XPool.
 * @author Vaclav Trojan
 */
public final class XBuilder implements XDBuilder {

	private XPool _xp;

	/** Creates instance of XDefBuilder with properties and external objects.
	 * @param props Properties or <tt>null</tt>.
	 * @param extObjects The array of classes where are available methods
	 * referred from definitions (may be <tt>null</tt>).
	 */
	public XBuilder(Properties props, final Class<?>... extObjects) {
		_xp = new XPool(props, null, extObjects);
	}

	@Override
	/** Add source data of X-definition or collection. If the argument starts
	 * with "&lt;" character then it is interpreted as source X-definition data,
	 * otherwise it can be the pathname of the file or URL. If it is a pathname
	 * format then it may contain also wildcard characters representing a group
	 * of files.
	 * @param source The string with source X-definition.
	 * @param sourceId name of source source data corresponding to
	 * the argument source (may be null).
	 */
	public void setSource(final String source, final String sourceId) {
		_xp.setSource(source, sourceId);
	}

	@Override
	/** Add source data of X-definitions or collections. If an item starts with
	 * "&lt;" character then it is interpreted as source data, otherwise
	 * it can be the pathname of the file or URL. If it is a pathname format,
	 * then it may contain also wildcard characters representing a group
	 * of files.
	 * @param sources The string with sources.
	 */
	public void setSource(final String... sources) {
		_xp.setSource(sources, null);
	}

	@Override
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
	public void setSource(final String[] sources, final String[] sourceIds) {
		_xp.setSource(sources, sourceIds);
	}

	@Override
	/** Add files with source data of  X-definitions or collections.
	 * @param sources array of files with sources.
	 */
	public void setSource(final File... sources) {_xp.setSource(sources);}

	@Override
	/** Add URLs with source data of X-definitions or collections.
	 * @param sources array of URLs with sources.
	 */
	public void setSource(final URL... sources) {_xp.setSource(sources);}

	@Override
	/** Add input stream with source data of a X-definition or collection.
	 * @param source The input stream with source.
	 * @param sourceId array of names of source source data corresponding to
	 * streams from the argument sources (any item or even this argument
	 * may be <tt>null</tt>).
	 */
	public void setSource(final InputStream source, final String sourceId) {
		_xp.setSource(source, sourceId);
	}

	@Override
	/** Add input streams with sources data of X-definitions or collections.
	 * @param sources array of input streams with sources.
	 * @param sourceIds array of names of source source data corresponding to
	 * the sources argument (any item may be null).
	 */
	public void setSource(final InputStream sources[],
		final String sourceIds[]) {
		_xp.setSource(sources, sourceIds);
	}

	@Override
	/** Set external classes with external methods.
	 * @param ext array of classes with external methods.
	 */
	public void setExternals(Class<?>... ext) {_xp._compiler.setExternals(ext);}

	@Override
	/** Set reporter. This method is should be used only for incremental
	 * message reporting. The reporter must be set before setting sources.
	 * @param reporter the reporter to be set to this builder.
	 */
	public void setReporter(final ReportWriter reporter) {
		if (reporter != null) {
			_xp._compiler.setReportWriter(reporter);
		}
		_xp._reporter = reporter;
	}

	@Override
	/** Build XDefPool from prepared sources.
	 * @return created XDefPool.
	 */
	public XDPool compileXD() {
		return build();
	}

	@Override
	/** Set class loader. The class loader must be set before setting sources.
	 * @param loader class loader.
	 */
	public void setClassLoader(final ClassLoader loader) {
		_xp._compiler.setClassLoader(loader);
	}

	/** Build XPool from prepared sources.
	 * @return created XPool.
	 */
	final XPool build() {
		XPool result;
		if ((result = _xp) == null || result._compiler == null) {
			//XDefPool object was already built
			throw new SRuntimeException(XDEF.XDEF901);
		}
		_xp = null;
		cz.syntea.xdef.impl.compile.CompileXdefPool p = result._compiler;
		result._compiler = null;
		if (result._reporter == null) {
			p.compileXPool(result);
			ReportWriter reporter = p.getReportWriter();
			boolean display = result.getDisplayMode() == XPool.DISPLAY_TRUE
				|| reporter.errorWarnings()
				&& (result.getDisplayMode() == XPool.DISPLAY_ERRORS
				 || result.isDebugMode());
			if (display) {
				Class<?>[] externals = p.getExternals(); //save external classes
				ChkGUIDisplay edit = new ChkGUIDisplay();
				ArrayReporter ar = (ArrayReporter) reporter;
				for (;;) {
					Map<String, XSourceItem> map = result._sourcesMap;
					if (edit.setGUI(result, ar)) {
						break;
					}
					result = new XPool(result.getProperties(),null, externals);
					for (Map.Entry<String, XSourceItem> e: map.entrySet()) {
						String key = e.getKey();
						XSourceItem src = e.getValue();
						if (src._source != null) {
							result.setSource(src._source, key);
							result._sourcesMap.put(key, src);
						} else if (src._url != null) {
							result.setSource(src._url);
						}
					}
					p = result._compiler;
					p.compileXPool(result);
					result._compiler = null;
					ar = (ArrayReporter) p.getReportWriter();
				}
			}
			if (result.isChkWarnings()) {
				p.getReportWriter().checkAndThrowErrorWarnings();
			} else {
				p.getReportWriter().checkAndThrowErrors();
			}
		} else {
			try {
				p.compileXPool(result);
			} catch (Exception ex) {
				result._reporter.putReport(new Report(ex));
			}
		}
		result.clearSourcesMap(!result.isDebugMode());
		return result;
	}

	/** Parse XML with X-definition declared in source input stream.
	 * @param source where to read XML.
	 * @param reporter used for error messages or <tt>null</tt>.
	 * @return created XDDocument object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDDocument xparse(final InputStream source,
		final ReportWriter reporter) throws SRuntimeException {
		ChkDocument chkdoc = new ChkDocument(new Class<?>[0], null);
		chkdoc.xparse(source, null, reporter);
		return chkdoc;
	}

	/** Parse XML with X-definition declared in source.
	 * @param source URL, pathname direct to XML or direct XML.
	 * @param reporter used for error messages or <tt>null</tt>.
	 * @return created XDDocument object.
	 * @throws SRuntimeException if an error occurs.
	 */
	public static XDDocument xparse(final String source,
		final ReportWriter reporter) throws SRuntimeException {
		ChkDocument chkdoc = new ChkDocument(new Class<?>[0], null);
		chkdoc.xparse(source, reporter);
		return chkdoc;
	}

	/** Builds XPool with properties and external objects from source.
	 * @param props Properties or <tt>null</tt>. If the argument is
	 * <tt>null</tt> then System properties object is used.
	 * @param extObjects The array of classes where are available methods
	 * referred from definitions (may be <tt>null</tt>).
	 * @param source source X-definition ()
	 * @return created XDPool object.
	 */
	static XPool build(final Properties props,
		final Class<?>[] extObjects,
		final URL source) {
		XBuilder xb = new XBuilder(props, extObjects);
		xb.setSource(source);
		return xb.build();
	}

	/** Create XDValue object.
	 * @param obj the object from which XDValue will be created.
	 * It may be one of:
	 * <ul>
	 * <li>XDValue</li>
	 * <li>String</li>
	 * <li>Short, Integer, Long</li>
	 * <li>Float, Double</li>
	 * <li>BigDecimal</li>
	 * <li>BNFGrammar</li>
	 * <li>BNFRule</li>
	 * <li>Boolean</li>
	 * <li>Calendar, SDatetime</li>
	 * <li>SDuration</li>
	 * </ul>
	 * @return new XDValue object.
	 * @throws RuntimeException if the object from argument is not possible
	 * to convert to XDValue object.
	 */
	public static XDValue createXDValue(final Object obj) {
		if (obj == null) {
			return new DefNull();
		} else if (obj instanceof XDValue) {
			return (XDValue) obj;
		} else if (obj instanceof String) {
			return new DefString((String) obj);
		} else if (obj instanceof Short ||
			obj instanceof Integer || obj instanceof Long) {
			return new DefLong(((Number) obj).longValue());
		} else if (obj instanceof Float || obj instanceof Double) {
			return new DefDouble(((Number) obj).doubleValue());
		} else if (obj instanceof BigDecimal) {
			return new DefDecimal((BigDecimal) obj);
		} else if (obj instanceof byte[]) {
			return new DefBytes((byte[]) obj);
		} else if (obj instanceof BNFGrammar) {
			return new DefBNFGrammar((BNFGrammar) obj);
		} else if (obj instanceof BNFRule) {
			return new DefBNFRule((BNFRule) obj);
		} else if (obj instanceof Boolean) {
			return new DefBoolean(((Boolean) obj));
		} else if (obj instanceof Calendar) {
			return new DefDate((Calendar) obj);
		} else if (obj instanceof SDatetime) {
			return new DefDate((SDatetime) obj);
		} else if (obj instanceof SDuration) {
			return new DefDuration((SDuration) obj);
		}
		//Can't convert the object of type '&{0}' to XDValue
		throw new SRuntimeException(XDEF.XDEF542, obj.getClass().getName());
	}
}