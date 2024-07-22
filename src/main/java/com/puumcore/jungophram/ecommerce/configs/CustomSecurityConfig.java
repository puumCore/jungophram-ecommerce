package com.puumcore.jungophram.ecommerce.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 1:05 PM
 */

@Configuration
@ConfigurationProperties(prefix = "custom.security")
@Getter
@Setter
public class CustomSecurityConfig {

    private String secret;
    private Long accessTokenExpiry;
    private String tokenPrefix;

}
