package com.puumcore.jungophram.ecommerce.services;

import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.exceptions.FailureException;
import com.puumcore.jungophram.ecommerce.exceptions.NotFoundException;
import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.AccountOps;
import com.puumcore.jungophram.ecommerce.repositories.LoginOps;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 9:31 PM
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService extends Assistant {

    private final AccountOps accountOps;
    private final PasswordEncoder passwordEncoder;
    private final LoginOps loginOps;

    public final GenericResponse<Paged<Account>> filter(final Pageable pageable, final GenericRequest<Form.Search> request) {
        log.info("Request={}", request);

        Form.Search body = request.getBody();
        Optional.ofNullable(body.param())
                .ifPresent(s -> {
                    if (!s.isBlank() && containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Invalid search param");
                    }
                });

        Optional<Paged<Account>> optionalPaged = Optional.ofNullable(body.param()).orElse("").isBlank() ? accountOps.getUsers(pageable) : accountOps.getUsers(body.param(), pageable);
        if (optionalPaged.isEmpty() || optionalPaged.get().getTotalPages() == 0) {
            throw new NotFoundException("No user accounts found");
        }

        GenericResponse<Paged<Account>> response = buildSuccessfulResponse(request.getHeader(), "Here are the requested user accounts", optionalPaged.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Void> changePassword(final String jwt, final GenericRequest<Form.PasswordToUpdate> request) {
        log.info("Request={}", request);

        Form.PasswordToUpdate body = request.getBody();
        accountOps.validatePassword(body.password());

        final Account user = accountOps.getUserFromToken(jwt);
        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            throw new BadRequestException("Consider changing the provided password to something different from your current password");
        }
        final String encodedPassword = passwordEncoder.encode(body.password());
        if (accountOps.updateUser(user.getUser_id(), encodedPassword).isEmpty()) {
            throw new FailureException("Couldn't update your password");
        }

        loginOps.logout(user.getUser_id());

        GenericResponse<Void> response = buildSuccessfulResponse(request.getHeader(), "Your password has been successfully changed. You are required to login again.");
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Account> update(final GenericRequest<Form.UserToUpdate> request) {
        log.info("Request={}", request);

        Form.UserToUpdate body = request.getBody();

        Optional<Account> optionalAccount = accountOps.getUser(body.id());
        if (optionalAccount.isEmpty()) {
            throw new NotFoundException("No such user found");
        }

        Optional.ofNullable(body.name())
                .ifPresent(s -> {
                    if (s.isBlank() || containsSpecialCharacter(s.replace(" ", ""))) {
                        throw new BadRequestException("Invalid name for the user.");
                    }
                });
        Optional.ofNullable(body.email())
                .ifPresent(s -> {
                    if (s.isBlank() || !correctEmailFormat(s)) {
                        throw new BadRequestException("Invalid email for the user.");
                    }
                    if (s.equals(optionalAccount.get().getEmail())) {
                        throw new BadRequestException("The desired email your current email. Update/Remove the it to continue");
                    }
                    accountOps.getUser(s)
                            .ifPresent(account -> {
                                throw new BadRequestException("Duplicate user found. Please provide a unique email.");
                            });
                });

        Optional<Account> updatedUser = accountOps.updateUser(body.id(), body.name(), body.email());
        if (updatedUser.isEmpty()) {
            throw new FailureException("The user information couldn't be updated");
        }

        GenericResponse<Account> response = buildSuccessfulResponse(request.getHeader(), "User account info successfully updated", optionalAccount.get());
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Account> byId(final Long id, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Optional<Account> optionalAccount = accountOps.getUser(id);
        if (optionalAccount.isEmpty()) {
            throw new NotFoundException("No such user found");
        }

        GenericResponse<Account> response = buildSuccessfulResponse(request.getHeader(), "User found", optionalAccount.get());
        log.info("Response={}", response);
        return response;
    }

}
