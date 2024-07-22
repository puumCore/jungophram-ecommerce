package com.puumcore.jungophram.ecommerce.models.objects;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 1:43 PM
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BatchSummary<K> implements Serializable {

    @Serial
    private static final long serialVersionUID = 879709L;

    private Float successRate;
    private final Set<BatchItem<K>> successful = new HashSet<>();
    private final Set<BatchItem<K>> failed = new HashSet<>();

    @Getter
    @Setter
    @AllArgsConstructor
    public static class BatchItem<K> implements Serializable {

        @Serial
        private static final long serialVersionUID = 652920L;

        @NonNull
        private K value;
        @NonNull
        private String message;

    }

}
