package com.puumcore.jungophram.ecommerce.controller;

import com.puumcore.jungophram.ecommerce.models.objects.BatchSummary;
import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;
import com.puumcore.jungophram.ecommerce.services.ShoppingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 5:52 PM
 */

@RestController
@RequestMapping(path = "shopping-cart", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class ShoppingCtrl {

    private final ShoppingService service;

    @Operation(
            summary = "Update cart",
            description = "Adds the desired items to the requesting user's cart. When a cart is not found, a new one is created",
            tags = "shopping-cart-mgnt"
    )
    @PutMapping("add")
    GenericResponse<BatchSummary<Object>> add(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Set<Form.CartItem>> request) {
        return service.add(jwt, request);
    }

    @Operation(
            summary = "Update cart",
            description = "Removes the desired items from the requesting user's cart",
            tags = "shopping-cart-mgnt"
    )
    @PutMapping("remove")
    GenericResponse<BatchSummary<Object>> remove(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Set<Long>> request) {
        return service.remove(jwt, request);
    }

    @Operation(
            summary = "Get cart",
            description = "Returns a copy of the requesting user's shopping cart",
            tags = "shopping-cart-mgnt"
    )
    @PostMapping("get")
    GenericResponse<ShoppingCart> getCart(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.getCart(jwt, request);
    }

    @Operation(
            summary = "Get available balance",
            description = "Returns the available balance of the item provided by taking into account the existence of the product across all existing shopping carts",
            tags = "shopping-cart-mgnt"
    )
    @PostMapping("available-balance")
    GenericResponse<Integer> availableBalance(@RequestBody @NonNull final GenericRequest<Form.ById> request) {
        return service.availableBalance(request);
    }

}
