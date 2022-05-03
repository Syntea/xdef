package buildtools;

import java.io.File;

/** Call preprocessor and reset all switches.
 * @author Vaclav Trojan
 */
public class ResetPreprocessorSwitches {

	/** Reset all switches in source code.
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		String projectBase;
		try {
			File baseDir = args == null || args.length == 0
				? new File("../xdef") : new File(args[0]);
			if (!baseDir.exists() || !baseDir.isDirectory()) {
				throw new RuntimeException("Base is not directory.");
			}
			projectBase = baseDir.getCanonicalPath().replace('\\', '/');
		} catch (Exception ex) {
			throw new RuntimeException("Can't find project base directory");
		}
		JavaPreprocessor.main(
			"-i", new File(projectBase, "src/main/java").getAbsolutePath(),
//			"-o", new File("temp").getAbsolutePath(),// output directery
			"-r", // recursive process of directories
			"-l", // generate CR and LF as the end of line
			"-v", // verbose output
			"-t"); // remove trailibg white sapces
	}
}
