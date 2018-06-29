package test.xdef;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.SDatetime;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestXComponents_B implements XComponent{
  public String _sId;
  public TestXComponents_B() {}
  public TestXComponents_B(XComponent parent, String name, String nsURI, String xPos, String XDPos){
	XD_NodeName=name; XD_NamespaceURI=nsURI;
	XD_XPos=xPos;
	XD_Model=XDPos;
	XD_Object = (XD_Parent=parent)!=null ? parent.xGetObject() : null;
  }
  public TestXComponents_B(XComponent parent, XXNode xx){
	Element el=xx.getElement();
	XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
	XD_XPos=xx.getXPos();
	XD_Model=xx.getXMElement().getXDPosition();
	XD_Object = (XD_Parent=parent)!=null ? parent.xGetObject() : null;
	if (!"E0776C12914E1FC94E1038ECDCE1B6F9".equals(xx.getXMElement().getDigest())) {
	  throw new SRuntimeException(XDEF.XDEF374); //incompatible element model
	}
  }
  public Long getid() {return _id;}
  public SDatetime gettime() {return _time;}
  public Double getnum() {return _num;}
  public String getname() {return _name;}
  public SDatetime getdate() {return _date;}
  public void setid(Long x) {_id = x; _sId= x==null ? null : String.valueOf(x);}
  public void settime(SDatetime x) {_time = x;}
  public void setnum(Double x) {_num = x;}
  public void setname(String x) {_name = x;}
  public void setdate(SDatetime x) {_date = x;}
//<editor-fold defaultstate="collapsed" desc="XComponent interface">
  @Override
  public Element toXml() {return (Element) toXml((Document) null);}
  @Override
  public String xGetNodeName() {return XD_NodeName;}
  @Override
  public String xGetNamespaceURI() {return XD_NamespaceURI;}
  @Override
  public String xGetXPos() {return XD_XPos;}
  @Override
  public void xSetXPos(String xpos) {XD_XPos = xpos;}
  @Override
  public int xGetNodeIndex() {return XD_Index;}
  @Override
  public void xSetNodeIndex(int index) {XD_Index = index;}
  @Override
  public XComponent xGetParent() {return XD_Parent;}
  @Override
  public final Object xGetObject() {return XD_Object;}
  @Override
  public final void xSetObject(final Object obj) {XD_Object = obj;}
  @Override
  public String toString() {return "XComponent: " + xGetModelPosition();}
  @Override
  public String xGetModelPosition() {return XD_Model;}
  @Override
  public int xGetModelIndex() {return -1;}
  @Override
  public void xInit(XComponent p, String name, String ns, String xdPos) {
	XD_Parent=p;
	XD_NodeName=name;
	XD_NamespaceURI=ns;
	XD_Model=xdPos;
  }

  @Override
  public Node toXml(Document doc) {
	Element el;
	if (doc == null) {
	  doc = KXmlUtils.newDocument(XD_NamespaceURI, XD_NodeName, null);
	  el = doc.getDocumentElement();
	} else {
	  el = doc.createElementNS(XD_NamespaceURI, XD_NodeName);
	  if (doc.getDocumentElement() == null) doc.appendChild(el);
	}
	if (getid() != null) el.setAttribute("id", String.valueOf(getid()));
	if (gettime() != null) el.setAttribute("time", gettime().formatDate("HH:mm:ss"));
	if (getnum() != null) el.setAttribute("num", String.valueOf(getnum()));
	if (getname() != null) el.setAttribute("name", getname());
	if (getdate() != null) el.setAttribute("date", getdate().formatDate("yyyy-MM-dd"));
	return el;
  }
  @Override
  public List<XComponent> xGetNodeList() {return new ArrayList<XComponent>();}
  private Long _id;
  private SDatetime _time;
  private Double _num;
  private String _name;
  private SDatetime _date;
  private XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "A";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private String XD_XPos;
  private String XD_Model="B#A";
  @Override
  public void xSetText(XXNode xx, XDParseResult parseResult) {}
  @Override
  public void xSetAttr(XXNode xx, XDParseResult parseResult) {
	if (xx.getXMNode().getXDPosition().endsWith("/@num"))
	  setnum(parseResult.getParsedValue().doubleValue());
	else if (xx.getXMNode().getXDPosition().endsWith("/@time"))
	  settime(parseResult.getParsedValue().datetimeValue());
	else if (xx.getXMNode().getXDPosition().endsWith("/@name"))
	  setname(parseResult.getParsedValue().stringValue());
	else if (xx.getXMNode().getXDPosition().endsWith("/@date"))
	  setdate(parseResult.getParsedValue().datetimeValue());
	else setid(parseResult.getParsedValue().longValue());
  }
  @Override
  public XComponent xCreateXChild(XXNode xx) {return null;}
  @Override
  public void xAddXChild(XComponent xc) {}
  @Override
  public void xSetAny(Element el) {}
// </editor-fold>
}