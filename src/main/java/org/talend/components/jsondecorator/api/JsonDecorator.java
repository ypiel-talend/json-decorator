package org.talend.components.jsondecorator.api;

import javax.json.JsonValue;

public interface JsonDecorator {

  JsonValue decorate(JsonValue rawValue);

  class IdentDecorator implements JsonDecorator {
    @Override
    public JsonValue decorate(final JsonValue rawValue) {
      return rawValue;
    }
  }

  interface ArrayDecoratorBuilder {
    ArrayDecoratorBuilder filter(JsonDecoratorBuilder.ValueTypeExtended filter);

    ArrayDecoratorBuilder decorator(JsonDecoratorBuilder.ValueTypeExtended filter, JsonDecorator decorator);

    JsonDecorator build();
  }

  interface ObjectDecoratorBuilder {
    ObjectDecoratorBuilder decorator(String field, JsonDecorator decorator);

    JsonDecorator build();
  }

  interface ValueDecoratorBuilder {
    ValueDecoratorBuilder accept(JsonValue.ValueType inputType);

    ValueDecoratorBuilder defaultValue(JsonValue defaultValue);

    JsonDecorator build();
  }

  interface BuilderFactory {
    JsonDecorator ident = new JsonDecorator.IdentDecorator();

    ArrayDecoratorBuilder array();

    ObjectDecoratorBuilder object();

    ValueDecoratorBuilder value(JsonDecoratorBuilder.ValueTypeExtended toType);

    default JsonDecorator ident() {
      return BuilderFactory.ident;
    }
  }

}
