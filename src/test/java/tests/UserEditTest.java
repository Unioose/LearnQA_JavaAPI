package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
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

@Epic("User edit cases")
@Feature("Update user")
public class UserEditTest extends BaseTestCase {

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

    @BeforeAll
    public static void setUp() throws IOException {
        Properties properties = new Properties();

        try (FileReader reader = new FileReader("ci.properties")) {
            properties.load(reader);
            baseUrl = properties.getProperty("baseUrl");
            basePath = properties.getProperty("basePath");
        }
    }

    @BeforeEach
    public void generateUser(){
        //GENERATE USER
        userData = DataGenerator.getRegistrationData();
        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);
        userId = responseCreateAuth.jsonPath().getString("id");
    }

    @Test
    @Description("Проверка успешного редактирования имени пользователя после авторизации")
    @DisplayName("Редактирование имени существующего пользователя")
    public void testEditJustCreatedTest(){

        //LOGIN
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));

        //EDIT
        String newName = "Changed Name";
        Map<String , String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData);

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Description("Должен возвращать ошибку при попытке редактирования пользователя без авторизации")
    @DisplayName("Редактирование без авторизации")
    public void testEditUserWithoutAuthorization()
    {
        //EDIT
        String newName = "Changed Name";
        Map<String , String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(baseUrl+basePath+"/user/"+userId,
                null,
                null,
                editData);

        Assertions.assertErrorResponse(responseEditUser,
                400,
                "Auth token not supplied");

        //Проверка, что имя пользователя не изменено

        //LOGIN
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }


    @Test
    @Description("Должен возвращать ошибку при попытке редактирования под другим пользователем")
    @DisplayName("Редактирование под другим пользователем")
    public void testEditUserForAnotherUser()
    {
        //GENERATE SECOND USER
        Map<String, String> secondUserData = DataGenerator.getRegistrationData();
        Response responseCreateAuthSecondUser = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", secondUserData);

        //LOGIN FOR SECOND USER
        Response responseGetAuthSecondUser = loginUser(secondUserData.get("email"), secondUserData.get("password"));

        //EDIT FIRST USER
        String newName = "Changed Name";
        Map<String , String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuthSecondUser, "x-csrf-token"),
                this.getCookie(responseGetAuthSecondUser, "auth_sid"),
                editData);

        Assertions.assertErrorResponse(responseEditUser,
                400,
                "This user can only edit their own data.");

        //Проверка, что имя пользователя не изменено

        //LOGIN
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }

    @Test
    @Description("Должен возвращать ошибку при попытке редактирования с невалидным email")
    @DisplayName("Редактирование с невалидным email")
    public void testEditUserWithInvalidEmail()
    {
        //LOGIN
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));

        //EDIT
        String newEmail = "invalidEmailexample.com";
        Map<String , String> editData = new HashMap<>();
        editData.put("email", newEmail);

        Response responseEditUser = apiCoreRequests.makePutRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData);

        Assertions.assertErrorResponse(responseEditUser,
                400,
                "Invalid email format");

        //Проверка, что email пользователя не изменился

        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "email", userData.get("email"));
    }

    @Test
    @Description("Должен возвращать ошибку при попытке редактирования с коротким firstName")
    @DisplayName("Редактирование с невалидным firstName")
    public void testEditUserWithShortFirstName()
    {
        //LOGIN
        Response responseGetAuth = loginUser(userData.get("email"), userData.get("password"));

        //EDIT
        String newShortName = DataGenerator.getString(1);
        Map<String , String> editData = new HashMap<>();
        editData.put("firstName", newShortName);

        Response responseEditUser = apiCoreRequests.makePutRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"),
                editData);

        Assertions.assertErrorResponse(responseEditUser,
                400,
                "The value for field `firstName` is too short");

        //Проверка, что имя пользователя не изменено

        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }
}
