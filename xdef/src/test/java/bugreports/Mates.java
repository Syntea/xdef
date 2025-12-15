package bugreports;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/** Tests.
 * @author Vaclav Trojan
 */
public class Mates extends XDTester {

	public Mates() {
		super();
		setChkSyntax(false); // here it MUST be false!
	}

	/** Run test and display error information. */
	@Override
	public void test() {
		System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
		System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
		setProperty(XDConstants.XDPROPERTY_DISPLAY, // xdef_display
			XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_TRUE); // true | errors | false
//			XDConstants.XDPROPERTYVALUE_DISPLAY_ERRORS);// true | errors | false
//		setProperty(XDConstants.XDPROPERTY_DEBUG, // xdef_debug
//			XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
		Object o;
		String json, xdef;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		StringWriter swr;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try {
			xdef =
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='SynPLscript' impl-version='2025/11.0' impl-date='2025-11-07' root='SynPLscript'>\n" +
"  <xd:declaration scope='global'>\n" +
"        type  actionCode      string(1,9999);\n" +
"        type  changeLog       enum('Y');\n" +
"        type  desc            string(1,1000);\n" +
"        type  eventName       string(1,30);\n" +
"        type  functionDesc    string(1,100);\n" +
"        type  interval        regex('^[0-9]{1,3}[S|M|H|D]$');\n" +
"        type  scriptVersion   xdatetime('yyyy-MM-dd');\n" +
"        type  statusName      string(1,100);\n" +
"        type  sysValue        string(1,100);\n" +
"        type  userRole        string(1,30);\n" +
"        type  valueType       enum('STRING', 'INTEGER', 'LONG', 'DATE', 'DATETIME', 'INTERVAL');\n" +
"        type  variableName    string(1,30);\n" +
"        type  variableRef     string(1,255);\n" +
"        type  wfScriptName    string(1,30);\n" +
"\n" +
"        uniqueSet statusSet      { statusName: statusName() }; /* duplicita proměnných */\n" +
"        uniqueSet variableSet    { variableName: variableName() }; /* duplicita proměnných */\n" +
"        uniqueSet eventStatusSet { statusName: statusName(); eventName: eventName()};/*duplicita eventu*/\n"+
"  </xd:declaration>\n" +
"\n" +
"  <xd:json name = \"SynPLscript\">\n" +
"        {\"SynPLscript\":\n" +
"            {\n" +
"                \"Name\":    \"  wfScriptName()\",\n" +
"                \"Version\": \"  scriptVersion()\",\n" +
"                \"Desc\":    \"  desc()\",\n" +
"                \"Note\":    \"? desc()\",\n" +
"                \"SysValues\": [ {\"%script\": \"*; ref SysValues#SysValues;\"} ],\n" +
"                \"Variables\": [ {\"%script\":  \"*; ref Variables#Variables;\"} ],\n" +
"                \"UserRoles\": [ {\"%script\":  \"*; ref UserRoles#UserRoles\"} ],\n" +
"                \"UsedFunctions\": [ {\"%script\":  \"*; ref UsedFunctions#UsedFunctions\"} ],\n" +
"                \"Statuses\": [ {\"%script\": \"+; ref Statuses#Statuses\"} ]\n" +
"                \"EndStatuses\": [ {\"%script\": \"+; ref EndStatuses#EndStatuses\"} ]\n" +
"            }\n" +
"        }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_SynPLscript %link SynPLscript;\n" +
"    %interface "+_package+".Mates_SynPLscript_I %link SynPLscript;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'SysValues'>\n" +
"  <xd:json name='SysValues'>\n" +
"                 { \"SysValue\": \"  variableSet.variableName.ID()\",\n" +
"                   \"Type\":     \"  valueType()\",\n" +
"                   \"Value\":    \"  sysValue()\",\n" +
"                   \"Desc\":     \"? desc()\"}\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_SysValues %link SysValues;\n" +
"    %interface "+_package+".Mates_SysValues_I %link SysValues;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'Variables'>\n" +
"<xd:json name='Variables'>\n" +
"                 {\n" +
"                 \"Variable\": \"  variableSet.variableName.ID()\",\n" +
"                 \"Type\":     \"  valueType()\",\n" +
"                 \"Ref\":      \"? variableRef()\",\n" +
"                 \"Desc\":     \"? desc()\"}\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_Variables %link Variables;\n" +
"    %interface "+_package+".Mates_Variables_I %link Variables;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'UserRoles'>\n" +
"  <xd:json name='UserRoles'>\n" +
"                 { \"UserRole\": \"  userRole()\",\n" +
"                   \"Desc\":     \"  desc()\"}\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_UserRoles %link UserRoles;\n" +
"    %interface "+_package+".Mates_UserRoles_I %link UserRoles;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'UsedFunctions'>\n" +
"  <xd:json name='UsedFunctions'>\n" +
"                 {\n" +
"                    \"%anyName\": \"  functionDesc()\",\n" +
"                    \"Params\":   \"  desc()\"}\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_UsedFunctions %link UsedFunctions;\n" +
"    %interface "+_package+".Mates_UsedFunctions_I %link UsedFunctions;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'Statuses'>\n" +
"  <xd:json name = 'Statuses'>\n" +
"                {\n" +
"                  \"Status\":       \"  statusSet.statusName.ID() AND eventStatusSet.statusName()\",\n" +
"                  \"ActionCode\":   \"? actionCode()\",\n" +
"                  \"TimeOverStep\": \"? interval() OOR variableSet.variableName.IDREF()\",\n" +
"                  \"ChangeLog\":    \"? changeLog()\",\n" +
"                  \"Events\": [\n" +
"                    {\"%script\": \"+\",\n" +
"                      \"Event\":       \"  eventStatusSet.eventName.ID()\",\n" +
"                      \"UserRoleAny\": [\"1.. userRole()\"],\n" +
"                      \"ActionCode\":  \"? actionCode()\",\n" +
"                      \"NextStatus\":  \"? statusSet.statusName.IDREF()\"\n" +
"                    }\n" +
"                  ]\n" +
"                }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_Statuses %link Statuses;\n" +
"    %interface "+_package+".Mates_Statuses_I %link Statuses;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name = 'EndStatuses'>\n" +
"  <xd:json name = 'EndStatuses'>\n" +
"                    {\n" +
"                    \"EndStatus\":  \"  statusSet.statusName.ID()\",\n" +
"                    \"ActionCode\": \"? actionCode()\",\n" +
"                    \"ChangeLog\":  \"? changeLog()\"\n" +
"                    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_EndStatuses %link EndStatuses;\n" +
"    %interface "+_package+".Mates_EndStatuses_I %link EndStatuses;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp);
			json =
"{\"SynPLscript\":\n" +
"            {\n" +
"                \"Name\":    \"wfScriptName\",\n" +
"                \"Version\": \"2025-11-21\",\n" +
"                \"Desc\":    \"???????\",\n" +
"                \"SysValues\": [ ],\n" +
"                \"Variables\": [ ],\n" +
"                \"UserRoles\": [ ],\n" +
"                \"UsedFunctions\": [ ],\n" +
"                \"Statuses\": [\n" +							//10
"                  {\n" +
"                    \"Status\":       \"statusName\",\n" +
"                    \"ActionCode\":   \"actionCode\",\n" +
"                    \"TimeOverStep\": \"60H\",\n" +
"                    \"ChangeLog\":    \"Y\",\n" +
"                    \"Events\": [\n" +
"                      {\n" +
"                        \"Event\":       \"eventName\",\n" +
"                        \"UserRoleAny\": [\"userRole\"],\n" +
"                        \"ActionCode\":  \"actionCode\",\n" +	//20
"                        \"NextStatus\":  \"statusName\"\n" +
"                      }\n" +
"                    ]\n" +
"                  }\n" +
"                ],\n" +
"                \"EndStatuses\": [\n" +
"                  {\n" +
"                    \"EndStatus\":  \"statusName1\",\n" +
"                    \"ActionCode\": \"actionCode\",\n" +
"                    \"ChangeLog\":  \"Y\"\n" +					//30
"                  }\n" +
"                ]\n" +
"            }\n" +
"}\n";
			xd = xp.createXDDocument("SynPLscript");
			jparse(xd, json, reporter, swr=new StringWriter(), null, null);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			Mates_SynPLscript xPLscript = (Mates_SynPLscript) xc;
			Map<String, Object> map = xPLscript.getMap$();
			System.out.println(map.get("SynPLscript").getClass());
			System.out.println(map.get("SynPLscript"));
			Map map1 = (Map) map.get("SynPLscript");
			System.out.println(map1.get("Statuses"));
			List list1 = (List) map1.get("Statuses");
			Map map2 = (Map) list1.get(0);
			System.out.println(map2.get("Events"));
			List list2 = (List) map2.get("Events");
			Map map3 = (Map) list2.get(0);
			System.out.println(map3);
			System.out.println(map3.get("NextStatus"));
			Mates_SynPLscript xsyn = (Mates_SynPLscript) xc;
			xsyn.getjx$map();
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**/
		clearTempDir(); // delete temporary files.
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {
			System.exit(1);
		}
	}
}
