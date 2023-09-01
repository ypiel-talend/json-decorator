package org.talend.components.jsondecorator.impl;

import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;
import org.talend.components.jsondecorator.api.cast.JsonDecoratorCastException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DecoratedJsonObject extends DecoratedJsonValueImpl implements JsonObject {

    private final JsonObject delegateAsJsonObject;

    DecoratedJsonObject(JsonValue delegate, JsonDecoratorBuilder.JsonDecorator decorator, String path, JsonValue parent) {
        super(delegate, decorator, path, parent);
        this.delegateAsJsonObject = JsonObject.class.cast(super.getDelegate());
    }


    @Override
    public JsonArray getJsonArray(String s) {
        return new DecoratedJsonArray(this.get(s), this.getDecorator(), this.buildPath(s), this);
    }

    @Override
    public DecoratedJsonObject getJsonObject(String s) {
        return new DecoratedJsonObject(this.delegateAsJsonObject.getJsonObject(s), this.getDecorator(), this.buildPath(s), this);
    }

    @Override
    public JsonNumber getJsonNumber(String s) {
        JsonValue jsonValue = this.get(s);
        return JsonNumber.class.cast(jsonValue);
    }

    @Override
    public JsonString getJsonString(String s) {
        return this.delegateAsJsonObject.getJsonString(s);
    }

    @Override
    public String getString(String s) {
        return ((JsonString)this.get(s)).getString();
    }

    @Override
    public String getString(String s, String s1) {
        JsonValue v = this.get(s);
        return v != null && v instanceof JsonString ? (JsonString.class.cast(v)).getString() : s1;
    }

    @Override
    public int getInt(String s) {
        return ((JsonNumber)this.get(s)).intValue();
    }

    @Override
    public int getInt(String s, int i) {
        JsonValue v = this.get(s);
        return v != null && v instanceof JsonNumber ? (JsonNumber.class.cast(v)).intValue() : i;
    }

    @Override
    public boolean getBoolean(String s) {
        return this.get(s) == JsonValue.TRUE;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        JsonValue v = this.get(s);
        return (v != null && (v == JsonValue.TRUE || v != JsonValue.FALSE)) ? v == JsonValue.TRUE : b;
    }

    @Override
    public boolean isNull(String s) {
        return this.get(s) == JsonValue.NULL;
    }

    @Override
    public int size() {
        return this.entrySet().size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.entrySet().stream().map(e -> e.getKey()).filter(e -> e.equals(key)).findFirst().isPresent();
    }

    @Override
    public boolean containsValue(Object value) {
        return this.entrySet().stream().map(e -> e.getValue()).filter(e -> e.equals(value)).findFirst().isPresent();
    }

    @Override
    public JsonValue get(Object key) {
        try {
            JsonValue delegatedValue = this.delegateAsJsonObject.get(key);
            String childPath = this.buildPath(String.class.cast(key));
            List<JsonDecoratorBuilder.JsonDecoratorConfiguration> childCast = this.getDecorator().getConfigurations(childPath, JsonDecoratorBuilder.JsonDecoratorAction.CAST);

            if (delegatedValue.getValueType() == ValueType.OBJECT) {
                delegatedValue = new DecoratedJsonObject(delegatedValue, this.getDecorator(), childPath, this);
            } else if (delegatedValue.getValueType() == ValueType.ARRAY) {
                delegatedValue = new DecoratedJsonArray(delegatedValue, this.getDecorator(), childPath, this);
            }
            JsonValue cast = this.getDecorator().cast(childPath, delegatedValue);

            if (!childCast.isEmpty() && cast.getValueType() == ValueType.OBJECT) {
                cast = new DecoratedJsonObject(cast, this.getDecorator(), childPath, this);
            }
            return cast;
        } catch (JsonDecoratorCastException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public JsonValue put(String key, JsonValue value) {
        return this.delegateAsJsonObject.put(key, value);
    }

    @Override
    public JsonValue remove(Object key) {
        return this.delegateAsJsonObject.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends JsonValue> m) {
        this.delegateAsJsonObject.putAll(m);
    }

    @Override
    public void clear() {
        this.delegateAsJsonObject.clear();
    }

    @Override
    public Set<String> keySet() {
        return this.delegateAsJsonObject.keySet();
    }

    @Override
    public Collection<JsonValue> values() {
        Map<String, JsonValue> decoratedMap = this.toMap();
        return decoratedMap.values();
    }

    @Override
    public Set<Entry<String, JsonValue>> entrySet() {
        Map<String, JsonValue> decoratedMap = this.toMap();
        return decoratedMap.entrySet();
    }

    private Map<String, JsonValue> toMap(){
        Set<Entry<String, JsonValue>> entries = this.delegateAsJsonObject.entrySet();

        Map<String, JsonValue> decoratedMap = new HashMap<>();
        entries.stream().forEach(e -> {
            String key = e.getKey();
            JsonValue value = this.get(key); // Return the decorated value
            decoratedMap.put(key, value);
        });

        return decoratedMap;
    }

    @Override
    public ValueType getValueType() {
        return this.delegateAsJsonObject.getValueType();
    }

    @Override
    public String toString() {
        return String.format("DecoratedJsonObject(%s)", this.delegateAsJsonObject.toString());
    }
}
