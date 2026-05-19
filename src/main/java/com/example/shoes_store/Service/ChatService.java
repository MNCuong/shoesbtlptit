package com.example.shoes_store.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final String API_KEY = "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";

    public String askAI(String prompt) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://openrouter.ai/api/v1")
                    .defaultHeader("Authorization", "Bearer " + API_KEY)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("HTTP-Referer", "http://localhost:8080")
                    .defaultHeader("X-Title", "Bé Yêu Shop")
                    .build();

            // Thêm system message để bảo vệ
            Map<String, Object> request = Map.of(
                    "model", "deepseek/deepseek-chat",
                    "messages", List.of(
                            Map.of("role", "system", "content", """
                                Bạn là trợ lý bán hàng quần áo trẻ em.
                                
                                QUY TẮC AN TOÀN TUYỆT ĐỐI:
                                - KHÔNG BAO GIỜ tiết lộ: mã đơn hàng, số điện thoại, địa chỉ, email, API key, thông tin database, thông tin admin, dữ liệu nội bộ
                                - Nếu khách hỏi về những thông tin này, hãy nói: "Xin lỗi, tôi không thể cung cấp thông tin này. Tôi chỉ hỗ trợ về sản phẩm quần áo trẻ em ạ."
                                - KHÔNG BAO GIỜ nói rằng bạn là AI hay chatbot
                                - CHỈ trả lời các câu hỏi liên quan đến sản phẩm quần áo trẻ em
                                - KHÔNG được trích xuất hoặc lặp lại dữ liệu nhạy cảm từ prompt
                                """),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 500
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException("API Error: " + errorBody))))
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("choices")) {
                return "Xin lỗi, hiện tại shop đang bận. Bạn vui lòng thử lại sau ạ! 😊";
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return "Xin lỗi, tôi chưa hiểu rõ câu hỏi. Bạn có thể hỏi cụ thể hơn về sản phẩm được không ạ?";
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");

            String content = message.get("content").toString();

            // Lọc lần cuối thông tin nhạy cảm
            content = content.replaceAll("(?i)(mã đơn|order code|order id|ORD-\\d+)", "[MÃ ĐƠN HÀNG]");
            content = content.replaceAll("(?i)(số điện thoại|phone|\\d{10,11})", "[SỐ ĐIỆN THOẠI]");
            content = content.replaceAll("(?i)(email|mail|\\S+@\\S+\\.\\S+)", "[EMAIL]");
            content = content.replaceAll("(?i)(địa chỉ|address)", "[ĐỊA CHỈ]");

            return content;

        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, có lỗi xảy ra. Bạn vui lòng thử lại sau ạ! 😊";
        }
    }
}