package com.sensorberg.front.resolver.layout;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.path.json.JsonPath;
import com.sensorberg.front.resolver.TestConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

/**
 * Created by Andreas Dörner on 27.07.15.
 */
public class LayoutTest {

    private final String wrongApiKey = "wrongApiKey";
    private final String apiKey = "8a60185c54446c807a5383bf3ec5acc33dab3537ed011d10973e667f8836e998";
    private final String beaconPID = "7367672374000000ffff0000ffff00002699720950";

    @Before
    public void setup() throws Exception {
    }

    @Test
    public void getLayoutTest() {

        // Get layout
        Response response =
        given()
                .header("X-Api-Key", apiKey)
                .when()
                .get(TestConfig.getHost() + "layout")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // Test with JsonPath
        List<String> idList = from(response.body().asString()).getList("accountProximityUUIDs", String.class);
        assertThat("7367672374000000ffff0000ffff0000", isIn(idList));
    }

    public void wrongApiKeyTest() throws Exception {

        given()
                .header("X-Api-Key", wrongApiKey)
                .when()
                .get(com.sensorberg.front.resolver.TestConfig.getHost() + "layout")
                .then()
                .statusCode(204);
    }

    @Test
    public void getBeaconTest() {

        // Get layout/beacon
        Response response =
                given()
                        .header("X-pid", beaconPID, "X-Api-Key", apiKey)
                        .when()
                        .get(TestConfig.getHost() + "layout")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        // Test with JsonPath
        List<String> idList = from(response.body().asString()).getList("accountProximityUUIDs", String.class);
        assertThat("7367672374000000ffff0000ffff0000", isIn(idList));


    }

}
