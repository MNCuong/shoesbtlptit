package com.example.shoes_store.Service;

import com.example.shoes_store.dto.ImportReceiptDTO;
import com.example.shoes_store.dto.ImportReceiptDetailDTO;
import com.example.shoes_store.dto.InventoryDTO;
import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.*;
import com.example.shoes_store.dto.InventoryItemDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InventoryService {

    @Autowired
    private CImportReceiptRepository importReceiptRepository;

    @Autowired
    private CExportReceiptRepository exportReceiptRepository;

    @Autowired
    private StoreRepo storeRepository;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private UserRepo userRepository;

    @Transactional
    public void createImportReceipt(InventoryDTO dto, HttpSession session) {
//        Store store = storeRepository.findById(dto.getStoreId())
//                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng"));
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Tạo phiếu nhập
        ImportReceipt receipt = new ImportReceipt();
        receipt.setReceiptCode("IMP-" + System.currentTimeMillis());
//        receipt.setStore(store);
        receipt.setImportDate(LocalDateTime.now());
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setNote(dto.getNote());
        receipt.setCreatedBy(loggedInUser);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Tạo chi tiết phiếu nhập
        for (InventoryItemDTO item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            ImportReceiptDetail detail = new ImportReceiptDetail();
            detail.setImportReceipt(receipt);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getPrice());
            detail.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            detail.setGender(item.getGender());

            receipt.getDetails().add(detail);
            totalAmount = totalAmount.add(detail.getTotalPrice());

            // Cập nhật tồn kho cho sản phẩm
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        receipt.setTotalAmount(totalAmount);
        importReceiptRepository.save(receipt);
    }

    @Transactional
    public void createExportReceipt(InventoryDTO dto, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        Store store = storeRepository.findById(dto.getStoreId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cửa hàng"));

        // Tạo phiếu xuất
        ExportReceipt receipt = new ExportReceipt();
        receipt.setReceiptCode("EXP-" + System.currentTimeMillis());
        receipt.setStore(store);
        receipt.setExportDate(LocalDateTime.now());
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setReason(dto.getReason()); // SALE, TRANSFER, DAMAGE, RETURN
        receipt.setNote(dto.getNote());
        receipt.setCreatedBy(loggedInUser);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Tạo chi tiết phiếu xuất
        for (InventoryItemDTO item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho");
            }

            ExportReceiptDetail detail = new ExportReceiptDetail();
            detail.setExportReceipt(receipt);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getPrice());
            detail.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            detail.setGender(item.getGender());

            receipt.getDetails().add(detail);
            totalAmount = totalAmount.add(detail.getTotalPrice());

            // Cập nhật tồn kho
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        receipt.setTotalAmount(totalAmount);
        exportReceiptRepository.save(receipt);
    }

    // Lấy danh sách phiếu nhập
    public List<ImportReceipt> getAllImportReceipts() {

        log.info("addd: {}",importReceiptRepository.findAllByOrderByCreatedAtDesc().size());
        return importReceiptRepository.findAllByOrderByCreatedAtDesc();
    }

    // Lấy danh sách phiếu xuất
    public List<ExportReceipt> getAllExportReceipts() {
        log.info("uddd: {}",exportReceiptRepository.findAllByOrderByCreatedAtDesc().size());
        return exportReceiptRepository.findAllByOrderByCreatedAtDesc();
    }

    // Lấy chi tiết phiếu nhập
    public ImportReceiptDTO getImportReceiptDetail(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        ImportReceiptDTO dto = new ImportReceiptDTO();
        dto.setId(receipt.getId());
        dto.setReceiptCode(receipt.getReceiptCode());

        // Lấy tên cửa hàng an toàn
//        if (receipt.getStore() != null) {
//            dto.setStoreName(receipt.getStore().getName());
//            dto.setStoreAddress(receipt.getStore().getAddress());
//        } else {
//            dto.setStoreName("Chưa có thông tin");
//            dto.setStoreAddress("Chưa có thông tin");
//        }

        dto.setCreatedAt(receipt.getCreatedAt());
        dto.setImportDate(receipt.getImportDate());
        dto.setTotalAmount(receipt.getTotalAmount());
        dto.setNote(receipt.getNote());

        List<ImportReceiptDetailDTO> details = new ArrayList<>();
        if (receipt.getDetails() != null) {
            for (ImportReceiptDetail detail : receipt.getDetails()) {
                ImportReceiptDetailDTO detailDTO = new ImportReceiptDetailDTO();
                detailDTO.setId(detail.getId());
                detailDTO.setQuantity(detail.getQuantity());
                detailDTO.setUnitPrice(detail.getUnitPrice());
                detailDTO.setTotalPrice(detail.getTotalPrice());
                detailDTO.setGender(detail.getGender());

                // Lấy tên sản phẩm an toàn
                if (detail.getProduct() != null) {
                    detailDTO.setProductId(detail.getProduct().getId());
                    detailDTO.setProductName(detail.getProduct().getName());
                } else {
                    detailDTO.setProductId(null);
                    detailDTO.setProductName("Sản phẩm không tồn tại (ID: " + detail.getProduct().getId() + ")");
                }

                details.add(detailDTO);
            }
        }
        dto.setDetails(details);

        return dto;
    }

    // Lấy chi tiết phiếu xuất
    public ExportReceipt getExportReceiptDetail(Long id) {
        return exportReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));
    }
}