package dev.blucobalt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MavenUpdater extends Updater {
    private static final Logger LOGGER = LogManager.getLogger("MavenUpdater");

    private String repositoryURL;
    private String groupId;
    private String artifactId;

    public MavenUpdater(JsonConfig config) {
        super(config);
    }

    private static String getLatestArtifactVersion(String repositoryUrl, String groupId, String artifactId)
    {
        // remove any trailing slash
        if (repositoryUrl.endsWith("/")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 1);
        }

        // Construct the URL to the Maven metadata file for the artifact
        String metadataUrl = repositoryUrl + "/" + groupId + "/" + artifactId + "/maven-metadata.xml";

        try {
            // Fetch the Maven metadata file from the repository
            URL url = new URL(metadataUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();

            // Parse the Maven metadata file to extract the latest version of the artifact
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);

            NodeList versionNodes = document.getElementsByTagName("latest");

            // Return the latest version of the artifact
            return versionNodes.item(0).getTextContent();
        } catch (Exception e) {
            LOGGER.error(e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void consumeConfig(JsonConfig config) {
        this.repositoryURL = config.url;
        this.groupId = config.groupId;
        this.artifactId = config.artifactId;
    }

    @Override
    public String getLatestVersion() {
        return getLatestArtifactVersion(this.repositoryURL, this.groupId, this.artifactId);
    }

    @Override
    public URL getDownloadURL(String version) {
        try {
            // remove trailing slash from repositoryURL if it has one, and just append groupId, artifactId, and version
            String download = (this.repositoryURL.endsWith("/") ? this.repositoryURL.substring(0, this.repositoryURL.length() - 1) : this.repositoryURL)
                    + "/" + groupId + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
            return new URL(download);
        } catch (Exception e) {
            LOGGER.error(e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String getDownloadName(String version) {
        return this.artifactId + "-" + version + ".jar";
    }
}
