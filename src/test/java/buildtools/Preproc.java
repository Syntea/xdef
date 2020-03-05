package buildtools;

import org.xdef.sys.RegisterReportTables;
import java.io.File;

/** Provides preprocessing of sources according to given switches in
 * the "src" directory and in the "test" directory.
 * @author  Vaclav Trojan
 */
public class Preproc {

	private Preproc() {}

	/** Return the directory of project.
	 * @param clazz the class from the project.
	 * @return path to the directory.
	 */
	public static String getProjectHomeDir(Class clazz) {
		String home;
		File f;
		try {
			f = new File(".");
			home = f.getCanonicalPath();
		} catch (Exception ex) {
			throw new RuntimeException("Can't find home dir");
		}
		if (!(f = new File(home, "src")).exists() || !f.isDirectory()) {
			throw new RuntimeException("Can't find home dir");
		}
		return home.replace('\\', '/');
	}

	/** Return the directory of source files of the project.
	 * @param clazz the class from the project.
	 * @return path to the directory.
	 */
	public static String getSourceHomeDir(Class clazz) {
		String className = clazz.getName().replace('.', '/');
		String home = new File(ClassLoader.getSystemClassLoader()
			.getResource(className + ".class").getFile()).getAbsolutePath()
			.replace(File.separatorChar, '/');
		//application
		int i = home.indexOf("build/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "src/";
		}
		//web application
		i = home.indexOf("build/web/WEB-INF/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "src/java/";
		}
		//running in test mode.
		i = home.indexOf("build/test/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "src/";
		}
		String sourceName = className + ".java";
		File f = new File("src/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length());
		}
		f = new File("src/java/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length());
		}
		f = new File("src/test/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length());
		}
		throw new RuntimeException("Can't find source home dir");
	}

	/** Return the directory of test files of the project.
	 * @param clazz the class from the project.
	 * @return path to the directory.
	 */
	public static String getTestHomeDir(Class clazz) {
		String className = clazz.getName().replace('.', '/');
		String home = new File(ClassLoader.getSystemClassLoader()
			.getResource(className + ".class").getFile()).getAbsolutePath()
			.replace(File.separatorChar, '/');
		//application
		int i = home.indexOf("build/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "test/";
		}
		//web application
		i = home.indexOf("build/web/WEB-INF/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "test/";
		}
		//running in test mode.
		i = home.indexOf("build/test/classes/" + className);
		if (i > 0) {
			return home.substring(0, i) + "test/";
		}
		String sourceName = className + ".java";
		File f = new File("src/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length() - 4) +
				"test/";
		}
		f = new File("src/java/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length() - 9) +
				"test/";
		}
		f = new File("src/test/" + sourceName);
		if (f.exists()) {
			home = f.getAbsolutePath().replace('\\', '/');
			return home.substring(0, home.length() - sourceName.length() - 9) +
				 "test/";
		}
		throw new RuntimeException("Can't find source home dir");
	}

	/** Modify preprocessor lines in specified files.
	 * @param clazz the class from the project.
	 * @param args parameters containing information from command line from
	 * JavaPreprocessor:
	 * <p>[-h] [-r] [-t] [-v] -i input [-o output] [-c charset] [-s switches]</p>
	 * <p>where</p>
	 * <p> -r recurse input directory. The parameter is optional.</p>
	 * <p> -t delete trailing spaces. The parameter is optional.</p>
	 * <p> -v make verbose output. The parameter is optional.</p>
	 * <p> -s switches: The list of switch names. Each switch name is composed from
	 * letters, digits, '.' or '_'s. The switch can be either prefixed with
	 * '!' or not specified. The parameter is optional.</p>
	 * <p> -i input: The file name list of the directories with the Java packages.
	 * Each directory is supposed to be the root of package. Only the files with
	 * the extension '.java' are processed. The parameter is obligatory.</p>
	 * <p> -o output: The directory where the output files are stored. The
	 * parameter is optional. If it is missing the source files are
	 * replaced.</p>
	 * <p> -c charset: name of character table, if it is not specified then the
	 * default system character set is used. The parameter is optional.</p>
	 */
	public static void modify(Class clazz, String[] args) {
		args[1] = getSourceHomeDir(clazz);
		System.out.println("Directory: " + args[1]);

		// generate message classes
		// Generate Java source report files
		String s = Preproc.getProjectHomeDir(Canonize.class) +
			"src/main/java/org/xdef/msg/";
		boolean genReportTable = false;
		for (File f: new File(s).listFiles()) {
			String name = f.getName();
			int ndx;
			if ((ndx = name.indexOf(".properties")) > 0) {
				File f1 = new File(s, name.substring(0, ndx) + ".java");
				if (!f1.exists() || f.lastModified() > f1.lastModified()) {
					genReportTable = true;
					break;
				}
			}
		}
		if (genReportTable) {
			RegisterReportTables.main(new String[] {
				"-i", s + "*.properties",
				"-c", GenConstants.JAVA_SOURCE_CHARSET,
				"-o", s,
				"-r"});
			System.out.println("Report classes generated to " + s);
		}

		String errMsg = JavaPreprocessor.proc(args, System.out, System.err);
		if (errMsg != null) {
			System.err.println(errMsg);
		}
		args[1] = getTestHomeDir(clazz);
		if (!new File(args[1]).exists()) {
			return;
		}
		System.out.println("Directory: " + args[1]);
		errMsg = JavaPreprocessor.proc(args, System.out, System.err);
		if (errMsg != null) {
			System.err.println(errMsg);
		}
	}
}