package org.xdef.xon;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.xdef.msg.JSON;
import org.xdef.sys.SRuntimeException;

/** Tools for YAML.
 * Processing YAML objects requires the org.yaml.snakeyaml package
 * in the classpath.
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
				throw new RuntimeException(
					"Error when creating instance of org.yaml.snakeyaml.Yaml",
					ex);
			}
		}
	}

	public static final String toYamlString(final Object o) {
		prepareYAML();
		try {
			return (String) _yamlDump.invoke(_yaml, XonUtils.xonToJson(o));
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause() != null
				&& ex.getCause().getMessage() != null ? ex.getCause() : ex);
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
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause() != null
				&& ex.getCause().getMessage() != null ? ex.getCause() : ex);
		}
	}

	public static final Object parseYAML(final InputStream source) {
		prepareYAML();
		try {
			return _yamlLoadInputStream.invoke(_yaml, source);
		} catch (IllegalAccessException | IllegalArgumentException
			| InvocationTargetException ex) {
			throw new RuntimeException(ex.getCause() != null
				&& ex.getCause().getMessage() != null ? ex.getCause() : ex);
		}
	}
	
	/** Convert YAML source format of data X-definition model to JSON format. 
	 * @param yaml string with YAML format of data X-definition model.
	 * @return JSON format of data X-definition model.
	 */
	public static final String yamlToJsonXScript(final String yaml) {
		Object x = XonUtils.parseYAML(yaml);
		String s = XonUtils.toJsonString(XonUtils.xonToJson(x), true);
		String[] keys = {"%anyName", "%anyObj", "%script", "%oneOf"};
		for (String key : keys) {
			int ndx; // index of the key in the source
			while ((ndx = s.indexOf("\"" + key)) >= 0) {
				int ndx1 = ndx + key.length() + 1;
				if (ndx1 >= s.length()) break;
				switch(s.charAt(ndx1++)) {
					case '=':
						s = s.substring(0, ndx) + key+"=\"" + s.substring(ndx1);
						break;
					case '"':
						s = ("%script".equals(key) && ndx1 < s.length()
							&& s.charAt(ndx1) == ':') //%script: -> %script=
							? s.substring(0,ndx) + key+"=" + s.substring(++ndx1)
							: s.substring(0, ndx) + key + s.substring(ndx1);
				}
			}
		}
		return s.endsWith("\n") ? s : (s + '\n');
	}
}