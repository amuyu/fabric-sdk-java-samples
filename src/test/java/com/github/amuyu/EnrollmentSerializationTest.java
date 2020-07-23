package com.github.amuyu;


import com.github.amuyu.fabric.ca.NEnrollment;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;

public class EnrollmentSerializationTest {


    @Test
    public void process() throws Exception {

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        KeyPair keyPair = cryptoSuite.keyGen();
        X509Enrollment enrollment = new X509Enrollment(keyPair, "cert");
        NEnrollment ne = NEnrollment.builder()
                .key(enrollment.getKey())
                .cert(enrollment.getCert())
                .build();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ne);
        oos.flush();
        byte[] data = bos.toByteArray();


        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        NEnrollment de = (NEnrollment) ois.readObject();

        Assert.assertEquals(ne, de);

    }

}
