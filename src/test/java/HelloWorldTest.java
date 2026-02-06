import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelloWorldTest {

    private static String baseUrl;
    private static String basePath;

    public static Response getRequest(String endpoint)
    {
        return  given()
                .baseUri(baseUrl)
                .basePath(basePath)
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    @BeforeAll
    public static void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader("ci.properties"));
        baseUrl = properties.getProperty("baseUrl");
        basePath = properties.getProperty("basePath");
    }

    @Test
    public void testRestAssured(){
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get(baseUrl+basePath+"/get_303")
                .andReturn();

        int statusCode = response.getStatusCode();
        System.out.println(statusCode);
        response.print();
        String locationHeader = response.getHeader("location");
        System.out.println(locationHeader);
    }

    @Test
    public void testRestAssured2(){
        Map <String, Object> data = new HashMap<>();
        data.put("login","secret_login");
        data.put("password","secret_pass");
        Response response = RestAssured
                .given()
                .body(data)
                .when()
                .post(baseUrl+basePath+"/get_auth_cookie")
                .andReturn();

        System.out.println("\nPretty text:");
        response.prettyPrint();

        System.out.println("\nHeaders:");
        Headers responseHeaders = response.getHeaders();
        System.out.println(responseHeaders);

        System.out.println("\nCookies:");
        String responseCookie = response.getCookie("auth_cookie");
        System.out.println(responseCookie);

        Map<String,String> cookies = new HashMap<>();
        cookies.put("auth_cookie", responseCookie);
        Response checkAuth = RestAssured
                .given()
                .body(data)
                .cookies(cookies)
                .when()
                .post(baseUrl+basePath+"/check_auth_cookie")
                .andReturn();

        checkAuth.print();
    }

    @Test
    public void testGetText(){
        Response response = RestAssured
                .get(baseUrl+basePath+"/get_text")
                .andReturn();
        response.prettyPrint();
        String body = response.getBody().asString();
        assertEquals("<html>\n  <body>Hello, world</body>\n</html>", body);
    }

    @Test
    public void testGetJsonHomework(){
        Response response = getRequest("/get_json_homework");
        //Простой вариант если нужно вывести 2 сообщение
//        String secondMessage = response.jsonPath().getString("messages[1].message");
//        System.out.println(secondMessage);

        //Вариант если нужно работать со всем ответом из запроса
        List<Map<String, String>> allMessages = response.jsonPath().getList("messages");
        if(allMessages.size()<2)
        {
            System.out.println("В ответе отсутствует второе сообщение");
        }
        else System.out.println(allMessages.get(1).get("message"));

    }

    @Test
    public void testRedirect(){
        int statusCode = 0;
        String redirectURL = "https://playground.learnqa.ru/api/long_redirect";
        String URL = redirectURL;
        ArrayList<String> redirectPath = new ArrayList<>();
        while(statusCode != 200){
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(URL)
                    .andReturn();

            statusCode = response.getStatusCode();
            if (statusCode <199|| statusCode>399)
            {
                System.out.println("Запрос вернулся с кодом: "+statusCode);
                break;
            }
            URL = response.getHeader("location");
            if(URL != null)
            {
                System.out.println("Осуществлен редирект на:");
                System.out.println(URL);
                redirectPath.add(URL);
            }
        }

        assertEquals(200,statusCode);
        if (!redirectPath.isEmpty()) {
            System.out.printf("Полный путь редиректа c %s: %s%n", redirectURL, String.join(" -> ", redirectPath));
        }
    }
}
