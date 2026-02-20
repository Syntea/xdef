package org.xdef.impl;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefBigInteger;
import org.xdef.impl.code.DefBoolean;
import org.xdef.impl.code.DefBytes;
import org.xdef.impl.code.DefDate;
import org.xdef.impl.code.DefDecimal;
import org.xdef.impl.code.DefDouble;
import org.xdef.impl.code.DefDuration;
import org.xdef.impl.code.DefLong;
import org.xdef.impl.code.DefNull;
import org.xdef.impl.code.DefString;
import org.xdef.impl.compile.CompileXDPool;
import org.xdef.impl.debug.ChkGUIDisplay;
import org.xdef.impl.debug.XEditor;
import org.xdef.msg.SYS;
import org.xdef.msg.XDEF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.BNFGrammar;
import org.xdef.sys.BNFRule;
import org.xdef.sys.Report;
import org.xdef.sys.ReportReader;
import org.xdef.sys.ReportWriter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.STester;
import org.xdef.sys.SThrowable;

/** Builder of XPool.
 * @author Vaclav Trojan
 */
public final class XBuilder implements XDBuilder {

    private XPool _xp;

    /** Creates instance of XDBuilder with properties and external objects.
     * @param props Properties or null.
     * @param extObjects The array of classes where are available methods
     * referred from definitions (may be null).
     */
    public XBuilder(final Properties props, final Class<?>... extObjects) {this(null, props, extObjects);}

    /** Creates instance of XDBuilder with properties and external objects.
     * @param reporter ReportWriter or null.
     * @param props Properties or null.
     * @param extObjects The array of classes where are available methods referred from definitions
     * (may be null).
     */
    public XBuilder(final ReportWriter reporter,
        final Properties props,
        final Class<?>... extObjects) {
        _xp = new XPool(props, null, extObjects);
        if (reporter != null) {
            _xp._compiler.setReportWriter(reporter);
        }
        _xp._reporter = reporter;
    }

    /** Add source data of X-definition or collection. If the argument starts with "&lt;" character then
     * it is interpreted as source X-definition data, otherwise it can be the pathname of the file or URL.
     * If it is a pathname format then it may contain also wildcard characters representing a group of files.
     * @param source The string with source X-definition.
     * @param sourceId name of source source data corresponding to the argument source (may be null).
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final String source, final String sourceId) {
        _xp.setSource(source, sourceId);
        return this;
    }

    /** Add source data of X-definitions or collections. If an item starts with "&lt;" character then
     * it is interpreted as source data, otherwise it can be the pathname of the file or URL. If it is
     * a pathname format, then it may contain also wildcard characters representing a group of files.
     * @param sources The string with sources.
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final String... sources) {_xp.setSource(sources, null); return this;}

    /** Add source data of X-definitions or collections. If an item starts with "&lt;" character then it is
     * interpreted as source data, otherwise it can be the pathname of the file or URL. If it is a pathname
     * format, then it may contain also wildcard characters representing a group of files.
     * @param sources The string with sources.
     * @param sourceIds array of names of source source data corresponding to the sources argument (any item
     * or even this argument may be null).
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final String[] sources, final String[] sourceIds) {
        _xp.setSource(sources, sourceIds);
        return this;
    }

    /** Add files with source data of  X-definitions or collections.
     * @param sources array of files with sources.
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final File... sources) {_xp.setSource(sources); return this;}

    /** Add URLs with source data of X-definitions or collections.
     * @param sources array of URLs with sources.
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final URL... sources) {_xp.setSource(sources); return this;}

    /** Add input stream with source data of a X-definition or collection.
     * @param source The input stream with source.
     * @param sourceId name of source source data corresponding to stream from the argument sources (any item
     * or even this argument may be null).
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final InputStream source, final String sourceId) {
        _xp.setSource(source, sourceId);
        return this;
    }

    /** Add input streams with sources data of X-definitions or collections.
     * @param sources array of input streams with sources.
     * @param sourceIds array of names of source source data corresponding to the sources argument
     * (any item may be null).
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setSource(final InputStream sources[], final String sourceIds[]) {
        _xp.setSource(sources, sourceIds);
        return this;
    }

    /** Set class loader. The class loader must be set before setting sources.
     * @param loader class loader.
     * @return this XDBuilde object.
     */
    @Override
    public final XDBuilder setClassLoader(final ClassLoader loader) {
        _xp._compiler.setClassLoader(loader);
        return this;
    }

    /** Get compiler.
     * @return created XDPool.
     */
    public final CompileXDPool getCompiler() {return _xp._compiler;}

    /** Finish compilation and return compiled XDPool.
     * @param result compiled XDPool.
     * @param p compiler of X-definitions.
     * @param userReporter the reporter which was set by user.
     * @param reporter actual reporter.
     * @return compiled XDPool.
     */
    private XDPool finishCompilation(final XPool result,
        final CompileXDPool p,
        final ReportWriter userReporter,
        final ReportWriter reporter) {
        if (userReporter == null) { // reporter was no set.
            if (result.isChkWarnings()) {
                p.getReportWriter().checkAndThrowErrorWarnings();
            } else {
                p.getReportWriter().checkAndThrowErrors();
            }
        } else if (reporter != userReporter) {
            userReporter.addReports(reporter instanceof ArrayReporter
                ? (ArrayReporter) reporter : reporter.getReportReader());
        }
        result.clearSourcesMap(!result.isDebugMode());
        return result;
    }

