Popis a ucel teto vetve
=======================

Tato vetev ukazuje, jak zmodernizovat unit-testy, a to pomoci knihovny "TestNG".
Pro logovani se pouziva knihovna "logback".

Predelane pomocne tridy:
  * test.utils
    * Assert.java - rozsireni testNG-assert o nova porovnani
    * Assertion.java - rozsireni testNG-assert o nova porovnani
    * SoftAssert.java - rozsireni testNG-assert o nova porovnani
    * TestUtil.java - pomocne nase metody
    * STester.java - jen pridana metoda runUnitTest(), aby se kazda puvodni unit-test-trida tvarila jako samostatny test
    * XDTesterNT.java - cca plne zmodernizovana XDTester.java

Priklady zmodernizovanych testu:
  * test.xdef.TestKeyAndRefNT.java

Konfigurace:
  * pom.xml - pluginy maven-surefire-plugin
  * src/test/resources/
    * testng.xml - konfigurace TestNG
    * logback-test.xml - konfigurace logback pro unit-testy

