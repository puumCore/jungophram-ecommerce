package com.puumcore.jungophram.ecommerce.controller;

import com.puumcore.jungophram.ecommerce.models.objects.Form;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import com.puumcore.jungophram.ecommerce.models.objects.Paged;
import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import com.puumcore.jungophram.ecommerce.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 9:28 PM
 */

@RestController
@RequestMapping(path = "users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class UserCtrl {

    private final UserService service;

    @Operation(
            summary = "Get user",
            description = "Fetch user by Id",
            tags = "user-mgnt"
    )
    @PostMapping(path = "byId", params = "id")
    GenericResponse<Account> byId(@RequestParam(value = "id") @NonNull Long userId, @RequestBody @NonNull final GenericRequest<Void> request) {
        return service.byId(userId, request);
    }

    @Operation(
            summary = "Update user",
            description = "Updates user account with the desired name & email",
            tags = "user-mgnt"
    )
    @PatchMapping("update")
    GenericResponse<Account> update(@RequestBody @NonNull final GenericRequest<Form.UserToUpdate> request) {
        return service.update(request);
    }

    @Operation(
            summary = "Update user",
            description = "Updates user account with the desired password",
            tags = "user-mgnt"
    )
    @PutMapping("reset-password")
    GenericResponse<Void> resetPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) @NonNull final String jwt, @RequestBody @NonNull final GenericRequest<Form.PasswordToUpdate> request) {
        return service.changePassword(jwt, request);
    }

    @Operation(
            summary = "Filter users",
            description = "Enables the admin to filter through existing users even with a desired parameter",
            tags = "user-mgnt"
    )
    @PostMapping("filter")
    GenericResponse<Paged<Account>> filter(
            @ParameterObject
            @PageableDefault(size = 20)
            @SortDefault(sort = "user_id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestBody @NonNull final GenericRequest<Form.Search> request
    ) {
        return service.filter(pageable, request);
    }

}
