package com.ssafy.enjoyTrip.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class PromptTemplateLoader {

    @Value("classpath:prompts/prompt-attraction-system.st")
    private Resource systemPromptResource;

    @Value("classpath:prompts/prompt-attraction-user.st")
    private Resource userPromptResource;

    @Value("classpath:prompts/prompt-season-system.st")
    private Resource seasonSystemPromptResource;

    @Value("classpath:prompts/prompt-season-user.st")
    private Resource seasonUserPromptResource;

    public String loadSystemPrompt() {
        try {
            return new String(FileCopyUtils.copyToByteArray(systemPromptResource.getInputStream()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading system prompt template", e);
            throw new RuntimeException("Failed to load system prompt template", e);
        }
    }

    public String loadUserPrompt() {
        try {
            return new String(FileCopyUtils.copyToByteArray(userPromptResource.getInputStream()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading user prompt template", e);
            throw new RuntimeException("Failed to load user prompt template", e);
        }
    }

    public String loadSeasonSystemPrompt() {
        try {
            return new String(FileCopyUtils.copyToByteArray(seasonSystemPromptResource.getInputStream()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading season system prompt template", e);
            throw new RuntimeException("Failed to load season system prompt template", e);
        }
    }

    public String loadSeasonUserPrompt() {
        try {
            return new String(FileCopyUtils.copyToByteArray(seasonUserPromptResource.getInputStream()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error loading season user prompt template", e);
            throw new RuntimeException("Failed to load season user prompt template", e);
        }
    }

}
