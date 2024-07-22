package com.puumcore.jungophram.ecommerce.controller;

import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.models.objects.SecuredReply;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import com.puumcore.jungophram.ecommerce.services.IamService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 1:48 PM
 */

@RestController
@RequestMapping(path = "iam", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class IamCtrl {

    private final IamService service;

    @Operation(
            summary = "Sign-up",
            description = "Creates accounts for new users",
            tags = "iam"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("sign-up")
    GenericResponse<Account> signup(@RequestBody @NonNull final GenericRequest<Form.Signup> request) {
        return service.signup(request);
    }

    @Operation(
            summary = "Log in",
            description = "Authenticates a user to allow to use the system",
            tags = "iam"
    )
    @PostMapping("login")
    GenericResponse<SecuredReply> login(@RequestBody @NonNull final GenericRequest<Form.Auth> request) {
        return service.login(request);
    }

    @Operation(
            summary = "Log out",
            description = "Ends a user login session and invalidates access token even if that have not expired",
            tags = "iam"
    )
    @PostMapping("logout")
    GenericResponse<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.logout(jwt, request);
    }

    @Operation(
            summary = "Reset password",
            description = "Enables unauthenticated users to reset their passwords",
            tags = "iam"
    )
    @PostMapping("forgot-password")
    GenericResponse<Void> forgotPassword(@RequestBody @NonNull final GenericRequest<Form.Auth> request) {
        return service.changePassword(request);
    }

}
