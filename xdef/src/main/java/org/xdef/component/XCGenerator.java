package org.xdef.component;

import java.util.Map;

/** Interface for generators of X-components.
 * @author Vaclav Trojan
 */
interface XCGenerator {

	/** Generate XComponent Java source class from X-definition.
	 * @param model name of model.
	 * @param className name of generated class.
	 * @param extClass class extension.
	 * @param interfaceName name of interface
	 * @param packageName the package of generated class (may be null).
	 * @param components Map with components.
	 * @return String with generated Java source code.
	 */
	String genXComponent(final String model,
		final String className,
		final String extClass,
		final String interfaceName,
		final String packageName,
		final Map<String, String> components);

	/** Get StringBuilder with interface specifications. */
	StringBuilder getIinterfaces();
}