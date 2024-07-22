package com.puumcore.jungophram.ecommerce.repositories.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.puumcore.jungophram.ecommerce.models.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;
import java.util.StringJoiner;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 1:53 PM
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 973329L;

    public static final String SEQUENCE_NAME = "user_id_sequence";
    public static final String collection = "accounts";

    @Id
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long user_id;
    private String name;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private transient String password;
    private Role role;

    public Account(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String toString() {
        return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
                .add("user_id=" + user_id)
                .add("name='" + name + "'")
                .add("email='" + email + "'")
                .add("password='<hidden>'")
                .add("role='" + role + "'")
                .toString();
    }
}
