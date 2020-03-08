import java.io.File;
import java.sql.Connection;
import org.apache.derby.jdbc.EmbeddedDataSource;

/** Prepare Derby database (auxiliary class). */
public class GenDerby {
	static final String DB_URL;
	private static final String DB_USER = "myself";
	private static final String DB_PASSWORD = "blabla";
	static {
		File f = new File("./temp");
		f.mkdirs();
		f = new File(f, "temp/");
		String derby = f.getAbsolutePath().replace('\\', '/');
		DB_URL = "jdbc:derby:" + derby;
	}

	/** Delete all files and subdirectories.
	 * @param files files and directories to be cleared.
	 */
	private static void clearDirectory(final File[] files) {
		if (files == null) {
			return;
		}
		for (File f: files) {
			if (f.exists()) {
				if (f.isDirectory()) {
					clearDirectory(f.listFiles());
				}
				f.delete();
			}
		}
	}

	/** Prepare derby database */
	public static void prepare() {
		File f = new File("./temp");
		f.mkdirs();
		f = new File(f, "temp/");
		clearDirectory(f.listFiles());
		f.delete();

		EmbeddedDataSource ds = new EmbeddedDataSource();
		// path to store data for database schema "temp"
		ds.setDatabaseName(f.getAbsolutePath().replace('\\', '/'));
		// for security reason restrict access to the test database for
		// the specific user identified by the password
		ds.setUser(DB_USER);
		ds.setPassword(DB_PASSWORD);
		// create a new database (or use the existing one)
		ds.setCreateDatabase("create");
		Connection con;
		try {
			con = ds.getConnection();
			con.close();
			System.out.println("Derby database prepared");
		} catch (Exception ex) {
			throw new RuntimeException("Can't create database connection", ex);
		}
	}

}