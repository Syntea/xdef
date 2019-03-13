package org.xdef.impl.util.conv.xd2xsd;

import org.xdef.sys.SReporter;
import org.xdef.impl.util.conv.xd2xsd.xd_2_0.xsd_1_0.Xd_2_0_to_Xsd_1_0;
import org.xdef.impl.util.conv.xd.doc.XdDoc;
import org.xdef.impl.util.conv.xd.doc.XdDoc_2_0;
import org.xdef.impl.util.conv.xsd.doc.XsdVersion;
import java.util.Map;
import org.w3c.dom.Document;

/** Represents any X-definition to XML Schema convertor.
 * @author Ilia Alexandrov
 */
public abstract class Convertor {

	/** Reporter for reporting warnings and errors. */
	protected final SReporter _reporter;

	/** Creates instance of convertor.
	 * @param reporter reporter for reporting warnings and errors.
	 * @throws NullPointerException if given reporter is <tt>null</tt>.
	 */
	public Convertor(SReporter reporter) {
		if (reporter == null) {
			throw new NullPointerException("Given reporter is null!");
		}
		_reporter = reporter;
	}

	/** Returns map of all generated schema documents.
	 * @return map with file name as key and schema document as value of all
	 * generated schemas.
	 */
	public abstract Map<String, Document> getSchemaDocuments();

	/** Gets instance of convertor implementation according to given parameters.
	 * @param xdDoc X-definition document representation.
	 * @param schemaVersion schema document representation.
	 * @param schemaPrefix prefix for schema nodes.
	 * @param reporter reporter for reporting warnings and errors.
	 * @param schemaFileExt extension for schema files.
	 * @return proper implementation of convertor.
	 * @throws IllegalStateException if given combination of X-definition and
	 * schema version is not supported.
	 */
	public static Convertor getConvertor(XdDoc xdDoc,
		XsdVersion schemaVersion,
		String schemaPrefix,
		SReporter reporter,
		String schemaFileExt) {
		XdDoc_2_0 xdDoc_2_0 = (XdDoc_2_0) xdDoc;
		return new Xd_2_0_to_Xsd_1_0(xdDoc_2_0,
			reporter, schemaPrefix, schemaFileExt);
	}
}