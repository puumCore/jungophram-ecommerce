package com.puumcore.jungophram.ecommerce.models.objects;

import lombok.*;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ms-core
 * @version 1.x
 * @since 7/12/2024 4:37 PM
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GenericResponse<B> {

    private Header header;
    private B body;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Header {

        private String timestamp;
        private String requestId;
        private String message;

    }
}
