package io.quarkus.registry.app.maven;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;

public class ArtifactParser {
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

//quarkus-non-platform-extensions-1.0-SNAPSHOT-1.13.0.Final.json

    public static Artifact parseArtifact(List<String> pathSegmentList) {
        if (pathSegmentList.size() < 3) {
            throw new WebApplicationException("Coordinates are missing", Response.Status.BAD_REQUEST);
        }

        final String fileName = pathSegmentList.get(pathSegmentList.size() - 1);
        final String version = pathSegmentList.get(pathSegmentList.size() - 2);
        String artifactId = pathSegmentList.get(pathSegmentList.size() - 3);
        final StringBuilder builder = new StringBuilder();
        builder.append(pathSegmentList.get(0));
        for (int i = 1; i < pathSegmentList.size() - 3; ++i) {
            builder.append('.').append(pathSegmentList.get(i));
        }
        final String groupId = builder.toString();

        final String classifier;
        final String type;
        if (fileName.startsWith(MAVEN_METADATA_XML)) {
            type = fileName;
            classifier = "";
        } else if (fileName.startsWith(artifactId)) {
            int idxType;
            if (fileName.endsWith("sha1") || fileName.endsWith("md5")) {
                idxType = fileName.lastIndexOf('.', fileName.length() - 6);
            } else {
                idxType = fileName.lastIndexOf('.');
            }
            type = fileName.substring(idxType + 1);
            String remaining = fileName
                    .replace(artifactId + "-" + version, "")
                    .replace("." + type, "");
            if (!remaining.isEmpty()) {
                classifier = remaining.substring(1);
            } else {
                classifier = "";
            }
        } else {
            throw new WebApplicationException(
                    "Artifact file name " + fileName + " does not start with the artifactId " + artifactId,
                    Response.Status.BAD_REQUEST);
        }

        return new DefaultArtifact(groupId, artifactId, version, null, type, classifier, null);
    }
}
