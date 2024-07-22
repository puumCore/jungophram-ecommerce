package com.puumcore.jungophram.ecommerce.repositories.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 1:32 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "stock")
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = 423843L;

    public static final String SEQUENCE_NAME = "product_id_sequence";
    public static final String collection = "stock";

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long product_id;

    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String category;
    private String imageUrl;

    public Product(String name, String description, Double price, Integer stockQuantity, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;

        return getProduct_id().equals(product.getProduct_id()) && getName().equals(product.getName()) && getDescription().equals(product.getDescription()) && getPrice().equals(product.getPrice()) && getStockQuantity().equals(product.getStockQuantity()) && getCategory().equals(product.getCategory()) && Objects.equals(getImageUrl(), product.getImageUrl());
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public int hashCode() {
        int result = getProduct_id().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getPrice().hashCode();
        result = 31 * result + getStockQuantity().hashCode();
        result = 31 * result + getCategory().hashCode();
        result = 31 * result + Objects.hashCode(getImageUrl());
        return result;
    }
}
