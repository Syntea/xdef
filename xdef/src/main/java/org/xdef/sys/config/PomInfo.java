package org.xdef.sys.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;


/**
 * Class to read info from pom.xml (see maven). That's: <ul>
 * <li>project.groupId</li>
 * <li>project.artifactId</li>
 * <li>project.version</li>
 * <li>project.name</li>
 * <li>project.description</li>
 * <li>properties.release.date - user's property</li>
 * </ul>
 * <p>
 * Load info from resource "pominfo.properties" that is automatically filled
 * by maven-plugin build/resources/resource/filtering
 * </p><p>
 * All items must be one-lined or in the format of java.util.Properties.
 * </p>
 */
public class PomInfo {


	/**
	 * init instance - load pominfo.properties
	 */
	public PomInfo() {
		try {
			InputStream ppIs = PomInfo.class.getResourceAsStream(pomInfoPropsName);

			if (ppIs == null) {
				throw new FileNotFoundException("java-resource " + pomInfoPropsName + " not found");
			}

			loadProps(ppIs);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}



	private void loadProps(InputStream ppIs) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(new InputStreamReader(ppIs, charset));
		} finally {
			ppIs.close();
		}

		loadProps(properties);
	}



	private void loadProps(Properties pp) {
		groupId     = pp.getProperty("project.groupId");
		artifactId  = pp.getProperty("project.artifactId");
		version     = pp.getProperty("project.version");
		name        = pp.getProperty("project.name");
		description = pp.getProperty("project.description");
		releaseDate = pp.getProperty("release.date");
	}



	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getReleaseDate() {
		return releaseDate;
	}



	/**
	 * @return product-identifier
	 */
	public String getProductIdentifier() {
		return artifactId + "(" + version + ", " + releaseDate + ")";
	}



	private String groupId        = null;
	private String artifactId     = null;
	private String version        = null;
	private String name           = null;
	private String description    = null;
	private String releaseDate    = null;

	private static final String     pomInfoPropsName = "pominfo.properties";
	/** default charset */
	private static final Charset    charset          = Charset.forName("UTF-8");

	/** singleton instance */
	public static final PomInfo     pomInfo          = new PomInfo();
}