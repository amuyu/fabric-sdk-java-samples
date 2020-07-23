package com.github.amuyu.fabric;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 *  hyperledger network 설정 정보
 */
@Getter
@Setter
public class NetworkConfig {

    private String channelName;
    private List<Node> peers;
    private Node orderer;
    private Node eventHub;

    private NetworkConfig(Builder builder) {
        channelName = builder.channelName;
        peers = builder.peers;
        orderer = builder.orderer;
        eventHub = builder.eventHub;
    }

    public static final class Builder {
        private String channelName;
        private List<Node> peers = new ArrayList<>();
        private Node orderer;
        private Node eventHub;

        public Builder() {
        }

        public Builder channelName(String val) {
            channelName = val;
            return this;
        }

        public Builder peer(NPeer npeer) {
            peers.add(npeer.toNode());
            return this;
        }

        public Builder peer(String name, String url) {
            Node peer = new Node.Builder()
                    .name(name)
                    .url(url)
                    .build();
            peers.add(peer);
            return this;
        }

        public Builder orderer(NPeer npeer) {
            this.orderer = npeer.toNode();
            return this;
        }

        public Builder orderer(String name, String url) {
            orderer = new Node.Builder()
                    .name(name)
                    .url(url)
                    .build();
            return this;
        }

        public Builder eventHub(String name, String url) {
            eventHub = new Node.Builder()
                    .name(name)
                    .url(url)
                    .build();
            return this;
        }

        public Builder peer(Node val) {
            peers.add(val);
            return this;
        }

        public Builder orderer(Node val) {
            orderer = val;
            return this;
        }

        public Builder eventHub(Node val) {
            eventHub = val;
            return this;
        }

        public NetworkConfig build() {
            if (channelName == null) {
                throw new IllegalArgumentException("channelName is null");
            }
            if (peers == null || peers.isEmpty()) {
                throw new IllegalArgumentException("peer is null");
            }
            if (orderer == null) {
                throw new IllegalArgumentException("orderer is null");
            }

            for (Node peer : peers) {
                peerProperties(peer.getProperties());
            }
            ordererProperties(orderer.getProperties());

            return new NetworkConfig(this);
        }

        public Properties baseProperties(Properties properties) {
            properties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[]{1L, TimeUnit.MINUTES});
            properties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[]{30L, TimeUnit.SECONDS});
            properties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[]{true});
            return properties;
        }

        public Properties peerProperties(Properties properties) {
            Properties peerProperties = baseProperties(properties);
            peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 20000000);
            return peerProperties;
        }

        public Properties ordererProperties(Properties properties) {
            Properties peerProperties = baseProperties(properties);
            peerProperties.put("ordererWaitTimeMilliSecs", "90000");
            return peerProperties;
        }
    }
}
