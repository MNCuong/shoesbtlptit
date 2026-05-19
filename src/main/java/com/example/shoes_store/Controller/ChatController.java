package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.Product;
import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ProductRepo productRepository;

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");

        // Phát hiện câu hỏi nhạy cảm
        if (isSensitiveQuestion(userMessage)) {
            return Map.of("reply", "Xin lỗi, tôi không thể cung cấp thông tin này. Tôi chỉ có thể hỗ trợ về sản phẩm quần áo trẻ em. Bạn cần tìm sản phẩm gì ạ? 😊");
        }

        List<Product> products = productRepository.findAll();

        // Giới hạn số lượng sản phẩm gửi đi để tránh quá tải
        List<Product> limitedProducts = products.size() > 30 ? products.subList(0, 30) : products;

        StringBuilder productData = new StringBuilder();
        productData.append("=== DANH SÁCH SẢN PHẨM ===\n");

        for(Product p : limitedProducts) {
            productData.append(String.format(
                    "Tên: %s | Giá: %,.0fđ | Độ tuổi: %s | Giới tính: %s\n",
                    p.getName(),
                    p.getPrice(),
                    p.getAgeGroup() != null ? p.getAgeGroup() : "Không xác định",
                    p.getGender() != null ? p.getGender() : "UNISEX"
            ));
        }

        // Phân tích ý định của khách hàng
        String intent = detectIntent(userMessage);

        String prompt = buildPrompt(userMessage, productData.toString(), intent);

        String reply = chatService.askAI(prompt);

        // Hậu xử lý để đảm bảo không có thông tin nhạy cảm
        reply = sanitizeReply(reply);

        return Map.of("reply", reply);
    }

    // Phát hiện câu hỏi nhạy cảm
    private boolean isSensitiveQuestion(String message) {
        String lowerMsg = message.toLowerCase();
        String[] sensitiveKeywords = {
                "mã đơn", "order code", "order id", "orderid",
                "số điện thoại", "phone", "sđt",
                "địa chỉ", "address",
                "thông tin admin", "admin",
                "email", "mail",
                "mật khẩu", "password",
                "tài khoản", "account",
                "dữ liệu nội bộ", "internal",
                "cơ sở dữ liệu", "database",
                "bảng", "table",
                "api key", "apikey"
        };

        for (String keyword : sensitiveKeywords) {
            if (lowerMsg.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // Phát hiện ý định của khách hàng
    private String detectIntent(String message) {
        String lowerMsg = message.toLowerCase();

        if (lowerMsg.contains("áo") || lowerMsg.contains("quần") || lowerMsg.contains("đồ")) {
            return "PRODUCT_INQUIRY";
        } else if (lowerMsg.contains("giá") || lowerMsg.contains("bao nhiêu")) {
            return "PRICE_INQUIRY";
        } else if (lowerMsg.contains("size") || lowerMsg.contains("mặc vừa")) {
            return "SIZE_INQUIRY";
        } else if (lowerMsg.contains("màu") || lowerMsg.contains("xanh") || lowerMsg.contains("đỏ") || lowerMsg.contains("hồng")) {
            return "COLOR_INQUIRY";
        } else if (lowerMsg.contains("tuổi") || lowerMsg.contains("bé") || lowerMsg.contains("mấy tuổi")) {
            return "AGE_INQUIRY";
        } else if (lowerMsg.contains("trai") || lowerMsg.contains("gái")) {
            return "GENDER_INQUIRY";
        } else if (lowerMsg.contains("cảm ơn") || lowerMsg.contains("thanks")) {
            return "THANKS";
        } else if (lowerMsg.contains("chào") || lowerMsg.contains("hi") || lowerMsg.contains("hello")) {
            return "GREETING";
        }

        return "GENERAL";
    }

    // Xây dựng prompt theo ý định
    private String buildPrompt(String userMessage, String productData, String intent) {

        String basePrompt = """
            Bạn là chatbot bán quần áo trẻ em thân thiện, tên là "Bé Yêu Shop".
            
            QUAN TRỌNG - TUYỆT ĐỐI KHÔNG ĐƯỢC:
            - Tiết lộ bất kỳ thông tin về: mã đơn hàng, số điện thoại, địa chỉ, email, admin, database, API
            - Trả lời những câu hỏi không liên quan đến sản phẩm quần áo trẻ em
            - Nói rằng bạn là AI hoặc chatbot
            - Liệt kê quá 5 sản phẩm trong 1 câu trả lời
            
            QUY TẮC ĐỊNH DẠNG:
            - Mỗi sản phẩm trên 1 dòng riêng, bắt đầu bằng dấu •
            - Dùng icon cảm xúc nhẹ nhàng: 😊 🎀 👕
            - Luôn kết thúc bằng câu hỏi để tương tác
            
            DỮ LIỆU SẢN PHẨM:
            %s
            
            """;

        String intentSpecific = switch (intent) {
            case "GREETING" -> """
                KHÁCH CHÀO: %s
                
                HƯỚNG DẪN: Chào lại thân thiện, giới thiệu shop và hỏi khách cần tìm gì.
                Ví dụ: "Dạ chào bạn! Shop Bé Yêu chuyên quần áo trẻ em đẹp và chất lượng. Bạn đang tìm đồ cho bé trai hay gái ạ? 😊"
                """;

            case "PRODUCT_INQUIRY" -> """
                KHÁCH HỎI VỀ SẢN PHẨM: %s
                
                HƯỚNG DẪN: 
                - Tìm trong dữ liệu sản phẩm, chọn 3-5 sản phẩm phù hợp nhất
                - Liệt kê mỗi sản phẩm trên 1 dòng riêng
                - Hỏi thêm về sở thích (màu sắc, độ tuổi, giới tính)
                """;

            case "PRICE_INQUIRY" -> """
                KHÁCH HỎI GIÁ: %s
                
                HƯỚNG DẪN:
                - Nếu hỏi sản phẩm cụ thể: trả lời giá chính xác
                - Nếu hỏi chung: liệt kê 3 sản phẩm kèm giá
                - Định dạng giá: XXX.XXXđ (ví dụ: 250.000đ)
                """;

            case "THANKS" -> """
                KHÁCH CẢM ƠN: %s
                
                HƯỚNG DẪN: Đáp lại lịch sự, chúc khách vui vẻ và mời ghé lại.
                Ví dụ: "Dạ không có gì ạ! Cảm ơn bạn đã quan tâm. Chúc bạn và bé yêu luôn vui vẻ! 💝"
                """;

            default -> """
                KHÁCH HỎI: %s
                
                HƯỚNG DẪN: Trả lời tự nhiên, tập trung vào sản phẩm, không lan man.
                Nếu không chắc chắn, hãy nói: "Dạ em chưa rõ lắm, bạn có thể cho em biết bạn đang tìm sản phẩm gì không ạ?"
                """;
        };

        String finalPrompt = basePrompt + "\n" + intentSpecific;

        return String.format(finalPrompt, productData, userMessage);
    }

    // Lọc bỏ thông tin nhạy cảm khỏi câu trả lời
    private String sanitizeReply(String reply) {
        // Xóa các pattern nhạy cảm
        reply = reply.replaceAll("\\bORD-\\d+\\b", "[MÃ ĐƠN HÀNG]");
        reply = reply.replaceAll("\\b\\d{10,11}\\b", "[SỐ ĐIỆN THOẠI]");
        reply = reply.replaceAll("\\b[\\w.-]+@[\\w.-]+\\.\\w+\\b", "[EMAIL]");
        reply = reply.replaceAll("sk-or-v1-[a-zA-Z0-9]+", "[API_KEY]");
        reply = reply.replaceAll("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", "[IP_ADDRESS]");

        // Xóa nếu vẫn còn từ khóa nhạy cảm
        String[] sensitiveWords = {"password", "secret", "token", "key", "database", "table", "column", "select", "insert"};
        for (String word : sensitiveWords) {
            reply = reply.replaceAll("(?i)\\b" + word + "\\b", "***");
        }

        return reply;
    }
}