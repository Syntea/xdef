package test.xdef;

import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.component.GenXComponent;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.XDFactory;
import cz.syntea.xdef.XDPool;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.sys.FUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Generate XComponents Java source files.
 * @author Vaclav Trojan
 */
public class TestXComponentsGen {

	XComponent _X, _Y, _G;

	private String _XX;
	private int _flags;

////////////////////////////////////////////////////////////////////////////////
	static final String XDEF =
"<xd:collection xmlns:xd='http://www.syntea.cz/xdef/2.0'>\n"+
"\n"+
"<xd:def>\n"+
"<xd:component>\n"+
"  %bind Jedna %with test.xdef.TestXComponents_Y04 %link Y04#Part/@One;\n"+
"  %bind g %with test.xdef.TestXComponentsGen %link G#G/@g;\n"+
"  %bind XXX %with test.xdef.TestXComponentsGen %link G#G/XXX;\n"+
"  %bind YYY %with test.xdef.TestXComponentsGen %link G#G/YYY;\n"+
"  %class test.xdef.component.AZ\n"+
"     extends test.xdef.TestXComponentsGen %link A#A/Z;\n"+//010
"  %class test.xdef.component.J %link J#A;\n"+
"  %class test.xdef.component.Z %link SouborD1A#SouborD1A;\n"+
"  %class test.xdef.component.Z1 %link SouborD1A#ZaznamPDN;\n"+
"  %class test.xdef.component.Z2 %link SouborD1A#ProtokolDN;\n"+
"  %class test.xdef.component.Z3 %link SouborD1A#Skoda;\n"+
"  %class test.xdef.component.Z4 %link SouborD1A#Osoba;\n"+
"  %class test.xdef.component.Z5 %link SouborD1A#Firma;\n"+
"  %class test.xdef.component.Z6 %link SouborD1A#Doklad;\n"+
"  %class test.xdef.component.Z7 %link SouborD1A#RozhodnutiDN;\n"+
"  %class test.xdef.component.Z8 %link SouborD1A#ObjStrankaDN;\n"+//020
"  %class test.xdef.component.Z9 %link SouborD1A#Adresa;\n"+
"  %class test.xdef.component.FotoDN %link SouborD1A#FotoDN;\n"+
"  %class test.xdef.component.VozidloDN %link SouborD1A#VozidloDN;\n"+
"  %class test.xdef.component.TramvajDN %link SouborD1A#TramvajDN;\n"+
"  %class test.xdef.component.TrolejbusDN %link SouborD1A#TrolejbusDN;\n"+
"  %class test.xdef.component.VlakDN %link SouborD1A#VlakDN;\n"+
"  %class test.xdef.component.PovozDN %link SouborD1A#PovozDN;\n"+
"  %class test.xdef.component.PredmetDN %link SouborD1A#PredmetDN;\n"+
"  %class test.xdef.component.ZvireDN %link SouborD1A#ZvireDN;\n"+
"  %class test.xdef.component.UcastnikDN %link SouborD1A#UcastnikDN;\n"+//030
"  %class test.xdef.component.A %link A#A;\n"+
"\n"+
"  %class test.xdef.component.B %link B#A;\n"+
"  %class test.xdef.component.C extends test.xdef.TestXComponents_C\n"+
"         %link C#Town;\n"+
"  %class test.xdef.component.C1 implements test.xdef.component.CI\n"+
"         %link C#Person;\n"+
"  %class test.xdef.component.C2 %link C#Town/Street/House;\n"+
"  %interface test.xdef.component.CI %link C#Person;\n"+
"</xd:component>\n"+//040
"</xd:def>\n"+
"<xd:def name='A' root='A'>\n"+
"  <xd:declaration> type XY enum('XX','YY');\n"+
"    type flt float(); type dat xdatetime('yyyy-MM-dd');\n"+
"    uniqueSet uflt flt; uniqueSet udat dat;\n"+
"  </xd:declaration>\n"+
"  <A a='string' dec='?dec(5,2)'>\n"+
"    <W xd:script='*' w='string(1, 6)'>?string(1, 6)</W>\n"+
"    <Y xd:script='*'>?num</Y>\n"+
"    <i xd:script='?'>integer();</i>\n"+//050
"    <f xd:script='?'>uflt()</f>\n"+
"    <d xd:script='?'>udat()</d>\n"+
"    <t xd:script='?'>xdatetime('HH:mm:ss')</t>\n"+
"    <s xd:script='?'>string</s>\n"+
"    <Z xd:script='?' z='string'/>\n"+
"    <d1 xd:script='?' d='dateYMDhms'>dateYMDhms</d1>\n"+
"    <d2 xd:script='*' d='emailDate'>emailDate</d2>\n"+
"    <d-e.f xd:script='?' d-e.f='string'>string</d-e.f>\n"+
"  </A>\n"+
"</xd:def>\n"+//060
"\n"+
"<xd:def name='B' root='A'>\n"+
"  <A id = \"? int; create from('i/text()')\"\n"+
"    num = \"? float; create from('f/text()')\"\n"+
"    date = \"? xdatetime('yyyy-MM-dd'); create from('d/text()')\"\n"+
"    time = \"? xdatetime('HH:mm:ss'); create from('t/text()')\"\n"+
"    name = \"? string; create from('s/text()')\"/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='C' root='Town | Persons'\n"+//070
"  methods='void test.xdef.TestXComponents_C.test(XXData)' >\n"+
"<Town Name=\"string\">\n"+
"  <Street xd:script=\"+\" Name=\"string\">\n"+
"    <House xd:script='*' Num='int'>\n"+
"      <Person xd:script=\"*; ref Person\" />\n"+
"    </House>\n"+
"  </Street>\n"+
"</Town>\n"+
"<Person FirstName=\"string\" LastName=\"string; finally test()\"/>\n"+
"<Persons>\n"+//080
"  <Person xd:script=\"*; create from('//Person');\">\n"+
"    string; create \n"+
"      from('@FirstName') + \" \" + from('@LastName') + \"; \" +\n"+
"      from('../../@Name')+' '+from('../@Num')+', '+from('/Town/@Name')\n"+
"  </Person>\n"+
"</Persons>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='D' root='A'>\n"+
"<xd:component>%class test.xdef.component.D %link A</xd:component>\n"+//090
"<A>\n"+
"  <_ xd:script='?'/>\n"+
"  <B A='boolean'>\n"+
"    string; <X xd:script='*'>boolean</X> string;\n"+
"    <Y>boolean</Y>\n"+
"  </B>\n"+
"  <I A='int'>\n"+
"    <X xd:script='*'>int</X>\n"+
"    <Y>int</Y>\n"+
"  </I>\n"+//100
"  <F A='flt'>\n"+
"    <X xd:script='*'>float</X>\n"+
"    <Y>float</Y>\n"+
"  </F>\n"+
"  <G A='base64Binary'>\n"+
"    <X xd:script='*'>base64Binary</X>\n"+
"    <Y>base64Binary</Y>\n"+
"  </G>\n"+
"  <H A='hexBinary'>\n"+
"    <X xd:script='*'>hexBinary</X>\n"+//110
"    <Y>hexBinary</Y>\n"+
"  </H>\n"+
"  <P A='dec'>\n"+
"    <X xd:script='*'>dec</X>\n"+
"    <Y>dec</Y>\n"+
"  </P>\n"+
"  <Q A='xdatetime(\"yyyy-MM-dd\")'>\n"+
"    <X xd:script='*'>xdatetime('yyyy-MM-dd')</X>\n"+
"    <Y>dat</Y>\n"+
"  </Q>\n"+//120
"  <R A='duration'>\n"+
"    <X xd:script='*'>duration</X>\n"+
"    <Y>duration</Y>\n"+
"  </R>\n"+
"  <S A='string'>\n"+
"    <X xd:script='*'>string</X>\n"+
"    <Y>string</Y>\n"+
"  </S>\n"+
"  <E/>\n"+
"  <T xd:script='?' xmlns='x.y' t='string'><I/></T>\n"+//130
"  <a:T xd:script='?' xmlns:a='a.b' a:t='?string'><a:I/></a:T>\n"+
"</A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='E' root='if'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.E %link if;\n"+
"  %bind Clazz %link if/class;\n"+
"</xd:component>\n"+
"  <if><class try='string'/></if>\n"+//140
"</xd:def>\n"+
"\n"+
"<xd:def name='F' root='X|Y'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.F %link X;\n"+
"  %class test.xdef.component.F1 %link Y\n"+
"</xd:component>\n"+
"  <X>\n"+
"    <xd:choice>\n"+
"		<A/>\n"+//150
"		<B/>\n"+
"    </xd:choice>\n"+
"  </X>\n"+
"  <Y>\n"+
"    <xd:mixed>\n"+
"		<A/>\n"+
"		<B/>\n"+
"    </xd:mixed>\n"+
"  </Y>\n"+
"</xd:def>\n"+//160
"\n"+
"<xd:def name='G' root='G'\n"+
"   xd:methods='void test.xdef.TestXComponentsGen.genXC(XXNode);'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.G extends test.xdef.TestXComponentsGen %link G\n"+
"</xd:component>\n"+
"  <G xd:script = 'finally ;' g = 'string'>\n"+
"    <XXX x = 'string; finally genXC()'/>\n"+
"    <YYY xd:script = '+' y = 'string; finally genXC()'/>\n"+
"  </G>\n"+//170
"</xd:def>\n"+
"\n"+
"<xd:def xmlns:s='soap' xmlns:b='request' name='H' root='s:H'>\n"+
"<xd:component>%class test.xdef.component.H %link s:H</xd:component>\n"+
" <s:H\n"+
"   s:encodingStyle = \"fixed 'encoding'\">\n"+
"   <s:Header>\n"+
"     <b:User xd:script = 'occurs 1; ref I#User'\n"+
"       s:understand = \"fixed 'true'\"\n"+
"       s:actor = 'illegal' />\n"+//180
"     <b:Request xd:script = 'occurs 1; ref I#Request'\n"+
"       s:understand = \"fixed 'true'\"\n"+
"       s:actor = 'illegal' />\n"+
"   </s:Header>\n"+
"   <s:Body>\n"+
"	  <xd:choice>\n"+
"       <b:Ping xd:script = 'occurs 1; ref I#Ping'/>\n"+
"       <b:PingFlow xd:script = 'occurs 1; ref I#PingFlow'/>\n"+
"	  </xd:choice>\n"+
"   </s:Body>\n"+//190
" </s:H>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name='I' root = 'User|Request|Ping|PingFlow'>\n"+
"<xd:component>%class test.xdef.component.I %link Ping</xd:component>\n"+
"  <User IdentUser = 'required string(1,32)'/>\n"+
"  <Request IdentZpravy = 'required string(1,32)'\n"+
"     ReqMsgId    = 'optional int()'\n"+
"     Mode = \"optional enum('STD', 'TST')\"/>\n"+
"  <Ping/>\n"+//200
"  <PingFlow Flow = \"required enum('B1', 'B1B')\"/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name='J' root='A|B|C'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.J1 %link B;\n"+
"  %class test.xdef.component.J2 %link C;\n"+
"  %bind B2 %link J#A/B[2];\n"+
"  %bind C2 %link J#A/C[2];\n"+
"  %bind X2 %link J#B/X[2];\n"+//210
"  %bind C2 %link J#B/C[2];\n"+
"  %bind X3 %link J#B/X[3];\n"+
"  %bind D2 %link J#C/D[2];\n"+
"</xd:component>\n"+
"  <A>\n"+
"    <B xd:script='?'/>\n"+
"    <C xd:script='*'/>\n"+
"    <B b='string'/>\n"+
"    <C c='string'/>\n"+
"  </A>\n"+//220
"  <B>\n"+
"    <X>string</X>\n"+
"    <C xd:script='*'/>\n"+
"    <X xd:script='*' x='string'/>\n"+
"    <C c='string'/>\n"+
"    <X xx='string'/>\n"+
"  </B>\n"+
"  <C>\n"+
"    string\n"+
"    <D/>\n"+//230
"    string\n"+
"    <D xd:script='1..3'/>\n"+
"    string\n"+
"  </C>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='K' root='A'>\n"+
"<xd:component>%class test.xdef.component.K %link A</xd:component>\n"+
"  <A>\n"+
"      <c xd:script='+' Kod=\"fixed '1'\"\n"+//240
"         Cislo=''\n"+
"         Rok=\"xdatetime('yy');\"/>\n"+
"	   int;\n"+
"      <d xd:script='?' a=\"xdatetime('d.M.y')\"/>\n"+
"  </A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='L' root='L|*'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.L %link L;\n"+//250
"  %class test.xdef.component.L1 %link *\n"+
"</xd:component>\n"+
"  <L>\n"+
"    <xd:any xd:script='ref X'/>\n"+
"  </L>\n"+
"  <xd:any xd:name='X'\n"+
"    xd:script='options moreAttributes, moreElements, moreText'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='M' root='X'>\n"+//260
"<xd:component>%class test.xdef.component.M %link X</xd:component>\n"+
"  <xd:any xd:name='X'\n"+
"     xd:script='options moreAttributes, moreElements, moreText'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='N' root='A'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.N %link A;\n"+
"  %class test.xdef.component.N_Part\n"+
"     implements test.xdef.component.N_i, java.io.Serializable\n"+//270
"         %interface test.xdef.component.N_i %link Part;\n"+
"  %class test.xdef.component.N_Operation\n"+
"         implements test.xdef.component.N_i %link A/Operation;\n"+
"</xd:component>\n"+
"  <A>\n"+
"    <Operation xd:script='ref Part'/>\n"+
"  </A>\n"+
"  <Part One='optional int()' Two='optional string()' />\n"+
"</xd:def>\n"+
"\n"+//280
"<xd:def name = 'O' root = \"A\">\n"+
"    <xd:component>\n"+
"        %class test.xdef.component.O\n"+
"            %link O#A;\n"+
"    </xd:component>\n"+
"    <A>\n"+
"      <xd:choice xd:script='+'>\n"+
"        <B/>\n"+
"        <C/>\n"+
"       int\n"+//290
"      </xd:choice>\n"+
"    </A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='P' root=\"A\">\n"+
"<xd:declaration>\n"+
"  type pflt float(1,6);\n"+
"  type pdat xdatetime('M.d.yyyy');\n"+
"  uniqueSet ppdat{x: pdat;}\n"+
"  uniqueSet ppflt{x: pflt;}\n"+ //300
"</xd:declaration>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.P %link P#A;\n"+
"</xd:component>\n"+
"\n"+
"<A>\n"+
"  <a xd:script='?' a='ppflt.x()' b='ppdat.x()'/>\n"+
"  <b xd:script='+' a='ppflt.x.ID()' b='ppdat.x.ID()'/>\n"+
"  <c xd:script='?' a='ppflt.x.IDREF()' b='ppdat.x.IDREF()'/>\n"+
"  <d xd:script='?' a='ppflt.x.CHKID()' b='ppdat.x.CHKID()'/>\n"+//310
"</A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='X' root='X|Y'\n"+
"  methods='void test.xdef.TestXComponentsGen.setXX(XXNode,String)'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.X %link X;\n"+
"  %class test.xdef.component.X1\n"+
"    extends test.xdef.TestXComponentsGen %link Y;\n"+
"</xd:component>\n"+//320
"   <X>\n"+
"     <A xd:script = '+;'>\n"+
"       <xd:mixed>\n"+
"         <B xd:script = '*'>\n"+
"            <E xd:script = '?;' >string;</E>\n"+
"         </B>\n"+
"         <C xd:script = '*'/>\n"+
"       </xd:mixed>\n"+
"     </A>\n"+
"   </X>\n"+//330
"   <Y>\n"+
"     <A xd:script='*' V=\"fixed '2'\"></A>\n"+
"     string; onTrue setXX('abc');\n"+
"     <B/>\n"+
"  </Y>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def xd:name='Y01' xd:root='Test'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.Y01 %link Test;\n"+//340
"  %interface test.xdef.component.s.Y01Part %link Part;\n"+
"  %class test.xdef.component.s.Y01Operation\n"+
"    implements test.xdef.component.s.Y01Part %link Test/Operation;\n"+
"</xd:component>\n"+
"    <Test>\n"+
"        <Operation xd:script='ref Part;' x='? string();'/>\n"+
"    </Test>\n"+
"    <Part\n"+
"        One = 'optional string()'\n"+
"        Two = 'optional string()'/>\n"+//350
"</xd:def>\n"+
"\n"+
"<xd:def name='Y02' root='Test'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.Y02\n"+
"     implements test.xdef.component.s.Y01Part %link Test\n"+
"</xd:component>\n"+
"  <Test One = 'optional string()' Two = 'optional string()'/>\n"+
"</xd:def>\n"+
"\n"+//360
"<xd:def name='Y03' root='Part'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.Y03 %link Part;\n"+
"  %interface test.xdef.component.Y03i %link Part;\n"+
"  %class test.xdef.component.Y03PartOne %link PartOne;\n"+
"  %class test.xdef.component.Y03PartTwo %link PartTwo\n"+
"</xd:component>\n"+
"  <PartOne One = 'required string()' />\n"+
"  <PartTwo One = 'required int()' />\n"+
"  <Part>\n"+//370
"     <PartOne xd:script='ref PartOne'/>\n"+
"     <PartTwo xd:script='ref PartTwo'/>\n"+
"  </Part>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y04' root='Part'>\n"+
"<xd:component>\n"+ // see %bind Jedna in the fist xd:component declaration!
"   %class test.xdef.component.Y04\n"+
"     extends test.xdef.TestXComponents_Y04\n"+
"       implements java.io.Serializable, Runnable %link Part\n"+//380
"</xd:component>\n"+
"  <Part One='required string()' Two='required string()'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y05' root='B'>\n"+
"  <xd:component>%class test.xdef.component.Y05 %link B</xd:component>\n"+
"  <B One='required string()' Two='required string()'/>\n"+
"</xd:def>\n"+
"<xd:def name='Y05a' root='A'>\n"+
"  <A xd:script=\"ref Y05#B\"/>\n"+//390
"</xd:def>\n"+
"\n"+
"<xd:def name='Y06' root='A'>\n"+
"<xd:component>\n"+
"  %class test.xdef.component.Y06\n"+
"     extends test.xdef.TestXComponents_Y06Container\n"+
"        &lt;test.xdef.TestXComponents_Y06Domain&gt; %link A;\n"+
"  %class test.xdef.component.Y06B\n"+
"     implements test.xdef.TestXComponents_Y06XCDomain\n"+
"        %link  A/B;\n"+//400
"  %bind Domain %with test.xdef.TestXComponents_Y06Container\n"+
"        %link A/B;\n"+
"</xd:component>\n"+
"<A>\n"+
"  <B One='required string()' Two='required string()'/>\n"+
"</A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y07' root='Smlouva'>\n"+
"    <xd:component>\n"+//410
"        %class test.xdef.component.Y07Smlouva\n"+
"            extends test.xdef.TestXComponents_Y07Operation\n"+
"                %link Smlouva;\n"+
"        %class test.xdef.component.Y07ControlId\n"+
"            %link ControlId;\n"+
"        %bind IdFlow %with test.xdef.TestXComponents_Y07Operation\n"+
"            %link Nehoda/@IdFlow;\n"+
"        %bind ControlId %with test.xdef.TestXComponents_Y07Operation\n"+
"            %link Nehoda/ControlId;\n"+
"    </xd:component>\n"+//420
"    <Smlouva xd:script = 'ref Nehoda'>\n"+
"        <Domain One='required string()' Two='required string()'/>\n"+
"    </Smlouva>\n"+
"    <Nehoda IdFlow='required int()'>\n"+
"        <ControlId xd:script='ref ControlId'/>\n"+
"    </Nehoda>\n"+
"    <ControlId IdNeco = 'required int()'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y08' root='A'>\n"+//430
"<xd:component>\n"+
" %class test.xdef.component.Y08\n"+
"    extends test.xdef.TestXComponents_Y08 %link A;\n"+
" %bind IdFlow %with test.xdef.TestXComponents_Y08 %link A/@Id;\n"+
"</xd:component>\n"+
"<A xd:script='ref B'>\n"+
"   <Domain One='required string()' Two='required string()'/>\n"+
"</A>\n"+
"<B Id='required int()'/>\n"+
"</xd:def>\n"+//440
"\n"+
"<xd:def name='Y09' root='A'>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y09 %link A;\n"+
"  </xd:component>\n"+
"  <A x='required string()' y='required string()'>\n"+
"    <xd:any xd:script='occurs 0..1; \n"+
"      options moreAttributes, moreElements, moreText'/>\n"+
"  </A>\n"+
"</xd:def>\n"+//450
"\n"+
"<xd:def name='Y10' root='A'>\n"+
" <xd:component>\n"+
"    %class test.xdef.component.Y10 %link A;\n"+
"    %class test.xdef.component.Y10p %link A/$mixed/a;\n"+
"    %class test.xdef.component.Y10q %link A/$mixed/b;\n"+
"    %bind p %link A/$mixed/a;\n"+
"    %bind q %link A/$mixed/b;\n"+
" </xd:component>\n"+
" <A a='required string()' b='required string()' c=\"XY\">\n"+//460
"   <xd:mixed>\n"+
"      <a x='string()'/>\n"+
"      <b x='string()'/>\n"+
"   </xd:mixed>\n"+
" </A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y11' root='A'>\n"+
"    <xd:component>\n"+
"        %class test.xdef.component.Y11 %link A;\n"+//470
"        %class test.xdef.component.Y11Op %link Op;\n"+
"    </xd:component>\n"+
"    <A>\n"+
"        <B xd:script='occurs 1..'\n"+
"           N ='required string();' I ='required'>\n"+
"            <xd:mixed>\n"+
"                <Ev xd:script='ref Op; occurs *'/>\n"+
"                <Op xd:script='ref Op; occurs *'/>\n"+
"            </xd:mixed>\n"+
"        </B>\n"+//480
"    </A>\n"+
"    <Op N='required string();' M='required;'>\n"+
"      <Co xd:script='occurs 0..'\n"+
"        C='required;' I=\"required enum('Yes', 'No', 'Join', 'Var')\">\n"+
"        <X/>\n"+
"      </Co>\n"+
"      <Y Y='required;'/>\n"+
"    </Op>\n"+
"</xd:def>\n"+
"\n"+//490
"<xd:def name='Y12' root='A|a'>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y12_S %link Actions/S;\n"+
"    %class test.xdef.component.Y12 %link A;\n"+
"    %class test.xdef.component.Y12_B %link A/B;\n"+
"    %class test.xdef.component.Y12_a %link a;\n"+
"  </xd:component>\n"+
"  <A>\n"+
"    <B xd:script='occurs 1..'>\n"+
"      <xd:mixed xd:script='ref Actions'/>\n"+//500
"    </B>\n"+
"  </A>\n"+
"  <xd:mixed name='Actions'>\n"+
"    <S xd:script='occurs *' V='required'/>\n"+
"    <M xd:script='occurs *' V='required'/>\n"+
"    <P xd:script='occurs *' O='required'/>\n"+
"    <Q xd:script='occurs *' O='required'/>\n"+
"  </xd:mixed>\n"+
"  <a>\n"+
"    <xd:sequence script='2'>\n"+//510
"      <xd:mixed>\n"+
"        <b />\n"+
"        <c />\n"+
"        int\n"+
"      </xd:mixed>\n"+
"    </xd:sequence>\n"+
"  </a>\n"+
"</xd:def>\n"+
"<xd:def name='R001'>\n"+
"</xd:def>\n"+//520
"\n"+
"<xd:def name='Y13' root='A'>\n"+
"  <xd:component>\n"+
"    %interface test.xdef.component.Y13C %link C;\n"+
"    %class test.xdef.component.Y13 %link A;\n"+
"    %class test.xdef.component.Y13B %link A/B;\n"+
"  </xd:component>\n"+
"  <A>\n"+
"    <B xd:script='ref C' />\n"+
"  </A>\n"+//530
"  <C a='string'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y14' root='C'>\n"+
"  <xd:component>\n"+
"    %interface test.xdef.component.Y14A %link A;\n"+
"    %interface test.xdef.component.Y14B %link B;\n"+
"    %class test.xdef.component.Y14C %link C;\n"+
"  </xd:component>\n"+
"  <A a='string'/>\n"+//540
"  <B>\n"+
"    <X xd:script='ref A'/>\n"+
"  </B>\n"+
"  <C xd:script='ref B'/>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y15' root='a'>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y15 %link a;\n"+
"  </xd:component>\n"+//550
"  <a>\n"+
"    string\n"+
"    <b xd:script='?'/>\n"+
"    ? string\n"+
"  </a>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y16' xmlns:x='x.int' root='a|c|e'>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y16 %link a;\n"+//560
"    %class test.xdef.component.Y16a %link x:x;\n"+
"    %class test.xdef.component.Y16c %link c;\n"+
"    %class test.xdef.component.Y16d %link y;\n"+
"    %class test.xdef.component.Y16e %link e;\n"+
"    %class test.xdef.component.Y16f %link g;\n"+
"  </xd:component>\n"+
"  <a>\n"+
"    <x:b xd:script='ref x:x'/>\n"+
"  </a>\n"+
"  <x:x y='int'/>\n"+//570
"  <c>\n"+
"    <d xmlns='y.int' xd:script='+; ref y'/>\n"+
"  </c>\n"+
"  <y xmlns='y.int' y='int'/>\n"+
"  <e>\n"+
"    <f xd:script='ref g'/>\n"+
"  </e>\n"+
"  <g y='int'/>\n"+
"</xd:def>\n"+
"\n"+//580
"<xd:def root='a' name='Y17'>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y17 %link a;\n"+
"    %bind b_1 %link a/b[1];\n"+
"    %bind b_2 %link a/b[2];\n"+
"  </xd:component>\n"+
"  <a>\n"+
"    <b a='int'/>\n"+
"    <c/>\n"+
"    <b a='string'/>\n"+//590
"  </a>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def root='A' name='Y18'>\n"+
"   <A a='string()' b='string()'>\n"+
"      <C xd:script = 'ref Y18a#B'\n"+
"        e='optional string()'>\n"+
"          optional string();\n"+
"      </C>\n"+
"   </A>\n"+//600
"   <xd:component>\n"+
"      %class test.xdef.component.Y18 %link A;\n"+
"      %bind x %link A/C/$text;\n"+
"   </xd:component>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y18a'>\n"+
"   <B c='optional string()' d='optional string()'/>\n"+
"   <xd:component>\n"+
"      %class test.xdef.component.Y18a %link B;\n"+//610
"   </xd:component>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def root='A' name='Y19'>\n"+ // should generate warninig XCOMPONENT037
"  <A>\n"+
"     <B>\n"+
"       <B_1>\n"+
"         <C>\n"+
"           <B b='int'/>\n"+
"         </C>\n"+//620
"       </B_1>\n"+
"     </B>\n"+
"  </A>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y19 %link Y19#A;\n"+
"  </xd:component>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def root='A|B|C|D' name='Y20'>\n"+//630
"  <A>\n"+
"    <X xd:script='0..1; ref X'/>\n"+
"  </A>\n"+
"  <X b='? string()' >\n"+
"    <X xd:script='*; ref X'/>\n"+
"  </X>\n"+
"  <B>\n"+
"    <X b='? string()'>\n"+
"		<X xd:script='*; ref B/X'/>\n"+
"	</X>\n"+
"  </B>\n"+//640
"  <C>\n"+
"    <B xd:script='ref Y'/>\n"+
"  </C>\n"+
"  <Y b='? string()' >\n"+
"    <Y xd:script='*; ref Y'/>\n"+
"  </Y>\n"+
"  <D>\n"+
"    <Z xd:script='0..1; ref Z'/>\n"+
"  </D>\n"+
"  <Z b='? string()' >\n"+//650
"    <C>\n"+
"      <Z xd:script='*; ref Z'/>\n"+
"    </C>\n"+
"  </Z>\n"+
"  <xd:component>\n"+
"    %class test.xdef.component.Y20_A %link Y20#A;\n"+
"    %class test.xdef.component.Y20_B %link Y20#B;\n"+
"    %class test.xdef.component.Y20_C %link Y20#C;\n"+
"    %class test.xdef.component.Y20_D %link Y20#D;\n"+
"  </xd:component>\n"+//660
"</xd:def>\n"+
"\n"+
"<xd:def root='A' name='Y21'>\n"+
"\n"+
"<xd:component>\n"+
"  %class test.xdef.component.Y21 %link Y21#A;\n"+
"  %enum test.xdef.component.Y21_enum eType;\n"+
"  %ref %enum test.xdef.TestXComponents_Y21enum eType1;\n"+
"</xd:component>\n"+
"\n"+//670
"<xd:declaration>\n"+
"  type eType enum('x', 'y', 'A1_b', 'z', '_1', 'A1_b2', '$');\n"+
"  type myType eType;\n"+
"  type eType1 enum('a', 'b', 'c');\n"+
"  type extType eType1;\n"+
"</xd:declaration>\n"+
"\n"+
"  <A b='myType;' >\n"+
"    ? myType;\n"+
"    <B xd:script='*' c='eType1;'>myType;</B>\n"+//680
"    ? myType;\n"+
"  </A>\n"+
"</xd:def>\n"+
"\n"+
"<xd:def name='Y22' xd:root ='A'>\n" +
"  <xd:component>%class test.xdef.component.Y22 %link Y22#A;</xd:component>\n" +
" <A xd:script='options ignoreOther'\n" +
"   Creator ='required string()' >\n" +
"   <Transfer xd:script='options ignoreOther'\n" +
"     Sender ='required num(4)' >\n" +//690
"     <DataFiles>\n" +
"       <Directory Path ='required string()' >\n" +
"         <File xd:script='occurs +;'\n" +
"           Name ='required string()' />\n" +
"       </Directory>\n" +
"     </DataFiles>\n" +
"   </Transfer>\n" +
" </A>\n" +
"</xd:def>\n" +
"\n"+//700
"</xd:collection>";

////////////////////////////////////////////////////////////////////////////////

