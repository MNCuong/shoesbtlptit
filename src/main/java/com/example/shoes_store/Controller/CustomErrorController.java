package com.example.shoes_store.Controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              Model model,
                              @RequestParam(value = "msg", required = false) String msg) {

        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object message = request.getAttribute("jakarta.servlet.error.message");

        if ("unauthorized".equals(msg)) {
            model.addAttribute("status", 403);
            model.addAttribute("error", "Access Denied");
            model.addAttribute("message", "Bạn không có quyền truy cập!");
        } else {
            model.addAttribute("status", status);
            model.addAttribute("error", "Something went wrong");
            model.addAttribute("message", message);
        }

        return "error";
    }
}