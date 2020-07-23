package com.github.amuyu;

import com.github.amuyu.fabric.Client;
import com.github.amuyu.fabric.FabricService;
import com.github.amuyu.fabric.NPeer;
import com.github.amuyu.fabric.NetworkConfig;
import com.github.amuyu.fabric.exception.BaseException;
import com.github.amuyu.fabric.exception.FabricServiceException;
import com.github.amuyu.fabric.util.FileUtils;
import org.hyperledger.fabric.sdk.Channel;

import java.io.IOException;

/**
 *  Hyperledger를 호출할 수 있는 FabricService 객체 생성 및 네트워크 정보 저장
 *
 */
public class SampleService {
    public NetworkConfig networkConfig;
    public FabricService fabricService;
    public Channel channel;

    SampleService(Client client, NetworkConfig networkConfig) throws FabricServiceException {
        this.fabricService = new FabricService(client);
        this.channel = fabricService.connectToChannel(networkConfig);
        this.networkConfig = networkConfig;
    }

    public static SampleService defaultCreate() throws BaseException, IOException {
        return defaultCreate(false);
    }

    public static SampleService defaultCreate(boolean single) throws BaseException, IOException {
        String userJson;
        try {
            userJson = FileUtils.read(Config.user1MspPath);
        } catch (IOException e) {
            throw new BaseException("msp를 확인해주세요 err:" + e.getMessage());
        }

        // peer0Org1
        NPeer peer0Org1 = NPeer.builder()
                .name(Config.peer0Org1Name)
                .url(Config.peer0Org1Url)
                .pemFile(Config.peer0Org1PemFilePath)
                .hostnameOverride(Config.peer0Org1Name)
                .build();

        // peer0Org2
        NPeer peer0Org2 = NPeer.builder()
                .name(Config.peer0Org2Name)
                .url(Config.peer0Org2Url)
                .pemFile(Config.peer0Org2PemFilePath)
                .hostnameOverride(Config.peer0Org2Name)
                .build();

        // orderer
        NPeer orderer = NPeer.builder()
                .name(Config.ordererName)
                .url(Config.ordererUrl)
                .pemFile(Config.ordererPemFilePath)
                .hostnameOverride(Config.ordererName)
                .build();

        NetworkConfig.Builder networkConfig = new NetworkConfig.Builder()
                .channelName(Config.channelName)
                .peer(peer0Org1)
                .orderer(orderer);

        if (!single) {
            networkConfig.peer(peer0Org2);
        }

        return new SampleService(Client.fromJson(userJson), networkConfig.build());
    }

}
