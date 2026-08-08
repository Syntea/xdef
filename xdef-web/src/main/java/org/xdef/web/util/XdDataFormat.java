package org.xdef.web.util;

import java.util.Optional;

/**
 * list of X-definition input data formats
 */
public enum XdDataFormat {
    xml,
    json,
    xon,
    yaml,
    csv,
    ini,
    ;

    /**
     * convert string to {@link XdDataFormat}
     * @param dataFormat    data-format as string
     * @return converted <code>dataFormat</code> to {@link XdDataFormat}, if not exists returns <code>null</code>
     */
    public static XdDataFormat valueOfN(String dataFormat) {
        try {
            return XdDataFormat.valueOf(dataFormat);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * convert string to {@link XdDataFormat}
     * @param dataFormat    data-format as string
     * @param defaultt      default value, if not <code>dataFormat</code> exists
     * @return converted <code>dataFormat</code> to {@link XdDataFormat}, if not exists returns <code>defaultt</code>
     */
    public static XdDataFormat valueOfN(String val, XdDataFormat defaultt) {
        return Optional.ofNullable(XdDataFormat.valueOfN(val))
            .orElse(defaultt)
        ;
    }
}
