package test.xdef;

import builtools.XDTester;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import java.util.ArrayList;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.xdef.proc.XXElement;

/** Test of using external methods (such as query) in create mode.
 * @author Vaclav Trojan
 */
public final class TestUserQuery extends XDTester {

	public TestUserQuery() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		XDPool xp;
		XDDocument xd;
		String xdef;
		Element el;
		String s;
		//Here the user object is assigned at root element (by the external
		// userQuery method)
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root xd:script=\"create userQuery('some query expression')\">\n"+
"  <firma xd:script='occurs *; create getNext()'\n"+
"     name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'\n"+
"       firstName = \"required string; create getValue('firstName')\"\n"+
"       lastName = \"required string; create getValue('lastName')\"/>\n"+
"  </firma>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			el = create(xp, "Example", (Element) null, "root");
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee lastName=\"Young\" firstName=\"Michael\"/>" +
						"<employee lastName=\"Brown\" firstName=\"John\"/>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee lastName=\"Todt\" firstName=\"Ulrich\"/>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}

		//Here the user object is passed to create process from outside by the
		// parameter new UserTable().
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root>\n"+
"  <firma xd:script='occurs *; create getNext()'\n"+
"     name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'\n"+
"       firstName = \"required string; create getValue('firstName')\"\n"+
"       lastName = \"required string; create getValue('lastName')\"/>\n"+
"  </firma>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("Example");
			xd.setUserObject(new UserTable());
			el = xd.xcreate("root", null);
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee lastName=\"Young\" firstName=\"Michael\"/>" +
						"<employee lastName=\"Brown\" firstName=\"John\"/>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee lastName=\"Todt\" firstName=\"Ulrich\"/>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}
		//Here the user object is assigned at root element and create method
		// is called in element
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root xd:script=\"create userQuery('some query expression')\">\n"+
"  <firma xd:script='occurs *; create getNext()'\n"+
"     name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'\n"+
"       firstName = \"required string; create getValue('firstName')\"\n"+
"       lastName = \"required string; create getValue('lastName')\"/>\n"+
"  </firma>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("Example");
			xd.setUserObject(new UserTable());
			el = xd.xcreate("root", null);
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee lastName=\"Young\" firstName=\"Michael\"/>" +
						"<employee lastName=\"Brown\" firstName=\"John\"/>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee lastName=\"Todt\" firstName=\"Ulrich\"/>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}
		//Here the user object is assigned at root element and create method
		// is called from sequence item
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root xd:script=\"create userQuery('some query expression')\">\n"+
" <xd:sequence xd:script='occurs *; create getNext()'>\n"+
"  <firma\n"+
"     name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'\n"+
"       firstName = \"required string; create getValue('firstName')\"\n"+
"       lastName = \"required string; create getValue('lastName')\"/>\n"+
"  </firma>\n"+
" </xd:sequence>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("Example");
			xd.setUserObject(new UserTable());
			el = xd.xcreate("root", null);
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee lastName=\"Young\" firstName=\"Michael\"/>" +
						"<employee lastName=\"Brown\" firstName=\"John\"/>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee lastName=\"Todt\" firstName=\"Ulrich\"/>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}
		//Here the user object passed as parameter and items are generated
		// inside of sequence block. User method is called inside of sequence.
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root>\n"+
" <xd:sequence>\n"+
"  <firma xd:script='occurs *;create getNext()'\n"+
"    name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'\n"+
"      firstName = \"required string; create getValue('firstName')\"\n"+
"      lastName = \"required string; create getValue('lastName')\"/>\n"+
"  </firma>\n"+
" </xd:sequence>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("Example");
			xd.setUserObject(new UserTable());
			el = xd.xcreate("root", null);
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee lastName=\"Young\" firstName=\"Michael\"/>" +
						"<employee lastName=\"Brown\" firstName=\"John\"/>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee lastName=\"Todt\" firstName=\"Ulrich\"/>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='Example' root='root'>\n"+
"<root xd:script=\"create userQuery('some query expression')\">\n"+
"  <firma xd:script='occurs *; create getNext()'\n"+
"     name = \"required string; create getValue('name')\">\n"+
"    <employee xd:script='occurs *; create getNext()'>\n"+
"       <personaldata>\n"+
"       <firstName>\n"+
"         required string; create getValue('firstName')\n"+
"       </firstName>\n"+
"       <lastName>\n"+
"         required string; create getValue('lastName')\n"+
"       </lastName>\n"+
"       </personaldata>\n"+
"    </employee>\n"+
"  </firma>\n"+
"</root>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("Example");
			xd.setUserObject(new UserTable());
			el = xd.xcreate("root", null);
			assertEq(el,
				"<root>" +
					"<firma name=\"Dreams Ltd\">" +
						"<employee>" +
							"<personaldata>" +
								"<firstName>Michael</firstName>" +
								"<lastName>Young</lastName>" +
							"</personaldata>" +
						"</employee>" +
						"<employee>" +
							"<personaldata>" +
								"<firstName>John</firstName>" +
								"<lastName>Brown</lastName>" +
							"</personaldata>" +
						"</employee>" +
					"</firma>" +
					"<firma name=\"Synthetic food\">" +
						"<employee>" +
							"<personaldata>" +
								"<firstName>Ulrich</firstName>" +
								"<lastName>Todt</lastName>" +
							"</personaldata>" +
						"</employee>" +
					"</firma>" +
				"</root>");
		} catch (Exception ex) {fail(ex);}
		try {//create books from simulated select (with element qualifier "*")
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='test'>\n"+
"  <test>\n"+
"    <book xd:script=\"occurs *; create select('here is probably a query')\""+
"          cover=\"optional string(); create getColumn('cover')\">"+
"       <title>required string(); create getColumn('title')</title>\n"+
"    </book>\n"+
"  </test>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			xd = xp.createXDDocument("test");
			s = KXmlUtils.nodeToString(xd.xcreate("test", null));
			assertEq("<test>" +
				"<book cover=\"paperback\"><title>Svejk</title></book>" +
				"<book cover=\"gold\"><title>Klaus</title></book>" +
				"</test>", s);
		} catch (Exception ex) {fail(ex);}
		try {// the same with sequence (qualifier "*")
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='test'>\n"+
"  <test>\n"+
"    <xd:sequence script = \"*; create select('??')\">\n"+
"      <book cover=\"optional string(); create getColumn('cover')\">"+
"        <title>required string(); create getColumn('title')</title>\n"+
"      </book>\n"+
"    </xd:sequence>\n"+
"  </test>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			Object userObj = new SelectResult();
			xd = xp.createXDDocument("test");
			xd.setUserObject(userObj);
			el = xd.xcreate("test", null);
			assertEq("<test>" +
				"<book cover=\"paperback\"><title>Svejk</title></book>" +
				"<book cover=\"gold\"><title>Klaus</title></book>" +
				"</test>", KXmlUtils.nodeToString(el));

		} catch (Exception ex) {fail(ex);}
		try {//this is the example of an external user object passed to creator
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='test' root='test'>\n"+
"  <test>\n"+
"    <book xd:script=\"occurs *; create select('here is probably a query')\""+
"          cover=\"optional string(); create getColumn('cover')\">"+
"       <title>required string(); create getColumn('title')</title>\n"+
"    </book>\n"+
"  </test>\n"+
"</xd:def>";
			xp = compile(xdef, getClass());
			Object userObj = new SelectResult();
			xd = xp.createXDDocument("test");
			xd.setUserObject(userObj);
			el = xd.xcreate("test", null);
			assertEq("<test>" +
				"<book cover=\"paperback\"><title>Svejk</title></book>" +
				"<book cover=\"gold\"><title>Klaus</title></book>" +
				"</test>", KXmlUtils.nodeToString(el));
		} catch (Exception ex) {fail(ex);}

		resetTester();
	}

	//Note query is here ignored (simulated). It is just created an
	// context looking as a result of query
	public static boolean select(XXElement chkEl, final String query) {
		Object obj;
		if ((obj = chkEl.getXDDocument().getUserObject()) == null) {
			SelectResult sr = new SelectResult();
			chkEl.getXDDocument().setUserObject(sr);
			return sr.hasNext();
		} else {
			return ((SelectResult) obj).hasNext();
		}
	}

	//This simulates how to get a column from a line of table.
	public static String getColumn(XXElement chkEl, String columnName) {
		Object obj = chkEl.getXDDocument().getUserObject();
		if (obj instanceof SelectResult) {
			return ((SelectResult) obj).getColumn(columnName);
		}
		return ""; //Item not available
	}

	// This class simulates an object retrieved from a database select.
	private static class SelectResult {

		int _index; //index of iterator

		ArrayList<HashMap<String, String>> _lines;

		SelectResult() {_index = -1;
			_lines = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> ht = new HashMap<String, String>();
			ht.put("cover", "paperback");
			ht.put("title", "Svejk");
			_lines.add(ht);
			ht = new HashMap<String, String>();
			ht.put("cover", "gold");
			ht.put("title", "Klaus");
			_lines.add(ht);
		}

		// This metnod returnes true if next line is available and prepares
		// the line.
		public boolean hasNext() {return ++_index < _lines.size();}

		// return value of the column.
		public String getColumn(String key) {
			if (_index < _lines.size()) { //has next item?
				String result;
				if ((result = _lines.get(_index).get(key)) != null) {
					return result;
				}
			}
			return "";
		}
	}

