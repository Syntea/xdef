package test.xdef;

import java.util.Map;
import org.xdef.XDDocument;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.component.XComponentUtil;
import org.xdef.xon.XonUtils;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SRuntimeException;
import static org.xdef.sys.STester.runTest;
import test.XDTester;
import static test.XDTester._xdNS;

/** Test XON/JSON/INI/Properties/CSV data. */
public class TestYaml extends XDTester {

    public TestYaml() {super();}

    /** Run all tests. */
    @Override
    public void test() {
        if (!_xdNS.startsWith("http://www.xdef.org/xdef/4.")) {
            return; // do not test versions 2.0, 2.1, and 3.1
        }
        String json, yaml;
        Object o, x;
        XDPool xp;
        XDDocument xd;
        ArrayReporter reporter = new ArrayReporter();
        XComponent xc;
        try {
            xp = compile(
"<xd:def xmlns:xd='http://www.xdef.org/xdef/4.2' xmlns:jx='http://www.xdef.org/json/nodes/4.2' root='a' name='a'>\n" +
"  <xd:json name = 'a'> {  \"a\" : { \"b\": { \"%script\" : \" ref a#SysValue\" } } } </xd:json>\n" +
"  <xd:json name='SysValue'> { \"c\": \" string();\", \"Desc\": \"? string();\" } </xd:json>\n" +
"  <xd:component>%class "+_package+".mates.XCa1 %link a#a;</xd:component>\n" +
"</xd:def>");
            genXComponent(xp);
            json = "{ \"a\": { \"b\": {\"c\": \"\\\\xxx\\\"\"} } }";
            o = xp.createXDDocument("a").jparse(json, reporter);
            assertNoErrorsAndClear(reporter);
            xc = xp.createXDDocument("a").jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq("\\xxx\"",
                XComponentUtil.getx(XComponentUtil.getx(XComponentUtil.getx(xc, "get$a"), "get$b"), "get$c"));
            x = XonUtils.xonToJson(xc.toXon());
            assertTrue(XonUtils.xonEqual(o, x), XonUtils.xonDiff(o, x));
            yaml ="a:\n  b: {c: \\xxx\"}";
            xc = xp.createXDDocument("a").yparseXComponent(yaml, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq("\\xxx\"",
                XComponentUtil.getx(XComponentUtil.getx(XComponentUtil.getx(xc, "get$a"), "get$b"), "get$c"));
            assertTrue(XonUtils.xonEqual(o, x), XonUtils.xonDiff(o, xc.toXon()));
        } catch (RuntimeException ex) {fail(ex);}
        try {
            xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='test' name='A'>\n" +
"  <xd:json name=\"test\">\n" +
"    { \"cities\": [\n" +
"        \"date();\",\n" +
"        { \"%script\": \"occurs 1..*;\",\n" +
"          \"from\": [\n" +
"            \"string();\",\n" +
"            { \"%script\": \"occurs 1..*; \", \"to\": \"jstring();\", \"distance\": \"int();\"\n" +
"            }\n" +
"    	  ]\n" +
"        }\n" +
"      ]\n" +
"    }" +
"  </xd:json>\n" +
"</xd:def>").createXDDocument("A");
            yaml =
"cities:\n" +
"- '2020-02-22'\n" +
"- from:\n" +
"  - Brussels\n" +
"  - {to: London, distance: 322}\n" +
"  - {to: Paris, distance: 265}\n" +
"- from:\n" +
"  - London\n" +
"  - {to: Brussels, distance: 322}\n" +
"  - {to: Paris, distance: 344}\n";
            x = xd.yparse(yaml, reporter);
            assertNoErrorwarningsAndClear(reporter);
            assertTrue(XonUtils.xonEqual(x, xd.yparse(XonUtils.toYamlString(x), reporter)));
            assertNoErrorwarningsAndClear(reporter);
        } catch (RuntimeException ex) {
            if (ex instanceof SRuntimeException && "JSON101".equals(((SRuntimeException) ex).getMsgID())) {
                setResultInfo("YAML tests skipped; org.yaml.snakeyaml is not available");
            } else {
                fail(ex);
            }
        }
        try {
            xp = compile(
"<xd:collection xmlns:xd=\"http://www.xdef.org/xdef/4.2\">\n" +
"    <xd:def impl-version ='2026/06.01' impl-date='2026-06-01' root='SynPLscript' name='SynPLscript'>\n" +
"        <xd:declaration scope = 'global'>\n" +
"            type  functionName    jstring(1,30);\n" +
"            type  code            jstring(1,9999);\n" +
"            type  params          jstring(1,9999);\n" +
"            type  async           enum('Y','N');\n" +
"            type  changeLog       enum('Y','N');\n" +
"            type  sla             enum('Y','N');\n" +
"            type  isAllowed       enum('Y','N');\n" +
"            type  desc            jstring(1,1000);\n" +
"            type  eventName       jstring(1,30);\n" +
"            type  functionDesc    jstring(1,9999);\n" +
"            type  scriptVersion   xdatetime('yyyy-MM-dd');\n" +
"            type  interval        regex('^[0-9]{1,3}[S|M|H|D]$');\n" +
"            type  statusName      jstring(1,100);\n" +
"            type  sysValue        jstring(1,100);\n" +
"            type  userRole        jstring(1,30);\n" +
"            type  valueType       enum('STRING', 'INTEGER', 'DECIMAL', 'DATE', 'DATETIME', 'INTERVAL');\n" +
"            type  variableName    jstring(1,30);\n" +
"            type  variableRef     jstring(1,255);\n" +
"            type  wfScriptName    jstring(1,30);\n" +
"            type  subjectRoleCode enum('IN','SD','SC');\n" +
"            uniqueSet variableSet    { variableName: variableName() };\n" +
"            uniqueSet paramSet       { paramName:  variableName() };\n" +
"            uniqueSet statusSet      { statusName: statusName()   };\n" +
"            uniqueSet eventStatusSet { statusName: statusName(); eventName: eventName()};\n"+
"        </xd:declaration>\n" +
"        <xd:json name = \"SynPLscript\">\n" +
"            {  \"SynPLscript\" : {\n" +
"                    \"Name\":             \"  wfScriptName()\",\n" +
"                    \"SubjectRoleCode\" : \"  subjectRoleCode()\",\n" +
"                    \"Version\":          \"  scriptVersion()\",\n" +
"                    \"Desc\":             \"? desc()\",\n" +
"                    \"Note\":             \"? desc()\",\n" +
"                    \"SysValues\":        [ { \"%script\" :  \"occurs 0..*; ref SysValue#SysValue\"         } ],\n" +
"                    \"Variables\":        [ { \"%script\" :  \"occurs 0..*; ref Variable#Variable\"         } ],\n" +
"                    \"UserRoles\":        [ { \"%script\" :  \"occurs 0..*; ref UserRole#UserRole\"         } ],\n" +
"                    \"UsedFunctions\":    [ { \"%script\" :  \"occurs 0..*; ref UsedFunction#UsedFunction\" } ],\n" +
"                    \"Statuses\":         [ { \"%script\" :  \"occurs 1..*; ref Status#Status\"             } ],\n" +
"                    \"EndStatuses\":      [ { \"%script\" :  \"occurs 1..*; ref EndStatus#EndStatus\"       } ]\n" +
"                }\n" +
"            }\n" +
"        </xd:json>\n" +
"        <xd:component>\n" +
"            %class "+_package+".mates.XCSynPLscript  %link  SynPLscript#SynPLscript;\n" +
//"            %bind SynPLscript %link SynPLscript#SynPLscript/jx:map/jx:map;\n" +
//"            %bind SysValues     %link SynPLscript#SynPLscript/jx:map/jx:map/$mixed/jx:array[1];\n" +
"        </xd:component>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'SysValue'>\n" +
"        <xd:json name='SysValue'>\n" +
"            {   \"SysValue\": \"  variableSet.variableName.ID()\",\n" +
"                \"Type\":     \"  valueType()\",\n" +
"                \"Value\":    \"  sysValue()\",\n" +
"                \"Desc\":     \"? desc()\" }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'Variable'>\n" +
"        <xd:json name='Variable'>\n" +
"            {   \"Variable\": \"  variableSet.variableName.ID()\",\n" +
"                \"Type\":     \"  valueType()\",\n" +
"                \"Ref\":      \"? variableRef()\",\n" +
"                \"Value\":    \"? sysValue()\",\n" +
"                \"Desc\":     \"? desc()\" }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'UserRole'>\n" +
"        <xd:json name='UserRole'>\n" +
"            {  \"UserRole\": \" userRole()\", \"Desc\": \"? desc()\" }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'UsedFunction'>\n" +
"        <xd:json name='UsedFunction'>\n" +
"            {   \"Name\": \"   functionName()\",\n" +
"                \"Desc\": \"?  functionDesc()\",\n" +
"                \"Version\": \"? scriptVersion()\",\n" +
"                \"Params\":  \"? code()\" }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'Status'>\n" +
"        <xd:json name = 'Status'>\n" +
"            {   \"Status\":       \"  statusSet.statusName.ID()\",\n" +
"                \"ActionCode\":   { \"%script\": \"0..1; ref ActionCode#ActionCode;\" },\n" +
"                \"TimeOverStep\": \"? interval()\",\n" +
"                \"IsSlaRunning\":     \"  sla()\",\n" +
"                \"IsSlaEnding\":      \"  sla()\",\n" +
"                \"IsSlaSettlement\":  \"  sla()\",\n" +
"                \"IsSlaWorkStart\":   \"? sla()\",\n" +
"                \"IsSlaFirstAction\": \"? sla()\",\n" +
"                \"ChangeLog\":    \"? changeLog()\",\n" +
"                \"Events\":       [ { \"%script\": \"1..*; ref Event#Event;\" } ]\n" +
"            }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'Event'>\n" +
"        <xd:json name = 'Event'>\n" +
"            {   \"Event\":                 \" eventName()\",\n" +
"                \"UserRoleAny\":           [\"occurs 1..* userRole()\"],\n" +
"                \"ActionCode\":            { \"%script\": \"0..1; ref ActionCode#ActionCode;\" },\n" +
"                \"NextStatus\":            \"? statusName()\",\n" +
"                \"Desc\":                  \"? desc()\",\n" +
"                \"IsAssigneeAllowed\":     \"? isAllowed()\",\n" +
"                \"IsWatcherAllowed\":      \"? isAllowed()\",\n" +
"                \"IsCollaboratorAllowed\": \"? isAllowed()\"\n" +
"            }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'ActionCode'>\n" +
"        <xd:json name = 'ActionCode'>\n" +
"            { \"Async\": \"? async()\", \"Code\": \"? code()\" }\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"    <xd:def name = 'EndStatus'>\n" +
"        <xd:json name = 'EndStatus'>\n" +
"            {   \"EndStatus\":  \" statusSet.statusName.ID()\",\n" +
"                \"ActionCode\": { \"%script\": \"0..1; ref ActionCode#ActionCode;\" },\n" +
"                \"ChangeLog\":  \"? changeLog()\"}\n" +
"        </xd:json>\n" +
"    </xd:def>\n" +
"</xd:collection>");
            genXComponent(xp);
            yaml = // YAML format
"SynPLscript:\n" +
"  Name: SD-Test_Scenario\n" +
"  SubjectRoleCode: SD\n" +
"  Version: \"2026-06-01\"\n" +
"  Desc: WF Script SD Test\n" +
"  SysValues:\n" +
"    - SysValue: SynPL\n" +
"      Type: STRING\n" +
"      Value: SynPL version 0.2\n" +
"  Variables:\n" +
"    - { Variable: TaskID, Type: STRING, Ref: Task@TaskID, Desc: Task ID}\n" +
"    - {Variable: TaskName, Type: STRING, Ref: Task@TaskName, Desc: Task Name}\n" +
"    - {Variable: TaskTypeIN, Type: STRING, Value: testTaskType, Desc: Task Type ID}\n" +
"    - {Variable: ProjectName, Type: STRING, Ref: Project@ProjectName, Desc: Project Name}\n" +
"    - {Variable: SubjectRoleCode, Type: STRING, Value: SD, Desc: \"Entity Role Code (IN, SD, SC)\"}\n" +
"    - {Variable: RoleName_PM, Type: STRING, Value: Projektovy manazer, Desc: Title of the Project Manager Role}\n" +
"    - {Variable: StatusName_Opened, Type: STRING, Value: Opened, Desc: \"Status Name: Opened\"}\n" +
"  UserRoles:\n" +
"    - { UserRole: \"#WF\", Desc: Automatic WF Script }\n" +
"    - { UserRole: PM,      Desc: Provider's Project Manager }\n" +
"    - { UserRole: REP,     Desc: \"The customer who reported the task\" }\n" +
"  UsedFunctions:\n" +
"    - Name: cloneTask\n" +
"      Desc: Cloning a partition from SD to IN\n" +
"      Params: |\n" +
"        newTaskType    - Task type name for an internal clone\n" +
"        copyWfStatus   - Whether to copy the status\n" +
"        copyDoc        - Whether to copy documents\n" +
"    - Name: setDefaultTaskAssignee\n" +
"      Desc: Change the owner of a task to the default user for a given role\n" +
"      Params: |\n" +
"        TaskID          - Task ID\n" +
"        ProjectName     - Project Name\n" +
"        UserRoleName    - User Role Name\n" +
"        subjectRoleCode - Entity Role Code (IN, SD, SC)\n" +
"  Statuses:\n" +
"    # status without ActionCode\n" +
"    - Status: INIT\n" +
"      IsSlaRunning: \"N\"\n" +
"      IsSlaEnding: \"N\"\n" +
"      IsSlaSettlement: \"N\"\n" +
"      Events:\n" +
"        - Event: Open\n" +
"          UserRoleAny: [\"#WF\", PM]\n" +
"          IsAssigneeAllowed: \"Y\"\n" +
"          ActionCode:\n" +
"            Async: \"Y\"\n" +
"            Code: |\n" +
"              def copyWfStatus = false\n" +
"              def copyDoc = true\n" +
"              cloneTask(TaskID, TaskName, TaskTypeIN, copyWfStatus, copyDoc)\n" +
"              if (TaskName == null) {\n" +
"                 setDefaultTaskAssignee(TaskID, ProjectName, RoleName_PM, SubjectRoleCode)\n" +
"              }\n" +
"          NextStatus: In Progress\n" +
"    - Status: In Progress\n" +
"      ActionCode:\n" +
"        Async: \"N\"\n" +
"        Code: |\n" +
"          setDefaultTaskAssignee(TaskID, ProjectName, RoleName_PM, SubjectRoleCode)\n" +
"      IsSlaRunning: \"Y\"\n" +
"      IsSlaEnding: \"N\"\n" +
"      IsSlaSettlement: \"N\"\n" +
"      ChangeLog: \"Y\"\n" +
"      Events:\n" +
"        # event without ActionCode\n" +
"        - Event: Ask customer\n" +
"          UserRoleAny: [\"#WF\", PM]\n" +
"          NextStatus: Solved\n" +
"    - Status: Solved\n" +
"      IsSlaRunning: \"N\"\n" +
"      IsSlaEnding: \"Y\"\n" +
"      IsSlaSettlement: \"Y\"\n" +
"      Events:\n" +
"        - Event: Accept\n" +
"          UserRoleAny: [\"#WF\", REP]\n" +
"          ActionCode:\n" +
"            Async: \"Y\"\n" +
"            Code: |\n" +
"              def note = \"Customer \\\"confirmed\\\" solution\"\n" +
"              println(note)\n" +
"          NextStatus: Closed\n" +
"  EndStatuses:\n" +
"    - EndStatus: Closed\n" +
"      ChangeLog: \"Y\"";
            json = XonUtils.toJsonString(XonUtils.parseYAML(yaml), true); //convert parsed data to JSON format
            //test YAML
            o = xp.createXDDocument("SynPLscript").yparse(yaml, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq(new SDatetime("2026-06-01"),((Map)((Map)o).get("SynPLscript")).get("Version"));
            xc = xp.createXDDocument("SynPLscript").yparseXComponent(yaml, null, reporter);
            assertEq(new SDatetime("2026-06-01"),
                XComponentUtil.getx(XComponentUtil.getx(xc, "get$SynPLscript"), "get$Version"));
            assertTrue(XonUtils.xonEqual(o, x = xc.toXon()), XonUtils.xonDiff(o, x));
            //test JSON
            x = xp.createXDDocument("SynPLscript").jparse(json, reporter);
            assertTrue(XonUtils.xonEqual(o, x), XonUtils.xonDiff(o, x));
            assertNoErrorsAndClear(reporter);
            xc = xp.createXDDocument("SynPLscript").jparseXComponent(json, null, reporter);
            assertNoErrorsAndClear(reporter);
            assertEq(new SDatetime("2026-06-01"),
                XComponentUtil.getx(XComponentUtil.getx(xc, "get$SynPLscript"), "get$Version"));
            assertTrue(XonUtils.xonEqual(o, x = xc.toXon()), XonUtils.xonDiff(o, x));
        } catch (RuntimeException ex) {fail(ex);}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        clearTempDir(); // clear temporary directory
    }

    /** Run test
     * @param args the command line arguments
     */
    public static void main(String... args) {
        XDTester.setFulltestMode(true);
        if (runTest(args) > 0) {System.exit(1);}
    }
}
