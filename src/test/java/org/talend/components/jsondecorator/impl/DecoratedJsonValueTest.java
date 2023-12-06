package org.talend.components.jsondecorator.impl;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.talend.components.jsondecorator.api.JsonDecorator;
import org.talend.components.jsondecorator.api.JsonDecorator.FieldPath;
import org.talend.components.jsondecorator.api.ValueTypeExtended;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

class DecoratedJsonValueTest {
    private static boolean OUTPUT_JSON_DIFF = false;

    @Test
    void ident() {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator identDecorator = factory.identity();

        JsonValue decoratedJsonValue = identDecorator.decorate(json);

        JsonPatch diff = Json.createDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject());
        Assertions.assertEquals(0, diff.toJsonArray().size());

        JsonNumber content_length_value = decoratedJsonValue.asJsonObject().getJsonNumber("content_length");
        JsonValue.ValueType content_length_type = content_length_value.getValueType();
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, content_length_type);
        Assertions.assertTrue(content_length_value.isIntegral());

        JsonArray contentArray = decoratedJsonValue.asJsonObject().getJsonArray("content");
        contentArray.forEach(e -> {
            JsonObject element = e.asJsonObject();
            Assertions.assertEquals(JsonValue.ValueType.STRING, element.get("tel").getValueType());
        });
    }

    @Test
    void forceTypes() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator addressDecorator = factory.object()
            .decorateField("zipcode", factory.value(ValueTypeExtended.FLOAT).build())
            .build();

        JsonDecorator arrayBag = factory.array()
            .cast(factory.value(ValueTypeExtended.STRING).build())
            .build();

        JsonDecorator contentDecorator = factory.object()
            .decorateField("age", factory.value(ValueTypeExtended.FLOAT))
            .decorateField("name", factory.value(ValueTypeExtended.ARRAY))
            .decorateField("address", addressDecorator)
            .decorateField("tel", factory.value(ValueTypeExtended.INT))
            .decorateField("bag", arrayBag)
            .build();


        JsonDecorator decorator = factory.object()
            .decorateField("content_length", factory.value(ValueTypeExtended.FLOAT))
            .decorateField("content",
                factory.array().decorator(ValueTypeExtended.OBJECT,
                    contentDecorator))
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);


        compareWithJsonDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject(), "/diff/forceTypes2.properties");

        JsonNumber content_length_value = decoratedJsonValue.asJsonObject().getJsonNumber("content_length");
        JsonValue.ValueType content_length_type = content_length_value.getValueType();
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, content_length_type);
        Assertions.assertFalse(content_length_value.isIntegral());

        JsonArray contentArray = decoratedJsonValue.asJsonObject().getJsonArray("content");
        contentArray.forEach(e -> {
            JsonObject element = e.asJsonObject();
            Assertions.assertEquals(JsonValue.ValueType.NUMBER, element.get("tel").getValueType());
        });

    }

    @Test
    void forceTypesWithSeparator() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator contentDecorator = factory.object()
            .decorateField("age", factory.value(ValueTypeExtended.FLOAT))
            .decorateField("name", factory.value(ValueTypeExtended.ARRAY))
            .decorateField(FieldPath.withDefaultSeparator().from("address/zipcode"),
                factory.value(ValueTypeExtended.FLOAT))
            .decorateField("tel", factory.value(ValueTypeExtended.INT))
            .decorateField("bag",
                factory.array().cast(factory.value(ValueTypeExtended.STRING).build()))
            .build();
        JsonDecorator decorator = factory.object()
            .decorateField("content_length", factory.value(ValueTypeExtended.FLOAT))
            .decorateField("content", factory.array().cast(contentDecorator))
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);


        compareWithJsonDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject(), "/diff/forceTypes2.properties");
    }

    @Test
    void testPaths() {
        JsonValue json = TestUtil.loadJson("/json/Object.json");
        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        FieldPath.Builder fp = FieldPath.withSeparator('/');
        JsonDecorator decorator = factory.object()
            .decorateField(fp.from("an_object/nested_bool"),
                factory.value(ValueTypeExtended.STRING))
            .decorateField(fp.from("an_object/nested_int"),
                factory.value(ValueTypeExtended.STRING))
            .decorateField(fp.from("an_object/nested_float"),
                factory.value(ValueTypeExtended.STRING))
            .decorateField(fp.from("an_object/nested_string"),
                factory.value(ValueTypeExtended.ARRAY))
            .decorateField(fp.from("an_object/nested_array"),
                factory.value(ValueTypeExtended.STRING))
            .build();
        JsonValue decoratedJsonValue = decorator.decorate(json);
        JsonObject anObject = decoratedJsonValue.asJsonObject().getJsonObject("an_object");

        Assertions.assertEquals("false", anObject.getString("nested_bool"));
        Assertions.assertEquals("20", anObject.getString("nested_int"));
        Assertions.assertEquals("7.14", anObject.getString("nested_float"));
        Assertions.assertEquals(Json.createArrayBuilder().add("Bye").build(),
            anObject.getJsonArray("nested_string"));
        Assertions.assertEquals("[5,6,7,8]", anObject.getString("nested_array"));
    }


    @Test
    void forceTypesToNullValue() {
        JsonValue json = TestUtil.loadJson("/json/simple.json");

        JsonObject defaultName =
            Json.createObjectBuilder().add("name", Json.createValue("peter")).build();

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator decorator = factory.object()
            .decorateField("address1",
                factory.value(ValueTypeExtended.OBJECT)
                    .defaultValue(defaultName))
            .build();

        JsonObject decoratedJsonValue = decorator.decorate(json).asJsonObject();

        Assertions.assertEquals(JsonValue.ValueType.STRING, decoratedJsonValue.get("name").getValueType());
        JsonValue address1 = decoratedJsonValue.get("address1");
        Assertions.assertEquals(JsonValue.ValueType.OBJECT, address1.getValueType());
        Assertions.assertEquals("peter", address1.asJsonObject().getString("name"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"STRING", "INT", "BOOLEAN"})
    void filterArrayByOneType(String filterTypeStr) throws IOException {
        ValueTypeExtended valueTypeExtended = ValueTypeExtended.valueOf(filterTypeStr);
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator bagDecorator = factory.object().decorateField("bag",
            factory.array().filter(valueTypeExtended)).build();
        JsonDecorator decorator = factory.object().decorateField("content",
                factory.array().decorator(ValueTypeExtended.OBJECT,
                        bagDecorator)
                    .build())
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            bag.forEach(v -> Assertions.assertTrue(valueTypeExtended.test(v)));
            Assertions.assertEquals(3, bag.size());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"INT", "STRING,INT", "BOOLEAN,INT", "BOOLEAN,INT,STRING"})
    void filterArrayByOneSeveralTypes(String filterTypeStrs) throws IOException {
        String[] typesStr = filterTypeStrs.split(",");
        List<ValueTypeExtended> valueTypesExtended = Arrays.stream(typesStr)
                .map(ValueTypeExtended::valueOf)
                .collect(Collectors.toList());

        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");


        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator.ArrayDecoratorBuilder bagArrayBuilder = factory.array();
        valueTypesExtended.forEach(bagArrayBuilder::filter);
        JsonDecorator bagArray = bagArrayBuilder.build();
        JsonDecorator bagObjectDecorator = factory.object().decorateField("bag", bagArray).build();

        JsonDecorator decorator = factory.object().decorateField("content",
            factory.array().decorator(ValueTypeExtended.OBJECT,
                bagObjectDecorator)).build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

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
    void filterArraySeveralTypes(String filterTypesStr) throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");


        List<ValueTypeExtended> types = Arrays.stream(filterTypesStr.split(","))
                .map(String::trim)
                .map(ValueTypeExtended::valueOf)
                .collect(Collectors.toList());

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator.ArrayDecoratorBuilder bagBuilder = factory.array();
        types.forEach(bagBuilder::filter);

        JsonDecorator decorator = factory.object()
            .decorateField("content",
                factory.array().decorator(ValueTypeExtended.OBJECT,
                    factory.object().decorateField("bag", bagBuilder).build()).build())
            .build();


        JsonValue decoratedJsonValue = decorator.decorate(json);

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            Map<JsonValue.ValueType, Long> collect = bag.stream().collect(Collectors.groupingBy(JsonValue::getValueType, Collectors.counting()));
            for (ValueTypeExtended t : types) {
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
    void arrayOfArrayCast() {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator decoratorSecondArray = factory.array().decorator(ValueTypeExtended.ARRAY,
            factory.array().decorator(null, factory.value(ValueTypeExtended.STRING).build())
                .build())
            .build();
        JsonDecorator.ObjectDecoratorBuilder decoratorBuilder = factory.object()
            .decorateField("second_array", decoratorSecondArray);

        JsonDecorator decoratorFourthArray = factory.array().decorator(ValueTypeExtended.ARRAY,
            decoratorSecondArray).build();
        JsonDecorator decorator = decoratorBuilder.decorateField("fourth_array", decoratorFourthArray).build();
        JsonValue decoratedJsonValue = decorator.decorate(json);

        JsonValue jsonValue = decoratedJsonValue.asJsonObject().getJsonArray("first_array").getJsonArray(0).get(0);
        Assertions.assertEquals(JsonValue.ValueType.NUMBER, jsonValue.getValueType());

        List<String> expected = new ArrayList<>(Arrays.asList("1", "2", "one", "two", "three", "3"));
        decoratedJsonValue.asJsonObject().getJsonArray("second_array").getJsonArray(0)
                .forEach(e -> {
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
    void arrayOfArrayCast2() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays2.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator arrayStringItemDecorator = factory.array()
            .cast(factory.value(ValueTypeExtended.STRING))
            .build();
        JsonDecorator decorator = factory.object()
            .decorateField("first_array", arrayStringItemDecorator)
            .decorateField("second_array", arrayStringItemDecorator)
            .decorateField("third_array",
                factory.array().decorator(ValueTypeExtended.ARRAY, arrayStringItemDecorator))
            .build();
        JsonValue decoratedJsonValue = decorator.decorate(json);

            List<String> expected1 = new ArrayList<>(Arrays.asList("1", "2", "three", "4"));
        decoratedJsonValue.asJsonObject().getJsonArray("first_array").forEach(e -> {
            Assertions.assertEquals(JsonValue.ValueType.STRING, e.getValueType());
            Assertions.assertTrue(expected1.remove(((JsonString) e).getString()));
        });
        Assertions.assertTrue(expected1.isEmpty());

        decoratedJsonValue.asJsonObject().getJsonArray("second_array")
                .forEach(e -> Assertions.assertEquals(JsonValue.ValueType.STRING, e.getValueType()));

        decoratedJsonValue.asJsonObject().getJsonArray("third_array")
                .forEach(e -> {
                    Assertions.assertEquals(JsonValue.ValueType.ARRAY, e.getValueType());
                    e.asJsonArray().forEach(i -> Assertions.assertEquals(JsonValue.ValueType.STRING, i.getValueType()));
                });


        JsonPatch diff = Json.createDiff(json.asJsonObject(), decoratedJsonValue.asJsonObject());
        if (OUTPUT_JSON_DIFF) {
            // Display the diff
            diff.toJsonArray().forEach(d -> System.out.println(String.format("%s=%s", d.asJsonObject().getString("path"),
                d)));
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
    void arrayOfArrayFilter(ValueTypeExtended type) {
        JsonValue json = TestUtil.loadJson("/json/arrayOfArrays.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator decorator = factory.object().decorateField("second_array",
                factory.array().decorator(ValueTypeExtended.ARRAY,
                        factory.array().filter(type)))
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

        List<Object> expected = new ArrayList<>();
        if (type == ValueTypeExtended.STRING) {
            expected.addAll(Arrays.asList("one", "two", "three"));
        } else {
            expected.addAll(Arrays.asList(1, 2, 3));
        }
        decoratedJsonValue.asJsonObject().getJsonArray("second_array").getJsonArray(0)
                .forEach(e -> {
                    JsonValue.ValueType jsonType = type == ValueTypeExtended.STRING ? JsonValue.ValueType.STRING : JsonValue.ValueType.NUMBER;
                    Assertions.assertEquals(jsonType, e.getValueType());
                    Object stringValue = type == ValueTypeExtended.STRING ? JsonString.class.cast(e).getString() : JsonNumber.class.cast(e).intValue();
                    Assertions.assertTrue(expected.remove(stringValue), String.format("'%s' not found in expected values.", stringValue));
                });
        Assertions.assertTrue(expected.isEmpty());
    }

    @Test
    void ObjectEntrySet() {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator.ObjectDecoratorBuilder decoratorBuilder = factory.object();

        decoratorBuilder.decorateField("content_length", factory.value(ValueTypeExtended.STRING).build());

        JsonDecorator arrayDecorator = factory.array().decorator(ValueTypeExtended.OBJECT,
            factory.object()
                .decorateField("objects",
                    factory.array().cast(
                        factory.object()
                            .decorateField("aaa", factory.value(ValueTypeExtended.STRING))
                    )
                )
                .decorateField("bag",
                    factory.array()
                        .decorator(null, factory.value(ValueTypeExtended.STRING)))
        ).build();

        decoratorBuilder.decorateField("content", arrayDecorator);
        JsonDecorator decorator = decoratorBuilder.build();
        JsonValue decoratedJsonValue = decorator.decorate(json);

        Set<Map.Entry<String, JsonValue>> entries = decoratedJsonValue.asJsonObject().entrySet();

        Optional<Map.Entry<String, JsonValue>> contentLength = entries.stream().filter(e -> "content_length".equals(e.getKey())).findFirst();
        Assertions.assertTrue(contentLength.isPresent());
        Assertions.assertEquals(JsonValue.ValueType.STRING, contentLength.get().getValue().getValueType());

        JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
        for (JsonObject obj : content.getValuesAs(JsonObject.class)) {
            JsonArray bag = obj.getJsonArray("bag");
            bag.forEach(v -> Assertions.assertEquals(JsonValue.ValueType.STRING, v.getValueType()));

            JsonArray objects = obj.getJsonArray("objects");
            objects.forEach(o -> Assertions.assertEquals(JsonValue.ValueType.STRING, o.asJsonObject().get("aaa").getValueType()));
        }
    }

    @Test
    void multiCast() {
        JsonValue json = TestUtil.loadJson("/json/geologistsComplex.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator chain = factory.chain(factory.value(ValueTypeExtended.STRING).build(),
            factory.value(ValueTypeExtended.ARRAY).build());

        JsonDecorator ageDecorator = factory.array().decorator(ValueTypeExtended.OBJECT,
                factory.object().decorateField("age", chain))
            .build();
        JsonDecorator decorator = factory.object().decorateField("content", ageDecorator).build();
        JsonValue decoratedJsonValue = decorator.decorate(json);

        String[] expecteds = {"35", "49", "55", "70", "40"};

        for (int i = 0; i < expecteds.length; i++) {
            JsonArray content = decoratedJsonValue.asJsonObject().getJsonArray("content");
            JsonArray age = content.getJsonObject(i).getJsonArray("age");
            Assertions.assertEquals(JsonValue.ValueType.ARRAY, age.getValueType());
            JsonValue jsonValue = age.get(0);
            Assertions.assertEquals(JsonValue.ValueType.STRING, jsonValue.getValueType());
            Assertions.assertEquals(expecteds[i], ((JsonString) jsonValue).getString());
        }
    }

    @Test
    void sanitazeArray() {
        JsonValue json = TestUtil.loadJson("/json/arrayToSanitize.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();


        JsonDecorator typesDecorator = factory.object().decorateField("types",
            factory.chain(factory.value(ValueTypeExtended.ARRAY).build(),
                factory.array().decorator(ValueTypeExtended.OBJECT,
                    factory.object().decorateField("default",
                        factory.value(ValueTypeExtended.STRING)))
            )).build();
        JsonDecorator decorator = factory.object().decorateField("data", factory.array().cast(typesDecorator)).build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

        JsonDecorator defaultDecorator = factory.object()
            .decorateField("default", factory.value(ValueTypeExtended.STRING).build())
            .build();
        factory.array().filter(ValueTypeExtended.OBJECT)
            .decorator(null, defaultDecorator).build();


        factory.object().decorateField("types",
            factory.value(ValueTypeExtended.ARRAY).build());

        JsonArray dataArray = decoratedJsonValue.asJsonObject().getJsonArray("data");
        dataArray.forEach(e -> {
            e.asJsonObject().getJsonArray("types").forEach(l -> Assertions.assertEquals(JsonValue.ValueType.OBJECT, l.getValueType()));
        });

        Assertions.assertEquals("String", dataArray
                .getJsonObject(0).getJsonArray("types").getJsonObject(0).getString("name"));

        Assertions.assertEquals("<empty>", dataArray
                .getJsonObject(0).getJsonArray("types").getJsonObject(0).getString("default"));

        Assertions.assertEquals("Int", dataArray
                .getJsonObject(1).getJsonArray("types").getJsonObject(0).getString("name"));

        Assertions.assertEquals("0", dataArray
                .getJsonObject(1).getJsonArray("types").getJsonObject(0).getString("default"));

    }

    @Test
    void objectGetter() {
        JsonValue json = TestUtil.loadJson("/json/Object.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator decorator = factory.object()
            .decorateField("a_bool", factory.value(ValueTypeExtended.STRING))
            .decorateField("a_int", factory.value(ValueTypeExtended.STRING))
            .decorateField("a_float", factory.value(ValueTypeExtended.STRING))
            .decorateField("a_string", factory.value(ValueTypeExtended.ARRAY))
            .decorateField("an_array", factory.value(ValueTypeExtended.STRING))
            .decorateField("an_object", factory.value(ValueTypeExtended.ARRAY))
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

            Assertions.assertEquals("true", decoratedJsonValue.asJsonObject().getString("a_bool"));
        Assertions.assertEquals("10", decoratedJsonValue.asJsonObject().getString("a_int"));
        Assertions.assertEquals("3.14", decoratedJsonValue.asJsonObject().getString("a_float"));
        Assertions.assertEquals("Hello", decoratedJsonValue.asJsonObject().getJsonArray("a_string").getString(0));
        Assertions.assertEquals("[1,2,3,4]", decoratedJsonValue.asJsonObject().getString("an_array"));

        Assertions.assertFalse(decoratedJsonValue.asJsonObject().getJsonArray("an_object")
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

    @Test
    void objectFilterAndCasts() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/geologistsUnconsistentAddresses.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();
        JsonDecorator decorator = factory.object()
            .decorateField("content_length", factory.value(ValueTypeExtended.STRING))
            .decorateField("content",
                factory.array().decorator(ValueTypeExtended.OBJECT,
                    factory.object().decorateField("addresses",
                        factory.array().filter(ValueTypeExtended.OBJECT))))
            .build();

        JsonValue decoratedJsonValue = decorator.decorate(json);

        JsonObject jsonObject = decoratedJsonValue.asJsonObject();
        JsonValue contentLength = jsonObject.get("content_length");
        Assertions.assertEquals(JsonValue.ValueType.STRING, contentLength.getValueType());

        JsonArray content = jsonObject.getJsonArray("content");
        content.forEach(e -> Assertions.assertEquals(3, e.asJsonObject().getJsonArray("addresses").size()));

    }

    private void compareWithJsonDiff(JsonObject o1, JsonObject o2, String expectedResourceProperty) throws IOException {
        JsonPatch diff = Json.createDiff(o1.asJsonObject(), o2.asJsonObject());

        if (OUTPUT_JSON_DIFF) {
            // Display the diff
            diff.toJsonArray().forEach(d -> System.out.println(String.format("%s=%s", d.asJsonObject().getString("path"), d.toString())));
        }

        Map<String, JsonValue> diffMap = diff.toJsonArray().stream().collect(Collectors.toMap(j -> j.asJsonObject().getString("path").toString(), j -> j));
        Properties prop = TestUtil.loadProperties(expectedResourceProperty);

        Assertions.assertAll("Diff",
            prop.entrySet().stream()
                .map(e -> {
                    JsonValue value = Json.createParser(new StringReader(e.getValue().toString())).getValue();
                    return () -> Assertions.assertEquals(value, diffMap.get(e.getKey()));
                }
        ));

        Assertions.assertEquals(prop.size(), diff.toJsonArray().size());
    }

    @Test
    void serializationTest() throws IOException {
        JsonValue json = TestUtil.loadJson("/json/simple.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator decorator = factory.object()
            .decorateField("address1", factory.value(ValueTypeExtended.OBJECT)
                .defaultValue(Json.createObjectBuilder().add("city", "Nantes").build()))
            .decorateField("age", factory.value(ValueTypeExtended.STRING))
            .decorateField("address2", factory.value(ValueTypeExtended.ARRAY))
            .build();
        JsonObject decoratedJsonValue = decorator.decorate(json).asJsonObject();

        String serialized = jsonObjectToString(decoratedJsonValue);

        JsonReader reader = Json.createReader(new StringReader(serialized));
        JsonValue newJsonValue = reader.readValue();

        compareWithJsonDiff(json.asJsonObject(), newJsonValue.asJsonObject(), "/diff/afterSerialization.properties");
    }

    @Test
    void innerObject() {
        JsonValue json = TestUtil.loadJson("/json/Objects.json");

        JsonDecorator.BuilderFactory factory = BuilderFactoryImpl.getInstance();

        JsonDecorator decoratorNestedf1 = factory.object().decorateField("field1", factory.value(ValueTypeExtended.STRING))
            .build();
        JsonDecorator decoratorNestedf2 = factory.object().decorateField("field2", factory.value(ValueTypeExtended.STRING))
            .build();
        FieldPath.Builder fpb = FieldPath.withSeparator('/');
        JsonDecorator decorator = factory.object()
            .decorateField(fpb.from("an_object/nested_object"), decoratorNestedf1)
            .decorateField(fpb.from("an_object/nested_object"), decoratorNestedf2)
            .decorateField(fpb.from("an_object/nested_object/array1"), factory.array().filter(ValueTypeExtended.INT))
            .build();
        JsonValue decorated = decorator.decorate(json);

        JsonObject nestedObject = decorated.asJsonObject().get("an_object")
            .asJsonObject().get("nested_object").asJsonObject();
        Assertions.assertEquals("23", nestedObject.getString("field1"));
        Assertions.assertEquals("true", nestedObject.getString("field2"));
        JsonArray array1 = nestedObject.getJsonArray("array1");
        Assertions.assertEquals(3, array1.size());

        JsonDecorator custom = (JsonValue rawValue) -> {
            if (rawValue == null || rawValue.getValueType() != JsonValue.ValueType.STRING) {
                return rawValue;
            }
            return Json.createValue(((JsonString) rawValue).getString() + "_");
        };
        FieldPath.Builder fp = FieldPath.withDefaultSeparator();
        JsonDecorator decoratorCustom = factory.object()
            .decorateField(fp.from("/an_object/nested_object/field1"), factory.value(ValueTypeExtended.STRING))
            .decorateField(fp.from("an_object/nested_object/field1"), custom)
            .decorateField(fp.from("an_object/nested_object"), decoratorNestedf2)
            .decorateField(fp.from("an_object/nested_object/field2"), custom)
            .build();
        JsonValue decoratedCustom = decoratorCustom.decorate(json);
        JsonObject nestedCustomObject = decoratedCustom.asJsonObject().get("an_object")
            .asJsonObject().get("nested_object").asJsonObject();
        Assertions.assertAll("custom",
            () -> Assertions.assertEquals("23_", nestedCustomObject.getString("field1")),
            () -> Assertions.assertEquals("true_", nestedCustomObject.getString("field2"))
        );
    }

    @Test
    void customDecorator() {
        JsonValue json = TestUtil.loadJson("/json/simple.json");
        JsonDecorator decorator = new MyCustomDecorator();

        JsonValue decorated = decorator.decorate(json);
        Assertions.assertEquals(JsonValue.ValueType.ARRAY, decorated.getValueType());
        Assertions.assertEquals(Json.createValue("Peter"),
            decorated.asJsonArray().get(0).asJsonObject().get("name"));
        Assertions.assertEquals(Json.createValue(32),
            decorated.asJsonArray().get(1).asJsonObject().get("age"));
        Assertions.assertEquals(JsonValue.NULL,
            decorated.asJsonArray().get(2).asJsonObject().get("address1"));
        Assertions.assertEquals(JsonValue.ValueType.OBJECT,
            decorated.asJsonArray().get(3).asJsonObject().get("address2").getValueType());
    }

    static class MyCustomDecorator implements JsonDecorator {

        @Override
        public JsonValue decorate(final JsonValue rawValue) {
            if (rawValue == null || rawValue.getValueType() != JsonValue.ValueType.OBJECT) {
                return rawValue;
            }
            JsonArrayBuilder builder = Json.createArrayBuilder();
            rawValue.asJsonObject().entrySet().forEach(
                e -> {
                    JsonObject item = Json.createObjectBuilder().add(e.getKey(), e.getValue()).build();
                    builder.add(item);
                }
            );
            return builder.build();
        }
    }

    private String jsonObjectToString(JsonObject decoratedJsonValue) {
        StringWriter sw = new StringWriter();
        JsonWriter writer = Json.createWriter(sw);
        writer.write(decoratedJsonValue);
        String serialized = sw.toString();
        return serialized;
    }

}
