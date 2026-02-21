package bugreports;

import org.xdef.XDConstants;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.component.XComponent;
import org.xdef.impl.XConstants;
import org.xdef.sys.ArrayReporter;
import static org.xdef.sys.STester.runTest;
import org.xdef.xon.XonUtils;
import test.XDTester;
import static test.XDTester._xdNS;

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
		setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
		setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
		Object o;
		String json, xml, xdef;
		XDDocument xd;
		XDPool xp;
		XComponent xc;
		ArrayReporter reporter = new ArrayReporter();
/**/
		try { // test extension of map and correct reporting.
			xd = compile(
"<xd:def xmlns:xd='"+_xdNS+"' root='A'>\n" +
"  <xd:json name='A'>{ \"address\": { \"%script\": \"optional; ref addr\", \"x\": \"int()\"} }</xd:json>\n" +
"  <xd:json name='addr'> { \"d\": \"string()\" } </xd:json>\n" +
"</xd:def>").createXDDocument();
			jparse(xd, "{ }", reporter);
			assertNoErrorsAndClear(reporter); //OK
			jparse(xd, "{ \"address\": { \"d\": \"cde\", \"x\": 1 } }", reporter);
			assertNoErrorsAndClear(reporter); //OK
			jparse(xd, "{ \"address\": { \"d\": \"dd\" } }", reporter);
			if (reporter.size() != 1 || !reporter.toString().contains("'x'")) {
				fail(reporter.toString()); // should be XDEF539: Required element 'x' is missing
			}
			jparse(xd, "{ \"address\": { \"x\": 1 } }", reporter);
//			if (reporter.size() != 1 || !reporter.toString().contains("'d'")) {
				fail(reporter.toString()); // should be XDEF539: Required element 'd' is missing
//			}
			jparse(xd, "{ \"address\": { } }", reporter);
//			if (reporter.size() != 2) {
//E XDEF539: Required element 'd' is missing; line=1; column=15; source="STRING_DATA"; path=$.['address']; X-position=#addr/$.['d']
//E XDEF539: Required element 'x' is missing; line=1; column=15; source="STRING_DATA"; path=$.['address']; X-position=#A/$.['address'].['d']

				fail(reporter.toString()); // should be XDEF539, elements 'd' and 'x' is missing
//			}
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**
		try {
			xdef =
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"<A>\n" +
"  <B>\n" +
"    <xd:mixed xd:ref='B#B'>\n" +
//"    <xd:mixed xd:script='ref B#B'>\n" +
"      <X/>\n" +
"      <Y/>\n" +
"    </xd:mixed>\n" +
"  </B>\n" +
"</A>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_A %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='B'>\n" +
"    <xd:mixed xd:name='B'>\n" +
"      <P/>\n" +
"      <Q/>\n" +
"    </xd:mixed>\n" +
//"  <B>\n" +
//"    <xd:mixed>\n" +
//"      <P/>\n" +
//"      <Q/>\n" +
//"    </xd:mixed>\n" +
//"  </B>\n" +
"  <xd:component>\n" +
//"    %class "+_package+".Mates_B %link B;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = XDFactory.compileXD(null, xdef);
			genXComponent(xp);
			xml =
"<A>\n" +
"  <B>\n" +
"    <P/>\n" +
"    <Q/>\n" +
"    <X/>\n" +
"    <Y/>\n" +
"  </B>\n" +
"</A>\n";
			xd = xp.createXDDocument("A");
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
			xml =
"<A>\n" +
"  <B>\n" +
"    <Y/>\n" +
"    <X/>\n" +
"    <Q/>\n" +
"    <P/>\n" +
"  </B>\n" +
"</A>\n";
			xd = xp.createXDDocument("A");
			parse(xd, xml, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.xparseXComponent(xml, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertEq(xml, xc.toXml());
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**
		try {
			xdef =
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name = \"A\">\n" +
"    {\"A\":\n" +
"      {\n" +
"         \"End\":\n" +
"           {\n" +
"             \"%script\": \"ref B#B\",\n" +
"             \"x\": \"?; string()\",\n" +
"             \"y\": \"?; string()\"\n" +
"           }\n" +
"      }\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_A %link A;\n" +
 "   %interface "+_package+".Mates_A_I %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='B' root='B'>\n" +
"  <xd:json name='B'>\n" +
"    {\n" +
"       \"p\":  \"string()\",\n" +
"       \"q\":  \"? string()\"\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_B %link B;\n" +
"    %interface "+_package+".Mates_B_I %link B;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xp = compile(xdef);
			genXComponent(xp);
			json =
"{\"A\":\n" +
"   {\n" +
"     \"End\":\n" +
"       {\n" +
"         \"p\": \"P\",\n" +
"         \"q\": \"Q\",\n" +
"         \"x\": \"x\",\n" +
"         \"y\": \"y\",\n" +
"       }\n" +
"   }\n" +
"}\n";
			xd = xp.createXDDocument("A");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			json =
"{\"A\":\n" +
"   {\n" +
"     \"End\":\n" +
"       {\n" +
"         \"y\": \"y\",\n" +
"         \"x\": \"x\",\n" +
"         \"q\": \"Q\",\n" +
"         \"p\": \"P\",\n" +
"       }\n" +
"   }\n" +
"}\n";
			xd = xp.createXDDocument("A");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
/**/
		try {
			xdef =
"<xd:collection xmlns:xd='"+_xdNS+"'>\n" +
"<xd:def name='A' root='A'>\n" +
"  <xd:json name = \"A\">\n" +
"    {\"A\":\n" +
"      {\n" +
"         \"Name\":    \"string()\",\n" +
"         \"End\": [\n" +
"           {\n" +
"             \"%script\": \"+; ref EndStatuses#EndStatuses\",\n" +
"             \"x\": \"?; string()\",\n" +
//"             \"y\": \"?; string()\"\n" +
"           }\n" +
"         ]\n" +
"      }\n" +
"    }\n" +
"  </xd:json>\n" +
"  <xd:component>\n" +
"    %class "+_package+".Mates_A %link A;\n" +
// "    %interface "+_package+".Mates_A_I %link A;\n" +
"  </xd:component>\n" +
"</xd:def>\n" +
"<xd:def name='EndStatuses' root='EndStatuses'>\n" +
"  <xd:json name='EndStatuses'>\n" +
"    {\n" +
"       \"EndStatus\":  \"string()\",\n" +
"       \"ChangeLog\":  \"? string()\"\n" +
"    }\n" +
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
"{\"A\":\n" +
"            {\n" +
"                \"Name\": \"xxxxxxx\",\n" +
"                \"End\": [\n" +
"                  {\n" +
"                    \"ChangeLog\": \"Y\",\n" +
"                    \"EndStatus\": \"actionCode\",\n" +
"                    \"x\":       \"xxx\",\n" +
"                  },\n" +
"                  {\n" +
"                    \"ChangeLog\": \"Y\",\n" +
"                    \"EndStatus\": \"actionCode1\",\n" +
"                    \"x\":       \"xxx1\",\n" +
"                  }\n" +
"                ]\n" +
"            }\n" +
"}\n";
			xd = xp.createXDDocument("A");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			json =
"{\"A\":\n" +
"            {\n" +
"                \"Name\": \"xxxxxxx\",\n" +
"                \"End\": [\n" +
"                  {\n" +
"                    \"ChangeLog\": \"Y\",\n" +
"                    \"EndStatus\": \"actionCode\",\n" +
"                    \"x\":       \"xxx\",\n" +
"                  },\n" +
"                  {\n" +
"                    \"ChangeLog\": \"Y\",\n" +
"                    \"x\":       \"xxx1\",\n" +
"                    \"EndStatus\": \"actionCode1\",\n" +
"                  }\n" +
"                ]\n" +
"            }\n" +
"}\n";
			xd = xp.createXDDocument("A");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
if(true)return;
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
"                \"Statuses\": [ {\"%script\": \"+; ref Statuses#Statuses\", \"xxx\": \"?; string();\"} ]\n" +
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
"<xd:def name='EndStatuses' root='EndStatuses'>\n" +
"  <xd:json name='EndStatuses'>\n" +
"    {\n" +
//"                    \"%script\": \"?; ref Statuses$Statuses;\",\n" +
"                    \"EndStatus\":  \"  statusSet.statusName.ID()\",\n" +
"                    \"ActionCode\": \"? actionCode()\",\n" +
"                    \"ChangeLog\":  \"? changeLog()\"\n" +
"    }\n" +
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
"                \"Version\": \"2025-11-21\",\n" +
"                \"Desc\":    \"???????\",\n" +
"                \"SysValues\": [ ],\n" +
"                \"Variables\": [ ],\n" +
"                \"UserRoles\": [ ],\n" +
"                \"UsedFunctions\": [ ],\n" +
"                \"EndStatuses\": [\n" +
"                  {\n" +
"                    \"EndStatus\":  \"statusName1\",\n" +
"                    \"ActionCode\": \"actionCode\",\n" +
"                    \"ChangeLog\":  \"Y\"\n" +
"                  }\n" +
"                ],\n" +
"                \"Name\":    \"wfScriptName\",\n" +
"                \"Statuses\": [\n" +
"                  {\n" +
"                    \"ActionCode\":   \"actionCode\",\n" +
"                    \"Status\":       \"statusName\",\n" +
"                    \"TimeOverStep\": \"60H\",\n" +
"                    \"Events\": [\n" +
"                      {\n" +
"                        \"Event\":       \"eventName\",\n" +
"                        \"ActionCode\":  \"actionCode\",\n" +
"                        \"UserRoleAny\": [\"userRole\"],\n" +
"                        \"NextStatus\":  \"statusName\"\n" +
"                      }\n" +
"                    ],\n" +
"                    \"ChangeLog\":    \"Y\",\n" +
"                    \"xxx\":          \"xxx\",\n" +
"                  }\n" +
"                ]\n" +
"            }\n" +
"}\n";
			xd = xp.createXDDocument("SynPLscript");
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
			json =
"{\n" +
"  \"ActionCode\": \"actionCode\",\n" +
"  \"EndStatus\":  \"statusSet.statusName\",\n" +
"  \"ChangeLog\":  \"Y\"\n" +
"}";
			o = jparse(xd, json, reporter);
			assertNoErrorsAndClear(reporter);
			xc = xd.jparseXComponent(json, null, reporter);
			assertNoErrorsAndClear(reporter);
			assertTrue(XonUtils.xonEqual(o, xc.toXon()));
		} catch (RuntimeException ex) {fail(ex);}
//if(true)return;
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