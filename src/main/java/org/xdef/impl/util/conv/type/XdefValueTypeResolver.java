package org.xdef.impl.util.conv.type;

import org.xdef.impl.util.conv.Util;
import org.xdef.impl.util.conv.type.domain.Other;
import org.xdef.impl.util.conv.type.domain.ValueType;
import org.xdef.impl.util.conv.type.domain.XdefBase;
import org.xdef.impl.util.conv.type.domain.XdefList;
import org.xdef.impl.util.conv.type.domain.XdefType;
import org.xdef.impl.util.conv.type.domain.XsdBase;
import org.xdef.impl.util.conv.type.domain.XsdFacet;
import org.xdef.impl.util.conv.type.domain.XsdList;
import org.xdef.impl.util.conv.type.domain.XsdRestricted;
import org.xdef.impl.util.conv.type.domain.XsdUnion;
import org.xdef.impl.util.conv.xd.doc.XdDoc_2_0;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdDecl;
import org.xdef.impl.util.conv.xd.xd_2_0.domain.XdModel;
import org.xdef.impl.util.conv.xsd.doc.XsdDoc_1_0;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdNames;
import org.xdef.impl.util.conv.xsd.xsd_1_0.XsdUtils;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdModel;
import org.xdef.impl.util.conv.xsd.xsd_1_0.domain.XsdSType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.w3c.dom.Element;

/** Provides functionality for resolving value type declarations.
 * @author Ilia Alexandrov
 */
public class XdefValueTypeResolver {

	private static final Map<XdefBase, XsdBase> XDEFBASETOXSDBASE =
		new HashMap<XdefBase, XsdBase>();
	private static final Set<XdefBase> UNCONVERTIBLEXDTYPES =
		new HashSet<XdefBase>();

