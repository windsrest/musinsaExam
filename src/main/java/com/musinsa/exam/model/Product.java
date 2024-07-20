package com.musinsa.exam.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"brand_id", "category_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Integer price;
}