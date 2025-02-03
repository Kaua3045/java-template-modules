package com.kaua.template.infrastructure.configurations.authentication;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NonNull final Jwt jwt) {
        return new UserAuthentication(
                jwt,
                extractPrincipal(jwt),
                Collections.emptyList()
        );
    }

    private AuthenticatedUser extractPrincipal(final Jwt jwt) {
        return new AuthenticatedUser(
                jwt.getClaimAsString(JwtClaimNames.SUB)
        );
    }
}
