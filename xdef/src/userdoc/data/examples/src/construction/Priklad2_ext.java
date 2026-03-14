package construction;

import org.xdef.XDContainer;
import org.xdef.XDFactory;

public class Priklad2_ext {

    public static String getTitle(String isbn) {
        if ("123456".equals(isbn)) {
            return "Dasenka";
        } else if ("65498712".equals(isbn)) {
            return "Zlate tele";
        }
        return null;
    }

    public static XDContainer getAuthors(String isbn) {
        XDContainer result = XDFactory.createXDContainer();
        if ("123456".equals(isbn)) {
            result.addXDItem("Karel Capek");
        } else if ("65498712".equals(isbn)) {
            result.addXDItem("Ilja Ilf");
            result.addXDItem("Jevgenij Petrov");
        }
        return result;
    }

}