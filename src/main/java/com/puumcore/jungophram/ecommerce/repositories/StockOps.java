package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 3:24 PM
 */
public interface StockOps {

    @Cacheable("product")
    Optional<Product> getProduct(String name, String description, String category);

    @CacheEvict(value = {"product", "products"}, allEntries = true)
    Optional<Product> saveProduct(String name, String description, double price, int qty, String category, String url);

    Optional<Product> updateProduct(Long id, String name, String description, String category, String url);

    Optional<Product> updateProduct(Long id, Integer qty);

    Optional<Product> updateProduct(Long id, Double price);

    @Cacheable("products")
    Optional<Paged<Product>> getProducts(Pageable pageable);

    @Cacheable("products")
    Optional<Paged<Product>> getProducts(String param, Pageable pageable);

}
