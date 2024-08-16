# Version ${version}, release-date ${release.date}

# Version 42.2.3, release-date 2024-08-16
* .

# Version 42.2.2, release-date 2024-08-14
* Fixed bug in generating X-definition from JSON data.
* Fixed a bug in construction mode when there is a recursive reference
  in the model.
 * Fixed bug StackOverflow exception in construction mode when there is
   a recursive reference in the element model.

# Version 42.2.1, release-date 2024-07-08
* Fixed error in compilation of type declaration with expression containing
  `CHECK` operator.
* Fixed bug where `clearReports` method in `onXmlError` section did not clear
  all reports.

# Version 42.2.0, release-date 2024-06-27
* Fixed a bug that the `onXMLError` section in the X-script was not compiled and
  an error was reported when compiling the X-definition.

# Version 42.1.8, release-date 2024-06-27
* Fixed bug in `onIllegalAttr` and `onIllegalText` section of script - code was
  not executed or was not enabled.
* Fixed incorrect error message `XDEF422 Duplicated part of script` to
  `XDEF425 Script error`.
* By setting the value of the pf property `xdef_clearReports` to `true` or
  `false` it is now possible to set whether or not the actullal reporter
  is cleared in the executed code of the `onFalse`, `onIllegalAttr`,
  `onIllegalText`, `onEllegalElement` sections.
* The `clearReports` option and the `preserveReports` option are implemented
  in the X-script of the element model.

# Version 42.1.7, release-date 2024-06-21
* Fixed bug when ignoring `whitespace` type restrictions in XML Schema when
  converting XML Schema to X-definition.
* Fixed bug of incorrect generation of X-definition models to XML Schema
  if an item was declared as `illegal`.
* Fixed a bug where no error was reported if an attribute is declared as
  `illegal` in the X-definition and the `moreAttributes` option is set.

# Version 42.1.6, release-date 2024-05-28
* Fixed bug that did not report XDEF385 error when type name in local
  declaration section was the same as the referenced type.
* Fixed bug generated negative value to `maxOccurs` attribute in 
  X-definition conversion to XML Schema.
* To the list of parameters of method `org.xdef.util.XdefToXsd.main(...)` was
  added the parameter `--outType` with the name of the file where the types
  declared in the X-definition are generated (note that the extension `.xsd` is
  added to the name).

# Version 42.1.5, release-date 2024-05-20
* If the type name in the declaration section is the same as the referenced type
  name an XDEF385 error is now reported.
* All declared user types in the conversion of X-definition to XML schema are
  written to separated XML schema file.

# Version 42.1.4, release-date 2024-04-08
* corrected bug in conversion of parsed properties/INI data to string.
* corrected bug in conversion of X-definition type `dec` to XML schema.
* In the conversion of X-definition to XML schema (`org.xdef.util.XdefToXsd`)
  is added parameter `--xx`. If this parameter is used than from xdatetime
  X-definition validation method is used the mask from parameter `outFormat`
  (the second sequential parameter) for validation of XML data.
* most of X-definition methods is now more converted to XML schema string type
  with `pattern` facets.
* Added error message `XDEF262` if occurrence was set to greater than 1
  in the text node model.

# Version 42.1.3, release-date 2024-03-04
* The conversion of X-definition to XML schema (`org.xdef.util.XdefToXsd`)
  now respects also the options "fixed" and "default" from X-script.
* In the conversion of X-definition to XML schema (`org.xdef.util.XdefToXsd`)
  is corrected bug with duplicated elements `restriction` and 'annotation'.

# Version 42.1.2, release-date 2024-02-26
* Corrected bugs in the `org.xdef.util.XdefToXsd` utility.

# Version 42.1.1, release-date 2024-01-29
* Fixed bug in `CHECK` operation.
* Implemented utility `org.xdef.util.XdefToXsd` - conversion of X-definition
  to XML schema.
* Implemented utility `org.xdef.util.XsdToXdef` - conversion of XXML schema
  to X-definition.

