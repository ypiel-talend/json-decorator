package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;
import org.talend.components.jsondecorator.api.JsonDecoratorBuilder;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class CastFactory {

    private static CastFactory instance;

    private Map<JsonValue.ValueType, Cast> castByType = new HashMap<>();

    public static synchronized CastFactory getInstance() {
        if (instance == null) {
            instance = new CastFactory();
        }
        return instance;
    }

    private CastFactory() {
        // Singleton pattern
    }

    public JsonValue cast(JsonValue v, JsonDecoratorBuilder.JsonDecoratorConfiguration castAttribute) throws JsonDecoratorCastException {
        JsonValue.ValueType valueType = v.getValueType();
        Cast cast = null;
        switch (valueType) {
            case NULL:
                return v;
            case NUMBER:
                cast = castByType.computeIfAbsent(valueType, k -> new CastNumber());
                break;
            case STRING:
                cast = castByType.computeIfAbsent(valueType, k -> new CastString());
                break;
            case FALSE:
            case TRUE:
                cast = castByType.computeIfAbsent(valueType, k -> new CastBoolean());
                break;
            case OBJECT:
                cast = castByType.computeIfAbsent(valueType, k -> new CastObject());
                break;
            case ARRAY:
                cast = castByType.computeIfAbsent(valueType, k -> new CastArray());
                break;
            default:
                cast = castByType.computeIfAbsent(valueType, k -> new CastIdent());
        }

        JsonValue casted = null;
        JsonDecoratorBuilder.ValueTypeExtended castType = castAttribute.getType();
        switch (castType) {
            case ARRAY:
                casted = cast.toArray(v);
                break;
            case BOOLEAN:
                casted = cast.toBoolean(v);
                break;
            case FLOAT:
                casted = cast.toFloat(v);
                break;
            case INT:
                casted = cast.toInt(v);
                break;
            case OBJECT:
                casted = cast.toObject(v);
                break;
            case STRING:
                casted = cast.toString(v);
                break;
            default:
                throw new JsonDecoratorCastException(String.format("Not supported type to cast %s.", castType));
        }

        return casted;
    }

}
