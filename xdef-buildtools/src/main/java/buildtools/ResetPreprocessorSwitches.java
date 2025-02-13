package buildtools;

import java.io.File;
import java.io.IOException;

/** Call preprocessor and reset all switches and update source code.
 * @author Vaclav Trojan
 */
public class ResetPreprocessorSwitches {

	/** Reset all switches in the preprocessor of source code.
	 * @param args path to base directory or null.
	 */
	public static void main(String... args) {
		File baseDir = args == null || args.length == 0 ? new File("../xdef") : new File(args[0]);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			throw new RuntimeException("Incorect project base directory");
		}
		String projectBase;
		try {
			projectBase = baseDir.getCanonicalPath().replace('\\', '/');
		} catch (IOException ex) {
			throw new RuntimeException("Incorect project base directory");
		}
		JavaPreprocessor.main(
			"-i", new File(projectBase, "src/main/java").getAbsolutePath(),
			"-r", // recursive process of directories
			"-l", // generate CR and LF as the end of line
			"-v", // verbose output
			"-t"); // remove trailibg white sapces
		JavaPreprocessor.main(
			"-i", new File(projectBase, "src/test/java").getAbsolutePath(),
			"-r", // recursive process of directories
			"-l", // generate CR and LF as the end of line
			"-v", // verbose output
			"-t"); // remove trailibg white sapces
	}
}