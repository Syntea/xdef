package org.xdef.sys.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Class to read info from pom.xml (see maven). That's: <ul>
 * <li>project.groupId
 * <li>project.artifactId
 * <li>project.version
 * <li>project.name
 * <li>project.description
 * <li>properties.release.date - user's property
 * <li>maven.build.timestamp   - build timestamp
 * </ul>
 * Load info from resource "pominfo.properties" that is automatically filled
 * by maven-plugin build/resources/resource/filtering
 * <p>
 * All items must be one-lined or in the format of java.util.Properties.
 */
public class PomInfo {
    private String groupId             = null;
    private String artifactId          = null;
    private String version             = null;
    private String name                = null;
    private String description         = null;
    private String releaseDate         = null;
    private String buildTimestamp      = null;
    private String gitTags             = null;
    private String gitBranch           = null;
    private String gitDirty            = null;
    private String gitCommitId         = null;
    private String gitCommitIdAbbrev   = null;
    private String gitCommitTime       = null;
    private static final String     POMINFOPROPSNAME = "pominfo.properties";
    /** singleton instance */
    public static final PomInfo     POMINFO          = new PomInfo();

    /** Create PomInfo instance - load pominfo.properties. */
    public PomInfo() {
        try {
            InputStream ppIs = PomInfo.class.getResourceAsStream(POMINFOPROPSNAME);
            if (ppIs == null) {
                throw new FileNotFoundException("java-resource " + POMINFOPROPSNAME + " not found");
            }
            loadProps(ppIs);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadProps(InputStream ppIs) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(ppIs,StandardCharsets.UTF_8));
        } finally {
            ppIs.close();
        }
        loadProps(properties);
    }

    private void loadProps(Properties pp) {
        groupId           = pp.getProperty("project.groupId");
        artifactId        = pp.getProperty("project.artifactId");
        version           = pp.getProperty("project.version");
        name              = pp.getProperty("project.name");
        description       = pp.getProperty("project.description");
        releaseDate       = pp.getProperty("release.date");
        buildTimestamp    = pp.getProperty("build.timestamp");
        gitTags           = pp.getProperty("git.tags");
        gitBranch         = pp.getProperty("git.branch");
        gitDirty          = pp.getProperty("git.dirty");
        gitCommitId       = pp.getProperty("git.commit.id");
        gitCommitIdAbbrev = pp.getProperty("git.commit.id.abbrev");
        gitCommitTime     = pp.getProperty("git.commit.time");
    }

    public boolean isVersionSnapshot() {
        return version.endsWith("-SNAPSHOT");
    }

    /** @return detailed identifier of the build - artifact-name, version, git-commit-info (mainly unexpected values) */
    public String getBuildIdentifier() {
        boolean tagOK    = ("version/" + getVersion()).equals(gitTags);
        boolean branchOK = "main".equals(gitBranch);
        return
            groupId + ":" + artifactId + ":" + version + " (" +
            (isVersionSnapshot() ? "built " + buildTimestamp : "released " + releaseDate) +
            (gitCommitIdAbbrev.isEmpty() ? "" :
                (tagOK || gitTags.isEmpty()      ? "" : ", tags: " + gitTags) +
                (tagOK || !gitTags.isEmpty()     ? "" : ", commit " + gitCommitIdAbbrev + " " + gitCommitTime) +
                (branchOK || gitBranch.isEmpty() ? "" : ", branch: " + gitBranch) +
                (!"true".equals(gitDirty)        ? "" : ", dirty-commit")
            ) +
            ")"
        ;
    }

    public String getGroupId() {return groupId;}

    public String getArtifactId() {return artifactId;}

    public String getVersion() {return version;}

    public String getName() {return name;}

    public String getDescription() {return description;}

    public String getReleaseDate() {return releaseDate;}

    public String getBuildTimestamp() {return buildTimestamp;}

    public String getGitTags() {return gitTags;}

    public String getGitBranch() {return gitBranch;}

    public String getGitDirty() {return gitDirty;}

    public String getGitCommitId() {return gitCommitId;}

    public String getGitCommitIdAbbrev() {return gitCommitIdAbbrev;}

    public String getGitCommitTime() {return gitCommitTime;}
}