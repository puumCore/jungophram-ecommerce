package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 1:37 PM
 */
@Repository
public interface ProductRepo extends MongoRepository<Product, Long> {
}
