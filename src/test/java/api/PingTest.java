package api;

import io.restassured.RestAssured;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;

public class PingTest {

    @Test
    public void PingTest()
    {

        RestAssured.baseURI = ConfigReader.get("api.base.url");
        given()
                .when()
                .get("/ping")
                .then()
                .statusCode(201);
    }
}
