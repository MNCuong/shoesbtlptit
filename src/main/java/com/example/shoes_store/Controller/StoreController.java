package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.Store;
import com.example.shoes_store.Repo.StoreRepo;
import com.example.shoes_store.dto.StoreDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/api/stores")
public class StoreController {
    @Autowired
    private StoreRepo storeRepo;

    @PostMapping("/add")
    public String addStore(@RequestBody StoreDTO storeDTO) {
        Store s= new Store();
        s.setName(storeDTO.getName());
        s.setAddress(storeDTO.getAddress());
        s.setPhone(storeDTO.getPhone());
        s.setActive(true);
        storeRepo.save(s);
        return "redirect:/admin/home";
    }
    @PostMapping("/delete/{id}")
    public String deleteStore(@PathVariable Long id) {
        // Gọi service xóa
        storeRepo.deleteById(id);
        return "redirect:/admin/home";
    }

    @PostMapping("/update/{id}")
    public String updateStore(@PathVariable Long id, @RequestBody StoreDTO storeDTO) {
        Store s= storeRepo.findById(id).get();
        s.setName(storeDTO.getName());
        s.setAddress(storeDTO.getAddress());
        s.setPhone(storeDTO.getPhone());
        storeRepo.save(s);
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
