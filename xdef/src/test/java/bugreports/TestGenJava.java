package bugreports;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xdef.XDContainer;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.code.DefContainer;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/**
 * @author Trojan
 */
public class TestGenJava extends XDTester {
	public TestGenJava() {super();}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static XDContainer x(XDContainer x, String a, String b, String mask) {return new DefContainer();}

	private void test(String packageName, String className, XDPool xp, String xdName, String xml) {
		try {
			// create java source with compied XDPool from xdef
			StringWriter swr = new StringWriter();
			try (PrintWriter pwr = new PrintWriter(swr)) {
				XDFactory.XDPoolClassWriter(pwr, className, packageName, xp);
				pwr.close();
			}
			System.out.println(className + ", len = " + swr.toString().length());
			ByteArrayInputStream bais = new ByteArrayInputStream(swr.toString().getBytes());

			// compile created Java class from source
			File f = clearTempDir();
			copyToTempDir(bais, packageName, className);
			compileSources(f);
			try {
				bais = new ByteArrayInputStream(swr.toString().getBytes());
				f = new File(getSourceDir(), className + ".java");
				System.out.println(f);
//				org.xdef.sys.FUtils.copyToFile(bais, f, false);
			} catch (Exception ex) {fail(ex);}

			// get XDPool from the created class.
			Class<?> c = ClassLoader.getSystemClassLoader().loadClass(packageName + "." + className);
			Method m = c.getMethod("getXDPool");
			m.setAccessible(true);
			XDPool xp1 = (XDPool) m.invoke(null);

			// run XDPool xith given XML
			ArrayReporter reporter = new ArrayReporter();
			assertEq(xml, parse(xp1, xdName, xml, reporter));
			assertNoErrorsAndClear(reporter);
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
			| InvocationTargetException ex) {fail(ex);}
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef, xml;
		try {
			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"A\"><A/></xd:def>";
			xml = "<A/>";
			test("bugreports", "TestGenJava0", compile(xdef), "", xml);

			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"Vehicle\">\n" +
"   <Vehicle>\n" +
"     <Part xd:script=\"+;\" a=\"? int(1,2)\" />\n" +
"   </Vehicle>\n" +
"</xd:def>";
			xml = "<Vehicle><Part a='1'/><Part a='2'/><Part/></Vehicle>";
			test("bugreports", "TestGenJava1", compile(xdef), "", xml);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"   external method XDContainer bugreports.TestGenJava.x(XDContainer x, String a, String b, String mask);\n"+
"   type xx xdatetime('yyyy-MM-dd');\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <B xd:script=\"occurs *; create x(from('//B'),'x','y','yyyy-MM-dd');\"\n"+
"    a='string' b='string' x=\"? xx\" y=\"? xx\"/>\n"+
"</A>\n"+
"</xd:def>";
			xml = "<A><B a='a' b='b' x='2023-12-08' y='2023-12-31'/><B a='a' b='b'/></A>";
			test("bugreports", "TestGenJava2", compile(xdef), "", xml);

			xdef =
"<xd:collection xmlns:xd=\"http://www.syntea.cz/xdef/3.1\">\n" +
"  <xd:def name=\"A\" root=\"A\" >\n" +
"    <A>\n" +
"       <B xd:script=\"ref B#B; occurs +\"/>\n" +
"    </A>\n" +
"  </xd:def>\n" +
"  <xd:def name=\"B\" root=\"B\">\n" +
"    <xd:declaration>type xxx xdatetime('dd.MM.yyyy','yyyy-MM-yy');</xd:declaration>\n" +
"    <B b='? xxx'/>\n" +
"  </xd:def>\n" +
"</xd:collection>";
			xml = "<B/>";
			XDPool xp = compile(xdef);
			assertEq(xml, parse(xp, "B", xml));
			test("bugreports", "TestGenJava3", compile(xdef), "B", xml);
			xml = "<A><B/></A>";
			assertEq(xml, parse(xp, "A", xml));
			test("bugreports", "TestGenJava3", compile(xdef), "A", xml);

			xdef =
"<xd:collection xmlns:xd='http://www.xdef.org/xdef/4.2'>\n" +
"<xd:def name='A' root='A|B#B'>\n" +
"<A a='string()'>\n" +
"  <B xd:script='*; ref B'/>\n" +
"</A>\n" +
"<B b='? string();'/>\n" +
"</xd:def>\n" +
"<xd:def name='B'>\n" +
"<B a='string();'>\n" +
"  <C xd:script='*; ref C'/>\n" +
"</B>\n" +
"<C b='? string();'/>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xml = "<A a='a'><B b='b'/></A>";
			test("bugreports", "TestGenJava4", compile(xdef), "A", xml);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
