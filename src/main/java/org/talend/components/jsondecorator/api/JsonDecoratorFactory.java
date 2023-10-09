package org.talend.components.jsondecorator.api;

import javax.json.JsonValue;

public interface JsonDecoratorFactory {

    JsonDecoratorBuilder createBuilder(char separator);

    default JsonDecoratorBuilder createBuilder(){
        return createBuilder('/');
    }

}
