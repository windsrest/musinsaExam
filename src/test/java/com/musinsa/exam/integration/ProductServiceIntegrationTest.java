package com.musinsa.exam.integration;

import com.musinsa.exam.dto.LowestPriceBrandsDto;
import com.musinsa.exam.dto.LowestPriceBrandDto;
import com.musinsa.exam.dto.CategoryPriceRangeDto;
import com.musinsa.exam.model.Brand;
import com.musinsa.exam.model.Category;
import com.musinsa.exam.model.Product;
import com.musinsa.exam.repository.BrandRepository;
import com.musinsa.exam.repository.CategoryRepository;
import com.musinsa.exam.repository.ProductRepository;
import com.musinsa.exam.service.ProductService;
import com.musinsa.exam.exception.ResourceNotFoundException;
import com.musinsa.exam.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // 기존 데이터 모두 삭제
        productRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        // 테스트 데이터 설정
        Brand brandA = brandRepository.save(new Brand(null, "A"));
        Brand brandB = brandRepository.save(new Brand(null, "B"));

        Category category1 = categoryRepository.save(new Category(null, "상의"));
        Category category2 = categoryRepository.save(new Category(null, "하의"));

        productRepository.save(new Product(null, brandA, category1, 10000));
        productRepository.save(new Product(null, brandA, category2, 20000));
        productRepository.save(new Product(null, brandB, category1, 15000));
        productRepository.save(new Product(null, brandB, category2, 25000));
    }

    /**
     * 최저가 브랜드 조회 기능 테스트
     *
     * 목적:
     * 1. 여러 브랜드와 카테고리가 존재할 때 각 카테고리별 최저가 브랜드를 정확히 식별하는지 검증
     * 2. 총액 계산이 정확한지 확인
     *
     * 테스트 설계 이유:
     * - 실제 데이터베이스 조회를 통해 복잡한 비즈니스 로직(최저가 계산)이 정확히 작동하는지 확인
     * - 여러 테이블 간의 조인 및 집계 연산이 올바르게 수행되는지 검증
     * - 성능 측면에서 대량의 데이터 처리 시 발생할 수 있는 잠재적 문제를 식별
     */
    @Test
    void getLowestPriceBrands() {
        LowestPriceBrandsDto result = productService.getLowestPriceBrands();

        assertNotNull(result);
        assertEquals(2, result.getCategoryPrices().size());
        assertEquals(30000, result.getTotalPrice());
    }

    /**
     * 단일 브랜드 최저가 조회 기능 테스트
     *
     * 목적:
     * 1. 모든 카테고리 제품의 총액이 가장 낮은 브랜드를 정확히 식별하는지 검증
     * 2. 해당 브랜드의 각 카테고리별 가격과 총액이 정확한지 확인
     *
     * 테스트 설계 이유:
     * - 브랜드 간 총액 비교 로직이 올바르게 작동하는지 확인
     * - 데이터베이스에서 복잡한 조건의 데이터를 정확히 조회하고 계산하는지 검증
     * - 실제 환경에서 발생할 수 있는 동일 총액 상황 등의 엣지 케이스 처리를 확인
     */
    @Test
    void getLowestPriceBrand() {
        LowestPriceBrandDto result = productService.getLowestPriceBrand();

        assertNotNull(result);
        assertEquals("A", result.getLowestPrice().getBrand());
        assertEquals(30000, result.getLowestPrice().getTotalPrice());
    }

    /**
     * 카테고리별 가격 범위 조회 기능 테스트
     *
     * 목적:
     * 1. 특정 카테고리 내에서 최저가와 최고가 브랜드 및 가격을 정확히 식별하는지 검증
     *
     * 테스트 설계 이유:
     * - 카테고리별 가격 비교 로직이 정확히 작동하는지 확인
     * - 데이터베이스에서 특정 조건(카테고리)에 따른 데이터 필터링이 올바르게 수행되는지 검증
     * - 최저가와 최고가가 동일한 경우 등의 특수 상황 처리를 확인
     */
    @Test
    void getCategoryPriceRange() {
        CategoryPriceRangeDto result = productService.getCategoryPriceRange("상의");

        assertNotNull(result);
        assertEquals("상의", result.getCategory());
        assertEquals("A", result.getLowestPrice().getBrand());
        assertEquals(10000, result.getLowestPrice().getPrice());
        assertEquals("B", result.getHighestPrice().getBrand());
        assertEquals(15000, result.getHighestPrice().getPrice());
    }

    /**
     * 새 브랜드 추가 기능 테스트
     *
     * 목적:
     * 1. 새로운 브랜드가 데이터베이스에 정상적으로 추가되는지 검증
     * 2. 추가된 브랜드의 정보가 정확한지 확인
     *
     * 테스트 설계 이유:
     * - 데이터 삽입 기능이 올바르게 작동하는지 확인
     * - 트랜잭션 처리가 정상적으로 이루어지는지 검증
     * - 데이터베이스의 제약 조건(예: 유니크 제약)이 잘 작동하는지 확인
     */
    @Test
    void addBrand() {
        Brand newBrand = productService.addBrand("C");

        assertNotNull(newBrand);
        assertEquals("C", newBrand.getName());
        assertTrue(brandRepository.findById(newBrand.getId()).isPresent());
    }

    /**
     * 중복 브랜드 추가 시 예외 처리 테스트
     *
     * 목적:
     * 1. 이미 존재하는 브랜드 이름으로 추가 시도 시 적절한 예외가 발생하는지 검증
     *
     * 테스트 설계 이유:
     * - 예외 처리 로직이 올바르게 작동하는지 확인
     * - 데이터 무결성이 유지되는지 검증
     * - 사용자 입력 오류에 대한 애플리케이션의 견고성 확인
     */
    @Test
    void addBrand_DuplicateName() {
        assertThrows(DuplicateResourceException.class, () -> productService.addBrand("A"));
    }

    /**
     * 브랜드 정보 업데이트 기능 테스트
     *
     * 목적:
     * 1. 기존 브랜드의 이름이 성공적으로 업데이트되는지 검증
     * 2. 업데이트된 정보가 데이터베이스에 정확히 반영되는지 확인
     *
     * 테스트 설계 이유:
     * - 데이터 수정 기능이 올바르게 작동하는지 확인
     * - 트랜잭션 내에서 데이터 변경이 정상적으로 처리되는지 검증
     * - 업데이트 후 조회 시 변경된 정보가 정확히 반환되는지 확인
     * - 실제 환경에서 발생할 수 있는 동시성 문제 등을 간접적으로 테스트
     */
    @Test
    void updateBrand() {
        Brand brandA = brandRepository.findByName("A").orElseThrow();
        Brand updatedBrand = productService.updateBrand(brandA.getId(), "A_Updated");

        assertNotNull(updatedBrand);
        assertEquals("A_Updated", updatedBrand.getName());
    }

    /**
     * 존재하지 않는 브랜드 업데이트 시 예외 처리 테스트
     *
     * 목적:
     * 1. 존재하지 않는 브랜드 ID로 업데이트 시도 시 적절한 예외가 발생하는지 검증
     *
     * 테스트 설계 이유:
     * - 예외 처리 로직이 올바르게 작동하는지 확인
     * - 존재하지 않는 리소스에 대한 요청 처리의 견고성 검증
     * - 데이터 무결성이 유지되는지 확인 (존재하지 않는 데이터는 수정되지 않아야 함)
     * - 사용자 입력 오류나 잘못된 API 호출에 대한 애플리케이션의 대응 능력 테스트
     */
    @Test
    void updateBrand_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> productService.updateBrand(999L, "NonExistent"));
    }

    /**
     * 브랜드 삭제 기능 테스트
     *
     * 목적:
     * 1. 기존 브랜드가 성공적으로 삭제되는지 검증
     * 2. 삭제 후 해당 브랜드가 더 이상 데이터베이스에 존재하지 않는지 확인
     *
     * 테스트 설계 이유:
     * - 데이터 삭제 기능이 올바르게 작동하는지 확인
     * - 트랜잭션 내에서 삭제 작업이 정상적으로 처리되는지 검증
     * - 삭제 후 조회 시 데이터가 실제로 제거되었는지 확인
     * - 관련된 다른 엔티티(예: Product)에 미치는 영향을 검증 (만약 연관 관계가 있다면)
     * - 캐시나 다른 저장소와의 동기화 문제 여부를 간접적으로 테스트
     */
    @Test
    void deleteBrand() {
        Brand brandToDelete = brandRepository.findByName("B").orElseThrow();
        productService.deleteBrand(brandToDelete.getId());

        assertFalse(brandRepository.findById(brandToDelete.getId()).isPresent());
    }

    /**
     * 존재하지 않는 브랜드 삭제 시 예외 처리 테스트
     *
     * 목적:
     * 1. 존재하지 않는 브랜드 ID로 삭제 시도 시 적절한 예외가 발생하는지 검증
     *
     * 테스트 설계 이유:
     * - 예외 처리 로직이 올바르게 작동하는지 확인
     * - 존재하지 않는 리소스에 대한 삭제 요청 처리의 견고성 검증
     * - 데이터 무결성이 유지되는지 확인 (존재하지 않는 데이터 삭제 시도로 인한 부작용이 없어야 함)
     * - 사용자 입력 오류나 잘못된 API 호출에 대한 애플리케이션의 대응 능력 테스트
     * - 데이터베이스의 일관성이 유지되는지 확인
     */
    @Test
    void deleteBrand_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteBrand(999L));
    }
}