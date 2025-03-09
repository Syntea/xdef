package mytests;

import org.xdef.XDFactory;
import org.xdef.XDPool;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

public class TestXP2 extends XDTester {
	TestXP2() {super();}

	@Override
	/** Run test and print error information. */
	public void test() {
		try {
			String xdef =
"<?xml version=\"1.0\" encoding=\"windows-1250\"?>\n" +
"\n" +
"<xd:def\n" +
"    xmlns:xd     = \"http://www.syntea.cz/xdef/3.1\"\n" +
"    xd:name      = \"Request_DNUpload\"\n" +
"    xd:root      = \"RequestEnv\"\n" +
"    xd:script    = \"options ignoreEmptyAttributes\"\n" +
"    impl-version = \"1.36.1\"\n" +
"    impl-date    = \"2020-10-13\"\n" +
">\n" +
"\n" +
"    <xd:macro name=\"KVADRANT\"       >list('0','1')</xd:macro>\n" +
"    <xd:macro name=\"JMENO\"          >string(1,24)</xd:macro>\n" +
"    <xd:macro name=\"PRIJMENI\"       >string(1,36)</xd:macro>\n" +
"    <xd:macro name=\"RC\"             >string(1,10)</xd:macro>\n" +
"    <xd:macro name=\"TEXTVETA\"       >string(1,80)</xd:macro>\n" +
"    <xd:macro name=\"TEXTVETA1\"      >string(1,256)</xd:macro>\n" +
"    <xd:macro name=\"TEXTODSTAVEC\"   >string(1,512)</xd:macro>\n" +
"    <xd:macro name=\"TEXTSTRANKA\"    >string(1,4000)</xd:macro>\n" +
"    <xd:macro name=\"TEXTDOKUMENT\"   >string(1,4000)</xd:macro>\n" +
"    <xd:macro name=\"DATEFORMAT\"     >datetime('d.M.y','yyyyMMdd')</xd:macro>\n" +
"    <xd:macro name=\"DATETIME\"       >datetime('d.M.y H:m','yyyyMMddHHmm00')</xd:macro>\n" +
"    <xd:macro name=\"DATETIME1\"      >datetime('d.M.y H:m:s','yyyyMMddHHmmss')</xd:macro>\n" +
"    <xd:macro name=\"DATETIMEISO\"    >datetime('yyyy-MM-ddTHH:mm:ss')</xd:macro>\n" +
"    <xd:macro name=\"TIME\"           >datetime('H:m','HHmm')</xd:macro>\n" +
"    <xd:macro name=\"TELCISLO\"       >string(1,16)</xd:macro>\n" +
"    <xd:macro name=\"STOPAPOLOHA\"    >int(-9_999_999,9_999_999)</xd:macro>\n" +
"    <xd:macro name=\"CASTKA\"         >dec(11,2)</xd:macro>\n" +
"    <xd:macro name=\"TEXTDIGESTINFO\" >string(0,1536)</xd:macro>\n" +
"\n" +
"    <xd:macro name=\"ONFALSE\"       >; onFalse setErr(4208)</xd:macro>\n" +
"    <xd:macro name=\"ONFALSETAB\"    >; onFalse setErr(4208)</xd:macro>\n" +
"    <xd:macro name=\"ONABSENCE\"     >; onAbsence setErr(4202)</xd:macro>\n" +
"    <xd:macro name=\"ONABSENCE_ELEM\">; onAbsence setErr(4201)</xd:macro>\n" +
"\n" +
"    <xd:declaration>\n" +
"        /* unikatni mnoziny id-cek pro jednotlive entity, unikatni po celem elementu DNNdn */\n" +
"        uniqueSet   osobaUnq        int();\n" +
"        uniqueSet   vecUnq          int();\n" +
"        uniqueSet   pricinaUnq      int();\n" +
"        uniqueSet   dokumentUnq     int();\n" +
"        uniqueSet   procUkonUnq     int();\n" +
"        uniqueSet   fotoCisloUnq    int();\n" +
"        /*TODO: po prechodu na novou Xdefinici 4.0 (nyni 2.0) pouzit typ Container() a metody hasNamedItem(), setNamedItem()*/\n" +
"        String      vozFotoOdomUnq  = \"\";\n" +
"        String      vozFotoVinUnq   = \"\";\n" +
"    </xd:declaration>\n" +
"\n" +
"   <RequestEnv                   xd:script= \"finally outputNOR()                                           ${ONABSENCE_ELEM}\"\n" +
"                                 MsgID    = \"required string(1,32)                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"   >\n" +
"      <ApplHeader                xd:script = \"occurs 1; ref Request_Common#ApplHeader                      ${ONABSENCE_ELEM}\" />\n" +
"      <StationHeader             xd:script = \"occurs 1; ref Request_Common#StationHeader                   ${ONABSENCE_ELEM}\" />\n" +
"      <UserHeader                xd:script = \"occurs 1; ref Request_Common#UserHeader                      ${ONABSENCE_ELEM}\" />\n" +
"\n" +
"      <DNUpload>\n" +
"         <DNNdn                  xd:script = \"occurs 1; ref DNNdn                                          ${ONABSENCE_ELEM}\" />\n" +
"      </DNUpload>\n" +
"   </RequestEnv>\n" +
"\n" +
"   <DNNdn\n" +
"           DnXMLVersion               = \"fixed '1'\"\n" +
"           BuildCode                  = \"optional int()\"\n" +
"           SeqDnCentrum               = \"optional int()                                                         ${ONFALSE}\"\n" +
"           SeqDnKlient                = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnKraj                     = \"required string(2)                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnOkres                    = \"required string(2)                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnUtvar                    = \"required string(2)                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnCislo                    = \"optional int(0,99999)                                                  ${ONFALSE}\"\n" +
"           DnVerzeAktualni            = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnVerzeSedn                = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnDatumPrvniVerze          = \"optional ${DATETIME}                                                   ${ONFALSE}\"\n" +
"           DnBlokovano                = \"required boolean()                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnBlokovanoOd              = \"optional ${DATETIME}                                                   ${ONFALSE}\"\n" +
"           KodBlokovanoUserLdap       = \"optional string()                                                      ${ONFALSE}\"\n" +
"           KodBlokovanoStationLdap    = \"optional string()                                                      ${ONFALSE}\"\n" +
"           KodUserLdap                = \"required string()                                                      ${ONFALSE} ${ONABSENCE}\"\n" +
"           Signature                  = \"optional string()                                                      ${ONFALSE}\"\n" +
"           KodNdnZaznamPlatSchval     = \"required tab('CC_NdnZaznamPlatSchval','KodNdnZaznamPlatSchval')        ${ONFALSE} ${ONABSENCE}\"\n" +
"           DnRepozitarChangeLast      = \"optional ${DATETIME1}                                                  ${ONFALSE}\"\n" +
"           DnRepozitarChangeLastVerze = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:mixed>\n" +
"         <NdnVerze                  xd:script= \"occurs 1;    ref NdnVerze                                       ${ONABSENCE_ELEM}\" />\n" +
"         <NdnDokument               xd:script= \"occurs 0..;  ref NdnDokument\"/>\n" +
"         <LokalRepozitar            xd:script= \"occurs 0..;  ref LokalRepozitar\"/>\n" +
"         <LogSession                xd:script= \"occurs 1;    ref LogSession                                     ${ONABSENCE_ELEM}\" />\n" +
"      </xd:mixed>\n" +
"   </DNNdn>\n" +
"\n" +
"   <!-- Logovn - sezen -->\n" +
"   <LogSession\n" +
"           SessionKlientCasOd         = \"required ${DATETIME}                                                   ${ONFALSE} ${ONABSENCE}\"\n" +
"           SessionKlientCasDo         = \"required ${DATETIME}                                                   ${ONFALSE} ${ONABSENCE}\"\n" +
"           SessionChangeDn            = \"required boolean()                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Ndn Verze DN -->\n" +
"   <NdnVerze\n" +
"           DnCj                       = \"optional string(0,35)                                                  ${ONFALSE}\"\n" +
"\n" +
"           KodNdnKniha                = \"optional tab('CC_NdnKniha', 'KodNdnKniha' )                            ${ONFALSE}\"\n" +
"           VerzeRokKniha              = \"required int(1,99)                                                     ${ONFALSE}\"\n" +
"           VerzeCisloKniha            = \"optional int(0,99999)                                                  ${ONFALSE}\"\n" +
"\n" +
"           SeqDnVerze                 = \"required int()                                                         ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VerzeOznaceni              = \"required ${TEXTVETA}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           VerzeDatumCas              = \"required ${DATETIME}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VerzeDatumCasDo            = \"optional ${DATETIME}                                                   ${ONFALSE}\"\n" +
"           KodSednOkres               = \"required tab('CC_SednOkres', 'KodSednOkres')                           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           VerzeUzemiUtvar            = \"required string(2)                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnStavSetreni          = \"required tab('CC_NdnStavSetreni',  'KodNdnStavSetreni')                ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnStavEvidence         = \"required tab('CC_NdnStavEvidence', 'KodNdnStavEvidence')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnKvalEvidence         = \"required tab('CC_NdnKvalEvidence', 'KodNdnKvalEvidence')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnKvalUdalost          = \"required tab('CC_NdnKvalUdalost',  'KodNdnKvalUdalost' )               ${ONFALSE}; onAbsence setText('9')\"\n" +
"           KodNdnHodnostPZ            = \"required tab('CC_NdnHodnostPZ',    'KodNdnHodnostPZ')                  ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           SynchronizaceRH            = \"required boolean()                                                     ${ONFALSE}; onAbsence setText('false')\"\n" +
"           ZraneniMrtviPocetRH        = \"required int(-1, 9999)                                                 ${ONFALSE}; onAbsence setText('-1')\"\n" +
"           ZraneniTezcePocetRH        = \"required int(-1, 9999)                                                 ${ONFALSE}; onAbsence setText('-1')\"\n" +
"           ZraneniLehcePocetRH        = \"required int(-1, 9999)                                                 ${ONFALSE}; onAbsence setText('-1')\"\n" +
"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\" >\n" +
"\n" +
"      <xd:mixed>\n" +
"         <Osoba                  xd:script= \"occurs 0..; ref Osoba\" />\n" +
"         <Vec                    xd:script= \"occurs 0..; ref Vec\" />\n" +
"         <Podnet                 xd:script= \"occurs 1..; ref Podnet\" />\n" +
"         <Ohledani               xd:script= \"occurs 1..; ref Ohledani\" />\n" +
"         <VerzePrubeh            xd:script= \"occurs 1..; ref VerzePrubeh \" />\n" +
"         <LogEvidence            xd:script= \"occurs 1..; ref LogEvidence\" />\n" +
"         <LogTisk                xd:script= \"occurs 0..; ref LogTisk\" />\n" +
"         <ProcesniUkon           xd:script= \"occurs 0..; ref ProcesniUkon\" />\n" +
"      </xd:mixed>\n" +
"   </NdnVerze>\n" +
"\n" +
"   <!-- Evidence  -->\n" +
"   <LogEvidence\n" +
"           SeqKlient                  = \"required int()                                                         ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnKvalEvidence         = \"required tab('CC_NdnKvalEvidence', 'KodNdnKvalEvidence')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnKvalUdalost          = \"required tab('CC_NdnKvalUdalost',  'KodNdnKvalUdalost' )               ${ONFALSE}; onAbsence setText('9')\"\n" +
"           DnCj                       = \"optional string(1,35)                                                  ${ONFALSE}\"\n" +
"           EvDatumPridel              = \"required ${DATEFORMAT}                                                 ${ONFALSE}    ${ONABSENCE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Vec na miste nehody -->\n" +
"   <Vec\n" +
"           SeqKlient                  = \"required vecUnq.ID()                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnTypVec               = \"required tab('CC_NdnTypVec', 'KodNdnTypVec')                           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           VecOznaceni                = \"required ${TEXTVETA}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           RefKlientVec               = \"optional vecUnq.IDREF()                                                ${ONFALSE}\"\n" +
"           KodNdnTypVazbaVecVec       = \"required tab('CC_NdnTypVazbaVecVec', 'KodNdnTypVazbaVecVec')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           VecSkoda                   = \"optional int(0,99_999_999)                                             ${ONFALSE}\"\n" +
"           VecSkodaEUR                = \"required ${CASTKA}                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecPopisSkoda              = \"optional ${TEXTSTRANKA}                                                ${ONFALSE}\"\n" +
"           VecSkodaNaklad             = \"optional int(0,99_999_999)                                             ${ONFALSE}\"\n" +
"           VecSkodaNakladEUR          = \"required ${CASTKA}                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecPopisSkodaNaklad        = \"optional ${TEXTSTRANKA}                                                ${ONFALSE}\"\n" +
"\n" +
"           VecKvadrantL               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantR               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantF               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantB               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantT               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantU               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantN               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantX               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VecKvadrantZ               = \"required ${KVADRANT}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           VecPopisL                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisR                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisF                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisB                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisT                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisU                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisN                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisX                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecPopisZ                  = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"\n" +
"           VecCisloVyrobni            = \"optional string(1,26)                                                  ${ONFALSE}\"\n" +
"           KodNdnZaznamPlat           = \"required tab('CC_NdnZaznamPlat', 'KodNdnZaznamPlat')                   ${ONFALSE} ${ONABSENCE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodNdnKvalExterniLustrace  = \"required tab('CC_NdnKvalExterniLustrace', 'KodNdnKvalExterniLustrace') ${ONFALSE}; onAbsence setAttr('KodNdnKvalExterniLustrace', '1')\">\n" +
"\n" +
"      <xd:choice  xd:script= \"occurs 0..1;\" >\n" +
"         <Vozidlo                xd:script= \"ref Vozidlo\"/>\n" +
"         <JinaVec                xd:script= \"ref JinaVec\" />\n" +
"      </xd:choice>\n" +
"\n" +
"      <xd:mixed>\n" +
"         <SubjektDrzitel         xd:script= \"occurs 0..; ref SubjektDrzitel \"/>\n" +
"         <VecProcesniUkon        xd:script= \"occurs 0..; ref VecProcesniUkon\" />\n" +
"         <VecPoloha              xd:script= \"occurs 0..; ref VecPoloha\" />\n" +
"      </xd:mixed>\n" +
"   </Vec>\n" +
"\n" +
"   <!-- Vec Procesni Ukony -->\n" +
"   <VecProcesniUkon\n" +
"           SeqKlient                  = \"required procUkonUnq.ID()\"\n" +
"           VecProcesniUkonCas         = \"required ${DATETIME}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDruhProcesniUkon     = \"required tab('CC_NdnDruhProcesniUkon', 'KodNdnDruhProcesniUkon')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           ProcesniUkonMisto          = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           RefKlientDokument          = \"optional dokumentUnq.IDREF()                                           ${ONFALSE}\"\n" +
"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:mixed>\n" +
"         <VecZajisteni           xd:script= \"occurs 0..; ref VecZajisteni\" />\n" +
"         <PatraniVec             xd:script= \"occurs 0..; ref PatraniVec\" />\n" +
"         <OdometrExtZapis        xd:script= \"occurs 0..; ref OdometrExtZapis\"/>\n" +
"      </xd:mixed>\n" +
"   </VecProcesniUkon>\n" +
"\n" +
"   <VecPoloha\n" +
"           SeqKlient                  = \"required int()\"\n" +
"           PolohaPopis                = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           PolohaZbm                  = \"required ${STOPAPOLOHA}                                                ${ONFALSE}    ${ONABSENCE}\"\n" +
"           PolohaPbm                  = \"optional ${STOPAPOLOHA}                                                ${ONFALSE}\"\n" +
"           KodNdnOhledaniZbmSmer      = \"required tab('CC_NdnOhledaniZbmSmer', 'KodNdnOhledaniZbmSmer')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnOhledaniPbmSmer      = \"required tab('CC_NdnOhledaniPbmSmer', 'KodNdnOhledaniPbmSmer')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PolohaPopisText            = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Silnicni vozidlo KodNdnVin ma defaultni hodnotu neuvedeno (-1) -->\n" +
"   <Vozidlo\n" +
"           KodNdn1KatVozidla          = \"required tab('CC_NdnKatVozidla', 'KodNdnKatVozidla')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdn2KatVozidla          = \"required string(1,10)                                                  ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           SilCisloEv                 = \"required string(1,12)                                                  ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodSednStat                = \"required tab('CC_SednStat', 'KodSednStat')                             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnVin                  = \"required string();\"\n" +
"           SilVIN                     = \"optional string(1,26)                                                  ${ONFALSE}\"\n" +
"           SilCisloTP                 = \"optional string(1,12)                                                  ${ONFALSE}\"\n" +
"           KodNdnKvalDokladVozidla    = \"required tab('CC_NdnKvalDokladVozidla', 'KodNdnKvalDokladVozidla')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SilCisloEvOsvedceni        = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           SilCisloEvidence           = \"optional string(0,8)                                                   ${ONFALSE}\"\n" +
"           SilDatum1Evidence          = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"           KodNdn1ZnackaTyp           = \"required tab('CC_NdnZnackaTyp', 'KodNdnZnackaTyp')                     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdn2ZnackaTyp           = \"required string(1,10)                                                  ${ONFALSE}    ${ONABSENCE}\"\n" +
"           SilRokVyroby               = \"optional datetime('y')                                                 ${ONFALSE}\"\n" +
"           KodNdnOdometrStavFyz       = \"required tab('CC_NdnOdometrStavFyz', 'KodNdnOdometrStavFyz')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SilKm                      = \"optional int(0,9_999_999)                                              ${ONFALSE}\"\n" +
"           SilVykon                   = \"optional int(0,99_999)                                                 ${ONFALSE}\"\n" +
"           SilCelkHmotnost            = \"optional int(0,999_999)                                                ${ONFALSE}\"\n" +
"           SilMistSezeni              = \"optional int(0,999)                                                    ${ONFALSE}\"\n" +
"           SilMistStani               = \"optional int(0,999)                                                    ${ONFALSE}\"\n" +
"           SilMistLuzek               = \"optional int(0,999)                                                    ${ONFALSE}\"\n" +
"           KodNdnBarva                = \"required tab('CC_NdnBarva', 'KodNdnBarva')                             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnTypBrzd              = \"required tab('CC_NdnTypBrzd', 'KodNdnTypBrzd')                         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SilStavBrzd                = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           SilPocetValcu              = \"optional int(0,999)                                                    ${ONFALSE}\"\n" +
"           SilObjemValcu              = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           SilPovoleneZatizeni        = \"optional int(0,999_999)                                                ${ONFALSE}\"\n" +
"\n" +
"           KodNdnTypPoistenie         = \"required tab('CC_NdnTypPoistenie', 'KodNdnTypPoistenie')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SilCisloPoistenieEv        = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           KodNdnPoistovna            = \"required tab('CC_NdnPoistovna', 'KodNdnPoistovna')                     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SilPoistovna               = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           SilPoisteniePlatneOd       = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"\n" +
"           KodSednDruhVozidla         = \"required tab('CC_SednDruhVozidla',    'KodSednDruhVozidla')            ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednProvozovatel        = \"required tab('CC_SednProvozovatel',   'KodSednProvozovatel')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednDruhPrepravy        = \"required tab('CC_SednDruhPrepravy',   'KodSednDruhPrepravy')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednStavPoNehode        = \"required tab('CC_SednStavPoNehode',   'KodSednStavPoNehode')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednVyprosteniOsob      = \"required tab('CC_SednVyprosteniOsob', 'KodSednVyprosteniOsob')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednSmerJizdy           = \"required tab('CC_SednSmerJizdy',      'KodSednSmerJizdy')              ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           SilBrzdnaDraha             = \"optional int(0,99_999)                                                 ${ONFALSE}\"\n" +
"\n" +
"           DrazniSpoj                 = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           DrazniOznaceni             = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"\n" +
"           SilTechKontrolaOd          = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"           SilTechKontrolaDo          = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"           SilTypVozidla              = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           SilVybaveniPas             = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniAirbag          = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniSedacka         = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniRam             = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniDefZona         = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniPPDP            = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniJiny            = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           SilVybaveniPopis           = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           \n" +
"           HlBezpPrZavesKolPosk       = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           HlBezpPrDeformZonyPosk     = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           HlBezpPrAirbagyPosk        = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           HlBezpPrRizeniPosk         = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"           HlBezpPrBrzdyPosk          = \"optional boolean()                                                     ${ONFALSE}\"\n" +
"\n" +
"           KodNdnStavPoNehode         = \"required tab('CC_NdnStavPoNehode',    'KodNdnStavPoNehode')            ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnVozStavPoNehode      = \"required tab('CC_NdnVozStavPoNehode', 'KodNdnVozStavPoNehode')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnVozManipulace        = \"required tab('CC_NdnVozManipulace',   'KodNdnVozManipulace')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnRizeniVozidla        = \"required tab('CC_NdnRizeniVozidla',   'KodNdnRizeniVozidla')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Jina vec -->\n" +
"   <JinaVec\n" +
"           KodNdnDruhJinaVec          = \"required tab('CC_NdnDruhJinaVec', 'KodNdnDruhJinaVec')                 ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednPrekazka            = \"required tab('CC_SednPrekazka',   'KodSednPrekazka')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Subjekt drzitel, Kodentifikace subjektu v ramci osoby  -->\n" +
"   <SubjektDrzitel\n" +
"           SeqKlient                  = \"required int()                                                         ${ONFALSE}    ${ONABSENCE}\"\n" +
"           RefKlientOsoba             = \"required osobaUnq.IDREF()                                              ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDrzitel              = \"required tab('CC_NdnDrzitel', 'KodNdnDrzitel')                         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: zajisteni, uschovani, zadrzeni -->\n" +
"   <VecZajisteni\n" +
"           KodNdnTypVecZajisteni      = \"required tab('CC_NdnTypVecZajisteni', 'KodNdnTypVecZajisteni')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           VecZajisteniPopis          = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           VecZajisteniKde            = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: patrani po veci -->\n" +
"   <PatraniVec\n" +
"           KodNdnKvalPatraniVec       = \"required tab('CC_NdnKvalPatraniVec', 'KodNdnKvalPatraniVec')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PatraniVecPopis            = \"optional ${TEXTSTRANKA}\"\n" +
"   />\n" +
"   \n" +
"    <!--\n" +
"        Procesni ukon: zapis stavu odometru do externiho registru RPZV.\n" +
"        - KodNdnDruhProcesniUkon: \"3004\"\n" +
"        - <Odpoved> - odpoved z RPZV\n" +
"        Ostatni polozky:\n" +
"        - DnIdentifikator bude centrum plnit z ../../../../self::DNNdn@SeqDnCentrum\n" +
"        - datum zpisu viz ../@VecProcesniUkonCas\n" +
"        - foto odometru - nepovinne, odkaz na foto viz ../@RefKlientDokument, takov foto je oznaeno druhem \"odometr\"\n" +
"    -->\n" +
"    <OdometrExtZapis\n" +
"        KodUserLdap                 = \"required string()                                                        ${ONFALSE}    ${ONABSENCE}\"\n" +
"        KodNdnUtvar                 = \"required tab('CC_NdnUtvar', 'KodNdnUtvar')                               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"        SilVIN                      = \"required string(1, 26)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"        SilCisloEvidence            = \"required string(1, 8)                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"        Odecet                      = \"optional ${DATETIMEISO}                                                  ${ONFALSE}\"\n" +
"        KodNdnOdometrStavFyz        = \"required tab('CC_NdnOdometrStavFyz', 'KodNdnOdometrStavFyz')             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"        SilKm                       = \"required int(0, 9_999_999)                                               ${ONFALSE}    ${ONABSENCE}\"\n" +
"        DnIdentifikator             = \"required string(1, 20)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"    >\n" +
"        <Odpoved>\n" +
"            <Id>optional int()</Id>\n" +
"            <Msg>\n" +
"                <ErrorId>optional string</ErrorId>\n" +
"                <Description>optional string</Description>\n" +
"                <KMKontrolaMsg>optional string</KMKontrolaMsg>\n" +
"                <VINexpertRes>\n" +
"                    <VINexpertErr>optional string</VINexpertErr>\n" +
"                    <VINexpertMsg>optional string</VINexpertMsg>\n" +
"                </VINexpertRes>                \n" +
"            </Msg>\n" +
"        </Odpoved>\n" +
"    </OdometrExtZapis>\n" +
"\n" +
"   <!-- Osoba -->\n" +
"   <Osoba   xd:script = \"finally checkAlkInlCount()\"\n" +
"            SeqKlient                  = \"required osobaUnq.ID()                                                 ${ONFALSE}    ${ONABSENCE}\"\n" +
"            RefKlientVec               = \"optional vecUnq.IDREF()                                                ${ONFALSE}\"\n" +
"            KodNdnKvalOsoba            = \"required tab('CC_NdnKvalOsoba', 'KodNdnKvalOsoba')                     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"            OsobaSpojeniTel            = \"optional ${TELCISLO}                                                   ${ONFALSE}\"\n" +
"            OsobaSpojeniTel2           = \"optional ${TELCISLO}                                                   ${ONFALSE}\"\n" +
"            OsobaSpojeniMail           = \"optional string(1,36)                                                  ${ONFALSE}\"\n" +
"            KodNdnZaznamPlat           = \"required tab('CC_NdnZaznamPlat', 'KodNdnZaznamPlat')                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"            LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"            KodNdnKvalExterniLustrace  = \"required tab('CC_NdnKvalExterniLustrace', 'KodNdnKvalExterniLustrace') ${ONFALSE}; onAbsence setAttr('KodNdnKvalExterniLustrace', '1')\">\n" +
"\n" +
"      <xd:choice>\n" +
"         <FO                     xd:script= \"occurs 0..1; ref FO\" />\n" +
"         <PO                     xd:script= \"occurs 0..1; ref PO\" />\n" +
"      </xd:choice>\n" +
"\n" +
"      <xd:mixed>\n" +
"         <Adresa                 xd:script= \"occurs 0..; ref Adresa\"/>\n" +
"         <Opravneni              xd:script= \"occurs 0..; ref Opravneni\" />\n" +
"         <OsobaProcesniUkon      xd:script= \"occurs 0..; ref OsobaProcesniUkon\" />\n" +
"      </xd:mixed>\n" +
"   </Osoba>\n" +
"\n" +
"   <!-- Osoba Procesni Ukony -->\n" +
"   <OsobaProcesniUkon\n" +
"           SeqKlient                  = \"required procUkonUnq.ID()                                              ${ONFALSE}    ${ONABSENCE}\"\n" +
"           OsobaProcesniUkonCas       = \"required ${DATETIME}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDruhProcesniUkon     = \"required tab('CC_NdnDruhProcesniUkon', 'KodNdnDruhProcesniUkon')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           ProcesniUkonMisto          = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           RefKlientDokument          = \"optional dokumentUnq.IDREF()                                           ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:choice>\n" +
"         <Osvedceni              xd:script= \"occurs 0..1; ref Osvedceni\" />\n" +
"         <TestOsoba              xd:script= \"occurs 0..1; ref TestOsoba\" />\n" +
"         <OsobaZajisteni         xd:script= \"occurs 0..1; ref OsobaZajisteni\" />\n" +
"         <Predvolani             xd:script= \"occurs 0..1; ref Predvolani\" />\n" +
"         <Vypoved                xd:script= \"occurs 0..1; ref Vypoved\" />\n" +
"         <Sankce                 xd:script= \"occurs 0..1; ref Sankce\" />\n" +
"         <PatraniOsoba           xd:script= \"occurs 0..1; ref PatraniOsoba\"/>\n" +
"         <UkonOsobaAlkohol       xd:script= \"occurs 0..1; ref UkonOsobaAlkohol\" />\n" +
"         <UkonOsobaInl           xd:script= \"occurs 0..1; ref UkonOsobaInl\" />\n" +
"      </xd:choice>\n" +
"   </OsobaProcesniUkon>\n" +
"\n" +
"   <!-- Fyzicka osoba -->\n" +
"   <FO\n" +
"           FoTitul                    = \"optional ${JMENO}                                                      ${ONFALSE}\"\n" +
"           FoJmeno                    = \"required ${JMENO}                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"           FoPrijmeni                 = \"required ${PRIJMENI}                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           FoDatumNarozeni            = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"           FoMistoNarozeni            = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"           FoRC                       = \"optional ${RC}                                                         ${ONFALSE}\"\n" +
"           KodNdnRc                   = \"optional string();                                                     onAbsence setText('2')\"\n" +
"           KodSednStat                = \"required tab('CC_SednStat',              'KodSednStat')                ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnPohlavi              = \"required tab('CC_NdnPohlavi',            'KodNdnPohlavi')              ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnKvalUcastnikProvoz   = \"required tab('CC_NdnKvalUcastnikProvoz', 'KodNdnKvalUcastnikProvoz')   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednUcastnikProvoz      = \"required tab('CC_SednUcastnikProvoz',    'KodSednUcastnikProvoz')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnRoleFo               = \"required tab('CC_NdnRoleFo',             'KodNdnRoleFo')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           FoZamestnavatel            = \"optional ${TEXTODSTAVEC}                                               ${ONFALSE}\"\n" +
"           FoZamestnani               = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"\n" +
"           KodSednZabezpeceni         = \"required tab('CC_SednZabezpeceni',       'KodSednZabezpeceni')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnTypZraneni           = \"required tab('CC_NdnTypZraneni',         'KodNdnTypZraneni')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           FoUcastnikPopisUcinku      = \"optional ${TEXTSTRANKA}                                                ${ONFALSE}\"\n" +
"           KodNdnStavUcastnika        = \"required tab('CC_NdnStavUcastnika',      'KodNdnStavUcastnika')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednPrvniPomoc          = \"required tab('CC_SednPrvniPomoc',        'KodSednPrvniPomoc')          ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           FoUmrti                    = \"optional ${DATETIMEISO}                                                ${ONFALSE}\"\n" +
"           FoUcastnikChovaniPopis     = \"optional ${TEXTSTRANKA}                                                ${ONFALSE}\"\n" +
"\n" +
"           KodSednKategorieChodce     = \"required tab('CC_SednKategorieChodce',   'KodSednKategorieChodce')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednSituaceChodce       = \"required tab('CC_SednSituaceChodce',     'KodSednSituaceChodce')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednChovaniChodce       = \"required tab('CC_SednChovaniChodce',     'KodSednChovaniChodce')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednKategorieRidice     = \"required tab('CC_SednKategorieRidice',   'KodSednKategorieRidice')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednOvlivneniRidice     = \"required tab('CC_SednOvlivneniRidice',   'KodSednOvlivneniRidice')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OsvedceniDelkaDrzeni       = \"optional int(0,99)                                                     ${ONFALSE}\"\n" +
"           KodNdnKvalOsobaOhledani    = \"required tab('CC_NdnKvalOsobaOhledani',  'KodNdnKvalOsobaOhledani')    ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnUcastSpecSubjekt     = \"required tab('CC_NdnUcastSpecSubjekt',   'KodNdnUcastSpecSubjekt')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnUcastnikSetrvani     = \"required tab('CC_NdnUcastnikSetrvani',   'KodNdnUcastnikSetrvani')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Pravnicka osoba -->\n" +
"   <PO\n" +
"           PoObchodniJmeno            = \"required string(1,36)                                                  ${ONFALSE}    ${ONABSENCE}\"\n" +
"           PoIC                       = \"required string(1,8)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodSednStat                = \"required tab('CC_SednStat', 'KodSednStat')                             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PoPredmetCinnosti          = \"optional ${TEXTVETA}                                                   ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Adresa -->\n" +
"   <Adresa\n" +
"           SeqKlient                  = \"required int()                                                         ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnKvalAdresa           = \"required tab('CC_NdnKvalAdresa', 'KodNdnKvalAdresa')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           AdresaObec                 = \"optional string(1,36)                                                  ${ONFALSE}\"\n" +
"           AdresaUlice                = \"optional string(1,36)                                                  ${ONFALSE}\"\n" +
"           AdresaCisloPopisne         = \"optional string(1,24)                                                  ${ONFALSE}\"\n" +
"           AdresaCisloEvidencni       = \"optional string(1,24)                                                  ${ONFALSE}\"\n" +
"           AdresaPSC                  = \"optional string(1,8)                                                   ${ONFALSE}\"\n" +
"           AdresaOkres                = \"optional string(1,36)                                                  ${ONFALSE}\"\n" +
"           KodSednStat                = \"required tab('CC_SednStat', 'KodSednStat')                             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: zajisteni komu -->\n" +
"   <Opravneni\n" +
"           SeqKlient                  = \"required int()                                                         ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnKategorieOpravneni   = \"required tab('CC_NdnKategorieOpravneni', 'KodNdnKategorieOpravneni')   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OpravneniOd                = \"optional ${DATEFORMAT}                                                 ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                     ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: zajisteni komu -->\n" +
"   <Osvedceni\n" +
"           KodNdnTypOsvedceni         = \"required tab('CC_NdnTypOsvedceni',        'KodNdnTypOsvedceni')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnTypDokladTotoznosti  = \"required tab('CC_NdnTypDokladTotoznosti', 'KodNdnTypDokladTotoznosti')  ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OsvedceniCislo             = \"optional string(1,12)                                                   ${ONFALSE}\"\n" +
"           OsvedceniKdoVydal          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           OsvedceniKdyVydal          = \"optional ${NdnMacro#DATEFORMAT}                                         ${ONFALSE}\"\n" +
"           OsvedceniPoznamka          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Test na alkohol nebo jinou navykovou latku -->\n" +
"   <TestOsoba\n" +
"           KodNdnAlkoholDruhTestu     = \"required tab('CC_NdnAlkoholDruhTestu',    'KodNdnAlkoholDruhTestu')     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnAlkoholTypPristroje  = \"required tab('CC_NdnAlkoholTypPristroje', 'KodNdnAlkoholTypPristroje')  ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           TestTypPristroje           = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           TestCisloPristroje         = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           KodNdnAlkInlVyhodTest    = \"required tab('CC_NdnAlkInlVyhodTest',       'KodNdnAlkInlVyhodTest')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           TestVysledek1              = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           TestVysledek2              = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           TestVysledek3              = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           CasOdberu1                 = \"optional ${NdnMacro#TIME}                                               ${ONFALSE}\"\n" +
"           CasOdberu2                 = \"optional ${NdnMacro#TIME}                                               ${ONFALSE}\"\n" +
"           CasOdberu3                 = \"optional ${NdnMacro#TIME}                                               ${ONFALSE}\"\n" +
"           TestHodnoceni              = \"optional ${TEXTSTRANKA}                                                 ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: zajisteni, zadrzeni -->\n" +
"   <OsobaZajisteni\n" +
"           KodNdnTypOsobaZajisteni    = \"required tab('CC_NdnTypOsobaZajisteni','KodNdnTypOsobaZajisteni')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OsobaZajisteniPopis        = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           OsobaZajisteniKde          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Predvolani-->\n" +
"   <Predvolani\n" +
"           KodNdnKvalSubjektProces    = \"required tab('CC_NdnKvalSubjektProces','KodNdnKvalSubjektProces')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnZpusobPredvolani     = \"required tab('CC_NdnZpusobPredvolani','KodNdnZpusobPredvolani')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnOznacUkonPredvolani  = \"required tab('CC_NdnOznacUkonPredvolani','KodNdnOznacUkonPredvolani')   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PredvolaniUcel             = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           PredvolaniCas              = \"required ${NdnMacro#DATETIME}                                           ${ONFALSE}    ${ONABSENCE}\"\n" +
"           PredvolaniMisto            = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Vypoved-->\n" +
"   <Vypoved\n" +
"           KodNdnKvalSubjektProces    = \"required tab('CC_NdnKvalSubjektProces','KodNdnKvalSubjektProces')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnOznacUkonVypoved     = \"required tab('CC_NdnOznacUkonVypoved','KodNdnOznacUkonVypoved')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SeqKlientVerzePricina      = \"optional int()                                                          ${ONFALSE}\"\n" +
"           VypovedUcel                = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           VypovedCas                 = \"required ${NdnMacro#DATETIME}                                           ${ONFALSE}    ${ONABSENCE}\"\n" +
"           VypovedCasDo               = \"optional ${NdnMacro#DATETIME}                                           ${ONFALSE}\"\n" +
"           VypovedMisto               = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           VypovedPritomen            = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           Vypoved                    = \"optional ${TEXTDOKUMENT}                                                ${ONFALSE}\"\n" +
"           VypovedPredchozJmeno       = \"optional ${JMENO}                                                       ${ONFALSE}\"\n" +
"           VypovedPredchozPrimeni     = \"optional ${PRIJMENI}                                                    ${ONFALSE}\"\n" +
"           VypovedRodinnyStav         = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Rozhodnuti -->\n" +
"   <Sankce\n" +
"           SankceCastka               = \"optional int()                                                          ${ONFALSE}\"\n" +
"           SankceCastkaEUR            = \"required ${CASTKA}                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnZpusobPlatby         = \"required tab('CC_NdnZpusobPlatby','KodNdnZpusobPlatby')                 ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           SankceSerieCislo           = \"required ${TEXTVETA}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Patrani po osobe -->\n" +
"   <PatraniOsoba\n" +
"           KodNdnKvalPatraniOsoba     = \"required tab('CC_NdnKvalPatraniOsoba','KodNdnKvalPatraniOsoba')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PatraniOsobaPopis          = \"optional ${TEXTSTRANKA}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Osoba alkohol -->\n" +
"   <UkonOsobaAlkohol\n" +
"           AlkMetodaDech              = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"           AlkMetodaPriznaky          = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"           AlkMetodaOdber             = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnAlkoholTypPristroje  = \"required tab('CC_NdnAlkoholTypPristroje','KodNdnAlkoholTypPristroje')   ${ONFALSE} ${ONABSENCE}\"\n" +
"           DechTextTypPristroje       = \"optional ${NdnMacro#TEXTVETA}                                           ${ONFALSE}\"\n" +
"           DechCisloPristroje         = \"optional ${NdnMacro#TEXTVETA}                                           ${ONFALSE}\"\n" +
"           DechVysledek1              = \"optional dec(3,2)                                                       ${ONFALSE}\"\n" +
"           DechVysledek2              = \"optional dec(3,2)                                                       ${ONFALSE}\"\n" +
"           DechVysledek3              = \"optional dec(3,2)                                                       ${ONFALSE}\"\n" +
"           DechCasOdberu1             = \"optional ${NdnMacro#DATETIME}                                           ${ONFALSE}\"\n" +
"           DechCasOdberu2             = \"optional ${NdnMacro#DATETIME}                                           ${ONFALSE}\"\n" +
"           DechCasOdberu3             = \"optional ${NdnMacro#DATETIME}                                           ${ONFALSE}\"\n" +
"           DechOsobaPoucen            = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"           DechOsobaOdmitl            = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"\n" +
"           KodNdnAlkInlRozhodOVys     = \"required tab('CC_NdnAlkInlRozhodOVys', 'KodNdnAlkInlRozhodOVys')        ${ONFALSE} ${ONABSENCE}\"\n" +
"           LekarPriznakPopis          = \"optional ${NdnMacro#TEXTSTRANKA}                                        ${ONFALSE}\"\n" +
"           KrevOdberOdmitl            = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"           KrevCasOdberu              = \"optional ${NdnMacro#DATETIME}                                           ${ONFALSE}\"\n" +
"           KrevVysledek               = \"optional dec(4,2)                                                       ${ONFALSE}\"\n" +
"           KrevVysledekMgNaL          = \"optional dec(4,2)                                                       ${ONFALSE}\"\n" +
"           KodNdnAlkInlVyhodTestLekar = \"required tab('CC_NdnAlkInlVyhodTest',   'KodNdnAlkInlVyhodTest')        ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnAlkInlVyhodTestPolicie = \"required tab('CC_NdnAlkInlVyhodTest', 'KodNdnAlkInlVyhodTest')        ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodNdnAlkInlDalsiJizda     = \"required tab('CC_NdnAlkInlDalsiJizda',  'KodNdnAlkInlDalsiJizda')       ${ONFALSE} ${ONABSENCE}\"\n" +
"           AlkCelkoveHodnoceni        = \"optional ${NdnMacro#TEXTSTRANKA}                                        ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Osoba navykova latka -->\n" +
"   <UkonOsobaInl\n" +
"           InlMetodaPsychoMotorika    = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           InlMetodaVysPristroj       = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           InlMetodaOdber             = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           PsychoMotorickaCas         = \"optional ${NdnMacro#DATETIME}                                          ${ONFALSE}\"\n" +
"           PsychoMotorickaPopis       = \"optional ${NdnMacro#TEXTSTRANKA}                                       ${ONFALSE}\"\n" +
"\n" +
"           VysPristrojPoucen          = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           VysPristrojiOdmitl         = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           VysPristrojCas             = \"optional ${NdnMacro#DATETIME}                                          ${ONFALSE}\"\n" +
"           KodNdnAlkInlVyhodTestLekar = \"required tab('CC_NdnAlkInlVyhodTest', 'KodNdnAlkInlVyhodTest')         ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnAlkInlRozhodOVys     = \"required tab('CC_NdnAlkInlRozhodOVys', 'KodNdnAlkInlRozhodOVys')       ${ONFALSE} ${ONABSENCE}\"\n" +
"           KrevOdberOdmitl            = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           KrevCasOdberu              = \"optional ${NdnMacro#DATETIME}                                          ${ONFALSE}\"\n" +
"           DruhScreenDrogLeciv        = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekOpiaty            = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekKanabinoidy       = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekKokain            = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekMetadon           = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekAmfetaminy        = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekMatamfetaminy     = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekExtaza            = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekFencyklidin       = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekBenzodiazepiny    = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekBarbituraty       = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           PozadavekTricyklickeAntidepres = \"required int()                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           DetekceOpiaty              = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceKanabinoidy         = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceKokain              = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceMetadon             = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceAmfetaminy          = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceMatamfetaminy       = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceExtaza              = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceFencyklidin         = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceBenzodiazepiny      = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceBarbituraty         = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DetekceTricyklickeAntidepres = \"required int()                                                       ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           DruhScreenPrchLatek        = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodNdnAlkInlVyhodTestPrch  = \"required tab('CC_NdnAlkInlVyhodTest',  'KodNdnAlkInlVyhodTest')        ${ONFALSE} ${ONABSENCE}\"\n" +
"           DruhScreenToxLatek         = \"required int()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodNdnAlkInlVyhodTestTox   = \"required tab('CC_NdnAlkInlVyhodTest',  'KodNdnAlkInlVyhodTest')        ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnAlkInlVyhodTestPolicie= \"required tab('CC_NdnAlkInlVyhodTest', 'KodNdnAlkInlVyhodTest')        ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodNdnAlkInlDalsiJizda     = \"required tab('CC_NdnAlkInlDalsiJizda', 'KodNdnAlkInlDalsiJizda')       ${ONFALSE} ${ONABSENCE}\"\n" +
"           InlCelkoveHodnoceni        = \"optional ${TEXTSTRANKA}                                                ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Podnet -->\n" +
"   <Podnet\n" +
"           SeqKlient                  = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnPodnetZdroj          = \"required tab('CC_NdnPodnetZdroj','KodNdnPodnetZdroj')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           PodnetKdoNahlasilJmeno     = \"optional ${JMENO}                                                       ${ONFALSE} \"\n" +
"           PodnetKdoNahlasilPrijmeni  = \"optional ${PRIJMENI}                                                    ${ONFALSE}\"\n" +
"           PodnetKdoPrijalJmeno       = \"optional ${JMENO}                                                       ${ONFALSE}\"\n" +
"           PodnetKdoPrijalPrijmeni    = \"required ${PRIJMENI}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"           PodnetKdoNahlasilTel       = \"optional ${TELCISLO}                                                    ${ONFALSE}\"\n" +
"           PodnetOpatreni             = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           PodnetCas                  = \"required ${DATETIME}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Stanoveni verze : popis prubehu DN jako pravni skutecnosti -->\n" +
"   <VerzePrubeh\n" +
"           SeqKlient                  = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           PrubehPopis                = \"optional ${TEXTSTRANKA}                                                 ${ONFALSE}\"\n" +
"           KodSednZavineni            = \"required tab('CC_SednZavineni','KodSednZavineni')                       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednAlkohol             = \"required tab('CC_SednAlkohol','KodSednAlkohol')                         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           TestVysledek               = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           KodSednDruhNehody          = \"required tab('CC_SednDruhNehody','KodSednDruhNehody')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednStretnuti           = \"required tab('CC_SednStretnuti','KodSednStretnuti')                     ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnInlVyhodTestVinik    = \"required tab('CC_NdnAlkInlVyhodTest', 'KodNdnAlkInlVyhodTest')          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnAlkVyhodTestVinik    = \"required tab('CC_NdnAlkInlVyhodTest', 'KodNdnAlkInlVyhodTest')          ${ONFALSE}    ${ONABSENCE}\"\n" +
"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:mixed>\n" +
"         <VerzePricina xd:script= \"occurs 0..; ref VerzePricina \" />\n" +
"      </xd:mixed>\n" +
"   </VerzePrubeh>\n" +
"\n" +
"   <!-- Stanoveni verze : popis pricin a nasledku DN -->\n" +
"   <VerzePricina\n" +
"           SeqKlient                  = \"required pricinaUnq.ID()                                                ${ONFALSE}    ${ONABSENCE}\"\n" +
"           RefKlientOsoba             = \"optional osobaUnq.IDREF()                                               ${ONFALSE}\"\n" +
"           KodNdnKvalObecObjekt       = \"required tab('CC_NdnKvalObecObjekt', 'KodNdnKvalObecObjekt')            ${ONFALSETAB} ;onAbsence setText('1')\"\n" +
"           KodNdn1PricinyTc           = \"required tab('CC_NdnPricinyTc',      'KodNdnPricinyTc')                 ${ONFALSETAB} ;onAbsence setText('00')\"\n" +
"           KodNdn2PricinyTc           = \"required string(1,10)                                                   ${ONFALSE}    ;onAbsence setText('01')\"\n" +
"           KodNdn1NasledkyTc          = \"required tab('CC_NdnNasledkyTc',     'KodNdnNasledkyTc')                ${ONFALSETAB} ;onAbsence setText('99')\"\n" +
"           KodNdn2NasledkyTc          = \"required string(1,10)                                                   ${ONFALSE}    ;onAbsence setText('01')\"\n" +
"           KodNdn1Priciny             = \"required tab('CC_NdnPriciny',        'KodNdnPriciny')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdn2Priciny             = \"required string(1,10)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdn1Nasledky            = \"required tab('CC_NdnNasledky',       'KodNdnNasledky')                  ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdn2Nasledky            = \"required string(1,10)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdn1Sankcie             = \"required tab('CC_NdnSankcie',        'KodNdnSankcie')                   ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdn2Sankcie             = \"required string(1,10)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"           RozhodPricina              = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}; finally zkontrolujHlavniPricinu()\"\n" +
"           KodNdnZaznamPlat           = \"required tab('CC_NdnZaznamPlat',     'KodNdnZaznamPlat')                ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Logovani : tisk -->\n" +
"   <LogTisk\n" +
"           SeqKlient                  = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDruhDokumentu        = \"required tab('CC_NdnDruhDokumentu',  'KodNdnDruhDokumentu')             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           TiskCas                    = \"required ${DATETIME}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"           NazevTiskDokument          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           RefKlientDokument          = \"optional dokumentUnq.IDREF()                                            ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Procesni ukon: Ohledani -->\n" +
"   <Ohledani\n" +
"           SeqKlient                  = \"fixed '1'\"\n" +
"           OhledCas                   = \"required ${DATETIME}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"           OhledCasDo                 = \"optional ${DATETIME}                                                    ${ONFALSE}\"\n" +
"           OhledKdoVykonal            = \"required ${TEXTVETA}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"           OhledKdoPritomen           = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           KodSednPovrchVozovky       = \"required tab('CC_SednPovrchVozovky',    'KodSednPovrchVozovky')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednKvalitaPovrchu      = \"required tab('CC_SednKvalitaPovrchu',   'KodSednKvalitaPovrchu')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledKvalitaPovrchu        = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednStavPovrchu         = \"required tab('CC_SednStavPovrchu',      'KodSednStavPovrchu')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledStavPovrchu           = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednStavKrajnice        = \"required tab('CC_SednStavKrajnice',     'KodSednStavKrajnice')          ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledStavKrajnice          = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednZavadaKomunikace    = \"required tab('CC_SednZavadaKomunikace', 'KodSednZavadaKomunikace')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednDeleniKomunikace    = \"required tab('CC_SednDeleniKomunikace', 'KodSednDeleniKomunikace')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledDeleniKomunikace      = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednPocasi              = \"required tab('CC_SednPocasi',           'KodSednPocasi')                ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednViditelnost         = \"required tab('CC_SednViditelnost',      'KodSednViditelnost')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednRozhled             = \"required tab('CC_SednRozhled',          'KodSednRozhled')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledRozhled               = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednRizeniProvozu       = \"required tab('CC_SednRizeniProvozu',    'KodSednRizeniProvozu')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledRizeniProvozu         = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           KodSednSituovaniDn         = \"required tab('CC_SednSituovaniDn',      'KodSednSituovaniDn')           ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednSpecifickeObjektyDn = \"required tab('CC_SednSpecifickeObjektyDn','KodSednSpecifickeObjektyDn') ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednSmerovePomery       = \"required tab('CC_SednSmerovePomery',    'KodSednSmerovePomery')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednRychlostniLimit     = \"required tab('CC_SednRychlostniLimit',  'KodSednRychlostniLimit')       ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnHustotaProvozu       = \"required tab('CC_NdnHustotaProvozu',    'KodNdnHustotaProvozu')         ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OhledRychlostniLimit       = \"optional ${TEXTVETA1}                                                   ${ONFALSE}\"\n" +
"           OhledDopravniSituace       = \"optional string(1,1536)                                                 ${ONFALSE}\"\n" +
"           OhledBodZakl               = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           OhledBodPomoc1             = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           OhledBodPomoc2             = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n";
			xdef +=
"           OhledBodZaklSmer           = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           OhledPocetStop             = \"optional int()                                                          ${ONFALSE}\"\n" +
"\n" +
"           KodSednVObci               = \"required tab('CC_SednVObci',            'KodSednVObci')                 ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednMistoDn             = \"required tab('CC_SednMistoDn',          'KodSednMistoDn')               ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednDruhKomunikace      = \"required tab('CC_SednDruhKomunikace',   'KodSednDruhKomunikace')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodSednDruhKomunikaceKriz  = \"required tab('CC_SednDruhKomunikaceKriz', 'KodSednDruhKomunikaceKriz')  ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           VerzeCisloKomunikace       = \"optional string(1,7)                                                    ${ONFALSE}\"\n" +
"           KodNdnSufixKomunikace      = \"required tab('CC_NdnSufixKomunikace',   'KodNdnSufixKomunikace' )       ${ONFALSE} ;onAbsence setText('0')\"\n" +
"           VerzeKm                    = \"optional int(0,999_999_999)                                             ${ONFALSE}\"\n" +
"           VerzeCisloUzlu1            = \"optional num(3,4)                                                       ${ONFALSE}\"\n" +
"           VerzeCisloUzlu2            = \"optional num(3,4)                                                       ${ONFALSE}\"\n" +
"           VerzeGPSSirka              = \"optional dec(9,7)                                                       ${ONFALSE}\"\n" +
"           VerzeGPSDelka              = \"optional dec(9,7)                                                       ${ONFALSE}\"\n" +
"           VerzePopisMista            = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           VerzeObec                  = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           SilProMotVoz               = \"optional boolean()                                                      ${ONFALSE}\"\n" +
"\n" +
"           KodNdnDruhKomunikaceKm     = \"required tab('CC_NdnDruhKomunikaceKm',   'KodNdnDruhKomunikaceKm')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnDruhKomunikaceKmKriz = \"required tab('CC_NdnDruhKomunikaceKm',   'KodNdnDruhKomunikaceKm')      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnDruhKomunikaceUzly   = \"required tab('CC_NdnDruhKomunikaceUzly', 'KodNdnDruhKomunikaceUzly')    ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           KodNdnUsekKmNaOkresy       = \"required tab('CC_NdnUsekKmNaOkresy',     'KodNdnUsekKmNaOkresy')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"\n" +
"           FotodokumentacePopis       = \"optional ${TEXTSTRANKA}                                                 ${ONFALSE}\"\n" +
"           RefKlientRefFoto           = \"optional dokumentUnq.IDREF()                                            ${ONFALSE}\"\n" +
"           KodNdnGpsRefFotoKval       = \"required tab('CC_NdnGpsRefFotoKval',     'KodNdnGpsRefFotoKval')        ${ONFALSE} ${ONABSENCE}\"\n" +
"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE} ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:mixed>\n" +
"         <Stopa                       xd:script= \"occurs 0..; ref Stopa\" />\n" +
"         <OhledaniProcesniUkon        xd:script= \"occurs 0..; ref OhledaniProcesniUkon\" />\n" +
"      </xd:mixed>\n" +
"   </Ohledani>\n" +
"\n" +
"   <Opatreni\n" +
"           KodNdnDruhOpatreni         = \"required tab('CC_NdnDruhOpatreni', 'KodNdnDruhOpatreni')                ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OpatreniPopis              = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <ExtLustrace\n" +
"           KodNdnTypExtLustrace       = \"required tab('CC_NdnTypExtLustrace', 'KodNdnTypExtLustrace')            ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           LustraceKriterium          = \"required ${TEXTODSTAVEC}                                                ${ONFALSE}    ${ONABSENCE}\"\n" +
"           LustraceResult             = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"   <ExtLustraceVozidla\n" +
"           idEvx                      = \"optional int()                                                          ${ONFALSE}\"\n" +
"           isdncallid                 = \"required string()                                                       ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDigestXslt           = \"optional tab('CC_NdnDigestXslt', 'KodNdnDigestXslt')                    ${ONFALSETAB}\"\n" +
"           digestInfo                 = \"optional ${TEXTDIGESTINFO}                                              ${ONFALSE}\"\n" +
"           vozidloVPatrani            = \"optional boolean()                                                      ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <Stopa\n" +
"           SeqKlient                  = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           StopaNumber                = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDruhStopy            = \"required tab('CC_NdnDruhStopy', 'KodNdnDruhStopy')                      ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           StopaPopis                 = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           StopaZbm                   = \"optional ${STOPAPOLOHA}                                                 ${ONFALSE}\"\n" +
"           StopaPbm                   = \"optional ${STOPAPOLOHA}                                                 ${ONFALSE}\"\n" +
"           KodNdnOhledaniZbmSmer      = \"required tab('CC_NdnOhledaniZbmSmer', 'KodNdnOhledaniZbmSmer')          ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           KodNdnOhledaniPbmSmer      = \"required tab('CC_NdnOhledaniPbmSmer', 'KodNdnOhledaniPbmSmer')          ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           StopaZajisteni             = \"required int()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"           StopaZajisteniPopis        = \"optional ${TEXTSTRANKA}                                                 ${ONFALSE}\"\n" +
"           StopaZajisteniKde          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <OhledaniProcesniUkon\n" +
"           xd:script = \"finally {\n" +
"               String refKlientVec  = toString(xpath('@RefKlientVec'));\n" +
"               String refKlientVecU = '|' + refKlientVec + '|';\n" +
"               String seqKlient     = toString(xpath('@SeqKlient'));\n" +
"               String logFD         = toString(xpath('VlozeniFoto/@LogFotodokumentace'));\n" +
"               String vlFoDruh      = toString(xpath('VlozeniFoto/@KodNdnDruhFoto'));\n" +
"               \n" +
"               if (refKlientVec != '' AND logFD == 'true' AND vlFoDruh == 'odom') {\n" +
"                   if (vozFotoOdomUnq.indexOf(refKlientVecU) == -1) {\n" +
"                       vozFotoOdomUnq += refKlientVecU;\n" +
"                   } else {\n" +
"                       setErr(4208);\n" +
"                   }\n" +
"               }\n" +
"               \n" +
"               if (refKlientVec != '' AND logFD == 'true' AND vlFoDruh == 'vin') {\n" +
"                   if (vozFotoVinUnq.indexOf(refKlientVecU) == -1) {\n" +
"                       vozFotoVinUnq += refKlientVecU;\n" +
"                   } else {\n" +
"                       setErr(4208);\n" +
"                   }\n" +
"               }\n" +
"           }\"\n" +
"           SeqKlient                  = \"required procUkonUnq.ID()\"\n" +
"           ProcesniUkonCas            = \"required ${DATETIME}                                                    ${ONFALSE}    ${ONABSENCE}\"\n" +
"           KodNdnDruhProcesniUkon     = \"required tab('CC_NdnDruhProcesniUkon', 'KodNdnDruhProcesniUkon')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           ProcesniUkonMisto          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           RefKlientVec               = \"optional vecUnq.IDREF()                                                 ${ONFALSE}\"\n" +
"           RefKlientDokument          = \"optional dokumentUnq.IDREF()                                            ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:choice>\n" +
"         <VlozeniFoto       xd:script = \"match toString(xpath('@LogFotodokumentace')) == 'true';  occurs 1; ref VlozeniFoto\"\n" +
"           FotoCislo                  = \"required fotoCisloUnq.ID()                                              ${ONFALSE} ${ONABSENCE}\"\n" +
"         />\n" +
"         <VlozeniFoto       xd:script = \"occurs 1; ref VlozeniFoto\"/>\n" +
"         <FotodokUkonceni   xd:script = \"occurs 1; ref FotodokUkonceni\"/>\n" +
"         <FotodokManipulace xd:script = \"occurs 1; ref FotodokManipulace\"/>\n" +
"         <RepozitarSynchro  xd:script = \"occurs 1; ref RepozitarSynchro\"/>\n" +
"      </xd:choice>\n" +
"   </OhledaniProcesniUkon>\n" +
"\n" +
"   <VlozeniFoto\n" +
"           FotoCislo                  = \"required int()                                                          ${ONFALSE} ${ONABSENCE}\"\n" +
"           FotoPopis                  = \"optional ${TEXTODSTAVEC}                                                ${ONFALSE}\"\n" +
"           FotoDatumZaberu            = \"optional ${NdnMacro#DATETIME1}                                          ${ONFALSE}\"\n" +
"           FotoGPSDelka               = \"optional dec(9,7)                                                       ${ONFALSE}\"\n" +
"           FotoGPSSirka               = \"optional dec(9,7)                                                       ${ONFALSE}\"\n" +
"           LogFotodokumentace         = \"required boolean()                                                      ${ONFALSE} ${ONABSENCE}\"\n" +
"           FotoRozliseniX             = \"required int()\"\n" +
"           FotoRozliseniY             = \"required int()\"\n" +
"           KodNdnDruhFoto             = \"required tab('CC_NdnDruhFoto', 'KodNdnDruhFoto')\"\n" +
"           FotoRozliseniXUploaded     = \"required int()\"\n" +
"           FotoRozliseniYUploaded     = \"required int()\"\n" +
"   />\n" +
"\n" +
"    <FotodokUkonceni\n" +
"            SeqInstallation           = \"required int()\"\n" +
"    />\n" +
"\n" +
"    <FotodokManipulace\n" +
"           KodNdnFotodokManipulaceZpusob   = \"required tab('CC_NdnFotodokManipulaceZpusob', 'KodNdnFotodokManipulaceZpusob')\"\n" +
"           KodNdnFotodokManipulaceDuvod    = \"required tab('CC_NdnFotodokManipulaceDuvod',  'KodNdnFotodokManipulaceDuvod')\"\n" +
"           SeqInstallationOld              = \"required int()\"\n" +
"           SeqInstallationNew              = \"required int()\"\n" +
"    />\n" +
"\n" +
"    <RepozitarSynchro\n" +
"           SeqInstallation            = \"required int()\"\n" +
"    />\n" +
"\n" +
"   <!-- Procesni ukon: k DN -->\n" +
"   <ProcesniUkon\n" +
"           SeqKlient                  = \"required procUkonUnq.ID()                                               ${ONFALSE}    ${ONABSENCE}\"\n" +
"           RefKlientOsoba             = \"optional osobaUnq.IDREF()                                               ${ONFALSE}\"\n" +
"           RefKlientVec               = \"optional vecUnq.IDREF()                                                 ${ONFALSE}\"\n" +
"           RefKlientPricina           = \"optional pricinaUnq.IDREF()                                             ${ONFALSE}\"\n" +
"           RefKlientOhledani          = \"optional int()                                                          ${ONFALSE}\"\n" +
"           RefKlientDokument          = \"optional dokumentUnq.IDREF()                                            ${ONFALSE}\"\n" +
"           KodNdnDruhProcesniUkon     = \"required tab('CC_NdnDruhProcesniUkon', 'KodNdnDruhProcesniUkon')        ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           ProcesniUkonCas            = \"optional ${DATETIME}                                                    ${ONFALSE}\"\n" +
"           ProcesniUkonMisto          = \"optional ${TEXTVETA}                                                    ${ONFALSE}\"\n" +
"           LogSession                 = \"required boolean()                                                      ${ONFALSE}    ${ONABSENCE}\"\n" +
"   >\n" +
"      <xd:choice>\n" +
"         <HlaseniOperacnimu          xd:script = \"occurs 1; ref HlaseniOperacnimu\" />\n" +
"         <Opatreni                   xd:script = \"occurs 1; ref Opatreni\" />\n" +
"         <ExtLustrace                xd:script = \"occurs 1; ref ExtLustrace\" />\n" +
"         <ExtLustraceVozidla         xd:script = \"occurs 1; ref ExtLustraceVozidla\" />\n" +
"      </xd:choice>\n" +
"   </ProcesniUkon>\n" +
"\n" +
"   <!-- Procesni ukon: k DN -->\n" +
"   <HlaseniOperacnimu\n" +
"           KodNdnKvalHlaseniOper   = \"required tab('CC_NdnKvalHlaseniOper', 'KodNdnKvalHlaseniOper')             ${ONFALSETAB} ${ONABSENCE}\"\n" +
"           OperacniRok             = \"required int(2010, 2030)                                                   ${ONFALSE}    ${ONABSENCE}\"\n" +
"           OperacniDen             = \"required int(1,366)                                                        ${ONFALSE}    ${ONABSENCE}\"\n" +
"           HlaseniKomentar         = \"optional ${TEXTSTRANKA}                                                    ${ONFALSE}\"\n" +
"           Nezverejnit             = \"required boolean()                                                         ${ONFALSE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Zaznam adresy -->\n" +
"   <AdresaZaznam\n" +
"           AdresaObec              = \"optional string(1,36)                                                      ${ONFALSE}\"\n" +
"           AdresaUlice             = \"optional string(1,36)                                                      ${ONFALSE}\"\n" +
"           AdresaCisloPopisne      = \"optional string(1,24)                                                      ${ONFALSE}\"\n" +
"           AdresaCisloEvidencni    = \"optional string(1,24)                                                      ${ONFALSE}\"\n" +
"           AdresaPSC               = \"optional string(1,8)                                                       ${ONFALSE}\"\n" +
"           AdresaOkres             = \"optional string(1,36)                                                      ${ONFALSE}\"\n" +
"           KodSednStat             = \"required string()                                                          ${ONFALSE}    ${ONABSENCE}\"\n" +
"   />\n" +
"\n" +
"   <!-- Dokument -->\n" +
"   <NdnDokument\n" +
"           SeqDokumentCentrum      = \"optional int()\"\n" +
"           SeqDokumentKlient       = \"required int()\"\n" +
"           SeqKlient               = \"required dokumentUnq.ID()\"\n" +
"           SeqInstallation         = \"required int()\"\n" +
"           SeqDnKlient             = \"optional int()\"\n" +
"           FileName                = \"required ${NdnMacro#FILEJMENO}                                            ${ONFALSE} ${ONABSENCE}\"\n" +
"           FileHashMD5             = \"required ${NdnMacro#HASHMD5} \"\n" +
"           KodNdnZaznamPlat        = \"required string()\"\n" +
"           KodStationLdap          = \"optional int()\"\n" +
"           LogDokumentObsah        = \"optional boolean()                                                        ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodSysContentType       = \"required tab('CC_SysContentType', 'KodSysContentType')\"\n" +
"   >\n" +
"      <FileProperties        xd:script=\"occurs 1; ref Request_DNUploadDokument#FileProperties\"/>\n" +
"   </NdnDokument>\n" +
"\n" +
"   <!-- Popis repozitare dokumentu nehody na instalaci  -->\n" +
"   <LokalRepozitar xd:script= \"occurs 0..*;\"\n" +
"           KodUserLdap             = \"required string()                                                         ${ONFALSE} ${ONABSENCE}\"\n" +
"           DeleteDatumRequest      = \"optional ${DATETIME1}                                                     ${ONFALSE}\"\n" +
"           DeleteDatum             = \"optional ${DATETIME1}                                                     ${ONFALSE}\"\n" +
"           CreateDatumRequest      = \"required ${DATETIME1}                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           CreateDatum             = \"optional ${DATETIME1}                                                     ${ONFALSE} ${ONABSENCE}\"\n" +
"           SeqInstallation         = \"required int()                                                            ${ONFALSE} ${ONABSENCE}\"\n" +
"           KodStationLdap          = \"required int()                                                            ${ONFALSE} ${ONABSENCE}\"\n" +
"           LogRepozitarSynchro     = \"required boolean()\"\n" +
"   />\n" +
"\n" +
"</xd:def>";
			XDPool xp = XDFactory.compileXD(null, xdef);
		} catch (RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		if (runTest(args) > 0) {System.exit(1);}
	}

}
