package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class CastIdent implements Cast<JsonValue> {
    @Override
    public JsonArray toArray(JsonValue value) throws JsonDecoratorCastException {
        return (JsonArray) value;
    }

    @Override
    public JsonObject toObject(JsonValue value) throws JsonDecoratorCastException {
        return (JsonObject) value;
    }

    @Override
    public JsonString toString(JsonValue value) throws JsonDecoratorCastException {
        return (JsonString) value;
    }

    @Override
    public JsonNumber toFloat(JsonValue value) throws JsonDecoratorCastException {
        return (JsonNumber) value;
    }

    @Override
    public JsonNumber toInt(JsonValue value) throws JsonDecoratorCastException {
        return (JsonNumber) value;
    }

    @Override
    public JsonValue toBoolean(JsonValue value) throws JsonDecoratorCastException {
        return value == JsonValue.TRUE ? JsonValue.TRUE : JsonValue.FALSE;
    }
}
