package task1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.FUtils;

public class Order1b_gen {

    public static void main(String... args) throws IOException {
        FUtils.deleteAndCreateDir("task1/output"); // ensure the directory task1/output exists

        // Compile X-definitions to XDPool
        XDPool xpool = XDFactory.compileXD(null, "src/task1/Order1.xdef");
        
        // Write the XDPool object to the file task1/output/Order1b.xp
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("task1/output/Order1b.xp"))) {
            // Generate data with compiled XDPool
            out.writeObject(xpool);
        }
    }
}