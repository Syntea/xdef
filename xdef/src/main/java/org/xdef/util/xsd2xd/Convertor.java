package org.xdef.util.xsd2xd;

import java.io.IOException;
import java.util.Set;
import org.w3c.dom.Document;

/** Describes interface for getting and writing generated Xdefinitions.
 * @author Ilia Alexandrov
 */
public interface Convertor {

	/** Creates collection file with given name.
	 * @param collectionFileName name of collection file.
	 * @throws IOException problems creating file.
	 * @throws IllegalStateException current state does not support this method.
	 */
	void writeCollection(String collectionFileName) throws IOException, IllegalStateException;

	/** Creates collection and prints it to standart output. */
	void printCollection();

	/** Creates directory with given name containing Xdefinition files.
	 * @param directoryName name of directory.
	 * @throws IOException exception during creating file.
	 * @throws IllegalStateException if current state does not support this method.
	 */
	void writeXdefFiles(String directoryName) throws IOException, IllegalStateException;

	/** Returns Document object of collection.
	 * @return Document object of collection.
	 * @throws IllegalStateException if current state does not support this method.
	 */
	Document getCollectionDocument() throws IllegalStateException;

	/** Returns set of Xdefinitions Document objects.
	 * @return set of Xdefinitions Document objects.
	 * @throws IllegalStateException if current state does not support this method.
	 */
	Set getXdefDocuments() throws IllegalStateException;
}