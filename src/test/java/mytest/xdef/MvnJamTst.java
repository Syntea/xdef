package mytest.xdef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.junit.Test;

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
	
	static class MyInputStream extends InputStream {
		final InputStream _in;
		boolean _closed = false;
		MyInputStream(InputStream in) {
			_in = in;
		}
		@Override
		public int read() throws IOException {
			return _in.read();
		}
		@Override
		public void close() throws IOException {
			_closed = true;
			_in.close();
		}
	}

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
		MichalTest.MyInputStream in = 
			new MichalTest.MyInputStream(new ByteArrayInputStream(DATA.getBytes()));
        xdDocument.xparse(in, null);  // pokud se tento  radek zaremuje nedojde z zaseknuti
		System.out.println("closed = " + in._closed);
		xdDocument = null;
		System.out.println("Hotovo");
    }
}