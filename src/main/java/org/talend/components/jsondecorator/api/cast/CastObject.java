package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.math.BigInteger;

public class CastObject implements Cast<JsonObject> {
    @Override
    public JsonArray toArray(JsonObject value) throws JsonDecoratorCastException {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;
    }

    @Override
    public JsonObject toObject(JsonObject value) throws JsonDecoratorCastException {
        return value;
    }

    @Override
    public JsonString toString(JsonObject value) throws JsonDecoratorCastException {
        JsonString string = Json.createValue(value.toString());
        return string;
    }

    @Override
    public JsonNumber toFloat(JsonObject value) throws JsonDecoratorCastException {
        throw new JsonDecoratorCastException("Can't cast JsonObject to Float.");
    }

    @Override
    public JsonNumber toInt(JsonObject value) throws JsonDecoratorCastException {
        throw new JsonDecoratorCastException("Can't cast JsonObject to Int.");
    }

    @Override
    public JsonValue toBoolean(JsonObject value) throws JsonDecoratorCastException {
        throw new JsonDecoratorCastException("Can't cast JsonObject to Boolean.");
    }
}
