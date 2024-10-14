package com.example.coconote.api.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexEntityMessage<T> {
    private Long workspaceId;
    private T entity;

    // ObjectMapper를 재사용하도록 static으로 선언
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT);

    // JSON 직렬화 메서드
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize IndexEntityMessage to JSON", e);
        }
    }

    // JSON 역직렬화 메서드 (제네릭 타입을 지원하도록 수정)
    public static <T> IndexEntityMessage<T> fromJson(String json, TypeReference<IndexEntityMessage<T>> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (RuntimeException | JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to IndexEntityMessage", e);
        }
    }
}
