import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

/**
 *
 * @author Vaclav Trojan
 *
 * @version 1.0.0
 */
public class UpdatePPT {
		
	UpdatePPT() {}

	static String getWebDir() {
		String className = UpdatePPT.class.getName();
		URL url = ClassLoader.getSystemClassLoader()
			.getResource(className.replace('.', '/') + ".class");
		String s = new File(url.getFile()).getAbsolutePath().replace('\\', '/');
		String cname = className.replace('.', '/');
		int i = s.indexOf("/build/web/WEB-INF/classes/" + cname);
		if (i >= 0) {
			s = s.substring(0, i + 1);
			return s + "web/";
		}
		throw new RuntimeException("Unknown web directory");
	}

	/** Read input stream to StringBuffer (decoded from given charset table).
	 * @param in The input stream.
	 * @param encoding The name of encoding table. If this argument is
	 * <tt>null</tt> the default encoding is aplayed.
	 * @return The StringBuffer.
	 * @throws Exception if an error occurs.
	 */
	static StringBuffer readToStringBuffer(final File in,
		final String encoding) throws UnsupportedEncodingException, IOException {
		InputStreamReader is;
		if (encoding == null) {
			is = new InputStreamReader(new FileInputStream(in));
		} else {
			is = new InputStreamReader(new FileInputStream(in), encoding);
		}
		StringBuffer sb = new StringBuffer();
		int len;
		char[] buf = new char[4096];
		while ((len = is.read(buf)) >= 0) {
			if (len == 0) {
				Thread.currentThread().yield();
			} else {
				sb.append(buf, 0 , len);
			}
		}
		return sb;
	}
	
	/** Write StringBuffer to file.
	 * @param file The file.
	 * @param buf The StringBuffer.
	 * @param encoding The name of encoding table. If this argument is
	 * <tt>null</tt> the default encoding is applayed.
	 * @throws Exception if an error occurs
	 */
	public static void writeStringBuffer(final File file,
		final StringBuffer buf,
		final String encoding)
		throws UnsupportedEncodingException, IOException {
		OutputStreamWriter os;
		if (encoding == null) {
			os = new OutputStreamWriter(new FileOutputStream(file));
		} else {
			os = new OutputStreamWriter(
				new FileOutputStream(file), encoding);
		}
		os.write(buf.toString());
		os.close();
	}
	
	/** Extract number ID from file name. The name has format "*" + n + ".html".
	 * Returns -1 if format is not correct. */
	static int extractId(String name) {
		if (!name.endsWith(".html")) {
			return - 1;
		}
		int ndx = name.lastIndexOf('.');
		if (ndx <= 0) {
			return -1;
		}
		for (int i = ndx - 1; i >= 0; i--) {
			if (!Character.isDigit(name.charAt(i))) {
				if (i == ndx - 1) {
					return -1;
				}
				return Integer.parseInt(name.substring(i + 1, ndx));
			}
		}
		return -1;
	}
	
	/** Process all htmls, remove text items and set links to examples */
	public static void proc(String webdir) {
		String dir = webdir.replace('\\', '/');
		if (!dir.endsWith("/")) {
			dir += "/";
		}
		if (!new File(dir + "img1.html").exists()) {
			System.err.println("Incorrect directory");
			return;
		}
		String encoding = "utf-8";
		File[] files = new File(dir + "examples").listFiles();
		Properties examples = new Properties();
		for (int i = 0; files != null && i < files.length; i++) {
			String s = files[i].getName();
			String x = "Example";
			if (!s.startsWith(x)) {
				continue;
			}
			int ii = extractId(s);
			if (ii >= 0) {
				examples.setProperty(String.valueOf(ii), s);
			}
		}
		files = new File(dir).listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			String s = files[i].getName();
			if (s.startsWith("text") && 
				(s.endsWith(".html") || s.endsWith(".gif"))) {
				files[i].delete();
				continue;
			}
			if (!s.endsWith(".html")) {
				continue;
			}
			try {
				StringBuffer sb = readToStringBuffer(files[i], encoding);
				int ndx1 = sb.indexOf("<a href=\"text");
				int ndx2 = sb.indexOf("</a>", ndx1 + 15);
				if (ndx1 > 0 && ndx2 > 0) {
					sb.delete(ndx1, ndx2 + 4);
				} else {
					continue;
				}
				int ii = extractId(s);
				if (ii >= 0 && s.startsWith("img")) {
					String example = examples.getProperty(String.valueOf(ii));
					if (example != null) {
						sb.insert(ndx1, "<a href=\"examples/" + example +
							"\" target=\"_blank\">" +
							"<img src=\"examples/try.gif\"" +
							" border=0 alt=\"Try example\"></a>");
						System.out.println("To slide " +
							files[i] + " added reference to " + example);
					}
				}
				writeStringBuffer(files[i], sb, encoding);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length != 1 || args[0].startsWith("${")) {
			System.exit(0);
//			proc(getWebDir() + "bpel");
//			proc(getWebDir() + "xdef");
//			proc(getWebDir() + "xdef_ces");
		} else {
			proc(args[0]);
		}
		
	}
	
}
/* Created at 1. prosinec 2006, 18:29 by Vaclav Trojan .
 * $State$
 * $Log$
 */
