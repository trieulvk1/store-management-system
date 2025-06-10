package com.app.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.entity.ProductSize;

@Repository
public interface ProductSizeDAO extends JpaRepository<ProductSize, Integer> {
  @Query("SELECT ps FROM ProductSize ps WHERE ps.product.id = ?1")
  List<ProductSize> findByProductId(Long productId);
}
