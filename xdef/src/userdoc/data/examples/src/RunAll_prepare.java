import java.io.IOException;
import org.xdef.sys.FUtils;

/** Prepare X-components for task6.
 * @author Vaclav Trojan
 */
public class RunAll_prepare {

    /** Run all tests.
     * @param args not used.
     * @throws IOException if an error occurs.
     */
    public static void main(String... args) throws IOException{
        for (int i = 1; i <= 10; i++) {
            String s = "task" + i + "/";
            FUtils.deleteAndCreateDir(s + "output");
            FUtils.deleteAndCreateDir(s + "errors");
        }
        task6.GenComponents1.main(args);
        task6.GenComponents2.main(args);
        System.out.println("X-components for task6 created.");   
    }
}