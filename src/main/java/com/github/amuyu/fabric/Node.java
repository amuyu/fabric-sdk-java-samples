package com.github.amuyu.fabric;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Properties;

@Getter
@Setter
@NoArgsConstructor
public class Node {
    private String name;
    private String url;
    private Properties properties;

    private Node(Builder builder) {
        this.name = builder.name;
        this.url = builder.url;
        this.properties = builder.properties == null ? new Properties() : (Properties) builder.properties.clone();
    }


    public static final class Builder {
        private String name;
        private String url;
        private Properties properties;

        public Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder properties(Properties val) {
            properties = val;
            return this;
        }

        public Node build() {
            if (name == null) {
                throw new IllegalArgumentException("name is null");
            }
            if (url == null) {
                throw new IllegalArgumentException("url is null");
            }
            return new Node(this);
        }
    }
}
