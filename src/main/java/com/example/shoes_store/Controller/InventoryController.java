package com.example.shoes_store.Controller;

import com.example.shoes_store.Repo.ProductRepo;
import com.example.shoes_store.Repo.StoreRepo;
import com.example.shoes_store.Service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepo productRepo;
    private final StoreRepo storeRepo;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("stores", storeRepo.findAll());
        return "admin/inventory";
    }

    @PostMapping("/process")
    public String process(
            Long storeId,
            List<Long> productIds,
            List<String> sizes,
            List<Integer> quantities,
            List<BigDecimal> prices,
            String type) {

        if ("import".equals(type)) {
            inventoryService.importGoods(storeId, productIds, sizes, quantities, prices);
        } else {
            inventoryService.exportGoods(storeId, productIds, sizes, quantities);
        }

        return "redirect:/admin/inventory";
    }
}