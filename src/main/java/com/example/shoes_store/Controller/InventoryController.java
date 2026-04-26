package com.example.shoes_store.Controller;

import com.example.shoes_store.Entity.ExportReceipt;
import com.example.shoes_store.Entity.ImportReceipt;
import com.example.shoes_store.Entity.ImportReceiptDetail;
import com.example.shoes_store.Entity.User;
import com.example.shoes_store.Service.InventoryService;
import com.example.shoes_store.dto.ImportReceiptDTO;
import com.example.shoes_store.dto.ImportReceiptDetailDTO;
import com.example.shoes_store.dto.InventoryDTO;
import com.example.shoes_store.Service.InventoryService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/process")
    public String processInventory(@ModelAttribute InventoryDTO inventoryDTO, HttpSession session ) {
//        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if ("import".equals(inventoryDTO.getType())) {
            inventoryService.createImportReceipt(inventoryDTO,session);
        } else if ("export".equals(inventoryDTO.getType())) {
            inventoryService.createExportReceipt(inventoryDTO,session);
        }
        return "redirect:/admin/home#inventory";
    }
    @GetMapping("/history")
    @ResponseBody
    public Map<String, Object> getHistory(@RequestParam(required = false) String type) {
        Map<String, Object> result = new HashMap<>();

        if (type == null || "all".equals(type)) {
            result.put("imports", inventoryService.getAllImportReceipts());
            result.put("exports", inventoryService.getAllExportReceipts());
        } else if ("import".equals(type)) {
            result.put("imports", inventoryService.getAllImportReceipts());
            log.info("size: {}", inventoryService.getAllImportReceipts().size());
            result.put("exports", List.of());
        } else if ("export".equals(type)) {
            result.put("imports", List.of());
            result.put("exports", inventoryService.getAllExportReceipts());
        }
        log.info("Import size: {}", ((List<?>) result.get("imports")).size());
        log.info("Export size: {}", ((List<?>) result.get("exports")).size());
        return result;
    }

    // Lấy chi tiết phiếu nhập
    @GetMapping("/import/{id}")
    @ResponseBody
    public ImportReceiptDTO  getImportDetail(@PathVariable Long id) {
        ImportReceiptDTO receipt = inventoryService.getImportReceiptDetail(id);

//        ImportReceiptDTO dto = new ImportReceiptDTO();
//        dto.setId(receipt.getId());
//        dto.setReceiptCode(receipt.getReceiptCode());
//        dto.setStoreName(receipt.getStore() != null ? receipt.getStore().getName() : "N/A");
//        dto.setStoreAddress(receipt.getStore() != null ? receipt.getStore().getAddress() : "N/A");
//        dto.setCreatedAt(receipt.getCreatedAt());
//        dto.setImportDate(receipt.getImportDate());
//        dto.setTotalAmount(receipt.getTotalAmount());
//        dto.setNote(receipt.getNote());
//
//        List<ImportReceiptDetailDTO> details = new ArrayList<>();
//        if (receipt.getDetails() != null) {
//            for (ImportReceiptDetail detail : receipt.getDetails()) {
//                ImportReceiptDetailDTO detailDTO = new ImportReceiptDetailDTO();
//                detailDTO.setId(detail.getId());
//                detailDTO.setProductId(detail.getProduct().getId());
//                detailDTO.setProductName(detail.getProduct().getName());
//                detailDTO.setQuantity(detail.getQuantity());
//                detailDTO.setUnitPrice(detail.getUnitPrice());
//                detailDTO.setTotalPrice(detail.getTotalPrice());
//                detailDTO.setSize(detail.getSize());
//                details.add(detailDTO);
//            }
//        }
//        dto.setDetails(details);

        return receipt;
    }

    // Lấy chi tiết phiếu xuất
    @GetMapping("/export/{id}")
    @ResponseBody
    public ExportReceipt getExportDetail(@PathVariable Long id) {
        return inventoryService.getExportReceiptDetail(id);
    }
}