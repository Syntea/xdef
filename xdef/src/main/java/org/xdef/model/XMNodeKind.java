package org.xdef.model;

/** XMNode kind types.
 * @author trojan
 */
public enum XMNodeKind {
    /** Undefinded. */
    XMUNDEFKIND,
    /** X-definition ID. */
    XMDEFINITION,
    /** Model of XML element ID - reserved. */
    XMDOCUMENT,
    /** Model of XML element ID. */
    XMELEMENT,
    /** Model of Text ID. */
    XMTEXT,
    /** Model of XML processing instruction ID. */
    XMPI,
    /** Model of XML comment ID. */
    XMCOMMENT,
    /** Model of XML attribute ID. */
    XMATTRIBUTE,
    /** Start of the sequence of XML nodes ID.. */
    XMSEQUENCE,
    /** Start of the choice group of XML nodes ID. */
    XMCHOICE,
    /** Start of the mixed group of XML nodes ID. */
    XMMIXED,
    /** End of a sequence of items ID. */
    XMSELECTOR_END,
    /** Kind of XNode XReference */
    XMREFERENCE,
    /** Kind of XNode  XINCLUDE */
    XMINCLUDE;
    public short toShort() {
        switch(this) {
            case XMDEFINITION: return 1;
            case XMDOCUMENT: return 2;
            case XMELEMENT: return 3;
            case XMTEXT: return 4;
            case XMPI: return 5;
            case XMCOMMENT: return 6;
            case XMATTRIBUTE: return 7;
            case XMSEQUENCE: return 8;
            case XMCHOICE: return 9;
            case XMMIXED: return 10;
            case XMSELECTOR_END: return 11;
            case XMREFERENCE: return 12;
            case XMINCLUDE: return 13;
            case XMUNDEFKIND:
            default: return 0;
        }
    }
    public static XMNodeKind fromShort(final short x) {
        switch(x) {
            case 1: return XMDEFINITION;
            case 2: return XMDOCUMENT;
            case 3: return XMELEMENT;
            case 4: return XMTEXT;
            case 5: return XMPI;
            case 6: return XMCOMMENT;
            case 7: return XMATTRIBUTE;
            case 8: return XMSEQUENCE;
            case 9: return XMCHOICE;
            case 10: return XMMIXED;
            case 11: return XMSELECTOR_END;
            case 12: return XMREFERENCE;
            case 13: return XMINCLUDE;
            default: return XMUNDEFKIND;
        }
    }
}