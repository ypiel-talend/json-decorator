package org.talend.components.jsondecorator.impl;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;

public class JsonArrayDecorator implements JsonDecorator {

  private final ArrayFilters filters;

  public JsonArrayDecorator(final ArrayFilters filters) {
    this.filters = filters;
  }

  @Override
  public JsonValue decorate(final JsonValue rawValue) {
    if (rawValue == null) {
      return null;
    }
    if (rawValue.getValueType() != JsonValue.ValueType.ARRAY) {
      throw new IllegalArgumentException("Not a json array but " + rawValue.getValueType());
    }
    return this.buildFrom(rawValue.asJsonArray());
  }


  private JsonArray buildFrom(JsonArray original) {
    final JsonArrayBuilder builder = Json.createArrayBuilder();
    original.stream()
        .filter(this.filters::isConcerned)
        .map(this.filters::apply)
        .forEach(builder::add);
    return builder.build();
  }
}
