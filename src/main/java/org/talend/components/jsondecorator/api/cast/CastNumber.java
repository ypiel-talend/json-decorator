package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.math.BigInteger;

public class CastNumber implements Cast<JsonNumber> {
    @Override
    public JsonArray toArray(JsonNumber value) throws JsonDecoratorCastException {
        JsonArray array = Json.createArrayBuilder().add(value).build();
        return array;    }

    @Override
    public JsonObject toObject(JsonNumber value) throws JsonDecoratorCastException {
        JsonObject object = Json.createObjectBuilder().add(DEFAULT_NAME, value).build();
        return object;
    }

    @Override
    public JsonString toString(JsonNumber value) throws JsonDecoratorCastException {
        JsonString string = Json.createValue(value.toString());
        return string;
    }

    @Override
    public JsonNumber toFloat(JsonNumber value) throws JsonDecoratorCastException {
        String string = value.toString();
        if(string.indexOf('.') < 0){
            string += ".0";
        }
        JsonNumber number = Json.createValue(Double.parseDouble(string));
        return number;
    }

    @Override
    public JsonNumber toInt(JsonNumber value) throws JsonDecoratorCastException {
        String string = value.toString();
        return value;
    }

    @Override
    public JsonValue toBoolean(JsonNumber value) throws JsonDecoratorCastException {
        JsonValue bool = value.bigIntegerValueExact().equals(BigInteger.ZERO) ? JsonValue.FALSE : JsonValue.TRUE;
        return bool;
    }
}
