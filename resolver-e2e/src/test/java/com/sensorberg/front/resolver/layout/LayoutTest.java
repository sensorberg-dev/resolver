package com.sensorberg.front.resolver.layout;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.sensorberg.front.resolver.Helper;
import com.sensorberg.front.resolver.TestConfig;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


/**
 * Created by Andreas Dörner on 27.07.15.
 */
public class LayoutTest {

    private final String wrongApiKey = "wrongApiKey";
    private final String apiKey = "8a60185c54446c807a5383bf3ec5acc33dab3537ed011d10973e667f8836e998";

    @Before
    public void setup() throws Exception {
        deleteDatabase();
    }

    @After
    public void cleanup() throws Exception {
        deleteDatabase();
    }

    private void deleteDatabase() {
        // Delete database
        given()
                .when()
                .delete(com.sensorberg.front.resolver.TestConfig.getHost() + "index")
                .then()
                .statusCode(200);
    }

    @Test
    public void wrongApiKeyTest() throws Exception {

        given()
                .header("X-Api-Key", wrongApiKey)
                .when()
                .get(com.sensorberg.front.resolver.TestConfig.getHost() + "layout")
                .then()
                .statusCode(204);
    }


    @Test
    /**
     * 1. Inject Sync Response
     * 2. Check Layout
     */
    public void fullLayoutTripTest() throws IOException, ParseException {

        final String syncId = "testSyncId";
        final int changedItems = 73;

        // get JSON body from file
        final InputStream stream = getClass().getClassLoader().getResourceAsStream("injectSyncResponse.json");
        final String jsonBody = Helper.convertToString(stream);

        // inject sync response
        given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(TestConfig.getHost() + "synchronizations/" + syncId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("synchronizationId", is(syncId))
                .body("status", is(true))
                .body("changedItems", is(changedItems));


        // check layouts
        // Get layout/beacon
        Response response =
                given()
                        .header("X-Api-Key", apiKey)
                        .when()
                        .get(TestConfig.getHost() + "layout")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();

        String responseJson = response.body().asString();

        // get JSON body from file
        final InputStream streamReference = getClass().getClassLoader().getResourceAsStream("referenceFullLayoutTrip.json");
        final String jsonReference = Helper.convertToString(streamReference);

        assertThat(responseJson, sameJSONAs(jsonReference)
                .allowingExtraUnexpectedFields()
                .allowingAnyArrayOrdering());

    }

}
