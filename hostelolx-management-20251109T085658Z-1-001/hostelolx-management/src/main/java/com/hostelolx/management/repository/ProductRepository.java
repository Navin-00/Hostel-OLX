package com.hostelolx.management.repository;
import com.hostelolx.management.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUser_Id(Long userId); // ✅ Correct way for JPA relationship




}