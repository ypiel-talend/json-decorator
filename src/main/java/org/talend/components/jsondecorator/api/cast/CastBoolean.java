package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.math.BigInteger;

public class CastBoolean implements Cast<JsonValue> {
    @Override
    public JsonArray toArray(JsonValue value) throws JsonDecoratorCastException {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;
    }

    @Override
    public JsonObject toObject(JsonValue value) throws JsonDecoratorCastException {
        JsonObject object = Json.createObjectBuilder().add(DEFAULT_NAME, value).build();
        return object;
    }

    @Override
    public JsonString toString(JsonValue value) throws JsonDecoratorCastException {
        JsonString string = Json.createValue(value.toString());
        return string;
    }

    @Override
    public JsonNumber toFloat(JsonValue value) throws JsonDecoratorCastException {
        JsonNumber number = value == JsonValue.TRUE ? Json.createValue(1d) : Json.createValue(0d);
        return number;
    }

    @Override
    public JsonNumber toInt(JsonValue value) throws JsonDecoratorCastException {
        JsonNumber number = value == JsonValue.TRUE ? Json.createValue(1) : Json.createValue(0);
        return number;
    }

    @Override
    public JsonValue toBoolean(JsonValue value) throws JsonDecoratorCastException {
        return value;
    }
}
