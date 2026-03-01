package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.BaseTestCase;
import lib.Assertions;
import lib.DataGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Epic("Кейсы для проверки запроса регистрации нового пользователя")
@Feature("Регистрация")
public class UserRegisterTests extends BaseTestCase {

    private static String baseUrl;
    private static String basePath;
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

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

    @Test
    @Description("Должен возвращать ошибку при попытке регистрации с уже существующим email")
    @DisplayName("Регистрация пользователя с email, который есть в системе")
    public void testCreateUserWithExistingEmail(){
        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertErrorResponse(responseCreateAuth, 400, "Users with email '"+ email +"' already exists");
    }

    @Test
    @Description("Должен возвращать идентификатор зарегистрированного пользователя")
    @DisplayName("Регистрация пользователя с валидными данными")
    public void testCreateUserSuccessfully(){
        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Test
    @Description("Должен возвращать ошибку при попытке регистрации с невалидным email")
    @DisplayName("Регистрация с невалидным email")
    public void testCreateUserWithInvalidEmail(){
        String invalidEmail = "invalidemailexample.com";
        Map<String, String> userData = new HashMap<>();
        userData.put("email", invalidEmail);
        userData = DataGenerator.getRegistrationData(userData);
        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertErrorResponse(responseCreateAuth, 400, "Invalid email format");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getMissingFieldData")
    @Description("Должен возвращать ошибку при попытке регистрации без обязательных полей")
    @DisplayName("Регистрация без обязательного поля: ")
    public void testCreateUserWithMissingField(String testName, String email, String password,
                                               String username, String firstName, String lastName){
        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", password);
        userData.put("username", username);
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertErrorResponse(responseCreateAuth, 400, "The following required params are missed: " +testName);
    }

    @Test
    @Description("Должен возвращать ошибку при попытке регистрации с коротким username")
    @DisplayName("Регистрация с коротким username")
    public void testCreateUserWithShortUserName(){
        String invalidIUserName = DataGenerator.getString(1);
        Map<String, String> userData = new HashMap<>();
        userData.put("username", invalidIUserName);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertErrorResponse(responseCreateAuth, 400, "The value of 'username' field is too short");

    }

    @Test
    @Description("Должен возвращать ошибку при попытке регистрации с длинным username")
    @DisplayName("Регистрация с длинным username")
    public void testCreateUserWithLongUserName(){
        String invalidIUserName = DataGenerator.getString(251);
        Map<String, String> userData = new HashMap<>();
        userData.put("username", invalidIUserName);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(baseUrl+basePath+"/user/", userData);

        Assertions.assertErrorResponse(responseCreateAuth, 400, "The value of 'username' field is too long");
    }
}
