package test.xdutils;

import org.xdef.xml.KXmlUtils;
import org.xdef.util.GenCollection;
import org.xdef.impl.util.gencollection.XDGenCollection;
import java.io.File;
import org.w3c.dom.Element;
import test.XDTester;

/** Test for the XdCollection.
 * @author Vaclav Trojan
 */
public class TestGenCollection extends XDTester {

	public TestGenCollection() {super();}

	@Override
	/** Test */
	public void test() {
		Element el, el1;
		String xdef;
		File file;
		File dataDir = new File(getDataDir(), "genCollection");
		try {
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' root='mtest' >"
+ "<mtest>"
+ " <xd:choice>"
+ "  <adam xd:script = '*'>required string();</adam>"
+ "  <adam2>required string();</adam2>"
+ "  <eve>required int();</eve>"
+ "  <peter>required string();</peter>"
+ " </xd:choice>"
+ "</mtest>"
+ "</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef}, true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			el1 = (Element) el.getChildNodes().item(0).getChildNodes().item(0);
			assertEq("mtest", el1.getNodeName());
			el1 = (Element) el1.getChildNodes().item(0).getChildNodes().item(0);
			assertEq("adam", el1.getNodeName());
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"default 'abc'\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"default 'abc'\"/>\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"default 1\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"default 1\" />\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"optional int; default 1\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"optional int; default 1\" />\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);
			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"'abc'\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"fixed 'abc'\" />\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"fixed 'abc'\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"fixed 'abc'\" />\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);

			xdef =
"<xd:def xmlns:xd='" + _xdNS + "' name='B0'>\n" +
" <A f=\"fixed 1\" />\n" +
"</xd:def>";
			el = GenCollection.genCollection(new String[]{xdef},true,true,true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			assertEq(
"<xd:collection	xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B0'>\n" +
" <A f=\"fixed 1\" />\n" +
"</xd:def>\n" +
"</xd:collection>", el);
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);
			file = new File(dataDir, "test_01.xdef");
			el = GenCollection.genCollection(new File[]{file}, true, true, true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);

			file = new File(dataDir, "multiXdefTest.xdef");
			el = GenCollection.genCollection(new File[]{file}, true, true, true);
			XDGenCollection.chkXdef(KXmlUtils.nodeToString(el, false));
			el1 = GenCollection.genCollection(
				new String[]{KXmlUtils.nodeToString(el)}, true,true,true);
			assertEq(el, el1);
		} catch (Exception ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		runTest();
	}
}