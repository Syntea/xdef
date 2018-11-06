package org.xdef.impl.util.conv.xsd.doc;

import org.xdef.sys.SReporter;
import org.xdef.impl.util.conv.Util;
import java.util.Map;

import org.w3c.dom.Document;

/** Represents any XML Schema document.
 * @author Ilia Alexandrov
 */
public abstract class XsdDoc {

	/** Reporter for reporting warnings and errors. */
	protected final SReporter _reporter;
	/** Schema file extension. */
	protected final String _schemaFileExt;
	/** Schema nodes namespace prefix. */
	protected final String _schemaPrefix;

	/** Creates instance of XML Schema document representation.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param schemaFileExt schema files extension.
	 * @param schemaPrefix schema nodes namespace prefix.
	 */
	public XsdDoc(SReporter reporter,String schemaFileExt,
			String schemaPrefix) {
		if (reporter == null) {
			throw new NullPointerException("Given reporter is null!");
		}
		if (schemaFileExt == null) {
			throw new NullPointerException(
				"Given schema file extenson is null");
		}
		if (schemaFileExt.length() == 0) {
			throw new IllegalArgumentException(
				"Given schema file extension is empty");
		}
		_reporter = reporter;
		_schemaFileExt = schemaFileExt;
		if (schemaPrefix != null && schemaPrefix.length() == 0) {
			_schemaPrefix = null;
		} else {
			_schemaPrefix = schemaPrefix;
		}
	}

	/** Creates schema node qualified name according to schema nodes prefix.
	 * @param nodeLocalName schema node local name.
	 * @return schema node qualified name.
	 */
	protected final String getSchemaNodeName(String nodeLocalName) {
		return Util.getNodeQName(_schemaPrefix, nodeLocalName);
	}

	/** Creates full schema file name according to schema files extension.
	 * @param schemaName name of schema.
	 * @return full schema fiel name with extension.
	 */
	protected final String getSchemaFileName(String schemaName) {
		return schemaName + "." + _schemaFileExt;
	}

	/** Gets type constant of schema document version of current implementation.
	 * @return type constant of schema document version.
	 */
	public abstract XsdVersion getVersion();

	/** Gets all generated XML Schema documents map with file name as key and
	 * document as value.
	 * @return map of all generated schema file names (String) and documents.
	 */
	public abstract Map<String, Document> getSchemaDocuments();
}