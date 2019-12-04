package eu.arrowhead.legacy.sr.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import eu.arrowhead.legacy.common.security.DefaultSecurityConfig;

@Configuration
@EnableWebSecurity
public class LegacySRSecurityConfig extends DefaultSecurityConfig {
}