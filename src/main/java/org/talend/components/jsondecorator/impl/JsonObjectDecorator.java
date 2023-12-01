package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;

import java.util.function.Function;

public class JsonObjectDecorator implements JsonDecorator {

  private final Function<String, JsonDecorator> fieldsDecorators;

  public JsonObjectDecorator(final Function<String, JsonDecorator> fieldsDecorators) {
    this.fieldsDecorators = fieldsDecorators;
  }

  @Override
  public JsonValue decorate(final JsonValue rawValue) {
    if (rawValue == null) {
      return null;
    }
    if (rawValue.getValueType() != JsonValue.ValueType.OBJECT) {
      throw new IllegalArgumentException("Not a json object but " + rawValue.getValueType());
    }
    return new DecoratedJsonObjectImpl(rawValue.asJsonObject(), fieldsDecorators);
  }

  /*@Override
  public JsonValue.ValueType jsonType() {
    return JsonValue.ValueType.OBJECT;
  }*/
}