////////////////////////////////////////////////////////////////////////////////
// External methods called from XDefinition
////////////////////////////////////////////////////////////////////////////////

	/** This method prepares the user object and assigns it to the engine.
	 * @param chkEl the actually processed chkElement object.
	 * @param query string with query expression (ignored in this example).
	 * @return true if query result is available (always true here).
	 */
	public static boolean userQuery(XXElement chkEl, String query) {
		chkEl.setUserObject(new UserTable()); //set user query object
		return true;
	}

	/** Take next item from the parent user context and sets the current
	 * raw of the table (as a current user object).
	 * @param chkEl the actualy processed chkElement object.
	 * @return if child context exists it returns true and sets the value
	 * of the child context to the chkElement object. If no child is
	 * available it returns false.
	 */
	public static boolean getNext(XXElement chkEl) {
		Object obj;
		if ((obj = chkEl.getUserObject()) != null &&
			(obj instanceof UserQuery)) {
			return ((UserQuery) obj).next(chkEl);
		}
		return false;
	}

	public static String getValue(XXElement chkEl, String name) {
	/** Get value of column from actual raw.
	 * @param chkEl the actually processed chkElement object.
	 * @param name of required column.
	 * @return value of column item or an empty string.
	 */
		Object obj;
		if ((obj = chkEl.getUserObject()) != null &&
			(obj instanceof UserQuery)) {
			return ((UserQuery) obj).value(chkEl, name);
		}
		return "";
	}

