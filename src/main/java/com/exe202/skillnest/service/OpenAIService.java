package com.exe202.skillnest.service;

public interface OpenAIService {
    double[] generateEmbedding(String text);
    double cosineSimilarity(double[] a, double[] b);
    String generateMatchExplanation(String projectDesc, String studentProfile, double score);
}
