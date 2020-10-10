package test.common.bnf;

import org.xdef.sys.BNFGrammar;
import org.xdef.sys.StringParser;
import org.xdef.sys.STester;
import java.io.File;

/** Test of SQL syntax.
 * @author Vaclav Trojan
 */
public class TestSQL extends STester {

	public TestSQL() {super();}


	private String parse(BNFGrammar grammar, String name, String source) {
		try {
			StringParser p = new StringParser(source);
			grammar.setUserObject(this);
			if (grammar.parse(p, name)) {
				if (grammar.getParser().errorWarnings()) {
					return grammar.getParser().getReportWriter().
						getReportReader().printToString();
				}
				return grammar.getParsedString();
			} else {
				return name + " failed, " + (p.eos()?
					"eos" : p.getPosition().toString()) + "; ";
			}
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "Exception " + ex;
		}
	}

////////////////////////////////////////////////////////////////////////////////
	@Override
	public void test() {
		try {
			BNFGrammar g = BNFGrammar.compile(
				null, new File(getDataDir() + "TestSQL.bnf"), null);
			String s =
"/* Comment */ select * /* Comment*/ from /* Comment*/ xxx;\n"+
"select XX from xxx;\n"+
"select a, b as \"xx yy\" from xxx;\n"+
"select 'a' as \"xx yy\" from xxx;\n"+
"select 'a', count(1) from xxx;\n"+
"SELECT /*+ SKIP_SC */ ALL AVG(c_n) FROM t_n;\n"+
"SELECT * FROM TEST;\n"+
"SELECT a.* FROM TEST;\n"+
"SELECT DISTINCT NAME FROM TEST;\n"+
"SELECT ID, COUNT(1) FROM TEST GROUP BY ID;\n"+
"SELECT NAME, SUM(VAL) FROM TEST GROUP BY NAME HAVING COUNT(1) > 2;\n"+
"SELECT 'ID', COL, MAX('ID') AS MAX FROM TEST;\n"+
"SELECT * FROM TEST LIMIT 1000;\n"+
"UPSERT INTO TEST VALUES('foo','bar',3);\n"+
"UPSERT INTO TEST(NAME, ID) VALUES('foo',123);\n"+
"SELECT col3, col4 FROM test WHERE col5 < 100;\n"+
"UPSERT INTO foo SELECT * FROM bar;\n" +
"UPSERT INTO table.targetTable(col1, col2) SELECT col3, col4 FROM table.sourceTable WHERE col5 < 100;\n"+
"DELETE FROM TEST;\n" +
"DELETE FROM TEST WHERE ID=123;\n" +
"DELETE FROM TEST WHERE NAME LIKE 'foo%';\n"+
"CREATE TABLE my_schema.my_table ( id BIGINT PRIMARY KEY);\n"+
"CREATE TABLE my_table ( id INTEGER not null primary key desc, date DATE not null,\n" +
"    m.db_utilization DECIMAL, i.db_utilization)\n" +
"    m.DATA_BLOCK_ENCODING='DIFF';\n"+
"CONSTRAINT my_pk PRIMARY KEY (host);\n" +
"CONSTRAINT my_pk PRIMARY KEY (host ASC,created_date DESC);"+
"DROP TABLE my_schema.my_table;\n" +
"DROP VIEW my_view;\n"+
"ALTER TABLE my_schema.my_table ADD d.dept_id char(10) VERSIONS=10;\n" +
"ALTER TABLE my_table ADD dept_name char(50);\n" +
"ALTER TABLE my_table ADD parent_id char(15) null primary key;\n" +
"ALTER TABLE my_table DROP COLUMN d.dept_id;\n" +
"ALTER TABLE my_table DROP COLUMN dept_name;\n" +
"ALTER TABLE my_table DROP COLUMN parent_id;\n" +
"ALTER TABLE my_table SET IMMUTABLE_ROWS=true;\n" +
"CREATE INDEX my_idx ON sales.opportunity(last_updated_date DESC);\n" +
"CREATE INDEX my_idx ON log.event(created_date DESC) INCLUDE (name, payload) SALT_BUCKETS=10;\n" +
"CREATE INDEX IF NOT EXISTS my_comp_idx ON server_metrics ( gc_time DESC, created_date DESC );\n" +
"DROP INDEX my_idx ON sales.opportunity;\n"+
"DROP INDEX IF EXISTS my_idx ON server_metrics;\n"+
"ALTER INDEX my_idx ON sales.opportunity DISABLE;\n" +
"ALTER INDEX IF EXISTS my_idx ON server_metrics REBUILD;\n"+
"EXPLAIN SELECT NAME, COUNT(*) FROM TEST GROUP BY NAME HAVING COUNT(*) > 2;\n" +
"EXPLAIN SELECT entity_id FROM CORE.CUSTOM_ENTITY_DATA\n"+
"   WHERE organization_id='00D300000000XHP' AND SUBSTR(entity_id,1,3) = '002'\n"+
"      AND created_date < CURRENT_DATE()-1\n;"+
"--This is line comment\n"+
"//This is line comment\n"+
"/* Comment */";
			assertEq(s, parse(g, "Commands", s));
		} catch (Exception ex) {
			fail(ex);
		}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}