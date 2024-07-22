package com.puumcore.jungophram.ecommerce.repositories.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/20/2024 3:08 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shopping_cart")
public class ShoppingCart implements Serializable {

    @Serial
    private static final long serialVersionUID = 343843L;

    public static final String collection = "shopping_cart";

    @Id
    private UUID id;
    private Customer customer;
    private List<Item> items = new ArrayList<>();

    public record Item(@NonNull Long id, @NonNull String name, @NonNull Integer quantity,
                       @NonNull Double totalCost) implements Serializable {

        @Serial
        private static final long serialVersionUID = 167711L;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Item item)) return false;

            return id.equals(item.id) && name.equals(item.name) && quantity.equals(item.quantity) && totalCost.equals(item.totalCost);
        }

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + quantity.hashCode();
            result = 31 * result + totalCost.hashCode();
            return result;
        }
    }

    public record Customer(@NonNull Long id, @NonNull String email) implements Serializable {

        @Serial
        private static final long serialVersionUID = 953242L;

    }

}
