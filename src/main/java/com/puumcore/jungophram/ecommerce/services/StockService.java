package com.puumcore.jungophram.ecommerce.services;

import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.exceptions.FailureException;
import com.puumcore.jungophram.ecommerce.exceptions.NotFoundException;
import com.puumcore.jungophram.ecommerce.models.objects.*;
import com.puumcore.jungophram.ecommerce.repositories.ProductRepo;
import com.puumcore.jungophram.ecommerce.repositories.StockOps;
import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 1:55 PM
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService extends Assistant {

    private final StockOps stockOps;
    private final ProductRepo productRepo;

    public final GenericResponse<Paged<Product>> filter(final Pageable pageable, final GenericRequest<Form.Search> request) {
        log.info("Request={}", request);

        Form.Search body = request.getBody();

        Optional<Paged<Product>> optionalPaged = Optional.ofNullable(body.param()).orElse("").isBlank() ? stockOps.getProducts(pageable) : stockOps.getProducts(body.param(), pageable);
        if (optionalPaged.isEmpty()) {
            throw new NotFoundException("No products found");
        }

        GenericResponse<Paged<Product>> response = buildSuccessfulResponse(request.getHeader(), "Here are the requested products", optionalPaged.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Product> delete(final Long id, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new NotFoundException("No such product found");
        }

        productRepo.deleteById(id);

        GenericResponse<Product> response = buildSuccessfulResponse(request.getHeader(), "Stock item successfully deleted. Attached is the deleted item", optionalProduct.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Product> byId(final Long id, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new NotFoundException("No such product found");
        }

        GenericResponse<Product> response = buildSuccessfulResponse(request.getHeader(), "Stock item found", optionalProduct.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Product> updateQuantity(final GenericRequest<Form.StockToUpdateQty> request) {
        log.info("Request={}", request);

        Form.StockToUpdateQty body = request.getBody();
        if (body.qty() < 0) {
            throw new BadRequestException("Invalid quantity");
        }

        if (productRepo.findById(body.id()).isEmpty()) {
            throw new NotFoundException("No such product found");
        }

        Optional<Product> optionalProduct = stockOps.updateProduct(body.id(), body.qty());
        if (optionalProduct.isEmpty()) {
            throw new FailureException("The stock item quantity couldn't be updated");
        }

        GenericResponse<Product> response = buildSuccessfulResponse(request.getHeader(), "Stock item price successfully updated", optionalProduct.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Product> updatePrice(final GenericRequest<Form.StockToUpdatePrice> request) {
        log.info("Request={}", request);

        Form.StockToUpdatePrice body = request.getBody();
        if (body.price() < 0.0) {
            throw new BadRequestException("Invalid price");
        }

        if (productRepo.findById(body.id()).isEmpty()) {
            throw new NotFoundException("No such product found");
        }

        Optional<Product> optionalProduct = stockOps.updateProduct(body.id(), body.price());
        if (optionalProduct.isEmpty()) {
            throw new FailureException("The stock item price couldn't be updated");
        }

        GenericResponse<Product> response = buildSuccessfulResponse(request.getHeader(), "Stock item price successfully updated", optionalProduct.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Product> updateInfo(final GenericRequest<Form.StockToUpdate> request) {
        log.info("Request={}", request);

        Form.StockToUpdate body = request.getBody();

        if (productRepo.findById(body.id()).isEmpty()) {
            throw new NotFoundException("No such product found");
        }

        Optional.ofNullable(body.name())
                .ifPresent(s -> {
                    if (s.isBlank() || containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Product name is invalid");
                    }
                });
        Optional.ofNullable(body.description())
                .ifPresent(s -> {
                    if (s.isBlank() || containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Product description is invalid");
                    }
                });
        Optional.ofNullable(body.category())
                .ifPresent(s -> {
                    if (s.isBlank() || containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Product category is invalid");
                    }
                });
        Optional.ofNullable(body.category())
                .ifPresent(s -> {
                    if (s.isBlank() || !correctHttpUrlFormat(s)) {
                        throw new BadRequestException("Product image URL is invalid. Provide only a http(s) endpoint");
                    }
                });

        Optional<Product> optionalProduct = stockOps.updateProduct(
                body.id(),
                body.name(),
                body.description(),
                body.category(),
                body.category()
        );
        if (optionalProduct.isEmpty()) {
            throw new FailureException("The stock item information couldn't be updated");
        }

        GenericResponse<Product> response = buildSuccessfulResponse(request.getHeader(), "Stock item information successfully updated", optionalProduct.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<BatchSummary<String>> create(final GenericRequest<Set<Form.StockToAdd>> request) {
        log.info("Request={}", request);

        Set<Form.StockToAdd> body = request.getBody();
        if (body.isEmpty()) {
            throw new BadRequestException("One or more product items are required to proceed");
        }
        final BatchSummary<String> batchSummary = new BatchSummary<>();
        body.forEach(stockToAdd -> {
            if (stockToAdd.name().isBlank() || containsSpecialCharacter(stockToAdd.name().replace(" ", ""))) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product name is invalid"));
                return;
            }
            if (stockToAdd.description().isBlank()) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product description is invalid"));
                return;
            }
            if (stockToAdd.category().isBlank() || containsSpecialCharacter(stockToAdd.category().replace(" ", ""))) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product category is invalid"));
                return;
            }
            if (!Optional.ofNullable(stockToAdd.image()).orElse("").isBlank()
                    && !correctHttpUrlFormat(stockToAdd.image())) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product image URL is invalid. Provide only a http(s) endpoint"));
                return;
            }
            Optional<Product> optionalProduct = stockOps.getProduct(stockToAdd.name(), stockToAdd.description(), stockToAdd.category());
            if (optionalProduct.isPresent()) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product already exists. Consider updating the product description"));
                return;
            }
            Optional<Product> savedProductOptional = stockOps.saveProduct(
                    stockToAdd.name(),
                    stockToAdd.description(),
                    stockToAdd.price(),
                    stockToAdd.quantity(),
                    stockToAdd.category(),
                    stockToAdd.image()
            );
            if (savedProductOptional.isEmpty()) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "We are unable to add this product to stock"));
            } else {
                batchSummary.getSuccessful().add(new BatchSummary.BatchItem<>(stockToAdd.name(), "Product added to stock"));
            }
        });
        final float successPercentage = ((float) batchSummary.getSuccessful().size() / body.size()) * 100;
        batchSummary.setSuccessRate(successPercentage);

        GenericResponse<BatchSummary<String>> response = buildSuccessfulResponse(request.getHeader(), "Product(s) successfully processed. Check the success rate for confirmation", batchSummary);
        log.info("Response={}", response);
        return response;
    }

}
