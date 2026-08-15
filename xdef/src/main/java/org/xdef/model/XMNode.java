package org.xdef.model;

import org.xdef.sys.SPosition;
import org.xdef.XDPool;
import javax.xml.namespace.QName;

/** General interface of models of objects of X-definitions.
 * @author Vaclav Trojan
 */
public interface XMNode extends XMOccurrence {

    /** Get name of model of the node.
     * @return name of node.
     */
    public String getName();

    /** Get name of node.
     * @return The name of node.
     */
    public String getLocalName();

    /** Get prefix of name.
     * @return prefix of name..
     */
    public String getNamePrefix();

    /** Get namespace URI of model of the node.
     * @return nasmespace URI.
     */
    public String getNSUri();

    /** Get QName of model of the node.
     * @return QName of node.
     */
    public QName getQName();

    /** Get kind of model (see values of constant fields).
     * @return the ID of node.
     */
    public XMNodeKind getKind();

    /** Get XMDefinition assigned to this node.
     * @return root XMdefinition node.
     */
    public XMDefinition getXMDefinition();

    /** Get XDPool.
     * @return XDPool to which this XMDefinition belongs.
     */
    public XDPool getXDPool();

    /** Get source position of this node.
     * @return string with  source position of this node.
     */
    public SPosition getSPosition();

    /** Get position of this node in XDPool.
     * @return string with position of this node in XDPool.
     */
    public String getXDPosition();

    /** Initialize code or -1.
     * @return address of initialize code.
     */
    public int getInitCode();
    /** Finally code or -1.
     * @return address of finally code or -1.
     */
    public int getFinallyCode();
    /** Match code or -1.
     * @return address of match code or -1.
     */
    public int getMatchCode();
    /** Compose action code or -1.
     * @return address of compose action code or -1.
     */
    public int getComposeCode();
    /** Check value of attribute or text node code or -1.
     * @return address of code of check value method or -1.
     */
    public int getCheckCode();
    /** Type check passed code or -1.
     * @return address of check passed method code or -1.
     */
    public int getOnTrueCode();
    /** Type check failed code or -1.
     * @return address of failed method code or -1.
     */
    public int getOnFalseCode();
    /** If text object is missing code or -1.
     * @return address of text object is missing code or -1.
     */
    public int getDefltCode();
    /** On start of element (all source attributes are accessible) code or -1.
     * @return address of on start of element method code or -1
     */
    public int getOnStartElementCode();
    /** OnAbsence code or -1.
     * @return address of onAbsence code or -1.
     */
    public int getOnAbsenceCode();
    /** OnExcess  code or -1.
     * @return address of onExcess  code or -1.
     */
    public int getOnExcessCode();
    /** Occurrence of illegal attribute code or -1.
     * @return address of occurrence of illegal attribute code or -1.
     */
    public int getOnIllegalAttrCode();
    /** Occurrence of illegal text node code or -1.
     * @return address of occurrence of illegal text node code or -1.
     */
    public int getOnIllegalTextCode();
    /** occurrence of illegal element code or -1.
     * @return address of occurrence of illegal element code or -1.
     */
    public int getOnIllegalElementCode();
    /** Variables initialization code or -1.
     * @return address of variables initialization code or -1.
     */
    public int getVarinitCode();
    /** Get occurrence.
     * @return Occurrence of the node.
     */
    public XMOccurrence getOccurence();
}