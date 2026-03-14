package construction;

import org.xdef.sys.ArrayReporter;
import org.xdef.xml.KXmlUtils;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.w3c.dom.Element;

public class Priklad2 {

    public static void main(String[] args) throws Exception {
        //cesta k souboru s Xdefinici
        String xdef = "src/construction/Priklad2.xdef";
        //cesta k souboru se vstupnimi daty
        String data = "src/construction/Priklad2.xml";
        //vytvoreni XDPool ("preklad" Xdefinice)
        XDPool xpool = XDFactory.compileXD(null, xdef);
        //vytvoreni objektu XDDocument
        XDDocument xdoc = xpool.createXDDocument();
        // nastaveni kontextu
        xdoc.setXDContext(KXmlUtils.parseXml(data));
        //reporter pro chyby
        ArrayReporter reporter = new ArrayReporter();
        //spusteni validace a ulozeni vysledku
        Element vysledek = xdoc.xcreate("knihy", reporter);
        //otestovani chyb
        if (reporter.errorWarnings()) {
           reporter.printReports(System.err); //vytisteni chyb
        } else {
           //zapis zpracovaneho dokumentu do souboru
           KXmlUtils.writeXml("src/construction/Priklad2_1.xml", vysledek, true, true);
        }
    }

}