package com.musinsa.exam.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BRAND")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}