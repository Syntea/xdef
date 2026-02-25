package org.xdef;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import org.xdef.sys.SException;
import org.xdef.sys.SUtils;

/** Get XDPool from the class created from XDPool.
 * @author Trojan
 */
public class XDPoolFromClass {

    /** Get XDPool from the class created from XDPool.
     * @param cls class created from XDPool object.
     * @return XDPool created from argument
     */
    public static final XDPool getXDPool(final Class<?> cls) {
        String className = cls.getName();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 1; ; i++) {
            try {
                Class<?> c = ClassLoader.getSystemClassLoader().loadClass(className +"$B" + i);
                if (c != null) {
                    Field y = c.getDeclaredField("b");
                    y.setAccessible(true);
                    Object o = y.get(null);
                    baos.write(SUtils.decodeBase64(((String) o).toCharArray()));
                }
            } catch (ClassNotFoundException ex) {
                try {
                    return (XDPool) new ObjectInputStream(// all internal class processed, read XDPool
                        new ByteArrayInputStream(baos.toByteArray())).readObject();
                } catch (ClassNotFoundException | IOException ex1) {
                    throw new RuntimeException("Can't create XDPool", ex1);
                }
            } catch (IOException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException
                | SecurityException | SException ex) {
                throw new RuntimeException("Can't create XDPool", ex);
            }
        }
    }
}