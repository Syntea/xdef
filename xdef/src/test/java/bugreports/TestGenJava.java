package bugreports;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import org.xdef.XDContainer;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.impl.code.DefContainer;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test creation of Java source from XDPool.
 * @author Trojan
 */
public class TestGenJava extends XDTester {
	public TestGenJava() {super();}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static XDContainer x(XDContainer x, String a, String b, String mask) {return new DefContainer();}

	private void test(String pckgName, String clsName, XDPool xp, String xdName, String xml) throws Exception{
		File f = clearTempDir();

		// create java source with compied XDPool from xdef
		f = new File(f, pckgName);
		f.mkdir();
		f = new File(f, clsName + ".java");
		Writer swr = new FileWriter(f);
		XDFactory.writeXDPoolClass(swr, clsName, pckgName, xp);
		swr.close();

		// compile created Java source
		compileSources(f);

/*********************************************************************************************************
		// copy Java source file to source directory.
		File f1 = new File(getSourceDir(), clsName + ".java");
		if (!f1.exists()) {
			System.out.println(f1);
			f1.delete();
			org.xdef.sys.FUtils.copyToFile(f, f1);
		}
 /**********************************************************************************************************/

		// get XDPool from the created class and run xparse of XML.
		test1(pckgName, clsName, xdName, xml);
	}

	private void test1(String pckgName, String clsName, String xdName, String xml) throws Exception {
		// get XDPool from the created class.
		Class<?> c = ClassLoader.getSystemClassLoader().loadClass(pckgName + "." + clsName);
		Method m = c.getMethod("getXDPool");
		m.setAccessible(true);
		XDPool xp = (XDPool) m.invoke(null);
		// run XDPool xith given XML
		ArrayReporter reporter = new ArrayReporter();
		assertEq(xml, parse(xp, xdName, xml, reporter));
		assertNoErrorsAndClear(reporter);
	}

	@Override
	/** Run test and display error information. */
	public void test() {
		String xdef, xml;
		try {
			xdef = "<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"A\"><A/></xd:def>";
			xml = "<A/>";
			test("bugreports", "TestGenJava_1", compile(xdef), "", xml);

			xdef =
"<xd:def xmlns:xd=\"http://www.xdef.org/xdef/4.0\" root=\"A\">\n" +
"   <A> <Part xd:script=\"+;\" a=\"? int(1,2)\" /> </A>\n" +
"</xd:def>";
			xml = "<A><Part a='1'/><Part a='2'/><Part/></A>";
			test("bugreports", "TestGenJava_2", compile(xdef), "", xml);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root = 'A'>\n"+
"<xd:declaration>\n"+
"   external method XDContainer bugreports.TestGenJava.x(XDContainer x, String a, String b, String mask);\n"+
"   type xx xdatetime('yyyy-MM-dd');\n"+
"</xd:declaration>\n"+
"<A>\n"+
"  <B xd:script=\"occurs *; create x(from('//B'),'x','y','yyyy-MM-dd');\"\n"+
"    a='? string' b='? string' x=\"? xx\" y=\"? xx\"/>\n"+
"</A>\n"+
"</xd:def>";
			xml = "<A><B a='a' b='b' x='2023-12-08' y='2023-12-31'/><B/></A>";
			test("bugreports", "TestGenJava_3", compile(xdef), "", xml);

			xdef =
"<xd:collection xmlns:xd=\"http://www.syntea.cz/xdef/3.1\">\n" +
"  <xd:def name=\"A\" root=\"A\" >\n" +
"    <A> <B xd:script=\"ref B#B; occurs +\"/> </A>\n" +
"  </xd:def>\n" +
"  <xd:def name=\"B\" root=\"B\">\n" +
"    <xd:declaration>type xxx xdatetime('dd.MM.yyyy','d.M.yyyy');</xd:declaration>\n" +
"    <B b='? xxx'/>\n" +
"  </xd:def>\n" +
"</xd:collection>";
			xml = "<B/>";
			test("bugreports", "TestGenJava_4", compile(xdef), "B", xml);
			xml = "<A><B b='18.12.2023'/></A>";
			test1("bugreports", "TestGenJava_4", "A", xml);

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
			test("bugreports", "TestGenJava_5", compile(xdef), "A", xml);
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
