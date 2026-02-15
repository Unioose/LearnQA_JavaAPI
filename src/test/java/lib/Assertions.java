package lib;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertJsonByName(Response Response, String name, int expectedValue){
        Response.then().assertThat().body("$", hasKey(name));

        int value = Response.jsonPath().getInt(name);
        assertEquals(expectedValue, value, "JSON value is not equal to expected value");
    }

    public static void assertJsonParamEquals(Response Response, String name, String expectedValue){
        Response.then().assertThat().body("$", hasKey(name));

        String value = Response.jsonPath().getString(name);
        assertEquals(expectedValue, value, "JSON value in param '"+name+"' is not equal to expected value");
    }

    public static void assertCookie(Response Response, String cookieName, String expectedValue)
    {
        Response.then().assertThat().cookie(cookieName, notNullValue());

        String value = Response.getCookie(cookieName);
        assertEquals(expectedValue, value, "Cookie value is not equal to expected value");
    }

    public static void assertHeader(Response Response, String headerName, String expectedValue){
        Response.then().assertThat().header(headerName, notNullValue());

        String value = Response.getHeader(headerName);
        assertEquals(expectedValue, value, "Header value is not equal to expected value");

    }
}
