package com.github.amuyu.fabric.ca;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bouncycastle.util.encoders.Base64;
import org.hyperledger.fabric.sdk.Enrollment;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

/**
 * {@link Enrollment} 를 구현 class
 * PrivateKey 가 있으므로 저장할 때, 암호화가 필요함
 */
@Builder
@Getter
@Setter
@ToString
public class NEnrollment implements Enrollment, Serializable {

    private static final long serialVersionUID = -5258228498456276306L;

    private PrivateKey key;
    private String cert;

    @Override
    public PrivateKey getKey() {
        return key;
    }

    @Override
    public String getCert() {
        return cert;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NEnrollment)) return false;
        NEnrollment that = (NEnrollment) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(cert, that.cert);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, cert);
    }

    public static NEnrollment of(Enrollment e) {
        return NEnrollment.builder()
                .key(e.getKey())
                .cert(e.getCert())
                .build();
    }

    public String serialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(NEnrollment.class, new Serializer());
        mapper.registerModule(module);
        return Base64.toBase64String(mapper.writeValueAsBytes(this));
    }

    public static NEnrollment deserialize(String base64) throws IOException, ClassNotFoundException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(NEnrollment.class, new DeSerializer());
        mapper.registerModule(module);
        return mapper.readValue(Base64.decode(base64), NEnrollment.class);
    }

    public static NEnrollment fromFile(String keyFilePath, String certFilePath)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        PrivateKey key;
        String certificate;

        try (InputStream isKey = new FileInputStream(keyFilePath);
             BufferedReader brKey = new BufferedReader(new InputStreamReader(isKey))) {

            StringBuilder keyBuilder = new StringBuilder();

            for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
                if (!line.contains("PRIVATE")) {
                    keyBuilder.append(line);
                }
            }

            certificate = new String(Files.readAllBytes(Paths.get(certFilePath)));

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

    public static class Serializer extends JsonSerializer<NEnrollment> {
        @Override
        public void serialize(NEnrollment value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("key", serialize(value.getKey()));
            gen.writeStringField("cert", value.getCert());
            gen.writeEndObject();
        }

        public String serialize(PrivateKey key) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(key);
            oos.flush();
            byte[] data = bos.toByteArray();
            bos.close();
            return Base64.toBase64String(data);
        }
    }

    public static class DeSerializer extends JsonDeserializer<NEnrollment> {

        @Override
        public NEnrollment deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();
            String key = node.get("key").asText();
            String cert = node.get("cert").asText();
            try {
                return NEnrollment.builder()
                        .key(deserialize(key))
                        .cert(cert)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }
        }

        PrivateKey deserialize(String key) throws IOException, ClassNotFoundException {
            byte[] data = Base64.decode(key);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (PrivateKey) ois.readObject();
        }
    }
}
