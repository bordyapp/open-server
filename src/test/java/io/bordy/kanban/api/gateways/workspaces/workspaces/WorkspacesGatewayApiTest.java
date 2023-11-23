package io.bordy.kanban.api.gateways.workspaces.workspaces;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(OidcWiremockTestResource.class)
public class WorkspacesGatewayApiTest {

    @Test
    @DisplayName("myWorkspaces: return 401 Unauthorized when request without token")
    public void myWorkspacesReturnUnauthorizedWhenRequestWithoutToken() {
        given()
                .when().get("/api/v1/gateway/workspaces")
                .then()
                .statusCode(401);
    }

    @ParameterizedTest
    @MethodSource("io.bordy.ApiTestUtils#brokenTokens")
    @DisplayName("myWorkspaces: return 401 Unauthorized when request with broken token")
    public void myWorkspacesReturnUnauthorizedWhenRequestWithBrokenToken(String jwt) {
        given()
                .when()
                .header("Authorization", jwt)
                .get("/api/v1/gateway/workspaces")
                .then()
                .statusCode(401);
    }

}
