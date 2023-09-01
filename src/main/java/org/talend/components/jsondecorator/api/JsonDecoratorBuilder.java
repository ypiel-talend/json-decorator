package org.talend.components.jsondecorator.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.talend.components.jsondecorator.api.cast.JsonDecoratorCastException;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface JsonDecoratorBuilder {

    /**
     * Force, and try to cast, values to the desired type.
     * If the path references an object attribute, it will change its type and cast the value.
     * If the path references an array, it will try to force the type and cast all elements of the array.
     *
     * @param path              The path of the attribute.
     * @param type              The type to cast.
     * @return The builder.
     */
    JsonDecoratorBuilder cast(String path, ValueTypeExtended type);

    /**
     * Force, and try to cast, values to the desired type.
     * If the path references an object attribute, it will change its type and cast the value.
     * If the path references an array, it will try to force the type and cast all elements of the array.
     *
     * @param path              The path of the attribute.
     * @param type              The type to cast.
     * @param forceNullValue    If the value to cast is null, replace it with the given value casted to the desired type.
     * @return The builder.
     */
    JsonDecoratorBuilder cast(String path, ValueTypeExtended type, String forceNullValue);

    /**
     * Keep only values of the selected type.
     * On an object, it keeps only attributes of the selected type (after cast).
     * On an array, it keeps only values of the desired type.
     *
     * @param path The path of the object or the array.
     * @param type The type to keep.
     * @return The builder.
     */
    JsonDecoratorBuilder filterByType(String path, ValueTypeExtended type);

    JsonDecoratorBuilder addDecorator(JsonDecorator decorator);

    JsonValue build(JsonValue json);

    /**
     * Decorate the given jsonValue with the configured decorator.
     *
     * @param json The jsonValue to decorate.
     * @param rootPath The root path of the object, / by default.
     * @return An implementation of decorated JsonValue
     */
    JsonValue build(String rootPath, JsonValue json);

    enum ValueTypeExtended {
        ARRAY(v -> v.getValueType() == JsonValue.ValueType.ARRAY),
        OBJECT(v -> v.getValueType() == JsonValue.ValueType.OBJECT),
        STRING(v -> v.getValueType() == JsonValue.ValueType.STRING),
        FLOAT(v -> v.getValueType() == JsonValue.ValueType.NUMBER && !((JsonNumber)v).isIntegral()),
        INT(v -> v.getValueType() == JsonValue.ValueType.NUMBER && ((JsonNumber)v).isIntegral()),
        BOOLEAN(v -> v.getValueType() == JsonValue.ValueType.TRUE || v.getValueType() == JsonValue.ValueType.FALSE);

        Function<JsonValue, Boolean> accept;

        ValueTypeExtended(Function<JsonValue, Boolean> accept){
            this.accept = accept;
        }

        public boolean accept(JsonValue v){
            return this.accept.apply(v);
        }


    }

    interface JsonDecorator {
        List<JsonDecoratorConfiguration> getConfigurations(String path, JsonDecoratorAction action);

        List<JsonDecoratorConfiguration> getAllConfigurations();

        JsonValue cast(String path, JsonValue delegatedValue) throws JsonDecoratorCastException;

    }

    @AllArgsConstructor
    @Data
    class JsonDecoratorConfiguration {
        private final String path;
        private final JsonDecoratorAction action;
        private final ValueTypeExtended type;
        private final String forceNullValue;
    }

    public enum JsonDecoratorAction {
        CAST, FILTER
    }

}
