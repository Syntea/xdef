package buildtools;

import java.io.File;
import org.xdef.XDValue;
import org.xdef.XDValueID;
import org.xdef.impl.compile.CompileBase;
import org.xdef.sys.FUtils;
import org.xdef.xml.KXmlUtils;

/** This class generates XML document with the information about implemented
 * methods used in GUI plugins.
 * @author Vaclav Trojan
 */
public class GenPluginInfo {

	private static String getMethodParams(final String name,
		final short baseType) {
		CompileBase.InternalMethod im =
			CompileBase.getTypeMethod(baseType, name);
		String result = "<" + name + ">";
		short[] partypes = im.getParamTypes();
		int maxparams = im.getMaxParams();
		if (maxparams >= 1 && partypes != null) {
			result += "<params>";
			String params = "";
			for (int i = 0; i < partypes.length; i++) {
				if (!params.isEmpty()) {
					params += ", ";
				}
				params += CompileBase.getTypeName(partypes[i]);
				String[] sqpars = im.getSqParamNames();
				if (sqpars != null) {
					if (i < sqpars.length) {
						params += '(' + sqpars[i] + ')';
					}
				}
			}
			result += params;
			if (maxparams > partypes.length) {
				result += "...";
			}
			result += "</params>";
		}
		String keys;
		CompileBase.KeyParam[] kpars = im.getKeyParams();
		keys = "";
		if (kpars != null) {
			for (CompileBase.KeyParam x : kpars) {
				if (!keys.isEmpty()) {
					keys += " ";
				}
				XDValue deflt = x.getDefaultValue();
				XDValue[] legals = x.getLegalValues();
				String params = "";
				if (legals != null && legals.length > 0) {
					for (XDValue y : legals) {
						if (!params.isEmpty()) {
							params += " | ";
						}
						params += (y.getItemId() == XDValueID.XD_STRING)
							? '"' + y.toString() + '"' : y.toString();
						if (y.equals(deflt)) {
							params += '*';
						}
					}
					if (!params.isEmpty()) {
						params = '[' + params + ']';
					}
				} else {
					params += '(' + CompileBase.getTypeName(x.getType()) + ')';
				}
				if (!params.isEmpty()) {
					keys += '%' + x.getName() + params;
				}
			}
		}
		if (!keys.isEmpty()) {
			result += "<keyParams>" + keys + "</keyParams>";
		}
		result += "</" + name + ">";
		return result;
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String s;
		try {
			File temp = new File("temp");
			temp.mkdirs();
			String[] names = new String[] {
				"anyURI",
				"base64Binary",
				"boolean",
				"byte",
				"date",
				"dateTime",
				"decimal",
				"double",
				"duration",
				"ENTITIES",
				"ENTITY",
				"float",
				"gDay",
				"gMonth",
				"gMonthDay",
				"gYear",
				"gYearMonth",
				"hexBinary",
				"ID",
				"IDREF",
				"IDREFS",
				"int",
				"integer",
				"language",
				"list",
				"long",
				"Name",
				"NCName",
				"negativeInteger",
				"NMTOKEN",
				"NMTOKENS",
				"nonNegativeInteger",
				"nonPositiveInteger",
				"normalizedString",
				"NOTATION",
				"positiveInteger",
				"QName",
				"short",
				"string",
				"time",
				"token",
				"union",
				"unsignedByte",
				"unsignedInt",
				"unsignedLong",
				"unsignedShort",
				"ID",
				"IDREF",
				"IDREFS",
			};
			s = "<SchemaValidationMethod>\n";
			for (String x : names) {
				s += getMethodParams(x, CompileBase.X_NOTYPE_VALUE);
			}
			s += "</SchemaValidationMethod>";
			s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
			FUtils.writeString(new File(temp, "SchemaValidationMethod.xml"),
				s, "UTF-8");
			names = new String[] {
				"an",
	//			"base64",
	//			"bool",
//				"BNF",
				"contains",
				"containsi",
				"dateYMDhms",
	//			"datetime",
	//			"dec",
				"emailDate",
				"empty",
				"ends",
				"endsi",
				"enum",
				"enumi",
				"eq",
				"eqi",
	//			"hex",
	//			"ISOdate",
	//			"ISOdateTime",
	//			"ISOday",
	//			"ISOduration",
				"ISOlanguage",
				"ISOlanguages",
	//			"ISOmonth",
	//			"ISOmonthDay",
	//			"ISOtime",
	//			"ISOyear",
	//			"ISOyearMonth",
	//			"jboolean",
	//			"jnull",
	//			"jnumber",
	//			"jstring",
	//			"jvalue",
				"languages",
				"letters",
				"MD5",
				"NCNameList",
				"nmTokens",
	//			"normString",
	//			"normToken",
	//			"normTokens",
				"num",
	//			"parseSequence",
				"QNameList",
				"QNameURI",
				"QNameURIList",
				"pic",
	//			"picture",
				"regex",
				"sequence",
				"starts",
				"startsi",
				"xdatetime",
				"CHKID",
				"CHKIDS",
				"SET"};
			s = "<XDValidationMethod>\n";
			for (String x : names) {
				s += getMethodParams(x, CompileBase.X_NOTYPE_VALUE);
			}
			s += "</XDValidationMethod>";
			s = KXmlUtils.nodeToString(KXmlUtils.parseXml(s), true);
			FUtils.writeString(new File(temp, "XDValidationMethod.xml"),
				s, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}