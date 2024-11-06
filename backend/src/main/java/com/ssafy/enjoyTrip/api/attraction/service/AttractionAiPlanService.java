package com.ssafy.enjoyTrip.api.attraction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssafy.enjoyTrip.api.attraction.dto.AiPlanResponseDto;
import com.ssafy.enjoyTrip.api.attraction.dto.DayPlanDto;
import com.ssafy.enjoyTrip.domain.attraction.dto.SidoCode;
import com.ssafy.enjoyTrip.api.exception.AttractionServiceException;
import com.ssafy.enjoyTrip.api.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionAiPlanService {
    private final AiPromptService aiPromptService;
    private final ChatModel chatModel;
    private final AiResponseParser aiResponseParser;
    private final AttractionMappingService attractionMappingService;

    public List<DayPlanDto> generatePlan(String sidoCode, String day) {
        try {

            String sidoName = SidoCode.getNameByCode(sidoCode);
            // AI 응답 생성
            Message userMessage = aiPromptService.createUserMessage(sidoName, day);
            Message systemMessage = aiPromptService.createSystemMessage(day);
            String aiResponse = chatModel.call(userMessage, systemMessage);

            aiResponseParser.logJsonStructure(aiResponse);

            // 응답 파싱
            AiPlanResponseDto planResponse = aiResponseParser.parseResponse(aiResponse);

            // 관광지 정보 매핑 (sidoCode를 Long으로 변환)
            return attractionMappingService.mapAttractionDetails(
                    SidoCode.getCodeAsLong(sidoCode),
                    planResponse.getPlans()
            );

        } catch (JsonProcessingException e) {
            throw new AttractionServiceException(
                    ErrorCode.INVALID_AI_RESPONSE,

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          "AI 응답을 처리할 수 없는 형식입니다: " + e.getMessage(),
                    e
            );
        } catch (Exception e) {
            throw new AttractionServiceException(
                    ErrorCode.AI_SERVICE_ERROR,
                    "AI 서비스 처리 중 오류가 발생했습니다: " + e.getMessage(),
                    e
            );
        }
    }

    private static String getString(String sidoCode) {
        String sido = null;
        switch(sidoCode){
            case "1" -> sido = "서울";
            case "2" -> sido = "인천";
            case "3" -> sido = "대전";
            case "4" -> sido = "대구";
            case "5" -> sido = "광주";
            case "6" -> sido = "부산";
            case "7" -> sido = "울산";
            case "8" -> sido = "세종";
            case "31" -> sido = "경기도";
            case "32" -> sido = "강원도";
            case "33" -> sido = "충청북도";
            case "34" -> sido = "충청남도";
            case "35" -> sido = "경상북도";
            case "36" -> sido = "경상남도";
            case "37" -> sido = "전라북도";
            case "38" -> sido = "전라남도";
            case "39" -> sido = "제주도";
        }
        return sido;
    }
}
