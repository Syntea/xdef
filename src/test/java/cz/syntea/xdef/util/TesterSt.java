package cz.syntea.xdef.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.w3c.dom.Element;

import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.util.gencollection.XDGenCollection;
import cz.syntea.xdef.xml.KXmlUtils;
import test.xdef.Tester;



public class TesterSt {
	
	
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
	
	
	
	public static void assertNoErrors(final ReportWriter reporter) {
		if (reporter.errorWarnings()) {
			String msg = "XDef-reporter:\n" +
				reporter.getReportReader().printToString() + "\n";
			
			if (reporter.errors()) {
				logger.error(msg);
				Assert.assertTrue(false, msg);
			} else {
				logger.warn(msg);
			}
		}
	}
	
	
	
	public static XDPool compile(final URL[] urls, final Class<?>... obj) {
		return TesterSt.checkExtObjects(XDFactory.compileXD(_props, urls, obj));
	}
	
	public static XDPool compile(final File file, final Class<?>... obj) throws Exception {
		if (Tester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(
				TesterSt.genCollection(file.getAbsolutePath()), null);
		}
		
		return TesterSt.checkExtObjects(XDFactory.compileXD(_props, file, obj));
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
	
	
	
	private static       Properties _props  = new Properties();
	private static final XDPool     _xdOfxd = genXdOfXd();
	/** logger */
	private static final Logger     logger  = LoggerFactory.getLogger(
		TesterSt.class
	);

}
