package org.talend.components.jsondecorator.impl;

import org.talend.components.jsondecorator.api.DecoratedJsonValue;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class DecoratedJsonValueImpl implements DecoratedJsonValue {

    private final JsonValue delegate;
    private final JsonDecoratorBuilder.JsonDecorator decorator;

    private String path;

    private JsonValue parent;

    private final char separator;

    DecoratedJsonValueImpl(JsonValue delegate, JsonDecoratorBuilder.JsonDecorator decorator, String path, JsonValue parent, char separator) {
        this.delegate = delegate;
        this.decorator = decorator;
        this.path = path;
        this.parent = parent;
        this.separator = separator;
    }

    protected String buildPath(String child) {
        if (this.path == null) {
            this.path = "" + this.separator;
        }

        if (this.path.charAt(this.path.length() - 1) != separator) {
            this.path += this.getSeparator();
        }

        return this.path + child;
    }

    JsonValue getDelegate() {
        return this.delegate;
    }

    @Override
    public char getSeparator() {
        return this.separator;
    }

    @Override
    public ValueType getValueType() {
        return this.delegate.getValueType();
    }

    @Override
    public JsonObject asJsonObject() {
        return new DecoratedJsonObject(this.delegate, this.decorator, this.path, this.getParent(), this.getSeparator());
    }

    @Override
    public JsonArray asJsonArray() {
        return new DecoratedJsonArray(this.delegate, this.decorator, this.path, this.getParent(), this.getSeparator());
    }


    @Override
    public JsonDecoratorBuilder.JsonDecorator getDecorator() {
        return this.decorator;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public JsonValue getParent() {
        return this.parent;
    }

}