# Version 42.1.0, release-date 2024-01-15
* Added the new binary operator `CHECK`. The result of an operation is 
  a `ParseResult` value. The type of the first operand must be `Parser` or
  `ParseResult` (if it is Parser then the parsing method is invoked so that it
  results from a `ParseResut` value). The second operand is a `boolean`
  expression (usually it is method) and it is invoked only if the `ParseResult`
  from the first processed operand does not contain any error messages. If the
  result of the invoked expression of the second operand is false, the error
  message `XDEF822: Parsed result does not fit to CHECK argument` is set to the
  `ParseResult` value.
* Fixed bug in nested sequence groups.
* If the type of a value in X-component is `union` and types of all items are
  compatible type, then the type of getter/settter is set according to that
  type (formally it was only `Object`).
* If in X-component the type is compatible with String`then type of
  getters/setters is now always String.
* corrected and improved `org.xdef.util.GUIEditor`.

# Version 42.0.11, release-date 2023-11-27
* Corrected bug in `org.xdef.xon.CsvReader` parser when after missing value at
  the end of the line follows the next line with a value.
* Corrected bug in X-component when the validation type is described as
  an expression.

# Version 42.0.10, release-date 2023-08-23
* Fixed bug in `equals(arg)` and `compareTo(arg)` methods in `SDatetime` class.
  Some internal variables were changed when it was called, which could cause
  an error when called in multitask.
* Generator of X-definition from source data supports now also YAML source data
  format (the package `org.yaml.snakeyaml` is available in classpath).

# Version 42.0.9, release-date 2023-08-07
. skipped

# Version 42.0.8, release-date 2023-07-10
* Corrected bug in generation of `XComponent` java source when was generated
  the modified name of getter/setter even if it was not necessary.
* Corrected bug in the method `toXml` from `xd:any` of generated XComponent.
  The returned `org.w3c.Element` was not inserted to the `org.w3c.Document`.
* Corrected bug in the construction`mode from a container which is an array
  with only one item, which is an array.
* X-definition is now generated from either XML and from JSON/XON data.
  (See `org.xdef.util.GenXDefinition` and `org.xdef.impl.GenXDef`.)

# Version 42.0.7, release-date 2023-05-10
* fixed bugs in methods `org.xdef.sys.FileUtils.filesToZip(...)`
  and `org.xdef.sys.FileUtils.filesFromZip(...)`.

# Version 42.0.6, release-date 2023-04-12
* Fixed bug in reference from model to model with a different namespace.
  The namespace of all child nodes and attributes with the original namespace
  is changed to the new namespace. However, if a reference from a model with
  an empty namespace to a model with a non-empty namespace, then the namespace
  of attributes and child nodes with the namespace in the referenced model
  is not changed.
* Fixed bug in X-component in the referenve to the model without namespace to
  the model with nonempty namespace.

# Version 42.0.5, release-date 2023-03-06
* The method `getDigest` of the `XMElement` now skips the version and build data.
* Therefore the X-components generated with different versions of X-definition
* are accepted as compatible (if the structure of the model of element was not
* changed).

# Version 42.0.4, release-date 2023-02-27
* corrected bug in `toXon()` method in the X-component class generated from
  an JSON/XON model with `%anyObj` spoecification.
* corrected conversion of JSON objects with values as byte array to JSON source.

# Version 42.0.3, release-date 2023-01-30
* Value of JSON/XON arrays, maps and %anyObj can be now obtained from
 X-component by methods `getArray$()`, `getMap${}` and `getAnyObj$()`.

# Version 42.0.2, release-date 2022-12-21
* `%anyName` named items of XON/JSON are now available in X-components as a map 
  created by the method `anyItem$()` (instead of `entriesOf()`).
* corrected bug in X-component in generation of names of getters of named values
  in map if the name if the name of named value is not acceptable
  as a Java identifier.
* corrected bug not recognized error if occurrence of `choice` group exceeds
  the specified occurrence limit.
* corrected bug not recognized error if occurrence of `sequence` group do not
  fit the specified occurrence limit and a `choice` group is the last one of
  the `sequence` group.
* corrected bug not recognized error if maximum occurrence of `%anyObj` exceeded
  the specified limit.

# Version 42.0.1, release-date 2022-11-30
* corrected bug in reading X-definition from URL where in the file name are
  spaces or URL encoded characters.