	public TestXComponentsGen() {}

	public static void setXX(XXNode xx, String s) {
		if (xx.getXComponent() != null) {
			((TestXComponentsGen) xx.getXComponent())._XX = s;
		}
	}

	public static void genXC(XXNode xnode) {
		String name = xnode.getXXName();
		TestXComponentsGen xm = (TestXComponentsGen) xnode.getUserObject();
		if ("G".equals(name)) {
			xm._G = xnode.getXComponent();
		} else if ("XXX".equals(name)) {
			xm._X = xnode.getXComponent();
		} else if ("YYY".equals(name)) {
			xm._Y = xnode.getXComponent();
		} else {
			throw new RuntimeException("Unknown element:" + name);
		}
	}

	public String getXX() {return _XX;}
	public final void xSetFlags(final int flags) {_flags |= flags;}
	public final void xClearFlags(final int flags) {_flags &= ~flags;}
	public final boolean xCheckFlags(final int flags) {
		return (flags & _flags) == flags;
	}
	public final int xGetFlags() {return _flags;}

////////////////////////////////////////////////////////////////////////////////
	private final List<XComponent> _YYY = new ArrayList<XComponent>();
	private String _g;
	private XComponent _XXX;

	public String getg() {return _g;}
	public void setg(String x) {_g = x + '_';}
	public XComponent getXXX() {return _XXX;}
	public void setXXX(XComponent x) {_XXX = x;}
	public List<XComponent> listOfYYY() {return _YYY;}
	public void setYYY(List<XComponent> x) {
		_YYY.clear();
		if (x != null) {_YYY.addAll(x);}
	}

