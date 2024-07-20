package com.musinsa.exam.controller;

import com.musinsa.exam.dto.LowestPriceBrandsDto;
import com.musinsa.exam.dto.LowestPriceBrandDto;
import com.musinsa.exam.dto.CategoryPriceRangeDto;
import com.musinsa.exam.model.Brand;
import com.musinsa.exam.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "상품 관련 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "카테고리별 최저가 브랜드 조회", description = "각 카테고리별 최저가 브랜드와 가격, 총액을 조회합니다.")
    @GetMapping("/lowest-price-brands")
    public ResponseEntity<LowestPriceBrandsDto> getLowestPriceBrands() {
        LowestPriceBrandsDto result = productService.getLowestPriceBrands();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "단일 브랜드 최저가 조회", description = "모든 카테고리 상품을 구매할 때 최저가격 브랜드와 총액을 조회합니다.")
    @GetMapping("/lowest-price-brand")
    public ResponseEntity<LowestPriceBrandDto> getLowestPriceBrand() {
        LowestPriceBrandDto result = productService.getLowestPriceBrand();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "카테고리별 최저/최고가 브랜드 조회", description = "특정 카테고리의 최저가와 최고가 브랜드 및 가격을 조회합니다.")
    @GetMapping("/category-price-range")
    public ResponseEntity<CategoryPriceRangeDto> getCategoryPriceRange(@RequestParam String category) {
        CategoryPriceRangeDto result = productService.getCategoryPriceRange(category);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "새 브랜드 추가", description = "새로운 브랜드를 추가합니다.")
    @PostMapping("/brand")
    public ResponseEntity<Brand> addBrand(@RequestBody String brandName) {
        Brand newBrand = productService.addBrand(brandName);
        return ResponseEntity.ok(newBrand);
    }

    @Operation(summary = "브랜드 정보 수정", description = "기존 브랜드의 정보를 수정합니다.")
    @PutMapping("/brand/{brandId}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long brandId, @RequestBody String brandName) {
        Brand updatedBrand = productService.updateBrand(brandId, brandName);
        return ResponseEntity.ok(updatedBrand);
    }

    @Operation(summary = "브랜드 삭제", description = "특정 브랜드를 삭제합니다.")
    @DeleteMapping("/brand/{brandId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long brandId) {
        productService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "모든 카테고리 조회", description = "모든 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}