# Version ${version}, release-date ${release.date}

# Version 40.0.0, release-date 2020-04-24
* new implementation of processing of JSON data.
* xd:def may now contain attribute xd:importLocal (the list of X-definition
  names from where are imported local declarations).
* xd:root may refer also to named xd:choice.
* corrected bug in error reporting (in some special cases was incorrect xpath).
* corrected bug in invoking methods from referred types.
* to the class org.xdef.XDFactory are implemented methods writeXDPool
  and readXDPool.
* in X-script is implemented method getCreateContextElement() which in
  the construction mode returns the actual element from context, otherwise it
  returns null.
* the default scope of accessibility of items from the xd:declaration declared
  as child of xd:def is now local (in previous versions it was global).
* to xd:def element is no possible to write the attribute "importLocal". The
  value of this attribute is the comma separated list of names of X-definitions
  from which are imported items from local xd:declarations (the "noname"
  X-definition is specified by the character "#").
* NOTE that the implementation of JSON is preliminary, it may be changed in
  future versions.

# Version 32.5.6, release-date 2020-02-15
* implemented possibility of processing of JSON data in X-definition. This
  version is preliminary to the version 40.0.0 (the documentation of JSON usage
  will come with the version 40.0.0).

# Version 32.5.5, release-date 2019-12-17
* implemented new X-script method String getEnv(String name) which reads
  an environment variable.
* it is now possible to use environment variables likewise as properties.
  The property item has priority. If there are specified both, the environmental
  variable and the property item, then it is used the property value instead of
  value of the environmental variable. The names of properties and environment
  variables are changed, the dots (".") in a names are replaced by
  underlines ("&lowbar;"), e.g. "_xdef.debug_" is now "_xdef&lowbar;debug_"
  (see _XDConstants_).
  However, since the names with dots are deprecated, they are still accepted.
* corrected bug when it is specified attribute "_xd:text_" and a text node
  follows the last one element in a sequence.
* corrected bug when in types "_union_" or "_list_" are references to declared
  types.
* corrected bug in processing of elements with "_xd:textcontent_" attribute.
* reports error if xpath expression is empty string or null.
* corrected bugs in construction of elements with "_xd:text_" attribute.

# Version 32.5.4, release-date 2019-11-17
* corrected bug that in the section "onStartElement" were ignored some errors
  reported by methods of UniqueSet.
* corrected bug in X-components where are used the validation methods "xdType".

# Version 32.5.3, release-date 2019-10-22
* improved the utility org.xdef.GUIEditor. Added parameter the "-g"
  which enables to create X-definition from given XML data

# Version 32.5.2, release-date 2019-10-09
* corrected org.xdef.sys.SDatetime methods implemented from
  javax.xml.datatype.XMLGregorianCalendar
* the parameters with sources of X-definitions and the items of the attribute
  "xd:include" of root elements "xd:def" or "xd:collection" may contain the file
  names with wildcard character "*" or "?".
  E.g.:
  ```java
  XDPool xp = XDFactory.compileXD(props, "classpath://xxx.yyy.a*.xdef");
  ```
  or
  ```xml
  <xd:collection xmlns:xd = "..." xd:include = "classpath://xxx.yyy.a*.xdef"/>
  ```

# Version 32.5.1, release-date 2019-09-25
* Corrected org.xdef.sys.SDatetime methods implemented from
  javax.xml.datatype.XMLGregorianCalendar.
* implemented the access to XML data or X-definition data with
  an URL-like form where the protocol name is "classpath".
  E.g.:
  ```java
  String urlName = "classpath://org.xdefimpl.compile.XdefOfXdefBase.xdef";
  ```
  This form may be used to parse XML source data:
  ```java
  Document dom = KXmlUtils.parseXml(urlName);
  ```
  or to X-definition in compilation of XDPool:
  ```java
  XDPool xp = XDFactory.compileXD(null, urlName);
  ```
  or in the attribute "xd:include" in header of X-definition:
  ```xml
  <xd:def xmlns:xd ="http://www.xdef.org/xdef/3.2"
    xd:include = "classpath://org.xdefimpl.compile.XdefOfXdefBase.xdef,
      classpath://org.xdefimpl.compile.XdefOfXdef20.xdef,
      classpath://org.xdefimpl.compile.XdefOfXdef31.xdef" 
  > ...
  ```
  
# Version 32.5.0, release-date 2019-09-16
* Prepared the version for processing of JSON data.
  The X-component interface has now new method to Json which returns a JSON
  object (i.e. java.util.Map as JSON object or java.util.List as JSON array).
  It is also now available the new class org.xdef.json.JsonUtil which
  enables to work with JSON objects (conversion JSON to XML, XML to JSON,
  parsing of source JSON data, compare JSON objects etc.).
  In the X-definition will be possible in future to describe structure
  of JSON models.

# Version 32.4.0, release-date 2019-04-02
* when X-component is generated from an element model where are no attributes
  and it has just a text child node, there are also generated getter/setter
  methods which have the "$" set before the name of element (e.g. get$name()).
* the namespace URI for the X-definition version 3.2 is changed
    * old: "http://www.xdef.org/3.2"
    * new: "http://www.xdef.org/xdef/3.2"
  The old value is also accepted (because of compatibility reason).

# Version 32.3.2, release-date 2019-03-24
* fixed error in generation of X-components from models where there are more
  nested models with the same name.

# Version 32.3.1, release-date 2019-03-21
* fixed errors in generation of X-components from models with namespace URI

# Version 32.3.0, release-date 2019-03-12
* to the X-script added method
    XDValue replaceItem(int index, XDValue value)
  This method is member of the Container objects
* fixed errors in conversion of X-definition to XML schema (in declared types)

# Version 32.2.1, release-date 2019-02-25
* fixed X-component bug when the prefix of the attribute namespace differs
  from the prefix in the model. The method toXml() returns not then correct
  result
* now is available the technology of X-lexicon (see the document
  xdef-32_Lexicon.pdf)

# Version 32.1.3, release-date 2019-02-04
* corrected bug in the construction mode. The initialization of variables
  in the X-script section "var" in the model of element was not invoked
  correctly
* corrected setting of stop addresses in the GUI of debugger
* X-definition validation methods with the prefix "xs:" are deprecated. You can
  use the name without this prefix (ie. "xs:string" should be "string")

# Version 3.2.1.2, release-date 2019-01-21
* corrected backward compatibility of the validation type declaration in
  the X-definition version 2.0 (the keyword "parse:")
* corrected the bug if the missing element in the model is followed by
  optional elements which are also missing
* corrected the bug in construction mode. It was not invoked the
  initialization section in the section "var" in the X-script of 
  an element model

# Previous versions
* the previous versions were distributed by the site www.syntea.cz.
  In this version were renamed the packages "cz.syntea.xdef" to "org.xdef"
