package com.github.amuyu;

import java.io.File;

/**
 * 샘플을 실행하기 위해 필요한 설정 정보
 */
public class Config {
    public static String mspFolder = "./msp";
    public static String tlsFolder = "./tls";

    public static String caName = "ca-org1";
    public static String caUrl = "https://localhsst:7054";
    public static String caPemFile = "ca-org1.pem";
    public static String caPemFilePath = Config.tlsFolder + File.separator  + Config.caPemFile;

    public static String caAdminName = "admin";
    public static String caAdminSecret = "adminpw";

    public static String userMspId = "Org1MSP";
    public static String userAffiliation = "org1.department1";
    public static String user1MspName = "user1.json";
    public static String user1MspPath = Config.mspFolder + File.separator + Config.user1MspName;

    public static String peer0Org1Name = "peer0.org1.example.com";
    public static String peer0Org1PemFile = "peer0-org1.pem";
    public static String peer0Org1PemFilePath = Config.tlsFolder + File.separator  + Config.peer0Org1PemFile;
    public static String peer0Org1Url = "grpcs://localhost:7051";

    public static String peer0Org2Name = "peer0.org2.example.com";
    public static String peer0Org2PemFile = "peer0-org2.pem";
    public static String peer0Org2PemFilePath = Config.tlsFolder + File.separator  + Config.peer0Org2PemFile;
    public static String peer0Org2Url = "grpcs://localhost:9051";

    public static String ordererName = "orderer.example.com";
    public static String ordererUrl = "grpcs://localhost:7050";
    public static String ordererPemFile = "orderer.pem";
    public static String ordererPemFilePath = Config.tlsFolder + File.separator  + Config.ordererPemFile;

    public static String channelName = "mychannel";
}
