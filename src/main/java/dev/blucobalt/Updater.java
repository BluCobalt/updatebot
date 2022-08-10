package dev.blucobalt;


import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class Updater
{
    private static Logger LOGGER = LogManager.getLogger("UpdaterV2");
    private static int running = 0;
    private static Process process = null;

    public static void main(String[] args)
            throws IOException, ParserConfigurationException, SAXException
    {
        // I cant figure out how to set the level to info non-programmatically
        Configurator.setRootLevel(Level.INFO);
        JsonConfig config = null;
        try
        {
            Path configPath = Paths.get(System.getProperty("updater.config", "updater.json"));
            LOGGER.info("Loading from: " + configPath.toAbsolutePath());
            config = new Gson().fromJson(Files.newBufferedReader(configPath), JsonConfig.class);
        } catch (IOException e)
        {
            LOGGER.error("Couldn't read config file", e);
            System.exit(1);
        }
        // I'll add more later
        //noinspection SwitchStatementWithTooFewBranches
        switch (config.resolutionStrategy)
        {
            case mavenRepository:
                maven(config);
                break;
            default:
                LOGGER.error("Unknown resolution strategy: " + config.resolutionStrategy);
                System.exit(1);
        }
    }

    private static void maven(JsonConfig config)
            throws IOException, ParserConfigurationException, SAXException
    {
        // if the url specified in the config doesnt have a trailing slash, add one and then just add the groupid
        // and artifactid and the maven-metadata.xml
        URL metadataXml = new URL(
                ((config.url.charAt(config.url.length() - 1) == '/') ? config.url : config.url + "/") +
                        config.groupId.replace(".", "/") + "/" + config.artifactId + "/maven-metadata.xml");
       Timer timer = new Timer();
       timer.schedule(new TimerTask()
       {
           @Override
           public void run()
           {
               try {
                   InputStream versionStream = metadataXml.openConnection().getInputStream();
                   DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                   Document document = db.parse(versionStream);
                   versionStream.close();
                   String rawVersion = document.getElementsByTagName("latest").item(0).getTextContent();
                   int version = Integer.parseInt(rawVersion.replaceAll("\\D", ""));
                   if (version > running)
                   {
                       LOGGER.info("New version available: " + version);
                       URL download = new URL(
                               ((config.url.charAt(
                                       config.url.length() - 1) == '/') ? config.url : config.url + "/") +                         // https://maven.legacyfabric.net/
                                       config.groupId.replace(".",
                                               "/") + "/" + config.artifactId + "/" +                                    // https://maven.legacyfabrcic.net/net/legacyfabric/fabric-meta
                                       rawVersion +                                                                                // https://maven.legacyfabrcic.net/net/legacyfabric/fabric-meta/1.6.7
                                       "/" + config.artifactId + "-" + rawVersion + ".jar"
                               // https://maven.legacyfabrcic.net/net/legacyfabric/fabric-meta/1.6.7/fabric-meta-1.6.7.jar
                       );
                       InputStream downloadStream = download.openConnection().getInputStream();
                       while (process != null && process.isAlive())
                       {
                           process.destroy();
                           LOGGER.warn("Waiting for process to die before updating");
                       }
                       try
                       {
                           Files.copy(downloadStream, Paths.get(config.artifactId + "-" + rawVersion + ".jar"));
                       } catch (FileAlreadyExistsException ignored)
                       {}
                       downloadStream.close();
                       String artifact = config.artifactId + "-" + rawVersion + ".jar";
                       LOGGER.info("Downloaded: " + artifact);
                       ProcessBuilder pb = new ProcessBuilder(
                               Arrays.stream(config.runArgs).map(s -> s.replace("{artifact}", artifact))
                                       .toArray(String[]::new));
                       pb.inheritIO();
                       Updater.process = pb.start();
                       Updater.running = version;
                   }
               } catch (IOException | SAXException | ParserConfigurationException e) {
                   LOGGER.error("There was an exception trying to run the update", e);
               }
           }
       }, 0, config.updateInterval * 1000L);
    }
}
