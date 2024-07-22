package com.puumcore.jungophram.ecommerce.models.objects;

import com.puumcore.jungophram.ecommerce.models.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 3:31 PM
 */

public abstract class Form {

    public record OrderById(@NonNull UUID id) implements Serializable {

        @Serial
        private static final long serialVersionUID = 843689L;

    }

    public record ById(@NonNull Long id) implements Serializable {

        @Serial
        private static final long serialVersionUID = 177455L;

    }

    public record CartItem(@NonNull Long productId, @NonNull Integer quantity) implements Serializable {

        @Serial
        private static final long serialVersionUID = 177455L;

    }

    public record PasswordToUpdate(@NonNull String password) implements Serializable {

        @Serial
        private static final long serialVersionUID = 129252L;

    }

    public record UserToUpdate(@NonNull Long id, String name, String email) implements Serializable {

        @Serial
        private static final long serialVersionUID = 123676L;

    }

    public record Search(String param) implements Serializable {

        @Serial
        private static final long serialVersionUID = 592979L;

    }

    public record StockToUpdatePrice(@NonNull Long id, @NonNull Double price) implements Serializable {

        @Serial
        private static final long serialVersionUID = 212105L;

    }

    public record StockToUpdateQty(@NonNull Long id, @NonNull Integer qty) implements Serializable {

        @Serial
        private static final long serialVersionUID = 627665L;

    }

    public record StockToUpdate(@NonNull Long id, String name, String description, Double price,
                                Integer quantity, String category,
                                String image) implements Serializable {

        @Serial
        private static final long serialVersionUID = 821956L;

    }

    public record StockToAdd(@NonNull String name, @NonNull String description, @NonNull Double price,
                             @NonNull Integer quantity, @NonNull String category,
                             String image) implements Serializable {

        @Serial
        private static final long serialVersionUID = 971723L;

    }

    public record Auth(@NonNull String username, @NonNull String password) implements Serializable {

        @Serial
        private static final long serialVersionUID = 474064L;

        @Override
        public String toString() {
            return new StringJoiner(", ", Auth.class.getSimpleName() + "[", "]")
                    .add("username='" + username + "'")
                    .add("password='<hidden>'")
                    .toString();
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class Signup implements Serializable {

        @Serial
        private static final long serialVersionUID = 363296L;

        @NonNull
        private String name;
        @NonNull
        private String email;
        @NonNull
        private Role role;
        @NonNull
        private String password;

        @Override
        public String toString() {
            return new StringJoiner(", ", Signup.class.getSimpleName() + "[", "]")
                    .add("name='" + name + "'")
                    .add("email='" + email + "'")
                    .add("role=" + role)
                    .add("password='<hidden>'")
                    .toString();
        }
    }
}
