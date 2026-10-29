package com.societyapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {
    @Test
    void clearsAuthenticationOnlyForActuallyPublicOperations() {
        var publicOperation = new Operation();
        var loginOperation = new Operation();
        var logoutOperation = new Operation();
        var profileOperation = new Operation();
        var logoutAllOperation = new Operation();
        var securedOperation = new Operation();
        var paths = new Paths();
        paths.addPathItem("/api/public/plans", new PathItem().get(publicOperation));
        paths.addPathItem("/api/auth/login", new PathItem().post(loginOperation));
        paths.addPathItem("/api/auth/logout", new PathItem().post(logoutOperation));
        paths.addPathItem("/api/auth/me", new PathItem().get(profileOperation));
        paths.addPathItem("/api/auth/logout-all", new PathItem().post(logoutAllOperation));
        paths.addPathItem("/api/activities", new PathItem().get(securedOperation));
        var api = new OpenAPI().paths(paths);

        new OpenApiConfig().publicOperationsDoNotRequireAuthentication().customise(api);

        assertThat(publicOperation.getSecurity()).isEmpty();
        assertThat(loginOperation.getSecurity()).isEmpty();
        assertThat(logoutOperation.getSecurity()).isEmpty();
        assertThat(profileOperation.getSecurity()).isNotEmpty();
        assertThat(logoutAllOperation.getSecurity()).isNotEmpty();
        assertThat(securedOperation.getSecurity()).isNotEmpty();
    }
}
