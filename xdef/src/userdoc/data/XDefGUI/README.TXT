In the directory "projects" are subdirectories with projects, where are
data necessary to run a project. Each project is in a subdirectory and
there is the file "project.xml" which contains properties necessary to
run a project. (The structure of the file "project.xml" is described
with the Xdefinition below):

Projects in stored the directory "projects":

construct    compile X-definition and run construction mode of XML data
extMethod    example with external method
json         compile X-definition and run validation mode with JSON data
test         example more executios of Xdefinition, setting external
             valiable
validate     compile X-definition and run validation mode with XML data

You can run the project by executing the following command "runProject"
(the file "runProject.cmd" for Windows and "runProject.sh" for UNIX)
with the parameter containing the name of project subdirectory
mentioned above.

=======================================================================
X-definition describing project.xml data:
=======================================================================
<xd:def xmlns:xd="http://www.xdef.org/xdef/4.1"
  root="Project"
  name="GUI">
  <Project
    Show="? enum('true','false'); /* if true the file with project is
                                     displayed and editable. */">

    <xd:mixed>
      <!-- "XDefinition" - items with the sources of X-definition -->
      <XDefinition xd:script="+">
        string(); /* a file, url or XML with X-definition source */
      </XDefinition>

      <!-- "External" - items to be added to the classpath -->
      <External xd:script="*">
        file() || url(); /* classpath item (url or pathname) */
      </External>

      <!-- "Property" items are used to set properties for compiling
        and executing the project -->
      <Property xd:script="*"
        Name="string();  /*property name*/"
        Value="string(); /*property value*/" />

      <!-- "Execute" items are used to specify the compiled XDPool
        is executed according to the specified parameters -->
      <Execute xd:script="*;"
        XDName="? string(1, 1000); /* root X-definition name (may be
                                      missing) */"
        DataType="? enum('XML', 'JSON', 'INI'); /* type of processed
                                                   data */"
        Mode="? enum('construct', 'validate'); /* mode of process */"
        DisplayResult="? enum('true', 'false'); /* if true the result
                                           of process is displayed */" >

        <xd:mixed>
          <!-- "Var" items are used to set variables to the X-definition
            processor -->
          <Var xd:script="*" Name="string(); /*name of variable*/">
            string(); /*value of variable*/
          </Var>

          <!-- "Context" item is used to specify a context in the construction
            mode -->
          <Context xd:script="?"
            Edit="? enum('true', 'false'); /* if true the context
                                        can be edited */" >
            string(); /* the file or XML data used as a context */
          </Context>

          <!-- "Input" item is used to specify the input data in the
            validation mode -->
          <Input xd:script="?"
            Edit="? enum('true', 'false'); /* true if input data can
                                                be edited */">
            string(); /* file with data */
          </Input>

          <!-- "SaveResult" item is used to specify if the result is saved
            to the specified file -->
          <SaveResult xd:script="?"
            Indent="? enum('true', 'false'); /* if true the result is
                                          indented */"
            Encoding="? string(); /* name of char encoding */"
            File="string(); /* where to store result of process */" />
        </xd:mixed>
      </Execute>
      <!-- \"WorkDir\" item is used to specify the directory where are
        stored the temporary files. If it is not specified a working
        directory is created and deleted on exit of GUI editor. -->
      <WorkDir xd:script=\"?;\">
        file(); /* Directory where to store temporary data. */
      </WorkDir>
    </xd:mixed>
  </Project>
</xd:def>
=======================================================================

You can also run the main method of the class "org.xdef.util.GUIEditor"
with following parameters:

 -p project_file
   | -v [switches]
   | -c [switches]
   | -g [xml source] [switches]

 -p run a project file
 -v compile X-definition and runs validation mode
 -c compile X-definition and runs construction mode
 -g generate X-definition and project from input data (see switch -data)

Switches:
 -xdef source with X-definition (input file or data; it may be
  specified more times)
 -external ist of classpath items with external resources (may be
  filenames or urls).
 -format specification of data format (XML or JSON; default XML)
 -data source (input file or data used for validation mode and as
  the context for construction mode or for generation of X-definition).
 -debug sets debugging mode when project is executed
 -editInput enables to edit input data before execution
 -displayResult displays result data
 -workDir directory where to store created data. This switch is
  optional. If not specified the work directory is created and
  deleted on exit.
