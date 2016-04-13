package com.sensorberg.front.resolver.synchronisation;

import com.jayway.restassured.http.ContentType;
import com.sensorberg.front.resolver.Helper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.jayway.restassured.RestAssured.given;


/**
 * Created by Andreas Dörner on 27.07.15.
 */
public class SynchronizationTest {

    @Test
    public void getWrongSyncTest() throws Exception {

        given()
                .when()
                .get(com.sensorberg.front.resolver.TestConfig.getHost() + "synchronizations/434543643")
                .then()
                .statusCode(405)
                .contentType(ContentType.JSON);
    }

    @Test
    public void deleteWrongSyncTest() throws Exception {

        given()
                .when()
                .delete(com.sensorberg.front.resolver.TestConfig.getHost() + "synchronizations/434543643")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    public void addSyncTest() throws Exception {

        final InputStream stream = getClass().getClassLoader().getResourceAsStream("singleSynchronization.json");
        final String jsonInput = Helper.convertToString(stream);
        final String syncId = Helper.getJsonValue(jsonInput, "id");

        // Add new synchronization
        given()
                .body(jsonInput)
                .contentType(ContentType.JSON)
                .when()
                .post(com.sensorberg.front.resolver.TestConfig.getHost() + "synchronizations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);

        // delete created synchronisation
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(com.sensorberg.front.resolver.TestConfig.getHost() + "synchronizations/" + syncId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

}