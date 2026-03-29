package com.exe202.skillnest.service.impl;

import com.exe202.skillnest.service.OpenAIService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import com.openai.models.EmbeddingCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAIServiceImpl implements OpenAIService {

    @Value("${openai.api-key:}")
    private String apiKey;

    private volatile OpenAIClient cachedClient;

    private OpenAIClient client() {
        if (cachedClient == null) {
            synchronized (this) {
                if (cachedClient == null) {
                    if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
                    }
                    cachedClient = OpenAIOkHttpClient.builder()
                            .apiKey(apiKey)
                            .build();
                }
            }
        }
        return cachedClient;
    }

    @Override
    public double[] generateEmbedding(String text) {
        var response = client().embeddings().create(
                EmbeddingCreateParams.builder()
                        .model("text-embedding-3-small")
                        .input(EmbeddingCreateParams.Input.ofString(text))
                        .build()
        );
        return response.data().get(0).embedding()
                .stream()
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    @Override
    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, magA = 0, magB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    @Override
    public String generateMatchExplanation(String projectDesc, String studentProfile, double score) {
        if (score < 0.6) return "Moderate match based on skills.";

        String prompt = String.format(
                "Explain why (2 sentences max):\nProject: %s\nStudent: %s\nMatch: %.0f%%",
                projectDesc, studentProfile, score * 100
        );
        var response = client().chat().completions().create(
                ChatCompletionCreateParams.builder()
                        .model("gpt-4o-mini")
                        .addMessage(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                                ChatCompletionUserMessageParam.builder()
                                        .content(prompt)
                                        .role(ChatCompletionUserMessageParam.Role.USER)
                                        .build()
                        ))
                        .maxCompletionTokens(100)
                        .build()
        );
        return response.choices().get(0).message().content()
                .orElse("Strong match based on skills and experience.");
    }
}
