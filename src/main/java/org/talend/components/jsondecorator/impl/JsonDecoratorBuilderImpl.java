package org.talend.components.jsondecorator.impl;

import lombok.NonNull;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import javax.json.JsonValue;

public class JsonDecoratorBuilderImpl implements JsonDecoratorBuilder {

    private final Decorator decorator = new Decorator();

    @Override
    public JsonDecoratorBuilder cast(@NonNull String path, @NonNull ValueTypeExtended type) {
        return this.cast(path, type, null);
    }

    @Override
    public JsonDecoratorBuilder cast(@NonNull String path, @NonNull ValueTypeExtended type, String forceNullValue) {
        this.decorator.addJsonDecoratorConfiguration(new JsonDecoratorConfiguration(path, JsonDecoratorAction.CAST, type, forceNullValue));
        return this;
    }

    @Override
    public JsonDecoratorBuilder filterByType(@NonNull String path, @NonNull ValueTypeExtended type) {
        this.decorator.addJsonDecoratorConfiguration(new JsonDecoratorConfiguration(path, JsonDecoratorAction.FILTER, type, null));
        return this;
    }

    @Override
    public JsonDecoratorBuilder addDecorator(JsonDecorator decoratorParam) {
        decoratorParam.getAllConfigurations().stream().forEach(e -> this.decorator.addJsonDecoratorConfiguration(e));
        return this;
    }

    @Override
    public JsonValue build(JsonValue json) {
        return build(null, json);
    }

    public JsonValue build(String rootPath, JsonValue json) {
        if(this.decorator.getAllConfigurations().isEmpty()){
            return json;
        }
        return new DecoratedJsonValueImpl(json, decorator, rootPath, null);
    }

}
