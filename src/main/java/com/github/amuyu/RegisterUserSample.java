package com.github.amuyu;

import com.github.amuyu.fabric.Client;
import com.github.amuyu.fabric.ca.FabricCAService;
import com.github.amuyu.fabric.ca.NCAInfo;
import com.github.amuyu.fabric.ca.Registar;
import com.github.amuyu.fabric.util.FileUtils;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Fabric-CA 에 사용자 계정을 등록하고 msp 를 저장하는 샘플
 */
public class RegisterUserSample {

    private static Logger logger = LoggerFactory.getLogger(RegisterUserSample.class);


    public static void main(String[] args) throws Exception {

        NCAInfo caInfo = new NCAInfo.Builder()
                .name(Config.caName)
                .url(Config.caUrl)
                .pemFile(Config.caPemFilePath)
                .allowAllHostNames(true)
                .build();
        FabricCAService caService = new FabricCAService(caInfo);
        HFCAInfo info = caService.info();
        logger.debug("info:{}", info);

        // admin 계정 호출
        String json = "";
        try {
            json = FileUtils.read(Config.mspFolder + "/ca-admin.json");
        } catch (IOException e) {
            logger.error("CAAdminEnrollSample 을 호출해서 admin 인증서를 받으세요", e);
            return;
        }

        // peer certificates 등
        Registar registar = Registar.fromJson(json);
        String mspId = Config.userMspId;
        String affiliation = Config.userAffiliation;
        Client client = caService.register(mspId, affiliation, registar);
        String clientJson = client.toJson();
        FileUtils.write(Config.user1MspPath, clientJson);
    }
}