* corrected bug in occurrence specification in %anyObj.
* improved X-script analysis of XON/JSON models.
* improved generation of X-components of XON/JSON models.
* %anyName named items of XON/JSON are now available in X-components as a map
  created by the method `entriesOf()`.

# Version 42.0.0, release-date 2022-10-14
* Corrected bug in `uses` and `implements` in X-script when it refers 
* to a node with specified namespace URI.
* New things:
* This version of X-definition supports JSON, YAML, Propreties, Windows INI,
* CSV and XON data processing. See the documentation for this release.
* Implemented XON command %anyName.
* Implemented XON command %anyObj.
* Names of XON commands $:script and $:oneOf, changed to %script and %oneOf.
* Implemented %encoding directive in XON data.

# Version 41.0.9, release-date 2022-06-09
* This build is a pre-release of version 4.2 of X-definition.
  It supports implementation of data formats JSON, XON, Properties,
  and Windows INI, and CSV. Those features are undocumented yet.
  However, this buld supports all features of version 4.1.
* Asterisk (`*`) may be now used for sequential parameters specifying minimum
  or maximum of `minLength`, `maxLength`, `minInclusive`, or  `maxInclusive`.
* In XON format it is now possible to write `=` (equal sign) after a name of
  named value even if it is quoted.
* prepared new internal format of XON -> XMl contersion (see methods
  `org.xdef.xon.XonUtils.xonToXmlX(...)`).
* In XON format follows after the name of value a colon (instead of equal sign).
* XON command names in X-definition models are now `x:script`, and `x:oneOf`
  instead of `$script` and `$oneOf`.

# Version 41.0.8, release-date 2022-04-26
* added method `toXon()` the the interface `org.xdef.component.XComponent`.
* added generation of named items with `$oneOf` option in X-components.
* fixed bug in XON generation in models with `$oneOf` option.

