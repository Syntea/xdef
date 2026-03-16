package task1;

import java.io.FileOutputStream;
import java.io.IOException;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import java.io.ObjectOutputStream;
import java.util.Properties;
import org.xdef.sys.FUtils;

public class Order1a_gen {

    public static void main(String... args) throws IOException {
        FUtils.deleteAndCreateDir("task1/output"); // ensure the directory task1/output exists
        
        // Compile the XDPool from the X-definition source file
        Properties props = new Properties();
        XDPool xpool = XDFactory.compileXD(props, "src/task1/Order1.xdef");
        
        // Write the XDPool object to the file task1/output/Order1a.xp
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("task1/output/Order1a.xp"))){
            out.writeObject(xpool);
        }
    }
}