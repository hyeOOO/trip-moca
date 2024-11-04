package com.ssafy.enjoyTrip.api.attraction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.enjoyTrip.api.attraction.dto.AiPlanResponseDto;
import com.ssafy.enjoyTrip.api.attraction.dto.AttractionListResponseDto;
import com.ssafy.enjoyTrip.api.attraction.dto.AttractionTitleDto;
import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.api.attraction.repository.AttractionListRepository;
import com.ssafy.enjoyTrip.api.util.PromptTemplateLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attraction")
public class AttractionApiController {
    private final ChatModel chatModel;
    private final PromptTemplateLoader promptLoader;
    private final AttractionListRepository attractionListRepository;

    @GetMapping("/ai/plan/{sido}/{day}")
    public ResponseEntity<List<DayPlanDto>> getAiPlan(@PathVariable("sido") String sido, @PathVariable("day") String day) {
        try {
            // 유저 프롬프트 템플릿 로드 및 변수 설정
            String userPromptTemplate = promptLoader.loadUserPrompt();
            PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
            userTemplate.add("sido", sido); // 시,도
            userTemplate.add("day", day); // 여행일수
            String userCommand = userTemplate.render();

            // 시스템 프롬프트 로드
            String systemPromptTemplate = promptLoader.loadSystemPrompt();
            PromptTemplate systemTemplate = new PromptTemplate(systemPromptTemplate);
            systemTemplate.add("day", day);
            String systemCommand = systemTemplate.render();

            // 메시지 생성
            Message userMessage = new UserMessage(userCommand);
            Message systemMessage = new SystemMessage(systemCommand);

            // API 호출
            String response = chatModel.call(userMessage, systemMessage);

            // 백틱 제거 및 JSON 파싱
            String cleanedResponse = cleanJsonResponse(response);
            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            AiPlanResponseDto planResponse = objectMapper.readValue(cleanedResponse, AiPlanResponseDto.class);

            // 각 일자별 계획에 대해 관광지 정보 매핑
            for (DayPlanDto dayPlan : planResponse.getPlans()) {
                List<String> attractionTitles = dayPlan.getAttractions().stream()
                        .map(AttractionTitleDto::getTitle)
                        .collect(Collectors.toList());

                // DB에서 관광지 정보 조회 및 DTO 변환
                List<AttractionListResponseDto> attractionDetails = attractionListRepository
                        .findByTitleIn(attractionTitles)
                        .stream()
                        .map(AttractionListResponseDto::fromEntity)
                        .collect(Collectors.toList());

                // 결과 매핑
                dayPlan.setAttractionDetails(attractionDetails);
            }

            return ResponseEntity.ok(planResponse.getPlans());

        } catch (Exception e) {
            log.error("Error processing attraction request for city: " + sido, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String cleanJsonResponse(String response) {
        log.debug("Original response: {}", response);
        // 앞뒤 공백 제거
        String cleaned = response.trim();

        // 백틱이 있다면 제거
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        cleaned = cleaned.trim();
        log.debug("Cleaned response: {}", cleaned);

        return cleaned;
    }
}
