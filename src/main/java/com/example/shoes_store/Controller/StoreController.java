package com.example.shoes_store.Controller;

import com.example.shoes_store.Repo.StoreRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/api/categories")
public class StoreController {
    @Autowired
    private StoreRepo storeRepo;

    @PostMapping("add")
    public String addStore(@RequestParam("name") String name, @RequestParam("address") String address) {
        log.info("adm");
        return "redirect:/admin/home";
    }

    @GetMapping("/")
    public String listStore(Model model) {
        log.info("listStore: {}",storeRepo.findAll());
        log.info("listStoresize: {}",(storeRepo.findAll()).size());
        model.addAttribute("stores",storeRepo.findAll() );
        return "redirect:/admin/home";
    }

}
