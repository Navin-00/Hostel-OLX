package com.hostelolx.management.controller;

import com.hostelolx.management.entity.Product;
import com.hostelolx.management.entity.User;
import com.hostelolx.management.model.ProductDTO;
import com.hostelolx.management.repository.ProductRepository;
import com.hostelolx.management.repository.UserRepository;
import com.hostelolx.management.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired // <-- this was missing
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // Add product
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductDTO productDTO) {
        User user = userRepository.findById(productDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setUser(user); // 👈 sets relationship

        // Convert base64 image to byte[]
        if (productDTO.getImageBase64() != null) {
            product.setImage(Base64.getDecoder().decode(productDTO.getImageBase64()));
        }

        productRepository.save(product);
        return ResponseEntity.ok("Product saved successfully");
    }


    // Get all products
    // ✅ Get all products
    @GetMapping("/all")
    public List<Map<String, Object>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("price", p.getPrice());
            map.put("userId", p.getUser() != null ? p.getUser().getId() : null);
            map.put("userName", p.getUser() != null ? p.getUser().getName() : null);
            map.put("image", p.getImage() != null ? Base64.getEncoder().encodeToString(p.getImage()) : null);
            result.add(map);
        }

        return result;
    }


    // Get single product by ID
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // Update product
    @PutMapping("/update/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {

        // ✅ Fetch user first
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // ✅ Create and set updated fields
        Product updatedProduct = new Product();
        updatedProduct.setName(name);
        updatedProduct.setDescription(description);
        updatedProduct.setPrice(price);
        updatedProduct.setUser(user); // ✅ Correctly set user

        if (imageFile != null && !imageFile.isEmpty()) {
            updatedProduct.setImage(imageFile.getBytes());
        }

        // ✅ Delegate update logic to service
        return productService.updateProduct(id, updatedProduct);
    }


    // Delete product
    @DeleteMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? "Product deleted successfully" : "Product not found";
    }

    // Get products by user
    @GetMapping("/user/{userId}")
    public List<Map<String, Object>> getProductsByUser(@PathVariable Long userId) {
        // ✅ Fetch all products where the user ID matches
        List<Product> products = productRepository.findByUser_Id(userId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Product p : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("price", p.getPrice());

            // ✅ Access nested user ID
            map.put("userId", p.getUser() != null ? p.getUser().getId() : null);
            map.put("userName", p.getUser() != null ? p.getUser().getName() : null);

            // ✅ Convert image to Base64 if exists
            map.put("image", p.getImage() != null ? Base64.getEncoder().encodeToString(p.getImage()) : null);

            result.add(map);
        }

        return result;
    }


}
