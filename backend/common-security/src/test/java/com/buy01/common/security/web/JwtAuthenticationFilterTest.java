package com.buy01.common.security.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.buy01.common.security.TestKeys;
import com.buy01.common.security.jwt.AuthenticatedUser;
import com.buy01.common.security.jwt.JwtIssuer;
import com.buy01.common.security.jwt.JwtVerifier;

class JwtAuthenticationFilterTest {

    private final JwtIssuer issuer = new JwtIssuer(TestKeys.PAIR.getPrivate(), Duration.ofHours(1), Clock.systemUTC());
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtVerifier(TestKeys.PAIR.getPublic()));

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + issuer.issue("u1", "a@b.c", "SELLER").token());
        Authentication[] seen = new Authentication[1];

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                seen[0] = SecurityContextHolder.getContext().getAuthentication();
            }
        });

        assertThat(seen[0]).isNotNull();
        assertThat(seen[0].getPrincipal()).isEqualTo(new AuthenticatedUser("u1", "a@b.c", "SELLER"));
        assertThat(seen[0].getAuthorities()).extracting(Object::toString).containsExactly("ROLE_SELLER");
    }

    @Test
    void leavesRequestAnonymousWhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer forged");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void ignoresNonBearerAuthorization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
