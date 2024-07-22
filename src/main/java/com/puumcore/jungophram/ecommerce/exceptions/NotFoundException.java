package com.puumcore.jungophram.ecommerce.exceptions;

import com.puumcore.jungophram.ecommerce.custom.Assistant;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ms-customer-management
 * @version 1.x
 * @since 6/8/2024 10:05 PM
 */

@Slf4j
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message.contains(Assistant.ERROR_SUFFIX) ? message : message.concat(Assistant.ERROR_SUFFIX));
        log.error(super.getMessage());
    }

    public NotFoundException(String message, Object... args) {
        super(String.format(message.contains(Assistant.ERROR_SUFFIX) ? message : message.concat(Assistant.ERROR_SUFFIX), args));
        log.error(super.getMessage());
    }

}
