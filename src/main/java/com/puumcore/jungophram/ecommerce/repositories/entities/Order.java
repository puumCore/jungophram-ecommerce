package com.puumcore.jungophram.ecommerce.repositories.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.puumcore.jungophram.ecommerce.models.constants.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 7:22 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 970101L;

    public static final String collection = "orders";

    @Id
    private UUID id;
    private ShoppingCart cart;
    private Double totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime orderDate;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;

        return getId().equals(order.getId()) && getCart().equals(order.getCart()) && getTotalAmount().equals(order.getTotalAmount()) && getOrderStatus() == order.getOrderStatus() && getOrderDate().equals(order.getOrderDate());
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getCart().hashCode();
        result = 31 * result + getTotalAmount().hashCode();
        result = 31 * result + getOrderStatus().hashCode();
        result = 31 * result + getOrderDate().hashCode();
        return result;
    }
}
