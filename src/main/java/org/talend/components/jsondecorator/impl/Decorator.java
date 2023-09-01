package org.talend.components.jsondecorator.impl;

import lombok.Data;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;
import org.talend.components.jsondecorator.api.cast.CastFactory;
import org.talend.components.jsondecorator.api.cast.JsonDecoratorCastException;

import javax.json.Json;
import javax.json.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
class Decorator implements JsonDecoratorBuilder.JsonDecorator {
    private List<JsonDecoratorBuilder.JsonDecoratorConfiguration> jsonDecoratorConfigurationList = new ArrayList<>();

    void addJsonDecoratorConfiguration(JsonDecoratorBuilder.JsonDecoratorConfiguration jsonDecoratorConfiguration) {
        this.jsonDecoratorConfigurationList.add(jsonDecoratorConfiguration);
    }


    @Override
    public List<JsonDecoratorBuilder.JsonDecoratorConfiguration> getConfigurations(String path, JsonDecoratorBuilder.JsonDecoratorAction action) {
        return this.jsonDecoratorConfigurationList.stream().filter(c -> c.getPath().equals(path) && c.getAction() == action).collect(Collectors.toList());
    }

    @Override
    public List<JsonDecoratorBuilder.JsonDecoratorConfiguration> getAllConfigurations() {
        return Collections.unmodifiableList(this.jsonDecoratorConfigurationList);
    }

    @Override
    public JsonValue cast(String path, JsonValue delegatedValue) throws JsonDecoratorCastException {
        List<JsonDecoratorBuilder.JsonDecoratorConfiguration> castConfigList = this.getConfigurations(path,
                JsonDecoratorBuilder.JsonDecoratorAction.CAST);

        if (castConfigList.isEmpty()) {
            return delegatedValue;
        }

        JsonValue cast = null;
        for (JsonDecoratorBuilder.JsonDecoratorConfiguration castAttribute : castConfigList) {
            if (delegatedValue == JsonValue.NULL && castAttribute.getForceNullValue() != null) {
                delegatedValue = Json.createValue(castAttribute.getForceNullValue());
            }
            cast = CastFactory.getInstance().cast(delegatedValue, castAttribute);
            delegatedValue = cast; // in case we loop over several CAST actions
        }

        return cast;
    }
}
