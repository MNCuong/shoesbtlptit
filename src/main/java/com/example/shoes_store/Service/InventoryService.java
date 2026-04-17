package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.*;
import com.example.shoes_store.Repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepo;
    private final ImportRepository importRepo;
    private final ExportRepository exportRepo;
    private final ProductRepo productRepo;
    private final StoreRepo storeRepo;

    // ================= NHẬP KHO =================
    public void importGoods(Long storeId,
                            List<Long> productIds,
                            List<String> sizes,
                            List<Integer> quantities,
                            List<BigDecimal> prices) {

        Store store = storeRepo.findById(storeId).orElseThrow();

        Import importBill = new Import();
        importBill.setStore(store);
        importBill.setCreatedAt(LocalDateTime.now());

        List<ImportItem> items = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {

            Product product = productRepo.findById(productIds.get(i)).orElseThrow();

            ImportItem item = new ImportItem();
            item.setImportBill(importBill);
            item.setProduct(product);
            item.setSize(sizes.get(i));
            item.setQuantity(quantities.get(i));
            item.setPrice(prices.get(i));

            items.add(item);

            // UPDATE INVENTORY
            Inventory inv = inventoryRepo
                    .findByStoreIdAndProductIdAndSize(storeId, product.getId(), sizes.get(i))
                    .orElse(null);

            if (inv == null) {
                inv = new Inventory();
                inv.setStore(store);
                inv.setProduct(product);
                inv.setSize(sizes.get(i));
                inv.setQuantity(quantities.get(i));
            } else {
                inv.setQuantity(inv.getQuantity() + quantities.get(i));
            }

            inventoryRepo.save(inv);
        }

        importBill.setItems(items);
        importRepo.save(importBill);
    }

    // ================= XUẤT KHO =================
    public void exportGoods(Long storeId,
                            List<Long> productIds,
                            List<String> sizes,
                            List<Integer> quantities) {

        Store store = storeRepo.findById(storeId).orElseThrow();

        Export exportBill = new Export();
        exportBill.setStore(store);
        exportBill.setCreatedAt(LocalDateTime.now());

        List<ExportItem> items = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {

            Product product = productRepo.findById(productIds.get(i)).orElseThrow();

            Inventory inv = inventoryRepo
                    .findByStoreIdAndProductIdAndSize(storeId, product.getId(), sizes.get(i))
                    .orElseThrow(() -> new RuntimeException("Không đủ hàng"));

            if (inv.getQuantity() < quantities.get(i)) {
                throw new RuntimeException("Không đủ tồn kho");
            }

            inv.setQuantity(inv.getQuantity() - quantities.get(i));
            inventoryRepo.save(inv);

            ExportItem item = new ExportItem();
            item.setExportBill(exportBill);
            item.setProduct(product);
            item.setSize(sizes.get(i));
            item.setQuantity(quantities.get(i));

            items.add(item);
        }

        exportBill.setItems(items);
        exportRepo.save(exportBill);
    }
}