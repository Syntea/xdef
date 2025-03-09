package mytests;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Method;
import test.XDTester;

public class ClassPathSetter extends XDTester {
	private static final Class[] URLPARAMS = new Class[]{URL.class};

	public static void addFile(String s) throws IOException {
		addFile(new File(s));
	}

	public static void addFile(File f) throws IOException {
		addURL(f.toURI().toURL());
	}

	@SuppressWarnings("unchecked")
	public static void addURL(URL u) throws IOException {
		URLClassLoader sysloader =
			(URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", URLPARAMS);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[]{u});
		} catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException
			| InvocationTargetException t) {
			t.printStackTrace();
			throw new IOException("Could not add URL to system classloader");
		}
	}

	@Override
	/** Run test and display error information. */
	public void test() {}
	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		URLClassLoader sysloader =
			(URLClassLoader) ClassLoader.getSystemClassLoader();
		for (URL x: sysloader.getURLs()) {
			System.out.println(x.toExternalForm());
		}
		for (URL x: sysloader.getURLs()) {
			System.out.println(x.toExternalForm());
		}
//		System.out.println("===");
//		for (URL x: ((URLClassLoader)sysloader.getParent()).getURLs()) {
//			System.out.println(x.toExternalForm());
//		}
	}
}