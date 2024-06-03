package bugreports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import org.xdef.XDDocument;
import org.xdef.XDFactory;
import org.xdef.XDPool;
import org.xdef.sys.NullReportWriter;
import static org.xdef.sys.STester.runTest;
import test.XDTester;

/**
 * @author Trojan
 */
public class B extends XDTester {
	public B() {super();}

	@Override
	/** Run test and display error information. */
	public void test() {
		try {
//This XML file does not appear to have any style information associated with it.
//The document tree is shown below.
			String xmlSchema =
"<xsd:schema xmlns=\"http://www.loc.gov/MARC21/slim\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.loc.gov/MARC21/slim\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" version=\"1.1\" xml:lang=\"en\">\n" +
"<!--xsd:annotation>\n" +
"<xsd:documentation> MARCXML: The MARC 21 XML Schema Prepared by Corey Keith May 21, 2002 - Version 1.0 - Initial Release ********************************************** Changes. August 4, 2003 - Version 1.1 - Removed import of xml namespace and the use of xml:space=\"preserve\" attributes on the leader and controlfields. Whitespace preservation in these subfields is accomplished by the use of xsd:whiteSpace value=\"preserve\" May 21, 2009 - Version 1.2 - in subfieldcodeDataType the pattern \"[\\da-z!\"#$%&'()*+,-./:;<=>?{}_^`~\\[\\]\\\\]{1}\" changed to: \"[\\dA-Za-z!\"#$%&'()*+,-./:;<=>?{}_^`~\\[\\]\\\\]{1}\" i.e \"A-Z\" added after \"[\\d\" before \"a-z\" to allow upper case. This change is for consistency with the documentation. ************************************************************ This schema supports XML markup of MARC21 records as specified in the MARC documentation (see www.loc.gov). It allows tags with alphabetics and subfield codes that are symbols, neither of which are as yet used in the MARC 21 communications formats, but are allowed by MARC 21 for local data. The schema accommodates all types of MARC 21 records: bibliographic, holdings, bibliographic with embedded holdings, authority, classification, and community information. </xsd:documentation>\n" +
"</xsd:annotation-->\n" +
"<xsd:element name=\"record\" type=\"recordType\" nillable=\"true\" id=\"record.e\">\n" +
"<xsd:annotation>\n" +
"<xsd:documentation>record is a top level container element for all of the field elements which compose the record</xsd:documentation>\n" +
"</xsd:annotation>\n" +
"</xsd:element>\n" +
"<xsd:element name=\"collection\" type=\"collectionType\" nillable=\"true\" id=\"collection.e\">\n" +
"<xsd:annotation>\n" +
"<xsd:documentation>collection is a top level container element for 0 or many records</xsd:documentation>\n" +
"</xsd:annotation>\n" +
"</xsd:element>\n" +
"<xsd:complexType name=\"collectionType\" id=\"collection.ct\">\n" +
"<xsd:sequence minOccurs=\"0\" maxOccurs=\"unbounded\">\n" +
"<xsd:element ref=\"record\"/>\n" +
"</xsd:sequence>\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"</xsd:complexType>\n" +
"<xsd:complexType name=\"recordType\" id=\"record.ct\">\n" +
"<xsd:sequence minOccurs=\"0\">\n" +
"<xsd:element name=\"leader\" type=\"leaderFieldType\"/>\n" +
"<xsd:element name=\"controlfield\" type=\"controlFieldType\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
"<xsd:element name=\"datafield\" type=\"dataFieldType\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
"</xsd:sequence>\n" +
"<xsd:attribute name=\"type\" type=\"recordTypeType\" use=\"optional\"/>\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"</xsd:complexType>\n" +
"<xsd:simpleType name=\"recordTypeType\" id=\"type.st\">\n" +
"<xsd:restriction base=\"xsd:NMTOKEN\">\n" +
"<xsd:enumeration value=\"Bibliographic\"/>\n" +
"<xsd:enumeration value=\"Authority\"/>\n" +
"<xsd:enumeration value=\"Holdings\"/>\n" +
"<xsd:enumeration value=\"Classification\"/>\n" +
"<xsd:enumeration value=\"Community\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:complexType name=\"leaderFieldType\" id=\"leader.ct\">\n" +
"<xsd:annotation>\n" +
"<xsd:documentation>MARC21 Leader, 24 bytes</xsd:documentation>\n" +
"</xsd:annotation>\n" +
"<xsd:simpleContent>\n" +
"<xsd:extension base=\"leaderDataType\">\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"</xsd:extension>\n" +
"</xsd:simpleContent>\n" +
"</xsd:complexType>\n" +
"<xsd:simpleType name=\"leaderDataType\" id=\"leader.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"<xsd:pattern value=\"[\\d ]{5}[\\dA-Za-z ]{1}[\\dA-Za-z]{1}[\\dA-Za-z ]{3}(2| )(2| )[\\d ]{5}[\\dA-Za-z ]{3}(4500| )\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:complexType name=\"controlFieldType\" id=\"controlfield.ct\">\n" +
"<xsd:annotation>\n" +
"<xsd:documentation>MARC21 Fields 001-009</xsd:documentation>\n" +
"</xsd:annotation>\n" +
"<xsd:simpleContent>\n" +
"<xsd:extension base=\"controlDataType\">\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"<xsd:attribute name=\"tag\" type=\"controltagDataType\" use=\"required\"/>\n" +
"</xsd:extension>\n" +
"</xsd:simpleContent>\n" +
"</xsd:complexType>\n" +
"<xsd:simpleType name=\"controlDataType\" id=\"controlfield.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:simpleType name=\"controltagDataType\" id=\"controltag.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"<xsd:pattern value=\"00[1-9A-Za-z]{1}\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:complexType name=\"dataFieldType\" id=\"datafield.ct\">\n" +
"<xsd:annotation>\n" +
"<xsd:documentation>MARC21 Variable Data Fields 010-999</xsd:documentation>\n" +
"</xsd:annotation>\n" +
"<xsd:sequence maxOccurs=\"unbounded\">\n" +
"<xsd:element name=\"subfield\" type=\"subfieldatafieldType\"/>\n" +
"</xsd:sequence>\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"<xsd:attribute name=\"tag\" type=\"tagDataType\" use=\"required\"/>\n" +
"<xsd:attribute name=\"ind1\" type=\"indicatorDataType\" use=\"required\"/>\n" +
"<xsd:attribute name=\"ind2\" type=\"indicatorDataType\" use=\"required\"/>\n" +
"</xsd:complexType>\n" +
"<xsd:simpleType name=\"tagDataType\" id=\"tag.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"<xsd:pattern value=\"(0([1-9A-Z][0-9A-Z])|0([1-9a-z][0-9a-z]))|(([1-9A-Z][0-9A-Z]{2})|([1-9a-z][0-9a-z]{2}))\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:simpleType name=\"indicatorDataType\" id=\"ind.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"<xsd:pattern value=\"[\\da-z ]{1}\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:complexType name=\"subfieldatafieldType\" id=\"subfield.ct\">\n" +
"<xsd:simpleContent>\n" +
"<xsd:extension base=\"subfieldDataType\">\n" +
"<xsd:attribute name=\"id\" type=\"idDataType\" use=\"optional\"/>\n" +
"<xsd:attribute name=\"code\" type=\"subfieldcodeDataType\" use=\"required\"/>\n" +
"</xsd:extension>\n" +
"</xsd:simpleContent>\n" +
"</xsd:complexType>\n" +
"<xsd:simpleType name=\"subfieldDataType\" id=\"subfield.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:simpleType name=\"subfieldcodeDataType\" id=\"code.st\">\n" +
"<xsd:restriction base=\"xsd:string\">\n" +
"<xsd:whiteSpace value=\"preserve\"/>\n" +
"<xsd:pattern value=\"[\\dA-Za-z!&quot;#$%&'()*+,-./:;<=>?{}_^`~\\[\\]\\\\]{1}\"/>\n" +
"<!--xsd:pattern value=\"[\\dA-Za-z!\"#$%&'()*+,-./:;<=>?{}_^`~\\[\\]\\\\]{1}\"/-->\n" +
"<!-- \"A-Z\" added after \"\\d\" May 21, 2009  -->\n" +
"</xsd:restriction>\n" +
"</xsd:simpleType>\n" +
"<xsd:simpleType name=\"idDataType\" id=\"id.st\">\n" +
"<xsd:restriction base=\"xsd:ID\"/>\n" +
"</xsd:simpleType>\n" +
"</xsd:schema>";
			File f = null;
			for (char c = 'C'; c <= 'Z'; c++) {
				f = new File(c + ":/C1/tempx/Y");
				if (f.exists() && f.isDirectory()) {
					break;
				}
				f = null;
			}
			if (f == null) {
				throw new RuntimeException("Can't find tempx on flash");
			}
			f = new File(f, "Test1a.xdef");
//			File schemaFile = new File(dir, "x.xsd");
//			FUtils.writeString(schemaFile, xmlSchema);
//			XsdToXdef.genCollection(schemaFile.getAbsolutePath(),
//				f.getAbsolutePath(), null, null);
			if (!f.exists() || !f.isFile()) {
				throw new RuntimeException("Can't find Test1a.xdef on flash");
			}
			XDPool xp = XDFactory.compileXD(null, f);
//			xp.displayCode();
//if(true)return;
			f = null;
			for (char c = 'C'; c <= 'Z'; c++) {
				f = new File(
					c + ":/C1/Downloads/dnb_all_dnbmarc_20240213-3.mrc.xml.gz");
				if (f.exists() && f.isFile()) {
					break;
				}
				f = null;
			}
			if (f == null) {
				throw new RuntimeException("Can't find archive file. on flash");
			}
			try (GZIPInputStream in =
				new GZIPInputStream(new FileInputStream(f))) {
				XDDocument xd = xp.createXDDocument();
				xd.xparse(in, new NullReportWriter(true));
			}
		} catch (IOException | RuntimeException ex) {fail(ex);}
	}

	/** Run test
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		XDTester.setFulltestMode(true);
		if (runTest(args) > 0) {System.exit(1);}
	}
}
