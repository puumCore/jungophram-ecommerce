package com.puumcore.jungophram.ecommerce.repositories;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.puumcore.jungophram.ecommerce.configs.CustomSecurityConfig;
import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.AccessDeniedException;
import com.puumcore.jungophram.ecommerce.models.constants.Channel;
import com.puumcore.jungophram.ecommerce.models.constants.OrderStatus;
import com.puumcore.jungophram.ecommerce.models.constants.Role;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.*;
import com.puumcore.jungophram.ecommerce.services.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 2:11 PM
 */


@Slf4j
@RequiredArgsConstructor
@Repository
public class Brain implements AccountOps, LoginOps,
        StockOps, ShoppingOps,
        OrdersOps {

    private final MongoTemplate mongoTemplate;
    private final UserRepo userRepo;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final LoginSessionRepo loginSessionRepo;
    private final CustomSecurityConfig customSecurityConfig;
    private final ProductRepo productRepo;
    private final ShoppingCartRepo shoppingCartRepo;
    private final OrderRepo orderRepo;

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    @Override
    public Optional<Account> createUser(String name, String email, String password, Role role) {
        try {
            Account account = new Account(name, email, password, role);
            account.setUser_id(sequenceGeneratorService.generateSequence(Account.SEQUENCE_NAME));
            return Optional.of(userRepo.save(account));
        } catch (Exception e) {
            log.error("Failed to create user account", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> getUser(String username) {
        try {
            var query = new Query();
            query.addCriteria(
                    Criteria
                            .where("email").is(username)
            );
            return Optional.ofNullable(mongoTemplate.findOne(query, Account.class));
        } catch (Exception e) {
            log.error("Failed to get user", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> getUser(Long id) {
        try {
            return userRepo.findById(id);
        } catch (Exception e) {
            log.error("Failed to get user", e);
        }
        return Optional.empty();
    }

    @Override
    public Account getUserFromToken(String jwt) throws JWTVerificationException {
        String jwtToken = jwt.substring(customSecurityConfig.getTokenPrefix().length()).trim();
        Algorithm algorithm = Algorithm.HMAC256(customSecurityConfig.getSecret());
        JWTVerifier verifier = JWT.require(algorithm)
                .withSubject("E-commerce")
                .withIssuer("Jungopharm")
                .build();
        DecodedJWT decodedJWT = verifier.verify(jwtToken);
        final String session = decodedJWT.getAudience().get(0);
        if (!Assistant.correctUUIDFormat(session)) {
            throw new AccessDeniedException("Invalid login ID");
        }
        Optional<LoginSession> optionalLoginSession = getLogin(UUID.fromString(session));
        if (optionalLoginSession.isEmpty()) {
            throw new AccessDeniedException("Please login to continue");
        }

        Optional<Account> accountOptional = getUser(optionalLoginSession.get().getUser().id());
        if (accountOptional.isEmpty()) {
            throw new AccessDeniedException("Couldn't identify requesting user");
        }

        return accountOptional.get();
    }

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    @Override
    public Optional<Account> updateUser(Long id, String name, String email) {
        Optional<Account> accountOptional = userRepo.findById(id);
        if (accountOptional.isPresent()) {
            try {
                Account account = accountOptional.get();
                Optional.ofNullable(name)
                        .ifPresent(account::setName);
                Optional.ofNullable(email)
                        .ifPresent(account::setEmail);

                Optional<Account> previousAccountOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(account.getUser_id()), account));
                if (previousAccountOptional.isPresent()) {
                    return userRepo.findById(previousAccountOptional.get().getUser_id());
                }
            } catch (Exception e) {
                log.error("Failed to user info", e);
            }
        }
        return Optional.empty();
    }

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    @Override
    public Optional<Account> updateUser(Long id, String password) {
        Optional<Account> accountOptional = userRepo.findById(id);
        if (accountOptional.isPresent()) {
            try {
                Account account = accountOptional.get();
                Optional.ofNullable(password)
                        .ifPresent(account::setPassword);

                Optional<Account> previousAccountOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(account.getUser_id()), account));
                if (previousAccountOptional.isPresent()) {
                    return userRepo.findById(previousAccountOptional.get().getUser_id());
                }
            } catch (Exception e) {
                log.error("Failed to user info", e);
            }
        }
        return Optional.empty();
    }

    @Cacheable("users")
    @Override
    public Optional<Paged<Account>> getUsers(Pageable pageable) {
        try {
            Page<Account> page = userRepo.findAll(pageable);
            Paged<Account> paged = new Paged<>(page.getTotalPages());
            paged.getData().addAll(page.get().toList());
            return Optional.of(paged);
        } catch (Exception e) {
            log.error("Failed to get all user accounts", e);
        }
        return Optional.empty();
    }

    @Cacheable("users")
    @Override
    public Optional<Paged<Account>> getUsers(String param, Pageable pageable) {
        try {
            var criteria = new Criteria();
            var containsPattern = Pattern.compile("%s(?i)".formatted(param));
            criteria.orOperator(
                    Criteria.where("name").regex(containsPattern),
                    Criteria.where("email").regex(containsPattern),
                    Criteria.where("role").regex(containsPattern)
            );
            final Query query = new Query(criteria).with(pageable);
            final long total = mongoTemplate.count(query, Account.class);
            if (total > 0) {
                final List<Account> accountList = mongoTemplate.find(query, Account.class);
                final int totalPages = Assistant.roundOffToNearestWholeNumber(total, pageable.getPageSize());
                Paged<Account> paged = new Paged<>(totalPages);
                paged.getData().addAll(accountList);
                return Optional.of(paged);
            }
        } catch (Exception e) {
            log.error("Failed to get user accounts based on param", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LoginSession> login(Channel channel, LoginSession.User user) {
        try {
            LoginSession loginSession = new LoginSession();
            loginSession.setId(UUID.randomUUID());
            loginSession.setChannel(channel);
            loginSession.setUser(user);
            loginSession.setLogin_at(LocalDateTime.now(Assistant.clock));

            return Optional.of(loginSessionRepo.save(loginSession));
        } catch (Exception e) {
            log.error("Failed to log login attempt", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<LoginSession> getLogin(Long id) {
        try {
            return Optional.ofNullable(mongoTemplate.findOne(getActiveLoginQuery(id), LoginSession.class));
        } catch (Exception e) {
            log.error("Failed to get login session", e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<LoginSession> getLogin(UUID sessionId) {
        try {
            var query = new Query();
            query.addCriteria(Criteria.where("_id").is(sessionId));
            var criteria = new Criteria();
            criteria.andOperator(
                    Criteria.where("logout_at").isNull()
            );
            query.addCriteria(criteria);
            return Optional.ofNullable(mongoTemplate.findOne(query, LoginSession.class));
        } catch (Exception e) {
            log.error("Failed to get login session", e);
        }

        return Optional.empty();
    }

    @CacheEvict({"user"})
    @Override
    public void logout(Long id) {
        try {
            getLogin(id)
                    .ifPresent(loginSession -> {
                        loginSession.setLogout_at(LocalDateTime.now(Assistant.clock));
                        mongoTemplate.findAndReplace(getActiveLoginQuery(id), loginSession);
                    });
        } catch (Exception e) {
            log.error("Failed to get logout the user", e);
        }
    }

    private Query getActiveLoginQuery(final Long id) {
        var query = new Query();
        query.addCriteria(Criteria.where("user._id").is(id));
        var criteria = new Criteria();
        criteria.andOperator(
                Criteria.where("logout_at").isNull()
        );
        query.addCriteria(criteria);
        return query;
    }

    /**
     * Locates the user based on the username. In the actual implementation, the search
     * may possibly be case sensitive, or case insensitive depending on how the
     * implementation instance is configured. In this case, the <code>UserDetails</code>
     * object that comes back may have a username that is of a different case than what
     * was actually requested..
     *
     * @param username the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException if the user could not be found or the user has no
     *                                   GrantedAuthority
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Account> accountOptional = getUser(username);
        if (accountOptional.isEmpty()) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        Account account = accountOptional.get();
        if (account.getRole() == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }
        final List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>(
                Collections.singletonList(new SimpleGrantedAuthority(account.getRole().name()))
        ) {
        };
        return new User(account.getEmail(), account.getPassword(), grantedAuthorities);
    }

    @Cacheable("product")
    @Override
    public Optional<Product> getProduct(String name, String description, String category) {
        var query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        var criteria = new Criteria();
        criteria.andOperator(
                Criteria.where("description").is(description),
                Criteria.where("category").is(category)
        );
        query.addCriteria(criteria);

        try {
            return Optional.ofNullable(mongoTemplate.findOne(query, Product.class));
        } catch (Exception e) {
            log.error("Failed to get stock item", e);
        }

        return Optional.empty();
    }

    @CacheEvict(value = {"product"}, allEntries = true)
    @Override
    public Optional<Product> saveProduct(String name, String description, double price, int qty, String category, String url) {
        try {
            Product product = new Product(name, description, price, qty, category);
            product.setProduct_id(sequenceGeneratorService.generateSequence(Product.SEQUENCE_NAME));
            if (!Optional.ofNullable(url).orElse("").isBlank()) {
                product.setImageUrl(url);
            }
            return Optional.of(productRepo.save(product));
        } catch (Exception e) {
            log.error("Failed to create stock item", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> updateProduct(Long id, String name, String description, String category, String url) {
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()) {
            try {
                Product product = optionalProduct.get();
                Optional.ofNullable(name)
                        .ifPresent(product::setName);
                Optional.ofNullable(description)
                        .ifPresent(product::setDescription);
                Optional.ofNullable(category)
                        .ifPresent(product::setCategory);
                Optional.ofNullable(url)
                        .ifPresent(product::setImageUrl);

                Optional<Product> previousProductOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(product.getProduct_id()), product));
                if (previousProductOptional.isPresent()) {
                    return productRepo.findById(previousProductOptional.get().getProduct_id());
                }
            } catch (Exception e) {
                log.error("Failed to update stock item", e);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> updateProduct(Long id, Integer qty) {
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()) {
            try {
                Product product = optionalProduct.get();
                product.setStockQuantity(qty);

                Optional<Product> previousProductOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(product.getProduct_id()), product));
                if (previousProductOptional.isPresent()) {
                    return productRepo.findById(previousProductOptional.get().getProduct_id());
                }
            } catch (Exception e) {
                log.error("Failed to update stock item quantity", e);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Product> updateProduct(Long id, Double price) {
        Optional<Product> optionalProduct = productRepo.findById(id);
        if (optionalProduct.isPresent()) {
            try {
                Product product = optionalProduct.get();
                product.setPrice(price);

                Optional<Product> previousProductOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(product.getProduct_id()), product));
                if (previousProductOptional.isPresent()) {
                    return productRepo.findById(previousProductOptional.get().getProduct_id());
                }
            } catch (Exception e) {
                log.error("Failed to update stock item price", e);
            }
        }
        return Optional.empty();
    }

    @CachePut("products")
    @Override
    public Optional<Paged<Product>> getProducts(Pageable pageable) {
        try {
            Page<Product> page = productRepo.findAll(pageable);
            Paged<Product> paged = new Paged<>(page.getTotalPages());
            paged.getData().addAll(page.get().toList());
            return Optional.of(paged);
        } catch (Exception e) {
            log.error("Failed to get all stock items", e);
        }
        return Optional.empty();
    }

    @CachePut("products")
    @Override
    public Optional<Paged<Product>> getProducts(String param, Pageable pageable) {
        try {
            var criteria = new Criteria();
            var containsPattern = Pattern.compile("%s(?i)".formatted(param));
            criteria.orOperator(
                    Criteria.where("name").regex(containsPattern),
                    Criteria.where("description").regex(containsPattern),
                    Criteria.where("category").regex(containsPattern)
            );
            final Query query = new Query(criteria).with(pageable);
            final long total = mongoTemplate.count(query, Product.class);
            if (total > 0) {
                final List<Product> productList = mongoTemplate.find(query, Product.class);
                final int totalPages = Assistant.roundOffToNearestWholeNumber(total, pageable.getPageSize());
                Paged<Product> paged = new Paged<>(totalPages);
                paged.getData().addAll(productList);
                return Optional.of(paged);
            }
        } catch (Exception e) {
            log.error("Failed to get stock items based on param", e);
        }
        return Optional.empty();
    }

    private Query getByIdQuery(Object id) {
        var query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        return query;
    }

    @Override
    public Integer getAvailableQuantity(Long productId) {
        try {
            Optional<Product> optionalProduct = productRepo.findById(productId);
            if (optionalProduct.isPresent()) {
                final int totalQuantitiesInShoppingCart = quantityFromShoppingCart(productId);
                int totalQuantitiesFromPendingOrders = quantityFromPendingOrders(productId);
                final int availableBalance = optionalProduct.get().getStockQuantity() - (totalQuantitiesInShoppingCart + totalQuantitiesFromPendingOrders);
                return Math.max(availableBalance, 0);
            }
        } catch (Exception e) {
            log.error("Failed to get stock available balance", e);
        }
        return 0;
    }

    private int quantityFromPendingOrders(Long productId) {
        // Unwind the items array
        UnwindOperation unwind = Aggregation.unwind("cart.items");

        // Match items with the specified product id
        MatchOperation match = Aggregation.match(
                Criteria
                        .where("cart.items._id").is(productId)
                        .and("orderStatus").is(OrderStatus.PENDING)
        );

        // Project only the desired product id field
        AggregationOperation project = Aggregation.project().and("cart.items.quantity").as("quantity");

        // Create the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(unwind, match, project);

                /*
                  Execute the aggregation to return a document with the following model:
                  Expected to read Document{{_id=76c7a0cb-0978-468b-a90d-d427d8756fea, quantity=1}} into type class java.lang.Integer but didn't find a PersistentEntity for the latter
                  We only need the 'quantity' value in this aggregation
                */
        AggregationResults<Document> documentAggregationResults = mongoTemplate.aggregate(aggregation, Order.collection, Document.class);
        List<Integer> orderQuantities = documentAggregationResults.getMappedResults().stream()
                .map(doc -> doc.getInteger("quantity"))
                .toList();

        return orderQuantities.stream()
                .mapToInt(value -> value)
                .sum();
    }

    private int quantityFromShoppingCart(Long productId) {
        // Unwind the items array
        UnwindOperation unwind = Aggregation.unwind("items");

        // Match items with the specified product id
        MatchOperation match = Aggregation.match(Criteria.where("items._id").is(productId));

        // Project only the desired product id field
        AggregationOperation project = Aggregation.project().and("items.quantity").as("quantity");

        // Create the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(unwind, match, project);

                /*
                  Execute the aggregation to return a document with the following model:
                  Expected to read Document{{_id=76c7a0cb-0978-468b-a90d-d427d8756fea, quantity=1}} into type class java.lang.Integer but didn't find a PersistentEntity for the latter
                  We only need the 'quantity' value in this aggregation
                */
        AggregationResults<Document> documentAggregationResults = mongoTemplate.aggregate(aggregation, ShoppingCart.collection, Document.class);
        List<Integer> shoppedQuantities = documentAggregationResults.getMappedResults().stream()
                .map(doc -> doc.getInteger("quantity"))
                .toList();

        return shoppedQuantities.stream()
                .mapToInt(value -> value)
                .sum();
    }

    @Override
    public Optional<ShoppingCart> getCustomerShoppingCart(Long userId) {
        try {
            return Optional.ofNullable(mongoTemplate.findOne(getCustomerByIdQuery(userId), ShoppingCart.class));
        } catch (Exception e) {
            log.error("Failed to get customer shopping cart", e);
        }

        return Optional.empty();
    }

    private Query getCustomerByIdQuery(Long id) {
        var query = new Query();
        query.addCriteria(Criteria.where("customer._id").is(id));
        return query;
    }

    @Override
    public Optional<ShoppingCart> createCart(Long userId, String customerEmail, List<ShoppingCart.Item> items) {
        try {
            final ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setId(UUID.randomUUID());
            shoppingCart.setCustomer(new ShoppingCart.Customer(userId, customerEmail));
            shoppingCart.getItems().addAll(items);

            return Optional.of(shoppingCartRepo.save(shoppingCart));
        } catch (Exception e) {
            log.error("Failed to create a shopping cart", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ShoppingCart> updateShoppingCartItems(Long userId, List<ShoppingCart.Item> items, boolean adding) {
        try {
            Optional<ShoppingCart> optionalShoppingCart = getCustomerShoppingCart(userId);
            if (optionalShoppingCart.isPresent()) {
                ShoppingCart shoppingCart = optionalShoppingCart.get();
                if (!adding) {
                    shoppingCart.getItems().clear();
                }
                shoppingCart.getItems().addAll(items);

                Optional<ShoppingCart> cartOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getCustomerByIdQuery(userId), shoppingCart));
                if (cartOptional.isPresent()) {
                    return getCustomerShoppingCart(userId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update shopping cart items", e);
        }
        return Optional.empty();
    }

    @CacheEvict(value = {"orders", "customer_orders"}, allEntries = true)
    @Override
    public Optional<Order> create(ShoppingCart shoppingCart) {
        try {
            Order order = new Order();
            order.setId(UUID.randomUUID());
            order.setCart(shoppingCart);
            final double bill = shoppingCart.getItems().stream()
                    .mapToDouble(ShoppingCart.Item::totalCost)
                    .sum();

            order.setTotalAmount(Assistant.roundOffToTheNearestDecimal("#.##", bill));
            order.setOrderStatus(OrderStatus.PENDING);
            order.setOrderDate(LocalDateTime.now(Assistant.clock));
            return Optional.of(orderRepo.save(order));
        } catch (Exception e) {
            log.error("Failed to create order", e);
        }

        return Optional.empty();
    }

    @CacheEvict(value = {"orders"}, allEntries = true)
    @Override
    public Optional<Order> updateStatus(UUID orderId, OrderStatus status) {
        try {
            Optional<Order> optionalOrder = orderRepo.findById(orderId);
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setOrderStatus(status);

                Optional<Order> orderOptional = Optional.ofNullable(mongoTemplate.findAndReplace(getByIdQuery(orderId), order));
                if (orderOptional.isPresent()) {
                    return orderRepo.findById(orderId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to update order status", e);
        }

        return Optional.empty();
    }

    @Cacheable("orders")
    @Override
    public Optional<Paged<Order>> getOrders(Pageable pageable) {
        try {
            Page<Order> page = orderRepo.findAll(pageable);
            Paged<Order> paged = new Paged<>(page.getTotalPages());
            paged.getData().addAll(page.get().toList());
            return Optional.of(paged);
        } catch (Exception e) {
            log.error("Failed to get all orders", e);
        }
        return Optional.empty();
    }

    @Cacheable("orders")
    @Override
    public Optional<Paged<Order>> filterOrders(String param, Pageable pageable) {
        try {
            var criteria = new Criteria();

            var containsPattern = Pattern.compile("%s(?i)".formatted(param));
            criteria.orOperator(
                    Criteria.where("cart.customer.email").regex(containsPattern),
                    Criteria.where("cart.items.name").regex(containsPattern),
                    Criteria.where("orderStatus").regex(containsPattern),
                    Criteria.where("orderDate").regex(containsPattern)
            );
            final Query query = new Query(criteria).with(pageable);

            final long total = mongoTemplate.count(query, Order.class);
            if (total > 0) {
                final List<Order> orderList = mongoTemplate.find(query, Order.class);
                final int totalPages = Assistant.roundOffToNearestWholeNumber(total, pageable.getPageSize());
                Paged<Order> paged = new Paged<>(totalPages);
                paged.getData().addAll(orderList);
                return Optional.of(paged);
            }
        } catch (Exception e) {
            log.error("Failed to get orders based on param", e);
        }
        return Optional.empty();
    }

    @Cacheable("customer_orders")
    @Override
    public Optional<Paged<Order>> getOrders(Long userId, Pageable pageable) {
        try {
            final Query query = new Query().with(pageable);
            query.addCriteria(Criteria.where("cart.customer._id").is(userId));

            final long total = mongoTemplate.count(query, Order.class);
            if (total > 0) {
                final List<Order> orderList = mongoTemplate.find(query, Order.class);
                final int totalPages = Assistant.roundOffToNearestWholeNumber(total, pageable.getPageSize());
                Paged<Order> paged = new Paged<>(totalPages);
                paged.getData().addAll(orderList);
                return Optional.of(paged);
            }
        } catch (Exception e) {
            log.error("Failed to get customer orders based on param", e);
        }
        return Optional.empty();
    }

    @Cacheable("customer_orders")
    @Override
    public Optional<Paged<Order>> filterOrders(Long userId, String param, Pageable pageable) {
        try {
            var criteria = new Criteria();

            var containsPattern = Pattern.compile("%s(?i)".formatted(param));
            criteria.orOperator(
                    Criteria.where("cart.customer.email").regex(containsPattern),
                    Criteria.where("cart.items.name").regex(containsPattern),
                    Criteria.where("orderStatus").regex(containsPattern),
                    Criteria.where("orderDate").regex(containsPattern)
            );
            final Query query = new Query(criteria).with(pageable);
            query.addCriteria(Criteria.where("cart.customer._id").is(userId));

            final long total = mongoTemplate.count(query, Order.class);
            if (total > 0) {
                final List<Order> orderList = mongoTemplate.find(query, Order.class);
                final int totalPages = Assistant.roundOffToNearestWholeNumber(total, pageable.getPageSize());
                Paged<Order> paged = new Paged<>(totalPages);
                paged.getData().addAll(orderList);
                return Optional.of(paged);
            }
        } catch (Exception e) {
            log.error("Failed to get customer orders based on param", e);
        }
        return Optional.empty();
    }
}
