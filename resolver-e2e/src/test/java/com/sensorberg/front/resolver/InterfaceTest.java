package com.sensorberg.front.resolver;

import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import com.sensorberg.front.resolver.TestConfig;

import static com.jayway.restassured.RestAssured.given;


/**
 * Created by Andreas Dörner on 27.07.15.
 */
public class InterfaceTest {

    @Test
    public void pingServiceTest() throws Exception {

        given()
                .when()
                .get(TestConfig.getHost() + "ping")
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON);

        given()
                .when()
                .get(TestConfig.getHost() + "synchronizations")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);


        given()
                .when()
                .get(TestConfig.getHost() + "logs")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);


        given()
                .when()
                .get(TestConfig.getHost() + "index")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

}
