package xyz.victorolaitan.easyjson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public static <T> T deserializeToClass(Class<T> aClass, EasyJSON jsonStructure) throws EasyJSONException {
        return deserializeToClass(aClass, jsonStructure.getRootNode());
    }

    private static <T> T deserializeToClass(Class<T> aClass, JSONElement jsonElement) throws EasyJSONException {
        T instance;
        try {
            instance = aClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new EasyJSONException(EasyJSONException.INSTANTIATION_ERROR, e);
        } catch (IllegalAccessException e) {
            throw new EasyJSONException(EasyJSONException.ILLEGAL_ACCESS, e);
        }
        deserializeToInstance(instance, jsonElement);
        return instance;
    }

    public static <T> void deserializeToInstance(T instance, EasyJSON jsonStructure) throws EasyJSONException {
        deserializeToInstance(instance, jsonStructure.getRootNode());
    }

    private static <T> void deserializeToInstance(T instance, JSONElement jsonElement) throws EasyJSONException {
        for (JSONElement childElement : jsonElement) {
            try {
                Field field = instance.getClass().getDeclaredField(childElement.getKey());
                field.setAccessible(true);
                switch (childElement.getType()) {
                    case PRIMITIVE:
                        field.set(instance, childElement.getValue());
                        break;
                    case ARRAY:
                        if (field.getType().isAssignableFrom(List.class)) {
                            field.set(
                                    instance,
                                    extractArrayElements(childElement)
                            );
                        }
                        break;
                    case STRUCTURE:
                        field.set(instance, deserializeToClass(field.getType(), childElement));
                        break;
                    case ROOT:
                        break;
                }
            } catch (IllegalAccessException e) {
                throw new EasyJSONException(EasyJSONException.ILLEGAL_ACCESS, e);
            } catch (NoSuchFieldException e) {
                throw new EasyJSONException(EasyJSONException.FIELD_NOT_FOUND, e);
            } catch (InstantiationException e) {
                throw new EasyJSONException(EasyJSONException.INSTANTIATION_ERROR, e);
            }
        }
    }

    private static List<?> extractArrayElements(JSONElement arrayElement) throws IllegalAccessException, InstantiationException {
        List<Object> values = new ArrayList<>();
        for (JSONElement e : arrayElement) {
            values.add(e.getValue());
        }
        return values;
    }

    public static <T> EasyJSON serializeInstance(T instance, Object... excludeFields) throws EasyJSONException {
        return serializeInstance(instance, EasyJSON.create().getRootNode(), excludeFields).getEasyJSONStructure();
    }

    private static <T> JSONElement serializeInstance(T instance, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        return serialize(instance, instance.getClass(), sourceElement, excludeFields);
    }

    public static <T> EasyJSON serializeClass(Class<T> aClass, Object... excludeFields) throws EasyJSONException {
        return serializeClass(aClass, EasyJSON.create().getRootNode(), excludeFields).getEasyJSONStructure();
    }

    public static <T> JSONElement serializeClass(Class<T> aClass, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        return serialize(null, aClass, sourceElement, excludeFields);
    }

    private static <T> JSONElement serialize(T instance, Class<? extends T> tClass, JSONElement sourceElement, Object... excludeFields) throws EasyJSONException {
        for (Field field : tClass.getDeclaredFields()) {
            try {
                if (!excludeContains(field, instance, excludeFields)) {
                    Object fieldValue = field.get(instance);
                    Class<?> enclosingClass = fieldValue.getClass().getEnclosingClass();
                    if (enclosingClass != null && enclosingClass.equals(tClass)) {
                        // typeof fieldValue == some subclass of tClass
                        sourceElement.putStructure(
                                field.getName(),
                                serialize(fieldValue, fieldValue.getClass(), EasyJSON.create().getRootNode(), excludeFields)
                        );
                    } else if (fieldValue instanceof List) {
                        // typeof fieldValue == List<?>
                        sourceElement.putArray(field.getName(), ((List<?>) fieldValue).toArray());
                    } else {
                        // typeof fieldValue == some class implementing .toString()
                        sourceElement.putPrimitive(field.getName(), fieldValue);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new EasyJSONException(EasyJSONException.ILLEGAL_ACCESS, e);
            }
        }
        return sourceElement;
    }

    private static boolean excludeContains(Field field, Object instance, Object... excludeFields) throws IllegalAccessException {
        Object fieldValue;
        try {
            fieldValue = field.get(instance);
        } catch (NullPointerException ignored) {
            return true; // all instance fields in a static context are excluded
        }
        for (Object e : excludeFields) {
            if (fieldValue == e) {
                return true;
            }
        }
        return false;
    }
}
