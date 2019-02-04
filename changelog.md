#Version 32.1.3, release-date 2019-02-04
- corrected bug in the construction mode. Te initialization of variables
  in the X-script section "var" in the model of element was not invoked
  correctly.
- corrected setting of stop addresses in the GUI of debugger.
- X-definition validation methods with the prefix "xs:" are deprecated. You can
  use the name without this prefix (ie. "xs:string" should be "string").

#Version 3.2.1.2, release-date 2019-01-21
- corrected backward compatibility of the validation type declaration in
  the X-definition version 2.0 (the keyword "parse:")
- corrected the bug if the missing element in the model is followed by
  optional elements which are also missing.
- corrected the bug in construction mode. It was not invoked the
  initialization section in the section "var" in the X-script of 
  an element model.

previous versions
======================
- the previous versions were distributed by the site www.synntea.cz.
  in this version were renamed the packages "cz.synte.xdef" to "org.xdef"
