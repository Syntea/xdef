package bugreports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xdef.XDContainer;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.XDValue;
import org.xdef.impl.XDReader;
import org.xdef.impl.XDSourceInfo;
import org.xdef.impl.XDWriter;
import org.xdef.impl.XDebugInfo;
import org.xdef.impl.XDefinition;
import org.xdef.impl.XElement;
import org.xdef.impl.XLexicon;
import org.xdef.impl.XNode;
import org.xdef.impl.XPool;
import org.xdef.impl.XVariableTable;
import org.xdef.impl.code.DefContainer;
import org.xdef.model.XMDefinition;
import org.xdef.msg.SYS;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import org.xdef.sys.SUtils;
import org.xdef.sys.StringParser;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 * @author Trojan
 */
public class TestGenJava extends XDTester {

	public TestGenJava() {super();}

	private static void setFieldBool(final XPool p, final Class<?> x, final String name) throws Exception {
		Field y = x.getDeclaredField(name);
		boolean z = y.getBoolean(null);
		if (z) {
			y = p.getClass().getDeclaredField(name);
			y.setAccessible(true);
			y.setBoolean(p, z);
		}
	}

	private static void setFieldByte(final XPool p, final Class<?> x, final String name) throws Exception {
		Field y = x.getDeclaredField(name);
		byte z = y.getByte(null);
		if (z != 0) {
			y = p.getClass().getDeclaredField(name);
			y.setAccessible(true);
			y.setByte(p, z);
		}
	}

	private static void setFieldInt(final XPool p, final Class<?> x, final String name) throws Exception {
		Field y = x.getDeclaredField(name);
		int z = y.getInt(null);
		if (z != 0) {
			y = p.getClass().getDeclaredField(name);
			y.setAccessible(true);
			y.setInt(p, z);
		}
	}

	private static void setFieldString(final XPool p, final Class<?> x, final String name) throws Exception {
		Field y = x.getDeclaredField(name);
		String z = (String) y.get(null);
		if (z != null) {
			y = p.getClass().getDeclaredField(name);
			y.setAccessible(true);
			y.set(p, z);
		}
	}

	private static Object getField(final Class<?> x, final String name) throws Exception {
		Field y = x.getDeclaredField(name);
		y.setAccessible(true);
		return y.get(null);
	}

	private static void setField(final XPool p, final Object z, final String name) throws Exception{
		if (z != null) {
			Class<?> c = p.getClass();
			Field y = c.getDeclaredField(name);
			y.setAccessible(true);
			y.set(p, z);
		}
	}

