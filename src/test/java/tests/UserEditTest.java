package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Epic("User edit cases")
@Feature("Update user")
public class UserEditTest extends BaseTestCase {

    static Map<String, String> userData;
    static String userId;
    private static String baseUrl;
    private static String basePath;
    private static final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    static Stream<Arguments> getMissingFieldData() {
        return new DataGenerator().provideMissingFieldScenarios();
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
    @Description("Успешное редактирование пользователя")
    @DisplayName("Редактирвоание  имени пользователя")
    public void testEditJustCreatedTest(){

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/login", authData);

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

        Assertions.assertResponseCodeEquals(responseEditUser, 400);
        Assertions.assertResponseTextEquals(responseEditUser, "\"error\":\"Auth token not supplied\"");

        System.out.println(responseEditUser.asString());
        System.out.println(responseEditUser.statusCode());

        //Проверка что имя пользователя не измененно

        //LOGIN
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/login", authData);

        //GET
        Response responseUserData = apiCoreRequests.makeGetRequest(baseUrl+basePath+"/user/"+userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));
        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }



}
