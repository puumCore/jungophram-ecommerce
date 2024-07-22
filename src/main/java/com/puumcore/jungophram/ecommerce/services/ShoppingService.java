package com.puumcore.jungophram.ecommerce.services;

import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.exceptions.FailureException;
import com.puumcore.jungophram.ecommerce.exceptions.NotFoundException;
import com.puumcore.jungophram.ecommerce.models.objects.BatchSummary;
import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.repositories.AccountOps;
import com.puumcore.jungophram.ecommerce.repositories.ProductRepo;
import com.puumcore.jungophram.ecommerce.repositories.ShoppingOps;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 3:27 PM
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class ShoppingService extends Assistant {

    private final AccountOps accountOps;
    private final ProductRepo productRepo;
    private final ShoppingOps shoppingOps;

    public final GenericResponse<Integer> availableBalance(final GenericRequest<Form.ById> request) {
        log.info("Request={}", request);

        Form.ById body = request.getBody();
        if (!productRepo.existsById(body.id())) {
            throw new NotFoundException("No such product found");
        }

        final int availableQuantity = shoppingOps.getAvailableQuantity(body.id());

        GenericResponse<Integer> response = buildSuccessfulResponse(request.getHeader(), "Found available balance of the desired item", availableQuantity);
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<ShoppingCart> getCart(final String jwt, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);

        Optional<ShoppingCart> optionalShoppingCart = shoppingOps.getCustomerShoppingCart(userFromToken.getUser_id());
        if (optionalShoppingCart.isEmpty()) {
            throw new NotFoundException("You don't have a shopping cart with us");
        }

        GenericResponse<ShoppingCart> response = buildSuccessfulResponse(request.getHeader(), "Shopping cart found", optionalShoppingCart.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<BatchSummary<Object>> remove(final String jwt, final GenericRequest<Set<Long>> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);

        Set<Long> body = request.getBody();
        if (body.isEmpty()) {
            throw new BadRequestException("One or more product items are required to proceed");
        }

        Optional<ShoppingCart> optionalShoppingCart = shoppingOps.getCustomerShoppingCart(userFromToken.getUser_id());
        if (optionalShoppingCart.isEmpty()) {
            throw new NotFoundException("You do don't have a shopping cart with us");
        }
        final ShoppingCart shoppingCart = optionalShoppingCart.get();
        if (shoppingCart.getItems().isEmpty()) {
            throw new NotFoundException("You cart is empty at the moment. Add one or more items to continue");
        }

        final BatchSummary<Object> batchSummary = new BatchSummary<>();
        body.forEach(productId -> {
            if (shoppingCart.getItems().stream().noneMatch(item -> productId.equals(item.id()))) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(productId, "This item isn't included in your shopping cart"));
                return;
            }
            if (!productRepo.existsById(productId)) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(productId, "No such product found"));
                return;
            }

            boolean removed = shoppingCart.getItems().removeIf(item -> productId.equals(item.id()));
            if (removed) {
                batchSummary.getSuccessful().add(new BatchSummary.BatchItem<>(productId, "Product removed to shopping cart"));
            } else {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(productId, "We are unable to remove the item from your shopping cart"));
            }

        });

        if (batchSummary.getFailed().isEmpty()) {
            if (shoppingOps.updateShoppingCartItems(userFromToken.getUser_id(), shoppingCart.getItems(), false).isEmpty()) {
                throw new FailureException("Couldn't update your shopping cart with your desired items");
            }
        }

        final float successPercentage = ((float) batchSummary.getSuccessful().size() / body.size()) * 100;
        batchSummary.setSuccessRate(successPercentage);

        GenericResponse<BatchSummary<Object>> response = buildSuccessfulResponse(request.getHeader(), "Shopping cart item(s) successfully processed. Check the success rate for confirmation", batchSummary);
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<BatchSummary<Object>> add(final String jwt, final GenericRequest<Set<Form.CartItem>> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);

        Set<Form.CartItem> body = request.getBody();
        if (body.isEmpty()) {
            throw new BadRequestException("One or more product items are required to proceed");
        }

        final BatchSummary<Object> batchSummary = new BatchSummary<>();
        List<ShoppingCart.Item> itemList = new ArrayList<>();
        body.forEach(cartItem -> {
            Optional<Product> optionalProduct = productRepo.findById(cartItem.productId());
            if (optionalProduct.isEmpty()) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(cartItem.productId(), "No such product found"));
                return;
            }
            final int availableQuantity = shoppingOps.getAvailableQuantity(cartItem.productId());
            if (cartItem.quantity() > availableQuantity) {
                batchSummary.getFailed().add(new BatchSummary.BatchItem<>(cartItem.productId(), "This product is currently out of stock at the moment. Please try again later"));
                return;
            }

            Product product = optionalProduct.get();
            final double totalCost = cartItem.quantity() * product.getPrice();
            itemList.add(new ShoppingCart.Item(product.getProduct_id(), product.getName(), cartItem.quantity(), totalCost));
            batchSummary.getSuccessful().add(new BatchSummary.BatchItem<>(cartItem.productId(), "Product added to shopping cart"));
        });

        if (!itemList.isEmpty()) {
            Optional<ShoppingCart> optionalShoppingCart = shoppingOps.getCustomerShoppingCart(userFromToken.getUser_id());
            if (optionalShoppingCart.isEmpty()) {
                if (shoppingOps.createCart(userFromToken.getUser_id(), userFromToken.getEmail(), itemList).isEmpty()) {
                    throw new FailureException("Couldn't create a shopping cart for your items");
                }
            } else {
                if (shoppingOps.updateShoppingCartItems(userFromToken.getUser_id(), itemList, true).isEmpty()) {
                    throw new FailureException("Couldn't update your shopping cart with your desired items");
                }
            }
        }

        final float successPercentage = ((float) batchSummary.getSuccessful().size() / body.size()) * 100;
        batchSummary.setSuccessRate(successPercentage);

        GenericResponse<BatchSummary<Object>> response = buildSuccessfulResponse(request.getHeader(), "Shopping cart item(s) successfully processed. Check the success rate for confirmation", batchSummary);
        log.info("Response={}", response);
        return response;
    }

}
