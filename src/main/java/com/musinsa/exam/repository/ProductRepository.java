package com.musinsa.exam.repository;

import com.musinsa.exam.model.Brand;
import com.musinsa.exam.model.Category;
import com.musinsa.exam.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findTopByCategoryOrderByPriceAsc(Category category);
    Product findTopByCategoryOrderByPriceDesc(Category category);
    List<Product> findByBrand(Brand brand);
}