////////////////////////////////////////////////////////////////////////////////
// Object simulating a query (where the user context interface is implemented).
////////////////////////////////////////////////////////////////////////////////

	/** This is the interface implemented for each "user" query object. */
	private interface UserQuery {
		public boolean next(XXElement chkEl);
		public UserQuery newContext(XXElement chkEl);
		public String value(XXElement chkEl, String name);
	}

	/** This is the interface implemented for each "user" query object. */
	private class EmptyContext implements UserQuery {
		EmptyContext() {}
		@Override
		public boolean next(XXElement chkEl) {return false;}
		@Override
		public UserQuery newContext(XXElement chkEl) {return this;}
		@Override
		public String value(XXElement chkEl, String name) {return "";}
	}

	/** Implementation of an object a table, contains raws and each raw
	 * (describing a company) contains a table (of employees).
	 * For each item it is implemented the UserQuery interface. */
	private static class UserTable implements UserQuery {
		ArrayList<Firma> _firmas = new ArrayList<Firma>();
		int _index = 0; //the iterator index.
		/** This simulates a query. */
		UserTable() {
			Firma firma = new Firma("Dreams Ltd", 1);
			firma.addEmployee("Michael", "Young");
			firma.addEmployee("John", "Brown");
			_firmas.add(firma);
			firma = new Firma("Synthetic food", 2);
			firma.addEmployee("Ulrich", "Todt");
			_firmas.add(firma);
		}

		@Override
		/** Implementation of the interface UserQuery on the table.
		 * @param chkEl the actually processed chkElement object.
		 * @return if child context exists it returns true and sets the value
		 * of the child context to the chkElement object. If no child is
		 * available it returns false.
		 */
		public boolean next(XXElement chkEl) {
			if (_index < _firmas.size()) {
				chkEl.setUserObject(_firmas.get(_index++));
				return true;
			}
			return false;
		}

		@Override
		/** Implementation of the interface UserQuery. Since the table itself
		 * has no values it does nothing here and it returns the empty string.
		 * @param chkEl the actually processed chkElement object.
		 * @param name of required column.
		 * @return since no columns are here it returns an empty string.
		 */
		public String value(XXElement chkEl, String name) {return "";}

		@Override
		public UserQuery newContext(XXElement chkEl) {
			return new UserQuery() {
				@Override
				public boolean next(XXElement chkEl) {return false;}
				@Override
				public UserQuery newContext(XXElement chkEl) {return this;}
				@Override
				public String value(XXElement chkEl, String name) {return "";}
			};
		}

		/** Object describing "Firma". */
		private class Firma implements UserQuery {
			String _name;
			int _id;
			private int _index = 0;
			ArrayList<Employee> _persons = new ArrayList<Employee>();
			Firma(String name, int id) {_name = name; _id = id;}

			void addEmployee(String firstName, String lastName) {
				_persons.add(new Employee(firstName, lastName));
			}

			@Override
			/** Implementation of the interface UserQuery on the table. The
			 * method takes the nexr raw from the table and sets it as the
			 * context for further processing.
			 * @param chkEl the actualy processed chkElement object.
			 * @return if child context exists it returns true and sets
			 * the value of the child context to the chkElement object.
			 * If no child is available it returns false.
			 */
			public boolean next(XXElement chkEl) {
				if (_index < _persons.size()) {
					chkEl.setUserObject(_persons.get(_index++));
					return true;
				}
				return false;
			}

			@Override
			/** Implementation of the interface UserQuery. Returns here th name
			 * or the id of Firma.
			 * @param chkEl the actualy processed chkElement object.
			 * @param name of required column.
			 * @return value of column item or the empty string.
			 */
			public String value(XXElement chkEl, String name) {
				if ("name".equals(name)) {
					return _name;
				} else if ("id".equals(name)) {
					return String.valueOf(_id);
				}
				return "";
			}

			@Override
			public UserQuery newContext(XXElement chkEl) {
				return new UserQuery() {
					@Override
					public boolean next(XXElement chkEl) {return false;}
					@Override
					public UserQuery newContext(XXElement chkEl) {return this;}
					@Override
					public String value(XXElement chkEl, String name)
						{return "";}
				};
			}

			/** Object describing "Employee". */
			private class Employee implements UserQuery {
				HashMap<String,String> _columns = new HashMap<String,String>();
				Employee(String firstName, String lastName) {
					_columns.put("firstName", firstName);
					_columns.put("lastName", lastName);
				}

				@Override
				/** Implementation of the interface UserQuery on the table.
				 * Since Employe is a raw of the table it has no raws and so
				 * it does nothig and returns always false.
				 * @param chkEl the actualy processed chkElement object.
				 * @return always false - nas no children.
				 */
				public boolean next(XXElement chkEl) {
					return false; //has no children
				}

				@Override
				/** Implementation of the interface UserQuery. Returns the value
				 * of specified column of the actual raw of the table. If
				 * required value doesn't exist it returns the empty string.
				 * @param chkEl the actualy processed chkElement object.
				 * @param name of required column.
				 * @return value of column item or the empty string.
				 */
				public String value(XXElement chkEl, String name) {
					if (_columns.containsKey(name)) {
						return _columns.get(name);
					}
					return "";
				}
				@Override
				public UserQuery newContext(XXElement chkEl) {
					return new UserQuery() {
						@Override
						public boolean next(XXElement chkEl) {return false;}
						@Override
						public UserQuery newContext(XXElement chkEl)
							{return this;}
						@Override
						public String value(XXElement chkEl, String name)
							{return "";}
					};
				}
			}// end of class Employee
		}// end of class Firma
	}// end of class UserTable

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		XDTester.setFulltestMode(true);
		if (runTest() != 0) {System.exit(1);}
	}
}