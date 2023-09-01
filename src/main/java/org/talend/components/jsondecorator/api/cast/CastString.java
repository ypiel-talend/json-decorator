package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CastString implements Cast<JsonString> {
    @Override
    public JsonArray toArray(JsonString value) throws JsonDecoratorCastException {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;
    }

    @Override
    public JsonObject toObject(JsonString value) throws JsonDecoratorCastException {
        String content = value.getString();
        String trimed = content.trim();
        if(trimed.charAt(0) == '{' && trimed.charAt(trimed.length() - 1) == '}'){
            // It is maybe a json document
            // Try to create a JsonObject if it starts en ends by { }
            try(JsonReader reader = Json.createReader(new StringReader(trimed))) {
                JsonObject jsonObject = reader.readObject();
                return jsonObject;
            }
            catch (JsonParsingException e){
                // Do nothing
            }
        }
        JsonObject object = Json.createObjectBuilder().add(DEFAULT_NAME, value).build();
        return object;
    }

    @Override
    public JsonString toString(JsonString value) throws JsonDecoratorCastException {
        return value;
    }

    @Override
    public JsonNumber toFloat(JsonString value) throws JsonDecoratorCastException {
        String string = value.getString();
        double d = Double.parseDouble(string);
        JsonNumber number = Json.createValue(d);
        return number;
    }

    @Override
    public JsonNumber toInt(JsonString value) throws JsonDecoratorCastException {
        String string = value.getString();
        int i = Integer.parseInt(string);
        JsonNumber number = Json.createValue(i);
        return number;
    }

    @Override
    public JsonValue toBoolean(JsonString value) throws JsonDecoratorCastException {
        String string = value.getString();
        JsonValue bool = Boolean.getBoolean(string) ? JsonValue.FALSE : JsonValue.TRUE;
        return bool;
    }
}
