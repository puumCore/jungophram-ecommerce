package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.models.constants.Channel;
import com.puumcore.jungophram.ecommerce.repositories.entities.LoginSession;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 8:28 PM
 */

public interface LoginOps {

    Optional<LoginSession> login(Channel channel, LoginSession.User user);

    Optional<LoginSession> getLogin(Long id);

    Optional<LoginSession> getLogin(UUID sessionId);

    @CacheEvict(value = {"user"}, allEntries = true)
    void logout(Long id);

}
