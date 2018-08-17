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
import test.xdef.Tester;



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
	
	
	
	public static String getErrors(final ReportWriter reporter) {
		if (reporter.errorWarnings()) {
			String msg = "XDef-reporter:\n" +
				reporter.getReportReader().printToString() + "\n";
			
			if (reporter.errors()) {
				logger.error(msg);
				return msg;
			} else {
				logger.warn(msg);
			}
		}
		
		return null;
	}
	
	
	
	/** Check equality of elements.
	 * @param act actual value.
	 * @param exp expected value.
	 */
	public static void assertEq(final String act, final Element exp) {
		assertEq(KXmlUtils.parseXml(act).getDocumentElement(), exp);
	}

	/** Check equality of elements.
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg message to be printed or null.
	 */
	public static void assertEq(final String act,
		final Element exp,
		final String msg) {
		assertEq(KXmlUtils.parseXml(act).getDocumentElement(), exp, msg);
	}

	/** Check equality of elements.
	 * @param act actual value.
	 * @param exp expected value.
	 */
	public static void assertEq(final Element act, final String exp) {
		assertEq(act, KXmlUtils.parseXml(exp).getDocumentElement());
	}

	/** Check equality of elements.
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg message to be printed or null.
	 */
	public static void assertEq(final Element act,
		final String exp,
		final String msg) {
		assertEq(act, KXmlUtils.parseXml(exp).getDocumentElement(), msg);
	}

	/** Check equality of elements.
	 * @param act first value.
	 * @param exp expected value.
	 */
	public static void assertEq(Element act, Element exp) {assertEq(act, exp, null);}

	/** Check elements are equal (text nodes are trimmed).
	 * @param act actual value.
	 * @param exp expected value.
	 * @param msg message to be printed or null.
	 */
	public static void assertEq(final Element act,
		final Element exp,
		final String msg) {
		assertEq(act, exp, msg, true);
	}
	
	/** Check equality of elements.
	 * @param act first value
	 * @param exp expected value
	 * @param msg message to be printed or null.
	 * @param trim whether trim
	 */
	public static void assertEq(
		final Element act,
		final Element exp,
		final Object msg,
		final boolean trim
	) {
		assertNoErrors(KXmlUtils.compareElements(act, exp, true, null));
	}
	
	
	
	public static XDPool compile(final URL[] urls, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, urls, obj));
	}
	
	public static XDPool compile(final File file, final Class<?>... obj) throws Exception {
		if (Tester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(file.getPath()), null);
		}
		
		return checkExtObjects(XDFactory.compileXD(_props, file, obj));
	}

	public static XDPool compile(final String xdef, final Class<?>... obj) {
		if (Tester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdef), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdef, obj));
	}

	public static XDPool compile(String[] xdefs, final Class<?>... obj) {
		if (Tester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdefs), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdefs, obj));
	}

	
	
	public static Element parse(
		final XDPool xp,
		final String defName,
		final String xml,
		final ReportWriter reporter
	) {
		if (reporter != null) {
			reporter.clear();
		}
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
		if (reporter != null) {
			reporter.clear();
		}
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
		if (!Tester.getFulltestMode()) { return xp; }
		
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
	
	
    public  static final String     XDEFNS  = Tester.XDEFNS;
    
	private static       Properties _props  = new Properties();
	private static final XDPool     _xdOfxd = genXdOfXd();
	/** logger */
	private static final Logger     logger  = LoggerFactory.getLogger(
		TestUtil.class
	);

}
