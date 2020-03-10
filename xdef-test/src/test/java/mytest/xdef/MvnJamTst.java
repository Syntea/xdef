package mytest.xdef;

import org.junit.Test;
//import org.testng.annotations.Test;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/**
 * Testovani zda se zasekne 'mvn test' po skonceni java testu.
 * Pro simulaci spustte skript:<p>
 * <code>
 * mvn test -Dtest=MvnJamTst
 * </code>
 *</p>
 * @author hejny
 * @since 2020-03-2020
 */
public class MvnJamTst {

	String XDEF=
"<xd:def xmlns:xd=\"http://www.syntea.cz/xdef/3.1\"\n" +
"        xd:name=\"Pokus\" xd:root=\"Pokus\">\n" +
"    <Pokus><a/></Pokus>\n" +
"</xd:def>";

	String DATA="<Pokus><a/></Pokus>";

	@Test
	public void testPokus()  {
		XDBuilder xdBuilder = XDFactory.getXDBuilder(null);
		xdBuilder.setSource(XDEF);
		XDPool xdPool = xdBuilder.compileXD();
		xdBuilder = null;
		XDDocument xdDocument=xdPool.createXDDocument("Pokus#Pokus");
		xdPool = null;
		xdDocument.xparse(DATA, null);  // pokud se tento  radek zaremuje nedojde z zaseknuti
		xdDocument = null;
		System.out.println("Hotovo");
		ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
		ThreadGroup parentGroup;
		while ((parentGroup = rootGroup.getParent()) != null) {
			rootGroup = parentGroup;
		}
		Thread[] threads = new Thread[rootGroup.activeCount()];
		while (rootGroup.enumerate(threads, true ) == threads.length) {
			threads = new Thread[threads.length * 2];
		}
		for (Thread x: threads) {
			if (x != null) {
				Thread.State state = x.getState();
				System.out.print(x.getName() + "; " + state);
				if (state == state.WAITING || state == state.TIMED_WAITING) {
					x.interrupt();
					System.out.print(" ... interrupted");
				}
				System.out.println();
			}
		}
	}
}