package com.buy01.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.buy01.common.security.jwt.JwtIssuer;
import com.buy01.common.security.jwt.JwtProperties;
import com.buy01.common.security.jwt.JwtVerifier;

class CommonSecurityAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CommonSecurityAutoConfiguration.class));

    @Test
    void verifierOnlyWhenNoPrivateKey() {
        runner.withPropertyValues("security.jwt.public-key=" + TestKeys.publicBase64(TestKeys.PAIR))
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JwtVerifier.class);
                    assertThat(ctx).doesNotHaveBean(JwtIssuer.class);
                });
    }

    @Test
    void issuerWhenPrivateKeyPresentAndExpirationInMillis() {
        runner.withPropertyValues(
                        "security.jwt.public-key=" + TestKeys.publicBase64(TestKeys.PAIR),
                        "security.jwt.private-key=" + TestKeys.privateBase64(TestKeys.PAIR),
                        "security.jwt.expiration=900000")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JwtIssuer.class);
                    assertThat(ctx.getBean(JwtProperties.class).expiration()).isEqualTo(Duration.ofMinutes(15));
                });
    }

    @Test
    void rejectsNonPositiveExpiration() {
        runner.withPropertyValues(
                        "security.jwt.public-key=" + TestKeys.publicBase64(TestKeys.PAIR),
                        "security.jwt.expiration=0")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    @Test
    void failsFastWithoutPublicKey() {
        runner.run(ctx -> assertThat(ctx).hasFailed());
    }
}
