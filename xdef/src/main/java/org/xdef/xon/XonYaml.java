package org.xdef.xon;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;

/** Tools for YAML.
 * Processing YAML objects requires the availability of the org.yaml.snakeyaml
 * package in the classpath.
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
	 * @throws SRuntimeException if the package org.yaml.snakeyaml
	 * is not available.
	 */
	private static void prepareYAML() throws SRuntimeException {
		if (_yaml == null) {
			try {
				Class<?> _yamlClass = Class.forName("org.yaml.snakeyaml.Yaml");
				_yamlConstructor = _yamlClass.getConstructor();
				_yaml = _yamlConstructor.newInstance();
				_yamlDump = _yamlClass.getDeclaredMethod("dump", Object.class);
				_yamlLoadReader =
					_yamlClass.getDeclaredMethod("load", Reader.class);
				_yamlLoadInputStream =
					_yamlClass.getDeclaredMethod("load", InputStream.class);
			} catch (ClassNotFoundException ex) {
				//The package org.yaml.snakeyaml is not available.
				//Please add it to classPath
				throw new SRuntimeException(JSON.JSON101);
			} catch (IllegalAccessException | IllegalArgumentException
				| InstantiationException | NoSuchMethodException
				| SecurityException | InvocationTargetException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public static final String toYamlString(final Object o) {
		prepareYAML();
		try {
			return (String) _yamlDump.invoke(_yaml, XonUtils.xonToJson(o));
		} catch (SRuntimeException ex) {
			throw ex;
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(
				"Error when creating Yaml string from object", ex);
		}
	}

	public static final Object parseYAML(final String source) {
		XonTools.InputData x = XonTools.getInputFromObject(source, null);
		return x._reader != null ? parseYAML(x._reader) : parseYAML(x._in);
	}

	public static final Object parseYAML(final Reader source) {
		prepareYAML();
		try {
			return _yamlLoadReader.invoke(_yaml, source);
		} catch (SRuntimeException ex) {
			throw ex;
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(
				"Error when parsing Yaml from Reader", ex);
		}
	}

	public static final Object parseYAML(final InputStream source) {
		prepareYAML();
		try {
			return _yamlLoadInputStream.invoke(_yaml, source);
		} catch (SRuntimeException ex) {
			throw ex;
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(
				"Error when parsing Yaml from InputStream", ex);
		}
	}
}