package cz.syntea.xdef.xml;

import org.w3c.dom.DocumentType;

/** Interface for creating handlers above KXmlParser.
 * @author Vaclav Trojan
 */
public interface KXmlContentHandler extends KXmlParsedResult {

	/** The method is called after root document element header was parsed. */
	public void documentStart();

	/** This method is called when parser reaches the end of document. */
	public void documentEnd();

	/** Allow the application to resolve external entities.
	 * The Parser will call this method before opening any external entity
	 * except the top-level document entity (including the external DTD subset,
	 * external entities referenced within the DTD, and external entities
	 * referenced within the document element): the application may request that
	 * the parser resolve the entity itself, that it use an alternative URI,
	 * or that it use an entirely different input source.
	 *
	 * Application writers can use this method to redirect external system
	 * identifiers to secure and/or local URIs, to look up public identifiers
	 * in a catalog, or to read an entity from a database or other input
	 * source (including, for example, a dialog box).
	 *
	 * If the system identifier is a URL, the SAX parser must resolve it fully
	 * before reporting it to the application.
	 *
	 * @param pubID The public identifier of the external entity being
	 * referenced, or null if none was supplied.
	 * @param sysID The system identifier of the external entity being
	 * referenced.
	 * @return InputStreamt with input source (or null to request that
	 * the parser to open a regular URI connection to the system identifier.
	 */
	public java.io.InputStream resolveEntityStream(String pubID, String sysID);

	/** This method is called after all attributes of the current element
	 * attribute list was reached. The implementation may check the list of
	 * attributes and to invoke appropriate actions. The method is invoked
	 * when parser reaches the end of the attribute list.
	 * @param parsedElem object with result of parsed element header.
	 */
	public void elementStart(KParsedElement parsedElem);

	/** This method is invoked when parser reached the end of element. */
	public void elementEnd();

	/** New text value of current element parsed.
	 * @param leadingSpaces all leading spaces.
	 * @param text The text value (a string first non space character and with
	 * all ignorable white space characters and unresolved entity references).
	 */
	public void text(String leadingSpaces, String text);

	/** Start of parsing of parsed Entity.
	 * @param name the name of referenced entity.
	 */
	public void entityReference(String name);

	/** Parsing of parsed Entity finished.*/
	public void entityReferenceEnd();

	/** Add the new Comment node to the current element.
	 * @param comment The comment.
	 */
	public void comment(String comment);

	/** Add new CDATASection node to current element.
	 * @param cdata The CDATA contents.
	 */
	public void CDATASection(String cdata);

	/** Add new ProcessingInstruction node to current element.
	 * @param target The target name of processing instruction.
	 * @param data value of data field.
	 */
	public void procInstruction(String target, String data);

	/** This method invoked by XML parser after it was processed DOCTYPE section.
	 * The argument is the DocumentType object containing information about DTD.
	 * @param docType DocumentType object from which it is possible to generate
	 * source form of DTD.
	 */
	public void dtdEnd(DocumentType docType);
	public void declInternalEntity(String name, String value);
	public void declNotation(String name, String publicId, String systemId);
	public void declUnparsedEntity(String name,
		String publicId, String systemId, String notationName);
}
