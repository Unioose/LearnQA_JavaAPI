package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import lib.ApiCoreRequests;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Epic("User info cases")
@Feature("Get user info by id")
public class UserGetTest extends BaseTestCase {

    private static String baseUrl;
    private static String basePath;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    private Response loginUser(String email, String password) {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", email);
        authData.put("password", password);
        return apiCoreRequests.makePostRequest(baseUrl + basePath + "/user/login", authData);
    }

    @BeforeAll
    public static void setUp() throws IOException {
        Properties properties = new Properties();

        try (FileReader reader = new FileReader("ci.properties")) {
            properties.load(reader);
            baseUrl = properties.getProperty("baseUrl");
            basePath = properties.getProperty("basePath");
        }
    }

    @Test
    @Description("Должен возвращать только username пользователя")
    @DisplayName("Получение информации о пользователе без авторизации")
    public void testGetUserDataNotAuth(){
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/2", null, null);

        String[] unexpectedFields = {"firstName", "lastName", "email"};
        Assertions.assertJsonHasField(responseUserData,"username");
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }

    @Test
    @Description("Должен возвращать всю информацию о пользователе")
    @DisplayName("Получение информации о пользователе авторизованным тем же пользователем")
    public void testGetUserDetailsAuthAsSameUser(){
        Response responseGetAuth = loginUser("vinkotov@example.com", "1234");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        String header = this.getHeader(responseGetAuth, "x-csrf-token");

        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/2",header,cookie);
        
        String[] expectedFields = {"username","firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);
    }

    @Test
    @Description("Должен возвращать только username пользователя")
    @DisplayName("Получение информации о пользователе за другого пользователя")
    public void testGetUserDetailsAuthAsAnotherUser(){
        String emailFirstUser = DataGenerator.getRandomEmail();

        Map<String, String> userData = new HashMap<>();

        String[] unexpectedFields = {"firstName", "lastName", "email"};

        //Создание пользователя
        userData.put("email", emailFirstUser);
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateFirstUser = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);
        String userId = responseCreateFirstUser.jsonPath().getString("id");

        //Авторизация за пользователя
        Response responseGetAuth = loginUser(emailFirstUser, "123");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");
        String header = this.getHeader(responseGetAuth, "x-csrf-token");

        //Получение данных за пользователя, авторизованным другим пользователем
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/2",header,cookie);

        Assertions.assertJsonHasField(responseUserData,"username");
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);

        //Дополнительная проверка, что ранее созданные пользователи так же не получают данные другого пользователя
        responseGetAuth = loginUser("vinkotov@example.com", "1234");
        cookie = this.getCookie(responseGetAuth, "auth_sid");
        header = this.getHeader(responseGetAuth, "x-csrf-token");

        responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,header,cookie);

        Assertions.assertJsonHasField(responseUserData,"username");
        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);

    }
}
