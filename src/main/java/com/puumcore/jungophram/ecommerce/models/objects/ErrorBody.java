package com.puumcore.jungophram.ecommerce.models.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ms-customer-management
 * @version 1.x
 * @since 6/28/2024 5:04 PM
 */

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ErrorBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 480194L;

    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}
