package dev.blucobalt;


import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Entrypoint {
    private static final Logger LOGGER = LogManager.getLogger("UpdaterV2");
    public static void main(String[] args)
            throws IOException {
        // I cant figure out how to set the level to info non-programmatically
        Configurator.setRootLevel(Level.INFO);
        JsonConfig config = null;
        try {
            Path configPath = Paths.get(System.getProperty("updater.config", "updater.json"));
            LOGGER.info("Loading from: " + configPath.toAbsolutePath());
            config = new Gson().fromJson(Files.newBufferedReader(configPath), JsonConfig.class);
        } catch (IOException e) {
            LOGGER.error("Couldn't read config file", e);
            System.exit(1);
        }

        Updater updater = null;

        // I'll add more later
        //noinspection SwitchStatementWithTooFewBranches
        switch (config.resolutionStrategy) {
            case mavenRepository:
                updater = new MavenUpdater(config);
                break;
            default:
                LOGGER.error("Unknown resolution strategy: " + config.resolutionStrategy);
                System.exit(1);
        }

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(updater, 0, config.updateInterval, TimeUnit.SECONDS);
    }
}
