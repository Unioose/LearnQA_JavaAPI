package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Epic("User delete cases")
@Feature("Delete user")
public class UserDeleteTest extends BaseTestCase {
    static Map<String, String> userData;
    static String userId;
    private static String baseUrl;
    private static String basePath;
    private static final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    private Response loginUser(String email, String password) {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", email);
        authData.put("password", password);
        return apiCoreRequests.makePostRequest(baseUrl + basePath + "/user/login", authData);
    }

    private Response generateUser(){
        //GENERATE USER
        userData = DataGenerator.getRegistrationData();
        return apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);
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
    @Description("Должен возвращать ошибку при попытке удаления пользователя, которого нельзя удалить из системы")
    @DisplayName("Удаление не удаляемого пользователя")
    public void testDeleteUndeletableUser(){
        Response responseGetAuth = loginUser("vinkotov@example.com", "1234");
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(baseUrl+basePath+"/user/2",
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));

        Assertions.assertErrorResponse(responseDeleteUser,
                400,
                "Please, do not delete test users with ID 1, 2, 3, 4 or 5." );
    }

    @Test
    @Description("Проверка успешного удаления пользователя после авторизации")
    @DisplayName("Удаление существующего пользователя")
    public void testDeleteUserSuccessfully(){
        //Удаление пользователя
        Response createUser = generateUser();
        userId = createUser.jsonPath().getString("id");
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));

        Assertions.assertSuccessResponse(responseDeleteUser,
                200,
                "success",
                "!");

        //Проверка, что пользователь удален
        responseGetAuth = loginUser("vinkotov@example.com", "1234");
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertErrorResponse(responseUserData,
                404,
                "User not found" );
    }

    @Test
    @Description("Должен возвращать ошибку при попытке удаления пользователя под другим пользователем")
    @DisplayName("Удаление пользователя под другим пользователем")
    public void testDeleteUserForAnotherUser(){
        //Создание первого пользователя
        Response createUser = generateUser();
        userId = createUser.jsonPath().getString("id");
        Map<String, String> firstUserData = new HashMap<>(userData);

        //Удаление первого пользователя под вторым пользователем
        Response createSecondUser = generateUser();
        Map<String, String> secondUserData = new HashMap<>(userData);
        Response responseGetAuth = loginUser(secondUserData.get("email"), secondUserData.get("password"));
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));

        Assertions.assertErrorResponse(responseDeleteUser,
                400,
                "This user can only delete their own account." );

        //Проверка, что пользователь НЕ удален
        responseGetAuth = loginUser(firstUserData.get("email"),firstUserData.get("password"));
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertSuccessResponse(responseUserData,
                200,
                "id",
                userId);

    }
}
