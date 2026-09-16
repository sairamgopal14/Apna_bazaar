package in.apnabazaar.gemini;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

record GeminiGenerateContentRequest(@JsonProperty("system_instruction") SystemInstruction systemInstruction,
                                     List<Content> contents,
                                     GenerationConfig generationConfig) {

    record SystemInstruction(List<Part> parts) {
    }

    record Content(String role, List<Part> parts) {
    }

    record Part(String text) {
    }

    record GenerationConfig(double temperature, @JsonProperty("maxOutputTokens") int maxOutputTokens) {
    }
}
