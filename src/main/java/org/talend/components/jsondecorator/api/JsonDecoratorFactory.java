package org.talend.components.jsondecorator.api;

import javax.json.JsonValue;

public interface JsonDecoratorFactory {

    JsonDecoratorBuilder createBuilder();

}
