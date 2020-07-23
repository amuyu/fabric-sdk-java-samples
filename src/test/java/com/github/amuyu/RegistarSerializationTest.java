package com.github.amuyu;

import com.github.amuyu.fabric.ca.NEnrollment;
import com.github.amuyu.fabric.ca.Registar;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyPair;

public class RegistarSerializationTest {

    @Test
    public void serializeAndDeserialize() throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        KeyPair keyPair = cryptoSuite.keyGen();
        X509Enrollment enrollment = new X509Enrollment(keyPair, "cert");
        NEnrollment ne = NEnrollment.builder()
                .key(enrollment.getKey())
                .cert(enrollment.getCert())
                .build();

        Registar registar = Registar.builder()
                .name("admin")
                .enrollment(ne)
                .build();

        String json = registar.toJson();

        Registar rr = Registar.fromJson(json);
        Assert.assertEquals(registar, rr);

    }

}
