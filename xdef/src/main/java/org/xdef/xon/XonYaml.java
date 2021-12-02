package org.xdef.xon;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;

/** Tools for YAML.
 * Note for the full function it must be in the classpath available
 * the package org.yaml.snakeyaml.
 * @author Vaclav Trojan
 */
public final class XonYaml {
	private static Object _yaml = null;
	private static Constructor<?> _yamlConstructor = null;
	private static Method _yamlDump = null;
	private static Method _yamlLoadReader = null;
	private static Method _yamlLoadInputStream = null;

	/** Prepare YAML object, load  methods and dump method.
	 * @return instance of org.yaml.snakeyaml.Yaml object.
	 * @throws SRuntimeException if the library org.yaml.snakeyaml
	 * is not available.
	 */
	public static final Object prepareYAML() throws SRuntimeException {
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
			} catch (Exception ex) {
				//The package org.yaml.snakeyaml is not available.
				//Please add it to classPath
				throw new SRuntimeException(JSON.JSON101);
			}
		}
		return _yaml;
	}

	public static final String toYamlString(final Object o) {
		try {
			return (String) _yamlDump.invoke(_yaml, XonUtil.xonToJson(o));
		} catch (SRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(
				"Error when creating Yaml string from object", ex);
		}
	}

	public static final Object parseYAML(final String source) {
		return parseYAML((Reader) XonTools.getReader(source, null)[0]);
	}

	public static final Object parseYAML(final Reader source) {
		prepareYAML();
		try {
			return _yamlLoadReader.invoke(_yaml, source);
		} catch (SRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
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
		} catch (Exception ex) {
			throw new RuntimeException(
				"Error when parsing Yaml from InputStream", ex);
		}
	}
}