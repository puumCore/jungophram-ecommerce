package com.puumcore.jungophram.ecommerce.repositories.entities;

import com.puumcore.jungophram.ecommerce.models.constants.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
 * @since 7/18/2024 6:57 PM
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "login_sessions")
public class LoginSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 469526L;

    public static final String collection = "login_sessions";

    @Id
    private UUID id;
    private Channel channel;
    private User user;
    private LocalDateTime login_at;
    private LocalDateTime logout_at;


    public record User(@NonNull Long id, @NonNull String username) implements Serializable {

        @Serial
        private static final long serialVersionUID = 803820L;

    }

}
