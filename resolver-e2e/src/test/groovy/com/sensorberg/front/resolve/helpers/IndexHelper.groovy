package com.sensorberg.front.resolve.helpers

import com.sensorberg.front.resolve.TestConfig

import static com.jayway.restassured.RestAssured.expect

class IndexHelper {

    private static String PATH = "index"

    /**
     * Call a delete in index to delete all Data
     * @return
     */
    static public String deleteIndex() {
        return expect()
                .given()
                .headers(["Content-Type": "application/json"])
                .when()
                .delete(TestConfig.getHost() + PATH)
                .thenReturn().getBody()
    }
}
