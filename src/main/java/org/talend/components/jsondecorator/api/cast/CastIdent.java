package org.talend.components.jsondecorator.api.cast;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.Cast;

public class CastIdent implements Cast<JsonValue> {
    @Override
    public JsonArray toArray(JsonValue value) {
        return (JsonArray) value;
    }

    @Override
    public JsonObject toObject(JsonValue value) {
        return (JsonObject) value;
    }

    @Override
    public JsonString toString(JsonValue value) {
        return (JsonString) value;
    }

    @Override
    public JsonNumber toFloat(JsonValue value) {
        return (JsonNumber) value;
    }

    @Override
    public JsonNumber toInt(JsonValue value) {
        return (JsonNumber) value;
    }

    @Override
    public JsonValue toBoolean(JsonValue value) {
        return value == JsonValue.TRUE ? JsonValue.TRUE : JsonValue.FALSE;
    }
}
