package com.github.amuyu.fabric.ca;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *  Fabric-CA server 에서 MSP 를 발급받기 위해 필요한 정보
 */
@Builder
@Getter
@Setter
public class NUser {
    private String name;
    private String secret;
}
