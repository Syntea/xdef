The X-definition source and the XML data files are available in the directory
"src". To run an example set the classpath parameter as follows:
      ./lib/syntea_xdef3.1.jar;derby/derby.jar
(the "derby.jar" is required to run the database examples)

You can run any example by the command
      runExample.bat name
where the parameter "name" is the name of an example (i.e. the name of
the example Java source file without the extension ".java"). E.g.:
      runExample.bat Example1

The database examples you should compile and run in following sequence:

1. Example6_DBCreate  (prepares database and creates schema)
2. Example6_DBInsert  (inserts few items)
3. Example6_DBInsert1 (inserts one new item and tries to insert existing item)
4. Example6_DBRead    (displays created XML from database)
5. Example6_DBRead1   (variant of step 4)
6. Example6_DBDrop    (drops tables and schema from the database)

The examples with X-components are: Example_XC1 and Example_XC2.
Note you must run Example_XC1 first. The X-components are generated
to the directory src/components. The documentation is in
   "../doc/xdef3.1_X-component_eng.pdf".

In the directories "task1" .. "task3" are data files of tasks from the book
   "../doc/xdef3.1_Programming_eng.pdf".
In the directories "src/task1" .. "src/task3" are Java source codes of tasks
from this book.

Try it and enjoy it!
