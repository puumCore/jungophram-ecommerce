package com.puumcore.jungophram.ecommerce.controller;

import com.puumcore.jungophram.ecommerce.models.constants.OrderStatus;
import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.Order;
import com.puumcore.jungophram.ecommerce.services.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 10:37 PM
 */

@RestController
@RequestMapping(path = "orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class OrderCtrl {

    private final OrdersService service;

    @Operation(
            summary = "Make order",
            description = "Creates an order for the requesting user from their existing shopping cart",
            tags = "orders-mgnt"
    )
    @PostMapping("make")
    GenericResponse<Order> make(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.make(jwt, request);
    }

    @Operation(
            summary = "Get order",
            description = "Fetch order by Id",
            tags = "orders-mgnt"
    )
    @PostMapping(path = "byId", params = "id")
    GenericResponse<Order> byId(@RequestBody @NonNull final GenericRequest<Form.OrderById> request) {
        return service.byId(request);
    }

    @Operation(
            summary = "Complete order",
            description = "Completes only pending orders and updates the stock quantity of the affected items",
            tags = "orders-mgnt"
    )
    @PutMapping("complete")
    GenericResponse<Order> complete(@RequestBody @NonNull final GenericRequest<Form.OrderById> request) {
        return service.updateStatus(OrderStatus.COMPLETED, request);
    }

    @Operation(
            summary = "Cancel order",
            description = "Cancels only pending orders",
            tags = "orders-mgnt"
    )
    @PutMapping("cancel")
    GenericResponse<Order> cancel(@RequestBody @NonNull final GenericRequest<Form.OrderById> request) {
        return service.updateStatus(OrderStatus.CANCELED, request);
    }

    @Operation(
            summary = "Filter orders",
            description = "Enables the requesting user to filter through existing orders even with a desired parameter",
            tags = "orders-mgnt"
    )
    @PostMapping("filter/byUser")
    GenericResponse<Paged<Order>> filter(
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault(sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt,
            @RequestBody @NonNull final GenericRequest<Form.Search> request
    ) {
        return service.getOrders(jwt, pageable, request);
    }

    @Operation(
            summary = "Filter orders",
            description = "Enables the admin to filter through existing orders even with a desired parameter",
            tags = "orders-mgnt"
    )
    @PostMapping("filter")
    GenericResponse<Paged<Order>> filter(
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault(sort = "orderDate", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestBody @NonNull final GenericRequest<Form.Search> request
    ) {
        return service.getOrders(pageable, request);
    }

}
