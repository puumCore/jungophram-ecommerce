package com.puumcore.jungophram.ecommerce.controller;

import com.puumcore.jungophram.ecommerce.models.objects.*;
import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import com.puumcore.jungophram.ecommerce.services.StockService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 12:29 PM
 */

@RestController
@RequestMapping(path = "products", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class StockCtrl {

    private final StockService service;

    @Operation(
            summary = "Add stock",
            description = """
                    Batch process desired products in a non-blocking manner and returns a summary for each item.
                    The success rate is a percentage value indicating the products that were successfully added.
                    """,
            tags = "stock-mgnt"
    )
    @PostMapping("add")
    GenericResponse<BatchSummary<String>> create(@RequestBody @NonNull final GenericRequest<Set<Form.StockToAdd>> request) {
        return service.create(request);
    }

    @Operation(
            summary = "Update stock",
            description = "Updates stock item information",
            tags = "stock-mgnt"
    )
    @PatchMapping("update-info")
    GenericResponse<Product> updateInfo(@RequestBody @NonNull final GenericRequest<Form.StockToUpdate> request) {
        return service.updateInfo(request);
    }

    @Operation(
            summary = "Update stock",
            description = "Updates stock item quantity",
            tags = "stock-mgnt"
    )
    @PutMapping("update-qty")
    GenericResponse<Product> updateQuantity(@RequestBody @NonNull final GenericRequest<Form.StockToUpdateQty> request) {
        return service.updateQuantity(request);
    }

    @Operation(
            summary = "Update stock",
            description = "Updates stock item price",
            tags = "stock-mgnt"
    )
    @PutMapping("update-price")
    GenericResponse<Product> updatePrice(@RequestBody @NonNull final GenericRequest<Form.StockToUpdatePrice> request) {
        return service.updatePrice(request);
    }

    @Operation(
            summary = "Delete stock",
            description = "Deletes a stock item and returns a copy of the deleted item",
            tags = "stock-mgnt"
    )
    @DeleteMapping(path = "remove", params = "id")
    GenericResponse<Product> delete(@RequestParam(value = "id") @NonNull Long itemId, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.delete(itemId, request);
    }

    @Operation(
            summary = "Get stock item",
            description = "Fetch stock item by Id",
            tags = "stock-mgnt"
    )
    @PostMapping(path = "byId", params = "id")
    GenericResponse<Product> byId(@RequestParam(value = "id") @NonNull Long itemId, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.byId(itemId, request);
    }

    @Operation(
            summary = "Filter stock",
            description = "Enables users to filter through existing stock items even with a desired parameter",
            tags = "stock-mgnt"
    )
    @PostMapping("filter")
    GenericResponse<Paged<Product>> filter(
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault(sort = "product_id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestBody @NonNull final GenericRequest<Form.Search> request
    ) {
        return service.filter(pageable, request);
    }

}
