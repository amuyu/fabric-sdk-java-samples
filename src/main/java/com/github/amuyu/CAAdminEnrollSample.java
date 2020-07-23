package com.github.amuyu;

import com.github.amuyu.fabric.ca.FabricCAService;
import com.github.amuyu.fabric.ca.NCAInfo;
import com.github.amuyu.fabric.ca.NUser;
import com.github.amuyu.fabric.ca.Registar;
import com.github.amuyu.fabric.util.FileUtils;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric-CA admin 계정을 enroll 하는 샘플
 *
 */
public class CAAdminEnrollSample {

    private static Logger logger = LoggerFactory.getLogger(CAAdminEnrollSample.class);

    public static void main(String[] args) throws Exception {
        NCAInfo caInfo = new NCAInfo.Builder()
                .name(Config.caName)
                .url(Config.caUrl)
                .pemFile(Config.caPemFilePath)
                .allowAllHostNames(true)
                .build();
        FabricCAService caService = new FabricCAService(caInfo);
        HFCAInfo info = caService.info();
        logger.debug("caName:{}, version:{}", info.getCAName(), info.getVersion());

        // admin 계정 enroll
        NUser admin = NUser.builder()
                .name(Config.caAdminName)
                .secret(Config.caAdminSecret)
                .build();
        Registar registar = caService.registarEnroll(admin);
        String adminJson = registar.toJson();
        FileUtils.write(Config.mspFolder + "/ca-admin.json", adminJson);
    }

}
