package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderFactoryImpl implements JsonDecorator.BuilderFactory {

  private BuilderFactoryImpl() {
  }

  private static final JsonDecorator.BuilderFactory instance = new BuilderFactoryImpl();

  public static JsonDecorator.BuilderFactory getInstance() {
    return BuilderFactoryImpl.instance;
  }


  @Override
  public JsonDecorator.ArrayDecoratorBuilder array() {
    return new ArrayBuilderImpl();
  }

  @Override
  public JsonDecorator.ObjectDecoratorBuilder object() {
    return new ObjectBuilderImpl();
  }

  @Override
  public JsonDecorator.ValueDecoratorBuilder value(JsonDecoratorBuilder.ValueTypeExtended toType) {
    return new ValueBuilderImpl(toType);
  }

  static class ArrayBuilderImpl implements JsonDecorator.ArrayDecoratorBuilder {

    private Collection<ArrayFilters.ArrayFilter> filters = new ArrayList<>();

    @Override
    public JsonDecorator.ArrayDecoratorBuilder filter(final JsonDecoratorBuilder.ValueTypeExtended filter) {
      this.filters.add(new ArrayFilters.ArrayFilter(filter, null));
      return this;
    }

    @Override
    public JsonDecorator.ArrayDecoratorBuilder decorator(final JsonDecoratorBuilder.ValueTypeExtended filter,
        final JsonDecorator decorator) {
      this.filters.add(new ArrayFilters.ArrayFilter(filter, decorator));
      return this;
    }

    @Override
    public JsonDecorator build() {
      return new JsonArrayDecorator(new ArrayFilters(this.filters));
    }
  }

  static class ObjectBuilderImpl implements JsonDecorator.ObjectDecoratorBuilder {

    private final Map<String, JsonDecorator> fieldsDecorator = new HashMap<>();

    @Override
    public JsonDecorator.ObjectDecoratorBuilder decorator(final String field, final JsonDecorator decorator) {
      this.fieldsDecorator.put(field, decorator);
      return this;
    }

    @Override
    public JsonDecorator build() {
      Map<String, JsonDecorator> fieldsDecoratorCopy = new HashMap<>(this.fieldsDecorator);
      return new JsonObjectDecorator(fieldsDecoratorCopy::get);
    }
  }

  static class ValueBuilderImpl implements JsonDecorator.ValueDecoratorBuilder {

    private final JsonDecoratorBuilder.ValueTypeExtended toType;

    private JsonValue defaultValue = null;

    private final List<JsonValue.ValueType> acceptedInput = new ArrayList<>();

    public ValueBuilderImpl(final JsonDecoratorBuilder.ValueTypeExtended toType) {
      this.toType = toType;
    }

    @Override
    public JsonDecorator.ValueDecoratorBuilder accept(final JsonValue.ValueType inputType) {
      this.acceptedInput.add(inputType);
      return this;
    }

    @Override
    public JsonDecorator.ValueDecoratorBuilder defaultValue(final JsonValue defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    @Override
    public JsonDecorator build() {
      return new JsonValueDecorator(this.acceptedInput, this.toType, this.defaultValue);
    }

  }
}
