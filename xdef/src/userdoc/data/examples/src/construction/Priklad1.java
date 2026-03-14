package construction;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Priklad1 {

    public static void main(String[] args) throws Exception {
        // soubor s Xdefinici
        String xdef= "src/construction/Priklad1.xdef";
        // soubor se vstupnimi daty
        String data= "src/construction/Priklad1.xml";
        //vytvoreni XDPool ("preklad" Xdefinice)
        XDPool xpool = XDFactory.compileXD(null, xdef);
        //vytvoreni objektu XDDocument
        XDDocument xdoc = xpool. createXDDocument("Knihy");
        // nastaveni promenne "knihovna"
        xdoc.setVariable("knihovna", "Moje knihovna");
        // reporter pro chyby
        ArrayReporter reporter = new ArrayReporter();
        // spusteni validace a ulozeni vysledku
        Element vysledek = xdoc.xparse(data, reporter);
        // otestovani chyb
        if (reporter.errorWarnings()) {
           reporter.printReports(System.err); //vytisteni chyb
        } else {
           //vytisteni poctu zpracovanych chyb
           System.out.println("Pocet knih: " + xdoc.getVariable("pocet"));
           //zapis zpracovaneho dokumentu do souboru
           KXmlUtils.writeXml("src/construction/Priklad1_1.xml",
               vysledek, true, true);
        }
    }
}