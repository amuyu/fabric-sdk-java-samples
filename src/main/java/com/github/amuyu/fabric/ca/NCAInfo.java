package com.github.amuyu.fabric.ca;

import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.NetworkConfig;

/**
 * {@link org.hyperledger.fabric_ca.sdk.HFCAClient} instance 를 생성하기 위해 필요한 정보
 * {@link NetworkConfig.CAInfo}
 */
@Getter
@Setter
public class NCAInfo {
    private String name;
    private String url;
    private boolean allowAllHostNames;
    private String pemFile;

    private NCAInfo(Builder builder) {
        setName(builder.name);
        setUrl(builder.url);
        setAllowAllHostNames(builder.allowAllHostNames);
        setPemFile(builder.pemFile);
    }

    public static final class Builder {
        private String name;
        private String url;
        private boolean allowAllHostNames = false;
        private String pemFile = null;

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

        public Builder allowAllHostNames(boolean val) {
            allowAllHostNames = val;
            return this;
        }

        public Builder pemFile(String val) {
            pemFile = val;
            return this;
        }

        public NCAInfo build() {
            return new NCAInfo(this);
        }
    }
}
