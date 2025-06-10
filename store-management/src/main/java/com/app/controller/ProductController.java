package com.app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.dao.CategoryDAO;
import com.app.dao.ProductDAO;
import com.app.entity.Category;
import com.app.entity.Product;
import com.app.entity.ProductSize;
import com.app.service.CategoryService;
import com.app.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("product")
public class ProductController {

	@Autowired
	ProductService productService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	CategoryDAO cdao;

	@Autowired
	ProductDAO pdao;

	@Autowired
	HttpSession session;

	@ModelAttribute("categoryCounts")
	public Map<Category, Long> categoryCounts() {
		return categoryService.countProductsByCategory();
	}

	@RequestMapping("list")
	public String list(Model model, 
										@RequestParam("cid") Optional<String> cid,
										@RequestParam("sortType") Optional<String> sortType,
										@RequestParam(defaultValue = "1") int pageNo,
										@RequestParam(defaultValue = "10") int pageSize) {
		Sort sort;
		Page<Product> page;
		if (sortType.isPresent() && sortType.get().equalsIgnoreCase("desc")) {
			sort = Sort.by(Sort.Direction.DESC, "price");
			model.addAttribute("sortType", sortType.get());
		} else if (sortType.isPresent() && sortType.get().equalsIgnoreCase("asc")){
			sort = Sort.by(Sort.Direction.ASC, "price");
			model.addAttribute("sortType", sortType.get());
		} else{
			sort = Sort.by(Sort.Direction.ASC, "name");
		}
		
		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		if (cid.isPresent()) {
			session.setAttribute("categoryId", cid.get());
			page = pdao.findByCategoryId(cid.get(), pageable);
			model.addAttribute("cname", cdao.getReferenceById(Integer.parseInt(cid.get())).getName());
			model.addAttribute("cid", cdao.getReferenceById(Integer.parseInt(cid.get())).getId());
		} else {
			page = pdao.findAll(pageable);
			model.addAttribute("cname", "ALL");
		}
		model.addAttribute("page", page);
		return "product/list";
	}

	@GetMapping("filter")
	public String range(Model model,
                    @RequestParam(required = false) Double fromPrice,
                    @RequestParam(required = false) Double toPrice,
                    @RequestParam(required = false) List<Integer> categoryIds,
										@RequestParam("sortType") Optional<String> sortType,
										@RequestParam(defaultValue = "1") int pageNo,
										@RequestParam(defaultValue = "10") int pageSize) {
    Page<Product> page;
		String showCondition = "";
		String categoryParam = "";
		Sort sort;
		if (sortType.isPresent() && sortType.get().equalsIgnoreCase("desc")) {
			sort = Sort.by(Sort.Direction.DESC, "price");
			model.addAttribute("sortType", sortType.get());
		} else if (sortType.isPresent() && sortType.get().equalsIgnoreCase("asc")){
			sort = Sort.by(Sort.Direction.ASC, "price");
			model.addAttribute("sortType", sortType.get());
		} else{
			sort = Sort.by(Sort.Direction.ASC, "name");
		}	

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);

		List<Category> categoryEntities = new ArrayList<>();

    if (categoryIds != null && !categoryIds.isEmpty()) {
			for (Integer categoryId : categoryIds) {
				Optional<Category> category = cdao.findById(categoryId);

				categoryParam += "&categoryIds=" + String.valueOf(categoryId);

				if (category != null) {
					categoryEntities.add(category.get());
				}
			}

			model.addAttribute("categoryIds", categoryParam);

      page = pdao.findByPriceBetweenAndCategoryIn(fromPrice, toPrice, categoryEntities, pageable); 
    } else {

			page = pdao.findByPriceBetween(fromPrice,toPrice, pageable);
    }

		for (Category category : categoryEntities) {
			if(category == categoryEntities.get(categoryEntities.size() - 1)) {
				showCondition += category.getName();
			} else {
				showCondition += category.getName() + ", ";
			}
		}
		// page.nextPageable();
		model.addAttribute("fromPrice", String.valueOf(fromPrice.intValue()));
		model.addAttribute("toPrice", String.valueOf(toPrice.intValue()));

		model.addAttribute("page", page);

		if (categoryIds == null) {
			model.addAttribute("cname", "$" + fromPrice + " to " + "$" + toPrice);
		} else if(categoryIds != null && (fromPrice != 0 || toPrice != 5000000)) {
			model.addAttribute("cname", showCondition + " $" + fromPrice + " to " + "$" + toPrice);
		} else {
			model.addAttribute("cname", showCondition);
		}
   
    return "product/list_filter";
	}

	@RequestMapping("search")
	public String search(Model model, @RequestParam("name") String name,
											@RequestParam(defaultValue = "1") int pageNo,
											@RequestParam(defaultValue = "10") int pageSize,
											@RequestParam("sortType") Optional<String> sortType) {
		Sort sort;
		if (sortType.isPresent() && sortType.get().equalsIgnoreCase("desc")) {
			sort = Sort.by(Sort.Direction.DESC, "price");
			model.addAttribute("sortType", sortType.get());
		} else if (sortType.isPresent() && sortType.get().equalsIgnoreCase("asc")){
			sort = Sort.by(Sort.Direction.ASC, "price");
			model.addAttribute("sortType", sortType.get());
		} else{
			sort = Sort.by(Sort.Direction.ASC, "name");
		}	

		Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
		Page<Product> page = pdao.findByName("%" + name + "%", pageable);

		model.addAttribute("page", page);

		if(name.isEmpty()) {
			model.addAttribute("cname", "ALL");
		}else {
			model.addAttribute("cname", name);
		}

		model.addAttribute("name", name);

		return "product/list_search";
	}

	@GetMapping("{id}")
	public String getMethodName(@PathVariable Integer id, Model model) {

		Product product = pdao.getReferenceById(id);
		Pageable pageable = PageRequest.of(0, 5);
		Page<Product> page = pdao.findByCategoryId(product.getCategory().getId().toString(), pageable);

		List<ProductSize> size = pdao.findSizesByProductId(id);

		model.addAttribute("size", size);
		model.addAttribute("page", page);
		model.addAttribute("product", product);
		return "product/detail";
	}
	
}
