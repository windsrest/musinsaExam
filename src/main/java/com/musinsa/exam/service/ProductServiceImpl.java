package com.musinsa.exam.service;

import com.musinsa.exam.dto.LowestPriceBrandsDto;
import com.musinsa.exam.dto.LowestPriceBrandDto;
import com.musinsa.exam.dto.CategoryPriceRangeDto;
import com.musinsa.exam.model.Brand;
import com.musinsa.exam.model.Category;
import com.musinsa.exam.model.Product;
import com.musinsa.exam.repository.BrandRepository;
import com.musinsa.exam.repository.CategoryRepository;
import com.musinsa.exam.repository.ProductRepository;
import com.musinsa.exam.exception.ResourceNotFoundException;
import com.musinsa.exam.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public LowestPriceBrandsDto getLowestPriceBrands() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new ResourceNotFoundException("카테고리를 찾을 수 없습니다");
        }

        LowestPriceBrandsDto result = new LowestPriceBrandsDto();
        List<LowestPriceBrandsDto.CategoryPrice> categoryPrices = new ArrayList<>();
        int totalPrice = 0;

        for (Category category : categories) {
            Product lowestPriceProduct = productRepository.findTopByCategoryOrderByPriceAsc(category);
            if (lowestPriceProduct != null) {
                LowestPriceBrandsDto.CategoryPrice categoryPrice = new LowestPriceBrandsDto.CategoryPrice();
                categoryPrice.setCategory(category.getName());
                categoryPrice.setBrand(lowestPriceProduct.getBrand().getName());
                categoryPrice.setPrice(lowestPriceProduct.getPrice());
                categoryPrices.add(categoryPrice);
                totalPrice += lowestPriceProduct.getPrice();
            }
        }

        if (categoryPrices.isEmpty()) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다");
        }

        result.setCategoryPrices(categoryPrices);
        result.setTotalPrice(totalPrice);
        return result;
    }

    @Override
    public LowestPriceBrandDto getLowestPriceBrand() {
        List<Brand> brands = brandRepository.findAll();
        if (brands.isEmpty()) {
            throw new ResourceNotFoundException("브랜드를 찾을 수 없습니다");
        }

        Brand lowestPriceBrand = null;
        int lowestTotalPrice = Integer.MAX_VALUE;
        List<Product> lowestPriceProducts = null;

        for (Brand brand : brands) {
            List<Product> products = productRepository.findByBrand(brand);
            int totalPrice = products.stream().mapToInt(Product::getPrice).sum();
            if (totalPrice < lowestTotalPrice) {
                lowestTotalPrice = totalPrice;
                lowestPriceBrand = brand;
                lowestPriceProducts = products;
            }
        }

        if (lowestPriceBrand == null) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다");
        }

        LowestPriceBrandDto result = new LowestPriceBrandDto();
        LowestPriceBrandDto.LowestPrice lowestPrice = new LowestPriceBrandDto.LowestPrice();
        lowestPrice.setBrand(lowestPriceBrand.getName());
        lowestPrice.setTotalPrice(lowestTotalPrice);

        List<LowestPriceBrandDto.CategoryPrice> categoryPrices = lowestPriceProducts.stream()
                .map(p -> {
                    LowestPriceBrandDto.CategoryPrice categoryPrice = new LowestPriceBrandDto.CategoryPrice();
                    categoryPrice.setCategory(p.getCategory().getName());
                    categoryPrice.setPrice(p.getPrice());
                    return categoryPrice;
                })
                .collect(Collectors.toList());
        lowestPrice.setCategories(categoryPrices);

        result.setLowestPrice(lowestPrice);
        return result;
    }

    @Override
    public CategoryPriceRangeDto getCategoryPriceRange(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryName));

        Product lowestPriceProduct = productRepository.findTopByCategoryOrderByPriceAsc(category);
        Product highestPriceProduct = productRepository.findTopByCategoryOrderByPriceDesc(category);

        if (lowestPriceProduct == null || highestPriceProduct == null) {
            throw new ResourceNotFoundException("해당 카테고리의 상품을 찾을 수 없습니다: " + categoryName);
        }

        CategoryPriceRangeDto result = new CategoryPriceRangeDto();
        result.setCategory(categoryName);

        CategoryPriceRangeDto.BrandPrice lowestPrice = new CategoryPriceRangeDto.BrandPrice();
        lowestPrice.setBrand(lowestPriceProduct.getBrand().getName());
        lowestPrice.setPrice(lowestPriceProduct.getPrice());
        result.setLowestPrice(lowestPrice);

        CategoryPriceRangeDto.BrandPrice highestPrice = new CategoryPriceRangeDto.BrandPrice();
        highestPrice.setBrand(highestPriceProduct.getBrand().getName());
        highestPrice.setPrice(highestPriceProduct.getPrice());
        result.setHighestPrice(highestPrice);

        return result;
    }

    @Override
    @Transactional
    public Brand addBrand(String brandName) {
        if (brandRepository.findByName(brandName).isPresent()) {
            throw new DuplicateResourceException("Brand already exists: " + brandName);
        }
        Brand brand = new Brand();
        brand.setName(brandName);
        return brandRepository.save(brand);
    }

    @Override
    @Transactional
    public Brand updateBrand(Long brandId, String brandName) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brandId));

        Optional<Brand> existingBrandWithName = brandRepository.findByName(brandName);
        if (existingBrandWithName.isPresent() && !existingBrandWithName.get().getId().equals(brandId)) {
            throw new DuplicateResourceException("Brand name already in use: " + brandName);
        }

        brand.setName(brandName);
        return brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long brandId) {
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("Brand not found with id: " + brandId);
        }
        brandRepository.deleteById(brandId);
    }

    @Override
    public List<String> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}