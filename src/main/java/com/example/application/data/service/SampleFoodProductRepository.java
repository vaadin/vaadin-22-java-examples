package com.example.application.data.service;

import com.example.application.data.entity.SampleFoodProduct;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleFoodProductRepository extends JpaRepository<SampleFoodProduct, UUID> {

}