	static {
		XDEFBASETOXSDBASE.put(XdefBase.BASE_64, XsdBase.BASE64_BINARY);
		XDEFBASETOXSDBASE.put(XdefBase.BOOLEAN, XsdBase.BOOLEAN);
		XDEFBASETOXSDBASE.put(XdefBase.ENTITIES, XsdBase.ENTITIES);
		XDEFBASETOXSDBASE.put(XdefBase.ENTITY, XsdBase.ENTITY);
		XDEFBASETOXSDBASE.put(XdefBase.FLOAT, XsdBase.DOUBLE);
		XDEFBASETOXSDBASE.put(XdefBase.HEX, XsdBase.HEX_BINARY);
		XDEFBASETOXSDBASE.put(XdefBase.ID, XsdBase.ID);
		XDEFBASETOXSDBASE.put(XdefBase.IDREF, XsdBase.IDREF);
		XDEFBASETOXSDBASE.put(XdefBase.IDREFS, XsdBase.IDREFS);
		XDEFBASETOXSDBASE.put(XdefBase.INT, XsdBase.LONG);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_DATE, XsdBase.DATE);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_DATE_TIME, XsdBase.DATE_TIME);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_DAY, XsdBase.G_DAY);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_DURATION, XsdBase.DURATION);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_LANGUAGE, XsdBase.LANGUAGE);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_MONTH, XsdBase.G_MONTH);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_MONTH_DAY, XsdBase.G_MONTH_DAY);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_TIME, XsdBase.TIME);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_YEAR, XsdBase.G_YEAR);
		XDEFBASETOXSDBASE.put(XdefBase.ISO_YEAR_MONTH, XsdBase.G_YEAR_MONTH);
		XDEFBASETOXSDBASE.put(XdefBase.NC_NAME, XsdBase.NCNAME);
		XDEFBASETOXSDBASE.put(XdefBase.NM_TOKEN, XsdBase.NMTOKEN);
		XDEFBASETOXSDBASE.put(XdefBase.NM_TOKENS, XsdBase.NMTOKENS);
		XDEFBASETOXSDBASE.put(XdefBase.NORM_STRING, XsdBase.NORMALIZED_STRING);
		XDEFBASETOXSDBASE.put(XdefBase.NOTATION, XsdBase.NOTATION);
		XDEFBASETOXSDBASE.put(XdefBase.Q_NAME, XsdBase.QNAME);
		XDEFBASETOXSDBASE.put(XdefBase.STRING, XsdBase.STRING);
		XDEFBASETOXSDBASE.put(XdefBase.URI, XsdBase.ANY_URI);
		UNCONVERTIBLEXDTYPES.add(XdefBase.EMAIL);
		UNCONVERTIBLEXDTYPES.add(XdefBase.EMAIL_DATE);
		UNCONVERTIBLEXDTYPES.add(XdefBase.EMAIL_LIST);
		UNCONVERTIBLEXDTYPES.add(XdefBase.FILE);
		UNCONVERTIBLEXDTYPES.add(XdefBase.MD5);
		UNCONVERTIBLEXDTYPES.add(XdefBase.NC_NAME_LIST);
		UNCONVERTIBLEXDTYPES.add(XdefBase.NORM_TOKEN);
		UNCONVERTIBLEXDTYPES.add(XdefBase.NORM_TOKENS);
		UNCONVERTIBLEXDTYPES.add(XdefBase.PICTURE);
		UNCONVERTIBLEXDTYPES.add(XdefBase.Q_NAME_LIST);
		UNCONVERTIBLEXDTYPES.add(XdefBase.Q_NAME_LIST_URI);
		UNCONVERTIBLEXDTYPES.add(XdefBase.Q_NAME_URI);
		UNCONVERTIBLEXDTYPES.add(XdefBase.URI_LIST);
		UNCONVERTIBLEXDTYPES.add(XdefBase.URL);
		UNCONVERTIBLEXDTYPES.add(XdefBase.URL_LIST);
	}

	private final XdDoc_2_0  _xdDoc;
	private final XsdDoc_1_0 _xsdDoc;

	/** XdModel as key and XsdModel as value model map. */
	private final Map<XdModel, XsdModel> _xdModelXsdModelMap;

	/** Creates instance of type resolver.
	 * @param xdDoc X-definition document representation.
	 * @param xsdDoc schema document representation.
	 * @param xdModelXsdModelMap map of X-definition models mapped to schema models.
	 * @throws NullPointerException if X-definition document, schema document
	 * or models map is <tt>null</tt>.
	 */
	public XdefValueTypeResolver(XdDoc_2_0 xdDoc,
		XsdDoc_1_0 xsdDoc,
		Map<XdModel, XsdModel> xdModelXsdModelMap) {
		if (xdDoc == null) {
			throw new NullPointerException("X-definition document is null");
		}
		if (xdModelXsdModelMap == null) {
			throw new NullPointerException("Given models map is null");
		}
		_xdDoc              = xdDoc;
		_xsdDoc             = xsdDoc;
		_xdModelXsdModelMap = xdModelXsdModelMap;
	}

	/** Get qualified name of given type.
	 * @param type type to get qualified name of.
	 * @param contextElem context element (needed only for OtherType).
	 * @return qualified name of given type or <tt>null</tt>.
	 */
	private String getTypeQName(ValueType type, Element contextElem) {
		switch (type.getKind()) {
			case ValueType.XDEF_TYPE:
				return getXdefTypeQName((XdefType) type);
			case ValueType.XDEF_LIST:
				return getXdefListQName((XdefList) type);
			case ValueType.SCHEMA_BASE:
				return getXsdBaseQName((XsdBase) type);
			case ValueType.SCHEMA_RESTRICTION:
				return getXsdTypeQName((XsdRestricted) type);
			case ValueType.OTHER:
				return getOtherTypeQName((Other) type, contextElem);
		}
		return null;
	}

	/** Get X-definition <code>ListOf</code> type qualified name if possible.
	 * If type contains delimeter, and it is not space returns schema
	 * <code>string</code> qualified name.
	 * @param xdefList X-definition <code>ListOf</code> type object.
	 * @return schema <code>string</code> type qualified name or null.
	 */
	private String getXdefListQName(XdefList xdefList) {
		if (xdefList.getDelimeter() != null
			&& !xdefList.getDelimeter().equals(" ")) {
			return _xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName());
		}
		return null;
	}

	/** Returns qualified name of given X-definition type if type can be
	 * declared as string reference.
	 * @param xdefType X-definition type to get qualified name.
	 * @return qualified name or <tt>null</tt>.
	 */
	private String getXdefTypeQName(XdefType xdefType) {
		XdefBase base = xdefType.getBase();
		if (UNCONVERTIBLEXDTYPES.contains(base)) {
			return _xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName());
		}
		XsdBase simpleXsdTypeEnum;
		if (xdefType.getParams().isEmpty()
			&& (simpleXsdTypeEnum=(XsdBase)XDEFBASETOXSDBASE.get(base))!=null){
			return _xsdDoc.getSchemaTypeQName(simpleXsdTypeEnum.getName());
		}
		return null;
	}

	/** Returns given schema base type qualified name.
	 * @param xsdBase schema base type object.
	 * @return schema base type qualified name.
	 */
	private String getXsdBaseQName(XsdBase xsdBase) {
		return _xsdDoc.getSchemaTypeQName(xsdBase.getName());
	}

	/** Returns qualified name of given schema type if it is possible.
	 * @param xsdType schema type to get qualified name of.
	 * @return type quailifed name or <tt>null</tt>.
	 */
	private String getXsdTypeQName(XsdRestricted xsdType) {
		if (xsdType.getBase() == null && xsdType.getEnumerations().isEmpty()
			&& xsdType.getPatterns().isEmpty()
			&& xsdType.getFractionDigits() == null
			&& xsdType.getLength() == null
			&& xsdType.getMaxExclusive() == null
			&& xsdType.getMaxInclusive() == null
			&& xsdType.getMaxLength() == null
			&& xsdType.getMinExclusive() == null
			&& xsdType.getMinInclusive() == null
			&& xsdType.getMinLength() == null
			&& xsdType.getTotalDigits() == null
			&& xsdType.getWhiteSpace() == null) {
			return _xsdDoc.getSchemaTypeQName(xsdType.getXdefBase().getName());
		}
		return null;
	}

	/** Gets qualified name of given other type. If it is reference to declaration
	 * returns qualified name of referred declaration.
	 * @param otherType other type to get qualified name of.
	 * @param contextElem schema context element.
	 * @return other type's qualified name.
	 */
	private String getOtherTypeQName(Other otherType, Element contextElem) {
		XdDecl xdDecl = getXdDecl(otherType);
		if (xdDecl == null) {
			String xdname;
			if (otherType.isSimple() && (xdname=otherType.getXdefName())!=null) {
				String typename = otherType.getName();
				xdDecl = _xdDoc.getXdDecl('_'+xdname+'.' + typename);
			}
		}
		if (xdDecl != null) {
			XsdModel model = (XsdModel) _xdModelXsdModelMap.get(xdDecl);
			Element schema = XsdUtils.getAncestorSchema(contextElem);
			return _xsdDoc.getQName(schema, model.getSchema(), model.getName());
		}
		return _xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName());

	}

	/** Returns X-definition declaration model if given type is reference to
	 * declaration.
	 * @param type type to test.
	 * @return declaration model or <tt>null</tt>.
	 */
	public XdDecl getXdDecl(ValueType type) {
		if (ValueType.OTHER == type.getKind()) {
			Other otherType = (Other) type;
			String xdname;
			if (otherType.isSimple() && (xdname=otherType.getXdefName())!=null){
				String typename = otherType.getName();
				if (xdname != null) {
					XdDecl xdDecl = _xdDoc.getXdDecl('_'+xdname+'.' + typename);
					if (xdDecl != null) {
						return xdDecl;
					}
				}
				return _xdDoc.getXdDecl(otherType.getName());
			}
		}
		return null;
	}

	/** Creates schema <tt>simpleType</tt> model with given name according
	 * to given type and adds declaration to ancestor schema <tt>schema</tt>
	 * element of given context element.
	 * @param type type to add.
	 * @param sTypeName simple type name.
	 * @param contextElem schema context element.
	 */
	public void createSimpleType(ValueType type,
		String sTypeName,
		Element contextElem) {
		Element schemaElem = XsdUtils.getAncestorSchema(contextElem);
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(schemaElem, sTypeName);
		addTypeToSType(type, sTypeElem);
	}

	/** Resolves X-definition declaration model.
	 * @param xdDecl X-definition declaration model to resolve.
	 */
	public void resolveXdDecl(XdDecl xdDecl) {
		Element declElem = (Element) _xdDoc.getXdModels().get(xdDecl);
		String declName = xdDecl.getName();
		String xdName = xdDecl.getDef().getName();
		String localNamePrefix = xdName != null ? '_' + xdName+ '.' : "_.";
		Map<String, String> map = new HashMap<String, String>();
		Util.getDeclaredTypes(declElem, map);
		String typeDecl = map.get(declName); // try local
		if (typeDecl != null) {
			String s = map.get(localNamePrefix + typeDecl); // try local
			if (s == null) {
				s = map.get(typeDecl); // try local
			}
			while (s != null) {
				typeDecl = s;
				s = map.get(localNamePrefix + s); // try local
				if (s == null) {
					s = map.get(s); // try global
				}
			}
			}
/*vtxx*
		String typeDecl = map.get(localNamePrefix + declName); // try local
		if (typeDecl == null) {
			typeDecl = map.get(declName); // global
		}
/*vtxx*/
		if (typeDecl != null) {
			try {
				ValueType type = XdefValueTypeParser.parse(typeDecl);
				if (type.getKind() == ValueType.OTHER) {
					((Other) type).setXdefName(xdName);
				}
				XsdSType model = (XsdSType) _xdModelXsdModelMap.get(xdDecl);
				Element sTypeElem = (Element) _xsdDoc.getModels().get(model);
				addTypeToSType(type, sTypeElem);
			} catch (Exception ex) {
				throw new RuntimeException("Can not parse type",ex);
			}
		}
	}

	/** Resolves attribute type and adds proper declaration to given schema
	 * <tt>attribute</tt> element.
	 * @param typeDecl attribute type declaration string.
	 * @param xdname name of actual X-definition
	 * @param attrElem schema <tt>attribute</tt> element to add declaration to.
	 */
	public void resolveAttrType(final String typeDecl,
		final String xdname,
		final Element attrElem) {
		//type declaration is empty => anySimpleType
		if (typeDecl == null || typeDecl.length() == 0) {
			_xsdDoc.setType(attrElem,
				_xsdDoc.getSchemaTypeQName(XsdNames.ANY_SIMPLE_TYPE));
			return;
		}
		try {
			ValueType parsedType = XdefValueTypeParser.parse(typeDecl);
			if (parsedType.getKind() == ValueType.OTHER) {
				((Other) parsedType).setXdefName(xdname);
			}
			addType(parsedType, attrElem);
		} catch (Exception ex) {
			throw new RuntimeException(
				"Could not parse given type declaration: " + typeDecl, ex);
		}
	}

	/** Adds given type declaration to given schema <tt>element</tt> element.
	 * @param type type string to parse and add.
	 * @param elemElement schema <tt>element</tt> element to add type
	 * declaration to.
	 * @throws RuntimeException if could not parse type string.
	 */
	public void resolveElemType(String type, Element elemElement) {
		ValueType parsedType;
		try {
			parsedType = XdefValueTypeParser.parse(type);
		} catch (Exception ex) {
			throw new RuntimeException("Could not parse given type string", ex);
		}
		resolveElemType(parsedType, elemElement);
	}

	/** Adds given type declaration to given schema <tt>element</tt> element.
	 * @param type type to add.
	 * @param elemElement schema <tt>element</tt> element to add type
	 * declaration to.
	 */
	public void resolveElemType(ValueType type, Element elemElement) {
		addType(type, elemElement);
	}

	/** Adds given type to given schema context element.
	 * @param type type to add.
	 * @param contextElem schema context element to add type to.
	 * @throws IllegalArgumentException if given type is unknown.
	 */
	private void addType(ValueType type, Element contextElem) {
		switch (type.getKind()) {
			case ValueType.XDEF_TYPE:
				addXdefType((XdefType) type, contextElem);
				break;
			case ValueType.XDEF_LIST:
				addXdefList((XdefList) type, contextElem);
				break;
			case ValueType.SCHEMA_BASE:
				addXsdBase((XsdBase) type, contextElem);
				break;
			case ValueType.SCHEMA_RESTRICTION:
				addXsdType((XsdRestricted) type, contextElem);
				break;
			case ValueType.SCHEMA_LIST:
				addXsdList((XsdList) type, contextElem);
				break;
			case ValueType.SCHEMA_UNION:
				addXsdUnion((XsdUnion) type, contextElem);
				break;
			case ValueType.OTHER:
				addOtherType((Other) type, contextElem);
				break;
			default:
				throw new IllegalArgumentException("Given type is unknown");
		}
	}

	/** Adds given type to given schema <tt>simpleType</tt> element.
	 * @param type type to add.
	 * @param sTypeElem schema <tt>simpleType</tt> element to add type to.
	 * @throws IllegalArgumentException if given type is unknown.
	 */
	private void addTypeToSType(ValueType type, Element sTypeElem) {
		switch (type.getKind()) {
			case ValueType.XDEF_TYPE:
				addXdefTypeToSType((XdefType) type, sTypeElem);
				break;
			case ValueType.XDEF_LIST:
				addXdefListToSType((XdefList) type, sTypeElem);
				break;
			case ValueType.SCHEMA_BASE:
				addXsdBaseToSType((XsdBase) type, sTypeElem);
				break;
			case ValueType.SCHEMA_RESTRICTION:
				addXsdTypeToSType((XsdRestricted) type, sTypeElem);
				break;
			case ValueType.SCHEMA_LIST:
				addXsdListToSType((XsdList) type, sTypeElem);
				break;
			case ValueType.SCHEMA_UNION:
				addXsdUnionToSType((XsdUnion) type, sTypeElem);
				break;
			case ValueType.OTHER:
				addOtherTypeToSType((Other) type, sTypeElem);
				break;
			default:
				throw new IllegalArgumentException("Given type is unknown");
		}
	}

	/** Adds X-definition <code>ListOf</code> type to given schema context
	 * element. If type contains unsupported delimeter schema
	 * <code>string</code> type will be added.
	 * @param xdefList X-definition <code>ListOf</code> type object.
	 * @param contextElem context element to add type to.
	 */
	private void addXdefList(XdefList xdefList, Element contextElem) {
		if (xdefList.getDelimeter() != null
			&& !xdefList.getDelimeter().equals(" ")) {
			_xsdDoc.addDocumentation(contextElem, xdefList.getTypeString());
			_xsdDoc.setType(contextElem,
				_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
			return;
		}
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(contextElem, null);
		addXdefListToSType(xdefList, sTypeElem);
	}

	/** Adds given X-definition <code>ListOf</code> type to given schema
	 * <code>simpleType</code> element.
	 * @param xdefList X-definition <code>ListOf</code> type object.
	 * @param sTypeElement schema <code>simpleType</code> element.
	 */
	private void addXdefListToSType(XdefList xdefList, Element sTypeElement) {
		_xsdDoc.addDocumentation(sTypeElement, xdefList.getTypeString());
		String listItemQName = getTypeQName(xdefList.getItem(), sTypeElement);
		if (listItemQName != null) {
			_xsdDoc.addListDecl(sTypeElement, listItemQName);
			return;
		}
		Element listElem = _xsdDoc.addListDecl(sTypeElement, null);
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(listElem, null);
		addTypeToSType(xdefList.getItem(), sTypeElem);
	}

	/** Adds proper schema elements to add given X-definition type to given
	 * schema context element. If it is possible, type will be added as
	 * qualified name to <tt>type</tt> attribute to given schema context element.
	 * @param xdefType X-definition type to add.
	 * @param contextElem schema context element to add declaration to.
	 */
	private void addXdefType(XdefType xdefType, Element contextElem) {
		String qName = getXdefTypeQName(xdefType);
		if (qName != null) {
			_xsdDoc.addDocumentation(contextElem, xdefType.getTypeString());
			_xsdDoc.setType(contextElem, qName);
			return;
		}
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(contextElem, null);
		addXdefTypeToSType(xdefType, sTypeElem);
	}

	/** Adds schema <tt>restriction</tt> element and facet elements to
	 * given <tt>simpleType</tt> element if needed according to given
	 * X-definition type.
	 * @param xdefType X-definition type to add.
	 * @param simpleTypeElem schema <tt>simpleType</tt> element to add
	 * <tt>restriction</tt> element to.
	 */
	private void addXdefTypeToSType(XdefType xdefType, Element simpleTypeElem){
		_xsdDoc.addDocumentation(simpleTypeElem, xdefType.getTypeString());
		XdefBase base = xdefType.getBase();
		int paramCount = xdefType.getParams().size();
		switch (base.getId()) {
			case XdefBase.Id.ALFA_NUMERIC: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				String alfaNumeric = "[A-Za-z0-9]";
				StringBuilder pattern = new StringBuilder();
				if (paramCount == 0) {
					pattern.append(alfaNumeric).append("+");
				} else if (paramCount == 1) {
					pattern.append(alfaNumeric).append("{")
						.append(xdefType.getParams().get(0)).append("}");
				} else if (paramCount == 2) {
					pattern.append(alfaNumeric).append("{")
						.append(xdefType.getParams().get(0)).append(",")
						.append(xdefType.getParams().get(1)).append("}");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
				_xsdDoc.addFacet(restrElem,
					XsdFacet.PATTERN.getXsdName(), pattern.toString());
			}
			break;
			case XdefBase.Id.BASE_64: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(
						XsdBase.BASE64_BINARY.getName()));
				if (paramCount == 0) {
					//do nothing
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem, XsdFacet.LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(
						restrElem, XsdFacet.MIN_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MAX_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(1));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.BOOLEAN: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.BOOLEAN.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
//			case XdefBase.Id.BNF: {
//				_xsdDoc.addRestrictionDecl(simpleTypeElem,
//				_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
//				if (paramCount != 1) {
//					throwUnexpectedParams(base, paramCount);
//				}
//			}
//			break;
			case XdefBase.Id.CONTAINS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
						_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(),
							".*" + getEscapedString(param, false) + ".*");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.CONTAINS_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
						_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(),
							".*" + getEscapedString(param, true) + ".*");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.XDATE_TIME: /*VT*/
			case XdefBase.Id.DATE_TIME: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1 || paramCount == 2) {
					String mask = (String) xdefType.getParams().get(0);
					Set<String> regexes = DatetimeMaskAnalyzer.getRegexes(mask);
					Iterator<String> it = regexes.iterator();
					while (it.hasNext()) {
						String regex = (String) it.next();
						_xsdDoc.addFacet(restrElem,
							XsdFacet.PATTERN.getXsdName(), regex);
					}
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.DATE_YMDHMS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(),
					(String) DatetimeMaskAnalyzer
						.getRegexes("yyyyMMddHHmmss").iterator().next());
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.DECIMAL: {
				Element unionElem;
				if (paramCount == 0) {
					unionElem = _xsdDoc.addUnionDecl(simpleTypeElem,
						_xsdDoc.getSchemaTypeQName(XsdBase.DECIMAL.getName()));
				} else {
					unionElem = _xsdDoc.addUnionDecl(simpleTypeElem, null);
					Element decimalSTypeElem =
						_xsdDoc.addSimpleTypeDecl(unionElem, null);
					Element restrElem = _xsdDoc.addRestrictionDecl(
						decimalSTypeElem,
						_xsdDoc.getSchemaTypeQName(XsdBase.DECIMAL.getName()));
					if (paramCount == 1) {
						_xsdDoc.addFacet(restrElem,
							XsdFacet.TOTAL_DIGITS.getXsdName(),
							(String) xdefType.getParams().get(0));
					} else if (paramCount == 2) {
						_xsdDoc.addFacet(restrElem,
							XsdFacet.TOTAL_DIGITS.getXsdName(),
							(String) xdefType.getParams().get(0));
						_xsdDoc.addFacet(restrElem,
							XsdFacet.FRACTION_DIGITS.getXsdName(),
							(String) xdefType.getParams().get(1));
					} else {
						throwUnexpectedParams(base, paramCount);
					}
				}
				Element patternSTypeElem =
					_xsdDoc.addSimpleTypeDecl(unionElem, null);
				Element restrElem = _xsdDoc.addRestrictionDecl(patternSTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				_xsdDoc.addFacet(restrElem,
					XsdFacet.PATTERN.getXsdName(), "\\d+(,\\d+)?");
			}
			break;
			case XdefBase.Id.EMAIL: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.EMAIL_DATE: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.EMAIL_LIST: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ENDS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(),
						".*" + getEscapedString(param, false));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ENDS_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						".*" + getEscapedString(param, true));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ENTITIES: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.ENTITIES.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ENTITY: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.ENTITY.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.EQUALS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.ENUMERATION.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.EQUALS_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						getEscapedString(param, true));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.FILE: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.FLOAT: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.DOUBLE.getName()));
				if (paramCount == 0) {
					//do nothing
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.ENUMERATION.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MIN_INCLUSIVE.getXsdName(),
						(String) xdefType.getParams().get(0));
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MAX_INCLUSIVE.getXsdName(),
						(String) xdefType.getParams().get(1));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.HEX: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.HEX_BINARY.getName()));
				if (paramCount == 0) {
					//do nothing
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MIN_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MAX_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(1));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ID: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.ID.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.IDREF: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.IDREF.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.IDREFS: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.IDREFS.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.INT: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.LONG.getName()));
				if (paramCount == 0) {
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.ENUMERATION.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MIN_INCLUSIVE.getXsdName(),
						(String) xdefType.getParams().get(0));
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MAX_INCLUSIVE.getXsdName(),
						(String) xdefType.getParams().get(1));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_DATE: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.DATE.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_DATE_TIME: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.DATE_TIME.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_DAY: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.G_DAY.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_DURATION: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.DURATION.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_LANGUAGE: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.LANGUAGE.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_LANGUAGES: {
				_xsdDoc.addListDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.LANGUAGE.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_MONTH: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.G_MONTH.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_MONTH_DAY: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.G_MONTH_DAY.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_TIME: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.TIME.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_YEAR: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.G_YEAR.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.ISO_YEAR_MONTH: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.G_YEAR_MONTH.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.LIST: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 0) {
					throwUnexpectedParams(base, paramCount);
				}
				List<String> params = xdefType.getParams();
				for (int i = 0; i < params.size(); i++) {
					String param = (String) params.get(i);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.ENUMERATION.getXsdName(), param);
				}
			}
			break;
			case XdefBase.Id.LIST_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 0) {
					throwUnexpectedParams(base, paramCount);
				}
				List<String> params = xdefType.getParams();
				for (int i = 0; i < params.size(); i++) {
					String param = (String) params.get(i);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						getEscapedString(param, true));
				}
			}
			break;
			case XdefBase.Id.MD5: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NC_NAME: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.NCNAME.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NC_NAME_LIST: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NM_TOKEN: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.NMTOKEN.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NM_TOKENS: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.NMTOKENS.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NORM_STRING: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(
						XsdBase.NORMALIZED_STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NORM_TOKEN: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NORM_TOKENS: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NOTATION: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.NOTATION.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.NUMBER: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 0) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(), "\\d+");
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						"\\d{" + xdefType.getParams().get(0) + "}");
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						"\\d{" + xdefType.getParams().get(0)
							+ "," + xdefType.getParams().get(1) + "}");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.PICTURE: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.Q_NAME: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.QNAME.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.Q_NAME_LIST: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 1) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.Q_NAME_LIST_URI: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.Q_NAME_URI: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 0) {
					//do nothing
				} else if (paramCount == 1) {
					//
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.REGEX: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.STARTS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						getEscapedString(param, false) + ".*");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.STARTS_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					String param = (String) xdefType.getParams().get(0);
					_xsdDoc.addFacet(restrElem,
						XsdFacet.PATTERN.getXsdName(),
						getEscapedString(param, true) + ".*");
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.STRING: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 0) {
					//do nothing
				} else if (paramCount == 1) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
				} else if (paramCount == 2) {
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MIN_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(0));
					_xsdDoc.addFacet(restrElem,
						XsdFacet.MAX_LENGTH.getXsdName(),
						(String) xdefType.getParams().get(1));
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.TOKENS: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					Set<String> tokenSet =
						getTokens(xdefType.getParams().get(0));
					Iterator<String> it = tokenSet.iterator();
					while (it.hasNext()) {
						String token = (String) it.next();
						_xsdDoc.addFacet(restrElem,
							XsdFacet.ENUMERATION.getXsdName(), token);
					}
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.TOKENS_I: {
				Element restrElem = _xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount == 1) {
					Set<String> tokenSet =
						getTokens((String) xdefType.getParams().get(0));
					Iterator<String> it = tokenSet.iterator();
					while (it.hasNext()) {
						String token = (String) it.next();
						_xsdDoc.addFacet(restrElem,
							XsdFacet.PATTERN.getXsdName(),
							getEscapedString(token, true));
					}
				} else {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.URI: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.ANY_URI.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.URI_LIST: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.URL: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			case XdefBase.Id.URL_LIST: {
				_xsdDoc.addRestrictionDecl(simpleTypeElem,
					_xsdDoc.getSchemaTypeQName(XsdBase.STRING.getName()));
				if (paramCount != 0) {
					throwUnexpectedParams(base, paramCount);
				}
			}
			break;
			default: {
				throw new RuntimeException(
					"Unknown or not implemented type name!");
			}
		}
	}

	/** Gets set of tokens from string containing all tokens.
	 * @param tokensString string of tokens.
	 * @return set of tokens (String).
	 */
	private Set<String> getTokens(String tokensString) {
		Set<String> ret = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(tokensString, "|");
		while (st.hasMoreTokens()) {
			ret.add(st.nextToken().trim());
		}
		return ret;
	}

	/** Throws exception with unexpected parameters.
	 * @param xsdType schema type.
	 * @param paramCount parameter count.
	 * @throws RuntimeException always.
	 */
	private void throwUnexpectedParams(XdefBase xdefType, int paramCount) {
		StringBuilder sb = new StringBuilder();
		throw new RuntimeException(sb.append(
			"Unexpected parameter count: ").
			append(paramCount).append(" in method: ").
			append(xdefType.getName()).toString());
	}

	/** Adds given schema base type to given schema context element.
	 * @param xsdBase schema base type object.
	 * @param contextElem schema context element.
	 */
	private void addXsdBase(XsdBase xsdBase, Element contextElem) {
		_xsdDoc.setType(contextElem, getXsdBaseQName(xsdBase));
	}

	/** Adds schema <code>restriction</code> declaration element with schema
	 * base type qualified name as <code>base</code> to given
	 * <code>simpleType</code> element.
	 * @param xsdBase schema base type object.
	 * @param sTypeElem schema <code>simpleType</code> element.
	 */
	private void addXsdBaseToSType(XsdBase xsdBase, Element sTypeElem) {
		_xsdDoc.addRestrictionDecl(sTypeElem, getXsdBaseQName(xsdBase));
	}

	/** Adds given schema type to given schema context element. If it is
	 * possible adds type as <tt>type</tt> attribute, otherwise as child
	 * <tt>simpleType</tt> element.
	 * @param xsdType schema type to add.
	 * @param contextElem schema context element to add type declaration to.
	 */
	private void addXsdType(XsdRestricted xsdType, Element contextElem) {
		String qName = getXsdTypeQName(xsdType);
		if (qName != null) {
			_xsdDoc.addDocumentation(contextElem, xsdType.getTypeString());
			_xsdDoc.setType(contextElem, qName);
			return;
		}
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(contextElem, null);
		addXsdTypeToSType(xsdType, sTypeElem);
	}

	/** Adds given schema type to given schema <tt>simpleType</tt> element.
	 * @param xsdType schema type to add.
	 * @param sTypeElem schema <tt>simpleType</tt> element to add type to.
	 */
	private void addXsdTypeToSType(XsdRestricted xsdType, Element sTypeElem) {
		_xsdDoc.addDocumentation(sTypeElem, xsdType.getTypeString());
		Element restrElem;
		if (xsdType.getBase() == null) {
			String baseTypeName =
				_xsdDoc.getSchemaTypeQName(xsdType.getXdefBase().getName());
			restrElem = _xsdDoc.addRestrictionDecl(sTypeElem, baseTypeName);
		} else {
			//TODO:
			//addType(restrElem,null,xsdType.getBase(),_xsdDoc,xdDoc,models);
			restrElem = _xsdDoc.addRestrictionDecl(sTypeElem, null);
		}
		//resolve restrictions
		Set<String> enums = xsdType.getEnumerations();
		Iterator<String> it = enums.iterator();
		while (it.hasNext()) {
			String enumeration = it.next();
			_xsdDoc.addFacet(restrElem,
				XsdFacet.ENUMERATION.getXsdName(), enumeration);
		}
		Set<String> patterns = xsdType.getPatterns();
		it = patterns.iterator();
		while (it.hasNext()) {
			String pattern = it.next();
			_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(), pattern);
		}
		if (xsdType.getFractionDigits() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.FRACTION_DIGITS.getXsdName(),
				xsdType.getFractionDigits().toString());
		}
		if (xsdType.getLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.LENGTH.getXsdName(), xsdType.getLength().toString());
		}
		if (xsdType.getMaxLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MAX_LENGTH.getXsdName(),
				xsdType.getMaxLength().toString());
		}
		if (xsdType.getMinLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MIN_LENGTH.getXsdName(),
				xsdType.getMinLength().toString());
		}
		if (xsdType.getTotalDigits() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.TOTAL_DIGITS.getXsdName(),
				xsdType.getTotalDigits().toString());
		}
		if (xsdType.getMaxExclusive() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MAX_EXCLUSIVE.getXsdName(), xsdType.getMaxExclusive());
		}
		if (xsdType.getMaxInclusive() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MAX_INCLUSIVE.getXsdName(), xsdType.getMaxInclusive());
		}
		if (xsdType.getMinExclusive() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MIN_EXCLUSIVE.getXsdName(), xsdType.getMinExclusive());
		}
		if (xsdType.getMinInclusive() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MIN_INCLUSIVE.getXsdName(), xsdType.getMinInclusive());
		}
		if (xsdType.getWhiteSpace() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.WHITE_SPACE.getXsdName(), xsdType.getWhiteSpace());
		}
	}

	/** Adds schema <tt>simpleType</tt> element according to given schema list
	 * type to given schema context.
	 * @param xsdList schema list type to add.
	 * @param contextElem schema context element to add <tt>simpleType</tt>
	 * element to.
	 */
	private void addXsdList(XsdList xsdList, Element contextElem) {
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(contextElem, null);
		addXsdListToSType(xsdList, sTypeElem);
	}

	/** Adds schema <tt>list</tt> element to given schema <tt>simpleType</tt>
	 * element according to given schema list type.
	 *
	 * @param xsdList schema list type to add.
	 * @param sTypeElem schema <tt>simpleType</tt> to add list type to.
	 */
	private void addXsdListToSType(XsdList xsdList, Element sTypeElem) {
		_xsdDoc.addDocumentation(sTypeElem, xsdList.getTypeString());
		boolean hasRestrictions = !(xsdList.getEnumerations().isEmpty()
			&& xsdList.getPatterns().isEmpty()
			&& xsdList.getLength() == null && xsdList.getMinLength() == null
			&& xsdList.getMaxLength() == null
			&& xsdList.getWhiteSpace() == null);
		if (!hasRestrictions) {
			addList(xsdList, sTypeElem);
			return;
		}
		Element restrElem = _xsdDoc.addRestrictionDecl(sTypeElem, null);
		Element listSTypeElem = _xsdDoc.addSimpleTypeDecl(restrElem, null);
		addList(xsdList, listSTypeElem);
		Set<String> enums = xsdList.getEnumerations();
		Iterator<String> it = enums.iterator();
		while (it.hasNext()) {
			String enumeration = it.next();
			_xsdDoc.addFacet(
				restrElem, XsdFacet.ENUMERATION.getXsdName(), enumeration);
		}
		Set<String> patterns = xsdList.getPatterns();
		it = patterns.iterator();
		while (it.hasNext()) {
			String pattern = it.next();
			_xsdDoc.addFacet(restrElem, XsdFacet.PATTERN.getXsdName(), pattern);
		}
		if (xsdList.getLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.LENGTH.getXsdName(), xsdList.getLength().toString());
		}
		if (xsdList.getMinLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MIN_LENGTH.getXsdName(),
				xsdList.getMinLength().toString());
		}
		if (xsdList.getMaxLength() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.MAX_LENGTH.getXsdName(),
				xsdList.getMaxLength().toString());
		}
		if (xsdList.getWhiteSpace() != null) {
			_xsdDoc.addFacet(restrElem,
				XsdFacet.WHITE_SPACE.getXsdName(), xsdList.getWhiteSpace());
		}
	}

	/** Adds schema <tt>list</tt> element to given schema <tt>simpleType</tt>
	 * element according to given schema list type.
	 * @param xsdList schema list type to add.
	 * @param sTypeElem schema <tt>simpleType</tt> element to add <tt>list<tt>
	 * element to.
	 */
	private void addList(XsdList xsdList, Element sTypeElem) {
		String qName = getTypeQName(xsdList, sTypeElem);
		if (qName != null) {
			_xsdDoc.addListDecl(sTypeElem, qName);
			return;
		}
		Element listElem = _xsdDoc.addListDecl(sTypeElem, null);
		Element itemTypeSTypeElem = _xsdDoc.addSimpleTypeDecl(listElem, null);
		addTypeToSType(xsdList.getValueType(), itemTypeSTypeElem);
	}

	/** Adds schema <tt>simpleType</tt> element to given schema context element
	 * according to given schema union type.
	 * @param xsdUnion schema union type to add.
	 * @param contextElem schema context element to add <tt>simpleType</tt>
	 * element.
	 */
	private void addXsdUnion(XsdUnion xsdUnion, Element contextElem) {
		Element sTypeElem = _xsdDoc.addSimpleTypeDecl(contextElem, null);
		addXsdUnionToSType(xsdUnion, sTypeElem);
	}

	/** Adds schema <tt>union</tt> element to given schema <tt>simpleType</tt>
	 * element according to given union type.
	 * @param xsdUnion schema union type to add.
	 * @param sTypeElem schema <tt>simpleType</tt> element to add union type to.
	 */
	private void addXsdUnionToSType(XsdUnion xsdUnion, Element sTypeElem) {
		_xsdDoc.addDocumentation(sTypeElem, xsdUnion.getTypeString());
		Element unionElem = _xsdDoc.addUnionDecl(sTypeElem, null);
		StringBuilder sb = new StringBuilder();
		Set<ValueType> memberTypes = xsdUnion.getMemberTypes();
		Iterator<ValueType> it = memberTypes.iterator();
		while (it.hasNext()) {
			ValueType type = it.next();
			String qName = getTypeQName(type, sTypeElem);
			if (qName != null) {
				if (sb.length() > 0) {
					sb.append(" ").append(qName);
				} else {
					sb.append(qName);
				}
				continue;
			}
			Element memberTypeSTypeElem =
				_xsdDoc.addSimpleTypeDecl(unionElem, null);
			addTypeToSType(type, memberTypeSTypeElem);
		}
		if (sb.length() > 0) {
			_xsdDoc.setMemeberTypes(unionElem, sb.toString());
		}
	}

	/** Sets schema <tt>type</tt> attribute to qualified name of given other
	 * type.
	 * @param otherType other type to add.
	 * @param contextElem schema context element to add type to.
	 */
	private void addOtherType(Other otherType, Element contextElem) {
		_xsdDoc.addDocumentation(contextElem, otherType.getTypeString());
		_xsdDoc.setType(contextElem, getOtherTypeQName(otherType, contextElem));
	}

	private void addOtherTypeToSType(Other otherType, Element sTypeElem) {
		_xsdDoc.addDocumentation(sTypeElem, otherType.getTypeString());
		_xsdDoc.addRestrictionDecl(sTypeElem,
			getOtherTypeQName(otherType, sTypeElem));
	}

	/** Returns string for XML Schema patterns with escaped characters from
	 * regular string.
	 * @param string string to get pattern from.
	 * @param caseInsensitive case insensitive letters switch.
	 * @return string for XML Schema patterns with escaped characters from
	 * regular string.
	 */
	private String getEscapedString(String string, boolean caseInsensitive) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			String st;
			if ((st = getEscapeString(ch)) != null) {
				ret.append(st);
			} else if (caseInsensitive && (st = getCaseInsensitive(ch))!=null) {
				ret.append(st);
			} else {
				ret.append(ch);
			}
		}
		return ret.toString();
	}

	/** Returns <tt>true</tt> if given character is character, that has to be
	 * escaped in schema patterns.
	 * @param ch character to test.
	 * @return <tt>true</tt> if given character is character, that has to be
	 * escaped in schema patterns.
	 */
	private boolean isEscapeChar(char ch) {
		return "\\|.-?*+{}(){}^".indexOf(ch) >= 0;
	}

	/** Returns escaped string of special characters for schema patterns or
	 * <tt>null</tt> if character does not have to be escaped.
	 * @param ch character to get escaped string from.
	 * @return escaped string of special characters for schema patterns or
	 * <tt>null</tt> if character does not have to be escaped.
	 */
	private String getEscapeString(char ch) {
		if (isEscapeChar(ch)) {
			return "\\" + ch;
		} else if (' ' == ch) {
			return "\\s";
		}
		return null;
	}

	/** Returns XML Schema pattern for case insensitive character
	 * or <tt>null</tt>.
	 * @param ch character to get case insensitive string.
	 * @return pattern for case insensitive character or <tt>null</tt>.
	 */
	private String getCaseInsensitive(char ch) {
		String ret = "[";
		if (Character.LOWERCASE_LETTER == Character.getType(ch)) {
			ret += ch;
			ret += Character.toUpperCase(ch);
			return ret + "]";
		} else if (Character.UPPERCASE_LETTER == Character.getType(ch)) {
			ret += Character.toLowerCase(ch);
			ret += ch;
			return ret + "]";
		}
		return null;
	}
}