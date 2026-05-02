package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.Supplier;
import com.example.shoes_store.Repo.SupperlieRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SupperlieService {

    @Autowired
    private SupperlieRepo supplierRepository;

    // Lấy tất cả nhà phân phối
    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    // Lấy nhà phân phối theo ID
    public Optional<Supplier> getById(Long id) {
        return supplierRepository.findById(id);
    }

    // Thêm nhà phân phối mới
    public Supplier addSupplier(Supplier supplier) {
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());
        return supplierRepository.save(supplier);
    }

    // Cập nhật nhà phân phối
    public Supplier updateSupplier(Supplier supplier) {
        Optional<Supplier> existing = supplierRepository.findById(supplier.getId());
        if (existing.isPresent()) {
            Supplier updated = existing.get();
            updated.setName(supplier.getName());
            updated.setContactPerson(supplier.getContactPerson());
            updated.setPhone(supplier.getPhone());
            updated.setEmail(supplier.getEmail());
            updated.setAddress(supplier.getAddress());
            updated.setNote(supplier.getNote());
            updated.setUpdatedAt(LocalDateTime.now());
            return supplierRepository.save(updated);
        }
        throw new RuntimeException("Không tìm thấy nhà phân phối với ID: " + supplier.getId());
    }

    // Xóa nhà phân phối
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    // Chuyển đổi trạng thái active/inactive
    public Supplier toggleSupplierStatus(Long id) {
        Optional<Supplier> existing = supplierRepository.findById(id);
        if (existing.isPresent()) {
            Supplier supplier = existing.get();
            supplier.setUpdatedAt(LocalDateTime.now());
            return supplierRepository.save(supplier);
        }
        throw new RuntimeException("Không tìm thấy nhà phân phối với ID: " + id);
    }

//    // Tìm kiếm theo tên
//    public List<Supplier> searchByName(String name) {
//        return supplierRepository.findByNameContainingIgnoreCase(name);
//    }
}