package com.ssafy.enjoyTrip.api.attraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoyTrip.api.attraction.dto.AiPlanResponseDto;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// AI 응답 양 끝 백틱 및 공백 제거 처리
@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseParser {
    private final ObjectMapper objectMapper;

    public AiPlanResponseDto parseResponse(String response) throws JsonProcessingException {
        String cleanedResponse = cleanJsonResponse(response);
        try {
            return objectMapper.readValue(cleanedResponse, AiPlanResponseDto.class);
        } catch (JsonProcessingException e) {

            String fixedJson = fixJsonSyntax(cleanedResponse);
            log.debug("Attempting to parse fixed JSON: {}", fixedJson);
            return objectMapper.readValue(fixedJson, AiPlanResponseDto.class);
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Response is null or empty");
        }

        String cleaned = response.trim();

        // 백틱 제거
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private String fixJsonSyntax(String json) {
        // 객체 끝의 콤마 제거 (}, 다음에 ] 또는 } 가 오는 경우)
        json = json.replaceAll("},\\s*([}\\]])", "}$1");

        // 배열 끝의 콤마 제거 (], 다음에 } 가 오는 경우)
        json = json.replaceAll("],\\s*}", "]}");

        return json;
    }

    // 디버깅을 위한 메서드
    public void logJsonStructure(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            log.info("JSON structure is valid");
            // 첫 번째 day의 구조를 로깅
            if (node.has("plans") && node.get("plans").isArray() &&
                    node.get("plans").size() > 0) {
                JsonNode firstDay = node.get("plans").get(0);
                log.info("First day structure: {}", firstDay.toPrettyString());
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON structure: {}", e.getMessage());
            // 에러 발생 위치 주변 컨텍스트 출력
            int errorOffset = (int)e.getLocation().getCharOffset();
            String context = getErrorContext(json, errorOffset);
            log.error("Context around error: {}", context);
        }
    }

    private String getErrorContext(String json, int errorOffset) {
        int contextSize = 50; // 에러 전후 50자
        int start = Math.max(0, errorOffset - contextSize);
        int end = Math.min(json.length(), errorOffset + contextSize);
        return "..." + json.substring(start, end) + "...";
    }
}