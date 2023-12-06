package org.talend.components.jsondecorator.api;

import javax.json.JsonValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@FunctionalInterface
public interface JsonDecorator {

  JsonValue decorate(JsonValue rawValue);

  class IdentDecorator implements JsonDecorator {
    @Override
    public JsonValue decorate(final JsonValue rawValue) {
      return rawValue;
    }
  }

  interface DecoratorBuilder {
    JsonDecorator build();
  }

  interface ArrayDecoratorBuilder extends DecoratorBuilder {
    ArrayDecoratorBuilder filter(Predicate<JsonValue> filter);

    default ArrayDecoratorBuilder cast(JsonDecorator decorator) {
      return this.decorator(t -> true, decorator);
    }

    default ArrayDecoratorBuilder cast(JsonDecorator.DecoratorBuilder decoratorBuilder) {
      return this.decorator(t -> true, decoratorBuilder);
    }

    ArrayDecoratorBuilder decorator(Predicate<JsonValue> filter, JsonDecorator decorator);

    ArrayDecoratorBuilder decorator(Predicate<JsonValue> filter, JsonDecorator.DecoratorBuilder decoratorBuilder);
  }

  class FieldPath {
    private final List<String> pathElements;

    private FieldPath(final List<String> pathElements) {
      this.pathElements = pathElements;
    }

    public Iterator<String> elements() {
      return this.pathElements.iterator();
    }

    public FieldPath makeChild(FieldPath child) {
      List<String> elements = new ArrayList<>(this.pathElements.size() + child.pathElements.size());
      elements.addAll(this.pathElements);
      elements.addAll(child.pathElements);
      return new FieldPath(elements);
    }

    public static FieldPath.Builder withSeparator(char separator)  {
      return new Builder(separator);
    }

    public static FieldPath.Builder withDefaultSeparator()  {
      return FieldPath.withSeparator('/');
    }

    public static class Builder {
      private final char separator;

      public Builder(final char separator) {
        this.separator = separator;
      }

      public FieldPath from(String data) {
        List<String> fields = new ArrayList<>();
        int index = 0;
        if (data.charAt(0) == separator) {
          index = 1;
        }
        while (index < data.length()) {
          int next = data.indexOf(separator, index);
          if (next > index) {
            fields.add(data.substring(index, next));
            index = next + 1;
          }
          else {
            fields.add(data.substring(index));
            index = data.length();
          }
        }
        return new FieldPath(fields);
      }

    }
  }

  interface ObjectDecoratorBuilder extends DecoratorBuilder {
    ObjectDecoratorBuilder decorateField(String field, JsonDecorator decorator);

    ObjectDecoratorBuilder decorateField(String field, DecoratorBuilder decorator);

    ObjectDecoratorBuilder decorateField(FieldPath field, JsonDecorator decorator);

    ObjectDecoratorBuilder decorateField(FieldPath field, DecoratorBuilder decorator);
  }

  interface ValueDecoratorBuilder extends DecoratorBuilder {
    ValueDecoratorBuilder accept(JsonValue.ValueType inputType);

    ValueDecoratorBuilder defaultValue(JsonValue defaultValue);
  }

  interface BuilderFactory {
    JsonDecorator ident = new JsonDecorator.IdentDecorator();

    JsonDecorator chain(JsonDecorator d1, JsonDecorator d2);

    JsonDecorator chain(JsonDecorator d1, JsonDecorator.DecoratorBuilder d2);

    JsonDecorator chain(JsonDecorator.DecoratorBuilder d1, JsonDecorator d2);

    JsonDecorator chain(JsonDecorator.DecoratorBuilder d1, JsonDecorator.DecoratorBuilder d2);

    ArrayDecoratorBuilder array();

    ObjectDecoratorBuilder object();

    ValueDecoratorBuilder value(ValueTypeExtended toType);

    default JsonDecorator identity() {
      return BuilderFactory.ident;
    }
  }

}
