package test;

import cz.syntea.xdef.sys.RegisterReportTables;
import java.io.File;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;

/** Run all available TestAll */
public class TestAll {

    /** run TestAll in test.common */
    @Test
    public static void testCommon() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        
        a.assertEquals(test.common. TestAll. runTests(args0), 0);
    }
    
    /** run TestAll in test.msgx */
    //@Test
    public static void testMsgX() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        
        a.assertEquals(test.msgx.   TestMsgX.runTest (args0), 0);
    }
    
    /** run TestAll in test.xdef */
    @Test
    public static void testXdef() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        
        a.assertEquals(test.xdef.   TestAll. runTests(args0), 0);
    }
    
    /** run TestAll in test.xdutil */
    @Test
    public static void testXDUtils() {
        final Assertion  a     = new Assertion();
        final String[]   args0 = {};
        
        a.assertEquals(test.xdutils.TestAll. runTests(args0), 0);
    }
    
    
    
    /** spusti zdejsi testy pres TestNG */
    public static void mainTestNG() {
        TestNG              testNG = new TestNG();
        TestListenerAdapter tla    = new TestListenerAdapter();
        
        testNG.setTestClasses(new Class<?>[] {TestAll.class});
        testNG.setOutputDirectory("run/output/test-ng");
        testNG.addListener(tla);
        testNG.run();
        
        for (ITestResult result : tla.getFailedTests()) {
            String    id = result.getTestClass().getName() + "." + result.getName();
            Throwable ex = result.getThrowable();
            
            if (ex != null) {
                System.err.println(id + ":");
                ex.printStackTrace();
            } else {
                System.err.println(id + ": failure bez vyjimky!");
            }
        }
    }
    
    
    
    /** spusti zdejsi testy primo */
    public static void mainTest() {
        try { testCommon();  } catch (Exception e) { System.err.println(e.getMessage()); }
        //try { testMsgX();    } catch (Exception e) { System.err.println(e.getMessage()); }
        try { testXdef();    } catch (Exception e) { System.err.println(e.getMessage()); }
        try { testXDUtils(); } catch (Exception e) { System.err.println(e.getMessage()); }
    }
    
    
    
    /**
     * spust mainTest()
     * @param args
     */
    public static void main(String... args) {
		/*
		String s = 
			new File("src/main/java/cz/syntea/xdef/msg/").getAbsolutePath();
		s = s.replace('\\', '/');
		if (!s.endsWith("/")) {
			s += '/';
		}
		System.out.println(s);		
		RegisterReportTables.main(new String[] {
			"-i", s + "*.properties",
			"-c", "windows-1250",
			"-p", "cz.syntea.xdef.msg",
			"-o", s,
			"-r"});
		System.out.println("Report classes generated to " + s);
		*/
        //mainTest();
        mainTestNG();
    }
    
}
