package bugreports;

import java.io.File;
import org.xdef.sys.ArrayReporter;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import test.XDTester;
import org.xdef.XDBuilder;
import org.xdef.impl.XPool;
import org.xdef.sys.SUtils;

public class Smid1 extends XDTester {

	public Smid1() {super();}

	@Override
	public void test() {
		ArrayReporter reporter = new ArrayReporter();
		final XDBuilder xb;
		String dataDir;
		try {
/**
			Properties properties = new Properties();
			properties.setProperty("xdef_ignoreUnresolvedExternals", "true");
			xb = XDFactory.getXDBuilder(reporter, properties);
			dataDir = "D:/syntea/projects/xdefinition/xdef-data-generator/repo/xdef-data-generator/src/test/resources/xdef/complex_p1/*.xdef";
			File[] defFiles = SUtils.getFileGroup(new String[]{dataDir});
			xb.setSource(defFiles);
			XDPool xdPool = xb.compileXD();
			XDDocument xdDocument = xdPool.createXDDocument("SouborP1A");
			String xml = "" +
					"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
					"<SouborP1A xmlns=\"http://ws.ckp.cz/pis/ds/L1/2013/09\" xmlns:vr=\"http://ws.ckp.cz/gam/vr/common/2018/04\">\n" +
					"  <NovaPS CisloDokladuPojisteni=\"+\" CisloSmlouvy=\"'y\\\" DatumUzavreni=\"2020-05-21\" IdentZaznamu=\"G\" NositelPSP=\"S\" PoradiVozidla=\"1\">\n" +
					"    <Pojistnik/>\n" +
					"    <Provozovatel/>\n" +
					"    <Vlastnik/>\n" +
					"    <VozidloSmlouvy DruhPojisteni=\"\" Leasing=\"A\" LimitPlneniMajetek=\"35000000\" LimitPlneniZdravi=\"35000000\" Pojistne=\"0\" PojistneBM=\"0\" TarifniSkupina=\"\">\n" +
					"      <Vozidlo DruhVozidla=\"\" KodModeluVozidla=\"\" MPZ=\"\"/>\n" +
					"    </VozidloSmlouvy>\n" +
					"    <DobaPojisteni PojisteniOd=\"30.09.2000 12:04\"/>\n" +
					"  </NovaPS>\n" +
					"  <UkonceniPS CisloSmlouvy=\"ZDy\" CisloTP=\"eH234630\" DatumUkonceni=\"2006-11-30\" DuvodZanikuSmlouvy=\"\" IdentZaznamu=\"B\" PojisteniDo=\"2020-02-1602:54\" PoradiVozidla=\"4\" SPZ=\"SZ\"/>\n" +
					"  <ReaktivacePS CisloDokladuPojisteni=\"*\" CisloSmlouvy=\"E\\+\" DatumReaktivace=\"30.08.2000\" IdentZaznamu=\"K\" PoradiVozidla=\"6\">\n" +
					"    <Pojistnik/>\n" +
					"    <Provozovatel/>\n" +
					"    <Vlastnik/>\n" +
					"    <VozidloSmlouvy DruhPojisteni=\"\" Leasing=\"A\" LimitPlneniMajetek=\"35000000\" LimitPlneniZdravi=\"35000000\" Pojistne=\"0\" PojistneBM=\"0\" TarifniSkupina=\"\">\n" +
					"      <Vozidlo DruhVozidla=\"\" KodModeluVozidla=\"\" MPZ=\"\"/>\n" +
					"    </VozidloSmlouvy>\n" +
					"    <DobaPojisteni PojisteniOd=\"2014-10-2207:19\"/>\n" +
					"  </ReaktivacePS>\n" +
					"  <ZmenaPS CisloDokladuPojisteni=\"g\" CisloSmlouvy=\"9u_\" DatumZmeny=\"17.06.2017\" IdentZaznamu=\"#\" PoradiVozidla=\"3\">\n" +
					"    <Pojistnik/>\n" +
					"    <Provozovatel/>\n" +
					"    <Vlastnik/>\n" +
					"    <VozidloSmlouvy DruhPojisteni=\"\" Leasing=\"A\" LimitPlneniMajetek=\"35000000\" LimitPlneniZdravi=\"35000000\" Pojistne=\"0\" PojistneBM=\"0\" TarifniSkupina=\"\">\n" +
					"      <Vozidlo DruhVozidla=\"\" KodModeluVozidla=\"\" MPZ=\"\"/>\n" +
					"    </VozidloSmlouvy>\n" +
					"    <DobaPojisteni PojisteniOd=\"2013-07-2613:11\"/>\n" +
					"  </ZmenaPS>\n" +
					"  <NovaIdentPS CisloSmlouvy=\"IM@\" DatumZmeny=\"20200711\" IdentZaznamu=\"l\" PoradiVozidla=\"7\">\n" +
					"    <PuvodniIdentPS CisloSmlouvy=\"{\\8\" KodPojistitele=\"\" PoradiVozidla=\"1\"/>\n" +
					"  </NovaIdentPS>\n" +
					"  <OpravaPS CisloDokladuPojisteni=\"Y\" CisloSmlouvy=\"ZNp\" DatumUzavreni=\"2019-04-16\" IdentZaznamu=\"^\" NositelPSP=\"S\" PoradiVozidla=\"6\">\n" +
					"    <Pojistnik/>\n" +
					"    <Provozovatel/>\n" +
					"    <Vlastnik/>\n" +
					"    <VozidloSmlouvy DruhPojisteni=\"\" Leasing=\"A\" LimitPlneniMajetek=\"35000000\" LimitPlneniZdravi=\"35000000\" Pojistne=\"0\" PojistneBM=\"0\" TarifniSkupina=\"\">\n" +
					"      <Vozidlo DruhVozidla=\"\" KodModeluVozidla=\"\" MPZ=\"\"/>\n" +
					"    </VozidloSmlouvy>\n" +
					"    <DobaPojisteni PojisteniOd=\"203103230814\"/>\n" +
					"  </OpravaPS>\n" +
					"  <OpravaPolPS CisloDokladuPojisteni=\"x\" CisloSmlouvy=\"V8K\" DatumUkonceni=\"19.12.2003\" DatumUzavreni=\"20000303\" DuvodZanikuSmlouvy=\"\" IdentZaznamu=\"c\" NositelPSP=\"S\" PoradiVozidla=\"9\">\n" +
					"    <Pojistnik/>\n" +
					"    <Provozovatel/>\n" +
					"    <Vlastnik/>\n" +
					"    <VozidloSmlouvy DruhPojisteni=\"\" Leasing=\"A\" LimitPlneniMajetek=\"35000000\" LimitPlneniZdravi=\"35000000\" Pojistne=\"0\" PojistneBM=\"0\" TarifniSkupina=\"\">\n" +
					"      <Vozidlo DruhVozidla=\"\" KodModeluVozidla=\"\" MPZ=\"\"/>\n" +
					"    </VozidloSmlouvy>\n" +
					"    <DobaPojisteni PojisteniOd=\"201707251306\"/>\n" +
					"  </OpravaPolPS>\n" +
					"  <OpravaXPolPS CisloDokladuPojisteni=\"k\" CisloSmlouvy=\"usP\" DatumUkonceni=\"25.11.2000\" DatumUzavreni=\"2013-06-17\" DuvodZanikuSmlouvy=\"\" IdentZaznamu=\"E\" NositelPSP=\"S\" PoradiVozidla=\"3\"/>\n" +
					"  <ZruseniPS CisloSmlouvy=\"Z8!\" DatumZruseni=\"20280902\" IdentZaznamu=\"x\" PoradiVozidla=\"8\"/>\n" +
					"  <VydaniZK CisloSmlouvy=\"_i0\" IdentZaznamu=\"5\" PoradiVozidla=\"0\">\n" +
					"    <ZelenaKarta CisloZK=\"&quot;\" PlatnostZKDo=\"28.03.2022\" PlatnostZKOd=\"2014-04-10\"/>\n" +
					"  </VydaniZK>\n" +
					"  <ZneplatneniZK CisloSmlouvy=\"kD7\" IdentZaznamu=\"/\" PoradiVozidla=\"3\">\n" +
					"    <ZelenaKarta CisloZK=\")\" DatumZneplatneni=\"20270702\" PlatnostZKDo=\"2006-12-29\" PlatnostZKOd=\"05.09.2027\"/>\n" +
					"  </ZneplatneniZK>\n" +
					"  <ZruseniZK CisloSmlouvy=\"EE-\" DatumZruseni=\"10.03.2022\" IdentZaznamu=\"h\" PoradiVozidla=\"3\">\n" +
					"    <ZelenaKarta CisloZK=\"'\" PlatnostZKDo=\"01.07.2006\" PlatnostZKOd=\"14.02.2028\"/>\n" +
					"  </ZruseniZK>\n" +
					"  <DotazPS CisloSmlouvy=\"!,n\" DatumUcinnosti=\"2015-10-27\" IdentZaznamu=\"l\" PoradiVozidla=\"1\"/>\n" +
					"</SouborP1A>\n";
			xdDocument.xparse(xml, reporter);
/**/

			xb = XDFactory.getXDBuilder(reporter, null);
			System.out.println(new File("D:/cvs/DEV/java/xdef31/resources/cz/syntea/xdef/impl/compile/").exists());
			dataDir =
				"D:/cvs/DEV/java/xdef31/resources/cz/syntea/xdef/impl/compile/*.xdef";
			File[] defFiles = SUtils.getFileGroup(new String[]{dataDir});
			System.out.println("Num of fiiles: "  +defFiles.length);
			xb.setSource(defFiles);
			XDPool xdPool = xb.compileXD();
			if (xdPool instanceof XPool && ((XPool)xdPool).getCode() == null) {
				throw new RuntimeException(
					"XDPool is not initialized (code == null).");
			}
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}