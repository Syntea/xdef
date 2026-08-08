package org.xdef.web.util;

import java.util.Optional;

/**
 * list of X-definition input data formats
 */
public enum XdDataFormat {
    /** XML */
    xml,
    /** JSON */
    json,
    /** XON - X-definition object notation */
    xon,
    /** YAML - yet anothor markup language */
    yaml,
    /** CSV - comma separated values */
    csv,
    /** INI */
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
     * @param dataFormatS   data-format as string
     * @param defaultt      default value, if not <code>dataFormat</code> exists
     * @return converted <code>dataFormat</code> to {@link XdDataFormat}, if not exists returns <code>defaultt</code>
     */
    public static XdDataFormat valueOfN(String dataFormatS, XdDataFormat defaultt) {
        return Optional.ofNullable(XdDataFormat.valueOfN(dataFormatS))
            .orElse(defaultt)
        ;
    }
}
