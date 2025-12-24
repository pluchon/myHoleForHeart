package org.example.treehole.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.treehole.entry.AIMessage;
import org.example.treehole.mapper.AIMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Service
public class AIService {

    @Autowired
    private AIMessageMapper aiMessageMapper;

    private static final String API_KEY = "Bearer sk-phzrlmfdrczrqkmdzslsvrkwkxsuxtsywvjgyxviqiedovom";
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String MODEL = "deepseek-ai/DeepSeek-V3";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public List<AIMessage> getHistory(Long userId, String aiType) {
        return aiMessageMapper.selectHistory(userId, aiType);
    }

    public String chat(Long userId, String aiType, String userContent) throws IOException, InterruptedException {
        // 1. Save User Message
        AIMessage userMsg = new AIMessage();
        userMsg.setUserId(userId);
        userMsg.setAiType(aiType);
        userMsg.setSender("user");
        userMsg.setContent(userContent);
        aiMessageMapper.insert(userMsg);

        // 2. Prepare Request to AI
        // Retrieve some recent history for context (e.g., last 10 messages)
        List<AIMessage> history = aiMessageMapper.selectHistory(userId, aiType);
        // Limit history size if needed, but for now send all (or last N)

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("stream", false);

        ArrayNode messages = requestBody.putArray("messages");

        // System Prompt based on AI Type
        String systemPrompt = getSystemPrompt(aiType);
        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        // Add history
        int historyLimit = 20; // Last 20 messages
        int start = Math.max(0, history.size() - historyLimit);
        for (int i = start; i < history.size(); i++) {
            AIMessage m = history.get(i);
            // Skip the one we just inserted (or include it? selectHistory might include it if we query after insert)
            // Wait, selectHistory will include the just inserted message if transaction committed or same transaction?
            // Usually insert is immediate in default isolation.
            // But let's just use the history list.
            ObjectNode msg = messages.addObject();
            msg.put("role", "user".equals(m.getSender()) ? "user" : "assistant");
            msg.put("content", m.getContent());
        }

        // 3. Call API
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("AI API Error: " + response.statusCode() + " " + response.body());
        }

        // 4. Parse Response
        JsonNode rootNode = objectMapper.readTree(response.body());
        String aiContent = rootNode.path("choices").get(0).path("message").path("content").asText();

        // 5. Save AI Response
        AIMessage aiMsg = new AIMessage();
        aiMsg.setUserId(userId);
        aiMsg.setAiType(aiType);
        aiMsg.setSender("ai");
        aiMsg.setContent(aiContent);
        aiMessageMapper.insert(aiMsg);

        return aiContent;
    }

    private String getSystemPrompt(String aiType) {
        switch (aiType) {
            case "emotional":
                return "你是一个情感抚慰师，名字叫'暖暖'。你的语气温柔、体贴，善于倾听用户的烦恼，并给出温暖的安慰和建议。";
            case "guardian":
                return "你是一个树洞守护者，名字叫'守树人'。你沉稳、睿智，像一位老者，守护着树洞里的秘密，给迷茫的人指引方向。";
            case "funny":
                return "你是一个幽默大师，名字叫'乐乐'。你说话风趣幽默，喜欢讲笑话，希望能让不开心的用户笑起来。";
            case "general":
            default:
                return "你是一个树洞AI助手，负责陪伴用户，倾听他们的心声。";
        }
    }
}
