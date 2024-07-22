package com.puumcore.jungophram.ecommerce.services;

import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.exceptions.FailureException;
import com.puumcore.jungophram.ecommerce.exceptions.NotFoundException;
import com.puumcore.jungophram.ecommerce.models.constants.OrderStatus;
import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.*;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import com.puumcore.jungophram.ecommerce.repositories.entities.Order;
import com.puumcore.jungophram.ecommerce.repositories.entities.Product;
import com.puumcore.jungophram.ecommerce.repositories.entities.ShoppingCart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 8:09 PM
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class OrdersService extends Assistant {

    private final OrdersOps ordersOps;
    private final OrderRepo orderRepo;
    private final ShoppingOps shoppingOps;
    private final AccountOps accountOps;
    private final ShoppingCartRepo shoppingCartRepo;
    private final ProductRepo productRepo;
    private final StockOps stockOps;

    public final GenericResponse<Order> byId(final GenericRequest<Form.OrderById> request) {
        log.info("Request={}", request);

        Form.OrderById body = request.getBody();
        Optional<Order> optionalOrder = orderRepo.findById(body.id());
        if (optionalOrder.isEmpty()) {
            throw new NotFoundException("You are yet to make an order with us");
        }
        Order order = optionalOrder.get();

        GenericResponse<Order> response = buildSuccessfulResponse(request.getHeader(), "Order found", order);
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Paged<Order>> getOrders(final String jwt, final Pageable pageable, final GenericRequest<Form.Search> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);

        Form.Search body = request.getBody();
        Optional.ofNullable(body.param())
                .ifPresent(s -> {
                    if (!s.isBlank() && containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Invalid search param");
                    }
                });

        Optional<Paged<Order>> optionalPaged = Optional.ofNullable(body.param()).orElse("").isBlank() ? ordersOps.getOrders(userFromToken.getUser_id(), pageable) : ordersOps.filterOrders(userFromToken.getUser_id(), body.param(), pageable);
        if (optionalPaged.isEmpty() || optionalPaged.get().getTotalPages() == 0) {
            throw new NotFoundException("No orders found");
        }

        GenericResponse<Paged<Order>> response = buildSuccessfulResponse(request.getHeader(), "Here are the requested orders", optionalPaged.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Paged<Order>> getOrders(final Pageable pageable, final GenericRequest<Form.Search> request) {
        log.info("Request={}", request);

        Form.Search body = request.getBody();
        Optional.ofNullable(body.param())
                .ifPresent(s -> {
                    if (!s.isBlank() && containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Invalid search param");
                    }
                });

        Optional<Paged<Order>> optionalPaged = Optional.ofNullable(body.param()).orElse("").isBlank() ? ordersOps.getOrders(pageable) : ordersOps.filterOrders(body.param(), pageable);
        if (optionalPaged.isEmpty() || optionalPaged.get().getTotalPages() == 0) {
            throw new NotFoundException("No orders found");
        }

        GenericResponse<Paged<Order>> response = buildSuccessfulResponse(request.getHeader(), "Here are the requested orders", optionalPaged.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Order> updateStatus(final OrderStatus status, final GenericRequest<Form.OrderById> request) {
        log.info("Request={}", request);

        Form.OrderById body = request.getBody();
        Optional<Order> optionalOrder = orderRepo.findById(body.id());
        if (optionalOrder.isEmpty()) {
            throw new NotFoundException("You are yet to make an order with us");
        }
        Order order = optionalOrder.get();
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Sorry, only pending orders can be handled. Your target order has a read only status.");
        }

        ShoppingCart shoppingCart = order.getCart();

        if (status == OrderStatus.COMPLETED) {
            shoppingCart.getItems()
                    .forEach(item -> productRepo.findById(item.id())
                            .ifPresent(product -> {
                                final int updatedQuantity = product.getStockQuantity() - item.quantity();
                                Optional<Product> optionalProduct = stockOps.updateProduct(item.id(), Math.max(updatedQuantity, 0));
                                if (optionalProduct.isEmpty()) {
                                    throw new FailureException("We are unable to process this order");
                                }
                            }));
        }

        Optional<Order> orderOptional = ordersOps.updateStatus(body.id(), status);
        if (orderOptional.isEmpty()) {
            throw new FailureException("Couldn't update your order to %s".formatted(status.name().toLowerCase(Locale.ROOT)));
        }

        GenericResponse<Order> response = buildSuccessfulResponse(request.getHeader(), "Successfully %s your order".formatted(status.name().toLowerCase(Locale.ROOT)), orderOptional.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Order> make(final String jwt, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);

        Optional<ShoppingCart> optionalShoppingCart = shoppingOps.getCustomerShoppingCart(userFromToken.getUser_id());
        if (optionalShoppingCart.isEmpty()) {
            throw new NotFoundException("You don't have a shopping cart with us");
        }
        final ShoppingCart shoppingCart = optionalShoppingCart.get();
        if (shoppingCart.getItems().isEmpty()) {
            throw new NotFoundException("You cart is empty at the moment. Add one or more items to continue");
        }

        shoppingCartRepo.deleteById(shoppingCart.getId());

        Optional<Order> orderOptional = ordersOps.create(shoppingCart);
        if (orderOptional.isEmpty()) {
            throw new FailureException("We couldn't create an order with from your shopping cart at this time");
        }

        GenericResponse<Order> response = buildSuccessfulResponse(request.getHeader(), "Successfully created an order with your shopping cart", orderOptional.get());
        log.info("Response={}", response);
        return response;
    }

}
