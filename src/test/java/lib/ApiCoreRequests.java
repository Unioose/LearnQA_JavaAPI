package lib;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiCoreRequests {

    @Step("Make a GET-request with token and auth cookie")
    public Response makeGetRequest(String url, String token, String cookie)
    {
        RequestSpecification requestSpec = given()
                .filter(new AllureRestAssured());

        if (token != null) {
            requestSpec.header(new Header("x-csrf-token", token));
        }
        if (cookie != null) {
            requestSpec.cookie("auth_sid", cookie);
        }

        return requestSpec.get(url).andReturn();
    }

    @Step("Make POST-request")
    public Response makePostRequest(String url, Map<String, String> authData)
    {
        return given()
                .filter(new AllureRestAssured())
                .body(authData)
                .post(url)
                .andReturn();
    }

    @Step("Make PUT-request")
    public Response makePutRequest(String url, String token, String cookie,Map<String, String> editData)
    {

        RequestSpecification requestSpec = given()
                .filter(new AllureRestAssured())
                .body(editData);

        if (token != null) {
            requestSpec.header(new Header("x-csrf-token", token));
        }
        if (cookie != null) {
            requestSpec.cookie("auth_sid", cookie);
        }

        return requestSpec.put(url).andReturn();
    }
}
