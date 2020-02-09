package mytest.xdef;

import org.xdef.sys.FUtils;
import org.xdef.util.XdefToXsd;
import java.io.File;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXParseException;
import buildtools.XDTester;

/** Test of error reporting (in combination of CDATA section and text).
 * @author Vaclav Trojan
 */
public class MyTestSchema extends XDTester {

	public MyTestSchema() {
		super();
/*#if DEBUG*/
//		setChkSyntax(true);
		setGenObjFile(true);
/*#end*/
	}

	@Override
	public void test() {
		String tempDir = getTempDir();
System.out.println(tempDir);
		String xml;
		String xdef;
		File xdefFile;
		try {
			xdef =
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:def name='B' root='B'>\n" +
" <xd:declaration scope='local'>\n" +
"   type k j;\n"+
" </xd:declaration>\n" +
" <xd:declaration scope='local'>\n" +
"   type i int(1,10);\n"+
"   type j i;\n"+
" </xd:declaration>\n" +
" <B b='? k'>? j;</B>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xdefFile = new File(tempDir+"B.xdef");
			FUtils.writeString(xdefFile, xdef, "UTF-8");
			XdefToXsd.main("-i", xdefFile.getAbsolutePath(),
				"-o", tempDir,
				"-x", "B");
			SchemaFactory schemaFactory =
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = schemaFactory.newSchema(new File(tempDir+"B.xsd"));
			Validator validator = schema.newValidator();
			xml = // valid
"<B xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
"   xsi:noNamespaceSchemaLocation='B.xsd'\n" +
"   b='1'>1</B>";
			File xmlb = new File(tempDir+"B.xml");
			FUtils.writeString(xmlb, xml, "UTF-8");
			validator.validate(new StreamSource(xmlb));
			xml = // invalid
"<B xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
"   xsi:noNamespaceSchemaLocation='B.xsd'\n" +
"   b='0'>0</B>";
			File xmlb1 = new File(tempDir+"B1.xml");
			FUtils.writeString(xmlb1, xml, "UTF-8");
			try {
				validator.validate(new StreamSource(xmlb1));
				fail("error not recognized");
			} catch (SAXParseException ex) {}
		} catch (Exception ex) {fail(ex);}
		try {
			xdef = 
"<xd:collection xmlns:xd='" + _xdNS + "'>\n" +
"<xd:declaration>\n" +
"   type i int(-1,0);\n"+
"   type j float(-3.0,-2.0);\n"+
"</xd:declaration>\n" +
"<xd:def name='C' >\n" +
" <Y x='i'>j;</Y>\n" +
"</xd:def>\n" +
"<xd:def name='A' root='X|Y|Z'>\n" +
" <xd:declaration scope='local'>\n" +
"   type i int(1,10);\n"+
"   type j float(20.0,30.0);\n"+
" </xd:declaration>\n" +
" <X x='i'>j;<B xd:script='ref C#Y'/></X>\n" +
" <Y x='i'>j</Y>\n" +
" <Z xd:script='ref C#Y'/>\n" +
"</xd:def>\n" +
"</xd:collection>";
			xdefFile = new File(tempDir+"xd.xdef");
			FUtils.writeString(xdefFile, xdef, "UTF-8");
			XdefToXsd.main(
				"-i", xdefFile.getAbsolutePath(),
				"-o", tempDir,
				"-x", "A");
			SchemaFactory schemaFactory =
				SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema schema = schemaFactory.newSchema(new File(tempDir+"A.xsd"));
			xml =
"<X xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
"   xsi:noNamespaceSchemaLocation='A.xsd'\n" +
"   x='10'>20.0<B x='-1'>-3.0</B></X>";
			File xmlx = new File(tempDir+"X.xml");
			FUtils.writeString(xmlx, xml, "UTF-8");
			xml =
"<Y xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
"   xsi:noNamespaceSchemaLocation='A.xsd'\n" +
"   x='10'>20.0</Y>";
			File xmly = new File(tempDir+"Y.xml");
			FUtils.writeString(xmly, xml, "UTF-8");
			xml =
"<Z xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'\n" +
"   xsi:noNamespaceSchemaLocation='A.xsd'\n" +
"   x='-1'>-3.0</Z>";
			File xmlz = new File(tempDir+"Z.xml");
			FUtils.writeString(xmlz, xml, "UTF-8");
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(xmlx));
			validator.validate(new StreamSource(xmly));
			validator.validate(new StreamSource(xmlz));
		} catch (Exception ex) {fail(ex);}
//if(T)return;
	}

	// <xs:complexType mixed="true">
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if (runTest(args) > 0) {System.exit(1);}
	}
}
