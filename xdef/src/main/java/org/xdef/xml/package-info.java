/**
 * <H1>XML tools</H1>
 * <H2>KDOMBuilder</H2>
 * Tools for creating, parsing and processing of XML data.
 * E.g.:
 * <UL>
 *
 * <li>
 * <b><code>getDOMImplementation</code></b> get the
 * <code>org.w3c.dom.DOMImplementation</code>
 * </li>
 *
 * <li>
 * <b><code>newDocument</code></b> creating of new
 * <code>org.w3c.dom.Documment</code>
 * </li>
 *
 * <li>
 * <b><code>parse</code></b> parse the XML source data and return the
 * <code>org.w3c.dom.Documment</code>
 * </li>
 *
 * </UL>
 *
 * <H2>KNodeList</H2>
 * Implementation of org.w3c.dom.NodeList interface (possibility to add a Node).
 * <H2>KNamespaceImpl</H2>
 * Implementation of namespace.
 * <H2>KXpathExpr</H2>
 * Implementation of XPath.
 * <H2>KXqueryExpr</H2>
 * Implementation of Xquery.
 * <H2>KDOMUtils</H2>
 * Collection of static methods designed for easy work with XML objects.
 * <H2>KXmlUtils</H2>
 * Collection of static methods parsing, writing, printing and comparing
 * XML data. Eg:
 * <UL>
 *
 * <li>
 * parsing of source XML data and creating of org.w3c.dom.Document
 * (see methods parseXML(...)).
 * </li>
 *
 * <li>writing of XML data from a org.w3c.dom.Node
 * (see methods writeXML(...)).
 * </li>
 *
 * <li>
 * writing of XML data from a org.w3c.dom.Node to string.
 * (see methods nodeToString(...)).
 * </li>
 *
 * <li>
 * get printable form of XML node (see methods nodeToString(...)).
 * </li>
 *
 * <li>comparing of XML data (see methods compare...(A,B,...)).
 * methods designed to compare org.w3c.dom objects.
 * </li>
 *
 * </UL>
 */
package org.xdef.xml;