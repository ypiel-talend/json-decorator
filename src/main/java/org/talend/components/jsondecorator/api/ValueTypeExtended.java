package org.talend.components.jsondecorator.api;

import javax.json.JsonNumber;
import javax.json.JsonValue;

import java.util.function.Function;
import java.util.function.Predicate;

public enum ValueTypeExtended implements Predicate<JsonValue> {
  ARRAY(v -> v.getValueType() == JsonValue.ValueType.ARRAY),
  OBJECT(v -> v.getValueType() == JsonValue.ValueType.OBJECT),
  STRING(v -> v.getValueType() == JsonValue.ValueType.STRING),
  FLOAT(v -> v.getValueType() == JsonValue.ValueType.NUMBER && !((JsonNumber) v).isIntegral()),
  INT(v -> v.getValueType() == JsonValue.ValueType.NUMBER && ((JsonNumber) v).isIntegral()),
  BOOLEAN(v -> v.getValueType() == JsonValue.ValueType.TRUE || v.getValueType() == JsonValue.ValueType.FALSE);

  Function<JsonValue, Boolean> accept;

  ValueTypeExtended(Function<JsonValue, Boolean> accept) {
    this.accept = accept;
  }

  @Override
  public boolean test(JsonValue v) {
    return this.accept.apply(v);
  }


}
