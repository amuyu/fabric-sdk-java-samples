package com.github.amuyu.fabric.ca;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.amuyu.fabric.util.FileUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.User;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Fabric-CA server 에 등록 권한을 가진 사용자 정보
 * <p>
 * {@link FabricCAService#register(String, String, Registar)} 요청 시 필요
 */
@Builder
@Getter
@Setter
public class Registar implements User {
    private String name;
    private NEnrollment enrollment;

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
    public String getMspId() {
        return null;
    }

    public void writeToFile(String path) throws IOException {
        FileUtils.write(path, toJson());
    }

    public static Registar fromFile(String path) throws IOException {
        String json = FileUtils.read(path);
        return fromJson(json);
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Registar.class, new Serializer());
        mapper.registerModule(module);
        return mapper.writeValueAsString(this);
    }

    public static Registar fromJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Registar.class, new Deserializer());
        mapper.registerModule(module);
        return mapper.readValue(json, Registar.class);
    }

    public static class Serializer extends JsonSerializer<Registar> {
        @Override
        public void serialize(Registar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getName());
            gen.writeStringField("enrollment", value.getEnrollment().serialize());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Registar> {
        @Override
        public Registar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode node = p.readValueAsTree();
            String name = node.get("name").asText();
            String en = node.get("enrollment").asText();
            try {
                NEnrollment enrollment = NEnrollment.deserialize(en);
                return Registar.builder()
                        .name(name)
                        .enrollment(enrollment)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registar)) return false;
        Registar registar = (Registar) o;
        return Objects.equals(name, registar.name) &&
                Objects.equals(enrollment, registar.enrollment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enrollment);
    }
}
