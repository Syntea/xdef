The X-definition source and the XML data files are available in the directory
"src". To run an example set the classpath parameter as follows:
      ..\xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar;derby/derby.jar
(the "derby.jar" is required to run the database examples)

You can run any example from the bat file  (Windows)
      runExample.bat name
or bash file (Unix)
      runExample.sh name
where the parameter "name" is  one of
  Example1
  Example2
  Example3
  Example4
  Example5

The exammples with X-components you should run in following sequence:

1.  Example_XC1 (this example generates X-components used in Example_XC2)
2.  Example_XC2

The examples with database you should run in following sequence:

1. Example6_DBCreate  (prepare and create database, must be invoked first)
2. Example6_DBInsert  (inserts few items)
3. Example6_DBInsert1 (inserts the one new item and one existing item)
4. Example6_DBRead    (displays created XML from database)
5. Example6_DBRead1   (variant of step 4)
6. Example6_DBDrop    (drop database tables and schema, invoke it last)

The examples with X-components are: Example_XC1 and Example_XC2.
Note you must run Example_XC1 first. The X-components are generated
to the directory src/components.

The documentation with X-components is available in
   "xdef-32_X-component.pdf".

In the directories "task1" .. "task3" there are data files of tasks from
   "xdef-32_Programming.pdf".
In the directories "src/task1" .. "src/task3" there are Java source codes
 of tasks from this book.

Try it and enjoy it!
