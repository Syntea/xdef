# Version 32.3.2, release-date 2019-03-24
- fixed error in generation of X-components from models where there are more
  nested models with the same name.

# Version 32.3.1, release-date 2019-03-21
- fixed error in generation of X-components from models with namespace URI.

# Version 32.3.0, release-date 2019-03-12
- to the X-script added method
    XDValue replaceItem(int index, XDValue value)
  This method is member of the Container objects.
- fixed errors in conversion of X-definition to XML schema (in declared types)

# Version 32.2.1, release-date 2019-02-25
- fixed X-component bug when the prefix of the attribute namespace differs
  from the prefix in the model. The method toXml() returns not then correct
  result.
- Now is available the technology of X-lexicon (see the document
  xdef-32_Lexicon.pdf).

# Version 32.1.3, release-date 2019-02-04
- corrected bug in the construction mode. Te initialization of variables
  in the X-script section "var" in the model of element was not invoked
  correctly.
- corrected setting of stop addresses in the GUI of debugger.
- X-definition validation methods with the prefix "xs:" are deprecated. You can
  use the name without this prefix (ie. "xs:string" should be "string").

# Version 3.2.1.2, release-date 2019-01-21
- corrected backward compatibility of the validation type declaration in
  the X-definition version 2.0 (the keyword "parse:")
- corrected the bug if the missing element in the model is followed by
  optional elements which are also missing.
- corrected the bug in construction mode. It was not invoked the
  initialization section in the section "var" in the X-script of 
  an element model.

# Previous versions
- the previous versions were distributed by the site www.synntea.cz.
  in this version were renamed the packages "cz.synte.xdef" to "org.xdef"
