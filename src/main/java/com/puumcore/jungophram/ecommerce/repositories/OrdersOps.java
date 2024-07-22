package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.models.constants.OrderStatus;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.Order;
import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 7:27 PM
 */

public interface OrdersOps {

    @CacheEvict(value = {"orders", "customer_orders"}, allEntries = true)
    Optional<Order> create(ShoppingCart shoppingCart);

    @CacheEvict(value = {"orders"}, allEntries = true)
    Optional<Order> updateStatus(UUID orderId, OrderStatus status);

    @Cacheable("orders")
    Optional<Paged<Order>> getOrders(Pageable pageable);

    @Cacheable("orders")
    Optional<Paged<Order>> filterOrders(String param, Pageable pageable);

    @Cacheable("customer_orders")
    Optional<Paged<Order>> getOrders(Long userId, Pageable pageable);

    @Cacheable("customer_orders")
    Optional<Paged<Order>> filterOrders(Long userId, String param, Pageable pageable);

}
