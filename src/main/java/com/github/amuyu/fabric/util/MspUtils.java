/******************************************************
 *  Copyright 2018 IBM Corporation
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.github.amuyu.fabric.util;


import ch.qos.logback.core.util.FileUtil;
import com.github.amuyu.fabric.ca.NEnrollment;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.exception.CryptoException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author Balaji Kadambi
 */

public class MspUtils {


    /**
     * Create enrollment from key and certificate files.
     *
     * @param
     * @param keyFileName
     * @param certFileName
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws CryptoException
     */
    public static NEnrollment getEnrollment(String keyFolderPath, String keyFileName, String certFolderPath, String certFileName)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey key;
        String certificate;

        try (InputStream isKey = new FileInputStream(keyFolderPath + File.separator + keyFileName);
             BufferedReader brKey = new BufferedReader(new InputStreamReader(isKey))) {

            StringBuilder keyBuilder = new StringBuilder();

            for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
                if (!line.contains("PRIVATE")) {
                    keyBuilder.append(line);
                }
            }

            certificate = new String(Files.readAllBytes(Paths.get(certFolderPath, certFileName)));

            byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("EC");
            key = kf.generatePrivate(keySpec);
        }

        return NEnrollment.builder()
                .key(key)
                .cert(certificate)
                .build();
    }

    public static void cleanUp() {
        String directoryPath = "network-resources/users";
        File directory = new File(directoryPath);
        deleteDirectory(directory);
    }

    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            assert children != null;
            for (File child : children) {
                boolean success = deleteDirectory(child);
                if (!success) {
                    return false;
                }
            }
        }

        // either file or an empty directory
        Logger.getLogger(FileUtil.class.getName()).log(Level.INFO, "Deleting - " + dir.getName());
        return dir.delete();
    }

}
