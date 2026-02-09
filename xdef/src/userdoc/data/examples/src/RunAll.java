import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Run all examples.
 * @author Vaclav Trojan
 */
public class RunAll {

	/** Run an example from the class.
	 * @param clazz the class with an example.
	 */
	private static void runExample(final Class<?> clazz) {
		System.out.flush();
		System.err.flush();
		try {
			Method method = clazz.getMethod("main", String[].class);
			System.out.println("***** " + clazz.getCanonicalName() + " *****");
			method.invoke((Class<?>) null, (Object) new String[0]);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException 
			| RuntimeException ex) {
			throw new RuntimeException("Error in " + clazz.getName(), ex);
		}
		System.out.flush();
		System.err.flush();
	}

	/** Run all tests.
	 * @param args ignored
	 */
	public static void main(String... args) {
		data.MyClass.class.getClass(); // force to translate
		task1.Order3ext.class.getClass(); //force translation
		task2.Orders3ext.class.getClass(); //force translation
		task4.Orders2ext.class.getClass(); //force translation
		
		runExample(Example1.class);
		runExample(Example1_errors.class);
		runExample(Example1_listing.class);
		runExample(Example2.class);
		runExample(Example2_errors.class);
		runExample(Example3.class);
		runExample(Example3a.class);
		runExample(Example4.class);
		runExample(Example4a.class);
		runExample(Example5.class);
		runExample(ExampleDBCreate.class);
		runExample(ExampleDBInsert.class);
		runExample(ExampleDBInsert1.class);
		runExample(ExampleDBRead.class);
		runExample(ExampleDBRead1.class);
		runExample(ExampleDBDrop.class);
		runExample(ExampleJSON1.class);
		runExample(ExampleJSON1.class);
		runExample(ExampleXQuery.class);
//////////////// task1 ////////////////
		runExample(task1.Order1.class);
		runExample(task1.Order1a_gen.class);
		runExample(task1.Order1a.class);
		runExample(task1.Order1b_gen.class);
		runExample(task1.Order1b.class);
		runExample(task1.Order2.class);
		runExample(task1.Order2a.class);
		runExample(task1.Order3.class);
//////////////// task2 ////////////////
		runExample(task2.Orders1.class);
		runExample(task2.Orders2.class);
		runExample(task2.Orders3.class);
//////////////// task3 ////////////////
		runExample(task3.Order1.class);
		runExample(task3.Order2.class);
		runExample(task3.Order2a.class);
//////////////// task4 ////////////////
		runExample(task4.Orders1.class);
		runExample(task4.Orders2.class);
//////////////// task5 ////////////////
		runExample(task5.JsonExample.class);
		runExample(task5.Network.class);
		runExample(task5.XonExample.class);
		runExample(task5.YamlExample.class);
		runExample(task5.IniExample.class);
		runExample(task5.PropsExample.class);
		runExample(task5.CsvExample.class);
//////////////// task6 ////////////////
		runExample(task6.GenComponents1.class);
		runExample(task6.Town1.class);
		runExample(task6.GenComponents2.class);
		runExample(task6.Town2.class);
		runExample(task6.BindWith.class);
//////////////// task7 ////////////////
		runExample(task7.Town.class);
//////////////// task8 ////////////////
		runExample(task8.GenXdefFromXML.class);
		runExample(task8.GenXdefFromJSON.class);
		runExample(task8.GenXdefFromYAML.class);
//////////////// task9 ////////////////
		runExample(task9.GenSchemaFromXdef.class);
//////////////// task10 ////////////////
		runExample(task10.GenXdefFromSchema.class);
	}
}