package com.github.amuyu.fabric.ca;

import com.github.amuyu.fabric.Client;
import com.github.amuyu.fabric.exception.FabricCAException;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InfoException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Fabric CA Client 서비스 제공 Class
 * register, enroll 등 호출해 MSP 를 발급받음
 */
public class FabricCAService {
    private Logger logger = LoggerFactory.getLogger(FabricCAService.class);
    private HFCAClient caClient;

    public FabricCAService(NCAInfo caInfo) throws FabricCAException {
        Properties properties = new Properties();
        if (caInfo.isAllowAllHostNames()) {
            properties.put("allowAllHostNames", "true");
        }

        if (caInfo.getPemFile() != null) {
            properties.put("pemFile", caInfo.getPemFile());
        }

        try {
            caClient = HFCAClient.createNewInstance(caInfo.getName(), caInfo.getUrl(), properties);
        } catch (MalformedURLException | InvalidArgumentException e) {
            throw new FabricCAException(e.getMessage());
        }

        try {
            caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                CryptoException | org.hyperledger.fabric.sdk.exception.InvalidArgumentException |
                NoSuchMethodException | InvocationTargetException e) {
            throw new FabricCAException(e.getMessage());
        }
    }

    /**
     * {@link HFCAClient} instance return
     *
     * @return
     */
    public HFCAClient getClient() {
        return caClient;
    }

    public HFCAInfo info() throws FabricCAException {
        try {
            return caClient.info();
        } catch (InfoException | InvalidArgumentException e) {
            throw new FabricCAException(e.getMessage());
        }
    }

    /**
     * Fabric-CA server 등록자 enroll
     *
     * @param enrollment
     * @return
     * @throws FabricCAException
     */
    public Registar registarEnroll(NUser enrollment) throws FabricCAException {
        try {
            Enrollment e = caClient.enroll(enrollment.getName(), enrollment.getSecret());
            return Registar.builder()
                    .name(enrollment.getName())
                    .enrollment(NEnrollment.of(e))
                    .build();
        } catch (EnrollmentException | InvalidArgumentException e) {
            throw new FabricCAException(e.getMessage());
        }
    }

    /**
     * Fabric-CA 에 사용자 등록 후, MSP 발급
     *
     * @throws FabricCAException
     */
    public Client register(String mspId, String affiliation, Registar registar) throws FabricCAException {
        try {
            String name = mspId + "-api-" + System.currentTimeMillis();
            RegistrationRequest request = new RegistrationRequest(
                    name, affiliation);
            request.addAttribute(new Attribute("role", HFCAClient.HFCA_TYPE_CLIENT));
            String secret = caClient.register(request, registar);
            Enrollment e = enroll(NUser.builder()
                    .name(request.getEnrollmentID())
                    .secret(secret)
                    .build());
            return Client.builder()
                    .name(name)
                    .mspId(mspId)
                    .enrollment(NEnrollment.of(e))
                    .build();
        } catch (Exception e) {
            throw new FabricCAException(e.getMessage());
        }
    }

    /**
     * MSP 발급
     *
     * @param enrollment
     * @return
     * @throws FabricCAException
     */
    public Enrollment enroll(NUser enrollment) throws FabricCAException {
        try {
            return caClient.enroll(enrollment.getName(), enrollment.getSecret());
        } catch (EnrollmentException | InvalidArgumentException e) {
            throw new FabricCAException(e.getMessage());
        }
    }

}
