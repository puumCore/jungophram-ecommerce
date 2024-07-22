package com.puumcore.jungophram.ecommerce.repositories;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.models.constants.Role;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 12:44 PM
 */
public interface AccountOps extends UserDetailsService {

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    Optional<Account> createUser(String name, String email, String password, Role role);

    Optional<Account> getUser(String username);

    Optional<Account> getUser(Long id);

    Account getUserFromToken(String jwt) throws JWTVerificationException;

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    Optional<Account> updateUser(Long id, String name, String email);

    @CacheEvict(value = {"user", "users"}, allEntries = true)
    Optional<Account> updateUser(Long id, String password);

    @Cacheable("users")
    Optional<Paged<Account>> getUsers(Pageable pageable);

    @Cacheable("users")
    Optional<Paged<Account>> getUsers(String param, Pageable pageable);

    default void validatePassword(final String rawPassword) {
        final var WHITE_SPACE_REGEX = "\\s+";
        final var LENGTH_REGEX = ".{8,}";
        final var ONE_DIGIT_REGEX = ".+\\d.+";
        final var LOWER_CASE_REGEX = ".+[a-z].+";
        final var UPPER_CASE_REGEX = ".*[A-Z].*";
        final var SPECIAL_CHAR_REGEX = "[^A-za-z0-9]";
        String failReason;
        if (Pattern.compile(WHITE_SPACE_REGEX).matcher(rawPassword).find()) {
            failReason = "The password must not include whitespaces";
            throw new BadRequestException(failReason);
        }
        if (!Pattern.compile(LENGTH_REGEX).matcher(rawPassword).matches()) {
            failReason = "The password should be at-least 8 characters long";
            throw new BadRequestException(failReason);
        }
        final String prefix = "The password must include at-least ";
        if (!Pattern.compile(ONE_DIGIT_REGEX).matcher(rawPassword).matches()) {
            failReason = prefix.concat("one digit");
            throw new BadRequestException(failReason);
        }
        if (!Pattern.compile(LOWER_CASE_REGEX).matcher(rawPassword).matches()) {
            failReason = prefix.concat("one lowercase letter");
            throw new BadRequestException(failReason);
        }
        if (!Pattern.compile(UPPER_CASE_REGEX).matcher(rawPassword).matches()) {
            failReason = prefix.concat("one uppercase letter");
            throw new BadRequestException(failReason);
        }
        if (!Pattern.compile(SPECIAL_CHAR_REGEX).matcher(rawPassword).find()) {
            failReason = prefix.concat("one special character");
            throw new BadRequestException(failReason);
        }
    }

}
