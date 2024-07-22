package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;

import java.util.List;
import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 3:38 PM
 */
public interface ShoppingOps {

    Integer getAvailableQuantity(Long productId);

    Optional<ShoppingCart> getCustomerShoppingCart(Long userId);

    Optional<ShoppingCart> createCart(Long userId, String customerEmail, List<ShoppingCart.Item> items);

    Optional<ShoppingCart> updateShoppingCartItems(Long userId, List<ShoppingCart.Item> items, boolean adding);

}
