package com.ssafy.enjoyTrip.api.attraction.service;

import com.ssafy.enjoyTrip.api.util.PromptTemplateLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptService {
    private final PromptTemplateLoader promptLoader;

    public Message createUserMessage(String sido, String day) {
        String userPromptTemplate = promptLoader.loadUserPrompt();
        PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
        userTemplate.add("sido", sido);
        userTemplate.add("day", day);
        return new UserMessage(userTemplate.render());
    }

    public Message createSystemMessage(String day) {
        String systemPromptTemplate = promptLoader.loadSystemPrompt();
        PromptTemplate systemTemplate = new PromptTemplate(systemPromptTemplate);
        systemTemplate.add("day", day);
        return new SystemMessage(systemTemplate.render());
    }

    public Message createSeasonUserMessage(String season) {
        String userPromptTemplate = promptLoader.loadSeasonUserPrompt();
        PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
        userTemplate.add("season", season);
        return new UserMessage(userTemplate.render());
    }

    public Message createKeywordSystemMessage() {
        String systemPromptTemplate = promptLoader.loadSeasonSystemPrompt();
        PromptTemplate systemTemplate = new PromptTemplate(systemPromptTemplate);
        return new SystemMessage(systemTemplate.render());
    }

    public Message createKeywordUserMessage(String keyword) {
        String userPromptTemplate = promptLoader.loadKeywordUserPrompt();
        PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
        userTemplate.add("keyword", keyword);
        return new UserMessage(userTemplate.render());
    }

    public Message createSeasonSystemMessage() {
        String systemPromptTemplate = promptLoader.loadKeywordSystemPrompt();
        PromptTemplate systemTemplate = new PromptTemplate(systemPromptTemplate);
        return new SystemMessage(systemTemplate.render());
    }
}
