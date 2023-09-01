package org.talend.components.jsondecorator.impl;

import org.talend.components.jsondecorator.api.DecoratedJsonValue;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class DecoratedJsonValueImpl implements DecoratedJsonValue {

    public final static char PATH_SEP = '/';

    private final JsonValue delegate;
    private final JsonDecoratorBuilder.JsonDecorator decorator;

    private String path;

    private JsonValue parent;

    /*DecoratedJsonValueImpl(JsonValue delegate, JsonDecoratorBuilder.JsonDecorator decorator) {
        this(delegate, decorator, null);
    }*/

    DecoratedJsonValueImpl(JsonValue delegate, JsonDecoratorBuilder.JsonDecorator decorator, String path, JsonValue parent) {
        this.delegate = delegate;
        this.decorator = decorator;
        this.path = path;
        this.parent = parent;
    }

    protected String buildPath(String child) {
        if (this.path == null) {
            this.path = "" + PATH_SEP;
        }

        if (this.path.charAt(this.path.length() - 1) != PATH_SEP) {
            this.path += "/";
        }

        return this.path + child;
    }

    JsonValue getDelegate() {
        return this.delegate;
    }

    @Override
    public ValueType getValueType() {
        return this.delegate.getValueType();
    }

    @Override
    public JsonObject asJsonObject() {
        return new DecoratedJsonObject(this.delegate, this.decorator, this.path, this.getParent());
    }

    @Override
    public JsonArray asJsonArray() {
        return new DecoratedJsonArray(this.delegate, this.decorator, this.path, this.getParent());
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
