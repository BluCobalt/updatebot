package dev.blucobalt;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public abstract class Updater
    implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger("Updater");
    private final Path downloadPath = Path.of("latest.bin");
    private final JsonConfig config;
    private Process process;

    private String runningVersion;

    public Updater(JsonConfig config) {
        this.config = config;
        this.consumeConfig(config);
    }

    protected abstract void consumeConfig(JsonConfig config);

    protected abstract String getLatestVersion();

    protected abstract URL getDownloadURL(String version) throws IOException;


    protected abstract String getDownloadName(String version);

    @SneakyThrows
    private boolean update0(String version)
    {
       try {
           URL downloadURL = this.getDownloadURL(version);
           Files.copy(downloadURL.openStream(), downloadPath, REPLACE_EXISTING);
       } catch (IOException e) {
           LOGGER.error("Failed to download update", e);
           e.printStackTrace();
           return false;
       }
        // stop any existing process
        if (this.process != null && this.process.isAlive())
        {
            if (this.process.supportsNormalTermination())
            {
                LOGGER.info("waiting ${String.valueOf(this.config.gracefulShutdownTimeout)} seconds for graceful shutdown");
                this.process.destroy();
                this.process.children().forEach(ProcessHandle::destroy);
                this.process.waitFor(this.config.updateInterval, TimeUnit.SECONDS);
            }

            if (this.process.isAlive())
            {
                while (this.process.isAlive())
                {
                    LOGGER.info("forcibly terminating process");
                    this.process.destroyForcibly();
                    this.process.children().forEach(ProcessHandle::destroyForcibly);
                }
            }
        }

        switch(this.config.nameStrategy){
            case versionSubstitution:
            default:
                // change default name to match whatever matches in the config
                Files.move(downloadPath, downloadPath.resolveSibling(this.getDownloadName(version)), REPLACE_EXISTING);
                this.config.runArgs = Arrays.stream(this.config.runArgs).map(arg -> arg.replace("{version}", version)).toArray(String[]::new);
                break;
        }
        this.process = new ProcessBuilder(this.config.runArgs).inheritIO().start();
        return this.process.isAlive();
    }

    public void update(String version)
    {
        LOGGER.info("downloading version $version");
        if (this.update0(version)) {
            LOGGER.info("successfully updated to version $version");
            this.runningVersion = version;
        } else {
            LOGGER.error("failed to update to version $version with exit code " + this.process.exitValue());
            LOGGER.warn("trying to restore previous version");
            if (this.update0(this.runningVersion)) {
                LOGGER.info("successfully restored to version " + this.runningVersion);
            } else {
                LOGGER.error("failed to restore to version " + this.runningVersion + " with exit code " + this.process.exitValue());
                LOGGER.error("please manually restore to version " + this.runningVersion);
                LOGGER.fatal("could not update or restore to previous version");
                System.exit(1);
            }
        }
    }

    public boolean checkAndUpdate()
    {
        String latestVersion = this.getLatestVersion();
        if (latestVersion == null)
        {
            LOGGER.error("failed to get latest version");
            return false;
        }
        if (this.runningVersion == null || !this.runningVersion.equals(latestVersion))
        {
            LOGGER.info("updating to $latestVersion");
            this.update(latestVersion);
            this.runningVersion = latestVersion;
            return true;
        }
        return false;
    }

    int updateInterval = 0;

    @Override
    public void run()
    {
        if (this.checkAndUpdate()) {
            LOGGER.info("updated to ${this.runningVersion}");
        }
        updateInterval++;
        if (updateInterval == 100)
        {
            LOGGER.info("sanity check - 100 update intervals have passed");
            updateInterval = 0;
        }
    }
}