# Version 41.0.7, release-date 2022-04-11
* fixed bug in method `XComponent.toXml()' when parsed value of byte array
  created by validateion method `hex()` was converted to base64 format
  (not to hexadecimal).
* corrected conversion `XonUtils.xonToXmlXD(xon)` if in an array is the first
  item an empty map.
* fixed bug in XON parser in map with duplicated name pair not reported error.

# Version 41.0.6, release-date 2022-04-06
* fixed bug in `org.xdef.util.GUIEditor` if X-definition is not a valid XML.
* values in XON mode now can be either null or the specified type.
* improved XON mode converion to XML.
* fixed bug in XON createmode where is used `$oneOf`.

# Version 41.0.5, release-date 2022-03-06
* fixed bug rounding of time and millisedonds are equal to 500 (the second was
  not increased by one).
* fixed bug in type parse methods with named parameters `%base` and `~item`.
* fixed bug in X-components with union types.
* methods `XDDocument.parseXComponent(...)` are now deprecated
  please use methods `XDDocument.xparseXComponent(...)` instead.
* implemented new methods `XDDocument.iparseXComponent(...)`,
  `XDDocument.jparseXComponent(...)` and `XDDocument.yparseXComponent(...)`.
* implemented `telephone`, the new validation method and the new type of object
  `Telephone`.

# Version 41.0.4, release-date 2022-01-13
* implemented new type validation methods `country` and `countries`.
* `<xd:json>` model in X-definition changed now to `<xd:xon>`.
* fixed bug in datetime formatting when mask of a xdatetime contains `SSS`
  and the value of milliseconds in datetime is zero.
* fixed bug NullPointerException in parsing of incorrect JSON data.
* fixed bug in the method `SDatetime.reset()`.
* corrected generation of XON from XComponent
  (`org.xdef.component.XComponentUtil.toXon(...)`.
* improved generation the indented string from XON/JSON.
* implemented new methods `org.xdef.XDDocument.xcreateXComponent(...)`.

# Version 41.0.3, release-date 2021-12-05
* added tools for processing YAML data (see methods `parseYAML`
  and `toYamlString` in the class `org.xdef.xon.XonUtil` and `yparse` in
  `org.xef.XDDocument`).
* added tools for processing Properties and Windows-ini data (see methods
  and `parseINI` in the class `org.xdef.xon.XonUtil` and `iparse` in
  `org.xef.XDDocument`).

# Version 41.0.2, release-date 2021-11-24
* Corrected generation of X-components from models containing a choice section.
* The compilation of X-definitions stops if an XML error occurs in
  an X-definition source and only the XML errors are reported.

# Version 41.0.1, release-date 2021-11-21
* Fixed bug in generation of X-component where in the choice section is an item
  with max occurrence greater then one.
* Fixed bug in X-script method `xquery`.
* The deprecated method `setExternals(...)` from `org.xdef.XDBuilder`
  was removed.
* The parameter list of the method `org.xdef.XDFactory(compileXD(...)` no more
  supports classes with external methods. All external classes must be now
  declared in X-definitions.

# Version 41.0.0, release-date 2021-11-15
* Preparing release version 4.1.
* From this project are removed utilities for conversion XML schema to/from
  X-definition (classes `org.xdef.util.XdefToXsd`, `org.xdef.util.XsdToXdef`).
* to the interface 'org.xdef.XDPool' added method `genXComponent`.
* added X-script type `InetAddr`
* added X-script type `Currency`
* X-component generator now generates for XON/JSON components the method `toXon`
* fixed bug in X-component generation with `%bind` command
* The method
  `XDDocument.setStreamWriter(Writer out, String encoding, boolean writeHeader)`
  is now deprecated. Please use `OutputStream` instead of `Writer`.

# Version 40.1.8, release-date 2021-10-26
* fixed bug if an external method throws an exception.
* BNF grammar is extended and has now possibility of "all" selection
* In the `org.xdef.XDConstants` was the item `BUILD_DATE` replaced
  with `BUILD_DATETIME`.
* In the `org.xdef.XDConstants` was removed the item 'JAVA_VERSION'.
*******************************************************************
* preliminary implementation of features of X-definition version 4.1:
* utilities for processing JSON, XON, INI and Properties data are now
  in the package 'org.xdef.json'
* added the readers `org.xdef.json.IniReader` for `INI` and `Properties` files.
* implemented description of JSON/XON models in the element "xd:json"
* implemented description of INI/Properties models in the element "xd:ini"

# Version 40.1.7, release-date 2021-08-13
* fixed bug in type declaration with a boolean expression.
* fixed bug in XQuery sequence item types: unsigned integer,
  non positive integer, nonnegative integer and unsigned long.
* fixed bug in XQuery: NullpointerException if called from declaration part.
* method `contextValue` in `org.xdef.XDValue` was renamed to `containerValue`.
* method `toContext` in `org.xdef.XDElement` and in X-script was renamed to
  `toContainer`.
* parameter java.io.Writer in constructors and methods connected with XMLWriters
  is now deprecated. You can use the OutputStream instead.
* to `org.xdef.XDTools` was added static method:
    * XDXmlOutStream createXDXmlOutStream(OutputStream out, String encoding,
      boolean writeDocumentHeader) throws IOException

# Version 40.1.6, release-date 2021-07-20
* fixed bug if the namespace of root element prefix is not specified (was
  reported only as null pointer Exception).
* preparing version 41.0.
* parameter java.io.Writer in constructors and methods connected with XMLWriters
  is now deprecated. You can use the OutputStream instead.
* to `org.xdef.XDTools` was added static method:
    * XDXmlOutStream createXDXmlOutStream(OutputStream out, String encoding,
      boolean writeDocumentHeader) throws IOException

# Version 40.1.5, release-date 2021-06-25
* fixed bug in nested type declarations.
* fixed few bugs in parsing of email address (it's now up to rfc288).
* X-definition 2.0 is now deprecated.
* removed deprecated type methods: `ISODay`, `ISOMonth`, `ISOMonthDay`,
 `ISOyearMonth`, `ISOYear`, `ISOdateTime`, `ISOdate`, `tokens`, `tokensi`,
 `ISOduration`, `ISOlanguage`, `parseISODate`, `parseSequence`, `ListOf`.
* implemented new X-script types `URI` and `EmailAddr`.
* changed names of validation methods: `email` to `emailAddr` and `emailList`
  to `emailAddrList`.

# Version 40.1.4, release-date 2021-05-13
* fixed bug: decimal point of amount in price is now always '.' (independent on
  `Locale` settings).
* fixed bug in XON to JSON conversion: datetime is not converted
  as JSON string.
* fixed bug in GUI debugger: not reported errors in source X-definition
  compilation.
* fixed bug not thrown exception when occurs some errors in JSON/XON parser.

# Version 40.1.3, release-date 2021-05-08
* fixed bug in display of zone with zero hours and negative minutes in offset.
* fixed bug in the method `setParsedString` of objects `ParseResult`.
* fixed bug in construction mode when in the section `xd:mixed` is embedded
  a section `xd:choice` (an error XDEF555 was incorrectly reported).
* fixed endless cycle in the X-position with reference to inner selection group.
* fixed bug in generation of X-components when the root model or the referred
* model has occurrence more then one and it has only a text child node.
* improved creation of XML from JSON.
* improved error reporting in JSON.
* ==================================================================
* NOTE this version implements some new features prepared for version 41.0.0:
* ==================================================================
* added the Java method `rule.validate(String)` to X-script of `BNFRule` objects.
  Result of this method is a `boolean` value.
* added new Java method to `XDDocument`:
  * `XDParseResult parseXDType(String typeName, String data)`
  this method invokes a validation method from declared type in X-definition
  and returns `XDParseResult` object.
* added new X-definition type of value: `GPSPosition`.
    * Methods with this object:
        * `latitude()` returns GPS latitude in degrees (-90.0 to 90.0).
        * `longitude()` returns GPS longitude in degrees (-180.0 to 180.0).
        * `altitude()` returns GPS altitude in meters.
        * `name()` returns name of GPS position or null.
        * `distanceTo(GPSPosition x)` returns distance to GPS position `x` in meters.
    * Constructors (latitude, longitude, altitude are float numbers, name is a string):
        * `new GPSPosition(latitude, longitude)`
        * `new GPSPosition(latitude, longitude, altitude)`
        * `new GPSPosition(latitude, longitude, altitude, name)`
        * `new GPSPosition(latitude, longitude, name)`
* added new XML validation method `gps` (The Java implementation is in class
  `org.xdef.sys.GPSPosition`). The required form is:
  `(latitude, longitude[, altitude[, name]])`
  where parameters `latitude`, `longitude`, `altitude` are numbers and `name`
  is a string either containing only letters. Otherwise it must be quoted.
  After the comma separator is one space. Examples:
    * `(51.52, -0.09, 0.0, London)` or
    * `(51.52, -0.09)` (altitude and name not specified).
* added new X-definition type of value `Price`
  (the Java implementation is in class `org.xdef.sys.Price`).
    * Constructor:
        * `new Price(amount, code)`
          where `amount` is a number and `code` is a ISO 4217 currency code. 
    * Methods with this object:
        * `amount()` returns amount of currency as decimal number.
        * `currencyCode()` returns ISO 4217 currency code.
        * `fractionDigits()` returns recommended number of fraction digits or -1.
        * `display()` returns string with printable form of currency (i.e. decimal
          number with recommended number of decimal digits, space and
          ISO 4217 currency code).
* added new XML validation method `price`. The required form is:
  * `(decimal_number code)`; e.g. `(12.25 USD)`.

# Version 40.1.2, release-date 2021-02-15
* corrected the bug in the X-script method `s.contains(s)`.
* corrected the bug in the construction node of JSON.
* corrected the bug in JSON parser: duplicated name in object not reported.
* corrected the bug in JSON parser: after decimal point and before decimal point
  must be a digit.
* to `org.xdef.XDDocument` it is implemented the new method
  `Object jcreate(String name, ReportWriter reporter)`.
  This method allows to construct JSON data acccording to JSON model with given
  name. The method returns constructed JSON object.
* to `org.xdef.proc.XXNode` it is implemented the new method
  `public void setJSONContext(Object data)`.
  This method sets the context with JSON data used in the construction mode.
* to the BNF grammar are implemented new inline functions `$skipToNextLine`,
  `$UTFChar`, and it is implemented the possibility to specify case insensitive
  terminal symbols (when the terminal symbol specification follows percentage
  character).
* if in the date and datetime validation method is specified any of constraining
  facets `%minInclusive`, `%maxInclusive`, `%minExclusive` or `%maxExclusive`,
  then it is now skipped the test of date validity (which can be set
  by properties or by methods `setMinDate` or `setMaxDate`).
* the method `setBufIndex(int)` in the class org.xdef.sys.StringParser was
  renamed to `setIndex(int)`.
* implemented new version of transformation of XML to JSON and of JSON to XML.

# Version 40.1.1, release-date 2020-09-17
* corrected bugs in BNF syntax of X-definitions
* implemented the new method `chkCompatibility(String version)` to XDPool.
  This allows to check the version of `XDPool` object with the actual
  X-definition software.
* in the class `org.xdef.KDOMUtils` is implemented new static method
  `public static void removeRedundantXmlnsAttrs(Element el)`. This method
  removes unused `xmlns` attributes and moves repeatable used xmlns attributes
  to the element el.
  This method is now invoked in the construction mode of X-definition on the
  constructed element.

# Version 40.1.0, release-date 2020-08-04
* corrected bug illegal type in `ObjectReader` when the code contains an item
  with `uniqueSetKey`.
* in the X-script was implemented the new type `uniqueSetKey`. This type of
  object enables to save the actual value of the key of an `uniqueSet`. To do it 
  you can invoke the new implemented method `getActualKey()` from `uniqueSet`
  object. With the new method `resetKey` from `uniqueSetKey` the actual key
  of given `uniqueSet` object is set to the saved value. E.g.:
  ```xml
  <NodeValue xd:script=
    'var uniqueSetKey k; init k=nodeSet.getActualKey(); finally k.resetKey();'>
  ...
  ```
* implemented the new X-script method `bindSet(uniqueSet u1[,uniqueSet u2...])`.
  This method can be specified only in the `init` section of the X-script
  of model of Element. At the end of processing of the element where it was
  invoked it sets to all specified `uniqueSet` parameters the value of the
  actual key from the init time (it happens after `finally` event).

# Version 40.0.2, release-date 2020-06-22
* improved generation of X-definition from XML data in
  `org.xdef.util.GenXDefinition`.
* the utility `org.xdef.util.GUIEditor` now supports also JSON data.
  of date time values. Eg.: `Mon May 11 23:39:07 CEST 2020`.
* corrected bug in v `org.xdef.XDFactory` when for the compilation of
  X-definition is specified a `org.xdef.sys.ReportWriter` of other type
  then `org.xdef.ArrayReporter`.
* corrected bug in parsing method `xdatetime` by mask with variants when
  the simpler variant precedes the more complex one.
* in X-script implemented new validation method `SHA1`.
* in X-script implemented new validation method `printableDate` for the
  printable format.
* implemented new X-script type of variable `uniqueSetKey` which holds the key
  of an item in `uniqueSet` table.
* in X-script is implemented new method `getActualKey` on `uniqueSet` objects.
  The result of this method is object uniqueSetKey which holds the actual item
  in the `uniqueSet` table (i.e. after invoking methods `ID`, `SET`, `IDREF`,
  `CHECK`).
* in X-script is implemented new method `resetKey` of `uniqueSetKey` objects.
  This method sets the actual value of key to the value which is saved
  in the `uniqueSetKey` object.

# Version 40.0.1, release-date 2020-05-15
* improved error reporting.
* corrected bug when a JSON string value contains some of escape characters.
* The default value of property xdef_warnings was changed to true.
* The parameters with the type Class in the method `org.xdef.XDFactory.compileXD`
  are deprecated.

# Version 40.0.0, release-date 2020-04-24
* new implementation of processing of JSON data.
* `xd:def` may now contain attribute `xd:importLocal` (the list of X-definition
  names from where are imported local declarations).
* xd:root may refer also to named xd:choice.
* corrected bug in error reporting (in some special cases was incorrect `xpath`).
* corrected bug in invoking methods from referred types.
* to the class `org.xdef.XDFactory` are implemented methods `writeXDPool`
  and `readXDPool`.
* in X-script is implemented method `getCreateContextElement` which in
  the construction mode returns the actual element from context, otherwise it
  returns null.
* the default scope of accessibility of items from the `xd:declaration` declared
  as child of `xd:def` is now local (in previous versions it was global).
* to xd:def element is no possible to write the attribute `importLocal`. The
  value of this attribute is the comma separated list of names of X-definitions
  from which are imported items from local xd:declarations (the `noname`
  X-definition is specified by the character `#`).
* NOTE that the implementation of JSON is preliminary, it may be changed in
  future versions.

# Version 32.5.6, release-date 2020-02-15
* implemented possibility of processing of JSON data in X-definition. This
  version is preliminary to the version 40.0.0 (the documentation of JSON usage
  will come with the version 40.0.0).

# Version 32.5.5, release-date 2019-12-17
* implemented new X-script method String `getEnv(String name)` which reads
  an environment variable.
* it is now possible to use environment variables likewise as properties.
  The property item has priority. If there are specified both, the environmental
  variable and the property item, then it is used the property value instead of
  value of the environmental variable. The names of properties and environment
  variables are changed, the dots (`.`) in a names are replaced by
  underlines (`_`), e.g. `_xdef.debug_` is now `_xdef_debug_`
  (see `org.xdef.XDConstants`).
  However, since the names with dots are deprecated, they are still accepted.
* corrected bug when it is specified attribute `_xd:text_` and a text node
  follows the last one element in a sequence.
* corrected bug when in types `_union_` or `_list_` are references to declared
  types.
* corrected bug in processing of elements with `_xd:textcontent_` attribute.
* reports error if xpath expression is empty string or null.
* corrected bugs in construction of elements with `_xd:text_` attribute.

# Version 32.5.4, release-date 2019-11-17
* corrected bug that in the section `onStartElement` were ignored some errors
  reported by methods of `uniqueSet`.
* corrected bug in X-components where are used the validation methods `xdType`.

# Version 32.5.3, release-date 2019-10-22
* improved the utility `org.xdef.GUIEditor`. Added parameter the `-g`
  which enables to create X-definition from given XML data

# Version 32.5.2, release-date 2019-10-09
* corrected `org.xdef.sys.SDatetime` methods implemented from
  `javax.xml.datatype.XMLGregorianCalendar`
* the parameters with sources of X-definitions and the items of the attribute
  `xd:include` of root elements `xd:def` or `xd:collection` may contain the file
  names with wildcard character `*` or `?`.
  E.g.:
  ```java
  XDPool xp = XDFactory.compileXD(props, "classpath://xxx.yyy.a*.xdef");
  ```
  or
  ```xml
  <xd:collection xmlns:xd = "..." xd:include = "classpath://xxx.yyy.a*.xdef"/>
  ```
  
# Version 32.5.1, release-date 2019-09-25
* Corrected `org.xdef.sys.SDatetime` methods implemented from
  `javax.xml.datatype.XMLGregorianCalendar`.
* implemented the access to XML data or X-definition data with
  an URL-like form where the protocol name is `classpath`.
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
  or in the attribute `xd:include` in header of X-definition:
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
  object (i.e. `java.util.Map` as JSON object or `java.util.List` as JSON array).
  It is also now available the new class `org.xdef.json.JsonUtil` which
  enables to work with JSON objects (conversion JSON to XML, XML to JSON,
  parsing of source JSON data, compare JSON objects etc.).
  In the X-definition will be possible in future to describe structure
  of JSON models.

# Version 32.4.0, release-date 2019-04-02
* when X-component is generated from an element model where are no attributes
  and it has just a text child node, there are also generated getter/setter
  methods which have the `$` set before the name of element (e.g. get$name()).
* the namespace URI for the X-definition version 3.2 is changed
    * old: `http://www.xdef.org/3.2`
    * new: `http://www.xdef.org/xdef/3.2`
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
* X-definition validation methods with the prefix `xs:` are deprecated. You can
  use the name without this prefix (ie. `xs:string` should be `string`)

# Version 3.2.1.2, release-date 2019-01-21
* corrected backward compatibility of the validation type declaration in
  the X-definition version 2.0 (the keyword `parse:`)
* corrected the bug if the missing element in the model is followed by
  optional elements which are also missing
* corrected the bug in construction mode. It was not invoked the
  initialization section in the section `var` in the X-script of 
  an element model
