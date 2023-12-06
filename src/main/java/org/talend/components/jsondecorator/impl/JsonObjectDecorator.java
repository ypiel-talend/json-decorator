package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;

import java.util.Map;
import java.util.Set;

public class JsonObjectDecorator implements JsonDecorator {

  private final Map<String, JsonDecorator> fieldsDecorators;

  JsonObjectDecorator(final Map<String, JsonDecorator> fieldsDecorators) {
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
    return new DecoratedJsonObjectImpl(rawValue.asJsonObject(), fieldsDecorators::get);
  }

  Set<Map.Entry<String, JsonDecorator>> getFieldsDecorators() {
    return fieldsDecorators.entrySet();
  }
}
