package com.puumcore.jungophram.ecommerce.models.objects;

import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 6:06 PM
 */

@Setter
@Getter
@ToString
public class SecuredReply implements Serializable {

    @Serial
    private static final long serialVersionUID = 911314L;

    private Account account;
    private String authorizationPrefix;
    private SecurityToken[] securityTokens;

    public SecuredReply(Account account, String authorizationPrefix, SecurityToken... securityTokens) {
        this.account = account;
        this.authorizationPrefix = authorizationPrefix;
        this.securityTokens = securityTokens;
    }

}