	/** Generate XComponents from XDPool.
	 * @param args not used.
	 */
	public static void main(String... args) {
		File f = new File("temp");
		f.mkdir();
		String dir = f.getAbsolutePath().replace('\\', '/');
		if (!dir.endsWith("/")) {
			dir += '/';
		}
		if (!f.isDirectory()) {
			System.err.println('\"' + dir + "\" is not directory");
			return;
		}
		File g = new File("test");
		if (!g.exists() || !g.isDirectory()) {
			g = new File("src/test/java");
			if (!g.isDirectory()) {
				throw new RuntimeException("Test directory is missing");
			}
		}
		String xcDir = g.getAbsolutePath().replace('\\', '/');
		if (!xcDir.endsWith("/")) {
			xcDir += '/';
		}
		// generate XCDPool from sources
		try {
			// force following classes to be compiled!
			TestXComponents_C.class.getClass();
			TestXComponents_Y04.class.getClass();
			TestXComponents_Y06Container.class.getClass();
			TestXComponents_Y06Domain.class.getClass();
			TestXComponents_Y06DomainContainer.class.getClass();
			TestXComponents_Y06XCDomain.class.getClass();
			TestXComponents_Y07Operation.class.getClass();
			String source = new File(xcDir,
				"test/xdef/data/test/TestXComponent_Z.xdef").getAbsolutePath();
			XDPool xp = XDFactory.compileXD(null, XDEF, source);
			// generate from xp the class containing the XDPool
			XDFactory.genXDPoolClass(xp, dir, "test.xdef.component.Pool", null);
			// generate XComponents from xp
			ArrayReporter reporter = GenXComponent.genXComponent(
				xp, dir,"UTF-8", false, false, true);
			// should generate warning XCOMPONENT037 on xdef Y19
			if (reporter.getWarningCount() != 1
				|| !reporter.printToString().contains("W XDEF377")
				|| !reporter.printToString().contains("Y19#A/B/B_1/C/B")) {
				System.err.println("Warning XDEF377 not reported.");
			}
			String msg = FUtils.updateDirectories(
				new File(f, "test/xdef/component"),
				new File(g, "test/xdef/component"),
				null, // all extensions
				true, // delete others
				true); // process subdirectories
			if (msg.isEmpty()) {
				System.out.println("X-component data was not changed");
			} else {
				System.out.println(msg);
				System.out.println("X-component data created");
			}

			FUtils.deleteAll(f, true); // delete temp directory
		} catch (Exception ex) {ex.printStackTrace(System.err);}
	}
}