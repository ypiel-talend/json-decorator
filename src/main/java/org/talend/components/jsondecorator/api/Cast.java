package org.talend.components.jsondecorator.api;

import org.talend.components.jsondecorator.api.cast.JsonDecoratorCastException;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public interface Cast<T extends JsonValue> {

    String DEFAULT_NAME = "value";

    JsonArray toArray(T value) throws JsonDecoratorCastException;
    JsonObject toObject(T value) throws JsonDecoratorCastException;
    JsonString toString(T value) throws JsonDecoratorCastException;
    JsonNumber toFloat(T value) throws JsonDecoratorCastException;
    JsonNumber toInt(T value) throws JsonDecoratorCastException;
    JsonValue toBoolean(T value) throws JsonDecoratorCastException;

}
