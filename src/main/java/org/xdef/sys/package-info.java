/**
 * Support for reporting, exceptions, parsing tools
 * and collection of useful classes.
 *
 * <H2>Reports, report files and exceptions</H2>
 * Provides flexible tool for reporting with multi language support. The
 * reports are instances of {@link org.xdef.sys.Report}.
 * The report objects have the attribute "type" which classifies the type
 * of report (such as "ERROR", WARNINING, INFO etc). The report object
 * may be "registered" - i.e. the report may have the ID (the string,
 * which identifies the report. If the ID is specified, the text of
 * report may be found in the report table and report table may contain
 * localized texts in different languages. The text of reports may is
 * modified by parameters specified in the report text. If the
 * report ID is <code>null</code> (the unregistered report) or if the report
 * ID is not found in report table the "default" report text specified
 * by the constructor of <code>Report</code> object. If the localized report
 * for given language is not found it is used the English version of the
 * report.The default language is set as to the static variable assigned
 * to <tt>Report</tt> object at the initialization time and it is taken
 * from system properties of Java virtual machine. However, the user can
 * set it by the static method
 * {@link org.xdef.sys.Report#setLanguage(String)}. The language
 * ID may be two letters accodring to either <code>ISO-639</code> or three
 * letters according to <code>ISO-639-2</code> specifications (see method
 * {@link org.xdef.sys.SUtils#getISO3Language()} and
 * {@link org.xdef.sys.SUtils#getISO3Language(String)}.
 * The files with localized report texts are in the XML format. Each
 * report is declared in the separated element whith the tag name equal
 * to report ID. The text of report is in the attribute which has name
 * equal to three letters <code>ISO-639-2</code> code. There are two
 * possibilities how to maintain the files with localized report texts:
 *
 * <UL>
 * <li>
 * all localized report texts are in one file.
 * <p>Example:</p>
 * <pre><code><b>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;messages&gt;
 * &lt;MSG012 eng="This is a message" deu="Das ist ein Nachricht" /&gt;
 * &lt;/messages&gt;
 * </b></code></pre>
 * </li>
 *
 * <li>
 * <p>localized report are separated to different documents. The name
 * of each document in this case MUST end with characters "_"lng".xml"
 * where lng is <code>ISO-639-2</code> of given language. This helps to
 * maintain the localized reports separately and enables to add some
 * localization separately. Also the size of report files are smaller and
 * its processing is faster.</p>
 * The reports from the above example are separated to two files, say
 * "<code>msg_<b>eng</b>.xml</code>" with English version:
 * <pre><code><b>
 * &lt;?xml version="1.0" encoding="ASCII"?&gt;
 * &lt;messages&gt;
 * &lt;MSG012 eng="This is a message" /&gt;
 * &lt;/messages&gt;
 * </b></code></pre>
 * and "<code>msg_<b>deu</b>.xml</code>" with German version:
 * <pre><code><b>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;messages&gt;
 * &lt;MSG012 deu="Das ist ein Nachricht" /&gt;
 * &lt;/messages&gt;
 * </b></code></pre>
 *
 * Report objects can be converted to strings
 *
 * <li>
 * thrown as a parameter of exceptions (see
 * {@link org.xdef.sys.SException},
 *   {@link org.xdef.sys.SRuntimeException},
 * {@link org.xdef.sys.SError} or
 * {@link org.xdef.sys.SExternalError}),
 * </li>
 * <li>written to the output stream (see
 * {@link org.xdef.sys.ReportWriter}). The files created by ReportWriter
 * may be read with {@link org.xdef.sys.ReportReader}.
 * <p>The classes providing reading and writing of reports are
 * {@link org.xdef.sys.FileReportReader} and
 * {@link org.xdef.sys.FileReportWriter}. See also
 * {@link org.xdef.sys.ArrayReporter} which provides both
 * read and write interface and it is useful as th temporary buffer of
 * reports.</p>
 * </li>
 *
 * </UL>
 *
 * <H2>RegisterReportTables</H2>
 * Creates Java source code of registered report tables from XML document.
 * See {@link org.xdef.sys.RegisterReportTables}.
 *
 * <H2>Parsing tools</H2>
 * Parsing of data is provided with implementation of the SParser interface
 * (see {@link org.xdef.sys.SParser}).There is available the
 * class implementing methods for strings, streams or files
 * (see {@link org.xdef.sys.StringParser}). It provides
 * number of methods for error and reporting connected with the
 * possibility to give to user the information about the source position.
 * Complex syntax of source data you can describe and process with
 * extended Backus Naur form (see {@link org.xdef.sys.BNFGrammar}).
 *
 * <H2>Collection of system utilities methods</H2>
 * Collection of useful methods which help the user to cooperate with the
 * different operating systems independent way. Methods are designed as
 * static methods of the classes {@link org.xdef.sys.SUtils}
 * and {@link org.xdef.sys.FUtils}.
 *
 * <H2>Serializing of objects</H2>
 * Primitive values and objects (e.g. Report, SDatetime, SDuration etc.)
 * can be written to an Ouput stream by the class
 * {@link org.xdef.sys.SObjectWriter} and you car read them by
 * {@link org.xdef.sys.SObjectReader}.
 * <p>For generation of unique IDs you can use for multi VM</p>
 *
 */
package org.xdef.sys;