    /** Build XDPool from prepared sources.
     * @return created XDPool.
     */
    @Override
    public final XDPool compileXD() {
        XPool result = _xp;
        if (result == null || result._compiler == null) {
            throw new SRuntimeException(XDEF.XDEF901); //XDPool object was already created
        }
        _xp = null;
        CompileXDPool p = result._compiler;
        result._compiler = null;
        ReportWriter userReporter = result._reporter; // user's reporter
        result._reporter = null;
        ReportWriter reporter = p.getReportWriter();
        if (reporter.errors()) {
            return finishCompilation(result, p, userReporter, reporter);
        }
        byte displayMode = result.getDisplayMode();
        boolean display = displayMode == XPool.DISPLAY_TRUE
            || reporter.errorWarnings() && (displayMode == XPool.DISPLAY_ERRORS);
        try {
            p.compileXPool(result);
        } catch (RuntimeException ex) {
            String s = STester.printThrowable(ex);
            //Program exception&{0}{: }
            reporter.putReport(display ? Report.error(SYS.SYS036, s) : Report.fatal(SYS.SYS036, s));
        }
        for (Object x: p.getPrecompiler().getIncluded()) { // add imported items
            if (x instanceof URL) {
                setSource((URL) x);
            } else {
                setSource((File) x);
            }
        }
        if (!display) {
            return finishCompilation(result, p, userReporter, reporter);
        }
        ArrayReporter ar;
        if (reporter instanceof ArrayReporter) {
            ar = (ArrayReporter) reporter;
        } else {
            ar = new ArrayReporter();
            ReportReader rr = reporter.getReportReader();
            ar.addReports(rr);
            rr.close();
        }
        Class<?>[] externals = p.getExternals(); //save external classes
        XEditor xeditor = null;
        try {
            String xdefEditor = result.getXdefEditor();
            if (xdefEditor != null) {
                Class<?> cls = Class.forName(xdefEditor);
                Constructor<?> c = cls.getDeclaredConstructor();
                c.setAccessible(true);
                xeditor = (XEditor) c.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
            | InstantiationException | NoSuchMethodException | SecurityException
            | InvocationTargetException ex) {
            xeditor = null;
            // Class with the external debug editor &{0}{"}{"} is not available.
            throw new SRuntimeException(XDEF.XDEF850, ex, result.getXdefEditor());
        }
        if (xeditor == null) {
            // create editor with the default screen position.
            xeditor = new ChkGUIDisplay(result.getXDSourceInfo());
        }
        while(!xeditor.setXEditor(result, ar)) {
            XDSourceInfo is = result.getXDSourceInfo();
            Map<String, XDSourceItem> map = is.getMap();
            // compile again
            result = new XPool(result.getProperties(),null, externals);
            XDSourceInfo is1 = result.getXDSourceInfo();
            // update source info (something might be changed)
            is1._xpos = is._xpos;
            is1._ypos = is._ypos;
            is1._width = is._width;
            is1._height = is._height;
            for (Map.Entry<String, XDSourceItem> e: map.entrySet()) {
                String key = e.getKey();
                XDSourceItem src = e.getValue();
                if (src._source != null) {
                    result.setSource(src._source, key);
                    result.getXDSourceInfo().getMap().put(key, src);
                } else if (src._url != null) {
                    result.setSource(src._url);
                }
            }
            // compile again
            p = result._compiler;
            try {
                p.compileXPool(result);
                ar = (ArrayReporter) p.getReportWriter();
            } catch (Exception ex) {
                if (!(ex instanceof SThrowable)) {
                    //Program exception&{0}{: }
                    reporter.putReport(Report.error(SYS.SYS036, STester.printThrowable(ex)));
                }
            }
            result._compiler = null;
        }
        return finishCompilation(result, p, userReporter, reporter);
    }

    /** Parse XML with X-definition declared in source input stream.
     * @param source where to read XML.
     * @param reporter used for error messages or null.
     * @return created XDDocument object.
     * @throws SRuntimeException if an error occurs.
     */
    public static final XDDocument xparse(final InputStream source, final ReportWriter reporter)
        throws SRuntimeException {
        ChkDocument chkdoc = new ChkDocument((Properties) null);
        chkdoc.xparse(source, null, reporter);
        return chkdoc;
    }

    /** Parse XML with X-definition declared in source.
     * @param source URL, pathname direct to XML or direct XML.
     * @param reporter used for error messages or null.
     * @return created XDDocument object.
     * @throws SRuntimeException if an error occurs.
     */
    public static final XDDocument xparse(final String source, final ReportWriter reporter)
        throws SRuntimeException {
        ChkDocument chkdoc = new ChkDocument((Properties) null);
        chkdoc.xparse(source, reporter);
        return chkdoc;
    }

    /** Create XDValue object.
     * @param obj the object from which XDValue will be created.
     * It may be one of:
     * <ul>
     * <li>XDValue
     * <li>String
     * <li>Short, Integer, Long
     * <li>Float, Double
     * <li>BigDecimal
     * <li>BNFGrammar
     * <li>BNFRule
     * <li>Boolean
     * <li>Calendar, SDatetime
     * <li>SDuration
     * </ul>
     * @return new XDValue object.
     * @throws RuntimeException if the object from argument is not possible
     * to convert to XDValue object.
     */
    public static final XDValue createXDValue(final Object obj) {
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
        } else if (obj instanceof BigInteger) {
            return new DefBigInteger((BigInteger) obj);
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