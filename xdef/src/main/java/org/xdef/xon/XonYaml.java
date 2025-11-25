package org.xdef.xon;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;

/** Tools for YAML. Processing YAML objects requires the org.yaml.snakeyaml package in the classpath.
 * @author Vaclav Trojan
 */
public final class XonYaml {
	private static Object _yaml;
	private static Constructor<?> _yamlConstructor;
	private static Method _yamlDump;
	private static Method _yamlLoadReader;
	private static Method _yamlLoadInputStream;

	/** Prepare YAML object, load  methods and dump method.
	 * @return instance of org.yaml.snakeyaml.Yaml object.
	 * @throws SRuntimeException if the package org.yaml.snakeyaml is not available.
	 */
	private static void prepareYAML() throws SRuntimeException {
		if (_yaml == null) {
			try {
				Class<?> _yamlClass = Class.forName("org.yaml.snakeyaml.Yaml");
				_yamlConstructor = _yamlClass.getConstructor();
				_yaml = _yamlConstructor.newInstance();
				_yamlDump = _yamlClass.getDeclaredMethod("dump", Object.class);
				_yamlLoadReader = _yamlClass.getDeclaredMethod("load", Reader.class);
				_yamlLoadInputStream = _yamlClass.getDeclaredMethod("load", InputStream.class);
			} catch (ClassNotFoundException ex) {
				//The package org.yaml.snakeyaml is not available. Please add it to classPath
				throw new SRuntimeException(JSON.JSON101);
			} catch (IllegalAccessException | IllegalArgumentException | InstantiationException
				| NoSuchMethodException | SecurityException | InvocationTargetException ex) {
				throw new RuntimeException("Error when creating instance of org.yaml.snakeyaml.Yaml", ex);
			}
		}
	}

	/** Create string from YAML object.
	 * @param o YAML object.
	 * @return string creasted from YAML object.
	 */
	public static final String toYamlString(final Object o) {
		prepareYAML();
		try {
			return (String) _yamlDump.invoke(_yaml, XonUtils.xonToJson(o));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(
				ex.getCause() != null && ex.getCause().getMessage() != null ? ex.getCause() : ex);
		}
	}

	/** Parse YAML string and return YAML object.
	 * @param source  string with YAML source or file name,
	 * @return parsed  YAML object.
	 */
	public static final Object parseYAML(final String source) {
		XonTools.InputData x = XonTools.getInputFromObject(source, null);
		return x._reader != null ? parseYAML(x._reader) : parseYAML(x._in);
	}

	/** Parse YAML source and return YAML object.
	 * @param source reader with YAML source,
	 * @return parsed  YAML object.
	 */
	public static final Object parseYAML(final Reader source) {
		prepareYAML();
		try {
			return _yamlLoadReader.invoke(_yaml, source);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause() != null && ex.getCause().getMessage() != null
				? ex.getCause() : ex);
		}
	}

	/** Parse YAML source and return YAML object.
	 * @param source input stram with YAML source,
	 * @return parsed  YAML object.
	 */
	public static final Object parseYAML(final InputStream source) {
		prepareYAML();
		try {
			return _yamlLoadInputStream.invoke(_yaml, source);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause() != null && ex.getCause().getMessage() != null
				? ex.getCause() : ex);
		}
	}
}