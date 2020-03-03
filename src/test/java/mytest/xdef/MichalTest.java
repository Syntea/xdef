/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mytest.xdef;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xdef.XDBuilder;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;

/**
 *
 * @author Vaclav Trojan
 */
public class MichalTest {

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

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String XDEF=
"<xd:def xmlns:xd=\"http://www.syntea.cz/xdef/3.1\"\n" +
"        xd:name=\"Pokus\" xd:root=\"Pokus\">\n" +
"    <Pokus><a/></Pokus>\n" +
"</xd:def>";

        XDBuilder xdBuilder = XDFactory.getXDBuilder(null);
        xdBuilder.setSource(XDEF);
        XDPool xdPool = xdBuilder.compileXD();
        XDDocument xdDocument=xdPool.createXDDocument("Pokus#Pokus");
		String DATA="<Pokus><a/></Pokus>";
		MyInputStream in = 
			new MyInputStream(new ByteArrayInputStream(DATA.getBytes()));
        xdDocument.xparse(in, null);  // pokud se tento  radek zaremuje nedojde z zaseknuti
		System.out.println("closed = " + in._closed);
	}
}
