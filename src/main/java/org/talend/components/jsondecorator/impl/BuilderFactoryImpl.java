package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;
import org.talend.components.jsondecorator.api.ValueTypeExtended;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
  public JsonDecorator.ValueDecoratorBuilder value(ValueTypeExtended toType) {
    return new ValueBuilderImpl(toType);
  }

  @Override
  public JsonDecorator chain(final JsonDecorator d1, final JsonDecorator d2) {
    return new ChainedDecorator(d1, d2);
  }

  private static class DecoratorSupplier {
    private final JsonDecorator decorator;

    private final JsonDecorator.DecoratorBuilder decoratorBuilder;

    public DecoratorSupplier(final JsonDecorator decorator) {
      this.decorator = decorator;
      this.decoratorBuilder = null;
    }

    public DecoratorSupplier(final JsonDecorator.DecoratorBuilder decoratorBuilder) {
      this.decorator = null;
      this.decoratorBuilder = decoratorBuilder;
    }

    public JsonDecorator toDecorator() {
      return this.decorator != null ? decorator : (this.decoratorBuilder != null ? this.decoratorBuilder.build() : null);
    }

    JsonDecorator.DecoratorBuilder toBuilder() {
      if (this.decoratorBuilder != null) {
        return this.decoratorBuilder;
      }
      if (this.decorator instanceof JsonObjectDecorator) {
        ObjectBuilderImpl builder = new ObjectBuilderImpl();
        JsonObjectDecorator dec = (JsonObjectDecorator) this.decorator;
        for (Map.Entry<String, JsonDecorator> fieldDec: dec.getFieldsDecorators()) {
          builder.decorateField(fieldDec.getKey(), fieldDec.getValue());
        }
        return builder;
      }
      if (this.decorator instanceof JsonArrayDecorator) {
        ArrayFilters filters = ((JsonArrayDecorator) this.decorator).getFilters();
        return new ArrayBuilderImpl(filters.getFilters());
      }
      return null;
    }
  }

  static class ArrayBuilderImpl implements JsonDecorator.ArrayDecoratorBuilder {

    private final Collection<ArrayFilters.ArrayItemDecorator> filters = new ArrayList<>();

    public ArrayBuilderImpl() {
    }

    ArrayBuilderImpl(final Collection<ArrayFilters.ArrayItemDecorator> filters) {
      this.filters.addAll(filters);
    }

    @Override
    public JsonDecorator.ArrayDecoratorBuilder filter(final Predicate<JsonValue> filter) {
      this.filters.add(new ArrayFilters.ArrayItemDecorator(filter, null));
      return this;
    }

    @Override
    public JsonDecorator.ArrayDecoratorBuilder decorator(final Predicate<JsonValue> filter,
        final JsonDecorator decorator) {
      this.filters.add(new ArrayFilters.ArrayItemDecorator(filter, decorator));
      return this;
    }

    @Override
    public JsonDecorator build() {
      return new JsonArrayDecorator(new ArrayFilters(this.filters));
    }
  }



  static class ObjectBuilderImpl implements JsonDecorator.ObjectDecoratorBuilder {

    private final Map<String, DecoratorSupplier> fieldsDecorator = new HashMap<>();

    @Override
    public JsonDecorator.ObjectDecoratorBuilder decorateField(final String field, final JsonDecorator decorator) {
      this.fieldsDecorator.put(field, new DecoratorSupplier(decorator));
      return this;
    }

    @Override
    public JsonDecorator.ObjectDecoratorBuilder decorateField(final String field,
        final JsonDecorator.DecoratorBuilder decorator) {
      this.fieldsDecorator.put(field, new DecoratorSupplier(decorator));
      return this;
    }

    @Override
    public JsonDecorator build() {
      final Map<String, JsonDecorator> fieldsDecoratorCopy =
          this.fieldsDecorator.entrySet().stream()
              .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toDecorator()));
      return new JsonObjectDecorator(fieldsDecoratorCopy);
    }

    @Override
    public JsonDecorator.ObjectDecoratorBuilder decorateField(final JsonDecorator.FieldPath field,
        final JsonDecorator decorator) {
      this.decorateFields(field.elements(), new DecoratorSupplier(decorator));
      return this;
    }

    @Override
    public JsonDecorator.ObjectDecoratorBuilder decorateField(final JsonDecorator.FieldPath field,
        final JsonDecorator.DecoratorBuilder decorator) {
      this.decorateFields(field.elements(), new DecoratorSupplier(decorator));
      return this;
    }

    private void decorateFields(Iterator<String> elements,
              final DecoratorSupplier decorator) {
      ObjectBuilderImpl current = this;
      while (elements.hasNext()) {
        String next = elements.next();
        if (elements.hasNext()) {
          final DecoratorSupplier nextDecorator;
          if (current.fieldsDecorator.containsKey(next)) {
            nextDecorator = current.fieldsDecorator.get(next);
            JsonDecorator.DecoratorBuilder builder = nextDecorator.toBuilder();
            current.decorateField(next, builder);
            current = (ObjectBuilderImpl) builder;
          }
          else {
            ObjectBuilderImpl nextBuilder = new ObjectBuilderImpl();
            nextDecorator = new DecoratorSupplier(nextBuilder);
            current.fieldsDecorator.put(next, nextDecorator);
            current = nextBuilder;
          }
        }
        else {
          current.fieldsDecorator.put(next, decorator);
        }

      }
    }
  }

  static class ValueBuilderImpl implements JsonDecorator.ValueDecoratorBuilder {

    private final ValueTypeExtended toType;

    private JsonValue defaultValue = null;

    private final List<JsonValue.ValueType> acceptedInput = new ArrayList<>();

    public ValueBuilderImpl(final ValueTypeExtended toType) {
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

  static class ChainedDecorator implements JsonDecorator {
    private final JsonDecorator d1;

    private final JsonDecorator d2;

    public ChainedDecorator(final JsonDecorator d1, final JsonDecorator d2) {
      this.d1 = d1;
      this.d2 = d2;
    }

    @Override
    public JsonValue decorate(final JsonValue rawValue) {
      return d2.decorate(d1.decorate(rawValue));
    }
  }
}
