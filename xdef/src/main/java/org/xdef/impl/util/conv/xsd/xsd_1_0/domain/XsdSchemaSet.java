package org.xdef.impl.util.conv.xsd.xsd_1_0.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Represents set of schemas.
 * @author Ilia Alexandrov
 */
public class XsdSchemaSet implements XsdSchemaContainer {

	/** Main schema.*/
	private final XsdSchema _mainSchema;
	/** Set of schemas (XsdSchema). */
	private final Set<XsdSchema> _extSchemas = new HashSet<XsdSchema>();
	/** Map of schema namespaces (String) mapped to schemas (XsdSchema). */
	private final Map<String, XsdSchema> _namespaces =
		new HashMap<String, XsdSchema>();

	/** Creates instance of schema set with main schema and no secondary schemas.
	 * @param mainSchema schemas set main schema.
	 * @throws NullPointerException if given main schema is <tt>null</tt>.
	 */
	public XsdSchemaSet(XsdSchema mainSchema) {
		if (mainSchema == null) {
			throw new NullPointerException("Given main schema is null!");
		}
		_mainSchema = mainSchema;
	}

	/** Schema set main schema getter.
	 * @return main schema.
	 */
	public XsdSchema getMainSchema() {return _mainSchema;}

	/** External schemas set getter.
	 * @return external schemas (XsdSchema) set.
	 */
	public Set<XsdSchema> getExtSchemas() {return _extSchemas;}

	/** Adds given schema to scheams map.
	 * @param schema schema to add.
	 * @throws NullPointerException if given schema is <tt>null</tt>.
	 */
	public void addSchema(XsdSchema schema) {
		if (schema == null) {
			throw new NullPointerException("Given schema is null!");
		}
		if (_namespaces.containsKey(schema.getTargetNS())) {
			throw new IllegalArgumentException("Schema set already contains external "
					+ "schema with given namespace!");
		}
		_extSchemas.add(schema);
		_namespaces.put(schema.getTargetNS(), schema);
	}

	/** Gets schema from schemas set with given target namespace or <tt>null</tt>
	 * if there is no schema with such target namespace.
	 * @param targetNS target namespace of schema.
	 * @return schema representation.
	 */
	public XsdSchema getSchema(String targetNS) {
		return _namespaces.get(targetNS);
	}
	@Override
	public int getType() {return Type.SCHEMA_SET;}
	@Override
	public String toString() {
		return "XsdSchemaSet[mainSchema='" + _mainSchema.toString() + "', "
				+ "extSchemasCount='" + _extSchemas.size() + "']";
	}
}