	public static final XPool genPool(final Class<?> x) {
		ByteArrayInputStream bais;
		GZIPInputStream in;
		XDReader xr;
		Object o;
		String[] ss;
		int n;
		try {
			Constructor constr = XPool.class.getDeclaredConstructor();
			constr.setAccessible(true);
			XPool p = (XPool) constr.newInstance();
			for (String name: new String[] {"_chkWarnings","_illegalDoctype","_clearReports",
				"_ignoreUnresolvedExternals", "_locationdetails", "_resolveIncludes"}) {
				setFieldBool(p, x, name);
			}
			for (String name: new String[] {"_debugMode", "_displayMode"}) {
				setFieldByte(p, x, name);
			}
			for (String name: new String[] {"_debugEditor", "_xdefEditor"}) {
				setFieldString(p, x, name);
			}
			for (String name: new String[] {"_stackLen", "_init", "_globalVariablesSize",
				"_localVariablesMaxSize", "_stringItem", "_streamItem", "_sqId", "_maxYear", "_minYear"}) {
				setFieldInt(p, x, name);
			}
			if ((o = getField(x, "_extClasses")) != null) {
				ss = (String[]) o;
				Class<?>[] z = new Class<?>[ss.length];
				for (int i = 0; i < ss.length; i++) {
					z[i] = Class.forName(ss[i],
						false, Thread.currentThread().getContextClassLoader());
				}
				setField(p, z, "_extClasses");
			}
			if ((o = getField(x, "_defaultZone")) != null) {
				setField(p, java.util.TimeZone.getTimeZone((String) o), "_defaultZone");
			}
			if ((o = getField(x, "_specialDates")) != null) {
				SDatetime[] z = new SDatetime[(ss = (String[]) o).length];
				for (int i = 0; i < ss.length; i++) {
					z[i] = new SDatetime(ss[i]);
				}
				setField(p, z, "_specialDates");
			}
			if ((o = getField(x, "_debugInfo")) != null) {
				bais = new ByteArrayInputStream(SUtils.decodeBase64(((String) o).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				XDebugInfo z = XDebugInfo.readXDebugInfo(xr);
				setField(p, z, "_debugInfo");
			}
			if ((o = getField(x, "_code")) != null) {
				bais = new ByteArrayInputStream(SUtils.decodeBase64(((String) o).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				n = xr.readLength();
				XDValue[] z = new XDValue[n];
				for (int i = 0; i < n; i++) {
					try {
						z[i] = xr.readXD();
					} catch (IOException ex) {
						//SObject reader: incorrect format of data&{0}{: }
						throw new SRuntimeException(SYS.SYS039, ex, "code["+i+"]; " + z[i]);
					}
				}
				setField(p, z, "_code");
			}
			if ((o = getField(x, "_sourceInfo")) != null) {
				bais = new ByteArrayInputStream(SUtils.decodeBase64(((String) o).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				XDSourceInfo z = XDSourceInfo.readXDSourceInfo(xr);
				setField(p, z, "_sourceInfo");
			}
			if ((o = getField(x, "_props")) != null) {
				Properties z = new Properties();
				ss = (String[]) o;
				for (int i = 0; i < ss.length; i += 2) {
					z.setProperty(ss[i], ss[i+1]);
				}
				setField(p, z, "_props");
			}
			if ((o = getField(x, "_variables")) != null) {
				bais = new ByteArrayInputStream(SUtils.decodeBase64(((String) o).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				XVariableTable z = XVariableTable.readXD(xr);
				setField(p, z, "_variables");
			}
			if ((o = getField(x, "_debugEditor")) != null) {
				setField(p, o, "_debugEditor");
			}
			if ((o = getField(x, "_xdefEditor")) != null) {
				setField(p, o, "_xdefEditor");
			}
			if ((o = getField(x, "_components")) != null) {
				Map<String, String> z = new LinkedHashMap<>();
				ss = (String[]) o;
				for (int i = 0; i < ss.length; i += 2) {
					z.put(ss[i], ss[i+1]);
				}
				setField(p, z, "_components");
			}
			if ((o = getField(x, "_binds")) != null) {
				Map<String, String> z = new LinkedHashMap<>();
				ss = (String[]) o;
				for (int i = 0; i < ss.length; i += 2) {
					z.put(ss[i], ss[i+1]);
				}
				setField(p, z, "_binds");
			}
			if ((o = getField(x, "_enums")) != null) {
				Map<String, String> z = new LinkedHashMap<>();
				ss = (String[]) o;
				for (int i = 0; i < ss.length; i += 2) {
					z.put(ss[i], ss[i+1]);
				}
				setField(p, z, "_enums");
			}
			Map<String, XDefinition> xdefs = new LinkedHashMap<>();
			if ((o = getField(x, "_xdefs")) != null) {
				bais = new ByteArrayInputStream(
					SUtils.decodeBase64(((String) getField(x, "_xdefs")).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				n = xr.readLength();
				List<XNode> list = new ArrayList<>();
				for(int i = 0; i < n; i++) {
					try {
						xdefs.put(xr.readString(), XDefinition.readXDefinition(xr, p, list));
					} catch (IOException ex) {
						throw new SRuntimeException(SYS.SYS039, ex);//SObject reader: incorrect format of data&{0}{: }
					}
				}
				setField(p, xdefs, "_xdefs");
				//resolve root selections - references to models!
				for (int i = 0; i < n; i++) {
					String name = xr.readString();
					XDefinition xd = (XDefinition) p.getXMDefinition(name);
					int len1 = xr.readLength();
					for (int j = 0; j < len1; j++) {
						String key = xr.readString();
						XNode ref;
						if ("*".equals(key)) {
							XElement xe = new XElement("$any", null, xd);
							xe._moreAttributes = 'T';
							xe._moreElements = 'T';
							xe.setOccurrence(0, Integer.MAX_VALUE);
							xe.setXDPosition(xd.getXDPosition() + "*");
							ref = xe;
						} else {
							String refDefName = xr.readString();
							XDefinition refxd = (XDefinition) p.getXMDefinition(refDefName);
							String refName = xr.readString();
							ref = (XNode) refxd.getXDPool().findModel(refName);
						}
						xd._rootSelection.put(key, ref);
					}
				}
			}
			if ((o = getField(x, "_lexicon")) != null) {
				bais = new ByteArrayInputStream(SUtils.decodeBase64(((String) o).toCharArray()));
				in = new GZIPInputStream(bais);
				xr = new XDReader(in);
				n = xr.readLength();
				if (n > 0) {
					String[] languages = new String[n];
					for (int i = 0; i < n; i++) {
						languages[i] = xr.readString();
					}
					XLexicon z = new XLexicon(languages);
					n = xr.readLength(); // number of aliases
					for (int i = 0; i < n; i++) {
						String base = xr.readString();
						for (int j = 0; j < languages.length; j++) {
							z.setItem(base, j, xr.readString());
						}
					}
					setField(p, z, "_lexicon");
				}
			}
			return p;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void genBoolFields(final PrintWriter pw, final XPool p, final String[] names) throws Exception {
		Class<?> c = p.getClass();
		for (String s: names) {
			Field y = c.getDeclaredField(s);
			y.setAccessible(true);
			pw.println("\tpublic static final boolean " + s + " = " + y.getBoolean(p) + ";");
		}
	}

	private void genByteFields(final PrintWriter pw, final XPool p, final String[] names) throws Exception {
		Class<?> c = p.getClass();
		for (String s: names) {
			Field y = c.getDeclaredField(s);
			y.setAccessible(true);
			pw.println("\tpublic static final byte " + s + " = (byte) " + y.getByte(p) + ";");
		}
	}

	private void genIntFields(final PrintWriter pw, final XPool p, final String[] names) throws Exception {
		Class<?> c = p.getClass();
		for (String s: names) {
			Field y = c.getDeclaredField(s);
			y.setAccessible(true);
			pw.println("\tpublic static final int " + s + " = " + y.getInt(p) + ";");
		}
	}

	private void genStringFields(final PrintWriter pw, final XPool p, final String[] names) throws Exception {
		Class<?> c = p.getClass();
		for (String s: names) {
			Field y = c.getDeclaredField(s);
			y.setAccessible(true);
			String t = (String) y.get(p);
			if (t != null) {
				t = '"' + t + '"';
			}
			pw.println("\tpublic static final String " + s + " = " + t + ";");
		}
	}

	private void genXDPoolClass(final PrintWriter pw, final String cname, final String pckg, final XDPool xdp)
		throws Exception {
		ByteArrayOutputStream baos;
		GZIPOutputStream gzo;
		XDWriter xw;
		String b64;
		Map<String, String> map;
		Field y;
		pw.println("package " + pckg + ";");
		pw.println();
		pw.println("public final class " + cname + " {");
		pw.println();
		pw.println("\tpublic " + cname + "() {}");
		pw.println();
		XPool p = (XPool) xdp;
		Class c = p.getClass();
		genBoolFields(pw, p, new String[] {"_chkWarnings","_illegalDoctype","_clearReports",
			"_ignoreUnresolvedExternals", "_locationdetails", "_resolveIncludes"});
		genByteFields(pw, p, new String[] {"_debugMode", "_displayMode"});
		genIntFields(pw, p, new String[] {"_stackLen", "_init", "_globalVariablesSize",
			"_localVariablesMaxSize", "_stringItem", "_streamItem", "_sqId", "_maxYear", "_minYear"});
		genStringFields(pw, p, new String[] {"_debugEditor", "_xdefEditor"});
		String t = p.getDefaultZone() != null ? '"' + p.getDefaultZone().getID() + '"' : "null";
		pw.println("\tpublic static final String _defaultZone = " + t + ";");
		baos = new ByteArrayOutputStream();
		gzo = new GZIPOutputStream(baos);
		xw = new XDWriter(gzo);
		((XDebugInfo)p.getDebugInfo()).writeXD(xw);
		xw.close();
		b64 = new String(SUtils.encodeBase64(baos.toByteArray()));
		pw.println("\tpublic static final String _debugInfo = \"" + b64 + "\";");

		baos = new ByteArrayOutputStream();
		gzo = new GZIPOutputStream(baos);
		xw = new XDWriter(gzo);
		p.getXDSourceInfo().writeXDSourceInfo(xw);
		xw.close();
		b64 = new String(SUtils.encodeBase64(baos.toByteArray()));
		pw.println("\tpublic static final String _sourceInfo = \"" + b64 + "\";");

		XDValue[] code = p.getCode();
		baos = new ByteArrayOutputStream();
		gzo = new GZIPOutputStream(baos);
		xw = new XDWriter(gzo);
		xw.writeLength(code.length);
		for (int i = 0; i < code.length; i++) {
			xw.writeXD(code[i]);
		}
		xw.close();
		b64 = new String(SUtils.encodeBase64(baos.toByteArray()));
		pw.println("\tpublic static final String _code = \"" + b64 + "\";");
		y = c.getDeclaredField("_variables");
		y.setAccessible(true);
		XVariableTable v = (XVariableTable) y.get(p);
		if (v != null) {
			baos = new ByteArrayOutputStream();
			gzo = new GZIPOutputStream(baos);
			xw = new XDWriter(gzo);
			v.writeXD(xw);
			xw.close();
			b64 = '"' + new String(SUtils.encodeBase64(baos.toByteArray())) + '"';
		} else {
			b64 = null;
		}
		pw.println("\tpublic static final String _variables = " + b64 + ";");

		Properties props = p.getProperties();
		pw.print("\tpublic static final String[] _props = new String[] {");
		if (props != null) {
			for (Entry e: props.entrySet()) {
				String key = (String) e.getKey();
				if (key.startsWith("xdef_")) { // write only xdef properties
					pw.print("\"" + key + "\",\"" + e.getValue() + "\",");
				}
			}
		}
		pw.println("};");

		pw.print("\tpublic static final String[] _specialDates = ");
		SDatetime[] specialDates = p.getSpecialDates();
		if (specialDates != null) {
			pw.print("new String[] {");
			for (SDatetime xdt: specialDates) {
				pw.print("\"" + xdt.toString() + "\",");
			}
			pw.println("};");
		} else {
			pw.println("null;");
		}

		pw.print("\tpublic static final String[] _extClasses = ");
		y = c.getDeclaredField("_extClasses");
		y.setAccessible(true);
		Class<?>[] classes = (Class<?>[]) y.get(p);
		if (classes != null) {
			pw.print("new String[] {");
			for (Class cls: classes) {
				pw.print("\"" + cls.getName() + "\",");
			}
			pw.println("};");
		} else {
			pw.println("null;");
		}

		pw.print("\tpublic static final String[] _components = new String[] {");
		map = p.getXComponents();
		for (String key: map.keySet()) {
			pw.print("\"" + key + "\",\"" + map.get(key) + "\",");
		}
		pw.println("};");
		pw.print("\tpublic static final String[] _binds = new String[] {");
		map = p.getXComponentBinds();
		for (String key: map.keySet()) {
			pw.print("\"" + key + "\",\"" + map.get(key) + "\",");
		}
		pw.println("};");
		pw.print("\tpublic static final String[] _enums = new String[] {");
		map = p.getXComponentEnums();
		for (String key: map.keySet()) {
			pw.print("\"" + key + "\",\"" + map.get(key) + "\",");
		}
		pw.println("};");

		XMDefinition[] xmds = p.getXMDefinitions();
		baos = new ByteArrayOutputStream();
		gzo = new GZIPOutputStream(baos);
		xw = new XDWriter(gzo);
		xw.writeLength(xmds.length);
		List<XNode> list = new ArrayList<>();
		y = c.getDeclaredField("_xdefs");
		y.setAccessible(true);
		for(XMDefinition xmd: xmds) {
			xw.writeString(xmd.getName());
			((XDefinition) xmd).writeXNode(xw, list);
		}
		for(XMDefinition xmd: xmds) {
			String name = xmd.getName();
			xw.writeString(name);
			XDefinition xd = (XDefinition) xmd;
			int len;
			xw.writeLength(len = xd._rootSelection.size());
			//here are references to models, so we write names and XPositions!
			for (String key: xd._rootSelection.keySet()){
				xw.writeString(key);
				if (!"*".equals(key)) {
					XElement xel = (XElement) xd._rootSelection.get(key);
					xw.writeString(xel._definition.getName());
					xw.writeString(xel.getXDPosition());
				}
			}
		}
		xw.close();
		b64 = new String(SUtils.encodeBase64(baos.toByteArray()));
		pw.println("\tpublic static final String _xdefs = \"" + b64 + "\";");
		y = c.getDeclaredField("_lexicon");
		y.setAccessible(true);
		XDLexicon lex = (XDLexicon) y.get(p);
		baos = new ByteArrayOutputStream();
		gzo = new GZIPOutputStream(baos);
		xw = new XDWriter(gzo);
		if (lex == null) {
			pw.println("\tpublic static final String _lexicon = null;");
		} else {
			String[] languages = lex.getLanguages();
			int len;
			xw.writeLength(len = languages.length);
			for (int i = 0; i < len; i++) {
				xw.writeString(languages[i]);
			}
			String[] keys = lex.getKeys();
			xw.writeLength(len = keys.length);
			for (int i = 0; i < len; i++) {
				String key = keys[i];
				xw.writeString(key);
				String[] words = lex.findTexts(key);
				for (String word: words) {
					xw.writeString(word);
				}
			}
			xw.close();
			b64 = new String(SUtils.encodeBase64(baos.toByteArray()));
			pw.println("\tpublic static final String _lexicon = \"" + b64 + "\";");
		}
		pw.print("}");
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Get SDatetime from the string according to mask.
	 * @param mask datetime mask.
	 * @param s string data.
	 * @return parsed SDatetime object.
	 */
	private static SDatetime readTime(String mask, String s) {
		StringParser p = new StringParser(s);
		return p.isDatetime(mask) ? p.getParsedSDatetime() : new SDatetime();
	}

	/** Test if the element f can be joined with the element e.
	 * @param e first element.
	 * @param f second element.
	 * @param a name of attribute with datetime "start"
	 * @param b name of attribute with datetime "end"
	 * @param mask description of datetime format.
	 * @return true if and only if f can be joined with e.
	 */
	private static boolean join(Element e, Element f, String a, String b, String mask) {
		NamedNodeMap eatrs = e.getAttributes();
		NamedNodeMap fatrs = f.getAttributes();
		if (!e.hasAttribute(a) || !f.hasAttribute(a)
			|| !e.hasAttribute(b) || !f.hasAttribute(b)
			|| eatrs.getLength() != fatrs.getLength()) {
			return false;
		}
		SDatetime t1 = readTime(mask, e.getAttribute(b)); // e.do
		if (mask.endsWith("d")) {
			t1.addDay(1);
		} else if (mask.endsWith("H")) {
			t1.addHour(1);
		} else if (mask.endsWith("s")) {
			t1.addSecond(1);
		} else {
			return false;
		}
		if (!t1.equals(readTime(mask, f.getAttribute(a)))) { // f.od
			return false;
		}
		for (int i = 0; i < eatrs.getLength(); i++) {
			String name = eatrs.item(i).getNodeName();
			if (!a.equals(name) && !b.equals(name)
				 && !e.getAttribute(name).equals(f.getAttribute(name))) {
				return false;
			}
		}
		return true;
	}

	/** Create list of joined elements.
	 * @param x container with elements.
	 * @param a name of attribute with datetime "start"
	 * @param b name of attribute with datetime "end"
	 * @param mask description of datetime format.
	 * @return Container with joined elements.
	 */
	public static XDContainer x(XDContainer x, String a, String b, String mask) {
		XDContainer y = new DefContainer();
		if (!x.isEmpty()) {
			Element e = x.getXDElement(0);
			y.addXDItem(e);
			for (int i = 1; i < x.getXDItemsNumber(); i++) {
				Element f = x.getXDElement(i);
				if (join(e, f, a, b, mask)) {
					e.setAttribute(b, f.getAttribute(b));
				} else {
					y.addXDItem(e = f);
				}
			}
		}
		return y;
	}

	private void test(String packageName, String className , String xdef, String xml) {
		try {
			// 1. create java source with compied XDPool from xdef
			StringWriter swr = new StringWriter();
			try (PrintWriter pwr = new PrintWriter(swr)) {
				genXDPoolClass(pwr, className, packageName, XDFactory.compileXD(null, xdef));
				pwr.close();
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(swr.toString().getBytes());
			// compile created class
			File f = clearTempDir();
			copyToTempDir(bais, packageName, className);
			compileSources(f);

			// get XDPool from the created class and run it with xml.
			XDPool xp = genPool(ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className));
			ArrayReporter reporter = new ArrayReporter();
			assertEq(xml, parse(xp, "", xml, reporter));
			assertNoErrorsAndClear(reporter);
		} catch (Exception ex) {fail(ex);}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef, xml;
		xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"+;\" a=\"? int(1,2)\" />\n" +
"   </Vehicle>\n" +
"</xd:def>";
		xml = "<Vehicle><Part a='1'/><Part a='2'/><Part/></Vehicle>";
		test("bugreports", "TestGenJava1", xdef, xml);

		xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"   external method XDContainer bugreports.TestGenJava.x(XDContainer x, String a, String b, String mask);\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <B xd:script=\"occurs *; create x(from('//B'), 'x', 'y', 'yyyy-MM-dd');\"\n"+
"  a='string' b='string'\n"+
"     x=\"xdatetime('yyyy-MM-dd')\" y=\"xdatetime('yyyy-MM-dd')\"/>\n"+
"</A>\n"+
"</xd:def>";
		xml =
"<A>\n"+
"  <B a='a' b='b' x='2023-12-08' y='2023-12-31'/>\n"+
"  <B a='a' b='b' x='2024-01-01' y='2024-09-11'/>\n"+
"  <B a='a' b='b' x='2024-09-12' y='2024-09-13'/>\n"+
"  <B a='a' b='b' x='2024-09-20' y='2024-09-30'/>\n"+
"  <B a='a' b='b' x='2024-10-01' y='2024-10-02'/>\n"+
"</A>";
		test("bugreports", "TestGenJava2", xdef, xml);
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
