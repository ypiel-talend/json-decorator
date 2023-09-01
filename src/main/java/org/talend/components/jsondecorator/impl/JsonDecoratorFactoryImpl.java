package org.talend.components.jsondecorator.impl;

import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;
import org.talend.components.jsondecorator.api.JsonDecoratorFactory;

public class JsonDecoratorFactoryImpl implements JsonDecoratorFactory {

    private static JsonDecoratorFactoryImpl instance;

    public static synchronized JsonDecoratorFactory getInstance() {
        if (instance == null) {
            instance = new JsonDecoratorFactoryImpl();
        }

        return instance;
    }

    private JsonDecoratorFactoryImpl() {
        // singleton pattern
    }

    @Override
    public JsonDecoratorBuilder createBuilder() {
        return new JsonDecoratorBuilderImpl();
    }


}
