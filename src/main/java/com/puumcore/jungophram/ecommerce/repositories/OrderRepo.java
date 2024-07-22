package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.repositories.entities.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 7:41 PM
 */

@Repository
public interface OrderRepo extends MongoRepository<Order, UUID> {
}
