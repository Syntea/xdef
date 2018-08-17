package test.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.syntea.xdef.XDDocument;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.util.gencollection.XDGenCollection;
import cz.syntea.xdef.xml.KXmlUtils;



public class TestUtil {
	
	
	public static URL getResrc(Class<?> clazz, String name) {
		URL url = clazz.getResource(name);
		
		if (url == null) {
			String msg = "resource not found: class=" + clazz.getName() + ", name=" + name;
			logger.debug(msg);
			throw new RuntimeException(msg);
		}
		
		logger.debug("resource found: class=" + clazz.getName() + ", name=" + name);
		
		return url;
	}
	
	
	
	/**
	 * @param reporter
	 * @return see {@link #reportErrors(ReportWriter, String)}
	 */
	public static String reportErrors(final ReportWriter reporter) {
		return reportErrors(reporter, null);
	}
	
	/**
	 * @param reporter given reporter
	 * @param msg user message to be added to report-message
	 * @return report-message. If reporter doesn't contain errors then null
	 */
	public static String reportErrors(
		final ReportWriter reporter,
		final String       msg
	) {
		if (reporter.errorWarnings()) {
			String msg2 =
				(msg != null ? msg.toString().trim() + "\n" : "") +
				"XDef-reporter:\n" +
				reporter.getReportReader().printToString() + "\n";
			
			if (reporter.errors()) {
				return msg2;
			} else {
				logger.warn(msg2);
			}
		}
		
		return null;
	}
	
	
	
	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(final String act, final Element exp) {
		return reportDiff(KXmlUtils.parseXml(act).getDocumentElement(), exp);
	}

	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg user message to be added to diff-message
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(
		final String act,
		final Element exp,
		final String msg
	) {
		return reportDiff(KXmlUtils.parseXml(act).getDocumentElement(), exp, msg);
	}

	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(final Element act, final String exp) {
		return reportDiff(act, KXmlUtils.parseXml(exp).getDocumentElement());
	}

	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg user message to be added to diff-message
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(
		final Element act,
		final String exp,
		final String msg
	) {
		return reportDiff(act, KXmlUtils.parseXml(exp).getDocumentElement(), msg);
	}

	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(Element act, Element exp) {
		return reportDiff(act, exp, null);
	}

	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg user message to be added to diff-message
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(
		final Element act,
		final Element exp,
		final String msg
	) {
		return reportDiff(act, exp, msg, true);
	}
	
	/**
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg user message to be added to diff-message
	 * @param trim whether trim
	 * @return diff-message. If args equal then null
	 */
	public static String reportDiff(
		final Element act,
		final Element exp,
		final String msg,
		final boolean trim
	) {
		return reportErrors(
			KXmlUtils.compareElements(act, exp, true, null),
			msg
		);
	}
	
	
	
	public static XDPool compile(final URL[] urls, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, urls, obj));
	}
	
	public static XDPool compile(final File file, final Class<?>... obj) throws Exception {
		if (XDefTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(file.getPath()), null);
		}
		
		return checkExtObjects(XDFactory.compileXD(_props, file, obj));
	}

	public static XDPool compile(final String xdef, final Class<?>... obj) {
		if (XDefTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdef), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdef, obj));
	}

	public static XDPool compile(String[] xdefs, final Class<?>... obj) {
		if (XDefTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdefs), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdefs, obj));
	}

	
	
	/**
	 * @param reporter
	 * @return cleared given reporter
	 */
	public static ReportWriter clear(ReportWriter reporter) {
		reporter.clear();
		return reporter;
	}
		
	
	
	public static Element parse(
		final XDPool xp,
		final String defName,
		final String xml,
		final ReportWriter reporter
	) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(xml, reporter);
		return result;
	}

	public static Element parse(
		final XDPool xp,
		final String defName,
		final Element el,
		final ReportWriter reporter
	) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(el, reporter);
		return result;
	}

	
	
	private static String genCollection(final String... sources) {
		Element el;
		try {
			el = XDGenCollection.genCollection(sources,
				true, //resolvemacros
				true, //removeActions
				false);
		} catch (Exception e) {
			throw new SRuntimeException(e);
		}
		
		return KXmlUtils.nodeToString(el, true);
	}
	
	
	
	public static XDPool checkExtObjects(final XDPool xp) {
		if (!XDefTester.getFulltestMode()) { return xp; }
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			xp.writeXDPool(baos);
			baos.close();
			return XDFactory.readXDPool(
				new ByteArrayInputStream(baos.toByteArray()));
		} catch(RuntimeException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch(Error e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
    public static String exceptionStackTrace(Throwable ex) {
        
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
   
        ex.printStackTrace(pw);
        
        return sw.toString();
    }
	
    
    
    private static XDPool genXdOfXd() {
		String dir = "test/test/xdef/data/test/";
		File f = new File(dir);
		
		if (!f.exists() || !f.isDirectory()) {
			dir = "src/test/java/test/xdef/data/test/";
		}
		
		return XDFactory.compileXD(null,dir+"TestXdefOfXdef*.xdef");
	}
	
	
    public  static final String     XDEFNS         = XDefTester.XDEFNS;
    public  static final File       dataTmpRootDir = new File("target/test-output/data-tmp");
    
	private static       Properties _props  = new Properties();
	private static final XDPool     _xdOfxd = genXdOfXd();
	/** logger */
	private static final Logger     logger  = LoggerFactory.getLogger(
		TestUtil.class
	);

}
