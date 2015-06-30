package com.sensorberg.front.resolve.helpers

import com.sensorberg.front.resolve.TestConfig
import com.sensorberg.front.resolve.resources.layout.domain.LayoutRequestBody
import com.sensorberg.front.resolve.resources.layout.domain.LayoutResponse

import static com.jayway.restassured.RestAssured.expect

class ResolverLayoutHelper {
    static LayoutResponse layout(String apiKey) {
        def response = expect()
                .given()
                .headers(["X-Api-Key": apiKey, "Content-Type": "application/json"])
                .when()
                .get(TestConfig.url())
                .thenReturn()
        if(response.statusCode == 204) {
            return null
        }
        return response.getBody().as(LayoutResponse)
    }

    static LayoutResponse layout(String apiKey, LayoutRequestBody body) {
        return expect()
                .statusCode(200)
                .given()
                .body(body)
                .headers(["X-Api-Key": apiKey, "Content-Type": "application/json"])
                .when()
                .post(TestConfig.url())
                .thenReturn().getBody().as(LayoutResponse)
    }
}
