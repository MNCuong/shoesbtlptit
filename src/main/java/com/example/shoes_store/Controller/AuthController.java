package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model, RedirectAttributes redirectAttributes) {
        User user = userService.login(username, password);

        if (user != null) {
            if ("ADMIN".equals(user.getRole()) || "STAFF".equals(user.getRole())) {
                session.setAttribute("loggedInUser", user);
                return "redirect:/admin/home";
            }else if ("USER".equals(user.getRole()) ) {
                session.setAttribute("loggedInUser", user);
                model.addAttribute("user", user);
                return "redirect:/";
            } else {
                model.addAttribute("status", 403);
                model.addAttribute("error", "Access Denied");
                model.addAttribute("message", "Bạn không có quyền truy cập vào trang admin");

                return "error";
            }
        }
        redirectAttributes.addFlashAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
        return "redirect:login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "/user/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           @RequestParam String fullname,
                           @RequestParam String address,
                           Model model, RedirectAttributes redirectAttributes) {

        if (userService.userExists(username)) {
            model.addAttribute("error", "Tên người dùng đã tồn tại!");
            redirectAttributes.addFlashAttribute("error", "Tên người dùng đã tồn tại!");
            return "redirect:register";
        }
        if (userService.emailExists(email)) {
            model.addAttribute("error", "Email đã tồn tại!");
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setFullname(fullname);
        newUser.setAddress(address);

        userService.registerUser(newUser);

        model.addAttribute("message", "Đăng ký thành công!");
        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công!");

        return "redirect:login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}


