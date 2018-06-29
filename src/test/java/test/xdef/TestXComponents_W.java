package test.xdef;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.xml.KXmlUtils;
import cz.syntea.xdef.component.XComponent;
import cz.syntea.xdef.component.XComponentUtil;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.proc.XXNode;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TestXComponents_W {
	public static class W implements XComponent{
  public W() {}
  public W(XComponent parent, String name, String nsURI, String xPos, String XDPos){
	XD_NodeName=name; XD_NamespaceURI=nsURI;
	XD_XPos=xPos;
	XD_Model=XDPos;
	XD_Object = (XD_Parent=parent)!=null ? parent.xGetObject() : null;
  }
  public W(XComponent parent, XXNode xx){
	Element el=xx.getElement();
	XD_NodeName=el.getNodeName(); XD_NamespaceURI=el.getNamespaceURI();
	XD_XPos=xx.getXPos();
	XD_Model=xx.getXMElement().getXDPosition();
	XD_Object = (XD_Parent=parent)!=null ? parent.xGetObject() : null;
	if (!"33A3CE0B39EBA636A78035489973F975".equals(xx.getXMElement().getDigest())) {
	  throw new SRuntimeException(XDEF.XDEF374); //Incompatible element model
	}
  }
  public String getw() {return _w;}
  public String get$value() {return _$value;}
  public void setw(String x) {_w = x;}
  public void set$value(String x) {_$value = x;}
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
  public int xGetModelIndex() {return 0;}
  @Override
  public Node toXml(Document doc) {
	Element el;
	if (doc == null) {
	  doc = KXmlUtils.newDocument(XD_NamespaceURI, XD_NodeName, null);
	  el = doc.getDocumentElement();
	} else {
	  el = doc.createElementNS(XD_NamespaceURI, XD_NodeName);
	}
	if (getw() != null) el.setAttribute("w", getw());
	for (XComponent x: XD_List == null ? xGetNodeList() : XD_List)
	  el.appendChild(x.toXml(doc));
	XD_List = null;
	return el;
  }
  @Override
  public List<XComponent> xGetNodeList() {
	ArrayList<XComponent> a=new ArrayList<XComponent>();
	if (get$value() != null)
	  XComponentUtil.addText(this, "A#A/W/text()", a, get$value(), _$$value);
	return XD_List = a;
  }
  private String _w;
  private String _$value;
  private char _$$value= (char) -1;
  private XComponent XD_Parent;
  private Object XD_Object;
  private String XD_NodeName = "W";
  private String XD_NamespaceURI;
  private int XD_Index = -1;
  private int XD_ndx;
  private String XD_XPos;
  private List<XComponent> XD_List;
  private String XD_Model="A#A/W";
  @Override
  public void xSetText(XXNode xx, XDParseResult parseResult) {
	_$$value=(char) XD_ndx++;
	set$value(parseResult.getParsedValue().stringValue());
  }
  @Override
  public void xSetAttr(XXNode xx, XDParseResult parseResult) {
	setw(parseResult.getParsedValue().stringValue());
  }
  @Override
  public XComponent xCreateXChild(XXNode xx) {return null;}
  @Override
  public void xAddXChild(XComponent xc) {}
  @Override
  public void xSetAny(Element el) {}
  @Override
  public void xInit(XComponent p, String name, String ns, String xdPos) {
	XD_Parent=p;
	XD_NodeName=name;
	XD_NamespaceURI=ns;
	XD_Model=xdPos;
  }
// </editor-fold>
}
}