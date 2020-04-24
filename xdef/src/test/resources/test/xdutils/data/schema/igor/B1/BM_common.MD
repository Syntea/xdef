<?xml version="1.0" encoding="windows-1250"?>

<!-- 21.12.2006 upraveny nazvy atributu chyby -->
<!-- 28.12.2006 doplneni blokace a uvolneni PSP -->
<!-- 02.01.2007 zmena nazvu atributu chyby Kod -> KodChyby -->
<!-- 26.01.2007 pridan xd:script="options ..." pro Nositele a Smlouvu  -->
<!-- 15.05.2007 pridana KlasifikovanaUdalost -->
<!-- 15.05.2007 pridan  CilovyPSP -->
<!-- 17.05.2007 Odestranen chybny atribut Mode -->
<!-- 18.05.2007 Docasne pridan atribut Mode kvuli ladeni -->
<!-- 02.05.2008 Odstranen MultiPrenos, v PrenosPSP alternativne ZdrojSml/PSP s opakovanim -->
<!-- 22.05.2008 Odstranen ZdrojovyPSP@CisloDokladuPojisteni -->
<!-- 26.05.2008 v PSP pridany atributy StavSmlouvy a AltIdentPSP -->
<!-- 21.07.2008 v Get_NositelPSP pridan CilovyPSP -->

<xd:def
   xmlns:xd     = "http://www.syntea.cz/xdef/2.0"
   xd:name      = "BM_common"
   impl-version = "2.5.4.0"
   impl-date    = "02.05.2008" >

   <PSP
      ORD                     = "optional int(-9999,9999)"
      InfoDatum               = "required datetime('yyyyMMdd')"
      KodPojistitele          = "required num(4)"
      CisloSmlouvy            = "required string(1,35)"
      PoradiVozidla           = "required num(1,10)"
      StavSmlouvy             = "required enum('T','U')"
      Blokace                 = "optional num(4)"
      AltIdentPSP             = "optional enum('A')" >

      <UsekPojisteni          xd:script = "occurs 1..; ref UsekPojisteni" />
   </PSP>

   <UsekPojisteni
      KodPojistitele          = "required num(4)"
      CisloSmlouvy            = "required string(1,35)"
      PoradiVozidla           = "required num(1,10)"
      DruhVozidla             = "required string(1,2)"
      UsekPojisteniOd         = "required datetime('yyyyMMdd')"
      UsekPojisteniDo         = "optional datetime('yyyyMMdd')">

      <RozhodnaUdalost        xd:script="occurs 0..; ref RozhodnaUdalost" />
   </UsekPojisteni>

   <RozhodnaUdalost
      Rezerva                 = "required int(0,999999999)"
      Vyplaceno               = "required int(0,999999999)"
      Regres                  = "required int(0,999999999)"
      DatumCasSU              = "required datetime('yyyyMMddHHmm')"
      KlasifikovanaUdalost    = "required enum('A','N')"
   />

   <ZdrojovyPSP
      KodPojistitele          = "required string(1,4)"
      CisloSmlouvy            = "required string(1,35)"
      PoradiVozidla           = "required num(1,10)">

      <PSP                    xd:script = "occurs 1; ref PSP" />
   </ZdrojovyPSP>

<!-- Pozor musí se přidat k již existujícímu BM_common pro dávkové zpracování -->

  <AnswerId
         Program           ="required string()"
         Status            ="required enum('OK', 'Pending', 'Fault')"
  />
<!--     Mode              ="optional enum('STD','TST')"	-->

  <Fault>
     <Detail>
         <Chyba            xd:script="occurs 1.."
              KodChyby     ="required int()"
              Typ          ="required string(1)"
              Xpath        ="optional string()"
              Hodnota      ="optional string()"
              Popis        ="optional string()"
         />
     </Detail>
  </Fault>

  <BlokujPSP>
     <PSP                  xd:script="occurs 1; ref PSP" />
  </BlokujPSP>

  <Get_NositelPSP>
   <xd:choice>
     <Nositel              xd:script="ref Id_Nositel" />
     <CilovaSmlouva        xd:script="ref Id_Smlouva" />
     <CilovyPSP            xd:script="ref Id_Smlouva" />
   </xd:choice>
  </Get_NositelPSP>

  <Get_PSP                 xd:script="ref Id_Smlouva" />

  <Get_SmlouvyPSP          xd:script="ref Id_Smlouva" />

  <Get_VolnePSP>
   <xd:choice>
     <Nositel              xd:script="ref Id_Nositel"
         InfoDatum         ="required datetime('yyyyMMdd')">
     </Nositel>
     <CilovaSmlouva        xd:script="ref Id_Smlouva" />
     <CilovyPSP            xd:script="ref Id_Smlouva" />
   </xd:choice>
  </Get_VolnePSP>

  <Id_Nositel              xd:script="options setAttrUpperCase"
         Jmeno             ="optional string()"
         Prijmeni          ="optional string()"
         RC                ="optional num()"
         DatumNarozeni     ="optional datetime('yyyyMMdd')"
         IdStatPrislusnost ="optional int()"
         NazevFirmy        ="optional string()"
         IC                ="optional int()"
  />

  <Id_Smlouva              xd:script="options setAttrUpperCase"
         IdPojistitel      ="required int()"
         CisloSmlouvy      ="required string()"
         PoradiVozidla     ="required num()"
  />

  <NositelPSP
         AddInfo           ="optional enum('0','1')">
    <PSP                   xd:script="occurs 0..; ref PSP" />
  </NositelPSP>

  <PingStatus>
   <xd:mixed>
      <Program             xd:script="occurs 1.."
         Name              ="optional string()"
         Version           ="optional string()"
         InstallationDate  ="optional datetime('d.M.y H:m')"
      />
      <Db                  xd:script="occurs 1.."
         Part              ="optional string()"
         Version           ="optional string()"
         InstallationDate  ="optional datetime('d.M.y H:m')"
      />
   </xd:mixed>
  </PingStatus>

  <RequestId
         IdPartner         ="required int()"
         IdentZpravy       ="required int()"
         RefMsgID          ="optional int()"
  />
<!--     Mode              ="optional enum('STD','TST')"	-->

  <Set_BlokujPSP
         IdPojistitel      ="required int()">
    <ZdrojovaSmlouva       xd:script="occurs 1; ref Id_Smlouva" />
  </Set_BlokujPSP>


  <Set_PrenosPSP
         IdPojistitel      ="required int()"
         CisloSmlouvy      ="required string()"
         PoradiVozidla     ="required num()" >
    <xd:choice>
      <ZdrojovaSmlouva       xd:script="occurs 1..; ref Id_Smlouva" />
      <ZdrojovyPSP           xd:script="occurs 1..; ref Id_Smlouva" />
    </xd:choice>
  </Set_PrenosPSP>


  <Set_UvolniPSP
         IdPojistitel      ="required int()">
    <ZdrojovaSmlouva       xd:script="occurs 1; ref Id_Smlouva" />
  </Set_UvolniPSP>

  <SmlouvyPSP>
    <PSP                   xd:script="occurs 0..; ref PSP" />
  </SmlouvyPSP>

  <UvolniPSP>
     <PSP                  xd:script="occurs 1; ref PSP" />
  </UvolniPSP>

  <VolnePSP
        AddInfo            ="optional enum('0', '1')" >
    <PSP                   xd:script="occurs 0..; ref PSP" />
  </VolnePSP>

</xd:def>
