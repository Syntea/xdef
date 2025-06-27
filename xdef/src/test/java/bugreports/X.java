package bugreports;

import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.xml.KXmlUtils;

/**
 *
 * @author trojan
 */
public class X {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String xdef =
"<xd:def xmlns:xd    =\"http://www.xdef.org/xdef/4.0\"\n" +
"        xd:name     =\"JIRAscript\"\n" +
"        xd:root     =\"JIRAscript\"\n" +
"        impl-version=\"1.0.0_0\" impl-date=\"2025-06-09\">\n" +
"\n" +
"<xd:declaration>\n" +
"     type xdate               xdatetime('yyyy-MM-dd');\n" +
"     type isCheckPoint        enum('Y','N');\n" +
"     type name                string(1,30);\n" +
"     type role                enum('SD','PM','AN','PRG','TE');\n" +
"     type script              string();\n" +
"     type timeOverSec         int(1,3600);\n" +
"     type valueType           enum('STRING', 'INTEGER', 'LONG', 'DATE', 'ELEMENT', 'BOOLEAN');\n" +
"     type version             string(1,20);\n" +
"\n" +
"     uniqueSet StateSet       {StateName:      name()};\n" +
"     uniqueSet variableSet    {variableName:   name()};\n" +
"     uniqueSet eventStateSet  {StateName:      name();     eventName: name()};\n" +
"</xd:declaration>\n" +
"\n" +
"<JIRAscript\n" +
"           Name              =\"  name()\"\n" +
"           Version           =\"  version()\"\n" +
"           Date              =\"  xdate()\">\n" +
"   <Variables>\n" +
"     <SysValue               xd:script=\"0..;\"\n" +
"           Name              =\"  variableSet.variableName.ID()\"\n" +
"           ValueType         =\"  valueType()\"\n" +
"     />\n" +
"     <Const                  xd:script=\"0..;  ref Variable\"/>\n" +
"     <Variable               xd:script=\"0..;  ref Variable\"/>\n" +
"   </Variables>\n" +
"\n" +
"   <State               \n" +
"           Name              =\"  (enum('INIT') AND eventStateSet.StateName)\">\n" +
"     <Transition             xd:script=\"ref Transition\"\n" +
"          Event             =\"  (enum('START') AND eventStateSet.eventName)\"\n" +
"          Role              =\"  (enum('SD')\"\n" +
"     />\n" +
"   </State>\n" +
"\n" +
"   <xd:mixed>\n" +
"     <State                  xd:script=\"0..\"\n" +
"           Name              =\"  (StateSet.StateName.ID() AND eventStateSet.StateName)\"\n" +
"           CheckPoint        =\"? isCheckPoint()\"\n" +
"           TimeOverStep      =\"? timeOverSec()\">\n" +
"           optional script();\n" +
"       <Transition           xd:script=\"1..;  ref Transition\"/>\n" +
"     </State>\n" +
"\n" +
"<!--\n" +
"     <IfState               xd:script=\"0..;\"\n" +
"           Name              =\"  StateSet.StateName.ID()\" >\n" +
"           optional script();\n" +
"       <Condition>required script();</Condition>\n" +
"       <Yes   NextState=\"  StateSet.StateName.IDREF()\">? script();</Yes>\n" +
"       <No    NextState=\"  StateSet.StateName.IDREF()\">? script();</No>\n" +
"     </IfState>\n" +
"     <SubState              xd:script=\"0.. \">\n" +
"       <RefState            xd:script=\"0..\"\n" +
"           Name              =\"  StateSet.StateName.IDREF()\"\n" +
"       />\n" +
"       <Event                xd:script=\"1..;  ref Event\"/>\n" +
"     </SubState>\n" +
"   </xd:mixed>\n" +
"-->\n" +
"\n" +
"   <EndState                xd:script=\"1\" \n" +
"           Name              =\"  (StateSet.StateName.ID())\"\n" +
"           optional script();\n" +
"   </EndState>\n" +
"</JIRAscript>\n" +
"\n" +
"<Variable\n" +
"           Name              =\"  variableSet.variableName.ID()\"\n" +
"           ValueType         =\"  valueType()\">\n" +
"        required script();\n" +
"</Variable>\n" +
"\n" +
"<Transition                 xd:script=\"finally eventStateSet.ID()\"\n" +
"           Event             =\"  eventStateSet.eventName\"\n" +
"           NextState         =\"  StateSet.StateName.IDREF()\">\n" +
"           Role              =\"  role()\">\n" +
"        optional script();\n" +
"</Transition>\n" +
"\n" +
"</xd:def>";
		try {
			KXmlUtils.parseXml(xdef);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			XDPool xp = XDFactory.compileXD(null, xdef);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
