
import java.io.IOException;

/** Prepare X-components for task6.
 * @author Vaclav Trojan
 */
public class RunAll_prepare {

	/** Run all tests.
	 * @param args ignored
	 */
	public static void main(String... args) {
		try {
			task6.GenComponents1.main(args);
			task6.GenComponents2.main(args);
			System.out.println("X-components for task6 created.");
		} catch (IOException ex) {
			System.err.println("Can't create X-components for task6: " + ex);
			throw new RuntimeException(ex);
		}
	}
}