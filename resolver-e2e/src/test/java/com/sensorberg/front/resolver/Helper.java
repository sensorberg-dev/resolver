package com.sensorberg.front.resolver;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Andreas Dörner on 27.07.15.
 */
public class Helper {

    /**
     * Helper function.
     * @return
     */
    public static String convertToString(InputStream stream) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line = bufferedReader.readLine();
        while(line != null){
            inputStringBuilder.append(line);inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }

        return inputStringBuilder.toString();
    }

    /**
     *
     * @param json
     * @param key
     * @return
     */
    public static String getJsonValue(final String json, final String key) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(json);

        JSONObject jsonObject = (JSONObject) obj;

        return (String) jsonObject.get(key);
    }
}
