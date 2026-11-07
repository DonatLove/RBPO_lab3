package org.example.configuration;

import org.example.annotation.Cumponent;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Cumponent
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private String name;
    private String description;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
