package com.example.shoes_store.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    @Value("${API_KEY}")
    private static String API_KEY;

    public String askAI(String prompt) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.groq.com/openai/v1")
                    .defaultHeader("Authorization", "Bearer " + API_KEY)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            Map<String, Object> request = Map.of(
                    "model", "llama-3.3-70b-versatile",     // Model khuyến nghị
                    // Các model phổ biến khác:
                    // "llama-3.1-70b-versatile"
                    // "mixtral-8x7b-32768"
                    // "gemma2-9b-it"

                    "messages", List.of(
                            Map.of("role", "system", "content", """
                                Bạn là trợ lý bán hàng quần áo trẻ em thân thiện.
                                Tên shop: Bé Yêu Shop. 
                                Chỉ trả lời về sản phẩm quần áo trẻ em.
                                Không tiết lộ thông tin nhạy cảm (đơn hàng, sđt, địa chỉ...).
                                """),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.7,
                    "max_tokens", 600,
                    "top_p", 0.9
            );

            Map response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return message.get("content").toString();

        } catch (WebClientResponseException e) {
            System.err.println("Groq Error Status: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            return "Xin lỗi, hiện tại đang quá tải. Bạn thử lại sau một chút nhé! 😊";
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, shop đang bận. Bạn thử lại sau ạ! 😊";
        }
    }
}