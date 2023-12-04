package org.talend.components.jsondecorator.impl;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.JsonDecorator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;

public class ArrayFilters {

  public static class ArrayItemDecorator {
    private final Predicate<JsonValue> filter;

    private final JsonDecorator decorator;

    public ArrayItemDecorator(final Predicate<JsonValue> filter,
        final JsonDecorator decorator) {
      this.filter = filter;
      this.decorator = decorator;
    }

    public boolean isConcerned(JsonValue value) {
      return this.filter == null || this.filter.test(value);
    }

    public JsonValue apply(JsonValue value) {
      if (this.decorator == null) {
        return value;
      }
      return this.decorator.decorate(value);
    }
  }

  private final ArrayList<ArrayItemDecorator> filters = new ArrayList<>();

  public ArrayFilters(final Iterable<ArrayItemDecorator> filters) {
    filters.forEach(this.filters::add);
  }

  public boolean isConcerned(JsonValue value) {
    return this.findFilter(value) != null;
  }

  public JsonValue apply(JsonValue value) {
    final ArrayItemDecorator filter = findFilter(value);
    if (filter == null) {
      return value;
    }
    return filter.apply(value);
  }

  ArrayList<ArrayItemDecorator> getFilters() {
    return filters;
  }

  private ArrayItemDecorator findFilter(JsonValue value) {
    final Iterator<ArrayItemDecorator> iterator = this.filters.iterator();
    while (iterator.hasNext()) {
      ArrayItemDecorator filter = iterator.next();
      if (filter.isConcerned(value)) {
        return filter;
      }
    }
    return null;
  }
}
