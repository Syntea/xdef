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
		System.out.println("Reset preprocessor switches...");
		JavaPreprocessor.main(
			"-i", new File("src/main/java").getAbsolutePath(), //input directory
//			"-o", new File("temp").getAbsolutePath(),// output directery
			"-r", // recursive process of directories
			"-v", // verbose output
			"-t"); // remove trailibg white sapces
	}
}
