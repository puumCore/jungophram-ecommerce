package com.puumcore.jungophram.ecommerce.models.objects;

import com.puumcore.jungophram.ecommerce.models.constants.TokenType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.StringJoiner;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 6:02 PM
 */

@Getter
@Setter
@AllArgsConstructor
public class SecurityToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 753577L;

    private TokenType type;
    private String token;
    private long expiresIn;

    @Override
    public String toString() {
        return new StringJoiner(", ", SecurityToken.class.getSimpleName() + "[", "]")
                .add("type=" + type)
                .add("token='<hidden>'")
                .add("expiresIn=" + expiresIn)
                .toString();
    }
}
