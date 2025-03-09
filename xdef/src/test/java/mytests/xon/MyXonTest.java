package mytests.xon;

import org.xdef.sys.SDatetime;
import org.xdef.sys.STester;
import org.xdef.xon.XonUtils;

public class MyXonTest extends STester {

	public MyXonTest() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		XONArray a = new XONArray();
		a.add("true");
		a.add(true);
		XONMap m = new XONMap();
		m.put("a", 1);
		m.put("b", (long) 0);
		m.put("c", null);
		a.add(m);
		a.add((double) 0);
		a.add(new SDatetime("0001"));
		assertTrue(a.getType()==XONObject.XON_ARRAY);
		assertTrue(!a.isEmpty());
		assertTrue(a.size()==5);
		assertTrue(XonUtils.xonEqual(XonUtils.parseXON(a.toString()), a));
		assertTrue(XonUtils.parseXON(a.toString()).equals(a));
		assertEq(XonUtils.parseXON(a.toString()), a);
		assertTrue(m.getType()==XONObject.XON_MAP);
		assertTrue(!m.isEmpty());
		assertTrue(m.size()==3);
		assertTrue(XonUtils.parseXON(m.toString()).equals(m));
		assertTrue(XonUtils.xonEqual(XonUtils.parseXON(m.toString()), m));
		assertTrue(XonUtils.xonEqual(a.get(2), m));
		assertTrue(a.get(2).equals(m));
		assertEq(a.get(2), m);
		assertEq(a.get(4), new SDatetime("0001"));
	}

	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}