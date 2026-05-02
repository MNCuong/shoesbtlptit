package com.example.shoes_store.Service;

import com.example.shoes_store.dto.*;
import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.*;
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
    @Autowired
    private SupperlieRepo supperlieRepo;

    @Transactional
    public void createImportReceipt(InventoryDTO dto, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Lấy nhà phân phối
        Supplier supplier = null;
        if (dto.getSupplierId() != null) {
            supplier = supperlieRepo.findById(dto.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà phân phối"));
        }

        // Tạo phiếu nhập
        ImportReceipt receipt = new ImportReceipt();
        receipt.setReceiptCode("IMP-" + System.currentTimeMillis());
        receipt.setImportDate(LocalDateTime.now());
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setNote(dto.getNote());
        receipt.setCreatedBy(loggedInUser);
        receipt.setSupplier(supplier);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (InventoryItemDTO item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + item.getProductId()));

            ImportReceiptDetail detail = new ImportReceiptDetail();
            detail.setImportReceipt(receipt);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getPrice());
            detail.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            detail.setGender(item.getGender());

            receipt.getDetails().add(detail);
            totalAmount = totalAmount.add(detail.getTotalPrice());

            // Cập nhật tồn kho
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

    // Trong InventoryService.java

    // Lấy danh sách phiếu nhập (chỉ thông tin cơ bản, không details)
    public List<ImportReceiptListDTO> getAllImportReceiptsList() {
        List<ImportReceipt> receipts = importReceiptRepository.findAllByOrderByCreatedAtDesc();
        List<ImportReceiptListDTO> result = new ArrayList<>();

        for (ImportReceipt receipt : receipts) {
            ImportReceiptListDTO dto = new ImportReceiptListDTO();
            dto.setId(receipt.getId());
            dto.setReceiptCode(receipt.getReceiptCode());
            dto.setImportDate(receipt.getImportDate());
            dto.setTotalAmount(receipt.getTotalAmount());
            dto.setNote(receipt.getNote());
            dto.setCreatedAt(receipt.getCreatedAt());
            dto.setItemCount(receipt.getDetails() != null ? receipt.getDetails().size() : 0);

            // Lấy tên nhà phân phối
            if (receipt.getSupplier() != null) {
                dto.setSupplierName(receipt.getSupplier().getName());
            }

            // Lấy tên người tạo
            if (receipt.getCreatedBy() != null) {
                dto.setCreatedByName(receipt.getCreatedBy().getFullname());
            }

            result.add(dto);
        }

        log.info("Số lượng phiếu nhập trả về: {}", result.size());
        return result;
    }

    // Lấy danh sách phiếu xuất (chỉ thông tin cơ bản, không details)
    public List<ExportReceiptListDTO> getAllExportReceiptsList() {
        List<ExportReceipt> receipts = exportReceiptRepository.findAllByOrderByCreatedAtDesc();
        List<ExportReceiptListDTO> result = new ArrayList<>();

        for (ExportReceipt receipt : receipts) {
            ExportReceiptListDTO dto = new ExportReceiptListDTO();
            dto.setId(receipt.getId());
            dto.setReceiptCode(receipt.getReceiptCode());
            dto.setExportDate(receipt.getExportDate());
            dto.setTotalAmount(receipt.getTotalAmount());
            dto.setNote(receipt.getNote());
            dto.setCreatedAt(receipt.getCreatedAt());
            dto.setReason(receipt.getReason());
            dto.setItemCount(receipt.getDetails() != null ? receipt.getDetails().size() : 0);

            // Lấy tên cửa hàng
            if (receipt.getStore() != null) {
                dto.setStoreName(receipt.getStore().getName());
            }

            // Lấy tên người tạo
            if (receipt.getCreatedBy() != null) {
                dto.setCreatedByName(receipt.getCreatedBy().getFullname());
            }

            result.add(dto);
        }

        log.info("Số lượng phiếu xuất trả về: {}", result.size());
        return result;
    }

    // Lấy chi tiết phiếu nhập
    public ImportReceiptDTO getImportReceiptDetail(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));

        ImportReceiptDTO dto = new ImportReceiptDTO();
        dto.setId(receipt.getId());
        dto.setReceiptCode(receipt.getReceiptCode());
        dto.setCreatedAt(receipt.getCreatedAt());
        dto.setImportDate(receipt.getImportDate());
        dto.setTotalAmount(receipt.getTotalAmount());
        dto.setNote(receipt.getNote());

        if (receipt.getCreatedBy() != null) {
            dto.setCreatedByName(receipt.getCreatedBy().getFullname());
        } else {
            dto.setCreatedByName("N/A");
        }

        if (receipt.getSupplier() != null) {
            dto.setSupplierName(receipt.getSupplier().getName());
            dto.setSupplierAddress(receipt.getSupplier().getAddress());
        } else {
            dto.setSupplierName("N/A");
            dto.setSupplierAddress("N/A");
        }

        List<ImportReceiptDetailDTO> details = new ArrayList<>();
        if (receipt.getDetails() != null) {
            for (ImportReceiptDetail detail : receipt.getDetails()) {
                ImportReceiptDetailDTO detailDTO = new ImportReceiptDetailDTO();
                detailDTO.setId(detail.getId());
                detailDTO.setQuantity(detail.getQuantity());
                detailDTO.setUnitPrice(detail.getUnitPrice());
                detailDTO.setTotalPrice(detail.getTotalPrice());
                detailDTO.setGender(detail.getGender());

                if (detail.getProduct() != null) {
                    detailDTO.setProductId(detail.getProduct().getId());
                    detailDTO.setProductName(detail.getProduct().getName());
                } else {
                    detailDTO.setProductId(null);
                    detailDTO.setProductName("Sản phẩm không tồn tại");
                }
                details.add(detailDTO);
            }
        }
        dto.setDetails(details);

        // Log để debug
        log.info("=== IMPORT RECEIPT DETAIL ===");
        log.info("CreatedByName: {}", dto.getCreatedByName());
        log.info("SupplierName: {}", dto.getSupplierName());
        log.info("Total amount: {}", dto.getTotalAmount());
        log.info("Details count: {}", details.size());

        return dto;
    }
    // Lấy chi tiết phiếu xuất
    public ExportReceiptDTO getExportReceiptDetail(Long id) {
        ExportReceipt receipt = exportReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));

        ExportReceiptDTO dto = new ExportReceiptDTO();
        dto.setId(receipt.getId());
        dto.setReceiptCode(receipt.getReceiptCode());
        dto.setExportDate(receipt.getExportDate());
        dto.setTotalAmount(receipt.getTotalAmount());
        dto.setNote(receipt.getNote());
        dto.setCreatedAt(receipt.getCreatedAt());
        dto.setReason(receipt.getReason());

        // Lấy thông tin cửa hàng
        if (receipt.getStore() != null) {
            dto.setStoreName(receipt.getStore().getName());
            dto.setStoreAddress(receipt.getStore().getAddress());
        } else {
            dto.setStoreName("N/A");
            dto.setStoreAddress("N/A");
        }

        // Lấy thông tin người tạo
        if (receipt.getCreatedBy() != null) {
            dto.setCreatedByName(receipt.getCreatedBy().getFullname());
        } else {
            dto.setCreatedByName("N/A");
        }

        // Lấy danh sách chi tiết sản phẩm
        List<ExportReceiptDetailDTO> details = new ArrayList<>();
        if (receipt.getDetails() != null && !receipt.getDetails().isEmpty()) {
            for (ExportReceiptDetail detail : receipt.getDetails()) {
                ExportReceiptDetailDTO detailDTO = new ExportReceiptDetailDTO();
                detailDTO.setId(detail.getId());
                detailDTO.setQuantity(detail.getQuantity());
                detailDTO.setUnitPrice(detail.getUnitPrice());
                detailDTO.setTotalPrice(detail.getTotalPrice());
                detailDTO.setGender(detail.getGender());

                if (detail.getProduct() != null) {
                    detailDTO.setProductId(detail.getProduct().getId());
                    detailDTO.setProductName(detail.getProduct().getName());
                    detailDTO.setProductGender(detail.getProduct().getGender());
                } else {
                    detailDTO.setProductId(null);
                    detailDTO.setProductName("Sản phẩm không tồn tại");
                }

                details.add(detailDTO);
            }
        }
        dto.setDetails(details);

        log.info("Export receipt details size: {}", details.size());

        return dto;
    }
}