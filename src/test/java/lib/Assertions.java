package lib;

import io.restassured.response.Response;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Assertions {
    public static void assertJsonByName(Response Response, String name, int expectedValue){
        Response.then().assertThat().body("$", hasKey(name));

        int value = Response.jsonPath().getInt(name);
        assertEquals(expectedValue, value, "JSON value is not equal to expected value");
    }

    public static void assertJsonByName(Response Response, String name, String expectedValue){
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

    public static void assertResponseTextEquals(Response Response, String expectedAnswer){
        assertEquals(
                expectedAnswer,
                Response.asString(),
                "Response text is not as expected"
        );
    }

    public static void assertResponseCodeEquals(Response Response, int expectedStatusCode){
        assertEquals(
                expectedStatusCode,
                Response.statusCode(),
                "Response status code is not as expected"
        );
    }

    public static void assertJsonHasField(Response Response, String expectedFieldName){
        Response.then().assertThat().body("$", hasKey(expectedFieldName));
    }

    public static void assertJsonHasFields(Response Response, String[] expectedFieldNames){
        for(String expectedFiledName : expectedFieldNames)
            Assertions.assertJsonHasField(Response, expectedFiledName);
    }

    public static void assertJsonHasNotField(Response Response, String unexpectedFieldName){
        Response.then().assertThat().body("$", not(hasKey(unexpectedFieldName)));
    }

    public static void assertJsonHasNotFields(Response Response, String[] unexpectedFieldNames){
        for(String unexpectedFiledName : unexpectedFieldNames)
            Assertions.assertJsonHasNotField(Response, unexpectedFiledName);
    }
}
