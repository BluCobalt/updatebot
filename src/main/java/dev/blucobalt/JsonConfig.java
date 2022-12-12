package dev.blucobalt;


public class JsonConfig
{
    public ResolutionStrategy resolutionStrategy;
    public NameStrategy nameStrategy;
    public String url;
    public int updateInterval;
    public String[] runArgs;

    public int gracefulShutdownTimeout;

    // maven specific
    public String groupId;
    public String artifactId;
}

enum ResolutionStrategy
{
    mavenRepository
}

enum NameStrategy
{
    versionSubstitution
}
