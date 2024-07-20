package com.musinsa.exam.service;

import com.musinsa.exam.dto.LowestPriceBrandsDto;
import com.musinsa.exam.dto.LowestPriceBrandDto;
import com.musinsa.exam.dto.CategoryPriceRangeDto;
import com.musinsa.exam.model.Brand;

import java.util.List;

public interface ProductService {
    LowestPriceBrandsDto getLowestPriceBrands();
    LowestPriceBrandDto getLowestPriceBrand();
    CategoryPriceRangeDto getCategoryPriceRange(String categoryName);
    Brand addBrand(String brandName);
    Brand updateBrand(Long brandId, String brandName);
    void deleteBrand(Long brandId);
    List<String> getAllCategories();
}