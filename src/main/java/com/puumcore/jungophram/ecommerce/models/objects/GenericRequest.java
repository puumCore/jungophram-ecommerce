package com.puumcore.jungophram.ecommerce.models.objects;

import com.puumcore.jungophram.ecommerce.models.constants.Channel;
import lombok.*;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ms-core
 * @version 1.x
 * @since 7/12/2024 4:38 PM
 */

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GenericRequest<B> {

    private Header header;
    private B body;

    public record Header(@NonNull String requestId, @NonNull Channel channel) {
    }

}
