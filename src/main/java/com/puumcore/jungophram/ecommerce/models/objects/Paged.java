package com.puumcore.jungophram.ecommerce.models.objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/19/2024 7:40 PM
 */

@Getter
@Setter
@RequiredArgsConstructor
public class Paged<E> implements Serializable {

    @Serial
    private static final long serialVersionUID = 564354L;

    private final Integer totalPages;
    private transient List<E> data = new ArrayList<>();

}
