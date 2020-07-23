package com.github.amuyu.fabric;

import lombok.Builder;
import lombok.Getter;

import java.util.Properties;

@Builder
@Getter
public class NPeer {
    private String name;
    private String url;
    private String pemFile;
    private String hostnameOverride;

    Node toNode() {
        Properties properties = new Properties();
        if (hostnameOverride != null) {
            properties.setProperty("ssl-target-name-override", hostnameOverride);
            properties.setProperty("hostnameOverride", hostnameOverride);
        }
        if (pemFile != null) {
            properties.setProperty("pemFile", pemFile);
        }
        return new Node.Builder()
                .name(name)
                .url(url)
                .properties(properties)
                .build();
    }
}
