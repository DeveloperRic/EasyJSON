package xyz.victorolaitan.easyjson;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONElementImpl implements JSONElement {
    private EasyJSON easyJSONStructure;
    private JSONElement parent;
    private JSONElementType type;
    private List<JSONElement> children = new ArrayList<>();
    private String key;
    private Object value;

    JSONElementImpl(EasyJSON easyJSONStructure, JSONElement parent, JSONElementType type, String key, Object value) {
        this.easyJSONStructure = easyJSONStructure;
        this.parent = parent;
        this.type = type;
        this.key = key;
        this.value = value;
    }

    @Override
    public EasyJSON getEasyJSONStructure() {
        return easyJSONStructure;
    }

    @Override
    public JSONElement getParent() {
        return parent;
    }

    @Override
    public void mutateAncestry(EasyJSON easyJSONStructure, JSONElement parent) {
        this.easyJSONStructure = easyJSONStructure;
        this.parent = parent;
    }

    @Override
    public JSONElementType getType() {
        return type;
    }

    @Override
    public void setType(SafeJSONElementType type) {
        this.type = type.getRealType();
    }

    @Override
    public List<JSONElement> getChildren() {
        return children;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public void putElement(JSONElement... elements) {
        for (JSONElement element : elements) {
            putElement(element.getKey(), element);
        }
    }

    @Override
    public JSONElement putElement(String key, JSONElement jsonElement) {
        return switch (jsonElement.getType()) {
            case PRIMITIVE -> putPrimitive(key, jsonElement);
            case ARRAY -> putArray(key, jsonElement.getChildren().stream().map(JSONElement::getValue).toArray());
            case STRUCTURE -> putStructure(key, jsonElement);
            case ROOT -> putStructure(null, jsonElement);
        };
    }

    @Override
    public JSONElement putPrimitive(Object value) {
        JSONElementImpl element;
        if (value instanceof JSONElementImpl) {
            element = (JSONElementImpl) value;
            element.parent = this;
        } else {
            element = new JSONElementImpl(easyJSONStructure, this, JSONElementType.PRIMITIVE, null, value);
        }
        children.add(element);
        return element;
    }

    @Override
    public JSONElement putPrimitive(String key, Object value) {
        JSONElement search = search(key);
        if (search == null) {
            JSONElementImpl element;
            if (value instanceof JSONElementImpl) {
                element = (JSONElementImpl) value;
                element.key = key;
                claimElement(element);
            } else {
                element = new JSONElementImpl(easyJSONStructure, this, JSONElementType.PRIMITIVE, key, value);
                children.add(element);
            }
            return element;
        } else {
            if (value instanceof JSONElementImpl) {
                search.merge((JSONElementImpl) value);
            } else {
                search.setValue(value);
            }
            return search;
        }
    }

    @Override
    public JSONElement putStructure(String key) {
        JSONElement element = search(key);
        if (element == null) {
            element = new JSONElementImpl(easyJSONStructure, this, JSONElementType.STRUCTURE, key, null);
            children.add(element);
        } else {
            throw new RuntimeException("EasyJSON: An element already exists with that key!");
        }
        return element;
    }

    @Override
    public JSONElement putStructure(String key, EasyJSON easyJSON) {
        return putStructure(key, easyJSON.getRootNode());
    }

    @Override
    public JSONElement putStructure(String key, JSONElement structure) {
        JSONElement searchResult = search(key);
        if (searchResult == null) {
            structure.setType(SafeJSONElementType.STRUCTURE);
            structure.setKey(key);
            claimElement(structure);
            return structure;
        } else {
            return searchResult.merge(structure);
        }
    }

    @Override
    public JSONElement putArray(String key, Object... items) {
        JSONElement search = search(key);
        if (search == null || search.getType() != JSONElementType.ARRAY) {
            JSONElementImpl element = new JSONElementImpl(easyJSONStructure, this, JSONElementType.ARRAY, key, null);
            for (Object item : items) {
                if (item instanceof JSONElementImpl itemElement) {
                    element.putElement(itemElement.getKey(), itemElement);
                } else {
                    element.putPrimitive(item);
                }
            }
            children.add(element);
            return element;
        } else {
            for (Object item : items) {
                if (item instanceof JSONElementImpl itemElement) {
                    search.putElement(itemElement.getKey(), itemElement);
                } else {
                    search.putPrimitive(item);
                }
            }
            return search;
        }
    }

    @Override
    public void putAll(EasyJSON easyJSONStructure) {
        putAll(easyJSONStructure.getRootNode());
    }

    @Override
    public void putAll(JSONElement jsonElement) {
        for (JSONElement child : jsonElement.getChildren()) {
            putElement(child);
        }
    }

    @Override
    public void claimElement(JSONElement jsonElement) {
        jsonElement.mutateAncestry(easyJSONStructure, this);
        children.add(jsonElement);
    }

    @Override
    public JSONElement merge(JSONElement newElement) {
        type = newElement.getType();
        children = newElement.getChildren();
        value = newElement.getValue();
        return this;
    }

    @Override
    public boolean removeElement(String... location) {
        JSONElement search = search(location);
        if (search != null) {
            search.getParent().getChildren().remove(search);
            search.mutateAncestry(null, null);
            return true;
        } else return false;
    }

    @Override
    public boolean elementExists(String... location) {
        return search(location) != null;
    }

    @Override
    public JSONElement search(String... location) {
        return deepSearch(this, location, 0);
    }

    @Override
    public JSONElement deepSearch(JSONElement element, String[] location, int locPosition) {
        List<JSONElement> children = element.getChildren();
        for (int i = 0; locPosition < location.length && i < children.size(); i++) {
            String currentLoc = location[locPosition];
            JSONElement child = children.get(i);
            boolean childIsAMatch;
            if (element.getType() == JSONElementType.ARRAY) {
                childIsAMatch = String.valueOf(i).equals(currentLoc);
            } else {
                childIsAMatch = child.getKey() != null && child.getKey().equals(location[locPosition]);
            }
            if (childIsAMatch) {
                if (locPosition == location.length - 1) {
                    return child;
                } else {
                    return deepSearch(child, location, locPosition + 1);
                }
            }
        }
        return null;
    }

    @Override
    public Object valueOf(String... location) {
        JSONElement result = search(location);
        return result != null ? result.getValue() : null;
    }

    @Override
    public Iterator<JSONElement> iterator() {
        return children.iterator();
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        try {
            obj = easyJSONStructure.deepSave(obj, this);
        } catch (EasyJSONException e) {
            e.printStackTrace();
            return super.toString();
        }
        return obj.toJSONString();
    }
}
