<?xml version="1.0" encoding="windows-1250"?>

<!--  ver: 2.3.0.2 date: 04.05.2005  -->
<!--  ver: 2.3.0.3 date: 22.05.2008 @CisloDokladuPojisteni optional -->

<xdef:def
   xmlns:xdef = "http://www.syntea.cz/xdef/2.0"
   xdef:name  = "ChybnyPSP"
   xdef:root  = "ChybnyPSP">

   <ChybnyPSP
      IdentZaznamu            = "required string(1,12)"
      CisloSmlouvy            = "required string(1,35)"
      PoradiVozidla           = "required num(1,10)"
      CisloDokladuPojisteni   = "optional string(1,35)"
      Operace                 = "required string(1,20)" >

      <Chyby                  xdef:script = "occurs 0..1; ref Chyby" />
   </ChybnyPSP>

   <Chyby>
      <Chyba                  xdef:script="occurs 1..; ref Chyba" />
   </Chyby>

   <Chyba
      KodChyby                = "required num(4)"
      Typ                     = "required string(1)"
      Xpath                   = "optional string(1,128)"
      Hodnota                 = "optional string(1,255)"
      Popis                   = "optional string(1,255)" />

</xdef:def>
