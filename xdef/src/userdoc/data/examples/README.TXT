The X-definition source and the XML data files are available in the directory
"src". To run an example set the classpath parameter as follows:
      ..\xdef-*.jar;lib/derby-*.jar
(the "derby-*.jar" is required to run the database examples)

You can run any example from the cmd file (Windows):
    > runExample.cmd name
or bash file (Unix):
    > runExample.sh name
where the parameter "name" is  one of:
  Example1
  Example2
  Example3
  Example4
  Example5

Examples with XQuery:
  ExampleXQuery

Examples with JSON:
  ExampleJSON1
  ExampleJSON2
  ExampleJSON3

Example with Lexicon:
  ExampleLexicon

The exammples with X-components you should run in following sequence:
1.  ExampleXC1 (this example generates X-components used in ExampleXC2)
2.  ExampleXC2

The examples with database you should run in following sequence:
1. ExampleDBCreate  (prepare and create database, must be invoked first)
2. ExampleDBInsert  (inserts few items)
3. ExampleDBInsert1 (inserts the one new item and one existing item)
4. ExampleDBRead    (displays created XML from database)
5. ExampleDBRead1   (variant of step 4)
6. ExampleDBDrop    (drop database tables and schema, invoke it last)

The examples with X-components are: Example_XC1 and Example_XC2.
Note you must run Example_XC1 first. The X-components are generated
to the directory src/components.

Other examples is possible to run from directories task1, task2 ... e.g.:
Windows: runExample.cmd task1/Order1
Unix: runExample.sh task1/Order1

The documentation with X-components is available in
   "xdef-*_X-component.pdf".

In the directories "task1" .. "task7" there are data files of tasks from
   "xdef-*_Programming.pdf".
In the directories "src/task1" .. "src/task7" there are Java source codes
 of tasks from this book.

Try it and enjoy it!
