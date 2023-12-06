package org.talend.components.jsondecorator.api.cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.Cast;

public class CastBoolean implements Cast<JsonValue> {
    @Override
    public JsonArray toArray(JsonValue value) {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;
    }

    @Override
    public JsonObject toObject(JsonValue value) {
        JsonObject object = Json.createObjectBuilder().add(DEFAULT_NAME, value).build();
        return object;
    }

    @Override
    public JsonString toString(JsonValue value) {
        JsonString string = Json.createValue(value.toString());
        return string;
    }

    @Override
    public JsonNumber toFloat(JsonValue value) {
        JsonNumber number = value == JsonValue.TRUE ? Json.createValue(1d) : Json.createValue(0d);
        return number;
    }

    @Override
    public JsonNumber toInt(JsonValue value) {
        JsonNumber number = value == JsonValue.TRUE ? Json.createValue(1) : Json.createValue(0);
        return number;
    }

    @Override
    public JsonValue toBoolean(JsonValue value) {
        return value;
    }
}
