package mytests;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

/** Test INI
 * @author Vaclav Trojan
 */
public class TestIni4j {

	private static Map<String, Object> readIni(final String data) throws Exception {
		Ini ini = new Ini();
		Map<String, Object> result = new LinkedHashMap<>();
		ini.load(new StringReader(data));
		for (String section: ini.keySet()) {
			if (section != null) {
				Map<String, Object> item = new LinkedHashMap<>();
				result.put(section, item);
				Section sect = ini.get(section);
				for (String name: sect.keySet()) {
					String s = sect.get(name);
					if (s.isEmpty()) {
						s = null;
					}
					item.put(name, s);
				}
			} else {
				System.out.println("=========NULL=========");
			}
		}
		return result;
	}

	private static void testIni(final String data) {
		try {
			Map<String, Object> result = readIni(data);
			for (String section:  result.keySet()) {
				System.out.println("[" + section + "]");
				Map item = (Map) result.get(section);
				for (Object x:  item.keySet()) {
					String s = (String) item.get(x);
					if (s == null) {
						s = " ===========NULL===========";
					}
					System.out.println("'" + x + "=" + s + "'");
				}
			}
		} catch (Exception ex) {throw new RuntimeException(ex);}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String data =
"  # comment 1\n" +
"; comment 2\n" +
" [X]\n" +
"date = 2021-02-03 #comment\n" +
"name = Jan Novak \n" +
" \t ;comment 3\n" +
"email = \n" +
" [X.Y]\n" +
" IPAddr = 255.0.0.0\n";
		testIni(data);
		data =
" [happy]\n" +
" age = 99\n" +
" height = 77.66\n" +
" homeDir = /home/happy\n" +
"[dopey]\n" +
" weight = ${bashful/weight}\n" +
" height = ${doc/height}\n" +
"\n" +
"[bashful]\n" +
" weight = 45.7\n" +
" height = 98.8\n" +
"\n" +
" [doc]\n" +
" weight = 49.5\n" +
" height = 87.7";
		testIni(data);
		data =
"[Launch]\n" +
"ProjectID=900\n" +
"FileType=0x0000\n" +
"Show=0x0000\n" +
"Maker=E\n" +
"\n" +
"[Launch_Win]\n" +
"Path=EProjManager.exe\n" +
"Option=\n" +
"Reg=HKEY_LOCAL_MACHINE\\SOFTWARE\\EPSON\\Event Manager,InstallPath1\n" +
"\n" +
"[Launch_Mac9]\n" +
"Path=ePm1\n" +
"Option=\n" +
"ProjectAppName=Event Manager\n" +
"\n" +
"[Launch_MacX]\n" +
"Path=ePm1\n" +
"Option=\n" +
"AppPath=/Applications/Epson Software/Event Manager/EProjManager\n" +
"ProjectAppName=Event Manager\n" +
"\n" +
"[Event_Launch_Win]\n" +
"Path=EProjManager.exe\n" +
"Option=\n" +
"Reg=HKEY_LOCAL_MACHINE\\SOFTWARE\\EPSON\\Event Manager,InstallPath\n" +
"\n" +
"[Event_Launch_Mac9]\n" +
"Path=ePm1\n" +
"Option=\n" +
"ProjectAppName=Event Manager\n" +
"\n" +
"[Event_Launch_MacX]\n" +
"Path=ePm1\n" +
"Option=\n" +
"AppPath=/Applications/EPSON/Creativity Suite/Event Manager/EProjManager\n" +
"ProjectAppName=Event Manager";
		testIni(data);

//		data =
//"[INFO] PROVIDER=; TABLENAME=; DEFAULTQUERY=;\n" +
//"VERSION=1,50,3518,0;\n" +
//"TABLE=Privlib;  PROVIDER=TestPWProv_VC; TABLENAME=Privlib;\n" +
//"OUTPUT=c:\\bin\\TestPWProv_VC.ini; BINDINGTYPE=DBTYPE_VARIANT;";
//		testIni(data);
//		data =
//"TableDump.exe PROVIDER=MSDASQL; data source=OLE_DB_NWind_Jet;\n" +
//"user id =admin; password=; TABLENAME=Customers;\n" +
//"DEFAULTQUERY=\"select * from Customers\"; OUTPUT=Customers.ini;\n" +
//"ROOT_URL=MSDASQL://dso/session/%s;";
//		testIni(data);
	}
}
