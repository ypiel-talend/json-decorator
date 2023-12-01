package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import java.util.ArrayList;
import java.util.Iterator;

public class ArrayFilters {

  public static class ArrayFilter {
    private final JsonDecoratorBuilder.ValueTypeExtended filter;

    private final JsonDecorator decorator;

    public ArrayFilter(final JsonDecoratorBuilder.ValueTypeExtended filter,
        final JsonDecorator decorator) {
      this.filter = filter;
      this.decorator = decorator;
    }

    public boolean isConcerned(JsonValue value) {
      return this.filter == null || this.filter.accept(value);
    }

    public JsonValue apply(JsonValue value) {
      if (this.decorator == null) {
        return value;
      }
      return this.decorator.decorate(value);
    }
  }

  private final ArrayList<ArrayFilter> filters = new ArrayList<>();

  public ArrayFilters(final Iterable<ArrayFilter> filters) {
    filters.forEach(this.filters::add);
  }

  public boolean isConcerned(JsonValue value) {
    return this.findFilter(value) != null;
  }

  public JsonValue apply(JsonValue value) {
    final ArrayFilter filter = findFilter(value);
    if (filter == null) {
      return value;
    }
    return filter.apply(value);
  }

  private ArrayFilter findFilter(JsonValue value) {
    final Iterator<ArrayFilter> iterator = this.filters.iterator();
    while (iterator.hasNext()) {
      ArrayFilter filter = iterator.next();
      if (filter.isConcerned(value)) {
        return filter;
      }
    }
    return null;
  }
}
