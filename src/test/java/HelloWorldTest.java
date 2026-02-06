import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        Properties properties = new Properties();
        String test = baseUrl+basePath;
        System.out.println(test);
        Map<String,String> params = new HashMap<>();
        params.put("name", "John");
        JsonPath response = given()
                .queryParams(params)
                .get(baseUrl+basePath+"/hello")
                .jsonPath();
        String answer = response.get("answer");
        String name = response.get("answer2");
        assertNotNull(answer);
        // assertNotNull(name); //Упадёт т.к. answer2 отсутствует в ответе
        System.out.println(answer);
    }

    @Test
    public void testGetText(){
        Response response = RestAssured
                .get(baseUrl + "/get_text")
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
}
