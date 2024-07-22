package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 3:18 PM
 */
@Repository
public interface ShoppingCartRepo extends MongoRepository<ShoppingCart, UUID> {
}
