package org.talend.components.jsondecorator.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class DecoratedJsonValueImplTest {

    @Test
    public void ident() {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder.build(json);

        JsonPatch diff = Json.createDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject());
        Assertions.assertEquals(0, diff.toJsonArray().size());

        JsonNumber content_length_value = decoratedJsonValue.asJsonObject().getJsonNumber("content_length");
        JsonValue.ValueType content_length_type = content_length_value.getValueType();
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, content_length_type);
        Assertions.assertTrue(content_length_value.isIntegral());

        JsonArray contentArray = decoratedJsonValue.asJsonObject().getJsonArray("content");
        contentArray.stream().forEach(e -> {
            JsonObject element = e.asJsonObject();
            Assertions.assertEquals(JsonValue.ValueType.STRING, element.get("tel").getValueType());
        });
    }

    @Test
    public void forceTypes() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .cast("/content_length", JsonDecoratorBuilder.ValueTypeExtended.FLOAT)
                .cast("/content/*/age", JsonDecoratorBuilder.ValueTypeExtended.FLOAT)
                .cast("/content/*/name", JsonDecoratorBuilder.ValueTypeExtended.ARRAY)
                .cast("/content/*/address/zipcode", JsonDecoratorBuilder.ValueTypeExtended.FLOAT)
                .cast("/content/*/tel", JsonDecoratorBuilder.ValueTypeExtended.INT)
                .cast("/content/*/bag/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .build(json);

        JsonPatch diff = Json.createDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject());
        if (true) {
            // Display the diff
            diff.toJsonArray().stream().forEach(d -> System.out.println(String.format("%s=%s", d.asJsonObject().getString("path"), d.toString())));
        }

        Map<String, JsonValue> diffMap = diff.toJsonArray().stream().collect(Collectors.toMap(j -> j.asJsonObject().getString("path").toString(), j -> j));

        Properties prop = TestUtil.loadProperties("/diff/forceTypes.properties");
        prop.forEach((k, v) -> Assertions.assertEquals(v, diffMap.get(k).toString()));
        Assertions.assertEquals(prop.size(), diff.toJsonArray().size());

        JsonNumber content_length_value = decoratedJsonValue.asJsonObject().getJsonNumber("content_length");
        JsonValue.ValueType content_length_type = content_length_value.getValueType();
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, content_length_type);
        Assertions.assertFalse(content_length_value.isIntegral());

        JsonArray contentArray = decoratedJsonValue.asJsonObject().getJsonArray("content");
        contentArray.stream().forEach(e -> {
            JsonObject element = e.asJsonObject();
            Assertions.assertEquals(JsonValue.ValueType.NUMBER, element.get("tel").getValueType());
        });

    }

    @Test
    public void forceTypesToNullValue() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/simple.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonObject decoratedJsonValue = builder
                .cast("/address1", JsonDecoratorBuilder.ValueTypeExtended.OBJECT, "{\"name\": \"peter\"}")
                .build(json).asJsonObject();

        Assertions.assertEquals(JsonValue.ValueType.STRING, decoratedJsonValue.get("name").getValueType());
        JsonValue address1 = decoratedJsonValue.get("address1");
        Assertions.assertEquals(JsonValue.ValueType.OBJECT, address1.getValueType());
        Assertions.assertEquals("peter", address1.asJsonObject().getString("name"));

    }

    @ParameterizedTest
    @ValueSource(strings = {"STRING", "INT", "BOOLEAN"})
    public void filterArrayByOneType(String filterTypeStr) throws IOException {
        JsonDecoratorBuilder.ValueTypeExtended valueTypeExtended = JsonDecoratorBuilder.ValueTypeExtended.valueOf(filterTypeStr);
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .filterByType("/content/*/bag", valueTypeExtended)
                .build(json);

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            bag.stream().forEach(v -> Assertions.assertTrue(valueTypeExtended.accept(v)));
            Assertions.assertEquals(3, bag.size());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {"INT", "STRING,INT", "BOOLEAN,INT", "BOOLEAN,INT,STRING"})
    public void filterArrayByOneSeveralTypes(String filterTypeStrs) throws IOException {
        String[] typesStr = filterTypeStrs.split(",");
        List<JsonDecoratorBuilder.ValueTypeExtended> valueTypesExtended = Arrays.stream(typesStr)
                .map(t -> JsonDecoratorBuilder.ValueTypeExtended.valueOf(t))
                .collect(Collectors.toList());

        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        valueTypesExtended.stream().forEach(t -> builder.filterByType("/content/*/bag", t));
        JsonValue decoratedJsonValue = builder.build(json);

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            Assertions.assertEquals(3 * typesStr.length, bag.size());
        }

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "STRING,INT",
            "STRING,BOOLEAN",
            "BOOLEAN,INT",
            "BOOLEAN,INT,STRING"
    })
    public void filterArraySeveralTypes(String filterTypesStr) throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        List<JsonDecoratorBuilder.ValueTypeExtended> types = Arrays.stream(filterTypesStr.split(","))
                .map(s -> s.trim())
                .map(s -> JsonDecoratorBuilder.ValueTypeExtended.valueOf(s))
                .collect(Collectors.toList());

        types.stream()
                .forEach(t ->
                        builder.filterByType("/content/bag", t));

        JsonValue decoratedJsonValue = builder.build(json);

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            Map<JsonValue.ValueType, Long> collect = bag.stream().collect(Collectors.groupingBy(JsonValue::getValueType, Collectors.counting()));
            for (JsonDecoratorBuilder.ValueTypeExtended t : types) {
                switch (t) {
                    case STRING:
                        Assertions.assertEquals(3, collect.get(JsonValue.ValueType.STRING));
                        break;
                    case INT:
                        Assertions.assertEquals(3, collect.get(JsonValue.ValueType.NUMBER));
                        break;
                    case BOOLEAN:
                        Long boolCount = collect.getOrDefault(JsonValue.ValueType.TRUE, 0L)
                                + collect.getOrDefault(JsonValue.ValueType.FALSE, 0L);
                        Assertions.assertEquals(3, boolCount);
                        break;
                }
            }
        }

    }

    @Test
    public void arrayOfArrayCast() {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .cast("/second_array/*/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/fourth_array/*/*/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .build(json);

        JsonValue jsonValue = decoratedJsonValue.asJsonObject().getJsonArray("first_array").getJsonArray(0).get(0);
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, jsonValue.getValueType());

        List<String> expected = new ArrayList<>(Arrays.asList("1", "2", "one", "two", "three", "3"));
        decoratedJsonValue.asJsonObject().getJsonArray("second_array").getJsonArray(0)
                .stream().forEach(e -> {
                    Assertions.assertEquals(JsonValue.ValueType.STRING, e.getValueType());
                    String stringValue = JsonString.class.cast(e).getString();
                    Assertions.assertTrue(expected.remove(stringValue), String.format("'%s' not found in expected values.", stringValue));
                });
        Assertions.assertTrue(expected.isEmpty());

        jsonValue = decoratedJsonValue.asJsonObject().getJsonArray("third_array").getJsonArray(0).getJsonArray(0).get(0);
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, jsonValue.getValueType());

        jsonValue = decoratedJsonValue.asJsonObject().getJsonArray("fourth_array").getJsonArray(0).getJsonArray(0).get(0);
        Assertions.assertEquals(JsonValue.ValueType.STRING, jsonValue.getValueType());
    }

    @Test
    public void arrayOfArrayCast2() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays2.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .cast("/first_array/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/second_array/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/third_array/*/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .build(json);

        List<String> expected1 = new ArrayList<>(Arrays.asList("1", "2", "three", "4"));
        decoratedJsonValue.asJsonObject().getJsonArray("first_array").stream().forEach(e -> {
            Assertions.assertEquals(JsonValue.ValueType.STRING, e.getValueType());
            Assertions.assertTrue(expected1.remove(((JsonString) e).getString()));
        });
        Assertions.assertTrue(expected1.isEmpty());

        decoratedJsonValue.asJsonObject().getJsonArray("second_array").stream()
                .forEach(e -> Assertions.assertEquals(JsonValue.ValueType.STRING, e.getValueType()));

        decoratedJsonValue.asJsonObject().getJsonArray("third_array").stream()
                .forEach(e -> {
                    Assertions.assertEquals(JsonValue.ValueType.ARRAY, e.getValueType());
                    e.asJsonArray().stream().forEach(i -> Assertions.assertEquals(JsonValue.ValueType.STRING, i.getValueType()));
                });


        JsonPatch diff = Json.createDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject());
        if (true) {
            // Display the diff
            diff.toJsonArray().stream().forEach(d -> System.out.println(String.format("%s=%s", d.asJsonObject().getString("path"), d.toString())));
        }

        Map<String, JsonValue> diffMap = diff.toJsonArray().stream().collect(Collectors.toMap(j -> j.asJsonObject().getString("path").toString(), j -> j));

        Properties prop = TestUtil.loadProperties("/diff/arrayOfArrayCast2.properties");
        prop.forEach((k, v) -> Assertions.assertEquals(v, diffMap.get(k).toString()));
        Assertions.assertEquals(prop.size(), diff.toJsonArray().size());


    }

    @ParameterizedTest
    @ValueSource(strings = {
            "STRING",
            "INT"
    })
    public void arrayOfArrayFilter(JsonDecoratorBuilder.ValueTypeExtended type) {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .filterByType("/second_array/*", type)
                .build(json);

        List expected = new ArrayList<>();
        if (type == JsonDecoratorBuilder.ValueTypeExtended.STRING) {
            expected.addAll(Arrays.asList("one", "two", "three"));
        } else {
            expected.addAll(Arrays.asList(1, 2, 3));
        }
        decoratedJsonValue.asJsonObject().getJsonArray("second_array").getJsonArray(0)
                .stream().forEach(e -> {
                    JsonValue.ValueType jsonType = type == JsonDecoratorBuilder.ValueTypeExtended.STRING ? JsonValue.ValueType.STRING : JsonValue.ValueType.NUMBER;
                    Assertions.assertEquals(jsonType, e.getValueType());
                    Object stringValue = type == JsonDecoratorBuilder.ValueTypeExtended.STRING ? JsonString.class.cast(e).getString() : JsonNumber.class.cast(e).intValue();
                    Assertions.assertTrue(expected.remove(stringValue), String.format("'%s' not found in expected values.", stringValue));
                });
        Assertions.assertTrue(expected.isEmpty());
    }

    @Test
    public void ObjectEntrySet() {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder()
                .cast("/content_length", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/content/*/objects/*/aaa", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/content/*/bag/*", JsonDecoratorBuilder.ValueTypeExtended.STRING);
        JsonValue decoratedJsonValue = builder.build(json);

        Set<Map.Entry<String, JsonValue>> entries = decoratedJsonValue.asJsonObject().entrySet();

        Optional<Map.Entry<String, JsonValue>> contentLength = entries.stream().filter(e -> "content_length".equals(e.getKey())).findFirst();
        Assertions.assertTrue(contentLength.isPresent());
        Assertions.assertEquals(JsonValue.ValueType.STRING, contentLength.get().getValue().getValueType());

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            bag.stream().forEach(v -> Assertions.assertEquals(JsonValue.ValueType.STRING, v.getValueType()));

            JsonArray objects = obj.getJsonArray("objects");
            objects.stream().forEach(o -> Assertions.assertEquals(JsonValue.ValueType.STRING, o.asJsonObject().get("aaa").getValueType()));
        }
    }

    @Test
    public void multiCast() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        JsonValue decoratedJsonValue = builder
                .cast("/content/*/age", JsonDecoratorBuilder.ValueTypeExtended.ARRAY)
                .cast("/content/*/age/*", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .build(json);

        String[] expecteds = {"35", "49", "55", "70", "40"};

        for (int i = 0; i < expecteds.length; i++) {
            JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
            JsonValue age = content.getJsonObject(i).getJsonArray("age");
            Assertions.assertEquals(JsonValue.ValueType.ARRAY, age.getValueType());
            JsonValue jsonValue = ((JsonArray) age).get(0);
            Assertions.assertEquals(JsonValue.ValueType.STRING, jsonValue.getValueType());
            Assertions.assertEquals(expecteds[i], ((JsonString) jsonValue).getString());

        }
    }

    @Test
    public void sanitazeArray() {
        JsonValue json = TestUtil.loadJson("/json/arrayToSanitize.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        builder.cast("/data/*/types", JsonDecoratorBuilder.ValueTypeExtended.ARRAY);
        builder.filterByType("/data/*/types", JsonDecoratorBuilder.ValueTypeExtended.OBJECT);
        builder.cast("/data/*/types/*/default", JsonDecoratorBuilder.ValueTypeExtended.STRING);
        JsonValue decoratedJsonValue = builder.build(json);

        decoratedJsonValue.asJsonObject().getJsonArray("data").stream().forEach(e -> {
            e.asJsonObject().getJsonArray("types").stream().forEach(l -> Assertions.assertEquals(JsonValue.ValueType.OBJECT, l.getValueType()));
        });

        Assertions.assertEquals("String", decoratedJsonValue.asJsonObject().getJsonArray("data")
                .getJsonObject(0).getJsonArray("types").getJsonObject(0).getString("name"));

        Assertions.assertEquals("<empty>", decoratedJsonValue.asJsonObject().getJsonArray("data")
                .getJsonObject(0).getJsonArray("types").getJsonObject(0).getString("default"));

        Assertions.assertEquals("Int", decoratedJsonValue.asJsonObject().getJsonArray("data")
                .getJsonObject(1).getJsonArray("types").getJsonObject(0).getString("name"));

        Assertions.assertEquals("0", decoratedJsonValue.asJsonObject().getJsonArray("data")
                .getJsonObject(1).getJsonArray("types").getJsonObject(0).getString("default"));

    }

    @Test
    public void objectGetter() {
        JsonValue json = TestUtil.loadJson("/json/Object.json");

        JsonDecoratorBuilder builder = JsonDecoratorFactoryImpl.getInstance().createBuilder();
        builder.cast("/a_bool", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/a_int", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/a_float", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/a_string", JsonDecoratorBuilder.ValueTypeExtended.ARRAY)
                .cast("/an_array", JsonDecoratorBuilder.ValueTypeExtended.STRING)
                .cast("/an_object", JsonDecoratorBuilder.ValueTypeExtended.ARRAY);
        JsonValue decoratedJsonValue = builder.build(json);

        Assertions.assertEquals("true", decoratedJsonValue.asJsonObject().getString("a_bool"));
        Assertions.assertEquals("10", decoratedJsonValue.asJsonObject().getString("a_int"));
        Assertions.assertEquals("3.14", decoratedJsonValue.asJsonObject().getString("a_float"));
        Assertions.assertEquals("Hello", decoratedJsonValue.asJsonObject().getJsonArray("a_string").getString(0));
        Assertions.assertEquals("[1,2,3,4]", decoratedJsonValue.asJsonObject().getString("an_array"));

        Assertions.assertEquals(false, decoratedJsonValue.asJsonObject().getJsonArray("an_object")
                .getJsonObject(0).getBoolean("nested_bool"));
        Assertions.assertEquals(20, decoratedJsonValue.asJsonObject().getJsonArray("an_object")
                .getJsonObject(0).getInt("nested_int"));
        Assertions.assertEquals(7.14, decoratedJsonValue.asJsonObject().getJsonArray("an_object")
                .getJsonObject(0).getJsonNumber("nested_float").doubleValue());
        Assertions.assertEquals("Bye", decoratedJsonValue.asJsonObject().getJsonArray("an_object")
                .getJsonObject(0).getString("nested_string"));
        JsonArray nestedArray = decoratedJsonValue.asJsonObject().getJsonArray("an_object")
                .getJsonObject(0).getJsonArray("nested_array");
        Assertions.assertEquals(4, nestedArray.size());
        Assertions.assertEquals(5, nestedArray.getInt(0));
        Assertions.assertEquals(6, nestedArray.getInt(1));
        Assertions.assertEquals(7, nestedArray.getInt(2));
        Assertions.assertEquals(8, nestedArray.getInt(3));
    }

}
