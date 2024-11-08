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
}
