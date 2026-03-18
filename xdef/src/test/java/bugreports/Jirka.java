package bugreports;

import java.io.StringWriter;
import java.util.List;
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
public class Jirka extends XDTester {

    public Jirka() {
        super();
        setChkSyntax(false); // here it MUST be false!
    }

    /** Run test and display error information. */
    @Override
    public void test() {
////////////////////////////////////////////////////////////////////////////////
        boolean T = false; // if false, all tests are invoked
////////////////////////////////////////////////////////////////////////////////
        System.out.println("X-definition version: " + XDFactory.getXDVersion());
////////////////////////////////////////////////////////////////////////////////
        System.setProperty(XConstants.XDPROPERTY_XDEF_DBGSWITCHES, XConstants.XDPROPERTYVALUE_DBG_SHOWXON);
        setProperty(XDConstants.XDPROPERTY_DISPLAY, XDConstants.XDPROPERTYVALUE_DISPLAY_FALSE);//true | errors
//		setProperty(XDConstants.XDPROPERTY_DEBUG,  XDConstants.XDPROPERTYVALUE_DEBUG_TRUE); // true | false
        setProperty(XDConstants.XDPROPERTY_WARNINGS, XDConstants.XDPROPERTYVALUE_WARNINGS_TRUE); //true|false
////////////////////////////////////////////////////////////////////////////////
        Object o, x, j;
        String json, s, xdef, xml;
        List list;
        XDDocument xd;
        XDPool xp;
        XComponent xc;
        StringWriter swr;
        ArrayReporter reporter = new ArrayReporter();
        try {
            xdef =
"<xd:def xmlns:xd = \"http://www.xdef.org/xdef/4.2\" name = \"Example\" root = \"S2KF\">\n" +
"  <xd:declaration>\n" +
"    type  beginReasonCode   enum('A','B','C');\n" +
"    type  caseID            long(1,999_999_999); \n" +
"    type  color             string(3,30); \n" +
"    type  companyName       string(1,100); \n" +
"    type  district          string(1,30);\n" +
"    type  endReasonCode     enum('D','E','F');\n" +
"    type  enginePower       dec(5,1); \n" +
"    type  evoVehicleID      long(1,999_999_999);\n" +
"    type  firstName         string(1,50); \n" +
"    type  grossWeight       long(1,999_999);\n" +
"    type  houseNum          string(1,10);\n" +
"    type  ico               string(1,14);\n" +
"    type  lastName          string(1,50); \n" +
"    type  phoneNum          string(1,18);\n" +
"    type  psc               string(1,6);\n" +
"    type  rc                string(1,14);\n" +
"    type  regCertificate1   regex('^[A-Z]{2}[0-9]{6}|^[A-Z]{3}[0-9]{6}');\n" +
"    type  regCertificate2   regex('^[A-Z]{2}[0-9]{6}');\n" +
"    type  spz               string(6,7); \n" +
"    type  stateCode         string(1,3);\n" +
"    type  statusCode        enum('X', 'Y', 'Z');\n" +
"    type  street            string(1,20);\n" +
"    type  subjectType       enum('NP', 'LE', 'SEP');\n" +
"    type  town              string(1,30);\n" +
"    type  vehicleBrand      string(1,20);\n" +
"    type  vehicleCategory   string(1,50);\n" +
"    type  vin               string(1,26);\n" +
"    type  xsDate            xdatetime('yyyy-MM-dd'); \n" +
"    type  xsDateTime        xdatetime('yyyy-MM-dd[THH:mm:ss]'); \n" +
"    type  yearOfManufacture integer(1920, 2050);\n" +
"  </xd:declaration>\n" +
" \n" +
"  <xd:json name = \"S2KF\">\n" +
"  {  \"caseID\":             \"  caseID()\",\n" +
"     \"createdTime\":        \"  xsDateTime()\",\n" +
"     \"modifiedTime\":       \"  xsDateTime()\",\n" +
"     \"statusCode\":         \"  statusCode()\",\n" +
"     \"holder\":             {\"%script\":  \"ref Subject\"},\n" +
"     \"owner\":              {\"%script\":  \"ref Subject\"},\n" +
//"     \"owner\":              {\"%script\":  \"?; ref Subject\"},\n" +
"  }\n" +
"  </xd:json>\n" +
"\n" +
"  <xd:json name = \"Subject\">\n" +
"   {  \"subjectType\":       \"  subjectType()\",\n" +
"      \"firstName\":         \"? firstName()\",\n" +
"      \"lastName\":          \"? lastName()\",\n" +
"      \"companyName\":       \"? companyName()\",\n" +
"      \"birthDate\":         \"? xsDate()\",\n" +
"      \"CIN\":               \"? ico()\",\n" +
"      \"PIN\":               \"? rc()\",\n" +
"      \"contacts\":   {\"%script\": \"ref Contact\"}\n" +
"   }\n" +
"  </xd:json>\n" +
"\n" +
"  <xd:json name = \"Contact\">\n" +
"   {  \"phoneNum\":  \"? phoneNum()\",\n" +
"      \"emailAddr\": \"? emailAddr()\"\n" +
"   }\n" +
"  </xd:json>\n" +
"</xd:def>";
System.out.println(xdef);
            json =
"{\"caseID\":      112233,\n" +
"\"createdTime\":  \"2026-02-02\",\n" +
"\"createdTime\":  \"2026-01-29\",\n" +
//"\"modifiedTime\": \"2026-02-02\",\n" +
"\"statusCode\":   \"Y\",\n" +
"\"holder\": {\n" +
"    \"subjectType\": \"NP\",\n" +
"    \"firstName\":   \"JAN\",\n" +
"    \"lastName\":    \"NOVÁK\",\n" +
"    \"PIN\":         \"7403160123\",\n" +
"    \"contacts\":    {\"phoneNum\": \"9988776655\"}\n" +
" },\n" +
"\"holder\": {\n" +
"    \"subjectType\": \"NP\",\n" +
"    \"firstName\":   \"JAN\",\n" +
"    \"lastName\":    \"NOVÁK\",\n" +
"    \"PIN\":         \"7403160123\",\n" +
"    \"contacts\":    {\"phoneNum\": \"9988776655\"}\n" +
//" },\n" +
//"\"owner\": {\n" +
//"    \"subjectType\": \"LE\",\n" +
//"    \"firstName\":   \"PETR\",\n" +
//"    \"lastName\":    \"NOVÁK\",\n" +
//"    \"PIN\":         \"7403160124\",\n" +
//"    \"contacts\":    {\"phoneNum\": \"9988776656\"}\n" +
" }\n" +
"}";
System.out.println(json);
            jparse(xdef, "Example", json, reporter, swr=new StringWriter(), null, null);
            if (reporter.errors()) {
                s = reporter.toString();
                 if (reporter.getErrorCount() != 4 || !s.contains("'createdTime'") || !s.contains("'holder'")
                    || !s.contains("'modifiedTime'") || !s.contains("'owner'")) {
                   fail(reporter);
                }
            }
            assertEq("", swr.toString());
        } catch (RuntimeException ex) {fail(ex);}
if(T)return;

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