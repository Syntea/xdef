Popis a ucel vetve "dev-newTests-20180906"
==========================================

Tato větev ukazuje, jak zmodernizovat unit-testy, a to pomocí knihovny "TestNG".
Pro logovaní se používá knihovna "logback".

Předělané pomocné třídy:
  * test.utils
    * Assert.java - rozšíření testNG-assert o nová porovnání
    * Assertion.java - rozšíření testNG-assert o nová porovnání
    * SoftAssert.java - rozšíření testNG-assert o nová porovnání
    * TestUtil.java - pomocné naše metody
    * STester.java - jen přidaná metoda runUnitTest(), aby se každá původní
      unit-test-třída tvářila jako samostatný test
    * XDTesterNT.java - cca plně zmodernizovaná XDTester.java

Příklady zmodernizovaných testů:
  * test.xdef.TestKeyAndRefNT.java

Další návrhy na zlepšení:
  * šlo by to ještě lépe pomocí @TestFactory nebo @DataSource, ale to by už
    vyžadovalo mnohem větší přepsání kódu 

Konfigurace:
  * pom.xml - pluginy maven-surefire-plugin
  * src/test/resources/
    * testng.xml - konfigurace TestNG
    * logback-test.xml - konfigurace logback pro unit-testy

