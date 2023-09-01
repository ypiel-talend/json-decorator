package org.talend.components.jsondecorator.impl;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TestUtil {

    public static InputStream loadStream(String path){
        InputStream stream = TestUtil.class.getResourceAsStream(path);
        return stream;
    }

    public static JsonValue loadJson(String path){
        InputStream stream = loadStream(path);
        JsonReader reader = Json.createReader(stream);
        return reader.readValue();
    }

    public static Properties loadProperties(String path) throws IOException {
        InputStream stream = loadStream(path);
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Properties prop = new Properties();
        prop.load(isr);

        return prop;
    }

}
