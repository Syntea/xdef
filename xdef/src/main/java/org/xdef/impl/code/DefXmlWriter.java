package org.xdef.impl.code;

import org.xdef.msg.SYS;
import org.xdef.sys.SDatetime;
import org.xdef.sys.SDuration;
import org.xdef.sys.SIllegalArgumentException;
import org.xdef.sys.SUnsupportedOperationException;
import org.xdef.xml.KXmlOutStream;
import org.xdef.XDParseResult;
import org.xdef.XDResultSet;
import org.xdef.XDService;
import org.xdef.XDStatement;
import org.xdef.XDValue;
import org.xdef.XDXmlOutStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xdef.XDContainer;
import org.xdef.XDValueType;
import java.math.BigInteger;

/** Provides incremental writing of XML data to a data stream.
 * @author Vaclav Trojan
 */
public class DefXmlWriter extends KXmlOutStream
	implements XDXmlOutStream, XDValue {

	/** Creates new instance of DefXmlOutStream with java.io.Writer
	 * @param writer where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 */
	public DefXmlWriter(final Writer writer,
		final String encoding,
		final boolean writeDocumentHeader) {
		super(writer, encoding, writeDocumentHeader);
	}

	/** Creates new instance of DefXmlOutStream with java.io.OutputStream.
	 * @param out where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @throws IOException if an error occurs.
	 */
	public DefXmlWriter(final OutputStream out,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		super(out, encoding, writeDocumentHeader);
	}

	/** Creates new instance of DefXmlOutStream with the name of output file.
	 * If the file already exists it is deleted. The file will be created
	 * only if something was written.
	 * @param filename the name of file where to write XML.
	 * @param encoding encoding of XML stream.
	 * @param writeDocumentHeader if <tt>true</tt> then the XML header is
	 * written, otherwise no XML header is written.
	 * @throws IOException if an error occurs.
	 */
	public DefXmlWriter(final String filename,
		final String encoding,
		final boolean writeDocumentHeader) throws IOException {
		super(filename, encoding, writeDocumentHeader);
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of CodeItem
////////////////////////////////////////////////////////////////////////////////

	@Override
	/** This method is used internally only in the code interpreter -
	 * do not override this method!.
	 * Set result type of operation (if this is an operation it makes nothing).
	 * @param type id of type.
	 */
	public void setItemType(final short type) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setItemType(short)", getClass().getName());
	}

	@Override
	/** This method is used internally only in the code interpreter -
	 * do not override this method!.
	 * Set code of an operation (if this is not an instruction it does nothing).
	 * @param code the new code of operation.
	 */
	public void setCode(final short code) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setCode(short)", getClass().getName());
	}

	@Override
	/** This method is used internally only in the code interpreter -
	 * do not override this method!.
	 * Set parameter of operation (if this is an operation it makes nothing).
	 * @param param value of operation parameter.
	 */
	public void setParam(final int param) {
		//Unsupported operation &{0}&{1}{ on }
		throw new SUnsupportedOperationException(SYS.SYS090,
			"setParam(int)", getClass().getName());
	}

////////////////////////////////////////////////////////////////////////////////
// Implementation of XDValue
////////////////////////////////////////////////////////////////////////////////
	@Override
	public short getItemId() {return XD_XMLWRITER;}
	@Override
	public XDValueType getItemType() {return XDValueType.XMLWRITER;}
	@Override
	public boolean equals(final XDValue arg) {return arg == this;}
	@Override
	public int compareTo(final XDValue arg) throws IllegalArgumentException {
		throw new SIllegalArgumentException(SYS.SYS085);//Incomparable arguments
	}
	@Override
	public String stringValue() {return super.toString();}
	@Override
	public char charValue() {return 0;}
	@Override
	public byte byteValue() {return 0;}
	@Override
	public short shortValue() {return 0;}
	@Override
	public int intValue() {return 0;}
	@Override
	public long longValue() {return 0;}
	@Override
	public float floatValue() {return 0.0f;}
	@Override
	public double doubleValue() {return 0.0;}
	@Override
	public BigDecimal decimalValue() {return null;}
	@Override
	public BigInteger integerValue() {return null;}
	@Override
	public boolean booleanValue() {return false;}
	@Override
	public Node getXMLNode() {return null;}
	@Override
	public Element getElement() {return null;}
	@Override
	public SDatetime datetimeValue() {return null;}
	@Override
	public SDuration durationValue() {return null;}
	@Override
	public byte[] getBytes() {return null;}
	@Override
	public XDContainer contextValue() {return null;}
	@Override
	public XDService serviceValue() {return null;}
	@Override
	public XDStatement statementValue() {return null;}
	@Override
	public XDResultSet resultSetValue() {return null;}
	@Override
	public XDParseResult parseResultValue() {return null;}
	@Override
	/** Check if the object is <tt>null</tt>.
	 * @return <tt>true</tt> if the object is <tt>null</tt> otherwise returns
	 * <tt>false</tt>.
	 */
	public boolean isNull() { return false;}
	@Override
	public Object getObject() {return this;}
////////////////////////////////////////////////////////////////////////////////
// Methods used in XD processor for internal code - DO NOT IMPLEMENT!
////////////////////////////////////////////////////////////////////////////////
	@Override
	public short getCode() {return CodeTable.LD_CONST;}
	@Override
	public int getParam() {return 0;}
	@Override
	public XDValue cloneItem() {return this;}
	@Override
	public String toString() {return stringValue();}
}