package api;

import api.models.Booking;
import api.models.BookingDates;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.ConfigReader;

import static io.restassured.RestAssured.given;

public class BookingTest {


    @BeforeMethod
    public void beforeMethod() {
        RestAssured.baseURI= ConfigReader.get("api.base.url");
    }
    @Test
    public void checkout() {

        BookingDates dates= new BookingDates("2026-01-01", "2026-01-10");
        Booking newBooking = new Booking("Hafid", "idados", 150, true, dates, "Breakfast");

        Response createResponse=
                given()
                        .contentType("application/json")
                        .body(newBooking)
                        .when()
                        .post("/booking")
                        .then()
                        .statusCode(200)
                        .extract().response();

        int bookingId = createResponse.jsonPath().getInt("bookingid");
        Assert.assertTrue(bookingId > 0, "Booking ID should be a positive number");

        given()
                .pathParam("id", bookingId)
                .when()
                .get("/booking/{id}")
                .then()
                .statusCode(200)
                .body("firstname", org.hamcrest.Matchers.equalTo("Hafid"))
                .body("lastname", org.hamcrest.Matchers.equalTo("idados"))
                .body("totalprice", org.hamcrest.Matchers.equalTo(150));
    }
}
