package org.xdef.sys;

import java.io.IOException;

/** This interface is used to reading from streams, readers and parsers.
 * @author Vaclav Trojan
 */
public interface SReader {

	/** Read.byte or character from the source stream.
	 * @return integer value of byte or character or return -1 if end of stream
	 * is reached.
	 * @throws IOException if an error occurs.
	 */
	public int read() throws IOException;
}