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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Brand brandA, brandB;
    private Category category1, category2;
    private Product productA1, productA2, productB1, productB2;

    @BeforeEach
    void setUp() {
        brandA = new Brand(1L, "A");
        brandB = new Brand(2L, "B");
        category1 = new Category(1L, "상의");
        category2 = new Category(2L, "하의");
        productA1 = new Product(1L, brandA, category1, 10000);
        productA2 = new Product(2L, brandA, category2, 20000);
        productB1 = new Product(3L, brandB, category1, 15000);
        productB2 = new Product(4L, brandB, category2, 25000);
    }

    /**
     * 최저가 브랜드 조회 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 다음을 확인합니다:
     * 1. 모든 카테고리에 대해 최저가 상품이 올바르게 조회되는지
     * 2. 총액이 정확하게 계산되는지
     * 3. 반환된 DTO의 구조가 예상과 일치하는지
     */
    @Test
    void getLowestPriceBrands_Success() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));
        when(productRepository.findTopByCategoryOrderByPriceAsc(category1)).thenReturn(productA1);
        when(productRepository.findTopByCategoryOrderByPriceAsc(category2)).thenReturn(productA2);

        LowestPriceBrandsDto result = productService.getLowestPriceBrands();

        assertNotNull(result);
        assertEquals(2, result.getCategoryPrices().size());
        assertEquals(30000, result.getTotalPrice());
        assertEquals("A", result.getCategoryPrices().get(0).getBrand());
        assertEquals(10000, result.getCategoryPrices().get(0).getPrice());
        assertEquals("A", result.getCategoryPrices().get(1).getBrand());
        assertEquals(20000, result.getCategoryPrices().get(1).getPrice());
    }

    /**
     * 카테고리가 없을 때 최저가 브랜드 조회 기능의 예외 처리를 테스트합니다.
     * 이는 데이터 무결성 검사의 중요한 부분으로, 예상치 못한 데이터 상태를 처리할 수 있는지 확인합니다.
     */
    @Test
    void getLowestPriceBrands_NoCategoriesFound() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> productService.getLowestPriceBrands());
    }

    /**
     * 상품이 없을 때 최저가 브랜드 조회 기능의 예외 처리를 테스트합니다.
     * 이 테스트는 카테고리는 존재하지만 상품이 없는 극단적인 상황을 시뮬레이션합니다.
     */
    @Test
    void getLowestPriceBrands_NoProductsFound() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));
        when(productRepository.findTopByCategoryOrderByPriceAsc(any(Category.class))).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> productService.getLowestPriceBrands());
    }

    /**
     * 단일 브랜드 최저가 조회 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 여러 브랜드 중 총액이 가장 낮은 브랜드를 올바르게 식별하는지 확인합니다.
     */
    @Test
    void getLowestPriceBrand_Success() {
        when(brandRepository.findAll()).thenReturn(Arrays.asList(brandA, brandB));
        when(productRepository.findByBrand(brandA)).thenReturn(Arrays.asList(productA1, productA2));
        when(productRepository.findByBrand(brandB)).thenReturn(Arrays.asList(productB1, productB2));

        LowestPriceBrandDto result = productService.getLowestPriceBrand();

        assertNotNull(result);
        assertEquals("A", result.getLowestPrice().getBrand());
        assertEquals(30000, result.getLowestPrice().getTotalPrice());
        assertEquals(2, result.getLowestPrice().getCategories().size());
    }

    /**
     * 브랜드가 없을 때 단일 브랜드 최저가 조회 기능의 예외 처리를 테스트합니다.
     * 이는 데이터베이스가 비어 있는 상황을 시뮬레이션합니다.
     */
    @Test
    void getLowestPriceBrand_NoBrandsFound() {
        when(brandRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> productService.getLowestPriceBrand());
    }

    /**
     * 카테고리별 가격 범위 조회 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 특정 카테고리의 최저가와 최고가 상품을 정확히 식별하는지 확인합니다.
     */
    @Test
    void getCategoryPriceRange_Success() {
        when(categoryRepository.findByName("상의")).thenReturn(Optional.of(category1));
        when(productRepository.findTopByCategoryOrderByPriceAsc(category1)).thenReturn(productA1);
        when(productRepository.findTopByCategoryOrderByPriceDesc(category1)).thenReturn(productB1);

        CategoryPriceRangeDto result = productService.getCategoryPriceRange("상의");

        assertNotNull(result);
        assertEquals("상의", result.getCategory());
        assertEquals("A", result.getLowestPrice().getBrand());
        assertEquals(10000, result.getLowestPrice().getPrice());
        assertEquals("B", result.getHighestPrice().getBrand());
        assertEquals(15000, result.getHighestPrice().getPrice());
    }

    /**
     * 존재하지 않는 카테고리에 대한 가격 범위 조회 시 예외 처리를 테스트합니다.
     * 이는 잘못된 입력에 대한 애플리케이션의 견고성을 검증합니다.
     */
    @Test
    void getCategoryPriceRange_CategoryNotFound() {
        when(categoryRepository.findByName("없는카테고리")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getCategoryPriceRange("없는카테고리"));
    }

    /**
     * 새로운 브랜드 추가 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 브랜드가 성공적으로 저장되고 올바른 정보를 반환하는지 확인합니다.
     */
    @Test
    void addBrand_Success() {
        String newBrandName = "New Brand";
        Brand newBrand = new Brand(3L, newBrandName);
        when(brandRepository.findByName(newBrandName)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(newBrand);

        Brand result = productService.addBrand(newBrandName);

        assertNotNull(result);
        assertEquals(newBrandName, result.getName());
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    /**
     * 이미 존재하는 브랜드 이름으로 추가를 시도할 때의 예외 처리를 테스트합니다.
     * 이는 데이터 중복을 방지하는 비즈니스 로직을 검증합니다.
     */
    @Test
    void addBrand_DuplicateBrand() {
        String existingBrandName = "A";
        when(brandRepository.findByName(existingBrandName)).thenReturn(Optional.of(brandA));

        assertThrows(DuplicateResourceException.class, () -> productService.addBrand(existingBrandName));
    }

    /**
     * 브랜드 정보 업데이트 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 브랜드 정보가 올바르게 업데이트되는지 확인합니다.
     */
    @Test
    void updateBrand_Success() {
        Long brandId = 1L;
        String newBrandName = "Updated A";
        Brand updatedBrand = new Brand(brandId, newBrandName);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brandA));
        when(brandRepository.findByName(newBrandName)).thenReturn(Optional.empty());
        when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);

        Brand result = productService.updateBrand(brandId, newBrandName);

        assertNotNull(result);
        assertEquals(brandId, result.getId());
        assertEquals(newBrandName, result.getName());
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    /**
     * 존재하지 않는 브랜드 ID로 업데이트를 시도할 때의 예외 처리를 테스트합니다.
     * 이는 잘못된 입력에 대한 애플리케이션의 견고성을 검증합니다.
     */
    @Test
    void updateBrand_BrandNotFound() {
        Long nonExistentBrandId = 99L;
        when(brandRepository.findById(nonExistentBrandId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateBrand(nonExistentBrandId, "New Name"));
    }

    /**
     * 이미 존재하는 브랜드 이름으로 업데이트를 시도할 때의 예외 처리를 테스트합니다.
     * 이는 데이터 중복을 방지하는 비즈니스 로직을 검증합니다.
     */
    @Test
    void updateBrand_DuplicateName() {
        Long brandId = 1L;
        String existingName = "B";
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brandA));
        when(brandRepository.findByName(existingName)).thenReturn(Optional.of(brandB));

        assertThrows(DuplicateResourceException.class, () -> productService.updateBrand(brandId, existingName));
    }

    /**
     * 브랜드 삭제 기능의 정상 동작을 테스트합니다.
     * 이 테스트는 브랜드가 성공적으로 삭제되는지 확인합니다.
     */
    @Test
    void deleteBrand_Success() {
        Long brandId = 1L;
        when(brandRepository.existsById(brandId)).thenReturn(true);
        doNothing().when(brandRepository).deleteById(brandId);

        assertDoesNotThrow(() -> productService.deleteBrand(brandId));
        verify(brandRepository, times(1)).deleteById(brandId);
    }

    /**
     * 존재하지 않는 브랜드 ID로 삭제를 시도할 때의 예외 처리를 테스트합니다.
     * 이는 잘못된 입력에 대한 애플리케이션의 견고성을 검증합니다.
     */
    @Test
    void deleteBrand_BrandNotFound() {
        Long nonExistentBrandId = 99L;
        when(brandRepository.existsById(nonExistentBrandId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteBrand(nonExistentBrandId));
    }
}