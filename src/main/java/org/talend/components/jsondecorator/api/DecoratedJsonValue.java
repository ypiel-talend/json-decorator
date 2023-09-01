package org.talend.components.jsondecorator.api;

import javax.json.JsonValue;

public interface DecoratedJsonValue extends JsonValue {

    JsonDecoratorBuilder.JsonDecorator getDecorator();

    String getPath();

    JsonValue getParent();

}
