package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;
import org.talend.components.jsondecorator.api.ValueTypeExtended;
import org.talend.components.jsondecorator.api.cast.CastFactory;
import org.talend.components.jsondecorator.api.cast.JsonDecoratorCastException;

import java.util.ArrayList;
import java.util.Collection;

public class JsonValueDecorator implements JsonDecorator {

  private final Collection<JsonValue.ValueType> acceptedSourceTypes = new ArrayList<>();

  private final ValueTypeExtended targetType;

  private final JsonValue defaultValue;

  public JsonValueDecorator(final Collection<JsonValue.ValueType> acceptedSourceTypes,
      final ValueTypeExtended targetType, final JsonValue defaultValue) {
    this.acceptedSourceTypes.addAll(acceptedSourceTypes);
    this.targetType = targetType;
    this.defaultValue = defaultValue;
  }

  @Override
  public JsonValue decorate(final JsonValue rawValue) {
    if (rawValue == null || rawValue.getValueType() == JsonValue.ValueType.NULL) {
      return this.defaultValue;
    }
    if ((!acceptedSourceTypes.isEmpty()) && !acceptedSourceTypes.contains(rawValue.getValueType())) {
      throw new IllegalArgumentException("Not a json accepted type but " + rawValue.getValueType());
    }
    try {
      return CastFactory.getInstance().cast(rawValue, this.targetType);
    }
    catch (JsonDecoratorCastException ex) {
      throw new IllegalArgumentException(ex);
    }
  }
}
