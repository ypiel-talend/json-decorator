package org.talend.components.jsondecorator.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

class FieldPathTest {

  @ParameterizedTest
  @MethodSource("fieldPathData")
  void fieldPath(String source, List<String> list) {
    JsonDecorator.FieldPath p1 = JsonDecorator.FieldPath.withDefaultSeparator().from(source);
    Iterator<String> elements = p1.elements();
    for (String ref: list) {
      Assertions.assertEquals(ref, elements.next());
    }
    Assertions.assertFalse(elements.hasNext());

    JsonDecorator.FieldPath pnext = JsonDecorator.FieldPath.withSeparator('#').from("#n1#n2#");
    JsonDecorator.FieldPath global = p1.makeChild(pnext);
    List<String> completeList = new ArrayList<>(list);
    completeList.addAll(Arrays.asList("n1", "n2"));

    Iterator<String> elementsAll = global.elements();
    for (String ref: completeList) {
      Assertions.assertEquals(ref, elementsAll.next());
    }
    Assertions.assertFalse(elementsAll.hasNext());
  }

  public static Stream<Arguments> fieldPathData() {
    return Stream.of(
        Arguments.of("Hello/world", Arrays.asList("Hello", "world")),
        Arguments.of("/Hello/world", Arrays.asList("Hello", "world")),
        Arguments.of("/Hello/world/", Arrays.asList("Hello", "world"))
    );
  }

}
