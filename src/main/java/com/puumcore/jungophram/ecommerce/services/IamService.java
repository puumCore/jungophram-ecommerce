package com.puumcore.jungophram.ecommerce.services;

import com.puumcore.jungophram.ecommerce.configs.CustomSecurityConfig;
import com.puumcore.jungophram.ecommerce.custom.Assistant;
import com.puumcore.jungophram.ecommerce.exceptions.BadRequestException;
import com.puumcore.jungophram.ecommerce.exceptions.FailureException;
import com.puumcore.jungophram.ecommerce.models.constants.TokenType;
import com.puumcore.jungophram.ecommerce.models.objects.*;
import com.puumcore.jungophram.ecommerce.repositories.AccountOps;
import com.puumcore.jungophram.ecommerce.repositories.LoginOps;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import com.puumcore.jungophram.ecommerce.repositories.entities.LoginSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 2:18 PM
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class IamService extends Assistant {

    private final AccountOps accountOps;
    private final PasswordEncoder passwordEncoder;
    private final CustomSecurityConfig customSecurityConfig;
    private final LoginOps loginOps;

    public final GenericResponse<Void> changePassword(final GenericRequest<Form.Auth> request) {
        log.info("Request={}", request);

        Form.Auth body = request.getBody();
        if (body.username().isBlank() || !correctEmailFormat(body.username())) {
            throw new BadRequestException("Invalid username provided");
        }
        accountOps.validatePassword(body.password());

        final String encodedPassword = passwordEncoder.encode(body.password());
        accountOps.getUser(body.username())
                .ifPresent(account -> {
                    if (passwordEncoder.matches(body.password(), account.getPassword())) {
                        throw new BadRequestException("Consider changing the provided password to something different from your current password");
                    }
                    if (accountOps.updateUser(account.getUser_id(), encodedPassword).isEmpty()) {
                        log.error("Failed to update user '{}' password", account.getUser_id());
                    }
                });

        GenericResponse<Void> response = buildSuccessfulResponse(request.getHeader(), "Processed successfully, if your account exists your password will be updated");
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Void> logout(final String jwt, final GenericRequest<Void> request) {
        log.info("Request={}", request);

        Account userFromToken = accountOps.getUserFromToken(jwt);
        loginOps.logout(userFromToken.getUser_id());

        GenericResponse<Void> response = buildSuccessfulResponse(request.getHeader(), "Successfully logged out");
        log.info("Response={}", response);
        return response;

    }

    public final GenericResponse<SecuredReply> login(final GenericRequest<Form.Auth> request) {
        log.info("Request={}", request);

        Form.Auth body = request.getBody();
        if (body.username().isBlank() || !correctEmailFormat(body.username())) {
            throw new BadRequestException("Invalid username");
        }

        Optional<Account> accountOptional = accountOps.getUser(body.username());
        if (accountOptional.isEmpty()) {
            throw new BadRequestException("Invalid credentials provided");
        }
        final Account account = accountOptional.get();
        loginOps.logout(account.getUser_id());

        if (!passwordEncoder.matches(body.password(), account.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        accountOps.loadUserByUsername(body.username());

        Optional<LoginSession> optionalLoginSession = loginOps.login(request.getHeader().channel(), new LoginSession.User(account.getUser_id(), account.getEmail()));
        if (optionalLoginSession.isEmpty()) {
            throw new FailureException("We experienced an issue with attempting to log you in");
        }

        String accessToken = generateToken(
                request.getHeader().channel(),
                optionalLoginSession.get().getId(),
                customSecurityConfig.getAccessTokenExpiry(),
                customSecurityConfig.getSecret()
        );
        SecuredReply securedReply = new SecuredReply(
                account,
                customSecurityConfig.getTokenPrefix(),
                new SecurityToken(
                        TokenType.ACCESS,
                        accessToken,
                        customSecurityConfig.getAccessTokenExpiry()
                )
        );

        GenericResponse<SecuredReply> response = buildSuccessfulResponse(request.getHeader(), "Login was successful", securedReply);
        log.info("Response={}", response);
        return response;
    }

    public final GenericResponse<Account> signup(final GenericRequest<Form.Signup> request) {
        log.info("Request={}", request);

        Form.Signup body = request.getBody();
        if (body.getName().isBlank() || containsSpecialCharacter(body.getName().replace(" ", ""))) {
            throw new BadRequestException("Invalid name for the user.");
        }
        if (body.getEmail().isBlank() || !correctEmailFormat(body.getEmail())) {
            throw new BadRequestException("Invalid email for the user.");
        }
        if (body.getPassword().isBlank()) {
            throw new BadRequestException("Invalid password for the user.");
        }
        accountOps.validatePassword(body.getPassword());

        accountOps.getUser(body.getEmail())
                .ifPresent(account -> {
                    throw new BadRequestException("Duplicate user found. Please provide a unique email.");
                });
        final String encodedPassword = passwordEncoder.encode(body.getPassword());
        Optional<Account> accountOptional = accountOps.createUser(body.getName(), body.getEmail(), encodedPassword, body.getRole());
        if (accountOptional.isEmpty()) {
            throw new FailureException("The user account could not be created");
        }

        GenericResponse<Account> response = buildSuccessfulResponse(request.getHeader(), "Thank you '%s' for creating an account with us".formatted(accountOptional.get().getName()), accountOptional.get());
        log.info("Response={}", response);
        return response;
    }

}
