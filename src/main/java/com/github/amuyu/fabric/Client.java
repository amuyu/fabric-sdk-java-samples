package com.github.amuyu.fabric;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.amuyu.fabric.ca.NEnrollment;
import com.github.amuyu.fabric.util.FileUtils;
import lombok.*;
import org.hyperledger.fabric.sdk.User;

import java.io.IOException;
import java.util.Set;

/**
 * Transaction invoke/query 하기 위한 사용자 정보
 *
 * {@link FabricCAService#register(String, String, Registar)} 를 호출해서 얻을 수 있다.
 * <p>
 * {@link User#userContextCheck(User)} 에서 확인하는 정보들로 구성
 */
@Builder
@Getter
@Setter
@ToString
public class Client implements User {

    @NonNull
    private String name;
    @NonNull
    private String mspId;
    @NonNull
    private NEnrollment enrollment;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getRoles() {
        return null;
    }

    @Override
    public String getAccount() {
        return null;
    }

    @Override
    public String getAffiliation() {
        return null;
    }

    @Override
    public NEnrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return mspId;
    }

    public void writeToFile(String path) throws IOException {
        FileUtils.write(path, toJson());
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Client.class, new Serializer());
        mapper.registerModule(module);
        return mapper.writeValueAsString(this);
    }

    /**
     * Json file 로부터 client 객체를 리턴
     * @param path client 정보가 담긴 json 파일
     * @return client 객체를 return
     * @throws IOException
     */
    public static Client fromFile(String path) throws IOException {
        String json = FileUtils.read(path);
        return fromJson(json);
    }

    /**
     * Json string 으로부터 client 객체를 리턴
     * @param json client 정보가 담긴 json string
     * @return client 객체를 return
     * @throws IOException
     */
    public static Client fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Client.class, new Deserializer());
        mapper.registerModule(module);
        return mapper.readValue(json, Client.class);
    }

    public static class Serializer extends JsonSerializer<Client> {
        @Override
        public void serialize(Client value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getName());
            gen.writeStringField("mspId", value.getMspId());
            gen.writeStringField("enrollment", value.getEnrollment().serialize());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Client> {
        @Override
        public Client deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();
            String name = node.get("name").asText();
            String mspId = node.get("mspId").asText();
            String en = node.get("enrollment").asText();
            try {
                NEnrollment enrollment = NEnrollment.deserialize(en);
                return Client.builder()
                        .name(name)
                        .mspId(mspId)
                        .enrollment(enrollment)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }
        }
    }
}
