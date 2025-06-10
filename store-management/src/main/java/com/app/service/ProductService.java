package com.app.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.app.entity.Product;

public interface ProductService {
	List<Product> findAll();

	Product findById(Integer id);

	Product create(Product product);

	Product update(Product product);

	void delete(Integer id);
}
