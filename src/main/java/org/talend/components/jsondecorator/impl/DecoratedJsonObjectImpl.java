package org.talend.components.jsondecorator.impl;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DecoratedJsonObjectImpl implements JsonObject {

  private final JsonObject delegateJsonObject;

  private final Function<String, JsonDecorator> fieldsDecorators;


  public DecoratedJsonObjectImpl(final JsonObject delegateJsonObject,
      final Function<String, JsonDecorator> fieldsDecorators) {
    this.delegateJsonObject = delegateJsonObject;
    this.fieldsDecorators = fieldsDecorators;
  }

  @Override
  public JsonArray getJsonArray(final String name) {
    final JsonValue value = this.transform(name);
    return value != null ? JsonArray.class.cast(value) : null;
  }

  @Override
  public JsonObject getJsonObject(final String name) {
    final JsonValue value = this.transform(name);
    return value != null ? JsonObject.class.cast(value) : null;
  }

  @Override
  public JsonNumber getJsonNumber(final String name) {
    final JsonValue value = this.transform(name);
    return value != null ? JsonNumber.class.cast(value) : null;
  }

  @Override
  public JsonString getJsonString(final String name) {
    final JsonValue value = this.transform(name);
    return value != null ? JsonString.class.cast(value) : null;
  }

  @Override
  public String getString(final String name) {
    final JsonValue value = this.transform(name);
    return value != null ? JsonString.class.cast(value).getString() : null;
  }

  @Override
  public String getString(final String name, final String defaultValue) {
    final String value = this.getString(name);
    return value == null ? defaultValue : value;
  }

  @Override
  public int getInt(final String name) {
    final JsonNumber jsonNumber = this.getJsonNumber(name);
    return jsonNumber.intValue();
  }

  @Override
  public int getInt(final String name, final int defaultValue) {
    final JsonNumber value = this.getJsonNumber(name);
    return value == null ? defaultValue : value.intValue();
  }

  @Override
  public boolean getBoolean(final String name) {
    final JsonValue value = this.transform(name);
    return value == JsonValue.TRUE;
  }

  @Override
  public boolean getBoolean(final String name, final boolean defaultValue) {
    final JsonValue value = this.transform(name);
    return value == null ? defaultValue : value == JsonValue.TRUE;
  }

  @Override
  public boolean isNull(final String name) {
    final JsonValue value = this.transform(name);
    return value == null || value.getValueType() == ValueType.NULL;
  }

  @Override
  public int size() {
    return this.delegateJsonObject.size();
  }

  @Override
  public boolean isEmpty() {
    return this.delegateJsonObject.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return this.delegateJsonObject.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return false;
  }

  @Override
  public JsonValue get(final Object key) {
    if (key == null) {
      return null;
    }
    return this.transform(key.toString());
  }

  @Override
  public JsonValue put(final String key, final JsonValue value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public JsonValue remove(final Object key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(final Map<? extends String, ? extends JsonValue> m) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    return this.delegateJsonObject.keySet();
  }

  @Override
  public Collection<JsonValue> values() {
    return this.keySet().stream().map(this::transform).collect(Collectors.toList());
  }

  @Override
  public Set<Entry<String, JsonValue>> entrySet() {
    return this.delegateJsonObject.entrySet()
        .stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), this.transform(e.getKey(), e.getValue())))
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
        .entrySet();
  }

  @Override
  public ValueType getValueType() {
    return this.delegateJsonObject.getValueType();
  }

  private JsonValue transform(String name) {
    final JsonValue rawValue = this.delegateJsonObject.get(name);
    final JsonDecorator decorator = this.fieldsDecorators.apply(name);

    if (decorator != null) {
      return decorator.decorate(rawValue);
    }
    else {
      return rawValue;
    }
  }

  private JsonValue transform(String name, JsonValue rawValue) {
    final JsonDecorator decorator = this.fieldsDecorators.apply(name);

    if (decorator != null) {
      return decorator.decorate(rawValue);
    }
    else {
      return rawValue;
    }
  }

}
