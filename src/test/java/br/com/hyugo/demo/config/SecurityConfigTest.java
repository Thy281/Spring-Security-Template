package br.com.hyugo.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void shouldConfigureCorsForAllRoutesIncludingPreflight() {
        SecurityConfig securityConfig = new SecurityConfig(mock(SecurityFilter.class));

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);

        CorsConfiguration configuration = ((UrlBasedCorsConfigurationSource) source)
                .getCorsConfigurations()
                .get("/**");

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOriginPatterns()).contains("*");
        assertThat(configuration.getAllowedMethods()).contains("POST", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).contains("*");
    }
}
