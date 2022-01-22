package buildtools;

import java.io.File;

/** Call preprocessor and reset all options.
 * @author Vaclav Trojan
 */
public class ResetAllPreprocessorOptions {

	/** Reset all switches in source code.
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		JavaPreprocessor.main(
			"-i", new File("src/main/java").getAbsolutePath(), //input directory
//			"-o", new File("temp").getAbsolutePath(),// output directery
			"-r", // recursive process of directories
			"-v", // verbose output
			"-t"); // remove trailibg white sapces
	}
}
