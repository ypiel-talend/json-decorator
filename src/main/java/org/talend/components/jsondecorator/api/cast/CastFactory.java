package org.talend.components.jsondecorator.api.cast;

import org.talend.components.jsondecorator.api.Cast;

import javax.json.JsonValue;
import org.talend.components.jsondecorator.api.ValueTypeExtended;

import java.util.HashMap;
import java.util.Map;

public class CastFactory {

    private static CastFactory instance;

    private Map<JsonValue.ValueType, Cast> castByType = new HashMap<>();

    private final CastIdent ident = new CastIdent();

    public static synchronized CastFactory getInstance() {
        if (instance == null) {
            instance = new CastFactory();
        }
        return instance;
    }

    private CastFactory() {
        castByType.put(JsonValue.ValueType.NUMBER, new CastNumber());
        castByType.put(JsonValue.ValueType.STRING, new CastString());

        final CastBoolean castBoolean = new CastBoolean();
        castByType.put(JsonValue.ValueType.FALSE, castBoolean);
        castByType.put(JsonValue.ValueType.TRUE, castBoolean);

        castByType.put(JsonValue.ValueType.OBJECT, new CastObject());
        castByType.put(JsonValue.ValueType.ARRAY, new CastArray());
    }

    public JsonValue cast(JsonValue v, ValueTypeExtended targetType) throws JsonDecoratorCastException {
        JsonValue.ValueType valueType = v.getValueType();
        Cast cast = this.find(valueType);

        JsonValue casted = null;

        switch (targetType) {
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
            throw new JsonDecoratorCastException(String.format("Not supported type to cast %s.", targetType));
        }

        return casted;
    }

    private Cast find(JsonValue.ValueType valueType) {
        Cast cast = this.castByType.get(valueType);
        if (cast == null) {
            return ident;
        }
        return cast;
    }

}
