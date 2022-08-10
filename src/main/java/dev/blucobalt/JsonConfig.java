package dev.blucobalt;


public class JsonConfig
{
    public ResolutionStrategy resolutionStrategy;
    public String url;
    public int updateInterval;
    public String[] runArgs;

    // maven specific
    public String groupId;
    public String artifactId;
}

enum ResolutionStrategy
{
    mavenRepository
}
