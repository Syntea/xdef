package cz.syntea.xdef.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Library for IO operations.
 * </p>
 * <p>
 * Holds:
 * <ul>
 * <li>if the method works with a stream and reads/writes the stream entire
 * then it tries to close it
 * </li>
 * </ul>
 * </p>
 *
 * @author sisma
 */
public class IOUtil {
	
	/**
	 * copies the input to the output
	 * 
	 * @param srcStream  input stream
	 * @param destStream output stream
	 * @throws IOException if error occurs
	 */
	public static void copyFile(
		InputStream  srcStream,
		OutputStream destStream
	) throws IOException {

		byte[] buf = new byte[fileBufferSize];
		int len;
		
		try {
			while ((len = srcStream.read(buf)) > 0) {
				destStream.write(buf, 0, len);
			}
		} finally {
			closeSure(srcStream, destStream);
		}
	}
	
	/** see {@link #copyFile(InputStream, OutputStream)} */
	public static void copyFile(
		Reader srcStream,
		Writer destStream
	) throws IOException {
		char[] buf = new char[fileBufferSize];
		int len;
		
		try {
			while ((len = srcStream.read(buf)) > 0) {
				destStream.write(buf, 0, len);
			}
		} finally {
			closeSure(srcStream, destStream);
		}
	}
	
	/**
	 * @param file
	 * @return string with the content of the file
	 * @throws IOException if error occurs
	 */
	public static String copyFile(File file) throws IOException {
		return copyFile(new FileInputStream(file));
	}
	
	/**
	 * @param is stream
	 * @return string with the content of the stream
	 * @throws IOException if error occurs
	 */
	public static String copyFile(InputStream is) throws IOException {
		StringWriter writer = new StringWriter();
		
		copyFile(new InputStreamReader(is, charset), writer);
		
		return writer.toString();
	}
	
	/**
	 * it tries close all not-null objects in the list, although some closures
	 * fail
	 * 
	 * @param closableArray list of the closeable objects
	 * @throws IOException if some closures fail then reports
	 *                     each failed closure 
	 */
	public static void closeSure(Closeable... closableArray)
		throws IOException {
		List<String> msgList = new ArrayList<String>();
		int parametr = 0;
		
		for (Closeable closable : closableArray) {
			++parametr;
			
			try {
				if (closable != null) {
					closable.close();
				}
			} catch (IOException ex) {
				msgList.add("#parameter=" + parametr + ": " + ex.getMessage());
			}
		}
		
		if (!msgList.isEmpty()) {
			throw new IOException(msgList.toString());
		}
	}
	
	
	
	/** default charset */
	public static final Charset	charset        = Charset.forName("UTF-8");
	/** default size of the bufferu for manipulation with files */
	private static final int    fileBufferSize = 32768;
	
}
