package test.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import cz.syntea.xdef.sys.FUtils;
import cz.syntea.xdef.sys.ReportWriter;
import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.util.gencollection.XDGenCollection;
import cz.syntea.xdef.xml.KXmlUtils;



public class TestUtil {
	
	
    /**
     * @param clazz root class of the resource
     * @param name name of the resource
     * @return URL of the resource clazz/name
     * @throws CommonBaseIOExc when doesn't exist
     */
    public static URL getResrc(Class<?> clazz, String name) {
        URL url = clazz.getResource(name);
        
        if (url == null) {
            String msg = "resource not found: class=" + clazz.getName() + ", name=" + name;
            logger.error(msg);
            throw new RuntimeException(msg);
        }
        
        logger.debug("resource found: class=" + clazz.getName() + ", name=" + name);
        
        return url;
    }
    
    /**
     * @param clazz root class of the resource
     * @param name name of the resource
     * @return stream of the resource clazz/name, viz {@link #getResrc(Class, String)}
     * @throws CommonBaseIOExc when couldn't open stream
     */
    public static InputStream getResrcIS(Class<?> clazz, String name) {
        try {
            return getResrc(clazz, name).openStream();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
	
    /**
     * @param clazz root class of the resource
     * @param name name of the resource
     * @return stream of the resource clazz/name, viz {@link #getResrc(Class, String)}
     * @throws CommonBaseIOExc when couldn't open stream
     */
    public static String getResrcStr(Class<?> clazz, String name) {
        try {
			return FUtils.readString(getResrcIS(clazz, name));
		} catch (SException ex) {
            throw new RuntimeException(ex);
		}
    }
    
    
    
    public static void setProperty(final String key, final String value) {
		if (value == null) {
			_props.remove(key);
		} else {
			_props.setProperty(key, value);
		}
	}
	
	
	
	public static XDPool compile(final URL url, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, url, obj));
	}
	
	public static XDPool compile(final URL[] urls, final Class<?>... obj) {
		return checkExtObjects(XDFactory.compileXD(_props, urls, obj));
	}
	
	public static XDPool compile(final File file, final Class<?>... obj) throws Exception {
		if (XDTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(file.getPath()), null);
		}
		
		return checkExtObjects(XDFactory.compileXD(_props, file, obj));
	}

	public static XDPool compile(final String xdef, final Class<?>... obj) {
		if (XDTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdef), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdef, obj));
	}

	public static XDPool compile(String[] xdefs, final Class<?>... obj) {
		if (XDTester.getFulltestMode()) {
			_xdOfxd.createXDDocument().xparse(genCollection(xdefs), null);
		}
		return checkExtObjects(XDFactory.compileXD(_props, xdefs, obj));
	}

	
	
	/**
	 * @param reporter
	 * @return cleared given reporter
	 */
	public static ReportWriter clear(ReportWriter reporter) {
		if (reporter != null) {
			reporter.clear();
		}
		
		return reporter;
	}
		
	
	
	public static Element parse(
		final XDPool xp,
		final String defName,
		final URL xml,
		final ReportWriter reporter
	) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(xml, clear(reporter));
		return result;
	}

	public static Element parse(
		final XDPool xp,
		final String defName,
		final String xml,
		final ReportWriter reporter
	) {
		XDDocument xd = xp.createXDDocument(defName);
		xd.setProperties(_props);
		Element result = xd.xparse(xml, clear(reporter));
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
		Element result = xd.xparse(el, clear(reporter));
		return result;
	}

	public static Element parse(
		final String xp,
		final String defName,
		final String xml,
		final ReportWriter reporter
	) {
		return parse(compile(xp), defName, xml, reporter);
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
		if (!XDTester.getFulltestMode()) { return xp; }
		
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
	
	
    
	public  static final String     XDEFNS         = XDTester.XDEFNS;
    public  static final String     dataDir        = "data/";
    public  static final File       dataTmpRootDir = new File("target/test-output/data-tmp");
    
	private static       Properties _props         = new Properties();
	private static final XDPool     _xdOfxd        = genXdOfXd();
	
	/** logger */
	private static final Logger     logger         = LoggerFactory.getLogger(
		TestUtil.class
	);
	
}
