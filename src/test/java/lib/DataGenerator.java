package lib;

import org.junit.jupiter.params.provider.Arguments;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class DataGenerator {

    public static String getRandomEmail(){
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        return "learnqa" + getString(5) + timestamp + "@example.com";
    }

    public static String getString(int numberOfCharacters){
        Random random = new Random();
        char randomChar = (char) ('A' + random.nextInt(26));
        StringBuilder sb = new StringBuilder(numberOfCharacters);
        for (int i = 0; i < numberOfCharacters; i++) {
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public static Map<String, String> getRegistrationData(){
        Map<String, String> data = new HashMap<>();
        data.put("email", DataGenerator.getRandomEmail());
        data.put("password", "123");
        data.put("username","learnqa");
        data.put("firstName","learnqa");
        data.put("lastName","learnqa");

        return data;
    }

    public static Map<String, String> getRegistrationData(Map <String, String> nonDefaultValues) {
        Map<String, String> defaultValues = DataGenerator.getRegistrationData();

        Map<String, String> userData = new HashMap<>();
        String[] keys = {"email","password", "username", "firstName", "lastName"};
        for(String key: keys){
            if(nonDefaultValues.containsKey(key)){
                userData.put(key, nonDefaultValues.get(key));
            }else {
                userData.put(key, defaultValues.get(key));
            }
        }
        return userData;
    }

    public Stream<Arguments> provideMissingFieldScenarios() {
        return Stream.of(
                Arguments.of("email", null, "password123", "testuser", "Test", "User"),
                Arguments.of("password", "test@example.com", null, "testuser", "Test", "User"),
                Arguments.of("username", "test@example.com", "password123", null, "Test", "User"),
                Arguments.of("firstName", "test@example.com", "password123", "testuser", null, "User"),
                Arguments.of("lastName", "test@example.com", "password123", "testuser", "Test", null)
        );
    }
}
