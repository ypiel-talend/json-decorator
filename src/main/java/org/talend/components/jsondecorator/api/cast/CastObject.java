package org.talend.components.jsondecorator.api.cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.Cast;

public class CastObject implements Cast<JsonObject> {
    @Override
    public JsonArray toArray(JsonObject value) {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;
    }

    @Override
    public JsonObject toObject(JsonObject value) {
        return value;
    }

    @Override
    public JsonString toString(JsonObject value) {
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
